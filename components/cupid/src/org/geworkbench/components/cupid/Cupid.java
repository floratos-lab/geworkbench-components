/**
 * 
 */
package org.geworkbench.components.cupid;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.util.CsvFileFilter;
import org.geworkbench.util.ProgressDialog;
import org.geworkbench.util.ProgressItem;
import org.geworkbench.util.ProgressTask;

import com.Ostermiller.util.CSVPrinter;

/**
 * @author zji
 * 
 */
public class Cupid extends JPanel implements VisualPlugin {

	private static final String MI_RNA_ID = "miRNA ID";
	private static final String REF_SEQ_ID = "RefSeq ID";
	private static final long serialVersionUID = 1717902242692528577L;
	private Log log = LogFactory.getLog(Cupid.class);

	private static final String defaultServerUrl = System.getProperty("cupid.host");

	public Cupid() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		JPanel panel1 = new JPanel();
		panel1.add(new JLabel("Server URL"));
		final JTextField urlField = new JTextField(defaultServerUrl, 20);
		panel1.add(urlField);

		JPanel panel2 = new JPanel();
		panel2.add(new JLabel("Query Type"));
		final JComboBox queryTypeComboBox = new JComboBox(new String[] {
				REF_SEQ_ID, MI_RNA_ID });
		panel2.add(queryTypeComboBox);
		panel2.add(new JLabel("Query Value"));
		final JTextField queryValueField = new JTextField(10);
		panel2.add(queryValueField);
		final JButton submitButton = new JButton("Submit");
		submitButton.addActionListener(new ActionListener() {

			final ProgressDialog pd = ProgressDialog.getInstance(true);
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ProgressTask<Void, Void> task = new ProgressTask<Void, Void>(ProgressItem.INDETERMINATE_TYPE, "Querying CUPID database") {

					
					@Override
					protected Void doInBackground() throws Exception {
						queryRemoteData();
						return null;
					}
					
			    	@Override
					protected void done(){
			    		pd.removeTask(this);
			    	}
				};
				pd.executeTask(task);
			}
			
			private void queryRemoteData() {
				String queryType = encodeSpace( (String) queryTypeComboBox.getSelectedItem() );
				String queryValue = encodeSpace( queryValueField.getText() );
				String host = urlField.getText();

				URL url;
				try {
					url = new URL(host+"/CupidServlet/CupidServlet?type="+queryType+"&value="+queryValue);
					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();

					int respCode = connection.getResponseCode();
					if (respCode != HttpURLConnection.HTTP_OK) {
						log.error("CupidServlet is not working: " + respCode);
						return;
					}

					BufferedReader in = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
					
					String line = in.readLine();
					Vector<String[]> data = new Vector<String[]>();
					while(line!=null) {
						String[] fields = line.split("\\|");
						data.add(fields);
						line = in.readLine();
					}

					in.close();
					
					tableModel.setValues(data);
					tableModel.fireTableDataChanged();

				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
					return;
				}

			}

		});
		panel2.add(submitButton);

		JButton exportButton = new JButton("Export");
		exportButton.setToolTipText("Export to CSV files");
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc=new JFileChooser();
				fc.removeChoosableFileFilter(fc.getAcceptAllFileFilter());
				fc.setFileFilter(new CsvFileFilter());
				fc.setDialogTitle("Save ceRNA results");
				int choice = fc.showOpenDialog(Cupid.this.getComponent());
				if (choice == JFileChooser.APPROVE_OPTION) {
					String fname = fc.getSelectedFile().getAbsolutePath();
					if (!fname.toLowerCase().endsWith(".csv")){
						fname += ".csv";
					}
					File f = new File(fname);
					if(f.exists()){
						int option = JOptionPane.showConfirmDialog(null,
								"File exists. Replace it?",
								"Replace the existing file?",
								JOptionPane.YES_NO_OPTION);
						if(option != JOptionPane.YES_OPTION) return;
					}
					CSVPrinter csvout = null;
					try {
						csvout = new CSVPrinter(new BufferedOutputStream(
								new FileOutputStream(fname)));
						for (int i = 0; i < tableModel.getColumnCount(); i++) {
							csvout.print(tableModel.getColumnName(i));
						}
						csvout.println();
						for (int row = 0; row < tableModel.getRowCount(); row++) {
							for (int col = 0; col < tableModel.getColumnCount(); col++){
								csvout.print((String)tableModel.getValueAt(row, col));
							}
							csvout.println();
						}
						csvout.flush();
						csvout.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					} finally{
						if(csvout!=null) {
							try{csvout.close();}catch(Exception ex){ex.printStackTrace();}
						}
					}
				}
			}
		});
		panel2.add(exportButton);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
		topPanel.add(panel1);
		topPanel.add(panel2);
		topPanel.setMaximumSize(new Dimension(500, 30));

		result = new JTable(tableModel);
		result.setAutoCreateRowSorter(true);
		JScrollPane scrollPane = new JScrollPane(result);

		add(topPanel);
		add(scrollPane);
	}
	
	private JTable result; 
	private CupidTableModel tableModel = new CupidTableModel();

	private static String encodeSpace(String s) {
		return s.replaceAll(" " , "%20");
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

}
