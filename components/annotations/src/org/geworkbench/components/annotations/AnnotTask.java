package org.geworkbench.components.annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.util.ProgressTask;
import org.geworkbench.util.annotation.Pathway;

/**
 * AnnotTask: retrieve gene annotation and pathway data from CGAP
 * $Id$
 */

public class AnnotTask extends ProgressTask<AnnotData, String> {
    	private AnnotationsPanel2 ap = null;

    	public AnnotTask(int pbtype, String message, AnnotationsPanel2 ap2){
    		super(pbtype, message);
    		ap = ap2;
    	}

        @Override
        protected AnnotData doInBackground() {
        			DSItemList<DSGeneMarker> selectedMarkerInfo = ap.selectedMarkerInfo;
            		ArrayList<MarkerData> markerData = new ArrayList<MarkerData>();
            		ArrayList<GeneData> geneData = new ArrayList<GeneData>();
            		ArrayList<PathwayData> pathwayData = new ArrayList<PathwayData>();
                    if (selectedMarkerInfo != null) {
                        //pb.setTitle("Querying caBIO..");
                        //if (!pd.isActive()) {
                        //}
                        if (ap.criteria == null) {
                            try {
                    			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                                ap.criteria = new GeneSearchCriteriaImpl();
                            } catch (Exception e) {
                       			JOptionPane.showMessageDialog(null, 
                    					"Exception: could not create caBIO search criteria in Annotation Panel.\nIt could be connection error. Please check your internet connection or try again later.",
                    					"Data processing/connection error", JOptionPane.ERROR_MESSAGE);
                    			return null;
                            }
                        }

                        for (int i = 0; i < selectedMarkerInfo.size(); i++) {

                           	if (isCancelled()) return null;
                            publish("Getting Marker Annotation/Pathways: " + selectedMarkerInfo.get(i).getLabel());
                            setProgress(100 * (i+1)/selectedMarkerInfo.size());

                            String geneName = selectedMarkerInfo.get(i).getGeneName();
                            GeneAnnotation[] annotations;
                            annotations = ap.criteria.searchByName(geneName, ap.humanOrMouse);

                            if (annotations == null ) {
                                ap.clearTable("annot");
                            	return null;
                            }

                            MarkerData marker = new MarkerData(selectedMarkerInfo.get(i),"");
                            if ( annotations.length > 0) {
                                for (int j = 0; j < annotations.length; j++) {
                                   	if (isCancelled()) return null;

                                    Pathway[] pways = annotations[j].getPathways();
                                    Pathway[] temp = new Pathway[ap.pathways.length + pways.length];
                                    System.arraycopy(ap.pathways, 0, temp, 0, ap.pathways.length);
                                    System.arraycopy(pways, 0, temp, ap.pathways.length, pways.length);
                                    ap.pathways = temp;
                                    GeneData gene = new GeneData(annotations[j]);
                                    if (pways.length > 0) {
                                        for (int k = 0; k < pways.length; k++) {
                                            pathwayData.add(new PathwayData(pways[k].getPathwayName(), pways[k]));
                                            gene.setOrganism(annotations[j].getOrganismAbbreviation());
                                            geneData.add(gene);
                                            markerData.add(marker);
                                        }
                                    }
                                    else {
                                        pathwayData.add(new PathwayData("", null));
                                        geneData.add(gene);
                                        markerData.add(marker);
                                    }
                                }
                            }
                        }
                    }
                    return new AnnotData(markerData, geneData, pathwayData);
        }

        @Override
        protected void done() {
        	setProgress(100);
        	ap.pd.removeTask(this);
        	if (isCancelled()) return;
        	
    		AnnotData annotData = null;
    		try{
    			annotData = get();
    		}catch(ExecutionException e){
    			e.printStackTrace();
    		}catch(InterruptedException e){
    			e.printStackTrace();
    		}
    		if ( annotData == null )
            	return;
        	MarkerData[] markers = annotData.markerData.toArray(new MarkerData[0]);
            GeneData[] genes = annotData.geneData.toArray(new GeneData[0]);
            PathwayData[] pathways = annotData.pathwayData.toArray(new PathwayData[0]);
            if (pathways.length == 0)
				JOptionPane
						.showMessageDialog(
								null,
								"Server does not have records about these markers for this organism, please try other markers or organism.",
								"Server returns no records",
								JOptionPane.OK_OPTION);

            ap.annotationModel = new AnnotationTableModel(ap, markers, genes, pathways);
            ap.annotationTableList.put(new Integer(ap.maSet.hashCode()),  ap.annotationModel);
            ap.annotationTable.setSortableModel(ap.annotationModel);
            ap.annotationTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
            ap.annotationTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
            ap.annotationTable.getColumnModel().getColumn(2).setHeaderValue("     Pathway");
            ap.annotationTable.getTableHeader().repaint();
            if ((!ap.userAlsoWantPathwayData) && ap.userAlsoWantCaBioData)
            	ap.jTabbedPane1.setSelectedComponent(ap.cgiPanel);
        }
        
        @Override
        protected void process(List<String> chunks){
        	for (String message : chunks){
        		if (isCancelled()) return;
        		pb.setMessage(message);
            	ap.pd.updateWidth(message);
        	}
        }
    }