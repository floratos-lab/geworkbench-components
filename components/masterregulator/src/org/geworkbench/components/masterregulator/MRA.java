package org.geworkbench.components.masterregulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
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
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.builtin.projects.DataSetNode;
import org.geworkbench.builtin.projects.DataSetSubNode;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.components.masterregulator.TAnalysis.TAnalysisException;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.FishersExactTest;
import org.geworkbench.util.ProgressDialog;
import org.geworkbench.util.ProgressItem;
import org.geworkbench.util.ProgressTask;

/**
 * @author yc2480
 * @version $Id$
 */
public class MRA extends AbstractAnalysis implements
		ClusteringAnalysis {
	private static final long serialVersionUID = 940204157465957195L;
	
	private Log log = LogFactory.getLog(this.getClass());
	private final String analysisName = "MRA-FET";

	private MRAPanel mraAnalysisPanel = new MRAPanel();
	private ProgressDialog pd = ProgressDialog.getInstance(false);
	private class ResultWrapper{
		private AlgorithmExecutionResults rst = null;
		private void setResult(AlgorithmExecutionResults rst){
			this.rst = rst;
		}
		private AlgorithmExecutionResults getResult(){
			return this.rst;
		}
	}

	public MRA() {
		setDefaultPanel(mraAnalysisPanel);
	}

	@Override
	public AlgorithmExecutionResults execute(Object input) {
		ResultWrapper rw = new ResultWrapper();
		MRATask task = new MRATask(ProgressItem.BOUNDED_TYPE, "Executing Master Regulator Analysis: started", input, rw);
		pd.executeTask(task);
		while(!task.isDone()){
			try{
				Thread.sleep(1000);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return rw.getResult();
	}

	private class MRATask extends ProgressTask<Void, String>{
		Object input;
		ResultWrapper rw = null;
		MRATask(int pbtype, String message, Object input, ResultWrapper rw){
    		super(pbtype, message);
			this.input = input;
			this.rw = rw;
		}

		@Override
    	protected void done(){
	   		pd.removeTask(this);
	   	}

		@Override
	   	protected void process(List<String> chunks){
	   		for (String message : chunks){
	   			if (isCancelled()) return;
	   			this.setMessage(message);
	   		}
	   	}

		@Override
		protected Void doInBackground() {
			try{
				rw.setResult(executeInBackground());
			}catch(Exception e){
				rw.setResult(new AlgorithmExecutionResults(false, "Exception: "+e, null));
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		private AlgorithmExecutionResults executeInBackground() throws Exception {
			// read input data, dataset view, dataset, etc.
			if (!(input instanceof DSMicroarraySetView)){
				return new AlgorithmExecutionResults(false,
						"Input dataset for MRA analysis should be a MicroarraySet.\n"+
						"But you selected a "+input.getClass().getName(), null);
			};

			if (mraAnalysisPanel.use5colnetwork()){
				return new AlgorithmExecutionResults(false,
						"Local MRA does not use network in "+mraAnalysisPanel.marina5colformat, null);
			}
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
			DSMicroarraySet maSet = view.getMicroarraySet();
			AdjacencyMatrixDataSet amSet = mraAnalysisPanel.getAdjMatrixDataSet();
			if (amSet==null || amSet.getMatrix()==null){
				return new AlgorithmExecutionResults(false,
						"Network (Adjacency Matrix) has not been loaded yet.", null);
			};		
	
			String transcriptionFactorsStr = mraAnalysisPanel.getTranscriptionFactor();
			String signatureMarkersSTr = mraAnalysisPanel.getSigMarkers();
			if (transcriptionFactorsStr.equals("")){
				return new AlgorithmExecutionResults(false,
						"Transcription Factor has not been entered yet.", 
						null);
			};
			if (signatureMarkersSTr.equals("") && mraAnalysisPanel.getTTestNode()==null){
				return new AlgorithmExecutionResults(false,
						"Signature Marker has not been entered yet.", 
						null);
			};
			 
			// validate data and parameters.
			ParamValidationResults validation = validateParameters();
			if (!validation.isValid()) {
				return new AlgorithmExecutionResults(false,
						validation.getMessage(), null);
			}
			if(isCancelled()) return null;
			// analysis
			DSMasterRagulatorResultSet<DSGeneMarker> mraResultSet = new CSMasterRegulatorResultSet<DSGeneMarker>(
					maSet, analysisName, view.markers().size());		
			//t-analysis
			log.info("Executing T Analysis");
			Map<DSGeneMarker, Double> values = null;
			try {
				TAnalysis tTestAnalysis= new TAnalysis(view);
				values = tTestAnalysis.calculateDisplayValues();
			} catch (TAnalysisException e1) {
				return new AlgorithmExecutionResults(false,
						e1.getMessage(), 
						null);
			}
			if (values==null) return new AlgorithmExecutionResults(false,
					"The set of display values is set null.", 
					null);
			mraResultSet.setValues(values);
			
			sortByValue(values, mraResultSet);
		 
			//get TFs
			ArrayList<DSGeneMarker> transcriptionFactors = parseMarkers(maSet.getMarkers(), transcriptionFactorsStr);
			log.info("We got "+transcriptionFactors.size()+" transcription factors");
			
			if (transcriptionFactors.size()==0){
				return new AlgorithmExecutionResults(false,
						"Sorry, but in the Microarray Set, I can not find the Transcription Factors you entered.", 
						null);
			}
			
			
			//get Signature Markers
			ArrayList<DSGeneMarker> signatureMarkers=new ArrayList<DSGeneMarker>();
			//genes with positive differential expression(t-value>=0)
			ArrayList<DSGeneMarker> posDE = new ArrayList<DSGeneMarker>();
			//genes with negative differential expression(t-value<0)
			ArrayList<DSGeneMarker> negDE = new ArrayList<DSGeneMarker>();
			String ttestlabel = mraAnalysisPanel.getTTestNode();
			DSSignificanceResultSet<DSGeneMarker> ttestrst = null;
			if (ttestlabel != null)
				ttestrst = ttesthm.get(ttestlabel);
			if (ttestrst != null){
				for (DSGeneMarker marker : ttestrst.getSignificantMarkers()){
					signatureMarkers.add(marker);
					if (mraAnalysisPanel.twoFET()){
						if (mraResultSet.getValue(marker)>=0)	posDE.add(marker);
						else									negDE.add(marker);
					}
				}
			}else{
				signatureMarkers = parseMarkers(maSet.getMarkers(), signatureMarkersSTr);
				if (mraAnalysisPanel.twoFET()){
					for (DSGeneMarker marker : signatureMarkers){
						if (mraResultSet.getValue(marker)>=0)	posDE.add(marker);
						else									negDE.add(marker);
					}
				}
			}
			log.info("We got "+signatureMarkers.size()+" signature markers");
	
			//y = size of significantMarkers		 
			int y = signatureMarkers.size();
			//x = size of markers
			int x = maSet.getMarkers().size();
			
			DSMicroarraySet microarraySet = (DSMicroarraySet) amSet.getParentDataSet();
			//for each TF A 
			int i = 0;
			for (DSGeneMarker tfA: transcriptionFactors){
				if(isCancelled()) return null;
				i++;
				publish("Executing Master Regulator Analysis: "+100*i/transcriptionFactors.size()+"%");
				setProgress(100*i/transcriptionFactors.size());
				//we calculate the set N(A), i.e., all the direct neighbors of A in N (Aracne network) 
				ArrayList<DSGeneMarker> nA = new ArrayList<DSGeneMarker>();
				//regulon genes with positive spearman correlation to the TF(sc>=0)
				ArrayList<DSGeneMarker> posSC = new ArrayList<DSGeneMarker>();
				//regulon genes with negative spearman correlation to the TF(sc<0)
				ArrayList<DSGeneMarker> negSC = new ArrayList<DSGeneMarker>();
				//int geneid = tfA.getSerial();
				AdjacencyMatrix adjMatrix = amSet.getMatrix();
				Set<DSGeneMarker> neighbors = adjMatrix.get(tfA, microarraySet );
				if (neighbors!=null){
					for (DSGeneMarker neighbor : neighbors){//for each neighbor
						nA.add(neighbor);
						if (mraAnalysisPanel.twoFET()){
							SpearmansCorrelation SC = new SpearmansCorrelation();
							if (SC.correlation(maSet.getRow(tfA), maSet.getRow(neighbor)) >= 0)
								posSC.add(neighbor);
							else negSC.add(neighbor);
						}
					}
				}
				//now we got z/nA that are also found in M (because transcriptionFactors is a subset of M)
				int z = nA.size();
				
				//genes in regulon = nA
	
				//calculate genes in target list, which is intersection of nA and significant genes from t-test  
				ArrayList<DSGeneMarker> genesInTargetList = new ArrayList<DSGeneMarker>();
				//a1=(posDE AND posSC) + (negDE AND negSC)
				ArrayList<DSGeneMarker> a1 = new ArrayList<DSGeneMarker>();
				//a2=(negDE AND posSC) + (posDE AND negSC)
				ArrayList<DSGeneMarker> a2 = new ArrayList<DSGeneMarker>();
				for (DSGeneMarker marker: signatureMarkers){
					if (nA.contains(marker)){
						genesInTargetList.add(marker);				 
						log.debug(tfA.getShortName()+"\t"+ marker.getShortName()+"\tT:"+values.get(marker));
						if (mraAnalysisPanel.twoFET()){
							if ((posDE.contains(marker) && posSC.contains(marker)) ||
									(negDE.contains(marker) && negSC.contains(marker)))
								a1.add(marker);
							else a2.add(marker);
						}
					}
				}
				
				//now we got w in genesInTargetList
				int w = mraAnalysisPanel.twoFET()?a1.size():genesInTargetList.size();
				
				//calculate P-value by using Fisher's Exact test
				int a=w;
				int b=y-w;
				int c=z-w;
				int d=x-z-y+w;
				double pValue = FishersExactTest.getRightSideOneTailedP(a,b,c,d);
				log.debug(tfA.getLabel()+"\t"+ pValue +"\ta:"+a+"\tb:"+b+"\tc:"+c+"\td:"+d+"\tGenes in Regulon:"+z+"\tGenes in activated Targetlist:"+w);
	
				double pValue2 = 1.1;
				char mode = 0;
				if (mraAnalysisPanel.twoFET()){
					w = a2.size();
					a=w;
					b=y-w;
					c=z-w;
					d=x-z-y+w;
					pValue2 = FishersExactTest.getRightSideOneTailedP(a,b,c,d);
	
					log.debug(tfA.getLabel()+"\t"+ pValue2 +"\ta:"+a+"\tb:"+b+"\tc:"+c+"\td:"+d+"\tGenes in Regulon:"+z+"\tGenes in repressed Targetlist:"+w);
	
					//show the most significant pValue of the two FET
					if (pValue <= pValue2){ //activator
						mode = CSMasterRegulatorResultSet.ACTIVATOR;
						log.debug(pValue+" <= "+pValue2+" "+mode);
					}else {                 //repressor
						mode = CSMasterRegulatorResultSet.REPRESSOR;
						log.debug(pValue+" > "+pValue2+" "+mode);
						pValue = pValue2;
					}
				}
	
				double threshold = mraAnalysisPanel.getPValue();
				if (mraAnalysisPanel.standardBonferroni())
					threshold /= transcriptionFactors.size();
				if ( pValue > threshold)	continue;
	
				DSItemList<DSGeneMarker> nAItemList = new CSItemList<DSGeneMarker>();
				nAItemList.addAll(nA);
				mraResultSet.setGenesInRegulon(tfA, nAItemList);
				
				DSItemList<DSGeneMarker> genesInTargetItemList = new CSItemList<DSGeneMarker>();
				genesInTargetItemList.addAll(genesInTargetList);
				mraResultSet.setGenesInTargetList(tfA, genesInTargetItemList);
	
				mraResultSet.setPValue(tfA, pValue);
				mraResultSet.setMode(tfA, mode);
				log.debug(tfA.getLabel()+"\t"+mode+" "+pValue);
			}
			
			// generate result
			String historyStr = generateHistoryString(view);
	
			/*String groupAndChipsString = "Groups analyzed:\n";
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
			historyStr += groupAndChipsString;*/
	
			HistoryPanel.addToHistory(mraResultSet, historyStr);
			
			DSPanel<DSGeneMarker> selectedMarkers = new CSPanel<DSGeneMarker>(
					"MRA Genes", "MRA");
			
			selectedMarkers.addAll(mraResultSet.getTFs());
			mraResultSet.setDescription("# of master regulators (MRA): "+selectedMarkers.size());
	
			publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(
					DSGeneMarker.class, selectedMarkers,
					SubpanelChangedEvent.NEW));
	
			AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
					"MRA Analysis", mraResultSet);
			return results;
		}
	}

	//to be refactored with genepanel
	private ArrayList<DSGeneMarker> parseMarkers(DSItemList<DSGeneMarker> itemList, String str){
		ArrayList<DSGeneMarker> transcriptionFactors = new ArrayList<DSGeneMarker>();
		if (str==null || str.length()==0) return transcriptionFactors;

		List<String> markerstr = new ArrayList<String>();
		String[] labels = str.split(", ");
		if (labels.length==1)
			markerstr = Arrays.asList(labels[0].split(","));
		else{
			for(String label : labels){
				String[] a = label.split(",");
				if (a.length > 0) label = a[0];
				markerstr.add(label);
			}
		}
		for(DSGeneMarker marker: itemList) {
			if(markerstr.contains(marker.getGeneName()))
				transcriptionFactors.add(marker);
		}
		if (transcriptionFactors.isEmpty()){
			for (String label : markerstr){
				DSGeneMarker marker = itemList.get(label);
				if (marker != null)
					transcriptionFactors.add(marker);
			}
		}
		return transcriptionFactors;
	}
	
	static void sortByValue(final Map<DSGeneMarker, Double> values,
			DSMasterRagulatorResultSet<DSGeneMarker> mraResultSet){
		Map<DSGeneMarker, Integer> gene2rankMap = new HashMap<DSGeneMarker, Integer>();
		List<DSGeneMarker> genes = new ArrayList<DSGeneMarker>();
		genes.addAll(values.keySet());

		//sort genes by value
		Collections.sort(genes, new Comparator<DSGeneMarker>(){
			public int compare(DSGeneMarker m1, DSGeneMarker m2){
				return values.get(m1).compareTo(values.get(m2));
			}
		});
		mraResultSet.setMinValue(values.get(genes.get(0)));
		mraResultSet.setMaxValue(values.get(genes.get(genes.size()-1)));

		//give same ranks to genes with same value
		// TODO we should use standard library, e.g. apache commons math to do this
		gene2rankMap.put(genes.get(0), 0);
		double lastValue = values.get(genes.get(0));
		int lastRank = 0;
		for (int i = 1; i < genes.size(); i++) {
			int rank = i;
			DSGeneMarker marker = genes.get(i);
			double value = values.get(marker);
			if (value == lastValue) {
				rank = lastRank;
			}
			gene2rankMap.put(marker, rank);
			lastValue = value;
			lastRank = rank;
		}
		mraResultSet.setRanks(gene2rankMap);
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
		StringBuffer histStr = new StringBuffer("Generated with MRA run with parameters:\n\n");	 
		histStr .append( "[PARA] Load Network: " + mraAnalysisPanel.getAdjMatrixDataSet().getDataSetName()  ).append("\n");
		histStr .append( "[PARA] FET/GSEA p-value : " + mraAnalysisPanel.getPValue() ).append("\n");		
		if (mraAnalysisPanel.twoFET())
		   histStr .append( "[PARA] FET Runs: Two(enrichment plus mode of activity)\n");		
		else
		   histStr .append( "[PARA] FET Runs: One(enrichment only\n");		
		
		if (mraAnalysisPanel.standardBonferroni())
			   histStr .append( "[PARA] Multiple Testing Correction: Standard Bonferroni\n");		
			else
			   histStr .append( "[PARA] Multiple Testing Correction: No correction\n");			
		histStr .append( "[PARA] Master Regulators: " + mraAnalysisPanel.getTranscriptionFactor() ).append("\n");
		histStr .append( "[PARA] Signature Markers: " + mraAnalysisPanel.getSigMarkers() ).append("\n\n\n");
		histStr.append(generateHistoryForMaSetView(view));
		
		return histStr.toString();
	}
	
	@Subscribe
	public void receive(GeneSelectorEvent e, Object source) {
		if (e.getPanel() != null) {
			DSPanel<DSGeneMarker> selectorPanel = e.getPanel();
			((MRAPanel) aspp).setSelectorPanel(selectorPanel);		
		} else
			log.debug("MRA Received Gene Selector Event: Selection panel sent was null");
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
	@SuppressWarnings({ "rawtypes","unchecked" })
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
		DSDataSet dataSet = e.getDataSet();
		if (!(dataSet instanceof DSMicroarraySet)) {
			return;
		}

		mraAnalysisPanel.setMicroarraySet((DSMicroarraySet)dataSet);

		ProjectSelection selection = ProjectPanel.getInstance().getSelection();
        DataSetNode dNode = selection.getSelectedDataSetNode();
        if(dNode == null){
        	return;
        }

        String currentTargetSet = this.mraAnalysisPanel.getSelectedAdjMatrix();
        this.mraAnalysisPanel.clearAdjMatrixCombobox();
        this.mraAnalysisPanel.clearTTestNodes();
        ttesthm.clear();
        Enumeration children = dNode.children();
        while (children.hasMoreElements()) {
            Object obj = children.nextElement();
            if (obj instanceof DataSetSubNode) {
                DSAncillaryDataSet ads = ((DataSetSubNode) obj)._aDataSet;
                if (ads instanceof AdjacencyMatrixDataSet) {
                    this.mraAnalysisPanel.addAdjMatrixToCombobox((AdjacencyMatrixDataSet) ads);                    
                    if (currentTargetSet != null && StringUtils.equals(ads.getDataSetName(), currentTargetSet.trim())) {
                    	mraAnalysisPanel.setSelectedAdjMatrix(ads.getDataSetName());
        			}
                } else if (ads instanceof DSSignificanceResultSet){
                	mraAnalysisPanel.addTTestNode(ads.getLabel());
                	ttesthm.put(ads.getLabel(), (DSSignificanceResultSet<DSGeneMarker>)ads);
                }
            }
        }
	}
	private HashMap<String, DSSignificanceResultSet<DSGeneMarker>> ttesthm = new HashMap<String, DSSignificanceResultSet<DSGeneMarker>>();
	
	
	@Publish
	public org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> event) {
		return event;
	}	

}
