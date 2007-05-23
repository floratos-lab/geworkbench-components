package org.geworkbench.components.medusa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;

/**
 * 
 * @author keshav
 * @version $Id: MedusaHelper.java,v 1.8 2007-05-23 16:49:56 keshav Exp $
 */
public class MedusaHelper {

	private static Log log = LogFactory.getLog(MedusaHelper.class);

	private static final String TAB_SEPARATOR = "\t";

	/**
	 * Creates a labels file.
	 * 
	 * @param microarraySetView
	 * @param filename
	 * @param regulators
	 * @param targets
	 * @return boolean
	 */
	public static boolean writeMedusaLabelsFile(
			DSMicroarraySetView microarraySetView, String filename,
			List<DSGeneMarker> regulators, List<DSGeneMarker> targets) {

		BufferedWriter out = null;
		boolean pass = true;
		try {

			out = new BufferedWriter(new FileWriter(filename));

			DSItemList<DSGeneMarker> markers = microarraySetView.allMarkers();
			for (DSGeneMarker marker : markers) {

				double[] data = microarraySetView.getMicroarraySet().getRow(
						marker.getSerial());

				if (data == null)
					continue;

				if (regulators.contains(marker)) {
					out.write('R');
				} else if (targets.contains(marker)) {
					out.write('T');
				} else {
					log.info("Marker " + marker.getLabel()
							+ " neither regulator nor target ... skipping.");
					continue;
				}
				out.write(TAB_SEPARATOR);
				out.write(marker.getLabel());
				out.write(TAB_SEPARATOR);
				for (int j = 0; j < data.length; j++) {
					out.write(String.valueOf(data[j]));
					if (j < data.length - 1)
						out.write(TAB_SEPARATOR);
					else
						out.write("\n");
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			pass = false;
		}

		return pass;
	}

	/**
	 * Generate a consensue sequence.
	 * 
	 * @param data
	 * @return String
	 */
	public static String generateConsensusSequence(double[][] data) {
		StringBuffer sequence = new StringBuffer();

		for (int j = 0; j < data[0].length; j++) {

			String currentLetter = null;

			double aVal = data[0][j];
			double cVal = data[1][j];
			double gVal = data[2][j];
			double tVal = data[3][j];

			Map<String, Double> dataMap = new LinkedHashMap<String, Double>();
			dataMap.put("A", aVal);
			dataMap.put("C", cVal);
			dataMap.put("G", gVal);
			dataMap.put("T", tVal);

			dataMap = sortMapByValueDecreasing(dataMap);

			currentLetter = dataMap.keySet().iterator().next();

			// log.debug(dataMap.get(currentLetter));

			if (dataMap.get(currentLetter) < 0.75)
				currentLetter = currentLetter.toLowerCase();

			sequence.append(currentLetter);
		}

		log.info("Returning sequence from " + MedusaHelper.class + ": "
				+ sequence);

		return sequence.toString();

	}

	/**
	 * Sort the map by value in decreasing order.
	 * 
	 * @param dataMap
	 * @return Map
	 */
	private static Map<String, Double> sortMapByValueDecreasing(
			Map<String, Double> dataMap) {
		List<String> mapKeys = new ArrayList(dataMap.keySet());
		List<Double> mapValues = new ArrayList(dataMap.values());

		dataMap.clear();

		TreeSet sortedSet = new TreeSet(mapValues);

		Object[] sortedArray = sortedSet.toArray();

		int size = sortedArray.length;

		// Descending sort

		for (int i = size; i > 0;) {

			dataMap.put(mapKeys.get(mapValues.indexOf(sortedArray[--i])),
					(Double) sortedArray[i]);
		}

		return dataMap;
	}

	/**
	 * Generate the for the optimal pssm represented by this data score.
	 * 
	 * @param data
	 * @return double
	 */
	public static double generateScore(double[][] data) {
		double score = 0;
		for (int j = 0; j < data[0].length; j++) {

			String currentLetter = null;

			double aVal = data[0][j];
			double cVal = data[1][j];
			double gVal = data[2][j];
			double tVal = data[3][j];

			Map<String, Double> dataMap = new LinkedHashMap<String, Double>();
			dataMap.put("A", aVal);
			dataMap.put("C", cVal);
			dataMap.put("G", gVal);
			dataMap.put("T", tVal);

			dataMap = sortMapByValueDecreasing(dataMap);

			/* gets the best score for this column */
			currentLetter = dataMap.keySet().iterator().next();

			double val = dataMap.get(currentLetter);

			score = score + Math.log(val);

			log.info("score: " + score);
		}

		return score;
	}

	/**
	 * Print the data from the PSSM matrix.
	 * 
	 * @param data
	 */
	private void printData(double[][] data) {
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				log.info("[" + i + "][" + j + "] = " + data[i][j]);
			}
		}
	}

	/**
	 * Returns true if the score represents a hit, else false.
	 * 
	 * Both the data and pssmThreshold are the raw vales as per the rule_N.xml
	 * file and are internally transformed accordingly.
	 * 
	 * @param data
	 * @param pssmThreshold
	 * @return
	 */
	public static boolean isPssmHit(double[][] data, double pssmThreshold) {

		double score = generateScore(data);

		pssmThreshold = Math.log(pssmThreshold);

		return isHit(score, pssmThreshold);
	}

	/**
	 * Checks if the score represents a "hit".
	 * 
	 * If score is >= threshold, hit, else miss.
	 * 
	 * @param score
	 * @param threshold
	 * @return boolean
	 */
	private static boolean isHit(double score, double threshold) {
		boolean hit = false;

		if (score >= threshold)
			hit = true;

		return hit;

	}
}
