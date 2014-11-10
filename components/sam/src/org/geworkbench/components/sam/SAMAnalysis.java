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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

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
		ClusteringAnalysis, Observer {

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
 	
	private final String rScriptTemplate;

	private int[] groupAssignments;	// FIXME

 	public SAMAnalysis() throws IOException {		
		setDefaultPanel(new SAMPanel());
		
		InputStream input = this.getClass().getResourceAsStream("sam_template.r");
		BufferedReader br = new BufferedReader(new InputStreamReader(input));
		String line = br.readLine();
		StringBuffer sb = new StringBuffer();
		while(line!=null) {
			sb.append(line).append('\n');
			line = br.readLine();
		}
		rScriptTemplate = sb.toString();
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
		
		groupAssignments = new int[numExps];
		
		DSDataSet<? extends DSBioObject> set = data.getDataSet();

		if (!(set instanceof DSMicroarraySet)) {			
			return null;
		}		
		
		String currdir=System.getProperty("user.dir");		
		String predir=currdir+FILE_SEPARATOR+"data"+FILE_SEPARATOR+"sam";
		
		File prefile=new File(predir);
		if(!prefile.exists()){
			if(!(prefile ).mkdir())
				return new AlgorithmExecutionResults(false, "Cannot create directory at "+predir, null);
		}
		String samdir=predir+FILE_SEPARATOR;
		
		if(!getStopMessageConfigure()){
		
			JCheckBox checkbox = new JCheckBox("Do not show this message again.");
			String message = "SAM requires R installed on your computer. R location should be assigned in Tools->Preference->R location.\n" +
				    "R package of SAM is also required which will be installed automatically if not installed yet.\n" +
				    "Do you want to continue?";
			Object[] params = {message, checkbox};
			int n = JOptionPane.showConfirmDialog(
					null,
					params,
				    "Pleas be aware of",
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

		int numCase = 0, numControl = 0;
		
		for (int i = 0; i < numExps; i++) {
			DSMicroarray ma = data.items().get(i);
			String[] labels = context.getLabelsForItem(ma);

			for (String label : labels) {
				if (context.isLabelActive(label) ) {
					String v = context.getClassForLabel(label);
					if (v.equals(CSAnnotationContext.CLASS_CASE)) {
						groupAssignments[i] = GROUP_CASE;
						numCase++;						
					} else if (v.equals(CSAnnotationContext.CLASS_CONTROL)) {
						groupAssignments[i] = GROUP_CONTROL;
						numControl++;						
					} else {
						groupAssignments[i] = NEITHER_GROUP;
					}
				}
			}
		}		
		//System.out.println("numCase="+numCase+"\tnumberGroupB="+numControl);
		
		if (numCase == 0 && numControl == 0) {
			pbSam.dispose();
			return new AlgorithmExecutionResults(
					false,
					"Please activate at least one set of arrays for \"case\", and one set of arrays for \"control\".",
					null);
		}
		if (numCase == 0) {
			pbSam.dispose();
			return new AlgorithmExecutionResults(false,
					"Please activate at least one set of arrays for \"case\".",
					null);
		}
		if (numControl == 0) {
			pbSam.dispose();
			return new AlgorithmExecutionResults(
					false,
					"Please activate at least one set of arrays for \"control\".",
					null);
		}
		
		String[][] labels = new String[2][];
		labels[0] = context.getLabelsForClass(CSAnnotationContext.CLASS_CASE);
		labels[1] = context
				.getLabelsForClass(CSAnnotationContext.CLASS_CONTROL);
		HashSet<String> caseSet = new HashSet<String>();
		HashSet<String> controlSet = new HashSet<String>();

		String groupAndChipsString = "";

		// case
		String[] classLabels = labels[0];
		groupAndChipsString += "\t case group(s): \n";
		for (int i = 0; i < classLabels.length; i++) {
			String label = classLabels[i];
			if (context.isLabelActive(label) ) {
				caseSet.add(label);
				groupAndChipsString += GenerateGroupAndChipsString(context
						.getItemsWithLabel(label));
			}
		}

		// control
		classLabels = labels[1];
		groupAndChipsString += "\t control group(s): \n";
		for (int i = 0; i < classLabels.length; i++) {
			String label = classLabels[i];
			if (context.isLabelActive(label) ) {
				controlSet.add(label);
				groupAndChipsString += GenerateGroupAndChipsString(context
						.getItemsWithLabel(label));
			}
		}

		int totalSelectedGroup = caseSet.size() + controlSet.size();
		String histMarkerString = GenerateMarkerString(data);

		groupAndChipsString = totalSelectedGroup + " groups analyzed:\n"
				+ groupAndChipsString;
				
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
		}catch (FileNotFoundException e) {
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
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}		
		
		String permFile=samdir+"perm.txt";
		try{
			out = new PrintWriter(new File(permFile));
			out.println(perm);			
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}
		String unlogFile=samdir+"unlog.txt";
		try{
			out = new PrintWriter(new File(unlogFile));
			if(samPanel.needUnLog())
				out.print(""+1);
			else
				out.print(""+0);			
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}
		
		try{
			prepareRscripts(samdir);
		}
		catch(IOException e){
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
		
		while(!resultFile.exists()){			
			 try{
				    if (this.stopAlgorithm == true)
				    	return null;
			    	Thread.sleep(POLL_INTERVAL);			    	
			    	
			    	String err = null;
				    if ((err = runError(samdir)) != null){
				    	pbSam.dispose();
				    	return new AlgorithmExecutionResults(false,
				    			"Sam run got error:\n"+err, null);				    	
				    }
				}
			 catch(InterruptedException e){
			    	
			    }		
		}		
		
	 	float[] dd, dbar, pvalue, fold, fdr;
		try {
			dd=getResultFromFile(samOutput+"outd.txt");
			dbar=getResultFromFile(samOutput+"outdbar.txt");
			pvalue=getResultFromFile(samOutput+"outpvalue.txt");
			fold=getResultFromFile(samOutput+"outfold.txt");
			fdr=getResultFromFile(samOutput+"outmatfdr.txt");
		} catch (IOException e) {
			pbSam.dispose();			
			e.printStackTrace();
			return new AlgorithmExecutionResults(false,
					"Error at reading R output file!", null);
		}
		
		
		if (this.stopAlgorithm == true)
	    	return null;
		SamResultData analysisResult=new SamResultData(maSet,"SAM result", data,
				deltaInc, deltaMax,	dd, dbar, pvalue, fold, fdr);
		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"SAM Analysis", analysisResult);
		
		// add data set history.
		String hist = generateHistoryString(groupAndChipsString, histMarkerString);
		HistoryPanel.addToHistory(analysisResult, hist);
		
		pbSam.dispose();
		
		return results;
	}
	
	private void prepareRscripts(String samdir) throws IOException{
		
		PrintWriter out = null;
		try{
			out = new PrintWriter(new File(samdir+R_SCRIPT));
			out.println("samdir<-\""+samdir.replace("\\", "/")+"\"");
			out.println(rScriptTemplate);
			
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}
	}
	
	private static float[] getResultFromFile(String filename) throws IOException{
		
		int rowTotal=0;		
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while(line!=null && line.trim().length()>0) {			
			rowTotal++;
			line=br.readLine();	
		}
		br.close();
		rowTotal--; // skip the header line
		float[] out=new float[rowTotal];
		
		int i=0;
		br = new BufferedReader(new FileReader(filename));
		line = br.readLine();// skip the header line
		line=br.readLine();
		while(line!=null && line.trim().length()>0&&i<rowTotal) {			
			String[] token=line.split("\\s");
			if(token.length<3){	//which means results are vector
				if(token[1].equalsIgnoreCase("Inf")) token[1]="0";	//FIXME:outfold.txt can have Inf value
				out[i]=Float.parseFloat(token[1]);
			}
			else{
				float falseValue=Float.parseFloat(token[2]);
				float calledValue=Float.parseFloat(token[3]);
				out[i]=falseValue/calledValue;
			}
			line=br.readLine();
			i++;			
		}
		br.close();
		
		return out;
	}	
	
	private String generateHistoryString(String groupAndChipsString, String histMarkerString) {
		String histStr = ((SAMPanel) this.getParameterPanel())
				.getDataSetHistory();
		return histStr+groupAndChipsString+histMarkerString;
	}

	private String GenerateGroupAndChipsString(DSPanel<DSMicroarray> panel) {
		String histStr = null;

		histStr = "\t     " + panel.getLabel() + " (" + panel.size()
				+ " chips)" + ":\n";

		int aSize = panel.size();
		for (int aIndex = 0; aIndex < aSize; aIndex++)
			histStr += "\t\t" + panel.get(aIndex) + "\n";

		return histStr;
	}

	private String GenerateMarkerString(
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
		// TODO Auto-generated method stub
		return "Sam";
	}

	@Override
	public Class<?> getBisonReturnType() {
		return SamResultData.class;
	}	

	@Override
	protected boolean useMicroarraySetView() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean useOtherDataSet() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Map<Serializable, Serializable> getBisonParameters() {
		Map<Serializable, Serializable> bisonParameters = new HashMap<Serializable, Serializable>();
		SAMPanel paramPanel = (SAMPanel) this.aspp;
		float deltaIncrement=Float.parseFloat(paramPanel.getDeltaInc());
		bisonParameters.put("deltaIncrement", deltaIncrement);
		float deltaMax=Float.parseFloat(paramPanel.getDeltaMax());
		bisonParameters.put("deltaMax", deltaMax);
		int m=Integer.parseInt(paramPanel.getPermutation());
		bisonParameters.put("m", m);
		boolean unlog=paramPanel.needUnLog();
		bisonParameters.put("unlog", unlog);
		
		int[] cl=groupAssignments;
		bisonParameters.put("cl", cl);		
		
		return bisonParameters;
	}

	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet<?> refMASet) {
		if (aspp == null)
			return new ParamValidationResults(true, null);
		// Use this to get params
		SAMPanel paramPanel = (SAMPanel) aspp;
		
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = maSetView;
		
		int numExps = view.items().size();
		groupAssignments = new int[numExps];
		
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
						groupAssignments[i] = GROUP_CASE;
						numCase++;						
					} else if (v.equals(CSAnnotationContext.CLASS_CONTROL)) {
						groupAssignments[i] = GROUP_CONTROL;
						numControl++;						
					} else {
						groupAssignments[i] = NEITHER_GROUP;
					}
				}
			}
		}
		
		if (numCase == 0 && numControl == 0) {			
			return new ParamValidationResults(
					false,
					"Please activate at least one set of arrays for \"case\", and one set of arrays for \"control\".");
		}
		if (numCase == 0) {			
			return new ParamValidationResults(false,
					"Please activate at least one set of arrays for \"case\".");
		}
		if (numControl == 0) {			
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
		}
		catch (Exception e){
			return new ParamValidationResults(false,
					"Delta Increment is invalid.");
		}
		
		float deltaMax;
		try{
			deltaMax=Float.parseFloat(paramPanel.getDeltaMax());
			if(deltaMax<=0)
				return new ParamValidationResults(false,
						"Delta Increment value should be a positive number.");
			if(deltaMax<=deltaIncrement)
				return new ParamValidationResults(false,
						"Delta Max should be great than Delta Increment value.");
		}
		catch (Exception e){
			return new ParamValidationResults(false,
					"Delta Max is invalid.");
		}
			
		int permutation;
		try{
			permutation=Integer.parseInt(paramPanel.getPermutation());
			if(permutation<=0)
				return new ParamValidationResults(false,
						"Number of label permutations should be a positive number.");
		}
		catch (Exception e){
			return new ParamValidationResults(false,
					"Number of label permutations is invalid.");
		}	
			
		
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
	
	private boolean getStopMessageConfigure(){
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
	
	private void rememberStopMessageConfigure(boolean conf){
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
