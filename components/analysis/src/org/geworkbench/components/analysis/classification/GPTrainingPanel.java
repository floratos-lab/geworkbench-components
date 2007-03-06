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
package org.geworkbench.components.analysis.classification;

import org.geworkbench.algorithms.AbstractTrainingPanel;
import org.genepattern.util.GPpropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.systemsbiology.util.InvalidInputException;

import javax.swing.*;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.*;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

/**
 * @author: Marc-Danie Nazaire
 */
public abstract class GPTrainingPanel extends AbstractTrainingPanel
{
    private static Log log = LogFactory.getLog(AbstractTrainingPanel.class);
    protected JTabbedPane gpTabbedPane;
    protected JPanel gpConfig;
    private JFormattedTextField protocol;
    private JFormattedTextField host;
    private JFormattedTextField port;
    private JFormattedTextField username;

    public GPTrainingPanel()
    {
        initGPUI();
    }

    private void initGPUI()
    {
        gpTabbedPane = new JTabbedPane();
        initGPConfigPanel();
    }

    private void initGPConfigPanel()
    {
        gpConfig = new JPanel();
        FormLayout layout = new FormLayout(
                    "right:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(70dlu;pref)",
                    "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        protocol = new JFormattedTextField("");
        host = new JFormattedTextField("");
        port = new JFormattedTextField("");
        username = new JFormattedTextField("");

        resetGPConfigParameters();

        protocol.setPreferredSize(new Dimension(170, 20));
        protocol.setMinimumSize(new Dimension(170, 20));
        protocol.setMaximumSize(new Dimension(170, 20));
        builder.append(new JLabel("Protocol"), protocol);
        builder.nextLine();

        host.setPreferredSize(new Dimension(170, 20));
        host.setMinimumSize(new Dimension(170, 20));
        host.setMaximumSize(new Dimension(170, 20));
        builder.append(new JLabel("Host"), host);
        builder.nextLine();

        port.setPreferredSize(new Dimension(170, 20));
        port.setMinimumSize(new Dimension(170, 20));
        port.setMaximumSize(new Dimension(170, 20));
        builder.append(new JLabel("Port"), port);
        builder.nextLine();

        username.setPreferredSize(new Dimension(170, 20));
        username.setMinimumSize(new Dimension(170, 20));
        username.setMaximumSize(new Dimension(170, 20));
        builder.append(new JLabel("Username"), username);
        builder.nextLine();

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
               saveGPConfigActionPerformed(event);
            }
        });

        builder.nextColumn();
        builder.append(new JPanel(), saveButton);

        gpConfig.add(builder.getPanel());
        gpConfig.addComponentListener( new ComponentListener()
        {
            public void componentShown(ComponentEvent event)
            {
                resetGPConfigParameters();                        
            }

            public void componentResized(ComponentEvent event){}
            public void componentMoved(ComponentEvent event){}
            public void componentHidden(ComponentEvent event){}
        });
    }

    private void resetGPConfigParameters()
    {
        String serverName = GPpropertiesManager.getProperty("gp.server");

        URL gpServer = null;
        try
        {
            if(serverName != null)
                gpServer = new URL(serverName);
        }
        catch(MalformedURLException e)
        {
            log.debug(e.getMessage());
        }

        if(gpServer != null)
        {
            protocol.setValue(gpServer.getProtocol());
            host.setValue(gpServer.getHost());
            port.setValue(new Integer(gpServer.getPort()));
        }
        else
        {
            protocol.setValue("");
            host.setValue("");
            port.setValue("");
        }

        if(GPpropertiesManager.getProperty("gp.user.name") != null)
            username.setValue(GPpropertiesManager.getProperty("gp.user.name"));
        else
            username.setValue("");
    }
    
    private void validateInput() throws InvalidInputException
    {
        if(protocol.getValue() == null || ((String)protocol.getValue()).length() == 0)
        {
            throw new InvalidInputException("Protocol must be provided");
        }
        else if(host.getValue() == null || ((String)host.getValue()).length() == 0)
        {
            throw new InvalidInputException("Host must be provided");
        }
        else if(port.getValue() == null || ((Integer)port.getValue()).intValue() == -1)
        {
            throw new InvalidInputException("Port must be provided");
        }
        else if(username.getValue() == null || ((String)username.getValue()).length() == 0)
        {
            throw new InvalidInputException("Username must be provided");
        }
    }
    private void saveGPConfigActionPerformed(ActionEvent event)
    {
        if(event.getActionCommand().equalsIgnoreCase("Save"))
        {
            try
            {
                validateInput();
                String protocolInput = (String)protocol.getValue();
                String hostInput = (String)host.getValue();
                int portInput = ((Integer)port.getValue()).intValue();
                String userNameInput = (String)username.getValue();
                
                URL gpServer = new URL(protocolInput, hostInput , portInput, "");
                GPpropertiesManager.setProperty("gp.server", gpServer.toString());
                GPpropertiesManager.setProperty("gp.user.name", userNameInput);
                GPpropertiesManager.saveGenePatternProperties();
                JOptionPane.showMessageDialog(this, "GenePattern server settings saved");
            }
            catch(IOException e)
            {
                log.debug(e.getMessage());
                resetGPConfigParameters();
                JOptionPane.showMessageDialog(this, "Problem saving GenePattern server settings");
            }
            catch(InvalidInputException e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }             
        }
    }

    protected abstract JPanel getParameterPanel();

    protected void addParameters(DefaultFormBuilder builder) 
    {
        gpTabbedPane.removeAll();
        
        gpTabbedPane.addTab("Parameters", getParameterPanel());
        gpTabbedPane.addTab("GenePattern Server Settings", gpConfig);

        add(gpTabbedPane, BorderLayout.PAGE_START);
        builder.appendUnrelatedComponentsGapRow();
    }
}
