package org.geworkbench.components.gpmodule.consensusclustering;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.model.clusters.DSConsensusClusterResultSet;
import org.geworkbench.bison.model.clusters.CSConsensusClusterResultSet.CCData;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.preferences.GlobalPreferences;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.Util;

@AcceptTypes( { DSConsensusClusterResultSet.class })
public class ConsensusClusteringViewer implements VisualPlugin {
	private static final long serialVersionUID = -4680852009126758639L;
	private static final Log log = LogFactory.getLog(ConsensusClusteringViewer.class);
	private JPanel mainPanel;
	private JTable table;
	private ListTableModel tableModel;
	private static final String[] tableHeader = {"Select", "File", "Type"};
	private static final String rootDir = FilePathnameUtils.getTemporaryFilesDirectoryPath();
	private static final String lastDirConf = FilePathnameUtils.getUserSettingDirectoryPath()
	+ "gpmodule_v3_0" + FilePathnameUtils.FILE_SEPARATOR + "consensusLastDir.conf";

	public Component getComponent() {
		return mainPanel;
	}
	
	public ConsensusClusteringViewer() {
		tableModel = new ListTableModel();
		table = new JTable(tableModel);
		table.getColumnModel().getColumn(0).setMaxWidth(60); 
		table.setRowSorter(new TableRowSorter<ListTableModel>(tableModel));
		table.addMouseListener(new MouseAdapter(){
			@Override
	        public void mouseReleased(MouseEvent e) {
				if (e.isMetaDown() && e.getComponent() instanceof JTable 
						&& table.columnAtPoint(e.getPoint()) == 1) {
	                final int row = table.rowAtPoint(e.getPoint());
	                JPopupMenu popup = new JPopupMenu();
	                JMenuItem item = new JMenuItem("Open");
	                item.addActionListener(new java.awt.event.ActionListener() {
	    				public void actionPerformed(ActionEvent paramActionEvent) {	
	    					CCData ccData = (CCData)table.getValueAt(row, 1);
	    					File file = new File(rootDir, ccData.getName());
	    					if(!file.exists()){
	    						saveFile(ccData, new File(rootDir));
	    						file.deleteOnExit();
	    					}
	    					String type = (String)(table.getValueAt(row, 2));
							if(type.equals("PDF")){
								viewPDF(file);
							}else{
								viewInTextEditor(file);
							}
	    				}
	                });
	                popup.add(item);
	                popup.show(e.getComponent(), e.getX(), e.getY());
	            }
			}
		});
		JScrollPane jsp = new JScrollPane(table);
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		JButton saveBtn = new JButton("Save selected file");
		saveBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
				JFileChooser fc = new JFileChooser(".");
				fc.setDialogTitle("Select directory to save files");
				fc.setCurrentDirectory(getLastDirectory());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int choice = fc.showOpenDialog(mainPanel);
				if (choice == JFileChooser.APPROVE_OPTION) {
					File saveDir = fc.getSelectedFile();
					setLastDirectory(saveDir);
					for(int i = 0; i < table.getRowCount(); i++){
						if(table.getValueAt(i, 0) == Boolean.TRUE){
							CCData ccData = (CCData)table.getValueAt(i, 1);
							saveFile(ccData, saveDir);
						}
					}
				}
			}
		});
		mainPanel.add(saveBtn);
		mainPanel.add(jsp);
	}
	
	private void saveFile(CCData data, File saveDir){
		File dest = new File(saveDir, data.getName());
		ByteArrayInputStream bin = null;
		BufferedOutputStream bout = null;
		try{
			bin = new ByteArrayInputStream(data.getBytes());
			bout = new BufferedOutputStream(new FileOutputStream(dest));
			int bytes = 0;
			byte[] buffer = new byte[1024];
			while((bytes = bin.read(buffer)) != -1){
				if(bytes > 0)
					bout.write(buffer, 0, bytes);
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(bin !=null)  {
				try{bin.close();}catch(IOException e){e.printStackTrace();}
			}
			if(bout!=null){
				try{bout.close();}catch(IOException e){e.printStackTrace();}
			}
		}
	}

	private File getLastDirectory() {
		String dir = FilePathnameUtils.getDataFilesDirPath();
		BufferedReader br = null;
		try {
			File file = new File(lastDirConf);
			if (file.exists()) {
				br = new BufferedReader(new FileReader(file));
				dir = br.readLine();
				br.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}if(br!=null){
			try{br.close();}catch(IOException e){e.printStackTrace();}
		}
		return new File(dir);
	}

	private void setLastDirectory(File dir) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(lastDirConf));
			bw.write(dir.getCanonicalPath());
			bw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}finally{
			if(bw!=null){
				try{bw.close();}catch(IOException e){e.printStackTrace();}
			}
		}
	}
	
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<?> dataset = event.getDataSet();
		if(dataset instanceof DSConsensusClusterResultSet){
			DSConsensusClusterResultSet ccrslt = (DSConsensusClusterResultSet)dataset;
	        ArrayList<CCData> rslt = ccrslt.getDataList();
	        Object[][] data = new Object[rslt.size()][tableHeader.length];
 	        for(int i = 0; i < rslt.size(); i++){
 	        	CCData ccData = rslt.get(i);
 	        	data[i][0] = Boolean.FALSE;
 	        	data[i][1] = ccData;
 	        	data[i][2] = ccData.getType();
	        }
 	        tableModel.setData(data);

			mainPanel.revalidate();
			mainPanel.repaint();
		}
	}
	
	private class ListTableModel extends AbstractTableModel{
		private static final long serialVersionUID = -3860589994970290199L;
		private String[] columnNames = tableHeader;
		private Object[][] tableData;
		
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			if (tableData == null) return 0;
			return  tableData.length;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (tableData == null) return null;
			return tableData[row][column];
		}
		
		@Override
		public String getColumnName(int index) {
			return columnNames[index];
		}

		@Override
		public Class<?> getColumnClass(int column){
			if (tableData == null) return null;
			return getValueAt(0, column).getClass();
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 0;
		}
		
		@Override
		public void setValueAt(Object value, int row, int column){
			tableData[row][column] = value;
		}
		
		public void setData(Object[][] data){
			tableData = data;
			fireTableDataChanged();
		}
	}
	
	private void viewPDF(File file){
		String[] args = null;
		String fpath = file.getAbsolutePath();
		String osname = System.getProperty("os.name").toLowerCase();
		if(osname.indexOf("windows") > -1)
			args = new String[]{"rundll32", "url.dll,FileProtocolHandler", fpath};
		else if(osname.indexOf("mac") > -1)
			args = new String[]{"Open", fpath};
		else args= new String[]{"/usr/bin/open", fpath};

		try{
			Runtime.getRuntime().exec(args);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,
					"Exception in opening PDF: " + e.getMessage(),
					"Unable to Open", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void viewInTextEditor(File file) {
		GlobalPreferences prefs = GlobalPreferences.getInstance();
		String editor = prefs.getTextEditor();
		if (editor == null || editor.trim().length()==0) {
			log.info("No editor configured.");
			JOptionPane.showMessageDialog(null, "No editor configured.",
					"Unable to Edit", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (Util.isRunningOnAMac())
			editor = "Open";

		String[] args = { editor, file.getAbsolutePath() };
		try {
			Runtime.getRuntime().exec(args);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Exception in opening editor: "+ e.getMessage(),
					"Unable to Edit", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
