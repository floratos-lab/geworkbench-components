/**
 * JpredService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package ext.vamsas;

public interface JpredService
    extends javax.xml.rpc.Service
{
  public java.lang.String getjpredAddress();

  public ext.vamsas.Jpred getjpred()
      throws javax.xml.rpc.ServiceException;

  public ext.vamsas.Jpred getjpred(java.net.URL portAddress)
      throws javax.xml.rpc.ServiceException;
}
