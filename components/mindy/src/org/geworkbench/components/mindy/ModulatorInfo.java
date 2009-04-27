package org.geworkbench.components.mindy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

/**
 * @author oshteynb
 * @version $Id: ModulatorInfo.java,v 1.3 2009-04-27 15:49:02 keshav Exp $
 *
 */
public class ModulatorInfo {
    public ModulatorInfo(DSGeneMarker modulator) {
		this.modulator = modulator;
	}

    public void insertRow(MindyResultRow row) {
    	data.add(row);

    	targetResultMap.put(row.getTarget(), row);

        // calculate modulator statistics
        if (row.getScore() < 0) {
            modStat.munder++;
            modStat.count++;
        } else if(row.getScore() > 0){
            modStat.mover++;
            modStat.count++;
        }

    }

	public ModulatorStatistics getModStat() {
		return modStat;
	}

    public List<MindyResultRow> getData() {
		return data;
	}

    public float getScore(DSGeneMarker target) {
    	MindyResultRow row = getRow(target);

    	float result;
        if (row == null) {
        	result = 0;
        } else {
        	result = row.getScore();
        }

        return result;
    }

	/**
	 * @param target
	 * @return MindyResultRow, null if not there
	 */
	public MindyResultRow getRow(DSGeneMarker target) {
		MindyResultRow row = targetResultMap.get(target);
		return row;
	}

	private DSGeneMarker modulator;
    private ModulatorStatistics modStat= new ModulatorStatistics(0, 0, 0);;

    private List<MindyResultRow> data = new ArrayList<MindyResultRow>();
	private HashMap<DSGeneMarker, MindyResultRow> targetResultMap = new HashMap<DSGeneMarker, MindyResultRow>();

}
