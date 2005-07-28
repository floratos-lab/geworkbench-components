package org.geworkbench.components.alignment.grid;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 1.0
 */

public class ServiceDataModel {
    private org.geworkbench.components.alignment.grid.service.SystemInformation systemInformation;
    private org.geworkbench.components.alignment.grid.service.ServiceInformation serviceSpcificData;

    public ServiceDataModel() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public org.geworkbench.components.alignment.grid.service.SystemInformation getSystemInformation() {
        return systemInformation;
    }

    public void setSystemInformation(org.geworkbench.components.alignment.grid.service.SystemInformation systemInformation) {
        this.systemInformation = systemInformation;
    }

    public org.geworkbench.components.alignment.grid.service.ServiceInformation getServiceSpcificData() {
        return serviceSpcificData;
    }

    public void setServiceSpcificData(org.geworkbench.components.alignment.grid.service.ServiceInformation serviceSpcificData) {
        this.serviceSpcificData = serviceSpcificData;
    }

    private void jbInit() throws Exception {
    }

}
