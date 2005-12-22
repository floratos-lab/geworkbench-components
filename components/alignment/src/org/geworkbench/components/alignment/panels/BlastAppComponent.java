package org.geworkbench.components.alignment.panels;

import org.geworkbench.events.ProgressBarEvent;
import org.geworkbench.events.StatusBarEvent;
import org.geworkbench.events.listeners.StatusChangeListener;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.engine.config.VisualPlugin;

import java.awt.*;


@AcceptTypes({CSSequenceSet.class}) public class BlastAppComponent implements VisualPlugin, StatusChangeListener {

    //    static URLClassLoader url = null;
    //    static {
    //        try {
    //            url = new URLClassLoader(new URL[] {new URL(
    //                "http://amdec-bioinfo.cu-genome.org/html/xalan")},
    //                                     Thread.currentThread().
    //                                     getContextClassLoader());
    //            File f = new File("C:/List.txt");
    //            BufferedReader in = new BufferedReader(new FileReader(f));
    //            String s = null;
    //            while ((s = in.readLine())!= null){
    //                url.loadClass(s.trim());
    //                System.out.println(s + " is loaded");
    //            }
    //            //url.loadClass("org.apache.xalan.lib.**");
    //        }
    //        catch (MalformedURLException mfue) {mfue.printStackTrace();}
    //        catch (ClassNotFoundException cnfe){cnfe.printStackTrace();}
    //        catch (Exception ef){ef.printStackTrace();}
    //        Thread.currentThread().setContextClassLoader(url);
    //    }

    private ParameterViewWidget tWidget = null;

    public BlastAppComponent() {
        try {
            tWidget = new ParameterViewWidget();
            tWidget.setBlastAppComponent(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public DSSequenceSet getFastFile() {
        return tWidget.getFastaFile();
    }

    // the method for VisualPlugin interface
    public Component getComponent() {
        return tWidget;
    }

    public void progressBarChanged(ProgressBarEvent evt) {
    };
    public void statusBarChanged(StatusBarEvent evt) {
    };



    @Subscribe public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
        //System.out.println("Event on parameter receive project selection: fasta file id......." );
            ProjectSelection selection = ((ProjectPanel) source).getSelection();
            DSDataSet df = selection.getDataSet();
            if (df != null) {
                //update db with the selected file in the project
                if (df instanceof DSSequenceSet) {

                    //System.out.println("at BlastAppComponent: fasta file id..." + df.getID());
                    // currentSessionID = df.getID() + df.getDataSetName();
                    tWidget.setFastaFile((CSSequenceSet) df);
                }
            }
    }

    /**
     * blastFinished
     */
    public void blastFinished(String cmd) {
        tWidget.blastFinished(cmd);
    }

    private void jbInit() throws Exception {
    }

    @Publish public org.geworkbench.events.ProjectNodeAddedEvent publishProjectNodeAddedEvent(org.geworkbench.events.ProjectNodeAddedEvent event) {
        return event;
    }
}
