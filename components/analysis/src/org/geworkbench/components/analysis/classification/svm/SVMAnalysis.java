package org.geworkbench.components.analysis.classification.svm;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.svm.SupportVectorMachine;
import org.geworkbench.util.svm.ClassifierException;
import org.apache.commons.math.stat.inference.TTest;
import org.apache.commons.math.stat.inference.TTestImpl;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.swing.*;
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
        float epsilon = panel.getEpsilon();
        float C = panel.getC();

        DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(maSet);

        List<float[]> caseData = new ArrayList<float[]>();
        addMicroarrayData(context.getItemsForClass(CSAnnotationContext.CLASS_CASE), caseData);
        List<float[]> controlData = new ArrayList<float[]>();
        addMicroarrayData(context.getItemsForClass(CSAnnotationContext.CLASS_CONTROL), controlData);
        List<float[]> testData = new ArrayList<float[]>();
        addMicroarrayData(context.getItemsForClass(CSAnnotationContext.CLASS_TEST), testData);

        log.debug("Training classifier.");
        SupportVectorMachine svm = null;
        try {
            svm = new SupportVectorMachine(caseData, controlData,
                    panel.getSelectedKernel(), 0.1f);
            // Non-SMO
            // svm.buildSupportVectors(1000, 1e-6);
            // SMO
            svm.buildSupportVectorsSMO(C, epsilon);
            log.debug("Classifier training complete.");

            DSPanel<DSMicroarray> newPanel = new CSPanel<DSMicroarray>("SVM Results");
            log.debug("Classifying test data.");
            classifyData(context.getItemsForClass(CSAnnotationContext.CLASS_TEST), svm, newPanel);

            publishSubpanelChangedEvent(new SubpanelChangedEvent<DSMicroarray>(DSMicroarray.class, newPanel, SubpanelChangedEvent.NEW));
        } catch (ClassifierException e) {
            JOptionPane.showMessageDialog(panel, e.getMessage());
            log.warn(e);
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

    public static void addMicroarrayData(DSPanel<DSMicroarray> panel, List<float[]> dataToAddTo) {
        for (DSMicroarray microarray : panel) {
            DSMutableMarkerValue[] values = microarray.getMarkerValues();
            float[] data = new float[values.length];
            for (int j = 0; j < values.length; j++) {
                data[j] = (float) values[j].getValue();
            }
            dataToAddTo.add(data);
        }
    }

    public static void classifyData(DSPanel<DSMicroarray> panel, SupportVectorMachine svm, DSPanel<DSMicroarray> newGroupPanel) {
        for (DSMicroarray microarray : panel) {
            DSMutableMarkerValue[] values = microarray.getMarkerValues();
            float[] data = new float[values.length];
            for (int j = 0; j < values.length; j++) {
                data[j] = (float) values[j].getValue();
            }
            if (svm.evaluate(data)) {
                newGroupPanel.add(microarray);
            }
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
