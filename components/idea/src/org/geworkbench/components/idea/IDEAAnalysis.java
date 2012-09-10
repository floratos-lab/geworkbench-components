package org.geworkbench.components.idea;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.MathException;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.IdeaGLoc;
import org.geworkbench.bison.datastructure.bioobjects.IdeaModule;
import org.geworkbench.bison.datastructure.bioobjects.IdeaNode;
import org.geworkbench.bison.datastructure.bioobjects.IdeaResultDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
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
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.util.FishersExactTest;
import org.geworkbench.util.ProgressBar;

/**
 * IDEAAnalysis of IDEA analysis component
 * 
 * @author zm2165
 * @version $Id$
 */
public class IDEAAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {
	private static final long serialVersionUID = 7928879302023716304L;

	private static Log log = LogFactory.getLog(IDEAAnalysis.class);

	private IDEAPanel IDEAAnalysisPanel = new IDEAPanel();	
	
	private String[] nullDataAsString;
	
	private static final String PROBE_SET="Probe Set ID";
	private static final String GENE_SYMBOL="Gene Symbol";
	private static final String CHROMO_LOCATION="Chromosomal Location";
	private static final String ENTREZ_GENE="Entrez Gene";	
	private ProgressBar pbIdea = null;
	private ProgressBar pbPrepare = null;
	
	public IDEAAnalysis() {
		setDefaultPanel(IDEAAnalysisPanel);
	}

	@SuppressWarnings("unchecked")
	public AlgorithmExecutionResults execute(Object input) {

		// read input data, dataset view, dataset, etc.
		if (!(input instanceof DSMicroarraySetView)) {
			return new AlgorithmExecutionResults(false,
					"Input dataset for IDEA analysis should be a MicroarraySet.\n"
							+ "But you selected a "
							+ input.getClass().getName(), null);
		}

		double pvalue;
		try{
			pvalue=Double.parseDouble(IDEAAnalysisPanel.getPvalue());
			if((pvalue<=0)||(pvalue>=1))
				return new AlgorithmExecutionResults(false,
						"P-value should be between 0 and 1.", null);
		}
		catch (Exception e){
			return new AlgorithmExecutionResults(false,
					"P-value is invalid.", null);
		}
		
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> datasetView = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;	
		DSMicroarraySet maSet = datasetView.getMicroarraySet();
		if(maSet.getAnnotationFileName()==null){
			return new AlgorithmExecutionResults(false,
					"IDEA analysis needs annotation file. Please load it first.", null);
		}		
		int numGenes = maSet.getMarkers().size();
		
		pbIdea = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
		pbIdea.addObserver(this);
		pbIdea.setTitle("IDEA Analysis");
		pbIdea.setBounds(new ProgressBar.IncrementModel(0, numGenes, 0,
				numGenes, 1));

		pbIdea.setMessage("Calculating IDEA, please wait...");
		pbIdea.start();
		this.stopAlgorithm = false;

		HashSet<IdeaEdge> edgeIndex = new HashSet<IdeaEdge>();

		List<IdeaNetworkEdge> network = IDEAAnalysisPanel.getNetwork();
		if (network == null){			
			pbIdea.dispose();			
			return new AlgorithmExecutionResults(false,
					"network file is invalid.", null);
		}

		Map<Integer, List<DSGeneMarker>> preGeneList = new HashMap<Integer, List<DSGeneMarker>>();
		for (IdeaNetworkEdge edge : network) {
			preGeneList.put(edge.getGene1(), new ArrayList<DSGeneMarker>());
			preGeneList.put(edge.getGene2(), new ArrayList<DSGeneMarker>());
		}
		
		for (DSGeneMarker marker : datasetView.markers()) {
			int id = marker.getGeneId();

			List<DSGeneMarker> markers = preGeneList.get(id);
			if (markers != null) {
				markers.add(marker);
			}
		}

		if (this.stopAlgorithm) {
			pbIdea.dispose();
			return null;
		}

		for (IdeaNetworkEdge edge : network) {

			List<DSGeneMarker> markers1 = preGeneList.get(edge.getGene1());
			List<DSGeneMarker> markers2 = preGeneList.get(edge.getGene2());

			if ((markers1 != null) && (markers2 != null)) {
				for (DSGeneMarker marker1 : markers1) {
					for (DSGeneMarker marker2 : markers2) {

						IdeaEdge anEdge = new IdeaEdge(marker1, marker2,
								edge.getInteractionType());
						edgeIndex.add(anEdge);
					}
				}
			}
		}
		
		if(edgeIndex.size()==0){
			pbIdea.dispose();
			return new AlgorithmExecutionResults(false,
					"No overlap between network edges and data set, analysis aborted!", null);
		}
		
		Phenotype phenotype = IDEAAnalysisPanel.getPhenotype(); // never null
		Set<Integer> includeSet=IDEAAnalysisPanel.preparePhenoSet(IDEAAnalysisPanel.getIncludeString());
		phenotype.setIncludeList(includeSet);
		Set<Integer> excludeSet=IDEAAnalysisPanel.preparePhenoSet(IDEAAnalysisPanel.getExcludeString());
		phenotype.setExcludeList(excludeSet);
		if (IDEAAnalysisPanel.getIncludeString().equals("")) {
			pbIdea.dispose();
			return new AlgorithmExecutionResults(false,
					"phenotype data is invalid.", null);
		}
		
		if (this.stopAlgorithm) {
			pbIdea.dispose();
			return null;
		}

		try {
			/* ============= key process =========== */
			NullDistribution nullDist = new NullDistribution(this);
			boolean finished = nullDist.calcNullDist(maSet, edgeIndex, pvalue,
					phenotype);
			if (!finished || this.stopAlgorithm) {
				pbIdea.dispose();
				return null;
			}
		} catch (MathException e) {
			e.printStackTrace();
			pbIdea.dispose();
			log.error(e);
			return new AlgorithmExecutionResults(false,
					"IDEA calculation failed due to MathException", null);
		} catch (IOException e) {
			e.printStackTrace();
			pbIdea.dispose();
			log.error(e);
			return new AlgorithmExecutionResults(false,
					"IDEA calculation failed due to IOException", null);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			pbIdea.dispose();
			log.error(e);
			return new AlgorithmExecutionResults(false,
					"IDEA calculation failed due to ClassNotFoundException",
					null);
		} catch (Exception e) {
			e.printStackTrace();
			pbIdea.dispose();
			log.error(e);
			return new AlgorithmExecutionResults(false,
					"IDEA calculation failed due to Exception", null);
		} 
		
		
		if (this.stopAlgorithm) {
			pbIdea.dispose();
			return null;
		}

		List<IdeaEdge> locList = new ArrayList<IdeaEdge>();
		List<IdeaEdge> gocList = new ArrayList<IdeaEdge>();
		for (IdeaEdge anEdge : edgeIndex) {
			if (anEdge.isLoc())
				locList.add(anEdge);
			else if (anEdge.isGoc())
				gocList.add(anEdge);
		}

		Collections.sort(locList);

		Collections.sort(gocList, Collections.reverseOrder());

		TreeSet<IdeaProbeGene> probes = new TreeSet<IdeaProbeGene>();
		/*
		 * process probes, which is a alternative way to evaluate genes other
		 * than entrez genes which I call gene in this code
		 */
		for (IdeaEdge e : edgeIndex) {
			IdeaProbeGene p1 = new IdeaProbeGene(e.getProbeId1());
			IdeaProbeGene p2 = new IdeaProbeGene(e.getProbeId2());
			probes.add(p1);
			probes.add(p2);
		}

		for (IdeaProbeGene p : probes) {
			p.setEdges(edgeIndex);
		}

		if (this.stopAlgorithm) {
			pbIdea.dispose();
			return null;
		}
		
		int N = edgeIndex.size();
		FishersExactTest fe = new FishersExactTest(2 * N);

		int Sl = locList.size();
		int Sg = gocList.size();

		// calculate LOC p-value using fisher exact test to evaluate entrez
		try{
			for (IdeaProbeGene p : probes) {
				int H = p.getEdgeCount();
				// default values are 1
				double cumLoc = 1;
				double cumGoc = 1;
				if (p.getLocs() > 0) { // calculate LOC p-value using fisher
										// exact test to evaluate probe
					int Dl = p.getLocs();
					cumLoc = fe.calculateRightSideOneTailedP(Dl, H - Dl, Sl - Dl, N
							- Sl - H + Dl);
				}
				if (p.getGocs() > 0) {
					int Dg = p.getGocs();
					cumGoc = fe.calculateRightSideOneTailedP(Dg, H - Dg, Sg - Dg, N
							- Sg - H + Dg);
				}
				p.updateNes(cumLoc, cumGoc);
			}
		}
		catch(Exception e){
			e.printStackTrace();
			pbIdea.dispose();
			log.error(e);			
			return new AlgorithmExecutionResults(false,
					"IDEA calculation failed due to exception:" +
					"\nfinding significant genes using Fisher Exact Test.",
					null);
		}
		
		List<IdeaProbeGene> probeNes = new ArrayList<IdeaProbeGene>();
		for (IdeaProbeGene p : probes) {
			probeNes.add(p);
		}
		Collections.sort(probeNes, new IdeaProbeGene.NesComparator());

		if (this.stopAlgorithm) {
			pbIdea.dispose();
			return null;
		}
		
		//prepare locResultList,gocResultList,nodeResultList
		//locList->locResultList		
		List<IdeaGLoc> locResultList=new ArrayList<IdeaGLoc>();
		for(IdeaEdge e:locList){
			IdeaGLoc anItem=new IdeaGLoc(e.getProbeId1(),e.getMarker1().getGeneName(),
					e.getProbeId2(), e.getMarker2().getGeneName(),
					e.getMI(), e.getDeltaCorr(), e.getzDeltaCorr() );
			locResultList.add(anItem);
		}
		//gocList->gocResultList
		List<IdeaGLoc> gocResultList=new ArrayList<IdeaGLoc>();
		for(IdeaEdge e:gocList){
			IdeaGLoc anItem=new IdeaGLoc(e.getProbeId1(),e.getMarker1().getGeneName(),
					e.getProbeId2(), e.getMarker2().getGeneName(),
					e.getMI(), e.getDeltaCorr(), e.getzDeltaCorr() );
			gocResultList.add(anItem);
		}
		//probeNes->nodeResultList
		List<IdeaNode> nodeResultList=new ArrayList<IdeaNode>();
		DSItemList<DSGeneMarker> markers = maSet.getMarkers();
		for (IdeaProbeGene pg : probeNes) {
			if (pg.isSignificant()) // FIXME inconsistent with remote result
			{
				nodeResultList.add(pg.getIdeaNode(markers));
			}
		}
		
		//prepare moduleResultList
		List<IdeaModule> moduleResultList = new ArrayList<IdeaModule>();
		for (IdeaProbeGene p : probeNes) {// present significant node with its
			// edges
			if (p.isSignificant(pvalue)) {
				moduleResultList.addAll(p.getModuleList());
			}
		}
		if (this.stopAlgorithm) {
			pbIdea.dispose();
			return null;
		}		
		IdeaResultDataSet analysisResult = new IdeaResultDataSet(maSet,
				"IDEA Analysis Result",locResultList, gocResultList, nodeResultList, moduleResultList,pvalue);
		String stemp = generateHistoryString()+"\n";
		HistoryPanel.addToHistory(analysisResult, stemp+ this.generateHistoryForMaSetView(datasetView));

		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"Idea Analysis", analysisResult);
		pbIdea.dispose();
		return results;
	}

	private String generateHistoryString() {
		StringBuffer histStr = new StringBuffer();
		histStr.append(IDEAAnalysisPanel.getDataSetHistory());
		return histStr.toString();
	}

	
	public void stop(){
		if (pbIdea!=null)
			pbIdea.dispose();
		if (pbPrepare!=null)
			pbPrepare.dispose();
	}

	@Override
	public String getAnalysisName() {
		return "Idea";
	}

	@Override
	protected Map<Serializable, Serializable> getBisonParameters() {
		
		Map<Serializable, Serializable> bisonParameters = new HashMap<Serializable, Serializable>();
		IDEAPanel paramPanel = (IDEAPanel) this.aspp;
		float pvalue=Float.parseFloat(paramPanel.getPvalue());
		bisonParameters.put("pvalue", pvalue);				
		String[] phenotype= paramPanel.getPhenotypeAsString();
		bisonParameters.put("phenotype", phenotype);
		String[] network= paramPanel.getNetworkAsString();
		bisonParameters.put("network", network);
		
		String[] nullData= nullDataAsString;
		bisonParameters.put("nullData", nullData);
		
		return bisonParameters;
	}

	@Override
	public Class<?> getBisonReturnType() {
		return IdeaResultDataSet.class;
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
		if (aspp == null)
			return new ParamValidationResults(true, null);
		// Use this to get params
		IDEAPanel params = (IDEAPanel) aspp;
		Phenotype phenotype = params.getPhenotype();		
		
		if ((params.getIncludeString().equals(""))||(params.getIncludeString().equals(" "))) {
			return new ParamValidationResults(false,
					"Arrays defining phenotype are required.");
		}
		double pvalue;
		try{
			pvalue=Double.parseDouble(IDEAAnalysisPanel.getPvalue());
			if((pvalue<=0)||(pvalue>=1))
				return new ParamValidationResults(false,
						"P-value should be between 0 and 1.");
		}
		catch (Exception e){
			return new ParamValidationResults(false,
					"P-value is invalid.");
		}
		
		Set<Integer> includeSet=params.preparePhenoSet(params.getIncludeString());
		phenotype.setIncludeList(includeSet);
		Set<Integer> excludeSet=params.preparePhenoSet(params.getExcludeString());
		phenotype.setExcludeList(excludeSet);
		
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = maSetView;
		DSMicroarraySet maSet = view.getMicroarraySet();
		if(maSet.getAnnotationFileName()==null){
			return new ParamValidationResults(false,
					"IDEA analysis needs annotation file. Please load it first.");
		}
		
		pbPrepare = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
		pbPrepare.addObserver(this);
		pbPrepare.setTitle("Data Preparation");		
		pbPrepare.setMessage("Preparing data for calculation...");
		pbPrepare.start();
		
		this.stopAlgorithm = false;
		
		AdjacencyMatrixDataSet selectedAdjSet=IDEAAnalysisPanel.getSelectedAdjSet();
		if ((selectedAdjSet!=null)&&(IDEAAnalysisPanel.getSelectedAdjMatrix()!=" ")){
		  if(!IDEAAnalysisPanel.getNetworkFromProject(selectedAdjSet)){
			  pbPrepare.dispose();
			  return new ParamValidationResults(false,
				"Preparing data aborted.");
		  }
		}
		
		if (params.getNetwork()== null) {
			pbPrepare.dispose();
			return new ParamValidationResults(false,
					"Please load networks.");
		}		
		
		String annoFile=maSet.getAnnotationFileName();
		ArrayList<String> nullDataList;		
			FileReader filereader;				
			try {
				filereader = new FileReader(annoFile);			
				Scanner in = new Scanner(filereader);
				nullDataList =new ArrayList<String>();
				int probeSetCol=0;				
				int geneSymbolCol=14;
				int chromoCol=15;
				int entrezCol=18;
				boolean headLineProcessed=false;
				while (in.hasNextLine()) {
					String line = in.nextLine();
					char firstChar=line.charAt(0);	//the following lines parsing the annotation file
					if(!Character.toString(firstChar).equals("#")){//remove the line begin with #
						String[] tokens = line.split("\",\"");
						//only column ProbesetId,GeneSymbol,Chromosomal,EntrezGene are picked up
						if(!headLineProcessed){
							if(tokens[0].indexOf(PROBE_SET)!=-1){//means the head line without comments
								for(int i=0;i<tokens.length;i++){								
									if(tokens[i].equalsIgnoreCase(GENE_SYMBOL))
										geneSymbolCol=i;
									if(tokens[i].equalsIgnoreCase(CHROMO_LOCATION))
										chromoCol=i;
									if(tokens[i].equalsIgnoreCase(ENTREZ_GENE))
										entrezCol=i;								
								}
								headLineProcessed=true;
							}
						}
						String[] token0s=tokens[probeSetCol].split("\"");
						String oneLine=token0s[1]+"\t"+tokens[geneSymbolCol]+"\t"+tokens[chromoCol]+"\t"+tokens[entrezCol]+"\t";
						nullDataList.add(oneLine);
					}
					if (this.stopAlgorithm) {
						pbPrepare.dispose();
						return new ParamValidationResults(false,
						"Analysis aborted!");
					}	
					
				}
				
				nullDataAsString=new String[nullDataList.size()];
				int l=0;
				for(String s:nullDataList){
					nullDataAsString[l]=s+"\n";
					l++;
				}
				
			} catch (FileNotFoundException e1) {
				pbPrepare.dispose();
				return new ParamValidationResults(false,
						"Annotation file is not valid. Please load it first.");
			}		
		pbPrepare.dispose();
		return new ParamValidationResults(true, "No, no Error");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Subscribe
	public void receive(org.geworkbench.events.PhenotypeSelectorEvent e,
			Object source) {		

		if (e.getTaggedItemSetTree() != null) {
			DSPanel<DSMicroarray> activatedArrays = e.getTaggedItemSetTree();
			((IDEAPanel) aspp).setSelectorPanelForArray(activatedArrays);
		}else
			log.debug("IDEA Received Microarray Selector Event: Selection panel sent was null");		

	}

	@SuppressWarnings("rawtypes")
	@Subscribe
	public void receive(org.geworkbench.events.ProjectNodeRemovedEvent e,
			Object source) {
		DSDataSet dataSet = e.getAncillaryDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			this.IDEAAnalysisPanel
					.removeAdjMatrixToCombobox((AdjacencyMatrixDataSet) dataSet);
		}
	}
	

	@SuppressWarnings("rawtypes")
	@Subscribe
	public void receive(org.geworkbench.events.ProjectNodeRenamedEvent e,
			Object source) {
		DSDataSet dataSet = e.getDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			this.IDEAAnalysisPanel
					.renameAdjMatrixToCombobox((AdjacencyMatrixDataSet)dataSet, e.getOldName(),e.getNewName());
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
		if (e.getValue()==org.geworkbench.events.ProjectEvent.Message.SELECT){
			DSDataSet dataSet = e.getDataSet();
			if (dataSet instanceof DSMicroarraySet) {
				this.IDEAAnalysisPanel.setMicroarraySet((DSMicroarraySet)dataSet);
			}else{
				this.IDEAAnalysisPanel.setMicroarraySet(null);
			}
		}

        ProjectSelection selection = ((ProjectPanel) source).getSelection();
        DataSetNode dNode = selection.getSelectedDataSetNode();
        if(dNode == null){
        	return;
        }
       
        String currentTargetSet = this.IDEAAnalysisPanel.getSelectedAdjMatrix();
        this.IDEAAnalysisPanel.clearAdjMatrixCombobox();
        Enumeration children = dNode.children();
        while (children.hasMoreElements()) {
            Object obj = children.nextElement();
            if (obj instanceof DataSetSubNode) {
                DSAncillaryDataSet ads = ((DataSetSubNode) obj)._aDataSet;
                if (ads instanceof AdjacencyMatrixDataSet) {
                    this.IDEAAnalysisPanel.addAdjMatrixToCombobox((AdjacencyMatrixDataSet) ads);                    
                    if (currentTargetSet != null && StringUtils.equals(ads.getDataSetName(), currentTargetSet.trim())) {
                    	IDEAAnalysisPanel.setSelectedAdjMatrix(ads.getDataSetName());
        			}
                }
            }
        }
	}
	
}
