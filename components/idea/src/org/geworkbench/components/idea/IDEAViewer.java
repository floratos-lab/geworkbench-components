package org.geworkbench.components.idea;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.io.FileReader;
import java.util.Scanner;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.IdeaResult;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * IDEAViewer of IDEA analysis component
 * 
 * @author zm2165
 * @version $Id$
 *
 */
@AcceptTypes( { IdeaResult.class })
public class IDEAViewer extends JPanel implements VisualPlugin {
	
	private static final long serialVersionUID = -4415752683103679560L;
	private JTabbedPane tabs;
	
    String dir="c:\\idea_test";

	public IDEAViewer() {		
		tabs = new JTabbedPane();
	}	
	
	@SuppressWarnings({ "unchecked" })
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet dataSet = event.getDataSet();
		if (dataSet instanceof IdeaResult) {		
			//label.setText("here is result");
			//this.add(label);
			tabs.removeAll();			
			buildTable("Genes of Significance", "\\output\\output2.txt");
			buildTable("Edges of Goc", "\\output\\output1_goc.txt");
			buildTable("Edges of Loc", "\\output\\output1_loc.txt");			
			add(tabs, BorderLayout.CENTER);			
			
			
			//this.repaint();
		}
	}
	
	
	
	private void buildTable(String tabName, String filename){
			    
	    Atable contentPane = new Atable();
		JPanel viewPanel = new JPanel();
		viewPanel.setLayout(new BoxLayout(viewPanel, BoxLayout.PAGE_AXIS));
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
				
		FormLayout layout = new FormLayout("500dlu:grow, pref",
		"20dlu, pref:grow");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);

		FormLayout headerLayout = new FormLayout(
				"60dlu, 6dlu, 60dlu, 30dlu, 90dlu, 6dlu, 90dlu, 200dlu, 90dlu",
				"20dlu");
		DefaultFormBuilder headerBuilder = new DefaultFormBuilder(headerLayout);		
		
		builder.append(headerBuilder.getPanel(), 2);
		builder.nextLine();		
		
		String [] columnNames=null;
		Object [][] tableData=null;
		int rCount=0;	
		String output2=dir+filename;
		try{
			FileReader reader = new FileReader(output2);
			Scanner in = new Scanner(reader);
			String firstRow;
			
			if(in.hasNextLine()){ 
				firstRow=in.nextLine();
				String [] tokens=firstRow.split("\\s");				
				columnNames=tokens;
			}
		
			
			while (in.hasNextLine()){
				in.nextLine();
				rCount++;
			}
			tableData = new String[rCount][columnNames.length];			
			reader = new FileReader(output2);	
			in = new Scanner(reader);
		
			int i=0;
			if(in.hasNextLine()) in.nextLine();
			while (in.hasNextLine()){
				String line = in.nextLine();
				String [] tokens=line.split("\\s+");
				tableData[i]=tokens;
				i++;
			}//end of while
		}
		catch(java.io.FileNotFoundException e){
			e.printStackTrace();			
		}		
		
		contentPane.setTable(tableData,columnNames);
        contentPane.setOpaque(true); //content panes must be opaque
        
        builder.add(contentPane, new CellConstraints("1,2,f,f"));
        
        JScrollPane wholeWindowScrollPane = new JScrollPane(builder.getPanel());
		this.setLayout(new BorderLayout());		
		
		tabs.add(tabName, wholeWindowScrollPane);	
		
		
	}
	
	
	public Component getComponent() {
		// TODO Auto-generated method stub
		return this;
	}
	
	/*
	 * Add to Set
	 */
	@Publish
	public org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> event) {
		return event;
	}
	private class Atable extends JPanel {
		private static final long serialVersionUID = -2227188169463388568L;
		public Atable() {
	        super(new GridLayout(1,0));	       
	    }	    
	    public void setTable(Object[][] tableData, String[] columnNames){			
			JTable table = new JTable(tableData, columnNames);
			//table.setPreferredScrollableViewportSize(new Dimension(100, 70));
			table.setFillsViewportHeight(true);		
			
			//Create the scroll pane and add the table to it.
			JScrollPane scrollPane = new JScrollPane(table);			
			//Add the scroll pane to this panel.
			add(scrollPane);
	    }	    
	}

	

}
