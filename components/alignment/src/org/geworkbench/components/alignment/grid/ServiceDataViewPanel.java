package org.geworkbench.components.alignment.grid;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */

import org.geworkbench.components.alignment.grid.service.ServiceInformation;
import org.geworkbench.components.alignment.grid.service.SystemInformation;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

public class ServiceDataViewPanel extends JPanel implements Serializable {
    JPasswordField security = new JPasswordField();
    JTextField lastResult = new JTextField();
    JTextField costInfo = new JTextField();
    JLabel costLabel = new JLabel();
    JTextField GSH = new JTextField();
    JLabel serverLabel = new JLabel();
    JLabel jLabel3 = new JLabel();
    JLabel passwordLabel = new JLabel();
    Border border1, border3;
    ServiceDataModel model;
    JComboBox serverTypeName = new JComboBox();
    JLabel jLabel4 = new JLabel();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    private SystemInformation systemInfomationBean;
    private ServiceInformation serviceInformationBean;

    public ServiceDataViewPanel(ServiceDataModel model) {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setModel(model);
        setSystemInfomationBean(model.getSystemInformation());

        // model.addServiceDataModelListener(this);
        // model.fireServiceDataModelChanged();
    }

    /** This method retuns the host name.
     * @return  host name
     */

    /**
     * Sets the model for this panel
     *
     * @param model the model
     */
    public void setModel(ServiceDataModel model) {
        if (model == null) {
            throw new IllegalArgumentException("Cannot set a null ServicePanelModel");
        }
        this.model = model;
        updateValues();

    }

    /**
     * updateValues
     */
    public void updateValues() {
        //      costInfo.setText(model.getSystemInformation().getCost());
        security.setText("no implemented yet");
        lastResult.setText("none");
    }

    /**
     * Returns the model for this login panel
     *
     * @return model
     */
    public ServiceDataModel getModel() {
        return model;
    }

    /* public void ServiceDataViewPanelChanged(ServiceDataModelEvent evt){
       ServiceDataModel lpm = (ServiceDataModel) evt.getSource();
       userName.setText(lpm.getUserName());
       portName.setText(lpm.getPort());

       showHostSet(lpm.getHostSet(), lpm.getHostName());
       showTypeSet(lpm.getHostTypeSet(), lpm.getCurrentType());
     }*/
    private void showTypeSet(Set host, String selected) {
        if (selected != null) {
            serverTypeName.addItem(selected);
        }
        if (host != null) {
            for (Iterator iter = host.iterator(); iter.hasNext();) {
                serverTypeName.addItem(iter.next());
            }
        }

    }

    private void showHostSet(Set host, String selected) {
        if (selected != null) {
            GSH.setText(selected);
        }

    }

    /**
     * The method writes the information of the panel to the model.
     **/

    /* public void write(){

       systemInfomationBean.setCurrentHostName((String)hostName.getSelectedItem());
       systemInfomationBean.setPort(portName.getText());
       systemInfomationBean.setUserName(userName.getText());
       systemInfomationBean.setPassword(password.getPassword());
       systemInfomationBean.setCurrentType((String)serverTypeName.getSelectedItem());
     }
   */
    /**
     * This method retuns the user name.
     *
     * @return user name
     */
    public String getCPU() {
        return systemInfomationBean.getCpu();
    }

    /** This method retuns the port number.
     * @return  port number
     */

    /**
     * This method retuns the password.
     *
     * @return password
     */

    public void setHostNames(Set hostSet, String first) {
        //getModel().setHostNames(hostSet, first);
    }


    public void setUserName(String name) {
        //getModel().setUserName(name);
    }

    public ServiceDataViewPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    public ServiceDataViewPanel(LayoutManager layout) {
        super(layout);
    }

    public ServiceDataViewPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    private void jbInit() throws Exception {
        border1 = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        border3 = BorderFactory.createCompoundBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(142, 142, 142)), "System Data"), BorderFactory.createEmptyBorder(2, 2, 2, 2));

        passwordLabel.setText("Security:");
        jLabel3.setText("Last Result:");
        serverLabel.setText("GSH:");
        GSH.setEditable(true);
        GSH.addActionListener(new ServiceDataViewPanel_hostName_actionAdapter(this));
        GSH.setPreferredSize(new Dimension(150, 21));
        GSH.setMinimumSize(new Dimension(150, 21));
        costLabel.setText("Cost:");
        costInfo.setText("");
        costInfo.setPreferredSize(new Dimension(150, 21));
        costInfo.setMinimumSize(new Dimension(150, 21));
        lastResult.setScrollOffset(0);
        lastResult.setPreferredSize(new Dimension(150, 21));
        lastResult.setMinimumSize(new Dimension(150, 21));
        security.setToolTipText("");
        security.setPreferredSize(new Dimension(150, 21));
        security.setMinimumSize(new Dimension(150, 21));
        security.setText("");
        this.setLayout(gridBagLayout1);
        this.setBackground(new Color(204, 204, 204));
        this.setBorder(border1);
        serverTypeName.setMinimumSize(new Dimension(150, 21));
        serverTypeName.setPreferredSize(new Dimension(150, 21));
        serverTypeName.addActionListener(new ServiceDataViewPanel_serverTypeName_actionAdapter(this));
        serverTypeName.setEditable(true);
        jLabel4.setText("Server Type:");
        this.add(security, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        this.add(lastResult, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        this.add(costInfo, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        this.add(costLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
        this.add(GSH, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        this.add(serverLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
        this.add(jLabel3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
        this.add(passwordLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
        this.add(serverTypeName, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        this.add(jLabel4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 3, 0));
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
    }

    void hostName_actionPerformed(ActionEvent e) {

    }

    public org.geworkbench.components.alignment.grid.service.SystemInformation getSystemInfomationBean() {
        return systemInfomationBean;
    }

    public void setSystemInfomationBean(org.geworkbench.components.alignment.grid.service.SystemInformation systemInfomationBean) {
        this.systemInfomationBean = systemInfomationBean;
    }

    public org.geworkbench.components.alignment.grid.service.ServiceInformation getServiceInformationBean() {
        update();
        return serviceInformationBean;

    }

    public void update() {
        // costInfo.setText(serviceInformationBean.get);
    }

    public void setServiceInformationBean(org.geworkbench.components.alignment.grid.service.ServiceInformation serviceInformationBean) {
        this.serviceInformationBean = serviceInformationBean;
    }
}

class ServiceDataViewPanel_hostName_actionAdapter implements java.awt.event.ActionListener {
    ServiceDataViewPanel adaptee;

    ServiceDataViewPanel_hostName_actionAdapter(ServiceDataViewPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.hostName_actionPerformed(e);
    }
}

class ServiceDataViewPanel_serverTypeName_actionAdapter implements java.awt.event.ActionListener {
    ServiceDataViewPanel adaptee;

    ServiceDataViewPanel_serverTypeName_actionAdapter(ServiceDataViewPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.hostName_actionPerformed(e);
    }
}


