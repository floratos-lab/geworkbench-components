package org.geworkbench.components.skybase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.analysis.ProteinSequenceAnalysis;

/**
 * AbstractGridAnalysis for blast skybase on grid service on web1
 * 
 * @author mw2518
 * @version $Id: SkyBaseAnalysis.java,v 1.7 2009-09-10 16:40:26 chiangy Exp $
 *
 */
public class SkyBaseAnalysis extends AbstractGridAnalysis implements
		ProteinSequenceAnalysis {
	private Log log = LogFactory.getLog(this.getClass());
	private static final long serialVersionUID = 1L;
	SkyBaseConfigPanel scp;
	String seqname, seqfilename, seqcontent;

	SkyBaseAnalysis() {
		scp = new SkyBaseConfigPanel();
		setDefaultPanel(scp);
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.bison.model.analysis.Analysis#execute(java.lang.Object)
	 */
	public AlgorithmExecutionResults execute(Object input) {
		// inform the user that only remote service is available
		return new AlgorithmExecutionResults(
				false,
				"SkyBase does not have a local algorithm service.  Please choose \"Grid\" on the SkyBase analysis panel.",
				null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractAnalysis#getAnalysisType()
	 */
	public int getAnalysisType() {
		return SKYBASE_TYPE;
	}

	public String getType() {
		return "SkyBaseAnalysis";
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getAnalysisName()
	 */
	public String getAnalysisName() {
		return "SkyBase";
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#useMicroarraySetView()
	 */
	public boolean useMicroarraySetView() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#useOtherDataSet()
	 */
	public boolean useOtherDataSet() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonReturnType()
	 */
	public Class<?> getBisonReturnType() {
		return String.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonParameters()
	 */
	public Map<Serializable, Serializable> getBisonParameters() {
		Map<Serializable, Serializable> parameterMap = new HashMap<Serializable, Serializable>();

		String cfgcommand = scp.getmincovValue() + " " + scp.getminsidValue()
				+ " " + scp.getrphitsValue() + " " + scp.getdatabase();
		log.info("blastskybaseparam: " + cfgcommand);
		parameterMap.put("skybaseParameter", cfgcommand);

		return parameterMap;
	}

	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet<?> refMASet) {
		return new ParamValidationResults(true, "Not Checked");
	}

	@Override
	public boolean isAuthorizationRequired() {
		return false;
	}
}
