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
    private JFormattedTextField flank = new JFormattedTextField(3);
    private JFormattedTextField minGap = new JFormattedTextField(0);
    private JFormattedTextField maxGap = new JFormattedTextField(20);
    private JFormattedTextField numPrint = new JFormattedTextField(50);
    private JCheckBox singleStrand = new JCheckBox();
    private JFormattedTextField maxMotif = new JFormattedTextField(20);
    private JFormattedTextField maxIteration = new JFormattedTextField(1000000);
    private JButton sequenceButton = new JButton("Load...");
    private String sequenceFile = new String("data/Y5_600_Bst.fa");

    public MatrixReduceParamPanel() {
        this.setLayout(new BorderLayout());
        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 3dlu, 40dlu, 7dlu, right:max(40dlu;pref), 3dlu, 40dlu, 7dlu",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator("Sequence File");
        builder.append(sequenceButton);
        builder.nextRow();
        builder.appendSeparator("Parameters");
        builder.append("Dyad Length", dyadLength);
        builder.append("P Value", pValue);
        builder.append("Min Counts", minCounts);
        builder.append("Flank", flank);
        builder.append("Min Gap", minGap);
        builder.append("Max Gap", maxGap);
        builder.append("Num Print", numPrint);
        builder.append("Single Strand", singleStrand);
        builder.append("Max Motif", maxMotif);
        builder.append("Max Iterations", maxIteration);
        this.add(builder.getPanel());
        sequenceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = new File(sequenceFile);
                JFileChooser chooser = new JFileChooser(file.getParentFile());
                chooser.showOpenDialog(MatrixReduceParamPanel.this);
                sequenceFile = chooser.getSelectedFile().getPath();
            }
        });
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
