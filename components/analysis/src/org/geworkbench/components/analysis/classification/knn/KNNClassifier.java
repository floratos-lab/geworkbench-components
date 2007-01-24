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
package org.geworkbench.components.analysis.classification.knn;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.components.analysis.classification.GPClassifier;
import org.geworkbench.components.analysis.classification.PredictionModel;
import org.genepattern.webservice.Parameter;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Marc-Danie Nazaire
 */
public class KNNClassifier extends GPClassifier
{
    List knnParameters = null;
    PredictionModel predModel;

    public KNNClassifier(DSDataSet parent, String label, String[] classifications, PredictionModel predModel, List featureNames, List knnParams)
    {
        super(parent, label, classifications);
        this.predModel = predModel;
        this.featureNames = featureNames;
        this.knnParameters = knnParams;
        this.predModel = predModel;
    }

    public int classify(float[] data)
    {
        File testData = createTestGCTFile("KNNTest_Data", data);
        File testCLSData = createTestCLSFile("KNNTest_Cls");

        List parameters = new ArrayList();

        parameters.add(new Parameter("saved.model.filename", predModel.getPredModelFile().getAbsolutePath()));
        parameters.add(new Parameter("test.filename", testData.getAbsolutePath()));
        parameters.add(new Parameter("test.class.filename", testCLSData.getAbsolutePath()));
        parameters.addAll(knnParameters);
        

        File predFile = runPredictor("KNN", (Parameter[])parameters.toArray(new Parameter[0]));
      
        return (getPredictedClass(predFile).equals("Control") ? 1 : 0);
    }
}
