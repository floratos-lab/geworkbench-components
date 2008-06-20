package org.geworkbench.components.discovery;

import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.complex.pattern.SoapParmsDataSet;
import org.geworkbench.components.discovery.algorithm.*;
import org.geworkbench.components.discovery.model.GenericModel;
import org.geworkbench.components.discovery.model.PatternTableModelWrapper;
import org.geworkbench.components.discovery.model.PatternTreeModel;
import org.geworkbench.components.discovery.view.PatternTableView;
import org.geworkbench.components.discovery.view.PatternTreeView;
import org.geworkbench.events.ProgressBarEvent;
import org.geworkbench.events.StatusBarEvent;
import org.geworkbench.events.listeners.StatusChangeListener;
import org.geworkbench.util.AlgorithmSelectionPanel;
import org.geworkbench.util.remote.SPLASHDefinition;
import org.geworkbench.util.session.DiscoverySession;
import polgara.soapPD_wsdl.Parameters;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.HashMap;

/**
 * <p>
 * This class services the needs of different algorithms. It enables them to use
 * a "GUI template" for displaying the progress/status of a computation and for
 * displaying the transformation of their input data. Each algorithm instance is
 * associated with a "view" (Note: there is a one to many mapping btwn
 * algorithms and a particular view). A view is a graphical represenation of the
 * Algorithm result set. For an algorithm to display its results, a DataSource
 * is attached to a model of the corresponding view. The Datasource contains the
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
 * @version 1.0
 */

public class SequenceDiscoveryViewWidget extends JPanel implements
		StatusChangeListener, PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7663914616670165388L;

	// holds the id of the current selected project file.
	// this id is the key for mapping to an AlgorithmStub.
	private String currentStubId = "";

	// Hold the reference to current PD result.
	private File currentResultFile;

	// Contains all the algorithm Stubs - they are mapped by the selected file.
	private HashMap algorithmStubManager = new HashMap();

	// Conatains sessions - they are mapped by the selected file.
	private HashMap sessionManager = new HashMap();
	private HashMap globusManager = new HashMap();
	private AlgorithmSelectionPanel algoPanel = new org.geworkbench.util.AlgorithmSelectionPanel();

	// property changes
	public static final String PATTERN_DB = "patternDB";
	public static final String PARAMETERS = "parameters";
	public static final String TABLE_EVENT = "tableEvent";
	public static final String TREE_EVENT = "treeEvent";

	// the displayed view component in this widget
	private Component currentViewComponent = new JPanel();
	private final int DEFAULT_VIEW = -1;
	private int currentModel = 0;
	private final int PATTERN_TABLE = 0;
	private final int PATTERN_TREE = 1;

	// views and models
	private JPanel[] viewList = null;
	private GenericModel[] modelList;

	private Parameters parms = null;
	private BorderLayout borderLayout1 = new BorderLayout();
	private JPanel sequenceViewPanel = new JPanel();
	private JToolBar jToolBar1 = new JToolBar();
	private BorderLayout borderLayout3 = new BorderLayout();
	private DSSequenceSet sequenceDB = new CSSequenceSet();
	private SequenceDiscoveryViewAppComponent appComponent = null;
	private JPanel panelView = new JPanel();
	private JProgressBar progressBar = new JProgressBar(0, 100);
	private BorderLayout borderLayout4 = new BorderLayout();
	private JLabel jPatternLabel = new JLabel();
	private ParameterPanel parameterPanel = new ParameterPanel();

	private ParametersHandler parmsHandler = new ParametersHandler();
	private JCheckBox useglobus = new JCheckBox("use globus");
	private String currentNodeID = "";

	JButton executeButton = new JButton();
	JButton stopButton = new JButton();
	TitledBorder titledBorder4;
	Box box1;
	JButton loadBttn = new JButton();

	public SequenceDiscoveryViewWidget() throws Exception {
		initViewAndModel();
		try {
			jbInit();
		} catch (Exception ex) {
			System.err.println("SequenceDiscoveryViewWidget:::constructor: "
					+ ex.toString());
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * initialized the views and models
	 */
	private void initViewAndModel() {
		int view_model = 2;
		viewList = new JPanel[view_model];
		modelList = new GenericModel[view_model];

		// pattern table model
		PatternTableModelWrapper ptModel = new PatternTableModelWrapper();
		modelList[PATTERN_TABLE] = ptModel;
		// pattern table view
		PatternTableView patternTableView = new PatternTableView(ptModel, this);
		patternTableView.addPropertyChangeListener(this);
		viewList[PATTERN_TABLE] = patternTableView;

		// pattern tree model
		PatternTreeModel pTreeModel = new PatternTreeModel();
		modelList[this.PATTERN_TREE] = pTreeModel;
		// pattern tree view
		PatternTreeView pTreeView = new PatternTreeView(pTreeModel, this);
		pTreeModel.addTreeModelListener(pTreeView);
		pTreeView.addPropertyChangeListener(this);
		viewList[this.PATTERN_TREE] = pTreeView;
	}

	public void jbInit() throws Exception {
		ImageIcon startButtonIcon = new ImageIcon(this.getClass().getResource(
				"start.gif"));
		ImageIcon stopButtonIcon = new ImageIcon(this.getClass().getResource(
				"stop.gif"));
		ImageIcon loadtButtonIcon = new ImageIcon(this.getClass().getResource(
				"load.gif"));
		this.setLayout(borderLayout1);

		sequenceViewPanel.setLayout(borderLayout3);

		// progress bar init
		progressBar.setOrientation(JProgressBar.HORIZONTAL);
		progressBar.setBorder(BorderFactory.createEtchedBorder());
		progressBar.setMaximumSize(new Dimension(32767, 26));
		progressBar.setMinimumSize(new Dimension(10, 26));
		progressBar.setPreferredSize(new Dimension(104, 26));
		progressBar.setStringPainted(true);
		// View panel - different views plug into this panel
		panelView.setLayout(borderLayout4);

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

		useglobus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (useglobus.isSelected()) {
					DiscoverySession.isNormalSession = false;
				} else {
					DiscoverySession.isNormalSession = true;
				}
			}
		});

		this.add(sequenceViewPanel);
		jToolBar1.add(executeButton);
		jToolBar1.add(stopButton);
		jToolBar1.add(loadBttn);
		jToolBar1.add(useglobus);
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
	public void statusBarChanged(StatusBarEvent evt) {
		String message = evt.getSatus();
		jPatternLabel.setText(message);
	}

	/**
	 * The dispatch of the different algorithms startes here.
	 * 
	 * @param e
	 *            ActionEvent
	 */
	private void executButton_actionPerformed(ActionEvent e) {
		// get a discoverySession for running the algo
		String stubKey = getProjectFileId();
		DiscoverySession discoverySession = getSession(stubKey);
		if (discoverySession == null) { // we cannot run this algorithm with no
										// discoverySession
			return;
		}

		// fire a clear table event
		firePropertyChange(TABLE_EVENT, null, null);
		selectAlgorithm(discoverySession);
	}

	public void setMinSupportTypeName(String currentMinSupportTypeName) {
		// System.out.println("In SDVW: " + currentMinSupportTypeName);
		if (currentMinSupportTypeName != null)
			parameterPanel.setCurrentSupportMenuStr(currentMinSupportTypeName);
	};

	private void selectAlgorithm(DiscoverySession discoverySession) {

		String selectedAlgo = algoPanel.getSelectedAlgorithmName();
		// the algorithm to run
		AbstractSequenceDiscoveryAlgorithm algorithm = null;
		int viewId = DEFAULT_VIEW;

		// select the algorithm to run
		if (selectedAlgo.equalsIgnoreCase(AlgorithmSelectionPanel.DISCOVER)) {
			algorithm = discovery_actionPerformed(discoverySession);
			viewId = PATTERN_TABLE;
		} else if (selectedAlgo
				.equalsIgnoreCase(AlgorithmSelectionPanel.EXHAUSTIVE)) {
			algorithm = exhaustive_actionPerformed(discoverySession);
			viewId = PATTERN_TABLE;
		} else if (selectedAlgo
				.equalsIgnoreCase(AlgorithmSelectionPanel.HIERARCHICAL)) {
			algorithm = hierarc_actionPerformed(discoverySession);
			viewId = PATTERN_TREE;
		} else {
			System.err.print("No Algorithm found...");
			return;
		}

		switchAlgo(selectedAlgo, algorithm, viewId);

	}

	private void switchAlgo(String selectedAlgo,
			AbstractSequenceDiscoveryAlgorithm algorithm, int viewId) {
		String stubKey = getProjectFileId();
		AlgorithmStub stub = getStub(stubKey);
		setStubAlgoAndPanel(stub, algorithm, selectedAlgo);
		algorithmStubManager.put(stubKey, stub);

		algorithm.addStatusChangeListener(this);
		initAndStart(stub, viewId);
	}

	/**
	 * Reads the parameters from the parameter panel.
	 * 
	 * @param type
	 */

	private String readParameterAndCreateResultfile(String type) {
		Parameters p = parmsHandler.readParameter(parameterPanel,
				getSequenceDB().getSequenceNo(), type);
		parms = p;
		// fire a parameter change to the application
		org.geworkbench.bison.datastructure.complex.pattern.Parameters pp;
		pp = ParameterTranslation.getParameterTranslation().translate(parms);
		pp.setMinSupportType(parameterPanel.getCurrentSupportMenuStr());// To
																		// fix
																		// bug
																		// 849

		SoapParmsDataSet pds = new SoapParmsDataSet(pp, "Pattern Discovery",
				getSequenceDB());
		String id = pds.getID();
		File resultFile = pds.getResultFile();
		currentStubId = currentNodeID + id;
		firePropertyChange(PARAMETERS, null, pds);
		return resultFile.getAbsolutePath();
	}

	private void readParameter(String type) {
		Parameters p = parmsHandler.readParameter(parameterPanel,
				getSequenceDB().getSequenceNo(), type);
		parms = p;
		// fire a parameter change to the application
		org.geworkbench.bison.datastructure.complex.pattern.Parameters pp = ParameterTranslation
				.getParameterTranslation().translate(parms);
		pp.setMinSupportType(parameterPanel.getCurrentSupportMenuStr());// To
																		// fix
																		// bug
																		// 849
		SoapParmsDataSet pds = new SoapParmsDataSet(pp, "Pattern Discovery",
				getSequenceDB());
		String id = pds.getID();
		currentStubId = currentNodeID + id;
		// currentStubId+=id;
		firePropertyChange(PARAMETERS, null, pds);
	}

	/**
	 * The views communicate with this widget through property changes.
	 * 
	 * @param evt
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String property = evt.getPropertyName();
		if (property.equalsIgnoreCase(PatternTableView.ROWSELECTION)) {
			firePropertyChange(TABLE_EVENT, null, evt.getNewValue());
		} else if (property
				.equalsIgnoreCase(PatternTableView.PATTERN_ADDTO_PROJECT)) {
			firePropertyChange(PATTERN_DB, null, evt.getNewValue());
		} else if (property.equalsIgnoreCase(PatternTreeView.TREESELECTION)) {
			firePropertyChange(TREE_EVENT, null, evt.getNewValue());
		}
	}

	/**
	 * The function display the result of the discoverySession.
	 * 
	 * @param discoverySession
	 *            DiscoverySession
	 */
	public void viewResult(DiscoverySession discoverySession) {
		String stubKey = getProjectFileId();

		if (!registerSession(stubKey, discoverySession)) {
			return;
		}

		String algoServerName = "";
		Parameters p = null;
		try {
			algoServerName = discoverySession.getAlgorithmName();
			p = discoverySession.getParameters();
		} catch (Exception ex) {
		}

		String algoPanelName = "";
		int id = DEFAULT_VIEW;
		AbstractSequenceDiscoveryAlgorithm torun = null;
		// connect to the algorithm
		if (algoServerName.equalsIgnoreCase(SPLASHDefinition.Algorithm.REGULAR)) {
			torun = new RegularDiscovery(discoverySession);
			algoPanelName = AlgorithmSelectionPanel.DISCOVER;
			id = PATTERN_TABLE;
		} else if (algoServerName
				.equalsIgnoreCase(SPLASHDefinition.Algorithm.EXHAUSTIVE)) {
			torun = new ExhaustiveDiscovery(discoverySession);
			algoPanelName = AlgorithmSelectionPanel.EXHAUSTIVE;
			id = PATTERN_TABLE;
		} else if (algoServerName
				.equalsIgnoreCase(SPLASHDefinition.Algorithm.HIERARCHICAL)) {
			torun = new HierarchicalDiscovery(discoverySession);
			algoPanelName = AlgorithmSelectionPanel.HIERARCHICAL;
			id = PATTERN_TREE;
		} else {
			return;
		}

		updateParameterPanel(p);
		updateParameterPanel(p);
		changeAlgorithmSelection(algoPanelName);
		switchAlgo(algoPanelName, torun, id);
	}

	private void updateParameterPanel(Parameters p) {

		parmsHandler.writeParameter(parameterPanel, p);
	}

	/**
	 * The function returns a session.
	 * 
	 * @param stubId
	 *            an identifier for the seesion
	 * @return session or null if a session does not exist.
	 */
	private DiscoverySession getSession(String stubId) {
		// DiscoverySession s = DiscoverySession.isNormalSession ?
		// (DiscoverySession) sessionManager.get(stubId) : (DiscoverySession)
		// globusManager.get(stubId);
		//
		// if (s != null && !s.isFailed()) {
		// return s;
		// }
		// we don't have a session mapped. Get one from the app component.
		DiscoverySession s = appComponent.getSession();
		return registerSession(stubId, s) ? s : null;
	}

	/**
	 * Registers a sesssion.
	 * 
	 * @param stubId
	 *            String
	 * @param s
	 *            DiscoverySession
	 * @return boolean
	 */
	private boolean registerSession(String stubId, DiscoverySession s) {
		if ((s != null) && (stubId != null)) {
			if (DiscoverySession.isNormalSession) {
				sessionManager.put(stubId, s);
			} else {
				globusManager.put(stubId, s);
			}
			return true;
		}
		System.err.println("Warning: registerSession failed" + "[subId="
				+ stubId + " session=" + s + "]");
		return false;
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

		AlgorithmStub oldStub = (AlgorithmStub) algorithmStubManager
				.get(oldStubId);
		AlgorithmStub newStub = (AlgorithmStub) algorithmStubManager
				.get(currentStubId);

		if (currentResultFile != null) {
			loadPatternFile(currentResultFile);
		}
		if (oldStub == null && newStub == null) {
			// no algorithm stub is mapped

			return;
		}

		if (oldStub != null) {
			oldStub.lostFocus(modelList[currentModel]);
			oldStub.removeStatusChangeListener(this);
		}

		if (newStub != null) {
			newStub.addStatusChangeListener(this);
			attachDataSource(newStub.getResultDataSource(), newStub);
			// update the algorithms selection panel to reflect the running
			// algo.
			changeAlgorithmSelection(newStub.getDescription());
		} else {
			setDefaultView();
		}
	}

	/**
	 * Sets the default view for a file with no stub association
	 */
	private void setDefaultView() {
		setCurrentView(DEFAULT_VIEW);
		setCurrentParameterPanel(new ParameterPanel());
	}

	private void changeAlgorithmSelection(String algorithmDescription) {
		algoPanel.setSelectedAlgorithm(algorithmDescription);
	}

	/**
	 * Replaces the view for this component.
	 * 
	 * @param i
	 *            the index of a view.
	 */
	private void setCurrentView(int i) {
		Component comp = null;
		if ((i < 0) || (i > modelList.length)) {
			comp = DefaultLook.panel;
			statusBarChanged(DefaultLook.statusEvt);
			progressBarChanged(DefaultLook.progressEvt);
		} else {
			comp = viewList[i];
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
		// we iterate over the models, searching for a model that can display
		// this algorithm result.
		int i = 0;
		for (; i < modelList.length; i++) {
			if (modelList[i].attach(source)) {
				// pass the model to the algorithm stub
				stub.gainedFocus(modelList[i]);
				setCurrentModel(i);
				// update the view panel
				setCurrentView(i);
				setCurrentParameterPanel(stub.getParameterPanel());
				break;
			}
		}

		if (i == modelList.length) {
			// we should not be here -- each algorithm should have a model!
			System.err.println("No model found for this algoritm");
			setCurrentView(DEFAULT_VIEW);
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

	private synchronized String getProjectFileId() {
		return currentStubId;
	}

	private AlgorithmStub getStub(String key) {
		AlgorithmStub stub = (AlgorithmStub) algorithmStubManager.get(key);
		// if stub is null we need to create stub for this algorithm
		if (stub == null) {
			stub = new AlgorithmStub();
		} else {
			// we had a stub.
			// stop the previous algorithm
			stub.stop();
			// remove the model from the stopped algorithm
			stub.lostFocus(modelList[currentModel]);
			stub.removeStatusChangeListener(this);
		}
		return stub;
	}

	/**
	 * The plain vanilla sequence discovery.
	 */
	private AbstractSequenceDiscoveryAlgorithm discovery_actionPerformed(
			DiscoverySession session) {
		String resultStr = readParameterAndCreateResultfile("Discovery");
		AbstractSequenceDiscoveryAlgorithm abstractSequenceDiscoveryAlgorithm = new RegularDiscovery(
				session, getParameters());
		abstractSequenceDiscoveryAlgorithm.setResultFile(new File(resultStr));
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
	public AbstractSequenceDiscoveryAlgorithm exhaustive_actionPerformed(
			DiscoverySession discoverySession) {

		// readParameter("Exhaustive");
		String resultStr = readParameterAndCreateResultfile("Exhaustive");
		AbstractSequenceDiscoveryAlgorithm abstractSequenceDiscoveryAlgorithm = new ExhaustiveDiscovery(
				discoverySession, getParameters());
		abstractSequenceDiscoveryAlgorithm.setResultFile(new File(resultStr));
		abstractSequenceDiscoveryAlgorithm.setSequenceInputData(this
				.getSequenceDB());
		return abstractSequenceDiscoveryAlgorithm;

	}

	/**
	 * Hiearchical discovery.
	 */
	public AbstractSequenceDiscoveryAlgorithm hierarc_actionPerformed(
			DiscoverySession discoverySession) {

		String resultStr = readParameterAndCreateResultfile("Hiearchical");
		AbstractSequenceDiscoveryAlgorithm abstractSequenceDiscoveryAlgorithm = new HierarchicalDiscovery(
				discoverySession, getParameters());
		abstractSequenceDiscoveryAlgorithm.setResultFile(new File(resultStr));
		abstractSequenceDiscoveryAlgorithm.setSequenceInputData(this
				.getSequenceDB());
		return abstractSequenceDiscoveryAlgorithm;

	}

	private void initAndStart(AlgorithmStub stub, int viewId) {
		GenericModel model = modelList[viewId];
		stub.gainedFocus(model);
		model.attach(stub.getResultDataSource());
		setViewAndModel(viewId);

		// start the algorithm
		stub.start(executeButton);
	}

	private void setViewAndModel(int index) {
		// replace the view and model
		setCurrentView(index);
		setCurrentModel(index);
	}

	private void stopButton_actionPerformed(ActionEvent e) {
		String stubKey = getProjectFileId();
		AlgorithmStub stub = (AlgorithmStub) algorithmStubManager.get(stubKey);
		if (stub != null) {
			stub.stop();
		}
	}

	private void setCurrentModel(int model) {
		currentModel = model;
	}

	public void setParms(Parameters parms) {
		this.parms = parms;
	}

	public synchronized void setSequenceDB(DSSequenceSet sDB,
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
				updateParameterPanel(p);
			}

		}
	}

	public synchronized void setSequenceDB(DSSequenceSet sDB) {
		if(sequenceDB!=null)
		 sequenceDB = sDB;
		 
//			String stubID = sDB.getID() + sDB.getDataSetName();
//			// Point currentNodeID to the name associated with the sequenceDB
//			// name no matter the node is subnode or node.
//			currentNodeID = stubID;
//
//			// change the stub for the widget
//			projectFileChanged(stubID);
	 

	}

	public synchronized void setSequenceDB(DSSequenceSet sDB,
			boolean withExistedPatternNode, String patternNodeID, Parameters p) {
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
				updateParameterPanel(p);
			}

		}
	}

	public synchronized DSSequenceSet getSequenceDB() {
		 return sequenceDB;
	}

	public Parameters getParms() {
		return parms;
	}

	public void setSequenceDiscoveryViewAppComponent(
			SequenceDiscoveryViewAppComponent s) {
		appComponent = s;
	}

	public SequenceDiscoveryViewAppComponent getSequenceDiscoveryViewAppComponent() {
		return appComponent;
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
			loader = new RegularDiscoveryFileLoader(sequenceFile, patternfile);
			id = PATTERN_TABLE;
			algoPanelName = AlgorithmSelectionPanel.DISCOVER;
		} else if (type != null
				&& type.equalsIgnoreCase(AlgorithmSelectionPanel.HIERARCHICAL)) {
			loader = new HierarchicalDiscoveryFileLoader(sequenceFile,
					patternfile);
			id = PATTERN_TREE;
			algoPanelName = AlgorithmSelectionPanel.HIERARCHICAL;

		} else {
			System.err.println("Loading failed. Did not recognize the data.");
			return;
		}

		switchAlgo(algoPanelName, loader, id);

		// fire a clear table event
		firePropertyChange(TABLE_EVENT, null, null);
	}

	void loadBttn_actionPerformed(ActionEvent e) {
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

		AbstractSequenceDiscoveryAlgorithm loader = null;
		String algoPanelName = "";
		int id = DEFAULT_VIEW;
		if (type != null
				&& type.equalsIgnoreCase(AlgorithmSelectionPanel.DISCOVER)) {
			loader = new RegularDiscoveryFileLoader(sequenceFile, patternfile);
			id = PATTERN_TABLE;
			algoPanelName = AlgorithmSelectionPanel.DISCOVER;
		} else if (type != null
				&& type.equalsIgnoreCase(AlgorithmSelectionPanel.HIERARCHICAL)) {
			loader = new HierarchicalDiscoveryFileLoader(sequenceFile,
					patternfile);
			id = PATTERN_TREE;
			algoPanelName = AlgorithmSelectionPanel.HIERARCHICAL;

		} else {
			System.err.println("Loading failed. Did not recognize the data.");
			return;
		}

		switchAlgo(algoPanelName, loader, id);

		// fire a clear table event
		firePropertyChange(TABLE_EVENT, null, null);
	}
}

/**
 * Default look and feel for the sequence panel
 * 
 * @version 1.0
 */
class DefaultLook {
	public static final JPanel panel = new JPanel();
	public static final StatusBarEvent statusEvt = new org.geworkbench.events.StatusBarEvent(
			"...");
	public static final ProgressBarEvent progressEvt = new org.geworkbench.events.ProgressBarEvent(
			null, null, 0);
}
