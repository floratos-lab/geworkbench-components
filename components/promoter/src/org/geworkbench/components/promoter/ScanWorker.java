package org.geworkbench.components.promoter;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.SwingWorker;

import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.DSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
import org.geworkbench.util.patterns.PatternOperations;
import org.jfree.util.Log;

/**
 * @author zji
 * @version $Id$
 */
public class ScanWorker extends SwingWorker<Boolean, Void> {

	private class ScoreStats {
		public double score;
		public double pValue;

		public ScoreStats(double score, double pValue) {
			this.score = score;
			this.pValue = pValue;
		}
	}

	final private double pValue0;
	final private DSSequenceSet<DSSequence> sequenceDB;
	final private DefaultListModel selectedTFModel;
	final private boolean set13KChecked;
	final private boolean userThreshold;
	final private double threshold0;

	final private PromoterViewPanel panel;

	private DSSequenceSet<DSSequence> background = null;

	private MatchStats msActual;
	private MatchStats msExpect;
	private int seqNo = 0;
	private int totalLength = 0;
	private double threshold = 0;
	private double pValue;

	ScanWorker(double pValue0, final DSSequenceSet<DSSequence> sequenceDB,
			final DefaultListModel selectedTFModel, boolean set13KChecked,
			final PromoterViewPanel panel, boolean useThreshold,
			double threshold0) {
		this.pValue0 = pValue0;
		this.sequenceDB = sequenceDB;
		this.selectedTFModel = selectedTFModel;
		this.set13KChecked = set13KChecked;
		this.panel = panel;
		this.userThreshold = useThreshold;
		this.threshold0 = threshold0;
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		ArrayList<TranscriptionFactor> ar = new ArrayList<TranscriptionFactor>();
		threshold = 0;

		pValue = pValue0;

		RandomSequenceGenerator rs = new RandomSequenceGenerator(sequenceDB,
				pValue);

		msActual = new MatchStats();
		msExpect = new MatchStats();
		seqNo = 0;
		totalLength = 0;
		for (Enumeration<?> en = selectedTFModel.elements(); en
				.hasMoreElements();) {
			TranscriptionFactor pattern = (TranscriptionFactor) en
					.nextElement();
			if (pattern == null)
				continue;

			panel.updateProgressBar(0, "Processing :" + pattern.getName());

			ar.add(pattern);
			ScoreStats stats = null;
			// Load the 13K set if needed
			if (set13KChecked) {
				load13KBSet();
			} else {
				background = null;
			}
			if (userThreshold) {
				pValue = 0.05;
				threshold = threshold0;
			} else {
				if (background != null) {
					// compute the threshold from the required
					// pValue
					// using the predefiedn background database
					stats = getThreshold(pattern, background, pValue);
				} else {
					// compute the threshold from the required
					// pValue
					// using a random generative model
					stats = getThreshold(pattern, rs, pValue);
				}
				// assign the new pValue based on what we could find
				if (stats != null) {
					pValue = stats.pValue;
					threshold = stats.score * 0.99;
				} else {
					// stopped.
					panel.updateProgressBar(1, "Stopped on " + new Date());
					return false;
				}
			}
			pattern.setThreshold(threshold);
			// Lengths are in base pairs (BP) and do not include the
			// reverse
			// strand. Analysis is the done on both the normal and
			// reverse
			// strand.
			int partialLength = 0;
			List<DSPatternMatch<DSSequence, CSSeqRegistration>> matches = new ArrayList<DSPatternMatch<DSSequence, CSSeqRegistration>>();
			for (int seqId = 0; seqId < sequenceDB.size(); seqId++) {
				double progress = (double) seqId / (double) sequenceDB.size();
				panel.updateProgressBar(progress,
						"Discovery: " + pattern.getName());
				DSSequence seq = sequenceDB.getSequence(seqId);
				// Count the valid positions so that we can compute
				// the background matches
				// in a meaningful way. E.g. don't count # or
				// stretches that do not contain
				// valid sequence data
				int positions = countValid(pattern, seq);
				if (positions > 10) {
					seqNo++;
					partialLength += positions;
					totalLength += positions;

					if (!userThreshold) {
						// This assumes that the pvalue has been
						// correctly estimated
						// the compute the expected matches from the
						// p-value
						int oldMatch = (int) msExpect.matchNo;
						msExpect.matchNo += pValue * (double) (positions)
								/ 1000.0;
						msExpect.match5primeNo += pValue * (double) (positions)
								/ 1000.0 / 2.0;
						msExpect.match3primeNo += pValue * (double) (positions)
								/ 1000.0 / 2.0;
						if (msExpect.matchNo - oldMatch >= 1) {
							msExpect.matchSeq++;
						}
					}
					List<DSPatternMatch<DSSequence, CSSeqRegistration>> seqMatches = pattern
							.match(seq);
					if (seqMatches.size() > 0) {
						msActual.matchSeq++;
					}
					matches.addAll(seqMatches);
				}
				if (panel.isCancelled()) {
					return false;
				}
			}
			panel.updateProgressBar(1, "Discovery: " + pattern.getName());
			if (matches != null) {
				for (DSPatternMatch<DSSequence, CSSeqRegistration> match : matches) {
					if (match.getRegistration().strand == 0) {
						msActual.match5primeNo++;
					}
					if (match.getRegistration().strand == 1) {
						msActual.match3primeNo++;
					}
					msActual.matchNo++;
				}

				PatternOperations.getPatternColor(pattern.hashCode());
				panel.addMatches(pattern, matches);
			}

			if (userThreshold) {
				if (set13KChecked) { // set13KCheck
					// using the length of the current sequences as
					// background, determine an appropriate pvalue
					// from the 13K Set
					getMatchesPerLength(pattern, partialLength, threshold,
							background, null, msExpect);
				} else {
					// using the length of the current sequences as
					// background, determine an appropriate pvalue
					// from random data
					getMatchesPerLength(pattern, partialLength, threshold,
							null, rs, msExpect);
				}
			}
		}
		return true;
	}

	@Override
	protected void done() {
		try {
			Boolean r = get();
			if (r == null || !r) {
				panel.updateAfterScanning(msActual, msExpect, totalLength,
						seqNo, threshold, pValue);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void load13KBSet() throws URISyntaxException {
		if (background == null) {
			URL set13K = this.getClass().getClassLoader().getResource("13K.fa");
			File set13KFile = new File(set13K.toURI());
			background = CSSequenceSet.getSequenceDB(set13KFile);
		} else {
			Log.debug("background is not null. Its sequnce number is "
					+ background.getSequenceNo());
		}
	}

	private ScoreStats getThreshold(TranscriptionFactor pattern,
			RandomSequenceGenerator rg, double pValue) {
		// computes the score based on a probability of a match of pValue
		// To get goo statistics, we expect at least 100 matches to exceed
		// the threshold in the null hypothesis. Hence, this is the number
		// of 1KB sequences we must test.
		int seqLen = 1000;
		int seqNo = (int) (100 / pValue);
		double scores[] = new double[seqLen * 3];

		for (int i = 0; i < seqNo; i++) {
			if (panel.isCancelled()) {
				return null;
			}
			double progress = (double) i / (double) seqNo;
			panel.updateProgressBar(progress, "Computing Null Hypothesis");
			DSSequence sequence = rg.getRandomSequence(seqLen
					+ pattern.getLength());
			pattern.getMatrix().collectSequenceScores(sequence, scores);
		}
		int x = scores.length - 101;
		while ((x < scores.length) && scores[x - 1] == scores[x]) {
			x++;
		}
		if (x >= scores.length) {
			x = scores.length - 101;
			while ((x > scores.length - 1000) && scores[x - 1] == scores[x]) {
				x--;
			}
		}
		return new ScoreStats(scores[x], (double) (scores.length - x - 1)
				/ (double) seqNo);
	}

	private ScoreStats getThreshold(TranscriptionFactor pattern,
			DSSequenceSet<DSSequence> seqDB, double pValue) {
		// computes the score based on a probability of a match of pValue
		// To get goo statistics, we expect at least 100 matches to exceed
		// the threshold in the null hypothesis. Hence, this is the number
		// of 1KB sequences we must test.

		// Total number of tokens required to compute statistics
		int totalLength = (int) (1000 * 100 / pValue);
		int partialLength = 0;
		int maxSeqLen = 2000;
		double scores[] = new double[maxSeqLen * 3];

		while (partialLength < totalLength) {
			if (panel.isCancelled()) {
				return null;
			}
			int i = (int) (Math.random() * seqDB.size());
			DSSequence sequence = seqDB.getSequence(i);
			double progress = (double) partialLength / (double) totalLength;
			panel.updateProgressBar(progress, "Computing Null Hypothesis");
			pattern.getMatrix().collectSequenceScores(sequence, scores);
			partialLength += Math.min(countValid(pattern, sequence), maxSeqLen);
		}

		int x = scores.length - 101;
		while ((x < scores.length) && scores[x - 1] == scores[x]) {
			x++;
		}
		if (x >= scores.length) {
			x = scores.length - 101;
			while ((x > scores.length - 1000) && scores[x - 1] == scores[x]) {
				x--;
			}
		}
		return new ScoreStats(scores[x], (double) (scores.length - x - 1)
				/ (double) partialLength * 1000);
	}

	private void getMatchesPerLength(TranscriptionFactor pattern, int length,
			double threshold, DSSequenceSet<DSSequence> seqDB,
			RandomSequenceGenerator rg, MatchStats ms) {
		int averageNo = panel.getAverageNo();

		// Determine the number of iterations so that the statistics are good
		int partialLength = 0;
		int totalLength = length * averageNo;
		while (partialLength < totalLength) {
			double progress = (double) partialLength / (double) totalLength;
			panel.updateProgressBar(progress, "Computing Null Hypothesis");
			DSSequence sequence = null;
			if (seqDB != null) {
				int i = (int) (Math.random() * seqDB.size());
				sequence = seqDB.getSequence(i);
			} else if (rg != null) {
				sequence = rg.getRandomSequence(1000 + pattern.getLength());
			} else {
				return;
			}
			pattern.getMatrix().countSequenceMatches(length, threshold,
					averageNo, partialLength, sequence, ms);
			partialLength += countValid(pattern, sequence);
		}
		ms.match3primeNo = (int) ms.match3primeNo / (double) averageNo;
		ms.match5primeNo = (int) ms.match5primeNo / (double) averageNo;
		ms.matchNo = (int) ms.matchNo / (double) averageNo;
		ms.matchSeq = (int) ms.matchSeq / (double) averageNo;

	}

	static private boolean isBasePair(char c) {
		switch (c) {
		case 'A':
		case 'C':
		case 'G':
		case 'T':
		case 'U':
			return true;
		default:
			return false;
		}
	}

	static private int countValid(TranscriptionFactor tf, DSSequence seq) {
		int validPositions = 0;
		int valid = 0;
		int tfLen = tf.getLength();
		String ascii = seq.getSequence();

		if (ascii.length() >= tfLen) {
			for (int i = 0; i < tfLen; i++) {
				char c = Character.toUpperCase(ascii.charAt(i));
				if (isBasePair(c)) {
					valid++;
				}
			}
			for (int i = tfLen; i < ascii.length(); i++) {
				char c1 = Character.toUpperCase(ascii.charAt(i));
				char c2 = Character.toUpperCase(ascii.charAt(i - tfLen));
				if (isBasePair(c1)) {
					valid++;
				}
				if (isBasePair(c2)) {
					valid--;
				}
				if (valid >= tfLen) {
					validPositions++;
				}
			}
		}
		return validPositions;
	}

}
