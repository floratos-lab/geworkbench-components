package org.geworkbench.components.netboost;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

/**
 * NetBoost Analysis
 * @author ch2514
 * @version $Id: NetBoostAnalysis.java,v 1.3 2007-10-19 00:28:45 hungc Exp $
 */

public class NetBoostAnalysis extends AbstractGridAnalysis implements ClusteringAnalysis {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
	
	public NetBoostAnalysis(){
		this.localAnalysisType = AbstractAnalysis.NETBOOST_TYPE;
		setLabel("NetBoostAnalysis");
		paramPanel = new NetBoostParamPanel();
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
		// inform the user that only remote service is available
		return new AlgorithmExecutionResults(false
				, "Net Boost does not have a local algorithm service.  Please choose \"Grid\" on the Net Boost analysis panel."
				, null
				);
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
    
    @Subscribe 
    public void receive(ProjectEvent projectEvent, Object source) {
    	log.debug("NetBoost Analysis received project event.");
    }	
}
