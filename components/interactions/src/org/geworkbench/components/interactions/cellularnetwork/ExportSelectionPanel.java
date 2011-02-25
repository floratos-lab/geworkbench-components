package org.geworkbench.components.interactions.cellularnetwork;

/**
 * @author my2248
 * @version $Id$ 
 */

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.util.ArrayList; 
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 

import org.geworkbench.util.ProgressBar;

@SuppressWarnings("unchecked")
public class ExportSelectionPanel extends JPanel {

	private static final long serialVersionUID = 8300065011440745717L;

	private Log log = LogFactory.getLog(this.getClass());

	public JDialog parent = null;

	private JComboBox formatJcb = new JComboBox();
	private JComboBox presentJcb = new JComboBox();

	private ProgressBar computePb = null;
	private String fileName  = null;
	private String context = null;
	private String version = null;
	private List<String> interactionTypes = null;

 
	exportWorker worker = null;

	public ExportSelectionPanel(JDialog parent, String context, String version,
			List<String> interactionTypes) {

		this.context = context;
		this.version = version;
		this.interactionTypes = interactionTypes;

		setLayout(new GridLayout(5, 3));
		this.parent = parent;

		init();

	}

	@SuppressWarnings("unchecked")
	private void init() {

		JLabel label1 = new JLabel("File Format:    ");

		formatJcb.addItem(Constants.SIF_FORMART);
		// formatJcb.addItem("adj format");
		JLabel label2 = new JLabel("Node Present By:   ");

		presentJcb.addItem(Constants.GENE_NAME);

		presentJcb.addItem(Constants.GENE_ID);

		JButton continueButton = new JButton("Continue");
		JButton cancelButton = new JButton("Cancel");

		continueButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				continueButtonActionPerformed();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.dispose();
			}
		});
		add(new JLabel("                "));
		add(new JLabel("                "));
		add(new JLabel("                "));
		add(label1);
		add(formatJcb);
		add(new JLabel("                "));
		add(label2);
		add(presentJcb);
		add(new JLabel("                "));
		add(new JLabel("                "));
		add(new JLabel("                "));
		add(new JLabel("                "));
		add(continueButton);
		add(new JLabel("                "));
		add(cancelButton);

	}

	private void continueButtonActionPerformed() {

		

		File f = new File("export_" + context + "_" + version + ".sif");
		JFileChooser jFileChooser1 = new JFileChooser(f);
		jFileChooser1.setSelectedFile(f);
		String newFileName = null;
		if (JFileChooser.APPROVE_OPTION == jFileChooser1.showSaveDialog(null)) {

			newFileName = jFileChooser1.getSelectedFile().getPath();

		}
		else
			return;

		if (new File(newFileName).exists()) {
			int o = JOptionPane.showConfirmDialog(null,

			"Replace the file", "Replace the existing file?",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (o != JOptionPane.YES_OPTION) {
				return;
			}
		}

		parent.dispose();
		computePb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);

		worker = new exportWorker(computePb, newFileName);
		worker.execute();

	}

	private class exportWorker extends SwingWorker<Void, Void> implements
			Observer {
		
		ProgressBar pb = null;
		
		exportWorker(ProgressBar pb, String newFileName) {
			super();
			this.pb = pb;
			fileName = newFileName;
		}

		@Override
		protected void done() {
			if (this.isCancelled()) {
				log.info("Exporting task is cancel.");

			} else {
				log.info("Exporting task is done.");			 
				pb.dispose();

			}
		}

		@Override
		protected Void doInBackground() {

			pb.addObserver(this);
			pb.setTitle("Export selected interactome");
			pb.setMessage("Process exporting ...");
			pb.start();
			pb.toFront();

			try {

				File file = new File(fileName);
				file.createNewFile();
				if (!file.canWrite()) {
					JOptionPane.showMessageDialog(null,
							"Cannot write to specified file.");
					return null;
				}

				BufferedWriter writer = new BufferedWriter(new FileWriter(file));

				String selectedFormart = formatJcb.getSelectedItem().toString();
				String selectedPresent = presentJcb.getSelectedItem()
						.toString();

				InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();

				if (selectedFormart.equalsIgnoreCase(Constants.SIF_FORMART)) {

					
					for (String interactionType : interactionTypes) {
						if (isCancelled())
							break;
						List<String> sifLines = new ArrayList<String>();
						sifLines = interactionsConnection
								.getInteractionsSifFormat(context, version,
										interactionType, selectedPresent);
						for (String line : sifLines) {
							if (isCancelled())
								break;
							writer.write(line);
							writer.write("\n");

						}
                        writer.flush();
					}
				}

				writer.close();
				if (isCancelled())
					file.delete();

			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
			 

			return null;
		}

		protected ProgressBar getProgressBar() {
			return pb;
		}

		public void update(Observable o, Object arg) {
			cancel(true);
		}

	}

}