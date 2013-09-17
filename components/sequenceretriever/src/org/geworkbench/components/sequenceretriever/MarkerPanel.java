package org.geworkbench.components.sequenceretriever;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;

class MarkerPanel extends JPanel {
	/**
	 * 
	 */
	private final SequenceRetriever sequenceRetriever;

	private static final long serialVersionUID = 8681983607024585608L;

	public static final String NEXT_BUTTON_TEXT = "Find";

	public static final String SEARCH_LABEL_TEXT = "Search:";

	private JList list;

	private JButton nextButton;

	private JTextField searchField;

	private DefaultListModel model;

	private JScrollPane scrollPane;

	public MarkerPanel(SequenceRetriever sequenceRetriever) {
		super();
		this.sequenceRetriever = sequenceRetriever;
		model = new DefaultListModel();
		setLayout(new BorderLayout());
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		JLabel searchLabel = new JLabel(SEARCH_LABEL_TEXT);
		nextButton = new JButton(NEXT_BUTTON_TEXT);
		searchField = new JTextField();
		list = new JList(model);
		scrollPane = new JScrollPane();
		// Compose components
		topPanel.add(searchLabel);
		topPanel.add(searchField);
		topPanel.add(nextButton);
		add(topPanel, BorderLayout.NORTH);
		scrollPane.getViewport().setView(list);
		add(scrollPane, BorderLayout.CENTER);
		// Add appropriate listeners
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findNext();
			}
		});

		searchField.getDocument().addDocumentListener(
				new DocumentListener() {
					public void insertUpdate(DocumentEvent e) {
						searchFieldChanged();
					}

					public void removeUpdate(DocumentEvent e) {
						searchFieldChanged();
					}

					public void changedUpdate(DocumentEvent e) {
						searchFieldChanged();
					}
				});

	}

	/**
	 * Override to customize the result of the 'next' button being clicked
	 * (or ENTER being pressed in text field).
	 */
	private void findNext() {
		searchField.setForeground(Color.black);

		String text = searchField.getText().toLowerCase();
		findNext(text);

		int confirm = JOptionPane.showConfirmDialog(this,
				"Use the markers to retrieve sequences?");
		if (confirm == JOptionPane.YES_OPTION) {
			this.sequenceRetriever.cleanUpCurrentView();
			if (model.getSize() > 0) {
				this.sequenceRetriever.initializeRetrievedSequencesPanel();
				sequenceRetriever.resetProgressBar();

				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

					@Override
					protected Void doInBackground() throws Exception {
						int size = model.getSize();
						Vector<DSGeneMarker> list = new Vector<DSGeneMarker>();
						for (int x = 0; x < size; ++x) {
							DSGeneMarker marker = (DSGeneMarker) model
									.get(x);
							list.addElement(marker);
						}
						MarkerPanel.this.sequenceRetriever.getSequences(list);

						return null;
					}

					@Override
					protected void done() {
						if (MarkerPanel.this.sequenceRetriever.status.equalsIgnoreCase(SequenceRetriever.STOP)) {
							MarkerPanel.this.sequenceRetriever.updateProgressBar(100,
									"Stopped on " + new Date());
						} else {
							MarkerPanel.this.sequenceRetriever.updateProgressBar(100,
									"Finished on " + new Date());
							MarkerPanel.this.sequenceRetriever.updateSelectedListUI();
							MarkerPanel.this.sequenceRetriever.updateRetrievedSequencesPanel();
						}
						MarkerPanel.this.sequenceRetriever.disableStopButton();
						
					}
				};
				worker.execute();
			}

		}
	}

	/**
	 * Search the markerList to get the matched markers.
	 */
	private void findNext(String query) {
		model.removeAllElements();
		DSItemList<DSGeneMarker> markerList = sequenceRetriever.getMarkerList();
		if (markerList != null) {
			Object theOne = markerList.get(query);
			if (theOne != null) {
				model.addElement(theOne);

			}
			for (Object o : markerList) {
				String element = o.toString().toLowerCase();
				if (element.contains(query)) {
					model.addElement(o);
				}
			}
		}
	}

	private void searchFieldChanged() {
		searchField.setForeground(Color.black);
	}

}