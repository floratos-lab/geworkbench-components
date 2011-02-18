package org.geworkbench.components.ttest;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
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
import org.geworkbench.builtin.projects.DataSetNode;
import org.geworkbench.builtin.projects.DataSetSubNode;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;

/**
 * @author yc2480
 * @version $Id$
 */
public class MasterRegulatorAnalysis extends AbstractAnalysis implements
		ClusteringAnalysis {
	private static final long serialVersionUID = 940204157465957195L;
	
	private Log log = LogFactory.getLog(this.getClass());
	private final String analysisName = "MRA";
	private MasterRegulatorPanel mraAnalysisPanel = new MasterRegulatorPanel();

	public MasterRegulatorAnalysis() {
		setDefaultPanel(mraAnalysisPanel);
	}

	@Override
	public int getAnalysisType() {
		return AbstractAnalysis.MRA_TYPE;
	}

	@SuppressWarnings("unchecked")
	public AlgorithmExecutionResults execute(Object input) {
		// read input data, dataset view, dataset, etc.
		if (!(input instanceof DSMicroarraySetView)){
			return new AlgorithmExecutionResults(false,
					"Input dataset for MRA analysis should be a MicroarraySet.\n"+
					"But you selected a "+input.getClass().getName(), null);
		};
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
		DSMicroarraySet<DSMicroarray> maSet = view.getMicroarraySet();
		AdjacencyMatrixDataSet amSet = mraAnalysisPanel.getAdjMatrixDataSet();
		if (amSet==null || amSet.getMatrix()==null){
			return new AlgorithmExecutionResults(false,
					"Network (Adjacency Matrix) has not been loaded yet.", null);
		};		

		String transcriptionFactorsStr = mraAnalysisPanel.getTranscriptionFactor();
		if (transcriptionFactorsStr.equals("")){
			return new AlgorithmExecutionResults(false,
					"Transcription Factor has not been entered yet.", 
					null);
		};
		TtestAnalysisPanel tTestAnalysisPanel = mraAnalysisPanel.getTTestPanel();
		TtestAnalysis tTestAnalysis= new TtestAnalysis(tTestAnalysisPanel);
		tTestAnalysisPanel.setVisible(false);
		// validate data and parameters.
		ParamValidationResults validation = validateParameters();
		if (!validation.isValid()) {
			return new AlgorithmExecutionResults(false,
					validation.getMessage(), null);
		}
		// analysis
		DSMasterRagulatorResultSet<DSGeneMarker> mraResultSet = new CSMasterRegulatorResultSet<DSGeneMarker>(
				maSet, analysisName, view.markers().size());		
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
		
		//get TFs
		ArrayList<DSGeneMarker> transcriptionFactors=new ArrayList<DSGeneMarker>();
		StringTokenizer st = new StringTokenizer(transcriptionFactorsStr, ", ");
		while (st.hasMoreTokens()){
			String markerName = st.nextToken();
			try{
				DSGeneMarker marker = maSet.getMarkers().get(markerName);
				if (marker!=null)
					transcriptionFactors.add(marker);
			}catch(Exception e){
				log.info("We can not find marker " + markerName + " in our MicroarraySet.");
			}
		}
		log.info("We got "+transcriptionFactors.size()+" transcription factors");
		
		if (transcriptionFactors.size()==0){
			return new AlgorithmExecutionResults(false,
					"Sorry, but in the Microarray Set, I can not find the Transcription Factors you entered.", 
					null);
		}
			
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
			if (adjMatrix.get(geneid)!=null){
				for (Object key : adjMatrix.get(tfA.getSerial()).keySet()){//for each neighbor
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
		String historyStr ="";
		historyStr += generateHistoryString(view);

		historyStr += tTestAnalysis.GenerateHistoryHeader();		
		historyStr += tTestAnalysis.GenerateMarkerString(view);

		String groupAndChipsString = "Groups analyzed:\n";
		{//generate group information
			DSAnnotationContextManager manager = CSAnnotationContextManager
			.getInstance();
	DSAnnotationContext<DSMicroarray> context = manager
			.getCurrentContext(maSet);
			String[][] labels = new String[2][];
			labels[0] = context
					.getLabelsForClass(CSAnnotationContext.CLASS_CASE);
			labels[1] = context
					.getLabelsForClass(CSAnnotationContext.CLASS_CONTROL);
			HashSet<String>[] classSets = new HashSet[2];
			for (int j = 0; j < 2; j++) {
				String[] classLabels = labels[j];
				classSets[j] = new HashSet<String>();
	
				if (j == 0)
					groupAndChipsString += "\t case group(s): \n";
				else
					groupAndChipsString += "\t control group(s): \n";
	
				for (int i = 0; i < classLabels.length; i++) {
					String label = classLabels[i];
					if (context.isLabelActive(label) || !view.useItemPanel()) {
						// if (context.isLabelActive(label)) {
						classSets[j].add(label);
						groupAndChipsString += GenerateGroupAndChipsString(context
								.getItemsWithLabel(label));
					}
				}
			}
		}
		historyStr += groupAndChipsString;

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
//		histStr += "Correction: " + mraAnalysisPanel.getCorrection() + "\n";
//		histStr += "P-Value: " + mraAnalysisPanel.getPValue() + "\n";
		histStr += "Transcription Factor: " + mraAnalysisPanel.getTranscriptionFactor() + "\n";
		histStr += "adjMatrix: "
				+ mraAnalysisPanel.getAdjMatrixDataSet().getDataSetName()
				+ "\n\n\n";
		return histStr;
	}
	
	@Subscribe
	public void receive(GeneSelectorEvent e, Object source) {
		if (e.getPanel() != null) {
			DSPanel<DSGeneMarker> selectorPanel = e.getPanel();
			((MasterRegulatorPanel) aspp).setSelectorPanel(((MasterRegulatorPanel) aspp), selectorPanel);
		} else
			log.debug("MRA Received Gene Selector Event: Selection panel sent was null");
	}

	@SuppressWarnings("rawtypes")
	@Subscribe
	public void receive(org.geworkbench.events.ProjectNodeAddedEvent e,
			Object source) {
		DSDataSet dataSet = e.getAncillaryDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			this.mraAnalysisPanel
					.addAdjMatrixToCombobox((AdjacencyMatrixDataSet) dataSet);
		}
	}
	@SuppressWarnings("rawtypes")
	@Subscribe
	public void receive(org.geworkbench.events.ProjectNodeRemovedEvent e,
			Object source) {
		DSDataSet dataSet = e.getAncillaryDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			this.mraAnalysisPanel
					.removeAdjMatrixToCombobox((AdjacencyMatrixDataSet) dataSet);
		}
	}
	@SuppressWarnings("rawtypes")
	@Subscribe
	public void receive(org.geworkbench.events.ProjectNodeRenamedEvent e,
			Object source) {
		DSDataSet dataSet = e.getDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			this.mraAnalysisPanel
					.renameAdjMatrixToCombobox((AdjacencyMatrixDataSet)dataSet, e.getOldName(),e.getNewName());
		}
	}
	@SuppressWarnings({ "rawtypes" })
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {

        ProjectSelection selection = ((ProjectPanel) source).getSelection();
        DataSetNode dNode = selection.getSelectedDataSetNode();
        if(dNode == null){
        	return;
        }
        
        this.mraAnalysisPanel.clearAdjMatrixCombobox();
        Enumeration children = dNode.children();
        while (children.hasMoreElements()) {
            Object obj = children.nextElement();
            if (obj instanceof DataSetSubNode) {
                DSAncillaryDataSet ads = ((DataSetSubNode) obj)._aDataSet;
                if (ads instanceof AdjacencyMatrixDataSet) {
                    this.mraAnalysisPanel.addAdjMatrixToCombobox((AdjacencyMatrixDataSet) ads);
                }
            }
        }
	}
	private String GenerateGroupAndChipsString(DSPanel<DSMicroarray> panel) {
		String histStr = null;

		histStr = "\t     " + panel.getLabel() + " (" + panel.size()
				+ " chips)" + ":\n";
		;

		int aSize = panel.size();
		for (int aIndex = 0; aIndex < aSize; aIndex++)
			histStr += "\t\t" + panel.get(aIndex) + "\n";

		return histStr;
	}
}
