package org.geworkbench.components.medusa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;

/**
 * 
 * @author keshav
 * @version $Id: MedusaHelper.java,v 1.5 2007-05-18 21:33:48 keshav Exp $
 */
public class MedusaHelper {

	private static Log log = LogFactory.getLog(MedusaHelper.class);

	private static final String TAB_SEPARATOR = "\t";

	public static boolean writeMedusaLabelsFile(
			DSMicroarraySetView microarraySetView, String filename,
			List<DSGeneMarker> regulators, List<DSGeneMarker> targets) {

		BufferedWriter out = null;
		boolean pass = true;
		try {

			out = new BufferedWriter(new FileWriter(filename));

			DSItemList<DSGeneMarker> markers = microarraySetView.allMarkers();
			for (DSGeneMarker marker : markers) {

				double[] data = microarraySetView.getMicroarraySet().getRow(
						marker.getSerial());

				if (data == null)
					continue;

				if (regulators.contains(marker)) {
					out.write('R');
				} else if (targets.contains(marker)) {
					out.write('T');
				} else {
					log.info("Marker " + marker.getLabel()
							+ " neither regulator nor target ... skipping.");
					continue;
				}
				out.write(TAB_SEPARATOR);
				out.write(marker.getLabel());
				out.write(TAB_SEPARATOR);
				for (int j = 0; j < data.length; j++) {
					out.write(String.valueOf(data[j]));
					if (j < data.length - 1)
						out.write(TAB_SEPARATOR);
					else
						out.write("\n");
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			pass = false;
		}

		return pass;
	}
}
