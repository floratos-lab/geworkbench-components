package org.geworkbench.components.idea;

import java.io.Serializable;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

/**
 * Edge data structure of IDEA Analysis.
 * 
 * @author zm2165 $Id$
 */
public class IdeaEdge implements Serializable, Comparable<IdeaEdge> {
	// Edge is the gene pair which is expanded on probe Ids. For each edge, the
	// geneNo1 and geneNo2 which is entrez gene number
	// may be the same, but probe Ids which is expRowNoG1 and expRowNoG2 are
	// unique.

	private static final long serialVersionUID = -1626720585421975791L;

	private DSGeneMarker marker1 = null;
	private DSGeneMarker marker2 = null;

	public enum InteractionType {
		PROTEIN_PROTEIN, PROTEIN_DNA
	};

	private InteractionType ppi;
	private double[] nullDeltaCorr;

	private double MI; // mutual information of the edge
	private double deltaCorr;
	private double zDeltaCorr;

	private boolean loc;
	private boolean goc;

	public IdeaEdge(DSGeneMarker marker1, DSGeneMarker marker2,
			InteractionType ppi) {
		this.marker1 = marker1;
		this.marker2 = marker2;
		this.ppi = ppi;
	}

	public void setMI(double MI) {
		this.MI = MI;
	}

	public double getMI() {
		return MI;
	}

	public void setDeltaCorr(double deltaCorr) {
		this.deltaCorr = deltaCorr;
		
	}

	public double getDeltaCorr() {
		return deltaCorr;
	}

	public void setNullData(double[] nullData) {
		nullDeltaCorr = nullData;
	}

	public double[] getNullData() {
		return nullDeltaCorr;
	}

	/* this method set LOC or GOC flags based on normCorr, deltaCorr */
	public void setFlags(double normCorr, double threshold) {

		if (normCorr < threshold) {
			// show significant edges
			if (deltaCorr < 0)
				loc = true;// save the flag for significant edge
			else if (deltaCorr > 0)
				goc = true;
		}
	}

	public boolean isLoc() {
		return loc;
	}

	public boolean isGoc() {
		return goc;
	}

	public int compareTo(IdeaEdge otherEdge) {
		double d = zDeltaCorr - otherEdge.getzDeltaCorr();
		if(d<0) return -1;
		else if (d>0) return 1;
		else return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IdeaEdge))
			return false;
		
		IdeaEdge ideaEdge = (IdeaEdge) obj;
		String m1 = marker1.getLabel();
		String m2 = marker2.getLabel();
		String o1 = ideaEdge.marker1.getLabel();
		String o2 = ideaEdge.marker2.getLabel();

		if((m1.equals(o1)&&m2.equals(o2))||((m1.equals(o2))&&(m2.equals(o1))))
			return true;
		else
			return false;		
	}
	
	@Override
	public int hashCode() {
		return marker1.getLabel().hashCode() * marker2.getLabel().hashCode();
	}

	/* set the computed result */
	public void setzDeltaCorr(double zDeltaCorr) {
		this.zDeltaCorr = zDeltaCorr;
	}

	public double getzDeltaCorr() {
		return zDeltaCorr;
	}

	public String getProbeId1() {
		return marker1.getLabel();
	}

	public String getProbeId2() {
		return marker2.getLabel();
	}

	public InteractionType getPpi() {
		return ppi;
	}

	public DSGeneMarker getMarker1() {
		return marker1;
	}

	public DSGeneMarker getMarker2() {
		return marker2;
	}

}
