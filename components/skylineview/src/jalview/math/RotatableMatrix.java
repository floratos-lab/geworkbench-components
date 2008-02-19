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
package jalview.math;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class RotatableMatrix
{
  float[][] matrix;
  float[] temp;
  float[][] rot;

  /**
   * Creates a new RotatableMatrix object.
   *
   * @param rows DOCUMENT ME!
   * @param cols DOCUMENT ME!
   */
  public RotatableMatrix(int rows, int cols)
  {
    matrix = new float[rows][cols];

    temp = new float[3];

    rot = new float[3][3];
  }

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   * @param j DOCUMENT ME!
   * @param value DOCUMENT ME!
   */
  public void addElement(int i, int j, float value)
  {
    matrix[i][j] = value;
  }

  /**
   * DOCUMENT ME!
   */
  public void print()
  {
    System.out.println(matrix[0][0] + " " + matrix[0][1] + " " +
                       matrix[0][2]);

    System.out.println(matrix[1][0] + " " + matrix[1][1] + " " +
                       matrix[1][2]);

    System.out.println(matrix[2][0] + " " + matrix[2][1] + " " +
                       matrix[2][2]);
  }

  /**
   * DOCUMENT ME!
   *
   * @param degrees DOCUMENT ME!
   * @param axis DOCUMENT ME!
   */
  public void rotate(float degrees, char axis)
  {
    float costheta = (float) Math.cos( (degrees * Math.PI) / (float) 180.0);

    float sintheta = (float) Math.sin( (degrees * Math.PI) / (float) 180.0);

    if (axis == 'z')
    {
      rot[0][0] = (float) costheta;

      rot[0][1] = (float) - sintheta;

      rot[0][2] = (float) 0.0;

      rot[1][0] = (float) sintheta;

      rot[1][1] = (float) costheta;

      rot[1][2] = (float) 0.0;

      rot[2][0] = (float) 0.0;

      rot[2][1] = (float) 0.0;

      rot[2][2] = (float) 1.0;

      preMultiply(rot);
    }

    if (axis == 'x')
    {
      rot[0][0] = (float) 1.0;

      rot[0][1] = (float) 0.0;

      rot[0][2] = (float) 0.0;

      rot[1][0] = (float) 0.0;

      rot[1][1] = (float) costheta;

      rot[1][2] = (float) sintheta;

      rot[2][0] = (float) 0.0;

      rot[2][1] = (float) - sintheta;

      rot[2][2] = (float) costheta;

      preMultiply(rot);
    }

    if (axis == 'y')
    {
      rot[0][0] = (float) costheta;

      rot[0][1] = (float) 0.0;

      rot[0][2] = (float) - sintheta;

      rot[1][0] = (float) 0.0;

      rot[1][1] = (float) 1.0;

      rot[1][2] = (float) 0.0;

      rot[2][0] = (float) sintheta;

      rot[2][1] = (float) 0.0;

      rot[2][2] = (float) costheta;

      preMultiply(rot);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param vect DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public float[] vectorMultiply(float[] vect)
  {
    temp[0] = vect[0];

    temp[1] = vect[1];

    temp[2] = vect[2];

    for (int i = 0; i < 3; i++)
    {
      temp[i] = (matrix[i][0] * vect[0]) + (matrix[i][1] * vect[1]) +
          (matrix[i][2] * vect[2]);
    }

    vect[0] = temp[0];

    vect[1] = temp[1];

    vect[2] = temp[2];

    return vect;
  }

  /**
   * DOCUMENT ME!
   *
   * @param mat DOCUMENT ME!
   */
  public void preMultiply(float[][] mat)
  {
    float[][] tmp = new float[3][3];

    for (int i = 0; i < 3; i++)
    {
      for (int j = 0; j < 3; j++)
      {
        tmp[i][j] = (mat[i][0] * matrix[0][j]) +
            (mat[i][1] * matrix[1][j]) + (mat[i][2] * matrix[2][j]);
      }
    }

    for (int i = 0; i < 3; i++)
    {
      for (int j = 0; j < 3; j++)
      {
        matrix[i][j] = tmp[i][j];
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param mat DOCUMENT ME!
   */
  public void postMultiply(float[][] mat)
  {
    float[][] tmp = new float[3][3];

    for (int i = 0; i < 3; i++)
    {
      for (int j = 0; j < 3; j++)
      {
        tmp[i][j] = (matrix[i][0] * mat[0][j]) +
            (matrix[i][1] * mat[1][j]) + (matrix[i][2] * mat[2][j]);
      }
    }

    for (int i = 0; i < 3; i++)
    {
      for (int j = 0; j < 3; j++)
      {
        matrix[i][j] = tmp[i][j];
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param args DOCUMENT ME!
   */
  public static void main(String[] args)
  {
    RotatableMatrix m = new RotatableMatrix(3, 3);

    m.addElement(0, 0, 1);

    m.addElement(0, 1, 0);

    m.addElement(0, 2, 0);

    m.addElement(1, 0, 0);

    m.addElement(1, 1, 2);

    m.addElement(1, 2, 0);

    m.addElement(2, 0, 0);

    m.addElement(2, 1, 0);

    m.addElement(2, 2, 1);

    m.print();

    RotatableMatrix n = new RotatableMatrix(3, 3);

    n.addElement(0, 0, 2);

    n.addElement(0, 1, 1);

    n.addElement(0, 2, 1);

    n.addElement(1, 0, 2);

    n.addElement(1, 1, 1);

    n.addElement(1, 2, 1);

    n.addElement(2, 0, 2);

    n.addElement(2, 1, 1);

    n.addElement(2, 2, 1);

    n.print();

    //m.postMultiply(n.matrix);
    //m.print();
    //     m.rotate(45,'z',new RotatableMatrix(3,3));
    float[] vect = new float[3];

    vect[0] = 2;

    vect[1] = 4;

    vect[2] = 6;

    vect = m.vectorMultiply(vect);

    System.out.println(vect[0] + " " + vect[1] + " " + vect[2]);
  }

  /**
   * DOCUMENT ME!
   */
  public void setIdentity()
  {
    matrix[0][0] = (float) 1.0;

    matrix[1][1] = (float) 1.0;

    matrix[2][2] = (float) 1.0;

    matrix[0][1] = (float) 0.0;

    matrix[0][2] = (float) 0.0;

    matrix[1][0] = (float) 0.0;

    matrix[1][2] = (float) 0.0;

    matrix[2][0] = (float) 0.0;

    matrix[2][1] = (float) 0.0;
  }
}
