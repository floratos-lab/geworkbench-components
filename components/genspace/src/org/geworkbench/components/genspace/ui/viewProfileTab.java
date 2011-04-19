package org.geworkbench.components.genspace.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.entity.User;

public class viewProfileTab extends SocialTab {
	boolean isFriend;
	User u;

	public viewProfileTab(User p) {
		this.u = p;

		
		this.isFriend = p.isFriends();
		
		String desc = p.toHTML();
		JLabel profile = new JLabel(desc);
		JLabel friend = new JLabel();
		panel1 = new JPanel();
		panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
		panel1.add(profile);
		JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
		panel1.add(sep);
		if (isFriend) {
			friend.setText("(" + p.getShortName() + " is a friend)");
		} else {
			friend.setText("(" + p.getShortName()
					+ " is not a friend)");
		}
		panel1.add(friend);
		if (isFriend) {
			JButton removeFriend = new JButton("Remove friend");
			removeFriend.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

						@Override
						protected Void doInBackground()
							 {
							GenSpaceServerFactory.getFriendOps().removeFriend(u.getId());
							return null;
							
						}

						@Override
						protected void done() {
							JOptionPane.showMessageDialog(panel1,
									"You are no longer friends with "
											+ u.getShortName() + "");
						}

					};
					worker.execute();
				}
			});
			panel1.add(removeFriend);
		} else {
			JButton addFriend = new JButton("Add as friend");
			addFriend.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

						@Override
						protected Void doInBackground()
								 {
								GenSpaceServerFactory.getFriendOps().addFriend(u.getId());
								return null;
						}

						@Override
						protected void done() {
							JOptionPane
									.showMessageDialog(
											panel1,
											"A friend sent for "
													+ u.getShortName()
													+ "'s approval. You will not become friends until he or she accepts your request.");
						}

					};
					worker.execute();
				}
			});
			panel1.add(addFriend);
		}
	}

	
	@Override
	public String getName() {
//		return p.profile.get("first_name") + " " + p.profile.get("last_name")
//				+ "'s profile";
		return "";
	}
}
