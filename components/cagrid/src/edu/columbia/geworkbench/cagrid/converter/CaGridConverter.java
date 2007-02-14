package edu.columbia.geworkbench.cagrid.converter;

import org.apache.axis.types.NonNegativeInteger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.ginkgo.labs.converter.BasicConverter;

import edu.columbia.geworkbench.cagrid.microarray.Marker;
import edu.columbia.geworkbench.cagrid.microarray.Microarray;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySet;
import edu.duke.cabig.rproteomics.model.statml.Array;
import edu.duke.cabig.rproteomics.model.statml.Data;
import edu.duke.cabig.rproteomics.model.statml.Scalar;

/**
 * Converts to/from cagrid microarray set types from/to geworkbench microarray
 * set types.
 * 
 * @author keshav
 * @version $Id: CagridMicroarrayTypeConverter.java,v 1.2 2007/01/04 22:03:15
 *          watkinson Exp $
 */
public class CaGridConverter {
	private static Log log = LogFactory.getLog(CaGridConverter.class);

	/**
	 * Convert to edu.columbia.geworkbench.cagrid.microarray.MicroarraySet from
	 * org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView
	 * 
	 * @param microarraySetView
	 * @return MicroarraySet
	 */
	public static MicroarraySet convertToCagridMicroarrayType(
			DSMicroarraySetView microarraySetView) {

		DSMicroarraySet microarraySet = microarraySetView.getMicroarraySet();

		/* extract microarray info from DSMicroarraySet */
		int numArrays = microarraySetView.size();
		String microarraySetName = microarraySet.getDataSetName();

		Microarray[] gridMicroarrays = new Microarray[numArrays];
		for (int i = 0; i < numArrays; i++) {
			/* geworkbench array */
			DSMicroarray microarray = (DSMicroarray) microarraySetView.get(i);
			float data[] = microarray.getRawMarkerData();
			String name = microarray.getLabel();
			if (name == null || StringUtils.isEmpty(name))
				name = "i";// give array a name

			/* cagrid array */
			Microarray gridMicroarray = new Microarray();
			gridMicroarray.setArrayData(data);
			gridMicroarray.setArrayName(name);
			gridMicroarrays[i] = gridMicroarray;
		}

		/* extract marker info from DSMicroarraySet */
		int numMarkers = ((DSMicroarray) microarraySet.get(0)).getMarkerNo();

		Marker[] gridMarkers = new Marker[numMarkers];
		int i = 0;
		for (DSGeneMarker marker : (DSItemList<DSGeneMarker>) microarraySetView
				.markers()) {
			Marker gridMarker = new Marker();
			gridMarker.setMarkerName(marker.getLabel());
			gridMarkers[i] = gridMarker;
			i++;
		}

		/* cagrid array set */
		MicroarraySet gridMicroarraySet = new MicroarraySet();
		gridMicroarraySet.setName(microarraySetName);
		gridMicroarraySet.setMicroarray(gridMicroarrays);
		gridMicroarraySet.setMarker(gridMarkers);
		// TODO set to get(set)Microarrays and get(set)Markers

		return gridMicroarraySet;
	}

	/**
	 * 
	 * @param microarraySetView
	 * @return Data
	 */
	public static Data convertToCagridData(
			DSMicroarraySetView microarraySetView) {

		DSMicroarraySet microarraySet = microarraySetView.getMicroarraySet();

		/* extract microarray info from DSMicroarraySet */
		int numArrays = microarraySetView.size();
		String arrayName = microarraySet.getDataSetName();

		/* extract marker info from DSMicroarraySet */
		int numMarkers = ((DSMicroarray) microarraySet.get(0)).getMarkerNo();

		Array[] arrays = new Array[numArrays];
		NonNegativeInteger numArraysAsNonNegInteger = new NonNegativeInteger(
				String.valueOf(numArrays));

		for (int i = 0; i < numArrays; i++) {
			/* geworkbench array */
			DSMicroarray microarray = (DSMicroarray) microarraySetView.get(i);
			float data[] = microarray.getRawMarkerData();
			String name = microarray.getLabel();
			if (name == null || StringUtils.isEmpty(name))
				name = "i";// give array a name

			/* cagrid array */
			Array array = new Array();
			String base64Value = BasicConverter.base64Encode(data);
			array.setBase64Value(base64Value);
			array.setName(name);
			array.setType("float");
			array.setDimensions(String.valueOf(numMarkers));
			arrays[i] = array;
		}

		Scalar[] markers = new Scalar[numMarkers];

		int i = 0;
		for (DSGeneMarker marker : (DSItemList<DSGeneMarker>) microarraySetView
				.markers()) {
			Scalar scalar = new Scalar();
			scalar.setName(String.valueOf(i));
			scalar.setValue(marker.getLabel());
			scalar.setType("String");
			markers[i] = scalar;
			i++;
		}

		/* cagrid array set */
		Data dataType = new Data();
		dataType.setArray(arrays);
		dataType.setScalar(markers);

		return dataType;
	}

	/**
	 * Convert from edu.columbia.geworkbench.cagrid.microarray.MicroarraySet to
	 * org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView.
	 * 
	 * @param gridMicroarraySet
	 * @return DSMicroarraySet
	 */
	public static DSMicroarraySetView convertFromCagridMicroarrayType(
			MicroarraySet gridMicroarraySet) {

		/* microarray info */
		int numMarkers = gridMicroarraySet.getMicroarray().length;
		String microarraySetName = gridMicroarraySet.getName();
		Microarray[] gridMicroarrays = gridMicroarraySet.getMicroarray();

		DSMicroarraySetView microarraySetView = new CSMicroarraySetView();
		DSMicroarraySet microarraySet = new CSMicroarraySet();
		microarraySet.setLabel(microarraySetName);

		for (int i = 0; i < numMarkers; i++) {
			/* cagrid array */
			float[] arrayData = gridMicroarrays[i].getArrayData();
			String arrayName = gridMicroarrays[i].getArrayName();

			/* bison array */
			DSMicroarray microarray = new CSMicroarray(arrayData.length);
			microarray.setLabel(arrayName);
			for (int j = 0; j < arrayData.length; j++) {
				DSMarkerValue markerValue = new CSExpressionMarkerValue(
						arrayData[j]);
				microarray.setMarkerValue(j, markerValue);
			}
			microarraySet.add(i, microarray);
		}

		// I need to add the marker names
		microarraySetView.setMicroarraySet(microarraySet);

		return microarraySetView;
	}

	/**
	 * @param data
	 * @return MicroarraySet
	 */
	public static MicroarraySet float2DToMicroarraySet(float[][] data) {

		int numMarkers = data.length;
		int numMicroarrays = data[0].length;

		log.debug("data set contains " + numMicroarrays + " microarrays");
		log.debug("data set contains " + numMarkers + " markers");

		MicroarraySet microarraySet = new MicroarraySet();
		Microarray microarrays[] = new Microarray[numMicroarrays];
		Marker markers[] = new Marker[numMarkers];
		// FIXME should have a marker equivalent of constructing this matrix
		// set array data
		for (int j = 0; j < numMicroarrays; j++) {
			float[] col = new float[numMarkers];
			for (int i = 0; i < data.length; i++) {
				col[i] = data[i][j];
			}
			Microarray microarray = new Microarray();
			microarray.setArrayName("array" + j);
			microarray.setArrayData(col);
			microarrays[j] = microarray;
		}

		// set marker names
		for (int i = 0; i < numMarkers; i++) {
			Marker marker = new Marker();
			marker.setMarkerName(i + "_at");
			markers[i] = marker;
		}

		microarraySet.setName("A test microaray set");
		microarraySet.setMicroarray(microarrays);
		microarraySet.setMarker(markers);
		return microarraySet;
	}

	/**
	 * 
	 * @param data
	 * @return Data
	 */
	public static Data float2DToDataType(float[][] data) {

		int numMarkers = data.length;
		int numMicroarrays = data[0].length;

		log.debug("data set contains " + numMicroarrays + " microarrays");
		log.debug("data set contains " + numMarkers + " markers");

		Data microarraySet = new Data();
		Array[] microarrays = new Array[numMicroarrays];
		Scalar[] markers = new Scalar[numMarkers];

		// FIXME should have a marker equivalent of constructing this matrix
		// set array data
		for (int j = 0; j < numMicroarrays; j++) {
			float[] col = new float[numMarkers];
			for (int i = 0; i < data.length; i++) {
				col[i] = data[i][j];
			}

			Array array = new Array();
			array.setName("array" + j);
			array.setType("float");
			array.setDimensions(String.valueOf(numMarkers));
			String base64Value = BasicConverter.base64Encode(col);
			array.setBase64Value(base64Value);
			microarrays[j] = array;
		}

		// set marker names
		for (int i = 0; i < numMarkers; i++) {
			Scalar scalar = new Scalar();
			scalar.setName(String.valueOf(i));
			scalar.setValue(i + "_at");
			scalar.setType("String");
			markers[i] = scalar;
		}

		microarraySet.setScalar(markers);
		microarraySet.setArray(microarrays);

		return microarraySet;
	}
}
