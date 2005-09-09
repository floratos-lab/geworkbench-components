package org.geworkbench.components.alignment.synteny;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.image.codec.jpeg.JPEGCodec;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SyntenyMapViewWidgetPanel
    extends JPanel implements MouseListener, MouseMotionListener {

    SyntenyMapObject smObj = null;
    BorderLayout borderLayout2 = new BorderLayout();
    public boolean scaledPicture = true;
    public boolean showLegends = true;

    public SyntenyMapViewWidgetPanel(SyntenyMapObject smo) {
        smObj = smo;
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
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setMaximumSize(new Dimension(2647, 2647));
        this.setMinimumSize(new Dimension(50, 50));
        this.setPreferredSize(new Dimension(300, 300));

    }

    public void drawStringAngle(Graphics2D g2, String st, int x, int y, int angle){
        if (showLegends) {
            g2.translate(x, y);
            g2.rotate( angle * Math.PI / 180.0);
            g2.drawString(st, 0, 0);
            g2.rotate( -1*angle * Math.PI / 180.0);
            g2.translate( -x, -y);
        }
    }

    public void paint(Graphics g) {

        int x1, x2, y1, y2;
        int i, j, k, cell_width, cell_hight;
        int fn;
        int[][] coords;
        int[] fs;
        int mashtab;
        int adds = 50;

        if (smObj == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;

        g.setColor(Color.black);
        g.clearRect(0, 0, getWidth(), getHeight());

        fn = smObj.getFragmentsNum();

        // determine number of fragments and their comparative size

        coords = new int[fn][2];
        fs = new int[fn];

        k = (int) Math.sqrt(fn);
        if (k * k < fn) {
            k++;
        }

        for (i = 0; i < k && i * k < fn; i++) {
            for (j = 0; j < k && i * k + j < fn; j++) {
                coords[i * k + j][0] = j;
                coords[i * k + j][1] = i;
            }
        }
        cell_width = 800 / k;
        cell_hight = 600 / k;

        for (i = 0; i < fn; i++) {
            fs[i] = smObj.getFragment(i).getSpan();
        }
        // fill plane with fragments
        // draw fragments

        for (i = 0; i < fn; i++) {
            mashtab = fs[i] / (cell_width - 2 * adds);

            // Draw titles
            g.drawString(smObj.getFragment(i).getUpperGenome() + " : " +
                         smObj.getFragment(i).getUpperChromosome() + " : " +
                         smObj.getFragment(i).getUpperCromosomeStart() + ":" +
                         smObj.getFragment(i).getUpperCromosomeEnd(),
                         coords[i][0] * cell_width + adds,
                         coords[i][1] * cell_hight + 20);
            // Draw titles
            g.drawString(smObj.getFragment(i).getLowerGenome() + " : " +
                         smObj.getFragment(i).getLowerChromosome() + " : " +
                         smObj.getFragment(i).getLowerCromosomeStart() + ":" +
                         smObj.getFragment(i).getLowerCromosomeEnd(),
                         coords[i][0] * cell_width + adds,
                         coords[i][1] * cell_hight + 32);

            if(scaledPicture){ // Draw picture in the scale of genomic position

                // draw upper line
                x1 = coords[i][0] * cell_width + adds;
                x2 = x1 + smObj.getFragment(i).getUpperSpan() / mashtab;
                y1 = y2 = coords[i][1] * cell_hight + 2 * adds;
                g.setColor(Color.black);
                g.drawLine(x1, y1, x2, y2);

                // draw lower line
                x2 = x1 + smObj.getFragment(i).getLowerSpan() / mashtab;
                y1 = y2 = coords[i][1] * cell_hight + cell_hight - 2 * adds;
                g.setColor(Color.black);
                g.drawLine(x1, y1, x2, y2);

                // draw features for Upper
                int lfn = smObj.getFragment(i).getUpperObjectsNum();
                for (j = 0; j < lfn; j++) {
                    int os = smObj.getFragment(i).getUpperObjectStart(j);
                    int oe = smObj.getFragment(i).getUpperObjectEnds(j);

                    k = (oe - os + 1) / mashtab;
                    os = os / mashtab;
                    x1 = coords[i][0] * cell_width + adds + os;
                    x2 = x1 + k;
                    y1 = y2 = coords[i][1] * cell_width + 2 * adds + 1;
                    g.setColor(Color.blue);
                    g.drawRect(x1, y1 - 2, x2 - x1, y2 + 2 - y1);

                    if(showLegends){
                        g2.setColor(Color.black);
                        drawStringAngle(g2, smObj.getFragment(i).getSingleUpperName(j), x1 + k/2, y1-4, -30);
                    }
                }

                // draw features for Lower
                int ufn = smObj.getFragment(i).getLowerObjectsNum();
                for (j = 0; j < ufn; j++) {
                    int os = smObj.getFragment(i).getLowerObjectStart(j);
                    int oe = smObj.getFragment(i).getLowerObjectEnds(j);

                    k = (oe - os + 1) / mashtab;
                    os = os / mashtab;
                    x1 = coords[i][0] * cell_width + adds + os;
                    x2 = x1 + k;
                    y1 = y2 = coords[i][1] * cell_hight + cell_hight - 2 * adds;
                    g.setColor(Color.blue);
                    g.drawRect(x1, y1 - 1, x2 - x1, y2 + 2 - y1);

                    if(showLegends){
                        g2.setColor(Color.black);
                        drawStringAngle(g2, smObj.getFragment(i).getSingleLowerName(j), x1 + k/2, y1+11, 30);
                    }
                }

                // draw connection lines
                int pfn = smObj.getFragment(i).getPairNum();
                for (j = 0; j < pfn; j++) {
                    int wg = smObj.getFragment(i).getPairWeight(j);
                    int lp = smObj.getFragment(i).getLowerPair(j);
                    int up = smObj.getFragment(i).getUpperPair(j);

                    int os = smObj.getFragment(i).getLowerObjectStart(up);
                    int oe = smObj.getFragment(i).getLowerObjectEnds(up);
                    k = (oe - os + 1) / mashtab;
                    os = os / mashtab;
                    oe = k / 2;
                    x1 = coords[i][0] * cell_width + adds + os + oe;
                    y1 = coords[i][1] * cell_width + 2 * adds + 2;

                    os = smObj.getFragment(i).getLowerObjectStart(lp);
                    oe = smObj.getFragment(i).getLowerObjectEnds(lp);
                    k = (oe - os + 1) / mashtab;
                    os = os / mashtab;
                    oe = k / 2;
                    x2 = coords[i][0] * cell_width + adds + os + oe;
                    y2 = coords[i][1] * cell_hight + cell_hight - 2 * adds - 2;

                    if (wg == 2) {
                        g.setColor(Color.red);
                    }
                    else {
                        g.setColor(Color.darkGray);
                    }

                    g.drawLine(x1, y1, x2, y2);
                }
            }
            else {// Draw all features equeal

                // Determin maximum features nmber and draw features
                int lo = smObj.getFragment(i).getLowerObjectsNum();
                int uo = smObj.getFragment(i).getUpperObjectsNum();
                int on = Math.max(lo,uo);
                on = (cell_width - 2*adds)/on;


                for(j=0;j<uo;j++){
                    g.setColor(Color.blue);
                    x1 = coords[i][0] * cell_width+j*on+1 + adds;
                    x2 = on - 3;
                    y1 = coords[i][1] * cell_hight + 2 * adds-2;
                    y2 = 4;
                    g.fillRect(x1,y1,x2,y2);
                    if(showLegends){
                        g.setColor(Color.black);
                        drawStringAngle(g2, smObj.getFragment(i).getSingleLowerName(j), x1+ on/2, y1-5, -30);
                    }
                }

                for(j=0;j<lo;j++){
                    g.setColor(Color.blue);
                    x1 = coords[i][0] * cell_width + j*on+1 + adds;
                    x2 = on - 3;
                    y1 = coords[i][1] * cell_hight + cell_hight - 2 * adds-2;
                    y2 = 4;
                    g.fillRect(x1,y1,x2,y2);
                    if(showLegends){
                        g.setColor(Color.black);
                        drawStringAngle(g2, smObj.getFragment(i).getSingleLowerName(j), x1+ on/2, y1+11, 30);
                    }
                }

                // draw connection lines
                int pfn = smObj.getFragment(i).getPairNum();
                for (j = 0; j < pfn; j++) {
                    int wg = smObj.getFragment(i).getPairWeight(j);
                    int lp = smObj.getFragment(i).getLowerPair(j);
                    int up = smObj.getFragment(i).getUpperPair(j);

                    x1 = coords[i][0] * cell_width + adds + up * on + on /2;
                    y1 = coords[i][1] * cell_width + 2 * adds + 3;

                    x2 = coords[i][0] * cell_width + adds + lp * on + on /2;
                    y2 = coords[i][1] * cell_hight + cell_hight - 2 * adds - 3;

                    if (wg == 2) {
                        g.setColor(Color.red);
                    }
                    else {
                        g.setColor(Color.darkGray);
                    }

                    g.drawLine(x1, y1, x2, y2);
                }
            }
        }
    }

    public void setNewData(SyntenyMapObject smo){
        smObj = smo;
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

// Mouse stuff
    public void mouseMoved(MouseEvent e) {
        int i, x, y;

        x = e.getX();
        y = e.getY();
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseClicked(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        Graphics gg;
        int i, j, k, x, y;

        x = e.getX();
        y = e.getY();
        gg = this.getGraphics();

    }

    public void mouseDragged(MouseEvent e) {}

    /**
     * isInitiated
     *
     * @return boolean
     */
}
