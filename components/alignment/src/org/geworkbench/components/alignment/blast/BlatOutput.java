/*
 * BlatOutput.java
 *
 * Created on August 23, 2006, 4:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.geworkbench.components.alignment.blast;

import java.util.ArrayList;

/**
 *
 * @author avv2101
 */
public class BlatOutput {
    
    String cDNA;//name of the sequence  
    int cDNAStart;//start of the match sequence
    int cDNAEnd;//end of the match sequence
    ArrayList cDNASegments;
    
    String genomic;//name of the sequence
    int gStart;//start of the match sequence
    int gEnd;//end of the match sequence
    ArrayList genomicSegments;
    
    ArrayList SBySAlignment;
    
    
    /** Creates a new instance of BlatOutput */
    public BlatOutput() {
        cDNAStart = -1;
        cDNAEnd = -1;
        cDNA = null;
        genomic = null;
        gStart = -1;
        gEnd = -1;
    }
    public void setCDNA(String cDNA){
        this.cDNA = cDNA;
    }
    public void setCDNAStart(int cDNAStart){
        this.cDNAStart = cDNAStart;
    }
    public void setCDNEnd(int cDNAEnd){
        this.cDNAEnd = cDNAEnd;
    }
    public void setGenomic(String genomic){
        this.genomic = genomic;
    }
    public void setGStart(int gStart){
        this.gStart = gStart;
    }
    public void setGEnd(int gEnd){
        this.gEnd = gEnd;
    }
}
