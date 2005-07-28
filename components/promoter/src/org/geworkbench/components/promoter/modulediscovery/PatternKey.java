/*
Written by (C) Kai Wang (kw2110@columbia.edu) Columbia University

This code was written using Borland Java Builder X and may be subject to
certain additional restrictions as a result.
*/
package org.geworkbench.components.promoter.modulediscovery;


import org.geworkbench.bison.datastructure.complex.pattern.DSPattern;

import java.util.Arrays;
import java.util.Hashtable;

public class PatternKey implements Comparable {
    public PatternKey(int[] key) {
        this.key = key;
        Arrays.sort(this.key);
    }

    public int[] key;
    public DSPattern[] subpatterns = null;

    public boolean isCombinable(PatternKey other_pattern) {
        if (key.length != other_pattern.key.length) return false;
        // two patterns must share up to the last motif to be combinable
        for (int i = 0; i < key.length - 1; i++) {
            if (key[i] != other_pattern.key[i]) return false;
        }
        return true;
    }

    public void getMapping(Hashtable<Integer, DSPattern> primerToKeys) {
        subpatterns = new DSPattern[key.length];
        for (int i = 0; i < key.length; i++) {
            subpatterns[i] = primerToKeys.get(new Integer(key[i]));
        }
    }

    public PatternKey mergePattern(PatternKey other_pattern) {

        int[] new_key = new int[key.length + 1];
        int i = 0;
        int j = 0;
        while (i < key.length) {
            if (key[i] == other_pattern.key[i]) {
                if (i == key.length - 1)// a patch to fix a bug.
                {
                    new_key[i + 1] = key[i];

                }
                new_key[j] = key[i];
                i++;
                j++;
            } else if (key[i] < other_pattern.key[i]) {
                new_key[j++] = key[i];
                new_key[j++] = other_pattern.key[i++];
            } else {
                new_key[j++] = other_pattern.key[i];
                new_key[j++] = key[i++];
            }
        }

        return new PatternKey(new_key);
    }

    public PatternKey addOne(Integer m) {
        int[] new_key = new int[key.length + 1];
        int b = m.intValue();
        int i = 0, j = 0;
        while (i < key.length && key[i] < b) new_key[j++] = key[i++];
        new_key[j++] = b;
        while (i < key.length) new_key[j++] = key[i++];
        return new PatternKey(new_key);
    }

    public int maxMotif() {
        int max = 0;
        for (int i = 0; i < key.length; i++) {
            if (key[i] > max) max = key[i];
        }
        return max;
    }

    public PatternKey findForward() {
        int[] new_key = new int[key.length];
        new_key[key.length - 1] = key[key.length - 1];
        int i = key.length - 2;
        while (i >= 0) {
            if (key[i] < key[i + 1]) {
                new_key[i] = key[i + 1];
                break;
            } else {
                new_key[i] = key[i];
                i--;
            }
        }
        while (--i >= 0) {
            new_key[i] = key[i];
        }
        return new PatternKey(new_key);
    }

    public int compareTo(Object o) {
        int[] other_key = ((PatternKey) o).key;
        int i = 0;

        while (i < key.length && i < other_key.length) {
            if (key[i] < other_key[i]) {
                return -1;
            }
            if (key[i] > other_key[i]) {
                return 1;
            }
            i++;
        }

        if (i < key.length)
            return 1;
        else if (i < other_key.length)
            return -1;
        else
            return 0;
    }

    public boolean equals(Object o) {
        return Arrays.equals(key, ((PatternKey) o).key);
    }

    public int hashCode() {
        int hash_value = 1;
        for (int i = 0; i < key.length; i++) {
            hash_value *= key[i];
        }

        return hash_value;
    }

    public String toString() {
        String s = "{";
        if (subpatterns != null) {
            for (int i = 0; i < subpatterns.length; i++) {
                if (subpatterns[i] == null) {
                    System.out.println("eoor");
                }
                s += subpatterns[i] + " ";
            }
        } else {
            for (int i = 0; i < key.length; i++) {
                if (key[i] == 0) {
                    System.out.println("eoor");
                }
                s += key[i] + " ";
            }
        }
        return s + "}";
    }

}

