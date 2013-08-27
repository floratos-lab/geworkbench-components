package org.geworkbench.components.normalization;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractAnalysisLabelComparator;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.analysis.HighlightCurrentParameterThread;
import org.geworkbench.analysis.ParameterKey;
import org.geworkbench.analysis.ReHighlightable;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.NormalizingAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.ComponentRegistry;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.AnalysisInvokedEvent;
import org.geworkbench.util.CommandBase;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: First Genetic Trust Inc.
 * </p>
 * 
 * Application component offering users a selection of microarray normalization
 * options.
 * 
 * @author First Genetic Trust, keshav, yc2480
 * @version $Id$
 */
@AcceptTypes( { DSMicroarraySet.class })
public class NormalizationPanel extends CommandBase implements VisualPlugin, ReHighlightable {
	private Log log = LogFactory.getLog(this.getClass());
	/**
	 * The underlying panel for the normalization component
	 */
	protected JPanel normalizationPanel = new JPanel();
	private JPanel mainPanel = new JPanel();

	/**
	 * The currently selected microarray set.
	 */
	protected DSMicroarraySet maSet = null;
	/**
	 * The most recently used normalizer.
	 */
	protected AbstractAnalysis selectedNormalizer = null;

	private JComboBox namedParameters = new JComboBox();
	
	BorderLayout borderLayout1 = new BorderLayout();
	JScrollPane jScrollPane2 = new JScrollPane();
	JPanel jPanel3 = new JPanel();
	JPanel jPanelControl = new JPanel();
	BorderLayout borderLayout2 = new BorderLayout();
	BorderLayout borderLayout3 = new BorderLayout();
	GridLayout gridLayout1 = new GridLayout();
	GridLayout gridLayout2 = new GridLayout();
	JButton analyze = new JButton("Normalize");
	JButton save = new JButton("Save Settings");
	JButton delete = new JButton("Delete Settings");
	JPanel jPanel4 = new JPanel();
	FlowLayout flowLayout1 = new FlowLayout();
	ParameterPanel emptyParameterPanel = new ParameterPanel();
	ParameterPanel currentParameterPanel = emptyParameterPanel;
	BorderLayout borderLayout4 = new BorderLayout();
	BorderLayout borderLayout5 = new BorderLayout();
	BorderLayout borderLayout6 = new BorderLayout();
	JPanel jPanel1 = new JPanel();

	/**
	 * Default Constructor
	 */
	public NormalizationPanel() {
		super("Normalization");
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * Get the available normalizers from the
		 * ComponentRegistry
		 */
		getAvailableNormalizers();
	}

	/**
	 * Implementation of method from interface <code>VisualPlugin</code>.
	 * 
	 * @return
	 */
	public Component getComponent() {
		return normalizationPanel;
	}

	/*
	 * 
	 */
	private void jbInit() throws Exception {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		jPanel4.setLayout(borderLayout5);
		normalizationPanel.setLayout(borderLayout2);
		jPanel3.setLayout(borderLayout3);
		jPanelControl.setLayout(borderLayout4);
		gridLayout1.setColumns(2);
		gridLayout1.setRows(3);
		gridLayout1.setVgap(0);
		gridLayout2.setColumns(4);
		gridLayout2.setRows(3);
		analyze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				normalization_actionPerformed(e);
				hideDialog();
			}

		});
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
		jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.LINE_AXIS));

		namedParameters.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				namedParameterSelection_action(e);
			}

		});
		namedParameters.setAutoscrolls(true);
		normalizationPanel.add(jScrollPane2, BorderLayout.CENTER);
		jScrollPane2.getViewport().add(jPanel3, null);
		jPanel3.add(mainPanel, BorderLayout.CENTER);
		currentParameterPanel.setLayout(borderLayout6);
		jPanelControl.add(jPanel4, BorderLayout.WEST);
		jPanel4.add(currentParameterPanel, BorderLayout.CENTER);

		/* Add buttons */
		analyze.setPreferredSize(delete.getPreferredSize());
		save.setPreferredSize(delete.getPreferredSize());
		delete.setEnabled(false);

		FormLayout layout = new FormLayout("right:100dlu,10dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Normalization Actions");
		builder.append(analyze);
		builder.nextLine();
		builder.append(save);
		builder.nextLine();
		builder.append(delete);

		jPanelControl.add(builder.getPanel(), BorderLayout.EAST);

		mainPanel.add(jPanel1);
		jPanel1.add(Box.createRigidArea(new Dimension(5, 0)));
		jPanel1.add(new JLabel("Normalizer"));
		jPanel1.add(Box.createRigidArea(new Dimension(5, 0)));
		jPanel1.add(new JLabel("Saved Parameters"));
		jPanel1.add(Box.createRigidArea(new Dimension(5, 0)));
		jPanel1.add(namedParameters, null);

		mainPanel.add(jPanelControl);
	}

	/**
	 * Listener invoked when the "Delete Settings" button is pressed
	 * 
	 * @param e
	 */
	private void delete_actionPerformed(ActionEvent e) {
		int choice = JOptionPane.showConfirmDialog(null,
				"Are you sure you want to delete saved parameters?",
				"Deleting Saved Parameters", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if ((selectedNormalizer != null) && (choice == 0)
				&& (namedParameters.getSelectedIndex() > 0)) {
			log.info("Deleting saved parameters: "
					+ (String) namedParameters.getSelectedItem());
			this.removeNamedParameter((String) namedParameters
					.getSelectedItem());
			if (namedParameters.getItemCount() <= 1)
				delete.setEnabled(false);
		}
	}
	
	/**
	 * Delete the selected saved parameter.
	 * 
	 * @param name -
	 *            name of the saved parameter
	 */
	private void removeNamedParameter(String name) {
		selectedNormalizer.removeNamedParameter(name);
		this.setNamedParameters(selectedNormalizer
				.getNamesOfStoredParameterSets());
	}


	/**
	 * Implementation of method from interface <code>ProjectListener</code>.
	 * Handles notifications about change of the currently selected microarray
	 * set.
	 * 
	 * @param pe
	 *            Project event containing the newly selected microarray set.
	 */
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent pe, Object source) {
		DSDataSet<?> dataSet = pe.getDataSet();
		if (dataSet != null) clearMenuItems();
		if (dataSet != null && dataSet instanceof DSMicroarraySet && !pendingNodeSelected()) {
			maSet = (DSMicroarraySet) dataSet;
			getAvailableNormalizers();
			updateMenuItems();
		}
	}

	@SuppressWarnings("rawtypes")
	@Subscribe
	public void receive(org.geworkbench.events.CCMUpdateEvent pe, Object source) {
		Class<? extends DSDataSet> dataSetType = pe.getDataSetType();
		if (dataSetType != null && DSMicroarraySet.class.isAssignableFrom(dataSetType) && !pendingNodeSelected()) {
			clearMenuItems();
			getAvailableNormalizers();
			updateMenuItems();
		}
	}
	
	/**
	 * Queries the extension point <code>normalizers</code> within the
	 * <code>PluginRegistry </code> for available normalizer-type plugins.
	 * 
	 * This method gets invoked every time that the normalization panel gets the
	 * focus, in order to get the most recent list of normalizers: given dynamic
	 * loading of components this approach guarantees that any new plugins
	 * loaded between uses of the normalization panel, will be correctly picked
	 * up.
	 */
	private void getAvailableNormalizers() {
		/* To check if the last used normalizer is still available. */
		boolean selectionChanged = true;
		/*
		 * Populate 'availableNormalizers[]' from ComponentRegistry.
		 */
		NormalizingAnalysis[] analyses = ComponentRegistry.getRegistry()
				.getModules(NormalizingAnalysis.class);
		availableCommands = new AbstractAnalysis[analyses.length];
		for (int i = 0; i < analyses.length; i++) {
			availableCommands[i] = (AbstractAnalysis) analyses[i];
			if (selectedNormalizer == availableCommands[i])
				selectionChanged = false;
		}

		/*
		 * If the selectedNormalizer has been removed from the list of available
		 * normalizers, reset.
		 */
		if (selectionChanged)
			if (analyses.length > 0)
				selectedNormalizer = availableCommands[0];
			else
				selectedNormalizer = null;
		
		AbstractAnalysisLabelComparator comparator = new AbstractAnalysisLabelComparator();
		Arrays.sort(availableCommands, comparator );
	}

	/**
	 * Set the parameters panel used in the normalization pane.
	 * 
	 * @param parameterPanel
	 */
	private void setParametersPanel(ParameterPanel parameterPanel) {
		jPanel4.remove(currentParameterPanel);
		currentParameterPanel = parameterPanel;
		jPanel4.add(currentParameterPanel, BorderLayout.CENTER);
		normalizationPanel.revalidate();
		normalizationPanel.repaint();
		/* Set the call back function for list highlighting. */
		if (currentParameterPanel instanceof AbstractSaveableParameterPanel)
			((AbstractSaveableParameterPanel) currentParameterPanel)
					.setParameterHighlightCallback(new HighlightCurrentParameterThread(
							this));
	}

	/**
	 * Update the list that shows the known preset parameter settings for the
	 * selected normalizer.
	 * 
	 * @param storedParameters
	 *            Parameter names you want to shown in the parameter set list UI
	 */
	private void setNamedParameters(String[] storedParameters) {
		namedParameters.removeAllItems();
		namedParameters.addItem("");
		for(String n: storedParameters)
			namedParameters.addItem(n);
		normalizationPanel.revalidate();
		highlightCurrentParameterGroup();
	}

	/**
	 * we'll need a flag to stop cycle events. eg: select set will change GUI,
	 * change GUI will refresh highlight.
	 */
	private static boolean calledFromProgram = false;

	/**
	 * scan the saved list, check each parameter set, see if the parameters in
	 * it are the same as in current parameter panel, if yes, highlight it.
	 */
	private void highlightCurrentParameterGroup() {
		if (!calledFromProgram) {
			calledFromProgram = true;
			ParameterPanel currentParameterPanel = selectedNormalizer
					.getParameterPanel();
			String[] parametersNameList = selectedNormalizer
					.getNamesOfStoredParameterSets();
			namedParameters.setSelectedIndex(0);
			for (int i = 0; i < parametersNameList.length; i++) {
				Map<Serializable, Serializable> parameter1 = ((AbstractSaveableParameterPanel) currentParameterPanel)
						.getParameters();
				Map<Serializable, Serializable> parameter2 = new HashMap<Serializable, Serializable>();
				parameter2.putAll(selectedNormalizer
						.getNamedParameterSet(parametersNameList[i]));
				parameter2.remove(ParameterKey.class.getSimpleName());
				if (parameter1.equals(parameter2)) {
					namedParameters.setSelectedIndex(i+1);
					break;
				}
			}
			calledFromProgram = false;
		}
	}

	/**
	 * Implement <code>ReHighlightable</code>, this method will be called by
	 * call back function.
	 * 
	 * When this method been called, the normalization panel will highlight the
	 * parameter group which contains same parameters as in current parameter
	 * panel.
	 */
	public void refreshHighLight() {
		highlightCurrentParameterGroup();
	}

	protected void setSelectedCommandByName(String commandName) {

		delete.setEnabled(false);

		selectedNormalizer = getCommandByName(commandName);
		/* Set the parameters panel for the selected normalizer. */
		ParameterPanel paramPanel = selectedNormalizer.getParameterPanel();
		/*
		 * Set the list of available named parameters for the selected
		 * normalizer.
		 */
		if ((paramPanel != null)
				&& ((paramPanel instanceof AbstractSaveableParameterPanel) && (paramPanel
						.getClass() != AbstractSaveableParameterPanel.class))) {
			String[] storedParameterSetNames = selectedNormalizer
					.getNamesOfStoredParameterSets();
			setNamedParameters(storedParameterSetNames);
			setParametersPanel(paramPanel);
			/*
			 * If it's first time (means just after load from file) for this
			 * normalizer, assign last saved parameters to current normalization
			 * panel and highlight last saved group.
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
			/*
			 * Since the normalizer admits no parameters, there are no named
			 * parameter settings to show.
			 */
			setParametersPanel(this.emptyParameterPanel);
			setNamedParameters(new String[0]);

			save.setEnabled(false);
		}

	}

	/**
	 * This method is used for select and highlight the last saved parameter
	 * set.
	 * 
	 * This method will only be called once, when a normalizer been selected.
	 */
	private void selectLastSavedParameterSet() {
		int lastIndex = namedParameters.getItemCount();
		if (lastIndex > 0) {
			String paramName = selectedNormalizer
					.getLastSavedParameterSetName();
			/* load from memory */
			Map<Serializable, Serializable> parameters = selectedNormalizer
					.getNamedParameterSet(paramName);
			if (parameters != null) {// fix share directory issue in gpmodule
				selectedNormalizer.setParameters(parameters);
				namedParameters.setSelectedItem(paramName);
			}
		} else {
			/* nothing saved, so select nothing */
		}
	}

	/**
	 * Listener invoked when a named parameter is selected from the relevant
	 * JList.
	 * 
	 * @param lse
	 *            the <code>ListSelectionEvent</code> received from the
	 *            <code>ListSelectionListener</code> listening to the
	 *            namedParameters JList
	 */
	private void namedParameterSelection_action(ActionEvent e) {
		if (calledFromProgram) return;
		if (selectedNormalizer == null) {
			delete.setEnabled(false);
			return;
		}
		int index = namedParameters.getSelectedIndex();
		if (index > 0) {
			delete.setEnabled(true);

			String paramName = (String) namedParameters.getItemAt(index);
			/* load from memory */
			Map<Serializable, Serializable> parameters = selectedNormalizer
					.getNamedParameterSet(paramName);
			selectedNormalizer.setParameters(parameters);
		}
	}

	/**
	 * Listener invoked when the "Normalization" button is pressed.
	 * 
	 * @param e
	 *            <code>ActionEvent</code> generated by the "analyze" button
	 */
	private void normalization_actionPerformed(ActionEvent e) {
		if (selectedNormalizer == null || maSet == null)
			return;
		ParamValidationResults pvr = selectedNormalizer.validateParameters();
		if (!pvr.isValid()) {
			/* Bring up an error message */
			JOptionPane.showMessageDialog(null, pvr.getMessage(),
					"Parameter Validation Error", JOptionPane.ERROR_MESSAGE);
		} else {
			final int PROCEED_OPTION = 0;
			Object[] options = { "Proceed", "Cancel" };
			int n = JOptionPane
					.showOptionDialog(
							null,
							"You're making changes to the data. \nDo you want to save the current workspace before the change takes place?\n"
									+ "If you want to save the workspace, please click cancel and then save it from the application menu.",
							"Proceed to change?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, /*
																 * do not use a
																 * custom Icon
																 */
							options, /* the titles of buttons */
							options[0]); /* default button title */
			if (n != PROCEED_OPTION)
				return;

			/* Invoke the selected normalizer */
			AlgorithmExecutionResults results = selectedNormalizer
					.execute(maSet);
			/* If there were problems encountered, let the user know. */
			if (!results.isExecutionSuccessful()) {
				JOptionPane
						.showMessageDialog(null, results.getMessage(),
								"Normalizer Execution Error",
								JOptionPane.ERROR_MESSAGE);
				return;
			}

			/*
			 * If everything was OK, notify interested application components
			 * with the results of the normalization operation.
			 */
			if (results.getResults() instanceof DSMicroarraySet) {
				DSMicroarraySet normalizedData = (DSMicroarraySet) results
						.getResults();
				ProjectPanel.getInstance().processNormalization(maSet,
						normalizedData, selectedNormalizer.getLabel());
				AnalysisInvokedEvent event = new AnalysisInvokedEvent(
						selectedNormalizer, maSet.getDataSetName());
				publishAnalysisInvokedEvent(event);
				
			} else {
				log
						.error("This shouldn't happen. results.getResults() should return a DSMicroarraySet<?>");
			}
		}
	}

	@Publish
	public AnalysisInvokedEvent publishAnalysisInvokedEvent(
			AnalysisInvokedEvent event) {
		return event;
	}
	
	/**
	 * Listener invoked when the "Save Parameters" button is pressed
	 * 
	 * @param e
	 *            <code>ActionEvent</code> generated by "save" button
	 */
	private void save_actionPerformed(ActionEvent e) {
		if (selectedNormalizer.parameterSetExist(selectedNormalizer
				.getParameters())) {
			JOptionPane.showMessageDialog(null, "Parameter set already exists.",
					"Canceled", JOptionPane.OK_OPTION);
		} else {
			/*
			 * Bring up a pop-up window for the user to enter the named to use.
			 * If the currently displayed parameter already has a name
			 * associated with it, use that name in the pop-up, otherwise show
			 * something like "New Parameter Setting Name".
			 */
			int index = namedParameters.getSelectedIndex();
			String namedParameter = null;
			if (index > 0) {
				namedParameter = (String) namedParameters.getItemAt(index);
			} else {
				namedParameter = "New Parameter Setting Name";
			}

			String paramName = JOptionPane.showInputDialog(normalizationPanel,
					namedParameter, namedParameter);
			File checkFile = new File(selectedNormalizer
					.scrubFilename(paramName));
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
			if (selectedNormalizer != null && paramName != null) {
				selectedNormalizer.saveParameters(paramName);
				setNamedParameters(selectedNormalizer
						.getNamesOfStoredParameterSets());
			}
			if (namedParameters.getItemCount() > 1)
				delete.setEnabled(true);
		}
	}
}