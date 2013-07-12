/**
 * 
 */
package org.geworkbench.components.alignment.blast;

/**
 * @author zji
 * @version $Id$
 */
public class Hsp {
	final int numHsp;
	final String[] bitScore;
	final String[] score;
	final String[] evalue;
	final int[] queryFrom;
	final int[] queryTo;
	final int[] hitFrom;
	final int[] hitTo;
	final int[] identity, positive, gaps, alignLen;
	final String[] qseq, hseq, midline;

	public Hsp(int numHsp, String[] bitScore, String[] score, String[] evalue,
			int[] queryFrom, int[] queryTo, int[] hitFrom, int[] hitTo,
			int[] identity, int[] positive, int[] gaps, int[] alignLen,
			String[] qseq, String[] hseq, String[] midline) {
		this.numHsp = numHsp;
		this.bitScore = bitScore;
		this.score = score;
		this.evalue = evalue;
		this.queryFrom = queryFrom;
		this.queryTo = queryTo;
		this.hitFrom = hitFrom;
		this.hitTo = hitTo;
		this.identity = identity;
		this.positive = positive;
		this.gaps = gaps;
		this.alignLen = alignLen;
		this.qseq = qseq;
		this.hseq = hseq;
		this.midline = midline;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numHsp; i++) {
			sb.append("\nScore = " + bitScore[i] + " bits (" + score[i]
					+ "), Expect = " + evalue[i] + "\n");

			int percentage = (int) Math.round(100. * identity[i] / alignLen[i]);
			if (percentage == 100) { // don't round to 100% if it is not really
				percentage = 100 * identity[i] / alignLen[i];
			}

			sb.append("Indentities = " + identity[i] + "/" + alignLen[i] + " ("
					+ percentage + "%), Gaps = " + gaps[i] + "/" + alignLen[i]
					+ "\n");

			sb.append("\n");
			String qseqStr = String.format("%-8s%-7d%s\n", "Query", queryFrom[i], qseq[i]);
			String midlineStr = String.format("%15c%s\n", ' ', midline[i]);
			String hseqStr = String.format("%-8s%-7d%s\n", "Subject", hitFrom[i], hseq[i]);
			sb.append(qseqStr);
			sb.append(midlineStr);
			sb.append(hseqStr);
		}
		return sb.toString();
	}
}
