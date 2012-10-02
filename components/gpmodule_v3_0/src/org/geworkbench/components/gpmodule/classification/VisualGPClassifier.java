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
package org.geworkbench.components.gpmodule.classification;

import org.geworkbench.components.gpmodule.GPDataset;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.PredictionModel;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.genepattern.webservice.Parameter;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Marc-Danie Nazaire
 * @version $Id$
 */
public abstract class VisualGPClassifier extends GPClassifier
{
	private static final long serialVersionUID = 6489090053268987991L;
	
	private PredictionResult trainPredResult;
    private PredictionResult testPredResult;

    public VisualGPClassifier(String moduleName, DSDataSet<?> parent, String label, String[] classifications,
                              GPDataset dataset, PredictionModel model, DSPanel<DSMicroarray> casePanel,
                              DSPanel<DSMicroarray> controlPanel)
    {
        super(moduleName, parent, label, classifications, model, dataset, casePanel, controlPanel);
    }

    public int classify(float[] data)
    {
        List<float[]> dataset = new ArrayList<float[]>();
        dataset.add(data);

        PredictionResult predResult = runPredictor(moduleName, buildParametersList(dataset, null, null));

        return (getPredictedClass(predResult).equals("Control") ? 1 : 0);
    }

    public PredictionResult classify(DSPanel<DSMicroarray> panel)
    {
        return classify(panel, null);
    }

    public PredictionResult classify(DSPanel<DSMicroarray> panel, String[] classLabels)
    {
        List<String> arrayNames = new ArrayList<String>();

        for(DSMicroarray microarray: panel)
        {
            arrayNames.add(microarray.getLabel());
        }

        List<float[]> dataset = new ArrayList<float[]>();
        for (DSMicroarray microarray : panel)
        {
            dataset.add(microarray.getRawMarkerData());
        }

        PredictionResult predResult = runPredictor(moduleName, buildParametersList(dataset, arrayNames, classLabels));

        return predResult;
    }

    public abstract Parameter[] buildParametersList(List<float[]> data, List<String> arrayNames, String[] classLabels);

    public void setTrainPredResult(PredictionResult result)
    {
        trainPredResult = result;
    }

    public PredictionResult getTrainPredResult()
    {
        return trainPredResult;
    }

    public void setTestPredResult(PredictionResult result)
    {
        testPredResult = result;
    }

    public PredictionResult getTestPredResult()
    {
        return testPredResult;
    }
}
