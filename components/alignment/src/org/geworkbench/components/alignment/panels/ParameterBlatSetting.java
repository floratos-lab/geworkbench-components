/*
 * ParameterBlatSetting.java
 *
 * Created on July 28, 2006, 2:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.geworkbench.components.alignment.panels;

/**
 *
 * @author Anh Vu
 * contact vietanh.vu@m4x.org
 */
public class ParameterBlatSetting {
    /*Declaration of necessary parameters to make a BLAT search
     *Refer to http://genome.ucsc.edu/cgi-bin/hgBlat for detail*/
    private String strGenome;
    private String strAssembly;
    private String strQueryType;
    private String strSortOutput;
    private String strOutputType;
    private boolean booFeelLucky;
    private boolean booOpenInBrowser;
    
    public void setStringGenome(String strGenome){
        this.strGenome = strGenome;
    }
    public void setStringAssembly(String strAssembly){
        this.strAssembly = strAssembly;
    }
    public void setStringQueryType(String strQueryType){
        this.strQueryType = strQueryType;
    }
    public void setStringSortOutput(String strSortOutput){
        this.strSortOutput = strSortOutput;
    }
    public void setStringOutputType(String strOutputType){
        this.strOutputType = strOutputType;
    }
    public void setBooleanFeelLucky(boolean booFeelLucky){
        this.booFeelLucky = booFeelLucky;
    }
    public void setBooleanOpenInBrowser(boolean booOpenInBrowser){
        this.booOpenInBrowser = booOpenInBrowser;
    }
    public String getStringGenome(){
        return this.strGenome;
    }
    public String getStringAssembly(){
        return this.strAssembly;
    }
    public String getStringQueryType(){
        return this.strQueryType;
    }
    public String getStringSortOutput(){
        return this.strSortOutput;
    }
    public String getStringOutputType(){
        return this.strOutputType;
    }
    public boolean getBooleanFeelLucky(){
        return this.booFeelLucky;
    }
    public boolean getBooleanOpenInBrowser(){
        return this.booOpenInBrowser;
    }
    /** Creates a new instance of ParameterBlatSetting */
    public ParameterBlatSetting() {
         try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
 
     private void jbInit() throws Exception{
    }
}
