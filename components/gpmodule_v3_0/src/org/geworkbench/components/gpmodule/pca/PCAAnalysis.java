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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JProgressBar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genepattern.webservice.Parameter;
import org.geworkbench.bison.datastructure.biocollections.pca.CSPCADataSet;
import org.geworkbench.bison.datastructure.biocollections.pca.DSPCADataSet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.components.gpmodule.GPAnalysis;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.threading.SwingWorker;

/**
 * @author: Marc-Danie Nazaire
 */
public class PCAAnalysis extends GPAnalysis
{
    private static Log log = LogFactory.getLog(PCAAnalysis.class);
    private PCAProgress progress = new PCAProgress();
    private DSPCADataSet pcaDataSet;
    private Task task;
    private boolean error = false;

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
        
        task = new Task(view);
        progress.setVisible(true);
        task.execute();
        
        while(!task.isDone()){}

        if(task.isCancelled())
        {
            return null;
        }
        else if(pcaDataSet == null)
        {
            if(error)
                return null;

            return new AlgorithmExecutionResults(false, "An error occurred when running PCA.", null);
        }
        else
            return new AlgorithmExecutionResults(true, "PCA Results", pcaDataSet);

    }

    @Publish
    public ProjectNodeAddedEvent publishProjectNodeAddedEvent(ProjectNodeAddedEvent event) 
    {
        return event;
    }

    private class PCAProgress extends JDialog
    {
        private JButton cancelButton = new JButton("Cancel");
        private JProgressBar progressBar = new JProgressBar(0, 100);

        public PCAProgress()
        {
            getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
            setTitle("PCA Progress");
            setSize(220, 120);
            setLocation((int)(getToolkit().getScreenSize().getWidth() - getWidth()) / 2, (int) (getToolkit().getScreenSize().getHeight() - getHeight()) / 2);

            progressBar.setStringPainted(true);

            add(Box.createRigidArea(new Dimension(0, 25)));
            add(progressBar);

            add(Box.createVerticalGlue());
            add(Box.createRigidArea(new Dimension(70, 0)));
            add(cancelButton);
            add(Box.createVerticalGlue());

            cancelButton.addActionListener( new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    if((task != null) && (!task.isCancelled()) && (!task.isDone()))
                    {
            		    task.cancel(true);
            		    log.info("Cancelling PCA Analysis");
            	    }
                }
            });
        }

        public void setProgress(int value)
        {
            progressBar.setValue(value);
        }      
    }

    public List runAnalysis(String analysisName, Parameter[] parameters, String password)
    {
        List result = null;
        try
        {
            result = super.runAnalysis(analysisName, parameters, password);
            if(result == null)
                error = true;
        }
        catch(Exception e)
        {	
        	e.printStackTrace();
            task.cancel(true);
            error = true;
        }

        return result;
    }
    
    private class Task extends SwingWorker<CSPCADataSet, Void>
    {
        DSMicroarraySetView<DSGeneMarker, DSMicroarray> view;
        public Task(DSMicroarraySetView<DSGeneMarker, DSMicroarray> view)
        {
            this.view = view;
        }

        public CSPCADataSet doInBackground()
        {
            progress.setProgress(0);

            if(((PCAAnalysisPanel)panel).getVariables().equals("genes"))
            {
                view.useItemPanel(false);
            }
            else
                view.useMarkerPanel(false);

            String gctFileName = createGCTFile("pcaDataset", view.markers(), view.items()).getAbsolutePath();
            progress.setProgress(20);

            String clusterBy = "rows";

            if(((PCAAnalysisPanel)panel).getVariables().equals("genes"))
            {
                List parameters = new ArrayList();

                parameters.add(new Parameter("input.filename", gctFileName));

                List results = runAnalysis("TransposeDataset", (Parameter[])parameters.toArray(new Parameter[0]), panel.getPassword());
                progress.setProgress(30);

                if(results == null)
                {
                    return null;
                }

                for(Object file : results)
                {
                    if(((String)file).contains(".gct"))
                        gctFileName = (String)file;
                }
            }

            progress.setProgress(40);
            List parameters = new ArrayList();

            parameters.add(new Parameter("input.filename", gctFileName));

            parameters.add(new Parameter("cluster.by", clusterBy));

            progress.setProgress(50);

            List results = runAnalysis("PCA", (Parameter[])parameters.toArray(new Parameter[0]), panel.getPassword());

            progress.setProgress(60);

            if(results == null)
            {
                return null;
            }

            progress.setProgress(70);
            Iterator it = results.iterator();
            while(it.hasNext())
            {
                String file = (String)it.next();
                if(!file.contains(".odf"))
                {
                    it.remove();
                }
            }

            progress.setProgress(80);

            if(results.size() == 0)
            {
                return null;
            }

            PCAData pcaData = new PCAData(results, ((PCAAnalysisPanel)panel).getVariables());

            CSPCADataSet pcaDs = new CSPCADataSet(view.getDataSet(), "PCA Results", pcaData.getVariables(),
                    pcaData.getNumPCs(), pcaData.getUMatrix().getArray(), pcaData.getEigenValues(), pcaData.getEigenVectors(), pcaData.getPercentVars());

            progress.setProgress(90);
            String history = "Generated by PCA run with parameters: \n" + "variables: " + ((PCAAnalysisPanel)panel).getVariables();

            ProjectPanel.addToHistory(pcaDs, history);

            progress.setProgress(100);

            log.info("Done running PCA. " + System.currentTimeMillis());

            pcaDataSet = pcaDs;
            
            return pcaDs;
        }

        public void done()
        {
            if(!this.isCancelled())
            {
                try
                {
                    log.debug("Transferring PCA data set back to event thread.");
                }
                catch (Exception e)
                {
                    log.error("Exception in finishing up worker thread that called PCA: " + e.getMessage(), e);
                }
            }

            progress.setVisible(false);
            progress.dispose();
            log.debug("Closing PCA progress bar.");
        }
    }
}