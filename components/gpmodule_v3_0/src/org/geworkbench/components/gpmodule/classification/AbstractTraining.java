package org.geworkbench.components.gpmodule.classification;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.SelectorResult;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.properties.DSExtendable;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.ProgressBar;

/**
 * An abstract trainer for a machine learning algorithm.
 *
 * @author John Watkinson
 * @version $Id$
 */
public abstract class AbstractTraining extends AbstractAnalysis implements ClusteringAnalysis {

	private static final long serialVersionUID = -2763961636709389815L;

	static Log log = LogFactory.getLog(AbstractTraining.class);

    protected AbstractTrainingPanel panel;

    @SuppressWarnings("unchecked")
	public AlgorithmExecutionResults execute(Object input) {
        assert (input instanceof DSMicroarraySetView);
        DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
        DSMicroarraySet maSet = view.getMicroarraySet();
        DSItemList<DSGeneMarker> markers = view.markers();

        DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(maSet);

        List<float[]> caseData = new ArrayList<float[]>();
        DSPanel<DSMicroarray> casePanel = context.getActivatedItemsForClass(CSAnnotationContext.CLASS_CASE);
        addMicroarrayData(casePanel, caseData, markers);

        List<float[]> controlData = new ArrayList<float[]>();
        DSPanel<DSMicroarray> controlPanel = context.getActivatedItemsForClass(CSAnnotationContext.CLASS_CONTROL);
        addMicroarrayData(controlPanel, controlData, markers);

        List<float[]> testData = new ArrayList<float[]>();
        DSPanel<DSMicroarray> testPanel = context.getActivatedItemsForClass(CSAnnotationContext.CLASS_TEST);
        addMicroarrayData(testPanel, testData, markers);
        CSClassifier classifier = trainClassifier(caseData, controlData);

    	AlgorithmExecutionResults results = null;
    	
        if (classifier != null)
        {
            String history = generateHistoryString(classifier, view);
            runClassifier(casePanel, controlPanel, testPanel, classifier);
            
            DSExtendable result = null;
        	String classifierName = classifier.getLabel();
        	
            if(classifier instanceof VisualGPClassifier)
            {
            	result = classifier;
            } 
            else {
            	result = new SelectorResult(maSet,classifierName);
            }
        	results = new AlgorithmExecutionResults(true,
				classifierName, result);
        	HistoryPanel.addToHistory(result, history);
        }
        return results;
    }

    public void runClassifier(DSPanel<DSMicroarray> casePanel, DSPanel<DSMicroarray> controlPanel, DSPanel<DSMicroarray> testPanel, CSClassifier classifier)
    {
        if(testPanel == null || testPanel.size() == 0)
            return;
        DSPanel<DSMicroarray> predictedCasePanel = new CSPanel<DSMicroarray>("Predicted Cases");
        DSPanel<DSMicroarray> predictedControlPanel = new CSPanel<DSMicroarray>("Predicted Controls");

        classifyData(testPanel, classifier, predictedCasePanel, predictedControlPanel);

        publishSubpanelChangedEvent(new SubpanelChangedEvent<DSMicroarray>(DSMicroarray.class, predictedCasePanel, SubpanelChangedEvent.NEW));
        publishSubpanelChangedEvent(new SubpanelChangedEvent<DSMicroarray>(DSMicroarray.class, predictedControlPanel, SubpanelChangedEvent.NEW));
    }

    public static void addMicroarrayData(DSPanel<DSMicroarray> panel, List<float[]> dataToAddTo, DSItemList<DSGeneMarker> markers) {
        int m = markers.size();
        for (DSMicroarray microarray : panel) {
            float[] data = new float[m];
            for (int i = 0; i < m; i++) {
                data[i] = (float)microarray.getMarkerValue(markers.get(i)).getValue();
            }
            dataToAddTo.add(data);
        }
    }

    public static void classifyData(DSPanel<DSMicroarray> panel, CSClassifier classifier, DSPanel<DSMicroarray> predictedCasesPanel, DSPanel<DSMicroarray> predictedControlsPanel)
    {
        int size = panel.size();
        ProgressBar progressBar = ProgressBar.create(ProgressBar.BOUNDED_TYPE);
        progressBar.setTitle("Classification Progress");
        progressBar.setBounds(new org.geworkbench.util.ProgressBar.IncrementModel(0, size, 0, size, 1));
        progressBar.setAlwaysOnTop(true);
        progressBar.showValues(false);

        progressBar.start();

        int count = 0;
        for (DSMicroarray microarray : panel) {
            if (classifier.classify(microarray.getRawMarkerData()) == 0)
            {
                predictedCasesPanel.add(microarray);
            }
            else
            {
                predictedControlsPanel.add(microarray);
            }

            count++;
            progressBar.update();
            progressBar.setMessage("Classified " + count + " out of " + size);
        }

        progressBar.stop();
    }

    /**
     * Implementing classes should train a classifier based on the data provided.
     */
    protected abstract CSClassifier trainClassifier(List<float[]> caseData, List<float[]> controlData);

    @Publish public SubpanelChangedEvent<DSMicroarray> publishSubpanelChangedEvent(SubpanelChangedEvent<DSMicroarray> event) {
        return event;
    }

    @Publish public ProjectNodeAddedEvent publishProjectNodeAddedEvent(ProjectNodeAddedEvent event) {
        return event;
    }

    @Subscribe public void receive(ProjectEvent event, Object source) {
        log.debug("abstracttraining received project event.");

        DSDataSet<?> dataSet = event.getDataSet();
        if ((dataSet != null) && (dataSet instanceof DSMicroarraySet)) {
            panel.setMaSet((DSMicroarraySet) dataSet);
            panel.rebuildForm();
        }
    }

    @Subscribe public void receive(org.geworkbench.events.GeneSelectorEvent e, Object source) {
        log.debug("Received gene selection event.");
        if (panel.getMaSet() != null) {
            DSPanel<DSGeneMarker> selectionPanel = e.getPanel();
            log.debug("Setting selection in parent panel.");
            panel.setMarkerPanel(selectionPanel);
        }
    }

    @Subscribe public void receive(PhenotypeSelectorEvent<DSMicroarray> event, Object source) {
        panel.rebuildForm();
    }

    public int getAnalysisType() {
        return AbstractAnalysis.TTEST_TYPE;
    }

    protected String generateParameterHistory() {
    	return "";
    }

    public String generateHistoryString(CSClassifier classifier, DSMicroarraySetView<DSGeneMarker, DSMicroarray> view)
    {
        String history="";

        String classifierName = classifier.getLabel();
        if(classifierName != null && classifierName.length() != 0)
        history = "Generated by " + classifierName + " run with the following parameters: \n";
		history += "----------------------------------------\n";

		history+=generateParameterHistory();
		
		history+=generateHistoryForMaSetView(view);
        
		return history;
	}
}
