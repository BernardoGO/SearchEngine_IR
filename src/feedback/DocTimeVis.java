/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package feedback;

import java.util.Comparator;

/**
 * 
 * @author Herbert
 */
class DocTimeVis implements Comparable{
    
    String doc; // id do documento no indice
    int time;   // tempo de visualização

    public DocTimeVis(String document, int time) {
        this.doc = document;
        this.time = time;
    }    

    @Override
    public int compareTo(Object o) {
        
        DocTimeVis obj2 = (DocTimeVis) o;
        if(this.time > obj2.time){
            return 1;
        } else if (this.time == obj2.time){
            return 0;
        } else {
            return -1;
        }
    }    
}
