package org.geworkbench.components.promoter;

import org.geworkbench.bison.datastructure.biocollections.DSCollection;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.DSPattern;
import org.geworkbench.bison.datastructure.complex.pattern.DSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
import org.geworkbench.util.patterns.CSMatchedSeqPattern;
import org.geworkbench.util.patterns.PatternOfflet;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author
 * @version 1.0
 */

public class SequenceDisplayPanel extends JPanel {
    final int xOff = 60;
    final int yOff = 20;
    final int xStep = 5;
    final int yStep = 14;
    boolean isText = false;
    boolean isDiscovery = false;
    double scale = 1.0;
    int maxLen = 1;
    ArrayList<DSPattern<DSSequence, CSSeqRegistration>> selectedPatterns = new ArrayList<DSPattern<DSSequence, CSSeqRegistration>>();
    org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern[] patterns = null;
    DSSequence selected = null;
    DSSequenceSet sequenceDB = new CSSequenceSet();
    JPanel jinfoPanel = null;

    public void setIsDiscovery(boolean isDiscovery) {
        this.isDiscovery = isDiscovery;
    }

    public void setInfoPanel(JPanel jinfoPanel) {
        this.jinfoPanel = jinfoPanel;
    }

    public JPanel getInfoPanel() {
        return jinfoPanel;
    }

    public SequenceDisplayPanel() {
        try {
            jbInit();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {

        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                this_mouseClicked(e);
            }

        });
        this.addMouseMotionListener(new MouseInputAdapter() {

            public void mouseMoved(MouseEvent e) {
                this_mouseMoved(e);
            }

        });

    }

    public void initialize(ArrayList ar, DSSequenceSet seqDB) {
        selectedPatterns = ar;
        sequenceDB = seqDB;
        repaint();
    }

    public void addAPattern(DSPattern<DSSequence, CSSeqRegistration> pattern) {
        Graphics g = this.getGraphics();
        JViewport scroller = (JViewport) this.getParent();
        Rectangle r = new Rectangle();
        r = scroller.getViewRect();

        for (int seqId = 0; seqId < sequenceDB.getSequenceNo(); seqId++) { // can be used to match to whole DB
            Annotable as = (Annotable) sequenceDB.getSequence(seqId);
            List<DSPatternMatch<DSSequence, CSSeqRegistration>> matches = (List<DSPatternMatch<DSSequence, CSSeqRegistration>>) as.get(pattern);
            if (matches == null) { //if it dosen't have current match
                matches = pattern.match(sequenceDB.getSequence(seqId), 1.0);
                as.set(pattern, matches);
            }
            drawPattern(g, seqId, matches, r, org.geworkbench.util.patterns.PatternOperations.getPatternColor(6));
        }

    }

    public void flipIsText() {

        isText = !isText;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isText) {
            paintText(g);
        } else {
            paintGraphic(g);
        }
    }

    private void paintGraphic(Graphics g) {
        Font f = new Font("Courier New", Font.PLAIN, 10);
        if (sequenceDB != null) {
            int rowId = -1;
            int maxLn = sequenceDB.getMaxLength();
            int seqNo = sequenceDB.getSequenceNo();

            scale = Math.min(5.0, (double) (this.getWidth() - 20 - xOff) / (double) maxLn);
            g.clearRect(0, 0, getWidth(), getHeight());
            // draw the patterns
            g.setFont(f);
            JViewport scroller = (JViewport) this.getParent();
            Rectangle r = new Rectangle();
            r = scroller.getViewRect();

            for (int seqId = 0; seqId < seqNo; seqId++) {
                rowId++;
                drawSequence(g, seqId, seqId, maxLn);
            }
            //patterns from string discovery
            if (patterns != null) {

                for (int x = 0; x < patterns.length; x++) {
                    if (isDiscovery) {
                        for (int locusId = 0; locusId < patterns[x].getSupport(); locusId++) {
                            int seqId = ((org.geworkbench.util.patterns.CSMatchedSeqPattern) patterns[x]).getId(locusId);
                            double off = (double) ((org.geworkbench.util.patterns.CSMatchedSeqPattern) patterns[x]).getAbsoluteOffset(locusId);
                            drawPattern(g, seqId, off, (org.geworkbench.util.patterns.CSMatchedSeqPattern) patterns[x], r, org.geworkbench.util.patterns.PatternOperations.getPatternColor(patterns[x].hashCode()));
                        }
                    } else {
                        String asc = ((org.geworkbench.util.patterns.CSMatchedSeqPattern) patterns[x]).getASCII();
                        for (int k = 0; k < sequenceDB.getSequenceNo(); k++) {
                            DSSequence seq = sequenceDB.getSequence(k);
                            String s = seq.getSequence();

                            java.util.regex.Pattern pt = java.util.regex.Pattern.compile(asc);
                            Matcher mt = pt.matcher(s);
                            while (mt.find()) {
                                int locus = mt.start();
                                drawPattern(g, k, (double) locus, (org.geworkbench.util.patterns.CSMatchedSeqPattern) patterns[x], r, org.geworkbench.util.patterns.PatternOperations.getPatternColor(patterns[x].hashCode()));

                            }
                        }

                    }
                }

            }
            // patterns from promoter matching
            if (selectedPatterns != null) {
                for (int row = 0; row < selectedPatterns.size(); row++) {
                    DSPattern<DSSequence, CSSeqRegistration> pattern = selectedPatterns.get(row);

                    if (pattern != null) {
                        for (int seqId = 0; seqId < seqNo; seqId++) { // can be used to match to whole DB
                            Annotable as = (Annotable) sequenceDB.getSequence(seqId);
                            List<DSPatternMatch<DSSequence, CSSeqRegistration>> matches = (List<DSPatternMatch<DSSequence, CSSeqRegistration>>) as.get(pattern);
                            if (matches != null) {
                                drawPattern(g, seqId, matches, r, org.geworkbench.util.patterns.PatternOperations.getPatternColor(pattern.hashCode()));
                            }
                        }
                    }
                }
            }

            int maxY = (rowId + 1) * yStep + yOff;
            setPreferredSize(new Dimension(this.getWidth() - yOff, maxY));
            revalidate();
        }
    }

    private void paintText(Graphics g) throws ArrayIndexOutOfBoundsException {
        if (selected == null) {
            selected = sequenceDB.getSequence(0);
        }
        if (selected != null) {
            Font f = new Font("Courier New", Font.PLAIN, 11);
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            FontMetrics fm = g.getFontMetrics(f);
            String asc = selected.getSequence();
            Rectangle2D r2d = fm.getStringBounds(asc, g);
            double xscale = (r2d.getWidth() + 3) / (double) (asc.length());
            double yscale = 1.4 * xscale;
            int width = this.getWidth();
            int cols = (int) (width / xscale) - 12;
            int rowId = -1;
            g.setFont(f);
            //      JViewport scroller = (JViewport)this.getParent();
            //      Rectangle r = scroller.getViewRect();
            String lab = selected.getLabel();
            int y = yOff + (int) (rowId * yscale);
            g.setColor(Color.black);
            if (lab.length() > 10) {
                g.drawString(lab.substring(0, 10), 2, y + 3);
            } else {
                g.drawString(lab, 2, y + 3);
            }

            int begin = 0;
            int end = cols;
            while (end < asc.length()) {
                g.drawString(asc.substring(begin, end), (int) (10 * xscale), y + 3);
                begin = end;
                end += cols;
                rowId++;
                y = yOff + (int) (rowId * yscale);
                if (end > asc.length()) {
                    end = asc.length();
                }
            }

            if ((patterns != null) && isDiscovery) {
                for (int row = 0; row < patterns.length; row++) {
                    for (int locusId = 0; locusId < patterns[row].getSupport(); locusId++) {
                        int seqId = ((org.geworkbench.util.patterns.CSMatchedSeqPattern) patterns[row]).getId(locusId);
                        if (selected.equals(sequenceDB.getSequence(seqId))) {
                            org.geworkbench.util.patterns.CSMatchedSeqPattern pat = (CSMatchedSeqPattern) patterns[row];
                            int offset = pat.getAbsoluteOffset(locusId);
                            //change for remove the CSMatchedSeqPattern polgara dependence.
                            int ty = pat.offset.size();
                            int MTwidth = (int) (ty * xscale);
                            g.setColor(org.geworkbench.util.patterns.PatternOperations.getPatternColor(pat.hashCode()));
                            int ycoord = yOff + (int) ((offset / cols - 1.7) * yscale);
                            int xcoord = (int) (10 * xscale) + (int) ((offset % cols) * xscale);

                            g.drawOval(xcoord, ycoord, MTwidth, (int) yscale + 1);
                        }
                    }
                }

            }

            if (selectedPatterns != null) {

                for (int row = 0; row < selectedPatterns.size(); row++) {
                    DSPattern<DSSequence, CSSeqRegistration> pattern = selectedPatterns.get(row);

                    if (pattern != null) {
                        DSCollection<DSPatternMatch<DSSequence, CSSeqRegistration>> matches = (DSCollection<DSPatternMatch<DSSequence, CSSeqRegistration>>) ((Annotable) selected).get(pattern);
                        if (matches != null) {
                            for (DSPatternMatch<DSSequence, CSSeqRegistration> match : matches) {
                                CSSeqRegistration reg = match.getRegistration();
                                //              System.out.println(selected.getSequence().substring(im.getOffset(),im.getOffset()+im.getLength()));
                                int MTwidth = (int) (reg.length() * xscale);
                                g.setColor(org.geworkbench.util.patterns.PatternOperations.getPatternColor(pattern.hashCode()));
                                int ycoord = yOff + (int) ((reg.x1 / cols - 1) * yscale) - 5;
                                int xcoord = (int) (10 * xscale) + (int) ((reg.x1 % cols) * xscale);
                                if (((reg.x1 % cols) + reg.length()) > cols) {
                                    g.drawRect(xcoord, ycoord, (int) ((cols - (reg.x1 % cols)) * xscale), (int) yscale + 1);
                                    g.drawRect((int) (10 * xscale), (int) (ycoord + yscale), (int) ((reg.length() + (reg.x1 % cols) - cols) * xscale), (int) yscale + 1);

                                } else {
                                    g.drawRect(xcoord, ycoord, MTwidth, (int) yscale + 1);

                                }
                            }

                        }
                    }

                }
            }
        }
    }

    /**
     * drawPattern
     *
     * @param g       Graphics
     * @param seqId   int
     * @param matches IGetPatternMatchCollection
     * @param r       Rectangle
     */
    private boolean drawPattern(Graphics g, int seqId, List<DSPatternMatch<DSSequence, CSSeqRegistration>> matches, Rectangle r, Color color) {
        if (matches != null) {
            for (int i = 0; i < matches.size(); i++) {
                DSPatternMatch<DSSequence, CSSeqRegistration> match = matches.get(i);
                CSSeqRegistration reg = match.getRegistration();
                int y = yOff + seqId * yStep;
                if (y > r.y) {
                    if (y > r.y + r.height) {
                        return true;
                    }
                    double x0 = reg.x1;
                    double dx = reg.length();
                    int xa = xOff + (int) (x0 * scale) + 1;
                    int xb = (int) (dx * scale) - 1;
                    if (xb < 4) {
                        xb = 4;
                    }
                    g.setColor(color);
                    g.draw3DRect(xa, y - 2, xb, 4, false);
                }
            }
        }
        return false;

    }

    void drawSequence(Graphics g, int rowId, int seqId) {
        String lab = ">seq " + seqId;
        DSSequence theSequence = null;
        if (sequenceDB.getSequenceNo() > 0) {
            theSequence = sequenceDB.getSequence(seqId);

            lab = theSequence.getLabel();

        }
        int y = yOff + rowId * yStep;

        g.setColor(Color.black);
        if (lab.length() > 10) {
            g.drawString(lab.substring(0, 10), 4, y + 3);
        } else {
            g.drawString(lab, 4, y + 3);
        }
        g.drawString(theSequence.getSequence(), 5, y + 3);
    }

    void drawSequence(Graphics g, int rowId, int seqId, double len) {
        String lab = ">seq " + seqId;
        if (sequenceDB.getSequenceNo() > 0) {
            DSSequence theSequence = sequenceDB.getSequence(seqId);
            len = (double) theSequence.length();
            lab = theSequence.getLabel();

        }
        int y = yOff + rowId * yStep;
        int x = xOff + (int) (len * scale);
        g.setColor(Color.black);
        if (lab.length() > 10) {
            g.drawString(lab.substring(0, 10), 4, y + 3);
        } else {
            g.drawString(lab, 4, y + 3);
        }
        g.drawLine(xOff, y, x, y);
    }

    public int getSeqId(int y) {
        int seqId = (y - yOff + 5) / yStep;
        return seqId;
    }

    public int getSeqDx(int x) {
        double scale = Math.min(5.0, (double) (this.getWidth() - 20 - xOff) / (double) sequenceDB.getMaxLength());
        int seqDx = (int) ((double) (x - xOff) / scale);
        return seqDx;
    }

    public String asString() {
        String result = "";
        if (sequenceDB != null) {
            for (int seqId = 0; seqId < sequenceDB.getSequenceNo(); seqId++) {
                AnnotableSequence as = (AnnotableSequence) sequenceDB.getSequence(seqId);

                String match = "";
                for (int row = 0; row < selectedPatterns.size(); row++) {
                    DSPattern<DSSequence, CSSeqRegistration> pattern = selectedPatterns.get(row);
                    DSCollection<DSPatternMatch<DSSequence, CSSeqRegistration>> matches = (DSCollection<DSPatternMatch<DSSequence, CSSeqRegistration>>) as.get(pattern);
                    if (matches != null) {
                        match += pattern.toString() + "\n";
                        for (DSPatternMatch<DSSequence, CSSeqRegistration> aMatch : matches) {
                            CSSeqRegistration reg = aMatch.getRegistration();
                            match += reg.x1 + "\t" + reg.length() + "\t score:" + aMatch.getPValue() + "\t";
                        }

                    }

                }
                if (match.length() > 1) {
                    result = result + as.getLabel() + "\n" + match + "\n";

                }
            }
        }
        return result;
    }

    //
    void this_mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            if (!isText) {
                int y = e.getY();
                int seqid = getSeqId(y);
                selected = sequenceDB.getSequence(seqid);
            }
            this.flipIsText();
            this.repaint();
        }

    }

    void this_mouseMoved(MouseEvent e) {
        if (!isText) {
            mouseOverGraph(e);
        } else {

            mouseOverText(e);

        }

    }

    private void mouseOverText(MouseEvent e) throws ArrayIndexOutOfBoundsException {

        int x1 = e.getX();
        int y1 = e.getY();

        if (selected != null) {
            Font f = new Font("Courier New", Font.PLAIN, 11);
            Graphics g = this.getGraphics();
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            FontMetrics fm = g.getFontMetrics(f);
            String asc = selected.getSequence();
            Rectangle2D r2d = fm.getStringBounds(asc, g);
            double xscale = (r2d.getWidth() + 3) / (double) (asc.length());
            double yscale = 1.4 * xscale;
            int width = this.getWidth();
            int cols = (int) (width / xscale) - 12;

            if ((patterns != null) && isDiscovery) {
                for (int row = 0; row < patterns.length; row++) {
                    for (int locusId = 0; locusId < patterns[row].getSupport(); locusId++) {
                        int seqId = ((CSMatchedSeqPattern) patterns[row]).getId(locusId);
                        if (selected.equals(sequenceDB.getSequence(seqId))) {
                            org.geworkbench.util.patterns.CSMatchedSeqPattern pat = (org.geworkbench.util.patterns.CSMatchedSeqPattern) patterns[row];
                            int offset = pat.getAbsoluteOffset(locusId);
                            //changed by xiaoqing 0n 1.27.07
                            int ty = pat.offset.size();
                            int MTwidth = (int) (ty * xscale);
                            int ycoord = yOff + (int) ((offset / cols - 1.7) * yscale);
                            int xcoord = (int) (10 * xscale) + (int) ((offset % cols) * xscale);
                            Rectangle r1 = new Rectangle(xcoord, ycoord, MTwidth, (int) yscale + 1);
                            if (r1.contains(x1, y1)) {
                                String display = "Pattern:" + pat.getASCII();
                                displayInfo(display);

                            }
                        }
                    }

                }
            }

            if (selectedPatterns != null) {

                for (int row = 0; row < selectedPatterns.size(); row++) {
                    DSPattern<DSSequence, CSSeqRegistration> pattern = selectedPatterns.get(row);
                    if (pattern != null) {
                        DSCollection<DSPatternMatch<DSSequence, CSSeqRegistration>> matches = (DSCollection<DSPatternMatch<DSSequence, CSSeqRegistration>>) ((Annotable) selected).get(pattern);
                        if (matches != null) {
                            for (DSPatternMatch<DSSequence, CSSeqRegistration> match : matches) {
                                CSSeqRegistration reg = match.getRegistration();
                                int MTwidth = (int) (reg.length() * xscale);

                                int ycoord = yOff + (int) ((reg.x1 / cols - 1) * yscale) - 5;
                                int xcoord = (int) (10 * xscale) + (int) ((reg.x1 % cols) * xscale);
                                if (((reg.x1 % cols) + reg.length()) > cols) {
                                    Rectangle r1 = new Rectangle(xcoord, ycoord, (int) ((cols - (reg.x1 % cols)) * xscale), (int) yscale + 1);

                                    Rectangle r2 = new Rectangle((int) (10 * xscale), (int) (ycoord + yscale), (int) ((reg.length() + (reg.x1 % cols) - cols) * xscale), (int) yscale + 1);
                                    if (r1.contains(x1, y1) || r2.contains(x1, y1)) {
                                        String display = "Pattern:" + pattern + " " + "possible hit per 1 kb:" + ((TranscriptionFactor) pattern).getMatrix().getRandom();
                                        displayInfo(display);

                                    }

                                } else {
                                    String display = "Pattern:" + pattern + " " + "possible hit per 1 kb:" + ((TranscriptionFactor) pattern).getMatrix().getRandom();

                                    Rectangle r1 = new Rectangle(xcoord, ycoord, MTwidth, (int) yscale + 1);
                                    if (r1.contains(x1, y1)) {
                                        displayInfo(display);

                                    }

                                }
                            }

                        }
                    }

                }
            }
        }

    }

    private void mouseOverGraph(MouseEvent e) throws ArrayIndexOutOfBoundsException {
        int y = e.getY();
        int seqid = getSeqId(y);
        int x = e.getX();
        x = this.getSeqDx(x);
        Annotable as = (Annotable) sequenceDB.getSequence(seqid);
        if (as != null) {
            String display = "";
            for (int row = 0; row < selectedPatterns.size(); row++) {
                DSPattern<DSSequence, CSSeqRegistration> pattern = selectedPatterns.get(row);
                DSCollection<DSPatternMatch<DSSequence, CSSeqRegistration>> matches = (DSCollection<DSPatternMatch<DSSequence, CSSeqRegistration>>) as.get(pattern);
                if (matches != null) {
                    for (DSPatternMatch<DSSequence, CSSeqRegistration> match : matches) {
                        CSSeqRegistration reg = match.getRegistration();
                        if ((x > reg.x1) && (x < reg.x2)) {
                            display = "Pattern:" + pattern + " " + "possible hit per 1 kb:" + ((TranscriptionFactor) pattern).getMatrix().getRandom();
                            displayInfo(display);
                        }
                    }
                }
            }
        }
    }

    private void displayInfo(String display) {

        //                                this.setToolTipText(display);
        if (jinfoPanel != null) {
            Graphics g = jinfoPanel.getGraphics();
            Font f = new Font("Courier New", Font.PLAIN, 11);
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.clearRect(0, 0, jinfoPanel.getWidth(), jinfoPanel.getHeight());
            g.setFont(f);
            g.drawString(display, 10, 20);

        }
    }

    /**
     * drawPatterns
     *
     * @param patterns Pattern[]
     */
    public void drawPatterns(org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern[] patterns) {
        isDiscovery = true;

        this.patterns = patterns;
        repaint();

    }

    boolean drawPattern(Graphics g, int rowId, double x0, org.geworkbench.util.patterns.CSMatchedSeqPattern pat, Rectangle r, Color color) {
        int y = yOff + rowId * yStep;
        if (y > r.y) {
            if (y > r.y + r.height) {
                return true;
            }
            //            double x0 = (double) pat.getOff(locusId);
            //changed on 1/25/07 by xiaoqing
            //double dx = pat.offset.value[pat.getLength() - 1].getDx() + 1;
            double dx = pat.offset.get(pat.getLength() - 1).getPosition() + 1;
            int xa = xOff + (int) (x0 * scale) + 1;
            int xb = (int) (dx * scale) - 1;
            g.setColor(color);
            if (xb < 4) {
                xb = 4;
            }

            //      g.fill3DRect(xa, y - 2, xb - xa, 3, false);
            g.drawOval(xa, y - 2, xb, 4);
        }
        return false;
    }

    boolean drawFlexiPattern(Graphics g, int rowId, double x0, org.geworkbench.util.patterns.CSMatchedSeqPattern pat, Rectangle r, Color color) {
        int y = yOff + rowId * yStep;
        if (y > r.y) {
            if (y > r.y + r.height) {
                return true;
            }
            //double dx = pat.offset.value[pat.getLength() - 1].getDx() + 1;
             double dx = pat.offset.get(pat.getLength() - 1).getPosition() + 1;
            int xa = xOff + (int) (x0 * scale) + 1;
            int xb = xa + (int) (dx * scale) - 1;
            g.setColor(color);
            g.drawOval(xa, y - 2, xb - xa, 4);

        }
        return false;
    }

}
