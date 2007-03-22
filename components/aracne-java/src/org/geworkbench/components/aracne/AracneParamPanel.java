package org.geworkbench.components.aracne;

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
public class AracneParamPanel extends AbstractSaveableParameterPanel implements Serializable {

    public static final String HUB_ALL = "All vs. All";
    public static final String HUB_LIST = "List";
    public static final String THRESHOLD_MI = "Mutual Info.";
    public static final String THRESHOLD_PVALUE = "P-Value";
    public static final String KERNEL_INFERRED = "Inferred";
    public static final String KERNEL_SPECIFY = "Specify";
    public static final String DPI_NONE = "Do Not Apply";
    public static final String DPI_APPLY = "Apply";

    private JButton loadResultsButton = new JButton("Load...");
    private String candidateModulatorsFile = new String("data/mindy/candidateModulators.txt");

    private JComboBox hubCombo = new JComboBox(new String[]{HUB_ALL, HUB_LIST});
    private JComboBox thresholdCombo = new JComboBox(new String[]{THRESHOLD_MI, THRESHOLD_PVALUE});
    private JComboBox kernelCombo = new JComboBox(new String[]{KERNEL_INFERRED, KERNEL_SPECIFY});
    private JComboBox dpiCombo = new JComboBox(new String[]{DPI_NONE, DPI_APPLY});
    private JButton loadMarkersButton = new JButton("Load Markers");
    private JTextField hubMarkerList = new JTextField();
    private JTextField kernelWidth = new JTextField("-1");
    private JTextField threshold = new JTextField("-1");
    private JTextField dpiTolerance = new JTextField("0.1");
    private JCheckBox targetCheckbox = new JCheckBox();
    private JTextField targetList = new JTextField();
    private JButton loadTargetsButton = new JButton("Load Targets");

    public AracneParamPanel() {
        this.setLayout(new BorderLayout());
        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 3dlu, 60dlu, 3dlu, 50dlu, 3dlu, 40dlu, 7dlu",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator("ARACNE Paramaters");

        builder.append("Hub Marker(s)", hubCombo);
        builder.append(hubMarkerList, loadMarkersButton);

        builder.append("Threshold Type", thresholdCombo, threshold);
        builder.nextRow();

        builder.append("Kernel Width", kernelCombo, kernelWidth);
        builder.nextRow();

        builder.append("DPI Tolerance", dpiCombo, dpiTolerance);
        builder.nextRow();

        builder.append("DPI Target List", targetCheckbox);
        builder.append(targetList, loadTargetsButton);

//        builder.append("Full Set Kernel Width", fullsetKernelWidth);
//        builder.append("Full Set Mi Threshold", fullsetMIThreshold);
//        builder.append("Candidate Modulators", loadResultsButton);
//        builder.append("Transcription Factor", transcriptionFactor);
//        builder.nextRow();
        this.add(builder.getPanel());
        loadResultsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = new File(candidateModulatorsFile);
                JFileChooser chooser = new JFileChooser(file.getParentFile());
                chooser.showOpenDialog(AracneParamPanel.this);
                candidateModulatorsFile = chooser.getSelectedFile().getPath();
            }
        });
    }

    public String getCandidateModulatorsFile() {
        return candidateModulatorsFile;
    }

    public float getKernelWidth() {
        return Float.valueOf(kernelWidth.getText());
    }

    public float getThreshold() {
        return Float.valueOf(threshold.getText());
    }

    public float getDPITolerance() {
        return Float.valueOf(dpiTolerance.getText());
    }
}
