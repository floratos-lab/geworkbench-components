package org.geworkbench.components.genspace;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import org.geworkbench.engine.config.MenuListener;

import com.sun.logging.LogDomains;

/**
 * A menu for the genSpace component.
 * 
 * @author sheths
 */
public class GenSpaceMenu implements MenuListener {
	public static GenSpace genspace;
	Logger glassfishLogger;
	Logger glassfishLogger2;
	public GenSpaceMenu()
	{
		glassfishLogger = LogDomains.getLogger(com.sun.enterprise.v3.server.CommonClassLoaderServiceImpl.class, LogDomains.LOADER_LOGGER);
		glassfishLogger.setLevel(Level.OFF);
		
		glassfishLogger2 = Logger.getLogger("javax.enterprise.resource.corba.ORBUtil");
		glassfishLogger2.setLevel(Level.OFF);
		SwingWorker<Void, Void> wkr = new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() throws Exception {
				GenSpaceServerFactory.getPublicFacade().getAllTools();
				return null;
			}
			
		};
		wkr.execute();
	}
	@Override
	public ActionListener getActionListener(String var) {
		if (var.equalsIgnoreCase("Tools.genSpace")) {
			return new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(genspace == null)
						genspace = new GenSpace();
					else
					{
						genspace.jframe.setVisible(true);
						genspace.jframe.toFront();
					}
				}
			};
		}
		return null;
	}
	
}
