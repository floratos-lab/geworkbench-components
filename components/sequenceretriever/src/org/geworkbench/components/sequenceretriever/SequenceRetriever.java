package org.geworkbench.components.sequenceretriever;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.sequences.GeneChromosomeMatcher;

/**
 * The plug-in component to retrieve sequences.
 * 
 * @author Xuegong Wang
 * @author manjunath at genomecenter dot columbia dot edu
 * @author xiaoqing at genomecenter dot columbia dot edu
 * @version $Id$
 */

@AcceptTypes({ DSMicroarraySet.class })
public class SequenceRetriever implements VisualPlugin {
	private Log log = LogFactory.getLog(SequenceRetriever.class);

	private static final String UCSC = "UCSC";
	private static final String EBI = "EBI";

	private final static String NORMAL = "normal";
	final static String STOP = "stop";
	private final static String CLEAR = "clear";
	private final static String RUNNING = "running";
	private final static String PROTEINVIEW = "Protein";
	private final static String DNAVIEW = "DNA";

	volatile String status = NORMAL;

	private CSSequenceSet<DSSequence> dnaSequenceDB = new CSSequenceSet<DSSequence>();

	private CSSequenceSet<DSSequence> proteinSequenceDB = new CSSequenceSet<DSSequence>();

	private static final String NOANNOTATION = "---";

	private DSMicroarraySet refMASet = null;

	private TreeMap<String, ArrayList<String>> currentRetrievedSequences = new TreeMap<String, ArrayList<String>>();

	// Map the sequence name with the sequence display
	private HashMap<String, RetrievedSequenceView> currentRetrievedMap = new HashMap<String, RetrievedSequenceView>();

	// Map the marker name with associated sequence name.
	private TreeMap<String, ArrayList<String>> retrievedProteinSequences = new TreeMap<String, ArrayList<String>>();

	// Map the sequence name with the sequence display
	private HashMap<String, RetrievedSequenceView> retrievedProteinMap = new HashMap<String, RetrievedSequenceView>();

	// Map the marker name with associated sequence name.
	private TreeMap<String, ArrayList<String>> retrievedDNASequences = new TreeMap<String, ArrayList<String>>();

	// //Map the sequence name with the sequence display
	private HashMap<String, RetrievedSequenceView> retrievedDNAMap = new HashMap<String, RetrievedSequenceView>();

	/* four maps named "cached...' are to support geneSelectorEvent only*/
	// Map the marker name with associated sequence name.
	private TreeMap<String, ArrayList<String>> cachedRetrievedProteinSequences = new TreeMap<String, ArrayList<String>>();

	// Map the sequence name with the sequence display
	private HashMap<String, RetrievedSequenceView> cachedRetrievedProteinMap = new HashMap<String, RetrievedSequenceView>();

	// Map the marker name with associated sequence name.
	private TreeMap<String, ArrayList<String>> cachedRetrievedDNASequences = new TreeMap<String, ArrayList<String>>();

	// //Map the sequence name with the sequence display
	private HashMap<String, RetrievedSequenceView> cachedRetrievedDNAMap = new HashMap<String, RetrievedSequenceView>();

	// Layouts,Panels and Panes
	private JPanel main = new JPanel();

	private RetrievedSequencesPanel seqDisPanel = new RetrievedSequencesPanel();

	final static private SpinnerNumberModel upstreamSpinnerModel = new SpinnerNumberModel(2000, 0, 98000, 100);

	private JSpinner beforeText = new JSpinner();

	final static private SpinnerNumberModel downstreamSpinnerModel = new SpinnerNumberModel(1000, 0, 10000, 100);

	private JSpinner afterText = new JSpinner();

	private JButton stopButton = new JButton();

	private DefaultListModel listModel = new DefaultListModel();

	// TODO check if it is necessary to directly manipulate list on GUI
	private JList jSelectedList = new JList();

	private JComboBox jComboCategory = new JComboBox();

	private JComboBox jSourceCategory = new JComboBox();

	private JProgressBar jProgressBar1 = new JProgressBar();

	DefaultListModel getListModel() {
		return listModel;
	}

	public SequenceRetriever() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	TreeMap<String, ArrayList<String>> getCurrentRetrievedSequences() {
		return currentRetrievedSequences;
	}
	
	private void jbInit() throws Exception {

		jProgressBar1.setMinimumSize(new Dimension(10, 16));
		jProgressBar1.setBorderPainted(true);

		JLabel jLabel6 = new JLabel("Type");
		jLabel6.setHorizontalAlignment(SwingConstants.LEFT);

		JSplitPane rightPanel = new JSplitPane();
		rightPanel = new JSplitPane();
		rightPanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

		seqDisPanel.setBorder(null);
		seqDisPanel.setMinimumSize(new Dimension(10, 10));

		main.setLayout(new BorderLayout());

		JScrollPane seqScrollPane = new JScrollPane();
		seqScrollPane.setBorder(BorderFactory.createEtchedBorder());
		seqScrollPane.setMaximumSize(new Dimension(32767, 32767));
		seqScrollPane.setMinimumSize(new Dimension(24, 24));
		seqScrollPane.setPreferredSize(new Dimension(250, 250));

		beforeText.setModel(upstreamSpinnerModel);
		afterText.setModel(downstreamSpinnerModel);

		JLabel jLabel1 = new JLabel();
		jLabel1.setToolTipText("Downstream");
		jLabel1.setText("+");
		JLabel jLabel2 = new JLabel();
		jLabel2.setToolTipText("Upstream");
		jLabel2.setText("-");
		seqDisPanel.setMaximumSize(new Dimension(32767, 32767));
		seqDisPanel.setPreferredSize(new Dimension(216, 40));

		JButton jActivateBttn = new JButton();
		jActivateBttn.setToolTipText("Add a data node to current project");
		jActivateBttn.setText("Add To Project");
		jActivateBttn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jActivateBttn_actionPerformed(e);
			}
		});

		JButton jButton2 = new JButton();
		jButton2.setToolTipText("Get sequence of selected markers");
		jButton2.setHorizontalTextPosition(SwingConstants.TRAILING);
		jButton2.setText("Get Sequence");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getSequences();
			}
		});
		stopButton.setText(STOP);
		stopButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				status = STOP;
				stopButton.setEnabled(false);
			}
		});

		JButton clearButton = new JButton();
		clearButton.setText(CLEAR);
		clearButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				status = CLEAR;
				cleanUpAll();
			}
		});

		jSelectedList = new JList(listModel) {
			private static final long serialVersionUID = 7249432162980526455L;

			public String getToolTipText(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				if (-1 < index) {
					String item = getModel().getElementAt(index).toString();
					ArrayList<String> arraylist = currentRetrievedSequences
							.get(item);
					if (arraylist != null && arraylist.size() > 0) {
						if (arraylist.size() == 1) {
							return arraylist.size() + " sequence found.";
						} else {
							return arraylist.size() + " sequences found.";
						}
					} else {
						return "No sequence.";
					}
				}
				return null;
			}
		};
		jSelectedList.setCellRenderer(new SequenceListRenderer(this));
		jSelectedList
				.addListSelectionListener(new SequenceListSelectionListener(this));
		jComboCategory.setSelectedItem(DNAVIEW);
		jComboCategory.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				changeSequenceCategory();
			}
		});

		JToolBar jToolbar2 = new JToolBar();
		jToolbar2.setBorder(BorderFactory.createEtchedBorder());
		jToolbar2.add(jLabel2, null);
		jToolbar2.add(beforeText, null);
		jToolbar2.add(jLabel1, null);
		jToolbar2.add(afterText, null);
		jToolbar2.add(stopButton, null);
		jToolbar2.add(clearButton, null);
		jToolbar2.add(jButton2, null);
		jToolbar2.add(jActivateBttn, null);
		main.add(jToolbar2, BorderLayout.SOUTH);
		
		JPanel jPanel1 = new JPanel();
		jPanel1.setLayout(new FlowLayout());

		JPanel jPanel2 = new JPanel();
		jPanel2.setLayout(new BorderLayout());
		jPanel2.add(rightPanel, BorderLayout.CENTER);
		
		JPanel jPanel4 = new JPanel();
		jPanel4.setLayout(new BorderLayout());
		
		JScrollPane jScrollPane2 = new JScrollPane();

		JTabbedPane tabPane = new JTabbedPane();

		JPanel markerPanel = new MarkerPanel(this);
		tabPane.add("Marker", jPanel4);
		tabPane.add("Find a Marker", markerPanel);
		rightPanel.add(seqScrollPane, JSplitPane.RIGHT);
		rightPanel.add(tabPane, JSplitPane.LEFT);
		jPanel4.add(jScrollPane2, BorderLayout.CENTER);
		jScrollPane2.getViewport().add(jSelectedList, null);
		jPanel2.add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jLabel6, null);
		jPanel1.add(jComboCategory, null);
		jPanel1.add(jSourceCategory, null);
		main.add(jProgressBar1, BorderLayout.NORTH);
		seqScrollPane.getViewport().add(seqDisPanel, null);
		main.add(jPanel2, BorderLayout.CENTER);
		jComboCategory.addItem(DNAVIEW);
		jComboCategory.addItem(PROTEINVIEW);
	}

	private void changeSequenceCategory() {
		CSSequenceSet<DSSequence> sequenceDB = null;
		String sequenceType = (String) jComboCategory.getSelectedItem();
		if (sequenceType.equalsIgnoreCase(PROTEINVIEW)) {
			beforeText.setEnabled(false);
			afterText.setEnabled(false);
			jSourceCategory.removeAllItems();
			jSourceCategory.addItem(EBI);
			sequenceDB = proteinSequenceDB;
			currentRetrievedMap = retrievedProteinMap;
			currentRetrievedSequences = retrievedProteinSequences;
			updateDisplay(proteinSequenceDB, retrievedProteinMap);
			seqDisPanel.setDNA(false);
		} else {
			beforeText.setEnabled(true);
			afterText.setEnabled(true);
			jSourceCategory.removeAllItems();
			jSourceCategory.addItem(UCSC);

			sequenceDB = dnaSequenceDB;
			currentRetrievedMap = retrievedDNAMap;
			currentRetrievedSequences = retrievedDNASequences;
			updateDisplay(dnaSequenceDB, retrievedDNAMap);
			seqDisPanel.setDNA(true);
		}
		sequenceDB.parseMarkers();
		jSelectedList.updateUI();

	}

	/* only call this from EDT */
	void updateProgressBar(final double percent, final String text) {
		jProgressBar1.setForeground(Color.GREEN);
		jProgressBar1.setString(text);
		jProgressBar1.setValue((int) (percent * 100));
		if (text.startsWith("Stop")) {
			jProgressBar1.setForeground(Color.RED);
		}
	}

	/* call this from non-EDT */
	private void updateProgressBarFromBackgroundThread(final double percent,
			final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateProgressBar(percent, text);
			}
		});
	}

	/**
	 * Clean up both views and the underlying data
	 */
	private void cleanUpAll() {
		// FIXME are these 'current' things all necessary?
		currentRetrievedMap = new HashMap<String, RetrievedSequenceView>();
		currentRetrievedSequences = new TreeMap<String, ArrayList<String>>();

		dnaSequenceDB = new CSSequenceSet<DSSequence>();
		retrievedDNAMap = new HashMap<String, RetrievedSequenceView>();
		retrievedDNASequences = new TreeMap<String, ArrayList<String>>();

		proteinSequenceDB = new CSSequenceSet<DSSequence>();
		retrievedProteinMap = new HashMap<String, RetrievedSequenceView>();
		retrievedProteinSequences = new TreeMap<String, ArrayList<String>>();

		seqDisPanel
				.setRetrievedMap(new HashMap<String, RetrievedSequenceView>());
		seqDisPanel.initialize();
		jSelectedList.clearSelection();
		jSelectedList.repaint();

	}

	/**
	 * Clean up the current view;
	 */
	void cleanUpCurrentView() {
		String sequenceType = (String) jComboCategory.getSelectedItem();
		if (sequenceType.endsWith(DNAVIEW)) {
			dnaSequenceDB = new CSSequenceSet<DSSequence>();
			retrievedDNAMap = new HashMap<String, RetrievedSequenceView>();
			retrievedDNASequences = new TreeMap<String, ArrayList<String>>();
		} else if (sequenceType.endsWith(PROTEINVIEW)) {
			proteinSequenceDB = new CSSequenceSet<DSSequence>();
			retrievedProteinMap = new HashMap<String, RetrievedSequenceView>();
			retrievedProteinSequences = new TreeMap<String, ArrayList<String>>();
		} else {
			log.error("Unknown sequence type :" + sequenceType);
			return;
		}
		seqDisPanel
				.setRetrievedMap(new HashMap<String, RetrievedSequenceView>());
		seqDisPanel.initialize();
		jSelectedList.clearSelection();
		jSelectedList.repaint();
	}
	
	/**
	 * The main entry point to get the sequences.
	 * 
	 */
	private void getSequences() {
		final String lastSequenceType = (String) jComboCategory.getSelectedItem();
		
		cleanUpCurrentView();
		status = RUNNING;
		stopButton.setEnabled(true);

		if (listModel.getSize() == 0)
			JOptionPane.showMessageDialog(null,
					"Please select gene(s) or marker(s).");

		seqDisPanel.initialize();
		jProgressBar1.setIndeterminate(false);
		jProgressBar1.setMinimum(0);
		jProgressBar1.setMaximum(100);
		jProgressBar1.setStringPainted(true);
		updateProgressBar(0, "");

		String annotationFileName = ((CSMicroarraySet) refMASet)
				.getAnnotationFileName();
		if (annotationFileName == null) {
			JOptionPane
					.showMessageDialog(null,
							"No annotation file was loaded for this dataset, cannot retrieve sequences.");
			return;
		}

		SwingWorker<Void, Void> t = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				int size = listModel.getSize();
				Vector<DSGeneMarker> list = new Vector<DSGeneMarker>();
				for (int x = 0; x < size; ++x) {
					DSGeneMarker marker = (DSGeneMarker) listModel.get(x);
					list.addElement(marker);
				}

				if (!getSequences(list)) {
					log.warn("getSequences did not succeed.");
				}
				return null;
			}

			@Override
			public void done() {
				if (status.equalsIgnoreCase(STOP)) {
					updateProgressBar(100, "Stopped on " + new Date());
				} else {
					updateProgressBar(100, "Finished on " + new Date());
					jSelectedList.updateUI();
					seqDisPanel.setRetrievedMap(currentRetrievedMap);

				}
				stopButton.setEnabled(false);
				jComboCategory.setSelectedItem(lastSequenceType);
			}

		};
		t.execute();
	}

	void initializeRetrievedSequencesPanel() {
		seqDisPanel.initialize();
	}
	
	void updateRetrievedSequencesPanel() {
		seqDisPanel.setRetrievedMap(currentRetrievedMap);
	}
	
	void jActivateBttn_actionPerformed(ActionEvent e) {
		CSSequenceSet<DSSequence> sequenceDB = null;
		String sequenceType = (String) jComboCategory.getSelectedItem(); 
		if (sequenceType.equalsIgnoreCase(DNAVIEW)) {
			sequenceDB = dnaSequenceDB;
			currentRetrievedMap = retrievedDNAMap;
			currentRetrievedSequences = retrievedDNASequences;
			if (cachedRetrievedDNAMap == null) {
				cachedRetrievedDNAMap = currentRetrievedMap;
				cachedRetrievedDNASequences = retrievedDNASequences;
			} else {
				cachedRetrievedDNAMap.putAll(currentRetrievedMap);
				cachedRetrievedDNASequences.putAll(currentRetrievedSequences);
			}
		} else {
			sequenceDB = proteinSequenceDB;
		}
		if ((sequenceDB != null) && (sequenceDB.getSequenceNo() >= 1)) {

			CSSequenceSet<DSSequence> selectedSequenceDB = new CSSequenceSet<DSSequence>();
			for (Object sequence : sequenceDB) {
				if (sequence != null) {
					RetrievedSequenceView retrievedSequenceView = currentRetrievedMap
							.get(sequence.toString());
					if (retrievedSequenceView.isIncluded()) {
						selectedSequenceDB.addASequence((DSSequence) sequence);
					}
				}
			}
			if (selectedSequenceDB.size() > 0) {
				String fileName = this.getRandomFileName();
				selectedSequenceDB.writeToFile(fileName);
				String label = JOptionPane
						.showInputDialog("Please enter a name for the dataset");
				if (label != null) {
					selectedSequenceDB.setLabel(label);
					selectedSequenceDB.parseMarkers();
					HistoryPanel.addToHistory(selectedSequenceDB,
							generateHistStr());
					ProjectNodeAddedEvent event = new ProjectNodeAddedEvent(
							"message", selectedSequenceDB, null);
					publishProjectNodeAddedEvent(event);

				}
			} else {
				JOptionPane.showMessageDialog(null,
						"Please select at least one sequence.");
			}
		}
	}

	/* on background thread (non-EDT) */
	private boolean getDnaSequences(Vector<DSGeneMarker> selectedList) {

		RetrievedSequenceView.setUpstreamTotal(((Integer) upstreamSpinnerModel.getNumber())
				.intValue());
		RetrievedSequenceView.setDownstreamTotal(((Integer) downstreamSpinnerModel.getNumber())
				.intValue());

			int startPoint = ((Integer) upstreamSpinnerModel.getNumber()).intValue();
			int endPoint = ((Integer) downstreamSpinnerModel.getNumber()).intValue();

			String annotationFileName = ((CSMicroarraySet) refMASet)
					.getAnnotationFileName();
			String database = SequenceFetcher.matchChipType(annotationFileName);

			/* No selection was made */
			if (database == null) {
				return false;
			}

			boolean serverWorking = true;
			for (int i = 0; i < selectedList.size(); i++) {
				if (!serverWorking)
					break;
				final DSGeneMarker marker = (DSGeneMarker) selectedList.get(i);
				final double progress = (double) (i + 1)
						/ (double) (selectedList.size());
				if (status.equalsIgnoreCase(STOP)) {
					return false;
				}
				updateProgressBarFromBackgroundThread(progress, "Retrieving "
						+ marker.getLabel());

				String[] knownGeneName = AnnotationParser.getInfo(
						marker.getLabel(), AnnotationParser.REFSEQ);
				if (knownGeneName == null || (knownGeneName.length==1 && (knownGeneName[0] == null || knownGeneName[0].equals(NOANNOTATION))) ) {
					Object[] options = { "Continue", "Cancel retrieval" };
					int n = JOptionPane
							.showOptionDialog(null,
									"No annotation information was available for marker "
											+ marker.getLabel()
											+ ". Cannot retrieve sequence.",
									"No annotation information", JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE, null, options,
									options[0]);
					if (JOptionPane.YES_OPTION == n)
						continue;
					else
						return false;
				}

				for (String geneName : knownGeneName) {
					if (!serverWorking)
						break;
					if (geneName == null || geneName.equals(NOANNOTATION)) {
						log.error("invalid gene symbol "+geneName); // should never happen because of the previous checking
						continue;
					}
					geneName = geneName.trim();
					Vector<GeneChromosomeMatcher> geneChromosomeMatchers = null;
					try {
						geneChromosomeMatchers = SequenceFetcher
								.getGeneChromosomeMatchers(geneName, database);
					} catch (SQLException sqle) {
						JOptionPane.showMessageDialog(null,
								"SQL Exception:\n"+sqle.getMessage(),
								"SQL exception during sequence query",
								JOptionPane.ERROR_MESSAGE);
						log.warn(sqle);
						serverWorking = false;
					}
					if (geneChromosomeMatchers == null)
						continue;

					for (GeneChromosomeMatcher o : geneChromosomeMatchers) {
						CSSequence seqs = SequenceFetcher.getSequences(o,
								startPoint, endPoint);
						if (seqs == null)
							continue;

						// set up label now only
						// marker.label + Real start point.
						// UCSC database start points are zero-based, end points are "1" based!
						// UCSC Genome Browser displayed values are all 1-based.
						// Correct start value.
						int startCoord = Integer.valueOf(o.getStartPoint()).intValue() + 1;
						if (o.isPositiveStrandDirection()) {
							seqs.setLabel(marker.getLabel() + "_" + geneName + "_" + o.getChr()
									+ "_" + startCoord); 
						} else {
							seqs.setLabel(marker.getLabel() + "_" + geneName + "_" + o.getChr()
									+ "_" + o.getEndPoint());
						}
						dnaSequenceDB.addASequence(seqs);
						RetrievedSequenceView retrievedSequenceView = new RetrievedSequenceView(
								seqs);
						retrievedSequenceView.setGeneChromosomeMatcher(o);
						retrievedDNAMap.put(seqs.toString(),
								retrievedSequenceView);
						if (retrievedDNASequences
								.containsKey(marker.toString())) {
							ArrayList<String> values = retrievedDNASequences
									.get(marker.toString());
							if (!values.contains(seqs.toString()))
								values.add(seqs.toString());
						} else {
							ArrayList<String> values = new ArrayList<String>();
							values.add(seqs.toString());
							retrievedDNASequences
									.put(marker.toString(), values);
						}
					} // end of loop of GeneChromosomeMatcher
				} // end of loop of gene name
			} // end of loop of markers

		return true;
	}

	/*
	 * * The caller already checked that selectedList if not null * background
	 * thread (non-EDT)
	 */
	private boolean getProteinSequences(Vector<DSGeneMarker> selectedList) {
		proteinSequenceDB = new CSSequenceSet<DSSequence>();

		try {

			for (int count = 0; count < selectedList.size(); count++) {
				DSGeneMarker geneMarker = (DSGeneMarker) selectedList
						.get(count);
				final String marker_id = geneMarker.getLabel();
				if (status.equalsIgnoreCase(STOP)) {
					return false;
				}
				
				final double progress = (double) count
						/ (double) (selectedList.size());
				updateProgressBarFromBackgroundThread(progress, "Retrieving "
						+ marker_id);

				CSSequenceSet<CSSequence> sequenceSet = SequenceFetcher
						.getAffyProteinSequences(marker_id);

				String[] uniprotids = AnnotationParser.getInfo(marker_id,
						AnnotationParser.SWISSPROT);

				ArrayList<String> values = new ArrayList<String>();
				int i = 0;
				for (Object o : sequenceSet) {
					retrievedProteinSequences
							.put(geneMarker.toString(), values);
					proteinSequenceDB.addASequence((CSSequence) o);
					RetrievedSequenceView retrievedSequenceView = new RetrievedSequenceView(
							(CSSequence) o);
					retrievedSequenceView.setUrl(uniprotids[i++].trim());
					retrievedProteinMap
							.put(o.toString(), retrievedSequenceView);
					proteinSequenceDB.parseMarkers();
					values.add(o.toString());
					retrievedProteinSequences
							.put(geneMarker.toString(), values);

				} // end of sequence loop
			} // end of for marker loop
		} catch (final RemoteException e) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(getComponent(),
								e.toString());
					}

				});
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e2) {
				e2.printStackTrace();
			}
			return false;
		}

		return true;
	}

	/* This method is meant to be invoked from background thread (not EDT) */
	/**
	 * return true if there is no error/exception
	 */
	boolean getSequences(Vector<DSGeneMarker> selectedList) {
		if (selectedList == null)
			return true;

		String sequenceType = (String) jComboCategory.getSelectedItem(); 
		if (sequenceType
				.equalsIgnoreCase(DNAVIEW)) {
			if (!getDnaSequences(selectedList))
				return false;
		} else {
			if (!getProteinSequences(selectedList))
				return false;
		}

		// post-processing
		final CSSequenceSet<DSSequence> sequenceDB;
		if (sequenceType.equalsIgnoreCase(DNAVIEW)) {
			sequenceDB = dnaSequenceDB;
			currentRetrievedMap = retrievedDNAMap;
			currentRetrievedSequences = retrievedDNASequences;
			if (cachedRetrievedDNAMap == null) {
				cachedRetrievedDNAMap = currentRetrievedMap;
				cachedRetrievedDNASequences = retrievedDNASequences;
			} else {
				cachedRetrievedDNAMap.putAll(currentRetrievedMap);
				cachedRetrievedDNASequences.putAll(currentRetrievedSequences);
			}
		} else {
			sequenceDB = proteinSequenceDB;
			currentRetrievedMap = retrievedProteinMap;
			currentRetrievedSequences = retrievedProteinSequences;
			if (cachedRetrievedProteinMap == null) {
				cachedRetrievedProteinMap = currentRetrievedMap;
				cachedRetrievedProteinSequences = retrievedDNASequences;
			} else {
				cachedRetrievedProteinMap.putAll(currentRetrievedMap);
				cachedRetrievedProteinSequences
						.putAll(currentRetrievedSequences);
			}
		}
		sequenceDB.parseMarkers();
		String fileName = this.getRandomFileName();
		if (sequenceDB.getSequenceNo() == 0) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(getComponent(),
								"No sequences retrieved for selected markers");
					}

				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		if (sequenceDB.getSequenceNo() != 0) {
			sequenceDB.writeToFile(fileName);

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					updateDisplay(sequenceDB, currentRetrievedMap);
				}
				
			});
		}

		return true;
	}

	private void updateDisplay(CSSequenceSet<DSSequence> selectedSet,
			HashMap<String, RetrievedSequenceView> newMap) {
		if (selectedSet == null || newMap == null || selectedSet.size() == 0
				|| newMap.size() == 0) {
			cleanUpCurrentView();
			return;
		}
		seqDisPanel.setSequenceDB(selectedSet);
		seqDisPanel.setRetrievedMap(newMap);
		seqDisPanel.initialize(selectedSet);
		main.revalidate();
		main.repaint();
	}

	@Publish
	public ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			org.geworkbench.events.ProjectNodeAddedEvent event) {
		return event;
	}

	/**
	 * receiveProjectSelection
	 * 
	 * @param e
	 *            ProjectEvent
	 */
	@SuppressWarnings("rawtypes")
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {

		log.debug("Source object " + source);

		if (e.getValue()==org.geworkbench.events.ProjectEvent.Message.CLEAR) {
			refMASet = null;
			cleanUpAll();

		} else {
			DSDataSet dataSet = e.getDataSet();
			if (dataSet instanceof DSMicroarraySet) {
				if (refMASet != dataSet) {
					this.refMASet = (DSMicroarraySet) dataSet;
					cleanUpAll();
				}
			}

		}
	}

	/**
	 * geneSelectorAction
	 * 
	 * @param e
	 *            GeneSelectorEvent
	 */
	@Subscribe
	public void receive(GeneSelectorEvent e, Object publisher) {
		log.debug("received GeneSelectorEvent::source="
				+ publisher.getClass().getName());
		final DSPanel<DSGeneMarker> markers = e.getPanel();
		
		if (markers == null)
			return;

		listModel.clear();

		if (markers.size() == 0) {
			cleanUpCurrentView();
			return;
		}
		
		final String sequenceType = (String) jComboCategory.getSelectedItem();
		
		// only non Swing/GUI action should be put in the background thread
		SwingWorker<Set<DSGeneMarker>, Void> worker = new SwingWorker<Set<DSGeneMarker>, Void>() {

			@Override
			protected Set<DSGeneMarker> doInBackground() throws Exception {
				Set<DSGeneMarker> markerSet = processGeneSelectorEvent(sequenceType, markers);
				return markerSet;
			}
			
			@Override
			public void done() {
				CSSequenceSet<DSSequence> sequenceDB;
				if (sequenceType.equalsIgnoreCase(DNAVIEW)) {
					sequenceDB = dnaSequenceDB;
				} else {
					sequenceDB = proteinSequenceDB;
				}
				updateDisplay(sequenceDB, currentRetrievedMap);

				try {
					Set<DSGeneMarker> markerSet = get();
					for(DSGeneMarker marker: markerSet) {
						listModel.addElement(marker);
					}
					if (markerSet.size()==0) {
						cleanUpCurrentView();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			
		};
		worker.execute();
	}

	/**
	 * background thread triggered by geneSelectorAction
	 * 
	 */
	private Set<DSGeneMarker> processGeneSelectorEvent(String sequenceType, final DSPanel<DSGeneMarker> markers) {

		CSSequenceSet<DSSequence> tempSequenceDB = new CSSequenceSet<DSSequence>();
		HashMap<String, RetrievedSequenceView> tempMap = new HashMap<String, RetrievedSequenceView>();
		TreeMap<String, ArrayList<String>> tempSequencesList = new TreeMap<String, ArrayList<String>>();
		Set<DSGeneMarker> set = new HashSet<DSGeneMarker>();
		for (int j = 0; j < markers.panels().size(); j++) {
			DSPanel<DSGeneMarker> mrk = markers.panels().get(j);
			if (mrk.isActive()) {
				for (int i = 0; i < mrk.size(); i++) {
					set.add(mrk.get(i));
				}
			}
		}

		// side-effect part
		for (Iterator<DSGeneMarker> markerIter = set.iterator(); markerIter
				.hasNext();) {
			DSGeneMarker mrk = markerIter.next();
			ArrayList<String> values = null;
			if (sequenceType.equalsIgnoreCase(DNAVIEW)) {
				values = cachedRetrievedDNASequences.get(mrk.toString());
			} else {
				values = cachedRetrievedProteinSequences.get(mrk.toString());
			}

			if (values != null) {
				for (String key : values) {
					RetrievedSequenceView retrievedSequenceView = currentRetrievedMap
							.get(key);

					if (sequenceType.equalsIgnoreCase(DNAVIEW)) {
						retrievedSequenceView = cachedRetrievedDNAMap.get(key);
					} else {
						retrievedSequenceView = cachedRetrievedProteinMap
								.get(key);
					}
					if (retrievedSequenceView != null) {
						DSSequence sequence = retrievedSequenceView
								.getSequence();
						if (sequence != null) {
							tempMap.put(key, retrievedSequenceView);
							tempSequenceDB.addASequence(sequence);
						}
					}

				}
				tempSequencesList.put(mrk.toString(), values);
			}
		}

		currentRetrievedMap = tempMap;
		currentRetrievedSequences = tempSequencesList;
		if (sequenceType.equalsIgnoreCase(DNAVIEW)) {
			dnaSequenceDB = tempSequenceDB;
		} else {
			proteinSequenceDB = tempSequenceDB;
		}

		return set;
	}

	/**
	 * getComponent
	 * 
	 * @return Component
	 */
	@Override
	public Component getComponent() {
		return main;
	}

	private String getRandomFileName() {
		String tempString = "temp" + RandomNumberGenerator.getID() + ".fasta";
		String tempFolder = FilePathnameUtils.getTemporaryFilesDirectoryPath();
		String fileName = tempFolder + tempString;
		return fileName;
	}

	// TODO this can be made less general
	void setDisplaySequenceDB(DSSequenceSet<?> displaySequenceDB) {
		seqDisPanel.setDisplaySequenceDB(displaySequenceDB);
	}

	private String generateHistStr() {
		String histStr = "Sequence Retriever\n"
			+ "get sequence parameters:\n"
			+ "Type Category: "	+ jComboCategory.getSelectedItem().toString() + "\n"
			+ "Source Category: " + jSourceCategory.getSelectedItem().toString() + "\n";
		if (((String) jComboCategory.getSelectedItem())
				.equalsIgnoreCase(DNAVIEW)) {
			histStr += "Start Point:"
					+ ((Integer) upstreamSpinnerModel.getNumber()).intValue() + "\n";
			histStr += "End Point:" + ((Integer) downstreamSpinnerModel.getNumber()).intValue()
					+ "\n";
			if (jSourceCategory.getSelectedItem().equals(UCSC)) // always true
				histStr += "Genome Assembly: "
						+ SequenceFetcher.getGenomeAssembly() + "\n";
		}

		histStr += "------------------------------------\n";
		return histStr;
	}

	public HashMap<String, RetrievedSequenceView> getCurrentRetrievedMap() {
		return currentRetrievedMap;
	}

	public DSItemList<DSGeneMarker> getMarkerList() {
		return refMASet.getMarkers();
	}

	public void disableStopButton() {
		stopButton.setEnabled(false);
	}

	// TODO check if this is really necessary
	public void updateSelectedListUI() {
		jSelectedList.updateUI();
	}

	public void resetProgressBar() {
		jProgressBar1.setIndeterminate(false);
		jProgressBar1.setMinimum(0);
		jProgressBar1.setMaximum(100);
		jProgressBar1.setStringPainted(true);
		updateProgressBar(0, "");
	}

}
