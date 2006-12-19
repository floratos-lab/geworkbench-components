package org.geworkbench.components.mindy.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSAffyMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.components.mindy.MindyPlugin;
import org.geworkbench.components.mindy.MindyData;
import org.geworkbench.components.mindy.MindyResultsParser;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.*;

import junit.framework.TestCase;

/**
 * @author mhall
 */
public class MindyTest extends TestCase {

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
        maSet.read(new File("data/mindy/SmallerMatrix.exp"));

        DSGeneMarker mod = maSet.getMarkers().get("35411_at");
        DSGeneMarker transFac = maSet.getMarkers().get("1850_at");

        log.debug("Creating JFrame");
        ArrayList<MindyData.MindyResultRow> mindyRows = new ArrayList<MindyData.MindyResultRow>();
        mindyRows.add(new MindyData.MindyResultRow(mod, transFac, maSet.getMarkers().get(0), 1f, 0.001f));
        mindyRows.add(new MindyData.MindyResultRow(mod, transFac, maSet.getMarkers().get(1), 0.9f, 0.01f));
        mindyRows.add(new MindyData.MindyResultRow(mod, transFac, maSet.getMarkers().get(2), -0.8f, 0.1f));
        mindyRows.add(new MindyData.MindyResultRow(maSet.getMarkers().get("846_s_at"), transFac, maSet.getMarkers().get(2), -0.8f, 0.1f));
//        ArrayList<DSGeneMarker> modulators = new ArrayList<DSGeneMarker>();
//        modulators.add(mod);
//        ArrayList<DSGeneMarker> transfacs = new ArrayList<DSGeneMarker>();
//        transfacs.add(transFac);

        MindyData data = new MindyData(maSet, mindyRows);
        MindyData loadedData = null;
        try {
            loadedData = MindyResultsParser.parseResults(maSet, new File("data/mindy/MINDY_output_MYC_cofactors.txt"));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        MindyPlugin mindy = new MindyPlugin(loadedData);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        JFrame frame = new JFrame("MINDY Plugin");
        frame.getContentPane().add(mindy, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(1024, 800);
        frame.setVisible(true);

        log.debug("Done.");

    }

}
