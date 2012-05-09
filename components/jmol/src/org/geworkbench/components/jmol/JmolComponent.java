package org.geworkbench.components.jmol;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.jmol.api.JmolSimpleViewer;
import org.jmol.api.JmolAdapter;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;

@AcceptTypes({DSProteinStructure.class})
public class JmolComponent extends JPanel implements VisualPlugin {

	private static final long serialVersionUID = -7312121815189456386L;
    static Log log = LogFactory.getLog(JmolComponent.class);

    private DSProteinStructure proteinData;

    final static String strXyzHOH =
            "3\n" +
                    "water\n" +
                    "O  0.0 0.0 0.0\n" +
                    "H  0.76923955 -0.59357141 0.0\n" +
                    "H -0.76923955 -0.59357141 0.0\n";

    final static String strScript = //"delay; move 360 0 0 0 0 0 0 0 4;";
	"wireframe off; spacefill off; cartoons; color structure;";
    final static int zoomMin = 5, zoomMax = 200000, zoomNorm = 100;

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
        DSDataSet<?> dataSet = event.getDataSet();
        // We will act on this object if it is a DSMicroarraySet
        if (dataSet instanceof DSProteinStructure) {
            proteinData = (DSProteinStructure) dataSet;

            JmolSimpleViewer viewer = jmolPanel.getViewer();
            //    viewer.openFile("../samples/caffeine.xyz");
            //    viewer.openFile("http://database.server/models/1pdb.pdb.gz");

            /*byte[] fileBytes;
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
            viewer.openStringInline(new String(fileBytes));*/
            String msg = viewer.openFile(proteinData.getFile().getAbsolutePath());
            if (msg != null && msg.contains("Error"))
            	JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
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
		private static final long serialVersionUID = -6989654578372619887L;
		JmolSimpleViewer viewer;
        JmolAdapter adapter;

        JmolPanel() {
            adapter = new SmarterJmolAdapter();
            viewer = JmolSimpleViewer.allocateSimpleViewer(this, adapter);
        }

        public JmolSimpleViewer getViewer() {
            return viewer;
        }

        final Dimension currentSize = new Dimension();
        final Rectangle rectClip = new Rectangle();

        public void paint(Graphics g) {
			//double zoom = getZoom();
    		//if (zoom != 100) viewer.evalString("zoom "+zoom);

            getSize(currentSize);
            g.getClipBounds(rectClip);
            viewer.renderScreenImage(g, currentSize.width, currentSize.height);
        }

/*
        private double getZoom() {
        	double zoom = zoomNorm;
        	int width = getVisibleRect().width;
        	int height = getVisibleRect().height;
        	if (width != height)
        		zoom = height < width ? 100.0*height/width : 100.0*width/height;
        	if (zoom < zoomMin || zoom > zoomMax) zoom = zoomNorm;
        	return zoom;
        }
*/
    }

}
