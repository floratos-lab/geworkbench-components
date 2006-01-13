package org.geworkbench.components.normalization;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
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
 * Parameters panel for the <code>MarkerCenteringNormalizer</code>.
 */
public class MarkerCenteringNormalizerPanel extends AbstractSaveableParameterPanel implements Serializable {
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
     * Available options for computing the center to be
     */
    private JComboBox averagingSelection = new JComboBox(new String[]{MEAN_OPTION, MEDIAN_OPTION});
    private JComboBox missingValuesSelection = new JComboBox(new String[]{MIN_OPTION, MAX_OPTION, ZERO_OPTION, IGNORE_OPTION});

    private static class SerialInstance implements Serializable {
        int averaging;
        int missing;

        public SerialInstance(int averaging, int missing) {
            this.averaging = averaging;
            this.missing = missing;
        }

        Object readResolve() throws ObjectStreamException {
            MarkerCenteringNormalizerPanel panel = new MarkerCenteringNormalizerPanel();
            panel.averagingSelection.setSelectedIndex(averaging);
            panel.missingValuesSelection.setSelectedIndex(missing);
            return panel;
        }
    }

    Object writeReplace() throws ObjectStreamException {
        return new SerialInstance(averagingSelection.getSelectedIndex(), missingValuesSelection.getSelectedIndex());
    }

    public MarkerCenteringNormalizerPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 8dlu, max(60dlu;pref)",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Centering Parameters");

        builder.append("Averaging method", averagingSelection);
        builder.append("Missing values", missingValuesSelection);
        this.add(builder.getPanel(), BorderLayout.CENTER);
    }

    /**
     * Gets the user-specified preference for the averaging method to use (mean
     * or median).
     *
     * @return <code>MarkerCenteringNormalizer.MEAN</code> or
     *         <code>MarkerCenteringNormalizer.MEDIAN</code>.
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
     * @return <code>MarkerCenteringNormalizer.MINIMUM</code>,
     *         <code>MarkerCenteringNormalizer.MAXIMUM</code>,
     *         <code>MarkerCenteringNormalizer.ZERO</code> or
     *         <code>MarkerCenteringNormalizer.IGNORE</code>.
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

