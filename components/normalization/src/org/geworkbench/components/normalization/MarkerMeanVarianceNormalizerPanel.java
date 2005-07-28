package org.geworkbench.components.normalization;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Serializable;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust
 * @version 1.0
 */

/**
 * Parameters panel for the <code>MarkerMeanVarianceNormalizer</code>..
 */
public class MarkerMeanVarianceNormalizerPanel extends AbstractSaveableParameterPanel implements Serializable {
    final String MIN_OPTION = "Min profile";
    final String MAX_OPTION = "Max profile";
    final String ZERO_OPTION = "Zero";
    final String IGNORE_OPTION = "Ignore";
    private GridLayout gridLayout1 = new GridLayout();
    private JLabel missingValuesTreatmentLabel = new JLabel("Missing values");
    /**
     * Available options for computing the center to be
     */
    private JComboBox missingValuesSelection = new JComboBox(new String[]{MIN_OPTION, MAX_OPTION, ZERO_OPTION, IGNORE_OPTION});

    public MarkerMeanVarianceNormalizerPanel() {
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
        gridLayout1.setRows(1);
        gridLayout1.setVgap(10);
        container.setLayout(gridLayout1);
        container.add(missingValuesTreatmentLabel);
        container.add(missingValuesSelection);
        container.setPreferredSize(new Dimension(250, 27));
        this.add(container);
    }

    /**
     * The user-specified parameter that designates how the missing values
     * should be treated by this normalizer (should they be ignored or set to
     * the prescribed value).
     *
     * @return <code>MarkerMeanVarianceNormalizer.MINIMUM</code>,
     *         <code>MarkerMeanVarianceNormalizer.MAXIMUM</code>,
     *         <code>MarkerMeanVarianceNormalizer.ZERO</code> or
     *         <code>MarkerMeanVarianceNormalizer.IGNORE</code>.
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

