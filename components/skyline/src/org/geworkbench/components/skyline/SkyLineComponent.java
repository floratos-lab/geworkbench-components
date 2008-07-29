package org.geworkbench.components.skyline;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.engine.management.ComponentRegistry;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.StructureAnalysisEvent;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.geworkbench.bison.model.analysis.ProteinStructureAnalysis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.*;

/**
 * This is skyline component for structure comparative modeling
 *
 */
// This annotation lists the data set types that this component accepts.
// The component will only appear when a data set of the appropriate type is selected.

@AcceptTypes({DSProteinStructure.class})
public class SkyLineComponent extends JPanel implements VisualPlugin {

    private JLabel infoLabel;
    private String pname;
    private String outdir = "http://156.111.188.2:8090/SkyLineData/output";
//    private String pdboutdir = "/nfs/apollo/2/c2b2/server_data/www/skyline/apache-tomcat-6.0.14/webapps/ROOT/SkyLineData/output/";
    // String outdir = ((SkyLineConfigPanel)currentParameterPanel).getoutdirValue();
    private String logfile;
    protected JPanel structureparamPanel = new JPanel();
    /**
     * Contains the pluggable normalizers available to the user to choose from.
     * These normalizers will have been defined in the application configuration
     * file as <code>plugin</code> components and they are expected to have been
     * associated with the extension point <code>normalizers</code>.
     */
    // Contains the normalizers available to the user to choose from.
    // E.g., availableNormalizers[i].getLabel(), should give the display name
    // for an analysis.
    protected AbstractAnalysis[] availableStructureAnalysis;
    /**
     * The currently selected microarray set.
     */
    //    protected DSMicroarraySet maSet = null;
    protected DSProteinStructure maSet = null;
    /**
     * The most recently used normalizer.
     */
    protected AbstractAnalysis selectedStructureAnalysis = null;
    /**
     * JList used to display the normalizers.
     */
    protected JList pluginStructureAnalysis = new JList();
    /**
     * JList used to display named parameter settings for a selected normalizer.
     */
    protected JList namedParameters = new JList();
    BorderLayout borderLayout1 = new BorderLayout();
    JScrollPane jScrollPane2 = new JScrollPane();
    JPanel jPanel3 = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    GridLayout gridLayout1 = new GridLayout();
    GridLayout gridLayout2 = new GridLayout();
    JButton resetparam = new JButton("Reset Params");
    JButton submit = new JButton("Submit Job");
    JButton checkResults = new JButton("Check Status");
    JButton save = new JButton("Save Settings");
    JPanel jPanel4 = new JPanel();
    FlowLayout flowLayout1 = new FlowLayout();
    ParameterPanel emptyParameterPanel = new AbstractSaveableParameterPanel();
    ParameterPanel currentParameterPanel = emptyParameterPanel;
    BorderLayout borderLayout4 = new BorderLayout();
    BorderLayout borderLayout5 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    /*    JProgressBar pbar = new JProgressBar();
	  JToolBar tbar = new JToolBar();*/
    GridLayout gridLayout3 = new GridLayout();
    JScrollPane jScrollPane1 = new JScrollPane();
    JScrollPane jScrollPane3 = new JScrollPane();
    HashMap<String, String> thread4pdb = new HashMap<String, String>();
    static final int maxlen_qname = 8;

    class MyRunnable implements Runnable
    {
	String mylogfile = new String();
	MyRunnable(String mylogfile) {
	    this.mylogfile = mylogfile;
	}
	public void run() {
	    System.out.println("runnable thread");
	    boolean change = false;

	    while (check_log_stat(mylogfile) < 1) {
		if (!change) { thread4pdb.put(mylogfile, new String("run")); }
		change = true;
		try{
		    Thread.sleep(30*1000);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }

	    if (!change) return;

	    thread4pdb.put(mylogfile, new String("done"));
	    System.out.println("job finished "+mylogfile);
	    //	    updateBar(false);
	    String msg = "SkyLine results available";
	    publishStructureAnalysisEvent(new StructureAnalysisEvent(maSet, msg, outdir));
	}
    }


    public SkyLineComponent() {
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
        return structureparamPanel;
    }

    private void jbInit() throws Exception {
        infoLabel = new JLabel("");

	/*	pbar.setOrientation(JProgressBar.HORIZONTAL);
        pbar.setBorder(BorderFactory.createEtchedBorder());
        pbar.setMaximumSize(new Dimension(32767, 35));
        pbar.setMinimumSize(new Dimension(10, 35));
        pbar.setPreferredSize(new Dimension(104, 35));
        pbar.setStringPainted(false); 
	*/

        jPanel4.setLayout(borderLayout4);
        structureparamPanel.setLayout(borderLayout2);
        jPanel3.setLayout(borderLayout3);
//        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
//        buttons.setPreferredSize(new Dimension(248, 60));
        gridLayout1.setColumns(2);
        gridLayout1.setRows(3);
        gridLayout1.setVgap(0);
        gridLayout2.setColumns(4);
        gridLayout2.setRows(3);
        resetparam.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetparam_actionPerformed(e);
            }

        });
        submit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                submit_actionPerformed(e);
            }

        });
        checkResults.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkResults_actionPerformed(e);
            }

        });
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                save_actionPerformed(e);
            }

        });
        jPanel1.setLayout(gridLayout3);
        jPanel1.setMinimumSize(new Dimension(0, 0));
	//        jPanel1.setPreferredSize(new Dimension(50, 50));
        jPanel1.setPreferredSize(new Dimension(50, 40));
        jScrollPane1.setPreferredSize(new Dimension(248, 68));
        // Make sure that only one normalizer can be selected at a time;
        pluginStructureAnalysis.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pluginStructureAnalysis.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                structureAnalysisSelected_action(e);
            }

        });
        pluginStructureAnalysis.setBorder(BorderFactory.createLineBorder(Color.black));
        // Make sure that only one parameter set can be selected at a time;
        namedParameters.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        namedParameters.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                namedParameterSelection_action(e);
            }

        });
        namedParameters.setAutoscrolls(true);
        namedParameters.setBorder(BorderFactory.createLineBorder(Color.black));
        structureparamPanel.add(jScrollPane2, BorderLayout.CENTER);

        jScrollPane2.getViewport().add(jPanel3, null);
        currentParameterPanel.setLayout(borderLayout5);
        jPanel3.add(jPanel4, BorderLayout.WEST);
        jPanel4.add(currentParameterPanel, BorderLayout.CENTER);

//        buttons.add(Box.createHorizontalGlue());
//        buttons.add(Box.createRigidArea(new Dimension(10, 0)));
//        buttons.add(Box.createRigidArea(new Dimension(10, 0)));
//        buttons.add(save);
//        buttons.add(analyze);

        // Add buttons
        resetparam.setPreferredSize(save.getPreferredSize());
        submit.setPreferredSize(save.getPreferredSize());
        checkResults.setPreferredSize(save.getPreferredSize());
        FormLayout layout = new FormLayout("right:100dlu,10dlu","");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator("Structure Analysis Actions");
        builder.append(resetparam);
        builder.append(submit);
        builder.append(checkResults);
        builder.nextLine();
        builder.append(save);

	/*
	tbar.add(pbar);
        tbar.setMaximumSize(new Dimension(32767, 20));
        tbar.setMinimumSize(new Dimension(10, 20));
        tbar.setPreferredSize(new Dimension(200, 20));
        builder.append(tbar);
	*/

        jPanel3.add(builder.getPanel(), BorderLayout.EAST);

        jPanel3.add(jPanel1, BorderLayout.NORTH);
        jPanel1.add(jScrollPane1, null);
        jPanel1.add(jScrollPane3, null);
        jScrollPane3.getViewport().add(namedParameters, null);
        jScrollPane1.getViewport().add(pluginStructureAnalysis, null);
//        buttons.add(Box.createRigidArea(new Dimension(10, 0)));
//        jPanel3.add(buttons, BorderLayout.EAST);
    }


    /*
    public void updateBar(final boolean indt)
    {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    pbar.setIndeterminate(indt);
		}
	    });
    }
    */

    /**
     * Implementation of method from interface <code>ProjectListener</code>.
     * Handles notifications about change of the currently selected protein
     * structure.
     *
     * @param pe Project event cotnaining the newly selected protein structure.
     */
    @Subscribe public void receive(org.geworkbench.events.ProjectEvent pe, Object source) {
        DSDataSet dataSet = pe.getDataSet();

        if (dataSet instanceof DSProteinStructure) {

	    pname = dataSet.getLabel();
	    logfile = outdir+"/"+pname+"/ANALYSIS/"+pname+".log";
	    System.out.println("\ndataset is: "+dataSet);

            maSet = (DSProteinStructure) dataSet;
            reset();
	    int logstat = check_log_stat(logfile);
	    System.out.println(logstat + " "+ logfile);
	    if (logstat < 1 && thread4pdb.get(logfile) == null) {
		Thread t = new Thread(new MyRunnable(logfile));
		t.start();
	    }
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
        ProteinStructureAnalysis[] analyses = ComponentRegistry.getRegistry().getModules(ProteinStructureAnalysis.class);
        availableStructureAnalysis = new AbstractAnalysis[analyses.length];
        for (int i = 0; i < analyses.length; i++) {
            availableStructureAnalysis[i] = (AbstractAnalysis) analyses[i];
            if (selectedStructureAnalysis == availableStructureAnalysis[i])
                selectionChanged = false;
        }

        // If the selectedStructureAnalysis has been removed from the list of available
        // normalizers, reset.
        if (selectionChanged)
            if (analyses.length > 0)
                selectedStructureAnalysis = availableStructureAnalysis[0];
            else
                selectedStructureAnalysis = null;
    }

    /**
     * Obtains from the <code>PluginRegistry</code> ans displays the set of
     * available normalzers.
     */
    public void reset() {
        // Get the most recent available normalizers. Redisplay
        getAvailableAnalyses();
        displayStructureAnalysis();
    }

    /**
     * Displays the list of available normalizers.
     */
    private void displayStructureAnalysis() {
        // Show graphical components
        pluginStructureAnalysis.removeAll();
        // Stores the dispay names of the available normalizers.
        String[] names = new String[availableStructureAnalysis.length];
        for (int i = 0; i < availableStructureAnalysis.length; i++) {
            names[i] = availableStructureAnalysis[i].getLabel();
        }

        pluginStructureAnalysis.setListData(names);
        if (selectedStructureAnalysis != null)
            pluginStructureAnalysis.setSelectedValue(selectedStructureAnalysis.getLabel(), true);
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
        structureparamPanel.revalidate();
        structureparamPanel.repaint();
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
        structureparamPanel.revalidate();
    }

    /**
     * Listener invoked when a new normalizer is selected from the
     * displayed list of normalizers.
     *
     * @param lse The <code>ListSelectionEvent</code> received from the list
     *            selection.
     */
    private void structureAnalysisSelected_action(ListSelectionEvent lse) {
        if (pluginStructureAnalysis.getSelectedIndex() == -1)
            return;
        selectedStructureAnalysis = availableStructureAnalysis[pluginStructureAnalysis.getSelectedIndex()];
        // Set the parameters panel for the selected normalizer.
        ParameterPanel paramPanel = selectedStructureAnalysis.getParameterPanel();
        // Set the list of available named parameters for the selected normalizer.
        if (paramPanel != null) {
            setParametersPanel(paramPanel);
            setNamedParameters(availableStructureAnalysis[pluginStructureAnalysis.getSelectedIndex()].getNamesOfStoredParameterSets());
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
        if (selectedStructureAnalysis == null)
            return;
        int index = namedParameters.getSelectedIndex();
        if (index != -1)
            setParametersPanel(selectedStructureAnalysis.getNamedParameterSetPanel((String) namedParameters.getModel().getElementAt(index)));
    }


    private void resetparam_actionPerformed(ActionEvent e) {
        ParameterPanel paramPanel = selectedStructureAnalysis.getParameterPanel();
	((SkyLineConfigPanel)paramPanel).setDefaultParameters();
	setParametersPanel(paramPanel);
        structureparamPanel.revalidate();
    }
    /**
     * Listener invoked when the "submit job" button is pressed.
     *
     * @param e <code>ActionEvent</code> generated by the "submit" button
     */
    private void submit_actionPerformed(ActionEvent e) {
	if (handle_status(pname) == false)  return;

	//job execution stopped with error if log file isn't complete and there's no qjob on the cluster
	if (check_log_stat(logfile) == 0) {
	    int response = JOptionPane.showConfirmDialog(null, "SkyLine job exited with error. Are you sure to rerun the analysis?", "Structure Analysis Status", JOptionPane.YES_NO_OPTION);
	    if (response == 1) return;
	}
	//previous results detected
	else if (check_log_stat(logfile) == 1) {
	    int response = JOptionPane.showConfirmDialog(null, "Previous SkyLine results detected. Are you sure to rerun the analysis?", "Structure Analysis Status", JOptionPane.YES_NO_OPTION);
	    if (response == 1)  return;
	}
	// Invoke the skyline analysis
	AlgorithmExecutionResults results = selectedStructureAnalysis.execute(maSet);
	// If there were problems encountered, let the user know.
	if (!results.isExecutionSuccessful()) {
	    JOptionPane.showMessageDialog(null, results.getMessage(), "Structure Analysis Execution Error", JOptionPane.ERROR_MESSAGE);
	    return;
	}
	// If everything was OK, notify interested application components with
	// the results of the submission.
	String htmlText = (String) results.getResults();
	JOptionPane.showMessageDialog(null, htmlText, "Structure Analysis Execution Status", JOptionPane.INFORMATION_MESSAGE);
	publishStructureAnalysisEvent(new StructureAnalysisEvent(maSet, htmlText, outdir));
    }

    private void checkResults_actionPerformed(ActionEvent e) {
	if (handle_status(pname) == false)  return;


	//job execution stopped with error if log file isn't complete and there's no qjob on the cluster
	if (check_log_stat(logfile) == 0) {
	    JOptionPane.showMessageDialog(null, "SkyLine job exited with error", "Structure Analysis Status", JOptionPane.INFORMATION_MESSAGE);
	}
	//previous results detected
	else if (check_log_stat(logfile) == 1) {
	    String msg = "SkyLine results available";
	    JOptionPane.showMessageDialog(null, msg, "Structure Analysis Execution Status", JOptionPane.INFORMATION_MESSAGE);
	    //	    publishStructureAnalysisEvent(new StructureAnalysisEvent(maSet, msg, outdir));
	}
	//job hasn't been run before
	else if (check_log_stat(logfile) == -1) {
	    JOptionPane.showMessageDialog(null, "SkyLine job needs to be submitted", "Structure Analysis Status", JOptionPane.INFORMATION_MESSAGE);
	}
    }

    //handle status
    private boolean handle_status(String pname)
    {
        if (selectedStructureAnalysis == null || maSet == null)   return false;
        ParamValidationResults pvr = selectedStructureAnalysis.validateParameters();
        if (!pvr.isValid()) {
            JOptionPane.showMessageDialog(null, pvr.getMessage(), "Parameter Validation Error", JOptionPane.ERROR_MESSAGE);
	    return false;
	}

	String status = get_qstat(pname);
	//no connection to webserver
	if (status.equals("not connected")) {
	    JOptionPane.showMessageDialog(null, "Cannot connect to SkyLine webserver", "Structure Analysis Status", JOptionPane.ERROR_MESSAGE);
	    return false;
	}
	//job is running if the qjob is on the cluster
	if (status.length() > 0) {
	    JOptionPane.showMessageDialog(null, "<html>SkyLine job hasn't finished<hr>"+status, "Structure Analysis Status", JOptionPane.INFORMATION_MESSAGE);
	    return false;
	}
	return true;
    }

    private String get_qstat(String pname)
    {
	String status = new String();
	pname = pname.substring(0, maxlen_qname);
	try{
	    status = ((SkyLineAnalysis)selectedStructureAnalysis).getJobStatus(pname);
	} catch (Exception ce) {
	    ce.printStackTrace();
	}
	System.out.println("getjobstatus for "+pname+": "+status);
	if (status.length() > 0 && !status.equals("not connected")) {
	    status = "Job-ID  Priority  Job-Name  User  State  Submit/Start at   Queue  Master   ja-task-ID"+status;
	    status = status.replaceAll("-0-", "-0");
	    status = status.replaceAll("\\b\\s+\\b", "</td><td>");
	    status = status.replaceAll("task-ID", "task-ID</tr><tr>");
	    status = "<table><tr><td>"+status;
	}
	return status;
    }
    // log file status
    // 1 finished; 0 running; -1 no record
    private int check_log_stat(String logfile)
    {
	String line = null; String tmp = null; String prev = null;
	try {
	    URL url = new URL(logfile);
	    URLConnection uc = url.openConnection();
	    if (uc.getContentLength() <= 0 ||
		((HttpURLConnection)uc).getResponseCode() == 404) 
		{ return -1; }
	    BufferedReader log = new BufferedReader(new InputStreamReader(uc.getInputStream()));
	    while ((tmp = log.readLine()) != null) { prev = line; line = tmp; }
	}catch (Exception e) {
	    e.printStackTrace();
	}
	if (prev != null && prev.endsWith("starts at 1.")) { return 1; }
	return 0;
    }


    //    @Publish public org.geworkbench.events.NormalizationEvent publishNormalizationEvent(org.geworkbench.events.NormalizationEvent event) {
    @Publish public org.geworkbench.events.StructureAnalysisEvent
	publishStructureAnalysisEvent(org.geworkbench.events.StructureAnalysisEvent event) {
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

        String paramName = JOptionPane.showInputDialog(structureparamPanel, namedParameter, namedParameter);
        if (selectedStructureAnalysis != null && paramName != null) {
            selectedStructureAnalysis.saveParametersUnderName(paramName);
            setNamedParameters(selectedStructureAnalysis.getNamesOfStoredParameterSets());
        }

    }

}
