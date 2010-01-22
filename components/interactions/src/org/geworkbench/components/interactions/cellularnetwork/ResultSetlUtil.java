package org.geworkbench.components.interactions.cellularnetwork;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Properties;
import java.util.TreeMap;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * @author oleg shteynbuk
 * 
 * tmp class that sends sql query over HTTP for execution see interactionsweb
 * component
 * 
 * some functionality is similar to functionality provided by classes:
 * DriverManager, Statement, Connection and ResultSet from java.sql
 * 
 * result set is returned as a text. data could be retrieved by column name or
 * by column number. supported data types: String, BigDecimal, double. nulls are
 * supported. "|" and end of line should not be part of the data as they are
 * used as delimiters.
 * 
 * if server couldn't process request, including SQLException, client will get
 * IOException. not sure that custom exception class is needed at this time.
 * 
 * for an example of usage and testing see function main.
 * 
 */
public class ResultSetlUtil {

	// currently in two files
	public static final int SPLIT_ALL = -2;
	public static final String DEL = "|";
	public static final String REGEX_DEL = "\\|";
	public static final String ORACLE = "oracle";
	public static final String MYSQL = "mysql";
	public static final String NULL_STR = "null";
	public static final BigDecimal NULL_BIGDECIMAL = new BigDecimal(0);

	public static String INTERACTIONS_SERVLET_URL = null;

	private TreeMap<String, Integer> metaMap;
	private String[] row;
	private String decodedString;
	private BufferedReader in;

	public ResultSetlUtil(BufferedReader in) throws IOException {
		this.in = in;
		metaMap = new TreeMap<String, Integer>();

		// metadata
		next();

		processMetadata();
	}

	public static void setUrl(String aUrl) {
		INTERACTIONS_SERVLET_URL = aUrl;
	}

	// reconstruct metadata
	public void processMetadata() {
		for (int i = 0; i < row.length; i++) {
			metaMap.put(row[i], new Integer(i + 1));
		}
		return;
	}

	public int getColumNum(String name) {
		int ret = metaMap.get(name).intValue();
		return ret;
	}

	public double getDouble(String colmName) {
		int columNum = getColumNum(colmName);

		return getDouble(columNum);
	}

	public double getDouble(int colmNum) {
		double ret = 0;

		String tmp = getString(colmNum).trim();

		if (!tmp.equals(NULL_STR)) {
			ret = Double.valueOf(tmp).doubleValue();
		}

		return ret;
	}

	public BigDecimal getBigDecimal(String colmName) {
		int columNum = getColumNum(colmName);
		return getBigDecimal(columNum);
	}

	public BigDecimal getBigDecimal(int colmNum) {
		BigDecimal ret = NULL_BIGDECIMAL;

		String tmp = getString(colmNum).trim();
		if (!tmp.equals(NULL_STR)) {
			ret = new BigDecimal(tmp);
		}

		return ret;
	}

	public String getString(String colmName) {
		int coluNum = getColumNum(colmName);
		return getString(coluNum);
	}

	public String getString(int colmNum) {
		// get from row
		return row[colmNum - 1];
	}

	public boolean next() throws IOException {
		boolean ret = false;

		if ((decodedString = in.readLine()) != null) {

			// System.out.println( decodedString);

			row = decodedString.split(REGEX_DEL, SPLIT_ALL);
			ret = true;
		}

		return ret;
	}

	public void close() throws IOException {
		in.close();
	}

	public static HttpURLConnection getConnection(String url)
			throws IOException {
		URL aURL = new URL(url);
		HttpURLConnection aConnection = (HttpURLConnection) (aURL
				.openConnection());
		aConnection.setDoOutput(true);
		return aConnection;
	}

	public static ResultSetlUtil executeQuery(String aSQL, String db,
			String urlStr) throws IOException, UnAuthenticatedException {

		HttpURLConnection aConnection = getConnection(urlStr);

		String data = db + DEL + aSQL;

		OutputStreamWriter out = new OutputStreamWriter(aConnection
				.getOutputStream());

		out.write(data);
		out.close();

		// errors, exceptions

		int respCode = aConnection.getResponseCode();

		if (respCode == HttpServletResponse.SC_UNAUTHORIZED)
			throw new UnAuthenticatedException("server response code = "
					+ respCode);

		if ((respCode == HttpServletResponse.SC_BAD_REQUEST)
				|| (respCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR)) {
			throw new IOException("server response code = " + respCode
					+ ", see server logs");
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(
				aConnection.getInputStream()));

		ResultSetlUtil rs = new ResultSetlUtil(in);

		return rs;
	}

	// test
	public static void main(String[] args) {

		ResultSetlUtil rs = null;
		try {
			Properties iteractionsProp = new Properties();
			iteractionsProp.load(new FileInputStream(
					"conf/application.properties"));
			String interactionsServletUrl = iteractionsProp
					.getProperty("interactions_servlet_url");
			ResultSetlUtil.setUrl(interactionsServletUrl);
			// java.net.URLConnection.setDefaultAllowUserInteraction(true);
			Authenticator.setDefault(new BasicAuthenticator());

			String aSQL = "getPairWiseInteraction" + ResultSetlUtil.DEL + "165"
					+ ResultSetlUtil.DEL + "BCi" + ResultSetlUtil.DEL + "1.0";
			rs = ResultSetlUtil.executeQuery(aSQL, MYSQL,
					INTERACTIONS_SERVLET_URL);

			while (rs.next()) {

				BigDecimal msid1 = rs.getBigDecimal("ms_id1");
				System.out.println("msid1 = " + msid1);

				BigDecimal msid2 = rs.getBigDecimal("ms_id2");
				System.out.println("msid2 = " + msid2);

				BigDecimal confidenceValue = rs
						.getBigDecimal("confidence_value");
				// double confidenceValue = rs.getDouble("confidence_value");
				System.out.println("confidence_value = " + confidenceValue);

				String isModulated = rs.getString("is_modulated");
				System.out.println("is_modulated = " + isModulated);

				String source = rs.getString("source");
				System.out.println("source = " + source);

				String interactionType = rs.getString("interaction_type");
				System.out.println("name = " + interactionType);
			}
			// rs.close();

		} catch (IOException ie) {
			// TODO Auto-generated catch block
			ie.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();

				} catch (Exception e) {
				}

			}
			System.exit(0);
		}
	}

}

/**
 * 
 * The following LoginDialog is not used by application, just for a testing
 * purpose."
 */

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

/**
 * Extends Authenticator to allow for user login *
 * 
 */
class BasicAuthenticator extends java.net.Authenticator {
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
