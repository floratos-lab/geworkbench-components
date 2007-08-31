package org.geworkbench.components.netboost;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.AdjacencyMatrixEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.components.netboost.NetBoostParamPanel;
import org.geworkbench.util.pathwaydecoder.mutualinformation.*;
import org.geworkbench.util.threading.*;
import org.geworkbench.bison.datastructure.biocollections.*;
import org.geworkbench.builtin.projects.DataSetNode;
import org.geworkbench.components.parsers.EdgeListFileFormat;

/**
 * NetBoost Analysis
 * @author ch2514
 * @version $Id: NetBoostAnalysis.java,v 1.1 2007-08-31 16:05:59 hungc Exp $
 */

public class NetBoostAnalysis extends AbstractGridAnalysis implements ClusteringAnalysis {
	// variable
	private Log log = LogFactory.getLog(this.getClass());
	
	public static final String TRAINING_EX = "trainingExample";
	public static final String BOOST_ITER = "boostingIteration";
	public static final String SUBGRAPH_COUNT = "subgraphCounting";
	public static final String CROSS_VALID = "crossValidationFolds";
	public static final String LPA = "lpa";
	public static final String RDG = "rdg";
	public static final String RDS = "rds";
	public static final String DMC = "dmc";
	public static final String AGV = "agv";
	public static final String SMW = "smw";
	public static final String DMR = "dmr";
	
	private int localAnalysisType;
	private final String analysisName = "NetBoost";
	private NetBoostParamPanel paramPanel;
	private String paramDesc;
	private EdgeListDataSet el;
	private NetBoostDataSet netboostDataSet;
	
	private JDialog dialog;
    private JProgressBar progressBar;
    private JButton cancelButton;
    private Task task;
    
    private boolean hasData = false;
	
	public NetBoostAnalysis(){
		this.localAnalysisType = AbstractAnalysis.ZERO_TYPE;
		setLabel("NetBoost Analysis");
		paramPanel = new NetBoostParamPanel();
		paramDesc = "";
		setDefaultPanel(paramPanel);
	}
	
	public Map<String, Object> getBisonParameters(){
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		
		parameterMap.put(TRAINING_EX, new Integer(((NetBoostParamPanel) aspp).getTrainingExamples()));
		parameterMap.put(BOOST_ITER, new Integer(((NetBoostParamPanel) aspp).getBoostingIterations()));
		parameterMap.put(SUBGRAPH_COUNT, ((NetBoostParamPanel) aspp).getSubgraphCountingMethods());
		parameterMap.put(CROSS_VALID, new Integer(((NetBoostParamPanel) aspp).getCrossValidationFolds()));
		parameterMap.put(LPA, new Boolean(((NetBoostParamPanel) aspp).getLPA()));
		parameterMap.put(RDG, new Boolean(((NetBoostParamPanel) aspp).getRDG()));
		parameterMap.put(RDS, new Boolean(((NetBoostParamPanel) aspp).getRDS()));
		parameterMap.put(DMC, new Boolean(((NetBoostParamPanel) aspp).getDMC()));
		parameterMap.put(AGV, new Boolean(((NetBoostParamPanel) aspp).getAGV()));
		parameterMap.put(SMW, new Boolean(((NetBoostParamPanel) aspp).getSMW()));
		parameterMap.put(DMR, new Boolean(((NetBoostParamPanel) aspp).getDMR()));

		return parameterMap;
	}
	
	public String getAnalysisName(){
		return this.analysisName;
	}

	public AlgorithmExecutionResults execute(Object input){
		
		
		
		// checking input object
		System.out.println("\tchecking input obj");
		if (input == null) {
			return new AlgorithmExecutionResults(false, "Invalid input: No data", null);
		} 
		AlgorithmExecutionResults result = null;
		
		if (input instanceof EdgeListDataSet) {
            log.debug("Input dataset is an edge list.");  
            el = (EdgeListDataSet) input;
            
            // checking file extension 
    		
    		System.out.println("\tchecking file ext");
    		boolean correctExt = false;
    		String filename = el.getFilename();
    		System.out.println("\tfilename=" + filename);
    		for(String ext: EdgeListFileFormat.EDGE_LIST_FILE_EXTENSIONS){
    			if(filename.endsWith(ext)) {
    				correctExt = true;
    				break;
    			}
    		}
    		if(!correctExt){
    			for(String ext: EdgeListFileFormat.ADJACENCY_MATRIX_FILE_EXTENSIONS){
    				if(filename.endsWith(ext)) {
    					correctExt = true;
    					break;
    				}
    			}
    		}
    		if(!correctExt){
    			return new AlgorithmExecutionResults(false, "Invalid input: Only .txt and .adj edge list files are valid inputs.", null);
    		}           
                        
        } else {
        	return new AlgorithmExecutionResults(false, "Invalid input: Only edge list files are valid inputs.", null);
        }
		
		// run NetBoost algorithm in the background
        // and display an indeterminate progress bar in the foreground
        createProgressBarDialog();
        task = new Task(el.getFilename());        
        task.execute();     
        dialog.setVisible(true);        
        
        if(hasData){
        	Map bison = this.getBisonParameters();
        	paramDesc += "\nNETBOOST\ntraining example: " + bison.get(NetBoostAnalysis.TRAINING_EX) + "\n"
        		+ "boosting iteration: " + bison.get(NetBoostAnalysis.BOOST_ITER) + "\n"
        		+ "subgraph counting method: " + bison.get(NetBoostAnalysis.SUBGRAPH_COUNT) + "\n"
        		+ "cross-validation folds: " + bison.get(NetBoostAnalysis.CROSS_VALID) + "\n"
        		+ NetBoostAnalysis.LPA + ": " + bison.get(NetBoostAnalysis.LPA) + "\n"
        		+ NetBoostAnalysis.RDG + ": " + bison.get(NetBoostAnalysis.RDG) + "\n"
        		+ NetBoostAnalysis.RDS + ": " + bison.get(NetBoostAnalysis.RDS) + "\n"
        		+ NetBoostAnalysis.DMC + ": " + bison.get(NetBoostAnalysis.DMC) + "\n"
        		+ NetBoostAnalysis.AGV + ": " + bison.get(NetBoostAnalysis.AGV) + "\n"
        		+ NetBoostAnalysis.SMW + ": " + bison.get(NetBoostAnalysis.SMW) + "\n"
        		+ NetBoostAnalysis.DMR + ": " + bison.get(NetBoostAnalysis.DMR)        	
        		;
	        ProjectPanel.addToHistory(new CSDataSet(), paramDesc);
	        log.info(paramDesc);
	        
	        // temporary		
	        if(this.netboostDataSet != null){
		        ProjectPanel.addToHistory(this.netboostDataSet, paramDesc);
		        return new AlgorithmExecutionResults(true, "NetBoost results", this.netboostDataSet);
	        } else {
	        	JOptionPane.showMessageDialog(paramPanel.getParent(), "Cannot analyze NetBoost data.", "NetBoost Analyze Error", JOptionPane.WARNING_MESSAGE);
	        	return null;
	        }
        } else {
        	JOptionPane.showMessageDialog(paramPanel.getParent(), "Cannot analyze NetBoost data.", "NetBoost Analyze Error", JOptionPane.WARNING_MESSAGE);
        	return null;
        }
	}
	
	public int getAnalysisType(){
		return this.localAnalysisType;
	}
	
	/**
	 * <code>AbstractAnalysis</code> method
	 * 
	 * @return Analysis type
	 */
	public String getType() {
		return this.analysisName;
	}
	
    @Publish
    public ProjectEvent publishProjectEvent(ProjectEvent event) {
        return event;
    }
    
    @Subscribe 
    public void receive(ProjectEvent projectEvent, Object source) {
    	log.debug("NetBoost Analysis received project event.");
    	/*
    	Object o = ((ProjectPanel) source).getSelection().getSelectedNode();
    	if(o instanceof DataSetNode){
	    	node = (DataSetNode) o;
	    	System.out.println("NetBoost Analysis received data node: " + node.dataFile.getLabel() 
	    			+ ", dataSetName=" + node.dataFile.getDataSetName()
	    			+ ", fileName=" + node.getFileName()
	    			);
    	}  	*/
    }
    
    private void createProgressBarDialog(){
    	// lay the groundwork for the progress bar dialog
        dialog = new JDialog();
        progressBar = new JProgressBar();
        cancelButton = new JButton("Cancel");
        dialog.setLayout(new BorderLayout());
        dialog.setModal(true);
        dialog.setTitle("NetBoost Process Running");
        dialog.setSize(300, 50);
        dialog.setLocation((int) (dialog.getToolkit().getScreenSize().getWidth() - dialog.getWidth()) / 2, (int) (dialog.getToolkit().getScreenSize().getHeight() - dialog.getHeight()) / 2);
        progressBar.setIndeterminate(true);
        dialog.add(progressBar, BorderLayout.CENTER);
        dialog.add(cancelButton, BorderLayout.EAST);
        
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
            	if((task != null) && (!task.isCancelled()) && (!task.isDone())) {
            		task.cancel(true);
            		log.info("Cancelling NetBoost Analysis");
            		System.out.println("\tCancelling NetBoost Analysis.");
            	}
            	dialog.setVisible(false);
            	dialog.dispose();            	
            }
        });

        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
            	if((task != null) && (!task.isCancelled()) && (!task.isDone())){
            		task.cancel(true);
            		log.info("Cancelling NetBoost Analysis");
            		System.out.println("\tCancelling NetBoost Analysis.");
            	}
            }
        });
    }
    
    /**
     * The swing worker class that runs NetBoost analysis in the background.
     * @author ch2514
     * @version $Id: NetBoostAnalysis.java,v 1.1 2007-08-31 16:05:59 hungc Exp $
     */
    class Task extends SwingWorker<NetBoostDataSet, Void> {
    	String filename;
    	
    	/**
    	 * Constructor.
    	 * Takes in all the arguments required to run the NetBoost algorithm.
    	 * 
    	 */
    	public Task(String filename){
    		this.filename = filename;
    	}
    	
    	/**
    	 * Runs NetBoost analysis.
    	 * @return a mindy data set.  If the analysis fails, returns null.
    	 */
    	public NetBoostDataSet doInBackground(){
    		log.info("Running NetBoost analysis.");
    		boolean results = false;
    		
            try{
            	// temporary
            	Thread.sleep((long) (10000 * Math.random()));
            	results = true;
            	
            } catch (Exception e){
            	log.error("Cannot analyze data.", e);            	
            	return null;
            }            
            log.info("NetBoost analysis complete.  Converting NetBoost results.");
            
            NetBoostData loadedData = new NetBoostData();
            NetBoostDataSet dataSet = new NetBoostDataSet(null, "NetBoost Results", loadedData, this.filename);
            log.info("Done converting NetBoost results.");
            
            return dataSet;
    	}
    	
    	/**
    	 * When the NetBoost analysis finishes, transfer the resulting NetBoost
    	 * data set back to the NetBoost analysis panel on the event thread.
    	 * Also disposes the progress bar dialog box.
    	 */
    	public void done(){
    		if(!this.isCancelled()){
	    		try{
	    			netboostDataSet = get(); 
	    			hasData = true;
	    			log.debug("Transferring mindy data set back to event thread.");   
	    		} catch (Exception e) {
	    			hasData = false;
	    			log.error("Exception in finishing up worker thread that called NetBoost: " + e.getMessage());
	    		}
    		} else {
    			System.out.println("\tNetBoost task cancelled.");
    		}
    		dialog.setVisible(false);
    		dialog.dispose();
    		log.debug("Closing progress bar dialog.");
    	}
    	
    }
	
}
