package org.geworkbench.components.annotations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JComboBox;

import org.geworkbench.util.ProgressTask;

/**
 * RetrieveAllTask: retrieve all disease/agent associations from CGI
 * $Id$
 */

public class RetrieveAllTask extends ProgressTask<AgentDiseaseResults, String>{
		private AnnotationsPanel2 ap = null;

    	public RetrieveAllTask(int pbtype, String message, AnnotationsPanel2 ap2){
    		super(pbtype, message);
    		ap = ap2;
    	}

    	@Override
    	protected AgentDiseaseResults doInBackground(){
    		GeneAnnotation geneAnnotation = new GeneAnnotationImpl();
    		AgentDiseaseResults agentDiseaseResults = geneAnnotation.retrieveAll(this, ap.retrieveMarkerInfo, ap.diseaseModel, ap.agentModel);
    		if (isCancelled()) return null;

    		if (agentDiseaseResults == null){
            	ap.clearTable("cgi");
            	return null;
            }
    		return agentDiseaseResults;
        }

    	public void publish(String msg){
        	super.publish(msg);
        }

    	public void setProgress(double progress) {
        	super.setProgress((int)progress);
        }

        @Override
    	protected void done(){
        	setProgress(100);
        	ap.pd.removeTask(this);
			ap.retrieveItem.setEnabled(true);
        	if (isCancelled()) return;

        	AgentDiseaseResults agentDiseaseResults = null;
        	try{
        		agentDiseaseResults = get();
        	}catch(ExecutionException e){
        		e.printStackTrace();
        	}catch(InterruptedException e){
        		e.printStackTrace();
        	}

            MarkerData[] markers = agentDiseaseResults.getMarkers();
            GeneData[] genes = agentDiseaseResults.getGenes();
            DiseaseData[] diseases = agentDiseaseResults.getDiseases();
            RoleData[] roles = agentDiseaseResults.getRoles();
            SentenceData[] sentences = agentDiseaseResults.getSentences();
            PubmedData[] pubmeds = agentDiseaseResults.getPubmeds();
            MarkerData[] markers2 = agentDiseaseResults.getMarkers2();
            GeneData[] genes2 = agentDiseaseResults.getGenes2();

            DiseaseData[] agents = agentDiseaseResults.getAgents();
            RoleData[] agentRoles = agentDiseaseResults.getAgentRoles();
            SentenceData[] agentSentences = agentDiseaseResults.getAgentSentences();
            PubmedData[] agentPubmeds = agentDiseaseResults.getAgentPubmeds();
			// FIXME: remove number appended on the list, then do these,
			// follow by recalculate the number and append to the list.
            for (int j = 0; j < diseases.length; j++) {
            	//removeItem() followed by addItem() will produce a non-duplicate list
				ap.dropDownLists[0].removeItem(markers[j].name);
				ap.dropDownLists[0].addItem(markers[j].name);
				ap.dropDownLists[1].removeItem(genes[j].name);
				ap.dropDownLists[1].addItem(genes[j].name);
				ap.dropDownLists[2].removeItem(diseases[j].name);
				ap.dropDownLists[2].addItem(diseases[j].name);
				ap.dropDownLists[3].removeItem(roles[j].role);
				ap.dropDownLists[3].addItem(roles[j].role);
			}
            for (int j = 0; j < agents.length; j++) {
            	ap.dropDownLists[4].removeItem(markers2[j].name);
            	ap.dropDownLists[4].addItem(markers2[j].name);
            	ap.dropDownLists[5].removeItem(genes2[j].name);
            	ap.dropDownLists[5].addItem(genes2[j].name);
            	ap.dropDownLists[6].removeItem(agents[j].name);
            	ap.dropDownLists[6].addItem(agents[j].name);
            	ap.dropDownLists[7].removeItem(agentRoles[j].role);
            	ap.dropDownLists[7].addItem(agentRoles[j].role);
			}
            //since we'll use updateDiseaseNumber(), no need to do it now.
//            appendDiseaseNumber(markers,diseases,dropDownLists[2]);
//            appendDiseaseNumber(markers2,agents,dropDownLists[6]);

            ap.diseaseTable.setVisible(false);
            ap.agentTable.setVisible(false);
            ap.diseaseModel.insertData(ap.diseaseModel.size, markers, genes, diseases, roles, sentences, pubmeds);
            ap.diseaseModel.filterBy(0, "");
            ap.diseaseModel.sortByColumn(0,true);
            ap.diseaseTable.revalidate();
            ap.agentModel.insertData(ap.agentModel.size, markers2, genes2, agents, agentRoles, agentSentences, agentPubmeds);
            ap.agentModel.filterBy(0, "");
            ap.agentModel.sortByColumn(0,true);
            ap.agentTable.revalidate();
            ap.diseaseTable.setVisible(true);
            ap.agentTable.setVisible(true);
            ap.sortByNumberOfRecords = true;
            //Clear numOutOfNum, so no "Retrieve All" option shown if we already got all records.
            for (int i = 0; i < ap.diseaseModel.markerData.length; i++) {
    			MarkerData markerData3 = ap.diseaseModel.markerData[i];
    			if (ap.retrieveMarker.getLabel().equals(markerData3.name))
    				markerData3.numOutOfNum="";
    		}
            for (int i = 0; i < ap.agentModel.markerData.length; i++) {
    			MarkerData markerData3 = ap.agentModel.markerData[i];
    			if (ap.retrieveMarker.getLabel().equals(markerData3.name))
    				markerData3.numOutOfNum="";
    		}

            ap.diseaseTable.getTableHeader().repaint();
            ap.agentTable.getTableHeader().repaint();
            ap.leftHeadPanel.setVisible(true);
            ap.leftHeadPanel.revalidate();
            ap.rightHeadPanel.setVisible(true);
            ap.rightHeadPanel.revalidate();
            ap.syncHeaderWidthWithTableWidth();
            updateDiseaseNumber();
            ap.orderDropDownLists(ap.dropDownLists[2]);
            ap.orderDropDownLists(ap.dropDownLists[6]);
			ap.diseaseModel.sortByColumn(2, false);
			ap.diseaseModel.sortByColumn(0, true);
			ap.agentModel.sortByColumn(2, false);
			ap.agentModel.sortByColumn(0, true);
    	}
    	@Override
    	protected void process(List<String> chunks){
        	for (String message : chunks){
        		if (isCancelled()) return;
        		pb.setMessage(message);
            	ap.pd.updateWidth(message);
        	}
        }
    	
        /*
        *
        */
       private void updateDiseaseNumber(){
       	JComboBox comboBox = ap.dropDownLists[2];
       	MarkerData[] mData = ((CGITableModel)ap.diseaseTable.getModel()).markerData;
       	DiseaseData[] dData = ((CGITableModel)ap.diseaseTable.getModel()).diseaseData;
       	HashSet<String> diseaseNames = new HashSet<String>();
       	for (int i = 0; i < dData.length; i++) {
   			String diseaseName = dData[i].name;
   			diseaseNames.add(diseaseName);
   		}
       	HashSet<String> markerNames = new HashSet<String>();
       	for (int i = 0; i < mData.length; i++) {
   			String markerName = mData[i].name;
   			markerNames.add(markerName);
   		}
       	HashMap<String, Integer> countMap = new HashMap<String, Integer>();
       	for (Iterator<String> iterator = diseaseNames.iterator(); iterator.hasNext();) {
       		String diseaseName = iterator.next();
       		HashSet<String> hitMarkers = new HashSet<String>();
   	    	for (Iterator<String> iterator2 = markerNames.iterator(); iterator2.hasNext();) {
   	    		String markerName = iterator2.next();
   	        	for (int i = 0; i < dData.length; i++) {
   	    			String aDiseaseName = dData[i].name;
   	    			if(mData[i].name.equals(markerName)){
   	        			if (aDiseaseName.equals(diseaseName)){
   	        				hitMarkers.add(markerName);
   	        			}
   	    			}
       			}
       		}
   	    	countMap.put(diseaseName, hitMarkers.size());
   		}
   		comboBox.removeAllItems();
   		comboBox.addItem("");
   		for (Iterator<String> iterator = diseaseNames.iterator(); iterator.hasNext();) {
   			String diseaseName = iterator.next();
   			comboBox.addItem(diseaseName+" ("+countMap.get(diseaseName)+")");
   		}
   		// do the same thing for table2
       	comboBox = ap.dropDownLists[6];
       	mData = ((CGITableModel)ap.agentTable.getModel()).markerData;
       	dData = ((CGITableModel)ap.agentTable.getModel()).diseaseData;
       	diseaseNames = new HashSet<String>();
       	for (int i = 0; i < dData.length; i++) {
   			String diseaseName = dData[i].name;
   			diseaseNames.add(diseaseName);
   		}
       	markerNames = new HashSet<String>();
       	for (int i = 0; i < mData.length; i++) {
   			String markerName = mData[i].name;
   			markerNames.add(markerName);
   		}
       	countMap = new HashMap<String, Integer>();
       	for (Iterator<String> iterator = diseaseNames.iterator(); iterator.hasNext();) {
       		String diseaseName = iterator.next();
       		HashSet<String> hitMarkers = new HashSet<String>();
   	    	for (Iterator<String> iterator2 = markerNames.iterator(); iterator2.hasNext();) {
   	    		String markerName = iterator2.next();
   	        	for (int i = 0; i < dData.length; i++) {
   	    			String aDiseaseName = dData[i].name;
   	    			if(mData[i].name.equals(markerName)){
   	        			if (aDiseaseName.equals(diseaseName)){
   	        				hitMarkers.add(markerName);
   	        			}
   	    			}
       			}
       		}
   	    	countMap.put(diseaseName, hitMarkers.size());
   		}
   		comboBox.removeAllItems();
   		comboBox.addItem("");
   		for (Iterator<String> iterator = diseaseNames.iterator(); iterator.hasNext();) {
   			String diseaseName = iterator.next();
   			comboBox.addItem(diseaseName+" ("+countMap.get(diseaseName)+")");
   		}
       }

    }
