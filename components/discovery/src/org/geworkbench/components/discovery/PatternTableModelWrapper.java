package org.geworkbench.components.discovery;

import javax.swing.SwingUtilities;

import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern;
import org.geworkbench.events.ProgressChangeEvent;
import org.geworkbench.events.listeners.ProgressChangeListener;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: This class wraps a pattern table model.
 * It implements the GenericModel and is responsible for updating
 * the patternModel. It does not hold its own data.
 * NOTE: Although one can get a handle to the PatternTableModel, one should
 * not update it directly. </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version $Id$
 */

public class PatternTableModelWrapper implements ProgressChangeListener {
    //holds patterns of the table
    PatternTableModel patternModel = new PatternTableModel();

    public PatternTableModelWrapper() {
    }

    public void progressChanged(ProgressChangeEvent evt) {
        int found = evt.getPatternFound();
        if (found > 0) {
            setPatternNumber(found);
        }
    }

    /**
     * This methos sets the total number of pattern for the model.
     *
     * @param numOfPatterns
     */
    public void setPatternNumber(int numOfPatterns) {
        patternModel.setRowCount(numOfPatterns);
        firePatternDataModelChanged();
    }

    /**
     * Clear the model.
     */
    public void clear() {
        patternModel.clear();
        patternModel.setRowCount(0);
        firePatternDataModelChanged();
    }

    public boolean attach(org.geworkbench.util.patterns.DataSource data) {
    	
        if (data instanceof org.geworkbench.util.patterns.SequentialPatternSource) {
            clear();
            org.geworkbench.util.patterns.SequentialPatternSource source = (org.geworkbench.util.patterns.SequentialPatternSource) data;
            patternModel.setPatternSource(source);
            patternModel.setRowCount(source.getPatternSourceSize());
            firePatternDataModelChanged();
            return true;
        }
        return false;
    }

    /**
     * Notifies the table that the data has changed.
     */
    private void firePatternDataModelChanged() {
        //if we were called from the event dispatch thread, fire away...
        if (SwingUtilities.isEventDispatchThread()) {
            patternModel.fireTableDataChanged();
        } else {
            //not on the event dispatch thread - get on the EvtDdptchThrd
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    patternModel.fireTableDataChanged();
                }
            });
        }
    }

    public DSMatchedSeqPattern getPattern(int i) {
        return patternModel.getPattern(i);
    }

    /**
     * Returns the number of pattern in the model
     *
     * @return number of patterns.
     */
    public int size() {
        return patternModel.getRowCount();
    }

    /**
     * Return a model for the pattern table.
     *
     * @return
     */
    public PatternTableModel getPatternTableModel() {
        return patternModel;
    }

    /**
     * Sort the patterns in the model on field
     */
    public void sort(int field) {
        patternModel.sort(field);
        firePatternDataModelChanged();
    }

    /**
     * Mask the patterns of this model
     */
    public void mask(int[] index, boolean maskOp) {
        patternModel.mask(index, maskOp);
    }
}
