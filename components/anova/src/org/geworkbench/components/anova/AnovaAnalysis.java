package org.geworkbench.components.anova;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSAnovaResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSSignificanceResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSAnnotPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSAnnotatedPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.components.anova.gui.AnovaAnalysisPanel;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.SubpanelChangedEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.impl.OneWayANOVA;
import org.tigr.microarray.mev.cluster.gui.impl.owa.OneWayANOVAInitBox;
import org.tigr.util.FloatMatrix;

import edu.columbia.geworkbench.cagrid.anova.AnovaResult;
import edu.columbia.geworkbench.cagrid.anova.FalseDiscoveryRateControl;
import edu.columbia.geworkbench.cagrid.anova.PValueEstimation;
/**
 * @author yc2480
 * @version $id$
 */
public class AnovaAnalysis extends AbstractGridAnalysis implements ClusteringAnalysis{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int localAnalysisType;

    int[] groupAssignments; //used for MeV's ANOVA algorithm. [3 3 2 2 2 4 4 4 4] means first two microarrays belongs to same group, and microarray 3,4,5 belongs to same group, and last four microarrays belongs to same group.  
    double pvalueth=0.05; //p-value threshold. Fixme: this should get from user input, but we don't have that GUI in use case yet.
    String GroupAndMarkerString; //store text output used in dataset history. Will be refreshed each time execute() been called.
    
    private AnovaAnalysisPanel anovaAnalysisPanel=new AnovaAnalysisPanel();
    
    public AnovaAnalysis(){
        localAnalysisType = AbstractAnalysis.TTEST_TYPE;
        setLabel("Anova Analysis");
        setDefaultPanel(anovaAnalysisPanel);
    }
    
    public int getAnalysisType() {
        return localAnalysisType;
    }
    
    @SuppressWarnings("unchecked")
    public AlgorithmExecutionResults execute(Object input) {
        assert (input instanceof DSMicroarraySetView);
        DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
        DSMicroarraySet<DSMicroarray> maSet = view.getMicroarraySet();

        if (!isLogNormalized(maSet)){
        	Object[] options = {"Proceed",
            	"Cancel"};
        	int n = JOptionPane.showOptionDialog(anovaAnalysisPanel.getTopLevelAncestor(), //this make it shown in the center of our software 
        			"The input dataset must be log-transformed; please reenter log-transformed data to run this anlysis.\n\nClick proceed to override and continue the analysis with the input dataset selected.",
        			"Log Transformation",
        			JOptionPane.YES_NO_OPTION,
        			JOptionPane.QUESTION_MESSAGE,
        			null,     //do not use a custom Icon
        			options,  //the titles of buttons
        			options[0]); //default button title
        	if (n==1){ //n==1 means canceled 
        		return null;
        	}
        }
        
        // Get params
//        pvalueth=0.05; //p-value threshold 
        Set<String> labelSet = new HashSet<String>();

        DSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();
        DSAnnotationContext<DSMicroarray> context = manager.getCurrentContext(maSet);

        int numGroups=0;
        int numLabels=0;
        int numGenes=0;
        int numSelectedGroups=0;
        int numSelectedLabels=0;
        int numSelectedGenes=0;
        
        
        String[] selectedGroupLabels;
        GroupAndMarkerString=""; 
               
        int nl = context.getNumberOfLabels();
        numGroups=nl;
        for (int i = 0; i < nl; i++) {
            String label = context.getLabel(i);
            DSPanel<DSMicroarray> panelA = context.getItemsWithLabel(label);
            if (panelA.isActive()){
            	numSelectedGroups++;
            	labelSet.add(label);
            }
//            System.out.println(label);
        }

        numSelectedGroups=labelSet.size();
        if (numSelectedGroups < 2) {
            return new AlgorithmExecutionResults(false, "At least two panels must be selected for comparison.", null);
        }
        // todo - check that all selected panels have at least two elements
        String[] labels = labelSet.toArray(new String[numSelectedGroups]);
        String[] labels1 = new String[0];
        numGenes = view.markers().size();
//System.out.println("NumGenes:"+numGenes);        
        // Create panels and significant result sets to store results
        DSSignificanceResultSet<DSGeneMarker> sigSet = new CSSignificanceResultSet<DSGeneMarker>(
                maSet,
                "Anova Analysis",
                labels1,
                labels,
                pvalueth
        );
        CSAnovaResultSet anovaResultSet=new CSAnovaResultSet(view, "Anova Analysis Result Set" , labels);
        
        // todo - use a F-test to filter genes prior to finding significant genes with Holm t Test
        // Run tests

        AlgorithmData data = new AlgorithmData();
    	
        // FIXME - this is just a quick fix about the "Selection" Panel, should check how many been selected.
        	int globleArrayIndex=0; //use as an index points to all microarrays put in array A
//        	int MicroarrayNum=view.getMicroarraySet().size();
        	
        	//calculating how many groups selected and arrays inside selected groups
            for (int i = 0; i < numSelectedGroups; i++) {//for each groups
                String labelA = labels[i];
                DSPanel<DSMicroarray> panelA = context.getItemsWithLabel(labelA);
                //put group label into history
                GroupAndMarkerString+=labelA+"\n";
                
                if (panelA.isActive()){
                    int aSize = panelA.size();
                    for (int aIndex = 0; aIndex < aSize; aIndex++) {		//for each array in this group
                    	GroupAndMarkerString+="\t"+panelA.get(aIndex)+"\n";	//put member of each group into history
                    	globleArrayIndex++; 								//count total arrays in selected groups.
                    }
                }
            }
        	numSelectedLabels=globleArrayIndex;
            //fill microarray view data into array A, and assign groups 
        	int[] groupAssignments=new int[globleArrayIndex];
        	float[][] A=new float[numGenes][globleArrayIndex];
        	globleArrayIndex=0;
            for (int i = 0; i < numSelectedGroups; i++) {//for each groups
                String labelA = labels[i];
                DSPanel<DSMicroarray> panelA = context.getItemsWithLabel(labelA);
                int aSize = panelA.size();
                for (int aIndex = 0; aIndex < aSize; aIndex++) {//for each array in this group
                	for (int k = 0; k < numGenes; k++) {//for each marker
                        A[k][globleArrayIndex]=(float)panelA.get(aIndex).getMarkerValue(k).getValue();
//                        System.out.println(labelA+Integer.toString(i)+","+Integer.toString(k)+"+"+Integer.toString(aIndex));
                    }
                    groupAssignments[globleArrayIndex]=i+1;
                	globleArrayIndex++;
                }
            }

            //call MeV's interface using their protocols
	        FloatMatrix FM=new FloatMatrix(A);
        	
            data.addMatrix("experiment", FM);
            data.addIntArray("group-assignments", groupAssignments);
            data.addParam("numGroups", String.valueOf(numSelectedGroups));

            data.addParam("usePerms",String.valueOf(anovaAnalysisPanel.anovaParameter.getPValueEstimation()==PValueEstimation.permutation));
//            System.out.println("usePerms:"+String.valueOf(anovaAnalysisPanel.anovaParameter.getPValueEstimation()==PValueEstimation.permutation));

            if (anovaAnalysisPanel.anovaParameter.getPValueEstimation()==PValueEstimation.fdistribution){

            }else if (anovaAnalysisPanel.anovaParameter.getPValueEstimation()==PValueEstimation.permutation){
                data.addParam("numPerms",String.valueOf(anovaAnalysisPanel.anovaParameter.getPermutationsNumber()));
//                System.out.println("numPerms:"+String.valueOf(anovaAnalysisPanel.anovaParameter.getPermutationsNumber()));
                if (anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.number){
                	data.addParam("falseNum",String.valueOf((new Float(anovaAnalysisPanel.anovaParameter.getFalseSignificantGenesLimit())).intValue()));
                }else if(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.proportion){
                	data.addParam("falseProp",String.valueOf(anovaAnalysisPanel.anovaParameter.getFalseSignificantGenesLimit()));
                }else{
                	System.out.println("This shouldn't happen! FalseDiscoveryRateControl should be either number or proportion");
                }            	
            }else{
            	System.out.println("This shouldn't happen! I don't understand that PValueEstimation");
            }
            
//            System.out.println(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl());
            if (anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.adjbonferroni){ 
                data.addParam("correction-method",String.valueOf(OneWayANOVAInitBox.ADJ_BONFERRONI));
            }else if(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.bonferroni){
            	data.addParam("correction-method",String.valueOf(OneWayANOVAInitBox.STD_BONFERRONI));
            }else if(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.alpha){
            	data.addParam("correction-method",String.valueOf(OneWayANOVAInitBox.JUST_ALPHA));
            }else if(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.westfallyoung){
            	System.out.println("don't know what to do with WestFallYoung yet.");
            	data.addParam("correction-method",String.valueOf(OneWayANOVAInitBox.MAX_T));
            }else if(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.number){
            	//it seems if it use false discovery control, it disregards false discovery rate
            	System.out.println("don't know what to do with FDC yet.");
            	data.addParam("correction-method",String.valueOf(OneWayANOVAInitBox.FALSE_NUM));
            }else if(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.proportion){
            	System.out.println("don't know what to do with FDC yet.");
            	data.addParam("correction-method",String.valueOf(OneWayANOVAInitBox.FALSE_PROP));
            }else{
            	System.out.println("This shouldn't happen! I don't understand that selection. It should be one of following: Alpha, Boferroni, Adj-Bonferroni, WestfallYoung, FalseNum, FalseProp.");
            }
            
            AnovaResult anovaResult=new AnovaResult();	//TODO: I use AnovaResult for now, was designed for grid, not local version, I think we'll also need a local version data type.
            
            OneWayANOVA OWA=new OneWayANOVA();
	        try{
	            AlgorithmData result=OWA.execute(data);
	            //get p-values in result
		        FloatMatrix pFM=result.getMatrix("rawPValues");
	            FloatMatrix apFM=result.getMatrix("adjPValues");
	            FloatMatrix fFM=result.getMatrix("fValues");
	            FloatMatrix mFM=result.getMatrix("geneGroupMeansMatrix");
	            FloatMatrix sFM=result.getMatrix("geneGroupSDsMatrix");

	            //I need to know how many will pass the threshold to initialize the array
	            int significantMarkerIndex=0;
	            for (int i = 0; i < pFM.getRowDimension(); i++) {
	            	if (pFM.A[i][0]<pvalueth){
	            		significantMarkerIndex++;
	            	};
	            };
	            int totalSignificantMarkerNum=significantMarkerIndex;

	            String[] significantMarkerNames=new String[totalSignificantMarkerNum];
	            significantMarkerIndex=0;
	            for (int i = 0; i < pFM.getRowDimension(); i++) {
	            	if (pFM.A[i][0]<pvalueth){
	            		significantMarkerNames[significantMarkerIndex]=view.markers().get(i).getLabel();	            	
	            		significantMarkerIndex++;
	            	};
	            };

	            //output f-value, p-value, adj-p-value, mean, std
	            anovaResult.setSignificantMarkerNames(significantMarkerNames);
	            anovaResult.setPVals(new float[totalSignificantMarkerNum]);
	            anovaResult.setAllMeans(new float[totalSignificantMarkerNum*mFM.getColumnDimension()]);
	            anovaResult.setAllStds(new float[totalSignificantMarkerNum*mFM.getColumnDimension()]);
	            anovaResult.setPVals(new float[totalSignificantMarkerNum]);
	            anovaResult.setAdjPVals(new float[totalSignificantMarkerNum]);
	            anovaResult.setFVals(new float[totalSignificantMarkerNum]);
	            anovaResult.setGroupNames(labels);
	            DSAnnotatedPanel<DSGeneMarker, Float> panelSignificant = new CSAnnotPanel<DSGeneMarker, Float>("Significant Genes");
	            //FIXME: only put means&stds where pvalue<pvalueth
	            
	            
	            significantMarkerIndex=0;
	            for (int i = 0; i < pFM.getRowDimension(); i++) {
	            	if (pFM.A[i][0]<pvalueth){
	            		DSGeneMarker item = view.markers().get(view.markers().get(i).getLabel());
	            		//if you don't want the whole label, you can use getShortName() to only get it's id.
	            		//System.out.println(view.markers().get(i).getShortName());
	            		panelSignificant.add(item, new Float(apFM.A[i][0]));
	            		sigSet.setSignificance(item, (double)apFM.A[i][0]);
	            		anovaResult.setPVals(significantMarkerIndex, pFM.A[i][0]);
	            		anovaResult.setAdjPVals(significantMarkerIndex, apFM.A[i][0]);
	            		anovaResult.setFVals(significantMarkerIndex, fFM.A[i][0]);
		            	for (int j=0; j<mFM.getColumnDimension();j++){
//		            		System.out.print("\tmean:G"+j+":"+new Float(mFM.A[i][j]));
//		            		System.out.print("±"+new Float(sFM.A[i][j]));
		            		anovaResult.setAllMeans(j*totalSignificantMarkerNum+significantMarkerIndex, mFM.A[i][j]);
		            		anovaResult.setAllStds(j*totalSignificantMarkerNum+significantMarkerIndex, sFM.A[i][j]);
		            	}
		            	significantMarkerIndex++;
//		            	System.out.print(view.markers().get(i).getLabel()+"\tp-value: "+new Float(pFM.A[i][0])+"\tadj-p-value: "+new Float(apFM.A[i][0])+"\tf-value: "+new Float(fFM.A[i][0]));
	            	}
//	            	System.out.print(view.markers().get(i).getLabel()+"\tp-value: "+new Float(pFM.A[i][0])+"\tadj-p-value: "+new Float(apFM.A[i][0])+"\tf-value: "+new Float(fFM.A[i][0]));	            	
//	            	System.out.println();
	            }
	            publishSubpanelChangedEvent(new SubpanelChangedEvent(DSGeneMarker.class, panelSignificant, SubpanelChangedEvent.NEW));
//	        	System.out.println(result.toString());
	        }catch (AlgorithmException AE){
	        	AE.printStackTrace();
	        }
	        //add to Dataset History
            ProjectPanel.addToHistory(sigSet, generateHistoryString(data));
            
//	        sigSet.sortMarkersBySignificance();
	        //AlgorithmExecutionResults results = new AlgorithmExecutionResults(true, "Anova Analysis", sigSet);
	        anovaResultSet=new CSAnovaResultSet(view, "Anova Analysis Result Set" , labels, anovaResult.getSignificantMarkerNames(), anovaResult2result2DArray(anovaResult));
	        anovaResultSet.getSignificantMarkers().addAll(sigSet.getSignificantMarkers());
	        anovaResultSet.sortMarkersBySignificance();
//    		anovaResultSet.setSignificance(item, (double)pFM.A[i][0]);
//	        anovaResultSet.sortMarkersBySignificance();
	        
	        //add to Dataset History
            ProjectPanel.addToHistory(anovaResultSet, generateHistoryString(data));
            
	        AlgorithmExecutionResults results = new AlgorithmExecutionResults(true, "Anova Analysis", anovaResultSet);
        return results;
    }
    
    private double[][] anovaResult2result2DArray(AnovaResult anovaResult){
        int arrayHeight=anovaResult.getPVals().length;
        int arrayWidth=anovaResult.getGroupNames().length*2+3; //each group needs two columns, plus pval, adjpval and fval.
        double[][] result2DArray=new double[arrayWidth][arrayHeight];
        //fill p-values
        for (int cx=0; cx<arrayHeight; cx++){
        	result2DArray[0][cx]=anovaResult.getPVals()[cx];
        }
        //fill adj-p-values
        for (int cx=0; cx<arrayHeight; cx++){
        	result2DArray[1][cx]=anovaResult.getAdjPVals()[cx];
        }
        //fill f-values
        for (int cx=0; cx<arrayHeight; cx++){
        	result2DArray[2][cx]=anovaResult.getFVals()[cx];
        }
        //fill means
        for (int cx=0; cx<arrayHeight; cx++){
        	for (int cy=0; cy<anovaResult.getGroupNames().length; cy++){
        		result2DArray[3+cy*2][cx]=anovaResult.getAllMeans()[cy*arrayHeight+cx];
        	}
        }
        //fill stds
        for (int cx=0; cx<arrayHeight; cx++){
        	for (int cy=0; cy<anovaResult.getGroupNames().length; cy++){
        		result2DArray[4+cy*2][cx]=anovaResult.getAllStds()[cy*arrayHeight+cx];
        	}
        }
    	
    	return result2DArray;
    }
    
    @Publish
    public org.geworkbench.events.SubpanelChangedEvent publishSubpanelChangedEvent
            (org.geworkbench.events.SubpanelChangedEvent
                    event) {
        return event;
    }
    private String generateHistoryString(AlgorithmData data){
    	String histStr="";
    	//Header
    	histStr+="Generated with ANOVA run with parameters:\n";
    	histStr+="----------------------------------------\n";
    	//P Value Estimation
    	histStr+="P Value estimation: ";
    	if (anovaAnalysisPanel.anovaParameter.getPValueEstimation()==PValueEstimation.permutation){
    		histStr+="Permutation\n";
    	}else{
    		histStr+="F-Distribution\n";
    	}
    	//P Value threshold
    	histStr+="P Value threshold: ";
    	histStr+=pvalueth+"\n";
    		
    	//Correction type
    	histStr+="correction-method: ";
    	histStr+=anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl().toString()+"\n";
/* you can change line above to human readable version below
        if (anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.adjbonferroni){ 
        	histStr+="ADJ_BONFERRONI";
        }else if(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.bonferroni){
        	histStr+="STD_BONFERRONI";
        }else if(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.alpha){
        	histStr+="JUST_ALPHA";
        }else if(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.westfallyoung){
        	histStr+="WestFallYoung";
        }else if(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.number){
        	histStr+="FALSE_NUM";
        }else if(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.proportion){
        	histStr+="FALSE_PROP";
        }else{
        	System.out.println("This shouldn't happen! I don't understand that selection. It should be one of following: Alpha, Boferroni, Adj-Bonferroni, WestfallYoung, FalseNum, FalseProp.");
        }
    	histStr+="\n";
end of human readable version*/    	
    	
    	//group names and markers
    	histStr+=GroupAndMarkerString;
    	
    	return histStr;
    }
    private boolean isLogNormalized(DSMicroarraySet<DSMicroarray> set) {
        double minValue = Double.POSITIVE_INFINITY;
        double maxValue = Double.NEGATIVE_INFINITY;
        for (DSMicroarray microarray : set) {
            DSMutableMarkerValue[] values = microarray.getMarkerValues();
            double v;
            for (DSMutableMarkerValue value : values) {
                v = value.getValue();
                if (v < minValue) {
                    minValue = v;
                }
                if (v > maxValue) {
                    maxValue = v;
                }
            }
        }
        return((maxValue - minValue) < 100); //if the range of the values is small enough, we guess it's lognormalized. 
    }

	@Override
	public String getAnalysisName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Serializable, Serializable> getBisonParameters() {
		// TODO Auto-generated method stub
		Map<Serializable, Serializable> parameterMap = new HashMap<Serializable, Serializable>();
		//put every parameters you need for execute(String encodedInput) in AnovaClient.java
		//microarray data already been added before these parameters in AnalysisPanel.java
		//I'll need to put group information and anovaParameters.
		//parameterMap.put(key, value);
		return parameterMap;
	}

	@Override
	public Class getBisonReturnType() {
		// TODO Auto-generated method stub
		return CSAnovaResultSet.class;
	}
}
