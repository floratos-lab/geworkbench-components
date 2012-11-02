package org.geworkbench.components.aracne.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.parsers.ExpressionFileFormat;
import org.geworkbench.parsers.InputFileFormatException; 
import org.geworkbench.util.Util;

import wb.data.Marker;
import wb.data.MarkerSet;
import wb.data.Microarray;
import wb.data.MicroarraySet;
import wb.plugins.aracne.WeightedGraph;

import com.Ostermiller.util.ExcelCSVParser;

import edu.columbia.c2b2.aracne.Aracne;
import edu.columbia.c2b2.aracne.Parameter;

public class PerformanceTest {

	/**
	 * @param args
	 * @throws IOException
	 * @throws InputFileFormatException 
	 */
	public static void main(String[] args) throws IOException, InputFileFormatException {
		DSMicroarraySet dataset = (DSMicroarraySet) new ExpressionFileFormat()
				.getDataFile(new File(
				// "C:\\Users\\zji\\Desktop\\ARACNE_testing\\Bcell-100.exp"));
						"C:\\Users\\zji\\Desktop\\ARACNE_testing\\brain_mas5_176_complete_dataset.exp"));
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(
				dataset);
		// set the following to be true should not have effect, just to make
		// everything is the same as running within geWorkbench
		//microarraySetView.useItemPanel(true);
		//microarraySetView.useMarkerPanel(true);

		final Parameter p = new Parameter();
		List<String> hubGeneList = readHubGeneList(dataset);
		for (String modGene : hubGeneList) {
			DSGeneMarker marker = microarraySetView.markers().get(modGene);
			if (marker == null) {
				System.out.println("Couldn't find marker " + modGene
						+ " specified as hub gene in microarray set.");
				return;
			}
		}
		p.setSubnet(new Vector<String>(hubGeneList));
		p.setPvalue(0.01 / microarraySetView.markers().size());

		// disabled in the case to test
		// if (params.isKernelWidthSpecified()) {
		// p.setSigma(params.getKernelWidth());
		// }
		// if (params.isDPIToleranceSpecified()) {
		// p.setEps(params.getDPITolerance());
		// }
		// List<String> targetGeneList = new ArrayList<String>();
		// p.setTf_list(new Vector<String>(targetGeneList ));

		p.setAlgorithm(Parameter.ALGORITHM.ADAPTIVE_PARTITIONING);
		p.setMode(Parameter.MODE.DISCOVERY);

		String dataSetName = dataset.getDataSetName();
		String ADAPTIVE = "ARACNe_AP";
		String DATASETNAME_ALGORITHM_kernel_file = dataSetName + "_" + ADAPTIVE
				+ "_" + "kernel.txt";
		String DATASETNAME_ALGORITHM_threshold_file = dataSetName + "_"
				+ ADAPTIVE + "_" + "threshold.txt";
		p.setKernelFile(DATASETNAME_ALGORITHM_kernel_file);
		p.setThresholdFile(DATASETNAME_ALGORITHM_threshold_file);

		MicroarraySet microarraySet = convert(microarraySetView);
		WeightedGraph result = Aracne.run(microarraySet, p);
		System.out.println("node count " + result.getNodes().size());
		System.out.println("edge count " + result.getEdges().size());

		Runtime runtime = Runtime.getRuntime();
		long total = runtime.totalMemory();
		long free = runtime.freeMemory();
		long used = total - free;
		final long MEGABYTE = 1024 * 1024;
		System.out.println("Memory: " + used / MEGABYTE + "M Used, " + free
				/ MEGABYTE + "M Free, " + total / MEGABYTE + "M Total.");

		// second round
		result = Aracne.run(microarraySet, p);
		System.out.println("node count " + result.getNodes().size());
		System.out.println("edge count " + result.getEdges().size());

		runtime = Runtime.getRuntime();
		total = runtime.totalMemory();
		free = runtime.freeMemory();
		used = total - free;
		System.out.println("Memory: " + used / MEGABYTE + "M Used, " + free
				/ MEGABYTE + "M Free, " + total / MEGABYTE + "M Total.");
	}

	private static List<String> readHubGeneList(final DSMicroarraySet dataset)
			throws IOException {
		DSPanel<DSGeneMarker> d = getPanelFromSymbols(
				new File(
						"C:\\Users\\zji\\Desktop\\ARACNE_testing\\tf_nov09_entrez_1800_symbols_only.csv"),
				dataset);

		List<String> hubGeneList = new ArrayList<String>();
		for (DSGeneMarker m : d) {
			hubGeneList.add(m.getLabel());
		}

		// BufferedReader br = new BufferedReader(
		// new FileReader(
		// "C:\\Users\\zji\\Desktop\\ARACNE_testing\\tf_nov09_entrez_1800_symbols_only.csv"));
		// String line = br.readLine();
		// while (line != null) {
		// line = line.trim();
		// if (line.length() > 0) {
		// hubGeneList.add(line);
		// }
		// line = br.readLine();
		// }
		return hubGeneList;
	}

	private static MicroarraySet convert(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> inSet) {
		MarkerSet markers = new MarkerSet();
		for (DSGeneMarker marker : inSet.markers()) {
			markers.addMarker(new Marker(marker.getLabel()));
		}
		MicroarraySet returnSet = new MicroarraySet(inSet.getDataSet()
				.getDataSetName(), inSet.getDataSet().getID(), "Unknown",
				markers);
		DSItemList<DSMicroarray> arrays = inSet.items();
		for (DSMicroarray microarray : arrays) {
			float[] markerData = new float[markers.size()];
			int i = 0;
			for (DSGeneMarker marker : inSet.markers()) {
				markerData[i++] = (float) microarray.getMarkerValue(marker)
						.getValue();
			}
			returnSet.addMicroarray(new Microarray(microarray.getLabel(),
					markerData));
		}
		return returnSet;
	}

	private static DSPanel<DSGeneMarker> getPanelFromSymbols(final File file,
			final DSMicroarraySet dataset) {
		FileInputStream inputStream = null;
		String filename = file.getName();
		if (filename.toLowerCase().endsWith(".csv")) {
			filename = filename.substring(0, filename.length() - 4);
		}
		// Ensure loaded file has unique name
		Set<String> nameSet = new HashSet<String>();
		filename = Util.getUniqueName(filename, nameSet);
		DSPanel<DSGeneMarker> panel = new CSPanel<DSGeneMarker>(filename);

		List<String> selectedNames = new ArrayList<String>();
		try {
			inputStream = new FileInputStream(file);
			ExcelCSVParser parser = new ExcelCSVParser(inputStream);
			String[][] data = parser.getAllValues();
			for (int i = 0; i < data.length; i++) {
				String[] line = data[i];
				if (line.length > 0) {
					selectedNames.add(line[0]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}

		DSItemList<DSGeneMarker> itemList = new CSItemList<DSGeneMarker>();
		itemList.addAll(dataset.getMarkers());
		for (DSGeneMarker marker : itemList) {
			if (selectedNames.contains(marker.getGeneName()))
				panel.add(marker);
		}

		return panel;
	}
}
