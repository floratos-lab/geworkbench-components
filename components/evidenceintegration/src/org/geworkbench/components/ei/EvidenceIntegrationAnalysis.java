package org.geworkbench.components.ei;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.builtin.projects.*;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix;
import org.geworkbench.events.AdjacencyMatrixEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import edu.columbia.c2b2.evidenceinegration.Evidence;
import edu.columbia.c2b2.evidenceinegration.EvidenceIntegration;

import javax.swing.*;

/**
 * @author mhall
 */
public class EvidenceIntegrationAnalysis extends AbstractGridAnalysis implements ClusteringAnalysis {

    public static final String DB_URL = "jdbc:mysql://afdev:3306/evidence_integration";
    public static final String DB_USERNAME = "matt";
    public static final String DB_PASSWORD = "matthall";
    private final String analysisName = "ei";

    EvidenceIntegration eiEngine = new EvidenceIntegration(DB_URL, DB_USERNAME, DB_PASSWORD);

    DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView;

    static Log log = LogFactory.getLog(EvidenceIntegrationAnalysis.class);
    private EvidenceIntegrationParamPanel eiParamPanel;

    public EvidenceIntegrationAnalysis() {
        setLabel("Evidence Integration");
        eiParamPanel = new EvidenceIntegrationParamPanel(eiEngine.getGoldStandardSources());
        setDefaultPanel(eiParamPanel);
    }

    public int getAnalysisType() {
        return AbstractAnalysis.ZERO_TYPE;
    }

    public  Map<String, Object> getBisonParameters(){
        return null;
    };

    	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getAnalysisName()
	 */
	@Override
	public String getAnalysisName() {
		return analysisName;
	}
    public AlgorithmExecutionResults execute(Object input) {
//        if (input instanceof DSMicroarraySetView) {
//            log.debug("Input dataset is microarray type.");
//            mSetView = (DSMicroarraySetView) input;
//        }
        List<Evidence> selectedEvidence = eiParamPanel.getSelectedEvidence();
        List<Integer> selectedGoldStandards = eiParamPanel.getSelectedGoldStandards();
        log.debug("Evidence selected: ");
        for (Evidence evidence : selectedEvidence) {
            log.debug("\t" + evidence.getName());
        }

        EIThread eiThread = new EIThread(mSetView, selectedEvidence, selectedGoldStandards);

        EIProgress progress = new EIProgress(eiThread);
        eiThread.setProgressWindow(progress);
        progress.setVisible(true);

//        eiEngine.doIntegration(selectedEvidence, selectedGoldStandards);
//        DSMicroarraySet<DSMicroarray> mSet = ((DSMicroarraySetView) input).getMicroarraySet();
//        EvidenceIntegrationDataSet dataset = new EvidenceIntegrationDataSet(mSetView.getMicroarraySet(), "Evidence Integration Results", selectedEvidence, "Unknown");

        return new AlgorithmExecutionResults(true, "Evidence Integration In Progress", null);
    }

    @Subscribe
    public void receive(org.geworkbench.events.ProjectEvent projectEvent, Object source) {
        log.error("Got project event.");
        if (projectEvent.getMessage().equals(org.geworkbench.events.ProjectEvent.CLEARED)) {
//            setMArraySet(null);
        }

        ProjectSelection selection = ((ProjectPanel) source).getSelection();
        DataSetNode dNode = selection.getSelectedDataSetNode();
        if (dNode.dataFile instanceof CSExprMicroarraySet) {
            log.debug("Selected dataset is microarray type.");
            CSExprMicroarraySet dataFile = (CSExprMicroarraySet) dNode.dataFile;
            mSetView = new CSMicroarraySetView(dataFile);
        }
        eiParamPanel.clearEvidence();
        Enumeration children = dNode.children();
        while (children.hasMoreElements()) {
            Object obj = children.nextElement();
            log.debug("Child: " + obj);
            if (obj instanceof DataSetSubNode) {
                DSAncillaryDataSet ads = ((DataSetSubNode) obj)._aDataSet;
                if (ads instanceof AdjacencyMatrixDataSet) {
                    AdjacencyMatrixDataSet adjData = (AdjacencyMatrixDataSet) ads;
                    log.debug("\tChild is subnode: " + ads);
                    eiParamPanel.addEvidence(convert(adjData, mSetView));
                }
            }
        }

    }

    public Evidence convert(AdjacencyMatrixDataSet adjMatrix, DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSet) {
        Evidence evidence = new Evidence(adjMatrix.getLabel());
        AdjacencyMatrix matrix = adjMatrix.getMatrix();
        HashMap<Integer, HashMap<Integer, Float>> geneRows = matrix.getGeneRows();
        DSItemList<DSGeneMarker> markers = mSet.markers();
        for (Map.Entry<Integer, HashMap<Integer, Float>> entry : geneRows.entrySet()) {
            DSGeneMarker gene1 = markers.get(entry.getKey());
            if (gene1 != null) {
                HashMap<Integer, Float> destGenes = entry.getValue();
                for (Map.Entry<Integer, Float> destEntry : destGenes.entrySet()) {
                    DSGeneMarker destGene = markers.get(destEntry.getKey());
                    if (destGene != null) {
                        log.debug("Adding evidence: " + gene1.getGeneId() + ", " + destGene.getGeneId() + ", " + destEntry.getValue());
                        evidence.addEdge(gene1.getGeneId(), destGene.getGeneId(), destEntry.getValue());
//                        graph.addEdge(gene1.getShortName(), destGene.getShortName(), destEntry.getValue());
                    } else {
                        log.debug("Gene with index " + destEntry.getKey() + " not found in selected genes, skipping.");
                    }
                }
            } else {
                log.debug("Gene with index " + entry.getKey() + " not found in selected genes, skipping.");
            }
        }
        return evidence;
    }

    @Publish
    public ProjectNodeAddedEvent publishProjectNodeAddedEvent(ProjectNodeAddedEvent event) {
        return event;
    }

    class EIThread extends Thread {
        List<Evidence> selectedEvidence;
        List<Integer> selectedGoldStandards;
        private EIProgress progressWindow;
        private DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView;

        public EIThread(DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView, List<Evidence> selectedEvidence, List<Integer> selectedGoldStandards) {
            this.mSetView = mSetView;
            this.selectedEvidence = selectedEvidence;
            this.selectedGoldStandards = selectedGoldStandards;
        }

        public void run() {
            log.debug("Running Evidence Integration in worker thread.");
            eiEngine.doIntegration(selectedEvidence, selectedGoldStandards);
            log.debug("Done running Evidence Integration in worker thread.");
            progressWindow.setVisible(false);

            EvidenceIntegrationDataSet dataset = new EvidenceIntegrationDataSet(mSetView.getMicroarraySet(), "Evidence Integration Results", selectedEvidence, "Unknown", eiEngine.getGoldStandardSources());

//            ProjectPanel.addToHistory(dataSet, "Generated with ARACNE run with paramters: " + p.getParamterDescription());

            publishProjectNodeAddedEvent(new ProjectNodeAddedEvent("Evidence Integration Results", null, dataset));

//        publishAdjacencyMatrixEvent(new AdjacencyMatrixEvent(convert(weightedGraph, mSetView), "ARACNE Set",
//                -1, 2, 0.5f, AdjacencyMatrixEvent.Action.RECEIVE));
        }

        public EIProgress getProgressWindow() {
            return progressWindow;
        }

        public void setProgressWindow(EIProgress progressWindow) {
            this.progressWindow = progressWindow;
        }

    }

}
