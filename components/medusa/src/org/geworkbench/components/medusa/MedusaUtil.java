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

import edu.columbia.ccls.medusa.io.MedusaReader;
import edu.columbia.ccls.medusa.io.RuleParser;
import edu.columbia.ccls.medusa.io.SerializedRule;

/**
 * 
 * @author keshav
 * @version $Id: MedusaUtil.java,v 1.5 2007-05-31 16:57:30 keshav Exp $
 */
public class MedusaUtil {

	private static Log log = LogFactory.getLog(MedusaUtil.class);

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
		List<String> mapKeys = new ArrayList<String>(dataMap.keySet());
		List<Double> mapValues = new ArrayList<Double>(dataMap.values());

		dataMap.clear();

		TreeSet<Double> sortedSet = new TreeSet<Double>(mapValues);

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
	 * Given the label of a target, returns true if the consensus sequence
	 * "hits" the sequence of this target. The concensus sequence is created
	 * internally from the pssm data. This method does not tell you where the
	 * hit occured, but just that it has occured.
	 * 
	 * @param pssm
	 *            The pssm data for a given (generated) rule
	 * @param targetLabel
	 * @return boolean
	 */
	public static boolean isHitByPssm(double[][] pssm, double threshold,
			String targetLabel, String sequencePath) {

		double score = 0;

		threshold = Math.log(threshold);

		String concensusSequence = generateConsensusSequence(pssm);

		Map<String, int[]> targetSequenceMap = MedusaUtil
				.getSequences(sequencePath);
		int[] numericSequence = targetSequenceMap.get(targetLabel);

		int boundary = numericSequence.length - concensusSequence.length() + 1;
		for (int i = 0; i < boundary; i++) {
			int start = i;
			int end = i + (concensusSequence.length());
			int[] windowSequence = new int[end - start];
			int k = 0;
			for (int j = start; j < end; j++) {
				windowSequence[k] = numericSequence[j];
				k++;
			}

			for (int l = 0; l < windowSequence.length; l++) {
				// -1 since Leslie index starts at 1
				int numericNucleotide = windowSequence[l] - 1;

				double val = pssm[numericNucleotide][l];
				score = score + Math.log(val);
			}

			if (isHit(score, threshold))
				return true;

		}

		return false;
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
	 * Returns the sequences used in the medusa run.
	 * 
	 * @param sequencePath
	 * @return Map<String, int[]>
	 */
	public static Map<String, int[]> getSequences(String sequencePath) {

		MedusaReader medusaReader = new MedusaReader();

		Map<String, int[]> targetSequenceMap = medusaReader
				.getCleanFasta(sequencePath);

		return targetSequenceMap;
	}

	/**
	 * Initializes the matrix of booleans which shows if the consensus sequences
	 * has a hit or miss anywhere along the upstream region of gene target.
	 * 
	 * @param targetNames
	 * @param ruleFiles
	 * @param rulePath
	 * @param sequencePath
	 * @return
	 */
	public static boolean[][] generateHitOrMissMatrix(List<String> targetNames,
			List<String> ruleFiles, String rulePath, String sequencePath) {
		boolean[][] hitOrMissMatrix = new boolean[targetNames.size()][ruleFiles
				.size()];
		int i = 0;
		for (String targetName : targetNames) {
			int j = 0;
			for (String ruleFile : ruleFiles) {
				SerializedRule srule = null;
				try {
					srule = RuleParser.read(rulePath + ruleFile);
				} catch (IOException e) {
					e.printStackTrace();
				}

				boolean isHit = MedusaUtil.isHitByPssm(srule.getPssm(), srule
						.getPssmThreshold(), targetName, sequencePath);

				hitOrMissMatrix[i][j] = isHit;

				j++;
			}
			i++;
		}

		return hitOrMissMatrix;
	}
}
