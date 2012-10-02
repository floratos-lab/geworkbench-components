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
package org.geworkbench.components.gpmodule.classification;

import org.genepattern.io.odf.*;
import org.geworkbench.bison.datastructure.biocollections.PredictionModel;

import java.io.File;

/**
 * @author  Marc-Danie Nazaire
 */
public class PredictionResult
{
    private OdfObject odfObject;
    private PredictionModel model;

    public PredictionResult(File fileName)
    {
        try
        {
            model = new PredictionModel(fileName);

            odfObject = new OdfObject(fileName.getAbsolutePath());
            fileName.delete();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public int getColumn(String name)
    {
        return odfObject.getColumnIndex(name);
    }

    public String getValueAt(int row, int col)
    {
        return String.valueOf(odfObject.getValueAt(row, col));
    }

    public int getNumRows()
    {
        return odfObject.getRowCount();
    }
    
    public PredictionModel getPredictionModel(){
    	return model;
    }
}
