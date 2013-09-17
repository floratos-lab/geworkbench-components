package org.geworkbench.components.ttest;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.OperationNotSupportedException;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.analysis.ParameterPanelIncludingNormalized;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust
 * @version $Id$
 */

/**
 * Parameters panel used by the <code>ThresholdNormalizer</code>.
 */
public class MultiTTestAnalysisPanel extends AbstractSaveableParameterPanel implements ParameterPanelIncludingNormalized {
	private static final long serialVersionUID = -4829462519463439143L;

	private Log log = LogFactory.getLog(this.getClass());

    private static final double DEFAULT_P_VALUE = 0.01;

    private JFormattedTextField pValue = new JFormattedTextField();

    private DSMicroarraySet maSet = null;

    private ArrayList<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();

    JCheckBox logCheckbox;    
    private boolean useroverride = false;
    
    public MultiTTestAnalysisPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        pValue = new JFormattedTextField(DEFAULT_P_VALUE);
        setLayout(new BorderLayout());
        
        logCheckbox = new JCheckBox("Data is log2-transformed", false);
        logCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	useroverride = true;                              
                 
            }
        });
        
        rebuildForm();
    }

    public void rebuildForm() {
        removeAll();
        Set<String> selected = getLabels();
        checkBoxes.clear();
        FormLayout layout = new FormLayout(
                "right:max(80dlu;pref), 3dlu, max(70dlu;pref), 3dlu, max(70dlu;pref), 3dlu, max(70dlu;pref)",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Multi t Test Parameters");

        builder.append("Critical P-Value", pValue);
        builder.nextLine();       
        builder.append("", logCheckbox);
        
        builder.nextLine();       

        builder.appendSeparator("Compare Panels");

        if (maSet != null) {
            // Get existing selections, if any
            DSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();
            DSAnnotationContext<DSMicroarray> context = manager.getCurrentContext(maSet);
            int n = context.getNumberOfLabels();
            for (int i = 0; i < n; i++) {
                String label = context.getLabel(i);
                JCheckBox checkBox = new JCheckBox(label, false);
                if (selected.contains(label)) {
                    checkBox.setSelected(true);
                }
                if (i % 3 == 0) {
                    builder.append("");
                }
                builder.append(checkBox);
                checkBoxes.add(checkBox);
            }
        }
        add(builder.getPanel());
        invalidate();
    }

    /**
     * Get the cutoff threashold that will be used to bound the array values.
     *
     * @return The cutoff value.
     */
    public double getPValue() {
        return ((Number) pValue.getValue()).doubleValue();
    }

    public Set<String> getLabels() {
        HashSet<String> result = new HashSet<String>();
        for (int i = 0; i < checkBoxes.size(); i++) {
            JCheckBox checkBox = checkBoxes.get(i);
            if (checkBox.isSelected()) {
                result.add(checkBox.getText());
            }
        }
        return result;
    }

    public void setMaSet(DSMicroarraySet maSet) {
        this.maSet = maSet;
    }
    
    public boolean isUseroverride()
    {
    	return this.useroverride;
    }
    
    public boolean isLogNormalized() {         
    	return logCheckbox.isSelected();
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

