package org.geworkbench.components.aracne;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSGeneMarker;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;
import org.geworkbench.events.AdjacencyMatrixEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;

import wb.data.MicroarraySet;
import wb.data.Microarray;
import wb.data.MarkerSet;
import wb.data.Marker;
import wb.plugins.aracne.WeightedGraph;
import wb.plugins.aracne.GraphEdge;
import edu.columbia.c2b2.aracne.Parameter;
import edu.columbia.c2b2.aracne.Aracne;

/**
 * @author Matt Hall
 */
public class AracneAnalysis extends AbstractAnalysis implements ClusteringAnalysis {
    Log log = LogFactory.getLog(this.getClass());

    private static final String TEMP_DIR = "temporary.files.directory";

    public AracneAnalysis() {
        setLabel("ARACNE");
        setDefaultPanel(new AracneParamPanel());
    }

    // not used
    public int getAnalysisType() {
        return AbstractAnalysis.ZERO_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        log.debug("input: " + input);
        // Use this to get params
        AracneParamPanel params = (AracneParamPanel) aspp;
        DSMicroarraySet<DSMicroarray> mSet = ((DSMicroarraySetView) input).getMicroarraySet();

//        MindyData loadedData = null;
//        try {
//            loadedData = MindyResultsParser.parseResults((CSMicroarraySet) mSet, new File(params.getHubMarkersFile()));
//        } catch (IOException e) {
//            log.error(e);
//        }

//        ArrayList<Marker> modulators = new ArrayList<Marker>();
//        try {
//            File modulatorFile = new File(params.getHubMarkersFile());
//            BufferedReader reader = new BufferedReader(new FileReader(modulatorFile));
//            String modulator = reader.readLine();
//            while (modulator != null) {
//                DSGeneMarker marker = mSet.getMarkers().get(modulator);
//                if (marker == null) {
//                    log.info("Couldn't find marker " + modulator + " from modulator file in microarray set.");
//                } else {
//                    modulators.add(new Marker(modulator));
//                }
//                modulator = reader.readLine();
//            }
//        } catch (IOException e) {
//            log.error(e);
//        }

        Parameter p = new Parameter();
        if (params.isHubListSpecified()) {
            p.setHub(params.getHubGeneString());
        }
        if (params.isThresholdMI()) {
            p.setThreshold(params.getThreshold());
        } else {
            p.setPvalue(params.getThreshold());
        }
        if (params.isKernelWidthSpecified()) {
            p.setSigma(params.getKernelWidth());
        }
        if (params.isDPIToleranceSpecified()) {
            p.setEps(params.getDPITolerance());
        }
        WeightedGraph weightedGraph = Aracne.run(convert(mSet), p);

        AdjacencyMatrixDataSet dataSet = new AdjacencyMatrixDataSet(convert(weightedGraph, mSet), -1, 0, 1000, "Adjacency Matrix",
                "ARACNE Set", mSet);
        publishProjectNodeAddedEvent(new ProjectNodeAddedEvent("Adjacency Matrix Added", null, dataSet));

//        publishAdjacencyMatrixEvent(new AdjacencyMatrixEvent(convert(weightedGraph, mSet), "ARACNE Set",
//                -1, 2, 0.5f, AdjacencyMatrixEvent.Action.RECEIVE));
        publishAdjacencyMatrixEvent(new AdjacencyMatrixEvent(convert(weightedGraph, mSet), "ARACNE Set",
                -1, 2, 0.5f, AdjacencyMatrixEvent.Action.DRAW_NETWORK));
/*
        Mindy mindy = new Mindy();
        DSGeneMarker transFac = mSet.getMarkers().get(params.getTranscriptionFactor());
        log.info("Running MINDY analysis.");
        MindyResults results = mindy.runMindy(convert(mSet), new Marker(params.getTranscriptionFactor()), modulators,
                params.getSetFraction()/100f, params.getDPITolerance());
        log.info("MINDY analysis complete.");
        List<MindyData.MindyResultRow> dataRows = new ArrayList<MindyData.MindyResultRow>();
        for (MindyResults.MindyResultForTarget result : results) {
            DSItemList<DSGeneMarker> markers = mSet.getMarkers();
            DSGeneMarker target = markers.get(result.getTarget().getName());
            for (MindyResults.MindyResultForTarget.ModulatorSpecificResult specificResult : result) {
                DSGeneMarker mod = markers.get(specificResult.getModulator().getName());
                dataRows.add(new MindyData.MindyResultRow(mod, transFac, target, specificResult.getScore(), 0f));
            }
        }

        MindyData loadedData = new MindyData((CSMicroarraySet) mSet, dataRows);
        log.info("Done converting MINDY results.");

        MindyDataSet dataSet = new MindyDataSet(mSet, "MINDY Results", loadedData, params.getHubMarkersFile());
*/
        return new AlgorithmExecutionResults(true, "MINDY Results Loaded.", null);

    }

    private MicroarraySet convert(DSMicroarraySet<DSMicroarray> inSet) {
        MarkerSet markers = new MarkerSet();
        for (DSGeneMarker marker : inSet.getMarkers()) {
            markers.addMarker(new Marker(marker.getLabel()));
        }
        MicroarraySet returnSet = new MicroarraySet("Converted Set", "ID", "ChipType", markers);
        for (DSMicroarray microarray : inSet) {
            returnSet.addMicroarray(new Microarray(microarray.getLabel(), microarray.getRawMarkerData()));
        }
        return returnSet;
    }

    private DSMicroarraySet<DSMicroarray> convert(MicroarraySet inSet) {
        DSMicroarraySet<DSMicroarray> microarraySet = new CSMicroarraySet<DSMicroarray>();
        microarraySet.setLabel(inSet.getName());

        for (int i = 0; i < inSet.getMarkers().size(); i++) {
            /* cagrid array */
            Microarray inArray = inSet.getMicroarray(i);
            float[] arrayData = inArray.getValues();
            String arrayName = inArray.getName();

            /* bison array */
            CSMicroarray microarray = new CSMicroarray(arrayData.length);
            microarray.setLabel(arrayName);
            for (int j = 0; j < arrayData.length; j++) {
                DSMarkerValue markerValue = new CSExpressionMarkerValue(
                        arrayData[j]);
                microarray.setMarkerValue(j, markerValue);
            }
            microarraySet.add(i, microarray);

            // Create marker
            microarraySet.getMarkers().add(new CSGeneMarker(inSet.getMarkers().getMarker(i).getName()));
        }

        return microarraySet;
    }

    private AdjacencyMatrix convert(WeightedGraph graph, DSMicroarraySet<DSMicroarray> mSet) {
        AdjacencyMatrix matrix = new AdjacencyMatrix();
        matrix.setMicroarraySet(mSet);
        for (String node : graph.getNodes()) {
            DSGeneMarker marker = mSet.getMarkers().get(node);
            matrix.addGeneRow(marker.getSerial());
        }
        for (GraphEdge graphEdge : graph.getEdges()) {
            DSGeneMarker marker1 = mSet.getMarkers().get(graphEdge.getNode1());
            DSGeneMarker marker2 = mSet.getMarkers().get(graphEdge.getNode2());
            matrix.add(marker1.getSerial(), marker2.getSerial(), graphEdge.getWeight());
        }
        return matrix;
    }

    @Publish public AdjacencyMatrixEvent publishAdjacencyMatrixEvent(AdjacencyMatrixEvent ae) {
        return ae;
    }

    @Publish public ProjectNodeAddedEvent publishProjectNodeAddedEvent(ProjectNodeAddedEvent event) {
        return event;
    }

}
