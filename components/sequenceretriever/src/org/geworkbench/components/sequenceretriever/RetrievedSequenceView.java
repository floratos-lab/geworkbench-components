package org.geworkbench.components.sequenceretriever;

import javax.swing.*;

import org.geworkbench.util.sequences.GeneChromosomeMatcher;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.util.SequenceUtils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;

/**
 * Created by IntelliJ IDEA. User: xiaoqing Date: Sep 6, 2006 Time: 5:07:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class RetrievedSequenceView extends JPanel {
	int xOff = 0;
	double scale = 1;
	static int maxSeqLen = 20000; // default is 20000.
	static int currentLocation = -1;
	private CSSequence sequence;
	private GeneChromosomeMatcher geneChromosomeMatcher;
	static int upstreamTotal = -1;
	static int downstreamTotal = -1;

	private String url;
	public static final int HEIGHT = 30;
	private static final Color SEQUENCEBACKGROUDCOLOR = Color.BLUE;
	private static final Color SEQUENCEDOWNSTREAMCOLOR = Color.RED;
	private final static String BASEUNIURLSTR = "http://www.ebi.ac.uk/cgi-bin/dbfetch?db=uniprot&id=";

	public static int getMaxSeqLen() {
		return maxSeqLen;
	}

	public static void setMaxSeqLen(int maxSeqLen) {
		RetrievedSequenceView.maxSeqLen = maxSeqLen;
	}

	public static int getUpstreamTotal() {
		return upstreamTotal;
	}

	public static int getDownstreamTotal() {
		return downstreamTotal;
	}

	public static void setDownstreamTotal(int downstreamTotal) {
		RetrievedSequenceView.downstreamTotal = downstreamTotal;
	}

	public static void setUpstreamTotal(int upstreamTotal) {
		RetrievedSequenceView.upstreamTotal = upstreamTotal;
	}

	public static int getCurrentLocation() {
		return currentLocation;
	}

	public static void setCurrentLocation(int currentLocation) {
		RetrievedSequenceView.currentLocation = currentLocation;
	}

	public void setGeneChromosomeMatcher(
			GeneChromosomeMatcher geneChromosomeMatcher) {
		this.geneChromosomeMatcher = geneChromosomeMatcher;
	}

	public GeneChromosomeMatcher getGeneChromosomeMatcher() {
		return geneChromosomeMatcher;
	}

	public void setUrl(String url) {
		this.url = BASEUNIURLSTR + url.trim();
	}

	public String getUrl() {
		return url;
	}

	public void setSequence(CSSequence sequence) {
		this.sequence = sequence;
	}

	public CSSequence getSequence() {
		return sequence;
	}

	public void setIncluded(boolean included) {
		isIncluded = included;
	}

	public boolean isIncluded() {
		return isIncluded;
	}

	private boolean isIncluded = false;

	public RetrievedSequenceView() {
		super(true);
		repaint();

	}

	public RetrievedSequenceView(CSSequence theSeq) {
		super(true);
		sequence = theSeq;

	}

	public void paintComponent(Graphics g1d) {
		super.paintComponent(g1d);
		scale = Math.min(5.0, (double) (this.getWidth() - 20 - xOff)
				/ (double) maxSeqLen);

		Graphics2D g = (Graphics2D) g1d;
		int width = getWidth() - 4;
		if (sequence != null) {
			int x = xOff + (int) (sequence.length() * scale / 2);
			if (upstreamTotal > -1) {
				x = xOff
						+ (int) (sequence.length() * scale * upstreamTotal / maxSeqLen);
			}

			g.setColor(SEQUENCEBACKGROUDCOLOR);
			int y = HEIGHT - 10;
			if (SequenceUtils.isValidDNASeqForBLAST(sequence)) {

				g.drawLine(xOff, y, x, y);
				g.drawLine(xOff, y - 10, xOff, y);
				int shape = 3;
				int[] xi = new int[shape];
				int[] yi = new int[shape];
				int height = 12;
				if (geneChromosomeMatcher != null
						&& geneChromosomeMatcher.isPositiveStrandDirection()) {
					xi[0] = xi[1] = xOff + 20;
					yi[0] = (int) y - height / 2 - 2;
					yi[1] = (int) y - height / 2 + 6;
					xi[2] = xi[0] + 4;
					yi[2] = (int) y - height / 2 + 2;
					// g.drawPolyline(xi, yi, addtionalPoint);
				} else if (geneChromosomeMatcher != null) {
					xi[0] = xi[1] = xOff + 20;
					yi[0] = (int) y - height / 2 - 2;
					yi[1] = (int) y - height / 2 + 6;
					xi[2] = xi[0] - 4;
					yi[2] = (int) y - height / 2 + 2;

				}

				g.drawPolygon(xi, yi, shape);
				g.fillPolygon(xi, yi, shape);
				g.setColor(SEQUENCEDOWNSTREAMCOLOR);
				int x1 = xOff + (int) (sequence.length() * scale);
				g.drawLine(x, y, x1, y);
				g.drawLine(x, y - 10, x, y);
				g.drawLine(x1, y - 10, x1, y);

			} else {
				x = xOff + (int) (sequence.length() * scale);
				g.drawLine(xOff, y, x, y);
				g.drawLine(x, y - 10, x, y);
				g.drawLine(xOff, y - 10, xOff, y);
			}

		}
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	 	 	g.setColor(Color.BLACK);

	}

	public String getToolTipText(MouseEvent event) {
		float x = event.getX() - xOff;
		int index = (int) (x / scale);
		if (sequence != null && (index >= 0) && (index < sequence.length())) {
			String highlight = null;
			int endPoint = Math.min(index + 10, sequence.length() - 1);
			highlight = sequence.getSequence().substring(index, endPoint);
			// Start from 1 not 0.
			index++;
			if (event.getID() != MouseEvent.MOUSE_CLICKED) {
				currentLocation = index;
			}

			return "" + index + ": " + highlight;
		}
		return null;
	}
}
