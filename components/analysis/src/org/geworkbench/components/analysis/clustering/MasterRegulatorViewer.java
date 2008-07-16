package org.geworkbench.components.analysis.clustering;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMasterRagulatorResultSet;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

@AcceptTypes({DSMasterRagulatorResultSet.class})
public class MasterRegulatorViewer extends JPanel implements VisualPlugin{

	private Log log = LogFactory.getLog(this.getClass());
	MyTableModel myTableModel = new MyTableModel(new Object[1][1]); 
	TableViewer tv;
	TableViewer tv2;
	String[] columnNames = {"Transcription Factor", "P-Value", "Genes in regulon", "Genes in target list"};
	String[] detailColumnNames = {"Genes in target list", "P-Value", "T-Test Value"};
	DSMasterRagulatorResultSet MRAResultSet;
	DetailedTFGraphViewer detailedTFGraphViewer;
	boolean useSymbol = false;
	public MasterRegulatorViewer(){
		
		Object[][] data=new Object[1][1];
		data[0][0] = "Start";
		tv = new TableViewer(columnNames, data);
		tv2 = new TableViewer(detailColumnNames, data);
		detailedTFGraphViewer = new DetailedTFGraphViewer();
		tv.setNumerical(1,true);
		tv.setNumerical(2,true);
		tv.setNumerical(3,true);
		tv2.setNumerical(1,true);
		tv2.setNumerical(2,true);
		//tv.setTableModel(myTableModel);
		FormLayout layout = new FormLayout("500dlu, 340dlu","10dlu, 300dlu, 100dlu");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);

			FormLayout headerLayout = new FormLayout("60dlu, 6dlu, 60dlu, 6dlu, 60dlu, 6dlu, 60dlu","10dlu");
			DefaultFormBuilder headerBuilder = new DefaultFormBuilder(headerLayout);
			ActionListener actionListener = new ActionListener(){
	            public void actionPerformed(ActionEvent evt){
			        AbstractButton aButton = (AbstractButton)evt.getSource();
			        ButtonModel aModel = aButton.getModel();
			        boolean selected = aModel.isSelected();
			        JRadioButton jRadioButton = (JRadioButton)aButton;
			        String GeneOrProbeStr = jRadioButton.getText();
			        log.debug(GeneOrProbeStr+" selected : "+selected);
			        if (GeneOrProbeStr.equals("Symbol")&&selected)
			        	showSymbol();
			        if (GeneOrProbeStr.equals("Probe Set")&&selected)
			        	showProbeSet();			        
	            }
	        };
	        ButtonGroup SymbolProbeSetGroup = new ButtonGroup();
 			JRadioButton showSymbolButton= new JRadioButton("Symbol");
			showSymbolButton.addActionListener(actionListener);
			JRadioButton showProbeSetButton= new JRadioButton("Probe Set");
			showProbeSetButton.addActionListener(actionListener);
			SymbolProbeSetGroup.add(showSymbolButton);
			SymbolProbeSetGroup.add(showProbeSetButton);
			headerBuilder.append(showSymbolButton);
			headerBuilder.append(showProbeSetButton);
		
		builder.append(headerBuilder.getPanel(),2);
		builder.nextLine();
		
		builder.add(new JScrollPane(tv,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		builder.nextColumn();
		builder.add(new JScrollPane(tv2,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		builder.nextLine();
		detailedTFGraphViewer.setPreferredSize(new Dimension(600,100));
		detailedTFGraphViewer.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		builder.append(new JScrollPane(detailedTFGraphViewer),2);
		this.add(builder.getPanel());
		
		
	}

	ChangeListener changeListener = new ChangeListener() {
	      public void stateChanged(ChangeEvent changEvent) {
	        AbstractButton aButton = (AbstractButton)changEvent.getSource();
	        ButtonModel aModel = aButton.getModel();
	        boolean armed = aModel.isArmed();
	        boolean pressed = aModel.isPressed();
	        boolean selected = aModel.isSelected();
	        JRadioButton jRadioButton = (JRadioButton)aButton;
	        String GeneMarkerStr = jRadioButton.getName();
	        log.debug(GeneMarkerStr+" selected : "+selected);
	        //fire a TF selected event //but event won't deliver to itself.
	        tv.updateUI();
	        DSGeneMarker tfA=null;
	        tfA = (DSGeneMarker)MRAResultSet.getTFs().get(GeneMarkerStr);
	        updateSelectedTF(MRAResultSet, tfA, tv2);
	        detailedTFGraphViewer.setTFA(MRAResultSet, tfA);
	        detailedTFGraphViewer.updateUI();
	      }
	    };

	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet dataSet = event.getDataSet();
		if (dataSet instanceof DSMasterRagulatorResultSet) {
			MRAResultSet = (DSMasterRagulatorResultSet)dataSet;
			updateTable();
			useSymbol = true;
		}
	}

	private void updateTable(){
		Object data[][] = new Object[MRAResultSet.getTFs().size()][5];
		int cx=0;
		ButtonGroup group1 = new ButtonGroup();
		for (Iterator iterator = MRAResultSet.getTFs().iterator(); iterator
				.hasNext();) {
			DSGeneMarker tfA = (DSGeneMarker) iterator.next();
			JRadioButton tfRadioButton = new JRadioButton();
			tfRadioButton.setName(tfA.getLabel());
			if (useSymbol)
				tfRadioButton.setText(tfA.getShortName());
			else
				tfRadioButton.setText(tfA.getLabel());
			tfRadioButton.addChangeListener(changeListener);
			tfRadioButton.setEnabled(true);
			group1.add(tfRadioButton);
			data[cx][0]= tfRadioButton;
//			data[cx][1]= tfA.getShortName();
			data[cx][1]= MRAResultSet.getPValue(tfA);
			data[cx][2]= MRAResultSet.getGenesInRegulon(tfA).size();
			data[cx][3]= MRAResultSet.getGenesInTargetList(tfA).size();
			cx++;
		}
//		myTableModel.updateData(data);
		tv.setTableModel(data);
		tv.updateUI();
		tv2.setTableModel(new String[0][0]);
		tv2.updateUI();
		detailedTFGraphViewer.setTFA(null, null);
		detailedTFGraphViewer.updateUI();
	}
	private void showSymbol(){
		useSymbol = true;
		updateTable();
	}

	private void showProbeSet(){
		useSymbol = false;
		updateTable();
	}

	class MyTableModel extends AbstractTableModel {
	    private String[] columnNames = {"Transcription Factor", "P-Value", "Genes in regulon", "Genes in target list"};
	    private Object[][] data;
	    
	    public MyTableModel(Object[][] data){
	    	this.data = data;
	    }
	    
	    public int getColumnCount() {
	        return columnNames.length;
	    }

	    public int getRowCount() {
	        return data.length;
	    }

	    public String getColumnName(int col) {
	        return columnNames[col];
	    }

	    public Object getValueAt(int row, int col) {
	        return data[row][col];
	    }

	    public Class getColumnClass(int c) {
	        return getValueAt(0, c).getClass();
	    }

	    public void updateData(Object[][] data) {
	        this.data = data;
	        fireTableDataChanged();
	    } 
	}
	public Component getComponent() {
		// TODO Auto-generated method stub
		return this;
	}
	
	protected void updateSelectedTF(DSMasterRagulatorResultSet mraResultSet, DSGeneMarker tfA, TableViewer tv){
		Object data[][] = new Object[mraResultSet.getGenesInTargetList(tfA).size()][3];
		int cx=0;
		DSItemList<DSGeneMarker> genesInTargetList = mraResultSet.getGenesInTargetList(tfA);
		for (Iterator iterator = genesInTargetList.iterator(); iterator
				.hasNext();) {
			DSGeneMarker geneInTargetList = (DSGeneMarker) iterator.next();
			if (useSymbol)
				data[cx][0]= geneInTargetList.getShortName();
			else
				data[cx][0]= geneInTargetList.getLabel();
			data[cx][1]= mraResultSet.getPValueOf(tfA, geneInTargetList);
			data[cx][2]= mraResultSet.getTTestValueOf(tfA, geneInTargetList);
			cx++;
		}
//		myTableModel.updateData(data);
		tv.setTableModel(data);
		tv.updateUI();
	}
}
