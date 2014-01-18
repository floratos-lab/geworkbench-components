package org.geworkbench.components.poshistogram;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern;
import org.geworkbench.util.patterns.CSMatchedSeqPattern;
import org.geworkbench.util.patterns.PatternOperations;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.axis.NumberAxis;

/**
 * <p>PositionHistogramWidget</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version $Id$
 */

public final class PositionHistogramWidget extends JPanel {
	private static final long serialVersionUID = -1642260844923573623L;

	private List<DSMatchedPattern<DSSequence, CSSeqRegistration>>
            patterns = new ArrayList<DSMatchedPattern<DSSequence,
            CSSeqRegistration>>();

	private ChartPanel chartPanel = new ChartPanel(null, true);
    private JFreeChart chart = null;
    private BorderLayout borderLayout1 = new BorderLayout();
    private JToolBar jToolBar1 = new JToolBar();
    private JButton plotButton = new JButton();
    private JButton imageSnapshotButton = new JButton();
    private Component component1;
    private Component component2;
    private Component component3;

    private JLabel jLabel1 = new JLabel();
    private Component component4;
    private Component component5;
    private JTextField jStepBox = new JTextField(5);
    private Component component6;
    private Component component7;

	private DSSequenceSet<DSSequence> sequenceDB = null;
    private PositionHistogramAppComponent parentComponent;

    public PositionHistogramWidget(PositionHistogramAppComponent positionHistogramAppComponent) {
        this.parentComponent = positionHistogramAppComponent;
        // An XYDataset can create area, line, and step XY charts. The following example creates an XYDataset from a series of data containing three XY points. Next, ChartFactory's createAreaXYChart() method creates an area XY chart. In addition to parameters for title, dataset, and legend, createAreaXYChart() takes in the labels for the X and Y *
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        component1 = Box.createHorizontalStrut(8);
        component2 = Box.createHorizontalStrut(8);
        component3 = Box.createHorizontalStrut(8);
        component4 = Box.createHorizontalStrut(8);
        component5 = Box.createHorizontalStrut(8);
        component6 = Box.createHorizontalStrut(8);
        component7 = Box.createHorizontalStrut(8);
        this.setLayout(borderLayout1);

        plotButton.setText("Plot Position");
        plotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                plotAction(e);
            }
        });

        imageSnapshotButton.setHorizontalAlignment(SwingConstants.CENTER);
        imageSnapshotButton.setText("Image Snapshot");
        imageSnapshotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                imageSnapshotAction(e);
            }
        });

        jLabel1.setText("Step:");
        jStepBox.setText("2");

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(chartPanel);
		this.add(p , BorderLayout.CENTER);
        this.add(jToolBar1, BorderLayout.NORTH);
        jToolBar1.add(plotButton, null);
        jToolBar1.add(component1, null);
        jToolBar1.add(imageSnapshotButton, null);
        jToolBar1.add(component2, null);
        jToolBar1.add(component3, null);
        jToolBar1.add(component7, null);
        jToolBar1.add(component6, null);
        jToolBar1.add(component5, null);
        jToolBar1.add(jLabel1, null);
        jToolBar1.add(component4, null);
        jToolBar1.add(jStepBox, null);
    }

    private int getMaxLength() {
        int maxLen = 0;
        for (DSMatchedPattern<DSSequence, CSSeqRegistration> item : patterns) {
            maxLen = Math.max(maxLen, ((DSMatchedSeqPattern)item).getMaxLength());
        }
        return maxLen;
    }

    private void plotAction(ActionEvent e) {
        int maxLen = getMaxLength();
        int step = 0;
		try {
			step = Integer.parseInt(jStepBox.getText());
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, "Step should be a positive integer");
			return;
		}
		if (step <= 0) {
			JOptionPane.showMessageDialog(null, "Step should be a positive integer");
			return;
		}	

        int maxBin = maxLen / step;
		



        XYSeriesCollection plots = new XYSeriesCollection();
        for (int rowId = 0; rowId < patterns.size(); rowId++) {

            int[] yAxis = new int[maxBin];
            double[] yMean = new double[maxBin];
            DSMatchedSeqPattern pat = (DSMatchedSeqPattern) patterns.get(rowId);
            if (pat != null && pat.getClass().isAssignableFrom(CSMatchedSeqPattern.class)) {
				CSMatchedSeqPattern pattern = (CSMatchedSeqPattern) pat;
				for (int id = 0; id < pattern.getSupport(); id++) {
					int dx = pattern.getOffset(id) / step;
					if (dx < maxBin) {
						yAxis[dx]++;
					}
				}
				
                String ascii = pat.getASCII();
                if (ascii == null) {
                    PatternOperations.fill(pat, sequenceDB);
                    ascii = pat.getASCII();
                }
                XYSeries series = new XYSeries(ascii);
                for (int i = 0; i < maxBin; i++) {
                    series.add((double) i * step + 1, (double) yAxis[i] / pat.getSupport()); //start sequence numbering at 1.
                }
                plots.addSeries(series);
            }
        }
        chart = ChartFactory.createXYLineChart("Motif Location Histogram", // Title
                "Position", // X-Axis label
                "Support", // Y-Axis label
                plots, // Dataset
                PlotOrientation.VERTICAL, true, // Show legend
                false, false);
				
		XYPlot xyplot = chart.getXYPlot();
		ValueAxis valueAxis = xyplot.getDomainAxis();
        valueAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
        chartPanel.setChart(chart);
    }

    public void imageSnapshotAction(ActionEvent e) {
        if (chart != null) {
            ByteArrayOutputStream byteout = new ByteArrayOutputStream();
            try {
                ChartUtilities.writeChartAsPNG(byteout, chart, 500, 300);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            ImageIcon chartImage = new ImageIcon(byteout.toByteArray());
            ImageIcon newIcon = new ImageIcon(chartImage.getImage(), "Position Histogram Snapshot");
            org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent("Positions Histogram Snapshot", newIcon, org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
            parentComponent.publishImageSnapshotEvent(event);
        }

    }

    public void setPatterns(List<DSMatchedPattern<DSSequence, CSSeqRegistration>> matches) {
        patterns.clear();
        for (int i = 0; i < matches.size(); i++) {
            patterns.add(matches.get(i));
        }
    }

	public void setSequenceDB(DSSequenceSet<DSSequence> sDB) {
        sequenceDB = sDB;
    }
}
