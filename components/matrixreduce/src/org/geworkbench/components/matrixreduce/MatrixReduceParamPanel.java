package org.geworkbench.components.matrixreduce;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;

import javax.swing.*;

import java.io.Serializable;
import java.io.File;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
/**
 * @author John Watkinson
 *
 * todo - make serializable work
 */
public class MatrixReduceParamPanel extends AbstractSaveableParameterPanel implements Serializable {


    /* Example run
MatrixREDUCE -sequence=../sequences/Y5_600_Bst.fa \
	-output=May31 \
	-p_value=0.001000 \
	-dyad_length=3 \
	-min_counts=5 \
	-min_gap=0 \
	-max_gap=4 \
	-flank=0 \
	-max_motif=20 \
	-max_iteration=1000000 \
	-num_print=50 \
	-expression=../data/Spellman1998Alpha
	
    */
    private JFormattedTextField dyadLength = new JFormattedTextField(3);
    private JFormattedTextField pValue = new JFormattedTextField(0.001);
    private JFormattedTextField minCounts = new JFormattedTextField(5);
    private JFormattedTextField flank = new JFormattedTextField(0);
    private JFormattedTextField minGap = new JFormattedTextField(0);
    private JFormattedTextField maxGap = new JFormattedTextField(0);
    private JFormattedTextField numPrint = new JFormattedTextField(50);
    private JCheckBox singleStrand = new JCheckBox();
    private JFormattedTextField maxMotif = new JFormattedTextField(10);
    private JFormattedTextField maxIteration = new JFormattedTextField(10);
    private JButton sequenceButton = new JButton("Load...");
    private String sequenceFile = new String("data/Y5_600_Bst.fa");
	private JLabel filename = new JLabel();

    public MatrixReduceParamPanel() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.gridx =0; c.gridy=0;
    	filename.setText(sequenceFile);
    	
        FormLayout layout0 = new FormLayout(
      		"right:max(40dlu;pref), 3dlu, right:max(40dlu;pref)",
        	"");
        DefaultFormBuilder builder0 = new DefaultFormBuilder(layout0);
      	builder0.setDefaultDialogBorder();
      	builder0.appendSeparator("Sequence File");
      	builder0.append(sequenceButton);
      	builder0.append(filename);

      	this.add(builder0.getPanel(),c);
        
        FormLayout layout = new FormLayout(
        	"right:max(40dlu;pref), 3dlu, 40dlu, 7dlu, right:max(40dlu;pref), 3dlu, 40dlu, 7dlu, right:50dlu",
        	"");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator("Parameters");
        builder.append("Dyad Length", dyadLength);
        builder.append("P Value", pValue);
        builder.nextRow();
        builder.append("Min Counts", minCounts);
        builder.append("Flank", flank);
        builder.nextRow();
        builder.append("Min Gap", minGap);
        minGap.setInputVerifier(new minGapVerifier());
        builder.append("Max Gap", maxGap);
        maxGap.setInputVerifier(new minGapVerifier());
        builder.nextRow();
        //builder.append("Num Print", numPrint);
        //builder.append("Single Strand", singleStrand);
        //builder.nextRow();
        builder.append("Max Motif", maxMotif);
        builder.append("Max Iterations", maxIteration);
        c.weightx = 0.5;
        c.gridx =0; c.gridy=1;
 
        rangeVerifierInt rvMC = new rangeVerifierInt();
        rvMC.setrange(1,100000);
        minCounts.setInputVerifier(rvMC);
        rangeVerifierInt rvF = new rangeVerifierInt();
        rvF.setrange(0,10);
        flank.setInputVerifier(rvF);
        rangeVerifierInt rvMM = new rangeVerifierInt();
        rvMM.setrange(0,20);
        maxMotif.setInputVerifier(rvMM);
        rangeVerifierInt rvMI = new rangeVerifierInt();
        rvMI.setrange(0,6000);
        maxIteration.setInputVerifier(rvMI);
        rangeVerifier rvPV = new rangeVerifier();
        rvPV.setrange(0.0,1.0);
        pValue.setInputVerifier(rvPV);
        rangeVerifierInt rvDL = new rangeVerifierInt();
        rvDL.setrange(2,5);
        dyadLength.setInputVerifier(rvDL);

        
        this.add(builder.getPanel(),c);
        sequenceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = new File(sequenceFile);
                JFileChooser chooser = new JFileChooser(file.getParentFile());
                chooser.showOpenDialog(MatrixReduceParamPanel.this);
                sequenceFile = chooser.getSelectedFile().getPath();
            	filename.setText(sequenceFile);
            }
        });
    }
    
    
   public class rangeVerifier extends InputVerifier {
    	double min, max;
    	public void setrange(double mi, double ma){
    		min = mi;
    		max = ma;
    	}
        public boolean verify(JComponent input) {
            if (input instanceof JFormattedTextField) {
                JFormattedTextField ftf = (JFormattedTextField)input;
                javax.swing.JFormattedTextField.AbstractFormatter formatter = ftf.getFormatter();
                if (formatter != null) {
                    try {
                    	String str = ftf.getText();
                    	double val = Double.parseDouble(str);
                         if( min<= val && val <= max){
                        	 return true;
                         }
                         JOptionPane.showMessageDialog(minGap.getParent(), "value should be between " + Double.toString(min) 
                        		 + " and " + Double.toString(max));
                         return false;
                     } catch (java.lang.Exception pe) {
                    	 JOptionPane.showMessageDialog(minGap.getParent(), "values need to be numerical");
                    	 return false;
                    }
                 }
             }
             return true;
         }
         public boolean shouldYieldFocus(JComponent input) {
             return verify(input);
         }
     }
    public class rangeVerifierInt extends InputVerifier {
    	int min, max;
    	public void setrange(int mi, int ma){
    		min = mi;
    		max = ma;
    	}
        public boolean verify(JComponent input) {
            if (input instanceof JFormattedTextField) {
                JFormattedTextField ftf = (JFormattedTextField)input;
                javax.swing.JFormattedTextField.AbstractFormatter formatter = ftf.getFormatter();
                if (formatter != null) {
                    try {
                    	String str = ftf.getText();
                    	int val = Integer.parseInt(str);
                         if( min<= val && val <= max){
                        	 return true;
                         }
                         JOptionPane.showMessageDialog(minGap.getParent(), "value should be between " + Integer.toString(min) 
                        		 + " and " + Integer.toString(max));
                         return false;
                     } catch (java.lang.Exception pe) {
                    	 JOptionPane.showMessageDialog(minGap.getParent(), "values need to be integers");
                    	 return false;
                    }
                 }
             }
             return true;
         }
         public boolean shouldYieldFocus(JComponent input) {
             return verify(input);
         }
     }

    public class minGapVerifier extends InputVerifier {
        public boolean verify(JComponent input) {
            if (input instanceof JFormattedTextField) {
                JFormattedTextField ftf = (JFormattedTextField)input;
                javax.swing.JFormattedTextField.AbstractFormatter formatter = ftf.getFormatter();
                if (formatter != null) {
                    try {
                         if(Integer.parseInt( minGap.getText()) <= 
                        	 Integer.parseInt( maxGap.getText()) &&
                        	 Integer.parseInt( minGap.getText()) >=0 &&
                        	 Integer.parseInt( minGap.getText()) <=10 &&
                        	 Integer.parseInt( maxGap.getText()) >=0 &&
                        	 Integer.parseInt( maxGap.getText()) <=10){
                        	 return true;
                         }
                         JOptionPane.showMessageDialog(minGap.getParent(), "min Gap should be smaller than MaxGap \nand between 0 and 10");
                         return false;
                     } catch (java.lang.Exception pe) {
                    	 JOptionPane.showMessageDialog(minGap.getParent(), "values need to be integers");
                    	 return false;
                    }
                 }
             }
             return true;
         }
         public boolean shouldYieldFocus(JComponent input) {
             return verify(input);
         }
     }

    public int getDyadLength() {
        return ((Number) dyadLength.getValue()).intValue();
    }

    public double getPValue() {
        return ((Number) pValue.getValue()).doubleValue();
    }

    public int getMinCounts() {
        return ((Number) minCounts.getValue()).intValue();
    }

    public int getFlank() {
        return ((Number) flank.getValue()).intValue();
    }

    public int getMinGap() {
        return ((Number) minGap.getValue()).intValue();
    }

    public int getMaxGap() {
        return ((Number) maxGap.getValue()).intValue();
    }

    public int getNumPrint() {
        return ((Number) numPrint.getValue()).intValue();
    }

    public boolean isSingleStrand() {
        return singleStrand.isSelected();
    }

    public int getMaxMotif() {
        return ((Number) maxMotif.getValue()).intValue();
    }

    public int getMaxIteration() {
        return ((Number) maxIteration.getValue()).intValue();
    }

    public String getSequenceFile() {
        return sequenceFile;
    }

}
