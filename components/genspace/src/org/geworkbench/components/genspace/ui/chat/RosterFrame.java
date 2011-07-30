/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * RosterFrame.java
 *
 * Created on Jul 11, 2009, 2:23:51 PM
 */

package org.geworkbench.components.genspace.ui.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.chat.ChatReceiver;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;

/**
 * A RosterFrame displays roster (buddy list) information
 * 
 * @author jsb2125
 */
public class RosterFrame extends javax.swing.JFrame implements RosterListener {
	public static Set<String> removedCache = new HashSet<String>();
	public void refresh()
	{
		((RosterModel) rosterTree.getModel()).clear();
		ChatReceiver.connection.getRoster().reload();
		roster.reload();
	}
	private static final long serialVersionUID = 7609367478611608296L;
	private class RosterModel implements TreeModel{
		private Roster roster;
		private ArrayList<RosterGroup> rootGroups;
		private HashMap<RosterGroup, ArrayList<RosterEntry>> children;
		public RosterModel() {
			clear();
		}
	
		public void clear() {
			roster = null;
			rootGroups = null;
			children = null;
		}

		public void setData(Roster roster) {
			this.roster = roster;
			rootGroups = new ArrayList<RosterGroup>();
			children = new HashMap<RosterGroup, ArrayList<RosterEntry>>();
			

			for(RosterGroup g : roster.getGroups())
			{
				rootGroups.add(g);
				children.put(g, new ArrayList<RosterEntry>());
				for(RosterEntry e : g.getEntries())
				{
					if(! e.getUser().equalsIgnoreCase(GenSpaceServerFactory.getUser().getUsername() + "@genspace") && !(removedCache.contains(e.getUser()) && g.getName().equals("Friends")))
						children.get(g).add(e);
				}
				if(children.get(g).size() == 0)
				{
					children.remove(g);
					rootGroups.remove(g);
				}
				if(g != null && children != null && children.get(g) != null)
					Collections.sort(children.get(g),new Comparator<RosterEntry>() {
	
						@Override
						public int compare(RosterEntry o1, RosterEntry o2) {
							return o1.getName().compareTo(o2.getName());
						}
						
					});
			}
			
			Collections.sort(rootGroups, new Comparator<RosterGroup>() {

				@Override
				public int compare(RosterGroup l, RosterGroup r) {
					if(l.getName().equals("Friends"))
						return -1;
					else
						return l.getName().compareTo(r.getName());
				}
				
		
			});
		}

		@Override
		public void addTreeModelListener(TreeModelListener l) {			
		}
		
		@Override
		public Object getChild(Object parent, int index) {
			if(parent instanceof RosterGroup)
			{
				RosterGroup g = (RosterGroup) parent;
				return children.get(g).get(index);
			}
			else if(parent instanceof Roster)
			{
				return rootGroups.get(index);
			}
			return roster;
		}

		@Override
		public int getChildCount(Object parent) {

			if(parent instanceof RosterGroup)
			{
				RosterGroup g = (RosterGroup) parent;
				if(children == null || children.get(g) == null)
					return 0;
				return children.get(g).size();	
			}
			else if(parent instanceof Roster)
			{
				return rootGroups.size();
			}
			return 0;
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			if(parent instanceof RosterGroup)
			{
				RosterGroup g = (RosterGroup) parent;
				return children.get(g).indexOf(child);
			}
			else if(parent instanceof Roster)
			{
				return rootGroups.indexOf(child);
			}
			return 0;
		}

		@Override
		public Object getRoot() {
			return roster;
		}

		@Override
		public boolean isLeaf(Object node) {
			return (node instanceof RosterEntry);
		}

		@Override
		public void removeTreeModelListener(TreeModelListener l) {
			// No-Op
			
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
			// No-Op
			
		}
	}


	@Override
	public void entriesAdded(Collection<String> r) {
		this.setRoster(ChatReceiver.connection.getRoster());
		rosterTree.repaint();
	}

	@Override
	public void entriesDeleted(Collection<String> r) {
		this.setRoster(ChatReceiver.connection.getRoster());
		rosterTree.repaint();
	}

	@Override
	public void entriesUpdated(Collection<String> r) {
		this.setRoster(ChatReceiver.connection.getRoster());
		rosterTree.repaint();
	}

	@Override
	public void presenceChanged(Presence p) {
		rosterTree.repaint();

	}
	Roster roster;
	/**
	 * Update the roster
	 * 
	 * @param newr
	 *            new Roster
	 */
	public void setRoster(Roster newr) {
		roster = newr;
		roster.addRosterListener(this);

		RosterModel model = new RosterModel();
		model.setData(roster);
		rosterTree.setModel(model);
		for(int i =0;i<=rosterTree.getRowCount();i++)
		{
			rosterTree.expandRow(i);
		}
		repaint();
	}

	public void setAvailable()
	{
		Presence pr = new Presence(Presence.Type.available);
		pr.setStatus("On genSpace...");
		ChatReceiver.connection.sendPacket(pr);
		cmbStatus.setSelectedIndex(0);
	}
	/** Creates new form RosterFrame */
	public RosterFrame() {
		initComponents();
		addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				setAvailable();
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				Presence pr = new Presence(Presence.Type.unavailable);
				pr.setStatus("genSpace hidden");
				ChatReceiver.connection.sendPacket(pr);
				cmbStatus.setSelectedIndex(2);
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
	}

	/**
	 * Change the current presence
	 * 
	 * @param e
	 */
	private void cmbStatusActionPerformed(ActionEvent e) {
		String status = cmbStatus.getSelectedItem().toString();
		Presence pr;
		if (status.equals("Available")) {
			pr = new Presence(Presence.Type.available);
			pr.setMode(Presence.Mode.available);
		} else if (status.equals("Away")) {
			pr = new Presence(Presence.Type.available);
			pr.setMode(Presence.Mode.away);
		} else
			pr = new Presence(Presence.Type.unavailable);
		pr.setStatus(cmbStatus.getSelectedItem().toString());
		ChatReceiver.connection.sendPacket(pr);

	};

	JScrollPane jScrollPane1;
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		lblStatus = new javax.swing.JLabel();
		jScrollPane1 = new javax.swing.JScrollPane();

		String[] statuses = { "Available", "Away", "Offline" };
		cmbStatus = new javax.swing.JComboBox(statuses);
		cmbStatus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				cmbStatusActionPerformed(e);
			}

		});

		jMenuBar1 = new javax.swing.JMenuBar();
		jMenu1 = new javax.swing.JMenu();
		jMenu2 = new javax.swing.JMenu();

		lblStatus.setText("YourStatus:");

		jMenu1.setText("File");
		jMenuBar1.add(jMenu1);

		jMenu2.setText("Edit");
		jMenuBar1.add(jMenu2);

//		setJMenuBar(jMenuBar1);
		setTitle("Buddies");
		rosterTree = new JTree();
		rosterTree.setRootVisible(false);
		rosterTree.setUI(new BasicTreeUI(){
			@Override
	        protected AbstractLayoutCache.NodeDimensions createNodeDimensions() {
	            return new NodeDimensionsHandler() {
	                @Override
	                public Rectangle getNodeDimensions(
	                        Object value, int row, int depth, boolean expanded,
	                        Rectangle size) {
	                    Rectangle dimensions = super.getNodeDimensions(value, row,
	                            depth, expanded, size);
	                    dimensions.width =
	                            getWidth() - 5;
	                    Rectangle d2 = dimensions;
	                    d2.x = 0;
	                    return d2;
	                }
	            };
	        }
			
			   protected void paintExpandControl(Graphics g, Rectangle clipBounds,
	                    Insets insets, Rectangle bounds, TreePath path, int row,
	                    boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
	                Object value = path.getLastPathComponent();

	                // Draw icons if not a leaf and either hasn't been loaded,
	                // or the model child count is > 0.
	                if (!isLeaf
	                        && (treeModel.getChildCount(value) > 0)) {
	                    int middleXOfKnob;
	                    middleXOfKnob = bounds.width - expandedIcon.getIconWidth();
	                    
	                    int middleYOfKnob = bounds.y + (bounds.height / 2);

	                    if (isExpanded) {
	                        Icon expandedIcon = getExpandedIcon();
	                        if (expandedIcon != null)
	                            drawCentered(tree, g, expandedIcon, middleXOfKnob,
	                                    middleYOfKnob);
	                    } else {
	                        Icon collapsedIcon = getCollapsedIcon();
	                        if (collapsedIcon != null)
	                            drawCentered(tree, g, collapsedIcon, middleXOfKnob,
	                                    middleYOfKnob);
	                    }
	                }
	            }

		});
		rosterTree.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// No-Op
				
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
				// No-Op
				
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				// No-Op
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				// No-Op
				
			}
			
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					// Want to chat with this person
					Object o = rosterTree.getSelectionPath().getLastPathComponent();
					if(o instanceof RosterEntry)
					{
						RosterEntry e = (RosterEntry) o;
						if(e != null)
							ChatReceiver.manager.createChat(
								e.getUser(), null);
					}
				}
			}
		});
		rosterTree.setCellRenderer(new TreeCellRenderer() {

			@Override
			public Component getTreeCellRendererComponent(JTree tree,
                    Object o,
                    boolean selected,
                    boolean expanded,
                    boolean leaf,
                    int row,
                    boolean hasFocus) {

				JLabel l = new JLabel();
				if(o instanceof RosterEntry)
				{
					RosterEntry entry = (RosterEntry) o;
					Presence p = roster.getPresence(entry.getUser());
//					System.out.println(entry.getName() + " -> " + p.getStatus());
					if (p.getType().equals(Presence.Type.unavailable)) {
						// User is unavailable
						l.setForeground(new Color(153, 153, 153));
					} else {
						if (p.getMode() != null
								&& (p.getMode().equals(Mode.away) || p.getMode()
										.equals(Mode.dnd)))
							l.setForeground(new Color(154, 0, 0));
						else
							l.setForeground(new Color(44, 119, 0));
					}
					if(selected)
					{
						l.setBackground(new Color(232,242,254));
						l.setOpaque(true);
					}
					l.setText((entry.getName().length() > 1 ? entry.getName() : entry.getUser().replace("@genspace", "")));
				}
				else if(o instanceof RosterGroup)
				{
					RosterGroup g = (RosterGroup) o;
					l.setText(" " + g.getName() + (g.getName().equals("Friends") ? "" : " Network"));
					l.setBackground(new Color(29, 125, 223));
					l.setForeground(new Color(255,255,255));
					l.setFont(l.getFont().deriveFont(Font.BOLD));
					l.setOpaque(true);
				}
				else
				{
					l.setText("Contacts");
				}
				return l;
				
			}
		});

		jScrollPane1.setViewportView(rosterTree);
		getContentPane().setLayout(new BorderLayout());
		this.add(jScrollPane1,BorderLayout.CENTER);
		this.add(cmbStatus,BorderLayout.SOUTH);





		pack();
	}// </editor-fold>//GEN-END:initComponents

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JMenu jMenu1;
	private javax.swing.JMenu jMenu2;
	private javax.swing.JMenuBar jMenuBar1;
	private javax.swing.JLabel lblStatus;
	private JTree rosterTree;
	private javax.swing.JComboBox cmbStatus;
	// End of variables declaration//GEN-END:variables

}
