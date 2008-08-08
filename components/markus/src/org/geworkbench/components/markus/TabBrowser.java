package org.geworkbench.components.markus;

import java.awt.BorderLayout;
import java.net.*;
import javax.swing.*;

import org.geworkbench.components.markus.MarkUsBrowser.WebTabListener;
import org.jdesktop.jdic.browser.*;

class TabBrowser extends WebBrowser
{
    	protected PopupBlockerOpenWindowRunnable openWindowRunnable = new PopupBlockerOpenWindowRunnable();
	protected URL lastToOpenURL = null;
	public static MarkUsBrowser mb;
    private boolean useIE = true;

        public TabBrowser() { super(); }

	public TabBrowser(URL url)
	{
		super(url);
		setLastToOpenURL(url);
	}

	public TabBrowser(URL url, boolean IE)
	{
		super(url);
		setURL(url, url.getQuery(), "user-agent:geWorkbench");
		useIE = IE;
		setLastToOpenURL(url);
	}

	//return false to block popup window
	protected boolean willOpenWindow(URL url)
	{
		openWindowRunnable.setUrl(url);
		SwingUtilities.invokeLater(openWindowRunnable);
		////		System.out.println("wow: "+url);
		////		if (url.toString().indexOf("javascript:mot") >-1) return true;
		return false;
	}
	
	//return false to block url navigation
	protected boolean willOpenURL(URL url)
	{
		boolean rc = super.willOpenURL(url);
		setLastToOpenURL(url);
		return rc;
	}
	
	protected void setLastToOpenURL(URL url)
	{
		lastToOpenURL = url;
	}
	
	protected URL getLastToOpenURL()
	{
		return lastToOpenURL;
	}
	
	public void setMainBrowser(MarkUsBrowser mainbrowser)
	{
		mb = mainbrowser;
	}
	public MarkUsBrowser getMainBrowser()
	{
		return mb;
	}
	public void createAllowedContent(URL url)
	{
    		TabBrowser newtb = new TabBrowser(url, useIE);
    		WebTabListener wtl = mb.new WebTabListener();
    		newtb.addWebBrowserListener(wtl);
    		JPanel subjp = new JPanel(new BorderLayout());
    		subjp.add(newtb, BorderLayout.CENTER);
    		mb.getTabbedPane().addTab("Tab", subjp);
    		mb.getTabbedPane().setSelectedIndex(mb.getTabbedPane().getTabCount()-1);
	}
	
	public class PopupBlockerOpenWindowRunnable implements Runnable
	{
		protected URL url;
		
		public PopupBlockerOpenWindowRunnable()
		{
		}
		
		public void run()
		{
			URL lastToOpenURL = getLastToOpenURL();
			URL popupURL = url != null ? url : lastToOpenURL;
			
	/*		
	 		URL srcURL = getURL();
			if (shouldBlockPopup(srcURL, popupURL))
			processBlockedPopup(srcURL, popupURL);
			else
	*/
			createAllowedContent(popupURL);	
		}
		
		
		/**
		* @return Returns the url.
		*/
		public URL getUrl()
		{
		return url;
		}
		
		/**
		* @param url
		* The url to set.
		*/
		public void setUrl(URL url)
		{
		this.url = url;
		}
	}
	
}
