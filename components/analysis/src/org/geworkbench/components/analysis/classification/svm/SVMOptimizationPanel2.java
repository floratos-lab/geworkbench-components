package org.geworkbench.components.analysis.classification.svm;

import org.geworkbench.util.microarrayutils.MicroarrayViewEventBase;
import org.geworkbench.bison.algorithm.classification.crossvalidation.CVParameter2;
import org.geworkbench.bison.algorithm.classification.crossvalidation.CVParameterMatrix;
import org.geworkbench.bison.algorithm.classification.crossvalidation.CVParameterSelection2;
import org.geworkbench.bison.algorithm.classification.svm.WekaUtil;
import org.geworkbench.engine.config.VisualPlugin;
import weka.classifiers.functions.SMO;
import weka.core.Instances;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
public class SVMOptimizationPanel2 extends MicroarrayViewEventBase implements VisualPlugin {
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

    JTable tblData;

    public void btnDoIt_actionPerformed(ActionEvent e) {
        doIt();
    }

    public SVMOptimizationPanel2() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void jbInit() throws Exception {
        super.jbInit();
        BorderLayout borderLayout1 = new BorderLayout();
        JPanel jPanel1 = new JPanel();
        JScrollPane jScrollPane1 = new JScrollPane();
        tblData = new JTable();
        JButton btnDoIt = new JButton();

        //        this.setLayout(borderLayout1);
        btnDoIt.setText("Do It");
        btnDoIt.addActionListener(new SVMOptimizationPanel2_btnDoIt_actionAdapter(this));
        mainPanel.add(jPanel1, java.awt.BorderLayout.NORTH);
        jPanel1.add(btnDoIt);
        tblData.setDefaultRenderer(Double.class, tableRenderer);
        mainPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jScrollPane1.getViewport().add(tblData);
    }

    void doIt() {
        try {

            WekaUtil wekaUtil = new WekaUtil();
            //            Instances instances = wekaUtil.generateInstances(maSetView);
            Instances instances = null;
            Instances normalizedInstances = wekaUtil.normalizeInstances(instances);

            classify(normalizedInstances);
            //            System.out.println(instances.toString());


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void classify(Instances instances) {
        try {
            SMO classifier = new SMO();
            classifier.setUseRBF(true);
            CVParameterSelection2 parameterSelection = new CVParameterSelection2();
            parameterSelection.setClassifier(classifier);

            double[] cTestVals = {20, 30, 40, 50, 60, 70};
            parameterSelection.addCVParameter(new CVParameter2('C', cTestVals));

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

}

class SVMOptimizationPanel2_btnDoIt_actionAdapter implements ActionListener {
    private SVMOptimizationPanel2 adaptee;

    SVMOptimizationPanel2_btnDoIt_actionAdapter(SVMOptimizationPanel2 adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.btnDoIt_actionPerformed(e);
    }
}
