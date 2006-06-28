package org.geworkbench.components.matrixreduce;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.DSMatrixReduceSet;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.CSMatrixReduceSet;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.DSPositionSpecificAffintyMatrix;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.CSPositionSpecificAffinityMatrix;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.engine.management.Publish;

import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author John Watkinson
 */
public class MatrixReduceAnalysis extends AbstractAnalysis implements ClusteringAnalysis {

    public static final String[] NUCLEOTIDES = {"A", "C", "G", "T"};

    private static final String ANALYSIS_DIR = "c:/tmp/matrixreduce/TestRun";

    public MatrixReduceAnalysis() {
        setLabel("Matrix Reduce");
        setDefaultPanel(new MatrixReduceParamPanel());
    }

    // not used
    public int getAnalysisType() {
        return AbstractAnalysis.ZERO_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        DSMicroarraySet parentSet = ((DSMicroarraySetView) input).getMicroarraySet();
        // Parse results
        File dir = new File(ANALYSIS_DIR);
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.startsWith("matrix") && name.endsWith("out")) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        DSMatrixReduceSet dataSet = new CSMatrixReduceSet(parentSet, "MatrixREDUCE Results");
        for (File file : files) {
            int aIndex = file.getName().lastIndexOf('_');
            int bIndex = file.getName().lastIndexOf('.');
            if ((aIndex == -1) || (bIndex == -1)) {
                continue;
            }
            int i = -1;
            try {
                i = Integer.parseInt(file.getName().substring(aIndex + 1, bIndex));
            } catch (NumberFormatException nfe) {
                continue;
            }
            try {
                BufferedReader in = new BufferedReader(new FileReader(file));
                // Line 1
                String line = in.readLine();
                String[] tokens = line.split(" ");
                String seed = tokens[0];
                String experiment = tokens[2];
                // Line 2
                line = in.readLine();
                tokens = line.split(" ");
                double pValue = Double.parseDouble(tokens[0]);
                // Line 3
                line = in.readLine();
                tokens = line.split(" ");
                double bonferonni = Double.parseDouble(tokens[0]);
                // Line 4
                line = in.readLine();
                tokens = line.split(" ");
                int strand = Integer.parseInt(tokens[0]);
                // Line 5 - ignore
                line = in.readLine();
                // Remaining lines have PSAM data
                DSPositionSpecificAffintyMatrix psam = new CSPositionSpecificAffinityMatrix();
                psam.setSeedSequence(seed);
                psam.setExperiment(experiment);
                psam.setPValue(pValue);
                StringBuffer sb = new StringBuffer();
                line = in.readLine();
                while (line != null) {
                    tokens = line.split("\t");
                    int best = 0;
                    double bestValue = -1;
                    for (int j = 0; j < 4; j++) {
                        double v = Double.parseDouble(tokens[1 + j]);
                        if (v > bestValue) {
                            bestValue = v;
                            best = j;
                        }
                    }
                    sb.append(NUCLEOTIDES[best]);
                    line = in.readLine();
                }
                psam.setConsensusSequence(sb.toString());
                File logoFile = new File(file.getParentFile(), file.getName().replace(".out", ".png"));
                ImageIcon psamImage = new ImageIcon(logoFile.getAbsolutePath());
                psam.setPsamImage(psamImage);
                dataSet.add(psam);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception parsing " + file + ".");
            }
        }
        return new AlgorithmExecutionResults(true, "Completed.", dataSet);
    }

    @Publish public DSMatrixReduceSet publishMatrixReduceSet(DSMatrixReduceSet data) {
        return data;
    }
}
