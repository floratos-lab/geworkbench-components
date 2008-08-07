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
package org.geworkbench.components.gpmodule.classification.knn;

import org.geworkbench.components.gpmodule.classification.GPTraining;
import org.geworkbench.components.gpmodule.classification.PredictionModel;
import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.util.TrainingTask;
import org.geworkbench.util.TrainingProgressListener;
import org.genepattern.matrix.ClassVector;
import org.genepattern.matrix.DefaultClassVector;
import org.genepattern.webservice.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

/**
 * @author Marc-Danie Nazaire
 */
public class KNNTraining extends GPTraining implements TrainingTask
{
    static Log log = LogFactory.getLog(KNNTraining.class);

    TrainingProgressListener trainingProgressListener = null;
    private boolean cancelled = false;

    public KNNTraining()
    {
       setLabel("KNN Classifier");
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

    protected CSClassifier trainClassifier(List<float[]> caseData, List<float[]> controlData)
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
            DSItemList markers = knnPanel.getActiveMarkers();

            List featureNames = new ArrayList();
            for(int i =0; i < markers.size();i++)
            {
                featureNames.add(((DSGeneMarker)markers.get(i)).getLabel());
            }

            List trainingSet = new ArrayList<double[]>();
            trainingSet.addAll(controlData);
            trainingSet.addAll(caseData);

            File trainingData = createGCTFile("KNN_Data", trainingSet, featureNames);

            int sampleSize = caseData.size() + controlData.size();
            String[] classLabels = new String[sampleSize];
            for(int i = 0; i < sampleSize; i++)
            {
                if(i < controlData.size())
                    classLabels[i] = "Control";
                else
                    classLabels[i] = "Case";
            }

            ClassVector classVec = new DefaultClassVector(classLabels);
            File clsData = createCLSFile("KNN_Cls", classVec);

            int numNeighbors = knnPanel.getNumNeighbors();
            int weightType = getWeightType(knnPanel.getWeightType());

            List parameters = new ArrayList();
            parameters.add(new Parameter("train.filename", trainingData.getAbsolutePath()));
            parameters.add(new Parameter("train.class.filename", clsData.getAbsolutePath()));
            parameters.add(new Parameter("model.file", ++modelCount + trainingData.getName()));

            if(knnPanel.useFeatureFileMethod())
            {
                String featureFile = knnPanel.getFeatureFile();
                validateFeatureFile(featureFile, featureNames);

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
                       
            ArrayList knnParams = new ArrayList();
            knnParams.add(new Parameter("num.neighbors", numNeighbors));
            knnParams.add(new Parameter("weighting.type", weightType));
            knnParams.add(new Parameter("distance.measure", getDistance(knnPanel.getDistanceMeasure())));

            if(trainingProgressListener != null)
                trainingProgressListener.stepUpdate("training classifier", 2);

            parameters.addAll(knnParams);
            PredictionModel predModel = createModel("KNN", (Parameter[])parameters.toArray(new Parameter[0]));

            knnClassifier = new KNNClassifier(null, "KNN Classifier", new String[]{"Positive", "Negative"}, predModel, featureNames, knnParams);
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
}
