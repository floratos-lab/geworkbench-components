package org.geworkbench.components.idea;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.IdeaResultDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.builtin.projects.DataSetNode;
import org.geworkbench.builtin.projects.DataSetSubNode;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.engine.management.Subscribe;
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

	@Override
	public AlgorithmExecutionResults execute(Object input) {		// inform the user that only remote service is available
		return new AlgorithmExecutionResults(
				false,
				"The local service for IDEA is not available. Please select the grid version in the Services tab",
				null);
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
		DSDataSet dataSet = e.getDataSet();
		if (dataSet instanceof DSMicroarraySet) {
			this.IDEAAnalysisPanel.setMicroarraySet((DSMicroarraySet) dataSet);
		} else {
			this.IDEAAnalysisPanel.setMicroarraySet(null);
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
