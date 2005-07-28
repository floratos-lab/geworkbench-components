package org.geworkbench.components.alignment.synteny;

import org.geworkbench.components.alignment.synteny.*;
import org.geworkbench.components.alignment.synteny.AnnotationGraphicalObjects;
import org.geworkbench.components.alignment.synteny.AnnotationColors;
import org.geworkbench.components.alignment.synteny.AnnotationViewWidget;
import org.geworkbench.util.sequences.SequenceAnnotation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class DotMatrixViewWidgetPanel extends JPanel implements MouseListener, MouseMotionListener {

    final int xOff = 15;
    final int yOff = 15;
    final int ruler = 10;
    final int annospace = 500;
    static double scaleX;
    static double scaleY;
    int info_pane_x;
    int info_pane_y;
    int dm_right;
    int dm_bottom;

    private JScrollPane scPane = new JScrollPane();

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

    public DotMatrixViewWidgetPanel() {

        DotM = null;
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public DotMatrixViewWidgetPanel(DotMatrixObj dmo) {
        DotM = dmo;

        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public DotMatrixViewWidgetPanel(DotMatrixObj dmo, SequenceAnnotation sfox, SequenceAnnotation sfoy) {
        DotM = dmo;

        annoX = sfox;
        annoY = sfoy;

        agoX = new AnnotationGraphicalObjects(annoX);
        agoY = new org.geworkbench.components.alignment.synteny.AnnotationGraphicalObjects(annoY);

        scaleX = ((double) (annoX.getSeqSegmentEnd() - annoX.getSeqSegmentStart() + 1)) / DotM.getPixX();
        scaleY = ((double) (annoY.getSeqSegmentEnd() - annoY.getSeqSegmentStart() + 1)) / DotM.getPixY();

        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setBackground(Color.green);

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
        g.fillRect(0, 0, DotM.getPixX() + xOff + 2 * ruler + annospace, DotM.getPixY() + yOff + 2 * ruler + annospace);

        g.setColor(Color.black);
        g.drawRect(ruler + xOff - 1, ruler + yOff - 1, DotM.getPixX() + 2, DotM.getPixY() + 2);

        g.drawLine(ruler + xOff + DotM.getPixX() / 4, ruler + yOff + DotM.getPixY(), ruler + xOff + DotM.getPixX() / 4, ruler + yOff + DotM.getPixY() + 2);
        g.drawLine(ruler + xOff + DotM.getPixX() / 2, ruler + yOff + DotM.getPixY(), ruler + xOff + DotM.getPixX() / 2, ruler + yOff + DotM.getPixY() + 2);
        g.drawLine(ruler + xOff + 3 * DotM.getPixX() / 4, ruler + yOff + DotM.getPixY(), ruler + xOff + 3 * DotM.getPixX() / 4, ruler + yOff + DotM.getPixY() + 2);

        g.drawLine(ruler + xOff, ruler + yOff + DotM.getPixY() / 4, ruler + xOff - 2, ruler + yOff + DotM.getPixY() / 4);
        g.drawLine(ruler + xOff, ruler + yOff + DotM.getPixY() / 2, ruler + xOff - 2, ruler + yOff + DotM.getPixY() / 2);
        g.drawLine(ruler + xOff, ruler + yOff + 3 * DotM.getPixY() / 4, ruler + xOff - 2, ruler + yOff + 3 * DotM.getPixY() / 4);

        /* Draw ruler */
        if (DotM != null) {
            /* along X */
            i = (DotM.getEndX() - DotM.getStartX()) / 4;
            /* to the neares round number */

        }

        if (DotM != null) {
            // draw direct matches
            if (showDirect) {
                // Set direct style
                for (i = 0; i < DotM.getPixX(); i++) {
                    g.setColor(Color.black);
                    for (j = 0; j < DotM.getPixX(); j++) {
                        if (DotM.getDirectPixel(i, j) == '1') {
                            g.drawLine(i + xOff + ruler, j + yOff + ruler, i + xOff + ruler, j + yOff + ruler);
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
                            g.drawLine(i + xOff + ruler, j + yOff + ruler, i + xOff + ruler, j + yOff + ruler);
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
                    String NameX = (annoX.getAnnotationTrack(i)).getAnnotationName();
                    for (j = 0; j < annoY.getAnnotationTrackNum(); j++) {
                        String NameY = (annoY.getAnnotationTrack(j)).getAnnotationName();
                        if (NameY.equalsIgnoreCase(NameX)) {
                            annoY.getAnnotationTrack(j).setColorNum(i);
                        }
                    }
                }
            } else {
                for (i = 0; i < annoY.getAnnotationTrackNum(); i++) {
                    annoY.getAnnotationTrack(i).setColorNum(i);
                    String NameY = (annoY.getAnnotationTrack(i)).getAnnotationName();
                    for (j = 0; j < annoX.getAnnotationTrackNum(); j++) {
                        String NameX = (annoX.getAnnotationTrack(j)).getAnnotationName();
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
                g.fillRect(xOff + ruler - 1, yOff + 2 * ruler + DotM.getPixY(), DotM.getPixX() + 2, annospace);
            }
            if (showAnnotationY) {
                drawAnnoY(g2d);
            } else {
                g.setColor(Color.white);
                g.fillRect(xOff + 2 * ruler + DotM.getPixX(), yOff + ruler - 1, annospace, DotM.getPixY() + 2);
            }

            /* Clearing legends space */
            g.setColor(Color.white);
            g.fillRect(info_pane_x - ruler + 1, info_pane_y - ruler + 1, annospace + ruler, annospace + ruler);

            /* Draw annotation legends *************************************/
            /* First - draw all from annoX */
            for (i = 0, j = 0; i < annoX.getAnnotationTrackNum(); i++) {
                if (annoX.getAnnoTrackActive(i)) {
                    g.setColor(AnnotationColors.AnnoColorMap[annoX.getAnnotationTrack(i).getColorNum()]);
                    g.fillRect(info_pane_x + 20, info_pane_y + 10 + j * 15, 10, 10);
                    g.setColor(Color.black);
                    g.drawString(annoX.getAnnotationTrack(i).getAnnotationName(), info_pane_x + 40, info_pane_y + 15 + j * 15);
                    j++;
                }
            }
            /* Second draw all annotation from annoY, which is not in annoX */
            boolean flag = true;
            for (i = 0; i < annoY.getAnnotationTrackNum(); i++) {
                if (annoY.getAnnoTrackActive(i)) {
                    flag = true;
                    for (k = 0; k < annoX.getAnnotationTrackNum(); k++) {
                        if (annoX.getAnnoTrackActive(k)) {
                            if ((annoY.getAnnotationTrack(i).getAnnotationName()).compareTo(annoX.getAnnotationTrack(k).getAnnotationName()) == 0) {
                                flag = false;
                                break;
                            }
                        }
                    }

                    if (flag) {
                        g.setColor(AnnotationColors.AnnoColorMap[annoY.getAnnotationTrack(i).getColorNum()]);
                        g.fillRect(info_pane_x + 20, info_pane_y + 10 + j * 15, 10, 10);
                        g.setColor(Color.black);
                        g.drawString(annoY.getAnnotationTrack(i).getAnnotationName(), info_pane_x + 40, info_pane_y + 15 + j * 15);
                        j++;
                    }

                }
            }
            /* End of drawing the legends ***********************************/

        }
    }

    private void drawAnnoX(Graphics2D g2d) {
        AnnotationViewWidget avwx = new AnnotationViewWidget(g2d, annoX, agoX, xOff + ruler, yOff + 2 * ruler + DotM.getPixY(), DotM.getPixX(), annospace, 0, DotM.getStartX(), DotM.getEndX());
    }

    private void drawAnnoY(Graphics2D g2d) {
        AnnotationViewWidget avwx = new AnnotationViewWidget(g2d, annoY, agoY, xOff + 2 * ruler + DotM.getPixX(), yOff + ruler, annospace, DotM.getPixY(), 1, DotM.getStartY(), DotM.getEndY());
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
        Graphics gg;
        int i, j, k, x, y;
        String line;

        x = e.getX();
        y = e.getY();
        gg = this.getGraphics();

        if (x > (xOff + ruler) && x < dm_right) {
            /* The mouse is in X zone */
            if (y > (yOff + ruler) && y < dm_bottom) {
                /* The mouse is in dotmatrix */
                gg.setColor(Color.white);
                //                gg.fillRect(info_pane_x + 10, info_pane_y + 10, 180, 20);
                gg.fillRect(10, 5, 500, 15);

                //        this.getGraphics().clearRect(info_pane_x + 10, info_pane_y + 10, 180, 20);

                gg.setColor(Color.black);
                gg.drawString("\nX: " + (int) ((x - xOff - ruler) * scaleX) + DotM.getStartX() + //                                     annoX.getSeqSegmentStart()) +
                        " [ " + (int) ((x - xOff - ruler) * scaleX) + " ]  Y: " + (int) ((y - yOff - ruler) * scaleY) + DotM.getStartY() + //                                     annoY.getSeqSegmentStart())
                        " [ " + (int) ((y - yOff - ruler) * scaleY) + " ]", //                              info_pane_x + 15, info_pane_y + 25);
                        10, 15);
            } else {
                gg.setColor(Color.white);
                //                gg.fillRect(info_pane_x + 10, info_pane_y + 10, 400, 20);
                gg.fillRect(10, 5, 500, 15);
                //        this.getGraphics().clearRect(info_pane_x + 10, info_pane_y + 10, 400, 20);

                if (y > dm_bottom + ruler) {
                    if ((i = agoX.getHit(x, y)) != -1) {
                        if ((annoX.getAnnotationTrack(agoX.getTrackNum(i))).getFeatureTag(agoX.getNumInTrack(i)) != null) {
                            gg.setColor(Color.white);
                            gg.fillRect(10, 5, 500, 15);
                            gg.setColor(Color.black);
                            gg.drawString((annoX.getAnnotationTrack(agoX.getTrackNum(i))).getFeatureTag(agoX.getNumInTrack(i)) + " { " + (annoX.getAnnotationTrack(agoX.getTrackNum(i))).getAnnotationName() + " } ", 10, 15);

                        }
                    }
                } else {
                    gg.setColor(Color.white);
                    gg.fillRect(10, 5, 500, 15);
                }
            }
        } else {
            gg.setColor(Color.white);
            gg.fillRect(10, 5, 500, 15);
            if (x > dm_right + ruler && y < dm_bottom) {
                if ((i = agoY.getHit(x, y)) != -1) {
                    if ((annoY.getAnnotationTrack(agoY.getTrackNum(i))).getFeatureTag(agoY.getNumInTrack(i)) != null) {
                        gg.setColor(Color.white);
                        gg.fillRect(10, 5, 500, 15);
                        gg.setColor(Color.black);
                        gg.drawString((annoY.getAnnotationTrack(agoY.getTrackNum(i))).getFeatureTag(agoY.getNumInTrack(i)) + " { " + (annoY.getAnnotationTrack(agoY.getTrackNum(i))).getAnnotationName() + " } ", 10, 15);
                    }
                }
            } else {
                gg.setColor(Color.white);
                gg.fillRect(10, 5, 500, 15);
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
    }

    public void mouseDragged(MouseEvent e) {
    }
}
