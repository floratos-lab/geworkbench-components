package org.geworkbench.components.filtering;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.ComponentRegistry;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.FilteringEvent;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust
 * @version 1.0
 */

/**
 * Application component offering users a selection of microarray filtering
 * options.
 */
@AcceptTypes({DSMicroarraySet.class}) public class FilteringPanel implements VisualPlugin {
    /**
     * The underlying GUI panel for the filtering component
     */
    protected JPanel filteringPanel = new JPanel();
    /**
     * Contains the pluggable filters available to the user to choose from.
     * These filters will have been defined in the application configuration
     * file as <code>plugin</code> components and they are expected to have been
     * associated with the extension point <code>filters</code>.
     */
    

	private JSplitPane jSplitPane1 = new JSplitPane();
	/**
	 * Visual Widget
	 */
    protected AbstractAnalysis[] availableFilters;
    /**
     * The currently selected microarray set.
     */
    protected DSMicroarraySet maSet = null;
    /**
     * The most recently used filter.
     */
    protected AbstractAnalysis selectedFilter = null;
    /**
     * JList used to display the normalizers.
     */
    protected JList pluginFilters = new JList();
    /**
     * JList used to display named parameter settings for a selected filter.
     */


    protected JList namedParameters = new JList();
    BorderLayout borderLayout1 = new BorderLayout();
    JScrollPane jScrollPane2 = new JScrollPane();
    JPanel jPanel3 = new JPanel();
    JPanel jPanelControl = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    JPanel buttons = new JPanel();
    GridLayout gridLayout1 = new GridLayout();
    GridLayout gridLayout2 = new GridLayout();
    JButton analyze = new JButton("Filter");
    JButton save = new JButton("Save Settings");
    JPanel jPanel4 = new JPanel();
    FlowLayout flowLayout1 = new FlowLayout();
    ParameterPanel emptyParameterPanel = new AbstractSaveableParameterPanel();
    ParameterPanel currentParameterPanel = emptyParameterPanel;
    BorderLayout borderLayout4 = new BorderLayout();
    BorderLayout borderLayout5 = new BorderLayout();
    BorderLayout borderLayout6 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    GridLayout gridLayout3 = new GridLayout();
    JScrollPane jScrollPane1 = new JScrollPane();
    JScrollPane jScrollPane3 = new JScrollPane();

    public FilteringPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get (and display) the available normalizers from the PluginRegistry
        reset();
    }

    public Component getComponent() {
        return filteringPanel;
    }

    private void jbInit() throws Exception {
    	jSplitPane1 = new JSplitPane();
    	jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPane1.setDividerSize(3);
		
        jPanel4.setLayout(borderLayout5);
        filteringPanel.setLayout(borderLayout2);
        jPanel3.setLayout(borderLayout3);
        jPanelControl.setLayout(borderLayout4);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.setPreferredSize(new Dimension(248, 60));
        gridLayout1.setColumns(2);
        gridLayout1.setRows(3);
        gridLayout1.setVgap(0);
        gridLayout2.setColumns(4);
        gridLayout2.setRows(3);
        analyze.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                filtering_actionPerformed(e);
            }

        });
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                save_actionPerformed(e);
            }

        });
        jScrollPane1.setPreferredSize(new Dimension(248, 80));
        jPanel1.setLayout(gridLayout3);
        jPanel1.setMinimumSize(new Dimension(0, 0));
        jPanel1.setPreferredSize(new Dimension(50, 50));
        jPanel1.setMaximumSize(new Dimension(50, 80));
        // Make sure that only one filter can be selected at a time;
        pluginFilters.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pluginFilters.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                filterSelected_action(e);
            }

        });
        pluginFilters.setBorder(BorderFactory.createLineBorder(Color.black));
        // Make sure that only one parameter set can be selected at a time;
        namedParameters.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        namedParameters.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                namedParameterSelection_action(e);
            }

        });
        namedParameters.setAutoscrolls(true);
        namedParameters.setBorder(BorderFactory.createLineBorder(Color.black));
        filteringPanel.add(jScrollPane2, BorderLayout.CENTER);
        jScrollPane2.getViewport().add(jPanel3, null);
        jPanel3.add(jSplitPane1, BorderLayout.CENTER);
        currentParameterPanel.setLayout(borderLayout6);
        jSplitPane1.add(jPanelControl, JSplitPane.BOTTOM);
        jPanelControl.add(jPanel4, BorderLayout.WEST);
        jPanel4.add(currentParameterPanel, BorderLayout.CENTER);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(Box.createRigidArea(new Dimension(10, 0)));
        buttons.add(Box.createRigidArea(new Dimension(10, 0)));
        buttons.add(save);
        buttons.add(analyze);
//        jPanel3.add(jPanel1, BorderLayout.NORTH);
        jSplitPane1.add(jPanel1, JSplitPane.TOP);
        jPanel1.add(jScrollPane1, null);
        jPanel1.add(jScrollPane3, null);
        jScrollPane3.getViewport().add(namedParameters, null);
        jScrollPane1.getViewport().add(pluginFilters, null);
        buttons.add(Box.createRigidArea(new Dimension(10, 0)));
        jPanelControl.add(buttons, BorderLayout.EAST);
    }

    /**
     * Implementation of method from interface <code>ProjectListener</code>.
     * Handles notifications about change of the currently selected microarray
     * set.
     *
     * @param pe Project event cotnaining the newly selected microarray set.
     */
    @Subscribe public void receive(org.geworkbench.events.ProjectEvent pe, Object source) {
        DSDataSet dataSet = pe.getDataSet();
        if (dataSet != null && dataSet instanceof DSMicroarraySet) {
            maSet = (DSMicroarraySet) dataSet;
            reset();
        }
    }

    /**
     * Queries the extension point <code>filters</code> within the
     * <code>PluginRegistry </code> for available filter-type plugins.
     *
     * @return <code>true</code> if the most recently used filter is
     *         found in the <code>PluginRegistry </code>. <code>fales</code>,
     *         otherwise.
     */
    // This method gets invoked every time that the analysis
    // pane gets the focus, in order to get the most recent list of filters:
    // given dynamic loading of components this approach guarantees that any new
    // plugins loaded between uses of the filtering panel, will be correctly
    // picked up.
    public void getAvailableFilters() {
        // To check if the last used normalizer is still available.
        boolean selectionChanged = true;
        // Populate 'availableFilters[]' from PluginRegistry.
        //        PluginDescriptor[] analyses =
        //        PluginRegistry.getPluginsAtExtension("filters");
        FilteringAnalysis[] analyses = ComponentRegistry.getRegistry().getModules(FilteringAnalysis.class);
        availableFilters = new AbstractAnalysis[analyses.length];
        for (int i = 0; i < analyses.length; i++) {
            availableFilters[i] = (AbstractAnalysis) analyses[i];
            if (selectedFilter == availableFilters[i])
                selectionChanged = false;
        }

        // If the selectedFilter has been removed from the list of available
        // normalizers, reset.
        if (selectionChanged)
            if (analyses.length > 0)
                selectedFilter = availableFilters[0];
            else
                selectedFilter = null;
    }

    /**
     * Obtains from the <code>PluginRegistry</code> ans displays the set of
     * available filters.
     */
    public void reset() {
        // Get the most recent available normalizers. Redisplay
        getAvailableFilters();
        displayFilters();
    }

    /**
     * Displays the list of available filters.
     */
    private void displayFilters() {
        // Show graphical components
        pluginFilters.removeAll();
        // Stores the dispay names of the available filters.
        String[] names = new String[availableFilters.length];
        for (int i = 0; i < availableFilters.length; i++) {
            names[i] = availableFilters[i].getLabel();
        }

        pluginFilters.setListData(names);
        if (selectedFilter != null)
            pluginFilters.setSelectedValue(selectedFilter.getLabel(), true);
        else {
            setParametersPanel(this.emptyParameterPanel);
            save.setEnabled(false);
        }

    }

    /**
     * Set the parameters panel used in the filtering pane.
     *
     * @param parameterPanel
     */
    private void setParametersPanel(ParameterPanel parameterPanel) {
        //    currentParameterPanel.removeAll();
        //    currentParameterPanel.add(parameterPanel, BorderLayout.CENTER);
        jPanel4.remove(currentParameterPanel);
        currentParameterPanel = parameterPanel;
        jPanel4.add(currentParameterPanel, BorderLayout.CENTER);
        filteringPanel.revalidate();
        filteringPanel.repaint();
    }

    /**
     * Update the list that shows the known preset parameter settings for the
     * selected filter.
     *
     * @param storedParameters
     */
    private void setNamedParameters(String[] storedParameters) {
        namedParameters.removeAll();
        namedParameters.setListData(storedParameters);
        // Make sure that only one parameter set can be selected at a time;
        namedParameters.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        filteringPanel.revalidate();
    }

    /**
     * Listener invoked when a new filter is selected from the
     * displayed list of filters.
     *
     * @param lse The <code>ListSelectionEvent</code> received from the list
     *            selection.
     */
    private void filterSelected_action(ListSelectionEvent lse) {
        if (pluginFilters.getSelectedIndex() == -1)
            return;
        selectedFilter = availableFilters[pluginFilters.getSelectedIndex()];
        // Set the parameters panel for the selected filter.
        ParameterPanel paramPanel = selectedFilter.getParameterPanel();
        // Set the list of available named parameters for the selected filter.
        if (paramPanel != null) {
            setParametersPanel(paramPanel);
            setNamedParameters(availableFilters[pluginFilters.getSelectedIndex()].getNamesOfStoredParameterSets());
            save.setEnabled(true);
        } else {
            setParametersPanel(this.emptyParameterPanel);
            save.setEnabled(false);
            // Since the filter admits no parameters, there are no named parameter
            // settings to show.
            setNamedParameters(new String[0]);
        }

    }

    /**
     * Listener invoked when a named parameter is selected from the
     * relevant JList.
     *
     * @param lse the <code>MouseEvent</code> received from the
     *            <code>MouseListener</code> listening to the namedParameters JList
     */
    private void namedParameterSelection_action(ListSelectionEvent e) {
        if (selectedFilter == null)
            return;
        int index = namedParameters.getSelectedIndex();
        if (index != -1)
            setParametersPanel(selectedFilter.getNamedParameterSetPanel((String) namedParameters.getModel().getElementAt(index)));
    }

    /**
     * Listener invoked when the "Filter" button is pressed.
     *
     * @param e <code>ActionEvent</code> generated by the "analyze" button
     */
    private void filtering_actionPerformed(ActionEvent e) {
        if (selectedFilter == null || maSet == null)
            return;
        ParamValidationResults pvr = selectedFilter.validateParameters();
        if (!pvr.isValid()) {
            // Bring up an error message
            JOptionPane.showMessageDialog(null, pvr.getMessage(), "Parameter Validation Error", JOptionPane.ERROR_MESSAGE);
        } else {
            // Invoke the selected filter
            AlgorithmExecutionResults results = selectedFilter.execute(maSet);
            // If there were problems encountered, let the user know.
            if (!results.isExecutionSuccessful()) {
                JOptionPane.showMessageDialog(null, results.getMessage(), "Filter Execution Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // If everything was OK, notify interested application components with
            // the results of the normalization operation.
            DSMicroarraySet filteredData = (DSMicroarraySet) results.getResults();
            String historyString = "";
            if (selectedFilter.getLabel()!=null)
            	historyString += selectedFilter.getLabel()+"\n";
            if (selectedFilter.createHistory()!=null)	//to avoid printing null for panels didn't implement this method.
            	historyString += selectedFilter.createHistory()+"\n";
            historyString += "\n"; //to separate with next section (if any) 
            publishFilteringEvent(new FilteringEvent(maSet, filteredData, historyString));
        }

    }

    @Publish public FilteringEvent publishFilteringEvent(FilteringEvent event) {
        return event;
    }

    /**
     * Listener invoked when the "Save Parameters" button is pressed
     *
     * @param e <code>ActionEvent</code> generated by "save" button
     */
    private void save_actionPerformed(ActionEvent e) {
        // Bring up a pop-up window for the user to enter the named to use.
        // If the currently dispayed parameter already has a name associated
        // with it, use that name in the pop-up, otherwise show somwthing like
        // "New Parameter Setting Name".
        int index = namedParameters.getSelectedIndex();
        String namedParameter = null;
        if (index != -1) {
            namedParameter = (String) namedParameters.getModel().getElementAt(index);
            if (currentParameterPanel.isDirty())
                namedParameter = "New Parameter Setting Name";
        } else {
            namedParameter = "New Parameter Setting Name";
        }

        String paramName = JOptionPane.showInputDialog(filteringPanel, namedParameter, namedParameter);
        if (selectedFilter != null && paramName != null) {
            selectedFilter.saveParametersUnderName(paramName);
            setNamedParameters(selectedFilter.getNamesOfStoredParameterSets());
        }

    }

}

