package org.geworkbench.components.netboost;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectTreeNode;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.*;
import org.geworkbench.util.pathwaydecoder.mutualinformation.*;

/**
 * NetBoost Component
 * @author ch2514
 * @version $Id: NetBoostVisualComponent.java,v 1.2 2007-08-31 21:16:00 hungc Exp $
 */
@AcceptTypes(NetBoostDataSet.class)
public class NetBoostVisualComponent extends JPanel implements VisualPlugin {
	// variables
	private Log log = LogFactory.getLog(this.getClass());
	
	private JPanel plugin;
    private NetBoostPlugin netboostPlugin;
    private HashMap<ProjectTreeNode, NetBoostPlugin> ht;
	
	public NetBoostVisualComponent(){
		ht = new HashMap<ProjectTreeNode, NetBoostPlugin>(5);
		plugin = new JPanel(new BorderLayout());
	}

	public Component getComponent() {
		return plugin;
	}
	
    
    @Subscribe 
    public void receive(ProjectEvent projectEvent, Object source) {    	
        log.debug("NetBoost received project event.");
        
        DSDataSet data = projectEvent.getDataSet();
        ProjectTreeNode node = ((ProjectPanel) source).getSelection().getSelectedNode();
        
        if ((data != null) && (data instanceof NetBoostDataSet)) {
        	// Check to see if the hashtable has a mindy plugin associated with the selected project tree node
        	if(ht.containsKey(node)){
        		// if so, set netboostPlugin to the one stored in the hashtable
        		netboostPlugin = (NetBoostPlugin) ht.get(node);
        		this.paintPlugin();
        	} else {
        		// if not, create a brand new netboostPlugin, add to the hashtable (with key=selected project tree node)
        		NetBoostDataSet nbdata = (NetBoostDataSet) data;
            	if(!nbdata.getData().isEmpty()) {
            		netboostPlugin = new NetBoostPlugin(nbdata, this);
            		ht.put(node, netboostPlugin);
            		this.paintPlugin();
            	}        		
        	}
        	
        }  
    }
    
    @Publish 
    public ImageSnapshotEvent createImageSnapshot(Component comp) {
        Dimension panelSize = comp.getSize();
        BufferedImage image = new BufferedImage(panelSize.width, panelSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        comp.print(g);
        ImageIcon icon = new ImageIcon(image, "Net Boost");
        org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent("Net Boost", icon, org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
        return event;
    }
    
    private void paintPlugin(){
    	plugin.removeAll();  
    	
        // Display the plugin  
    	plugin.add(netboostPlugin, BorderLayout.CENTER); 
    	plugin.revalidate();
        plugin.repaint();
    }
}
