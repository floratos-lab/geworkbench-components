/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2007) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.gpmodule.classification;

import org.geworkbench.algorithms.AbstractTrainingPanel;
import org.geworkbench.components.gpmodule.GPConfigPanel;
import org.geworkbench.components.gpmodule.GPHelpPanel;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import javax.swing.*;
import java.awt.*;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;

/**
 * @author: Marc-Danie Nazaire
 */
public abstract class GPTrainingPanel extends AbstractTrainingPanel
{
    private JTabbedPane gpTabbedPane;
    private GPConfigPanel gpConfigPanel;
    private GPHelpPanel gpHelpPanel;
    protected ParameterPanel parameterPanel;

    public GPTrainingPanel(String label)
    {
        gpTabbedPane = new JTabbedPane();
        gpConfigPanel = new GPConfigPanel();
        gpConfigPanel.setPreferredSize(getPreferredSize());
        gpConfigPanel.setMinimumSize(getPreferredSize());
        gpConfigPanel.setMaximumSize(getPreferredSize());
        gpHelpPanel = new GPHelpPanel(label, getParamDescriptFile(), getSummaryFile());
    }

    protected abstract JPanel getParameterPanel();
    protected abstract String getSummaryFile();
    protected abstract String getParamDescriptFile();


    protected void init()
    {
        setLayout(new BorderLayout());
    }

    protected JLabel getGPLogo()
    {
        java.net.URL imageURL = GPTrainingPanel.class.getResource("images/gp-logo.gif");
        ImageIcon image = new ImageIcon(imageURL);

        JLabel label = new JLabel();
        label.setIcon(image);

        return label;
    }
    
    protected void addParameters(DefaultFormBuilder builder)
    {
        gpTabbedPane.removeAll();

        JPanel parameterPanel = getParameterPanel();
        gpTabbedPane.addTab("Parameters", parameterPanel);
        gpTabbedPane.addTab("GenePattern Server Settings", gpConfigPanel);
        gpTabbedPane.addTab("Help", gpHelpPanel);

        CellConstraints cc = new CellConstraints();
        builder.append("");
        
        builder.add(gpTabbedPane, cc.xywh(1, builder.getRow(), 7, 1));
    }

    public String getPassword()
    {
        return gpConfigPanel.getPassword();
    }

    public GPConfigPanel getConfigPanel()
    {
        return gpConfigPanel;
    }
}