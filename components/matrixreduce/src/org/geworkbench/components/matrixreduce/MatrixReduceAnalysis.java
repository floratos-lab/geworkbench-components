package org.geworkbench.components.matrixreduce;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import org.apache.commons.collections15.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bussemakerlab.MatrixREDUCE.XML.BasePair;
import org.bussemakerlab.MatrixREDUCE.XML.Experiment;
import org.bussemakerlab.MatrixREDUCE.XML.Psam;
import org.bussemakerlab.MatrixREDUCE.XML.Slope;
import org.bussemakerlab.MatrixREDUCE.engine.MatrixREDUCE;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.CSMatrixReduceExperiment;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.CSMatrixReduceSet;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.CSPositionSpecificAffinityMatrix;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.DSMatrixReduceExperiment;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.DSMatrixReduceSet;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.DSPositionSpecificAffintyMatrix;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectNodePostCompletedEvent;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;

/**
 * @author John Watkinson
 * @author keshav
 * @author ch2514
 * @version $Id$
 */
public class MatrixReduceAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis, Observer {
	private static final long serialVersionUID = -4742601328385348469L;

	Log log = LogFactory.getLog(this.getClass());

	private static final String TEMP_DIR = FilePathnameUtils.getTemporaryFilesDirectoryPath();

	private static final String MICROARRAY_SET_FILE_NAME = "microarraySet.tsv";

	private static final String SEQUENCE_FILE_NAME = "sequence.fa";

	private static final String TOPOLOGY_FILE_NAME = "test.topo";

	public static final String[] NUCLEOTIDES = { "A", "C", "G", "T" };

	public static final String[] NUCLEOTIDES_SMALL = { "a", "c", "g", "t" };

	public static final String DELIMITER = "#";

	public static final String DYNAMIC_DIRECTION = "dynamic";

	public static final String PARAM_SEQUENCE = "sequence";

	public static final String PARAM_TOPOLOGY = "topology";

	public static final String PARAM_PVALUE = "pvalue";

	public static final String PARAM_MAXMOTIF = "maxmotif";

	public static final String PARAM_STRAND = "strand";

	private final String analysisName = "MatrixREDUCE";

	MatrixReduceParamPanel params = null;

	private ProgressBar progressBar = null;

	private MatrixREDUCE mr;

	private int exitVal = -1;

	private DSMatrixReduceSet dataSet = null;

	private File tempDir = null;

	public MatrixReduceAnalysis() {
		setDefaultPanel(new MatrixReduceParamPanel());
		params = (MatrixReduceParamPanel) aspp;
	}

	public Map<Serializable, Serializable> getBisonParameters() {
		Map<Serializable, Serializable> parameterMap = new HashMap<Serializable, Serializable>();

		// seq file - read from file
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		String seq = "";
		try {
			String seqfilepath = params.getSequenceFile();
			log.info("seq file=" + seqfilepath);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					seqfilepath)));
			sb = new StringBuilder();
			String s;
			while ((s = br.readLine()) != null) {
				sb.append(s + "\n");
			}
			seq = sb.toString();
		} catch (Exception e) {
			log.error("Cannot read topology file [" + params.getSequenceFile()
					+ "]: " + e.getMessage() + ": fasta sequence is empty.");
		} finally {
			try {
				br.close();
			} catch (Exception close) {
				log
						.error("Cannot close topology file ["
								+ params.getSequenceFile() + "]: "
								+ close.getMessage());
			}
		}
		parameterMap.put(PARAM_SEQUENCE, seq);

		// topo list - read from file if file is specified
		String topology = "";
		if (!params.getTopoFile().trim().equals("")) {
			br = null;
			sb = null;
			try {
				br = new BufferedReader(new InputStreamReader(
						new FileInputStream(params.getTopoFile())));
				sb = new StringBuilder();
				String s;
				while ((s = br.readLine()) != null) {
					sb.append(s + "\n");
				}
				topology = sb.toString();
			} catch (Exception e) {
				log.error("Cannot read topology file [" + params.getTopoFile()
						+ "]: " + e.getMessage()
						+ ": topology pattern is empty.");
			} finally {
				try {
					br.close();
				} catch (Exception close) {
					log
							.error("Cannot close topology file ["
									+ params.getTopoFile() + "]: "
									+ close.getMessage());
				}
			}
		} else {
			topology = params.getTopoPattern().toLowerCase();
		}
		parameterMap.put(PARAM_TOPOLOGY, topology);

		// pvalue
		parameterMap.put(PARAM_PVALUE, new Float(params.getPValue()));

		// maxMotif
		parameterMap.put(PARAM_MAXMOTIF, new Integer(params.getMaxMotif()));

		// strand
		parameterMap.put(PARAM_STRAND, new Integer(params.getStrand()));

		return parameterMap;
	}

	@Override
	public Class<?> getBisonReturnType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAnalysisName() {
		return this.analysisName;
	}

	// not used
	public int getAnalysisType() {
		return AbstractAnalysis.ZERO_TYPE;
	}

	public ParamValidationResults validateParameters() {
		MatrixReduceParamPanel params = (MatrixReduceParamPanel) aspp;
		String errMsg = "";
		// all 3 parameter ranges are validated right on the gui component
		// just need to verify that the required files/pattern are specified
		String file = params.getSequenceFile();
		if ((file == null) || file.trim().equals("")
				|| file.trim().equals(MatrixReduceParamPanel.FILE_SPECIFY)) {
			errMsg += "No sequence file specified.  ";
		}

		if (params.getTopoPattern().trim().equals("")) {
			file = params.getTopoFile();
			if ((file == null)
					|| file.trim().equals("")
					|| file.trim().equals(
							MatrixReduceParamPanel.PATTERN_REQUIRED)) {
				errMsg += "No topological pattern or file specified.  ";
			}
		}
		if (errMsg.equals(""))
			return new ParamValidationResults(true, null);
		else
			return new ParamValidationResults(false, errMsg);
	}

	@SuppressWarnings({ "rawtypes" })
	public AlgorithmExecutionResults execute(Object input) {
		if (input == null) {
			return new AlgorithmExecutionResults(false, "Invalid input.", null);
		}
		try {
			DSMicroarraySet mSet = ((DSMicroarraySetView) input)
					.getMicroarraySet();
			progressBar = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
			progressBar.addObserver(this);
			progressBar.setTitle("MatrixREDUCE");
			progressBar.setMessage("Processing Data Source");
			progressBar.start();
			// Write set out to tab-delimited format
			String tempDirParent = TEMP_DIR;
			String tempDirName = "mr";
			tempDir = new File(tempDirParent + tempDirName);
			tempDir.mkdirs();
			File microarrayFile = new File(FilePathnameUtils.getTemporaryFilesDirectoryPath() + MICROARRAY_SET_FILE_NAME);
			if (stopAlgorithm) {
				stopAlgorithm = false;
				progressBar.stop();
				return null;
			}
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

			if (stopAlgorithm) {
				stopAlgorithm = false;
				progressBar.stop();
				return null;
			}

			// Copy sequence file in to temp dir
			File sequenceSource = new File(params.getSequenceFile());
			File sequenceFileTemp = new File(SEQUENCE_FILE_NAME);
			Util
					.copyFile(new FileInputStream(sequenceSource),
							sequenceFileTemp);

			// Copy sequence file in to temp dir
			String s = params.getTopoFile();
			File topoFileTemp = null;
			if (!StringUtils.isEmpty(s)) {
				File topoSource = new File(s);
				topoFileTemp = new File(TOPOLOGY_FILE_NAME);
				Util.copyFile(new FileInputStream(topoSource), topoFileTemp);
			}

			if (stopAlgorithm) {
				stopAlgorithm = false;
				progressBar.stop();
				return null;
			}

			// get params and construct query
			float pval = (float) params.getPValue();
			int maxMotif = params.getMaxMotif();
			int strandNum = params.getStrand();

			// get file names
			String expressionFile = StringUtils.replace(microarrayFile
					.getPath(), "\\", "/");
			String sequenceFile = StringUtils.replace(sequenceFileTemp
					.getPath(), "\\", "/");
			String topoFile = "";
			if (topoFileTemp != null)
				topoFile = StringUtils.replace(topoFileTemp.getPath(), "\\",
						"/");

			String topology = "";
			String list = "";
			if (!StringUtils.isEmpty(topoFile)) {
				topology = topoFile;
				list = "-list";
			} else {
				/*
				 * @author - Nikhil
				 * The topological pattern is converted into temporary topofile and supplied to 'FitModel'.
				 * This change is made since 'FitModel' doesn't work on Linux and Mac if we supply pattern as an argument.
				 * We don't have source for 'FitModel' to fix that issue. 
				 */
				String topologyPattern = params.getTopoPattern().toLowerCase();
                topoFileTemp = new File(TOPOLOGY_FILE_NAME);
                InputStream is = new ByteArrayInputStream(topologyPattern.getBytes("UTF-8"));
                Util.copyFile(is, topoFileTemp);
                topoFile = StringUtils.replace(topoFileTemp.getPath(), "\\",
                "/");
                topology = topoFile;
                list = "-list";
			}

			String[] newArgs = { "-se=" + sequenceFile, "-e=" + expressionFile,
					"-topo=" + topology, list, "-o=" + TEMP_DIR,
					"-p_value=" + pval, "-max_motif=" + maxMotif,
					"-strand=" + strandNum };

			String NEWARGS = "";
			for (int i = 0; i < newArgs.length; i++) {
				NEWARGS += newArgs[i] + " ";
			}

			if (stopAlgorithm) {
				stopAlgorithm = false;
				progressBar.stop();
				return null;
			}
			log.info("Running ./FitModel " + NEWARGS);
			progressBar.setMessage("Running FitModel");
			mr = new MatrixREDUCE(newArgs);
			exitVal = mr.run();
			log.info("Exit Value: " + exitVal + "\n");
			log.info("STDOUT: " + mr.stdout);
			log.info("STDERR: " + mr.stderr);

			if (stopAlgorithm) {
				stopAlgorithm = false;
				progressBar.stop();
				return null;
			}

			if (exitVal == 0) {

				progressBar
						.setMessage("Constructing sequence data for display");
				// Sequences: Parse FASTA sequence data
				File fastaFile = new File(sequenceFile);

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
					in.close();
					if (gene != null) {
						sequenceMap
								.put(gene, sequence.toString().toUpperCase());
					}
				}

				dataSet = new CSMatrixReduceSet(mSet, "MatrixREDUCE Results");

				if (stopAlgorithm) {
					stopAlgorithm = false;
					progressBar.stop();
					return null;
				}

				progressBar.setMessage("Creating PSAMs");
				log.debug("calling mr.getResults()");
				try {
					mr.getResults();
					log.debug("called mr.getResults()");
				} catch (Exception mre) {
					log.error("Cannot get results from MatrixREDUCE run.", mre);
				}

				if (stopAlgorithm) {
					stopAlgorithm = false;
					progressBar.stop();
					return null;
				}

				// Experiments
				ArrayList<Psam> psams = mr.mrRes.Results.getPsam();
				log.debug("mr.mrRes.Results.getPsam().size()=" + psams.size());
				ArrayList<Experiment> exps = mr.mrRes.Results.getExperiment();
				ListOrderedMap<String, List<DSMatrixReduceExperiment>> expMap = new ListOrderedMap<String, List<DSMatrixReduceExperiment>>();
				HashMap<String, DSMatrixReduceExperiment> tempMap = new HashMap<String, DSMatrixReduceExperiment>(
						exps.size());
				for (int i = 0; i < psams.size(); i++) {
					expMap
							.put("" + psams.get(i).get_psam_id(),
									new ArrayList<DSMatrixReduceExperiment>(
											exps.size()));
				}
				for (int i = 0; i < exps.size(); i++) {
					Experiment experiment = exps.get(i);
					ArrayList<Slope> slopes = exps.get(i).getMultivariateFit()
							.getSlope();
					for (int j = 0; j < slopes.size(); j++) {
						DSMatrixReduceExperiment exp = new CSMatrixReduceExperiment();
						Slope slope = slopes.get(j);
						exp.setID("" + experiment.get_expt_id());
						exp.setLabel(experiment.get_description().trim());
						exp.setPsamId("" + slope.psam_id);
						tempMap
								.put(exp.getLabel() + "#" + exp.getPsamId(),
										exp);
						exp.setCoeff(slope.get_coeff());
						exp.setPValue(slope.get_p_value());
						exp.setTValue(slope.get_t_value());
						expMap.get(exp.getPsamId()).add(exp);
					}
				}

				if (stopAlgorithm) {
					stopAlgorithm = false;
					progressBar.stop();
					return null;
				}

				// PSAMs
				for (int i = 0; i < psams.size(); i++) {
					DSPositionSpecificAffintyMatrix psam = new CSPositionSpecificAffinityMatrix();
					psam.setID("" + psams.get(i).get_psam_id());
					psam.setExperiment(psams.get(i)
							.get_experiment_description().trim());
					DSMatrixReduceExperiment bestExp = tempMap.get(psam
							.getExperiment()
							+ "#" + psam.getID());
					psam.setExperimentID(bestExp.getID());
					psam.setPValue(bestExp.getPValue());
					psam.setTValue(bestExp.getTValue());
					psam.setCoeff(bestExp.getCoeff());
					if (psams.get(i).get_directionality().toLowerCase()
							.indexOf(DYNAMIC_DIRECTION) >= 0) {
						psam.setTrailingStrand(true);
					} else {
						psam.setTrailingStrand(false);
					}
					psam.setSeedSequence(psams.get(i).get_seed_motif());
					psam.setConsensusSequence(psams.get(i)
							.get_optimal_sequence());

					// n is the position
					// 4 is a, c, g, t
					ArrayList<BasePair> affPos = psams.get(i).getAffinities()
							.getBasePair();
					int n = affPos.size();
					double[][] scores = new double[n][4];
					for (int j = 0; j < n; j++) {
						scores[j][0] = affPos.get(j).get_a();
						scores[j][1] = affPos.get(j).get_c();
						scores[j][2] = affPos.get(j).get_g();
						scores[j][3] = affPos.get(j).get_t();
					}
					psam.setScores(scores);
					dataSet.add(psam);
				}
				dataSet.setSequences(sequenceMap);
				dataSet.setMatrixReduceExperiments(expMap);
				tempMap = null;

				if (stopAlgorithm) {
					stopAlgorithm = false;
					progressBar.stop();
					return null;
				}

				if (params.saveRunLog()) {
					log.info("Saving run log to project history.");
					progressBar.setMessage("Saving Run Log");
					if (!StringUtils.isEmpty(mr.stderr)) {
						HistoryPanel
								.addToHistory(
										dataSet,
										params.toString()
												+ "\nMatrixREDUCE Output:\n----------------------------------------\n"
												+ StringUtils
														.replace(
																StringUtils
																		.replace(
																				mr.stderr
																						.substring(mr.stderr
																								.indexOf("\n")),
																				TOPOLOGY_FILE_NAME,
																				params
																						.getTopoFile()),
																SEQUENCE_FILE_NAME,
																params
																		.getSequenceFile()));
						dataSet.setRunLog(mr.stderr);
					} else {
						HistoryPanel
								.addToHistory(
										dataSet,
										params.toString()
												+ "\nMatrixREDUCE Output:\n----------------------------------------\n"
												+ mr.stdout);
						dataSet.setRunLog(mr.stdout);
					}
				} else {					
					StringBuilder paramDescB = new StringBuilder(
							"Running MatrixReduce Analysis:\n");
					paramDescB.append("Sequence: "+params.getSequenceFile()+"\n");
					paramDescB.append("Topological Pattern: "+params.getTopoPattern()+"\n");
					paramDescB.append("p Value: "+params.getPValue()+"\n");
					paramDescB.append("Max Motif: "+params.getMaxMotif()+"\n");
					paramDescB.append("Strand: "+params.getStrandString()+"\n");
					HistoryPanel.addToHistory(dataSet, paramDescB.toString());
				}
				progressBar.stop();
				return new AlgorithmExecutionResults(true, "Completed", dataSet);
			} else {
				// TODO:
				if (tempDir == null) {
					tempDir = new File(tempDirParent + tempDirName);
					tempDir.mkdirs();
				}
				String slash = "/";
				if (System.getProperty("os.name").toLowerCase().indexOf(
						"window") >= 0)
					slash = "\\";
				String errLogFileName = tempDir.getAbsolutePath() + slash
						+ "MatrixREDUCE_ErrorLog.txt";
				PrintWriter errout = new PrintWriter(new BufferedWriter(
						new FileWriter(errLogFileName)), true);
				String errMsg = StringUtils.replace(StringUtils.replace(
						mr.stderr, TOPOLOGY_FILE_NAME, params.getTopoFile()),
						SEQUENCE_FILE_NAME, params.getSequenceFile());
				errout.println(errMsg);
				errout.close();
				progressBar.stop();
				return new AlgorithmExecutionResults(false,
						"MatrixREDUCE run error:  see " + errLogFileName, null);
			}
		} catch (Throwable e) {
			log.error("Runtime error while running MatrixREDUCE", e);
			progressBar.stop();
			return new AlgorithmExecutionResults(false,
					"MatrixREDUCE cannot run: " + e.getMessage(), null);
		}

	}

	public static void writePSAM(DSPositionSpecificAffintyMatrix psam,
			PrintWriter out) throws IOException {
		out.println(psam.getSeedSequence() + " # " + psam.getExperiment());
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

	/*
	 * (non-Javadoc)
	 *
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#useMicroarraySetView()
	 */
	@Override
	protected boolean useMicroarraySetView() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#useOtherDataSet()
	 */
	@Override
	protected boolean useOtherDataSet() {
		return false;
	}

	public void update(java.util.Observable ob, Object o) {
		log.warn("Cancelling MatrixREDUCE Analysis.");
		stopAlgorithm = true;
		if (mr != null)
			mr.destroy();
	}

	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet<?> refMASet) {
		// FIXME: we should do some checking before analysis.
		return new ParamValidationResults(true, "Didn't check");
	}

	@Subscribe
	public void receive(ProjectNodePostCompletedEvent projectNodeCompleteEvent,
			Object source) {
		if(params.saveRunLog()){
			DSDataSet<? extends DSBioObject> data = projectNodeCompleteEvent.getAncillaryDataSet();
			if ((data != null) && (data instanceof DSMatrixReduceSet)) {
				String runlog = ((DSMatrixReduceSet) data).getRunLog();
				if (!StringUtils.isEmpty(runlog)) {
					log.info("Received run log from grid service.");
					HistoryPanel.addToHistory(data,
							"\nMatrixREDUCE Output:\n----------------------------------------\n"
									+ runlog);
				}
			}
		}
	}
}
