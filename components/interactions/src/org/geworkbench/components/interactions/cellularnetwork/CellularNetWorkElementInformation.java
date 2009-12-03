package org.geworkbench.components.interactions.cellularnetwork;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GOTerm;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.GeneOntologyUtil;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.List;  
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: xiaoqing
 * Date: Jan 5, 2007
 * Time: 12:31:48 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * It is used to save all celllualr Network information related to a specific marker.
 *
 */
public class CellularNetWorkElementInformation {
    private HashMap<String,Integer> interactionNumMap = new HashMap<String,Integer>();
	//private int ppInteractionNum;
    //private int pdInteractionNum;
    //private HashMap<String, Boolean> interactionIsIncludedMap;
    private Set<String> interactionIncludedSet =  new HashSet<String>();
    //private boolean includePPInteraction;
    //private boolean includePDInteraction;
    private DSGeneMarker dSGeneMarker;
    private String goInfoStr;
    private String geneType;
    private InteractionDetail[] interactionDetails;
    private static double threshold;
    private int[] distribution;
    private HashMap<String, int[]> interactionDistributionMap = new HashMap<String, int[]>();
    //private int[] ppDistribution;
    //private int[] pdDistribution;
    private static double smallestIncrement;
    private static Double defaultSmallestIncrement = 0.01;
    private static int binNumber;
    private boolean isDirty;

    public TreeMap<String, List<GOTerm>> getTreeMapForComponent() {
        return treeMapForComponent;
    }

    public void setTreeMapForComponent(TreeMap<String, List<GOTerm>> treeMapForComponent) {
        this.treeMapForComponent = treeMapForComponent;
    }

    public TreeMap<String, List<GOTerm>> getTreeMapForFunction() {
        return treeMapForFunction;
    }

    public void setTreeMapForFunction(TreeMap<String, List<GOTerm>> treeMapForFunction) {
        this.treeMapForFunction = treeMapForFunction;
    }

    public TreeMap<String, List<GOTerm>> getTreeMapForProcess() {
        return treeMapForProcess;
    }

    public void setTreeMapForProcess(TreeMap<String, List<GOTerm>> treeMapForProcess) {
        this.treeMapForProcess = treeMapForProcess;
    }

    //For go terms
   private TreeMap<String, List<GOTerm>> treeMapForComponent;
    private TreeMap<String, List<GOTerm>> treeMapForFunction;
    private TreeMap<String, List<GOTerm>> treeMapForProcess;

    public CellularNetWorkElementInformation(HashMap<String, Integer> interactionNumMap,  ArrayList<String> interactionIncludedSeList, DSGeneMarker dSGeneMarker, String goInfoStr, String geneType) {
        this.interactionNumMap = interactionNumMap;       
        this.interactionIncludedSet.addAll(interactionIncludedSeList);       
        this.dSGeneMarker = dSGeneMarker;
        this.goInfoStr = goInfoStr;
        this.geneType = geneType;
       
    } 

    public CellularNetWorkElementInformation(DSGeneMarker dSGeneMarker, List<String> allInteractionTypes) {
        this.dSGeneMarker = dSGeneMarker;
        smallestIncrement = defaultSmallestIncrement;
        isDirty = true;
        goInfoStr = "";
        Set<GOTerm> set = GeneOntologyUtil.getOntologyUtil().getAllGOTerms(dSGeneMarker);
       treeMapForComponent = GeneOntologyUtil.getOntologyUtil().getAllGoTerms(dSGeneMarker, AnnotationParser.GENE_ONTOLOGY_CELLULAR_COMPONENT);
        treeMapForFunction = GeneOntologyUtil.getOntologyUtil().getAllGoTerms(dSGeneMarker, AnnotationParser.GENE_ONTOLOGY_MOLECULAR_FUNCTION);
        treeMapForProcess = GeneOntologyUtil.getOntologyUtil().getAllGoTerms(dSGeneMarker, AnnotationParser.GENE_ONTOLOGY_BIOLOGICAL_PROCESS);


        if (set != null && set.size() > 0) {
            for (GOTerm goTerm : set) {
                goInfoStr += goTerm.getName() + "; ";
            }
        }
        geneType = GeneOntologyUtil.getOntologyUtil().checkMarkerFunctions(dSGeneMarker);
    	for (String interactionType: allInteractionTypes)
    	{
    		interactionNumMap.put(interactionType, 0);
    	}
    	
    	for (String interactionType: allInteractionTypes)
       	{
           	interactionDistributionMap.put(interactionType,new int[binNumber]);
           	 for (int i = 0; i < binNumber; i++)  
           		 (interactionDistributionMap.get(interactionType))[i]=0;
        }
    	
        
        reset();

    }

    /**
     * Remove all previous retrieved information.
     */
    public void reset() {
        // isDirty = true;
        //ppInteractionNum = 0;
        //pdInteractionNum = 0;
    	for (String interactionType: interactionNumMap.keySet())
    	{
    		interactionNumMap.put(interactionType, 0);
    	}
        binNumber = (int) (1 / smallestIncrement) + 1;
        distribution = new int[binNumber];
       // ppDistribution = new int[binNumber];
        //pdDistribution = new int[binNumber];
        
        for (String interactionType: interactionDistributionMap.keySet())
    	{
        	interactionDistributionMap.put(interactionType,new int[binNumber]);
        	 for (int i = 0; i < binNumber; i++) {
        		 (interactionDistributionMap.get(interactionType))[i]=0;
                
             }
    	}
        
        for (int i = 0; i < binNumber; i++) {
            distribution[i] = 0;
            
        }
    }

    public ArrayList<InteractionDetail> getSelectedInteractions() {
        ArrayList<InteractionDetail> arrayList = new ArrayList<InteractionDetail>();
        if (interactionDetails != null && interactionDetails.length > 0) {
            for (int i = 0; i < interactionDetails.length; i++) {
                InteractionDetail interactionDetail = interactionDetails[i];
                if (interactionDetail != null && interactionDetail.getConfidence() >= threshold) {
                    if (this.isIncludedInteraction(interactionDetail.getInteractionType()))
                    {
                        arrayList.add(interactionDetail);
                    }
                   
                }
            }
        }
        return arrayList;
    }

    public static double getSmallestIncrement() {
        return smallestIncrement;
    }

    public void setSmallestIncrement(double smallestIncrement) {
        this.smallestIncrement = smallestIncrement;
    }

    public int[] getDistribution() {
        return distribution;
    }

    public void setDistribution(int[] distribution) {
        this.distribution = distribution;
    }

    public static int getBinNumber() {
        return binNumber;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public static void setBinNumber(int binNumber) {
        CellularNetWorkElementInformation.binNumber = binNumber;
    }

    public CellularNetWorkElementInformation(HashMap<String,Integer> interactionNumMap, DSGeneMarker dSGeneMarker) {
        this.interactionNumMap = interactionNumMap;       
        this.dSGeneMarker = dSGeneMarker;
    }

    public Integer getInteractionNum(String interactionType) {
        if (interactionNumMap.containsKey(interactionType))
    	   return interactionNumMap.get(interactionType);
        else
        	return null;
    }
    
    public HashMap<String, Integer> getInteractionNumMap() {
        return this.interactionNumMap;
    }

    public static double getThreshold() {
        return threshold;
    }

    public void setThreshold(double _threshold) {
        if (this.threshold != _threshold) {
            this.threshold = _threshold;

        }
        update();
    }

    public InteractionDetail[] getInteractionDetails() {
        return interactionDetails;
    }

    /**
     * Associate the gene marker with the details.
     * @param arrayList
     */
    public void setInteractionDetails(ArrayList<InteractionDetail> arrayList) {

        if (arrayList != null && arrayList.size() > 0) {
            interactionDetails = new InteractionDetail[2];
            this.interactionDetails = arrayList.toArray(interactionDetails);
        }
        if (interactionDetails != null) {
            update();
        }
    }

    /**
     * Update the number of interaction based on the new threshold or new InteractionDetails.
     */
    private void update() {
        if (interactionDetails == null || interactionDetails.length == 0) {
            return;
        }

        reset();
        
        for (InteractionDetail interactionDetail : interactionDetails) {
            if (interactionDetail != null) {
                int confidence = (int) (interactionDetail.getConfidence() * 100);
                String interactionType = interactionDetail.getInteractionType();
                if (confidence < distribution.length && confidence >= 0) {
                    for (int i = 0; i <= confidence; i++) {
                        distribution[i]++;
                    }
                    
                    
                    if (interactionDistributionMap.containsKey(interactionType))
                    {
                    	 
                    	 for (int i = 0; i <= confidence; i++) {
                    		 interactionDistributionMap.get(interactionType)[i]++;
                         }
                    	 
                    }
                    else
                    {
                    	 int[] interactionDistribution = new int[binNumber];
                    	 for (int i = 0; i < binNumber; i++) {
                    		 if (i <= confidence)
                    		   interactionDistribution[i]=1;
                    		 else
                    		   interactionDistribution[i]=0;
                            
                         }                    	 
                    	 interactionDistributionMap.put(interactionType, new int[binNumber]);
                    	 
                    	 
                    }
                  
                }
                if (confidence >= 100 * threshold) {
                    if (this.interactionNumMap.containsKey(interactionType))
                    {
                    	int num = interactionNumMap.get(interactionType) + 1;
                    	interactionNumMap.put(interactionType, num);
                     
                    }
                    else
                    {
                    	interactionNumMap.put(interactionType, 1);
                     
                    }
                    
                }
            }
        }
        
       
    }

    public int[] getInteractionDistribution(String interactionType) {
        return this.interactionDistributionMap.get(interactionType);
    }

    public HashMap<String, int[]> getInteractionDistributionMap() {
        return this.interactionDistributionMap;
    }

    public Set<String> getIncludledInteractionSet() {
        return this.interactionIncludedSet;
    }
    public void setInteractionDistribution(String interactionType, int[] interactionDistribution) {
        this.interactionDistributionMap.put(interactionType, interactionDistribution);
    }

     

    public void setInteractionNum(String interactionType, int interactionNum) {
        this.interactionNumMap.put(interactionType, interactionNum);
    }

    

    public boolean isIncludedInteraction(String interactionType) {
        return this.interactionIncludedSet.contains(interactionType);
    }

    public void addIncludedInteractionType(String interactionType) {
        this.interactionIncludedSet.add(interactionType);
    }
    
    public void setIncludedInteractionTypeList(List<String> selectedInteractionTypes) {
        interactionIncludedSet.clear();
        interactionIncludedSet.addAll(selectedInteractionTypes);
    }
    
    public void removeIncludedInteraction(String interactionType) {
        this.interactionIncludedSet.remove(interactionType);
    }

    public DSGeneMarker getdSGeneMarker() {
        return dSGeneMarker;
    }

    public void setdSGeneMarker(DSGeneMarker dSGeneMarker) {
        this.dSGeneMarker = dSGeneMarker;
    }

    public String getGoInfoStr() {
        return goInfoStr;
    }

    public void setGoInfoStr(String goInfoStr) {
        this.goInfoStr = goInfoStr;
    }

    public String getGeneType() {
        return geneType;
    }

    public void setGeneType(String geneType) {
        this.geneType = geneType;
    }

    public boolean equals(Object obj){
        if(obj instanceof CellularNetWorkElementInformation){
            return dSGeneMarker.getGeneName().equals(((CellularNetWorkElementInformation)obj).getdSGeneMarker().getGeneName())&&dSGeneMarker.getLabel().equals(((CellularNetWorkElementInformation)obj).getdSGeneMarker().getLabel());
        }else{
            return false;
        }
    }
}
