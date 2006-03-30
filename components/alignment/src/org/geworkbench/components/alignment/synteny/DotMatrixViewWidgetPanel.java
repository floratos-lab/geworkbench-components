package org.geworkbench.components.alignment.synteny;

import java.awt.event.*;
import javax.swing.*;
import java.awt.LayoutManager;
import org.geworkbench.util.sequences.SequenceAnnotation;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.image.codec.jpeg.JPEGCodec;
import org.geworkbench.components.alignment.panels.BrowserLauncher;
import java.io.IOException;
import java.awt.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DotMatrixViewWidgetPanel
    extends JPanel implements MouseListener, MouseMotionListener {

    JScrollPane displayScrollPane = new JScrollPane();
    LayoutManager borderLayout2 = null;
    DotMatrixInfoPanel DMInfo = null;

    final int xOff = 150;
    final int yOff = 15;
    final int ruler = 10;
    final int annospace = 500;
    static double scaleX;
    static double scaleY;
    int info_pane_x;
    int info_pane_y;
    int dm_right;
    int dm_bottom;

    static DotMatrixObj DotM;
    static SequenceAnnotation annoX = null;
    static SequenceAnnotation annoY = null;

    boolean showDirect = true;
    boolean showInverted = true;
    boolean showAnnotationX = true;
    boolean showAnnotationY = true;
    String pos_info_string = new String(" ");
    static AnnotationGraphicalObjects agoX;
    static AnnotationGraphicalObjects agoY;
    static AnnotationViewWidget avwx= null;
    static AnnotationViewWidget avwy= null;
    JButton jButton1 = new JButton();
    public DotMatrixViewWidgetPanel() {

        DotM = null;
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*****************************************************/
    public DotMatrixViewWidgetPanel(DotMatrixObj dmo) {
        DotM = dmo;

        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*****************************************************/
    public void DMViewWidgetPaneladd(DotMatrixObj dmo, SequenceAnnotation sfox,
                                     SequenceAnnotation sfoy,
                                     DotMatrixInfoPanel dmi) {
        DotM = dmo;
        DMInfo = dmi;

        annoX = sfox;
        annoY = sfoy;

        agoX = new AnnotationGraphicalObjects(annoX);
        agoY = new AnnotationGraphicalObjects(annoY);
        scaleX = ( (double) (annoX.getSeqSegmentEnd() -
                             annoX.getSeqSegmentStart() +
                             1)) / DotM.getPixX();
        scaleY = ( (double) (annoY.getSeqSegmentEnd() -
                             annoY.getSeqSegmentStart() +
                             1)) / DotM.getPixY();
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*****************************************************/
    public DotMatrixViewWidgetPanel(DotMatrixObj dmo, SequenceAnnotation sfox,
                                    SequenceAnnotation sfoy) {
        DotM = dmo;

        annoX = sfox;
        annoY = sfoy;

        agoX = new AnnotationGraphicalObjects(annoX);
        agoY = new AnnotationGraphicalObjects(annoY);

        scaleX = ( (double) (annoX.getSeqSegmentEnd() -
                             annoX.getSeqSegmentStart() +
                             1)) / DotM.getPixX();
        scaleY = ( (double) (annoY.getSeqSegmentEnd() -
                             annoY.getSeqSegmentStart() +
                             1)) / DotM.getPixY();

        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*****************************************************/
    void jbInit() throws Exception {
        this.setLayout(borderLayout2);
        /*
                 displayScrollPane.setHorizontalScrollBarPolicy(JScrollPane.
            HORIZONTAL_SCROLLBAR_ALWAYS);
                 displayScrollPane.setVerticalScrollBarPolicy(JScrollPane.
                                                 VERTICAL_SCROLLBAR_ALWAYS);
         //        displayScrollPane.setBorder(BorderFactory.createEtchedBorder());
                 this.add(displayScrollPane);
         */

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setBackground(Color.white);
    }

    private void setMaxLength() {
    }

    public void initialize(DotMatrixObj dmObj) {
        DotM = DotsParser.dm;
//    this.addMouseListener(this);
        repaint();
    }

    public void paint(Graphics g) {
        int i, j, k;

        if (DotM == null) {
            return;
        }

        info_pane_x = xOff + 2 * ruler + DotM.getPixX();
        info_pane_y = yOff + 2 * ruler + DotM.getPixY();
        dm_right = xOff + ruler + DotM.getPixX();
        dm_bottom = yOff + ruler + DotM.getPixY();

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        Font f = new Font("Courier New", Font.PLAIN, 10);

        g.setColor(Color.black);
        g.clearRect(0, 0, getWidth(), getHeight());

        // draw the dotmatrix
        g.setFont(f);
        /*      JViewport scroller = (JViewport)this.getParent();
          Rectangle r = new Rectangle();
          r = scroller.getViewRect();
         */
        if (DotM == null) {
            return;
        }

        // draw rectangles
        g.setColor(Color.white);
        g.fillRect(0, 0, DotM.getPixX() + xOff + 2 * ruler + annospace,
                   DotM.getPixY() + yOff + 2 * ruler + annospace);

        g.setColor(Color.black);
        g.drawRect(ruler + xOff - 1, ruler + yOff - 1, DotM.getPixX() + 2,
                   DotM.getPixY() + 2);

        g.drawLine(ruler + xOff + DotM.getPixX() / 4,
                   ruler + yOff + DotM.getPixY(),
                   ruler + xOff + DotM.getPixX() / 4,
                   ruler + yOff + DotM.getPixY() + 2);
        g.drawLine(ruler + xOff + DotM.getPixX() / 2,
                   ruler + yOff + DotM.getPixY(),
                   ruler + xOff + DotM.getPixX() / 2,
                   ruler + yOff + DotM.getPixY() + 2);
        g.drawLine(ruler + xOff + 3 * DotM.getPixX() / 4,
                   ruler + yOff + DotM.getPixY(),
                   ruler + xOff + 3 * DotM.getPixX() / 4,
                   ruler + yOff + DotM.getPixY() + 2);

        g.drawLine(ruler + xOff, ruler + yOff + DotM.getPixY() / 4,
                   ruler + xOff - 2,
                   ruler + yOff + DotM.getPixY() / 4);
        g.drawLine(ruler + xOff, ruler + yOff + DotM.getPixY() / 2,
                   ruler + xOff - 2,
                   ruler + yOff + DotM.getPixY() / 2);
        g.drawLine(ruler + xOff, ruler + yOff + 3 * DotM.getPixY() / 4,
                   ruler + xOff - 2,
                   ruler + yOff + 3 * DotM.getPixY() / 4);

        /* Draw ruler */
        if (DotM != null) {
            /* along X */
            i = (DotM.getEndX() - DotM.getStartX()) / 4;
            /* to the neares round number */

        }

        // Draw information
        g.drawString("X: " + DotM.getDescriptionX(), 10, 10);
        g.drawString("Y: " + DotM.getDescriptionY(), 10, 20);

        if (DotM != null) {
            // draw direct matches
            if (showDirect) {
                // Set direct style
                for (i = 0; i < DotM.getPixX(); i++) {
                    g.setColor(Color.black);
                    for (j = 0; j < DotM.getPixX(); j++) {
                        if (DotM.getDirectPixel(i, j) == '1') {
                            g.drawLine(i + xOff + ruler, j + yOff + ruler,
                                       i + xOff + ruler,
                                       j + yOff + ruler);
                        }
                    }
                }
            }

            // Draw inverted matches
            if (showInverted) {
                // Set inverted style
                g.setColor(Color.blue);
                for (i = 0; i < DotM.getPixX(); i++) {
                    for (j = 0; j < DotM.getPixX(); j++) {
                        if (DotM.getInvertedPixel(i, j) == '1') {
                            g.drawLine(i + xOff + ruler, j + yOff + ruler,
                                       i + xOff + ruler,
                                       j + yOff + ruler);
                        }
                    }
                }
            }

            // Draw additional information
            /* Setting colors */
            /* For X axis */
            if (annoX.getAnnotationTrackNum() > annoY.getAnnotationTrackNum()) {
                for (i = 0; i < annoX.getAnnotationTrackNum(); i++) {
                    annoX.getAnnotationTrack(i).setColorNum(i);
                    String NameX = (annoX.getAnnotationTrack(i)).
                        getAnnotationName();
                    for (j = 0; j < annoY.getAnnotationTrackNum(); j++) {
                        String NameY = (annoY.getAnnotationTrack(j)).
                            getAnnotationName();
                        if (NameY.equalsIgnoreCase(NameX)) {
                            annoY.getAnnotationTrack(j).setColorNum(i);
                        }
                    }
                }
            }
            else {
                for (i = 0; i < annoY.getAnnotationTrackNum(); i++) {
                    annoY.getAnnotationTrack(i).setColorNum(i);
                    String NameY = (annoY.getAnnotationTrack(i)).
                        getAnnotationName();
                    for (j = 0; j < annoX.getAnnotationTrackNum(); j++) {
                        String NameX = (annoX.getAnnotationTrack(j)).
                            getAnnotationName();
                        if (NameX.equalsIgnoreCase(NameY)) {
                            annoX.getAnnotationTrack(j).setColorNum(i);
                        }
                    }
                }
            }

            /* draw annotation */
            if (showAnnotationX) {
                drawAnnoX(g2d);
            }
            else {
                g.setColor(Color.white);
                g.fillRect(xOff + ruler - 1, yOff + 2 * ruler + DotM.getPixY(),
                           DotM.getPixX() + 2, annospace);
            }
            if (showAnnotationY) {
                drawAnnoY(g2d);
            }
            else {
                g.setColor(Color.white);
                g.fillRect(xOff + 2 * ruler + DotM.getPixX(), yOff + ruler - 1,
                           annospace,
                           DotM.getPixY() + 2);
            }
        }
    }

    private void drawAnnoX(Graphics2D g2d) {
        int wx = DotM.getEndX() - DotM.getStartX() + 1;
        int wy = DotM.getEndY() - DotM.getStartY() + 1;
        int awidth = (DotM.getPixX() * wx) / wy;

        if (awidth > DotM.getPixX()) {
            awidth = DotM.getPixX();
        }

        avwx = new AnnotationViewWidget(g2d, annoX, agoX,
                                        xOff + ruler, yOff + 2 * ruler + DotM.getPixY(), awidth,
                                        annospace, 0, DotM.getStartX(), DotM.getEndX());
    }

    private void drawAnnoY(Graphics2D g2d) {
        int wx = DotM.getEndX() - DotM.getStartX() + 1;
        int wy = DotM.getEndY() - DotM.getStartY() + 1;
        int awidth = (DotM.getPixY() * wy) / wx;
        if (awidth > DotM.getPixY()) {
            awidth = DotM.getPixY();
        }

        avwy = new AnnotationViewWidget(g2d, annoY, agoY,
                                        xOff + 2 * ruler + DotM.getPixX(),
                                        yOff + ruler, annospace, awidth, 1,
                                        DotM.getStartY(), DotM.getEndY());
    }

    public void saveToJpeg() {
        String fn = null;
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save current image to jpeg.");
        int returnVal = fc.showSaveDialog(this.getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fn = fc.getSelectedFile().getAbsolutePath();
        }

        Dimension size = this.getSize();
        size.setSize(1050,900);
        BufferedImage myImage = new BufferedImage(size.width, size.height,
                                                  BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = myImage.createGraphics();
        this.paint(g2);
        try {
            FileOutputStream out = new FileOutputStream(fn);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            encoder.encode(myImage);
            out.close();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }


    public void setShowDirect(boolean all) {
        showDirect = all;
    }

    public void setShowInverted(boolean all) {
        showInverted = all;
    }

    public void setShowAnnoX(boolean all) {
        showAnnotationX = all;
    }

    public void setShowAnnoY(boolean all) {
        showAnnotationY = all;
    }

// Mouse stuff
    public void mouseMoved(MouseEvent e) {
        int i, x, y;

        if (DotM == null) {
            return;
        }

        x = e.getX();
        y = e.getY();
        if (x > (xOff + ruler) && x < dm_right) {
            /* The mouse is in X zone */
            if (y > (yOff + ruler) && y < dm_bottom) {
                /* The mouse is in dotmatrix */
                DMInfo.showInfo("\nX: " +
                                (int) ( ( (x - xOff - ruler) * scaleX) +
                                       DotM.getStartX()) +
                                " [ " + (int) ( (x - xOff - ruler) * scaleX) +
                                " ]  Y: " +
                                (int) ( ( (y - yOff - ruler) * scaleY) +
                                       DotM.getStartY()) +
                                " [ " + (int) ( (y - yOff - ruler) * scaleY) +
                                " ]");
            }
            else {
                if (y > dm_bottom + ruler) {
                    if ( (i = agoX.getHit(x, y)) != -1) {
                        if ( (annoX.getAnnotationTrack(agoX.getTrackNum(i))).
                            getFeatureTag(
                                agoX.getNumInTrack(i)) != null) {
                            DMInfo.showInfo( (annoX.getAnnotationTrack(agoX.
                                getTrackNum(i))).getFeatureTag(agoX.
                                getNumInTrack(i)) + " { " +
                                            (annoX.
                                             getAnnotationTrack(agoX.
                                getTrackNum(i))).getAnnotationName() + " } "
                                );
                        }
                    }
                }
                else {
                    DMInfo.showInfo(" ");
                }
            }
        }
        else {
            if (x > dm_right + ruler && y < dm_bottom) {
                if ( (i = agoY.getHit(x, y)) != -1) {
                    if ( (annoY.getAnnotationTrack(agoY.getTrackNum(i))).
                        getFeatureTag(
                            agoY.getNumInTrack(i)) != null) {
                        DMInfo.showInfo( (annoY.getAnnotationTrack(agoY.
                            getTrackNum(i))).getFeatureTag(agoY.getNumInTrack(i))
                                        + " { " +
                                        (annoY.getAnnotationTrack(agoY.
                            getTrackNum(i))).getAnnotationName() + " } ");
                    }
                }
            }
            else {
                DMInfo.showInfo(" ");
            }
        }
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseClicked(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        Graphics gg;
        int i, j, k, x, y;
        String line;

        if (DotM == null) {
            return;
        }

        x = e.getX();
        y = e.getY();
        gg = this.getGraphics();

        if (x > (xOff + ruler) && x < dm_right) {
            /* The mouse is in X zone */
            if (y > (yOff + ruler) && y < dm_bottom) {
                /* The mouse is in dotmatrix */
            }
            else {
                if (y > dm_bottom + ruler) {
                    avwx.AnnotationMenu(x,y);
                    /* The mouse is in annotation zone */

                    if ( (i = agoX.getHit(x, y)) != -1) {
                        if ( (annoX.getAnnotationTrack(agoX.getTrackNum(i))).
                            getFeatureURL(i) != null) {
                            try {
                                BrowserLauncher.openURL( (annoX.
                                    getAnnotationTrack(agoX.getTrackNum(i))).
                                    getFeatureURL(i));
                            }
                            catch (IOException ioe) {}
                        }
                    }
                }
                else {
                }
            }
        }
        else {
            if (x > dm_right + ruler && y < dm_bottom) {
                if ( (i = agoY.getHit(x, y)) != -1) {
                    avwy.AnnotationMenu(x,y);
                    if ( (annoY.getAnnotationTrack(agoY.getTrackNum(i))).
                        getFeatureURL(i) != null) {
                        try {
                            BrowserLauncher.openURL( (annoY.
                                getAnnotationTrack(agoY.getTrackNum(i))).
                                getFeatureURL(i));
                        }
                        catch (IOException ioe) {}
                    }
                }
            }
            else {
            }
        }
    }

    public void mouseDragged(MouseEvent e) {}

    /**
     * isInitiated
     *
     * @return boolean
     */
    public boolean isInitiated() {
        if (DotM != null) {
            return true;
        }
        else {
            return false;
        }
    }
}
