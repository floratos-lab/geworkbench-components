package org.geworkbench.components.filtering;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

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
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.ComponentRegistry;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.AnalysisInvokedEvent;
import org.geworkbench.events.FilteringEvent;
import org.geworkbench.util.CommandBase;
import org.geworkbench.util.ProgressBar;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Application component offering users a selection of microarray filtering
 * options.
 * 
 * @author First Genetic Trust, yc2480
 * @version $Id$
 */
@AcceptTypes( { DSMicroarraySet.class })
public class FilteringPanel extends CommandBase implements VisualPlugin, ReHighlightable {
	private Log log = LogFactory.getLog(FilteringPanel.class);

	/**
	 * The underlying GUI panel for the filtering component
	 */
	protected JPanel filteringPanel = new JPanel();
	private JPanel mainPane1 = new JPanel();

	/**
	 * The currently selected microarray set.
	 */
	protected DSMicroarraySet maSet = null;

	/**
	 * The most recently used filter.
	 */
	protected AbstractAnalysis selectedFilter = null;

	private JComboBox namedParameters = new JComboBox();
	
	BorderLayout borderLayout1 = new BorderLayout();
	JScrollPane jScrollPane2 = new JScrollPane();
	JPanel jPanel3 = new JPanel();
	JPanel jPanelControl = new JPanel();
	BorderLayout borderLayout2 = new BorderLayout();
	BorderLayout borderLayout3 = new BorderLayout();

	GridLayout gridLayout1 = new GridLayout();
	GridLayout gridLayout2 = new GridLayout();
	JButton filter = new JButton("Filter");
	JButton save = new JButton("Save Settings");
	JButton deleteSetting = new JButton("Delete Settings");
	private JButton previewButton = new JButton("Preview");
	JPanel jPanel4 = new JPanel();
	FlowLayout flowLayout1 = new FlowLayout();
	ParameterPanel emptyParameterPanel = new ParameterPanel();
	ParameterPanel currentParameterPanel = emptyParameterPanel;
	BorderLayout borderLayout4 = new BorderLayout();
	BorderLayout borderLayout5 = new BorderLayout();
	BorderLayout borderLayout6 = new BorderLayout();
	JPanel jPanel1 = new JPanel();

	public FilteringPanel() {
		super("Filtering");
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * Get the available normalizers from the
		 * ComponentRegistry
		 */
		getAvailableFilters();
	}

	public Component getComponent() {
		return filteringPanel;
	}

	/*
	 * 
	 */
	private void jbInit() throws Exception {
		mainPane1 = new JPanel();
		mainPane1.setLayout(new BoxLayout(mainPane1, BoxLayout.Y_AXIS));

		jPanel4.setLayout(borderLayout5);
		filteringPanel.setLayout(borderLayout2);
		jPanel3.setLayout(borderLayout3);
		jPanelControl.setLayout(borderLayout4);

		gridLayout1.setColumns(2);
		gridLayout1.setRows(3);
		gridLayout1.setVgap(0);
		gridLayout2.setColumns(4);
		gridLayout2.setRows(3);
		filter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filtering_actionPerformed();
			}

		});
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save_actionPerformed(e);
			}

		});
		deleteSetting.addActionListener(new ActionListener() {
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
		filteringPanel.add(jScrollPane2, BorderLayout.CENTER);
		jScrollPane2.getViewport().add(jPanel3, null);
		jPanel3.add(mainPane1, BorderLayout.CENTER);
		currentParameterPanel.setLayout(borderLayout6);
		jPanelControl.add(jPanel4, BorderLayout.WEST);
		jPanel4.add(currentParameterPanel, BorderLayout.CENTER);

		mainPane1.add(jPanel1);
		jPanel1.add(Box.createRigidArea(new Dimension(5, 0)));
		jPanel1.add(new JLabel("Filter"));
		jPanel1.add(Box.createRigidArea(new Dimension(5, 0)));
		jPanel1.add(new JLabel("Saved Parameters"));
		jPanel1.add(Box.createRigidArea(new Dimension(5, 0)));
		jPanel1.add(namedParameters, null);

		mainPane1.add(jPanelControl);

		save.setPreferredSize(deleteSetting.getPreferredSize());
		filter.setPreferredSize(deleteSetting.getPreferredSize());
		previewButton.setPreferredSize(deleteSetting.getPreferredSize());
		deleteSetting.setEnabled(false);

		FormLayout layout = new FormLayout("right:100dlu,10dlu", "");
		DefaultFormBuilder buttonsBuilder = new DefaultFormBuilder(layout);
		buttonsBuilder.setDefaultDialogBorder();
		buttonsBuilder.append(filter);
		buttonsBuilder.nextLine();
		buttonsBuilder.append(save);
		buttonsBuilder.nextLine();
		buttonsBuilder.append(deleteSetting);
		buttonsBuilder.nextLine();
		buttonsBuilder.append(previewButton);

		jPanelControl.add(buttonsBuilder.getPanel(), BorderLayout.EAST);
		
		previewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				preview();
			}
		});

	}


	protected void preview() {
		if (selectedFilter == null || maSet == null)
			return;
		
		ParamValidationResults pvr = selectedFilter.validateParameters();
		if (!pvr.isValid()) { /* Bring up an error message */
			JOptionPane.showMessageDialog(null, pvr.getMessage(),
					"Parameter Validation Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		List<Integer> indexList = ((FilteringAnalysis)selectedFilter).getMarkersToBeRemoved(maSet);
		if (indexList==null){			
			return;
			}
		List<DSGeneMarker> list = new ArrayList<DSGeneMarker>();
		for(Integer index: indexList)
			list.add(maSet.getMarkers().get(index));
		PreviewDialog dialog = new PreviewDialog(list, maSet.getMarkers().size(), this);
		dialog.setVisible(true);
	}

	// this is only necessary for preview window
	Frame getFrame() {
		Container container = getComponent().getParent();
		while(!(container instanceof Frame)) {
			container = container.getParent();
		}
		return (Frame)container;
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
		if ((selectedFilter != null) && (choice == 0)
				&& (namedParameters.getSelectedIndex() > 0)) {
			log.info("Deleting saved parameters: "
					+ (String) namedParameters.getSelectedItem());
			this.removeNamedParameter((String) namedParameters
					.getSelectedItem());
			if (namedParameters.getItemCount() <= 1)
				deleteSetting.setEnabled(false);
		}
	}
	
	/**
	 * Delete the selected saved parameter.
	 * 
	 * @param name -
	 *            name of the saved parameter
	 */
	private void removeNamedParameter(String name) {
		selectedFilter.removeNamedParameter(name);
		this.setNamedParameters(selectedFilter
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
			FilterOptionPanel.arrayNumber = maSet.size();
			getAvailableFilters();
			updateMenuItems();
		}
	}

	/**
	 * Queries the extension point <code>filters</code> within the
	 * <code>ComponentRegistry </code> for available filter-type plugins.
	 * 
	 * This method gets invoked every time that the analysis pane gets the
	 * focus, in order to get the most recent list of filters: given dynamic
	 * loading of components this approach guarantees that any new plugins
	 * loaded between uses of the filtering panel, will be correctly picked up.
	 * 
	 * @return <code>true</code> if the most recently used filter is found in
	 *         the <code>ComponentRegistry </code>. <code>fales</code>,
	 *         otherwise.
	 */
	public void getAvailableFilters() {
		/* To check if the last used normalizer is still available. */
		boolean selectionChanged = true;
		/* Populate 'availableFilters[]' from ComponentRegistry. */
		FilteringAnalysis[] analyses = ComponentRegistry.getRegistry()
				.getModules(FilteringAnalysis.class);
		availableCommands = new AbstractAnalysis[analyses.length];
		for (int i = 0; i < analyses.length; i++) {
			availableCommands[i] = (AbstractAnalysis) analyses[i];
			if (selectedFilter == availableCommands[i])
				selectionChanged = false;
		}

		/*
		 * If the selectedFilter has been removed from the list of available
		 * normalizers, reset.
		 */
		if (selectionChanged)
			if (analyses.length > 0)
				selectedFilter = availableCommands[0];
			else
				selectedFilter = null;
		
		AbstractAnalysisLabelComparator comparator = new AbstractAnalysisLabelComparator();
		Arrays.sort(availableCommands, comparator );
	}

	/**
	 * Set the parameters panel used in the filtering pane.
	 * 
	 * @param parameterPanel
	 */
	private void setParametersPanel(ParameterPanel parameterPanel) {
		jPanel4.remove(currentParameterPanel);
		currentParameterPanel = parameterPanel;
		jPanel4.add(currentParameterPanel, BorderLayout.CENTER);
		filteringPanel.revalidate();
		filteringPanel.repaint();
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
		namedParameters.removeAllItems();
		namedParameters.addItem("");
		for(String n: storedParameters)
			namedParameters.addItem(n);
		filteringPanel.revalidate();
		highlightCurrentParameterGroup();
	}

	/**
	 * Scan the saved list, see if the parameters in it are same as current one,
	 * if yes, highlight it.
	 */
	private void highlightCurrentParameterGroup() {
		ParameterPanel currentParameterPanel = selectedFilter
				.getParameterPanel();
		String[] parametersNameList = selectedFilter
				.getNamesOfStoredParameterSets();
		namedParameters.setSelectedIndex(0);
		for (int i = 0; i < parametersNameList.length; i++) {
			Map<Serializable, Serializable> parameter1 = ((AbstractSaveableParameterPanel) currentParameterPanel)
					.getParameters();
			Map<Serializable, Serializable> parameter2 = new HashMap<Serializable, Serializable>();
			parameter2.putAll(selectedFilter
					.getNamedParameterSet(parametersNameList[i]));
			parameter2.remove(ParameterKey.class.getSimpleName());
			if (parameter1.equals(parameter2)) {
				namedParameters.setSelectedIndex(i+1);
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

	protected void setSelectedCommandByName(String commandName) {

		deleteSetting.setEnabled(false);

		selectedFilter = getCommandByName(commandName);
		/* Get the parameters panel for the selected filter. */
		ParameterPanel paramPanel = selectedFilter.getParameterPanel();
		/* Set the list of available named parameters for the selected filter. */
		if (paramPanel != null) {
			setNamedParameters(selectedFilter.getNamesOfStoredParameterSets());
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
			setParametersPanel(this.emptyParameterPanel);
			save.setEnabled(false);
			/*
			 * Since the filter admits no parameters, there are no named
			 * parameter settings to show.
			 */
			setNamedParameters(new String[0]);
		}

	}

	/**
	 * 
	 */
	private void selectLastSavedParameterSet() {
		int lastIndex = namedParameters.getItemCount();
		if (lastIndex > 0) {
			String paramName = selectedFilter.getLastSavedParameterSetName();
			/* load from memory */			
			Map<Serializable, Serializable> parameters = selectedFilter
					.getNamedParameterSet(paramName);
			if (parameters != null) // fix share directory issue in gpmodule
				selectedFilter.setParameters(parameters);
			namedParameters.setSelectedItem(paramName);
		} else {
			/* nothing saved, so select nothing */
		}
	}

	/**
	 * Listener invoked when a named parameter is selected from the relevant
	 * JList.
	 * 
	 * @param lse
	 *            the <code>MouseEvent</code> received from the
	 *            <code>MouseListener</code> listening to the namedParameters
	 *            JList
	 */
	private void namedParameterSelection_action(ActionEvent e) {
		if (selectedFilter == null) {
			deleteSetting.setEnabled(false);
			return;
		}
		int index = namedParameters.getSelectedIndex();
		if (index >0) {
			deleteSetting.setEnabled(true);

			String paramName = (String) namedParameters.getItemAt(index);
			/* load from memory */
			Map<Serializable, Serializable> parameters = selectedFilter
					.getNamedParameterSet(paramName);
			if(parameters==null) {
				log.error("Saved paremeter '"+paramName+"' was not found.");
				return;
			}
			selectedFilter.setParameters(parameters);
		}
	}

	/**
	 * Listener invoked when the "Filter" button is pressed.
	 * 
	 */
	void filtering_actionPerformed() {
		
		if (selectedFilter == null || maSet == null)
			return;
		
		ParamValidationResults pvr = selectedFilter.validateParameters();
		if (!pvr.isValid()) { /* Bring up an error message */
			JOptionPane.showMessageDialog(null, pvr.getMessage(),
					"Parameter Validation Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		List<Integer> indexList = ((FilteringAnalysis)selectedFilter).getMarkersToBeRemoved(maSet);
		if (indexList==null){			
			return;
			}

		Object[] options = { "Proceed", "Cancel" };
		int n = JOptionPane
				.showOptionDialog(
						null,
						"You're making changes to the data. \nDo you want to save the current workspace before the change takes place?\n"
								+ "If you want to save the workspace, please click cancel and then save it from the application menu.",
						"Proceed to change?", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, /* do not use a custom Icon */
						options, /* the titles of buttons */
						options[0]); /* default button title */
		if (n != JOptionPane.YES_OPTION)
			return;

		progressBar.setTitle(selectedFilter.getLabel());
		progressBar.setMessage("Filtering is ongoing. Please wait.");
		progressBar.setAlwaysOnTop(true);
		progressBar.start(); // this progress-bar is not properly cancellable

		/* Invoke the selected filter */
		FilterWorker worker = new FilterWorker();
		worker.execute();
		
		hideDialog();
	}
	
	final private ProgressBar progressBar = ProgressBar
			.create(ProgressBar.INDETERMINATE_TYPE);

	private class FilterWorker extends
			SwingWorker<AlgorithmExecutionResults, Void> {
		private int count = 0;

		@Override
		protected AlgorithmExecutionResults doInBackground() throws Exception {
			//Thread.sleep(5000);// just to make it slow to test the progress-bar
			count = ((FilteringAnalysis)selectedFilter).getMarkersToBeRemoved(maSet).size();
			return selectedFilter.execute(maSet);
		}

		@Override
		protected void done() {
			AlgorithmExecutionResults results = null;
			try {
				results = get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} finally {
				progressBar.stop();
			}
			/* If there were problems encountered, let the user know. */
			if (!results.isExecutionSuccessful()) {
				JOptionPane.showMessageDialog(null, results.getMessage(),
						"Filter Execution Error", JOptionPane.ERROR_MESSAGE);
				progressBar.stop();
				return;
			}

			/*
			 * If everything was OK, notify interested application components
			 * with the results of the normalization operation.
			 */
			DSMicroarraySet filteredData = (DSMicroarraySet) results
					.getResults();
			String historyString = "";
			if (selectedFilter.getLabel() != null)
				historyString += selectedFilter.getLabel() + "\n";
			/* to avoid printing null for panels  didn't implement this method. */
			if (selectedFilter.createHistory() != null) 
				historyString += selectedFilter.createHistory();
			 /* to separate with next section (if any) */
			historyString += count + " markers were removed.\n";
			historyString += "----------------------------------------\n";
			
			progressBar.stop();
			AnalysisInvokedEvent event = new AnalysisInvokedEvent(
					selectedFilter,maSet.getDataSetName() );
			publishAnalysisInvokedEvent(event);
			publishFilteringEvent(new FilteringEvent(maSet, filteredData,
					historyString));
			log.debug("filtering done.");

		}

	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	@Publish
	public FilteringEvent publishFilteringEvent(FilteringEvent event) {
		return event;
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
		/*
		 * If the parameterSet already exist, we popup a message window to
		 * inform user
		 */
		if (selectedFilter == null || maSet == null)
			return;
		
		ParamValidationResults pvr = selectedFilter.validateParameters();
		if (!pvr.isValid()) { /* Bring up an error message */
			JOptionPane.showMessageDialog(null, pvr.getMessage(),
					"Parameter Validation Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if (selectedFilter.parameterSetExist(selectedFilter.getParameters())) {
			JOptionPane.showMessageDialog(null, "Parameter set already exists.",
					"Canceled", JOptionPane.OK_OPTION);
		} else {
			int index = namedParameters.getSelectedIndex();
			String namedParameter = null;
			if (index > 0) {
				namedParameter = (String) namedParameters.getItemAt(index);
			} else {
				namedParameter = "New Parameter Setting Name";
			}

			String paramName = JOptionPane.showInputDialog(filteringPanel,
					namedParameter, namedParameter);
			File checkFile = new File(selectedFilter.scrubFilename(paramName));
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
	
			if (selectedFilter != null && paramName != null) {
				selectedFilter.saveParameters(paramName);
				setNamedParameters(selectedFilter
						.getNamesOfStoredParameterSets());
			}
		}
	}

}