
package org.geworkbench.components.gpmodule.kmeans;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.geworkbench.components.gpmodule.GPAnalysisPanel;
import org.geworkbench.util.ClassifierException;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author zm2165 
 * @version $Id$
 */
public class KMeansPanel extends GPAnalysisPanel
{   
	
	private static final long serialVersionUID = 664252701591315663L;
	private JTextField numClusters=null;
    private JComboBox clusterBy=null;
    private JComboBox distanceMetric=null;

    public KMeansPanel()
    {
       super(new ParameterPanel(), "KMeans");
        
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
 
    	numClusters=new JTextField(3);
    	numClusters.setText("6");	//default value of number of clusters
    	clusterBy = new JComboBox();
        clusterBy.addItem("Genes");
        clusterBy.addItem("Arrays");
        distanceMetric=new JComboBox();
        distanceMetric.addItem("Euclidean");
    	
    	FormLayout layout = new FormLayout(
                "right:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(25dlu;pref),7dlu, max(100dlu;pref)",
                "");
	    DefaultFormBuilder builder = new DefaultFormBuilder(layout);
	    builder.setDefaultDialogBorder();
	    
	    builder.appendSeparator("K-Means Clustering Parameters");
	    builder.nextRow(); 
	
	    builder.appendColumn(new ColumnSpec("25dlu"));
	    builder.append("Number of clusters", numClusters); 
	    // add the GenePattern logo
	    builder.setColumn(7);
	    builder.add(getGPLogo());
	   
	    builder.nextRow();
	    builder.append("Cluster by", clusterBy);
	    builder.nextRow();
	    builder.append("Distance metric", distanceMetric);
	
	    parameterPanel.add(builder.getPanel(), BorderLayout.WEST);
        
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
			if (key.equals("numClusters")){
				numClusters.setText((String)value);
			}
			if (key.equals("clusterBy")){
				clusterBy.setSelectedItem((String)value);
			}
			if (key.equals("distanceMetric")){
				distanceMetric.setSelectedItem((String)value);
			}
		}
    }
    
    public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();

		parameters.put("numClusters", numClusters.getText());
		parameters.put("clusterBy", (String) clusterBy.getSelectedItem());
		parameters.put("distanceMetric", (String) distanceMetric.getSelectedItem());
		return parameters;
	}
    
    
    public String getNumClusters(){
    	return numClusters.getText();
    }
    
    public int getClusterBy(){
    	return clusterBy.getSelectedIndex();
    }

    protected String getParamDescriptFile()
    {
    	return KMeansPanel.class.getResource("help.html").getPath();
    }

    protected String getSummaryFile()
    {
        return KMeansPanel.class.getResource("help.html").getPath();    	
    }

    protected CSClassifier trainForValidation(java.util.List<float[]> trainingCaseData, java.util.List<float[]> trainingControlData) throws ClassifierException
    {
        return null;    
    }  
    
	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}

	 protected String getDescriptionFile()
	    {
	        return KMeansPanel.class.getResource("help.html").getPath();
	    }
	    

	protected String getParamDescriptionFile()
    {
        return KMeansPanel.class.getResource("help.html").getPath();
    }

	
}
