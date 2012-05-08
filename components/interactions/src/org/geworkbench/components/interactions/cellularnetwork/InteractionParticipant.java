package org.geworkbench.components.interactions.cellularnetwork;

public class InteractionParticipant {

	private String dSGeneId;    
    private String dSGeneName;     
    private String dbSource;   
   
    public InteractionParticipant(String dSGeneMarker, String dSGeneName, String dbSource) {
        this.dSGeneId = dSGeneMarker; 
        this.dSGeneName = dSGeneName;        
        this.dbSource = dbSource;       
    }

    
    public String getdSGeneId() {
        return dSGeneId;
    }

    public void setdSGeneId(String dSGeneId) {
        this.dSGeneId = dSGeneId;
    }
    
   
    public String getdSGeneName() {
        return dSGeneName;
    }

    public void setdSGeneName(String dSGeneName) {
        this.dSGeneName = dSGeneName;
    }

  
    public String getDbSource() {
        return this.dbSource;
    }

    public void setDbSource(String dbSource) {
        this.dbSource = dbSource;
    }
 
}
