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
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * @author: Marc-Danie Nazaire
 */
public class PCAAnalysisPanel extends GPAnalysisPanel implements Serializable 
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

         FormLayout layout = new FormLayout(
                    "right:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(95dlu;pref),7dlu, max(70dlu;pref)",
                    "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Principal Components Analysis Parameters");
        builder.nextLine();

        builder.append("variables", variables);

        // add the GenePattern logo
        builder.setColumn(7);
        builder.add(getGPLogo());
        builder.nextRow();

        parameterPanel.add(builder.getPanel(), BorderLayout.WEST);
        
    }

    public String getVariables()
    {
        return (String) variables.getSelectedItem();
    }
    
    public void setVariables(String s){
    	variables.setSelectedItem(s);
    }

    protected String getParamDescriptionFile()
    {
        return PCAAnalysisPanel.class.getResource("paramDesc.html").getPath();
    }

    protected String getDescriptionFile()
    {
        return PCAAnalysisPanel.class.getResource("paramDesc.html").getPath();
    }
    
    private static class SerialInstance implements Serializable {
    	private String variables;
    	
    	public SerialInstance(String variables){
    		this.variables = variables;
    	}
    	
    	Object readResolve() throws ObjectStreamException {
    		PCAAnalysisPanel result = new PCAAnalysisPanel();
    		result.setVariables(this.variables);		
    		return result;
    	}
    }
    
    Object writeReplace() throws ObjectStreamException {
    	return new SerialInstance(
    			getVariables()
    			);
    }
}