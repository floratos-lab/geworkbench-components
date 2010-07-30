package org.geworkbench.components.sequenceretriever;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.geworkbench.bison.datastructure.biocollections.Collection;
import org.geworkbench.bison.datastructure.biocollections.DSCollection;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.MicroarraySetViewEvent;
import org.geworkbench.events.SequenceDiscoveryTableEvent;
import org.geworkbench.util.Util;
import org.geworkbench.util.patterns.CSMatchedSeqPattern;
import org.geworkbench.util.patterns.PatternLocations;
import org.geworkbench.util.patterns.PatternOperations;
import org.geworkbench.util.patterns.PatternSequenceDisplayUtil;

/**
 * Originally created by IntelliJ IDEA.
 * 
 * @author xiaoqing
 * @version $Id$
 */

/**
 * The main GUI class for sequence display panel.
 * 
 * @version $Id$
 */
public class RetrievedSequencesPanel extends JPanel {
	private static final long serialVersionUID = 430612435863058186L;
	
    private int prevSeqId = -1;
    private int prevSeqDx = 0;
    private DSSequenceSet sequenceDB = new CSSequenceSet();
    private DSSequenceSet orgSequenceDB = new CSSequenceSet();
    private DSSequenceSet displaySequenceDB = new CSSequenceSet();
    private HashMap<CSSequence,
            PatternSequenceDisplayUtil> patternLocationsMatches;

    //Layouts
    private BorderLayout borderLayout2 = new BorderLayout();
    private DSSequence selectedSequence = null;
    //Panels and Panes
    private JDetailPanel sequencedetailPanel = new JDetailPanel();
    private JPanel bottomPanel = new JPanel();
    private JButton leftShiftButton = new JButton();
    private JButton rightShiftButton = null;
    private JScrollPane seqScrollPane = new JScrollPane();
    
    private RetrievedSequenceDisplayPanel seqViewWPanel = new
            RetrievedSequenceDisplayPanel();
    private SingleSequenceViewPanel oldViewPanel = new SingleSequenceViewPanel();
    private DSCollection<DSMatchedPattern<DSSequence, CSSeqRegistration>>
            selectedPatterns = new Collection<DSMatchedPattern<DSSequence,
            CSSeqRegistration>>();
    private JToolBar jToolBar1 = new JToolBar();
    
    private JToggleButton showAllBtn = new JToggleButton();
    private JCheckBox jAllSequenceCheckBox = new JCheckBox();
    private JLabel jViewLabel = new JLabel();
    private JComboBox jViewComboBox = new JComboBox();
    private static final String LINEVIEW = "Line";
    private static final String FULLVIEW = "Full Sequence";
    private JTextField jSequenceSummaryTextField = new JTextField();
    private boolean isLineView = true; //true is for LineView.
    
    private CSSequenceSet activeSequenceDB = null;
    private boolean subsetMarkerOn = true;
    private static final String NONBASIC = "NONBASIC";
    
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private boolean goLeft = false;
    private int xStartPoint = -1;
    private static final int GAP = 40;

    public RetrievedSequencesPanel() {
        try {
            jbInit();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {

        this.setLayout(borderLayout2);
        sequencedetailPanel.setBorder(BorderFactory.createEtchedBorder());
        sequencedetailPanel.setMinimumSize(new Dimension(50, 40));
        sequencedetailPanel.setPreferredSize(new Dimension(60, 50));

        seqViewWPanel.setRetrievedSequencesPanel(this);
        oldViewPanel.setRetrievedSequencesPanel(this);

        oldViewPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                joldViewPanel_mouseClicked(e);
            }
        });
        oldViewPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                oldViewPanel.this_mouseMoved(e);
            }
        });
        this.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(InputMethodEvent e) {
            }

            public void caretPositionChanged(InputMethodEvent e) {
                this_caretPositionChanged(e);
            }
        });
        this.addPropertyChangeListener(new java.beans.
                PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                this_propertyChange(e);
            }
        });
        jAllSequenceCheckBox.setToolTipText("Click to display all sequences.");
        jAllSequenceCheckBox.setSelected(false);
        jAllSequenceCheckBox.setText("All Sequences");
        jAllSequenceCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jAllSequenceCheckBox_actionPerformed(e);
            }
        });
        jViewLabel.setText("View: ");
        jSequenceSummaryTextField.setText("Move the mouse over to see details.");

        seqViewWPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                seqViewWPanel_mouseMoved(e);
            }
        });
        jViewComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jViewComboBox_actionPerformed(e);
            }
        });
        jViewComboBox.setToolTipText("Select a view to display results.");
        bottomPanel = new JPanel();
        leftShiftButton = new JButton();

        ImageIcon leftButtonIcon = Util.createImageIcon("/images/back.gif");
        leftShiftButton.setIcon(leftButtonIcon);
        ImageIcon rightButtonIcon = Util.createImageIcon("/images/forward.gif");
        rightShiftButton = new JButton();
        rightShiftButton.setIcon(rightButtonIcon);
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(leftShiftButton, BorderLayout.WEST);
        bottomPanel.add(sequencedetailPanel, BorderLayout.CENTER);
        bottomPanel.add(rightShiftButton, BorderLayout.EAST);
        leftShiftButton.setEnabled(false);
        rightShiftButton.setEnabled(false);
        leftShiftButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setMoveDirection(LEFT);
                updateBottomPanel();
            }
        });
        rightShiftButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setMoveDirection(RIGHT);
                updateBottomPanel();
            }
        });

        this.add(bottomPanel, BorderLayout.SOUTH);
        this.add(seqScrollPane, BorderLayout.CENTER);
        this.add(jToolBar1, BorderLayout.NORTH);
        jToolBar1.add(jViewLabel);
        jToolBar1.add(jViewComboBox);

        jViewComboBox.addItem(LINEVIEW);
        jViewComboBox.addItem(FULLVIEW);
        jViewComboBox.setSize(jViewComboBox.getPreferredSize());
        if (sequenceDB != null) {
            seqViewWPanel.setMaxSeqLen(sequenceDB.getMaxLength());
        }
        //  seqViewWPanel.initialize(selectedPatterns, sequenceDB);
        seqScrollPane.getViewport().add(seqViewWPanel, null);
        seqViewWPanel.setShowAll(showAllBtn.isSelected());
    }

    public void setRetrievedMap(HashMap<String, RetrievedSequenceView> retrievedMap) {
        seqViewWPanel.setRetrievedMap(retrievedMap);
    }

    /**
     * cleanButtons
     *
     * @param aString String
     */
    public void removeButtons(String aString) {
        if (aString.equals(NONBASIC)) {
            jToolBar1.remove(showAllBtn);
            jToolBar1.remove(jAllSequenceCheckBox);  //fix bug 924
            jToolBar1.remove(jSequenceSummaryTextField);
            repaint();
        }
    }

    public DSSequence getSelectedSequence() {
        return selectedSequence;
    }

    public void setSelectedSequence(DSSequence selectedSequence) {
        this.selectedSequence = selectedSequence;
    }

    public DSSequenceSet getDisplaySequenceDB() {
        return displaySequenceDB;
    }

    public void setDisplaySequenceDB(DSSequenceSet displaySequenceDB) {
        this.displaySequenceDB = displaySequenceDB;
        getDataSetView();
    }

    public void flapToNewView(boolean newView) {
        if (!newView) {
            seqScrollPane.getViewport().removeAll();
            seqScrollPane.getViewport().add(oldViewPanel);
            oldViewPanel.setSelectedSequence(seqViewWPanel.getSelectedSequence());
            oldViewPanel.setSequenceDB(sequenceDB);
            oldViewPanel.setSingleSequenceView(true);
            oldViewPanel.setLineView(true);
            oldViewPanel.repaint();
            revalidate();
            repaint();
        } else {
            seqScrollPane.getViewport().removeAll();
            seqScrollPane.getViewport().add(seqViewWPanel, null);
            revalidate();
            repaint();
        }

    }

    /**
     * @param directionStr
     */
    private void setMoveDirection(String directionStr) {
        if (directionStr.equals(LEFT)) {
            goLeft = true;
        } else {
            goLeft = false;
        }
    }

    /**
     * geneSelectorAction
     *
     * @param e GeneSelectorEvent
     */
    public void sequenceDBUpdate(GeneSelectorEvent e) {
        getDataSetView();
    }


    protected void fireModelChangedEvent(MicroarraySetViewEvent event) {
        this.repaint();
    }


    void chkActivateMarkers_actionPerformed(ActionEvent e) {
        subsetMarkerOn = !((JCheckBox) e.getSource()).isSelected();
        getDataSetView();
    }

    public void getDataSetView() {
        subsetMarkerOn = !jAllSequenceCheckBox.isSelected();
        if (subsetMarkerOn) {
              activeSequenceDB = (CSSequenceSet) displaySequenceDB;
        } else if (orgSequenceDB != null) {
            activeSequenceDB = (CSSequenceSet) orgSequenceDB;
        }
        if (activeSequenceDB != null) {
            sequenceDB = activeSequenceDB;
            initPanelView();
        }
         initPanelView();
    }


    public void patternSelectionHasChanged(SequenceDiscoveryTableEvent e) {
        setPatterns(e.getPatternMatchCollection());
        getDataSetView();

    }

    public void initialize(DSSequenceSet seqDB) {
        setSequenceDB(seqDB);
        updateBottomPanel();
        repaint();
    }


    public void initialize() {

        sequenceDB = null;
        seqViewWPanel.removeAll();
        selectedSequence = null;
        seqViewWPanel.setSelectedSequence(null);
        displaySequenceDB = new CSSequenceSet();
        oldViewPanel.initialize();
        updateBottomPanel();
        revalidate();
        repaint();
    }

    private boolean processEvent(){
    	
    	if (selectedSequence == null) {
            return true;
        }
    	
        if (goLeft) {
            if (xStartPoint - GAP > 0) {
                xStartPoint = xStartPoint >= GAP ? xStartPoint - GAP : 1;
            } else {
                xStartPoint = 1;
                leftShiftButton.setEnabled(false);
            }
        } else {
            if (xStartPoint < selectedSequence.length() - GAP) {
                xStartPoint += GAP;
            } else {
                rightShiftButton.setEnabled(false);
            }
        }
        sequencedetailPanel.repaint();
        prevSeqDx = xStartPoint;
        sequencedetailPanel.setOpaque(false);
        return true;

    }
    
    private void updateBottomPanel() {

    	final Runnable processEventThread = new Runnable() {
			public void run() {
				 processEvent();
			}
		};

		Thread t = new Thread(processEventThread);
		t.setPriority(Thread.MAX_PRIORITY);
 		t.start();
		 
		//SwingUtilities.invokeLater(t);
       
    }

    
    

    void updateDetailPanel(int newPosition) {
        // seqViewWPanel.this_mouseClicked(e);
        xStartPoint = newPosition;
        sequencedetailPanel.repaint();
        //updateBottomPanel(e);
    }

    /**
     * Change view from single sequence view to new view.
     *
     * @param e MouseEvent
     */

    void joldViewPanel_mouseClicked(MouseEvent e) {
        oldViewPanel.this_mouseClicked(e);
        if (e.getClickCount() == 2) {
            if (isLineView) {
                flapToNewView(true);
            }
        }
        xStartPoint = oldViewPanel.getSeqXclickPoint();
        selectedSequence = oldViewPanel.getSelectedSequence();
        sequencedetailPanel.repaint();
        //updateBottomPanel(e);
    }

    void showPatterns() {
        if (selectedPatterns.size() > 0) {
            for (int i = 0; i < selectedPatterns.size(); i++) {
                DSMatchedSeqPattern pattern = (DSMatchedSeqPattern)
                        selectedPatterns.get(i);
                if (pattern instanceof CSMatchedSeqPattern) {
                    if (pattern.getASCII() == null) {
                        PatternOperations.fill((CSMatchedSeqPattern) pattern,
                                displaySequenceDB);
                    }
                    //( (DefaultListModel) patternList.getModel()).addElement(pattern);
                    this.repaint();
                }
            }
        }
    }

    void this_caretPositionChanged(InputMethodEvent e) {
        showPatterns();
    }

    void this_propertyChange(PropertyChangeEvent e) {

        showPatterns();
    }

    public void deserialize(String filename) {

    }


    public void setSequenceDB(DSSequenceSet db) {
        orgSequenceDB = db;
        sequenceDB = db;
        displaySequenceDB = db;

        getDataSetView();

        if (sequenceDB != null) {
            seqViewWPanel.setMaxSeqLen(sequenceDB.getMaxLength());
            //      seqViewWPanel.initialize(null, db);
            selectedPatterns.clear();
            repaint();
        }
    }

    public void setDirection(boolean direction) {
        this.goLeft = direction;
    }


    public DSSequenceSet getSequenceDB() {
        return sequenceDB;
    }

    public RetrievedSequenceDisplayPanel getSeqViewWPanel() {
        return seqViewWPanel;
    }

    public boolean isDirection() {
        return goLeft;
    }


    public void setPatterns(DSCollection<DSMatchedPattern<DSSequence, CSSeqRegistration>> matches) {
        selectedPatterns.clear();
        for (int i = 0; i < matches.size(); i++) {
            selectedPatterns.add(matches.get(i));
        }
    }


    public void showAllBtn_actionPerformed(ActionEvent e) {
        if (selectedPatterns == null && showAllBtn.isSelected()) {
            if (sequenceDB == null) {
                JOptionPane.showMessageDialog(null,
                        "No sequence is stored right now, please load sequences first.",
                        "No Pattern",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "No pattern is stored right now, please generate patterns with Pattern Discory module first.",
                        "No Pattern",
                        JOptionPane.ERROR_MESSAGE);
                seqViewWPanel.setMaxSeqLen(sequenceDB.getMaxLength());
                displaySequenceDB = sequenceDB;
                seqViewWPanel.setShowAll(false);
            }

            showAllBtn.setSelected(false);

        }
        initPanelView();

    }

    public void jViewComboBox_actionPerformed(ActionEvent e) {

        initPanelView();

    }

    public void seqViewWPanel_mouseMoved(MouseEvent e) {

        seqViewWPanel.this_mouseMoved(e);
        jSequenceSummaryTextField.setText(seqViewWPanel.getDisplayInfo());

    }

    /**
     * Transform the patterns to patternsUtil class.
     * Child class should override this method.
     */
    public void updatePatternSeqMatches() {
        patternLocationsMatches = PatternOperations.processPatterns(
                selectedPatterns,
                sequenceDB);

    }

    /**
     * Initate the Panel, which should be used as the entry point.
     *
     * @return boolean
     */
    public boolean initPanelView() {
        //updatePatternSeqMatches();
        isLineView = jViewComboBox.getSelectedItem().equals(LINEVIEW);
        if (isLineView) {
            seqViewWPanel.initialize(sequenceDB, true);
            flapToNewView(true);

        } else {
            seqScrollPane.getViewport().removeAll();
            seqScrollPane.getViewport().add(oldViewPanel);
            oldViewPanel.setSelectedSequence(seqViewWPanel.getSelectedSequence());
            oldViewPanel.setSequenceDB(sequenceDB);
            oldViewPanel.setSingleSequenceView(true);
            oldViewPanel.setLineView(isLineView);
            oldViewPanel.repaint();
        }
        seqViewWPanel.revalidate();
        seqViewWPanel.repaint();
        this.revalidate();
        this.repaint();
        return true;
    }

    public void jAllSequenceCheckBox_actionPerformed(ActionEvent e) {
        getDataSetView();
    }

    private class JDetailPanel extends JPanel {
		private static final long serialVersionUID = -9171605516043986751L;

		public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // DSSequence selectedSequence = seqViewWPanel.getSelectedSequence();
            if (selectedSequence == null) {
                g.clearRect(0, 0, sequencedetailPanel.getWidth(),
                        sequencedetailPanel.getHeight());
                rightShiftButton.setEnabled(false);
                leftShiftButton.setEnabled(false);
                return;

            }
            if (xStartPoint < 0 || xStartPoint >= selectedSequence.length()) {
                rightShiftButton.setEnabled(false);
                leftShiftButton.setEnabled(false);
                return;
            }
            if (xStartPoint >= 0 &&
                    xStartPoint < selectedSequence.length() - 2 * GAP) {
                rightShiftButton.setEnabled(true);
            } else {
                rightShiftButton.setEnabled(false);
            }
            if (xStartPoint > GAP) {
                leftShiftButton.setEnabled(true);
            } else {
                leftShiftButton.setEnabled(false);
            }
            final Font font = new Font("Courier", Font.BOLD, 10);
            int seqId = -1;
            int seqDx = -1;
            if (sequenceDB != null) {
                for (int i = 0; i < sequenceDB.size(); i++) {
                    DSSequence seq = sequenceDB.getSequence(i);
                    if (seq == selectedSequence) {
                        seqId = i;
                    }
                }
            }
            seqDx = xStartPoint;

            DSSequence sequence = selectedSequence;
            // Check if we are clicking on a new sequence
            if ((seqId >= 0) && (seqId != prevSeqId) || (seqDx != prevSeqDx)) {
                g.clearRect(0, 0, sequencedetailPanel.getWidth(),
                        sequencedetailPanel.getHeight());
                g.setFont(font);
                if (sequence != null && (seqDx >= 0) &&
                        (seqDx < sequence.length())) {
                    //turn anti-aliasing on
                    ((Graphics2D) g).setRenderingHint(RenderingHints.
                            KEY_ANTIALIASING,
                            RenderingHints.
                                    VALUE_ANTIALIAS_ON);
                    //shift the selected pattern/sequence into middle of the panel.
                    int startPoint = 0;
                    if (seqDx > GAP) {
                        startPoint = seqDx / 10 * 10 - GAP;
                    }
                    FontMetrics fm = g.getFontMetrics(font);

                    String seqAscii = sequence.getSequence().substring(
                            startPoint);
                    Rectangle2D r2d = fm.getStringBounds(seqAscii, g);
                    int seqLength = seqAscii.length();
                    double xscale = (r2d.getWidth() + 3) /
                            (double) (seqLength);
                    double yscale = 0.6 * r2d.getHeight();
                    g.drawString(seqAscii, 10, 20);
                    int paintPoint = 0;
                    while (paintPoint < seqLength) {
                        g.drawString("|",
                                10 + (int) (paintPoint * xscale),
                                (int) (GAP / 2 + yscale));
                        g.drawString(new Integer(paintPoint + 1 +
                                startPoint).toString(),
                                10 + (int) (paintPoint * xscale),
                                (int) (GAP / 2 + 2 * yscale));
                        paintPoint += GAP;
                    }

                    if (patternLocationsMatches != null) {
                        PatternSequenceDisplayUtil psd =
                                patternLocationsMatches.
                                        get(sequence);
                        if (psd == null) {
                            return;
                        }
                        TreeSet<PatternLocations>
                                patternsPerSequence = psd.getTreeSet();
                        if (patternsPerSequence != null &&
                                patternsPerSequence.size() > 0) {
                            for (PatternLocations pl : patternsPerSequence) {
                                CSSeqRegistration registration = pl.
                                        getRegistration();
                                if (registration != null) {
                                    Rectangle2D r = fm.getStringBounds(seqAscii,
                                            g);
                                    double scale = (r.getWidth() + 3) /
                                            (double) (seqAscii.length());
                                    CSSeqRegistration seqReg = (
                                            CSSeqRegistration) registration;
                                    int patLength = pl.getAscii().length();
                                    int dx = seqReg.x1;
                                    double x1 = (dx - startPoint) * scale +
                                            10;
                                    double x2 = ((double) patLength) *
                                            scale;
                                    if (pl.getPatternType().equals(
                                            PatternLocations.TFTYPE)) {
                                        x2 = Math.abs(registration.x2 - registration.x1) * scale;
                                    }
                                    g.setColor(PatternOperations.
                                            getPatternColor(new Integer(pl.
                                            getIdForDisplay())));
                                    g.drawRect((int) x1, 2, (int) x2, 23);
                                    g.drawString("|",
                                            (int) x1,
                                            (int) (GAP / 2 + yscale));
                                    g.drawString("|",
                                            (int) (x1 + x2 - scale),
                                            (int) (GAP / 2 + yscale));
                                    g.drawString(new Integer(dx + 1).
                                            toString(),
                                            (int) x1,
                                            (int) (GAP / 2 + 2 * yscale));
                                    g.drawString(new Integer(dx +
                                            seqReg.length() + 1).toString(),
                                            (int) (x1 + x2 - scale),
                                            (int) (GAP / 2 + 2 * yscale));
                                    if (pl.getPatternType().equals(
                                            PatternLocations.
                                                    TFTYPE)) {

                                        g.setColor(Color.RED);

                                        int shape = 3;
                                        int[] xi = new int[shape];
                                        int[] yi = new int[shape];
                                        int triangleSize = 8;
                                        if (registration.strand == 0) {
                                            xi[0] = xi[1] = (int) x1;
                                            yi[0] = 2;
                                            yi[1] = 2 + triangleSize;
                                            xi[2] = xi[0] + triangleSize / 2;
                                            yi[2] = 2 + triangleSize / 2;
                                            // g.drawPolyline(xi, yi, addtionalPoint);
                                        } else {
                                            xi[0] = xi[1] = (int) (x1 + x2);
                                            yi[0] = 2;
                                            yi[1] = 2 + triangleSize;
                                            xi[2] = xi[0] - triangleSize / 2;
                                            yi[2] = 2 + triangleSize / 2;

                                        }

                                        g.drawPolygon(xi, yi, shape);
                                        g.fillPolygon(xi, yi, shape);

                                    }

                                }

                            }
                        }
                    }

                }

            }

        }
    }

}
