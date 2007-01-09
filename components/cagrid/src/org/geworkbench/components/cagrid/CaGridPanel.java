package org.geworkbench.components.cagrid;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.DSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.bison.model.clusters.MarkerHierCluster;
import org.geworkbench.bison.model.clusters.MicroarrayHierCluster;
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
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalCluster;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusteringParameter;
import edu.columbia.geworkbench.cagrid.converter.CagridMicroarrayTypeConverter;
import edu.columbia.geworkbench.cagrid.discovery.client.AnalyticalServiceDiscoveryClient;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySet;
import gov.nih.nci.cagrid.discovery.MetadataUtils;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;

/**
 * @author John Watkinson
 * @version $Id: CaGridPanel.java,v 1.3 2007-01-09 16:15:37 keshav Exp $
 */
public class CaGridPanel extends JPanel implements VisualPlugin {

	static Log log = LogFactory.getLog(CaGridPanel.class);

	private static final String DEFAULT_HOST = "cagridnode.c2b2.columbia.edu";

	private static final int DEFAULT_PORT = 8080;

	private static final String HIERARCHICAL_CLUSTERING = "HierarchicalClustering";

	private static final String SOM_CLUSTERING = "SomClustering";

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
					AnalyticalServiceDiscoveryClient client = new AnalyticalServiceDiscoveryClient(
							hostField.getText(), port);
					EndpointReferenceType[] allServices = client
							.getAllServices();
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
						// HierarchicalClusteringParameter parameters = new
						// HierarchicalClusteringParameter("microarray",
						// "euclidean", "total");
						if (url.contains(HIERARCHICAL_CLUSTERING)) {
							GridHierarchicalClusteringDialog dialog = new GridHierarchicalClusteringDialog();
							HierarchicalClusteringParameter parameters = dialog
									.getParameters();
							if (parameters == null) {
								// Cancelled dialog
								return;
							}
							pBar.setTitle("caGrid");
							pBar.setMessage("Running Hierarchical Clustering");
							pBar.start();
							pBar.reset();
							HierarchicalClusteringClient client = new HierarchicalClusteringClient(
									url);
							HierarchicalCluster hierarchicalCluster = client
									.execute(gridSet, parameters);
							if (hierarchicalCluster != null) {
								// Create Bison-friendly structure
								CSHierClusterDataSet dataSet = createBisonClustering(
										hierarchicalCluster, view);
								ProjectNodeAddedEvent event = new ProjectNodeAddedEvent(
										"Hierarchical Clustering", null,
										dataSet);
								publishProjectNodeAddedEvent(event);
							}
						} else if (url.contains(SOM_CLUSTERING)) {

						} else {
							log.info("No services exist at " + url);
							// TODO add error message
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

	private CSHierClusterDataSet createBisonClustering(
			HierarchicalCluster hierarchicalCluster, CSMicroarraySetView view) {
		log.debug("creating bison cluster");
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
				"Hierarchical Clustering", view);
		return dataSet;
	}

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
			CSHierClusterDataSet dataSet = createBisonClustering(
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
