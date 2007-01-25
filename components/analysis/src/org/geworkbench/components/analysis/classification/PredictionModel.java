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
package org.geworkbench.components.analysis.classification;

import org.genepattern.io.odf.OdfWriter;
import org.genepattern.io.odf.OdfObject;

import java.io.File;

/**
 * @author Marc-Danie Nazaire
 */
public class PredictionModel
{
    OdfObject model;
    private File predFile;

    public PredictionModel(File fileName)
    {
        try
        {
            OdfObject odf = new OdfObject(fileName.getAbsolutePath());
            model = odf;

           fileName.delete();
        }
       catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public File getPredModelFile()
    {
        try
        {
            if(predFile!= null && predFile.exists())
                return predFile;

            predFile = File.createTempFile("predModel", ".odf", new File(System.getProperty("temporary.files.directory")));
            predFile.deleteOnExit();

            OdfWriter odfWriter = new OdfWriter(predFile.getAbsolutePath(), null,
                                    model.getHeader("Model"), model.getIntHeader("DataLines"), false);

            String[] columnTypes = new String[model.getColumnCount()];
            for(int i=0; i < model.getColumnCount(); i++)
                columnTypes[i]= model.getColumnClass(i).getSimpleName();

            odfWriter.setColumnTypes(columnTypes);

            String[] assignments = model.getArrayHeader("ASSIGNMENTS");
            odfWriter.addHeader("ASSIGNMENTS", assignments);

            odfWriter.printHeader();
            for(int r = 0; r < model.getRowCount(); r++)
            {
                for(int c = 0; c < model.getColumnCount(); c++ )
                {
                     odfWriter.write(model.getValueAt(r, c).toString());
                     odfWriter.write("\t");
                }
                odfWriter.write(System.getProperty("line.separator"));
            }

            odfWriter.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return predFile;
    }
}
