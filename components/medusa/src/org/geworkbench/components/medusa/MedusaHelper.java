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
 * @version $Id: MedusaHelper.java,v 1.1 2007-05-11 17:00:16 keshav Exp $
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

			DSItemList<DSGeneMarker> markers = (DSItemList<DSGeneMarker>) microarraySetView
					.markers();

			out = new BufferedWriter(new FileWriter(filename));

			for (DSGeneMarker marker : markers) {
				double[] data = microarraySetView.getRow(marker);

				if (regulatorNames.contains(marker.getLabel())) {
					out.write('R');
				} else if (targetNames.contains(marker.getLabel())) {
					out.write('T');
				} else {
					log.info("marker " + marker.getLabel()
							+ " neither regulator nor target.");
					continue;
				}
				out.write(TAB_SEPARATOR);
				out.write(marker.getLabel());
				out.write(TAB_SEPARATOR);
				for (int i = 0; i < data.length; i++) {
					out.write(String.valueOf(data[i]));
					if (i < data.length - 1)
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
