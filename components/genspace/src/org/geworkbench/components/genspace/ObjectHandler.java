package org.geworkbench.components.genspace;

import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Calendar;
import java.util.Random;
import java.util.Scanner;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.model.analysis.Analysis;
import org.geworkbench.util.FilePathnameUtils;
import javax.swing.SwingWorker;

/**
 * A handler used to log events.
 * 
 * @author sheths
 * @version $Id: ObjectHandler.java,v 1.13 2009-06-11 21:06:52 sheths Exp $
 */
public class ObjectHandler {

	private Log log = LogFactory.getLog(this.getClass());
	private String host = RuntimeEnvironmentSettings.EVENT_SERVER.getHost();
	private int port = RuntimeEnvironmentSettings.EVENT_SERVER.getPort();
	private String lookupServer = RuntimeEnvironmentSettings.LOOKUP_HOST;
	private int lookupPort = RuntimeEnvironmentSettings.LOOKUP_PORT;
	private int frequency = 2;
	private static int count = 0;
	private static String lastRunDataSetName = "";
	private static long lastRunTime = 0;
	private long defaultRunTime = 1000 * 60 * 10; // 10 min
	private static String lastTransactionId = "0";
	private static int logStatus = 1; //0 = log, 1 = log anonymously, 2 = dont log
	private static String userName = "";

	public ObjectHandler(Object event, Object source) {

		if (event.getClass().getName().equals("org.geworkbench.events.AnalysisInvokedEvent")) {

			if (logStatus != 2) {

				Method methods[] = event.getClass().getDeclaredMethods();

				Analysis analysis = null;
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

				if (userName.equals("")) {
					username = System.getProperty("user.name"); // so use the system name
				} else
				{
					username = userName;
				}

				for (Method m : methods) {
					try {
						if (m.getName().equals("getAnalysis")) {
							analysis = (Analysis)m.invoke(event);
						} else if (m.getName().equals("getDataSetName")) {
							dataSetName = m.invoke(event).toString();
						}
					} catch (Exception e) {
						log.info("Could not call this method");
					}
				}

				incrementTransactionId(dataSetName);
				ObjectLogger o = null;
				String analysisName = "";
				analysisName = ((AbstractAnalysis)analysis).getLabel();
				Map parameters = analysis.getParameters();

				if (logStatus == 0) {
					log.debug("genspace - Logging");

					o = new ObjectLogger(analysisName, dataSetName,
							lastTransactionId, username, genspace, parameters);
				} else if (logStatus == 1) {
					log.debug("genspace - Logging anonymously");
					o = new ObjectLogger(analysisName, dataSetName,
							lastTransactionId, "anonymous", false, parameters);
				}

				count++;
				if (count % frequency == 0) {
					phoneHome(o);
				}
			}
		}
	}

	/**
	 * This method will phone home and try to send the log file.
	 * If this doesn't work, it will try updating the log server information in case the log server has been changed
	 * @param logger The ObjectLogger object
	 */
	private void phoneHome(final ObjectLogger logger) {

		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			public Void doInBackground() {


				boolean success = sendLogFile(logger);

				if (success != true) {
					//could not send log file to the server.
					//we have 2 possibilities - 1.the server is down, in which case look up the new server
					//or 2.the client is not connected to the internet, so just try again later
					//this is for possibility 1.
					log.info("");
					log.info("genspace - sending log file failed, trying to update the server information");
					log.info("");
					updateLogServer();
					//try resending the log file to the new server
					sendLogFile(logger);
				}
				return null;
			}
		};
		worker.execute();
	}

	/**
	 * This method will send the log files to the current log server
	 * @param logger The ObjectLogger object
	 * @return true if file sent successfully, false otherwise
	 */
	private boolean sendLogFile(ObjectLogger logger) {
		XmlClient x = new XmlClient(host, port);
		boolean success = x.readAndSendFile(FilePathnameUtils.getUserSettingDirectoryPath() + "geworkbench_log.xml");

		if (success == true) {
			log.info("");
			log.info("genspace - log file sent succesfully to the server");
			log.info("");
			logger.deleteFile();
		}
		return success;
	}

	/**
	 * This method will check the lookup server to get the hostname and port number of the new log server and update the information
	 */
	private void updateLogServer() {
		try {
			Socket socket = new Socket(lookupServer, lookupPort);
			Scanner in = new Scanner(socket.getInputStream());
			if (in.hasNext()) {
				String msg = in.nextLine();
				String[] server = msg.split(":");
				//set the host name and port to the new information received
				host = server[0];
				port = Integer.parseInt(server[1]);
				log.info("");
				log.info("genSpace - updated server:" + server[0] + "\nport:" + server[1]);
				log.info("");
			}
			in.close();
			socket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	/**
	 * This method generates a new random transaction id
	 * Changed from the incrementing id to randomly generating a new one - just incrementing creates duplicates
	 */
	private void incrementTransactionId() {
		Random r = new Random();
		int i = Math.abs(r.nextInt());
		Integer j = new Integer(i);
		lastTransactionId = j.toString();
	}

	public static void setLogStatus(int i) {
		//System.out.println(logStatus);
		logStatus = i;
		//System.out.println(logStatus);
	}

	public static void setUserName(String name) {
		//System.out.println(userName);
		userName = name;
		//System.out.println(userName);
	}
}
