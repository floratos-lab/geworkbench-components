package org.geworkbench.components.genspace;

import java.util.Calendar;
import java.io.FileWriter;
import java.net.InetAddress;
import java.io.File;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.FilePathnameUtils;

/**
 * The event logger
 *
 * @author sheths
 */
public class ObjectLogger {

	private Log log = LogFactory.getLog(this.getClass());

	public ObjectLogger(String analysisName, String dataSetName, String transactionId, String username, boolean genspace, Map parameters) {

		try {
			File f = new File( FilePathnameUtils.getUserSettingDirectoryPath() + "geworkbench_log.xml" );

			FileWriter fw = new FileWriter(f, true);

			//log only the file extension and not the filename
			String[] fileName = dataSetName.split("\\.");
			String fileExtension = fileName[fileName.length - 1];

			//fw.write("<measurement>");
			fw.write("\t<metric name=\"analysis\">");
			fw.write("\n\t\t<user name=\""+ username + "\" genspace=\"" + genspace + "\"/>");
			fw.write("\n\t\t<host name=\""+ InetAddress.getLocalHost().getHostName() + "\"/>");
			fw.write("\n\t\t<analysis name=\""+ analysisName + "\"/>");
			fw.write("\n\t\t<dataset name=\""+ fileExtension + "\"/>");
			fw.write("\n\t\t<transaction id=\""+ transactionId + "\"/>");
			fw.write("\n\t\t<time>");

			Calendar c = Calendar.getInstance();

			fw.write("\n\t\t\t<year>" + c.get(Calendar.YEAR) + "</year>");
			fw.write("\n\t\t\t<month>" + (c.get(Calendar.MONTH)+1) + "</month>");
			fw.write("\n\t\t\t<day>" + c.get(Calendar.DATE) + "</day>");
			fw.write("\n\t\t\t<hour>" + c.get(Calendar.HOUR_OF_DAY) + "</hour>");
			fw.write("\n\t\t\t<minute>" + c.get(Calendar.MINUTE) + "</minute>");
			fw.write("\n\t\t\t<second>" + c.get(Calendar.SECOND) + "</second>");
			fw.write("\n\t\t</time>");

			//log the parameters
			Set keys = parameters.keySet();

			fw.write("\n\t\t<parameters count=\"" + keys.size() + "\">");
			int count = 0;

			for (Object key : keys) {
				Object value = parameters.get(key);
				fw.write("\n\t\t\t<parameter id=\"" + count + "\">");
				fw.write("\n\t\t\t\t<key>" + key.toString() + "</key>");
				fw.write("\n\t\t\t\t<value>" + value.toString() + "</value>");
				fw.write("\n\t\t\t</parameter>");

				count++;
			}

			fw.write("\n\t\t</parameters>");

			fw.write("\n\t</metric>\n");
			//fw.write("\n</measurement>\n");

			fw.close();

			//Modified by cheng
			//System.out.println("AAAAAA: " + analysisName);
			//System.out.println("BBBBBB: " + transactionId);
			RealTimeWorkFlowSuggestion.updateCWFStatus(c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),  c.get(Calendar.SECOND),  analysisName, transactionId);

		}
		catch (Exception e) {
			//TODO: Does nothing for now
			//e.printStackTrace();
		}
	}

	void deleteFile() {

		//TODO: Stupid hack... file.delete() doesn't seem to work on windows... find a better way to do this!
		try {
			File f = new File( FilePathnameUtils.getUserSettingDirectoryPath() + "geworkbench_log.xml" );
			FileWriter fw = new FileWriter(f, false);
			fw.close();
		}
		catch (Exception e) {
		    // don't complain if something happens here
		    //e.printStackTrace();
		}
	}
}
