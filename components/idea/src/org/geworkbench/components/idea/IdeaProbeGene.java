package org.geworkbench.components.idea;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.geworkbench.bison.datastructure.bioobjects.IdeaModule;
import org.geworkbench.bison.datastructure.bioobjects.IdeaNode;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;

/**
 * ProbeGene data structure. The key attribute is Probe Id.
 * 
 * @author zm2165
 * @version $Id$
 */
public class IdeaProbeGene implements Serializable, Comparable<IdeaProbeGene>{
	
	private static final long serialVersionUID = -8212763757638573012L;
	
	private final String probeId; // only identifier.
	
	private int locs = 0; // LOC edges of the gene
	private int gocs = 0; // GOC edges of the gene
	private List<IdeaEdge> edges = new ArrayList<IdeaEdge>();
	private double cumLoc=1;//default is big
	private double cumGoc=1;
	private double nes;

	private double locnes;
	private double gocnes;

	private int locHits;
	private int gocHits;

	// the only constructor
	public IdeaProbeGene(String probeId){
		this.probeId=probeId;
	}	
	
	// value set in setEdges
	public int getLocs() {
		return locs;
	}

	// value set in setEdges
	public int getGocs() {
		return gocs;
	}

	// TODO what is the difference between the two isSignificant? should they
	// always return the same result?
	public boolean isSignificant(double pvalue) {
		return cumLoc < pvalue || cumGoc < pvalue;
	}

	// TODO merge this with constructor if we can avoid constructor duplicates
	public void setEdges(HashSet<IdeaEdge> edgeIndex) {
		ArrayList<IdeaEdge> edges = new ArrayList<IdeaEdge>();
		for (IdeaEdge e : edgeIndex) {
			if ((probeId == e.getProbeId1()) || (probeId == e.getProbeId2()))
				edges.add(e);
		}
		this.edges = edges;
		
		// enrichment to find the significant probe
		locs = 0;
		gocs = 0;
		for (IdeaEdge anEdge : edges) {
			if (anEdge.isLoc()) {
				locs++;
			} else if (anEdge.isGoc()) {
				gocs++;
			}
		}

		locHits = 0;
		gocHits = 0;
		for (IdeaEdge e : edges) {
			if (e.getDeltaCorr() < 0)
				locHits++;
			else if (e.getDeltaCorr() > 0)
				gocHits++;
		}
	}

	public int getEdgeCount() {
		return edges.size();
	}

	@Override
	public int compareTo(IdeaProbeGene otherP) {
		return probeId.compareTo(otherP.probeId);
	}

	public void updateNes(double cumLoc, double cumGoc) {
		this.cumLoc = cumLoc;
		this.cumGoc = cumGoc;
		locnes = -Math.log( cumLoc );
		gocnes = -Math.log( cumGoc );
		nes = locnes + gocnes;
	}

	public static class NesComparator implements Comparator<IdeaProbeGene> {

		// descent order
		public int compare(IdeaProbeGene p1, IdeaProbeGene p2) {
			double d = p1.nes - p2.nes;
			if(d<0) return 1;
			else if (d>0) return -1;
			else return 0;
		}

	}

	public boolean isSignificant() {
		return locnes > 0 || gocnes > 0;
	}

	IdeaNode getIdeaNode(final DSItemList<DSGeneMarker> markers) {
		DSGeneMarker m = markers.get(probeId);
		return new IdeaNode(probeId, m.getGeneName(),
				"", // TODO ChrBand
				edges.size(), Math.abs(nes), locs, locHits, cumLoc, locnes,
				gocs, gocHits, cumGoc, gocnes);
	}
	
	List<IdeaModule> getModuleList() {
		List<IdeaModule> moduleResultList = new ArrayList<IdeaModule>();
		for (IdeaEdge e : edges) {
			String gLoc = "";
			String ppi = "";
			if (e.isLoc() || e.isGoc()) {
				if (e.isLoc())
					gLoc = "LoC";
				if (e.isGoc())
					gLoc = "GoC";
			} else
				gLoc = "None";
			if (e.getPpi() == IdeaEdge.InteractionType.PROTEIN_PROTEIN)
				ppi = "ppi";
			else if (e.getPpi() == IdeaEdge.InteractionType.PROTEIN_DNA)
				ppi = "pdi";

			IdeaModule aModule = new IdeaModule(e.getProbeId1(),
					e.getProbeId2(), ppi, gLoc);
			moduleResultList.add(aModule);
		}
		return moduleResultList;
	}
}
