package org.geworkbench.components.ei;

/**
 * Created by IntelliJ IDEA.
 * User: xiaoqing
 * Date: Sep 10, 2007
 * Time: 2:33:35 PM
 * To change this template use File | Settings | File Templates.
 */

import javax.swing.*;
import java.util.*;

public class StandAloneWindow {
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */

    //private JTabbedPane tabbedPane;

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("HelloWorldSwing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add the ubiquitous "Hello World" label.
        JLabel label = new JLabel("Hello World");
        String[] evidentNames = {"e1", "e2", "e3"};

        ArrayList arrayList = new ArrayList();
        for(String s: evidentNames){
            arrayList.add(s);
        }
        String[] goldStrNames = {"g1", "g2", "g3"};

        Map garrayList = new TreeMap();
        int i =0;
        for(String s: goldStrNames){
            garrayList.put(new Integer(i++), s);
        }

        GenericDisplayPanel performancePanel = new GenericDisplayPanel(null, GenericDisplayPanel.PlotType.PERF, arrayList, garrayList);

//
//           evidentNames = new String[]{"e4", "e5", "e3"};
//
//          arrayList = new ArrayList();
//        for(String s: evidentNames){
//            arrayList.add(s);
//        }
//         goldStrNames = new String[]{"g4", "g6", "g3"};
//
//          garrayList = new TreeMap();
//         i =0;
//        for(String s: evidentNames){
//            garrayList.put(new Integer(i++), s);
//        }
         GenericDisplayPanel rocPanel = new GenericDisplayPanel(null, GenericDisplayPanel.PlotType.ROC, arrayList, garrayList);
         rocPanel.setCurrentMaximumCharts(1);
        performancePanel.setCurrentMaximumCharts(4);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Performance Graphs", performancePanel);
        tabbedPane.add("ROC Graph", rocPanel);
                    frame.getContentPane().add(tabbedPane);
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}