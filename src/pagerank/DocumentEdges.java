/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pagerank;

import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author Herbert
 */
class DocumentEdges implements Comparable{
    
    private String document;                // Nome do documento
    private int documentID;                 // ID do Documento
    private ArrayList<String> inLinks;      // Nome dos Documentos que apontam para esse
    private ArrayList<Integer> inLinksID;   // IDs dos Documentos que apontam para esse
    private int outLinks;                   // quntidade de Documentos apontados por esse
    
    
    public DocumentEdges(){
        document = "";
        inLinks = new ArrayList<String>();
        inLinksID = new ArrayList<Integer>();
        outLinks = -1;
    }
    
    public DocumentEdges(String doc, ArrayList<String> in, int out){
        
        this.document = doc;
        this.inLinks = in;
        this.outLinks = out;
        
    }

    /**
     * @return the document
     */
    public String getDocument() {
        return document;
    }

    /**
     * @param document the document to set
     */
    public void setDocument(String document) {
        this.document = document;
    }

    /**
     * @return the inLinks
     */
    public ArrayList<String> getInLinks() {
        return inLinks;
    }

    /**
     * @param inLinks the inLinks to set
     */
    public void setInLinks(ArrayList<String> inLinks) {
        this.inLinks = inLinks;
    }

    /**
     * @return the inLinksID
     */
    public ArrayList<Integer> getInLinksID() {
        return inLinksID;
    }

    /**
     * @param inLinksID the inLinksID to set
     */
    public void setInLinksID(ArrayList<Integer> inLinksID) {
        this.inLinksID = inLinksID;
    }

    /**
     * @return the outLinks
     */
    public int getOutLinks() {
        return outLinks;
    }

    /**
     * @param outLinks the outLinks to set
     */
    public void setOutLinks(int outLinks) {
        this.outLinks = outLinks;
    }

    /**
     * @return the documentID
     */
    public int getDocumentID() {
        return documentID;
    }

    /**
     * @param documentID the documentID to set
     */
    public void setDocumentID(int documentID) {
        this.documentID = documentID;
    }

    

    @Override
    public int compareTo(Object o) {       
        DocumentEdges de2 = (DocumentEdges) o;
        return this.getDocument().compareTo(de2.getDocument());
    }
    
}
