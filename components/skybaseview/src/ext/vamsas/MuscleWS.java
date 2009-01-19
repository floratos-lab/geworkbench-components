/**
 * MuscleWS.java
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
package ext.vamsas;

public interface MuscleWS
    extends java.rmi.Remote
{
  public vamsas.objects.simple.WsJobId align(
      vamsas.objects.simple.SequenceSet seqSet)
      throws java.rmi.RemoteException;

  public vamsas.objects.simple.Alignment getalign(java.lang.String job_id)
      throws java.rmi.RemoteException;

  public vamsas.objects.simple.MsaResult getResult(java.lang.String job_id)
      throws java.rmi.RemoteException;

  public vamsas.objects.simple.WsJobId cancel(java.lang.String jobId)
      throws java.rmi.RemoteException;
}
