package org.geworkbench.components.mindy;

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
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyDataSet;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import edu.columbia.c2b2.mindy.Mindy;
import edu.columbia.c2b2.mindy.MindyResults;
import wb.data.MicroarraySet;
import wb.data.Microarray;
import wb.data.MarkerSet;
import wb.data.Marker;

/**
 * @author Matt Hall
 */
public class MindyAnalysis extends AbstractAnalysis implements ClusteringAnalysis {
    Log log = LogFactory.getLog(this.getClass());

    private static final String TEMP_DIR = "temporary.files.directory";

    public MindyAnalysis() {
        setLabel("MINDY");
        setDefaultPanel(new MindyParamPanel());
    }

    // not used
    public int getAnalysisType() {
        return AbstractAnalysis.ZERO_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        log.debug("input: " + input);
        // Use this to get params
        MindyParamPanel params = (MindyParamPanel) aspp;
        DSMicroarraySet<DSMicroarray> mSet = ((DSMicroarraySetView) input).getMicroarraySet();

//        MindyData loadedData = null;
//        try {
//            loadedData = MindyResultsParser.parseResults((CSMicroarraySet) mSet, new File(params.getCandidateModulatorsFile()));
//        } catch (IOException e) {
//            log.error(e);
//        }

        ArrayList<Marker> modulators = new ArrayList<Marker>();
        try {
            File modulatorFile = new File(params.getCandidateModulatorsFile());
            BufferedReader reader = new BufferedReader(new FileReader(modulatorFile));
            String modulator = reader.readLine();
            while (modulator != null) {
                DSGeneMarker marker = mSet.getMarkers().get(modulator);
                if (marker == null) {
                    log.info("Couldn't find marker " + modulator + " from modulator file in microarray set.");
                } else {
                    modulators.add(new Marker(modulator));
                }
                modulator = reader.readLine();
            }
        } catch (IOException e) {
            log.error(e);
        }

        Mindy mindy = new Mindy();
        DSGeneMarker transFac = mSet.getMarkers().get(params.getTranscriptionFactor());
        MindyResults results = mindy.runMindy(convert(mSet), new Marker(params.getTranscriptionFactor()), modulators,
                params.getSetFraction()/100f, params.getDPITolerance());
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

        MindyDataSet dataSet = new MindyDataSet(mSet, "MINDY Results", loadedData, params.getCandidateModulatorsFile());
        return new AlgorithmExecutionResults(true, "MINDY Results Loaded.", dataSet);

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

    @Publish
    public MindyDataSet publishMatrixReduceSet(MindyDataSet data) {
        return data;
    }
}
