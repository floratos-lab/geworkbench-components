package org.geworkbench.components.gpmodule.gsea.browser;

import org.jdesktop.jdic.browser.WebBrowser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.net.URL;
import java.awt.*;

/**
 * User: nazaire
 */
public class TabBrowser extends WebBrowser
{
    Log log = LogFactory.getLog(TabBrowser.class);

    public static GSEABrowser gb;
    private boolean useIE = true;

    public TabBrowser(URL url, boolean IE)
    {
        super(url);
        setURL(url, url.getQuery(), "user-agent:geWorkbench");
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
