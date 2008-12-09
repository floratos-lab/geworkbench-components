package org.geworkbench.components.hierarchicalclustering;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * Component to receive user input regarding the parameters to be used for
 * Hierarchical clustering analysis
 *
 * @author First Genetic Trust
 * @version 1.0
 */
public class HierClustPanel extends AbstractSaveableParameterPanel implements Serializable {
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
     * Visual Widget
     */
    private FlowLayout flowLayout1 = new FlowLayout();

    private static class SerializedInstance implements Serializable {
        private int metric;
        private int dimension;
        private int method;

        public SerializedInstance(int metric, int dimension, int method) {
            this.metric = metric;
            this.dimension = dimension;
            this.method = method;
        }

        Object readResolve() throws ObjectStreamException {
            HierClustPanel panel = new HierClustPanel();
            panel.metric.setSelectedIndex(metric);
            panel.dimension.setSelectedIndex(dimension);
            panel.method.setSelectedIndex(method);
            return panel;
        }
    }

    Object writeReplace() throws ObjectStreamException {
        return new SerializedInstance(metric.getSelectedIndex(), dimension.getSelectedIndex(), method.getSelectedIndex());
    }

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
        metric.addItem("Euclidean");
        metric.addItem("Pearson's");
        metric.addItem("Spearman's");
//        this.add(jPanel1, null);

        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 3dlu, 70dlu, 7dlu",
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

    /**
     * {@link java.io.Serializable} method
     *
     * @param out <code>ObjectOutputStream</code>
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        //    out.writeObject((String)method.getSelectedItem());
        //    out.writeObject((String)dimension.getSelectedItem());
        //    out.writeObject((String)metric.getSelectedItem());
    }

    /**
     * {@link java.io.Serializable} method
     *
     * @param in <code>ObjectInputStream</code>
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        //    method.setSelectedItem((String)in.readObject());
        //    dimension.setSelectedItem((String)in.readObject());
        //    metric.setSelectedItem((String)in.readObject());
        revalidate();
    }

}
