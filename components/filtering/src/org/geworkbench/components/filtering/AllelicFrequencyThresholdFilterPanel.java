/**
 * AllelicFrequencyThresholdFilterPanel.java
 */

package org.geworkbench.components.filtering;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.Serializable;
import java.util.Map;

import javax.naming.OperationNotSupportedException;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;

/**
 * @author manjunath at genomecenter dot columbia dot edu
 */
public class AllelicFrequencyThresholdFilterPanel extends AbstractSaveableParameterPanel {
	private Log log = LogFactory.getLog(this.getClass());
    private JLabel minPercentLabel = new JLabel("Minimum Allele Frequency (Percent): ");
    private JFormattedTextField minPercentValue = new JFormattedTextField();
    private GridLayout gridLayout1 = new GridLayout();

    public AllelicFrequencyThresholdFilterPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        this.setLayout(new FlowLayout());
        JPanel container = new JPanel();
        gridLayout1.setColumns(2);
        gridLayout1.setHgap(10);
        gridLayout1.setRows(3);
        gridLayout1.setVgap(10);
        container.setLayout(gridLayout1);
        container.add(minPercentLabel);
        container.add(minPercentValue);
        this.add(container);
        minPercentValue.setValue(new Double(0.0));
        minPercentValue.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
    }

    /**
     * Get the user-specifed lower bound for the expression value of a marker so
     * that the marker does not get filtered out.
     *
     * @return
     */
    public double getLowerFilterBound() {
        return ((Number) minPercentValue.getValue()).doubleValue();
    }

    /**
     * Overrides the method from <code>AbstractSaveableParameterPanel</code>.
     * Provides an error message if the user provided parameter values are
     * outside their permitted ranges.
     *
     * @return
     */
    public ParamValidationResults validateParameters() {
        if (getLowerFilterBound() < 0d || getLowerFilterBound() > 100d)
            return new ParamValidationResults(false, "Percent value should be between 0-100.");
        else
            return new ParamValidationResults(true, "No Error");
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

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}

}