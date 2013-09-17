package org.geworkbench.components.promoter;

import java.util.Iterator;

import org.biojava.bio.dist.SimpleDistribution;
import org.biojava.bio.gui.DNAStyle;
import org.biojava.bio.gui.DistributionLogo;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.utils.ChangeVetoException;

/**
 * 
 * @author zji
 * @version $Id$
 *
 */
public class Distribution {
    private DistributionLogo logo = null; // this is the display of the distribution.
    private char[] symbols = null;
    private double[] counts = null;
    private double totalCounts = 0;

    public Distribution(char[] symbolist) {
        symbols = symbolist;
        counts = new double[symbols.length];
        for (int i = 0; i < counts.length; i++) {
            counts[i] = 0;
        }
    }

    public Distribution() {
	}

	public Distribution copy ( ){
    	Distribution newDistribution = new Distribution();
    	newDistribution.symbols = this.symbols;
    	newDistribution.totalCounts = this.totalCounts;

    	newDistribution.counts = new double[this.counts.length];
        for (int i = 0; i < newDistribution.counts.length; i++) {
        	newDistribution.counts[i] = this.counts[i];
        }
    	
    	return newDistribution;
    }
    
    public void set(char sym, double value) {
        for (int i = 0; i < symbols.length; i++) {
            if (sym == symbols[i]) {
                counts[i] = value;
                break;
            }
        }
    }

    public double get(char sym) {
        for (int i = 0; i < symbols.length; i++) {
            if (sym == symbols[i]) {
                return counts[i];
            }
        }
        return 1E-4;
    }

    public void addCount(char sym) {
        for (int i = 0; i < symbols.length; i++) {
            if (sym == symbols[i]) {
                counts[i]++;
                break;
            }
        }
    }

    public void pseudoNormalize(boolean sqrtNSelected, double pseudocount) {
        getTotal();
        double b = 0;
        
        if (sqrtNSelected){
        	b = Math.sqrt(totalCounts);
        }else{
        	b = pseudocount;
        }
        
        for (int i = 0; i < symbols.length; i++) {
            counts[i] = (counts[i] + b / 4) / (totalCounts + b);
        }
    }

    public void normalize() {
        getTotal();

        for (int i = 0; i < symbols.length; i++) {
        	if (totalCounts ==0){
        		counts[i] = 0;
        	}
        	
            counts[i] = counts[i] / totalCounts;
            
        	if (counts[i] < Double.MIN_VALUE){
        		counts[i] = Double.MIN_VALUE;
        	}
        }
    }

    
    public double getMax() {
        double max = 0;
        if (counts != null) {
            for (int k = 0; k < counts.length; k++) {
                if (max < counts[k]) {
                    max = counts[k];
                }
            }
        }
        return max;
    }

    public double getTotal() {
        if (totalCounts == 0) {
            for (int i = 0; i < counts.length; i++) {
                totalCounts += counts[i];
            }
        }
        return totalCounts;

    }

    public DistributionLogo getLogo() {
        if (logo == null) {

            //make the Binary Alphabet
            FiniteAlphabet a1 = DNATools.getDNA();
            SimpleDistribution sp = new SimpleDistribution(a1);

            for (Iterator<?> it = a1.iterator(); it.hasNext();) {
                Symbol x = (Symbol) it.next();
                try {
                    char sb = x.getName().toUpperCase().charAt(0);
                    sp.setWeight(x, get(sb));
                } catch (ChangeVetoException ex1) {
                    ex1.printStackTrace();
                } catch (IllegalSymbolException ex2) {
                    ex2.printStackTrace();
                }
            }
            DistributionLogo dis = new DistributionLogo();
              dis.setStyle(new DNAStyle());
            try {
                dis.setDistribution(sp);
            } catch (IllegalAlphabetException ex) {
                ex.printStackTrace();
            }
            logo = dis;

        }

        return logo;

    }

}
