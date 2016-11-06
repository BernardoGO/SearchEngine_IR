/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package feedback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Herbert
 */
class LogInstance {
    String query;
    String[] documents;
    int[] time;
    List result;
    
    
    public LogInstance(String instance){
        String[] parts = instance.split("\t");
        query = parts[1];
        int len = (parts.length-2)/2;
        documents = new String[len];
        time = new int[len];
        
        for(int i=0; i<len; i++){
            documents[i] = parts[i+2];
            time[i] = Integer.parseInt(parts[len+2]);
        }
        result = new ArrayList(len);
        for(int i=0; i<len; i++){
           result.add(new DocTimeVis(documents[i], time[i])); 
        }        
        Collections.sort(result);
    }
}
