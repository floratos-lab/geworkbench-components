package org.geworkbench.components.plots;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * Cell rendered for the lists that handles the special selection conditions.
 */
class CellRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = -33685131076219308L;

	private final ChartGroup group;
	public CellRenderer(ChartGroup group) {
		this.group = group;
	}

	@Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);    //To change body of overridden methods use File | Settings | File Templates.
        String labelToMatch = label.getText().trim();
        if(group.charts.size() > 0){
        	// Check for x-axis
        	String xLabel = group.charts.get(0).chartData.getXLabel().trim();
        	if(labelToMatch.trim().startsWith(xLabel)){
        		// Color the label to indicate that it is on the x-axis of the chart.
                label.setBackground(Color.BLACK);
                label.setForeground(Color.WHITE);
        	} else {            	
            	// Check for y-axis
            	for(Chart c: group.charts){
            		String yLabel = c.chartData.getYLabel().trim();
            		if(labelToMatch.trim().startsWith(yLabel)){
            			// Color the label to indicate that there is a chart.
                        label.setBackground(Color.LIGHT_GRAY);
            		}
            	}                   
        	}
        }               
        return label;
    }
}