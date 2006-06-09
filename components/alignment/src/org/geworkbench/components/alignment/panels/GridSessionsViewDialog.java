package org.geworkbench.components.alignment.panels;

import org.geworkbench.components.alignment.grid.GridSessionViewController;
import org.globus.ogsa.gui.ServiceContext;
import org.globus.ogsa.gui.XMLTree;
import org.globus.ogsa.types.properties.PropertiesDetailType;
import org.globus.ogsa.utils.MessageUtils;
import org.globus.ogsa.wsdl.GSR;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Collection;


/**
 * <p>Title: Bioworks</p>
 *
 * <p>Description: Modular Application Framework for Gene Expession, Sequence
 * and Genotype Analysis</p>
 *
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 *
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class GridSessionsViewDialog
    extends JDialog
    implements TreeSelectionListener {
    JTable sessionTable;
    JTree tree;
    BlastListTableModel model;
    GridSessionViewController gsc = new GridSessionViewController(null);
    //ServiceDataDescriptionListTableModel sdeModel;
    SimpleServiceDataListTableModel sdeModel;
    private URL handle;
    private GSR gsr = null;
    private ServiceContext context;
    private Collection portTypes;
    public static boolean DEBUG = true;

    private XMLTree wsdlTree;
    JTable table;
    JPanel detailPanel ;

    public GridSessionsViewDialog() throws HeadlessException {
        super();
        try {
            jbInit();
            pack();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public GridSessionsViewDialog(Frame owner) throws HeadlessException {
        super(owner);
    }

    public GridSessionsViewDialog(Frame owner, boolean modal) throws
        HeadlessException {
        super(owner, modal);
    }

    public GridSessionsViewDialog(Frame owner, String title) throws
        HeadlessException {
        super(owner, title);
    }

    public GridSessionsViewDialog(Frame owner, String title, boolean modal) throws
        HeadlessException {
        super(owner, title, modal);
    }

    public GridSessionsViewDialog(Frame owner, String title, boolean modal,
                                  GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
    }

    public GridSessionsViewDialog(Dialog owner) throws HeadlessException {
        super(owner);
    }

    public GridSessionsViewDialog(Dialog owner, boolean modal) throws
        HeadlessException {
        super(owner, modal);
    }

    public GridSessionsViewDialog(Dialog owner, String title) throws
        HeadlessException {
        super(owner, title);
    }

    public GridSessionsViewDialog(Dialog owner, String title, boolean modal) throws
        HeadlessException {
        super(owner, title, modal);
    }

    public GridSessionsViewDialog(Dialog owner, String title, boolean modal,
                                  GraphicsConfiguration gc) throws
        HeadlessException {
        super(owner, title, modal, gc);
    }

    /** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
            tree.getLastSelectedPathComponent();

        if (node == null) {
            return;
        }

        Object nodeInfo = node.getUserObject();
        if (node.isLeaf()) {
            // BookInfo book = (BookInfo)nodeInfo;
            // displayURL(book.bookURL);
            //model.setPrimarySelectionCrit(nodeInfo.toString());
            model.getRegistry();
            if (DEBUG) {
                System.out.print(nodeInfo + ":  \n    ");
            }
        }
        else {
            //displayURL(helpURL);
        }
        if (DEBUG) {
            System.out.println(nodeInfo.toString());
        }
    }

    private void createNodes(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode category = null;
        DefaultMutableTreeNode book = null;

        category = new DefaultMutableTreeNode("SequenceRelated");
        top.add(category);

        book = new DefaultMutableTreeNode("BLAST");
        category.add(book);

        // SPALSH
        book = new DefaultMutableTreeNode("SPLASH");
        category.add(book);

        book = new DefaultMutableTreeNode(
            ("ARACNE"));
        category.add(book);

    }

    private void jbInit() throws Exception {
        detailPanel = new JPanel();
        this.getContentPane().setLayout(borderLayout6);
        this.getContentPane().setBackground(SystemColor.control);
        this.setTitle("Grid Sessions View");
        viewButton.setToolTipText("View all available sessions");
        viewButton.setText("View All");
        viewButton.addActionListener(new
                                     GridSessionsViewDialog_jButton4_actionAdapter(this));
        deleteButton.setToolTipText("Delete selected session");
        deleteButton.setText("Delete");
        //Create the nodes.
        String name = System.getProperty("user.name");
        DefaultMutableTreeNode top =
            new DefaultMutableTreeNode(name);
        createNodes(top);

        //Create a tree that allows one selection at a time.
        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        tree.addTreeSelectionListener(this);

        //Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane(tree);

        sessionTable = new JTable();

        // sessionTable.setModel(model);
        String[] selectionCrit = {
            "Instance", "sequenceAlignment"};
        // sessionTable.setM
        model = new BlastListTableModel(selectionCrit);
        sessionTable.setModel(model);
        sessionTable.addMouseListener(new
                                      GridSessionsViewDialog_sessionTable_mouseAdapter(this));
        JScrollPane hitsPane = new JScrollPane(sessionTable);

        // table.setModel(this.model);

        //sessionTable.setBorder(border4);
        jPanel1.setBorder(border6);
        jPanel1.setDebugGraphicsOptions(0);
        jPanel1.setMinimumSize(new Dimension(30, 60));
        jPanel1.setPreferredSize(new Dimension(51, 60));
        jPanel1.setLayout(flowLayout4);
        selectButton.setToolTipText("Pick your session");
        selectButton.addActionListener(new
                                       GridSessionsViewDialog_selectButton_actionAdapter(this));
        //jPanel3.setBounds(new Rectangle(10, 10, 316, 98));
        controlPanel.setBorder(border5);
        controlPanel.setLayout(flowLayout2);
        sessionPanel.setBorder(border4);
        sessionPanel.setPreferredSize(new Dimension(230, 110));
        sessionPanel.setLayout(borderLayout1);
        hitsPane.setPreferredSize(new Dimension(225, 100));
        jPanel6.setLayout(borderLayout4);
        jPanel6.setBounds(new Rectangle(2, 38, 311, 29));
        jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setBorder(titledBorder2);
        detailPanel.setPreferredSize(new Dimension(230, 100));
        jSplitPane2.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        jSplitPane2.setRightComponent(jSplitPane1);
        jSplitPane2.setTopComponent(controlPanel);
        jSplitPane3.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        jSplitPane3.setRightComponent(jPanel1);
        closeButton.setToolTipText("Pick your session");
        closeButton.setText("Close");
        closeButton.addActionListener(new
                                      GridSessionsViewDialog_closeButton_actionAdapter(this));
        jSplitPane3.add(jSplitPane2, JSplitPane.TOP);
        jSplitPane3.setDividerSize(5);
        jSplitPane2.setDividerSize(5);
        jSplitPane1.setDividerSize(5);
        jSplitPane2.add(jSplitPane1, JSplitPane.RIGHT);
        selectButton.setText("Select");

        //sdeModel = new ServiceDataDescriptionListTableModel();
        sdeModel = new SimpleServiceDataListTableModel();
        table = new JTable();
        table.setModel(sdeModel);
        JScrollPane detailPane = new JScrollPane(table);

        detailPanel.setBorder(border2);
        detailPanel.setLayout(borderLayout5);
        jSplitPane1.add(sessionPanel, JSplitPane.TOP);
        sessionPanel.add(hitsPane, BorderLayout.CENTER);
        jSplitPane1.add(detailPanel, JSplitPane.BOTTOM);
        detailPanel.add(detailPane, java.awt.BorderLayout.CENTER);
        detailPanel.add(jPanel1, BorderLayout.SOUTH);
        jSplitPane2.add(controlPanel, JSplitPane.LEFT);
        detailPane.getViewport().add(table);
        hitsPane.getViewport().add(sessionTable, null);
        controlPanel.add(jPanel6, null);
        jPanel6.add(treeView, BorderLayout.CENTER);
        this.getContentPane().add(jSplitPane3, BorderLayout.CENTER);
        jPanel1.add(viewButton, null);
        jPanel1.add(selectButton, null);
        jPanel1.add(deleteButton, null);
        jPanel1.add(closeButton, null);
    jSplitPane1.setDividerLocation(180);
        //JScrollPane treeView1 = new JScrollPane(tree);
    }

    JPanel jPanel1 = new JPanel();

    // sessionTable = new JTable();

    // sessionTable.setModel(model);
    //String[] selectionCrit = {
    //     "Math", "blast", "Blast", "BLAST"};
    // sessionTable.setM
    //AbstractTableModel model  = new BlastListTableModel(selectionCrit);
    //sessionTable.setModel(model);

    // table.setModel(this.model);

    JButton selectButton = new JButton();
    JButton deleteButton = new JButton();
    JButton viewButton = new JButton();
    TitledBorder titledBorder1 = new TitledBorder("");
    Border border1 = BorderFactory.createEtchedBorder(Color.white,
        new Color(165, 163, 151));
    Border border2 = new TitledBorder(border1, "Service Details");
    Border border3 = BorderFactory.createEtchedBorder(Color.white,
        new Color(178, 178, 178));
    Border border4 = new TitledBorder(border3, "Session Table");
    Border border5 = BorderFactory.createEtchedBorder(EtchedBorder.RAISED,
        Color.white, new Color(165, 163, 151));
    Border border6 = new TitledBorder(border5, "Your Action");
    FlowLayout flowLayout1 = new FlowLayout();
    GridLayout gridLayout1 = new GridLayout();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    JPanel controlPanel = new JPanel();
    JPanel sessionPanel = new JPanel(); //JTextField jTextField1 = new JTextField();
    JPanel jPanel6 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    BorderLayout borderLayout4 = new BorderLayout();
    FlowLayout flowLayout2 = new FlowLayout();
    JSplitPane jSplitPane1 = new JSplitPane();
    TitledBorder titledBorder2 = new TitledBorder("");
    FlowLayout flowLayout3 = new FlowLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    BorderLayout borderLayout5 = new BorderLayout();
    JSplitPane jSplitPane2 = new JSplitPane();
    JSplitPane jSplitPane3 = new JSplitPane();
    BorderLayout borderLayout6 = new BorderLayout();
    JButton closeButton = new JButton();
    FlowLayout flowLayout4 = new FlowLayout();
    public void jButton4_actionPerformed(ActionEvent e) {
        model.getRegistry();
    }

    public void getDefaultService(String[] crit) {
        model.getRegistry();
    }

    public void sessionTable_mouseClicked(MouseEvent e) {
        //int index = sessionTable.locationToIndex(e.getPoint());
        int n = 0;
        int row = sessionTable.rowAtPoint(e.getPoint());
        int col = sessionTable.columnAtPoint(e.getPoint());
        PropertiesDetailType entry = (PropertiesDetailType) model.getList().
            get(row);
        if (e.getClickCount() < 2) {
            Object[] options = {
                "Yes",
                "No, thanks",
                "Cancel"};
            /* n = JOptionPane.showOptionDialog(this,
                                              "Would you like use the selected session to handle all connctions to the server? ",
                                              "Confirm the session",
                                              JOptionPane.
                                              YES_NO_CANCEL_OPTION,
                                              JOptionPane.QUESTION_MESSAGE,
                                              null,
                                              options,
                                              options[2]);

             // etc....*/
            System.out.println("1 click");
        }

        if (n == 0) {

            try {
                String newHandler = entry.getHandle().replaceAll("127.0.0.1",
                    "adgate.cu-genome.org");
                sdeModel.setGsr(newHandler);

            }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this, "Failed to obtain WSDL: " +
                    MessageUtils.getErrorMessage(ex), "Error",
                    JOptionPane.ERROR_MESSAGE
                    );
                ex.printStackTrace();

                return;
            }

        }
        if (e.getClickCount() == 2) {

            Object selectedItem = model.getList().get(row);

            String name = entry.getName();
            Object[] options = {
                "Yes, please",
                "No, thanks",
                name
            };
            n = JOptionPane.showOptionDialog(this,
                                             "Would you like use the selected session to handle all connctions to the server? ",
                                             "Confirm the session",
                                             JOptionPane.
                                             YES_NO_CANCEL_OPTION,
                                             JOptionPane.QUESTION_MESSAGE,
                                             null,
                                             options,
                                             options[2]);

        }

        if (n == 0) {
            System.out.println("CONFIRMED");

            try {
                String newHandler = entry.getHandle().replaceAll("127.0.0.1",
                    "adgate.cu-genome.org");
//                gsc.generateEvent(SessionConnectListener.class,
//                                  "sessionConnectAction",
//                                  new SessionConnectEvent(gsc, newHandler));

                sdeModel.setGsr(newHandler);
                //newHandler = "http://156.145.235.50:8080/ogsa/services/progtutorial/core/second/BlastFactoryService/hash-19161420-1112889410515";
                /* this.gsr = GSR.newInstance(new HandleType(newHandler));
                 Document doc = this.gsr.getDocument();
                 wsdlTree = new XMLTree(XmlFactory.newDocument());
                 wsdlTree.setModel(new XMLTreeModel(this.gsr.getDocument()));
                 this.portTypes = this.gsr.getGWSDLPortTypes();

                 //Pasre the portTypes.
                 for (Iterator iterator = this.portTypes.iterator();
                      iterator.hasNext(); ) {
                     QName name = null;
                     JComponent portTypePanel = null;

                     PortTypeExtensibilityElement element =
                         (PortTypeExtensibilityElement) iterator.next();
                     ServiceDataType[] serviceData = element.getServiceData();
                     String s = element.getName().toString();
                     System.out.println("element s: " + s);

                    // this.sdeModel= new ServiceDataDescriptionListTableModel();
                     this.sdeModel.addList(serviceData, element.getName());
                 }
                 */
            }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this, "Failed to obtain WSDL: " +
                    MessageUtils.getErrorMessage(ex), "Error",
                    JOptionPane.ERROR_MESSAGE
                    );
                ex.printStackTrace();

                return;
            }

        }
    }

    public void closeButton_actionPerformed(ActionEvent e) {
        this.setVisible(false);
    }

    public void selectButton_actionPerformed(ActionEvent e) {

    }

}

class GridSessionsViewDialog_selectButton_actionAdapter
    implements ActionListener {
    private GridSessionsViewDialog adaptee;
    GridSessionsViewDialog_selectButton_actionAdapter(GridSessionsViewDialog
        adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.selectButton_actionPerformed(e);
    }
}

class GridSessionsViewDialog_closeButton_actionAdapter
    implements ActionListener {
    private GridSessionsViewDialog adaptee;
    GridSessionsViewDialog_closeButton_actionAdapter(GridSessionsViewDialog
        adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.closeButton_actionPerformed(e);
    }
}

class GridSessionsViewDialog_sessionTable_mouseAdapter
    extends MouseAdapter {
    private GridSessionsViewDialog adaptee;
    GridSessionsViewDialog_sessionTable_mouseAdapter(GridSessionsViewDialog
        adaptee) {
        this.adaptee = adaptee;
    }

    public void mouseClicked(MouseEvent e) {

        adaptee.sessionTable_mouseClicked(e);
    }
}

class GridSessionsViewDialog_jButton4_actionAdapter
    implements ActionListener {
    private GridSessionsViewDialog adaptee;
    GridSessionsViewDialog_jButton4_actionAdapter(GridSessionsViewDialog
                                                  adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButton4_actionPerformed(e);
    }
}
