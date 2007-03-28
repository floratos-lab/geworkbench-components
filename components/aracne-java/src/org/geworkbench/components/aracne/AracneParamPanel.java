package org.geworkbench.components.aracne;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import wb.data.Marker;

/**
 * @author mhall
 */
public class AracneParamPanel extends AbstractSaveableParameterPanel implements Serializable {

    static Log log = LogFactory.getLog(AracneParamPanel.class);

    public static final String DEFAULT_HUB = "31564_at";

    public static final String HUB_ALL = "All vs. All";
    public static final String HUB_LIST = "List";
    public static final String THRESHOLD_MI = "Mutual Info.";
    public static final String THRESHOLD_PVALUE = "P-Value";
    public static final String KERNEL_INFERRED = "Inferred";
    public static final String KERNEL_SPECIFY = "Specify";
    public static final String DPI_NONE = "Do Not Apply";
    public static final String DPI_APPLY = "Apply";

    private JButton loadResultsButton = new JButton("Load...");
    private String hubMarkersFile = new String("data/test.txt");

    private JComboBox hubCombo = new JComboBox(new String[]{HUB_ALL, HUB_LIST});
    private JComboBox thresholdCombo = new JComboBox(new String[]{THRESHOLD_MI, THRESHOLD_PVALUE});
    private JComboBox kernelCombo = new JComboBox(new String[]{KERNEL_INFERRED, KERNEL_SPECIFY});
    private JComboBox dpiCombo = new JComboBox(new String[]{DPI_NONE, DPI_APPLY});
    private JButton loadMarkersButton = new JButton("Load Markers");
    private JTextField hubMarkerList = new JTextField(DEFAULT_HUB);
    private JTextField kernelWidth = new JTextField("0.1");
    private JTextField threshold = new JTextField("0.3");
    private JTextField dpiTolerance = new JTextField("0.1");
    private JCheckBox targetCheckbox = new JCheckBox();
    private JTextField targetList = new JTextField();
    private JButton loadTargetsButton = new JButton("Load Targets");

    private ArrayList<String> hubGenes = new ArrayList<String>();

    {
        // Default hub gene
        hubGenes.add(DEFAULT_HUB);
    }

    public AracneParamPanel() {
        this.setLayout(new BorderLayout());

        hubMarkerList.setEnabled(false);
        loadMarkersButton.setEnabled(false);
        kernelWidth.setEnabled(false);
        dpiTolerance.setEnabled(false);
        targetList.setEnabled(false);
        loadTargetsButton.setEnabled(false);

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

        hubCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String selectedItem = (String) cb.getSelectedItem();
                if (HUB_ALL.equals(selectedItem)) {
                    hubMarkerList.setEnabled(false);
                    loadMarkersButton.setEnabled(false);
                } else {
                    hubMarkerList.setEnabled(true);
                    loadMarkersButton.setEnabled(true);
                }
            }
        });

        kernelCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String selectedItem = (String) cb.getSelectedItem();
                if (KERNEL_INFERRED.equals(selectedItem)) {
                    kernelWidth.setEnabled(false);
                } else {
                    kernelWidth.setEnabled(true);
                }
            }
        });

        dpiCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String selectedItem = (String) cb.getSelectedItem();
                if (DPI_NONE.equals(selectedItem)) {
                    dpiTolerance.setEnabled(false);
                } else {
                    dpiTolerance.setEnabled(true);
                }
            }
        });

        targetCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (!targetCheckbox.isSelected()) {
                    targetList.setEnabled(false);
                    loadTargetsButton.setEnabled(false);
                } else {
                    targetList.setEnabled(true);
                    loadTargetsButton.setEnabled(true);
                }
            }
        });

        loadMarkersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                hubGenes = new ArrayList<String>();
                StringBuilder geneListBuilder = new StringBuilder();
                try {
                    File hubFile = new File(hubMarkersFile);
                    JFileChooser chooser = new JFileChooser(hubFile.getParent());
                    chooser.showOpenDialog(AracneParamPanel.this);
                    hubMarkersFile = chooser.getSelectedFile().getPath();

                    BufferedReader reader = new BufferedReader(new FileReader(hubMarkersFile));
                    String hub = reader.readLine();
                    while (hub != null) {
                        hubGenes.add(hub);
                        geneListBuilder.append(hub + ",");
                    }

                    hubMarkerList.setText(geneListBuilder.toString());

                } catch (IOException e) {
                    log.error(e);
                }

            }
        });

//        builder.append("Full Set Kernel Width", fullsetKernelWidth);
//        builder.append("Full Set Mi Threshold", fullsetMIThreshold);
//        builder.append("Candidate Modulators", loadResultsButton);
//        builder.append("Transcription Factor", transcriptionFactor);
//        builder.nextRow();
        this.add(builder.getPanel());
        loadResultsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = new File(hubMarkersFile);
                JFileChooser chooser = new JFileChooser(file.getParentFile());
                chooser.showOpenDialog(AracneParamPanel.this);
                hubMarkersFile = chooser.getSelectedFile().getPath();
            }
        });
    }

    public boolean isHubListSpecified() {
        return hubCombo.getSelectedItem().equals(HUB_LIST);
    }

    public String getHubMarkersFile() {
        return hubMarkersFile;
    }

    public boolean isKernelWidthSpecified() {
        return kernelCombo.getSelectedItem().equals(KERNEL_SPECIFY);
    }

    public float getKernelWidth() {
        return Float.valueOf(kernelWidth.getText());
    }

    public boolean isThresholdMI() {
        return thresholdCombo.getSelectedItem().equals(THRESHOLD_MI);
    }

    public float getThreshold() {
        return Float.valueOf(threshold.getText());
    }

    public boolean isDPIToleranceSpecified() {
        return dpiCombo.getSelectedItem().equals(DPI_APPLY);
    }

    public float getDPITolerance() {
        return Float.valueOf(dpiTolerance.getText());
    }

    public java.util.List<String> getHubGeneList() {
        return hubGenes;
    }

    public String getHubGeneString() {
        return hubMarkerList.getText();
    }

    public boolean isTargetListSpecified() {
        return targetCheckbox.isSelected();
    }

}
