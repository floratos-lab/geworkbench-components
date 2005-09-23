package org.geworkbench.components.normalization;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Serializable;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Columbia University</p>
 * @author non attributable
 */

/**
 * Parameters panels used by the <code>QuantileNormalizer</code>.
 */
public class QuantileNormalizerPanel extends AbstractSaveableParameterPanel implements Serializable {
    final String MARKER_OPTION = "Mean profile marker";
    final String MICROARRAY_OPTION = "Mean microarray value";
    private GridLayout gridLayout1 = new GridLayout();
    private JLabel averagingTypeLabel = new JLabel("Averaging method");
    private JComboBox averagingTypeSelection = new JComboBox(new String[]{MARKER_OPTION, MICROARRAY_OPTION});

    public QuantileNormalizerPanel() {
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
        container.add(averagingTypeLabel);
        container.add(averagingTypeSelection);
        container.setPreferredSize(new Dimension(280, 27));
        this.add(container);
    }

    /**
     * Return the user-specified parameter that designates if a missing value for
     * a marker X within a microarray Y will be replaced by the mean value of the
     * marker X across all micorarrays in the set, or with the mean value of all
     * markers within Y.
     *
     * @return <code>MARKER_PROFILE_MEAN</code> or <code>MICROARRAY_MEAN</code>.
     */
    public int getAveragingType() {
        if (averagingTypeSelection.getSelectedItem().equals(MARKER_OPTION))
            return MissingValueNormalizer.MARKER_PROFILE_MEAN;
        else
            return MissingValueNormalizer.MICROARRAY_MEAN;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        revalidate();
    }

}

