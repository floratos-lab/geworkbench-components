package org.geworkbench.components.mindy;

import java.util.Comparator;
import org.geworkbench.bison.datastructure.bioobjects.markers.*;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;

public class GeneMarkerListComparator implements Comparator<DSGeneMarker> {
	// modes
    public static final int SHORT_NAME = 1;
    public static final int M_POUND = 2;
    public static final int M_PLUS = 3;
    public static final int M_MINUS = 4;
    public static final int DESCRIPTION = 5;
    public static final int SCORE = 6;
    public static final int MODE = 7;
    
    // variables
    private MindyData md;
    private int mode;
    private boolean ascending;
    private DSGeneMarker modulator;
	
	public GeneMarkerListComparator(MindyData md, int mode, boolean ascending){
		this.md = md;
		this.mode = mode;
		this.ascending = ascending;
	}
	
	public GeneMarkerListComparator(MindyData md, DSGeneMarker modulator, int mode, boolean ascending){
		this(md, mode, ascending);
		this.modulator = modulator;
	}

	public int compare(DSGeneMarker x, DSGeneMarker y) {
        switch(mode){
        case SHORT_NAME:
        	if(ascending)
        		return x.getShortName().compareTo(y.getShortName());
        	else
        		return y.getShortName().compareTo(x.getShortName());
        case M_POUND:
        	if(ascending)
        		return new Integer(md.getStatistics(x).getCount()).compareTo(new Integer(md.getStatistics(y).getCount()));
        	else
        		return new Integer(md.getStatistics(y).getCount()).compareTo(new Integer(md.getStatistics(x).getCount()));
        case M_PLUS:
        	if(ascending)
        		return new Integer(md.getStatistics(x).getMover()).compareTo(new Integer(md.getStatistics(y).getMover()));
        	else
        		return new Integer(md.getStatistics(y).getMover()).compareTo(new Integer(md.getStatistics(x).getMover()));
        case M_MINUS:
        	if(ascending)
        		return new Integer(md.getStatistics(x).getMunder()).compareTo(new Integer(md.getStatistics(y).getMunder()));
        	else
        		return new Integer(md.getStatistics(y).getMunder()).compareTo(new Integer(md.getStatistics(x).getMunder()));
        case DESCRIPTION:
        	if(ascending)
        		return x.getDescription().compareTo(y.getDescription());
        	else
        		return y.getDescription().compareTo(x.getDescription());
        case SCORE:
        	if(modulator != null){
        		if(ascending)
        			return Float.compare(md.getScore(modulator, md.getTranscriptionFactor(), x), md.getScore(modulator, md.getTranscriptionFactor(), y));
        		else
        			return Float.compare(md.getScore(modulator, md.getTranscriptionFactor(), y), md.getScore(modulator, md.getTranscriptionFactor(), x));
        	}
        	break;
        case MODE:
        	int xmover = md.getStatistics(x).getMover();
        	int xmunder = md.getStatistics(x).getMunder();
        	int ymover = md.getStatistics(y).getMover();
        	int ymunder = md.getStatistics(y).getMunder();
        	int xmode = 0;
        	int ymode = 0;
        	if(xmover > xmunder) xmode = 1;
        	else if(xmover < xmunder) xmode = -1;
        	else xmode = 0;
        	if(ymover > ymunder) ymode = 1;
        	else if(ymover < ymunder) ymode = -1;
        	else ymode = 0;        	
        	if(ascending)
        		return new Integer(xmode).compareTo(new Integer(ymode));
        	else
        		return new Integer(ymode).compareTo(new Integer(xmode));
        }
		return 0;
	}

}
