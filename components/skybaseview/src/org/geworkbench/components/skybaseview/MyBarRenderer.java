package org.geworkbench.components.skybaseview;

import java.awt.*;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.renderer.category.BarRenderer;

public class MyBarRenderer extends BarRenderer
{
    private String seqcat = null;
    GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, new Color(143, 200, 143), 0.0f, 0.0f, new Color(0, 64, 0));
    GradientPaint gp1 = new GradientPaint(0.0f, 0.0f, new Color(70, 130, 255), 0.0f, 0.0f, new Color(0, 0, 64));
    GradientPaint gp2 = new GradientPaint(0.0f, 0.0f, Color.yellow, 0.0f, 0.0f, new Color(64, 64, 0));

    public Paint getItemPaint(int x_row, int x_col)
    {
	CategoryDataset cds = getPlot().getDataset();
	String l_colKey = (String)cds.getColumnKey(x_col);

	if (l_colKey.equals(seqcat))
	{
	    switch (x_row)
	    {
		case 0: return gp0;
		case 1: return gp1;
		case 2: return gp2;
		default: return Color.white;
	    }
	}
	return getSeriesPaint(x_row);
    }
    public void setcol(String seqid)
    {
	seqcat = seqid;
	getPlot().setRenderer(this);
    }
}
