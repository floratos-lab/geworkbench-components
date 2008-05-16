package org.geworkbench.components.matrixreduce;

import org.apache.commons.lang.StringUtils;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.engine.properties.PropertiesManager;

import javax.swing.*;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.File;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.DefaultFormBuilder;
/**
 * @author John Watkinson
 * @author ch2514
 *
 * todo - make serializable work
 */
public class MatrixReduceParamPanel extends AbstractSaveableParameterPanel implements Serializable {

	static final String FILE_SPECIFY = "Please specify file";
	static final String PATTERN_REQUIRED = "Please specify file or pattern";
	static final String USE_SPECIFIED_PATTERN = "";
	
	private static final String[] TOPO_CHOICES = {"Specify pattern", "Load from file"};
	private static final String[] STRAND_CHOICES = {"Auto-detect", "Leading", "Reverse", "Both"};
	private static final int[] STRAND_NUMBERS = {0, 1, -1, 2};  // These numbers correspond to the choices above
	
	private static final String SEQUENCE_DATA_DIR = "sequence";
	private static final String TOPOLOGY_DATA_DIR = "topology";
	
	private static final String DEFAULT_DATA_DIR = "data";
	
    private JFormattedTextField pValue = new JFormattedTextField(0.001);
    private JComboBox strandCombo = new JComboBox(STRAND_CHOICES);
    private JFormattedTextField maxMotif = new JFormattedTextField(20);
    private JButton sequenceButton = new JButton("Load...");
    private String sequenceFile = FILE_SPECIFY;
	private JLabel filename = new JLabel();
	private JComboBox topoCombo = new JComboBox(TOPO_CHOICES);
	private JButton topoButton = new JButton("Load...");
	private String topoFile = USE_SPECIFIED_PATTERN;
	private JLabel topoFilename = new JLabel();
	private JFormattedTextField topoPattern = new JFormattedTextField("N8");
	private JCheckBox saveRunlog = new JCheckBox("Save run log");
	
	private String seqDir = DEFAULT_DATA_DIR;
	private String topoDir = DEFAULT_DATA_DIR;
	
	private static class SerialInstance implements Serializable {
		private String seqFile;
		private Object topoChoice;
		private Object topoPattern;
		private String topoFile;
		private Object pvalue;
		private Object maxMotif;
		private int strand;	// this is the combo selected index, not the strand value sent to the service
		private boolean saveRunlog;
		
		public SerialInstance(String seqFile,
			Object topoChoice,
			Object topoPattern,
			String topoFile,
			Object pvalue,
			Object maxMotif,
			int strand,
			boolean saveRunlog){
			
			this.seqFile = seqFile;
			this.topoChoice = topoChoice;
			this.topoPattern = topoPattern;
			this.topoFile = topoFile;
			this.pvalue = pvalue;
			this.maxMotif = maxMotif;
			this.strand = strand;	// this is the combo selected index, not the strand value sent to the service
			this.saveRunlog = saveRunlog;
		}
		
		Object readResolve() throws ObjectStreamException {
			MatrixReduceParamPanel result = new MatrixReduceParamPanel();
			result.sequenceFile = this.seqFile;
			result.filename.setForeground(Color.BLACK);
			if(this.seqFile.trim().equals(MatrixReduceParamPanel.FILE_SPECIFY))
				result.filename.setForeground(Color.RED);
			result.filename.setText(result.sequenceFile);
			result.topoCombo.setSelectedItem(this.topoChoice);
			result.topoPattern.setValue(this.topoPattern);
			result.topoFile = this.topoFile;
			result.topoFilename.setForeground(Color.BLACK);
			if(this.topoFile.trim().equals(MatrixReduceParamPanel.FILE_SPECIFY))
				result.topoFilename.setForeground(Color.RED);
			result.topoFilename.setText(result.topoFile);
			result.pValue.setValue(this.pvalue);
			result.maxMotif.setValue(this.maxMotif);
			// strand is the combo selected index, not the strand value sent to the service
			result.strandCombo.setSelectedIndex(strand);	
			result.saveRunlog.setSelected(this.saveRunlog);
			return result;
		}
	}
	
	Object writeReplace() throws ObjectStreamException {
		return new SerialInstance(this.getSequenceFile()
				, this.getTopoChoice()
				, this.topoPattern.getValue()
				, this.getTopoFile()
				, this.pValue.getValue()
				, this.maxMotif.getValue()
				, this.strandCombo.getSelectedIndex()
				, this.saveRunlog.isSelected()
			);
	}


    public MatrixReduceParamPanel() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.gridx =0; c.gridy=0;
        
        readProperties();
        filename.setForeground(Color.RED); 
        filename.setText(sequenceFile);    
        topoPattern.setColumns(5);
        topoFilename.setText(topoFile);
        topoButton.setEnabled(false);
        saveRunlog.setSelected(false);
        
        FormLayout layout0 = new FormLayout(
        		"left:max(40dlu;pref), 3dlu, 70dlu, 7dlu, " +
            	"right:max(40dlu;pref), 3dlu, 60dlu, 7dlu, " +
            	"left:max(40dlu;pref), 3dlu, 60dlu, 7dlu, " +
            	"right:20dlu",
            	"");    	
        DefaultFormBuilder builder0 = new DefaultFormBuilder(layout0);
      	builder0.setDefaultDialogBorder();
      	builder0.appendSeparator("Files");
      	builder0.append("Sequence");
      	builder0.append(new JLabel("  "));
      	builder0.append(new JLabel("  "));
      	builder0.append(sequenceButton, filename);
      	builder0.nextRow();
      	builder0.append("Topological Pattern", topoCombo);
      	builder0.append(topoPattern);
      	builder0.append(topoButton, topoFilename);
      	this.add(builder0.getPanel(),c);      	      	
        
      	FormLayout layout = new FormLayout(
            	"left:max(40dlu;pref), 3dlu, 40dlu, 7dlu, " +
            	"right:max(40dlu;pref), 3dlu, 40dlu, 7dlu, " +
            	"right:max(40dlu;pref), 3dlu, 60dlu, 7dlu, " +
            	"right:max(40dlu;pref), 3dlu, 60dlu, 7dlu, " +
            	"right:20dlu",
            	"");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator("Parameters");
        builder.append("P Value", pValue);
        builder.append("Max Motif", maxMotif);
        builder.append("Strand", strandCombo);
        builder.append("", saveRunlog);
        c.weightx = 0.5;
        c.gridx =0; c.gridy=1;
        this.add(builder.getPanel(),c);
 
        rangeVerifier rvPV = new rangeVerifier();
        rvPV.setrange(0.0,1.0);
        pValue.setInputVerifier(rvPV);
        rangeVerifierInt rvMM = new rangeVerifierInt();
        rvMM.setrange(0,20);
        maxMotif.setInputVerifier(rvMM);
        
        
        sequenceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = new File(sequenceFile);
                JFileChooser chooser = new JFileChooser(seqDir);
                int returnVal = chooser.showOpenDialog(MatrixReduceParamPanel.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
	                sequenceFile = chooser.getSelectedFile().getPath();
	                String sequenceFileName = chooser.getSelectedFile().getName();
	                filename.setForeground(Color.BLACK);
	            	filename.setText(sequenceFileName);
	            	filename.setToolTipText(sequenceFile);
	            	seqDir = chooser.getSelectedFile().getParent();
	            	saveProperties();
                }
            }
        });
        
        topoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = new File(topoFile);
                JFileChooser chooser = new JFileChooser(topoDir);
                int returnVal = chooser.showOpenDialog(MatrixReduceParamPanel.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
	                topoFile = chooser.getSelectedFile().getPath();
	                String topoFileName = chooser.getSelectedFile().getName();
	                topoFilename.setForeground(Color.BLACK);
	            	topoFilename.setText(topoFileName);
	            	topoFilename.setToolTipText(topoFile);
	            	topoPattern.setText("");
	            	topoDir = chooser.getSelectedFile().getParent();
	            	saveProperties();
                }
            }
        });
        
        topoCombo.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if(((String) topoCombo.getSelectedItem()).equals(TOPO_CHOICES[0])){
        			topoFile = "";
        			topoFilename.setText(topoFile);
        			topoButton.setEnabled(false);
        			topoPattern.setEnabled(true);
        		} else {
        			topoButton.setEnabled(true);
        			topoPattern.setText("");
        			topoPattern.setEnabled(false);
        			if(topoFile.trim().equals("")){
        				topoFile = FILE_SPECIFY;
        				topoFilename.setForeground(Color.RED);
        				topoFilename.setText(topoFile);        				
        			} 
        		}
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
                         
                         JOptionPane.showMessageDialog(MatrixReduceParamPanel.this.getParent(), "value should be between " + Double.toString(min) 
                        		 + " and " + Double.toString(max));
                         return false;
                     } catch (java.lang.Exception pe) {
                    	 JOptionPane.showMessageDialog(MatrixReduceParamPanel.this.getParent(), "values need to be numerical");
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
                         JOptionPane.showMessageDialog(MatrixReduceParamPanel.this.getParent(), "value should be between " + Integer.toString(min) 
                        		 + " and " + Integer.toString(max));
                         return false;
                     } catch (java.lang.Exception pe) {
                    	 JOptionPane.showMessageDialog(MatrixReduceParamPanel.this.getParent(), "values need to be integers");
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

    public double getPValue() {
        return ((Number) pValue.getValue()).doubleValue();
    }

    public int getStrand() {
        return STRAND_NUMBERS[strandCombo.getSelectedIndex()];
    }

    public int getMaxMotif() {
        return ((Number) maxMotif.getValue()).intValue();
    }

    public String getSequenceFile() {
        return sequenceFile;
    }
    
    public Object getTopoChoice(){
    	return topoCombo.getSelectedItem();
    }
    
    public String getTopoPattern() {
    	return topoPattern.getText();
    }
    
    public String getTopoFile(){
    	return topoFile;
    }
    
    public boolean saveRunLog(){
    	return saveRunlog.isSelected();
    }

	/**
	 * {@link java.io.Serializable} method
	 * 
	 * @param out
	 *            <code>ObjectOutputStream</code>
	 * @throws IOException
	 */
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	/**
	 * {@link java.io.Serializable} method
	 * 
	 * @param in
	 *            <code>ObjectInputStream</code>
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		revalidate();
	}
	
	/**
	 * 
	 * 
	 */
	private void saveProperties() {

		PropertiesManager properties = PropertiesManager.getInstance();
		try {
			properties.setProperty(this.getClass(), SEQUENCE_DATA_DIR, String
					.valueOf(seqDir));
			properties.setProperty(this.getClass(), TOPOLOGY_DATA_DIR, String
					.valueOf(topoDir));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 
	 */
	private void readProperties() {
		PropertiesManager pm = PropertiesManager.getInstance();
		String savedSeqDir = null;
		String savedTopoDir = null;
		try {
			savedSeqDir = pm.getProperty(this.getClass(), SEQUENCE_DATA_DIR, seqDir);
			if (!StringUtils.isEmpty(savedSeqDir)) {
				seqDir = savedSeqDir;
			}
			savedTopoDir = pm.getProperty(this.getClass(), TOPOLOGY_DATA_DIR, topoDir);
			if (!StringUtils.isEmpty(savedTopoDir)) {
				topoDir = savedTopoDir;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
