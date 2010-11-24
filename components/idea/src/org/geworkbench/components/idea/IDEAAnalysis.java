package org.geworkbench.components.idea;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.math.MathException;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.IdeaResult;
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
	/**
	 * 
	 */
	private static final long serialVersionUID = 7928879302023716304L;

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
		ArrayList<Edge> edgeIndex = new ArrayList<Edge>();
		String dir = "c:\\idea_test";

		String network = IDEAAnalysisPanel.getNetwork().trim();
		String[] networkLines = network.split(",");
		for (String line : networkLines) {
			line = line.trim();
			int headLine = line.indexOf("Gene1");
			if (headLine == -1) {// there is no key word
				// System.out.println(line);

				String[] tokens = line.split("\\s");
				String first = tokens[0];
				String second = tokens[1];
				try {
					int geneNo1 = Integer.parseInt(first);
					int geneNo2 = Integer.parseInt(second);
					Gene gene1 = new Gene(geneNo1);
					Gene gene2 = new Gene(geneNo2);

					preGeneList.add(gene1);
					preGeneList.add(gene2);
				} catch (Exception e) {
					e.printStackTrace();
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

		try {
			String outDir = dir + "\\output";
			File file = new File(outDir);
			boolean exists = file.exists();
			if (!exists) {
				(new File(outDir)).mkdir();
			}
			String midLog = dir + "\\output\\myEdgeIndex.txt"; // expand
																// edgeIndex
																// from
																// network.txt
			PrintWriter midOut = new PrintWriter(midLog);

			for (String line : networkLines) {
				line = line.trim();
				int headLine = line.indexOf("Gene1");
				if (headLine == -1) {// there is no key word
					// System.out.println(line);
					String[] tokens = line.split("\\s");
					String first = tokens[0];
					String second = tokens[1];
					String forth = tokens[3]; // not defined clearly yet,
												// direction?transitional
												// factor?

					int geneNo1 = Integer.parseInt(first);
					int geneNo2 = Integer.parseInt(second);
					int ppi = Integer.parseInt(forth);
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
								Edge anEdge = new Edge(geneNo1, geneNo2,
										marker1, marker2, marker1.getSerial(),
										marker2.getSerial(),
										marker1.getLabel(), marker2.getLabel(),
										ppi);
								edgeIndex.add(anEdge);
								midOut.println(geneNo1 + "\t" + geneNo2 + "\t"
										+ marker1.getSerial() + "\t"
										+ marker2.getSerial() + "\t" + ppi);
								gene1.addEdge(anEdge);// add the edge to related
														// gene in preGeneList
								gene2.addEdge(anEdge);
							}
						}
					}

				}// end of while
			}
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
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
					expCols[i] = Integer.parseInt(tokens[i + 1]) - 1; // because
																		// the
																		// exp
																		// columns
																		// in
																		// phenotype
																		// file
																		// is
																		// from
																		// 1,
																		// however
																		// they
																		// are
																		// from
																		// 2 in
																		// exp
																		// file,
																		// not
																		// fix
																		// the
																		// major
																		// part
																		// yet
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

		Object[][] output1_loc = null;
		Object[][] output1_goc = null;
		Object[][] output2 = null;

		try {

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
			MutualInfo mutual;
			try {
				mutual = new MutualInfo(x, y);
				double mi = mutual.getMI();
				System.out.println("first MI is " + mi);

				// ************Key process********************
				NullDistribution nullDist = new NullDistribution(preGeneList,
						edgeIndex, expData, phenoType, HEADCOL);
				nullDist.calcNullDist();
				edgeIndex = nullDist.getEdgeIndex();
				// *******************************************

			} catch (MathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (this.stopAlgorithm) {
				pbIdea.dispose();
				return null;
			}

			String edgeStr = "";
			edgeStr += "Gene1\tGene2\texpRow1\texpRow2\tDeltaCorr\tNormCorr\tzDeltaCorr\tLoc\tGoc\n";
			String strLoc = "";
			String strGoc = "";
			for (Edge e : edgeIndex) {// for debug
				if (e.isLoc())
					strLoc = "T";
				else
					strLoc = "";
				if ((e.isGoc()))
					strGoc = "T";
				else
					strGoc = "";
				edgeStr += e.getGeneNo1() + "\t" + e.getGeneNo2() + "\t"
						+ e.getExpRowNoG1() + "\t" + e.getExpRowNoG2() + "\t"
						+ e.getDeltaCorr() + "\t" + e.getNormCorr() + "\t"
						+ e.getzDeltaCorr() + "\t" + strLoc + "\t" + strGoc
						+ "\n";
			}
			String fstr = dir + "\\output\\edgesReport.txt"; // expand edgeIndex
																// from
																// network.txt
			PrintWriter out = new PrintWriter(fstr);
			out.println(edgeStr);
			out.close();

			List<Edge> locList = new ArrayList<Edge>();
			List<Edge> gocList = new ArrayList<Edge>();
			for (Edge anEdge : edgeIndex) {
				if (anEdge.isLoc())
					locList.add(anEdge);
				else if (anEdge.isGoc())
					gocList.add(anEdge);
			}

			output1_loc = new Object[locList.size()][8];
			output1_goc = new Object[gocList.size()][8];
			
			Collections.sort(locList, new SortByZ());
			edgeStr = "";
			// edgeStr+="LOC---------------\n";
			edgeStr += "Probe1\tGene1\tProbe2\tGene2\tMI\tDeltaMI\tNormDelta\tZ-score";
			int output1Row = 0;
			for (Edge e : locList) {
				edgeStr += "\n" + e.getProbeId1() + "\t"
						+ e.getMarker1().getGeneName() + "\t" + e.getProbeId2()
						+ "\t" + e.getMarker2().getGeneName() + "\t"
						+ e.getMI() + "\t" + e.getDeltaCorr() + "\t"
						+ e.getNormCorr() + "\t" + e.getzDeltaCorr();
				
				output1_loc[output1Row][0] = e.getProbeId1();
				output1_loc[output1Row][1] = e.getMarker1().getGeneName();
				output1_loc[output1Row][2] = e.getProbeId2();
				output1_loc[output1Row][3] = e.getMarker2().getGeneName();
				output1_loc[output1Row][4] = e.getMI();
				output1_loc[output1Row][5] = e.getDeltaCorr();
				output1_loc[output1Row][6] = e.getNormCorr();
				output1_loc[output1Row][7] = e.getzDeltaCorr();

				output1Row++;
			}
			fstr = dir + "\\output\\output1_loc.txt"; // expand edgeIndex from
														// network.txt
			out = new PrintWriter(fstr);
			out.println(edgeStr);
			out.close();

			Collections.sort(gocList, new SortByZa());
			edgeStr = "";
			// edgeStr+="GOC---------------\n";
			edgeStr += "Probe1\tGene1\tProbe2\tGene2\tMI\tDeltaMI\tNormDelta\tZ-score";
			int gocRow = 0;
			for (Edge e : gocList) {
				edgeStr += "\n" + e.getProbeId1() + "\t"
						+ e.getMarker1().getGeneName() + "\t" + e.getProbeId2()
						+ "\t" + e.getMarker2().getGeneName() + "\t"
						+ e.getMI() + "\t" + e.getDeltaCorr() + "\t"
						+ e.getNormCorr() + "\t" + e.getzDeltaCorr();
				
				output1_goc[gocRow][0] = e.getProbeId1();
				output1_goc[gocRow][1] = e.getMarker1().getGeneName();
				output1_goc[gocRow][2] = e.getProbeId2();
				output1_goc[gocRow][3] = e.getMarker2().getGeneName();
				output1_goc[gocRow][4] = e.getMI();
				output1_goc[gocRow][5] = e.getDeltaCorr();
				output1_goc[gocRow][6] = e.getNormCorr();
				output1_goc[gocRow][7] = e.getzDeltaCorr();
				
				gocRow++;
			}
			fstr = dir + "\\output\\output1_goc.txt"; // expand edgeIndex from
														// network.txt
			out = new PrintWriter(fstr);
			out.println(edgeStr);
			out.close();

			for (Gene g : preGeneList) {// edge in preGeneList need update from
										// edgeIndex, because edgeIndex may be
										// updated from null distribution
				ArrayList<Edge> edges = new ArrayList<Edge>();
				for (Edge anEdge : g.getEdges()) {
					for (Edge eInEdgeIndex : edgeIndex) {
						if ((eInEdgeIndex.compareTo(anEdge) == 0)
								&& (eInEdgeIndex.getGeneNo1() == g.getGeneNo())) {
							edges.add(eInEdgeIndex);
						}
					}
				}
				g.setEdges(edges);// replace the old edges

			}

			TreeSet<ProbeGene> probes = new TreeSet<ProbeGene>();// process
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
			for (Edge e : edgeIndex) {
				ProbeGene p1 = new ProbeGene(e.getProbeId1());
				ProbeGene p2 = new ProbeGene(e.getProbeId2());
				probes.add(p1);
				probes.add(p2);
			}

			for (ProbeGene p : probes) {
				// System.out.println(p.getProbeId());
				ArrayList<Edge> edges = new ArrayList<Edge>();
				for (Edge e : edgeIndex) {
					if ((p.getProbeId() == e.getProbeId1())
							|| (p.getProbeId() == e.getProbeId2()))
						edges.add(e);
				}
				p.setEdges(edges);
			}

			for (ProbeGene p : probes) { // enrichment to find the significant
											// probe
				int locs = 0;
				int gocs = 0;
				for (Edge anEdge : p.getEdges()) {
					if (anEdge.isLoc()) {
						locs++;
					} else if (anEdge.isGoc()) {
						gocs++;
					}
				}

				p.setLocs(locs);// divided by 2 because each egde has two genes,
								// the LOC were counted twice
				p.setGocs(gocs);

			}

			int allLoc = 0;
			int allGoc = 0;
			for (Gene g : preGeneList) { // enrichment to find the significant
											// entrez genes
				int locs = 0;
				int gocs = 0;
				for (Edge anEdge : g.getEdges()) {
					if (anEdge.isLoc()) {
						locs++;
					} else if (anEdge.isGoc()) {
						gocs++;
					}
				}

				g.setLocs(locs);// divided by 2 because each egde has two genes,
								// the LOC were counted twice
				g.setGocs(gocs);
				allLoc += g.getLocs();
				allGoc += g.getGocs();
			}

			int allLoc2 = 0; // vv remove the following 10 lines should be
								// removed after test
			int allGoc2 = 0;
			for (Edge anEdge : edgeIndex) {
				if (anEdge.isLoc()) {
					allLoc2++;
				} else if (anEdge.isGoc())
					allGoc2++;
			}

			int N = edgeIndex.size();
			int Sl = allLoc;
			int Sg = allGoc;
			// calculate LOC p-value using fisher exact test to evaluate entrez
			// genes
			for (Gene g : preGeneList) {
				int H = g.getEdges().size();
				FisherExact fe = new FisherExact(2 * edgeIndex.size());
				if (g.getLocs() > 0) {
					int Dl = g.getLocs();
					double cumulativeP = fe.getCumlativeP(Dl, H - Dl, Sl - Dl,
							N - Sl - H + Dl);
					g.setCumLoc(cumulativeP);
				}
				if (g.getGocs() > 0) {
					int Dg = g.getGocs();
					double cumulativeP = fe.getCumlativeP(Dg, H - Dg, Sg - Dg,
							N - Sg - H + Dg);
					g.setCumGoc(cumulativeP);
				}
			}

			for (ProbeGene p : probes) {
				int H = p.getEdges().size();
				FisherExact fe = new FisherExact(2 * edgeIndex.size());
				if (p.getLocs() > 0) { // calculate LOC p-value using fisher
										// exact test to evaluate probe
					int Dl = p.getLocs();
					double cumulativeP = fe.getCumlativeP(Dl, H - Dl, Sl - Dl,
							N - Sl - H + Dl);
					p.setCumLoc(cumulativeP);
				}
				if (p.getGocs() > 0) {
					int Dg = p.getGocs();
					double cumulativeP = fe.getCumlativeP(Dg, H - Dg, Sg - Dg,
							N - Sg - H + Dg);
					p.setCumGoc(cumulativeP);
				}
				double locnes = -Math.log(p.getCumLoc());
				double gocnes = -Math.log(p.getCumGoc());
				double nes = locnes + gocnes;
				p.setNes(nes);
			}

			List<ProbeGene> probeNes = new ArrayList<ProbeGene>();
			for (ProbeGene p : probes) {
				probeNes.add(p);
			}
			Collections.sort(probeNes, new SortByNes());

			output2 = new Object[probeNes.size()][13];

			String nodeStr = "";
			nodeStr += "Probe\tGene\tChrBand\tConn\tNes\tLoc\tLoCHits\tLoCEs\tLoCNes\tGoc\tGoCHits\tGoCEs\tGoCNes";
			int row = 0;
			for (ProbeGene p : probeNes) {// present significant nodes
				int locHits = 0;
				int gocHits = 0;
				for (Edge e : p.getEdges()) {
					if (e.getDeltaCorr() < 0)
						locHits++;
					else if (e.getDeltaCorr() > 0)
						gocHits++;
				}
				double locnes = -Math.log(p.getCumLoc());
				double gocnes = -Math.log(p.getCumGoc());

				DSGeneMarker m = maSet.getMarkers().get(p.getProbeId());

				nodeStr += "\n" + p.getProbeId() + "\t" + m.getGeneName()
						+ "\t";
				nodeStr += "chromosomal" + "\t" + p.getEdges().size() + "\t"
						+ p.getNes() + "\t" + p.getLocs() + "\t" + locHits
						+ "\t" + p.getCumLoc() + "\t" + locnes + "\t"
						+ p.getGocs() + "\t" + gocHits + "\t" + p.getCumGoc()
						+ "\t" + gocnes;

				output2[row][0] = p.getProbeId();
				output2[row][1] = m.getGeneName();
				output2[row][2] = "chromosomal";
				output2[row][3] = p.getEdges().size();
				output2[row][4] = p.getNes();
				output2[row][5] = p.getLocs();
				output2[row][6] = locHits;
				output2[row][7] = p.getCumLoc();
				output2[row][8] = locnes;
				output2[row][9] = p.getGocs();
				output2[row][10] = gocHits;
				output2[row][11] = p.getCumGoc();
				output2[row][12] = gocnes;
				row++;
			}
			fstr = dir + "\\output\\output2.txt"; // expand edgeIndex from
													// network.txt
			out = new PrintWriter(fstr);
			out.println(nodeStr);
			out.close();
			if (this.stopAlgorithm) {
				pbIdea.dispose();
				return null;
			}

			nodeStr = "";
			nodeStr += "Gene1\tGene2\tconn_type\tLoc\tGoc";
			for (ProbeGene p : probes) {// present significant node with its
										// edges
				if ((p.getCumLoc() < 0.05) || (p.getCumGoc() < 0.05)) {
					// nodeStr+=p.getProbeId()+"\n";
					for (Edge e : p.getEdges()) {
						String isLoc = "";
						String isGoc = "";
						String ppi = "";
						if (e.isLoc())
							isLoc = "X";
						if (e.isGoc())
							isGoc = "X";
						if (e.getPpi() == 1)
							ppi = "ppi";
						else
							ppi = "pdi";

						nodeStr += "\n" + e.getProbeId1() + "\t"
								+ e.getProbeId2() + "\t" + ppi + "\t" + isLoc
								+ "\t" + isGoc;
					}
				}
			}
			fstr = dir + "\\output\\output3.txt"; // expand edgeIndex from
													// network.txt
			out = new PrintWriter(fstr);
			out.println(nodeStr);

			out.close();

			System.out.println("Done!");

		} catch (java.io.IOException e) {
			e.printStackTrace();
		}

		pbIdea.dispose();

		int itemp = 10;
		IdeaResult analysisResult = new IdeaResult(maSet,
				"IDEA Analysis Result", output1_loc, output1_goc, output2);
		String stemp = generateHistoryString(itemp);
		ProjectPanel.addToHistory(analysisResult, stemp);

		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"Idea Analysis", analysisResult);
		return results;
	}

	private String generateHistoryString(int resultSize) {
		StringBuffer histStr = new StringBuffer();
		histStr.append(IDEAAnalysisPanel.getDataSetHistory());
		return histStr.toString();
	}

}
