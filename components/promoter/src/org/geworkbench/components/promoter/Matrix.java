package org.geworkbench.components.promoter;

import java.util.Arrays;

import org.biojava.bio.gui.DistributionLogo;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;

/**
 * 
 * @author unattributable
 * @version $Id: Matrix.java,v 1.9 2009-09-24 14:21:40 tgarben Exp $
 */
public class Matrix {
    public Matrix() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public char[] symbols = null;
    public boolean normalized = false;
    public double random = 0;
    public Distribution[] countTable = new Distribution[1];
    private Distribution[] rawCountTable = new Distribution[1];
    private Distribution[] normalizedCountTable = null;
    private double [] smallSampleCorrection = null;
    
    public double getRandom() {
        return random;
    }

    public Distribution[] getRawCountTable() {
        return rawCountTable;
    }

    public Distribution[] getNormalizedCountTable() {
        return normalizedCountTable;
    }

    public char[] getSymbols() {
        return symbols;
    }

    public Matrix(char[] symbol) {
        symbols = symbol;
    }

    public int getLength() {
        return countTable.length;
    }

    public void initialize(boolean sqrtNSelected, double pseduocount) {
    	calcSmallSampleCorrections();
    	createRawCountTable();
    	createNormalizedCountTable();
    	pseudoNormalizeCountTable(sqrtNSelected, pseduocount);
    }

    public void refresh(boolean sqrtNSelected, double pseduocount) {
    	restoreCountTable();
    	pseudoNormalizeCountTable(sqrtNSelected, pseduocount);
    }
    
    public void createRawCountTable() {

    	rawCountTable = new Distribution[countTable.length];

		for (int i = 0; i < countTable.length; i++) {
			rawCountTable[i] = countTable[i].copy();
		}
	}
    
    public void calcSmallSampleCorrections(){
    	smallSampleCorrection = new double[countTable.length];
    	
		for (int i = 0; i < countTable.length; i++) {
			double totalCounts = countTable[i].getTotal();
			smallSampleCorrection[i] = 3/(2* Math.log(2) * totalCounts);
		}
    }

   	public void createNormalizedCountTable(){
   		
    	normalizedCountTable = new Distribution[rawCountTable.length];

		for (int i = 0; i < normalizedCountTable.length; i++) {
			normalizedCountTable[i] = rawCountTable[i].copy();
		}
   		
        for (int i = 0; i < normalizedCountTable.length; i++) {
            normalizedCountTable[i].normalize();
        }
   	}
   	
    public void restoreCountTable() {
		for (int i = 0; i < countTable.length; i++) {
			countTable[i] = rawCountTable[i].copy();
		}

		normalized = false;
    }
   	
    public void pseudoNormalizeCountTable(boolean sqrtNSelected, double pseduocount) {
        for (int i = 0; i < countTable.length; i++) {
            countTable[i].pseudoNormalize(sqrtNSelected, pseduocount);
        }
        normalized = true;
    }

    public void setCounts(char symbol, int position, double value) {

        if (position >= countTable.length) {
            Distribution dis[] = new Distribution[position + 1];

            for (int i = 0; i < countTable.length; i++) {
                dis[i] = countTable[i];
            }
            countTable = dis;

        }
        if (countTable[position] == null) {
            countTable[position] = new Distribution(symbols);
        }
        countTable[position].set(symbol, value);

    }

    public double getMatch(int index, char value) {

        return countTable[index].get(value);

    }


//    public double[][] getScores() {
//        double scores[][] = new double[countTable.length][symbols.length];
//        for (int i = 0; i < countTable.length; i++) {
//            for (int j = 0; j < symbols.length; j++) {
//                double rawValue = countTable[i].get(symbols[j]);
//                scores[i][j] = Math.abs(rawValue * Math.log(4 * rawValue));
//
//            }
//        }
//        return scores;
//    }
    
    /**
     * 
     * @author tg2321
     * 
     */
    public double[][] getSmallSampleScores() {
		double scores[][] = new double[countTable.length][symbols.length];
		for (int i = 0; i < countTable.length; i++) {
			double frequency = 0;
			double frequencyLog2 = 0;
			double entropy = 0;
			double ssc = smallSampleCorrection[i];
			double entropyPlusSSC = 0;
			double information = 0;

			for (int j = 0; j < symbols.length; j++) {
				frequency = normalizedCountTable[i].get(symbols[j]);
				frequencyLog2 = Math.log(frequency) / Math.log(2);
				entropy -= frequency * frequencyLog2;
			}

			entropyPlusSSC = entropy + ssc;
			information = (Math.log(symbols.length) / Math.log(2)) - entropyPlusSSC;

			for (int j = 0; j < symbols.length; j++) {
				frequency = normalizedCountTable[i].get(symbols[j]);
				double score = frequency * information;

				if (score<0){
					score = 0;
				}
				
				scores[i][j] = score;
			}
		}
		return scores;
	}


    public double[][] getPseudoCountScores() {
        double scores[][] = new double[countTable.length][symbols.length];
        for (int i = 0; i < countTable.length; i++) {
        	double entropy = 0;
        	double information = 0;
        	double rawValue = 0;
            for (int j = 0; j < symbols.length; j++) {
                rawValue = countTable[i].get(symbols[j]);
                entropy -= rawValue * Math.log(rawValue)/Math.log(2);
            }
//            information = Math.log(symbols.length)/Math.log(2) - (entropy + smallSampleCorrection);
            information = Math.log(symbols.length)/Math.log(2) - (entropy);
            for (int j = 0; j < symbols.length; j++) {
                rawValue = countTable[i].get(symbols[j]);
                scores[i][j] = rawValue * information;
            }
        }
        return scores;
    }

    public double score(DSSequence sequence, int offset) {
        double score = 0;
        int cols = countTable.length;
        String seq = sequence.getSequence();
        for (int c = 0; c < cols; c++) {
            char x = Character.toUpperCase(seq.charAt(c + offset));
            score += Math.log(getMatch(c, x));
        }
        return score;

    }

    public double scoreReverse(DSSequence sequence, int offset) {
        double score = 0;
        int cols = countTable.length;
        String seq = sequence.getSequence();
        for (int c = 0; c < cols; c++) {
            //for (int c = cols - 1; c >= 0; c--) {
            //char x = getComplement(Character.toUpperCase(seq.charAt(c + offset)));
            int dx = cols - 1 - c;
            char x = getComplement(Character.toUpperCase(seq.charAt(offset + dx)));
            score += Math.log(getMatch(c, x));
        }

        return score;
    }

    public void collectSequenceScores(DSSequence sequence, double[] scores) {
        int i = 0;
        for (int offset = 0;
                          offset < Math.min(scores.length / 3,
                                            sequence.length() -
                                            countTable.length - 1); offset++) {
            double score1 = Math.exp(score(sequence, offset));
            scores[i++] = score1;
            double score2 = Math.exp(scoreReverse(sequence, offset));
            scores[i++] = score2;
            if (score1 == 1) {
                score1 = Math.exp(score(sequence, offset));
            }
            if (score2 == 1) {
                score2 = Math.exp(scoreReverse(sequence, offset));
            }
        }
        Arrays.sort(scores);
    }

    public void countSequenceMatches(int length, double threshold,
                                     int averageNo, int partialLength,
                                     DSSequence sequence, MatchStats ms) {
        double score;
        int count3 = 0;
        int count5 = 0;
        if (sequence != null) {
            for (int offset = 0; offset < sequence.length() - countTable.length;
                              offset++) {
                score = Math.exp(score(sequence, offset));
                if (score >= threshold) {
                    count5++;
                }
                score = Math.exp(scoreReverse(sequence, offset));
                if (score >= threshold) {
                    count3++;
                }
                partialLength++;
                if (partialLength > length * averageNo) {
                    break;
                }
            }
            if (count3 + count5 > 0) {
                ms.matchSeq++;
            }
            ms.match5primeNo += count5;
            ms.match3primeNo += count3;
            ms.matchNo += count3 + count5;
        }
    }

    private char getComplement(char char1) {

        switch (char1) {
        case 'A':
            return 'T';
        case 'C':
            return 'G';
        case 'G':
            return 'C';
        case 'T':
            return 'A';
        case 'a':
            return 't';
        case 'c':
            return 'g';
        case 'g':
            return 'c';
        case 't':
            return 'a';
        default:
            return char1;
        }

    }

    public DistributionLogo[] getLogo() {
        DistributionLogo[] result = new DistributionLogo[countTable.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = countTable[i].getLogo();
        }
        return result;

    }

    private void jbInit() throws Exception {
    }

    public void setRawCountTable(Distribution[] rowCountTable) {
        this.rawCountTable = rowCountTable;
    }

    public void setSymbols(char[] symbols) {
        this.symbols = symbols;
    }

}
