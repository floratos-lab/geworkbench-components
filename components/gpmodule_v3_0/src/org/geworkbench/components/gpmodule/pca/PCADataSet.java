/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2007) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.gpmodule.pca;

import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.*;

/**
 * @author: Marc-Danie Nazaire
 */
public class PCADataSet extends CSAncillaryDataSet implements DSAncillaryDataSet
{
    static Log log = LogFactory.getLog(PCADataSet.class);
    private PCAData pcaData;

    public PCADataSet(DSDataSet parent, String label, PCAData pcaData)
    {
        super(parent, label);
        this.pcaData = pcaData;
    }

    public File getDataSetFile()
    {
        // no-op
        return null;
    }

    public void setDataSetFile(File file)
    {
        // no-op
    }

    public PCAData getData()
    {
        return pcaData;
    }

    public void writeToFile(String fileName)
    {
        File file = new File(fileName);

        try
        {
            file.createNewFile();
            if (!file.canWrite()) {
                JOptionPane.showMessageDialog(null, "Cannot write to specified file.");
                return;
            }
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            String fileSeparator = System.getProperty("line.separator");
            for(int i=1; i <= pcaData.getNumPCs(); i++)
            {
                writer.write("Principal Component:\t" + i);
                writer.write(fileSeparator) ;
                writer.write("Eigenvalue:\t" + pcaData.getEigenValues().get(new Integer(i)));
                writer.write(fileSeparator) ;
                writer.write("Percentage Variation:\t" + pcaData.getPercentVars().get(new Integer(i)));
                writer.write(fileSeparator) ;
                writer.write("Eigenvector:\t" + pcaData.getEigenVectors().get(new Integer(i)));
                writer.write(fileSeparator);
                writer.write(fileSeparator) ;
            }

            writer.flush();
            writer.close();
        }
        catch(IOException io)
        {
           log.error(io);
        }
    }
}
