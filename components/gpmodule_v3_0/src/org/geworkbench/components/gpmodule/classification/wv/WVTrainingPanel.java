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
package org.geworkbench.components.gpmodule.classification.wv;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.builtin.projects.LoadDataDialog;
import org.geworkbench.components.gpmodule.classification.GPTrainingPanel;
import org.geworkbench.events.listeners.ParameterActionListener;
import org.geworkbench.util.ClassifierException;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
/**
 * @author Marc-Danie Nazaire          
 * @version $Id$
 * FIXME: saving parameter set to file not working yet. Only save in memory works.                            
 */
public class WVTrainingPanel extends GPTrainingPanel {

	private static final long serialVersionUID = -8154071395478765299L;

	private static final int DEFAULT_NUM_FEATURES = 10;

    private String featureFile = "";
    private javax.swing.JTextField featureFileTextBox;
    private JButton loadFeatureFileButton;
    private JFileChooser featureFileChooser = new JFileChooser();
    private JFormattedTextField numFeatures;
    private JComboBox statistic;
    private JFormattedTextField minStdDev;
    private JCheckBox medianCheckbox;
    private JCheckBox minStdDevCheckbox;
    private JRadioButton numFeatureMethod;
    private JRadioButton featureFileMethod;
    private WVTraining wvTraining;

    public WVTrainingPanel(WVTraining wvTraining)
    {
        super(wvTraining.getLabel());
        this.wvTraining = wvTraining;
        try
        {   jbInit(); }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void initUI()
    {
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

        numFeatures = new JFormattedTextField();
        numFeatures.setValue(DEFAULT_NUM_FEATURES);

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

        featureFileTextBox = new JTextField();
        featureFileTextBox.setEnabled(false);

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
        statistic.setName("feature selection statistic");
        statistic.addItem("SNR");
        statistic.addItem("T-Test");

        medianCheckbox = new JCheckBox();
        medianCheckbox.setText("median") ;

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

        minStdDev = new JFormattedTextField("");
        minStdDev.setMaximumSize(new Dimension(145, 20));
        minStdDev.setMinimumSize(new Dimension(145, 20));
        minStdDev.setMaximumSize(new Dimension(145, 20));
        minStdDev.setEnabled(false);
    }

    public int getNumFeatures()
    {
        return ((Integer)numFeatures.getValue()).intValue();
    }

    public String getFeatureFile()
    {
        return this.featureFile;
    }

    public String getStatistic()
    {
        return (String)statistic.getSelectedItem();
    }

    public String getMinStdDev()
    {
        return (String)minStdDev.getValue();
    }

    public boolean useMinStdDev()
    {
        return minStdDevCheckbox.isSelected();
    }

    public boolean useMedian()
    {
        return medianCheckbox.isSelected();
    }

    public boolean useFeatureFileMethod()
    {
        return(featureFileMethod.isSelected());
    }

    protected String getSummaryFile()
    {
        return WVTrainingPanel.class.getResource("help.html").getPath();
    }

    protected String getParamDescriptFile()
    {
        return WVTrainingPanel.class.getResource("paramDesc.html").getPath();
    }
    
    protected JPanel getParameterPanel()
    {
        FormLayout layout = new FormLayout(
                    "right:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(70dlu;pref),7dlu, max(70dlu;pref)",
                    "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Weighted Voting Parameters");
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
        builder.nextLine();

        builder.append(featureFileMethod, featureFileTextBox, loadFeatureFileButton);

        ParameterActionListener parameterActionListener = new ParameterActionListener(this);

        numFeatureMethod.addActionListener(parameterActionListener);
        numFeatures.addPropertyChangeListener(parameterActionListener);
        statistic.addActionListener(parameterActionListener);
        medianCheckbox.addActionListener(parameterActionListener);
        minStdDevCheckbox.addActionListener(parameterActionListener);
        minStdDev.addPropertyChangeListener(parameterActionListener);
        featureFileMethod.addActionListener(parameterActionListener);
        featureFileTextBox.addPropertyChangeListener(parameterActionListener);

        return builder.getPanel();
    }

    protected CSClassifier trainForValidation(java.util.List<float[]> trainingCaseData, java.util.List<float[]> trainingControlData) throws ClassifierException
    {
        ParamValidationResults validationResults = validateParameters();
        if(!validationResults.isValid())
            throw new ClassifierException(validationResults.getMessage());

        setTrainingTask(this.wvTraining);

        List<String> caseArrayNames = new ArrayList<String>();
        for(int i = 0; i < trainingCaseData.size(); i++)
        {
            caseArrayNames.add("Case_" + i);
        }

        List<String> controlArrayNames = new ArrayList<String>();
        for(int i = 0; i < trainingControlData.size(); i++)
        {
            controlArrayNames.add("Control_" + i);
        }

        DSItemList<DSGeneMarker> markers = getActiveMarkers();

        List<String> featureNames = new ArrayList<String>();
        for(int i =0; i < markers.size();i++)
        {
            featureNames.add(((DSGeneMarker)markers.get(i)).getLabel());
        }
        
        return wvTraining.trainClassifier(trainingCaseData, trainingControlData, featureNames, caseArrayNames, controlArrayNames);
    }

    public ParamValidationResults validateParameters()
    {
        Set<DSGeneMarker> uniqueMarkers = new HashSet<DSGeneMarker>(getActiveMarkers());
        int numFeatures = uniqueMarkers.size();
        if(!useFeatureFileMethod() && getNumFeatures() <= 0)
            return new ParamValidationResults(false, "num features must be greater than 0");
        else if(!useFeatureFileMethod() && getNumFeatures() > numFeatures)
            return new ParamValidationResults(false, "num features cannot be greater than \nnumber of markers: " + numFeatures);
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

        return new ParamValidationResults(true, "WV Parameter validations passed");
    }

    private void featureFileLoadHandler()
    {
        String lwd = LoadDataDialog.getLastDataDirectory();
        featureFileChooser.setCurrentDirectory(new File(lwd));
        featureFileChooser.showOpenDialog(this);
        File file = featureFileChooser.getSelectedFile();
        if (file != null)
        {
            featureFile = featureFileChooser.getSelectedFile().getAbsolutePath();
            featureFileTextBox.setSelectionEnd(10);
            featureFileTextBox.setText(featureFile);
        }
    }

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    public void setParameters(Map<Serializable, Serializable> parameters){
        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        if (numFeatureMethod==null) initUI();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if (key.equals("numFeatureMethod")){
				this.numFeatureMethod.setSelected((Boolean)value);
			}
			if (key.equals("numFeatures")){
				this.numFeatures.setValue((Integer)value);
			}
			if (key.equals("statistic")){
				this.statistic.setSelectedItem((String)value);
			}
			if (key.equals("useMedian")){
				this.medianCheckbox.setSelected((Boolean)value);
			}
			if (key.equals("useMinStdDev")){
				this.minStdDevCheckbox.setSelected((Boolean)value);
			}
			if (key.equals("minStdDev")){
				this.minStdDev.setValue((String)value);
			}
			if (key.equals("featureFileMethod")){
				this.featureFileMethod.setSelected((Boolean)value);
			}
			if (key.equals("featureFile")){
				this.featureFileTextBox.setText((String)value);
			}
            if (key.equals("numFolds")){
				this.numberFolds.setValue(value);
			}
		}
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
    public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		if (numFeatureMethod==null){
			initUI();
		}
		parameters.put("numFeatureMethod", numFeatureMethod.isSelected());
		parameters.put("numFeatures", (Integer)numFeatures.getValue());
		parameters.put("statistic", (String)statistic.getSelectedItem());
		parameters.put("useMedian", medianCheckbox.isSelected());
		parameters.put("useMinStdDev", minStdDevCheckbox.isSelected());
		parameters.put("minStdDev", (String)minStdDev.getValue());
		parameters.put("featureFileMethod", featureFileMethod.isSelected());
		parameters.put("featureFile", featureFile);
        parameters.put("numFolds", getNumberFolds());

		return parameters;
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}

}



