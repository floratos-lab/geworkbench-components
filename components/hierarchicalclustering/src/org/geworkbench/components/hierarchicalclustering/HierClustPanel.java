package org.geworkbench.components.hierarchicalclustering;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * @version $Id: HierClustPanel.java,v 1.5 2009-06-22 15:20:26 chiangy Exp $
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
	private static final long serialVersionUID = 1L;
	private Log log = LogFactory.getLog(this.getClass());
	/**
     * Visual Widget
     */
    private JPanel jPanel1 = new JPanel();
    /**
     * Visual Widget
     */
    private GridLayout gridLayout1 = new GridLayout();
    /**
     * Visual Widget
     */
    private JLabel clusteringMethod = new JLabel();
    /**
     * Visual Widget
     */
    private JComboBox metric = new JComboBox();
    /**
     * Visual Widget
     */
    private JLabel distance = new JLabel();
    /**
     * Visual Widget
     */
    private JComboBox dimension = new JComboBox();
    /**
     * Visual Widget
     */
    private JLabel clusteringDim = new JLabel();
    /**
     * Visual Widget
     */
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
        clusteringDim.setText("Clustering Dimension");
        clusteringDim.setRequestFocusEnabled(true);
        clusteringDim.setBorder(BorderFactory.createEtchedBorder());
        this.setLayout(new BorderLayout());
        jPanel1.setLayout(gridLayout1);
        gridLayout1.setColumns(2);
        gridLayout1.setHgap(0);
        gridLayout1.setRows(3);
        clusteringMethod.setVerticalTextPosition(SwingConstants.CENTER);
        clusteringMethod.setVerticalAlignment(SwingConstants.CENTER);
        clusteringMethod.setText("Clustering Method");
        clusteringMethod.setVerifyInputWhenFocusTarget(true);
        clusteringMethod.setRequestFocusEnabled(true);
        clusteringMethod.setBorder(BorderFactory.createEtchedBorder());
        distance.setBorder(BorderFactory.createEtchedBorder());
        distance.setText("Distance Metric");
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

    public void setMethod(int index) {
        method.setSelectedIndex(index);
    }

    /**
     * Gets the dimension of clustering, if marker, microarray or both
     *
     * @return dimension of clustering, if marker, microarray or both
     */
    public int getDimension() {
        return dimension.getSelectedIndex();
    }

    public void setDimension(int index) {
        dimension.setSelectedIndex(index);
    }

    /**
     * Gets the distance metric to be used for clustering
     *
     * @return distance metric to be used for clustering
     */
    public int getDistanceMetric() {
        return metric.getSelectedIndex();
    }

    public void setDistanceMetric(int index) {
        metric.setSelectedIndex(index);
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

    // This method will be moved to AbstractSaveableParameterPanel.java.
	// PS: getDataSetHistory() should be moved too.
	/**
	 * Currently this method is used to generate dataset history, will be used
	 * by GenSpace and others.
	 * 
	 * @return This method returns human readable parameters. The data should be
	 *         the same as returned from getParameters(), but human readable. If
	 *         getParameters() returns metric:euclidean, this method would
	 *         probably return "Clustering Metric: euclidean"
	 */
	public Map<Serializable, Serializable> getHumanReadableParameters() {
		/*
		 * We use LinkedHashMap to retain the order of parameters, so we can
		 * control the order when displaying it.
		 */
		Map<Serializable, Serializable> parameters = new LinkedHashMap<Serializable, Serializable>();

		int method = (Integer) getParameters().get(METHOD);
		parameters.put(METHOD_HR, METHODS[method]);

		int dimension = (Integer) getParameters().get(DIMENSION);
		parameters.put(DIMENSION_HR, DIMENSIONS[dimension]);

		int metric = (Integer) getParameters().get(METRIC);
		parameters.put(METRIC_HR, METRICS[metric]);

		return parameters;
	}

	
	// getDataSetHistory() is general enough to be moved to
	// AbstractSaveableParameterPanel.java after getHumanReadableParameters()
	// moved.
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getDataSetHistory()
	 */
	@Override
	public String getDataSetHistory() {
		/* translate between machine index to human readable text. */
		String histStr = "";
		Map<Serializable, Serializable> pMap = getHumanReadableParameters();
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
    
    /*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 *      Since HierClustPanel only has three parameters, we return metric,
	 *      dimension and method in the format same as getBisonParameters().
	 */
    public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();

		parameters.put(METRIC, this.metric.getSelectedIndex());
		parameters.put(DIMENSION, this.dimension.getSelectedIndex());
		parameters.put(METHOD, this.method.getSelectedIndex());
		return parameters;
	}

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#clone() We
	 *      create a new HierClustPanel, and copy the parameters, in this case,
	 *      three parameters.
	 */
    @Override
    public HierClustPanel clone(){
        HierClustPanel panel = new HierClustPanel();
        
        log.debug("Clone"+metric.getSelectedIndex()+","+dimension.getSelectedIndex()+","+method.getSelectedIndex());
        panel.metric.setSelectedIndex(metric.getSelectedIndex());
        panel.dimension.setSelectedIndex(dimension.getSelectedIndex());
        panel.method.setSelectedIndex(method.getSelectedIndex());
        return panel;
    }

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}
}
