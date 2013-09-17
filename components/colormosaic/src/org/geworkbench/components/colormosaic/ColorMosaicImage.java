package org.geworkbench.components.colormosaic;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.awt.BasicStroke;
import java.awt.Stroke;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.util.colorcontext.ColorContext;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.PhenotypeSelectedEvent; 

/**
 * Color mosaic image.
 * 
 * This class is used in CMHRuler, CNVRuler and ColorMosaicPanel.
 * 
 * @author Manjunath Kustagi
 * @version $Id$
 */

public class ColorMosaicImage extends JPanel implements Scrollable {
	private static final long serialVersionUID = -1651298591477619917L;

	private static Log log = LogFactory.getLog(ColorMosaicImage.class);
	
	// Static Variables
	private static Font labelFont = null;
	final static int gutter = 4;
	final static int maxClusterNo = 64;
	final static int MAXFONTSIZE = 10;
	final static int DEFAULTRES = 120;

	// Instance Variables
	// used by CMHRUler and CMVRuler
	int geneWidth = 20; 
	int geneHeight = 10;
	boolean showAllMArrays = true;
	
	private int newMArrayProxy = 0;
	private int newMarkerProxy = 0;
	private int markerProxy = -1;
	private int microarrayProxy = -1;
	private int markerId = -1;
	private int microarrayId = -1;
	private int geneNo = 0;
	private int clusterNo = 0;
	private EisenBlock[] cluster = new EisenBlock[maxClusterNo];
	private DSMicroarraySet microarraySet = null;
	private BorderLayout borderLayout1 = new BorderLayout();

	private DSPanel<DSGeneMarker> markerPanel = null;
	protected DSPanel<DSMicroarray> microarrayPanel = null;

	private boolean isPrintLabels = true;
	private boolean isPrintRatio = true;
	private boolean isPrintDescription = true;
	private boolean isPrintAccession = true;
	private int ratioWidth = 0;
	private int accessionWidth = 0;
	
	// variables used by ColorMosaicPanel
	boolean isPrintPValue = false;
	int pValueWidth = 0;
	
	private int labelWidth = 0;
	private int labelGutter = 5;
	private double intensity = 1.0;
	private int resolution = DEFAULTRES;
	private int oldRes = DEFAULTRES;
	private int fontSize = 0;
	private int textSize = 0;
	private BasicColorMosaicPanel parent = null;	 
	private boolean showAllMarkers = true;
	private boolean showSignal = false;
	private DecimalFormat format = new DecimalFormat("0.#E00");
	protected DSSignificanceResultSet<DSGeneMarker> significanceResultSet = null;

	private JPopupMenu popupMenu;
	private JMenuItem imageSnapshotItem;
	protected Composite comp = null;
    protected AlphaComposite hltcomp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

    @Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		paint(g, DEFAULTRES, true);
	}

	/* used only by ColorMosaicPanel */
	void setSignificanceResultSet(
			DSSignificanceResultSet<DSGeneMarker> significanceResultSet) {
		this.significanceResultSet = significanceResultSet;
		recomputeDimensions();
	}

	/* used only by ColorMosaicPanel */
	void clearSignificanceResultSet() {
		significanceResultSet = null;
		recomputeDimensions();
	}

	/* used only by ColorMosaicPanel */ 
	void paint(Graphics g, int res, boolean screenMode) {
		if (res != oldRes) {
			resolution = res;
			recomputeDimensions();
			oldRes = res;
		}
		g.setFont(labelFont);
		comp = ((Graphics2D)g).getComposite();

		int row = 0;
		markerList.clear();
		for (int patId = 0; patId < clusterNo; patId++) {
			row += showCluster(g, cluster[patId], row, screenMode);
		}
		resolution = DEFAULTRES;
	}

	private EisenBlock currentCluster = null;

	protected int maxDisplayMarker = 0;
	protected int maxDisplayArray = 0;
	private String searchAccession = null;
	private String searchLabel = null;
	protected String searchArray = null;
	private int selectedLabel = -1;
	public void setSelectedLabel(int i, String searchStr) {
		selectedLabel = i;
		searchLabel = searchStr;
	}
	public int getSelectedLabel() {
		return selectedLabel;
	}
	private int selectedAccession = -1;
	public void setSelectedAccession(int i, String searchStr) {
		selectedAccession = i;
		searchAccession = searchStr;
	}
	public int getSelectedAccession() {
		return selectedAccession;
	}
	private int selectedArray = -1;
	public void setSelectedArray(int i, String searchStr) {
		selectedArray = i;
		searchArray = searchStr;
	}
	public int getSelectedArray() {
		return selectedArray;
	}
	public int getVWidth() {
		return getRequiredWidth()-getVisibleRect().width;
	}
	public int getVHeight() {
		return getRequiredHeight()-getVisibleRect().height;
	}

	protected List<DSGeneMarker> markerList = new ArrayList<DSGeneMarker>();
	private int showCluster(Graphics g, EisenBlock cluster, int row,
			boolean screenMode) {
		Rectangle visibleRect = getVisibleRect();
		maxDisplayMarker = visibleRect.height / geneHeight;
		maxDisplayArray = visibleRect.width / geneWidth;

		currentCluster = cluster;
		int geneNo = cluster.getMarkerNo();
		int chipNo = getChipNo();
		int startIndex;
		if (screenMode) {
			startIndex = visibleRect.y / geneHeight - row - 1;
		} else {
			startIndex = 0;
		}
		int stopIndex;
		if (screenMode) {
			stopIndex = (visibleRect.y + visibleRect.height) / geneHeight - row
					+ 1;
		} else {
			stopIndex = geneNo;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		if (stopIndex > geneNo) {
			stopIndex = geneNo;
		}
		if (this.significanceResultSet!=null) {
			for (int i = 0; i < geneNo; i++) {
				DSGeneMarker stats = cluster.getGeneLabel(i);
				 markerList.add(stats);
			}
		}
		for (int i = startIndex; i < stopIndex; i++) {
			DSGeneMarker stats = cluster.getGeneLabel(i);
			if(stats==null)continue;
			
			DSGeneMarker mkInfo = microarraySet.getMarkers().get(
					stats.getSerial());
			org.geworkbench.bison.util.colorcontext.ColorContext colorContext = (ColorContext) microarraySet
					.getObject(ColorContext.class);
			int y = (row + i) * geneHeight;

			for (int j = 0; j < chipNo; j++) {
				DSMicroarray pl = getPhenoLabel(j);
				if (pl instanceof DSMicroarray) {
					DSMicroarray mArray = (DSMicroarray) pl;
					int x = (j * geneWidth) / 1;
					int width = ((j + 1) * geneWidth) / 1 - x;
					DSMutableMarkerValue marker = (DSMutableMarkerValue) mArray
							.getMarkerValue(stats.getSerial());
					Color color = colorContext.getMarkerValueColor(marker,
							mkInfo, (float) intensity);
					g.setColor(color);
					g.fillRect(x, y, width, geneHeight);
					if (parent.levelTwoIds != null && parent.levelTwoIds[stats.getSerial()][j] > 0)
					{
						Graphics2D g2 = (Graphics2D) g;
					    g.setColor(Color.black);
					    Stroke oldStroke = g2.getStroke();
					    g2.setStroke(new BasicStroke(2));
					    g2.drawRect(x, y, width, geneHeight);
					    g2.setStroke(oldStroke);
					}
					if (j == 0) {
						
						g.setColor(Color.black);
						g.fillRect(x, y, 2, geneHeight);					 
					} else if (j == chipNo - 1) {
						g.setColor(Color.black);
						g.fillRect(x + geneWidth - 1, y, 2, geneHeight);
					} else if ((microarrayPanel != null)
							&& microarrayPanel.isBoundary(j - 1)
							&& !showAllMArrays) {
						g.setColor(Color.black);
						g.fillRect(x / 1 - 2, y, 2, geneHeight);
					}
				}
			}

			int xLabel = (chipNo * geneWidth) / 1 + 4;
			int yLabel = (row + i + 1) * geneHeight - (geneHeight - fontSize)
					/ 2;
			print(g, xLabel, yLabel, mkInfo, 
					i==selectedAccession && mkInfo.getLabel().toLowerCase().indexOf(searchAccession)>=0, 
					i==selectedLabel && mkInfo.getShortName().toLowerCase().indexOf(searchLabel)>=0);
			if (i == 0) {
				g.setColor(Color.black);
				int y0 = 0;
				int x0 = 0;
				int x1 = (chipNo * geneWidth) / 1 - 1;
				g.fillRect(x0, y0, x1 - x0, 2);
			} else if (i == geneNo - 1) {
				g.setColor(Color.black);
				int y0 = (row + i + 1) * geneHeight;
				int x0 = 0;
				int x1 = (chipNo * geneWidth) / 1 - 1;
				g.fillRect(x0, y0, x1 - x0, 2);
			} else if ((cluster.getPanel() != null)
					&& cluster.getPanel().isBoundary(i - 1) && !showAllMarkers) {
				g.setColor(Color.black);
				int y0 = (row + i) * geneHeight - 2;
				int x0 = 0;
				int x1 = (chipNo * geneWidth) / 1 - 1;
				g.fillRect(x0, y0, x1 - x0, 2);
			}
		} // loop of i
		// Draw the bottom right corner of the boundary box.
		int x0 = chipNo * geneWidth - 1;
		int y0 = (row + geneNo) * geneHeight;
		g.setColor(Color.black);
		g.fillRect(x0, y0, 2, 2);
		return cluster.getMarkerNo();
	}

	/* The behavior of this class is basically asynchronous and not thread safe.
	 * A synchronous design is preferred. TODO */
	protected DSMicroarray getPhenoLabel(int j) {
		DSMicroarray mArray = null;
		// the exceptions are expected because these fields are not synchronous
		try {
			if ((showAllMArrays || (microarrayPanel == null) || (microarrayPanel
					.size() == 0)) && microarraySet != null) {
				mArray = microarraySet.get(j);
			} else {
				mArray = microarrayPanel.get(j);
			}
		} catch (IndexOutOfBoundsException e) {
			log.debug(e);
		} catch (NullPointerException e) {
			log.debug(e);
		}
		return mArray;
	}

	protected int getChipNo() {
		int chipNo=0;
		if ((showAllMArrays || (microarrayPanel == null)
				|| (microarrayPanel.size() == 0)) && microarraySet!= null) {
			chipNo = microarraySet.size();
		} else if (microarrayPanel!=null){
			chipNo = microarrayPanel.size();
		}
		return chipNo;
	}

	public ColorMosaicImage() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {
		this.setBackground(Color.white);
		this.setMinimumSize(new Dimension(200, 300));
		this.setPreferredSize(new Dimension(438, 300));
		this.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseExited(MouseEvent e) {
				this_mouseExited(e);
			}

			public void mouseClicked(MouseEvent e) {
				this_mouseClicked(e);
			}
		});
		this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				this_mouseMoved(e);
			}
		});
		this.setLayout(borderLayout1);
		popupMenu = new JPopupMenu();
		imageSnapshotItem = new JMenuItem("Image Snapshot");
		popupMenu.add(imageSnapshotItem);
		imageSnapshotItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.createImageSnapshot();
			}
		});
	}

	protected void this_mouseMoved(MouseEvent e) {
		markerId = this.getGeneId(e);
		microarrayId = this.getChipId(e);
		if ((markerId >= 0) && (microarrayId >= 0)) {
			if ((newMarkerProxy != markerProxy)
					|| (newMArrayProxy != microarrayProxy)) {
				Graphics g = this.getGraphics();
				if (microarrayProxy >= 0) {
					drawCell(microarrayProxy, markerProxy, g);
				}
				drawCell(newMArrayProxy, newMarkerProxy, g);
				if (newMArrayProxy != microarrayProxy) {
					microarrayProxy = newMArrayProxy;
					markerProxy = newMarkerProxy;
				} else if (newMarkerProxy != markerProxy) {
					markerProxy = newMarkerProxy;
				}

				/*
				 * remove the tool tip code from drawcell and put it here. the
				 * indexes used in drawcell are just wrong for this purpose
				 */
				if (showSignal) {
					DSGeneMarker mInfo = microarraySet.getMarkers().get(
							markerId);
					DSMicroarray marray = microarraySet.get(microarrayId);
					if (parent.variableNames != null)
					{
						String toolTipText = "<html>" + parent.variableNames[0]+ ": "+ marray.getLabel()
						+ "<br>" + parent.variableNames[1] + ": " + mInfo.getLabel()
						+ "<br>" + "Score: "
						+ marray.getMarkerValue(mInfo).getValue() 
						+ "<br>" + "P-value: " + parent.pValues[markerId][microarrayId]
						+ "</html>";
				        setToolTipText(toolTipText);
					}
					else
					{
					    String toolTipText = "<html>Chip: " + marray.getLabel()
							+ "<br>" + "Marker: " + mInfo.getLabel()
							+ "<br>" + "Signal: "
							+ marray.getMarkerValue(mInfo).getValue()
							+ "</html>";
					    setToolTipText(toolTipText);
					}
				} else {
					this.setToolTipText(null);
				}

			}
		} else {
			if ((markerProxy != -1) && (microarrayProxy != -1)) {
				Graphics g = this.getGraphics();
				drawCell(microarrayProxy, markerProxy, g);
				markerProxy = -1;
				microarrayProxy = -1;
			}
		}
	}

	/*
	 * This method is only called in COlorMosaicPanel.
	 */
	void setChips(DSMicroarraySet chips) {
		microarraySet = chips;
		clearPatterns();
	}

	/* used only by ColorMOsaicPanel */
	void setSignal(boolean b) {
		this.showSignal = b;
	}

	/* used only by ColorMosaicPanel */
	DSMicroarraySet getChips() {
		return microarraySet;
	}

	/* only used by CMHRuler */
	int getRequiredWidth() {
		if (microarraySet == null) {
			return 0;
		}
		int width = 0;
		if (isPrintLabels) {
			width += labelGutter;
			if (isPrintRatio) {
				width += ratioWidth;
			}
			if (isPrintPValue) {
				width += pValueWidth;
			}
			if (isPrintAccession) {
				width += accessionWidth;
			}
			if (isPrintDescription) {
				width += labelWidth;
			}
		}
		int chipNo = getChipNo();
		return ((chipNo * geneWidth) / 1 + width + 2 * gutter);
	}

	/* used only by CMVRuler */
	int getRequiredHeight() {
		if (microarraySet == null) {
			return 0;
		}
		return (geneNo * geneHeight + 2 * gutter);
	}

	/* used only by ColorMosaicPanel */
	void addPattern(DSMatrixPattern pattern)
			throws ArrayIndexOutOfBoundsException {
		if (clusterNo < maxClusterNo) {
			cluster[clusterNo] = new EisenBlock(pattern, markerPanel,
					microarraySet);
			cluster[clusterNo].showAllMarkers(showAllMarkers);
			geneNo += cluster[clusterNo].getMarkerNo();
			clusterNo++;
			recomputeDimensions();
		} else {
			throw new ArrayIndexOutOfBoundsException();
		}
	}

	private void setSize() {
		maxUnitIncrement = geneHeight;
		if (microarraySet != null) {
			Dimension preferredSize = new Dimension(getRequiredWidth(),
					getRequiredHeight());
			this.setPreferredSize(preferredSize);
			this.revalidate();
		} else {
			this.setPreferredSize(new Dimension(0, 0));
			this.revalidate();
		}
	}

	/* used only ColorMosaicPanel */
	void clearPatterns() {
		for (int i = 0; i < clusterNo; i++) {
			cluster[i] = null;
		}
		clusterNo = 0;
		geneNo = 0;
		recomputeDimensions();
		repaint();
	}

	/*
	 * This method is to make a visual hint that the mouse is moving.
	 */
	private void drawCell(int expId, int geneId, Graphics g) {
		g.setXORMode(Color.black);
		g.setColor(Color.white);
		int x = (expId * geneWidth) / 1;
		int width = ((expId + 1) * geneWidth) / 1 - x;
		int y = geneId * geneHeight;
		g.drawLine(x, y, x, y + geneHeight - 1);
		g.drawLine(x, y, x + width - 1, y);
		g.setXORMode(Color.white);
		g.setColor(Color.black);
		g.drawLine(x, y + geneHeight - 1, x + width - 1, y + geneHeight - 1);
		g.drawLine(x + width - 1, y, x + width - 1, y + geneHeight - 1);
		//Graphics2D g2 = (Graphics2D) g;
		//g2.setStroke(new BasicStroke(11));
		this.revalidate();
		this.repaint();
	}

	private void this_mouseExited(MouseEvent e) {
		Graphics g = this.getGraphics();

		drawCell(microarrayProxy, markerProxy, g);
		markerProxy = -1;
		microarrayProxy = -1;
	}

	/* used only ColorMosaicPanel */
	DSMicroarraySet getGeneChips() {
		return microarraySet;
	}

	private int maxUnitIncrement = 1;

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		// Get the current position.
		int currentPosition = 0;
		if (orientation == SwingConstants.HORIZONTAL)
			currentPosition = visibleRect.x;
		else
			currentPosition = visibleRect.y;

		// Return the number of pixels between currentPosition
		// and the nearest tick mark in the indicated direction.
		if (direction < 0) {
			int newPosition = currentPosition
					- (currentPosition / maxUnitIncrement) * maxUnitIncrement;
			return (newPosition == 0) ? maxUnitIncrement : newPosition;
		} else {
			return ((currentPosition / maxUnitIncrement) + 1)
					* maxUnitIncrement - currentPosition;
		}
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL)
			return visibleRect.width - maxUnitIncrement;
		else
			return visibleRect.height - maxUnitIncrement;
	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	/* used only in CMVRuler */
	int getFontSize() {
		return Math.min(Math.max(geneHeight, 6), 15);
	}

	/* only used by ColorMosaicPanel */
	void setGeneHeight(int h) {
		geneHeight = h;
		recomputeDimensions();
	}

	/* only used by ColorMosaicPanel */
	void setGeneWidth(int w) {
		geneWidth = w;
		recomputeDimensions();
	}

	protected void this_mouseClicked(MouseEvent e) {
		if (e.isMetaDown()) {
			popupMenu.show(this, e.getX(), e.getY());
		}

		int geneId = getGeneId(e);
		if (geneId != -1 && geneId < microarraySet.getMarkers().size()) {
			DSGeneMarker marker = microarraySet.getMarkers().get(geneId);
			MarkerSelectedEvent mse = new org.geworkbench.events.MarkerSelectedEvent(
					marker);
			parent.publishMarkerSelectedEvent(mse);
		}
		if (microarrayId < 0)  return;
		DSMicroarray microarray = microarraySet.get(microarrayId);
		if (microarray!=null) {
			PhenotypeSelectedEvent pse = new PhenotypeSelectedEvent(microarray);
			parent.publishPhenotypeSelectedEvent(pse);
		}
		
		if ( e.getClickCount() == 2 && parent.levelTwoIds != null && parent.levelTwoIds[geneId][microarrayId] > 0 )
		{
			parent.createTitrationCurve(parent.levelTwoIds[geneId][microarrayId]);
 
		}
			
		
	}

	protected int getChipId(MouseEvent e) {
		int x = e.getX();
		int chipProxyId = (x - gutter) * 1 / geneWidth;

		int chipNo = getChipNo();
		if (chipProxyId >= 0 && chipProxyId < chipNo) {
			newMArrayProxy = chipProxyId;
		} else {
			newMArrayProxy = -1;
			return -1;
		}
		DSMicroarray pl = getPhenoLabel(newMArrayProxy);
		return pl.getSerial();
	}

	protected int getGeneId(MouseEvent e) {
		int y = e.getY();
		int geneProxyId = (y - gutter) / geneHeight;
		if (geneProxyId >= 0) {
			int clusterId = 0;
			int geneNo = 0;
			int realGeneId = -1;
			if (clusterId < clusterNo) {
				while (geneProxyId >= geneNo + cluster[clusterId].getMarkerNo()) {
					geneNo += cluster[clusterId].getMarkerNo();
					clusterId++;
					if (clusterId >= clusterNo) {
						newMarkerProxy = -1;
						return -1;
					}
				}
				newMarkerProxy = geneProxyId;
				DSGeneMarker stats = cluster[clusterId]
						.getGeneLabel(geneProxyId - geneNo);
				realGeneId = stats.getSerial();
			} else {
				newMarkerProxy = -1;
				return -1;
			}
			newMarkerProxy = geneProxyId;
			return realGeneId;
		}
		return 0;
	}

	/* used in CMHRuler and ColorMosaicPanel */
	DSPanel<DSMicroarray> getMArrayPanel() {
		return microarrayPanel;
	}

	/* used in CMVRuler and ColorMosaicPanel */
	DSPanel<DSGeneMarker> getPanel() {
		return markerPanel;
	}

	/* used in CMVRuler and ColorMosaicPanel */
	EisenBlock[] getClusters() {
		return cluster;
	}

	/* used in CMVRuler and ColorMosaicPanel */
	int getClusterNo() {
		return clusterNo;
	}

	/* used only in ColorMosaicPanel */
	void setMarkerPanel(DSPanel<DSGeneMarker> panel) {
		markerPanel = panel;
		geneNo = 0;
		for (int i = 0; i < clusterNo; i++) {
			cluster[i].setPanel(panel);
			geneNo += cluster[i].getMarkerNo();
		}
		if (isDisplayable()) {
			recomputeDimensions();
		}
	}

	/* used only in ColorMosaicPanel */
	void setPanel(DSPanel<DSMicroarray> panel) {
		microarrayPanel = panel;
		if (isDisplayable()) {
			recomputeDimensions();
		}
	}

	/* used only in ColorMosaicPanel */
	void setIntensity(double intensity) {
		this.intensity = intensity;
		repaint();
	}

	private void print(Graphics g, int x, int y, DSGeneMarker stats, boolean selectedAccession, boolean selectedLabel) {
		if (isPrintLabels) {
			g.setColor(Color.black);
			if (isPrintRatio) {
				// String ratio = Formatter.format(eg.Ratio);
				// g.drawString(ratio, x, y);
				// x += RatioWidth;
			}
			if (isPrintPValue) {
				double pValue = currentCluster.getGenePValue(stats);
				if ((pValue == -1d) && (significanceResultSet != null)) {
					Double sig = significanceResultSet.getSignificance(stats);
					if (sig != null) {
						pValue = sig;
					}
				}
				String p = null;
				if (pValue == -1d) {
					p = " ";
				} else if (pValue <= 0f) {
					p = "< " + format.format(Float.MIN_VALUE);
				} else {
					p = format.format(pValue);
				}
				g.drawString(p, x, y);
				x += pValueWidth;
			}
			if (isPrintAccession) {
				String accession = stats.getLabel();
				if (accession == null) {
					accession = "Undefined";
				}
				if (selectedAccession) {
			        ((Graphics2D)g).setComposite(hltcomp);
			        g.setColor(Color.blue);
					g.fillRect(x, y - geneHeight, accessionWidth, geneHeight);
					((Graphics2D)g).setComposite(comp);
					g.setColor(Color.black);
				}
				g.drawString(accession, x, y);
				x += accessionWidth;
			}
			if (isPrintDescription) {
				String label = stats.getShortName();
				if (label == null) {
					label = "Undefined";
				}
				if (selectedLabel) {
			        ((Graphics2D)g).setComposite(hltcomp);
					g.setColor(Color.blue);
					g.fillRect(x, y - geneHeight, labelWidth, geneHeight);
					((Graphics2D)g).setComposite(comp);
					g.setColor(Color.black);
				}
				g.drawString(label, x, y);
			}
		}
	}

	void setPrintRatio(boolean flag) {
		isPrintRatio = flag;
		recomputeDimensions();
	}

	void setPrintAccession(boolean flag) {
		isPrintAccession = flag;
		recomputeDimensions();
	}

	void setPrintDescription(boolean flag) {
		isPrintDescription = flag;
		recomputeDimensions();
	}

	void setFont() {
		int fontSize = Math.min(getFontSize(), (int) ((double) MAXFONTSIZE
				/ (double) DEFAULTRES * (double) resolution));
		if ((fontSize != this.fontSize) || (labelFont == null)) {
			this.fontSize = fontSize;
			labelFont = new Font("Times New Roman", Font.PLAIN, this.fontSize);
		}
	}

	private void recomputeDimensions() {
		Graphics2D g = (Graphics2D) this.getGraphics();
		if (g == null) {
			// Not visible
			return;
		}
		Rectangle2D rect = null;
		accessionWidth = 0;
		ratioWidth = 0;
		labelWidth = 0;
		pValueWidth = 0;
		geneNo = 0;
		setFont();
		for (int patId = 0; patId < clusterNo; patId++) {
			EisenBlock cl = cluster[patId];
			if(cl==null) continue; // necessary only because of asynchronous behavior
			int geneNumber = cl.getMarkerNo();

			geneNo += geneNumber;
			for (int i = 0; i < geneNumber; i++) {
				DSGeneMarker stats = cl.getGeneLabel(i);
				if(stats==null) continue; // necessary only because of asynchronous behavior

				String label = stats.getShortName();
				String accession = stats.getLabel();
				double pValue = cl.getGenePValue(stats);
				if ((pValue == -1d) && (significanceResultSet != null)) {
					Double sig = significanceResultSet.getSignificance(stats);
					if (sig != null) {
						pValue = sig;
					}
				}
				String p = "";

				ratioWidth = 0;
				if (pValue != -1d) {
					if (pValue <= 0f) {
						p = "< " + format.format(Float.MIN_VALUE);
					} else {
						p = format.format(pValue);
					}
					isPrintPValue = true;
					rect = labelFont.getStringBounds(p, g
							.getFontRenderContext());
					pValueWidth = Math.max(pValueWidth,
							(int) rect.getWidth() + 4);
				}
				if (accession != null) {
					rect = labelFont.getStringBounds(accession, g
							.getFontRenderContext());
					accessionWidth = Math.max(accessionWidth, (int) rect
							.getWidth());
				} else {
					accessionWidth = 0;
				}
				if (label == null)
					label = "Undefined";
				rect = labelFont.getStringBounds(label, g
						.getFontRenderContext());
				labelWidth = Math.max(labelWidth, (int) rect.getWidth());
			}
		}
		textSize = 0;
		if (isPrintLabels) {
			if (isPrintPValue) {
				textSize += pValueWidth;
			}
			if (isPrintRatio) {
				textSize += ratioWidth;
			}
			if (isPrintAccession) {
				textSize += accessionWidth;
			}
			if (isPrintDescription) {
				textSize += labelWidth;
			}
		}

		accessionWidth += 3;
		ratioWidth += 3;
		labelWidth += 0;
		setSize();
	}

	/* used only in ColorMosaicPanel */
	void setAutoWidth(double inches, int res) {
		if (res != oldRes) {
			resolution = res;
			recomputeDimensions();
			oldRes = res;
		}

		double width = inches * resolution;
		int chipNo = getChipNo();
		geneWidth = Math
				.min(
						40,
						(int) ((width - textSize - labelGutter) * 1.0 / (double) chipNo));
		geneHeight = 30;
		recomputeDimensions();
	}

	void setParent(BasicColorMosaicPanel parent) {
		this.parent = parent;
	}

	/* used only in ColorMosaicPanel */
	void showAllMArrays(boolean yes_no) {
		showAllMArrays = yes_no;
		recomputeDimensions();
		setSize();
	}

	/* used only in ColorMosaicPanel */
	void showAllMarkers(boolean yes_no) {
		showAllMarkers = yes_no;
		for (int i = 0; i < clusterNo; i++) {
			cluster[i].showAllMarkers(yes_no);
		}
		recomputeDimensions();
		setSize();
	}
	
    private static final HashMap<String, Integer> colorMap = new HashMap<String, Integer>();

    static {
        colorMap.put(CSAnnotationContext.CLASS_CASE, Color.RED.getRGB());
        colorMap.put(CSAnnotationContext.CLASS_CONTROL, Color.BLUE.getRGB());
        colorMap.put(CSAnnotationContext.CLASS_TEST, Color.GREEN.getRGB());
        colorMap.put(CSAnnotationContext.CLASS_IGNORE, Color.DARK_GRAY.getRGB());
    }

}
