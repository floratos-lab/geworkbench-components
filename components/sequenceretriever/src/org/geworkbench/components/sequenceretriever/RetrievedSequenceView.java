package org.geworkbench.components.sequenceretriever;

import javax.swing.*;

import org.geworkbench.util.sequences.GeneChromosomeMatcher;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;

/**
 * Created by IntelliJ IDEA.
 * User: xiaoqing
 * Date: Sep 6, 2006
 * Time: 5:07:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class RetrievedSequenceView extends JPanel {
    int xOff = 4;
    double scale = 1;
    static int maxSeqLen = 20000;
    private CSSequence sequence;
    private GeneChromosomeMatcher geneChromosomeMatcher;


    private String url;
    public static final int HEIGHT = 50;
    private static final Color SEQUENCEBACKGROUDCOLOR = Color.BLUE;
    private static final Color SEQUENCEDOWNSTREAMCOLOR = Color.RED;
    private final static String baseUNIPROURLStr = "http://www.ebi.ac.uk/cgi-bin/dbfetch?db=uniprot&id=";


    public static int getMaxSeqLen() {
        return maxSeqLen;
    }

    public static void setMaxSeqLen(int maxSeqLen) {
        RetrievedSequenceView.maxSeqLen = maxSeqLen;
    }

    public void setGeneChromosomeMatcher(GeneChromosomeMatcher geneChromosomeMatcher) {
        this.geneChromosomeMatcher = geneChromosomeMatcher;
    }

    public GeneChromosomeMatcher getGeneChromosomeMatcher() {
        return geneChromosomeMatcher;
    }

    public void setUrl(String url) {
        this.url = baseUNIPROURLStr + url.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setSequence(CSSequence sequence) {
        this.sequence = sequence;
    }

    public void setIncluded(boolean included) {
        isIncluded = included;
    }

    public boolean isIncluded() {
        return isIncluded;
    }

    private boolean isIncluded = false;

    public RetrievedSequenceView() {
        super(true);
        repaint();

    }

    public RetrievedSequenceView(CSSequence theSeq) {
        super(true);
        sequence = theSeq;

    }

    private void jbInit() throws Exception {

//        this.setLayout(new BorderLayout());
//        upperPanel = new JPanel();
//        lowerPanel = new JPanel();
//        jCheckBox1 = new JCheckBox();
//        jCheckBox1.setText("TEST2");
//        jLabel1 = new JLabel("<HTML><font color=red>TEST</font></html>");
//        //upperPanel.add(jLabel1);
//        upperPanel.add(jCheckBox1);
//        this.add(upperPanel, BorderLayout.WEST);
//        this.add(lowerPanel, BorderLayout.CENTER);
//        this.setPreferredSize(new Dimension(500, 50));
//        System.out.println("here");
    }

//    class SequenceDetailPanel extends JPanel {
//
//        public SequenceDetailPanel() {
//            try {
//                jbInit();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//       public void paint(Graphics g1d) {

    public void paintComponent(Graphics g1d) {
        super.paintComponent(g1d);
        scale = Math.min(5.0,
                (double) (this.getWidth() - 20 - xOff) /
                        (double) maxSeqLen);

        Graphics2D g = (Graphics2D) g1d;
        int width = getWidth() - 4;
        if (sequence != null) {
            int x = xOff + (int) (sequence.length() * scale / 2);
            g.setColor(SEQUENCEBACKGROUDCOLOR);
            int y = 30;
            if (sequence.isDNA()) {

                g.drawLine(xOff, y, x, y);
                g.drawLine(xOff, y - 10, xOff, y);
                int shape = 3;
                int[] xi = new int[shape];
                int[] yi = new int[shape];
                int height = 12;
                if (geneChromosomeMatcher.isPositiveStrandDirection()) {
                    xi[0] = xi[1] = xOff + 20;
                    yi[0] = (int) y - height / 2 - 2;
                    yi[1] = (int) y - height / 2 + 6;
                    xi[2] = xi[0] + 4;
                    yi[2] = (int) y - height / 2 + 2;
                    // g.drawPolyline(xi, yi, addtionalPoint);
                } else {
                    xi[0] = xi[1] = xOff + 20;
                    yi[0] = (int) y - height / 2 - 2;
                    yi[1] = (int) y - height / 2 + 6;
                    xi[2] = xi[0] - 4;
                    yi[2] = (int) y - height / 2 + 2;

                }

                g.drawPolygon(xi, yi, shape);
                g.fillPolygon(xi, yi, shape);

                g.setColor(SEQUENCEDOWNSTREAMCOLOR);

                int x1 = xOff + (int) (sequence.length() * scale);
                g.drawLine(x, y, x1, y);
                g.drawLine(x, y - 10, x, y);
                g.drawLine(x1, y - 10, x1, y);

            } else {
                x = xOff + (int) (sequence.length() * scale);
                g.drawLine(xOff, y, x, y);
            }

        }
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int componentWidth = width;
        //float charWidth = ((float) width) / maxLength;
        FontRenderContext fontRenderContext = g.getFontRenderContext();
        int centerLine = HEIGHT / 2;
        g.setColor(Color.BLACK);
        //g.drawString("LABLE", 4, 4);

    }
    public String getToolTipText(MouseEvent event) {
        float x = event.getX() - xOff;

        int index = (int) (x / scale);
        if ((index >= 0) && (index < sequence.length())) {

             String highlight = null;
            highlight = sequence.getSequence().substring(index, index+10);

            return "" + index + ": " + highlight;
        }
        return null;
    }
}
