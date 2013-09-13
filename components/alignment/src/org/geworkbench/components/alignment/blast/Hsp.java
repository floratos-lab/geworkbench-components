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
	final int[] queryFrame;
	final int[] hitFrame;
	final int[] identity, positive, gaps, alignLen;
	final String[] qseq, hseq, midline;
	
	final int queryStep;
	final int hitStep;

	final String programName;
	
	public Hsp(String programName, int numHsp, String[] bitScore, String[] score, String[] evalue,
			int[] queryFrom, int[] queryTo, int[] hitFrom, int[] hitTo,
			int[] queryFrame, int[] hitFrame,
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
		this.queryFrame = queryFrame;
		this.hitFrame = hitFrame;
		this.identity = identity;
		this.positive = positive;
		this.gaps = gaps;
		this.alignLen = alignLen;
		this.qseq = qseq;
		this.hseq = hseq;
		this.midline = midline;
		
		this.programName = programName;
		
		if(programName.equals("tblastn")) {
			queryStep = 1;
			hitStep = 3;
		} else if(programName.equals("blastx")) {
			queryStep = 3;
			hitStep = 1;
		} else if (programName.equals("tblastx")) {
			queryStep = 3;
			hitStep = 3;
		} else {
			queryStep = 1;
			hitStep = 1;
		}

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

			sb.append("Identities = " + identity[i] + "/" + alignLen[i] + " ("
					+ percentage + "%), Gaps = " + gaps[i] + "/" + alignLen[i]
					+ makeStrandOrFrameText(queryFrame[i], hitFrame[i])+"\n");

			sb.append("\n");
			
			int queryDirection = 1;
			int hitDirection = 1;
			int queryFromModified = queryFrom[i];
			if(queryFrame[i]<0) {
				queryDirection = -1;
				if(queryFrom[i]<queryTo[i]) { // the starting and ending points are not switched
					queryFromModified = queryTo[i];
				}
			}
			if(hitFrame[i]<0) hitDirection = -1;
			sb.append(formatSequence(alignLen[i], queryFromModified, hitFrom[i],
					qseq[i], midline[i], hseq[i], queryStep, hitStep, queryDirection, hitDirection));
		}
		return sb.toString();
	}

	private static String formatSequence(int alignLen, int queryFrom,
			int hitFrom, String q, String m, String h, int queryStep, int hitStep, int queryDirection, int hitDirection) {
		StringBuilder sb = new StringBuilder();
		int starting = 0;
		int ending = starting + LENGTH - 1;

		int totalGapInQuery = 0;
		int totalGapInSubject = 0;
		do {
			if (ending >= alignLen) {
				ending = alignLen - 1;
			}

			String query = q.substring(starting, ending + 1);
			int gapInQuery = gapCount(query);
			int startingNumber = queryFrom + (starting - totalGapInQuery)*queryStep*queryDirection;
			int endingNumber = queryFrom + (ending - totalGapInQuery - gapInQuery)*queryStep*queryDirection + (queryStep-1)*queryDirection;
			String qseqStr = String.format("%-8s%-7d%s  %-7d\n", "Query",
					startingNumber, query, endingNumber);
			totalGapInQuery += gapInQuery;

			// mid-line is allowed to be shorter than alignLen!
			String midline = "";
			if (ending >= m.length()) {
				midline = m.substring(starting);
			} else {
				midline = m.substring(starting, ending + 1);
			}
			String midlineStr = String.format("%15c%s\n", ' ', midline);

			String subject = h.substring(starting, ending + 1);
			int gapInSubject = gapCount(subject);
			startingNumber = hitFrom + (starting - totalGapInSubject)*hitStep*hitDirection;
			endingNumber = hitFrom + (ending - totalGapInSubject - gapInSubject)*hitStep*hitDirection + (hitStep-1)*hitDirection;
			String hseqStr = String.format("%-8s%-7d%s  %-7d\n", "Subject",
					startingNumber, subject, endingNumber);
			totalGapInSubject += gapInSubject;

			sb.append(qseqStr);
			sb.append(midlineStr);
			sb.append(hseqStr);
			sb.append("\n");

			starting += LENGTH;
			ending += LENGTH;
		} while (starting < alignLen);

		return sb.toString();
	}

	private String makeStrandOrFrameText(int queryFrame, int hitFrame) {
		StringBuilder sb = new StringBuilder();
		if(programName.equals("blastn")) {
			sb.append(", Strand ");
			if(queryFrame==1) sb.append("Plus");
			else if(queryFrame==-1) sb.append("/Minus");

			if(hitFrame==1) sb.append("Plus");
			else if(hitFrame==-1) sb.append("/Minus");
		} else if (programName.equals("blastx") || programName.equals("tblastn")
				|| programName.equals("tblastx")) {
			sb.append(", Frame ");
			boolean queryFrameWritten = true;
			if(queryFrame>0) sb.append("+"+queryFrame);
			else if(queryFrame<0) sb.append(queryFrame);
			else queryFrameWritten = false;

			if(queryFrameWritten && hitFrame!=0) sb.append("/");
			
			if(hitFrame>0) sb.append("+"+hitFrame);
			else if(hitFrame<0) sb.append(hitFrame);
		}
		return sb.toString();
	}
	
	private static int gapCount(String s) {
		int gap = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '-')
				gap++;
		}
		return gap;
	}
}
