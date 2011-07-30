package org.geworkbench.components.genspace;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.logging.Logger;

import javax.swing.SwingWorker;

import org.geworkbench.engine.config.MenuListener;


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
		
		SwingWorker<Void, Void> wkr = new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() {
				try
				{
				GenSpaceServerFactory.getUsageOps().getAllTools();
				}
				catch(Exception e)
				{
					
				}
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
