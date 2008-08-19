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
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;

import java.util.List;

/**
 * @author Marc-Danie Nazaire
 */
public class SVMClassifier extends GPClassifier
{
    public SVMClassifier(DSDataSet parent, String label, String[] classifications, PredictionModel predModel, List featureNames, List knnParams)
    {
        super(parent, label, classifications);
    }
    
    public int classify(float[] data)
    {
        return -1;
    }
}
