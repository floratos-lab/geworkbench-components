package org.geworkbench.components.filtering;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
 * Pararameters panel for the <code>AffyDetectionCallFilter</code>. Prompts
 * the user to designate which markers (those whose detection call is "Present",
 * "Absent" or "Marginal") should be filtered out.
 */
public class AffyDetectionCallFilterPanel extends AbstractSaveableParameterPanel implements Serializable, ItemListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = -1144320550372709784L;
	
	final String PRESENT_OPTION = "P";
    final String ABSENT_OPTION = "A";
    final String MARGINAL_OPTION = "M";
    private GridLayout gridLayout1 = new GridLayout();
    private JLabel callSelectionLabel = new JLabel("<html><p>Detection calls to</p><p>be filtered out.</p></html>");
    private JCheckBox presentButton = new JCheckBox(PRESENT_OPTION);
    private JCheckBox absentButton = new JCheckBox(ABSENT_OPTION);
    private JCheckBox marginalButton = new JCheckBox(MARGINAL_OPTION);
    private boolean presentButtonStatus;
    private boolean absentButtonStatus;
    private boolean marginalButtonStatus;

    private static class SerializedInstance implements Serializable {
        /**
		 * 
		 */
		private static final long serialVersionUID = 108562498655239047L;
		boolean present;
        boolean absent;
        boolean marginal;

        public SerializedInstance(boolean present, boolean absent, boolean marginal) {
            this.present = present;
            this.absent = absent;
            this.marginal = marginal;
        }

        Object readResolve() throws ObjectStreamException {
            AffyDetectionCallFilterPanel panel = new AffyDetectionCallFilterPanel();
            panel.presentButton.setSelected(present);
            panel.absentButton.setSelected(absent);
            panel.marginalButton.setSelected(marginal);
            return panel;
        }
    }

    Object writeReplace() throws ObjectStreamException {
        return new SerializedInstance(presentButton.isSelected(), absentButton.isSelected(), marginalButton.isSelected());
    }


    public AffyDetectionCallFilterPanel() {
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
        gridLayout1.setRows(1);
        gridLayout1.setVgap(10);
        container.setLayout(gridLayout1);
        // Initialize the selection status for the check box buttons.
        presentButton.setSelected(false);
        presentButtonStatus = false;
        absentButton.setSelected(false);
        absentButtonStatus = false;
        marginalButton.setSelected(false);
        marginalButtonStatus = false;
        //Put the check boxes in a column in a panel
        JPanel buttonContainer = new JPanel(new GridLayout(0, 1));
        buttonContainer.add(presentButton);
        buttonContainer.add(marginalButton);
        buttonContainer.add(absentButton);
        // Set the button item selection listener.
        presentButton.addItemListener(this);
        absentButton.addItemListener(this);
        marginalButton.addItemListener(this);
        container.add(callSelectionLabel);
        container.add(buttonContainer);
        container.setPreferredSize(new Dimension(250, 55));
        this.add(container);
    }

    /**
     * Check if the "Present" option is selected.
     */
    public boolean isPresentSelected() {
        return presentButtonStatus;
    }

    /**
     * Check if the "Absent" option is selected.
     */
    public boolean isAbsentSelected() {
        return absentButtonStatus;
    }

    /**
     * Check if the "Marginal" option is selected.
     */
    public boolean isMarginalSelected() {
        return marginalButtonStatus;
    }

    /**
     * Listens to the check boxes.
     */
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        if (source == presentButton)
            presentButtonStatus = !presentButtonStatus;
        else if (source == absentButton)
            absentButtonStatus = !absentButtonStatus;
        else if (source == marginalButton)
            marginalButtonStatus = !marginalButtonStatus;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        revalidate();
    }
    
    @Override
    public String toString(){
    	return 
    	"present: "+isPresentSelected()+"\n"+
        "marginal: "+isMarginalSelected()+"\n"+
        "absent: "+isAbsentSelected()+"\n";
    }
}

