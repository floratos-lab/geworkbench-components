package org.geworkbench.components.gpmodule;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.genepattern.matrix.Dataset;
import org.genepattern.matrix.AbstractDataset;
import org.genepattern.io.gct.GctWriter;
import org.genepattern.util.GPpropertiesManager;
import org.genepattern.client.GPClient;
import org.genepattern.webservice.JobResult;
import org.genepattern.webservice.AnalysisWebServiceProxy;
import org.genepattern.webservice.WebServiceException;
import org.genepattern.webservice.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author: Marc-Danie Nazaire
 */
public abstract class GPAnalysis extends AbstractAnalysis implements ClusteringAnalysis
{
    static Log log = LogFactory.getLog(GPAnalysis.class);
        
    protected GPAnalysisPanel panel;

    protected File createGCTFile(String fileName, final DSItemList<DSGeneMarker> markers, final DSItemList<DSMicroarray> arrays)
    {
        File gctFile = null;

        Dataset data = new AbstractDataset() {

            public double getValue(int row, int column)
            {
                return arrays.get(column).getRawMarkerData()[row];
            }

            public String getRowName(int row)
            {
                return markers.get(row).getLabel();
            }

            public int getRowCount()
            {
                return markers.size();
            }

            public String getRowDescription(int row)
            {
                return "";
            }

            public int getColumnCount()
            {
                return arrays.size();
            }

            public String getColumnName(int column)
            {
                return arrays.get(column).getLabel();
            }

            public String getColumnDescription(int column)
            {
                return "";
            }
        };

        GctWriter writer = new GctWriter();
        OutputStream os = null;
        try
        {
            gctFile = new File(fileName + ".gct");
            gctFile.deleteOnExit();
            os = new BufferedOutputStream(new FileOutputStream(gctFile));
            writer.write(data, os);
        }
        catch (IOException ioe)
        {   ioe.printStackTrace();  }
        finally
        {
            try
            {
                if (os != null)
                {   os.close(); }
            }
            catch (IOException e)
            {   e.printStackTrace();    }
        }

        return gctFile;
    }

    public List runAnalysis(String analysisName, Parameter[] parameters, String password) throws Exception
    {
        List resultFiles = new ArrayList();
        String serverName = GPpropertiesManager.getProperty("gp.server");
        String userName = GPpropertiesManager.getProperty("gp.user.name");

        try
        {
            if(serverName == null)
            {
                JOptionPane.showMessageDialog(panel, "Please set your GenePattern server settings.");
                return null;
            }

            if(userName == null)
            {
                JOptionPane.showMessageDialog(panel, "Please set your GenePattern user name.");
                return null;
            }

            GPClient server = new GPClient(serverName, userName, password);

            JobResult analysisResult = server.runAnalysis(analysisName, parameters);

            if(analysisResult.hasStandardError())
            {
                JOptionPane.showMessageDialog(panel, "An error occurred while running analysis.");
            }

            String[] outputFiles = analysisResult.getOutputFileNames();

            if(outputFiles != null && outputFiles.length > 0)
            {
                AnalysisWebServiceProxy analysisProxy = new AnalysisWebServiceProxy(server.getServer(), server.getUsername(), password);
                File[] results = analysisProxy.getResultFiles(analysisResult.getJobNumber(), outputFiles, new File(System.getProperty("temporary.files.directory")), true);

                for(int i = 0; i < results.length; i++)
                {
                    resultFiles.add(results[i].getAbsolutePath());
                }
            }
            else
                resultFiles = null;
            
            // remove job from GenePattern server
            AnalysisWebServiceProxy analysisProxy = new AnalysisWebServiceProxy(server.getServer(), server.getUsername(), password);
            analysisProxy.purgeJob(analysisResult.getJobNumber());
        }
        catch(WebServiceException we)
        {
            if(we.getMessage().contains("ConnectException"))
                JOptionPane.showMessageDialog(panel, "Could not connect to the GenePattern server at " + serverName + ".\n Please verify the GenePattern server settings and that the GenePattern server is running.");
            else if(we.getMessage().contains("not found on server"))
                JOptionPane.showMessageDialog(panel, analysisName + " module was not found on the GenePattern server at " + serverName + ".");
            else
                JOptionPane.showMessageDialog(panel, "An error occurred while trying to connect to the GenePattern server " + serverName + ".\n Please verify the GenePattern server settings.");

            throw we;
        }
        /*catch(IOException io)
        {
            log.error(io);
            JOptionPane.showMessageDialog(panel, "An error occurred while retrieving results from the GenePattern server.");
        } */

        return resultFiles;
    }

    public int getAnalysisType()
    {
        return AbstractAnalysis.ZERO_TYPE;
    }
}
