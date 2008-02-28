package org.geworkbench.components.poshistogram;

import org.geworkbench.bison.datastructure.biocollections.Collection;
import org.geworkbench.bison.datastructure.biocollections.DSCollection;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
import org.geworkbench.events.SequenceDiscoveryTableEvent;
import org.geworkbench.util.patterns.CSMatchedSeqPattern;
import org.geworkbench.util.patterns.FlexiblePattern;
import org.geworkbench.util.patterns.PatternOperations;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

/**
 * <p>PositionHistogramWidget</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class PositionHistogramWidget extends JPanel {
    //JPanel jPanel1 = new JPanel();
    //private ArrayList patterns = new ArrayList();

    private DSCollection<DSMatchedPattern<DSSequence, CSSeqRegistration>>
            patterns = new Collection<DSMatchedPattern<DSSequence,
            CSSeqRegistration>>();


    private JLabel lblChart = new JLabel();
    private JFreeChart chart = null;
    private BorderLayout borderLayout1 = new BorderLayout();
    private JToolBar jToolBar1 = new JToolBar();
    private JButton plotButton = new JButton();
    private JButton imageSnapshotButton = new JButton();
    private Component component1;
    private Component component2;
    // mantis issue 0000792
    //private JToggleButton jAbsRelBtn = new JToggleButton();
    private Component component3;
    // mantis issue 0000792
    //private JButton filterButton = new JButton();
    // mantis issue 0000792
    //private JToggleButton jAvgPeakBtn = new JToggleButton();
    private JButton pairsButton = new JButton();
    // mantis issue 0000792
    //private JTextField jFlexiSupportBox = new JTextField();
    private JLabel jLabel1 = new JLabel();
    private Component component4;
    private Component component5;
    private JTextField jStepBox = new JTextField();
    // mantis issue 0000792
    //private JLabel jLabel2 = new JLabel();
    private Component component6;
    private Component component7;
    private DSSequenceSet sequenceDB = null;
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
        plotButton.setPreferredSize(new Dimension(80, 27));
        plotButton.setText("Plot Position");
        plotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                plotAction(e);
            }
        });
        imageSnapshotButton.setPreferredSize(new Dimension(80, 27));
        imageSnapshotButton.setHorizontalAlignment(SwingConstants.CENTER);
        imageSnapshotButton.setText("Image Snapshot");
        imageSnapshotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                imageSnapshotAction(e);
            }
        });
        // mantis issue 0000792
        //jAbsRelBtn.setPreferredSize(new Dimension(80, 27));
        //jAbsRelBtn.setText("Abs/Rel");
        //filterButton.setPreferredSize(new Dimension(80, 27));
        //filterButton.setText("Filter");
        /*
        filterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                filterAction(e);
            }
        });
        
        jAvgPeakBtn.setPreferredSize(new Dimension(80, 27));
        jAvgPeakBtn.setText("Avg/Peak");
        jAvgPeakBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jAvgPeakBtn_actionPerformed(e);
            }
        });*/
        pairsButton.setToolTipText("");
        pairsButton.setText("Pairs");
        pairsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pairsAction(e);
            }
        });
        // mantis issue 0000792
        //jFlexiSupportBox.setText("100");
        jLabel1.setText("Step:");
        jStepBox.setText("2");
        jStepBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jStepBox_actionPerformed(e);
            }
        });
        // mantis issue 0000792
        //jLabel2.setText("flex thr.");
        this.add(lblChart, BorderLayout.CENTER);
        this.add(jToolBar1, BorderLayout.NORTH);
        jToolBar1.add(plotButton, null);
        jToolBar1.add(component1, null);
        jToolBar1.add(imageSnapshotButton, null);
        jToolBar1.add(component2, null);
        // mantis issue 0000792
        //jToolBar1.add(filterButton, null);
        jToolBar1.add(component3, null);
        // mantis issue 0000792
        //jToolBar1.add(jAbsRelBtn, null);
        //jToolBar1.add(jAvgPeakBtn, null);
        // jToolBar1.add(pairsButton, null);
        jToolBar1.add(component7, null);
        // mantis issue 0000792
        //jToolBar1.add(jLabel2, null);
        jToolBar1.add(component6, null);
        // mantis issue 0000792
        //jToolBar1.add(jFlexiSupportBox, null);
        jToolBar1.add(component5, null);
        jToolBar1.add(jLabel1, null);
        jToolBar1.add(component4, null);
        jToolBar1.add(jStepBox, null);
    }

    private int getMaxLength() {
        int maxLen = 0;
        for (Iterator iter = patterns.iterator(); iter.hasNext();) {
            DSMatchedSeqPattern item = (DSMatchedSeqPattern) iter.next();
            maxLen = Math.max(maxLen, item.getMaxLength());
        }
        return maxLen;
    }

    void plotAction(ActionEvent e) {
        int maxLen = getMaxLength();
        int step = Integer.parseInt(jStepBox.getText());
        int wind = 1;
        int maxBin = maxLen / step + 1;
        // mantis issue 0000792
        //boolean isAbs = !jAbsRelBtn.isSelected();
        int factor = 1;
        XYSeriesCollection plots = new XYSeriesCollection();
        for (int rowId = 0; rowId < patterns.size(); rowId++) {
            int[] yAxis = new int[maxBin * 2 + 1];
            double[] yMean = new double[maxBin * 2 + 1];
            DSMatchedSeqPattern pat = (DSMatchedSeqPattern) patterns.get(rowId);
            if (pat != null) {
                if (pat.getClass().isAssignableFrom(CSMatchedSeqPattern.class)) {
                    CSMatchedSeqPattern pattern = (CSMatchedSeqPattern) pat;
                    for (int id = 0; id < pattern.getSupport(); id++) {
                        int dx = pattern.getOffset(id) / step;
                        if (dx < maxBin) {
                            yAxis[dx]++;
                        }
                    }
                } else if (pat.getClass().isAssignableFrom(org.geworkbench.util.patterns.FlexiblePattern.class)) {
                    factor = 2;
                    FlexiblePattern pattern = (org.geworkbench.util.patterns.FlexiblePattern) pat;
                    pattern.buildHistogram(yAxis, step, maxBin);
                }
                int count = 0;
                yMean[0] = (double) count / (double) wind;
                for (int x = 0; x < wind; x++) {
                    count += yAxis[x];
                }
                for (int id = 0; id < maxBin * factor - wind; id++) {
                    count -= yAxis[id];
                    count += yAxis[id + wind];
                    yMean[id + 1] = (double) count / (double) wind;
                }
                String ascii = pat.getASCII();
                if (ascii == null) {
                    PatternOperations.fill(pat, sequenceDB);
                    ascii = pat.getASCII();
                }
                XYSeries series = new XYSeries(ascii);
                for (int i = 0; i < maxBin * factor; i++) {
                	/* mantis issue 0000792
                    if (isAbs) {
                        if (factor == 1) {
                            series.add((double) i * step, (double) yMean[i]);
                        } else {
                            series.add((double) ((i - maxBin) * step), (double) yMean[i]);
                        }
                    } else {*/
                        if (factor == 1) {
                            series.add((double) i * step, (double) yMean[i] / pat.getSupport());
                        } else {
                            series.add((double) ((i - maxBin) * step), (double) yMean[i] / pat.getSupport());
                        }
                    //}
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
        BufferedImage image = chart.createBufferedImage(lblChart.getWidth() - 20, lblChart.getHeight() - 20);
        lblChart.setIcon(new ImageIcon(image));
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


    public void computeAllPatternStatistics() {
        // Assuming a binomial model, the average density should be given by the likelyhood * SeqNo * size of step
        // and the variance should be sqrt of that. For the time being, we assume a uniform likelihood
        int step = Integer.parseInt(jStepBox.getText());
        int rows = patterns.size();
        int maxLen = getMaxLength();
        int maxBin = maxLen / step + 1;
        // mantis issue 0000792
        //boolean isAverage = !jAvgPeakBtn.isSelected();
        int maxIdNo = 1;
        for (int i = 0; i < rows; i++) {
            DSMatchedSeqPattern pattern = (DSMatchedSeqPattern) patterns.get(i);
            if (pattern != null) {
                maxIdNo = Math.max(maxIdNo, pattern.getSupport());
            }
        }
        int maxCBin = (int) Math.log(maxIdNo) + 1;
        int[] y = new int[maxBin * 2 + 1];
        double[][] x = new double[maxCBin][];
        double[][] xx = new double[maxCBin][];
        double[] n = new double[maxCBin];
        for (int i = 0; i < maxCBin; i++) {
            x[i] = new double[maxBin];
            xx[i] = new double[maxBin];
        }
        int factor = 1;
        for (int i = 0; i < rows; i++) {
            DSMatchedSeqPattern pattern = (DSMatchedSeqPattern) patterns.get(i);
            if (pattern != null) {
                for (int j = 0; j < 2 * maxBin + 1; j++) {
                    y[j] = 0;
                }
                if (pattern instanceof org.geworkbench.util.patterns.CSMatchedSeqPattern) {
                    org.geworkbench.util.patterns.CSMatchedSeqPattern p = (CSMatchedSeqPattern) pattern;
                    for (int id = 0; id < p.getSupport(); id++) {
                        int dx = p.getOffset(id) / step;
                        if (dx < maxBin) {
                            y[dx]++;
                        }
                    }
                } else if (pattern instanceof org.geworkbench.util.patterns.FlexiblePattern) {
                    factor = 2;
                    org.geworkbench.util.patterns.FlexiblePattern p = (org.geworkbench.util.patterns.FlexiblePattern) pattern;
                    p.buildHistogram(y, step, maxBin);
                }
                // Compute the expected density
                double idNo = (double) pattern.getSupport();
                double mean = (double) pattern.getSupport() / maxLen * step / idNo;
                double sdev = Math.sqrt(mean) / Math.sqrt(idNo);

                int cBin = (int) Math.log(idNo);
                double zScore = 0;
                for (int j = 0; j < maxBin * factor; j++) {
                    double m0 = y[j] / idNo;
                    double p = (double) (m0 - mean) / (sdev + 0.000000001);
                    /* mantis issue 0000792
                    if (isAverage) {
                        if (p > 0) {
                            zScore += p;
                        }
                    } else {*/
                        zScore = Math.max(zScore, p);
                    //}
                }
                pattern.setPValue(zScore);
            }
        }
        //model.fireTableDataChanged();
    }

    void filterAction(ActionEvent e) {
        computeAllPatternStatistics();
    }

    void pairsAction(ActionEvent e) {
        /*  RepeatFilter filter = new RepeatFilter();
          int flexiSupport = Integer.parseInt(jFlexiSupportBox.getText());
          PatternTableModel model    = new PatternTableModel(null, -1);
          JTable            table    = patternTable;
          PatternTableModel model_0  = (PatternTableModel)table.getModel();
          int s1 = model_0.getPatterns().size();
          ArrayList patterns = filter.filter(model_0.getPatterns(), sequenceDB);
          s1 = patterns.size();
          for(int i = 0; i < patterns.size(); i++) {
            IPattern pat0 = (Pattern)patterns.get(i);
            if (pat0.getClass().isAssignableFrom(Pattern.class)) {
              Pattern p0 = (Pattern) pat0;
              for(int j = i + 1; j < patterns.size(); j++) {
                IPattern pat1 = (Pattern)patterns.get(j);
                if (pat1.getClass().isAssignableFrom(Pattern.class)) {
                  Pattern p1 = (Pattern) pat1;
                  FlexiPattern fp = new FlexiPattern(p0, p1);
                  int size = fp.getSupport();
                  if(size >  flexiSupport) {
                    model.addPattern(fp);
                  }
                }
              }
            }
          }
          table.setModel(model);
          model.fireTableDataChanged(); */
    }

    void jStepBox_actionPerformed(ActionEvent e) {

    }


    public void sequenceDiscoveryTableRowSelected(SequenceDiscoveryTableEvent e) {

        //    setSequenceDB(e.getSequenceDB());
        //    setPatterns(e.getPatterns());
        //XQ changed to fix bug 252
        setPatterns(e.getPatternMatchCollection());
    }

//    public void setPatterns(DSMatchedSeqPattern[] _patterns) {
//        patterns.clear();
//        for (int i = 0; i < _patterns.length; i++) {
//            patterns.add(_patterns[i]);
//        }
//    }

    public void setPatterns(DSCollection<DSMatchedPattern<DSSequence, CSSeqRegistration>> matches) {
        patterns.clear();
        for (int i = 0; i < matches.size(); i++) {
            patterns.add(matches.get(i));
        }
    }


    public void setSequenceDB(DSSequenceSet sDB) {
        sequenceDB = sDB;
    }

    public DSSequenceSet getSequenceDB() {
        return sequenceDB;
    }

    void jAvgPeakBtn_actionPerformed(ActionEvent e) {

    }
}
