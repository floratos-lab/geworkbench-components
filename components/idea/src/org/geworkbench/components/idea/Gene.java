package org.geworkbench.components.idea;

import java.io.Serializable;
import java.util.ArrayList;

import org.geworkbench.bison.datastructure.bioobjects.IdeaEdge;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;

/**
 * Gene data structure. It is actually Entrez Gene which has an attribute of geneNo.
 *@author zm2165
 *@version $Id$ 
 */
public class Gene implements Serializable, Comparable<Gene>{
	
	private static final long serialVersionUID = 7020364544812223644L;
	
	private int geneNo;
	private String probeIds="";	//""
	private String chromosomal;
	private String expRows="";	//""
	private ArrayList<IdeaEdge> edges=new ArrayList<IdeaEdge>();
	private int locs=0;		//LOC edges of the gene
	private int gocs=0;		//GOC edges of the gene
	private double cumLoc=1;//default is big
	private double cumGoc=1;
	private DSItemList<DSGeneMarker> markers=null;
	
	public Gene(){
		
	}
	
	public Gene(int geneNo){
		this.geneNo=geneNo;
	}

	public int getGeneNo(){
		return geneNo;
	}
		
	public String getProbeIds(){
		return probeIds;
	}
	
	public void setProbeIds(String probeIds){
		this.probeIds=probeIds;
	}
	
	public String getExpRows(){
		return expRows;
	}
	
	public void setExpRows(String expRows){
		this.expRows=expRows;
	}
	
	public void addEdge(IdeaEdge anEdge){
		edges.add(anEdge);
	}
	public ArrayList<IdeaEdge> getEdges(){
		return edges;
	}
	public void setEdges(ArrayList<IdeaEdge> edges){
		//this.edges=null;
		this.edges=edges;
	}	

	@Override
	public int compareTo(Gene otherGene) {
		// TODO Auto-generated method stub
		if (this.geneNo==otherGene.getGeneNo()) 
			return 0;
		else if(this.geneNo>otherGene.getGeneNo()) return 1;
		else return -1;
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
	
	public String getChromosomal() {
		return chromosomal;
	}
	public void setMarkers(DSItemList<DSGeneMarker> markers) {
		this.markers = markers;
	}

	public DSItemList<DSGeneMarker> getMarkers() {
		return markers;
	}
	
	
}
