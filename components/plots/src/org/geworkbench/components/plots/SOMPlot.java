package org.geworkbench.components.plots;

import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.jfree.chart.*;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Columbia University</p>
 * <p/>
 * <code>Canvas</code> on which Marker profile constituting individual markers in
 * SOM clusters are drawn. Each <code>SOMPlot</code> refers to one
 * <code>SOMCluster</code>
 *
 * @author manjunath at genomecenter dot columbia dot edu
 * @version 3.0
 */

public class SOMPlot extends ChartPanel {

    DSItemList<DSGeneMarker> markerStats;

    /**
     * Property used for conveying the origin of a <code>PropertyChange</code>
     * event for distinguishing messages from other components which throw
     * <code>PropertyChange</code> events
     */
    static final String SAVEIMAGE_PROPERTY = "saveSOMClusterImage";

    /**
     * Property used for conveying the origin of a <code>PropertyChange</code>
     * event for distinguishing messages from other components which throw
     * <code>PropertyChange</code> events
     */
    static String SINGLE_MARKER_SELECTED_PROPERTY = "SOMSingleMarkerSelected";

    /**
     * Property used for conveying the origin of a <code>PropertyChange</code>
     * event for distinguishing messages from other components which throw
     * <code>PropertyChange</code> events
     */
    static String MULTIPLE_MARKER_SELECTED_PROPERTY = "SOMMultipleMarkerSelected";


    static String PLOT_MOUSE_CLICKED = "SOMMouseClicked";

    /**
     * Constructor
     *
     * @param parent the widget containing this plot
     */
    public SOMPlot(JFreeChart chart) {
        super(chart, false);
        this.setLayout(new BorderLayout());
        JPopupMenu popup = createPopupMenu(true, false, false, true);
        setPopupMenu(popup);
        this.addChartMouseListener(new ChartMouseListener() {
            public void chartMouseClicked(ChartMouseEvent e) {
                ancillaryMouseClickHandler(e);
            }

            public void chartMouseMoved(ChartMouseEvent e) {

            }
        });
    }

    /**
     * The <code>MicroarraySetView</code> set from the enclosing widget
     *
     * @param chips the view containing the microarraySet to be rendered
     */
    public void setChips(final DSMicroarraySetView<DSGeneMarker, DSMicroarray> chips) {
        if (chips == null) {
            return;
        }
        Thread t = new Thread() {
            public void run() {
                markerStats = chips.markers();
                XYSeriesCollection plots = new XYSeriesCollection();
                int numGenes = (chips.markers().size() > 500) ? 500 : chips.markers().size();
                for (int geneCtr = 0; geneCtr < numGenes; geneCtr++) {
                    XYSeries series = new XYSeries(chips.markers().get(geneCtr).getLabel());
                    for (int maCtr = 0; maCtr < chips.size(); maCtr++) {
                        double value = chips.getValue(geneCtr, maCtr);
                        if (Double.isNaN(value) || value <= 0) {
                            series.add(maCtr, 0);
                        } else {
                            series.add(maCtr, value);
                        }
                    }
                    plots.addSeries(series);
                }
                JFreeChart chart = ChartFactory.createXYLineChart(null, // Title
                        "Experiment", // X-Axis label
                        "Value", // Y-Axis label
                        plots, // Dataset
                        PlotOrientation.VERTICAL, false, // Show legend
                        true, true);
                setChart(chart);
            }
        };
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    public DSItemList<DSGeneMarker> getMarkerInfo() {
        return markerStats;
    }

    public void ancillaryMouseClickHandler(ChartMouseEvent cme) {
        ChartEntity entity = cme.getEntity();
        if (entity != null && entity.getToolTipText() != null) {
            String label = entity.getToolTipText().split(":")[0];
            if (getMarkerInfo() != null) {
                DSGeneMarker marker = getMarkerInfo().get(label);
                firePropertyChange(SINGLE_MARKER_SELECTED_PROPERTY, null, marker);
            }
        }
    }

    @Override public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        firePropertyChange(PLOT_MOUSE_CLICKED, null, this);
    }

    protected void addToPanel_actionPerformed(ActionEvent e) {
        if (getMarkerInfo() != null) {
            firePropertyChange(MULTIPLE_MARKER_SELECTED_PROPERTY, null, getMarkerInfo());
        }
    }

    protected void imageSnapshot_actionPerformed(ActionEvent e) {
        if (getChart() != null) {
            Image image = getChart().createBufferedImage(getWidth(), getHeight());
            if (image != null) {
                firePropertyChange(SAVEIMAGE_PROPERTY, null, image);
            }
        }
    }

    /**
     * Creates a popup menu for the panel.
     *
     * @param properties include a menu item for the chart property editor.
     * @param save       include a menu item for saving the chart.
     * @param print      include a menu item for printing the chart.
     * @param zoom       include menu items for zooming.
     * @return The popup menu.
     */
    protected JPopupMenu createPopupMenu(boolean properties, boolean save, boolean print, boolean zoom) {
        JPopupMenu result = super.createPopupMenu(properties, save, print, zoom);

        result.addSeparator();

        JMenuItem imageSnapshot = new JMenuItem("Image Snapshot");
        imageSnapshot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                imageSnapshot_actionPerformed(e);
            }
        });
        result.add(imageSnapshot);

        JMenuItem addToPanel = new JMenuItem("Add to Panel");
        addToPanel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addToPanel_actionPerformed(e);
            }
        });
        result.add(addToPanel);

        return result;
    }
}
