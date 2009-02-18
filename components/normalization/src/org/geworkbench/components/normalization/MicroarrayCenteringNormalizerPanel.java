package org.geworkbench.components.normalization;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.Serializable;
import java.util.Map;

import javax.naming.OperationNotSupportedException;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust
 * @version 1.0
 */

/**
 * The parameters panel used by the <code>MicroarraysCenteringNormalizer</code>.
 */
public class MicroarrayCenteringNormalizerPanel extends AbstractSaveableParameterPanel {
	private Log log = LogFactory.getLog(this.getClass());
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

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
	public Map<Serializable, Serializable> getParameters() {
		// TODO Auto-generated method stub
		log.error(new OperationNotSupportedException("Please implement getParameters()"));		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 */
	public void setParameters(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		log.error(new OperationNotSupportedException("Please implement setParameters()"));
	}

}

