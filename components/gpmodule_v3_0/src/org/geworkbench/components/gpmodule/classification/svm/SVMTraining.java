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
package org.geworkbench.components.gpmodule.classification.svm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.TrainingProgressListener;
import org.geworkbench.util.TrainingTask;
import org.geworkbench.components.gpmodule.classification.GPTraining;
import org.geworkbench.bison.algorithm.classification.CSClassifier;

import java.util.List;


/**
 * @author Marc-Danie Nazaire
 */
public class SVMTraining extends GPTraining implements TrainingTask
{
    static Log log = LogFactory.getLog(SVMTraining.class);

    TrainingProgressListener trainingProgressListener = null;
    private boolean cancelled = false;

    public SVMTraining()
    {
        setLabel("SVM Classifier");
        panel = new SVMTrainingPanel(this);
        setDefaultPanel(panel);
    }

    protected CSClassifier trainClassifier(List<float[]> caseData, List<float[]> controlData)
    {
        log.debug("Training classifier.");

        SVMClassifier svmClassifier = null;


        return svmClassifier;
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