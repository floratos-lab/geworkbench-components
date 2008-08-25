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
package org.geworkbench.components.gpmodule.classification.svm;

import org.geworkbench.components.gpmodule.classification.GPClassifier;
import org.geworkbench.components.gpmodule.classification.PredictionModel;
import org.geworkbench.components.gpmodule.classification.PredictionResult;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * @author Marc-Danie Nazaire
 */
public class SVMClassifier extends GPClassifier
{
    private PredictionModel model;
    private PredictionResult trainPredResult;

    public SVMClassifier(DSDataSet parent, String label, String[] classifications, PredictionModel model, List featureNames)
    {
        super(parent, label, classifications);
        this.model = model;
        this.featureNames = featureNames;
    }
    
    public int classify(float[] data)
    {
        File testData = createTestGCTFile("SVMTest_Data", data);
        File testCLSData = createTestCLSFile("SVMTest_Cls");

        List parameters = new ArrayList();


        return -1;
    }

    public void setTrainPredResult(PredictionResult result)
    {
        trainPredResult = result;
    }

    public PredictionResult getTrainPredResult()
    {
        return trainPredResult;
    }

    public void classifierTestResult()
    {

    }
}
