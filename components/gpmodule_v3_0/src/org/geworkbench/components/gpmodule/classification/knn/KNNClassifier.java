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
package org.geworkbench.components.gpmodule.classification.knn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.genepattern.webservice.Parameter;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.PredictionModel;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.components.gpmodule.GPDataset;
import org.geworkbench.components.gpmodule.classification.GPClassifier;
import org.geworkbench.components.gpmodule.classification.PredictionResult;

/**
 * @author Marc-Danie Nazaire
 * @version $Id$
 */
public class KNNClassifier extends GPClassifier {

	private static final long serialVersionUID = -2025344726442268636L;
	
	List<Parameter> knnParameters = null;

    public KNNClassifier(DSDataSet<?> parent, String label, String[] classifications, PredictionModel predModel,
                         GPDataset dataset, DSPanel<DSMicroarray> casePanel,
                         DSPanel<DSMicroarray> controlPanel, List<Parameter> knnParams)
    {
        super("KNN", parent, label, classifications, predModel, dataset, casePanel, controlPanel);
        this.knnParameters = knnParams;
    }

    public int classify(float[] data)
    {
        List<float[]> dataset = new ArrayList<float[]>();
        dataset.add(data);

        File testData = createTestGCTFile("KNNTest_Data", dataset);
        File testCLSData = createTestCLSFile("KNNTest_Cls", dataset.size());

        List<Parameter> parameters = new ArrayList<Parameter>();

        parameters.add(new Parameter("saved.model.filename", predModel.getPredModelFile().getAbsolutePath()));
        parameters.add(new Parameter("test.filename", testData.getAbsolutePath()));
        parameters.add(new Parameter("test.class.filename", testCLSData.getAbsolutePath()));
        parameters.add(new Parameter("pred.results.file", predModel.getPredModelFile().getName() + "pred"));
        parameters.addAll(knnParameters);

        PredictionResult predResult = runPredictor("KNN", (Parameter[])parameters.toArray(new Parameter[0]));
      
        return (getPredictedClass(predResult).equals("Control") ? 1 : 0);
    }
}
