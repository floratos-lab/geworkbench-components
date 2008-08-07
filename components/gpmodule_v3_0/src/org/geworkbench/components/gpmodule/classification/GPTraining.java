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

import org.genepattern.matrix.Dataset;
//import org.genepattern.data.expr.AbstractExpressionData;
import org.genepattern.matrix.ClassVector;
import org.genepattern.matrix.AbstractDataset;
import org.genepattern.io.gct.GctWriter;
import org.genepattern.io.cls.ClsWriter;
//import org.genepattern.client.GPServer;
import org.genepattern.client.GPClient;
import org.genepattern.webservice.JobResult;
import org.genepattern.webservice.Parameter;
import org.genepattern.webservice.AnalysisWebServiceProxy;
import org.genepattern.webservice.WebServiceException;
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
    protected static int modelCount = 0;
    
    protected File createGCTFile(String fileName, final List trainingSet, final List featureNames)
    {
        File gctFile = null;
        Dataset data = new AbstractDataset() {

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

    protected PredictionModel createModel(String modelName, Parameter[] parameters) throws ClassifierException
    {
        PredictionModel predModel = null;
        try
        {
            String serverName = GPpropertiesManager.getProperty("gp.server");
            String userName = GPpropertiesManager.getProperty("gp.user.name");
            String password = ((GPTrainingPanel)this.panel).getPassword();
            GPClient server = new GPClient(serverName, userName, password);            

            JobResult analysisResult = server.runAnalysis(modelName, parameters);
            String[] outputFiles = analysisResult.getOutputFileNames();

            String modelFileName = null;
            for(int i = 0; i < outputFiles.length; i++)
            {
                if(outputFiles[i].indexOf(".odf") != -1)
                    modelFileName = outputFiles[i];
            }

            if(modelFileName == null)
                throw new ClassifierException("Error: Classifier model could not be generated");

            //download model result file from server
            AnalysisWebServiceProxy analysisProxy = new AnalysisWebServiceProxy(server.getServer(), server.getUsername(), password);
            String[] resultFiles = new String[1] ;
            resultFiles[0] = modelFileName;

            File[] result = analysisProxy.getResultFiles(analysisResult.getJobNumber(), resultFiles, new File(System.getProperty("temporary.files.directory")), true);
            if(result == null || result.length == 0)
                throw new ClassifierException("Error: Could not retrieve classifier model from GenePattern");

            // save the model of the classifer
            File modelFile =  result[0];
            modelFile.deleteOnExit();

            predModel = new PredictionModel(modelFile);

            // remove job from GenePattern server
            analysisProxy = new AnalysisWebServiceProxy(server.getServer(), server.getUsername(), password);
            analysisProxy.purgeJob(analysisResult.getJobNumber());
        }
        catch(WebServiceException we)
        {
             we.printStackTrace();
             throw new ClassifierException("Could not connect to GenePattern server");           
        }
        catch(ClassifierException ce)
        {
            throw ce;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new ClassifierException("Error creating " + modelName + " model");
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
                    throw new ClassifierException("Marker: " + feature + " in "+ filename
                                + "\nnot found in training data");
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
