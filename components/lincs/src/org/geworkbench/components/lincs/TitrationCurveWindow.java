package org.geworkbench.components.lincs;

import java.util.List;
import javax.swing.JFrame;
 
import java.awt.Dimension;  
 
 
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.geworkbench.service.lincs.LincsServiceException_Exception;
import org.geworkbench.service.lincs.data.xsd.TitrationCurveData;
import org.geworkbench.service.lincs.data.xsd.ArrayOffloat;
 

/**
 * 
 * @author my2248
 * @version $Id: NetworkRedrawWindow.java 9734 2012-07-24 14:24:57Z zji $
 */
public class TitrationCurveWindow {

	 
    private Long levelTwoId;
	
	public TitrationCurveWindow(Long levelTwoId) {
		this.levelTwoId = levelTwoId;
		display();
	}

	/**
	 * Set up the GUI
	 * 
	 * @param void
	 * @return void
	 */
	 
	 private void display() {
		 
		    try {
				 TitrationCurveData data = LincsInterface.getLincsService().getTitrationCurveData(levelTwoId);
				 String title = "Titration of \"" + data.getCompound2Name() + "\"(drug2) against \"" + data.getCompound1Name() + "\"";
			     JFrame frame = new JFrame("");
			        
			     
			     XYSeriesCollection dataset = new XYSeriesCollection();
			     List<ArrayOffloat> measurements = data.getCompoundMeasMatrix();  
			     List<Float> compound1ConcList = data.getCompound1ConcList();
			     List<Float> compound2ConcList = data.getCompound2ConcList();
				  
			     for(int i =0; i<measurements.size(); i++)
			     {
			    	  XYSeries series = new XYSeries("\""+ data.getCompound1Name() +"\" - " + compound1ConcList.get(i) + " uM");
			     
			          for (int j = 1; j < measurements.get(i).getArray().size(); j++) {
			            series.add(compound2ConcList.get(j), measurements.get(i).getArray().get(j));
			          }
			        
			         dataset.addSeries(series);
			     }
			        NumberAxis range = new NumberAxis("cellular response");
			        NumberAxis domain = new NumberAxis("drug 2 concentration (uM)");
			        XYSplineRenderer r = new XYSplineRenderer(5);
			        XYPlot xyplot = new XYPlot(dataset, domain, range, r);
			        JFreeChart chart = new JFreeChart(xyplot);
			        chart.setTitle(title);
			        ChartPanel chartPanel = new ChartPanel(chart){
		 
						private static final long serialVersionUID = 1L;

						@Override
			            public Dimension getPreferredSize() {
			                return new Dimension(640, 480);
			            }
			        };
			       
			        frame.add(chartPanel);
			        frame.pack();
			        frame.setLocationRelativeTo(frame.getOwner());
			        frame.setVisible(true);      
			       
			        
		    } catch (LincsServiceException_Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      
	        
	    }
	
}
