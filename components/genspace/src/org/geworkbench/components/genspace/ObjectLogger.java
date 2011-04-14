package org.geworkbench.components.genspace;

import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.geworkbench.components.genspace.entity.AnalysisEvent;
import org.geworkbench.components.genspace.entity.AnalysisEventParameter;
import org.geworkbench.components.genspace.entity.Transaction;
import org.geworkbench.util.FilePathnameUtils;

/**
 * The event logger
 * 
 * @author sheths
 */
public class ObjectLogger {

//	private Log log = LogFactory.getLog(this.getClass());

	public static Transaction curTransaction = null;
	
	public ObjectLogger()
	{
		
	}
	public void log(final String analysisName,final String dataSetName,
			final String transactionId,
			@SuppressWarnings("rawtypes") final Map parameters) {

		
			SwingWorker<Transaction, Void > worker = new SwingWorker<Transaction, Void>()
			{
				@Override
				protected void done() {
					Transaction ret = null;
					try {
						ret = get();
					} catch (InterruptedException e) {

					} catch (ExecutionException e) {
					}
					
					if(ret != null)
					{
						curTransaction = ret;
					}
					
					super.done();
				}
				@Override
				protected Transaction doInBackground(){
					if(curTransaction == null || !curTransaction.getClientID().equals(transactionId))
					{
						String hostname = "";
						try {
							hostname = InetAddress.getLocalHost().getHostName();
						} catch (UnknownHostException e1) {
							e1.printStackTrace();
						}
						curTransaction = new Transaction();
						curTransaction.setDataSetName(dataSetName);
						curTransaction.setDate(new Date());
						curTransaction.setClientID(transactionId);
						curTransaction.setHostname(hostname);
						curTransaction.setUser(LoginFactory.getUser());
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
							done= LoginFactory.getUsageOps().sendUsageLog(pending);
						}
						catch(Exception ex)
						{
							//be silent
						}
						if(done != null)
						{
							f.delete();
							RealTimeWorkFlowSuggestion.cwfUpdated(done.getWorkflow());
							curTransaction = done;
						}
					}
					AnalysisEvent e = new AnalysisEvent();
					e.setToolname(analysisName);
					e.setCreatedAt(new Date());
					e.setTransaction(curTransaction);
					HashSet<AnalysisEventParameter> params = new HashSet<AnalysisEventParameter>();
					for(Object key : parameters.keySet())
					{
						AnalysisEventParameter p = new AnalysisEventParameter();
						p.setEvent(e);
						p.setParameterKey(key.toString());
						p.setParameterValue(parameters.get(key).toString());
						params.add(p);
					}
					e.setParameters(params);
					try
					{
						Transaction retTrans = LoginFactory.getUsageOps().sendUsageEvent(e); //try to send the log event
						if(retTrans != null)
						{
							RealTimeWorkFlowSuggestion.cwfUpdated(retTrans.getWorkflow());
							return retTrans;
						}
					}
					catch(Exception ex)
					{
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
						fw.write("\n\t\t<user name=\"" + LoginFactory.getUsername() + "\" genspace=\""
								+1+ "\"/>");
												
						fw.write("\n\t\t<host name=\""
								+ InetAddress.getLocalHost().getHostName() + "\"/>");
						fw.write("\n\t\t<analysis name=\"" + analysisName + "\"/>");
						fw.write("\n\t\t<dataset name=\"" + fileExtension + "\"/>");
						fw.write("\n\t\t<transaction id=\"" + transactionId + "\"/>");
						fw.write("\n\t\t<time>");

						Calendar c = Calendar.getInstance();

						fw.write("\n\t\t\t<year>" + c.get(Calendar.YEAR) + "</year>");
						fw.write("\n\t\t\t<month>" + (c.get(Calendar.MONTH) + 1)
								+ "</month>");
						fw.write("\n\t\t\t<day>" + c.get(Calendar.DATE) + "</day>");
						fw.write("\n\t\t\t<hour>" + c.get(Calendar.HOUR_OF_DAY) + "</hour>");
						fw.write("\n\t\t\t<minute>" + c.get(Calendar.MINUTE) + "</minute>");
						fw.write("\n\t\t\t<second>" + c.get(Calendar.SECOND) + "</second>");
						fw.write("\n\t\t</time>");

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

						fw.write("\n\t</metric>\n");
						// fw.write("\n</measurement>\n");

						fw.close();
					} catch (Exception e1) {
						GenSpace.logger.error("Unable to write log file",e1);
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
