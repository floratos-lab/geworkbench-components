/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2007) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.gpmodule.classification.knn;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.Serializable;
import java.util.*;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.builtin.projects.LoadData;
import org.geworkbench.components.gpmodule.classification.GPTrainingPanel;
import org.geworkbench.events.listeners.ParameterActionListener;
import org.geworkbench.util.ClassifierException;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Marc-Danie Nazaire
 */
public class KNNTrainingPanel extends GPTrainingPanel {
    private static final int DEFAULT_NUM_FEATURES = 10;
    private static final int DEFAULT_NUM_NEIGHBORS = 3;

    private JRadioButton featureFileMethod;
    private String featureFile = "";
    private javax.swing.JTextField featureFileTextBox;
    private JButton loadFeatureFileButton;
    private JFileChooser featureFileChooser = new JFileChooser();
    private JRadioButton numFeatureMethod;
    private JFormattedTextField numFeatures;
    private JComboBox statistic;
    private JFormattedTextField minStdDev;
    private JCheckBox medianCheckbox;
    private JCheckBox minStdDevCheckbox;
    private JFormattedTextField numNeighbors;
    private JComboBox weightType;
    private JComboBox distanceMeasure;
    private KNNTraining knnTraining;

    public KNNTrainingPanel(KNNTraining knnTraining)
    {
        super(knnTraining.getLabel());
        this.knnTraining = knnTraining;
        try
        {   jbInit();   }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void initUI()
    {
    	
        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        
        numFeatureMethod = new JRadioButton();
        numFeatureMethod.setText("num features");
        numFeatureMethod.setSelected(true);
        numFeatureMethod.addItemListener( new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == 1)
                {
                    numFeatures.setEnabled(true);
                    statistic.setEnabled(true);
                    medianCheckbox.setEnabled(true);
                    minStdDevCheckbox.setEnabled(true);
                }
                else
                {
                    numFeatures.setEnabled(false);
                    statistic.setEnabled(false);
                    medianCheckbox.setEnabled(false);
                    minStdDevCheckbox.setEnabled(false);
                }
            }
        });
        numFeatureMethod.addActionListener(parameterActionListener);

        numFeatures = new JFormattedTextField();
        numFeatures.setValue(DEFAULT_NUM_FEATURES);
        numFeatures.addActionListener(parameterActionListener);

        featureFileMethod = new JRadioButton();
        featureFileMethod.setText("feature filename");
        featureFileMethod.addItemListener( new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == 1)
                {
                    featureFileTextBox.setEnabled(true);
                    loadFeatureFileButton.setEnabled(true);
                }
                else
                {
                    featureFileTextBox.setEnabled(false);
                    loadFeatureFileButton.setEnabled(false);
                }
            }
        });
        featureFileMethod.addActionListener(parameterActionListener);

        featureFileTextBox = new JTextField();
        featureFileTextBox.setEnabled(false);
        featureFileTextBox.addActionListener(parameterActionListener);
        loadFeatureFileButton = new JButton("Load");
        loadFeatureFileButton.setEnabled(false);
        loadFeatureFileButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
               featureFileLoadHandler();
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(numFeatureMethod);
        group.add(featureFileMethod);

        statistic = new JComboBox();
        statistic.addItem("SNR");
        statistic.addItem("T-Test");
        statistic.addActionListener(parameterActionListener);
        
        medianCheckbox = new JCheckBox();
        medianCheckbox.setText("median") ;
        medianCheckbox.addActionListener(parameterActionListener);
        
        minStdDevCheckbox = new JCheckBox();
        minStdDevCheckbox.setText("min std dev");
        minStdDevCheckbox.addItemListener( new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == 1)
                {   minStdDev.setEnabled(true);     }
                else
                {   minStdDev.setEnabled(false);    }
            }
        });
        minStdDevCheckbox.addActionListener(parameterActionListener);
        
        minStdDev = new JFormattedTextField("");
        minStdDev.setMaximumSize(new Dimension(145, 20));
        minStdDev.setMinimumSize(new Dimension(145, 20));
        minStdDev.setMaximumSize(new Dimension(145, 20));
        minStdDev.setEnabled(false);
        minStdDev.addActionListener(parameterActionListener);
        
        numNeighbors = new JFormattedTextField();
        numNeighbors.setValue(DEFAULT_NUM_NEIGHBORS);
        numNeighbors.addActionListener(parameterActionListener);
        
        weightType = new JComboBox();
        weightType.setMaximumSize(new Dimension(145, 20));
        weightType.setMinimumSize(new Dimension(145, 20));
        weightType.setMaximumSize(new Dimension(145, 20));
        weightType.addItem("none");
        weightType.addItem("one-over-k");
        weightType.addItem("distance");
        weightType.addActionListener(parameterActionListener);
     
        distanceMeasure = new JComboBox();
        distanceMeasure.setMaximumSize(new Dimension(145, 20));
        distanceMeasure.setMinimumSize(new Dimension(145, 20));
        distanceMeasure.setMaximumSize(new Dimension(145, 20));
        distanceMeasure.addItem("Cosine");
        distanceMeasure.addItem("Euclidean");
        distanceMeasure.addActionListener(parameterActionListener);
    }

    public int getNumFeatures()
    {
        return ((Integer)numFeatures.getValue()).intValue();
    }

    public void setNumFeatures(int n)
    {
        numFeatures.setValue(n);
    }

    public String getFeatureFile()
    {
        return this.featureFile;
    }

    public String getStatistic()
    {
        return (String)statistic.getSelectedItem();
    }

    public void setStatistic(String s)
    {
        statistic.setSelectedItem(s);
    }

    public String getMinStdDev()
    {
        return  (String)minStdDev.getValue();
    }
    public void setMinStdDev(String value)
    {
        minStdDev.setValue(value);
    }

    public boolean useMinStdDev()
    {
        return minStdDevCheckbox.isSelected();
    }
    
    public void setUseMinStdDev(boolean b)
    {
        minStdDevCheckbox.setSelected(b);
    }

    public boolean useMedian()
    {
        return medianCheckbox.isSelected();
    }
    
    public void setUseMedian(boolean b)
    {
        medianCheckbox.setSelected(b);
    }
    
    public boolean useFeatureFileMethod()
    {
        return(featureFileMethod.isSelected());
    }
    public void setUseFeatureFileMethod(boolean b)
    {
        featureFileMethod.setSelected(b);
    }

    public String getWeightType()
    {
        return (String)weightType.getSelectedItem();
    }
    public void setWeightType(String s)
    {
        weightType.setSelectedItem(s);
    }

    public String getDistanceMeasure()
    {
        return (String)distanceMeasure.getSelectedItem();
    }
    public void setDistanceMeasure(String s)
    {
        distanceMeasure.setSelectedItem(s);
    }

    public int getNumNeighbors()
    {
        return ((Integer)numNeighbors.getValue()).intValue();
    }

    public void setNumNeighbors(int n)
    {
        numNeighbors.setValue(n);
    }

    protected String getSummaryFile()
    {
        return KNNTrainingPanel.class.getResource("help.html").getPath();
    }

    protected String getParamDescriptFile()
    {
        return KNNTrainingPanel.class.getResource("paramDesc.html").getPath();
    }

    protected JPanel getParameterPanel()
    {
        FormLayout layout = new FormLayout(
                    "right:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(70dlu;pref),7dlu, max(70dlu;pref)",
                    "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        
        builder.appendSeparator("K-Nearest Neighbor Parameters");
        builder.nextRow();

        builder.appendColumn(new ColumnSpec("25dlu"));
        builder.append(numFeatureMethod, numFeatures);

        // add the GenePattern logo
        builder.setColumn(7);
        builder.add(getGPLogo());
        builder.nextRow();

        builder.append("feature selection statistic", statistic);
        builder.nextRow();

        builder.append(minStdDevCheckbox, minStdDev, medianCheckbox);
        builder.nextRow();

        builder.append(featureFileMethod, featureFileTextBox, loadFeatureFileButton);
        builder.nextRow();

        builder.append("num neighbors", numNeighbors);
        builder.nextRow();

        builder.append("neighbor weight type", weightType);
        builder.append("distance measure", distanceMeasure);

        return builder.getPanel();
    }

    protected CSClassifier trainForValidation(java.util.List<float[]> trainingCaseData, java.util.List<float[]> trainingControlData) throws ClassifierException
    {
        ParamValidationResults validationResults = validateParameters();
        if(!validationResults.isValid())
            throw new ClassifierException(validationResults.getMessage());

        setTrainingTask(knnTraining);

        List<String> caseArrayNames = new ArrayList();
        for(int i = 0; i < trainingCaseData.size(); i++)
        {
            caseArrayNames.add("Case_" + i);
        }

        List<String> controlArrayNames = new ArrayList();
        for(int i = 0; i < trainingControlData.size(); i++)
        {
            controlArrayNames.add("Control_" + i);
        }

        DSItemList markers = getActiveMarkers();

        List featureNames = new ArrayList();
        for(int i =0; i < markers.size();i++)
        {
            featureNames.add(((DSGeneMarker)markers.get(i)).getLabel());
        }

        return knnTraining.trainClassifier(trainingCaseData, trainingControlData, featureNames, caseArrayNames, controlArrayNames);
    }

    public ParamValidationResults validateParameters()
    {
        Set uniqueMarkers = new HashSet(getActiveMarkers());
        int numFeatures = uniqueMarkers.size();
        if(!useFeatureFileMethod() && getNumFeatures() <= 0)
            return new ParamValidationResults(false, "num features must be greater than 0");
        else if(!useFeatureFileMethod() && getNumFeatures() > numFeatures)
            return new ParamValidationResults(false, "num features cannot be greater than \nnumber of markers: "
                    + numFeatures);
        else if(useMinStdDev())
        {
            if(getMinStdDev() == null || getMinStdDev().equals(""))
                return new ParamValidationResults(false, "no value given for min std dev");

            double m;
            try
            {
                m = Double.parseDouble(getMinStdDev());

                if(m <= 0)
                    return new ParamValidationResults(false, "min std dev must be greater than 0");
            }
            catch(NumberFormatException ne)
            {
                return new ParamValidationResults(false, "Invalid min std dev: " + getMinStdDev());
            }

             if(m <= 0)
                    return new ParamValidationResults(false, "min std dev must be greater than 0");
        }
        else if(getNumNeighbors() <= 0)
            return new ParamValidationResults(false, "num neighbors must be greater than 0");

        return new ParamValidationResults(true, "KNN Parameter validations passed");
    }

    private void featureFileLoadHandler()
    {
        String lwd = LoadData.getLastDataDirectory();
        featureFileChooser.setCurrentDirectory(new File(lwd));
        featureFileChooser.showOpenDialog(this);
        File file = featureFileChooser.getSelectedFile();
        if (file != null) {
            featureFile = featureFileChooser.getSelectedFile().getAbsolutePath();
            featureFileTextBox.setText(featureFile);
        }
    }

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    public void setParameters(Map<Serializable, Serializable> parameters){
    	if (parameters==null){
    		return;
    	}
    	if (getStopNotifyAnalysisPanelTemporaryFlag()==true) return;
    	stopNotifyAnalysisPanelTemporary(true);

        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();

        
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			

			if (key.equals("numFeatureMethod")){
				this.numFeatureMethod.setSelected((Boolean)value);
			}
			if (key.equals("numFeatures")){
				setNumFeatures((Integer)value);
			}
			if (key.equals("statistic")){
				setStatistic((String)value);
			}
			if (key.equals("useMedian")){
				setUseMedian((Boolean)value);
			}
			if (key.equals("useMinStdDev")){
				setUseMinStdDev((Boolean)value);
			}
			if (key.equals("minStdDev")){
				setMinStdDev((String)value);
			}
			if (key.equals("featureFileMethod")){
				setUseFeatureFileMethod((Boolean)value);
			}
			if (key.equals("featureFile")){
				featureFile = ((String)value);
			}
			if (key.equals("numNeighbors")){
				setNumNeighbors((Integer)value);
			}
	        String distanceMeasure = getDistanceMeasure();
			if (key.equals("weightType")){
				setWeightType((String)value);
			}
			if (key.equals("distanceMeasure")){
				setDistanceMeasure((String)value);
			}
            if (key.equals("numFolds")){
				this.numberFolds.setValue(value);
			}
		}
		stopNotifyAnalysisPanelTemporary(false);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
    public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();

		parameters.put("numFeatureMethod", this.numFeatureMethod.isSelected());
		parameters.put("numFeatures", getNumFeatures());
		parameters.put("statistic", getStatistic());
		parameters.put("useMedian", useMedian());
		parameters.put("useMinStdDev", useMinStdDev());
		parameters.put("minStdDev", getMinStdDev());
		parameters.put("featureFileMethod", useFeatureFileMethod());
		parameters.put("featureFile", featureFile);
		parameters.put("numNeighbors", getNumNeighbors());
		parameters.put("weightType", getWeightType());
		parameters.put("distanceMeasure", getDistanceMeasure());
        parameters.put("numFolds", getNumberFolds());

		return parameters;
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}

}
