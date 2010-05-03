/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2010) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.gpmodule.classification.wv;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.components.gpmodule.classification.PredictionModel;
import org.geworkbench.components.gpmodule.classification.GPClassifier;
import org.geworkbench.components.gpmodule.classification.PredictionResult;
import org.geworkbench.components.gpmodule.GPDataset;
import org.genepattern.webservice.Parameter;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Marc-Danie Nazaire
 */
public class WVClassifier extends GPClassifier {

    public WVClassifier(DSDataSet parent, String label, String[] classifications, PredictionModel model,
                        GPDataset dataset, DSPanel<DSMicroarray> casePanel,
                        DSPanel<DSMicroarray> controlPanel)
    {
        super("WeightedVoting", parent, label, classifications, model, dataset, casePanel, controlPanel);
    }

    public int classify(float[] data)
    {
        List dataset = new ArrayList();
        dataset.add(data);

        File testData = createTestGCTFile("WVTest_Data", dataset);
        File testCLSData = createTestCLSFile("WVTest_Cls", dataset.size());

        List parameters = new ArrayList();
        parameters.add(new Parameter("saved.model.filename", predModel.getPredModelFile().getAbsolutePath()));
        parameters.add(new Parameter("test.filename", testData.getAbsolutePath()));
        parameters.add(new Parameter("test.class.filename", testCLSData.getAbsolutePath()));
        parameters.add(new Parameter("pred.results.file", predModel.getPredModelFile().getName() + "_pred"));

        PredictionResult predResult = runPredictor("WeightedVoting", (Parameter[])parameters.toArray(new Parameter[0]));

        return (getPredictedClass(predResult).equals("Control") ? 1 : 0);
    }
}
