/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package retriever;

/**
 *
 * @author Debasis
 */

import evaluator.Evaluator;
import indexing.TrecDocIndexer;
import static indexing.TrecDocIndexer.FIELD_ANALYZED_CONTENT;
import static indexing.TrecDocIndexer.FIELD_ID;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.BytesRef;
import trec.*;
import wvec.WordVecs;
import feedback.ImplicitFeedback;

/**
 *
 * @author Debasis
 */

public class TrecDocRetriever {

    TrecDocIndexer indexer;
    IndexReader reader;
    IndexSearcher searcher;
    int numWanted;
    Properties prop;
    String runName;
    String kdeType;
    boolean postRLMQE;
    boolean postQERerank;
    FileWriter fw;
    FileReader docs = new FileReader("docs.ids");
    BufferedReader readerDocs = new BufferedReader(docs);
    int numDocs = 1692096;
    String [][] map = new String[numDocs][3];
    
    ArrayList<String[]> links = new ArrayList<String[]>();
    
    public TrecDocRetriever(String propFile) throws Exception {
        indexer = new TrecDocIndexer(propFile);
        prop = indexer.getProperties();
        
        try {
            File indexDir = indexer.getIndexDir();
            System.out.println("Running queries against index: " + indexDir.getPath());
            
            String mappingURLFile = "wt10g/info/docid_to_url";
            FileReader fr = new FileReader(mappingURLFile);
            BufferedReader br = new BufferedReader(fr);
            for(int i=0; i<numDocs; i++){
                map[i][0] = "";
                map[i][1] = "";
            }
            makeMap(br);
            
            //fw = new FileWriter("docs.ids");
            
            reader = DirectoryReader.open(FSDirectory.open(indexDir));
            searcher = new IndexSearcher(reader);
            
            float lambda = Float.parseFloat(""+0.4);
            searcher.setSimilarity(new BM25Similarity());
            
            numWanted = 150;
            runName = "zettair";
            
            
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }        
    }
    
    public Properties getProperties() { return prop; }
    public IndexReader getReader() { return reader; }
    
    public List<TRECQuery> constructQueries() throws Exception {        
        String queryFile = "wt10g/topics/topics.451-500.txt";
        TRECQueryParser parser = new TRECQueryParser(queryFile, indexer.getAnalyzer());
        parser.parse();
        return parser.getQueries();
    }    
    
    public void makeMap(BufferedReader br) throws IOException{
        
        /*while(linha != null){
            int indice = hash(linha[0]);
            System.out.println(indice);
            if(map[indice][0].compareTo("")==0){
                map[indice][0] = linha[0];
                map[indice][1] = linha[1];  
            } else {
                mapColisionArea.add(linha);
            }
            linha = br.readLine().split(" ");
        }*/
        
        for (int i=0; i<map.length; i++){
            String [] linha = br.readLine().split(" ");
            map[i][0] = linha[0];
            map[i][1] = linha[1];
        }
    }
    
    public int hash(String term){
        int resp = 1;
        
        for(int i=0; i<term.length(); i++){
            int t = term.charAt(i)%5;
            
            //System.out.println((int)term.charAt(i));            
            if(t!=0){
                resp *= t;
            } 
        }
        
        return resp % numDocs;
    }
    
    public ArrayList<String[]> retrieve(String terms) throws Exception{
        
        String resultsFile = "Resp/naotrec.res";   
        
        FileWriter fw = new FileWriter(resultsFile);
                
        TopScoreDocCollector collector;
        TopDocs topDocs;
        
        String expanded = applyImplicitFeedback(terms);
        
        StandardQueryParser queryParser = new StandardQueryParser(indexer.getAnalyzer());
        Query luceneQuery = queryParser.parse(expanded, TrecDocIndexer.FIELD_ANALYZED_CONTENT);
        
        
        
        collector = TopScoreDocCollector.create(numWanted, true);
        searcher.search(luceneQuery, collector);
        topDocs = collector.topDocs();
        
        saveRetrievedTuples(fw, topDocs);
        
        fw.close();
        return links;
             
    }
    
    private String applyImplicitFeedback(String terms) throws IOException {
        
        ImplicitFeedback expansor = new ImplicitFeedback(terms, reader, prop);
        return expansor.apply(3);
    }
    
    public void retrieveAll() throws Exception {
        TopScoreDocCollector collector;
        TopDocs topDocs;
        String resultsFile = "Resp/trec9.res";        
        FileWriter fw = new FileWriter(resultsFile);
        
        List<TRECQuery> queries = constructQueries();   

        //***********************************************************************************************
        BM25Similarity sim = new BM25Similarity();
        searcher.setSimilarity(sim);
        int k = 5; // top 5
        for (TRECQuery query : queries) {

            // Print query
            System.out.println(query.getLuceneQueryObj());
            //System.out.println("----"+FIELD_ANALYZED_CONTENT);
            // Retrieve results
            collector = TopScoreDocCollector.create(numWanted, true);
            searcher.search(query.getLuceneQueryObj(), collector);
            topDocs = collector.topDocs();
            System.out.println("Retrieved results for query " + query.id);
            
            
            // Cria o arquivo de TermVectors
            //createTermVectorsFile();

            // Apply feedback            
            topDocs = applyFeedback2 (query, topDocs, k);            
            
            // Save results
            saveRetrievedTuples(fw, query, topDocs);
        }
        //**********************************************************************************************
              
        //this.fw.close();
        fw.close();        
        reader.close();
        
        // Evaluate
        evaluate();
        
    }
    
    public void evaluate() throws Exception {
        Evaluator evaluator = new Evaluator(this.getProperties());
        evaluator.load();
        evaluator.fillRelInfo();
        System.out.println(evaluator.computeAll());        
    }
    
    public void saveRetrievedTuples(FileWriter fw, TRECQuery query, TopDocs topDocs) throws Exception {
        StringBuffer buff = new StringBuffer();
        ScoreDoc[] hits = topDocs.scoreDocs;
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            
            buff.append(query.id.trim()).append("\tQ0\t").
                    append(d.get(TrecDocIndexer.FIELD_ID)).append("\t").
                    append((i+1)).append("\t").
                    append(hits[i].score).append("\t").
                    append(runName).append("\n");                
        }
        fw.write(buff.toString());        
    }
    
    public void saveRetrievedTuples(FileWriter fw, TopDocs topDocs) throws Exception {
        StringBuffer buff = new StringBuffer();
        ScoreDoc[] hits = topDocs.scoreDocs;
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
          /* Tentativa com Hash  
           int indice = hash(d.get(TrecDocIndexer.FIELD_ID));
           if(map[indice][0].compareTo(d.get(TrecDocIndexer.FIELD_ID))==0){
               links.add(map[indice][1]);
           } else {
               for(int j=0; j<links.size(); j++){                                 
                   if(mapColisionArea.get(j)[0].compareTo(d.get(TrecDocIndexer.FIELD_ID))==0){
                       links.add(mapColisionArea.get(j)[1]);
                       break;
                   }
               }
           }*/
            
            // Tentativa com busca binaria ***FICOU MUITO MELHOR****
            int index = binarySearch(d.get(TrecDocIndexer.FIELD_ID), 0, map.length-1);
            map[index][2] = ""+docId;
            links.add(map[index]);
            
            buff.append(d.get(TrecDocIndexer.FIELD_ID)).append("\t").
                 append((i+1)).append("\t").
                 append(hits[i].score).append("\n");   
            
        }
        fw.write(buff.toString());        
    }
    
    public int binarySearch(String id, int init, int end){
        
        int pos = (end + init)/2;
        int comp = map[pos][0].compareTo(id);
        //System.out.println(map[pos][0]+" "+id+" "+pos);
        if(comp > 0){
            return binarySearch(id, init, pos-1);
        } else if(comp == 0){
            return pos;
        } else {
            return binarySearch(id, pos+1, end);
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            args = new String[1];
            args[0] = "init.properties";
        }
        try {
            TrecDocRetriever searcher = new TrecDocRetriever(args[0]);
            searcher.retrieveAll();            
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

   
    private TopDocs applyFeedback2(TRECQuery query, TopDocs topDocs, int k) throws IOException, QueryNodeException {
        StringBuffer buff = new StringBuffer();
        FileReader fr = new FileReader("wt10g/topics/qrels.trec9.main_web");
        
        BufferedReader br = new BufferedReader(fr);
        
        String line = br.readLine();
        String[] tokens = line.split(" ");
        boolean foundQuery = false;
        int count = 0;
        
        while (tokens[0].compareTo(query.id.trim())==0 || !foundQuery){
            
            foundQuery = tokens[0].compareTo(query.id.trim())==0;
            //System.out.println("-->"+foundQuery+" "+tokens[2]);
            
            if(foundQuery){
                
                
                int relevance = Integer.parseInt(tokens[3]);
                                
                if(relevance > 0){
                   /* boolean foundDoc = false;
                    
                    // Primeira Vez - Montagem do arquivo de ids
                    for(int i=0; i<reader.numDocs() && !foundDoc; i++){
                        if(tokens[2].compareTo(reader.document(i).get(TrecDocIndexer.FIELD_ID)) == 0){
                            foundDoc = true;
                            Terms terms = reader.getTermVector(i, TrecDocIndexer.FIELD_ANALYZED_CONTENT);
                            buff.append(" ").append(terms.getMax().utf8ToString()); //adicionando o termo mais frequente na query
                            this.fw.write(i+"\n");
                            this.fw.flush();
                            
                        }                        
                    }
                    */
                    // Demais vezes - doc.ids pronto
                    int doc = Integer.parseInt(readerDocs.readLine());
                    if(count < 5){
                        Terms terms = reader.getTermVector(doc, TrecDocIndexer.FIELD_ANALYZED_CONTENT);
                        //buff.append(" ").append(terms.getMin().utf8ToString()); //adicionando o termo mais frequente na query
                        
                        //*********************************************************************************************************
                        // access the terms for this field
                        TermsEnum termsEnum = terms.iterator(null); 
                        BytesRef term = null;
                           int termCount = 0;
                        // explore the terms for this field
                        while ((term = termsEnum.next()) != null) {
                            if(Math.random() < 0.03){
                                buff.append(" ").append(term.utf8ToString()); 
                                termCount++;
                            }
                        }
                        //************************************************************************************************************
                        
                        /*
                        if(topDocs.totalHits > count){                            
                            terms = reader.getTermVector(topDocs.scoreDocs[count].doc, TrecDocIndexer.FIELD_ANALYZED_CONTENT);
                            buff.append(" ").append(terms.getMax().utf8ToString()); //adicionando o termo mais frequente na query
                        }*/
                        count++;
                        //buff.append(" ").append(query.title);                          
                    }
                }                
            } 
            
            line = br.readLine();
            if(line!=null){
                tokens = line.split(" ");
            }else tokens[0] = "FIM";
            
        }
        //buff.append(" ").append(query.title);
        
       /* for(int i=0; i<k; i++){
            if(topDocs.totalHits > i){
                Terms terms = reader.getTermVector(topDocs.scoreDocs[i].doc, "words");
                buff.append(" ").append(terms.getMax().utf8ToString()); //adicionando o termo mais frequente na query
                if (terms != null && terms.size() > 0) {
                    // access the terms for this field
                    TermsEnum termsEnum = terms.iterator(null); 
                    BytesRef term = null;
                       int count = 0;
                    // explore the terms for this field
                    while ((term = termsEnum.next()) != null && count <= 5) {
                        buff.append(" ").append(term.utf8ToString()); 
                        count++;
                    }
                }
            }
        }*/
        
        query.title += buff.toString();
        System.out.println("Expandindo a Consulta com: "+buff.toString());
        //System.out.println("-------------->"+reader.document(500).get("id"));
        StandardQueryParser parser = new StandardQueryParser();
        
        query.luceneQuery = parser.parse(query.title, TrecDocIndexer.FIELD_ANALYZED_CONTENT);
        //query.luceneQuery = searcher.rewrite(query.getLuceneQueryObj());        
        
        TopScoreDocCollector collector;
        TopDocs topDocs2;
        collector = TopScoreDocCollector.create(numWanted, true);  
                
        searcher.search(query.getLuceneQueryObj(), collector);
        topDocs2 = collector.topDocs();
        
        return topDocs2;
    }

    public void saveLog(ArrayList<String> queries, ArrayList<String> documents, ArrayList<String> times) throws IOException {
        String log = "log/log.log";
        GregorianCalendar gc = new GregorianCalendar();
        FileWriter fw = new FileWriter(log, true);
        StringBuffer buffer = new StringBuffer();
        
        for(int i=0; i<queries.size(); i++){
            buffer.append(gc.getTime().toString()).append("\t").
                    append(queries.get(i)).append("\t").
                    append(documents.get(i)).
                        append(times.get(i)).append("\n");
            
            fw.write(buffer.toString());
            buffer = new StringBuffer();
        }
        fw.close();
    }

   
}
