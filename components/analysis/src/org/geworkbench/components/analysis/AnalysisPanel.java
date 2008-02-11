package org.geworkbench.components.analysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.geworkbench.components.cagrid.gui.GridServicePanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.ComponentRegistry;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.AnalysisInvokedEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.events.ProjectNodeCompletedEvent;
import org.geworkbench.events.ProjectNodePendingEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;
import org.geworkbench.util.microarrayutils.MicroarrayViewEventBase;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;
import org.geworkbench.util.pathwaydecoder.mutualinformation.EdgeListDataSet;
import org.ginkgo.labs.ws.GridEndpointReferenceType;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.columbia.geworkbench.cagrid.dispatcher.client.DispatcherClient;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust
 * @version 1.0
 */

/**
 * Application component offering users a selection of microarray data
 * clustering options.
 */
@AcceptTypes( { DSMicroarraySet.class, AdjacencyMatrixDataSet.class,
		EdgeListDataSet.class })
public class AnalysisPanel extends MicroarrayViewEventBase implements
		VisualPlugin {

	private static final String SERVICE = "Service";

	private static final String PARAMETERS = "Parameters";

	private static final int ANALYSIS_TAB_COUNT = 1;

	private Log log = LogFactory.getLog(this.getClass());

	// FIXME this needs to be read from configure file
	private final String dispatcherUrl = "http://156.145.29.181:8080/wsrf/services/cagrid/Dispatcher";

	/**
	 * The underlying GUI panel for the clustering component
	 */
	protected JPanel analysisPanel = new JPanel();

	/**
	 * Contains the pluggable clustering analyses available to the user to
	 * choose from. These analyses will have been defined in the application
	 * configuration file as <code>plugin</code> components and they are
	 * expected to have been associated with the extension point
	 * <code>clustering</code>.
	 */
	protected AbstractAnalysis[] availableAnalyses;

	/**
	 * The most recently used analysis.
	 */
	protected AbstractAnalysis selectedAnalysis = null;

	/**
	 * Results obtained from execution of an analysis. This is an instance
	 * variable as the analysis is carried out on a worker thread.
	 */
	private AlgorithmExecutionResults results = null;

	/**
	 * Visual Widget
	 */
	private JScrollPane jScrollPane2 = new JScrollPane();

	/**
	 * Visual Widget
	 */
	private JPanel jPanel3 = new JPanel();

	/**
	 * Visual Widget
	 */
	private BorderLayout borderLayout2 = new BorderLayout();

	/**
	 * Visual Widget
	 */
	private BorderLayout borderLayout3 = new BorderLayout();

	/**
	 * Visual Widget
	 */
	private GridLayout gridLayout1 = new GridLayout();

	/**
	 * Visual Widget
	 */
	private GridLayout gridLayout2 = new GridLayout();

	/**
	 * Parameter panel with no saved parameters
	 */
	private ParameterPanel emptyParameterPanel = new AbstractSaveableParameterPanel();

	/**
	 * Visual Widget
	 */
	private JSplitPane jSplitPane1 = new JSplitPane();

	/**
	 * Visual Widget
	 */
	private JButton save = new JButton("Save Settings");

	/**
	 * Visual Widget
	 */
	private JButton delete = new JButton("Delete Settings");

	/**
	 * Visual Widget
	 */
	private JPanel jPanel4 = new JPanel();

	/**
	 * Visual Widget
	 */
	private ParameterPanel currentParameterPanel = emptyParameterPanel;

	/**
	 * Visual Widget
	 */
	private JPanel buttons = new JPanel();

	/**
	 * Visual Widget
	 */
	private BorderLayout borderLayout4 = new BorderLayout();

	/**
	 * Visual Widget
	 */
	private BorderLayout borderLayout5 = new BorderLayout();

	/**
	 * Visual Widget
	 */
	private JPanel jParameterPanel = null;

	/**
	 * Visual Widget
	 */
	private JButton analyze = new JButton("Analyze");

	/**
	 * Visual Widget
	 */
	private BorderLayout borderLayout6 = new BorderLayout();

	/**
	 * Visual Widget
	 */
	private GridLayout gridLayout3 = new GridLayout();

	JList namedParameters = new JList();

	/**
	 * Visual Widget
	 */
	private JScrollPane jScrollPane1 = new JScrollPane();

	/**
	 * Visual Widget
	 */
	private JPanel jPanel1 = new JPanel();

	JList pluginAnalyses = new JList();

	/**
	 * Visual Widget
	 */
	private JScrollPane jScrollPane3 = new JScrollPane();

	// keshav
	private JTabbedPane jAnalysisTabbedPane = null;

	private GridServicePanel jGridServicePanel = null;

	// end keshav

	/**
	 * Default Constructor
	 */
	public AnalysisPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		reset();
	}

	public AnalysisPanel getAnalysisPanel() {
		return this;
	}

	/**
	 * Utility method to construct the GUI
	 * 
	 * @throws Exception
	 *             exception thrown during GUI construction
	 */
	public void jbInit() throws Exception {
		super.jbInit();
		analysisPanel = new JPanel();
		jScrollPane2 = new JScrollPane();
		jPanel3 = new JPanel();
		borderLayout2 = new BorderLayout();
		borderLayout3 = new BorderLayout();
		gridLayout1 = new GridLayout();
		gridLayout2 = new GridLayout();
		emptyParameterPanel = new AbstractSaveableParameterPanel();
		jSplitPane1 = new JSplitPane();
		save = new JButton("Save Settings");
		delete = new JButton("Delete Settings");
		jPanel4 = new JPanel();
		currentParameterPanel = emptyParameterPanel;
		buttons = new JPanel();
		borderLayout4 = new BorderLayout();
		borderLayout5 = new BorderLayout();
		jParameterPanel = new JPanel();

		analyze = new JButton("Analyze");
		// Double it's width
		Dimension d = analyze.getPreferredSize();
		d.setSize(d.getWidth() * 2, d.getHeight());
		analyze.setPreferredSize(d);

		borderLayout6 = new BorderLayout();
		gridLayout3 = new GridLayout();
		namedParameters = new JList();
		jScrollPane1 = new JScrollPane();
		jPanel1 = new JPanel();
		pluginAnalyses = new JList();
		jScrollPane3 = new JScrollPane();

		analysisPanel.setLayout(borderLayout2);
		jPanel3.setLayout(borderLayout3);
		gridLayout1.setColumns(2);
		gridLayout1.setRows(3);
		gridLayout1.setVgap(0);
		gridLayout2.setColumns(4);
		gridLayout2.setRows(3);
		// Make sure that only one analysis can be selected at a time;
		// Make sure that only one parameter set can be selected at a time;
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
		jPanel4.setLayout(borderLayout4);
		currentParameterPanel.setLayout(borderLayout5);
		// buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		// buttons.setPreferredSize(new Dimension(248, 60));
		jParameterPanel.setLayout(new BorderLayout());
		analyze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				analyze_actionPerformed(e);
			}

		});
		jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPane1.setDividerSize(3);
		namedParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		namedParameters.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				namedParameterSelection_action(e);
			}
		});
		namedParameters.setAutoscrolls(true);
		namedParameters.setBorder(BorderFactory.createLineBorder(Color.black));
		jScrollPane1.setPreferredSize(new Dimension(248, 68));
		jPanel1.setLayout(gridLayout3);
		jPanel1.setMinimumSize(new Dimension(0, 0));
		jPanel1.setPreferredSize(new Dimension(50, 50));
		jPanel1.setToolTipText("");
		pluginAnalyses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pluginAnalyses.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				analysisSelected_action(e);
			}
		});
		pluginAnalyses.setBorder(BorderFactory.createLineBorder(Color.black));
		gridLayout3.setColumns(2);
		analysisPanel.add(jScrollPane2, BorderLayout.CENTER);
		jScrollPane2.getViewport().add(jPanel3, null);
		jPanel3.add(jSplitPane1, BorderLayout.CENTER);
		jSplitPane1.add(jParameterPanel, JSplitPane.BOTTOM);

		jPanel4.add(currentParameterPanel, BorderLayout.CENTER);
		jParameterPanel.add(jPanel4, BorderLayout.CENTER);

		// Add buttons
		save.setPreferredSize(analyze.getPreferredSize());
		delete.setPreferredSize(analyze.getPreferredSize());
		delete.setEnabled(false);
		FormLayout layout = new FormLayout("right:100dlu,10dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Analysis Actions");
		builder.append(analyze);
		builder.nextLine();
		builder.append(save);
		builder.nextLine();
		builder.append(delete);

		jParameterPanel.add(builder.getPanel(), BorderLayout.LINE_END);

		jSplitPane1.add(jPanel1, JSplitPane.TOP);
		jPanel1.add(jScrollPane1, null);
		jPanel1.add(jScrollPane3, null);
		jScrollPane3.getViewport().add(namedParameters);
		jScrollPane1.getViewport().add(pluginAnalyses);

		// keshav
		jAnalysisTabbedPane = new JTabbedPane();
		jParameterPanel.setName(PARAMETERS);
		jAnalysisTabbedPane.add(jParameterPanel);
		jSplitPane1.add(jAnalysisTabbedPane);
		// end keshav

		mainPanel.add(analysisPanel, BorderLayout.CENTER);
	}

	/**
	 * Queries the extension point <code>clustering</code> within the
	 * <code>PluginRegistry </code> for available analysis-type plugins. <p/>
	 * This method gets invoked every time that the analysis pane gets the
	 * focus, in order to get the most recent list of analyses: given dynamic
	 * loading of components this approach guarantees that any new plugins
	 * loaded between uses of the analysis panel, will be correctly picked up.
	 */
	public void getAvailableAnalyses() {
		// To check if the last used analysis is still available.
		boolean selectionChanged = true;
		ClusteringAnalysis[] analyses = ComponentRegistry.getRegistry()
				.getModules(ClusteringAnalysis.class);
		availableAnalyses = new AbstractAnalysis[analyses.length];
		for (int i = 0; i < analyses.length; i++) {
			availableAnalyses[i] = (AbstractAnalysis) analyses[i];
			if (selectedAnalysis == availableAnalyses[i]) {
				selectionChanged = false;
			}
		}
		if (selectionChanged) {
			if (availableAnalyses.length > 0) {
				selectedAnalysis = availableAnalyses[0];
			} else {
				selectedAnalysis = null;
			}
		}
	}

	/**
	 * Obtains from the <code>PluginRegistry</code> ans displays the set of
	 * available filters.
	 */
	public void reset() {
		// Get the most recent available normalizers. Redisplay
		getAvailableAnalyses();
		displayAnalyses();
	}

	/**
	 * Displays the list of available analyses.
	 */
	private void displayAnalyses() {
		// Show graphical components
		pluginAnalyses.removeAll();
		// Stores the dispay names of the available analyses.
		String[] names = new String[availableAnalyses.length];
		for (int i = 0; i < availableAnalyses.length; i++) {
			names[i] = ComponentRegistry.getRegistry().getDescriptorForPlugin(
					availableAnalyses[i]).getLabel();
			if (log.isDebugEnabled())
				if (availableAnalyses[i] instanceof AbstractGridAnalysis) {
					log.info("Analysis: " + availableAnalyses[i]
							+ ", Is grid enabled? " + true);
				} else {
					log.info("Analysis: " + availableAnalyses[i]
							+ ", Is grid enabled? " + false);
				}
		}

		pluginAnalyses.setListData(names);
		if (selectedAnalysis != null) {
			pluginAnalyses.setSelectedValue(selectedAnalysis.getLabel(), true);
		} else {
			setParametersPanel(this.emptyParameterPanel);
			save.setEnabled(false);
		}
		analysisPanel.revalidate();
	}

	/**
	 * Set the parameters panel used in the analysis pane.
	 * 
	 * @param parameterPanel
	 *            parameter panel stored on the file system
	 */
	private void setParametersPanel(ParameterPanel parameterPanel) {
		jPanel4.remove(currentParameterPanel);
		currentParameterPanel = parameterPanel;
		jPanel4.add(currentParameterPanel, BorderLayout.CENTER);
		analysisPanel.revalidate();
		analysisPanel.repaint();
	}

	/**
	 * Update the list that shows the known preset parameter settings for the
	 * selected filter.
	 * 
	 * @param storedParameters
	 *            parameters stored on the file system
	 */
	private void setNamedParameters(String[] storedParameters) {
		namedParameters.removeAll();
		namedParameters.setListData(storedParameters);
		// Make sure that only one parameter set can be selected at a time;
		namedParameters.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		analysisPanel.revalidate();
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

	/**
	 * Listener invoked when a new analysis is selected from the displayed list
	 * of analyses.
	 * 
	 * @param lse
	 *            The <code>ListSelectionEvent</code> received from the list
	 *            selection.
	 */
	private void analysisSelected_action(ListSelectionEvent lse) {
		if (pluginAnalyses.getSelectedIndex() == -1) {
			return;
		}
		delete.setEnabled(false);
		selectedAnalysis = availableAnalyses[pluginAnalyses.getSelectedIndex()];
		// Set the parameters panel for the selected analysis.
		ParameterPanel paramPanel = selectedAnalysis.getParameterPanel();
		// Set the list of available named parameters for the selected analysis.
		if (paramPanel != null) {
			setParametersPanel(paramPanel);
			setNamedParameters(availableAnalyses[pluginAnalyses
					.getSelectedIndex()].getNamesOfStoredParameterSets());
			save.setEnabled(true);
		} else {
			setParametersPanel(this.emptyParameterPanel);
			save.setEnabled(false);
			// Since the analysis admits no parameters, there are no named
			// parameter
			// settings to show.
			setNamedParameters(new String[0]);
		}

		// keshav
		if (selectedAnalysis instanceof AbstractGridAnalysis) {
			jGridServicePanel = new GridServicePanel(SERVICE);
			jGridServicePanel.setAnalysisType(selectedAnalysis);
			if (jAnalysisTabbedPane.getTabCount() > ANALYSIS_TAB_COUNT)
				jAnalysisTabbedPane.remove(ANALYSIS_TAB_COUNT);

			jAnalysisTabbedPane.addTab("Services", jGridServicePanel);
		} else {
			jAnalysisTabbedPane.remove(jGridServicePanel);
			jGridServicePanel=null; // this is just a quick fix for bug 0001174, Quick fix made user input service information every time. Should have a better implementation. 
		}
	}

	/**
	 * Listener invoked when a named parameter is selected from the relevant
	 * JList.
	 * 
	 * @param e
	 *            the <code>ListSelectionEvent</code> received from the
	 *            <code>MouseListener</code> listening to the namedParameters
	 *            JList
	 */
	private void namedParameterSelection_action(ListSelectionEvent e) {
		if (selectedAnalysis == null) {
			delete.setEnabled(false);
			return;
		}
		int index = namedParameters.getSelectedIndex();
		if (index != -1) {
			delete.setEnabled(true);
			setParametersPanel(selectedAnalysis
					.getNamedParameterSetPanel((String) namedParameters
							.getModel().getElementAt(index)));
		}
	}

	/**
	 * Listener invoked when the "Analyze" button is pressed.
	 * 
	 * @param e
	 *            <code>ActionEvent</code> generated by the "analyze" button
	 */
	private void analyze_actionPerformed(ActionEvent e) {
		maSetView = getDataSetView();
		if (selectedAnalysis == null
				|| ((refMASet == null) && (refOtherSet == null))) {
			return;
		}

		AnalysisInvokedEvent event = new AnalysisInvokedEvent(selectedAnalysis
				.getLabel());
		publishAnalysisInvokedEvent(event);

		ParamValidationResults pvr = selectedAnalysis.validateParameters();
		if (!pvr.isValid()) {
			JOptionPane.showMessageDialog(null, pvr.getMessage(),
					"Parameter Validation Error", JOptionPane.ERROR_MESSAGE);
		} else {
			analyze.setEnabled(false);
			maSetView.useMarkerPanel(onlyActivatedMarkers);
			maSetView.useItemPanel(onlyActivatedArrays);
			Thread t = new Thread(new Runnable() {
				public void run() {
					ProgressBar pBar = null;

					try {
						/* check if we are dealing with a grid analysis */
						if (isGridAnalysis()) {
							pBar = Util.createProgressBar("Grid Services",
									"Submitting service request");
							pBar.start();
							pBar.reset();
							String url = getServiceUrl();
							if (!StringUtils.isEmpty(url)) {

								List<Serializable> serviceParameterList = new ArrayList<Serializable>();

								serviceParameterList.add(maSetView);
								if (refOtherSet != null) { // added for
															// analysis that do
															// not take in
															// microarray data
															// set
									serviceParameterList.add(refOtherSet);
								}
								serviceParameterList
										.add((Serializable) (((AbstractGridAnalysis) selectedAnalysis)
												.getBisonParameters()));

								DispatcherClient dispatcherClient = new DispatcherClient(
										dispatcherUrl);

								GridEndpointReferenceType gridEpr = dispatcherClient
										.submit(
												serviceParameterList,
												url,
												((AbstractGridAnalysis) selectedAnalysis)
														.getBisonReturnType());

								ProjectNodePendingEvent pendingEvent = new ProjectNodePendingEvent(
										"Analysis Pending", gridEpr);
								pendingEvent.setDescription(selectedAnalysis
										.getLabel()
										+ " (pending)");
								log.info("event is " + pendingEvent);

								publishProjectNodePendingEvent(pendingEvent);

								PollingThread pollingThread = new PollingThread(
										getAnalysisPanel(), gridEpr,
										dispatcherClient);
								pollingThread.start();

							} else {
								log.error("Cannot execute with url:  " + url);
							}
						} else {
							if ((maSetView != null) && (refMASet != null)) {
								results = selectedAnalysis.execute(maSetView);
							}else if (refOtherSet != null) { // added for analysis
														// that do not take in
														// microarray data set
								results = selectedAnalysis.execute(refOtherSet);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						analyze.setEnabled(true);
					}
					// FIXME don't use this check - remove projectNodeEvent from
					// executeGridAnalysis first
					if (!isGridAnalysis()) {
						analysisDone();
					} else {
						pBar.stop();
					}
				}

			});
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		}
	}

	@Publish
	public org.geworkbench.events.SubpanelChangedEvent publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent event) {
		return event;
	}

	/**
	 * Post analysis steps to check if analysis terminated properly and then to
	 * fire the appropriate application event
	 */
	private void analysisDone() {
		// If everything was OK construct and fire the proper application-level
		// event, thus notify interested application components of
		// the results of the analysis operation.
		// If there were problems encountered, let the user know.
		if (results != null) {
			if (!results.isExecutionSuccessful()) {
				JOptionPane.showMessageDialog(null, results.getMessage(),
						"Analysis Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			Object resultObject = results.getResults();
			if (resultObject instanceof DSAncillaryDataSet) {
				DSAncillaryDataSet dataSet = (DSAncillaryDataSet) resultObject;
				final ProjectNodeAddedEvent event = new ProjectNodeAddedEvent(
						"Analysis Result", null, dataSet);
				// SwingUtilities.invokeLater(new Runnable() {
				// public void run() {
				publishProjectNodeAddedEvent(event);
				// }
				// });
				return;
			}
			if (resultObject instanceof Hashtable) {
				DSPanel<DSGeneMarker> panel = (DSPanel) ((Hashtable) resultObject)
						.get("Significant Genes");
				if (panel != null) {
					publishSubpanelChangedEvent(new org.geworkbench.events.SubpanelChangedEvent(
							DSGeneMarker.class, panel, SubpanelChangedEvent.NEW));
				}
			}
		}
	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	@Publish
	public ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			ProjectNodeAddedEvent event) {
		return event;
	}

	@Publish
	public AnalysisInvokedEvent publishAnalysisInvokedEvent(
			AnalysisInvokedEvent event) {
		return event;
	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	@Publish
	public ProjectNodePendingEvent publishProjectNodePendingEvent(
			ProjectNodePendingEvent event) {
		return event;
	}

	/**
	 * 
	 * @param pendingEvent
	 * @return
	 */
	@Publish
	public ProjectNodeCompletedEvent publishProjectNodeCompletedEvent(
			ProjectNodeCompletedEvent event) {
		log.info("publishing project node completed event");
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
			dispatcherClient = new DispatcherClient(dispatcherUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Collection<GridEndpointReferenceType> gridEprs = ppne.getGridEprs();

		for (GridEndpointReferenceType gridEpr : gridEprs) {

			PollingThread pollingThread = new PollingThread(getAnalysisPanel(),
					gridEpr, dispatcherClient);
			pollingThread.start();

		}

	}

	/**
	 * Listener invoked when the "Save Settings" button is pressed
	 * 
	 * @param e
	 *            <code>ActionEvent</code> generated by "save" button
	 */
	private void save_actionPerformed(ActionEvent e) {
		// Bring up a pop-up window for the user to enter the named to use.
		// If the currently dispayed parameter already has a name associated
		// with it, use that name in the pop-up, otherwise show somwthing like
		// "New Parameter Setting Name".
		int index = namedParameters.getSelectedIndex();
		String namedParameter = null;
		if (index != -1) {
			namedParameter = (String) namedParameters.getModel().getElementAt(
					index);
			if (currentParameterPanel.isDirty()) {
				namedParameter = "New Parameter Setting Name";
			}
		} else {
			namedParameter = "New Parameter Setting Name";
		}
		String paramName = JOptionPane.showInputDialog(analysisPanel,
				namedParameter, namedParameter);
		if (selectedAnalysis != null && paramName != null) {
			selectedAnalysis.saveParametersUnderName(paramName);
			setNamedParameters(selectedAnalysis.getNamesOfStoredParameterSets());
		}
	}

	/**
	 * Listener invoked when the "Delete Settings" button is pressed
	 * 
	 * @param e -
	 *            action event
	 */
	private void delete_actionPerformed(ActionEvent e) {
		int choice = JOptionPane.showConfirmDialog(null,
				"Are you sure you want to delete saved parameters?",
				"Deleting Saved Parameters", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if ((selectedAnalysis != null) && (choice == 0)
				&& (this.namedParameters.getSelectedIndex() >= 0)) {
			log.info("Deleting saved parameters: "
					+ (String) this.namedParameters.getSelectedValue());
			this.removeNamedParameter((String) this.namedParameters
					.getSelectedValue());
			this.delete.setEnabled(false);
		}
	}

	/**
	 * 
	 * @return boolean
	 */
	private boolean isGridAnalysis() {
		//TODO: use this to check if it's a grid analysis or not is wrong. it cause bug 0001174 in Mantis. Quick fix made user input service information every time. Should have a better implementation. 
		if (jGridServicePanel != null) {
			ButtonGroup gridSelectionButtonGroup = jGridServicePanel
					.getButtonGroup();

			ButtonModel bm = gridSelectionButtonGroup.getSelection();
			if (StringUtils.equals(bm.getActionCommand(), "Grid")) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @return String
	 */
	private String getServiceUrl() {
		ButtonGroup bg = jGridServicePanel.getServicesButtonGroup();

		ButtonModel bm = bg.getSelection();

		if (bm == null) {
			return null;
		}

		String url = bm.getActionCommand();
		return url;
	}
}
