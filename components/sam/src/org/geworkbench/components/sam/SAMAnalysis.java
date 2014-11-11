package org.geworkbench.components.sam;
 
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.SamResultData;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.engine.preferences.GlobalPreferences;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.ProgressBar;

/**
 * 
 * @author zm2165 
 */
public class SAMAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {

 	private static final long serialVersionUID = -1672201775884915447L;
 	private static Log log = LogFactory.getLog(SAMAnalysis.class);
 	
 	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
 	private static final String R_SCRIPT="sam.r";
 	private static final String R__ERROR_LOG = "err.log";
 	private static final String STOP_MESSAGE_CONF = FilePathnameUtils.getUserSettingDirectoryPath()
 			+ "sam" + FILE_SEPARATOR + "stop_message.conf";

	private static final int GROUP_CASE = 1;
	private static final int GROUP_CONTROL = 0;
	private static final int NEITHER_GROUP = 2;
	
 	private static final long POLL_INTERVAL = 5000; //5 seconds
 	
	/*
	 * This has to be a field to handle the need that the the gridServive needs
	 * it as part of the parameter map AND it in fact depends on the input data
	 * set. It is not a ideal design, but a hack for the existing architecture.
	 */
 	private int[] groupAssignments;
 	
 	public SAMAnalysis() throws IOException {		
		setDefaultPanel(new SAMPanel());
	}
 	
 	@Override
	public void update(Observable o, Object arg) {
 		this.stopAlgorithm = true;
	}
 	
	@SuppressWarnings("unchecked")
	@Override
	public AlgorithmExecutionResults execute(Object input) {
		
		if (input == null || !(input instanceof DSMicroarraySetView)) {
			log.error("Invalid input type");
			return new AlgorithmExecutionResults(false, "Invalid input.", null);
		}

		DSMicroarraySetView<DSGeneMarker, DSMicroarray> data = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;

		int numGenes = data.markers().size();
		int numExps = data.items().size();
		
		DSDataSet<? extends DSBioObject> set = data.getDataSet();

		if (!(set instanceof DSMicroarraySet)) {			
			return null;
		}		
		

		String version = System.getProperty("application.version");	
		String currdir = FilePathnameUtils.getTemporaryFilesDirectoryPath();
		
		String predir=currdir+"sam";
		File prefile_sam=new File(predir);
		if(!prefile_sam.exists()){
			if(!(prefile_sam).mkdir())
				return new AlgorithmExecutionResults(false, "Cannot create directory at "+predir, null);
		}

		predir=predir + FILE_SEPARATOR + version;
		File prefile_version=new File(predir);
		if(!prefile_version.exists()){
			if(!(prefile_version).mkdir())
				return new AlgorithmExecutionResults(false, "Cannot create directory at "+predir, null);
		}	

		String samdir=predir+FILE_SEPARATOR;
		
		if(!getStopMessageConfigure()){
		
			JCheckBox checkbox = new JCheckBox("Do not show this message again.");
			String message = "SAM requires that R already be installed on your computer. The R location should be entered in Tools->Preference->R location.\n" +
				    "The SAM R package is also required. It will be installed automatically if not already installed.\n" +
				    "Do you want to continue?";
			Object[] params = {message, checkbox};
			int n = JOptionPane.showConfirmDialog(
					null,
					params,
				    "Please be aware of",
				    JOptionPane.YES_NO_OPTION);
			rememberStopMessageConfigure(checkbox.isSelected());
			
			if(n!=JOptionPane.YES_OPTION)
				return new AlgorithmExecutionResults(false, "Analysis aborted.", null);
			
		}
				
		String rExe = GlobalPreferences.getInstance().getRLocation();
		if ((rExe == null)||(rExe.equals(""))) {
			//log.info("No R location configured.");
			return new AlgorithmExecutionResults(false, "Rscript.exe's location is not assigned", null);
		} else {
			File rExeFile=new File(rExe);
			if(!rExeFile.exists()) {
				return new AlgorithmExecutionResults(false, "Rscript.exe not exist. Please check it's location at Tools->Preference->R location.", null);
			}
		}
		String rLibPath = GlobalPreferences.getInstance().getRLibPath();
		if (rLibPath.trim().length()>0){
			File rLibFile=new File(rLibPath);
			if(!rLibFile.exists() || !rLibFile.isDirectory() || !rLibFile.canWrite())
				return new AlgorithmExecutionResults(false, "R package directory " + rLibPath+" is not valid.\nPlease "+
						"provide a valid writeable user directory at Tools->Preference->R package directory.", null);
		}

		ProgressBar pbSam = ProgressBar
				.create(ProgressBar.INDETERMINATE_TYPE);
		pbSam.addObserver(this);
		pbSam.setTitle("SAM Analysis");
		pbSam.setBounds(new ProgressBar.IncrementModel(0, numGenes, 0,
				numGenes, 1));

		pbSam.setMessage("Calculating SAM, please wait...");
		pbSam.start();
		this.stopAlgorithm = false;
		
		DSMicroarraySet maSet = (DSMicroarraySet) set;
		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<DSMicroarray> context = manager
				.getCurrentContext(maSet);

		SAMPanel samPanel = (SAMPanel) this.getParameterPanel();
		float deltaInc, deltaMax;
		int perm;
		try {
			deltaInc=Float.parseFloat(samPanel.getDeltaInc());
			deltaMax=Float.parseFloat(samPanel.getDeltaMax());
			perm=Integer.parseInt(samPanel.getPermutation());
		} catch(NumberFormatException e) {
			pbSam.dispose();
			return new AlgorithmExecutionResults(false,
					"Parameters are not valid.", null);
		}
		boolean unlog = samPanel.needUnLog();
		
		try {
			prepareInputFiles(samdir, data, numGenes, numExps, deltaInc,
					deltaMax, perm, unlog, groupAssignments);
		} catch (IOException e) {
			pbSam.dispose();
			e.printStackTrace();
			return new AlgorithmExecutionResults(false,
					"R scripts preparing is failed.", null);
		}
		
		String samOutput=samdir + "output" + FILE_SEPARATOR;
		File resultFile=new File(samOutput+"done.txt");
		if(resultFile.exists())
			resultFile.delete();
		
		String[] command = new String[] {rExe, samdir+R_SCRIPT, rLibPath, ">", (samdir+R__ERROR_LOG), "2>&1"};		
		
		try {			
			Runtime.getRuntime().exec(command);			
		} catch (Exception e) {
			pbSam.dispose();
			e.printStackTrace();
			return new AlgorithmExecutionResults(false,
					"error running R scripts.", null);
		}		
		
		while (!resultFile.exists()) {
			try {
				if (this.stopAlgorithm) {
					pbSam.dispose();
					return null;
				}
				Thread.sleep(POLL_INTERVAL);

				String err = null;
				if ((err = runError(samdir)) != null) {
					pbSam.dispose();
					return new AlgorithmExecutionResults(false,
							"Sam run got error:\n" + err, null);
				}
			} catch (InterruptedException e) {
				// no-op
			}
		}
		
		pbSam.dispose();
		
		if (this.stopAlgorithm) {
	    	return null;
		}

		AlgorithmExecutionResults results = createResultObject(samdir, maSet,
				data, deltaInc, deltaMax, context, (SAMPanel) this.getParameterPanel());
		
		return results;
	}
	
	private static void prepareInputFiles(String samdir,
			final DSMicroarraySetView<DSGeneMarker, DSMicroarray> data,
			int numGenes, int numExps, float deltaInc, float deltaMax,
			int perm, boolean unlog, int[] groupAssignments) throws IOException {
		String inputFile=samdir+"data.txt";		
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File(inputFile));
			for(int i=0;i<numGenes;i++){
				for(int j=0;j<numExps-1;j++){
					out.print((float)data.getValue(i, j)+"\t");
				}
				out.print((float)data.getValue(i, numExps-1));
				out.println();
			}			
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			out.close();
		}
		
		String clFile=samdir+"cl.txt";
		try{
			out = new PrintWriter(new File(clFile));
			for(int i=0;i<groupAssignments.length-1;i++){
				out.print(groupAssignments[i]+ "\t");
			}
			out.print(groupAssignments[groupAssignments.length-1]);
			out.println();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}
		
		String deltaFile=samdir+"delta_vec.txt";
		try{
			int deltaNo=(int) (deltaMax/deltaInc*10.0/10);
			out = new PrintWriter(new File(deltaFile));
			for(int i=0;i<deltaNo-1;i++){
				out.print(deltaInc*(i+1)+ "\t");
			}
			out.print(deltaInc*deltaNo);
			out.println();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}		
		
		String permFile=samdir+"perm.txt";
		try{
			out = new PrintWriter(new File(permFile));
			out.println(perm);			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}
		String unlogFile=samdir+"unlog.txt";
		try{
			out = new PrintWriter(new File(unlogFile));
			if(unlog)
				out.print(""+1);
			else
				out.print(""+0);			
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}

		// this throws un-handled IOExpcetion
		prepareRscripts(samdir);
	}
	
	private static void prepareRscripts(String samdir) throws IOException {
		
		PrintWriter out = null;
		try{
			out = new PrintWriter(new File(samdir+R_SCRIPT));
			out.println("samdir<-\""+samdir.replace("\\", "/")+"\"");

			InputStream input = SAMAnalysis.class.getResourceAsStream("sam_template.r");
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String line = br.readLine();
			while(line!=null) {
				out.println(line);
				line = br.readLine();
			}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}
	}
	
	private static AlgorithmExecutionResults createResultObject(String samdir,
			final DSMicroarraySet maSet,
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> data,
			float deltaInc, float deltaMax,
			DSAnnotationContext<DSMicroarray> context, SAMPanel samPanel) {
		String samOutput=samdir + "output" + FILE_SEPARATOR;

		float[] dd, dbar, pvalue, fold, fdr;
		try {
			dd = getResultFromFile(samOutput + "outd.txt");
			dbar = getResultFromFile(samOutput + "outdbar.txt");
			pvalue = getResultFromFile(samOutput + "outpvalue.txt");
			fold = getResultFromFile(samOutput + "outfold.txt");
			fdr = getResultFromFile(samOutput + "outmatfdr.txt");
		} catch (IOException e) {
			e.printStackTrace();
			return new AlgorithmExecutionResults(false,
					"Error at reading R output file!", null);
		}
		SamResultData analysisResult = new SamResultData(maSet, "SAM result",
				data, deltaInc, deltaMax, dd, dbar, pvalue, fold, fdr);
		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"SAM Analysis", analysisResult);

		// add data set history.
		String hist = samPanel.getDataSetHistory()
				+ generateGroupHistoryString(context)
				+ generateMarkerString(data);
		HistoryPanel.addToHistory(analysisResult, hist);
		
		return results;
	}
	
	private static float[] getResultFromFile(String filename) throws IOException{
		
		ArrayList<Float> list = new ArrayList<Float>();
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();// skip the header line
		line=br.readLine();
		while(line!=null && line.trim().length()>0) {
			String[] token=line.split("\\s");
			if(token.length<3){	//which means results are vector
				if(token[1].equalsIgnoreCase("Inf")) token[1]="0";	//FIXME:outfold.txt can have Inf value
				list.add( Float.parseFloat(token[1]) );
			}
			else{
				float falseValue=Float.parseFloat(token[2]);
				float calledValue=Float.parseFloat(token[3]);
				list.add( falseValue/calledValue );
			}
			line=br.readLine();
		}
		br.close();
		
		float[] out=new float[list.size()];
		for(int i=0; i<list.size(); i++) out[i] = list.get(i);
		return out;
	}	
	
	private static String generateGroupHistoryString(DSAnnotationContext<DSMicroarray> context) {
		
		String[][] labels = new String[2][];
		labels[0] = context.getLabelsForClass(CSAnnotationContext.CLASS_CASE);
		labels[1] = context
				.getLabelsForClass(CSAnnotationContext.CLASS_CONTROL);
		
		int caseSetSize = 0, controlSetSize = 0;
		String groupAndChipsString = "";

		// case
		String[] classLabels = labels[0];
		groupAndChipsString += "\t case group(s): \n";
		for (int i = 0; i < classLabels.length; i++) {
			String label = classLabels[i];
			if (context.isLabelActive(label) ) {
				caseSetSize++;
				groupAndChipsString += generateGroupAndChipsString(context
						.getItemsWithLabel(label));
			}
		}

		// control
		classLabels = labels[1];
		groupAndChipsString += "\t control group(s): \n";
		for (int i = 0; i < classLabels.length; i++) {
			String label = classLabels[i];
			if (context.isLabelActive(label) ) {
				controlSetSize++;
				groupAndChipsString += generateGroupAndChipsString(context
						.getItemsWithLabel(label));
			}
		}

		int totalSelectedGroup = caseSetSize + controlSetSize;

		groupAndChipsString = totalSelectedGroup + " groups analyzed:\n"
				+ groupAndChipsString;
		
		return groupAndChipsString;
	}

	private static String generateGroupAndChipsString(DSPanel<DSMicroarray> panel) {
		String histStr = null;

		histStr = "\t     " + panel.getLabel() + " (" + panel.size()
				+ " chips)" + ":\n";

		int aSize = panel.size();
		for (int aIndex = 0; aIndex < aSize; aIndex++)
			histStr += "\t\t" + panel.get(aIndex) + "\n";

		return histStr;
	}

	private static String generateMarkerString(
			DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> view) {
		StringBuffer histStr = new StringBuffer();

		histStr .append( view.markers().size() ).append( " markers analyzed:\n");
		for (DSGeneMarker marker : view.markers()) {
			histStr .append( "\t" + marker.getLabel() ).append( "\n");
		}

		return histStr.toString();
	}

	@Override
	public String getAnalysisName() {
		return "Sam";
	}

	@Override
	public Class<?> getBisonReturnType() {
		return SamResultData.class;
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
	protected Map<Serializable, Serializable> getBisonParameters() {
		Map<Serializable, Serializable> bisonParameters = new HashMap<Serializable, Serializable>();
		SAMPanel paramPanel = (SAMPanel) this.getParameterPanel();
		float deltaIncrement=Float.parseFloat(paramPanel.getDeltaInc());
		bisonParameters.put("deltaIncrement", deltaIncrement);
		float deltaMax=Float.parseFloat(paramPanel.getDeltaMax());
		bisonParameters.put("deltaMax", deltaMax);
		int m=Integer.parseInt(paramPanel.getPermutation());
		bisonParameters.put("m", m);
		boolean unlog=paramPanel.needUnLog();
		bisonParameters.put("unlog", unlog);
		
		bisonParameters.put("cl", groupAssignments);		
		
		return bisonParameters;
	}
	
	/* Calling this in validInputData() is sort of hack, not a clean design. */
	private void createGroupAssignments(int numExps,
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> view,
			DSAnnotationContext<DSMicroarray> context) {
		groupAssignments = new int[numExps];
		
		for (int i = 0; i < numExps; i++) {
			DSMicroarray ma = view.items().get(i);
			String[] labels = context.getLabelsForItem(ma);

			for (String label : labels) {
				if (context.isLabelActive(label) ) {
					String v = context.getClassForLabel(label);
					if (v.equals(CSAnnotationContext.CLASS_CASE)) {
						groupAssignments[i] = GROUP_CASE;
					} else if (v.equals(CSAnnotationContext.CLASS_CONTROL)) {
						groupAssignments[i] = GROUP_CONTROL;
					} else {
						groupAssignments[i] = NEITHER_GROUP;
					}
				}
			}
		}
	}

	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> view,
			DSDataSet<?> ignored) {
		SAMPanel paramPanel = (SAMPanel) this.getParameterPanel();
		
		int numExps = view.items().size();
		
		DSDataSet<? extends DSBioObject> set = view.getDataSet();

		if (!(set instanceof DSMicroarraySet)) {			
			return new ParamValidationResults(false,
					"Data is invalid.");
		}
		DSMicroarraySet maSet = (DSMicroarraySet) set;
		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<DSMicroarray> context = manager
				.getCurrentContext(maSet);

		int numCase = 0, numControl = 0;
		
		for (int i = 0; i < numExps; i++) {
			DSMicroarray ma = view.items().get(i);
			String[] labels = context.getLabelsForItem(ma);

			for (String label : labels) {
				if (context.isLabelActive(label) ) {
					String v = context.getClassForLabel(label);
					if (v.equals(CSAnnotationContext.CLASS_CASE)) {
						numCase++;						
					} else if (v.equals(CSAnnotationContext.CLASS_CONTROL)) {
						numControl++;						
					}
				}
			}
		}
		
		if (numCase == 0 && numControl == 0) {			
			return new ParamValidationResults(
					false,
					"Please activate at least one set of arrays for \"case\", and one set of arrays for \"control\".");
		} else if (numCase == 0) {			
			return new ParamValidationResults(false,
					"Please activate at least one set of arrays for \"case\".");
		} else if (numControl == 0) {			
			return new ParamValidationResults(
					false,
					"Please activate at least one set of arrays for \"control\".");
		}

		float deltaIncrement;
		try{
			deltaIncrement=Float.parseFloat(paramPanel.getDeltaInc());
			if(deltaIncrement<=0)
				return new ParamValidationResults(false,
						"Delta Increment value should be a positive number.");
		} catch (Exception e) {
			return new ParamValidationResults(false,
					"Delta Increment is invalid.");
		}
		
		float deltaMax;
		try {
			deltaMax=Float.parseFloat(paramPanel.getDeltaMax());
			if(deltaMax<=0)
				return new ParamValidationResults(false,
						"Delta Increment value should be a positive number.");
			if(deltaMax<=deltaIncrement)
				return new ParamValidationResults(false,
						"Delta Max should be great than Delta Increment value.");
		} catch (Exception e){
			return new ParamValidationResults(false,
					"Delta Max is invalid.");
		}
			
		int permutation;
		try {
			permutation=Integer.parseInt(paramPanel.getPermutation());
			if(permutation<=0)
				return new ParamValidationResults(false,
						"Number of label permutations should be a positive number.");
		} catch (Exception e){
			return new ParamValidationResults(false,
					"Number of label permutations is invalid.");
		}	

		// intentional side-effect
		createGroupAssignments(numExps, view, context);
		
		return new ParamValidationResults(true, "No, no Error");
	}//end of validInputData
	
	private static String runError(String samdir){
		StringBuilder str = new StringBuilder();
		BufferedReader br = null;
		boolean error = false;
		if (!new File(samdir+R__ERROR_LOG).exists()) return null;
		try{
			br = new BufferedReader(new FileReader(samdir+R__ERROR_LOG));
			String line = null;
			int i = 0;
			while((line = br.readLine())!=null){
				if (((i = line.indexOf("Error:"))>-1)||((i = line.indexOf("error:"))>-1)){
					str.append(line.substring(i)+"\n");
					error = true;
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				if (br!=null) br.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if (error)  return str.toString();
		return null;
	}
	
	private static boolean getStopMessageConfigure(){
		String conf = "";
		try {
			File file = new File(STOP_MESSAGE_CONF);
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				conf = br.readLine();
				br.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		if(conf.equals("") || conf.equalsIgnoreCase("false")) {
			return false; // keep the message
		} else {
			return true; // hide the message
		}
	}
	
	private static void rememberStopMessageConfigure(boolean conf){
		//save as last used conf
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter(STOP_MESSAGE_CONF));
			br.write(String.valueOf(conf));
			br.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
