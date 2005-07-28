package org.geworkbench.components.plots;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

/**
 * @author John Watkinson
 */
public class SelectableChartPanel extends ChartPanel {

    private Rectangle2D selectRectangle = null;
    private Point selectPoint = null;
    private JPopupMenu popup;
    private Rectangle2D popupRectangle = null;
    private MouseEvent releasedMouseEvent;

    public SelectableChartPanel(JFreeChart jFreeChart) {
        super(jFreeChart);
        popup = new JPopupMenu();
        JMenuItem selectItem = new JMenuItem("Add Selection to Panel", 'P');
        JMenuItem zoomItem = new JMenuItem("Zoom to Selection", 'Z');
        popup.add(selectItem);
        popup.add(zoomItem);
        // Add behavior
        selectItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Selection action: " + popupRectangle);
                repaint();
                // todo - implement
            }
        });
        zoomItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SelectableChartPanel.super.mouseReleased(releasedMouseEvent);
            }
        });
    }

    private Point getPointInRectangle(int x, int y, Rectangle2D area) {
        x = (int) Math.max(Math.ceil(area.getMinX()), Math.min(x, Math.floor(area.getMaxX())));
        y = (int) Math.max(Math.ceil(area.getMinY()), Math.min(y, Math.floor(area.getMaxY())));
        return new Point(x, y);
    }

    @Override public void mousePressed(MouseEvent event) {
        // Maintain a select rectangle
        if (selectRectangle == null) {
            Rectangle2D screenDataArea = getScreenDataArea(event.getX(), event.getY());
            if (screenDataArea != null) {
                selectPoint = getPointInRectangle(event.getX(), event.getY(), screenDataArea);
            } else {
                selectPoint = null;
            }
        }
        // pass through event
        super.mousePressed(event);
    }

    @Override public void mouseDragged(MouseEvent event) {
        // Ignore if popup is already up
        if (!popup.isShowing() && (selectPoint != null)) {
            // selected rectangle shouldn't extend outside the data area...
            Rectangle2D scaledDataArea = getScreenDataArea((int) selectPoint.getX(), (int) selectPoint.getY());
            double xmax = Math.min(event.getX(), scaledDataArea.getMaxX());
            double ymax = Math.min(event.getY(), scaledDataArea.getMaxY());
            selectRectangle = new Rectangle2D.Double(selectPoint.getX(), selectPoint.getY(), xmax - selectPoint.getX(), ymax - selectPoint.getY());
        }
        super.mouseDragged(event);
    }

    @Override public void mouseReleased(MouseEvent event) {
        if (selectRectangle != null) {
            // Save mouse event for later
            releasedMouseEvent = event;
            // Show our popup
            popupRectangle = selectRectangle;
            selectPoint = null;
            selectRectangle = null;
            popup.show(this, event.getX(), event.getY());
        } else {
            selectPoint = null;
            super.mouseReleased(event);
        }
    }
}
