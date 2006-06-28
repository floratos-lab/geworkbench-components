/**
 * INTERACTIONS.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package interactions;

public interface INTERACTIONS extends java.rmi.Remote {
    public java.lang.String getINTERACTIONTYPE() throws java.rmi.RemoteException;
    public void setINTERACTIONTYPE(java.lang.String in0) throws java.rmi.RemoteException;
    public java.math.BigDecimal getMSID2() throws java.rmi.RemoteException;
    public void setMSID2(java.math.BigDecimal in0) throws java.rmi.RemoteException;
    public java.lang.String getISREVERSIBLE() throws java.rmi.RemoteException;
    public void setISREVERSIBLE(java.lang.String in0) throws java.rmi.RemoteException;
    public java.math.BigDecimal getMSID1() throws java.rmi.RemoteException;
    public void setMSID1(java.math.BigDecimal in0) throws java.rmi.RemoteException;
    public java.lang.String getSOURCE() throws java.rmi.RemoteException;
    public void setSOURCE(java.lang.String in0) throws java.rmi.RemoteException;
    public java.lang.String getCONTROLTYPE() throws java.rmi.RemoteException;
    public void setCONTROLTYPE(java.lang.String in0) throws java.rmi.RemoteException;
    public java.lang.String getDIRECTION() throws java.rmi.RemoteException;
    public void setDIRECTION(java.lang.String in0) throws java.rmi.RemoteException;
    public double getCONFIDENCEVALUE() throws java.rmi.RemoteException;
    public void setCONFIDENCEVALUE(double in0) throws java.rmi.RemoteException;
    public java.lang.String getISMODULATED() throws java.rmi.RemoteException;
    public void setISMODULATED(java.lang.String in0) throws java.rmi.RemoteException;
    public java.math.BigDecimal getID() throws java.rmi.RemoteException;
    public void insert() throws java.rmi.RemoteException;
    public void retrieve() throws java.rmi.RemoteException;
    public void setID(java.math.BigDecimal in0) throws java.rmi.RemoteException;
    public java.lang.String getCHROMOSOME() throws java.rmi.RemoteException;
    public void setCHROMOSOME(java.lang.String in0) throws java.rmi.RemoteException;
    public java.math.BigDecimal getGENECOUNT() throws java.rmi.RemoteException;
    public java.lang.Object[] getFIRSTNEIGHBORS(java.math.BigDecimal in0, java.lang.String in1) throws java.rmi.RemoteException;
    public java.math.BigDecimal getINTERACTIONCOUNT(java.math.BigDecimal in0, java.lang.String in1) throws java.rmi.RemoteException;
    public java.math.BigDecimal getENTREZID() throws java.rmi.RemoteException;
    public void setENTREZID(java.math.BigDecimal in0) throws java.rmi.RemoteException;
    public java.math.BigDecimal getTAXONID() throws java.rmi.RemoteException;
    public void setTAXONID(java.math.BigDecimal in0) throws java.rmi.RemoteException;
    public java.lang.String getGENETYPE() throws java.rmi.RemoteException;
    public void setGENETYPE(java.lang.String in0) throws java.rmi.RemoteException;
    public java.lang.String getGENESYMBOL() throws java.rmi.RemoteException;
    public void setGENESYMBOL(java.lang.String in0) throws java.rmi.RemoteException;
    public java.lang.Object[] getGENEROW(java.math.BigDecimal in0) throws java.rmi.RemoteException;
    public java.lang.Object[] getENTREZTOGO() throws java.rmi.RemoteException;
    public java.lang.String getLOCUSTAG() throws java.rmi.RemoteException;
    public void setLOCUSTAG(java.lang.String in0) throws java.rmi.RemoteException;
    public java.lang.String getDESCRIPTION() throws java.rmi.RemoteException;
    public void setDESCRIPTION(java.lang.String in0) throws java.rmi.RemoteException;
}
