package edu.columbia.geworkbench.cagrid.converter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.axis.encoding.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.columbia.geworkbench.cagrid.microarray.Marker;
import edu.columbia.geworkbench.cagrid.microarray.Microarray;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySet;
import edu.duke.cabig.rproteomics.model.statml.ArrayType;
import edu.duke.cabig.rproteomics.model.statml.ArrayTypeType;
import edu.duke.cabig.rproteomics.model.statml.DataType;
import edu.duke.cabig.rproteomics.model.statml.ListType;
import edu.duke.cabig.rproteomics.model.statml.ScalarType;

/**
 * A converter that contains methods for conversion of basic data structures.
 * 
 * @author keshav
 * @version $Id: Converter.java,v 1.2 2007-02-09 17:18:00 keshav Exp $
 */
public class Converter {

	private static final Log log = LogFactory.getLog(Converter.class);

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
	 * @return DataType
	 */
	public static DataType float2DToDataType(float[][] data) {

		int numMarkers = data.length;
		int numMicroarrays = data[0].length;

		log.debug("data set contains " + numMicroarrays + " microarrays");
		log.debug("data set contains " + numMarkers + " markers");

		DataType microarraySet = new DataType();
		ListType microarrays = new ListType();
		ListType markers = new ListType();

		// FIXME should have a marker equivalent of constructing this matrix
		// set array data
		for (int j = 0; j < numMicroarrays; j++) {
			float[] col = new float[numMarkers];
			for (int i = 0; i < data.length; i++) {
				col[i] = data[i][j];
			}

			ArrayType array = new ArrayType();
			array.setName("array" + j);
			String base64Value = Converter.base64Encode(col);
			array.set_value(base64Value);
			array.setType(ArrayTypeType.value5);
			microarrays.setArray(array);
		}

		// set marker names
		for (int i = 0; i < numMarkers; i++) {

			ScalarType scalar = new ScalarType();
			scalar.setName(i + "_at");
			markers.setScalar(scalar);
		}

		microarraySet.setList(markers);
		microarraySet.setList(microarrays);

		return microarraySet;
	}

	/**
	 * @param farray
	 * @return byte[]
	 */
	public static byte[] floatArrayToBytes(float[] farray) {
		if (farray == null)
			return null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			for (int i = 0; i < farray.length; i++) {
				dos.writeDouble(farray[i]);
			}
		} catch (IOException e) {
			throw new RuntimeException(
					"Error converting float array to byte array.");
		}
		return bos.toByteArray();
	}

	/**
	 * 
	 * @param data
	 * @return String
	 */
	public static String base64Encode(float[] data) {
		byte[] bdata = Converter.floatArrayToBytes(data);
		String base64Value = Base64.encode(bdata);
		return base64Value;
	}
}
