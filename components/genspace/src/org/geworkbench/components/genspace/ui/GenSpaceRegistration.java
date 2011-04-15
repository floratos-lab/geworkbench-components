package org.geworkbench.components.genspace.ui;

import javax.swing.*;

import org.geworkbench.components.genspace.bean.*;
import org.geworkbench.engine.config.VisualPlugin;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import org.geworkbench.engine.properties.PropertiesManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdesktop.swingworker.*;

public class GenSpaceRegistration extends JPanel implements  VisualPlugin, ActionListener {
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

	//TODO: All validations. Field validations.
	//Invoke call to LoginMgr to pass the bean
	//Display message from the LoginMgr

	JButton save, reset, b_login;

	GenSpaceLogin login;

	public GenSpaceRegistration()
	{	
		// read the preferences from the properties file
		try 
		{
			// ideally this should also be in the properties file
			String title = "Please enter your registration information below.";
		} 
		catch (Exception e) { }		

		initComponents();
	}

	/**
	 * This method fulfills the contract of the {@link VisualPlugin} interface.
	 * It returns the GUI component for this visual plugin.
	 */
	public Component getComponent() {
		// In this case, this object is also the GUI component.
		return this;
	}

	private void initComponents() {	
		this.setSize(500, 500);
		this.setLayout(new  GridLayout( 15, 2));
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
		labaff= new JTextField("", 20);
		add(j4);
		add(labaff);

		JLabel emailLabel = new JLabel("Email Address");
		email= new JTextField("", 20);
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

		JPanel saveReset;

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

	private RegisterBean getBean() {
		RegisterBean bean = new RegisterBean();

		bean.setMessage("Register");	//This is set to Login for logging in. Appropriately set in Jpanel for registration/login

		bean.setUName(userId.getText());

		char[] pass = password.getPassword();
		bean.setPassword(pass);

		bean.setFName(fname.getText());
		bean.setLName(lname.getText());

		bean.setLabAffiliation(labaff.getText());

		bean.setEmail(email.getText());

		bean.setPhoneNumber(phone.getText());

		bean.setAddr1(addr1.getText());
		bean.setAddr2(addr2.getText());
		bean.setCity(city.getText());
		bean.setState(state.getText());
		bean.setZipcode(zipcode.getText());

		return bean;
	}

	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == save) {

			org.jdesktop.swingworker.SwingWorker<Void, Void> worker = new org.jdesktop.swingworker.SwingWorker<Void, Void>() {
				public Void doInBackground() {

					save.setEnabled(false);

					try 
					{
						StringBuffer errMsg = new StringBuffer();
						if(isValid(errMsg))
						{
							LoginManager manager = new LoginManager(getBean());

							boolean userDupCheck = manager.userDupCheck();

							if (userDupCheck) {
								boolean userRegister = manager.userRegister();

								if (userRegister) {
									String msg="User Registered with default preferences";

									JOptionPane.showMessageDialog(null, msg);

									callLogin();
								} else {
									String msg="User Registration failed. Cannot connect to server.";

									JOptionPane.showMessageDialog(null, msg);
								}
							} else {
								String msg="User ID is duplicated";

								JOptionPane.showMessageDialog(null, msg);

								userId.setText("");
							}
						}
						else {
							JOptionPane.showMessageDialog(null, errMsg.toString(),
									"Error Information", JOptionPane.INFORMATION_MESSAGE);

							getThisPanel().revalidate();
						}
					}
					catch (Exception ex) { }
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

	private boolean empty(String str)
	{
		if("".equalsIgnoreCase(str) || null == str)
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
		if(empty(id))
		{
			msg.append("UserId cannot be empty\n");
			valid = false;
		}
		if(empty(pw))
		{
			msg.append("Password cannot be empty\n");
			valid = false;
		}
		if(empty(confirm))
		{
			msg.append("Confirm password field cannot be empty\n");
			valid = false;
		}
		if(empty(labaffStr))
		{
			msg.append("Lab affiliation cannot be empty\n");
			valid = false;
		}
		if(!empty(pw) && !empty(confirm))
		{
			if(!pw.equals(confirm))
			{
				msg.append("Password confirmation does not match password\n");
				valid = false;

			}
		}

		Pattern pattern;
		Matcher matcher;

		// user name special character validation
		if(!empty(id))
		{
			pattern = Pattern.compile("[^0-9a-zA-Z()-_]");

			matcher = pattern.matcher(id);

			if(matcher.find()) 
			{
				msg.append("Invalid user name.\n");
				valid = false;
			}
		}

		// Phone number validation
		if(!empty(pho))
		{
			pattern = Pattern.compile("[^0-9a-zA-Z()-]");

			matcher = 
				pattern.matcher(pho);

			if(matcher.find()) 
			{
				msg.append("Phone number contains invalid characters\n");
				valid = false;
			}
		}

		// email validation
		if(!empty(em))
		{
			pattern = Pattern.compile("[0-9a-zA-Z()-_.]+@[0-9a-zA-Z()-_.]+");

			matcher = pattern.matcher(em);

			if(!matcher.find()) 
			{
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

	public void initFrame()
	{
		jframe = new JFrame();
		jframe.add(this);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//jframe.setSize(400,400);
	}    

	public void showFrame()
	{
		jframe.setVisible(true); 	
	} 

	public void hideFrame()
	{
		jframe.setVisible(false); 	
	}     

	public static void main(String args[]) throws Exception
	{
		GenSpaceRegistration panel = new  GenSpaceRegistration();
		panel.initFrame();
		panel.showFrame();
	}
}
