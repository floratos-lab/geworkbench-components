/**
 * 
 */
package org.geworkbench.components.geneontology2;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.TreeNode;

import org.geworkbench.bison.datastructure.biocollections.GoAnalysisResult;

/**
 * Tree node for GO term tree.
 * @author zji
 * @version $Id: GoTreeNode.java,v 1.1 2009-10-01 16:49:50 jiz Exp $
 *
 */
class GoTreeNode implements TreeNode {
	int goId; 
	private GoTreeNode parent;
	private List<GoTreeNode> children;
	private GoAnalysisResult result;
	
	
	GoTreeNode(final GoAnalysisResult result, int goId, GoTreeNode parent) {
		this.result = result;
		
		this.goId = goId;
		this.parent = parent;
		this.children = null;
	}

	public void removeAllChildren() {
    	children = null;
    }

	GoTreeNode() {
		goId = 0;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(goId==0)return "ROOT"; 

		if(result==null || result.getRowAsString(goId)==null) {// not in the result 
			return GoAnalysisResult.getGoTermName(goId); 
		} else { // in the result from ontologizer
			return result.getRowAsString(goId);
		}
	}
	
	public void add(GoTreeNode goTreeNode) {
		goTreeNode.parent = this;
		if(children==null) {
			children = new ArrayList<GoTreeNode>();
		}
		// keep sorted
		for(int index=0; index<children.size(); index++) {
			GoTreeNode node = children.get(index);
			String name = GoAnalysisResult.getGoTermName(node.goId); 
			String newNodeName = GoAnalysisResult.getGoTermName(goTreeNode.goId); 
			if(newNodeName.compareTo(name)<0) {
				children.add(index, goTreeNode);
				return;
			}
		}
		children.add(goTreeNode); // if larger than everyone, add the last
		
	}

	public Enumeration<GoTreeNode> children() {
		Vector<GoTreeNode> v = new Vector<GoTreeNode>(children);
		return v.elements();
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public TreeNode getChildAt(int childIndex) {
		if(children==null)return null;
		return children.get(childIndex);
	}

	public int getChildCount() {
		if(children==null)return 0;
		return children.size();
	}

	public int getIndex(TreeNode node) {
		if(children==null)return -1;
		return children.indexOf(node); // return -1 if not contain
	}

	public TreeNode getParent() {
		return parent;
	}

	public boolean isLeaf() {
		if(getChildCount()==0)return true;
		else return false;
	}
}