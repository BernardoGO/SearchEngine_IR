/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package feedback;

/**
 *
 * @author Herbert
 */
class TermFreq implements Comparable{
    public String term;
    int frequency;
    
    
    public TermFreq(String termString, int termFrequency) {
        this.term = termString;
        this.frequency = termFrequency;
    }

    @Override
    public int compareTo(Object o) {
        TermFreq obj2 = (TermFreq) o;
        if(this.frequency > obj2.frequency){
            return 1;
        } else if (this.frequency == obj2.frequency){
            return 0;
        } else {
            return -1;
        }
    }
    
}
