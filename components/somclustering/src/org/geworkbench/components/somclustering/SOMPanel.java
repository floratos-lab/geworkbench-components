package org.geworkbench.components.somclustering;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

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
 * SOM analysis
 *
 * @author First Genetic Trust
 * @version $Id$
 */
public class SOMPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = 8945772233173480884L;

	private static final String NEWLINE = "\n";
	
	private static final String GAUSSIAN = "Gaussian";
	private static final String BUBBLE = "Bubble";
	private static final String[] FUNCTIONS = { BUBBLE, GAUSSIAN };

	private static final String ROWS = "rows";
	private static final String COLUMNS = "columns";
	private static final String RADIUS = "radius";
	private static final String ITERATIONS = "iterations";
	private static final String ALPHA = "alpha";
	private static final String FUNCTION = "function";

    /* Human readable text */
    private static final String ROWS_HR = "Rows: ";
	private static final String COLUMNS_HR = "Columns: ";
	private static final String RADIUS_HR = "Radius: ";
	private static final String ITERATIONS_HR = "Iterations: ";
	private static final String ALPHA_HR = "Alpha: ";
    private static final String FUNCTION_HR = "Function: ";
    
	/**
     * Visual Widget
     */
    private JLabel rowLabel = new JLabel("Grid");
    /**
     * Visual Widget
     */
    private BorderLayout borderLayout1 = new BorderLayout();
    /**
     * Visual Widget
     */
    private JComboBox function = new JComboBox();
    /**
     * Visual Widget
     */
    private JLabel functionLabel = new JLabel("Initial Representatives");
    /**
     * Visual Widget
     */
    private JFormattedTextField columns = new JFormattedTextField();
    /**
     * Visual Widget
     */
    private JLabel columnLabel = new JLabel("Number of Updates");
    /**
     * Visual Widget
     */
    private JFormattedTextField iterations = new JFormattedTextField();
    /**
     * Visual Widget
     */
    private JLabel iterationsLabel = new JLabel("Amount of Change");
    /**
     * Visual Widget
     */
    private JFormattedTextField radius = new JFormattedTextField();
    /**
     * Visual Widget
     */
    private JFormattedTextField rows = new JFormattedTextField();
    /**
     * Visual Widget
     */
    private JLabel alphaLabel = new JLabel();
    /**
     * Visual Widget
     */
    private JFormattedTextField alpha = new JFormattedTextField();

    /*
     * (non-Javadoc)
     * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
     */
    public void setParameters(Map<Serializable, Serializable> parameters){
        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if (key.equals(ROWS)){
				this.rows.setValue(value);
			}
			if (key.equals(COLUMNS)){
				this.columns.setValue(value);
			}
			if (key.equals(RADIUS)){
				this.radius.setValue(value);
			}
			if (key.equals(ITERATIONS)){
				this.iterations.setValue(value);
			}
			if (key.equals(ALPHA)){
				this.alpha.setValue(value);
			}
			if (key.equals(FUNCTION)){
				this.function.setSelectedIndex((Integer)value);
			}
		}
    }

    /*
     * (non-Javadoc)
     * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
     */
    public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		parameters.put(ROWS, (Number)rows.getValue());
		parameters.put(COLUMNS, (Number)columns.getValue());
		parameters.put(RADIUS, (Number)radius.getValue());
		parameters.put(ITERATIONS, (Number)iterations.getValue());
		parameters.put(ALPHA, (Number)alpha.getValue());
		parameters.put(FUNCTION, function.getSelectedIndex());
		return parameters;
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

		Number rows = (Number) getParameters().get(ROWS);
		parameters.put(ROWS_HR, rows);

		Number columns = (Number) getParameters().get(COLUMNS);
		parameters.put(COLUMNS_HR, columns);

		Number radius = (Number) getParameters().get(RADIUS);
		parameters.put(RADIUS_HR, radius);

		Number iterations = (Number) getParameters().get(ITERATIONS);
		parameters.put(ITERATIONS_HR, iterations);

		Number alpha = (Number) getParameters().get(ALPHA);
		parameters.put(ALPHA_HR, alpha);

		int function = (Integer) getParameters().get(FUNCTION);
		parameters.put(FUNCTION_HR, FUNCTIONS[function]);

		return parameters;
	}

	
	// getDataSetHistory() is general enough to be moved to
	// AbstractSaveableParameterPanel.java after getHumanReadableParameters() moved.
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
		// Header
		histStr += "SOM Clustering Analysis run with parameters:"
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

    /**
     * Default Constructor
     */
    public SOMPanel() {
        super();
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
    private void jbInit() {
        this.setLayout(borderLayout1);

        rowLabel.setBorder(BorderFactory.createEtchedBorder());
        rowLabel.setText("Number of Rows");
        rows.setValue(new Integer(3));
        functionLabel.setBorder(BorderFactory.createEtchedBorder());
        functionLabel.setText("Function");
        columnLabel.setBorder(BorderFactory.createEtchedBorder());
        columnLabel.setText("Number of Columns");
        columns.setValue(new Integer(3));
        iterationsLabel.setBorder(BorderFactory.createEtchedBorder());
        iterationsLabel.setText("Iterations");
        iterations.setValue(new Integer(4000));

        radius.setValue(new Float(3.0));
        alphaLabel.setText("Learning rate (Alpha)");
        alphaLabel.setBorder(BorderFactory.createEtchedBorder());
        alpha.setValue(new Float(0.8));
        alpha.setBorder(BorderFactory.createEtchedBorder());
//        this.add(jPanel1, BorderLayout.CENTER);
        function.addItem(BUBBLE);
        function.addItem(GAUSSIAN);
        function.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String selectedItem = (String) cb.getSelectedItem();
                if (selectedItem.equals(BUBBLE)) {
                	radius.setEnabled(true);
                } else {
                	radius.setEnabled(false);
                }
            }
        });

        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        
        rows.addActionListener(parameterActionListener);
        columns.addActionListener(parameterActionListener);
        iterations.addActionListener(parameterActionListener);
        radius.addActionListener(parameterActionListener);
        alpha.addActionListener(parameterActionListener);
        function.addActionListener(parameterActionListener);

        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 3dlu, 70dlu, 7dlu, "
              + "right:max(40dlu;pref), 3dlu, 70dlu",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();


        builder.appendSeparator("SOM Analysis Parameters");

        builder.append("Number of Rows", rows);
        builder.append("Number of Columns", columns);
        builder.nextLine();

        builder.append("Radius", radius);
        builder.append("Iterations", iterations);
        builder.nextLine();

        builder.append("Learning Rate (Alpha)", alpha);
        builder.append("Function", function);
        builder.nextLine();

        this.add(builder.getPanel());
    }

    /**
     * Gets the number of rows in the initial SOM Grid
     *
     * @return number of rows in the initial SOM Grid
     */
    public int getRows() {
        return ((Number) rows.getValue()).intValue();
    }

    /**
     * Gets the number of columns in the initial SOM Grid
     *
     * @return number of columns in the initial SOM Grid
     */
    public int getColumns() {
        return ((Number) columns.getValue()).intValue();
    }

    /**
     * Gets the number of iterations for the SOM analysis
     *
     * @return number of iterations for the SOM analysis
     */
    public int getIterations() {
        return ((Number) iterations.getValue()).intValue();
    }

    /**
     * Get radius of the SOM clusters
     *
     * @return radius of the SOM clusters
     */
    public float getRadius() {
        return ((Number) radius.getValue()).floatValue();
    }

    /**
     * Gets the Adaptation function
     *
     * @return Adaptation function
     */
    public String getFunction() {
        return (String) function.getSelectedItem();
    }

    /**
     * Gets the learning rate
     *
     * @return learning rate
     */
    public float getLearningRate() {
        return ((Number) alpha.getValue()).floatValue();
    }

    /**
     * Validates if the parameters to be passed to the analysis routine are indeed
     * valid
     *
     * @return <code>ParamValidationResults</code> containing results of
     *         validation
     */
    public ParamValidationResults validateParameters() {
        if (getRows() <= 0)
            return new ParamValidationResults(false, "Number of Rows has to be greater than 0");
        else if (getColumns() <= 0)
            return new ParamValidationResults(false, "Number of Columns has to be greater than 0");
        else if (getIterations() <= 0)
            return new ParamValidationResults(false, "Number of Iterations has to be greater than 0");
        else if (getRadius() <= 0.0)
            return new ParamValidationResults(false, "Radius has to be greater than 0");
        else if (getLearningRate() <= 0f || getLearningRate() > 1f)
            return new ParamValidationResults(false, "Learning Rate has to be between 0 and 1");
        return new ParamValidationResults(true, "SOM Parameter validations passed");
    }

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}

}