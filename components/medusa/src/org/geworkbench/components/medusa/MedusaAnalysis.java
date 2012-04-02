package org.geworkbench.components.medusa;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.medusa.MedusaCommand;
import org.geworkbench.bison.datastructure.biocollections.medusa.MedusaData;
import org.geworkbench.bison.datastructure.biocollections.medusa.MedusaDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.components.medusa.gui.MedusaParamPanel;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;

import edu.columbia.ccls.medusa.MedusaLoader;

/**
 * 
 * @author keshav
 * @version $Id$
 */
public class MedusaAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {

	private Log log = LogFactory.getLog(this.getClass());
	
	private final String analysisName = "Medusa";
	
	private StringBuilder s = null;

	// TODO change name to inputdataset.labels
	final private String fileLabels = "data/medusa/dataset/web100_test.labels";
	private static final String outdir = FilePathnameUtils.getTemporaryFilesDirectoryPath()+"temp/medusa/dataset/output/";

	private ArrayList<DSGeneMarker> regulators = null;

	private ArrayList<DSGeneMarker> targets = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 9030365941219312428L;
	/**
	 * 
	 * 
	 */
	public MedusaAnalysis() {
		setDefaultPanel(new MedusaParamPanel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getAnalysisName()
	 */
	@Override
	public String getAnalysisName() {
		return analysisName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonParameters()
	 */
	@Override
	protected Map<Serializable, Serializable> getBisonParameters() {
	    Map<Serializable, Serializable> parameterMap = new HashMap<Serializable, Serializable>();
		MedusaParamPanel paramPanel = (MedusaParamPanel) aspp;
		
		//main parameter panel
		String featuresFileName = "";
		String featuresFileContent="";
		try{
			String featuresFilePath = paramPanel.getFeaturesFilePath();
			File featuresFile = new File(featuresFilePath);
			byte[] buffer = new byte[(int)featuresFile.length()];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(featuresFilePath));
			bis.read(buffer);
			featuresFileContent=new String(buffer);
			featuresFileName=featuresFile.getName();
		}catch(Exception e){
			e.printStackTrace();
		}
	    parameterMap.put("featuresFileName", featuresFileName);
	    parameterMap.put("featuresFileContent", featuresFileContent);
	    
	    parameterMap.put("useSelectedAsRegulators", paramPanel.isUseSelectedAsRegulators());
	    parameterMap.put("regulatorTextField", paramPanel.getRegulatorTextField().getText());
	    
	    parameterMap.put("useAllAsTargets", paramPanel.isUseAllAsTargets());
	    parameterMap.put("targetTextField", paramPanel.getTargetTextField().getText());
	    
	    parameterMap.put("intervalBase", paramPanel.getIntervalBase());
	    parameterMap.put("intervalBound", paramPanel.getIntervalBound());
	    parameterMap.put("boostingIterations", paramPanel.getBoostingIterations());
	    
	    //secondary parameter panel
	    parameterMap.put("minKmer", paramPanel.getMinKmer());
	    parameterMap.put("maxKmer", paramPanel.getMaxKmer());
	    parameterMap.put("usingDimers", paramPanel.isUsingDimers());
	    parameterMap.put("minGap", paramPanel.getMinGap());
	    parameterMap.put("maxGap", paramPanel.getMaxGap());
	    parameterMap.put("reverseComplement", paramPanel.isReverseComplement());
	    parameterMap.put("pssmLength", paramPanel.getPssmLength());
	    parameterMap.put("agg", paramPanel.getAgg());

	    return parameterMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.bison.model.analysis.Analysis#execute(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public AlgorithmExecutionResults execute(Object input) {
		MedusaParamPanel params = (MedusaParamPanel) aspp;

		//DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySetView = (CSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
		
		//clone the microarraySetView, so we'll have new microarraySetView for each sessions.
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> newMicroarraySetView = null;
		try{
			byte[] encodedInput;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(input);
			encodedInput = bos.toByteArray();
			oos.flush();
			oos.close();
			bos.close();
			ByteArrayInputStream bais = new ByteArrayInputStream(encodedInput);
			ObjectInputStream ois = new ObjectInputStream(bais);
			newMicroarraySetView = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// now we have a new microarraySetView.
		
		ProgressBar pBar = Util.createProgressBar("Medusa Analysis");
		pBar.setMessage("Running Medusa");
		pBar.start();

		/* create output dir */
	    File dirPath = new File(outdir);
	    if (!(dirPath.exists())) {
	    	dirPath.mkdirs();
	    }

		/* cleanup other runs */
		//TODO: we'll need to find a way to delete outputDir/ 
//		MedusaUtil.deleteRunDir();

		/* PHASE 1 - discretize and create the labels file */

		// discretize
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> discretizedInput = 
				discretize(newMicroarraySetView, params.getIntervalBase(), params
						.getIntervalBound());
		if(discretizedInput==null) {
    		log.error("markerValue is "+ newMicroarraySetView.get(0).getMarkerValue(0).getClass().getName() +", not DSMutableMarkerValue");
    		return new AlgorithmExecutionResults(false, "mutable marker value expected", null);
		}

		// create labels file (and get targets & regulators)
		if (StringUtils.isEmpty(params.getLabelsFilePath()))
			params.setLabelsFilePath(this.fileLabels);

		createLabelsFile(discretizedInput, params);

		/* PHASE 2 - either read config file and update with user parameters */
		String configFile = params.getConfigFilePath();

		String updatedConfig = "data/medusa/dataset/config_hacked.xml";
		
		MedusaCommand command = new MedusaCommand();
		try {
			command = getParameters(input, params);
		} catch (Exception e) {
			pBar.stop();			
			log.error(e);
			return new AlgorithmExecutionResults(false,
					"Medusa analysis canceled due to error occurred while examing parameters: "+e.getMessage(), e);
		}
		String outputDir = MedusaUtil.updateConfigXml(configFile, updatedConfig, command);
		s = new StringBuilder();
		s.append("-i=" + updatedConfig);

		String[] args = StringUtils.split(s.toString(), " ");

		/* PHASE 3 - run MEDUSA */
		try {
			log.info("Running Medusa with: " + s.toString());
			MedusaLoader.main(args);			
		} catch (IllegalArgumentException iae) {
			pBar.stop();
			if (iae.getMessage().contains("MISSING FASTA ENTRY")){
				log.error(iae);
				return new AlgorithmExecutionResults(false,
						"Please check your Features File, and make sure it contains following fasta entry.\n"+
						"Error occurred while running MEDUSA: "+iae.getMessage(), iae);
			}else{
				log.error(iae);
				return new AlgorithmExecutionResults(false,
						"Error occurred while running MEDUSA: "+iae.getMessage(), iae);				
			}
		} catch (Exception e) {
			pBar.stop();
			log.error(e);
			//e.printStackTrace();
			//throw new RuntimeException("Error running medusa: " + e);
			return new AlgorithmExecutionResults(false,
					"Error occurred while running MEDUSA: "+e.getMessage(), e);
		}

		MedusaData medusaData = new MedusaData(discretizedInput
				.getMicroarraySet(), regulators, targets, command);
		MedusaDataSet dataSet = new MedusaDataSet(newMicroarraySetView
				.getMicroarraySet(), "MEDUSA Results", medusaData, null);
		dataSet.setOuputPath(outputDir);
		
		pBar.stop();
		return new AlgorithmExecutionResults(true, "MEDUSA Results Loaded.",
				dataSet);
	}

	/**
	 * Create the configuration file.
	 * 
	 * @param input
	 * @param params
	 */
	@SuppressWarnings("unchecked")
	private void createLabelsFile(Object input, MedusaParamPanel params) {
		// TODO move me to the MedusaHelper
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySetView = (CSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;

		regulators = getRegulators(params, microarraySetView);

		targets = getTargets(params, microarraySetView);

		MedusaUtil.writeMedusaLabelsFile(microarraySetView, params
				.getLabelsFilePath(), regulators, targets);
	}

	/**
	 * Returns a List of markers to be used as the regulators.
	 * 
	 * @param params
	 * @param microarraySetView
	 * @param regulators
	 * @return {@link List}
	 */
	private ArrayList<DSGeneMarker> getRegulators(MedusaParamPanel params,
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySetView) {

		ArrayList<DSGeneMarker> regulators = new ArrayList<DSGeneMarker>();

		DSGeneMarker marker = null;

		/* check if we should just use selected */
		if (params.isUseSelectedAsRegulators()) {
			DSItemList<DSGeneMarker> selectedMarkers = microarraySetView
					.getUniqueMarkers();
			for (int i = 0; i < selectedMarkers.size(); i++) {
				marker = selectedMarkers.get(i);
				regulators.add(marker);
				log.debug("added: " + marker.getLabel());
			}
		}

		/* else use either csv file or text field */
		else {
			String regulatorText = params.getRegulatorTextField().getText();
			String[] regs = StringUtils.split(regulatorText, ",");
			DSItemList<DSGeneMarker> itemList = microarraySetView.allMarkers();
			for (String reg : regs) {
				reg = StringUtils.trim(reg);
				marker = itemList.get(reg);
				regulators.add(marker);
			}
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
	private ArrayList<DSGeneMarker> getTargets(MedusaParamPanel params,
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySetView) {

		ArrayList<DSGeneMarker> targets = new ArrayList<DSGeneMarker>();

		DSGeneMarker marker = null;

		if (params.isUseAllAsTargets()) {

			DSItemList<DSGeneMarker> allMarkers = microarraySetView
					.allMarkers();
			for (int i = 0; i < allMarkers.size(); i++) {
				marker = allMarkers.get(i);
				targets.add(marker);
				log.debug("added: " + marker.getLabel());
			}
		}

		/* else use either csv file or text field */
		else {
			String targetText = params.getTargetTextField().getText();
			String[] targs = StringUtils.split(targetText, ",");
			DSItemList<DSGeneMarker> itemList = microarraySetView.allMarkers();
			for (String tar : targs) {
				tar = StringUtils.trim(tar);
				marker = itemList.get(tar);
				targets.add(marker);
			}
		}

		removeDuplicateTargetsAndRegulators(params, microarraySetView, targets);

		return targets;
	}

	/**
	 * If a marker has been selected as BOTH a target and regulator, it will be
	 * removed from the larger list. That is, if there are more targets, the
	 * marker will be removed from the list of targets. If there are more
	 * regulators, the marker will be removed frm the list of regulators.
	 * 
	 * @param params
	 * @param microarraySetView
	 * @param targets
	 */
	private void removeDuplicateTargetsAndRegulators(MedusaParamPanel params,
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySetView,
			List<DSGeneMarker> targets) {
		if (regulators == null)
			regulators = this.getRegulators(params, microarraySetView);

		if (regulators.size() >= targets.size()) {
			for (DSGeneMarker m : targets) {
				log
						.debug("Marker "
								+ m.getLabel()
								+ " has been selected as both a target and regultator.  Since there are "
								+ "more regulators than targets, will remove this from the current list of targets.");
				regulators.remove(m);
			}
		}

		else {
			for (DSGeneMarker m : regulators) {
				log
						.debug("Marker "
								+ m.getLabel()
								+ " has been selected as both a target and regultator.  Since there are "
								+ "more targets than regulators, will remove this from the current list of targets.");
				targets.remove(m);
			}
		}
	}

	/**
	 * Read the parameters from the parameters panel.
	 * 
	 * @param params
	 */
	private MedusaCommand getParameters(Object input, MedusaParamPanel params) {

		MedusaCommand command = new MedusaCommand();

		/* input section of config file */
		if (params.getFeaturesFilePath() == ""){
			JOptionPane.showMessageDialog(null,
					"Features File has not been set yet.", "Error",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException("Features File has not been set yet.");
		}
		command.setFeaturesFile(params.getFeaturesFilePath());

		command.setMinKer(params.getMinKmer());

		command.setMaxKer(params.getMaxKmer());

		if (params.getMinKmer() > params.getMaxKmer()) {
			JOptionPane.showMessageDialog(null,
					"Min kmer cannot exceed max kmer.", "Error",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException("Min kmer cannot exceed max kmer.");
		}

		command.setBase(params.getIntervalBase());

		command.setBound(params.getIntervalBound());

		// medusa group has dimers_max_gap, dimers_smallest, dimers_largest
		if (params.isUsingDimers()) {
			command.setUsingDimers(true);
			command.setMinGap(params.getMinGap());
			command.setMaxGap(params.getMaxGap());

			if (params.getMinGap() > params.getMaxGap()) {
				JOptionPane.showMessageDialog(null,
						"Min gap cannot exceed max gap.", "Error",
						JOptionPane.ERROR_MESSAGE);
				throw new RuntimeException("Min gap cannot exceed max gap.");
			}
		}

		else {
			command.setUsingDimers(false);
		}

		if (params.isReverseComplement()) {
			command.setReverseComplement(true);
		} else {
			command.setReverseComplement(false);
		}

		/* parameters */
		command.setIter(params.getBoostingIterations());

		command.setPssmLength(params.getPssmLength());

		command.setAgg(params.getAgg());

		if (params.isReverseComplement()) {
			// s.append(" -revcompsame=T");
		} else {
			// s.append(" -revcompsame=F");
		}

		return command;
	}

	/**
	 * 
	 * 
	 */
	public void printhelp() {
		log.info(MedusaLoader.getHelpMessage());
	}

	@Override
	public Class<MedusaDataSet> getBisonReturnType() {
		return MedusaDataSet.class;
	}

	@Override
	protected boolean useMicroarraySetView() {
		return true;
	}

	@Override
	protected boolean useOtherDataSet() {
		return false;
	}

	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet<?> refMASet) {
		MedusaParamPanel params = (MedusaParamPanel) aspp;
		if (params.getFeaturesFilePath() == ""){
			return new ParamValidationResults(false,"Features File has not been set yet.");
		}
		if (params.getMinKmer() > params.getMaxKmer()) {
			return new ParamValidationResults(false,"Min kmer cannot exceed max kmer.");
		}
		if (params.isUsingDimers()) {
			if (params.getMinGap() > params.getMaxGap()) {
				return new ParamValidationResults(false,"Min gap cannot exceed max gap.");
			}
		}
		DSItemList<DSGeneMarker> markerList = maSetView.allMarkers();
		if (!isRegulatorValid(markerList))
			return new ParamValidationResults(false, "Regulator Not Valid");
		if (!isTargetValid(markerList))
			return new ParamValidationResults(false, "Target Not Valid");
		return new ParamValidationResults(true,"No Error");
	}
	
	/* either csv file or text field */
	private boolean isRegulatorValid(DSItemList<DSGeneMarker> markerList) {
		MedusaParamPanel params = (MedusaParamPanel) aspp;
		boolean valid = false;
		if (!params.isUseSelectedAsRegulators()) {
			String regulatorText = params.getRegulatorTextField().getText();
			String[] regs = StringUtils.split(regulatorText, ",");
			
			for (String reg : regs) {
				if (markerList.get(StringUtils.trim(reg)) != null) {
					valid = true;
					break;
				}
			}
		} else valid = true;
		return valid;
	}

	/* either csv file or text field */
	private boolean isTargetValid(DSItemList<DSGeneMarker> markerList) {
		MedusaParamPanel params = (MedusaParamPanel) aspp;
		boolean valid = false;
		if (!params.isUseAllAsTargets()) {
			String targetText = params.getTargetTextField().getText();
			String[] targs = StringUtils.split(targetText, ",");
			for (String tar : targs) {
				if (markerList.get(StringUtils.trim(tar)) != null) {
					valid = true;
					break;
				}
			}
		} else valid = true;
		return valid;
	}

	/**
	 * 
	 * @param microarraySetView
	 * @param base
	 * @param bound
	 * @return
	 */
	static private DSMicroarraySetView<DSGeneMarker, DSMicroarray> discretize(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySetView, double base, double bound) {

		DSMicroarraySet microarraySet = microarraySetView.getMicroarraySet();

		/* extract microarray info from DSMicroarraySet */
		int numArrays = microarraySetView.size();

		for (int i = 0; i < numArrays; i++) {
			/* geworkbench array */
			DSMicroarray microarray = (DSMicroarray) microarraySetView.get(i);
			float data[] = microarray.getRawMarkerData();
			String name = microarray.getLabel();
			if (name == null || StringUtils.isEmpty(name))
				name = "i";// give array a name

			float[] ddata = discretize(data, base, bound);
			DSMicroarray discreteMicroarray = microarray;

			for (int j = 0; j < ddata.length; j++) {

                DSMarkerValue v = discreteMicroarray.getMarkerValue(j);
            	if(! (v instanceof DSMutableMarkerValue) ) {
            		// this may happen after future design improvement. in that case the the value need to set at microarray set level
            		return null;
            	}

                DSMutableMarkerValue markerValue = (DSMutableMarkerValue)v;
				markerValue.setValue(ddata[j]);
				discreteMicroarray.setMarkerValue(j, markerValue);
			}
			microarraySet.setLabel(microarraySet.getLabel());
		}

		return microarraySetView;
	}
	
	/**
	 * Creates a dircretized array of data from the original data.
	 * <p>
	 * If data[i] < base - bound, discreteData[i] = -1
	 * <p>
	 * If data[i] > base + bound, discreteData[i] = 1.
	 * <p>
	 * If base - bound <= data[i] < base + bound, discreteData[i] = 0.
	 * 
	 * @param data
	 * @param base
	 * @param bound
	 * @return
	 */
	static private float[] discretize(float[] data, double base, double bound) {
		float[] discreteData = new float[data.length];

		double pinterval = base + bound;

		double ninterval = base - bound;

		for (int i = 0; i < discreteData.length; i++) {

			float val = data[i];
			if ((ninterval) <= val && val <= (pinterval)) {
				discreteData[i] = 0;
			} else if (val < ninterval) {
				discreteData[i] = -1;
			} else {
				discreteData[i] = 1;
			}
		}

		return discreteData;

	}
}
