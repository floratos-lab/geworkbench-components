package org.geworkbench.components.analysis;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.events.AnalysisAbortEvent;
import org.geworkbench.events.AnalysisCompleteEvent;
import org.geworkbench.events.AnalysisInvokedEvent;
import org.ginkgo.labs.ws.GridEndpointReferenceType;

import edu.columbia.geworkbench.cagrid.dispatcher.client.DispatcherClient;

/**
 * A thread that handles remote service polling.
 * 
 * @author keshav
 * @version $Id$
 */
public class PollingThread extends Thread {

	private static Log log = LogFactory.getLog(PollingThread.class);

	private GridEndpointReferenceType gridEPR = null;

	private DispatcherClient dispatcherClient = null;

	volatile private boolean cancelled = false;
	
	final AnalysisInvokedEvent invokeEvent;
	final AnalysisPanel analysisPanel;
	
	final AbstractGridAnalysis analysis;

	public PollingThread(GridEndpointReferenceType gridEPR,
			DispatcherClient dispatcherClient,
			final AnalysisInvokedEvent invokeEvent,
			final AnalysisPanel analysisPanel,
			final AbstractGridAnalysis analysis) {

		this.gridEPR = gridEPR;
		this.dispatcherClient = dispatcherClient;
		this.invokeEvent = invokeEvent;
		this.analysisPanel = analysisPanel;
		
		this.analysis = analysis; 
	}

	public void run() {

		try {
			Object result = null;
			while (result == null) {
				log.debug("polling");
				Thread.sleep(10000);
				if (cancelled) {
					analysisPanel.publishAnalysisAbortEvent(new AnalysisAbortEvent(invokeEvent));
					return;
				}
				try {
					result = dispatcherClient.getResults(gridEPR);
				} catch (Throwable e) {
					result = e;
				}
			}
			DSAncillaryDataSet<? extends DSBioObject> ancillaryDataSet = null;
			if (result instanceof Throwable) {
				/*
				 * Generate user understandable messages. Detailed information
				 * is located on dispatcher server's log.debug
				 */
				String errorMessage = "";
				errorMessage += ((Throwable) result).getMessage();

				/* This filters out some messages */
				errorMessage = org.geworkbench.util.Util.filter(
						errorMessage, "java.rmi.RemoteException: ");

				if (result instanceof OutOfMemoryError){
					errorMessage = "Out-of-memory error: " + errorMessage
					+ "\n\nIt is advisable to restart geWorkbench."
					+ "\n\nYou may also wish to increase the geWorkbench memory size.";
				}

				JOptionPane.showMessageDialog(null, errorMessage,
						"Your analysis has been canceled.",
						JOptionPane.ERROR_MESSAGE);
			} else {
				ancillaryDataSet = analysis.postProcessResult(result);
			}
			ProjectPanel.getInstance().processNodeCompleted(gridEPR, ancillaryDataSet);
		} catch (Exception e) {
			log
					.error("Error when polling for remote service results.  Error is: ");
			e.printStackTrace();
			analysisPanel.publishAnalysisAbortEvent(new AnalysisAbortEvent(invokeEvent));
			return;
		}
		analysisPanel.publishAnalysisCompleteEvent(new AnalysisCompleteEvent(invokeEvent));

	}

	public void cancel() {
		cancelled = true;
	}

	public GridEndpointReferenceType getGridEPR() {
		return gridEPR;
	}
}
