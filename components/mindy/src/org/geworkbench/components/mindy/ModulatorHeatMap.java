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
import java.util.List;

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
 * @version $Id$
 */
public class ModulatorHeatMap extends JPanel {

	private static final long serialVersionUID = -1571049509507400635L;

	private static Log log = LogFactory.getLog(ModulatorHeatMap.class);

	private static final String LEFT_LABEL = "Lowest ";
	private static final String RIGHT_LABEL = "Highest ";
	private static final String PERCENT = "%";

	private static final int SPACER_TOP = 20;
	private static final int SPACER_SIDE = 20;
	private static final int BAR_HEIGHT = 12;
	private static final int PREFERRED_CELL_WIDTH = 3;

	private static final Font BASE_FONT = new Font("SansSerif", Font.BOLD, 12);
	private static final Color COLOR_TEXT = new Color(0.2f, 0.2f, 0.2f);

	private int maxGeneNameWidth = -1;
	private boolean showProbeName = false;

	private final ModulatorHeatMapModel model;

	/**
	 * The only constructor of ModulatorHeatMap.
	 * 
	 * ModulatorHeatMap depends on ModulatorHeatMapModel; not the other way around.
	 * 
	 * @param model
	 */
	public ModulatorHeatMap(ModulatorHeatMapModel model) {
		this.model = model;

		// if no symbol names available show probe name
		if (!model.isAnnotated()) {
			showProbeName = true;
		}

		updateMaxGeneNameWidth();
	}

	public void updateMaxGeneNameWidth() {
		// calculate Max Gene Name Width
		List<DSGeneMarker> markers = model.getTargets();
		FontRenderContext context = new FontRenderContext(null, true, false);
		for (DSGeneMarker marker : markers) {
			String shortName = this.getMarkerDisplayName(marker);
			Rectangle2D bounds = BASE_FONT.getStringBounds(shortName, context);
			if (bounds.getWidth() > maxGeneNameWidth) {
				maxGeneNameWidth = (int) bounds.getWidth() + 1;
			}
		}
	}
	
	/**
	 * Reset variables that are used for painting and repaint.
	 */
	public void reset() {
		updateMaxGeneNameWidth();
		revalidate();
		repaint();
	}

	@Override
	public void paintComponent(Graphics graphics) {

		Graphics2D g = (Graphics2D)graphics;

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setFont(BASE_FONT);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());

		// Draw the modulator expression line
		g.setColor(COLOR_TEXT);
		FontMetrics metrics = g.getFontMetrics();
		String modulatorName = this.getMarkerDisplayName(model.getModulator());
		int modNameWidth = metrics.stringWidth(modulatorName);
		g.drawString(modulatorName, (getWidth() / 2) - (modNameWidth / 2),
				SPACER_TOP);
		int modBarTopY = SPACER_TOP + metrics.getDescent() + 1;

		// Some variables useful for the next two sections of painting
		float expressionBarWidth = (getWidth() - (2 * SPACER_SIDE) - (2 * SPACER_SIDE + maxGeneNameWidth)) / 2f;
		float cellWidth = expressionBarWidth
				/ ((model.getHalf1().size() + model.getHalf2().size()) / 2f);
		int transFacStartY = modBarTopY + BAR_HEIGHT + SPACER_TOP;

		// Draw the two transcription factor gradients
		// TransFac headers
		g.setColor(COLOR_TEXT);
		String transFacName = this.getMarkerDisplayName(
				model.getTranscriptionFactor()).trim();
		int transFacNameWidth = metrics.stringWidth(transFacName);
		// Left Side (marker name)
		g.drawString(transFacName, SPACER_SIDE
				+ (expressionBarWidth - transFacNameWidth) / 2 + 1,
				transFacStartY);
		// Right Side (marker name)
		g.drawString(
				transFacName,
				Math.round((getWidth() - SPACER_SIDE - expressionBarWidth - 1)
						+ (expressionBarWidth + 1) / 2 - transFacNameWidth / 2),
				transFacStartY);

		int transFacBarY = transFacStartY + metrics.getDescent() + 1;

		// Drawing line under modulator name
		int x1 = Math.round(SPACER_SIDE
				+ (expressionBarWidth - transFacNameWidth) / 2 + 1
				+ transFacNameWidth / 2);
		int y1 = modBarTopY;
		int x2 = Math.round((getWidth() - SPACER_SIDE - expressionBarWidth - 1)
				+ (expressionBarWidth + 1) / 2);
		int y2 = y1 + transFacBarY / 4;
		g.setColor(COLOR_TEXT);
		g.drawLine(x1, y1, x2, y1); // top line
		g.drawLine(x1, y1, x1, y2); // left vertical line
		g.drawLine(x2, y1, x2, y2); // right vertical line

		// Label two ends of the line
		float setFractionPercent = model.getSetFraction() * 100;
		g.drawString(LEFT_LABEL + setFractionPercent + PERCENT, x1, SPACER_TOP);
		String s = RIGHT_LABEL + setFractionPercent + PERCENT;
		g.drawString(s, x2 - metrics.stringWidth(s), SPACER_TOP);

		// Outlines for trans fac gradients (the triangles)
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

		for (int i = 0; i < model.getTargetRows().size(); i++) {
			MindyResultRow mindyRow = model.getTargetRows().get(i);
			DSGeneMarker target = mindyRow.getTarget();
			paintExpressionBar(cellWidth, expressionBarWidth, g, targetCurrY,
					target);
			String targetName = this.getMarkerDisplayName(target);

			int targetNameWidth = metrics.stringWidth(targetName);
			g.setColor(COLOR_TEXT);

			g.drawString(targetName, (getWidth() / 2) - (targetNameWidth / 2),
					targetCurrY + BAR_HEIGHT - 1);
			targetCurrY += BAR_HEIGHT;
		}

		// Outlines for target gradients
		g.setColor(Color.GRAY);
		g.drawRect(SPACER_SIDE, targetStartY, (int) expressionBarWidth,
				targetCurrY - targetStartY);
		g.drawRect((int) (getWidth() - SPACER_SIDE - expressionBarWidth - 1),
				targetStartY, (int) (expressionBarWidth + 1), targetCurrY
						- targetStartY);

		MindyPlugin.setCursorFinished();
		log.debug("paint(Graphics graphics) called");
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

			ColorContext colorContext = model.getColorContext();
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

}
