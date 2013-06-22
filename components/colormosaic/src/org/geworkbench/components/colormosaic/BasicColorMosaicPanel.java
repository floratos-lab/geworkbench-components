package org.geworkbench.components.colormosaic;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.lincs.LincsDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.util.colorcontext.ColorContext;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.util.ColorScale;

/**
 * <p>
 * Title: Plug And Play
 * </p>
 * <p>
 * Description: Dynamic Proxy Implementation of enGenious
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: First Genetic Trust Inc.
 * </p>
 * 
 * @author Manjunath Kustagi
 * @version $Id: ColorMosaicPanel.java 10033 2012-10-12 19:14:13Z wangmen $
 */

@AcceptTypes({ DSMicroarraySet.class, DSSignificanceResultSet.class,
		LincsDataSet.class })
public class BasicColorMosaicPanel implements Printable, VisualPlugin,
		MenuListener {
	private static Log log = LogFactory.getLog(BasicColorMosaicPanel.class);

	protected JPanel mainPanel = new JPanel();
	protected JScrollPane jScrollPane = new JScrollPane();
	protected ColorMosaicImage colorMosaicImage = new ColorMosaicImage();
	protected JSlider jIntensitySlider = new JSlider();
	protected CMHRuler colRuler = new CMHRuler(colorMosaicImage);
	protected CMVRuler rowRuler = new CMVRuler(colorMosaicImage);
    protected HashMap<String, ActionListener> listeners = new HashMap<String, ActionListener>();
	protected boolean significanceMode = false;	
	protected ArrayList<DSGeneMarker> sortedMarkers = new ArrayList<DSGeneMarker>();
	protected ArrayList<DSGeneMarker> unsortedMarkers = new ArrayList<DSGeneMarker>();
	protected DSSignificanceResultSet<DSGeneMarker> sigSet = null;
	protected static final int GENE_HEIGHT = 10;
	protected static final int GENE_WIDTH = 20;
	protected enum searchBy {
		ARRAYNAME, ACCESSION, LABEL
	};

	protected boolean isDisplay = false;
    
	//this may not the good way to implement it 
	protected long[][] levelTwoIds = null;
	protected double[][] pValues = null;
	protected String[] variableNames = null;
	
	private JPanel jPanel1 = new JPanel();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JSpinner jGeneWidthSlider = new JSpinner(new SpinnerNumberModel(20,
			1, 100, 1));
	private JSpinner jGeneHeightSlider = new JSpinner(new SpinnerNumberModel(
			10, 1, 100, 1));
	private JLabel jLabel1 = new JLabel();
	private JLabel jLabel2 = new JLabel();	
	private JLabel jLabel5 = new JLabel();
	protected JButton exportButton = new JButton("Export Data");
	private BorderLayout borderLayout2 = new BorderLayout();	
	private JPopupMenu jCMMenu = new JPopupMenu();
	private JMenuItem jZoomInItem = new JMenuItem();
	private JMenuItem jZoomOutItem = new JMenuItem();
	private JMenuItem jPrintItem = new JMenuItem();
	private JMenuItem jSnapshotItem = new JMenuItem();
	protected JMenuItem jExportItem = new JMenuItem();
	private DSSignificanceResultSet<DSGeneMarker> significance = null;
	
	private List<DSGeneMarker> markerSet = null;

	private static final double displayXFactor = 2 / 3.0;

	public BasicColorMosaicPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void setChips(DSMicroarraySet chips) {
		if (chips != null) {
			colorMosaicImage.setChips(chips);
			mainPanel.repaint();
		}
	}

	private void setSignificance(DSSignificanceResultSet<DSGeneMarker> sigSet) {
		colorMosaicImage.setSignificanceResultSet(sigSet);
	}

	protected ColorScale colorScale = new ColorScale(Color.gray, Color.gray,
			Color.gray);

	private void jbInit() throws Exception {
		mainPanel.setBackground(Color.WHITE);
		colRuler.setPreferredWidth(1000);
		rowRuler.setPreferredSize(new Dimension(20, 1000));

		mainPanel.setMinimumSize(new Dimension(200, 300));
		mainPanel.setPreferredSize(new Dimension(438, 300));
		mainPanel.setLayout(borderLayout2);

		jPanel1.setLayout(gridBagLayout1);
		jLabel1.setText("Gene Height");
		jLabel2.setText("Gene Width");

		colorMosaicImage.setGeneHeight(10);
		colorMosaicImage.setParent(this);
		jGeneHeightSlider
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						jGeneHeightSlider_stateChanged(e);
					}
				});

		colorMosaicImage.setGeneWidth(20);
		jGeneWidthSlider
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						jGeneWidthSlider_stateChanged(e);
					}
				});
		jIntensitySlider
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						jIntensitySlider_stateChanged(e);
					}
				});
		jIntensitySlider.setPaintTicks(true);
		jIntensitySlider.setValue(100);
		jIntensitySlider.setMinorTickSpacing(2);
		jIntensitySlider.setMinimum(1);
		jIntensitySlider.setMaximum(200);
		jIntensitySlider.setMajorTickSpacing(50);
		jLabel5.setText("Intensity");

		jScrollPane.getViewport().setBackground(Color.white);

		exportButton.setMargin(new Insets(2, 3, 2, 3));
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (significance != null) {
					significance.saveDataToCSVFile();
				} else
					JOptionPane
							.showMessageDialog(
									null,
									"No significance data to export. Please do this after you got significance data.",
									"Operation failed",
									JOptionPane.ERROR_MESSAGE);
			}
		});
		exportButton.setEnabled(false);
		jExportItem.setEnabled(false);

		mainPanel.add(jScrollPane, BorderLayout.CENTER);
		JToolBar jToolBar2 = new JToolBar();
		jToolBar2.add(exportButton, null);
		jToolBar2.addSeparator();
		jToolBar2.add(jLabel5, null);
		jToolBar2.add(jIntensitySlider);
		jToolBar2.add(colorScale, null);
		jToolBar2.add(jLabel1, null);
		jToolBar2.add(jGeneHeightSlider, null);
		jToolBar2.addSeparator();
		jToolBar2.add(jLabel2, null);
		jToolBar2.add(jGeneWidthSlider, null);
		mainPanel.add(jToolBar2, BorderLayout.SOUTH);
		jScrollPane.getViewport().add(colorMosaicImage, null);
		jScrollPane.setColumnHeaderView(colRuler);
		jScrollPane.setRowHeaderView(rowRuler);
		jScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, new JComponent() {
			private static final long serialVersionUID = 9052547911998188349L;

			public void paint(Graphics g) {
				g.setColor(Color.WHITE);
				Rectangle clip = g.getClipBounds();
				g.fillRect(clip.x, clip.y, clip.width, clip.height);
			}
		});

		colorMosaicImage.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3)
					jCMMenu.show(colorMosaicImage, e.getX(), e.getY());
			}
		});
		jPrintItem.setText("Print...");
		ActionListener listenerPrint = new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printBtn_actionPerformed(e);
			}
		};
		jPrintItem.addActionListener(listenerPrint);
		jCMMenu.add(jPrintItem);

		jCMMenu.addSeparator();

		jZoomInItem.setText("Zoom In");
		ActionListener listenerZoomIn = new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int h = ((Integer) jGeneHeightSlider.getValue()).intValue();
				int w = ((Integer) jGeneWidthSlider.getValue()).intValue();
				jGeneHeightSlider.setValue(h + 1);
				jGeneWidthSlider.setValue(w + 1);
			}
		};
		jZoomInItem.addActionListener(listenerZoomIn);
		jCMMenu.add(jZoomInItem);

		jZoomOutItem.setText("Zoom Out");
		ActionListener listenerZoomOut = new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int h = ((Integer) jGeneHeightSlider.getValue()).intValue();
				int w = ((Integer) jGeneWidthSlider.getValue()).intValue();
				if (h > 1)
					jGeneHeightSlider.setValue(h - 1);
				if (w > 1)
					jGeneWidthSlider.setValue(w - 1);
			}
		};
		jZoomOutItem.addActionListener(listenerZoomOut);
		jCMMenu.add(jZoomOutItem);

		jCMMenu.addSeparator();

		jSnapshotItem.setText("Take Snapshot");
		ActionListener listenerjSnapshotItem = new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createImageSnapshot();
			}
		};
		jSnapshotItem.addActionListener(listenerjSnapshotItem);
		jCMMenu.add(jSnapshotItem);

		jExportItem.setText("Export significance value");
		ActionListener listenerExportItem = new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (significance != null) {
					significance.saveDataToCSVFile();
				} else
					JOptionPane
							.showMessageDialog(
									null,
									"No significance data to export. Please do this after you got significance data.",
									"Operation failed",
									JOptionPane.ERROR_MESSAGE);
			}
		};
		jExportItem.addActionListener(listenerExportItem);
		jCMMenu.add(jExportItem);
	}

	protected void revalidate() {
		if (colorMosaicImage.isDisplayable()) {
			mainPanel.revalidate();
			if (rowRuler != null) {
				rowRuler.revalidate();
				rowRuler.repaint();
			}
			if (colRuler != null) {
				colRuler.revalidate();
				colRuler.repaint();
			}
		}
	}

	protected void resetColorMosaicImage(int geneHeight, int geneWidth,
			boolean printRatio, boolean printAccession, boolean printDesc) {
		colorMosaicImage.setGeneHeight(geneHeight);
		colorMosaicImage.setGeneWidth(geneWidth);
		colorMosaicImage.setPrintRatio(printRatio);
		colorMosaicImage.setPrintAccession(printAccession);
		colorMosaicImage.setPrintRatio(printRatio);
		colorMosaicImage.setMarkerPanel(null);
		colorMosaicImage.setPanel(null);
		colorMosaicImage.setChips(null);
	}

	private void jGeneHeightSlider_stateChanged(ChangeEvent e) {
		int h = ((Integer) jGeneHeightSlider.getValue()).intValue();
		colorMosaicImage.setGeneHeight(h);
		revalidate();
	}

	private void jGeneWidthSlider_stateChanged(ChangeEvent e) {
		int w = ((Integer) jGeneWidthSlider.getValue()).intValue();
		colorMosaicImage.setGeneWidth(w);
		revalidate();
	}

	private void jIntensitySlider_stateChanged(ChangeEvent e) {
		double v = (double) jIntensitySlider.getValue() / 100.0;
		if (v > 1) {
			colorMosaicImage.setIntensity(1 + Math.exp(v) - Math.exp(1.0));
		} else {
			colorMosaicImage.setIntensity(v);
		}
	}

	/* invoke from EDT only */
	protected void display_actionPerformed() {
		if (colorMosaicImage.isDisplayable()) {
			if (isDisplay) {				 
			   displayMosaic();				 
			} else {
				clearMosaic();
			}
			if ((colorMosaicImage.getPanel() != null)
					&& (colorMosaicImage.getPanel().size() > 0)) {
				colorMosaicImage.showAllMarkers(false);
			}
			if ((colorMosaicImage.getMArrayPanel() != null)
					&& (colorMosaicImage.getMArrayPanel().size() > 0)) {
				colorMosaicImage.showAllMArrays(false);

			}
			if ((colorMosaicImage.getPanel() == null)
					|| (colorMosaicImage.getPanel().size() <= 0)) {
				colorMosaicImage.showAllMarkers(true);
			}
			if ((colorMosaicImage.getMArrayPanel() == null)
					|| (colorMosaicImage.getMArrayPanel().size() <= 0)) {
				colorMosaicImage.showAllMArrays(true);
			}
			revalidate();
		}
	}

	/*
	 * Enter: search forward starting from next item Ctl-B: search backwards
	 * from next item
	 */
	protected void searchText(KeyEvent e, searchBy type, String searchString) {
		char c = e.getKeyChar();
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			findNext(1, c, type, searchString);
		} else if (e.isControlDown() && (c == '\u0002')) {
			findNext(-1, c, type, searchString);
		}
	}

	private void findNext(int offset, char c, searchBy type, String searchString) {
		int index = 0;
		String markerString = searchString;
		if (type == searchBy.ACCESSION) {
			index = colorMosaicImage.getSelectedAccession();
		} else if (type == searchBy.LABEL) {
			index = colorMosaicImage.getSelectedLabel();
		} else if (type == searchBy.ARRAYNAME) {
			index = colorMosaicImage.getSelectedArray();
		}
		if (index < 0)
			index = 0;
		if (Character.isLetterOrDigit(c))
			markerString += Character.toLowerCase(c);
		int chipNo = colorMosaicImage.getChipNo();
		boolean found = false;
		if (type == searchBy.ARRAYNAME) {
			for (int idx = Math.abs(offset); idx <= chipNo; idx++) {
				int j = 0;
				if (offset < 0)
					j = (index + chipNo - idx) % chipNo;
				else
					j = (index + idx) % chipNo;
				DSMicroarray pl = colorMosaicImage.getPhenoLabel(j);
				if (pl instanceof DSMicroarray) {
					DSMicroarray mArray = (DSMicroarray) pl;
					String name = mArray.toString().toLowerCase();
					if (name.indexOf(markerString) >= 0) {
						colorMosaicImage.setSelectedArray(j, markerString);
						found = true;// searchArray.setForeground(Color.black);
						int xstart = (j - colorMosaicImage.maxDisplayArray / 2)
								* colorMosaicImage.geneWidth;
						if (xstart < 0)
							xstart = 0;
						else if (xstart > colorMosaicImage.getVWidth())
							xstart = colorMosaicImage.getVWidth();
						jScrollPane.getViewport().setViewPosition(
								new Point(xstart, 0));
						break;
					}
				}
			}
			if (!found || markerString.length() == 0) {
				if (!found)
					// searchArray.setForeground(Color.red);
					JOptionPane.showMessageDialog(null,
							"No match found for search term: " + markerString,
							"Warning", JOptionPane.WARNING_MESSAGE);
				colorMosaicImage.setSelectedArray(-1, null);
				jScrollPane.getViewport().setViewPosition(new Point(0, 0));
			}
			colRuler.repaint();
		} else {
			if (markerSet == null)
				return;

			List<DSGeneMarker> markers = markerSet;
			DSPanel<DSGeneMarker> mp = colorMosaicImage.getPanel();
			if (mp != null && mp.size() > 0)
				markers = mp;

			int markerNo = markers.size();
			if (colorMosaicImage.significanceResultSet != null)
				markerNo = colorMosaicImage.markerList.size();
			found = false;
			int xstart = colorMosaicImage.getVWidth();
			if (xstart < 0)
				xstart = 0;
			if (xstart > 0) {
				int cstartmax = chipNo
						- (int) (colorMosaicImage.maxDisplayArray * displayXFactor);
				if (cstartmax < 0)
					cstartmax = 0;
				int xstartmax = cstartmax * colorMosaicImage.geneWidth;
				if (xstart > xstartmax)
					xstart = xstartmax;
			}
			for (int idx = Math.abs(offset); idx <= markerNo; idx++) {
				int i = 0;
				if (offset < 0)
					i = (index + markerNo - idx) % markerNo;
				else
					i = (index + idx) % markerNo;
				DSGeneMarker marker = markers.get(i);
				if (colorMosaicImage.significanceResultSet != null)
					marker = colorMosaicImage.markerList.get(i);
				String name = "";
				if (type == searchBy.ACCESSION)
					name = marker.getLabel().toLowerCase();
				else if (type == searchBy.LABEL)
					name = marker.getShortName().toLowerCase();
				if (name.indexOf(markerString) >= 0) {
					if (type == searchBy.ACCESSION)
						colorMosaicImage.setSelectedAccession(i, markerString);
					else if (type == searchBy.LABEL)
						colorMosaicImage.setSelectedLabel(i, markerString);
					found = true;
					int ystart = (i - colorMosaicImage.maxDisplayMarker / 2)
							* colorMosaicImage.geneHeight;
					if (ystart < 0)
						ystart = 0;
					else if (ystart > colorMosaicImage.getVHeight())
						ystart = colorMosaicImage.getVHeight();
					jScrollPane.getViewport().setViewPosition(
							new Point(xstart, ystart));
					break;
				}
			}
			if (!found || markerString.length() == 0) {
				if (!found)
					JOptionPane.showMessageDialog(null,
							"No match found for search term: " + markerString,
							"Warning", JOptionPane.WARNING_MESSAGE);
				if (type == searchBy.ACCESSION)
					colorMosaicImage.setSelectedAccession(-1, null);
				else if (type == searchBy.LABEL)
					colorMosaicImage.setSelectedLabel(-1, null);
				jScrollPane.getViewport().setViewPosition(new Point(xstart, 0));
			}
			colorMosaicImage.repaint();
		}
	}

	private void clearMosaic() {
		colorMosaicImage.clearPatterns();
		colRuler.setClearDisplay(true);
	}

	protected void displayMosaic() {
		colorMosaicImage.clearPatterns();
		DSMicroarraySet mArraySet = colorMosaicImage.getGeneChips();
		if (mArraySet != null) {
			markerSet = mArraySet.getMarkers();
			int markerNo = markerSet.size();
			CSMatrixPattern thePattern = new CSMatrixPattern();
			thePattern.init(markerNo);
			for (int i = 0; i < markerNo; i++) {
				thePattern.markers()[i] = markerSet.get(i);
			}
			colorMosaicImage.addPattern(thePattern);

			org.geworkbench.bison.util.colorcontext.ColorContext colorContext = (org.geworkbench.bison.util.colorcontext.ColorContext) mArraySet
					.getObject(org.geworkbench.bison.util.colorcontext.ColorContext.class);
			colorScale.setMinColor(colorContext
					.getMinColorValue(jIntensitySlider.getValue()));
			colorScale.setCenterColor(colorContext
					.getMiddleColorValue(jIntensitySlider.getValue()));
			colorScale.setMaxColor(colorContext
					.getMaxColorValue(jIntensitySlider.getValue()));
			colorScale.repaint();
			colorMosaicImage.revalidate();
			colRuler.setClearDisplay(false);
		}
	}

	protected void printBtn_actionPerformed(ActionEvent e) {
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(this);
		if (printJob.printDialog()) {
			try {
				printJob.print();
			} catch (Exception PrinterExeption) {
				System.out.println("Exception: " + PrinterExeption);
			}
		}

	}

	@Override
	public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
		int[] res = { 600, 600, 3 };
		Graphics2D g2 = (Graphics2D) g;
		double scaleX = (double) res[0] / 72.0;
		double scaleY = (double) res[0] / 72.0;
		g2.scale(1.0 / scaleX, 1.0 / scaleY);
		g2.setStroke(new BasicStroke(1.0F));
		if (pi >= 1) {
			return Printable.NO_SUCH_PAGE;
		}
		double inches = 0.0;
		rowRuler.computeWidth(scaleY);

		int rowSpace = (int) rowRuler.getScaledWidth();
		int colSpace = (int) ((double) colRuler.getHeight() * scaleX);
		int r = 72;
		double d = pf.getImageableWidth();
		inches = d / (double) r - (double) rowSpace / (double) res[0];

		g2.translate(pf.getImageableX() * scaleX, pf.getImageableY() * scaleY);
		int w = colorMosaicImage.geneWidth;
		int h = colorMosaicImage.geneHeight;
		colorMosaicImage.setAutoWidth(inches, res[0]);
		System.out.print("Painting ... ");
		g2.translate(rowSpace, 0);
		colRuler.paint(g2, res[0]);
		g2.translate(-rowSpace, colSpace);
		rowRuler.paint(g2, res[0]);
		g2.translate(rowSpace, 0);
		colorMosaicImage.paint(g2, res[0], false);
		System.out.println("Done");
		colorMosaicImage.geneWidth = w;
		colorMosaicImage.geneHeight = h;
		return Printable.PAGE_EXISTS;
	}	

	private void setMarkerPanel(DSPanel<DSGeneMarker> panel) {
		if (panel != null) {
			colorMosaicImage.setMarkerPanel(panel);
			if (colorMosaicImage.isDisplayable()) {
				revalidate();
				colorMosaicImage.repaint();
			}
		}
	}

	private void setMicroarrayPanel(DSPanel<DSMicroarray> panel) {
		if (panel != null) {
			colorMosaicImage.setPanel(panel);
			if (colorMosaicImage.isDisplayable()) {
				revalidate();
				colorMosaicImage.repaint();
			}
		}
	}

	@Override
	public Component getComponent() {
		return mainPanel;
	}


	@Override
	public ActionListener getActionListener(String key) {
		return (ActionListener) listeners.get(key);
	}

	protected PhenotypeSelectorEvent<DSMicroarray> pse = null;

	@Subscribe
	public void receive(PhenotypeSelectorEvent<DSMicroarray> e, Object source) {
		pse = e;
		if (significanceMode || e == null) {
			return;
		}

		DSPanel<DSMicroarray> pl = e.getTaggedItemSetTree();
		setMicroarrayPanel(pl);
		display_actionPerformed();
	}

	protected GeneSelectorEvent gse = null;

	@Subscribe
	public void receive(GeneSelectorEvent e, Object source) {
		gse = e;
		if (significanceMode || e == null) {
			return;
		}
		DSPanel<DSGeneMarker> panel = e.getPanel();
		if (panel != null) {
			colorMosaicImage.pValueWidth = 18;
			colorMosaicImage.isPrintPValue = true;
			setMarkerPanel(panel);
			if (SwingUtilities.isEventDispatchThread()) {
				display_actionPerformed();
			} else {
				log.debug("non-EDT");
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						display_actionPerformed();
					}

				});
			}
		}
	}

	@Publish
	public MarkerSelectedEvent publishMarkerSelectedEvent(
			MarkerSelectedEvent event) {
		return event;
	}

	@Publish
	public PhenotypeSelectedEvent publishPhenotypeSelectedEvent(
			PhenotypeSelectedEvent event) {
		return event;
	}

	private Image getColorMosaicAsImage() {
		Dimension mainDim = colorMosaicImage.getPreferredSize();
		Dimension topDim = colRuler.getPreferredSize();
		Dimension leftDim = rowRuler.getPreferredSize();
		Dimension dim = new Dimension(mainDim.width + leftDim.width,
				mainDim.height + topDim.height);
		int w = dim.width;
		int h = dim.height;
		long size = w * h;
		final long MAX_SIZE = 100 * 1024 * 1024;
		if (size > MAX_SIZE) {
			JOptionPane.showMessageDialog(this.getComponent(),
					"Cannot create snapshot.\n" + "The requested snapshot is "
							+ w + "X" + h + " pixels, or about " + size
							/ 1000000 + " megapixels.\n"
							+ "The upper limit is 100 megapixels.");
			return null;
		}
		BufferedImage image;
		try {
			image = new BufferedImage(dim.width, dim.height,
					BufferedImage.TYPE_INT_RGB);
		} catch (OutOfMemoryError e) {
			JOptionPane.showMessageDialog(this.getComponent(),
					"OutOfMemoryError when the image's size is " + w + "X" + h);
			return null;
		}

		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, dim.width, dim.height);
		AffineTransform start = g.getTransform();
		// Draw the main image
		g.translate(leftDim.width, topDim.height);
		colorMosaicImage.paint(g, ColorMosaicImage.DEFAULTRES, false);
		// Draw the column ruler
		g.setTransform(start);
		g.translate(leftDim.width, 0);
		// g.setClip(leftDim.width, 0, mainDim.width, topDim.height);
		colRuler.paint(g);
		// Draw the row ruler
		g.setTransform(start);
		g.translate(0, topDim.height);

		rowRuler.paint(g);
		return image;
	}

	public void createImageSnapshot() {
		Image currentImage = getColorMosaicAsImage();
		if (currentImage == null)
			return;

		ImageIcon newIcon = new ImageIcon(currentImage, "Color Mosaic View");
		org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent(
				"Color Mosaic View", newIcon,
				org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
		publishImageSnapshotEvent(event);

	}

	@Publish
	public org.geworkbench.events.ImageSnapshotEvent publishImageSnapshotEvent(
			org.geworkbench.events.ImageSnapshotEvent event) {
		return event;
	}

	private static class TValueComparator implements Comparator<DSGeneMarker> {
		DSSignificanceResultSet<DSGeneMarker> significantResultSet;

		public TValueComparator(
				DSSignificanceResultSet<DSGeneMarker> significantResultSet) {
			this.significantResultSet = significantResultSet;
		}

		public int compare(DSGeneMarker x, DSGeneMarker y) {
			double tX = significantResultSet.getTValue(x);
			double tY = significantResultSet.getTValue(y);
			if (tX > tY) {
				return 1;
			} else if (tX < tY) {
				return -1;
			} else {
				return 0;
			}
		}

	}

	// this should be called only from EDT
	protected void refreshSignificanceResultView() {
		significance = sigSet;
		DSMicroarraySet set = sigSet.getParentDataSet();
		exportButton.setEnabled(true);
		jExportItem.setEnabled(true);
		// by default color mosaic displays unsorted markers
		if (colorMosaicImage.getChips() != set) {
			colorMosaicImage.setChips(set);
		}
		// // Make panels from the significance data and display only that
		// Pheno
		CSPanel<DSMicroarray> phenoPanel = new CSPanel<DSMicroarray>(
				"Phenotypes");
		DSItemList<DSPanel<DSMicroarray>> list = phenoPanel.panels();
		list.clear();
		DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager
				.getInstance().getCurrentContext(set);
		for (int i = 0; i < 2; i++) {
			String[] labels = sigSet.getLabels(i);
			for (int j = 0; j < labels.length; j++) {
				DSPanel<DSMicroarray> p = new CSPanel<DSMicroarray>(
						context.getItemsWithLabel(labels[j]));
				if (p != null) {
					p.setActive(true);
					phenoPanel.panels().add(p);
				}
			}
		}

		setMicroarrayPanel(phenoPanel);
		// Keep fixed marker ranges for significance resultset
		CSMicroarraySetView<DSGeneMarker, DSMicroarray> view = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(
				set);
		view.setItemPanel(phenoPanel);
		ColorContext colorContext = (ColorContext) set
				.getObject(ColorContext.class);
		if (colorContext != null)
			colorContext.updateContext(view);

		// Markers
		CSPanel<DSGeneMarker> genePanel = new CSPanel<DSGeneMarker>("Markers");
		sigSet.getSignificantMarkers().setActive(true);
		genePanel.panels().add(sigSet.getSignificantMarkers());
		setMarkerPanel(genePanel);
		sortByTValue(genePanel);
		// Force all arrays and all markers off
		colorMosaicImage.showAllMArrays(false);
		colorMosaicImage.showAllMarkers(false);	 
		setSignificance(sigSet);
		displayMosaic();
		revalidate();
		mainPanel.repaint();
	}

	 
	private void sortByTValue(DSItemList<DSGeneMarker> origMarkers) {
		if (significanceMode) {
			// create two marker lists: unsorted vs. sorted
			unsortedMarkers.clear();
			sortedMarkers.clear();
			for (DSGeneMarker m : origMarkers) {
				unsortedMarkers.add(m);
				sortedMarkers.add(m);
			}

			// sort first by fold changes
			Collections.sort(sortedMarkers, new FoldChangesComparator(sigSet));

			// break down the list of markers into positive and negative fold
			// changes
			ArrayList<DSGeneMarker> positiveFolds = new ArrayList<DSGeneMarker>();
			ArrayList<DSGeneMarker> negativeFolds = new ArrayList<DSGeneMarker>();
			for (DSGeneMarker m : sortedMarkers) {
				if (sigSet.getTValue(m) >= 0)
					positiveFolds.add(m);
				if (sigSet.getTValue(m) < 0)
					negativeFolds.add(m);
			}

			// sort each list by t-value
			Collections.sort(positiveFolds, new TValueComparator(sigSet));
			Collections.sort(negativeFolds, new TValueComparator(sigSet));

			// recombine lists
			sortedMarkers = positiveFolds;
			for (DSGeneMarker m : negativeFolds)
				positiveFolds.add(m);
		}
	}

	protected void printTValueAndPValue(DSPanel<DSGeneMarker> markersInUse,
			boolean sorted) {
		StringBuilder sb = new StringBuilder();

		for (DSGeneMarker m : markersInUse) {
			sb.append(m.getShortName());
			sb.append("\t");
			sb.append(sigSet.getTValue(m));
			sb.append("\t\t");
			sb.append(sigSet.getSignificance(m));
			sb.append("\n");
		}
		sb.trimToSize();

		StringBuilder headerSB = new StringBuilder();

		if (sorted)
			headerSB.append("Sorted markers:\n");
		else
			headerSB.append("Unsorted markers:\n");

		headerSB.append("marker\t\tt-value\t\tp-value\n");
		headerSB.trimToSize();

		log.debug(headerSB.toString() + sb.toString());
	}

	private static class FoldChangesComparator implements
			Comparator<DSGeneMarker> {
		DSSignificanceResultSet<DSGeneMarker> significantResultSet;

		public FoldChangesComparator(
				DSSignificanceResultSet<DSGeneMarker> significantResultSet) {
			this.significantResultSet = significantResultSet;
		}

		public int compare(DSGeneMarker x, DSGeneMarker y) {
			double foldX = significantResultSet.getFoldChange(x);
			double foldY = significantResultSet.getFoldChange(y);
			if (foldX > foldY) {
				return 1;
			} else if (foldX < foldY) {
				return -1;
			} else {
				return 0;
			}
		}

	}

	protected void disableExportItem() {
		exportButton.setEnabled(false);
		jExportItem.setEnabled(false);
	}

	protected class DocListener implements DocumentListener {
		private searchBy searchType;

		DocListener(searchBy type) {
			searchType = type;
		}

		public void insertUpdate(DocumentEvent e) {
			String searchString = "";
			try {
				searchString = e.getDocument().getText(0,
						e.getDocument().getLength());
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			findNext(0, '\u000E', searchType, searchString);
		}

		public void removeUpdate(DocumentEvent e) {
			String searchString = "";
			try {
				searchString = e.getDocument().getText(0,
						e.getDocument().getLength());
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			findNext(0, '\u000E', searchType, searchString);
		}

		public void changedUpdate(DocumentEvent e) {
		}
	}

}
