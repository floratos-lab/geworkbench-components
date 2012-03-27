package org.geworkbench.components.hierarchicalclustering;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.events.listeners.ParameterActionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * Component to receive user input regarding the parameters to be used for
 * Hierarchical clustering analysis
 *
 * @author First Genetic Trust
 * @version $Id$
 */
public class HierClustPanel extends AbstractSaveableParameterPanel{
	private static final String NEWLINE = "\n";
	
	/* identification text string for parameter saving. */
    private static final String METHOD = "method";
	private static final String DIMENSION = "dimension";
	private static final String METRIC = "metric";

	private static final String METHOD_HR = "Clustering Method: ";
	private static final String DIMENSION_HR = "Clustering Dimension: ";
	private static final String METRIC_HR = "Clustering Metric: ";

	private static final String METRICS[] = { "euclidean", "pearson", "spearman" };
	private static final String METHODS[] = { "single", "average", "complete" };
	private static final String DIMENSIONS[] = { "marker", "microarray", "both" };

	/**
	 * 
	 */
	private static final long serialVersionUID = -3787482991189174700L;

    /**
     * Visual Widget
     */
    private JComboBox metric = new JComboBox();
    private JComboBox dimension = new JComboBox();
    private JComboBox method = new JComboBox();
    

    /**
     * Default Constructor
     */
    public HierClustPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Configures the Graphical User Interface and Listeners
     *
     * @throws Exception
     */
    private void jbInit() throws Exception {
        this.setLayout(new BorderLayout());

        method.addItem("Single Linkage");
        method.addItem("Average Linkage");
        method.addItem("Total Linkage");
        dimension.addItem("Marker");
        dimension.addItem("Microarray");
        dimension.addItem("Both");
        metric.addItem("Euclidean Distance");
        metric.addItem("Pearson's Correlation");
        metric.addItem("Spearman's Rank Correlation");

        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        
        method.addActionListener(parameterActionListener);
        dimension.addActionListener(parameterActionListener);
        metric.addActionListener(parameterActionListener);

        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 3dlu, 100dlu, 7dlu",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Hierarchical Clustering Parameters");

        builder.append("Clustering Method", method);
        builder.append("Clustering Dimension", dimension);
        builder.append("Clustering Metric", metric);
        this.add(builder.getPanel());
    }
    
    /**
     * Gets the currently selected hierarchical clustering method
     *
     * @return currently selected hierarchical clustering method
     */
    public int getMethod() {
        return method.getSelectedIndex();
    }

    /**
     * Gets the dimension of clustering, if marker, microarray or both
     *
     * @return dimension of clustering, if marker, microarray or both
     */
    public int getDimension() {
        return dimension.getSelectedIndex();
    }

    /**
     * Gets the distance metric to be used for clustering
     *
     * @return distance metric to be used for clustering
     */
    public int getDistanceMetric() {
        return metric.getSelectedIndex();
    }

    /**
     * Validates if the parameters to be passed to the analysis routine are indeed
     * valid
     *
     * @return <code>ParamValidationResults</code> containing results of
     *         validation
     */
    public ParamValidationResults validateParameters() {
        return new ParamValidationResults(true, "Hierarchical Clustering Parameter validations passed");
    }

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    public void setParameters(Map<Serializable, Serializable> parameters){
    	if (parameters == null) return;
        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if (key.equals(METRIC)){
				this.metric.setSelectedIndex((Integer)value);
			}
			if (key.equals(DIMENSION)){
				this.dimension.setSelectedIndex((Integer)value);
			}
			if (key.equals(METHOD)){
				this.method.setSelectedIndex((Integer)value);
			}
		}
    }
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getDataSetHistory()
	 */
	@Override
	public String getDataSetHistory() {
		/* translate between machine index to human readable text. */
		String histStr = "";
		Map<Serializable, Serializable> pMap = getParameters();
		// Header, could be moved to AbstractAnalysis.java
		histStr += "Hierarchical Clustering Analysis run with parameters:"
				+ NEWLINE;
		histStr += "----------------------------------------"
				+ NEWLINE;
		for (Iterator<Map.Entry<Serializable, Serializable>> iterator = pMap
				.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			histStr += key.toString() + value.toString() + NEWLINE;
		}
		return histStr;
	}
  
	public Map<Serializable, Serializable> getParameters() {
		
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();

		parameters.put(METHOD_HR, METHODS[method.getSelectedIndex()]);

		parameters.put(DIMENSION_HR, DIMENSIONS[dimension.getSelectedIndex()]);

		parameters.put(METRIC_HR, METRICS[metric.getSelectedIndex()]);

		return parameters;
	}
	
	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}
}
