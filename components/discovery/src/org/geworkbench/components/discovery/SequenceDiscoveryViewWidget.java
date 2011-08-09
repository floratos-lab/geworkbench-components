package org.geworkbench.components.discovery;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.PatternDiscoveryParameters;
import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.components.discovery.algorithm.AlgorithmStub;
import org.geworkbench.components.discovery.algorithm.RegularDiscoveryFileLoader;
import org.geworkbench.components.discovery.algorithm.ServerBaseDiscovery;
import org.geworkbench.components.discovery.session.DiscoverySession;
import org.geworkbench.events.HistoryEvent;
import org.geworkbench.events.ProgressBarEvent;
import org.geworkbench.events.ProgressChangeEvent;
import org.geworkbench.events.StatusBarEvent;
import org.geworkbench.events.listeners.ProgressChangeListener;
import org.geworkbench.events.listeners.StatusChangeListener;

import polgara.soapPD_wsdl.Exhaustive;
import polgara.soapPD_wsdl.Parameters;
import polgara.soapPD_wsdl.ProfileHMM;

/**
 * <p>
 * This class services the needs of different algorithms. It enables them to use
 * a "GUI template" for displaying the progress/status of a computation and for
 * displaying the transformation of their input data. Each algorithm instance is
 * associated with a "view" (Note: there is a one to many mapping between
 * algorithms and a particular view). A view is a graphical representation of the
 * Algorithm result set. For an algorithm to display its results, a DataSource
 * is attached to a model of the corresponding view. The Data source contains the
 * algorithm's result. Communications: viewer - model: defined by these
 * components. viewer - SequenceDiscoveryViewWidget: through property changes
 * model - Algorithm's DataSource: as defined by the model interface
 * SequenceDiscoveryViewWidgetAppComponent- SequenceDiscoveryViewWidget: through
 * property changes.
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: Califano Lab
 * </p>
 *
 * @author
 * @version $Id$
 */
public class SequenceDiscoveryViewWidget extends JPanel implements
		StatusChangeListener, PropertyChangeListener, ProgressChangeListener {

	private static final long serialVersionUID = -7663914616670165388L;
	private static Log log = LogFactory.getLog(SequenceDiscoveryViewWidget.class);

	// holds the id of the current selected project file.
	// this id is the key for mapping to an AlgorithmStub.
	private String currentStubId = "";

	// Hold the reference to current PD result.
	private File currentResultFile; // FIXME let's assume this is always null 

	// Contains all the algorithm Stubs - they are mapped by the selected file.
	private Map<String, AlgorithmStub> algorithmStubMap = new HashMap<String, AlgorithmStub>();

    private JRadioButton discovery = new JRadioButton("Normal");
    private JRadioButton exhaustive = new JRadioButton("Exhaustive");
    
	// property changes
	public static final String TABLE_EVENT = "tableEvent";

	// the displayed view component in this widget
	private Component currentViewComponent = new JPanel();

	// view and model
	private JPanel view;
	private PatternTableModelWrapper model;

	private Parameters parms = null;
	private JPanel sequenceViewPanel = new JPanel();
	private JToolBar jToolBar1 = new JToolBar();

	private DSSequenceSet<? extends DSSequence> sequenceDB = new CSSequenceSet<CSSequence>();
	private SequenceDiscoveryViewAppComponent appComponent = null;

	private JPanel panelView = new JPanel();
	private JProgressBar progressBar = new JProgressBar(0, 100);

	private JLabel jPatternLabel = new JLabel();
	private ParameterPanel parameterPanel = new ParameterPanel();

	private String currentNodeID = "";

	private JButton executeButton = new JButton();
	private JButton stopButton = new JButton();
	private JButton loadBttn = new JButton();

	public SequenceDiscoveryViewWidget() throws Exception {
		// initialized the views and models
		// pattern table model
		model = new PatternTableModelWrapper();
		// pattern table view
		view = new PatternTableView(model, this);
		view.addPropertyChangeListener(this);

		try {
			jbInit();
		} catch (Exception ex) {
			log.error("SequenceDiscoveryViewWidget:::constructor: "
					+ ex.toString());
			throw ex;
		}
	}

	private void jbInit() throws Exception {
		ImageIcon startButtonIcon = new ImageIcon(this.getClass().getResource(
				"start.gif"));
		ImageIcon stopButtonIcon = new ImageIcon(this.getClass().getResource(
				"stop.gif"));
		ImageIcon loadtButtonIcon = new ImageIcon(this.getClass().getResource(
				"load.gif"));
		this.setLayout(new BorderLayout());

		sequenceViewPanel.setLayout(new BorderLayout());

		// progress bar init
		progressBar.setOrientation(JProgressBar.HORIZONTAL);
		progressBar.setBorder(BorderFactory.createEtchedBorder());
		progressBar.setStringPainted(true);
		// View panel - different views plug into this panel
		panelView.setLayout(new BorderLayout());

		jPatternLabel.setBorder(BorderFactory.createEtchedBorder());
		jPatternLabel.setText("...");

		this.setMinimumSize(new Dimension(542, 243));
		this.setPreferredSize(new Dimension(553, 636));
		executeButton.setMaximumSize(new Dimension(26, 26));
		executeButton.setMinimumSize(new Dimension(26, 26));
		executeButton.setPreferredSize(new Dimension(26, 26));
		executeButton.setToolTipText("Start Pattern Discovery");
		executeButton.setIcon(startButtonIcon);
		executeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executButton_actionPerformed(e);
			}
		});
		stopButton.setMaximumSize(new Dimension(26, 26));
		stopButton.setMinimumSize(new Dimension(26, 26));
		stopButton.setPreferredSize(new Dimension(26, 26));
		stopButton.setToolTipText("Stop Pattern Discovery");
		stopButton.setIcon(stopButtonIcon);
		stopButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopButton_actionPerformed(e);
			}
		});
		loadBttn.setMaximumSize(new Dimension(26, 26));
		loadBttn.setMinimumSize(new Dimension(26, 26));
		loadBttn.setPreferredSize(new Dimension(26, 26));
		loadBttn.setToolTipText("Load a pattern file");
		loadBttn.setIcon(loadtButtonIcon);
		loadBttn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadBttn_actionPerformed(e);
			}
		});

		discovery.setSelected(true);

		ButtonGroup algorithmGroup = new ButtonGroup();
        algorithmGroup.add(discovery);
        algorithmGroup.add(exhaustive);

        JPanel algoPanel = new JPanel();
        algoPanel.add(discovery);
        algoPanel.add(exhaustive);
		
		algoPanel.setMaximumSize(new Dimension(160, 20));
		algoPanel.setMinimumSize(new Dimension(160, 20));
		algoPanel.setPreferredSize(new Dimension(160, 32));
		sequenceViewPanel.add(panelView, BorderLayout.CENTER);
		panelView.add(jPatternLabel, BorderLayout.SOUTH);
		panelView.add(currentViewComponent, BorderLayout.WEST);
		sequenceViewPanel.add(parameterPanel, BorderLayout.SOUTH);
		sequenceViewPanel.add(algoPanel, java.awt.BorderLayout.NORTH);

		this.add(sequenceViewPanel);

		jToolBar1.setLayout(new BoxLayout(jToolBar1, BoxLayout.LINE_AXIS));
		jToolBar1.add(executeButton);
		jToolBar1.add(stopButton);
		jToolBar1.add(loadBttn);

		jToolBar1.add(progressBar);
		panelView.add(jToolBar1, java.awt.BorderLayout.NORTH);
	}

	/**
	 * The method updates the progress bar. It is called by the different
	 * algorithms.
	 *
	 * @param evt
	 *            the event for updating the progress bar.
	 * @see org.geworkbench.events.ProgressBarEvent.
	 */
	@Override
	public void progressBarChanged(org.geworkbench.events.ProgressBarEvent evt) {
		int min = evt.getMin();
		int max = evt.getMax();
		String message = evt.getMessage();
		Color c = evt.getColor();
		progressBar.setMinimum(min);
		progressBar.setMaximum(max);
		int percent = evt.getPercentDone();
		progressBar.setValue(percent);
		progressBar.setForeground(c);
		progressBar.setString(message);
	}

	/**
	 * The method updates the Status bar. It is called by the different
	 * algorithms.
	 *
	 * @param evt
	 *            the event for updating the status bar.
	 * @see StatusBarEvent.
	 */
	@Override
	public void statusBarChanged(StatusBarEvent evt) {
		String message = evt.getSatus();
		jPatternLabel.setText(message);
	}

	/**
	 * The dispatch of the different algorithms starts here.
	 *
	 * @param e
	 *            ActionEvent
	 */
	private void executButton_actionPerformed(ActionEvent e) {
		appComponent.updateDataSetView();

		// get a discoverySession for running the algo
		DiscoverySession discoverySession = appComponent.getSession();
		// we cannot run this algorithm with no discoverySession
		if ( discoverySession == null ) {
			log.debug("Warning: registerSession failed" + "[subId=" + currentStubId
					+ " session=" + discoverySession + "]");
			return;
		}
		
		firePropertyChange(TABLE_EVENT, null, null);

		String selectedAlgo = getSelectedAlgorithmName();

		// select the algorithm to run
		boolean exhaustive = false;
		String algorithmName = REGULAR;
		if (selectedAlgo.equalsIgnoreCase(PatternResult.EXHAUSTIVE)) {
			exhaustive = true;
			algorithmName = EXHAUSTIVE;
		}

		try {
			parms = readParameter(parameterPanel, getSequenceDB().getSequenceNo(),
					exhaustive);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage(), "Invalid Parameter",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// fire a parameter change to the application
		PatternDiscoveryParameters pp = ParameterTranslation.translate(parms);

		PatternResult patternResult = new PatternResult(pp,
				"Pattern Discovery", getSequenceDB());
		String id = patternResult.getID();
		currentStubId = currentNodeID + id;
		resultData = patternResult;

		final ServerBaseDiscovery algorithm = new ServerBaseDiscovery(
				discoverySession, parms, algorithmName, this, patternResult);

		algorithm.addProgressChangeListener(this);
		AlgorithmStub stub = getStub(currentStubId, algorithm);
		
		stub.setParameterPanel(parameterPanel);
		stub.setDescription(selectedAlgo);

		algorithmStubMap.put(currentStubId, stub);

		algorithm.addStatusChangeListener(this);

		stub.gainedFocus(model);
		model.attach(stub.getResultDataSource());

		// replace the view and model
		clearTableView();

		// start the algorithm
        executeButton.setEnabled(false);
        Thread thread = new Thread(
        	new Runnable() {
        		public void run() {
        			algorithm.start();
        			executeButton.setEnabled(true);
        		}
        	}, 
        	"Algorithm thread Start");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
	}

	private final static String REGULAR = "regular";
    private final static String EXHAUSTIVE = "exhaustive";

	private String getSelectedAlgorithmName() {
		if(discovery.isSelected()) {
			return PatternResult.DISCOVER;
		} else if(exhaustive.isSelected()) {
			return PatternResult.EXHAUSTIVE;
		} else {
			log.error("Unexpected choice");
			return null;
		}
	}

	// invoked from SequenceDiscoveryViewAppComponent.receive(ProjectEvent, Object)
	void setMinSupportTypeName(String currentMinSupportTypeName) {
		if (currentMinSupportTypeName != null)
			parameterPanel.setCurrentSupportMenuStr(currentMinSupportTypeName);
	};

	private DSAncillaryDataSet<? extends DSBioObject> resultData = null;

	public void setResultData(DSAncillaryDataSet<? extends DSBioObject> resultData) {
		this.resultData = resultData;
	}

	// only in EDT
	public void firePropertyChangeAlgo(){
		appComponent.createNewNode(resultData);
		firePropertyChange(TABLE_EVENT, null, null);
		setTableView();
	}

	/**
	 * Reads the parameters in the parameter panel.
	 * 
	 * @return Parameters the parameters from the panel
	 */
	private Parameters readParameter(ParameterPanel parmsPanel, int seqNo,
			boolean exhaustive) throws Exception {
		Parameters parms = new Parameters();
		try {

			String supportString = parmsPanel.getMinSupport();
			String supportType = parmsPanel.getCurrentSupportMenuStr();
			if (supportType.equalsIgnoreCase(ParameterPanel.SUPPORT_PERCENT_1_100)) {
				double minSupport = Double.parseDouble(supportString.replace(
						'%', ' ')) / 100.0;
				parms.setMinPer100Support(minSupport);
				parms.setMinSupport((int) (Math.ceil(parms
						.getMinPer100Support()
						* (double) seqNo)));
				parms.setCountSeq(1);
			}
			if (supportType.equalsIgnoreCase(ParameterPanel.SUPPORT_SEQUENCES)) {
				// parms.setMinPer100Support(0);
				int minSupport = (int) Double.parseDouble(supportString);
				parms.setMinSupport(minSupport);
				// parms.setMinSupport((int)
				// (Math.ceil(parms.getMinPer100Support() * (double) seqNo)));
				parms.setCountSeq(1);

			}
			if (supportType.equalsIgnoreCase(ParameterPanel.SUPPORT_OCCURRENCES)) {
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
	
	/**
	 * The views communicate with this widget through property changes.
	 *
	 * @param evt
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String property = evt.getPropertyName();
		if (property.equalsIgnoreCase(PatternTableView.ROWSELECTION)) {
			firePropertyChange(TABLE_EVENT, null, evt.getNewValue());
		}
	}

	/**
	 * The method changes the algorithm stub and the view in the widget. It is
	 * called when a file is selected in the project.
	 *
	 * @param stub
	 *            the id of the selected file.
	 */
	// only in EDT
	private void projectFileChanged(String stub) {
		if (stub == null) {
			return;
		}

		if (currentStubId!=null && currentStubId.equals(stub)) {
			return; // same stub no need to update
		}

		// get a stubs.
		String oldStubId = currentStubId;
		currentStubId = stub;

		AlgorithmStub oldStub = algorithmStubMap
				.get(oldStubId);
		AlgorithmStub newStub = algorithmStubMap
				.get(currentStubId);

		if (currentResultFile != null) {
			loadPatternFile(currentResultFile, false);
		}
		if (oldStub == null && newStub == null) {
			// no algorithm stub is mapped
			return;
		}

		if (oldStub != null) {
			oldStub.lostFocus(model);
			oldStub.removeStatusChangeListener(this);
		}

		if (newStub != null) {
			newStub.addStatusChangeListener(this);
			attachDataSource(newStub.getResultDataSource(), newStub);
			// update the algorithms selection panel to reflect the running
			// algo.
			setSelectedAlgorithm(newStub.getDescription());
		} else {
			// set Default View
			clearTableView();
			ParameterPanel paramP = new ParameterPanel();

			//bug 2425
			paramP.setMaxSeqNumber(getSequenceDB().size());
			setCurrentParameterPanel(paramP);
		}
	}
	
	private void setSelectedAlgorithm(String description) {
        if (description.equalsIgnoreCase(PatternResult.DISCOVER)) {
            discovery.setSelected(true);
        } else if (description.equalsIgnoreCase(PatternResult.EXHAUSTIVE)) {
            exhaustive.setSelected(true);
        }
	}

	// only in EDT
	private void loadPatternResult(PatternResult result) {

		currentStubId = null;

		// replace the view and model
		setTableView();

		String idString = RandomNumberGenerator.getID();
		result.setID(idString);

		PatternDataSource PatternSource = new PatternDataSource(result);
		model.attach(PatternSource);

		int patternNumber = result.getPatternNo();
		progressChanged(new ProgressChangeEvent(true, patternNumber));

		// fire a clear table event
		firePropertyChange(TABLE_EVENT, null, null);
	}

	// replace the original setCurrentView for the 'DEFAULT_VIEW' or empty case
	// make it public so ServerBaseDiscovery can call. TODO move to one package
	public void clearTableView() {
		statusBarChanged(DefaultLook.statusEvt);
		progressBarChanged(DefaultLook.progressEvt);

		panelView.remove(currentViewComponent);
		currentViewComponent = DefaultLook.panel;
		panelView.add(currentViewComponent, BorderLayout.CENTER);
		revalidate();
		repaint();
	}
	
	// replace the original setCurrentView for the 'PATTERN_TABLE' or not-empty case
	void setTableView() {
		panelView.remove(currentViewComponent);
		currentViewComponent = view;
		panelView.add(currentViewComponent, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	/**
	 * This method tries to attach a DataSource to one of the models.
	 */
	// only in EDT
	private void attachDataSource(
			org.geworkbench.util.patterns.DataSource source, AlgorithmStub stub) {
		// only one model now
		if (model.attach(source)) {
			// pass the model to the algorithm stub
			stub.gainedFocus(model);
			// update the view panel
			setTableView();
			setCurrentParameterPanel(stub.getParameterPanel());
		}
	}

	/**
	 * Sets a new parameter panel into view.
	 *
	 * @param panel
	 */
	private void setCurrentParameterPanel(JPanel panel) {
		sequenceViewPanel.remove(parameterPanel);
		parameterPanel = (ParameterPanel) panel;
		sequenceViewPanel.add(parameterPanel, BorderLayout.SOUTH);
		revalidate();
		repaint();
	}

	private AlgorithmStub getStub(String key, ServerBaseDiscovery algorithm) {
		AlgorithmStub stub = algorithmStubMap.get(key);
		// if stub is null we need to create stub for this algorithm
		if (stub == null) {
			stub = new AlgorithmStub(algorithm);
		} else {
			// we had a stub.
			// stop the previous algorithm
			stub.stop();
			// remove the model from the stopped algorithm
			stub.lostFocus(model);
			stub.removeStatusChangeListener(this);
			
			stub.setAlgorithm(algorithm);
		}
		return stub;
	}

	private void stopButton_actionPerformed(ActionEvent e) {
		AlgorithmStub stub = algorithmStubMap.get(currentStubId);
		if (stub != null) {
			stub.stop();
		}
	}

	// invoked by SequenceDiscoveryViewAppComponent.receive(ProjectEvent, Object)
	synchronized void setSequenceDB(DSSequenceSet<? extends DSSequence> sDB,
			boolean withExistedPatternNode, String patternNodeID, Parameters p, PatternResult result) {
		// reset the currentResultFile.
		//currentResultFile = null; // FIXME I expect this is always null
		parameterPanel.setMaxSeqNumber(sDB.size());
		if (result != null) {
			currentResultFile = result.getFile();
			loadPatternResult(result);
			return;
		}
		 if (sequenceDB.getID() != sDB.getID()) {
			sequenceDB = sDB;

			String stubID = sDB.getID() + sDB.getDataSetName();
			// Point currentNodeID to the name associated with the sequenceDB
			// name no matter the node is subnode or node.
			currentNodeID = stubID;
			if (withExistedPatternNode && patternNodeID != null) {
				stubID += patternNodeID;
			}
			 	// change the stub for the widget
			projectFileChanged(stubID);
		} else {

			String stubID = sDB.getID() + sDB.getDataSetName();
			// Point currentNodeID to the name associated with the sequenceDB
			// name no matter the node is subnode or node.
			currentNodeID = stubID;
			if (withExistedPatternNode && patternNodeID != null) {
				stubID += patternNodeID;
			}
			// change the stub for the widget
			projectFileChanged(stubID);
			if (p != null) {
				parameterPanel.setParameters(p);
			}
		}
	}

	// invoked by SequnceDiscoveryViewAppComponent.updateDataSetView()
	synchronized void setSequenceDB(DSSequenceSet<? extends DSSequence> sDB) {
		//if(sequenceDB!=null)
		 sequenceDB = sDB;
	}

	public synchronized DSSequenceSet<? extends DSSequence> getSequenceDB() {
		 return sequenceDB;
	}

	// invoked in SequnceDiscoveryViewAppComponent constructor
	void setSequenceDiscoveryViewAppComponent(
			SequenceDiscoveryViewAppComponent s) {
		appComponent = s;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
	}

	// only in EDT
	private void loadPatternFile(File patternfile, boolean newNode) {
		File sequenceFile = getSequenceDB().getFile();
		if (!patternfile.getName().endsWith(".pat")) {
			String msg = "Not a valid file! File extension must end with .pat";

			JOptionPane.showMessageDialog(null, msg, "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (!patternfile.exists()) {
			String msg = "Not a valid file! File " + patternfile.getName()
					+ " does not exist";

			JOptionPane.showMessageDialog(null, msg, "Error",
					JOptionPane.ERROR_MESSAGE);
			return;

		}

		String type = "";
		try {
			BufferedReader bf = new BufferedReader(new FileReader(patternfile));
			type = bf.readLine();
			bf.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (type != null
				&& type.equalsIgnoreCase(PatternResult.DISCOVER)) {
			RegularDiscoveryFileLoader loader = new RegularDiscoveryFileLoader();

			currentStubId = null;

			loader.addProgressChangeListener(this);
			
			loader.addStatusChangeListener(this);

			loader.addProgressChangeListener(model);
			model.attach(loader.getPatternSource());

			// replace the view and model
			setTableView();

			// start reading the file without creating new thread
			@SuppressWarnings("unchecked")
			PatternResult patternDB = loader.read(sequenceFile, patternfile, (DSDataSet<DSSequence>) getSequenceDB());
            if(newNode) {
            	if(patternDB!=null) {
            		appComponent.createNewNode(patternDB);
            	} else {
                    JOptionPane.showMessageDialog(null, "The file " + patternfile + " could not be loaded.\n " + "Please make sure that the sequence file" + " is loaded and selected in the project.");
            	}
            } else { // the case newNode==false may not be necessary at all 
            	log.debug("read file "+patternfile+" but do nothing further");
            }

			// fire a clear table event
			firePropertyChange(TABLE_EVENT, null, null);
		} else {
			log.error("Loading failed. Did not recognize the data.");
			return;
		}
	}

	private void loadBttn_actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser(
				org.geworkbench.util.PropertiesMonitor.getPropertiesMonitor()
						.getDefPath());

		PatFilter filter = new PatFilter();
		chooser.setFileFilter(filter);

		int returnVal = chooser.showOpenDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File patternfile = chooser.getSelectedFile();
		loadPatternFile(patternfile, true);
	}

	/**
	 * Handle the data history.
	 */
	@Override
	public void progressChanged(ProgressChangeEvent evt) {
		if (evt == null || evt.isInitial() ) {
			return;
		}
		int patternFound = evt.getPatternFound();
		String selectedAlgo = getSelectedAlgorithmName();
		ProjectPanel.addToHistory(resultData, "Pattern Discovery"
				+"\nAlgorithm type: "+selectedAlgo
				+"\nParameters: \n"+parametersText()
				+"\nNumber of patterns found: "+patternFound
				);

		// trigger the update of history panel
		appComponent.publishHistoryEvent(new HistoryEvent(resultData));
	}

	private String parametersText() {
		StringBuffer sb = new StringBuffer();
		sb.append("   Compute P-value: " + parms.getComputePValue());
		sb.append("\n   Count of sequences: " + parms.getCountSeq());
		sb.append("\n   Grouping N: " + parms.getGroupingN());
		sb.append("\n   Grouping type: " + parms.getGroupingType());
		sb.append("\n   Input name: " + parms.getInputName());
		sb.append("\n   Max. pattern number: " + parms.getMaxPatternNo());
		sb.append("\n   Max. run time: " + parms.getMaxRunTime());
		sb.append("\n   Min. pattern number: " + parms.getMinPatternNo());
		sb.append("\n   Min. per 100 support: " + parms.getMinPer100Support());
		sb.append("\n   Min. P-value: " + parms.getMinPValue());
		sb.append("\n   Min. support: " + parms.getMinSupport());
		sb.append("\n   Min. tokens: " + parms.getMinTokens());
		sb.append("\n   Density window: " + parms.getWindow());
		sb.append("\n   Density window min. tokens: " + parms.getMinWTokens());
		sb.append("\n   Output mode: " + parms.getOutputMode());
		sb.append("\n   Print details: " + parms.getPrintDetails());
		
		sb.append("\n   Exact: " + parms.getExact());
		sb.append("\n   Exact tokens: " + parms.getExactTokens());
		if (parms.getExact() == 0) {
			sb.append("\n   Similarity matrix: " + parms.getSimilarityMatrix());
			sb.append("\n   Similarity threshold: "
					+ parms.getSimilarityThreshold());
		}


		Exhaustive e = parms.getExhaustive();
		if (e != null) {
			int m = e.getMinSupport();
			int d = (int) ((1 - e.getDecrease()) * 100); // convert back to the
															// way the user
															// entered.
			sb.append("\n   Exhaustive - minimum support: " + m);
			sb.append("\n   Exhaustive - decrease support: " + d);
		}
		
		sb.append("\n   Sort mode: " + parms.getSortMode());
		sb.append("\n   Thread ID: " + parms.getThreadId());
		sb.append("\n   Thread number: " + parms.getThreadNo());

		return sb.toString();
	}

	/**
	 * Default look and feel for the sequence panel
	 */
	private static class DefaultLook {
		public static final JPanel panel = new JPanel();
		public static final StatusBarEvent statusEvt = new org.geworkbench.events.StatusBarEvent(
				"...");
		public static final ProgressBarEvent progressEvt = new org.geworkbench.events.ProgressBarEvent(
				null, null, 0);
	}

	public SequenceDiscoveryViewAppComponent getAppComponent() {
		return appComponent;
	}
}
