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
import org.genepattern.util.GPpropertiesManager;
import org.genepattern.util.BrowserLauncher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.systemsbiology.util.InvalidInputException;

import javax.swing.*;
import javax.swing.text.StyledEditorKit;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.*;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.File;
/**
 * @author: Marc-Danie Nazaire
 */
public abstract class GPTrainingPanel extends AbstractTrainingPanel
{
    private static Log log = LogFactory.getLog(AbstractTrainingPanel.class);
    protected JTabbedPane gpTabbedPane;
    protected JPanel gpConfig;
    protected JSplitPane gpHelp;
    protected JScrollPane paramDescPanel;
    protected JScrollPane classDescPanel;
    private JFormattedTextField protocol;
    private JFormattedTextField host;
    private JFormattedTextField port;
    private JFormattedTextField username;
    private String trainLabel;

    public GPTrainingPanel(String label)
    {
        this.trainLabel = label;
        initGPUI();
    }

    private void initGPUI()
    {
        gpTabbedPane = new JTabbedPane();
        initGPConfigPanel();
        initGPHelp();
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

        protocol.setPreferredSize(new Dimension(145, 20));
        protocol.setMinimumSize(new Dimension(145, 20));
        protocol.setMaximumSize(new Dimension(145, 20));
        builder.append(new JLabel("Protocol"), protocol);
        builder.append("Whether to connect using http or https protocol");
        builder.nextLine();

        host.setPreferredSize(new Dimension(145, 20));
        host.setMinimumSize(new Dimension(145, 20));
        host.setMaximumSize(new Dimension(145, 20));
        builder.append(new JLabel("Host"), host);
        builder.append("The name or ip address of the server");
        builder.nextLine();

        port.setPreferredSize(new Dimension(145, 20));
        port.setMinimumSize(new Dimension(145, 20));
        port.setMaximumSize(new Dimension(145, 20));
        builder.append(new JLabel("Port"), port);
        builder.append("The port specified for the server");
        builder.nextLine();

        username.setPreferredSize(new Dimension(145, 20));
        username.setMinimumSize(new Dimension(145, 20));
        username.setMaximumSize(new Dimension(145, 20));
        builder.append(new JLabel("Username"), username);
        builder.append("The login username");
        builder.nextLine();

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
               saveGPConfigActionPerformed(event);
            }
        });
       
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
    protected abstract File getDescriptionFile();
    protected abstract File getParamDescriptions();

    protected void initParamDescPanel()
    {
        paramDescPanel = new JScrollPane();

        JTextPane paramDescTextPane = new JTextPane();
        paramDescTextPane.setEditorKit(new StyledEditorKit());
        try
        {
            paramDescTextPane.setPage(getParamDescriptions().toURL());
            paramDescTextPane.setEditable(false);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        JViewport viewPort = new JViewport();
        viewPort.setMinimumSize(new Dimension(220, 150));
        viewPort.setPreferredSize(new Dimension(220, 150));
        viewPort.setMaximumSize(new Dimension(220, 150));
        viewPort.setView(paramDescTextPane);
        paramDescPanel.setViewport(viewPort);       
    }

    protected void initClassDescPanel()
    {
        classDescPanel = new JScrollPane();

        JTextPane paramDescTextPane = new JTextPane();
        paramDescTextPane.setEditorKit(new StyledEditorKit());
        try
        {
            paramDescTextPane.setPage(getDescriptionFile().toURL());
            paramDescTextPane.setEditable(false);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        JViewport viewPort = new JViewport();
        viewPort.setMinimumSize(new Dimension(220, 150));
        viewPort.setPreferredSize(new Dimension(220, 150));
        viewPort.setMaximumSize(new Dimension(220, 150));
        viewPort.setView(paramDescTextPane);
        classDescPanel.setViewport(viewPort);
    }

    private void initGPHelp()
    {
        gpHelp = new JSplitPane();

        JPanel leftComponent = new JPanel();
        BoxLayout bLayout = new BoxLayout(leftComponent, BoxLayout.PAGE_AXIS);
        leftComponent.setLayout(bLayout);

        initClassDescPanel();
        JButton classifierButton = new JButton(trainLabel);
        classifierButton.setMinimumSize(new Dimension(145, 24));
        classifierButton.setPreferredSize(new Dimension(145, 24));
        classifierButton.setMaximumSize(new Dimension(145, 24));
        classifierButton.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                gpHelp.remove(gpHelp.getRightComponent());
                gpHelp.setRightComponent(classDescPanel);
            }
        });

        leftComponent.add(Box.createRigidArea(new Dimension(1, 8)));
        leftComponent.add(classifierButton);

        initParamDescPanel();
        JButton parameterButton = new JButton("Parameters");
        parameterButton.setMinimumSize(new Dimension(145, 24));
        parameterButton.setPreferredSize(new Dimension(145, 24));
        parameterButton.setMaximumSize(new Dimension(145, 24));
        parameterButton.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                gpHelp.remove(gpHelp.getRightComponent());
                gpHelp.setRightComponent(paramDescPanel);
            }
        });

        leftComponent.add(Box.createRigidArea(new Dimension(1,10)));
        leftComponent.add(parameterButton);

        JButton genePatternButton = new JButton("GenePattern Website");
        genePatternButton.setMinimumSize(new Dimension(145, 24));
        genePatternButton.setPreferredSize(new Dimension(145, 24));
        genePatternButton.setMaximumSize(new Dimension(145, 24));
        genePatternButton.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                try
                {
                    BrowserLauncher.openURL("http://www.genepattern.org");
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        JButton gpDownloadButton = new JButton("Download GenePattern");
        gpDownloadButton.setMinimumSize(new Dimension(145, 24));
        gpDownloadButton.setPreferredSize(new Dimension(145, 24));
        gpDownloadButton.setMaximumSize(new Dimension(145, 24));
        leftComponent.add(Box.createRigidArea(new Dimension(1, 10)));
        leftComponent.add(gpDownloadButton);
        gpDownloadButton.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                try
                {
                    BrowserLauncher.openURL("http://www.broad.mit.edu/cancer/software/genepattern/download/");
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        leftComponent.add(Box.createRigidArea(new Dimension(1, 10)));
        leftComponent.add(genePatternButton, BorderLayout.CENTER);
        leftComponent.setVisible(true);

        gpHelp.setDividerSize(10);
        gpHelp.setLeftComponent(leftComponent);
        gpHelp.setRightComponent(classDescPanel);
    }

    protected void addParameters(DefaultFormBuilder builder)
    {
        gpTabbedPane.removeAll();

        gpTabbedPane.addTab("Parameters", getParameterPanel());
        gpTabbedPane.addTab("GenePattern Server Settings", gpConfig);
        gpTabbedPane.addTab("Help", gpHelp);

        gpTabbedPane.setMaximumSize(new Dimension(240, 180));
        add(gpTabbedPane, BorderLayout.PAGE_START);
        builder.appendUnrelatedComponentsGapRow();
    }
}

