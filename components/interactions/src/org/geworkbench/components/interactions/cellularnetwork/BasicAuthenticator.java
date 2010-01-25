package org.geworkbench.components.interactions.cellularnetwork;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener; 
import java.net.Authenticator; 
import java.net.PasswordAuthentication; 
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

 

/**
 * Extends Authenticator to allow for user login *
 * 
 */
public class BasicAuthenticator extends Authenticator {
	private LoginDialog dialog;

	/** Creates a new instance of BasicAuthenticator */
	public BasicAuthenticator() {
	}

	protected java.net.PasswordAuthentication getPasswordAuthentication() {
		PasswordAuthentication authentication = createLoginDialog();
		return authentication;
	}

	private PasswordAuthentication createLoginDialog() {
		if (dialog == null)
			dialog = new LoginDialog();

		dialog.setVisible(true);
		PasswordAuthentication p = dialog.getAuth();
		return p;
	}
}


class LoginDialog extends JDialog implements KeyListener, ActionListener {

	private static final long serialVersionUID = -851685398982531107L;
	private JPanel north, south;
	private JLabel nameLabel, passwordLabel;
	private JTextField nameTextField;
	private JPasswordField passwordField;
	private JButton loginButton, cancelButton;
	private String username;
	private char password[];
	private PasswordAuthentication privateAuth;

	public LoginDialog() {
		setModal(true);
		setTitle("Login Required");
		north = new JPanel();
		north.setLayout(null);
		north.setPreferredSize(new Dimension(277, 110));
		nameLabel = new JLabel("Username:", JLabel.LEFT);
		nameLabel.setBounds(10, 40, 90, 20);
		north.add(nameLabel);
		passwordLabel = new JLabel("Password:", JLabel.LEFT);
		passwordLabel.setBounds(10, 70, 90, 20);
		north.add(passwordLabel);
		nameTextField = new JTextField();
		nameTextField.setBounds(100, 40, 157, 20);
		north.add(nameTextField);
		passwordField = new JPasswordField();
		passwordField.setBounds(100, 70, 157, 20);
		north.add(passwordField);

		getContentPane().add(north, BorderLayout.CENTER);
		south = new JPanel();
		south.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		loginButton = new JButton("Login");
		loginButton.addActionListener(this);
		south.add(loginButton);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				privateAuth = null;
				setVisible(false);
			}
		});

		south.add(cancelButton);
		getContentPane().add(south, BorderLayout.SOUTH);
		loginButton.addKeyListener(this);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		pack();
		// parent frame may not be up so use center of screen
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int) d.getWidth() / 2 - getWidth() / 2, (int) d
				.getHeight()
				/ 2 - getHeight() / 2);
	}

	public void actionPerformed(ActionEvent ae) {

		username = nameTextField.getText();
		nameTextField.setText("");
		password = passwordField.getPassword();
		passwordField.setText("");
		privateAuth = new PasswordAuthentication(username, password);
		// Delete attributes for userID and password
		username = "";
		password = new String("").toCharArray();
		nameTextField.requestFocus();
		setVisible(false);
	}

	/**
	 * Returns the actual PasswordAuthentication, which was generated from the
	 * login dialog.
	 * 
	 * @return PasswordAuthentication - actual PasswordAuthentication.
	 */
	public PasswordAuthentication getAuth() {
		return privateAuth;
	}

	public void keyPressed(java.awt.event.KeyEvent keyEvent) {
	}

	public void keyReleased(java.awt.event.KeyEvent keyEvent) {
	}

	public void keyTyped(java.awt.event.KeyEvent keyEvent) {
		if (keyEvent.getKeyCode() == KeyEvent.VK_UNDEFINED
				|| keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
			actionPerformed(null);
	}
}

