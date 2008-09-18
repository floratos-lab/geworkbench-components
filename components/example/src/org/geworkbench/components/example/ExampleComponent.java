package org.geworkbench.components.example;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GeneOntologyTree;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GoMapping;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GOTerm;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.events.ProjectEvent;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.stat.descriptive.SummaryStatisticsImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.io.IOException;

/**
 * This is an example geWorkbench component.
 *
 * @author John Watkinson
 */
// This annotation lists the data set types that this component accepts.
// The component will only appear when a data set of the appropriate type is selected.
@AcceptTypes({DSMicroarraySet.class})
public class ExampleComponent extends JPanel implements VisualPlugin {

    private DSMicroarraySet microarraySet;
    private JLabel infoLabel;


    public ExampleComponent() {
        infoLabel = new JLabel("");
        add(infoLabel);
        JButton testButton = new JButton("Test Annotations");
        testButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Map<String, List<String>> groups = AnnotationParser.getCustomAnnotationGroupings("Pathway", "///", microarraySet);
                System.out.println("Worked.");
                // Build up GO Term data
                try {
                    GeneOntologyTree tree = GeneOntologyTree.getInstance();
                    
                    GoMapping mapping = new GoMapping(tree, microarraySet);
                    String markerID = "YAR015W";
                    int goID = 6144;
                    Set<GOTerm> allTerms = mapping.getGOTermsForMarker(markerID);
                    Set<String> allMarkers = mapping.getMarkersForGOTerm(goID);
                    Set<GOTerm> directTerms = mapping.getDirectGOTermsForMarker(markerID);
                    Set<String> directMarkers = mapping.getDirectMarkersForGOTerm(goID);
                    System.out.println("Done.");
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        });
        add(testButton);
        try {
            PropertiesManager.getInstance().setProperty(getClass(), "test", "value");
        } catch (IOException e) {
            System.out.println("----- Property test failed.");
            e.printStackTrace();
        }
    }

    /**
     * This method fulfills the contract of the {@link VisualPlugin} interface.
     * It returns the GUI component for this visual plugin.
     */
    public Component getComponent() {
        // In this case, this object is also the GUI component.
        return this;
    }

    /**
     * This is a <b>Subscribe</b> method. The annotation before the method alerts
     * the engine that it should route published objects to this method.
     * The type of objects that are routed to this method are indicated by the first parameter of the method.
     * In this case, it is {@link ProjectEvent}.
     *
     * @param event  the received object.
     * @param source the entity that published the object.
     */
    @Subscribe
    public void receive(ProjectEvent event, Object source) {
        DSDataSet dataSet = event.getDataSet();
        // We will act on this object if it is a DSMicroarraySet
        if (dataSet instanceof DSMicroarraySet) {
            microarraySet = (DSMicroarraySet) dataSet;
            // We just received a new microarray set, so populate the info label with some basic stats.
            String htmlText = "<html><body>"
                    + "<h3>" + microarraySet.getLabel() + "</h3><br>"
                    + "<table>"
                    + "<tr><td>Arrays:</td><td><b>" + microarraySet.size() + "</b></td></tr>"
                    + "<tr><td>Markers:</td><td><b>" + microarraySet.getMarkers().size() + "</b></td></tr>"
                    + "</table>"
                    + "</body></html>";
            infoLabel.setText(htmlText);
        }
    }

    private void tProfilerStuff() throws Exception {
        // Get all markers in the microarrays
        DSItemList<DSGeneMarker> markers = microarraySet.getMarkers();
        // Get case and control sets
        DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(microarraySet);
        DSPanel<DSMicroarray> caseMicroararys = context.getActivatedItemsForClass(CSAnnotationContext.CLASS_CASE);
        DSPanel<DSMicroarray> controlMicroararys = context.getActivatedItemsForClass(CSAnnotationContext.CLASS_CONTROL);
        //// GO Term stuff
        // These two lines are a bit of a hack right now-- we will probably load this in a standard way later
        GeneOntologyTree tree = GeneOntologyTree.getInstance();
        // Construct a mapping of the microarray set markers on to the tree
        GoMapping mapping = new GoMapping(tree, microarraySet);
        // Get all GO terms
        Collection<GOTerm> terms = tree.getAllTerms();
        for (GOTerm term : terms) {
            // For each term, find the associated markers
            Set<String> markerIDs = mapping.getMarkersForGOTerm(term.getId());
            // Ignore the group if there are fewer than, say 4 markers in the group
            if (markerIDs.size() >= 4) {
                // You can now iterate through these markers, just like we do below (see  "* Iterate..." in comments below)
                // todo - iterate through markers, etc.
            }
        }
        //// END GO Term stuff
        // Get marker groupings by a custom annotation
        Map<String, List<String>> groups = AnnotationParser.getCustomAnnotationGroupings("Pathway", "///", microarraySet);
        // Iterate through the groups
        Set<String> groupNames = groups.keySet();
        for (String groupName : groupNames) {
            List<String> markerIDs = groups.get(groupName);
            // * Iterate through the markers
            int index = 0;
            int n = markerIDs.size();
            double[] caseValues = new double[n];
            double[] controlValues = new double[n];
            for (String markerID : markerIDs) {
                DSGeneMarker marker = markers.get(markerID);
                SummaryStatistics caseStats = new SummaryStatisticsImpl();
                SummaryStatistics controlStats = new SummaryStatisticsImpl();
                // Iterate through the case microarrays
                for (DSMicroarray microarray : caseMicroararys) {
                    // Add the microaray value for this marker to the summary stats
                    caseStats.addValue(microarray.getMarkerValue(marker).getValue());
                }
                for (DSMicroarray microarray : controlMicroararys) {
                    // Add the microaray value for this marker to the summary stats
                    controlStats.addValue(microarray.getMarkerValue(marker).getValue());
                }
                double caseMeanValue = caseStats.getMean();
                double controlMeanValue = controlStats.getMean();
                caseValues[index] = caseMeanValue;
                controlValues[index] = controlMeanValue;
                index++;
            }
            // todo - Compute t-value for group using caseValues and controlValues
            double tValue = 5; // Fake t-value for now
        }
        //// Make a new microarray, given an array of float values
        float[] example = {1f, 2f, 3f, 4f, 5f};
        int n = example.length;
        CSMicroarray array = new CSMicroarray(n);
        for (int i = 0; i < n; i++) {
            array.setMarkerValue(i, new CSExpressionMarkerValue(example[i]));
        }
    }
}
