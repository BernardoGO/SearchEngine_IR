/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pagerank;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Herbert
 */
public class PageRank {
    static final int ITERATIONS = 2;        // Iterações
    static final double d = 0.85;           // fator de amortecimento
    
    static ArrayList<DocumentEdges> docs = new ArrayList<DocumentEdges>();
    static ArrayList<Double> scores = new ArrayList<Double>();
    
    public static void main(String[] args){
        FileReader fr = null;
        try {
            
            fr = new FileReader("pagerank/pagerank.pr");
            BufferedReader br = new BufferedReader(fr);            
            
            String line = br.readLine();
            
            while(line != null){
                String[] tokens = line.split(" ");
                
                DocumentEdges de = new DocumentEdges();
                de.setDocument(tokens[0]);
                
                ArrayList<Integer> inLinks = new ArrayList<Integer>();
                for (int i=2; i<tokens.length-1; i++){
                    inLinks.add(Integer.parseInt(tokens[i]));
                }
                de.setInLinksID(inLinks);
                
                de.setOutLinks(Integer.parseInt(tokens[tokens.length-1]));
                
                docs.add(de);
                
                line = br.readLine();
            }
            System.out.println("Arquivo carregado.");
            FileWriter fw = new FileWriter("pagerank/pagerank.score");
            calculatePageRank(0);
            saveScores(fw);
            fw.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fr.close();                
            } catch (IOException ex) {
                Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void calculatePageRank(int it) {
        System.out.println(it);
        int N = docs.size();
        
        if(it == 0){            
            for(int x=0; x<N; x++){
                scores.add(1.0/(double)N);
            }            
        } else{
            ArrayList<Double> copy = (ArrayList<Double>) scores.clone(); 
            
            double part1 = (1.0 - d)/(double)N;
            
            double part2 = 0.0;
            
            // para cada documento na base
            for(int x=0; x<N; x++){
                double part2A = 0.0;
                // para cada documento que aponta para o documento x
                for(int y=0; y<docs.get(x).getInLinksID().size(); y++){
                    double previousPageRank = copy.get(docs.get(x).getInLinksID().get(y));
                    double nOutLinks = docs.get(docs.get(x).getInLinksID().get(y)).getOutLinks();
                    if(nOutLinks == 0)
                        nOutLinks = 1.0;
                    part2A = previousPageRank / nOutLinks;
                    part2 += part2A;                   
                }
                scores.set(x, part1 + (d * part2));
                part2 = 0.0;
            }            
        }
        ++it;
        if(it < ITERATIONS){
            calculatePageRank(it);
        }
    }

    private static void saveScores(FileWriter fw) throws IOException {      
        for(int i=0; i<scores.size(); i++){
            fw.write(scores.get(i)+"\n");
        }        
    }
    
}
