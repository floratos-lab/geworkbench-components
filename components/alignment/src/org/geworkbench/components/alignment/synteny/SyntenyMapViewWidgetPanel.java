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
    SyntenyMapViewWidget smvw = null;
    BorderLayout borderLayout2 = new BorderLayout();
    public boolean scaledPicture = true;
    public boolean showLegends = false;
    public int[] coords;
    public int small_left, small_right, big_left, big_right;
    public int fn;
    public int cell_hight;

    public SyntenyMapViewWidgetPanel() {
        smObj = null;
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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
        this.setPreferredSize(new Dimension(500, 800));
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

    public void setWidgetAddress(SyntenyMapViewWidget sw){
        smvw = sw;
    }

    public void paint(Graphics g) {

        int i, j=0, big_l, big_h, small_l;

        if (smObj == null) {
            return;
        }
        g.setColor(Color.black);
        g.clearRect(0, 0, getWidth(), getHeight());
        small_l = (int)(this.getVisibleRect().getWidth())/6;
        big_l = small_l * 5;

        big_h = (int)(this.getVisibleRect().getHeight())-20;
        if(big_h<200)big_h = 200;
        if(big_h>350)big_h = 350;


//        smvw.populateDistanceBox(smObj);

        // determine number of fragments and their comparative size
        fn = smObj.getFragmentsNum();
        coords = new int[fn];
        if(fn>1)
            cell_hight = 750 / fn;
        else
            cell_hight = 90;
        if(cell_hight > 90 )cell_hight = 90;

        int heap = 20 - cell_hight;
        for (i = 0; i < fn; i++) {
                coords[i] = heap + cell_hight;
                heap = coords[i];
        }

        small_left = big_l+1;
        small_right = small_left+small_l-2;
        big_left = 5;
        big_right = big_l-6;

        for (i = 0; i < fn; i++) {
            if(i == smObj.getActiveFragmentNum()){
                draw_SynMapFragment(smObj.getFragment(i), small_left, coords[i],
                                    small_right, coords[i] + cell_hight, g, false,true);
            } else {
                draw_SynMapFragment(smObj.getFragment(i), small_left, coords[i],
                                    small_right, coords[i] + cell_hight, g, false,false);
            }
        }
        /* Draw this big */
        draw_SynMapFragment(smObj.getFragment(smObj.getActiveFragmentNum()), big_left, 5,
                            big_right, big_h, g, true, false);
    }

    public void draw_SynMapFragment(SyntenyMapFragment smf, int xl, int yt, int xr, int ylw , Graphics g, boolean draw_label, boolean marked){
        int i, j, x1, x2, yu, yl;
        int mashtab = smf.getSpan() / (xr-xl+1);

        Graphics2D g2 = (Graphics2D) g;
        if(marked){
            g.setColor(Color.red);
        } else {
            g.setColor(Color.black);
        }
        // Draw titles
        g.drawString(smf.getUpperGenome() + " : " + smf.getUpperChromosome() + " : " +
                     smf.getUpperCromosomeStart() + ":" + smf.getUpperCromosomeEnd(),
                     xl+1, yt + 28);
        // Draw titles
        g.drawString(smf.getLowerGenome() + " : " + smf.getLowerChromosome() + " : " +
                     smf.getLowerCromosomeStart() + ":" + smf.getLowerCromosomeEnd(),
                     xl+1, yt + 38);

        if(showLegends && draw_label){
            yu = yt + 85;
            yl = ylw - 45;
            if (yu + 10 > yl) {
                yl = yu + 10;
            }
        }else{
            yu = yt + 45;
            yl = ylw - 5;
            if (yu + 10 > yl) {
                yl = yu + 10;
            }
        }


        if(scaledPicture){ // Draw picture in the scale of genomic position
            // draw connection lines
            int pfn = smf.getPairNum();
            int lines_num = 0;
            for (i = 0; i < pfn; i++) {
                double wg = smf.getPairWeight(i);
                if(wg == 0)continue;
                lines_num++;
                int up = smf.getUpperPair(i);
                int lp = smf.getLowerPair(i);
                int os = smf.getUpperObjectStart(up)-smf.getUpperCromosomeStart()+1;
                int oe = smf.getUpperObjectEnd(up)-smf.getUpperCromosomeStart()+1;

                j = (oe - os + 1) / mashtab;
                os = os / mashtab;
                oe = j / 2;
                x1 = xl + os + oe;

                os = smf.getLowerObjectStart(lp)-smf.getLowerCromosomeStart()+1;
                oe = smf.getLowerObjectEnd(lp)-smf.getLowerCromosomeStart()+1;

                j = (oe - os + 1) / mashtab;
                os = os / mashtab;
                oe = j / 2;
                x2 = xl + os + oe;

            if (wg == 1.) {
                g.setColor(Color.red);
            }
            else {
                if (wg != 0.)
                g.setColor(Color.darkGray);
            }
            g.drawLine(x1, yu, x2, yl);
        }
        if(lines_num == 0){
            g2.drawString("No synteny features found.", xl+5, yu+15);
        }


        // draw upper line
        x2 = xl + smf.getUpperSpan() / mashtab;
        g.setColor(Color.black);
        g.drawLine(xl, yu, x2, yu);

        // draw features for Upper
        for (i = 0; i < smf.getUpperObjectsNum(); i++) {
            int os = smf.getUpperObjectStart(i)-smf.getUpperCromosomeStart()+1;
            int oe = smf.getUpperObjectEnd(i)-smf.getUpperCromosomeStart()+1;

            j = (oe - os + 1) / mashtab;
            os = os / mashtab;
            g.setColor(Color.blue);
            g.fillRect(xl + os , yu - 2/* - j%2*/, j /* x2 - x1 */, 4);
            g.setColor(Color.gray);
            g.drawRect(xl + os , yu - 2/* - j%2*/, j /* x2 - x1 */, 4);

            if(showLegends && draw_label){
                g2.setColor(Color.black);
                drawStringAngle(g2, smf.getSingleUpperName(i), xl + os + j/2, yu-4, -20);
            }
        }

        // draw lower line
        x2 = xl + smf.getLowerSpan() / mashtab;
        g.setColor(Color.black);
        g.drawLine(xl, yl, x2, yl);

        // draw features for Lower
        for (i = 0; i < smf.getLowerObjectsNum(); i++) {
            int os = smf.getLowerObjectStart(i)-smf.getLowerCromosomeStart()+1;
            int oe = smf.getLowerObjectEnd(i)-smf.getLowerCromosomeStart()+1;

            j = (oe - os + 1) / mashtab;
            os = os / mashtab;

            g.setColor(Color.blue);
            g.fillRect(xl + os , yl - 2 /* - j%2*/, j /* x2 - x1 */, 4);
            g.setColor(Color.gray);
            g.drawRect(xl + os , yl - 2 /*- j%2*/, j /* x2 - x1 */, 4);

            if(showLegends && draw_label){
                g2.setColor(Color.black);
                drawStringAngle(g2, smf.getSingleLowerName(i), xl + os + j/2, yl+11, 20);
            }
        }

    }
    else {// Draw all features equal

        // Determin maximum features nmber and draw features
        int lo = smf.getLowerObjectsNum();
        int uo = smf.getUpperObjectsNum();
        int on = Math.max(lo,uo);
        on = (xr-xl)/on;

        /* draw upper objects */
        x2 = on - 3;
        for(j=0;j<uo;j++){
            g.setColor(Color.blue);
            x1 = xl + j*on + 1;
            g.fillRect(x1,yu,x2,4);
            if(showLegends && draw_label){
                g.setColor(Color.black);
                drawStringAngle(g2, smf.getSingleUpperName(j), x1+ on/2, yu-5, -80);
           }
        }

        for(j=0;j<lo;j++){
            g.setColor(Color.blue);
            x1 = xl + j*on + 1;
            g.fillRect(x1,yl,x2,4);
            if(showLegends && draw_label){
                g.setColor(Color.black);
                drawStringAngle(g2, smf.getSingleLowerName(j), x1+ on/2, yl+11, 80);
            }
        }

        // draw connection lines
        int lines_num = 0;
        for (j = 0; j < smf.getPairNum(); j++) {
            double wg = smf.getPairWeight(j);
            if(wg == 0)continue;
            lines_num++;
            int lp = smf.getLowerPair(j);
            int up = smf.getUpperPair(j);

            x1 = xl + up * on + on /2;
            x2 = xl + lp * on + on /2;

            if (wg == 2) {
                g.setColor(Color.red);
            }
            else {
                g.setColor(Color.darkGray);
            }
            g.drawLine(x1, yu, x2, yl);
        }
        if(lines_num == 0){
            g2.drawString("No synteny features found.", xl+5, yu+15);
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
        int returnVal = fc.showSaveDialog(this.getParent());
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

    public void mouseClicked(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        Graphics gg;
        int i, j, k, x, y;

        x = e.getX();
        y = e.getY();
        gg = this.getGraphics();

        if(x>small_left && x<small_right){
            for (i = 0; i < fn; i++) {
                if (i != smObj.getActiveFragmentNum()) {
                    if(y>coords[i] && y<coords[i] + cell_hight){
                        smObj.setActiveFragmentNum(i);
                        smvw.populateDistanceBox(smObj);
                        repaint();
                    }
                }
            }
        }
        if(x>big_left && x<big_right){

        }
    }

    public void mouseDragged(MouseEvent e) {}

    /**
     * isInitiated
     *
     * @return boolean
     */
}
