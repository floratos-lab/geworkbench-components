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
package org.geworkbench.components.gpmodule.classification.kmeans;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.components.gpmodule.classification.GPTrainingPanel;
import org.geworkbench.util.ClassifierException;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * @author Marc-Danie Nazaire
 * @version $Id: SVMTrainingPanel.java,v 1.7 2009-06-19 19:18:48 jiz Exp $
 */
public class KMeansPanel extends GPTrainingPanel
{
    private KMeansClustering svmTraining;
    private JTextField numClusters=null;
    private JComboBox clusterBy=null;
    private JComboBox distanceMetric=null;

    public KMeansPanel(KMeansClustering svmTraining)
    {
        super(svmTraining.getLabel());
        this.svmTraining = svmTraining;
        try
        {   jbInit();   }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void initUI()
    {
    	numClusters=new JTextField(3);
    	numClusters.setText("7");
    	clusterBy = new JComboBox();
        clusterBy.addItem("genes");
        clusterBy.addItem("arrays");
        distanceMetric=new JComboBox();
        distanceMetric.addItem("Euclidean");
    }

    public void rebuildForm()
    {
        removeAll();
        FormLayout layout = new FormLayout(
        		"right:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(35dlu;pref),7dlu, max(95dlu;pref)",
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
   	/*
        FormLayout layout = new FormLayout(
         "right:max(140dlu;pref), " // 1st major colum 140
            + "right:max(130dlu;pref)",        // 2nd major column 130        		
         "");

        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendRow(new RowSpec("25dlu"));
        builder.addSeparator("K-Means Clustering Parameters");
        builder.nextLine();
        
        builder.append("variables", variables);

        builder.appendRow(new RowSpec("25dlu"));        
      
        CellConstraints cc = new CellConstraints();
        builder.add(getGPLogo(), cc.xy(3, builder.getRow()));
     
        builder.nextLine();
        */
    	FormLayout layout = new FormLayout(
                "right:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(25dlu;pref),7dlu, max(100dlu;pref)",
                "");
    DefaultFormBuilder builder = new DefaultFormBuilder(layout);
    builder.setDefaultDialogBorder();
    
    builder.appendSeparator("K-Means Clustering Parameters");
    builder.nextRow(); 

    builder.appendColumn(new ColumnSpec("25dlu"));
    builder.append("number of clusters", numClusters); 
 // add the GenePattern logo
    builder.setColumn(7);
    builder.add(getGPLogo());
   
    builder.nextRow();
    builder.append("variables", clusterBy);
    builder.nextRow();
    builder.append("distance metric", distanceMetric);

    
        return builder.getPanel();
    }
    
    public String getNumClusters(){
    	return numClusters.getText();
    }

    protected String getParamDescriptFile()
    {
        return null;
    }

    protected String getSummaryFile()
    {
        return KMeansPanel.class.getResource("help.html").getPath();    	
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
