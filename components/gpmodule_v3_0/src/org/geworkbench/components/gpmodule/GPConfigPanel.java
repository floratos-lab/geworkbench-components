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
import javax.swing.table.TableColumn;
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
import org.genepattern.webservice.TaskInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.components.gpmodule.event.ServerConnectionEvent;
import org.geworkbench.components.gpmodule.listener.ServerConnectionListener;

/**
 * @author: Marc-Danie Nazaire 
 * @version $Id$
 */
public class GPConfigPanel extends JPanel
{
	private static final long serialVersionUID = -218725992804879126L;
	
	private boolean highlightPassword = false;

    private static class InvalidInputException extends Exception {
		private static final long serialVersionUID = -667139707540590720L;

		InvalidInputException(String message) {
    		super(message);
    	}
	}

	private static Log log = LogFactory.getLog(GPConfigPanel.class);
    private JFormattedTextField protocol;
    private JFormattedTextField host;
    private JFormattedTextField port;
    private JFormattedTextField username;
    private JPasswordField password;
    private static String passwordValue = null;
    private JDialog editSettingsFrame;
    private JTable serverSettingsTable;
    private DefaultFormBuilder builder;
    public static ServerConnectionListener listener = null;
    private TaskInfo moduleInfo = null;

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

        resetGPConfigParameters(true);

//        protocol.setPreferredSize(new Dimension(145, 20));
//        protocol.setMinimumSize(new Dimension(145, 20));
//        protocol.setMaximumSize(new Dimension(145, 20));
        builder.append(new JLabel("Protocol"), protocol);
        builder.append("Whether to connect using http or https protocol");
        builder.nextLine();

//        host.setPreferredSize(new Dimension(145, 20));
//        host.setMinimumSize(new Dimension(145, 20));
//        host.setMaximumSize(new Dimension(145, 20));
        builder.append(new JLabel("Host"), host);
        builder.append("The name or ip address of the server");
        builder.nextLine();    

//        port.setPreferredSize(new Dimension(145, 20));
//        port.setMinimumSize(new Dimension(145, 20));
//        port.setMaximumSize(new Dimension(145, 20));
        builder.append(new JLabel("Port"), port);
        builder.append("The port specified for the server");
        builder.nextLine();

//        username.setPreferredSize(new Dimension(145, 20));
//        username.setMinimumSize(new Dimension(145, 20));
//        username.setMaximumSize(new Dimension(145, 20));
        builder.append(new JLabel("Username"), username);
        builder.append("The login username");
        builder.nextLine();

//        password.setPreferredSize(new Dimension(145, 20));
//        password.setMinimumSize(new Dimension(145, 20));
//        password.setMaximumSize(new Dimension(145, 20));
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
               resetGPConfigParameters(false);
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
                resetGPConfigParameters(true);
            }

            public void componentResized(ComponentEvent event){}
            public void componentMoved(ComponentEvent event){}
            public void componentHidden(ComponentEvent event){}
        });

        final MyCellRenderer myCellRenderer = new MyCellRenderer();
        serverSettingsTable = new JTable();
        serverSettingsTable.setDefaultRenderer(Object.class, myCellRenderer);

        rebuildTable();
        JScrollPane tableScrollPane = new JScrollPane();
        tableScrollPane.setViewportView(serverSettingsTable);

        editSettingsFrame = new JDialog();
        editSettingsFrame.setModal(true);
        editSettingsFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        editSettingsFrame.setAlwaysOnTop(true);
        JButton modifyButton = new JButton("Modify");
        modifyButton.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                try
                {
                    String title = "Edit GenePattern Server Settings";                    
                    showEditServerSettingsFrame(title);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
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

    /*
      hack to indicate password is missing when it is required
     */
    public void highlightPassword(boolean value)
    {
        highlightPassword = value;
    }

    public void showEditServerSettingsFrame(String title)
    {
        if(highlightPassword)
        {
            password.setBackground(new Color(255, 255, 204));
            password.setBorder(BorderFactory.createLineBorder(Color.black));
        }
        else
        {
            //reset password field bg color and border
            password.setBackground((new JPasswordField()).getBackground());
            password.setBorder((new JPasswordField()).getBorder());
        }
        editSettingsFrame.setTitle(title);
        builder.getPanel().setVisible(true);

        editSettingsFrame.getContentPane().add(builder.getPanel());
        editSettingsFrame.pack();

        // center on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        editSettingsFrame.setLocation((screenSize.width - editSettingsFrame.getWidth()) / 2,
        (screenSize.height - editSettingsFrame.getHeight()) / 2);

        editSettingsFrame.setVisible(true);

        editSettingsFrame.getContentPane().repaint();
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
			private static final long serialVersionUID = 2626845886563790888L;

			public boolean isCellEditable(int r, int c)
            {
                return false;
            }
        };

        model.setColumnIdentifiers(columnNames);

        Vector<Object> protocolRow = new Vector<Object>();
        protocolRow.add("Protocol");
        protocolRow.add(protocol.getValue());
        protocolRow.add("Whether to connect using http or https protocol");

        Vector<Object> hostRow = new Vector<Object>();
        hostRow.add("Host");
        hostRow.add(host.getValue());
        hostRow.add("The name or ip address of the server");

        Vector<Object> portRow = new Vector<Object>();
        portRow.add("Port");
        portRow.add(port.getValue());

        portRow.add("The port specified for the server");

        Vector<Object> userNameRow = new Vector<Object>();
        userNameRow.add("Username");
        userNameRow.add(username.getValue());
        userNameRow.add("The login username");

        Vector<String> passwordRow = new Vector<String>();
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
        serverSettingsTable.getColumnModel().getColumn(0).setMaxWidth(70);
        serverSettingsTable.setRowHeight(27);
        fitContentsInTable(serverSettingsTable);
    }

    private void fitContentsInTable(JTable table)
    {
        for(int c =0; c < table.getColumnCount(); c++)
        {
            TableColumn column = table.getColumnModel().getColumn(c);
			int w = column.getWidth();
			int n = table.getRowCount();
			for (int i = 0; i < n; i ++)
			{
				TableCellRenderer r = table.getCellRenderer(i, c);
				Component comp = r.getTableCellRendererComponent(
						table, table.getValueAt(i, c), false, false, i, c);
				w = Math.max(w, comp.getPreferredSize().width);
			}
            
            column.setPreferredWidth(w);
        }
    }

    public String passwordRequired(String serverName, String userName)
    {
        AdminProxy admin = null;
        try
        {
            admin = new AdminProxy(serverName, userName, null);

            String passwordRequired = (String)admin.getServiceInfo().get("require.password");
            passwordRequired = passwordRequired.toLowerCase();

            if(passwordRequired != null && passwordRequired.equals("false"))
            {
                return "false";
            }

            if(passwordRequired != null && passwordRequired.equals("true"))
            {
                return "true";
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private boolean testConfigSettings(String serverName, String userName, String password, boolean automatic)
    {
        AdminProxy admin = null;
        try
        {
            admin = new AdminProxy(serverName, userName, password);

            //hack to force to connect to server
            admin.getServiceInfo();
        }
        catch(Exception e)
        {
            // this is not executed when login is initiated automatically and not by user
            if(!automatic)
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
            }
            return false;
        }

        if(admin != null)
        {
            try
            {
                moduleInfo = admin.getTask("GSEA");
                fireStatusEvent(new ServerConnectionEvent(moduleInfo));
            }
            catch(Exception we)
            {
                // this is not executed when login is initiated automatically and not by user
                if(!automatic)
                {
                    we.printStackTrace();
                }
            }
        }
        return true;
    }

    private void resetGPConfigParameters(boolean automatic)
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

            if(gpServer.getPort() != -1)
            {
                port.setValue(String.valueOf(gpServer.getPort()));
            }

            
            if(passwordValue != null)
            {
                password.setText(passwordValue);
            }

            testConfigSettings(gpServer.toString(), (String)username.getValue(), passwordValue, automatic);
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

                String userNameInput = (String)username.getValue();
                passwordValue = String.valueOf(password.getPassword());

                URL gpServer = null;
                if(port.getValue() != null && ((String)port.getValue()).length() != 0)
                {
                    int portInput = (Integer.valueOf((String)port.getValue())).intValue();
                    gpServer = new URL(protocolInput, hostInput , portInput, "");
                }
                else
                    gpServer = new URL(protocolInput, hostInput , "");

                boolean success = testConfigSettings(gpServer.toString(), userNameInput, passwordValue, false);
                if(!success)
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
                resetGPConfigParameters(false);
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
            if(protocol.getText() == null || ((String)protocol.getValue()).length() == 0)
            {
            	editSettingsFrame.dispose();
                throw new InvalidInputException("Protocol must be provided");
            }
            else if(host.getText() == null || ((String)host.getValue()).length() == 0)
            {
            	editSettingsFrame.dispose();
                throw new InvalidInputException("Host must be provided");
            }
            else if(port.getValue() != null && ((String)port.getValue()).length() != 0 && (!((String)port.getValue()).matches("[0-9]+") ||  Integer.valueOf((String)port.getValue()).intValue() <= -1))
            {
            	editSettingsFrame.dispose();
                throw new InvalidInputException("Invalid port setting: " + port.getValue());
            }
            else if(username.getValue() == null || ((String)username.getValue()).length() == 0)
            {
            	editSettingsFrame.dispose();
                throw new InvalidInputException("Username must be provided");
            }
        }
        catch(NumberFormatException nf)
        {
            log.error(nf);
            editSettingsFrame.dispose();
            throw new InvalidInputException("Invalid value found for GenePattern server setting.");
        }
    }

    public String getPassword()
    {
        return new String(password.getPassword());
    }

    public static void addServerConnectionListener(ServerConnectionListener sl)
    {
        listener = sl;
    }

    public void fireStatusEvent(AWTEvent evt)
    {
        if (listener != null && evt instanceof ServerConnectionEvent)
        {
            listener.serverConnected((ServerConnectionEvent)evt);
        }
    }

    private class MyCellRenderer extends JLabel implements TableCellRenderer
    {
		private static final long serialVersionUID = 8598006263754034044L;

		public MyCellRenderer()
        {
           setFont(new Font("TestFont", Font.PLAIN, 13));
           setHorizontalTextPosition(JLabel.CENTER);
        }

        public Component getTableCellRendererComponent(JTable table, Object
	           value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            if(value != null)
            {
                setText("<html>" + value.toString() + "</html>");
            }
            else
                setText("");

            return this;
	    }
	}

    public TaskInfo getTaskInfo()
    {
        return moduleInfo;
    }
}
