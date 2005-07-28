/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */
/*
 public class SWDataSet extends BlastDataSet {
   static private ImageIcon iconNew = new ImageIcon(SWDataSet.class.getResource(
    "sw.gif"));
 static private ImageIcon icon  = new ImageIcon(SWDataSet.class.getResource(
    "sw.gif"));
  private String matrixName;
  private String cmd;
  public SWDataSet(String fileName, String inputFile) {
    super(fileName, inputFile);
    // super.setIcon(iconNew);

  }
  public SWDataSet(String fileName, String inputFile, String cmdHistory) {
   super(fileName, inputFile);
   cmd = cmdHistory;
 }



 }*/

package org.geworkbench.components.alignment.client;

import org.geworkbench.bison.datastructure.biocollections.CSDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;

import javax.swing.*;
import java.io.File;

public class SWDataSet extends CSDataSet implements DSAncillaryDataSet {
    public SWDataSet(String fileName, String inputFile) {

        resultFile = new File(fileName);
        fastaFile = new File(inputFile);
        //System.out.println("in construtor" + resultFile.getAbsolutePath());
    }

    static private ImageIcon icon = new ImageIcon(SWDataSet.class.getResource("sw.gif"));
    private String matrixName;
    private String cmd;

    private String label = "SW_Result";
    private File fastaFile = null;
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

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    /**
     * getIcon
     *
     * @return ImageIcon
     * @todo Implement this medusa.components.projects.IDataSet method
     */
    public ImageIcon getIcon() {
        return icon;
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
        System.out.println("in get Datasetname " + resultFile.getAbsolutePath());
        return resultFile.getName();
    }

    public String getResultFilePath() {
        if (resultFile.canRead()) {
            return resultFile.getAbsolutePath();
        }
        return null;
    }

    public File getDataSetFile() {
        String filename = resultFile.toString();
        System.out.println("in getData" + resultFile.getAbsolutePath());
        return resultFile;
    }

    public void setDataSetFile(File _file) {
        fastaFile = _file;
        //System.out.println("in setDataSetFile " + fastaFile.getAbsolutePath());
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
