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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.geworkbench.util.FilePathnameUtils;

/**
 * @author  Marc-Danie Nazaire
 */
public class PredictionModel
{
    private byte[] model;
    private File modelFile;

    public PredictionModel(File file)
    {
        modelFile = file;
        model = null;
        try
        {
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            model = new byte[(int)(fc.size())];
            ByteBuffer bb = ByteBuffer.wrap(model);
            fc.read(bb);
        }
        catch(IOException io)
        {
           io.printStackTrace();

        }
    }

    public File getPredModelFile()
    {
        if(modelFile!= null && modelFile.exists())
            return modelFile;

        try
        {
            modelFile = File.createTempFile("predModel", ".odf", new File(FilePathnameUtils.getTemporaryFilesDirectoryPath()));
            modelFile.deleteOnExit();

            FileOutputStream out = new FileOutputStream(modelFile);

            out.write(model);

            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return modelFile;
    }
}
