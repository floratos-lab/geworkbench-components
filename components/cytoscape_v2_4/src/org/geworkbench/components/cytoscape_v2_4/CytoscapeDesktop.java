package org.geworkbench.components.cytoscape_v2_4;

//import cytoscape.CytoscapeObj;


/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author manjunath at genomecenter dot columbia dot edu
 * @version 1.0
 */

public class CytoscapeDesktop extends cytoscape.view.CytoscapeDesktop {

    public CytoscapeDesktop() {
        this(TABBED_VIEW);
    }

    public CytoscapeDesktop(int view_type) {
        this.VIEW_TYPE = view_type;
        this.initialize();
    }

    /*protected void initialize() {
        JPanel main_panel = new JPanel();

        cyHelpBroker = new CyHelpBroker();

        main_panel.setLayout(new BorderLayout());

        networkPanel = new NetworkPanel(this);
        cyMenus = new CyMenus();
        networkViewManager = new NetworkViewManager(this);

        Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(this);

        this.getSwingPropertyChangeSupport().addPropertyChangeListener(networkViewManager);
        networkViewManager.getSwingPropertyChangeSupport().addPropertyChangeListener(this);

        this.getSwingPropertyChangeSupport().addPropertyChangeListener(networkPanel);
        networkPanel.getSwingPropertyChangeSupport().addPropertyChangeListener(this);

        // add a listener for node bypass
        cytoscape.Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(new VizMapBypassNetworkListener());

        // initialize undo manager
        undo = new cytoscape.util.UndoManager(cyMenus);

        cyMenus.initializeMenus();

        // initialize Help Menu
        //kkumar:  have to check how it can be done.
        //cyMenus.initializeHelp(cyHelpBroker.getHelpBroker());

        // create the CytoscapeDesktop
        BiModalJSplitPane masterPane = setupCytoPanels(networkPanel,
                networkViewManager);

        if (VIEW_TYPE == TABBED_VIEW) {
            JScrollPane scroll_tab = new JScrollPane(networkViewManager.getTabbedPane());

            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, networkPanel, scroll_tab);
            split.setOneTouchExpandable(true);

            main_panel.add(split, BorderLayout.CENTER);
            main_panel.add(cyMenus.getToolBar(), BorderLayout.NORTH);
            if (!System.getProperty("os.name").startsWith("Mac")) {
                JFrame menuFrame = new JFrame("Cytoscape Menus");
                menuFrame.setJMenuBar(cyMenus.getMenuBar());
                menuFrame.setSize(400, 60);
            } else {
                setJMenuBar(cyMenus.getMenuBar());
            }
        } else if (VIEW_TYPE == INTERNAL_VIEW) {
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, networkPanel, networkViewManager.getDesktopPane());
            main_panel.add(split, BorderLayout.CENTER);
            main_panel.add(cyMenus.getToolBar(), BorderLayout.NORTH);
            setJMenuBar(cyMenus.getMenuBar());
        } else if (VIEW_TYPE == EXTERNAL_VIEW) {
            main_panel.add(networkPanel);
            cyMenus.getToolBar().setOrientation(JToolBar.VERTICAL);
            main_panel.add(cyMenus.getToolBar(), BorderLayout.EAST);

            if (!System.getProperty("os.name").startsWith("Mac")) {
                JFrame menuFrame = new JFrame("Cytoscape Menus");
                menuFrame.setJMenuBar(cyMenus.getMenuBar());
                menuFrame.setSize(400, 60);
            } else {
                setJMenuBar(cyMenus.getMenuBar());
            }
        }
        setupVizMapper(main_panel);

//        Cytoscape.getCytoscapeObj().getPluginRegistry().addPluginListener(this);

        final CytoscapeObj theCytoscapeObj = Cytoscape.getCytoscapeObj();
        final CytoscapeDesktop thisWindow = this;

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                theCytoscapeObj.saveCalculatorCatalog();
            }

            public void windowClosed() {
            }
        });

            addWindowListener(Cytoscape.getCytoscapeObj().getParentApp());

        setContentPane(main_panel);
        pack();
        if (VIEW_TYPE != EXTERNAL_VIEW)
            setSize(700, 700);
    }  */
}
