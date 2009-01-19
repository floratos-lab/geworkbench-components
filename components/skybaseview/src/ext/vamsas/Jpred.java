/**
 * Jpred.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package ext.vamsas;

public interface Jpred
    extends java.rmi.Remote
{
  public java.lang.String predict(vamsas.objects.simple.Sequence seq)
      throws java.rmi.RemoteException;

  public java.lang.String predictOnMsa(vamsas.objects.simple.Msfalignment msf)
      throws java.rmi.RemoteException;

  public vamsas.objects.simple.Secstructpred getpredict(java.lang.String job_id)
      throws java.rmi.RemoteException;

  public vamsas.objects.simple.JpredResult getresult(java.lang.String job_id)
      throws java.rmi.RemoteException;
}
