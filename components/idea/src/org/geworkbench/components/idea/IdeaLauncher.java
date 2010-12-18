package org.geworkbench.components.idea;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.MathException;
import org.geworkbench.bison.datastructure.bioobjects.IdeaEdge;
import org.geworkbench.bison.datastructure.bioobjects.IdeaEdge.InteractionType;
import org.geworkbench.bison.datastructure.bioobjects.IdeaProbeGene;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

public class IdeaLauncher {
	private static Log log = LogFactory.getLog(IdeaLauncher.class);
	
	/**
	 * Stand alone version.
	 * 
	 * @author zm2165
	 * @version $Id$
	 * 
	 * @param args
	 * @throws MathException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static void main(String[] args) throws MathException, IOException,
			ClassNotFoundException {
		final double pvalue=0.05;
		String dir = System.getProperty("user.dir");

		// initialize input file names with default setting
		String networkFile = dir + "\\input\\network.txt"; // to be processed
															// file
		// prepare the expression data
		String expressionFile = dir
				+ "\\input\\bcell_mas5_254_filtered_classinfo.exp";
		String annotationFile = dir + "\\input\\HG_U95Av2.na23.annot.csv";
		// prepare the phenotype data
		String phenotypeFile = dir + "\\input\\myPhenotype.txt";

		if (args.length != 0 && args.length != 4) {
			System.out
					.println("Usage:\n"
							+ "java IdeaLauncher network_file expression_file annotation_file phenotype_file"
							+ "\nor: java IdeaLauncher");
			System.exit(0);
		} else if (args.length == 4) {
			networkFile = args[0];
			expressionFile = args[1];
			annotationFile = args[2];
			phenotypeFile = args[3];
		}

		double[][] expData = null;
		int expColLength = 0, expRowLength = 0;

		final int EXP_ROW_START = 40; // the first row in exp file to count for
										// gene expression data

		TreeSet<Gene> preGeneList = new TreeSet<Gene>();
		ArrayList<IdeaEdge> edgeIndex = new ArrayList<IdeaEdge>();

		FileReader prereader = new FileReader(networkFile);
		Scanner prein = new Scanner(prereader);
		while (prein.hasNextLine()) {
			String line = prein.nextLine();

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
		}// end of while

		File dataFile = new File(annotationFile);

		Map<String, String> probe_chromosomal =  AnnotationParser.processAnnotationData("other", dataFile, preGeneList);

		String expFirstCol = "";
		FileReader expreader = new FileReader(expressionFile);
		Scanner expin = new Scanner(expreader);

		while (expin.hasNextLine()) {
			String line = expin.nextLine();
			String[] tokens = line.split("\\s");
			expColLength = tokens.length;
			expFirstCol += tokens[0] + "\t";
			expRowLength++;
		}
		System.out.println(expFirstCol);

		Iterator<Gene> iter = preGeneList.iterator();
		while (iter.hasNext()) {
			Gene g = iter.next();

			String s = g.getProbeIds();
			String[] ids = s.split("\\s");
			if (ids.length > 0) {
				for (int i = 0; i < ids.length; i++) {
					String[] exps = expFirstCol.split("\\s");
					for (int j = EXP_ROW_START; j < exps.length; j++) {
						if (ids[i].equals(exps[j])) {
							String rows = g.getExpRows();
							rows += j + "\t";
							g.setExpRows(rows);
						}
					}
				}
			}
			// System.out.println(g.getGeneNo()+":"+g.getExpRows());
		}

		expData = new double[expRowLength][expColLength]; // expData saves the
															// whole exp file
															// except strings
															// which are set to
															// 0
		String[] expCol0 = new String[expRowLength]; // save the exp file
														// column0, because
														// those are probeId
														// names

		Map<String, String> probe_symbol = new HashMap<String, String>();

		expreader = new FileReader(expressionFile);
		expin = new Scanner(expreader);
		int row = 0;
		while (expin.hasNextLine()) {
			String line = expin.nextLine();
			String[] items = line.split("\\s");
			// expData[row]=new double[items.length];
			expCol0[row] = items[0];
			probe_symbol.put(items[0], items[1]);// the second column should be
													// gene symbol
			for (int i = 2; i < expColLength; i++) {
				int col = i-2;
				try {
					expData[row][col] = Double.parseDouble(items[i]);
				} catch (NumberFormatException e) {
					log.error(e);
				}
			}
			row++;
		}// while

		String outDir = dir + "\\output";

		File file = new File(outDir);
		boolean exists = file.exists();
		if (!exists) {
			(new File(outDir)).mkdir();
		}

		FileReader reader = new FileReader(networkFile);
		Scanner in = new Scanner(reader);// process network second time
		while (in.hasNextLine()) {
			String line = in.nextLine();

			int headLine = line.indexOf("Gene1");
			if (headLine == -1) {// there is no key word
				// System.out.println(line);

				IdeaNetworkEdge edge = new IdeaNetworkEdge(line);

				Gene gene1 = null;
				Gene gene2 = null;

				Iterator<Gene> anIter = preGeneList.iterator();
				while (anIter.hasNext()) {
					Gene g = anIter.next();
					if (g.getGeneNo() == edge.geneId1) {
						gene1 = g; // gene1 points to preGeneList
					} else if (g.getGeneNo() == edge.geneId2) {
						gene2 = g;
					}
				}

				String[] expRowsG1 = gene1.getExpRows().split("\\s");// expRowsG1
																		// expand
																		// entrez
																		// gene
																		// to
																		// rows
																		// in
																		// exp
																		// file
				String[] expRowsG2 = gene2.getExpRows().split("\\s");

				if ((expRowsG1.length > 0) && (gene1.getExpRows() != "")
						&& (expRowsG2.length > 0) && (gene2.getExpRows() != "")) {
					for (int i = 0; i < expRowsG1.length; i++) {
						for (int j = 0; j < expRowsG2.length; j++) {
							try {
								int rowG1 = Integer.parseInt(expRowsG1[i]); // the
																			// value-6
																			// equals
																			// the
																			// sample
																			// edgeIndex
																			// value
																			// of
																			// matlab
																			// code
								int rowG2 = Integer.parseInt(expRowsG2[j]);
								String probeId1 = expCol0[rowG1];
								String probeId2 = expCol0[rowG2];
								DSGeneMarker marker1 = null;
								DSGeneMarker marker2 = null;
								IdeaEdge anEdge = new IdeaEdge(edge.geneId1,
										edge.geneId2, marker1, marker2, rowG1,
										rowG2, probeId1, probeId2,
										edge.interactionType);// marker1,marker2
								// are no
								// use here,
								// just for
								// consistent
								// with
								// IDEAAnalysis
								edgeIndex.add(anEdge);// after calcu Null
														// distribution, if
														// there is a null file,
														// the edges in
														// edgeIndex will be
														// expired
								gene1.addEdge(anEdge);// add the edge to related
														// gene in preGeneList
								gene2.addEdge(anEdge);
							} catch (Exception e) {
								System.out
										.println("exption:processing gene-exp row Number!");
								e.printStackTrace();
							}
						}
					}
				}

			}
		}// end of while

		for (Gene g : preGeneList) {
			String geneEdges = "";
			for (IdeaEdge e : g.getEdges()) {
				geneEdges += e.getGeneNo1() + " " + e.getGeneNo2() + " "
						+ e.getExpRowNoG1() + " " + e.getExpRowNoG2() + "\n";
			}
			System.out.println(g.getGeneNo() + "\tprobeIds:\t"
					+ g.getProbeIds() + "\nedges:\t" + geneEdges);
		}
		System.out.println("total gene:" + preGeneList.size());

		Phenotype phenotype = new Phenotype(new File(phenotypeFile));
		int columnCount = expColLength - phenotype.getExcludedCount();

		boolean useExistNull = true;
		String nullFileName = dir + "\\null.dat";
		// ************Key process********************
		NullDistribution nullDist = new NullDistribution(edgeIndex, expData,
				useExistNull, nullFileName, columnCount, phenotype);
		nullDist.calcNullDist();
		edgeIndex = nullDist.getEdgeIndex();
		
		for (IdeaEdge anEdge : edgeIndex) {
			
			if (anEdge.getNormCorr() < pvalue / edgeIndex.size()) { // show
																	// significant
				// edges
				if (anEdge.getDeltaCorr() < 0)
					anEdge.setLoc(true);// save the flag for significant edge
				else if (anEdge.getDeltaCorr() > 0)
					anEdge.setGoc(true);				
			}
		}	

		// *******************************************
		String edgeStr = "";
		edgeStr += "Gene1\tGene2\texpRow1\texpRow2\tDeltaCorr\tNormCorr\tzDeltaCorr\tLoc\tGoc\n";
		String strLoc = "";
		String strGoc = "";
		for (IdeaEdge e : edgeIndex) {// for debug
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
					+ e.getzDeltaCorr() + "\t" + strLoc + "\t" + strGoc + "\n";
		}
		String fstr = dir + "\\output\\edgesReport.txt"; // expand edgeIndex
															// from network.txt
		PrintWriter out = new PrintWriter(fstr);
		out.println(edgeStr);
		out.close();

		List<IdeaEdge> locList = new ArrayList<IdeaEdge>();
		List<IdeaEdge> gocList = new ArrayList<IdeaEdge>();
		for (IdeaEdge anEdge : edgeIndex) {
			if (anEdge.isLoc())
				locList.add(anEdge);
			else if (anEdge.isGoc())
				gocList.add(anEdge);
		}
		Collections.sort(locList);
		edgeStr = "";
		// edgeStr+="LOC---------------\n";
		edgeStr += "Probe1\tGene1\tProbe2\tGene2\tMI\tDeltaMI\tNormDelta\tZ-score\n";
		for (IdeaEdge e : locList) {
			edgeStr += e.getProbeId1() + "\t"
					+ probe_symbol.get(e.getProbeId1()) + "\t"
					+ e.getProbeId2() + "\t"
					+ probe_symbol.get(e.getProbeId2()) + "\t" + e.getMI()
					+ "\t" + e.getDeltaCorr() + "\t" + e.getNormCorr() + "\t"
					+ e.getzDeltaCorr() + "\n";
		}
		fstr = dir + "\\output\\output1_loc.txt"; // expand edgeIndex from
													// network.txt
		out = new PrintWriter(fstr);
		out.println(edgeStr);
		out.close();

		Collections.sort(gocList, Collections.reverseOrder());
		edgeStr = "";
		// edgeStr+="GOC---------------\n";
		edgeStr += "Probe1\tGene1\tProbe2\tGene2\tMI\tDeltaMI\tNormDelta\tZ-score\n";
		for (IdeaEdge e : gocList) {
			edgeStr += e.getProbeId1() + "\t"
					+ probe_symbol.get(e.getProbeId1()) + "\t"
					+ e.getProbeId2() + "\t"
					+ probe_symbol.get(e.getProbeId2()) + "\t" + e.getMI()
					+ "\t" + e.getDeltaCorr() + "\t" + e.getNormCorr() + "\t"
					+ e.getzDeltaCorr() + "\n";
		}
		fstr = dir + "\\output\\output1_goc.txt"; // expand edgeIndex from
													// network.txt
		out = new PrintWriter(fstr);
		out.println(edgeStr);
		out.close();

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
		// which is a
		// alternative
		// way to
		// evaluate
		// genes other
		// than entrez
		// genes which I
		// call gene in
		// this code
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

			p.setLocs(locs);// divided by 2 because each egde has two genes, the
							// LOC were counted twice
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

			g.setLocs(locs);// divided by 2 because each egde has two genes, the
							// LOC were counted twice
			g.setGocs(gocs);
			allLoc += g.getLocs();
			allGoc += g.getGocs();
		}

		int allLoc2 = 0; // vv remove the following 10 lines should be removed
							// after test
		int allGoc2 = 0;
		for (IdeaEdge anEdge : edgeIndex) {
			if (anEdge.isLoc()) {
				allLoc2++;
				// System.out.println(anEdge.getGeneNo1()+"\t"+anEdge.getGeneNo2()+"\t"+(anEdge.getExpRowNoG1()-6)+"\t"
				// +(anEdge.getExpRowNoG2()-6)+"\tDeltaCorr:\t"+anEdge.getDeltaCorr()+"\tNormCorr:\t"+anEdge.getNormCorr()+"\tLOC:"+anEdge.getLoc()+"\tGOC:"+anEdge.getGoc());
			} else if (anEdge.isGoc())
				allGoc2++;
		}
		System.out.println("loc1:" + allLoc + "\tloc2:" + allLoc2);
		System.out.println("goc1:" + allGoc + "\tgoc2:" + allGoc2); // ^^remove

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
				double cumulativeP = fe.getCumlativeP(Dl, H - Dl, Sl - Dl, N
						- Sl - H + Dl);
				g.setCumLoc(cumulativeP);
				// System.out.println("*"+g.getGeneNo()+"\nLOC\tpValue:\t"+cumulativeP+"\n"+g.getProbeIds()+"\na:"+Dl+"\tb:"+(H-Dl)+"\tc:"+(Sl-Dl)+"\td:"+(N-Sl-H+Dl));
				// System.out.println();
			}
			if (g.getGocs() > 0) {
				int Dg = g.getGocs();
				double cumulativeP = fe.getCumlativeP(Dg, H - Dg, Sg - Dg, N
						- Sg - H + Dg);
				g.setCumGoc(cumulativeP);
				// System.out.println("*"+g.getGeneNo()+"\nGOC\tpValue:\t"+cumulativeP+"\n"+g.getProbeIds()+"\na:"+Dg+"\tb:"+(H-Dg)+"\tc:"+(Sg-Dg)+"\td:"+(N-Sg-H+Dg));
				// System.out.println();
			}
		}

		for (IdeaProbeGene p : probes) {
			int H = p.getEdges().size();
			FisherExact fe = new FisherExact(2 * edgeIndex.size());
			if (p.getLocs() > 0) { // calculate LOC p-value using fisher exact
									// test to evaluate probe
				int Dl = p.getLocs();
				double cumulativeP = fe.getCumlativeP(Dl, H - Dl, Sl - Dl, N
						- Sl - H + Dl);
				p.setCumLoc(cumulativeP);
				// System.out.println("*"+g.getGeneNo()+"\nLOC\tpValue:\t"+cumulativeP+"\n"+g.getProbeIds()+"\na:"+Dl+"\tb:"+(H-Dl)+"\tc:"+(Sl-Dl)+"\td:"+(N-Sl-H+Dl));
				// System.out.println();
			}
			if (p.getGocs() > 0) {
				int Dg = p.getGocs();
				double cumulativeP = fe.getCumlativeP(Dg, H - Dg, Sg - Dg, N
						- Sg - H + Dg);
				p.setCumGoc(cumulativeP);
				// System.out.println("*"+g.getGeneNo()+"\nGOC\tpValue:\t"+cumulativeP+"\n"+g.getProbeIds()+"\na:"+Dg+"\tb:"+(H-Dg)+"\tc:"+(Sg-Dg)+"\td:"+(N-Sg-H+Dg));
				// System.out.println();
			}
			double locnes = -Math.log(p.getCumLoc());
			double gocnes = -Math.log(p.getCumGoc());
			double nes = locnes + gocnes;
			p.setNes(nes);
		}

		List<IdeaProbeGene> probeNes = new ArrayList<IdeaProbeGene>();
		for (IdeaProbeGene p : probes) {
			probeNes.add(p);
		}
		Collections.sort(probeNes);

		String nodeStr = "";
		nodeStr += "Probe\tGene\tChrBand\tConn\tNes\tLoc\tLoCHits\tLoCEs\tLoCNes\tGoc\tGoCHits\tGoCEs\tGoCNes\n";
		for (IdeaProbeGene p : probeNes) {// present significant nodes
			int locHits = 0;
			int gocHits = 0;
			for (IdeaEdge e : p.getEdges()) {
				if (e.getDeltaCorr() < 0)
					locHits++;
				else if (e.getDeltaCorr() > 0)
					gocHits++;
			}
			double locnes = -Math.log(p.getCumLoc());
			double gocnes = -Math.log(p.getCumGoc());

			// if((p.getLocs()>0&&p.getCumLoc()<0.05)||(p.getGocs()>0&&p.getCumGoc()<0.05)){
			nodeStr += p.getProbeId() + "\t" + probe_symbol.get(p.getProbeId())
					+ "\t";
			nodeStr += probe_chromosomal.get(p.getProbeId()) + "\t"
					+ p.getEdges().size() + "\t" + p.getNes() + "\t"
					+ p.getLocs() + "\t" + locHits + "\t" + p.getCumLoc()
					+ "\t" + locnes + "\t" + p.getGocs() + "\t" + gocHits
					+ "\t" + p.getCumGoc() + "\t" + gocnes + "\n";
			// }
		}
		fstr = dir + "\\output\\output2.txt"; // expand edgeIndex from
												// network.txt
		out = new PrintWriter(fstr);
		out.println(nodeStr);
		out.close();

		nodeStr = "";
		nodeStr += "Gene1\tGene2\tconn_type\tLoc\tGoc\n";
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

					nodeStr += e.getProbeId1() + "\t" + e.getProbeId2() + "\t"
							+ ppi + "\t" + isLoc + "\t" + isGoc + "\n";
				}
				// System.out.println(nodeStr);

			}
		}
		fstr = dir + "\\output\\output3.txt"; // expand edgeIndex from
												// network.txt
		out = new PrintWriter(fstr);
		out.println(nodeStr);

		out.close();

		System.out.println("Done!");

	}// end of main
}
