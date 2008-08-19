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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.Vector;
import java.util.Arrays;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.genepattern.util.GPpropertiesManager;
import org.genepattern.webservice.AdminProxy;
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
    private JFrame editSettingsFrame;
    private JTable serverSettingsTable;
    private DefaultFormBuilder builder;

    public GPConfigPanel()
    {
        initGPConfigPanel();        
    }

    public void initGPConfigPanel()
    {
        FormLayout layout = new FormLayout(
                    "right:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(70dlu;pref)",
                    "");
        builder = new DefaultFormBuilder(layout);
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
        builder.nextLine();

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                boolean save = saveGPConfigActionPerformed(event);
                if(save)
                {
                    editSettingsFrame.dispose();
                    rebuildTable();
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
               resetGPConfigParameters();
               editSettingsFrame.dispose();    
            }
        });

        builder.setLeadingColumnOffset(2);

        JPanel optionsPanel = new JPanel();
        optionsPanel.add(cancelButton);
        optionsPanel.add(saveButton);
        builder.append(optionsPanel);

        this.addComponentListener( new ComponentListener()
        {
            public void componentShown(ComponentEvent event)
            {
                resetGPConfigParameters();
            }

            public void componentResized(ComponentEvent event){}
            public void componentMoved(ComponentEvent event){}
            public void componentHidden(ComponentEvent event){}
        });

        serverSettingsTable = new JTable()
        {
            public TableCellRenderer getCellRenderer(int row, int column)
            {
                return new MyCellRenderer();
            }

            public boolean getScrollableTracksViewportHeight()
            {
                Component parent = getParent();
                if(parent instanceof JViewport)
                    return parent.getHeight() > getPreferredSize().height;

                return false;
            }

            public boolean getScrollableTracksViewportWidth()
            {
                Component parent = getParent();
                if(parent instanceof JViewport)
                    return parent.getWidth() > getPreferredSize().width;

                return false;
            }                                 
        };

        rebuildTable();
        JScrollPane tableScrollPane = new JScrollPane(serverSettingsTable);
        tableScrollPane.setBackground(this.getBackground());

        editSettingsFrame = new JFrame();
        JButton modifyButton = new JButton("Modify");
        modifyButton.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                editSettingsFrame.setTitle("Edit GenePattern Server Settings");
                editSettingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                builder.getPanel().setVisible(true);

                editSettingsFrame.getContentPane().add(builder.getPanel());
                editSettingsFrame.pack();
                                       
                // center on the screen
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	            editSettingsFrame.setLocation((screenSize.width - editSettingsFrame.getWidth()) / 2,
                        (screenSize.height - editSettingsFrame.getHeight()) / 2);

                editSettingsFrame.setAlwaysOnTop(true);

                editSettingsFrame.setVisible(true);

                editSettingsFrame.getContentPane().repaint();
            }
        });
        modifyButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(tableScrollPane);
        panel.add(Box.createRigidArea(new Dimension(2, 0)));
        panel.add(modifyButton);
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(panel);
    }

    private void showMessageDialog(String message)
    {
        final JOptionPane optionPane = new JOptionPane(
                message,
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION);
        optionPane.setMessage(message);

        final JFrame frame = new JFrame();
        frame.setTitle("GenePattern Server Settings");
        frame.setAlwaysOnTop(true);
        frame.getContentPane().add(optionPane);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();

        // center on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    frame.setLocation((screenSize.width - frame.getWidth()) / 2,
            (screenSize.height - frame.getHeight()) / 2);

        optionPane.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent e)
            {
                String prop = e.getPropertyName();

                if (frame.isVisible() && (e.getSource() == optionPane)
                    && (prop.equals(JOptionPane.VALUE_PROPERTY)))
                {
                    frame.dispose();
                }
            }
        });
        frame.setVisible(true);
        frame.getContentPane().repaint();
    }

    private void rebuildTable()
    {
        String[] columnNames = {"Name", "Value", "Description"};
        DefaultTableModel model = new DefaultTableModel()
        {
            public boolean isCellEditable(int r, int c)
            {
                return false;
            }
        };

        model.setColumnIdentifiers(columnNames);

        Vector protocolRow = new Vector();
        protocolRow.add("Protocol");
        protocolRow.add(protocol.getValue());
        protocolRow.add("Whether to connect using http or https protocol");

        Vector hostRow = new Vector();
        hostRow.add("Host");
        hostRow.add(host.getValue());
        hostRow.add("The name or ip address of the server");

        Vector portRow = new Vector();
        portRow.add("Port");
        portRow.add(port.getValue());
        portRow.add("The port specified for the server");

        Vector userNameRow = new Vector();
        userNameRow.add("Username");
        userNameRow.add(username.getValue());
        userNameRow.add("The login username");

        Vector passwordRow = new Vector();
        passwordRow.add("Password");

        char[] pw = password.getPassword();

        if( pw == null || pw.length == 0)
        {
            passwordRow.add("");
        }
        else
        {
            Arrays.fill(pw, password.getEchoChar());            
            passwordRow.add(String.valueOf(pw));
        }

        passwordRow.add("The login password");

        model.addRow(protocolRow);
        model.addRow(hostRow);
        model.addRow(portRow);
        model.addRow(userNameRow);
        model.addRow(passwordRow);

        serverSettingsTable.setBackground(this.getBackground());
        serverSettingsTable.setModel(model);
        serverSettingsTable.setGridColor(Color.GRAY);
        serverSettingsTable.setVisible(true);
        serverSettingsTable.setCellSelectionEnabled(false);
        serverSettingsTable.getColumnModel().getColumn(0).setPreferredWidth(12);
        serverSettingsTable.getColumnModel().getColumn(0).setWidth(12);
        serverSettingsTable.setRowHeight(24);
    }

    private boolean testConfigSettings(String serverName, String userName, String password)
    {
        try
        {
            AdminProxy admin = new AdminProxy(serverName, userName, password);
            admin.getAllTasks();
        }
        catch(Exception e)
        {
            log.info(e);

            if(e.getMessage() != null && e.getMessage().contains("Unknown user"))
            {
                showMessageDialog("Unknown user or invalid password");
            }
            else
            {
                showMessageDialog("Could not connect to the GenePattern server at " + serverName + "/gp" +  
                                    "\nPlease verify the settings and that the GenePattern server is running");
            }
            
            return false;
        }

        return true;
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

    private boolean saveGPConfigActionPerformed(ActionEvent event)
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
                passwordValue = String.valueOf(password.getPassword());

                URL gpServer = new URL(protocolInput, hostInput , portInput, "");

                boolean result = testConfigSettings(gpServer.toString(), userNameInput, passwordValue);
                if(!result)
                {
                    return false;
                }

                GPpropertiesManager.setProperty("gp.server", gpServer.toString());
                GPpropertiesManager.setProperty("gp.user.name", userNameInput);

                GPpropertiesManager.saveGenePatternProperties();
                showMessageDialog("GenePattern server settings saved");

                return true;
            }
            catch(IOException e)
            {
                log.debug(e.getMessage());
                resetGPConfigParameters();
                showMessageDialog("Problem saving GenePattern server settings");
            }
            catch(InvalidInputException e)
            {
                showMessageDialog(e.getMessage());
            }
        }

        return false;
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
            else if(port.getValue() == null || ((String)port.getValue()).length() == 0 || !((String)port.getValue()).matches("[0-9]+") ||  Integer.valueOf((String)port.getValue()).intValue() <= -1)
            {
                if(port == null || ((String)port.getValue()).length() == 0)
                    throw new InvalidInputException("Port must be provided");

                throw new InvalidInputException("Invalid port setting: " + port.getValue());
            }
            else if(username.getValue() == null || ((String)username.getValue()).length() == 0)
            {
                throw new InvalidInputException("Username must be provided");
            }
        }
        catch(NumberFormatException nf)
        {
            log.error(nf);
            throw new InvalidInputException("Invalid value found for GenePattern server setting.");
        }
    }

    public String getPassword()
    {
        return new String(password.getPassword());
    }

    private class MyCellRenderer extends JTextArea implements TableCellRenderer
    {
	    public MyCellRenderer() {
	       setLineWrap(true);
	       setWrapStyleWord(true);
        }
        public Color getBackground()
        {
            if(getParent()!= null)
                return getParent().getBackground();

            return  super.getBackground();
        }
        public Component getTableCellRendererComponent(JTable table, Object
	           value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            setText(value.toString());
            setFont(new Font("TestFont", Font.PLAIN, 13));
            setEditable(false);
            setAlignmentX(JTable.BOTTOM_ALIGNMENT);
            setSize(table.getColumnModel().getColumn(column).getWidth(),
	               getPreferredSize().height);
	        if(table.getRowHeight(row) != getPreferredSize().height) {
	               table.setRowHeight(row, getPreferredSize().height);
	        }
	        return this;
	    }
	}
}
