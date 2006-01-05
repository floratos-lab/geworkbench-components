package org.geworkbench.components.analysis.classification.svm;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.svm.SupportVectorMachine;
import org.apache.commons.math.stat.inference.TTest;
import org.apache.commons.math.stat.inference.TTestImpl;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.*;

public class SVMAnalysis extends AbstractAnalysis implements ClusteringAnalysis {

    static Log log = LogFactory.getLog(SVMAnalysis.class);

    Random rand = new Random();

    private static class Indexable implements Comparable {

        private double[] data;
        private int index;

        public Indexable(double[] data, int index) {
            this.data = data;
            this.index = index;
        }

        public int compareTo(Object o) {
            // Assumes that the other object is an indexable referencing the same data
            Indexable other = (Indexable) o;
            if (data[index] > data[other.index]) {
                return 1;
            } else if (data[index] < data[other.index]) {
                return -1;
            } else {
                return 0;
            }
        }

    }

    private SVMOptimizationPanel panel;

    public SVMAnalysis() {
        setLabel("SVM Analysis");
        panel = new SVMOptimizationPanel();
        setDefaultPanel(panel);
    }

    public int getAnalysisType() {
        return AbstractAnalysis.TTEST_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        assert (input instanceof DSMicroarraySetView);
        DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
        DSMicroarraySet maSet = view.getMicroarraySet();
        TTest tTest = new TTestImpl();
        // Get params
        Set<String> labelSet = panel.getTrainingtLabels();
        Set<String> classifyLabelSet = panel.getClassifyLabels();
        double alpha = panel.getPValue();
        int m = labelSet.size();
        if (m < 2) {
            return new AlgorithmExecutionResults(false, "At least two panels must be selected for classification.", null);
        }
        int numTests = m * (m - 1) / 2;
        DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(maSet);
        String[] labels = labelSet.toArray(new String[m]);
        int n = view.markers().size();
        double[][] pValues = new double[n][numTests];
        int testIndex = 0;
        // Create panels and significant result sets to store results
        DSPanel<DSGeneMarker>[] panels = new DSPanel[numTests];
        DSSignificanceResultSet<DSGeneMarker>[] sigSets = new DSSignificanceResultSet[numTests];

        // SVM objects
        log.debug("Target is " + labels[0] + "(1) ");
        DSPanel<DSMicroarray> temppanel = context.getItemsWithLabel(labels[0]);

        List<float[]> caseData = new ArrayList<float[]>();
        List<float[]> controlData = new ArrayList<float[]>();

        String attribLabel = labels[0];

        {
            DSPanel<DSMicroarray> panel = new CSPanel<DSMicroarray>(context.getItemsWithLabel(attribLabel));
            addMicroarrayData(panel, caseData);
        }

        {
            DSPanel<DSMicroarray> panel = new CSPanel<DSMicroarray>(context.getItemsWithoutLabel(attribLabel));
            addMicroarrayData(panel, controlData);
        }

        warnOnInvalidData(caseData);
        warnOnInvalidData(controlData);

        KFoldCrossValidation cross = new KFoldCrossValidation(3, caseData, controlData);

        for (int i = 0; i < cross.getNumFolds(); i++) {
            KFoldCrossValidation.CrossValidationData crossData = cross.getData(i);
            log.debug("Training classifier data set #" + i);
            SupportVectorMachine svm = new SupportVectorMachine(crossData.getTrainingCaseData(), crossData.getTrainingControlData(),
                    SupportVectorMachine.LINEAR_KERNAL_FUNCTION, 0.1f);
            // Non-SMO
            // svm.buildSupportVectors(1000, 1e-6);
            // SMO
            svm.buildSupportVectorsSMO(1);
            log.debug("Classifier training complete.");

            log.debug("Classifying test case data for set #" + i);
            int correct = 0;
            for (float[] values : crossData.getTestCaseData()) {
                if (svm.evaluate(values)) {
                    correct++;
                }
            }
            log.debug("True positives: "+correct+",  false negatives: "+(crossData.getTestCaseData().size()-correct));

            log.debug("Classifying test control data for set #" + i);
            correct = 0;
            for (float[] values : crossData.getTestControlData()) {
                if (svm.evaluate(values)) {
                    correct++;
                }
            }
            log.debug("False positives: "+correct+", true negatives:  "+(crossData.getTrainingControlData().size()-correct));
        }

        return null;
    }

    private void warnOnInvalidData(List<float[]> data) {
        for (float[] floats : data) {
            for (float v : floats) {
                if (Float.isNaN(v)) {
                    log.warn("NaN at location ");
                } else if (Float.isInfinite(v)) {
                    log.warn("Infinite.");
                }
            }
        }
    }

    private void addMicroarrayData(DSPanel<DSMicroarray> panel, List<float[]> dataToAddTo) {
        for (DSMicroarray microarray : panel) {
            DSMutableMarkerValue[] values = microarray.getMarkerValues();
            float[] data = new float[values.length];
            for (int j = 0; j < values.length; j++) {
                data[j] = (float) values[j].getValue();
            }
            dataToAddTo.add(data);
        }
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

    @Publish public SubpanelChangedEvent publishSubpanelChangedEvent(SubpanelChangedEvent event) {
        return event;
    }

    @Publish public ProjectNodeAddedEvent publishProjectNodeAddedEvent(ProjectNodeAddedEvent event) {
        return event;
    }

}
