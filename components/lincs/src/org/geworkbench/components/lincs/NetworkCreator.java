package org.geworkbench.components.lincs;

 
import java.util.List; 
import java.util.Observable;
import java.util.Observer; 

import javax.swing.JButton;
import javax.swing.JOptionPane;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.builtin.projects.ProjectPanel;
 
import org.geworkbench.service.lincs.data.xsd.CnkbInteractionData;
import org.geworkbench.service.lincs.data.xsd.InteractionDetail;
import org.geworkbench.util.ProgressBar;

// the only reason to use raw Thread+Observer instead of, say, SwingWorker,
// is to use the existing progress bar code, which is shabby in the first
// place
class NetworkCreator extends Thread implements Observer {
	static private Log log = LogFactory.getLog(NetworkCreator.class);

	private List<ValueObject> geneList;
	private long interactomeVersionId;
	private boolean isShowDiffExpr;
	private JButton createNetWorkButton;
	private ProgressBar pb;
 
	private boolean cancel = false;

	public NetworkCreator(List<ValueObject> geneList,
			long interactomeVersionId, JButton createNetWorkButton,
			boolean isShowDiffExpr, ProgressBar createNetworkPb) {

		this.geneList = geneList;
		this.interactomeVersionId = interactomeVersionId;
		this.createNetWorkButton = createNetWorkButton;
		this.isShowDiffExpr = isShowDiffExpr;
		this.pb = createNetworkPb;

		pb.addObserver(this);
	}

	@Override
	public void run() {

		AdjacencyMatrix matrix = new AdjacencyMatrix("Lincs Fmoa Data");
		AdjacencyMatrixDataSet adjacencyMatrixdataSet = null;

		Lincs lincsService = LincsInterface.getLincsService();
		int interactionNum = 0;
		boolean createNetwork = false;

		try {
			for (ValueObject v : geneList) {
				if (isCancelled())
					return;
				CnkbInteractionData interactionData = lincsService
						.getInteractionData(v.getReferenceId(), v.getValue()
								.toString(), interactomeVersionId);

				List<InteractionDetail> detailList = interactionData
						.getInteractionDetails();

				if (detailList == null || detailList.size() <= 0) {
					continue;
				}

				String geneSymbol1 = interactionData.getGeneSymbol();

				for (InteractionDetail interactionDetail : detailList) {
					if (isCancelled() == true)
						return;

					String geneSymbol2 = interactionDetail.getGeneSymbol();
					AdjacencyMatrix.Node node2 = new AdjacencyMatrix.Node(
							NodeType.STRING, geneSymbol2);

					AdjacencyMatrix.Node node1 = new AdjacencyMatrix.Node(
							NodeType.STRING, geneSymbol1);

					// String shortNameType =
					// CellularNetworkPreferencePanel.interactionTypeSifMap
					// .get(interactionDetail.getInteractionType());

					matrix.add(node1, node2,
							new Float(interactionDetail.getConfidenceValue()),
							interactionDetail.getInteractionType());

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
				return;

			}
			if (createNetwork == true) {

				adjacencyMatrixdataSet = new AdjacencyMatrixDataSet(matrix, 0,
						"Adjacency Matrix", "Lincs Fmoa Data", null);

				if (isCancelled())
					return;
				else {
					pb.setTitle("Draw cytoscape graph");
					pb.setMessage("Draw cytoscape graph ...");

				}

				ProjectPanel.getInstance().addDataSetNode(
						adjacencyMatrixdataSet);

			}
 
			if (!isCancelled()) {
				log.info("task is completed");
				createNetWorkButton.setEnabled(true);
				pb.dispose();
			} else {
				log.info("task is canceled");

			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	private boolean isCancelled() {
		return this.cancel;
	}

	public void update(Observable o, Object arg) {
		cancel = true;
		if (pb.getTitle().equals("Draw cytoscape graph")) {
			pb.dispose();

		} else {
			pb.dispose();

		}
		createNetWorkButton.setEnabled(true);
		log.info("Create network canceled.");
	}

}