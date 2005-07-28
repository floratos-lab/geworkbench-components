package org.geworkbench.components.alignment.blast;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 1.0
 */

public class HmmObj extends BlastObj {
    private String name;
    private String ID;
    private String description;
    private String detail;
    private java.net.URL URL;
    private String DetailedAlignment;

    public HmmObj() {
    }

    public HmmObj(String thename, String theID, String theDes) {
        name = thename;
        ID = theID;
        description = theDes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public java.net.URL getURL() {
        return URL;
    }

    public void setURL(java.net.URL URL) {
        this.URL = URL;
    }

    public String toString() {
        return name + "\n" + ID + "\n" + description + "\n" + detail;
    }

    public String getDetailedAlignment() {
        return DetailedAlignment;
    }

    public void setDetailedAlignment(String DetailedAlignment) {
        this.DetailedAlignment = DetailedAlignment;
    }
}
