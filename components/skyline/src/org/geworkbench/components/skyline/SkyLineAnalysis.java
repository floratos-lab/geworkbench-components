package org.geworkbench.components.skyline;

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
import org.geworkbench.bison.model.analysis.ProteinStructureAnalysis;

/**
 * Run SkyLine analysis on grid service on web1 Replaces all values less (or
 * more) than a user designated Threshold X with the value X.
 * 
 * @author mw2518
 * @author zji
 * @version $Id: SkyLineAnalysis.java,v 1.8 2009-09-10 16:40:26 chiangy Exp $
 * 
 */
public class SkyLineAnalysis extends AbstractGridAnalysis implements
		ProteinStructureAnalysis {
	private static final long serialVersionUID = 5531166361344848544L;
	Log log = LogFactory.getLog(SkyLineAnalysis.class);

	private final String analysisName = "SkyLine";
	
	protected static final int MINIMUM = 0;
	protected static final int MAXIMUM = 1;
	protected static final int IGNORE = 0;
	protected static final int REPLACE = 1;

	private	SkyLineConfigPanel slp;

	public SkyLineAnalysis() {
		slp = new SkyLineConfigPanel();
		setDefaultPanel(slp);
	}

	public int getAnalysisType() {
		return SKYLINE_TYPE;
	}

	/** implements org.geworkbench.bison.model.analysis.Analysis.execute */
	public AlgorithmExecutionResults execute(Object input) {
		// inform the user that only remote service is available
		return new AlgorithmExecutionResults(
				false,
				"SkyLine does not have a local algorithm service.  Please choose \"Grid\" on the Services tab.",
				null);
	}

	@Override
	public String getAnalysisName() {
    	return analysisName;
	}

	@Override
	protected Map<Serializable, Serializable> getBisonParameters() {
		Map<Serializable, Serializable> bisonParameters = new HashMap<Serializable, Serializable>();
		SkyLineConfigPanel paramPanel= (SkyLineConfigPanel) this.aspp;
		
		bisonParameters.put("chain", paramPanel.getchainValue()); // String
		bisonParameters.put("pbOnInput", paramPanel.getrun_pb1Value()); // String
		bisonParameters.put("pbOnHomolugous", paramPanel.getrun_pb2Value()); // String 
		bisonParameters.put("sequenceDatabase", paramPanel.getdValue()) ; // String
		bisonParameters.put("chosenSpecies", paramPanel.getchosen_speciesValue()); /// String
		bisonParameters.put("roundToRun", paramPanel.getjValue()); /// int
		bisonParameters.put("maximumNumber", paramPanel.getbValue()); /// int
		bisonParameters.put("inclusionThreshold", paramPanel.gethValue()); // double
		bisonParameters.put("expectation", paramPanel.geteValue()); // double
		bisonParameters.put("filter", paramPanel.getfValue()); // String
		bisonParameters.put("redundancyLevel", paramPanel.getredundancy_levelValue()); // int
		bisonParameters.put("runModeller", paramPanel.getrun_modellerValue()); // String
		bisonParameters.put("modelNumber", paramPanel.getmodel_numberValue()); // int
		bisonParameters.put("hetatm", paramPanel.gethetatmValue()); // String
		bisonParameters.put("clustal", paramPanel.getclustalValue()); // String

		return bisonParameters;
	}

	@Override
	public Class<?> getBisonReturnType() {
		return String.class;
	}

	@Override
	protected boolean useMicroarraySetView() {
		return false;
	}

	@Override
	protected boolean useOtherDataSet() {
		return true;
	}

	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet<?> refMASet) {
		// TODO Auto-generated method stub
		return new ParamValidationResults(true, null);
	}

}
