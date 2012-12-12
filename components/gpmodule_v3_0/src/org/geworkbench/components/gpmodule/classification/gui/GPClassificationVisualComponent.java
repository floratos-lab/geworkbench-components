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
package org.geworkbench.components.gpmodule.classification.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.geworkbench.bison.algorithm.classification.CSVisualClassifier;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.PredictionModel;
import org.geworkbench.bison.datastructure.biocollections.SVMResultSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.builtin.projects.DataSetNode;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.components.gpmodule.GPDataset;
import org.geworkbench.components.gpmodule.classification.PredictionResult;
import org.geworkbench.components.gpmodule.classification.VisualGPClassifier;
import org.geworkbench.components.gpmodule.classification.svm.SVMClassifier;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;

/**
 *@author Marc-Danie Nazaire
 *@version $Id$
 */

@AcceptTypes( {CSVisualClassifier.class, SVMResultSet.class})
public class GPClassificationVisualComponent implements VisualPlugin
{
    public static ArrayList<DSMicroarraySet> microarraySets = new ArrayList<DSMicroarraySet>();

    protected JPanel component;
    protected GPClassificationVisualizationPanel gpClassVisPanel;

    public GPClassificationVisualComponent()
    {
        component = new JPanel(new BorderLayout());
    }

    public Component getComponent()
    {
        return component;
    }

    @Subscribe
    public void receive(org.geworkbench.events.ProjectEvent e, Object source)
    {
        DSDataSet<?> dataSet = e.getDataSet();
        if(dataSet != null && (dataSet instanceof VisualGPClassifier || dataSet instanceof SVMResultSet))
        {
        	microarraySets.clear();
        	for(DataSetNode dataNode : ProjectPanel.getInstance().getTopLevelDataSetNodes() ) {
				DSDataSet<?> dset = ((DataSetNode)dataNode).getDataset();
				if (dset instanceof DSMicroarraySet){
					DSMicroarraySet mset = (DSMicroarraySet)dset;
    				microarraySets.add(mset);
				}
        	}

            component.removeAll();

            VisualGPClassifier gpVisClassifier;
            if (dataSet instanceof SVMResultSet) 
            	gpVisClassifier = convertSVMResultSetToClassifier((SVMResultSet)dataSet);
            else gpVisClassifier = (VisualGPClassifier)dataSet;

            gpClassVisPanel = new GPClassificationVisualizationPanel(gpVisClassifier, this);
            gpClassVisPanel.setBorder(BorderFactory.createEmptyBorder());

            component.add(gpClassVisPanel);

            component.revalidate();
		    component.repaint();
        }

    }

    private SVMClassifier convertSVMResultSetToClassifier(SVMResultSet result) {
    	if (result == null) return null;
		SVMClassifier svmClassifier = new SVMClassifier(result.getParentDataSet(), 
				result.getLabel(), result.getClassifications(), result.getPredictionModel(),
				new GPDataset(result.getData(), result.getRowNames(), result.getColumnNames()),
				result.getCasePanel(), result.getControlPanel());
		svmClassifier.setPassword(result.getPassword());

		PredictionModel trainModel = result.getTrainPredResultModel();
		PredictionModel testModel  = result.getTestPredResultModel();
		if (trainModel != null)
			svmClassifier.setTrainPredResult(new PredictionResult(trainModel.getPredModelFile()));
		if (testModel != null)
			svmClassifier.setTestPredResult(new PredictionResult(testModel.getPredModelFile()));

		return svmClassifier;
	}
    
    @Publish
    public org.geworkbench.events.SubpanelChangedEvent<DSMicroarray> publishSubpanelChangedEvent(org.geworkbench.events.SubpanelChangedEvent<DSMicroarray> event)
    {
        return event;
    }
}
