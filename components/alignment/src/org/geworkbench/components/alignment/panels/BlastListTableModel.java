package org.geworkbench.components.alignment.panels;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
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

public class BlastListTableModel
    extends AbstractTableModel {
    private String[] columns = {
        "JobName", "Status", "Start Time", "End Time", "Detail"};
    private List list;
    private String[] selectionCrit = {
        "Sequence", "session"};
    private String primarySelectionCrit = "Instance";
    private XMLTree entryTree; //=  new XMLTree(XmlFactory.newDocument());;
    private String DEFAULTURL = "http://gridgate.genomecenter.columbia.edu:18080/ogsa/services/core/registry/ContainerRegistryService";

    private URL defaultEndpoint;
    public BlastListTableModel() {
        super();

    }

    public BlastListTableModel(String[] selectedMarkers) {
        super();
        selectionCrit = selectedMarkers;
        //getRegistry();

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
                System.out.println("HERE" + name + name.indexOf("Sequence") +
                                   name.indexOf(primarySelectionCrit));
                for (int k = 0; k < length; k++) {
                    if (name.indexOf(primarySelectionCrit) != -1) {
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
                int k = selectionCrit.length;
                System.out.println("THERE" + name + name.indexOf("Sequence") +
                                   k);
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

    public String getPrimarySelectionCrit() {
        return primarySelectionCrit;
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
        try {

            Object obj = this.list.get(row);
            String termDate = "";
            String startDate = "";
            String finishedPercentage = "0";
            String detail = "";
            if (obj instanceof PropertiesDetailType) {
                PropertiesDetailType entry = (PropertiesDetailType) obj;
                String endPoint = ((PropertiesDetailType)obj).getHandle();
                endPoint = endPoint.replaceAll("127.0.0.1", "adgate.cu-genome.org");
                URL GSH = new java.net.URL(endPoint);

                // Get a reference to the GridService portType
                OGSIServiceGridLocator locator = new OGSIServiceGridLocator();
                GridService gridService = locator.getGridServicePort(GSH);

                // gridServiceHandle
                // GSH of this instance (can have more than one)
                ExtensibilityType extensibility = gridService.findServiceData(
                    QueryHelper.getNamesQuery("gridServiceHandle"));
                ServiceDataValuesType serviceData = AnyHelper.
                    getAsServiceDataValues(extensibility);

                // terminationTime
                // The termination time for this service
                extensibility = gridService.findServiceData(QueryHelper.
                    getNamesQuery("terminationTime"));
                serviceData = AnyHelper.getAsServiceDataValues(extensibility);
                TerminationTimeType terminationTime = (TerminationTimeType)
                    AnyHelper.getAsSingleObject(serviceData);
                ExtendedDateTimeType after, before;
                after = terminationTime.getAfter();

                termDate = after.getInfinityTypeValue().getValue().toString();
                startDate = terminationTime.getTimestamp().getTime().toString();

                //For sequence grid service specific SDE.
                SequenceAlignmentServiceGridLocator mathServiceLocator = new
                    SequenceAlignmentServiceGridLocator();
                SequenceAlignmentPortType math = mathServiceLocator.
                    getSequenceAlignmentServicePort(GSH); // getMathServicePort(GSH);

// Get Service Data Element "MathData"
                extensibility =
                    math.findServiceData(QueryHelper.getNamesQuery(
                        "SessionData"));
                serviceData = AnyHelper.getAsServiceDataValues(extensibility);
                SessionDataType mathData =
                    (SessionDataType) AnyHelper.getAsSingleObject(serviceData,
                    SessionDataType.class);
//get session information.
                finishedPercentage = new Integer(mathData.getValue()).toString();
                detail = mathData.getLastOp();

// Write service data



                String totalNumOps[] = {
                    "# of operations",
                    new Integer(mathData.getNumOps()).toString()};

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

                System.out.println("terminationTime (timestamp): " +
                                   terminationTime.getTimestamp().getTime());

                switch (column) {
                    case 0:
                        String s[] = entry.getHandle().split("/");
                        return s[s.length - 1];
                    case 1:
                        return finishedPercentage;
                    case 2:

                        return startDate;
                    case 3:
                        return termDate;

                    case 4:
                        return detail;
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void jbInit() throws Exception {
    }

    public void setSelectionCrit(String[] selectionCrit) {
        this.selectionCrit = selectionCrit;
    }

    public void setPrimarySelectionCrit(String primarySelectionCrit) {
        this.primarySelectionCrit = primarySelectionCrit;
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
