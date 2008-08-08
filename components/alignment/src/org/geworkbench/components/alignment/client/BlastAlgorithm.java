package org.geworkbench.components.alignment.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.algorithms.BWAbstractAlgorithm;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
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
import org.geworkbench.util.session.SoapClient;
import org.globus.progtutorial.clients.BlastService.Client;

/**
 * BlastAlgorithm.
 * 
 * @author XZ
 * @author zji
 * @version $Id: BlastAlgorithm.java,v 1.30 2008-08-08 18:14:28 xiaoqing Exp $
 */
public class BlastAlgorithm extends BWAbstractAlgorithm implements SoapClientIn {
	static Log LOG = LogFactory.getLog(RemoteBlast.class);

	/**
	 * BlastAlgorithm
	 * 
	 * @param aBoolean
	 *            boolean
	 */
	public BlastAlgorithm(boolean aBoolean, String inputFile, Client _client) {
		gridEnabled = aBoolean;
		client = _client;
		inputFilename = inputFile;

	}

	private Client client;
	private BlastAppComponent blastAppComponent = null;
	private SoapClient soapClient = null;
	private boolean startBrowser;
	private boolean gridEnabled = false;
	private boolean jobFinished = false;
	private String inputFilename;
	private static final String TEMPURLFOLDER = "http://adparacel.cu-genome.org/examples/output/";
	// "http://amdec-bioinfo.cu-genome.org/html/temp/";
	private boolean useNCBI = false;
	private ParameterSetting parameterSetting;
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
	 * Execute in the case when NCBI is not used.
	 */
	@SuppressWarnings("unchecked") // one line affected
	private void executeNotUsingNcbi() {
		if (soapClient != null) {
			String cmd = soapClient.getCmd();
			String textFile = "";
			String htmlFile = null;

			try {
				if (cmd.startsWith("pb")) {

					if (!soapClient.startRun(true)) {
						// fail to connect or other problem.
						blastAppComponent.reportError(BlastAppComponent.ERROR2,
								"Server unreachable");
						blastAppComponent
								.setBlastDisplayPanel(BlastAppComponent.SERVER);
						blastAppComponent
								.blastFinished(BlastAppComponent.ERROR1);
						return;
					}
					htmlFile = ((SoapClient) soapClient).getOutputfile();
					if (stopRequested) {
						return;
					}
					if (startBrowser && !stopRequested) {
						if ((new File(htmlFile)).canRead()) {
							BrowserLauncher.openURL(TEMPURLFOLDER
									+ getFileName(htmlFile));
						} else {
							LOG.warn("CANNOT READ " + htmlFile);
						}
					}

				} else {
					soapClient.startRun();
					textFile = ((SoapClient) soapClient).getOutputfile();
				}
			} catch (Exception exce) {
				blastAppComponent.reportError(BlastAppComponent.ERROR2,
						"Server unreachable");

			}
			if (blastAppComponent != null) {
				blastAppComponent.blastFinished(cmd);
			}

			DSAncillaryDataSet blastResult = null;
			if (htmlFile != null) {
				if (soapClient.getSequenceDB() != null
						&& soapClient.getSequenceDB().getFASTAFileName() != null) {
					blastResult = new CSAlignmentResultSet(htmlFile, soapClient
							.getSequenceDB().getFASTAFileName(), soapClient
							.getSequenceDB());
				}
			} else if (cmd.startsWith("btk search")) {

				blastResult = new SWDataSet(textFile, soapClient
						.getInputFileName(), blastAppComponent.getFastaFile());
			} else if (cmd.startsWith("btk hmm")) {

				blastResult = new HMMDataSet(textFile, soapClient
						.getInputFileName(), blastAppComponent.getFastaFile());
			}
			ProjectNodeAddedEvent event = new ProjectNodeAddedEvent(null, null,
					blastResult);
			if (blastAppComponent != null) {
				blastAppComponent.publishProjectNodeAddedEvent(event);
			} else {
				blastAppComponent.publishProjectNodeAddedEvent(event);
			}
		}
		// Handle grid situation.
		if (gridEnabled) {
			String tempFolder = System.getProperties().getProperty(
					"temporary.files.directory");
			if (tempFolder == null) {
				tempFolder = ".";
			}

			CSAlignmentResultSet blastResult = new CSAlignmentResultSet(tempFolder + "a.html",
					inputFilename, soapClient.getSequenceDB());
			org.geworkbench.events.ProjectNodeAddedEvent event = new org.geworkbench.events.ProjectNodeAddedEvent(
					"message", null, blastResult);
			blastAppComponent.publishProjectNodeAddedEvent(event);
			String output = client.submitRequest(inputFilename);
			URL url = null;
			try {
				url = new URL(output);
				String filename = "C:\\" + url.getFile();
				blastResult.setResultFile(filename);
				jobFinished = true;
				BrowserLauncher.openURL(output);
				PrintWriter bw = new PrintWriter(new FileOutputStream(filename));
				URLConnection urlCon = url.openConnection();

				String line = "";
				BufferedReader br = new BufferedReader(new InputStreamReader(
						urlCon.getInputStream()));
				while ((line = br.readLine()) != null) {
					bw.println(line);
				}
				br.close();
				bw.close();
				blastResult = new CSAlignmentResultSet(filename, inputFilename,
						soapClient.getSequenceDB());
			} catch (MalformedURLException e) {
				LOG
						.warn("MalformedURLException in the grid-enabled part of execute() in BlastAlgorithm: "
								+ e.getMessage());
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				LOG
						.warn("FileNotFoundException in the grid-enabled part of execute() in BlastAlgorithm: "
								+ e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				LOG
						.warn("IOException in the grid-enabled part of execute() in BlastAlgorithm: "
								+ e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Execute in the case when NCBI is used.
	 */
	@SuppressWarnings("unchecked") // two lines affected
	private void executeUsingNcbi() {
		String tempFolder = System.getProperties().getProperty(
				"temporary.files.directory");
		if (tempFolder == null) {
			tempFolder = ".";

		}
		/* generate a new file name for the coming output file. */
		String outputFile = tempFolder + "Blast"
				+ RandomNumberGenerator.getID() + ".html";

		RemoteBlast blast;
		CSSequenceSet<CSSequence> activeSequenceDB = soapClient
				.getSequenceDB();
		DSSequenceSet parentSequenceSet = soapClient.getParentSequenceDB();

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
		if (soapClient == null) {
			try {
				Thread.sleep(SHORTTIMEGAP);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if (useNCBI) {
			executeUsingNcbi();
		} else {
			executeNotUsingNcbi();
		}
	}

	public void setSoapClient(SoapClient client) {
		soapClient = client;
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

	private String getFileName(String path) {
		StringTokenizer st = new StringTokenizer(path, "/");
		String s = null;
		if (st.countTokens() <= 1) {
			st = new StringTokenizer(path, "\\");
			if (st.countTokens() <= 1) {
				return path;
			}

			while (st.hasMoreTokens())
				s = st.nextToken();

			return s;
		}

		while (st.hasMoreTokens())
			s = st.nextToken();

		return s;
	}
}
