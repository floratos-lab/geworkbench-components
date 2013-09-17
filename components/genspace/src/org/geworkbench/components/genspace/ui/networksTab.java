package org.geworkbench.components.genspace.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.geworkbench.components.genspace.GenSpace;
import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.server.stubs.Network;
import org.geworkbench.components.genspace.server.stubs.UserNetwork;
import org.geworkbench.components.genspace.server.wrapper.UserWrapper;
import org.geworkbench.components.genspace.ui.AutoCompleteCombo.Model;

/**
 * Created by IntelliJ IDEA. User: jon Date: Aug 28, 2010 Time: 12:48:15 PM To
 * change this template use File | Settings | File Templates.
 */
public class networksTab extends SocialTab {
	private AutoCompleteCombo chooseNetwork;
	private JList lstMyNetworks;
	private JButton button1;
	private JButton button2;

	@Override
	public String getName() {
		return "My Networks";

	}

	public void initComponents() {
		lstMyNetworks.setOpaque(false);
		lstMyNetworks.setCellRenderer(new ListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JPanel pan = new JPanel();
				UserNetwork un = (UserNetwork) value;
				pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
				JLabel label = new JLabel(un.getNetwork().getName());
				Font f = new Font(label.getFont().getName(), Font.BOLD, 18);
				label.setFont(f);
				label.setForeground(new Color(-16777012));

				pan.add(label);
				JLabel label2 = new JLabel("Moderated by " + (new UserWrapper(un.getNetwork().getOwner()).getFullName()));
				pan.add(label2);
				pan.add(new JSeparator(SwingConstants.HORIZONTAL));
				if (isSelected)
					pan.setBackground(new Color(251, 251, 228));

				return pan;
			}
		});
		lstMyNetworks.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2)
					parentFrame.setContent(new friendsTab(
							((UserNetwork) lstMyNetworks.getSelectedValue()).getNetwork(),
							parentFrame));
			}
		});
		button1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean found = false;
				for (Network m : cachedAllNetworks) {
					if (m.getName().equals(chooseNetwork.getText())) {
						found = true;
						break;
					}
				}
				for (UserNetwork m : cachedMyNetworks) {
					if (m.getNetwork().getName().equals(chooseNetwork.getText())) {
						return;
					}
				}
				if (found) {
					// Join
					SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
					{
						@Override
						protected Void doInBackground() throws Exception {
							GenSpaceServerFactory.getNetworkOps().joinNetwork(chooseNetwork.getText());
							return null;
						}
						@Override
						protected void done() {
							JOptionPane
							.showMessageDialog(
									panel1,
									"A request has been sent to the network's owner for approval. It will not show up in your list until you have been approved.");
							chooseNetwork.setText("");
							
							super.done();
						}
					};
					worker.execute();
				} else {
					// create
					SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
					{
						@Override
						protected Void doInBackground() throws Exception {
							GenSpaceServerFactory.getNetworkOps().createNetwork(chooseNetwork.getText());
							return null;
						}
						@Override
						protected void done() {
							JOptionPane.showMessageDialog(panel1, "This network has been created");
							updateFormFields();
							chooseNetwork.setText("");
							super.done();
						}
					};
					worker.execute();
					
				}
				updateFormFields();

			}
		});
		button2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				UserNetwork selected = (UserNetwork) lstMyNetworks
						.getSelectedValue();
				if (selected != null)
					try {
						GenSpaceServerFactory.getNetworkOps().leaveNetwork(selected.getId());
					} catch (Exception e1) {
					}
				updateFormFields();
			}
		});
	}

	{
		// GUI initializer generated by IntelliJ IDEA GUI Designer
		// >>> IMPORTANT!! <<<
		// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
		initComponents();
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT
	 * edit this method OR call it in your code!
	 * 
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		panel1 = new JPanel();
		panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(6,
				2, new Insets(0, 0, 0, 0), -1, -1));
		final JLabel label1 = new JLabel();
		label1.setText("Join/Create a network:");
		panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(1,
				1, 1, 1,
				com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
				com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
				com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
				com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
				null, null, null, 0, false));
		chooseNetwork = new AutoCompleteCombo();
		chooseNetwork.setSize(new Dimension(100, chooseNetwork.getWidth()));
		panel1.add(
				chooseNetwork,
				new com.intellij.uiDesigner.core.GridConstraints(
						2,
						1,
						1,
						1,
						com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
						com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
						chooseNetwork.getSize(), null, null, 0, false));
		lstMyNetworks = new JList();
		lstMyNetworks.setBackground(panel1.getBackground());
		JScrollPane jScrollPane1 = new JScrollPane();
		jScrollPane1.setViewportView(lstMyNetworks);
		panel1.add(
				jScrollPane1,
				new com.intellij.uiDesigner.core.GridConstraints(
						0,
						0,
						6,
						1,
						com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
						com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH,
						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW,
						null, new Dimension(320, 50), null, 0, false));
		button1 = new JButton();
		button1.setText("Go");
		panel1.add(
				button1,
				new com.intellij.uiDesigner.core.GridConstraints(
						3,
						1,
						1,
						1,
						com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
						com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
								| com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
						null, null, null, 0, false));
		button2 = new JButton("Leave selected network");
		panel1.add(
				button2,
				new com.intellij.uiDesigner.core.GridConstraints(
						4,
						1,
						1,
						1,
						com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
						com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
								| com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
						null, null, null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
		panel1.add(
				spacer1,
				new com.intellij.uiDesigner.core.GridConstraints(
						5,
						1,
						1,
						1,
						com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
						com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL,
						1,
						com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW,
						null, null, null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
		panel1.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(0,
				1, 1, 1,
				com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
				com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1,
				com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
				null, new Dimension(11, 24), null, 0, false));
	}

	private List<Network> cachedAllNetworks;
	private List<UserNetwork> cachedMyNetworks;

	@Override
	public void updateFormFields() {
		if (GenSpaceServerFactory.isLoggedIn()) {
			SwingWorker<List<UserNetwork>, Void> worker2 = new SwingWorker<List<UserNetwork>, Void>() {

				@Override
				protected List<UserNetwork> doInBackground()
						throws Exception {
					return GenSpaceServerFactory.getNetworkOps().getMyNetworks();
				}

				@Override
				protected void done() {
					try {
						cachedMyNetworks = get();
					} catch (InterruptedException e) {
						GenSpace.logger.warn("Error",e);
					} catch (ExecutionException e) {
						GenSpaceServerFactory.clearCache();
						updateFormFields();
						return;
					}
					DefaultListModel model = new DefaultListModel();
					if(cachedMyNetworks != null)
						for (UserNetwork t : cachedMyNetworks) {
							model.addElement(t);
						}
					lstMyNetworks.setModel(model);
				}

			};
			worker2.execute();
			SwingWorker<List<Network>, Void> worker = new SwingWorker<List<Network>, Void>() {

				@Override
				protected List<Network> doInBackground()
						throws Exception {
					return GenSpaceServerFactory.getNetworkOps().getAllNetworks();
				}

				@Override
				protected void done() {
					try {
						cachedAllNetworks = get();
					} catch (InterruptedException e) {
						GenSpace.logger.warn("Error",e);
					} catch (ExecutionException e) {
						GenSpaceServerFactory.clearCache();
						updateFormFields();
						return;
					}
					Model m = (Model) chooseNetwork.getModel();
					m.data.clear();
					for (Network t : cachedAllNetworks) {
						m.data.add(t.getName());
					}
					chooseNetwork.setText("");
				}

			};
			worker.execute();
		}
	}
}
