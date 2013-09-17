package org.geworkbench.components.filtering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;
import org.geworkbench.components.filtering.MultipleProbesetFilterPanel.Action;
import org.geworkbench.util.AnnotationLookupHelper;

/**
 * 
 * @author zji
 * @version $Id$
 */

/**
 * The user preferences are collected in this filter's associate parameters GUI
 * (<code>MultipleProbesetFilter</code>).
 */
public class MultipleProbesetFilter extends FilteringAnalysis {
	private static final long serialVersionUID = -6590700967249955520L;

	private Action filterAction = null;

	public MultipleProbesetFilter() {
		setDefaultPanel(new MultipleProbesetFilterPanel());
	}

	@Override
	public List<Integer> getMarkersToBeRemoved(DSMicroarraySet input) {

		getParametersFromPanel();

		maSet = (DSMicroarraySet) input;
		DSItemList<DSGeneMarker> dsItemList = maSet.getMarkers();

		if(maSet.getAnnotationFileName()==null) {
			JOptionPane.showMessageDialog(null,
				    "This filter requires that an annotation file be loaded. You must reload the data file to add an annotation file.",
				    "No annotation warning",
				    JOptionPane.WARNING_MESSAGE);
			return null;
		}
		 
		Map<String, Integer> probesetIndexMap = new HashMap<String, Integer>();
		Map<String, String> entrezProbesMap = new HashMap<String, String>(); // ~ delimited Probes per Entrez Gene
		Map<String, String> entrezIDsWithMultipleProbes = new HashMap<String, String>();
		int markerCount = dsItemList.size();
		for (int i = 0; i < markerCount; i++) {
			DSGeneMarker dsGeneMarker = dsItemList.get(i);
			String probeSetID = dsGeneMarker.getLabel();
			probesetIndexMap.put(probeSetID, i);

			Set<String> set = AnnotationLookupHelper.getGeneIDs(probeSetID);
			if(set.size()==0)
				continue;
			
			String firstGeneID = set.toArray(new String[0])[0];

			if (firstGeneID == null || firstGeneID.trim().equals("") || firstGeneID.trim().equals("---")) {
				continue;
			}

			if (entrezProbesMap.containsKey(firstGeneID)) {
				String value = entrezProbesMap.get(firstGeneID);
				entrezProbesMap.put(firstGeneID, value + "~" + probeSetID);
				entrezIDsWithMultipleProbes.put(firstGeneID, value + "~"
						+ probeSetID);
			} else {
				entrezProbesMap.put(firstGeneID, probeSetID);
			}
		}

		// Find the best Probeset
		List<Integer> indexList = new ArrayList<Integer>();
		Set<Integer> keepTheseProbes = new HashSet<Integer>();
		Iterator<String> iterator = entrezIDsWithMultipleProbes.keySet().iterator();
		while (iterator.hasNext()) {
			String entrezGeneIDWithMultipleProbes = (String) iterator.next();
			String genes = entrezIDsWithMultipleProbes
					.get(entrezGeneIDWithMultipleProbes);
			int theWinner = 0;
			double champ = 0.0;
			StringTokenizer stringTokenizer = new StringTokenizer(genes, "~");
			while (stringTokenizer.hasMoreTokens()) {
				String probeSetID = stringTokenizer.nextToken();

				int probeSetIndex = probesetIndexMap.get(probeSetID);
				indexList.add(probeSetIndex);
				if (probeSetIndex == -1) {
					continue;
				}

				double[] profile = getProfile(maSet, probeSetIndex);
				double challenger = 0.0;
				
				if (filterAction == MultipleProbesetFilterPanel.Action.RETAIN_HIGH_COV ) {
					challenger = getCV(profile);
				} else if (filterAction == MultipleProbesetFilterPanel.Action.RETAIN_HIGH_MEAN ) {
					challenger = getMean(profile);
				} else if (filterAction == MultipleProbesetFilterPanel.Action.RETAIN_HIGH_MEDIAN ) {
					challenger = getMedian(profile);
				} 

				if (challenger > champ) {
					theWinner = probeSetIndex;
					champ = challenger;
				}
			}

			keepTheseProbes.add(theWinner);
		}

		List<Integer> filterList = new ArrayList<Integer>();
		for (int i = 0; i < indexList.size(); i++) {
			Integer index = indexList.get(i);
			if (!keepTheseProbes.contains(index)) {
				filterList.add(index);
			}
		}

		Collections.sort(filterList);
		return filterList;
	}

	private double getCV(double[] profile) {
		if (profile == null)
			return 0.0;
		double meanValue = getMean(profile);
		double deviation = 0.0;
		double diff = 0.0;
		for (int i = 0; i < profile.length; i++) {
			diff = (profile[i] - meanValue);
			deviation += diff * diff;
		}
		if (profile.length > 1)
			deviation /= (profile.length - 1);
		double returnValue = 0.0;
		if (Math.abs(meanValue) > Double.MIN_VALUE)
			returnValue = Math.sqrt(deviation) / meanValue;
		return returnValue;
	}

	/**
	 * Compute the mean value for the numbers in <code>profile</code>.
	 * 
	 * @param profile
	 * @return
	 */
	private double getMean(double[] profile) {
		if (profile == null)
			return 0.0;

		double sum = 0.0d;
		for (int i = 0; i < profile.length; i++)
			sum += profile[i];
		if (profile.length > 0)
			sum /= profile.length;
		return (double) sum;
	}

	/**
	 * Compute the mean value for the numbers in <code>profile</code>.
	 * 
	 * @param profile
	 * @return
	 */
	private double getMedian(double[] profile) {
		if (profile == null)
			return 0.0;
		
		double high = 0.0d;
		double low = 999.0d;
		for (int i = 0; i < profile.length; i++){
			if (high < profile[i]){
				high = profile[i];
			}
			if (low > profile[i]){
				low = profile[i];
			}
		}

		double median = (high + low)/2;
		return median;
	}
	
	private double[] getProfile(DSMicroarraySet maSet, int index) {
		if (maSet == null || index < 0 || index >= maSet.getMarkers().size())
			return null;
		int arrayCount = maSet.size();

		int nonMissing = 0;
		for (int i = 0; i < arrayCount; i++) {
			DSMarkerValue mv = maSet.get(i).getMarkerValue(index);
			if (!mv.isMissing()) {
				++nonMissing;
			}
		}

		double[] profile = new double[nonMissing];

		for (int i = 0, j = 0; i < arrayCount; i++) {
			DSMicroarray microarray = maSet.get(i);
			DSMarkerValue mv = microarray.getMarkerValue(index);
			if (!mv.isMissing()) {
				profile[j++] = mv.getValue();
			}
		}

		return profile;
	}

    /**
     * This filter is different from other filters in that the marker are recognized as missing directly
     * instead of being based on count or percentage. So this method is in fact ignored.
     */
    @Override
    public boolean isMissing(int arrayIndex, int markerIndex) {
    	return true; // does not matter
    }


	@Override
	protected void getParametersFromPanel() {
		MultipleProbesetFilterPanel multipleProbesetFilterPanel = (MultipleProbesetFilterPanel) aspp;
		filterAction = multipleProbesetFilterPanel.getFilterAction();
	}
}