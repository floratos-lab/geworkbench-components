package org.geworkbench.components.idea;

/**
 * Fisher Exact Test
 * 
 * adapted from code originally by Ed Buckler from
 * http://www.koders.com/java/fid868948AD5196B75C4C39FEA15A0D6EAF34920B55
 * .aspx?s=252
 * 
 * This does a Fisher Exact test. The Fisher's Exact test procedure calculates
 * an exact probability value for the relationship between two dichotomous
 * variables, as found in a two by two crosstable. The program calculates the
 * difference between the data observed and the data expected, considering the
 * given marginal and the assumptions of the model of independence. It works in
 * exactly the same way as the Chi-square test for independence; however, the
 * Chi-square gives only an estimate of the true probability value, an estimate
 * which might not be very accurate if the marginal is very uneven or if there
 * is a small value (less than five) in one of the cells.
 * 
 * @version $Id$
 */

public class FisherExact {
	private double[] f;
	private int maxSize;

	/**
	 * constructor for FisherExact table
	 * 
	 * @param maxSize
	 *            is the maximum sum that will be encountered by the table
	 *            (a+b+c+d)
	 */
	public FisherExact(int maxSize) {
		this.maxSize = maxSize;
		f = new double[maxSize + 1];
		f[0] = 0.0;
		for (int i = 1; i <= this.maxSize; i++) {
			f[i] = f[i - 1] + Math.log(i);
		}
	}

	/**
	 * calculates the P-value for this specific state
	 * 
	 * @param a
	 *            a, b, c, d are the four cells in a 2x2 matrix
	 * @param b
	 * @param c
	 * @param d
	 * @return the P-value
	 */
	private final double getP(int a, int b, int c, int d) {
		int n = a + b + c + d;
		if (n > maxSize) {
			return Double.NaN;
		}
		double p;
		p = (f[a + b] + f[c + d] + f[a + c] + f[b + d])
				- (f[a] + f[b] + f[c] + f[d] + f[n]);
		return Math.exp(p);
	}

	/**
	 * Calculates the one-tail P-value for the Fisher Exact test. Determines
	 * whether to calculate the right- or left- tail, thereby always returning
	 * the smallest p-value.
	 * 
	 * @param a
	 *            a, b, c, d are the four cells in a 2x2 matrix
	 * @param b
	 * @param c
	 * @param d
	 * @return one-tailed P-value (right or left, whichever is smallest)
	 */
	public final double getCumlativeP(int a, int b, int c, int d) {
		int n = a + b + c + d;
		if (n > maxSize) {
			return Double.NaN;
		}
		double p = getP(a, b, c, d);

		if ((a * d) >= (b * c)) {
			int min = (c < b) ? c : b;
			for (int i = 0; i < min; i++) {
				p += getP(++a, --b, --c, ++d);
			}
		} else { // if ((a * d) < (b * c))
			int min = (a < d) ? a : d;
			for (int i = 0; i < min; i++) {
				p += getP(--a, ++b, ++c, --d);
			}
		}
		return p;
	}

}