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
package org.geworkbench.components.gpmodule.classification.cart;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genepattern.matrix.ClassVector;
import org.genepattern.webservice.Parameter;
import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.components.gpmodule.GPDataset;
import org.geworkbench.components.gpmodule.classification.GPClassificationUtils;
import org.geworkbench.components.gpmodule.classification.GPTraining;
import org.geworkbench.components.gpmodule.classification.GPTrainingPanel;
import org.geworkbench.components.gpmodule.classification.PredictionModel;
import org.geworkbench.components.gpmodule.classification.PredictionResult;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.TrainingProgressListener;
import org.geworkbench.util.TrainingTask;

/**
 * @author Marc-Danie Nazaire
 */
public class CARTTraining extends GPTraining implements TrainingTask
{
	private static final long serialVersionUID = -5272258841692641867L;

	static Log log = LogFactory.getLog(CARTTraining.class);

    TrainingProgressListener trainingProgressListener = null;

    public CARTTraining()
    {
        panel = new CARTTrainingPanel(this);
        setDefaultPanel(panel);
    }

    protected CSClassifier trainClassifier(List<float[]> caseData, List<float[]> controlData, List<String> featureNames,
                                           List<String> caseArrayNames, List<String> controlArrayNames)
    {
        log.debug("Training classifier.");
        CARTClassifier classifier = null;

        try
        {
            if(controlData.size() == 0)
                throw new ClassifierException("Control data must be provided");

            if(caseData.size() == 0)
                throw new ClassifierException("Case data must be provided");

            DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(panel.getMaSet());
            DSPanel<DSMicroarray> casePanel = context.getActivatedItemsForClass(CSAnnotationContext.CLASS_CASE);

            DSPanel<DSMicroarray> controlPanel = context.getActivatedItemsForClass(CSAnnotationContext.CLASS_CONTROL);

            if(caseArrayNames == null || caseArrayNames.size() == 0)
                caseArrayNames = getArrayNames(casePanel);
            if(controlArrayNames == null || controlArrayNames.size() == 0)
                controlArrayNames = getArrayNames(controlPanel);

            //Create gct file
            GPDataset dataset = createGCTDataset(caseData, controlData, caseArrayNames,
                                                     controlArrayNames);
            File trainingDataFile;

            try
            {
                String fileName = GPClassificationUtils.createGCTFile(dataset, "CART_Data");
                trainingDataFile = new File(fileName);
                trainingDataFile.deleteOnExit();
            }
            catch(IOException io)
            {
                io.printStackTrace();
                throw new ClassifierException("An error occurred when training CART classifier");
            }

            //Create cls file
            ClassVector clsVector = createClassVector(caseData, controlData);
            File clsData = GPClassificationUtils.createCLSFile("CART_Cls", clsVector);

            //Set parameters for running the module
            Parameter[] parameters = new Parameter[3];
            parameters[0] = new Parameter("train.data.filename", trainingDataFile.getAbsolutePath());
            parameters[1] = new Parameter("train.cls.filename", clsData.getAbsolutePath());
            parameters[2] = new Parameter("model.output.file", ++modelCount + trainingDataFile.getName() + ".model");

            //Run module and get model result file
            File modelFile = trainData("CART", parameters);
            PredictionModel model = new PredictionModel(modelFile);

            classifier = new CARTClassifier(panel.getMaSet(), "CART Classifier",
                    new String[]{"Positive", "Negative"}, model, dataset, casePanel, controlPanel);
            classifier.setPassword(((GPTrainingPanel)panel).getPassword());
        }
        catch(ClassifierException e)
        {
            JOptionPane.showMessageDialog(panel, e.getMessage());
            log.warn(e);
        }

        return classifier;
    }

    @SuppressWarnings("unchecked")
	public void runClassifier(DSPanel<DSMicroarray> casePanel, DSPanel<DSMicroarray> controlPanel, DSPanel<DSMicroarray> testPanel, CSClassifier classifier)
    {
        ProgressBar progressBar;
        progressBar = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
        progressBar.setTitle("Running classifier on train data");
        progressBar.setAlwaysOnTop(false);
        progressBar.showValues(false);

        progressBar.start();

        DSPanel<DSMicroarray> trainPanel = new CSPanel<DSMicroarray>();
        trainPanel.addAll(controlPanel);
        trainPanel.addAll(casePanel);
        CARTClassifier svmClassifier = ((CARTClassifier)classifier);

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
}
