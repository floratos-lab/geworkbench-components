package org.geworkbench.components.idea;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * ProbeGene data structure. The key attribute is Probe Id.
 * 
 * @author zm2165
 * @version $id$
 */
public class ProbeGene implements Serializable, Comparable<ProbeGene>{
	
	private static final long serialVersionUID = -8212763757638573012L;
	
	private String probeId;
	private int locs=0;		//LOC edges of the gene
	private int gocs=0;		//GOC edges of the gene
	private ArrayList<Edge> edges=new ArrayList<Edge>();
	private double cumLoc=1;//default is big
	private double cumGoc=1;
	private double nes;
	
	public ProbeGene(String probeId){
		this.probeId=probeId;
	}	
	
	public String getProbeId() {
		return probeId;
	}

	public void setLocs(int locs) {
		this.locs = locs;
	}

	public int getLocs() {
		return locs;
	}

	public void setGocs(int gocs) {
		this.gocs = gocs;
	}

	public int getGocs() {
		return gocs;
	}

	public void setCumLoc(double cumLoc) {
		this.cumLoc = cumLoc;
	}

	public double getCumLoc() {
		return cumLoc;
	}

	public void setCumGoc(double cumGoc) {
		this.cumGoc = cumGoc;
	}

	public double getCumGoc() {
		return cumGoc;
	}

	public void setEdges(ArrayList<Edge> edges) {
		this.edges = edges;
	}

	public ArrayList<Edge> getEdges() {
		return edges;
	}

	@Override
	public int compareTo(ProbeGene otherP) {
		return probeId.compareTo(otherP.getProbeId());
	}

	public void setNes(double nes) {
		this.nes = nes;
	}

	public double getNes() {
		return nes;
	}

}
