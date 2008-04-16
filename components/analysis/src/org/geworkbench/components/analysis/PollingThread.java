package org.geworkbench.components.analysis;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.events.ProjectNodeCompletedEvent;
import org.ginkgo.labs.ws.GridEndpointReferenceType;

import edu.columbia.geworkbench.cagrid.dispatcher.client.DispatcherClient;

/**
 * A thread that handles remote service polling.
 * 
 * @author keshav
 * @version $Id: PollingThread.java,v 1.4 2008-04-16 17:17:34 chiangy Exp $
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
			if (result instanceof Exception){
				//generate user understandalbe messages. Detailed information will shown on dispatcher server's log.debug
				String errorMessage = "While executing analysis on remote server, an error occurred.\nYour analysis has been canceled.\nDetail error message as follow:\n";
				errorMessage += ((Exception)result).getMessage();
				
				JOptionPane.showMessageDialog(null, errorMessage,
						"Remote Analysis Error", JOptionPane.ERROR_MESSAGE);
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
}
