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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import org.geworkbench.util.Util;
import org.geworkbench.util.patterns.CSMatchedSeqPattern;
import org.geworkbench.util.patterns.PatternLocations;
import org.geworkbench.util.patterns.PatternOperations;
import org.geworkbench.util.patterns.PatternSequenceDisplayUtil;

/**
 * The main GUI class for sequence display panel.
 * 
 * This class is similar to SequenceViewWidget expect that it support a new view (RetrievedSequenceDisplayPanel).
 * That new view takes effect only if it is "line view" AND it is not showing individual sequence detail.
 *  
 * @author xiaoqing
 * @version $Id$
 */
public final class RetrievedSequencesPanel extends JPanel {
	private static final long serialVersionUID = 430612435863058186L;
	
    private HashMap<CSSequence, PatternSequenceDisplayUtil> patternLocationsMatches;
    private DSCollection<DSMatchedPattern<DSSequence, CSSeqRegistration>> selectedPatterns = new Collection<DSMatchedPattern<DSSequence, CSSeqRegistration>>();

    private JToolBar jToolBar1 = new JToolBar();

    private RetrievedSequenceDisplayPanel sequenceRetrieverNewLineView = new
    	RetrievedSequenceDisplayPanel();
    private SingleSequenceViewPanel seqViewWPanel = new SingleSequenceViewPanel();
    private CSSequenceSet<DSSequence> activeSequenceDB = null;
    
    private int prevSeqId = -1;
    private int prevSeqDx = 0;
    private DSSequenceSet<DSSequence> sequenceDB = new CSSequenceSet<DSSequence>();
    private DSSequenceSet<?> orgSequenceDB = new CSSequenceSet<CSSequence>();
    private DSSequenceSet<? extends DSSequence> displaySequenceDB = new CSSequenceSet<DSSequence>();

    private DSSequence selectedSequence = null;
    //Panels and Panes
    private JDetailPanel sequencedetailPanel = new JDetailPanel();
    private JPanel bottomPanel = new JPanel();
    private JButton leftShiftButton = new JButton();
    private JButton rightShiftButton = new JButton();
    private JScrollPane seqScrollPane = new JScrollPane();
    
    
    private JLabel jViewLabel = new JLabel();
    private JComboBox jViewComboBox = new JComboBox();
    private static final String LINEVIEW = "Line";
    private static final String FULLVIEW = "Full Sequence";

    private boolean isLineView = true; //true is for LineView.
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

    private void jbInit() throws Exception {

        this.setLayout(new BorderLayout());
        sequencedetailPanel.setBorder(BorderFactory.createEtchedBorder());
        sequencedetailPanel.setMinimumSize(new Dimension(50, 40));
        sequencedetailPanel.setPreferredSize(new Dimension(60, 50));

        sequenceRetrieverNewLineView.setRetrievedSequencesPanel(this);

        seqViewWPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                seqViewWPanel.this_mouseClicked(e);
                if (e.getClickCount() == 2) {
                    if (isLineView) {
                        flapToNewView(true);
                    }
                }
                xStartPoint = seqViewWPanel.getSeqXclickPoint();
                selectedSequence = seqViewWPanel.getSelectedSequence();
                sequencedetailPanel.repaint();
            }
        });
        seqViewWPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                seqViewWPanel.this_mouseMoved(e);
            }
        });
        this.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(InputMethodEvent e) {
            }

            public void caretPositionChanged(InputMethodEvent e) {
                showPatterns();
            }
        });
        this.addPropertyChangeListener(new java.beans.
                PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                showPatterns();
            }
        });
        jViewLabel.setText("View: ");

        sequenceRetrieverNewLineView.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                sequenceRetrieverNewLineView.this_mouseMoved(e);
            }
        });
        jViewComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                initPanelView();
            }
        });
        jViewComboBox.setToolTipText("Select a view to display results.");

        bottomPanel = new JPanel();

        ImageIcon leftButtonIcon = Util.createImageIcon("/images/back.gif");
        leftShiftButton.setIcon(leftButtonIcon);
        ImageIcon rightButtonIcon = Util.createImageIcon("/images/forward.gif");
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

        if (sequenceDB != null) {
            sequenceRetrieverNewLineView.setMaxSeqLen(sequenceDB.getMaxLength());
        }
        //  seqViewWPanel.initialize(selectedPatterns, sequenceDB);
        seqScrollPane.getViewport().add(sequenceRetrieverNewLineView, null);
    }

    private void setMoveDirection(String directionStr) {
        if (directionStr.equals(LEFT)) {
            goLeft = true;
        } else {
            goLeft = false;
        }
    }

    void setRetrievedMap(HashMap<String, RetrievedSequenceView> retrievedMap) {
        sequenceRetrieverNewLineView.setRetrievedMap(retrievedMap);
    }

    void setSelectedSequence(DSSequence selectedSequence) {
        this.selectedSequence = selectedSequence;
    }

    @SuppressWarnings("unchecked")
	void setDisplaySequenceDB(DSSequenceSet<? extends DSSequence> displaySequenceDB) {
        this.displaySequenceDB = displaySequenceDB;

        if (orgSequenceDB != null) {
            activeSequenceDB = (CSSequenceSet<DSSequence>) orgSequenceDB;
        }
        if (activeSequenceDB != null) {
            sequenceDB = (DSSequenceSet<DSSequence>) activeSequenceDB;
            initPanelView();
        }
    }

    void flapToNewView(boolean newView) {
        if (!newView) {
            seqScrollPane.getViewport().removeAll();
            seqScrollPane.getViewport().add(seqViewWPanel);
            seqViewWPanel.setSelectedSequence(sequenceRetrieverNewLineView.getSelectedSequence());
            seqViewWPanel.setSequenceDB(sequenceDB);
            seqViewWPanel.setSingleSequenceView(true);
            seqViewWPanel.setLineView(true);
            seqViewWPanel.repaint();
            revalidate();
            repaint();
        } else {
            seqScrollPane.getViewport().removeAll();
            seqScrollPane.getViewport().add(sequenceRetrieverNewLineView, null);
            revalidate();
            repaint();
        }

    }

    // only called from SequenceRetriever.updateDisplay
    void initialize(DSSequenceSet<DSSequence> seqDB) {
        orgSequenceDB = seqDB;
        sequenceDB = (DSSequenceSet<DSSequence>) seqDB;
        setDisplaySequenceDB(seqDB);

        if (sequenceDB != null) {
            sequenceRetrieverNewLineView.setMaxSeqLen(sequenceDB.getMaxLength());
            //      seqViewWPanel.initialize(null, db);
            selectedPatterns.clear();
            repaint();
        }

        updateBottomPanel();
        repaint();
    }

    // called from SequenceRetriver
    void initialize() {
        sequenceDB = null;
        sequenceRetrieverNewLineView.removeAll();
        selectedSequence = null;
        sequenceRetrieverNewLineView.setSelectedSequence(null);
        displaySequenceDB = new CSSequenceSet<DSSequence>();
        seqViewWPanel.initialize();
        updateBottomPanel();
        revalidate();
        repaint();
    }

    private void updateBottomPanel() {

    	if (selectedSequence == null) {
            return;
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
    }

    void updateDetailPanel(int newPosition) {
        xStartPoint = newPosition;
        sequencedetailPanel.repaint();
    }

	private void showPatterns() {
		for (DSMatchedPattern<DSSequence, CSSeqRegistration> pattern : selectedPatterns) {
			if (pattern instanceof DSMatchedSeqPattern) {
				if (((DSMatchedSeqPattern)pattern).getASCII() == null) {
					PatternOperations.fill((CSMatchedSeqPattern) pattern,
							displaySequenceDB);
				}
			}
		}
		repaint();
	}

    /**
     * Initiate the Panel, which should be used as the entry point.
     *
     * @return boolean
     */
    private void initPanelView() {
        //updatePatternSeqMatches();
        isLineView = jViewComboBox.getSelectedItem().equals(LINEVIEW);
        if (isLineView) {
            sequenceRetrieverNewLineView.initialize(sequenceDB, true);
            flapToNewView(true);
        } else {
            seqScrollPane.getViewport().removeAll();
            seqScrollPane.getViewport().add(seqViewWPanel);
            seqViewWPanel.setSelectedSequence(sequenceRetrieverNewLineView.getSelectedSequence());
            seqViewWPanel.setSequenceDB(sequenceDB);
            seqViewWPanel.setSingleSequenceView(true);
            seqViewWPanel.setLineView(isLineView);
            seqViewWPanel.repaint();
        }
        sequenceRetrieverNewLineView.revalidate();
        sequenceRetrieverNewLineView.repaint();
        this.revalidate();
        this.repaint();
    }

	/*
	 * This class is to implement the paint behavior. All the fields are from
	 * the enclosing class instance.
	 */
    private class JDetailPanel extends JPanel {
		private static final long serialVersionUID = -9171605516043986751L;

		@Override
		public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // DSSequence selectedSequence = seqViewWPanel.getSelectedSequence();
            if (selectedSequence == null) {
                g.clearRect(0, 0, getWidth(), getHeight());
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
                g.clearRect(0, 0, getWidth(),
                        getHeight());
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

					if (patternLocationsMatches == null)
						return;
					PatternSequenceDisplayUtil psd = patternLocationsMatches
							.get(sequence);
					if (psd == null)
						return;

					TreeSet<PatternLocations> patternsPerSequence = psd
							.getTreeSet();
					if (patternsPerSequence != null
							&& patternsPerSequence.size() > 0) {
						for (PatternLocations pl : patternsPerSequence) {
							CSSeqRegistration registration = pl
									.getRegistration();
							if (registration != null) {
								Rectangle2D r = fm.getStringBounds(seqAscii, g);
								double scale = (r.getWidth() + 3)
										/ (double) (seqAscii.length());
								CSSeqRegistration seqReg = (CSSeqRegistration) registration;
								int patLength = pl.getAscii().length();
								int dx = seqReg.x1;
								double x1 = (dx - startPoint) * scale + 10;
								double x2 = ((double) patLength) * scale;
								if (pl.getPatternType().equals(
										PatternLocations.TFTYPE)) {
									x2 = Math.abs(registration.x2
											- registration.x1)
											* scale;
								}
								g.setColor(PatternOperations
										.getPatternColor(new Integer(pl
												.getIdForDisplay())));
								g.drawRect((int) x1, 2, (int) x2, 23);
								g.drawString("|", (int) x1,
										(int) (GAP / 2 + yscale));
								g.drawString("|", (int) (x1 + x2 - scale),
										(int) (GAP / 2 + yscale));
								g.drawString(new Integer(dx + 1).toString(), (int) x1,
										(int) (GAP / 2 + 2 * yscale));
								g.drawString(new Integer(dx + seqReg.length()).toString(),
										(int) (x1 + x2 - scale), (int) (GAP / 2 + 2 * yscale));
								if (pl.getPatternType().equals(
										PatternLocations.TFTYPE)) {

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
