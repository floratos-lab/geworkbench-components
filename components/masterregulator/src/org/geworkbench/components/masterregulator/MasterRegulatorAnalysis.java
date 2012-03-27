package org.geworkbench.components.masterregulator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContext; 
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
public class MasterRegulatorAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {
	private static final long serialVersionUID = 940204157465957195L;
	
	private Log log = LogFactory.getLog(this.getClass());
	private final String analysisName = "MRA";
    private static final Pattern pattern = Pattern.compile("^mra\\d+$");
	private MasterRegulatorPanel mraAnalysisPanel = new MasterRegulatorPanel();
	private ProgressDialog pd = ProgressDialog.create(ProgressDialog.NONMODAL_TYPE);
	private class ResultWrapper{
		AlgorithmExecutionResults rst = null;
		private void setResult(AlgorithmExecutionResults rst){
			this.rst = rst;
		}
		private AlgorithmExecutionResults getResult(){
			return this.rst;
		}
	}

	public MasterRegulatorAnalysis() {
		setDefaultPanel(mraAnalysisPanel);
	}

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

	private class MRATask extends ProgressTask<AlgorithmExecutionResults, String>{
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
    		if (isCancelled()) return;
    		try{
    			AlgorithmExecutionResults rst = get();
    			rw.setResult(rst);
    		}catch(Exception e){
    			e.printStackTrace();
    		}
	   	}

		@Override
	   	protected void process(List<String> chunks){
	   		for (String message : chunks){
	   			if (isCancelled()) return;
	   			pb.setMessage(message);
	   		}
	   	}

		@Override
		@SuppressWarnings("unchecked")
		protected AlgorithmExecutionResults doInBackground() throws Exception {
			// read input data, dataset view, dataset, etc.
			if (!(input instanceof DSMicroarraySetView)){
				return new AlgorithmExecutionResults(false,
						"Input dataset for MRA analysis should be a MicroarraySet.\n"+
						"But you selected a "+input.getClass().getName(), null);
			};
			if (mraAnalysisPanel.getResultid() != null)
				return new AlgorithmExecutionResults(false,
						"Retrieving prior result is not supported by local MRA.", null);
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
			String ttestlabel = mraAnalysisPanel.getTTestNode();
			DSSignificanceResultSet<DSGeneMarker> ttestrst = null;
			if (ttestlabel != null){
				if ((ttestrst = ttesthm.get(ttestlabel))!=null){
					values = new HashMap<DSGeneMarker, Double>();
					for (int i = 0; i < view.markers().size(); i++) {
						DSGeneMarker m = view.markers().get(i);
						double pval = ttestrst.getSignificance(m);
						double tval = ttestrst.getTValue(m);
						values.put(m, Math.copySign(-Math.log10( pval ), tval));
					}
				}
			}else{
				try {
					TAnalysis tTestAnalysis= new TAnalysis(view);
					values = tTestAnalysis.calculateDisplayValues();
				} catch (TAnalysisException e1) {
					return new AlgorithmExecutionResults(false,
							e1.getMessage(), 
							null);
				}
			}
			if (values==null) return new AlgorithmExecutionResults(false,
					"The set of display values is set null.", 
					null);
			mraResultSet.setValues(values);
			
			sortByValue(values, mraResultSet);
		 
			//get TFs
			ArrayList<DSGeneMarker> transcriptionFactors=new ArrayList<DSGeneMarker>();
			StringTokenizer tfSt = new StringTokenizer(transcriptionFactorsStr, ", ");
			while (tfSt.hasMoreTokens()){
				String markerName = tfSt.nextToken();
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
			
			
			//get Signature Markers
			ArrayList<DSGeneMarker> signatureMarkers=new ArrayList<DSGeneMarker>();
			//genes with positive differential expression(t-value>=0)
			ArrayList<DSGeneMarker> posDE = new ArrayList<DSGeneMarker>();
			//genes with negative differential expression(t-value<0)
			ArrayList<DSGeneMarker> negDE = new ArrayList<DSGeneMarker>();
			if (ttestrst != null){
				for (DSGeneMarker marker : ttestrst.getSignificantMarkers()){
					signatureMarkers.add(marker);
					if (mraAnalysisPanel.twoFET()){
						if (mraResultSet.getValue(marker)>=0)	posDE.add(marker);
						else									negDE.add(marker);
					}
				}
			}else{
				StringTokenizer sigSt = new StringTokenizer(signatureMarkersSTr, ", ");
				while (sigSt.hasMoreTokens()){
					String markerName = sigSt.nextToken();
					try{
						DSGeneMarker marker = maSet.getMarkers().get(markerName);
						if (marker!=null){
							signatureMarkers.add(marker);
							if (mraAnalysisPanel.twoFET()){
								if (mraResultSet.getValue(marker)>=0)	posDE.add(marker);
								else									negDE.add(marker);
							}
						}
					}catch(Exception e){
						log.info("We can not find marker " + markerName + " in our MicroarraySet.");
					}
				}
			}
			log.info("We got "+signatureMarkers.size()+" signature markers");
	
			//y = size of significantMarkers		 
			int y = signatureMarkers.size();
			//x = size of markers
			int x = maSet.getMarkers().size();
			
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
				Set<DSGeneMarker> neighbors = adjMatrix.get(tfA);
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
				
				//calculate P-value by using Fishe's Exact test
				int a=w;
				int b=y-w;
				int c=z-w;
				int d=x-z-y+w;
				double pValue = FishersExactTest.getRightSideOneTailedP(a,b,c,d);
				//System.out.println(tfA.getLabel()+"\t"+ pValue +"\ta:"+a+"\tb:"+b+"\tc:"+c+"\td:"+d+"\tGenes in Regulon:"+z+"\tGenes in activated Targetlist:"+w);
	
				double pValue2 = 1.1;
				char mode = 0;
				if (mraAnalysisPanel.twoFET()){
					w = a2.size();
					a=w;
					b=y-w;
					c=z-w;
					d=x-z-y+w;
					pValue2 = FishersExactTest.getRightSideOneTailedP(a,b,c,d);
	
					//log.debug(tfA.getLabel()+"\t"+ pValue2 +"\ta:"+a+"\tb:"+b+"\tc:"+c+"\td:"+d+"\tGenes in Regulon:"+z+"\tGenes in repressed Targetlist:"+w);
	
					//show the most significant pValue of the two FET
					if (pValue <= pValue2){ //activator
						mode = CSMasterRegulatorResultSet.ACTIVATOR;
						//log.debug(pValue+" <= "+pValue2+" "+mode);
					}else {                 //repressor
						mode = CSMasterRegulatorResultSet.REPRESSOR;
						//log.debug(pValue+" > "+pValue2+" "+mode);
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
	
	private void sortByValue(final Map<DSGeneMarker, Double> values,
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
		for (int i = 0; i < genes.size(); i++){
			int rank = (i > 0 && values.get(genes.get(i)) == values.get(genes.get(i-1))) ?
					gene2rankMap.get(genes.get(i-1)) : i;
			gene2rankMap.put(genes.get(i), rank);
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
		StringBuffer histStr = new StringBuffer("Generated with MRA run with parameters:\n");	 
		histStr .append( "\n [PARA] Load Network " + mraAnalysisPanel.getSelectedAdjMatrix() + ":  " + mraAnalysisPanel.getAdjMatrixDataSet().getDataSetName()  ).append("\n");
		histStr .append( "[PARA] FET/GSEA p-value : " + mraAnalysisPanel.getPValue() ).append("\n");		
		histStr .append( "Master Regulators: " + mraAnalysisPanel.getTranscriptionFactor() ).append("\n");
		histStr .append( "Signature Markers: " + mraAnalysisPanel.getSigMarkers() ).append("\n\n\n");
		 
		histStr.append(generateHistoryForMaSetView(view, useMarkersFromSelector()));
		
		return histStr.toString();
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
		if (e.getMessage().equals(org.geworkbench.events.ProjectEvent.SELECTED)){
			DSDataSet dataSet = e.getDataSet();
			if (dataSet instanceof DSMicroarraySet) {
				this.mraAnalysisPanel.setMicroarraySet((DSMicroarraySet)dataSet);
			}else{
				this.mraAnalysisPanel.setMicroarraySet(null);
			}
		}

        ProjectSelection selection = ((ProjectPanel) source).getSelection();
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
	 
	@Override
	public String getAnalysisName() {
		return analysisName;
	}

	@Override
	protected Map<Serializable, Serializable> getBisonParameters() {
		Map<Serializable, Serializable> parameterMap = new HashMap<Serializable, Serializable>();
		if (mraAnalysisPanel.getResultid() != null){
			parameterMap.put("resultid", mraAnalysisPanel.getResultid());
			return parameterMap;
		}
		byte[] network = mraAnalysisPanel.getNetwork();
		if (network == null){
			parameterMap.put("network", null);
			return parameterMap;
		}
		parameterMap.put("mintg", mraAnalysisPanel.getMintg());
		parameterMap.put("minsp", mraAnalysisPanel.getMinsp());
		parameterMap.put("nperm", mraAnalysisPanel.getNperm());
		parameterMap.put("pvgsea", mraAnalysisPanel.getPValue());
		parameterMap.put("tail", mraAnalysisPanel.getTail());
		parameterMap.put("pvshadow", mraAnalysisPanel.getPVshadow());
		parameterMap.put("pvsynergy", mraAnalysisPanel.getPVsynergy());
		parameterMap.put("networkname", mraAnalysisPanel.getNetworkFilename());
		parameterMap.put("network", network);network=null;
		if (mraAnalysisPanel.allpos && mraAnalysisPanel.getTail()==2){
			JOptionPane.showMessageDialog(null, "Since all Spearman's correlation >= 0, gsea will use tail = 1.");
			parameterMap.put("tail", 1);
		}
		parameterMap.put("class1", mraAnalysisPanel.getIxClass(CSAnnotationContext.CLASS_CONTROL).toArray(new String[0]));
		parameterMap.put("class2", mraAnalysisPanel.getIxClass(CSAnnotationContext.CLASS_CASE).toArray(new String[0]));
		return parameterMap;
	}

	@Override
	public Class<?> getBisonReturnType() {
		return String.class;
	}

	@Override
	protected boolean useMicroarraySetView() {
		return true;
	}

	@Override
	protected boolean useOtherDataSet() {
		return false;
	}

	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet<?> refMASet) {
		if (maSetView == null)
			return new ParamValidationResults(false, "Invalid input.");
		assert maSetView instanceof DSMicroarraySetView;

		String runid = mraAnalysisPanel.getResultid();
		if (runid != null){
			if (!pattern.matcher(runid).find())
				return new ParamValidationResults(false, "Invalid MRA Result ID: "+runid);
			else
				return new ParamValidationResults(true, "No Error");
		}
		String vn = mraAnalysisPanel.validateNetwork();
		if (!vn.equals("Valid"))
			return new ParamValidationResults(false, "Invalid network: "+vn);
		
		if (mraAnalysisPanel.getMintg() <= 0)
			return new ParamValidationResults(false, "Min targets should be a positive integer.");
		if (mraAnalysisPanel.getMinsp() <= 0)
			return new ParamValidationResults(false, "Min samples should be a positive integer.");
		if (mraAnalysisPanel.getNperm() <= 0)
			return new ParamValidationResults(false, "Nperm should be a positive integer.");
		double pvgsea = mraAnalysisPanel.getPValue();
		if (pvgsea < 0 || pvgsea > 1)
			return new ParamValidationResults(false, "GSEA Pvalue should be between 0 and 1.");
		int tail = mraAnalysisPanel.getTail();
		if (tail != 1 && tail != 2)
			return new ParamValidationResults(false, "Tail should be 1 or 2.");
		double pvshadow = mraAnalysisPanel.getPVshadow();
		if (pvshadow < 0 || pvshadow > 1)
			return new ParamValidationResults(false, "Shadow Pvalue should be between 0 and 1.");
		double pvsynergy = mraAnalysisPanel.getPVsynergy();
		if (pvsynergy < 0 || pvsynergy > 1)
			return new ParamValidationResults(false, "Synergy Pvalue should be between 0 and 1.");
		HashSet<String> ctrls = mraAnalysisPanel.getIxClass(CSAnnotationContext.CLASS_CONTROL);
		if (ctrls.size() == 0)
			return new ParamValidationResults(false, "Please activate at least one control array.");
		Iterator<String> casei = mraAnalysisPanel.getIxClass(CSAnnotationContext.CLASS_CASE).iterator();
		if (!casei.hasNext()){
			int c = JOptionPane.showConfirmDialog(null, "Are you sure to use only a control group?");
			if (c != JOptionPane.YES_OPTION)
				return new ParamValidationResults(false, "Please activate both control and case.");
		}
		while (casei.hasNext()){
		    if (ctrls.contains(casei.next()))
			return new ParamValidationResults(false, "An array cannot be in case and control at the same time.");
		}
		
		return new ParamValidationResults(true, "No Error");
	}
}
