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
package org.geworkbench.components.gpmodule.classification.wv;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genepattern.matrix.ClassVector;
import org.genepattern.webservice.Parameter;
import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.PredictionModel;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.components.gpmodule.GPDataset;
import org.geworkbench.components.gpmodule.classification.GPClassificationUtils;
import org.geworkbench.components.gpmodule.classification.GPTraining;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.util.TrainingProgressListener;
import org.geworkbench.util.TrainingTask;

/**
 * @author Marc-Danie Nazaire
 * @versino $Id$
 */
public class WVTraining extends GPTraining implements TrainingTask
{
	private static final long serialVersionUID = -2784344084512872041L;

	static Log log = LogFactory.getLog(WVTraining.class);

    TrainingProgressListener trainingProgressListener = null;
    private boolean cancelled = false;

    /* This constructor can be invoked from either EDT or nonEDT. 
     * This may be a design flaw of geWorkbench ccm, but it has to be taken care of properly here for now. */
    public WVTraining()
    {
		if (SwingUtilities.isEventDispatchThread()) {
			panel = new WVTrainingPanel(WVTraining.this);
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						panel = new WVTrainingPanel(WVTraining.this);
					}

				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
        setDefaultPanel(panel);
    }

    protected CSClassifier trainClassifier(List<float[]> caseData, List<float[]> controlData, List<String> featureNames,
                                           List<String> caseArrayNames, List<String> controlArrayNames)
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
                String fileName = GPClassificationUtils.createGCTFile(dataset, "WV_Data");
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
            File clsData = GPClassificationUtils.createCLSFile("WV_Cls", clsVector);

            List<Parameter> parameters = new ArrayList<Parameter>();
            parameters.add(new Parameter("train.filename", trainingDataFile.getAbsolutePath()));
            parameters.add(new Parameter("train.class.filename", clsData.getAbsolutePath()));
            parameters.add(new Parameter("model.file", ++modelCount + trainingDataFile.getName()));
            
            if(wvPanel.useFeatureFileMethod())
            {
                String featureFile = wvPanel.getFeatureFile();
                validateFeatureFile(featureFile, Arrays.asList(dataset.getRowNames()));
                parameters.add(new Parameter("feature.list.filename", featureFile));
            }
            else
            {                
                int numFeatures = wvPanel.getNumFeatures();
                String statistic = wvPanel.getStatistic();
                boolean useMedian = wvPanel.useMedian();

                boolean useStdDev = wvPanel.useMinStdDev();
                int stat = getStatistic(statistic, useMedian, useStdDev);

                parameters.add(new Parameter("num.features", numFeatures));
                parameters.add(new Parameter("feature.selection.statistic", stat));

                if(useStdDev)
                    parameters.add(new Parameter("min.std", wvPanel.getMinStdDev()));
            }

            if(trainingProgressListener != null)
                trainingProgressListener.stepUpdate("training classifier", 2);

            File modelFile = trainData("WeightedVoting", (Parameter[])parameters.toArray(new Parameter[0]));
            PredictionModel predModel = createModel(modelFile);

            wvClassifier = new WVClassifier(null, "WV Classifier", new String[]{"Positive", "Negative"}, 
                            predModel, dataset, casePanel, controlPanel);
            wvClassifier.setPassword(((WVTrainingPanel)panel).getPassword());

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
    
    @Override
    protected String generateParameterHistory() {
    	WVTrainingPanel wvPanel = (WVTrainingPanel)panel;
		String ans = "Num Features: "+  wvPanel.getNumFeatures() + "\n";
		ans+="Feature Selection Statistic: "+ wvPanel.getStatistic()+"\n";
        ans+="Number of Cross Validation Folds: "+ wvPanel.getNumberFolds()+"\n";
        if(wvPanel.useMedian()){
        	ans+= "Median used\n";
        }
        if(wvPanel.useFeatureFileMethod())
        {
            ans += "Feature Filename: "+ wvPanel.getFeatureFile()+"\n";
        }
        if(wvPanel.useMinStdDev()){
        	ans += "Min Std Dev: "+ wvPanel.getMinStdDev()+"\n";
        }
        return ans;
        
    }
}
