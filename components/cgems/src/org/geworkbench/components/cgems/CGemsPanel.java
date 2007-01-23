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
import org.geworkbench.events.AnnotationsEvent;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.ProgressBar;
import org.jfree.ui.SortableTable;
import org.jfree.ui.SortableTableModel;

/**
 * 
 * @author keshav
 * @version $Id: CGemsPanel.java,v 1.6 2007-01-23 18:32:20 keshav Exp $
 */
@AcceptTypes( { DSMicroarraySet.class })
public class CGemsPanel implements VisualPlugin {
	static Log log = LogFactory.getLog(CGemsPanel.class);

	private final String HEADER_SNP = "SNP Id";
	private final String HEADER_FINDING = "Association Finding Id";
	private final String HEADER_ANALYSIS = "Analysis";
	private final String HEADER_GENE = "Gene Symbol";
	private final String HEADER_MARKER = "Marker";
	private final String HEADER_RANK = "Rank";
	private final String HEADER_PVAL = "P-Value";

	private final int COL_MARKER = 0;
	public final int COL_GENE = 1;
	public final int COL_FINDING = 2;
	private final int COL_RANK = 3;
	public final int COL_ANALYSIS = 4;
	public final int COL_SNP = 5;
	public final int COL_PVAL = 6;

	/**
	 * 
	 * @author keshav
	 * 
	 */
	private class TableModel extends SortableTableModel {

		private SnpAssociationFindingData[] snpAssociationFindingData;

		private SnpData[] snpData;

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
		public TableModel(SnpData[] snpData,
				SnpAssociationFindingData[] snpAssociationFindingData,
				AnalysisData[] analysisData, GeneData[] geneData,
				MarkerData[] markerData, RankData[] rankData,
				PValData[] pvalData) {
			this.snpData = snpData;
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
			this.snpData = new SnpData[0];
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
			return 7;
		}

		/**
		 * 
		 * @param s
		 * @return String
		 */
		private String wrapInHTML(String s) {
			return "<html><a href=\"__noop\">" + s + "</a></html>";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case COL_SNP:
				return snpData[indices[rowIndex]].name;

			case COL_FINDING:
				return snpAssociationFindingData[indices[rowIndex]].name;

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
		public void sortByColumn(final int column, final boolean ascending) {
			resetIndices();
			final Comparable[][] columns = { snpData,
					snpAssociationFindingData, analysisData, geneData,
					markerData, rankData, pvalData };
			Comparator<Integer> comparator = new Comparator<Integer>() {
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
			case COL_SNP:
				SnpData snp = snpData[indices[rowIndex]];
				// activateSnp(snp); TODO add for snp
				break;
			case COL_FINDING:
				SnpAssociationFindingData snpFinding = snpAssociationFindingData[indices[rowIndex]];
				// activateSnp(snp); TODO add for snp
				break;
			case COL_ANALYSIS:
				AnalysisData analysis = analysisData[indices[rowIndex]];
				// activateAnalysis(analysis);TODO add for analysis
				break;
			case COL_GENE:
				GeneData gene = geneData[indices[rowIndex]];
				// activateGene(gene);TODO add for gene
				break;
			case COL_MARKER:
				MarkerData marker = markerData[indices[rowIndex]];
				// activateMarker(marker);TODO add for marker
				break;
			case COL_RANK:
				RankData rank = rankData[indices[rowIndex]];
				// activateMarker(marker);TODO add for marker
				break;
			case COL_PVAL:
				PValData pval = pvalData[indices[rowIndex]];
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
	private static class SnpData implements Comparable {

		public String name;

		public SnpData(String name) {
			this.name = name;
		}

		public int compareTo(Object o) {
			if (o instanceof SnpData) {
				return name.compareTo(((SnpData) o).name);
			}
			return -1;
		}
	}

	/**
	 * 
	 * @author keshav
	 * 
	 */
	private static class SnpAssociationFindingData implements Comparable {

		public String name;

		public SNPAnnotation snpAnnotation;

		public SnpAssociationFindingData(String name,
				SNPAnnotation snpAnnotation) {
			this.name = name;
			this.snpAnnotation = snpAnnotation;
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
	private static class AnalysisData implements Comparable {

		public String name;

		public SNPAssociationAnalysis snpAssociationAnalysis;

		public AnalysisData(String name,
				SNPAssociationAnalysis snpAssociationAnalysis) {
			this.name = name;
			this.snpAssociationAnalysis = snpAssociationAnalysis;
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
	private static class GeneData implements Comparable {

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
	private static class MarkerData implements Comparable {

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
	private static class RankData implements Comparable {

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
	private static class PValData implements Comparable {

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
		annotationsPanel.setLayout(borderLayout1);
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
		clearButton.setToolTipText("");
		clearButton.setFocusPainted(true);
		clearButton.setText("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearButton_actionPerformed(e);
			}
		});
		annotationsPanel.add(jScrollPane1, BorderLayout.CENTER);
		annotationsPanel.add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.add(showPanels);
		buttonPanel.add(clearButton);
		model = new TableModel();
		table = new SortableTable(model);
		table.getColumnModel().getColumn(COL_SNP).setHeaderValue(HEADER_SNP);
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
					if ((column == COL_SNP) || (column == COL_FINDING)
							|| (column == COL_ANALYSIS) || (column == COL_GENE)
							|| (column == COL_MARKER) || (column == COL_RANK)
							|| (column == COL_PVAL)) {
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
		return annotationsPanel;
	}

	/**
	 * 
	 * 
	 */
	public void searchSNPAssociationFinding() {
		// TODO keshav - remove me ... this is just a test
		Collection geneBiomarkerCollection = new ArrayList();
		GeneBiomarker wt1 = new GeneBiomarker();
		wt1.setHugoGeneSymbol("WT1");
		geneBiomarkerCollection.add(wt1);

		SNPAnnotation snpAnnotation = new SNPAnnotation();
		snpAnnotation.setGeneBiomarkerCollection(geneBiomarkerCollection);
		try {
			System.out
					.println("______________________________________________________________________");
			System.out.println("Retrieving all SNPAssiciationFindings for WT1");
			ApplicationService appService = ApplicationServiceProvider
					.getApplicationService();

			List resultList = appService.search(SNPAssociationFinding.class,
					snpAnnotation);
			if (resultList != null) {
				System.out.println("Number of results returned: "
						+ resultList.size());
				System.out.println("DbsnpId" + "\t" + "ChromosomeName" + "\t"
						+ "ChromosomeLocation" + "\t" + "GenomeBuild" + "\t"
						+ "ReferenceSequence" + "\t" + "ReferenceStrand" + "\t"
						+ "GeneBiomarker(s)" + "\t" + "Analysis Name" + "\t"
						+ "p-Value" + "\t" + "rank" + "\n");
				for (Iterator resultsIterator = resultList.iterator(); resultsIterator
						.hasNext();) {
					SNPAssociationFinding returnedObj = (SNPAssociationFinding) resultsIterator
							.next();
					System.out.println(returnedObj.getSnpAnnotation()
							.getDbsnpId()
							+ "\t"
							+ returnedObj.getSnpAnnotation()
									.getChromosomeName()
							+ "\t"
							+ returnedObj.getSnpAnnotation()
									.getChromosomeLocation()
							+ "\t"
							+ pipeGeneBiomarkers(returnedObj.getSnpAnnotation()
									.getGeneBiomarkerCollection())
							+ "\t"
							+ returnedObj.getSnpAssociationAnalysis().getName()
							+ "\t"
							+ returnedObj.getPvalue()
							+ "\t"
							+ returnedObj.getRank() + "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

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
				public void run() {
					ProgressBar pb = ProgressBar
							.create(ProgressBar.INDETERMINATE_TYPE);
					pb.setMessage("Connecting to server...");

					ArrayList<SnpData> snpData = new ArrayList<SnpData>();
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

							List resultList = null;
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
								for (Iterator resultsIterator = resultList
										.iterator(); resultsIterator.hasNext();) {
									SNPAssociationFinding returnedObj = (SNPAssociationFinding) resultsIterator
											.next();

									/* SnpData */
									SnpData snp = new SnpData(returnedObj
											.getSnpAnnotation().getId());
									snpData.add(snp);

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
										if (geneBioMarkerCol.size() > 1) {
											entrezIds = entrezIds + "|"
													+ entrezId;
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
								snpData.add(new SnpData(""));
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

					SnpData[] snps = snpData.toArray(new SnpData[0]);
					SnpAssociationFindingData[] snpFindings = snpAssociationFindingData
							.toArray(new SnpAssociationFindingData[0]);
					AnalysisData[] analyses = analysisData
							.toArray(new AnalysisData[0]);
					GeneData[] genes = geneData.toArray(new GeneData[0]);
					MarkerData[] markers = markerData
							.toArray(new MarkerData[0]);
					RankData[] ranks = rankData.toArray(new RankData[0]);
					PValData[] pvals = pvalData.toArray(new PValData[0]);

					model = new TableModel(snps, snpFindings, analyses, genes,
							markers, ranks, pvals);

					table.setSortableModel(model);
					table.getColumnModel().getColumn(COL_SNP).setHeaderValue(
							HEADER_SNP);
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
	 * @param geneBiomarkerCollection
	 * @return String
	 */
	public static String pipeGeneBiomarkers(Collection geneBiomarkerCollection) {
		String geneList = "";
		if (geneBiomarkerCollection != null) {
			for (Object object : geneBiomarkerCollection) {
				GeneBiomarker geneBiomarker = (GeneBiomarker) object;
				geneList = geneList + geneBiomarker.getHugoGeneSymbol() + "|";
			}
			// remove Last |
			if (geneList.endsWith("|")) {
				geneList = geneList.substring(0, geneList.lastIndexOf("|"));
			}
		}
		return geneList;
	}

	/**
	 * 
	 * @param e
	 */
	private void clearButton_actionPerformed(ActionEvent e) {
		table.setSortableModel(new TableModel());
		table.getColumnModel().getColumn(COL_SNP).setHeaderValue(HEADER_SNP);
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
			JOptionPane.showMessageDialog(annotationsPanel,
					"Must activate marker panels to retrieve SNP data.");
		}
		showSnpData();
		// searchSNPAssociationFinding();
	}

	@Publish
	public org.geworkbench.events.SubpanelChangedEvent publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent event) {
		return event;
	}

	@Publish
	public AnnotationsEvent publishAnnotationsEvent(AnnotationsEvent ae) {
		return ae;
	}

	@Publish
	public MarkerSelectedEvent publishMarkerSelectedEvent(
			MarkerSelectedEvent event) {
		return event;
	}

	/**
	 * The Visual Component on which the annotation results are shown
	 */
	private JPanel annotationsPanel = new JPanel();

	/**
	 * Visual Widget
	 */
	private JScrollPane jScrollPane1 = new JScrollPane();

	/**
	 * Visual Widget
	 */
	private BorderLayout borderLayout1 = new BorderLayout();

	/**
	 * Visual Widget
	 */
	private SortableTable table;

	private TableModel model;

	/**
	 * Visual Widget
	 */
	private JPanel buttonPanel = new JPanel();

	/**
	 * Visual Widget
	 */
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
			maView.useMarkerPanel(true);
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
		DSDataSet data = e.getDataSet();
		if (data != null && data instanceof DSMicroarraySet) {
			maSet = (DSMicroarraySet) data;
		}
	}
}
