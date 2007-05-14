package org.geworkbench.components.medusa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

/**
 * 
 * @author keshav
 * @version $Id: MedusaHelper.java,v 1.2 2007-05-14 14:56:45 keshav Exp $
 */
public class MedusaHelper {

	private static Log log = LogFactory.getLog(MedusaHelper.class);

	private static final String TAB_SEPARATOR = "\t";

	public static boolean writeMedusaLabelsFile(
			DSMicroarraySetView microarraySetView, String filename,
			List<String> regulatorNames, List<String> targetNames) {

		BufferedWriter out = null;
		boolean pass = true;
		try {

			out = new BufferedWriter(new FileWriter(filename));

			int markerSize = microarraySetView.getMarkerPanel().size();

			for (int i = 0; i < markerSize; i++) {

				DSGeneMarker marker = (CSGeneMarker) microarraySetView
						.getMarkerPanel().get(i);
				double[] data = microarraySetView.getRow(i);

				if (data == null)
					continue;

				if (regulatorNames.contains(marker.getLabel())) {
					out.write('R');
				} else if (targetNames.contains(marker.getLabel())) {
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
