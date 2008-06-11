package org.geworkbench.components.analysis;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.PendingNodeCancelledEvent;
import org.geworkbench.events.ProjectNodeCompletedEvent;
import org.ginkgo.labs.ws.GridEndpointReferenceType;

import edu.columbia.geworkbench.cagrid.dispatcher.client.DispatcherClient;

/**
 * A thread that handles remote service polling.
 * 
 * @author keshav
 * @version $Id: PollingThread.java,v 1.6 2008-06-11 21:11:59 chiangy Exp $
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
				if (cancelled) return;
				result = dispatcherClient.getResults(gridEPR);
			}
			ProjectNodeCompletedEvent completedEvent = new ProjectNodeCompletedEvent(
					"Analysis Completed", gridEPR);
			if (result instanceof Exception){
				//generate user understandalbe messages. Detailed information will shown on dispatcher server's log.debug
				String errorMessage = "";
				errorMessage += ((Exception)result).getMessage();
				//TODO: this filter out some messages, there's potential that message do have ":" in it. we should have a way to prevent this.
				//errorMessage=org.geworkbench.bison.util.StringUtils.filter(errorMessage,"^.*:");
				errorMessage=org.geworkbench.bison.util.StringUtils.filter(errorMessage,"java.rmi.RemoteException: "); 
				errorMessage=org.geworkbench.bison.util.StringUtils.filter(errorMessage,"^.*::");
				
				JOptionPane.showMessageDialog(null, errorMessage,
						"Your analysis has been canceled.", JOptionPane.ERROR_MESSAGE);
			}else if (result != null && (result instanceof String)
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
	public void cancel(){
		cancelled = true;
	}
	
	public GridEndpointReferenceType getGridEPR(){
		return gridEPR;
	}
}
