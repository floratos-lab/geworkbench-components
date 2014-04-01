package org.geworkbench.components.markus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbench.bison.datastructure.bioobjects.structure.MarkUsResultDataSet;
import org.geworkbench.engine.config.UILauncher;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.BrowserLauncher;
import org.jdesktop.jdic.browser.BrowserEngineManager;
import org.jdesktop.jdic.browser.IBrowserEngine;
import org.jdesktop.jdic.browser.IWebBrowser;
import org.jdesktop.jdic.browser.WebBrowser;
import org.jdesktop.jdic.browser.WebBrowserEvent;
import org.jdesktop.jdic.browser.WebBrowserListener;

/**
 * 
 * @author mwang
 * @version $Id: MarkUsBrowser.java,v 1.10 2009-10-08 15:32:47 wangm Exp $
 *
 */

@AcceptTypes( { MarkUsResultDataSet.class })
public class MarkUsBrowser implements VisualPlugin {
	private static final String MARKUS_RESULT_URL = "https://bhapp.c2b2.columbia.edu/MarkUs/cgi-bin/browse.pl?pdb_id=";

	private static Log log = LogFactory.getLog(MarkUsResultDataSet.class);
	
	private DSProteinStructure proteinData;
	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JPanel jp = new JPanel(new BorderLayout());
	
	private static CloseableTabbedPane jtp = new CloseableTabbedPane();
	private TabBrowser tb;
	private WebBrowser wb = null;
	private Object webBrowser = null;
	private Method setURL = null;
	private String tabtitle = null;

	private MyStatusBar statusBar = new MyStatusBar();
	private boolean initial = true;
	private boolean link = true;
	private HashMap<DSProteinStructure, String> musid4prt = new HashMap<DSProteinStructure, String>();
	private String process_id = null;
	private String lastpid = null;
	
	private int tc = 0;
	private static String osname = System.getProperty("os.name").toLowerCase();
    private final static boolean is_mac = (osname.indexOf("mac") > -1);
    private final static boolean is_windows = (osname.indexOf("windows") > -1);
	private static String osarch = System.getProperty("os.arch").toLowerCase();
	private static String jvmbit = System.getProperty("sun.arch.data.model").toLowerCase();

	// set true for jdic to use IE browser; false for Mozilla(FIXME: LINK in TAB
	// NOT WORKING)
	private final static boolean useIE = is_windows;
	
	private static Properties prop = new Properties();
	private static String mozilla_path = null;
	static {
		BrowserEngineManager bem = BrowserEngineManager.instance();
		if (is_mac)
			bem.setActiveEngine(BrowserEngineManager.WEBKIT);
		else if (is_windows)
			bem.setActiveEngine(BrowserEngineManager.IE);
		else {
			bem.setActiveEngine(BrowserEngineManager.MOZILLA);
			IBrowserEngine be = bem.getActiveEngine();
			try{
				FileInputStream fis = new FileInputStream("conf/jdic.properties");
				prop.load(fis);
				mozilla_path = prop.getProperty("mozilla.path");
			}catch(Exception e){
				e.printStackTrace();
			}
			be.setEnginePath(mozilla_path);
		}
	}
	
	protected MarkUsBrowser() {
	}

	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<?> dataset = event.getDataSet();
		if (is_mac && webBrowser != null && dataset instanceof DSProteinStructure)
		{
			boolean isshowing = ((java.awt.Component) webBrowser).isShowing();
			if (isshowing == true) {
				try {
					setURL.invoke(webBrowser, new URL("http://wiki.c2b2.columbia.edu/workbench/index.php/Home"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (dataset instanceof MarkUsResultDataSet) {
			MarkUsResultDataSet resultData = (MarkUsResultDataSet) dataset;
			proteinData = (DSProteinStructure) resultData.getParentDataSet();
			
			link = false;
			System.out.println("In receive(ProjectEvent event: " + proteinData + " "
					+ musid4prt.get(proteinData));
			System.out.println("initial: "+initial);
			jtp.removeAll();
			if (!initial) {
				if ((tc = jtp.getTabCount()) > 1)
					for (int i = tc - 1; i > 0; i--)
						jtp.remove(i);
			}

			if (musid4prt.get(proteinData) == null) {
				process_id = resultData.getResult();
				musid4prt.put(proteinData, process_id);
			}

			lastpid = process_id;
			process_id = resultData.getResult();
			log.debug("proteinData found: "+process_id);

			if ((is_windows && jvmbit.equals("64")) || is_mac) {
				handleUnsupportedOS();
				return;
			}

			if (is_windows)
			{
			    if(tb==null || !tb.isInitialized()) {
				try {
					tb = new TabBrowser(new URL(MARKUS_RESULT_URL
							+ process_id), useIE);
					tb.addWebBrowserListener(new WebTabListener());
					tb.setMainBrowser(this);
					jp.removeAll();
					jp.add(tb, BorderLayout.CENTER);
					jtp.setTabBrowser(tb);
					jtp.addTab(tabtitle, jp);
					mainPanel.add(jtp, BorderLayout.CENTER);
					mainPanel.revalidate();
					mainPanel.repaint();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
			    }
			}

			log.debug("process_id=" + process_id + "; proteinData="
						+ proteinData + ";lastpid " + lastpid);

			if ((process_id.startsWith("MUS") && !lastpid.equals(process_id)) || initial) {
				showResults(process_id);
			}
			else if (!initial)
			{
			    try{
				if (is_mac)
				    setURL.invoke(webBrowser, new URL(MARKUS_RESULT_URL+process_id));
				else if (!is_windows)
				{
				    wb = new WebBrowser(new URL(MARKUS_RESULT_URL+process_id));
				    jp.removeAll();
				    jp.add(wb, BorderLayout.CENTER);
				    jtp.setTabBrowser(wb);
				    jtp.addTab(process_id, jp);
				    mainPanel.add(jtp, BorderLayout.CENTER);
				    mainPanel.revalidate();
				    mainPanel.repaint();
				}
			    }catch(Exception e){
				e.printStackTrace();
			    }
			}
		}
	}

	private void showResults(String process_id) {
		log.debug("showResults called for "+process_id);
		String url = MARKUS_RESULT_URL + process_id;

		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

		statusBar.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		statusBar.lblDesc.setText("JDIC Browser");

		if (!initial)
		    if (is_windows)
			tb.setContent("<html><head><title>blank</title></head><body></body></html>");
		try {
			if (initial) {
				initial = false;
				
			    if (is_mac)
			    {
			    	//WebKitWebBrowser calls Mac-only com.apple.eawt.CocoaComponent
			    	//load it with reflect to allow compilation under windows
				    Class<?> wkwbc = Class.forName("org.jdesktop.jdic.browser.WebKitWebBrowser");
				    webBrowser = wkwbc.newInstance();
				    Method setContent = wkwbc.getMethod("setContent", Class.forName("java.lang.String"));
				    setURL = wkwbc.getMethod("setURL", Class.forName("java.net.URL"));
				    setContent.invoke(webBrowser, "MacRoman");
				    setURL.invoke(webBrowser, new URL(url));
				    jp.add((java.awt.Component)webBrowser, BorderLayout.CENTER);

				    JButton jStopButton = new JButton("Stop",
						new ImageIcon(MarkUsBrowser.class.getResource("/images/Stop.png")));
				    JButton jRefreshButton = new JButton("Refresh",
						new ImageIcon(MarkUsBrowser.class.getResource("/images/Reload.png")));
				    JButton jForwardButton = new JButton("Forward",
						new ImageIcon(MarkUsBrowser.class.getResource("/images/Forward.png")));
				    JButton jBackButton = new JButton("Back",
						new ImageIcon(MarkUsBrowser.class.getResource("/images/Back.png")));
				    jBackButton.setHorizontalTextPosition(SwingConstants.TRAILING);
				    jBackButton.setEnabled(true);
				    jBackButton.setMaximumSize(new Dimension(75, 27));
				    jBackButton.setPreferredSize(new Dimension(75, 27));
				    jBackButton.addActionListener(new ActionListener(){
					    public void actionPerformed(ActionEvent ae) {
						((IWebBrowser)webBrowser).back(); }});
				    jForwardButton.setEnabled(true);
				    jForwardButton.addActionListener(new ActionListener(){
					    public void actionPerformed(ActionEvent ae) {
						((IWebBrowser)webBrowser).forward(); }});
				    jRefreshButton.setEnabled(true);
				    jRefreshButton.setMaximumSize(new Dimension(75, 27));
				    jRefreshButton.setMinimumSize(new Dimension(75, 27));
				    jRefreshButton.setPreferredSize(new Dimension(75, 27));
				    jRefreshButton.addActionListener(new ActionListener(){
					    public void actionPerformed(ActionEvent ae) {
						((IWebBrowser)webBrowser).refresh(); }});
				    jStopButton.setVerifyInputWhenFocusTarget(true);
				    jStopButton.setText("Stop");
				    jStopButton.setEnabled(true);
				    jStopButton.setMaximumSize(new Dimension(75, 27));
				    jStopButton.setMinimumSize(new Dimension(75, 27));
				    jStopButton.setPreferredSize(new Dimension(75, 27));
				    jStopButton.addActionListener(new ActionListener(){
					    public void actionPerformed(ActionEvent ae){
						((IWebBrowser)webBrowser).stop(); }});
				    JToolBar jBrowserToolBar = new JToolBar();
				    jBrowserToolBar.setFloatable(false);
				    jBrowserToolBar.add(jBackButton, null);
				    jBrowserToolBar.add(jForwardButton, null);
				    jBrowserToolBar.addSeparator();
				    jBrowserToolBar.add(jRefreshButton, null);
				    jBrowserToolBar.add(jStopButton, null);
				    jBrowserToolBar.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createEtchedBorder(),
							BorderFactory.createEmptyBorder(2, 2, 2, 0)));
				    jp.add(jBrowserToolBar, BorderLayout.NORTH);
				    jtp.addTab(tabtitle, jp);
			    }
			    else if (is_windows)
			    {
					// Print out debug messages in the command line.
					// tb.setDebug(true);
					tb = new TabBrowser(new URL(url), useIE);
					tb.addWebBrowserListener(new WebTabListener());
					tb.setMainBrowser(this);
	
					jp.removeAll();
					jp.add(tb, BorderLayout.CENTER);
					jtp.setTabBrowser(tb);
					jtp.addTab(tabtitle, jp);
			    }
			    else
			    {
					// set auto_dispose=false to avoid dead mozilla browser in linux
					wb = new WebBrowser(new URL(url), useIE);
					jp.removeAll();
					jp.add(wb, BorderLayout.CENTER);
					jtp.setTabBrowser(wb);
					jtp.addTab(process_id, jp);
			    }
			    mainPanel.add(jtp, BorderLayout.CENTER);
			    mainPanel.add(statusBar, BorderLayout.SOUTH);
			} else {
				if (is_mac)
				    setURL.invoke(webBrowser, new URL(url));
				else if (is_windows)
					tb.setURL(new URL(url)); // add this no matter what
				else
				{
					wb = new WebBrowser(new URL(url));
				    jp.removeAll();
				    jp.add(wb, BorderLayout.CENTER);
				    jtp.setTabBrowser(wb);
				    jtp.addTab(process_id, jp);
				    mainPanel.add(jtp, BorderLayout.CENTER);
				    mainPanel.invalidate();
				    mainPanel.repaint();
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		mainPanel.revalidate();
		mainPanel.repaint();
	}

	protected JTabbedPane getTabbedPane() {
		return jtp;
	}

	public Component getComponent() {
		return mainPanel;
	}

	// web browser listener
	protected class WebTabListener implements WebBrowserListener {
		public void downloadStarted(WebBrowserEvent event) {
			updateStatusInfo("Loading started.");
		}

		public void initializationCompleted(WebBrowserEvent event) {
			updateStatusInfo("Initialization completed.");
		}

		public void downloadCompleted(WebBrowserEvent event) {
			updateStatusInfo("Loading completed.");
		}

		public void downloadError(WebBrowserEvent event) {
			updateStatusInfo("Loading error.");
		}

		public void documentCompleted(WebBrowserEvent event) {
			if (link == true)
				return;

			// set url or content in this function to get the page displayed
			String url = MARKUS_RESULT_URL + process_id;

			try {
				if (tb.getURL() != null) {
					URL currentUrl = tb.getURL();
					String curl = currentUrl.toString();
					System.out.println("\nfrom " + curl + " to " + url);
					if (!url.equals(curl)) {
						System.out.println("isinitialized: "
								+ tb.isInitialized() + "; newurl: " + url);

						tb.setURL(new URL(url));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			updateStatusInfo("Document loading completed.");
			link = true;
		}

		public void downloadProgress(WebBrowserEvent webEvent) {
			String values = webEvent.getData();
			if (values != null) {
				StringTokenizer tokenizer = new StringTokenizer(values, " ");

				if (tokenizer.hasMoreTokens()) {
					String current = tokenizer.nextToken();
					if (tokenizer.hasMoreTokens()) {
						String max = tokenizer.nextToken();

						int progress = Integer.parseInt(current);
						int progressMax = Integer.parseInt(max);

						onDownloadProgress(webEvent, progress, progressMax);
					}
				}
			}
		}

		// whatever this was meant to do, it was not implemented
		private void onDownloadProgress(WebBrowserEvent webEvent, int progress,
				int progressMax) {
		}

		public void windowClose(WebBrowserEvent event) {
			updateStatusInfo("Closed by script.");
			System.out.println("closed by script." + event.getData());
			if (JOptionPane.YES_OPTION == JOptionPane
					.showConfirmDialog(
							tb,
							"The webpage you are viewing is trying to close the window.\n Do you want to close this window?",
							"Warning", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE)) {
				UILauncher.printTimeStamp("geWorkbench exited.");
				System.exit(0);
			}
		}

		public void titleChange(WebBrowserEvent event) {
			tabtitle = event.getData();
			jtp.setTitleAt(jtp.getSelectedIndex(), tabtitle);
			updateStatusInfo("Title of the browser window changed.");
		}

		public void statusTextChange(WebBrowserEvent event) {
		}
	}

	void updateStatusInfo(String statusMessage) {
		statusBar.lblStatus.setText(statusMessage);
	}

	private void handleUnsupportedOS() {
		mainPanel.removeAll();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		JPanel jp = new JPanel();
		jp.add(new JLabel("Embedded browser doesn't support "+osname+" "+osarch+". "));
		JLabel rstlb = new JLabel("<html><u>Click here to</u></html>");
		jp.add(rstlb);
		jp.add(new JLabel("access MarkUs website directly"));
		mainPanel.add(new JLabel(" "));
		mainPanel.add(jp);
		rstlb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		rstlb.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent me) {
					try {
						BrowserLauncher.openURL(MARKUS_RESULT_URL+process_id);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				public void mouseEntered(MouseEvent evt) {
					evt.getComponent().setForeground(new Color(0xC0, 0xC0, 0xF0));
				}
				public void mouseExited(MouseEvent evt) {
					evt.getComponent().setForeground(Color.BLACK);
				}
			});
		mainPanel.revalidate();
		mainPanel.repaint();
	}
}
