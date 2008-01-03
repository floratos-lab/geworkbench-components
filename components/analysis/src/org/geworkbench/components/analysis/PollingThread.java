package org.geworkbench.components.analysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.events.ProjectNodeCompletedEvent;
import org.ginkgo.labs.ws.GridEndpointReferenceType;

import edu.columbia.geworkbench.cagrid.dispatcher.client.DispatcherClient;

/**
 * A thread that handles remote service polling.
 * 
 * @author keshav
 * @version $Id: PollingThread.java,v 1.2 2008-01-03 19:27:27 keshav Exp $
 */
public class PollingThread extends Thread {

	private Log log = LogFactory.getLog(this.getClass());

	private AnalysisPanel panel = null;

	private GridEndpointReferenceType gridEPR = null;

	private DispatcherClient dispatcherClient = null;

	public PollingThread(AnalysisPanel panel,
			GridEndpointReferenceType gridEPR, DispatcherClient dispatcherClient) {

		this.panel = panel;
		this.gridEPR = gridEPR;
		this.dispatcherClient = dispatcherClient;

	}

	public void run() {

		try {
			Object result = null;
			while (result == null) {
				log.debug("polling");
				Thread.sleep(10000);
				result = dispatcherClient.getResults(gridEPR);
			}
			ProjectNodeCompletedEvent completedEvent = new ProjectNodeCompletedEvent(
					"Analysis Completed", gridEPR);
			if(result != null && (result instanceof String) && result.equals("null")){
				completedEvent.setDataSet(null);
			}else if(result != null)
				completedEvent.setDataSet((DSDataSet) result);
				
			panel.publishProjectNodeCompletedEvent(completedEvent);
		} catch (Exception e) {
			log
					.error("Error when polling for remote service results.  Error is: ");
			e.printStackTrace();
		}

	}
}
