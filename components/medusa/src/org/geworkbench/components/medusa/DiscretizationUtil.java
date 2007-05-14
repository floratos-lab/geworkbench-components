package org.geworkbench.components.medusa;

import org.apache.commons.lang.StringUtils;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;

/**
 * 
 * @author keshav
 * @version $Id: DiscretizationUtil.java,v 1.2 2007-05-14 14:43:11 keshav Exp $
 */
public class DiscretizationUtil {

	/**
	 * 
	 * @param microarraySetView
	 * @param base
	 * @param bound
	 * @return
	 */
	public DSMicroarraySetView<DSGeneMarker, DSMicroarray> discretize(
			DSMicroarraySetView microarraySetView, double base, double bound) {

		DSMicroarraySet microarraySet = microarraySetView.getMicroarraySet();

		/* extract microarray info from DSMicroarraySet */
		int numArrays = microarraySetView.size();

		/* extract marker info from DSMicroarraySet */
		int numMarkers = ((DSMicroarray) microarraySet.get(0)).getMarkerNo();

		for (int i = 0; i < numArrays; i++) {
			/* geworkbench array */
			DSMicroarray microarray = (DSMicroarray) microarraySetView.get(i);
			float data[] = microarray.getRawMarkerData();
			String name = microarray.getLabel();
			if (name == null || StringUtils.isEmpty(name))
				name = "i";// give array a name

			float[] ddata = discretize(data, base, bound);
			DSMicroarray discreteMicroarray = microarray;

			for (int j = 0; j < ddata.length; j++) {

				DSMutableMarkerValue markerValue = discreteMicroarray
						.getMarkerValue(j);
				markerValue.setValue(ddata[j]);
				discreteMicroarray.setMarkerValue(j, markerValue);
			}
			microarraySet.setLabel(microarraySet.getLabel());
			microarraySet.add(discreteMicroarray);
		}

		return microarraySetView;
	}

	/**
	 * 
	 * @param data
	 * @param base
	 * @param bound
	 * @return
	 */
	public float[] discretize(float[] data, double base, double bound) {
		float[] discreteData = new float[data.length];

		double pinterval = base + bound;

		double ninterval = base - bound;

		for (int i = 0; i < discreteData.length; i++) {

			float val = data[i];
			if ((ninterval) <= val && val <= (pinterval)) {
				discreteData[i] = 0;
			} else if (val < ninterval) {
				discreteData[i] = -1;
			} else {
				discreteData[i] = 1;
			}
		}

		return discreteData;

	}

}
