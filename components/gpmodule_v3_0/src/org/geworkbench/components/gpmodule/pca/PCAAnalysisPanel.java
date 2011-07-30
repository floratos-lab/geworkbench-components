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

import java.awt.BorderLayout;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;

import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.geworkbench.components.gpmodule.GPAnalysisPanel;
import org.geworkbench.events.listeners.ParameterActionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author: Marc-Danie Nazaire
 * @author yc2480
 * @version $Id$
 */
public class PCAAnalysisPanel extends GPAnalysisPanel  
{
	private static final long serialVersionUID = -2258456667092378338L;
	
	private JComboBox variables;
    
    public PCAAnalysisPanel()
     {
        super(new ParameterPanel(), "PCA");
         
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

        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        
        variables.addActionListener(parameterActionListener);

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

    protected URL getParamDescriptionFile()
    {
        return PCAAnalysisPanel.class.getResource("paramDesc.html");
    }

    protected URL getDescriptionFile()
    {
        return PCAAnalysisPanel.class.getResource("help.html");
    }
    
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    public void setParameters(Map<Serializable, Serializable> parameters){
        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if (key.equals("variables")){
				setVariables((String)value);
			}
		}
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

		parameters.put("variables", getVariables());
		return parameters;
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}

}