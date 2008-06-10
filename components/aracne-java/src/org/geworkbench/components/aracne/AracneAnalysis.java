package org.geworkbench.components.aracne;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.util.Iterator;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.AdjacencyMatrixEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;

import wb.data.Marker;
import wb.data.MarkerSet;
import wb.data.Microarray;
import wb.data.MicroarraySet;
import wb.plugins.aracne.GraphEdge;
import wb.plugins.aracne.WeightedGraph;
import edu.columbia.c2b2.aracne.Aracne;
import edu.columbia.c2b2.aracne.Parameter;

/**
 * @author Matt Hall
 */
public class AracneAnalysis extends AbstractGridAnalysis implements ClusteringAnalysis {
    static Log log = LogFactory.getLog(AracneAnalysis.class);

    private static final String TEMP_DIR = "temporary.files.directory";
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView;
    private AdjacencyMatrixDataSet adjMatrix;
    private String COMMA_SEP = ",";
    private final String analysisName = "Aracne";

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
            for (String modGene : hubGeneList) {
                DSGeneMarker marker = mSetView.markers().get(modGene);
                if (marker == null) {
                    log.info("Couldn't find marker " + modGene + " specified as hub gene in microarray set.");
                    JOptionPane.showMessageDialog(null, "Couldn't find marker " + modGene + " specified as hub gene in microarray set.");
                    return null;
                }
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
//            filterByAdjMatrix(adjMatrix, mSetView);
            adjMatrix = null;
        }

//        AracneWorker aracneWorker = new AracneWorker(mSetView, p);
        AracneThread aracneThread = new AracneThread(mSetView, p);

        AracneProgress progress = new AracneProgress(aracneThread);
        aracneThread.setProgressWindow(progress);
        progress.setVisible(true);

        return new AlgorithmExecutionResults(true, "ARACNE in progress.", null);

    }

    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> filterByAdjMatrix(AdjacencyMatrixDataSet adjMatrix, DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSet){
        AdjacencyMatrix matrix = adjMatrix.getMatrix();
        DSMicroarraySetView newView=new CSMicroarraySetView(mSet.getMicroarraySet());
        newView.setMarkerPanel(mSet.getMarkerPanel());
        newView.getMarkerPanel().addAll(mSet.getMarkerPanel());
        newView.setItemPanel(mSet.getItemPanel());
        DSItemList<DSGeneMarker> markers = mSet.markers();
        DSItemList<DSGeneMarker> markers2 = newView.markers();
        DSItemList<DSGeneMarker> retainSet=new CSItemList<DSGeneMarker>();
        HashMap<Integer, HashMap<Integer, Float>> geneRows = matrix.getGeneRows();
        for (Map.Entry<Integer, HashMap<Integer, Float>> entry : geneRows.entrySet()) {
            DSGeneMarker gene1 = markers.get(entry.getKey());
            retainSet.add(gene1);
        }
        markers2.retainAll(retainSet);
        return newView;    	
    }

    private WeightedGraph convert(AdjacencyMatrixDataSet adjMatrix, DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSet) {
        WeightedGraph graph = new WeightedGraph(adjMatrix.getNetworkName());
        AdjacencyMatrix matrix = adjMatrix.getMatrix();
        HashMap<Integer, HashMap<Integer, Float>> geneRows = matrix.getGeneRows();
        DSItemList<DSGeneMarker> markers = mSet.markers();
        for (DSGeneMarker marker:markers){
        	System.out.println(marker.getLabel()+"added");
        	graph.addEdge(marker.getLabel(),marker.getLabel(),0);
        }        
        for (Map.Entry<Integer, HashMap<Integer, Float>> entry : geneRows.entrySet()) {
            DSGeneMarker gene1 = markers.get(entry.getKey());
            if (gene1 != null) {
                HashMap<Integer, Float> destGenes = entry.getValue();
                for (Map.Entry<Integer, Float> destEntry : destGenes.entrySet()) {
                    DSGeneMarker destGene = markers.get(destEntry.getKey());
                    if (destGene != null) {
                        graph.addEdge(gene1.getLabel(), destGene.getLabel(), destEntry.getValue());
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

    //This convert() has bug in it !!!
    //TODO: Since Microarray.getValues() in workbook.jar will return all the marker values, we should filter out those inactive markers in DSMicroarraySetView before put into MicroarraySet  
    private MicroarraySet convert(DSMicroarraySetView<DSGeneMarker, DSMicroarray> inSet) {
        MarkerSet markers = new MarkerSet();
        for (DSGeneMarker marker : inSet.markers()) {
            markers.addMarker(new Marker(marker.getLabel()));
        }
        MicroarraySet returnSet = new MicroarraySet(inSet.getDataSet().getDataSetName(), inSet.getDataSet().getID(), "Unknown", markers);
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
            progressWindow.dispose();

            if (weightedGraph.getEdges().size() > 0) {
                AdjacencyMatrixDataSet dataSet = new AdjacencyMatrixDataSet(convert(weightedGraph, mSetView.getMicroarraySet()), -1, 0, 1000,
                        "Adjacency Matrix", "ARACNE Set", mSetView.getMicroarraySet());               
                StringBuilder paramDescB = new StringBuilder("Generated with ARACNE run with data:\nArrays:\n");
                for(DSMicroarray ma: this.mSetView.getMicroarraySet()) {
                	paramDescB.append("\t");
                	paramDescB.append(ma.getLabel());
                	paramDescB.append("\n");
                }
                paramDescB.append("Markers:\n");
                for(DSGeneMarker m: this.mSetView.markers()){
                	paramDescB.append("\t");
                	paramDescB.append(m.getShortName());
                	paramDescB.append("\n");
                }
                ProjectPanel.addToHistory(dataSet, "Generated with ARACNE run with paramters:\n" + p.getParamterDescription() + paramDescB.toString());
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
	
	/**
	 * Validates the user-entered parameter values.
	 */
	@Override
	public ParamValidationResults validateParameters() {
		// Delegates the validation to the panel.

		if (aspp == null)
			return new ParamValidationResults(true, null);

		// Use this to get params
        AracneParamPanel params = (AracneParamPanel) aspp;

        final Parameter p = new Parameter();
        if (params.isHubListSpecified()) {
            if (params.getHubGeneList() == null || params.getHubGeneList().size() == 0) {
                return new ParamValidationResults(false, "You did not load any genes as hub markers.");
            }
        }
        if (params.isThresholdMI()) {
        	try{
	        	if ((0<=params.getThreshold())&&(params.getThreshold()<=1)){}
	        	else
	        		return new ParamValidationResults(false, "Threshold Mutual Info. should between 0.0 and 1.0");
        	}catch(NumberFormatException nfe){
        		return new ParamValidationResults(false, "Threshold Mutual Info. should be a float number between 0.0 and 1.0.");
        	};
        } else {
        	try{
	        	if ((0<=params.getThreshold())&&(params.getThreshold()<=1)){}
	        	else
	        		return new ParamValidationResults(false, "Threshold P-Value should between 0.0 and 1.0");
        	}catch(NumberFormatException nfe){
        		return new ParamValidationResults(false, "Threshold P-Value should be a float number between 0.0 and 1.0.");
        	};
        }
        if (params.isKernelWidthSpecified()) {
        	try{
	        	if ((0<=params.getKernelWidth())&&(params.getKernelWidth()<=1)){}
	        	else
	        		return new ParamValidationResults(false, "Kernel Width should between 0.0 and 1.0");
        	}catch(NumberFormatException nfe){
        		return new ParamValidationResults(false, "Kernel Width should be a float number between 0.0 and 1.0.");
        	};
        }
        if (params.isDPIToleranceSpecified()) {
        	try{
	        	if ((params.getDPITolerance()!=Float.NaN)&&(0<=params.getDPITolerance())&&(params.getDPITolerance()<=1)){}
	        	else
	        		return new ParamValidationResults(false, "DPI Tolerance should between 0.0 and 1.0");
        	}catch(NumberFormatException nfe){
        		return new ParamValidationResults(false, "DPI Tolerance should be a float number between 0.0 and 1.0.");
        	};
        }
        if (params.isTargetListSpecified()) {
            if (params.getTargetGenes() == null || params.getTargetGenes().size() == 0) {
        		return new ParamValidationResults(false, "You did not load any target genes.");
            }
        }
		return new ParamValidationResults(true, null);
	}
	
	/*
     * (non-Javadoc)
     * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonParameters()
     */
    @Override
	protected Map<Serializable, Serializable> getBisonParameters() {
		log.debug("Reading bison parameters");

		Map<Serializable, Serializable> bisonParameters = new HashMap<Serializable, Serializable>();
		AracneParamPanel paramPanel = (AracneParamPanel) this.aspp;

		if (paramPanel.isDPIToleranceSpecified()) {
			float dpiTolerence = paramPanel.getDPITolerance();
			bisonParameters.put("dpi", dpiTolerence);
		}
		if (paramPanel.isKernelWidthSpecified()) {
			float kernelWidth = paramPanel.getKernelWidth();
			bisonParameters.put("kernel", kernelWidth);
		}

		// TODO allow user to enter many markers or a file of markers
		// String hubMarkersFile = paramPanel.getHubMarkersFile();
		// ArrayList<String> hubGeneList = paramPanel.getHubGeneList();
		String hubGene = paramPanel.getHubGeneString();
		String[] genes = StringUtils.split(hubGene, COMMA_SEP);
		if (genes.length > 1)
			hubGene = genes[0];
		bisonParameters.put("hub", hubGene);

   		bisonParameters.put("isMI", paramPanel.isThresholdMI());		

   		float threshold = paramPanel.getThreshold();
		bisonParameters.put("threshold", threshold);

		return bisonParameters;
	}
    
    /*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getAnalysisName()
	 */
	@Override
	public String getAnalysisName() {
		return analysisName;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonReturnType()
	 */
	@Override
	public Class getBisonReturnType() {
		return AdjacencyMatrix.class;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#useMicroarraySetView()
	 */
	@Override
	protected boolean useMicroarraySetView() {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#useOtherDataSet()
	 */
	@Override
	protected boolean useOtherDataSet() {
		return false;
	}	
}
