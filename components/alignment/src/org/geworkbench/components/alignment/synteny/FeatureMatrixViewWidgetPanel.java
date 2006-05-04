package org.geworkbench.components.alignment.synteny;

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
 * @author not attributable
 * @version 1.0
 */

public class FeatureMatrixViewWidgetPanel
    extends JPanel implements MouseListener, MouseMotionListener {

    final int dm_dim = 400;
    final int xOff = 15;
    final int yOff = 50;
    final int ruler = 10;
    final int annospace = 500;
    static double scaleX;
    static double scaleY;
    int info_pane_x;
    int info_pane_y;
    int dm_right;
    int dm_bottom;
    int ftsize;
    int ftm_size;
    FeatureObjectDisplayPanel[] fodp;
    double nf;

    private JScrollPane scPane = new JScrollPane();

    static FeatureMatrixObj FetM;
    static FeatureMatrixObjList FetML;

    public FeatureMatrixViewWidgetPanel() {

        FetML = null;
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public FeatureMatrixViewWidgetPanel(FeatureMatrixObjList dmol) {
        FetML = dmol;

        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setBackground(Color.green);
    }

    public void paint(Graphics g) {

        int i, j, k;

        if (FetML == null) {
            return;
        }

// Now we have to put the linear list into 2D representation
        nf = Math.sqrt(FetML.getNum());
        nf = Math.ceil(nf);

        ftm_size = (int) (dm_dim / nf);

        dm_right = ftm_size * FetML.getNum() + xOff + ruler;
        dm_bottom = ftm_size * FetML.getNum() + yOff + ruler;

        info_pane_x = ruler + dm_right;
        info_pane_y = ruler + dm_bottom;

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        Font f = new Font("Courier New", Font.BOLD, 12);

        // draw the feature matrix
        g.setFont(f);

        // draw rectangles
        g.setColor(Color.white);
        g.fillRect(0, 0, dm_dim + xOff + 2 * ruler + annospace,
                   dm_dim + yOff + 2 * ruler + annospace);

        // draw real matrixes
        fodp = new FeatureObjectDisplayPanel[FetML.getNum()];
        for (i = 0; i < FetML.getNum(); i++) {
            k = (int) (i / nf);
            j = (int) (i % nf);
            fodp[i] = new FeatureObjectDisplayPanel(g, FetML.getFMObj(i),
                xOff + ruler + ftm_size * j, yOff + ruler + ftm_size * k,
                ftm_size, ftm_size);
        }

        // The title
        g.setColor(Color.black);
        g.drawString(FetML.getFeatureName(), xOff + ruler, 9);

        AnnotationColors.initSmoothColdMap();

        /* Drawing the color bar */
        for (i = 0; i < 256; i++) {
            g.setColor(AnnotationColors.SmoothColdMap[i]);
            g.fillRect(xOff + 4 * ruler + dm_dim, yOff + ruler + i + 5, 20, 1);
        }

        g.setColor(Color.black);
        g.drawString(" " + FetML.getMin(), xOff + 4 * ruler + dm_dim + 22,
                     yOff + ruler + 8);
        g.drawString(" " + FetML.getMax(), xOff + 4 * ruler + dm_dim + 22,
                     yOff + ruler + 7 + 256);
    }

// Mouse stuff
    public void mouseMoved(MouseEvent e) {
        Graphics gg;
        int i, j, k, l, m, x, y;

        x = e.getX();
        y = e.getY();
        gg = this.getGraphics();
        if (x > xOff + ruler && y > yOff + ruler) {
            if (y < yOff + 2 * ruler + dm_dim && x < xOff + 2 * ruler + dm_dim) {
                // it's inside the resion of interest
                for (i = 0; i < FetML.getNum(); i++) {
                    k = (int) (i / nf);
                    j = (int) (i % nf);
                    if(x > xOff + ruler + ftm_size * j && x < xOff + ruler + ftm_size * (j+1)){
                        if(y > yOff + ruler + ftm_size * k && y < yOff + ruler + ftm_size * (k+1)){
                            gg.setColor(Color.white);
                            gg.fillRect(xOff + ruler, 10, 500, 20);
                            gg.setColor(Color.black);
                            gg.drawString("X: "+FetML.getXName(i), xOff + ruler, 20);
                            gg.drawString("Y: "+FetML.getYName(i), xOff + ruler, 30);
                            // Now we have to look into the object to find out the feature number
                            int nfo=(FetML.getFMObj(i)).getXnum();
                            for(l=0;l<nfo;l++)if(x-xOff-ruler-ftm_size * j < l*(ftm_size/nfo))break;
                            nfo=(FetML.getFMObj(i)).getYnum();
                            for(m=0;m<nfo;m++)if(y-yOff-ruler-ftm_size * k < m*(ftm_size/nfo))break;

                            gg.setColor(Color.white);
                            gg.fillRect(xOff + ruler, 30, 500, 20);
                            gg.setColor(Color.black);
                            gg.drawString("XX: "+(FetML.getFMObj(i)).getXname(l-1), xOff + ruler, 40);
                            gg.drawString("YY: "+(FetML.getFMObj(i)).getXname(m-1), xOff + ruler, 50);

                        }

                    }
                }
            }
        }
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseClicked(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {}

    public void mouseDragged(MouseEvent e) {}
}
