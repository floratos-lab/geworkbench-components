package org.geworkbench.components.idea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.MathException;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.IdeaEdge;
import org.geworkbench.bison.datastructure.bioobjects.IdeaProbeGene;
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
	private static final long serialVersionUID = 7928879302023716304L;

	private static Log log = LogFactory.getLog(IDEAAnalysis.class);

	private IDEAPanel IDEAAnalysisPanel = new IDEAPanel();

	public IDEAAnalysis() {
		setDefaultPanel(IDEAAnalysisPanel);
	}

	@Override
	public int getAnalysisType() {
		return AbstractAnalysis.IGNORE_TYPE;
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

		double pvalue;
		try{
			pvalue=Double.parseDouble(IDEAAnalysisPanel.getPvalue());
			if((pvalue<=0)||(pvalue>=1))
				return new AlgorithmExecutionResults(false,
						"P-value should be between 0 and 1.", null);
		}
		catch (Exception e){
			return new AlgorithmExecutionResults(false,
					"P-value is invalid.", null);
		}
		
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
		DSMicroarraySet<DSMicroarray> maSet = view.getMicroarraySet();
		if(maSet.getAnnotationFileName()==null){
			return new AlgorithmExecutionResults(false,
					"IDEA analysis needs annotation file. Please load it first.", null);
		}
		int numGenes = view.markers().size();

		ProgressBar pbIdea = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
		pbIdea.addObserver(this);
		pbIdea.setTitle("IDEA Analysis");
		pbIdea.setBounds(new ProgressBar.IncrementModel(0, numGenes, 0,
				numGenes, 1));

		pbIdea.setMessage("Calculating IDEA, please wait...");
		pbIdea.start();
		this.stopAlgorithm = false;

		ArrayList<IdeaEdge> edgeIndex = new ArrayList<IdeaEdge>();

		List<IdeaNetworkEdge> network = IDEAAnalysisPanel.getNetwork();
		if (network == null){			
			pbIdea.dispose();			
			return new AlgorithmExecutionResults(false,
					"network file is invalid.", null);
		}

		TreeSet<Gene> preGeneList = new TreeSet<Gene>();
		for (IdeaNetworkEdge edge : network) {
			preGeneList.add(new Gene(edge.geneId1));
			preGeneList.add(new Gene(edge.geneId2));
		}

		for (DSGeneMarker marker : maSet.getMarkers()) {
			int id = marker.getGeneId();

			for (Gene g : preGeneList) {
				if (g.getGeneNo() == id) {
					String s1 = g.getProbeIds() + marker.getLabel() + "\t";
					g.setProbeIds(s1);
					String s2 = g.getExpRows() + marker.getSerial() + "\t";
					g.setExpRows(s2);
					if (g.getMarkers() == null) {
						DSItemList<DSGeneMarker> geneMarkers = new CSItemList<DSGeneMarker>();
						geneMarkers.add(marker);
						g.setMarkers(geneMarkers);
					} else {
						DSItemList<DSGeneMarker> geneMarkers = g.getMarkers();
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

		for (IdeaNetworkEdge edge : network) {

			Gene gene1 = null;
			Gene gene2 = null;

			for (Gene g : preGeneList) {
				if (g.getGeneNo() == edge.geneId1) {
					gene1 = g; // gene1 points to preGeneList
				} else if (g.getGeneNo() == edge.geneId2) {
					gene2 = g;
				}
			}

			if ((gene1.getMarkers() != null) && (gene2.getMarkers() != null)) {
				for (DSGeneMarker marker1 : gene1.getMarkers()) {
					for (DSGeneMarker marker2 : gene2.getMarkers()) {
						IdeaEdge anEdge = new IdeaEdge(edge.geneId1,
								edge.geneId2, marker1, marker2,
								marker1.getSerial(), marker2.getSerial(),
								marker1.getLabel(), marker2.getLabel(),
								edge.interactionType);
						edgeIndex.add(anEdge);

						gene1.addEdge(anEdge);// add the edge to related
												// gene in preGeneList
						gene2.addEdge(anEdge);
					}
				}
			}
		}

		Phenotype phenotype = IDEAAnalysisPanel.getPhenotype();
		if (phenotype == null){			
			pbIdea.dispose();			
			return new AlgorithmExecutionResults(false,
					"pheno type file is invalid.", null);
		}


		int columnCountOverall = view.items().size(); 
		int columnCount = columnCountOverall - phenotype.getExcludedCount();

		// this 2-d array hold the expression values except those are excluded by phenotype file
		double[][] expressionData = new double[numGenes][columnCount];
		int columnIndex = 0;
		int columnIndexOverall = 0;
		while(columnIndexOverall<columnCountOverall) {
			if(!phenotype.isExcluded(columnIndexOverall)) {
				for (int i = 0; i < numGenes; i++) {
					expressionData[i][columnIndex] = view.getValue(i, columnIndexOverall);
				}
				columnIndex++;
			}
			columnIndexOverall++;
		}

		try {
			// ************Key process********************
			NullDistribution nullDist = new NullDistribution(edgeIndex,
					expressionData, IDEAAnalysisPanel.getUseNullData(),
					IDEAAnalysisPanel.getNullFileName(), columnCount, phenotype);
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
		} catch (Exception e) {
			e.printStackTrace();
			pbIdea.dispose();
			log.error(e);
			return new AlgorithmExecutionResults(false,
					"IDEA calculation failed due to Exception", null);
		} 
		
		
		if (this.stopAlgorithm) {
			pbIdea.dispose();
			return null;
		}

		List<IdeaEdge> locList = new ArrayList<IdeaEdge>();
		List<IdeaEdge> gocList = new ArrayList<IdeaEdge>();
		for (IdeaEdge anEdge : edgeIndex) {
			if (anEdge.isLoc())
				locList.add(anEdge);
			else if (anEdge.isGoc())
				gocList.add(anEdge);
		}

		Collections.sort(locList);

		Collections.sort(gocList, Collections.reverseOrder());

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

		TreeSet<IdeaProbeGene> probes = new TreeSet<IdeaProbeGene>();
		/*
		 * process probes, which is a alternative way to evaluate genes other
		 * than entrez genes which I call gene in this code
		 */
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

		int N = edgeIndex.size();
		FisherExact fe = new FisherExact(2 * N);

		int Sl = locList.size();
		int Sg = gocList.size();
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
		try{
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
		}
		catch(Exception e){
			e.printStackTrace();
			pbIdea.dispose();
			log.error(e);			
			return new AlgorithmExecutionResults(false,
					"IDEA calculation failed due to exception:" +
					"\nfinding significant genes using Fisher Exact Test.",
					null);
		}

		List<IdeaProbeGene> probeNes = new ArrayList<IdeaProbeGene>();
		for (IdeaProbeGene p : probes) {
			probeNes.add(p);
		}
		Collections.sort(probeNes, new IdeaProbeGene.NesComparator());

		if (this.stopAlgorithm) {
			pbIdea.dispose();
			return null;
		}
		
		IdeaResult analysisResult = new IdeaResult(maSet,
				"IDEA Analysis Result",locList, gocList, probeNes, pvalue);
		String stemp = generateHistoryString();
		ProjectPanel.addToHistory(analysisResult, stemp);

		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"Idea Analysis", analysisResult);
		pbIdea.dispose();
		return results;
	}

	private String generateHistoryString() {
		StringBuffer histStr = new StringBuffer();
		histStr.append(IDEAAnalysisPanel.getDataSetHistory());
		return histStr.toString();
	}
}
