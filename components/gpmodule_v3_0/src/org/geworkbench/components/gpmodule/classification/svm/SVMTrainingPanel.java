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
package org.geworkbench.components.gpmodule.classification.svm;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.components.gpmodule.classification.GPTrainingPanel;
import org.geworkbench.util.ClassifierException;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * @author Marc-Danie Nazaire
 * @version $Id: SVMTrainingPanel.java,v 1.7 2009-06-19 19:18:48 jiz Exp $
 */
public class SVMTrainingPanel extends GPTrainingPanel
{
    private SVMTraining svmTraining;

    public SVMTrainingPanel(SVMTraining svmTraining)
    {
        super(svmTraining.getLabel());
        this.svmTraining = svmTraining;
    }

    protected void initUI(){}

    public void rebuildForm()
    {
        removeAll();
        FormLayout layout = new FormLayout(
                "right:max(80dlu;pref), 7dlu, max(70dlu;pref), 7dlu, right:max(70dlu;pref), 7dlu, max(70dlu;pref)",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        addParameters(builder);

        JPanel panel = builder.getPanel();
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        invalidate();
    }

    protected JPanel getParameterPanel()
    {
        FormLayout layout = new FormLayout(
         "right:max(140dlu;pref), " // 1st major colum
            + "right:max(130dlu;pref)",        // 2nd major column
         "");

        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendRow(new RowSpec("25dlu"));
        builder.addSeparator("Support Vector Machine Parameters");
        builder.nextLine();

        builder.appendRow(new RowSpec("25dlu"));
      
        CellConstraints cc = new CellConstraints();
        builder.add(getGPLogo(), cc.xy(2, builder.getRow()));
        builder.nextLine();
        
        return builder.getPanel();
    }

    protected String getParamDescriptFile()
    {
        return null;
    }

    protected String getSummaryFile()
    {
        return SVMTrainingPanel.class.getResource("help.html").getPath();
    }

    protected CSClassifier trainForValidation(java.util.List<float[]> trainingCaseData, java.util.List<float[]> trainingControlData) throws ClassifierException
    {
        return null;    
    }
    
    public void setParameters(Map<Serializable, Serializable> parameter){
    	//for SVM, nothing to set
    	return;
    }
    public Map<Serializable, Serializable> getParameters(){
    	//for SVM, nothing to save
    	return new HashMap<Serializable, Serializable>();
    }

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}
}
