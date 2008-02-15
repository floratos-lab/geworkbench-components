package org.geworkbench.components.genspace;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ojb.otm.lock.ObjectLock;

/**
 * A handler used to log events.
 * 
 * @author sheths
 * @version $Id: ObjectHandler.java,v 1.3 2008-02-15 23:40:03 sheths Exp $
 */
public class ObjectHandler {

	private Log log = LogFactory.getLog(this.getClass());
	private String host = "beach.cs.columbia.edu";
	private int port = 12346;
	private int frequency = 2;
	private static int count = 0;

	public ObjectHandler(Object event, Object source) {
		
		if (event.getClass().getName().equals("org.geworkbench.events.AnalysisInvokedEvent")) {
			
			Method methods[] = event.getClass().getDeclaredMethods();

			String analysisName = "";
			String dataSetName = "";
			String username;
			boolean genspace = genspaceLogin.isLoggedIn;
			
			if (genspace) {
				username = genspaceLogin.genspaceLogin;
			} else {
				username = System.getProperty("user.name");
			}
			
			for (Method m : methods) {
				try {
					if (m.getName().equals("getAnalysisName")) {
						analysisName = m.invoke(event).toString();
					}
					else if (m.getName().equals("getDataSetName")) {
						dataSetName = m.invoke(event).toString();
					}
				}
				catch (Exception e) {
					log.info("Could not call this method");
				}
			}
			
			ObjectLogger o = new ObjectLogger(analysisName, dataSetName, username, genspace);
			
			count++;
			if (count%frequency == 0) {
				XmlClient x = new XmlClient(host, port);
				boolean success = x.readAndSendFile("geworkbench_log.xml");

				if (success == true) {
					System.out.println("file sent succesfully");
					o.deleteFile();
				}
			}	
		}
	}
}