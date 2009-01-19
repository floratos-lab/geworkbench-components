/**
 * WSWUBlastService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package uk.ac.ebi.www;

public interface WSWUBlastService
    extends javax.xml.rpc.Service
{
  public java.lang.String getWSWUBlastAddress();

  public uk.ac.ebi.www.WSWUBlast getWSWUBlast()
      throws javax.xml.rpc.ServiceException;

  public uk.ac.ebi.www.WSWUBlast getWSWUBlast(java.net.URL portAddress)
      throws javax.xml.rpc.ServiceException;
}
