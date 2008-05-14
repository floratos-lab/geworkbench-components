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
package org.geworkbench.components.gpmodule;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.DefaultFormBuilder;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

import org.genepattern.util.GPpropertiesManager;
import org.systemsbiology.util.InvalidInputException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author: Marc-Danie Nazaire 
 */
public class GPConfigPanel extends JPanel
{
    private static Log log = LogFactory.getLog(GPConfigPanel.class);
    private JFormattedTextField protocol;
    private JFormattedTextField host;
    private JFormattedTextField port;
    private JFormattedTextField username;
    private JPasswordField password;
    private static String passwordValue = null;

    public GPConfigPanel()
    {
        initGPConfigPanel();        
    }

    public void initGPConfigPanel()
    {
        FormLayout layout = new FormLayout(
                    "right:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(70dlu;pref)",
                    "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        DefaultFormatter defaultFormatter = new DefaultFormatter();
        defaultFormatter.setOverwriteMode(false);

        protocol = new JFormattedTextField(defaultFormatter);
        host = new JFormattedTextField(defaultFormatter);
        port = new JFormattedTextField(defaultFormatter);
        username = new JFormattedTextField(defaultFormatter);
        password = new JPasswordField();

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

        password.setPreferredSize(new Dimension(145, 20));
        password.setMinimumSize(new Dimension(145, 20));
        password.setMaximumSize(new Dimension(145, 20));
        builder.append(new JLabel("Password"), password);
        builder.append("The login password (if required)");

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
               saveGPConfigActionPerformed(event);
            }
        });

        builder.append(new JPanel(), saveButton);

        this.add(builder.getPanel());
        this.addComponentListener( new ComponentListener()
        {
            public void componentShown(ComponentEvent event)
            {
                //resetGPConfigParameters();
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

        if(GPpropertiesManager.getProperty("gp.user.name") != null)
            username.setValue(GPpropertiesManager.getProperty("gp.user.name"));

        if(gpServer != null)
        {
            protocol.setValue(gpServer.getProtocol());
            host.setValue(gpServer.getHost());
            port.setValue(String.valueOf(gpServer.getPort()));

            if(passwordValue != null)
                password.setText(passwordValue);
        }
        else
        {
            protocol.setValue("");
            host.setValue("");
            port.setValue("");
            username.setValue("");

            passwordValue = null;
            password.setText(passwordValue);
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
                int portInput = (Integer.valueOf((String)port.getValue())).intValue();
                String userNameInput = (String)username.getValue();

                URL gpServer = new URL(protocolInput, hostInput , portInput, "");
                GPpropertiesManager.setProperty("gp.server", gpServer.toString());
                GPpropertiesManager.setProperty("gp.user.name", userNameInput);
                passwordValue = String.valueOf(password.getPassword());

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

    private void validateInput() throws InvalidInputException
    {
        try
        {
            if(protocol.getValue() == null || ((String)protocol.getValue()).length() == 0)
            {
                throw new InvalidInputException("Protocol must be provided");
            }
            else if(host.getValue() == null || ((String)host.getValue()).length() == 0)
            {
                throw new InvalidInputException("Host must be provided");
            }
            else if(port.getValue() == null || ((String)port.getValue()).matches("[^0-9+]") ||  Integer.valueOf((String)port.getValue()).intValue() == -1)
            {
                throw new InvalidInputException("Invalid port setting");
            }
            else if(username.getValue() == null || ((String)username.getValue()).length() == 0)
            {
                throw new InvalidInputException("Username must be provided");
            }
        }
        catch(NumberFormatException nf)
        {
            log.error(nf);
            throw new InvalidInputException("Invalid server setting");
        }
    }

    protected String getPassword()
    {
        return new String(password.getPassword());
    }
    
    public String getProtocol(){
    	return protocol.getText();
    }
    
    public void setProtocol(String s){
    	protocol.setText(s);
    }
    
    public String getHost(){
    	return host.getText();
    }
    
    public void setHost(String s){
    	host.setText(s);
    }
    
    public String getPort(){
    	return port.getText();
    }
    
    public void setPort(String s){
    	port.setText(s);
    }
    
    public String getUserName(){
    	return username.getText();
    }
    
    public void setUserName(String s){
    	username.setText(s);
    }
}
