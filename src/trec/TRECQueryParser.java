/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trec;

/**
 *
 * @author Debasis
 */
import indexing.TrecDocIndexer;
import java.io.FileReader;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;
import java.util.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import retriever.NNQueryExpander;

public class TRECQueryParser extends DefaultHandler {
    StringBuffer        buff;      // Accumulation buffer for storing the current topic
    String              fileName;
    TRECQuery           query;
    Analyzer            analyzer;
    StandardQueryParser queryParser;
    
    public List<TRECQuery>  queries;
    final static String[] tags = {"id", "title", "desc", "narr"};

    public TRECQueryParser(String fileName, Analyzer analyzer) throws SAXException {
        this.fileName = fileName;
        this.analyzer = analyzer;
        buff = new StringBuffer();
        queries = new LinkedList<>();
        queryParser = new StandardQueryParser(analyzer);
    }

    public StandardQueryParser getQueryParser() { return queryParser; }
    
    public void parse() throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setValidating(false);
        SAXParser saxParser = saxParserFactory.newSAXParser();
        System.out.println(fileName);
        saxParser.parse(fileName, this);
    }

    public List<TRECQuery> getQueries() { return queries; }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            if (qName.equalsIgnoreCase("top")) {
                query = new TRECQuery();
                queries.add(query);
            }
            else
                buff = new StringBuffer();
        }
        catch (Exception ex) { ex.printStackTrace(); }
    }
    
    public Query constructLuceneQueryObj(TRECQuery trecQuery) throws QueryNodeException {        
        Query luceneQuery = queryParser.parse(trecQuery.title, TrecDocIndexer.FIELD_ANALYZED_CONTENT);
        return luceneQuery;
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if (qName.equalsIgnoreCase("title"))
                query.title = buff.toString();            
            else if (qName.equalsIgnoreCase("desc"))
                query.desc = buff.toString();
            else if (qName.equalsIgnoreCase("narr"))
                query.narr = buff.toString();
            else if (qName.equalsIgnoreCase("num"))
                query.id = buff.toString();
            else if (qName.equalsIgnoreCase("top"))
                query.luceneQuery = constructLuceneQueryObj(query);  
            
            //System.out.println(buff.toString());
        }
        catch (Exception ex) { ex.printStackTrace(); }
    }
    
    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        buff.append(new String(ch, start, length));
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            args = new String[1];
            args[0] = "wt10g.properties";
        }

        try {
            Properties prop = new Properties();
            prop.load(new FileReader(args[0]));
            String queryFile = "wt10g/topics/topics.451-500.txt";
            
            TRECQueryParser parser = new TRECQueryParser(queryFile, new EnglishAnalyzer(Version.LUCENE_4_9));
            parser.parse();
            System.out.println("Before expansion");
            for (TRECQuery q : parser.queries) {
                System.out.println(q);
            }
            
            NNQueryExpander qc = new NNQueryExpander(prop);
            qc.expandQueriesWithNN(parser.queries);

            System.out.println("After expansion");
            for (TRECQuery q : parser.queries) {
                System.out.println(q);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}    
