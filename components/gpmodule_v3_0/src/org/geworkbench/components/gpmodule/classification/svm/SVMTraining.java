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
import org.geworkbench.util.ClassifierException;
import org.geworkbench.components.gpmodule.classification.GPTraining;
import org.geworkbench.components.gpmodule.classification.PredictionModel;
import org.geworkbench.components.gpmodule.classification.PredictionResult;
import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.genepattern.matrix.ClassVector;
import org.genepattern.matrix.DefaultClassVector;
import org.genepattern.webservice.Parameter;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.io.File;


/**
 * @author Marc-Danie Nazaire
 */
public class SVMTraining extends GPTraining implements TrainingTask
{
    static Log log = LogFactory.getLog(SVMTraining.class);

    TrainingProgressListener trainingProgressListener = null;

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

        try
        {
            if(controlData.size() == 0)
                throw new ClassifierException("Control data must be provided");

            if(caseData.size() == 0)
                throw new ClassifierException("Case data must be provided");

            SVMTrainingPanel svmPanel = (SVMTrainingPanel)panel;
            DSItemList markers = svmPanel.getActiveMarkers();

            List featureNames = new ArrayList();
            for(int i =0; i < markers.size();i++)
            {
                featureNames.add(((DSGeneMarker)markers.get(i)).getLabel());
            }

            List trainingSet = new ArrayList<double[]>();
            trainingSet.addAll(controlData);

            List arrayNames = new ArrayList();
            DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(svmPanel.getMaSet());
            DSPanel<DSMicroarray> dsPanel = context.getActivatedItemsForClass(CSAnnotationContext.CLASS_CONTROL);
            for(DSMicroarray microarray: dsPanel)
            {
                arrayNames.add(microarray.getLabel());
            }

            trainingSet.addAll(caseData);
            dsPanel = context.getActivatedItemsForClass(CSAnnotationContext.CLASS_CASE);
            for(DSMicroarray microarray: dsPanel)
            {
                arrayNames.add(microarray.getLabel());
            }

            File trainingData = createGCTFile("SVM_Data", trainingSet, featureNames, arrayNames);


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
            File clsData = createCLSFile("SVM_Cls", classVec);

            Parameter[] parameters = new Parameter[3];
            parameters[0] = new Parameter("train.data.filename", trainingData.getAbsolutePath());
            parameters[1] = new Parameter("train.cls.filename", clsData.getAbsolutePath());
            parameters[2] = new Parameter("model.output.file", ++modelCount + trainingData.getName());

            File modelFile = trainData("SVM", parameters);
            //byte[] model = read(modelFile);
            PredictionModel model = new PredictionModel(modelFile);

            svmClassifier = new SVMClassifier(null, "SVM Classifier", new String[]{"Positive", "Negative"}, model, featureNames);
            svmClassifier.setPassword(((SVMTrainingPanel)panel).getPassword());

            //testclassifier on training data
            parameters = new Parameter[4];
            parameters[0] = new Parameter("test.data.filename", trainingData.getAbsolutePath());
            parameters[1] = new Parameter("test.cls.filename", clsData.getAbsolutePath());
            parameters[2] = new Parameter("saved.model.filename", modelFile.getAbsolutePath());
            parameters[3] = new Parameter("pred.results.output.file", modelCount + trainingData.getName()+ "pred");

            PredictionResult predResult = svmClassifier.runPredictor("SVM", parameters);
            svmClassifier.setTrainPredResult(predResult);
        }
        catch(ClassifierException e)
        {
            JOptionPane.showMessageDialog(panel, e.getMessage());
            log.warn(e);
        }

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