/*package org.geworkbench.components.alignment.client;
 import javax.swing.ImageIcon;

 /**
  * <p>Title: Bioworks</p>
  * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
  * <p>Copyright: Copyright (c) 2003 -2004</p>
  * <p>Company: Columbia University</p>
  * @author not attributable
  * @version 1.0
  */
/*
 public class HMMDataSet
    extends CSAlignmentResultSet {
  static private ImageIcon iconNew = new ImageIcon(HMMDataSet.class.getResource(
      "hmm.gif"));
  public HMMDataSet(String fileName, String inputFile) {
    super(fileName, inputFile);
    super.setIcon(iconNew);
  }

 }
 */
package org.geworkbench.components.alignment.client;

import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.builtin.projects.Icons;
import org.geworkbench.builtin.projects.ProjectPanel;

import java.io.File;

public class HMMDataSet extends CSAncillaryDataSet implements DSAncillaryDataSet {
    public HMMDataSet(String fileName, String inputFile, DSDataSet dataSet) {
        super(dataSet, "HMM Result");
        resultFile = new File(fileName);
        fastaFile = new File(inputFile);
        //System.out.println("in construtor" + resultFile.getAbsolutePath());
    }

    //private static ImageIcon icon = new ImageIcon("share/images/blast.gif");
    static {
        ProjectPanel.setIconForType(HMMDataSet.class, Icons.ALIGNMENT_ICON);
    }

    private File fastaFile = null;
    private File resultFile = null;

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
