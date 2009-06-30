package org.geworkbench.components.pudge;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.JToolBar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.structure.PudgeResultSet;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.jdesktop.jdic.browser.BrowserEngineManager;
import org.jdesktop.jdic.browser.IBrowserEngine;
import org.jdesktop.jdic.browser.WebBrowser;
import org.jdesktop.jdic.browser.IWebBrowser;
import org.jdesktop.jdic.browser.WebBrowserEvent;
import org.jdesktop.jdic.browser.WebBrowserListener;

/**
 * display Pudge website in JDIC embedded IE web browser
 * 
 * @author mw2518
 * @version $Id: PudgeBrowser.java,v 1.3 2009-06-30 19:21:27 wangm Exp $
 */
@AcceptTypes( { PudgeResultSet.class })
public class PudgeBrowser implements VisualPlugin {
	private Log log = LogFactory.getLog(this.getClass());
	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JPanel jp = new JPanel(new BorderLayout());
	private IWebBrowser wb;
	private boolean finish = false;
	boolean initial = true, link = true;

	public static ImageIcon browseIcon = new ImageIcon(
	   "src/images/Right.gif");
	JToolBar jBrowserToolBar = new JToolBar();
	JButton jStopButton = new JButton("Stop",
		new ImageIcon("src/images/Stop.png"));
	JButton jRefreshButton = new JButton("Refresh",
		new ImageIcon("src/images/Reload.png"));
	JButton jForwardButton = new JButton("Forward",
		new ImageIcon("src/images/Forward.png"));
	JButton jBackButton = new JButton("Back",
		new ImageIcon("src/images/Back.png"));
	MyStatusBar statusBar = new MyStatusBar();
	JPanel jAddressPanel = new JPanel();
	JLabel jAddressLabel = new JLabel();
	JTextField jAddressTextField = new JTextField();
	JButton jGoButton = new JButton();
	JPanel jAddrToolBarPanel = new JPanel();

	private PudgeResultSet resultData;
	private String jobname = "";
	String urlbase = "http://luna.bioc.columbia.edu/honiglab/pudge/cgi-bin/";
	private static String osname = System.getProperty("os.name").toLowerCase();
	private final static boolean is_mac = (osname.indexOf("mac") > -1);
	private final static boolean is_windows = (osname.indexOf("windows") > -1);
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
			try {
				FileInputStream fis = new FileInputStream(
						"conf/jdic.properties");
				prop.load(fis);
				mozilla_path = prop.getProperty("mozilla.path");
			} catch (Exception e) {
				e.printStackTrace();
			}
			be.setEnginePath(mozilla_path);
		}
	}

	@Subscribe
	public void receive(ProjectEvent event, Object source) {

		DSDataSet dataset = event.getDataSet();
		if (dataset instanceof PudgeResultSet) {
			resultData = (PudgeResultSet) dataset;
			String r1 = resultData.getResult();
			if (r1.length() == 0) {
				System.out.println("No PudgeResultSet!");
				r1 = urlbase + "pipe_int.cgi";
			}
			jobname = resultData.getLabel();
			link = false;
			showResults(r1);
		}
	}

	class RunURL implements Runnable {
		String resultURL = "";
		int type = 2;

		RunURL(String res, int t) {
			resultURL = res;
			type = t;
		}

		public void run() {
			if (type == 2) {
				int i1, i2;
				if ((i1 = resultURL.indexOf("tmpdir=")) > 0
						&& (i2 = resultURL.indexOf("&", i1)) > 0)
					resultURL = urlbase + "pipe_int.cgi?status=1&jobID="
							+ resultURL.substring(i1 + 7, i2) + "&dir_name="
							+ jobname;
				runtype(resultURL, type);
			}

			resultURL = urlbase + "show_results.cgi?from_user=" + jobname;
			runtype(resultURL, 3);
		}

		public void runtype(String resultURL, int type) {
			while (!checkJobFinish(resultURL, type)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			//if new data set is received while job is pending, stop the thread
			if (resultURL.indexOf(jobname)<0) return;
			
			resultData.setResult(resultURL);
			link = false;
			try {
				wb.setURL(new URL(resultURL));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void showResults(String resultURL) {
		int type = 1;
		if (resultURL.indexOf("status=1") > -1)
			type = 2;
		else if (resultURL.indexOf("show_results.cgi") > -1)
			type = 3;

		jbInit(resultURL, type);

		if (type < 3 && !resultURL.equals(urlbase + "pipe_int.cgi"))
			new Thread(new RunURL(resultURL, type + 1)).start();
	}

	/*
	 * check if result page is valid
	 */
	boolean checkJobFinish(String url, int type) {
		try {
			if (type == 1)
				return true;
			URLConnection uc = new URL(url).openConnection();
			if (uc == null || !uc.getContentType().startsWith("text/html")) {
				return false;
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(uc
					.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				if (type == 2) {
					if (line.indexOf("Please use this link to see the results") > -1)
						return false;
				} else if (type == 3) {
					if (line.indexOf("show_results.cgi") > -1)
						return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (type == 2)
			return true;
		return false;
	}

	/*
	 * start embedded web browser
	 */
	private void jbInit(String url, int type) {
		finish = checkJobFinish(url, type);
		if (!finish) {
			mainPanel.removeAll();
			mainPanel.revalidate();
			mainPanel.repaint();
			return;
		}

		jAddressPanel.setLayout(new BorderLayout());
		jAddressTextField
				.addActionListener(new Browser_jAddressTextField_actionAdapter(
						this));
		jAddressLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		jAddressLabel.setText(" URL: ");

		jGoButton.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0, 2, 0, 2),
								       new EtchedBorder()));
		jGoButton.setMaximumSize(new Dimension(60, 25));
		jGoButton.setMinimumSize(new Dimension(60, 25));
		jGoButton.setPreferredSize(new Dimension(60, 25));
		jGoButton.setIcon(browseIcon);
		jGoButton.setText("GO");
		jGoButton.addActionListener(new Browser_jGoButton_actionAdapter(this));

		jBackButton.setHorizontalTextPosition(SwingConstants.TRAILING);
		jBackButton.setEnabled(false);
		jBackButton.setMaximumSize(new Dimension(75, 27));
		jBackButton.setPreferredSize(new Dimension(75, 27));
		jBackButton.addActionListener(new Browser_jBackButton_actionAdapter(this));
		jForwardButton.setEnabled(false);
		jForwardButton.addActionListener(new Browser_jForwardButton_actionAdapter(this));
		jRefreshButton.setEnabled(true);
		jRefreshButton.setMaximumSize(new Dimension(75, 27));
		jRefreshButton.setMinimumSize(new Dimension(75, 27));
		jRefreshButton.setPreferredSize(new Dimension(75, 27));
		jRefreshButton.addActionListener(new Browser_jRefreshButton_actionAdapter(this));
		jStopButton.setVerifyInputWhenFocusTarget(true);
		jStopButton.setText("Stop");
		jStopButton.setEnabled(true);
		jStopButton.setMaximumSize(new Dimension(75, 27));
		jStopButton.setMinimumSize(new Dimension(75, 27));
		jStopButton.setPreferredSize(new Dimension(75, 27));
		jStopButton.addActionListener(new Browser_jStopButton_actionAdapter(this));
		jAddressPanel.add(jAddressLabel, BorderLayout.WEST);
		jAddressPanel.add(jAddressTextField, BorderLayout.CENTER);
		jAddressPanel.add(jGoButton, BorderLayout.EAST);
		jAddressPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(2, 0, 2, 0)));

		jBrowserToolBar.setFloatable(false);
		jBrowserToolBar.add(jBackButton, null);
		jBrowserToolBar.add(jForwardButton, null);
		jBrowserToolBar.addSeparator();
		jBrowserToolBar.add(jRefreshButton, null);
		jBrowserToolBar.add(jStopButton, null);
		jBrowserToolBar.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(2, 2, 2, 0)));

		jAddrToolBarPanel.setLayout(new BorderLayout());
		jAddrToolBarPanel.add(jAddressPanel, BorderLayout.CENTER);
		jAddrToolBarPanel.add(jBrowserToolBar, BorderLayout.WEST);
		jAddrToolBarPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

		statusBar.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		statusBar.lblDesc.setText("JDIC Browser");

		try {
			if (initial) {
				initial = false;

				if (is_mac) {
					// WebKitWebBrowser calls Mac-only
					// com.apple.eawt.CocoaComponent
					// load it with reflect to allow compilation under windows
					Class wkwbc = Class
							.forName("org.jdesktop.jdic.browser.WebKitWebBrowser");
					wb = (IWebBrowser)wkwbc.newInstance();
					wb.setContent("MacRoman");
					wb.setURL(new URL(url));
				} else {
				    //set auto_dispose=false to avoid dead mozilla browser in linux
					wb = new WebBrowser(new URL(url), is_windows);
				}
				wb.addWebBrowserListener(new WebListener());
				jp.add(wb.asComponent(), BorderLayout.CENTER);
				mainPanel.add(jp, BorderLayout.CENTER);
				mainPanel.add(jAddrToolBarPanel, BorderLayout.NORTH);
				mainPanel.add(statusBar, BorderLayout.SOUTH);
			} else {
				if (is_mac || is_windows)
					wb.setURL(new URL(url));
				else
				{
					wb = new WebBrowser(new URL(url));
					wb.addWebBrowserListener(new WebListener());
					jp.removeAll();
					jp.add(wb.asComponent(), BorderLayout.CENTER);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mainPanel.revalidate();
		mainPanel.repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.engine.config.VisualPlugin#getComponent()
	 */
	public Component getComponent() {
		return mainPanel;
	}

	/*
	 * web browser listener
	 */
	public class WebListener implements WebBrowserListener {
		public void downloadStarted(WebBrowserEvent event) {
			updateStatusInfo("Loading started.");
		}

		public void initializationCompleted(WebBrowserEvent event) {
		}

		public void downloadCompleted(WebBrowserEvent event) {
			jBackButton.setEnabled(wb.isBackEnabled());
			jForwardButton.setEnabled(wb.isForwardEnabled());
			updateStatusInfo("Loading completed.");
			URL currentUrl = wb.getURL();
			if (currentUrl != null)
				jAddressTextField.setText(currentUrl.toString());
		}

		public void downloadError(WebBrowserEvent event) {
			updateStatusInfo("Loading error.");
		}

		public void documentCompleted(WebBrowserEvent event) {
			if (link == true)
				return;
			URL currentUrl = wb.getURL();

			// set url or content in this function to get the page displayed
			String url = resultData.getResult();
			try {
				String curl = null;
				if (currentUrl != null) {
					curl = currentUrl.toString();
				}
				if (!url.equals(curl))
					wb.setURL(new URL(url));
			} catch (Exception e) {
				e.printStackTrace();
			}

			updateStatusInfo("Document loading completed.");
			link = true;

		}

		public void downloadProgress(WebBrowserEvent webEvent) {
		}

		public void windowClose(WebBrowserEvent event) {
			updateStatusInfo("Closed by script.");
			log.info("closed by script." + event.getData());
			if (JOptionPane.YES_OPTION == JOptionPane
					.showConfirmDialog(
							wb.asComponent(),
							"The webpage you are viewing is trying to close the window.\n Do you want to close this window?",
							"Warning", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE)) {
				System.exit(0);
			}
		}

		public void titleChange(WebBrowserEvent event) {
		}

		public void statusTextChange(WebBrowserEvent event) {
		}
	}

	void updateStatusInfo(String statusMessage) {
		statusBar.lblStatus.setText(statusMessage);
	}

	void jAddressTextField_actionPerformed(ActionEvent e) {
		loadURL();
	}

	void jBackButton_actionPerformed(ActionEvent e) {
		wb.back();
	}

	void jForwardButton_actionPerformed(ActionEvent e) {
		wb.forward();
	}

	void jRefreshButton_actionPerformed(ActionEvent e) {
		wb.refresh();
	}

	void jStopButton_actionPerformed(ActionEvent e) {
		wb.stop();
	}

	void jGoButton_actionPerformed(ActionEvent e) {
		loadURL();
	}

    /**
	 * Check the current input URL string in the address text field, load it,
	 * and update the status info and toolbar info.
	 */
	void loadURL() {
		String inputValue = jAddressTextField.getText();

		if (inputValue == null) {
			JOptionPane.showMessageDialog(null, "The given URL is NULL:",
					"Warning", JOptionPane.WARNING_MESSAGE);
		} else {
			// Check if the text value is a URL string.
			URL curUrl = null;

			try {
				// Check if the input string is a local path by checking if it
				// starts
				// with a driver name(on Windows) or root path(on Unix).
				File[] roots = File.listRoots();

				for (int i = 0; i < roots.length; i++) {
					if (inputValue.toLowerCase().startsWith(
							roots[i].toString().toLowerCase())) {
						File curLocalFile = new File(inputValue);

						curUrl = curLocalFile.toURL();
						break;
					}
				}

				if (curUrl == null) {
					// Check if the text value is a valid URL.
					try {
						curUrl = new URL(inputValue);
					} catch (MalformedURLException e) {
						if (inputValue.toLowerCase().startsWith("ftp.")) {
							curUrl = new URL("ftp://" + inputValue);
						} else if (inputValue.toLowerCase().startsWith(
								"gopher.")) {
							curUrl = new URL("gopher://" + inputValue);
						} else {
							curUrl = new URL("http://" + inputValue);
						}
					}
				}

				wb.setURL(curUrl);

				// Update the address text field, statusbar, and toolbar info.
				updateStatusInfo("Loading " + curUrl.toString() + " ......");

			} catch (MalformedURLException mue) {
				JOptionPane.showMessageDialog(null,
						"The given URL is not valid:" + inputValue, "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	class Browser_jAddressTextField_actionAdapter implements
			java.awt.event.ActionListener {
		PudgeBrowser adaptee;

		Browser_jAddressTextField_actionAdapter(PudgeBrowser adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.jAddressTextField_actionPerformed(e);
		}
	}

	class Browser_jBackButton_actionAdapter implements java.awt.event.ActionListener {
		PudgeBrowser adaptee;

		Browser_jBackButton_actionAdapter(PudgeBrowser adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.jBackButton_actionPerformed(e);
		}
	}


	class Browser_jForwardButton_actionAdapter implements java.awt.event.ActionListener {
		PudgeBrowser adaptee;

		Browser_jForwardButton_actionAdapter(PudgeBrowser adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.jForwardButton_actionPerformed(e);
		}
	}

	class Browser_jRefreshButton_actionAdapter implements java.awt.event.ActionListener {
		PudgeBrowser adaptee;

		Browser_jRefreshButton_actionAdapter(PudgeBrowser adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.jRefreshButton_actionPerformed(e);
		}
	}

	class Browser_jStopButton_actionAdapter implements java.awt.event.ActionListener {
		PudgeBrowser adaptee;

		Browser_jStopButton_actionAdapter(PudgeBrowser adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.jStopButton_actionPerformed(e);
		}
	}

	class Browser_jGoButton_actionAdapter implements java.awt.event.ActionListener {
		PudgeBrowser adaptee;

		Browser_jGoButton_actionAdapter(PudgeBrowser adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.jGoButton_actionPerformed(e);
		}
	}
}
