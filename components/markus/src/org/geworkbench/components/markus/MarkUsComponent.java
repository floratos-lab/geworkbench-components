package org.geworkbench.components.markus;

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
import org.geworkbench.events.AnnotationAnalysisEvent;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.geworkbench.bison.model.analysis.ProteinAnnotationAnalysis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.io.*;
import java.net.*;

/**
 * This is markus component for protein function annotation analysis
 *
 */
// This annotation lists the data set types that this component accepts.
// The component will only appear when a data set of the appropriate type is selected.

@AcceptTypes({DSProteinStructure.class})
public class MarkUsComponent extends JPanel implements VisualPlugin 
{
    private JLabel infoLabel;
    private String pname;
    private String logfile;
    protected JPanel annotationparamPanel = new JPanel();
    protected AbstractAnalysis[] availableAnnotationAnalysis;
    protected DSProteinStructure prt = null;
    protected AbstractAnalysis selectedAnnotationAnalysis = null;
    protected JList pluginAnnotationAnalysis = new JList();
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
    GridLayout gridLayout3 = new GridLayout();
    JScrollPane jScrollPane1 = new JScrollPane();
    JScrollPane jScrollPane3 = new JScrollPane();
    HashMap<String, String> thread4pdb = new HashMap<String, String>();

    class MyRunnable implements Runnable
    {
	String myurl = new String();
	String process_id = null;
	MyRunnable(String myurl, String process_id) {
	    this.myurl = myurl;
	    this.process_id = process_id;
	}
	public void run() 
	{
	    boolean change = false;
	    while (check_url_stat(myurl) < 1)
	    {
		if (!change) { thread4pdb.put(myurl, new String("run")); }
		change = true;
		try{
		    Thread.sleep(30*1000);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	    if (!change) return;

	    thread4pdb.put(myurl, new String("done"));
	    String msg = "MarkUs results available";
	    publishAnnotationAnalysisEvent(new AnnotationAnalysisEvent(prt, msg, process_id));
	}
    }

    //url status: 1 finished; 0 pending; -1 no record
    private int check_url_stat(String url)
    {
	String line = null; String tmp = null;
	try{
	    URLConnection uc = new URL(url).openConnection();
	    //redirected cgi page return content length -1
	    if (((HttpURLConnection)uc).getResponseCode() == 404)
	    { return -1; }
	    BufferedReader log = new BufferedReader(new InputStreamReader(uc.getInputStream()));
	    while((tmp = log.readLine()) != null)
	    {
		if (tmp.indexOf("<div class=\"error\">") > -1) { return 0; }
	    }
	}catch (Exception e){
	    e.printStackTrace();
	}
	return 1;
    }

    public MarkUsComponent() 
    {
        try 
	{
            jbInit();
        } catch (Exception e) 
	{
            e.printStackTrace();
        }
        // Get (and display) the available analysis from the PluginRegistry
        reset();
    }

    /**
     * Implementation of method from interface <code>VisualPlugin</code>.
     *
     * @return
     */
    public Component getComponent() 
    {
        return annotationparamPanel;
    }

    private void jbInit() throws Exception 
    {
        infoLabel = new JLabel("");

        jPanel4.setLayout(borderLayout4);
        annotationparamPanel.setLayout(borderLayout2);
        jPanel3.setLayout(borderLayout3);
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
		public void actionPerformed(ActionEvent e) 
		{
		    submit_actionPerformed(e);
		}
	    });
        checkResults.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) 
		{
		    checkResults_actionPerformed(e);
		}
	    });
        save.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) 
		{
		    save_actionPerformed(e);
		}
	    });
        jPanel1.setLayout(gridLayout3);
        jPanel1.setMinimumSize(new Dimension(0, 0));
        jPanel1.setPreferredSize(new Dimension(50, 20));
        jScrollPane1.setPreferredSize(new Dimension(248, 68));
        pluginAnnotationAnalysis.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pluginAnnotationAnalysis.addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) 
		{
		    annotationAnalysisSelected_action(e);
		}
	    });
        pluginAnnotationAnalysis.setBorder(BorderFactory.createLineBorder(Color.black));
        // Make sure that only one parameter set can be selected at a time;
        namedParameters.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        namedParameters.addListSelectionListener(new ListSelectionListener() 
	    {
		public void valueChanged(ListSelectionEvent e) {
		    namedParameterSelection_action(e);
		}
	    });
        namedParameters.setAutoscrolls(true);
        namedParameters.setBorder(BorderFactory.createLineBorder(Color.black));
        annotationparamPanel.add(jScrollPane2, BorderLayout.CENTER);

        jScrollPane2.getViewport().add(jPanel3, null);
        currentParameterPanel.setLayout(borderLayout5);
        jPanel3.add(jPanel4, BorderLayout.WEST);
        jPanel4.add(currentParameterPanel, BorderLayout.CENTER);

        // Add buttons
        resetparam.setPreferredSize(save.getPreferredSize());
        submit.setPreferredSize(save.getPreferredSize());
        checkResults.setPreferredSize(save.getPreferredSize());
        FormLayout layout = new FormLayout("right:100dlu,10dlu","");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator("Annotation Analysis Actions");
        builder.append(resetparam);
        builder.append(submit);
        builder.append(checkResults);
        builder.nextLine();
        builder.append(save);

        jPanel3.add(builder.getPanel(), BorderLayout.EAST);

        jPanel3.add(jPanel1, BorderLayout.NORTH);
        jPanel1.add(jScrollPane1, null);
        jPanel1.add(jScrollPane3, null);
        jScrollPane3.getViewport().add(namedParameters, null);
        jScrollPane1.getViewport().add(pluginAnnotationAnalysis, null);
    }


    /**
     * Implementation of method from interface <code>ProjectListener</code>.
     * Handles notifications about change of the currently selected protein
     * structure.
     *
     * @param pe Project event containing the newly selected protein structure.
     */
    @Subscribe public void receive(org.geworkbench.events.ProjectEvent pe, Object source) 
    {
        DSDataSet dataSet = pe.getDataSet();

        if (dataSet instanceof DSProteinStructure)
        {
	    prt = (DSProteinStructure) dataSet;
	    reset();

        }
    }

    /*
    @Subscribe public void receive(org.geworkbench.events.AnnotationAnalysisEvent pe, Object source) 
    {
	//no annotatoinanalysisevent, opening a pdb file can't link to a MUSid
        DSDataSet dataSet = pe.getDataSet();

        if (dataSet instanceof DSProteinStructure)
        {
	    prt = (DSProteinStructure) dataSet;
	    reset();

	    process_id = pe.getInformation();
	    if (process_id.startsWith("MUS"))
	    {
		String url = "http://luna.bioc.columbia.edu/honiglab/mark-us/cgi-bin/browse.pl?pdb_id="+process_id;
		int urlstat = check_url_stat(url);
		System.out.println(urlstat + " " + url);
		if (urlstat < 1 && thread4pdb.get(url) == null) 
		{
		    Thread t = new Thread(new MyRunnable(url));
		    t.start();
		}
	    }
        }
    }
    */


    // This method gets invoked every time that the annotation analysis
    // panel gets the focus, in order to get the most recent list of analysis:
    // given dynamic loading of components this approach guarantees that any new
    // plugins loaded between uses of the analysis panel, will be correctly
    // picked up.
    public void getAvailableAnalyses() {
        boolean selectionChanged = true;
        ProteinAnnotationAnalysis[] analyses = ComponentRegistry.getRegistry().getModules(ProteinAnnotationAnalysis.class);
        availableAnnotationAnalysis = new AbstractAnalysis[analyses.length];
        for (int i = 0; i < analyses.length; i++) {
            availableAnnotationAnalysis[i] = (AbstractAnalysis) analyses[i];
            if (selectedAnnotationAnalysis == availableAnnotationAnalysis[i])
                selectionChanged = false;
        }

        // If the selectedAnnotationAnalysis has been removed from the list of available
        // analysis, reset.
        if (selectionChanged)
            if (analyses.length > 0)
                selectedAnnotationAnalysis = availableAnnotationAnalysis[0];
            else
                selectedAnnotationAnalysis = null;
    }

    /**
     * Obtains from the <code>PluginRegistry</code> ans displays the set of
     * available analysis
     */
    public void reset() {
        // Get the most recent available analysis. Redisplay
        getAvailableAnalyses();
        displayAnnotationAnalysis();
    }

    /**
     * Displays the list of available analysis
     */
    private void displayAnnotationAnalysis() {
        // Show graphical components
        pluginAnnotationAnalysis.removeAll();
        // Stores the display names of the available analysis
        String[] names = new String[availableAnnotationAnalysis.length];
        for (int i = 0; i < availableAnnotationAnalysis.length; i++) {
            names[i] = availableAnnotationAnalysis[i].getLabel();
        }

        pluginAnnotationAnalysis.setListData(names);
        if (selectedAnnotationAnalysis != null){
            pluginAnnotationAnalysis.setSelectedValue(selectedAnnotationAnalysis.getLabel(), true);
        }
        else {
            setParametersPanel(this.emptyParameterPanel);
            save.setEnabled(false);
        }
    }

    /**
     * Set the parameters panel used in the analysis pane.
     *
     * @param parameterPanel
     */
    private void setParametersPanel(ParameterPanel parameterPanel) {
        jPanel4.remove(currentParameterPanel);
        currentParameterPanel = parameterPanel;
        jPanel4.add(currentParameterPanel, BorderLayout.CENTER);
        annotationparamPanel.revalidate();
        annotationparamPanel.repaint();
    }

    /**
     * Update the list that shows the known preset parameter settings for the
     * selected analysis
     *
     * @param storedParameters
     */
    private void setNamedParameters(String[] storedParameters) {
        namedParameters.removeAll();
        namedParameters.setListData(storedParameters);
        // Make sure that only one parameter set can be selected at a time;
        namedParameters.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        annotationparamPanel.revalidate();
    }

    /**
     * Listener invoked when a new analysis is selected from the
     * displayed list of analysis.
     *
     * @param lse The <code>ListSelectionEvent</code> received from the list
     *            selection.
     */
    private void annotationAnalysisSelected_action(ListSelectionEvent lse) {
        if (pluginAnnotationAnalysis.getSelectedIndex() == -1)
            return;
        selectedAnnotationAnalysis = availableAnnotationAnalysis[pluginAnnotationAnalysis.getSelectedIndex()];
        // Set the parameters panel for the selected analysis.
        ParameterPanel paramPanel = selectedAnnotationAnalysis.getParameterPanel();
        // Set the list of available named parameters for the selected analysis
        if (paramPanel != null) {
            setParametersPanel(paramPanel);
            setNamedParameters(availableAnnotationAnalysis[pluginAnnotationAnalysis.getSelectedIndex()].getNamesOfStoredParameterSets());
            save.setEnabled(true);
        } else {
            setParametersPanel(this.emptyParameterPanel);
            save.setEnabled(false);
            // Since the analysis admits no parameters, there are no named parameter
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
        if (selectedAnnotationAnalysis == null)
            return;
        int index = namedParameters.getSelectedIndex();
        if (index != -1)
            setParametersPanel(selectedAnnotationAnalysis.getNamedParameterSetPanel((String) namedParameters.getModel().getElementAt(index)));
    }


    private void resetparam_actionPerformed(ActionEvent e) {
        ParameterPanel paramPanel = selectedAnnotationAnalysis.getParameterPanel();
	((MarkUsConfigPanel)paramPanel).setDefaultParameters();
	setParametersPanel(paramPanel);
        annotationparamPanel.revalidate();
    }

    /**
     * Listener invoked when the "submit job" button is pressed.
     *
     * @param e <code>ActionEvent</code> generated by the "submit" button
     */
    private void submit_actionPerformed(ActionEvent e) {

		// Invoke the markus analysis
		AlgorithmExecutionResults results = selectedAnnotationAnalysis.execute(prt);
		// If there were problems encountered, let the user know.
		if (!results.isExecutionSuccessful()) {
		    JOptionPane.showMessageDialog(null, results.getMessage(), "Annotation Analysis Execution Error", JOptionPane.ERROR_MESSAGE);
		    return;
		}
		// If everything was OK, notify interested application components with
		// the results of the submission.
		String process_id = (String) results.getResults();
		if (process_id.equals("cancelled")) { return; }
		if (process_id.equals("na")) 
		{ 
		    JOptionPane.showMessageDialog(null, "<html>Please check if the MarkUs server is running then retry<br>http://luna.bioc.columbia.edu/honiglab/mark-us/cgi-bin/submit.pl</html>", "Submission Failed", JOptionPane.ERROR_MESSAGE);
		    return;
		}

		JOptionPane.showMessageDialog(null, process_id, "Annotation Analysis Execution Status", JOptionPane.INFORMATION_MESSAGE);
		publishAnnotationAnalysisEvent(new AnnotationAnalysisEvent(prt, "MarkUs job submitted", process_id));

		//start waiting for this job's results
		String url = "http://luna.bioc.columbia.edu/honiglab/mark-us/cgi-bin/browse.pl?pdb_id="+process_id;
		int urlstat = check_url_stat(url);
		System.out.println(urlstat + " " + url);
		if (urlstat < 1 && thread4pdb.get(url) == null) 
		{
		    Thread t = new Thread(new MyRunnable(url, process_id));
		    t.start();
		}
		else if (urlstat == 1)
		{
		    publishAnnotationAnalysisEvent(new AnnotationAnalysisEvent(prt, "MarkUs results available", process_id));
		}
    }

    private void checkResults_actionPerformed(ActionEvent e) {

    }

    @Publish public org.geworkbench.events.AnnotationAnalysisEvent
	publishAnnotationAnalysisEvent(org.geworkbench.events.AnnotationAnalysisEvent event) {
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

        String paramName = JOptionPane.showInputDialog(annotationparamPanel, namedParameter, namedParameter);
        if (selectedAnnotationAnalysis != null && paramName != null) {
            selectedAnnotationAnalysis.saveParametersUnderName(paramName);
            setNamedParameters(selectedAnnotationAnalysis.getNamesOfStoredParameterSets());
        }

    }

}
