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
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.util.ClassifierException;
import org.genepattern.io.gct.GctWriter;
import org.genepattern.io.cls.ClsWriter;
import org.genepattern.util.GPpropertiesManager;
import org.genepattern.client.GPClient;
import org.genepattern.webservice.JobResult;
import org.genepattern.webservice.Parameter;
import org.genepattern.webservice.AnalysisWebServiceProxy;
import org.genepattern.matrix.Dataset;
import org.genepattern.matrix.AbstractDataset;
import org.genepattern.matrix.ClassVector;
import org.genepattern.matrix.DefaultClassVector;

import javax.swing.*;
import java.io.*;
import java.util.List;
import java.util.Arrays;

/**
 * @author Marc-Danie Nazaire
 */
public abstract class GPClassifier extends CSClassifier
{
    protected List featureNames;
    private String password;

    protected GPClassifier(DSDataSet parent, String label, String[] classifications)
    {       
        super(parent, label, classifications);
        password = null;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    protected File createTestGCTFile(String fileName, final List trainingSet)
    {
        return createTestGCTFile(fileName, trainingSet, null);
    }

    protected File createTestGCTFile(String fileName, final List trainingSet, final List arrayNames)
    {
        File gctTestFile = null;

        Dataset testData = new AbstractDataset()
        {
            public double getValue(int row, int column)
            {
                 return ((float[])trainingSet.get(column))[row];
            }

            public String getRowName(int row)
            {
                return (String)featureNames.get(row);
            }

            public int getRowCount()
            {
                return featureNames.size();
            }

            public String getRowDescription(int row)
            {
                return "";
            }

            public int getColumnCount()
            {
                return trainingSet.size();
            }

            public String getColumnName(int column)
            {
                if(arrayNames != null && arrayNames.get(column) != null)
                {
                    return (String) arrayNames.get(column);
                }

                return ("Column " + column);
            }

            public String getColumnDescription(int column)
            {
                return "";
            }
        };

        try
        {
            gctTestFile = new File(fileName + ".gct") ;
            gctTestFile.deleteOnExit();
            GctWriter writer = new GctWriter();
            OutputStream os = new BufferedOutputStream(new FileOutputStream(gctTestFile));
            writer.write(testData, os);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return gctTestFile;
    }

    protected File createTestCLSFile(String fileName, int numArrays)
    {
        File testClsData = null;
        BufferedOutputStream clsOutputStream = null;

        try
        {
            testClsData = new File(fileName + ".cls");
            testClsData.deleteOnExit();
            clsOutputStream = new BufferedOutputStream(new FileOutputStream(testClsData));

            String[] classLabels = new String[numArrays];
            Arrays.fill(classLabels, "Control");
            Arrays.fill(classLabels, 0, numArrays/2, "Case");
            ClassVector classVector = new DefaultClassVector(classLabels);

            ClsWriter writer = new ClsWriter();
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

    public PredictionResult runPredictor(String classifierName, Parameter[] parameters)
    {
        PredictionResult predResult = null;
        try
        {
            String serverName = GPpropertiesManager.getProperty("gp.server");
            String userName = GPpropertiesManager.getProperty("gp.user.name");
            GPClient server = new GPClient(serverName, userName, password);
            
            JobResult analysisResult = server.runAnalysis(classifierName, parameters);

            System.out.println("Error occurred: " + analysisResult.hasStandardError());

            String[] outputFiles = analysisResult.getOutputFileNames();

            String predFileName = null;
            for(int i = 0; i < outputFiles.length; i++)
            {
                if(outputFiles[i].indexOf(".odf") != -1)
                    predFileName = outputFiles[i];
            }

            if(predFileName == null)
                throw new ClassifierException("Error: Classifier prediction model could not be generated");

            AnalysisWebServiceProxy analysisProxy = new AnalysisWebServiceProxy(serverName, userName, password);
            String[] resultFiles = new String[1] ;
            resultFiles[0] = predFileName;

            File[] result = analysisProxy.getResultFiles(analysisResult.getJobNumber(), resultFiles, new File(System.getProperty("temporary.files.directory")), true);
            if(result == null || result.length == 0)
                throw new ClassifierException("Error: Could not retrieve classifier model from GenePattern");
           
            File predFile = result[0];
            predFile.deleteOnExit();

            predResult = new PredictionResult(predFile);
            // remove job from GenePattern server
            analysisProxy = new AnalysisWebServiceProxy(server.getServer(), server.getUsername(), password);
            analysisProxy.purgeJob(analysisResult.getJobNumber());
        }
        catch(ClassifierException ce)
        {
            JOptionPane.showMessageDialog(null, ce.getMessage());
        }
        catch(Exception e)
        {
            if(e.getMessage().indexOf("Unknown user or invalid password") != -1)
                JOptionPane.showMessageDialog(null, "Could not connect to GenePattern: " + e.getMessage());    

            e.printStackTrace();
        }

        return predResult;
    }

    protected String getPredictedClass(PredictionResult result)
    {
        int predClassIndx = result.getColumn("Predicted Class");

        return result.getValueAt(0, predClassIndx);
    }
}
