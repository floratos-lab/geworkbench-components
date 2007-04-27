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
package org.geworkbench.components.gpmodule.classification.wv;

import org.geworkbench.components.gpmodule.classification.GPTraining;
import org.geworkbench.components.gpmodule.classification.PredictionModel;
import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.util.TrainingTask;
import org.geworkbench.util.TrainingProgressListener;
import org.genepattern.data.matrix.ClassVector;
import org.genepattern.webservice.Parameter;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.apache.commons.logging.Log;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

/**
 * @author Marc-Danie Nazaire
 */
public class WVTraining extends GPTraining implements TrainingTask
{
    static Log log = LogFactory.getLog(WVTraining.class);

    TrainingProgressListener trainingProgressListener = null;
    private boolean cancelled = false;

    public WVTraining()
    {
        setLabel("WV Classifier");
        panel = new WVTrainingPanel(this);
        setDefaultPanel(panel);
    }

    protected CSClassifier trainClassifier(List<float[]> caseData, List<float[]> controlData)
    {
        log.debug("Training classifier.");
       
        WVClassifier wvClassifier = null;

        try
        {
            if(controlData.size() == 0)
                throw new ClassifierException("Control data must be provided");

            if(caseData.size() == 0)
                throw new ClassifierException("Case data must be provided");

            if(trainingProgressListener != null)
                trainingProgressListener.stepUpdate("processing training parameters", 1);

            WVTrainingPanel wvPanel = (WVTrainingPanel)panel;
            DSItemList markers = wvPanel.getActiveMarkers();

            List featureNames = new ArrayList();
            for(int i =0; i < markers.size(); i++)
            {
                featureNames.add(((DSGeneMarker)markers.get(i)).getLabel());
            }

            List trainingSet = new ArrayList<double[]>();
            trainingSet.addAll(controlData);
            trainingSet.addAll(caseData);

            File trainingData = createGCTFile("WV_Data", trainingSet, featureNames);

            int sampleSize = caseData.size() + controlData.size();
            String[] classLabels = new String[sampleSize];
            for(int i = 0; i < sampleSize; i++)
            {
                if(i < controlData.size())
                    classLabels[i] = "Control";
                else
                    classLabels[i] = "Case";
            }

            ClassVector classVec = new ClassVector(classLabels);
            File clsData = createCLSFile("WV_Cls", classVec);

            List parameters = new ArrayList();
            if(wvPanel.useFeatureFileMethod())
            {
                String featureFile = wvPanel.getFeatureFile();
                validateFeatureFile(featureFile, featureNames);
                parameters.add(new Parameter("train.filename", trainingData.getAbsolutePath()));
                parameters.add(new Parameter("train.class.filename", clsData.getAbsolutePath()));
                parameters.add(new Parameter("feature.list.filename", featureFile));
            }
            else
            {                
                int numFeatures = wvPanel.getNumFeatures();
                String statistic = wvPanel.getStatistic();
                boolean useMedian = wvPanel.useMedian();

                boolean useStdDev = wvPanel.useMinStdDev();
                int stat = getStatistic(statistic, useMedian, useStdDev);

                parameters.add(new Parameter("train.filename", trainingData.getAbsolutePath()));
                parameters.add(new Parameter("train.class.filename", clsData.getAbsolutePath()));
                parameters.add(new Parameter("num.features", numFeatures));
                parameters.add(new Parameter("feature.selection.statistic", stat));

                if(useStdDev)
                    parameters.add(new Parameter("min.std", wvPanel.getMinStdDev()));
            }

            if(trainingProgressListener != null)
                trainingProgressListener.stepUpdate("training classifier", 2);

            PredictionModel predModel = createModel("WeightedVoting", (Parameter[])parameters.toArray(new Parameter[0]));

            wvClassifier = new WVClassifier(null, "WV Classifier", new String[]{"Positive", "Negative"}, predModel, featureNames);

            if(trainingProgressListener != null)
                trainingProgressListener.stepUpdate("classifier trained", 3);

            trainingProgressListener = null;
        }
        catch(ClassifierException e)
        {
            JOptionPane.showMessageDialog(panel, e.getMessage());
            log.warn(e);
        }

        return wvClassifier;
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
