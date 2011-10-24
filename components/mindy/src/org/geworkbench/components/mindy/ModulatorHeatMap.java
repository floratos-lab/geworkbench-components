package org.geworkbench.components.mindy;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.util.colorcontext.ColorContext;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyResultRow;

/**
 * Creates a heat map of selected modulator, transcription factor, and targets.
 *
 * @author mhall
 * @author ch2514
 * @author oshteynb
 * @version $Id: ModulatorHeatMap.java,v 1.35 2009-04-29 19:55:33 oshteynb Exp $
 */
@SuppressWarnings("serial")
public class ModulatorHeatMap extends JPanel {

	private static Log log = LogFactory.getLog(ModulatorHeatMap.class);

	private static final String LEFT_LABEL = "Lowest ";
	private static final String RIGHT_LABEL = "Highest ";
	private static final String PERCENT = "%";

	private static final int SPACER_TOP = 20;
	private static final int SPACER_SIDE = 20;
	private static final int BAR_HEIGHT = 12;
	private static final int PREFERRED_CELL_WIDTH = 3;

	public static final int MAX_MARKER_NAME_CHARS = 30;

	public static final Font BASE_FONT = new Font("SansSerif", Font.BOLD, 12);
	public static final Color COLOR_TEXT = new Color(0.2f, 0.2f, 0.2f);

	private int maxGeneNameWidth = -1;
	private boolean showProbeName = false;

	private float setFractionPercent;
	private ColorContext colorContext = null;

	private ModulatorHeatMapModel model;

	private BufferedImage offscreen;

	/*
	 * true if out of memory exception while allocating BufferedImage( set in
	 * catch block)
	 */
	private boolean doNotPaint = false;

	/**
	 * for now will have simple default constructor.
	 *
	 */
	public ModulatorHeatMap() {
		log.debug("\tHeatMap::constructor::start...");

		this.setBackground(Color.white);
		this.setOpaque(true);

		log.debug("\tHeatMap::constructor::end.");
	}

	/**
	 * refactored from MindyPlugin, called from several ActionListener classes
	 */
	public void prepareGraphics() {
		Graphics g = getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	/**
	 * reset variables that are used for painting, for example, modulator is
	 * different now, instead of discarding the heatMap and recreating it from
	 * the scratch
	 *
	 * fire repainting
	 */
	public void reset() {
		this.maxGeneNameWidth = calculateMaxGeneNameWidth();

		offscreen = null;
		doNotPaint = false;

		HeatmapChanged();
	}

	private int calculateMaxGeneNameWidth() {
		List<DSGeneMarker> markers = model.getTargets();
		FontRenderContext context = new FontRenderContext(null, true, false);
		for (DSGeneMarker marker : markers) {
			String shortName = this.getMarkerDisplayName(marker);
			Rectangle2D bounds = BASE_FONT.getStringBounds(shortName, context);
			if (bounds.getWidth() > maxGeneNameWidth) {
				maxGeneNameWidth = (int) bounds.getWidth() + 1;
			}
		}
		log.debug("Max gene name width: " + maxGeneNameWidth);

		return maxGeneNameWidth;
	}

	/**
	 * Paint the heat map graphics object.
	 *
	 * @param graphics
	 *            - the graphics object representing the heat map.
	 */
	public void paint(Graphics graphics) {
		if (!doNotPaint)
			doPaint(graphics);
	}

	public void update(Graphics g) {
		if (!doNotPaint)
			paint(g);
	}

	/**
	 * Print the heat map graphics object.
	 *
	 * @param graphics
	 *            - the graphics object representing the heat map.
	 */
	public void print(Graphics graphics) {
		doPaint(graphics);
	}

	/**
	 * Paint the heat map graphics object.
	 *
	 * @param graphics
	 *            - the graphics object representing the heat map.
	 * @param print
	 *            - true if this graphics object is to be printed.
	 */
	public void doPaint(Graphics graphics) {
		log.debug("\t\tdoPaint()::start...");

		Graphics2D g = null;
		Dimension dim = getSize();

		int w = (int) dim.getWidth();
		int h = (int) dim.getHeight();
		if ((offscreen == null) && (w > 0) && (h > 0)) {
			log.debug("\t\t\tbuffer processing...");
			try {
				offscreen = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				g = (Graphics2D) offscreen.getGraphics();

				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
						RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g.setFont(BASE_FONT);
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, w, (int) h);
			} catch (OutOfMemoryError err) {
				JOptionPane.showMessageDialog(null,
						"There is not enough memory to display the heatmap.",
						"Warning", JOptionPane.WARNING_MESSAGE);
				log.error("Not enough memory to display the heatmap:"
						+ err.getMessage());
				doNotPaint = true;
			}

			// Draw the modulator expression line
			log.debug("\t\t\tdrawing modular expression line...");
			g.setColor(COLOR_TEXT);
			FontMetrics metrics = g.getFontMetrics();
			String modulatorName = this.getMarkerDisplayName(model
					.getModulator());
			int modNameWidth = metrics.stringWidth(modulatorName);
			g.drawString(modulatorName, (getWidth() / 2) - (modNameWidth / 2),
					SPACER_TOP);
			int modBarTopY = SPACER_TOP + metrics.getDescent() + 1;

			// Some variables useful for the next two sections of painting
			log.debug("\t\t\tpainting prep...");
			float expressionBarWidth = (getWidth() - (2 * SPACER_SIDE) - (2 * SPACER_SIDE + maxGeneNameWidth)) / 2f;
			float cellWidth = expressionBarWidth
					/ ((model.getHalf1().size() + model.getHalf2().size()) / 2f);
			int transFacStartY = modBarTopY + BAR_HEIGHT + SPACER_TOP;

			// Draw the two transcription factor gradients
			// TransFac headers
			log.debug("\t\t\tdrawing tf...");
			g.setColor(COLOR_TEXT);
			String transFacName = this.getMarkerDisplayName(
					model.getTranscriptionFactor()).trim();
			int transFacNameWidth = metrics.stringWidth(transFacName);
			// Left Side (marker name)
			g.drawString(transFacName, SPACER_SIDE
					+ (expressionBarWidth - transFacNameWidth) / 2 + 1,
					transFacStartY);
			// Right Side (marker name)
			g.drawString(transFacName, Math.round((getWidth() - SPACER_SIDE
					- expressionBarWidth - 1)
					+ (expressionBarWidth + 1) / 2 - transFacNameWidth / 2),
					transFacStartY);

			int transFacBarY = transFacStartY + metrics.getDescent() + 1;

			// Drawing line under modulator name
			log.debug("\t\t\tdrawing line under mod name...");
			int x1 = Math.round(SPACER_SIDE
					+ (expressionBarWidth - transFacNameWidth) / 2 + 1
					+ transFacNameWidth / 2);
			int y1 = modBarTopY;
			int x2 = Math
					.round((getWidth() - SPACER_SIDE - expressionBarWidth - 1)
							+ (expressionBarWidth + 1) / 2);
			int y2 = y1 + transFacBarY / 4;
			g.setColor(COLOR_TEXT);
			g.drawLine(x1, y1, x2, y1); // top line
			g.drawLine(x1, y1, x1, y2); // left vertical line
			g.drawLine(x2, y1, x2, y2); // right vertical line

			// Label two ends of the line
			g.drawString(LEFT_LABEL + this.setFractionPercent + PERCENT, x1,
					SPACER_TOP);
			String s = RIGHT_LABEL + this.setFractionPercent + PERCENT;
			g.drawString(s, x2 - metrics.stringWidth(s), SPACER_TOP);

			// Outlines for trans fac gradients (the triangles)
			log.debug("\t\t\tdrawing triangles...");
			g.setColor(COLOR_TEXT);
			int xx1 = SPACER_SIDE;
			int yy1 = transFacBarY;
			int xx2 = xx1 + (int) expressionBarWidth;
			int yy2 = yy1 + BAR_HEIGHT;
			g.drawLine(xx1, yy2, xx2, yy1); // left triangle: diagonal
			g.drawLine(xx2, yy1, xx2, yy2); // vertical
			g.drawLine(xx1, yy2, xx2, yy2); // horizontal
			xx1 = (int) (getWidth() - SPACER_SIDE - expressionBarWidth - 1);
			xx2 = xx1 + (int) (expressionBarWidth + 1);
			g.drawLine(xx1, yy2, xx2, yy1); // right triangle: diagonal
			g.drawLine(xx2, yy1, xx2, yy2); // vertical
			g.drawLine(xx1, yy2, xx2, yy2); // horizontal

			// Draw the target's expression values
			int targetStartY = transFacBarY + BAR_HEIGHT + 5;
			int targetCurrY = targetStartY;

			log.debug("\t\t\tloop start...");
			for (int i = 0; i < model.getTargetRows().size(); i++) {
				MindyResultRow mindyRow = model.getTargetRows().get(i);
				DSGeneMarker target = mindyRow.getTarget();
				paintExpressionBar(cellWidth, expressionBarWidth, g,
						targetCurrY, target);
				String targetName = this.getMarkerDisplayName(target);

				int targetNameWidth = metrics.stringWidth(targetName);
				g.setColor(COLOR_TEXT);

				g.drawString(targetName, (getWidth() / 2)
						- (targetNameWidth / 2), targetCurrY + BAR_HEIGHT - 1);
				targetCurrY += BAR_HEIGHT;
			}
			log.debug("\t\t\tloop end.");
			// Outlines for target gradients
			g.setColor(Color.GRAY);
			g.drawRect(SPACER_SIDE, targetStartY, (int) expressionBarWidth,
					targetCurrY - targetStartY);
			g.drawRect(
					(int) (getWidth() - SPACER_SIDE - expressionBarWidth - 1),
					targetStartY, (int) (expressionBarWidth + 1), targetCurrY
							- targetStartY);
		}
		graphics.drawImage(offscreen, 0, 0, this);

		log.debug(" Cursor ***set finished flag");
		MindyPlugin.setCursorFinished();

		log.debug("\t\tdoPaint()::end.");
	}

	private void paintExpressionBar(float cellWidth, float expressionBarWidth,
			Graphics2D g, int y, DSGeneMarker markerToPaint) {
		int size = model.getHalf1().size() + model.getHalf2().size();
		int halfArrays = size / 2;
		for (int i = 0; i < size; i++) {
			int startX;
			DSMarkerValue value = null;
			if (i < halfArrays) {
				startX = SPACER_SIDE + (int) (i * cellWidth);
				value = ((DSMicroarray) model.getHalf1().get(i))
						.getMarkerValue(markerToPaint);
			} else {
				startX = (int) (getWidth() - SPACER_SIDE - expressionBarWidth + ((i - halfArrays) * cellWidth));
				value = ((DSMicroarray) model.getHalf2().get(i - halfArrays))
						.getMarkerValue(markerToPaint);
			}
			g.setColor(colorContext.getMarkerValueColor(value, markerToPaint,
					1.0f));
			g.fillRect(startX, y, (int) (cellWidth + 1), BAR_HEIGHT);
		}
	}

	/**
	 * Get the preferred size of the heat map.
	 *
	 * @return A Dimension object representing the preferred size of the heat
	 *         map.
	 */
	public Dimension getPreferredSize() {
		int preferredWidth = (int) ((PREFERRED_CELL_WIDTH * model
				.getSortedPerMod().size()) * 1.5 + (2 * SPACER_SIDE));
		if (preferredWidth < 3 * maxGeneNameWidth) {
			// This means there are probably a small number of arrays, so the
			// calculated width is going to be small
			preferredWidth = 3 * maxGeneNameWidth + (2 * SPACER_SIDE);
		}
		int preferredHeight = (model.getTargetRows().size() * BAR_HEIGHT)
				+ (6 * BAR_HEIGHT) + (2 * SPACER_TOP);
		return new Dimension(preferredWidth, preferredHeight);
	}

	/**
	 * Get the minimum size of the heat map.
	 *
	 * @return A Dimension object representing the minimum size of the heat map.
	 */
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	/**
	 * Check to see if the heat map should display probe names or gene names.
	 *
	 * @return If true, the heat map displays probe names. If not, the map
	 *         displays gene names.
	 */
	public boolean isShowProbeName() {
		return this.showProbeName;
	}

	/**
	 * Specify whether or not the heat map should display probe names or gene
	 * names.
	 *
	 * @param showProbeName
	 *            - if true, the heat map displays probe names. If not, the map
	 *            displays gene names.
	 */
	public void setShowProbeName(boolean showProbeName) {
		this.showProbeName = showProbeName;
	}

	/**
	 * Specifies the marker name (probe name vs. gene name) to display on the
	 * heat map.
	 *
	 * @param marker
	 *            - gene marker
	 * @return The marker name (probe vs. gene) to display on the heat map.
	 */
	public String getMarkerDisplayName(DSGeneMarker marker) {
		return MindyPlugin.getMarkerDisplayName(this.showProbeName, marker);
	}

	public void HeatmapChanged() {
		this.doResizeAndRepaint();
	}

	private void doResizeAndRepaint() {
		revalidate();
		repaint();
	}

	public ModulatorHeatMapModel getModel() {
		return model;
	}

	public void setModel(ModulatorHeatMapModel model) {
		this.model = model;

		// if no symbol names available show probe name
		if (!model.isAnnotated()) {
			showProbeName = true;
		}

		this.setFractionPercent = model.getSetFraction() * 100;
		this.colorContext = model.getColorContext();

		reset();
	}
}
