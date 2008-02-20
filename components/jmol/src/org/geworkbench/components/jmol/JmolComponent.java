package org.geworkbench.components.jmol;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.jmol.api.JmolSimpleViewer;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolViewer;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openscience.jmol.ui.JmolPopup;
import org.openscience.jmol.ui.JmolPopupSwing;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

@AcceptTypes({DSProteinStructure.class})
public class JmolComponent extends JPanel implements VisualPlugin {

    static Log log = LogFactory.getLog(JmolComponent.class);

    private DSProteinStructure proteinData;
    private JLabel infoLabel;

    final static String strXyzHOH =
            "3\n" +
                    "water\n" +
                    "O  0.0 0.0 0.0\n" +
                    "H  0.76923955 -0.59357141 0.0\n" +
                    "H -0.76923955 -0.59357141 0.0\n";

    final static String strScript = //"delay; move 360 0 0 0 0 0 0 0 4;";
	"wireframe off; spacefill off; cartoons; color structure;";

    private JmolPanel jmolPanel;

    public JmolComponent() {
        setLayout(new BorderLayout());
        jmolPanel = new JmolPanel();
//        JmolSimpleViewer viewer = jmolPanel.getViewer();
//        viewer.openFile("c:/code/jmol-10.00/samples/pdb/toluene.pdb");
//        viewer.openStringInline(strXyzHOH);
        add(jmolPanel, BorderLayout.CENTER);

//        infoLabel = new JLabel("");
//        add(infoLabel);
    }

    /**
     * This method fulfills the contract of the {@link VisualPlugin} interface.
     * It returns the GUI component for this visual plugin.
     */
    public Component getComponent() {
        // In this case, this object is also the GUI component.
        return this;
    }

    @Subscribe
    public void receive(ProjectEvent event, Object source) {
        DSDataSet dataSet = event.getDataSet();
        // We will act on this object if it is a DSMicroarraySet
        if (dataSet instanceof DSProteinStructure) {
            proteinData = (DSProteinStructure) dataSet;

            JmolSimpleViewer viewer = jmolPanel.getViewer();
            //    viewer.openFile("../samples/caffeine.xyz");
            //    viewer.openFile("http://database.server/models/1pdb.pdb.gz");

            byte[] fileBytes;
            try {
                FileInputStream fileIn = new FileInputStream(proteinData.getFile().getAbsolutePath());
                DataInputStream dataIn = new DataInputStream(fileIn);
                fileBytes = new byte[dataIn.available()];
                dataIn.readFully(fileBytes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return;
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return;
            }
            viewer.openStringInline(new String(fileBytes));
            viewer.evalString(strScript);
//
//            revalidate();
            repaint();
            // We just received a new microarray set, so populate the info label with some basic stats.
/*
            String htmlText = "<html><body>"
                    + "<h3>" + proteinData.getLabel() + "</h3><br>"
                    + "<table>"
                    + "<tr><td>File:</td><td><b>" + proteinData.getFile().getAbsoluteFile() + "</b></td></tr>"
                    + "</table>"
                    + "</body></html>";
            infoLabel.setText(htmlText);
*/
        }
    }


    static class JmolPanel extends JPanel {
        JmolViewer viewer;
        JmolAdapter adapter;
        JmolPopup popup;
        MyStatusListener listener;

        JmolPanel() {
            adapter = new SmarterJmolAdapter(null);
            viewer = JmolViewer.allocateViewer(this, adapter);
            popup = new JmolPopupSwing(viewer);
            listener = new MyStatusListener(popup);
            viewer.setJmolStatusListener(listener);
        }

        public JmolSimpleViewer getViewer() {
            return viewer;
        }

        final Dimension currentSize = new Dimension();
        final Rectangle rectClip = new Rectangle();

        public void paint(Graphics g) {
            getSize(currentSize);
            g.getClipBounds(rectClip);
            viewer.renderScreenImage(g, currentSize, rectClip);
        }
    }

    static class MyStatusListener implements JmolStatusListener {

        JmolPopup jmolpopup;

        public MyStatusListener(JmolPopup jmolpopup) {
            this.jmolpopup = jmolpopup;
        }

        public void notifyFileLoaded(String fullPathName, String fileName,
                                     String modelName, Object clientFile,
                                     String errorMsg) {
            jmolpopup.updateComputedMenus();
        }

        public void setStatusMessage(String statusMessage) {
            if (statusMessage == null)
                return;
//            if (messageCallback != null && jsoWindow != null)
//                jsoWindow.call(messageCallback, new Object[]{htmlName, statusMessage});
//            showStatusAndConsole(statusMessage);
        }

        public void scriptEcho(String strEcho) {
            scriptStatus(strEcho);
        }

        public void scriptStatus(String strStatus) {
//            if (strStatus != null && messageCallback != null && jsoWindow != null)
//                jsoWindow.call(messageCallback, new Object[]{htmlName, strStatus});
//            consoleMessage(strStatus);
        }

        public void notifyScriptTermination(String errorMessage, int msWalltime) {
//            showStatusAndConsole("Jmol script completed");
//            if (buttonCallbackNotificationPending) {
//                System.out.println("!!!! calling back " + buttonCallback);
//                buttonCallbackAfter[0] = buttonName;
//                buttonWindow.call(buttonCallback, buttonCallbackAfter);
//            }
        }

        public void handlePopupMenu(int x, int y) {
            if (jmolpopup != null)
                jmolpopup.show(x, y);
        }

        public void measureSelection(int atomIndex) {
        }

        public void notifyMeasurementsChanged() {
        }

        public void notifyFrameChanged(int frameNo) {
            //System.out.println("notifyFrameChanged(" + frameNo +")");
//            if (animFrameCallback != null && jsoWindow != null)
//                jsoWindow.call(animFrameCallback,
//                        new Object[]{htmlName, new Integer(frameNo)});
        }

        public void notifyAtomPicked(int atomIndex, String strInfo) {
            //System.out.println("notifyAtomPicked(" + atomIndex + "," + strInfo +")");
//            showStatusAndConsole(strInfo);
//            if (pickCallback != null && jsoWindow != null)
//                jsoWindow.call(pickCallback,
//                        new Object[]{htmlName, strInfo, new Integer(atomIndex)});
        }

        public void showUrl(String urlString) {
        }

        public void showConsole(boolean showConsole) {
        }

    }

}
