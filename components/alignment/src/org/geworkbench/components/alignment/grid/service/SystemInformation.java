package org.geworkbench.components.alignment.grid.service;

//import org.geworkbench.util.session.Session;


public class SystemInformation {
    private String cpu;
   // private Session currentSession;
    private String lastResults;
    private String cost;
    private org.globus.ogsa.wsdl.GSR GSH;

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

//    public Session getCurrentSession() {
//        return currentSession;
//    }
//
//    public void setCurrentSession(Session currentSession) {
//        this.currentSession = currentSession;
//    }

    public String getLastResults() {
        return lastResults;
    }

    public void setLastResults(String lastResults) {
        this.lastResults = lastResults;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public org.globus.ogsa.wsdl.GSR getGSH() {
        return GSH;
    }

    public void setGSH(org.globus.ogsa.wsdl.GSR GSH) {
        this.GSH = GSH;
    }


}
