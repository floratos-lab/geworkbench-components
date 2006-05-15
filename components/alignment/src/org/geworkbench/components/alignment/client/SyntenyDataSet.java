package org.geworkbench.components.alignment.client;

import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;

import java.io.File;

public class SyntenyDataSet extends CSAncillaryDataSet {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6683934264637554208L;

	public SyntenyDataSet(String fileName) {
        super(null, fileName);
        resultFile = new File(fileName);
    }

    // private static ImageIcon icon = new ImageIcon("share/images/blast.gif");
    private String label = "Synteny_Result";
    private File resultFile = null;

    /**
     * isDirty
     *
     * @return boolean
     * @todo Implement this geaw.bean.microarray.MAMemoryStatus method
     */
    public boolean isDirty() {
        return false;
    }

    /**
     * setDirty
     *
     * @param boolean0 boolean
     * @todo Implement this geaw.bean.microarray.MAMemoryStatus method
     */
    public void setDirty(boolean boolean0) {
    }

    /**
     * getDataSetName
     *
     * @return String
     * @todo Implement this medusa.components.projects.IDataSet method
     */
    public String getDataSetName() {
        return resultFile.getName();
    }

    public File getDataSetFile() {

        return resultFile;
    }

    public void setDataSetFile(File _file) {
        resultFile = _file;
    }

    /**
     * @param ads IAncillaryDataSet
     * @return boolean
     * @todo implement later.
     */
    public boolean equals(Object ads) {
        if (ads instanceof DSAncillaryDataSet) {
            return getDataSetName() == ((DSAncillaryDataSet) ads).getDataSetName();
        } else {
            return false;
        }
    }
    ;

    /**
     * getFile
     *
     * @return File
     * @todo Implement this medusa.components.projects.IDataSet method
     */
    public File getFile() {
        return resultFile;
    }

    /**
     * writeToFile
     *
     * @param fileName String
     */
    public void writeToFile(String fileName) {
    }

}
