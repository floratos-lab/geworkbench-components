package org.geworkbench.components.annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.util.ProgressTask;

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
            		ArrayList<DSGeneMarker> markerData = new ArrayList<DSGeneMarker>();
            		ArrayList<GeneAnnotation> geneData = new ArrayList<GeneAnnotation>();
            		ArrayList<String> pathwayData = new ArrayList<String>();
                    if (selectedMarkerInfo != null) {

                        for (int i = 0; i < selectedMarkerInfo.size(); i++) {

                           	if (isCancelled()) return null;
                            publish("Getting Marker Annotation/Pathways: " + selectedMarkerInfo.get(i).getLabel());
                            setProgress(100 * (i+1)/selectedMarkerInfo.size());

                            String geneName = selectedMarkerInfo.get(i).getGeneName();
                    		BioDBnetClient client = new BioDBnetClient();
                    		GeneAnnotation[] annotations = client.queryByGeneSymbol(ap.humanOrMouse, geneName);

                            if (annotations == null ) {
                                ap.clearTable();
                            	return null;
                            }

                            DSGeneMarker marker = selectedMarkerInfo.get(i);
                            if ( annotations.length > 0) {
                                for (int j = 0; j < annotations.length; j++) {
                                   	if (isCancelled()) return null;

                                    String[] pways = annotations[j].getPathways();
                                    String[] temp = new String[ap.pathways.length + pways.length];
                                    System.arraycopy(ap.pathways, 0, temp, 0, ap.pathways.length);
                                    System.arraycopy(pways, 0, temp, ap.pathways.length, pways.length);
                                    ap.pathways = temp;
                                    if (pways.length > 0) {
                                        for (int k = 0; k < pways.length; k++) {
                                            pathwayData.add(pways[k]);
                                            geneData.add(annotations[j]);
                                            markerData.add(marker);
                                        }
                                    }
                                    else {
                                        pathwayData.add(null);
                                        geneData.add(annotations[j]);
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
    		DSGeneMarker[] markers = annotData.markerData.toArray(new DSGeneMarker[0]);
            GeneAnnotation[] genes = annotData.geneData.toArray(new GeneAnnotation[0]);
            String[] pathways = annotData.pathwayData.toArray(new String[0]);
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
        }
        
        @Override
        protected void process(List<String> chunks){
        	for (String message : chunks){
        		if (isCancelled()) return;
        		this.setMessage(message);
            	ap.pd.updateWidth(message);
        	}
        }
    }