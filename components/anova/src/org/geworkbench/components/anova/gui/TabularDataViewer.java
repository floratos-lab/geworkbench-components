package org.geworkbench.components.anova.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSAnovaResultSet;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.ProgressBar;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.binding.beans.Model;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
/**
 * This is an example geWorkbench component.
 * 
 * @author Mark Chiang
 * @version $Id$
 */
// This annotation lists the data set types that this component accepts.
// The component will only appear when a data set of the appropriate type is
// selected.
@AcceptTypes({DSAnovaResultSet.class})
public class TabularDataViewer extends JPanel implements
		VisualPlugin {
	private static final long serialVersionUID = 2021859129692430268L;
	
	private DSAnovaResultSet<? extends DSGeneMarker> anovaResultSet;
	private TableViewer TV = null;

	//preferences
	private boolean fStat=true;
	private boolean pVal=true;
	private boolean adjPVal=false;	//pVal showed here is already adjusted.
	private boolean mean=true;
	private boolean std=true;
	private String[] header;
	private DispPref DP=null; //Panel for "Display Preference", make it global so it won't popup multiple times.  
	
	public TabularDataViewer() {
		this.setLayout(new BorderLayout());
		//add a space on top and add a button "Display Preference" on the right.
		JPanel panelDispPref = new JPanel();
		panelDispPref.setLayout(new BorderLayout());
		add(panelDispPref,java.awt.BorderLayout.NORTH);
		JButton PrefButton = new JButton("Display Preference");
		panelDispPref.setLayout(new BorderLayout());
		panelDispPref.add(PrefButton,java.awt.BorderLayout.EAST);
		
		PrefButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (DP==null){
					DP=new DispPref();
				}else{
					DP.popup.setVisible(true);
					DP.popup.toFront();
					DP.popup.requestFocus();
					DP.popup.requestFocusInWindow();
				}
		    }
		});
		
		//hold the place for the table, because we need to add export after it.
		if (TV == null) {
			TV = new TableViewer();
			add(TV);
		} else {
			TV = new TableViewer();
		}
		
		//export panel
		JPanel panelExport = new JPanel();
		panelExport.setLayout(new BorderLayout());
		add(panelExport,java.awt.BorderLayout.SOUTH);
		JButton exportButton = new JButton("Export");
		panelExport.setLayout(new BorderLayout());
		panelExport.add(exportButton,java.awt.BorderLayout.EAST);
		
		exportButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				export();
		    }
		});
	}

	/**
	 * This method fulfills the contract of the {@link VisualPlugin} interface.
	 * It returns the GUI component for this visual plugin.
	 */
	public Component getComponent() {
		// In this case, this object is also the GUI component.
		return this;
	}

	/**
	 * This is a <b>Subscribe</b> method. The annotation before the method
	 * alerts the engine that it should route published objects to this method.
	 * The type of objects that are routed to this method are indicated by the
	 * first parameter of the method. In this case, it is {@link ProjectEvent}.
	 * 
	 * @param event
	 *            the received object.
	 * @param source
	 *            the entity that published the object.
	 */
	@SuppressWarnings("unchecked")
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<? extends DSBioObject> dataSet = event.getDataSet();

		// We will act on this object if it is a DSMicroarraySet
		if (dataSet instanceof DSAnovaResultSet) {
			anovaResultSet = (DSAnovaResultSet) dataSet;
			refreshTableViewer();
		}
	}
	
	/* This function fill the array A, and generate a new TableViewer to show in the UI.
	 * 
	 */
	private void refreshTableViewer(){
		//since this procedure only take very short time to finish even for large dataset, I set useProgressBar to false for now.
		boolean useProgressBar=false;	
		int groupNum=anovaResultSet.getLabels(0).length;
		int meanStdStartAtIndex=1+(fStat?1:0)+(pVal?1:0)+(adjPVal?1:0);
		header=new String[meanStdStartAtIndex+groupNum*((mean?1:0)+(std?1:0))];
		int fieldIndex=0;
		header[fieldIndex++]="Marker Name";
		if (pVal){header[fieldIndex++]="P-Value";};
		if (adjPVal){header[fieldIndex++]="Adj-P-Value";};
		if (fStat){header[fieldIndex++]="F-statistic";};
		for (int cx=0;cx<groupNum;cx++){
			if (mean){
				header[meanStdStartAtIndex+cx*((mean?1:0)+(std?1:0))+0]=anovaResultSet.getLabels(0)[cx]+"_Mean";
			}
			if (std){
				header[meanStdStartAtIndex+cx*((mean?1:0)+(std?1:0))+(mean?1:0)]=anovaResultSet.getLabels(0)[cx]+"_Std";
			}
		}

		Object[][] A = new Object[anovaResultSet.getSignificantMarkers().size()][header.length];
		ProgressBar pb=null;
		if (useProgressBar){
	        pb = ProgressBar.create(ProgressBar.BOUNDED_TYPE);
	        pb.setTitle("Refreshing Table");
	        pb.reset();
	        pb.updateTo(0);
	        pb.start();
		}
        //try a quicker version
        
        double[][] result2DArray=anovaResultSet.getResult2DArray();
        int significantMarkerNumbers=anovaResultSet.getSignificantMarkers().size();
		for (int cx = 0; cx < significantMarkerNumbers; cx++) {
			fieldIndex=0;
			A[cx][fieldIndex++] = ((DSGeneMarker)anovaResultSet.getSignificantMarkers().get(cx)).getShortName();
			if (pVal){A[cx][fieldIndex++] = new Float(result2DArray[0][cx]);};
			//TODO: change float to Float object
			if (adjPVal){A[cx][fieldIndex++] = result2DArray[1][cx];};
			if (fStat){A[cx][fieldIndex++] = result2DArray[2][cx];};
			for (int gc=0;gc<groupNum;gc++){
				if (mean){
					A[cx][meanStdStartAtIndex+gc*((mean?1:0)+(std?1:0))+0]=result2DArray[3+gc*2][cx];
//					A[cx][meanStdStartAtIndex+gc*((mean?1:0)+(std?1:0))+0]=anovaResultSet.getMean(aMarker, anovaResultSet.getLabels(0)[gc]);
				}
				if (std){
					A[cx][meanStdStartAtIndex+gc*((mean?1:0)+(std?1:0))+(mean?1:0)]=result2DArray[4+gc*2][cx];
//					A[cx][meanStdStartAtIndex+gc*((mean?1:0)+(std?1:0))+(mean?1:0)]=anovaResultSet.getDeviation(aMarker, anovaResultSet.getLabels(0)[gc]);
				}
			}
			if (useProgressBar){
				pb.setMessage(cx+"/"+result2DArray[0].length+" finished...");
				pb.updateTo(cx*100/result2DArray[0].length);
			}
			Thread.yield();
		}

		if (TV == null) {
			TV = new TableViewer(header, A);
			//TODO: according to total number of columns, software choose one of the following. 
			//this line make the table sizable, but probably not large enough, and can not be scrolled horizontal.
//			TV.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			//this line make the table large enough and horizontal scrollalbe, but non-sizable. 				
//			TV.getTable().setPreferredSize(new Dimension(TV.getTable().getColumnCount()*100,TV.getTable().getRowHeight()*TV.getTable().getRowCount()));
			add(TV,java.awt.BorderLayout.CENTER);
			TV.updateUI();
		} else {
			remove(TV);
			TV = new TableViewer(header, A);
			TV.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			add(TV,java.awt.BorderLayout.CENTER);
			TV.updateUI();
		}		
		if (useProgressBar){
			pb.stop();
			pb.dispose();
		}
	}

	/* This function popup a file chooser and save the table as a CSV file using that file name
	 * 
	 */
	public void export(){
		JFileChooser jFC=new JFileChooser();

		//We remove "all files" from filter, since we only allow CSV format
		FileFilter ft = jFC.getAcceptAllFileFilter();
		jFC.removeChoosableFileFilter(ft);
		
		TabularFileFilter filter = new TabularFileFilter();
        jFC.setFileFilter(filter);
        
		int returnVal = jFC.showSaveDialog(this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    try {
				String tabFilename; 
				tabFilename = jFC.getSelectedFile().getAbsolutePath();
				if (!tabFilename.toLowerCase().endsWith("." + filter.getExtension().toLowerCase())) {
					tabFilename += "." + filter.getExtension();
				}
		        BufferedWriter out = new BufferedWriter(new FileWriter(tabFilename));
		        out.write(this.toCVS());
		        out.close();
		    } catch (IOException e) {
		    	e.printStackTrace();
		    }
	    }
	}
	
	private String toCVS(){
		String answer = "";
		
        boolean newLine=true;
        //print the header
        
        
        for (int cx=0;cx<TV.getTable().getColumnCount();cx++){
			if (newLine){
				newLine=false;
			}else{
				answer += ",";
			}
			answer += "\""+TV.getTable().getColumnName(cx)+"\"";
        }
		answer += "\n";
		newLine=true;
        
        //print the table
		for (int cx=0;cx<TV.getTable().getRowCount();cx++){
			for (int cy=0;cy<TV.getTable().getColumnCount();cy++){
				//TODO: I do csv as in AnnotationsPanel, but csv shouldn't do this way. Should have error checking and conversion.
				if (newLine){
					newLine=false;
				}else{
					answer += ",";
				}
				answer += "\""+TV.getTable().getValueAt(cx, cy)+"\"";
			}
			answer += "\n";
			newLine=true;
		}		        
		return answer;
	}
	
	/* This is a JDialog box which shows the options for user to check or uncheck.
	 * When user check a checkbox, preferences variables will be changed and refreshTableViewer will be called to redraw the table.
	 */ 
	private class DispPref{
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		JCheckBox bF;
        JCheckBox bP;
        JCheckBox bA;
        JCheckBox bM;
        JCheckBox bS;
        public JDialog popup=null; 
        
		public DispPref(){
//			System.out.println("Preference button pressed."); // TODO Auto-generated Event stub actionPerformed()
			FormLayout layout = new FormLayout("right:max(80dlu;pref), 3dlu, max(70dlu;pref), 3dlu, max(70dlu;pref), 3dlu, max(70dlu;pref)","");
//			DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p, 2dlu, p"));
	        
	        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
	        builder.setDefaultDialogBorder();
	        builder.appendSeparator("Select the columns to display in the Tabular View");
	        bF=new JCheckBox("F-Statistic");
	        
	        ToggleChangeListener toggleChangeListener = new ToggleChangeListener(){
	        	public void propertyChange(PropertyChangeEvent evt) {
	            	fStat=evt.getNewValue().equals(Boolean.TRUE);
//	            	System.out.println("fStat was changed to " + evt.getNewValue());
	            	refreshTableViewer();
	            }
	        };
	        BooleanBean booleanBean = new BooleanBean();
	        BeanAdapter booleanBeanAdapter = new BeanAdapter(booleanBean, true);
	        booleanBeanAdapter.addBeanPropertyChangeListener(toggleChangeListener);
	        ValueModel booleanValueModel = booleanBeanAdapter.getValueModel("enabled");
	        bF = BasicComponentFactory.createCheckBox(booleanValueModel, "F-Statistic");

	        
	        bP=new JCheckBox("P-Value");
	        ToggleChangeListener bPToggleChangeListener = new ToggleChangeListener(){
	        	public void propertyChange(PropertyChangeEvent evt) {
	            	pVal=evt.getNewValue().equals(Boolean.TRUE);
//	            	System.out.println("pVal was changed to " + evt.getNewValue());
	            	refreshTableViewer();
	            }
	        };
	        BooleanBean bPBooleanBean = new BooleanBean();
	        BeanAdapter bPBooleanBeanAdapter = new BeanAdapter(bPBooleanBean, true);
	        bPBooleanBeanAdapter.addBeanPropertyChangeListener(bPToggleChangeListener);
	        ValueModel bPBooleanValueModel = bPBooleanBeanAdapter.getValueModel("enabled");
	        bP = BasicComponentFactory.createCheckBox(bPBooleanValueModel, "P-Value");
	        
	        bA=new JCheckBox("Adj-P-Value");
	        ToggleChangeListener bAToggleChangeListener = new ToggleChangeListener(){
	        	public void propertyChange(PropertyChangeEvent evt) {
	            	adjPVal=evt.getNewValue().equals(Boolean.TRUE);
//	            	System.out.println("adjPVal was changed to " + evt.getNewValue());
	            	refreshTableViewer();
	            }
	        };
	        BooleanBean bABooleanBean = new BooleanBean();
	        BeanAdapter bABooleanBeanAdapter = new BeanAdapter(bABooleanBean, true);
	        bABooleanBeanAdapter.addBeanPropertyChangeListener(bAToggleChangeListener);
	        ValueModel bABooleanValueModel = bABooleanBeanAdapter.getValueModel("enabled");
	        bA = BasicComponentFactory.createCheckBox(bABooleanValueModel, "Adj-P-Value");

	        bM=new JCheckBox("Mean");
	        ToggleChangeListener bMToggleChangeListener = new ToggleChangeListener(){
	        	public void propertyChange(PropertyChangeEvent evt) {
	            	mean=evt.getNewValue().equals(Boolean.TRUE);
//	            	System.out.println("mean was changed to " + evt.getNewValue());
	            	refreshTableViewer();
	            }
	        };
	        BooleanBean bMBooleanBean = new BooleanBean();
	        BeanAdapter bMBooleanBeanAdapter = new BeanAdapter(bMBooleanBean, true);
	        bMBooleanBeanAdapter.addBeanPropertyChangeListener(bMToggleChangeListener);
	        ValueModel bMBooleanValueModel = bMBooleanBeanAdapter.getValueModel("enabled");
	        bM = BasicComponentFactory.createCheckBox(bMBooleanValueModel, "Mean");

	        bS=new JCheckBox("Std");
	        ToggleChangeListener bSToggleChangeListener = new ToggleChangeListener(){
	        	public void propertyChange(PropertyChangeEvent evt) {
	            	std=evt.getNewValue().equals(Boolean.TRUE);
//	            	System.out.println("std was changed to " + evt.getNewValue());
	            	refreshTableViewer();
	            }
	        };
	        BooleanBean bSBooleanBean = new BooleanBean();
	        BeanAdapter bSBooleanBeanAdapter = new BeanAdapter(bSBooleanBean, true);
	        bSBooleanBeanAdapter.addBeanPropertyChangeListener(bSToggleChangeListener);
	        ValueModel bSBooleanValueModel = bSBooleanBeanAdapter.getValueModel("enabled");
	        bS = BasicComponentFactory.createCheckBox(bSBooleanValueModel, "Std");

	        builder.append(new JLabel());
	        builder.append(bF);
	        builder.append(bP);
	        //builder.append(bA);	//P-Value showed here is already adjuested.	
	        builder.nextLine();
	        builder.append(new JLabel());
	        builder.append(bM);
	        builder.append(bS);
	        if (popup==null){
		        popup=new JDialog();
		        popup.setTitle("Display Preference");
		        popup.add(builder.getPanel());
		        popup.pack();
		        popup.setVisible(true);
	        }else{
	        	System.out.println("This shouldn't happen, since if the popup already there, this Display Preference panel should be called again.");
	        }
		}
	}
    private class ToggleChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
        	fStat=evt.getNewValue().equals(Boolean.TRUE);
//        	System.out.println(fStat);
//            JOptionPane.showMessageDialog(null, "Property " + evt.getPropertyName() + " was changed to " + evt.getNewValue());
        }
    }

    public class BooleanBean extends Model {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public final static String ENABLED_PROPERTY = "enabled";
        private Boolean enabled = Boolean.TRUE;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            Boolean oldValue = this.enabled;
            this.enabled = enabled;
            firePropertyChange(ENABLED_PROPERTY, oldValue, this.enabled);
        }
    }
	private class TabularFileFilter extends FileFilter {
		public String getDescription() {
			return "CSV Files";
		}

		public boolean accept(File f) {
			String name = f.getName();
			boolean tabFile = name.endsWith("csv") || name.endsWith("CSV");
			if (f.isDirectory() || tabFile) {
				return true;
			}

			return false;
		}

		public String getExtension() {
			return "csv";
		}

	}

}
