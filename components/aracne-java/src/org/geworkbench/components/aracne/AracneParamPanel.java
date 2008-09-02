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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Vector;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.DefaultFormBuilder;

import edu.columbia.c2b2.aracne.Parameter;

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

    private String targetListFile = new String("data/targets.txt");
    
    // Add two new parameters for "hardening" ARACNE. They are adapted from perl script instead of Java implementation.
    private JFormattedTextField bootstrapField = new JFormattedTextField("1");
    private JTextField pThresholdField = new JTextField("1.e-6");

    public AracneParamPanel() {
        this.setLayout(new BorderLayout());

        hubMarkerList.setEnabled(false);
        loadMarkersButton.setEnabled(false);
        kernelWidth.setEnabled(false);
        dpiTolerance.setEnabled(false);
        targetList.setEnabled(false);
        loadTargetsButton.setEnabled(false);
        
        pThresholdField.setEnabled(false);

        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 3dlu, 60dlu, 3dlu, 90dlu, 3dlu, 40dlu, 7dlu",
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
        
        builder.append("Bootstrap number", bootstrapField);
        builder.append("Consensus threshold", pThresholdField);

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
//                hubGenes = new ArrayList<String>();
                StringBuilder geneListBuilder = new StringBuilder();
                try {
                    File hubFile = new File(hubMarkersFile);
                    JFileChooser chooser = new JFileChooser(hubFile.getParent());
                    chooser.showOpenDialog(AracneParamPanel.this);
                    hubMarkersFile = chooser.getSelectedFile().getPath();

                    BufferedReader reader = new BufferedReader(new FileReader(hubMarkersFile));
                    String hub = reader.readLine();
                    while (hub != null && !"".equals(hub)) {
//                        hubGenes.add(hub);
                        geneListBuilder.append(hub + ", ");
                        hub = reader.readLine();
                    }

                    String geneString = geneListBuilder.toString();
                    hubMarkerList.setText(geneString.substring(0, geneString.length() - 2));

                } catch (IOException e) {
                    log.error(e);
                }

            }
        });

        loadTargetsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
//                targetGenes = new ArrayList<String>();
                StringBuilder geneListBuilder = new StringBuilder();
                try {
                    File targetFile = new File(targetListFile);
                    JFileChooser chooser = new JFileChooser(targetFile.getParent());
                    chooser.showOpenDialog(AracneParamPanel.this);
                    targetListFile = chooser.getSelectedFile().getPath();

                    BufferedReader reader = new BufferedReader(new FileReader(targetListFile));
                    String target = reader.readLine();
                    while (target != null && !"".equals(target)) {
//                        targetGenes.add(target);
                        geneListBuilder.append(target + ", ");
                        target = reader.readLine();
                    }

                    String geneString = geneListBuilder.toString();
                    targetList.setText(geneString.substring(0, geneString.length() - 2));

                } catch (IOException e) {
                    log.error(e);
                }

            }
        });

        bootstrapField.addKeyListener(new KeyAdapter() {

        	public void keyReleased(KeyEvent e) {
        		if(bootstrapField.getText().trim().equals("1"))
        			pThresholdField.setEnabled(false);
        		else
        			pThresholdField.setEnabled(true);
			}
        	
        });

        /*
         * this listener is triggered by bootstrapField losing the focus
         * another possible way is to listen whenever a key is typed - with its pro (easier to wake pThresholField) 
         * and con (more checkings; higher level so not depending platform as keyReleased)
         */
//        bootstrapField.addPropertyChangeListener("value", new PropertyChangeListener() {
//
//			public void propertyChange(PropertyChangeEvent evt) {
//        		if(bootstrapField.getText().trim().equals("1"))
//        			pThresholdField.setEnabled(false);
//        		else
//        			pThresholdField.setEnabled(true);
//			}
//        
//        });

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

    public ArrayList<String> getHubGeneList() {
        String geneString = hubMarkerList.getText();
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

    public String getHubGeneString() {
        return hubMarkerList.getText();
    }

    public boolean isTargetListSpecified() {
        return targetCheckbox.isSelected();
    }

    public ArrayList<String> getTargetGenes() {
        String geneString = targetList.getText();
        ArrayList<String> geneList = breakStringIntoGenes(geneString);
        return geneList;
    }

    public String getTargetListFile() {
        return targetListFile;
    }

    public double getConsensusThreshold() {
    	double p = 0;
    	try {
    		p = Double.parseDouble(pThresholdField.getText());
    	} catch (NumberFormatException e) {
    		log.warn("[Exception] Consensus threhold field is not a proper number: "+e.getMessage());
    		// the caller of this method has to handle the case that 0 is returned, which is not a valid value
    	}
    	return p;
    }
    
    public int getBootstrapNumber() {
    	int b = 0;
    	try {
    		b = Integer.parseInt(bootstrapField.getText());
    	} catch (NumberFormatException e) {
    		log.warn("[Exception] Bootstrap number field is not a proper number: "+e.getMessage());
    		// the caller of this method has to handle the case that 0 is returned, which is not a valid value
    	}
    	return b;
    }
	@Override
	public String toString() {
        final Parameter p = new Parameter();
        AracneParamPanel params = this;
        if (params.isHubListSpecified()) {
            ArrayList<String> hubGeneList = params.getHubGeneList();
            p.setSubnet(new Vector<String>(hubGeneList));
        }
        if (params.isThresholdMI()) {
            p.setThreshold(params.getThreshold());
        } else {
            p.setPvalue(params.getThreshold());
        }
        if (params.isKernelWidthSpecified()) {
            p.setSigma(params.getKernelWidth());
        }
        if (params.isDPIToleranceSpecified()) {
            p.setEps(params.getDPITolerance());
        }
        if (params.isTargetListSpecified()) {
            p.setTf_list(new Vector<String>(params.getTargetGenes()));
        }
        return p.getParamterDescription();
		
	}    
}
