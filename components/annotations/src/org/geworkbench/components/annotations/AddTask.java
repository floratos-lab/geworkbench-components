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

public class AddTask extends ProgressTask<GeneBase[], Void>{
	    private static Log log = LogFactory.getLog(AddTask.class);
    	private String label = null;
    	private String pathway = null;
    	private AnnotationsPanel2 ap = null;

    	/**
    	 * 
    	 * @param pbtype: progress bar type for constructing ProgressItem
    	 * @param message: initial message for constructing ProgressItem
    	 * @param ap2: AnnotationsPanel2
    	 * @param lb: pathway label 
    	 * @param pw: Pathway
    	 */
    	public AddTask(int pbtype, String message, AnnotationsPanel2 ap2, String lb, String pw){
    		super(pbtype, message);
    		ap = ap2;
    		label = lb;
    		pathway = pw;
    	}

    	@Override
    	protected GeneBase[] doInBackground(){
    		if (isCancelled()) return null;
    		BioDBnetClient client = new BioDBnetClient();
    		return client.queryGenesForPathway(pathway);
    	}

    	@Override
		protected void done(){
    		ap.pd.removeTask(this);
    		if (isCancelled()) return;
    		GeneBase[] genesInPathway = null;
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
            	GeneBase gene = genesInPathway[i];
                log.info(gene.getGeneSymbol() + " : " + gene.getGeneName());
                for (Object obj : ap.maSet.getMarkers()) {
                	DSGeneMarker marker = (DSGeneMarker) obj;
                    if (marker.getShortName().equalsIgnoreCase(gene.getGeneSymbol())) {
                        log.debug("Found " + gene.getGeneSymbol() + " in set.");
                        selectedMarkers.add(marker);
                       // break; Disabled it because there may be mutiple markers for the same gene.
                    }
                }
            }

            selectedMarkers.setActive(true);
            ap.publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(DSGeneMarker.class, selectedMarkers, SubpanelChangedEvent.SET_CONTENTS));
    	}
    	

    }