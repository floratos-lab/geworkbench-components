package org.geworkbench.components.alignment.panels;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */


import org.globus.ogsa.impl.core.handle.HandleHelper;
import org.globus.ogsa.types.properties.PropertiesDetailType;
import org.globus.ogsa.utils.AnyHelper;
import org.gridforum.ogsi.EntryType;
import org.gridforum.ogsi.HandleType;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class BlastListTableModel extends AbstractTableModel {
    private String[] columns = {"Name", "Handle", "State"};
    private List list;
    private String[] selectionCrit = {"blast", "Blast", "BLAST"};

    public BlastListTableModel() {
        super();
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setList(Object[] entries) {
        List newList = new ArrayList();

        for (int i = 0; i < entries.length; i++) {
            try {
                EntryType entry = (EntryType) entries[i];
                if (AnyHelper.contains(entry.getContent(), PropertiesDetailType.class)) {
                    newList.add(AnyHelper.getAsSingleObject(entry.getContent(), PropertiesDetailType.class));
                    // System.out.println("1st: " + ((PropertiesDetailType)entry).getLabel() );
                    // System.out.println("1st: " + entry.getContent());
                } else {
                    newList.add(entry.getMemberServiceLocator().getHandle()[0]);
                    // System.out.println("2nd: " + entry.getContent());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.list = new ArrayList();
        for (int i = 0; i < newList.size(); i++) {
            Object obj = newList.get(i);
            if (obj instanceof PropertiesDetailType) {
                PropertiesDetailType entry = (PropertiesDetailType) obj;
                String name = entry.getName();
                //     System.out.println(name + name.indexOf("Blast"));
                if (name.indexOf("Blast") != -1 || name.indexOf("BLAST") != -1) {
                    this.list.add(obj);
                    //System.out.println("match Blast.");
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

    public String getLocation(int row) {
        return (String) getValueAt(row, 1);
    }

    public boolean isCellEditable(int row, int col) {
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

    public Object getValueAt(int row, int column) {
        Object obj = this.list.get(row);
        if (obj instanceof PropertiesDetailType) {
            PropertiesDetailType entry = (PropertiesDetailType) obj;
            switch (column) {
                case 0:
                    return entry.getName();

                case 1:
                    return entry.getHandle();

                case 2:
                    return "" + entry.getState();
            }
        } else if (obj instanceof HandleType) {
            switch (column) {
                case 0:
                    try {
                        return HandleHelper.getInstanceID((HandleType) obj);
                    } catch (Exception e) {
                        return "";
                    }
                case 1:
                    return obj.toString();
            }
        }
        return "";
    }

    private void jbInit() throws Exception {
    }

    public void setSelectionCrit(String[] selectionCrit) {
        this.selectionCrit = selectionCrit;
    }
}
