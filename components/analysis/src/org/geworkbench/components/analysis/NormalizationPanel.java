package org.geworkbench.components.analysis;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.NormalizingAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.ComponentRegistry;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.NormalizationEvent;

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
 * Application component offering users a selection of microarray
 * normalization options.
 */
@AcceptTypes({DSMicroarraySet.class}) public class NormalizationPanel implements VisualPlugin {
    /**
     * The underlying panel for the normalization component
     */
    protected JPanel normalizationPanel = new JPanel();
    /**
     * Contains the pluggable normalizers available to the user to choose from.
     * These normalizers will have been defined in the application configuration
     * file as <code>plugin</code> components and they are expected to have been
     * associated with the extension point <code>normalizers</code>.
     */
    // Contains the normalizers available to the user to choose from.
    // E.g., availableNormalizers[i].getLabel(), should give the display name
    // for an analysis.
	private JSplitPane jSplitPane1 = new JSplitPane();
	/**
	 * Visual Widget
	 */
    protected AbstractAnalysis[] availableNormalizers;
    /**
     * The currently selected microarray set.
     */
    protected DSMicroarraySet maSet = null;
    /**
     * The most recently used normalizer.
     */
    protected AbstractAnalysis selectedNormalizer = null;
    /**
     * JList used to display the normalizers.
     */
    protected JList pluginNormalizers = new JList();
    /**
     * JList used to display named parameter settings for a selected normalizer.
     */
    protected JList namedParameters = new JList();
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

    public NormalizationPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get (and display) the available normalizers from the PluginRegistry
        reset();
    }

    /**
     * Implementation of method from interface <code>VisualPlugin</code>.
     *
     * @return
     */
    public Component getComponent() {
        return normalizationPanel;
    }

    private void jbInit() throws Exception {
    	jSplitPane1 = new JSplitPane();
    	jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPane1.setDividerSize(3);
    	
        jPanel4.setLayout(borderLayout5);
        normalizationPanel.setLayout(borderLayout2);
        jPanel3.setLayout(borderLayout3);
        jPanelControl.setLayout(borderLayout4);
//        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
//        buttons.setPreferredSize(new Dimension(248, 60));
        gridLayout1.setColumns(2);
        gridLayout1.setRows(3);
        gridLayout1.setVgap(0);
        gridLayout2.setColumns(4);
        gridLayout2.setRows(3);
        analyze.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                normalization_actionPerformed(e);
            }

        });
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                save_actionPerformed(e);
            }

        });
        jPanel1.setLayout(gridLayout3);
        jPanel1.setMinimumSize(new Dimension(0, 0));
        jPanel1.setPreferredSize(new Dimension(50, 50));
        jPanel1.setMaximumSize(new Dimension(50, 100));
        jScrollPane1.setPreferredSize(new Dimension(248, 100));
        // Make sure that only one normalizer can be selected at a time;
        pluginNormalizers.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pluginNormalizers.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                normalizerSelected_action(e);
            }

        });
        pluginNormalizers.setBorder(BorderFactory.createLineBorder(Color.black));
        // Make sure that only one parameter set can be selected at a time;
        namedParameters.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        namedParameters.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                namedParameterSelection_action(e);
            }

        });
        namedParameters.setAutoscrolls(true);
        namedParameters.setBorder(BorderFactory.createLineBorder(Color.black));
        normalizationPanel.add(jScrollPane2, BorderLayout.CENTER);
        jScrollPane2.getViewport().add(jPanel3, null);
        jPanel3.add(jSplitPane1, BorderLayout.CENTER);
        currentParameterPanel.setLayout(borderLayout6);
        jSplitPane1.add(jPanelControl, JSplitPane.BOTTOM);
        jPanelControl.add(jPanel4, BorderLayout.WEST);
        jPanel4.add(currentParameterPanel, BorderLayout.CENTER);
//        buttons.add(Box.createHorizontalGlue());
//        buttons.add(Box.createRigidArea(new Dimension(10, 0)));
//        buttons.add(Box.createRigidArea(new Dimension(10, 0)));
//        buttons.add(save);
//        buttons.add(analyze);

        // Add buttons
        analyze.setPreferredSize(save.getPreferredSize());
        FormLayout layout = new FormLayout("right:100dlu,10dlu","");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator("Normalization Actions");
        builder.append(analyze);
        builder.nextLine();
        builder.append(save);

        jPanelControl.add(builder.getPanel(), BorderLayout.EAST);

//        jPanel3.add(jPanel1, BorderLayout.NORTH);
        jSplitPane1.add(jPanel1, JSplitPane.TOP);
        jPanel1.add(jScrollPane1, null);
        jPanel1.add(jScrollPane3, null);
        jScrollPane3.getViewport().add(namedParameters, null);
        jScrollPane1.getViewport().add(pluginNormalizers, null);
//        buttons.add(Box.createRigidArea(new Dimension(10, 0)));
//        jPanel3.add(buttons, BorderLayout.EAST);
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
        if (dataSet instanceof DSMicroarraySet) {
            maSet = (DSMicroarraySet) dataSet;
            reset();
        }
    }

    /**
     * Queries the extension point <code>normalizers</code> within the
     * <code>PluginRegistry </code> for available normalizer-type plugins.
     *
     * @return <code>true</code> if the most recently used normalizer is
     *         found in the <code>PluginRegistry </code>. <code>fales</code>,
     *         otherwise.
     */
    // This method gets invoked every time that the normalization
    // panel gets the focus, in order to get the most recent list of normalizers:
    // given dynamic loading of components this approach guarantees that any new
    // plugins loaded between uses of the normalization panel, will be correctly
    // picked up.
    public void getAvailableAnalyses() {
        // To check if the last used normalizer is still available.
        boolean selectionChanged = true;
        // Populate 'availableNormalizers[]' from PluginRegistry.
        //        PluginDescriptor[] analyses =
        //        PluginRegistry.getPluginsAtExtension("normalizers");
        NormalizingAnalysis[] analyses = ComponentRegistry.getRegistry().getModules(NormalizingAnalysis.class);
        availableNormalizers = new AbstractAnalysis[analyses.length];
        for (int i = 0; i < analyses.length; i++) {
            availableNormalizers[i] = (AbstractAnalysis) analyses[i];
            if (selectedNormalizer == availableNormalizers[i])
                selectionChanged = false;
        }

        // If the selectedNormalizer has been removed from the list of available
        // normalizers, reset.
        if (selectionChanged)
            if (analyses.length > 0)
                selectedNormalizer = availableNormalizers[0];
            else
                selectedNormalizer = null;
    }

    /**
     * Obtains from the <code>PluginRegistry</code> ans displays the set of
     * available normalzers.
     */
    public void reset() {
        // Get the most recent available normalizers. Redisplay
        getAvailableAnalyses();
        displayNormalizers();
    }

    /**
     * Displays the list of available normalizers.
     */
    private void displayNormalizers() {
        // Show graphical components
        pluginNormalizers.removeAll();
        // Stores the dispay names of the available normalizers.
        String[] names = new String[availableNormalizers.length];
        for (int i = 0; i < availableNormalizers.length; i++) {
            names[i] = availableNormalizers[i].getLabel();
        }

        pluginNormalizers.setListData(names);
        if (selectedNormalizer != null)
            pluginNormalizers.setSelectedValue(selectedNormalizer.getLabel(), true);
        else {
            setParametersPanel(this.emptyParameterPanel);
            save.setEnabled(false);
        }

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
    }

    /**
     * Update the list that shows the known preset parameter settings for the
     * selected normalizer.
     *
     * @param storedParameters
     */
    private void setNamedParameters(String[] storedParameters) {
        namedParameters.removeAll();
        namedParameters.setListData(storedParameters);
        // Make sure that only one parameter set can be selected at a time;
        namedParameters.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        normalizationPanel.revalidate();
    }

    /**
     * Listener invoked when a new normalizer is selected from the
     * displayed list of normalizers.
     *
     * @param lse The <code>ListSelectionEvent</code> received from the list
     *            selection.
     */
    private void normalizerSelected_action(ListSelectionEvent lse) {
        if (pluginNormalizers.getSelectedIndex() == -1)
            return;
        selectedNormalizer = availableNormalizers[pluginNormalizers.getSelectedIndex()];
        // Set the parameters panel for the selected normalizer.
        ParameterPanel paramPanel = selectedNormalizer.getParameterPanel();
        // Set the list of available named parameters for the selected normalizer.
        if (paramPanel != null) {
            setParametersPanel(paramPanel);
            setNamedParameters(availableNormalizers[pluginNormalizers.getSelectedIndex()].getNamesOfStoredParameterSets());
            save.setEnabled(true);
        } else {
            setParametersPanel(this.emptyParameterPanel);
            save.setEnabled(false);
            // Since the normalizer admits no parameters, there are no named parameter
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
        if (selectedNormalizer == null)
            return;
        int index = namedParameters.getSelectedIndex();
        if (index != -1)
            setParametersPanel(selectedNormalizer.getNamedParameterSetPanel((String) namedParameters.getModel().getElementAt(index)));
    }

    /**
     * Listener invoked when the "Normalization" button is pressed.
     *
     * @param e <code>ActionEvent</code> generated by the "analyze" button
     */
    private void normalization_actionPerformed(ActionEvent e) {
        if (selectedNormalizer == null || maSet == null)
            return;
        ParamValidationResults pvr = selectedNormalizer.validateParameters();
        if (!pvr.isValid()) {
            // Bring up an error message
            JOptionPane.showMessageDialog(null, pvr.getMessage(), "Parameter Validation Error", JOptionPane.ERROR_MESSAGE);
        } else {
            // Invoke the selected normalizer
            AlgorithmExecutionResults results = //            selectedNormalizer.execute(maSet.deepCopy());
                    selectedNormalizer.execute(maSet);
            // If there were problems encountered, let the user know.
            if (!results.isExecutionSuccessful()) {
                JOptionPane.showMessageDialog(null, results.getMessage(), "Normalizer Execution Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // If everything was OK, notify interested application components with
            // the results of the normalization operation.
            DSMicroarraySet normalizedData = (DSMicroarraySet) results.getResults();
            publishNormalizationEvent(new NormalizationEvent(maSet, normalizedData, selectedNormalizer.getLabel()));
        }

    }

    @Publish public org.geworkbench.events.NormalizationEvent publishNormalizationEvent(org.geworkbench.events.NormalizationEvent event) {
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
        // with it, use that name in the pop-up, otherwise show something like
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

        String paramName = JOptionPane.showInputDialog(normalizationPanel, namedParameter, namedParameter);
        if (selectedNormalizer != null && paramName != null) {
            selectedNormalizer.saveParametersUnderName(paramName);
            setNamedParameters(selectedNormalizer.getNamesOfStoredParameterSets());
        }

    }

}

