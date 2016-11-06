/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package feedback;

import indexing.TrecDocIndexer;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author Herbert
 */
public class ImplicitFeedback {
    
    String terms;
    IndexReader reader;
    Properties prop;
    ArrayList<LogInstance> log;

    public ImplicitFeedback(String terms, IndexReader reader, Properties prop) {        
        this.terms = terms;
        this.reader = reader;
        this.prop = prop;
        log = new ArrayList<LogInstance>();
        
        loadLog();               
    }
    
    /**
     * Carrega o arquivo de log de consultas
     */
    public void loadLog() {
        
        FileReader fr = null;
        try {
            String file = "log/log.log";
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            
            while(line != null){
                LogInstance li = new LogInstance(line);
                log.add(li);
                line = br.readLine();
            }            
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImplicitFeedback.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImplicitFeedback.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                Logger.getLogger(ImplicitFeedback.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Adiciona termos dos k top documentos para a consulta informada
     * @param k quantidade de documentos a extrair termos
     * @return nova consulta
     * @throws IOException 
     */
    public String apply(int k) throws IOException {
        System.out.println("Consulta Original -> "+terms);
        
        ArrayList<LogInstance> relDocs = new ArrayList<LogInstance>();
        for(int i=0; i<log.size(); i++){
            if(terms.contains(log.get(i).query)){
                relDocs.add(log.get(i));
            }
        }
        
        for(int i=0; i<relDocs.size(); i++){
            
            for(int j=0; j<k && j<relDocs.get(i).documents.length; j++){
                
                Terms terms = reader.getTermVector(Integer.parseInt(relDocs.get(i).documents[j]), TrecDocIndexer.FIELD_ANALYZED_CONTENT);
                
                TermsEnum termsEnum = terms.iterator(null); 
                BytesRef term = null;
                StringBuffer termBuff = new StringBuffer();
                StringBuffer freqBuff = new StringBuffer();
                // explore the terms for this field
                
                term = termsEnum.next();
                if(term != null){
                    termBuff.append(term.utf8ToString()); 
                    freqBuff.append(termsEnum.totalTermFreq());
                }
                
                while (term != null) {                    
                    termBuff.append(" ").append(term.utf8ToString()); 
                    freqBuff.append(" ").append(termsEnum.totalTermFreq());
                        //System.out.println(freqBuff); 
                    term = termsEnum.next();
                }
                String[] termStrings = termBuff.toString().split(" ");
                String[] tf = freqBuff.toString().split(" ");
                int[] termFrequency = new int[tf.length];
                List<TermFreq> result = new ArrayList<TermFreq>(tf.length);
                for(int x=0; x< tf.length; x++){
                    termFrequency[x] = Integer.parseInt(tf[x]);
                    result.add(new TermFreq(termStrings[x], termFrequency[x]));
                }
                Collections.sort(result);
                result = result.subList(0, 4);
                for(int m=0; m<result.size(); m++){
                    this.terms += " "+result.get(m).term;
                }   
            }
        }
        System.out.println("Nova Consulta -> "+terms);
        return terms;
    }
    
}
