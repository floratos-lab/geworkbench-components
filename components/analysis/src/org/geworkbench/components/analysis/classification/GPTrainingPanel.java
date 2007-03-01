package org.geworkbench.components.analysis.classification;

import org.geworkbench.algorithms.AbstractTrainingPanel;
import org.genepattern.util.GPpropertiesManager;

import javax.swing.*;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * @author: Marc-Danie Nazaire
 */
public abstract class GPTrainingPanel extends AbstractTrainingPanel {
    private JTabbedPane tabbedPane;
    private JPanel gpConfig;
    private JFormattedTextField protocol;
    private JFormattedTextField host;
    private JFormattedTextField port;
    private JFormattedTextField userName;

    public GPTrainingPanel()
    {
        initGPUI();
    }

    private void initGPUI()
    {
        tabbedPane = new JTabbedPane();

        gpConfig = new JPanel();
        FormLayout layout = new FormLayout(
                    "right:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(70dlu;pref)",
                    "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        String serverName = GPpropertiesManager.getProperty("gp.server");
        URL gpServer = null;
        try
        {
            gpServer = new URL(serverName);
        }
        catch(MalformedURLException e)
        {
            JOptionPane.showMessageDialog(this, "Invalid GenePattern server: " + (serverName == null ? "No server provided":serverName));
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

        if(GPpropertiesManager.getProperty("gp.user.name") != null)
            userName = new JFormattedTextField(GPpropertiesManager.getProperty("gp.user.name"));
        else
             userName = new JFormattedTextField();
        
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

    private void saveGPConfigActionPerformed(ActionEvent event)
    {
        if(event.getActionCommand().equalsIgnoreCase("Save"))
        {
            try
            {
                URL gpServer = new URL(protocol.getText(), host.getText(), port.getText());
                GPpropertiesManager.setProperty("gp.server", gpServer.toString());
                GPpropertiesManager.setProperty("gp.user.name", userName.getText());
                GPpropertiesManager.saveGenePatternProperties();
                JOptionPane.showMessageDialog(this, "GenePattern server settings saved");
            }
            catch(IOException io)
            {
                io.printStackTrace();
                JOptionPane.showMessageDialog(this, "Problem saving GenePattern server settings");
            }            
        }
    }

    protected abstract JPanel getParameterPanel();

    protected void addParameters(DefaultFormBuilder builder) 
    {
        tabbedPane.removeAll();
        tabbedPane.addTab("Parameters", getParameterPanel());
        tabbedPane.addTab("GenePattern Server Settings", gpConfig);

        add(tabbedPane, BorderLayout.PAGE_START);
        builder.appendUnrelatedComponentsGapRow();
    }
}
