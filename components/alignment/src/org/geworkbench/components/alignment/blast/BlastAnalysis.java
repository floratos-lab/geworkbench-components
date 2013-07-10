package org.geworkbench.components.alignment.blast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.sequence.BlastObj;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSAlignmentResultSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ProteinSequenceAnalysis;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.components.alignment.blast.RemoteBlast.NcbiResponseException;
import org.geworkbench.components.alignment.blast.RemoteBlast.Status;
import org.geworkbench.util.BrowserLauncher;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.ProgressBar;

/**
 * BlastAlgorithm.
 * 
 * @author XZ
 * @author zji
 * @version $Id$
 */
public class BlastAnalysis extends AbstractAnalysis implements
		ProteinSequenceAnalysis {

	private static final long serialVersionUID = -7530924672555933168L;

	private static Log log = LogFactory.getLog(BlastAnalysis.class);

	private BlastAnalysisPanel blastAnalysisPanel = null;

	private boolean startBrowser;
	private boolean jobFinished = false;

	private CSSequenceSet<CSSequence> activeSequenceDB;
	private DSSequenceSet<? extends DSSequence> sequenceDB;

	private final static int TIMEGAP = 4000;
	private final static String LINEBREAK = System
			.getProperty("line.separator");

	public BlastAnalysis() {
		blastAnalysisPanel = new BlastAnalysisPanel();
		setDefaultPanel(blastAnalysisPanel);
	}

	public int getAnalysisType() {
		return AbstractAnalysis.BLAST_TYPE;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public AlgorithmExecutionResults execute(Object input) {

		if (input == null || !(input instanceof DSSequenceSet)) {
			return new AlgorithmExecutionResults(false, "Invalid input. ", null);
		}

		sequenceDB = (DSSequenceSet<?>) input;

		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<?> context = manager.getCurrentContext(sequenceDB
				.getMarkerList());

		DSPanel<? extends DSGeneMarker> activatedMarkers = (DSPanel<? extends DSGeneMarker>) context
				.getActiveItems().activeSubset();

		if (activatedMarkers != null && activatedMarkers.size() > 0) {
			activeSequenceDB = (CSSequenceSet<CSSequence>) ((CSSequenceSet) sequenceDB)
					.getActiveSequenceSet(activatedMarkers);
		} else if (sequenceDB != null) {

			activeSequenceDB = (CSSequenceSet<CSSequence>) sequenceDB;
		}

		ParameterSetting parameterSetting = blastAnalysisPanel
				.collectParameters();
		if (parameterSetting == null) {
			return new AlgorithmExecutionResults(false,
					"The parameter setting is null.", null);
		}

		parameterSetting.setUseNCBI(true);

		ProgressBar pbTtest = ProgressBar
				.create(ProgressBar.INDETERMINATE_TYPE);
		if (activeSequenceDB != null) {
			try {
				 
				pbTtest.addObserver(this);
				pbTtest.setTitle("Blast Analysis");
				int seqNum = activeSequenceDB.size();
				pbTtest.setBounds(new ProgressBar.IncrementModel(0, seqNum, 0,
						seqNum, 1));

				pbTtest.start();
				this.stopAlgorithm = false;

				String tempFolder = FilePathnameUtils
						.getTemporaryFilesDirectoryPath();

				/* generate a new file name for the coming output file. */
				String outputFile = tempFolder + "Blast"
						+ RandomNumberGenerator.getID() + ".html";

				List<Vector<BlastObj>> blastDataSet = new ArrayList<Vector<BlastObj>>();
				for (CSSequence sequence : activeSequenceDB) {
					if (this.stopAlgorithm) {
						return new AlgorithmExecutionResults(false,
								"NCBI Blast is canceled at " + new Date(), null);
					}
					pbTtest.setMessage("Uploading sequence: " + sequence);
				 
					RemoteBlast blast = new RemoteBlast(sequence.getSequence(), outputFile,
							AlgorithmMatcher
									.translateToCommandline(parameterSetting));
					String BLAST_rid = null;
					try {
						BLAST_rid = blast.submitBlast();
					} catch (NcbiResponseException e1) {
						// processExceptionFromNcbi(e1, sequence);
						String exceptionName = e1.getClass().getName();
						String msg = "Sequence " + sequence
								+ " cannot be blasted due to " + exceptionName
								+ ":\n" + e1.getMessage();
 

						return new AlgorithmExecutionResults(false, msg, null);
					}

					if (BLAST_rid == null) {

						pbTtest.setMessage("NCBI Blast is stopped at "
								+ new Date());
					 
						String msg = "Sequence "
								+ sequence
								+ " cannot be blasted, please check your parameters.";
						return new AlgorithmExecutionResults(false, msg, null);

					}
					pbTtest.setMessage("The Request ID is : " + BLAST_rid);
			 
					String resultURLString = "http://blast.ncbi.nlm.nih.gov/Blast.cgi?CMD=Get&FORMAT_TYPE=HTML&RID="
							+ BLAST_rid+"&ALIGNMENTS="+parameterSetting.getMaxTargetNumber();
					Status status = blast.retrieveResult(resultURLString);
					while (status == Status.WAITING && !this.stopAlgorithm) {
					 
						pbTtest.setMessage("For sequence " + sequence
								+ ",  the blast job is running. ");
						try {
							Thread.sleep(TIMEGAP);
						} catch (InterruptedException e) {
							// do nothing
						}
						status = blast.retrieveResult(resultURLString);
					}
					if (this.stopAlgorithm) {
					 
						return new AlgorithmExecutionResults(false,
								"NCBI Blast is canceled at " + new Date(), null);
					} else if (status != Status.READY) {
						String errMsg = parseError(outputFile);
						// processExceptionFromNcbi(new Exception(errMsg),
						// sequence);

						String msg = "Sequence " + sequence
								+ " cannot be blasted due to" + ":\n" + errMsg;
 
						return new AlgorithmExecutionResults(false, msg, null);

					}
					
					Vector<BlastObj> oneSequenceResult = new BlastXmlParser(
							BLAST_rid, parameterSetting.getMaxTargetNumber())
							.getResult();
					if (oneSequenceResult != null) {
						blastDataSet.add(oneSequenceResult);
					} else {
						log.warn("null result for sequence " + sequence
								+ " RID=" + BLAST_rid);
					}
				}
				if (this.stopAlgorithm) {
				 
					return new AlgorithmExecutionResults(false,
							"NCBI Blast is canceled at " + new Date(), null);
				}
			 
				String outputFilePath = "file://"
						+ new File(outputFile).getAbsolutePath();

				if (parameterSetting.isViewInBrowser()) {
					if ((new File(outputFile)).canRead()) {
						try {
							String osName = System.getProperty("os.name");
							if (osName.startsWith("Mac OS")) {
								BrowserLauncher.openURL(outputFilePath);
							} else {
								BrowserLauncher.openURL("file:///"
										+ new File(outputFile)
												.getAbsolutePath().replace(
														"\\", "/").replace(" ",
														"%20"));
							}
						} catch (Exception ex) {
							ex.printStackTrace();
							JOptionPane.showMessageDialog(null,
									"No web browser can be launched, the result is saved at "
											+ outputFile, "No Web Browser",
									JOptionPane.ERROR_MESSAGE);

						}
					} else {

						JOptionPane.showMessageDialog(null,
								"The result cannot be read at " + outputFile,
								"File cannot be read",
								JOptionPane.ERROR_MESSAGE);

					}

				}
				
				NCBIBlastParser nbp = new NCBIBlastParser(sequenceDB.size(),
						outputFile, blastDataSet);
			 	nbp.parseResults(); // this is to parse the detail from the saved file

				CSAlignmentResultSet blastResult = new CSAlignmentResultSet(
						blastDataSet, sequenceDB, BlastAnalysisPanel.NCBILABEL);
 
				if (this.stopAlgorithm) {				 
					return new AlgorithmExecutionResults(false,
							"NCBI Blast is canceled at " + new Date(), null);
				}
				String historyStr = generateHistoryStr(activeSequenceDB,
						parameterSetting);
				HistoryPanel.addToHistory(blastResult, historyStr);
				return new AlgorithmExecutionResults(true, "Blast", blastResult);

			} catch (Exception e) {				
				e.printStackTrace();
				return new AlgorithmExecutionResults(false, e.getMessage(),
						null);

			} finally {
				pbTtest.dispose();
			}

		}
		else
		    return new AlgorithmExecutionResults(false, "There is an internal error, please contact developer.",
				null);


	}	 

	private static String parseError(String outputFile) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(outputFile));
			String line = br.readLine();
			while (line != null) {
				int index = line.indexOf("Error: ");				 
				if (index >= 0) {					 
					int index2 = line.indexOf("<", index);
					if (index2 > 0 )
					  return line.substring(index, index2);
					else
					  return line.substring(index);
				}
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "FileNotFoundException in parsing error";
		} catch (IOException e) {
			e.printStackTrace();
			return "IOException in parsing error";
		} finally {
			try {
				if (br != null)
				br.close();
			} catch (IOException e) {
			}
		}
		return "Other error (not able to be parsed)";
	}
	 

	private String generateHistoryStr(
			CSSequenceSet<CSSequence> activeSequenceDB,
			ParameterSetting parameterSetting) {
		String histStr = "";
		if (parameterSetting != null) {

			// Header
			histStr += "Blast run with the following parameters:\n";
			histStr += "----------------------------------------\n";

			String dbName = parameterSetting.getDbName();
			histStr += "Database: " + dbName + LINEBREAK;
			histStr += "BLAST Program: " + parameterSetting.getProgramName()
					+ LINEBREAK;

			histStr += "Exclude of Models(XM/XP): "
					+ parameterSetting.isExcludeModelsOn() + LINEBREAK;
			histStr += "Exclude of Uncultured/environmental sequences: "
					+ parameterSetting.isExcludeUncultureOn() + LINEBREAK;
			if (parameterSetting.getFromQuery() != null)
				histStr += "Query subrange from: "
						+ parameterSetting.getFromQuery() + LINEBREAK;
			if (parameterSetting.getToQuery() != null)
				histStr += "Query subrange to: "
						+ parameterSetting.getToQuery() + LINEBREAK;
			if (parameterSetting.getEntrezQuery() != null)
				histStr += "Entrez Query: " + parameterSetting.getEntrezQuery()
						+ LINEBREAK;
			if (parameterSetting.getProgramName().equalsIgnoreCase("blastn")) {
				String optimizeFor = "";
				if (parameterSetting.isMegaBlastOn())
					optimizeFor = "Highly similar sequences (megablast)";
				else if (parameterSetting.isDiscontiguousOn())
					optimizeFor = "More dissimilar sequences (discontiguous megablast)";
				else
					optimizeFor = "Optimize for Somewhat similar sequences (blastn)";
				histStr += "Optimize For: " + optimizeFor + LINEBREAK;
			}
			if (parameterSetting.getProgramName().equalsIgnoreCase("blastx")
					|| parameterSetting.getProgramName().equalsIgnoreCase(
							"tblastx")) {
				histStr += "Genetic Code: " + parameterSetting.getGeneticCode()
						+ LINEBREAK;
			}

			histStr += "Short Queries: " + parameterSetting.isShortQueriesOn()
					+ LINEBREAK;

			histStr += "Expect: " + parameterSetting.getExpect() + LINEBREAK;
			histStr += "Word Size: " + parameterSetting.getWordsize()
					+ LINEBREAK;
			histStr += "Max match in a query range: "
					+ parameterSetting.getHspRange() + LINEBREAK;
			if (!parameterSetting.getProgramName().equalsIgnoreCase("blastn"))
				histStr += "Matrix: " + parameterSetting.getMatrix()
						+ LINEBREAK;
			histStr += "Match/mismatch Scores: "
					+ parameterSetting.getMatchScores() + LINEBREAK;
			histStr += "Gap Cost: " + parameterSetting.getGapCost() + LINEBREAK;
			if (parameterSetting.getProgramName().equalsIgnoreCase("blastp")
					|| parameterSetting.getProgramName().equalsIgnoreCase(
							"tblastn")) {
				histStr += "Compositional Adjustment: "
						+ parameterSetting.getCompositionalAdjustment()
						+ LINEBREAK;
			}
			histStr += "Low Complexity Filter On: "
					+ parameterSetting.isLowComplexityFilterOn() + LINEBREAK;
			if (parameterSetting.getProgramName().equalsIgnoreCase("blastn")) {
				histStr += "Species-specific repeats Filter On: "
						+ parameterSetting.isHumanRepeatFilterOn() + LINEBREAK;
				histStr += "Species-specific repeats Filter For: "
						+ parameterSetting.getSpeciesRepeat() + LINEBREAK;
			}
			histStr += "Mask Low Case: " + parameterSetting.isMaskLowCase()
					+ LINEBREAK;
			histStr += "Mask Lookup Table: "
					+ parameterSetting.isMaskLookupTable() + LINEBREAK;
			if (parameterSetting.isDiscontiguousOn()
					&& parameterSetting.getProgramName().equalsIgnoreCase(
							"blastn")) {
				histStr += "Template Length: "
						+ parameterSetting.getTemplateLength() + LINEBREAK;
				histStr += "Template Type: "
						+ parameterSetting.getTemplateType() + LINEBREAK;
			}

			histStr += "Number of Sequences: " + activeSequenceDB.size()
					+ LINEBREAK;
			for (CSSequence marker : activeSequenceDB) {
				histStr += "\t" + marker.getLabel() + "\n";
			}
		}
		return histStr;
	}

	public boolean isStartBrowser() {
		return startBrowser;
	}

	public BlastAnalysisPanel getblastAnalysisPanel() {
		return blastAnalysisPanel;
	}

	public boolean isJobFinished() {
		return jobFinished;
	}

	public void setStartBrowser(boolean startBrowser) {
		this.startBrowser = startBrowser;
	}

	public void setJobFinished(boolean jobFinished) {
		this.jobFinished = jobFinished;
	}

}
