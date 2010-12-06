package org.geworkbench.components.analysis;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.builtin.projects.ProjectPanel;
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

	public PollingThread(GridEndpointReferenceType gridEPR,
			DispatcherClient dispatcherClient) {

		this.gridEPR = gridEPR;
		this.dispatcherClient = dispatcherClient;

	}

	@SuppressWarnings({"unchecked" })
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
			DSAncillaryDataSet<? extends DSBioObject> ancillaryDataSet = null;
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
			} else if (result instanceof DSAncillaryDataSet) {
				ancillaryDataSet = ((DSAncillaryDataSet<? extends DSBioObject>) result);
			}
			ProjectPanel.getInstance().processNodeCompleted(gridEPR, ancillaryDataSet);
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