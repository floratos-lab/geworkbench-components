package org.geworkbench.components.annotations;

import java.util.HashMap;

import org.geworkbench.util.ProgressTask;

/**
 * DupTask: updateNumOfDuplicates
 * $Id$
 */

public class DupTask extends ProgressTask<Void, Void>{
    	private CGITableModel ctm = null;

    	public DupTask(int pbtype, String message, CGITableModel ctm2){
    		super(pbtype, message);
    		ctm = ctm2;
    	}

    	@Override
    	protected Void doInBackground(){
        	ctm.numOfDuplicatesMap = new HashMap<String, Integer>();
        	ctm.numOfDuplicatesArray = new int[ctm.size];
    		for (int i = 0; i < ctm.markerData.length; i++) {
    			if (isCancelled()) return null;
    			String key = ctm.geneData[ctm.indices[i]].name+ctm.diseaseData[ctm.indices[i]].name;
    			Integer count = ctm.numOfDuplicatesMap.get(key);
    			if (count==null)
    				count=1;
    			else
    				count = count + 1;
    			ctm.numOfDuplicatesMap.put(key, count);
    		}
    		for (int i = 0; i < ctm.markerData.length; i++) {
    			if (isCancelled()) return null;
    			String key = ctm.geneData[ctm.indices[i]].name+ctm.diseaseData[ctm.indices[i]].name;
    			ctm.numOfDuplicatesArray[i] = ctm.numOfDuplicatesMap.get(key);
    		}
    		return null;	
    	}
    	@Override
    	protected void done(){
    		ctm.annotationsPanel.pd.removeTask(this);
    	}
    }
