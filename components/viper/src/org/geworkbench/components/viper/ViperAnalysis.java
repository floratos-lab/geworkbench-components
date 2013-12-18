package org.geworkbench.components.viper;
 
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.engine.preferences.GlobalPreferences;
import org.geworkbench.parsers.TabDelimitedDataMatrixFileFormat;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.ProgressBar;

public class ViperAnalysis extends AbstractAnalysis implements
		ClusteringAnalysis {
	 
 	private static final long serialVersionUID = -1672201775884915447L;
 	private static Log log = LogFactory.getLog(ViperAnalysis.class);
	//private final String analysisName = "Viper";

 	private static final String R_SCRIPTS="viper_starter.r";
	private static final String DEFAULT_COMPONENTS_DIR = "components";
    private static final String COMPONENTS_DIR_PROPERTY = "components.dir";
 	private static String scriptDir = null;
 	private static final String dataDir = FilePathnameUtils.getUserSettingDirectoryPath() + "viper/";
 	private static final char extSeparator = '.';
 	private static final String logExt = ".log";		//viper log file
 	private static final String expExt = ".txt";		//viper input dataset in tab-delim format
 	private static final String rmaExt = ".rma";		//viper output tfa in rma format
 	private static final String tfaExt = "_tfa.txt";	//viper output tfa in tab-delim format
 	
 	private static final String lastConf = FilePathnameUtils.getUserSettingDirectoryPath()
 			+ "viper" + FilePathnameUtils.FILE_SEPARATOR + "last.conf";
 	private ViperPanel viperPanel=new ViperPanel();
 	private static final Random random = new Random();
 	private ViperAxisClient client = null;

	public ViperAnalysis() {		
		setDefaultPanel(viperPanel);
		String componentsDir = System.getProperty(COMPONENTS_DIR_PROPERTY);
		if (componentsDir == null) {
			componentsDir = DEFAULT_COMPONENTS_DIR;
		}
		scriptDir = componentsDir + "/viper/viperScripts/";
		client = new ViperAxisClient();
	}

	@Override
	@SuppressWarnings("unchecked")
	public AlgorithmExecutionResults execute(Object input) {
		
		if (input == null || !(input instanceof DSMicroarraySetView)) {
			log.error("Invalid input type");
			return new AlgorithmExecutionResults(false, "Invalid input.", null);
		}

		DSMicroarraySetView<DSGeneMarker, DSMicroarray> data = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;

		DSMicroarraySet maSet = data.getMicroarraySet();
		if (maSet.getAnnotationFileName()==null && !intPat.matcher(maSet.getMarkers().get(0).getLabel()).matches())
			return new AlgorithmExecutionResults(false, "Please load annotation file first.", null);

		//missing marker values not allowed
		if (containsMissingValues(data))
			return new AlgorithmExecutionResults(false,
					"Microarray set contains missing values.\n"
							+ "Fix before proceeding.", null);
		
		String noShow=getLast();
		if((noShow.equals(""))||(noShow.equalsIgnoreCase("false"))){
		
			JCheckBox checkbox = new JCheckBox("Do not show this message again.");
			String message = "Viper requires that R be installed on your computer. The R location should be assigned in Tools->Preference->R location.\n" +
				    "The VIPER R package will be installed automatically if not installed yet.\n" +
				    "Do you want to continue?";
			Object[] params = {message, checkbox};
			int n = JOptionPane.showConfirmDialog(
					null,
					params,
				    "Please be aware of",
				    JOptionPane.YES_NO_OPTION);
			boolean dontShow = checkbox.isSelected();
			String s=dontShow?"True":"False";
			saveLast(s);
			
			if(n!=JOptionPane.YES_OPTION)
				return new AlgorithmExecutionResults(false, "Viper analysis: cancelled", null);
			
		}
				
		String rExe = GlobalPreferences.getInstance().getRLocation();
		if ((rExe == null)||(rExe.equals(""))) {
			//log.info("No R location configured.");
			return new AlgorithmExecutionResults(false, "Rscript.exe's location is not assigned", null);
		}		
		else{
			File rExeFile=new File(rExe);
			if(!rExeFile.exists())
				return new AlgorithmExecutionResults(false, "Rscript.exe not exist. Please check its location at Tools->Preference->R location.", null);
		}
		
		String rLibPath = "";
		String localDataDir = dataDir;
		String service = ((ViperPanel)aspp).getService();
		if (service.equals("local service")){
			rLibPath = GlobalPreferences.getInstance().getRLibPath();
		} else {
			localDataDir = dataDir + "web" + random.nextInt(Short.MAX_VALUE) + "/";
			File webDir = new File(localDataDir);
			if (!webDir.exists()) {
				if (!webDir.mkdir())
					return new AlgorithmExecutionResults(false, "Local data dir for viper cannot be created: "+localDataDir, null);
			}
			webDir.deleteOnExit();
		}
		if (rLibPath.trim().length()>0){
			File rLibFile=new File(rLibPath);
			if(!rLibFile.exists() || !rLibFile.isDirectory() || !rLibFile.canWrite())
				return new AlgorithmExecutionResults(false, "R package directory " + rLibPath+" is not valid.\nPlease leave it blank "+
						"or provide a valid writeable user directory at Tools->Preference->R package directory.", null);
		}

		ProgressBar pbar = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
		pbar.setTitle("Viper Analysis");
		pbar.setMessage("Viper analysis: started");
		pbar.start();

		String setName = maSet.getDataSetName();
		int index = setName.lastIndexOf(extSeparator);
		if (index >= 0) setName = setName.substring(0, index);

		String expFname = localDataDir + setName + expExt;
		File expFile = new File(expFname);
		writeDataset(maSet, expFile);
		if(!expFile.exists())
			return new AlgorithmExecutionResults(false, "Expset data file for viper does not exist.", null);
		
		String outFname = localDataDir + setName + rmaExt;
		
		String err = null;
		if (service.equals("web service")){
			pbar.setMessage("Viper web service: computing the association scores");
			try{
				String serviceAddress = client.findService("viper");
				if (serviceAddress == null) 
					return new AlgorithmExecutionResults(false, "cannot find viper web service.", null);
				log.info("Discovered viper web service: "+serviceAddress);
				
				err = client.executeViper(
					serviceAddress,
					expFname, 
					outFname,
					((ViperPanel) aspp).getRegulon(),
					((ViperPanel) aspp).getRegType(),
					((ViperPanel) aspp).getMethod(),
					rLibPath
				);
				if (err!=null) log.debug(err);
			}catch(AxisFault af){
				pbar.dispose();
				af.printStackTrace();
				return new AlgorithmExecutionResults(false, "error executing viper web service.", null);
			}
		}else{
		
			String[] commands = new String[]{
				rExe,
				scriptDir + R_SCRIPTS,
				scriptDir + "viper.tar.gz",
				expFname,
				outFname,
				((ViperPanel) aspp).getRegulon(),
				((ViperPanel) aspp).getRegType(),
				((ViperPanel) aspp).getMethod(),
				rLibPath
			};
			
			pbar.setMessage("Viper analysis: computing the association scores");
			String logFname = localDataDir + setName + logExt;
			File logFile = new File(logFname);
			FileOutputStream fos = null;
	        try {
				fos = new FileOutputStream(logFile);
				Process proc = Runtime.getRuntime().exec(commands);
	
				StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "INFO", fos);
				StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT", fos);
				errorGobbler.start();
				outputGobbler.start();
	
				int exitVal = proc.waitFor();
				log.info("ExitValue: " + exitVal);
				fos.flush();
			} catch (Exception t) {
				pbar.dispose();
				t.printStackTrace();
				return new AlgorithmExecutionResults(false, "error running R scripts.", null);
			}finally{
				try{
					if (fos!=null) fos.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
			
			err = runError(logFile);
		}
		File rmaFile = new File(outFname);
		if (err != null || !rmaFile.exists()){
	    	pbar.dispose();
	    	if (err!=null && err.contains("Error in install.packages"))
	    		return new AlgorithmExecutionResults(false, 
	    				"Unable to install viper package into a system directory.\n"+
	    				"Please provide a valid writeable user directory at Tools->Preference->R package directory.", null);
	    	return new AlgorithmExecutionResults(false, "Viper analysis returns no result", null);				    	
	    }

		HashMap<String, String> geneIdToMarkers = mapGeneIdToMarkers(maSet);
		String tfaFname = localDataDir + setName + tfaExt;
		File tfaFile = new File(tfaFname);
		convertRMA(rmaFile, tfaFile, geneIdToMarkers);
		if(!tfaFile.exists())
			return new AlgorithmExecutionResults(false, "Converted output tfa file does not exist.", null);
		
		pbar.setMessage("Viper analysis: reading output tfa file");
		DSMicroarraySet analysisResult = null;
		try{
			analysisResult = (DSMicroarraySet)new TabDelimitedDataMatrixFileFormat().getDataFileSkipAnnotation(tfaFile);
		}catch(Exception e){
			e.printStackTrace();
		}

		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"Viper Analysis", analysisResult);
		
		// add data set history.
		String histString = generateHistoryString(data);
		HistoryPanel.addToHistory(analysisResult, histString );
		
		pbar.dispose();
		
		return results;
	}

	private boolean containsMissingValues(
			DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> maSet) {
        if (maSet == null)
            return true;
        int markerCount = maSet.markers().size();
        DSItemList<? extends DSGeneMarker> uniqueMarkers = maSet.getUniqueMarkers();
        for (int i = 0; i < maSet.items().size(); ++i) {
            DSMicroarray mArray = maSet.get(i);
            for (int j = 0; j < markerCount; ++j)
            	if (mArray.getMarkerValue(uniqueMarkers.get(j)).isMissing())
                    return true;
        }
        return false;
	}

	private final static Pattern intPat = Pattern.compile("^\\d+$");
	private void writeDataset(DSMicroarraySet dataset, File dataFile){
		BufferedWriter bw = null;
		HashMap<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < dataset.getMarkers().size(); i++){
			int geneid = 0;
			DSGeneMarker marker = dataset.getMarkers().get(i);
			String markerName = marker.getLabel();
			//marker represented by geneid
			if (intPat.matcher(markerName).matches())
				geneid = Integer.parseInt(markerName);
			else
				geneid = marker.getGeneId();
			//ignore markers without geneid
			if (geneid == 0) continue;
			if (!map.containsKey(geneid)) map.put(geneid, new ArrayList<Integer>());
			map.get(geneid).add(i);
		}
		try{
			bw = new BufferedWriter(new FileWriter(dataFile));
			bw.write("ID");
			for (DSMicroarray array : dataset)
				bw.write("\t" + array.getLabel());
			bw.newLine();

			for (int geneid : map.keySet()){
				bw.write(String.valueOf(geneid));
				
				ArrayList<Integer> dups = map.get(geneid);
				for (DSMicroarray array : dataset){
					//average marker values for the same gene id
					double sum = 0;
					for (int i : dups){
						//missing marker values not allowed
						sum += array.getMarkerValue(i).getValue();
					}
					bw.write("\t" + sum/dups.size());
				}
				bw.newLine();
			}
			bw.flush();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if (bw!=null) bw.close();
				dataFile.deleteOnExit();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	private void convertRMA(File rmaFile, File tfaFile, HashMap<String, String> geneIdToMarkers){
		BufferedWriter bw = null;
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(rmaFile));
			bw = new BufferedWriter(new FileWriter(tfaFile));
			String line = br.readLine();
			bw.write("AffyID");
			bw.write(line);
			bw.newLine();
			while((line=br.readLine())!=null){
				String[] toks = line.split("\t", 2);
				String marker = geneIdToMarkers.get(toks[0].trim());
				if (marker == null){
					log.warn("unable to find gene symbol for entrez id: "+toks[0]);
				}else{
					bw.write(marker + "\t" + toks[1]);
					bw.newLine();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if (br!=null) br.close();
				if (bw!=null) bw.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		if (!rmaFile.delete()) rmaFile.deleteOnExit();
		tfaFile.deleteOnExit();
	}
	
	private HashMap<String, String> mapGeneIdToMarkers(DSMicroarraySet dataset){
		HashMap<String, String> geneIdToMarkers = new HashMap<String, String>();
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(scriptDir+"entrez_to_symbol.tab"));
			String line = br.readLine();
			while((line = br.readLine())!=null){
				String[] toks = line.split("\t", 2);
				String entrez = toks[0].trim();
				String symbol = toks[1].trim();
				if (entrez.length()>0)
					geneIdToMarkers.put(entrez, symbol);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if (br!=null) br.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return geneIdToMarkers;
	}

	private String generateHistoryString(DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
		StringBuffer histStr = new StringBuffer();

		histStr.append(aspp.getDataSetHistory());
		histStr.append(generateHistoryForMaSetView(view));
		
		return histStr.toString();
	}

	private String getLast(){
		String conf = "";
		try {
			File file = new File(lastConf);
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				conf = br.readLine();
				br.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return conf;
	}
	private void saveLast(String conf){
		//save as last used conf
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter(lastConf));
			br.write(conf);
			br.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private String runError(File logFile){
		StringBuilder str = new StringBuilder();
		BufferedReader br = null;
		boolean error = false;
		if (!logFile.exists()) return null;
		try{
			br = new BufferedReader(new FileReader(logFile));
			String line = null;
			int i = 0;
			while((line = br.readLine())!=null){
				if (((i = line.indexOf("Error"))>-1)){
					str.append(line.substring(i)+"\n");
					error = true;
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				if (br!=null) br.close();
				logFile.deleteOnExit();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if (error)  return str.toString();
		return null;
	}

	/*
	@Override
	public String getAnalysisName() {
		return analysisName;
	}

	@Override
	protected Map<Serializable, Serializable> getBisonParameters() {
		Map<Serializable, Serializable> parameterMap = new HashMap<Serializable, Serializable>();

		String method = ((ViperPanel) aspp).getMethod();
		parameterMap.put("Method", method);

		String regulon = ((ViperPanel) aspp).getRegulon();
		parameterMap.put("Regulon", regulon);

		return parameterMap;
	}

	@Override
	public Class<?> getBisonReturnType() {
		return DSMicroarraySet.class;
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
		if (maSetView == null)
			return new ParamValidationResults(false, "Invalid input.");
		assert maSetView instanceof DSMicroarraySetView;

		int numMAs = maSetView.items().size();
		int numMarkers = maSetView.getUniqueMarkers().size();

		// warning danger of out of memory error
		final int LARGE_SET_SIZE = 13000;
		String setTooLarge = null;
		if(numMAs>LARGE_SET_SIZE) {
			setTooLarge = "Microarray set size "+numMAs;
		}
		if(numMarkers>LARGE_SET_SIZE) {
			setTooLarge = "Gene Marker set size "+numMarkers;
		}
		if(setTooLarge!=null) {
			int n = JOptionPane.showConfirmDialog(null,
					setTooLarge+" is very large and may cause out-of-memory error.\n Do you want to continue?",
				    "Too large set",
				    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (n != JOptionPane.YES_OPTION) {
				return new ParamValidationResults(false, "You chose to cancel because marker set is too large.");
			}
		}

		return new ParamValidationResults(true,"No Error");
	}//end of validInputData
	*/
	
	public static class StreamGobbler extends Thread
	{
	    private InputStream is;
	    private String type;
	    private OutputStream os;
	    
	    StreamGobbler(InputStream is, String type)
	    {
	        this(is, type, null);
	    }
	    StreamGobbler(InputStream is, String type, OutputStream redirect)
	    {
	        this.is = is;
	        this.type = type;
	        this.os = redirect;
	    }
	    
	    public void run()
	    {
            PrintWriter pw = null;
            BufferedReader br = null;
	        try {
	            if (os != null)
	                pw = new PrintWriter(os, true);
	                
	            InputStreamReader isr = new InputStreamReader(is);
	            br = new BufferedReader(isr);
	            String line=null;
	            while ( (line = br.readLine()) != null)
	            {
	                if (pw != null){
	                    pw.println(line);
	                }
	                System.out.println(type + ">" + line);    
	            }
	        } catch (IOException ioe) {
	            ioe.printStackTrace();  
	        } finally {
	        	try{
		        	if (pw!=null) pw.close();
	        		if (br!=null) br.close();
	            }catch(Exception e){
	            	e.printStackTrace();
	            }
	        }
	    }
	}
}
