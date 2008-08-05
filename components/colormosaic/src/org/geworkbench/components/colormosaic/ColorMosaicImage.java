package org.geworkbench.components.colormosaic;

import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.clusters.MarkerHierCluster;
import org.geworkbench.bison.model.clusters.MicroarrayHierCluster;
import org.geworkbench.bison.util.colorcontext.ColorContext;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.geworkbench.util.associationdiscovery.cluster.CSMatchedMatrixPattern;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

/**
 * <p>Title: Plug And Play</p>
 * <p>Description: Dynamic Proxy Implementation of enGenious</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: First Genetic Trust Inc.</p>
 *
 * @author Manjunath Kustagi
 * @version 1.0
 */

public class ColorMosaicImage extends JPanel implements Scrollable {
    // Static Variables
    protected static DecimalFormat formatter = new DecimalFormat("##.##");
    protected static Font labelFont = null;
    final static int gutter = 4;
    final static int maxClusterNo = 64;
    final static int MAXFONTSIZE = 10;
    final static int DEFAULTRES = 120;

    // Instance Variables
    public int geneWidth = 20;
    public int geneHeight = 10;
    protected int newMArrayProxy = 0;
    protected int newMarkerProxy = 0;
    protected int markerProxy = -1;
    protected int microarrayProxy = -1;
    protected int markerId = -1;
    protected int microarrayId = -1;
    protected int geneNo = 0;
    protected int clusterNo = 0;
    protected EisenBlock[] cluster = new EisenBlock[maxClusterNo];
    protected DSMicroarraySet<DSMicroarray> microarraySet = null;
    BorderLayout borderLayout1 = new BorderLayout();
    //protected JMicroarrayManager ChipManager    = null;
    protected DSPanel<DSGeneMarker> markerPanel = null;
    protected DSPanel<DSMicroarray> microarrayPanel = null;
    protected boolean isAbsDisplay = false;
    protected boolean showPattern = false;
    protected boolean sortRows = false;
    protected boolean isPrintLabels = true;
    protected boolean isPrintRatio = true;
    protected boolean isPrintDescription = true;
    protected boolean isPrintAccession = true;
    protected boolean isPrintPValue = false;
    protected int ratioWidth = 0;
    protected int accessionWidth = 0;
    protected int pValueWidth = 0;
    protected int labelWidth = 0;
    protected int labelGutter = 5;
    protected double intensity = 1.0;
    protected boolean hideMasked = false;
    protected int resolution = DEFAULTRES;
    private int oldRes = DEFAULTRES;
    protected int fontSize = 0;
    protected int textSize = 0;
    protected ColorMosaicPanel parent = null;
    protected boolean showAllMArrays = true;
    protected boolean showAllMarkers = true;
    protected boolean showSignal = false;
    private DecimalFormat format = new DecimalFormat("0.#E00");
    private DSSignificanceResultSet<DSGeneMarker> significanceResultSet = null;

    private JPopupMenu popupMenu;
    private JMenuItem imageSnapshotItem;

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paint(g, DEFAULTRES, true);
    }

    public void setSignificanceResultSet(DSSignificanceResultSet<DSGeneMarker> significanceResultSet) {
        this.significanceResultSet = significanceResultSet;
        recomputeDimensions();
    }

    public void clearSignificanceResultSet() {
        significanceResultSet = null;
        recomputeDimensions();
    }

    public void paint(Graphics g, int res, boolean screenMode) {
        if (res != oldRes) {
            resolution = res;
            recomputeDimensions();
            oldRes = res;
        }
        g.setFont(labelFont);
        int row = 0;
        for (int patId = 0; patId < clusterNo; patId++) {
            cluster[patId].setFirstRow(row);
            row += showCluster(g, cluster[patId], row, screenMode);
        }
        resolution = DEFAULTRES;
    }

    private EisenBlock currentCluster = null;

    protected int showCluster(Graphics g, EisenBlock cluster, int row, boolean screenMode) {
        Rectangle visibleRect = getVisibleRect();
        // DSClassCriteria classCriteria = CSCriterionManager.getClassCriteria(microarraySet);
        DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(microarraySet);
        //        DSPanel<DSMicroarray> criterion = CSCriterionManager.getCriteria(microarraySet).getSelectedCriterion();
        int fontGutter = (int) ((double) geneHeight * .22);
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
            stopIndex = (visibleRect.y + visibleRect.height) / geneHeight - row + 1;
        } else {
            stopIndex = geneNo;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (stopIndex > geneNo) {
            stopIndex = geneNo;
        }
        for (int i = startIndex; i < stopIndex; i++) {
            DSGeneMarker stats = cluster.getGeneLabel(i);
            DSGeneMarker mkInfo = microarraySet.getMarkers().get(stats.getSerial());
            org.geworkbench.bison.util.colorcontext.ColorContext colorContext = (ColorContext) microarraySet.getObject(ColorContext.class);
            int y = (row + i) * geneHeight;
//            if (row + i > 500) {
//                return i;
//            }
            for (int j = 0; j < chipNo; j++) {
                DSMicroarray pl = getPhenoLabel(j);
                if (pl instanceof DSMicroarray) {
                    DSMicroarray mArray = (DSMicroarray) pl;
                    int x = (j * geneWidth) / 1;
                    int width = ((j + 1) * geneWidth) / 1 - x;
                    DSMutableMarkerValue marker = (DSMutableMarkerValue) mArray.getMarkerValue(stats.getSerial());
                    Color color = colorContext.getMarkerValueColor(marker, mkInfo, (float) intensity);
                    g.setColor(color);
                    g.fillRect(x, y, width, geneHeight);
                    if (j == 0) {
                        g.setColor(Color.black);
                        g.fillRect(x, y, 2, geneHeight);
                    } else if (j == chipNo - 1) {
                        g.setColor(Color.black);
                        g.fillRect(x + geneWidth - 1, y, 2, geneHeight);
                    } else if ((microarrayPanel != null) && microarrayPanel.isBoundary(j - 1) && !showAllMArrays) {
                        g.setColor(Color.black);
                        g.fillRect(x / 1 - 2, y, 2, geneHeight);
                    }
                }
            }
            for (int j = 0; j < chipNo; j++) {
                DSMicroarray pl = getPhenoLabel(j);
                if (pl instanceof DSMicroarray) {
                    DSMicroarray mArray = (DSMicroarray) pl;
                    if (showPattern) {
                        if (cluster.getPattern().getPattern().match(mArray).getPValue() < 1.0) {
                            int x = (j * geneWidth) / 1;
                            int width = ((j + 1) * geneWidth) / 1 - x;
                            String v = context.getClassForItem(mArray);
                            g.setColor(new Color(CSAnnotationContext.getRGBForClass(v)));
                            g.drawRect(x, y, width, geneHeight);
                        }
                    }
                }

            }
            int xLabel = (chipNo * geneWidth) / 1 + 4;
            int yLabel = (row + i + 1) * geneHeight - (geneHeight - fontSize) / 2;
            print(g, xLabel, yLabel, mkInfo);
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
            } else if ((cluster.getPanel() != null) && cluster.getPanel().isBoundary(i - 1) && !showAllMarkers) {
                g.setColor(Color.black);
                int y0 = (row + i) * geneHeight - 2;
                int x0 = 0;
                int x1 = (chipNo * geneWidth) / 1 - 1;
                g.fillRect(x0, y0, x1 - x0, 2);
            }
        }
        // Draw the bottom right corner of the boundary box.
        int x0 = chipNo * geneWidth - 1;
        int y0 = (row + geneNo) * geneHeight;
        g.setColor(Color.black);
        g.fillRect(x0, y0, 2, 2);
        return cluster.getMarkerNo();
    }

    private DSMicroarray getPhenoLabel(int j) {
        DSMicroarray mArray = null;
        if (showAllMArrays || (microarrayPanel == null) || (microarrayPanel.size() == 0)) {
            mArray = microarraySet.get(j);
        } else {
            if (j >= 0 && j < microarrayPanel.size()) {
                mArray = microarrayPanel.get(j);
            } else {
                System.out.println("What's up!");
            }
        }
        return mArray;
    }

    private int getChipNo() {
        int chipNo;
        if (showAllMArrays || (microarrayPanel == null) || (microarrayPanel.size() == 0)) {
            chipNo = microarraySet.size();
        } else {
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

    void this_mouseMoved(MouseEvent e) {
        markerId = this.getGeneId(e);
        microarrayId = this.getChipId(e);
        if ((markerId >= 0) && (microarrayId >= 0)) {
            if ((newMarkerProxy != markerProxy) || (newMArrayProxy != microarrayProxy)) {
                Graphics g = this.getGraphics();
                if (microarrayProxy >= 0) {
                    drawCell(microarrayProxy, markerProxy, g);
                }
                drawCell(newMArrayProxy, newMarkerProxy, g);
                if (newMArrayProxy != microarrayProxy) {
                    microarrayProxy = newMArrayProxy;
                    markerProxy = newMarkerProxy;
                    //***parent.notifyChange(IMicroarrayIdChangeSubscriber.class);
                    //***parent.notifyChange(IMarkerIdChangeSubscriber.class);
                } else if (newMarkerProxy != markerProxy) {
                    markerProxy = newMarkerProxy;
                    //***parent.notifyChange(IMarkerIdChangeSubscriber.class);
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

    void setChips(DSMicroarraySet chips) {
        microarraySet = chips;
        clearPatterns();
    }
    
    boolean getSignal(){
    	return this.showSignal;
    }
    
    void setSignal(boolean b){
    	this.showSignal = b;
    }

    public DSMicroarraySet<DSMicroarray> getChips() {
        return microarraySet;
    }

    public int getRequiredWidth() {
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

    public int getRequiredHeight() {
        if (microarraySet == null) {
            return 0;
        }
        return (geneNo * geneHeight + 2 * gutter);
    }

    void addPattern(CSMatchedMatrixPattern pattern) throws ArrayIndexOutOfBoundsException {
        if (clusterNo < maxClusterNo) {
            cluster[clusterNo] = new EisenBlock(pattern, markerPanel, microarraySet);
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
            Dimension preferredSize = new Dimension(getRequiredWidth(), getRequiredHeight());
            this.setPreferredSize(preferredSize);
            this.revalidate();
        } else {
            this.setPreferredSize(new Dimension(0, 0));
            this.revalidate();
        }
    }

    public void clearPatterns() {
        for (int i = 0; i < clusterNo; i++) {
            cluster[i] = null;
        }
        clusterNo = 0;
        geneNo = 0;
        recomputeDimensions();
        repaint();
    }

    void drawCell(int expId, int geneId, Graphics g) {
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
        
        if (showSignal) {        	
            DSGeneMarker mInfo = microarraySet.getMarkers().get(geneId);
            DSMicroarray marray = microarraySet.get(expId);

            this.setToolTipText("<html>Chip: " + marray.getLabel() + "<br>" + "Marker: " + mInfo.getLabel() + "<br>" + "Signal: " + marray.getMarkerValue(mInfo).getValue() + "</html>");
        } else {
        	this.setToolTipText(null);
        }
        
        this.revalidate();
        this.repaint();
    }

    void this_mouseExited(MouseEvent e) {
        Graphics g = this.getGraphics();
        //g.translate(Gutter - X,  Gutter - Y);
        drawCell(microarrayProxy, markerProxy, g);
        markerProxy = -1;
        microarrayProxy = -1;
    }

    public DSMicroarraySet getGeneChips() {
        return microarraySet;
    }

    private int maxUnitIncrement = 1;

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        //Get the current position.
        int currentPosition = 0;
        if (orientation == SwingConstants.HORIZONTAL)
            currentPosition = visibleRect.x;
        else
            currentPosition = visibleRect.y;

        //Return the number of pixels between currentPosition
        //and the nearest tick mark in the indicated direction.
        if (direction < 0) {
            int newPosition = currentPosition - (currentPosition / maxUnitIncrement) * maxUnitIncrement;
            return (newPosition == 0) ? maxUnitIncrement : newPosition;
        } else {
            return ((currentPosition / maxUnitIncrement) + 1) * maxUnitIncrement - currentPosition;
        }
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
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

    public void setMaxUnitIncrement(int pixels) {
        maxUnitIncrement = pixels;
    }

    public int getFontSize() {
        return Math.min(Math.max(geneHeight, 6), 15);
    }

    public void setGeneHeight(int h) {
        geneHeight = h;
        recomputeDimensions();
    }

    public void setGeneWidth(int w) {
        geneWidth = w;
        recomputeDimensions();
    }

    void this_mouseClicked(MouseEvent e) {
        if (e.isMetaDown()) {
            popupMenu.show(this, e.getX(), e.getY());
        }
        int x = e.getX();
        int y = e.getY();
        int expId = (x - gutter) * 1 / geneWidth;
        int geneId = getGeneId(e);
        if (geneId != -1 && geneId < microarraySet.getMarkers().size()) {            
        	DSGeneMarker marker = microarraySet.getMarkers().get(geneId);
            MarkerSelectedEvent mse = new org.geworkbench.events.MarkerSelectedEvent(marker);
            parent.publishMarkerSelectedEvent(mse);
        }
        if(expId != -1 && expId < microarraySet.size()){
        	DSMicroarray microarray = microarraySet.get(expId);
        	PhenotypeSelectedEvent pse = new PhenotypeSelectedEvent(microarray);
            parent.publishPhenotypeSelectedEvent(pse);
        }
    }

    int getChipId(MouseEvent e) {
        int x = e.getX();
        int chipProxyId = (x - gutter) * 1 / geneWidth;
        //if(chipProxyId < ChipManager.GetChipNo()) {
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

    int getGeneId(MouseEvent e) {
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
                DSGeneMarker stats = cluster[clusterId].getGeneLabel(geneProxyId - geneNo);
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

    public DSPanel<DSMicroarray> getMArrayPanel() {
        return microarrayPanel;
    }

    public DSPanel<DSGeneMarker> getPanel() {
        return markerPanel;
    }

    public EisenBlock[] getClusters() {
        return cluster;
    }

    public int getClusterNo() {
        return clusterNo;
    }

    public void setMarkerPanel(DSPanel<DSGeneMarker> panel) {
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

    public void setPanel(DSPanel<DSMicroarray> panel) {
        microarrayPanel = panel;
        if (isDisplayable()) {
            recomputeDimensions();
        }
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
        repaint();
    }

    public void setAbsDisplay(boolean isAbsModel) {
        isAbsDisplay = isAbsModel;
        repaint();
    }

    public void print(Graphics g, int x, int y, DSGeneMarker stats) {
        if (isPrintLabels) {
            g.setColor(Color.black);
            if (isPrintRatio) {
                //String ratio = Formatter.format(eg.Ratio);
                //g.drawString(ratio, x, y);
                //x += RatioWidth;
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
                g.drawString(accession, x, y);
                x += accessionWidth;
            }
            if (isPrintDescription) {
                FontRenderContext frc = new FontRenderContext(null, false, false);
                String label = stats.getShortName();
                if (label == null) {
                    label = "Undefined";
                }
                Rectangle2D rect = labelFont.getStringBounds(label, frc); //g.getFontRenderContext());
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
        int fontSize = Math.min(getFontSize(), (int) ((double) MAXFONTSIZE / (double) DEFAULTRES * (double) resolution));
        if ((fontSize != this.fontSize) || (labelFont == null)) {
            this.fontSize = fontSize;
            labelFont = new Font("Times New Roman", Font.PLAIN, this.fontSize);
        }
    }

    protected void recomputeDimensions() {
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
            int geneNumber = cl.getMarkerNo();
            int chipNo = getChipNo();
            geneNo += geneNumber;
            for (int i = 0; i < geneNumber; i++) {
                DSGeneMarker stats = cl.getGeneLabel(i);
                //String ratio       = Formatter.format(eg.Ratio);
                String label = stats.getShortName(); //stats.getDescription();
                String accession = stats.getLabel();
                double pValue = cl.getGenePValue(stats);
                if ((pValue == -1d) && (significanceResultSet != null)) {
                    Double sig = significanceResultSet.getSignificance(stats);
                    if (sig != null) {
                        pValue = sig;
                    }
                }
                String p = "";
                //rect                 = LabelFont.getStringBounds(ratio, g.getFontRenderContext());
                ratioWidth = 0; //Math.max(RatioWidth, (int)rect.getWidth());
                if (pValue != -1d) {
                    if (pValue <= 0f) {
                        p = "< " + format.format(Float.MIN_VALUE);
                    } else {
                        p = format.format(pValue);
                    }
                    isPrintPValue = true;
                    rect = labelFont.getStringBounds(p, g.getFontRenderContext());
                    pValueWidth = Math.max(pValueWidth, (int) rect.getWidth() + 4);
                }
                if (accession != null) {
                    rect = labelFont.getStringBounds(accession, g.getFontRenderContext());
                    accessionWidth = Math.max(accessionWidth, (int) rect.getWidth());
                } else {
                    accessionWidth = 0;
                }
                if (label == null)
                    label = "Undefined";
                rect = labelFont.getStringBounds(label, g.getFontRenderContext());
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
        // repaint();
    }

    public void toggleShowPattern(boolean state) {
        showPattern = state;
        repaint();
    }

    public void sortRows(boolean state) {
        sortRows = state;
    }

    public void setHideMasked(boolean state) {
        hideMasked = state;
    }

    public void setAutoWidth(double inches, int res) {
        if (res != oldRes) {
            resolution = res;
            recomputeDimensions();
            oldRes = res;
        }
        //double pixels = (double)((ChipManager.GetChipNo() * GeneWidth) / 1 + LabelGutter + TextSize);
        double width = inches * resolution;
        int chipNo = getChipNo();
        geneWidth = Math.min(40, (int) ((width - textSize - labelGutter) * 1.0 / (double) chipNo));
        geneHeight = 30;
        recomputeDimensions();
        // resolution = DEFAULTRES;
    }

    public void notifyComponent(Object subscriber, Class anInterface) {
        //***
        /*
         if(anInterface == IMicroarrayIdChangeSubscriber.class) {
             ((IMicroarrayIdChangeSubscriber)subscriber).notifyMArrayIdChange(microarraySet, microarrayId);
         } else if (anInterface == IMarkerIdChangeSubscriber.class){
             ((IMarkerIdChangeSubscriber)subscriber).notifyMarkerIdChange(microarraySet, microarrayId, markerId);
         } else if (anInterface == IMarkerIdClickSubscriber.class){
             ((IMarkerIdClickSubscriber)subscriber).notifyMarkerClicked(markerId, microarrayId);
         }
 */
    }

    void setParent(ColorMosaicPanel parent) {
        this.parent = parent;
    }

    public void showAllMArrays(boolean yes_no) {
        showAllMArrays = yes_no;
        recomputeDimensions();
        setSize();
    }

    public void showAllMarkers(boolean yes_no) {
        showAllMarkers = yes_no;
        for (int i = 0; i < clusterNo; i++) {
            cluster[i].showAllMarkers(yes_no);
        }
        recomputeDimensions();
        setSize();
    }
}
