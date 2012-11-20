package org.geworkbench.components.ei;

import java.util.Enumeration;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.builtin.projects.DataSetNode;
import org.geworkbench.builtin.projects.DataSetSubNode;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectNodeAddedEvent;

import edu.columbia.c2b2.evidenceinegration.Evidence;
import edu.columbia.c2b2.evidenceinegration.EvidenceIntegration;

/**
 * @author mhall
 * @version $Id$
 */
public class EvidenceIntegrationAnalysis extends AbstractAnalysis implements ClusteringAnalysis {

	private static final long serialVersionUID = 7288801656726834224L;
	
	public static final String DB_URL = "jdbc:mysql://afdev.c2b2.columbia.edu:3306/evidence_integration";
    public static final String DB_USERNAME = "evidence_integ";
    public static final String DB_PASSWORD = "S@cUrE_aR@a56";

    EvidenceIntegration eiEngine = new EvidenceIntegration(DB_URL, DB_USERNAME, DB_PASSWORD);

    DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView;

    static Log log = LogFactory.getLog(EvidenceIntegrationAnalysis.class);
    private EvidenceIntegrationParamPanel eiParamPanel;

    public EvidenceIntegrationAnalysis() {
        eiParamPanel = new EvidenceIntegrationParamPanel(eiEngine.getGoldStandardSources());
        setDefaultPanel(eiParamPanel);
    }

    public int getAnalysisType() {
        return AbstractAnalysis.ZERO_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
//        if (input instanceof DSMicroarraySetView) {
//            log.debug("Input dataset is microarray type.");
//            mSetView = (DSMicroarraySetView) input;
//        }
        List<Evidence> selectedEvidence = eiParamPanel.getSelectedEvidence();
        List<Integer> selectedGoldStandards = eiParamPanel.getSelectedGoldStandards();
        List<Evidence> selectedUserDefinedGoldStandards = eiParamPanel.getSelectedUserDefinedGoldStandards();
        log.debug("Evidence selected: ");
        for (Evidence evidence : selectedEvidence) {
            log.debug("\t" + evidence.getName());
        }

        EIThread eiThread = null;
        if (selectedUserDefinedGoldStandards == null || selectedUserDefinedGoldStandards.size() < 1) {
            eiThread = new EIThread(mSetView, selectedEvidence, selectedGoldStandards);
        } else {
            eiThread = new EIThread(mSetView, selectedEvidence, selectedGoldStandards, selectedUserDefinedGoldStandards);
        }

        EIProgress progress = new EIProgress(eiThread);
        eiThread.setProgressWindow(progress);
        progress.setVisible(true);

        return new AlgorithmExecutionResults(true, "Evidence Integration In Progress", null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Subscribe
    public void receive(org.geworkbench.events.ProjectEvent projectEvent, Object source) {

        ProjectSelection selection = ((ProjectPanel) source).getSelection();
        DataSetNode dNode = selection.getSelectedDataSetNode();
        if(dNode == null){
        	log.warn("No data node selected.");
        	return;
        }
        if (dNode.getDataset() instanceof CSMicroarraySet) {
            log.debug("Selected dataset is microarray type.");
            CSMicroarraySet dataFile = (CSMicroarraySet) dNode.getDataset();
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

		for (AdjacencyMatrix.Edge edge : matrix.getEdges()) {
			DSGeneMarker gene1 = edge.node1.marker;
			if (gene1 != null) {
				DSGeneMarker destGene = edge.node2.marker;
				if (destGene != null) {
					log.debug("Adding evidence: " + gene1.getGeneId() + ", "
							+ destGene.getGeneId() + ", " + edge.info.value);
					evidence.addEdge(gene1.getGeneId(), destGene.getGeneId(),
							edge.info.value);
				} else {
					log.debug("Gene with index " + edge.node2
							+ " not found in selected genes, skipping.");
				}
			} else {
				log.debug("Gene with index " + edge.node1
						+ " not found in selected genes, skipping.");
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
        List<Evidence> selectedUserDefinedGoldStandards;
        private EIProgress progressWindow;
        private DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView;

        public EIThread(DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView, List<Evidence> selectedEvidence, List<Integer> selectedGoldStandards,  List<Evidence> selectedUserDefinedGoldStandards) {
            this.mSetView = mSetView;
            this.selectedEvidence = selectedEvidence;
            this.selectedGoldStandards = selectedGoldStandards;
            this.selectedUserDefinedGoldStandards = selectedUserDefinedGoldStandards;
        }

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

            publishProjectNodeAddedEvent(new ProjectNodeAddedEvent("Evidence Integration Results", null, dataset));

        }

        public EIProgress getProgressWindow() {
            return progressWindow;
        }

        public void setProgressWindow(EIProgress progressWindow) {
            this.progressWindow = progressWindow;
        }

    }

}
