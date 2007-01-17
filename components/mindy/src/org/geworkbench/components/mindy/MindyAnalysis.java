package org.geworkbench.components.mindy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyDataSet;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;

import java.util.Observer;
import java.io.File;
import java.io.IOException;

/**
 * @author Matt Hall
 */
public class MindyAnalysis extends AbstractAnalysis implements ClusteringAnalysis {
    Log log = LogFactory.getLog(this.getClass());

    private static final String TEMP_DIR = "temporary.files.directory";

    public MindyAnalysis() {
        setLabel("MINDY");
        setDefaultPanel(new MindyParamPanel());
    }

    // not used
    public int getAnalysisType() {
        return AbstractAnalysis.ZERO_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        log.debug("input: " + input);
        // Use this to get params
        MindyParamPanel params = (MindyParamPanel) aspp;
        DSMicroarraySet<DSMicroarray> mSet = ((DSMicroarraySetView) input).getMicroarraySet();

        MindyData loadedData = null;
        try {
            loadedData = MindyResultsParser.parseResults((CSMicroarraySet) mSet, new File(params.getResultsFile()));
        } catch (IOException e) {
            log.error(e);
        }

        MindyDataSet dataSet = new MindyDataSet(mSet, "MINDY Results", loadedData, params.getResultsFile());
        return new AlgorithmExecutionResults(true, "MINDY Results Loaded.", dataSet);

    }

    @Publish
    public MindyDataSet publishMatrixReduceSet(MindyDataSet data) {
        return data;
    }
}
