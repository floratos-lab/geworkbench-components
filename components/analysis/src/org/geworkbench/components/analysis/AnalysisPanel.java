package org.geworkbench.components.analysis;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.apache.axis.types.URI.MalformedURIException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractAnalysisLabelComparator;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.analysis.HighlightCurrentParameterThread;
import org.geworkbench.analysis.ParameterKey;
import org.geworkbench.analysis.ReHighlightable;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet; 
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject; 
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.structure.CSProteinStructure;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel; 
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.Analysis;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.geworkbench.bison.model.analysis.ParameterPanelIncludingNormalized;
import org.geworkbench.bison.model.analysis.ProteinSequenceAnalysis;
import org.geworkbench.bison.model.analysis.ProteinStructureAnalysis;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.components.cagrid.gui.GridServicePanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.ComponentRegistry;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.events.AnalysisAbortEvent;
import org.geworkbench.events.AnalysisCompleteEvent;
import org.geworkbench.events.AnalysisInvokedEvent;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.CommandBase;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;
import org.geworkbench.util.pathwaydecoder.mutualinformation.EdgeListDataSet;
import org.ginkgo.labs.ws.GridEndpointReferenceType;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.columbia.geworkbench.cagrid.dispatcher.client.DispatcherClient;

/**
 * Application component offering users a selection of microarray data
 * clustering options.
 * 
 * @author First Genetic Trust Inc.
 * @author keshav
 * @author yc2480
 * @version $Id$
 * 
 */
@AcceptTypes( { DSMicroarraySet.class, EdgeListDataSet.class,
		CSProteinStructure.class, CSSequenceSet.class })
public class AnalysisPanel extends CommandBase implements
		VisualPlugin, ReHighlightable {

	private static Log log = LogFactory.getLog(AnalysisPanel.class);

	/* static variables */
	private static final String DEFAULT_PARAMETER_SETTING_NAME = "New Parameter Setting Name";
	private static final String SERVICE = "Service";
	private static final String PARAMETERS = "Parameters";
	private static final String USER_INFO = "userinfo";
	private static final int ANALYSIS_TAB_COUNT = 1;
	private static final String USER_INFO_DELIMIETER = "==";

	private static final String NEWLINE = "\n";
	private static final String TAB = "\t";
	
	/* from application.properties */
	private final static String DISPATCHER_URL = "dispatcher.url";

	/* from PropertiesManager (user preference) */
	private static final String GRID_HOST_KEY = "dispatcherURL";

	private String dispatcherUrl = System.getProperty(DISPATCHER_URL);

	private String userInfo = null;

	/* user interface */
	private JPanel analysisPanel = null;

	private final ParameterPanel emptyParameterPanel = new ParameterPanel();;

	private JPanel selectedAnalysisParameterPanel = null;

	private ParameterPanel currentParameterPanel = null;

	private JButton analyze = null;

	private JTabbedPane jAnalysisTabbedPane = null;

	private GridServicePanel jGridServicePanel = null;

	private JButton save = null;
	private JButton delete = null;

	// threads to check submitted caGrid service jobs
	private List<Thread> threadList = new ArrayList<Thread>();

	private AbstractAnalysis selectedAnalysis = null;

	private JComboBox parameterComboBox = new JComboBox();

	/**
	 * Default Constructor
	 */
	public AnalysisPanel() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/**
		 * Resets the list of analysis.
		 */
		if (currentDataType == null)
			return;
		else {
			log.error("I don't see how this can happen");
			if (currentDataType.equals(CSProteinStructure.class)) {
				getAvailableAnalyses(ProteinStructureAnalysis.class);
			} else if (currentDataType.equals(CSSequenceSet.class)) {
				getAvailableAnalyses(ProteinSequenceAnalysis.class);
			} else {
				getAvailableAnalyses(ClusteringAnalysis.class);
			}
		}
	}

	/**
	 * initialize GUI
	 * 
	 * @throws Exception
	 *             exception thrown during GUI construction
	 */
	private void init() throws Exception {
		// this part used to be in MicroarrayViewEvent: initialize tool bar for 'all markers', 'all arrays'
		mainPanel = new JPanel();
		JToolBar jToolBar3 = new JToolBar();
		mainPanel.setLayout(new BorderLayout());
		chkAllMarkers.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				refreshMaSetView();
				mainPanel.repaint();
			}

		});
		chkAllArrays.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				refreshMaSetView();
				mainPanel.repaint();
			}

		});
		jToolBar3.add(chkAllArrays, null);
		jToolBar3.add(chkAllMarkers, null);
		mainPanel.add(jToolBar3, java.awt.BorderLayout.SOUTH);

		analysisPanel = new JPanel();
		JScrollPane analysisScrollPane = new JScrollPane();

		save = new JButton("Save Settings");
		delete = new JButton("Delete Settings");
		selectedAnalysisParameterPanel = new JPanel();
		currentParameterPanel = emptyParameterPanel;

		analyze = new JButton("Analyze");
		/* Double it's width */
		Dimension d = analyze.getPreferredSize();
		d.setSize(d.getWidth() * 2, d.getHeight());
		analyze.setPreferredSize(d);

		analysisPanel.setLayout(new BorderLayout());

		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save_actionPerformed(e);
			}

		});
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delete_actionPerformed(e);
			}

		});

		selectedAnalysisParameterPanel.setLayout(new BorderLayout());
		currentParameterPanel.setLayout(new BorderLayout());

		analyze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(startAnalysis()) {
					hideDialog();
				}
			}

		});

		parameterComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				namedParameterSelection_action(e);
			}
		});
		parameterComboBox.setAutoscrolls(true);

		popMenuItem = "Analysis";
		analysisPanel.add(analysisScrollPane, BorderLayout.CENTER);
		
		JPanel innerAnalysisPanel = new JPanel();
		innerAnalysisPanel.setLayout(new BorderLayout());
		analysisScrollPane.getViewport().add(innerAnalysisPanel, null);
		JPanel analysisMainPanel = new JPanel();
		analysisMainPanel.setLayout(new BoxLayout(analysisMainPanel, BoxLayout.Y_AXIS));
		innerAnalysisPanel.add(analysisMainPanel, BorderLayout.CENTER);

		selectedAnalysisParameterPanel.add(currentParameterPanel,
				BorderLayout.CENTER);

		JPanel parameterPanel = new JPanel();
		parameterPanel.setLayout(new BorderLayout());
		parameterPanel.add(selectedAnalysisParameterPanel, BorderLayout.CENTER);

		/* buttons */
		save.setPreferredSize(analyze.getPreferredSize());
		delete.setPreferredSize(analyze.getPreferredSize());
		delete.setEnabled(false);

		FormLayout layout = new FormLayout("right:100dlu,10dlu", "");
		DefaultFormBuilder buttonsBuilder = new DefaultFormBuilder(layout);
		buttonsBuilder.setDefaultDialogBorder();
		buttonsBuilder.appendSeparator("Analysis Actions");
		buttonsBuilder.append(analyze);
		buttonsBuilder.nextLine();
		buttonsBuilder.append(save);
		buttonsBuilder.nextLine();
		buttonsBuilder.append(delete);

		parameterPanel.add(buttonsBuilder.getPanel(), BorderLayout.LINE_END);

		JPanel jPanel1 = new JPanel();
		analysisMainPanel.add(jPanel1);
		jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.LINE_AXIS));
		jPanel1.add(Box.createRigidArea(new Dimension(5, 0)));
		jPanel1.add(new JLabel("Analysis"));
		jPanel1.add(Box.createRigidArea(new Dimension(5, 0)));
		jPanel1.add(new JLabel("Saved Parameters"));
		jPanel1.add(Box.createRigidArea(new Dimension(5, 0)));
		jPanel1.add(parameterComboBox, null);
		jPanel1.setMaximumSize(new Dimension(1000, 50));
		jPanel1.setAlignmentX(Component.LEFT_ALIGNMENT);

		jAnalysisTabbedPane = new JTabbedPane();
		parameterPanel.setName(PARAMETERS);
		jAnalysisTabbedPane.add(parameterPanel);
		jAnalysisTabbedPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		analysisMainPanel.add(jAnalysisTabbedPane);

		mainPanel.add(analysisPanel, BorderLayout.CENTER);
	}

	@SuppressWarnings("rawtypes")
	@Publish
	public org.geworkbench.events.SubpanelChangedEvent publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent event) {
		return event;
	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	@Publish
	public AnalysisInvokedEvent publishAnalysisInvokedEvent(
			AnalysisInvokedEvent event) {
		return event;
	}

	/**
	 * 
	 * @param ppne
	 * @param source
	 */
	@Subscribe
	public void receive(
			org.geworkbench.events.PendingNodeLoadedFromWorkspaceEvent ppne,
			Object source) {
		DispatcherClient dispatcherClient = null;
		try {
			PropertiesManager pm = PropertiesManager.getInstance();
			String savedHost = null;
			try {
				savedHost = pm.getProperty(this.getClass(), GRID_HOST_KEY,
						dispatcherUrl);
				if (!StringUtils.isEmpty(savedHost)) {
					dispatcherUrl = savedHost;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			dispatcherClient = new DispatcherClient(dispatcherUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Collection<GridEndpointReferenceType> gridEprs = ppne.getGridEprs();

		for (GridEndpointReferenceType gridEpr : gridEprs) {

			// to control the complexity, let's not support the analysis Abort/Complete event
			// in the case of restoring workspace for now
			PollingThread pollingThread = new PollingThread(gridEpr,
					dispatcherClient, null, this);
			threadList.add(pollingThread);
			pollingThread.start();

		}

	}

	 

	@Subscribe
	public void receive(org.geworkbench.events.PendingNodeCancelledEvent e,
			Object source) {
		for (Iterator<Thread> iterator = threadList.iterator(); iterator
				.hasNext();) {
			PollingThread element = (PollingThread) iterator.next();
			if (element.getGridEPR() == e.getGridEpr()) {
				element.cancel();
			}
		}
	}

	/**
	 * 
	 * @return boolean
	 */
	private boolean isGridAnalysis() {
		if (jGridServicePanel != null) {
			return jGridServicePanel.isCaGridVersion();
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @return String
	 */
	private String getServiceUrl() {
		return jGridServicePanel.getServiceUrl();
	}

	/*
	 * 
	 */
	private void getUserInfo() {
		final JDialog userpasswdDialog = new JDialog();
		log.debug("getting user info...");

		DefaultFormBuilder usernamePasswdPanelBuilder = new DefaultFormBuilder(
				new FormLayout("right:35dlu"));

		final JTextField usernameField = new JTextField(15);
		final JPasswordField passwordField = new JPasswordField(15);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String username = usernameField.getText();
				String passwd = new String(passwordField.getPassword());
				if (username.trim().equals("") || passwd.trim().equals("")) {
					userInfo = null;
				} else {
					userInfo = username + USER_INFO_DELIMIETER + passwd;
					PropertiesManager properties = PropertiesManager
							.getInstance();
					try {
						properties.setProperty(this.getClass(), USER_INFO,
								String.valueOf(userInfo));
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
				userpasswdDialog.dispose();
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				userInfo = "";
				userpasswdDialog.dispose();
			}
		});

		/* add to button panel */
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		/* the builder */
		usernamePasswdPanelBuilder.appendColumn("5dlu");
		usernamePasswdPanelBuilder.appendColumn("45dlu");

		usernamePasswdPanelBuilder.append("username", usernameField);
		usernamePasswdPanelBuilder.append("password", passwordField);

		PropertiesManager pm = PropertiesManager.getInstance();
		String savedUserInfo = null;
		try {
			savedUserInfo = pm.getProperty(this.getClass(), USER_INFO, "");
			if (!StringUtils.isEmpty(savedUserInfo)) {
				String s[] = savedUserInfo.split(USER_INFO_DELIMIETER);
				if (s.length >= 2) {
					usernameField.setText(s[0]);
					passwordField.setText(s[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		JPanel indexServicePanel = new JPanel(new BorderLayout());
		indexServicePanel.add(usernamePasswdPanelBuilder.getPanel());
		indexServicePanel.add(buttonPanel, BorderLayout.SOUTH);
		userpasswdDialog.add(indexServicePanel);
		userpasswdDialog.setModal(true);
		userpasswdDialog.pack();
		Util.centerWindow(userpasswdDialog);
		userpasswdDialog.setVisible(true);
		log.debug("got user info: " + userInfo);
	}

	/**
	 * Set the parameters panel used in the analysis pane.
	 * 
	 * @param parameterPanel
	 *            parameter panel stored on the file system
	 */
	private void setParametersPanel(ParameterPanel parameterPanel) {
		selectedAnalysisParameterPanel.remove(currentParameterPanel);
		currentParameterPanel = parameterPanel;
		selectedAnalysisParameterPanel.add(currentParameterPanel,
				BorderLayout.CENTER);
		analysisPanel.revalidate();
		analysisPanel.repaint();
		if (currentParameterPanel instanceof AbstractSaveableParameterPanel)
			((AbstractSaveableParameterPanel) currentParameterPanel)
					.setParameterHighlightCallback(new HighlightCurrentParameterThread(
							this));
	}

	/**
	 * Update the list that shows the known preset parameter settings for the
	 * selected filter.
	 * 
	 * @param storedParameters
	 */
	private void setNamedParameters(String[] storedParameters) {
		parameterComboBox.removeAllItems();
		parameterComboBox.addItem("");
		for(String n: storedParameters) {
			parameterComboBox.addItem(n);
		}

		analysisPanel.revalidate();
		highlightCurrentParameterGroup();
	}

	/**
	 * scan the saved list, see if the parameters in it are same as current one,
	 * if yes, highlight it.
	 */
	private void highlightCurrentParameterGroup() {
		if(selectedAnalysis==null)return;
		
		ParameterPanel currentParameterPanel = selectedAnalysis
				.getParameterPanel();
		String[] parametersNameList = selectedAnalysis
				.getNamesOfStoredParameterSets();
		parameterComboBox.setSelectedIndex(0);
		for (int i = 0; i < parametersNameList.length; i++) {
			Map<Serializable, Serializable> parameter1 = ((AbstractSaveableParameterPanel) currentParameterPanel)
					.getParameters();
			Map<Serializable, Serializable> parameter2 = new HashMap<Serializable, Serializable>();
			parameter2.putAll(selectedAnalysis
					.getNamedParameterSet(parametersNameList[i]));
			parameter2.remove(ParameterKey.class.getSimpleName());
			if (parameter1.equals(parameter2)) {
				analysisPanel.revalidate();
				parameterComboBox.setSelectedIndex(i+1);
				/*
				 * Since we don't allow duplicate parameter sets in the list, so
				 * if we detect one, we can skip the rest.
				 */
				break;
			}
		}
	}

	/**
	 * 
	 */
	public void refreshHighLight() {
		highlightCurrentParameterGroup();
	}

	/**
	 * Delete the selected saved parameter.
	 * 
	 * @param name -
	 *            name of the saved parameter
	 */
	private void removeNamedParameter(String name) {
		selectedAnalysis.removeNamedParameter(name);
		this.setNamedParameters(selectedAnalysis
				.getNamesOfStoredParameterSets());
	}

	/* action listeners */
	/**
	 * Listener invoked when the "Save Settings" button is pressed.
	 * 
	 * @param e
	 */
	private void save_actionPerformed(ActionEvent e) {

		/*
		 * If the parameterSet already exist, we popup a message window to
		 * inform user
		 */
		ParamValidationResults pvr = selectedAnalysis.validateParameters();
		if (!pvr.isValid()) {
			JOptionPane.showMessageDialog(null, pvr.getMessage(),
					"Parameter Validation Error", JOptionPane.ERROR_MESSAGE);
		     return;
		}
		if (selectedAnalysis
				.parameterSetExist(selectedAnalysis.getParameters())) {
			JOptionPane.showMessageDialog(null, "ParameterSet already exists.",
					"Canceled", JOptionPane.OK_OPTION);
		} else {
			/*
			 * A pop-up window for the user to enter the parameter name. If the
			 * currently displayed parameter already has a name associated with
			 * it, use that name in the pop-up, otherwise the default.
			 */
			int index = parameterComboBox.getSelectedIndex();
			String namedParameter = null;
			if (index != 0) {
				namedParameter = (String) parameterComboBox.getItemAt(
						index);
			} else {
				namedParameter = DEFAULT_PARAMETER_SETTING_NAME;
			}
			String paramName = JOptionPane.showInputDialog(analysisPanel,
					namedParameter, namedParameter);
			File checkFile = new File(selectedAnalysis.scrubFilename(paramName));
			if (checkFile.exists()) {
				int answer = JOptionPane
						.showConfirmDialog(
								null,
								"The requested parameter set name is already used by another set in the same directory. Click OK to override it, or click Cancel to choose another name.",
								"Warning", JOptionPane.OK_CANCEL_OPTION);
				if (answer == JOptionPane.CANCEL_OPTION) {
					return;
				}
			}
			if (paramName != null) {

				selectedAnalysis.saveParameters(paramName);

				String[] savedParameterSetNames = selectedAnalysis
						.getNamesOfStoredParameterSets();

				/* set the JList to display the saved parameter groups */
				setNamedParameters(savedParameterSetNames);

			}
		}
	}

	/**
	 * Listener invoked when the "Delete Settings" button is pressed
	 * 
	 * @param e
	 */
	private void delete_actionPerformed(ActionEvent e) {
		if (selectedAnalysis != null
				&& this.parameterComboBox.getSelectedIndex() <= 0) {
			JOptionPane.showMessageDialog(null,
					"You must select a setting before you can delete it.",
					"Canceled", JOptionPane.OK_OPTION);
		} else {
			int choice = JOptionPane.showConfirmDialog(null,
					"Are you sure you want to delete the saved parameters?",
					"Deleting Saved Parameters", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);

			if ((selectedAnalysis != null) && (choice == 0)
					&& (this.parameterComboBox.getSelectedIndex() > 0)) {
				log.info("Deleting saved parameters: "
						+ (String) this.parameterComboBox.getSelectedItem());
				this.removeNamedParameter((String) this.parameterComboBox
						.getSelectedItem());
				if (this.parameterComboBox.getItemCount() <= 1)
					this.delete.setEnabled(false);
			}
		}
	}

	protected void setSelectedCommandByName(String commandName) {

		delete.setEnabled(false);

		selectedAnalysis = getCommandByName(commandName);

		/* Set the parameters panel for the selected analysis. */
		ParameterPanel paramPanel = selectedAnalysis.getParameterPanel();
		if (paramPanel != null) {
			String[] storedParameterSetNames = selectedAnalysis
					.getNamesOfStoredParameterSets();
			setNamedParameters(storedParameterSetNames);
			setParametersPanel(paramPanel);

			String className = paramPanel.getClass().getName();
			if (className.equals("org.geworkbench.components.ttest.MultiTTestAnalysisPanel")
					|| className.equals("org.geworkbench.components.ttest.TtestAnalysis"))
				chkAllArrays.setVisible(false);
			else
				chkAllArrays.setVisible(true);

			/*
			 * If it's first time (means just after load from file) for this
			 * analysis, assign last saved parameters to current parameter panel
			 * and highlight last saved group.
			 */
			if (paramPanel instanceof AbstractSaveableParameterPanel) {
				if (((AbstractSaveableParameterPanel) paramPanel).isFirstTime()) {
					selectLastSavedParameterSet();
					((AbstractSaveableParameterPanel) paramPanel)
							.setFirstTime(false);
				}
			}

			save.setEnabled(true);
		} else {
			setParametersPanel(emptyParameterPanel);
			save.setEnabled(false);
			/*
			 * Since the analysis admits no parameters, there are no named
			 * parametersettings to show.
			 */
			setNamedParameters(new String[0]);
		}

		if (selectedAnalysis instanceof AbstractGridAnalysis) {
			if (selectedAnalysis != pidMap.get(currentDataType)) {
				jGridServicePanel = new GridServicePanel(SERVICE);
				jGridServicePanel.setAnalysisType(selectedAnalysis);
				if (jAnalysisTabbedPane.getTabCount() > ANALYSIS_TAB_COUNT)
					jAnalysisTabbedPane.remove(ANALYSIS_TAB_COUNT);

				jAnalysisTabbedPane.addTab("Services", jGridServicePanel);
			}
		} else {
			jAnalysisTabbedPane.remove(jGridServicePanel);
			jGridServicePanel = null; // TODO this is just a quick fix for bug
			// 0001174, Quick fix made user input
			// service information every time.
			// Should have a better implementation.
		}
		pidMap.put(currentDataType, selectedAnalysis);
	}

	/**
	 * 
	 */
	private void selectLastSavedParameterSet() {
		int lastIndex = parameterComboBox.getItemCount();
		if (lastIndex > 0) {
			String paramName = selectedAnalysis.getLastSavedParameterSetName();
			/* load from memory */
			Map<Serializable, Serializable> parameters = selectedAnalysis
					.getNamedParameterSet(paramName);
			if (parameters != null) // fix share directory issue in gpmodule
				selectedAnalysis.setParameters(parameters);
		} else {
			/* nothing saved, so select nothing */
		}
	}

	/**
	 * Listener invoked when a named parameter is selected.
	 * 
	 * @param e
	 */
	private void namedParameterSelection_action(ActionEvent e) {
		if (selectedAnalysis == null) {
			delete.setEnabled(false);
			return;
		}
		int index = parameterComboBox.getSelectedIndex();
		if (index > 0) {
			delete.setEnabled(true);

			String paramName = (String) parameterComboBox.getItemAt(
					index);
			/* load from memory */
			Map<Serializable, Serializable> parameters = selectedAnalysis
					.getNamedParameterSet(paramName);
			selectedAnalysis.setParameters(parameters);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private DSMicroarraySetView getDataSetView() {
		DSMicroarraySetView dataSetView = new CSMicroarraySetView(this.refMASet);
		if (activatedMarkers != null && activatedMarkers.panels().size() > 0)
			dataSetView.setMarkerPanel(activatedMarkers);
		if (activatedArrays != null && activatedArrays.panels().size() > 0 && activatedArrays.size() > 0)
			dataSetView.setItemPanel(activatedArrays);
		dataSetView.useMarkerPanel(!chkAllMarkers.isSelected());
		dataSetView.useItemPanel(!chkAllArrays.isSelected());

		return dataSetView;
	}

	/**
	 * Listener invoked when the "Analyze" button is pressed.
	 * 
	 * @param e
	 * @return true if the analysis is actual started instead abort, e.g. for invalid input
	 */
	@SuppressWarnings("unchecked")
	private boolean startAnalysis() {
		DSDataSet<? extends DSBioObject> dataset = ProjectPanel.getInstance()
				.getDataSet();
		if ((refMASet == null && (refOtherSet == null || refOtherSet != dataset))
				|| (refMASet !=null && refMASet != dataset) ) {
			JOptionPane
					.showMessageDialog(
							null,
							"Dataset node is not currently selected. Please try to left click the data node first.",
							"Data not selected",
							JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		maSetView = getDataSetView();

		boolean onlyActivatedArrays = false;
		if (currentParameterPanel instanceof ParameterPanelIncludingNormalized) {
			onlyActivatedArrays = true;

			ParameterPanelIncludingNormalized p = (ParameterPanelIncludingNormalized)currentParameterPanel;
			boolean isLogNormalized = p.isLogNormalized();

			boolean isLogNormalizedFromGuess = guessLogNormalized(maSetView);

			if (isLogNormalizedFromGuess != isLogNormalized) {
				String theMessage = "The checkbox 'Data is log2-tranformed' may not be set correctly. Do you want to proceed anyway?";
				int result = JOptionPane.showConfirmDialog((Component) null,
						theMessage, "alert", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.NO_OPTION)
					return false;
			}
		} else {
			onlyActivatedArrays = !chkAllArrays.isSelected();
		}

		if (selectedAnalysis == null
				|| ((refMASet == null) && (refOtherSet == null))) {
			return false;
		}

		AnalysisInvokedEvent event = null;
		if (refOtherSet != null) { /*
									 * added for analysis that do not take in
									 * microarray data set
									 */

			event = new AnalysisInvokedEvent(
					selectedAnalysis, "");
			publishAnalysisInvokedEvent(event);
		} else if ((maSetView != null) && (refMASet != null)) {
			event = new AnalysisInvokedEvent(
					selectedAnalysis, maSetView.getDataSet().getLabel());
			publishAnalysisInvokedEvent(event);
		}
		final AnalysisInvokedEvent invokeEvent = event;

		ParamValidationResults pvr = selectedAnalysis.validateParameters();
		if (!pvr.isValid()) {
			JOptionPane.showMessageDialog(null, pvr.getMessage(),
					"Parameter Validation Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			analyze.setEnabled(false);
			maSetView.useMarkerPanel(!chkAllMarkers.isSelected());
			maSetView.useItemPanel(onlyActivatedArrays);
			Thread t = new Thread(new Runnable() {
				public void run() {
					/* check if we are dealing with a grid analysis */
					if (isGridAnalysis()) {
						submitAsCaGridService(invokeEvent);
					} else {						
						executeLocally(invokeEvent);
					}
					analyze.setEnabled(true);
				}

			});
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
			return true;
		}
	}

	private void submitAsCaGridService(final AnalysisInvokedEvent invokeEvent) {

		Date startDate = new Date();
		Long startTime =startDate.getTime();
		
		AbstractGridAnalysis selectedGridAnalysis = (AbstractGridAnalysis) selectedAnalysis;

		ParamValidationResults validResult = ((AbstractGridAnalysis) selectedAnalysis)
				.validInputData(maSetView, refMASet);
		if (!validResult.isValid()) {
			JOptionPane.showMessageDialog(null, validResult
					.getMessage(), "Invalid Input Data",
					JOptionPane.ERROR_MESSAGE);
			return;
		} else if(validResult.getMessage()!=null && validResult.getMessage().equals("QUIT")) {
			return;
		}

		if (selectedGridAnalysis.isAuthorizationRequired()) {
			/* ask for username and password */
			getUserInfo();
			if (userInfo == null) {
				JOptionPane
						.showMessageDialog(
								null,
								"Please make sure you entered a valid username and password",
								"Invalid User Account",
								JOptionPane.ERROR_MESSAGE);
				publishAnalysisAbortEvent(new AnalysisAbortEvent(invokeEvent));
				return;
			}
			if (StringUtils.isEmpty(userInfo)) {
				userInfo = null;
				publishAnalysisAbortEvent(new AnalysisAbortEvent(invokeEvent));
				return;
			}
		}

		String url = getServiceUrl();
		if (StringUtils.isEmpty(url)) {
			log.error("Cannot execute with url:  " + url);
			JOptionPane.showMessageDialog(null,
					"Cannot execute grid analysis: Invalid URL "+url+" specified.",
					"Invalid grid URL Error", JOptionPane.ERROR_MESSAGE);
			publishAnalysisAbortEvent(new AnalysisAbortEvent(invokeEvent));
			return;
		}

		ProgressBar pBar = Util.createProgressBar("Grid Services",
				"Submitting service request");
		pBar.start();
		pBar.reset();

		List<Serializable> serviceParameterList = ((AbstractGridAnalysis) selectedGridAnalysis)
				.handleBisonInputs(maSetView, refOtherSet);

		/* adding user info */
		serviceParameterList.add(userInfo);

		dispatcherUrl = jGridServicePanel.getDispatcherUrl();
		DispatcherClient dispatcherClient = null;
		GridEndpointReferenceType gridEpr = null;
		try {
			dispatcherClient = new DispatcherClient(dispatcherUrl);
			gridEpr = dispatcherClient.submit(serviceParameterList, url,
					((AbstractGridAnalysis) selectedGridAnalysis)
							.getBisonReturnType());
		} catch (MalformedURIException e) {
			e.printStackTrace();
			publishAnalysisAbortEvent(new AnalysisAbortEvent(invokeEvent));
			return;
		} catch (RemoteException e) {
			e.printStackTrace();
			publishAnalysisAbortEvent(new AnalysisAbortEvent(invokeEvent));
			return;
		} finally {
			pBar.stop();
		}

		/* generate history for grid analysis */	
		String history = "Grid service started at: " + Util.formatDateStandard(startDate) + ", milliseconds=" + startTime + NEWLINE;
		history += "Grid service information:" + NEWLINE;
		history += TAB + "Index server url: "
				+ jGridServicePanel.getIndexServerUrl() + NEWLINE;
		history += TAB + "Dispatcher url: " + dispatcherUrl
				+ NEWLINE;
		history += TAB + "Service url: " + url + NEWLINE
				+ NEWLINE;
		history += selectedGridAnalysis.createHistory()
	            + NEWLINE;
		
		if (refOtherSet != null) {
			history += selectedGridAnalysis.generateHistoryStringForGeneralDataSet(refOtherSet);
		} else if (maSetView != null && refMASet != null) {
			history += selectedGridAnalysis.generateHistoryForMaSetView(maSetView, selectedGridAnalysis.useMarkersFromSelector());
		}

		ProjectPanel.getInstance().addPendingNode(gridEpr,
				selectedGridAnalysis.getLabel() + " (pending)", history, false);

		PollingThread pollingThread = new PollingThread(gridEpr,
				dispatcherClient, invokeEvent, this);
		threadList.add(pollingThread);
		pollingThread.start();
	}

	// this method is only invoked form background thread
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void executeLocally(final AnalysisInvokedEvent invokeEvent) {
		
		Date startDate = new Date();
		Long startTime =startDate.getTime();
		
		AlgorithmExecutionResults results = null;
		if (refOtherSet != null) {
			// first case: analysis that does not take in microarray data set
			if (refOtherSet instanceof CSSequenceSet)
				((CSSequenceSet)refOtherSet).useMarkerPanel(!chkAllMarkers.isSelected());
			results = selectedAnalysis.execute(refOtherSet);
		} else if (maSetView != null && refMASet != null) {
			// second case: analysis that takes microarray set 
			if(selectedAnalysis instanceof AbstractGridAnalysis) {
				ParamValidationResults validResult = ((AbstractGridAnalysis) selectedAnalysis)
				.validInputData(maSetView, refMASet);
				if (!validResult.isValid()) {
					JOptionPane.showMessageDialog(null, validResult.getMessage(),
							"Invalid Input Data", JOptionPane.ERROR_MESSAGE);
					results = null;
					analyze.setEnabled(true);
					publishAnalysisAbortEvent(new AnalysisAbortEvent(invokeEvent));
					return;
				} else if(validResult.getMessage().equals("QUIT")) {
					publishAnalysisAbortEvent(new AnalysisAbortEvent(invokeEvent));
					return;
				}
			}
			results = selectedAnalysis.execute(maSetView);
		}
		
		// check the result before further publishing event 
		if (results == null) {
			log.info("null result"); // e.g. cancelled
			publishAnalysisAbortEvent(new AnalysisAbortEvent(invokeEvent));
			return;
		}
		/*
		 * If everything was OK construct and fire the proper application-level
		 * event, thus notify interested application components of the results
		 * of the analysis operation. If there were problems encountered, let
		 * the user know.
		 */
		if (!results.isExecutionSuccessful()) {
			JOptionPane.showMessageDialog(null, results.getMessage(),
					"Analysis Error", JOptionPane.ERROR_MESSAGE);
			publishAnalysisAbortEvent(new AnalysisAbortEvent(invokeEvent));
			return;
		}
		Object resultObject = results.getResults();
		if (resultObject instanceof DSAncillaryDataSet) {
			DSAncillaryDataSet<DSBioObject> dataSet = (DSAncillaryDataSet<DSBioObject>) resultObject;		
			
			//add start/end time to history
			String history = "Analysis started at: " + Util.formatDateStandard(startDate) +  NEWLINE;
			HistoryPanel.addBeforeToHistory(dataSet, history);
			 
			Date endDate = new Date();
			long endTime = endDate.getTime();
			history = "\nAnalysis finished at: "
					+ Util.formatDateStandard(endDate) + NEWLINE;			 
			long elapsedTime = endTime - startTime;
			history += "\nTotal elapsed time: " + DurationFormatUtils.formatDurationHMS(elapsedTime);
			HistoryPanel.addToHistory(dataSet, history);
			
			ProjectPanel.getInstance().addProjectNode(null, dataSet);
		}
		if (resultObject instanceof Hashtable) {
			DSPanel<DSGeneMarker> panel = ((Hashtable<?, DSPanel<DSGeneMarker>>) resultObject)
					.get("Significant Genes");
			if (panel != null) {
				publishSubpanelChangedEvent(new org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker>(
						DSGeneMarker.class, panel, SubpanelChangedEvent.NEW));
			}
		}
		publishAnalysisCompleteEvent(new AnalysisCompleteEvent(invokeEvent));
	}
	
	@Publish
	public AnalysisAbortEvent publishAnalysisAbortEvent(AnalysisAbortEvent analysisAbortEvent) {
		return analysisAbortEvent;
	}

	@Publish
	public AnalysisCompleteEvent publishAnalysisCompleteEvent(AnalysisCompleteEvent analysisCompleteEvent) {
		return analysisCompleteEvent;
	}

	private Class<?> currentDataType = null, lastDataType = null;
	private HashMap<Class<?>, AbstractAnalysis> pidMap = new HashMap<Class<?>, AbstractAnalysis>();

	@SuppressWarnings("rawtypes")
	private DSDataSet refOtherSet = null;
	/**
	 * Refresh the list of available analyses.
	 */
	@SuppressWarnings({ "rawtypes" })
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent event, Object source) {

		// the first part used to be in MicroarrayViewEventBase
		if (event.getMessage().equals(org.geworkbench.events.ProjectEvent.CLEARED)) {
			refMASet = null;
		} else {
			DSDataSet<?> dataSet = event.getDataSet();
			if (dataSet instanceof DSMicroarraySet) {
				if (refMASet != dataSet) {
					this.refMASet = (DSMicroarraySet) dataSet;					
				}
			}
			refreshMaSetView();
		}
		
		DSDataSet dataSet = event.getDataSet();
		if(dataSet==null) return;
		
		clearMenuItems();
		if ( dataSet instanceof DSMicroarraySet ) {
			refOtherSet = null;
		} else {
			refOtherSet = dataSet;
			refMASet = null;
		}
		if (event.getParent() == null  && !pendingNodeSelected()) { // if not a sub-node under DataSet node nor a pending node
			lastDataType = currentDataType;
			currentDataType = event.getDataSet().getClass();
			if (!pidMap.containsKey(currentDataType) || lastDataType != currentDataType)
				pidMap.put(currentDataType, null);
			if (event.getDataSet().getClass().equals(CSProteinStructure.class)) {
				getAvailableAnalyses(ProteinStructureAnalysis.class);
			} else if (event.getDataSet().getClass().equals(CSSequenceSet.class)) {
				getAvailableAnalyses(ProteinSequenceAnalysis.class);
			} else {
				getAvailableAnalyses(ClusteringAnalysis.class);
			}
			updateMenuItems();
		}
	}

	/**
	 * Get Analysis of given type.
	 */
	private void getAvailableAnalyses(Class<? extends Analysis> analysisType) {
		boolean selectionChanged = true;
		Analysis[] analyses = ComponentRegistry.getRegistry().getModules(analysisType);
		availableCommands = new AbstractAnalysis[analyses.length];
		for (int i = 0; i < analyses.length; i++) {
			availableCommands[i] = (AbstractAnalysis) analyses[i];
			if (selectedAnalysis == availableCommands[i]) {
				selectionChanged = false;
			}
		}
		if (selectionChanged) {
			if (availableCommands.length > 0) {
				selectedAnalysis = availableCommands[0];
			} else {
				selectedAnalysis = null;
			}
		}
		
		String[] names = new String[availableCommands.length];
		for (int i = 0; i < availableCommands.length; i++) {
			names[i] = ComponentRegistry.getRegistry().getDescriptorForPlugin(
					availableCommands[i]).getLabel();
			availableCommands[i].setLabel(names[i]);
		}

		AbstractAnalysisLabelComparator comparator = new AbstractAnalysisLabelComparator();
		Arrays.sort(availableCommands, comparator);
	}
	
	private boolean guessLogNormalized(DSMicroarraySetView<DSGeneMarker, DSMicroarray> data) {
		if (data == null) {
			return false;
		}
		
		boolean isLogNormalized = false;
		try {
			DSDataSet<DSMicroarray> set = data.getDataSet();
			if (set instanceof DSMicroarraySet) {
				DSMicroarraySet maSet = (DSMicroarraySet) set;
				double minValue = Double.POSITIVE_INFINITY;
				double maxValue = Double.NEGATIVE_INFINITY;
				for (DSMicroarray microarray : maSet) {
					DSMarkerValue[] values = microarray
							.getMarkerValues();
					double v;
					for (DSMarkerValue value : values) {
						v = value.getValue();
						if (v < minValue) {
							minValue = v;
						}
						if (v > maxValue) {
							maxValue = v;
						}
					}
				}
				if (maxValue - minValue < 100) {
					isLogNormalized = true;
				} else {
					isLogNormalized = false;
				}
			}
		} catch (Exception e) {
			// do nothing, TtestAnalysis.execute() will do validation
		}
		return isLogNormalized;

	}

	////////////////////////////////////////////////////////////////////////////////////
	// the following code used to be in MicroarrayViewEventBase. see bug 2743 note 10396
	/**
	 * The reference microarray set.
	 */
	private DSMicroarraySet refMASet = null;
	private DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView = null;

	private JCheckBox chkAllMarkers = new JCheckBox("All Markers", false);
	private JCheckBox chkAllArrays = new JCheckBox("All Arrays", false);

	private final String markerLabelPrefix = "  Markers: ";
	private JLabel numMarkersSelectedLabel = new JLabel(markerLabelPrefix);
	private JPanel mainPanel;

	private DSPanel<DSGeneMarker> activatedMarkers = null;
	private DSPanel<DSMicroarray> activatedArrays = null;

	/**
	 * getComponent
	 *
	 * @return Component
	 */
	public Component getComponent() {
		return mainPanel;
	}

	/**
	 * geneSelectorAction
	 *
	 * @param e
	 *            GeneSelectorEvent
	 */
	@Subscribe
	public void receive(GeneSelectorEvent e, Object source) {

		log.debug("Source object " + source);

		DSPanel<DSGeneMarker> markersPanel = e.getPanel();
		activatedMarkers = new CSPanel<DSGeneMarker>();
		if (markersPanel != null && markersPanel.size() > 0) {            
			for (int j = 0; j < markersPanel.panels().size(); j++) {
				DSPanel<DSGeneMarker> mrk = markersPanel.panels().get(j);
				if (mrk.isActive()) {
					for (int i = 0; i < mrk.size(); i++) {						
						activatedMarkers.add(mrk.get(i));

					}
				}
			}
			markersPanel = activatedMarkers;

			numMarkersSelectedLabel.setText(markerLabelPrefix
					+ activatedMarkers.size() + "  ");

		} else {
			numMarkersSelectedLabel.setText(markerLabelPrefix);
		}

		if (markersPanel!=null)
			refreshMaSetView();

	}

	/**
	 * phenotypeSelectorAction
	 *
	 * @param e
	 *            PhenotypeSelectorEvent
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Subscribe
	public void receive(org.geworkbench.events.PhenotypeSelectorEvent e,
			Object source) {

		log.debug("Source object " + source);

		if (e.getTaggedItemSetTree() != null) {
			activatedArrays = e.getTaggedItemSetTree().activeSubset();
		}

		refreshMaSetView();

	}

	/**
	 * Refreshes the chart view.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	final private void refreshMaSetView() {
		maSetView = new CSMicroarraySetView(this.refMASet);
		if (activatedMarkers != null && activatedMarkers.panels().size() > 0)
			maSetView.setMarkerPanel(activatedMarkers);
		if (activatedArrays != null && activatedArrays.panels().size() > 0 && activatedArrays.size() > 0)
			maSetView.setItemPanel(activatedArrays);
		maSetView.useMarkerPanel(!chkAllMarkers.isSelected());
		maSetView.useItemPanel(!chkAllArrays.isSelected());
	}
}