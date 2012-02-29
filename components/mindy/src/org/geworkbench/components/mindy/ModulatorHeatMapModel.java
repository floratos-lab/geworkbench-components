package org.geworkbench.components.mindy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.util.colorcontext.ColorContext;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyResultRow;

/**
 * @author oshteynb
 * @version $Id: ModulatorHeatMapModel.java,v 1.3 2009-04-29 19:55:33 oshteynb Exp $
 *
 */
public class ModulatorHeatMapModel {
	static Log log = LogFactory.getLog(ModulatorHeatMapModel.class);

    private DSGeneMarker modulator;
	private DSGeneMarker transcriptionFactor;

	private MindyData mindyData;
    private List<MindyResultRow> targetRows;

	private float setFractionPercent;
    private ColorContext colorContext;

    private ArrayList<DSMicroarray> sortedPerMod = null;
    private ArrayList<DSMicroarray> half1 = null;
    private ArrayList<DSMicroarray> half2 = null;

    public ModulatorHeatMapModel(DSGeneMarker modulator, MindyData mindyData) {
		this.mindyData = mindyData;
		this.transcriptionFactor = mindyData.getTranscriptionFactor();
		this.colorContext = (ColorContext) mindyData.getArraySet().getObject(ColorContext.class);

		updateModulator(modulator);
	}

	/**
	 * set modulator and update related data, for internal usage by ctr and setModulator
	 * setModulator will fire events if needed.
	 *
	 * @param modulator
	 */
	private void updateModulator(DSGeneMarker modulator) {
		this.modulator = modulator;
	    setTargetRows(this.mindyData.getRows(this.modulator));
        createArraysForHeatMap();
	}

	/**
	 *
	 * set rows
	 * sort rows by pearson correlation
	 *
	 */
	private void setTargetRows(List<MindyResultRow> targetRows) {
		if (targetRows != null){
			this.targetRows = targetRows;
	        Collections.sort(this.targetRows, new MindyRowComparator(MindyRowComparator.PEARSON_CORRELATION, false, this.mindyData));
		} else {
			/* revisit later */
			this.targetRows = new ArrayList<MindyResultRow>();
		}
	}

    public void setModulator(DSGeneMarker modulator) {
    	updateModulator(modulator);
	}

	public float getSetFraction() {
        return mindyData.getSetFraction();
    }

    public ColorContext getColorContext() {
        return colorContext;
    }

    public List<DSGeneMarker> getTargets() {
        List<DSGeneMarker> markers = this.mindyData.getTargets(this.modulator);
        return markers;
    }

    public DSGeneMarker getModulator() {
		return modulator;
	}

    public float getSetFractionPercent() {
		return setFractionPercent;
	}

	public ArrayList<DSMicroarray> getSortedPerMod() {
		return sortedPerMod;
	}

	public ArrayList<DSMicroarray> getHalf1() {
		return half1;
	}

	public ArrayList<DSMicroarray> getHalf2() {
		return half2;
	}

	public DSGeneMarker getTranscriptionFactor() {
		return transcriptionFactor;
	}

    public java.util.List<MindyResultRow> getTargetRows() {
		return targetRows;
	}

    public boolean isAnnotated() {
        return mindyData.isAnnotated();
    }

    public void limitMarkers(List<DSGeneMarker> limitList) {
		List<MindyResultRow> tmpRows = mindyData.getRows(modulator, limitList);
		setTargetRows(tmpRows);
	}

    /**
     *      create arrays:
     *  sortedPerMod
     *  half1
     *  half2
     *
     *      hopefully will get from mindy.jar currently getting them from mindyData:
     *
     * @param modulator
     * @param transcriptionFactor, probably not needed but just to show that calculations depends on it
     */
    private void createArraysForHeatMap() {
		// Extract and sort set based on modulator
        sortedPerMod = this.mindyData.getArrayForMindyRun();
        Collections.sort(sortedPerMod, new MicroarrayMarkerPositionComparator(modulator.getSerial()
        		, MicroarrayMarkerPositionComparator.EXPRESSION_VALUE
        		,  true));

        // Sort half sets based on trans factor
        int size = sortedPerMod.size()/2;
        // For odd number of arrays, cut out the array in the middle (i.e. the overlapping array)
        // -1 means even number of arrays
        int oddNumberCutout = -1;
        if((sortedPerMod.size() % 2) != 0){
        	oddNumberCutout = (int) sortedPerMod.size()/2;
        }
        // stop index for the L- array
        int stopIndex = (int) Math.round(size * this.mindyData.getSetFraction() * 2);
        if(stopIndex > size) stopIndex = size;
        // start index for the L+ array
        int startIndex = sortedPerMod.size() - ((int) Math.round(size * this.mindyData.getSetFraction() * 2));
        if(startIndex < 0) startIndex = 0;
        half1 = new ArrayList<DSMicroarray>(stopIndex);
        half2 = new ArrayList<DSMicroarray>(stopIndex);
        int count = 0;
        for(DSMicroarray ma : sortedPerMod){
        	if(count < size){
        		if((count != oddNumberCutout) && (count < stopIndex)){
        			half1.add(ma);
        		}
        	} else {
        		if((count != oddNumberCutout) && (count >= startIndex)){
        			half2.add(ma);
        		}
        	}
        	count++;
        }
        half1.trimToSize();
        half2.trimToSize();
        Collections.sort(half1, new MicroarrayMarkerPositionComparator(transcriptionFactor.getSerial()
        		, MicroarrayMarkerPositionComparator.EXPRESSION_VALUE
        		, true));
        Collections.sort(half2, new MicroarrayMarkerPositionComparator(transcriptionFactor.getSerial()
        		, MicroarrayMarkerPositionComparator.EXPRESSION_VALUE
        		, true));
	}
}
