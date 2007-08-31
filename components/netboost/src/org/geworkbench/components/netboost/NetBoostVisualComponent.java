package org.geworkbench.components.netboost;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.*;
import org.geworkbench.util.pathwaydecoder.mutualinformation.*;

/**
 * NetBoost Component
 * @author ch2514
 * @version $Id: NetBoostVisualComponent.java,v 1.1 2007-08-31 16:05:59 hungc Exp $
 */
@AcceptTypes(NetBoostDataSet.class)
public class NetBoostVisualComponent extends JPanel implements VisualPlugin {
	// variables
	private Log log = LogFactory.getLog(this.getClass());
	
	private JPanel plugin;
    private NetBoostPlugin netboostPlugin;
	
	public NetBoostVisualComponent(){
		plugin = new JPanel(new BorderLayout());
	}

	public Component getComponent() {
		return plugin;
	}
	
    
    @Subscribe 
    public void receive(ProjectEvent projectEvent, Object source) {    	
        log.debug("NetBoost received project event.");
        
        DSDataSet data = projectEvent.getDataSet();
        if ((data != null) && (data instanceof NetBoostDataSet)) {
        	NetBoostDataSet nbdata = (NetBoostDataSet) data;
        	if(!nbdata.getData().isEmpty()) {
        		this.paintPlugin(nbdata.getData());
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
    
    private void paintPlugin(NetBoostData nbdata){
    	plugin.removeAll();
        
        netboostPlugin = new NetBoostPlugin(nbdata, this);
        
        // Display the plugin  
    	plugin.add(netboostPlugin, BorderLayout.CENTER); 
    	plugin.revalidate();
        plugin.repaint();
    }
}
