package org.geworkbench.components.alignment.panels;

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


import edu.columbia.stubs.SequenceAlignmentService_sd.SequenceAlignmentPortType;
import edu.columbia.stubs.SequenceAlignmentService_sd.service.SequenceAlignmentServiceGridLocator;
import edu.columbia.stubs.SequenceAlignmentService_sd.servicedata.SessionDataType;
import org.globus.ogsa.ServiceData;
import org.globus.ogsa.gui.XMLTree;
import org.globus.ogsa.gui.XMLTreeModel;
import org.globus.ogsa.impl.core.handle.HandleHelper;
import org.globus.ogsa.types.properties.PropertiesDetailType;
import org.globus.ogsa.utils.AnyHelper;
import org.globus.ogsa.utils.GSIUtils;
import org.globus.ogsa.utils.QueryHelper;
import org.globus.ogsa.utils.XmlFactory;
import org.gridforum.ogsi.*;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.xml.rpc.Stub;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SimpleServiceDataListTableModel
    extends AbstractTableModel {
    private String[] columns = {
        "Name", "Value"};
    private List list;
    private String[] selectionCrit = {
        "blast", "Blast", "BLAST"};
    private XMLTree entryTree; //=  new XMLTree(XmlFactory.newDocument());;
    private String DEFAULTURL = "http://adgate.cu-genome.org:8080/ogsa/services/core/registry/ContainerRegistryService";
    private String gsr;
    private URL defaultEndpoint;
    public SimpleServiceDataListTableModel() {
        super();

    }

    public SimpleServiceDataListTableModel(String[] selectedMarkers) {
        super();
        selectionCrit = selectedMarkers;
        //getRegistry();

    }

    /**
     * create the content of the SDE.
     * @param selectedGSR String
     */
    public SimpleServiceDataListTableModel(String selectedGSR) {
        super();
        if (selectedGSR != null) {
            gsr = selectedGSR;
            setList();
        }

    }

    /**
     * setList
     *
     * @param gsr String
     */
    private void setList() {
        list = new ArrayList();
        try {
            //String s = "http://156.145.235.50:8080/ogsa/services/progtutorial/core/second/BlastFactoryService/hash-7359733-1114018273796";
            // Get command-line arguments
            // s = "http://adgate.cu-genome.org:8080/ogsa/services/progtutorial/core/second/BlastFactoryService/Test1";
            URL GSH = new java.net.URL(gsr);

            // Get a reference to the GridService portType
            OGSIServiceGridLocator locator = new OGSIServiceGridLocator();
            GridService gridService = locator.getGridServicePort(GSH);

            // gridServiceHandle
            // GSH of this instance (can have more than one)
            ExtensibilityType extensibility = gridService.findServiceData(
                QueryHelper.getNamesQuery("gridServiceHandle"));
            ServiceDataValuesType serviceData = AnyHelper.
                getAsServiceDataValues(extensibility);
            Object[] gridServiceHandles = AnyHelper.getAsObject(serviceData);
            for (int i = 0; i < gridServiceHandles.length; i++) {
                HandleType gridServiceHandle = (HandleType) gridServiceHandles[
                    i];
                String[] sh = {
                    "ServiceHandle", gridServiceHandle.toString()};
                System.out.println("gridServiceHandle: " + gridServiceHandle);
                list.add(sh);
            }

            // factoryLocator
            // Locator for the factory that created this instance
            extensibility = gridService.findServiceData(QueryHelper.
                getNamesQuery("factoryLocator"));
            serviceData = AnyHelper.getAsServiceDataValues(extensibility);
            if (serviceData.get_any() == null) {
                System.out.println("factoryLocator: Not created by a factory!");
            }
            else {
                Object[] factoryLocators = AnyHelper.getAsObject(serviceData);
                for (int i = 0; i < factoryLocators.length; i++) {
                    LocatorType factoryLocator = (LocatorType) factoryLocators[
                        i];
                    System.out.println("factoryLocator: " +
                                       factoryLocator.getHandle(0));
                    String[] sh = {
                        "FactoryLocator", factoryLocator.toString()};

                    list.add(sh);

                }
            }

            // terminationTime
            // The termination time for this service
            extensibility = gridService.findServiceData(QueryHelper.
                getNamesQuery("terminationTime"));
            serviceData = AnyHelper.getAsServiceDataValues(extensibility);
            TerminationTimeType terminationTime = (TerminationTimeType)
                AnyHelper.getAsSingleObject(serviceData);
            ExtendedDateTimeType after, before;
            after = terminationTime.getAfter();
            before = terminationTime.getBefore();
            String termTime[] = {
                "TerminationTime",
                after.getInfinityTypeValue().getValue().toString()};
            String startTime[] = {
                "StartupTime",
                terminationTime.getTimestamp().getTime().toString()};
            list.add(startTime);
            list.add(termTime);

            //For sequence grid service specific SDE.
            SequenceAlignmentServiceGridLocator mathServiceLocator = new
                SequenceAlignmentServiceGridLocator();
            SequenceAlignmentPortType math = mathServiceLocator.
                getSequenceAlignmentServicePort(GSH); // getMathServicePort(GSH);

// Get Service Data Element "MathData"
            extensibility =
                math.findServiceData(QueryHelper.getNamesQuery("SessionData"));
            serviceData = AnyHelper.getAsServiceDataValues(extensibility);
            SessionDataType mathData =
                (SessionDataType) AnyHelper.getAsSingleObject(serviceData,
                SessionDataType.class);

// Write service data
            System.out.println("Value: " + mathData.getValue());
            System.out.println("Previous operation: " + mathData.getLastOp());
            System.out.println("# of operations: " + mathData.getNumOps());

            String finishedPrecentage[] = {
                "Finished Precentage", new Integer(mathData.getValue()).toString()};
            String owner[] = {
                "owner", "xiaoqing"};
            String lastResult[] = {
                "Last Result", mathData.getLastOp()};
            String totalNumOps[] = {"# of operations", new Integer(mathData.getNumOps()).toString()};
             list.add(owner);
            list.add(finishedPrecentage);

            list.add(lastResult);
            list.add(totalNumOps);
            if (after.getInfinityTypeValue().getValue().equals("infinity")) {

                System.out.println(
                    "terminationTime (after): The service plans to exist indefinitely");
            }
            else if (after.getDateTimeValue().before(terminationTime.
                getTimestamp().getTime())) {
                System.out.println(
                    "terminationTime (after): May terminate at any time.");
            }
            else {
                System.out.println("terminationTime (after): " +
                                   after.getDateTimeValue().getTime());
            }

            if (before.getInfinityTypeValue().getValue().equals("infinity")) {
                System.out.println(
                    "terminationTime (before): The service has no plans to terminate.");
            }
            else if (before.getDateTimeValue().before(terminationTime.
                getTimestamp().getTime())) {
                System.out.println(
                    "terminationTime (before): The service is trying to terminate.");
            }
            else {
                System.out.println("terminationTime (before): " +
                                   before.getDateTimeValue().getTime());
            }

            System.out.println("terminationTime (timestamp): " +
                               terminationTime.getTimestamp().getTime());

            // serviceDataNames
            // Names of Service Data offered by this Grid Service


            // interfaces
            // Names of interfaces exposed by this Grid Service
            fireTableDataChanged();

        }
        catch (Exception e) {
            System.out.println("ERROR!");
            e.printStackTrace();
        }

    }

    public void setList(Object[] entries) {
        List newList = new ArrayList();

        for (int i = 0; i < entries.length; i++) {
            try {
                EntryType entry = (EntryType) entries[i];
                if (AnyHelper.contains(entry.getContent(), PropertiesDetailType.class)) {
                    newList.add(
                        AnyHelper.getAsSingleObject(
                            entry.getContent(), PropertiesDetailType.class
                        )
                        );
                    // System.out.println("1st: " + ((PropertiesDetailType)entry).getName() );
                    // System.out.println("1st: " + entry.getContent());
                }
                else {
                    newList.add(
                        entry.getMemberServiceLocator().getHandle()[0]

                        );
                    // System.out.println("2nd: " + entry.getContent());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.list = new ArrayList();
        for (int i = 0; i < newList.size(); i++) {
            Object obj = newList.get(i);
            if (obj instanceof PropertiesDetailType) {
                PropertiesDetailType entry = (PropertiesDetailType) obj;
                String name = entry.getName();
                System.out.println(name + name.indexOf("Blast"));
                int length = selectionCrit.length;
                boolean selectedItem = false;
                for (int k = 0; k < length; k++) {
                    if (name.indexOf("Instance") != -1) {
                        if (name.indexOf(selectionCrit[k]) != -1) {
                            this.list.add(obj);
                            break;
                            //System.out.println("match Blast.");
                        }

                    }
                    else {
                        break;
                    }

                }
            }

        }

        fireTableDataChanged();
    }

    public List getList() {
        return this.list;
    }

    public String[] getSelectionCrit() {
        return selectionCrit;
    }

    public String getGsr() {
        return gsr;
    }

    public String getLocation(int row) {
        return (String) getValueAt(row, 1);
    }

    public boolean isCellEditable(
        int row,
        int col
        ) {
        return false;
    }

    public String getColumnName(int index) {
        return this.columns[index];
    }

    public int getRowCount() {
        if (this.list == null) {
            return 0;
        }

        return this.list.size();
    }

    public int getColumnCount() {
        return this.columns.length;
    }

    public Object getValueAt(
        int row,
        int column
        ) {
        Object obj = this.list.get(row);
        if (obj instanceof PropertiesDetailType) {
            PropertiesDetailType entry = (PropertiesDetailType) obj;
            switch (column) {
                case 0:
                    return entry.getName();

                case 1:
                    String s[] = entry.getHandle().split("/");
                    return s[s.length - 1];

                case 2:
                    return "" + entry.getState();
            }
        }
        else if (obj instanceof HandleType) {
            switch (column) {
                case 0:
                    try {
                        return HandleHelper.getInstanceID( (HandleType) obj);
                    }
                    catch (Exception e) {
                        return "";
                    }
                    case 1:
                        return obj.toString();
            }
        }
        if (obj instanceof String[]) {
            String[] sde = (String[]) obj;
            switch (column) {
                case 0:
                    return sde[0];

                case 1:

                    return sde[1];
            }
        }

        return "";
    }

    private void jbInit() throws Exception {
    }

    public void setSelectionCrit(String[] selectionCrit) {
        this.selectionCrit = selectionCrit;
    }

    public void setGsr(String gsr) {
        this.gsr = gsr;
        setList();

    }

    public void getRegistry() {

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
                this.entryTree = new XMLTree(XmlFactory.newDocument());
                this.entryTree.setModel(new XMLTreeModel(element));

                ServiceDataValuesType serviceDataValues =
                    (ServiceDataValuesType) AnyHelper.getAsSingleObject(
                        queryResult, ServiceDataValuesType.class
                    );

                if (serviceDataValues.get_any() != null) {
                    setList(
                        AnyHelper.getAsObject(
                            serviceDataValues, EntryType.class
                        )
                        );
                }
            }
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(
                null, "Failed to get registry: " + e.getMessage(),
                "Registry: getRegistry error", JOptionPane.ERROR_MESSAGE
                );
            e.printStackTrace();

            return;
        }
    }

}
