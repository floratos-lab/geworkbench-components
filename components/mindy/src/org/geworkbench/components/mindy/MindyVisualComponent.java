package org.geworkbench.components.mindy;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.builtin.projects.ProjectTreeNode;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.*;
import org.geworkbench.util.pathwaydecoder.mutualinformation.*;
import org.geworkbench.util.threading.SwingWorker;
import org.geworkbench.bison.datastructure.complex.panels.*;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.ImageIcon;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * @author mhall
 * @author ch2514
 * @version $ID$
 */
@AcceptTypes(MindyDataSet.class)
public class MindyVisualComponent implements VisualPlugin {
    static Log log = LogFactory.getLog(MindyVisualComponent.class);

    private MindyDataSet dataSet;
    private JPanel plugin;
    private MindyPlugin mindyPlugin;
    private List<DSGeneMarker> selectedMarkers;
    private DSPanel<DSGeneMarker> selectorPanel;
    private HashMap<ProjectTreeNode, MindyPlugin> ht;
    
    private JDialog dialog;
    private JProgressBar progressBar;
    private JButton cancelButton;
    private Task task;
    
    private int prevStateMarkers = -1;
    private int currentStateMarkers = -1;
    private int maxMarkers = -1;

    /**
     * Constructor.
     * Includes a place holder for a MINDY result view (i.e. class MindyPlugin).
     *
     */
    public MindyVisualComponent() {
        // Just a place holder
    	ht = new HashMap<ProjectTreeNode, MindyPlugin>(5);
        plugin = new JPanel(new BorderLayout());
        selectorPanel = null;
        selectedMarkers = null;
    }

    /**
     * @return The MINDY result view component (of class MindyPlugin)
     */
    public Component getComponent() {
        return plugin;
    }

    /**
     * Receives the general ProjectEvent from the framework.
     * Creates MINDY's data set based on data from the ProjectEvent.
     * @param projectEvent 
     * @param source - source of the ProjectEvent
     */
    @Subscribe public void receive(ProjectEvent projectEvent, Object source) {
        log.debug("MINDY received project event.");
        plugin.removeAll();
        DSDataSet data = projectEvent.getDataSet();
        ProjectTreeNode node = ((ProjectPanel) source).getSelection().getSelectedNode();
        if ((data != null) && (data instanceof MindyDataSet)) {
        	// Check to see if the hashtable has a mindy plugin associated with the selected project tree node
        	if(ht.containsKey(node)){
        		// if so, set mindyPlugin to the one stored in the hashtable
        		mindyPlugin = (MindyPlugin) ht.get(node);
        	} else {
        		// if not, create a brand new mindyPlugin, add to the hashtable (with key=selected project tree node)
                if (dataSet != data) {
                    dataSet = ((MindyDataSet) data);   
                    
                    // Create mindy gui and display an indeterminate progress bar in the foreground
                    createProgressBarDialog();
                    task = new Task(this);
                    task.execute();     
                    dialog.setVisible(true);
                    
                    // Register the mindy plugin with our hashtable for keeping track
                	if(mindyPlugin != null) ht.put(node, mindyPlugin);
                	else {
                		log.error("Failed to create MINDY plugin for project node: " + node);
                		return;
                	}  
                }
        	}     
            
            // Display the plugin  
        	plugin.add(mindyPlugin, BorderLayout.CENTER); 
        	plugin.revalidate();
            plugin.repaint();
        }
    }

    /**
     * Receives GeneSelectorEvent from the framework.
     * Extracts markers in the selected marker sets from the Selector Panel.
     * @param e - GeneSelectorEvent
     * @param source - source of the GeneSelectorEvent
     */
    @SuppressWarnings("unchecked")
    @Subscribe
    public void receive(GeneSelectorEvent e, Object source) {
        if (dataSet != null) {
        	if(e.getPanel() != null) 
        		this.selectorPanel = e.getPanel();
        	else 
        		log.debug("Received Gene Selector Event: Selection panel sent was null");
        	
        	DSMicroarraySetView<DSGeneMarker, DSMicroarray> maView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(dataSet.getData().getArraySet());
            maView.setMarkerPanel(e.getPanel());
            maView.useMarkerPanel(true);
            if((maView.getMarkerPanel() != null) 
            		&& (maView.getMarkerPanel().activeSubset() != null) 
            		&& (maView.getMarkerPanel().activeSubset().size() == 0)
            		) {            	
                selectedMarkers = null;
                this.prevStateMarkers = this.currentStateMarkers;
                this.currentStateMarkers = this.maxMarkers;
                if(this.currentStateMarkers == this.prevStateMarkers)
                	return;
            } else {
            	try{
	            	if((maView != null) 
	            			&& (maView.getUniqueMarkers() != null)
	            			){
		                DSItemList<DSGeneMarker> uniqueMarkers = maView.getUniqueMarkers();
		                if (uniqueMarkers.size() > 0) {
		                	selectedMarkers = (List<DSGeneMarker>) uniqueMarkers;
		                	this.prevStateMarkers = this.currentStateMarkers;
		                	this.currentStateMarkers = selectedMarkers.size();		                	
		                	/*
		                    selectedMarkers = new ArrayList<DSGeneMarker>(uniqueMarkers.size());
		                    for (Iterator<DSGeneMarker> iterator = uniqueMarkers.iterator(); iterator.hasNext();) {
		                        DSGeneMarker marker = iterator.next();
		                        log.debug("Selected " + marker.getShortName());
		                        selectedMarkers.add(marker);
		                    }
		                    */
		                }
	            	}
            	} catch (NullPointerException npe) {
            		npe.printStackTrace();
            		log.debug("Gene Selector Event contained no marker data.");
            	}
            }          
            
            
            Iterator it = ht.values().iterator();
            if (selectedMarkers != null) {    
            	while(it.hasNext()){
            		log.debug("***received gene selector event::calling limitMarkers");
            		((MindyPlugin) it.next()).limitMarkers(selectedMarkers);
            	}
            } else {            	
            	while(it.hasNext()){
            		log.debug("***received gene selector event::calling limitMarkers with null");
            		((MindyPlugin) it.next()).limitMarkers(null);
            	}
            }
        } else {
        	log.debug("Received Gene Selector Event: Dataset in this component is null");
        }
    }

    /**
     * Publish SubpanelChangedEvent to the framework to add selected markers to the marker set(s)
     * on the Selector Panel.
     * @param e - SubpanelChangedEvent
     * @return
     */
    @Publish public SubpanelChangedEvent publishSubpanelChangedEvent(SubpanelChangedEvent e){
    	return e;
    }

    /**
     * Publish ImageSnapshotEvent to the framework to capture an image of the heat map.
     * @param heatmap - MINDY's heat map panel
     * @return ImageSnapshotEvent
     */
    @Publish public ImageSnapshotEvent createImageSnapshot(Component heatmap) {
        Dimension panelSize = heatmap.getSize();
        BufferedImage image = new BufferedImage(panelSize.width, panelSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        heatmap.print(g);
        ImageIcon icon = new ImageIcon(image, "MINDY Heat Map");
        org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent("MINDY Heat Map", icon, org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
        return event;
    }
    
    DSPanel getSelectorPanel(){
    	return this.selectorPanel;
    }

    List<DSGeneMarker> getSelectedMarkers(){
    	return this.selectedMarkers;
    }
    
    private void createProgressBarDialog(){
    	// lay the groundwork for the progress bar dialog
        dialog = new JDialog();
        progressBar = new JProgressBar();
        cancelButton = new JButton("Cancel");
        dialog.setLayout(new BorderLayout());
        dialog.setModal(true);
        dialog.setTitle("MINDY GUI processing.");
        dialog.setSize(300, 50);
        dialog.setLocation((int) (dialog.getToolkit().getScreenSize().getWidth() - dialog.getWidth()) / 2, (int) (dialog.getToolkit().getScreenSize().getHeight() - dialog.getHeight()) / 2);
        progressBar.setIndeterminate(true);
        dialog.add(progressBar, BorderLayout.CENTER);
        dialog.add(cancelButton, BorderLayout.EAST);
        
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
            	if((task != null) && (!task.isCancelled()) && (!task.isDone())) {
            		task.cancel(true);
            		log.warn("Cancelling Mindy GUI.");
            	}
            	dialog.setVisible(false);
            	dialog.dispose();            	
            }
        });

        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
            	if((task != null) && (!task.isCancelled()) && (!task.isDone())){
            		task.cancel(true);
            		log.warn("Cancelling Mindy GUI.");
            	}
            }
        });
    }
    
    class Task extends SwingWorker<MindyPlugin, Void> {
    	private MindyPlugin mplugin;
    	private MindyVisualComponent vp;
    	
    	public Task(MindyVisualComponent vp){
    		this.vp = vp;
    	}
    	
    	public MindyPlugin doInBackground(){
    		log.info("Creating MINDY GUI.");  		
            
            MindyData mindyData = dataSet.getData();
            // The following sort no longer required -- keeping it in case the use case changes again....
            	// Sort via |M+ - M-| i.e. Math.abs(mindy score)
            	// Take largest 100 out of the results
            	//Collections.sort(mindyData.getData(), new MindyRowComparator(MindyRowComparator.DELTA_I, false));            
            List<MindyData.MindyResultRow> mindyRows = mindyData.getData();
            mindyData.setData(mindyRows);                    
            
            // Then pass into mindy plugin
            mplugin = new MindyPlugin(mindyData, vp);
            
            // Incorporate selections from marker set selection panel
            
        	DSMicroarraySetView<DSGeneMarker, DSMicroarray> maView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(dataSet.getData().getArraySet());
            DSItemList<DSGeneMarker> uniqueMarkers = maView.getUniqueMarkers();
            maxMarkers = uniqueMarkers.size();
            currentStateMarkers = maxMarkers;            
            maView.useMarkerPanel(true);
            
            // ch2514 -- Do we need this check??
            /*
            if (uniqueMarkers.size() > 0) {
                selectedMarkers = new ArrayList<DSGeneMarker>(uniqueMarkers.size());
                for (Iterator<DSGeneMarker> iterator = uniqueMarkers.iterator(); iterator.hasNext();) {
                    DSGeneMarker marker = iterator.next();
                    log.debug("Selected " + marker.getShortName());
                    selectedMarkers.add(marker);
                }
            }
            //system.out.println("VP::doInBackground()::");
            if(uniqueMarkers != null){
            	//system.out.println("uniqueMarkers=" + uniqueMarkers.size());
            	if(selectedMarkers != null){
            		//system.out.println("selected markers=" + selectedMarkers.size());
            	} else {
            		//system.out.println("selected markers=null");
            	}
            } else {
            	//system.out.println("uniqueMarkers=null");
            	if(selectedMarkers != null){
            		//system.out.println("selected markers=" + selectedMarkers.size());
            	} else {
            		//system.out.println("selected markers=null");
            	}
            }
            mplugin.limitMarkers(selectedMarkers); 
            */
            
    		return mplugin;
    	}
    	
    	public void done(){
    		if(!this.isCancelled()){
	    		try{
	    			mindyPlugin = get();    			
	    			log.debug("Transferring mindy plugin back to event thread.");
	    		} catch (Exception e) {
	    			log.error("Exception in finishing up worker thread that is creating the new MINDY GUI: " + e.getMessage(), e);
	    		}
    		}
    		dialog.setVisible(false);
    		dialog.dispose();
    		log.debug("Closing progress bar dialog.");
    	}
    }
}
