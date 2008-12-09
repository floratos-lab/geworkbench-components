/* $RCSfile: Util.java,v $
 * $Author: wangm $
 * $Date: 2008-12-09 17:00:32 $
 * $Revision: 1.1 $
 *
 * Copyright (C) 2003-2004  The Jmol Development Team
 *
 * Contact: jmol-developers@lists.sf.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307  USA.
 */
package org.jmol.viewer;
import java.lang.reflect.Array;

final class Util {

  static Object ensureLength(Object array, int minimumLength) {
    if (array != null && Array.getLength(array) >= minimumLength)
      return array;
    return setLength(array, minimumLength);
  }

  static String[] ensureLength(String[] array, int minimumLength) {
    if (array != null && array.length >= minimumLength)
      return array;
    return setLength(array, minimumLength);
  }

  static short[] ensureLength(short[] array, int minimumLength) {
    if (array != null && array.length >= minimumLength)
      return array;
    return setLength(array, minimumLength);
  }

  static byte[] ensureLength(byte[] array, int minimumLength) {
    if (array != null && array.length >= minimumLength)
      return array;
    return setLength(array, minimumLength);
  }

  static Object doubleLength(Object array) {
    return setLength(array, 2 * Array.getLength(array));
  }

  static String[] doubleLength(String[] array) {
    return setLength(array, 2 * array.length);
  }

  static float[] doubleLength(float[] array) {
    return setLength(array, (array == null ? 16 : 2 * array.length));
  }

  static int[] doubleLength(int[] array) {
    return setLength(array, (array == null ? 16 : 2 * array.length));
  }
  
  static short[] doubleLength(short[] array) {
    return setLength(array, (array == null ? 16 : 2 * array.length));
  }

  static byte[] doubleLength(byte[] array) {
    return setLength(array, (array == null ? 16 : 2 * array.length));
  }

  static Object setLength(Object array, int newLength) {
    Object t =
      Array.newInstance(array.getClass().getComponentType(), newLength);
    int oldLength = Array.getLength(array);
    System.arraycopy(array, 0, t, 0,
                     oldLength < newLength ? oldLength : newLength);
    return t;
  }

  static String[] setLength(String[] array, int newLength) {
    String[] t = new String[newLength];
    if (array != null) {
      int oldLength = array.length;
      System.arraycopy(array, 0, t, 0, 
                     oldLength < newLength ? oldLength : newLength);
    }
    return t;
  }
  
  static float[] setLength(float[] array, int newLength) {
    float[] t = new float[newLength];
    if (array != null) {
      int oldLength = array.length;
      System.arraycopy(array, 0, t, 0, 
                       oldLength < newLength ? oldLength : newLength);
    }
    return t;
  }
  
  static int[] setLength(int[] array, int newLength) {
    int[] t = new int[newLength];
    if (array != null) {
      int oldLength = array.length;
      System.arraycopy(array, 0, t, 0, 
                       oldLength < newLength ? oldLength : newLength);
    }
    return t;
  }
  
  static short[] setLength(short[] array, int newLength) {
    short[] t = new short[newLength];
    if (array != null) {
      int oldLength = array.length;
      System.arraycopy(array, 0, t, 0, 
                       oldLength < newLength ? oldLength : newLength);
    }
    return t;
  }

  static byte[] setLength(byte[] array, int newLength) {
    byte[] t = new byte[newLength];
    if (array != null) {
      int oldLength = array.length;
      System.arraycopy(array, 0, t, 0, 
                       oldLength < newLength ? oldLength : newLength);
    }
    return t;
  }
}
