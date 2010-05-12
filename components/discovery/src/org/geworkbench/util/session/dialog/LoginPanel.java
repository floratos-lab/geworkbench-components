package org.geworkbench.util.session.dialog;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.geworkbench.events.LoginPanelModelEvent;
import org.geworkbench.util.session.LoginPanelModel;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version $Id$
 */
public class LoginPanel extends JPanel implements Serializable, org.geworkbench.events.LoginPanelModelListener {
	private static final long serialVersionUID = -7704117180666397695L;
	
	private JPasswordField password = new JPasswordField();
	private JTextField userName = new JTextField();
	private JTextField portName = new JTextField();
	private JLabel jLabel2 = new JLabel();
	private JComboBox hostName = new JComboBox();
	private JLabel serverLabel = new JLabel();
	private JLabel jLabel3 = new JLabel();
	private JLabel passwordLabel = new JLabel();
	private Border border1;
	private LoginPanelModel model;

    public LoginPanel(LoginPanelModel model) {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (model == null) {
            throw new IllegalArgumentException("Cannot set a null LoginPanelModel");
        }
        this.model = model;
        model.addLoginPanelModelListener(this);
        model.fireLoginPanelModelChanged();
    }

    /**
     * This method returns the host name.
     *
     * @return host name
     */
    public Object getHostName() {
        return model.getHostName();
    }

    @Override
    public void loginPanelChanged(LoginPanelModelEvent evt) {
        LoginPanelModel lpm = (LoginPanelModel) evt.getSource();
        userName.setText(lpm.getUserName());
        portName.setText(lpm.getPort());

        // show Host Set
        String name = lpm.getHostName();
        if (name != null) {
            hostName.addItem(name);
        }
        Set<?> hostSet = lpm.getHostSet();
        if (hostSet != null) {
            for (Object h: hostSet) {
                hostName.addItem(h);
            }
        }
    }

    /**
     * The method writes the information of the panel to the model.
     */
    public void write() {
        LoginPanelModel m = model;
        m.setCurrentHostName((String) hostName.getSelectedItem());
        m.setPort(portName.getText());
        m.setUserName(userName.getText());
        m.setPassword(password.getPassword());
    }

    /**
     * This method returns the user name.
     *
     * @return user name
     */
    public String getUserName() {
        return model.getUserName();
    }

    /**
     * This method returns the port number.
     *
     * @return port number
     */
    public String getPortNum() {
        return model.getPort();
    }

    /**
     * This method returns the password.
     *
     * @return password
     */
    public char[] getPassword() {
        return model.getPassword();
    }

    private void jbInit() throws Exception {
        border1 = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        passwordLabel.setText("Password:");
        jLabel3.setText("User Name:");
        serverLabel.setText("Server:");
        hostName.setEditable(true);
        jLabel2.setText("Port:");
        portName.setText("");
        userName.setScrollOffset(0);
        password.setToolTipText("");
        password.setText("");
        
        this.setLayout(new GridBagLayout());
        this.setBackground(new Color(204, 204, 204));
        this.setBorder(border1);

        this.add(password, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        this.add(userName, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        this.add(portName, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        this.add(jLabel2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
        this.add(hostName, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        this.add(serverLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
        this.add(jLabel3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
        this.add(passwordLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
    }
}



