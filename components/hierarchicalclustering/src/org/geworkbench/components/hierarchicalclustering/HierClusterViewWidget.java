package org.geworkbench.components.hierarchicalclustering;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.Cluster;
import org.geworkbench.bison.model.clusters.DSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.bison.model.clusters.MarkerHierCluster;
import org.geworkbench.bison.model.clusters.MicroarrayHierCluster;
import org.geworkbench.events.HierClusterModelEvent;
import org.geworkbench.events.HierClusterModelEventListener;
import org.geworkbench.util.ColorScale;

import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * The widget should throw Java events (to be handled by the wrapping
 * <code>HierClusterViewAppComponent</code> component), when the following
 * user-initiated actions occur:
 * <ul>
 * <li>
 * A branch is clicked. The event should communicate to the wrapping
 * component what cluster was selected.
 * </li>
 * <li>
 * A marker (or a microarray) is double clicked. The event thrown
 * should communicate to the wrapping component of the marker (or microarray)
 * selected.
 * </li>
 * </ul>
 * Utility for visualization of cluster analysis for genome-wide expression data
 * from DNA microarray hybridization
 * as described in:
 * <p/>
 * <p><h3>Cluster analysis and display of genome-wide expression patterns</h3></p>
 * <p>Michael B. Eisen, Paul T. Spellman, Patrick O. Brown AND David Botstein</p>
 * <p>Proc. Natl. Acad. Sci. USA</p>
 * <p>Vol. 95, pp. 14863 - 14868, December 1998</p>
 * <p>Genetics</p>
 *
 * @author First Genetic Trust
 * @version $Id$
 */
public class HierClusterViewWidget extends JPanel implements HierClusterModelEventListener {
	private static final long serialVersionUID = 3261372914974476040L;

	private static Log log = LogFactory.getLog(HierClusterViewWidget.class);

    /**
     * Property used for conveying the origin of a <code>PropertyChange</code>
     * event for distinguishing messages from other components which throw
     * <code>PropertyChange</code> events
     */
    static final String SAVEIMAGE_PROPERTY = "saveClusterImage";

    /**
     * Property to signify that a Single Marker Selection originated from
     * the <code>HierClusterViewWidget</code>
     */
    static String SINGLE_MARKER_SELECTED_PROPERTY = "HierarchicalClusterSingleMarkerSelected";

    /**
     * Property to signify that a Single Marker Selection originated from
     * the <code>HierClusterViewWidget</code>
     */
    static String MULTIPLE_MARKER_SELECTED_PROPERTY = "HierarchicalClusterMultipleMarkerSelected";

    /**
     * Property to signify that a Single Marker Selection originated from
     * the <code>HierClusterViewWidget</code>
     */
    static String MULTIPLE_ARRAY_SELECTED_PROPERTY = "HierarchicalClusterMultipleArraySelected";

    /**
     * The underlying micorarray set used in the hierarchical clustering
     * analysis.
     */
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> mASet = null;

    /**
     * The subcluster of markerCluster currently being displayed. This
     * variable is set when the user clicks on a marker branch of the displayed
     * image.
     */
    private MarkerHierCluster originalMarkerCluster = null;

    /**
     * Currently displayed marker cluster
     */
    private MarkerHierCluster selectedMarkerCluster = null;

    /**
     * Currently displayed microarray cluster
     */
    private MicroarrayHierCluster selectedArrayCluster = null;

    private DSHierClusterDataSet clusterSet = null;

    /**
     * The subcluster of arrayCluster currently being displayed. This
     * variable is set when the user clicks on a branch of the displayed
     * image.
     */
    private MicroarrayHierCluster originalArrayCluster;

    /**
     * The canvas on which the actual dendrogram is drawn.
     */
    private JPanel jPanel2 = new JPanel();

    /**
     * The <code>JPanel</code> on which the marker dendrogram is painted
     */
    private HierClusterTree markerDendrogram = null;

    /**
     * The <code>JPanel</code> on which the array dendrogram is painted
     */
    private HierClusterTree arrayDendrogram = null;

    /**
     * The <code>JPanel</code> on which the array names is painted
     */
    private HierClusterLabels arrayNames = null;
    private JPanel arrayContainer = new JPanel();

    /**
     * Slider for controlling color intensity of markers in the dendrogram
     */
    private JSlider slider = new JSlider();

    /**
     * Bit that controls if zooming the dendrograms is enabled
     */
    private boolean zoomEnabled = false;

    /**
     * Saves the state of the "Tootip" button
     */
    private boolean showSignal = false;

    /**
     * <code>MouseListener</code> that captures clicks on the dendrograms
     */
    private MouseListener dendrogramListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            dendrogram_mouseClicked(e);
        }
    };

    /**
     * <code>MouseMotionListener</code> that captures mouse motion over the
     * dendrogram and the cluster leaf display panel
     */
    private MouseMotionListener motionListener = new MouseMotionAdapter() {
        public void mouseMoved(MouseEvent e) {
            this_mouseMoved(e);
        }
    };

    /**
     * <code>MouseListener</code> that triggers a <code>JPopupMenu</code>
     */
    private MouseListener mouseListener = new MouseAdapter() {
        public void mouseReleased(MouseEvent e) {
            this_mouseReleased(e);
        }

        public void mouseClicked(MouseEvent e) {
            this_mouseClicked(e);
        }

        public void mouseExited(MouseEvent e) {
            if (e.getSource() == markerDendrogram) {
                log.debug("Setting highlight on marker dendrogram to null");
                markerDendrogram.setCurrentHighlight(null);
            } else if (e.getSource() == arrayDendrogram) {
                log.debug("Setting highlight on array dendrogram to null");
                arrayDendrogram.setCurrentHighlight(null);
            }
        }
    };

    /**
     * <code>AdjustmentListener</code> that is added to <code>JScrollBar</code>
     * components to track if the <code>JScrollBar</code> has stopped moving
     */
    private AdjustmentListener scrollBarListener = new AdjustmentListener() {
        public void adjustmentValueChanged(AdjustmentEvent e) {
            this_adjustmentValueChanged(e);
        }
    };

   

    /**
     * Visual widget
     */
    private BorderLayout borderLayout1 = new BorderLayout();

    /**
     * Visual widget
     */
    private JScrollPane jScrollPane1 = new JScrollPane();

    /**
     * Visual widget
     */
    private BorderLayout borderLayout2 = new BorderLayout();

    /**
     * Visual widget
     */
    private HierClusterDisplay display = new HierClusterDisplay();

    /**
     * Visual widget
     */
    private JToggleButton jToolTipToggleButton = new JToggleButton();

    /**
     * Visual widget
     */
    private JSpinner jGeneHeight = new JSpinner();
    private JLabel heightLabel = new JLabel("Gene Height");

    /**
     * Visual widget
     */
    private JSpinner jGeneWidth = new JSpinner();
    private JLabel widthLabel = new JLabel("Gene Width");

    /**
     * Visual widget
     */
    private JCheckBox jCheckBox1 = new JCheckBox();

    /**
     * Visual widget
     */
    private JPopupMenu contextMenu = new JPopupMenu();

    /**
     * Visual widget
     */
    private JMenuItem imageSnapShot = new JMenuItem();

    /**
     * Visual Widget
     */
    private JMenuItem addToPanel = new JMenuItem();

    /**
     * Default Constructor
     */
    public HierClusterViewWidget() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  

    /**
     * <code>HierClusterModelEventListener</code> interface method that notifies
     * this component of a change in the Clustering data model
     *
     * @param hcme the new model wrapping the clustering data
     */
	public void hierClusterModelChange(HierClusterModelEvent hcme) {
        mASet = hcme.getMicroarraySet();
        originalMarkerCluster = hcme.getMarkerCluster();
        originalArrayCluster = hcme.getMicroarrayCluster();
        selectedMarkerCluster = hcme.getSelectedMarkerCluster();
        selectedArrayCluster = hcme.getSelectedMicroarrayCluster();
    	clusterSet = hcme.getClusterSet();
        zoomEnabled = hcme.getSelectionEnabled();
        jCheckBox1.setSelected(zoomEnabled);
    	display.resetVariables(mASet);
        markerDendrogram.setChips(mASet);
        arrayDendrogram.setChips(mASet);
        arrayNames.setChips(mASet);
        if (!zoomEnabled || (selectedMarkerCluster == null && selectedArrayCluster == null))
        	init(originalMarkerCluster, originalArrayCluster);
        else
        	init(selectedMarkerCluster, selectedArrayCluster);
        
        org.geworkbench.bison.util.colorcontext.ColorContext colorContext = (org.geworkbench.bison.util.colorcontext.ColorContext) mASet.getDataSet()
		.getObject(org.geworkbench.bison.util.colorcontext.ColorContext.class);
        colorScale.setMinColor(colorContext
				.getMinColorValue(slider.getValue()));
        colorScale.setCenterColor(colorContext
				.getMiddleColorValue(slider.getValue()));
        colorScale.setMaxColor(colorContext
				.getMaxColorValue(slider.getValue()));
        colorScale.repaint();
    }

    /**
     * Resets clusters and sizes
     *
     * @param markerCluster <code>HierCluster</code> representing the
     *                      Marker Dendrogram
     * @param arrayCluster  <code>HierCluster</code> representing the
     *                      Microarray Dendrogram
     */
    private void init(HierCluster markerCluster, HierCluster arrayCluster) {
        if (mASet == null) {
            return;
        }
        jGeneHeight.setValue(HierClusterDisplay.geneHeight);
        jGeneWidth.setValue(HierClusterDisplay.geneWidth);

        display.setMarkerHierCluster((MarkerHierCluster) markerCluster);
        display.setMicroarrayHierCluster((MicroarrayHierCluster) arrayCluster);
        arrayNames.setMicroarrayHierCluster((MicroarrayHierCluster) arrayCluster);
        markerDendrogram.setTreeData(markerCluster);

        if ((mASet != null) && (mASet.items().size() == 1)) {
            arrayDendrogram.setTreeData(null);
            arrayNames.setTreeData(null);
        } else {
            arrayDendrogram.setTreeData(arrayCluster);
            arrayNames.setTreeData(arrayCluster);
        }
        setSizes();
        revalidate();
        repaint();
    }

    /**
     * Utility method to set sizes of the trees and the cluster mosaic
     */
    private void setSizes() {
        int mdw = markerDendrogram.getWidth();
        int ht = markerDendrogram.getMaxHeight();
        int adw = arrayDendrogram.getMaxHeight();
        int adh = arrayDendrogram.getHeight();
        int ndh = arrayNames.getHeight();
        arrayDendrogram.leftOffset = mdw;
        arrayNames.leftOffset = mdw;
        jPanel2.setPreferredSize(new Dimension((int) ((adw + mdw) * 1.5), (int) (ndh + adh + ht + (ht / 5))));
        jPanel2.setSize(new Dimension((int) ((adw + mdw) * 1.5), (int) (ndh + adh + ht + (ht / 5))));
        display.setPreferredSize(new Dimension((int) mdw / 2, ht));
        display.setSize(new Dimension((int) mdw / 2, ht));
    }

    private ColorScale colorScale = new ColorScale(Color.gray, Color.gray, Color.gray);

    /**
     * Configures the Graphical User Interface and Listeners
     *
     * @throws Exception
     */
    private void jbInit() throws Exception {
        this.setLayout(borderLayout1);
        jPanel2.setLayout(borderLayout2);
        jToolTipToggleButton.setToolTipText("Toggle signal");
        jToolTipToggleButton.setActionCommand("TOOL_TIP_TOGGLE");
        jToolTipToggleButton.setSelected(false);
        jToolTipToggleButton.setIcon(new ImageIcon(this.getClass().getResource("bulb_icon_grey.gif")));
        jToolTipToggleButton.setSelectedIcon(new ImageIcon(this.getClass().getResource("bulb_icon_gold.gif")));
        jToolTipToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jToolTipToggleButton_actionPerformed(e);
            }
        });
        slider.setPaintTicks(true);
        slider.setValue(100);
        slider.setMinorTickSpacing(2);
        slider.setMinimum(1);
        slider.setMaximum(200);
        slider.setMajorTickSpacing(50);
        slider.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        slider.setBorder(new LineBorder(Color.black));
        slider.setToolTipText("Intensity");
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                slider_stateChanged(e);
            }
        });
        jCheckBox1.setText("Enable Selection");
        jCheckBox1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                zoomCheckBox_actionPerformed(e);
            }
        });

        SpinnerNumberModel snm2 = new SpinnerNumberModel(HierClusterDisplay.geneHeight, 1, 100, 1);
        jGeneHeight = new JSpinner(snm2);
        jGeneHeight.setToolTipText("Gene Height");
        jGeneHeight.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                jGeneHeight_stateChanged(e);
            }
        });
        jGeneWidth.setToolTipText("Gene Width");

        SpinnerNumberModel snm1 = new SpinnerNumberModel(new Integer(HierClusterDisplay.geneWidth), new Integer(1), new Integer(100), new Integer(1));
        jGeneWidth.setModel(snm1);
        jGeneWidth.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                jGeneWidth_stateChanged(e);
            }
        });
        ButtonBarBuilder bbuilder = new ButtonBarBuilder();
        bbuilder.addFixed(jCheckBox1);
        bbuilder.addGlue();
        bbuilder.addFixed(heightLabel);
        bbuilder.addRelatedGap();
        bbuilder.addFixed(jGeneHeight);
        bbuilder.addUnrelatedGap();
        bbuilder.addFixed(widthLabel);
        bbuilder.addRelatedGap();
        bbuilder.addFixed(jGeneWidth);
        bbuilder.addGlue();
        bbuilder.addFixed(colorScale);
        bbuilder.addGlue();
        bbuilder.addFixed(new JLabel("Intensity"));
        bbuilder.addRelatedGap();
        bbuilder.addGriddedGrowing(slider);
        bbuilder.addGlue();
        bbuilder.addFixed(jToolTipToggleButton);

        this.add(bbuilder.getPanel(), BorderLayout.SOUTH);

        this.add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(jPanel2, null);
        jScrollPane1.getVerticalScrollBar().addAdjustmentListener(scrollBarListener);
        jScrollPane1.getHorizontalScrollBar().addAdjustmentListener(scrollBarListener);
        jPanel2.add(display, BorderLayout.CENTER);
        display.addMouseMotionListener(motionListener);
        display.addMouseListener(mouseListener);
        markerDendrogram = new HierClusterTree(this, null, HierClusterTree.Orientation.HORIZONTAL);
        markerDendrogram.addMouseListener(dendrogramListener);
        markerDendrogram.addMouseMotionListener(motionListener);
        markerDendrogram.addMouseListener(mouseListener);
        jPanel2.add(markerDendrogram, BorderLayout.WEST);
        arrayDendrogram = new HierClusterTree(this, null, HierClusterTree.Orientation.VERTICAL);
        arrayDendrogram.addMouseListener(dendrogramListener);
        arrayDendrogram.addMouseMotionListener(motionListener);
        arrayDendrogram.addMouseListener(mouseListener);
        arrayNames = new HierClusterLabels(this);
        arrayContainer.setLayout(new BorderLayout());
        arrayContainer.add(arrayNames, BorderLayout.NORTH);
        arrayContainer.add(arrayDendrogram, BorderLayout.CENTER);
        jPanel2.add(arrayContainer, BorderLayout.NORTH);
        imageSnapShot.setText("Image Snapshot");
        addToPanel.setText("Add to Set");
        contextMenu.add(imageSnapShot);
        contextMenu.add(addToPanel);

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveImage_actionPerformed(e);
            }
        };

        imageSnapShot.addActionListener(listener);
        addToPanel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addToPanel_actionPerformed(e);
            }
        });
    }

    /**
     * Handles selections/deselections of the 'Zoom' checkbox
     *
     * @param e <code>ActionEvent</code> forwarded by the listener
     */
    private void zoomCheckBox_actionPerformed(ActionEvent e) {
        zoomEnabled = jCheckBox1.isSelected();
        clusterSet.setSelectionEnabled(zoomEnabled);

        if (!zoomEnabled) {
            resetOriginal();
        }
    }

    /**
     * Handles changes in the intensity slider
     *
     * @param e <code>ChangeEvent</code> forwarded by the slider listener
     */
    private void slider_stateChanged(ChangeEvent e) {
        double v = (double) slider.getValue() / 100.0;

        if (v > 1) {
            display.setIntensity((1 + Math.exp(v)) - Math.exp(1.0));
        } else {
            display.setIntensity(v);
        }
    }

    /**
     * Handles {@link java.awt.event.MouseMotionListener#mouseReleased}
     * <p/>
     * invocations on this widget
     *
     * @param e <code>MouseEvent</code> forwarded by the mouse listener.
     */
    private void this_mouseMoved(MouseEvent e) {
        if (zoomEnabled) {
            if (e.getSource() == markerDendrogram) {
                markerDendrogram.setCurrentHighlightForMouseLocation(e.getY(), e.getX());
                if (markerDendrogram.isPointClickable(e.getX(), e.getY(), false)) {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            } else if (e.getSource() == arrayDendrogram) {
                arrayDendrogram.setCurrentHighlightForMouseLocation(e.getX(), e.getY());
                if (arrayDendrogram.isPointClickable(e.getX(), e.getY(), true)) {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        }

        if (e.getSource() instanceof HierClusterDisplay) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            display.drawCell(e.getX(), e.getY(), showSignal);
        }
    }

    /**
     * Handles {@link java.awt.event.MouseListener#mouseClicked} invocations on
     * this one of the Dendrograms
     *
     * @param e <code>MouseEvent</code> forwarded by the Dendrogram.
     */
    private void dendrogram_mouseClicked(MouseEvent e) {
        if (zoomEnabled) {
            if (e.getSource() == markerDendrogram) {
                selectedMarkerCluster = (MarkerHierCluster) markerDendrogram.getNodeClicked(e.getX(), e.getY());

                if (selectedMarkerCluster != null) {
                    init(selectedMarkerCluster, selectedArrayCluster);
                }
            } else if (e.getSource() == arrayDendrogram) {
                selectedArrayCluster = (MicroarrayHierCluster) arrayDendrogram.getNodeClicked(e.getX(), e.getY());

                if (selectedArrayCluster != null) {
                    init(selectedMarkerCluster, selectedArrayCluster);
                }
            }
            HierCluster[] selectedClusters = {selectedMarkerCluster, selectedArrayCluster};
            clusterSet.setSelectedClusters(selectedClusters);

            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Handles {@link java.awt.event.MouseListener#mouseReleased} invocations on
     * this widget
     *
     * @param e <code>MouseEvent</code> forwarded by the mouse listener.
     */
    private void this_mouseReleased(MouseEvent e) {
        if (e.isMetaDown()) {
            if (e.getSource() == display) {
                e.translatePoint(markerDendrogram.getWidth(), arrayNames.getHeight() + arrayDendrogram.getHeight());
            }

            if (e.getSource() == markerDendrogram) {
                e.translatePoint(0, arrayNames.getHeight() + arrayDendrogram.getHeight());
            }

            if (e.getSource() == arrayDendrogram) {
                e.translatePoint(0, arrayNames.getHeight());
            }

            if (e.getSource() == arrayNames) {
                e.translatePoint(0, 0);
            }

            contextMenu.show(jPanel2, e.getX(), e.getY());
        }
    }

    /**
     * Handles {@link java.awt.event.MouseListener#mouseClicked} invocations on
     * this widget
     *
     * @param e <code>MouseEvent</code> forwarded by the mouse listener.
     */
    private void this_mouseClicked(MouseEvent e) {
        if (e.getSource() == display) {
            DSGeneMarker mInfo = display.getMarkerInfoClicked(e.getX(), e.getY());

            if (mInfo != null) {
                firePropertyChange(SINGLE_MARKER_SELECTED_PROPERTY, null, mInfo);
            }
        }
    }

    /**
     * Handles changes to the Gene Height widget
     *
     * @param e <code>ChangeEvent</code> forwarded by the listener
     */
    private void jGeneHeight_stateChanged(ChangeEvent e) {
        HierClusterDisplay.geneHeight = ((Integer) ((JSpinner) e.getSource()).getValue()).intValue();
        markerDendrogram.resizingMarker = true;
        arrayDendrogram.resizingMarker = true;
        arrayNames.resizingMarker = true;
        setSizes();
        revalidate();
        repaint();
    }

    /**
     * Handles changes to the Gene Width widget
     *
     * @param e <code>ChangeEvent</code> forwarded by the listener
     */
    private void jGeneWidth_stateChanged(ChangeEvent e) {
        HierClusterDisplay.geneWidth = ((Integer) ((JSpinner) e.getSource()).getValue()).intValue();
        markerDendrogram.resizingMarker = true;
        arrayDendrogram.resizingMarker = true;
        arrayNames.resizingMarker = true;
        setSizes();
        revalidate();
        repaint();
    }

    /**
     * Handles Image Snapshot menu item selection
     *
     * @param e <code>ActionEvent</code> forwarded by the listener
     */
    private void saveImage_actionPerformed(ActionEvent e) {
        if ((markerDendrogram != null) && (arrayDendrogram != null) && (display != null)) {

            int w = display.getWidth() + markerDendrogram.getWidth();
            int h = arrayDendrogram.getHeight() + display.getHeight() + arrayNames.getHeight();
            BufferedImage tempImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D ig = tempImage.createGraphics();
            jPanel2.paint(ig);

            ImageIcon newIcon = new ImageIcon(tempImage, "Hierarchical Clustering Image : " + mASet.getDataSet().getLabel());
            firePropertyChange(SAVEIMAGE_PROPERTY, null, newIcon);
        }
    }

    /**
     * Handles Add to Panel menu item selection
     *
     * @param e <code>ActionEvent</code> forwarded by the listener
     */
    private void addToPanel_actionPerformed(ActionEvent e) {
        if (selectedMarkerCluster != null) {
            java.util.List<Cluster> leaves = selectedMarkerCluster.getLeafChildren();

            DSGeneMarker[] mInfos = new DSGeneMarker[leaves.size()];

            for (int i = 0; i < leaves.size(); i++)
                mInfos[i] = ((MarkerHierCluster) leaves.get(i)).getMarkerInfo();

            if (mInfos != null) {
                firePropertyChange(MULTIPLE_MARKER_SELECTED_PROPERTY, null, mInfos);
            }
        }
        if (selectedArrayCluster != null){
            java.util.List<Cluster> leaves = selectedArrayCluster.getLeafChildren();        	
            DSMicroarray[] mInfos = new DSMicroarray[leaves.size()];
            for (int i = 0; i < leaves.size(); i++)
                mInfos[i] = ((MicroarrayHierCluster) leaves.get(i)).getMicroarray();
            if (mInfos != null) {
                firePropertyChange(MULTIPLE_ARRAY_SELECTED_PROPERTY, null, mInfos);
            }
        }
    }

    /**
     * Resets original clusters
     */
    private void resetOriginal() {
        selectedMarkerCluster = originalMarkerCluster;
        selectedArrayCluster = originalArrayCluster;
        init(originalMarkerCluster, originalArrayCluster);
    }

    /**
     * Handles selection/deselections of the ToolTip toggle button
     *
     * @param e <code>ActionEvent</code> forwarded by the listener
     */
    private void jToolTipToggleButton_actionPerformed(ActionEvent e) {
        showSignal = jToolTipToggleButton.isSelected();
    }

    /**
     * Method to handle <code>AdjustmentListener</code> that is added to
     * <code>JScrollBar</code> components to track if the <code>JScrollBar</code>
     * has stopped moving
     *
     * @param e <code>AdjustmentEvent</code> that is fired by a
     *          <code>JScrollBar</code>
     */
    private void this_adjustmentValueChanged(AdjustmentEvent e) {
        if (!e.getValueIsAdjusting()) {
            this.revalidate();
            this.repaint();
        }
    }

	// this is necessary because arrayDendrogram.leftOffset depends the width of
	// markerDendrogram, which is known only after painting
	@Override
	protected void paintChildren(Graphics g) {
		super.paintChildren(g);

		int mdw = markerDendrogram.getWidth();
		if (arrayDendrogram.leftOffset != mdw) {
			arrayDendrogram.leftOffset = mdw;
			arrayNames.leftOffset = mdw;
		}
	}
}
