/**
 * Sequence.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package vamsas.objects.simple;

public class Sequence
    implements java.io.Serializable
{
  private java.lang.String id;
  private java.lang.String seq;

  public Sequence()
  {
  }

  public Sequence(
      java.lang.String id,
      java.lang.String seq)
  {
    this.id = id;
    this.seq = seq;
  }

  /**
   * Gets the id value for this Sequence.
   *
   * @return id
   */
  public java.lang.String getId()
  {
    return id;
  }

  /**
   * Sets the id value for this Sequence.
   *
   * @param id
   */
  public void setId(java.lang.String id)
  {
    this.id = id;
  }

  /**
   * Gets the seq value for this Sequence.
   *
   * @return seq
   */
  public java.lang.String getSeq()
  {
    return seq;
  }

  /**
   * Sets the seq value for this Sequence.
   *
   * @param seq
   */
  public void setSeq(java.lang.String seq)
  {
    this.seq = seq;
  }

  private java.lang.Object __equalsCalc = null;
  public synchronized boolean equals(java.lang.Object obj)
  {
    if (! (obj instanceof Sequence))
    {
      return false;
    }
    Sequence other = (Sequence) obj;
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
        ( (this.id == null && other.getId() == null) ||
         (this.id != null &&
          this.id.equals(other.getId()))) &&
        ( (this.seq == null && other.getSeq() == null) ||
         (this.seq != null &&
          this.seq.equals(other.getSeq())));
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
    if (getId() != null)
    {
      _hashCode += getId().hashCode();
    }
    if (getSeq() != null)
    {
      _hashCode += getSeq().hashCode();
    }
    __hashCodeCalc = false;
    return _hashCode;
  }

}
