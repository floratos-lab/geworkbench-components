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
package jalview.analysis;

import java.io.*;

import jalview.datamodel.*;
import jalview.math.*;

/**
 * Performs Principal Component Analysis on given sequences
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class PCA
    implements Runnable
{
  Matrix m;
  Matrix symm;
  Matrix m2;
  double[] eigenvalue;
  Matrix eigenvector;
  StringBuffer details = new StringBuffer();

  /**
   * Creates a new PCA object.
   *
   * @param s Set of sequences to perform PCA on
   */
  public PCA(String[] s)
  {

    BinarySequence[] bs = new BinarySequence[s.length];
    int ii = 0;

    while ( (ii < s.length) && (s[ii] != null))
    {
      bs[ii] = new BinarySequence(s[ii]);
      bs[ii].encode();
      ii++;
    }

    BinarySequence[] bs2 = new BinarySequence[s.length];
    ii = 0;

    while ( (ii < s.length) && (s[ii] != null))
    {
      bs2[ii] = new BinarySequence(s[ii]);
      bs2[ii].blosumEncode();
      ii++;
    }

    //System.out.println("Created binary encoding");
    //printMemory(rt);
    int count = 0;

    while ( (count < bs.length) && (bs[count] != null))
    {
      count++;
    }

    double[][] seqmat = new double[count][bs[0].getDBinary().length];
    double[][] seqmat2 = new double[count][bs2[0].getDBinary().length];
    int i = 0;

    while (i < count)
    {
      seqmat[i] = bs[i].getDBinary();
      seqmat2[i] = bs2[i].getDBinary();
      i++;
    }

    //System.out.println("Created array");
    //printMemory(rt);
    //    System.out.println(" --- Original matrix ---- ");
    m = new Matrix(seqmat, count, bs[0].getDBinary().length);
    m2 = new Matrix(seqmat2, count, bs2[0].getDBinary().length);

  }

  /**
   * Returns the matrix used in PCA calculation
   *
   * @return java.math.Matrix object
   */

  public Matrix getM()
  {
    return m;
  }

  /**
   * Returns Eigenvalue
   *
   * @param i Index of diagonal within matrix
   *
   * @return Returns value of diagonal from matrix
   */
  public double getEigenvalue(int i)
  {
    return eigenvector.d[i];
  }

  /**
   * DOCUMENT ME!
   *
   * @param l DOCUMENT ME!
   * @param n DOCUMENT ME!
   * @param mm DOCUMENT ME!
   * @param factor DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public float[][] getComponents(int l, int n, int mm, float factor)
  {
    float[][] out = new float[m.rows][3];

    for (int i = 0; i < m.rows; i++)
    {
      out[i][0] = (float) component(i, l) * factor;
      out[i][1] = (float) component(i, n) * factor;
      out[i][2] = (float) component(i, mm) * factor;
    }

    return out;
  }

  /**
   * DOCUMENT ME!
   *
   * @param n DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public double[] component(int n)
  {
    // n = index of eigenvector
    double[] out = new double[m.rows];

    for (int i = 0; i < m.rows; i++)
    {
      out[i] = component(i, n);
    }

    return out;
  }

  /**
   * DOCUMENT ME!
   *
   * @param row DOCUMENT ME!
   * @param n DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  double component(int row, int n)
  {
    double out = 0.0;

    for (int i = 0; i < symm.cols; i++)
    {
      out += (symm.value[row][i] * eigenvector.value[i][n]);
    }

    return out / eigenvector.d[n];
  }

  public String getDetails()
  {
    return details.toString();
  }

  /**
   * DOCUMENT ME!
   */
  public void run()
  {
    Matrix mt = m.transpose();

    details.append(" --- OrigT * Orig ---- \n");
    eigenvector = mt.preMultiply(m2);

    PrintStream ps = new PrintStream(System.out)
    {
      public void print(String x)
      {
        details.append(x);
      }

      public void println()
      {
        details.append("\n");
      }
    };

    eigenvector.print(ps);

    symm = eigenvector.copy();

    eigenvector.tred();

    details.append(" ---Tridiag transform matrix ---\n");
    details.append(" --- D vector ---\n");
    eigenvector.printD(ps);
    ps.println();
    details.append("--- E vector ---\n");
    eigenvector.printE(ps);
    ps.println();

    // Now produce the diagonalization matrix
    eigenvector.tqli();

    details.append(" --- New diagonalization matrix ---\n");
    details.append(" --- Eigenvalues ---\n");
    eigenvector.printD(ps);
    ps.println();
    //  taps.println();
    //  taps.println("Transformed sequences = ");
    // Matrix trans =  m.preMultiply(eigenvector);
    //  trans.print(System.out);
  }
}
