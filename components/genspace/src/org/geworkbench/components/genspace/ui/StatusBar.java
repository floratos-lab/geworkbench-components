package org.geworkbench.components.genspace.ui;

import java.awt.BorderLayout;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class StatusBar extends JPanel{
	private static final long serialVersionUID = -567366608355501212L;
	private HashMap<Integer,String> messages;
	
	int evtCt = 0;
	private JProgressBar progressBar;
	private JLabel statusDisplay;
	public StatusBar()
	{
		statusDisplay = new JLabel("  Status: Ready");
		progressBar = new JProgressBar();
		progressBar.setValue(0);
		messages = new HashMap<Integer, String>();
		setLayout(new BorderLayout());
		add(statusDisplay,BorderLayout.NORTH);
		add(progressBar,BorderLayout.CENTER);
	}
	
	private void updateLabel()
	{
		if(messages.size() == 0)
		{
			statusDisplay.setText("  Status: Ready");
			progressBar.setValue(0);
			progressBar.setIndeterminate(false);
			evtCt = 0;
		}
		else if(messages.size() == 1 && messages.values().iterator().hasNext())
		{
			String ev = messages.values().iterator().next();
			statusDisplay.setText("  Status: Loading" + (ev != null ? " ("+ev +")" : ""));
			progressBar.setIndeterminate(true);
		}
		else
		{
			statusDisplay.setText("  Status: Loading (waiting on " + messages.size() + " operations)");
			progressBar.setIndeterminate(true);
		}
	}

	public synchronized int start(String message)
	{
		evtCt++;
		messages.put(evtCt,message);
		updateLabel();
		return evtCt;
	}
	
	public synchronized void stop(int n)
	{
		messages.remove(n);
		updateLabel();
//		GenSpace.logger.info("Called stop, messages = " + messages + "(have " + messages.size() + ")");
	}
}
