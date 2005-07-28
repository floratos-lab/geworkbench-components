package org.geworkbench.components.discovery.model;

import org.geworkbench.util.patterns.DataSource;
import org.geworkbench.events.HierarchicalProgressEvent;
import org.geworkbench.events.ProgressChangeEvent;
import org.geworkbench.util.patterns.TreePatternSource;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class PatternTreeModel extends DefaultTreeModel implements GenericModel {
    public final static DefaultMutableTreeNode ROOT = new DefaultMutableTreeNode("Hierarchical");

    //a reference to the model
    //  DefaultTreeModel treeModel = null;
    public PatternTreeModel() {
        super(ROOT);
    }

    /**
     * As specified by GenericModel
     */
    public void clear() {
        setRoot(ROOT);
        reload();
    }

    /**
     * As specified by GenericModel
     */
    public boolean attach(DataSource source) {
        if (source instanceof TreePatternSource) {
            clear();
            org.geworkbench.util.patterns.TreePatternSource tSource = (TreePatternSource) source;
            //we pass this model to get populated with a new root.
            //This is very coupled with the data source -- but it works for now!
            tSource.getRoot(this);
            reload();
            return true;
        }
        return false;
    }

    public void progressChanged(org.geworkbench.events.ProgressChangeEvent evt) {
        if (evt instanceof org.geworkbench.events.HierarchicalProgressEvent) {
            org.geworkbench.events.HierarchicalProgressEvent hEvt = (org.geworkbench.events.HierarchicalProgressEvent) evt;
            DefaultMutableTreeNode parent = hEvt.getParent();
            DefaultMutableTreeNode child = hEvt.getChild();
            if (child != null) {
                reload(child.getParent());
                //fire insert node for updating tree view
                int[] newIndexs = new int[1];
                newIndexs[0] = parent.getIndex(child);
                Object[] newChildren = new Object[1];
                newChildren[0] = child;
                fireTreeNodesInserted(this, child.getPath(), newIndexs, newChildren);
            } else {
                reload();
            }
        }
    }
}
