package org.geworkbench.components.mindy;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
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

/**
 * @author mhall
 */
public class MindyParamPanel extends AbstractSaveableParameterPanel implements Serializable {

    static Log log = LogFactory.getLog(MindyParamPanel.class);

    private JButton loadResultsButton = new JButton("Load...");
    private JButton loadModulatorsFile = new JButton("Load");
    private JButton loadDPIAnnotationFile = new JButton("Load");
    private String candidateModulatorsFile = new String("data/mindy/candidateModulators.txt");

    private JTextField modulatorList = new JTextField("");
    private JTextField dpiAnnotationList = new JTextField("");
    private JSpinner setFraction = new JSpinner(new SpinnerNumberModel(35, 1, 50, 1));
    private JSpinner subsetMIThreshold = new JSpinner(new SpinnerNumberModel(0d, 0d, 1d, 0.1d));
    private JSpinner subsetPValue = new JSpinner(new SpinnerNumberModel(0d, 0d, 1d, 0.1d));
    private JSpinner dpiTolerance = new JSpinner(new SpinnerNumberModel(0.1d, 0d, 1d, 0.1d));
    private JSpinner fullsetMIThreshold = new JSpinner(new SpinnerNumberModel(0d, 0d, 1d, 0.1d));
    private JSpinner fullsetPValue = new JSpinner(new SpinnerNumberModel(0d, 0d, 1d, 0.1d));
    private JTextField transcriptionFactor = new JTextField("1973_s_at");
    private String modulatorFile = "data/mindy/candidate_modulator.lst";
    private String dpiAnnotationFile = "data/mindy/transcription_factor.lst";

    public MindyParamPanel() {
        this.setLayout(new BorderLayout());
        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 3dlu, 40dlu, 7dlu, right:max(40dlu;pref), 3dlu, 40dlu, 7dlu",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator("MINDY Parameters");
        builder.append("Conditional Gene List", modulatorList, 3);
        builder.append(loadModulatorsFile);

        builder.append("DPI Annotation List", dpiAnnotationList, 3);
        builder.append(loadDPIAnnotationFile);

        builder.append("Hub Gene", transcriptionFactor);
        builder.append("Conditional Mutual Info.", subsetMIThreshold);
        builder.append("Sample per Condition (%)", setFraction);
        builder.append("Conditional P-Value", subsetPValue);
        builder.append("DPI Tolerance", dpiTolerance);
        builder.append("Unconditional Mutual Info.", fullsetMIThreshold);
        builder.append("Unconditional P-Value", fullsetPValue);
        builder.nextRow();
        this.add(builder.getPanel());

        loadModulatorsFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
//                hubGenes = new ArrayList<String>();
                StringBuilder geneListBuilder = new StringBuilder();
                try {
                    File hubFile = new File(modulatorFile);
                    JFileChooser chooser = new JFileChooser(hubFile.getParent());
                    chooser.showOpenDialog(MindyParamPanel.this);
                    if (chooser.getSelectedFile().getPath() != null) {
                        modulatorFile = chooser.getSelectedFile().getPath();

                        BufferedReader reader = new BufferedReader(new FileReader(modulatorFile));
                        String hub = reader.readLine();
                        while (hub != null && !"".equals(hub)) {
                        //                        hubGenes.add(hub);
                            geneListBuilder.append(hub + ", ");
                            hub = reader.readLine();
                        }

                        String geneString = geneListBuilder.toString();
                        modulatorList.setText(geneString.substring(0, geneString.length() - 2));
                    }

                } catch (IOException e) {
                    log.error(e);
                }

            }
        });

        loadDPIAnnotationFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
//                hubGenes = new ArrayList<String>();
                StringBuilder geneListBuilder = new StringBuilder();
                try {
                    File hubFile = new File(dpiAnnotationFile);
                    JFileChooser chooser = new JFileChooser(hubFile.getParent());
                    chooser.showOpenDialog(MindyParamPanel.this);
                    if (chooser.getSelectedFile().getPath() != null) {
                        dpiAnnotationFile = chooser.getSelectedFile().getPath();

                        BufferedReader reader = new BufferedReader(new FileReader(dpiAnnotationFile));
                        String hub = reader.readLine();
                        while (hub != null && !"".equals(hub)) {
                        //                        hubGenes.add(hub);
                            geneListBuilder.append(hub + ", ");
                            hub = reader.readLine();
                        }

                        String geneString = geneListBuilder.toString();
                        dpiAnnotationList.setText(geneString.substring(0, geneString.length() - 2));
                    }

                } catch (IOException e) {
                    log.error(e);
                }

            }
        });


        loadResultsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = new File(candidateModulatorsFile);
                JFileChooser chooser = new JFileChooser(file.getParentFile());
                chooser.showOpenDialog(MindyParamPanel.this);
                candidateModulatorsFile = chooser.getSelectedFile().getPath();
            }
        });
    }

    public void setTranscriptionFactor(String label) {
        transcriptionFactor.setText(label);
    }

    public String getCandidateModulatorsFile() {
        return candidateModulatorsFile;
    }

    public int getSetFraction() {
        return ((Number) setFraction.getModel().getValue()).intValue();
    }

    public float getSubsetMIThreshold() {
        return ((Number) subsetMIThreshold.getModel().getValue()).floatValue();
    }

    public float getSubsetPValueThreshold() {
        return ((Number) subsetPValue.getModel().getValue()).floatValue();
    }

    public float getDPITolerance() {
        return ((Number) dpiTolerance.getModel().getValue()).floatValue();
    }

    public float getFullSetMIThreshold() {
        return ((Number) fullsetMIThreshold.getModel().getValue()).floatValue();
    }

    public float getFullsetPValueThreshold() {
        return ((Number) fullsetPValue.getModel().getValue()).floatValue();
    }

    public String getTranscriptionFactor() {
        return transcriptionFactor.getText();
    }

    public ArrayList<String> getModulatorGeneList() {
        String geneString = modulatorList.getText();
        ArrayList<String> geneList = breakStringIntoGenes(geneString);
        return geneList;
    }

    public ArrayList<String> getDPIAnnotatedGeneList() {
        String geneString = dpiAnnotationList.getText();
        ArrayList<String> geneList = breakStringIntoGenes(geneString);
        return geneList;
    }

    private ArrayList<String> breakStringIntoGenes(String geneString) {
        String[] genes = geneString.split(",");
        ArrayList<String> geneList = new ArrayList<String>();
        for (String gene : genes) {
            if (gene != null && !"".equals(gene)) {
                geneList.add(gene.trim());
            }
        }
        return geneList;
    }

}
