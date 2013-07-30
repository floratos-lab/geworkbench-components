package org.geworkbench.components.cerna;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.util.ProgressDialog;
import org.geworkbench.util.ProgressItem;
import org.geworkbench.util.ProgressTask;

public class CeRNA extends JPanel implements VisualPlugin {

	private static final long serialVersionUID = 1717902242692528577L;
	private Log log = LogFactory.getLog(CeRNA.class);
	private static final String[] queryTypes = {"gene ID", "miRNA ID"};
	private static final String[] tableHeader = {"InteractionID", "Gene1", "Gene2", "miRNA"};
	private static final ProgressDialog pd = ProgressDialog.getInstance(false);
	private static final String defaultServerUrl = "http://afdev.c2b2.columbia.edu:9090";
	private JTextField urlField = new JTextField(defaultServerUrl, 20);
	private JComboBox dbBox = new JComboBox();
	private JComboBox queryTypeComboBox = new JComboBox(queryTypes);
	private JTextField queryValueField = new JTextField(10);

	public CeRNA() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		JPanel urlPanel = new JPanel();
		urlPanel.add(new JLabel("Server URL"));
		urlPanel.add(urlField);

		JPanel dbPanel = new JPanel();
		dbBox.setPreferredSize(new Dimension(130, dbBox.getPreferredSize().height));
		JButton dbButton = new JButton("Retrieve ceRNA Database List");
		dbPanel.add(dbBox);
		dbPanel.add(dbButton);	
		
		JPanel valPanel = new JPanel();
		valPanel.add(new JLabel("Query Type"));
		valPanel.add(queryTypeComboBox);
		valPanel.add(new JLabel("Query Value"));
		valPanel.add(queryValueField);
		final JButton submitButton = new JButton("Submit");
		submitButton.setEnabled(false);
		valPanel.add(submitButton);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
		topPanel.add(urlPanel);
		topPanel.add(dbPanel);
		topPanel.add(valPanel);
		topPanel.setMaximumSize(new Dimension(500, 30));

		final CeRNATableModel tableModel = new CeRNATableModel();
		JTable result = new JTable(tableModel);
		result.setAutoCreateRowSorter(true);
		final JScrollPane scrollPane = new JScrollPane(result);

		add(topPanel);
		add(scrollPane);
		
		dbButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				final String host = urlField.getText().trim();
				if(host.length()==0){
					JOptionPane.showMessageDialog(null, "Please enter server url");
					return;
				}
				ProgressTask<ArrayList<String>, Void> task = new ProgressTask<ArrayList<String>, Void>(ProgressItem.INDETERMINATE_TYPE, "Retrieving ceRNA database") {
					protected ArrayList<String> doInBackground() throws Exception {
						return queryDB(host);
					}					
					protected void done(){
			    		pd.removeTask(this);
			    		if (isCancelled()) return;
			    		ArrayList<String> res = new ArrayList<String>();
			    		try{
			    			res = get();
			    		}catch(Exception e){
			    			e.printStackTrace();
			    			return;
			    		}
			    		dbBox.removeAllItems();
			    		for(String str : res){
			    			dbBox.addItem(str);
			    		}
						dbBox.setSelectedItem("OV_ceRNA_v1");
			    	}
				};
				pd.executeTask(task);
			}
		});

		dbBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(dbBox.getSelectedIndex() > -1){
					submitButton.setEnabled(true);
				}
			}
		});

		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String queryDb	= (String)dbBox.getSelectedItem();
				final String queryType	= encodeSpace( (String) queryTypeComboBox.getSelectedItem() );
				final String queryValue	= encodeSpace( queryValueField.getText().trim().toUpperCase() );
				final String host		= urlField.getText().trim();
				if(queryValue.length()==0){
					JOptionPane.showMessageDialog(null, "Please enter query value");
					return;
				}
				if(host.length()==0){
					JOptionPane.showMessageDialog(null, "Please enter server url");
					return;
				}
				ProgressTask<ArrayList<String[]>, Void> task = new ProgressTask<ArrayList<String[]>, Void>(ProgressItem.INDETERMINATE_TYPE, "Querying ceRNA database") {
					protected ArrayList<String[]> doInBackground() throws Exception {
						return queryRemoteData(queryDb, queryType, queryValue, host);
					}					
					protected void done(){
			    		pd.removeTask(this);
			    		if (isCancelled()) return;
			    		ArrayList<String[]> res = new ArrayList<String[]>();
			    		try{
			    			res = get();
			    		}catch(Exception e){
			    			e.printStackTrace();
			    			return;
			    		}
			    		tableModel.setData(res.toArray(new String[0][0]));
			    	}
				};
				pd.executeTask(task);
			}
		});
	}
	
	private ArrayList<String> queryDB(String host){
		ArrayList<String> res = new ArrayList<String>();
		HttpURLConnection connection = null;
		BufferedReader in = null;
		try {
			URL url = new URL(host+"/ceRNAServlet");
			connection = (HttpURLConnection) url.openConnection();

			int respCode = connection.getResponseCode();
			if (respCode != HttpURLConnection.HTTP_OK) {
				JOptionPane.showMessageDialog(null, "ceRNAServlet is not working: " + url.toString()+" "+respCode);
				log.error("ceRNAServlet is not working: " + url.toString()+" "+respCode);
				return res;
			}

			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = null;
			while((line = in.readLine())!=null) {
				res.add(line);
			}
			return res;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Exception in retrieving ceRNA database: "+e.getMessage());
			return res;
		} finally{
			if(in != null) 
				try{ in.close(); }catch(Exception e){e.printStackTrace();}
			if(connection!=null) connection.disconnect();
		}
	}

	private ArrayList<String[]> queryRemoteData(String queryDb, String queryType, String queryValue, String host) {
		ArrayList<String[]> res = new ArrayList<String[]>();
		HttpURLConnection connection = null;
		BufferedReader in = null;
		try {
			URL url = new URL(host+"/ceRNAServlet/ceRNAServlet?db="+queryDb+"&type="+queryType+"&value="+queryValue);
			connection = (HttpURLConnection) url.openConnection();

			int respCode = connection.getResponseCode();
			if (respCode != HttpURLConnection.HTTP_OK) {
				JOptionPane.showMessageDialog(null, "ceRNAServlet is not working: " + url.toString()+" "+respCode);
				log.error("ceRNAServlet is not working: " + respCode);
				return res;
			}

			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = null;
			while((line = in.readLine())!=null) {
				res.add(line.split("\\|"));
			}
			return res;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Exception in querying ceRNA database: "+e.getMessage());
			return res;
		} finally{
			if(in != null) 
				try{ in.close(); }catch(Exception e){e.printStackTrace();}
			if(connection!=null) connection.disconnect();
		}
	}

	private static String encodeSpace(String s) {
		return s.replaceAll(" " , "%20");
	}
	
	@Override
	public Component getComponent() {
		return this;
	}
	
	private class CeRNATableModel extends AbstractTableModel{
		private static final long serialVersionUID = 2530510297699442190L;
		private String[][] data;
		private String[] columnNames = tableHeader;
		
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			if (data==null) return 0;
			return data.length;
		}

		@Override
		public Object getValueAt(int row, int col) {
			if(data==null) return null;
			return data[row][col];
		}
		
		@Override
		public String getColumnName(int col){
			return columnNames[col];
		}
		
		public void setData(String[][] tableData){
			data = tableData;
			fireTableDataChanged();
		}
		
	}

}
