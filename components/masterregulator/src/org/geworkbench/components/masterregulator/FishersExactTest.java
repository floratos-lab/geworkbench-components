package org.geworkbench.components.masterregulator;

/*
 * Fisher's Exact Test
 * usually the formula is: P = factorial(a+b)*factorial(c+d)*factorial(a+c)*factorial(b+d)/factorial(a+b+c+d)*factorial(a)*factorial(b)*factorial(c)*factorial(d);
 * but when things touch "factorial", 200! = 7.8865 E 374, which can not be handled by type double
 * So, we use the version do the calculation in log/exp way. 
 */

/*
 * The code used to calculate a Fisher p-value comes originally from a
 * <a href="http://infofarm.affrc.go.jp/~kadasowa/fishertest.htm">JavaScript program</a>
 * by T. Kadosawa (kadosawa@niaes.affrc.go.jp).
 * Translate to java by David Hopwood (http://www.users.zetnet.co.uk/hopwood/tools/StatTests.java)
 * fisher's exact test part extracted by Yih-Shien Chiang
 */
public class FishersExactTest {

    public static void main(String[] args) throws Exception {

        int a=1;
        int b=15;
        int c=2;
        int d=306;
        System.out.println(getPValue(a, b, c, d));
    }

    public static double getPValue(int a, int b, int c, int d){
        int m=a+b+c+d;
    	double[] logFactorial = null;
        logFactorial = new double[m+1];
        logFactorial[0] = 0.0;
        for (int i = 1; i <= m; i++) {
            logFactorial[i] = logFactorial[i-1] + Math.log(i);
        }
        return fisher(a, b, c, d, logFactorial);
    }
    
    /** Calculate a p-value for Fisher's Exact Test. */
    private static double fisher(int a, int b, int c, int d, double[] logFactorial) {
        if (a * d > b * c) {
            a = a + b; b = a - b; a = a - b; 
            c = c + d; d = c - d; c = c - d;
        }
        if (a > d) { a = a + d; d = a - d; a = a - d; }
        if (b > c) { b = b + c; c = b - c; b = b - c; }

        int a_org = a;
        double p_sum = 0.0d;

        double p = fisherSub(a, b, c, d, logFactorial);
        double p_1 = p;

        while (a >= 0) {
            p_sum += p;
            if (a == 0) break;
            --a; ++b; ++c; --d;
            p = fisherSub(a, b, c, d, logFactorial);
        }

        a = b; b = 0; c = c - a; d = d + a;
        p = fisherSub(a, b, c, d, logFactorial);

        while (p < p_1) {
            if (a == a_org) break;
            p_sum += p;
            --a; ++b; ++c; --d;
            p = fisherSub(a, b, c, d, logFactorial);
        }
        return p_sum;
    }

    private static double fisherSub(int a, int b, int c, int d, double[] logFactorial) {
        return Math.exp(logFactorial[a + b] +
                        logFactorial[c + d] +
                        logFactorial[a + c] +
                        logFactorial[b + d] -
                        logFactorial[a + b + c + d] -
                        logFactorial[a] -
                        logFactorial[b] -
                        logFactorial[c] -
                        logFactorial[d]);
    }

}

