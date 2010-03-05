package org.geworkbench.components.alignment.client;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.algorithms.BWAbstractAlgorithm;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSAlignmentResultSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.components.alignment.blast.RemoteBlast;
import org.geworkbench.components.alignment.blast.RemoteBlast.NcbiResponseException;
import org.geworkbench.components.alignment.panels.AlgorithmMatcher;
import org.geworkbench.components.alignment.panels.BlastAppComponent;
import org.geworkbench.components.alignment.panels.ParameterSetting;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.BrowserLauncher;
import org.geworkbench.util.FilePathnameUtils;

/**
 * BlastAlgorithm.
 *
 * @author XZ
 * @author zji
 * @version $Id$
 */
public class BlastAlgorithm extends BWAbstractAlgorithm {

	static private Log LOG = LogFactory.getLog(RemoteBlast.class);

	private BlastAppComponent blastAppComponent = null;

	private boolean startBrowser;
	private boolean jobFinished = false;

	private boolean useNCBI = false;
	private ParameterSetting parameterSetting;

	private CSSequenceSet sequenceDB;
	private DSSequenceSet parentSequenceDB;
	
	private final static int TIMEGAP = 4000;
	private final static int SHORTTIMEGAP = 50;
	private final static String LINEBREAK = System.getProperty("line.separator");
	public void setBlastAppComponent(BlastAppComponent _blastAppComponent) {
		blastAppComponent = _blastAppComponent;
	}

	public BlastAlgorithm() {

	}

	/**
	 * Update Progress with finished percentage and related information.
	 *
	 * @param percent
	 *            double
	 * @param text
	 *            String
	 */

	void updateProgressStatus(final double percent, final String text) {
		if (blastAppComponent != null) {
			blastAppComponent.updateProgressBar(percent, text);
		}
	}

	/**
	 * Update progress only with String information.
	 *
	 * @param text
	 *            String
	 */
	void updateStatus(String text) {
		if (blastAppComponent != null) {
			blastAppComponent.updateProgressBar(text);
		}
	}

	/**
	 * Update the component's progressBar with information and reset the
	 * ProgressBar.
	 *
	 * @param boo
	 *            boolean
	 * @param text
	 *            String
	 */

	void updateStatus(boolean boo, String text) {
		if (blastAppComponent != null) {
			blastAppComponent.updateProgressBar(boo, text);
		}
	}

	/**
	 * Get the percentage of completion.
	 *
	 * @return double
	 */
	public double getCompletion() {
		if (jobFinished) {
			// Make it bigger than 1, it means that job is done.
			return 3;
		}
		return super.getCompletion();
	}

	/**
	 * Show error message and update status
	 *
	 * @param e
	 */
	private void processExceptionFromNcbi(Exception e, CSSequence sequence) {
		e.printStackTrace();
		if (blastAppComponent != null) {
			String exceptionName = e.getClass().getName();
			blastAppComponent.reportError("Sequence " + sequence
					+ " cannot be blasted due to " + exceptionName + ":\n"
					+ e.getMessage(), "Error: " + exceptionName);

		}
		updateStatus(false, "NCBI Blast is stopped at " + new Date());
	}

	/**
	 * Execute in the case when NCBI is used.
	 */
	@SuppressWarnings("unchecked") // two lines affected
	private void executeUsingNcbi() {
		String tempFolder = FilePathnameUtils.getTemporaryFilesDirectoryPath();

		/* generate a new file name for the coming output file. */
		String outputFile = tempFolder + "Blast"
				+ RandomNumberGenerator.getID() + ".html";

		RemoteBlast blast;
		CSSequenceSet<CSSequence> activeSequenceDB = sequenceDB;
		DSSequenceSet parentSequenceSet = parentSequenceDB;

		for (CSSequence sequence : activeSequenceDB) {
			updateStatus("Uploading sequence: " + sequence);
			blast = new RemoteBlast(sequence.getSequence(), outputFile);

			blast.setCmdLine(AlgorithmMatcher
					.translateToCommandline(parameterSetting));
			String BLAST_rid = null;
			try {
				BLAST_rid = blast.submitBlast();
			} catch (UnknownHostException e1) {
				processExceptionFromNcbi(e1, sequence);
				return;
			} catch (NcbiResponseException e1) {
				processExceptionFromNcbi(e1, sequence);
				return;
			} catch (IOException e1) {
				processExceptionFromNcbi(e1, sequence);
				return;
			}
			if (BLAST_rid == null) {
				if (blastAppComponent != null) {
					blastAppComponent
							.reportError(
									"Sequence "
											+ sequence
											+ " cannot be blasted, please check your parameters.",
									"Parameter Error");

				}
				updateStatus(false, "NCBI Blast is stopped at "
						+ new Date());
				return;

			}
			updateStatus("Querying sequence: "
					+ sequence.getDescriptions().toString());
			updateStatus("The Request ID is : " + BLAST_rid);

			blast.getBlast(BLAST_rid, "HTML");
			while (!blast.getBlastDone()) {
				try {
					if (blastAppComponent != null
							&& !blastAppComponent.isStopButtonPushed()
							&& !stopRequested) {
						updateStatus("For sequence " + sequence
								+ ",  the blast job is running. ");
						Thread.sleep(TIMEGAP);
					} else {
						return;
					}
				} catch (Exception e) {

				}
				updateStatus("Querying sequence: "
						+ sequence.getDescriptions().toString());

			}
			if (stopRequested) {
				return;
			}

		}
		updateStatus(false, "NCBI Blast is finisheded at " + new Date());
		String outputFilePath = "file://"
				+ new File(outputFile).getAbsolutePath();
		if (parameterSetting.isViewInBrowser()) {
			if ((new File(outputFile)).canRead()) {
				try {
					String osName = System.getProperty("os.name");
					if (osName.startsWith("Mac OS")) {
						BrowserLauncher.openURL(outputFilePath);
					} else {
						BrowserLauncher.openURL(new File(outputFile)
								.getAbsolutePath());
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
						"File cannot be read", JOptionPane.ERROR_MESSAGE);

			}

		}
		CSAlignmentResultSet blastResult = new CSAlignmentResultSet(outputFile, activeSequenceDB
				.getFASTAFileName(), activeSequenceDB, parentSequenceSet);
		blastResult.setLabel(BlastAppComponent.NCBILABEL);
		ProjectNodeAddedEvent event = new ProjectNodeAddedEvent(null, null,
				blastResult);
		String historyStr = generateHistoryStr(activeSequenceDB);
		ProjectPanel.addToHistory(blastResult, historyStr);

		if (blastAppComponent != null) {
			blastAppComponent.publishProjectNodeAddedEvent(event);
		}
	}

	private String generateHistoryStr(CSSequenceSet<CSSequence> activeSequenceDB) {
		String histStr = "";
		if(parameterSetting!=null){

			// Header
			histStr += "Blast run with the following parameters:\n";
			histStr += "----------------------------------------\n\n";


				histStr += "Database: " + parameterSetting.getDbName() + LINEBREAK;
				histStr += "BLAST Program: " + parameterSetting.getProgramName() + LINEBREAK;
				histStr += "Expect: " + parameterSetting.getExpect() + LINEBREAK;
				histStr += "Matrix: " + parameterSetting.getMatrix() + LINEBREAK;
				histStr += "Gap Cost: " + parameterSetting.getGapCost() + LINEBREAK;
				histStr += "Word Size: " + parameterSetting.getWordsize() + LINEBREAK;
				histStr += "Low Complexity Filter On: " + parameterSetting.isLowComplexityFilterOn() + LINEBREAK;
				histStr += "Human Repeat Filter On: " + parameterSetting.isHumanRepeatFilterOn() + LINEBREAK;
				histStr += "Mask Low Case: " + parameterSetting.isMaskLowCase() + LINEBREAK;
				histStr += "Mask Lookup Table: " + parameterSetting.isMaskLookupTable() + LINEBREAK + LINEBREAK;

				histStr += "Number of Sequences: " + activeSequenceDB.size() + LINEBREAK;
				for (CSSequence marker : activeSequenceDB) {
					histStr += "\t" + marker.getLabel() + "\n";
				}
		}
		// TODO Auto-generated method stub
		return histStr;
	}

	/**
	 * The workhorse to run Blast program.
	 *
	 * This method is only invoked by construct() defined in BWAbstractAlgorithm.
	 */
	protected void execute() {
		if (sequenceDB == null || parentSequenceDB == null) {
			try {
				Thread.sleep(SHORTTIMEGAP);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (useNCBI) {
			executeUsingNcbi();
		} else {
			LOG.error("useNCBI is never expected to be false");
		}
	}

	public boolean isStartBrowser() {
		return startBrowser;
	}

	public BlastAppComponent getBlastAppComponent() {
		return blastAppComponent;
	}

	public boolean isUseNCBI() {
		return useNCBI;
	}

	public ParameterSetting getParameterSetting() {
		return parameterSetting;
	}

	public boolean isJobFinished() {
		return jobFinished;
	}

	public void setStartBrowser(boolean startBrowser) {
		this.startBrowser = startBrowser;
	}

	public void setUseNCBI(boolean useNCBI) {
		this.useNCBI = useNCBI;
	}

	public void setParameterSetting(ParameterSetting parameterSetting) {
		this.parameterSetting = parameterSetting;
	}

	public void setJobFinished(boolean jobFinished) {
		this.jobFinished = jobFinished;
	}

	public void setSequenceDB(CSSequenceSet sequenceDB) {
		this.sequenceDB = sequenceDB;
		
	}

	public void setParentSequenceDB(DSSequenceSet parentSequenceDB) {
		this.parentSequenceDB = parentSequenceDB;
		
	}

}
