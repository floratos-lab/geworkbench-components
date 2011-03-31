package org.geworkbench.components.somclustering;

import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel; 
import org.jfree.chart.*;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.SymbolicXYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Columbia University</p>
 * <p/>
 * <code>Canvas</code> on which Marker profile constituting individual markers in
 * SOM clusters are drawn. Each <code>SOMPlot</code> refers to one
 * <code>SOMCluster</code>
 *
 * @author manjunath at genomecenter dot columbia dot edu
 * @version $Id$
 */

public class SOMPlot extends ChartPanel {

	private static final long serialVersionUID = 1563687038438582101L;
	
	private DSItemList<DSGeneMarker> markerStats;
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView;
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
    private String titleText;

    /**
     * Constructor
     *
     * @param parent the widget containing this plot
     */
    public SOMPlot(JFreeChart chart) {
        super(chart, false);
        titleText=chart.getTitle().getText();
        chart.setTitle("");
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
            	maSetView = chips;
                markerStats = chips.markers();
                XYSeriesCollection plots = new XYSeriesCollection();
                int numGenes = chips.markers().size();
                for (int geneCtr = 0; geneCtr < numGenes; geneCtr++) {
                    XYSeries series = new XYSeries(chips.markers().get(geneCtr).getLabel());
                    for (int maCtr = 0; maCtr < chips.size(); maCtr++) {
                        double value = chips.getValue(geneCtr, maCtr);
                        if (Double.isNaN(value)) {
                            series.add(maCtr, 0);
                        } else {
                            series.add(maCtr, value);
                        }
                    }
                    plots.addSeries(series);
                }
                
                ExpressionXYToolTip tooltipGenerator = null;

                DSPanel<DSMicroarray> arrays = new CSPanel<DSMicroarray>("");
				arrays.addAll(chips.items());
				DSPanel<DSGeneMarker> genes = new CSPanel<DSGeneMarker>("");
				genes.addAll(chips.markers());
				
				tooltipGenerator = new ExpressionXYToolTip();
				tooltipGenerator.setCurrentGenes(genes, arrays);
				 

				StandardXYItemRenderer renderer = new StandardXYItemRenderer(
						StandardXYItemRenderer.LINES, tooltipGenerator);
				
                JFreeChart chart = ChartFactory.createXYLineChart(null, // Title
                        "Experiment", // X-Axis label
                        "Value", // Y-Axis label
                        plots, // Dataset
                        PlotOrientation.VERTICAL, false, // Show legend
                        true, true);
                
                
                
				chart.getXYPlot().setRenderer(renderer);
				
                if (arrays != null) {
					String[] alist;
					alist = new String[arrays.size()];
					for (int maCtr = 0; maCtr < arrays.size(); maCtr++)
						alist[maCtr] = arrays.get(maCtr).getLabel() + " ";
					chart.getXYPlot().setDomainAxis(
							new SymbolAxis("Cluster "+titleText, alist));
					chart.getXYPlot().getDomainAxis().setVerticalTickLabels(
							true);

				}
                              
                setChart(chart);
            }
        };
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }
    
    
    /**
	 * Tool-tip renderer for gene charts.
	 * 
	 * @author unattributable
	 */
	private class ExpressionXYToolTip extends SymbolicXYItemLabelGenerator {

		private static final long serialVersionUID = 6347518430675084013L;
		
		DSPanel<DSGeneMarker> markers = new CSPanel<DSGeneMarker>("");
		DSPanel<DSMicroarray> arrays = new CSPanel<DSMicroarray>("");

		public void setCurrentGenes(DSPanel<DSGeneMarker> markers,
				DSPanel<DSMicroarray> arrays) {
			this.markers = markers;
			this.arrays = arrays;
		}

		public String generateToolTip(XYDataset data, int series, int item) {

			String tooltip = "";

			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);

			DSGeneMarker marker = markers.get(series);
			DSMicroarray array = arrays.get(item);

			if (marker != null) {
				tooltip += marker.getLabel()+ ": (";
			}
			if (array != null) {
				tooltip +=  array.getLabel() + ", ";
			}

			tooltip +=  nf.format(data.getYValue(series, item)) + ")";

			return tooltip;

		}
	}

    

    public DSItemList<DSGeneMarker> getMarkerInfo() {
        return markerStats;
    }

    public void ancillaryMouseClickHandler(ChartMouseEvent cme) {
        ChartEntity entity = cme.getEntity();
        if ((entity != null) && (entity instanceof XYItemEntity)) {
			XYItemEntity xyEntity = (XYItemEntity) entity;
			int series = xyEntity.getSeriesIndex();
			int item = xyEntity.getItem();

			DSMicroarray array = maSetView.items().get(item);
			DSGeneMarker marker = maSetView.markers().get(series);
            
				 
            firePropertyChange(SINGLE_MARKER_SELECTED_PROPERTY, null, array);
            firePropertyChange(SINGLE_MARKER_SELECTED_PROPERTY, null, marker);
             
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

        JMenuItem addToPanel = new JMenuItem("Add to Set");
        addToPanel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addToPanel_actionPerformed(e);
            }
        });
        result.add(addToPanel);

        return result;
    }
}
