package org.geworkbench.components.ei;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author mhall
 */
public class EvidenceIntegrationAnalysis extends AbstractAnalysis implements ClusteringAnalysis {

    static Log log = LogFactory.getLog(EvidenceIntegrationAnalysis.class);

    public EvidenceIntegrationAnalysis() {
        setLabel("Evidence Integration");
        setDefaultPanel(new EvidenceIntegrationParamPanel());
    }

    public int getAnalysisType() {
        return AbstractAnalysis.ZERO_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
