package org.geworkbench.components.matrixreduce;

import com.larvalabs.chart.PSAMPlot;
import org.apache.commons.collections15.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.CSMatrixReduceSet;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.CSPositionSpecificAffinityMatrix;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.DSMatrixReduceSet;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.DSPositionSpecificAffintyMatrix;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;
import org.bussemakerlab.MatrixREDUCE.engine.MatrixREDUCE;
import org.bussemakerlab.MatrixREDUCE.XML.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;


/**
 * @author John Watkinson
 * @author keshav
 * @author ch2514
 */
public class MatrixReduceAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis, Observer {
	Log log = LogFactory.getLog(this.getClass());

	private static final String TEMP_DIR = "MatrixREDUCE";

	public static final String[] NUCLEOTIDES = { "A", "C", "G", "T" };
	public static final String[] NUCLEOTIDES_SMALL = { "a", "c", "g", "t" };
	public static final String DELIMITER = "#";
	public static final String EXPERIMENT = "based on experiment:";
	public static final String PVALUE = "p_value=";
	public static final String PCUTOFF = "p-cutoff";
	public static final String DYNAMIC_DIRECTION = "dynamic";
	public static final String PARAM_SEQUENCE = "sequence";
	public static final String PARAM_TOPOLOGY = "topology";
	public static final String PARAM_PVALUE = "pvalue";
	public static final String PARAM_MAXMOTIF = "maxmotif";
	public static final String PARAM_STRAND = "strand";
	
	private final String analysisName = "MatrixREDUCE";
	MatrixReduceParamPanel params = null;
	

	public MatrixReduceAnalysis() {
		setLabel("MatrixREDUCE");
		setDefaultPanel(new MatrixReduceParamPanel());
		params = (MatrixReduceParamPanel) aspp;
	}
	
	public Map<Serializable, Serializable> getBisonParameters() {
		Map<Serializable, Serializable> parameterMap = new HashMap<Serializable, Serializable>();
		
		//seq file - read from file
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		String seq = "";
		try{
			String seqfilepath = params.getSequenceFile();
			log.info("seq file=" + seqfilepath);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(seqfilepath)));
			sb = new StringBuilder();
			String s;
			while((s = br.readLine()) != null){
				sb.append(s + "\n");
			}
			seq = sb.toString();
		} catch (Exception e){
			log.error("Cannot read topology file [" + params.getSequenceFile() + "]: " + e.getMessage() + ": fasta sequence is empty.");
		} finally {
			try{
				br.close();
			} catch (Exception close){
				log.error("Cannot close topology file [" + params.getSequenceFile() + "]: " + close.getMessage());
			}
		}
		parameterMap.put(PARAM_SEQUENCE, seq);
		
		//topo list - read from file if file is specified
		String topology = "";
		if(!params.getTopoFile().trim().equals("")){
			br = null; sb = null;
			try{
				br = new BufferedReader(new InputStreamReader(new FileInputStream(params.getTopoFile())));
				sb = new StringBuilder();
				String s;
				while((s = br.readLine()) != null){
					sb.append(s + "\n");
				}
				topology = sb.toString();
			} catch (Exception e){
				log.error("Cannot read topology file [" + params.getTopoFile() + "]: " + e.getMessage() + ": topology pattern is empty.");
			} finally {
				try{
					br.close();
				} catch (Exception close){
					log.error("Cannot close topology file [" + params.getTopoFile() + "]: " + close.getMessage());
				}
			}
		} else {
			topology = params.getTopoPattern().toLowerCase();
		}
		parameterMap.put(PARAM_TOPOLOGY, topology);
		
		//pvalue
		parameterMap.put(PARAM_PVALUE, new Float(params.getPValue()));
		
		//maxMotif
		parameterMap.put(PARAM_MAXMOTIF, new Integer(params.getMaxMotif()));
		
		//strand	
		parameterMap.put(PARAM_STRAND, new Integer(params.getStrand()));

		return parameterMap;
	}
	
	@Override
	public Class getBisonReturnType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getAnalysisName() {
		return this.analysisName;
	}

	// not used
	public int getAnalysisType() {
		return AbstractAnalysis.ZERO_TYPE;
	}
	
	public ParamValidationResults validateParameters() {
		MatrixReduceParamPanel params = (MatrixReduceParamPanel) aspp;
		String errMsg = "";
		// all 3 parameter ranges are validated right on the gui component
		// just need to verify that the required files/pattern are specified
		String file = params.getSequenceFile();
		if((file == null)
				|| file.trim().equals("") 
				|| file.trim().equals(MatrixReduceParamPanel.FILE_SPECIFY)
				){
			errMsg += "No sequence file specified.  ";
		}
		
		
		if(params.getTopoPattern().trim().equals("")){
			file = params.getTopoFile();
			if((file == null)
					|| file.trim().equals("") 
					|| file.trim().equals(MatrixReduceParamPanel.PATTERN_REQUIRED)
					){
				errMsg += "No topological pattern or file specified.  ";
			}
		}
		if (errMsg.equals(""))
			return new ParamValidationResults(true, null);
		else
			return new ParamValidationResults(false, errMsg);
	}

	private static double[][] convertScoresToWeights(double[][] psamData) {
		double[][] psamddG = new double[psamData.length][4];
		for (int i = 0; i < psamData.length; i++) {
			double logMean = 0;
			for (int j = 0; j < 4; j++) {
				logMean += Math.log(psamData[i][j]);
			}
			logMean /= 4;
			for (int j = 0; j < 4; j++) {
				psamddG[i][j] = Math.log(psamData[i][j]) - logMean;
			}
		}
		return psamddG;
	}

	public AlgorithmExecutionResults execute(Object input) {	
		try {
			DSMicroarraySet<DSMicroarray> mSet = ((DSMicroarraySetView) input).getMicroarraySet();
			
			// get params and construct query
			float pval = (float) params.getPValue();
			int maxMotif = params.getMaxMotif();
			int strandNum = params.getStrand();
			
			// get file names
			String expressionFile = StringUtils.replace(mSet.getPath(), "\\", "/");
			String sequenceFile = StringUtils.replace(params.getSequenceFile(), "\\", "/");
			String topoFile = StringUtils.replace(params.getTopoFile(), "\\", "/");		
			
			String topology = "";
			String list = "";
			if(!params.getTopoFile().trim().equals("")){
				topology = topoFile;
				list = "-list";
			} else {
				topology = params.getTopoPattern().toLowerCase();
			}
			
			String[] newArgs = {"-se=" + sequenceFile 
					, "-e=" + expressionFile 
					, "-topo=" + topology 
					, list
					, "-o=" + TEMP_DIR
					, "-p_value=" + pval
					, "-max_motif=" + maxMotif
					, "-strand=" + strandNum
					};				
			
			
			String NEWARGS = "";
			for(int i = 0; i < newArgs.length; i++){
				NEWARGS += newArgs[i] + " ";
			}

			log.info("Running ./FitModel with " + NEWARGS);
			
			MatrixREDUCE mr = new MatrixREDUCE(newArgs);
			ProgressBar pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
			pb.setModal(true);
			pb.setTitle("MatrixREDUCE");
			pb.setMessage("Running MatrixREDUCE...");
			pb.start();
			int exitVal = -1;
			exitVal = mr.run();
			if(exitVal >= 0){ 
				pb.stop();
			}
			
			log.info("Exit Value: " + exitVal + "\n");
			log.info("STDOUT: " + mr.stdout);
			log.info("STDERR: " + mr.stderr);			

			if (exitVal == 0) {
			
				// Parse FASTA sequence data
				File fastaFile = new File(sequenceFile);

				ListOrderedMap<String, String> sequenceMap = new ListOrderedMap<String, String>();
				{
					BufferedReader in = new BufferedReader(new FileReader(
							fastaFile));
					String line = in.readLine();
					String gene = null;
					StringBuffer sequence = null;
					while (line != null) {
						if (line.startsWith(">")) {
							if (gene != null) {
								sequenceMap.put(gene, sequence.toString()
										.toUpperCase());
							}
							sequence = new StringBuffer();
							gene = line.substring(1).split(" ")[0];
						} else {
							sequence.append(line.trim());
						}
						line = in.readLine();
					}
					if (gene != null) {
						sequenceMap
								.put(gene, sequence.toString().toUpperCase());
					}
				}
				
				DSMatrixReduceSet dataSet = new CSMatrixReduceSet(mSet,
						"MatrixREDUCE Results");
				
				log.debug("calling mr.getResults()");
				try{
					mr.getResults();				
					log.debug("called mr.getResults()");
				} catch (Exception mre){
					log.error("Cannot get results from MatrixREDUCE run.", mre);
				}
				
				ArrayList<Psam> psams = mr.mrRes.Results.getPsam();
	            for ( int i = 0; i < psams.size(); i++ ) {
	            	DSPositionSpecificAffintyMatrix psam = new CSPositionSpecificAffinityMatrix();		           	
	            	
	            	psam.setExperiment(psams.get(i).get_experiment_description());
					psam.setPValue(pval);		            	
	            	if (psams.get(i).get_directionality().toLowerCase().indexOf(DYNAMIC_DIRECTION) >= 0) {
						psam.setTrailingStrand(true);
					} else {
						psam.setTrailingStrand(false);
					}	            	
					psam.setSeedSequence(psams.get(i).get_seed_motif());
					psam.setConsensusSequence(psams.get(i).get_optimal_sequence());
									
					// n is the position
					// 4 is a, c, g, t
					ArrayList<BasePair> affPos = psams.get(i).getAffinities().getBasePair();
	                int n = affPos.size();
					double[][] scores = new double[n][4];							
					for(int j = 0; j < n; j++){
						scores[j][0] = affPos.get(j).get_a(); 
                        scores[j][1] = affPos.get(j).get_c();
                        scores[j][2] = affPos.get(j).get_g();
                        scores[j][3] = affPos.get(j).get_t();
					}
					psam.setScores(scores);
					// Generate logo
					PSAMPlot psamPlot = new PSAMPlot(
							convertScoresToWeights(scores));
					psamPlot.setMaintainProportions(false);
					psamPlot.setAxisDensityScale(4);
					psamPlot.setAxisLabelScale(3);
					BufferedImage image = new BufferedImage(
							MatrixReduceViewer.IMAGE_WIDTH,
							MatrixReduceViewer.IMAGE_HEIGHT,
							BufferedImage.TYPE_INT_RGB);
					Graphics2D graphics = (Graphics2D) image.getGraphics();
					psamPlot.layoutChart(MatrixReduceViewer.IMAGE_WIDTH,
							MatrixReduceViewer.IMAGE_HEIGHT, graphics
									.getFontRenderContext());
					psamPlot.paint(graphics);
					ImageIcon psamImage = new ImageIcon(image);
					psam.setPsamImage(psamImage);
					dataSet.add(psam);
	            } 			
				dataSet.setSequences(sequenceMap);
				if(params.saveRunLog()){
					log.info("Saving run log to project history.");
					if(!StringUtils.isEmpty(mr.stderr))
						ProjectPanel.addToHistory(dataSet, mr.stderr.substring(mr.stderr.indexOf("\n")));
					else
						ProjectPanel.addToHistory(dataSet, mr.stdout);
				}
				return new AlgorithmExecutionResults(true, "Completed",
						dataSet);
			} else {
				return new AlgorithmExecutionResults(false, "Cancelled", null);
			}
		} catch (Throwable e) {
			log.error("Runtime error while running MatrixREDUCE", e);
			throw new RuntimeException(e);
		}
		
	}

	
	public static void writePSAM(DSPositionSpecificAffintyMatrix psam,
			PrintWriter out) throws IOException {
		out.println(psam.getSeedSequence() + " # " + psam.getExperiment());
		out.println("" + psam.getPValue() + " # p-value");
		out.println("" + psam.getBonferroni() + " # bonferroni");
		if (psam.isTrailingStrand()) {
			out.println("1 # derived from trailing strand");
		} else {
			out.println("0 # derived from leading strand");
		}
		// Header
		out
				.println("#\ta                   \tc                   \tg                   \tt                   ");
		double[][] scores = psam.getScores();
		for (double[] values : scores) {
			int best = 0;
			double bestValue = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < values.length; i++) {
				if (values[i] > bestValue) {
					bestValue = values[i];
					best = i;
				}
			}
			out.print(NUCLEOTIDES_SMALL[best]);
			for (int i = 0; i < values.length; i++) {
				out.printf("\t%1.20f", values[i]);
			}
			out.println();
		}
	}
	

	@Publish
	public DSMatrixReduceSet publishMatrixReduceSet(DSMatrixReduceSet data) {
		return data;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#useMicroarraySetView()
	 */
	@Override
	protected boolean useMicroarraySetView() {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#useOtherDataSet()
	 */
	@Override
	protected boolean useOtherDataSet() {
		return false;
	}
}
