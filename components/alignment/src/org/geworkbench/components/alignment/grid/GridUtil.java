package org.geworkbench.components.alignment.grid;

import java.io.*;
import java.net.*;
import java.rmi.*;
import java.util.*;
import javax.xml.rpc.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;


import org.globus.axis.gsi.*;
import org.globus.ogsa.*;
import org.globus.ogsa.client.managers.*;
import org.globus.ogsa.gui.*;
import org.globus.ogsa.impl.security.*;
import org.globus.ogsa.impl.security.authorization.*;
import org.globus.ogsa.utils.*;
import org.globus.progtutorial.clients.BlastService.*;
import org.gridforum.ogsi.*;
import org.w3c.dom.*;

import org.globus.ogsa.wsdl.GSR;


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
public class GridUtil
    extends AbstractPortTypePanel implements NotificationSinkCallback {
    private String DEFAULTURL = "http://adgate.cu-genome.org:8080/ogsa/services/core/registry/ContainerRegistryService";
    private Object[] intanceArray;
    private XMLTree entryTree;
    private Timer timer;

    private Hashtable properties = new Hashtable();
    private NotificationSinkManager manager;
    private String sink;

    public GridUtil() {
        super("Create Instance Table");

        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void jbInit() throws Exception {

        // this.model = new BlastListTableModel();


        try {
            this.entryTree = new XMLTree(XmlFactory.newDocument());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Object[] getInstances() {
        return null;
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

                    intanceArray = AnyHelper.getAsObject(
                        serviceDataValues, EntryType.class
                        );

                }
            }
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(
                this, "Failed to Connect to server: " + e.getMessage(),
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

            AnyHelper.getAsObject(serviceDataValues, EntryType.class);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}

/*
  class BlastGridServiceDataPanel
    extends AbstractPortTypePanel implements NotificationSinkCallback {
    private BlastListTableModel model;
    private Timer timer;
    private XMLTree entryTree;
    private Hashtable properties = new Hashtable();
    private NotificationSinkManager manager;
    private String sink;
    private ParameterViewWidget pv;
    JCheckBox autoUpdateCB;
    JButton updateBT;
    JTable table;
    JPanel btPanel;
    JTabbedPane tbPane;
    JScrollPane p1;
    JScrollPane p2;
    ServiceDataModel sdModel;
    JPanel blastData;
    ServiceDataViewPanel sdvPanel;
    private String DEFAULTURL = "http://adgate.cu-genome.org:8080/ogsa/services/core/registry/ContainerRegistryService";
    private String DEFAULTFACTORYURL = "http://adgate.cu-genome.org:8080/ogsa/services/progtutorial/core/second/BlastFactoryService";

    JButton runBlast = new JButton("Refresh");
    JButton checkServices = new JButton("Refresh");

    //private String DEFAULTURL = "http://localhost:8080/ogsa/services/core/registry/ContainerRegistryService";

    public BlastGridServiceDataPanel() {
        super("Blast Grid Service Information");

        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
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

    private void jbInit() throws Exception {
        sdModel = new ServiceDataModel();
        sdvPanel = new ServiceDataViewPanel(sdModel);

        autoUpdateCB = new JCheckBox("Auto update");
        autoUpdateCB.setSelected(false);

        this.timer =
            new Timer(
                5000,
                new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getRegistry();
            }
        }
        );

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

        updateBT = new JButton("Refresh");

        updateBT.addActionListener(
            new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getRegistry();
            }
        }
        );

        this.model = new BlastListTableModel();

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

        btPanel = new JPanel();
        runBlast.setText("Run Blast");
        runBlast.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runBlast_actionPerformed(e);
            }
        });
        checkServices.setText("Check Services");
        checkServices.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkServices_actionPerformed(e);
            }
        });
        btPanel.add(checkServices);
        btPanel.add(autoUpdateCB);
        btPanel.add(updateBT);
        btPanel.add(runBlast);

        tbPane = new JTabbedPane();
        blastData = new JPanel();
        blastData.setLayout(new BorderLayout());
        JScrollPane hitsPane = new JScrollPane(table);
        blastData.add(hitsPane, BorderLayout.CENTER);
        hitsPane.setPreferredSize(new Dimension(100, 150));
        blastData.add(sdvPanel, BorderLayout.SOUTH);
        p1 = new JScrollPane(blastData);
        //p1 = new JScrollPane(table);
        p1.setPreferredSize(new Dimension(200, 150));

        p2 = new JScrollPane(this.entryTree);
        p2.setPreferredSize(new Dimension(200, 150));

        tbPane.add("BlastGrid", p1);
        tbPane.add("ServiceGroup XML", p2);

        add(tbPane, BorderLayout.CENTER);
        add(btPanel, BorderLayout.SOUTH);
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
                    sdvPanel.setModel(sdModel);

                }
                catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
        );

    }

    public void runBlast_actionPerformed(ActionEvent e) {
        try {

            //String input = "C:/test.txt";
            SequenceDB seq = pv.getFastaFile(); //

            if (seq == null) {
                pv.reportError("Please select a sequence file first!",
                               "Parameter Error");
                return;

            }

            String input = seq.getFASTAFileName();
            //test life cycle and factory set


            String location = createInstance();
 location = location.replaceAll("127.0.0.1", "adgate.cu-genome.org");
            System.out.println("use blastservice: " + location);
            String defaultParameter =
                "pb blastall -p blastp -d ncbi/pdbaa -T T ";

            Client client = new Client(defaultParameter, input, location);

            String output = client.submitRequest(input);
            System.out.println(output);
            BrowserLauncher.openURL(output);
            URL url = new URL(output);
            String tempFolder = System.getProperties().getProperty(
                "temporary.files.directory");
            if (tempFolder == null) {
                tempFolder = ".";

            }

            String filename = "C:\\" + url.getFile();
            PrintWriter bw = new PrintWriter(new FileOutputStream(filename));
            URLConnection urlCon = url.openConnection();

            StringBuffer sb = new StringBuffer("");
            String line = "";

            BufferedReader br = new BufferedReader(
                new InputStreamReader(urlCon.getInputStream()));
            while ( (line = br.readLine()) != null) {
                bw.println(line);
            }
            br.close();
            bw.close();

            BlastDataSet blastResult = new BlastDataSet( filename, input);
            System.out.println(input + " " + filename);
            ProjectNodeAddedEvent event =
                new ProjectNodeAddedEvent(null, "message", null,
                                          blastResult);
            BlastAppComponent blastAppComponent = pv.getBlastAppComponent();
                  blastAppComponent.throwEvent(ProjectNodeAddedListener.class,
                                             "projectNodeAdded", event);


        }
        catch (Exception f) {
            f.printStackTrace();
        }

    }

    private String createInstance() throws Exception {
        if ( (this.context == null) || (this.defaultEndpoint == null)) {
            // TODO: defaultEndpoint error
        // System.out.println("enter 2" + context + "default" +
         //                      this.defaultEndpoint);
            //CHECK!
            //return;
        }

        try {

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

            LocatorType locator = factory.createService();

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
            JOptionPane.showMessageDialog(
                this, "Failed to create instance: " + e.getMessage(),
                "Factory: createService error", JOptionPane.ERROR_MESSAGE
                );
            System.err.println(MessageUtils.toString(e));

            return null;
        }
    }

    public void setPv(ParameterViewWidget pv) {
        this.pv = pv;
    }

    public ParameterViewWidget getPv() {
        return pv;
    }

    public void checkServices_actionPerformed(ActionEvent e) {
        getRegistry();
    }
 }
 */
