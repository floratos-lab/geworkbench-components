package org.geworkbench.components.genspace;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.model.analysis.Analysis;

/**
 * A handler used to log events.
 * 
 * @author sheths
 * @version $Id$
 */
public class ObjectHandler {

	private Log log = LogFactory.getLog(this.getClass());
	private static String lastRunDataSetName = "";
	private static long lastRunTime = 0;
	private long defaultRunTime = 1000 * 60 * 10; // 10 min
	private static String lastTransactionId = "0";
	private static int logStatus = 1; // 0 = log, 1 = log anonymously, 2 = dont
										// log

	public ObjectHandler(Object event, Object source) {

		if (event.getClass().getName()
				.equals("org.geworkbench.events.AnalysisInvokedEvent")) {

			if (logStatus != 2) {

				Method methods[] = event.getClass().getDeclaredMethods();

				Analysis analysis = null;
				String dataSetName = "";

				for (Method m : methods) {
					try {
						if (m.getName().equals("getAnalysis")) {
							analysis = (Analysis) m.invoke(event);
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
				analysisName = ((AbstractAnalysis) analysis).getLabel();
				@SuppressWarnings("rawtypes")
				Map parameters;
				if(analysis.getParameterPanel() != null)
					parameters = analysis.getParameters();
				else
					parameters = new HashMap<Serializable, Serializable>();

				if (logStatus == 0) {
					log.debug("genspace - Logging");

					o = new ObjectLogger();
					o.log(analysisName, dataSetName,
							lastTransactionId, parameters);
				} else if (logStatus == 1) {
					log.debug("genspace - Logging anonymously");
					o = new ObjectLogger();
					o.log(analysisName, dataSetName,
							lastTransactionId, parameters);
				}

			}
		}
	}



	/*
	 * This function will update the lastTransactionId, lastRunTime and
	 * lastRunDataSetName if needed
	 * 
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
	 * This method generates a new random transaction id Changed from the
	 * incrementing id to randomly generating a new one - just incrementing
	 * creates duplicates
	 */
	private void incrementTransactionId() {
		Random r = new Random();
		Integer j = Integer.valueOf(r.nextInt(Integer.MAX_VALUE));
		lastTransactionId = j.toString();
	}

	public static void setLogStatus(int i) {
		// System.out.println(logStatus);
		logStatus = i;
		// System.out.println(logStatus);
	}

}
