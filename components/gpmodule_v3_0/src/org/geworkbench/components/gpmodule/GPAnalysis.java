package org.geworkbench.components.gpmodule;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.builtin.projects.Icons;
import org.genepattern.matrix.Dataset;
import org.genepattern.matrix.AbstractDataset;
import org.genepattern.matrix.ClassVector;
import org.genepattern.io.gct.GctWriter;
import org.genepattern.io.cls.ClsWriter;
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
import java.awt.*;

/**
 * @author: Marc-Danie Nazaire
 */
public abstract class GPAnalysis extends AbstractAnalysis implements ClusteringAnalysis
{
    static Log log = LogFactory.getLog(GPAnalysis.class);
        
    protected GPAnalysisPanel panel;
    //public static ImageIcon GP_ICON = new ImageIcon(GPAnalysis.class.getResource("images/gp-result-logo.gif"));

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

        gctFile = fixFileName(gctFile);
        
        return gctFile;
    }

    protected File createCLSFile(String fileName, ClassVector classLabel)
    {
        File clsFile = null;

        try
        {
            clsFile = new File(fileName + ".cls");
            clsFile.deleteOnExit();
            FileOutputStream clsOutputStream = new FileOutputStream(clsFile);

            ClsWriter writer = new ClsWriter();
            writer.write(classLabel, clsOutputStream);
        }
        catch(Exception e)
        {   e.printStackTrace(); }

        return clsFile;
    }

    protected File fixFileName(File fileName)
    {
        if(fileName == null)
        {
            return null;
        }

        String newFileName = fileName.getName();
        newFileName = newFileName.replaceAll("-", "_");
        newFileName = newFileName.replaceAll(" ", "_");

        File newFile = new File(fileName.getParent(), newFileName);
        if(!fileName.getName().equals(newFileName))
        {
            if(!fileName.renameTo(newFile))
            {
                InputStream in = null;
                OutputStream out = null;

                try {
                    in = new FileInputStream(fileName);
                    out = new FileOutputStream(newFileName);

                    copy(in, out);
                    in.close();

                    out.flush();
                    out.close();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                finally {
                    try
                    {
                        if (in != null){
                            in.close();
                        }
                        if (out != null){
                            out.flush();
                            out.close();
                        }
                    }
                    catch(Exception e){}
                }
            }
        }

        return newFile;
    }

    private static void copy(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
    }
    public List runAnalysis(String analysisName, Parameter[] parameters, String password) throws Exception
    {
        List resultFiles = new ArrayList();
        String serverName = GPpropertiesManager.getProperty("gp.server");
        String userName = GPpropertiesManager.getProperty("gp.user.name");

        JobResult analysisResult = null;
        GPClient server = null;
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

            server = new GPClient(serverName, userName, password);

            analysisResult = server.runAnalysis(analysisName, parameters);
            analysisResult.downloadFiles(System.getProperty("temporary.files.directory"), true);

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

            if(analysisResult.hasStandardError())
            {
                File errorFile = new File(System.getProperty("temporary.files.directory") + "/stderr.txt");

                boolean result = extractErrorMessages(errorFile, analysisName);
                if(!result)
                {
                    JOptionPane.showMessageDialog(panel, "Some errors where generated while running analysis.");
                }           
            }
        }
        catch(WebServiceException we)
        {
            if(we.getMessage().contains("ConnectException"))
                JOptionPane.showMessageDialog(panel, "Could not connect to the GenePattern server at " + serverName + ".\n Please verify the GenePattern server settings and that the GenePattern server is running.");
            else if(we.getMessage().contains("not found on server"))
                JOptionPane.showMessageDialog(panel, analysisName + " module was not found on the GenePattern server at " + serverName + ".");
            else if(analysisResult == null)
                JOptionPane.showMessageDialog(panel, "An error occurred while trying to connect to the GenePattern server " + serverName + ".\n Please verify the GenePattern server settings.");
            else
            {
                JOptionPane.showMessageDialog(panel, "An error occurred while retrieving results from GenePattern server " + serverName + ".\n Please check the logs for details.");                    
            }
            throw we;
        }
        finally
        {
            if(server != null && analysisResult != null)
            {
                // remove job from GenePattern server
                AnalysisWebServiceProxy analysisProxy = new AnalysisWebServiceProxy(server.getServer(), server.getUsername(), password);
                analysisProxy.purgeJob(analysisResult.getJobNumber());
            }
        }

        return resultFiles;
    }


    private boolean extractErrorMessages(File file, String analysisName)
    {
        try
        {
            BufferedReader reader = new BufferedReader( new FileReader(file));
            String line;
            StringBuffer sb = new StringBuffer();
            while((line = reader.readLine()) != null)
            {
                sb.append(line);
                sb.append("\n");                
            }

            log.error(sb + "\n");

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setMargin(new Insets(5, 5, 5, 5));
            textArea.setText(sb.toString());

            JScrollPane scroll_pane = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(null, scroll_pane, (analysisName +  " analysis error"),
                         JOptionPane.ERROR_MESSAGE);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void abort()
    {
        log.error("Aborting analysis");
    }

    public int getAnalysisType()
    {
        return AbstractAnalysis.ZERO_TYPE;
    }
}
