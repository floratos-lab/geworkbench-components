package org.geworkbench.components.genspace;

import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.geworkbench.components.genspace.server.stubs.AnalysisEvent;
import org.geworkbench.components.genspace.server.stubs.AnalysisEventParameter;
import org.geworkbench.components.genspace.server.stubs.Transaction;
import org.geworkbench.components.genspace.server.stubs.WorkflowTool;
import org.geworkbench.events.AnalysisInvokedEvent;
import org.geworkbench.util.FilePathnameUtils;

/**
 * The event logger
 * 
 * @author sheths
 */
public class ObjectLogger {

//	private Log log = LogFactory.getLog(this.getClass());

	public static HashMap<String, Transaction> curTransactions  =new HashMap<String, Transaction>();
	public ObjectLogger()
	{
		
	}
	public void log(final String analysisName,final String dataSetName,
			final String transactionId,
			@SuppressWarnings("rawtypes") final Map parameters, final AnalysisInvokedEvent event) {
			SwingWorker<Transaction, Void > worker = new SwingWorker<Transaction, Void>()
			{
				@Override
				protected void done() {
					Transaction ret = null;
					try {
						ret = get();
					} catch (InterruptedException e) {

					} catch (ExecutionException e) {
						GenSpaceServerFactory.clearCache();
					}
					
					if(ret != null)
					{
						curTransactions.put(dataSetName, ret);
					}
					
					super.done();
				}
				@Override
				protected Transaction doInBackground(){
//					System.out.println("Background logging "  + analysisName);
					String hostname = "";
					try {
						hostname = InetAddress.getLocalHost().getHostName();
					} catch (UnknownHostException e1) {
//						e1.printStackTrace();
					}
					Transaction curTransaction = curTransactions.get(dataSetName);
					if(curTransaction == null || !curTransaction.getClientID().equals(GenSpaceServerFactory.getUsername() + hostname + transactionId))
					{
						
						curTransaction = new Transaction();
						curTransaction.setDataSetName(dataSetName);
						try {
							curTransaction.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
						} catch (DatatypeConfigurationException e1) {
							e1.printStackTrace();
						} //TODO verify
						curTransaction.setClientID(GenSpaceServerFactory.getUsername() + hostname + transactionId);
						curTransaction.setHostname(hostname);
						curTransaction.setUser(GenSpaceServerFactory.getUser());
					}
					File f = new File(FilePathnameUtils.getUserSettingDirectoryPath()
							+ "geworkbench_log.xml");
					if(f.exists())
					{
						//Try to send up the log file
						XMLLoader loader =new XMLLoader();
						ArrayList<AnalysisEvent> pending = loader.readAndLoad(FilePathnameUtils.getUserSettingDirectoryPath()
								+ "geworkbench_log.xml");
						Transaction done = null;
						try
						{
							done= (Transaction) (GenSpaceServerFactory.getUsageOps().sendUsageLog((pending)));
						}
						catch(Exception ex)
						{
//							ex.printStackTrace();
							//be silent
						}
						if(done != null)
						{
							for(WorkflowTool t : done.getWorkflow().getTools())
							{
								t.setTool(RuntimeEnvironmentSettings.tools.get(t.getTool().getId()));
							}
							f.delete();
							Calendar recentTime = Calendar.getInstance();
							recentTime.add(Calendar.MINUTE, -20);
							if(pending.get(pending.size()).getCreatedAt().toGregorianCalendar().after(recentTime))
							{
								RealTimeWorkFlowSuggestion.cwfUpdated(done.getWorkflow());
								curTransactions.put(dataSetName, done);
							}
						}
					}
					AnalysisEvent e = new AnalysisEvent();
					e.setToolname(analysisName);

					try {
						e.setCreatedAt(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
					} catch (DatatypeConfigurationException e2) {
						e2.printStackTrace();
					} 
					e.setTransaction(curTransaction);
					HashSet<AnalysisEventParameter> params = new HashSet<AnalysisEventParameter>();
					if(parameters != null)
						for(Object key : parameters.keySet())
						{
							if(!key.equals("referenceListSets"))
							{
							AnalysisEventParameter p = new AnalysisEventParameter();
							p.setParameterKey(key.toString());
							p.setParameterValue(parameters.get(key).toString());
							params.add(p);
							}
						}
					e.getParameters().addAll(params);
					try
					{
						Transaction retTrans = (GenSpaceServerFactory.getUsageOps().sendUsageEvent((e))); //try to send the log event
						curTransactions.put(dataSetName, retTrans);
						GenSpaceServerFactory.clearCache();
						if(retTrans != null)
						{
							for(WorkflowTool t : retTrans.getWorkflow().getTools())
							{
								int id = t.getTool().getId();
								t.getTool().getRatings().clear();
								t.getTool().setRef(null);
								t.getTool().getComments().clear();
								t.setTool(null);
								t.setTool(RuntimeEnvironmentSettings.tools.get(id));
							}
							ObjectHandler.runningAnalyses.put(event,retTrans);
							RealTimeWorkFlowSuggestion.cwfUpdated(retTrans.getWorkflow());
							return retTrans;
						}
					}
					catch(Exception ex)
					{
//						ex.printStackTrace();
						//be silent on errors... if we get them, we'll just log to the file instead
					}
					
					try {
						
						if(!f.exists())
							f.createNewFile();
						
						FileWriter fw = new FileWriter(f, true);

						// log only the file extension and not the filename
						String[] fileName = dataSetName.split("\\.");
						String fileExtension = fileName[fileName.length - 1];

						// fw.write("<measurement>");
						fw.write("\t<metric name=\"analysis\">");
						fw.write("\n\t\t<user name=\"" + GenSpaceServerFactory.getUsername() + "\" genspace=\""
								+1+ "\"/>");
												
						fw.write("\n\t\t<host name=\""
								+ InetAddress.getLocalHost().getHostName() + "\"/>");
						fw.write("\n\t\t<analysis name=\"" + analysisName + "\"/>");
						fw.write("\n\t\t<dataset name=\"" + fileExtension + "\"/>");
						fw.write("\n\t\t<transaction id=\"" + GenSpaceServerFactory.getUsername() + hostname +  transactionId + "\"/>");
						fw.write("\n\t\t<time>");

						Calendar c = Calendar.getInstance();

						fw.write("\n\t\t\t<year>" + c.get(Calendar.YEAR) + "</year>");
						fw.write("\n\t\t\t<month>" + (c.get(Calendar.MONTH) )
								+ "</month>");
						fw.write("\n\t\t\t<day>" + c.get(Calendar.DATE) + "</day>");
						fw.write("\n\t\t\t<hour>" + c.get(Calendar.HOUR_OF_DAY) + "</hour>");
						fw.write("\n\t\t\t<minute>" + c.get(Calendar.MINUTE) + "</minute>");
						fw.write("\n\t\t\t<second>" + c.get(Calendar.SECOND) + "</second>");
						fw.write("\n\t\t</time>");

						if(parameters != null)
						{
							// log the parameters
							@SuppressWarnings("rawtypes")
							Set keys = parameters.keySet();
	
							fw.write("\n\t\t<parameters count=\"" + keys.size() + "\">");
							int count = 0;
	
							for (Object key : keys) {
								Object value = parameters.get(key);
								fw.write("\n\t\t\t<parameter id=\"" + count + "\">");
								fw.write("\n\t\t\t\t<key>" + key.toString() + "</key>");
								fw.write("\n\t\t\t\t<value>" + value.toString() + "</value>");
								fw.write("\n\t\t\t</parameter>");
	
								count++;
							}
	
							fw.write("\n\t\t</parameters>");
						}
						else
						{
							fw.write("\n\t\t<parameters count=\"0\">");

							fw.write("\n\t\t</parameters>");
						}
						fw.write("\n\t</metric>\n");
						// fw.write("\n</measurement>\n");

						fw.close();
					} catch (Exception e1) {
						GenSpace.logger.warn("Unable to write log file",e1);
					}
					
					return null;
				}
			};
			worker.execute();
	
	}

	void deleteFile() {

		// find a better way to do this!
		try {
			File f = new File(FilePathnameUtils.getUserSettingDirectoryPath()
					+ "geworkbench_usage_log.xml");
			FileWriter fw = new FileWriter(f, false);
			fw.close();
		} catch (Exception e) {
			// don't complain if something happens here
			// e.printStackTrace();
		}
	}
}
