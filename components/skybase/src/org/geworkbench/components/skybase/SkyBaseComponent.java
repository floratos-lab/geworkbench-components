package org.geworkbench.components.skybase;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.DefaultFormBuilder;

import edu.columbia.geworkbench.cagrid.dispatcher.client.DispatcherClient;

import org.apache.commons.lang.StringUtils;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.engine.management.ComponentRegistry;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.events.ProjectNodeCompletedEvent;
import org.geworkbench.events.ProjectNodePendingEvent;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.geworkbench.bison.model.analysis.ProteinDatabaseAnalysis;
import org.ginkgo.labs.ws.GridEndpointReferenceType;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.io.*;

/*skybase component: display homologous models in skybase for an input sequence*/

@AcceptTypes({CSSequenceSet.class})
public class SkyBaseComponent extends JPanel implements VisualPlugin
{
    protected JPanel skybaseparamPanel = new JPanel();
    protected AbstractGridAnalysis[] availableSkyBaseAnalysis;
    protected DSSequenceSet seq = null;
    protected AbstractGridAnalysis selectedSkyBaseAnalysis = null;
    protected JList pluginSkyBaseAnalysis = new JList();
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

    private List<Thread> threadList=new ArrayList<Thread>();
    private static final String GRID_HOST_KEY = "web1DispatcherURL";
    private static final String DISPATCHER_URL = "web1Dispatcher.url";
    private String web1DispatcherUrl = System.getProperty(DISPATCHER_URL);
    private static final String skybaseweb = "http://156.145.238.15:8070/wsrf/services/cagrid/SkyBaseWeb";
    private static final String username = "cagrid", passwd="cagrid123", USER_INFO_DELIMIETER="==";

    public SkyBaseComponent()
    {
	try{
	    jbInit();
	} catch(Exception e){
	    e.printStackTrace();
	}
	reset();
    }

    public Component getComponent()
    {
	return skybaseparamPanel;
    }

    private void jbInit() throws Exception
    {
        jPanel4.setLayout(borderLayout4);
        skybaseparamPanel.setLayout(borderLayout2);
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
        pluginSkyBaseAnalysis.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pluginSkyBaseAnalysis.addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) 
		{
		    skybaseAnalysisSelected_action(e);
		}
	    });
        pluginSkyBaseAnalysis.setBorder(BorderFactory.createLineBorder(Color.black));
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
        skybaseparamPanel.add(jScrollPane2, BorderLayout.CENTER);

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
	//        builder.append(checkResults);
        builder.nextLine();
        builder.append(save);

        jPanel3.add(builder.getPanel(), BorderLayout.EAST);

        jPanel3.add(jPanel1, BorderLayout.NORTH);
        jPanel1.add(jScrollPane1, null);
        jPanel1.add(jScrollPane3, null);
        jScrollPane3.getViewport().add(namedParameters, null);
        jScrollPane1.getViewport().add(pluginSkyBaseAnalysis, null);

    }

    @Subscribe 
    public void receive(org.geworkbench.events.ProjectEvent pe,Object source)
    {
	DSDataSet dataSet = pe.getDataSet();

	if (dataSet instanceof DSSequenceSet)
	{
	    seq = (DSSequenceSet)dataSet;
	    reset();
	}
    }

    public void getAvailableAnalyses()
    {
	boolean selectionChanged = true;
	ProteinDatabaseAnalysis[] analyses = ComponentRegistry.getRegistry().getModules(ProteinDatabaseAnalysis.class);
	availableSkyBaseAnalysis = new AbstractGridAnalysis[analyses.length];
	for (int i = 0; i < analyses.length; i++)
	{
	    availableSkyBaseAnalysis[i] = (AbstractGridAnalysis) analyses[i];
	    if (selectedSkyBaseAnalysis == availableSkyBaseAnalysis[i])
		selectionChanged = false;
	}

	if (selectionChanged)
	    if (analyses.length > 0)
		selectedSkyBaseAnalysis = availableSkyBaseAnalysis[0];
	    else
		selectedSkyBaseAnalysis = null;
    }

    public void reset()
    {
	getAvailableAnalyses();
	displaySkyBaseAnalysis();
    }

    private void displaySkyBaseAnalysis()
    {
	pluginSkyBaseAnalysis.removeAll();
	String[] names = new String[availableSkyBaseAnalysis.length];
	for (int i = 0; i < availableSkyBaseAnalysis.length; i++)
	{
	    names[i] = availableSkyBaseAnalysis[i].getLabel();
	}

	pluginSkyBaseAnalysis.setListData(names);
	if (selectedSkyBaseAnalysis != null)
	{
	    pluginSkyBaseAnalysis.setSelectedValue(selectedSkyBaseAnalysis.getLabel(), true);
	}
	else
	{
	    setParametersPanel(this.emptyParameterPanel);
	    save.setEnabled(false);
	}
    }

    private void setParametersPanel(ParameterPanel parameterPanel)
    {
	jPanel4.remove(currentParameterPanel);
	currentParameterPanel = parameterPanel;
	jPanel4.add(currentParameterPanel, BorderLayout.CENTER);
	skybaseparamPanel.revalidate();
	skybaseparamPanel.repaint();
    }

    private void setNamedParameters(String[] storedParameters)
    {
	namedParameters.removeAll();
	namedParameters.setListData(storedParameters);
	namedParameters.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	skybaseparamPanel.revalidate();
    }

    private void skybaseAnalysisSelected_action(ListSelectionEvent lse)
    {
	if (pluginSkyBaseAnalysis.getSelectedIndex() == -1)
	    return;
	selectedSkyBaseAnalysis = availableSkyBaseAnalysis[pluginSkyBaseAnalysis.getSelectedIndex()];
	ParameterPanel paramPanel = selectedSkyBaseAnalysis.getParameterPanel();
	if (paramPanel != null)
	{
	    setParametersPanel(paramPanel);
	    setNamedParameters(availableSkyBaseAnalysis[pluginSkyBaseAnalysis.getSelectedIndex()].getNamesOfStoredParameterSets());
	    save.setEnabled(true);
	}
	else
	{
	    setParametersPanel(this.emptyParameterPanel);
	    save.setEnabled(false);
	    setNamedParameters(new String[0]);
	}
    }

    private void namedParameterSelection_action(ListSelectionEvent e)
    {
	if (selectedSkyBaseAnalysis == null)
	    return;
	int index = namedParameters.getSelectedIndex();
	if (index != -1)
	    setParametersPanel(selectedSkyBaseAnalysis.getNamedParameterSetPanel((String) namedParameters.getModel().getElementAt(index)));
    }

    private void resetparam_actionPerformed(ActionEvent e)
    {
	ParameterPanel paramPanel = selectedSkyBaseAnalysis.getParameterPanel();
	((SkyBaseConfigPanel)paramPanel).setDefaultParameters();
	setParametersPanel(paramPanel);
	skybaseparamPanel.revalidate();
    }

    public void submit_actionPerformed(ActionEvent e)
    {

	String userInfo = username + USER_INFO_DELIMIETER + passwd;
	System.out.println(userInfo);
	((SkyBaseAnalysis)selectedSkyBaseAnalysis).set_seqfile(seq);
	try{
	    List<Serializable> serviceParameterList = selectedSkyBaseAnalysis.handleBisonInputs(null, seq);
	    serviceParameterList.add(userInfo);
	    DispatcherClient dispatcherClient = new DispatcherClient(web1DispatcherUrl);
	    GridEndpointReferenceType gridEpr = dispatcherClient.submit(
									serviceParameterList, skybaseweb, selectedSkyBaseAnalysis.getBisonReturnType());

	    ProjectNodePendingEvent pendingEvent = new ProjectNodePendingEvent("Analysis Pending", gridEpr);
	    pendingEvent.setDescription(selectedSkyBaseAnalysis.getLabel()+" (pending)");

	    String history = "";
	    history += selectedSkyBaseAnalysis.createHistory();
	    pendingEvent.setHistory(history);
	    publishProjectNodePendingEvent(pendingEvent);
	    PollingThread pollingThread = new PollingThread(this, gridEpr, dispatcherClient);
	    threadList.add(pollingThread);
	    pollingThread.start();
	}catch(Exception ex){
	    ex.printStackTrace();
	}
    }

    @Publish
    public ProjectNodePendingEvent publishProjectNodePendingEvent(
								  ProjectNodePendingEvent event) {
	return event;
    }

    @Publish
	public ProjectNodeCompletedEvent publishProjectNodeCompletedEvent(
									  ProjectNodeCompletedEvent event)
    {
	return event;
    }


    private void checkResults_actionPerformed(ActionEvent e){}

    @Subscribe
    public void receive(
			org.geworkbench.events.PendingNodeLoadedFromWorkspaceEvent ppne, Object source)
    {
	DispatcherClient dispatcherClient = null;
	try{
	    PropertiesManager pm = PropertiesManager.getInstance();
	    String savedHost = null;
	    try{
		savedHost = pm.getProperty(this.getClass(), GRID_HOST_KEY, web1DispatcherUrl);
		if (!StringUtils.isEmpty(savedHost)){
		    web1DispatcherUrl = savedHost;
		}
	    } catch(IOException e){
		e.printStackTrace();
	    }
	    dispatcherClient = new DispatcherClient(web1DispatcherUrl);
	}catch(Exception e){
	    e.printStackTrace();
	}

	Collection<GridEndpointReferenceType> gridEprs = ppne.getGridEprs();
	for (GridEndpointReferenceType gridEpr : gridEprs)
	{
	    PollingThread pollingThread = new PollingThread(this,
					    gridEpr, dispatcherClient);
	    threadList.add(pollingThread);
	    pollingThread.start();
	}
    }

    @Subscribe
    public void receive(org.geworkbench.events.PendingNodeCancelledEvent e, Object source)
    {
	for (Iterator iterator = threadList.iterator(); iterator.hasNext();)
	{
	    PollingThread element = (PollingThread) iterator.next();
	    if (element.getGridEPR()==e.getGridEpr()){
		element.cancel();
	    }
	}
    }

    private void save_actionPerformed(ActionEvent e)
    {
	int index = namedParameters.getSelectedIndex();
	String namedParameter = null;
	if (index != -1)
	{
	    namedParameter = (String)namedParameters.getModel().getElementAt(index);
	    if(currentParameterPanel.isDirty())
		namedParameter = "New Parameter Setting Name";
	}
	else
	    namedParameter = "New Parameter Setting Name";

	String paramName = JOptionPane.showInputDialog(skybaseparamPanel,namedParameter, namedParameter);
	if (selectedSkyBaseAnalysis != null && paramName != null)
	{
	    selectedSkyBaseAnalysis.saveParametersUnderName(paramName);
	    setNamedParameters(selectedSkyBaseAnalysis.getNamesOfStoredParameterSets());
	}
    }
}
