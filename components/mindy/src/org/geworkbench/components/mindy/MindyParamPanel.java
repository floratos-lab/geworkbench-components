package org.geworkbench.components.mindy;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wb.data.Marker;

import javax.swing.*;

import java.io.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.DefaultFormBuilder;

import edu.columbia.c2b2.mindy.Mindy;

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
    
    public static final String P_VALUE = "P-Value";
    public static final String MI = "Mutual Info";
    public static final String NONE = "None";
    public static final String BONFERRONI = "Bonferroni";
    public static final String FALSE = "False";
    public static final String DISC_RATE = "Discovery Rate";
    
    private static final String[] CONDITIONAL_UNCONDITIONAL = {P_VALUE, MI};
    private static final String[] CONDITIONAL_UNCONDITIONAL_DEFAULT_VALUES = {"1e-2", "0"};
    private static final String[] CORRECTIONS = {NONE, BONFERRONI, FALSE, DISC_RATE};
    private static final String DEFAULT_THRESHOLD_TYPE = CONDITIONAL_UNCONDITIONAL[0];
    private static final String DEFAULT_THRESHOLD_VALUE = CONDITIONAL_UNCONDITIONAL_DEFAULT_VALUES[0];

    private JButton loadModulatorsFile = new JButton("Load");
    private JButton loadDPIAnnotationFile = new JButton("Load");
    private JButton loadTargetsFile = new JButton("Load");
    private String candidateModulatorsFile = new String("data/mindy/candidateModulators.txt");
    private String modulatorFile = "data/mindy/candidate_modulator.lst";
    private String targetFile = "data/mindy/candidate_target.lst";
    private String dpiAnnotationFile = "data/mindy/transcription_factor.lst";

    private JTextField modulatorList = new JTextField("");
    private JTextField targetList = new JTextField("");
    private JTextField dpiAnnotationList = new JTextField("");   
    private JTextField transcriptionFactor = new JTextField("");
    private JSpinner setFraction = new JSpinner(new SpinnerNumberModel(35, 1, 49, 1));
    private JSpinner dpiTolerance = new JSpinner(new SpinnerNumberModel(0.1d, 0d, 1d, 0.1d));
    private JComboBox conditionalCombo = new JComboBox(CONDITIONAL_UNCONDITIONAL);
    private JComboBox unconditionalCombo = new JComboBox(CONDITIONAL_UNCONDITIONAL);
    private JTextField conditional = new JTextField(DEFAULT_THRESHOLD_VALUE);
    private JTextField unconditional = new JTextField(DEFAULT_THRESHOLD_VALUE);
    private JComboBox conditionalCorrection = new JComboBox(CORRECTIONS);
    private JComboBox unconditionalCorrection = new JComboBox(CORRECTIONS);
    
    private JTabbedPane tabs;
    /**
     * Constructor.
     * Creates the parameter panel GUI.
     *
     */
    public MindyParamPanel() {
    	super();
        try {
            init();
        } catch (Exception e) {
        	e.printStackTrace();
            log.debug("Cannot initialize MINDY parameter panel.", e);
        }	        
    }
    
    private void init(){        	
        tabs = new JTabbedPane();
		tabs.addTab("Main", initMainPanel());
		tabs.addTab("Advanced", initAdvancedPanel());
		this.setLayout(new BorderLayout());
		this.add(tabs, BorderLayout.PAGE_START);
	}
    
    private JPanel initMainPanel(){
    	JPanel result = new JPanel(new BorderLayout());
    	FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 3dlu, 40dlu, 7dlu, right:max(40dlu;pref), 3dlu, 40dlu, 7dlu",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator("MINDY Main Parameters");
        builder.append("Modulators List", modulatorList, 3);
        builder.append(loadModulatorsFile);

        builder.append("Target List", targetList, 3);
        builder.append(loadTargetsFile);

        builder.append("Hub Marker", transcriptionFactor);
        builder.nextRow();
        result.add(builder.getPanel());
        

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

        loadTargetsFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                StringBuilder geneListBuilder = new StringBuilder();
                try {
                    File hubFile = new File(targetFile);
                    JFileChooser chooser = new JFileChooser(hubFile.getParent());
                    chooser.showOpenDialog(MindyParamPanel.this);
                    if (chooser.getSelectedFile().getPath() != null) {
                    	targetFile = chooser.getSelectedFile().getPath();

                        BufferedReader reader = new BufferedReader(new FileReader(targetFile));
                        String line = reader.readLine();
                        while (line != null && !"".equals(line)) {
                            geneListBuilder.append(line + ", ");
                            line = reader.readLine();
                        }

                        String geneString = geneListBuilder.toString();
                        targetList.setText(geneString.substring(0, geneString.length() - 2));
                    }

                } catch (IOException e) {
                    log.error(e);
                }

            }
        });

    	return result;
    }
    
    private JPanel initAdvancedPanel(){
    	JPanel result = new JPanel(new BorderLayout());
    	FormLayout layout = new FormLayout(
                "right:max(100dlu;pref), 3dlu, 40dlu, 7dlu, " +
                "right:max(40dlu;pref), 3dlu, 40dlu, 7dlu, " +
                "right:max(100dlu;pref), 3dlu, 40dlu, 7dlu, " +
                "right:max(40dlu;pref), 3dlu, 40dlu, 7dlu",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator("MINDY Advanced Parameters");
        builder.append("Sample per Condition (%)", setFraction); 
        builder.append("", new JLabel(""));
        builder.append("", new JLabel(""));
        builder.append("", new JLabel(""));
        
        builder.append("Conditional", this.conditionalCombo, 3);
        builder.append(this.conditional);        
        builder.append("Correction", this.conditionalCorrection, 3);
        builder.append(new JLabel(""));
        
        builder.append("Unconditional", this.unconditionalCombo, 3);
        builder.append(this.unconditional);      
        builder.append("Correction", this.unconditionalCorrection, 3);
        builder.append(new JLabel(""));
        
        builder.append("DPI Target List", dpiAnnotationList, 3);
        builder.append(loadDPIAnnotationFile);
        builder.append("", new JLabel(""));
        builder.append("", new JLabel(""));
        
        builder.append("DPI Tolerance", dpiTolerance); 
        builder.append("", new JLabel(""));
        builder.append("", new JLabel(""));
        builder.append("", new JLabel(""));
        
        builder.nextRow();
        result.add(builder.getPanel());
        
        // setting up default selections
        this.conditionalCombo.setSelectedIndex(0);
        this.conditionalCorrection.setSelectedIndex(0);
        this.unconditionalCombo.setSelectedIndex(0);
        this.unconditionalCorrection.setSelectedIndex(0);
        
        this.conditionalCombo.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent actionEvent){
        		conditional.setText("" + CONDITIONAL_UNCONDITIONAL_DEFAULT_VALUES[conditionalCombo.getSelectedIndex()]);
        		if(getConditional().trim().equals(P_VALUE)){
        			conditionalCorrection.setEnabled(true);
        		} else {
        			conditionalCorrection.setSelectedIndex(0);
        			conditionalCorrection.setEnabled(false);
        		}
        	}
        });
        this.unconditionalCombo.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent actionEvent){
        		unconditional.setText("" + CONDITIONAL_UNCONDITIONAL_DEFAULT_VALUES[unconditionalCombo.getSelectedIndex()]);
        		if(getUnconditional().trim().equals(P_VALUE)){
        			unconditionalCorrection.setEnabled(true);
        		} else {
        			unconditionalCorrection.setSelectedIndex(0);
        			unconditionalCorrection.setEnabled(false);
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
        
    	return result;
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
     * Gets the DPI tolerance.
     * @return the DPI tolerance
     */
    public float getDPITolerance() {
        return ((Number) dpiTolerance.getModel().getValue()).floatValue();
    }
    
    public String getConditional(){
    	return CONDITIONAL_UNCONDITIONAL[this.conditionalCombo.getSelectedIndex()];
    }
    
    public float getConditionalValue(){
    	float result = new Double(CONDITIONAL_UNCONDITIONAL_DEFAULT_VALUES[this.conditionalCombo.getSelectedIndex()]).floatValue();
    	try{
    		result = new Double(this.conditional.getText()).floatValue();
    	} catch (NumberFormatException e){
    		log.debug("Failed to get the conditional value. " + e.getMessage());
    	}
    	return result;
    }
    
    public String getConditionalCorrection(){
    	return CORRECTIONS[this.conditionalCombo.getSelectedIndex()];
    }
    
    public String getUnconditional(){
    	return CONDITIONAL_UNCONDITIONAL[this.unconditionalCombo.getSelectedIndex()];
    }
    
    public String getUnconditionalCorrection(){
    	return CORRECTIONS[this.unconditionalCombo.getSelectedIndex()];
    }

    public float getUnconditionalValue(){
    	float result = new Double(CONDITIONAL_UNCONDITIONAL_DEFAULT_VALUES[this.unconditionalCombo.getSelectedIndex()]).floatValue();
    	try{
    		result = new Double(this.unconditional.getText()).floatValue();
    	} catch (NumberFormatException e){
    		log.debug("Failed to get the conditional value. " + e.getMessage());
    	}
    	return result;
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
    	private String targets;
        private String annotations;
        private String tf;        
        private Object fraction;
        private int conditionalType;
        private String conditionalValue;
        private int conditionalCorrection;
        private int unconditionalType;
        private String unconditionalValue;
        private int unconditionalCorrection;
        private String dpitargets;
        private Object dpitolerance;
        

        public SerializedInstance(String modulators
        		, String targets
        		, String annotations
        		, String tf
        		, Object fraction
        		, int conditionalType
        		, String conditionalValue
        		, int conditionalCorrection
        		, int unconditionalType
        		, String unconditionalValue
        		, int unconditionalCorrection
        		, String dpitargets
        		, Object dpitolerance 
        		) {
            this.modulators = modulators;
            this.targets = targets;
            this.annotations = annotations;
            this.tf = tf;            
            this.fraction = fraction;
            this.conditionalType = conditionalType;
            this.conditionalValue = conditionalValue;
            this.conditionalCorrection = conditionalCorrection;
            this.unconditionalType = unconditionalType;
            this.unconditionalValue = unconditionalValue;
            this.unconditionalCorrection = unconditionalCorrection;  
            this.dpitargets = dpitargets;
            this.dpitolerance = dpitolerance;
        }

        Object readResolve() throws ObjectStreamException {
            MindyParamPanel panel = new MindyParamPanel();
            panel.modulatorList.setText(this.modulators);
            panel.targetList.setText(this.targets);
            panel.dpiAnnotationList.setText(this.annotations);
            panel.transcriptionFactor.setText(this.tf);
            panel.setFraction.setValue(this.fraction);
            panel.conditionalCombo.setSelectedIndex(this.conditionalType);
            panel.conditional.setText(this.conditionalValue);
            panel.conditionalCorrection.setSelectedIndex(this.conditionalCorrection);
            panel.unconditionalCombo.setSelectedIndex(this.unconditionalType);
            panel.unconditional.setText(this.unconditionalValue);
            panel.unconditionalCorrection.setSelectedIndex(this.unconditionalCorrection);
            panel.dpiAnnotationList.setText(this.dpitargets);
            panel.dpiTolerance.setValue(this.dpitolerance);            
            
            return panel;
        }
    }

    Object writeReplace() throws ObjectStreamException {
        return new SerializedInstance(this.modulatorList.getText()
        		, this.targetList.getText()
        		, this.dpiAnnotationList.getText()
        		, this.transcriptionFactor.getText()
        		, this.setFraction.getValue()
        		, this.conditionalCombo.getSelectedIndex()
        		, this.conditional.getText()
        		, this.conditionalCorrection.getSelectedIndex()
        		, this.unconditionalCombo.getSelectedIndex()
        		, this.unconditional.getText()
        		, this.unconditionalCorrection.getSelectedIndex()
        		, this.dpiAnnotationList.getText()
        		, this.dpiTolerance.getValue()        		
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
