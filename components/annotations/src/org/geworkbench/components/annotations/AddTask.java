package org.geworkbench.components.annotations;

import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.ProgressTask;

/**
 * AddTask: add pathway genes to set
 * $Id$
 */

public class AddTask extends ProgressTask<GeneAnnotation[], Void>{
	    private static Log log = LogFactory.getLog(AddTask.class);
    	private String label = null;
    	private Pathway pathway = null;
    	private AnnotationsPanel2 ap = null;

    	/**
    	 * 
    	 * @param pbtype: progress bar type for constructing ProgressItem
    	 * @param message: initial message for constructing ProgressItem
    	 * @param ap2: AnnotationsPanel2
    	 * @param lb: pathway label 
    	 * @param pw: Pathway
    	 */
    	public AddTask(int pbtype, String message, AnnotationsPanel2 ap2, String lb, Pathway pw){
    		super(pbtype, message);
    		ap = ap2;
    		label = lb;
    		pathway = pw;
    	}

    	@Override
    	protected GeneAnnotation[] doInBackground(){
    		if (isCancelled()) return null;
    		GeneAnnotation[] genesInPathway = ap.criteria.getGenesInPathway(pathway);
    		return genesInPathway;
    	}

    	@Override
		protected void done(){
    		ap.pd.removeTask(this);
    		if (isCancelled()) return;
    		GeneAnnotation[] genesInPathway = null;
    		try{
    			genesInPathway = get();
    		}catch(ExecutionException e){
    			e.printStackTrace();
    		}catch(InterruptedException e){
    			e.printStackTrace();
    		}
    		if ( genesInPathway == null )
            	return;
            DSPanel<DSGeneMarker> selectedMarkers = new CSPanel<DSGeneMarker>(label, label);
            for (int i = 0; i < genesInPathway.length; i++) {
                GeneAnnotation geneAnnotation = genesInPathway[i];
                log.info(geneAnnotation.getGeneSymbol() + " : " + geneAnnotation.getGeneName());
                for (Object obj : ap.maSet.getMarkers()) {
                	DSGeneMarker marker = (DSGeneMarker) obj;
                    if (marker.getShortName().equalsIgnoreCase(geneAnnotation.getGeneSymbol())) {
                        log.debug("Found " + geneAnnotation.getGeneSymbol() + " in set.");
                        selectedMarkers.add(marker);
                       // break; Disabled it because there may be mutiple markers for the same gene.
                    }
                }
            }

            selectedMarkers.setActive(true);
            ap.publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(DSGeneMarker.class, selectedMarkers, SubpanelChangedEvent.SET_CONTENTS));
    	}
    	

    }