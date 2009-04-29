package org.geworkbench.components.mindy.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.components.mindy.MindyPlugin;
import org.geworkbench.components.mindy.MindyResultsParser;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;

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

        ArrayList<DSGeneMarker> limitList = new ArrayList<DSGeneMarker>();
        // Modulators
        limitList.add(maSet.getMarkers().get("316_g_at"));
        limitList.add(maSet.getMarkers().get("31947_r_at"));
        // Targets
        limitList.add(maSet.getMarkers().get("34451_at"));
        limitList.add(maSet.getMarkers().get("40325_at"));


        log.debug("Creating JFrame");
        MindyData loadedData = null;
        try {
            loadedData = MindyResultsParser.parseResults(maSet, new File("data/mindy/MINDY_output_MYC_cofactors.txt"));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        MindyPlugin mindy = new MindyPlugin(loadedData, null);
        System.out.println("MindyTest.main()");
        mindy.limitMarkers(limitList);
        

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
