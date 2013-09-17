package org.geworkbench.components.normalization;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.CSMarkerVector;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSGenepixMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.NormalizingAnalysis;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.engine.management.Subscribe;
import org.jfree.util.Log;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Columbia University.</p>
 * @author Xiaoqing Zhang
 * @version $Id$
 */

/**
 * Normalization the expression values based on the assumption that the expression level of housekeeping genes
 * are constant in different experiments.
 */
public class HouseKeepingGeneNormalizer extends AbstractAnalysis implements
        NormalizingAnalysis {
	private static final long serialVersionUID = -66936300682683764L;

    private TreeSet<String> nonFoundGenes = new TreeSet<String>();

    private boolean haveNonExistMarker = false;
    
    // these are used for data passing for the case of genepix
    private transient double[] ch1fArray = null;
    private transient double[] ch1bArray = null;
    private transient double[] ch2fArray = null;
    private transient double[] ch2bArray = null;
    
    private boolean ignoreMissingValues = true;
    private HouseKeepingGeneNormalizerPanel houseKeepingGeneNormalizerPanel = new HouseKeepingGeneNormalizerPanel();

    public HouseKeepingGeneNormalizer() {
        setDefaultPanel(houseKeepingGeneNormalizerPanel);
    }

    public int getAnalysisType() {
        return HOUSEKEEPINGGENES_VALUE_NORMALIZER_TYPE;
    }

	public AlgorithmExecutionResults execute(Object input) {
        if (input == null || !(input instanceof DSMicroarraySet)) {
            return new AlgorithmExecutionResults(false, "Invalid input.", null);
        }
        
        DSMicroarraySet maSet = (DSMicroarraySet) input;

        haveNonExistMarker = false;
        // Collect the parameters needed for the execution of the normalizer
        DSPanel<DSGeneMarker> markerPanel = ((HouseKeepingGeneNormalizerPanel) aspp).getPanel();
        ignoreMissingValues = ((HouseKeepingGeneNormalizerPanel) aspp).isMissingValueIgnored();

        // this is only used by the case that is not genepix
        double[] ratioArray = null;

        if (maSet instanceof CSMicroarraySet &&
                maSet.getCompatibilityLabel().equals("Genepix")) {
            getRatioForGenepix(maSet, markerPanel);
        } else {
            //ratioArray = getRatioArrary(maSet, markerPanel, BASEARRAY);
            ratioArray = getHouseKeepingGenesValue(maSet, markerPanel);
        }
        ((HouseKeepingGeneNormalizerPanel) aspp).setHighlightedMarkers(
                nonFoundGenes);
        if (haveNonExistMarker) {

        	/*int choice = JOptionPane.showConfirmDialog(null,
                    "Some of the designated genes are not in the dataset or have missing values. Proceed?  ",
                    "Warning",
                    JOptionPane.OK_CANCEL_OPTION);

            if (choice == 2) {
                return new AlgorithmExecutionResults(false, "Cancelled by user.", input);
            }*/
        }


        int arrayCount = maSet.size();
        int markerCount = maSet.getMarkers().size();

        for (int i = 0; i < arrayCount; i++) {
            if (Double.isNaN(ratioArray[i])) {
                continue;
            }
        	DSMicroarray microarray = maSet.get(i);
            for (int j = 0; j < markerCount; j++) {
                DSMutableMarkerValue markerValue = (DSMutableMarkerValue) microarray.getMarkerValue(
                        j);
                if (!markerValue.isMissing()) {
                    if (markerValue instanceof CSGenepixMarkerValue) {

                        adjustMarkerValue((CSGenepixMarkerValue) markerValue, i);
                    } else {
                        if (ratioArray[i] != 0) {
                            markerValue.setValue(markerValue.getValue() /
                                    ratioArray[i]);
                        }

                    }

                }
            }

        }
       
        HistoryPanel.addHistoryDetail(maSet,((HouseKeepingGeneNormalizerPanel) aspp).getParamDetail());
        
        return new AlgorithmExecutionResults(true, "No errors", input);
    }

    /**
     * adjustForHousekeepingGenes
     *
     * @param markerValue DSMutableMarkerValue
     */
    private void adjustMarkerValue(CSGenepixMarkerValue markerValue,
                                   int arrayLocation) {
        boolean recompute = false;
        if (ch1bArray[arrayLocation] != 0) {
            markerValue.setCh1Bg(markerValue.getCh1Bg() /
                    ch1bArray[arrayLocation]);
            recompute = true;
        }
        if (ch2bArray[arrayLocation] != 0) {
            markerValue.setCh2Bg(markerValue.getCh2Bg() /
                    ch2bArray[arrayLocation]);
            recompute = true;
        }
        if (ch1fArray[arrayLocation] != 0) {
            markerValue.setCh1Fg(markerValue.getCh1Fg() /
                    ch1fArray[arrayLocation]);
            recompute = true;
        }
        if (ch2fArray[arrayLocation] != 0) {
            markerValue.setCh2Fg(markerValue.getCh2Fg() /
                    ch2fArray[arrayLocation]);
            recompute = true;
        }
        if (recompute) {
            markerValue.computeSignal();
        }

    }

    /**
     * sum the housekeeping genes expression values together.
     *
     * @param maSet       DSMicroarraySet
     * @param markerPanel DSPanel
     * @return double[]
     */
    private double[] getHouseKeepingGenesValue(DSMicroarraySet maSet,
                                              DSPanel<DSGeneMarker> markerPanel) {
        int markerCount = markerPanel.size();

        int arrayCount = maSet.size();

        double[][] arrays = new double[markerCount][arrayCount];
        double ratio[] = new double[arrayCount];

        boolean[] ExistedMarkers = new boolean[markerPanel.size()];
        for (int j = 0; j < markerCount; j++) {
            ExistedMarkers[j] = true;
            DSGeneMarker csgMarker = (CSGeneMarker) markerPanel.get(j);

			DSGeneMarker markerAvaiable = ((CSMicroarraySet) maSet)
					.getMarkers().get(csgMarker.getLabel());
			if (markerAvaiable != null) { // this is null for the marker that is not in the dataset
				csgMarker = markerAvaiable;
			}

            for (int k = 0; k < arrayCount; k++) {
                arrays[j][k] = maSet.getValue(csgMarker, k);

                if (Double.isNaN(arrays[j][k]) || arrays[j][k] == 0) {
                    if (ignoreMissingValues) {
                        nonFoundGenes.add(csgMarker.getLabel());
                        ExistedMarkers[j] = false;
                        haveNonExistMarker = true;
                        break;
                    } else {
                        ExistedMarkers[j] = updateMissingValue(csgMarker, maSet);
                        arrays[j][k] = maSet.getValue(csgMarker, k);
                        if (!ExistedMarkers[j]) {
                            nonFoundGenes.add(csgMarker.getLabel());
                            haveNonExistMarker = true;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < arrayCount; i++) {
            ratio[i] = 0d;
            int existedHousegenesNum = 0;
            for (int j = 0; j < markerCount; j++) {
                if (ExistedMarkers[j]) {
                    existedHousegenesNum++;
                    ratio[i] += arrays[j][i];
                }
            }
            //For bug 334 use avg not sum of housekeeping genes
            if (existedHousegenesNum > 0) {
                ratio[i] /= existedHousegenesNum;
            }
        }
        return ratio;
    }

    /**
     * Update the missing value of the housekeeping genes to mean value for non-genpix platforms.
     *
     * @param csgMarker
     * @param maSet
     * @return
     */
    private boolean updateMissingValue(DSGeneMarker csgMarker, DSMicroarraySet maSet) {
        int arrayCount = maSet.size();
        DSMutableMarkerValue markerValue = null;
        int existingValues = 0;
        double meanValue = 0;

        for (int k = 0; k < arrayCount; k++) {
            double newValue = maSet.getValue(csgMarker, k);
            if (!Double.isNaN(newValue)) {
                meanValue += newValue;
                existingValues++;
            }
        }
        if (existingValues == 0) {
            return false;
        } else {
            meanValue = meanValue / existingValues;
            for (int k = 0; k < arrayCount; k++) {
                DSMicroarray microarray = (DSMicroarray) (maSet.get(k));
                markerValue = (DSMutableMarkerValue) microarray.getMarkerValue(
                        csgMarker);
                if (markerValue.isMissing()) {
                    markerValue.setMissing(false);
                    markerValue.setValue(meanValue);
                }
            }
        }

        return true;
    }

    /**
     * Update the missing value of the housekeeping genes to mean value for genpix platforms.
     *
     * @param csgMarkerStr
     * @param maSet
     * @return true if some values are not missing; false if everything is missing
     */
    private boolean updateMissingValue(final Vector<DSGeneMarker> matchingMarkers, final DSMicroarraySet maSet) {
        int arrayCount = maSet.size();

        int existingValues = 0;
        double theCh1f = 0;
        double theCh1b = 0;
        double theCh2f = 0;
        double theCh2b = 0;

        for (int i = 0; i < arrayCount; i++) {
			DSMicroarray microarray = (DSMicroarray) maSet.get(i);

			for (DSGeneMarker dsgMarker : matchingMarkers) {
				CSGenepixMarkerValue csgMarkerValue = (CSGenepixMarkerValue) microarray
						.getMarkerValue(dsgMarker);

				if (csgMarkerValue != null && !csgMarkerValue.isMissing()) {
					existingValues++;
					theCh1f += csgMarkerValue.getCh1Fg();
					theCh2f += csgMarkerValue.getCh2Fg();
					theCh1b += csgMarkerValue.getCh1Bg();
					theCh2b += csgMarkerValue.getCh2Bg();
				}

			}
        }
        
        if (existingValues == 0) {
            return false;
        }

		theCh1f /= existingValues;
		theCh2f /= existingValues;
		theCh1b /= existingValues;
		theCh2b /= existingValues;

		for (int k = 0; k < arrayCount; k++) {
			DSMicroarray microarray = (DSMicroarray) (maSet.get(k));

			for (DSGeneMarker dsgMarker : matchingMarkers) {
				CSGenepixMarkerValue csgMarkerValue = (CSGenepixMarkerValue) microarray
						.getMarkerValue(dsgMarker);
				if (csgMarkerValue.isMissing()) {
					csgMarkerValue.setMissing(false);
					csgMarkerValue.setCh1Bg(theCh1b);
					csgMarkerValue.setCh1Fg(theCh1f);
					csgMarkerValue.setCh2Bg(theCh2b);
					csgMarkerValue.setCh2Fg(theCh2f);
				}
			}

		}

		return true;
    }

    // this is only to track the DSMicroarraySet coming with the last ProjectEvent
    // not used in other part of the class
    private transient DSMicroarraySet refMASet = null;

    /**
     * receiveProjectSelection
     *
     * @param e ProjectEvent
     */
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {

		DSDataSet<?> dataSet = e.getDataSet();

		if (dataSet instanceof DSMicroarraySet && refMASet != dataSet) {
			houseKeepingGeneNormalizerPanel.clearAllActionPerformed();
			refMASet = (DSMicroarraySet) dataSet;
			houseKeepingGeneNormalizerPanel.maSet = refMASet;
		}
	}

    /**
     * getRatioForGenepix
     *
     * @param maSet       DSMicroarraySet
     * @param markerPanel DSPanel
     * @return 
     */
    private void getRatioForGenepix(DSMicroarraySet maSet,
                                       DSPanel<DSGeneMarker> markerPanel) {

        CSMarkerVector csMarkerVector = ((CSMicroarraySet) maSet).
        getMarkers();
        refreshMaps(csMarkerVector);
        int markerCount = markerPanel.size();
        int arrayCount = maSet.size();

        ch1fArray = new double[arrayCount];
        ch1bArray = new double[arrayCount];
        ch2fArray = new double[arrayCount];
        ch2bArray = new double[arrayCount];
        
        for (int i = 0; i < arrayCount; i++) {
            int existedHousegenesNum = 0;
            ch1fArray[i] = 0;
            ch1bArray[i] = 0;
            ch2fArray[i] = 0;
            ch2bArray[i] = 0;
            DSMicroarray microarray = (DSMicroarray) maSet.get(i);
            for (int j = 0; j < markerCount; j++) {

                String csgMarkerString = ((CSGeneMarker) markerPanel.get(j)).
                        getLabel();
                Vector<DSGeneMarker>
                matchingMarkers = getMatchingMarkers(csMarkerVector,
                        csgMarkerString);

                boolean someExisting = false;
                if(!ignoreMissingValues) {
                	someExisting = updateMissingValue(matchingMarkers, maSet);
                }
                

                if ( matchingMarkers.size() > 0) {
                    for (DSGeneMarker dsgMarker : matchingMarkers) {
                        CSGenepixMarkerValue csgMarkerValue = (
                                CSGenepixMarkerValue)
                                microarray.getMarkerValue(
                                        dsgMarker);

                        if (csgMarkerValue != null && !csgMarkerValue.isMissing()) {
                            existedHousegenesNum++;
                            ch1fArray[i] += csgMarkerValue.getCh1Fg();
                            ch2fArray[i] += csgMarkerValue.getCh2Fg();
                            ch1bArray[i] += csgMarkerValue.getCh1Bg();
                            ch2bArray[i] += csgMarkerValue.getCh2Bg();
                        } else {
                            if (ignoreMissingValues) {
                                nonFoundGenes.add(dsgMarker.getLabel());
                                haveNonExistMarker = true;
                            } else {
                                if (someExisting) {
                                    ch1fArray[i] += csgMarkerValue.getCh1Fg();
                                    ch2fArray[i] += csgMarkerValue.getCh2Fg();
                                    ch1bArray[i] += csgMarkerValue.getCh1Bg();
                                    ch2bArray[i] += csgMarkerValue.getCh2Bg();
                                }
                            }
                        }

                    }

                } else {

                    nonFoundGenes.add(csgMarkerString);
                    haveNonExistMarker = true;

                }


            }
            //For bug 334 use ave not sum of housekeeping genes
            if (existedHousegenesNum > 0) {
                ch1fArray[i] /= existedHousegenesNum;
                ch2fArray[i] /= existedHousegenesNum;
                ch1bArray[i] /= existedHousegenesNum;
                ch1bArray[i] /= existedHousegenesNum;
            }

        }
    }

    // the following code was moved from CSMarkerVector to avoid unnecessary dependency
	private Hashtable<Integer, Set<DSGeneMarker>> geneIdMap = new Hashtable<Integer, Set<DSGeneMarker>>();
	private Hashtable<String, Set<DSGeneMarker>> geneNameMap = new Hashtable<String, Set<DSGeneMarker>>();

	private void refreshMaps(CSMarkerVector markerVector) {
		geneIdMap.clear();
		geneNameMap.clear();
		for (DSGeneMarker item : markerVector) {

			if (item.getGeneIds() != null && item.getGeneIds().length > 0) {
				int[] ids = item.getGeneIds();
				for (int i = 0; i < ids.length; i++) {
					Integer geneId = new Integer(ids[i]);
					if (geneId != null && geneId.intValue() > 0) {
						addItem(geneIdMap, geneId, item);
					}
				}
			}

			if (item.getShortNames() != null && item.getShortNames().length > 0) {
				String[] geneNames = item.getShortNames();
				String label = item.getLabel();
				for (int i = 0; i < geneNames.length; i++) {
					String geneName = geneNames[i];
					if (geneName != null && (!"---".equals(geneName.trim()))) {
						if (label != null && geneName.equals("")) {
							addItem(geneNameMap, label, item);
						} else {
							addItem(geneNameMap, geneName.trim(), item);
						}
					}
				}
			}

		}
	}

	private static <T> void addItem(Map<T, Set<DSGeneMarker>> map, T key,
			DSGeneMarker marker) {
		Set<DSGeneMarker> set = map.get(key);
		if (set == null) {
			set = new HashSet<DSGeneMarker>();
			set.add(marker);
			map.put(key, set);
		} else {
			set.add(marker);
		}
	}
	
	private Vector<DSGeneMarker> getMatchingMarkers(CSMarkerVector markerVector, String aString) {
		Vector<DSGeneMarker> matchingMarkers = new Vector<DSGeneMarker>();
		DSGeneMarker uniqueKeyMarker = markerVector.get(aString);
		if (uniqueKeyMarker != null) {
			matchingMarkers.add(uniqueKeyMarker);
		}

		Set<DSGeneMarker> markersSet = geneNameMap.get(aString);
		if (markersSet != null && markersSet.size() > 0) {
			for (DSGeneMarker marker : markersSet) {
				if (!matchingMarkers.contains(marker)) {
					matchingMarkers.add(marker);
				}
			}
		}

		try {
			Integer geneId = Integer.parseInt(aString);
			markersSet = geneIdMap.get(geneId);
			if (markersSet != null && markersSet.size() > 0) {
				for (DSGeneMarker marker : markersSet) {
					if (!matchingMarkers.contains(marker)) {
						matchingMarkers.add(marker);
					}
				}
			}
		} catch (NumberFormatException e) {
			Log.error(e);
		}

		return matchingMarkers;
	}
}
