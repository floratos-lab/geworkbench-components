/**
 * 
 */
package org.geworkbench.components.alignment.blast;

/**
 * @author zji
 * @version $Id$
 */
public class Hsp {
	private static final int LENGTH = 60;

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
			sb.append(formatSequence(alignLen[i], queryFrom[i], hitFrom[i],
					qseq[i], midline[i], hseq[i]));
		}
		return sb.toString();
	}

	private static String formatSequence(int alignLen, int queryFrom,
			int hitFrom, String q, String m, String h) {
		StringBuilder sb = new StringBuilder();
		int starting = 0;
		int ending = starting + LENGTH - 1;
		do {
			if (ending >= alignLen) {
				ending = alignLen - 1;
			}
			String qseqStr = String.format("%-8s%-7d%s  %-7d\n", "Query",
					queryFrom + starting, q.substring(starting, ending + 1),
					queryFrom + ending);
			// mid-line is allowed to be shorter than alignLen!
			String midline = "";
			if (ending >= m.length()) {
				midline = m.substring(starting);
			} else {
				midline = m.substring(starting, ending + 1);
			}
			String midlineStr = String.format("%15c%s\n", ' ', midline);
			String hseqStr = String.format("%-8s%-7d%s  %-7d\n", "Subject",
					hitFrom + starting, h.substring(starting, ending + 1),
					hitFrom + ending);
			sb.append(qseqStr);
			sb.append(midlineStr);
			sb.append(hseqStr);
			sb.append("\n");

			starting += LENGTH;
			ending += LENGTH;
		} while (starting < alignLen);

		return sb.toString();
	}
}
