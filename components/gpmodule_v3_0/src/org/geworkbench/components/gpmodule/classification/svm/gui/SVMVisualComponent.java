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
import org.geworkbench.events.ProjectEvent;

import javax.swing.*;
import java.awt.*;

import org.geworkbench.components.gpmodule.classification.svm.SVMClassifier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *@author Marc-Danie Nazaire
 */

@AcceptTypes( {SVMClassifier.class})
public class SVMVisualComponent implements VisualPlugin
{
    private Log log = LogFactory.getLog(this.getClass());

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
    public void receive(ProjectEvent e, Object source)
    {
        log.debug("SVMVisualComponent received project event.");
        if(e.getDataSet() != null && e.getDataSet() instanceof SVMClassifier)
        {
            component.removeAll();

            svmVPanel = new SVMVisualizationPanel((SVMClassifier)e.getDataSet());
            svmVPanel.setBorder(BorderFactory.createEmptyBorder());

            component.add(svmVPanel);

            component.revalidate();
		    component.repaint();
        }
    }
}
