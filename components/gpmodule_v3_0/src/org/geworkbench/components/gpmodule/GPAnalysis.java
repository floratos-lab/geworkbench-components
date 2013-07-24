package org.geworkbench.components.gpmodule;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.StyledEditorKit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genepattern.client.GPClient;
import org.genepattern.gui.UIUtil;
import org.genepattern.io.cls.ClsWriter;
import org.genepattern.io.gct.GctWriter;
import org.genepattern.matrix.AbstractDataset;
import org.genepattern.matrix.ClassVector;
import org.genepattern.matrix.Dataset;
import org.genepattern.util.GPpropertiesManager;
import org.genepattern.webservice.AdminProxy;
import org.genepattern.webservice.AnalysisWebServiceProxy;
import org.genepattern.webservice.JobResult;
import org.genepattern.webservice.Parameter;
import org.genepattern.webservice.WebServiceException;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.components.gpmodule.gsea.GSEAAnalysisPanel;
import org.geworkbench.util.FilePathnameUtils;

/**
 * @author: Marc-Danie Nazaire
 * @version $Id$
 */
public abstract class GPAnalysis extends AbstractAnalysis implements ClusteringAnalysis
{
	private static final long serialVersionUID = -6732924818185459971L;

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

            public int getColumnCount()
            {
                return arrays.size();
            }

            public String getColumnName(int column)
            {
                return arrays.get(column).getLabel();
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
        newFile.deleteOnExit();
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
                    catch(IOException e){}
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
    
    public List<String> runAnalysis(String analysisName, Parameter[] parameters, String password) throws Exception
    {
        List<String> resultFiles = new ArrayList<String>();
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

            //hack for GSEA Analysis
            if(analysisName.toLowerCase().equals("gsea"))
            {
                AdminProxy admin = new AdminProxy(serverName, userName, password);
                String lsid = admin.getTask(analysisName).getLsid();
                int index = lsid.lastIndexOf(":")+1;
                if(index != -1)
                {
                    String version = lsid.substring(lsid.lastIndexOf(":")+1);
                    //check that the GSEA version is at least 5 since earlier
                    // versions will not work
                    if(version != null && Integer.parseInt(version) < 5)
                    {
                        JOptionPane.showMessageDialog(panel, "Found GSEA version "+ version + ", but GSEA version 5 or greater is " +
                                "required to run this analysis. \nPlease update the GSEA module on the GenePattern server: \n\t"+ serverName);
                        return null;
                    }
                }
            }

            analysisResult = server.runAnalysis(analysisName, parameters);
            analysisResult.downloadFiles( FilePathnameUtils.getTemporaryFilesDirectoryPath(), true);

            String[] outputFiles = analysisResult.getOutputFileNames();
            if(outputFiles != null && outputFiles.length > 0)
            {
                AnalysisWebServiceProxy analysisProxy = new AnalysisWebServiceProxy(server.getServer(), server.getUsername(), password);
                File[] results = analysisProxy.getResultFiles(analysisResult.getJobNumber(), outputFiles, new File(FilePathnameUtils.getTemporaryFilesDirectoryPath()), true);

                for(int i = 0; i < results.length; i++)
                {
                    resultFiles.add(results[i].getAbsolutePath());
                }
            }

            if(analysisResult.hasStandardError())
            {
                File errorFile = new File(FilePathnameUtils.getTemporaryFilesDirectoryPath() + "/stderr.txt");

                boolean result = extractErrorMessages(errorFile, analysisName);
                if(!result)
                {
                    JOptionPane.showMessageDialog(panel, "Some errors and/or warnings where generated while running analysis.");
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
        boolean status = false;
        try
        {
            BufferedReader reader = new BufferedReader( new FileReader(file));
            String line;
            StringBuffer sb = new StringBuffer();
            boolean just_pref_warning = true;
            while((line = reader.readLine()) != null)
            {
                // special hack to hide java preferences warning in GSEA
                if(line.contains("java.util.prefs.FileSystemPreferences")
                    || line.contains("WARNING: Prefs"))
                {
                    log.error(line + "\n");
                    continue;
                }

                just_pref_warning = false;
                sb.append(line);
                sb.append("\n");
            }
            reader.close();

            if(just_pref_warning)
                return true;

            log.error(sb + "\n");

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setMargin(new Insets(5, 5, 5, 5));
            textArea.setText(sb.toString());

            JScrollPane scroll_pane = new JScrollPane(textArea);

            Object[] options = {"Help", "OK"};
            final JOptionPane pane = new JOptionPane(scroll_pane, JOptionPane.ERROR_MESSAGE,
                    JOptionPane.YES_NO_OPTION, null, options, options[1]);

            String title = analysisName +  " analysis error";

            final JDialog dialog = new JDialog((JFrame)null, title, true);
            dialog.setContentPane(pane);
            dialog.setDefaultCloseOperation(
            JDialog.DO_NOTHING_ON_CLOSE);
            dialog.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {}
            });

            pane.addPropertyChangeListener(
                new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    String prop = e.getPropertyName();

                    if (dialog.isVisible()
                    && (e.getSource() == pane)
                        && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                        dialog.setVisible(false);
                    }
                }
            });
            dialog.pack();
            dialog.setAlwaysOnTop(true);
            UIUtil.centerOnScreen(dialog);
            dialog.setVisible(true);

            status = true;
            String value = (String)pane.getValue();
            if (value.equals("Help")) {
                File helpFile = new File(GSEAAnalysisPanel.class.getResource("gsea_common_errors.html").getPath());
                JTextPane textPane = new JTextPane();
                textPane.setEditorKit(new StyledEditorKit());

                try
                {
                    textPane.setPage(helpFile.toURI().toURL());
                    textPane.setEditable(false);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

                textPane.setMinimumSize(new Dimension(440, 400));
                textPane.setPreferredSize(new Dimension(740, 700));
                textPane.setMaximumSize(new Dimension(950, 760));

                JScrollPane sp = new JScrollPane(textPane);

                //JOptionPane.showMessageDialog(panel, (sp));
                JFrame frame = new JFrame("GSEA Help");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                frame.add(sp, BorderLayout.CENTER);

                frame.pack();
                UIUtil.centerOnScreen(frame);
                frame.setVisible(true);

            } else if (value.equals("OK")) {
                dialog.setVisible(false);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return status;
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
