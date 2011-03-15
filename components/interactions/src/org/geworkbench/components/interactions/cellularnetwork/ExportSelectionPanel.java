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

public class ExportSelectionPanel extends JPanel {

	private static final long serialVersionUID = 8300065011440745717L;

	private Log log = LogFactory.getLog(ExportSelectionPanel.class);

	private final JDialog parent;

	private JComboBox formatJcb = new JComboBox();
	private JComboBox presentJcb = new JComboBox();

	private String fileName = null;
	private final String context;
	private final String version;
	private final List<String> interactionTypes;

	public ExportSelectionPanel(JDialog parent, String context, String version,
			List<String> interactionTypes) {

		this.context = context;
		this.version = version;
		this.interactionTypes = interactionTypes;

		setLayout(new GridLayout(5, 3));
		this.parent = parent;

		init();

	}

	private void init() {

		JLabel label1 = new JLabel("File Format:    ");

		formatJcb.addItem(Constants.SIF_FORMART);
		formatJcb.addItem(Constants.ADJ_FORMART);
		JLabel label2 = new JLabel("Node Represented By:   ");

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
		String selectedFormart = formatJcb.getSelectedItem().toString();
		if (selectedFormart.equals(Constants.ADJ_FORMART))
			f = new File("export_" + context + "_" + version + ".adj");
		JFileChooser jFileChooser1 = new JFileChooser(f);
		jFileChooser1.setSelectedFile(f);
		String newFileName = null;
		if (JFileChooser.APPROVE_OPTION == jFileChooser1.showSaveDialog(null)) {
			newFileName = jFileChooser1.getSelectedFile().getPath();
		} else {
			return;
		}

		if (new File(newFileName).exists()) {
			int o = JOptionPane.showConfirmDialog(null,

			"Replace the file", "Replace the existing file?",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (o != JOptionPane.YES_OPTION) {
				return;
			}
		}

		parent.dispose();
		ProgressBar computePb = ProgressBar
				.create(ProgressBar.INDETERMINATE_TYPE);

		new ExportWorker(computePb, newFileName).execute();

	}

	private class ExportWorker extends SwingWorker<Void, Void> implements
			Observer {

		ProgressBar pb = null;

		ExportWorker(ProgressBar pb, String newFileName) {
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

				for (String interactionType : interactionTypes) {
					if (isCancelled())
						break;
					List<String> lines = new ArrayList<String>();

					if (selectedFormart.equalsIgnoreCase(Constants.SIF_FORMART))
						lines = interactionsConnection
								.getInteractionsSifFormat(context, version,
										interactionType, selectedPresent);
					else if ((selectedFormart
							.equalsIgnoreCase(Constants.ADJ_FORMART)))
						lines = interactionsConnection
								.getInteractionsAdjFormat(context, version,
										interactionType, selectedPresent);

					for (String line : lines) {
						if (isCancelled())
							break;
						writer.write(line);
						writer.write("\n");

					}
					writer.flush();
				}

				writer.close();
				if (isCancelled())
					file.delete();

			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}

			return null;
		}

		public void update(Observable o, Object arg) {
			cancel(true);
		}

	}

}