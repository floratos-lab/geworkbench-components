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
 * MINDY analysis GUI.  Allows the user to enter parameters to analyze.
 * 
 * @author mhall
 * @author ch2514
 * @version $ID$
 */
@SuppressWarnings("serial")
public class MindyParamPanel extends AbstractSaveableParameterPanel implements Serializable {

    static Log log = LogFactory.getLog(MindyParamPanel.class);

    private JButton loadResultsButton = new JButton("Load...");
    private JButton loadModulatorsFile = new JButton("Load");
    private JButton loadDPIAnnotationFile = new JButton("Load");
    private String candidateModulatorsFile = new String("data/mindy/candidateModulators.txt");
    private String modulatorFile = "data/mindy/candidate_modulator.lst";
    private String dpiAnnotationFile = "data/mindy/transcription_factor.lst";

    private JTextField modulatorList = new JTextField("");
    private JTextField dpiAnnotationList = new JTextField("");
    private JSpinner setFraction = new JSpinner(new SpinnerNumberModel(35, 1, 49, 1));
    private JSpinner subsetMIThreshold = new JSpinner(new SpinnerNumberModel(0d, 0d, 1d, 0.1d));
    private JSpinner subsetPValue = new JSpinner(new SpinnerNumberModel(1.0d, 0d, 1d, 0.1d));
    private JSpinner dpiTolerance = new JSpinner(new SpinnerNumberModel(0.1d, 0d, 1d, 0.1d));
    private JSpinner fullsetMIThreshold = new JSpinner(new SpinnerNumberModel(0d, 0d, 1d, 0.1d));
    private JSpinner fullsetPValue = new JSpinner(new SpinnerNumberModel(1.0d, 0d, 1d, 0.1d));
    private JTextField transcriptionFactor = new JTextField("1973_s_at");
    private JRadioButton subsetMIThresholdButton = new JRadioButton("Conditional Mutual Info.  ");
    private JRadioButton subsetPValueButton = new JRadioButton("Conditional P-Value      ");
    private JRadioButton fullsetMIThresholdButton = new JRadioButton("Unconditional Mutual Info.");
    private JRadioButton fullsetPValueButton = new JRadioButton("Unconditional P-Value  ");


    /**
     * Constructor.
     * Creates the parameter panel GUI.
     *
     */
    public MindyParamPanel() {
        this.setLayout(new BorderLayout());
        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 3dlu, 40dlu, 7dlu, right:max(40dlu;pref), 3dlu, 40dlu, 7dlu",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator("MINDY Parameters");
        builder.append("Candidate Modulators", modulatorList, 3);
        builder.append(loadModulatorsFile);

        builder.append("DPI Transcription Factors", dpiAnnotationList, 3);
        builder.append(loadDPIAnnotationFile);

        builder.append("Transcription Factor", transcriptionFactor);
        builder.append(subsetMIThresholdButton, subsetMIThreshold);
        
        builder.append("Sample per Condition (%)", setFraction);   
        builder.append(subsetPValueButton, subsetPValue);
        
        builder.append("DPI Tolerance", dpiTolerance);        
        builder.append(fullsetMIThresholdButton, fullsetMIThreshold);
        
        builder.append("", new JLabel(""));  
        builder.append(fullsetPValueButton, fullsetPValue); 

        builder.nextRow();
        this.add(builder.getPanel());
        
        ButtonGroup cond = new ButtonGroup();
        cond.add(subsetMIThresholdButton);
        cond.add(subsetPValueButton);
        subsetMIThresholdButton.setSelected(true);
        subsetMIThreshold.setEnabled(true);
        subsetPValue.setEnabled(false);    
        
        
        subsetMIThresholdButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent actionEvent){
        		subsetMIThreshold.setEnabled(true);
                subsetPValue.setEnabled(false);
        	}
        });
        
        subsetPValueButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent actionEvent){
        		subsetMIThreshold.setEnabled(false);
                subsetPValue.setEnabled(true);
        	}
        });
        
        ButtonGroup uncond = new ButtonGroup();
        uncond.add(fullsetMIThresholdButton);
        uncond.add(fullsetPValueButton);
        fullsetMIThresholdButton.setSelected(true);
        fullsetMIThreshold.setEnabled(true);
        fullsetPValue.setEnabled(false);
        
        
        fullsetMIThresholdButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent actionEvent){
        		fullsetMIThreshold.setEnabled(true);
        		fullsetPValue.setEnabled(false);
        	}
        });
        
        fullsetPValueButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent actionEvent){
        		fullsetMIThreshold.setEnabled(false);
        		fullsetPValue.setEnabled(true);
        	}
        });

        loadModulatorsFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
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

    /**
     * Sets the transcription factor
     * @param label
     */
    public void setTranscriptionFactor(String label) {
        transcriptionFactor.setText(label);
    }

    /**
     * Gets the candidate modulator file name.
     * @return candidate modulator file name.
     */
    public String getCandidateModulatorsFile() {
        return candidateModulatorsFile;
    }

    /**
     * Gets the set fraction.
     * @return the set fraction
     */
    public int getSetFraction() {
        return ((Number) setFraction.getModel().getValue()).intValue();
    }

    /**
     * Gets the subset MI threshold.
     * @return the subset MI threshold
     */
    public float getSubsetMIThreshold() {
        return ((Number) subsetMIThreshold.getModel().getValue()).floatValue();
    }

    /**
     * Gets the subset P value threshold.
     * @return the subset P value threshold
     */
    public float getSubsetPValueThreshold() {
        return ((Number) subsetPValue.getModel().getValue()).floatValue();
    }

    /**
     * Gets the DPI tolerance.
     * @return the DPI tolerance
     */
    public float getDPITolerance() {
        return ((Number) dpiTolerance.getModel().getValue()).floatValue();
    }

    /**
     * Gets the full set P value threshold.
     * @return the full set P value threshold
     */
    public float getFullSetMIThreshold() {
        return ((Number) fullsetMIThreshold.getModel().getValue()).floatValue();
    }

    /**
     * Gets the full set MI threshold.
     * @return the full set MI threshold
     */
    public float getFullsetPValueThreshold() {
        return ((Number) fullsetPValue.getModel().getValue()).floatValue();
    }

    /**
     * Gets the transcription factor.
     * @return the transcription factor
     */
    public String getTranscriptionFactor() {
        return transcriptionFactor.getText();
    }

    /**
     * Gets the modulator gene list.
     * @return the modulator gene list
     */
    public ArrayList<String> getModulatorGeneList() {
        String geneString = modulatorList.getText();
        ArrayList<String> geneList = breakStringIntoGenes(geneString);
        return geneList;
    }

    /**
     * Gets the DPI annotated gene list.
     * @return the DPI annotated gene list
     */
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
    
    // For framework serialization process
    private static class SerializedInstance implements Serializable {
    	private String modulators;
        private String annotations;
        private String tf;
        
        private Object fraction;
        private Object subsetmithreshold;
        private Object subsetpvalue;
        private Object dpitolerance;
        private Object fullsetmithreshold;
        private Object fullsetpvalue;

        public SerializedInstance(String modulators
        		, String annotations
        		, String tf
        		, Object fraction
        		, Object subsetmithreshold
        		, Object subsetpvalue
        		, Object dpitolerance
        		, Object fullsetmithreshold
        		, Object fullsetpvalue
        		) {
            this.modulators = modulators;
            this.annotations = annotations;
            this.tf = tf;
            
            this.fraction = fraction;
            this.subsetmithreshold = subsetmithreshold;
            this.subsetpvalue = subsetpvalue;
            this.dpitolerance = dpitolerance;
            this.fullsetmithreshold = fullsetmithreshold;
            this.fullsetpvalue = fullsetpvalue;
        }

        Object readResolve() throws ObjectStreamException {
            MindyParamPanel panel = new MindyParamPanel();
            panel.modulatorList.setText(this.modulators);
            panel.dpiAnnotationList.setText(this.annotations);
            panel.transcriptionFactor.setText(this.tf);
            panel.setFraction.setValue(this.fraction);
            panel.subsetMIThreshold.setValue(this.subsetmithreshold);
            panel.subsetPValue.setValue(this.subsetpvalue);
            panel.dpiTolerance.setValue(this.dpitolerance);
            panel.fullsetMIThreshold.setValue(this.fullsetmithreshold);
            panel.fullsetPValue.setValue(this.fullsetpvalue);            
            return panel;
        }
    }

    Object writeReplace() throws ObjectStreamException {
        return new SerializedInstance(this.modulatorList.getText()
        		, this.dpiAnnotationList.getText()
        		, this.transcriptionFactor.getText()
        		, this.setFraction.getValue()
        		, this.subsetMIThreshold.getValue()
        		, this.subsetPValue.getValue()
        		, this.dpiTolerance.getValue()
        		, this.fullsetMIThreshold.getValue()
        		, this.fullsetPValue.getValue()
        		);
    }
    
    /**
     * {@link java.io.Serializable} method
     *
     * @param out <code>ObjectOutputStream</code>
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    /**
     * {@link java.io.Serializable} method
     *
     * @param in <code>ObjectInputStream</code>
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        revalidate();
    }

}
