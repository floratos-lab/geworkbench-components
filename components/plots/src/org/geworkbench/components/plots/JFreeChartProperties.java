package org.geworkbench.components.plots;

import java.awt.Paint;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;

class JFreeChartProperties{
	
	private final boolean antiAlias;
	private final Paint backgroundPaint;
		
	private final TextTitle textTitle;
		
	private final boolean  domainAxisLineVisible;
	private final boolean  domainTickLabelsVisible;
	private final boolean  domainTickMarksVisible;
	private final boolean  domainVisible;

	private final boolean  rangeAxisLineVisible;
	private final boolean  rangeTickLabelsVisible;
	private final boolean  rangeTickMarksVisible;
	private final boolean  rangeVisible;
		
	private final Paint paint;
	private final PlotOrientation plotOrientation;
		
	private final boolean  domainCrosshairLockedOnData;
	private final boolean  domainCrosshairVisible; 
	private final boolean  domainGridlinesVisible; 
		
	private final boolean  rangeCrosshairLockedOnData;
	private final boolean  rangeCrosshairVisible;
	private final boolean  rangeGridlinesVisible;
	private final boolean  rangeZeroBaselineVisible;
		
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