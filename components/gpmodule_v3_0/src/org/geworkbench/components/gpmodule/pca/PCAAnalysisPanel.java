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
package org.geworkbench.components.gpmodule.pca;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.components.gpmodule.GPAnalysisPanel;

import javax.swing.*;
import java.awt.*;

/**
 * @author: Marc-Danie Nazaire
 */
public class PCAAnalysisPanel extends GPAnalysisPanel
{
    private JComboBox variables;
    
    public PCAAnalysisPanel()
     {
        super(new AbstractSaveableParameterPanel(), "PCA");
         
        try
        {
            init();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void initParameterPanel()
    {
        variables = new JComboBox();
        variables.addItem("genes");
        variables.addItem("experiments");

        FormLayout formLayout = new FormLayout("right:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(70dlu;pref)");

        DefaultFormBuilder builder = new DefaultFormBuilder(formLayout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Principal Components Analysis parameters");
        builder.nextLine();

        builder.append("variables", variables);

        parameterPanel.add(builder.getPanel(), BorderLayout.LINE_START);
    }

    public String getVariables()
    {
        return (String) variables.getSelectedItem();
    }

    protected String getParamDescriptionFile()
    {
        return PCAAnalysisPanel.class.getResource("paramDesc.html").getPath();
    }

    protected String getDescriptionFile()
    {
        return PCAAnalysisPanel.class.getResource("paramDesc.html").getPath();
    }
}