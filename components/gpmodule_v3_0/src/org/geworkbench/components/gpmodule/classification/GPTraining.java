/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2010) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.gpmodule.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genepattern.client.GPClient;
import org.genepattern.io.IOUtil;
import org.genepattern.matrix.ClassVector;
import org.genepattern.matrix.DefaultClassVector;
import org.genepattern.util.GPpropertiesManager;
import org.genepattern.webservice.AnalysisWebServiceProxy;
import org.genepattern.webservice.JobResult;
import org.genepattern.webservice.Parameter;
import org.genepattern.webservice.WebServiceException;
import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.bison.datastructure.biocollections.PredictionModel;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.components.gpmodule.GPDataset;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.util.FilePathnameUtils;

/**
 * @author Marc-Danie Nazaire
 * @version $Id$
 */
public abstract class GPTraining extends AbstractTraining
{
	private static final long serialVersionUID = -4115702476557037773L;

	static Log log = LogFactory.getLog(GPTraining.class);

    protected static int modelCount = 0;

    public List<String> getArrayNames(DSPanel<DSMicroarray> panel)
    {
        List<String> arrayNames = new ArrayList<String>();
        for(DSMicroarray microarray: panel)
        {
            arrayNames.add(microarray.getLabel());
        }

        return arrayNames;
    }

    public GPDataset createGCTDataset(List<float[]> caseData, List<float[]> controlData,
                                            List<String> caseArrayNames, List<String> controlArrayNames)
    {
        DSItemList<?> markers = panel.getActiveMarkers();

        Set<String> featureNames = new HashSet<String>();
        for(int i =0; i < markers.size();i++)
        {
            featureNames.add(((DSGeneMarker)markers.get(i)).getLabel());
        }

        List<float[]> trainingSet = new ArrayList<float[]>();
        trainingSet.addAll(controlData);
        trainingSet.addAll(caseData);

        List<String> arrayNames = new ArrayList<String>();
        arrayNames.addAll(controlArrayNames);
        arrayNames.addAll(caseArrayNames);

        GPDataset dataset = new GPDataset(trainingSet, (String[])featureNames.toArray(new String[0]), (String[])arrayNames.toArray(new String[0]));

        return dataset;
    }

    public ClassVector createClassVector(List<float[]> caseData, List<float[]> controlData)
    {
        int sampleSize = caseData.size() + controlData.size();
        String[] classLabels = new String[sampleSize];

        for(int i = 0; i < sampleSize; i++)
        {
            if(i < controlData.size())
                classLabels[i] = "Control";
            else
                classLabels[i] = "Case";
        }

        ClassVector clsVector = new DefaultClassVector(classLabels);

        return clsVector;
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

    protected File trainData(String classifierName, Parameter[] parameters) throws ClassifierException
    {
        File modelFile = null;
        try
        {
            String serverName = GPpropertiesManager.getProperty("gp.server");
            String userName = GPpropertiesManager.getProperty("gp.user.name");
            String password = ((GPTrainingPanel)this.panel).getPassword();

            String passwordRequired = ((GPTrainingPanel)this.panel).
                    getConfigPanel().passwordRequired(serverName, userName);

            //Check if password needs to be entered
            if((password == null || password.equals("")) && (passwordRequired != null &&  passwordRequired.equals("true")))
            {
                ((GPTrainingPanel)this.panel).
                    getConfigPanel().highlightPassword(true);
                ((GPTrainingPanel)this.panel).
                    getConfigPanel().showEditServerSettingsFrame("Please enter your password");
                password = ((GPTrainingPanel)this.panel).getPassword();
                ((GPTrainingPanel)this.panel).
                    getConfigPanel().highlightPassword(false);
            }

            if(password == null)
                password="";
            GPClient server = new GPClient(serverName, userName, password);
            
            JobResult analysisResult = server.runAnalysis(classifierName, parameters);
            String[] outputFiles = analysisResult.getOutputFileNames();

            String modelFileName = null;
            for(int i = 0; i < outputFiles.length; i++)
            {
                String extension = IOUtil.getExtension(outputFiles[i]);
                if(extension.equalsIgnoreCase("odf") || extension.equalsIgnoreCase("model"))
                    modelFileName = outputFiles[i];
            }

            if(modelFileName == null)
                throw new ClassifierException("Error: Classifier model could not be generated");

            //download model result file from server
            AnalysisWebServiceProxy analysisProxy = new AnalysisWebServiceProxy(server.getServer(), server.getUsername(), password);
            String[] resultFiles = new String[1] ;
            resultFiles[0] = modelFileName;

            File[] result = analysisProxy.getResultFiles(analysisResult.getJobNumber(), resultFiles, new File(FilePathnameUtils.getTemporaryFilesDirectoryPath()), true);
            if(result == null || result.length == 0)
                throw new ClassifierException("Error: Could not retrieve training model from GenePattern");

            // save the model of the classifier
            modelFile =  result[0];

            // remove job from GenePattern server
            analysisProxy = new AnalysisWebServiceProxy(server.getServer(), server.getUsername(), password);
            analysisProxy.purgeJob(analysisResult.getJobNumber());
        }
        catch(WebServiceException we)
        {
             we.printStackTrace();

             if(we.getMessage().indexOf(classifierName + " not found on server") != -1)
             {
                throw new ClassifierException(classifierName + " module not found on GenePattern server");
             }
             else
                throw new ClassifierException("Could not connect to GenePattern server");
        }
        catch(ClassifierException ce)
        {
            throw ce;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new ClassifierException("Error creating " + classifierName + " model");
        }

        return modelFile;
    }

    protected CSClassifier trainClassifier(List<float[]> caseData, List<float[]> controlData)
    {
            return trainClassifier(caseData, controlData, null, null, null);
    }

    protected abstract CSClassifier trainClassifier(List<float[]> caseData, List<float[]> controlData, List<String> featureNames,
                                           List<String> caseArrayNames, List<String> controlArrayNames);

    public PredictionModel createModel(File fileName)
    {
        PredictionModel model = new PredictionModel(fileName);

        return model;
    }

    protected void validateFeatureFile(String filename, List<String> featureNames)throws ClassifierException
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
