package org.geworkbench.components.analysis.classification;

import org.geworkbench.algorithms.AbstractTrainingPanel;
import org.genepattern.util.GPpropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * @author: Marc-Danie Nazaire
 */
public abstract class GPTrainingPanel extends AbstractTrainingPanel
{
    private static Log log = LogFactory.getLog(AbstractTrainingPanel.class);
    protected JTabbedPane gpTabbedPane;
    protected JPanel gpConfig;
    private static JFormattedTextField protocol;
    private static JFormattedTextField host;
    private static JFormattedTextField port;
    private static JFormattedTextField userName;

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

        resetGPConfigParameters();

        protocol.setPreferredSize(new Dimension(170, 20));
        protocol.setMinimumSize(new Dimension(170, 20));
        protocol.setMaximumSize(new Dimension(170, 20));
        builder.append(new JLabel("Protocol:"), protocol);
        builder.nextLine();

        host.setPreferredSize(new Dimension(170, 20));
        host.setMinimumSize(new Dimension(170, 20));
        host.setMaximumSize(new Dimension(170, 20));
        builder.append(new JLabel("Host:"), host);
        builder.nextLine();

        port.setPreferredSize(new Dimension(170, 20));
        port.setMinimumSize(new Dimension(170, 20));
        port.setMaximumSize(new Dimension(170, 20));
        builder.append(new JLabel("Port:"), port);
        builder.nextLine();

        userName.setPreferredSize(new Dimension(170, 20));
        userName.setMinimumSize(new Dimension(170, 20));
        userName.setMaximumSize(new Dimension(170, 20));
        builder.append(new JLabel("Username:"), userName);
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
    }

    private void resetGPConfigParameters()
    {
        String serverName = GPpropertiesManager.getProperty("gp.server");
        URL gpServer = null;
        try
        {
            gpServer = new URL(serverName);
        }
        catch(MalformedURLException e)
        {
            log.debug(e.getMessage());
        }

        if(gpServer == null)
        {
            protocol = new JFormattedTextField();
            host = new JFormattedTextField();
            port = new JFormattedTextField();
        }
        else
        {
            protocol = new JFormattedTextField(gpServer.getProtocol());
            host = new JFormattedTextField(gpServer.getHost());
            port = new JFormattedTextField(String.valueOf(gpServer.getPort()));
        }

        if(GPpropertiesManager.getProperty("gp.user.name") != null)
            userName = new JFormattedTextField(GPpropertiesManager.getProperty("gp.user.name"));
        else
             userName = new JFormattedTextField();
    }
    
    private void saveGPConfigActionPerformed(ActionEvent event)
    {
        if(event.getActionCommand().equalsIgnoreCase("Save"))
        {
            try
            {
                String protocolInput = protocol.getText();
                String hostInput = host.getText();
                int portInput = Integer.parseInt(port.getText());
                String userNameInput = userName.getText();

                if(protocolInput == null || protocolInput.equals("") ||
                        hostInput == null || hostInput.equals("") ||
                        portInput == -1 || userNameInput == null || userNameInput.equals(""))
                    throw new Exception();
                
                URL gpServer = new URL(protocolInput, hostInput , portInput, "");
                GPpropertiesManager.setProperty("gp.server", gpServer.toString());
                GPpropertiesManager.setProperty("gp.user.name", userNameInput);
                GPpropertiesManager.saveGenePatternProperties();
                JOptionPane.showMessageDialog(this, "GenePattern server settings saved");
            }
            catch(Exception e)
            {
                log.debug(e.getMessage());
                resetGPConfigParameters();
                JOptionPane.showMessageDialog(this, "Problem saving GenePattern server settings");
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
