package org.geworkbench.components.sequenceretriever;

import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
import org.geworkbench.bison.datastructure.biocollections.DSCollection;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.util.patterns.PatternSequenceDisplayUtil;
import org.geworkbench.util.patterns.PatternLocations;
import org.geworkbench.util.BrowserLauncher;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.AbstractTableModel;
import java.util.HashMap;
import java.util.TreeSet;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

/**
 * Created by IntelliJ IDEA.
 * User: xiaoqing
 * Date: Sep 2, 2006
 * Time: 12:10:12 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * A new display specifically for all retrievedSequences.
 */
public class RetrievedSequenceDisplayPanel extends JPanel {
    boolean imageMode = true;
    final int xOff = 60;
    final int yOff = 20;
    final int xStep = 5;
    final int yStep = 14;
    double scale = 1.0;
    private TableModel model;
    private JTable table;
    int selected = 0;
    int maxSeqLen = 1;
    private String displayInfo = "";
    public static final int ROW_HEIGHT = 60;
    //ArrayList  selectedPatterns   = null;
    DSCollection<DSMatchedPattern<DSSequence, CSSeqRegistration>> selectedPatterns = null;
    DSSequenceSet sequenceDB = null;
    HashMap<CSSequence,
            PatternSequenceDisplayUtil> sequencePatternmatches;
    private HashMap<String, RetrievedSequenceView> retrievedMap = new HashMap<String, RetrievedSequenceView>();
    boolean showAll = false;
    private boolean lineView;
    private boolean singleSequenceView;
    private final static Color SEQUENCEBACKGROUDCOLOR = Color.BLACK;
    public final static Color DRECTIONCOLOR = Color.RED;
    private double yBasescale;
    private int xBaseCols;
    private int[] eachSeqStartRowNum;
    private double xBasescale;
    private int seqXclickPoint = 0;
    private DSSequence selectedSequence;
    private JPopupMenu itemListPopup = new JPopupMenu();
    private RetrievedSequencesPanel retrievedSequencesPanel;
    JMenuItem imageSnapshotItem = new JMenuItem("Image Snapshot");

    public RetrievedSequenceDisplayPanel() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    void jbInit() throws Exception {
        itemListPopup = new JPopupMenu();
        //imageSnapshotItem = new JMenuItem("Image Snapshot");
        //itemListPopup.add(imageSnapshotItem);
        //itemListPopup.add(saveItem);
        imageSnapshotItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createImageSnapshot();
            }

        });
        model = new TableModel();
        table = new JTable(model);


        final JLabel imageLabel = new JLabel() {
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        table.setDefaultRenderer(RetrievedSequenceView.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof RetrievedSequenceView) {
                    return (RetrievedSequenceView) value;
                }
                return new RetrievedSequenceView(null);
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != -1) {
                    selectedSequence = (DSSequence) sequenceDB.get(row);
                    int column = table.columnAtPoint(e.getPoint());
                    if (column == 2) {
                        Object sequence = sequenceDB.get(row);
                        RetrievedSequenceView retrievedSequenceView = retrievedMap.get(sequence.toString());
                        double xT = retrievedSequenceView.getCurrentLocation();
                        if (xT < ((CSSequence) sequence).length()) {
                            seqXclickPoint = new Integer((int) xT);
                            retrievedSequencesPanel.setSelectedSequence(selectedSequence);
                            retrievedSequencesPanel.updateDetailPanel(seqXclickPoint);
                        }
                    }
                }
                if (e.getClickCount() == 2) {
                    row = table.rowAtPoint(e.getPoint());
                    if (row != -1) {
                        int column = table.columnAtPoint(e.getPoint());
                        if (column == 1 && !sequenceDB.isDNA()) {
                            Object sequence = sequenceDB.get(row);
                            RetrievedSequenceView retrievedSequenceView = retrievedMap.get(sequence.toString());
                            try {
                                if (retrievedSequenceView.getUrl() != null) {
                                    BrowserLauncher.openURL(retrievedSequenceView.getUrl());
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else if (column == 2) {
                            if (retrievedSequencesPanel != null) {
                                retrievedSequencesPanel.flapToNewView(false);
                            }
                        }
                    }
                }
            }
        });
        table.setRowHeight(ROW_HEIGHT);
        // setting the size of the table and its columns
        if (sequenceDB != null) {
            table.setPreferredScrollableViewportSize(new Dimension(600, 300));
            table.getColumnModel().getColumn(0).setPreferredWidth(15);
            table.getColumnModel().getColumn(1).setPreferredWidth(100);
            table.getColumnModel().getColumn(2).setPreferredWidth(400);
        }
        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new BlastDetaillistSelectionListener());
        table.setRowHeight(RetrievedSequenceView.HEIGHT);
        JScrollPane scrollPane = new JScrollPane(table);
        this.setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

    }

    public void setRetrievedMap(HashMap<String, RetrievedSequenceView> retrievedMap) {
        this.retrievedMap = retrievedMap;
    }

    public void addMenuItem(JMenuItem item) {
        itemListPopup.add(item);
        repaint();
    }

    public org.geworkbench.events.ImageSnapshotEvent
            createImageSnapshot() {
        Dimension panelSize = this.getSize();
        BufferedImage image = new BufferedImage(panelSize.width,
                panelSize.height,
                BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        this.paint(g);
        ImageIcon icon = new ImageIcon(image, "Sequence Snapshot");
           return   new org.geworkbench.
                events.ImageSnapshotEvent("Sequence Snapshot", icon,
                org.geworkbench.events.
                        ImageSnapshotEvent.Action.SAVE);

    }

    /**
     * New Initialization method. It should be used as a main entry point. Others initialization method
     * should be disabled or replaced.
     *
     * @param patternSeqMatches HashMap
     * @param seqDB             DSSequenceSet
     * @param isLineView        boolean
     */
    public void initialize(HashMap<CSSequence,
            PatternSequenceDisplayUtil> patternSeqMatches,
                           DSSequenceSet seqDB,
                           boolean isLineView) {
        sequencePatternmatches = patternSeqMatches;
        sequenceDB = seqDB;
        lineView = isLineView;
        repaint();

    }

    public void initialize(
            DSSequenceSet seqDB,
            boolean isLineView) {

        sequenceDB = seqDB;
        lineView = isLineView;
        try {
            this.removeAll();
            this.jbInit();
            repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setShowAll(boolean all) {
        showAll = all;
    }

    public void setMaxSeqLen(int maxSeqLen) {
        this.maxSeqLen = maxSeqLen;
        RetrievedSequenceView.setMaxSeqLen(maxSeqLen);

    }

    public void setlineView(boolean lineView) {
        this.lineView = lineView;
        revalidate();
    }

    public void setDisplayInfo(String displayInfo) {
        this.displayInfo = displayInfo;
    }

    public void setSeqXclickPoint(int seqXclickPoint) {
        this.seqXclickPoint = seqXclickPoint;
    }

    public void setSelectedSequence(DSSequence selectedSequence) {
        this.selectedSequence = selectedSequence;
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
        g.setColor(SEQUENCEBACKGROUDCOLOR);
        if (lab.length() > 9) {
            g.drawString(lab.substring(0, 9), 4, y + 3);
        } else {
            g.drawString(lab, 4, y + 3);
        }
        g.drawLine(xOff, y, x, y);
    }


    public int getMaxSeqLen() {
        return maxSeqLen;
    }

    public boolean islineView() {
        return lineView;
    }

    public String getDisplayInfo() {
        return displayInfo;
    }

    public int getSeqXclickPoint() {
        return seqXclickPoint;
    }

    public DSSequence getSelectedSequence() {
        return selectedSequence;
    }

    private int getSeqDx(int x) {

       return (int) ((double) (x - xOff) / scale);

    }

//    /**
//     * Handle Mouse clicks.
//     *
//     * @param e MouseEvent
//     */
//    public void this_mouseClicked(final MouseEvent e) {
//        if (e.isMetaDown()) {
//            SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
//                    itemListPopup.show(e.getComponent(), e.getX(), e.getY());
//                }
//            });
//
//            return;
//        }
//        setTranslatedParameters(e);
//
//        if (e.getClickCount() == 2) {
//
//            this.flipLineView();
//            this.repaint();
//        }
//
//    }

    /**
     * Set up the coresponding parameters when mouse moves.
     *
     * @param e MouseEvent
     */
    public void setMouseMoveParameters(MouseEvent e) {
        int y = e.getY();
        int x = e.getX();
        int mouseSelected = -1;
        int mouseMovePoint = -1;
        DSSequence mouseSelectedSequence;
        if (!lineView) {
            mouseSelected = getSeqIdInFullView(y);
            if (eachSeqStartRowNum != null &&
                    mouseSelected < eachSeqStartRowNum.length) {
                mouseMovePoint = (int) ((int) ((y - yOff - 1 -
                        ((double) eachSeqStartRowNum[
                                mouseSelected]) *
                                yBasescale) / yBasescale) *
                        xBaseCols +
                        x / xBasescale -
                        5);
            }
        } else {
            if (!singleSequenceView) {
                mouseSelected = getSeqId(y);
                mouseMovePoint = getSeqDx(x);

            } else {

                mouseMovePoint = (int) ((int) ((y - yOff - 1) / yBasescale) *
                        xBaseCols +
                        x / xBasescale -
                        5);
            }
        }
        if (sequenceDB != null && selected < sequenceDB.size()) {
            mouseSelectedSequence = sequenceDB.getSequence(mouseSelected);
        } else {
            mouseSelectedSequence = null;
        }
        if (mouseSelectedSequence != null) {
            displayInfo = "For sequence " + mouseSelectedSequence.getLabel() +
                    ", total length: " +
                    mouseSelectedSequence.length();
            if (sequencePatternmatches != null) {
                PatternSequenceDisplayUtil psu = sequencePatternmatches.get(
                        mouseSelectedSequence);
                if (psu != null && psu.getTreeSet() != null) {
                    displayInfo += ", pattern number: " + psu.getTreeSet().size();
                }
            }
            if ((mouseMovePoint <= mouseSelectedSequence.length()) &&
                    (mouseMovePoint > 0)) {
                this.setToolTipText("" + mouseMovePoint);
                displayInfo += ". Current location: " + mouseMovePoint;
            }
        }
        {
            this.setToolTipText(null);
        }

    }



    /**
     * getSeqIdInFullView
     *
     * @param y int
     * @return int
     */
    private int getSeqIdInFullView(int y) {
        double yBase = (y - yOff - 3) / yBasescale + 1;
        if (eachSeqStartRowNum != null) {
            for (int i = 0; i < eachSeqStartRowNum.length; i++) {
                if (eachSeqStartRowNum[i] > yBase) {
                    return Math.max(0, i - 1);
                }

            }
            return Math.max(0, eachSeqStartRowNum.length - 1);
        }
        return 0;
    }

    public void flipLineView() {
        singleSequenceView = !singleSequenceView;
        //lineView = !lineView;
    }

    public int getSeqId(int y) {
        return  (y - yOff + 5) / yStep;
             }

    public void this_mouseMoved(MouseEvent e) {
        setMouseMoveParameters(e);
        if (!lineView) {
            mouseOverFullView(e);
        } else {
            mouseOverLineView(e);

        }

    }

    public RetrievedSequencesPanel getRetrievedSequencesPanel() {
        return retrievedSequencesPanel;
    }

    public void setRetrievedSequencesPanel(RetrievedSequencesPanel retrievedSequencesPanel) {
        this.retrievedSequencesPanel = retrievedSequencesPanel;
    }

    private void mouseOverFullView(MouseEvent e) throws
            ArrayIndexOutOfBoundsException {
        if (sequenceDB == null) {
            return;
        }
        int x1 = e.getX();
        int y1 = e.getY();

        Font f = new Font("Courier New", Font.PLAIN, 11);
        Graphics g = this.getGraphics();
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics fm = g.getFontMetrics(f);
        if (sequenceDB.getSequence(selected) == null ||
                sequenceDB.getSequence(selected).getSequence() == null) {
            return;
        }
        String asc = sequenceDB.getSequence(selected).getSequence();
        Rectangle2D r2d = fm.getStringBounds(asc, g);
        double xscale = (r2d.getWidth() + 3) / (double) (asc.length());
        double yscale = 1.3 * r2d.getHeight();
        int width = this.getWidth();
        int cols = (int) (width / xscale) - 8;
        int dis = (int) ((int) ((y1 - yOff - 1) / yscale) * cols + x1 / xscale -
                5);
        if (sequenceDB.getSequence(selected) != null) {
            if (((y1 - yOff - 1) / yscale > 0) && (dis > 0) &&
                    (dis <= sequenceDB.getSequence(selected).length())) {
                this.setToolTipText("" + dis);
            }
        }
    }

    private void mouseOverLineView(MouseEvent e) throws
            ArrayIndexOutOfBoundsException {
        int y = e.getY();
        int x = e.getX();
        //displayInfo = "";
        if (!singleSequenceView) {
            int seqid = getSeqId(y);

            if (sequenceDB == null) {
                return;
            }
            int off = this.getSeqDx(x);
            DSSequence sequence = sequenceDB.getSequence(seqid);
            if (sequence != null) {
                if ((off <= sequenceDB.getSequence(seqid).length()) && (off > 0)) {
                    String texttip = getTipInfo(sequence, off);
                    this.setToolTipText(texttip);
                }
            }
        } else {

            Font f = new Font("Courier New", Font.PLAIN, 11);
            Graphics g = this.getGraphics();
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            FontMetrics fm = g.getFontMetrics(f);
            if (sequenceDB.getSequence(selected) == null ||
                    sequenceDB.getSequence(selected).getSequence() == null) {
                return;
            }
            DSSequence sequence = sequenceDB.getSequence(selected);
            String asc = sequence.getSequence();
            displayInfo = "Length of " + sequence.getLabel() + ": " +
                    sequence.length();
            Rectangle2D r2d = fm.getStringBounds(asc, g);
            double xscale = (r2d.getWidth() + 3) / (double) (asc.length());
            double yscale = 1.3 * r2d.getHeight();
            int width = this.getWidth();
            int cols = (int) (width / xscale) - 8;
            int dis = (int) ((int) ((y - yOff - 1) / yscale) * cols +
                    x / xscale -
                    5);
            if (sequenceDB.getSequence(selected) != null) {
                if (x >= 6 * xscale && x <= (cols + 6) * xscale &&
                        ((y - yOff - 1) / yscale > 0) && (dis > 0) &&
                        (dis <= sequenceDB.getSequence(selected).length())) {

                    String texttip = getTipInfo(sequence, dis);
                    this.setToolTipText(texttip);

                    //  this.setToolTipText("" + dis);
                }
                displayInfo += ". Current location: " + dis;
            }
        }

    }

    /**
     * getTipInfo
     *
     * @param sequence DSSequence
     * @param off      int
     * @return String
     */
    private String getTipInfo(DSSequence sequence, int off) {
        String tip = "" + off;
        if (sequencePatternmatches != null) {
            PatternSequenceDisplayUtil psd = sequencePatternmatches.
                    get(sequence);
            if (psd != null) {
                TreeSet<PatternLocations>
                        patternsPerSequence =psd.getTreeSet();
                if (patternsPerSequence != null &&
                        patternsPerSequence.size() > 0) {
                    for (PatternLocations pl : patternsPerSequence) {
                        CSSeqRegistration reg = pl.getRegistration();
                        if (reg != null && reg.x1 <= off && reg.x2 >= off) {
                            int x1 = reg.x1 + 1;
                            int x2 = reg.x2 + 1;
                            if (pl.getPatternType().equals(
                                    PatternLocations.DEFAULTTYPE)) {
                                tip = tip + " " + pl.getAscii() + "<" + x1 + "," + x2 + "> ";
                            } else if (pl.getPatternType().equals(
                                    PatternLocations.TFTYPE)) {
                                tip = tip + " " + pl.getAscii() + "<" + x1 + "," + x2 + "> ";
                            }
                        }
                    }
                }
            }
        }

        return tip;
    }

    /**
     * initialize
     */
    public void initialize() {
    }

    private class TableModel extends AbstractTableModel {

        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Include";
                case 1:
                    return "Name";
                case 2:
                    return "Sequence Detail";
                default:
                    return "Sequence Detail";
            }
        }

        public int getRowCount() {
            if (sequenceDB == null) {
                return 0;
            } else {
                return sequenceDB.size();
            }
        }

        public int getColumnCount() {
            if (sequenceDB == null) {
                return 0;
            } else {
                return 3;
            }
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Object sequence = sequenceDB.get(rowIndex);
            RetrievedSequenceView retrievedSequenceView = retrievedMap.get(sequence.toString());
            if (retrievedSequenceView == null) {
                return null;
            }
            switch (columnIndex) {
                case 0:
                    return  retrievedSequenceView.isIncluded();
                case 1:
                    if (sequenceDB.isDNA()) {
                        return sequence.toString();
                    } else {
                        return "<html><<font  color=\"#0000FF\"><u>" + sequence.toString() + "</u></font>";
                    }
                case 2:
                    return retrievedSequenceView;
                default:
                    return sequence.toString();
            }
        }

        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Boolean.class;
                case 2:
                    return RetrievedSequenceView.class;

                default:
                    return String.class;

            }
        }

        /*returns if the cell is editable; returns false for all cells in columns except column 6*/
        public boolean isCellEditable(int row, int col) {
            return col == 0;

        }

        /*detect change in cell at (row, col); set cell to value; update the table */
        public void setValueAt(Object value, int row, int col) {
            Object sequence = sequenceDB.get(row);
            if (sequence != null) {
                RetrievedSequenceView retrievedSequenceView = retrievedMap.get(sequence.toString());
                if (value != null) {
                    retrievedSequenceView.setIncluded(((Boolean) value).booleanValue());
                }
            }
            fireTableCellUpdated(row, col);
        }


    }

    class JDetailPanel extends JPanel {
        public JDetailPanel(Object o) {
            if (o instanceof CSSequence) {
                repaint();
            }
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            int seqId = 0;
            DSSequence selectedSequence = (DSSequence) sequenceDB.get(0);
            if (selectedSequence == null) {
                g.clearRect(0, 0, getWidth(),
                        getHeight());
                String lab = ">seq " + seqId;
                double len = 0;
                if (sequenceDB.getSequenceNo() > 0) {
                    DSSequence theSequence = sequenceDB.getSequence(seqId);
                    len = (double) theSequence.length();
                    lab = theSequence.getLabel();

                }
                double newScale = Math.min(5.0,
                        (double) (this.getWidth() - 20 - xOff) /
                                (double) maxSeqLen);
                int y = yOff;
                int x = xOff + (int) (len * newScale);
                g.setColor(SEQUENCEBACKGROUDCOLOR);
                if (lab.length() > 9) {
                    g.drawString(lab.substring(0, 9), 4, y + 3);
                } else {
                    g.drawString(lab, 4, y + 3);
                }
                g.drawLine(xOff, y, x, y);
               }
        }
    }

    private class BlastDetaillistSelectionListener implements
            ListSelectionListener {
        int selectedRow;


        public void valueChanged(ListSelectionEvent e) {
            //Ignore extra messages.
            if (e.getValueIsAdjusting()) {
                return;
            }
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty()) {

            } else {
                selectedRow = lsm.getMinSelectionIndex();
//                if (hits != null && hits.size() > selectedRow) {
//                    selectedHit = (BlastObj) hits.get(selectedRow);
//                    showAlignment(selectedHit);
///
//                }

            }
        }
    }
}
