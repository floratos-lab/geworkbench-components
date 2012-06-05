package org.geworkbench.components.sequenceretriever;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;

import javax.swing.JCheckBox;

import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.util.sequences.SequenceViewWidget;

/**
 * The main GUI class for sequence display panel.
 * 
 * This class is similar to SequenceViewWidget expect that it support a new view (RetrievedSequenceDisplayPanel).
 * That new view takes effect only if it is "line view" AND it is not showing individual sequence detail.
 *  
 * @author xiaoqing
 * @version $Id$
 */
public final class RetrievedSequencesPanel extends SequenceViewWidget {
	private static final long serialVersionUID = 430612435863058186L;
	
    private RetrievedSequenceDisplayPanel sequenceRetrieverNewLineView = new
    	RetrievedSequenceDisplayPanel();

    private DSSequence selectedSequence = null;

	private JCheckBox checkBoxHideDuplicate = new JCheckBox("Show only unique transcript-start sites", true);

    public RetrievedSequencesPanel() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
    	jToolBar1.add(checkBoxHideDuplicate );
    	checkBoxHideDuplicate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sequenceRetrieverNewLineView.refreshSequenceNameList(checkBoxHideDuplicate.isSelected());
			}
    		
    	});
        
        sequenceRetrieverNewLineView.setRetrievedSequencesPanel(this);

        seqViewWPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                seqViewWPanel.this_mouseClicked(e);
                if (e.getClickCount() == 2) {
                    if (isLineView) {
                        seqScrollPane.setViewportView(sequenceRetrieverNewLineView);
                        revalidate();
                        repaint();
                    }
                }
                xStartPoint = seqViewWPanel.getSeqXclickPoint();
                selectedSequence = seqViewWPanel.getSelectedSequence();
                sequencedetailPanel.repaint();
            }
        });

        sequenceRetrieverNewLineView.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                sequenceRetrieverNewLineView.this_mouseMoved(e);
            }
        });

		showAllBtn.setVisible(false);
		jAllSequenceCheckBox.setVisible(false);

        if (sequenceDB != null) {
            sequenceRetrieverNewLineView.setMaxSeqLen(sequenceDB.getMaxLength());
        }
        //  seqViewWPanel.initialize(selectedPatterns, sequenceDB);
        seqScrollPane.setViewportView(sequenceRetrieverNewLineView);
    }

    void setRetrievedMap(HashMap<String, RetrievedSequenceView> retrievedMap) {
        sequenceRetrieverNewLineView.setRetrievedMap(retrievedMap);
    }

    void setSelectedSequence(DSSequence selectedSequence) {
        this.selectedSequence = selectedSequence;
    }

    @SuppressWarnings("unchecked")
	void setDisplaySequenceDB(DSSequenceSet<?> displaySequenceDB) {
        this.displaySequenceDB = (DSSequenceSet<DSSequence>) displaySequenceDB;

        activeSequenceDB = (CSSequenceSet<DSSequence>) displaySequenceDB;
        if (activeSequenceDB != null) {
            sequenceDB = (DSSequenceSet<DSSequence>) activeSequenceDB;
            initPanelView();
        }
    }

	void switchToBaseView() {
		seqScrollPane.setViewportView(seqViewWPanel);
		seqViewWPanel.setSelectedSequence(sequenceRetrieverNewLineView
				.getSelectedSequence());
		seqViewWPanel.setSequenceDB(sequenceDB);
		seqViewWPanel.setSingleSequenceView(true);
		seqViewWPanel.setLineView(true);
		seqViewWPanel.repaint();
		revalidate();
		repaint();
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

        seqViewWPanel.removeAll();
        seqViewWPanel.setSequenceDB(null);
        seqViewWPanel.setSelectedSequence(null);
        seqViewWPanel.setSeqXclickPoint(-1);
        
        updateBottomPanel();
        revalidate();
        repaint();
    }

    // only called from RetrievedSequenceDisplayPanel
    void updateDetailPanel(int newPosition) {
        xStartPoint = newPosition;
        sequencedetailPanel.repaint();
    }

    /**
     * Initiate the Panel, which should be used as the entry point.
     *
     * @return boolean
     */
    @Override
    public void initPanelView() {
        //updatePatternSeqMatches();
        isLineView = jViewComboBox.getSelectedItem().equals(LINEVIEW);
        if (isLineView) {
            sequenceRetrieverNewLineView.initializeLineView(sequenceDB, checkBoxHideDuplicate.isSelected());
            seqScrollPane.setViewportView(sequenceRetrieverNewLineView);
        } else {
            seqScrollPane.setViewportView(seqViewWPanel);
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

	@Override
	protected DSSequence getSelectedSequence() {
		return selectedSequence;
	}

	boolean isDNA = true;
	
	public boolean isDNA() {
		return isDNA;
	}

	public void setDNA(boolean isDNA) {
		this.isDNA = isDNA;
	}
}
