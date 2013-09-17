package org.geworkbench.components.markus;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.components.markus.MarkUsBrowser.WebTabListener;
import org.jdesktop.jdic.browser.WebBrowser;

/**
 * Embedded browser for MarkUsResult
 * 
 * @author mwang
 * @version $Id: TabBrowser.java,v 1.4 2009-03-06 17:10:45 jiz Exp $
 *
 */
class TabBrowser extends WebBrowser {
	private static final long serialVersionUID = -4833106758989294420L;
	Log log = LogFactory.getLog(TabBrowser.class);

	public static MarkUsBrowser mb;
	private boolean useIE = true;

	public TabBrowser(URL url, boolean IE) {
		super(url);
		setURL(url, url.getQuery(), "user-agent:geWorkbench");
		useIE = IE;
	}

	// return false to block popup window
	protected boolean willOpenWindow(URL url) {
		if (SwingUtilities.isEventDispatchThread()) {
			log.info("new url '" + url + "'");
			createAllowedContent(url);
		} else {
			log.error("willOpenWindow called in a non-EDT thread.");
		}
		return false;
	}

	// return false to block url navigation
	protected boolean willOpenURL(URL url) {
		boolean rc = super.willOpenURL(url);
		return rc;
	}

	public void setMainBrowser(MarkUsBrowser mainbrowser) {
		mb = mainbrowser;
	}

	public MarkUsBrowser getMainBrowser() {
		return mb;
	}

	/**
	 * add a new tab for the main browser panel
	 * @param url
	 */
	private void createAllowedContent(URL url) {
		TabBrowser newtb = new TabBrowser(url, useIE);
		WebTabListener wtl = mb.new WebTabListener();
		newtb.addWebBrowserListener(wtl);
		JPanel subjp = new JPanel(new BorderLayout());
		subjp.add(newtb, BorderLayout.CENTER);
		mb.getTabbedPane().addTab("Tab", subjp);
		mb.getTabbedPane().setSelectedIndex(
				mb.getTabbedPane().getTabCount() - 1);
	}
}
