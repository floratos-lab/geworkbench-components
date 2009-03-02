package org.geworkbench.components.mindy;

import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.regression.SimpleRegression;
import org.apache.commons.collections15.map.MultiKeyMap;
import org.apache.ojb.broker.transaction.tm.TransactionManagerFactoryFactory.TMFactoryFactory;

import edu.columbia.c2b2.mindy.MindyResults;

import java.text.CollationKey;
import java.text.Collator;
import java.util.*;
import java.io.Serializable;

/**
 * Class containing MINDY run results.
 * @author mhall
 */
public class MindyData implements Serializable {

    static Log log = LogFactory.getLog(MindyData.class);

    // all arrays,
    private CSMicroarraySet arraySet;
    // selected arrays
    private ArrayList<DSMicroarray> arrayForMindyRun;

	private DSGeneMarker transcriptionFactor;

    private List<MindyResultRow> data;
    private MultiKeyMap<DSGeneMarker, MindyResultRow> dataMap = new MultiKeyMap<DSGeneMarker, MindyResultRow>();


    private HashMap<DSGeneMarker, TargetInfo> targetInfoMap = new HashMap<DSGeneMarker, TargetInfo>();



    // can go to global key repository, maybe related to a file
    private HashMap<DSGeneMarker, MindyGeneMarker> geneSortkeyMap = new HashMap<DSGeneMarker, MindyGeneMarker>();

    private HashMap<DSGeneMarker, ModulatorStatistics> modulatorStatistics = new HashMap<DSGeneMarker, ModulatorStatistics>();

    private float setFraction;
    private boolean annotated = false;

	public MindyData(MindyResults results, CSMicroarraySet arraySet, ArrayList<DSMicroarray> arrayForMindyRun, float setFraction, DSGeneMarker transFac) {

        this.arraySet = arraySet;

        this.transcriptionFactor = transFac;

    	if ( arrayForMindyRun == null){
    		this.arrayForMindyRun = MindyData.createArrayForMindyRun(arraySet, null);
    	} else {
            this.arrayForMindyRun = arrayForMindyRun;
    	}

    	int numWithSymbols = 0;
		List<MindyResultRow> dataRows = new ArrayList<MindyResultRow>();

		// process mindy run results
		Collator myCollator = Collator.getInstance();
		for (MindyResults.MindyResultForTarget result : results) {
			DSItemList<DSGeneMarker> markers = arraySet.getMarkers();
			DSGeneMarker target = markers.get(result.getTarget().getName());

			// used to find out if annotations file was loaded
			// reminder: look at the class that does file processing.
			if (!StringUtils.isEmpty(target.getGeneName()))
				numWithSymbols++;

			for (MindyResults.MindyResultForTarget.ModulatorSpecificResult specificResult : result) {

				// process results with nonzero scores
				float score = specificResult.getScore();
				if (score != 0.0){
					addToSortkeyMap(myCollator, target);

					DSGeneMarker mod = markers.get(specificResult
							.getModulator().getName());

					// used to find out if annotations file was loaded
					if (!StringUtils.isEmpty(mod.getGeneName()))
						numWithSymbols++;

					addToSortkeyMap(myCollator, mod);

					double correlation = calcPearsonCorrelation(target);
					addToTargetInfoMap(correlation, target);

					// load data
					dataRows.add(new MindyResultRow(mod, target, score));
				}
			}
		}

	//

        this.data = dataRows;
        this.setFraction = setFraction;

        calculateModulatorStatistics(false);

		if (numWithSymbols > 0)
			setAnnotated(true);

	}

	/**
	 * used for sorting
	 *
	 * @param myCollator
	 * @param target
	 */
	private void addToSortkeyMap(Collator myCollator, DSGeneMarker target) {
		if (!geneSortkeyMap.containsKey(target)) {
			geneSortkeyMap.put(target, new MindyGeneMarker(target, myCollator
					.getCollationKey(target.getLabel()), myCollator
					.getCollationKey(target.getDescription())));
		}
	}

	/**
	 * used for sorting
	 *
	 * @param myCollator
	 * @param target
	 */
	private void addToTargetInfoMap(double correlation, DSGeneMarker target) {
		if (!targetInfoMap.containsKey(target)) {
			targetInfoMap.put(target, new TargetInfo(target, correlation));
		}
	}

	/**
	 * Pearson correlation between target and TF
	 *
	 * @param target
	 */
	private double calcPearsonCorrelation(DSGeneMarker target) {
		SimpleRegression sr = new SimpleRegression();

		int sizeArraySet = this.arraySet.size();
		for(int i = 0; i < sizeArraySet; i++){
			DSMicroarray ma = (DSMicroarray)this.arraySet.get(i);
			sr.addData(ma.getMarkerValue(target).getValue(), ma
					.getMarkerValue(getTranscriptionFactor())
					.getValue());
		}

		return sr.getR();
	}


    /**
     * Constructor.
     *
     * @param arraySet - microarray set
     * @param arrayForMindyRun - microarray set that was actually passed to mindy run,
     * 							if null assuming that mindy were run with all arrays
     * @param data - list of MINDY result rows
     * @param setFraction - Sample per Condition in fraction
     */
/*    public MindyData(CSMicroarraySet arraySet, ArrayList<DSMicroarray> arrayForMindyRun, List<MindyResultRow> data, float setFraction) {

        this.arraySet = arraySet;

    	if ( arrayForMindyRun == null){
    		this.arrayForMindyRun = MindyData.createArrayForMindyRun(arraySet, null);
    	} else {
            this.arrayForMindyRun = arrayForMindyRun;
    	}

        this.data = data;
        this.setFraction = setFraction;
        if (data.size() > 0) {
            this.transcriptionFactor = data.get(0).getTranscriptionFactor();
        } else {
            log.warn("Data passed in had 0 records, unable to determine transcription factor under consideration.");
        }
        calculateModulatorStatistics(false);
    }

*/	public static ArrayList<DSMicroarray> createArrayForMindyRun(
			DSMicroarraySet<DSMicroarray> inSet, DSPanel arraySet) {
		ArrayList<DSMicroarray> arrayListForMindyRun = new ArrayList<DSMicroarray>();

		if ((arraySet != null) && (arraySet.size() > 0)) {
			int size = arraySet.size();
			for (int i = 0; i < size; i++) {
				DSMicroarray ma = (DSMicroarray) arraySet.get(i);
				arrayListForMindyRun.add(ma);
			}
		} else {
			for (DSMicroarray microarray : inSet) {
				arrayListForMindyRun.add(microarray);
			}
		}

		return arrayListForMindyRun;
	}

    public boolean isAnnotated(){
    	return this.annotated;
    }

    public void setAnnotated(boolean annotated){
    	this.annotated = annotated;
    }

    /**
     * Get the microarray set.
     *
     * @return microarray set
     */
    public CSMicroarraySet getArraySet() {
        return arraySet;
    }

    /**
     * Set the specified microarray set to MINDY data.
     *
     * @param arraySet - the microarray set to associate with MINDY data
     */
    public void setArraySet(CSMicroarraySet arraySet) {
        this.arraySet = arraySet;
    }

    public ArrayList<DSMicroarray> getArrayForMindyRun() {
		return arrayForMindyRun;
	}

    public ArrayList<MindyGeneMarker> convertToMindyGeneMarker(List<DSGeneMarker> list){
    	ArrayList<MindyGeneMarker> result = new ArrayList<MindyGeneMarker>(list.size());
    	for(DSGeneMarker m: list){
    		result.add(this.geneSortkeyMap.get(m));
    	}
    	return result;
    }

    public ArrayList<DSGeneMarker> convertToDSGeneMarker(List<MindyGeneMarker> list){
    	ArrayList<DSGeneMarker> result = new ArrayList<DSGeneMarker>(list.size());
    	for(MindyGeneMarker m: list){
    		result.add(m.getGeneMarker());
    	}
    	return result;
    }

    /**
     * Get the MINDY result rows.
     *
     * @return MINDY result rows
     */
    public List<MindyResultRow> getData() {
        return data;
    }

    /**
     * Set the MINDY result rows associated with MINDY data.
     *
     * @param data - the list of MINDY result rows to associate with MINDY data.
     */
    public void setData(List<MindyResultRow> data) {
        this.data = data;
        calculateModulatorStatistics(true);
    }

    /**
     * Get the statics for the specified modulator.
     *
     * @param modulator - modulator for which to get the statistics
     * @return - ModulatorStatistics object
     */
    public ModulatorStatistics getStatistics(DSGeneMarker modulator) {
        return modulatorStatistics.get(modulator);
    }

    /**
     * Get the transcription factor specified for MINDY data.
     *
     * @return the transcription factor gene marker
     */
    public DSGeneMarker getTranscriptionFactor() {
        return transcriptionFactor;
    }

    /**
     * Get the fraction of the sample to display on the heat map.
     *
     * @return fraction of the sample to display
     */
    public float getSetFraction(){
    	return this.setFraction;
    }

    /**
     * Set the transcription factor for the MINDY data.
     *
     * @param transcriptionFactor
     */
    public void setTranscriptionFactor(DSGeneMarker transcriptionFactor) {
        this.transcriptionFactor = transcriptionFactor;
    }

    /**
     * Get the list of mondulators.
     *
     * @return list of modulators
     */
    public List<DSGeneMarker> getModulators() {
        ArrayList<DSGeneMarker> modulators = new ArrayList<DSGeneMarker>();
        for (Map.Entry<DSGeneMarker, ModulatorStatistics> entry : modulatorStatistics.entrySet()) {
            modulators.add(entry.getKey());
        }
        return modulators;
    }

    /**
     * Get the list of MINDY result rows based on specified modulator and transcription factor.
     *
     * @param modulator
     * @param transFactor - transcription factor
     * @return list of MINDY result rows
     */
    public List<MindyResultRow> getRows(DSGeneMarker modulator, DSGeneMarker transFactor) {
        List<MindyResultRow> results = new ArrayList<MindyResultRow>();
        for (MindyResultRow mindyResultRow : data) {
            if (mindyResultRow.getModulator().equals(modulator))
            {
                results.add(mindyResultRow);
            }
        }
        return results;
    }

    public boolean isEmpty() {
		if (data.size() <= 0) {
			return true;
		} else {
			return false;
		}
    }

    /**
     * Get the list of MINDY result rows based on specified modulator, transcription factor, and target marker set.
     *
     * @param modulator
     * @param transFactor - transcription factor
     * @param limitTargets - target marker set being displayed
     * @return list of MINDY result rows
     */
    public List<MindyResultRow> getRows(DSGeneMarker modulator, DSGeneMarker transFactor, List<DSGeneMarker> limitTargets) {
        List<MindyResultRow> results = new ArrayList<MindyResultRow>();
        for (MindyResultRow mindyResultRow : data) {
            if (mindyResultRow.getModulator().equals(modulator) )
            {
                if (limitTargets != null && limitTargets.contains(mindyResultRow.getTarget())) {
                    results.add(mindyResultRow);
                }
            }
        }
        return results;
    }


    /**
     * @param recalculate
     *
     * code looks suspiciously wrong when recalculate is true, but will be ok if one row per modulator
     * but it never get called with true, change later
     * We don't need no ... arguments!
     * also modifying dataMap looks wrong - Oleg
     */
    private void calculateModulatorStatistics(boolean recalculate) {
        log.debug("Calculating modulator stats...");
        for (MindyResultRow row : data) {
            dataMap.put(row.getModulator(), getTranscriptionFactor(), row.getTarget(), row);
            ModulatorStatistics modStats = modulatorStatistics.get(row.getModulator());
            if (recalculate){
            	modStats = null;
            }
            if (modStats == null) {
                modStats = new ModulatorStatistics(0, 0, 0);
                modulatorStatistics.put(row.getModulator(), modStats);
            }

            if (row.getScore() < 0) {
                modStats.munder++;
                modStats.count++;
            } else if(row.getScore() > 0){
                modStats.mover++;
                modStats.count++;
            }
        }
        log.debug("Done calculating modulator stats.");
    }

    /**
     * Pearson correlation between the transcription factor and the target gene.
     * Used primarily for the heat map.
     * @return result of Pearson correlation
     */
    public double getCorrelation(DSGeneMarker target){
    	return this.targetInfoMap.get(target).getCorrelation();
    }

    /**
     * Gene name CollationKey that is used for sorting
     *
     * @return CollationKey
     */
    public CollationKey getGeneNameSortKey(DSGeneMarker target){
    	return this.geneSortkeyMap.get(target).getNameSortKey();
    }

    /**
     * Get a hash map storing modulators and their statistics.
     *
     * @return the hash map
     */
    public HashMap<DSGeneMarker, ModulatorStatistics> getAllModulatorStatistics() {
        return modulatorStatistics;
    }

    /**
     * Get a list of targets based on specified modulator and transcription factor.
     *
     * @param modulator
     * @param transcriptionFactor
     * @return list of targets
     */
    public List<DSGeneMarker> getTargets(DSGeneMarker modulator, DSGeneMarker transcriptionFactor) {
        List<DSGeneMarker> targets = new ArrayList<DSGeneMarker>();
        List<MindyResultRow> rows = getRows(modulator, transcriptionFactor);
        for (MindyResultRow mindyResultRow : rows) {
            targets.add(mindyResultRow.getTarget());
        }
        return targets;
    }

    /**
     * Get the score of the specified modulator, transcription factor and target.
     *
     * @param modulator
     * @param transcriptionFactor
     * @param target
     * @return the score used in MINDY data
     */
    public float getScore(DSGeneMarker modulator, DSGeneMarker transcriptionFactor, DSGeneMarker target) {
        MindyResultRow row = dataMap.get(modulator, transcriptionFactor, target);
        if (row == null) {
            return 0;
        } else {
            return row.getScore();
        }
    }

    /**
     * Get the MINDY result row that has the specified modulator, transcription factor, and target.
     *
     * @param modulator
     * @param transcriptionFactor
     * @param target
     * @return MINDY result row
     */
    public MindyResultRow getRow(DSGeneMarker modulator, DSGeneMarker transcriptionFactor, DSGeneMarker target) {
        return dataMap.get(modulator, transcriptionFactor, target);
    }

//////////////////////////////////////////////////////////////////////////////////////
///    refactoring stuff that is here for legacy code and should eventually go away or changed
////////////////////////////////////////////////////////////////////////


    // tmp for refactoring two ctr, just for compiling
    // used by MindyResultsParser, probably testing
    /**
     * Constructor.
     *
     * @param arraySet - microarray set
     * @param data - list of MINDY result rows
     * @param setFraction - Sample per Condition in fraction
     */
    public MindyData(CSMicroarraySet arraySet, List<MindyResultRow> data, float setFraction) {
        this.arraySet = arraySet;
        this.data = data;
        this.setFraction = setFraction;
        calculateModulatorStatistics(false);


    }

    public MindyData(CSMicroarraySet arraySet, List<MindyResultRow> data) {
        this.arraySet = arraySet;
        this.data = data;
        calculateModulatorStatistics(false);
    }

    // end - tmp for refactoring two ctr


}
