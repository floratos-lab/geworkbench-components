/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2008) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.gpmodule.classification.svm.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.algorithm.classification.CSSvmClassifier;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.components.gpmodule.classification.PredictionModel;
import org.geworkbench.components.gpmodule.classification.PredictionResult;
import org.geworkbench.components.gpmodule.classification.svm.SVMClassifier;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.util.FilePathnameUtils;

/**
 *@author Marc-Danie Nazaire
 */

@AcceptTypes( {CSSvmClassifier.class})
public class SVMVisualComponent implements VisualPlugin
{
    private Log log = LogFactory.getLog(this.getClass());
    public static SortedMap microarraySets = new TreeMap();

    private JPanel component;
    private SVMVisualizationPanel svmVPanel;

    public SVMVisualComponent()
    {
        component = new JPanel(new BorderLayout());
    }

    public Component getComponent()
    {
        return component;
    }

    @Subscribe
    public void receive(org.geworkbench.events.ProjectNodeAddedEvent e, Object source)
    {
        DSDataSet dataSet = e.getDataSet();

        if (dataSet instanceof DSMicroarraySet)
        {
            System.out.println("SVMVisualComponent received project node added event.");            
            microarraySets.put(dataSet.getDataSetName(), dataSet);
		}
    }

    @Subscribe
    public void receive(org.geworkbench.events.ProjectNodeRemovedEvent e, Object source)
    {
        System.out.println("SVMVisualComponent received project node removed event.");
        DSDataSet dataSet = e.getDataSet();

        if (dataSet instanceof DSMicroarraySet)
        {
            microarraySets.remove(dataSet.getDataSetName());
		}
	}

    @Subscribe
    public void receive(org.geworkbench.events.ProjectNodeRenamedEvent e, Object source)
    {
        System.out.println("SVMVisualComponent received project node renamed event.");
        DSDataSet dataSet = e.getDataSet();

        if (dataSet instanceof DSMicroarraySet)
        {
            String oldName = e.getOldName();
            Iterator it = microarraySets.keySet().iterator();
            while(it.hasNext())
            {
                String key = (String)it.next();
                DSMicroarraySet microarraySet = (DSMicroarraySet)microarraySets.get(key);

                if(oldName.equals(microarraySet.getDataSetName()))
                {
                    microarraySets.remove(microarraySet.getDataSetName());
                    break;
                }
            }

            microarraySets.put(dataSet.getDataSetName(),dataSet);
		}
	}

    @Subscribe
    public void receive(org.geworkbench.events.ProjectEvent e, Object source)
    {
        System.out.println("SVMVisualComponent received project event.");

        DSDataSet dataSet = e.getDataSet();
        if(dataSet != null && dataSet instanceof CSSvmClassifier)
        {
            component.removeAll();

            CSSvmClassifier csClassifier = (CSSvmClassifier)dataSet;
            File modelFile = null;
            FileOutputStream out = null;
			try {
				modelFile = File.createTempFile("modelFile_"+System.currentTimeMillis(), ".odf", new File(FilePathnameUtils.getTemporaryFilesDirectoryPath()));
	            modelFile.deleteOnExit();
	            out = new FileOutputStream(modelFile);
	            out.write(csClassifier.getModelFileContent());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				try {
					if(out!=null)
						out.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
            
            PredictionModel predictionModel = new PredictionModel(modelFile);

            SVMClassifier svmClassifier = new SVMClassifier(csClassifier
					.getParentDataSet(), csClassifier.getLabel(), csClassifier
					.getClassifications(), predictionModel, csClassifier.getFeatureNames());
            
            // recreate prediction result (adopted from AbstarctTraining.execute()
            DSPanel<DSMicroarray> casePanel = csClassifier.getCasePanel();
            DSPanel<DSMicroarray> controlPanel = csClassifier.getControlPanel();
            
            DSPanel<DSMicroarray> trainPanel = new CSPanel<DSMicroarray> ();
            trainPanel.addAll(controlPanel);
            trainPanel.addAll(casePanel);
            String[] classLabels = new String[trainPanel.size()];
            Arrays.fill(classLabels, 0, controlPanel.size(), "Control");
            Arrays.fill(classLabels, controlPanel.size(), trainPanel.size(), "Case");
            PredictionResult trainResult = svmClassifier.classify(trainPanel, classLabels);
            svmClassifier.setTrainPredResult(trainResult);
            System.out.println(trainResult);
            if(trainResult==null)System.exit(1);
                
            svmVPanel = new SVMVisualizationPanel(svmClassifier, this);
            svmVPanel.setBorder(BorderFactory.createEmptyBorder());

            component.add(svmVPanel);

            component.revalidate();
		    component.repaint();
        }

        if (dataSet instanceof DSMicroarraySet)
        {
            microarraySets.put(dataSet.getDataSetName(),dataSet);
		}
    }

    @Publish
    public org.geworkbench.events.SubpanelChangedEvent publishSubpanelChangedEvent(org.geworkbench.events.SubpanelChangedEvent event)
    {
        return event;
    }

}