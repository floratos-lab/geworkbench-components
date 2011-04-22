package org.geworkbench.components.selectors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.geworkbench.bison.datastructure.properties.DSSequential;

/**
 * @author John Watkinson
 * @version $Id$
 */
public class SelectorTreeRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -4624114008085733267L;
	
	protected JCheckBox checkBox;
    private JPanel component;
    protected JLabel cellLabel;
    private Color selectionForeground, selectionBackground, textForeground, textBackground;
    private int checkBoxWidth;
    protected SelectorPanel<? extends DSSequential> selectorPanel;

    public SelectorTreeRenderer(SelectorPanel<? extends DSSequential> panel) {
        selectorPanel = panel;
        checkBox = new JCheckBox();
        checkBox.setBackground(Color.WHITE);
        selectionForeground = UIManager.getColor("Tree.selectionForeground");
        selectionBackground = UIManager.getColor("Tree.selectionBackground");
        textForeground = UIManager.getColor("Tree.textForeground");
        textBackground = UIManager.getColor("Tree.textBackground");
        checkBoxWidth = checkBox.getPreferredSize().width;
        component = new JPanel();
        component.setLayout(new BorderLayout());
        component.setBackground(Color.WHITE);
        component.add(checkBox, BorderLayout.WEST);
        cellLabel = new JLabel("");
        cellLabel.setOpaque(true);
        cellLabel.setIconTextGap(0);
        component.add(cellLabel, BorderLayout.CENTER);
        Font fontValue;
        fontValue = UIManager.getFont("Tree.font");
        if (fontValue != null) {
            cellLabel.setFont(fontValue);
        }
    }

    public int getCheckBoxWidth() {
        return checkBoxWidth;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof String) {
            String label = (String) value;
            // Use custom renderer
            String displayLabel = label + " [" + selectorPanel.getContext().getItemsWithLabel(label).size() + "]";
            cellLabel.setText(" " + displayLabel);
            checkBox.setSelected(selectorPanel.getContext().isLabelActive(label));
            if (selected) {
                cellLabel.setForeground(selectionForeground);
                cellLabel.setBackground(selectionBackground);
            } else {
                cellLabel.setForeground(textForeground);
                cellLabel.setBackground(textBackground);
            }
            return component;
        }
        // Root
        if (value == selectorPanel.getContext()) {
            return super.getTreeCellRendererComponent(tree, "", selected, expanded, leaf, row, hasFocus);
        }
        return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }

}
