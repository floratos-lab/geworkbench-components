package org.geworkbench.components.genspace.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.server.wrapper.UserWrapper;
import org.geworkbench.engine.config.VisualPlugin;

public class GenSpaceRegistration extends JPanel implements VisualPlugin,
		ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6627550806528655509L;

	private JFrame jframe;

	private JTextField userId;
	private JPasswordField password;
	private JPasswordField passwordDup;
	private JTextField fname;
	private JTextField lname;
	private JTextField labaff;
	private JTextField email;
	private JTextField phone;
	private JTextField addr1;
	private JTextField addr2;
	private JTextField city;
	private JTextField state;
	private JTextField zipcode;

	// TODO: All validations. Field validations.
	// Invoke call to LoginMgr to pass the bean
	// Display message from the LoginMgr

	JButton save, reset, b_login;

	GenSpaceLogin login;

	public GenSpaceRegistration() {
		initComponents();
	}

	/**
	 * This method fulfills the contract of the {@link VisualPlugin} interface.
	 * It returns the GUI component for this visual plugin.
	 */
	@Override
	public Component getComponent() {
		// In this case, this object is also the GUI component.
		return this;
	}

	private void initComponents() {
		this.setSize(500, 500);
		this.setLayout(new GridLayout(15, 2));
		JLabel j1 = new JLabel("Enter sign in user id *");
		userId = new JTextField("", 20);
		add(j1);
		add(userId);

		JLabel jp = new JLabel("Select your password *");
		password = new JPasswordField(10);
		password.addActionListener(this);

		add(jp);
		add(password);

		JLabel jpc = new JLabel("Confirm your password *");
		passwordDup = new JPasswordField(10);
		add(jpc);
		add(passwordDup);

		JLabel j2 = new JLabel("First Name");
		fname = new JTextField("", 20);
		add(j2);
		add(fname);

		JLabel j3 = new JLabel("Last Name");
		lname = new JTextField("", 20);
		add(j3);
		add(lname);

		JLabel j4 = new JLabel("Lab Affiliation *");
		labaff = new JTextField("", 20);
		add(j4);
		add(labaff);

		JLabel emailLabel = new JLabel("Email Address");
		email = new JTextField("", 20);
		add(emailLabel);
		add(email);

		JLabel phoneLabel = new JLabel("Phone");
		phone = new JTextField("", 20);
		add(phoneLabel);
		add(phone);

		JLabel j5 = new JLabel("Address 1");
		addr1 = new JTextField("", 20);
		add(j5);
		add(addr1);

		JLabel j6 = new JLabel("Address 2");
		addr2 = new JTextField("", 20);
		add(j6);
		add(addr2);

		JLabel j7 = new JLabel("City");
		city = new JTextField("", 20);
		add(j7);
		add(city);

		JLabel j9 = new JLabel("State");
		state = new JTextField("", 20);
		add(j9);
		add(state);

		JLabel j8 = new JLabel("ZIP Code");
		zipcode = new JTextField("", 20);
		add(j8);
		add(zipcode);


		save = new JButton("Save");
		save.addActionListener(this);
		reset = new JButton("Reset");
		reset.addActionListener(this);
		b_login = new JButton("Login");
		b_login.addActionListener(this);

		add(save);
		add(reset);
		add(b_login);

		save.setEnabled(true);
	}

	private User getNewUser() {
		UserWrapper u = new UserWrapper(new User());

		u.setUsername(userId.getText());

		char[] pass = password.getPassword();
		u.setPasswordClearText(new String(pass));

		u.setFirstName(fname.getText());
		u.setLastName(lname.getText());

		u.setLabAffiliation(labaff.getText());

		u.setEmail(email.getText());

		u.setPhone(phone.getText());

		u.setAddr1(addr1.getText());
		u.setAddr2(addr2.getText());
		u.setCity(city.getText());
		u.setState(state.getText());
		u.setZipcode(zipcode.getText());

		return u.getDelegate();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == save) {

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				@Override
				public Void doInBackground() {

					save.setEnabled(false);

					try {
						StringBuffer errMsg = new StringBuffer();
						if (isValid(errMsg)) {
						
							if(GenSpaceServerFactory.userRegister(getNewUser()))
							{
								String msg = "User Registered";

								JOptionPane.showMessageDialog(null, msg);

								callLogin();
							}
							else
							{
								if(errMsg.toString().equals(""))
								{
									errMsg.append("Error: This username is already in use");
								}
								JOptionPane.showMessageDialog(null,
										errMsg.toString(), "Error Information",
										JOptionPane.INFORMATION_MESSAGE);

								getThisPanel().revalidate();
							}
							
						} else {
							
						}
					} catch (Exception ex) {
					}
					save.setEnabled(true);

					return null;
				}
			};
			worker.execute();

		} else if (e.getSource() == reset) {

		} else if (e.getSource() == b_login) {
			callLogin();
		}
	}

	public JPanel getThisPanel() {
		return this;
	}

	private boolean empty(String str) {
		if ("".equalsIgnoreCase(str) || null == str)
			return true;
		else
			return false;
	}

	public boolean isValid(StringBuffer msg) {
		String id = userId.getText();

		char input[] = password.getPassword();
		String pw = new String(input);
		String confirm = new String(passwordDup.getPassword());

		String labaffStr = labaff.getText();

		String pho = phone.getText();
		String em = email.getText();

		boolean valid = true;
		if (empty(id)) {
			msg.append("UserId cannot be empty\n");
			valid = false;
		}
		if (empty(pw)) {
			msg.append("Password cannot be empty\n");
			valid = false;
		}
		if (empty(confirm)) {
			msg.append("Confirm password field cannot be empty\n");
			valid = false;
		}
		if (empty(labaffStr)) {
			msg.append("Lab affiliation cannot be empty\n");
			valid = false;
		}
		if (!empty(pw) && !empty(confirm)) {
			if (!pw.equals(confirm)) {
				msg.append("Password confirmation does not match password\n");
				valid = false;

			}
		}

		Pattern pattern;
		Matcher matcher;

		// user name special character validation
		if (!empty(id)) {
			pattern = Pattern.compile("[^0-9a-zA-Z()-_]");

			matcher = pattern.matcher(id);

			if (matcher.find()) {
				msg.append("Invalid user name.\n");
				valid = false;
			}
		}

		// Phone number validation
		if (!empty(pho)) {
			pattern = Pattern.compile("[^0-9a-zA-Z()-]");

			matcher = pattern.matcher(pho);

			if (matcher.find()) {
				msg.append("Phone number contains invalid characters\n");
				valid = false;
			}
		}

		// email validation
		if (!empty(em)) {
			pattern = Pattern.compile("[0-9a-zA-Z()-_.]+@[0-9a-zA-Z()-_.]+");

			matcher = pattern.matcher(em);

			if (!matcher.find()) {
				msg.append("Invalid Email.\n");
				valid = false;
			}
		}
		return valid;
	}

	private void callLogin() {
		try {
			login = new GenSpaceLogin();
			this.setLayout(new FlowLayout());
			this.removeAll();
			this.add(login);
			this.revalidate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initFrame() {
		jframe = new JFrame();
		jframe.add(this);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// jframe.setSize(400,400);
	}

	public void showFrame() {
		jframe.setVisible(true);
	}

	public void hideFrame() {
		jframe.setVisible(false);
	}

	public static void main(String args[]) throws Exception {
		GenSpaceRegistration panel = new GenSpaceRegistration();
		panel.initFrame();
		panel.showFrame();
	}
}
