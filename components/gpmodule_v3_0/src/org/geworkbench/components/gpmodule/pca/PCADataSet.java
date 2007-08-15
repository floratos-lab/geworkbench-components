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

import java.io.File;

/**
 * @author: Marc-Danie Nazaire
 */
public class PCADataSet extends CSAncillaryDataSet implements DSAncillaryDataSet
{
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
}
