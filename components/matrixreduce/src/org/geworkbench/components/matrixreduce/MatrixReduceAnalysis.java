package org.geworkbench.components.matrixreduce;

import com.larvalabs.chart.PSAMPlot;
import org.apache.commons.collections15.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.CSMatrixReduceSet;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.CSPositionSpecificAffinityMatrix;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.DSMatrixReduceSet;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.DSPositionSpecificAffintyMatrix;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Enumeration;

/**
 * @author John Watkinson
 * @author keshav
 */
public class MatrixReduceAnalysis extends AbstractAnalysis implements
		ClusteringAnalysis, Observer {
	Log log = LogFactory.getLog(this.getClass());

	private static final String TEMP_DIR = "temporary.files.directory";

	public static final String[] NUCLEOTIDES = { "A", "C", "G", "T" };
	public static final String[] NUCLEOTIDES_SMALL = { "a", "c", "g", "t" };

	private static final String ANALYSIS_DIR = "c:/tmp/matrixreduce/TestRun";
	private static final String MICROARRAY_SET_FILE_NAME = "microarraySet.txt";

	public MatrixReduceAnalysis() {
		setLabel("Matrix Reduce");
		setDefaultPanel(new MatrixReduceParamPanel());
	}

	// not used
	public int getAnalysisType() {
		return AbstractAnalysis.ZERO_TYPE;
	}

	private static double[][] convertScoresToWeights(double[][] psamData) {
		double[][] psamddG = new double[psamData.length][4];
		for (int i = 0; i < psamData.length; i++) {
			double logMean = 0;
			for (int j = 0; j < 4; j++) {
				logMean += Math.log(psamData[i][j]);
			}
			logMean /= 4;
			for (int j = 0; j < 4; j++) {
				psamddG[i][j] = Math.log(psamData[i][j]) - logMean;
			}
		}
		return psamddG;
	}

	public AlgorithmExecutionResults execute(Object input) {
		log.debug("input: " + input);
		// Use this to get params for MatrixREDUCE executable
		MatrixReduceParamPanel params = (MatrixReduceParamPanel) aspp;

		try {
			DSMicroarraySet<DSMicroarray> mSet = ((DSMicroarraySetView) input)
					.getMicroarraySet();
			// Write set out to tab-delimited format
			String tempDirParent = System.getProperty(TEMP_DIR);
			String tempDirName = "mr" + System.currentTimeMillis();
			File tempDir = new File(tempDirParent, tempDirName);
			tempDir.mkdirs();
			File microarrayFile = new File(MICROARRAY_SET_FILE_NAME);
			// Write out microarray data
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(microarrayFile)));
			out.print("ID");
			for (int i = 0; i < mSet.size(); i++) {
				out.print("\t" + mSet.get(i).getLabel());
			}
			out.println();
			DSItemList<DSGeneMarker> markers = mSet.getMarkers();
			for (int j = 0; j < markers.size(); j++) {
				out.print(markers.get(j).getLabel());
				for (int i = 0; i < mSet.size(); i++) {
					double v = mSet.getValue(j, i);
					if (Double.isNaN(v)) {
						out.print("\tNA");
					} else {
						out.print("\t" + v);
					}
				}
				out.println();
			}
			out.close();

			// Copy sequence file in to temp dir
			File sequenceSource = new File(params.getSequenceFile());
			File sequenceFile = new File(sequenceSource.getName());
			Util.copyFile(new FileInputStream(sequenceSource), sequenceFile);
			// get params and construct query
			int flank = params.getFlank();
			double pval = params.getPValue();
			int dyadLength = params.getDyadLength();
			int minGap = params.getMinGap();
			int minCounts = params.getMinCounts();
			int maxGap = params.getMaxGap();
			int maxIteration = params.getMaxIteration();
			int maxMotif = params.getMaxMotif();

			String[] args = { "MatrixREDUCE.exe",
					"-sequence=" + sequenceFile.getName(),
					"-expression=" + microarrayFile.getPath(), "-p=" + pval,
					"-fl=" + flank, "-dy=" + dyadLength, "-min_g=" + minGap,
					"-min_c=" + minCounts, "-max_g=" + maxGap,
					"-max_i=" + maxIteration, "-max_m=" + maxMotif,
					"-o=" + tempDir.getAbsolutePath() };
			log.warn("invoking MatrixREDUCE with: " + args.toString());

			// invoking MatrixREDUCE.exe
			Process process = null;
			ProgressBar pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
			pb.setTitle("MatrixREDUCE");
			pb.setMessage("Running MatrixREDUCE...");
			pb.start();
			boolean completed = false;

			try {
				// FIXME refactor to construct query from params from the
				// MatrixReduceParamPanel.
				ProcessBuilder builder = new ProcessBuilder(args);
				Map<String, String> env = builder.environment();
				env.clear();

				// env.put("Path", "components\\matrixreduce\\lib");
				java.util.Properties props = (java.util.Properties) System
						.getProperties();
				// Ecipse specific setup stuff
				if (props.getProperty("components.dir") == null) {
					env.put("Path", "components\\matrixreduce\\lib");
				} else {
					env.put("Path", props.getProperty("components.dir")
							+ "\\matrixreduce\\lib");
				}
				Runtime.getRuntime();

				process = builder.start();
				final Process proc = process;
				pb.addObserver(new Observer() {
					public void update(Observable o, Object arg) {
						proc.destroy();
					}
				});
				InputStream is = process.getErrorStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;

				System.out.println("STDERR:");
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
				completed = (process.waitFor() == 0);

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (process != null) {
					process.destroy();
				}
				pb.stop();
			}
			if (completed) {

				// Parse FASTA sequence data
				File fastaFile = new File(params.getSequenceFile());

				ListOrderedMap<String, String> sequenceMap = new ListOrderedMap<String, String>();
				{
					BufferedReader in = new BufferedReader(new FileReader(
							fastaFile));
					String line = in.readLine();
					String gene = null;
					StringBuffer sequence = null;
					while (line != null) {
						if (line.startsWith(">")) {
							if (gene != null) {
								sequenceMap.put(gene, sequence.toString()
										.toUpperCase());
							}
							sequence = new StringBuffer();
							gene = line.substring(1).split(" ")[0];
						} else {
							sequence.append(line.trim());
						}
						line = in.readLine();
					}
					if (gene != null) {
						sequenceMap
								.put(gene, sequence.toString().toUpperCase());
					}
				}
				// Parse results
				File[] files = tempDir.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						if (name.startsWith("matrix") && name.endsWith("out")) {
							return true;
						} else {
							return false;
						}
					}
				});
				DSMatrixReduceSet dataSet = new CSMatrixReduceSet(mSet,
						"MatrixREDUCE Results");
				for (File file : files) {
					int aIndex = file.getName().lastIndexOf('_');
					int bIndex = file.getName().lastIndexOf('.');
					if ((aIndex == -1) || (bIndex == -1)) {
						continue;
					}
					int i = -1;
					try {
						i = Integer.parseInt(file.getName().substring(
								aIndex + 1, bIndex));
					} catch (NumberFormatException nfe) {
						continue;
					}
					try {
						BufferedReader in = new BufferedReader(new FileReader(
								file));
						// Line 1
						String line = in.readLine();
						String[] tokens = line.split(" ");
						String seed = tokens[0];
						String experiment = tokens[2];
						// Line 2
						line = in.readLine();
						tokens = line.split(" ");
						double pValue = Double.parseDouble(tokens[0]);
						// Line 3
						line = in.readLine();
						tokens = line.split(" ");
						long bonferroni = Long.parseLong(tokens[0]);
						// Line 4
						line = in.readLine();
						tokens = line.split(" ");
						int strand = Integer.parseInt(tokens[0]);
						// Line 5 - ignore
						line = in.readLine();
						// Remaining lines have PSAM data
						DSPositionSpecificAffintyMatrix psam = new CSPositionSpecificAffinityMatrix();
						if (strand != 0) {
							psam.setTrailingStrand(true);
						} else {
							psam.setTrailingStrand(false);
						}
						psam.setBonferroni(bonferroni);
						psam.setSeedSequence(seed);
						psam.setExperiment(experiment);
						psam.setPValue(pValue);
						StringBuffer sb = new StringBuffer();
						line = in.readLine();
						ArrayList<double[]> values = new ArrayList<double[]>();
						while (line != null) {
							tokens = line.split("\t");
							int best = 0;
							double bestValue = -1;
							double[] vals = new double[4];
							for (int j = 0; j < 4; j++) {
								double v = Double.parseDouble(tokens[1 + j]);
								vals[j] = v;
								if (v > bestValue) {
									bestValue = v;
									best = j;
								}
							}
							sb.append(NUCLEOTIDES[best]);
							values.add(vals);
							line = in.readLine();
						}
						int n = values.size();
						double[][] scores = new double[n][4];
						for (int j = 0; j < n; j++) {
							scores[j] = values.get(j);
						}
						psam.setScores(scores);
						psam.setConsensusSequence(sb.toString());
						// Generate logo
						PSAMPlot psamPlot = new PSAMPlot(
								convertScoresToWeights(scores));
						psamPlot.setMaintainProportions(false);
						psamPlot.setAxisDensityScale(4);
						psamPlot.setAxisLabelScale(3);
						// psamPlot.setAxisTitleScale(3);
						BufferedImage image = new BufferedImage(
								MatrixReduceViewer.IMAGE_WIDTH,
								MatrixReduceViewer.IMAGE_HEIGHT,
								BufferedImage.TYPE_INT_RGB);
						Graphics2D graphics = (Graphics2D) image.getGraphics();
						psamPlot.layoutChart(MatrixReduceViewer.IMAGE_WIDTH,
								MatrixReduceViewer.IMAGE_HEIGHT, graphics
										.getFontRenderContext());
						psamPlot.paint(graphics);
						ImageIcon psamImage = new ImageIcon(image);
						// Load logo - no longer used.
						// File logoFile = new File(file.getParentFile(),
						// file.getName().replace(".out", ".png"));
						// ImageIcon psamImage = new
						// ImageIcon(logoFile.getAbsolutePath());
						psam.setPsamImage(psamImage);
						dataSet.add(psam);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Exception parsing " + file + ".");
					}
				}
				dataSet.setSequences(sequenceMap);
				return new AlgorithmExecutionResults(true, "Completed.",
						dataSet);
			} else {
				return new AlgorithmExecutionResults(false, "Cancelled", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static void writePSAM(DSPositionSpecificAffintyMatrix psam,
			PrintWriter out) throws IOException {
		out.println(psam.getSeedSequence() + " # " + psam.getExperiment());
		out.println("" + psam.getPValue() + " # p-value");
		out.println("" + psam.getBonferroni() + " # bonferroni");
		if (psam.isTrailingStrand()) {
			out.println("1 # derived from trailing strand");
		} else {
			out.println("0 # derived from leading strand");
		}
		// Header
		out
				.println("#\ta                   \tc                   \tg                   \tt                   ");
		double[][] scores = psam.getScores();
		for (double[] values : scores) {
			int best = 0;
			double bestValue = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < values.length; i++) {
				if (values[i] > bestValue) {
					bestValue = values[i];
					best = i;
				}
			}
			out.print(NUCLEOTIDES_SMALL[best]);
			for (int i = 0; i < values.length; i++) {
				out.printf("\t%1.20f", values[i]);
			}
			out.println();
		}
	}

	@Publish
	public DSMatrixReduceSet publishMatrixReduceSet(DSMatrixReduceSet data) {
		return data;
	}
}
