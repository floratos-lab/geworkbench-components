package org.geworkbench.components.aracne;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.events.listeners.ParameterActionListener;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.columbia.c2b2.aracne.Parameter;

/**
 * @author mhall
 * @author yc2480
 * @version $Id$
 */
public class AracneParamPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = 4023695671471667725L;

	static Log log = LogFactory.getLog(AracneParamPanel.class);

    public static final String DEFAULT_HUB = "31564_at";

    public static final String HUB_ALL = "All vs. All";
    public static final String FROM_SETS = "From Sets";
    public static final String FROM_FILE = "From File";
    public static final String THRESHOLD_MI = "Mutual Info.";
    public static final String THRESHOLD_PVALUE = "P-Value";
    public static final String KERNEL_INFERRED = "Inferred";
    public static final String KERNEL_SPECIFY = "Specify";
    public static final String DPI_NONE = "Do Not Apply";
    public static final String DPI_APPLY = "Apply";

    public static final String FIXED_BANDWIDTH = "Fixed Bandwidth";
    public static final String ADAPTIVE_PARTITIONING = "Adaptive Partitioning";

    public static final String FIXED = "ARACNe_FBW";
    public static final String ADAPTIVE = "ARACNe_AP";

    public static final String COMPLETE = "Complete";
    public static final String PREPROCESSING = "Preprocessing";
    public static final String DISCOVERY = "Discovery";

    private JButton loadResultsButton = new JButton("Load...");
    private String hubMarkersFile = new String("data/test.txt");

    private JComboBox hubCombo = new JComboBox(new String[]{HUB_ALL, FROM_SETS, FROM_FILE});
    private JComboBox algorithmCombo = new JComboBox(new String[]{ADAPTIVE_PARTITIONING,FIXED_BANDWIDTH});
    private JComboBox modeCombo = new JComboBox(new String[]{COMPLETE,PREPROCESSING, DISCOVERY});

    private JComboBox thresholdCombo = new JComboBox(new String[]{THRESHOLD_MI, THRESHOLD_PVALUE});
    private JComboBox kernelCombo = new JComboBox(new String[]{KERNEL_INFERRED, KERNEL_SPECIFY});
    private JComboBox dpiCombo = new JComboBox(new String[]{DPI_NONE, DPI_APPLY});
    private JButton loadMarkersButton = new JButton("Load Markers");
    private JComboBox markerSetCombo = new JComboBox();
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
                "right:max(40dlu;pref), 3dlu, 90dlu, 3dlu, 90dlu, 3dlu, 90dlu, 3dlu, 90dlu, 7dlu",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator("ARACNE Paramaters");

        builder.append("Hub Marker(s)", hubCombo);        
		markerSetCombo.setEnabled(false);
        builder.append(markerSetCombo, hubMarkerList, loadMarkersButton);

        builder.append("Threshold Type", thresholdCombo, threshold);
        builder.nextRow();

        /* choices of two algorithms for now     */
        builder.append("Algorithm", algorithmCombo);
        builder.nextRow();

        /* choices of three modes     */
        builder.append("Mode", modeCombo);
        builder.nextRow();

        builder.append("Kernel Width", kernelCombo, kernelWidth);
        builder.nextRow();

        builder.append("DPI Tolerance", dpiCombo, dpiTolerance);
        builder.nextRow();

        builder.append("DPI Target List", targetCheckbox);
        builder.append(targetList, loadTargetsButton);
        builder.nextRow();

        builder.append("Bootstrap number", bootstrapField);
        builder.append("Consensus threshold", pThresholdField);
        
        markerSetCombo.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent actionEvent) {
    			String selectedLabel = (String) markerSetCombo.getSelectedItem();
    			if (!StringUtils.isEmpty(selectedLabel))
    				if (!chooseMarkersFromSet(selectedLabel, hubMarkerList)) {
    					markerSetCombo.setSelectedIndex(0);
    					hubMarkerList.setText("");
    				}
    		}
    	});
        

        hubCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String selectedItem = (String) cb.getSelectedItem();
                if (HUB_ALL.equals(selectedItem)) {
                    markerSetCombo.setEnabled(false);
                    hubMarkerList.setEnabled(false);
                    loadMarkersButton.setEnabled(false);
                } else if (FROM_SETS.equals(selectedItem)) {
                	markerSetCombo.setEnabled(true);
                    hubMarkerList.setEnabled(true);
                    loadMarkersButton.setEnabled(false);

                    markerSetCombo.removeAllItems();
                    markerSetCombo.addItem(" ");
                	for(String setName: getMarkerSets()) {
                		markerSetCombo.addItem(setName);
                	}
                	markerSetCombo.setSelectedIndex(-1); // -1 for no selection
                } else if (FROM_FILE.equals(selectedItem)) {
                	markerSetCombo.setEnabled(false);
                    hubMarkerList.setEnabled(true);
                    loadMarkersButton.setEnabled(true);
                }
            }
        });

        algorithmCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setKernelCombo();
			}
		});


        kernelCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                JComboBox cb = (JComboBox) e.getSource();
                setKernelWidth();
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

	    ParameterActionListener parameterActionListener = new ParameterActionListener(this);
	    hubCombo.addActionListener(parameterActionListener);
	    markerSetCombo.addActionListener(parameterActionListener);
	    hubMarkerList.addActionListener(parameterActionListener);
	    thresholdCombo.addActionListener(parameterActionListener);
	    threshold.addActionListener(parameterActionListener);
	    kernelCombo.addActionListener(parameterActionListener);
	    kernelWidth.addActionListener(parameterActionListener);
	    dpiCombo.addActionListener(parameterActionListener);
	    dpiTolerance.addActionListener(parameterActionListener);
	    targetCheckbox.addActionListener(parameterActionListener);
	    targetList.addActionListener(parameterActionListener);
	    algorithmCombo.addActionListener(parameterActionListener);
	    modeCombo.addActionListener(parameterActionListener);
	    bootstrapField.addActionListener(parameterActionListener);
	    pThresholdField.addActionListener(parameterActionListener);
    }

	public void setKernelCombo() {
        String selectedItem = (String) algorithmCombo.getSelectedItem();

        if (selectedItem.equals(ADAPTIVE_PARTITIONING) ){
        	kernelCombo.setEnabled(false);
        	kernelWidth.setEnabled(false);
        } else {
        	kernelCombo.setEnabled(true);
        	setKernelWidth();
        }
	}

    public boolean isHubListSpecified() {
        return hubCombo.getSelectedItem().equals(FROM_FILE) || hubCombo.getSelectedItem().equals(FROM_SETS);
    }

    public void setIsHubListSpecified(boolean b) {
    	if (!b)
    		hubCombo.setSelectedItem(HUB_ALL);
    }

    public String getHubMarkersFile() {
        return hubMarkersFile;
    }

    public boolean isKernelWidthSpecified() {
        return kernelCombo.getSelectedItem().equals(KERNEL_SPECIFY);
    }

    public void setIsKernelWidthSpecified(boolean b) {
    	if (b){
    		kernelCombo.setSelectedItem(KERNEL_SPECIFY);
    	}else{
    		kernelCombo.setSelectedItem(KERNEL_INFERRED);
    	}
    }

    public float getKernelWidth() {
        return Float.valueOf(kernelWidth.getText());
    }

    public void setKernelWidth(Float f) {
    	kernelWidth.setText(f.toString());
    }

    public boolean isThresholdMI() {
        return thresholdCombo.getSelectedItem().equals(THRESHOLD_MI);
    }

    public void setIsThresholdMI(boolean b) {
    	if (b){
    		thresholdCombo.setSelectedItem(THRESHOLD_MI);
    	}else{
    		thresholdCombo.setSelectedItem(THRESHOLD_PVALUE);
    	}
    }

    public float getThreshold() {
        return Float.valueOf(threshold.getText());
    }

    public void setThreshold(Float f) {
    	threshold.setText(f.toString());
    }

    public boolean isDPIToleranceSpecified() {
        return dpiCombo.getSelectedItem().equals(DPI_APPLY);
    }

    public void setIsDPIToleranceSpecified(boolean b) {
    	if (b)
    		dpiCombo.setSelectedItem(DPI_APPLY);
    	else
    		dpiCombo.setSelectedItem(DPI_NONE);
    }

    public void setAlgorithm(String algor) {
		algorithmCombo.setSelectedItem(algor);
	}

    public Parameter.ALGORITHM getAlgorithm() {
		Parameter.ALGORITHM algor = Parameter.ALGORITHM.FIXED_BANDWIDTH;

    	if (algorithmCombo.getSelectedItem().equals(FIXED_BANDWIDTH)){
    		algor = Parameter.ALGORITHM.FIXED_BANDWIDTH;
    	}

    	if (algorithmCombo.getSelectedItem().equals(ADAPTIVE_PARTITIONING)){
    		algor = Parameter.ALGORITHM.ADAPTIVE_PARTITIONING;
    	}

        return algor;
    }

    public String getAlgorithmAsString() {
		String algor = algorithmCombo.getSelectedItem().toString();

        return algor;
    }

    public String getAlgorithmForFileName() {
		String algor = FIXED;

    	if (algorithmCombo.getSelectedItem().equals(FIXED_BANDWIDTH)){
    		algor = FIXED;
    	}

    	if (algorithmCombo.getSelectedItem().equals(ADAPTIVE_PARTITIONING)){
    		algor = ADAPTIVE;
    	}

        return algor;
    }

    public String getModeAsString() {
		String mode = modeCombo.getSelectedItem().toString();

        return mode;
    }

    public Parameter.MODE getMode() {
		Parameter.MODE mode = Parameter.MODE.COMPLETE;

    	if (modeCombo.getSelectedItem().equals(PREPROCESSING)){
    		mode = Parameter.MODE.PREPROCESSING;
    	}

    	if (modeCombo.getSelectedItem().equals(DISCOVERY)){
    		mode = Parameter.MODE.DISCOVERY;
    	}

        return mode;
    }

    public void setMode(String mode) {
    	modeCombo.setSelectedItem(mode);
	}

    public String getMarkerSet() {
    	String markerSet = (String) markerSetCombo.getSelectedItem();

        return markerSet;
    }

    public void setMarkerSet(String markerSet) {
    	markerSetCombo.setSelectedItem(markerSet);
	}

    public String getHubAsString() {
		String mode = hubCombo.getSelectedItem().toString();

        return mode;
    }

    public void setHub(String hub) {
    	hubCombo.setSelectedItem(hub);
	}

    public void setDPITolerance(Float f) {
    	dpiTolerance.setText(f.toString());
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

    public void setHubGeneString(String s) {
        hubMarkerList.setText(s);
    }

    public String getTargetGeneString() {
        return targetList.getText();
    }

    public void setTargetGeneString(String s) {
    	targetList.setText(s);
    }

    public boolean isTargetListSpecified() {
        return targetCheckbox.isSelected();
    }

    public void setIsTargetListSpecified(boolean b) {
    	targetCheckbox.setSelected(b);
    }

    public ArrayList<String> getTargetGenes() {
        String geneString = targetList.getText();
        ArrayList<String> geneList = breakStringIntoGenes(geneString);
        return geneList;
    }

    public String getTargetListFile() {
        return targetListFile;
    }

    public String getConsensusThresholdAsText() {
    	return pThresholdField.getText();
    }
    public void setConsensusThresholdAsText(String thresh) {
    	pThresholdField.setText(thresh);
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

    public String getBootstrapField() {
    	return bootstrapField.getText();
    }
    public void setBootstrapField(String bootstrap) {
    	bootstrapField.setText(bootstrap);
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
	public String getDataSetHistory() {
        final Parameter p = new Parameter();
        AracneParamPanel params = this;
        if (params.isHubListSpecified()) {
            ArrayList<String> hubGeneList = params.getHubGeneList();
            p.setSubnet(new Vector<String>(hubGeneList));
        }
        p.setAlgorithm(params.getAlgorithm());
        p.setMode(params.getMode());
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

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    public void setParameters(Map<Serializable, Serializable> parameters){
    	if (getStopNotifyAnalysisPanelTemporaryFlag()==true) return;
    	stopNotifyAnalysisPanelTemporary(true);

        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
		String markerSetTmp = null;
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();

			if (key.equals("isHubListSpecified")){
				setIsHubListSpecified((Boolean)value);
			}
			if (key.equals("HubGeneList")){
				setHubGeneString((String)value);
			}
			if (key.equals("isThresholdMI")){
				setIsThresholdMI((Boolean)value);
			}
			if (key.equals("Threshold")){
				setThreshold((Float)value);
			}
			if (key.equals("isKernelWidthSpecified")){
				setIsKernelWidthSpecified((Boolean)value);
			}
			if (key.equals("KernelWidth")){
				setKernelWidth((Float)value);
			}
			if (key.equals("isDPIToleranceSpecified")){
				setIsDPIToleranceSpecified((Boolean)value);
			}
			if (key.equals("DPITolerance")){
				setDPITolerance((Float)value);
			}
			if (key.equals("isTargetListSpecified")){
				setIsTargetListSpecified((Boolean)value);
                if (!targetCheckbox.isSelected()) {
                    targetList.setEnabled(false);
                    loadTargetsButton.setEnabled(false);
                } else {
                    targetList.setEnabled(true);
                    loadTargetsButton.setEnabled(true);
                }
			}
			if (key.equals("TargetGenes")){
				setTargetGeneString((String)value);
			}

			if (key.equals("Algorithm")){
				setAlgorithm((String)value);
			}
			if (key.equals("Mode")){
				setMode((String)value);
			}
			if (key.equals("Hub")){
				setHub((String)value);
			}
			if (key.equals("MarkerSet")){
				markerSetTmp = (String)value;
//				setMarkerSet(markerSetTmp);
			}
			if (key.equals("BootstrapNumber")){
				setBootstrapField((String)value);
			}
			if (key.equals("ConsensusThreshold")){
				setConsensusThresholdAsText((String)value);
			}

		}
        /*  setHub method can reset or disable value of markerSetCombo, see wiki Parameter Panel for more details  */
		setMarkerSet(markerSetTmp);

		stopNotifyAnalysisPanelTemporary(false);
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 *      Since HierClustPanel only has three parameters, we return metric,
	 *      dimension and method in the format same as getBisonParameters().
	 */
    public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();

		parameters.put("Hub", this.getHubAsString());
		parameters.put("MarkerSet", this.getMarkerSet());

		parameters.put("isHubListSpecified", this.isHubListSpecified());
		parameters.put("HubGeneList", this.getHubGeneString());
		parameters.put("isThresholdMI", this.isThresholdMI());
		parameters.put("Threshold", this.getThreshold());
		parameters.put("isKernelWidthSpecified", this.isKernelWidthSpecified());
		parameters.put("KernelWidth", this.getKernelWidth());
		parameters.put("isDPIToleranceSpecified", this.isDPIToleranceSpecified());
		parameters.put("DPITolerance", this.getDPITolerance());
		parameters.put("isTargetListSpecified", this.isTargetListSpecified());
		parameters.put("TargetGenes", this.getTargetGeneString());
		parameters.put("Algorithm", this.getAlgorithmAsString());
		parameters.put("Mode", this.getModeAsString());

		parameters.put("BootstrapNumber", this.getBootstrapField());
		parameters.put("ConsensusThreshold", this.getConsensusThresholdAsText());

		return parameters;
	}

	public void maMode(){	//switch to microarray analysis mode
		hubCombo.setEnabled(true);
		setKernelCombo();

        String selectedItem = (String) hubCombo.getSelectedItem();
        if (HUB_ALL.equals(selectedItem)) {
            markerSetCombo.setEnabled(false);
            hubMarkerList.setEnabled(false);
            loadMarkersButton.setEnabled(false);
        } else if (FROM_SETS.equals(selectedItem)) {
        	markerSetCombo.setEnabled(true);
            hubMarkerList.setEnabled(true);
            loadMarkersButton.setEnabled(false);

        	markerSetCombo.removeAllItems();
        	markerSetCombo.addItem(" ");
        	for(String setName: getMarkerSets()) {
        		markerSetCombo.addItem(setName);
        	}
        	markerSetCombo.setSelectedIndex(-1); // -1 for no selection
        } else if (FROM_FILE.equals(selectedItem)) {
        	markerSetCombo.setEnabled(false);
            hubMarkerList.setEnabled(true);
            loadMarkersButton.setEnabled(true);
        }

	}
	public void adjMode(AdjacencyMatrixDataSet adjDataSet){	//switch to adj matrix mode, disable some parameters.
		hubCombo.setEnabled(false);
		loadMarkersButton.setEnabled(false);
        markerSetCombo.setEnabled(false);
		kernelCombo.setEnabled(false);
		hubMarkerList.setEnabled(false);
		kernelWidth.setEnabled(false);
	}

	/**
	 * This is added to make the marker sets available.
	 */
	private DSMicroarraySet<DSMicroarray> maSet=null;


	/**
	 * was added as fix bug #1997
	 */
	public String getMaSetName() {
		return maSet.getDataSetName();
	}

	/**
	 * This method needs to be called to make microarray set available.
	 * @param maSet
	 */
	public void setMicroarraySet(DSMicroarraySet<DSMicroarray> maSet){
		this.maSet = maSet;
	}	

	/**
	 * Get the list of available mark sets.
	 */
	private List<String> getMarkerSets() {
		List<String> list = new ArrayList<String>();
		if (maSet == null)
			return list; // in case maSet is not properly set

		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<DSGeneMarker> markerSet = manager
				.getCurrentContext(maSet.getMarkers());

		for (int cx = 0; cx < markerSet.getNumberOfLabels(); cx++) {
			list.add(markerSet.getLabel(cx));
		}
		return list;
	}

	/**
	 * enable or disable kernelWidth depending on the kernelCombo selection
	 */
	private void setKernelWidth() {
		String selectedItem = (String) kernelCombo.getSelectedItem();
		if (KERNEL_INFERRED.equals(selectedItem)) {
		    kernelWidth.setEnabled(false);
		} else {
		    kernelWidth.setEnabled(true);
		}
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	}

	public String getThresholdFile(String dataSetName) {
		String DATASETNAME_ALGORITHM_threshold_file = dataSetName +"_" + getAlgorithmForFileName() +"_"  + "threshold.txt";

		return DATASETNAME_ALGORITHM_threshold_file;
	}

	public String getKernelFile(String dataSetName) {
		String DATASETNAME_ALGORITHM_kernel_file = dataSetName +"_" + getAlgorithmForFileName() +"_"  + "kernel.txt";

		return DATASETNAME_ALGORITHM_kernel_file;
	}
	
	void setSelectorPanel(DSPanel<DSGeneMarker> ap) {
		selectorPanel = ap;		
		String currentTargetSet = (String) markerSetCombo.getSelectedItem();
		DefaultComboBoxModel targetComboModel = (DefaultComboBoxModel) markerSetCombo.getModel();
		targetComboModel.removeAllElements();
		targetComboModel.addElement(" ");
		hubMarkerList.setText("");
		for (DSPanel<DSGeneMarker> panel : selectorPanel.panels()) {
			String label = panel.getLabel().trim();
			targetComboModel.addElement(label);
			if(currentTargetSet!=null){
				if (StringUtils.equals(label, currentTargetSet.trim())){
					targetComboModel.setSelectedItem(label);					
				}
			}
		}
	}
	
}
