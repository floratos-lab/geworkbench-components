package org.geworkbench.util.session;

import org.geworkbench.util.remote.Connection;
import polgara.soapPD_wsdl.SoapPDPortType;

import java.rmi.RemoteException;


/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: This class is responsible for login/logout from a server.
 * Currently this is a class but may be extended to an interface in future.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version $Id$
 */
public class Logger {
    private SoapPDPortType port;

    //User Id is obtained from the server
    private int userId = 0;

    //user name as known on the server. We assume this is unique name on server.
    private String userName;

    //user password
    private char[] password;

    public Logger(Connection connection, String userName, char[] password) throws LoggerException {
            this.port = connection.getPort();

            try {
                login(userName, password);
            } catch (RemoteException exp) {
                 throw new LoggerException("Could not connect to the server.");
            }
    }

    /**
     * This method log a user to a server that is connect through
     * this connection.
     *
     * @param userName the name of user
     * @param password the password of the user
     * @return boolean true if the user was logged false otherwise
     * @throws RemoteException
     */
    private void login(String userName, char[] password) throws RemoteException {
        String pass = new String(password);
        int returnVal = port.login(userName, pass);

        if (returnVal > -1) {
            userId = returnVal;
            this.userName = userName;
            this.password = new char[password.length];
            System.arraycopy(password, 0, this.password, 0, password.length);
        }
    }

    public char[] getPassword() {
            char[] retPassword = new char[password.length];
            System.arraycopy(password, 0, retPassword, 0, password.length);

            return retPassword;
    }

    /**
     * This method returns the userId.
     *
     * @return userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * This method returns the userName.
     *
     * @return user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * This method logs out a user from the server that he/she is connected
     * throgh this logger.
     *
     * @throws RemoteException
     */
    public void logout() throws LoggerException {
            try {
                port.logout(userId);
            } catch (RemoteException e) {
                throw new LoggerException("Could not reach the server to logout");
            }
    }
}
