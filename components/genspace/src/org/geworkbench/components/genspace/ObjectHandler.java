package org.geworkbench.components.genspace;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.model.analysis.Analysis;
import org.geworkbench.components.genspace.server.stubs.Transaction;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.events.AnalysisInvokedEvent;

/**
 * A handler used to log events.
 * 
 * @author sheths
 * @version $Id$
 */
public class ObjectHandler {

	private Log log = LogFactory.getLog(this.getClass());
	private static HashMap<String,Long> lastRunTime = new HashMap<String, Long>();
	private long defaultRunTime = 1000 * 60 * 10; // 10 min
	private static HashMap<String,String> lastTransactionId = new HashMap<String, String>();
	private static int logStatus = 1; // 0 = log, 1 = log anonymously, 2 = dont
										// log
	private static HashMap<String, Long> lastEventCompleteTime = new HashMap<String, Long>();
	
	public ObjectHandler(Object event, Object source) {
		if (event.getClass().getName()
				.equals("org.geworkbench.events.AnalysisInvokedEvent")) {

			if (logStatus != 2) {
				Method methods[] = event.getClass().getDeclaredMethods();
				Analysis analysis = null;
				String dataSetName = "";
				runningAnalyses.put(((AnalysisInvokedEvent) event), null);

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
							lastTransactionId.get(dataSetName), parameters,(AnalysisInvokedEvent) event);
				} else if (logStatus == 1) {
					log.debug("genspace - Logging anonymously");
					o = new ObjectLogger();
					o.log(analysisName, dataSetName,
							lastTransactionId.get(dataSetName), parameters,(AnalysisInvokedEvent) event);
				}

			}
		}
	}
	static HashMap<AnalysisInvokedEvent,Transaction> runningAnalyses = new HashMap<AnalysisInvokedEvent,Transaction>();
	public static void eventCompleted(final AnalysisInvokedEvent invokeEvent)
	{
		if(invokeEvent != null)
			lastEventCompleteTime.put(invokeEvent.getDataSetName(),System.currentTimeMillis());
		final Transaction tr = runningAnalyses.remove(invokeEvent);
		
		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
			
			@Override
			protected Void doInBackground() {
				try
				{
				GenSpaceServerFactory.getUsageOps().analysisEventCompleted(tr,((AbstractAnalysis) invokeEvent.getAnalysis()).getLabel());
				}
				catch(Exception ex)
				{
					//The sound of silence - this probably just means that the server had never received this event, or it's a continuation, etc.
				}
				return null;
			}
		};
		sw.execute();
	}
	public static void eventAborted(final AnalysisInvokedEvent invokeEvent) {
		if(invokeEvent != null)
			lastEventCompleteTime.put(invokeEvent.getDataSetName(),System.currentTimeMillis());
		final Transaction tr = runningAnalyses.remove(invokeEvent);
		SwingWorker<Workflow, Void> sw = new SwingWorker<Workflow, Void>() {
			
			@Override
			protected Workflow doInBackground() throws Exception {
				try
				{
					ObjectLogger.curTransactions.put(invokeEvent.getDataSetName(), GenSpaceServerFactory.getUsageOps().popAnalysisFromTransaction(tr,((AbstractAnalysis) invokeEvent.getAnalysis()).getLabel()));
				}
				catch(Exception ex)
				{
					//The sound of silence - this probably just means that the server had never received this event, or it's a continuation, etc.
				}
				return (ObjectLogger.curTransactions.get(invokeEvent.getDataSetName()) == null ? null : ObjectLogger.curTransactions.get(invokeEvent.getDataSetName()).getWorkflow());
			}
			@Override
			protected void done() {
				try {
					Workflow wf = get();
					if(wf == null)
					{
						wf = new Workflow();
					}
					if(GenSpace.getInstance() != null && GenSpace.getInstance().notebookPanel != null)
						GenSpace.getInstance().notebookPanel.updateFormFields();
					RealTimeWorkFlowSuggestion.cwfUpdated(wf);
				} catch (InterruptedException e) {
					
				} catch (ExecutionException e) {
					
				}
			}
		};
		sw.execute();
	}
	/*
	 * This function will update the lastTransactionId, lastRunTime and
	 * lastRunDataSetName if needed
	 * 
	 * @param dataSetName - the name of the data set the analysis was run on
	 */
	private void incrementTransactionId(String dataSetName) {
			long currentTime = Calendar.getInstance().getTimeInMillis();
			long lastRunTime = ObjectHandler.lastRunTime.get(dataSetName) == null ? 0 : ObjectHandler.lastRunTime.get(dataSetName);
			long lastEventCompleteTime = ObjectHandler.lastEventCompleteTime.get(dataSetName) == null ? 0 : ObjectHandler.lastEventCompleteTime.get(dataSetName);
			if ((currentTime - Math.max(lastRunTime,lastEventCompleteTime)) > defaultRunTime) {
				Random r = new Random();
				Integer j = Integer.valueOf(r.nextInt(Integer.MAX_VALUE));
				lastTransactionId.put(dataSetName, j.toString());
				ObjectLogger.curTransactions.remove(dataSetName); //Will generate a new transaction this way
			}
			ObjectHandler.lastRunTime.put(dataSetName, currentTime);
	}

	

	public static void setLogStatus(int i) {
		logStatus = i;
	}



}
