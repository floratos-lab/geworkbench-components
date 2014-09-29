package org.geworkbench.components.plots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.JAutoList;
import org.geworkbench.util.PrintUtils;
import org.geworkbench.util.pathwaydecoder.RankSorter;
import org.geworkbench.util.visualproperties.PanelVisualProperties;
import org.geworkbench.util.visualproperties.PanelVisualPropertiesManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.SeriesException;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Scatter Plot component.
 *
 * @author John Watkinson
 * @version $Id$
 */
@AcceptTypes({DSMicroarraySet.class})
public class ScatterPlot implements VisualPlugin {

    /**
     * Maximum number of charts that can be viewed at once.
     */
    private static final int MAXIMUM_CHARTS = 6;

    /**
     * The two types of plots.
     * <ul>
     * <li>ARRAY - Plots the values of each marker for two experiments.
     * <li>MARKER - Plots the values of two markers for each experiment.
     * </ul>
     */
    public enum PlotType {
        ARRAY, MARKER
    }

    private static final int TAB_ARRAY = 0;
    // private static final int TAB_MARKER = 1;

    /**
     * ListModel for the marker list.
     */
    private class MarkerListModel extends AbstractListModel {
		private static final long serialVersionUID = -2709192821511189399L;

		public int getSize() {
            if (dataSetView.getMicroarraySet() == null) {
                return 0;
            }

			DSPanel<DSGeneMarker> mp = dataSetView.getMarkerPanel();
			if ((mp != null) && (mp.size() > 0)) {
				return mp.size();
			} else {
				return dataSetView.allMarkers().size();
			}
        }

        public Object getElementAt(int index) {
            if (dataSetView.getMicroarraySet() == null) {
                return null;
            }

			DSPanel<DSGeneMarker> mp = dataSetView.getMarkerPanel();
			if ((mp != null) && (mp.size() > 0) && (index < mp.size())) {
				return mp.get(index);
			} else {
				return dataSetView.allMarkers().get(index);
			}
        }

        /**
         * Indicates to the associated JList that the contents need to be redrawn.
         */
        public void refresh() {
            if (dataSetView.getMicroarraySet() == null) {
                fireContentsChanged(this, 0, 0);
            } else {
           		fireContentsChanged(this, 0, dataSetView.getMarkerPanel().size() - 1);
            }
        }

    }

    /**
     * ListModel for the microarray list.
     */
    private class MicroarrayListModel extends AbstractListModel {
		private static final long serialVersionUID = 1508449468167888966L;

		public int getSize() {
            if (dataSetView.getMicroarraySet() == null) {
                return 0;
            }

           	DSPanel<DSMicroarray> ap = dataSetView.getItemPanel();
           	if((ap != null) && (ap.size() > 0)){
           		return ap.size();
           	} else {
           		return dataSetView.size();
           	}
        }

        public Object getElementAt(int index) {
            if (dataSetView.getMicroarraySet() == null) {
                return null;
            }

           	DSPanel<DSMicroarray> ap = dataSetView.getItemPanel();
           	if((ap != null) && (ap.size() > 0) && (index < ap.size())){
           		return ap.get(index).getLabel();
           	} else {
           		return dataSetView.get(index);
           	}
        }

        /**
         * Indicates to the associated JList that the contents need to be redrawn.
         */
        public void refresh() {        	
            if (dataSetView.getMicroarraySet() == null) {
                fireContentsChanged(this, 0, 0);
            } else {
           		fireContentsChanged(this, 0, dataSetView.getItemPanel().size() - 1);
            }
        }

    }

    /**
     * The marker JAutoList type.
     */
    private class MarkerAutoList extends JAutoList {
		private static final long serialVersionUID = -2486840260492993013L;

		public MarkerAutoList() {
            super(markerModel);
        }

        @Override protected void elementClicked(int index, MouseEvent e) {
            itemClicked(PlotType.MARKER, index);
            markerModel.refresh();
        }

        @Override protected void elementRightClicked(int index, MouseEvent e) {
            itemRightClicked(PlotType.MARKER, e, index);
        }
    }

    /**
     * The microarray JAutoList type.
     */
    private class MicroarrayAutoList extends JAutoList {
		private static final long serialVersionUID = 2260567334770861847L;

		public MicroarrayAutoList() {
            super(microarrayModel);
        }

        @Override protected void elementClicked(int index, MouseEvent e) {
            itemClicked(PlotType.ARRAY, index);
            microarrayModel.refresh();
        }

        @Override protected void elementRightClicked(int index, MouseEvent e) {
            itemRightClicked(PlotType.ARRAY, e, index);
        }

    }

    /**
     * Struct for the objects associated with a chart.
     */
    private class Chart {

        public Chart() {
            chartData = new ChartData();
        }

        public int index;
        public ChartPanel panel;
        public ChartData chartData;
    }

    /**
     * Struct for a group of charts.
     */
    private static class ChartGroup {
        public ChartGroup() {
            charts = new ArrayList<Chart>();
        }

        public int xIndex = -1;
        public ArrayList<Chart> charts;
        public boolean referenceLineEnabled = true;
        public double slope = 1.0;
        public XYLineAnnotation lineAnnotation = getXYLineAnnotation(slope);
    }

    /**
     * Cell rendered for the lists that handles the special selection conditions.
     */
    private class CellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = -33685131076219308L;

		@Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);    //To change body of overridden methods use File | Settings | File Templates.
            String labelToMatch = label.getText().trim();
            ChartGroup group;
            if (list == microarrayList.getList()) {
                group = chartGroups.get(PlotType.ARRAY);
            } else {
                group = chartGroups.get(PlotType.MARKER);
            }
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

    /**
     * Responsible for handling microarray selection in a gene scatter plot.
     */
    private class GeneChartMouseListener implements ChartMouseListener {

        private ChartData chartData;

        public GeneChartMouseListener(ChartData data) {
            this.chartData = data;
        }

        public void chartMouseClicked(ChartMouseEvent event) {
            // Currently a no-op, but could select the microarray
            ChartEntity entity = event.getEntity();
            if ((entity != null) && (entity instanceof XYItemEntity)) {
                XYItemEntity xyEntity = (XYItemEntity) entity;
                int series = xyEntity.getSeriesIndex();
                int item = xyEntity.getItem();
                DSMicroarray microarray = chartData.getMicroarray(series, item);
                PhenotypeSelectedEvent pse = new PhenotypeSelectedEvent(microarray);
                publishPhenotypeSelectedEvent(pse);
            }
        }

        public void chartMouseMoved(ChartMouseEvent event) {
            // No-op
        }
    }

    /**
     * Responsible for handling marker selection in a microarray scatter plot.
     */
    private class MicroarrayChartMouseListener implements ChartMouseListener {

        private ChartData chartData;

        public MicroarrayChartMouseListener(ChartData data) {
            this.chartData = data;
        }

        public void chartMouseClicked(ChartMouseEvent event) {
            ChartEntity entity = event.getEntity();
            if ((entity != null) && (entity instanceof XYItemEntity)) {
                XYItemEntity xyEntity = (XYItemEntity) entity;
                int series = xyEntity.getSeriesIndex();
                int item = xyEntity.getItem();
                DSGeneMarker marker = chartData.getMarker(series, item);
                if (marker != null) {
                    MarkerSelectedEvent mse = new org.geworkbench.events.MarkerSelectedEvent(marker);
                    publishMarkerSelectedEvent(mse);
                }
            }
        }

        public void chartMouseMoved(ChartMouseEvent event) {
            // No-op
        }
    }

    private class ChartData {
        private ArrayList<ArrayList<org.geworkbench.util.pathwaydecoder.RankSorter>> xyPoints;
        private String xLabel = "";
        private String yLabel = "";

        public ChartData() {
        }

        public void setXyPoints(ArrayList<ArrayList<RankSorter>> xyPoints) {
            this.xyPoints = xyPoints;
        }

        public DSGeneMarker getMarker(int series, int item) {
            ArrayList<RankSorter> list = xyPoints.get(series);
            org.geworkbench.util.pathwaydecoder.RankSorter rs = (RankSorter) list.get(item);
            DSMicroarraySet maSet = (DSMicroarraySet) dataSetView.getDataSet();
            if (maSet != null) {
                if (rs.id < maSet.getMarkers().size()) {
                    DSGeneMarker marker = maSet.getMarkers().get(rs.id);
                    return marker;
                }
            }
            return null;
        }

        public DSMicroarray getMicroarray(int series, int item) {
            ArrayList<RankSorter> list = xyPoints.get(series);
            RankSorter rs = (org.geworkbench.util.pathwaydecoder.RankSorter) list.get(item);
            DSMicroarraySet maSet = (DSMicroarraySet) dataSetView.getDataSet();
            if (maSet != null) {
                if (rs.id < maSet.size()) {
                    DSMicroarray ma = maSet.get(rs.id);
                    return ma;
                }
            }
            return null;
        }

        public org.geworkbench.util.pathwaydecoder.RankSorter getRankSorter(int series, int item) {
            ArrayList<RankSorter> list = xyPoints.get(series);
            RankSorter rs = (RankSorter) list.get(item);
            return rs;
        }
        
        public String getXLabel(){
        	return this.xLabel;
        }
        
        public void setXLabel(String x){
        	this.xLabel = x;
        }
        
        public String getYLabel(){
        	return this.yLabel;
        }
        
        public void setYLabel(String y){
        	this.yLabel = y;
        }
    }

    /**
     * Tool-tip renderer for gene charts.
     */
    private class GeneXYToolTip extends StandardXYToolTipGenerator {
		private static final long serialVersionUID = 3928716706611595907L;
		
		private ChartData chartData;

        public GeneXYToolTip(ChartData data) {
            this.chartData = data;
        }

        public String generateToolTip(XYDataset data, int series, int item) {
            String result = "Unknown: ";
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2);

            DSMicroarray ma = chartData.getMicroarray(series, item);
            if (ma != null) {
                org.geworkbench.util.pathwaydecoder.RankSorter rs = chartData.getRankSorter(series, item);
                DSMicroarraySet maSet = (DSMicroarraySet) dataSetView.getDataSet();
                DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(maSet);
                String xLabel = nf.format(rs.x);
                String yLabel = nf.format(rs.y);
                String[] labels = context.getLabelsForItem(ma);

                if (labels.length > 0) {
                    result = ma.getLabel() + ": " + labels[0] + " [" + xLabel + "," + yLabel + "]";
                } else {
                    result = ma.getLabel() + ": " + "No Panel, " + " [" + xLabel + "," + yLabel + "]";
                }
                return result;
            } else {
                return "";
            }
        }
    }

    /**
     * Tool-tip renderer for microarray charts.
     */
    private class MicroarrayXYToolTip extends StandardXYToolTipGenerator {
		private static final long serialVersionUID = -896282253416405020L;
		
		private ChartData chartData;
        private ChartPanel chartPanel;
        private Rectangle2D shapeBound;
        private XYPlot xyPlot;
        
        public MicroarrayXYToolTip(ChartData data, ChartPanel chartPanel, XYPlot xyPlot) {
            this.chartData = data;
            this.chartPanel = chartPanel;
            this.xyPlot = xyPlot;
            shapeBound = null;
        }

        public String generateToolTip(XYDataset data, int series, int item) {
        	/*
			 * because this customized tooltip needs to know the chart panel
			 * size, it is NOT supposed to called in any case when chartPanel is
			 * null - which was OK for a more regular tool tip.
			 */
			if (chartPanel == null) {
				throw new RuntimeException("tooltip error: no ChartPanel");
				}
			/* this customized tooltip also needs to know the the symbol shape */
			if (shapeBound == null){
				throw new RuntimeException("tooltip error: shape bound unknown");}
        	
            ValueAxis domainAxis = xyPlot.getDomainAxis();
            ValueAxis rangeAxis = xyPlot.getRangeAxis();
            double rangeLower = rangeAxis.getLowerBound();
            double rangeHeight = rangeAxis.getUpperBound() - rangeLower;
            double domainLower = domainAxis.getLowerBound();
            double domainWidth = domainAxis.getUpperBound() - domainLower;

           	/*
			 * generateToolTip should be called only when the chartPanel is not
			 * null
			 */
			Rectangle2D area = chartPanel.getScreenDataArea();

			org.geworkbench.util.pathwaydecoder.RankSorter rs = chartData
					.getRankSorter(series, item);
			double x0 = (rs.x - domainLower) / domainWidth * area.getWidth()
					- 0.5 * shapeBound.getWidth();
			double y0 = (rs.y - rangeLower) / rangeHeight * area.getHeight()
					- 0.5 * shapeBound.getHeight();
			Rectangle2D bound = new Rectangle2D.Double(x0, y0, shapeBound
					.getWidth(), shapeBound.getHeight());

			DSPanel<DSGeneMarker> panel = dataSetView.getMarkerPanel();

			DSMicroarraySet maSet = (DSMicroarraySet) dataSetView
			.getDataSet();
			if (maSet == null)
				return ""; /*
								 * only process if rankSorter is interesting.
								 * Behavior copied from method
								 * chartData.getMarker(series, item)
								 */
			
			StringBuilder sb = new StringBuilder("<html>");
			int count = 0;
			final int MAXIMUM_NUMBER_FOR_SMART_TOOLTIP = 100;
			final int MAXIMUM_ITEMS_IN_A_TOOLTIP = 10;
			for (ArrayList<RankSorter> list : chartData.xyPoints) {

				/* If a list has too many points, use the simple tooltip to avoid slow response. */
				if (list.size() > MAXIMUM_NUMBER_FOR_SMART_TOOLTIP) {
					DSGeneMarker marker = chartData.getMarker(series, item);
					String label = "No Panel, ";
					DSPanel<DSGeneMarker> value = null;
					if(panel!=null)
						value = panel.getPanel(marker);
					if (value != null)
						label = value.getLabel();
					return tooltipForMarker(marker, label, rs);
				}

				for (org.geworkbench.util.pathwaydecoder.RankSorter rankSorter : list) {
					if (rankSorter.id < maSet.getMarkers().size()) {
						double x = (rankSorter.x - domainLower) / domainWidth
								* area.getWidth();
						double y = (rankSorter.y - rangeLower) / rangeHeight
								* area.getHeight();
						if (bound.contains(new Point2D.Double(x, y))) {
							DSGeneMarker markerNear = maSet.getMarkers().get(
									rankSorter.id);
							String label = "No Panel, ";
							DSPanel<DSGeneMarker> value = null;
							if (panel != null)
								value = panel.getPanel(markerNear);
							if (value != null)
								label = value.getLabel();
							count++;
							if(count>MAXIMUM_ITEMS_IN_A_TOOLTIP) /* If there are too many points for this tool tip, show the first group.*/
								return sb.append("...</html>").toString();
							sb.append(
									tooltipForMarker(markerNear, label,
											rankSorter)).append("<br>");
						}
					}
				}
			}
			return sb.append("</html>").toString();
        }
        
        private String tooltipForMarker(DSGeneMarker marker, String label, RankSorter rs) {
        	return marker.getLabel() + ": " + label + " [" + rs.x + "," + rs.y + "]";
        }

		public void setShapeBound(Rectangle2D bound) {
			shapeBound = bound;
		}
    }

	private static class JFreeChartProperties{
		
    	private Boolean antiAlias = null;
    	private Paint backgroundPaint = null;
    		
    	private TextTitle textTitle = null;
    		
    	private Boolean  domainAxisLineVisible   = null;
    	private Boolean  domainTickLabelsVisible = null;
    	private Boolean  domainTickMarksVisible  = null;
    	private Boolean  domainVisible           = null;
    
    	private Boolean  rangeAxisLineVisible   = null;
    	private Boolean  rangeTickLabelsVisible = null;
    	private Boolean  rangeTickMarksVisible  = null;
    	private Boolean  rangeVisible           = null;
    		
    	private Paint paint = null;
    	private PlotOrientation plotOrientation = null;
    		
    	private Boolean  domainCrosshairLockedOnData     = null;
    	private Boolean  domainCrosshairVisible          = null; 
    	private Boolean  domainGridlinesVisible          = null; 
    		
    	private Boolean  rangeCrosshairLockedOnData      = null; 
    	private Boolean  rangeCrosshairVisible           = null;
    	private Boolean  rangeGridlinesVisible           = null;
    	private Boolean  rangeZeroBaselineVisible        = null;
    		
    	public JFreeChartProperties(JFreeChart jFreeChartOriginal) {
    
	    	antiAlias = jFreeChartOriginal.getAntiAlias();
	    	backgroundPaint = jFreeChartOriginal.getBackgroundPaint();
	    			
	    	textTitle = jFreeChartOriginal.getTitle();
	    		
	    	XYPlot xyPlot = (XYPlot)jFreeChartOriginal.getPlot();
	    	ValueAxis domainAxis  = xyPlot.getDomainAxis();
	    	domainAxisLineVisible   = domainAxis.isAxisLineVisible();
	    	domainTickLabelsVisible = domainAxis.isTickLabelsVisible();// 5
	    	domainTickMarksVisible  = domainAxis.isTickMarksVisible();
	    	domainVisible           = domainAxis.isVisible();
	    
	    	ValueAxis rangeAxis = xyPlot.getRangeAxis();
	    	rangeAxisLineVisible   = rangeAxis.isAxisLineVisible();
	    	rangeTickLabelsVisible = rangeAxis.isTickLabelsVisible();
	    	rangeTickMarksVisible  = rangeAxis.isTickMarksVisible();
	    	rangeVisible           = rangeAxis.isVisible();
	    		
	    	paint = xyPlot.getBackgroundPaint();
	    	plotOrientation =  xyPlot.getOrientation();
	    		
	    	domainCrosshairLockedOnData     = xyPlot.isDomainCrosshairLockedOnData();
	    	domainCrosshairVisible          = xyPlot.isDomainCrosshairVisible(); 
	    	domainGridlinesVisible          = xyPlot.isDomainGridlinesVisible(); 
	    		
	    	rangeCrosshairLockedOnData      = xyPlot.isRangeCrosshairLockedOnData(); 
	    	rangeCrosshairVisible           = xyPlot.isRangeCrosshairVisible() ;
	    	rangeGridlinesVisible           = xyPlot.isRangeGridlinesVisible() ;
	    	rangeZeroBaselineVisible        = xyPlot.isRangeZeroBaselineVisible();
    	}		
	
		public void updateJFreeChartProperties(JFreeChart jFreeChart){
	
			jFreeChart.setAntiAlias(antiAlias);
			jFreeChart.setBackgroundPaint(backgroundPaint);
			
			jFreeChart.setTitle(textTitle);

			XYPlot xyPlot = (XYPlot)jFreeChart.getPlot();
			ValueAxis domainAxis  = xyPlot.getDomainAxis();
			domainAxis = xyPlot.getDomainAxis();
			domainAxis.setAxisLineVisible(domainAxisLineVisible);
			domainAxis.setTickLabelsVisible(domainTickLabelsVisible);// 5
			domainAxis.setTickMarksVisible(domainTickMarksVisible);
			domainAxis.setVisible(domainVisible);
	
			ValueAxis rangeAxis = xyPlot.getRangeAxis();
			rangeAxis = xyPlot.getRangeAxis();
			rangeAxis.setAxisLineVisible(rangeAxisLineVisible);
			rangeAxis.setTickLabelsVisible(rangeTickLabelsVisible);
			rangeAxis.setTickMarksVisible(rangeTickMarksVisible);
			rangeAxis.setVisible(rangeVisible);
			
			xyPlot.setBackgroundPaint(paint);
			xyPlot.setOrientation(plotOrientation);
			
			xyPlot.setDomainCrosshairLockedOnData(domainCrosshairLockedOnData);
	        xyPlot.setDomainCrosshairVisible(domainCrosshairVisible); 
	        xyPlot.setDomainGridlinesVisible(domainGridlinesVisible); 
	        
	        xyPlot.setRangeCrosshairLockedOnData(rangeCrosshairLockedOnData); 
	        xyPlot.setRangeCrosshairVisible(rangeCrosshairVisible);
	        xyPlot.setRangeGridlinesVisible(rangeGridlinesVisible) ;
			xyPlot.setRangeZeroBaselineVisible(rangeZeroBaselineVisible);
		}
	}

    
    private JSplitPane mainPanel;
    private JTabbedPane tabbedPane;
    private org.geworkbench.util.JAutoList microarrayList;
    private JAutoList markerList;
    private JPanel chartPanel, topChartPanel, bottomChartPanel;
    private JCheckBox rankStatisticsCheckbox;

    private JCheckBox referenceLineCheckBox;
    private JButton clearButton;
    private JButton printButton;
    private JButton imageSnapshotButton;
    private JTextField slopeField;
    private JPopupMenu popupMenu;
    private PlotType popupType;
    private int popupIndex;
    private int limitMarkers = 0;
    private int limitArrays = 0;
    private boolean showAllMarkers = true;
    private boolean showAllArrays = true;

    /**
     * The dataset that holds the microarrayset and panels.
     */
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(null);

    /**
     * The two chart groups (one for microarrays, one for markers).
     */
    private EnumMap<PlotType, ChartGroup> chartGroups;

    private MarkerListModel markerModel = new MarkerListModel();
    private MicroarrayListModel microarrayModel = new MicroarrayListModel();

    /**
     * Constructor lays out the component and adds behaviors.
     */
    public ScatterPlot() {
        //// Create empty chart groups
        chartGroups = new EnumMap<PlotType, ChartGroup>(PlotType.class);
        chartGroups.put(PlotType.ARRAY, new ChartGroup());
        chartGroups.put(PlotType.MARKER, new ChartGroup());
        //// Create and lay out components
        // Left side:
        mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainPanel.setOneTouchExpandable(true);
        JPanel westPanel = new JPanel(new BorderLayout());
        mainPanel.add(westPanel);
        tabbedPane = new JTabbedPane();
        westPanel.add(tabbedPane, BorderLayout.CENTER);
        makeDefaultMicroArrayList();
        makeDefaultMarkerList();
        tabbedPane.add("Array", microarrayList);
        tabbedPane.add("Marker", markerList);

        // Right side:
        JPanel rightPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(Box.createGlue());
        rankStatisticsCheckbox = new JCheckBox("Rank Statistics Plot");
        topPanel.add(rankStatisticsCheckbox);
        rightPanel.add(topPanel, BorderLayout.NORTH);

        chartPanel = new JPanel();
        chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.Y_AXIS));
        topChartPanel = new JPanel();
        topChartPanel.setLayout(new BoxLayout(topChartPanel, BoxLayout.X_AXIS));
        bottomChartPanel = new JPanel();
        bottomChartPanel.setLayout(new BoxLayout(bottomChartPanel, BoxLayout.X_AXIS));
        chartPanel.add(topChartPanel);
        chartPanel.add(bottomChartPanel);
        // setChartBackgroundColor(chart);
        rightPanel.add(chartPanel, BorderLayout.CENTER);
        JPanel bottomSpacingPanel = new JPanel();
        bottomSpacingPanel.setLayout(new BoxLayout(bottomSpacingPanel, BoxLayout.Y_AXIS));
        bottomSpacingPanel.add(Box.createVerticalStrut(6));
        JPanel bottomPanel = new JPanel();
        bottomSpacingPanel.add(bottomPanel);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        referenceLineCheckBox = new JCheckBox("Reference Line", true);
        JLabel slopeLabel = new JLabel("Slope:");
        slopeField = new JTextField("1.0", 10) {
			private static final long serialVersionUID = 6545882976044437591L;

			@Override public Dimension getMaximumSize() {
                return super.getPreferredSize();
            }
        };
        bottomPanel.add(referenceLineCheckBox);
        bottomPanel.add(Box.createHorizontalStrut(10));
        bottomPanel.add(slopeLabel);
        bottomPanel.add(Box.createHorizontalStrut(5));
        bottomPanel.add(slopeField);
        bottomPanel.add(Box.createGlue());
        clearButton = new JButton("Clear Charts");
        bottomPanel.add(clearButton);
        bottomPanel.add(Box.createHorizontalStrut(5));
        printButton = new JButton("Print...");
        imageSnapshotButton = new JButton("Image Snapshot");
        bottomPanel.add(printButton);
        bottomPanel.add(Box.createHorizontalStrut(5));
        bottomPanel.add(imageSnapshotButton);
        rightPanel.add(bottomSpacingPanel, BorderLayout.SOUTH);
        mainPanel.add(rightPanel);
        popupMenu = new JPopupMenu();
        JMenuItem xAxisItem = new JMenuItem("Put on X-axis", 'X');
        popupMenu.add(xAxisItem);
        JMenuItem clearAllItem = new JMenuItem("Clear All", 'A');
        popupMenu.add(clearAllItem);

        // Initially inactive
        rankStatisticsCheckbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                // Update all charts
                updateCharts(PlotType.MARKER);
                updateCharts(PlotType.ARRAY);
            }
        });
        // Use these listeners to effectively prevent selection (we handle that specially ourselves)
        microarrayList.getList().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                // microarrayList.getList().clearSelection();
            }
        });
        markerList.getList().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                // markerList.getList().clearSelection();
            }
        });
        // Change over the charts when the user switches between microarray and marker.
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (tabbedPane.getSelectedIndex() == TAB_ARRAY) {
                	updateCharts(PlotType.ARRAY);
                    packChartPanel(PlotType.ARRAY);
                } else {
                	updateCharts(PlotType.MARKER);
                    packChartPanel(PlotType.MARKER);
                }
                ChartGroup group = chartGroups.get(getActivePlotType());
                if (group.referenceLineEnabled) {
                    referenceLineCheckBox.setSelected(true);
                    slopeField.setEnabled(true);
                } else {
                    referenceLineCheckBox.setSelected(false);
                    slopeField.setEnabled(false);
                }
                slopeField.setText("" + group.slope);
            }
        });
        // Popup action for putting an item ono the x-axis
        xAxisItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setNewXAxis(popupType, popupIndex);
            }
        });
        // Popup action for clearing all charts.
        clearAllItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearAllCharts(popupType);
                if (popupType == PlotType.ARRAY) {
                    microarrayModel.refresh();
                } else {
                    markerModel.refresh();
                }
                packChartPanel(popupType);
            }
        });
        // Clear button action
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearAllCharts(getActivePlotType());
                packChartPanel(getActivePlotType());
                if (getActivePlotType() == PlotType.MARKER) {
                    markerModel.refresh();
                } else {
                    microarrayModel.refresh();
                }
            }
        });
        // Initially disabled
        clearButton.setEnabled(false);
        printButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PrintUtils.printComponent(chartPanel);
            }
        });
        // Initially disabled
        printButton.setEnabled(false);
        imageSnapshotButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createImageSnapshot();
            }
        });
        // Initially disabled
        imageSnapshotButton.setEnabled(false);
        // Reference line on/off
        referenceLineCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ChartGroup group = chartGroups.get(getActivePlotType());
                if (referenceLineCheckBox.isSelected()) {
                    slopeField.setEnabled(true);
                    group.referenceLineEnabled = true;
                    slopeFieldChanged();
                } else {
                    slopeField.setEnabled(false);
                    group.referenceLineEnabled = false;
                    slopeChanged(group);
                }
            }
        });
        // Reference line slope field
        slopeField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                slopeFieldChanged();
            }
        });
        slopeField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                // Ignore
            }

            public void focusLost(FocusEvent e) {
                slopeFieldChanged();
            }
        });
        // Initialize chart panel
        packChartPanel(PlotType.ARRAY);
    }

    private void makeDefaultMarkerList() {
        markerList = new MarkerAutoList();
        markerList.getList().setCellRenderer(new CellRenderer());
        markerList.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        markerList.getList().setFixedCellWidth(250);
    }

    private void makeDefaultMicroArrayList() {
        microarrayList = new MicroarrayAutoList();
        microarrayList.getList().setCellRenderer(new CellRenderer());
        microarrayList.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        microarrayList.getList().setFixedCellWidth(250);
    }

    private void slopeFieldChanged() {
        String text = slopeField.getText();
        ChartGroup group = chartGroups.get(getActivePlotType());
        double newSlope;
        try {
            newSlope = Double.parseDouble(text);
            if (newSlope <= 0) {
                throw new NumberFormatException();
            }
            group.slope = newSlope;
            slopeChanged(group);
        } catch (NumberFormatException nfe) {
            // Switch back to old slope
            slopeField.setText("" + group.slope);
        }
    }

    /**
     * Adds the appropriate chart panels to the component.
     * Call this whenever charts are added or removed, or the view is switched between microarray and marker.
     * Charts are layed out in a single row (1 or 2 charts) or two rows (3 to 6 charts).
     *
     * @param type the type of charts to display.
     */
    private void packChartPanel(PlotType type) {
        // Empty panel of current contents
        topChartPanel.removeAll();
        bottomChartPanel.removeAll();
        ChartGroup group = chartGroups.get(type);
        int numCharts = group.charts.size();
        if (numCharts == 0) {
            // No charts
            // Disable buttons
            clearButton.setEnabled(false);
            printButton.setEnabled(false);
            imageSnapshotButton.setEnabled(false);
        } else {
            // Enable buttons
            clearButton.setEnabled(true);
            printButton.setEnabled(true);
            imageSnapshotButton.setEnabled(true);
            int topHalf = (numCharts - 1) / 2;
            ChartPanel panel = null;
            for (int i = 0; i < numCharts; i++) {
                panel = group.charts.get(i).panel;
                if ((numCharts <= 2) || (i <= topHalf)) {
                    topChartPanel.add(panel);
                } else {
                    bottomChartPanel.add(panel);
                }
            }
            if ((numCharts > 1) && (numCharts % 2 == 1)) {
                // Add padding component so that charts are not stretched at the bottom
                final JPanel template = panel;
                JComponent padding = new JComponent() {
					private static final long serialVersionUID = -8827231207172510858L;

					@Override public Dimension getPreferredSize() {
                        return template.getPreferredSize();
                    }

                    @Override public Dimension getMaximumSize() {
                        return template.getMaximumSize();
                    }

                    @Override public Dimension getMinimumSize() {
                        return template.getMinimumSize();
                    }
                };
                padding.setForeground(Color.white);
                bottomChartPanel.setBackground(Color.white);
                bottomChartPanel.add(padding);
            }
        }
        // Ensure that the panel repaints.
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private static XYLineAnnotation getXYLineAnnotation(double slope) {
        // Lines must unfortunately be limited in length due to bug in JFreeChart.
        double low = -100.0;
        double high = 1000.0;
        if (slope > 1) {
            double highX = high / slope;
            double lowX = low / slope;
            return new XYLineAnnotation(lowX, low, highX, high);
        } else {
            double highY = high * slope;
            double lowY = low * slope;
            return new XYLineAnnotation(low, lowY, high, highY);
        }
    }

    private void slopeChanged(ChartGroup group) {
        // Change annotations on all charts in this chart group
        XYLineAnnotation newAnnotation;
        if (group.referenceLineEnabled) {
            newAnnotation = getXYLineAnnotation(group.slope);
        } else {
            newAnnotation = null;
        }
        for (int i = 0; i < group.charts.size(); i++) {
            Chart chart = group.charts.get(i);
            XYPlot plot = chart.panel.getChart().getXYPlot();
            if (group.lineAnnotation != null) {
                plot.removeAnnotation(group.lineAnnotation);
            }
            if (newAnnotation != null) {
                plot.addAnnotation(newAnnotation);
            }
        }
        group.lineAnnotation = newAnnotation;
    }

    /**
     * Called when an item is clicked in either the microarray or marker list.
     *
     * @param type the type of item clicked.
     * @param e    the mouse event for the action.
     */
    private void itemRightClicked(PlotType type, MouseEvent e, int index) {
        popupType = type;
        popupIndex = index;
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void setNewXAxis(PlotType type, int xIndex) {
    	String clickedLabel = "";
    	ChartGroup group = chartGroups.get(type);
    	if(type == PlotType.ARRAY){
    		Object o = this.microarrayModel.getElementAt(xIndex);
    		if(o != null){
    			clickedLabel = (String) o;
    		}
    	} else {
    		Object o = this.markerModel.getElementAt(xIndex);
    		if(o != null){
    			clickedLabel = ((DSGeneMarker) o).getLabel();
    		}
    	}
    	if(StringUtils.isEmpty(clickedLabel)){
    		return;
    	}
    	if(type == PlotType.ARRAY){
    		group.xIndex = this.findMAIndex(clickedLabel);
    	} else {
    		group.xIndex = this.findMarkerIndex(clickedLabel);
    	}
        Iterator<Chart> chartIterator = group.charts.iterator();
        boolean chartRemoved = false;
        while (chartIterator.hasNext()) {
            Chart chart = chartIterator.next();
            if (chart.index == group.xIndex) {
                chartIterator.remove();
                chartRemoved = true;
                break;
            }
        }
        updateCharts(type);
        if (chartRemoved) {
            packChartPanel(type);
        }
        if (type == PlotType.ARRAY) {
            microarrayModel.refresh();
        } else {
            markerModel.refresh();
        }
    }

    /**
     * Called when an item is clicked in either the microarray or marker list.
     *
     * @param type  the type of item clicked.
     * @param index the index of the item.
     */
    private void itemClicked(PlotType type, int index) {
    	String clickedLabel = "";
    	if(type == PlotType.ARRAY){
    		Object o = this.microarrayModel.getElementAt(index);
    		if(o != null){
    			clickedLabel = (String) o;
    		}
    	} else {
    		Object o = this.markerModel.getElementAt(index);
    		if(o != null){
    			clickedLabel = ((DSGeneMarker) o).getLabel();
    		}
    	}
    	if(StringUtils.isEmpty(clickedLabel)){
    		return;
    	}
    	ChartGroup group = chartGroups.get(type);
        if (group.xIndex == -1) {
            // This item goes on the x-axis.
        	if (type == PlotType.ARRAY) {
        		group.xIndex = findMAIndex(clickedLabel);
            } else {
        		group.xIndex = findMarkerIndex(clickedLabel);
            }
        } else {
        	int currentIndex = -1;
        	if (type == PlotType.ARRAY) {
        		currentIndex = findMAIndex(clickedLabel);
            } else {
            	currentIndex = findMarkerIndex(clickedLabel);
            }
            if ((currentIndex >= 0) && (currentIndex == group.xIndex)) {
                // Clicked on the x-axis item-- ignore.
            } else {
                // Is it already plotted on the y-axis?
                Chart existing = null;
                if(currentIndex >= 0) existing = getChartForTypeAndIndex(type, currentIndex);
                if (existing == null) {
                    // Put item on the y-axis.
                    // Already reach maximum plots? Replace the oldest one.
                    Chart attributes;
                    if (group.charts.size() == MAXIMUM_CHARTS) {
                        attributes = group.charts.remove(0);
                    } else {
                        // Otherwise, create a new one.
                        attributes = new Chart();
                    }
                    //attributes.index = index;
                    group.charts.add(attributes);
                    JFreeChart chart;
                    if (type == PlotType.ARRAY) {
                		attributes.index = findMAIndex(clickedLabel);
                        chart = createMicroarrayChart(group.xIndex, attributes.index, attributes.chartData, attributes.panel);
                    } else {
                		attributes.index = findMarkerIndex(clickedLabel);
                        chart = createGeneChart(group.xIndex, attributes.index, attributes.chartData);
                    }
                    if (attributes.panel == null) {
                        attributes.panel = new ChartPanel(chart);
                        if (type == PlotType.ARRAY) {
                            attributes.panel.addChartMouseListener(new MicroarrayChartMouseListener(attributes.chartData));
                        } else {
                            attributes.panel.addChartMouseListener(new GeneChartMouseListener(attributes.chartData));
                        }
                        /* This following is necessary to make tooltip aware of chart panel, which is out of the 'regular" chain of info flow. */
                        MicroarrayXYToolTip tooltips = new MicroarrayXYToolTip(attributes.chartData, attributes.panel, chart.getXYPlot());
                        XYItemRenderer renderer = chart.getXYPlot().getRenderer();                      
                        Rectangle2D bound = renderer.getBaseShape().getBounds2D();
                        tooltips.setShapeBound(bound);
                        renderer. setBaseToolTipGenerator(tooltips);
                        chart.getXYPlot().setRenderer(renderer);
                        /* END of section to handle tooltip */
                    } else {
                        attributes.panel.setChart(chart);
                    }
                } else {
                    // Otherwise, remove the chart
                    group.charts.remove(existing);
                }
                // Lay out the chart panel again (as the number of charts has changed)
                packChartPanel(type);
            }
        }
    }
    
    private void updateBothTabs(){
    	microarrayList.clearSelections();
        markerList.clearSelections();
    	
        if (tabbedPane.getSelectedIndex() == TAB_ARRAY) {
        	updateCharts(PlotType.ARRAY);
            packChartPanel(PlotType.ARRAY);
        } else {
        	updateCharts(PlotType.MARKER);
            packChartPanel(PlotType.MARKER);
        }        
    }

    private void clearAllCharts(PlotType type) {
        ChartGroup chartGroup = chartGroups.get(type);
        chartGroup.xIndex = -1;
        chartGroup.charts.clear();
    }

    private PlotType getActivePlotType() {
        if (tabbedPane.getSelectedIndex() == TAB_ARRAY) {
            return PlotType.ARRAY;
        } else {
            return PlotType.MARKER;
        }
    }

    /**
     * Call this to update the data of all charts of a specific type.
     *
     * @param type the type of charts to update.
     */
    private void updateCharts(PlotType type) {
        ChartGroup group = chartGroups.get(type);
        for (int i = 0; i < group.charts.size(); i++) {
            Chart chart = group.charts.get(i);
			ChartPanel localChartPanel = chart.panel;
			JFreeChart jFreeChartOriginal = localChartPanel.getChart();
			JFreeChartProperties jFreeChartProperties = new JFreeChartProperties(jFreeChartOriginal);
			JFreeChart jFreeChartFinal = null;
			if (type == PlotType.ARRAY) {
				jFreeChartFinal =  createMicroarrayChart(group.xIndex, chart.index, chart.chartData, chart.panel);
			} else {
				jFreeChartFinal =  createGeneChart(group.xIndex, chart.index, chart.chartData);
			}
			jFreeChartProperties.updateJFreeChartProperties(jFreeChartFinal);
			chart.panel.setChart(jFreeChartFinal);
        }
    }

    /**
     * Gets the chart for the given attributes.
     *
     * @param type  the chart type (microarray or marker).
     * @param index the index of the microarray or marker.
     * @return the chart or <code>null</code> if there is no chart at this index.
     */
    private Chart getChartForTypeAndIndex(PlotType type, int index) {
        ChartGroup group = chartGroups.get(type);
        for (int i = 0; i < group.charts.size(); i++) {
            Chart chart = group.charts.get(i);
            if (chart.index == index) {
                return chart;
            }
        }
        return null;
    }

    @Publish public ImageSnapshotEvent createImageSnapshot() {
        Dimension panelSize = chartPanel.getSize();
        BufferedImage image = new BufferedImage(panelSize.width, panelSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        chartPanel.paint(g);
        ImageIcon icon = new ImageIcon(image, "Scatter Plot");
        org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent("Scatter Plot Snapshot", icon, org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
        return event;
    }

    /**
     * The component for the GUI engine.
     */
    public Component getComponent() {
        return mainPanel;
    }

    /**
     * Receives a gene selection event.
     * The list of markers is updated and microarray plots are updated to reflect the selection.
     *
     * @param e      the event
     * @param source the source of the event (unused).
     */
    @Subscribe public void receive(org.geworkbench.events.GeneSelectorEvent e, Object source) {
    	if(e.getPanel() != null){    		
    		if ((e.getPanel().size() > 0) && (limitMarkers != e.getPanel().size())){ 
    			ChartGroup group = chartGroups.get(PlotType.MARKER);
    			if(group.charts.size() > 0){
    				ChartData cd = group.charts.get(0).chartData;
    				String xlabel = cd.getXLabel();
    				if(!isInMarkerPanel(e.getPanel(), xlabel)){
            			clearAllCharts(PlotType.MARKER);
            			group.xIndex = -1;
            		} else {
		            	for(int i = 0; i < group.charts.size(); i++){
		            		cd = group.charts.get(i).chartData;
		            		String ylabel = cd.getYLabel();
		            		if(!isInMarkerPanel(e.getPanel(), ylabel)){
		            			group.charts.remove(i);
		            			i--;
		            		}
		            	}
            		}
    			}
            	showAllMarkers = false;
                dataSetView.setMarkerPanel(e.getPanel());
                limitMarkers = dataSetView.getMarkerPanel().size();
                markerModel.refresh();
                updateBothTabs();
            }

    		if( (e.getPanel().size() <= 0) && (limitMarkers > 0)) {
            	showAllMarkers = true;
            	dataSetView.setMarkerPanel(new CSPanel<DSGeneMarker>(""));
            	limitMarkers = 0;
            	markerModel.refresh();
            	updateBothTabs();
            }
    	}     	
    }

    /**
     * Receives a phenotype selection event.
     * The list of microarrays is updated and marker plots are updated to reflect the selection.
     *
     * @param e      the event.
     * @param source the source of the event (unused).
     */
    @Subscribe public void receive(org.geworkbench.events.PhenotypeSelectorEvent<DSMicroarray> e, Object source) {   
        if (e.getTaggedItemSetTree() != null) {
    		DSPanel<DSMicroarray> activatedArrays = e.getTaggedItemSetTree().activeSubset();
            if((activatedArrays != null) && (activatedArrays.size() > 0) && (limitArrays != activatedArrays.size())){
            	ChartGroup group = chartGroups.get(PlotType.ARRAY);
    			if(group.charts.size() > 0){
    				ChartData cd = group.charts.get(0).chartData;
    				String xlabel = cd.getXLabel();
    				if(!isInMicroarrayPanel(activatedArrays, xlabel)){
            			clearAllCharts(PlotType.ARRAY);
            			group.xIndex = -1;
            		} else {
		            	for(int i = 0; i < group.charts.size(); i++){
		            		cd = group.charts.get(i).chartData;
		            		String ylabel = cd.getYLabel();
		            		if(!isInMicroarrayPanel(activatedArrays, ylabel)){
		            			group.charts.remove(i);
		            			i--;
		            		}
		            	}
            		}
    			}
            	showAllArrays = false;
	            dataSetView.setItemPanel(activatedArrays);
	            limitArrays = dataSetView.getItemPanel().size();
	            microarrayModel.refresh();
	            updateBothTabs();
            } 
            
            if( ((activatedArrays != null) && (activatedArrays.size() <= 0)) && (limitArrays > 0) ) {
            	showAllArrays = true;
            	dataSetView.setItemPanel(new CSPanel<DSMicroarray>(""));
            	limitArrays = 0;
            	microarrayModel.refresh();
            	updateBothTabs();
            }
        }        
    }

    /**
     * Receives a project event.
     *
     * @param e      the event.
     * @param source the source of the event (unused).
     */
	@SuppressWarnings("unchecked")
	@Subscribe public void receive(ProjectEvent e, Object source) {

		ProjectSelection selection = ((ProjectPanel) source).getSelection();
		DSDataSet<? extends DSBioObject> dataFile = selection.getDataSet();

		if (!(dataFile instanceof DSMicroarraySet)) {
			dataSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(
					null);
		} else {
			DSMicroarraySet set = (DSMicroarraySet) dataFile;
			// If it is the same dataset as before, then don't reset everything
			if (dataSetView.getDataSet() == set) {
				return;
			}

			dataSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(
					set);
		}

		clearAllCharts(PlotType.ARRAY);
		clearAllCharts(PlotType.MARKER);
		markerModel.refresh();
		microarrayModel.refresh();
		packChartPanel(getActivePlotType());
	}
    
    private DSGeneMarker findMarker(String label){
    	DSItemList<DSGeneMarker> markers = dataSetView.getMicroarraySet().getMarkers();
    	for(DSGeneMarker m: markers){
    		if(StringUtils.equals(m.getLabel().trim(), label.trim())){
    			return m;
    		}
    	}
    	return null;
    }
    
    private int findMarkerIndex(String label){
    	DSItemList<DSGeneMarker> markers = dataSetView.getMicroarraySet().getMarkers();
    	for(int i = 0; i < markers.size(); i++){
    		if(StringUtils.equals(markers.get(i).getLabel().trim(), label.trim())){
    			return i;
    		}
    	}
    	return -1;
    }

    /**
     * @todo Merge this method with {@link #createMicroarrayChart(int, int, org.geworkbench.components.plots.ScatterPlot.ChartData)} .
     */
    private JFreeChart createGeneChart(int marker1, int marker2, ChartData chartData) throws SeriesException {
    	DSGeneMarker currentXMarker = null;
    	DSGeneMarker currentYMarker = null;
    	if(!StringUtils.isEmpty(chartData.getXLabel())) currentXMarker = findMarker(chartData.getXLabel());
    	if(!StringUtils.isEmpty(chartData.getYLabel())) currentYMarker = findMarker(chartData.getYLabel());
        XYSeriesCollection plots = new XYSeriesCollection();
        ArrayList<XYSeries> seriesList = new ArrayList<XYSeries>();
        ArrayList<PanelVisualProperties> propertiesList = new ArrayList<PanelVisualProperties>();
        PanelVisualPropertiesManager propertiesManager = PanelVisualPropertiesManager.getInstance();

        DSMicroarraySet maSet = (DSMicroarraySet) dataSetView.getDataSet();
        boolean rankPlot = rankStatisticsCheckbox.isSelected();
        HashMap<Integer, RankSorter> map = new HashMap<Integer, RankSorter>();
        int microarrayNo = dataSetView.getMicroarraySet().size();

        // First put all the gene pairs in the xyValues array
        org.geworkbench.util.pathwaydecoder.RankSorter[] xyValues = new RankSorter[microarrayNo];
        ArrayList<ArrayList<RankSorter>> xyPoints = new ArrayList<ArrayList<org.geworkbench.util.pathwaydecoder.RankSorter>>();
        for (int i = 0; i < microarrayNo; i++) {
            xyValues[i] = new org.geworkbench.util.pathwaydecoder.RankSorter();
            if ( (currentXMarker != null) && (currentYMarker != null) ){
            	xyValues[i].x = dataSetView.getMicroarraySet().getValue(currentXMarker, i);
            	xyValues[i].y = dataSetView.getMicroarraySet().getValue(currentYMarker, i);
            }else{
                xyValues[i].x = dataSetView.getMicroarraySet().getValue(marker1, i); 
                xyValues[i].y = dataSetView.getMicroarraySet().getValue(marker2, i);            	
            }
            xyValues[i].id = i;
            map.put(new Integer(dataSetView.getMicroarraySet().get(i).getSerial()), xyValues[i]);
        }
        boolean panelsSelected = (dataSetView.getItemPanel() != null) && (dataSetView.getItemPanel().size() > 0) && (dataSetView.getItemPanel().getLabel().compareToIgnoreCase("Unsupervised") != 0) && (dataSetView.getItemPanel().panels().size() > 0);
        if ((dataSetView.getItemPanel() != null) && (dataSetView.getItemPanel().activeSubset().size() == 0)) {
            panelsSelected = false;
            showAllArrays = true;
        }
        if (rankPlot && !showAllArrays) {
            // Must first activate all valid points
            if (panelsSelected) {
                for (int pId = 0; pId < dataSetView.getItemPanel().panels().size(); pId++) {
                    DSPanel<DSMicroarray> panel = dataSetView.getItemPanel().panels().get(pId);
                    int itemNo = panel.size();
                    if (itemNo > 0) {
                        for (int i = 0; i < itemNo; i++) {
                            int serial = panel.get(i).getSerial();
                            xyValues[serial].setActive(true);
                        }
                    }
                }
            }
        }

        // Perform rank sorting if required
        int rank = 0;

        Arrays.sort(xyValues, RankSorter.SORT_Y);
        for (int j = 0; j < xyValues.length; j++) {
            if (showAllArrays || xyValues[j].isActive() || xyValues[j].isFiltered()) {
                xyValues[j].iy = rank++;
            }
        }

        rank = 0;
        
        Arrays.sort(xyValues, org.geworkbench.util.pathwaydecoder.RankSorter.SORT_X);
        for (int j = 0; j < xyValues.length; j++) {
            if (showAllArrays || xyValues[j].isActive() || xyValues[j].isFiltered()) {
                xyValues[j].ix = rank++;
            }
        }

        boolean panelsUsed = false;
        int panelIndex = 0;
        // If phenotypic panels have been selected
        if (panelsSelected) {
            for (int pId = 0; pId < dataSetView.getItemPanel().panels().size(); pId++) {
                ArrayList<RankSorter> list = new ArrayList<RankSorter>();
                DSPanel<DSMicroarray> panel = dataSetView.getItemPanel().panels().get(pId);
                int itemNo = panel.size();
                if (panel.isActive() && itemNo > 0) {
                    panelIndex++;
                    XYSeries series = new XYSeries(panel.getLabel());
                    for (int i = 0; i < itemNo; i++) {
                        int serial = panel.get(i).getSerial();
                        RankSorter xy = (RankSorter) map.get(new Integer(serial));
                        xy.setPlotted();
                        list.add(xy);
                        double x = 0;
                        double y = 0;
                        if (rankPlot) {
                            x = xy.ix;
                            y = xy.iy;
                            series.add(x, y);
                        } else {
                            x = xy.x;
                            y = xy.y;
                            series.add(x, y);
                        }
                    }
                    seriesList.add(series);
                    PanelVisualProperties properties = propertiesManager.getVisualProperties(panel);
                    if (properties == null) {
                        properties = propertiesManager.getDefaultVisualProperties(panelIndex);
                    }
                    propertiesList.add(properties);
                    Collections.sort(list, org.geworkbench.util.pathwaydecoder.RankSorter.SORT_X);
                    xyPoints.add(list);
                }
            }
        }
        // finally if all the others must be shown as well
        if (showAllArrays) {
            ArrayList<RankSorter> list = new ArrayList<org.geworkbench.util.pathwaydecoder.RankSorter>();
            XYSeries series = new XYSeries(panelsUsed ? "Other" : "All");
            for (int serial = 0; serial < xyValues.length; serial++) {
                if (!xyValues[serial].isPlotted()) {
                    list.add(xyValues[serial]);
                    double x = 0;
                    double y = 0;
                    if (rankPlot) {
                        x = xyValues[serial].ix;
                        y = xyValues[serial].iy;
                        series.add(x, y);
                    } else {
                        x = xyValues[serial].x;
                        y = xyValues[serial].y;
                        series.add(x, y);
                    }
                }
            }
            xyPoints.add(0, list);
            plots.addSeries(series);
        }
        for (int i = 0; i < seriesList.size(); i++) {
            XYSeries series = (XYSeries) seriesList.get(i);
            plots.addSeries(series);
        }
        String label1 = "";
        String label2 = "";
		if ( (currentXMarker != null) && (currentYMarker != null)){
			label1 = currentXMarker.getLabel();
			label2 = currentYMarker.getLabel();
        }else{
        	label1 = maSet.getMarkers().get(marker1).getLabel();
        	label2 = maSet.getMarkers().get(marker2).getLabel();
        }        
        chartData.setXLabel(label1);
        chartData.setYLabel(label2);
        JFreeChart mainChart = ChartFactory.createScatterPlot("", label1, label2, plots, PlotOrientation.VERTICAL, true, true, false); // Title, (, // X-Axis label,  Y-Axis label,  Dataset,  Show legend
        XYLineAnnotation annotation = chartGroups.get(PlotType.MARKER).lineAnnotation;
        if (annotation != null) {
            mainChart.getXYPlot().addAnnotation(annotation);
        }
        chartData.setXyPoints(xyPoints);
        StandardXYToolTipGenerator tooltips = new GeneXYToolTip(chartData);
        StandardXYItemRenderer renderer = new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES, tooltips);
        for (int i = 0; i < propertiesList.size(); i++) {
            PanelVisualProperties panelVisualProperties = propertiesList.get(i);
            // Note: "i+1" because we skip the default 'other' series.
            int index = showAllArrays ? i + 1 : i;
            renderer.setSeriesPaint(index, panelVisualProperties.getColor());
            renderer.setSeriesShape(index, panelVisualProperties.getShape());
        }
        mainChart.getXYPlot().setRenderer(renderer);

        return mainChart;
    }
    
    private DSMicroarray findMA(String label){
    	DSMicroarraySet maSet = (DSMicroarraySet) dataSetView.getDataSet();
    	for(DSMicroarray ma: maSet){
    		if(StringUtils.equals(ma.getLabel(), label))
    			return ma;
    	}
    	return null;
    }
    
    private int findMAIndex(String label){
    	DSMicroarraySet maSet = (DSMicroarraySet) dataSetView.getDataSet();
    	for(int i = 0; i < maSet.size(); i++){
    		if(StringUtils.equals(maSet.get(i).getLabel(), label))
    			return i;
    	}
    	return -1;
    }

    /**
     * @TODO Merge this method with {@link #createGeneChart(int, int, org.geworkbench.components.plots.ScatterPlot.ChartData)} .
     */
    private JFreeChart createMicroarrayChart(int exp1, int exp2, ChartData chartData, ChartPanel chartPanel) throws SeriesException {
    	DSMicroarray ma1 = null;
        DSMicroarray ma2 = null;
        if(!StringUtils.isEmpty(chartData.getXLabel())) ma1 = findMA(chartData.getXLabel());
        if(!StringUtils.isEmpty(chartData.getYLabel())) ma2 = findMA(chartData.getYLabel());
        XYSeriesCollection plots = new XYSeriesCollection();
        ArrayList<XYSeries> seriesList = new ArrayList<XYSeries>();
        ArrayList<PanelVisualProperties> propertiesList = new ArrayList<PanelVisualProperties>();
        PanelVisualPropertiesManager propertiesManager = PanelVisualPropertiesManager.getInstance();

        DSMicroarraySet maSet = (DSMicroarraySet) dataSetView.getDataSet();
        boolean rankPlot = rankStatisticsCheckbox.isSelected();
        HashMap<Integer, RankSorter> map = new HashMap<Integer, RankSorter>();
        int numMarkers = maSet.getMarkers().size();
        // First put all the gene pairs in the xyValues array
        RankSorter[] xyValues = new RankSorter[numMarkers];
        ArrayList<ArrayList<RankSorter>> xyPoints = new ArrayList<ArrayList<org.geworkbench.util.pathwaydecoder.RankSorter>>();
      	ma1 = maSet.get(exp1);
        ma2 = maSet.get(exp2);
        for (int i = 0; i < numMarkers; i++) {
            xyValues[i] = new RankSorter();
            xyValues[i].x = ma1.getMarkerValue(i).getValue();
            xyValues[i].y = ma2.getMarkerValue(i).getValue();
            xyValues[i].id = i;
            map.put(new Integer(i), xyValues[i]);
        }
        boolean panelsSelected = (dataSetView.getMarkerPanel() != null) && (dataSetView.getMarkerPanel().size() > 0) && (dataSetView.getMarkerPanel().getLabel().compareToIgnoreCase("Unsupervised") != 0) && (dataSetView.getMarkerPanel().panels().size() > 0);
        if ((dataSetView.getMarkerPanel() != null) && (dataSetView.getMarkerPanel().activeSubset().size() == 0)) {
            panelsSelected = false;
            showAllMarkers = true;
        }
        if (rankPlot && !showAllMarkers) {
            // Must first activate all valid points
            if ((dataSetView.getMarkerPanel().size() > 0) && (dataSetView.getMarkerPanel().panels().size() > 0)) {
                for (int pId = 0; pId < dataSetView.getMarkerPanel().panels().size(); pId++) {
                    DSPanel<DSGeneMarker> panel = dataSetView.getMarkerPanel().panels().get(pId);
                    int itemNo = panel.size();
                    if (itemNo > 0) {
                        for (int i = 0; i < itemNo; i++) {
                            int serial = panel.get(i).getSerial();
                            xyValues[serial].setActive(true);
                        }
                    }
                }
            }
        }

        // Perform rank sorting if required
        int rank = 0;

        Arrays.sort(xyValues, RankSorter.SORT_Y);
        for (int j = 0; j < xyValues.length; j++) {
            if (showAllMarkers || xyValues[j].isActive() || xyValues[j].isFiltered()) {
                xyValues[j].iy = rank++;
            }
        }

        rank = 0;

        Arrays.sort(xyValues, RankSorter.SORT_X);
        for (int j = 0; j < xyValues.length; j++) {
            if (showAllMarkers || xyValues[j].isActive() || xyValues[j].isFiltered()) {
                xyValues[j].ix = rank++;
            }
        }

        int panelIndex = 0;
        boolean panelsUsed = false;
        // If gene panels have been selected
        if (panelsSelected) {
            panelsUsed = true;
            for (int pId = 0; pId < dataSetView.getMarkerPanel().panels().size(); pId++) {
                ArrayList<org.geworkbench.util.pathwaydecoder.RankSorter> list = new ArrayList<RankSorter>();
                DSPanel<DSGeneMarker> panel = dataSetView.getMarkerPanel().panels().get(pId);
                int itemNo = panel.size();
                if (panel.isActive() && itemNo > 0) {
                    panelIndex++;
                    XYSeries series = new XYSeries(panel.getLabel());
                    for (int i = 0; i < itemNo; i++) {
                        int serial = panel.get(i).getSerial();
                        RankSorter xy = (RankSorter) map.get(new Integer(serial));
                        xy.setPlotted();
                        list.add(xy);
                        double x = 0;
                        double y = 0;
                        if (rankPlot) {
                            x = xy.ix;
                            y = xy.iy;
                            series.add(x, y);
                        } else {
                            x = xy.x;
                            y = xy.y;
                            series.add(x, y);
                        }
                    }
                    //plots.addSeries(series);
                    seriesList.add(series);
                    PanelVisualProperties properties = propertiesManager.getVisualProperties(panel);
                    if (properties == null) {
                        properties = propertiesManager.getDefaultVisualProperties(panelIndex);
                    }
                    propertiesList.add(properties);
                    Collections.sort(list, RankSorter.SORT_X);
                    xyPoints.add(list);
                }
            }
        }
        // finally if all the others must be shown as well
        if (showAllMarkers) {
            ArrayList<RankSorter> list = new ArrayList<org.geworkbench.util.pathwaydecoder.RankSorter>();
            XYSeries series = new XYSeries(panelsUsed ? "Other" : "All");
            for (int serial = 0; serial < xyValues.length; serial++) {
                if (!xyValues[serial].isPlotted()) {
                    list.add(xyValues[serial]);
                    double x = 0;
                    double y = 0;
                    if (rankPlot) {
                        x = xyValues[serial].ix;
                        y = xyValues[serial].iy;
                        series.add(x, y);
                    } else {
                        x = xyValues[serial].x;
                        y = xyValues[serial].y;
                        series.add(x, y);
                    }
                }
            }
            xyPoints.add(0, list);
            plots.addSeries(series);
        }
        for (int i = 0; i < seriesList.size(); i++) {
            XYSeries series = (XYSeries) seriesList.get(i);
            plots.addSeries(series);
        }
        String label1 = "";
        String label2 = "";
        label1 = ma1.getLabel();
        label2 = ma2.getLabel();
        chartData.setXLabel(label1);
        chartData.setYLabel(label2);
        JFreeChart mainChart = ChartFactory.createScatterPlot("", label1, label2, plots, PlotOrientation.VERTICAL, true, true, false); // Title, (, // X-Axis label,  Y-Axis label,  Dataset,  Show legend
        XYLineAnnotation annotation = chartGroups.get(PlotType.ARRAY).lineAnnotation;
        if (annotation != null) {
            mainChart.getXYPlot().addAnnotation(annotation);
        }
        chartData.setXyPoints(xyPoints);
        MicroarrayXYToolTip tooltips = new MicroarrayXYToolTip(chartData, chartPanel, mainChart.getXYPlot());
        StandardXYItemRenderer renderer = new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES, tooltips);      
        Rectangle2D bound = renderer.getBaseShape().getBounds2D();
        tooltips.setShapeBound(bound);
        for (int i = 0; i < propertiesList.size(); i++) {
            PanelVisualProperties panelVisualProperties = propertiesList.get(i);
            // Note: "i+1" because we skip the default 'other' series.
            int index = showAllMarkers ? i + 1 : i;
            renderer.setSeriesPaint(index, panelVisualProperties.getColor());
            renderer.setSeriesShape(index, panelVisualProperties.getShape());
        }
        mainChart.getXYPlot().setRenderer(renderer);

        return mainChart;
    }

    @Publish public org.geworkbench.events.MarkerSelectedEvent publishMarkerSelectedEvent(MarkerSelectedEvent event) {
        return event;
    }

    @Publish public PhenotypeSelectedEvent publishPhenotypeSelectedEvent(PhenotypeSelectedEvent event) {
        return event;
    }
    
    private boolean isInMicroarrayPanel(DSPanel<DSMicroarray> mas, String label){
    	for(DSMicroarray ma: mas){
    		if(StringUtils.equals(ma.getLabel().trim(), label.trim())) 
    			return true;
    	}
    	return false;
    }
    
    private boolean isInMarkerPanel(DSPanel<DSGeneMarker> markers, String label){
    	for(DSGeneMarker m: markers){
    		if(StringUtils.equals(m.getLabel().trim(), label.trim())) 
    			return true;
    	}
    	return false;
    }

}
