package org.geworkbench.components.mindy;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyDataSet;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.swing.*;
import java.awt.*;

/**
 * @author mhall
 */
@AcceptTypes(MindyDataSet.class)
public class MindyVisualComponent implements VisualPlugin {

    static Log log = LogFactory.getLog(MindyVisualComponent.class);

    private MindyDataSet dataSet;
    private JPanel plugin;

    public MindyVisualComponent() {
        // Just a place holder
        plugin = new JPanel(new BorderLayout());
    }

    public Component getComponent() {
        return plugin;
    }

    @Subscribe public void receive(ProjectEvent projectEvent, Object source) {
        log.debug("MINDY received project event.");
        DSDataSet data = projectEvent.getDataSet();
        if ((data != null) && (data instanceof MindyDataSet)) {
            dataSet = ((MindyDataSet) data);
            plugin.removeAll();
            plugin.add(new MindyPlugin(dataSet.getData()), BorderLayout.CENTER);
//            plugin.revalidate();
            plugin.repaint();
        }
    }

}
