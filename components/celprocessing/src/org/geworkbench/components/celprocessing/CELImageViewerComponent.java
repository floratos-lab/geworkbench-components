package org.geworkbench.components.celprocessing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.AffineTransformOp;
import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSProbeIntensityArray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSProbeIntensityArray;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.events.ProjectEvent;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

@AcceptTypes({DSProbeIntensityArray.class})
public class CELImageViewerComponent extends JPanel implements VisualPlugin {

    static Log log = LogFactory.getLog(CELImageViewerComponent.class);

    private CSProbeIntensityArray probeData;
    private JLabel infoLabel;

    private JPanel imagePanel;

    private static final int CHIPIMAGE_SPACER = 10;

    private JPanel parentPanel;
    private JPanel chipContainer;
    private ChipImagePanel currentZoomedChip;
    private boolean zoomMode = false;
    private Rectangle visibleRect;

    public CELImageViewerComponent() {
        setLayout(new BorderLayout());
        parentPanel = this;
//        imagePanel = new JPanel();
//        add(imagePanel, BorderLayout.CENTER);

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
        if (dataSet instanceof DSProbeIntensityArray) {
            probeData = (CSProbeIntensityArray) dataSet;

            int gridSize = 1;

            chipContainer = new JPanel(new GridLayout(gridSize, gridSize)) {
                public Dimension getPreferredSize() {
                    return parentPanel.getSize();
                }
            };
            chipContainer.setBackground(Color.white);
            ChipImagePanel chipView = new ChipImagePanel(this, probeData, probeData.getMinMax()[0], probeData.getMinMax()[1]);
            chipContainer.add(chipView);
            chipContainer.addComponentListener(chipView);
            zoomMode = false;
            setProperView();

        }
    }

    private void setProperView() {
        if (zoomMode && currentZoomedChip != null) {
            parentPanel.removeAll();
            JScrollPane scroller = new JScrollPane();
            parentPanel.add(scroller);
            scroller.getViewport().setView(currentZoomedChip);
            parentPanel.revalidate();
            parentPanel.repaint();
//            System.out.println("Now in zoomed mode.");
        } else {
            parentPanel.removeAll();
            parentPanel.add(chipContainer);
            parentPanel.revalidate();
            parentPanel.repaint();
//            System.out.println("Now in regular mode.");
        }
    }

    private void setZoomedChip(ChipImagePanel chipImagePanel) {
        if (!zoomMode) {
            zoomMode = true;
            currentZoomedChip = chipImagePanel;
            setProperView();
        } else {
            zoomMode = false;
            currentZoomedChip = null;
            setProperView();
        }
    }

    static class ChipImagePanel extends JPanel implements ComponentListener, MouseListener {
        int LABEL_HEIGHT = 20;
        BufferedImage image;
        String name = "";
        float[] scaleAmount = new float[] {1,1};
        CELImageViewerComponent parentPlugin;

        public ChipImagePanel(CELImageViewerComponent parentPlugin, BufferedImage image) {
            this.parentPlugin = parentPlugin;
            this.image = image;
            addMouseListener(this);
        }

        public ChipImagePanel(CELImageViewerComponent parentPlugin, CSProbeIntensityArray array, float min, float max) {
            this.parentPlugin = parentPlugin;
            float[][] intensities = array.getProbeIntensities();
            setSize(intensities.length, intensities[0].length);
            this.image = new BufferedImage(intensities.length, intensities[0].length + LABEL_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.setColor(Color.gray);
            name = array.getLabel();
            g.drawString(name, 0, image.getHeight() - 2);

            for (int i = 0; i < intensities.length; i++) {
                float[] floats = intensities[i];
                for (int j = 0; j < floats.length; j++) {
                    float v = (float) Math.min(Math.abs((Math.log10(floats[j]) - min)) / max, 1f);
                    g.setColor(new Color(v, v, v));
                    g.drawLine(j, i, j, i);
                }
            }
            addMouseListener(this);
        }

        public void paint(Graphics g) {
            Rectangle clip = g.getClipBounds();
            g.setColor(Color.WHITE);
            // g.clearRect(0, 0, width + rowHeaderWidth, height + columnHeaderHeight);
            g.fillRect(clip.x, clip.y, clip.width, clip.height);

            AffineTransform trans = new AffineTransform();
            trans.scale(scaleAmount[0], scaleAmount[1]);
            int startPoint = (int) ((this.getWidth() - (image.getWidth() * scaleAmount[0])) / 2);
            ((Graphics2D) g).drawImage(image, new AffineTransformOp(trans, AffineTransformOp.TYPE_NEAREST_NEIGHBOR), startPoint, 0);
        }

        public Dimension getPreferredSize() {
            return new Dimension((int) (image.getWidth() * scaleAmount[0]), (int) ((image.getHeight() + LABEL_HEIGHT) * scaleAmount[1]));
        }

        protected ChipImagePanel makeCopy() {
            return new ChipImagePanel(parentPlugin, image);
        }

        public void componentResized(ComponentEvent e) {
//            System.out.println("Resize: This chip size now "+getWidth()+", "+getHeight());
            calculateScale();
            repaint();
//            System.out.println("This scale amount now "+scaleAmount[0]+", "+scaleAmount[1]);
        }

        private void calculateScale() {
            if (getWidth() < getHeight()) {
                scaleAmount[0] = (getWidth() - CHIPIMAGE_SPACER) / (float) image.getWidth();
                scaleAmount[1] = scaleAmount[0];
            } else {
                scaleAmount[0] = (getHeight() - CHIPIMAGE_SPACER) / (float) image.getHeight();
                scaleAmount[1] = scaleAmount[0];
            }
        }

        public void mouseClicked(MouseEvent e) {
            parentPlugin.setZoomedChip(makeCopy());
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
            Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
            setCursor(cursor);
        }

        public void mouseExited(MouseEvent e) {
            Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);
            setCursor(cursor);
        }

        public void componentMoved(ComponentEvent e) {
        }

        public void componentShown(ComponentEvent e) {
            System.out.println("Resize: This chip size now "+getWidth()+", "+getHeight());
        }

        public void componentHidden(ComponentEvent e) {
        }
    }

}
