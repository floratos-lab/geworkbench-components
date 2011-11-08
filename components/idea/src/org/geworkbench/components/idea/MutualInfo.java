package org.geworkbench.components.idea;

import org.apache.commons.math.MathException;
import org.apache.commons.math.special.Erf;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

/**
 * Mutual Information calculation
 * 
 * @author zm2165
 * @version $Id$
 * 
 */
public class MutualInfo {

	private static final double MINDOUBLE = 0.000000001;
	private static final double SQRT2 = Math.sqrt(2.0);

	private static Map<Integer, MutualInfo> cachedInstance = new Hashtable<Integer, MutualInfo>();

	private int n;
	private double[] normTable1D = null;
	private double[][] normTable2D = null;
	private double TWO_H_H = 0;
	private double MUTUAL_A=0.364119;
	private double MUTUAL_B=-0.151931;

	private MutualInfo(int n) throws MathException {
		this.n = n;
		normTable1D = new double[n];
		normTable2D = new double[n][n];

		double a = MUTUAL_A;
		double b = MUTUAL_B;
		double h = a * Math.pow(n, b);
		TWO_H_H = 2. * h * h;

		for (int i = 0; i < n; i++) {
			double k = (double) i / (n - 1);
			normTable1D[i] = 0.5 * (Erf.erf((1 - k) / (h * SQRT2)) - Erf
					.erf((-1) * k / (h * SQRT2)));
		}
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				normTable2D[i][j] = normTable1D[i] * normTable1D[j];
			}
		}
	}

	public static MutualInfo getInstance(int n) throws MathException {
		MutualInfo instance = cachedInstance.get(n);
		if (instance != null)
			return instance;
		else {
			MutualInfo newInstance = new MutualInfo(n);
			cachedInstance.put(n, newInstance);
			return newInstance;
		}
	}

	public double cacuMutualInfo(double[] x, double[] y) {

		double[][] probTable = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				probTable[i][j] = -1.0;
			}
		}

		double[] xCopy = new double[n];
		double[] yCopy = new double[n];
		int[] repeat = new int[n];
		for (int i = 0; i < n; i++) {
			xCopy[i] = x[i];
			yCopy[i] = y[i];
			repeat[i] = 0;
		}

		Arrays.sort(xCopy);
		Arrays.sort(yCopy);

		int[] rt_x = new int[n];
		int[] rt_y = new int[n];
		// process rt_x, regular binarySearch may return the same int when value
		// repeats
		// which is the reason I don't use it.
		for (int i = 0; i < n; i++) {
			boolean aFlag = false;
			for (int j = 0; j < n; j++) {
				if ((x[i] - xCopy[j]) < MINDOUBLE) {
					if (!aFlag) {
						rt_x[i] = j + repeat[j];
						repeat[j]++;
						aFlag = true;
					}

				}
			}
		}

		for (int i = 0; i < n; i++)
			repeat[i] = 0;

		for (int i = 0; i < n; i++) {// process rt_y
			boolean aFlag = false;
			for (int j = 0; j < n; j++) {
				if ((y[i] - yCopy[j]) < MINDOUBLE) {
					if (!aFlag) {
						rt_y[i] = j + repeat[j];
						repeat[j]++;
						aFlag = true;
					}

				}
			}
		}

		double[] xx = new double[n];
		double[] yy = new double[n];
		for (int i = 0; i < n; i++) {
			xx[i] = ((double) rt_x[i]) / (n - 1);// rt_x[i]-1)/(n-1) in matlab
			yy[i] = ((double) rt_y[i]) / (n - 1);
		}

		double ss = 0;
		for (int i = 0; i < n; i++) {
			double fxy = 0;
			double fx = 0;
			double fy = 0;
			for (int j = 0; j < n; j++) {
				int ix = Math.abs(rt_x[i] - rt_x[j]);
				int iy = Math.abs(rt_y[i] - rt_y[j]);
				double dx = xx[i] - xx[j];
				double dy = yy[i] - yy[j];

				if (Math.abs(probTable[ix][iy] + 1.0) < MINDOUBLE) {
					if (Math.abs(probTable[ix][0] + 1.0) < MINDOUBLE) {
						probTable[ix][0] = Math.exp(-(dx * dx) / TWO_H_H);
						probTable[0][ix] = probTable[ix][0];
					}
					if (Math.abs(probTable[0][iy] + 1.0) < MINDOUBLE) {
						probTable[0][iy] = Math.exp(-(dy * dy) / TWO_H_H);
						probTable[iy][0] = probTable[0][iy];
					}
					probTable[ix][iy] = Math
							.exp(-(dx * dx + dy * dy) / TWO_H_H);
					probTable[iy][ix] = probTable[ix][iy];
				}
				fx += probTable[ix][0] / normTable1D[rt_x[j]];
				fy += probTable[0][iy] / normTable1D[rt_y[j]];
				fxy += probTable[ix][iy] / normTable2D[rt_x[j]][rt_y[j]];
			}// inner for
			ss += Math.log(n * fxy / (fx * fy));
		}// outer for
		return Math.max(ss / n, 0);
	}

}
