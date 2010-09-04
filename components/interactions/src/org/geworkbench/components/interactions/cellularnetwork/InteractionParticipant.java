package org.geworkbench.components.interactions.cellularnetwork;

public class InteractionParticipant {
	private static final long serialVersionUID = -4163326138016520669L;
	 ;
	private String dSGeneMarker;    
    private String dSGeneName;     
    private String dbSource;   
   
    public InteractionParticipant(String dSGeneMarker, String dSGeneName, String dbSource) {
        this.dSGeneMarker = dSGeneMarker; 
        this.dSGeneName = dSGeneName;        
        this.dbSource = dbSource;       
    }

    
    public String getdSGeneMarker() {
        return dSGeneMarker;
    }

    public void setdSGeneMarker(String dSGeneMarker) {
        this.dSGeneMarker = dSGeneMarker;
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
