package org.geworkbench.components.medusa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.medusa.MedusaCommand;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.components.medusa.gui.TranscriptionFactorInfoBean;
import org.geworkbench.util.FilePathnameUtils;
import org.ginkgo.labs.reader.XmlReader;
import org.ginkgo.labs.reader.XmlWriter;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.columbia.ccls.medusa.io.MedusaReader;
import edu.columbia.ccls.medusa.io.RuleParser;
import edu.columbia.ccls.medusa.io.SerializedRule;

/**
 * This utility is specific to medusa, and is responsible for medusa related
 * tasks such as creating a medusa labels file, generating concensus sequences,
 * updating the configuration file, etc.
 * 
 * @author keshav
 * @version $Id$
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
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySetView, String filename,
			List<DSGeneMarker> regulators, List<DSGeneMarker> targets) {

		BufferedWriter out = null;
		boolean pass = true;
		try {

			out = new BufferedWriter(new FileWriter(filename));

			DSItemList<DSGeneMarker> markers = microarraySetView.allMarkers();
			for (DSGeneMarker marker : markers) {

				double[] data = microarraySetView.getMicroarraySet().getRow(
						marker);

				if (data == null)
					continue;

				if (regulators.contains(marker)) {
					out.write('R');
				} else if (targets.contains(marker)) {
					out.write('T');
				} else {
					log.debug("Marker " + marker.getLabel()
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
	 * Given the label of a target, returns true if the consensus sequence,
	 * which is generated from the supplied pssm, "hits" the sequence of this
	 * target. The concensus sequence is created internally from the pssm data.
	 * This method does not tell you where the hit occured, but just that it has
	 * occured.
	 * 
	 * @param pssm
	 *            The pssm data for a given (generated) rule
	 * @param targetLabel
	 * @return boolean
	 */
	public static boolean isHitByPssm(double[][] pssm, double threshold,
			String targetLabel, String sequencePath) {

		String concensusSequence = generateConsensusSequence(pssm);

		return isHitByConsensusSequence(concensusSequence, pssm, threshold,
				targetLabel, sequencePath);

	}

	/**
	 * Given the label of a target, returns true if the consensus sequence
	 * "hits" the sequence of this target. The concensus sequence is created
	 * internally from the pssm data. This method does not tell you where the
	 * hit occured, but just that it has occured.
	 * 
	 * @param consensusSequence
	 * @param pssm
	 * @param threshold
	 * @param targetLabel
	 * @param sequencePath
	 * @return
	 */
	public static boolean isHitByConsensusSequence(String consensusSequence,
			double[][] pssm, double threshold, String targetLabel,
			String sequencePath) {

		double score = 0;

		// threshold = Math.log(threshold);

		Map<String, int[]> targetSequenceMap = MedusaUtil
				.getSequences(sequencePath);
		int[] numericSequence = targetSequenceMap.get(targetLabel);

		int boundary = numericSequence.length - consensusSequence.length() + 1;
		for (int i = 0; i < boundary; i++) {
			int start = i;
			int end = i + (consensusSequence.length());
			int[] windowSequence = new int[end - start];
			int k = 0;
			for (int j = start; j < end; j++) {
				windowSequence[k] = numericSequence[j];
				k++;
			}

			for (int l = 0; l < windowSequence.length; l++) {
				/*-1 since Leslie index starts at 1*/
				int numericNucleotide = windowSequence[l] - 1;

				double val = pssm[numericNucleotide][l];
				// FIXME Don't hardcode 0.25. Get the Perseus jar which has
				// these values
				score = score + (Math.log(val) - Math.log(0.25));
				// score = score + val;
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
	 * @param srules
	 * @param sequencePath
	 * @return
	 */
	public static boolean[][] generateHitOrMissMatrix(List<String> targetNames,
			List<SerializedRule> srules, String sequencePath) {
		boolean[][] hitOrMissMatrix = new boolean[targetNames.size()][srules
				.size()];

		int col = 0;
		for (SerializedRule srule : srules) {
			String consensusSequence = generateConsensusSequence(srule
					.getPssm());
			double threshold = srule.getPssmThreshold();

			int row = 0;
			for (String targetName : targetNames) {
				boolean isHit = MedusaUtil.isHitByConsensusSequence(
						consensusSequence, srule.getPssm(), threshold,
						targetName, sequencePath);
				hitOrMissMatrix[row][col] = isHit;
				row++;
			}
			col++;
		}

		return hitOrMissMatrix;
	}

	/**
	 * 
	 * @param ruleFiles
	 * @param rulePath
	 * @return
	 */
	public static ArrayList<SerializedRule> getSerializedRules(
			List<String> ruleFiles, String rulePath) {
		ArrayList<SerializedRule> srules = new ArrayList<SerializedRule>();
		for (String ruleFile : ruleFiles) {
			SerializedRule srule = null;
			try {
				srule = RuleParser.read(rulePath + ruleFile);
				srules.add(srule);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return srules;
	}

	/**
	 * Updates the config file with new parameters.
	 * 
	 * @param configFile
	 * @param outFile
	 *            If null, the configFile is overwritten.
	 * @return directory for output
	 */
	public static String updateConfigXml(String configFile, String outFile,
			MedusaCommand command) {
		Document doc = XmlReader.readXmlFile(configFile);

		updateXmlNode(doc, "parameters", "iterations", String.valueOf(command
				.getIter()));

		updateXmlNode(doc, "file", "file_fasta", command.getFeaturesFile());

		updateXmlNode(doc, "file", "kmers_smallest", String.valueOf(command
				.getMinKer()));

		updateXmlNode(doc, "file", "kmers_largest", String.valueOf(command
				.getMaxKer()));

		if (command.isUsingDimers()) {
			updateXmlNode(doc, "file", "dimers_allowed", String.valueOf(true));
			updateXmlNode(doc, "file", "dimers_maximum_gap", String
					.valueOf(command.getMaxGap()));
			// TODO In the default config file, there is no
			// dimers_minimum_gap. Also,
			// I have left the dimers_largest and dimers_smallest as the
			// defaults since
			// they are not in the use case.
		}

		else {
			updateXmlNode(doc, "file", "dimers_allowed", String.valueOf(false));
		}

		if (command.isReverseComplement()) {
			updateXmlNode(doc, "parameters",
					"reverse_complement_matches_complement", String
							.valueOf(true));
		} else {
			updateXmlNode(doc, "parameters",
					"reverse_complement_matches_complement", String
							.valueOf(false));
		}

		updateXmlNode(doc, "parameters", "pssms_maximum_length", String
				.valueOf(command.getPssmLength()));

		updateXmlNode(doc, "parameters", "pssms_number_to_agglomerate", String
				.valueOf(command.getAgg()));

		updateXmlNode(doc, "output", "dir_experiment", 
				FilePathnameUtils.getTemporaryFilesDirectoryPath() + "temp/medusa/dataset/output");

		Date now = new Date();
		long nowLong = now.getTime();
		updateXmlNode(doc, "output", "run_name", "run_"+String
				.valueOf(nowLong));
		//TODO: if multiple session could be run at the same time, we could add a string of random number after nowLong.
		
		if (outFile == null)
			outFile = configFile;

		XmlWriter.writeXml(doc, outFile);

		return "run_"+String.valueOf(nowLong);
	}

	/**
	 * 
	 * @param doc
	 * @param targetElement
	 * @param targetAttribute
	 * @param newAttributeVal
	 */
	private static void updateXmlNode(Document doc, String targetElement,
			String targetAttribute, String newAttributeVal) {
		// TODO move me to base stuff
		NodeList nodes = doc.getElementsByTagName(targetElement);
		Node node = nodes.item(0);
		NamedNodeMap nodeMap = node.getAttributes();
		/* fasta file */
		Node fastaFileNode = nodeMap.getNamedItem(targetAttribute);
		fastaFileNode.setNodeValue(newAttributeVal);

	}

	/**
	 * 
	 * @param filename
	 *            If filename is null, the default path is used for the output
	 *            PSSM file. This is
	 *            temp/medusa/dataset/output/pssm_random.nextLong().
	 * @param srules
	 *            The rules from the run. This contains the discovered pssms.
	 */
	public static void writePssmToFile(String filename,
			List<SerializedRule> srules) {

		if (StringUtils.isEmpty(filename)) {
			Random r = new Random();
			filename = "temp/medusa/dataset/output/pssm_"
					+ Math.abs(r.nextLong());
		}

		File file = new File(filename);
		Writer out;
		try {
			out = new BufferedWriter(new FileWriter(file));

			for (SerializedRule srule : srules) {
				/* write out comment */
				out.write("# ");
				// out.write("the comment");
				out.write("\n>"); // FASTA_PREFIX;

				/* write out name and description of pssm */
				out.write("PSSM Name: \t");
				out.write("PSSM Description: \n");

				/* write out each pssm */

				double[][] pssm = srule.getPssm();
				for (int i = 0; i < pssm.length; i++) {
					for (int j = 0; j < pssm[i].length; j++) {
						out.write(String.valueOf(pssm[i][j]));
						if (j < pssm[i].length - 1)
							out.write("\t");
						else
							out.write("\n");
					}
				}
			}
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(
					"Error writing out pssm to file.  Exception is: " + e);
		}

	}

	/**
	 * 
	 * @param filePath
	 * @return
	 */
	public static List<TranscriptionFactorInfoBean> readPssmFromFile(String filePath) {
		ArrayList<TranscriptionFactorInfoBean> TFInfoBeanArr = new ArrayList<TranscriptionFactorInfoBean>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(
					filePath)));
			String nextLine = null;
			String nextName = null, nextDescr = null;
			StringTokenizer strTok = null;
			double nextPssm[][];
			while ((nextLine = br.readLine()) != null) {
				// nextLine = br.readLine();
				if (nextLine.trim().startsWith(">")) {
					nextPssm = new double[4][];
					int index = nextLine.indexOf(' ');
					if (index == -1) {
						nextName = nextLine;
						nextDescr = "";
					} else {
						nextName = nextLine.substring(0, index);
						if (index != nextLine.length() - 1)
							nextDescr = nextLine.substring(index + 1, nextLine
									.length());
						else
							nextDescr = "";
					}
					for (int i = 0; i < 4; i++) {
						if ((nextLine = br.readLine()) == null)
							break;
						strTok = new StringTokenizer(nextLine, "\t");
						/*
						 * if(strTok.countTokens() <= 1){ strTok = new
						 * StringTokenizer(nextLine," ");
						 * if(strTok.countTokens() != 4){
						 * System.out.println("The PSSM: "+nextName+" in the
						 * loaded file is not in correct format !!"); break; } }
						 */
						int j = 0;
						nextPssm[i] = new double[strTok.countTokens()];
						while (strTok.hasMoreTokens())
							nextPssm[i][j++] = Double.parseDouble(strTok
									.nextToken());
					}
					TranscriptionFactorInfoBean nextTF = new TranscriptionFactorInfoBean();
					nextTF.setName(nextName.replace(">", ""));
					nextTF.setDescription(nextDescr);
					nextTF.setPssm(nextPssm);
					nextTF.setSource(filePath);
					TFInfoBeanArr.add(nextTF);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			log.error(e);
			return null;
		} catch (IOException e) {
			log.error(e);
			return null;
		}

		return TFInfoBeanArr;
	}

	/**
	 * 
	 * @param TFInfoBeanList
	 * @param file
	 * @return
	 */
	public static boolean writeMatchedPssmsToFile(List<TranscriptionFactorInfoBean> TFInfoBeanList, File file) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			for (Iterator<TranscriptionFactorInfoBean> itr = TFInfoBeanList.iterator(); itr.hasNext();) {
				TranscriptionFactorInfoBean nextBean = (TranscriptionFactorInfoBean) itr
						.next();
				out.write(">" + nextBean.getName());
				if (nextBean.getDescription() != null
						&& !nextBean.getDescription().equals("null"))
					out.write(" " + nextBean.getDescription());
				out.newLine();
				double pssm[][] = nextBean.getPssm();
				for (int i = 0; i < pssm.length; i++) {
					for (int j = 0; j < pssm[i].length; j++) {
						if (j == 0)
							out.write("\t" + String.valueOf(pssm[i][j]));
						else
							out.write(String.valueOf(pssm[i][j]));
					}
					out.newLine();
				}
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Reads the PSSM from a file in JASPAR related format.
	 * 
	 * @param filePath
	 * @return {@link List}
	 */
	public static List<TranscriptionFactorInfoBean> readPssmFromJasperFile(String filePath) {
		ArrayList<TranscriptionFactorInfoBean> TFInfoBeanArr = new ArrayList<TranscriptionFactorInfoBean>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(
					filePath)));
			String nextLine = null;
			StringTokenizer strTok = null;
			double nextPssm[][];
			int i = 0;
			ArrayList<Double> A = new ArrayList<Double>(), C = new ArrayList<Double>(), G = new ArrayList<Double>(), T = new ArrayList<Double>();
			String prevTF = "", nextTF;
			String nextNuc = "";

			double nextVal = 0.0;
			while ((nextLine = br.readLine()) != null) {
				strTok = new StringTokenizer(nextLine, "\t");
				if (strTok.countTokens() != 4)
					continue;
				i++;
				nextTF = strTok.nextToken();
				nextNuc = strTok.nextToken();
				Integer.parseInt(strTok.nextToken()); // ignore the result
				nextVal = Double.parseDouble(strTok.nextToken());

				if (i == 1 || !nextTF.equals(prevTF)) {
					if (i != 1) {
						// create new Bean and add into array
						TranscriptionFactorInfoBean nextBean = new TranscriptionFactorInfoBean();
						nextBean.setName(prevTF);
						nextBean.setSource(filePath);
						nextPssm = new double[4][];

						nextPssm[0] = new double[A.size()];
						int j = 0;
						for (Iterator<Double> itr = A.iterator(); itr.hasNext();) {
							Double val = (Double) itr.next();
							nextPssm[0][j++] = val.doubleValue();
						}
						nextPssm[1] = new double[C.size()];
						j = 0;
						for (Iterator<Double> itr = C.iterator(); itr.hasNext();) {
							Double val = (Double) itr.next();
							nextPssm[1][j++] = val.doubleValue();
						}
						nextPssm[2] = new double[G.size()];
						j = 0;
						for (Iterator<Double> itr = G.iterator(); itr.hasNext();) {
							Double val = (Double) itr.next();
							nextPssm[2][j++] = val.doubleValue();
						}
						nextPssm[3] = new double[T.size()];
						j = 0;
						for (Iterator<Double> itr = T.iterator(); itr.hasNext();) {
							Double val = (Double) itr.next();
							nextPssm[3][j++] = val.doubleValue();
						}
						nextBean.setPssm(nextPssm);
						TFInfoBeanArr.add(nextBean);
						A = new ArrayList<Double>();
						C = new ArrayList<Double>();
						G = new ArrayList<Double>();
						T = new ArrayList<Double>();
					}
				}
				if (nextNuc.equals("A"))
					A.add(new Double(nextVal));
				if (nextNuc.equals("C"))
					C.add(new Double(nextVal));
				if (nextNuc.equals("G"))
					G.add(new Double(nextVal));
				if (nextNuc.equals("T"))
					T.add(new Double(nextVal));
				prevTF = nextTF;
			}
			// adding the last read TF in the array
			if (i > 1) {
				TranscriptionFactorInfoBean nextBean = new TranscriptionFactorInfoBean();
				nextBean.setName(prevTF);
				nextBean.setSource(filePath);
				nextPssm = new double[4][];

				nextPssm[0] = new double[A.size()];
				int j = 0;
				for (Iterator<Double> itr = A.iterator(); itr.hasNext();) {
					Double val = (Double) itr.next();
					nextPssm[0][j++] = val.doubleValue();
				}
				nextPssm[1] = new double[C.size()];
				j = 0;
				for (Iterator<Double> itr = C.iterator(); itr.hasNext();) {
					Double val = (Double) itr.next();
					nextPssm[1][j++] = val.doubleValue();
				}
				nextPssm[2] = new double[G.size()];
				j = 0;
				for (Iterator<Double> itr = G.iterator(); itr.hasNext();) {
					Double val = (Double) itr.next();
					nextPssm[2][j++] = val.doubleValue();
				}
				nextPssm[3] = new double[T.size()];
				j = 0;
				for (Iterator<Double> itr = T.iterator(); itr.hasNext();) {
					Double val = (Double) itr.next();
					nextPssm[3][j++] = val.doubleValue();
				}
				nextBean.setPssm(nextPssm);
				TFInfoBeanArr.add(nextBean);
			}

			br.close();
		} catch (FileNotFoundException e) {
			log.error(e);
			return null;
		} catch (IOException e) {
			log.error(e);
			return null;
		}

		return TFInfoBeanArr;
	}

}
