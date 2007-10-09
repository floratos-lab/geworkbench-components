package org.geworkbench.components.gpmodule;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.genepattern.data.expr.IExpressionData;
import org.genepattern.data.expr.AbstractExpressionData;
import org.genepattern.io.expr.gct.GctWriter;
import org.genepattern.util.GPpropertiesManager;
import org.genepattern.client.GPServer;
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

        IExpressionData data = new AbstractExpressionData() {

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
            //gctFile.deleteOnExit();
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
        List resultFiles = null;
        try
        {
            String serverName = GPpropertiesManager.getProperty("gp.server");
            String userName = GPpropertiesManager.getProperty("gp.user.name");

            GPServer server = new GPServer(serverName, userName, password);

            JobResult analysisResult = server.runAnalysis(analysisName, parameters);

            System.out.println("Error occurred: " + analysisResult.hasStandardError());
            if(analysisResult.hasStandardError())
            {
                JOptionPane.showMessageDialog(panel, "An error occurred");
            }

            String[] outputFiles = analysisResult.getOutputFileNames();

            resultFiles = new ArrayList();
            for(int i = 0; i < outputFiles.length; i++)
            {
                File resultFile = analysisResult.downloadFile(outputFiles[i], System.getProperty("temporary.files.directory"));
                resultFile.deleteOnExit();
                resultFiles.add(resultFile.getAbsolutePath());               
            }

            // remove job from GenePattern server
            AnalysisWebServiceProxy analysisProxy = new AnalysisWebServiceProxy(server.getServer(), server.getUsername(), password);
            analysisProxy.purgeJob(analysisResult.getJobNumber());
        }
        catch(WebServiceException we)
        {
            if(we.getMessage().contains("ConnectException"))
                JOptionPane.showMessageDialog(panel, "Could not connect to GenePattern server");
            throw we;
        }

        return resultFiles;
    }

    public int getAnalysisType()
    {
        return AbstractAnalysis.ZERO_TYPE;
    }
}
