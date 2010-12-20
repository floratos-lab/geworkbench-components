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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
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
import org.geworkbench.bison.datastructure.complex.pattern.SoapParmsDataSet;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.components.discovery.algorithm.AbstractSequenceDiscoveryAlgorithm;
import org.geworkbench.components.discovery.algorithm.AlgorithmStub;
import org.geworkbench.components.discovery.algorithm.RegularDiscoveryFileLoader;
import org.geworkbench.components.discovery.algorithm.ServerBaseDiscovery;
import org.geworkbench.components.discovery.model.PatternTableModelWrapper;
import org.geworkbench.components.discovery.view.PatternTableView;
import org.geworkbench.events.HistoryEvent;
import org.geworkbench.events.ProgressBarEvent;
import org.geworkbench.events.ProgressChangeEvent;
import org.geworkbench.events.StatusBarEvent;
import org.geworkbench.events.listeners.ProgressChangeListener;
import org.geworkbench.events.listeners.StatusChangeListener;
import org.geworkbench.util.AlgorithmSelectionPanel;
import org.geworkbench.util.remote.SPLASHDefinition;
import org.geworkbench.util.session.DiscoverySession;

import polgara.soapPD_wsdl.Parameters;

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
	private File currentResultFile;

	// Contains all the algorithm Stubs - they are mapped by the selected file.
	private Map<String, AlgorithmStub> algorithmStubMap = new HashMap<String, AlgorithmStub>();

	private AlgorithmSelectionPanel algoPanel = new org.geworkbench.util.AlgorithmSelectionPanel();

	// property changes
	public static final String TABLE_EVENT = "tableEvent";

	// the displayed view component in this widget
	private Component currentViewComponent = new JPanel();
	public static final int DEFAULT_VIEW = -1;
	public static final int PATTERN_TABLE = 0;

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

	private ParametersHandler parmsHandler = new ParametersHandler();
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
		if ((discoverySession != null) && (currentStubId != null)) {
			firePropertyChange(TABLE_EVENT, null, null);
			selectAlgorithm(discoverySession);
		} else {
			log.error("Warning: registerSession failed" + "[subId=" + currentStubId
					+ " session=" + discoverySession + "]");
		}
	}

	// invoked from SequenceDiscoveryViewAppComponent.receive(ProjectEvent, Object)
	void setMinSupportTypeName(String currentMinSupportTypeName) {
		if (currentMinSupportTypeName != null)
			parameterPanel.setCurrentSupportMenuStr(currentMinSupportTypeName);
	};

	private void selectAlgorithm(DiscoverySession discoverySession) {

		String selectedAlgo = algoPanel.getSelectedAlgorithmName();
		// the algorithm to run
		AbstractSequenceDiscoveryAlgorithm algorithm = null;

		// select the algorithm to run
		if (selectedAlgo.equalsIgnoreCase(AlgorithmSelectionPanel.DISCOVER)) {
			algorithm = createDiscoveryAlgorithm(discoverySession);
		} else if (selectedAlgo
				.equalsIgnoreCase(AlgorithmSelectionPanel.EXHAUSTIVE)) {
			algorithm = createExhaustive(discoverySession);
		} else {
			log.error("No Algorithm found...");
			return;
		}

		switchAlgo(selectedAlgo, algorithm, DEFAULT_VIEW);

	}

	private void switchAlgo(String selectedAlgo,
			AbstractSequenceDiscoveryAlgorithm algorithm, int viewId) {
		algorithm.addProgressChangeListener(this);
		AlgorithmStub stub = getStub(currentStubId);
		setStubAlgoAndPanel(stub, algorithm, selectedAlgo);
		algorithmStubMap.put(currentStubId, stub);

		algorithm.addStatusChangeListener(this);
		algorithm.setViewWidget(this);


		stub.gainedFocus(model);
		model.attach(stub.getResultDataSource());

		// replace the view and model
		setCurrentView(viewId);

		// start the algorithm
		stub.start(executeButton);
	}

	private DSAncillaryDataSet<? extends DSBioObject> resultData = null;

	public void setResultData(DSAncillaryDataSet<? extends DSBioObject> resultData) {
		this.resultData = resultData;
	}

	/**
	 * Reads the parameters from the parameter panel.
	 *
	 * @param type
	 */
	private SoapParmsDataSet readParameterAndCreateResultfile(String type) {
		SoapParmsDataSet pds = getParamsDataSet(type);
		String id = pds.getID();
		currentStubId = currentNodeID + id;
		resultData = pds;
		return pds;
	}

	public void firePropertyChangeAlgo(){
		appComponent.createNewNode(resultData);
		firePropertyChange(TABLE_EVENT, null, null);
	}

	private SoapParmsDataSet getParamsDataSet(String type) {
		Parameters p = parmsHandler.readParameter(parameterPanel,
				getSequenceDB().getSequenceNo(), type);
		parms = p;
		// fire a parameter change to the application
		org.geworkbench.bison.datastructure.complex.pattern.Parameters pp;
		pp = ParameterTranslation.translate(parms);
		pp.setMinSupportType(parameterPanel.getCurrentSupportMenuStr());// To
																		// fix
																		// bug
																		// 849

		SoapParmsDataSet pds = new SoapParmsDataSet(pp, "Pattern Discovery",
				getSequenceDB());
		return pds;
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
	private void projectFileChanged(String stub) {
		if (stub == null) {
			return;
		}

		if (currentStubId.equals(stub)) {
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
			loadPatternFile(currentResultFile);
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
			algoPanel.setSelectedAlgorithm(newStub.getDescription());
		} else {
			// set Default View
			setCurrentView(DEFAULT_VIEW);
			setCurrentParameterPanel(new ParameterPanel());
		}
	}

	/**
	 * Replaces the view for this component.
	 *
	 * @param i
	 *            the index of a view.
	 */
	public void setCurrentView(int i) {
		Component comp = null;
		if (i == DEFAULT_VIEW) {
			comp = DefaultLook.panel;
			statusBarChanged(DefaultLook.statusEvt);
			progressBarChanged(DefaultLook.progressEvt);
		} else {
			comp = view;
		}
		panelView.remove(currentViewComponent);
		currentViewComponent = comp;
		panelView.add(currentViewComponent, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	/**
	 * This method tries to attach a DataSource to one of the models.
	 */
	private void attachDataSource(
			org.geworkbench.util.patterns.DataSource source, AlgorithmStub stub) {
		// only one model now
		if (model.attach(source)) {
			// pass the model to the algorithm stub
			stub.gainedFocus(model);
			// update the view panel
			setCurrentView(0);
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

	private AlgorithmStub getStub(String key) {
		AlgorithmStub stub = algorithmStubMap.get(key);
		// if stub is null we need to create stub for this algorithm
		if (stub == null) {
			stub = new AlgorithmStub();
		} else {
			// we had a stub.
			// stop the previous algorithm
			stub.stop();
			// remove the model from the stopped algorithm
			stub.lostFocus(model);
			stub.removeStatusChangeListener(this);
		}
		return stub;
	}

	/**
	 * The plain vanilla sequence discovery.
	 */
	private AbstractSequenceDiscoveryAlgorithm createDiscoveryAlgorithm(
			DiscoverySession session) {
		SoapParmsDataSet resultFile = readParameterAndCreateResultfile("Discovery");
		AbstractSequenceDiscoveryAlgorithm abstractSequenceDiscoveryAlgorithm = new ServerBaseDiscovery(
				session, getParameters(), SPLASHDefinition.Algorithm.REGULAR);
		abstractSequenceDiscoveryAlgorithm.setResultFile(resultFile);
		abstractSequenceDiscoveryAlgorithm.setSequenceInputData(this
				.getSequenceDB());
		return abstractSequenceDiscoveryAlgorithm;
	}

	/**
	 * Initialize the stub.
	 *
	 * @param stub
	 *            AlgorithmStub
	 * @param algorithm
	 *            AbstractSequenceDiscoveryAlgorithm
	 * @param description
	 *            String
	 */
	private void setStubAlgoAndPanel(AlgorithmStub stub,
			AbstractSequenceDiscoveryAlgorithm algorithm, String description) {
		stub.setAlgorithm(algorithm);
		stub.setParameterPanel(parameterPanel);
		stub.setDescription(description);
	}

	/**
	 * Exhaustive
	 */
	private AbstractSequenceDiscoveryAlgorithm createExhaustive(
			DiscoverySession discoverySession) {
		SoapParmsDataSet resultFile = readParameterAndCreateResultfile("Exhaustive");
		AbstractSequenceDiscoveryAlgorithm abstractSequenceDiscoveryAlgorithm = new ServerBaseDiscovery(
				discoverySession, getParameters(), SPLASHDefinition.Algorithm.EXHAUSTIVE);
		abstractSequenceDiscoveryAlgorithm.setResultFile(resultFile);
		abstractSequenceDiscoveryAlgorithm.setSequenceInputData(this
				.getSequenceDB());
		return abstractSequenceDiscoveryAlgorithm;

	}

	private void stopButton_actionPerformed(ActionEvent e) {
		AlgorithmStub stub = algorithmStubMap.get(currentStubId);
		if (stub != null) {
			stub.stop();
		}
	}

	// invoked by SequenceDiscoveryViewAppComponent.receive(ProjectEvent, Object)
	synchronized void setSequenceDB(DSSequenceSet<? extends DSSequence> sDB,
			boolean withExistedPatternNode, String patternNodeID, Parameters p,
			File resultFile) {
		// reset the currentResultFile.
		currentResultFile = null;
		parameterPanel.setMaxSeqNumber(sDB.size());
		if (resultFile != null) {
			currentResultFile = resultFile;
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
				parmsHandler.writeParameter(parameterPanel, p);
			}
		}
	}

	// invoked by SequnceDiscoveryViewAppComponent.updateDataSetView()
	synchronized void setSequenceDB(DSSequenceSet<? extends DSSequence> sDB) {
		if(sequenceDB!=null)
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

	public Parameters getParameters() {
		return parms;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
	}

	private void loadPatternFile(File patternfile) {
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

		AbstractSequenceDiscoveryAlgorithm loader = null;
		String algoPanelName = "";
		int id = DEFAULT_VIEW;
		if (type != null
				&& type.equalsIgnoreCase(AlgorithmSelectionPanel.DISCOVER)) {
			loader = new RegularDiscoveryFileLoader(sequenceFile, patternfile, appComponent, false, (DSDataSet<DSSequence>) getSequenceDB());
			id = PATTERN_TABLE;
			algoPanelName = AlgorithmSelectionPanel.DISCOVER;
		} else {
			System.err.println("Loading failed. Did not recognize the data.");
			return;
		}

		switchAlgo(algoPanelName, loader, id);

		// fire a clear table event
		firePropertyChange(TABLE_EVENT, null, null);
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
		// updateFileProperty(chooser.getSelectedFile().getAbsolutePath());
		File sequenceFile = getSequenceDB().getFile();
		File patternfile = chooser.getSelectedFile();

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

		RegularDiscoveryFileLoader loader = null;
		String algoPanelName = "";
		int id = DEFAULT_VIEW;
		if (type != null
				&& type.equalsIgnoreCase(AlgorithmSelectionPanel.DISCOVER)) {
			loader = new RegularDiscoveryFileLoader(sequenceFile, patternfile, appComponent, true, (DSDataSet<DSSequence>) getSequenceDB());
			id = PATTERN_TABLE;
			algoPanelName = AlgorithmSelectionPanel.DISCOVER;
		} else {
			System.err.println("Loading failed. Did not recognize the data.");
			return;
		}

		switchAlgo(algoPanelName, loader, id);

		// fire a clear table event
		firePropertyChange(TABLE_EVENT, null, null);
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
		String selectedAlgo = algoPanel.getSelectedAlgorithmName();
		ProjectPanel.addToHistory(resultData, "Pattern Discovery"
				+"\nAlgorithm type: "+selectedAlgo
				+"\nParameters: \n"+parametersText()
				+"\nNumber of patterns found: "+patternFound
				);

		// trigger the update of history panel
		appComponent.publishHistoryEvent(new HistoryEvent(resultData));
	}

	private String parametersText() {
		return
		   "   Compute P-value: "+parms.getComputePValue()
		+"\n   Count of sequences: "+parms.getCountSeq()
		+"\n   Exact: "+parms.getExact()
		+"\n   Exact tokens: "+parms.getExactTokens()
		+"\n   Grouping N: "+parms.getGroupingN()
		+"\n   Grouping type: "+parms.getGroupingType()
		+"\n   Input name: "+parms.getInputName()
		+"\n   Max pattern number: "+parms.getMaxPatternNo()
		+"\n   Max run time: "+parms.getMaxRunTime()
		+"\n   Min pattern number: "+parms.getMinPatternNo()
		+"\n   Min per 100 support: "+parms.getMinPer100Support()
		+"\n   Min P-value: "+parms.getMinPValue()
		+"\n   Min support: "+parms.getMinSupport()
		+"\n   Min tokens: "+parms.getMinTokens()
		+"\n   Min W tokens: "+parms.getMinWTokens()
		+"\n   Output mode: "+parms.getOutputMode()
		+"\n   Print details: "+parms.getPrintDetails()
		+"\n   Similarity matrix: "+parms.getSimilarityMatrix()
		+"\n   Similarity threshold: "+parms.getSimilarityThreshold()
		+"\n   Sort mode: "+parms.getSortMode()
		+"\n   Threshold ID: "+parms.getThreadId()
		+"\n   Threshold number: "+parms.getThreadNo()
		+"\n   Window: "+parms.getWindow()
		;
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
