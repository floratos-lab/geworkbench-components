package org.geworkbench.components.sequenceretriever;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.util.SequenceUtils;
import org.geworkbench.util.sequences.GeneChromosomeMatcher;

/**
 * Created by IntelliJ IDEA. User: xiaoqing Date: Sep 6, 2006 Time: 5:07:02 PM
 * To change this template use File | Settings | File Templates.
 * 
 * The view of one cell in the display table of retrieved sequences.
 * 
 * @version $Id$
 */
public final class RetrievedSequenceView extends JPanel {
	private static final long serialVersionUID = -4686885853878188788L;

	// constants
	static final int HEIGHT = 30; // used in RetreivedSequenceDisplayPabel
	private static final Color SEQUENCEBACKGROUDCOLOR = Color.BLUE;
	private static final Color SEQUENCEDOWNSTREAMCOLOR = Color.RED;
	private final static String BASEUNIURLSTR = "http://www.ebi.ac.uk/cgi-bin/dbfetch?db=uniprot&id=";
	
	// static variables // FIXME the decision to make these static is problematic
	private static int maxSeqLen = 20000; // default is 20000.
	private static int currentLocation = -1;
	private static int upstreamTotal = -1;
	
	private int xOff = 0;
	private double scale = 1;
	private CSSequence sequence;
	private GeneChromosomeMatcher geneChromosomeMatcher;
	
	// the following members have no effects on this object other being set and get
	private boolean isIncluded = false;
	private String url;

	// called only from RetrivedSequenceDisplayPanel
	static void setMaxSeqLen(int maxSeqLen) {
		RetrievedSequenceView.maxSeqLen = maxSeqLen;
	}

	// called only from SequenceRereiver.getSequences
	static void setDownstreamTotal(int downstreamTotal) {
		// no effect
	}

	// called only from SequenceRereiver.getSequences
	static void setUpstreamTotal(int upstreamTotal) {
		RetrievedSequenceView.upstreamTotal = upstreamTotal;
	}

	// called only from RetrivedSequenceDisplayPanel
	static int getCurrentLocation() {
		return currentLocation;
	}

	// called only from SequenceRereiver.getSequences
	void setGeneChromosomeMatcher(
			GeneChromosomeMatcher geneChromosomeMatcher) {
		this.geneChromosomeMatcher = geneChromosomeMatcher;
	}

	// called only from SequenceRereiver.getSequences
	void setUrl(String url) {
		this.url = BASEUNIURLSTR + url.trim();
	}

	// called only from RetrivedSequenceDisplayPanel
	String getUrl() {
		return url;
	}

	// called from SequenceRetreiver
	CSSequence getSequence() {
		return sequence;
	}

	// called only from RetrivedSequenceDisplayPanel
	void setIncluded(boolean included) {
		isIncluded = included;
	}

	// called from SequenceRetreiver and RetrivedSequenceDisplayPanel
	boolean isIncluded() {
		return isIncluded;
	}

	// called from SequenceRetreiver and RetrivedSequenceDisplayPanel
	public RetrievedSequenceView(CSSequence theSeq) {
		super(true);
		sequence = theSeq;

	}

	@Override
	public void paintComponent(Graphics g1d) {
		super.paintComponent(g1d);
		scale = Math.min(5.0, (double) (this.getWidth() - 20 - xOff)
				/ (double) maxSeqLen);

		Graphics2D g = (Graphics2D) g1d;

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

	@Override
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
