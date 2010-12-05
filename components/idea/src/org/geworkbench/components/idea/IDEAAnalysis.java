package org.geworkbench.components.idea;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.MathException;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.IdeaEdge;
import org.geworkbench.bison.datastructure.bioobjects.IdeaProbeGene;
import org.geworkbench.bison.datastructure.bioobjects.IdeaResult;
import org.geworkbench.bison.datastructure.bioobjects.IdeaEdge.InteractionType;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.util.ProgressBar;

/**
 * IDEAAnalysis of IDEA analysis component
 * 
 * @author zm2165
 * @version $Id$
 */
public class IDEAAnalysis extends AbstractAnalysis implements
		ClusteringAnalysis {
	private static final long serialVersionUID = 7928879302023716304L;

	private static Log log = LogFactory.getLog(IDEAAnalysis.class);

	final static private String dir = "c:\\idea_test";

	private IDEAPanel IDEAAnalysisPanel = new IDEAPanel();
	final String PHENO_INCLUDE = "Include";
	final String PHENO_EXCLUDE = "Exclude";
	final int HEADCOL = 0;

	private int numGenes;

	public IDEAAnalysis() {
		setDefaultPanel(IDEAAnalysisPanel);
	}

	@Override
	public int getAnalysisType() {
		return AbstractAnalysis.MRA_TYPE;
	}

	@SuppressWarnings("unchecked")
	public AlgorithmExecutionResults execute(Object input) {

		// read input data, dataset view, dataset, etc.
		if (!(input instanceof DSMicroarraySetView)) {
			return new AlgorithmExecutionResults(false,
					"Input dataset for IDEA analysis should be a MicroarraySet.\n"
							+ "But you selected a "
							+ input.getClass().getName(), null);
		}
		;

		DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
		DSMicroarraySet<DSMicroarray> maSet = view.getMicroarraySet();
		numGenes = view.markers().size();

		ProgressBar pbIdea = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
		pbIdea.addObserver(this);
		pbIdea.setTitle("IDEA Analysis");
		pbIdea.setBounds(new ProgressBar.IncrementModel(0, numGenes, 0,
				numGenes, 1));

		pbIdea.setMessage("Calculating IDEA, please wait...");
		pbIdea.start();
		this.stopAlgorithm = false;

		double[][] expData = null;
		// Map<String,String> probe_symbol=new HashMap<String,String>();
		int expColLength = 0, expRowLength = 0;

		final String PHENO_INCLUDE = "Include";
		final String PHENO_EXCLUDE = "Exclude";
		TreeSet<Gene> preGeneList = new TreeSet<Gene>();
		ArrayList<IdeaEdge> edgeIndex = new ArrayList<IdeaEdge>();

		String network = IDEAAnalysisPanel.getNetwork().trim();
		String[] networkLines = network.split(",");
		for (String line : networkLines) {
			line = line.trim();
			int headLine = line.indexOf("Gene1");
			if (headLine == -1) {// there is no key word
				// System.out.println(line);
				try {
					String[] tokens = line.split("\\s");
					String first = tokens[0];
					String second = tokens[1];
					int geneNo1 = Integer.parseInt(first);
					int geneNo2 = Integer.parseInt(second);
					Gene gene1 = new Gene(geneNo1);
					Gene gene2 = new Gene(geneNo2);

					preGeneList.add(gene1);
					preGeneList.add(gene2);
				} catch (Exception e) {
					e.printStackTrace();
					pbIdea.dispose();
					log.error(e);
					return new AlgorithmExecutionResults(false,
							"network file is invalid.", null);
				}

			}
		}

		DSItemList markers = maSet.getMarkers();

		for (Object obj : markers) {
			DSGeneMarker marker = (DSGeneMarker) obj;
			int id = marker.getGeneId();

			for (Gene g : preGeneList) {
				if (g.getGeneNo() == id) {
					String s1 = g.getProbeIds() + marker.getLabel() + "\t";
					g.setProbeIds(s1);
					String s2 = g.getExpRows() + marker.getSerial() + "\t";
					g.setExpRows(s2);
					if (g.getMarkers() == null) {
						DSItemList geneMarkers = new CSItemList();
						geneMarkers.add(marker);
						g.setMarkers(geneMarkers);
					} else {
						DSItemList geneMarkers = g.getMarkers();
						geneMarkers.add(marker);
						g.setMarkers(geneMarkers);
					}
					break;
				}
			}
		}

		if (this.stopAlgorithm) {
			pbIdea.dispose();
			return null;
		}

		DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView = new CSMicroarraySetView(
				maSet);
		expColLength = maSetView.items().size();
		expRowLength = maSet.getMarkers().size();
		expData = new double[expRowLength][expColLength]; // expData saves the
															// whole exp file
															// except strings
															// which are set to
															// 0
		DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> data = (DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray>) input;
		for (int i = 0; i < expRowLength; i++) {
			for (int j = 0; j < expColLength; j++) {
				expData[i][j] = (float) data.getValue(i, j);
			}
		}

		String outDir = dir + "\\output";
		File file = new File(outDir);
		boolean exists = file.exists();
		if (!exists) {
			(new File(outDir)).mkdir();
		}

		for (String line : networkLines) {
			line = line.trim();
			int headLine = line.indexOf("Gene1");
			if (headLine == -1) {// there is no key word
				String[] tokens = line.split("\\s");
				String first = tokens[0];
				String second = tokens[1];

				int geneNo1 = Integer.parseInt(first);
				int geneNo2 = Integer.parseInt(second);
				InteractionType interactionType = stringToInteractionType(tokens[3]);

				Gene gene1 = null;
				Gene gene2 = null;

				for (Gene g : preGeneList) {
					if (g.getGeneNo() == geneNo1) {
						gene1 = g; // gene1 points to preGeneList
					} else if (g.getGeneNo() == geneNo2) {
						gene2 = g;
					}
				}

				if ((gene1.getMarkers() != null)
						&& (gene2.getMarkers() != null)) {
					DSItemList gList1 = gene1.getMarkers();
					for (Object obj1 : gList1) {
						DSGeneMarker marker1 = (DSGeneMarker) obj1;
						DSItemList gList2 = gene2.getMarkers();
						for (Object obj2 : gList2) {
							DSGeneMarker marker2 = (DSGeneMarker) obj2;
							IdeaEdge anEdge = new IdeaEdge(geneNo1, geneNo2,
									marker1, marker2, marker1.getSerial(),
									marker2.getSerial(), marker1.getLabel(),
									marker2.getLabel(), interactionType);
							edgeIndex.add(anEdge);

							gene1.addEdge(anEdge);// add the edge to related
													// gene in preGeneList
							gene2.addEdge(anEdge);
						}
					}
				}

			}// end of while
		}

		Phenotype phenoType = new Phenotype();
		String[] phenoLines = IDEAAnalysisPanel.getPhenotype().split(",");
		for (String line : phenoLines) {
			line = line.trim();
			String[] tokens = line.split("\\s");
			int phenoItemLength = tokens.length;
			if (line.indexOf(PHENO_INCLUDE) != -1) {
				int[] expCols = new int[phenoItemLength - 1];
				for (int i = 0; i < phenoItemLength - 1; i++) {
					expCols[i] = Integer.parseInt(tokens[i + 1]) - 1;
					/*
					 * because the exp columns in phenotype file is from 1,
					 * however they are from 2 in exp file, not fix the major
					 * part yet
					 */
				}
				phenoType.setExpCols(expCols);
			} else if (line.indexOf(PHENO_EXCLUDE) != -1) {
				int[] expExcludeCols = new int[phenoItemLength - 1];
				for (int i = 0; i < phenoItemLength - 1; i++) {
					expExcludeCols[i] = Integer.parseInt(tokens[i + 1]) - 1;// same
																			// as
																			// above,
				}
				phenoType.setExcludeCols(expExcludeCols);
			}

		}

		List<IdeaEdge> locList = null;
		List<IdeaEdge> gocList = null;
		List<IdeaProbeGene> probeNes = null;

		double[] x = new double[expColLength - HEADCOL
				- phenoType.getExcludeCols().length];
		double[] y = new double[expColLength - HEADCOL
				- phenoType.getExcludeCols().length];
		int[] t = new int[expColLength - HEADCOL
				- phenoType.getExcludeCols().length];
		int jj = 0;
		for (int i = 0; i < expColLength - HEADCOL; i++) {
			boolean exclude = false;
			for (int j = 0; j < phenoType.getExcludeCols().length; j++) {
				if (i == (phenoType.getExcludeCols()[j]))
					exclude = true;
			}
			if (!exclude) {
				t[jj] = i;
				jj++;
			}
		}
		phenoType.setAllExpCols(t);

		for (int i = 0; i < expColLength - HEADCOL
				- phenoType.getExcludeCols().length; i++) {
			x[i] = expData[7270 - 7][t[i] + HEADCOL];
			y[i] = expData[1567 - 7][t[i] + HEADCOL];
		}
		try {
			// ************Key process********************
			NullDistribution nullDist = new NullDistribution(edgeIndex,
					expData, phenoType, HEADCOL,
					IDEAAnalysisPanel.getUseNullData(),
					IDEAAnalysisPanel.getNullFileName());
			nullDist.calcNullDist();
			edgeIndex = nullDist.getEdgeIndex();
			// *******************************************

		} catch (MathException e) {
			e.printStackTrace();
			pbIdea.dispose();
			log.error(e);
			return new AlgorithmExecutionResults(false,
					"IDEA calculation failed due to MathException", null);
		} catch (IOException e) {
			e.printStackTrace();
			pbIdea.dispose();
			log.error(e);
			return new AlgorithmExecutionResults(false,
					"IDEA calculation failed due to IOException", null);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			pbIdea.dispose();
			log.error(e);
			return new AlgorithmExecutionResults(false,
					"IDEA calculation failed due to ClassNotFoundException",
					null);
		}
		if (this.stopAlgorithm) {
			pbIdea.dispose();
			return null;
		}

		locList = new ArrayList<IdeaEdge>();
		gocList = new ArrayList<IdeaEdge>();
		for (IdeaEdge anEdge : edgeIndex) {
			if (anEdge.isLoc())
				locList.add(anEdge);
			else if (anEdge.isGoc())
				gocList.add(anEdge);
		}

		Collections.sort(locList, new SortByZ());

		Collections.sort(gocList, new SortByZa());

		for (Gene g : preGeneList) {// edge in preGeneList need update from
									// edgeIndex, because edgeIndex may be
									// updated from null distribution
			ArrayList<IdeaEdge> edges = new ArrayList<IdeaEdge>();
			for (IdeaEdge anEdge : g.getEdges()) {
				for (IdeaEdge eInEdgeIndex : edgeIndex) {
					if ((eInEdgeIndex.compareTo(anEdge) == 0)
							&& (eInEdgeIndex.getGeneNo1() == g.getGeneNo())) {
						edges.add(eInEdgeIndex);
					}
				}
			}
			g.setEdges(edges);// replace the old edges

		}

		TreeSet<IdeaProbeGene> probes = new TreeSet<IdeaProbeGene>();// process
		// probes,
		// which is
		// a
		// alternative
		// way to
		// evaluate
		// genes
		// other
		// than
		// entrez
		// genes
		// which I
		// call gene
		// in this
		// code
		for (IdeaEdge e : edgeIndex) {
			IdeaProbeGene p1 = new IdeaProbeGene(e.getProbeId1());
			IdeaProbeGene p2 = new IdeaProbeGene(e.getProbeId2());
			probes.add(p1);
			probes.add(p2);
		}

		for (IdeaProbeGene p : probes) {
			// System.out.println(p.getProbeId());
			ArrayList<IdeaEdge> edges = new ArrayList<IdeaEdge>();
			for (IdeaEdge e : edgeIndex) {
				if ((p.getProbeId() == e.getProbeId1())
						|| (p.getProbeId() == e.getProbeId2()))
					edges.add(e);
			}
			p.setEdges(edges);
		}

		for (IdeaProbeGene p : probes) { // enrichment to find the significant
											// probe
			int locs = 0;
			int gocs = 0;
			for (IdeaEdge anEdge : p.getEdges()) {
				if (anEdge.isLoc()) {
					locs++;
				} else if (anEdge.isGoc()) {
					gocs++;
				}
			}

			p.setLocs(locs);
			p.setGocs(gocs);

		}

		int allLoc = 0;
		int allGoc = 0;
		for (Gene g : preGeneList) { // enrichment to find the significant
										// entrez genes
			int locs = 0;
			int gocs = 0;
			for (IdeaEdge anEdge : g.getEdges()) {
				if (anEdge.isLoc()) {
					locs++;
				} else if (anEdge.isGoc()) {
					gocs++;
				}
			}

			g.setLocs(locs);
			g.setGocs(gocs);
			allLoc += g.getLocs();
			allGoc += g.getGocs();
		}

		int N = edgeIndex.size();
		FisherExact fe = new FisherExact(2 * N);

		int Sl = allLoc;
		int Sg = allGoc;
		// calculate LOC p-value using fisher exact test to evaluate entrez
		// genes
		for (Gene g : preGeneList) {
			int H = g.getEdges().size();
			if (g.getLocs() > 0) {
				int Dl = g.getLocs();
				double cumulativeP = fe.getCumlativeP(Dl, H - Dl, Sl - Dl, N
						- Sl - H + Dl);
				g.setCumLoc(cumulativeP);
			}
			if (g.getGocs() > 0) {
				int Dg = g.getGocs();
				double cumulativeP = fe.getCumlativeP(Dg, H - Dg, Sg - Dg, N
						- Sg - H + Dg);
				g.setCumGoc(cumulativeP);
			}
		}

		for (IdeaProbeGene p : probes) {
			int H = p.getEdges().size();
			if (p.getLocs() > 0) { // calculate LOC p-value using fisher
									// exact test to evaluate probe
				int Dl = p.getLocs();
				double cumulativeP = fe.getCumlativeP(Dl, H - Dl, Sl - Dl, N
						- Sl - H + Dl);
				p.setCumLoc(cumulativeP);
			}
			if (p.getGocs() > 0) {
				int Dg = p.getGocs();
				double cumulativeP = fe.getCumlativeP(Dg, H - Dg, Sg - Dg, N
						- Sg - H + Dg);
				p.setCumGoc(cumulativeP);
			}
			double locnes = -Math.log(p.getCumLoc());
			double gocnes = -Math.log(p.getCumGoc());
			double nes = locnes + gocnes;
			p.setNes(nes);
		}

		probeNes = new ArrayList<IdeaProbeGene>();
		for (IdeaProbeGene p : probes) {
			probeNes.add(p);
		}
		Collections.sort(probeNes, new SortByNes());

		if (this.stopAlgorithm) {
			pbIdea.dispose();
			return null;
		}

		saveNodeInformationFile(dir + "\\output\\output3.txt", probes);

		IdeaResult analysisResult = new IdeaResult(maSet,
				"IDEA Analysis Result", locList, gocList, probeNes);
		String stemp = generateHistoryString();
		ProjectPanel.addToHistory(analysisResult, stemp);

		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"Idea Analysis", analysisResult);
		return results;
	}

	private void saveNodeInformationFile(String filename,
			TreeSet<IdeaProbeGene> probes) {
		String nodeStr = "";
		nodeStr += "Gene1\tGene2\tconn_type\tLoc\tGoc";
		for (IdeaProbeGene p : probes) {// present significant node with its
			// edges
			if ((p.getCumLoc() < 0.05) || (p.getCumGoc() < 0.05)) {
				// nodeStr+=p.getProbeId()+"\n";
				for (IdeaEdge e : p.getEdges()) {
					String isLoc = "";
					String isGoc = "";
					String ppi = "";
					if (e.isLoc())
						isLoc = "X";
					if (e.isGoc())
						isGoc = "X";
					if (e.getPpi() == InteractionType.PROTEIN_PROTEIN)
						ppi = "ppi";
					else if (e.getPpi() == InteractionType.PROTEIN_DNA)
						ppi = "pdi";

					nodeStr += "\n" + e.getProbeId1() + "\t" + e.getProbeId2()
							+ "\t" + ppi + "\t" + isLoc + "\t" + isGoc;
				}
			}
		}

		PrintWriter out = null;
		try {
			out = new PrintWriter(filename);
			out.println(nodeStr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (out != null)
				out.close();
		}

	}

	private String generateHistoryString() {
		StringBuffer histStr = new StringBuffer();
		histStr.append(IDEAAnalysisPanel.getDataSetHistory());
		return histStr.toString();
	}

	static InteractionType stringToInteractionType(String str) {
		int ppiId = Integer.parseInt(str);

		InteractionType interactionType = null;
		if (ppiId == 0)
			interactionType = InteractionType.PROTEIN_DNA;
		else if (ppiId == 1)
			interactionType = InteractionType.PROTEIN_PROTEIN;

		return interactionType;
	}

}
