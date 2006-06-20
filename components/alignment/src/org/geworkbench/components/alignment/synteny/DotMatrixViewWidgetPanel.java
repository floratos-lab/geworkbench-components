package org.geworkbench.components.alignment.synteny;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import org.geworkbench.components.alignment.panels.BrowserLauncher;
import org.geworkbench.util.sequences.SequenceAnnotation;

import javax.swing.*;
import javax.swing.text.Style;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class DotMatrixViewWidgetPanel
        extends JPanel implements MouseListener, MouseMotionListener {

    JScrollPane displayScrollPane = new JScrollPane();
    LayoutManager borderLayout2 = null;
    DotMatrixInfoPanel DMInfo = null;
    JPopupMenu AnnoMenu = new JPopupMenu();
    JMenuItem browsFeature = new JMenuItem();
    JMenuItem markFeature = new JMenuItem();
    JMenuItem browsRegion = new JMenuItem();
    ActionListener annoListener;

    long when = 0;
    final int xOff = 150;
    final int yOff = 15;
    final int ruler = 10;
    final int annospace = 500;
    static double scaleX;
    static double scaleY;
    int ms_x, ms_y;
    int info_pane_x;
    int info_pane_y;
    int dm_right;
    int dm_bottom;

    static DotMatrixObj DotM;
    static SequenceAnnotation annoX = null;
    static SequenceAnnotation annoY = null;
    static SequenceAnnotation curAnno = null;

    boolean showDirect = true;
    boolean showInverted = true;
    boolean showAnnotationX = true;
    boolean showAnnotationY = true;
    String pos_info_string = " ";
    static AnnotationGraphicalObjects agoX;
    static AnnotationGraphicalObjects agoY;
    static AnnotationGraphicalObjects curAgo;

    public DotMatrixViewWidgetPanel() {

        DotM = null;
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * *************************************************
     */
    public DotMatrixViewWidgetPanel(DotMatrixObj dmo) {
        DotM = dmo;

        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * *************************************************
     */
    public void DMViewWidgetPaneladd(DotMatrixObj dmo, SequenceAnnotation sfox,
                                     SequenceAnnotation sfoy,
                                     DotMatrixInfoPanel dmi) {
        DotM = dmo;
        DMInfo = dmi;

        annoX = sfox;
        annoY = sfoy;

        agoX = new AnnotationGraphicalObjects(annoX);
        agoY = new AnnotationGraphicalObjects(annoY);
        scaleX = ((double) (annoX.getSeqSegmentEnd() -
                annoX.getSeqSegmentStart() +
                1)) / DotM.getPixX();
        scaleY = ((double) (annoY.getSeqSegmentEnd() -
                annoY.getSeqSegmentStart() +
                1)) / DotM.getPixY();
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * *************************************************
     */
    public DotMatrixViewWidgetPanel(DotMatrixObj dmo, SequenceAnnotation sfox,
                                    SequenceAnnotation sfoy) {
        DotM = dmo;

        annoX = sfox;
        annoY = sfoy;

        agoX = new AnnotationGraphicalObjects(annoX);
        agoY = new AnnotationGraphicalObjects(annoY);

        scaleX = ((double) (annoX.getSeqSegmentEnd() -
                annoX.getSeqSegmentStart() +
                1)) / DotM.getPixX();
        scaleY = ((double) (annoY.getSeqSegmentEnd() -
                annoY.getSeqSegmentStart() +
                1)) / DotM.getPixY();

        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * *************************************************
     */
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

        browsFeature.setText("USCS genome browser for feature");
        browsRegion.setText("USCS genome browser for region");
        markFeature.setText("Mark feature");
        annoListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                annoListener_actionPerformed(e);
            }
        };

        browsFeature.addActionListener(annoListener);
        browsRegion.addActionListener(annoListener);
        markFeature.addActionListener(annoListener);

        AnnoMenu.add(browsFeature);
        AnnoMenu.add(browsRegion);
        AnnoMenu.add(markFeature);
    }

    public void initialize(DotMatrixObj dmObj) {
        DotM = DotsParser.dm;
//    this.addMouseListener(this);
        repaint();
    }

    public void paint(Graphics g) {
        int i, j;

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
        g.fillRect(0, 0, DotM.getPixX() + xOff + 2 * ruler + 2*annospace,
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

        // Draw information
        g.drawString("X: " + DotM.getDescriptionX(), 10, 10);
        g.drawString("Y: " + DotM.getDescriptionY(), 10, 20);

        // Draw additional information
        // Draw marking lines
        for(i=0;i<DotM.getMarkLinesXnum();i++){
            g.setColor(Color.lightGray);
            g.drawLine(ruler + xOff + DotM.markLinesX[i],
                    ruler + yOff,
                    ruler + xOff + DotM.markLinesX[i],
                    ruler + yOff + DotM.getPixY());
        }
        for(i=0;i<DotM.getMarkLinesYnum();i++){
            g.setColor(Color.lightGray);
            g.drawLine(ruler + xOff,
                    ruler + yOff + DotM.markLinesY[i],
                    ruler + xOff + DotM.getPixX(),
                    ruler + yOff + DotM.markLinesY[i]);
        }

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

/*            DotM.markLinesXnum=2;
            DotM.markLinesX[0]=20;
            DotM.markLinesX[1]=40;
*/

/*            for(i=0;i<DotM.markLinesYnum;i++){
                g.setColor(Color.gray);
                g.drawLine(ruler + xOff + DotM.markLinesY[i],
                        ruler + yOff+1,
                        ruler + xOff + DotM.markLinesY[i],
                        ruler + yOff + DotM.getPixY() + 1);
            }
*/
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
            } else {
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
            } else {
                g.setColor(Color.white);
                g.fillRect(xOff + ruler - 1, yOff + 2 * ruler + DotM.getPixY(),
                        DotM.getPixX() + 2, annospace);
            }
            if (showAnnotationY) {
                drawAnnoY(g2d);
            } else {
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

        new AnnotationViewWidget(g2d, annoX, agoX,
                xOff + ruler, yOff + 2 * ruler + DotM.getPixY(), awidth,
                annospace, 0, DotM.getStartX(), DotM.getEndX());
    }

    public void saveToJpeg() {
        String fn = null;
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save current image to jpeg.");
        int returnVal = fc.showOpenDialog(this.getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fn = fc.getSelectedFile().getAbsolutePath();
        }

        Dimension size = this.getSize();
        size.setSize(1050, 900);
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

    private void drawAnnoY(Graphics2D g2d) {
        int wx = DotM.getEndX() - DotM.getStartX() + 1;
        int wy = DotM.getEndY() - DotM.getStartY() + 1;
        int awidth = (DotM.getPixY() * wy) / wx;
        if (awidth > DotM.getPixY()) {
            awidth = DotM.getPixY();
        }

        new AnnotationViewWidget(g2d, annoY, agoY,
                xOff + 2 * ruler + DotM.getPixX(),
                yOff + ruler, annospace, awidth, 1,
                DotM.getStartY(), DotM.getEndY());
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
                        (int) (((x - xOff - ruler) * scaleX) +
                                DotM.getStartX()) +
                        " [ " + (int) ((x - xOff - ruler) * scaleX) +
                        " ]  Y: " +
                        (int) (((y - yOff - ruler) * scaleY) +
                                DotM.getStartY()) +
                        " [ " + (int) ((y - yOff - ruler) * scaleY) +
                        " ]");
            } else {
                if (y > dm_bottom + ruler) {
                    if ((i = agoX.getHit(x, y)) != -1) {
                        if ((annoX.getAnnotationTrack(agoX.getTrackNum(i))).
                                getFeatureTag(
                                        agoX.getNumInTrack(i)) != null) {
                            DMInfo.showInfo((annoX.getAnnotationTrack(agoX.
                                    getTrackNum(i))).getFeatureTag(agoX.
                                    getNumInTrack(i)) + " { " +
                                    (annoX.
                                            getAnnotationTrack(agoX.
                                            getTrackNum(i))).getAnnotationName() + " } "
                            );
                        }
                    }
                } else {
                    DMInfo.showInfo(" ");
                }
            }
        } else {
            if (x > dm_right + ruler && y < dm_bottom) {
                if ((i = agoY.getHit(x, y)) != -1) {
                    if ((annoY.getAnnotationTrack(agoY.getTrackNum(i))).
                            getFeatureTag(
                                    agoY.getNumInTrack(i)) != null) {
                        DMInfo.showInfo((annoY.getAnnotationTrack(agoY.
                                getTrackNum(i))).getFeatureTag(agoY.getNumInTrack(i))
                                + " { " +
                                (annoY.getAnnotationTrack(agoY.
                                        getTrackNum(i))).getAnnotationName() + " } ");
                    }
                }
            } else {
                DMInfo.showInfo(" ");
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        int x, y;

        if (DotM == null) {
            return;
        }

        ms_x = x = e.getX();
        ms_y = y = e.getY();

        if (x > (xOff + ruler) && x < dm_right) {
            /* The mouse is in X zone */
            if (y > (yOff + ruler) && y < dm_bottom) {
                /* The mouse is in dotmatrix */
            } else {

                if (y > dm_bottom + ruler) {
                    if (agoX.getHit(ms_x, ms_y) != -1) {
                        browsFeature.setEnabled(true);
                        markFeature.setEnabled(true);
                    } else {
                        browsFeature.setEnabled(false);
                        markFeature.setEnabled(false);
                    }

                    curAnno = annoX;
                    curAgo=agoX;
                    /* The mouse is in annotation zone */
                    AnnoMenu.show(e.getComponent(), x, y);

                } else {
                }
            }
        } else {
            if (x > dm_right + ruler && y < dm_bottom) {
                if (agoY.getHit(x, y) != -1) {
                    browsFeature.setEnabled(true);
                    markFeature.setEnabled(true);
                } else {
                    browsFeature.setEnabled(false);
                    markFeature.setEnabled(false);
                }
                curAnno = annoY;
                curAgo=agoY;
                AnnoMenu.show(e.getComponent(), x, y);
            }
        }
    }

    private void annoListener_actionPerformed(ActionEvent e) {
        int i, tn, nit, strt, ennd, fs=0, fe=0;
        String feature_url, genm, chrom;

        if (e.getWhen() == when)
            return;
        else
            when = e.getWhen();

        if(curAnno == null || curAgo == null)
            return;

        strt = curAnno.getSeqSegmentStart();
        ennd = curAnno.getSeqSegmentEnd();
        genm = curAnno.getGenome();
        chrom = curAnno.getChromosome();

        String str = e.getActionCommand();

        if ((i = curAgo.getHit(ms_x, ms_y)) != -1) {
            tn = curAgo.getTrackNum(i);
            nit = curAgo.getNumInTrack(i);
            fs = curAnno.getAnnotationTrack(tn).getSequenceHitStart(nit);
            fe = curAnno.getAnnotationTrack(tn).getSequenceHitEnd(nit);
            feature_url = curAnno.getAnnotationTrack(tn).getFeatureURL(nit);
        } else {
            feature_url = null;
        }

        if (str.indexOf("for feature") >= 0) {
            if (feature_url != null) {
                try {
                    BrowserLauncher.openURL(feature_url);
                }
                catch (IOException ioe) {
                    System.err.println(ioe);
                }
            }
        }
        if (str.indexOf("for region") >= 0) {
            try {
                BrowserLauncher.openURL("http://genome.ucsc.edu/cgi-bin/hgTracks?db=" + genm + "&position=" + chrom + ":" + strt + "-" + ennd);
            }
            catch (IOException ioe) {
                System.err.println(ioe);
            }
        }
        if (str.indexOf("Mark feature") >= 0) {
            if (feature_url != null) {
                 if(curAnno == annoX){
                     DotM.addMarkX(fs);
                     DotM.addMarkX(fe);
                     this.paint(this.getGraphics());
                 }
                if(curAnno == annoY){
                    DotM.addMarkY(fs);
                    DotM.addMarkY(fe);
                    this.paint(this.getGraphics());
                }
            }
        }

    }

    public void mouseDragged(MouseEvent e) {
    }

    /**
     * isInitiated
     *
     * @return boolean
     */
    public boolean isInitiated() {
        return (DotM != null);
    }
}
