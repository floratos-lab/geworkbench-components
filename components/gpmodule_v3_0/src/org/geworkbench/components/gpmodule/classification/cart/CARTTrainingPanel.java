/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2010) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.gpmodule.classification.cart;

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
 */
public class CARTTrainingPanel extends GPTrainingPanel
{
    private CARTTraining cartTraining;

    public CARTTrainingPanel(CARTTraining cartTraining)
    {
        super(cartTraining.getLabel());
        this.cartTraining = cartTraining;
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
        builder.addSeparator("Classification and Regression Trees Parameters");
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
        return CARTTrainingPanel.class.getResource("help.html").getPath();
    }

    protected CSClassifier trainForValidation(java.util.List<float[]> trainingCaseData, java.util.List<float[]> trainingControlData) throws ClassifierException
    {
        return null;
    }

    public void setParameters(Map<Serializable, Serializable> parameter){
    	//for CART, nothing to set
    	return;
    }
    public Map<Serializable, Serializable> getParameters(){
    	//for CART, nothing to save
    	return new HashMap<Serializable, Serializable>();
    }

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	}
}
