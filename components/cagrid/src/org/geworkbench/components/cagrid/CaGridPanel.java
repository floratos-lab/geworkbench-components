package org.geworkbench.components.cagrid;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.CSSOMClusterDataSet;
import org.geworkbench.bison.model.clusters.DSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.DefaultSOMCluster;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.bison.model.clusters.LeafSOMCluster;
import org.geworkbench.bison.model.clusters.MarkerHierCluster;
import org.geworkbench.bison.model.clusters.MicroarrayHierCluster;
import org.geworkbench.bison.model.clusters.SOMCluster;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Script;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.columbia.geworkbench.cagrid.cluster.client.HierarchicalClusteringClient;
import edu.columbia.geworkbench.cagrid.cluster.client.SomClusteringClient;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalCluster;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusteringParameter;
import edu.columbia.geworkbench.cagrid.cluster.som.SomCluster;
import edu.columbia.geworkbench.cagrid.cluster.som.SomClusteringParameter;
import edu.columbia.geworkbench.cagrid.converter.CagridMicroarrayTypeConverter;
import edu.columbia.geworkbench.cagrid.discovery.client.AnalyticalServiceDiscoveryClient;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySet;
import gov.nih.nci.cagrid.discovery.MetadataUtils;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;

/**
 * @author watkinson
 * @author keshav
 * @version $Id: CaGridPanel.java,v 1.21 2007-01-31 18:14:53 mhall Exp $
 */
public class CaGridPanel extends JPanel implements VisualPlugin {

	static Log log = LogFactory.getLog(CaGridPanel.class);

	private static final String DEFAULT_HOST = "cagridnode.c2b2.columbia.edu";

	private static final int DEFAULT_PORT = 8080;

	private static final String HIERARCHICAL_CLUSTERING = "HierarchicalClustering";

	private static final String HIERARCHICAL_CLUSTERING_NAME = "Hierarchical Clustering";

	private static final String SOM_CLUSTERING = "SomClustering";

	private static final String SOM_CLUSTERING_NAME = "Som Clustering";

	private static final String cagridTitle = "caGrid";

	private JPanel servicePanel;

	private DSMicroarraySet<DSMicroarray> microarraySet = null;

	/**
	 * 
	 */
	public Component getComponent() {
		log.debug("getting components");
		return this;
	}

	/**
	 * 
	 * 
	 */
	public CaGridPanel() {
		log.debug("initializing");
		servicePanel = new JPanel(new BorderLayout());
		// setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setLayout(new GridLayout(1, 3));
		FormLayout layout = new FormLayout(
				"right:max(40dlu;pref), 3dlu, 100dlu, 7dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		builder.appendSeparator("caGrid Discovery Host");
		final JTextArea hostField = new JTextArea(DEFAULT_HOST);
		final JTextArea portField = new JTextArea("" + DEFAULT_PORT);
		builder.append("Host", hostField);
		builder.append("Port", portField);
		JButton lookupButton = new JButton("Lookup Services");
		lookupButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ClassLoader oldClassLoader = Thread.currentThread()
						.getContextClassLoader();
				try {
					Thread.currentThread().setContextClassLoader(
							CaGridPanel.class.getClassLoader());
					// ClassUtils.setDefaultClassLoader(CaGridPanel.class.getClassLoader());
					int port = DEFAULT_PORT;
					try {
						port = Integer.parseInt(portField.getText());
					} catch (NumberFormatException nfe) {
						// Ignore for now
					}
                    EndpointReferenceType[] allServices = getServices(hostField.getText(), port);
                    populateServicePanel(allServices);
				} catch (Exception e1) {
					e1.printStackTrace();
				} finally {
					Thread.currentThread()
							.setContextClassLoader(oldClassLoader);
				}
			}
		});
		builder.append("", lookupButton);
		add(builder.getPanel());
		add(servicePanel);
		add(Box.createHorizontalGlue());
	}

    @Script
    public String getServiceUrl(String host, int port, String serviceFilter) {
        try {
            EndpointReferenceType[] services = getServices(host, port);
            ArrayList<String> urls = new ArrayList<String>();
            for (EndpointReferenceType service : services) {
                ServiceMetadata commonMetadata = MetadataUtils.getServiceMetadata(service);
                if (service.getAddress().toString().toLowerCase().contains(serviceFilter.toLowerCase())) {
                    urls.add(service.getAddress().toString());
                }
            }
            if (urls.size() > 0) {
                return urls.get(0);
            } else {
                return null;
            }
            // return urls.toArray(new String[]{});
        } catch (Exception e) {
            log.error("Error retrieving service list via script method.", e);
            return null;
        }
    }

    private EndpointReferenceType[] getServices(String host, int port) throws Exception {
        AnalyticalServiceDiscoveryClient client = new AnalyticalServiceDiscoveryClient(host, port);
        EndpointReferenceType[] allServices = client .getAllServices();
        if (allServices != null) {
            for (EndpointReferenceType service : allServices) {
                System.out.println("Service: "
                        + service.getAddress());
                ServiceMetadata commonMetadata = MetadataUtils
                        .getServiceMetadata(service);
                System.out.println("  Description: "
                        + commonMetadata.getServiceDescription()
                                .getService().getDescription());
            }
        }
        return allServices;
    }

    /**
     * @param services
     * @throws Exception
     */
    private void populateServicePanel(EndpointReferenceType[] services)
            throws Exception {
        log.debug("populating service panel");
        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 3dlu, 100dlu, 7dlu", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator("Services");

        ActionListener serviceListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    runService(e.getActionCommand());
                    log.info(e.getActionCommand());
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        };
        if (services == null) {
            builder.append("", new JLabel("No Services found."));
        } else {
            for (EndpointReferenceType service : services) {
                ServiceMetadata commonMetadata = MetadataUtils
                        .getServiceMetadata(service);
                JButton runButton = new JButton("Run");
                runButton.addActionListener(serviceListener);
                String name = commonMetadata.getServiceDescription()
                        .getService().getName();
                runButton.setActionCommand(service.getAddress().toString());
                builder.append(runButton, new JLabel(name));
            }
        }
        servicePanel.removeAll();
        servicePanel.add(builder.getPanel());
        revalidate();
        repaint();
    }

	/**
	 * @param event
	 * @param source
	 */
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		log.debug("receiving event");
		DSDataSet dataSet = event.getDataSet();
		// We will act on this object if it is a DSMicroarraySet
		if (dataSet instanceof DSMicroarraySet) {
			microarraySet = (DSMicroarraySet) dataSet;
		}
	}

	/**
	 * @param event
	 * @return ProjectNodeAddedEvent
	 */
	@Publish
	public ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			ProjectNodeAddedEvent event) {
		log.debug("project node added event");
		return event;
	}

	/**
	 * @param url
	 * @throws URI.MalformedURIException
	 * @throws RemoteException
	 */
	private void runService(final String url) throws URI.MalformedURIException,
			RemoteException {
		log.debug("Running service " + url);
		// Currently hard-coded to run Clustering
		if (microarraySet != null) {
			final ProgressBar pBar = ProgressBar
					.create(ProgressBar.INDETERMINATE_TYPE);
			Util.centerWindow(pBar);
			Runnable task = new Runnable() {
				public void run() {
					try {
						CSMicroarraySetView view = new CSMicroarraySetView(
								microarraySet);
						MicroarraySet gridSet = CagridMicroarrayTypeConverter
								.convertToCagridMicroarrayType(view);

						if (url.contains(HIERARCHICAL_CLUSTERING)) {
							GridHierarchicalClusteringDialog dialog = new GridHierarchicalClusteringDialog();

							HierarchicalClusteringParameter parameters = dialog
									.getParameters();

							if (parameters == null) {
								// Cancelled dialog
								return;
							}
							pBar.setTitle(cagridTitle);
							pBar.setMessage("Running "
									+ HIERARCHICAL_CLUSTERING_NAME);
							pBar.start();
							pBar.reset();
							HierarchicalClusteringClient client = new HierarchicalClusteringClient(
									url);
							HierarchicalCluster hierarchicalCluster = client
									.execute(gridSet, parameters);
							if (hierarchicalCluster != null) {
								// convert grid to bison hierarchical cluster
								CSHierClusterDataSet dataSet = createBisonHierarchicalClustering(
										hierarchicalCluster, view);
								ProjectNodeAddedEvent event = new ProjectNodeAddedEvent(
										HIERARCHICAL_CLUSTERING_NAME, null,
										dataSet);
								publishProjectNodeAddedEvent(event);
							}
						} else if (url.contains(SOM_CLUSTERING)) {

							GridSomClusteringDialog dialog = new GridSomClusteringDialog();

							SomClusteringParameter somClusteringParameters = dialog
									.getParameters();

							if (somClusteringParameters == null)
								return;

							pBar.setTitle(cagridTitle);
							pBar.setMessage("Running " + SOM_CLUSTERING_NAME);
							pBar.start();
							pBar.reset();
							SomClusteringClient client = new SomClusteringClient(
									url);
							SomCluster somCluster = client.execute(gridSet,
									somClusteringParameters);

							if (somCluster != null) {
								// convert grid to bison hierarchical cluster
								CSSOMClusterDataSet dataSet = createBisonSomClustering(
										somCluster, view);
								ProjectNodeAddedEvent event = new ProjectNodeAddedEvent(
										SOM_CLUSTERING_NAME, null, dataSet);
								publishProjectNodeAddedEvent(event);
							}

						} else {
							log.info("No services exist at " + url);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						// Shut down pBar
						pBar.stop();
					}
				}

			};
			Thread thread = new Thread(task);
			thread.start();
		}
	}

	/**
	 * 
	 * @param hierarchicalCluster
	 * @param view
	 * @return CSHierClusterDataSet
	 */
	private CSHierClusterDataSet createBisonHierarchicalClustering(
			HierarchicalCluster hierarchicalCluster, CSMicroarraySetView view) {
		log.debug("creating bison hierarchical cluster");
		HierarchicalClusterNode microarrayCluster = hierarchicalCluster
				.getMarkerCluster();
		HierarchicalClusterNode markerCluster = hierarchicalCluster
				.getMicroarrayCluster();
		HierCluster[] resultClusters = new HierCluster[2];
		if (markerCluster != null) {
			resultClusters[0] = convertToMarkerHierCluster(markerCluster);
		}
		if (microarrayCluster != null) {
			resultClusters[1] = convertToMicroarrayHierCluster(microarrayCluster);
		}
		CSHierClusterDataSet dataSet = new CSHierClusterDataSet(resultClusters,
				HIERARCHICAL_CLUSTERING_NAME, view);
		return dataSet;
	}

	/**
	 * 
	 * @param somCluster
	 * @param view
	 * @return CSSOMClusterDataSet
	 */
	private CSSOMClusterDataSet createBisonSomClustering(SomCluster somCluster,
			CSMicroarraySetView view) {
		log.debug("creating bison som cluster");

		int width = somCluster.getWidth();
		int height = somCluster.getHeight();
		// Initialize width x height Bison SOM Cluster
		SOMCluster[][] bisonSomCluster = new SOMCluster[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				bisonSomCluster[x][y] = new DefaultSOMCluster();
				bisonSomCluster[x][y].setGridCoordinates(x, y);
			}
		}
		// Assign each marker to its appropriate cluster
		for (int i = 0; i < somCluster.getXCoordinate().length; i++) {
			int x = somCluster.getXCoordinate(i);
			int y = somCluster.getYCoordinate(i);
			DSGeneMarker marker = (DSGeneMarker) view.getMicroarraySet()
					.getMarkers().get(i);
			LeafSOMCluster node = new LeafSOMCluster(marker);
			bisonSomCluster[x][y].addNode(node);
		}

		// Build final result set
		CSSOMClusterDataSet dataSet = new CSSOMClusterDataSet(bisonSomCluster,
				SOM_CLUSTERING_NAME, view);

		return dataSet;
	}

	/**
	 * 
	 * @param microarraySet
	 * @param method
	 * @param dimensions
	 * @param distance
	 * @param url
	 * @return DSHierClusterDataSet
	 * @throws Exception
	 */
	@Script
	public DSHierClusterDataSet doClustering(DSMicroarraySet microarraySet,
			String method, String dimensions, String distance, String url)
			throws Exception {
		log.debug("script method:  do clustering");
		CSMicroarraySetView view = new CSMicroarraySetView(microarraySet);
		MicroarraySet gridSet = CagridMicroarrayTypeConverter
				.convertToCagridMicroarrayType(view);
		HierarchicalClusteringParameter parameters = new HierarchicalClusteringParameter(
				dimensions, distance, method);
		HierarchicalClusteringClient client = new HierarchicalClusteringClient(
				url);
		HierarchicalCluster hierarchicalCluster = client.execute(gridSet,
				parameters);
		if (hierarchicalCluster != null) {
			CSHierClusterDataSet dataSet = createBisonHierarchicalClustering(
					hierarchicalCluster, view);
			return dataSet;
		} else {
			return null;
		}
	}

	/**
	 * @param name
	 * @return DSMicroarray
	 */
	private DSMicroarray getArray(String name) {
		for (DSMicroarray array : microarraySet) {
			if (array.getLabel().equals(name)) {
				return array;
			}
		}
		return null;
	}

	/**
	 * @param node
	 * @return MicroarrayHierCluster
	 */
	private MicroarrayHierCluster convertToMicroarrayHierCluster(
			HierarchicalClusterNode node) {
		log
				.debug("converting hierarchical cluster from bison to grid microarray cluster");
		MicroarrayHierCluster cluster;
		if (node.isLeaf()) {
			cluster = new MicroarrayHierCluster();
			cluster.setMicroarray(getArray(node.getLeafLabel()));
		} else {
			MicroarrayHierCluster left = convertToMicroarrayHierCluster(node
					.getHierarchicalClusterNode(0));
			MicroarrayHierCluster right = convertToMicroarrayHierCluster(node
					.getHierarchicalClusterNode(1));
			cluster = new MicroarrayHierCluster();
			cluster.setDepth(Math.max(left.getDepth(), right.getDepth()) + 1);
			cluster.setHeight(node.getHeight());
			cluster.addNode(left, 0);
			cluster.addNode(right, 0);
		}
		return cluster;
	}

	/**
	 * @param node
	 * @return MarkerHierCluster
	 */
	private MarkerHierCluster convertToMarkerHierCluster(
			HierarchicalClusterNode node) {
		log
				.debug("convert hierarchical cluster from bison to grid marker cluster");
		MarkerHierCluster cluster;
		if (node.isLeaf()) {
			cluster = new MarkerHierCluster();
			cluster.setMarkerInfo(microarraySet.getMarkers().get(
					node.getLeafLabel()));
		} else {
			MarkerHierCluster left = convertToMarkerHierCluster(node
					.getHierarchicalClusterNode(0));
			MarkerHierCluster right = convertToMarkerHierCluster(node
					.getHierarchicalClusterNode(1));
			cluster = new MarkerHierCluster();
			cluster.setDepth(Math.max(left.getDepth(), right.getDepth()) + 1);
			cluster.setHeight(node.getHeight());
			cluster.addNode(left, 0);
			cluster.addNode(right, 0);
		}
		return cluster;
	}

}
