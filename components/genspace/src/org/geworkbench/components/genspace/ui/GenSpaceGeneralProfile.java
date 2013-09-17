package org.geworkbench.components.genspace.ui;

import java.awt.Component;
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
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.engine.config.VisualPlugin;

public class GenSpaceGeneralProfile extends JPanel implements VisualPlugin,
		ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9002035453385092032L;

	private JFrame jframe;

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

	JButton save;

	GenSpaceLogin login;

	public GenSpaceGeneralProfile() {
		

		initComponents();
	}

	private void initComponents() {
		// String username = LoginManager.loggedInUser;
		User u = GenSpaceServerFactory.getUser();
		this.setLayout(new GridLayout(15, 2));

		JLabel j2 = new JLabel("First Name");
		fname = new JTextField(u.getFirstName(), 20);
		add(j2);
		add(fname);

		JLabel j3 = new JLabel("Last Name");
		lname = new JTextField(u.getLastName(), 20);
		add(j3);
		add(lname);

		JLabel j4 = new JLabel("Lab Affiliation *");
		labaff = new JTextField(u.getLabAffiliation(), 20);
		add(j4);
		add(labaff);

		JLabel emailLabel = new JLabel("Email Address");
		email = new JTextField(u.getEmail(), 20);
		add(emailLabel);
		add(email);

		JLabel phoneLabel = new JLabel("Phone");
		phone = new JTextField(u.getPhone(), 20);
		add(phoneLabel);
		add(phone);

		JLabel j5 = new JLabel("Address 1");
		addr1 = new JTextField(u.getAddr1(), 20);
		add(j5);
		add(addr1);

		JLabel j6 = new JLabel("Address 2");
		addr2 = new JTextField(u.getAddr2(), 20);
		add(j6);
		add(addr2);

		JLabel j7 = new JLabel("City");
		city = new JTextField(u.getCity(), 20);
		add(j7);
		add(city);

		JLabel j9 = new JLabel("State");
		state = new JTextField(u.getState(), 20);
		add(j9);
		add(state);

		JLabel j8 = new JLabel("ZIP Code");
		zipcode = new JTextField(u.getZipcode(), 20);
		add(j8);
		add(zipcode);


		save = new JButton("Save");
		save.addActionListener(this);

		add(save);

		save.setEnabled(true);
	}

	private void applyProfileChanges() {

		GenSpaceServerFactory.getUser().setFirstName(fname.getText());
		GenSpaceServerFactory.getUser().setLastName(lname.getText());
		GenSpaceServerFactory.getUser().setLabAffiliation(labaff.getText());
		GenSpaceServerFactory.getUser().setEmail(email.getText());
		GenSpaceServerFactory.getUser().setPhone(phone.getText());
		GenSpaceServerFactory.getUser().setAddr1(addr1.getText());
		GenSpaceServerFactory.getUser().setAddr2(addr2.getText());
		GenSpaceServerFactory.getUser().setCity(city.getText());
		GenSpaceServerFactory.getUser().setState(state.getText());
		GenSpaceServerFactory.getUser().setZipcode(zipcode.getText());
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
							applyProfileChanges();							
							// System.out.println("valid");
							if (GenSpaceServerFactory.userUpdate()) {
								String msg = "Information updated";

								JOptionPane.showMessageDialog(null, msg);

								// System.out.println("information updated");
							} else {
								String msg = "Information update failed";

								JOptionPane.showMessageDialog(null, msg);

							}
						}

						else {
							JOptionPane.showMessageDialog(null,
									errMsg.toString(), "Error Information",
									JOptionPane.INFORMATION_MESSAGE);

							getThisPanel().revalidate();
						}
					} catch (Exception ex) {
					}
					save.setEnabled(true);

					return null;
				}
			};
			worker.execute();
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

		String labaffStr = labaff.getText();
		boolean valid = true;

		if (empty(labaffStr)) {
			msg.append("Lab affiliation cannot be empty\n");
			valid = false;
		}

		Pattern pattern;
		Matcher matcher;

		// Phone number validation
		if (!empty(phone.getText())) {
			pattern = Pattern.compile("[^0-9a-zA-Z()-]");

			matcher = pattern.matcher(phone.getText());

			if (matcher.find()) {
				msg.append("Phone number contains invalid characters\n");
				valid = false;
			}
		}
		// email validation
		if (!empty(email.getText())) {
			pattern = Pattern.compile("[0-9a-zA-Z()-_.]+@[0-9a-zA-Z()-_.]+");

			matcher = pattern.matcher(email.getText());
			if (!matcher.find()) {
				msg.append("Invalid Email.\n");
				valid = false;
			}

		}
		// System.out.println("valid : " + valid);
		return valid;
	}

	public void initFrame() {
		jframe = new JFrame();
		jframe.add(this);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setSize(500, 500);
	}

	public void showFrame() {
		jframe.setVisible(true);
	}

	public void hideFrame() {
		jframe.setVisible(false);
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

	public static void main(String args[]) throws Exception {
		GenSpaceGeneralProfile panel = new GenSpaceGeneralProfile();
		panel.initFrame();
		panel.showFrame();
	}
}
