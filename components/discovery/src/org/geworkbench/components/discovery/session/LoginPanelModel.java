package org.geworkbench.components.discovery.session;

import java.util.Set;

import org.geworkbench.util.PropertiesMonitor;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: This class acts as the data model for the LoginPanel.
 * This class does not have ANY policy on the data with 1 exceptions:
 * 1) It stores a set of hosts.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version $Id$
 */
public class LoginPanelModel {
	
	public LoginPanelModel() {
        //parse initial Model data from the properties monitor
        PropertiesMonitor pm = org.geworkbench.util.PropertiesMonitor.getPropertiesMonitor();
        hostSet = pm.getHosts();
        currentHost = pm.getHostSelected();
        normalizeHost(hostSet, currentHost);
        normalizePort(pm.getPort());
        normalizeUserName(pm.getUserName());
        
        if(currentHost.equals("")) { // first time
        	currentHost = "splash.c2b2.columbia.edu";
        	port = "80";
        }
    }

    private String userName;
    private char[] password;
    private Set<String> hostSet;
    private String currentHost;
    private String port;

    /**
     * parse the host and host set
     *
     * @param hostSet
     * @param host
     */
    private void normalizeHost(Set<String> hostSet, String currentHost) {
        this.hostSet = hostSet;
        this.currentHost = currentHost;

        if (this.currentHost == null) {
            this.currentHost = "";
        }
        //the intersection of currentHost and hostSet should be empty
        if (this.hostSet != null) {
            if (this.currentHost != null) {
                hostSet.remove(this.currentHost);
            }
        }
    }

    private void normalizePort(String port) {
        this.port = port;
        if (this.port == null) {
            this.port = "";
        }
    }

    private void normalizeUserName(String userName) {
        this.userName = userName;
        if (this.userName == null) {
            try {
                this.userName = System.getProperties().getProperty("user.name");
            } catch (SecurityException exp) {
                //we are not allowed to read properties...
                this.userName = "";
            }
        }
    }

    public void setCurrentHostName(String host) {
        if (hostSet != null && currentHost != null) {
            hostSet.add(currentHost);
        }
        normalizeHost(hostSet, host);
    }

    /**
     * This method returns the host name.
     *
     * @return host name
     */
    public String getHostName() {
        return currentHost;
    }

    /**
     * Returns the host set.
     *
     * @return host set. May return null.
     */
    public Set<String> getHostSet() {
        return hostSet;
    }

    /**
     * This method returns the user name.
     *
     * @return user name
     */

    public String getUserName() {
        return userName;
    }

    /**
     * This method returns the port number.
     *
     * @return port number
     */
    public String getPort() {
        return port;
    }

    /**
     * This method returns the password.
     *
     * @return password
     */
    public char[] getPassword() {
        if (password == null) {
            return password;
        } else {
            char[] retPassword = new char[password.length];
            System.arraycopy(password, 0, retPassword, 0, password.length);
            return retPassword;
        }
    }

    /**
     * The method sets the password.
     *
     * @param hostSet
     * @param currentHost
     */
    public void setPassword(char[] newPassword) {
        if (newPassword == null) {
            this.password = null;
            return;
        }
        this.password = new char[newPassword.length];
        System.arraycopy(newPassword, 0, password, 0, newPassword.length);
    }

    /**
     * Set the host set.
     *
     * @param hostSet     the set of host
     * @param currentHost the default selected host
     */
    public void setHostNames(Set<String> hostSet, String currentHost) {
        normalizeHost(hostSet, currentHost);
    }

    public void setPort(String port) {
        normalizePort(port);
    }

    public void setUserName(String name) {
        normalizeUserName(name);
    }

}
