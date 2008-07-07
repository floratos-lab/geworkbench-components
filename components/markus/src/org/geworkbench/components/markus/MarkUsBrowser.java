package org.geworkbench.components.markus;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbench.bison.datastructure.bioobjects.structure.CSProteinStructure;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.AnnotationAnalysisEvent;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.File;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.ChangeEvent;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.net.*;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.jdesktop.jdic.browser.*;

@AcceptTypes({DSProteinStructure.class})
public class MarkUsBrowser implements VisualPlugin
{    
    private DSProteinStructure proteinData;
    private JPanel mainPanel = new JPanel(new BorderLayout());
    private JPanel jp = new JPanel(new BorderLayout());
    public static CloseableTabbedPane jtp = new CloseableTabbedPane();
    private TabBrowser tb;
    private boolean finish = false;
    public String tabtitle = null;
    
    MyStatusBar statusBar = new MyStatusBar();
    boolean initial=true, link=true;
    HashMap<DSProteinStructure, String> musid4prt = new HashMap<DSProteinStructure, String>();
    String process_id = null, lastpid = null;
    int tc=0;
    // set true for jdic to use IE browser; false for Mozilla(FIXME: LINK in TAB NOT WORKING)
    boolean useIE = true;

    @Subscribe
    public void receive(AnnotationAnalysisEvent event, Object source) {
	DSDataSet dataset = event.getDataSet();
	if (dataset instanceof DSProteinStructure) {
	    link = false;
	    proteinData = (DSProteinStructure) dataset;
	    String status = event.getAnalyzedStructure();
	    process_id = event.getInformation();
	    System.out.println("processid: "+process_id+" ; status: "+status);
	    musid4prt.put(proteinData, process_id);
	    if (status.equals("MarkUs results available") && process_id.startsWith("MUS")){
		showResults(process_id);
	    }
	}
    }
    
    @Subscribe
    public void receive(ProjectEvent event, Object source) {
	DSDataSet dataset = event.getDataSet();
	if (dataset instanceof DSProteinStructure) {
	    link = false;
	    proteinData = (DSProteinStructure) dataset;
	    System.out.println("prtdata: "+proteinData + " " +musid4prt.get(proteinData));
	    ////	    System.out.println("initial: "+initial);
	    if (!initial)
	    {
	    	if ((tc = jtp.getTabCount()) > 1)
	    		for (int i = tc-1; i > 0; i--)   jtp.remove(i);
	    }
	    if (musid4prt.get(proteinData) != null)
	    {
		lastpid = process_id;
		process_id = musid4prt.get(proteinData);
		System.out.println(tb.isInitialized()+" "+process_id+" "+proteinData+" "+lastpid);

		if (process_id.startsWith("MUS") && tb.isInitialized() && !lastpid.equals(process_id))
		    showResults(process_id);
	    }
	    else if (!initial)
	    {
		tb.setContent("<html><head><title>blank</title></head><body></body></html>");
		////		System.out.println("content: "+tb.getContent());
	    }
	}
    }
    
    private void showResults(String process_id)
    {
    	String url = "http://luna.bioc.columbia.edu/honiglab/mark-us/cgi-bin/browse.pl?pdb_id="+process_id;

	try {
	    jbInit(url);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    boolean checkJobFinish(String url)
    {
    	try{
	    URLConnection uc = new URL(url).openConnection();
	    if (!uc.getContentType().startsWith("text/html")) { return false; }
	    BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
	    String line = null;
	    while((line = in.readLine()) != null)
	    {
		if (line.indexOf("<div class=\"error\">") > -1 )
		{
		    System.out.println(line);
		    return false;
		}
	    }
    	}catch (Exception e){
	    e.printStackTrace();
    	}
	return true;
    }

    private void jbInit(String url)
    {
	//    	String url = "http://luna.bioc.columbia.edu/honiglab/mark-us/cgi-bin/browse.pl?stralign=on&method=skan&chain_id=21136";
	//    	String url = "http://luna.bioc.columbia.edu/honiglab/mark-us/cgi-bin/browse.pl?pdb_id="+process_id;
    	finish = checkJobFinish(url);
    	if (!finish) { mainPanel.removeAll(); mainPanel.revalidate(); mainPanel.repaint(); return; }
    	
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        statusBar.lblDesc.setText("JDIC Browser");

	if (!initial)
	    tb.setContent("<html><head><title>blank</title></head><body></body></html>");
    	try{
	    if (initial)
	    {
		initial = false;
		BrowserEngineManager bem = BrowserEngineManager.instance();
		if (bem.getActiveEngine() != bem.getEngines().get(BrowserEngineManager.IE))
		{ 
		    //set jdic to use IE or Mozilla browser
		    if (useIE == true) {
			bem.setActiveEngine(BrowserEngineManager.IE);
		    } else {
			bem.setActiveEngine(BrowserEngineManager.MOZILLA);
			IBrowserEngine be = bem.getActiveEngine();
			be.setEnginePath("C:\\Program Files\\mozilla\\mozilla.exe");
		    }
		}
		
		// Print out debug messages in the command line.
		//            tb.setDebug(true);
		tb = new TabBrowser(new URL(url), useIE);
		tb.addWebBrowserListener(new WebTabListener());
		tb.setMainBrowser(this);

		jp.add(tb, BorderLayout.CENTER);
		jtp.addTab(tabtitle, jp);

		mainPanel.add(jtp, BorderLayout.CENTER);
		mainPanel.add(statusBar, BorderLayout.SOUTH);
	    }
    	}catch (Exception e){
	    System.out.println(e.getMessage());
    	}
    	mainPanel.revalidate(); mainPanel.repaint(); 
    }

    public JTabbedPane getTabbedPane()
    {
    	return jtp;
    }
    public Component getComponent() 
    {
	return mainPanel;
    }


    //web browser listener 
    public class WebTabListener implements WebBrowserListener
    {
	public void downloadStarted(WebBrowserEvent event)
	{
	    updateStatusInfo("Loading started.");
	}
	
	public void initializationCompleted(WebBrowserEvent event)
	{
	    // set blank page if no results
	    if (musid4prt.get(proteinData) == null) 
	    {
		tb.setContent("<html><head><title>blank</title></head><body></body></html>");
	    }
	}

	public void downloadCompleted(WebBrowserEvent event)
	{
	    updateStatusInfo("Loading completed.");
	}
	
	public void downloadError(WebBrowserEvent event)
	{
	    updateStatusInfo("Loading error.");
	}
	
	public void documentCompleted(WebBrowserEvent event)
	{
	    if (link == true) return;

	    //set url or content in this function to get the page displayed
	    String url = "http://luna.bioc.columbia.edu/honiglab/mark-us/cgi-bin/browse.pl?pdb_id="+process_id;

	    try{
		if (tb.getURL() != null) {
		    URL currentUrl = tb.getURL();
		    String curl = currentUrl.toString();
		    System.out.println("\nfrom "+curl+ " to "+url);
		    if (!url.equals(curl))
		    {
			System.out.println("isinitialized: "+ tb.isInitialized() 
					   + "; newurl: " + url);
			
			tb.setURL(new URL(url));
		    }
		}
	    }catch(Exception e){e.printStackTrace();}
	    
	    updateStatusInfo("Document loading completed.");
	    link = true;
	}
	
	public void downloadProgress(WebBrowserEvent webEvent)
	{
	    String values = webEvent.getData();
	    if (values != null)
	    {
		StringTokenizer tokenizer = new StringTokenizer(values,
								" ");
		
		if (tokenizer.hasMoreTokens())
		{
		    String current = tokenizer.nextToken();
		    if (tokenizer.hasMoreTokens())
		    {
			String max = tokenizer.nextToken();
		
			int progress = Integer.parseInt(current);
			int progressMax = Integer.parseInt(max);
		
			onDownloadProgress(webEvent, progress,
					   progressMax);
		    }
		}
	    }
	}
	
	protected void onDownloadProgress(WebBrowserEvent webEvent, int progress, int progressMax)
	{
	}

	public void windowClose(WebBrowserEvent event)
	{
	    updateStatusInfo("Closed by script.");
	    System.out.println("closed by script."+event.getData());
	    if(JOptionPane.YES_OPTION==JOptionPane.showConfirmDialog(
								     tb,
								     "The webpage you are viewing is trying to close the window.\n Do you want to close this window?",
								     "Warning",
								     JOptionPane.YES_NO_OPTION,
								     JOptionPane.QUESTION_MESSAGE))
	    {
		System.exit(0);
	    }
	}

	public void titleChange(WebBrowserEvent event)
	{
	    tabtitle = event.getData();
	    jtp.setTitleAt(jtp.getTabCount()-1, tabtitle);
	    updateStatusInfo("Title of the browser window changed.");
	}

	public void statusTextChange(WebBrowserEvent event)
	{
	}
    }

    void updateStatusInfo(String statusMessage) {
	statusBar.lblStatus.setText(statusMessage);
    }   
}
