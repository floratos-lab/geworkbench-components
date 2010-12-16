package org.geworkbench.components.promoter.modulediscovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.CSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.DSPattern;
import org.geworkbench.bison.datastructure.complex.pattern.DSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqCmplxRegistration;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;

/**
 * Main Discovery program.
 * 
 * @author Kai Wang (kw2110@columbia.edu) Columbia University, Cai Duo, Xuegong
 *         Wang
 */
public class Discovery {
	private static Log log = LogFactory.getLog(Discovery.class);

	static private Vector<CSMultiSeqPattern> finalPattern = new Vector<CSMultiSeqPattern>();

	public static Vector<CSMultiSeqPattern> getFinalPattern() {
		return finalPattern;
	}

	public static void discover(
			DSSequenceSet<DSSequence> sequenceDB,
			Hashtable<DSPattern<DSSequence, CSSeqRegistration>, List<DSPatternMatch<DSSequence, CSSeqRegistration>>> patternMatch,
			int minOccur, int winSize, Hashtable<Integer, Integer> patternPrime) {

		if (minOccur <= 0) {
			log.error("Minimal suport must be specified and can not be negative!\n");
			return;
		}
		if (winSize <= 0) {
			log.error("Window length must be specified and can not be negative!\n");
			return;
		}

		// record all motifs for maximality check
		Hashtable<Integer, List<int[]>> motifTB = new Hashtable<Integer, List<int[]>>();
		Hashtable<PatternKey, CSMultiSeqPattern> patternTB = new Hashtable<PatternKey, CSMultiSeqPattern>();

		load(sequenceDB, patternPrime, patternMatch, motifTB, patternTB,
				winSize, minOccur);

		findPattern(motifTB, patternTB, winSize, minOccur);

	}

	/**
	 * The main pattern discovery algorithm
	 */
	private static void findPattern(Hashtable<Integer, List<int[]>> motifTB,
			Hashtable<PatternKey, CSMultiSeqPattern> patternTB, int winSize,
			int minOccur) {

		Object[] motifSet = motifTB.keySet().toArray();
		Arrays.sort(motifSet);

		int itr = 0;
		while (!patternTB.isEmpty()) {
			itr++;
			Object[] patternSet = patternTB.keySet().toArray();
			Arrays.sort(patternSet);

			Hashtable<PatternKey, CSMultiSeqPattern> patternTB2 = new Hashtable<PatternKey, CSMultiSeqPattern>();
			for (int i = 0; i < patternSet.length; i++) {
				CSMultiSeqPattern p = (CSMultiSeqPattern) patternTB
						.get(patternSet[i]);

				// check whether a pattern can be combined with itself
				if (p.isSelfCombinable(winSize)) {
					// only need to consider combining with patterns downstairs
					PatternKey forward_pattern = p.patternKey.findForward();
					if (patternTB.containsKey(forward_pattern)) {
						// OK even if the forward_pattern is just p itself
						CSMultiSeqPattern new_pattern = p.merge(
								(CSMultiSeqPattern) patternTB
										.get(forward_pattern), winSize,
								minOccur);
						// only add pattern that exist and has enough support
						if (new_pattern != null) {
							patternTB2.put(new_pattern.patternKey, new_pattern);
						}
					}
				}

				// combining with patterns downstairs that share up to the last
				// motif
				int j = i + 1;
				while (j < patternSet.length
						&& ((PatternKey) patternSet[i])
								.isCombinable((PatternKey) patternSet[j])) {
					CSMultiSeqPattern new_pattern = p.merge(
							(CSMultiSeqPattern) patternTB.get(patternSet[j]),
							winSize, minOccur);

					// only add pattern that exist and has enough support
					if (new_pattern != null) {
						patternTB2.put(new_pattern.patternKey, new_pattern);
					}
					j++;
				}
			}

			Object[] patternSet2 = patternTB2.keySet().toArray();
			Arrays.sort(patternSet2);

			// check the maximality of those patterns in patternTB that are
			// currently marked as maximal
			// Add the maximal patterns to the final set
			for (int i = 0; i < patternSet.length; i++) {
				CSMultiSeqPattern the_pattern = (CSMultiSeqPattern) patternTB
						.get(patternSet[i]);
				if (the_pattern.isMaximal) {
					if (checkMaximality(the_pattern, motifSet, patternTB2)) {

						finalPattern.add(the_pattern);
					}
				}
			}

			patternTB.clear();
			patternTB = patternTB2;
			patternTB2 = null;
		}
	}

	private static void load(
			DSSequenceSet<DSSequence> sequenceDB,
			Hashtable<Integer, Integer> keyPrime,
			Hashtable<DSPattern<DSSequence, CSSeqRegistration>, List<DSPatternMatch<DSSequence, CSSeqRegistration>>> patternMatch,
			Hashtable<Integer, List<int[]>> motifTB,
			Hashtable<PatternKey, CSMultiSeqPattern> patternTB, int winSize,
			int minOccur) {
		for (int k = 0; k < sequenceDB.getSequenceNo(); k++) {
			Vector<DSPatternMatch<DSSequence, CSSeqRegistration>> sortedMatches = new Vector<DSPatternMatch<DSSequence, CSSeqRegistration>>();
			Hashtable<DSPatternMatch<DSSequence, CSSeqRegistration>, DSPattern<DSSequence, CSSeqRegistration>> lookup = new Hashtable<DSPatternMatch<DSSequence, CSSeqRegistration>, DSPattern<DSSequence, CSSeqRegistration>>();
			for (DSPattern<DSSequence, CSSeqRegistration> pattern : patternMatch
					.keySet()) {
				List<DSPatternMatch<DSSequence, CSSeqRegistration>> matches = patternMatch
						.get(pattern);
				for (int i = 0; i < matches.size(); i++) {
					DSPatternMatch<DSSequence, CSSeqRegistration> match = matches
							.get(i);
					DSSequence sequence = match.getObject();
					if (sequence.getSerial() == k) {
						sortedMatches.add(match);
						lookup.put(match, pattern);
						Integer key = (Integer) keyPrime.get(pattern);
						int[] m = { k, key.intValue(),
								match.getRegistration().x1 };
						// m[0] - sid, m[1] - mid, m[2] - position
						updateMotifTable(m, motifTB);
					}

				}
			}
			@SuppressWarnings("unchecked")
			DSPatternMatch<DSSequence, CSSeqRegistration>[] sortedMatchArray = sortedMatches
					.toArray(new CSPatternMatch[0]);
			Arrays.sort(sortedMatchArray, new PatternMatchComparator());
			for (int i = 0; i < sortedMatchArray.length; i++) {
				DSPatternMatch<DSSequence, CSSeqRegistration> m1 = sortedMatchArray[i];
				CSSeqRegistration reg = (CSSeqRegistration) m1
						.getRegistration();

				int n = 1;
				while (n + i < sortedMatchArray.length) {
					int k2 = i + n++;

					DSPatternMatch<DSSequence, CSSeqRegistration> m2 = sortedMatchArray[k2];
					CSSeqRegistration reg2 = (CSSeqRegistration) m2
							.getRegistration();

					if ((reg2.x1 - reg.x1) < winSize) {
						if (reg.x2 <= reg2.x1) { // no overlap
							DSPattern<DSSequence, CSSeqRegistration> p1 = lookup
									.get(m1);
							DSPattern<DSSequence, CSSeqRegistration> p2 = lookup
									.get(m2);
							Integer key = (Integer) keyPrime.get(p1);
							if (key == null) {
								System.out.println("No key found for pattern:"
										+ p1);
							}
							int[] ml = { k, key.intValue(), reg.x1 };
							Integer key2 = (Integer) keyPrime.get(p2);
							int[] mt = { k, key2.intValue(), reg2.x1 };
							updatePatternTable(sequenceDB, ml, mt, patternTB);

						}
					} else {
						break;
					}

				}

			}
		}

		// remove those patterns of pairs that don't have enough support
		for (PatternKey key : patternTB.keySet()) {
			if (((CSMultiSeqPattern) patternTB.get(key)).matches().size() < minOccur) {
				patternTB.remove(key);
			}
		}
	}

	private static void updateMotifTable(int[] m,
			Hashtable<Integer, List<int[]>> motifTB) {

		Integer key = new Integer(m[1]);

		int[] position = { m[0], m[2] };
		if (motifTB.containsKey(key)) {
			motifTB.get(key).add(position);
		} else {
			ArrayList<int[]> positions = new ArrayList<int[]>();
			positions.add(position);
			motifTB.put(key, positions);
		}
	}

	private static void updatePatternTable(
			DSSequenceSet<? extends DSSequence> seqDB, int[] m1, int[] m2,
			Hashtable<PatternKey, CSMultiSeqPattern> patternTB) {
		// m[0] - sid, m[1] - mid, m[2] - position
		int[] comb = { m1[1], m2[1] };
		PatternKey pattern_key = new PatternKey(comb);

		CSPatternMatch<DSSequence, CSSeqRegistration> match = new CSPatternMatch<DSSequence, CSSeqRegistration>(
				seqDB.get(m1[0]));
		CSSeqCmplxRegistration reg = new CSSeqCmplxRegistration();
		reg.offsets.add(m1[2]);
		reg.offsets.add(m2[2]);
		match.setRegistration(reg);
		CSMultiSeqPattern pattern = null;
		if (patternTB.containsKey(pattern_key)) {
			pattern = patternTB.get(pattern_key);
		} else {
			pattern = new CSMultiSeqPattern(pattern_key);
			patternTB.put(pattern_key, pattern);
		}
		pattern.matches().add(match);
	}

	private static boolean checkMaximality(CSMultiSeqPattern p,
			Object[] motifSet,
			Hashtable<PatternKey, CSMultiSeqPattern> patternTB) {
		// only check sub-patterns that are from upstairs
		boolean result = true;
		int mm = p.patternKey.maxMotif();
		int i = 0;
		while (((Integer) motifSet[i]).intValue() < mm) {
			PatternKey sub_pattern = p.patternKey.addOne((Integer) motifSet[i]);
			i++;
			if (patternTB.containsKey(sub_pattern)
					&& ((CSMultiSeqPattern) patternTB.get(sub_pattern))
							.matches().size() >= p.matches().size()) {
				p.isMaximal = false;
				result = false;
				break;
			}
		}
		return result;
	}

}
