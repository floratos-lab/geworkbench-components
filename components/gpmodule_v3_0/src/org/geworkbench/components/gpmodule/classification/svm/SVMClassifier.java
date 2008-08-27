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
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.genepattern.webservice.Parameter;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * @author Marc-Danie Nazaire
 */
public class SVMClassifier extends GPClassifier
{
    private PredictionModel predModel;
    private PredictionResult trainPredResult;
    private PredictionResult testPredResult;

    public SVMClassifier(DSDataSet parent, String label, String[] classifications, PredictionModel model, List featureNames)
    {
        super(parent, label, classifications);
        this.predModel = model;
        this.featureNames = featureNames;
    }
    
    public int classify(float[] data)
    {
        List dataset = new ArrayList();
        dataset.add(data);

        PredictionResult predResult = runPredictor("SVM", buildParametersList(dataset, null));

        return (getPredictedClass(predResult).equals("Control") ? 1 : 0);
    }

    public void classify(DSPanel<DSMicroarray> panel)
    {
        List arrayNames = new ArrayList();

        for(DSMicroarray microarray: panel)
        {
            arrayNames.add(microarray.getLabel());
        }

        List dataset = new ArrayList();
        for (DSMicroarray microarray : panel)
        {
            dataset.add(microarray.getRawMarkerData());
        }

        PredictionResult predResult = runPredictor("SVM", buildParametersList(dataset, arrayNames));
        testPredResult = predResult;
    }

    private Parameter[] buildParametersList(List data, List arrayNames)
    {
        File testData = createTestGCTFile("SVMTest_Data", data, arrayNames);
        File testCLSData = createTestCLSFile("SVMTest_Cls", data.size());

        List parameters = new ArrayList();

        parameters.add(new Parameter("saved.model.filename", predModel.getPredModelFile().getAbsolutePath()));
        parameters.add(new Parameter("test.data.filename", testData.getAbsolutePath()));
        parameters.add(new Parameter("test.cls.filename", testCLSData.getAbsolutePath()));
        parameters.add(new Parameter("pred.results.output.file", predModel.getPredModelFile().getName() + "pred"));

        return (Parameter[])parameters.toArray(new Parameter[0]);
    }

    public void setTrainPredResult(PredictionResult result)
    {
        trainPredResult = result;
    }

    public PredictionResult getTrainPredResult()
    {
        return trainPredResult;
    }


    public PredictionResult getTestPredResult()
    {
        return testPredResult;
    }
}
