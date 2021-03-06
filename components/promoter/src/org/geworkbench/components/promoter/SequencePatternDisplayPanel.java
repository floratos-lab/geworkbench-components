package org.geworkbench.components.promoter;

import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JMenuItem;

import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.DSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
import org.geworkbench.util.patterns.PatternLocations;
import org.geworkbench.util.patterns.PatternOperations;
import org.geworkbench.util.patterns.PatternSequenceDisplayUtil;
import org.geworkbench.util.sequences.SequenceViewWidget;

/**
 *
 * @author
 * @version $Id$
 */
public class SequencePatternDisplayPanel extends SequenceViewWidget {
	private static final long serialVersionUID = -1554529235054789483L;
	
    private boolean displayTF = true;
    private boolean displaySeqPattern = true;
	private DSSequenceSet<DSSequence> sequenceDB = null;
    @SuppressWarnings("rawtypes")
	private HashMap patternDisplay = new HashMap();

    private HashMap<CSSequence,
            PatternSequenceDisplayUtil> patternTFMatches = new HashMap<
            CSSequence,
            PatternSequenceDisplayUtil>();
    private HashMap<CSSequence,
            PatternSequenceDisplayUtil>
            patternSeqMatches = new HashMap<CSSequence,
                                PatternSequenceDisplayUtil>();

    public SequencePatternDisplayPanel() {
		showAllBtn.setVisible(false);
		jAllSequenceCheckBox.setVisible(false);
    }

    public void addToolBarButton(AbstractButton jbutton) {
        jToolBar1.add(jbutton);
        repaint();
    }

    public void addMenuItem(JMenuItem saveItem) {
        seqViewWPanel.addMenuItem(saveItem);
        repaint();
    }

    /**
     * Transform the patterns to patternsUtil class.
     * Child class should override this method.
     */
    @Override
    public void updatePatternSeqMatches() {
        patternSeqMatches = PatternOperations.processPatterns(selectedPatterns,
                sequenceDB);
        if (displayTF) {
            if (displaySeqPattern) {
                patternLocationsMatches = PatternOperations.merge(
                        patternSeqMatches, patternTFMatches);
            } else {
                patternLocationsMatches = patternTFMatches;
            }
        } else {
            if (displaySeqPattern) {
                patternLocationsMatches = patternSeqMatches;
            } else {
                patternLocationsMatches = null;
            }

        }
    }

    public void initialize(DSSequenceSet<DSSequence> seqDB) {
        super.setSequenceDB(seqDB);

        patternDisplay.clear();
        sequenceDB = seqDB;
        updateBottomPanel();
        repaint();
    }

    public void addAPattern(TranscriptionFactor pt,
                            List<DSPatternMatch<DSSequence, CSSeqRegistration>> matches) {
        if (patternTFMatches == null) {
            patternTFMatches = new HashMap<
                               CSSequence,
                               PatternSequenceDisplayUtil>();

        }
        addTFMatches(patternTFMatches, matches, pt);
        initPanelView();

    }

    private static void addTFMatches(
			HashMap<CSSequence, PatternSequenceDisplayUtil> existedPatterns,
			List<DSPatternMatch<DSSequence, CSSeqRegistration>> matches,
			TranscriptionFactor tf) {
		if (matches == null) {
			return;
		}
		if (existedPatterns == null) {
			existedPatterns = new HashMap<CSSequence, PatternSequenceDisplayUtil>();

		}

		for (DSPatternMatch<DSSequence, CSSeqRegistration> sp : matches) {
			DSSequence hitSeq = sp.getObject();
			CSSeqRegistration reg = sp.getRegistration();
			if (existedPatterns.containsKey(hitSeq)) {
				PatternSequenceDisplayUtil pu = (PatternSequenceDisplayUtil) existedPatterns
						.get(hitSeq);
				PatternLocations pl = new PatternLocations(tf.toString(), reg, PatternLocations.TFTYPE);
				pl.setIDForDisplay(tf.hashCode());
				pu.addPattern(pl);

			} else {
				PatternSequenceDisplayUtil pu = new PatternSequenceDisplayUtil(
						(CSSequence) hitSeq);
				PatternLocations pl = new PatternLocations(tf.toString(), reg, PatternLocations.TFTYPE);
				pl.setIDForDisplay(tf.hashCode());
				pu.addPattern(pl);
				existedPatterns.put((CSSequence) hitSeq, pu);
			}
		}

	}

    public HashMap<CSSequence, PatternSequenceDisplayUtil> getPatternTFMatches() {
        return patternTFMatches;
    }

    @SuppressWarnings("rawtypes")
    public void setPatternDisplay(HashMap patternDisplay) {
        this.patternDisplay = patternDisplay;
    }

    public void setDisplaySeqPattern(boolean displaySeqPattern) {
        this.displaySeqPattern = displaySeqPattern;
    }

    public void setDisplayTF(boolean displayTF) {
        this.displayTF = displayTF;
    }

    // TODO this needs further investigation on whether patternTFMatches has any effect or not
    /* this is always set null */
    public void setPatternTFMatches(Object patternTFMatches) {
        this.patternTFMatches = null;
    }
}
