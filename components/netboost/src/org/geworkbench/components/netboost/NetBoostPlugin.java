package org.geworkbench.components.netboost;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
import java.util.*;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;
import org.jfree.chart.axis.*;

import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.util.pathwaydecoder.mutualinformation.*;

/**
 * NetBoost Plugin
 * @author ch2514
 * @version $Id: NetBoostPlugin.java,v 1.2 2007-08-31 21:16:00 hungc Exp $
 */

public class NetBoostPlugin extends JPanel {
	// variable
	private static Log log = LogFactory.getLog(NetBoostPlugin.class);
	
	private static final String BOOST_ITERATION_TITLE = "Net Boost";
	private static final String BOOST_ITERATION_X_LABEL = "Boosting Iterations";
	private static final String BOOST_ITERATION_Y_LABEL = "Losses";
	private static final String SCORES_TABLE_TITLE = "Scores";
	private static final String SCORES_TABLE_MODEL = "Model";
	private static final String SCORES_TABLE_DATA = "Score +/- Variance";
	private static final String CONFUSION_MATRIX_TITLE = "Confusion Matrix";
	private static final String CONFUSION_MATRIX_X_LABEL = "PREDICTIONS";
	private static final String CONFUSION_MATRIX_Y_LABEL = "TRUTH";	
	
	JFreeChart iterChart;
	ChartPanel iterChartPanel;
	JButton imageSnapshotButton;
	JTable scoresTable, confusionMatrix;
	
	NetBoostData nbdata;
	DefaultTableXYDataset iterDataSet;
	ScoresModel scoresModel;
	ConfusionModel confusedModel;
	
	public NetBoostPlugin(NetBoostDataSet data, NetBoostVisualComponent visualPlugin){		
		final NetBoostVisualComponent vp = visualPlugin;
		this.nbdata = data.getData();
		
		// init internal panels
		JPanel leftPanel = new JPanel(new BorderLayout());
		JPanel leftBottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel rightPanel = new JPanel(new GridLayout(2,1,20,20));
		JPanel rightTopPanel = new JPanel(new BorderLayout());
		JScrollPane rightTopTablePanel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
				, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JPanel rightBottomPanel = new JPanel(new BorderLayout());
		JScrollPane rightBottomTablePanel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
				, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JPanel rightBottomTitlePanel = new JPanel(new GridLayout(2, 1));
		
		// boost iteration chart
		iterDataSet = new DefaultTableXYDataset();
		iterChart = ChartFactory.createXYLineChart(BOOST_ITERATION_TITLE //+ "(" + data.getFilename() + ")"
				, BOOST_ITERATION_Y_LABEL, BOOST_ITERATION_X_LABEL
				, new IterDataSet().getDataSet(), PlotOrientation.VERTICAL
				, true, true, false);
		XYPlot plot = iterChart.getXYPlot();
        NumberAxis domainAxis = new NumberAxis(BOOST_ITERATION_X_LABEL);
        NumberAxis rangeAxis = new NumberAxis(BOOST_ITERATION_Y_LABEL);
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        iterChart.setBackgroundPaint(Color.white);
        plot.setOutlinePaint(Color.black);
		iterChartPanel = new ChartPanel(iterChart);
		iterChartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		imageSnapshotButton = new JButton("   Image Snapshot   ");
		imageSnapshotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				vp.createImageSnapshot(iterChartPanel);
			}
		});

		leftBottomPanel.add(imageSnapshotButton);
		leftPanel.add(iterChartPanel, BorderLayout.CENTER);
		leftPanel.add(leftBottomPanel, BorderLayout.SOUTH);
		
		// scores table
		JLabel scoresTableTitle = new JLabel(SCORES_TABLE_TITLE, SwingConstants.LEADING);
		scoresTableTitle.setFont(new Font("Arial", Font.BOLD, 12));
		scoresModel = new ScoresModel();
		scoresTable = new JTable(scoresModel);
		scoresTable.setAutoCreateColumnsFromModel(false);
		
		rightTopTablePanel.getViewport().add(scoresTable);
		rightTopPanel.add(scoresTableTitle, BorderLayout.NORTH);
		rightTopPanel.add(rightTopTablePanel, BorderLayout.CENTER);
		
		
		// confusion matrix
		JLabel confusedTitle = new JLabel(CONFUSION_MATRIX_TITLE, SwingConstants.LEADING);
		JLabel confusedY = new JLabel(CONFUSION_MATRIX_Y_LABEL, SwingConstants.CENTER);
		JLabel confusedX = new JLabel(CONFUSION_MATRIX_X_LABEL, SwingConstants.CENTER);
		confusedTitle.setFont(new Font("Arial", Font.BOLD, 12));
		confusedModel = new ConfusionModel();
		confusionMatrix = new JTable(confusedModel);
		confusionMatrix.setAutoCreateColumnsFromModel(false);		
		
		rightBottomTitlePanel.add(new JPanel(new FlowLayout(FlowLayout.LEADING)).add(confusedTitle));
		rightBottomTitlePanel.add(new JPanel(new FlowLayout(FlowLayout.CENTER)).add(confusedX));
		rightBottomTablePanel.getViewport().add(confusionMatrix);
		rightBottomPanel.add(rightBottomTitlePanel, BorderLayout.NORTH);
		rightBottomPanel.add(rightBottomTablePanel, BorderLayout.CENTER);
		
		// arrange internal panels
		rightPanel.add(rightTopPanel);
		rightPanel.add(rightBottomPanel);
		
		this.setLayout(new GridLayout(1, 2));
		this.add(new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
				, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED).getViewport().add(leftPanel));
		this.add(rightPanel);
	}
	
	private class IterDataSet {
		private static final String TEST_LOSS = "Test Loss";
		private static final String TRAIN_LOSS = "Train Loss";
		private static final String BOUND = "Bound";
		private static final String BIAS = "Bias";
		
		private XYSeriesCollection dataset;
		private XYSeries testLossSeries, trainLossSeries, boundSeries, biasSeries;
		
		public IterDataSet(){
			testLossSeries = new XYSeries(TEST_LOSS);
			trainLossSeries = new XYSeries(TRAIN_LOSS);
			boundSeries = new XYSeries(BOUND);
			biasSeries = new XYSeries(BIAS);
			
			nbdata.fillIterChartData(testLossSeries, trainLossSeries);
			
			dataset = new XYSeriesCollection();
	        dataset.addSeries(testLossSeries);
	        dataset.addSeries(trainLossSeries);
	        //dataset.addSeries(boundSeries);
	        //dataset.addSeries(biasSeries);
		}
		
		public XYSeriesCollection getDataSet(){
			return this.dataset;
		}
	}
	
	
	private class ScoresModel extends AbstractTableModel {
		private String[] columnNames = {SCORES_TABLE_MODEL, SCORES_TABLE_DATA};
	    private String[] models = {"LPA", "RDG", "RDS", "DMC", "AGV", "SMW", "DMR"};
	    private double[] scores = nbdata.getScores();
	    private double[] variances = nbdata.getVariances();
	    
	    public ScoresModel(){
	    	models = nbdata.getModels();
	    }
	    
	    public int getColumnCount() {
	        return 2;
	    }

	    public int getRowCount() {
	        return 7;
	    }

	    public String getColumnName(int col) {
	    	return columnNames[col];
	    }

	    public Object getValueAt(int row, int col) {
	    	if(col == 0)
	    		return models[row];
	        if(col == 1)
	        	return new String(scores[row] + " +/- " + variances[row]);
	        
	        return null;
	    }

	    public Class getColumnClass(int c) {
	        return getValueAt(0, c).getClass();
	    }
	    
	    public void setValueAt(Object value, int row, int col) {
	        // do nothing for now       
	    }
	    
	}
	
	private class ConfusionModel extends AbstractTableModel {
		private String[] columnNames = {CONFUSION_MATRIX_Y_LABEL, "LPA", "RDG", "RDS", "DMC", "AGV", "SMW", "DMR"};
		private String[] rowNames = {"LPA", "RDG", "RDS", "DMC", "AGV", "SMW", "DMR"};
		private double[][] data;
		
		public ConfusionModel(){
			data = nbdata.getConfusedData();
			String[] s = nbdata.getModels();
			if((s != null) && (s.length > 0)){
				rowNames = s;
				columnNames = new String[rowNames.length + 1];
				columnNames[0] = CONFUSION_MATRIX_Y_LABEL;
				for(int i = 0; i < rowNames.length; i++){
					columnNames[i+1] = rowNames[i];
				}
			}
		}
		
		public int getColumnCount() {
	        return 8;
	    }

	    public int getRowCount() {
	        return 7;
	    }

	    public String getColumnName(int col) {
	    	return columnNames[col];
	    }

	    public Object getValueAt(int row, int col) {
	    	if(col == 0)
	    		return rowNames[row];
	    	else
	        	return new String(data[col - 1][row] + "%");
	    }

	    public Class getColumnClass(int c) {
	        return getValueAt(0, c).getClass();
	    }
	    
	    public void setValueAt(Object value, int row, int col) {
	        // do nothing for now
	    }
	}
}


