package org.geworkbench.components.somclustering;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
 * @version $Id: SOMPanel.java,v 1.5 2009-06-19 19:25:05 jiz Exp $
 */
public class SOMPanel extends AbstractSaveableParameterPanel {
    /**
     * Visual Widget
     */
    private JLabel rowLabel = new JLabel("Grid");
    /**
     * Visual Widget
     */
    private JPanel jPanel1 = new JPanel();
    /**
     * Visual Widget
     */
    private BorderLayout borderLayout1 = new BorderLayout();
    /**
     * Visual Widget
     */
    private GridLayout gridLayout1 = new GridLayout();
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
    private JLabel radiusLabel = new JLabel("Radii of Neighborhood");
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
			if (key.equals("rows")){
				this.rows.setValue(value);
			}
			if (key.equals("columns")){
				this.columns.setValue(value);
			}
			if (key.equals("radius")){
				this.radius.setValue(value);
			}
			if (key.equals("iterations")){
				this.iterations.setValue(value);
			}
			if (key.equals("alpha")){
				this.alpha.setValue(value);
			}
			if (key.equals("function")){
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
		parameters.put("rows", (Number)rows.getValue());
		parameters.put("columns", (Number)columns.getValue());
		parameters.put("radius", (Number)radius.getValue());
		parameters.put("iterations", (Number)iterations.getValue());
		parameters.put("alpha", (Number)alpha.getValue());
		parameters.put("function", function.getSelectedIndex());
		return parameters;
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
//        jPanel1.setLayout(gridLayout1);
//        jPanel1.setMinimumSize(new Dimension(281, 75));
//        jPanel1.setPreferredSize(new Dimension(281, 75));
//        jPanel1.setToolTipText("");

//        this.setMinimumSize(new Dimension(281, 75));
//        this.setPreferredSize(new Dimension(281, 75));
        
//        gridLayout1.setColumns(4);
//        gridLayout1.setRows(3);
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
        radiusLabel.setBorder(BorderFactory.createEtchedBorder());
        radiusLabel.setToolTipText("");
        radiusLabel.setText("Radius");
        radius.setValue(new Float(3.0));
        alphaLabel.setText("Learning rate (Alpha)");
        alphaLabel.setBorder(BorderFactory.createEtchedBorder());
        alpha.setValue(new Float(0.8));
        alpha.setBorder(BorderFactory.createEtchedBorder());
//        this.add(jPanel1, BorderLayout.CENTER);
        function.addItem("Bubble");
        function.addItem("Gaussian");
        function.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String selectedItem = (String) cb.getSelectedItem();
                if (selectedItem.equals("Bubble")) {
                	radiusLabel.setEnabled(true);
                	radius.setEnabled(true);
                } else {
                	radiusLabel.setEnabled(false);
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