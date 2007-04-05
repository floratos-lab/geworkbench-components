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
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;
import org.geworkbench.util.threading.SwingWorker;
import org.geworkbench.events.AdjacencyMatrixEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.builtin.projects.ProjectPanel;

import java.util.ArrayList;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map;

import wb.data.MicroarraySet;
import wb.data.Microarray;
import wb.data.MarkerSet;
import wb.data.Marker;
import wb.plugins.aracne.WeightedGraph;
import wb.plugins.aracne.GraphEdge;
import edu.columbia.c2b2.aracne.Parameter;
import edu.columbia.c2b2.aracne.Aracne;

import javax.swing.*;

/**
 * @author Matt Hall
 */
public class AracneAnalysis extends AbstractAnalysis implements ClusteringAnalysis {
    static Log log = LogFactory.getLog(AracneAnalysis.class);

    private static final String TEMP_DIR = "temporary.files.directory";
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView;
    private AdjacencyMatrixDataSet adjMatrix;

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
        if (input instanceof DSMicroarraySetView) {
            log.debug("Input dataset is microarray type.");
            mSetView = (DSMicroarraySetView) input;
        } else if (input instanceof AdjacencyMatrixDataSet) {
            log.debug("Input dataset is adjacency matrix, will only perform DPI.");
            adjMatrix = (AdjacencyMatrixDataSet) input;
        }

//        MindyData loadedData = null;
//        try {
//            loadedData = MindyResultsParser.parseResults((CSMicroarraySet) mSetView, new File(params.getHubMarkersFile()));
//        } catch (IOException e) {
//            log.error(e);
//        }

//        ArrayList<Marker> modulators = new ArrayList<Marker>();
//        try {
//            File modulatorFile = new File(params.getHubMarkersFile());
//            BufferedReader reader = new BufferedReader(new FileReader(modulatorFile));
//            String modulator = reader.readLine();
//            while (modulator != null) {
//                DSGeneMarker marker = mSetView.getMarkers().get(modulator);
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

        final Parameter p = new Parameter();
        if (params.isHubListSpecified()) {
            if (params.getHubGeneList() == null || params.getHubGeneList().size() == 0) {
                JOptionPane.showMessageDialog(null, "You did not load any genes as hub markers.");
                return null;
            }

            ArrayList<String> hubGeneList = params.getHubGeneList();
            for (String gene : hubGeneList) {
                log.debug("Adding hub gene: " + gene);
            }
            p.setSubnet(new Vector<String>(hubGeneList));
//            p.setHub(params.getHubGeneString());
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
        if (params.isTargetListSpecified()) {
            if (params.getTargetGenes() == null || params.getTargetGenes().size() == 0) {
                JOptionPane.showMessageDialog(null, "You did not load any target genes.");
                return null;
            }
            p.setTf_list(new Vector<String>(params.getTargetGenes()));
        }
        if (adjMatrix != null) {
            p.setPrecomputedAdjacencies(convert(adjMatrix, mSetView));
        }

//        AracneWorker aracneWorker = new AracneWorker(mSetView, p);
        AracneThread aracneThread = new AracneThread(mSetView, p);

        AracneProgress progress = new AracneProgress(aracneThread);
        aracneThread.setProgressWindow(progress);
        progress.setVisible(true);

        return new AlgorithmExecutionResults(true, "ARACNE in progress.", null);

    }

    private WeightedGraph convert(AdjacencyMatrixDataSet adjMatrix, DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSet) {
        WeightedGraph graph = new WeightedGraph(adjMatrix.getNetworkName());
        AdjacencyMatrix matrix = adjMatrix.getMatrix();
        HashMap<Integer, HashMap<Integer, Float>> geneRows = matrix.getGeneRows();
        DSItemList<DSGeneMarker> markers = mSet.markers();
        for (Map.Entry<Integer, HashMap<Integer, Float>> entry : geneRows.entrySet()) {
            DSGeneMarker gene1 = markers.get(entry.getKey());
            if (gene1 != null) {
                HashMap<Integer, Float> destGenes = entry.getValue();
                for (Map.Entry<Integer, Float> destEntry : destGenes.entrySet()) {
                    DSGeneMarker destGene = markers.get(destEntry.getKey());
                    if (destGene != null) {
                        graph.addEdge(gene1.getShortName(), destGene.getShortName(), destEntry.getValue());
                    } else {
                        log.debug("Gene with index "+destEntry.getKey()+" not found in selected genes, skipping.");
                    }
                }
            } else {
                log.debug("Gene with index "+entry.getKey()+" not found in selected genes, skipping.");
            }
        }
        return graph;
    }

    private MicroarraySet convert(DSMicroarraySetView<DSGeneMarker, DSMicroarray> inSet) {
        MarkerSet markers = new MarkerSet();
        for (DSGeneMarker marker : inSet.markers()) {
            markers.addMarker(new Marker(marker.getLabel()));
        }
        MicroarraySet returnSet = new MicroarraySet("Converted Set", "ID", "ChipType", markers);
        DSItemList<DSMicroarray> arrays = inSet.items();
        for (DSMicroarray microarray : arrays) {
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

    @Publish
    public AdjacencyMatrixEvent publishAdjacencyMatrixEvent(AdjacencyMatrixEvent ae) {
        return ae;
    }

    @Publish
    public ProjectNodeAddedEvent publishProjectNodeAddedEvent(ProjectNodeAddedEvent event) {
        return event;
    }

    class AracneThread extends Thread {
        private WeightedGraph weightedGraph;
        private AracneProgress progressWindow;
        private DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView;
        private Parameter p;

        public AracneThread(DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSet, Parameter p) {
            this.mSetView = mSet;
            this.p = p;
        }

        public void run() {
            log.debug("Running ARACNE in worker thread.");
            p.setSuppressFileWriting(true);
            weightedGraph = Aracne.run(convert(mSetView), p);
            log.debug("Done running ARACNE in worker thread.");
            progressWindow.setVisible(false);

            if (weightedGraph.getEdges().size() > 0) {
                AdjacencyMatrixDataSet dataSet = new AdjacencyMatrixDataSet(convert(weightedGraph, mSetView.getMicroarraySet()), -1, 0, 1000,
                        "Adjacency Matrix", "ARACNE Set", mSetView.getMicroarraySet());
                ProjectPanel.addToHistory(dataSet, "Generated with ARACNE run with paramters: " + p.getParamterDescription());

                publishProjectNodeAddedEvent(new ProjectNodeAddedEvent("Adjacency Matrix Added", null, dataSet));

//        publishAdjacencyMatrixEvent(new AdjacencyMatrixEvent(convert(weightedGraph, mSetView), "ARACNE Set",
//                -1, 2, 0.5f, AdjacencyMatrixEvent.Action.RECEIVE));
                publishAdjacencyMatrixEvent(new AdjacencyMatrixEvent(convert(weightedGraph, mSetView.getMicroarraySet()), "ARACNE Set",
                        -1, 2, 0.5f, AdjacencyMatrixEvent.Action.DRAW_NETWORK));
            } else {
                JOptionPane.showMessageDialog(null, "The ARACNE run resulted in no adjacent genes, " +
                        "consider relaxing your thresholds.");
            }

        }

        public AracneProgress getProgressWindow() {
            return progressWindow;
        }

        public void setProgressWindow(AracneProgress progressWindow) {
            this.progressWindow = progressWindow;
        }

    }
}
