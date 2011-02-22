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

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSAffyMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust Inc.
 * @version $Id$
 */

/**
 * The user preferences are collected in this filter's associate parameters GUI
 * (<code>MultipleProbesetFilter</code>).
 */
public class MultipleProbesetFilter extends FilteringAnalysis {
	private static final long serialVersionUID = -6590700967249955520L;

	static final int MARKER = 0;
	static final int MICROARRAY = 1;
	static final int IGNORE = 2;

	static final double SMALLDOUBLE = 0.00000000000001;

	protected int filterAction;

	static final int HIGHEST_COEFFICIENT_OF_VARIATION = 0;
	static final int HIGHEST_MEAN_EXPRESSION = 1;
	static final int HIGHEST_MEDIAN_EXPRESSION = 2;
	static final int AVERAGE_EXPRESSION_VALUES = 3;

	protected boolean filterCoefficientOfVariation = false;
	protected boolean filterHighestMean = false;
	protected boolean filterHighestMedian = false;
	protected boolean filterAverageExpressionValues = false;

	Map<String, Integer> probesetIndexMap = new HashMap<String, Integer>();

	
	
	public MultipleProbesetFilter() {
		setDefaultPanel(new MultipleProbesetFilterPanel());
	}

	@Override
	public List<Integer> getMarkersToBeRemoved(DSMicroarraySet<?> input) {

		getParametersFromPanel();

		maSet = (DSMicroarraySet<DSMicroarray>) input;
		DSItemList<DSGeneMarker> dsItemList = maSet.getMarkers();
		makeProbesetIndexMap(dsItemList);
		
		Map<String, String> entrezProbesMap = new HashMap<String, String>(); // ~ delimited Probes per Entrez Gene
		Map<String, String> entrezIDsWithMultipleProbes = new HashMap<String, String>();
		int markerCount = dsItemList.size();
		for (int i = 0; i < markerCount; i++) {
			DSGeneMarker dsGeneMarker = dsItemList.get(i);
			String probeSetID = dsGeneMarker.getLabel();

			if (probeSetID.equals("1552497_a_at")) {
				String asdf = "asdf";
			}

			String firstGeneID = AnnotationParser.getGeneIDs(probeSetID).toArray(new String[0])[0];

			if (firstGeneID == null || firstGeneID.trim().equals("")) {
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
//				int probeSetIndex = getProbeSetIndex(dsItemList, probeSetID);
				int probeSetIndex = getProbeSetIndex(probeSetID);
				indexList.add(probeSetIndex);
				if (probeSetIndex == -1) {
					continue;
				}

				// int probesetIndex = dsItemList.indexOf("117_at");
				double[] profile = getProfile(maSet, probeSetIndex);
				double challenger = 0.0;
				
				if (filterAction == HIGHEST_COEFFICIENT_OF_VARIATION) {
					challenger = getCV(profile);
				} else if (filterAction == HIGHEST_MEAN_EXPRESSION) {
					challenger = getMean(profile);
				} else if (filterAction == HIGHEST_MEDIAN_EXPRESSION) {
					challenger = getMedian(profile);
				} 
//				else if (filterAction == AVERAGE_EXPRESSION_VALUES) {
//					challenger = getAverage(profile);
//				}

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

//	// there must be a better way to do this
//	int getProbeSetIndex(DSItemList<DSGeneMarker> dsItemList, String probeSetID) {
//		int index = -1;
//		for (index = 0; index < dsItemList.size(); index++) {
//			DSGeneMarker dsGeneMarker = dsItemList.get(index);
//			String probeSetIDlabel = dsGeneMarker.getLabel();
//
//			// if (probeSetIDlabel.equals("117_at")){
//			if (probeSetIDlabel.equals(probeSetID)) {
//				break;
//			}
//		}
//
//		return index;
//	}

	
	int getProbeSetIndex(String probeSetID) {

		int index = probesetIndexMap.get(probeSetID);
		
		return index;
	}

		
	void makeProbesetIndexMap(DSItemList<DSGeneMarker> dsItemList){
		int index = -1;
		for (index = 0; index < dsItemList.size(); index++) {
			DSGeneMarker dsGeneMarker = dsItemList.get(index);
			String probeSetIDlabel = dsGeneMarker.getLabel();
			probesetIndexMap.put(probeSetIDlabel, index);
		}
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
		if (Math.abs(meanValue) > SMALLDOUBLE)
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
	
	double[] getProfile(DSMicroarraySet<DSMicroarray> maSet, int index) {
		if (maSet == null || index < 0 || index >= maSet.getMarkers().size())
			return null;
		int arrayCount = maSet.size();
		// Compute the profile average (using non-missing values).
		double average = 0.0;
		int nonMissing = 0;
		for (int i = 0; i < arrayCount; i++) {
			DSMarkerValue mv = maSet.get(i).getMarkerValue(index);
			if (!mv.isMissing()) {
				average += mv.getValue();
				++nonMissing;
			}
		}

		if (nonMissing > 0) {
			average /= nonMissing;
		}

		double[] profile = null;

		// These options may be added similar to the CoefficientOfVariationFilet
		// static final int MARKER = 0;
		// static final int MICROARRAY = 1;
		// static final int IGNORE = 2;
		int missingValues = IGNORE;
		if (missingValues == IGNORE) {
			profile = new double[nonMissing];
		} else {
			profile = new double[arrayCount];
		}

		for (int i = 0, j = 0; i < arrayCount; i++) {
			DSMicroarray microarray = maSet.get(i);
			DSMarkerValue mv = microarray.getMarkerValue(index);
			if (!mv.isMissing()) {
				profile[j++] = mv.getValue();
			} else if (missingValues == MARKER) {
				profile[j++] = average;
			}
			// else if (missingValues == MICROARRAY)
			// profile[j++] = microarrayAverages[microarray.getSerial()];

			// if IGNORE, do nothing
		}

		return profile;
	}

	@Override
	protected boolean expectedType() {
		// assuming the first marker has the same type as all other markers
		DSMicroarray mArray = maSet.get(0);
		CSMarkerValue mv = (CSMarkerValue) mArray.getMarkerValue(0);
		return (mv instanceof DSAffyMarkerValue);
	}

	@Override
	protected boolean isMissing(int arrayIndex, int markerIndex) {
		DSMicroarray mArray = maSet.get(arrayIndex);
		CSMarkerValue mv = (CSMarkerValue) mArray.getMarkerValue(markerIndex);

		if (mv == null || mv.isMissing()) {
			return false;
		}
		if (mv.isPresent() && filterAverageExpressionValues) {
			return true;
		}
		if (mv.isAbsent() && filterCoefficientOfVariation) {
			return true;
		}
		if (mv.isMarginal() && filterHighestMean) {
			return true;
		}
		if (mv.isMarginal() && filterHighestMedian) {
			return true;
		}
		return false;
	}

	@Override
	protected void getParametersFromPanel() {
		MultipleProbesetFilterPanel multipleProbesetFilterPanel = (MultipleProbesetFilterPanel) aspp;
		filterAction = multipleProbesetFilterPanel.getFilterAction();
	}
}