package org.geworkbench.components.masterregulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMasterRagulatorResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSAnnotPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSAnnotatedPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.components.masterregulator.TableViewer.DefaultViewerTableModel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.OWFileChooser;
import org.geworkbench.util.SaveImage;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author y.c. mark, zji
 * @version $Id$
 *
 */
@AcceptTypes( { DSMasterRagulatorResultSet.class })
public class MasterRegulatorViewer extends JPanel implements VisualPlugin {
	private static final long serialVersionUID = -8166510475998731260L;
	
	private Log log = LogFactory.getLog(MasterRegulatorViewer.class);

	TableViewer tv;
	TableViewer tv2;
	String[] columnNames = { "Master Regulator", "FET P-Value", "Genes in regulon",
			"Genes in intersection set", "Mode" };
	// for label with -log10 etc, need to do something so Excel does not interpret it as a formula...
	// Here use leading space.
	String[] detailColumnNames = { "Genes in intersection set",
			" -log10(P-value) * sign of t-value" };
	DSMasterRagulatorResultSet<DSGeneMarker> MRAResultSet;
	boolean useSymbol = true;

	private ValueModel tfAHolder = new ValueHolder(" ");
	JRadioButton currentSelectedRadioButton = null;
	DSGeneMarker currentSelectedtfA = null;
	private static final String EXPORTDIR = "exportDir";
	private static final Color bgcolor = new Color(214,217,223);
	private static final String defaultnumtop = "10";
	private static final String defaultbarheight = "30";
	private JTextField numtop = new JTextField(defaultnumtop); 
	private JTextField barheight = new JTextField(defaultbarheight);
	private JScrollPane gspane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	private JScrollPane gspane2 = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	private static final int col0w = 120, col2w = 40;
	private static final String[] graphheader = new String[]{"Master Regulator","Bar Graph","Mode"};
	private GraphTableModel bgm = new GraphTableModel(new Object[0][3], graphheader);
	private JTable graphtable = new JTable(bgm);
	private GraphTableModel gdm = new GraphTableModel(new Object[0][3], graphheader);
	private JTable gradienttable = new JTable(gdm);
	private JRadioButton modeAll = new JRadioButton("All");
	private JRadioButton activator = new JRadioButton("Activator(+)");
	private JRadioButton repressor = new JRadioButton("Repressor(-)");
	private JRadioButton regulonBar = new JRadioButton("Regulons");
	private JRadioButton intersectionBar = new JRadioButton("Intersection Sets");
	private final JSplitPane jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private boolean shown = false;

	public MasterRegulatorViewer() {

		Object[][] data = new Object[1][1];
		data[0][0] = "Start";
		tv = new TableViewer(columnNames, data);
		tv2 = new TableViewer(detailColumnNames, data);
		tv.setPreferredSize(new Dimension(0, 156));
		tv2.setPreferredSize(new Dimension(0, 156));
		tv.setNumerical(1, true);
		tv.setNumerical(2, true);
		tv.setNumerical(3, true);
		tv2.setNumerical(1, true);
		tv2.setNumerical(2, true); 

		final JSplitPane jSplitPane2 = new JSplitPane();
		jSplitPane.setTopComponent(jSplitPane2);

		initGraphTables();
		JPanel graphpanel = new JPanel();
		graphpanel.setLayout(new BorderLayout());
		graphpanel.add(gspane, BorderLayout.CENTER);
		graphpanel.add(gspane2, BorderLayout.SOUTH);
		jSplitPane.setBottomComponent(graphpanel);
		jSplitPane2.setDividerLocation(650);
		jSplitPane2.setDividerSize(3);

		FormLayout layout = new FormLayout("500dlu:grow, pref",
				"20dlu, pref:grow");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);

		FormLayout headerLayout = new FormLayout(
				"40dlu,2dlu,45dlu,10dlu,70dlu,0dlu,20dlu,10dlu,35dlu,0dlu,20dlu,10dlu,52dlu,0dlu,44dlu,2dlu,65dlu,10dlu,70dlu,10dlu,72dlu",
				"20dlu");
		DefaultFormBuilder headerBuilder = new DefaultFormBuilder(headerLayout);
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				AbstractButton aButton = (AbstractButton) evt.getSource();
				ButtonModel aModel = aButton.getModel();
				boolean selected = aModel.isSelected();
				JRadioButton jRadioButton = (JRadioButton) aButton;
				String GeneOrProbeStr = jRadioButton.getText();
				log.debug(GeneOrProbeStr + " selected : " + selected);
				if (GeneOrProbeStr.equals("Symbol") && selected)
					showSymbol();
				if (GeneOrProbeStr.equals("Probe Set") && selected)
					showProbeSet();
			}
		};
		ButtonGroup SymbolProbeSetGroup = new ButtonGroup();
		JRadioButton showSymbolButton = new JRadioButton("Symbol");
		showSymbolButton.setSelected(true); // default to Symbol
		showSymbolButton.addActionListener(actionListener);
		JRadioButton showProbeSetButton = new JRadioButton("Probe Set");
		showProbeSetButton.addActionListener(actionListener);
		SymbolProbeSetGroup.add(showSymbolButton);
		SymbolProbeSetGroup.add(showProbeSetButton);
		headerBuilder.append(showSymbolButton);
		headerBuilder.append(showProbeSetButton);
		// end of symbol and probe set

		ActionListener barListener = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				updateGraph();
			}
		};

		headerBuilder.append("Display results for top ");
		headerBuilder.append(numtop);
		numtop.addActionListener(barListener);
		
		headerBuilder.append("Bar height ");
		headerBuilder.append(barheight);
		barheight.addActionListener(barListener);
		
		headerBuilder.append("Display bars for ");
		headerBuilder.append(regulonBar);
		headerBuilder.append(intersectionBar);
		ButtonGroup barGroup = new ButtonGroup();
		barGroup.add(regulonBar);
		barGroup.add(intersectionBar);
		regulonBar.setSelected(true);
		regulonBar.addActionListener(barListener);
		intersectionBar.addActionListener(barListener);

		// export part
		JButton exportAllButton = new JButton("Export all targets");
		exportAllButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {
					String exportFileStr = "exportALL.csv";
					PropertiesManager properties = PropertiesManager.getInstance();
					String exportDir = properties.getProperty(this.getClass(), EXPORTDIR, exportFileStr);
					File exportFile = new File(exportDir);
					OWFileChooser chooser = new OWFileChooser(exportFile);
					ExportFileFilter filter = new ExportFileFilter(".csv", "Comma Separated Value Files (.csv)", ",");
					chooser.setFileFilter(filter);
					chooser.setDialogTitle("Export All MRA Target Results");
					String extension = filter.getExtension();
					int c = chooser.showSaveDialog(MasterRegulatorViewer.this);
					if (c == OWFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
						exportFileStr = chooser.getSelectedFile().getPath();
						properties.setProperty(this.getClass(), EXPORTDIR, chooser.getSelectedFile().getParent());
						if (!exportFileStr.endsWith(extension))
							exportFileStr += extension;
						BufferedWriter writer = new BufferedWriter(
								new FileWriter(exportFileStr));
						// foreach tfA
						for (Iterator<DSGeneMarker> iterator = MRAResultSet
								.getTFs().iterator(); iterator.hasNext();) {
							DSGeneMarker tfA = (DSGeneMarker) iterator.next();

							String str = "";
							str += tfA.getLabel() + ", " + tfA.getShortName()
									+ "\n";
							writer.write(str);
							for (DSGeneMarker marker : MRAResultSet
									.getGenesInTargetList(tfA)) {
								str = "";
								str += marker.getLabel() + ", "
									+ marker.getShortName() + ", ";
								str += new Float(MRAResultSet.getValue(
									marker)).toString();
								writer.write(str);
								writer.newLine();
							}
							writer.newLine();
						}
						writer.close();
						JOptionPane.showMessageDialog(null, "File "
								+ exportFileStr + " has been saved.",
								"File saved.", JOptionPane.INFORMATION_MESSAGE);
					} else {
						// user canceled
					}
				} catch (IOException ioe) {
					log.error(ioe);
				}
			}
		});

		headerBuilder.append(exportAllButton);
	 
		// add to set button and function
		JButton addToSetButton = new JButton("Add targets to set");
		addToSetButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentSelectedtfA == null)
					return;
				DSAnnotatedPanel<DSGeneMarker, Float> panelSignificant = new CSAnnotPanel<DSGeneMarker, Float>(
						"Target List of "
								+ currentSelectedtfA.getLabel());

				for (DSGeneMarker marker : MRAResultSet
						.getGenesInTargetList(currentSelectedtfA)) {
					panelSignificant.add(marker, new Float(0));
				}
				publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(
						DSGeneMarker.class, panelSignificant,
						SubpanelChangedEvent.NEW));
			}
		});
		headerBuilder.append(addToSetButton);

		builder.append(headerBuilder.getPanel(), 2);
		builder.nextLine();

		// build the top-left panel
		FormLayout summaryTFFormLayout = new FormLayout("53dlu,70dlu,25dlu,6dlu,35dlu,6dlu,60dlu,6dlu,60dlu,pref:grow",
				"20dlu, pref:grow");
		DefaultFormBuilder summaryTFFormBuilder = new DefaultFormBuilder(
				summaryTFFormLayout);

		modeAll.setSelected(true);
		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(modeAll);
		modeGroup.add(activator);
		modeGroup.add(repressor);
		ActionListener modeListener = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				updateTable();
				updateSelectedTF(MRAResultSet, currentSelectedtfA, tv2);
			}
		};
		modeAll.addActionListener(modeListener);
		activator.addActionListener(modeListener);
		repressor.addActionListener(modeListener);
		
		summaryTFFormBuilder.append(tv.exportButton);
		summaryTFFormBuilder.append("Mode:");
		summaryTFFormBuilder.append(modeAll);
		summaryTFFormBuilder.append(activator);
		summaryTFFormBuilder.append(repressor);

		summaryTFFormBuilder.nextLine();
		summaryTFFormBuilder.add(tv, new CellConstraints("1,2,10,1,f,f"));

		jSplitPane2.setLeftComponent(new JScrollPane(summaryTFFormBuilder
				.getPanel(), JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		// build the top-right panel
		FormLayout detailTFFormLayout = new FormLayout(
				//"80dlu, 6dlu, 120dlu, pref:grow, 60dlu, 6dlu, 60dlu",
				"60dlu, 6dlu, 80dlu, 0dlu, 53dlu, pref:grow",
				"20dlu, pref:grow");
		DefaultFormBuilder detailTFFormBuilder = new DefaultFormBuilder(
				detailTFFormLayout);
		detailTFFormBuilder.append("Master Regulator:");
		JLabel tfALabelField = BasicComponentFactory.createLabel(tfAHolder);
		detailTFFormBuilder.append(tfALabelField);
		detailTFFormBuilder.append(tv2.exportButton);

		detailTFFormBuilder.nextLine();
		detailTFFormBuilder.add(tv2, new CellConstraints("1,2,6,1,f,f"));

		jSplitPane2.setRightComponent(new JScrollPane(detailTFFormBuilder
				.getPanel(), JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		builder.add(jSplitPane, new CellConstraints("1,2,f,f"));

		JScrollPane wholeWindowScrollPane = new JScrollPane(builder.getPanel());
		this.setLayout(new BorderLayout());
		this.add(wholeWindowScrollPane, BorderLayout.CENTER);	
		
		graphtable.addMouseListener(new MouseAdapter() {
	        @Override
	  
	        public void mouseReleased(MouseEvent e) {	        	
	            if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
	                JPopupMenu popup = createSavePopUp();
	                popup.show(e.getComponent(), e.getX(), e.getY());
	            }
	        }

		});
		
	}

	private JPopupMenu createSavePopUp(){
		return new PopUpOptions();
	}
	class PopUpOptions extends JPopupMenu {
	    
		private static final long serialVersionUID = 2993281547057589679L;
		JMenuItem itemSnapshot;
		JMenuItem itemToDisk;
	    public PopUpOptions(){
	        itemSnapshot = new JMenuItem("Save image to Project (snapshot)");
	        itemToDisk = new JMenuItem("Save image to Disk");
	        add(itemSnapshot);
	        add(itemToDisk);
	        
	        itemSnapshot.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(ActionEvent paramActionEvent) {					
					createImageSnapshot(graphtable, graphtable.getTableHeader());
				}});
	        
	        itemToDisk.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(ActionEvent paramActionEvent) {
					saveToImage(graphtable, graphtable.getTableHeader());
					
				}});
	    }
	}

	ChangeListener changeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent changEvent) {
			AbstractButton aButton = (AbstractButton) changEvent.getSource();
			ButtonModel aModel = aButton.getModel();
			boolean selected = aModel.isSelected();
			JRadioButton jRadioButton = (JRadioButton) aButton;
			if (selected)
				currentSelectedRadioButton = jRadioButton; // save current
															// selected one
			else return;
			String GeneMarkerStr = jRadioButton.getName();
			log.debug(GeneMarkerStr + " selected : " + selected);
			// fire a TF selected event //but event won't deliver to itself.
			tv.updateUI();
			DSGeneMarker tfA = null;
			tfA = (DSGeneMarker) MRAResultSet.getTFs().get(GeneMarkerStr);
			currentSelectedtfA = tfA;
			updateSelectedTF(MRAResultSet, tfA, tv2);
		}
	};

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet dataSet = event.getDataSet();
		if (dataSet instanceof DSMasterRagulatorResultSet) {
			MRAResultSet = (DSMasterRagulatorResultSet<DSGeneMarker>) dataSet;
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					updateTable();
					updateSelectedTF(MRAResultSet, currentSelectedtfA, tv2);
					useSymbol = true;
				}
			});
		}
	}
	
	private void clearSelection(){
		currentSelectedRadioButton = null;
		currentSelectedtfA = null;
		tfAHolder.setValue(null);
	}

	private void updateTable() {
		if (!shown && jSplitPane.getHeight()>0){
			jSplitPane.setDividerLocation(0.78);
			shown = true;
		}
		DSItemList<DSGeneMarker> markers = MRAResultSet.getTFs();
		char selectedtfAmode = MRAResultSet.getMode(currentSelectedtfA);
		if (activator.isSelected()){
			markers = MRAResultSet.getActivators();
			if (selectedtfAmode == DSMasterRagulatorResultSet.REPRESSOR)
				clearSelection();
		}else if (repressor.isSelected()){
			markers = MRAResultSet.getRepressors();
			if (selectedtfAmode == DSMasterRagulatorResultSet.ACTIVATOR)
				clearSelection();
		}
		Object data[][] = new Object[markers.size()][5];
		int cx = 0;
		ButtonGroup group1 = new ButtonGroup();
		for (DSGeneMarker tfA : markers) {
			JRadioButton tfRadioButton;
			if ((currentSelectedRadioButton != null)
					&& (currentSelectedRadioButton.getName().equals(tfA
							.getLabel())))
				tfRadioButton = currentSelectedRadioButton;
			else
				tfRadioButton = new JRadioButton();
			tfRadioButton.setName(tfA.getLabel());
			if (useSymbol)
				tfRadioButton.setText(tfA.getShortName());
			else
				tfRadioButton.setText(tfA.getLabel());
			tfRadioButton.addChangeListener(changeListener);
			tfRadioButton.setEnabled(true);
			group1.add(tfRadioButton);
			data[cx][0] = tfRadioButton;
			// data[cx][1]= tfA.getShortName();
			data[cx][1] = MRAResultSet.getPValue(tfA);
			data[cx][2] = MRAResultSet.getGenesInRegulon(tfA).size();
			data[cx][3] = MRAResultSet.getGenesInTargetList(tfA).size();
			data[cx][4] = MRAResultSet.getMode(tfA);
			cx++;
		}
		// myTableModel.updateData(data);
		tv.setTableModel(data);
		((DefaultViewerTableModel) tv.model).sort(1); //sort by p-values
		tv.table.getColumnModel().getColumn(4).setMaxWidth(40); // mode width

		tv.updateUI();
		tv.setMRViewer(this);
		updateGraph();
		tv2.setTableModel(new String[0][0]);
 
		tv2.updateUI();
	}

	void updateGraph(){
		int n = 0, h = 0;
		try{
			n = Integer.valueOf(numtop.getText());
			h = Integer.valueOf(barheight.getText());
			int totaltfs = tv.table.getRowCount();
			if (totaltfs == 0) n = 0;
			else if (n < 0 || n > totaltfs){
				numtop.setText(Integer.toString(totaltfs));
				JOptionPane.showMessageDialog(null, "Display top results number: 0 < n < "+totaltfs, 
						"Display results for top", JOptionPane.WARNING_MESSAGE);
				n = totaltfs;
			}
			if (h <= 0){
				barheight.setText(defaultbarheight);
				JOptionPane.showMessageDialog(null, "Bar height should be a positive integer", 
						"Bar Height", JOptionPane.WARNING_MESSAGE);
				h = Integer.valueOf(defaultbarheight);
			}
		}catch(NumberFormatException nfe){
			JOptionPane.showMessageDialog(null, "Please enter an integer.", 
					"NumberFormatException", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Object[][] graphdata = new Object[n][3];
		for (int i = 0; i < n; i++){
			JRadioButton button = (JRadioButton)tv.getTable().getValueAt(i, 0);
			graphdata[i][0] = button.getText();
			DetailedTFGraphViewer gv = new DetailedTFGraphViewer();
			gv.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			gv.setTFA(MRAResultSet, MRAResultSet.getTFs().get(button.getName()), regulonBar.isSelected());
			gv.updateUI();
			graphdata[i][1] = gv;
			graphdata[i][2] = tv.getTable().getValueAt(i, 4);
		}

		bgm.setDataVector(graphdata, graphheader);
		graphtable.setRowHeight(h);
		int length = gspane.getViewport().getWidth() - col0w - col2w - gspane.getVerticalScrollBar().getWidth();
		if (length <= 0) length = gspane.getViewport().getWidth();
		graphtable.getColumnModel().getColumn(1).setPreferredWidth(length); 

		int markercnt = 0;
		if (MRAResultSet != null && n > 0) markercnt = MRAResultSet.getMarkerCount();
		DetailedTFGraphViewer.GradientPanel gdpane = new DetailedTFGraphViewer().new GradientPanel();
		gdpane.setParams(markercnt, MRAResultSet.getMinValue(), MRAResultSet.getMaxValue());
		gdpane.updateUI();
		
		Object[][] gradientdata = new Object[1][3];
		gradientdata[0][0] = "";
		gradientdata[0][1] = gdpane;
		gradientdata[0][2] = "";

		gdm.setDataVector(gradientdata, graphheader);
		gradienttable.getColumnModel().getColumn(1).setPreferredWidth(length); 
	}
	
	private void initGraphTables(){
		//init bar graph table
		graphtable.setRowSelectionAllowed(false);
		Enumeration<TableColumn> columns = graphtable.getColumnModel().getColumns();
		while(columns.hasMoreElements()){
			TableColumn tc = columns.nextElement();
			switch (tc.getModelIndex()){
				case 0: tc.setPreferredWidth(col0w); break;
				case 1: tc.setCellRenderer(new DefaultTableCellRenderer() {
							private static final long serialVersionUID = 5010765085642920180L;
							public Component getTableCellRendererComponent(JTable jTable,
									Object obj, boolean param, boolean param3, int row, int col) {
								if (obj instanceof DetailedTFGraphViewer){
									DetailedTFGraphViewer gv = (DetailedTFGraphViewer)obj;
									return gv;
								}
								return this;
							}
						}); 
						break;                                                                                                                        
				case 2: tc.setPreferredWidth(col2w); break;
				default: log.error("invalid column in graph table"); break;
			}
		}

		graphtable.setAutoCreateColumnsFromModel(false);
		graphtable.setPreferredScrollableViewportSize(new Dimension(0, 150));
		gspane.setViewportView(graphtable);

		//init gradient table
		gradienttable.setTableHeader(null);
		gradienttable.setRowHeight(Integer.valueOf(defaultbarheight)/2);
		columns = gradienttable.getColumnModel().getColumns();
		while(columns.hasMoreElements()){
			TableColumn tc = columns.nextElement();
			switch (tc.getModelIndex()){
				case 0: tc.setPreferredWidth(col0w); 
						tc.setCellRenderer(new BlankCellRenderer());
						break;
				case 1: tc.setCellRenderer(new DefaultTableCellRenderer() {
							private static final long serialVersionUID = -2363543603658908463L;
							public Component getTableCellRendererComponent(JTable jTable,
									Object obj, boolean param, boolean param3, int row, int col) {
								if (obj instanceof DetailedTFGraphViewer.GradientPanel){
									DetailedTFGraphViewer.GradientPanel gd = (DetailedTFGraphViewer.GradientPanel)obj;
									return gd;
								}
								return this;
							}
						});
						break;                                                                                                                        
				case 2: tc.setPreferredWidth(col2w);
						tc.setCellRenderer(new BlankCellRenderer());
						break;
				default: log.error("invalid column in gradient table"); break;
			}
		}

		gspane2.setViewportView(gradienttable);
		Insets insets = gspane.getBorder().getBorderInsets(null);
		gspane2.setBorder(BorderFactory.createEmptyBorder(0,insets.left,0,insets.right));
		gspane2.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI());
		gspane2.setPreferredSize(new Dimension(0, Integer.valueOf(defaultbarheight)/2));
		gradienttable.setAutoCreateColumnsFromModel(false);
		//gspane2.getVerticalScrollBar().setEnabled(false);
		//gspane2.getVerticalScrollBar().removeAll();
	}

	private class BlankCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 7497061980958072825L;
		public Component getTableCellRendererComponent(JTable table, Object obj, boolean isSelected, boolean hasFocus, int row, int col) {
			Component c = super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, col);
			c.setBackground(bgcolor);
			return c;
		}
	}
	
	private void showSymbol() {
		DSGeneMarker currentTFA = currentSelectedtfA;
		useSymbol = true;
		updateTable();

		updateSelectedTF(MRAResultSet, currentTFA, tv2);
	}

	private void showProbeSet() {
		DSGeneMarker currentTFA = currentSelectedtfA;
		useSymbol = false;
		updateTable();

		updateSelectedTF(MRAResultSet, currentTFA, tv2);
	}


	public Component getComponent() {
		return this;
	}

	private void updateSelectedTF(
			DSMasterRagulatorResultSet<DSGeneMarker> mraResultSet,
			DSGeneMarker tfA, TableViewer tv) {
		boolean usePValue = true;

		int records = 0;
		if (tfA == null)
			return;
		if (usePValue) {
			DSItemList<DSGeneMarker> genesInTargetList = mraResultSet
					.getGenesInTargetList(tfA);
			if (genesInTargetList == null){
				clearSelection();
				return;
			}
			records = genesInTargetList.size();
		} else
			records = mraResultSet.getGenesInTargetList(tfA).size();
		Object data[][] = new Object[records][2];
		int cx = 0;
		DSItemList<DSGeneMarker> genesInTargetList = mraResultSet
				.getGenesInTargetList(tfA);
		for (Iterator<DSGeneMarker> iterator = genesInTargetList.iterator(); iterator
				.hasNext();) {
			DSGeneMarker geneInTargetList = (DSGeneMarker) iterator.next();

			if (useSymbol)
				data[cx][0] = geneInTargetList.getShortName();
			else
				data[cx][0] = geneInTargetList.getLabel();

			data[cx][1] = mraResultSet.getValue(geneInTargetList);
			cx++;
		}
		// myTableModel.updateData(data);
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
	public org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> event) {
		return event;
	}

	private class GraphTableModel extends DefaultTableModel{
		GraphTableModel(Object[][] data, Object[] columnNames) {
			super(data, columnNames);
		}
		
		public boolean isCellEditable(int row, int col){
			return false;
		}
	}
	
	@Publish public ImageSnapshotEvent createImageSnapshot(JTable table, JTableHeader header) {        
        
		int w = Math.max(table.getWidth(), header.getWidth());  
	    int h = table.getHeight() + header.getHeight();  
	    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);  
	    Graphics2D g2 = bi.createGraphics();  
	    header.paint(g2);  
	    g2.translate(0, header.getHeight());  
	    table.paint(g2);  
	    g2.dispose();
        ImageIcon icon = new ImageIcon(bi, "Bar Graph");
        org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent("Bar Graph Snapshot", icon, org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
        return event;
    }
	
	private static void saveToImage(JTable table, JTableHeader header)  
    {  
        int w = Math.max(table.getWidth(), header.getWidth());  
        int h = table.getHeight() + header.getHeight();  
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);  
        Graphics2D g2 = bi.createGraphics();  
        header.paint(g2);  
        g2.translate(0, header.getHeight());  
        table.paint(g2);  
        g2.dispose();
        Image currentImage=bi;
        SaveImage si = new SaveImage(currentImage);
        si.save();      
        
    }  
	 
}
