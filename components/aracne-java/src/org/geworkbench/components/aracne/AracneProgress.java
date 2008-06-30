package org.geworkbench.components.aracne;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JProgressBar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.threading.SwingWorker;

import wb.plugins.aracne.WeightedGraph;

/**
 * @author mhall
 */
public class AracneProgress implements Observer {
	static Log log = LogFactory.getLog(AracneProgress.class);
	
    SwingWorker<WeightedGraph, Object> worker;
    Thread aracneThread;
    private ProgressBar progressBar = null;

    public AracneProgress(Thread thread) throws HeadlessException {  
        aracneThread = thread;
        progressBar = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
		progressBar.addObserver(this);
		progressBar.setTitle("ARACNE");
		progressBar.setMessage("ARACNE Process Running");
		progressBar.start();
        
        aracneThread.start();
    }
    
    public void update(java.util.Observable ob, Object o) {
		log.debug("initiated close");
		aracneThread.stop();
		log.warn("Cancelling ARACNE Analysis.");
		
		stopProgress();
	}
    
    public void startProgress(){
    	progressBar.start();
    }
    
    public void stopProgress(){
    	progressBar.stop();
    }
}
