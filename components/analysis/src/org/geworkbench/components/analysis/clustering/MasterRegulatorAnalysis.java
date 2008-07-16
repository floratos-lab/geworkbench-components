package org.geworkbench.components.analysis.clustering;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMasterRegulatorResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMasterRagulatorResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;

/**
 * @author yc2480
 * @version $Id: MasterRegulatorAnalysis.java,v 1.1 2008-07-16 21:34:50 chiangy Exp $
 */
public class MasterRegulatorAnalysis extends AbstractAnalysis implements
		ClusteringAnalysis {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Log log = LogFactory.getLog(this.getClass());
	private final String analysisName = "MRA";
	private MasterRegulatorPanel mraAnalysisPanel = new MasterRegulatorPanel();

	public MasterRegulatorAnalysis() {
		setLabel("MRA Analysis");
		setDefaultPanel(mraAnalysisPanel);
	}

	@Override
	public int getAnalysisType() {
		return AbstractAnalysis.MRA_TYPE;
	}

	public AlgorithmExecutionResults execute(Object input) {
		// read input data, dataset view, dataset, etc.
		assert (input instanceof DSMicroarraySetView);
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
		DSMicroarraySet<DSMicroarray> maSet = view.getMicroarraySet();
		AdjacencyMatrixDataSet amSet = mraAnalysisPanel.getAdjMatrixDataSet();
		String correction = mraAnalysisPanel.getCorrection();
		//double pValue = mraAnalysisPanel.getPValue();
		String transcriptionFactorsStr = mraAnalysisPanel.getTranscriptionFactor();
		TtestAnalysisPanel tTestAnalysisPanel = mraAnalysisPanel.getTTestPanel();
		TtestAnalysis tTestAnalysis= new TtestAnalysis(tTestAnalysisPanel);
		String historyStr ="";
		historyStr = tTestAnalysis.GenerateHistoryHeader();
		historyStr = tTestAnalysis.GenerateMarkerString(view);
		// validate data and parameters.
		ParamValidationResults validation = validateParameters();
		if (!validation.isValid()) {
			return new AlgorithmExecutionResults(false,
					validation.getMessage(), null);
		}
		// analysis
		DSMasterRagulatorResultSet mraResultSet = new CSMasterRegulatorResultSet(
				maSet, analysisName);		
		//t-test
		log.info("Executing T-Test...");
		AlgorithmExecutionResults tTestResult = tTestAnalysis.calculate(input, true);
		if (!tTestResult.isExecutionSuccessful()) return tTestResult;
		DSSignificanceResultSet<DSGeneMarker> tTestResultSet = (DSSignificanceResultSet<DSGeneMarker>)tTestResult.getResults();
		mraResultSet.setSignificanceResultSet(tTestResultSet);
		log.info("We got TGs");
		for (DSGeneMarker marker : tTestResultSet.getSignificantMarkers()) {
			log.info("Significant markers: "+marker.getLabel());
		}
		
		
		log.info("Get network from adj matrix...");
		for (Iterator iterator = amSet.getMatrix().getKeys().iterator(); iterator
				.hasNext();) { // for each from node
			Integer id1 = (Integer) iterator.next();
			HashMap<Integer, Float> neighbors = amSet.getMatrix().get(id1);
			if (neighbors!=null)
			for (Iterator iterator2 = neighbors.keySet().iterator(); iterator2
					.hasNext();) { // for each end node
				Integer id2 = (Integer) iterator2.next();
				log.info("Got an edge:("+id1+")"+maSet.getMarkers().get(id1).getLabel()
						+ " - " + maSet.getMarkers().get(id2).getLabel());
			}
		}

		//get TFs
		ArrayList<DSGeneMarker> transcriptionFactors=new ArrayList<DSGeneMarker>();
		StringTokenizer st = new StringTokenizer(transcriptionFactorsStr, ", ");
		while (st.hasMoreTokens()){
			String markerName = st.nextToken();
			try{
				DSGeneMarker marker = maSet.getMarkers().get(markerName);
				transcriptionFactors.add(marker);
			}catch(Exception e){
				log.info("We can not find marker " + markerName + " in our MicroarraySet.");
			}
		}
		log.info("We got "+transcriptionFactors.size()+" transcription factors");
		
		//y = size of significantMarkers
		int y = tTestResultSet.getSignificantMarkers().size();
		//x = size of markers
		int x = maSet.getMarkers().size();
		
		//for each TF A 
		for (DSGeneMarker tfA: transcriptionFactors){
			//we calculate the set N(A), i.e., all the direct neighbors of A in N (Aracne network) 
			ArrayList<DSGeneMarker> nA = new ArrayList<DSGeneMarker>();
			//int geneid = tfA.getSerial();
			AdjacencyMatrix adjMatrix = amSet.getMatrix();
			int geneid = adjMatrix.getMappedId(tfA.getSerial());
			HashMap test = adjMatrix.get(geneid);
			if (test!=null){
				Set test2 = test.keySet();
				for (Object key : amSet.getMatrix().get(tfA.getSerial()).keySet()){//for each neighbor
					Integer neighborId = (Integer)key;
					DSGeneMarker neighbor = maSet.getMarkers().get(neighborId);
					nA.add(neighbor);
				}
			}
			//now we got z/nA that are also found in M (because transcriptionFactors is a subset of M)
			int z = nA.size();
			
			//genes in regulon = nA

			//calculate genes in target list, which is intersection of nA and significant genes from t-test  
			ArrayList<DSGeneMarker> genesInTargetList = new ArrayList<DSGeneMarker>();
			for (DSGeneMarker marker: tTestResultSet.getSignificantMarkers()){
				if (nA.contains(marker)){
					genesInTargetList.add(marker);
					mraResultSet.setPValueOf(tfA,marker,tTestResultSet.getSignificance(marker));
					mraResultSet.setTTestValueOf(tfA,marker,tTestResultSet.getTValue(marker));
					log.debug(tfA.getShortName()+"\t"+ marker.getShortName()+"\tP:"+tTestResultSet.getSignificance(marker)+"\tT:"+tTestResultSet.getTValue(marker));
				}
			}
			//now we got w in genesInTargetList
			int w = genesInTargetList.size();
			
			//calculate P-value by using Fishe's Exact test
			int a=w;
			int b=y-w;
			int c=z-w;
			int d=x-z-y+w;
			double pValue = FishersExactTest.getPValue(a,b,c,d);

			DSItemList<DSGeneMarker> nAItemList = new CSItemList<DSGeneMarker>();
			nAItemList.addAll(nA);
			mraResultSet.setGenesInRegulon(tfA, nAItemList);
			
			DSItemList<DSGeneMarker> genesInTargetItemList = new CSItemList<DSGeneMarker>();
			genesInTargetItemList.addAll(genesInTargetList);
			mraResultSet.setGenesInTargetList(tfA, genesInTargetItemList);

			mraResultSet.setPValue(tfA, pValue);
			
			log.debug(tfA.getLabel()+"\t"+ pValue +"\ta:"+a+"\tb:"+b+"\tc:"+c+"\td:"+d+"\tGenes in Regulon:"+z+"\tGenes in Targetlist:"+w);
		}
		
		// generate result

		historyStr = generateHistoryString(view)+historyStr;
		ProjectPanel.addToHistory(mraResultSet, historyStr);

		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"MRA Analysis", mraResultSet);
		return results;
	}

	public ParamValidationResults validateParameters() {
		try {
			if ((mraAnalysisPanel.getPValue() < 0)
					|| (mraAnalysisPanel.getPValue() > 1)) {
				return new ParamValidationResults(false,
						"P-value should be a number within 0.0~1.0");
			}
		} catch (NumberFormatException nfe) {
			return new ParamValidationResults(false,
					"P-value should be a number");
		}
		ParamValidationResults answer = new ParamValidationResults(true,
				"validate");
		return answer;
	}

	private String generateHistoryString(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
		String histStr = "";
		// Header
		histStr += "Generated with MRA run with parameters:\n";
		histStr += "----------------------------------------\n";
		histStr += "Correction: " + mraAnalysisPanel.getCorrection() + "\n";
		histStr += "P-Value: " + mraAnalysisPanel.getPValue() + "\n";
		histStr += "Transcription Factor: " + mraAnalysisPanel.getTranscriptionFactor() + "\n";
		histStr += "adjMatrix: "
				+ mraAnalysisPanel.getAdjMatrixDataSet().getDataSetName()
				+ "\n";
		return histStr;
	}

	@Subscribe
	public void receive(org.geworkbench.events.ProjectNodeAddedEvent e,
			Object source) {
		DSDataSet dataSet = e.getAncillaryDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			this.mraAnalysisPanel
					.addAdjMatrixToCombobox((AdjacencyMatrixDataSet) dataSet);
		}
	}
	@Subscribe
	public void receive(org.geworkbench.events.ProjectNodeRemovedEvent e,
			Object source) {
		DSDataSet dataSet = e.getAncillaryDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			this.mraAnalysisPanel
					.removeAdjMatrixToCombobox((AdjacencyMatrixDataSet) dataSet);
		}
	}
	@Subscribe
	public void receive(org.geworkbench.events.ProjectNodeRenamedEvent e,
			Object source) {
		DSDataSet dataSet = e.getDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			this.mraAnalysisPanel
					.renameAdjMatrixToCombobox((AdjacencyMatrixDataSet)dataSet, e.getOldName(),e.getNewName());
		}
	}
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e,
			Object source) {
		if (e.getMessage().equals(org.geworkbench.events.ProjectEvent.SELECTED)){
			DSDataSet dataSet = e.getDataSet();
			if (dataSet instanceof DSMicroarraySet) {
				this.mraAnalysisPanel.setMicroarraySet((DSMicroarraySet)dataSet);
			}else{
				this.mraAnalysisPanel.setMicroarraySet(null);
			}
		}
	}
	
}
