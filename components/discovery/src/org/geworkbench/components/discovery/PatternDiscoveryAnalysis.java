package org.geworkbench.components.discovery;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.xml.rpc.ServiceException;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ProteinSequenceAnalysis;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.components.discovery.session.CreateSessionDialog;
import org.geworkbench.components.discovery.session.DiscoverySession;
import org.geworkbench.components.discovery.session.LoginPanelModel;
import org.geworkbench.components.discovery.session.SessionCreationException;
import org.geworkbench.components.discovery.session.SessionOperationException;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.PropertiesMonitor;
import org.geworkbench.util.patterns.CSMatchedSeqPattern;
import org.geworkbench.util.patterns.PatternFetchException;
import org.geworkbench.util.patterns.PatternOperations;

import polgara.soapPD_wsdl.Exhaustive;
import polgara.soapPD_wsdl.Parameters;
import polgara.soapPD_wsdl.ProfileHMM;
import polgara.soapPD_wsdl.SoapPDLocator;
import polgara.soapPD_wsdl.SoapPDPortType;

/**
 * @author Nikhil 
 * @version Pattern Discovery analysis project
 * 
 */
public class PatternDiscoveryAnalysis extends AbstractAnalysis implements
		ProteinSequenceAnalysis {

	private static final long serialVersionUID = -6120204169478402787L;
	private PatternDiscoveryParamPanel patternPanel = null;
	private CSSequenceSet<CSSequence> activeSequenceDB;
	private DSSequenceSet<? extends DSSequence> sequenceDB;
	private final String analysisName = "Pattern Discovery";
	private Parameters parms = null;
	//algorithm names
    public static final String DISCOVER = "discovery";
    public ProgressBar progressBar;

	// login data parameters are stored here
	private LoginPanelModel loginPanelModel = new LoginPanelModel();
	
	public PatternDiscoveryAnalysis() {
		patternPanel = new PatternDiscoveryParamPanel();
		setDefaultPanel(patternPanel);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public AlgorithmExecutionResults execute(Object input) {
		
		if (input == null || !(input instanceof DSSequenceSet)) {
			return new AlgorithmExecutionResults(false, "Invalid input. ", null);
		}
		sequenceDB = (DSSequenceSet) input;
		
		String selectedAlgo = patternPanel.getSelectedAlgorithmName();

		DSPanel<? extends DSGeneMarker> activatedMarkers = null;
		if(sequenceDB!=null) {
			DSAnnotationContextManager manager = CSAnnotationContextManager
					.getInstance();
			DSAnnotationContext context = manager.getCurrentContext(sequenceDB
					.getMarkerList());
			activatedMarkers = context
					.getActiveItems().activeSubset();
		}

		if (activatedMarkers != null && activatedMarkers.size() > 0) {
			activeSequenceDB = (CSSequenceSet) ((CSSequenceSet) sequenceDB)
					.getActiveSequenceSet(activatedMarkers);
		} else {
			activeSequenceDB = (CSSequenceSet) sequenceDB;
		}

		// select the algorithm to run
		boolean exhaustive = false;
		String algorithmName = REGULAR;
		if (selectedAlgo.equalsIgnoreCase(PatternResult.EXHAUSTIVE)) {
			exhaustive = true;
			algorithmName = EXHAUSTIVE;
		}

		if(activeSequenceDB==null) {
			return new AlgorithmExecutionResults(false, "Active Sequence Set is null.", null);
		}
		
		try {
			parms = readParameter(patternPanel, activeSequenceDB.getSequenceNo(), exhaustive);
		} catch (Exception e1) {
			return new AlgorithmExecutionResults(false, "Invalid parameters supplied ", null);
		}
		
		DiscoverySession discoverySession = getSession();
		//Check for the discovery session
		if(discoverySession == null) {
			return new AlgorithmExecutionResults(false, "Unable to load the session.", null);
		}
		progressBar = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
		progressBar.addObserver(this);
		progressBar.setTitle("Pattern Discovery");
		progressBar.start();
		this.stopAlgorithm = false;
		
		final int databaseSize = discoverySession.getSequenceDB().getSequenceNo();
		//uploading sequence to the server
		for (int i = 0; i < databaseSize; ++i) {		
			if(this.stopAlgorithm) {
				try {
					discoverySession.stop();
				} catch (SessionOperationException e) {
					e.printStackTrace();
				}
				return new AlgorithmExecutionResults(false, "Pattern Discovery is canceled at " + new Date(), null);
			}
        	
			try {
        		progressBar.setMessage("Uploading sequence: " + discoverySession.getSequenceDB().getSequence(i).getLabel());
				discoverySession.upload(i);
			} catch (SessionOperationException ex) {
				System.out.println(ex.toString());
	            ex.printStackTrace();
			}
        }
		
		
        //Save sequence and setting parameters
		try {
			progressBar.setMessage("Saving sequences and parameters on the server");
			discoverySession.saveSeqDB();
			discoverySession.setParameters(parms);
			if(this.stopAlgorithm) {
				discoverySession.stop();
				return new AlgorithmExecutionResults(false, "Pattern Discovery is canceled at " + new Date(), null);
			}
			
		} catch (SessionOperationException ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
		}
       
		//start discovery
        try {
        	progressBar.setMessage("Running Pattern Discovery");
			discoverySession.discover(algorithmName);
			if(this.stopAlgorithm) {
				discoverySession.stop();
				return new AlgorithmExecutionResults(false, "Pattern Discovery is canceled at " + new Date(), null);
			}
		} catch (SessionOperationException ex) {
			System.out.println(ex.toString());
            ex.printStackTrace();
		}
        boolean done = false;
        // TODO ideally, PatternResult should be able to handle unfinished result
        while (!done) {
            try {
            	if(this.stopAlgorithm) {
					discoverySession.stop();
					return new AlgorithmExecutionResults(false, "Pattern Discovery is canceled at " + new Date(), null);
				}
            	Thread.sleep(100);
				done = discoverySession.isDone();
				if(this.stopAlgorithm) {
					discoverySession.stop();
					return new AlgorithmExecutionResults(false, "Pattern Discovery is canceled at " + new Date(), null);
				}
			} catch (SessionOperationException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        
        int totalPatternNum = -1;
		PatternResult patternResult = new PatternResult("Pattern Discovery", activeSequenceDB,
				parms.getMinSupport(), parms.getMinTokens(), parms.getMinWTokens(), parms.getWindow());
		try {
			totalPatternNum = discoverySession.getPatternNo();
			if(totalPatternNum == 0) {
				progressBar.stop();
				
				JOptionPane.showMessageDialog(null,
						"No patterns found for input provided ",
						"Pattern Discovery", JOptionPane.WARNING_MESSAGE);
				return new AlgorithmExecutionResults(true, "No patterns found", null);
			}else {
				for (int i = 0; i < totalPatternNum; i++) {
		            CSMatchedSeqPattern patternData = new CSMatchedSeqPattern(discoverySession.getSequenceDB());
		            try {
		            	discoverySession.getPattern(i, patternData);
		            } catch (SessionOperationException ext) {
		                throw new PatternFetchException(ext.getMessage());
		            }
		            PatternOperations.fill(patternData, discoverySession.getSequenceDB());
		            
					patternResult.add(patternData);
				}
			}
		} catch (SessionOperationException e) {
			e.printStackTrace();
		}
		
		patternResult.setDescription("Number of Patterns: " + totalPatternNum);

		String historyStr = generateHistoryStr(activeSequenceDB,
				parms, totalPatternNum);
		HistoryPanel.addToHistory(patternResult, historyStr);
		progressBar.stop();
		return new AlgorithmExecutionResults(true, "Pattern Discovery Done", patternResult);
	}

	private final static String REGULAR = "regular";
    private final static String EXHAUSTIVE = "exhaustive";

	public int getAnalysisType() {
		return AbstractAnalysis.BLAST_TYPE;
	}
	
	/**
	 * @return Analysis name of the component
	 * @see cwb.xml file of the component to change the analysis name 
	 */
	public String getAnalysisName() {
		return this.analysisName;
	}
	
	/**
	 * Reads the parameters in the parameter panel.
	 * 
	 * @return Parameters the parameters from the panel
	 */
	private Parameters readParameter(PatternDiscoveryParamPanel parmsPanel, int seqNo,
			boolean exhaustive) throws Exception {
		Parameters parms = new Parameters();
		try {

			String supportString = parmsPanel.getMinSupport();
			String supportType = parmsPanel.getCurrentSupportMenuStr();
			if (supportType.equalsIgnoreCase(PatternDiscoveryParamPanel.SUPPORT_PERCENT_1_100)) {
				double minSupport = Double.parseDouble(supportString.replace(
						'%', ' ')) / 100.0;
				parms.setMinPer100Support(minSupport);
				parms.setMinSupport((int) (Math.ceil(parms
						.getMinPer100Support()
						* (double) seqNo)));
				parms.setCountSeq(1);
			}
			if (supportType.equalsIgnoreCase(PatternDiscoveryParamPanel.SUPPORT_SEQUENCES)) {
				// parms.setMinPer100Support(0);
				int minSupport = (int) Double.parseDouble(supportString);
				parms.setMinSupport(minSupport);
				// parms.setMinSupport((int)
				// (Math.ceil(parms.getMinPer100Support() * (double) seqNo)));
				parms.setCountSeq(1);

			}
			if (supportType.equalsIgnoreCase(PatternDiscoveryParamPanel.SUPPORT_OCCURRENCES)) {
				// parms.setMinPer100Support(0);
				int minSupport = (int) Double.parseDouble(supportString);
				parms.setMinSupport(minSupport);
				// parms.setMinSupport((int)
				// (Math.ceil(parms.getMinPer100Support() * (double) seqNo)));
				parms.setCountSeq(0);

			}

			parms.setMinTokens(parmsPanel.getMinTokens());
			parms.setWindow(parmsPanel.getWindow());
			parms.setMinWTokens(parmsPanel.getMinWTokens());

			// Parsing the ADVANCED panel
			parms.setExactTokens(2);
			parms.setExact(parmsPanel.getExactOnlySelected());
			parms.setPrintDetails(0);// false by default
			parms.setComputePValue(parmsPanel.getPValueBoxSelected());
			parms.setSimilarityMatrix(parmsPanel.getMatrixSelection());
			parms.setSimilarityThreshold(parmsPanel.getSimilarityThreshold());
			parms.setMinPValue(parmsPanel.getMinPValue());

			// Parsing the GROUPING panel
			// no GUI to set these two, so make sure the default values are set as expected
			parms.setGroupingType(0);
			parms.setGroupingN(1);

			// Parsing the LIMITS panel
			parms.setMaxPatternNo(parmsPanel.getMaxPatternNo());
			parms.setMinPatternNo(parmsPanel.getMinPatternNo());
			parms.setMaxRunTime(parmsPanel.getMaxRunTime());
			parms.setThreadNo(1);
			parms.setThreadId(0);
			parms.setInputName("gp.fa");
			parms.setOutputName("results.txt");

			ProfileHMM hmm = new ProfileHMM();
			hmm.setEntropy(parmsPanel.getProfileEntropy());
			hmm.setWindow(parmsPanel.getWindow());
			parms.setProfile(hmm);

			if (exhaustive) {
				Exhaustive eparams = new Exhaustive();
				String decSupport = parmsPanel.getDecSupportExhaustive();
				final double REDUCTION = 0.95; // 5% default
				double reduction = REDUCTION;
				// If this is a percentage then CountSeq is true by default
				reduction = (1.0 - ((double) Integer.parseInt(decSupport) / 100.0));
				if (reduction <= 0.0 || reduction >= 1.0) {
					reduction = REDUCTION;
				} else {
					eparams.setDecrease(reduction);
				}

				final int SUPPORT = 1; // default
				int minSupport = SUPPORT;

				try {
					minSupport = Integer.parseInt(parmsPanel.getMinSupportExhaustive());
				} catch(NumberFormatException e) {
					throw new Exception("Min. Support must be an integer.");
				}

				// check that the min support is less than the initial support
				if ((minSupport > parms.getMinSupport()) || (minSupport == 0)) {
					minSupport = SUPPORT;
				}

				eparams.setMinSupport(minSupport);

				// ok set the Exhaustive parameters:
				parms.setExhaustive(eparams);

			}

		} catch (NumberFormatException ex) {
			return null;
		}
		return parms;
	}
	
	/*
	 * This method returns history string to display in DataSet History
	 * 
	 */
	
	private String generateHistoryStr(
			CSSequenceSet<CSSequence> activeSequenceDB, Parameters params, int totalPatternNum) {
    	String histStr = "";
    	int seqNo = activeSequenceDB.size();
    	if(params != null) {
    		
    		histStr += "Pattern Discovery run with the following parameters:\n";
			histStr += "----------------------------------------\n";
			
			histStr += "Algorithm Type: " + patternPanel.getSelectedAlgorithmName() + "\n";	
			if (patternPanel.getCurrentSupportMenuStr().equalsIgnoreCase(PatternDiscoveryParamPanel.SUPPORT_PERCENT_1_100)) 
				histStr += patternPanel.getCurrentSupportMenuStr() + ": " + patternPanel.getMinSupport() + "% (" + params.getMinSupport()+ "/" + seqNo + ")\n";
			else
				histStr += patternPanel.getCurrentSupportMenuStr() + ": " + patternPanel.getMinSupport() + "(" + params.getMinSupport() + ")\n";
			histStr += "Minimum Tokens: " + patternPanel.getMinTokens() + "\n";
			histStr += "Density Window: " + patternPanel.getWindow() + "\n";
			histStr += "Density Window Min. Tokens: " + patternPanel.getMinWTokens() + "\n";
			
			if(patternPanel.getSelectedAlgorithmName().equalsIgnoreCase(EXHAUSTIVE)) {
				histStr += "Decrease Support(%): " + patternPanel.getDecSupportExhaustive() + "\n";
				histStr += "Minimum Support(%): " + patternPanel.getMinSupportExhaustive() + "\n";
				histStr += "Minimum Support(%): " + patternPanel.getMinPatternNo() + "\n";
			}
			
			
			
			histStr += "Max. Pattern Number: " + patternPanel.getMaxPatternNo() + "\n";
			
    	
    	    if (patternPanel.getExactOnlySelected() == 1)    	    	
    	    	histStr += "Substitution Matrix: Exact Only is checked.\n";
    	    else
    	    {
    	    	histStr += "Substitution Matrix: Exact Only is not checked.\n";
    	    	histStr += "Similarity Matrix: " + patternPanel.getMatrixSelection() + "\n";
    	    	histStr += "Similarity Threshold: " + patternPanel.getSimilarityThreshold() + "\n";   	    	
    	    	
    	    }    	    
    	    
    	    histStr += "Number of Sequences: " + activeSequenceDB.size() + "\n";
    	    
    	    histStr += "Number of patterns found: " + totalPatternNum  + "\n";
    	    
    	    
    	
    	}
    	
    	return histStr;
	}
	
	/**
	 * The method returns a session. Note: The method will pop up a dialog to
	 * create a session.
	 *
	 * @return the active session.
	 */
	synchronized DiscoverySession getSession() {

		LoginPanelModel tempLoginModel = new LoginPanelModel();
		copyLoginPanelModel(loginPanelModel, tempLoginModel);
		CreateSessionDialog dialog = new CreateSessionDialog(null, "New DiscoverySession", tempLoginModel, true);

        dialog.setVisible(true);
        int ret = dialog.getReturnValue();

        String host = new String();
        int port = -1;
        String userName = new String();
        char[] passWord = null;
        String sessionName = new String();
        if (ret == CreateSessionDialog.CONNECT_OPTION) {
            host = dialog.getHostName();
            port = dialog.getPortNum();
            userName = dialog.getUserName();
            char[] pWord = dialog.getPassWord();
            if (pWord != null) {
                passWord = new char[pWord.length];
                System.arraycopy(pWord, 0, passWord, 0, pWord.length);
            } else {
                passWord = new char[0];
            }
            sessionName = dialog.getSessionName();
        } else {
        	return null;
        }

		// try to create this session
		DiscoverySession aDiscoverySession = null;
		try {
			aDiscoverySession = connectToService(host, port, userName,
					passWord, sessionName);

		} catch (SessionCreationException exp) {
			return null;
		}

		// save the user's choosing to the Properties file and to the model
		saveSessionProperties(host, port, userName);
		copyLoginPanelModel(tempLoginModel, loginPanelModel);

		return aDiscoverySession;
	}
	
	private DiscoverySession connectToService(String host, int port,
			String userName, char[] password, String sessionName)
			throws SessionCreationException {

		// establish a connection
		URL url = null;
		try {
	        String urlString = new String("http://" + host + ":" + port + "/" + "PDServ.exe");
	        url = new URL(urlString);
		} catch (MalformedURLException ex) {
			throw new SessionCreationException("Could not form URL. (host: "
					+ host + "port: " + port + ")");
		}

		SoapPDPortType soapPDPortType = null;
		try {
			soapPDPortType = new SoapPDLocator().getsoapPD(url);
		} catch (ServiceException ex) {
			throw new SessionCreationException(
			"Could not connect to the server.");
		}
		
		int userId = 0;
        try {
            String pass = new String(password);
            int returnVal = soapPDPortType.login(userName, pass);

            if (returnVal > -1) {
                userId = returnVal;
            }
        } catch (RemoteException exp) {
             throw new SessionCreationException("Login operation failed.");
        }

		DSSequenceSet<? extends DSSequence> database = activeSequenceDB;
		// the database will be saved with this name on the server.
		String databaseName = encodeFile(database.getFile(),
				userName);
		// create a session
		return new DiscoverySession(sessionName, database, databaseName,
				soapPDPortType, userName, userId);
	}
	
	private void copyLoginPanelModel(LoginPanelModel from, LoginPanelModel to) {
		to.setHostNames(from.getHostSet(), from.getHostName());
		to.setPort(from.getPort());
		to.setUserName(from.getUserName());
	}
	
	private void saveSessionProperties(String host, int port, String user) {
		org.geworkbench.util.PropertiesMonitor pmMgr = PropertiesMonitor
				.getPropertiesMonitor();
		pmMgr.addHost(host);
		pmMgr.setPort(port);
		pmMgr.setUserName(user);
		pmMgr.setHostSelected(host);
		pmMgr.writeProperties();
	}
	
	/**
     * The method constructs a string from a file and userName.
     */
    private static String encodeFile(File toEncode, String userName) {
        String databaseName = toEncode.getPath();
        databaseName = databaseName.replace(File.separatorChar, '_').replace(':', '[');

        //append user name to get uniqueness
        databaseName = userName + '_' + databaseName;
        return databaseName;
    }
    
}