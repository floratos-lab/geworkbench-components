package org.geworkbench.components.cgems;

import gov.nih.nci.caintegrator.domain.analysis.snp.SNPAssociationAnalysis;
import gov.nih.nci.caintegrator.domain.analysis.snp.SNPAssociationFinding;
import gov.nih.nci.caintegrator.domain.annotation.gene.GeneBiomarker;
import gov.nih.nci.caintegrator.domain.annotation.snp.SNPAnnotation;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.ApplicationService;
import gov.nih.nci.system.applicationservice.ApplicationServiceProvider;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.BrowserLauncher;
import org.geworkbench.util.ProgressBar;
import org.jfree.ui.SortableTable;
import org.jfree.ui.SortableTableModel;

/**
 * 
 * @author keshav
 * @version $Id$
 */
@AcceptTypes( { DSMicroarraySet.class })
public class CGemsPanel implements VisualPlugin {
	static Log log = LogFactory.getLog(CGemsPanel.class);

	private final String HEADER_FINDING = "SNP Id";
	private final String HEADER_ANALYSIS = "Analysis";
	private final String HEADER_GENE = "Gene Symbol";
	private final String HEADER_MARKER = "Marker";
	private final String HEADER_RANK = "Rank";
	private final String HEADER_PVAL = "P-Value";

	private final int COL_MARKER = 0;
	private final int COL_GENE = 1;
	private final int COL_FINDING = 2;
	private final int COL_RANK = 3;
	private final int COL_PVAL = 4;
	private final int COL_ANALYSIS = 5;

	private final int COL_COUNT = 6;

	private final String HTML_PREFIX = "<html><a href=\"";
	private final String HTML_SUFFIX = "</a></html>";
	private final String SNP_URL = "http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?searchType=adhoc_search&type=rs&rs=";

	/**
	 * 
	 * @author keshav
	 * 
	 */
	private class TableModel extends SortableTableModel {

		private static final long serialVersionUID = 5161675173055622999L;

		private SnpAssociationFindingData[] snpAssociationFindingData;

		private AnalysisData[] analysisData;

		private GeneData[] geneData;

		private MarkerData[] markerData;

		private RankData[] rankData;

		private PValData[] pvalData;

		private Integer[] indices;

		private int size;

		/**
		 * 
		 * @param snpAssociationFindingData
		 * @param analysisData
		 * @param geneData
		 * @param markerData
		 */
		public TableModel(
				SnpAssociationFindingData[] snpAssociationFindingData,
				AnalysisData[] analysisData, GeneData[] geneData,
				MarkerData[] markerData, RankData[] rankData,
				PValData[] pvalData) {
			this.snpAssociationFindingData = snpAssociationFindingData;
			this.analysisData = analysisData;
			this.geneData = geneData;
			this.markerData = markerData;
			this.rankData = rankData;
			this.pvalData = pvalData;
			size = snpAssociationFindingData.length;
			indices = new Integer[size];
			resetIndices();
		}

		/**
		 * 
		 * 
		 */
		public TableModel() {
			this.snpAssociationFindingData = new SnpAssociationFindingData[0];
			this.analysisData = new AnalysisData[0];
			this.geneData = new GeneData[0];
			this.markerData = new MarkerData[0];
			this.rankData = new RankData[0];
			this.pvalData = new PValData[0];
			size = 0;
			indices = new Integer[0];
		}

		/*
		 * 
		 */
		private void resetIndices() {
			for (int i = 0; i < size; i++) {
				indices[i] = i;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount() {
			return size;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return COL_COUNT;
		}

		/**
		 * Adds html to the given snp id.
		 * 
		 * @param s
		 * @return String
		 */
		private String addHtmlToSnp(String s) {
			if (s.contains("http"))
				throw new RuntimeException(
						"Do not pass a url to this method.  The url gets formed here.");

			return HTML_PREFIX + SNP_URL + s + "\">" + s + HTML_SUFFIX;
		}

		/**
		 * Removes html from the String.
		 * 
		 * @param s
		 * @return
		 */
		private String removeHtml(String s) {

			String url = null;
			String wrappedUrl = s;
			url = StringUtils.substringBetween(wrappedUrl, HTML_PREFIX, "\">");

			return url;
		}

		/**
		 * Launches the given url in a browser.
		 * 
		 */
		private void launchInBrowser(String url) {
			if (!url.startsWith("http"))
				throw new RuntimeException("Invalid url: " + url);

			try {

				BrowserLauncher.openURL(url);
			} catch (IOException e) {
				log.error("Error removing html from " + url + "."
						+ "Exception is: ");
				e.printStackTrace();
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {

			case COL_FINDING:
				return addHtmlToSnp(snpAssociationFindingData[indices[rowIndex]].name);

			case COL_ANALYSIS:
				return analysisData[indices[rowIndex]].name;

			case COL_GENE:
				return geneData[indices[rowIndex]].name;

			case COL_MARKER:
				return markerData[indices[rowIndex]].name;

			case COL_RANK:
				return rankData[indices[rowIndex]].rank;

			case COL_PVAL:
				return pvalData[indices[rowIndex]].pval;
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jfree.ui.SortableTableModel#sortByColumn(int, boolean)
		 */
		@SuppressWarnings("rawtypes")
		public void sortByColumn(final int column, final boolean ascending) {
			resetIndices();
			final Comparable[][] columns = { markerData, geneData,
					snpAssociationFindingData, rankData, pvalData, analysisData };
			Comparator<Integer> comparator = new Comparator<Integer>() {
				@SuppressWarnings("unchecked")
				public int compare(Integer i, Integer j) {
					if (ascending) {
						return columns[column][i].compareTo(columns[column][j]);
					} else {
						return columns[column][j].compareTo(columns[column][i]);
					}
				}
			};
			Arrays.sort(indices, comparator);
			super.sortByColumn(column, ascending);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jfree.ui.SortableTableModel#isSortable(int)
		 */
		public boolean isSortable(int i) {
			return true;
		}

		/**
		 * 
		 * @param rowIndex
		 * @param columnIndex
		 */
		public void activateCell(int rowIndex, int columnIndex) {
			switch (columnIndex) {

			case COL_FINDING:
				String url = removeHtml((String) getValueAt(rowIndex,
						columnIndex));
				launchInBrowser(url);
				break;
			case COL_ANALYSIS:
				// activateAnalysis(analysis);TODO add for analysis
				break;
			case COL_GENE:
				// activateGene(gene);TODO add for gene
				break;
			case COL_MARKER:
				// activateMarker(marker);TODO add for marker
				break;
			case COL_RANK:
				// activateMarker(marker);TODO add for marker
				break;
			case COL_PVAL:
				// activateMarker(marker);TODO add for marker
				break;
			}
		}
	}

	/**
	 * 
	 * @author keshav
	 * 
	 */
	private static class SnpAssociationFindingData implements Comparable<Object> {

		public String name;

		public SnpAssociationFindingData(String name,
				SNPAnnotation snpAnnotation) {
			this.name = name;
		}

		public int compareTo(Object o) {
			if (o instanceof SnpAssociationFindingData) {
				return name.compareTo(((SnpAssociationFindingData) o).name);
			}
			return -1;
		}
	}

	/**
	 * 
	 * @author keshav
	 * 
	 */
	private static class AnalysisData implements Comparable<Object> {

		public String name;

		public AnalysisData(String name,
				SNPAssociationAnalysis snpAssociationAnalysis) {
			this.name = name;
		}

		public int compareTo(Object o) {
			if (o instanceof AnalysisData) {
				return name.compareTo(((AnalysisData) o).name);
			}
			return -1;
		}
	}

	/**
	 * 
	 * @author keshav
	 * 
	 */
	private static class GeneData implements Comparable<Object> {

		public String name;

		public GeneData(String name) {
			this.name = name;
		}

		public int compareTo(Object o) {
			if (o instanceof GeneData) {
				return name.compareTo(((GeneData) o).name);
			}
			return -1;
		}
	}

	/**
	 * 
	 * @author keshav
	 * 
	 */
	private static class MarkerData implements Comparable<Object> {

		public String name;

		public MarkerData(String name) {
			this.name = name;
		}

		public int compareTo(Object o) {
			if (o instanceof MarkerData) {
				return name.compareTo(((MarkerData) o).name);
			}
			return -1;
		}
	}

	/**
	 * 
	 * @author keshav
	 * 
	 */
	private static class RankData implements Comparable<Object> {

		public Integer rank;

		public RankData(Integer rank) {
			this.rank = rank;
		}

		public int compareTo(Object o) {
			if (o instanceof RankData) {
				return rank.compareTo(((RankData) o).rank);
			}
			return -1;
		}
	}

	/**
	 * 
	 * @author keshav
	 * 
	 */
	private static class PValData implements Comparable<Object> {

		public Float pval;

		public PValData(Float pval) {
			this.pval = pval;
		}

		public int compareTo(Object o) {
			if (o instanceof PValData) {
				return pval.compareTo(((PValData) o).pval);
			}
			return -1;
		}
	}

	/**
	 * 
	 * 
	 */
	public CGemsPanel() {
		try {
			jbInit();
		}

		catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Configures the Graphical User Interface and Listeners
	 * 
	 * @throws Exception
	 */
	private void jbInit() throws Exception {
		cgemsPanel.setLayout(borderLayout1);
		showPanels.setHorizontalAlignment(SwingConstants.CENTER);
		showPanels.setText("Retrieve SNP Data");
		showPanels
				.setToolTipText("Retrieve SNP information for markers in activated panels");
		showPanels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showPanels_actionPerformed(e);
			}

		});
		clearButton.setForeground(Color.black);
		clearButton.setFocusPainted(true);
		clearButton.setText("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearButton_actionPerformed(e);
			}
		});
		cgemsPanel.add(jScrollPane1, BorderLayout.CENTER);
		cgemsPanel.add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.add(showPanels);
		buttonPanel.add(clearButton);
		model = new TableModel();
		table = new SortableTable(model);
		table.getColumnModel().getColumn(COL_FINDING).setHeaderValue(
				HEADER_FINDING);
		table.getColumnModel().getColumn(COL_ANALYSIS).setHeaderValue(
				HEADER_ANALYSIS);
		table.getColumnModel().getColumn(COL_GENE).setHeaderValue(HEADER_GENE);
		table.getColumnModel().getColumn(COL_MARKER).setHeaderValue(
				HEADER_MARKER);
		table.getColumnModel().getColumn(COL_RANK).setHeaderValue(HEADER_RANK);
		table.getColumnModel().getColumn(COL_PVAL).setHeaderValue(HEADER_PVAL);
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int column = table.columnAtPoint(e.getPoint());
				int row = table.rowAtPoint(e.getPoint());
				if ((column >= 0) && (row >= 0)) {
					model.activateCell(row, column);
				}
			}
		});
		table.addMouseMotionListener(new MouseMotionAdapter() {
			private boolean isHand = false;

			public void mouseMoved(MouseEvent e) {
				int column = table.columnAtPoint(e.getPoint());
				int row = table.rowAtPoint(e.getPoint());
				if ((column >= 0) && (row >= 0)) {
					if ((column == COL_FINDING) || (column == COL_ANALYSIS)
							|| (column == COL_GENE) || (column == COL_MARKER)
							|| (column == COL_RANK) || (column == COL_PVAL)) {
						if (!isHand) {
							isHand = true;
							table.setCursor(new Cursor(Cursor.HAND_CURSOR));
						}
					} else {
						if (isHand) {
							isHand = false;
							table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						}
					}
				}
			}
		});
		jScrollPane1.getViewport().add(table, null);
	}

	/**
	 * Interface <code>VisualPlugin</code> method that returns a
	 * <code>Component</code> which is the visual representation of the this
	 * plugin.
	 * 
	 * @return <code>Component</code> visual representation of
	 *         <code>AnnotationsPanel</code>
	 */
	public Component getComponent() {
		return cgemsPanel;
	}

	/**
	 * query caIntegrator
	 */
	private void showSnpData() {

		if (appService == null) {
			try {
				appService = ApplicationServiceProvider.getApplicationService();
			} catch (Exception e) {
				log.error("Could not create ApplicationService in "
						+ this.getClass().getName() + ".  Exception is: ");
				e.printStackTrace();
			}
		}
		try {
			Runnable query = new Runnable() {
				@SuppressWarnings("unchecked")
				public void run() {
					ProgressBar pb = ProgressBar
							.create(ProgressBar.INDETERMINATE_TYPE);
					pb.setMessage("Connecting to server...");

					ArrayList<SnpAssociationFindingData> snpAssociationFindingData = new ArrayList<SnpAssociationFindingData>();
					ArrayList<AnalysisData> analysisData = new ArrayList<AnalysisData>();
					ArrayList<GeneData> geneData = new ArrayList<GeneData>();
					ArrayList<MarkerData> markerData = new ArrayList<MarkerData>();
					ArrayList<RankData> rankData = new ArrayList<RankData>();
					ArrayList<PValData> pvalData = new ArrayList<PValData>();
					if (selectedMarkerInfo != null) {
						pb.setTitle("Querying caIntegrator..");
						pb.start();
						for (int i = 0; i < selectedMarkerInfo.size(); i++) {
							String geneName = selectedMarkerInfo.get(i)
									.getGeneName();
							String probeLabel = selectedMarkerInfo.get(i)
									.getLabel();

							String labelToUse = null;
							if ("".equals(geneName)
									|| geneName.equals(probeLabel)) {
								labelToUse = probeLabel;
							} else {
								labelToUse = geneName;
							}

							/*
							 * Construct geneBiomarker to query caIntegrator. To
							 * do this query, the geneBiomarker must be part of
							 * a collection so for each marker, create a unary
							 * collection
							 */
							GeneBiomarker geneBiomarker = createGeneBioMarker(labelToUse);
							ArrayList<GeneBiomarker> geneBiomarkerCollection = new ArrayList<GeneBiomarker>();
							geneBiomarkerCollection.add(geneBiomarker);
							SNPAnnotation snpAnnotation = new SNPAnnotation();
							snpAnnotation
									.setGeneBiomarkerCollection(geneBiomarkerCollection);

							List<?> resultList = null;
							try {
								resultList = appService.search(
										SNPAssociationFinding.class,
										snpAnnotation);
							} catch (ApplicationException e) {
								e.printStackTrace();
							}

							pb
									.setMessage("Getting SNP Association Findings for : "
											+ selectedMarkerInfo.get(i)
													.getLabel());

							/* extract the results from the resultList */
							if (resultList != null) {
								for (Iterator<?> resultsIterator = resultList
										.iterator(); resultsIterator.hasNext();) {
									SNPAssociationFinding returnedObj = (SNPAssociationFinding) resultsIterator
											.next();

									/* SnpAssociationFindingData */
									SnpAssociationFindingData snpFinding = new SnpAssociationFindingData(
											returnedObj.getSnpAnnotation()
													.getDbsnpId(), returnedObj
													.getSnpAnnotation());
									snpAssociationFindingData.add(snpFinding);

									/* AnalysisData */
									AnalysisData analysis = new AnalysisData(
											returnedObj
													.getSnpAssociationAnalysis()
													.getName(),
											returnedObj
													.getSnpAssociationAnalysis());
									analysisData.add(analysis);

									/* GeneData */
									String entrezIds = null;
									Collection<GeneBiomarker> geneBioMarkerCol = returnedObj
											.getSnpAnnotation()
											.getGeneBiomarkerCollection();
									for (GeneBiomarker gbm : geneBioMarkerCol) {
										String entrezId = gbm
												.getHugoGeneSymbol();
										if (geneBioMarkerCol.size() > 1
												&& StringUtils
														.isNotEmpty(entrezIds)) {
											if (StringUtils
													.isNotEmpty(entrezId)) {
												entrezIds = entrezIds + ", "
														+ entrezId;
											}
										} else {
											entrezIds = entrezId;
										}
									}
									GeneData gene = new GeneData(entrezIds);
									geneData.add(gene);

									/* Marker Data */
									MarkerData marker = new MarkerData(
											selectedMarkerInfo.get(i)
													.getLabel());
									markerData.add(marker);

									/* Rank Data */
									RankData rank = new RankData(returnedObj
											.getRank());
									rankData.add(rank);

									/* PVal Data */
									PValData pval = new PValData(returnedObj
											.getPvalue());
									pvalData.add(pval);
								}
							}

							else {
								snpAssociationFindingData
										.add(new SnpAssociationFindingData("",
												null));
								analysisData.add(new AnalysisData("", null));
								geneData.add(new GeneData(""));
								markerData.add(new MarkerData(""));
								rankData.add(new RankData(null));
								pvalData.add(new PValData(null));
							}
						}
						pb.stop();
						pb.dispose();
					}

					SnpAssociationFindingData[] snpFindings = snpAssociationFindingData
							.toArray(new SnpAssociationFindingData[0]);
					AnalysisData[] analyses = analysisData
							.toArray(new AnalysisData[0]);
					GeneData[] genes = geneData.toArray(new GeneData[0]);
					MarkerData[] markers = markerData
							.toArray(new MarkerData[0]);
					RankData[] ranks = rankData.toArray(new RankData[0]);
					PValData[] pvals = pvalData.toArray(new PValData[0]);

					model = new TableModel(snpFindings, analyses, genes,
							markers, ranks, pvals);

					table.setSortableModel(model);

					table.getColumnModel().getColumn(COL_FINDING)
							.setHeaderValue(HEADER_FINDING);
					table.getColumnModel().getColumn(COL_ANALYSIS)
							.setHeaderValue(HEADER_ANALYSIS);
					table.getColumnModel().getColumn(COL_GENE).setHeaderValue(
							HEADER_GENE);
					table.getColumnModel().getColumn(COL_MARKER)
							.setHeaderValue(HEADER_MARKER);
					table.getColumnModel().getColumn(COL_RANK).setHeaderValue(
							HEADER_RANK);
					table.getColumnModel().getColumn(COL_PVAL).setHeaderValue(
							HEADER_PVAL);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							table.getTableHeader().repaint();
						}
					});
				}
			};
			Thread t = new Thread(query);
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param geneName
	 * @return GeneBiomarker
	 */
	private GeneBiomarker createGeneBioMarker(String geneName) {
		GeneBiomarker geneBiomarker = new GeneBiomarker();
		geneBiomarker.setHugoGeneSymbol(geneName);
		return geneBiomarker;
	}

	/**
	 * 
	 * @param e
	 */
	private void clearButton_actionPerformed(ActionEvent e) {
		table.setSortableModel(new TableModel());

		table.getColumnModel().getColumn(COL_FINDING).setHeaderValue(
				HEADER_FINDING);
		table.getColumnModel().getColumn(COL_ANALYSIS).setHeaderValue(
				HEADER_ANALYSIS);
		table.getColumnModel().getColumn(COL_GENE).setHeaderValue(HEADER_GENE);
		table.getColumnModel().getColumn(COL_MARKER).setHeaderValue(
				HEADER_MARKER);
		table.getColumnModel().getColumn(COL_RANK).setHeaderValue(HEADER_RANK);
		table.getColumnModel().getColumn(COL_PVAL).setHeaderValue(HEADER_PVAL);

		table.getTableHeader().revalidate();
	}

	/**
	 * 
	 * @param e
	 */
	private void showPanels_actionPerformed(ActionEvent e) {
		if (selectedMarkerInfo == null || selectedMarkerInfo.size() == 0) {
			JOptionPane.showMessageDialog(cgemsPanel,
					"Must activate marker panels to retrieve SNP data.");
		}
		showSnpData();
		// searchSNPAssociationFinding();
	}

	@Publish
	public MarkerSelectedEvent publishMarkerSelectedEvent(
			MarkerSelectedEvent event) {
		return event;
	}

	/* Panel on which to view the snp data. */
	private JPanel cgemsPanel = new JPanel();

	/* Visual Widgets */
	private JScrollPane jScrollPane1 = new JScrollPane();
	private BorderLayout borderLayout1 = new BorderLayout();
	private SortableTable table;
	private TableModel model;
	private JPanel buttonPanel = new JPanel();
	private JButton showPanels = new JButton();

	private DSItemList<DSGeneMarker> selectedMarkerInfo = null;

	private ApplicationService appService = null;

	private DSMicroarraySet maSet = null;

	JButton clearButton = new JButton();

	/**
	 * geneSelectorAction
	 * 
	 * @param e
	 * @param source
	 */
	@Subscribe
	public void receive(GeneSelectorEvent e, Object source) {
		if (maSet != null && e.getPanel() != null) {
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(
					maSet);
			maView.setMarkerPanel(e.getPanel());
			if (maView.getMarkerPanel().activeSubset().size() == 0) {
				selectedMarkerInfo = new CSItemList<DSGeneMarker>();
			} else {
				selectedMarkerInfo = maView.getUniqueMarkers();
			}
		}
	}

	/**
	 * receiveProjectSelection
	 * 
	 * @param e
	 * @param source
	 */
	@Subscribe
	public void receive(ProjectEvent e, Object source) {
		DSDataSet<?> data = e.getDataSet();
		if (data != null && data instanceof DSMicroarraySet) {
			maSet = (DSMicroarraySet) data;
		}
	}
}
