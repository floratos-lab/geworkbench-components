/**
 *
 * @author zm2165
 * @version $Id$
 */
package org.geworkbench.components.gpmodule.classification.kmeans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.TrainingProgressListener;
import org.geworkbench.util.TrainingTask;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.components.gpmodule.classification.GPTraining;
import org.geworkbench.components.gpmodule.classification.GPTrainingPanel;
import org.geworkbench.components.gpmodule.classification.PredictionModel;
import org.geworkbench.components.gpmodule.classification.PredictionResult;
import org.geworkbench.components.gpmodule.classification.GPClassificationUtils;
import org.geworkbench.components.gpmodule.GPDataset;
import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.genepattern.client.GPClient;
import org.genepattern.io.IOUtil;
import org.genepattern.matrix.ClassVector;
import org.genepattern.util.GPpropertiesManager;
import org.genepattern.webservice.AnalysisWebServiceProxy;
import org.genepattern.webservice.JobResult;
import org.genepattern.webservice.Parameter;
import org.genepattern.webservice.WebServiceException;

import javax.swing.*;
import java.util.List;
import java.util.Arrays;
import java.io.File;
import java.io.IOException;


/**
 * @author zm2165
 * @version $Id$
 */
public class KMeansClustering extends GPTraining //implements TrainingTask
{
    static Log log = LogFactory.getLog(KMeansClustering.class);

    TrainingProgressListener trainingProgressListener = null;
    private String clusterNum;

    public KMeansClustering()
    {
        panel = new KMeansPanel(this);
        setDefaultPanel(panel);
    }

    protected CSClassifier trainClassifier(List<float[]> caseData, List<float[]> controlData, List<String> featureNames,
                                           List<String> caseArrayNames, List<String> controlArrayNames)
    {
        log.debug("Training classifier.");

        KMeansClassifier svmClassifier = null;

        try
        {
        	/*
            if(controlData.size() == 0)
                throw new ClassifierException("Control data must be provided");

            if(caseData.size() == 0)
                throw new ClassifierException("Case data must be provided");
*/
            DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(panel.getMaSet());
            DSPanel<DSMicroarray> casePanel = context.getActivatedItemsForClass(CSAnnotationContext.CLASS_CASE);

            DSPanel<DSMicroarray> controlPanel = context.getActivatedItemsForClass(CSAnnotationContext.CLASS_CONTROL);

            if(caseArrayNames == null || caseArrayNames.size() == 0)
                caseArrayNames = getArrayNames(casePanel);
            if(controlArrayNames == null || controlArrayNames.size() == 0)
                controlArrayNames = getArrayNames(controlPanel);
            
            GPDataset dataset=null;
/*
            //Create gct file
            dataset = createGCTDataset(caseData, controlData, caseArrayNames,
                                                      controlArrayNames);

            File trainingDataFile;

            try
            {
                String fileName  = GPClassificationUtils.createGCTFile(dataset, "SVM_Data");
                trainingDataFile = new File(fileName);
                trainingDataFile.deleteOnExit();
            }
            catch(IOException io)
            {
                io.printStackTrace();
                throw new ClassifierException("An error occurred when training SVM classifier");
            }
*/
            //Create cls file
            ClassVector clsVector = createClassVector(caseData, controlData);
            File clsData = GPClassificationUtils.createCLSFile("SVM_Cls", clsVector);

            clusterNum=((KMeansPanel)panel).getNumClusters();
            //Set parameters for running the module
            Parameter[] parameters = new Parameter[6];
            parameters[0] = new Parameter("input.filename", "c:\\temp\\all_aml_test.res");
            parameters[1] = new Parameter("output.base.name", "<input.filename_basename>_KMcluster_output");
            parameters[2] = new Parameter("number.of.clusters", clusterNum); 
            parameters[3] = new Parameter("seed.value", "12345");
            parameters[4] = new Parameter("cluster.by", "0");
            parameters[5] = new Parameter("distance.metric", "0");

            //Run module and get model result file
            File modelFile = trainData("urn:lsid:broad.mit.edu:cancer.software.genepattern.module.analysis:00081:1", parameters);
            PredictionModel model = new PredictionModel(modelFile);
/*
            svmClassifier = new KMeansClassifier(panel.getMaSet(), "SVM Classifier",
                    new String[]{"Positive", "Negative"}, model, dataset, casePanel, controlPanel);
            svmClassifier.setPassword(((KMeansPanel)panel).getPassword());
            */
        }
        catch(ClassifierException e)
        {
            JOptionPane.showMessageDialog(panel, e.getMessage());
            log.warn(e);
        }

        return svmClassifier;
    }

    public void runClassifier(DSPanel<DSMicroarray> casePanel, DSPanel<DSMicroarray> controlPanel, DSPanel<DSMicroarray> testPanel, CSClassifier classifier)
    {
  /*  	
        ProgressBar progressBar;
        progressBar = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
        progressBar.setTitle("Running classifier on train data");
        progressBar.setAlwaysOnTop(false);
        progressBar.showValues(false);

        progressBar.start();

        DSPanel<DSMicroarray> trainPanel = new CSPanel();
        trainPanel.addAll(controlPanel);
        trainPanel.addAll(casePanel);
        KMeansClassifier svmClassifier = ((KMeansClassifier)classifier);

        String[] classLabels = new String[trainPanel.size()];
        Arrays.fill(classLabels, 0, controlPanel.size(), "Control");
        Arrays.fill(classLabels, controlPanel.size(), trainPanel.size(), "Case");

        PredictionResult trainResult = null;
        try
        {
            trainResult = svmClassifier.classify(trainPanel, classLabels);
            svmClassifier.setTrainPredResult(trainResult);
        }
        catch(Exception e)
        {
            log.error(e);
        }
        finally
        {
            progressBar.stop();
        }

        if(trainResult == null)
            return;

        if(testPanel == null || testPanel.size() == 0)
        {
            publishProjectNodeAddedEvent(new ProjectNodeAddedEvent(classifier.getLabel(), null, classifier));
            return;
        }

        progressBar = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
        progressBar.setTitle("Running classifier on test data");
        progressBar.setAlwaysOnTop(false);
        progressBar.showValues(false);

        progressBar.start();

        PredictionResult testResult = null;
        try
        {
            testResult = svmClassifier.classify(testPanel, null);
            svmClassifier.setTestPredResult(testResult);
        }
        catch(Exception e)
        {
            log.error(e);
        }
        finally
        {
            progressBar.stop();
        }

        publishProjectNodeAddedEvent(new ProjectNodeAddedEvent(classifier.getLabel(), null, classifier));
  */
  }
    
    public boolean isCancelled()
    {
        return false;
    }

    public void setCancelled(boolean cancel){}

    public TrainingProgressListener getTrainingProgressListener()
    {
        return null;
    }

    public void setTrainingProgressListener(TrainingProgressListener trainingProgressListener){}
    
    @Override
    public File trainData(String classifierName, Parameter[] parameters) throws ClassifierException
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
            
            JobResult analysisResult = server.runAnalysis(classifierName, parameters);//FIX ME LATER
            //JobResult analysisResult=server.runAnalysis("urn:lsid:broad.mit.edu:cancer.software.genepattern.module.analysis:00081:1", new Parameter[]{new Parameter("input.filename", "c:\\temp\\all_aml_test.res"), new Parameter("output.base.name", "<input.filename_basename>_KMcluster_output"), new Parameter("number.of.clusters", "7"), new Parameter("seed.value", "12345"), new Parameter("cluster.by", "0"), new Parameter("distance.metric", "0")});
            String[] outputFiles = analysisResult.getOutputFileNames();

            String modelFileName = null;
            modelFileName ="stdout.txt";
/*          
            for(int i = 0; i < outputFiles.length; i++)
            {
                String extension = IOUtil.getExtension(outputFiles[i]);
                if(extension.equalsIgnoreCase("odf") || extension.equalsIgnoreCase("model"))
                    modelFileName = outputFiles[i];
            }

            if(modelFileName == null)
                throw new ClassifierException("Error: Classifier model could not be generated");
*/
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
    
}