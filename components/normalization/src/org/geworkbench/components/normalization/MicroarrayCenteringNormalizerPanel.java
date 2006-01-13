package org.geworkbench.components.normalization;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust
 * @version 1.0
 */

/**
 * The parameters panel used by the <code>MicroarraysCenteringNormalizer</code>.
 */
public class MicroarrayCenteringNormalizerPanel extends AbstractSaveableParameterPanel implements Serializable {
    final String MEAN_OPTION = "Mean";
    final String MEDIAN_OPTION = "Median";
    final String MIN_OPTION = "Min profile";
    final String MAX_OPTION = "Max profile";
    final String ZERO_OPTION = "Zero";
    final String IGNORE_OPTION = "Ignore";
    private GridLayout gridLayout1 = new GridLayout();
    private JLabel averagingLabel = new JLabel("Averaging method");
    private JLabel missingValuesTreatmentLabel = new JLabel("Missing values");
    /**
     * Available options for computing the center point to be used.
     */
    private JComboBox averagingSelection = new JComboBox(new String[]{MEAN_OPTION, MEDIAN_OPTION});
    private JComboBox missingValuesSelection = new JComboBox(new String[]{MIN_OPTION, MAX_OPTION, ZERO_OPTION, IGNORE_OPTION});

    private static class SerializedInstance implements Serializable {
        int averaging;
        int missing;

        public SerializedInstance(int averaging, int missing) {
            this.averaging = averaging;
            this.missing = missing;
        }

        Object readResolve() throws ObjectStreamException {
            MicroarrayCenteringNormalizerPanel panel = new MicroarrayCenteringNormalizerPanel();
            panel.averagingSelection.setSelectedIndex(averaging);
            panel.missingValuesSelection.setSelectedIndex(missing);
            return panel;
        }
    }

    Object writeReplace() throws ObjectStreamException {
        return new SerializedInstance(averagingSelection.getSelectedIndex(), missingValuesSelection.getSelectedIndex());
    }
    
    public MicroarrayCenteringNormalizerPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        JPanel container = new JPanel();
        this.setLayout(new FlowLayout());
        gridLayout1.setColumns(2);
        gridLayout1.setHgap(10);
        gridLayout1.setRows(2);
        gridLayout1.setVgap(10);
        container.setLayout(gridLayout1);
        container.add(averagingLabel);
        container.add(averagingSelection);
        container.add(missingValuesTreatmentLabel);
        container.add(missingValuesSelection);
        container.setPreferredSize(new Dimension(250, 55));
        this.add(container);
    }

    /**
     * Gets the user-specified preference for the averaging method to use (mean
     * or median).
     *
     * @return <code>MicroarrayCenteringNormalizer.MEAN</code> or
     *         <code>MicroarrayCenteringNormalizer.MEDIAN</code>.
     */
    public int getAveragingSelection() {
        if (averagingSelection.getSelectedItem().equals(MEAN_OPTION))
            return MarkerCenteringNormalizer.MEAN;
        else
            return MarkerCenteringNormalizer.MEDIAN;
    }

    /**
     * The user-specified parameter that designates how the missing values
     * should be treated by this normalizer (should they be ignored or set to
     * the prescribed value).
     *
     * @return <code>MicroarrayCenteringNormalizer.MINIMUM</code>,
     *         <code>MicroarrayCenteringNormalizer.MAXIMUM</code>,
     *         <code>MicroarrayCenteringNormalizer.ZERO</code> or
     *         <code>MicroarrayCenteringNormalizer.IGNORE</code>.
     */
    public int getMissingValueTreatment() {
        if (missingValuesSelection.getSelectedItem().equals(MIN_OPTION))
            return MarkerCenteringNormalizer.MINIMUM;
        else if (missingValuesSelection.getSelectedItem().equals(MAX_OPTION))
            return MarkerCenteringNormalizer.MAXIMUM;
        else if (missingValuesSelection.getSelectedItem().equals(ZERO_OPTION))
            return MarkerCenteringNormalizer.ZERO;
        else
            return MarkerCenteringNormalizer.IGNORE;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        revalidate();
    }

}

