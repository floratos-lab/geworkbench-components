/**
 * WsJobId.java
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

public class WsJobId
    implements java.io.Serializable
{
  private java.lang.String jobId;
  private int status;
  private java.lang.Object __equalsCalc = null;
  private boolean __hashCodeCalc = false;

  public WsJobId()
  {
  }

  public WsJobId(java.lang.String jobId, int status)
  {
    this.jobId = jobId;
    this.status = status;
  }

  /**
   * Gets the jobId value for this WsJobId.
   *
   * @return jobId
   */
  public java.lang.String getJobId()
  {
    return jobId;
  }

  /**
   * Sets the jobId value for this WsJobId.
   *
   * @param jobId
   */
  public void setJobId(java.lang.String jobId)
  {
    this.jobId = jobId;
  }

  /**
   * Gets the status value for this WsJobId.
   *
   * @return status
   */
  public int getStatus()
  {
    return status;
  }

  /**
   * Sets the status value for this WsJobId.
   *
   * @param status
   */
  public void setStatus(int status)
  {
    this.status = status;
  }

  public synchronized boolean equals(java.lang.Object obj)
  {
    if (! (obj instanceof WsJobId))
    {
      return false;
    }

    WsJobId other = (WsJobId) obj;

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
        ( ( (this.jobId == null) && (other.getJobId() == null)) ||
         ( (this.jobId != null) && this.jobId.equals(other.getJobId()))) &&
        (this.status == other.getStatus());
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

    int _hashCode = 1;

    if (getJobId() != null)
    {
      _hashCode += getJobId().hashCode();
    }

    _hashCode += getStatus();
    __hashCodeCalc = false;

    return _hashCode;
  }
}
