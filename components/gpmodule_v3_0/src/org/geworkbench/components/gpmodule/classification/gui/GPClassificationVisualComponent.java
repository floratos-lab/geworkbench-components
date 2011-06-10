/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2010) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.gpmodule.classification.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.geworkbench.bison.algorithm.classification.CSVisualClassifier;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.components.gpmodule.classification.VisualGPClassifier;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;

/**
 *@author Marc-Danie Nazaire
 *@version $Id$
 */

@AcceptTypes( {CSVisualClassifier.class})
public class GPClassificationVisualComponent implements VisualPlugin
{
    public static SortedMap<String, DSMicroarraySet<DSMicroarray>> microarraySets = new TreeMap<String, DSMicroarraySet<DSMicroarray>>();

    protected JPanel component;
    protected GPClassificationVisualizationPanel gpClassVisPanel;

    public GPClassificationVisualComponent()
    {
        component = new JPanel(new BorderLayout());
    }

    public Component getComponent()
    {
        return component;
    }

    @SuppressWarnings("unchecked")
	@Subscribe
    public void receive(org.geworkbench.events.ProjectNodeAddedEvent e, Object source)
    {
        DSDataSet<?> dataSet = e.getDataSet();

        if (dataSet instanceof DSMicroarraySet)
        {
            microarraySets.put(dataSet.getDataSetName(), (DSMicroarraySet<DSMicroarray>)dataSet);
		}
    }

    @Subscribe
    public void receive(org.geworkbench.events.ProjectNodeRemovedEvent e, Object source)
    {
        DSDataSet<?> dataSet = e.getDataSet();

        if (dataSet instanceof DSMicroarraySet)
        {
            microarraySets.remove(dataSet.getDataSetName());
		}
	}

    @SuppressWarnings("unchecked")
	@Subscribe
    public void receive(org.geworkbench.events.ProjectNodeRenamedEvent e, Object source)
    {
        DSDataSet<?> dataSet = e.getDataSet();

        if (dataSet instanceof DSMicroarraySet)
        {
            String oldName = e.getOldName();
            Iterator<String> it = microarraySets.keySet().iterator();
            while(it.hasNext())
            {
                String key = it.next();
                @SuppressWarnings("rawtypes")
				DSMicroarraySet microarraySet = (DSMicroarraySet)microarraySets.get(key);

                if(oldName.equals(microarraySet.getDataSetName()))
                {
                    microarraySets.remove(microarraySet.getDataSetName());
                    break;
                }
            }

            microarraySets.put(dataSet.getDataSetName(), (DSMicroarraySet<DSMicroarray>)dataSet);
		}
	}

    @SuppressWarnings("unchecked")
	@Subscribe
    public void receive(org.geworkbench.events.ProjectEvent e, Object source)
    {
        DSDataSet<?> dataSet = e.getDataSet();
        if(dataSet != null && dataSet instanceof VisualGPClassifier)
        {
            component.removeAll();

            VisualGPClassifier gpVisClassifier = (VisualGPClassifier)dataSet;

            gpClassVisPanel = new GPClassificationVisualizationPanel(gpVisClassifier, this);
            gpClassVisPanel.setBorder(BorderFactory.createEmptyBorder());

            component.add(gpClassVisPanel);

            component.revalidate();
		    component.repaint();
        }

        if (dataSet instanceof DSMicroarraySet)
        {
            microarraySets.put(dataSet.getDataSetName(), (DSMicroarraySet<DSMicroarray>)dataSet);
		}
    }

    @Publish
    public org.geworkbench.events.SubpanelChangedEvent<DSMicroarray> publishSubpanelChangedEvent(org.geworkbench.events.SubpanelChangedEvent<DSMicroarray> event)
    {
        return event;
    }
}
