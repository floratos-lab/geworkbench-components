package org.geworkbench.components.mindy.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSAffyMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.components.mindy.MindyPlugin;
import org.geworkbench.components.mindy.MindyData;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.*;

/**
 * @author mhall
 */
public class MindyTest {

    static Log log = LogFactory.getLog(MindyTest.class);

    public static void main(String[] args) {
//        AnnotationsManager am = AnnotationsManager.getManager();
//        try {
//            am.loadAnnotationsFromFile("hgu95av2", new File("synergy/hgu95av2.txt.zip"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        log.debug("Step 1 - Loading microarray data...");
        CSExprMicroarraySet maSet = new CSExprMicroarraySet();
        maSet.read(new File("data/webmatrix.exp"));

        DSGeneMarker mod = maSet.getMarkers().get("35411_at");
        DSGeneMarker transFac = maSet.getMarkers().get("1850_at");

        log.debug("Creating JFrame");
        ArrayList<MindyData.MindyResultRow> mindyRows = new ArrayList<MindyData.MindyResultRow>();
        mindyRows.add(new MindyData.MindyResultRow(mod, transFac, maSet.getMarkers().get(0), 1f, 0.001f));
        mindyRows.add(new MindyData.MindyResultRow(mod, transFac, maSet.getMarkers().get(1), 0.9f, 0.01f));
//        mindyRows.add(new MindyData.MindyResultRow(mod, transFac, maSet.getMarkers().get(2), 0.8f, 0.1f));
//        ArrayList<DSGeneMarker> modulators = new ArrayList<DSGeneMarker>();
//        modulators.add(mod);
//        ArrayList<DSGeneMarker> transfacs = new ArrayList<DSGeneMarker>();
//        transfacs.add(transFac);

        MindyData data = new MindyData(maSet, mindyRows);

        MindyPlugin mindy = new MindyPlugin(data);

        JFrame frame = new JFrame("HeatMap Test");
        frame.getContentPane().add(mindy, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);

        log.debug("Done.");

    }

}
