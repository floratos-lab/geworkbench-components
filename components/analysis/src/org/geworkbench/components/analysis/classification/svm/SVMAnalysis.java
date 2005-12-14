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

        List<DSMicroarray> validateDataCase = new ArrayList<DSMicroarray>();
        List<DSMicroarray> validateDataControl = new ArrayList<DSMicroarray>();

        try {
            String attribLabel = labels[0];

            {
                DSPanel<DSMicroarray> panel = new CSPanel<DSMicroarray>(context.getItemsWithLabel(attribLabel));
                int aThird = panel.size() / 3;
                int twoThirds = panel.size() - aThird;

                for (int i = 0; i < twoThirds; i++) {
                    DSMicroarray array = panel.remove(rand.nextInt(panel.size()));
                    DSMutableMarkerValue[] values = array.getMarkerValues();
                    float[] data = new float[values.length];
                    for (int j = 0; j < values.length; j++) {
                        data[j] = (float) values[j].getValue();
                    }
                    caseData.add(data);
                }

                for (DSMicroarray microarray : panel) {
                    validateDataCase.add(microarray);
                }
            }

            {
                DSPanel<DSMicroarray> panel = new CSPanel<DSMicroarray>(context.getItemsWithoutLabel(attribLabel));
                int aThird = panel.size() / 3;
//                int twoThirds = panel.size() - aThird;
                int twoThirds = validateDataCase.size() * 2;
                log.debug("Two thirds " + twoThirds);

                for (int i = 0; i < twoThirds; i++) {
                    DSMicroarray array = panel.remove(rand.nextInt(panel.size()));
                    DSMutableMarkerValue[] values = array.getMarkerValues();
                    float[] data = new float[values.length];
                    for (int j = 0; j < values.length; j++) {
                        data[j] = (float) values[j].getValue();
                    }
                    controlData.add(data);
                }

                for (DSMicroarray microarray : panel) {
                    validateDataControl.add(microarray);
                }
            }

        } catch (Exception e) {
            log.error(e);
        }

        for (float[] floats : caseData) {
            for (float v : floats) {
                if (Float.isNaN(v)) {
                    log.warn("NaN at location ");
                } else if (Float.isInfinite(v)) {
                    log.warn("Infinite.");
                }
            }
        }

        for (float[] floats : controlData) {
            for (float v : floats) {
                if (Float.isNaN(v)) {
                    log.warn("NaN at location ");
                } else if (Float.isInfinite(v)) {
                    log.warn("Infinite.");
                }
            }
        }

        log.debug("Training classifier.");
        SupportVectorMachine svm = new SupportVectorMachine(caseData, controlData, SupportVectorMachine.LINEAR_KERNAL_FUNCTION, 1000, 1e-6);
        log.debug("Classifier training complete.");

        log.debug("Testing classification of arrays in case test group");
        int correct = 0;
        for (DSMicroarray microarray : validateDataCase) {
            DSMutableMarkerValue[] values = microarray.getMarkerValues();
            float[] nodes = new float[values.length];
            for (int j = 0; j < values.length; j++) {
                nodes[j] = (float) values[j].getValue();
            }
            if (svm.evaluate(nodes)) {
                correct++;
            }

        }
        log.debug("True positives: "+correct+", false negatives: "+(validateDataCase.size()-correct));

        log.debug("Testing classification of arrays in control test group");
        correct = 0;
        for (DSMicroarray microarray : validateDataControl) {
            DSMutableMarkerValue[] values = microarray.getMarkerValues();
            float[] nodes = new float[values.length];
            for (int j = 0; j < values.length; j++) {
                nodes[j] = (float) values[j].getValue();
            }
            if (!svm.evaluate(nodes)) {
                correct++;
            }

        }
        log.debug("True negatives: "+correct+", false positives: "+(validateDataControl.size()-correct));

        return null;
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
