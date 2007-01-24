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

import org.genepattern.data.expr.IExpressionData;
import org.genepattern.data.matrix.ClassVector;
import org.genepattern.io.expr.gct.GctWriter;
import org.genepattern.io.expr.cls.ClsWriter;
import org.genepattern.client.GPServer;
import org.genepattern.webservice.JobResult;
import org.genepattern.webservice.Parameter;
import org.genepattern.webservice.AnalysisWebServiceProxy;
import org.genepattern.util.GPpropertiesManager;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.algorithms.AbstractTraining;

import java.io.*;
import java.util.List;

/**
 * @author Marc-Danie Nazaire
 */
public abstract class GPTraining extends AbstractTraining
{
    protected File createGCTFile(String fileName, final List trainingSet, final List featureNames)
    {
        File gctFile = null;
        IExpressionData data = new IExpressionData() {

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
                return "Column " + column;
            }

            public String getColumnDescription(int column)
            {
                return "";
            }

            public int getRowIndex(String row)
            {
                return featureNames.indexOf(row);
            }

            public int getColumnIndex(String row)
            {
                return 0;
            }

            public String getValueAsString(int row, int column)
            {
                return String.valueOf(((float[])trainingSet.get(column))[row]);
            }
        };

        GctWriter writer = new GctWriter();
        OutputStream os = null;
        try
        {
            gctFile = new File(fileName + ".gct");
            gctFile.deleteOnExit();
            os = new BufferedOutputStream(new FileOutputStream(gctFile));
            writer.write(data, os);
        }
        catch (IOException ioe)
        {   ioe.printStackTrace();  }
        finally
        {
            try
            {
                if (os != null)
                {   os.close(); }
            }
            catch (IOException e)
            {   e.printStackTrace();    }
        }

        return gctFile;
    }

    protected File createCLSFile(String fileName, ClassVector classLabel)
    {
        File clsFile = null;

        try
        {
            clsFile = new File(fileName + ".cls");
            clsFile.deleteOnExit();
            FileOutputStream clsOutputStream = new FileOutputStream(clsFile);

            ClsWriter writer = new ClsWriter();
            writer.write(classLabel, clsOutputStream);
        }
        catch(Exception e)
        {   e.printStackTrace(); }

        return clsFile;
    }

    protected int getStatistic(String statistic, boolean median, boolean stdDev)
    {
        // generate a numeric equivalent for desired statistic
        int stat = 0;

        if(statistic.equals("T-Test"))
            stat = 1;

        if(median)
        {
            stat += 2;
        }
        if(stdDev)
        {
            stat += 4;
        }

        return stat;
    }

    protected PredictionModel createModel(String modelName, Parameter[] parameters)
    {
        PredictionModel predModel = null;
        try
        {
            String serverName = GPpropertiesManager.getProperty("gp.server");
            String userName = GPpropertiesManager.getProperty("gp.user.name");

            GPServer server = new GPServer(serverName, userName);
            JobResult analysisResult = server.runAnalysis(modelName, parameters);

            System.out.println("Error occurred: " + analysisResult.hasStandardError());

            String[] outputFiles = analysisResult.getOutputFileNames();

            String modelFileName = null;
            for(int i = 0; i < outputFiles.length; i++)
            {
                if(outputFiles[i].indexOf(".odf") != -1)
                    modelFileName = outputFiles[i];
            }

            if(modelFileName == null)
                throw new Exception("Error: No classifier model created");

            // save the model of the classifer
            File modelFile = analysisResult.downloadFile(modelFileName, System.getProperty("temporary.files.directory"));

            predModel = new PredictionModel(modelFile);

            // remove job from GenePattern server
            AnalysisWebServiceProxy analysisProxy = new AnalysisWebServiceProxy(serverName, userName);
            analysisProxy.purgeJob(analysisResult.getJobNumber());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return predModel;
    }

    protected void validateFeatureFile(String filename, List featureNames)throws ClassifierException
    {   try
        {
            BufferedReader bufReader = new BufferedReader(new FileReader(filename));
            String feature;
            while((feature = bufReader.readLine()) != null)
            {
                if(!featureNames.contains(feature))
                {
                    throw new ClassifierException("Marker " + feature + " not found in training data");
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
