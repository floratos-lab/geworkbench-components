package org.geworkbench.components.annotations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import org.geworkbench.util.ProgressTask;
import org.jfree.ui.SortableTableModel;

/**
 * CGITask: retrieve disease/agent association from CGI
 * $Id$
 */

public class CGITask extends ProgressTask<AgentDiseaseResults, String>{
    	private AnnotationsPanel2 ap = null;

    	public CGITask(int pbtype, String message, AnnotationsPanel2 ap2){
    		super(pbtype, message);
    		ap = ap2;
    	}

        @Override
        protected AgentDiseaseResults doInBackground() {
            GeneAnnotation geneAnnotation = new GeneAnnotationImpl();
            AgentDiseaseResults agentDiseaseResults = geneAnnotation.showAnnotation(this, ap.selectedMarkerInfo);
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
        protected void done() {
        	setProgress(100);
        	ap.pd.removeTask(this);
        	if (isCancelled()) return;

        	AgentDiseaseResults agentDiseaseResults = null;
        	try{
        		agentDiseaseResults = get();
        	}catch(ExecutionException e){
        		e.printStackTrace();
        	}catch(InterruptedException e){
        		e.printStackTrace();
        	}
        	if (agentDiseaseResults == null)
        		return;
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
            if (diseases.length+agents.length==0){
				JOptionPane
				.showMessageDialog(
						null,
						"Server does not have records about these markers, please try other markers.",
						"Server returns no records", JOptionPane.OK_OPTION);
				ap.clearTable("cgi");
				return;
            }
            for (int i = 0; i < ap.dropDownLists.length; i++) {
            	ap.dropDownLists[i].removeAllItems();
            	ap.dropDownLists[i].addItem("");
			}
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
            for (int i = 0; i < ap.dropDownLists.length; i++) {
            	ap.orderDropDownLists(ap.dropDownLists[i]);
			}
            appendDiseaseNumber(markers,diseases,ap.dropDownLists[2]);
            appendDiseaseNumber(markers2,agents,ap.dropDownLists[6]);

            ap.diseaseModel = new CGITableModel(ap, markers, genes, diseases, roles, sentences, pubmeds);
            ap.diseaseTableList.put(new Integer(ap.maSet.hashCode()), ap.diseaseModel);
            ap.diseaseTable.setSortableModel(ap.diseaseModel);
            ap.agentModel = new CGITableModel(ap, markers2, genes2, agents, agentRoles, agentSentences, agentPubmeds);
            ap.agentTableList.put(new Integer(ap.maSet.hashCode()), ap.agentModel);
            ap.agentTable.setSortableModel(ap.agentModel);

            ap.diseaseTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
            ap.diseaseTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
            ap.diseaseTable.getColumnModel().getColumn(2).setHeaderValue("     Disease");
            ap.diseaseTable.getColumnModel().getColumn(3).setHeaderValue("     Role");
            ap.diseaseTable.getColumnModel().getColumn(4).setHeaderValue("     Sentence");
            ap.diseaseTable.getColumnModel().getColumn(5).setHeaderValue("     Pubmed");
            ap.agentTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
            ap.agentTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
            ap.agentTable.getColumnModel().getColumn(2).setHeaderValue("     Agent");
            ap.agentTable.getColumnModel().getColumn(3).setHeaderValue("     Role");
            ap.agentTable.getColumnModel().getColumn(4).setHeaderValue("     Sentence");
            ap.agentTable.getColumnModel().getColumn(5).setHeaderValue("     Pubmed");
            ap.sortByNumberOfRecords = true;

            ap.agentTable.getTableHeader().repaint();
            ap.leftHeadPanel.setVisible(true);
            ap.rightHeadPanel.setVisible(true);
            ap.selectedTable = ap.diseaseTable;
            for (int j = 0; j < ((CGITableModel) ap.selectedTable.getModel()).size; j++) {
				((CGITableModel) ap.selectedTable.getModel()).collapseBy(ap.selectedTable,
						((GeneData) (((CGITableModel) ap.selectedTable.getModel())
								.getObject(j, CGITableModel.COL_GENE))).name,
						((DiseaseData) (((CGITableModel) ap.selectedTable
								.getModel()).getObject(j,
								CGITableModel.COL_DISEASE))).name);
			}
           ((CGITableModel) ap.selectedTable.getModel()).updateNumOfDuplicates();
           ((SortableTableModel)ap.selectedTable.getModel()).sortByColumn(2,false);
           ((SortableTableModel)ap.selectedTable.getModel()).sortByColumn(0,true);
           ap.selectedTable = ap.agentTable;
           for (int j = 0; j < ((CGITableModel) ap.selectedTable.getModel()).size; j++) {
				((CGITableModel) ap.selectedTable.getModel()).collapseBy(ap.selectedTable,
						((GeneData) (((CGITableModel) ap.selectedTable.getModel())
								.getObject(j, CGITableModel.COL_GENE))).name,
						((DiseaseData) (((CGITableModel) ap.selectedTable
								.getModel()).getObject(j,
								CGITableModel.COL_DISEASE))).name);
			}
            ((CGITableModel) ap.selectedTable.getModel()).updateNumOfDuplicates();
            ((SortableTableModel)ap.selectedTable.getModel()).sortByColumn(2,false);
            ((SortableTableModel)ap.selectedTable.getModel()).sortByColumn(0,true);
            ap.syncHeaderWidthWithTableWidth();

        	if (ap.userAlsoWantPathwayData &&(!ap.userAlsoWantCaBioData))
        		ap.jTabbedPane1.setSelectedComponent(ap.annotationPanel);
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
       private void appendDiseaseNumber(MarkerData[] markers2,
   			DiseaseData[] disease, JComboBox comboBox) {
   		int itemCount = comboBox.getItemCount();
       	String[] itemName = new String[itemCount];
   		//for each item in comboBox,
   		for (int i = 0; i < itemCount; i++) {
   			String diseaseName = (String)comboBox.getItemAt(i);
   			Set<String> markers = new HashSet<String>();
   			//check how many markers associate with it,
   			if (diseaseName.equals("")){
   				//it's the first one, we don't need to count.
   				itemName[i]=diseaseName;
   			}else{
   				int count = 0;
   				for (int j = 0; j < disease.length; j++) {
   					if (disease[j].name.equals(diseaseName)){
   						// we need to consider markers, if we already
   						// counted for this marker, we skip it.
   						String marker = markers2[j].name;
   						if (!markers.contains(marker)){
   							markers.add(marker);
   							count++;
   						}
   					}
   				}
   				//append the number
   				itemName[i]=diseaseName.concat(" ("+count+")");;
   			}
   		}
   		comboBox.removeAllItems();
       	for (int i = 0; i < itemCount; i++) {
       		comboBox.addItem(itemName[i]);
   		}
   	}
    }