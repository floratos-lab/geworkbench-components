package org.geworkbench.components.selectors;

import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.util.DSAnnotValue;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * <p>Title: caWorkbench</p>
 * <p/>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype
 * Analysis</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p/>
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 3.0
 */
public class CriterionTreeRenderer extends DefaultTreeCellRenderer {
    PropertyTagSelectorPanel testPanel = null;

    public CriterionTreeRenderer(PropertyTagSelectorPanel testPanel) {
        this.testPanel = testPanel;
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object object = node.getUserObject();
        if (object instanceof DSPanel) {
            // This is one of the criterion values (a final panel)
            DSPanel<DSMicroarray> maVector = (DSPanel<DSMicroarray>) object;
            DSAnnotValue classValue = testPanel.classCriteria.getValue(maVector);
            ImageIcon icon = testPanel.classCriteria.getIcon(classValue);
            if (icon != null) {
                setIcon(icon);
            }
            if (maVector.isActive()) {
                c.setForeground(Color.red);
            } else {
                c.setForeground(Color.black);
            }
        }
        return c;
    }

}
