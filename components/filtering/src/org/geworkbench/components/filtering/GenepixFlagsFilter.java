package org.geworkbench.components.filtering;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSGenepixMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSGenepixMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Columbia University.</p>
 * @author Xiaoqing Zhang
 * @version $Id$
 */

/**
 * Implementation of an flag-based filter for 2 channel data. The
 * class will filter out measures based on its flags.
 */
public class GenepixFlagsFilter extends FilteringAnalysis {
	private static final long serialVersionUID = -6605769712853311721L;
	private static Log log = LogFactory.getLog(GenepixFlagsFilter.class);
	
	private Map<String, Integer> flagsProbeNum = new TreeMap<String, Integer>();

    public GenepixFlagsFilter() {
        setDefaultPanel(new GenepixFlagsFilterPanel());
    }

    @Override
	protected boolean expectedType() {
        // TODO we need to do this a more clear way if it is really needed
        DSMicroarray mArray = maSet.get(0);
        CSMarkerValue mv = (CSMarkerValue) mArray.getMarkerValue(0);
        return (mv instanceof DSGenepixMarkerValue);
	}
    
	protected String expectedTypeName = "Genepix";

    /**
     * Check if the 2 channel signals for the argument marker value should be set as missing.
     *
     * @param mv
     * @return
     */
    @Override
    protected boolean isMissing(int arrayIndex, int markerIndex) {
        DSMicroarray mArray = maSet.get(arrayIndex);
        DSGenepixMarkerValue mv = (DSGenepixMarkerValue) mArray.getMarkerValue(markerIndex);

        if (mv == null || mv.isMissing()) {
            return false;
        }
        ArrayList<?> selectedFlags = ((GenepixFlagsFilterPanel) aspp).
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
        Set<String> flagSet = new TreeSet<String>();
        int unflaggedProbeNum = 0;
        flagsProbeNum.clear();
        if (e.getValue()==ProjectEvent.Message.CLEAR) {
            ((GenepixFlagsFilterPanel) aspp).setFlagInfoPanel();
        } else {
            DSDataSet<?> dataSet = e.getDataSet();
            if(dataSet==null){

                ((GenepixFlagsFilterPanel) aspp).setFlagInfoPanel(
                         "The data format is: null.");

            }else if (dataSet instanceof CSMicroarraySet) {
                String compatibilityLabel = ((CSMicroarraySet) dataSet).
                                            getCompatibilityLabel();
                CSMicroarraySet maSet = (CSMicroarraySet) dataSet;
                if (compatibilityLabel != null && compatibilityLabel.equals("Genepix")) {
                    int markerCount = maSet.getMarkers().size();
                    int arrayCount = maSet.size();
                    for (int i = 0; i < arrayCount; i++) {
                        DSMicroarray mArray = (DSMicroarray) maSet.get(i);
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
                        ((GenepixFlagsFilterPanel) aspp).setFlagInfoPanel("it contains only unflagged data.");
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

	@Override
	protected void getParametersFromPanel() {
		GenepixFlagsFilterPanel parameterPanel = (GenepixFlagsFilterPanel) aspp;
        FilterOptionPanel filterOptionPanel = parameterPanel.getFilterOptionPanel();
		if(filterOptionPanel.getSelectedOption()==FilterOptionPanel.Option.NUMBER_REMOVAL) {
	        criterionOption = CriterionOption.COUNT;	       
	        numberThreshold = filterOptionPanel.getNumberThreshold();
		} else if(filterOptionPanel.getSelectedOption()==FilterOptionPanel.Option.PERCENT_REMOVAL) {
	        criterionOption = CriterionOption.PERCENT;	       
	        percentThreshold = filterOptionPanel.getPercentThreshold();
		} else {
	        log.error("Invalid filtering option");
		}
		
      
       
	}
}
