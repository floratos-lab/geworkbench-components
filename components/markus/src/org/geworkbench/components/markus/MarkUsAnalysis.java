package org.geworkbench.components.markus;

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
 * MarkUs Analysis.

 * @author meng
 * @author zji
 * @version $Id: MarkUsAnalysis.java,v 1.7 2009-09-10 16:40:26 chiangy Exp $
 */
public class MarkUsAnalysis extends AbstractGridAnalysis implements ProteinStructureAnalysis 
{
	private static final long serialVersionUID = -4702468130439199874L;
	Log log = LogFactory.getLog(MarkUsAnalysis.class);

	private final String analysisName = "MarkUs";
	
    private MarkUsConfigPanel mcp;

    public MarkUsAnalysis() {
		mcp = new MarkUsConfigPanel();
		setDefaultPanel(mcp);
    }

    /** implements org.geworkbench.analysis.AbstractAnalysis.getAnalysisType */
    public int getAnalysisType() {
        return MARKUS_TYPE;
    }

    /** implements org.geworkbench.bison.model.analysis.Analysis.execute */
    public AlgorithmExecutionResults execute(Object input) {
		// inform the user that only remote service is available
		return new AlgorithmExecutionResults(
				false,
				"MarkUs does not have a local algorithm service.  Please choose \"Grid\" on the Services tab.",
				null);
    }
    
    @Override
	public String getAnalysisName() {
    	return analysisName;
	}

	@Override
	protected Map<Serializable, Serializable> getBisonParameters() {
		log.debug("Reading bison parameters");

		Map<Serializable, Serializable> bisonParameters = new HashMap<Serializable, Serializable>();
		MarkUsConfigPanel paramPanel = (MarkUsConfigPanel) this.aspp;

		// main - all booleans
		bisonParameters.put("skan", paramPanel.getskanValue());
		bisonParameters.put("dali", paramPanel.getdaliValue());
		bisonParameters.put("screen", paramPanel.getscreenValue());
		bisonParameters.put("delphi", paramPanel.getdelphiValue());
		bisonParameters.put("psi_blast", paramPanel.getpsiblastValue());
		bisonParameters.put("ips", paramPanel.getipsValue());
		bisonParameters.put("consurf", paramPanel.getconsurfValue());
		bisonParameters.put("consurf3", paramPanel.getconsurf3Value());
		bisonParameters.put("consurf4", paramPanel.getconsurf3Value());
		// string
		bisonParameters.put("chain", paramPanel.getChain());

		// delphi
		bisonParameters.put("grid_size", paramPanel.getgridsizeValue()); // int
		bisonParameters.put("box_fill", paramPanel.getboxfillValue()); // int
		bisonParameters.put("steps", paramPanel.getstepsValue()); // int
		bisonParameters.put("sc", paramPanel.getscValue()); // double
		bisonParameters.put("radius", paramPanel.getradiusValue()); // double
		bisonParameters.put("ibc", paramPanel.getibcValue()); // int
		bisonParameters.put("nli", paramPanel.getnliValue()); // int
		bisonParameters.put("li", paramPanel.getliValue()); // int
		bisonParameters.put("idc", paramPanel.getidcValue()); // int
		bisonParameters.put("edc", paramPanel.getedcValue()); // int

		// analysis 3
		if(paramPanel.getconsurf3Value()) {
			bisonParameters.put("csftitle3", paramPanel.getcsftitle3Value()); // String
			bisonParameters.put("eval3", paramPanel.geteval3Value()); // double
			bisonParameters.put("iter3", paramPanel.getiter3Value()); // int
			bisonParameters.put("filter3", paramPanel.getfilter3Value()); // int
			bisonParameters.put("msa3", paramPanel.getmsa3Value()); // String
		}

		// analysis 4
		if(paramPanel.getconsurf4Value()) {
			bisonParameters.put("csftitle4", paramPanel.getcsftitle4Value()); // String
			bisonParameters.put("eval4", paramPanel.geteval4Value()); // double
			bisonParameters.put("iter4", paramPanel.getiter4Value()); // int
			bisonParameters.put("filter4", paramPanel.getfilter4Value()); // int
			bisonParameters.put("msa4", paramPanel.getmsa4Value()); // String
		}
		return bisonParameters;	}

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

	@SuppressWarnings("unchecked")
	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet refMASet) {
		// TODO Auto-generated method stub
		return new ParamValidationResults(true, null);
	}
	
	@Override
	public boolean isAuthorizationRequired() {
		return false;
	}

}

