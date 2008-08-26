/*
 * The cytoscape project
 * 
 * Copyright (c) 2008 Columbia University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.geworkbench.components.cytoscape;

import giny.model.Node;
import giny.view.GraphViewChangeEvent;
import giny.view.GraphViewChangeListener;
import giny.view.NodeView;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenuBar;
import javax.swing.UIManager;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Script;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.AdjacencyMatrixEvent;
import org.geworkbench.events.GeneTaggedEvent;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.Util;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;
import org.geworkbench.util.pathwaydecoder.mutualinformation.IAdjacencyMatrix;

import phoebe.PNodeView;
import yfiles.YFilesLayout;

import com.jgoodies.plaf.FontSizeHints;
import com.jgoodies.plaf.Options;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeConfig;
import cytoscape.CytoscapeObj;
import cytoscape.CytoscapeVersion;
import cytoscape.data.Semantics;
import cytoscape.data.servers.BioDataServer;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.VisualStyle;

/**
 * 
 * @author manjunath
 * @author zji
 * @version $Id: CytoscapeWidget.java,v 1.23 2008-08-26 17:37:57 jiz Exp $
 */

@AcceptTypes({AdjacencyMatrixDataSet.class})
public class CytoscapeWidget implements VisualPlugin, MenuListener {
	private boolean publishEnabled = true;

	/** This is only used to check if an edge already exist in a set. */
    private class Edge {
        private String node1, node2;

        public int hashCode() { return node1.hashCode()^node2.hashCode(); }

        private Edge(String s1, String s2) {
            this.node1 = s1;
            this.node2 = s2;
        }

        public boolean equals(Object obj) {
        	if(!(obj instanceof Edge))return false;
            Edge edge = (Edge) obj;
            return ((node1.equals(edge.node1) && (node2.equals(edge.node2))) 
            		||  (node1.equals(edge.node2) && (node2.equals(edge.node1))));
        }
    }

    private CytoscapeVersion version = new CytoscapeVersion();
    private Logger logger;
    private boolean uiSetup = false;
    private DSMicroarraySet<DSMicroarray> maSet = null;
    private AdjacencyMatrixDataSet adjSet = null;
    private CyNetwork cytoNetwork = null;
    private AdjacencyMatrix adjMatrix = null;
    private CyNetworkView view = null;
    private Vector<IAdjacencyMatrix> adjStorage = new Vector<IAdjacencyMatrix>(); // to store the adjacency matrices it received
    private HashSet<String> dataSetIDs = new HashSet<String>();
    private VisualStyle interactionsVisualStyle = VisualStyleFactory.createDefaultVisualStyle();

    public CytoscapeWidget() {
        String[] args = new String[]{"-b", "annotation/manifest", "--JLD", "plugins"};
        UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
        Options.setGlobalFontSizeHints(FontSizeHints.MIXED);
        Options.setDefaultIconSize(new Dimension(18, 18));

        init(args);
        publishEnabled = true;
    }

    /**
     * <code>VisualPlugin</code>'s only method
     *
     * @return <code>Component</code> the view for this component
     */
    public Component getComponent() {
        JMenuBar menuBar = Cytoscape.getDesktop().getCyMenus().getMenuBar();
        Container contentPane = Cytoscape.getDesktop().getContentPane();
        final CytoscapeObj theCytoscapeObj = Cytoscape.getCytoscapeObj();
        if (!uiSetup) {
            Component[] components = contentPane.getComponents();
            contentPane.removeAll();
            contentPane.add(menuBar, BorderLayout.NORTH);
            contentPane.add(components[0], BorderLayout.CENTER);
            contentPane.add(components[1], BorderLayout.SOUTH);
            uiSetup = true;
        }

        contentPane.addComponentListener(new ComponentListener() {
            public void componentHidden(ComponentEvent e) {
                theCytoscapeObj.saveCalculatorCatalog();
            }

            public void componentMoved(ComponentEvent e) {
                theCytoscapeObj.saveCalculatorCatalog();
            }

            public void componentResized(ComponentEvent e) {
                theCytoscapeObj.saveCalculatorCatalog();
            }

            public void componentShown(ComponentEvent e) {
                theCytoscapeObj.saveCalculatorCatalog();
            }
        });

        return contentPane;
    }

    protected void init(String[] args) {

        CytoscapeConfig config = new CytoscapeConfig(args);

        if (config.helpRequested()) {
            displayHelp(config);
        } else if (config.inputsError()) {
            inputError(config);
        } else if (config.displayVersion()) {
            displayHelp(config);
        }

        setupLogger(config);
        logger.info(config.toString());

        CytoscapeObj cytoscapeObj = new CytoscapeObj(config);

        String bioDataDirectory = config.getBioDataDirectory();
        BioDataServer bioDataServer = Cytoscape.loadBioDataServer(bioDataDirectory); // null;

        boolean canonicalize = Semantics.getCanonicalize(cytoscapeObj);
        String defaultSpecies = Semantics.getDefaultSpecies(null, cytoscapeObj);

        Cytoscape.getDesktop().setVisible(false);

        for(Object geometry: config.getGeometryFilenames()) {
            // this method returns a CyNetwork, but not used 
            Cytoscape.createNetwork((String)geometry, Cytoscape.FILE_GML, false, null, null);
        }
        for(Object interaction: config.getInteractionsFilenames()) {
            Cytoscape.createNetwork((String)interaction, Cytoscape.FILE_SIF, canonicalize, bioDataServer, defaultSpecies);
        }

        logger.info("reading attribute files");
        Cytoscape.loadAttributes(config.getNodeAttributeFilenames(), config.getEdgeAttributeFilenames(), canonicalize, bioDataServer, defaultSpecies);
        logger.info(" done");

        String expDataFilename = config.getExpressionFilename();
        if (expDataFilename != null) {
            logger.info("reading " + expDataFilename + "...");
            try {
                Cytoscape.loadExpressionData(expDataFilename, config.getWhetherToCopyExpToAttribs());
            } catch (Exception e) {
                logger.severe("Exception reading expression data file '" + expDataFilename + "'");
                logger.severe(e.getMessage());
                e.printStackTrace();
            }
            logger.info("  done");
        }
        Cytoscape.getDesktop().setupPlugins();
    }

    protected void displayHelp(CytoscapeConfig config) {
        System.out.println(version);
        System.out.println(config.getUsage());
    }

    protected void inputError(CytoscapeConfig config) {
        System.out.println(version);
        System.out.println("------------- Inputs Error");
        System.out.println(config.getUsage());
        System.out.println(config);
    }

    /**
     * configure logging:  cytoscape.props specifies what level of logging
     * messages are written to the console; by default, only SEVERE messages
     * are written.  in time, more control of logging (i.e., optional logging
     * to a file, disabling console logging, per-window or per-plugin logging)
     * can be provided
     */
    protected void setupLogger(CytoscapeConfig config) {
        logger = Logger.getLogger("global");
        Properties properties = config.getProperties();
        String level = properties.getProperty("logging", "SEVERE");

        if (level.equalsIgnoreCase("severe")) {
            logger.setLevel(Level.SEVERE);
        } else if (level.equalsIgnoreCase("warning")) {
            logger.setLevel(Level.WARNING);
        } else if (level.equalsIgnoreCase("info")) {
            logger.setLevel(Level.INFO);
        } else if (level.equalsIgnoreCase("config")) {
            logger.setLevel(Level.CONFIG);
        } else if (level.equalsIgnoreCase("all")) {
            logger.setLevel(Level.ALL);
        } else if (level.equalsIgnoreCase("none")) {
            logger.setLevel(Level.OFF);
        } else if (level.equalsIgnoreCase("off")) {
            logger.setLevel(Level.OFF);
        }
    }

    /**
     * getActionListener
     *
     * @param var String
     * @return ActionListener
     */
    public ActionListener getActionListener(String var) {
        return null;
    }

    public AdjacencyMatrix getSelectedAdjMtx() {
        int idx = Integer.parseInt(Cytoscape.getCurrentNetwork().getIdentifier());
        return getStoredAdjMtx(idx);
    }

    public AdjacencyMatrix getStoredAdjMtx(int idx) {
        return (AdjacencyMatrix) this.adjStorage.get(idx);
    }

    /**
     * drawNetwork
     *
     * @param ae AdjacencyMatrixEvent
     */
    public void drawNetwork(AdjacencyMatrixEvent ae) {
        if (ae.getNetworkFocus() == -1) {
            drawCompleteNetwork(ae.getAdjacencyMatrix(), ae.getThreshold());
        } else {
            drawCentricNetwork(ae.getAdjacencyMatrix(), ae.getNetworkFocus(), ae.getThreshold(), ae.getDisplayDepth());
        }
    }

    public void drawCompleteNetwork(AdjacencyMatrix adjMatrix, double threshold) {
        this.adjMatrix = adjMatrix;
        HashSet<Edge> edgeSet = new HashSet<Edge>();
        for(Integer key: adjMatrix.getGeneRows().keySet()) {
            createSubNetwork(key.intValue(), threshold, 1, edgeSet);
        }
    }

    public void drawCentricNetwork(AdjacencyMatrix adjMatrix, int networkFocus, double threshold, int depth) {
        this.adjMatrix = adjMatrix;
        createSubNetwork(networkFocus, threshold, depth, null);
    }

	private void createSubNetwork(int geneId, double threshold, int level, HashSet<Edge> edgeSet) {
        if (level == 0) {
			return;
		}

		// get the first neighbors of gene(geneID)
		@SuppressWarnings("unchecked")
		HashMap<Integer, Float> map = adjMatrix.get(geneId);
		if (map == null)
			return;

		DSGeneMarker gm1 = (DSGeneMarker) maSet.getMarkers().get(geneId);
		int gm1Size = adjMatrix.getConnectionNo(gm1.getSerial(), threshold);

		for (Integer id2: map.keySet()) {
			if ((id2.intValue() < maSet.getMarkers().size())) {
				boolean thresholdTest = false;
				String type = null;
				if (adjMatrix.getInteraction(gm1.getSerial()) != null)
					type = (String) adjMatrix.getInteraction(gm1.getSerial())
							.get(id2);
				Float v12 = (Float) map.get(id2);
				thresholdTest = v12.doubleValue() > threshold;
				if (thresholdTest) {
					DSGeneMarker gm2 = (DSGeneMarker) maSet.getMarkers().get(
							id2.intValue());
					int gm2Size = adjMatrix.getConnectionNo(gm2.getSerial(),
							threshold);
					String geneName1 = gm1.getShortName();
					String geneName2 = gm2.getShortName();
					if (!geneName2.equalsIgnoreCase(geneName1)) {
						String g1Name = gm1.getShortName();
						String g2Name = gm2.getShortName();
						CyNode n1 = Cytoscape.getCyNode(g1Name);
						CyNode n2 = Cytoscape.getCyNode(g2Name);
						if (n1 == null) {
							n1 = Cytoscape.getCyNode(g1Name, true);
							n1.setIdentifier(g1Name);
							cytoNetwork.setNodeAttributeValue(n1, "Unigene",
									gm1.getUnigene().getUnigeneAsString());
							cytoNetwork.setNodeAttributeValue(n1, "Serial",
									new Integer(gm1.getSerial()));
						}
						cytoNetwork.setNodeAttributeValue(n1, "HubSize",
								new String(g1Name + ": "
										+ Integer.toString(gm1Size)));
						cytoNetwork.setNodeAttributeValue(n1, "Hub",
								new Integer(gm1Size));
						cytoNetwork.addNode(n1);
						if (n2 == null) {
							n2 = Cytoscape.getCyNode(g2Name, true);
							n2.setIdentifier(g2Name);
							cytoNetwork.setNodeAttributeValue(n2, "Unigene",
									gm2.getUnigene().getUnigeneAsString());
							cytoNetwork.setNodeAttributeValue(n2, "Serial",
									new Integer(gm2.getSerial()));
						}
						cytoNetwork.setNodeAttributeValue(n2, "HubSize",
								new String(g2Name + ": "
										+ Integer.toString(gm2Size)));
						cytoNetwork.setNodeAttributeValue(n2, "Hub",
								new Integer(gm2Size));
						cytoNetwork.addNode(n2);
						CyEdge e = null;
						// Only add edge if there is not already an edge for
						// these genes
						// Or add it regardless if the edgeSet is null
						Edge edge = new Edge(g1Name, g2Name);
						if (edgeSet == null || !edgeSet.contains(edge)) {
							if (edgeSet != null) {
								edgeSet.add(edge);
							}
							if (type != null) {
								if (gm1.getSerial() > gm2.getSerial()) {
									e = Cytoscape.getCyEdge(g1Name, g1Name
											+ "." + type + "." + g2Name,
											g2Name, type);
								} else {
									e = Cytoscape.getCyEdge(g2Name, g2Name
											+ "." + type + "." + g1Name,
											g1Name, type);
								}
								cytoNetwork.addEdge(e);
							} else {
								if (gm1.getSerial() > gm2.getSerial()) {
									e = Cytoscape.getCyEdge(g1Name, g1Name
											+ ".pp." + g2Name, g2Name, "pp");
								} else {
									e = Cytoscape.getCyEdge(g2Name, g2Name
											+ ".pp." + g1Name, g1Name, "pp");
								}
								cytoNetwork.addEdge(e);
							}
							createSubNetwork(gm2.getSerial(), threshold,
									level - 1, null);
						}
					}
				}
			}
		}
    }

    private void cyNetWorkView_graphViewChanged(GraphViewChangeEvent gvce) {
        if (Cytoscape.getCurrentNetworkView() != null && Cytoscape.getCurrentNetwork() != null) {
            DSPanel<DSGeneMarker> selectedMarkers = new CSPanel<DSGeneMarker>("Selected Genes", "Cytoscape");
            for (Object nodeView: Cytoscape.getCurrentNetworkView().getSelectedNodes()) {
                PNodeView pnode = (PNodeView) nodeView;
                Node node = pnode.getNode();
                if (node instanceof CyNode) {
                    int serial = ((Integer) Cytoscape.getCurrentNetworkView().getNetwork().getNodeAttributeValue((CyNode) node, "Serial")).intValue();
                    selectedMarkers.add(maSet.getMarkers().get(serial));
                }
            }
            selectedMarkers.setActive(true);
            if(publishEnabled) // skip if GeneTaggedEvent is being processed to avoid event cycle
            publishSubpanelChangedEvent(
            		new SubpanelChangedEvent<DSGeneMarker>(DSGeneMarker.class, selectedMarkers, SubpanelChangedEvent.SET_CONTENTS));
        }
    }

    @Publish
    public SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(SubpanelChangedEvent<DSGeneMarker> event) {
        return event;
    }

    /**
	 * receiveProjectSelection
	 * 
	 * @param e
	 *            ProjectEvent
	 */
    @SuppressWarnings("unchecked") // for unchecked generic in ProjectEvent.getDataSet() and AdjacencyMatrix.getMicroarraySet()
	@Subscribe public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
        DSDataSet<? extends DSBioObject> dataSet = e.getDataSet();
        if (dataSet instanceof AdjacencyMatrixDataSet) {
            adjSet = (AdjacencyMatrixDataSet) dataSet;
            maSet = adjSet.getMatrix().getMicroarraySet();
            boolean found = false;
            String foundID = null;
            if (!dataSetIDs.contains(adjSet.getID())) {
                dataSetIDs.add(adjSet.getID());
            } else {
                for (Object networkId: Cytoscape.getNetworkSet()) {
                    String id = (String) networkId;
                    CyNetwork network = Cytoscape.getNetwork(id);
                    String title = network.getTitle();
                    if (title.equals(adjSet.getNetworkName())) {
                        found = true;
                        foundID = id;
                        break;
                    }
                }
            }
            if (!found) {
                receiveMatrix();
            } else {
                Cytoscape.getDesktop().getNetworkPanel().focusNetworkNode(foundID);
            }
        }
    }

    private void receiveMatrix() {
        // 1) RECEIVE event
        String name = adjSet.getNetworkName();
        String tmpname = adjSet.getMatrix().getLabel();
        if ((tmpname != null) && (!name.contains(tmpname))) {
            name = tmpname + " [" + name + "]";
        }
        HashSet<String> names = new HashSet<String>();
        for (Object networkId: Cytoscape.getNetworkSet()) {
            String id = (String) networkId;
            CyNetwork network = Cytoscape.getNetwork(id);
            String title = network.getTitle();
            names.add(title);
        }
        name = Util.getUniqueName(name, names);
        adjSet.setNetworkName(name);
        cytoNetwork = Cytoscape.createNetwork(name);
        // 2) DRAW NETWORK event
        if (adjSet.getGeneId() == -1) {
            drawCompleteNetwork(adjSet.getMatrix(), adjSet.getThreshold());
        } else {
            drawCentricNetwork(adjSet.getMatrix(), adjSet.getGeneId(), adjSet.getThreshold(), adjSet.getDepth());
        }
        // 3) FINISH event
        if (maSet != null) {
            view = Cytoscape.createNetworkView(cytoNetwork, maSet.getLabel());
            view.addGraphViewChangeListener(new GraphViewChangeListener() {
                public void graphViewChanged(GraphViewChangeEvent graphViewChangeEvent) {
                    cyNetWorkView_graphViewChanged(graphViewChangeEvent);
                }
            });
        }
    }

    /**
     * phenotypeSelectorAction
     *
     * @param e PhenotypeSelectorEvent
     */
    @Subscribe public void receive(PhenotypeSelectorEvent<? extends DSMicroarray> e, Object source) {
    }

    @Subscribe public void receive(AdjacencyMatrixEvent ae, Object source) {
        switch (ae.getAction()) {
            case LOADED: {
                // no-op
            }
            break;
            case RECEIVE: {
                if (ae.getAdjacencyMatrix().getSource() == AdjacencyMatrix.fromGeneNetworkPanelTakenGoodCareOf) {
                    // this adj mtx should be sent only to EviInt panel since it has been drawn
                    // by GeneNetworkPanel once
                    return;
                }
                String name = (maSet == null ? "Test" : maSet.getLabel());
                String tmpname = ae.getAdjacencyMatrix().getLabel();
                if (tmpname != null) {
                    name = tmpname + " [" + name + "]";
                }
                cytoNetwork = Cytoscape.createNetwork(name);
            }
            break;
            case DRAW_NETWORK: {
                if (ae.getNetworkFocus() == -1) {
                    drawCompleteNetwork(ae.getAdjacencyMatrix(), ae.getThreshold());
                } else {
                    drawCentricNetwork(ae.getAdjacencyMatrix(), ae.getNetworkFocus(), ae.getThreshold(), ae.getDisplayDepth());
                }
                new YFilesLayout(Cytoscape.getCurrentNetworkView()).doLayout(YFilesLayout.g, 0.0D);

                Cytoscape.getCurrentNetworkView().applyVizmapper(interactionsVisualStyle);
                Cytoscape.getCurrentNetworkView().fitContent();
            }
            break;
            case FINISH: {
                if (maSet != null) {
                    view = Cytoscape.createNetworkView(cytoNetwork, maSet.getLabel());
                    view.addGraphViewChangeListener(new GraphViewChangeListener() {
                        public void graphViewChanged(GraphViewChangeEvent graphViewChangeEvent) {
                            cyNetWorkView_graphViewChanged(graphViewChangeEvent);
                        }
                    });
                }
            }
            break;
        }
    }

    @SuppressWarnings("unchecked") // IAdjacencyMatrix.getMicroarraySet()
	public void setAdjacencyMatrix(IAdjacencyMatrix adjMat) {
        String name = ((maSet = adjMat.getMicroarraySet()) == null ? "Test" : maSet.getLabel());
        cytoNetwork = Cytoscape.createNetwork(name);
        this.adjStorage.add(adjMat);
    }

    @Script
    public DSPanel<DSGeneMarker> getFirstNeighbors(int geneid, double threshold, int level) {
        createSubNetwork(geneid, threshold, level, null);
        int[] indices = cytoNetwork.neighborsArray(geneid);
        DSPanel<DSGeneMarker> selectedMarkers = new CSPanel<DSGeneMarker>("First Neighbors", cytoNetwork.getNode(geneid).getIdentifier());
        for (int i = 0; i < indices.length; i++) {
            Node node = cytoNetwork.getNode(indices[i]);
            if (node instanceof CyNode) {
                int serial = ((Integer) cytoNetwork.getNodeAttributeValue((CyNode) node, "Serial")).intValue();
                selectedMarkers.add(maSet.getMarkers().get(serial));
            }
            selectedMarkers.setActive(true);
        }
        return selectedMarkers;
    }

    @SuppressWarnings("unchecked") // AdjacencyMatrix.getMicroarraySet()
	@Script
    public void computeAndDrawFirstNeighbors(DSGeneMarker m, AdjacencyMatrix am) {
        adjMatrix = am;
        maSet = am.getMicroarraySet();
        cytoNetwork = Cytoscape.createNetwork(maSet.getLabel() + m.getSerial());
        createSubNetwork(m.getSerial(), 0, 1, null);
        view = Cytoscape.createNetworkView(cytoNetwork, maSet.getLabel());
        view.addGraphViewChangeListener(new GraphViewChangeListener() {
            public void graphViewChanged(GraphViewChangeEvent graphViewChangeEvent) {
                cyNetWorkView_graphViewChanged(graphViewChangeEvent);
            }
        });
    }

    @Script
    public void computeAndDrawFirstNeighbors(DSPanel<DSGeneMarker> p, int i, AdjacencyMatrix am) {
        adjMatrix = am;
        cytoNetwork = Cytoscape.createNetwork(maSet.getLabel() + i);
        createSubNetwork(p.get(i).getSerial(), 0, 1, null);
        view = Cytoscape.createNetworkView(cytoNetwork, maSet.getLabel());
        view.addGraphViewChangeListener(new GraphViewChangeListener() {
            public void graphViewChanged(GraphViewChangeEvent graphViewChangeEvent) {
                cyNetWorkView_graphViewChanged(graphViewChangeEvent);
            }
        });
    }
    
    
    /**
     * Update selection in visualization when the gene selection is changed
     *
     * @param e GeneSelectorEvent
     */
    @SuppressWarnings("unchecked") // for Iterator<NodeView> iter
	@Subscribe
    public void receive(GeneTaggedEvent e, Object source) {
    	DSPanel<DSGeneMarker> panel = e.getPanel();
    	if(panel==null){
    		System.err.print("panel is null in "+new Exception().getStackTrace()[0].getMethodName());
    		return;
    	}

    	List<String> selected = new ArrayList<String>();
    	for(DSGeneMarker m: panel) {
    		selected.add(m.getLabel().trim().toUpperCase());
    	}
    	
    	Iterator<NodeView> iter = Cytoscape.getCurrentNetworkView().getNodeViewsIterator();
    	publishEnabled = false;
    	while(iter.hasNext()) {
    		NodeView nodeView = iter.next();
    		if(selected.contains(nodeView.getLabel().getText().trim().toUpperCase()))nodeView.select();
    		else nodeView.unselect();
    	}
    	
    	publishEnabled = true;
    }
}