/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2008) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.gpmodule.classification.svm.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;

/**
 *@author Marc-Danie Nazaire
 *@version $Id$
 */
public class GPTreeModel implements TreeModel
{
    private DSAnnotationContext<?> context;
    private List<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();

    public GPTreeModel()
    {
        context = null;
    }

    public GPTreeModel(DSAnnotationContext<?> context)
    {
        this.context = context;
    }

    public Object getRoot()
    {
        return context;
    }

    public DSAnnotationContext<?> getContext()
    {
        return context;
    }

    public void setContext(DSAnnotationContext<?> context)
    {
        this.context = context;
    }

    public Object getChild(Object parent, int index)
    {
        if(context == null)
            return null;

        if (parent == context)
        {
            return context.getLabel(index);
        }
        else if (parent instanceof String)
        {
            String label = (String) parent;
            DSPanel<?> panel = context.getItemsWithLabel(label);
            return panel.get(index);
        }
        else
        {
            return null;
        }
    }

    public int getIndexOfChild(Object parent, Object child)
    {
        if(context == null)
            return -1;

        if (parent == context)
        {
            return context.indexOfLabel((String) child);
        }
        else if (parent instanceof String)
        {
            String label = (String) parent;
            Object item = child;
            DSPanel<?> panel = context.getItemsWithLabel(label);
            return panel.indexOf(item);
        }
        else
        {
            return 0;
        }
    }

    public int getChildCount(Object parent)
    {
        if(context == null)
            return -1;

        if (parent == context)
        {
            return context.getNumberOfLabels();
        }
        else if (parent instanceof String)
        {
            String label = (String) parent;
            DSPanel<?> panel = context.getItemsWithLabel(label);
            return panel.size();
        }
        else
        {
            return 0;
        }
    }

    public boolean isLeaf(Object node)
    {
        if(context == null)
            return true;

        if (node == context)
        {
            return (context.getNumberOfLabels() == 0);
        }
        else if (node instanceof String)
        {
            String label = (String) node;
            DSPanel<?> panel = context.getItemsWithLabel(label);
            return (panel.size() == 0);
        }
        else
        {
            return true;
        }
    }

    // Tree-wide change
    public void fireTreeStructureChanged()
    {
        TreeModelEvent e = new TreeModelEvent(this, new Object[]{context});
        int len = treeModelListeners.size();
        for (int i = 0; i < len; i++)
        {
            ((TreeModelListener) treeModelListeners.get(i)).treeStructureChanged(e);
        }
    }

    public void valueForPathChanged(TreePath path, Object newValue)
    {
        int len = treeModelListeners.size();
        TreeModelEvent e = new TreeModelEvent(this, new Object[]{(newValue)});

        for (int i = 0; i < len; i++)
        {
            ((TreeModelListener) treeModelListeners.get(i)).treeNodesChanged(e);
        }
    }

    public void fireLabelChanged(String label)
    {
        TreeModelEvent e = new TreeModelEvent(this, new Object[]{context}, new int[]{context.indexOfLabel(label)}, new Object[]{label});
        int len = treeModelListeners.size();
        for (int i = 0; i < len; i++)
        {
            ((TreeModelListener) treeModelListeners.get(i)).treeNodesChanged(e);
        }
    }

    public void addTreeModelListener(TreeModelListener l)
    {
        treeModelListeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l)
    {
        treeModelListeners.remove(l);
    }
}


