package org.geworkbench.components.lincs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.geworkbench.service.lincs.data.xsd.GeneRank;
import org.geworkbench.util.ProgressBar;

// the only reason to use raw Thread+Observer instead of, say, SwingWorker,
// is to use the existing progress bar code, which is shabby in the first
// place
class NetworkCreator extends Thread implements Observer {
	static private Log log = LogFactory.getLog(NetworkCreator.class);

	private List<ValueObject> geneList;
	private long interactomeVersionId;
	private long compoundId;
	private long differentialExpressionRunId;
	private boolean isShowDiffExpr;
	private JButton createNetWorkButton;
	private ProgressBar pb;

	private static Map<String, String> interactionTypeSifMap = null;
	private boolean cancel = false;

	public NetworkCreator(List<ValueObject> geneList, long compoundId, long differentialExpressionRunId,
			long interactomeVersionId, JButton createNetWorkButton,
			boolean isShowDiffExpr, ProgressBar createNetworkPb) {

		this.geneList = geneList;
		this.interactomeVersionId = interactomeVersionId;
		this.compoundId = compoundId;
		this.differentialExpressionRunId = differentialExpressionRunId;
		this.createNetWorkButton = createNetWorkButton;
		this.isShowDiffExpr = isShowDiffExpr;
		this.pb = createNetworkPb;
		
		pb.addObserver(this);
		loadInteractionTypeMap();
	}

	@Override
	public void run() {
		createNetWorkButton.setEnabled(false);
		AdjacencyMatrix matrix = new AdjacencyMatrix(null, interactionTypeSifMap);
		AdjacencyMatrixDataSet adjacencyMatrixdataSet = null;

		Lincs lincsService = LincsInterface.getLincsService();
		int interactionNum = 0;		 
		Map<String, String> geneIdToSymbol = new HashMap<String, String>();
		boolean createNetwork = false;
       
		try {
			for (ValueObject v : geneList) {
				if (isCancelled())
				{ 
					return;
				}
				CnkbInteractionData interactionData = lincsService
						.getInteractionData(v.getReferenceId(), v.getValue()
								.toString(), interactomeVersionId);

				List<InteractionDetail> detailList = interactionData
						.getInteractionDetails();

				if (detailList == null || detailList.size() <= 0) {
					continue;
				}

				String geneSymbol1 = interactionData.getGeneSymbol();			 
				geneIdToSymbol.put(interactionData.getGeneId().toString(), geneSymbol1);
	          
                for (InteractionDetail interactionDetail : detailList) {
					if (isCancelled() == true)
					{					 
						return;
					}

					String geneSymbol2 = interactionDetail.getGeneSymbol();
					geneIdToSymbol.put(interactionDetail.getGeneId().toString(),geneSymbol2);
					AdjacencyMatrix.Node node2 = new AdjacencyMatrix.Node(
							NodeType.STRING, geneSymbol2);
					
				
					AdjacencyMatrix.Node node1 = new AdjacencyMatrix.Node(
							NodeType.STRING, geneSymbol1);

					String shortNameType = interactionTypeSifMap
					     .get(interactionDetail.getInteractionType());

					matrix.add(node1, node2,
							new Float(interactionDetail.getConfidenceValue()),
							shortNameType);
                    
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
				if (this.isShowDiffExpr)
					matrix.setGeneRankingMap(getGeneRankMap(matrix, geneIdToSymbol));
				adjacencyMatrixdataSet = new AdjacencyMatrixDataSet(matrix, 0,
						"Adjacency Matrix", "Fmoa Data", null);

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

	private void loadInteractionTypeMap() {
		try {
			if (interactionTypeSifMap == null)
				interactionTypeSifMap = LincsInterface.getLincsService()
						.getInteractionTypeMap();
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}
	
	private Map<String, Integer> getGeneRankMap(AdjacencyMatrix matrix, Map<String, String> geneIdToSymbol)
	{
		Map<String, Integer> map = new HashMap<String, Integer>();
		String geneIds = "";
	    String[] keySet = geneIdToSymbol.keySet().toArray(new String[0]);
		geneIds = keySet[0];
		for(int i=1; i<keySet.length; i++)
			geneIds += "," + keySet[i];
		try {
		   List<GeneRank> geneRankList = LincsInterface.getLincsService().getGeneRankData(geneIds, compoundId, differentialExpressionRunId);
		 
		   for(int i=0; i<geneRankList.size(); i++)
		   {		  
			   map.put(geneIdToSymbol.get(geneRankList.get(i).getGeneId().toString()),i);
		   }		   
		   return map;
		
		}catch(Exception e)
		{
			log.error(e.getMessage());
		}
		
		return map;
	}	 

}