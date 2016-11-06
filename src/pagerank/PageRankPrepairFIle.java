/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pagerank;

import indexing.TrecDocIndexer;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import retriever.TrecDocRetriever;
import retriever.WTDocRetriever;

/**
 * Cria um arquivo com informações a serem utilizadas no calculo de PageRank
 * @author Herbert
 */
public class PageRankPrepairFIle {
    
    
    
    public static void main(String[] args){
        try {
            
            TrecDocRetriever retriever = new WTDocRetriever("wt10g.properties");
            
            FileReader frIn = new FileReader("wt10g/info/in_links");
            BufferedReader brIn = new BufferedReader(frIn);
            
            FileReader frOut = new FileReader("wt10g/info/out_links");
            BufferedReader brOut = new BufferedReader(frOut);
            
            FileWriter fw = new FileWriter("pagerank/pagerank.pr");
            
            ArrayList<DocumentEdges> docs;
            
            ArrayList<Links> in = new ArrayList<Links>();
            ArrayList<Links> out = new ArrayList<Links>();
            
            IndexReader reader = retriever.getReader();
            docs = new ArrayList<DocumentEdges>(reader.numDocs());
            System.out.println(reader.numDocs());
            
            ArrayList<DocumentEdges> teste = new ArrayList<DocumentEdges>();
            for(int i=0; i<reader.numDocs(); i++){
                DocumentEdges doc = new DocumentEdges();
                doc.setDocument(reader.document(i).get(TrecDocIndexer.FIELD_ID)); 
                doc.setDocumentID(i);
                docs.add(doc);
                teste.add(doc);                
            }
            Collections.sort(teste);
            
            int count = 0;
            String line = brIn.readLine();
            while(line != null){
                Links l = new Links(line, teste, true);                
                in.add(l);
                line = brIn.readLine();
                count++;
                if(count%1000 == 0)
                System.out.println(l.document);
            }
            
            count = 0;
            line = brOut.readLine();
            while(line != null){
                Links l = new Links(line, teste, false);                
                out.add(l);
                line = brOut.readLine();
                count++;
                if(count%1000 == 0)
                System.out.println(l.document);
            }
                                    
            for(int i=0; i<docs.size(); i++){
                System.out.println(i);  
                
                int index = binarySearch(docs.get(i).getDocument(), 0, in.size()-1, in);
                
                if(index >= 0){                    
                    docs.get(i).setInLinksID(in.get(index).links);                    
                }
                
                index = binarySearch(docs.get(i).getDocument(), 0, in.size()-1, out);
                if(index >= 0){
                    docs.get(i).setOutLinks(out.get(index).links.size());   
                } else{
                    docs.get(i).setOutLinks(0);
                }
                
            }
                        
            write(fw, docs);
            fw.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PageRankPrepairFIle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PageRankPrepairFIle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PageRankPrepairFIle.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static int binarySearch(String id, int init, int end, ArrayList<Links> list) {
        
        int pos = (end + init)/2;
        int comp = 0;
        
        if(end >= 0 && init < list.size() && init <= end && pos >= 0 && pos < list.size()){            
            comp = list.get(pos).document.compareTo(id);
        } else {
            return -1;
        }
       
        if(comp > 0){
            return binarySearch(id, init, pos-1, list);
        } else if(comp == 0){
            return pos;
        } else {
            return binarySearch(id, pos+1, end, list);
        }
    }

    private static void write(FileWriter fw, ArrayList<DocumentEdges> docs) throws IOException {
        
        for(int i=0; i<docs.size(); i++){
            
            StringBuffer buffer = new StringBuffer();
            buffer.append(docs.get(i).getDocument()).append(" ");
            
            buffer.append(docs.get(i).getInLinksID().size()).append(" ");
            
            for(int j=0; j<docs.get(i).getInLinksID().size(); j++){
                buffer.append(docs.get(i).getInLinksID().get(j)).append(" ");
            }
            
            buffer.append(docs.get(i).getOutLinks()).append("\n");
            fw.write(buffer.toString());
            
        }        
    }    
}
