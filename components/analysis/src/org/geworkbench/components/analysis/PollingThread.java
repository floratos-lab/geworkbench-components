package org.geworkbench.components.analysis;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.events.ProjectNodeCompletedEvent;
import org.ginkgo.labs.ws.GridEndpointReferenceType;

import edu.columbia.geworkbench.cagrid.dispatcher.client.DispatcherClient;

/**
 * A thread that handles remote service polling.
 * 
 * @author keshav
 * @version $Id: PollingThread.java,v 1.8 2009-02-24 18:12:18 keshav Exp $
 */
public class PollingThread extends Thread {

	private Log log = LogFactory.getLog(this.getClass());

	private AnalysisPanel panel = null;

	private GridEndpointReferenceType gridEPR = null;

	private DispatcherClient dispatcherClient = null;

	private boolean cancelled = false;

	public PollingThread(AnalysisPanel panel,
			GridEndpointReferenceType gridEPR, DispatcherClient dispatcherClient) {

		this.panel = panel;
		this.gridEPR = gridEPR;
		this.dispatcherClient = dispatcherClient;

	}

	@SuppressWarnings("unchecked")
	public void run() {

		try {
			Object result = null;
			while (result == null) {
				log.debug("polling");
				Thread.sleep(10000);
				if (cancelled)
					return;
				try {
					result = dispatcherClient.getResults(gridEPR);
				} catch (Exception e) {
					result = e;
				}
			}
			ProjectNodeCompletedEvent completedEvent = new ProjectNodeCompletedEvent(
					"Analysis Completed", gridEPR);
			if (result instanceof Exception) {
				/*
				 * Generate user understandable messages. Detailed information
				 * is located on dispatcher server's log.debug
				 */
				String errorMessage = "";
				errorMessage += ((Exception) result).getMessage();

				/* This filters out some messages */
				errorMessage = org.geworkbench.bison.util.StringUtils.filter(
						errorMessage, "java.rmi.RemoteException: ");

				JOptionPane.showMessageDialog(null, errorMessage,
						"Your analysis has been canceled.",
						JOptionPane.ERROR_MESSAGE);
			} else if (result != null && (result instanceof String)
					&& result.equals("null")) {
				completedEvent.setDataSet(null);
			} else if (result != null)
				completedEvent.setAncillaryDataSet((DSAncillaryDataSet) result);
			panel.publishProjectNodeCompletedEvent(completedEvent);
		} catch (Exception e) {
			log
					.error("Error when polling for remote service results.  Error is: ");
			e.printStackTrace();
		}

	}

	public void cancel() {
		cancelled = true;
	}

	public GridEndpointReferenceType getGridEPR() {
		return gridEPR;
	}
}
