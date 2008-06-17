package org.geworkbench.components.analysis.clustering;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust
 * @version 1.0
 */

/**
 * Parameters panel used by the <code>ThresholdNormalizer</code>.
 */
public class MultiTTestAnalysisPanel extends AbstractSaveableParameterPanel implements Serializable {

    private static final double DEFAULT_P_VALUE = 0.01;

    private JFormattedTextField pValue = new JFormattedTextField();

    private DSMicroarraySet maSet = null;

    private ArrayList<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();

    JCheckBox logCheckbox;    
    private boolean useroverride = false;
    
    
    private static class SerializedInstance implements Serializable {

        private Number pValue;

        public SerializedInstance(Number pValue) {
            this.pValue = pValue;
        }

        Object readResolve() throws ObjectStreamException {
            MultiTTestAnalysisPanel panel = new MultiTTestAnalysisPanel();
            panel.pValue.setValue(pValue);
            return panel;
        }
    }

    Object writeReplace() throws ObjectStreamException {
        return new SerializedInstance((Number) pValue.getValue());
    }

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
        
        logCheckbox = logCheckbox = new JCheckBox("Analyzed data was log2-transformed", false);
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
            DSAnnotationContext context = manager.getCurrentContext(maSet);
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

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        revalidate();
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
    
    

}

