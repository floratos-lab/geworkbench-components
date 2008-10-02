/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2008) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.gpmodule.classification.svm.gui;

import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.ProjectEvent;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import org.geworkbench.components.gpmodule.classification.svm.SVMClassifier;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *@author Marc-Danie Nazaire
 */

@AcceptTypes( {SVMClassifier.class})
public class SVMVisualComponent implements VisualPlugin
{
    private Log log = LogFactory.getLog(this.getClass());
    public static SortedMap microarraySets = new TreeMap();

    private JPanel component;
    private SVMVisualizationPanel svmVPanel;

    public SVMVisualComponent()
    {
        component = new JPanel(new BorderLayout());
    }

    public Component getComponent()
    {
        return component;
    }

    @Subscribe
    public void receive(org.geworkbench.events.ProjectNodeAddedEvent e, Object source)
    {
        System.out.println("SVMVisualComponent received project node added event.");
        DSDataSet dataSet = e.getDataSet();

        if (dataSet instanceof DSMicroarraySet)
        {
            microarraySets.put(dataSet.getDataSetName(), dataSet);
		}
    }

    @Subscribe
    public void receive(org.geworkbench.events.ProjectNodeRemovedEvent e, Object source)
    {
        System.out.println("SVMVisualComponent received project node removed event.");
        DSDataSet dataSet = e.getDataSet();

        if (dataSet instanceof DSMicroarraySet)
        {
            microarraySets.remove(dataSet.getDataSetName());
		}
	}

    @Subscribe
    public void receive(org.geworkbench.events.ProjectNodeRenamedEvent e, Object source)
    {
        System.out.println("SVMVisualComponent received project node renamed event.");
        DSDataSet dataSet = e.getDataSet();

        if (dataSet instanceof DSMicroarraySet)
        {
            String oldName = e.getOldName();
            Iterator it = microarraySets.keySet().iterator();
            while(it.hasNext())
            {
                String key = (String)it.next();
                DSMicroarraySet microarraySet = (DSMicroarraySet)microarraySets.get(key);

                if(oldName.equals(microarraySet.getDataSetName()))
                {
                    microarraySets.remove(microarraySet.getDataSetName());
                    break;
                }
            }

            microarraySets.put(dataSet.getDataSetName(),dataSet);
		}
	}

    @Subscribe
    public void receive(org.geworkbench.events.ProjectEvent e, Object source)
    {
        System.out.println("SVMVisualComponent received project event.");

        DSDataSet dataSet = e.getDataSet();
        if(dataSet != null && dataSet instanceof SVMClassifier)
        {
            component.removeAll();

            svmVPanel = new SVMVisualizationPanel((SVMClassifier)e.getDataSet(), this);
            svmVPanel.setBorder(BorderFactory.createEmptyBorder());

            component.add(svmVPanel);

            component.revalidate();
		    component.repaint();
        }

        if (dataSet instanceof DSMicroarraySet)
        {
            microarraySets.put(dataSet.getDataSetName(),dataSet);
		}
    }

    @Publish
    public org.geworkbench.events.SubpanelChangedEvent publishSubpanelChangedEvent(org.geworkbench.events.SubpanelChangedEvent event)
    {
        return event;
    }

}