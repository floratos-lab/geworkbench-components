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
package org.geworkbench.components.gpmodule_v3_0.classification;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.util.ClassifierException;
import org.genepattern.data.expr.IExpressionData;
import org.genepattern.data.expr.AbstractExpressionData;
import org.genepattern.data.matrix.ClassVector;
import org.genepattern.io.expr.gct.GctWriter;
import org.genepattern.io.expr.cls.ClsWriter;
import org.genepattern.util.GPpropertiesManager;
import org.genepattern.client.GPServer;
import org.genepattern.webservice.JobResult;
import org.genepattern.webservice.Parameter;
import org.genepattern.webservice.AnalysisWebServiceProxy;

import javax.swing.*;
import java.io.*;
import java.util.List;

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

    protected File createTestGCTFile(String fileName, final float[] data)
    {
        File gctTestFile = null;

        IExpressionData testData = new AbstractExpressionData()
        {
            public double getValue(int row, int column)
            {
                return data[row];
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
                return 1;
            }

            public String getColumnName(int column)
            {
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

    protected File createTestCLSFile(String fileName)
    {
        File testClsData = null;
        BufferedOutputStream clsOutputStream = null;

        try
        {
            testClsData = new File(fileName + ".cls");
            testClsData.deleteOnExit();
            clsOutputStream = new BufferedOutputStream(new FileOutputStream(testClsData));

            String[] classLabels = new String[]{"Control"};
            ClassVector classVector = new ClassVector(classLabels);

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

    protected File runPredictor(String classifierName, Parameter[] parameters)
    {
        File predFile = null;
        try
        {
            String serverName = GPpropertiesManager.getProperty("gp.server");
            String userName = GPpropertiesManager.getProperty("gp.user.name");
            GPServer server = new GPServer(serverName, userName, password);
            
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
                throw new ClassifierException("Error: Classifier model could not be generated");

            predFile = analysisResult.downloadFile(predFileName, System.getProperty("temporary.files.directory"));
            predFile.deleteOnExit();

            // remove job from GenePattern server
            AnalysisWebServiceProxy analysisProxy = new AnalysisWebServiceProxy(serverName, userName, password);
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

        return predFile;
    }

    protected String getPredictedClass(File predFile)
    {
        String className = null;
        try
        {
            BufferedReader bufReader = new BufferedReader(new FileReader(predFile));
            String line = "";
            String predLine = line;
            while((line = bufReader.readLine()) != null)
            {
                predLine = line;
            }

            className = predLine.split("\t")[2];
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return className;
    }
}
