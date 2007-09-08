/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2007) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.gpmodule.pca;

import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.components.gpmodule.GPAnalysis;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genepattern.webservice.Parameter;

import java.util.List;
import java.util.ArrayList;

/**
 * @author: Marc-Danie Nazaire
 */
public class PCAAnalysis extends GPAnalysis
{
    static Log log = LogFactory.getLog(PCAAnalysis.class);

    public PCAAnalysis()
    {
        setLabel("PCA Analysis");

        panel = new PCAAnalysisPanel();
        setDefaultPanel(panel);
    }

    public AlgorithmExecutionResults execute(Object input)
    {
        assert (input instanceof DSMicroarraySetView);
        DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
        
        String gctFileName = createGCTFile("pcaDataset", view.markers(), view.items()).getAbsolutePath();

        List parameters = new ArrayList();

        parameters.add(new Parameter("input.filename", gctFileName));
        parameters.add(new Parameter("cluster.by", ((PCAAnalysisPanel)panel).getClusterBy()));

        List results = runAnalysis("PCA", (Parameter[])parameters.toArray(new Parameter[0]), panel.getPassword());        

        if(results == null)
        {
            return new AlgorithmExecutionResults(false, "PCA Results", null);
        }
        
        PCAData pcaData = new PCAData(results);
        CSAncillaryDataSet pcaDataSet = new PCADataSet(view.getDataSet(), "PCA Results", pcaData);

        return new AlgorithmExecutionResults(true, "PCA Results", pcaDataSet);
    }

    @Publish
    public ProjectNodeAddedEvent publishProjectNodeAddedEvent(ProjectNodeAddedEvent event) 
    {
        return event;
    }
}
