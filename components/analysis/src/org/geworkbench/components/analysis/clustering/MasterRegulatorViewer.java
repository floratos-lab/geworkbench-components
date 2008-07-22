package org.geworkbench.components.analysis.clustering;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMasterRagulatorResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSAnnotPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSAnnotatedPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SubpanelChangedEvent;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

@AcceptTypes({DSMasterRagulatorResultSet.class})
public class MasterRegulatorViewer extends JPanel implements VisualPlugin{

	private static final long serialVersionUID = 1L;
	private Log log = LogFactory.getLog(this.getClass());
//	MyTableModel myTableModel = new MyTableModel(new Object[1][1]); 
	TableViewer tv;
	TableViewer tv2;
	String[] columnNames = {"Transcription Factor", "P-Value", "Genes in regulon", "Genes in target list"};
	String[] detailColumnNames = {"Genes in target list", "P-Value", "T-Test Value"};
	DSMasterRagulatorResultSet<DSGeneMarker> MRAResultSet;
	DetailedTFGraphViewer detailedTFGraphViewer;
	boolean useSymbol = false;
	private ValueModel pValueHolder = new ValueHolder("0.05");
	private ValueModel tfAHolder = new ValueHolder(" ");	
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
		
		final JSplitPane jSplitPane1 = new JSplitPane();
		final JSplitPane jSplitPane2 = new JSplitPane();

		jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		jSplitPane1.setDividerSize(2);
		jSplitPane1.setDividerLocation(400);
		jSplitPane1.setTopComponent(jSplitPane2);
		jSplitPane1.setBottomComponent(detailedTFGraphViewer);
		jSplitPane2.setDividerLocation(600);
		jSplitPane2.setDividerSize(3);

		FormLayout layout = new FormLayout("500dlu:grow, pref","20dlu, pref:grow, 100dlu");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);

			FormLayout headerLayout = new FormLayout("60dlu, 6dlu, 60dlu, 30dlu, 90dlu, 6dlu, 90dlu, 200dlu, 90dlu","20dlu");
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
			//end of symbol and probe set
			
			//export part
			JButton exportAllButton = new JButton("Export all");
			exportAllButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					double threshold = Double.valueOf(pValueHolder.getValue().toString());
					int n = JOptionPane.showConfirmDialog(
						    null,
						    "Would you like to use threshold when exporting genes?",
						    "Threshold?",
						    JOptionPane.YES_NO_OPTION);
					//PS: we don't need to check n, because "threshold+n" will work as checking.
	                try {
	                	String exportFileStr="data/exportALL.csv";
	                    File exportFile = new File(exportFileStr);
	                    JFileChooser chooser = new JFileChooser(exportFile);
	                    chooser.showSaveDialog(MasterRegulatorViewer.this);
	                    //TODO: file overwrite checking and warning
	                    if (chooser.getSelectedFile()!=null){
	                    	exportFileStr = chooser.getSelectedFile().getPath();
		                    BufferedWriter writer = new BufferedWriter(new FileWriter(exportFileStr));
		                    //foreach tfA
		                    for (Iterator<DSGeneMarker> iterator = MRAResultSet.getTFs().iterator(); iterator
									.hasNext();) {
		                    	DSGeneMarker tfA = (DSGeneMarker) iterator.next();
		                    	
								String str = "";
								str += tfA.getLabel() + ", " + tfA.getShortName()+"\n";
			                    writer.write(str);
			                    writer.newLine();
								for (DSGeneMarker marker : MRAResultSet.getGenesInTargetList(tfA)){
									str = "";
									str += marker.getLabel() + ", " + marker.getShortName()+", ";
									str += new Float(MRAResultSet.getPValueOf(tfA, marker)).toString()+", ";
									str += new Float(MRAResultSet.getTTestValueOf(tfA, marker)).toString();
									if (MRAResultSet.getPValueOf(tfA, marker)< (threshold+n)){
					                    writer.write(str);
					                    writer.newLine();
									}
								}									
							}
							writer.close();
							JOptionPane.showMessageDialog(null,
									"File "+exportFileStr+" has been saved.",
									"File saved.",
									JOptionPane.INFORMATION_MESSAGE);								
	                    }else{
	                    	//user canceled
	                    }
	                } catch (IOException ioe) {
	                    log.error(ioe);
					}
		        }
			});

			headerBuilder.append(exportAllButton);
			
			//export selected button and function
			JButton exportSelectedButton = new JButton("Export selected");
			exportSelectedButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (detailedTFGraphViewer.tfA!=null){
		                try {
		                	String exportFileStr="data/exportTGs.csv";
		                    File exportFile = new File(exportFileStr);
		                    JFileChooser chooser = new JFileChooser(exportFile);
		                    chooser.showSaveDialog(MasterRegulatorViewer.this);
		                    //TODO: file overwrite checking and warning
		                    if (chooser.getSelectedFile()!=null){
		                    	exportFileStr = chooser.getSelectedFile().getPath();
			                    BufferedWriter writer = new BufferedWriter(new FileWriter(exportFileStr));
								String str = "";
								str += detailedTFGraphViewer.tfA.getLabel() + ", " + detailedTFGraphViewer.tfA.getShortName()+"\n";
			                    writer.write(str);
			                    writer.newLine();
								for (DSGeneMarker marker : MRAResultSet.getGenesInTargetList(detailedTFGraphViewer.tfA)){
									str = "";
									str += marker.getLabel() + ", " + marker.getShortName()+", ";
									str += new Float(MRAResultSet.getPValueOf(detailedTFGraphViewer.tfA, marker)).toString()+", ";
									str += new Float(MRAResultSet.getTTestValueOf(detailedTFGraphViewer.tfA, marker)).toString();
				                    writer.write(str);
				                    writer.newLine();
								}
								writer.close();
								JOptionPane.showMessageDialog(null,
										"File "+exportFileStr+" has been saved.",
										"File saved.",
										JOptionPane.INFORMATION_MESSAGE);								
		                    }else{
		                    	//user canceled
		                    }
		                } catch (IOException ioe) {
		                    log.error(ioe);
		                }
					}else{
						JOptionPane.showMessageDialog(null,
								"Please select a Transcription Factor first.",
								"Please try again.",
								JOptionPane.INFORMATION_MESSAGE);
					}
		        }
			});
			headerBuilder.append(exportSelectedButton);

			//add to set button and function
			JButton addToSetButton = new JButton("Add to set");
			addToSetButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (detailedTFGraphViewer.tfA==null) return;
					DSAnnotatedPanel<DSGeneMarker, Float> panelSignificant = new CSAnnotPanel<DSGeneMarker, Float>(
					"Target List of "+detailedTFGraphViewer.tfA.getLabel());
					
					for (DSGeneMarker marker : MRAResultSet.getGenesInTargetList(detailedTFGraphViewer.tfA)){
						if (MRAResultSet.getPValueOf(detailedTFGraphViewer.tfA, marker)<Double.valueOf(pValueHolder.getValue().toString()))
							panelSignificant.add(marker, new Float(MRAResultSet.getPValueOf(detailedTFGraphViewer.tfA, marker)));
					}
					publishSubpanelChangedEvent(new SubpanelChangedEvent(
							DSGeneMarker.class, panelSignificant,
							SubpanelChangedEvent.NEW));
		        }
			});
			headerBuilder.append(addToSetButton);
		
			//end of header
			//now the main panels
			
		builder.append(headerBuilder.getPanel(),2);
		builder.nextLine();
		
			//build the top-left panel
			FormLayout summaryTFFormLayout = new FormLayout("pref:grow","20dlu, pref:grow");
			DefaultFormBuilder summaryTFFormBuilder = new DefaultFormBuilder(summaryTFFormLayout);
			summaryTFFormBuilder.nextLine();
			summaryTFFormBuilder.add(tv,new CellConstraints("1,1,1,2,f,f"));
			
			jSplitPane2.setLeftComponent(new JScrollPane(summaryTFFormBuilder.getPanel(),JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
//		builder.add(new JScrollPane(summaryTFFormBuilder.getPanel(),JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
//		builder.nextColumn();
		
			//build the top-right panel
			FormLayout detailTFFormLayout = new FormLayout("40dlu, 6dlu, 120dlu, pref:grow, 60dlu, 6dlu, 60dlu","20dlu, pref:grow");
			DefaultFormBuilder detailTFFormBuilder = new DefaultFormBuilder(detailTFFormLayout);
			detailTFFormBuilder.append("TF Name:");
			JLabel tfALabelField = BasicComponentFactory.createLabel(tfAHolder);
			detailTFFormBuilder.append(tfALabelField);
			//detailTFFormBuilder.nextColumn();
			detailTFFormBuilder.append("P-val threshold:");
			//detailTFFormBuilder.nextColumn();
			
			final JTextField pValueTextField = BasicComponentFactory.createTextField(pValueHolder);
			
			ActionListener pValueActionListener = new ActionListener(){
	            public void actionPerformed(ActionEvent evt){
	            	try{
	            		double pValue = Double.valueOf(pValueTextField.getText());
	            	}catch(Exception e){
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								pValueTextField.setText("1.00");
								pValueHolder.setValue("1.00");
							}
						});
						JOptionPane.showMessageDialog(null,
								"P-Value should be a float point number",
								"Invalide P-Value",
								JOptionPane.INFORMATION_MESSAGE);
						return;
	            	}
	            	pValueHolder.setValue(pValueTextField.getText());
	            	updateSelectedTF(MRAResultSet, detailedTFGraphViewer.tfA, tv2);
//	            	tv2.updateUI();
	            	detailedTFGraphViewer.setPValueFilter(Double.valueOf(pValueTextField.getText()));
	            }
	        };
	        pValueTextField.addActionListener(pValueActionListener);
	        pValueTextField.addFocusListener(new FocusListener(){
				public void focusGained(FocusEvent arg0) {
				}
				public void focusLost(FocusEvent arg0) {
	            	try{
	            		double pValue = Double.valueOf(pValueTextField.getText());
	            	}catch(Exception e){
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								pValueTextField.setText("1.00");
								pValueHolder.setValue("1.00");
							}
						});
						JOptionPane.showMessageDialog(null,
								"P-Value should be a float point number",
								"Invalide P-Value",
								JOptionPane.INFORMATION_MESSAGE);
						return;
	            	}
	            	pValueHolder.setValue(pValueTextField.getText());
	            	updateSelectedTF(MRAResultSet, detailedTFGraphViewer.tfA, tv2);
//	            	tv2.updateUI();
	            	detailedTFGraphViewer.setPValueFilter(Double.valueOf(pValueTextField.getText()));	
				}
	        });
	        /*
	         * following code has some bug and makes it recursively accords. 
	         */
/*	        
	        pValueTextField.addCaretListener(new javax.swing.event.CaretListener() {
				public void caretUpdate(javax.swing.event.CaretEvent evt) {
					System.out.println("user try to change something..");
	            	Object temp=evt.getSource();
	            	try{
	            		System.out.println("I got: "+pValueTextField.getText());
	            		double pValue = Double.valueOf(pValueTextField.getText());
	            		System.out.println("I parsed: "+pValue);
	            	}catch(Exception e){
	            		System.out.println("and it's invalid.");
//	            		pValueHolder.setValue("1.00");
	            		//((JTextField)temp).setText("1.00");
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								//((JTextField)temp).setText("1.00");
								//pValueHolder.setValue("1.00");
								pValueTextField.setText("0.05");
								//System.out.println("changed to "+pValueTextField.getText());
							}
						});
	            	}
				}
	        });
*/
	        detailTFFormBuilder.add(pValueTextField);
			//detailTFFormBuilder.nextRow();
			detailTFFormBuilder.nextLine();
			detailTFFormBuilder.add(tv2,new CellConstraints("1,2,7,1,f,f"));

		jSplitPane2.setRightComponent(new JScrollPane(detailTFFormBuilder.getPanel(),JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		//builder.add(new JScrollPane(detailTFFormBuilder.getPanel(),JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		//builder.nextLine();
		detailedTFGraphViewer.setPreferredSize(new Dimension(600,100));
		detailedTFGraphViewer.setMinimumSize(new Dimension(50,50));
		detailedTFGraphViewer.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		//builder.append(new JScrollPane(detailedTFGraphViewer),2);
		builder.add(jSplitPane1,new CellConstraints("1,2,f,f"));

		this.setLayout(new BorderLayout());
		this.add(builder.getPanel(),BorderLayout.CENTER);
		
	}

	ChangeListener changeListener = new ChangeListener() {
	      public void stateChanged(ChangeEvent changEvent) {
	        AbstractButton aButton = (AbstractButton)changEvent.getSource();
	        ButtonModel aModel = aButton.getModel();
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
		for (Iterator<DSGeneMarker> iterator = MRAResultSet.getTFs().iterator(); iterator
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
/*
	class MyTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
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
*/	
	public Component getComponent() {
		// TODO Auto-generated method stub
		return this;
	}
	
	protected void updateSelectedTF(DSMasterRagulatorResultSet mraResultSet, DSGeneMarker tfA, TableViewer tv){
		boolean usePValue = true;
		double pValue = Double.valueOf(pValueHolder.getValue().toString());
		int records = 0;
		int filterCounter=0;
		if (tfA==null) return;
		if (usePValue){
			DSItemList<DSGeneMarker> genesInTargetList = mraResultSet.getGenesInTargetList(tfA);
			for (Iterator<DSGeneMarker> iterator = genesInTargetList.iterator(); iterator
					.hasNext();) {
				DSGeneMarker geneInTargetList = (DSGeneMarker) iterator.next();
				if (mraResultSet.getPValueOf(tfA, geneInTargetList)<pValue)
					filterCounter++;
			}
			records=filterCounter;
		}else
			records=mraResultSet.getGenesInTargetList(tfA).size();
		Object data[][] = new Object[records][3];
		int cx=0;
		DSItemList<DSGeneMarker> genesInTargetList = mraResultSet.getGenesInTargetList(tfA);
		for (Iterator<DSGeneMarker> iterator = genesInTargetList.iterator(); iterator
				.hasNext();) {
			DSGeneMarker geneInTargetList = (DSGeneMarker) iterator.next();
			if ((!usePValue) || (mraResultSet.getPValueOf(tfA, geneInTargetList)<pValue)){
				if (useSymbol)
					data[cx][0]= geneInTargetList.getShortName();
				else
					data[cx][0]= geneInTargetList.getLabel();
				data[cx][1]= mraResultSet.getPValueOf(tfA, geneInTargetList);
				data[cx][2]= mraResultSet.getTTestValueOf(tfA, geneInTargetList);
				cx++;
			}
		}
//		myTableModel.updateData(data);
		tv.setTableModel(data);
		tv.updateUI();
		if (useSymbol)
			tfAHolder.setValue(tfA.getShortName());
		else 
			tfAHolder.setValue(tfA.getLabel());
	}
	/*
	 * Add to Set
	 */
	@Publish
	@SuppressWarnings("unchecked")
	public org.geworkbench.events.SubpanelChangedEvent publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent event) {
		return event;
	}

}
