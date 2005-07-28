package org.geworkbench.components.analysis.classification.svm;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.stat.Descriptive;
import org.geworkbench.util.microarrayutils.MicroarrayViewEventBase;
import org.geworkbench.bison.algorithm.classification.crossvalidation.CVParameter2;
import org.geworkbench.bison.algorithm.classification.crossvalidation.CVParameterMatrix;
import org.geworkbench.bison.algorithm.classification.crossvalidation.CVParameterSelection2;
import org.geworkbench.engine.config.VisualPlugin;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.Vector;

/**
 * <p>Title: caWorkbench</p>
 * <p/>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence
 * and Genotype Analysis</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p/>
 * <p>Company: Columbia University</p>
 *
 * @author Adam Margolin
 * @version 3.0
 */
public class SVMOptimizationPanel extends MicroarrayViewEventBase implements VisualPlugin {
    private DefaultTableCellRenderer tableRenderer = new svmDoubleTableRenderer();

    private class svmDoubleTableRenderer extends DefaultTableCellRenderer {
        Gradient grad = new Gradient();
        NumberFormat nf = NumberFormat.getInstance();

        public svmDoubleTableRenderer() {
            nf.setMaximumFractionDigits(4);
            grad.addPoint(Color.GREEN);
            grad.addPoint(Color.YELLOW);
            grad.addPoint(Color.RED);
            grad.createGradient();
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!(value instanceof Double)) {
                return c;
            }
            if (column == 0) {
                c.setBackground(Color.LIGHT_GRAY);
            } else {
                double val = ((Double) value).doubleValue();
                int colorIndex = (int) (val * 256) * 5;
                if (colorIndex > 255) {
                    colorIndex = 255;
                }
                ((JLabel) c).setText(nf.format(val));
                c.setBackground(grad.getColour(colorIndex));
                //            c.setBackground(Color.BLUE);
            }
            return c;
        }
    };

    public SVMOptimizationPanel() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void jbInit() throws Exception {
        super.jbInit();
        //        this.setLayout(borderLayout1);
        btnDoIt.setText("Do It");
        btnDoIt.addActionListener(new SVMOptimizationPanel_btnDoIt_actionAdapter(this));
        mainPanel.add(jPanel1, java.awt.BorderLayout.NORTH);
        jPanel1.add(btnDoIt);
        tblData.setDefaultRenderer(Double.class, tableRenderer);
        mainPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jScrollPane1.getViewport().add(tblData);
    }

    void doIt() {
        try {
            Instances origInstances = initializeInstances();
            Instances instances = normalizeInstances(origInstances);
            //            computeDistances(instances);
            //            if(true)
            //                return;

            SMO classifier = new SMO();
            classifier.setUseRBF(true);
            CVParameterSelection2 parameterSelection = new CVParameterSelection2();
            parameterSelection.setClassifier(classifier);

            //            double[] cTestVals = {2e-5, 2e-3, 2e-1, 2e1, 2e3, 2e5, 2e7, 2e9, 2e11, 2e13, 2e15};
            //            double[] cTestVals = {20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 500, 1000, 2000};
            double[] cTestVals = {20, 30, 40, 50, 60, 70};
            parameterSelection.addCVParameter(new CVParameter2('C', cTestVals));
            //            parameterSelection.addCVParameter("C .1 30 10");

            //            double[] gTestVals = {2e-15, 2e-13, 2e-11, 2e-9, 2e-7, 2e-5, 2e-3, 2e-1, 2e1, 2e3};
            //            double[] gTestVals = {.002, .001, .05, .01, .5, .3, .2};
            double[] gTestVals = {.002, .001, .05};

            parameterSelection.addCVParameter(new CVParameter2('G', gTestVals));

            parameterSelection.setNumFolds(5);

            parameterSelection.buildClassifier(instances);

            CVParameterMatrix parameterMatrix = parameterSelection.getParameterMatrix();
            Vector parameterValues = parameterMatrix.getDimensionValues();
            Vector param1Values = (Vector) parameterValues.get(0);
            Vector param2Values = (Vector) parameterValues.get(1);
            Vector columnNames = new Vector();
            columnNames.add("");
            for (int param2Ctr = 0; param2Ctr < param2Values.size(); param2Ctr++) {
                System.out.print("\t" + param2Values.get(param2Ctr));
                columnNames.add(param2Values.get(param2Ctr));
            }
            System.out.println();

            Vector data = new Vector();
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2);
            for (int param1Ctr = 0; param1Ctr < param1Values.size(); param1Ctr++) {
                System.out.print(param1Values.get(param1Ctr) + "\t");
                Vector rowData = new Vector();
                rowData.add(param1Values.get(param1Ctr));
                for (int param2Ctr = 0; param2Ctr < param2Values.size(); param2Ctr++) {

                    Vector paramVals = new Vector();
                    paramVals.add(param1Values.get(param1Ctr));
                    paramVals.add(param2Values.get(param2Ctr));
                    Double value = (Double) parameterMatrix.getValue(paramVals);
                    System.out.print(nf.format(value.doubleValue()) + "\t");
                    rowData.add(value);
                }
                data.add(rowData);
                System.out.println();
            }
            DefaultTableModel m = new DefaultTableModel(data, columnNames);
            //            tblData.setDefaultRenderer(Double.class, tableRenderer);
            tblData.setDefaultRenderer(Object.class, new svmDoubleTableRenderer());
            //            tblData.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
            tblData.setModel(m);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    Instances initializeInstances() {
        try {
            //            String filename = "c:/JavaProgs/WekaTest/Data/weather.arff";
            //                String filename = "c:/JavaProgs/WekaTest/Data/MicroarrayTest.txt";
            //                String filename = "c:/JavaProgs/WekaTest/Data/TestGenes.txt";
            String filename = "c:/JavaProgs/WekaTest/Data/Module0.txt";
            // Read all the instances in the file
            FileReader reader = new FileReader(filename);
            Instances instances = new Instances(reader);
            instances.setClassIndex(instances.numAttributes() - 1);
            return instances;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    Instances normalizeInstances(Instances origInstances) {
        try {
            Filter filter = new Normalize();
            filter.setInputFormat(origInstances);
            for (int i = 0; i < origInstances.numInstances(); i++) {
                filter.input(origInstances.instance(i));
            }
            filter.batchFinished();
            Instances newData = filter.getOutputFormat();
            Instance processed;
            while ((processed = filter.output()) != null) {
                newData.add(processed);
            }
            return newData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JScrollPane jScrollPane1 = new JScrollPane();
    JTable tblData = new JTable();
    JButton btnDoIt = new JButton();

    public void btnDoIt_actionPerformed(ActionEvent e) {
        doIt();
    }

    void computeDistances(Instances instances) {
        DenseDoubleMatrix2D instanceMatrix = convertToDoubleMatrix(instances);
        DoubleMatrix2D distanceMatrix = Statistic.distance(instanceMatrix, Statistic.EUCLID);
        //        System.out.println(distanceMatrix.toString());
        DoubleArrayList minDistances = new DoubleArrayList();

        IntArrayList class1Indices = new IntArrayList();
        IntArrayList class2Indices = new IntArrayList();
        for (int instanceCtr = 0; instanceCtr < instances.numInstances(); instanceCtr++) {
            Instance instance = instances.instance(instanceCtr);
            if (instance.classValue() == 0) {
                class1Indices.add(instanceCtr);
            } else {
                class2Indices.add(instanceCtr);
            }
        }

        DoubleMatrix2D classDistanceMatrix = distanceMatrix.viewSelection(class1Indices.elements(), class2Indices.elements());
        for (int i = 0; i < classDistanceMatrix.rows(); i++) {
            DoubleMatrix1D rowDistances = classDistanceMatrix.viewRow(i);
            double minDistance = Descriptive.min(new DoubleArrayList(rowDistances.toArray()));
            minDistances.add(minDistance);
        }
        System.out.println(Descriptive.mean(minDistances));

    }

    DenseDoubleMatrix2D convertToDoubleMatrix(Instances instances) {
        double[][] doubleMatrix = new double[instances.numAttributes()][];
        //        System.out.println(instances.numInstances());
        //        System.out.println(instances.numAttributes());
        for (int i = 0; i < instances.numAttributes(); i++) {
            doubleMatrix[i] = instances.attributeToDoubleArray(i);
        }
        DenseDoubleMatrix2D matrix = new DenseDoubleMatrix2D(doubleMatrix);
        return matrix;
    }
}

class SVMOptimizationPanel_btnDoIt_actionAdapter implements ActionListener {
    private SVMOptimizationPanel adaptee;

    SVMOptimizationPanel_btnDoIt_actionAdapter(SVMOptimizationPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.btnDoIt_actionPerformed(e);
    }
}
