package org.geworkbench.components.genspace.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.geworkbench.components.genspace.GenSpace;
import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.server.wrapper.UserWrapper;
import org.geworkbench.components.genspace.ui.AutoCompleteCombo.Model;

/**
 * Created by IntelliJ IDEA. User: jon Date: Aug 28, 2010 Time: 11:45:56 AM To
 * change this template use File | Settings | File Templates.
 */
public class SocialNetworksHome implements UpdateablePanel {
	private JLabel a1FriendRequestLabel;
//	private JLabel a1NetworkRequestLabel;
	private JButton goButton;
	private JLabel friendsLink;
	private JLabel requestsLink;
	private JLabel networksLink;
	private JLabel profileLink;
	private JLabel chatLink;

	private JLabel backLabel;
	private JLabel settingsLink;
	private JPanel content;
	private JLabel currentTabLabel;
	private JPanel panel1;
	private AutoCompleteCombo friendsSearch;
	private JPanel decoyPanel;
	private SocialTab current;
	private SocialTab friends;
	private SocialTab requests;
	private SocialTab profile;
	private SocialTab settings;
	private SocialTab networks;
//	private SocialTab viewProfile;
	private Stack<SocialTab> last = new Stack<SocialTab>();
	private JPanel shownPanel;

	private static SocialNetworksHome instance;
	public static SocialNetworksHome getInstance()
	{
		return instance;
	}
	
	public void goToFriends()
	{
		setContent(friends);
		friends.updateFormFields();
		updateFormFields();
	}
	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return shownPanel;
	}
	
	public void bringUpProfile(User u) {
		setContent(new viewProfileTab(new UserWrapper(u)));
	}

	private void createUIComponents() {
		friendsSearch = new AutoCompleteCombo();
		friendsSearch.setSize(150, (int) friendsSearch.getSize().getHeight());
		networks = new networksTab();
		networks.parentFrame = this;
		friends = new friendsTab();
		friends.parentFrame = this;
		profile = new profileTab();
		profile.parentFrame = this;
		settings = new privacyTab();
		settings.parentFrame = this;
		requests = new requestsTab();
		requests.parentFrame = this;
		a1FriendRequestLabel = new JLabel();
	}

	private void init() {
		instance = this;
		MouseListener listener = new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				String source = ((JLabel) e.getSource()).getText()
						.replace("<html><u>", "").replace("</u></html>", "");
//				System.out.println("Click@" + source);
				if (source.equals("My Networks")) {
					setContent(networks);
				} else if (source.equals("My Profile")) {
					setContent(profile);
				} else if (source.equals("My Friends")) {
					setContent(friends);
				} else if (source.equals("Settings")) {
					setContent(settings);
				} else if (source.equals("Back")) {
					setContentNoHistory(last.pop());
				} else if (source.equals("View Requests")) {
					setContent(requests);
				}
				else if(source.equals("Chat"))
				{
					GenSpaceLogin.chatHandler.rf.setVisible(true);
					GenSpaceLogin.chatHandler.rf.toFront();
					GenSpaceLogin.chatHandler.rf.setAvailable();
				}

			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				Cursor c = new Cursor(Cursor.HAND_CURSOR);
				panel1.setCursor(c);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				Cursor c = new Cursor(Cursor.DEFAULT_CURSOR);
				panel1.setCursor(c);
			}

		};
		goButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (GenSpaceServerFactory.isLoggedIn()) {
					SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {

						@Override
						protected User doInBackground() throws Exception {
							return GenSpaceServerFactory.getUserOps().getProfile(friendsSearch.getText());
						}

						@Override
						protected void done() {
							User prof = null;
							try {
								prof = get();
							} catch (InterruptedException e) {
								GenSpace.logger.warn("Error",e);
							} catch (ExecutionException e) {
								GenSpaceServerFactory.handleExecutionException(e);
								return;
							}
							if (prof == null) {
								JOptionPane
										.showMessageDialog(panel1,
												"Error: Could not find user's profile!");
							} else
								setContent(new viewProfileTab(new UserWrapper(prof)));
						}

					};
					worker.execute();
				}
			}
		});
		networksLink.addMouseListener(listener);
		profileLink.addMouseListener(listener);
		settingsLink.addMouseListener(listener);
		friendsLink.addMouseListener(listener);
		backLabel.addMouseListener(listener);
		requestsLink.addMouseListener(listener);
		chatLink.addMouseListener(listener);
		
		decoyPanel = new JPanel();
		shownPanel = new JPanel();
		shownPanel.setLayout(new BoxLayout(shownPanel, BoxLayout.X_AXIS));
		decoyPanel.add(new JLabel(
				"Please log in to utilize GenSpace's social features"));
		shownPanel.add(decoyPanel);

		setContent(friends);
		updateFormFields();
	}

	void setContent(SocialTab panel) {
		if (panel != null) {
			last.push(current);
			setContentNoHistory(panel);
		}
	}

	void setContentNoHistory(SocialTab panel) {
		if (panel != null) {
			currentTabLabel.setText(panel.getName());
			current = panel;
			content.removeAll();
			content.add(
					panel.getPanel(),
					new com.intellij.uiDesigner.core.GridConstraints(
							0,
							0,
							1,
							1,
							com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
							com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH,
							com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
									| com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
							com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
									| com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
							null, null, null, 0, false));
			panel1.repaint();
			panel1.revalidate();
		}
		updateFormFields();
	}
	
	{
		// GUI initializer generated by IntelliJ IDEA GUI Designer
		// >>> IMPORTANT!! <<<
		// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
		init();
	}

	private boolean showingDecoy = true;
	List<User> ourFriends = null;
	public boolean pendingFriendRequestTo(UserWrapper uw) {
		if(uw.isFriendsWith())
			return false;
		for(User u : ourFriends)
		{
			if(u.getUsername().equals(uw.getUsername()))
				return true;
		}
		return false;
	}

	public void updateFormFields() {

		if (GenSpaceServerFactory.isLoggedIn() && showingDecoy) {
			shownPanel.removeAll();
			shownPanel.add(panel1);
			showingDecoy = false;
		} else if (!	GenSpaceServerFactory.isLoggedIn() && !showingDecoy) {
			shownPanel.removeAll();
			shownPanel.add(decoyPanel);
			showingDecoy = true;
		}
		if (GenSpaceServerFactory.isLoggedIn()) {
			current.updateFormFields();
			SwingWorker<List<User>, Void> worker = new SwingWorker<List<User>, Void>() {
				int evt;
				@Override
				protected List<User> doInBackground()
						throws Exception {
					evt = GenSpace.getStatusBar().start("Refreshing social tab");
					return GenSpaceServerFactory.getFriendOps().getFriends();
				}

				@Override
				protected void done() {
					List<User> lst = null;
					GenSpace.getStatusBar().stop(evt);
					try {
						lst = get();
					} catch (InterruptedException e) {
						GenSpace.logger.warn("Error",e);
					} catch (ExecutionException e) {
						GenSpaceServerFactory.clearCache();
						updateFormFields();
						return;
					}
					ourFriends = lst;
//					friendsSearch.setText("");
					Model m = (Model) friendsSearch.getModel();
					m.data.clear();
					if(lst != null)
					for (User t : lst) {
						m.data.add(t.getFirstName() + " " + t.getLastName());
					}

				}
			};
			worker.execute();
			SwingWorker<Integer, Void> worker2 = new SwingWorker<Integer, Void>() {

				@Override
				protected Integer doInBackground()
						throws Exception {
					return GenSpaceServerFactory.getFriendOps().getFriendRequests().size() +
					GenSpaceServerFactory.getNetworkOps().getNumberOfNetworkRequests();
				}

				@Override
				protected void done() {
					Integer res = null;
					try {
						res = get();
					} catch (InterruptedException e) {
						GenSpace.logger.warn("Error",e);
					} catch (ExecutionException e) {
						e.printStackTrace();
						GenSpaceServerFactory.clearCache();
						updateFormFields();
						return;
//						GenSpace.logger.info("Error",e);
					}
					if(res != null)
					a1FriendRequestLabel.setText("" + res + " Request"
							+ (res == 1 ? "" : "s"));
				}
			};
			worker2.execute();
		}

	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT
	 * edit this method OR call it in your code!
	 * 
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		createUIComponents();
//		panel1 = new JPanel();
//		panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(10,
//				6, new Insets(0, 0, 0, 0), -1, -1));
//		panel1.setForeground(new Color(-16777012));
//		final JLabel label1 = new JLabel();
//		label1.setFont(new Font(label1.getFont().getName(), Font.BOLD, 16));
//		label1.setText("GenSpace Social Center");
//		panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0,
//				0, 1, 1,
//				com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
//				com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
//				com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//				com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//				null, new Dimension(60, 35), null, 0, false));
//		networksLink = new JLabel();
//		networksLink.setForeground(new Color(-16777012));
//		networksLink.setText("<html><u>My Networks</u></html>");
//		panel1.add(
//				networksLink,
//				new com.intellij.uiDesigner.core.GridConstraints(
//						3,
//						0,
//						1,
//						1,
//						com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
//						com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//						null, null, null, 0, false));
//		friendsLink = new JLabel();
//		friendsLink.setForeground(new Color(-16777012));
//		friendsLink.setText("<html><u>My Friends</u></html>");
//		panel1.add(
//				friendsLink,
//				new com.intellij.uiDesigner.core.GridConstraints(
//						4,
//						0,
//						1,
//						1,
//						com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
//						com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//						null, null, null, 0, false));
//		profileLink = new JLabel();
//		profileLink.setEnabled(false);
//		profileLink.setForeground(new Color(-16777012));
//		profileLink.setText("<html><u>My Profile</u></html>");
//		panel1.add(
//				profileLink,
//				new com.intellij.uiDesigner.core.GridConstraints(
//						2,
//						0,
//						1,
//						1,
//						com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
//						com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//						null, null, null, 0, false));
//		final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
//		panel1.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(1,
//				0, 1, 1,
//				com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
//				com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1,
//				com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//				null, new Dimension(11, 31), null, 0, false));
//		content = new JPanel();
//		content.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1,
//				1, new Insets(0, 0, 0, 0), -1, -1));
//		content.setBackground(new Color(-986896));
//		content.setEnabled(true);
//		content.putClientProperty("html.disable", Boolean.FALSE);
//		JScrollPane scroller = new JScrollPane();
//		scroller.setViewportView(content);
//        panel1.add(scroller, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 8, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
//
//		final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
//		content.add(
//				spacer2,
//				new com.intellij.uiDesigner.core.GridConstraints(
//						0,
//						0,
//						1,
//						1,
//						com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
//						com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL,
//						1,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW,
//						null, null, null, 0, false));
//		final JLabel label2 = new JLabel();
//		label2.setText("Search by Username");
//		panel1.add(label2, new com.intellij.uiDesigner.core.GridConstraints(0,
//				3, 1, 1,
//				com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
//				com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
//				com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//				com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//				null, null, null, 0, false));
//		final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
//        panel1.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(90, 11), null, 0, false));
//
//		a1FriendRequestLabel = new JLabel();
//		a1FriendRequestLabel.setText("0 Requests");
//		panel1.add(
//				a1FriendRequestLabel,
//				new com.intellij.uiDesigner.core.GridConstraints(
//						7,
//						0,
//						1,
//						1,
//						com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
//						com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//						null, null, null, 0, false));
//		requestsLink = new JLabel();
//		requestsLink.setText("<html><u>(View Requests)</u></html>");
//		requestsLink.setForeground(new Color(-16777012));
//		panel1.add(
//				requestsLink,
//				new com.intellij.uiDesigner.core.GridConstraints(
//						8,
//						0,
//						1,
//						1,
//						com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
//						com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//						null, null, null, 0, false));
//		final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
//		panel1.add(
//				spacer4,
//				new com.intellij.uiDesigner.core.GridConstraints(
//						9,
//						0,
//						1,
//						1,
//						com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
//						com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL,
//						1,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW,
//						null, null, null, 0, false));
//		final com.intellij.uiDesigner.core.Spacer spacer5 = new com.intellij.uiDesigner.core.Spacer();
//		panel1.add(spacer5, new com.intellij.uiDesigner.core.GridConstraints(6,
//				0, 1, 1,
//				com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
//				com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1,
//				com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//				null, null, null, 0, false));
//		button1 = new JButton();
//		button1.setText("Go");
//		panel1.add(button1, new com.intellij.uiDesigner.core.GridConstraints(0,
//				5, 1, 1,
//				com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
//				com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
//				com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//				com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//				null, null, null, 0, false));
//		settingsLink = new JLabel();
//		settingsLink.setForeground(new Color(-16777012));
//		settingsLink.setText("<html><u>Settings</u></html>");
//		panel1.add(
//				settingsLink,
//				new com.intellij.uiDesigner.core.GridConstraints(
//						5,
//						0,
//						1,
//						1,
//						com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
//						com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//						null, null, null, 0, false));
//		currentTabLabel = new JLabel();
//		currentTabLabel.setFont(new Font(currentTabLabel.getFont().getName(),
//				Font.BOLD, 18));
//		currentTabLabel.setText("My Friends");
//		panel1.add(
//				currentTabLabel,
//				new com.intellij.uiDesigner.core.GridConstraints(
//						1,
//						2,
//						1,
//						1,
//						com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
//						com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//						null, null, null, 0, false));
//		backLabel = new JLabel();
//		backLabel.setForeground(new Color(-16776976));
//		backLabel.setText("<html><u>Back</u></html>");
//		panel1.add(backLabel, new com.intellij.uiDesigner.core.GridConstraints(
//				1, 1, 1, 1,
//				com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
//				com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
//				com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//				com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//				null, null, null, 0, false));
//		panel1.add(
//				friendsSearch,
//				new com.intellij.uiDesigner.core.GridConstraints(
//						0,
//						4,
//						1,
//						1,
//						com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
//						com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
//						friendsSearch.getSize(), null, null, 0, false));
		createUIComponents();
       panel1 = new JPanel();
       panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(11, 7, new Insets(0, 0, 0, 0), -1, -1));
       panel1.setForeground(new Color(-16777012));
       final JLabel label1 = new JLabel();
       label1.setFont(new Font(label1.getFont().getName(), Font.BOLD, 16));
       label1.setText("GenSpace");
       panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 35), null, 0, false));
       networksLink = new JLabel();
       networksLink.setForeground(new Color(-16777012));
       networksLink.setText("<html><u>My Networks</u></html>");
       panel1.add(networksLink, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
       friendsLink = new JLabel();
       friendsLink.setForeground(new Color(-16777012));
       friendsLink.setText("<html><u>My Friends</u></html>");
       panel1.add(friendsLink, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
       profileLink = new JLabel();
       profileLink.setEnabled(false);
       profileLink.setForeground(new Color(-16777012));
       profileLink.setText("<html><u>My Profile</u></html>");
       panel1.add(profileLink, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
       final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
       panel1.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(11, 31), null, 0, false));
       content = new JPanel();
       content.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
       content.setBackground(new Color(-986896));
       content.setEnabled(true);
       content.putClientProperty("html.disable", Boolean.FALSE);
       panel1.add(content, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 9, 6, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
       final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
       content.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
       a1FriendRequestLabel.setText("1 Friend Request");
       panel1.add(a1FriendRequestLabel, new com.intellij.uiDesigner.core.GridConstraints(8, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
       requestsLink = new JLabel();
       requestsLink.setForeground(new Color(-16777012));
       requestsLink.setText("<html><u>View Requests</u></html>");
       panel1.add(requestsLink, new com.intellij.uiDesigner.core.GridConstraints(9, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
       final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
       panel1.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(10, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
       final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
       panel1.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(7, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
       settingsLink = new JLabel();
       settingsLink.setForeground(new Color(-16777012));
       settingsLink.setText("<html><u>Settings</u></html>");
       panel1.add(settingsLink, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
       backLabel = new JLabel();
       backLabel.setEnabled(true);
       backLabel.setForeground(new Color(-16776976));
       backLabel.setText("<html><u>Back</u></html>");
       panel1.add(backLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
       final JLabel label2 = new JLabel();
       label2.setText("Search");
       panel1.add(label2, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
       panel1.add(friendsSearch, new com.intellij.uiDesigner.core.GridConstraints(0, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(180, -1), new Dimension(180, -1), null, 0, false));
       goButton = new JButton();
       goButton.setText("Go");
       panel1.add(goButton, new com.intellij.uiDesigner.core.GridConstraints(0, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
       final com.intellij.uiDesigner.core.Spacer spacer5 = new com.intellij.uiDesigner.core.Spacer();
       panel1.add(spacer5, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
       currentTabLabel = new JLabel();
       currentTabLabel.setFont(new Font(currentTabLabel.getFont().getName(), Font.BOLD, 18));
       currentTabLabel.setText("My Friends");
       panel1.add(currentTabLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
       chatLink = new JLabel();
       chatLink.setForeground(new Color(-16777012));
       chatLink.setText("<html><u>Chat</u></html>");
       panel1.add(chatLink, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

	}




}