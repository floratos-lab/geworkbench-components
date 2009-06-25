package org.geworkbench.components.gpmodule.gsea;

import org.geworkbench.components.gpmodule.GPAnalysis;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: nazaire
 */
public class GSEAAnalysis extends GPAnalysis
{
    private static Log log = LogFactory.getLog(GSEAAnalysis.class);

    public GSEAAnalysis()
    {
        setLabel("GSEA Analysis");
        panel = new GSEAAnalysisPanel();
        setDefaultPanel(panel);
    }

    public AlgorithmExecutionResults execute(Object input)
    {
        assert (input instanceof DSMicroarraySetView);
        DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;

        return null;
    }
}
