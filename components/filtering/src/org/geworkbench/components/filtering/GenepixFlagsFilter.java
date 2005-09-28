package org.geworkbench.components.filtering;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.
        DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.
        DSGenepixMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.
        DSMutableMarkerValue;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.
        CSExprMicroarraySet;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.
        CSGenepixMarkerValue;
import java.util.TreeSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Columbia University.</p>
 * @author Xiaoqing Zhang
 * @version 1.0
 */

/**
 * Implementation of an flag-based filter for 2 channel data. The
 * class will filter out measures based on its flags.
 */
public class GenepixFlagsFilter extends AbstractAnalysis implements
        FilteringAnalysis {

    private TreeMap flagsProbeNum = new TreeMap<String, Integer>();

    public GenepixFlagsFilter() {
        setLabel("Genepix Flags Filter");
        setDefaultPanel(new GenepixFlagsFilterPanel());
    }

    public int getAnalysisType() {
        return AbstractAnalysis.GENEPIX_FlAGS_FILTER_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null) {
            return new AlgorithmExecutionResults(false, "Invalid input.", null);
        }
        assert input instanceof DSMicroarraySet;
        DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet) input;
        // Collect the parameters needed for the execution of the filter

        int markerCount = maSet.getMarkers().size();
        int arrayCount = maSet.size();
        for (int i = 0; i < arrayCount; i++) {
            DSMicroarray mArray = maSet.get(i);
            for (int j = 0; j < markerCount; ++j) {
                DSMutableMarkerValue mv = (DSMutableMarkerValue) mArray.
                                          getMarkerValue(j);
                if ((mv instanceof DSGenepixMarkerValue)) {
                    if (shouldBeFiltered((DSGenepixMarkerValue) mv)) {
                        mv.setMissing(true);
                    }
                } else {
                    return new AlgorithmExecutionResults(false,
                            "This filter can only be used with Genepix datasets", null);
                }
            }
        }

        return new AlgorithmExecutionResults(true, "No errors.", input);
    }


    /**
     * Check if the 2 channel signals for the argument marker value should be set as missing.
     *
     * @param mv
     * @return
     */
    private boolean shouldBeFiltered(DSGenepixMarkerValue mv) {
        if (mv == null || mv.isMissing()) {
            return false;
        }
        ArrayList selectedFlags = ((GenepixFlagsFilterPanel) aspp).
                                  getSelectedFlags();
        if (selectedFlags != null) {
            return selectedFlags.contains(mv.getFlag());
        }
        return false;

    }

    /**
     * receiveProjectSelection, grep the flag related information, then set the values into the GenepixFlagsFilterPanel.
     *
     * @param e ProjectEvent
     */
    @Subscribe public void receive(org.geworkbench.events.ProjectEvent e,
                                   Object source) {
        TreeSet flagSet = new TreeSet();
        int unflaggedProbeNum = 0;
        flagsProbeNum.clear();
        if (e.getMessage().equals(org.geworkbench.events.ProjectEvent.CLEARED)) {
            ((GenepixFlagsFilterPanel) aspp).setFlagInfoPanel();
        } else {
            DSDataSet dataSet = e.getDataSet();
            if (dataSet instanceof CSExprMicroarraySet) {
                String compatibilityLabel = ((CSExprMicroarraySet) dataSet).
                                            getCompatibilityLabel();
                CSExprMicroarraySet maSet = (CSExprMicroarraySet) dataSet;
                if (compatibilityLabel != null && compatibilityLabel.equals("Genepix")) {
                    int markerCount = maSet.getMarkers().size();
                    int arrayCount = maSet.size();
                    for (int i = 0; i < arrayCount; i++) {
                        DSMicroarray mArray = maSet.get(i);
                        unflaggedProbeNum = 0;
                        for (int j = 0; j < markerCount; ++j) {
                            DSMutableMarkerValue mv = (DSMutableMarkerValue)
                                    mArray.
                                    getMarkerValue(j);
                            if ((mv instanceof DSGenepixMarkerValue)) {
                                String flag = ((CSGenepixMarkerValue) mv).
                                              getFlag();
                                if (!mv.isMissing() && !flag.equals("0")) {
                                    flagSet.add(flag);
                                    if (flagsProbeNum.containsKey(flag)) {
                                        Integer integer = (Integer)
                                                flagsProbeNum.get(flag);
                                        integer++;
                                        flagsProbeNum.put(flag, integer);

                                    } else {
                                        flagsProbeNum.put(flag, new Integer(1));
                                    }

                                } else {
                                    unflaggedProbeNum++;
                                }
                            }
                        }
                    }
                    if (flagSet.size() > 0) {

                        ((GenepixFlagsFilterPanel) aspp).setFlagInfoPanel(
                                flagSet);
                        ((GenepixFlagsFilterPanel) aspp).setUnflaggedProbeNum(
                                unflaggedProbeNum);
                        ((GenepixFlagsFilterPanel) aspp).setflaggedProbeNum(
                                flagsProbeNum);
                    } else {
                        ((GenepixFlagsFilterPanel) aspp).setFlagInfoPanel(
                                "Only unFlagged data are found. The total probe number is " +
                                unflaggedProbeNum + ".");
                    }
                } else {
                    ((GenepixFlagsFilterPanel) aspp).setFlagInfoPanel(
                            "The data format is: " + compatibilityLabel);

                }
            }else {
                String datatype = dataSet.getCompatibilityLabel();
                    ((GenepixFlagsFilterPanel) aspp).setFlagInfoPanel(
                         "The data format is: " +  datatype);

                }


            }



    }

    // We override here the method AbstractAnalysis.saveParametersUnderName(String name)
    // to prohibit saving the "parameters" panel as this is not appropriate for
    // this filter (due to the fact that the parameters are dataset specific).
    public void saveParametersUnderName(String name) {
          }
}
