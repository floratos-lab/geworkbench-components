package org.geworkbench.components.medusa;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;

import edu.columbia.ccls.medusa.MedusaLoader;

/**
 * 
 * @author keshav
 * @version $Id: MedusaAnalysis.java,v 1.19 2007-05-15 19:16:23 keshav Exp $
 */
public class MedusaAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {

	private Log log = LogFactory.getLog(this.getClass());

	private StringBuilder s = null;

	String fileLabels = "data/medusa/dataset/web100_test.labels";

	List<String> regulators = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * 
	 */
	public MedusaAnalysis() {
		setLabel("MEDUSA");
		setDefaultPanel(new MedusaParamPanel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getAnalysisName()
	 */
	@Override
	public String getAnalysisName() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonParameters()
	 */
	@Override
	public Map<String, Object> getBisonParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractAnalysis#getAnalysisType()
	 */
	@Override
	public int getAnalysisType() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.bison.model.analysis.Analysis#execute(java.lang.Object)
	 */
	public AlgorithmExecutionResults execute(Object input) {
		MedusaParamPanel params = (MedusaParamPanel) aspp;

		DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySetView = (CSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;

		/* PHASE 1 - create the labels file */
		// discretize
		DiscretizationUtil discretizationUtil = new DiscretizationUtil();
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> discretizedInput = discretizationUtil
				.discretize(microarraySetView, params.getIntervalBase(), params
						.getIntervalBound());

		// create labels file
		createLabelsFile(discretizedInput, params);

		/* PHASE 2 - either read config file or read parameters */
		// read config file or user parameters
		String configFile = params.getConfigFilePath();
		// TODO change how this is done
		if (!StringUtils.isEmpty(configFile)) {
			s = new StringBuilder();
			s.append("-i=" + configFile);
		} else {
			getParameters(input, params);
		}

		String[] args = StringUtils.split(s.toString(), " ");

		/* PHASE 3 - run MEDUSA */
		try {
			log.info("Running MEDUSA with: " + s.toString());
			MedusaLoader.main(args);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error running medusa: " + e);
		}

		// TODO use the RuleBean to take a list of rule files.
		MedusaData medusaData = null;
		MedusaDataSet dataSet = new MedusaDataSet(microarraySetView
				.getMicroarraySet(), "MEDUSA Results", medusaData, null);

		return new AlgorithmExecutionResults(true, "MEDUSA Results Loaded.",
				dataSet);
	}

	/**
	 * Create the configuration file.
	 * 
	 * @param input
	 * @param params
	 */
	private void createLabelsFile(Object input, MedusaParamPanel params) {

		DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySetView = (CSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;

		List<String> regulators = getRegulators(params, microarraySetView);

		List<String> targets = getTargets(params, microarraySetView);

		MedusaHelper.writeMedusaLabelsFile(microarraySetView, fileLabels,
				regulators, targets);
	}

	/**
	 * Returns a List of marker labels to be used as the regulators.
	 * 
	 * @param params
	 * @param microarraySetView
	 * @param regulators
	 * @return {@link List}
	 */
	private List<String> getRegulators(MedusaParamPanel params,
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySetView) {
		regulators = null;
		if (params.isUseSelectedAsRegulators()) {
			regulators = new ArrayList<String>();
			DSItemList<DSGeneMarker> selectedMarkers = microarraySetView
					.getUniqueMarkers();
			for (int i = 0; i < selectedMarkers.size(); i++) {
				DSGeneMarker marker = selectedMarkers.get(i);
				regulators.add(marker.getLabel());
				log.debug("added: " + marker.getLabel());
			}
		}

		else if (!StringUtils.isEmpty(params.getRegulatorsFile())) {
			String regulatorText = params.getRegulatorTextField().getText();
			String[] regs = StringUtils.split(regulatorText, ",");
			int i = 0;
			for (String reg : regs) {
				regs[i] = StringUtils.strip(reg);
				i++;
			}
			regulators = Arrays.asList(regs);
		}

		else {
			// TODO load csv and extract the
		}
		return regulators;
	}

	/**
	 * Returns a list of the marker labels to be used as targets.
	 * 
	 * @param params
	 * @param microarraySetView
	 * @param regulators
	 * @return {@link List}
	 */
	private List<String> getTargets(MedusaParamPanel params,
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySetView) {
		List<String> targets = null;
		if (params.isUseAllAsTargets()) {
			targets = new ArrayList<String>();
			DSItemList<DSGeneMarker> allMarkers = microarraySetView
					.allMarkers();
			for (int i = 0; i < allMarkers.size(); i++) {
				DSGeneMarker marker = allMarkers.get(i);
				targets.add(marker.getLabel());
				log.debug("added: " + marker.getLabel());
			}
			for (String regulatorLabel : regulators) {
				targets.remove(regulatorLabel);
			}
		}

		else if (!StringUtils.isEmpty(params.getTargetsFile())) {
			String targetText = params.getTargetTextField().getText();
			String[] targs = StringUtils.split(targetText, ",");
			int i = 0;
			for (String tar : targs) {
				targs[i] = StringUtils.strip(tar);
				i++;
			}
			targets = Arrays.asList(targs);
		}

		else {
			// TODO load csv and extract the
		}
		return targets;
	}

	/**
	 * Read the parameters from the parameters panel.
	 * 
	 * @param params
	 */
	private void getParameters(Object input, MedusaParamPanel params) {
		/* input section of config file */
		String sequenceFile = params.getFeaturesFile();

		int minKmer = params.getMinKmer();
		int maxKmer = params.getMaxKmer();

		if (minKmer > maxKmer) {
			JOptionPane.showMessageDialog(null,
					"Min kmer cannot exceed max kmer.", "Error",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException("Min kmer cannot exceed max kmer.");
		}

		double base = params.getIntervalBase();

		double bound = params.getIntervalBound();

		DSMicroarraySetView<DSGeneMarker, DSMicroarray> inputData = null;
		if (input instanceof DSMicroarraySetView) {
			inputData = (CSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
			DiscretizationUtil discretizationUtil = new DiscretizationUtil();
			inputData = discretizationUtil.discretize(inputData, base, bound);
		}

		String baseArgs = " -filelabels=" + fileLabels + " -fasta="
				+ sequenceFile + " -mbtype=iterative" + " -lbounds=0"
				+ " -ubounds=0" + " -maxkmer=" + maxKmer + " -minkmer="
				+ minKmer;

		s = new StringBuilder(baseArgs);
		// medusa group has dimers_max_gap, dimers_smallest, dimers_largest
		if (params.isUsingDimers()) {
			int minGap = params.getMinGap();
			int maxGap = params.getMaxGap();

			if (minGap > maxGap) {
				JOptionPane.showMessageDialog(null,
						"Min gap cannot exceed max gap.", "Error",
						JOptionPane.ERROR_MESSAGE);
				throw new RuntimeException("Min gap cannot exceed max gap.");
			}

			s.append(" -dimers=T");
			s.append(" -mingap=" + minGap);
			s.append(" -maxgap=" + maxGap);
		}

		else {
			s.append(" -dimers=F");
		}

		// window_size
		// min_motif_count_per_window

		/* parameters */
		s.append(" -stumpsonly=F");
		int iter = params.getBoostingIterations();
		s.append(" -iter=" + iter);
		// is_corrected
		s.append(" -hotype=random");
		s.append(" -hopercent=10");
		// holdout_experiments
		// holdout_genes
		// holdout_matrix
		// pssms
		int pssmLength = params.getPssmLength();
		s.append(" -maxpssm=" + pssmLength);

		int agg = params.getAgg();
		s.append(" -clustersize=" + agg);

		if (params.isReverseComplement()) {
			s.append(" -revcompsame=T");
		} else {
			s.append(" -revcompsame=F");
		}

		s.append(" -fewesttrim=0");
		s.append(" -mosttrim=0");
		s.append(" -reportagglom=F");

		getOutput();
	}

	private void getOutput() {
		/* output */
		String rand = RandomStringUtils.randomAlphabetic(5);
		String outputDirPath = "/temp/medusa/dataset/output";
		File outputDir = new File(outputDirPath);

		boolean success = false;
		if (!outputDir.exists()) {
			success = outputDir.mkdirs();
			log.info("created dir? " + success);
		}

		s.append(" -direxpt=" + outputDirPath);
		s.append(" -runname=" + rand);
	}

	/**
	 * 
	 * 
	 */
	public void printhelp() {
		log.info(MedusaLoader.getHelpMessage());
	}

}
