package org.geworkbench.components.pudge;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.BoxLayout;
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
import org.geworkbench.engine.config.UILauncher;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.BrowserLauncher;
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
 * @version $Id: PudgeBrowser.java,v 1.3 2009/06/30 19:21:27 wangm Exp $
 */
@AcceptTypes( { PudgeResultSet.class })
public class PudgeBrowser implements VisualPlugin {
	private Log log = LogFactory.getLog(this.getClass());
	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JPanel jp = new JPanel(new BorderLayout());
	private IWebBrowser wb;
	private boolean initial = true, link = true;

	private static ImageIcon browseIcon = new ImageIcon(
			PudgeBrowser.class.getResource("/images/Right.gif"));
	private JToolBar jBrowserToolBar = new JToolBar();
	private JButton jStopButton = new JButton("Stop",
	   new ImageIcon(PudgeBrowser.class.getResource("/images/Stop.png")));
	private JButton jRefreshButton = new JButton("Refresh",
	   new ImageIcon(PudgeBrowser.class.getResource("/images/Reload.png")));
	private JButton jForwardButton = new JButton("Forward",
	   new ImageIcon(PudgeBrowser.class.getResource("/images/Forward.png")));
	private JButton jBackButton = new JButton("Back",
	   new ImageIcon(PudgeBrowser.class.getResource("/images/Back.png")));
	private MyStatusBar statusBar = new MyStatusBar();
	private JPanel jAddressPanel = new JPanel();
	private JLabel jAddressLabel = new JLabel();
	private JTextField jAddressTextField = new JTextField();
	private JButton jGoButton = new JButton();
	private JPanel jAddrToolBarPanel = new JPanel();

	private PudgeResultSet resultData;
	private String jobname = "";
	private String urlbase = "http://bhapp.c2b2.columbia.edu/pudge/cgi-bin/";
	private String finalResultURL = "";
	private static String osname = System.getProperty("os.name").toLowerCase();
	private final static boolean is_mac = (osname.indexOf("mac") > -1);
	private final static boolean is_windows = (osname.indexOf("windows") > -1);
	private static String osarch = System.getProperty("os.arch").toLowerCase();
	private final static boolean is_64bit = (osarch.indexOf("_64") > -1);
    private static String osversion = System.getProperty("os.version").toLowerCase();
    private final static boolean is_10_5 = (osversion.indexOf("10.5") > -1);
	private static String jvmbit = System.getProperty("sun.arch.data.model").toLowerCase();
	private static Properties prop = new Properties();
	private static String mozilla_path = null;
	private static enum Status {CONFIG, PENDING, FINAL};

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
	private String resultURL = null;
	@Subscribe
	public void receive(ProjectEvent event, Object source) {

		DSDataSet<?> dataset = event.getDataSet();
		if (dataset instanceof PudgeResultSet) {
			resultData = (PudgeResultSet) dataset;
			String r1 = resultData.getResult();
			if (r1.length() == 0) {
				JOptionPane.showMessageDialog(null, "No Pudge Result Set!",
						"Warning", JOptionPane.WARNING_MESSAGE);
				mainPanel.removeAll();
				mainPanel.revalidate();
				mainPanel.repaint();
				return;
			}
			jobname = resultData.getLabel();
			link = false;
			finalResultURL = urlbase + "show_results.cgi?from_user=" + jobname;

			// check job status from the pudge webserver only once, 
			// instead of keeping track of it until the job is done
			resultURL = getCurrentURL(r1);
			if (resultURL != r1)
				resultData.setResult(resultURL);
			
			if ((is_windows && jvmbit.equals("64")) || (is_mac && (!is_64bit||is_10_5))) {
				handleUnsupportedOS();
				return;
			}

			jbInit(resultURL);
		}
	}

	// get url for current job status
	private String getCurrentURL(String resultURL) {

		if (resultURL == finalResultURL)
			return resultURL;

		String pendingResultURL = resultURL;
		int i1, i2;
		if ((i1 = resultURL.indexOf("tmpdir=")) > 0
				&& (i2 = resultURL.indexOf("&", i1)) > 0) {
			pendingResultURL = urlbase + "pipe_int.cgi?status=1&jobID="
					+ resultURL.substring(i1 + 7, i2) + "&dir_name=" + jobname;
		}
		Status sts = checkJobFinish(pendingResultURL);

		if (sts == Status.FINAL)
			return finalResultURL;
		if (sts == Status.PENDING)
			return pendingResultURL;
		return resultURL;
	}

	/*
	 * check job running status
	 */
	private Status checkJobFinish(String url) {
		try {
			URLConnection uc = new URL(url).openConnection();
			if (uc == null || !uc.getContentType().startsWith("text/html")) {
				return Status.CONFIG;
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(uc
					.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				if (line.indexOf("Please use this link to see the results") > -1)
					return Status.CONFIG;
				if (line.indexOf("show_results.cgi") > -1)
					return Status.FINAL;
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Status.PENDING;
	}

	/*
	 * start embedded web browser
	 */
	private void jbInit(String url) {
		try {
			if (initial) {
				initial = false;
				setToolBar();

				if (is_mac) {
					// WebKitWebBrowser calls Mac-only
					// com.apple.eawt.CocoaComponent
					// load it with reflect to allow compilation under windows
					Class<?> wkwbc = Class
							.forName("org.jdesktop.jdic.browser.WebKitWebBrowser");
					wb = (IWebBrowser) wkwbc.newInstance();
					wb.setContent("MacRoman");
					wb.setURL(new URL(url));
				} else {
					System.out.println("initial: " + url);
					// set auto_dispose=false to avoid dead mozilla browser in
					// linux
					wb = new WebBrowser(new URL(url), is_windows);
				}
				wb.addWebBrowserListener(new WebListener());
				jp.add(wb.asComponent(), BorderLayout.CENTER);
				mainPanel.add(jp, BorderLayout.CENTER);
				mainPanel.add(jAddrToolBarPanel, BorderLayout.NORTH);
				mainPanel.add(statusBar, BorderLayout.SOUTH);
				if (is_mac) mainPanel.add(jp, BorderLayout.CENTER);
			} else {
				System.out.println("later: " + url);

				if (is_mac || is_windows)
					wb.setURL(new URL(url));
				else {
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

	private void setToolBar() {
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
	private class WebListener implements WebBrowserListener {
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
				UILauncher.printTimeStamp("geWorkbench exited.");
				System.exit(0);
			}
		}

		public void titleChange(WebBrowserEvent event) {
		}

		public void statusTextChange(WebBrowserEvent event) {
		}
	}

	private void updateStatusInfo(String statusMessage) {
		statusBar.lblStatus.setText(statusMessage);
	}

	private void jAddressTextField_actionPerformed(ActionEvent e) {
		loadURL();
	}

	private void jBackButton_actionPerformed(ActionEvent e) {
		wb.back();
	}

	private void jForwardButton_actionPerformed(ActionEvent e) {
		wb.forward();
	}

	private void jRefreshButton_actionPerformed(ActionEvent e) {
		wb.refresh();
	}

	private void jStopButton_actionPerformed(ActionEvent e) {
		wb.stop();
	}

	private void jGoButton_actionPerformed(ActionEvent e) {
		loadURL();
	}

	/**
	 * Check the current input URL string in the address text field, load it,
	 * and update the status info and toolbar info.
	 */
	@SuppressWarnings("deprecation")
	private void loadURL() {
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

	private static class Browser_jAddressTextField_actionAdapter implements
			java.awt.event.ActionListener {
		PudgeBrowser adaptee;

		Browser_jAddressTextField_actionAdapter(PudgeBrowser adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.jAddressTextField_actionPerformed(e);
		}
	}

	private static class Browser_jBackButton_actionAdapter implements java.awt.event.ActionListener {
		PudgeBrowser adaptee;

		Browser_jBackButton_actionAdapter(PudgeBrowser adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.jBackButton_actionPerformed(e);
		}
	}

	private static class Browser_jForwardButton_actionAdapter implements java.awt.event.ActionListener {
		PudgeBrowser adaptee;

		Browser_jForwardButton_actionAdapter(PudgeBrowser adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.jForwardButton_actionPerformed(e);
		}
	}

	private static class Browser_jRefreshButton_actionAdapter implements java.awt.event.ActionListener {
		PudgeBrowser adaptee;

		Browser_jRefreshButton_actionAdapter(PudgeBrowser adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.jRefreshButton_actionPerformed(e);
		}
	}

	private static class Browser_jStopButton_actionAdapter implements java.awt.event.ActionListener {
		PudgeBrowser adaptee;

		Browser_jStopButton_actionAdapter(PudgeBrowser adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.jStopButton_actionPerformed(e);
		}
	}

	private static class Browser_jGoButton_actionAdapter implements java.awt.event.ActionListener {
		PudgeBrowser adaptee;

		Browser_jGoButton_actionAdapter(PudgeBrowser adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e) {
			adaptee.jGoButton_actionPerformed(e);
		}
	}

	private void handleUnsupportedOS() {
		mainPanel.removeAll();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		JPanel jp = new JPanel();
		jp.add(new JLabel("Embedded browser doesn't support "+osname+" "+osversion+" "+osarch+". "));
		JLabel rstlb = new JLabel("<html><u>Click here to</u></html>");
		jp.add(rstlb);
		jp.add(new JLabel("access Pudge website directly"));
		mainPanel.add(new JLabel(" "));
		mainPanel.add(jp);
		rstlb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		rstlb.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent me) {
					try {
						BrowserLauncher.openURL(resultURL);
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
