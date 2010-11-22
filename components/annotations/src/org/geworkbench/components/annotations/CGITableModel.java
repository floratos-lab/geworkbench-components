package org.geworkbench.components.annotations;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.ProgressItem;
import org.jfree.ui.SortableTable;
import org.jfree.ui.SortableTableModel;

import com.Ostermiller.util.CSVPrinter;

/**
 * Use by Cancer Gene Index tables, both disease and agent table.
 * 
 * @version $Id$
 */
public class CGITableModel extends SortableTableModel {
	private static final long serialVersionUID = -1894565199518094545L;
	
	static Log log = LogFactory.getLog(CGITableModel.class);
    public static final int COL_MARKER = 0;
    public static final int COL_GENE = 1;
    public static final int COL_AGENT = 2;
    public static final int COL_DISEASE = 2;
    public static final int COL_ROLE = 3;
    public static final int COL_SENTENCE = 4;
    public static final int COL_PUBMED = 5;
    public static final int COL_EVSID = 6;
	
	final int numOfColumns = 6;
	MarkerData[] markerData;
    GeneData[] geneData;
    DiseaseData[] diseaseData;
    private RoleData[] roleData;
    private SentenceData[] sentenceData;
    private PubmedData[] pubmedData;

    Integer[] indices;			//for sorting feature
    private Integer[] filterIndices;	//for filtering feature
    int size;
    private int filteredSize;
    //for expand/collapse feature
    private Set<String> expandedByKeys;	//store unique gene,disease pairs of expanded nodes.
    private Boolean[] repExpandedDiseaseList;	//TRUE if that record is the representative node for it's collapsed group.
    private Boolean[] expandedDiseaseList;	//TRUE if that record is expanded.
    //for filter feature work with expand/collapse
    private String[] filterByStrings;	//store filterBy string for each column, empty if not filtered.
    private Boolean[] filteredDiseaseList;	//TRUE if that record is filtered.
    Map<String, Integer> numOfDuplicatesMap = null;	//for speed up the calculation duplications.
    int[] numOfDuplicatesArray = null;	//to store the number of duplications, so we can access it by index.

    int[] numOfDuplicatesCache;

    public CGITableModel(final AnnotationsPanel2 annotationsPanel, MarkerData[] markerData, GeneData[] geneData, DiseaseData[] diseaseData, RoleData[] roleData, SentenceData[] sentenceData, PubmedData[] pubmedData) {
        this.markerData = markerData;
        this.geneData = geneData;
        this.diseaseData = diseaseData;
        this.roleData = roleData;
        this.sentenceData = sentenceData;
        this.pubmedData = pubmedData;
        size = diseaseData.length;
        filteredSize = size;
        indices = new Integer[size];
        filterIndices = new Integer[size];
        expandedByKeys = new HashSet<String>();
        repExpandedDiseaseList = new Boolean[size];
        expandedDiseaseList = new Boolean[size];
        filterByStrings = new String[numOfColumns];
        filteredDiseaseList = new Boolean[size];
        numOfDuplicatesMap = new HashMap<String, Integer>();
        numOfDuplicatesArray = new int[size];
        resetIndices();
        for (int i = 0; i < size; i++) {
            indices[i] = i;
            filterIndices[i] = i;
            expandedDiseaseList[i]=true;
            filteredDiseaseList[i]=false;
            repExpandedDiseaseList[i]=true;
        }
        
        this.annotationsPanel = annotationsPanel;
    }

    /**
	 * This is the method contains the algorithm to determine if a record is
	 * already exist in the model or not.
	 *
	 * @param markerData
	 * @param geneData
	 * @param diseaseData
	 * @param roleData
	 * @param sentenceData
	 * @param pubmedData
	 * @return
	 */
    public boolean containsRecord(MarkerData markerData, GeneData geneData,
			DiseaseData diseaseData, RoleData roleData,
			SentenceData sentenceData, PubmedData pubmedData) {
		for (int i = 0; i < this.markerData.length; i++) {
			MarkerData markerData2 = this.markerData[i];
			GeneData geneData2 = this.geneData[i];
			DiseaseData diseaseData2 = this.diseaseData[i];
			RoleData roleData2 = this.roleData[i];
			SentenceData sentenceData2 = this.sentenceData[i];
			PubmedData pubmedData2 = this.pubmedData[i];

			if (markerData2.name.equals(markerData.name)
					&& geneData2.name.equals(geneData.name)
					&& diseaseData2.name.equals(diseaseData.name)
					&& roleData2.role.equals(roleData.role)
					&& sentenceData2.sentence.equals(sentenceData.sentence)
					&& pubmedData2.id.equals(pubmedData.id)) {
				return true;
			}
		}
		return false;
	}

    /* a reference of AnnotationsPanel2 to able to add it as observer */
    AnnotationsPanel2 annotationsPanel = null;
    public CGITableModel(final AnnotationsPanel2 annotationsPanel) {
        this.markerData = new MarkerData[0];
        this.geneData = new GeneData[0];
        this.diseaseData = new DiseaseData[0];
        this.roleData = new RoleData[0];
        this.sentenceData = new SentenceData[0];
        this.pubmedData = new PubmedData[0];
        size = 0;
        filteredSize = size;
        indices = new Integer[0];
        filterIndices = new Integer[0];
        expandedByKeys = new HashSet<String>();
        repExpandedDiseaseList = new Boolean[size];
        expandedDiseaseList = new Boolean[0];
        filterByStrings = new String[numOfColumns];
        filteredDiseaseList = new Boolean[0];
        
        this.annotationsPanel = annotationsPanel;
    }


    private void resetIndices() {
        for (int i = 0; i < size; i++) {
            indices[i] = i;
        }
    }

    public void filterBy(int columnIndex, String value) {
		filterByStrings[columnIndex]=value;
		if ((value == null)||(value.equals(""))) { // show all
			filteredSize = 0;
			for (int i = 0; i < markerData.length; i++) {
				if (expandedDiseaseList[i]){
					filterIndices[filteredSize]=indices[i];
					filteredSize++;
				}
				filteredDiseaseList[i]=false;
			}
		} else {
			// filter rows
			filteredSize = 0;
			switch (columnIndex) {
			case COL_MARKER:
				for (int i = 0; i < markerData.length; i++) {
					if (markerData[indices[i]].name.equals(value)){
						if (expandedDiseaseList[i]){
							filterIndices[filteredSize]=indices[i];
							filteredSize++;
						}else{
							filterIndices[i]=-1;
						}
						filteredDiseaseList[i]=false;
					}else{
						filterIndices[i]=-1;
						filteredDiseaseList[i]=true;
					}
				}
				break;
			case COL_GENE:
				for (int i = 0; i < markerData.length; i++) {
					if (geneData[indices[i]].name.equals(value)){
						if (expandedDiseaseList[i]){
							filterIndices[filteredSize]=indices[i];
							filteredSize++;
						}else{
							filterIndices[i]=-1;
						}
						filteredDiseaseList[i]=false;
					}else{
						filterIndices[i]=-1;
						filteredDiseaseList[i]=true;
					}
				}
				break;
			case COL_DISEASE:
				for (int i = 0; i < markerData.length; i++) {
					if (diseaseData[i].name.equals(value)){
						if (expandedDiseaseList[i]){
							filterIndices[filteredSize]=i;
							filteredSize++;
						}else{
							filterIndices[i]=-1;
						}
						filteredDiseaseList[i]=false;
					}else{
						filterIndices[i]=-1;
						filteredDiseaseList[i]=true;
					}
				}
				break;
			case COL_ROLE:
				for (int i = 0; i < markerData.length; i++) {
					if (roleData[indices[i]].role.equals(value)){
						if (expandedDiseaseList[i]){
							filterIndices[filteredSize]=indices[i];
							filteredSize++;
						}else{
							filterIndices[i]=-1;
						}
						filteredDiseaseList[i]=false;
					}else{
						filterIndices[i]=-1;
						filteredDiseaseList[i]=true;
					}
				}
				break;
			}
		}
	}
    Map<Integer, String> collapsedDisease = new HashMap<Integer, String>(); //key index

    /**
	 * This method will collapse given gene-disease pairs in the given
	 * table, then refresh the table.
	 *
	 * @param aTable
	 * @param gene
	 * @param disease
	 */
    public void collapseBy(SortableTable aTable, String gene, String disease) {
    	_collapseBy(aTable, gene, disease);
		aTable.revalidate();
		aTable.repaint();
	}

	/**
	 * This method will collapse given gene-disease pairs in the given table
	 * This method will NOT refresh the table, so you can call this method
	 * multiple time and refresh it at once.
	 *
	 * @param aTable
	 * @param gene
	 * @param disease
	 */
    public void _collapseBy(SortableTable aTable, String gene, String disease) {
    	log.debug("collapseBy "+gene+","+disease);
		filteredSize = 0;
		String collapsedKey = gene+disease;
		expandedByKeys.remove(collapsedKey);
		Map<String, Integer> uniqList = new HashMap<String, Integer>();
		for (int i = 0; i < markerData.length; i++) {
			filterIndices[i]=-1;
		}
		for (int i = 0; i < markerData.length; i++) {
			String key = geneData[indices[i]].name+diseaseData[indices[i]].name;
			if (!filteredDiseaseList[i]){
				if (geneData[indices[i]].name.equals(gene) && diseaseData[indices[i]].name.equals(disease))
					if (!uniqList.containsKey(key)){
						uniqList.put(key, indices[i]);
						repExpandedDiseaseList[i]=true;
						if (collapsedDisease.containsValue(key)){
							//skip this one
						}else{
							collapsedDisease.put(new Integer(i), key);
						}
					}else{
						repExpandedDiseaseList[i]=false;
						expandedDiseaseList[i] = false;
					}
				if (expandedDiseaseList[i]){
					filterIndices[filteredSize]=indices[i];
					filteredSize++;
				}
			}
			log.debug("filterIndices[i] (for "+key+")=="+filterIndices[i]);
		}
		log.debug("filteredSize becomes to "+filteredSize);
	}

    //FIXME: this method should also be called when retrieve, not only retrieve all.
    public void updateNumOfDuplicates(){
    	DupTask dupTask = new DupTask(ProgressItem.INDETERMINATE_TYPE, "Calculating number of duplicates...", this);
    	annotationsPanel.pd.executeTask(dupTask);
    }

    /*
	 * This is a wrapper for expandBy, to be called from inside of this
	 * file. So program will know it's called by the user, not by the code.
	 * When called, it will set sortByNumberOfRecords to false, so the
	 * program will exit it's first-time status. And the sorting will back
	 * to normal status (sort by String instead of sort by number of
	 * records).
	 */
    void _expandBy(SortableTable aTable, String gene, String disease) {
    	annotationsPanel.sortByNumberOfRecords=false;
    	expandBy(aTable, gene, disease);
    	//sort by disease
    	((SortableTableModel)aTable.getModel()).sortByColumn(2,true);
    	//sort by marker
    	((SortableTableModel)aTable.getModel()).sortByColumn(0,true);
    }

    public void expandBy(SortableTable aTable, String gene, String disease) {
		String expandKey = gene+disease;
		expandedByKeys.add(expandKey);
		filteredSize = 0;
		for (int i = 0; i < markerData.length; i++) {
				//log.error(i+": "+geneData[indices[i]].name+", "+ diseaseData[indices[i]].name);
				if (geneData[i].name.equals(gene) && diseaseData[i].name.equals(disease)){
					expandedDiseaseList[i] = true;
					//log.error("expand ^^^");
				}
				if (expandedDiseaseList[i] && (!filteredDiseaseList[i])){
					if ((repExpandedDiseaseList[i])&&(!(geneData[i].name.equals(gene) && diseaseData[i].name.equals(disease)))){
						//it's expanded because it's a representatives, not because it's expanded in this run.
					}else{
						//it's expanded, remove this one from collapsedDisease list
						collapsedDisease.remove(new Integer(i));
					}
					filterIndices[filteredSize]=i;
					filteredSize++;
				}else
					filterIndices[i]=-1;
		}
		aTable.revalidate();
		aTable.repaint();
	}

    public void toggleExpandCollapse(SortableTable aTable, String gene, String disease){
    	boolean collapsed = false;
    	String key = gene+disease;
    	if (collapsedDisease.containsValue(key)){
    		collapsed = true;
    	}
		if (collapsed)
			_expandBy(aTable, gene, disease);
		else
			collapseBy(aTable, gene, disease);
    }

    /**
	 * Internally used for retrieve all feature. Can by used externally.
	 *
	 * @param index
	 *            Where to insert to. 0 will insert from the beginning. The
	 *            records which has index large or equals to this number
	 *            will be moved toward the end of array to have space to
	 *            insert these records.
	 * @param markerData
	 * @param geneData
	 * @param diseaseData
	 * @param roleData
	 * @param sentenceData
	 * @param pubmedData
	 */
    public void insertData(int index, MarkerData[] markerData,
			GeneData[] geneData, DiseaseData[] diseaseData,
			RoleData[] roleData, SentenceData[] sentenceData,
			PubmedData[] pubmedData) {
		int sizeOfRecords = markerData.length;
		if (sizeOfRecords==0) return;
		if ((geneData.length != sizeOfRecords)
				|| (diseaseData.length != sizeOfRecords)
				|| (roleData.length != sizeOfRecords)
				|| (sentenceData.length != sizeOfRecords)
				|| (pubmedData.length != sizeOfRecords)) {
			log.error("Insert data should have equal numbers of rows for all arrays.");
			return;
		}

        //calculate new size
        int newSize = size+sizeOfRecords;

		//TODO: append arrays (eg: markerData, geneData) to old arrays.
        //this.markerData = markerData;
        MarkerData[] newMarkerData = new MarkerData[newSize];
        for (int i = 0; i < newMarkerData.length; i++) {
			if (i<index)
				newMarkerData[i]=this.markerData[i];
			else
				newMarkerData[i]=markerData[i-index];
		}
        //this.geneData = geneData;
        GeneData[] newGeneData = new GeneData[newSize];
        for (int i = 0; i < newGeneData.length; i++) {
			if (i<index)
				newGeneData[i]=this.geneData[i];
			else
				newGeneData[i]=geneData[i-index];
		}
        //this.diseaseData = diseaseData;
        DiseaseData[] newDiseaseData = new DiseaseData[newSize];
        for (int i = 0; i < newDiseaseData.length; i++) {
			if (i<index)
				newDiseaseData[i]=this.diseaseData[i];
			else
				newDiseaseData[i]=diseaseData[i-index];
		}
        //this.roleData = roleData;
        RoleData[] newRoleData = new RoleData[newSize];
        for (int i = 0; i < newRoleData.length; i++) {
			if (i<index)
				newRoleData[i]=this.roleData[i];
			else
				newRoleData[i]=roleData[i-index];
		}
        //this.sentenceData = sentenceData;
        SentenceData[] newSentenceData = new SentenceData[newSize];
        for (int i = 0; i < newSentenceData.length; i++) {
			if (i<index)
				newSentenceData[i]=this.sentenceData[i];
			else
				newSentenceData[i]=sentenceData[i-index];
		}
        //this.pubmedData = pubmedData;
        PubmedData[] newPubmedData = new PubmedData[newSize];
        for (int i = 0; i < newPubmedData.length; i++) {
			if (i<index)
				newPubmedData[i]=this.pubmedData[i];
			else
				newPubmedData[i]=pubmedData[i-index];
		}


        //update indices
        //indices = new Integer[size];
		//for this implementation, I put it at the end of the list.
        Integer[] newIndices = new Integer[newSize];	//with new size
        for (int i = 0; i < newIndices.length; i++) {
        	if (i<size)
        		newIndices[i]=indices[i];
        	else
        		newIndices[i]=i;
		}

        //filterIndices = new Integer[size];
        Integer[] newFilterIndices = new Integer[newSize];
        for (int i = 0; i < newFilterIndices.length; i++) {
        	if (i<size)
        		newFilterIndices[i]=filterIndices[i];
        	else
        		newFilterIndices[i]=i;
		}

        //expandedByKeys = new HashSet<String>();
        HashSet<String> newExpandedByKeys = new HashSet<String>();
        newExpandedByKeys.addAll(expandedByKeys);
        for (int i = 0; i < geneData.length; i++) {
        	String key = geneData[i].name+diseaseData[i].name;
            newExpandedByKeys.add(key);
		}

        //repExpandedDiseaseList = new Boolean[size];
        //expandedDiseaseList = new Boolean[size];
		Map<String, Integer> uniqList = new HashMap<String, Integer>();
        Boolean[] newRepExpandedDiseaseList = new Boolean[newSize];
        Boolean[] newExpandedDiseaseList = new Boolean[newSize];
        for (int i = 0; i < newRepExpandedDiseaseList.length; i++) {
        	if (i<index){
        		newRepExpandedDiseaseList[i]=repExpandedDiseaseList[i];
        		newExpandedDiseaseList[i]=expandedDiseaseList[i];
        	}else{
            	String key = geneData[i-index].name+diseaseData[i-index].name;
				if (!uniqList.containsKey(key)){
					uniqList.put(key, i);
					newRepExpandedDiseaseList[i]=true;
				}else
					newRepExpandedDiseaseList[i]=false;
				newExpandedDiseaseList[i]=true;
			}
		}

        //filterByStrings = new String[numOfColumns];
        //this will not be changed during insertion, we use the old one.
        String[] newFilterByStrings = filterByStrings;

        //filteredDiseaseList = new Boolean[size];
        Boolean[] newFilteredDiseaseList = new Boolean[newSize];
        for (int i = 0; i < newFilteredDiseaseList.length; i++) {
        	if (i<index){
        		newFilteredDiseaseList[i]=filteredDiseaseList[i];
        	}else{
        		//since there's no way user can retrieve a filtered-out record.
        		newFilteredDiseaseList[i]=false;
        	}
		}

        //numOfDuplicatesCache = new int[size];
        numOfDuplicatesMap = new HashMap<String, Integer>();

        this.markerData = newMarkerData;
        this.geneData = newGeneData;
        this.diseaseData = newDiseaseData;
        this.roleData = newRoleData;
        this.sentenceData = newSentenceData;
        this.pubmedData = newPubmedData;
        size = newSize;
        //filteredSize = newFilteredSize;
        indices = newIndices;
        filterIndices = newFilterIndices;
        expandedByKeys = newExpandedByKeys;
        repExpandedDiseaseList = newRepExpandedDiseaseList;
        expandedDiseaseList = newExpandedDiseaseList;
        filterByStrings = newFilterByStrings;
        filteredDiseaseList = newFilteredDiseaseList;
        //now, everything is in the model, we'll need to refresh the table
        filterBy(0, "");	//first, un-hide the hided records.
        //expand the collapsed
        SortableTable selectedTable = annotationsPanel.selectedTable;
        selectedTable = annotationsPanel.diseaseTable;
        ((CGITableModel)selectedTable.getModel()).updateNumOfDuplicates();
        //TODO: we probably can call expandAll() and collapseAll() for following steps.
        for (int j = 0; j < ((CGITableModel)selectedTable.getModel()).size; j++) {
			((CGITableModel) selectedTable.getModel())._expandBy(selectedTable,
					((GeneData) (((CGITableModel) selectedTable
							.getModel()).getObject(j,
							COL_GENE))).name,
					((DiseaseData) (((CGITableModel) selectedTable
							.getModel()).getObject(j,
							COL_DISEASE))).name);
        }
        //recollapse them
        for (int j = 0; j < ((CGITableModel)selectedTable.getModel()).size; j++) {
			((CGITableModel) selectedTable.getModel())._collapseBy(selectedTable,
					((GeneData) (((CGITableModel) selectedTable
							.getModel()).getObject(j,
							COL_GENE))).name,
					((DiseaseData) (((CGITableModel) selectedTable
							.getModel()).getObject(j,
							COL_DISEASE))).name);
        }
        selectedTable.revalidate();
        selectedTable.repaint();

        selectedTable = annotationsPanel.agentTable;
        ((CGITableModel)selectedTable.getModel()).updateNumOfDuplicates();
        //expand the collapsed
        for (int j = 0; j < ((CGITableModel)selectedTable.getModel()).size; j++) {
			((CGITableModel) selectedTable.getModel())._expandBy(selectedTable,
					((GeneData) (((CGITableModel) selectedTable
							.getModel()).getObject(j,
							COL_GENE))).name,
					((DiseaseData) (((CGITableModel) selectedTable
							.getModel()).getObject(j,
							COL_DISEASE))).name);
        }
        //recollapse them
        for (int j = 0; j < ((CGITableModel)selectedTable.getModel()).size; j++) {
			((CGITableModel) selectedTable.getModel())._collapseBy(selectedTable,
					((GeneData) (((CGITableModel) selectedTable
							.getModel()).getObject(j,
							COL_GENE))).name,
					((DiseaseData) (((CGITableModel) selectedTable
							.getModel()).getObject(j,
							COL_DISEASE))).name);
        }
        selectedTable.revalidate();
        selectedTable.repaint();
	}

    public int getRowCount() {
        return filteredSize;
    }

    public int getColumnCount() {
		return numOfColumns;
    }

    public Object getObjectAt(int rowIndex, int columnIndex) {
    	log.debug(rowIndex+","+columnIndex);
    	try{
        switch (columnIndex) {
            case COL_MARKER:
                return markerData[indices[filterIndices[rowIndex]]];
            case COL_GENE:
                return geneData[indices[filterIndices[rowIndex]]];
            case COL_DISEASE:
            	return diseaseData[filterIndices[rowIndex]];
            case COL_ROLE:
           		return roleData[filterIndices[rowIndex]];
            case COL_SENTENCE:
           		return sentenceData[filterIndices[rowIndex]];
            case COL_PUBMED:
           		return pubmedData[filterIndices[rowIndex]];
            case COL_EVSID:
                return diseaseData[filterIndices[rowIndex]];
        }
    	}catch (Exception e){
        	log.error(e,e);
        }
        return null;
    }
    public Object getObject(int rowIndex, int columnIndex) {
    	log.debug(rowIndex+","+columnIndex);
    	try{
        switch (columnIndex) {
            case COL_MARKER:
                return markerData[rowIndex];
            case COL_GENE:
                return geneData[rowIndex];
            case COL_DISEASE:
            	return diseaseData[rowIndex];
            case COL_ROLE:
           		return roleData[rowIndex];
            case COL_SENTENCE:
           		return sentenceData[rowIndex];
            case COL_PUBMED:
           		return pubmedData[rowIndex];
            case COL_EVSID:
                return diseaseData[rowIndex];
        }
    	}catch (Exception e){
        	log.error(e,e);
        }
        return null;
    }
    public int getNumOfDuplicates(String geneName, String diseaseName){
		int numOfDuplicates=0;
		for (int i = 0; i < markerData.length; i++) {
			if (geneData[i].name.equals(geneName) && diseaseData[i].name.equals(diseaseName)){
				numOfDuplicates++;
			}
		}
		return numOfDuplicates;
    }
    public Object getValueAt(int rowIndex, int columnIndex) {
    	log.debug(rowIndex+","+columnIndex);
    	Integer origIndex = null;
    	try{
        switch (columnIndex) {
            case COL_MARKER:
            	if ((filterIndices[rowIndex]<0)||(indices[filterIndices[rowIndex]]<0)) return null;
                return markerData[indices[filterIndices[rowIndex]]].name;
            case COL_GENE:
                return wrapInHTML(geneData[indices[filterIndices[rowIndex]]].name);
            case COL_DISEASE:
            	if (collapsedDisease.containsKey(indices[filterIndices[rowIndex]])){
            		int numOfDuplicates = numOfDuplicatesArray[indices[filterIndices[rowIndex]]];//getNumOfDuplicates(geneData[indices[filterIndices[rowIndex]]].name,diseaseData[filterIndices[rowIndex]].name);
            		return diseaseData[indices[filterIndices[rowIndex]]].name+"("+numOfDuplicates+")";
            	}else
            		return diseaseData[indices[filterIndices[rowIndex]]].name;
            case COL_ROLE:
            	origIndex = new Integer(indices[filterIndices[rowIndex]]);
            	if (collapsedDisease.containsKey(origIndex))
            		return "...";
            	else
            		return roleData[filterIndices[rowIndex]].role;
            case COL_SENTENCE:
            	origIndex = new Integer(indices[filterIndices[rowIndex]]);
            	if (collapsedDisease.containsKey(origIndex))
            		return "...";
            	else
            		return sentenceData[filterIndices[rowIndex]].sentence;
            case COL_PUBMED:
            	origIndex = new Integer(indices[filterIndices[rowIndex]]);
            	if (collapsedDisease.containsKey(origIndex))
            		return "...";
            	else
            		return wrapInHTML(pubmedData[filterIndices[rowIndex]].id);
            case COL_EVSID:
                return diseaseData[filterIndices[rowIndex]].evsId;
        }
    	}catch (Exception e){
        	log.error(e,e);
        }
        return null;
    }

    public MarkerData getMarkerAt(int rowIndex, int columnIndex) {
    	log.debug("getMarkerAt "+rowIndex+","+columnIndex);
    	if ((rowIndex==-1) || (filterIndices[rowIndex]==-1)){
    		log.error("getMarkerAt "+rowIndex+","+columnIndex);
    		log.error("filterIndices[rowIndex] "+filterIndices[rowIndex]);
    		return null;
    	}
    	return markerData[filterIndices[rowIndex]];
    }

    // TODO This method depends on the anonymity of which column used to sort. There could be a better way to implement.
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void sortByColumn(final int column, final boolean ascending) {
        resetIndices();
        final Comparable[][] columns = {markerData, geneData, diseaseData, roleData, sentenceData, pubmedData};
        Comparator<Integer> comparator = new Comparator<Integer>() {
            public int compare(Integer i, Integer j) {
                if (ascending) {
                	if ((i==-1)||(j==-1)) return -1;
                	if (!expandedDiseaseList[i]) return -1;
                	if (!expandedDiseaseList[j]) return -1;
                	if (column>COL_DISEASE){
                    	if (collapsedDisease.containsKey(i)&&collapsedDisease.containsKey(j))
                    		return 0;
                    	if (collapsedDisease.containsKey(i))
                    		return 1;
                    	if (collapsedDisease.containsKey(j))
                    		return -1;
                	}
                	if (annotationsPanel.sortByNumberOfRecords && (column==COL_DISEASE)) {
                		if (numOfDuplicatesArray[i]<numOfDuplicatesArray[j])
                			return -1;
                		else if (numOfDuplicatesArray[i]>numOfDuplicatesArray[j])
                			return 1;
                		else return 0;
                	}
                    return columns[column][i].compareTo(columns[column][j]);
                } else {
                	if ((i==-1)||(j==-1)) return -1;
                	if (!expandedDiseaseList[i]) return -1;
                	if (!expandedDiseaseList[j]) return -1;
                	if (column>COL_DISEASE){
                    	if (collapsedDisease.containsKey(i)&&collapsedDisease.containsKey(j))
                    		return 0;
                		if (collapsedDisease.containsKey(i))
                    		return -1;
                		if (collapsedDisease.containsKey(j))
                    		return 1;
                	}
                	if (annotationsPanel.sortByNumberOfRecords && (column==COL_DISEASE)) {
                		if (numOfDuplicatesArray[i]<numOfDuplicatesArray[j])
                			return 1;
                		else if (numOfDuplicatesArray[i]>numOfDuplicatesArray[j])
                			return -1;
                		else return 0;
                	}
                    return columns[column][j].compareTo(columns[column][i]);
                }
            }
        };
        Arrays.sort(filterIndices, comparator);
        super.sortByColumn(column, ascending);
    }

    public boolean isSortable(int i) {
        return true;
    }

    /**
     * Handles activated cell in CGI table
     * @param rowIndex
     * @param columnIndex
     */
    public void activateCell(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case COL_MARKER:
                MarkerData marker = markerData[filterIndices[indices[rowIndex]]];
                SortableTable selectedTable = annotationsPanel.selectedTable;
                if (selectedTable.equals(annotationsPanel.diseaseTable))
                	annotationsPanel.dropDownLists[4].setSelectedItem(marker.name);
                else if(selectedTable.equals(annotationsPanel.agentTable))
                	annotationsPanel.dropDownLists[0].setSelectedItem(marker.name);
                annotationsPanel.agentTable.repaint();
                annotationsPanel.activateMarker(marker);
                annotationsPanel.diseaseTable.repaint();
                break;
            case COL_GENE:
//                if (gene.getCGAPGeneURLs().size() > 0) {
//                    activateGene(gene);
//                }
                break;
            case COL_DISEASE:
            	DiseaseData disease = diseaseData[indices[rowIndex]];
                // Could be the blank "(none)" pathway.
                if (disease.diseaseOntology != null) {
                    //activatePathway(pathway);
                }
                break;
        }
    }

	public MarkerData[] getMarkerData() {
		return markerData;
	}

	public boolean toCSV(String filename) {
		String[] diseaseHeader = { "Marker ID", "Gene Symbol", "Disease",
				"Gene-Disease Relation", "Abstract Sentence", "PMID" };
		String[] agentHeader = { "Marker ID", "Gene Symbol", "Agent",
				"Gene-Agent Relation", "Abstract Sentence", "PMID" };
		File tempAnnot = new File(filename);
		try {
			CSVPrinter csvout = new CSVPrinter(new BufferedOutputStream(
					new FileOutputStream(tempAnnot)));
			if (this == annotationsPanel.diseaseModel){
				for (int i = 0; i < diseaseHeader.length; i++) {
					csvout.print(diseaseHeader[i]);
				}
				csvout.println();
			}else{
				for (int i = 0; i < agentHeader.length; i++) {
					csvout.print(agentHeader[i]);
				}
				csvout.println();
			}
			for (int cx = 0; cx < this.size; cx++) {
				String markerName = markerData[cx].name;
				String geneName = geneData[cx].name;
				String diseaseName = diseaseData[cx].name;
				String roleName = roleData[cx].role;
				String sentence = sentenceData[cx].sentence;
				String pubmedId = pubmedData[cx].id;
				csvout.print(markerName);
				csvout.print(geneName);
				csvout.print(diseaseName);
				csvout.print(roleName);
				csvout.print(sentence);
				csvout.print(pubmedId);
				csvout.println();
			}
			csvout.flush();
			csvout.close();
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}
		return true;
	}
	
	private static String wrapInHTML(String s) {
		return "<html><a href=\"__noop\">" + s + "</a></html>";
	}

}
