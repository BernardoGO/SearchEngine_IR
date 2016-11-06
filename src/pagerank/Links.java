/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pagerank;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Armazena informações sobre um documento e os links que ele contem ou docs que
 * apontem para ele
 * @author Herbert
 */
class Links{
    
    String document;
    int id;
    ArrayList<Integer> links;
    
    public Links(String line, ArrayList<DocumentEdges> docs, boolean type){       
        
        String[] links = line.split(" ");
        this.links = new ArrayList<Integer>(); 
        
        if(type){ // se links que entram         
            
            this.document = links[0];
            for(int i=1; i<links.length; i++){
                int position = getNumber(links[i], docs, 0, docs.size()-1);
                if(position >= 0 && position < docs.size()){
                    this.links.add(docs.get(position).getDocumentID());
                }        
            }   
        } else { //se links que saem
            this.document = links[0];
            for(int i=1; i<links.length; i++){
               this.links.add(-1);
            }   
        }
    }    
    
    /**
     * Dado um nome de um Documento retorna o id desse Doc no indice
     * @param id Nome do Documento
     * @param list lista onde procurar o id
     * @param init define o inicio do intervalo de busca
     * @param end define o fim do intervalo de busca
     * @return o numero correspondente ao id do documento no indice
     */
    private int getNumber(String id, ArrayList<DocumentEdges> list, int init, int end) {       
               
        int pos = (end + init)/2;
        int comp = 0;
        
        if(end >= 0 && init < list.size() && init <= end && pos >= 0 && pos < list.size()){
            comp = list.get(pos).getDocument().compareTo(id);
        } else {
            return -1;
        }
        
        if(comp > 0){
            return getNumber(id, list, init, pos-1);
        } else if(comp == 0){
            return pos;
        } else {
            return getNumber(id, list, pos+1, end);
        }
    }    
}
