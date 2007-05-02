package org.geworkbench.components.cytoscape_v2_4;

import com.jgoodies.plaf.FontSizeHints;
import com.jgoodies.plaf.Options;
import com.jgoodies.plaf.LookUtils;
import com.jgoodies.plaf.plastic.Plastic3DLookAndFeel;
import cytoscape.*;
import cytoscape.Cytoscape;
import cytoscape.giny.FingCyNetwork;
import cytoscape.util.FileUtil;
import cytoscape.init.CyInitParams;
import cytoscape.view.CyNetworkView;
//import cytoscape.view.CyWindow;
import cytoscape.visual.VisualStyle;
import giny.model.Node;
import giny.view.GraphViewChangeEvent;
import giny.view.GraphViewChangeListener;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Script;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.AdjacencyMatrixEvent;
import org.geworkbench.util.Util;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;
import org.geworkbench.util.pathwaydecoder.mutualinformation.IAdjacencyMatrix;
import org.apache.commons.cli.*;
import phoebe.PNodeView;
import phoebe.event.PSelectionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.InputStream;
import java.io.IOException;

import yfiles.YFilesLayoutPlugin;
import yfiles.OrganicLayout;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence
 * and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author manjunath at genomecenter dot columbia dot edu
 * @version 1.0
 */

@AcceptTypes({AdjacencyMatrixDataSet.class})
public class CytoscapeWidget implements VisualPlugin, MenuListener, CyInitParams {

    protected Vector windows = new Vector();
    protected CytoscapeVersion version = new CytoscapeVersion();
    protected Logger logger;
    protected boolean uiSetup = false;
    protected DSMicroarraySet maSet = null;
    protected AdjacencyMatrixDataSet adjSet = null;
    protected CyNetwork cytoNetwork = null;
    protected AdjacencyMatrix adjMatrix = null;
    protected CyNetworkView view = null;
    PSelectionHandler selectionHandler = null;
    protected Vector adjStorage = new Vector(); // to store the adjacency matrices it received
    protected HashSet<String> dataSetIDs = new HashSet<String>();
    protected VisualStyle interactionsVisualStyle = VisualStyleFactory.createDefaultVisualStyle();


    protected String[] args;
    protected Properties props;
    protected String[] graphFiles;
    protected String[] plugins;
    protected Properties vizmapProps;
    protected String sessionFile;
    protected String[] nodeAttrFiles;
    protected String[] edgeAttrFiles;
    protected String[] expressionFiles;
    protected int mode;
    protected org.apache.commons.cli.Options options;

    // mapping for genewaysNetwork, in which each geneId if mapped to
    // corresponding swissprot Id
    private HashMap genewaysGeneidNameMap = new HashMap();


    public CytoscapeWidget() {
        //String[] args = new String[]{"-b", "annotation/manifest", "--JLD", "plugins"};
        String[] args = new String[]{"-b", "annotation/manifest", "-p plugins_v2_4"};
        /*UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
        Options.setGlobalFontSizeHints(FontSizeHints.MIXED);
        Options.setDefaultIconSize(new Dimension(18, 18));*/

        init(args);
    }

    /**
     * <code>VisualPlugin</code> method
     *
     * @return <code>Component</code> the view for this component
     */
    public Component getComponent() {
        JMenuBar menuBar = Cytoscape.getDesktop().getCyMenus().getMenuBar();
        Container contentPane = Cytoscape.getDesktop().getContentPane();
        //final CytoscapeObj theCytoscapeObj = Cytoscape.getCytoscapeObj();
        if (!uiSetup) {
            Component[] components = contentPane.getComponents();
            contentPane.removeAll();
            contentPane.add(menuBar, BorderLayout.NORTH);
            contentPane.add(components[0], BorderLayout.CENTER);
            contentPane.add(components[1], BorderLayout.SOUTH);
            uiSetup = true;
        }

        /*contentPane.addComponentListener(new ComponentListener() {
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
        });  */

        return contentPane;
    }

    /* protected void init(String[] args) {

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

        Iterator gi = config.getGeometryFilenames().iterator();
        Iterator ii = config.getInteractionsFilenames().iterator();
        while (gi.hasNext()) {
            CyNetwork network = Cytoscape.createNetwork((String) gi.next(), Cytoscape.FILE_GML, false, null, null);
        }
        while (ii.hasNext()) {
            Cytoscape.createNetwork((String) ii.next(), Cytoscape.FILE_SIF, canonicalize, bioDataServer, defaultSpecies);
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
    }  */

    protected void init(String[] args) {
        Cytoscape.getDesktop().setVisible(false);

        props = null;
        graphFiles = null;
        plugins = null;
        vizmapProps = null;
        sessionFile = null;
        nodeAttrFiles = null;
        edgeAttrFiles = null;
        expressionFiles = null;
        this.args = args;
        mode = CyInitParams.ERROR;
        options = new org.apache.commons.cli.Options();

        //for (String asdf: args)
        //	System.out.println("arg: '" + asdf + "'");

        parseCommandLine(args);
        CytoscapeInit initializer = new CytoscapeInit();

        if ( !initializer.init(this) ) {
            printHelp();
            Cytoscape.exit(1);
        }
    }

    protected void parseCommandLine(String args[]) {

        // create the options
        options.addOption("h", "help", false, "Print this message.");
        options.addOption("v", "version", false, "Print the version number.");
// commented out until we actually support doing anything in headless mode
//		options.addOption("H", "headless", false, "Run in headless (no gui) mode.");

        options.addOption(OptionBuilder
                                    .withLongOpt("session")
                                    .withDescription( "Load a cytoscape session (.cys) file.")
                                    .withValueSeparator(' ')
                                    .withArgName("file")
                                    .hasArg() // only allow one session!!!
                    .create("s"));

        options.addOption(OptionBuilder
                                    .withLongOpt("network")
                                    .withDescription( "Load a network file (any format).")
                                    .withValueSeparator(' ')
                                    .withArgName("file")
                                    .hasArgs()
                    .create("N"));

        options.addOption(OptionBuilder
                                    .withLongOpt("edge-attrs")
                                    .withDescription( "Load an edge attributes file (edge attribute format).")
                                    .withValueSeparator(' ')
                                    .withArgName("file")
                                    .hasArgs()
                    .create("e"));
        options.addOption(OptionBuilder
                                    .withLongOpt("node-attrs")
                                    .withDescription( "Load a node attributes file (node attribute format).")
                                    .withValueSeparator(' ')
                                    .withArgName("file")
                                    .hasArgs()
                    .create("n"));
        options.addOption(OptionBuilder
                                    .withLongOpt("matrix")
                                    .withDescription( "Load a node attribute matrix file (table).")
                                    .withValueSeparator(' ')
                                    .withArgName("file")
                                    .hasArgs()
                    .create("m"));


        options.addOption(OptionBuilder
                                    .withLongOpt("plugin")
                                    .withDescription( "Load a plugin jar file, directory of jar files, plugin class name, or plugin jar URL.")
                                    .withValueSeparator(' ')
                                    .withArgName("file")
                                    .hasArgs()
                    .create("p"));

        options.addOption(OptionBuilder
                                    .withLongOpt("props")
                                    .withDescription( "Load cytoscape properties file (Java properties format) or individual property: -P name=value.")
                    // the null value here is so that properties can have spaces in them
                                    .withValueSeparator('\0')
                                    .withArgName("file")
                                    .hasArgs()
                    .create("P"));
        options.addOption(OptionBuilder
                                    .withLongOpt("vizmap")
                                    .withDescription( "Load vizmap properties file (Java properties format).")
                                    .withValueSeparator(' ')
                                    .withArgName("file")
                                    .hasArgs()
                    .create("V"));

        // try to parse the cmd line
        CommandLineParser parser = new PosixParser();
        CommandLine line = null;

        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Parsing command line failed: " + e.getMessage());
            printHelp();
            System.exit(1);
        }

        // use what is found on the command line to set values
        if ( line.hasOption("h") ) {
            printHelp();
            System.exit(0);
        }

        if ( line.hasOption("v") ) {
            CytoscapeVersion version = new CytoscapeVersion();
            System.out.println(version.getVersion());
            System.exit(0);
        }

        mode = CyInitParams.TEXT;
        /*if ( line.hasOption("H") ) {
            mode = CyInitParams.TEXT;
        } else {
            mode = CyInitParams.GUI;
            //setupLookAndFeel();
        }*/

        if ( line.hasOption("P") )
            props = createProperties( line.getOptionValues("P") );
        else
            props = createProperties( new String[0] );

        if ( line.hasOption("N") )
            graphFiles = line.getOptionValues("N");

        if ( line.hasOption("p") )
            plugins = line.getOptionValues("p");

        if ( line.hasOption("V") )
            vizmapProps = createProperties( line.getOptionValues("V") );
        else
            vizmapProps = createProperties( new String[0] );

        if ( line.hasOption("s") )
            sessionFile = line.getOptionValue("s");

        if ( line.hasOption("n") )
            nodeAttrFiles = line.getOptionValues("n");

        if ( line.hasOption("e") )
            edgeAttrFiles = line.getOptionValues("e");

        if ( line.hasOption("m") )
            expressionFiles = line.getOptionValues("m");
    }

    protected void setupLookAndFeel() {

        UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
        UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
        Options.setGlobalFontSizeHints(FontSizeHints.MIXED);
        Options.setDefaultIconSize(new Dimension(18, 18));

        try {
            if ( LookUtils.isWindowsXP() ) {
                // use XP L&F
                UIManager.setLookAndFeel( Options.getSystemLookAndFeelClassName() );
            } else if ( System.getProperty("os.name").startsWith( "Mac" ) ) {
                // do nothing, I like the OS X L&F
            } else {
                // this is for for *nix
                // I happen to like this color combo, there are others
                // jgoodies
                Plastic3DLookAndFeel laf = new Plastic3DLookAndFeel();
                laf.setTabStyle( Plastic3DLookAndFeel.TAB_STYLE_METAL_VALUE );
                laf.setHighContrastFocusColorsEnabled(true);
                laf.setMyCurrentTheme( new com.jgoodies.plaf.plastic.theme.ExperienceBlue() );
                UIManager.setLookAndFeel( laf );
            }
        } catch (Exception e) {
            System.err.println("Can't set look & feel:" + e);
        }
    }

    public Properties getProps() {
        return props;
    }

    public Properties getVizProps() {
        return vizmapProps;
    }

    protected void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -Xmx512M -jar cytoscape.jar [OPTIONS]", options);
    }

    private Properties createProperties(String[] potentialProps) {

        //for ( String asdf: potentialProps)
        //	System.out.println("prop: '" + asdf + "'");

        Properties props = new Properties();
        Properties argProps = new Properties();

        Matcher propPattern = Pattern.compile("^((\\w+\\.*)+)\\=(.+)$").matcher("");

        for ( int i = 0; i < potentialProps.length; i++ ) {

            propPattern.reset(potentialProps[i]);

            // check to see if the string is a key value pair
            if ( propPattern.matches() ) {
                argProps.setProperty(propPattern.group(1),propPattern.group(3));

            // otherwise assume it's a file/url
            } else {
                try {
                InputStream in = FileUtil.getInputStream( potentialProps[i] );

                if ( in != null )
                    props.load(in);
                else
                    System.out.println("Couldn't load property: " + potentialProps[i]);
                } catch (IOException e) {
                    System.out.println("Couldn't load property: " + potentialProps[i]);
                    e.printStackTrace();
                }
            }
        }

        // Transfer argument properties into the full properties.
        // We do this so that anything specified on the command line
        // overrides anything specified in a file.
        props.putAll(argProps);

        return props;
    }

    public List getGraphFiles() {
        return createList( graphFiles );
    }

    public List getEdgeAttributeFiles() {
        return createList( edgeAttrFiles );
    }

    public List getNodeAttributeFiles() {
        return createList( nodeAttrFiles );
    }

    public List getExpressionFiles() {
        return createList( expressionFiles );
    }

    public List getPlugins() {
        return createList( plugins );
    }

    public String getSessionFile() {
        return sessionFile;
    }

    public int getMode() {
        return mode;
    }

    public String[] getArgs() {
        return args;
    }

    private List createList(String[] vals) {
        if ( vals == null )
            return new ArrayList();
        ArrayList a = new ArrayList(vals.length);
        for ( int i = 0; i < vals.length; i++ )
            a.add(i,vals[i]);

        return a;
    }


   /* protected void displayHelp(CytoscapeConfig config) {
        System.out.println(version);
        System.out.println(config.getUsage());
    }

    protected void inputError(CytoscapeConfig config) {
        System.out.println(version);
        System.out.println("------------- Inputs Error");
        System.out.println(config.getUsage());
        System.out.println(config);
    } */

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
        Iterator keysIt = adjMatrix.getGeneRows().keySet().iterator();
        while (keysIt.hasNext()) {
            int key = ((Integer) keysIt.next()).intValue();
            createSubNetwork(key, threshold, 1);
        }
    }

    public void drawCentricNetwork(AdjacencyMatrix adjMatrix, int networkFocus, double threshold, int depth) {
        this.adjMatrix = adjMatrix;
        createSubNetwork(networkFocus, threshold, depth);
    }

    /**
     * @param geneId    int
     * @param threshold double
     * @param level     int
     * @deprecated -- maybe not used anymore...
     *             create the subnetwork and label or filter the interactions <BR>
     *             using the tradition method (center -> neighbor + iteration) <BR>
     */
   /* private void createSubNetworkAndInteraction(int geneId, double threshold, int level) {
        if (level == 0) {
            return;
        }
        HashMap map = adjMatrix.getInteraction(geneId);

        if (map != null) {
            DSGeneMarker gm1 = (DSGeneMarker) maSet.getMarkers().get(geneId);
            for (Iterator it = map.keySet().iterator(); it.hasNext();) {
                Integer id2 = (Integer) it.next();
                if ((id2.intValue() < maSet.getMarkers().size()) && (id2.intValue() != 12600)) {
                    String actiontype = map.get(id2).toString();
                    boolean thresholdTest = false;
                    thresholdTest = true;
                    if (thresholdTest) {
                        DSGeneMarker gm2 = (DSGeneMarker) maSet.getMarkers().get(id2.intValue());

                        String geneName1 = gm1.getShortName();
                        String geneName2 = gm2.getShortName();
                        //                        The following line creates an issue if one is drawing just a single gene
                        //                        rather than the full matrix. We'll need to rethink how we draw complete networks
                        //                        In practice Cytoscape may already take care of it.
                        //                        if (gm1.getSerial() < gm2.getSerial()) {
                        if (!geneName2.equalsIgnoreCase(geneName1)) {
                            String g1Name = gm1.getShortName();
                            String g2Name = gm2.getShortName();

                            CyNode n1 = Cytoscape.getCyNode(g1Name);
                            CyNode n2 = Cytoscape.getCyNode(g2Name);
                            if (n1 == null) {
                                n1 = Cytoscape.getCyNode(g1Name, true);
                                n1.setIdentifier(g1Name);
                                cytoNetwork.setNodeAttributeValue(n1, "Unigene", gm1.getUnigene().getUnigeneAsString());
                                cytoNetwork.setNodeAttributeValue(n1, "Serial", new Integer(gm1.getSerial()));
                            }

                            cytoNetwork.addNode(n1);
                            if (n2 == null) {
                                n2 = Cytoscape.getCyNode(g2Name, true);
                                n2.setIdentifier(g2Name);
                                cytoNetwork.setNodeAttributeValue(n2, "Unigene", gm2.getUnigene().getUnigeneAsString());
                                cytoNetwork.setNodeAttributeValue(n2, "Serial", new Integer(gm2.getSerial()));
                            }

                            cytoNetwork.addNode(n2);

                            // set up the edge
                            CyEdge e = Cytoscape.getCyEdge(g1Name, g1Name + ".pp." + g2Name, g2Name, actiontype);
                            cytoNetwork.addEdge(e);

                            // draw the subnetwork of this node2
                            createSubNetwork(gm2.getSerial(), threshold, level - 1);
                        } else {
                        }
                    }
                }
            }
        }
    } */

    /*private void createSubNetwork(int geneId, double threshold, int level) {
        if (level == 0) {
            return;
        }
        // get the first neighbors of gene(geneID)
        HashMap map = adjMatrix.get(geneId);
        if (map != null) {
            DSGeneMarker gm1 = (DSGeneMarker) maSet.getMarkers().get(geneId);
            int gm1Size = adjMatrix.getConnectionNo(gm1.getSerial(), threshold);

            for (Iterator it = map.keySet().iterator(); it.hasNext();) {
                Integer id2 = (Integer) it.next();
                if ((id2.intValue() < maSet.getMarkers().size())) {
                    boolean thresholdTest = false;
                    String type = null;
                    if (adjMatrix.getInteraction(gm1.getSerial()) != null)
                        type = (String)adjMatrix.getInteraction(gm1.getSerial()).get(id2);
                    Float v12 = (Float) map.get(id2);
                    thresholdTest = v12.doubleValue() > threshold;
                    if (thresholdTest) {
                        DSGeneMarker gm2 = (DSGeneMarker) maSet.getMarkers().get(id2.intValue());
                        int gm2Size = adjMatrix.getConnectionNo(gm2.getSerial(), threshold);
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
                                //cytoNetwork.setNodeAttributeValue(n1, "Unigene", gm1.getUnigene().getUnigeneAsString());
                                //cytoNetwork.setNodeAttributeValue(n1, "Serial", new Integer(gm1.getSerial()));
                            }
                            //cytoNetwork.setNodeAttributeValue(n1, "HubSize", new String(g1Name + ": " + Integer.toString(gm1Size)));
                            //cytoNetwork.setNodeAttributeValue(n1, "Hub", new Integer(gm1Size));
                            cytoNetwork.addNode(n1);
                            if (n2 == null) {
                                n2 = Cytoscape.getCyNode(g2Name, true);
                                n2.setIdentifier(g2Name);
                                //cytoNetwork.setNodeAttributeValue(n2, "Unigene", gm2.getUnigene().getUnigeneAsString());
                                //cytoNetwork.setNodeAttributeValue(n2, "Serial", new Integer(gm2.getSerial()));
                            }
                            //cytoNetwork.setNodeAttributeValue(n2, "HubSize", new String(g2Name + ": " + Integer.toString(gm2Size)));
                            //cytoNetwork.setNodeAttributeValue(n2, "Hub", new Integer(gm2Size));
                            cytoNetwork.addNode(n2);
                            CyEdge e = null;
                            if (type != null){
                                if (gm1.getSerial() > gm2.getSerial()) {
                                    e = Cytoscape.getCyEdge(g1Name, g1Name + "." + type + "." + g2Name, g2Name, type);
                                } else {
                                    e = Cytoscape.getCyEdge(g2Name, g2Name + "." + type + "." + g1Name, g1Name, type);
                                }
                                cytoNetwork.addEdge(e);
                            }
                            else {
                                if (gm1.getSerial() > gm2.getSerial()) {
                                    e = Cytoscape.getCyEdge(g1Name, g1Name + ".pp." + g2Name, g2Name, "pp");
                                } else {
                                    e = Cytoscape.getCyEdge(g2Name, g2Name + ".pp." + g1Name, g1Name, "pp");
                                }
                                cytoNetwork.addEdge(e);
                            }
                            createSubNetwork(gm2.getSerial(), threshold, level - 1);
                        } else {
                        }
                    }
                }
            }
        }
    }
   */


    private void createSubNetwork(int geneId, double threshold, int level) {

        if (level == 0) {
            return;
        }
        // get the first neighbors of gene(geneID)
        HashMap map = adjMatrix.get(geneId);
        if (map != null) {
            for (Iterator it = map.keySet().iterator(); it.hasNext();) {
                Integer id2 = (Integer) it.next();
                boolean thresholdTest = false;
                String type = null;
                Float v12 = (Float) map.get(id2);
                thresholdTest = v12.doubleValue() > threshold;
                if (thresholdTest) {
                    CyNode n1 = Cytoscape.getCyNode(String.valueOf(geneId));
                    CyNode n2 = Cytoscape.getCyNode(String.valueOf(id2));
                    String n1Id = (String)genewaysGeneidNameMap.get(new Integer(geneId));
                    if (n1 == null) {
                        n1 = Cytoscape.getCyNode(String.valueOf(geneId), true);
                        if(n1Id == null)
                            n1.setIdentifier(String.valueOf(geneId));
                        else
                            n1.setIdentifier(n1Id);
                    }else{
                        if(n1Id != null)
                            n1.setIdentifier(n1Id);
                    }
                    //System.out.println("Adding node n1 !!!"+n1);
                    cytoNetwork.addNode(n1);
                    String n2Id = (String)genewaysGeneidNameMap.get(new Integer(id2));
                    if (n2 == null) {
                        n2 = Cytoscape.getCyNode(String.valueOf(id2), true);
                        //n2.setIdentifier(String.valueOf(id2));
                        if(n2Id == null)
                            n2.setIdentifier(String.valueOf(id2));
                        else
                            n2.setIdentifier(n2Id);
                    }else{
                        if(n2Id != null)
                            n2.setIdentifier(n2Id);
                    }
                    //System.out.println("Adding node n2 !!!"+n2);
                    cytoNetwork.addNode(n2);
                    CyEdge e = null;
                    if (type != null){
                        if (geneId > id2) {
                            e = Cytoscape.getCyEdge(String.valueOf(geneId), String.valueOf(geneId) + "." + type + "." + String.valueOf(id2), String.valueOf(id2), type);
                        } else {
                            e = Cytoscape.getCyEdge(String.valueOf(id2), String.valueOf(id2) + "." + type + "." + String.valueOf(geneId), String.valueOf(geneId), type);
                        }
                        cytoNetwork.addEdge(e);
                    }
                    else {
                        if (geneId > id2) {
                            e = Cytoscape.getCyEdge(String.valueOf(geneId), String.valueOf(geneId) + ".pp." + String.valueOf(id2), String.valueOf(id2), "");
                        } else {
                            e = Cytoscape.getCyEdge(String.valueOf(id2), String.valueOf(id2) + ".pp." + String.valueOf(geneId), String.valueOf(geneId), "");
                        }
                        cytoNetwork.addEdge(e);
                    }
                    createSubNetwork(id2, threshold, level - 1);
                }
            }
        }
        YFilesLayoutPlugin plugin = new YFilesLayoutPlugin();
        OrganicLayout organiclayout = new OrganicLayout(plugin);
        organiclayout.actionPerformed(null);
        Cytoscape.getCurrentNetworkView().applyVizmapper(interactionsVisualStyle);
        Cytoscape.getCurrentNetworkView().fitContent();
        
    }













    private void cyNetWorkView_graphViewChanged(GraphViewChangeEvent gvce) {
        if (Cytoscape.getCurrentNetworkView() != null && Cytoscape.getCurrentNetwork() != null) {
            java.util.List nodes = Cytoscape.getCurrentNetworkView().getSelectedNodes();
            DSPanel selectedMarkers = new CSPanel("Selected Genes", "Cytoscape");
            for (int i = 0; i < nodes.size(); i++) {
                PNodeView pnode = (PNodeView) nodes.get(i);
                Node node = pnode.getNode();
                if (node instanceof CyNode) {
                    //int serial = ((Integer) Cytoscape.getCurrentNetworkView().getNetwork().getNodeAttributeValue((CyNode) node, "Serial")).intValue();
                    //selectedMarkers.add(maSet.getMarkers().get(serial));
                }
            }
            selectedMarkers.setActive(true);
            publishSubpanelChangedEvent(new org.geworkbench.events.SubpanelChangedEvent(DSGeneMarker.class, selectedMarkers, org.geworkbench.events.SubpanelChangedEvent.SET_CONTENTS));
        }
    }

    @Publish
    public org.geworkbench.events.SubpanelChangedEvent publishSubpanelChangedEvent(org.geworkbench.events.SubpanelChangedEvent event) {
        return event;
    }

    /**
     * receiveProjectSelection
     *
     * @param e ProjectEvent
     */
    @Subscribe public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
        DSDataSet dataSet = e.getDataSet();
        if (dataSet instanceof AdjacencyMatrixDataSet) {
            adjSet = (AdjacencyMatrixDataSet) dataSet;
            maSet = adjSet.getMatrix().getMicroarraySet();
            boolean found = false;
            String foundID = null;
            if (!dataSetIDs.contains(adjSet.getID())) {
                dataSetIDs.add(adjSet.getID());
            } else {
                Set networks = Cytoscape.getNetworkSet();
                for (Iterator iterator = networks.iterator(); iterator.hasNext();) {
                    String id = (String) iterator.next();
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
        if (tmpname != null) {
            name = tmpname + " [" + name + "]";
        }
        Set networks = Cytoscape.getNetworkSet();
        HashSet<String> names = new HashSet<String>();
        for (Iterator iterator = networks.iterator(); iterator.hasNext();) {
            //Object obj = iterator.next();
            //System.out.println("heere erererer: "+obj);
            //String id = (String) obj;
            //String id = (String) iterator.next();
            //CyNetwork network = Cytoscape.getNetwork(id);
            FingCyNetwork network = (FingCyNetwork)iterator.next();
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
            /*view.addGraphViewChangeListener(new GraphViewChangeListener() {
                public void graphViewChanged(GraphViewChangeEvent graphViewChangeEvent) {
                    cyNetWorkView_graphViewChanged(graphViewChangeEvent);
                }
            });*/
        }
    }

    /**
     * phenotypeSelectorAction
     *
     * @param e PhenotypeSelectorEvent
     */
    @Subscribe public void receive(org.geworkbench.events.PhenotypeSelectorEvent e, Object source) {
    }

    @Subscribe public void receive(AdjacencyMatrixEvent ae, Object source) {
        switch (ae.getAction()) {
            case LOADED: {
                // no-op
            }
            break;
            case RECEIVE: {
                // System.out.println("receiveAdjacencyMatrix from cytoscapeWidget");
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

                // this.adjStorage.add(ae.getAdjacencyMatrix());
                System.out.println(" adjSize  " + ae.getAdjacencyMatrix().getEdgeNo(5));
                // System.out.println("Current adj storage: "+ myio.vector2tring(this.adjStorage, "\n"));
            }
            break;
            case DRAW_NETWORK: {
                // System.out.println("drawNetwork from cytoscapeWidget");
                if (ae.getNetworkFocus() == -1) {
                    drawCompleteNetwork(ae.getAdjacencyMatrix(), ae.getThreshold());
                } else {
                    drawCentricNetwork(ae.getAdjacencyMatrix(), ae.getNetworkFocus(), ae.getThreshold(), ae.getDisplayDepth());
                }
                //CyWindowProxy proxy = new CyWindowProxy();
                YFilesLayoutPlugin plugin = new YFilesLayoutPlugin();
                OrganicLayout organiclayout = new OrganicLayout(plugin);
                organiclayout.actionPerformed(null);
                Cytoscape.getCurrentNetworkView().applyVizmapper(interactionsVisualStyle);
                Cytoscape.getCurrentNetworkView().fitContent();
            }
            break;
            case DRAW_GENEWAYS_COMPLETE_NETWORK: {
                // System.out.println("drawNetwork from cytoscapeWidget");
                this.genewaysGeneidNameMap = ae.getGenewaysGeneidNameMap();
                if (ae.getNetworkFocus() == -1) {
                    drawCompleteNetwork(ae.getAdjacencyMatrix(), ae.getThreshold());
                } else {
                    drawCentricNetwork(ae.getAdjacencyMatrix(), ae.getNetworkFocus(), ae.getThreshold(), ae.getDisplayDepth());
                }
                //CyWindowProxy proxy = new CyWindowProxy();
                YFilesLayoutPlugin plugin = new YFilesLayoutPlugin();
                OrganicLayout organiclayout = new OrganicLayout(plugin);
                organiclayout.actionPerformed(null);
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

    public void setAdjacencyMatrix(IAdjacencyMatrix adjMat) {
        String name = ((maSet = adjMat.getMicroarraySet()) == null ? "Test" : maSet.getLabel());
        cytoNetwork = Cytoscape.createNetwork(name);
        this.adjStorage.add(adjMat);
    }

    @Script
    public DSPanel getFirstNeighbors(int geneid, double threshold, int level) {
        createSubNetwork(geneid, threshold, level);
        int[] indices = cytoNetwork.neighborsArray(geneid);
        DSPanel selectedMarkers = new CSPanel("First Neighbors", cytoNetwork.getNode(geneid).getIdentifier());
        for (int i = 0; i < indices.length; i++) {
            Node node = cytoNetwork.getNode(indices[i]);
            if (node instanceof CyNode) {
                //int serial = ((Integer) cytoNetwork.getNodeAttributeValue((CyNode) node, "Serial")).intValue();
                //selectedMarkers.add(maSet.getMarkers().get(serial));
            }
            selectedMarkers.setActive(true);
        }
        return selectedMarkers;
    }

    @Script
    public void computeAndDrawFirstNeighbors(DSGeneMarker m, AdjacencyMatrix am) {
        adjMatrix = am;
        maSet = am.getMicroarraySet();
        cytoNetwork = Cytoscape.createNetwork(maSet.getLabel() + m.getSerial());
        createSubNetwork(m.getSerial(), 0, 1);
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
        createSubNetwork(p.get(i).getSerial(), 0, 1);
        view = Cytoscape.createNetworkView(cytoNetwork, maSet.getLabel());
        view.addGraphViewChangeListener(new GraphViewChangeListener() {
            public void graphViewChanged(GraphViewChangeEvent graphViewChangeEvent) {
                cyNetWorkView_graphViewChanged(graphViewChangeEvent);
            }
        });
    }


    /*public class CyWindowProxy implements CyWindow{
        public CyWindowProxy() {}
        public CyNetworkView getView() {return Cytoscape.getCurrentNetworkView();}
        public void showWindow(int i, int i0) {}
        public void showWindow() {}
        public CytoscapeObj getCytoscapeObj() {return Cytoscape.getCytoscapeObj();}
        public CyNetwork getNetwork() {return Cytoscape.getCurrentNetwork();}
        public GraphViewController getGraphViewController() {return Cytoscape.getDesktop().getGraphViewController();}
        public VisualMappingManager getVizMapManager() {return Cytoscape.getCurrentNetworkView().getVizMapManager();}
        public VizMapUI getVizMapUI() {return Cytoscape.getCurrentNetworkView().getVizMapUI();}
        public JFrame getMainFrame() {return Cytoscape.getDesktop().getMainFrame();}
        public String getWindowTitle() {return "";}
        public void setWindowTitle(String string) {}
        public CyMenus getCyMenus() {return Cytoscape.getDesktop().getCyMenus();}
        public void setNewNetwork(CyNetwork cyNetwork) {}
        public void setInteractivity(boolean b) {}
        public void redrawGraph() {}
        public void redrawGraph(boolean b) {}
        public void redrawGraph(boolean b, boolean b0) {}
        public void applyLayout(GraphView graphView) {}
        public void applySelLayout() {}
        public void setVisualMapperEnabled(boolean b) {}
        public void toggleVisualMapperEnabled() {}
        public void switchToReadOnlyMode() {}
        public void switchToEditMode() {}
     }*/
}