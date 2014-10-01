package org.geworkbench.components.cytoscape;

import giny.view.NodeView;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.BrowserLauncher;

import ding.view.NodeContextMenuListener;

/* This listener is implemented only for the network created by LINCS query. */
public class CompoundExpandMenuListener implements NodeContextMenuListener,
		MouseListener {
	final static Log log = LogFactory.getLog(CompoundExpandMenuListener.class);  
    final static String PUBCHEM_URL= "http://www.ncbi.nlm.nih.gov/pccompound/?db=pccompound&term=";
    final static String DRUGBANK_URL= "http://www.drugbank.ca/search?query=";
 

	public CompoundExpandMenuListener() {
	}

	/**
	 * @param nodeView
	 *            The clicked NodeView
	 * @param menu
	 *            popup menu to add the Bypass menu
	 */
	public void addNodeContextMenuItems(final NodeView nodeView, JPopupMenu menu) {
	 
		if (menu == null) {
			menu = new JPopupMenu();
		}
		
		int count = menu.getComponentCount();
        menu.remove(count-1);
        menu.remove(count-2);
        
        String nodeId = "\"" + nodeView.getNode().getIdentifier().trim() + "\"";
        
        JMenu linkOutMenu = new JMenu("LinkOut");
        
		JMenu menuItemCompound = new JMenu("Compound databases");
	 
		linkOutMenu.add(menuItemCompound);
		 
		JMenuItem menuItemPubchem = null;
		JMenuItem menuItemDrugbank = null;
		try {
			menuItemPubchem = new JMenuItem(new LinkOutAction("Pubchem", PUBCHEM_URL  + URLEncoder.encode(nodeId, "UTF-8")));
		    menuItemDrugbank = new JMenuItem(new LinkOutAction("Drugbank",  DRUGBANK_URL  + URLEncoder.encode(nodeId, "UTF-8")));
			menuItemCompound.add(menuItemPubchem);
		    menuItemCompound.add(menuItemDrugbank);
		} catch (UnsupportedEncodingException e) {
			 log.error(e.getMessage());
		}    
        
		menu.add(linkOutMenu);
	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mouseClicked(MouseEvent e) {	 
	}

	private static final class LinkOutAction extends AbstractAction {
 
		private static final long serialVersionUID = 1L;
		String urlStr = "";

		public LinkOutAction(String name, String urlStr) {
			super(name);
			this.urlStr = urlStr;
		}

		public void actionPerformed(ActionEvent actionEvent) {
			try {

				log.info("Opening " + urlStr);
				BrowserLauncher.openURL(urlStr);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

}