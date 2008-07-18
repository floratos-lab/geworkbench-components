package org.geworkbench.components.genspace;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ojb.otm.lock.ObjectLock;

/**
 * A handler used to log events.
 * 
 * @author sheths
 * @version $Id: ObjectHandler.java,v 1.6 2008-07-18 23:53:41 sheths Exp $
 */
public class ObjectHandler {

	private Log log = LogFactory.getLog(this.getClass());
	private String host = "vesey.cs.columbia.edu";
	private int port = 12346;
	private int frequency = 2;
	private static int count = 0;
	private static String lastRunDataSetName = "";
	private static long lastRunTime = 0;
	private long defaultRunTime = 1000 * 60; // 1 min
	private static String lastTransactionId = "0";
	private static int logStatus = 1; //0 = log, 1 = log anonymously, 2 = dont log

	public ObjectHandler(Object event, Object source) {

		if (event.getClass().getName().equals("org.geworkbench.events.AnalysisInvokedEvent")) {

			if (logStatus != 2) {

				Method methods[] = event.getClass().getDeclaredMethods();

				String analysisName = "";
				String dataSetName = "";
				String username;

				/*
				  // change this when we start using genSpace logins
				boolean genspace = genspaceLogin.isLoggedIn;

				if (genspace) {
					username = genspaceLogin.genspaceLogin;
				} else {
					username = System.getProperty("user.name");
				}
				*/

				// this is temporary
				boolean genspace = false; // for now they are NOT logged in
				username = System.getProperty("user.name"); // so use the system name

				for (Method m : methods) {
					try {
						if (m.getName().equals("getAnalysisName")) {
							analysisName = m.invoke(event).toString();
						} else if (m.getName().equals("getDataSetName")) {
							dataSetName = m.invoke(event).toString();
						}
					} catch (Exception e) {
						log.info("Could not call this method");
					}
				}

				incrementTransactionId(dataSetName);
				ObjectLogger o = null;
				if (logStatus == 0) {
					log.debug("genspace - Logging");
					o = new ObjectLogger(analysisName, dataSetName,
							lastTransactionId, username, genspace);
				} else if (logStatus == 1) {
					log.debug("genspace - Logging anonymously");
					o = new ObjectLogger(analysisName, dataSetName,
							lastTransactionId, "anonymous", false);
				}

				count++;
				if (count % frequency == 0) {
					XmlClient x = new XmlClient(host, port);
					boolean success = x.readAndSendFile("geworkbench_log.xml");

					if (success == true) {
						log.info("");
						log.info("genspace - log file sent succesfully to the server");
						log.info("");
						o.deleteFile();
					}
				}
			}
		}
	}

	/*
	 * This function will update the lastTransactionId, lastRunTime and lastRunDataSetName if needed
	 * @param dataSetName - the name of the data set the analysis was run on
	 */
	private void incrementTransactionId(String dataSetName) {
		if (dataSetName.equals(lastRunDataSetName)) {
			long currentTime = Calendar.getInstance().getTimeInMillis();
			if ((currentTime - lastRunTime) <= defaultRunTime) {
				lastRunTime = currentTime;
			} else {
				lastRunTime = currentTime;
				incrementTransactionId();
			}
		} else {
			lastRunDataSetName = dataSetName;
			lastRunTime = Calendar.getInstance().getTimeInMillis();
			incrementTransactionId();
		}
	}

	private void incrementTransactionId() {
		String last = lastTransactionId;
		int i = Integer.parseInt(last);
		i++;
		Integer j = new Integer(i);
		lastTransactionId = j.toString();
	}

	protected static void setLogStatus(int i) {
		//System.out.println(logStatus);
		logStatus = i;
		//System.out.println(logStatus);
	}
}
