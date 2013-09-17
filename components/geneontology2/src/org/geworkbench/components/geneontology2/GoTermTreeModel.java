/**
 * 
 */
package org.geworkbench.components.geneontology2;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.geworkbench.bison.datastructure.biocollections.GoAnalysisResult;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GOTerm;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GeneOntologyTree;

/**
 * @author zji
 * @version $Id$
 * 
 */
public class GoTermTreeModel implements TreeModel {
	GoAnalysisResult result = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	@Override
	public Object getRoot() {
		return root;
	}

	private GoTreeNode root = new GoTreeNode(this, new GOTerm(0));

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	@Override
	public Object getChild(Object parent, int index) {
		GoTreeNode node = (GoTreeNode) parent;
		if (node.goTerm.getId() == 0) {
			GeneOntologyTree geneOntologyTree = GeneOntologyTree.getInstance();
			if(geneOntologyTree!=null)
				return new GoTreeNode(this, geneOntologyTree.getRoot(index));
			else
				return new GoTreeNode(this, new GOTerm(-index)); // place holder node
		}

		List<GOTerm> list = Arrays.asList(node.goTerm.getChildren());
		Collections.sort(list, termComparator);
		return new GoTreeNode(this, list.get(index));
	}

	private GoTermComparator termComparator = new GoTermComparator();

	static private class GoTermComparator implements Comparator<GOTerm> {

		@Override
		public int compare(GOTerm o1, GOTerm o2) {
			return o1.getName().compareTo(o2.getName());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(Object parent) {
		GoTreeNode node = (GoTreeNode) parent;
		if (node.goTerm.getId() == 0) {
			GeneOntologyTree geneOntologyTree = GeneOntologyTree.getInstance();
			if(geneOntologyTree!=null)
				return geneOntologyTree.getNumberOfRoots();
			else
				return 0 ;
		}

		return node.goTerm.getChildren().length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	@Override
	public boolean isLeaf(Object node) {
		return getChildCount(node) == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath,
	 * java.lang.Object)
	 */
	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// no-op
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		GoTreeNode node = (GoTreeNode) parent;
		GoTreeNode childNode = (GoTreeNode) child;
		if (node.goTerm.getId() == 0) {
			GeneOntologyTree geneOntologyTree = GeneOntologyTree.getInstanceUntilAvailable();
			for (int i = 0; i < geneOntologyTree.getNumberOfRoots(); i++) {
				if (geneOntologyTree.getRoot(i).equals(childNode.goTerm))
					return i;
			}
			return -1;
		}

		List<GOTerm> list = Arrays.asList(node.goTerm.getChildren());
		Collections.sort(list, termComparator);
		return list.indexOf(childNode.goTerm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.
	 * TreeModelListener)
	 */
	@Override
	public void addTreeModelListener(TreeModelListener l) {
		// no-op
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.
	 * TreeModelListener)
	 */
	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		// no-op
	}

	public void setResult(GoAnalysisResult result) {
		this.result = result;
	}

}
