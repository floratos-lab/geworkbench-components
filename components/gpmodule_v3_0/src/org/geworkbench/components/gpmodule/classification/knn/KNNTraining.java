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
package org.geworkbench.components.gpmodule.classification.knn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.components.gpmodule.GPDataset;
import org.geworkbench.components.gpmodule.classification.GPClassificationUtils;
import org.geworkbench.components.gpmodule.classification.GPTraining;
import org.geworkbench.components.gpmodule.classification.PredictionModel;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.util.TrainingProgressListener;
import org.geworkbench.util.TrainingTask;

/**
 * @author Marc-Danie Nazaire
 * @version $Id$
 */
public class KNNTraining extends GPTraining implements TrainingTask
{
	private static final long serialVersionUID = 7983960185175470769L;

	static Log log = LogFactory.getLog(KNNTraining.class);

    TrainingProgressListener trainingProgressListener = null;
    private boolean cancelled = false;

    public KNNTraining()
    {
       panel = new KNNTrainingPanel(this);
       setDefaultPanel(panel);
    }

    private int getWeightType(String weightType)
    {
        if(weightType.equals("one-over-k"))
        {   return 2;   }
        else if(weightType.equals("distance"))
        {    return 3;  }
        else
        {   return 1;   }
    }

    private int getDistance(String distance)
    {
        if(distance.equals("Euclidean"))
        {   return 1;   }
        else
        {   return 2;   }
    }

    protected CSClassifier trainClassifier(List<float[]> caseData, List<float[]> controlData, List<String> featureNames,
                                           List<String> caseArrayNames, List<String> controlArrayNames)
    {
        log.debug("Training classifier.");

        KNNClassifier knnClassifier = null;

        try
        {
            if(controlData.size() == 0)
                throw new ClassifierException("Control data must be provided");

            if(caseData.size() == 0)
                throw new ClassifierException("Case data must be provided");

            if(trainingProgressListener != null)
                trainingProgressListener.stepUpdate("processing training parameters", 1);

            KNNTrainingPanel knnPanel = (KNNTrainingPanel)panel;
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
                String fileName = GPClassificationUtils.createGCTFile(dataset, "KNN_Data");
                trainingDataFile = new File(fileName);
                trainingDataFile.deleteOnExit();
            }
            catch(IOException io)
            {
                io.printStackTrace();
                throw new ClassifierException("An error occurred when training SVM classifier");
            }

            //Create cls file
            ClassVector clsVector = createClassVector(caseData, controlData);
            File clsData = GPClassificationUtils.createCLSFile("KNN_Cls", clsVector);

            int numNeighbors = knnPanel.getNumNeighbors();
            int weightType = getWeightType(knnPanel.getWeightType());

            List<Parameter> parameters = new ArrayList<Parameter>();
            parameters.add(new Parameter("train.filename", trainingDataFile.getAbsolutePath()));
            parameters.add(new Parameter("train.class.filename", clsData.getAbsolutePath()));
            parameters.add(new Parameter("model.file", ++modelCount + trainingDataFile.getName()));

            if(knnPanel.useFeatureFileMethod())
            {
                String featureFile = knnPanel.getFeatureFile();
                validateFeatureFile(featureFile, Arrays.asList(dataset.getRowNames()));

                parameters.add(new Parameter("feature.list.filename", featureFile));
            }
            else
            {
                int numFeatures = knnPanel.getNumFeatures();
                String statistic = knnPanel.getStatistic();
                boolean useMedian = knnPanel.useMedian();
                boolean useStdDev = knnPanel.useMinStdDev();
                int stat = getStatistic(statistic, useMedian, useStdDev);

                parameters.add(new Parameter("num.features", numFeatures));
                parameters.add(new Parameter("feature.selection.statistic", stat));

                if(useStdDev)
                    parameters.add(new Parameter("min.std", knnPanel.getMinStdDev()));
            }
                       
            ArrayList<Parameter> knnParams = new ArrayList<Parameter>();
            knnParams.add(new Parameter("num.neighbors", numNeighbors));
            knnParams.add(new Parameter("weighting.type", weightType));
            knnParams.add(new Parameter("distance.measure", getDistance(knnPanel.getDistanceMeasure())));

            if(trainingProgressListener != null)
                trainingProgressListener.stepUpdate("training classifier", 2);

            parameters.addAll(knnParams);

            File modelFile = trainData("KNN", (Parameter[])parameters.toArray(new Parameter[0]));
            PredictionModel predModel = createModel(modelFile);

            knnClassifier = new KNNClassifier(null, "KNN Classifier", new String[]{"Positive", "Negative"},
                    predModel, dataset, casePanel, controlPanel, knnParams);
            knnClassifier.setPassword(((KNNTrainingPanel)panel).getPassword());

            if(trainingProgressListener != null)
                trainingProgressListener.stepUpdate("classifier trained", 3);

            trainingProgressListener = null;
        }
        catch(ClassifierException e)
        {
            JOptionPane.showMessageDialog(panel, e.getMessage());
            log.warn(e);
        }

        return knnClassifier;
    }

    public TrainingProgressListener getTrainingProgressListener()
    {
        return trainingProgressListener;
    }

    public void setTrainingProgressListener(TrainingProgressListener trainingProgressListener)
    {
        this.trainingProgressListener = trainingProgressListener;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }

    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }
    
    @Override
    protected String generateParameterHistory() {
    	KNNTrainingPanel knnPanel = (KNNTrainingPanel)panel;
    	String ans="";
	    if(knnPanel.useFeatureFileMethod())
        {
            ans += "Feature Filename: "+ knnPanel.getFeatureFile()+"\n";
        }
        else
        {
        	ans+="Num Features: "+  knnPanel.getNumFeatures() + "\n";
			ans+="Feature Selection Statistic: "+ knnPanel.getStatistic()+"\n";
		    if(knnPanel.useMedian())
		    {
        		ans+= "Median used\n";
            }
	        if(knnPanel.useMinStdDev())
	        {
        		ans += "Min Std Dev: "+ knnPanel.getMinStdDev()+"\n";
        	}
        }
    	ans+="Num Neighbors: " +knnPanel.getNumNeighbors()+ "\n";
    	ans+="Neighbor Weight Type: " + knnPanel.getWeightType()+ "\n";
    	ans+="Distance measure: "+knnPanel.getDistanceMeasure()+ "\n";
        ans+="Number of Cross Validation Folds: "+ knnPanel.getNumberFolds()+"\n";

        return ans;
        
    }
}
