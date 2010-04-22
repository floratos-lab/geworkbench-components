package org.geworkbench.components.gpmodule.classification.cart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.components.gpmodule.classification.GPTraining;
import org.geworkbench.components.gpmodule.classification.PredictionModel;
import org.geworkbench.util.TrainingTask;
import org.geworkbench.util.TrainingProgressListener;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.genepattern.webservice.Parameter;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: nazaire
 * Date: Apr 20, 2010
 * Time: 3:25:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class CARTTraining extends GPTraining implements TrainingTask
{
    static Log log = LogFactory.getLog(CARTTraining.class);

    TrainingProgressListener trainingProgressListener = null;

    public CARTTraining()
    {
        panel = new CARTTrainingPanel(this);
        setDefaultPanel(panel);
    }

    protected CSClassifier trainClassifier(List<float[]> caseData, List<float[]> controlData)
    {
        log.debug("Training classifier.");

       // SVMClassifier svmClassifier = null;

        try
        {
            if(controlData.size() == 0)
                throw new ClassifierException("Control data must be provided");

            if(caseData.size() == 0)
                throw new ClassifierException("Case data must be provided");

            CARTTrainingPanel cartPanel = (CARTTrainingPanel)panel;
            DSItemList markers = cartPanel.getActiveMarkers();

            List featureNames = new ArrayList();
            for(int i =0; i < markers.size();i++)
            {
                featureNames.add(((DSGeneMarker)markers.get(i)).getLabel());
            }

            List trainingSet = new ArrayList<double[]>();
            trainingSet.addAll(controlData);

            List arrayNames = new ArrayList();
            DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(cartPanel.getMaSet());
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

            File trainingData = createGCTFile("CART_Data", trainingSet, featureNames, arrayNames);


            File clsData = createCLSFile("CART_Cls", caseData, controlData);

            Parameter[] parameters = new Parameter[3];
            parameters[0] = new Parameter("train.data.filename", trainingData.getAbsolutePath());
            parameters[1] = new Parameter("train.cls.filename", clsData.getAbsolutePath());
            parameters[2] = new Parameter("model.output.file", ++modelCount + trainingData.getName());

            File modelFile = trainData("CART", parameters);
            PredictionModel model = new PredictionModel(modelFile);            
        }
        catch(ClassifierException e)
        {
            JOptionPane.showMessageDialog(panel, e.getMessage());
            log.warn(e);
        }

        return null;
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
