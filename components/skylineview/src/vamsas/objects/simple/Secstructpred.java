/**
 * Secstructpred.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package vamsas.objects.simple;

public class Secstructpred
    implements java.io.Serializable
{
  private java.lang.String output;

  public Secstructpred()
  {
  }

  public Secstructpred(
      java.lang.String output)
  {
    this.output = output;
  }

  /**
   * Gets the output value for this Secstructpred.
   *
   * @return output
   */
  public java.lang.String getOutput()
  {
    return output;
  }

  /**
   * Sets the output value for this Secstructpred.
   *
   * @param output
   */
  public void setOutput(java.lang.String output)
  {
    this.output = output;
  }

  private java.lang.Object __equalsCalc = null;
  public synchronized boolean equals(java.lang.Object obj)
  {
    if (! (obj instanceof Secstructpred))
    {
      return false;
    }
    Secstructpred other = (Secstructpred) obj;
    if (obj == null)
    {
      return false;
    }
    if (this == obj)
    {
      return true;
    }
    if (__equalsCalc != null)
    {
      return (__equalsCalc == obj);
    }
    __equalsCalc = obj;
    boolean _equals;
    _equals = true &&
        ( (this.output == null && other.getOutput() == null) ||
         (this.output != null &&
          this.output.equals(other.getOutput())));
    __equalsCalc = null;
    return _equals;
  }

  private boolean __hashCodeCalc = false;
  public synchronized int hashCode()
  {
    if (__hashCodeCalc)
    {
      return 0;
    }
    __hashCodeCalc = true;
    int _hashCode = 1;
    if (getOutput() != null)
    {
      _hashCode += getOutput().hashCode();
    }
    __hashCodeCalc = false;
    return _hashCode;
  }

}
