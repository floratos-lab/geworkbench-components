package org.geworkbench.components.mindy;

import java.util.Comparator;

import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;

public class MindyRowComparator implements Comparator<MindyData.MindyResultRow> {
	// mode
	public static final int SCORE = 1;
    
	// variable
    private MindyData md;
    private int mode;
    private boolean ascending;
    
    public MindyRowComparator(MindyData md, int mode, boolean ascending){
    	this.md = md;
    	this.mode = mode;
    	this.ascending = ascending;
    }

	public int compare(MindyData.MindyResultRow x, MindyData.MindyResultRow y) {
        switch(mode){
        case SCORE:
        	if(ascending)
        		return Float.compare(x.getScore(), y.getScore());
        	else
        		return Float.compare(y.getScore(), x.getScore());
        }
		return 0;
	}

}
