/**
 * Alignment.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

/*
 * Jalview - A Sequence Alignment Editor and Viewer
 * Copyright (C) 2007 AM Waterhouse, J Procter, G Barton, M Clamp, S Searle
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package vamsas.objects.simple;

public class Alignment
    extends vamsas.objects.simple.Object implements java.io.Serializable
{
  private java.lang.String gapchar;
  private java.lang.String[] method;
  private vamsas.objects.simple.SequenceSet seqs;
  private java.lang.Object __equalsCalc = null;
  private boolean __hashCodeCalc = false;

  public Alignment()
  {
  }

  public Alignment(java.lang.String gapchar, java.lang.String[] method,
                   vamsas.objects.simple.SequenceSet seqs)
  {
    this.gapchar = gapchar;
    this.method = method;
    this.seqs = seqs;
  }

  /**
   * Gets the gapchar value for this Alignment.
   *
   * @return gapchar
   */
  public java.lang.String getGapchar()
  {
    return gapchar;
  }

  /**
   * Sets the gapchar value for this Alignment.
   *
   * @param gapchar
   */
  public void setGapchar(java.lang.String gapchar)
  {
    this.gapchar = gapchar;
  }

  /**
   * Gets the method value for this Alignment.
   *
   * @return method
   */
  public java.lang.String[] getMethod()
  {
    return method;
  }

  /**
   * Sets the method value for this Alignment.
   *
   * @param method
   */
  public void setMethod(java.lang.String[] method)
  {
    this.method = method;
  }

  /**
   * Gets the seqs value for this Alignment.
   *
   * @return seqs
   */
  public vamsas.objects.simple.SequenceSet getSeqs()
  {
    return seqs;
  }

  /**
   * Sets the seqs value for this Alignment.
   *
   * @param seqs
   */
  public void setSeqs(vamsas.objects.simple.SequenceSet seqs)
  {
    this.seqs = seqs;
  }

  public synchronized boolean equals(java.lang.Object obj)
  {
    if (! (obj instanceof Alignment))
    {
      return false;
    }

    Alignment other = (Alignment) obj;

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
    _equals = super.equals(obj) &&
        ( ( (this.gapchar == null) && (other.getGapchar() == null)) ||
         ( (this.gapchar != null) && this.gapchar.equals(other.getGapchar()))) &&
        ( ( (this.method == null) && (other.getMethod() == null)) ||
         ( (this.method != null) &&
          java.util.Arrays.equals(this.method, other.getMethod()))) &&
        ( ( (this.seqs == null) && (other.getSeqs() == null)) ||
         ( (this.seqs != null) && this.seqs.equals(other.getSeqs())));
    __equalsCalc = null;

    return _equals;
  }

  public synchronized int hashCode()
  {
    if (__hashCodeCalc)
    {
      return 0;
    }

    __hashCodeCalc = true;

    int _hashCode = super.hashCode();

    if (getGapchar() != null)
    {
      _hashCode += getGapchar().hashCode();
    }

    if (getMethod() != null)
    {
      for (int i = 0; i < java.lang.reflect.Array.getLength(getMethod());
           i++)
      {
        java.lang.Object obj = java.lang.reflect.Array.get(getMethod(),
            i);

        if ( (obj != null) && !obj.getClass().isArray())
        {
          _hashCode += obj.hashCode();
        }
      }
    }

    if (getSeqs() != null)
    {
      _hashCode += getSeqs().hashCode();
    }

    __hashCodeCalc = false;

    return _hashCode;
  }
}
