package org.geworkbench.components.mindy;

import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import javax.swing.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;

/**
 * @author mhall
 */
public class MindyResultsParser {

    public static MindyData parseResults(CSMicroarraySet maSet, File resultsFile) throws IOException {
        List<MindyData.MindyResultRow> rows = new ArrayList<MindyData.MindyResultRow>();
        BufferedReader in = new BufferedReader(new FileReader(resultsFile));

        String header = in.readLine();
        String line = in.readLine();
        while (line != null) {
            String[] tokens = line.split("\t");
            DSGeneMarker transFac = maSet.getMarkers().get(tokens[0]);
            DSGeneMarker hub = maSet.getMarkers().get(tokens[1]);
            DSGeneMarker target = maSet.getMarkers().get(tokens[2]);
            float score = Float.valueOf(tokens[3]);
            float pvalue = Float.valueOf(tokens[4]);
            rows.add(new MindyData.MindyResultRow(hub, transFac, target, score, pvalue));

            line = in.readLine();
        }
        return new MindyData(maSet, rows);
    }

    public static void main(String[] args) {
        JXTaskPane jxTaskPane = new JXTaskPane();
        jxTaskPane.setTitle("Pane 1");
        JList list = new JList(new String[]{"One", "Two"});
        jxTaskPane.add(list);
        JXTaskPaneContainer taskContainer = new JXTaskPaneContainer();
        taskContainer.add(jxTaskPane);

        JFrame frame = new JFrame("MINDY Plugin");
        frame.getContentPane().add(taskContainer, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
