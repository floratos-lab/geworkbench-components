package org.geworkbench.components.discovery;

import polgara.soapPD_wsdl.Exhaustive;
import polgara.soapPD_wsdl.Hierarchical;
import polgara.soapPD_wsdl.Parameters;
import polgara.soapPD_wsdl.ProfileHMM;

/**
 * <p>
 * Title: ParametersHandler
 * </p>
 * <p>
 * Description: Written to separate parameters view from parameters data
 * handling
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: CalifanoLab
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class ParametersHandler {
	public ParametersHandler() {
	}

	/**
	 * Reads the parameters in the parameter panel.
	 * 
	 * @param parmsPanel
	 *            ParameterPanel
	 * @param seqNo
	 *            int
	 * @param command
	 *            String
	 * @return Parameters the parameters from the panel
	 */
	public Parameters readParameter(ParameterPanel parmsPanel, int seqNo,
			String command) {
		Parameters parms = new Parameters();
		try {

			String supportString = parmsPanel.getMinSupport();
			String supportType = parmsPanel.getCurrentSupportMenuStr();
			if (supportType.equalsIgnoreCase(ParameterPanel.SUPPORT_PERCENT_1_100)) {
				double minSupport = Double.parseDouble(supportString.replace(
						'%', ' ')) / 100.0;
				parms.setMinPer100Support(minSupport);
				parms.setMinSupport((int) (Math.ceil(parms
						.getMinPer100Support()
						* (double) seqNo)));
				parms.setCountSeq(1);
			}
			if (supportType.equalsIgnoreCase(ParameterPanel.SUPPORT_SEQUENCES)) {
				// parms.setMinPer100Support(0);
				int minSupport = (int) Double.parseDouble(supportString);
				parms.setMinSupport(minSupport);
				// parms.setMinSupport((int)
				// (Math.ceil(parms.getMinPer100Support() * (double) seqNo)));
				parms.setCountSeq(1);

			}
			if (supportType.equalsIgnoreCase(ParameterPanel.SUPPORT_OCCURANCES)) {
				// parms.setMinPer100Support(0);
				int minSupport = (int) Double.parseDouble(supportString);
				parms.setMinSupport(minSupport);
				// parms.setMinSupport((int)
				// (Math.ceil(parms.getMinPer100Support() * (double) seqNo)));
				parms.setCountSeq(0);

			}
			// if(supportType.equalsIgnoreCase(parmsPanel.SUPPORT_OCCURANCES)){
			// parms.setCountSeq((int)Double.parseDouble(supportString));
			// }
			// Parsing the BASIC panel
//			if (supportString.endsWith("%")) {
//				// If this is a percentage then CountSeq is true by default
//				parms.setMinPer100Support(Double.parseDouble(supportString
//						.replace('%', ' ')) / 100.0);
//				parms.setMinSupport((int) (Math.ceil(parms
//						.getMinPer100Support()
//						* (double) seqNo)));
//				parms.setCountSeq(1);
//			} else {
//
//				parms.setMinPer100Support(0);
//				// parms.setMinSupport(Integer.parseInt(supportString));
//				// fix bug 849
//				double value = Double.parseDouble(supportString.trim());
//				if (value > 1) {
//					parms.setMinPer100Support(value / 100);
//
//				} else {
//
//					parms.setMinPer100Support(Double.parseDouble(supportString
//							.trim()));
//
//				}
//				parms.setMinSupport((int) (Math.ceil(parms
//						.getMinPer100Support()
//						* (double) seqNo)));
//				parms.setCountSeq(parmsPanel.getCountSeqBoxSelected());
//			}

			parms.setMinTokens(parmsPanel.getMinTokens());
			parms.setWindow(parmsPanel.getWindow());
			parms.setMinWTokens(parmsPanel.getMinWTokens());

			// Parsing the ADVANCED panel
			parms.setExactTokens(2);
			parms.setExact(parmsPanel.getExactOnlySelected());
			parms.setPrintDetails(0);// false by default
			parms.setComputePValue(parmsPanel.getPValueBoxSelected());
			parms.setSimilarityMatrix(parmsPanel.getMatrixSelection());
			parms.setSimilarityThreshold(parmsPanel.getSimilarityThreshold());
			parms.setMinPValue(parmsPanel.getMinPValue());

			// Parsing the GROUPING panel
			parms.setGroupingType(parmsPanel.getGroupingType());
			parms.setGroupingN(parmsPanel.getGroupingN());

			// Parsing the LIMITS panel
			parms.setMaxPatternNo(parmsPanel.getMaxPatternNo());
			parms.setMinPatternNo(parmsPanel.getMinPatternNo());
			parms.setMaxRunTime(parmsPanel.getMaxRunTime());
			parms.setThreadNo(1);
			parms.setThreadId(0);
			parms.setInputName("gp.fa");
			parms.setOutputName("results.txt");

			ProfileHMM hmm = new ProfileHMM();
			hmm.setEntropy(parmsPanel.getProfileEntropy());
			hmm.setWindow(parmsPanel.getWindow());
			parms.setProfile(hmm);

			if (command.equalsIgnoreCase("Exhaustive")) {
				Exhaustive eparams = new Exhaustive();
				String decSupport = parmsPanel.getDecSupportExhaustive();
				final double REDUCTION = 0.95; // 5% default
				double reduction = REDUCTION;
				// If this is a percentage then CountSeq is true by default
				reduction = (1.0 - ((double) Integer.parseInt(decSupport) / 100.0));
				if (reduction <= 0.0 || reduction >= 1.0) {
					reduction = REDUCTION;
				} else {
					eparams.setDecrease(reduction);
				}

				final int SUPPORT = 1; // default
				int minSupport = SUPPORT;

				String minSupportStr = parmsPanel.getMinSupportExhaustive();
				if (minSupportStr.endsWith("%")) {
					String temp = minSupportStr.replace('%', ' ').trim();
					double minSupportInt = Double.parseDouble(temp);

					double percent = (minSupportInt / 100.0);
					if (percent > 0.0 && percent < 1.0) {
						minSupport = (int) (percent * (double) parms
								.getMinSupport());
					}
				} else {
					minSupport = Integer.parseInt(minSupportStr.replace('%',
							' '));
				}
				// check that the min support is less than the initial support
				if ((minSupport > parms.getMinSupport()) || (minSupport == 0)) {
					minSupport = SUPPORT;
				}

				eparams.setMinSupport(minSupport);

				// ok set the Exhaustive parameters:
				parms.setExhaustive(eparams);

			}
			if (command.equalsIgnoreCase("Hiearchical")) {
				// Parsing the HIERARCHICAL panel
				int cluster = parmsPanel.getMinClusterSize();
				Hierarchical h = new Hierarchical();
				h.setClusterSize(cluster);
				parms.setHierarchical(h);
			}
		} catch (NumberFormatException ex) {
			return null;
		}
		return parms;
	}

	public void writeParameter(ParameterPanel panel, Parameters parms) {
		panel.setParameters(parms);
	}
}
