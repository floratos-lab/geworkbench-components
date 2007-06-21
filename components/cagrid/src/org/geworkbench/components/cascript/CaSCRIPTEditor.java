package org.geworkbench.components.cascript;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.List;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.*;

import org.geworkbench.engine.cascript.CasEngine;
import org.geworkbench.engine.cascript.CasException;
import org.geworkbench.engine.cascript.CasDataTypeImport;
import org.geworkbench.engine.cascript.CasDataPlug;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.config.PluginDescriptor;
import org.geworkbench.engine.management.ComponentRegistry;
import org.geworkbench.engine.management.Documentation;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.util.PropertiesMonitor;
import org.geworkbench.components.cagrid.CaGridPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis.message.addressing.EndpointReferenceType;
import com.ibm.wsdl.xml.WSDLReaderImpl;
import com.ibm.wsdl.ServiceImpl;
import com.ibm.wsdl.PortImpl;
import com.ibm.wsdl.BindingOperationImpl;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;
import gov.nih.nci.cagrid.discovery.MetadataUtils;

/**
 * <p>Title: caSCRIPT Editor</p>
 *
 * <p>Description: Provides a textual frontend for caSCRIPT input to caWorkbench</p>
 *
 * <p>Copyright: Copyright (c) 2003-2005</p>
 *
 * <p>Company: Columbia University</p>
 *
 * @author Behrooz Badii
 * @author manjunath at genomecenter dot columbia dot edu
 * @version 3.0
 */
public class CaSCRIPTEditor implements VisualPlugin {

    static Log log = LogFactory.getLog(CaSCRIPTEditor.class);

    private HashMap<String, OperationInfo> paramModuleMap = new HashMap<String, OperationInfo>();
    private OperationInfo hierClusterOperation = new OperationInfo(null, "execute", "HierarchicalClustering:ExecuteResponse",
            "HierarchicalClustering:ExecuteRequest", "HierClusterModule",
            "datatype DSHierClusterDataSet cluster = cagrid.doClustering(<DSMicroarraySet>, \"Average\", \"Both\", \"Euclidean\", <url>);\n"
            );
    private OperationInfo somClusterOperation = new OperationInfo(null, "execute", "SomClustering:ExecuteResponse",
            "SomClustering:ExecuteRequest", "SomClusterModule",
            "datatype DSSOMClusterDataSet cluster = cagrid.doSOMClustering(<DSMicroarraySet>, 0.8, 3, 3, 0, 4000, 3.0, <url>);\n"
            );

    {
        paramModuleMap.put(hierClusterOperation.getParameters(), hierClusterOperation);
        paramModuleMap.put(somClusterOperation.getParameters(), somClusterOperation);
    }

    HashMap<String, String> urlToDescription = new HashMap<String, String>();

    //CDTIImport is for the datatype testing of CTRL Period
    CasDataTypeImport CDTI = new CasDataTypeImport();
    CTP ctrlP;
    JEditorPane editor = new JEditorPane();
    JScrollPane scrollPane = new JScrollPane();
    JPanel jPanel1 = new JPanel();
    JToolBar jToolBar1 = new JToolBar();
    JButton jButton1 = new JButton();
    JButton jButton2 = new JButton();
    JButton jButton3 = new JButton();
    Component component1 = Box.createHorizontalStrut(8);
    Component component2 = Box.createHorizontalStrut(8);
    JButton jButton4 = new JButton();
    Component component3 = Box.createHorizontalStrut(8);
    String scriptText = "", partScript = "";
    int caretPlace = -1;
    String eoln = System.getProperty("line.separator");
       String LASTDIR = "lastdir";
    JTabbedPane rightTabs = new JTabbedPane();

    BorderLayout borderLayout1 = new BorderLayout();
    JPanel container = new JPanel();
    JSplitPane jSplitPane1 = new JSplitPane();
    JSplitPane jSplitPane2 = new JSplitPane();
    JScrollPane jScrollPane1 = new JScrollPane();
    JScrollPane jScrollPane2 = new JScrollPane();
    JList pluginList = new JList(); //holds plugin descriptors
    DefaultListModel dlm1 = new DefaultListModel();
    CasJTableModel dtm1 = new CasJTableModel();
    DefaultListCellRenderer dlcr1 = new DefaultListCellRenderer();
    String[] columnnames = {"Method name", "Return type", "Parameters"};
    JTable methodTable = new JTable(); //holds method for reach plugin descriptor

    // Grid components
    private JScrollPane gridServiceScrollPane = new JScrollPane();
    private DefaultListModel gridServiceListModel = new DefaultListModel();
    private JList gridServiceList;
    private JScrollPane gridMethodScrollPane = new JScrollPane();
    private GridMethodTableModel gridMethodModel = new GridMethodTableModel();
    private JTable gridMethodTable = new JTable(gridMethodModel);
    private JSplitPane gridTableSplitPane = new JSplitPane();
    private JTextField gridDiscoverySericeField = new JTextField("cagridnode.c2b2.columbia.edu");
    private int gridPort = 8080;
    private JButton gridDiscoverServicesButton = new JButton("Discover");

    public CaSCRIPTEditor(){
        try {
            jbInit();
            processFile(new File("cascripts/gridbasic.script"));
        } catch (Exception e) {}
    }

    void jbInit(){
        pluginList.setCellRenderer(dlcr1);
        methodTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //methodTable.setAutoCreateColumnsFromModel(false);
        component3 = Box.createHorizontalStrut(8);
        component2 = Box.createHorizontalStrut(8);
        component1 = Box.createHorizontalStrut(8);
        jButton1.setToolTipText("Save Script");
        URL res = CaSCRIPTEditor.class.getResource("Save16.gif");
        ImageIcon image = new ImageIcon(res, "Save icon");
        jButton1.setIcon(image);
        jButton1.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                save_actionPerformed(e);
            }
        });

        jButton2.setToolTipText("Open Script");
        res = CaSCRIPTEditor.class.getResource("Open16.gif");
        image = new ImageIcon(res, "Load icon");
        jButton2.setIcon(image);
        jButton2.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                open_actionPerformed(e);
            }
        });

        jButton3.setToolTipText("Stop Script");
        res = CaSCRIPTEditor.class.getResource("Stop16.gif");
        image = new ImageIcon(res, "Stop icon");
        jButton3.setIcon(image);
        jButton3.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                stop_actionPerformed(e);
            }
        });

        jToolBar1.setBorder(BorderFactory.createLineBorder(Color.black));

        jButton4.setToolTipText("Run Script");
        res = CaSCRIPTEditor.class.getResource("Play16.gif");
        image = new ImageIcon(res, "Run icon");
        jButton4.setIcon(image);
        jButton4.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                run_actionPerformed(e);
            }
        });

        jSplitPane1.setDividerSize(3);

        pluginList.addMouseMotionListener(new MouseMotionAdapter(){
            public void mouseMoved(MouseEvent e){
                jList1_mouseMoved(e);
            }
        });

        pluginList.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                jList1_mouseClicked(e);
            }
        });

        methodTable.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                methodTable_mouseClicked(e);
            }
        });

        jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setDividerSize(3);

        jToolBar1.add(jButton4);
        jToolBar1.add(component3);
        jToolBar1.add(jButton3);
        jToolBar1.add(component1);
        jToolBar1.add(jButton1);
        jToolBar1.add(component2);
        jToolBar1.add(jButton2);
        jSplitPane1.add(rightTabs, JSplitPane.RIGHT);
        rightTabs.add(jSplitPane2, "Local");
        jSplitPane2.add(jScrollPane1, JSplitPane.TOP);
        jSplitPane2.add(jScrollPane2, JSplitPane.BOTTOM);
        jScrollPane2.getViewport().add(methodTable);
        jScrollPane1.getViewport().add(pluginList);

        jPanel1.setLayout(borderLayout1);
        jPanel1.add(scrollPane, java.awt.BorderLayout.CENTER);
        jPanel1.add(jToolBar1, java.awt.BorderLayout.NORTH);
        scrollPane.getViewport().add(editor);

        jSplitPane1.add(jPanel1, JSplitPane.LEFT);
        editor.addCaretListener(new CaretListener(){
            public void caretUpdate(CaretEvent ce){
                editor_caretUpdate(ce);
            }
        });
        editor.addKeyListener(new KeyListener(){
            public void keyPressed(KeyEvent ke){
                editor_keyPressed(ke);
            }
            public void keyReleased(KeyEvent ke){
                editor_keyReleased(ke);
            }
            public void keyTyped(KeyEvent ke){
                editor_keyTyped(ke);
            }
        });
        jSplitPane1.setDividerLocation(450);
        jSplitPane2.setDividerLocation(125);
        pluginList.setModel(dlm1);
        dtm1.setColumnIdentifiers(columnnames);
        methodTable.setModel(dtm1);
        methodTable.setDefaultRenderer(Object.class, new MethodDetailsTableCellRenderer());
        pluginList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        container.setLayout(new BorderLayout());
        container.add(jSplitPane1, BorderLayout.CENTER);

        // Grid related components
        JPanel gridContainer = new JPanel(new BorderLayout());

        JPanel fields = new JPanel(new BorderLayout());
        fields.add(gridDiscoverySericeField, BorderLayout.CENTER);
        fields.add(gridDiscoverServicesButton, BorderLayout.EAST);
        gridContainer.add(fields, BorderLayout.NORTH);

        gridTableSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        gridTableSplitPane.setDividerSize(3);

        gridServiceList = new JList(gridServiceListModel) {
            public String getToolTipText(MouseEvent evt) {
                int index = locationToIndex(evt.getPoint());
                Object item = getModel().getElementAt(index);
                return urlToDescription.get(item.toString());
            }
        };
        gridServiceScrollPane.setMinimumSize(new Dimension(100, 100));
        gridServiceScrollPane.getViewport().add(gridServiceList);
        gridTableSplitPane.add(gridServiceScrollPane, JSplitPane.TOP);
        gridMethodModel.setColumnIdentifiers(columnnames);
        gridMethodTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        gridMethodScrollPane.getViewport().add(gridMethodTable);
        gridTableSplitPane.add(gridMethodScrollPane, JSplitPane.BOTTOM);
        gridContainer.add(gridTableSplitPane, BorderLayout.CENTER);
        rightTabs.add(gridContainer, "Grid");
        gridTableSplitPane.setDividerLocation(-1);      // Attempt to respect preferred sizes of components

        gridDiscoverServicesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    java.util.List<String> serviceURLs = CaGridPanel.getServiceURLs(gridDiscoverySericeField.getText(), gridPort);
                    EndpointReferenceType[] services = CaGridPanel.getServices(gridDiscoverySericeField.getText(), gridPort, null);
                    gridServiceListModel.clear();
                    urlToDescription.clear();
                    int index = 0;
                    for (String url : serviceURLs) {
                        log.debug("Added URL: " + url);
                        gridServiceListModel.addElement(url);
                        ServiceMetadata commonMetadata = MetadataUtils .getServiceMetadata(services[index]);

                        String desc = commonMetadata.getServiceDescription().getService().getDescription();
                        log.debug("Adding description: " + desc);
                        urlToDescription.put(url, desc);
                    }
                    gridServiceList.repaint();

                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });

        gridServiceList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                String urlSelected = (String) gridServiceList.getSelectedValue();
                try {
                    List<OperationInfo> operations = getValidOperations(urlSelected);
                    int rowCount = gridMethodModel.getRowCount();
                    for (int i = 0; i < rowCount; i++) {
                        gridMethodModel.removeRow(0);
                    }
                    for (OperationInfo operation : operations) {
                        gridMethodModel.addRow(new String[]{operation.getName(), formatForDisplay(operation.getParameters()), formatForDisplay(operation.getReturnType())});
                    }
                    gridMethodModel.fireTableDataChanged();
                } catch (WSDLException e) {
                    log.error(e);
                }
            }
        });


        gridMethodTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String param = (String) gridMethodModel.getValueAt(gridMethodTable.getSelectedRow(), 2);
                    log.debug("Searching for operation based on params: " + param);
                    CaSCRIPTEditor.OperationInfo operation = paramModuleMap.get(param);
                    if (operation != null) {
                        log.debug("Found operation: " + operation.getModuleName());
                        String operString = new String(operation.getScriptString());
                        operString = operString.replaceAll("<url>", "\"" + (String) gridServiceList.getSelectedValue() + "\"");
                        editor.replaceSelection(operString);
                    }
                }
            }
        }
        );
    }

    private String formatForDisplay(String gridObject) {
        // Example: {http://cagrid.geworkbench.columbia.edu/HierarchicalClustering}ExecuteRequest
        String domain = gridObject.substring(gridObject.lastIndexOf("/")+1, gridObject.lastIndexOf("}"));
        String objectName = gridObject.substring(gridObject.lastIndexOf("}")+1);
        return domain + ":" + objectName;
    }

    private List<OperationInfo> getValidOperations(String url) throws WSDLException {
        WSDLReader wsdlReader = new WSDLReaderImpl();
        Definition def = wsdlReader.readWSDL(null, url + "?wsdl");

        ArrayList<OperationInfo> operations = new ArrayList<OperationInfo>();
        Set<Map.Entry> set = def.getServices().entrySet();
        for (Map.Entry entry : set) {
            ServiceImpl service = (ServiceImpl) entry.getValue();
            log.debug("Found service: " + service.getQName());
            Set<Map.Entry> portEntries = service.getPorts().entrySet();
            for (Map.Entry portEntry : portEntries) {
                PortImpl port = (PortImpl) portEntry.getValue();
                log.debug("--Found port: " + port.getName());
                Binding binding = port.getBinding();
                List<BindingOperationImpl> bindingOperations = binding.getBindingOperations();
                for (BindingOperationImpl bindingOperation : bindingOperations) {
                    Operation operation = bindingOperation.getOperation();
                    log.debug("----Found operation: " + operation.getName());
                    Message inMessage = operation.getInput().getMessage();
                    Message outMessage = operation.getOutput().getMessage();
                    Part inPart = inMessage.getPart("parameters");
                    Part outPart = outMessage.getPart("parameters");
                    if (inPart != null && outPart != null) {
                        log.debug("------Input: " + inPart.getElementName());
                        log.debug("------Output: " + outPart.getElementName());
                        operations.add(new OperationInfo(operation.getName(), inPart.getElementName().toString(), outPart.getElementName().toString()));
                    }
                }
            }
        }
        return operations;
    }

    public Component getComponent() {
        return container;
    }

    boolean pluginsInited = false;

    void editor_keyPressed(KeyEvent ke){
    }

    void editor_keyReleased(KeyEvent ke){
        if (ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_PERIOD ){
            //System.out.println("Cntrl + . pressed");
            //System.out.println("entire script");
            //System.out.println(scriptText);
            //System.out.println("part of script");
            //System.out.println(partScript);
            //big connection to the secondary parser
            ctrlP = new CTP(new String(scriptText), new String(partScript));
            String ret = ctrlP.testid();
            //System.out.println(ret[1]);
            Method[] m = null;
            if (CDTI.containsKey(ret)) {
                m = CDTIhelp(ret);
            } else {
                Object[] pds = ComponentRegistry.getRegistry().getAllPluginDescriptors().toArray();
                for (int i = 0; i < pds.length; i++) {
                    if (((PluginDescriptor)pds[i]).getID().equals(ret)) {
                        m = PluginDescriptorHelp(ComponentRegistry.getRegistry().getPluginDescriptorByID(ret));
                        break;
                    }
                }
                //how to check if it exists before trying to get it;
            }
            filljTable1(m);
            if(m != null) {
                //this focuses the list of plugin descriptors to what we're working with
                int selectedindex = dlm1.indexOf(ret);
                pluginList.setSelectedIndex(selectedindex);
                pluginList.ensureIndexIsVisible(selectedindex);
            }
            //System.out.println("done");
        }
    }

    void editor_keyTyped(KeyEvent ke){
    }

    //shows all the plugins available for use in caScript
    void jList1_mouseMoved(MouseEvent e){
        if (!pluginsInited){
            Collection<PluginDescriptor> pd = ComponentRegistry.getRegistry().getAllPluginDescriptors();
            dlm1.removeAllElements();
            PluginDescriptor[] descriptors = pd.toArray(new PluginDescriptor[pd.size()]);
            Arrays.sort(descriptors);
            for (PluginDescriptor desc : descriptors) {
                dlm1.addElement(desc);
            }
            Object[] keySet = CDTI.keySet().toArray();
            Arrays.sort(keySet);
            for (Object o : keySet) {
                dlm1.addElement(o);
            }
            pluginList.setModel(new DefaultListModel());
            pluginList.setModel(dlm1);
            pluginsInited = true;
        }
        jSplitPane2.repaint();
    }

    //if 2, insert variable declaration, if 1, shows all methods
    void jList1_mouseClicked(MouseEvent e){
        if (e.getClickCount() == 2) {
            if (CDTI.containsKey(pluginList.getSelectedValue())) {
                editor.replaceSelection("datatype " + ((pluginList.getSelectedValue())) + " <datatype-name>;\n");
            } else {
                editor.replaceSelection("module " + ((PluginDescriptor)(pluginList.getSelectedValue())).getID() + " <module-name>;\n");
            }
        } else if (e.getClickCount() == 1) {
            populatejTable1();
        }
        jSplitPane2.repaint();
    }

    //if 1, nothing, if 2, insert method call
    void methodTable_mouseClicked(MouseEvent e){
        if (e.getClickCount() == 1) {
            ;
        } else if (e.getClickCount() == 2) {
            editor.replaceSelection(writeMethodtoEditor());
        }
        jSplitPane2.repaint();
    }

    Vector<Documentation> currentDocs = new Vector<Documentation>();

    String writeMethodtoEditor(){
        int row = methodTable.getSelectedRow();
        String mname = ((String)methodTable.getValueAt(row, 0));
        String mparams = ((String)methodTable.getValueAt(row, 2));
        return mname + "(" + mparams + ");\n";
    }

    //only called if pluginList.getSelecteValue is in CDTI
    Method[] CDTIhelp(String t) {
        CasDataPlug temp = new CasDataPlug(null, t, CDTI);
        Class clazz= temp.getRealClass();
        Method[] m = clazz.getMethods();
        return m;
    }

    Method[] PluginDescriptorHelp(PluginDescriptor t) {
        PluginDescriptor selected = t;
        return ComponentRegistry.getRegistry().getAllScriptMethods(selected.getPluginClass());
    }

    void filljTable1(Method[] m) {
        if (m != null) {
            dtm1.setRowCount(0); //remove all rows
            int count = 0;
            currentDocs.clear();
            for (Method me : m) {
                Vector methodinfo = new Vector();
                methodinfo.add(0, me.getName());
                methodinfo.add(1, me.getReturnType().getSimpleName());
                methodinfo.add(2, returnSimpleParameters(me.getParameterTypes()));
                dtm1.addRow(methodinfo);
                Documentation doc = (Documentation)me.getAnnotation(Documentation.class);
                currentDocs.add(count, doc);
                count++;
            }
//            dtm1.sortAllRowsBy(0, true);
            methodTable.setModel(new CasJTableModel());
            methodTable.setModel(dtm1);
        }
    }

    //Method is not comparable, should we even sort it?
    //shows all the methods for the selected plugindescriptor in jlist1
    void populatejTable1() {
        Method[] m = null;
        if (CDTI.containsKey(pluginList.getSelectedValue())) {
            m = CDTIhelp(pluginList.getSelectedValue().toString());
        } else {
            m = PluginDescriptorHelp((PluginDescriptor)pluginList.getSelectedValue());
        }
        filljTable1(m);
    }

    //returns simple parameters for easy reading
    String returnSimpleParameters(Class[] clazz) {
        StringBuilder params = new StringBuilder();
        if (clazz.length > 0) {
            params.append(clazz[0].getSimpleName());
            for (int i = 1; i < clazz.length; i++) {
                params.append(", " + clazz[i].getSimpleName());
            }
        }
        return params.toString();
    }

  void open_actionPerformed(ActionEvent e) {
        String lastdir = ".";
        try {
            lastdir = PropertiesManager.getInstance().getProperty(getClass(), LASTDIR, "Default Value");

            JFileChooser fc = new JFileChooser(lastdir);
            String scriptFilename = null;
            FileFilter filter = new CaSCRIPTFileFilter();
            fc.setFileFilter(filter);
            fc.setDialogTitle("Open Script");
            String extension = ((CaSCRIPTFileFilter) filter).getExtension();
            int choice = fc.showOpenDialog(null);
            if (choice == JFileChooser.APPROVE_OPTION) {
                lastdir = fc.getSelectedFile().getParentFile().getAbsolutePath();
                PropertiesManager.getInstance().setProperty(getClass(), LASTDIR, lastdir);
                scriptFilename = fc.getSelectedFile().getAbsolutePath();
                if (!scriptFilename.endsWith(extension)) {
                    scriptFilename += extension;
                }
                processFile(new File(scriptFilename));
            }
        } catch (Exception er) {
        }
    }
    void processFile(File file){
        try {
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            String line = "";
            scriptText = "";
            while ((line = br.readLine()) != null){
                scriptText += line + eoln;
            }
            editor.setText(scriptText);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(null,
                    "Check that the file contains a valid script.",
                    "Open Script Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void save_actionPerformed(ActionEvent e){
        JFileChooser fc = new JFileChooser(PropertiesMonitor.getPropertiesMonitor().getDefPath());
        String scriptFilename = null;
        FileFilter filter = new CaSCRIPTFileFilter();
        fc.setFileFilter(filter);
        fc.setDialogTitle("Save Script");
        String extension = ((CaSCRIPTFileFilter) filter).getExtension();
        int choice = fc.showSaveDialog(null);
        if (choice == JFileChooser.APPROVE_OPTION) {
            scriptFilename = fc.getSelectedFile().getAbsolutePath();
            if (!scriptFilename.endsWith(extension)) {
                scriptFilename += extension;
            }
            try {
                FileOutputStream f = new FileOutputStream(scriptFilename);
                String[] lines = scriptText.split(eoln);
                for (String line : lines){
                    f.write(line.getBytes());
                    f.write(eoln.getBytes());
                }
                f.flush();
                f.close();
            } catch (IOException ex) {
                System.out.println("Exception while saving script. " + ex.getMessage());
            }
        }
    }

    void run_actionPerformed(ActionEvent e){
        try {
            CasEngine.runScript(editor.getText());
        } catch (CasException exc) {
            JOptionPane.showMessageDialog(getComponent(), "caScript " + exc.getMsg() + "\nStopping script.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(getComponent(), "System error: \n" + ex.getMessage() +"\nStopping script.");
        }
    }

    void stop_actionPerformed(ActionEvent e){

    }

    void editor_caretUpdate(CaretEvent ce){
        scriptText = editor.getText();
        caretPlace = ce.getMark();
        //there is a discrepancy that needs to be filled because of newlines
        for (int i = 0; i < caretPlace; i++) {
            if (scriptText.charAt(i) == '\n') {
                caretPlace++;
            }
        }
        partScript = scriptText.substring(0, caretPlace);
    }

    class MethodDetailsTableCellRenderer extends DefaultTableCellRenderer{
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column){
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Documentation doc = currentDocs.get(row);
            ((JComponent)comp).setToolTipText(null);
            if (doc != null && doc.value() != null){
                ((JComponent)comp).setToolTipText(doc.value());
            }
            return comp;
        }
    }

    class CaSCRIPTFileFilter extends FileFilter {
        String fileExt;

        CaSCRIPTFileFilter() {
            fileExt = ".script";
        }

        public String getExtension() {
            return fileExt;
        }

        public String getDescription() {
            return "caCSRIPT Files";
        }

        public boolean accept(File f) {
            boolean returnVal = false;
            if (f.isDirectory() || f.getName().endsWith(fileExt)) {
                return true;
            }
            return returnVal;
        }
    }

    static class GridMethodTableModel extends DefaultTableModel {
        public boolean isCellEditable(int i, int i1) {
            return false;
        }
    }

    class OperationInfo {
        private String url;
        private String name;
        private String returnType;
        private String parameters;
        private String moduleName;
        private String scriptString;

        public OperationInfo(String url, String name, String returnType, String parameters, String moduleName, String scriptString) {
            this.url = url;
            this.name = name;
            this.returnType = returnType;
            this.parameters = parameters;
            this.moduleName = moduleName;
            this.scriptString = scriptString;
        }

        public OperationInfo(String name, String returnType, String parameters) {
            this.name = name;
            this.returnType = returnType;
            this.parameters = parameters;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getReturnType() {
            return returnType;
        }

        public void setReturnType(String returnType) {
            this.returnType = returnType;
        }

        public String getParameters() {
            return parameters;
        }

        public void setParameters(String parameters) {
            this.parameters = parameters;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getModuleName() {
            return moduleName;
        }

        public void setModuleName(String moduleName) {
            this.moduleName = moduleName;
        }

        public String getScriptString() {
            return scriptString;
        }

        public void setScriptString(String scriptString) {
            this.scriptString = scriptString;
        }
    }
}