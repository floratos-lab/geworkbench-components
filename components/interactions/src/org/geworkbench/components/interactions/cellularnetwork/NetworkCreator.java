package org.geworkbench.components.interactions.cellularnetwork;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.events.AdjacencyMatrixCancelEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.AnnotationLookupHelper;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.InteractionDetail;

// the only reason to use raw Thread+Observer instead of, say, SwingWorker,
// is to use the existing progress bar code, which is shabby in the first
// place
class NetworkCreator extends Thread implements Observer {
	static private Log log = LogFactory.getLog(NetworkCreator.class);

	final private List<CellularNetWorkElementInformation> list;
	final private List<String> selectedTypes;
	final private boolean isRestrictToGenesPresentInMicroarray;
	final private DSMicroarraySet dataset;
	final double threshold;
	final JButton createNetWorkButton;
	final ProgressBar pb;

	// only used for history info
	final private String selectedContext;
	final private String selectedVersion;

	// needed for (1) publishing event and (2) get hit list
	final private CellularNetworkKnowledgeWidget widget;

	private AdjacencyMatrix matrix; // this is useful only for cancellation
	private boolean cancel = false;

	public NetworkCreator(ProgressBar createNetworkPb,
			final JButton createNetWorkButton, double threshold,
			final DSMicroarraySet dataset,
			final boolean isRestrictToGenesPresentInMicroarray,
			final List<String> selectedTypes, final String selectedContext,
			final String selectedVersion, CellularNetworkKnowledgeWidget widget) {
		this.dataset = dataset;
		this.pb = createNetworkPb;
		this.createNetWorkButton = createNetWorkButton;
		this.threshold = threshold;
		this.isRestrictToGenesPresentInMicroarray = isRestrictToGenesPresentInMicroarray;
		this.selectedTypes = selectedTypes;

		this.selectedContext = selectedContext;
		this.selectedVersion = selectedVersion;

		this.widget = widget;
		list = widget.getHits();

		pb.addObserver(this);
	}

	@Override
	public void run() {

		DSItemList<DSGeneMarker> markers = dataset.getMarkers();
		DSItemList<DSGeneMarker> copy = new CSItemList<DSGeneMarker>();
		copy.addAll(markers);
		EntrezIdComparator eidc = new EntrezIdComparator();
		Collections.sort(copy, eidc);

		Map<String, List<DSGeneMarker>> geneNameToMarkerMap = AnnotationLookupHelper
				.getGeneNameToMarkerMapping(dataset);

		AdjacencyMatrix matrix = new AdjacencyMatrix(null,
				CellularNetworkPreferencePanel.interactionTypeSifMap,
				CellularNetworkPreferencePanel.interactionEvidenceMap);
		AdjacencyMatrixDataSet adjacencyMatrixdataSet = null;

		int interactionNum = 0;
		boolean createNetwork = false;

		boolean isGene2InMicroarray = true;
		StringBuffer historyStr = new StringBuffer();

		short usedConfidenceType = CellularNetWorkElementInformation
				.getUsedConfidenceType();

		for (CellularNetWorkElementInformation cellularNetWorkElementInformation : list) {
			if (cellularNetWorkElementInformation.isDirty())
				continue;

			ArrayList<InteractionDetail> arrayList = cellularNetWorkElementInformation
					.getSelectedInteractions(selectedTypes);

			List<String> networkSelectedInteractionTypes = selectedTypes;
			if (networkSelectedInteractionTypes.size() > 0)
				historyStr
						.append("           ")
						.append(cellularNetWorkElementInformation
								.getdSGeneMarker().getLabel()).append(": \n");
			for (String interactionType : networkSelectedInteractionTypes)
				historyStr
						.append("\t Include ")
						.append(interactionType)
						.append(": ")
						.append(cellularNetWorkElementInformation
								.getInteractionNum(interactionType))
						.append("\n");

			if (arrayList == null || arrayList.size() <= 0) {
				continue;
			}

			DSGeneMarker marker1 = cellularNetWorkElementInformation
					.getdSGeneMarker();

			for (InteractionDetail interactionDetail : arrayList) {
				if (isCancelled() == true)
					return;
				isGene2InMicroarray = true;
				DSGeneMarker marker = new CSGeneMarker();
				String mid2 = interactionDetail.getdSGeneId();
				AdjacencyMatrix.Node node2 = null;

				if (interactionDetail.getDbSource().equalsIgnoreCase(
						Constants.ENTREZ_GENE)) {
					try {
						marker.setGeneId(new Integer(mid2));
					} catch (NumberFormatException ne) {
						log.error("ms_id2 is expect to be an integer: " + mid2
								+ "This interaction is going to be dropped");
						continue;
					}
					int index = Collections.binarySearch(copy, marker, eidc);
					if (index >= 0) {
						node2 = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL,
								copy.get(index).getGeneName());
					} else {
						isGene2InMicroarray = false;
					}

				} else {
					Collection<DSGeneMarker> dSGeneMarkerList = geneNameToMarkerMap
							.get(interactionDetail.getdSGeneName());
					if (dSGeneMarkerList != null && !dSGeneMarkerList.isEmpty()) {
						for (DSGeneMarker dSGeneMarker : dSGeneMarkerList) {
							node2 = new AdjacencyMatrix.Node(
									NodeType.GENE_SYMBOL,
									dSGeneMarker.getGeneName());
							if (interactionDetail.getDbSource()
									.equalsIgnoreCase(Constants.UNIPORT)) {
								Set<String> SwissProtIds = new HashSet<String>();
								String[] ids = AnnotationParser.getInfo(
										dSGeneMarker.getLabel(),
										AnnotationParser.SWISSPROT);
								for (String s : ids) {
									SwissProtIds.add(s.trim());
								}
								if (SwissProtIds.contains(interactionDetail
										.getdSGeneId())) {
									break;
								}
							} else {

								break;
							}

						}
					} else {
						isGene2InMicroarray = false;
					}
				}

				if (isGene2InMicroarray == false) {
					log.info("Marker " + interactionDetail.getdSGeneId()
							+ " does not exist at the dataset. ");
					if (isRestrictToGenesPresentInMicroarray)
						continue;

					if (interactionDetail.getdSGeneName() != null
							&& !interactionDetail.getdSGeneName().trim()
									.equals("")
							&& !interactionDetail.getdSGeneName().trim()
									.equals("null")) {
						node2 = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL,
								interactionDetail.getdSGeneName(), 0);
					} else {
						node2 = new AdjacencyMatrix.Node(NodeType.STRING, mid2);
					}
				}
				AdjacencyMatrix.Node node1 = new AdjacencyMatrix.Node(
						NodeType.GENE_SYMBOL, marker1.getGeneName());

				String shortNameType = CellularNetworkPreferencePanel.interactionTypeSifMap
						.get(interactionDetail.getInteractionType());

				matrix.add(
						node1,
						node2,
						new Float(interactionDetail
								.getConfidenceValue(usedConfidenceType)),
						shortNameType, interactionDetail.getEvidenceId());

				interactionNum++;
			}
		} // end for loop

		if (interactionNum > 0) {
			createNetwork = true;
		} else if (interactionNum == 0) {
			JOptionPane.showMessageDialog(null,
					"No interactions exist in the current database.",
					"Empty Set", JOptionPane.ERROR_MESSAGE);
			createNetwork = false;

		}
		if (createNetwork == true) {

			adjacencyMatrixdataSet = new AdjacencyMatrixDataSet(matrix,
					threshold, "Adjacency Matrix", dataset.getLabel(), dataset);

			String history = "Cellular Network Parameters: \n"
					+ "      URL Used:     " + ResultSetlUtil.getUrl() + "\n"
					+ "      Selected Interactome:     " + selectedContext
					+ "\n" + "      Selected Version:     " + selectedVersion
					+ "\n" + "      Threshold:     " + threshold + "\n"
					+ "      Selected Marker List: \n" + historyStr + "\n";
			HistoryPanel.addToHistory(adjacencyMatrixdataSet, history);

			if (isCancelled())
				return;
			else {
				pb.setTitle("Draw cytoscape graph");
				pb.setMessage("Draw cytoscape graph ...");

			}
			widget.publishProjectNodeAddedEvent(new ProjectNodeAddedEvent(
					"Adjacency Matrix Added", null, adjacencyMatrixdataSet));

		}

		this.matrix = matrix;

		if (!isCancelled()) {
			log.info("task is completed");
			createNetWorkButton.setEnabled(true);
			pb.dispose();
		} else {
			log.info("task is canceled");

		}

	}

	private boolean isCancelled() {
		return this.cancel;
	}

	public void update(Observable o, Object arg) {
		cancel = true;
		if (pb.getTitle().equals("Draw cytoscape graph")) {
			pb.dispose();
			widget.publishAdjacencyMatrixCancelEvent(new AdjacencyMatrixCancelEvent(
					matrix));

		} else {
			pb.dispose();

		}
		createNetWorkButton.setEnabled(true);
		log.info("Create network canceled.");
	}

	static private class EntrezIdComparator implements Comparator<DSGeneMarker> {
		@Override
		public int compare(DSGeneMarker m1, DSGeneMarker m2) {
			return m1.getGeneId() - m2.getGeneId();
		}
	}

}