package org.geworkbench.components.gpmodule.classification;

import org.genepattern.matrix.Dataset;
import org.genepattern.matrix.ClassVector;
import org.genepattern.matrix.DefaultClassVector;
import org.genepattern.io.cls.ClsWriter;
import org.genepattern.io.IOUtil;

import java.io.*;
import java.util.Arrays;

public class GPClassificationUtils
{
    public static String createGCTFile(Dataset dataset, String outputFile) throws IOException
    {
        return IOUtil.writeDataset(dataset, "gct", outputFile, true);
    }

    public static File createCLSFile(String fileName, ClassVector classLabel)
    {
        File clsFile = null;

        try
        {
            ClsWriter writer = new ClsWriter();            
            fileName = writer.checkFileExtension(fileName);
            clsFile = new File(fileName);
            clsFile.deleteOnExit();
            FileOutputStream clsOutputStream = new FileOutputStream(clsFile);

            writer.write(classLabel, clsOutputStream);
        }
        catch(Exception e)
        {   e.printStackTrace(); }

        return clsFile;
    }


    public static File createCLSFile(String fileName, int numArrays)
    {
        return createCLSFile(fileName, numArrays, null);
    }

    public static File createCLSFile(String fileName, int numArrays, String[] classLabels)
    {
        File testClsData = null;
        BufferedOutputStream clsOutputStream = null;

        try
        {
            ClsWriter writer = new ClsWriter();
            fileName = writer.checkFileExtension(fileName);
            testClsData = new File(fileName);
            testClsData.deleteOnExit();
            clsOutputStream = new BufferedOutputStream(new FileOutputStream(testClsData));

            if(classLabels == null || classLabels.length < numArrays)
            {
                classLabels = new String[numArrays];
                Arrays.fill(classLabels, 0, numArrays/2, "Control");
                Arrays.fill(classLabels, numArrays/2, numArrays, "Case");
            }

            ClassVector classVector = new DefaultClassVector(classLabels);

            writer.write(classVector, clsOutputStream);
        }
        catch(Exception e)
        {   e.printStackTrace(); }
        finally
        {
            try
            {
                if (clsOutputStream != null)
                {   clsOutputStream.close(); }
            }
            catch (IOException e)
            {   e.printStackTrace();    }
        }

        return testClsData;
    }
}
