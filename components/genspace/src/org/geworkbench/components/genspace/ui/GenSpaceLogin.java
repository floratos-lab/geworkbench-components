//package genspace.ui;
package org.geworkbench.components.genspace.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.geworkbench.components.genspace.GenSpace;
import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.MahoutRecommendationPanel;
import org.geworkbench.components.genspace.chat.ChatReceiver;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.engine.config.VisualPlugin;

/**
 * This is an example geWorkbench component.
 * 
 * 
 */
// This annotation lists the data set types that this component accepts.
// The component will only appear when a data set of the appropriate type is
// selected.
// @AcceptTypes({DSMicroarraySet.class})
public class GenSpaceLogin extends JPanel implements VisualPlugin,
		ActionListener, Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1072570766485376819L;

	public static ChatReceiver chatHandler = new ChatReceiver();

	private JLabel l1, l2, l3;
	private JTextField tf;
	private JPasswordField pf;
	private JButton b1, b2, b3;
	
	private JLabel msgText, msgText1, msgText2, msgText3, msgText4, msgText5,
			msgText6;

	public MahoutRecommendationPanel mahoutRecommendationPanel;
	
	// a list of all ActionListeners waiting for login events
	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

	// the surrounding JFrame when this is used as a standalone window
	private JFrame frame;

	GenSpaceRegistration panel;

	/**
	 * Constructor
	 */
	public GenSpaceLogin() {
		run();
	}

	public void test()
	{
		System.out.println("test");
		tf.setText("jon");
		pf.setText("test123");
		ActionEvent e = new ActionEvent(b1, 1, "foo");
		actionPerformed(e);
	}
	@Override
	public void run() {

		setLayout(new BorderLayout());
		JPanel onePanel = new JPanel();
		JPanel twoPanel = new JPanel();
		onePanel.setSize(150, 150);
		onePanel.setLayout(new GridLayout(2, 1));

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
		msgPanel.setLayout(new GridLayout(7, 1));

		msgText = new JLabel("Not a registered user yet? ");
		msgText1 = new JLabel(
				"Register to take advantage of genSpace security features.");
		msgText2 = new JLabel("As a registered user you will be able to:");
		msgText3 = new JLabel("1. Set your data visbility preferences.");
		msgText4 = new JLabel("2. Post comments and rate workflows and tools.");
		msgText5 = new JLabel(
				"You can also choose to continue using genSpace without a login in which case");
		msgText6 = new JLabel("default security preferences will be applied.");

		msgPanel.add(msgText);
		msgPanel.add(msgText1);
		msgPanel.add(msgText2);
		msgPanel.add(msgText3);
		msgPanel.add(msgText4);
		msgPanel.add(msgText5);
		msgPanel.add(msgText6);
	
		
		onePanel.add(msgPanel);
		twoPanel.add(onePanel);
		
		twoPanel.setPreferredSize(new Dimension(1024, 400));
		add(twoPanel, BorderLayout.CENTER);

	}
	
	public void addMahoutPanel() {
		mahoutRecommendationPanel = new MahoutRecommendationPanel();
		mahoutRecommendationPanel.displayRecommedations();
		mahoutRecommendationPanel.setPreferredSize(new Dimension(1000, 250));
		add(mahoutRecommendationPanel, BorderLayout.SOUTH);
	}

	/**
	 * Action Listener
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == b1) {

			javax.swing.SwingWorker<User, Void> worker = new javax.swing.SwingWorker<User, Void>() {
				int evt;
				@Override
				protected void done() {
					if(GenSpaceServerFactory.getUser() != null)
					{
						String msg = "User Logged in.";
						GenSpace.getInstance().handleLogin();
						mahoutRecommendationPanel.handleLogin();
						JOptionPane.showMessageDialog(getComponent(),
								msg);
						chatHandler.login(tf.getText(),
								new String(pf.getPassword()));
						GenSpace.networksPanels.updateFormFields();
						GenSpaceSecurityPanel p = new GenSpaceSecurityPanel(
								GenSpaceServerFactory.getUsername());
						p.addMahoutPanel();
						p.handleLogin();
						getThisPanel().removeAll();
						getThisPanel().add(p);
						//this.setSize(500, 500);
						getThisPanel().revalidate();
						GenSpace.getStatusBar().stop(evt);
					}
					super.done();
				}
				@Override
				public User doInBackground() {
					evt = GenSpace.getStatusBar().start("Logging in");
					b1.setEnabled(false);

					try {
						StringBuffer errMsg = new StringBuffer();

						if (isValid(errMsg))
						{

							if (GenSpaceServerFactory.userLogin(tf.getText(),new String(pf.getPassword()))) {
								return GenSpaceServerFactory.getUser();

							} else {
								GenSpace.getStatusBar().stop(evt);
								String msg = "User Log in failed.";
								JOptionPane.showMessageDialog(getComponent(),
										msg);
							}
						} else {
							GenSpace.getStatusBar().stop(evt);
							JOptionPane.showMessageDialog(getComponent(),
									errMsg.toString(), "Error Information",
									JOptionPane.INFORMATION_MESSAGE);
							getThisPanel().revalidate();
						}
					} catch (Exception ex) {
						GenSpace.getStatusBar().stop(evt);
						ex.printStackTrace();
					}
					b1.setEnabled(true);

					return null;
				}
			};
			worker.execute();

		} else if (e.getSource() == b2) {
			tf.setText("");
			pf.setText("");
			l3.setVisible(false);
		} else if (e.getSource() == b3) {
			callRegisterMember();

			if (GenSpaceServerFactory.getUser() != null) {
				l3.setText("Login Created");
			} else {
				l3.setText("Login Creation Failed");
			}
			l3.setVisible(true);
		}

	}

	private boolean empty(String str) {
		if ("".equalsIgnoreCase(str) || null == str)
			return true;
		else
			return false;
	}

	public boolean isValid(StringBuffer msg) {

		String id = tf.getText();
		char input[] = pf.getPassword();
		String pw = new String(input);

		boolean valid = true;

		if (empty(id)) {
			msg.append("UserId cannot be empty\n");
			valid = false;
		}
		if (empty(pw)) {
			msg.append("Pasword cannot be empty\n");
			valid = false;
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

		// System.out.println("valid : " + valid);
		return valid;
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

	public JPanel getThisPanel() {
		return this;
	}

	/**
	 * For notifying other components of a login event.
	 */
	public void addActionListener(ActionListener al) {
		listeners.add(al);
	}

	public void notifyActionListeners(ActionEvent e) {
		for (ActionListener al : listeners) {
			al.actionPerformed(e);
		}
	}

	private void callRegisterMember() {
		// System.out.println("Register");

		panel = new GenSpaceRegistration();
		this.setLayout(new FlowLayout());
		this.removeAll();
		this.add(panel);
		this.repaint();
		// this.setSize(500, 500);
		this.revalidate();
	}

	/**
	 * For when we want to show this panel in its own frame.
	 */
	public void initFrame() {
		frame = new JFrame();
		frame.add(this);
		// frame.setSize(400,400);
		frame.setLocation(0, 0);
		frame.setResizable(false);
		frame.setTitle("Please login or register before starting jClaim");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * For when we want to show this panel in its own frame.
	 */
	public void showFrame() {
		frame.setVisible(true);
	}

	/**
	 * For when we want to show this panel in its own frame.
	 */
	public void hideFrame() {
		frame.setVisible(false);
	}

	public static void main(String[] args) {
		GenSpaceLogin login = new GenSpaceLogin();

		login.initFrame();
		login.showFrame();
	}
}
