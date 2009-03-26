package org.geworkbench.components.markus;

import java.awt.BorderLayout;
import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ToolTipManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbench.bison.datastructure.bioobjects.structure.MarkUsResultDataSet;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.ProjectNodeCompletedEvent;
import org.jdesktop.jdic.browser.BrowserEngineManager;
import org.jdesktop.jdic.browser.IBrowserEngine;
import org.jdesktop.jdic.browser.WebBrowserEvent;
import org.jdesktop.jdic.browser.WebBrowserListener;

/**
 * 
 * @author mwang
 * @version $Id: MarkUsBrowser.java,v 1.6 2009-03-26 19:51:33 jiz Exp $
 *
 */

@AcceptTypes( { MarkUsResultDataSet.class })
public class MarkUsBrowser implements VisualPlugin {
	private static final String MARKUS_RESULT_URL = "http://luna.bioc.columbia.edu/honiglab/mark-us/cgi-bin/browse.pl?pdb_id=";

	private static Log log = LogFactory.getLog(MarkUsResultDataSet.class);
	
	private DSProteinStructure proteinData;
	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JPanel jp = new JPanel(new BorderLayout());
	
	private static CloseableTabbedPane jtp = new CloseableTabbedPane();
	private TabBrowser tb;
	private String tabtitle = null;

	private MyStatusBar statusBar = new MyStatusBar();
	private boolean initial = true;
	private boolean link = true;
	private HashMap<DSProteinStructure, String> musid4prt = new HashMap<DSProteinStructure, String>();
	private String process_id = null;
	private String lastpid = null;
	
	private int tc = 0;

	// set true for jdic to use IE browser; false for Mozilla(FIXME: LINK in TAB
	// NOT WORKING)
	private final static boolean useIE = true;
	
	static {
		BrowserEngineManager bem = BrowserEngineManager.instance();
		if (bem.getActiveEngine() != bem.getEngines().get(
				BrowserEngineManager.IE)) {
			// set jdic to use IE or Mozilla browser
			if (useIE == true) {
				bem.setActiveEngine(BrowserEngineManager.IE);
			} else {
				bem.setActiveEngine(BrowserEngineManager.MOZILLA);
				IBrowserEngine be = bem.getActiveEngine();
				be.setEnginePath("C:\\Program Files\\mozilla\\mozilla.exe");
			}
		}
	}
	
	protected MarkUsBrowser() {
	}

	@SuppressWarnings("unchecked")
	@Subscribe
	public void receive(ProjectNodeCompletedEvent event, Object source) {
		DSAncillaryDataSet dataset = event.getAncillaryDataSet();
		if (dataset instanceof MarkUsResultDataSet) {
			link = false;
			MarkUsResultDataSet resultData  = (MarkUsResultDataSet) dataset;
			lastpid = process_id;
			process_id = resultData.getResult();
			proteinData = (DSProteinStructure) resultData.getParentDataSet();
			System.out.println("process_id: " + process_id);
			musid4prt.put(proteinData, process_id);
			if (process_id.startsWith("MUS")) {
				showResults(process_id);
			} else {
				System.out.println("not displayable job ID '"+process_id+"'");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet dataset = event.getDataSet();
		if (dataset instanceof MarkUsResultDataSet) {
			MarkUsResultDataSet resultData = (MarkUsResultDataSet) dataset;
			proteinData = (DSProteinStructure) resultData.getParentDataSet();
			
			link = false;
			System.out.println("In receive(ProjectEvent event: " + proteinData + " "
					+ musid4prt.get(proteinData));
			System.out.println("initial: "+initial);
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
			process_id = musid4prt.get(proteinData);
			log.debug("proteinData found: "+process_id);
			if(tb==null || !tb.isInitialized()) {
				try {
					tb = new TabBrowser(new URL(MARKUS_RESULT_URL
							+ process_id), useIE);
					tb.addWebBrowserListener(new WebTabListener());
					tb.setMainBrowser(this);
					jp.removeAll();
					jp.add(tb, BorderLayout.CENTER);
					jtp.addTab(tabtitle, jp);
					mainPanel.add(jtp, BorderLayout.CENTER);
					mainPanel.invalidate();
					mainPanel.repaint();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
			}
			log.debug("process_id=" + process_id + "; proteinData="
						+ proteinData + ";lastpid " + lastpid);

			if (process_id.startsWith("MUS") && !lastpid.equals(process_id)) {
				showResults(process_id);
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
			tb
					.setContent("<html><head><title>blank</title></head><body></body></html>");
		try {
			if (initial) {
				initial = false;

				// Print out debug messages in the command line.
				// tb.setDebug(true);
				tb = new TabBrowser(new URL(url), useIE);
				tb.addWebBrowserListener(new WebTabListener());
				tb.setMainBrowser(this);

				jp.removeAll();
				jp.add(tb, BorderLayout.CENTER);
				jtp.addTab(tabtitle, jp);

				mainPanel.add(jtp, BorderLayout.CENTER);
				mainPanel.add(statusBar, BorderLayout.SOUTH);
			} else {
				tb.setURL(new URL(url)); // add this no matter what
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
				System.exit(0);
			}
		}

		public void titleChange(WebBrowserEvent event) {
			tabtitle = event.getData();
			jtp.setTitleAt(jtp.getTabCount() - 1, tabtitle);
			updateStatusInfo("Title of the browser window changed.");
		}

		public void statusTextChange(WebBrowserEvent event) {
		}
	}

	void updateStatusInfo(String statusMessage) {
		statusBar.lblStatus.setText(statusMessage);
	}
}
