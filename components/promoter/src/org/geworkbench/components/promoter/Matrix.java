package org.geworkbench.components.promoter;

import org.biojava.bio.gui.DistributionLogo;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;

import java.util.Arrays;
import java.util.HashMap;

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
    public static HashMap hash = new HashMap();
    public double random = 0;
    public Distribution[] countTable = new Distribution[1];

    public double getRandom() {
        return random;
    }

    public Matrix(char[] symbol) {
        symbols = symbol;
    }

    public int getLength() {
        return countTable.length;
    }

    public void normalize() {

        for (int i = 0; i < countTable.length; i++) {

            countTable[i].normalize();
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

    public int collectSequenceScores(DSSequence sequence, double[] scores) {
        double score;
        int cUP = 0;
        int cDN = 0;
        int i = 0;
        for (int offset = 0; offset < Math.min(scores.length / 3, sequence.length() - countTable.length - 1); offset++) {
            double score1 = Math.exp(score(sequence, offset));
            scores[i++] = score1;
            double score2 = Math.exp(scoreReverse(sequence, offset));
            scores[i++] = score2;
            if (score1 == 1) {
                score1 = Math.exp(score(sequence, offset));
                cUP++;
            }
            if (score2 == 1) {
                score2 = Math.exp(scoreReverse(sequence, offset));
                cDN++;
            }
        }
        Arrays.sort(scores);
        return cUP + cDN;
    }

    public void countSequenceMatches(int length, double threshold, int averageNo, int partialLength, DSSequence sequence, MatchStats ms) {
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

}
