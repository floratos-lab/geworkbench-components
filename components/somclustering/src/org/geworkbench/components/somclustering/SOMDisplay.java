package org.geworkbench.components.somclustering;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.properties.DSNamed;
import org.geworkbench.bison.model.clusters.Cluster;
import org.geworkbench.bison.model.clusters.DSSOMClusterDataSet;
import org.geworkbench.bison.model.clusters.LeafSOMCluster;
import org.geworkbench.bison.model.clusters.SOMCluster;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Asynchronous;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.ProgressBar;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company: Columbia University
 * </p>
 * <p/> Graphical representation of SOM Clusters contained in the
 * {@link org.geworkbench.bison.model.clusters.SOMCluster} format
 * 
 * @author manjunath
 * @version $Id$
 */
@SuppressWarnings("unchecked")
@AcceptTypes( { DSSOMClusterDataSet.class })
public class SOMDisplay implements VisualPlugin, MenuListener,
		PropertyChangeListener {

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * Default constructor
	 */
	public SOMDisplay() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.engine.config.VisualPlugin#getComponent()
	 */
	public Component getComponent() {
		return somWidget;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.engine.config.MenuListener#getActionListener(java.lang.String)
	 */
	public ActionListener getActionListener(String var) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			}
		};
	}

	/**
	 * This is the method of the ClusterListener invoked by the 'throwEvent' in
	 * the AnalysisPanel when a hier clustering analysis ends. The event 'hcae'
	 * contains the microarray set used, as well as the clusters produced.
	 * 
	 * @param event
	 *            the application <code>ClusterEvent</code> SOM Clustering
	 *            event received by the wrapper
	 */
	@Subscribe(Asynchronous.class)
	public void receive(ProjectEvent event, Object source) {
		// Create and throw a HierClusterModelEvent event.
		DSDataSet<?> dataSet = event.getDataSet();
		if ((dataSet != null) && (dataSet instanceof DSSOMClusterDataSet)) {
			DSSOMClusterDataSet newClusterSet = (DSSOMClusterDataSet) dataSet;
			if (newClusterSet != clusterSet) {
				clusterSet = newClusterSet;
				originalClusters = clusterSet.getClusters();
				mASet = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) clusterSet.getDataSetView();
				origX = originalClusters.length;
				origY = originalClusters[0].length;
			
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						reset();
					}
					
				});
			}
		}
	}

	@Publish
	public org.geworkbench.events.ImageSnapshotEvent publishImageSnapshotEvent(
			org.geworkbench.events.ImageSnapshotEvent event) {
		return event;
	}

	@Publish
	public org.geworkbench.events.MarkerSelectedEvent publishMarkerSelectedEvent(
			org.geworkbench.events.MarkerSelectedEvent event) {
		return event;
	}

	@Publish
	public org.geworkbench.events.PhenotypeSelectedEvent publishPhenotypeSelectedEvent(
			org.geworkbench.events.PhenotypeSelectedEvent event) {
		return event;
	}

	@Publish
	public org.geworkbench.events.SubpanelChangedEvent<? extends DSNamed> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<?> event) {
		return event;
	}

	public void propertyChange(PropertyChangeEvent pce) {
		String propertyName = pce.getPropertyName();
		Object newValue = pce.getNewValue();
		if (propertyName.equals(SOMPlot.SAVEIMAGE_PROPERTY)) {
			ImageIcon icon = new ImageIcon((Image) newValue);
			icon
					.setDescription("SOM Cluster: "
							+ mASet.getDataSet().getLabel());
			publishImageSnapshotEvent(new org.geworkbench.events.ImageSnapshotEvent(
					"SOM Cluster ImageSnapshot", icon,
					org.geworkbench.events.ImageSnapshotEvent.Action.SAVE));
		} else if (propertyName.equals(SOMPlot.SINGLE_MARKER_SELECTED_PROPERTY)
				&& newValue instanceof DSGeneMarker) {
			MarkerSelectedEvent mse = new org.geworkbench.events.MarkerSelectedEvent(
					(DSGeneMarker) newValue);
			publishMarkerSelectedEvent(mse);

		} else if (propertyName.equals(SOMPlot.SINGLE_MARKER_SELECTED_PROPERTY)
				&& newValue instanceof DSMicroarray) {

			PhenotypeSelectedEvent pse = new PhenotypeSelectedEvent(
					(DSMicroarray) newValue);
			publishPhenotypeSelectedEvent(pse);

		} else if (propertyName
				.equals(SOMPlot.MULTIPLE_MARKER_SELECTED_PROPERTY)) {
			DSPanel<DSGeneMarker> clusterGrid = new CSPanel<DSGeneMarker>(
					"Cluster Grid", "SOM Display");

			DSItemList<DSGeneMarker> mInfos = (DSItemList<DSGeneMarker>) newValue;

			for (int i = 0; i < mInfos.size(); i++)
				clusterGrid.add(mInfos.get(i));
			clusterGrid.setActive(true);
			publishSubpanelChangedEvent(new org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker>(
					DSGeneMarker.class, clusterGrid,
					org.geworkbench.events.SubpanelChangedEvent.NEW));
		} else if (propertyName.equals(SOMPlot.PLOT_MOUSE_CLICKED)) {
			if (newValue != null && newValue instanceof SOMPlot) {
				showPlot((SOMPlot) newValue);
			}
		}
	}

	/**
	 * Configures the Graphical User Interface and Listeners
	 * 
	 * @throws Exception
	 */
	private void jbInit() throws Exception {
		somWidget.setLayout(borderLayout1);
		display.setBackground(Color.white);
		showSelected.setText("Show Selected");
		showSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showSelected_actionPerformed(e);
			}
		});
		somWidget.add(display, BorderLayout.CENTER);
		somWidget.add(jToolBar1, BorderLayout.SOUTH);
		jToolBar1.add(showSelected, null);
		origX = origY = -1;
	}

	/**
	 * Resets clusters and sizes
	 */
	// must be invoked from EDT
	private void reset() {
		x = originalClusters.length;
		y = originalClusters[0].length;
		plots = new SOMPlot[x][y];

		int diffWidth = somWidget.getWidth() / x;
		int diffHeight = somWidget.getHeight() / y;
		ProgressBar pb = org.geworkbench.util.ProgressBar
				.create(ProgressBar.INDETERMINATE_TYPE);
		pb.setTitle("SOM Clustering");
		pb.setMessage("Rendering Clusters...");
		pb.start();

		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				JFreeChart chart = ChartFactory.createXYLineChart(""+(i*y+j+1), // Title 
						"Experiment", // X-Axis label
						"Value", // Y-Axis label
						new XYSeriesCollection(), // Dataset
						PlotOrientation.VERTICAL, false, // Show legend
						true, true);
				SOMPlot plot = new SOMPlot(chart);
				plots[i][j] = plot;
				plot.setSize(diffWidth, diffHeight);
				plot.setPreferredSize(new Dimension(diffWidth, diffHeight));

				DSMicroarraySetView<DSGeneMarker, DSMicroarray> subSet = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(mASet
						.getMicroarraySet());

				Cluster[] leaves = originalClusters[i][j].getChildrenNodes();

				DSPanel<DSGeneMarker> mInfos = new CSPanel<DSGeneMarker>(
						"SOM Cluster");
				DSPanel<DSMicroarray> arrays = new CSPanel<DSMicroarray>(
						"SOM Cluster");

				if (leaves != null) {
					for (int k = 0; k < leaves.length; k++) {
						mInfos
								.add(((LeafSOMCluster) leaves[k])
										.getMarkerInfo());
					}
					if (mASet != null) {
						for (int k = 0; k < mASet.items().size(); k++) {
							DSMicroarray ma = mASet.get(k);
							arrays.add(ma);
						}
					}
					mInfos.setActive(true);
					arrays.setActive(true);
					subSet.setMarkerPanel(mInfos);
					if (mInfos.size() > 0) {
						subSet.useMarkerPanel(true);
					}

					subSet.setItemPanel(arrays);

					plot.setChips(subSet);
				} else {
					plot.setChips(null);
				}
				plot.addPropertyChangeListener(this);
				plots[i][j] = plot;
			}
		}
		log.debug("Rendering SOMs");

		display.removeAll();
		display.setLayout(new GridLayout(x, y));
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				display.add(plots[i][j]);
			}
		}
		display.revalidate();
		display.repaint();

		pb.stop();
		pb.dispose();
		currentPlots = plots;
		isReset = true;
	}

	/**
	 * Handles selection/deselection of the 'zoom' checkbox
	 * 
	 * @param e
	 *            <code>ActionEvent</code> forwarded by the listener
	 */
	private void showSelected_actionPerformed(ActionEvent e) {
		singleChart = ((JCheckBox) e.getSource()).isSelected();
		if (!singleChart) {
			showPlot(null);
		}
	}

	private void showPlot(SOMPlot plot) {
		if (singleChart) {
			if (origX > -1 && origY > -1) {
				x = y = 1;
				somWidget.repaint();
				isReset = false;
				display.removeAll();
				display.setLayout(new BorderLayout());
				display.add(plot, BorderLayout.CENTER);
				display.revalidate();
				somWidget.repaint();
			}
		} else {
			if (!isReset && plots != null) {
				currentPlots = plots;
				x = currentPlots.length;
				y = currentPlots[0].length;
				display.removeAll();
				display.setLayout(new GridLayout(x, y));
				int diffWidth = somWidget.getWidth() / x;
				int diffHeight = somWidget.getHeight() / y;
				for (int i = 0; i < x; i++) {
					for (int j = 0; j < y; j++) {
						SOMPlot p = currentPlots[i][j];
						p.setSize(diffWidth, diffHeight);
						p
								.setPreferredSize(new Dimension(diffWidth,
										diffHeight));
						display.add(p);
					}
				}
				display.revalidate();
				somWidget.repaint();
				isReset = true;
			}
		}
	}

	/**
	 * The widget used by the component.
	 */
	private JPanel somWidget = new JPanel();

	/**
	 * The underlying micorarray set used in the hierarchical clustering
	 * analysis.
	 */
	private DSMicroarraySetView<DSGeneMarker, DSMicroarray> mASet = null;

	/**
	 * The <code>SOMCluster[][]</code> received from the SOM analysis
	 */
	private SOMCluster[][] originalClusters = null;

	private DSSOMClusterDataSet clusterSet = null;

	/**
	 * There is one <code>SOMPlot</code> created for every SOMCluster
	 */
	private SOMPlot[][] plots = null;

	/**
	 * Stores the currently displayed plot(s)
	 */
	private SOMPlot[][] currentPlots = null;

	/**
	 * SOM cluster x coordinate
	 */
	private int x = 0;

	/**
	 * SOM Cluster y coordinate
	 */
	private int y = 0;

	/**
	 * Keeps track of the x length for the originalCluster
	 */
	private int origX;

	/**
	 * Keeps track of the y length for the originalCluster
	 */
	private int origY;

	/**
	 * Keeps track of state of zoom checkbox
	 */
	private boolean singleChart = false;

	/**
	 * Keeps track of any zoom inviked by user
	 */
	private boolean isReset = false;

	/**
	 * Visual Widget
	 */
	private BorderLayout borderLayout1 = new BorderLayout();

	/**
	 * Visual Widget
	 */
	private JToolBar jToolBar1 = new JToolBar();

	/**
	 * Visual Widget
	 */
	private JCheckBox showSelected = new JCheckBox();

	/**
	 * Visual Widget
	 */
	private JPanel display = new JPanel();
}
