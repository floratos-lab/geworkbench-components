package org.geworkbench.components.gpmodule.gsea.browser;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.jdic.browser.WebBrowser;

/**
 * @author nazaire
 * @version $Id$
 */
public class TabBrowser extends WebBrowser
{
	private static final long serialVersionUID = 6230750346483930171L;

	Log log = LogFactory.getLog(TabBrowser.class);

    public static GSEABrowser gb;
    private boolean useIE = true;

    public TabBrowser(URL url, boolean IE)
    {
        super(url);
        useIE = IE;
    }

   // return false to block popup window
    protected boolean willOpenWindow(URL url)
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            log.info("new url '" + url + "'");
            createAllowedContent(url);
        }
        else
        {
            log.error("willOpenWindow called in a non-EDT thread.");
        }
        return false;
    }

    // return false to block url navigation
    protected boolean willOpenURL(URL url)
    {
        boolean rc = super.willOpenURL(url);
        return rc;
    }

    public void setMainBrowser(GSEABrowser mainbrowser)
    {
        gb = mainbrowser;
    }

    public GSEABrowser getMainBrowser()
    {
        return gb;
    }

    /**
     * add a new tab for the main browser panel
     * @param url
     */
    private void createAllowedContent(URL url)
    {
        TabBrowser newtb = new TabBrowser(url, useIE);
        GSEABrowser.WebTabListener wtl = gb.new WebTabListener();
        newtb.addWebBrowserListener(wtl);
        JPanel subjp = new JPanel(new BorderLayout());
        subjp.add(newtb, BorderLayout.CENTER);
        gb.getTabbedPane().addTab("Tab", subjp);
        gb.getTabbedPane().setSelectedIndex(
        gb.getTabbedPane().getTabCount() - 1);
    }
}
