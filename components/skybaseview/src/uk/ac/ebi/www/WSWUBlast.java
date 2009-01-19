/**
 * WSWUBlast.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package uk.ac.ebi.www;

public interface WSWUBlast
    extends java.rmi.Remote
{
  public java.lang.String runWUBlast(uk.ac.ebi.www.InputParams params,
                                     uk.ac.ebi.www.Data[] content)
      throws java.rmi.RemoteException;

  public java.lang.String checkStatus(java.lang.String jobid)
      throws java.rmi.RemoteException;

  public byte[] poll(java.lang.String jobid, java.lang.String type)
      throws java.rmi.RemoteException;

  public uk.ac.ebi.www.WSFile[] getResults(java.lang.String jobid)
      throws java.rmi.RemoteException;

  public byte[] test(java.lang.String jobid, java.lang.String type)
      throws java.rmi.RemoteException;

  public byte[] polljob(java.lang.String jobid, java.lang.String outformat)
      throws java.rmi.RemoteException;

  public byte[] doWUBlast(uk.ac.ebi.www.InputParams params, byte[] content)
      throws java.rmi.RemoteException;
}
