package org.geworkbench.components.analysis.classification;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.algorithm.classification.Classifier;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.components.analysis.classification.svm.SVMTrainingPanel;

import java.util.List;
import java.util.ArrayList;

/**
 * @author John Watkinson
 */
public abstract class AbstractTraining extends AbstractAnalysis {
    protected AbstractTrainingPanel panel;

    public AlgorithmExecutionResults execute(Object input) {
        assert (input instanceof DSMicroarraySetView);
        DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
        DSMicroarraySet maSet = view.getMicroarraySet();
        DSItemList<DSGeneMarker> markers = view.markers();

        DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(maSet);

        List<float[]> caseData = new ArrayList<float[]>();
        addMicroarrayData(context.getActivatedItemsForClass(CSAnnotationContext.CLASS_CASE), caseData, markers);
        List<float[]> controlData = new ArrayList<float[]>();
        addMicroarrayData(context.getActivatedItemsForClass(CSAnnotationContext.CLASS_CONTROL), controlData, markers);
        List<float[]> testData = new ArrayList<float[]>();
        DSPanel<DSMicroarray> testPanel = context.getActivatedItemsForClass(CSAnnotationContext.CLASS_TEST);
        addMicroarrayData(testPanel, testData, markers);

        Classifier classifier = trainClassifier(caseData, controlData);

        if (classifier != null) {
            publishProjectNodeAddedEvent(new ProjectNodeAddedEvent(classifier.getLabel(), null, classifier));
            if (testData.size() > 0) {
                DSPanel<DSMicroarray> newPanel = new CSPanel<DSMicroarray>("Classification Results");
                classifyData(testPanel, classifier, newPanel);
                publishSubpanelChangedEvent(new SubpanelChangedEvent<DSMicroarray>(DSMicroarray.class, newPanel, SubpanelChangedEvent.NEW));
            }
        }
        return null;
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

    public static void classifyData(DSPanel<DSMicroarray> panel, Classifier classifier, DSPanel<DSMicroarray> newGroupPanel) {
        for (DSMicroarray microarray : panel) {
            if (classifier.classify(microarray.getRawMarkerData()).equals(classifier.getClassifications()[0])) {
                newGroupPanel.add(microarray);
            }
        }
    }

    protected abstract Classifier trainClassifier(List<float[]> caseData, List<float[]> controlData);

    @Publish public SubpanelChangedEvent publishSubpanelChangedEvent(SubpanelChangedEvent event) {
        return event;
    }

    @Publish public ProjectNodeAddedEvent publishProjectNodeAddedEvent(ProjectNodeAddedEvent event) {
        return event;
    }

    @Subscribe public void receive(ProjectEvent event, Object source) {
        DSDataSet dataSet = event.getDataSet();
        if ((dataSet != null) && (dataSet instanceof DSMicroarraySet)) {
            panel.setMaSet((DSMicroarraySet) dataSet);
            panel.rebuildForm();
        }
    }

    @Subscribe public void receive(PhenotypeSelectorEvent event, Object source) {
        panel.rebuildForm();
    }
}
