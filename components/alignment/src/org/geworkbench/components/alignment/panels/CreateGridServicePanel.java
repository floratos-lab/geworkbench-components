package org.geworkbench.components.alignment.panels;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */


import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.components.alignment.client.BlastAlgorithm;
import org.geworkbench.components.alignment.grid.ServiceDataModel;
import org.geworkbench.components.alignment.grid.service.SystemInformation;
import org.globus.ogsa.NotificationSinkCallback;
import org.globus.ogsa.ServiceData;
import org.globus.ogsa.client.managers.NotificationSinkManager;
import org.globus.ogsa.gui.XMLTree;
import org.globus.ogsa.gui.XMLTreeModel;
import org.globus.ogsa.utils.*;
import org.globus.ogsa.wsdl.GSR;
//import org.globus.progtutorial.clients.BlastService.Client;
import org.gridforum.ogsi.*;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.rpc.Stub;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Random;
import com.borland.jbcl.layout.VerticalFlowLayout;
import org.globus.progtutorial.clients.BlastService.Client;

public class CreateGridServicePanel
    extends JPanel
    implements TreeSelectionListener, NotificationSinkCallback,
    InstanceCreator {
    private JTree tree;
    private BlastListTableModel model;
    URL defaultEndpoint;
    private Timer timer;
    private XMLTree entryTree;
    private Hashtable properties = new Hashtable();
    private NotificationSinkManager manager;
    private String sink;
    private BlastAppComponent pv;
    private ButtonGroup bg = new ButtonGroup();
    private int sequenceNum = 0;
    private String[] expirationDays = {
        "Forever", "1 day", "2 days", "7 days", "30 days"};
    private URL handle;
    JTable table;
    JTabbedPane tbPane;
    JScrollPane p1;
    JScrollPane p2;
    ServiceDataModel sdModel;
    JPanel blastData;
    //ServiceDataViewPanel sdvPanel;
    private String newSessionName;

    private String DEFAULTURL = "http://gridgate.genomecenter.columbia.edu:18080/ogsa/services/core/registry/ContainerRegistryService";

    //private String DEFAULTFACTORYURL = "http://adgate.cu-genome.org:8080/ogsa/services/progtutorial/core/second/BlastFactoryService";
    private String DEFAULTFACTORYURL = "http://gridgate.genomecenter.columbia.edu:18080/ogsa/services/edu/columbia/session/SequenceAlignmentFactoryService";

    JPanel controlPanel = new JPanel();
    JSplitPane jSplitPane2 = new JSplitPane();
    //JButton update
    JPanel sessionPanel = new JPanel();
    BorderLayout borderLayout4 = new BorderLayout();
    JPanel jPanel6 = new JPanel();
    JSplitPane jSplitPane1 = new JSplitPane();
    JButton viewButton = new JButton();
    JCheckBox autoUpdateCB;
    //JScrollPane treeView;
    JPanel detailPanel = new JPanel();
    JPanel jPanel1 = new JPanel();
    JPanel jPanel2 = new JPanel();
    JRadioButton jRadioButton1 = new JRadioButton();
    JRadioButton jRadioButton2 = new JRadioButton();
    JRadioButton jRadioButton3 = new JRadioButton();
    JLabel jobnameLabel = new JLabel();
    JTextField jobnametField = new JTextField();
    JComboBox jobExpirationBox1 = new JComboBox(expirationDays);
    TitledBorder titledBorder1;
    TitledBorder titledBorder2;
    TitledBorder titledBorder3;
    TitledBorder titledBorder4;
    BorderLayout borderLayout1 = new BorderLayout();
    JLabel jobExpirationLabel = new JLabel();
    JLabel jLabel3 = new JLabel();
    JTextField cpuNumText = new JTextField();
    JLabel cpuNumberLabel = new JLabel();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
    JTextField userNameText = new JTextField();
    FlowLayout flowLayout1 = new FlowLayout();
    JButton checkButton = new JButton();
    private VerticalFlowLayout verticalFlowLayout1 = new VerticalFlowLayout();
    private BorderLayout borderLayout2 = new BorderLayout();

    //private String DEFAULTURL = "http://localhost:8080/ogsa/services/core/registry/ContainerRegistryService";

    public CreateGridServicePanel() {
        //super("Blast Grid Service Information");

        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
            tree.getLastSelectedPathComponent();

        if (node == null) {
            return;
        }

        Object nodeInfo = node.getUserObject();
        boolean DEBUG = false;
        if (node.toString().indexOf("MPI") > -1) {
            // BookInfo book = (BookInfo)nodeInfo;
            // displayURL(book.bookURL);
            //model.setPrimarySelectionCrit(nodeInfo.toString());
            //model.getRegistry();
            if (DEBUG) {
                System.out.print(nodeInfo + ":  \n    ");
            }
            cpuNumText.setVisible(true);
            cpuNumberLabel.setVisible(true);
            //revalidate();
        }
        else {
            cpuNumText.setVisible(false);
            cpuNumberLabel.setVisible(false);
        }
        if (DEBUG) {
            System.out.println(nodeInfo.toString());
        }
    }

    protected void getRegistry() {

        try {

            defaultEndpoint = new URL(DEFAULTURL);

            String entryDoc = null;
            OGSIServiceGridLocator registryService = new OGSIServiceGridLocator();

            GridService registry =
                registryService.getGridServicePort(this.defaultEndpoint);
            GSIUtils.setDefaultGSIProperties(
                (Stub) registry, this.defaultEndpoint
                );
            ExtensibilityType queryResult =
                registry.findServiceData(
                QueryHelper.getNamesQuery(ServiceData.ENTRY)
                );
            if (
                (queryResult.get_any() != null) &&
                (queryResult.get_any().length > 0)
                ) {
                Element element = AnyHelper.getAsSingleElement(queryResult);
                //System.out.println(queryResult);
                //System.out.println("is " + queryResult.getTypeDesc());
                this.entryTree.setModel(new XMLTreeModel(element));

                ServiceDataValuesType serviceDataValues =
                    (ServiceDataValuesType) AnyHelper.getAsSingleObject(
                    queryResult, ServiceDataValuesType.class
                    );

                if (serviceDataValues.get_any() != null) {
                    this.model.setList(
                        AnyHelper.getAsObject(
                        serviceDataValues, EntryType.class
                        )
                        );
                }
            }
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(
                this, "Failed to get registry: " + e.getMessage(),
                "Registry: getRegistry error", JOptionPane.ERROR_MESSAGE
                );
            e.printStackTrace();

            return;
        }
    }

//    public void init() {
//        getRegistry();
//
//        // for notification: sets authorization type
//        setProperty(Constants.AUTHORIZATION, SelfAuthorization.getInstance());
//
//        // sets gsi mode
//        setProperty(GSIConstants.GSI_MODE, GSIConstants.GSI_MODE_NO_DELEG);
//
//        addRegistryListener();
//    }

    public void dispose() {
        if (this.timer.isRunning()) {
            this.timer.stop();
        }

        try {
            if (this.manager.isListening()) {
                this.manager.removeListener(this.sink);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addRegistryListener() {
        try {
            // TODO: the right way of doing this is to check weather the defaultEndpoint service
            // supports the Source Service and exposes a Entry topic
            this.manager = NotificationSinkManager.getManager();
            this.manager.startListening(NotificationSinkManager.CHILD_THREAD);
            this.sink =
                this.manager.addListener(
                ServiceData.ENTRY, null,
                new HandleType(this.handle.toString()), this
                );
        }
        catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public Object getProperty(String name) {
        return this.properties.get(name);
    }

    public Object getPersistentProperty(String name) {
        return null;
    }

    public void setProperty(
        String name,
        Object property
        ) {
        this.properties.put(name, property);
    }

    public void setPersistentProperty(
        String name,
        Object property
        ) {
    }

    public void flush() {
    }

    public void deliverNotification(ExtensibilityType message) throws
        RemoteException {
        try {
            Element element = AnyHelper.getAsSingleElement(message);
            this.entryTree.setModel(new XMLTreeModel(element));

            ServiceDataValuesType serviceDataValues =
                (ServiceDataValuesType) AnyHelper.getAsSingleObject(
                message, ServiceDataValuesType.class
                );
            this.model.setList(
                AnyHelper.getAsObject(serviceDataValues, EntryType.class)
                );
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNodes(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode category = new DefaultMutableTreeNode("MPIBLAST"); ;
        DefaultMutableTreeNode book = new DefaultMutableTreeNode("BLAST");
        top.add(book);
        book.add(category);
        category = new DefaultMutableTreeNode("ParacelBLAST");
        book.add(category);

    }

    private void jbInit() throws Exception {
        sdModel = new ServiceDataModel();

        DefaultMutableTreeNode top =
            new DefaultMutableTreeNode("SequenceAlignment");
        createNodes(top);

        //Create a tree that allows one selection at a time.
        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        tree.addTreeSelectionListener(this);

        //Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane(tree);

        //sdvPanel = new ServiceDataViewPanel(sdModel);

        titledBorder1 = new TitledBorder("");
        titledBorder2 = new TitledBorder(BorderFactory.createEtchedBorder(Color.
            white, new Color(165, 163, 151)), "Job Size");
        titledBorder3 = new TitledBorder(BorderFactory.createEtchedBorder(Color.
            white, new Color(165, 163, 151)), "Job Details");
        titledBorder4 = new TitledBorder("");
        /*  autoUpdateCB.setMinimumSize(new Dimension(85, 23));
              autoUpdateCB.setToolTipText("Auto update");
              autoUpdateCB.setSelected(false);
         */
        this.timer =
            new Timer(
            5000,
            new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getRegistry();
            }
        }
        );
        /*
                autoUpdateCB.addActionListener(
                    new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (timer.isRunning()) {
                            timer.stop();
                        }
                        else {
                            getRegistry();
                            timer.start();
                        }
                    }
                }
                );

         */
        String[] selectionCrit = {
            "blast", "Blast", "BLAST"};

        this.model = new BlastListTableModel(selectionCrit);

        // final JTable table = new JTable();
        table = new JTable();
        table.setModel(this.model);

        setLayout(new BorderLayout());

        try {
            this.entryTree = new XMLTree(XmlFactory.newDocument());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        tbPane = new JTabbedPane();
        blastData = new JPanel();
        blastData.setLayout(new BorderLayout());
        // blastData.add(sdvPanel, BorderLayout.SOUTH);
        p1 = new JScrollPane(blastData);
        //p1 = new JScrollPane(table);
        p1.setPreferredSize(new Dimension(200, 150));

        p2 = new JScrollPane(this.entryTree);
        p2.setPreferredSize(new Dimension(200, 150));

        controlPanel.setLayout(borderLayout1);
        jSplitPane2.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        jSplitPane2.setRightComponent(jSplitPane1);
        jSplitPane2.setTopComponent(controlPanel);
        jSplitPane2.setDividerSize(5);
        jSplitPane2.setLastDividerLocation(150);
        sessionPanel.setMinimumSize(new Dimension(300, 190));
        sessionPanel.setPreferredSize(new Dimension(230, 190));
        sessionPanel.setToolTipText("");
        sessionPanel.setLayout(borderLayout2);
        jPanel6.setLayout(borderLayout4);
        jPanel6.setBounds(new Rectangle(2, 38, 311, 29));
        jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setMinimumSize(new Dimension(280, 254));
        jSplitPane1.setPreferredSize(new Dimension(200, 217));
        jSplitPane1.setDividerSize(5);
        viewButton.setToolTipText("Create a new job.");
        viewButton.setText("Submit");
        viewButton.addActionListener(new
                                     CreateGridServicePanel_viewButton_actionAdapter(this));
        //viewButton.addActionListener(new
        //                               GridSessionsViewDialog_jButton4_actionAdapter(this));
        detailPanel.setPreferredSize(new Dimension(230, 30));
        detailPanel.setLayout(flowLayout1);
        jPanel1.setBorder(titledBorder2);
        jPanel1.setDebugGraphicsOptions(0);
        jPanel1.setMinimumSize(new Dimension(77, 130));
        jPanel1.setVerifyInputWhenFocusTarget(true);
        jPanel1.setLayout(verticalFlowLayout1);
        jPanel2.setBorder(titledBorder3);
        jPanel2.setMinimumSize(new Dimension(223, 130));
        jPanel2.setLayout(gridBagLayout2);
        jRadioButton1.setText("large");
        jRadioButton2.setText("middle");
        jRadioButton3.setText("small");
        checkButton.setText("CheckStatus");
        checkButton.addActionListener(new
                CreateGridServicePanel_checkButton_actionAdapter(this));
        bg.add(jRadioButton1);
        bg.add(jRadioButton2);
        bg.add(jRadioButton3);
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jRadioButton3_actionPerformed(e);
            }
        });
        jobnameLabel.setText("Your new job name: ");
        jobnametField.setText("BlastTestA");
        treeView.setPreferredSize(new Dimension(45, 150));
        jobExpirationLabel.setText("Expected Expiration Date:");
        jLabel3.setVerifyInputWhenFocusTarget(true);
        jLabel3.setText("As User: ");
        cpuNumText.setText("4");
        cpuNumberLabel.setText("Max CPU number:");
        cpuNumText.setVisible(false);
        cpuNumberLabel.setVisible(false);
        userNameText.setText(System.getProperty("user.name"));
        userNameText.addActionListener(new
            CreateGridServicePanel_jTextField1_actionAdapter(this));
        controlPanel.setPreferredSize(new Dimension(200, 320));

        this.add(blastData, BorderLayout.CENTER);
        blastData.add(jSplitPane2, BorderLayout.CENTER);
        jPanel2.add(jobnametField,  new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 19, 0, 1), 127, 0));
        jPanel2.add(jobExpirationBox1,  new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(8, 19, 0, 95), 37, 1));
        jPanel2.add(jobExpirationLabel,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 2, 0, 66), 20, 11));
        jPanel2.add(jobnameLabel,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(22, 2, 0, 85), 23, 11));
        jPanel2.add(cpuNumberLabel,    new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(7, 2, 0, 124), 32, 11));
        jPanel2.add(cpuNumText,  new GridBagConstraints(0, 5, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 19, 5, 175), 0, 0));
        jSplitPane1.add(detailPanel, JSplitPane.BOTTOM);
        jSplitPane1.add(sessionPanel, JSplitPane.TOP);
        jSplitPane2.add(controlPanel, JSplitPane.LEFT);
        controlPanel.add(jPanel6, BorderLayout.CENTER);
        jPanel6.add(treeView, BorderLayout.CENTER);
        jSplitPane2.add(jSplitPane1, JSplitPane.RIGHT);
        detailPanel.add(checkButton);
        detailPanel.add(viewButton, null);
        detailPanel.add(jLabel3, null);
        detailPanel.add(userNameText, null);
        jPanel1.add(jRadioButton3, null);
        jPanel1.add(jRadioButton2, null);
        jPanel1.add(jRadioButton1, null);
        sessionPanel.add(jPanel1, java.awt.BorderLayout.WEST);
        sessionPanel.add(jPanel2, java.awt.BorderLayout.CENTER);
        table.addMouseListener(
            new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() < 2) {
                    return;
                }

                int i = table.getSelectedRow();

                String location = model.getLocation(i);

                try {
                    //ServiceBrowser browser = context.getBrowser();
                    //browser.refreshService(location);
                    //System.out.println(location);
                    SystemInformation syInfo = new SystemInformation();
                    syInfo.setCost(new Integer(new Random().nextInt()).toString());
                    sdModel.setSystemInformation(syInfo);
                    //      sdvPanel.setModel(sdModel);

                }
                catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
        );
    }

    public void start(String perferName) {
        try {

            //String input = "C:/test.txt";
            CSSequenceSet seq = pv.getFastaFile(); //

            if (seq == null) {
                pv.reportError("Please select a sequence file first!",
                               "Parameter Error");
                return;

            }

            String input = seq.getFASTAFileName();
            //test life cycle and factory set

            // String perferedInstanceName = "BlastNumber" + sequenceNum++;

            String location = createInstance(perferName).toString();
            location = location.replaceAll("127.0.0.1", "adgate.cu-genome.org");
            System.out.println("use blastservice: " + location);
            String defaultParameter =
                "pb blastall -p blastp -d ncbi/pdbaa -T T ";

            Client client = new Client(defaultParameter, input, location);
            BlastAlgorithm algo = new BlastAlgorithm(true, input, client);
            algo.setBlastAppComponent(pv.getBlastAppComponent());
            algo.start();
        }
        catch (Exception f) {
            f.printStackTrace();
        }

    }

    public void runBlast_actionPerformed(ActionEvent e) {
        String perferedInstanceName = jobnametField.getText();
        start(perferedInstanceName);

    }

    public Object createInstance(Object InstanceName) throws Exception {
        //  if ( (this.context == null) || (this.defaultEndpoint == null)) {
        // TODO: defaultEndpoint error
        // System.out.println("enter 2" + context + "default" +
        //                      this.defaultEndpoint);
        //CHECK!
        //return;
        ///   }

        try {
            DEFAULTFACTORYURL = "http://gridgate.genomecenter.columbia.edu:18080/ogsa/services/edu/columbia/SequenceFactoryService";
            URL defaultFactoryEndpoint = new URL(DEFAULTFACTORYURL);
            this.defaultEndpoint = defaultFactoryEndpoint;
            OGSIServiceGridLocator factoryService =
                new OGSIServiceGridLocator();
            GridServiceFactory factory =
                new GridServiceFactory(
                factoryService.getFactoryPort(this.defaultEndpoint)
                );

            GSIUtils.setDefaultGSIProperties(
                factory.getStub(), this.defaultEndpoint
                );

            //this.context.setAuthentication(factory.getStub());

            LocatorType locator = factory.createService(InstanceName.toString());
            //LocatorType locator = factory.createService();
            System.out.println(locator);
            GSR reference = GSR.newInstance(locator);
            String location = reference.getHandle().toString();
            return location;

        }
        catch (ServiceAlreadyExistsFaultType saef) {
            String[] descriptions = saef.getDescription();
            String description =
                ( (descriptions != null) && (descriptions.length > 0))
                ? (": " + descriptions[0]) : "";
            ExtensibilityType extension = saef.getExtension();

            if (extension != null) {
                System.err.println(AnyHelper.getAsString(extension));
            }

            JOptionPane.showMessageDialog(
                this,
                "Failed to create instance: Service Already exists " +
                description, "Factory: createService error",
                JOptionPane.ERROR_MESSAGE
                );

            return null;
        }
        catch (FaultType gsf) {
            String[] descriptions = gsf.getDescription();
            String description =
                ( (descriptions != null) && (descriptions.length > 0))
                ? (": " + descriptions[0]) : "";
            ExtensibilityType extension = gsf.getExtension();

            if (extension != null) {
                System.err.println(AnyHelper.getAsString(extension));
            }

            JOptionPane.showMessageDialog(
                this,
                "Failed to create instance: Grid Service Fault: " +
                description, "Factory: createService error",
                JOptionPane.ERROR_MESSAGE
                );

            return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                this, "Failed to create instance: " + e.getMessage(),
                "Factory: createService error", JOptionPane.ERROR_MESSAGE
                );
            System.err.println(MessageUtils.toString(e));

            return null;
        }
    }

    public void setPv(BlastAppComponent pv) {
        this.pv = pv;
    }

    public BlastAppComponent getPv() {
        return pv;
    }

    public void checkServices_actionPerformed(ActionEvent e) {
        //getRegistry();
        model.setPrimarySelectionCrit("Factory");
        model.getRegistry();
    }

    void jRadioButton3_actionPerformed(ActionEvent e) {

    }

    void viewButton_actionPerformed(ActionEvent e) {

    }

    public void jTextField1_actionPerformed(ActionEvent e) {

    }

    public void checkButton_actionPerformed(ActionEvent e) {
       //  CreateGridServiceDialog csd = new CreateGridServiceDialog(null, "grid service");

       JDialog gsd = new GridSessionsViewDialog( );
     gsd.setVisible(true);
    }
}


class CreateGridServicePanel_checkButton_actionAdapter implements
        ActionListener {
    private CreateGridServicePanel adaptee;
    CreateGridServicePanel_checkButton_actionAdapter(CreateGridServicePanel
            adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.checkButton_actionPerformed(e);
    }
}


class CreateGridServicePanel_jTextField1_actionAdapter
    implements ActionListener {
    private CreateGridServicePanel adaptee;
    CreateGridServicePanel_jTextField1_actionAdapter(CreateGridServicePanel
        adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jTextField1_actionPerformed(e);
    }
}

class CreateGridServicePanel_viewButton_actionAdapter
    implements java.awt.event.ActionListener {
    CreateGridServicePanel adaptee;

    CreateGridServicePanel_viewButton_actionAdapter(CreateGridServicePanel
        adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.runBlast_actionPerformed(e);
    }
}
