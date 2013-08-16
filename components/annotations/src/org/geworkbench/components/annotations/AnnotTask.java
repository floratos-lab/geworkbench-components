package org.geworkbench.components.annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.util.ProgressTask;

/**
 * AnnotTask: retrieve gene annotation and pathway data from bioDBnet.
 * $Id$
 */

public class AnnotTask extends ProgressTask<AnnotData, String> {
    	final private AnnotationsPanel2 ap;

    	public AnnotTask(int pbtype, String message, AnnotationsPanel2 ap2){
    		super(pbtype, message);
    		ap = ap2;
    	}

        @Override
        protected AnnotData doInBackground() {
        			DSItemList<DSGeneMarker> selectedMarkerInfo = ap.selectedMarkerInfo;
            		ArrayList<DSGeneMarker> markerData = new ArrayList<DSGeneMarker>();
            		ArrayList<GeneAnnotation> geneData = new ArrayList<GeneAnnotation>();

                    // selectedMarkerInfo has been checked not to be null or size 0
                    for (int i = 0; i < selectedMarkerInfo.size(); i++) {

                           	if (isCancelled()) return null;
                            publish("Getting Marker Annotation/Pathways: " + selectedMarkerInfo.get(i).getLabel());
                            setProgress(100 * (i+1)/selectedMarkerInfo.size());

                            String geneName = selectedMarkerInfo.get(i).getGeneName();
                    		BioDBnetClient client = new BioDBnetClient();
                    		GeneAnnotation[] annotations = client.queryByGeneSymbol(ap.humanOrMouse, geneName);

                            if (annotations == null ) {
                            	return null;
                            }

                            DSGeneMarker marker = selectedMarkerInfo.get(i);
							for (int j = 0; j < annotations.length; j++) {
								if (isCancelled())
									return null;
			
								geneData.add(annotations[j]);
								markerData.add(marker);
							}
                    }

					return new AnnotData(markerData.toArray(new DSGeneMarker[0]),
							geneData.toArray(new GeneAnnotation[0]));
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
    		// annotData can be null
    		ap.setTableModel(annotData);

    		if (annotData!=null && annotData.pathwayCount == 0)
				JOptionPane
						.showMessageDialog(
								null,
								"Server does not have records about these markers for this organism, please try other markers or organism.",
								"Server returns no records",
								JOptionPane.OK_OPTION);
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