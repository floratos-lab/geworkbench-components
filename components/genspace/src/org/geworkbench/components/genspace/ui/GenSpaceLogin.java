//package genspace.ui;
package org.geworkbench.components.genspace.ui;

import org.geworkbench.components.genspace.bean.RegisterBean;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.util.FilePathnameUtils;

import javax.swing.*;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdesktop.swingworker.*;

/**
 * This is an example geWorkbench component.
 *
 *
 */
//This annotation lists the data set types that this component accepts.
//The component will only appear when a data set of the appropriate type is selected.
//@AcceptTypes({DSMicroarraySet.class})
public class GenSpaceLogin extends JPanel implements VisualPlugin, ActionListener, Runnable {

	private JLabel l1, l2, l3;
	private JTextField tf;
	private JPasswordField pf;
	private JButton b1, b2, b3;
	private String filename = "genspace.txt";
	private String hash = "MD5";
	private JLabel msgText, msgText1, msgText2, msgText3, msgText4, msgText5, msgText6;
	protected final static String HEX_DIGITS = "0123456789abcdef";

	// to indicate whether the user is logged in or not
	public static boolean isLoggedIn = false;

	// the user's login ID
	public static String genspaceLogin = null;

	// a list of all ActionListeners waiting for login events
	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

	// the surrounding JFrame when this is used as a standalone window
	private JFrame frame;

	private boolean userLogin;

	GenSpaceRegistration panel;

	/**
	 * Constructor
	 */
	public GenSpaceLogin() {
		run();
	}

	public void run() {
		this.setSize(500, 500);

		JPanel onePanel = new JPanel();
		onePanel.setSize(150,150);


		onePanel.setLayout(new GridLayout(2,1));

		JPanel lPanel = new JPanel();

		l1 = new JLabel("Login");
		l2 = new JLabel("Password");
		l3 = new JLabel("");
		tf = new JTextField(10);
		pf = new JPasswordField(10);
		b1 = new JButton("Login");
		b2 = new JButton("Clear");
		b3 = new JButton("Register");
		l3.setVisible(true);
		lPanel.add(l1);
		lPanel.add(tf);
		lPanel.add(l2);
		lPanel.add(pf);
		lPanel.add(b1);
		lPanel.add(b2);
		lPanel.add(b3);
		lPanel.add(l3);
		b1.addActionListener(this);
		b2.addActionListener(this);
		b3.addActionListener(this);

		onePanel.add(lPanel);


		JPanel msgPanel = new JPanel();
		msgPanel.setLayout(new  GridLayout( 7, 1));

		msgText = new JLabel("Not a registered user yet? ");
		msgText1 = new JLabel ("Register to take advantage of genSpace security features.");
		msgText2 = new JLabel("As a registered user you will be able to:");
		msgText3 = new JLabel("1. Set your data visbility preferences.");
		msgText4 = new JLabel("2. Post comments and rate workflows and tools.");
		msgText5 = new JLabel("You can also choose to continue using genSpace without a login in which case") ;
		msgText6 = new JLabel("default security preferences will be applied.");

		msgPanel.add (msgText);
		msgPanel.add (msgText1);
		msgPanel.add (msgText2);
		msgPanel.add (msgText3);
		msgPanel.add (msgText4);
		msgPanel.add (msgText5);
		msgPanel.add (msgText6);
		onePanel.add (msgPanel);
		add(onePanel);

	}

	/**
	 * Action Listener
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == b1) {

			org.jdesktop.swingworker.SwingWorker<Void, Void> worker = new org.jdesktop.swingworker.SwingWorker<Void, Void>() {
				public Void doInBackground() {

					b1.setEnabled(false);

					try
					{
						StringBuffer errMsg = new StringBuffer();


						if(isValid(errMsg))

						{



							LoginManager manager = new LoginManager(getBean());
							userLogin = manager.userLogin();

							if (userLogin) {
								String msg="User Logged in.";
								JOptionPane.showMessageDialog(getComponent(), msg);
								GenSpaceSecurityPanel p = new GenSpaceSecurityPanel(getBean().getUsername());
								getThisPanel().removeAll();
								getThisPanel().add(p);
								//this.setSize(500, 500);
								getThisPanel().revalidate();

							} else {
								String msg="User Log in failed.";

								JOptionPane.showMessageDialog(getComponent(), msg);
							}
						}
						else {
							JOptionPane.showMessageDialog(getComponent(), errMsg.toString(),
									"Error Information", JOptionPane.INFORMATION_MESSAGE);

							getThisPanel().revalidate();
						}
					}
					catch (Exception ex) { }
					b1.setEnabled(true);

					return null;
				}
			};
			worker.execute();

		}
		else if (e.getSource() == b2) {
			tf.setText("");
			pf.setText("");
			l3.setVisible(false);
		}
		else if (e.getSource() == b3) {
			callRegisterMember();

			if (create() == 0) {
				l3.setText("Login Created");
				isLoggedIn = false;
			}
			else {
				l3.setText("Login Creation Failed");
				isLoggedIn = false;
			}
			l3.setVisible(true);
		}

	}

	private boolean empty(String str)
	{
		if("".equalsIgnoreCase(str) || null == str)
			return true;
		else
			return false;
	}

	public boolean isValid(StringBuffer msg) {

		String id = tf.getText();
		char input[] = pf.getPassword();
		String pw = new String(input);

		boolean valid = true;

		if(empty(id))
		{
			msg.append("UserId cannot be empty\n");
			valid = false;
		}
		if(empty(pw))
		{
			msg.append("Pasword cannot be empty\n");
			valid = false;
		}

		Pattern pattern;
		Matcher matcher;
		// user name special character validation
		if(!empty(id))
		{
			pattern =
				Pattern.compile("[^0-9a-zA-Z()-_]");

			matcher =
				pattern.matcher(id);
			if(matcher.find())
			{
				msg.append("Invalid user name.\n");
				valid = false;
			}
		}


		//System.out.println("valid : " + valid);
		return valid;
	}

	private static boolean isPasswordCorrect(char[] input) {
		boolean isCorrect = true;
		char[] correctPassword = { 'b', 'u', 'g', 'a', 'b', 'o', 'o' };
		String test = new String(input);
		//System.out.println("co : " + test);

		if (input.length != correctPassword.length) {
			isCorrect = false;
		} else {
			isCorrect = Arrays.equals (input, correctPassword);
		}

		//Zero out the password.
		Arrays.fill(correctPassword,'0');

		return isCorrect;
	}

	private static String getEncryptPassword(String plaintext) {
		java.security.MessageDigest d =null;
		try {
			d = java.security.MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		d.reset();
		d.update(plaintext.getBytes());
		byte[] hashedBytes =  d.digest();
		StringBuffer sb = new StringBuffer(hashedBytes.length * 2);
		for (int i = 0; i < hashedBytes.length; i++) {
			int b = hashedBytes[i] & 0xFF;
			sb.append(HEX_DIGITS.charAt(b >>> 4)).append(HEX_DIGITS.charAt(b & 0xF));
		}
		return sb.toString();
	}

	private RegisterBean getBean()
	{
		RegisterBean bean = new RegisterBean();
		bean.setMessage("Login");	//This is set to Login for logging in. Appropriately set in Jpanel for registration/login
		bean.setUName(tf.getText());
		char[] pass = pf.getPassword();
		String test = "TEST";
		//System.out.println("Pass 1: "+ pass);
		//System.out.println("Pass 2: "+ getEncryptPassword(test));
		//System.out.println("Pass 3: "+ getEncryptPassword(test));

		bean.setPassword(pass);
		//System.out.println("co : " + isPasswordCorrect(pass));
		bean.setFName("");
		bean.setLName("");
		bean.setLabAffiliation("");
		bean.setEmail("");
		bean.setPhoneNumber("");
		bean.setAddr1("");
		bean.setAddr2("");
		bean.setCity("");
		bean.setState("");
		bean.setZipcode("");
		return bean;
	}

	/**
	 * Checks if a login is valid
	 * @return 0 is valid, -1 otherwise
	 */
	private int check()
	{
		return check(tf.getText(), new String(pf.getPassword()));
	}

	/**
	 * Checks if a login is valid
	 * @return 0 is valid, -1 otherwise
	 */
	public int check(String login, String pass) {
		try{
			String encrypted_pass = getEncodedString(pass);
			BufferedReader br = new BufferedReader(new FileReader(FilePathnameUtils.getUserSettingDirectoryPath() + filename));
			String file_login = br.readLine();
			String file_encrypted_pass = br.readLine();
			br.close();
			if (login.equals(file_login) && encrypted_pass.equals(file_encrypted_pass)) {
				genspaceLogin = login;
				return 0;
			}
			else {
				return -1;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Converts a byte stream to a String
	 * @param b The byte stream
	 * @return The converted String
	 */
	public String bytesToHex(byte[] b) {
		char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7',
				'8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
		StringBuffer buf = new StringBuffer();
		for (int j=0; j<b.length; j++) {
			buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
			buf.append(hexDigit[b[j] & 0x0f]);
		}
		return buf.toString();
	}

	/**
	 * Creates a Hash
	 * @param clearText The Clear-Text String
	 * @return The Hashed String
	 */
	private String getEncodedString(String clearText) {
		try {
			MessageDigest md = MessageDigest.getInstance(hash);
			md.update(clearText.getBytes());
			byte[] output = md.digest();
			return bytesToHex(output);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Creates a Login
	 * @return 0 If Successful, -1 otherwise
	 */
	private int create()
	{
		return create(tf.getText(), new String(pf.getPassword()));
	}

	/**
	 * Creates a Login
	 * @return 0 If Successful, -1 otherwise
	 */
	public int create(String login, String pass) {
		try{
			String encrypted_pass = getEncodedString(pass);
    		FileWriter fw = new FileWriter( FilePathnameUtils.getUserSettingDirectoryPath() + filename );
			fw.write(login);
			fw.write("\n");
			fw.write(encrypted_pass);
			fw.close();
			genspaceLogin = login;
			return 0;
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * This method fulfills the contract of the {@link VisualPlugin} interface.
	 * It returns the GUI component for this visual plugin.
	 */
	public Component getComponent() {
		// In this case, this object is also the GUI component.
		return this;
	}

	public JPanel getThisPanel() {
		return this;
	}



	/**
	 * For notifying other components of a login event.
	 */
	public void addActionListener(ActionListener al)
	{
		listeners.add(al);
	}

	public void notifyActionListeners(ActionEvent e)
	{
		for (ActionListener al : listeners)
		{
			al.actionPerformed(e);
		}
	}

	private void callRegisterMember() {
		//System.out.println("Register");

		panel = new GenSpaceRegistration();
		this.setLayout(new FlowLayout());
		this.removeAll();
		this.add(panel);
		this.repaint();
		//this.setSize(500, 500);
		this.revalidate();
	}

	/**
	 * For when we want to show this panel in its own frame.
	 */
	public void initFrame()
	{
		frame = new JFrame();
		frame.add(this);
		//frame.setSize(400,400);
		frame.setLocation(0,0);
		frame.setResizable(false);
		frame.setTitle("Please login or register before starting jClaim");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * For when we want to show this panel in its own frame.
	 */
	public void showFrame()
	{
		frame.setVisible(true);
	}

	/**
	 * For when we want to show this panel in its own frame.
	 */
	public void hideFrame()
	{
		frame.setVisible(false);
	}

	public static void main(String[] args)
	{
		GenSpaceLogin login = new GenSpaceLogin();

		login.initFrame();
		login.showFrame();
	}
}
