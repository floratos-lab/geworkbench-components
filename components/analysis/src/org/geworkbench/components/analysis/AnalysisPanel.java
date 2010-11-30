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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.axis.types.URI.MalformedURIException;
import org.apache.commons.lang.StringUtils;
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
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSExpressionMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.structure.CSProteinStructure;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
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
import org.geworkbench.components.cagrid.gui.GridServicePanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.ComponentRegistry;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.events.AnalysisInvokedEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;
import org.geworkbench.util.microarrayutils.MicroarrayViewEventBase;
import org.geworkbench.util.pathwaydecoder.mutualinformation.EdgeListDataSet;
import org.ginkgo.labs.util.FileTools;
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
public class AnalysisPanel extends MicroarrayViewEventBase implements
		VisualPlugin, ReHighlightable {

	private static Log log = LogFactory.getLog(AnalysisPanel.class);

	/* static variables */
	private static final String DEFAULT_PARAMETER_SETTING_NAME = "New Parameter Setting Name";
	private static final String SERVICE = "Service";
	private static final String PARAMETERS = "Parameters";
	private static final String USER_INFO = "userinfo";
	private static final int ANALYSIS_TAB_COUNT = 1;
	private static final String USER_INFO_DELIMIETER = "==";

	/* from application.properties */
	final static String DISPATCHER_URL = "dispatcher.url";

	/* from PropertiesManager (user preference) */
	private static final String GRID_HOST_KEY = "dispatcherURL";

	private String dispatcherUrl = System.getProperty(DISPATCHER_URL);

	private String userInfo = null;

	/* user interface */
	private JPanel analysisPanel = null;

	private JScrollPane analysisScrollPane = null;

	private JPanel innerAnalysisPanel = null;

	private BorderLayout analysisPanelBorderLayout = null;

	private BorderLayout borderLayout3 = null;

	private ParameterPanel emptyParameterPanel = null;

	private JPanel analysisMainPanel = null;

	private JPanel selectedAnalysisParameterPanel = null;

	private ParameterPanel currentParameterPanel = null;

	private BorderLayout borderLayout4 = null;

	private BorderLayout borderLayout5 = null;

	private JPanel parameterPanel = null;

	private JButton analyze = null;

	private JPanel jPanel1 = null;

	private JTabbedPane jAnalysisTabbedPane = null;

	private GridServicePanel jGridServicePanel = null;

	private JButton save = null;
	private JButton delete = null;

	// threads to check submitted caGrid service jobs
	private List<Thread> threadList = new ArrayList<Thread>();

	/*
	 * Contains the pluggable clustering analysis available to the user to
	 * choose from. These analyses will have been defined in the application
	 * configuration file as <code>plugin</code> components and they are
	 * expected to have been associated with the extension point <code>clustering</code>.
	 */
	protected AbstractAnalysis[] availableAnalyses = null;
	protected AbstractAnalysis selectedAnalysis = null;

	private JComboBox analysisComboBox = new JComboBox();
	private JComboBox parameterComboBox = new JComboBox();

	/*
	 * Results obtained from execution of an analysis. This is an instance
	 * variable as the analysis is carried out on a worker thread.
	 */
	private AlgorithmExecutionResults results = null;

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
		if (currentDataType == null) return;
		else {
			log.error("I don't see how this can happen");
		if (currentDataType.equals(CSProteinStructure.class)) {
			getAvailableAnalyses(ProteinStructureAnalysis.class);
		} else if (currentDataType.equals(CSSequenceSet.class)) {
			getAvailableAnalyses(ProteinSequenceAnalysis.class);
		} else {
			getAvailableAnalyses(ClusteringAnalysis.class);
		}
		displayAnalyses();
		}
	}

	/**
	 * 
	 * @return Return the Analysis Panel
	 */
	public AnalysisPanel getAnalysisPanel() {
		return this;
	}

	/**
	 * initialize GUI
	 * 
	 * @throws Exception
	 *             exception thrown during GUI construction
	 */
	private void init() throws Exception {
		analysisPanel = new JPanel();
		analysisScrollPane = new JScrollPane();
		innerAnalysisPanel = new JPanel();
		analysisPanelBorderLayout = new BorderLayout();
		borderLayout3 = new BorderLayout();
		emptyParameterPanel = new ParameterPanel();
		analysisMainPanel = new JPanel();
		save = new JButton("Save Settings");
		delete = new JButton("Delete Settings");
		selectedAnalysisParameterPanel = new JPanel();
		currentParameterPanel = emptyParameterPanel;
		borderLayout4 = new BorderLayout();
		borderLayout5 = new BorderLayout();
		parameterPanel = new JPanel();

		analyze = new JButton("Analyze");
		/* Double it's width */
		Dimension d = analyze.getPreferredSize();
		d.setSize(d.getWidth() * 2, d.getHeight());
		analyze.setPreferredSize(d);

		jPanel1 = new JPanel();

		analysisPanel.setLayout(analysisPanelBorderLayout);
		innerAnalysisPanel.setLayout(borderLayout3);

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

		selectedAnalysisParameterPanel.setLayout(borderLayout4);
		currentParameterPanel.setLayout(borderLayout5);

		parameterPanel.setLayout(new BorderLayout());
		analyze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				analyze_actionPerformed(e);
			}

		});
		analysisMainPanel.setLayout(new BoxLayout(analysisMainPanel, BoxLayout.Y_AXIS));

		parameterComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				namedParameterSelection_action(e);
			}
		});
		parameterComboBox.setAutoscrolls(true);

		analysisComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				analysisSelected_action(e);
			}
			
		});
		analysisPanel.add(analysisScrollPane, BorderLayout.CENTER);
		analysisScrollPane.getViewport().add(innerAnalysisPanel, null);
		innerAnalysisPanel.add(analysisMainPanel, BorderLayout.CENTER);

		selectedAnalysisParameterPanel.add(currentParameterPanel,
				BorderLayout.CENTER);

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

		analysisMainPanel.add(jPanel1);
		jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.LINE_AXIS));
		jPanel1.add(Box.createRigidArea(new Dimension(5, 0)));
		jPanel1.add(new JLabel("Analysis"));
		jPanel1.add(Box.createRigidArea(new Dimension(5, 0)));
		jPanel1.add(analysisComboBox, null);
		jPanel1.add(Box.createRigidArea(new Dimension(50, 0)));
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
	 * Post analysis steps to check if analysis terminated properly and then to
	 * fire the appropriate application event
	 */
	@SuppressWarnings("unchecked")
	private void analysisDone() {
		if (results == null) {
			log.error("unexpected null result");
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
			return;
		}
		Object resultObject = results.getResults();
		if (resultObject instanceof DSAncillaryDataSet) {
			DSAncillaryDataSet<DSBioObject> dataSet = (DSAncillaryDataSet<DSBioObject>) resultObject;
			final ProjectNodeAddedEvent event = new ProjectNodeAddedEvent(
					"Analysis Result", null, dataSet);
			publishProjectNodeAddedEvent(event);
			return;
		}
		if (resultObject instanceof Hashtable) {
			DSPanel<DSGeneMarker> panel = ((Hashtable<?, DSPanel<DSGeneMarker>>) resultObject)
					.get("Significant Genes");
			if (panel != null) {
				publishSubpanelChangedEvent(new org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker>(
						DSGeneMarker.class, panel, SubpanelChangedEvent.NEW));
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

			PollingThread pollingThread = new PollingThread(gridEpr,
					dispatcherClient);
			threadList.add(pollingThread);
			pollingThread.start();

		}

	}

	/**
	 * 
	 * @param maSetView
	 * @return
	 */
	private String generateHistoryString(DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView) {
		StringBuilder ans = new StringBuilder("=The MicroarraySetView used for analysis contains following data=");
		/* Generate text for microarrays/groups */
		ans .append( FileTools.NEWLINE );
		try {
			log.debug("We got a " + maSetView.items().getClass().toString());
			if (maSetView.items().getClass() == CSPanel.class) {
				log.debug("situation 1: microarraySets selected");
				DSItemList<DSPanel<DSMicroarray>> paneltest = ((DSPanel<DSMicroarray>) maSetView.items()).panels();

				ans .append( "==Microarray Sets [" ).append( paneltest.size() + "]==" ).append(
						 FileTools.NEWLINE );
				for(DSPanel<DSMicroarray> temp: paneltest) {
					ans .append( FileTools.TAB + temp.toString() ).append( FileTools.NEWLINE );
					for (DSMicroarray temp2: temp) {
						ans .append( FileTools.TAB ).append( FileTools.TAB ).append( temp2.toString() )
								.append( FileTools.NEWLINE );
					}
				}
			} else if (maSetView.items().getClass() == CSExprMicroarraySet.class) {
				log.debug("situation 2: microarraySets not selected");
				CSExprMicroarraySet exprSet = (CSExprMicroarraySet) maSetView
						.items();
				ans .append( "==Used Microarrays [" ).append( exprSet.size() ).append( "]==" )
						.append( FileTools.NEWLINE );
				for (Iterator<DSMicroarray> iterator = exprSet.iterator(); iterator
						.hasNext();) {
					DSMicroarray array = iterator.next();
					ans .append( FileTools.TAB ).append( array.getLabel() ).append( FileTools.NEWLINE );
				}
			}
			ans .append( "==End of Microarray Sets==" ).append( FileTools.NEWLINE );
			/* Generate text for markers */
			DSPanel<DSGeneMarker> paneltest = maSetView.getMarkerPanel();
			if ((paneltest != null) && (paneltest.size() > 0)) {
				log.debug("situation 3: markers selected");

				ans .append( "==Used Markers [" ).append( paneltest.size() + "]==" )
						.append( FileTools.NEWLINE );
				for (DSGeneMarker obj: paneltest) {
					CSExpressionMarker temp = (CSExpressionMarker) obj;
					ans .append( FileTools.TAB ).append( temp.getLabel() ).append( FileTools.NEWLINE );
				}
			} else {
				log.debug("situation 4: no markers selected.");
				DSItemList<DSGeneMarker> markers = maSetView.markers();
				ans .append( "==Used Markers [" ).append( markers.size() ).append( "]==" )
						.append( FileTools.NEWLINE );
				for (DSGeneMarker marker : markers) {
					ans .append( FileTools.TAB ).append( marker.getLabel() )
							.append( FileTools.NEWLINE );
				}
			}
			ans .append( "==End of Used Markers==" ).append( FileTools.NEWLINE );
		} catch (ClassCastException cce) {
			/* it's not a DSPanel, we generate nothing for panel part */
			log.error(cce);
		}
		ans .append( "=End of MicroarraySetView data=");
		return ans.toString();
	}

	@SuppressWarnings("rawtypes")
	private String generateHistoryStringForGeneralDataSet(DSDataSet dataset) {
		if (dataset == null) {
			return "No information on the data set." + FileTools.NEWLINE;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("The data set used for analysis is [ ");
			sb.append(dataset.getDataSetName());
			sb.append(" ] from file [ ");
			sb.append(dataset.getPath());
			sb.append(" ]." + FileTools.NEWLINE);
			return sb.toString();
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
	 * Displays the list of available analyses.
	 */
	private void displayAnalyses() {
		/* Clean the list */
		analysisComboBox.removeAllItems();

		/* Get the display names of the available analyses. */
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

		String selectedAnalysisName = null;
		if(selectedAnalysis!=null) {
			selectedAnalysisName = selectedAnalysis.getLabel();
		}
		/* Show graphical components */
		// populate the combo box
		analysisComboBox.setModel(new DefaultComboBoxModel(names));

		// use name to restore selectedAnalysis because addItem de-selected combo box, and thus de-select the analysis 
		if (selectedAnalysisName != null) {
			for(int i=0; i<analysisComboBox.getItemCount(); i++) {
				if(selectedAnalysisName.equals(analysisComboBox.getItemAt(i))) {
					analysisComboBox.setSelectedIndex(i);
					break;
				}
			}
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
			JOptionPane.showMessageDialog(null, "ParameterSet already exist.",
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
			if (selectedAnalysis != null && paramName != null) {

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
					"You have to select a setting before you can delete it.",
					"Canceled", JOptionPane.OK_OPTION);
		} else {
			int choice = JOptionPane.showConfirmDialog(null,
					"Are you sure you want to delete saved parameters?",
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

	private static final int DEFAULT_SELECTED_INDEX = -1;

	/**
	 * Listener invoked when an analysis is selected from the combo box of
	 * analyses. The parameters for this analysis are shown.
	 * 
	 * @param action evene
	 */
	private void analysisSelected_action(ActionEvent actionEvent) {
		if (analysisComboBox.getSelectedIndex() == -1) {
			return;
		}
		delete.setEnabled(false);

		int index = analysisComboBox.getSelectedIndex();
		selectedAnalysis = availableAnalyses[index];

		/* Set the parameters panel for the selected analysis. */
		ParameterPanel paramPanel = selectedAnalysis.getParameterPanel();
		if (paramPanel != null) {
			String[] storedParameterSetNames = availableAnalyses[index]
					.getNamesOfStoredParameterSets();
			setNamedParameters(storedParameterSetNames);
			setParametersPanel(paramPanel);

			String className = paramPanel.getClass().getName();
			if (className.equals("org.geworkbench.components.ttest.MultiTTestAnalysisPanel")
					|| className.equals("org.geworkbench.components.ttest.TtestAnalysis"))
				super.chkAllArrays.setVisible(false);
			else
				super.chkAllArrays.setVisible(true);

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
			setParametersPanel(this.emptyParameterPanel);
			save.setEnabled(false);
			/*
			 * Since the analysis admits no parameters, there are no named
			 * parametersettings to show.
			 */
			setNamedParameters(new String[0]);
		}

		if (selectedAnalysis instanceof AbstractGridAnalysis) {
			if (analysisComboBox.getSelectedIndex() != pidMap.get(currentDataType)) {
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
		pidMap.put(currentDataType, analysisComboBox.getSelectedIndex());
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
	 */
	@SuppressWarnings("unchecked")
	private void analyze_actionPerformed(ActionEvent e) {
		maSetView = getDataSetView();

		boolean onlyActivatedArrays = false;
		if (currentParameterPanel instanceof ParameterPanelIncludingNormalized) {
			onlyActivatedArrays = true;

			ParameterPanelIncludingNormalized p = (ParameterPanelIncludingNormalized)currentParameterPanel;
			Boolean isLogNormalized = p.isLogNormalized();

			Boolean isLogNormalizedFromGuess = guessLogNormalized(maSetView);

			if (isLogNormalizedFromGuess != null
					&& isLogNormalizedFromGuess.booleanValue() != isLogNormalized
							.booleanValue()) {
				String theMessage = "We have detected that the checkbox 'Data is log2-tranformed' may not be correctly set. Do you want to proceed anyway?";
				int result = JOptionPane.showConfirmDialog((Component) null,
						theMessage, "alert", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.NO_OPTION)
					return;
			}
		} else {
			onlyActivatedArrays = !chkAllArrays.isSelected();
		}

		if (selectedAnalysis == null
				|| ((refMASet == null) && (refOtherSet == null))) {
			return;
		}

		if (refOtherSet != null) { /*
									 * added for analysis that do not take in
									 * microarray data set
									 */

			AnalysisInvokedEvent event = new AnalysisInvokedEvent(
					selectedAnalysis, "");
			publishAnalysisInvokedEvent(event);
		} else if ((maSetView != null) && (refMASet != null)) {
			AnalysisInvokedEvent event = new AnalysisInvokedEvent(
					selectedAnalysis, maSetView.getDataSet().getLabel());
			publishAnalysisInvokedEvent(event);
		}

		ParamValidationResults pvr = selectedAnalysis.validateParameters();
		if (!pvr.isValid()) {
			JOptionPane.showMessageDialog(null, pvr.getMessage(),
					"Parameter Validation Error", JOptionPane.ERROR_MESSAGE);
		} else {
			analyze.setEnabled(false);
			maSetView.useMarkerPanel(!chkAllMarkers.isSelected());
			maSetView.useItemPanel(onlyActivatedArrays);
			Thread t = new Thread(new Runnable() {
				public void run() {
					/* check if we are dealing with a grid analysis */
					if (isGridAnalysis()) {
						submitAsCaGridService();
					} else {
						executeLocally();
					}
					analyze.setEnabled(true);
				}

			});
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		}
	}

	private void submitAsCaGridService() {

		AbstractGridAnalysis selectedGridAnalysis = (AbstractGridAnalysis) selectedAnalysis;

		ParamValidationResults validResult = ((AbstractGridAnalysis) selectedAnalysis)
				.validInputData(maSetView, refMASet);
		if (!validResult.isValid()) {
			JOptionPane.showMessageDialog(null, validResult
					.getMessage(), "Invalid Input Data",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (selectedGridAnalysis.isAuthorizationRequired()) {
			/* ask for username and password */
			getUserInfo();
			if (userInfo == null) {
				JOptionPane
						.showMessageDialog(
								null,
								"Please make sure you entered valid username and password",
								"Invalid User Account",
								JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (StringUtils.isEmpty(userInfo)) {
				userInfo = null;
				return;
			}
		}

		String url = getServiceUrl();
		if (StringUtils.isEmpty(url)) {
			log.error("Cannot execute with url:  " + url);
			JOptionPane.showMessageDialog(null,
					"Cannot execute grid analysis: Invalid URL "+url+" specified.",
					"Invalid grid URL Error", JOptionPane.ERROR_MESSAGE);
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
			return;
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		} finally {
			pBar.stop();
		}

		/* generate history for grid analysis */
		String history = "";
		history += "Grid service information:" + FileTools.NEWLINE;
		history += FileTools.TAB + "Index server url: "
				+ jGridServicePanel.getIndexServerUrl() + FileTools.NEWLINE;
		history += FileTools.TAB + "Dispatcher url: " + dispatcherUrl
				+ FileTools.NEWLINE;
		history += FileTools.TAB + "Service url: " + url + FileTools.NEWLINE
				+ FileTools.NEWLINE;
		history += selectedAnalysis.createHistory();
		if (refOtherSet != null) {
			history += generateHistoryStringForGeneralDataSet(refOtherSet);
		} else if (maSetView != null && refMASet != null) {
			history += generateHistoryString(maSetView);
		}

		ProjectPanel.getInstance().addPendingNode(gridEpr,
				selectedGridAnalysis.getLabel() + " (pending)", history, false);

		PollingThread pollingThread = new PollingThread(gridEpr,
				dispatcherClient);
		threadList.add(pollingThread);
		pollingThread.start();
	}

	private void executeLocally() {
		if (refOtherSet != null) {
			// first case: analysis that does not take in microarray data set
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
					return;
				}
			}
			results = selectedAnalysis.execute(maSetView);
		}
		analysisDone();
	}
	
	private Class<?> currentDataType = null, lastDataType = null;
	private HashMap<Class<?>, Integer> pidMap = new HashMap<Class<?>, Integer>();

	@SuppressWarnings("rawtypes")
	private DSDataSet refOtherSet = null;
	/**
	 * Refresh the list of available analyses.
	 */
	@SuppressWarnings("rawtypes")
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent even, Object source) {
		super.receive(even, source);
		DSDataSet dataSet = even.getDataSet();
		if ( dataSet instanceof DSMicroarraySet ) {
			refOtherSet = null;
		} else {
			refOtherSet = dataSet;
		}
		if (even.getDataSet() != null && even.getParent() == null) {
			lastDataType = currentDataType;
			currentDataType = even.getDataSet().getClass();
			if (!pidMap.containsKey(currentDataType) || lastDataType != currentDataType)
				pidMap.put(currentDataType, DEFAULT_SELECTED_INDEX);
			if (even.getDataSet().getClass().equals(CSProteinStructure.class)) {
				getAvailableAnalyses(ProteinStructureAnalysis.class);
			} else if (even.getDataSet().getClass().equals(CSSequenceSet.class)) {
				getAvailableAnalyses(ProteinSequenceAnalysis.class);
			} else {
				getAvailableAnalyses(ClusteringAnalysis.class);
			}
			displayAnalyses();
		}
	}

	/**
	 * Get Analysis of given type.
	 */
	private void getAvailableAnalyses(Class<? extends Analysis> analysisType) {
		boolean selectionChanged = true;
		Analysis[] analyses = ComponentRegistry.getRegistry().getModules(analysisType);
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
		
		String[] names = new String[availableAnalyses.length];
		for (int i = 0; i < availableAnalyses.length; i++) {
			names[i] = ComponentRegistry.getRegistry().getDescriptorForPlugin(
					availableAnalyses[i]).getLabel();
			availableAnalyses[i].setLabel(names[i]);
		}

		AbstractAnalysisLabelComparator comparator = new AbstractAnalysisLabelComparator();
		Arrays.sort(availableAnalyses, comparator);
	}
	
	private Boolean guessLogNormalized(DSMicroarraySetView<DSGeneMarker, DSMicroarray> data) {
		if (data == null) {
			return null;
		}
		
		Boolean isLogNormalized = null;
		try {
			DSDataSet<DSMicroarray> set = data.getDataSet();
			if (set instanceof DSMicroarraySet) {
				DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet<DSMicroarray>) set;
				double minValue = Double.POSITIVE_INFINITY;
				double maxValue = Double.NEGATIVE_INFINITY;
				for (DSMicroarray microarray : maSet) {
					DSMutableMarkerValue[] values = microarray
							.getMarkerValues();
					double v;
					for (DSMutableMarkerValue value : values) {
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

}