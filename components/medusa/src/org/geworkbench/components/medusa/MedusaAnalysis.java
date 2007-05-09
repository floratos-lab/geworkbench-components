package org.geworkbench.components.medusa;

import java.io.File;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;

import edu.columbia.ccls.medusa.MedusaLoader;

/**
 * 
 * @author keshav
 * @version $Id: MedusaAnalysis.java,v 1.11 2007-05-09 19:40:06 keshav Exp $
 */
public class MedusaAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {

	private Log log = LogFactory.getLog(this.getClass());

	private StringBuilder s = null;

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

		String configFile = params.getConfigFilePath();
		if (!StringUtils.isEmpty(configFile)) {
			s = new StringBuilder();
			s.append("-i=" + configFile);
			// String[] args = { "-i=" + iFile };
			// String[] args = { "-i=data/medusa/dataset/config.xml" };
		} else {
			getParameters(params);
		}

		String[] args = StringUtils.split(s.toString(), " ");

		try {
			log.info("Running MEDUSA with: " + s.toString());
			MedusaLoader.main(args);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error running medusa: " + e);
		}

		return null;
	}

	/**
	 * Read the parameters from the parameters panel.
	 * 
	 * @param params
	 */
	private void getParameters(MedusaParamPanel params) {
		/* input section of config file */
		String fileLabels = "dataset/small_yeast/yeast_test_labels";

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

		String baseArgs = " -filelabels=" + fileLabels + " -fasta="
				+ sequenceFile + " -mbtype=iterative" + " -lbounds=0"
				+ " -ubounds=0" + " -maxkmer=" + maxKmer + " -minkmer="
				+ minKmer;

		s = new StringBuilder(baseArgs);
		// the have dimers_max_gap, dimers_smallest, dimers_largest
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
