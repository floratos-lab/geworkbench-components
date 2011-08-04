package org.geworkbench.components.discovery.session;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;


/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: Class CreateSessionDialog takes user's input for creating
 * a new session.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author Aner
 * @version $Id$
 */
public class CreateSessionDialog extends JDialog {
	private static final long serialVersionUID = -6879419192925394259L;
	
	private BorderLayout borderLayout2 = new BorderLayout();
	private Border border1;
	private JPanel jPanel1 = new JPanel();
	private JButton cancelButton = new JButton();
	private JButton connectButton = new JButton();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private Border border6;
	private GridBagLayout gridBagLayout3 = new GridBagLayout();
	private Border border7;

    //value to be returned
    private static int CANCEL_OPTION = 0;
    public static int CONNECT_OPTION = 1;
    private int returnValue = CANCEL_OPTION;
    private static int sessionNo = 1;
    private JPanel jPanel5 = new JPanel();
    private BorderLayout borderLayout1 = new BorderLayout();
    private JPanel loginPanel;
    private JPanel jPanel4 = new JPanel();
    private JTextField sessionName = new JTextField(10);
    private JLabel sessionL = new JLabel();
    private TitledBorder titledBorder3;
    private Border border9;
    private FlowLayout flowLayout1 = new FlowLayout();
    
    // these used to be in LoginPanel
    private JPasswordField password = new JPasswordField();
    private JTextField userName = new JTextField();
	private JTextField portName = new JTextField();
	private JComboBox hostName = new JComboBox();
	
	private LoginPanelModel model;
	
    private void initializeLoginPanel(LoginPanelModel model) {
    	loginPanel = new JPanel();

        hostName.setEditable(true);
        portName.setText("");
        userName.setScrollOffset(0);

        password.setText("");
        
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setBackground(new Color(204, 204, 204));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        loginPanel.add(password, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        loginPanel.add(userName, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        loginPanel.add(portName, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        loginPanel.add(new JLabel("Port:"), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
        loginPanel.add(hostName, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        loginPanel.add(new JLabel("Server:"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
        loginPanel.add(new JLabel("User Name:"), new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
        loginPanel.add(new JLabel("Password:"), new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));

        if (model == null) {
            throw new IllegalArgumentException("Cannot set a null LoginPanelModel");
        }
        this.model = model;
        loginPanelChanged();
}

    public CreateSessionDialog(Frame frame, String title, LoginPanelModel model, boolean modal) {
        super(frame, title, modal);

        initializeLoginPanel(model);
        
        try {
            jbInit();
            pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        initDialog();
    }

    private void jbInit() throws Exception {
        border1 = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), BorderFactory.createEmptyBorder(5, 5, 5, 5));
        border6 = BorderFactory.createCompoundBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(165, 163, 151)), "DiscoverySession"), BorderFactory.createEmptyBorder(2, 1, 1, 1));
        border7 = new TitledBorder(BorderFactory.createEmptyBorder(), "");
        titledBorder3 = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(142, 142, 142)), "DiscoverySession's server");
        border9 = BorderFactory.createCompoundBorder(titledBorder3, BorderFactory.createEmptyBorder(1, 1, 1, 1));
        this.getContentPane().setLayout(borderLayout2);
        jPanel1.setLayout(gridBagLayout3);
        jPanel1.setBorder(border1);
        jPanel1.setMinimumSize(new Dimension(500, 250));
        jPanel1.setPreferredSize(new Dimension(500, 250));
        jPanel1.setRequestFocusEnabled(true);
        this.setResizable(false);
        CreateSessionDialog_this_keyAdapter keyAdapter = new CreateSessionDialog_this_keyAdapter();
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new CreateSessionDialog_cancelButton_actionAdapter());
        cancelButton.addKeyListener(keyAdapter);
        cancelButton.setActionCommand("cancelAction");
        connectButton.setMaximumSize(new Dimension(73, 25));
        connectButton.setActionCommand("connectAction");
        connectButton.setText("Create");
        connectButton.addActionListener(new CreateSessionDialog_connectButton_actionAdapter());
        connectButton.addKeyListener(keyAdapter);
        jPanel2.setLayout(flowLayout1);
        jPanel2.setBackground(UIManager.getColor("Menu.background"));
        jPanel2.setBorder(border7);
        jPanel3.setMinimumSize(new Dimension(600, 230));
        jPanel3.setPreferredSize(new Dimension(600, 230));
        jPanel5.setLayout(borderLayout1);
        jPanel4.setEnabled(true);
        jPanel4.setFont(new java.awt.Font("MS Sans Serif", 0, 11));
        jPanel4.setBorder(border6);
        jPanel4.setDebugGraphicsOptions(0);
        jPanel4.setDoubleBuffered(true);
        jPanel4.setLayout(new BoxLayout(jPanel4, BoxLayout.LINE_AXIS));

        sessionName.setText("");
        sessionName.addKeyListener(keyAdapter);
        sessionL.setText("DiscoverySession Name:");
        jPanel5.setBorder(border9);
        jPanel4.add(sessionL);
        jPanel4.add(sessionName);
        jPanel3.add(jPanel4, null);
        jPanel3.add(jPanel5, null);
        jPanel5.add(loginPanel, BorderLayout.CENTER);
        jPanel1.add(jPanel2, new GridBagConstraints(2, 1, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        jPanel2.add(connectButton, null);
        jPanel2.add(cancelButton, null);
        jPanel1.add(jPanel3, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 2, 0), 0, 0));
        this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    }

    /**
     * Initialize general Dialog behaviors.
     */
    private void initDialog() {
        //center the dialog by default
        super.setLocationRelativeTo(null);
        //Handle window closing correctly.
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        //Ensure the session name field gets the first focus.
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                sessionName.requestFocusInWindow();
                sessionName.setText("session" + sessionNo);
                // FIXME this is not correct: low level component event triggers increment of session count
                ++sessionNo;
                sessionName.selectAll();
            }
        });
    }

    /**
     * Get the return value of this dialog.
     * This method should be called after the show method was called.
     */
    public int getReturnValue() {
        return returnValue;
    }

    /**
     * This method verifies the input fields.
     */
    private boolean verify() {
        //write the data in the loginPanel to the model
        model.setCurrentHostName((String) hostName.getSelectedItem());
        model.setPort(portName.getText());
        model.setUserName(userName.getText());
        model.setPassword(password.getPassword());
        if (!verifySession()) {
            return false;
        }
        if (!verifyhost()) {
            return false;
        }
        if (!verifyPort()) {
            return false;
        }
        if (!verifyUserName()) {
            return false;
        }
        return true;
    }

    private boolean verifyUserName() {
        if (model.getUserName().trim().equals("")) {
            popMessage("Please enter user name.");
            return false;
        }
        return true;
    }

    private boolean verifySession() {
        if (sessionName.getText().trim().equals("")) {
            popMessage("Please enter session name.");
            return false;
        }
        return true;
    }

    private boolean verifyPort() {
        try {
            Integer.parseInt(model.getPort());
        } catch (NumberFormatException exp) {
            popMessage("Please enter a number for the port.");
            return false;
        }
        return true;
    }

    private boolean verifyhost() {
        String selected = (String) model.getHostName();
        selected = selected.trim();
        if (selected.equals("")) {
            popMessage("Please enter host name.");
            return false;
        }
        return true;
    }

    private void popMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    /**
     * This method returns the host name.
     *
     * @return host name
     */
    public String getHostName() {
        return (String) (model.getHostName());
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
    public int getPortNum() {
        return Integer.parseInt(model.getPort());
    }

    /**
     * This method returns the password.
     *
     * @return password
     */
    public char[] getPassWord() {
        return model.getPassword();
    }

    public String getSessionName() {
        return sessionName.getText();
    }

    private void cancelButton_actionPerformed(ActionEvent e) {
        returnValue = CANCEL_OPTION;
        --sessionNo;
        setVisible(false);
    }

    private void connectButton_actionPerformed(ActionEvent e) {
        if (verify()) {
            returnValue = CONNECT_OPTION;
            setVisible(false);
        }
        return;
    }

    private class CreateSessionDialog_cancelButton_actionAdapter implements java.awt.event.ActionListener {

        public void actionPerformed(ActionEvent e) {
            cancelButton_actionPerformed(e);
        }
    }

    private class CreateSessionDialog_connectButton_actionAdapter implements java.awt.event.ActionListener {
        
    	public void actionPerformed(ActionEvent e) {
            connectButton_actionPerformed(e);
        }
    }

    private class CreateSessionDialog_this_keyAdapter extends java.awt.event.KeyAdapter {

        public void keyReleased(KeyEvent e) {
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_ENTER && e.getSource() != cancelButton) {
                connectButton_actionPerformed(null);
            } else if (keyCode == KeyEvent.VK_ENTER && e.getSource() == cancelButton) {
                cancelButton_actionPerformed(null);
            }
        }
    }

	private void loginPanelChanged() {
        userName.setText(model.getUserName());
        portName.setText(model.getPort());

        // show Host Set
        String name = model.getHostName();
        if (name != null) {
            hostName.addItem(name);
        }
        Set<?> hostSet = model.getHostSet();
        if (hostSet != null) {
            for (Object h: hostSet) {
                hostName.addItem(h);
            }
        }
	}

}
