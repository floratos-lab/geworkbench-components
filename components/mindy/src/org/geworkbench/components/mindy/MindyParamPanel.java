package org.geworkbench.components.mindy;

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
 * @author mhall
 */
public class MindyParamPanel extends AbstractSaveableParameterPanel implements Serializable {

    private JButton loadResultsButton = new JButton("Load...");
    private String candidateModulatorsFile = new String("data/mindy/candidateModulators.txt");

    private JTextField setFraction = new JTextField("35");
    private JTextField subsetKernelWidth = new JTextField("-1");
    private JTextField subsetMIThreshold = new JTextField("-1");
    private JTextField dpiTolerance = new JTextField("0.1");
    private JTextField fullsetKernelWidth = new JTextField("-1");
    private JTextField fullsetMIThreshold = new JTextField("-1");
    private JTextField transcriptionFactor = new JTextField("1973_s_at");

    public MindyParamPanel() {
        this.setLayout(new BorderLayout());
        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 3dlu, 40dlu, 7dlu, right:max(40dlu;pref), 3dlu, 40dlu, 7dlu",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator("MINDY Paramaters");
        builder.append("Set Fraction", setFraction);
        builder.append("Subset Kernel Width", subsetKernelWidth);
        builder.append("Subset MI Threshold", subsetMIThreshold);
        builder.append("DPI Tolerance", dpiTolerance);
        builder.append("Full Set Kernel Width", fullsetKernelWidth);
        builder.append("Full Set Mi Threshold", fullsetMIThreshold);
        builder.append("Candidate Modulators", loadResultsButton);
        builder.append("Transcription Factor", transcriptionFactor);
        builder.nextRow();
        this.add(builder.getPanel());
        loadResultsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = new File(candidateModulatorsFile);
                JFileChooser chooser = new JFileChooser(file.getParentFile());
                chooser.showOpenDialog(MindyParamPanel.this);
                candidateModulatorsFile = chooser.getSelectedFile().getPath();
            }
        });
    }

    public String getCandidateModulatorsFile() {
        return candidateModulatorsFile;
    }

    public int getSetFraction() {
        return Integer.valueOf(setFraction.getText());
    }

    public float getSubsetKernelWidth() {
        return Float.valueOf(subsetKernelWidth.getText());
    }

    public float getSubsetMIThreshold() {
        return Float.valueOf(subsetMIThreshold.getText());
    }

    public float getDPITolerance() {
        return Float.valueOf(dpiTolerance.getText());
    }

    public float getFullSetKernelWidth() {
        return Float.valueOf(fullsetKernelWidth.getText());
    }

    public float getFullSetMIThreshold() {
        return Float.valueOf(fullsetMIThreshold.getText());
    }

    public String getTranscriptionFactor() {
        return transcriptionFactor.getText();
    }
}
