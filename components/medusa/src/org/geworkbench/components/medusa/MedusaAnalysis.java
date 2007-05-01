package org.geworkbench.components.medusa;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;

/**
 * 
 * @author keshav
 * @version $Id: MedusaAnalysis.java,v 1.2 2007-05-01 19:02:47 keshav Exp $
 */
public class MedusaAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * 
	 */
	public MedusaAnalysis() {
		setLabel("MEDUSA");
		setDefaultPanel(new MedusaParamPanel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getAnalysisName()
	 */
	@Override
	public String getAnalysisName() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonParameters()
	 */
	@Override
	public Map<String, Object> getBisonParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractAnalysis#getAnalysisType()
	 */
	@Override
	public int getAnalysisType() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.bison.model.analysis.Analysis#execute(java.lang.Object)
	 */
	public AlgorithmExecutionResults execute(Object input) {
		MedusaParamPanel params = (MedusaParamPanel) aspp;

		double base = params.getIntervalBase();

		double bound = params.getIntervalBound();

		int boosting = params.getBoostingIterations();

		log.info("executing ...");

		return null;
	}

}
