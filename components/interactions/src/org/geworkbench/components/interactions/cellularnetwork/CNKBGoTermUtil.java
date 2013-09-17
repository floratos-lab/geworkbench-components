/**
 * 
 */
package org.geworkbench.components.interactions.cellularnetwork;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GOTerm;

/**
 * @author zji
 * @version $Id$
 *
 */
public class CNKBGoTermUtil {

	/**
	 * Create a TreeNode from the Go Term data.
	 * 
	 * @param set
	 * @return
	 */
	private static DefaultMutableTreeNode createNodes(Set<GOTerm> set) {
		DefaultMutableTreeNode node = null;

		for (GOTerm term : set) {
			if ((term.getParents() == null) || (term.getParents().length == 0)) {
				node = new DefaultMutableTreeNode(term);
				Set<DefaultMutableTreeNode> childrenNodeSet = getChildrenNodes(
						term, set);
				for (DefaultMutableTreeNode childrenNode : childrenNodeSet)
					node.add(childrenNode);
				break;
			}
		}

		return node;
	}

	private static Set<DefaultMutableTreeNode> getChildrenNodes(GOTerm parent,
			Set<GOTerm> set) {

		Set<DefaultMutableTreeNode> childrenNodeSet = new HashSet<DefaultMutableTreeNode>();

		for (GOTerm term : set) {
			GOTerm[] parentList = term.getParents();
			boolean isChildNode = false;
			for (GOTerm aParent : parentList) {
				if (aParent.getId() == parent.getId()) {
					isChildNode = true;
					break;
				}
			}

			if (isChildNode) {
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(
						term);
				Set<DefaultMutableTreeNode> grandChildrenNodeSet = getChildrenNodes(
						term, set);
				for (DefaultMutableTreeNode grandChildrenNode : grandChildrenNodeSet)
					childNode.add(grandChildrenNode);
				childrenNodeSet.add(childNode);

			}

		}

		return childrenNodeSet;
	}

	/**
	 * Display Go Tree.
	 * 
	 * @param set
	 */
	public static void displayGoTree(Set<GOTerm> set, CellularNetworkKnowledgeWidget cnbkWidget) {
		if (set == null || set.size() == 0) {
			return;
		}

		Frame frame = JOptionPane.getFrameForComponent(cnbkWidget);
		JDialog goDialog = new JDialog(frame, "Display Gene Ontology Tree", true);

		// Create a tree that allows one selection at a time.
		DefaultMutableTreeNode node = createNodes(set);
		JTree tree = new JTree(node);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		expandAll(tree);

		// Make sure the last node is selected.
		tree.scrollPathToVisible(new TreePath(node));
		tree.setSelectionPath(new TreePath(node.getPath()));

		// Create the scroll pane and add the tree to it.
		JScrollPane treeView = new JScrollPane(tree);
		goDialog.getContentPane().add(treeView);
		goDialog.setMinimumSize(new Dimension(100, 100));
		goDialog.setPreferredSize(new Dimension(300, 300));
		goDialog.pack();
		goDialog.setLocationRelativeTo(null);
		goDialog.setVisible(true);
	}

	/* expand entire single gene tree */
	private static void expandAll(JTree tree) {
		int row = 0;
		while (row < tree.getRowCount()) {
			tree.expandRow(row);
			row++;
		}
	}

}
