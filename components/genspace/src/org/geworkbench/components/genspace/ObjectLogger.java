package org.geworkbench.components.genspace;

import java.util.Calendar;
import java.io.FileWriter;
import java.net.InetAddress;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The event logger
 * 
 * @author sheths
 */
public class ObjectLogger {

	private Log log = LogFactory.getLog(this.getClass());

	public ObjectLogger(String analysisName, String dataSetName, String username, boolean genspace) {
		try {
			File f = new File("geworkbench_log.xml");
			
			FileWriter fw = new FileWriter(f, true);
			
			//fw.write("<measurement>");
			fw.write("\t<metric name=\"analysis\">");
			fw.write("\n\t\t<user name=\""+ username + "\" genspace=\"" + genspace + "\"/>");
			fw.write("\n\t\t<host name=\""+ InetAddress.getLocalHost().getHostName() + "\"/>");
			fw.write("\n\t\t<analysis name=\""+ analysisName + "\"/>");
			fw.write("\n\t\t<dataset name=\""+ dataSetName + "\"/>");
			fw.write("\n\t\t<time>");
			
			Calendar c = Calendar.getInstance();
			
			fw.write("\n\t\t\t<year>" + c.get(Calendar.YEAR) + "</year>");
			fw.write("\n\t\t\t<month>" + (c.get(Calendar.MONTH)+1) + "</month>");
			fw.write("\n\t\t\t<day>" + c.get(Calendar.DATE) + "</day>");
			fw.write("\n\t\t\t<hour>" + c.get(Calendar.HOUR_OF_DAY) + "</hour>");
			fw.write("\n\t\t\t<minute>" + c.get(Calendar.MINUTE) + "</minute>");
			fw.write("\n\t\t\t<second>" + c.get(Calendar.SECOND) + "</second>");
			fw.write("\n\t\t</time>");
			fw.write("\n\t</metric>\n");
			//fw.write("\n</measurement>\n");
			
			fw.close();
		}
		catch (Exception e) {
			//TODO: Does nothing for now
		}
	}
	
	void deleteFile() {
		try {
			File f = new File("geworkbench_log.xml");
			f.delete();
		} 
		catch (Exception e) {
			//TODO: Does nothing for now
		}
	}
}