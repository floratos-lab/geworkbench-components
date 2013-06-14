package org.geworkbench.components.demand;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSDemandResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSDemandResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.builtin.projects.DataSetNode;
import org.geworkbench.builtin.projects.DataSetSubNode;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.preferences.GlobalPreferences;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.ProgressBar;

public class DemandAnalysis extends AbstractAnalysis implements
		ClusteringAnalysis {

	private static final long serialVersionUID = 9038993421927784989L;
	private static final Log log = LogFactory.getLog(DemandAnalysis.class);
	private DemandPanel demandPanel = new DemandPanel();

	private static final String PROBE_SET="Probe Set ID";
	private static final String GENE_SYMBOL="Gene Symbol";
	private static final String CHROMO_LOCATION="Chromosomal Location";
	private static final String ENTREZ_GENE="Entrez Gene";
	private static final String DEFAULT_COMPONENTS_DIR = "components";
    private static final String COMPONENTS_DIR_PROPERTY = "components.dir";
    private static final String analysisName = "demand";
 	private static String scriptDir = null;
 	private static final String dataDir = FilePathnameUtils.getUserSettingDirectoryPath() + analysisName + "/";
 	private static final char extSeparator = '.';
 	/* input/output/log files for demand R runs */
 	private static final String R_SCRIPTS=	"DMAND.R";			//R script
 	private static final String logExt	=	".log";				//log file
 	private static final String expExt	=	".exp";				//input exp dataset file
 	private static final String spExt	=	"_sample.info.txt";	//input sample info
 	private static final String annoExt	=	"_annot.csv";		//input annotation
 	private static final String nwExt	=	"_network.txt";		//input network in lab format
 	private static final String resFile	=	"DMAND_result.txt";	//result file
 	private static final String resEdge	=	"KL_edge.txt";		//result edge file
 	private static final String resMod	=	"Module.txt";		//result module file
 	private static final String parName	=	"parameter.txt";	//configuration file
 	private static final String resDir	="result_Geldanamycin/";//result directory
	private static final int resultStrCol = 2, edgeStrCol = 2, modStrCol = 3;
 	
 	private static final String lastConf = FilePathnameUtils.getUserSettingDirectoryPath()
 			+ analysisName + FilePathnameUtils.FILE_SEPARATOR + "last.conf";
 	private static final Random random = new Random();
 	private DemandAxisClient client = null;
 	
	public DemandAnalysis(){
		setDefaultPanel(demandPanel);
		String componentsDir = System.getProperty(COMPONENTS_DIR_PROPERTY);
		if (componentsDir == null) {
			componentsDir = DEFAULT_COMPONENTS_DIR;
		}
		scriptDir = componentsDir + "/" + analysisName + "/demandScripts/";
		client = new DemandAxisClient();
	}


	@SuppressWarnings("unchecked")
	@Override
	public AlgorithmExecutionResults execute(Object input) {
		
		if (input == null || !(input instanceof DSMicroarraySetView))
			return new AlgorithmExecutionResults(false, "Invalid input.", null);
		
		Set<Integer> drugSet = demandPanel.getDrugSet();
		Set<Integer> ctrlSet = demandPanel.getCtrlSet();
		if (drugSet.isEmpty() || ctrlSet.isEmpty()) 
			return new AlgorithmExecutionResults(false, "Arrays defining phenotype are required.", null);

		DSMicroarraySetView<DSGeneMarker, DSMicroarray> data = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
		DSMicroarraySet maSet = data.getMicroarraySet();
		if (maSet.getAnnotationFileName()==null)
			return new AlgorithmExecutionResults(false, "Please load annotation file first.", null);

		//missing marker values not allowed
		if (containsMissingValues(data))
			return new AlgorithmExecutionResults(false,
					"Microarray set contains missing values.\n"
							+ "Remove before proceeding.", null);
		
		HashMap<String, ArrayList<String>> nwMap = null;
		if (!demandPanel.useLabFormat() && (nwMap = getNetwork()).isEmpty())
			return new AlgorithmExecutionResults(false, "No valid network loaded.", null);
		
		String noShow=getLast();
		if((noShow.equals(""))||(noShow.equalsIgnoreCase("false"))){
		
			JCheckBox checkbox = new JCheckBox("Do not show this message again.");
			String message = "Demand requires R installed on your computer. R location should be assigned in Tools->Preference->R location.\n" +
				    "R package of Demand is also required which will be installed automatically if not installed yet.\n" +
				    "Do you want to continue?";
			Object[] params = {message, checkbox};
			int n = JOptionPane.showConfirmDialog(
					null,
					params,
				    "Pleas be aware of",
				    JOptionPane.YES_NO_OPTION);
			boolean dontShow = checkbox.isSelected();
			String s=dontShow?"True":"False";
			saveLast(s);
			
			if(n!=JOptionPane.YES_OPTION)
				return new AlgorithmExecutionResults(false, "Demand analysis: cancelled", null);
		}
				
		String rExe = GlobalPreferences.getInstance().getRLocation();
		if ((rExe == null)||(rExe.equals(""))) {
			log.info("No R location configured.");
			return new AlgorithmExecutionResults(false, "Rscript.exe's location is not assigned", null);
		}		
		else{
			File rExeFile=new File(rExe);
			if(!rExeFile.exists())
				return new AlgorithmExecutionResults(false, "Rscript.exe not exist. Please check it's location at Tools->Preference->R location.", null);
		}
		
		String localDataDir = dataDir;
		String service = ((DemandPanel)aspp).getService();
		if (service.equals("web service")){
			localDataDir = dataDir + "web" + random.nextInt(Short.MAX_VALUE) + "/";
			File webDir = new File(localDataDir);
			if (!webDir.exists() && !webDir.mkdir())
				return new AlgorithmExecutionResults(false, "Local data dir for demand cannot be created: "+localDataDir, null);
			webDir.deleteOnExit();
		}

		ProgressBar pbar = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
		pbar.setTitle("Demand Analysis");
		pbar.setMessage("Demand analysis: preparing input files for R");
		pbar.start();

		String setName = maSet.getDataSetName();
		setName = setName.replaceAll(" ", "_");
		int index = setName.lastIndexOf(extSeparator);
		if (index >= 0) setName = setName.substring(0, index);
		
		String expFname = localDataDir + setName + expExt;
		File expFile = new File(expFname);
		writeExpset(maSet, expFile);
		if(!expFile.exists())
			return new AlgorithmExecutionResults(false, "Expset data file for demand does not exist.", null);
		
		String nwFname = localDataDir + setName + nwExt;
		File nwFile = new File(nwFname);
		if (demandPanel.useLabFormat()){
			nwFname = demandPanel.getLoadedNetworkFileName();
			nwFile = new File(nwFname);
		}else 
			writeNetwork(nwFile, nwMap);		
		if(!nwFile.exists())
			return new AlgorithmExecutionResults(false, "Network file for demand does not exist.", null);

		String annoFname = localDataDir + setName + annoExt;
		File annoFile = new File(annoFname);
		writeAnnoFile(maSet, annoFile);
		if(!annoFile.exists())
			return new AlgorithmExecutionResults(false, "Annotation file for demand does not exist.", null);
		
		String spFname = localDataDir + setName + spExt;
		File spFile = new File(spFname);
		writeSampleInfo(spFile, drugSet, ctrlSet);
		if(!spFile.exists())
			return new AlgorithmExecutionResults(false, "Sample info file for demand does not exist.", null);

		String resultDir = localDataDir + resDir;
		File rDir = new File(resultDir);
		if (!rDir.exists() && !rDir.mkdir())
			return new AlgorithmExecutionResults(false, "Local result dir for demand cannot be created: "+resultDir, null);
		rDir.deleteOnExit();

		String err = null;
		if (service.equals("web service")){
			pbar.setMessage("Demand web service: computing results");
			try{
				String serviceAddress = client.findService("demand");
				if (serviceAddress == null) 
					return new AlgorithmExecutionResults(false, "cannot find demand web service.", null);
				log.info("Discovered demand web service: "+serviceAddress);
				
				err = client.executeDemand(
						serviceAddress, setName, expFname, nwFname, annoFname, spFname, resultDir
				);
				if (err!=null) log.debug(err);
			}catch(AxisFault af){
				pbar.dispose();
				af.printStackTrace();
				return new AlgorithmExecutionResults(false, "error executing demand web service.", null);
			}
		}else{
		
			String paramFname = localDataDir + parName;
			File paramFile = new File(paramFname);
			writeParamFile(paramFile, expFname, nwFname, annoFname, spFname, resultDir);

			String[] commands = new String[]{
					rExe,
					scriptDir + R_SCRIPTS,
					paramFname};
			
			pbar.setMessage("Demand analysis: computing results");
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
		String resFname 	=	resultDir + resFile;
		File resultFile 	=	new File(resFname);
		String resEdgeFname	=	resultDir + resEdge;
		File resultEdgeFile	=	new File(resEdgeFname);
		String resModFname	=	resultDir + resMod;
		File resultModFile	=	new File(resModFname);
		new File(resultDir).deleteOnExit();

		if (err != null || !resultFile.exists()){
	    	pbar.dispose();
	    	return new AlgorithmExecutionResults(false, "Demand analysis returns no result", null);				    	
	    }
		
		pbar.setMessage("Demand analysis: reading output file");
		Object[][] rdata = getResult(resultFile, resultStrCol);
		Object[][] edata = getResult(resultEdgeFile, edgeStrCol);
		Object[][] mdata = getResult(resultModFile, modStrCol);
		
		DSDemandResultSet analysisResult = new CSDemandResultSet(maSet, "DEMAND Result",
				rdata, edata, mdata);
		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"Demand Analysis", analysisResult);
		
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
	
	private void writeExpset(DSMicroarraySet dataset, File dataFile){
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(dataFile));
			bw.write("AffyID\tAnnotation");
			for (DSMicroarray array : dataset)
				bw.write("\t" + array.getLabel());
			bw.newLine();

			for (int i = 0; i < dataset.getMarkers().size(); i++){
				DSGeneMarker marker = dataset.getMarkers().get(i);
				bw.write(marker.getLabel() + "\t" + marker.getGeneName());
				for (DSMicroarray array : dataset){
					//missing marker values not allowed
					bw.write("\t" + (float)array.getMarkerValue(i).getValue());
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
	
	private void writeSampleInfo(File sampleFile, Set<Integer> drug, Set<Integer> ctrl){
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(sampleFile));
			bw.write("Drug");
			for (int i : drug) bw.write("\t"+i);
			bw.newLine();
			bw.write("Ctrl");
			for (int i : ctrl) bw.write("\t" +i);
			bw.newLine();
			bw.flush();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if (bw!=null) bw.close();
				sampleFile.deleteOnExit();
			}catch(IOException e){
				e.printStackTrace();
			}
		}	
	}

	// write simplified annotation file: Entrez.Gene,Gene.Symbol,Chromosomal.Location
	private void writeAnnoFile(DSMicroarraySet dataset, File annoFile){
		Scanner in = null;
		BufferedWriter bw = null;
		//map entrez to list of geneSymbol,chromLoc
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		String originalAnnoFname = dataset.getAnnotationFileName();
		try {
			in = new Scanner(new BufferedReader(new FileReader(originalAnnoFname)));			
			int geneSymbolCol=14;
			int chromoCol=15;
			int entrezCol=18;
			boolean headLineProcessed=false;
			while (in.hasNextLine()) {
				String line = in.nextLine();
				char firstChar=line.charAt(0);	//the following lines parsing the annotation file
				if(!Character.toString(firstChar).equals("#")){//remove the line begin with #
					String[] tokens = line.split("\",\"");
					//only columns EntrezGene,GeneSymbol,ChromosomalLocation are picked up
					if(!headLineProcessed){
						if(tokens[0].indexOf(PROBE_SET)!=-1){//means the head line without comments
							for(int i=0;i<tokens.length;i++){								
								if(tokens[i].equalsIgnoreCase(GENE_SYMBOL))
									geneSymbolCol=i;
								if(tokens[i].equalsIgnoreCase(CHROMO_LOCATION))
									chromoCol=i;
								if(tokens[i].equalsIgnoreCase(ENTREZ_GENE))
									entrezCol=i;								
							}
							headLineProcessed=true;
						}
					}else{
						String oneLine = tokens[geneSymbolCol] + "," + tokens[chromoCol];
						String entrez = tokens[entrezCol];
						addMap(map, entrez, oneLine);
					}
				}
			}
			bw = new BufferedWriter(new FileWriter(annoFile));
			bw.write("Entrez.Gene,Gene.Symbol,Chromosomal.Location");
			bw.newLine();
			for (String entrez : map.keySet()){
				for (String str : map.get(entrez)){
					bw.write(entrez+","+str);
					bw.newLine();
				}
			}
			bw.flush();
		} catch (FileNotFoundException e) {
			log.error("Annotation file is not valid. Please load it first.");
		} catch (IOException e){
			e.printStackTrace();
		} finally{
			if (in!=null) in.close();
			try{
				if (bw!=null) bw.close();
				annoFile.deleteOnExit();
			}catch(IOException e){
				e.printStackTrace();
			}
		}	
	}

	private HashMap<String, ArrayList<String>> getNetwork(){
		//map gene1 to list of gene2\tpdi\tppi\tDir
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();

		AdjacencyMatrixDataSet amSet = demandPanel.getSelectedAdjSet();
		if (amSet==null) return map;
		AdjacencyMatrix matrix  = amSet.getMatrix();
		if (matrix==null) return map;
		
		DSMicroarraySet microarraySet = (DSMicroarraySet) amSet.getParentDataSet();
		for (AdjacencyMatrix.Node node1 : matrix.getNodes()) {
			DSGeneMarker marker1 = getMarkerInNode(node1, microarraySet);

			//ignore markers without geneid
			if (marker1 != null && marker1.getGeneId() > 0) {
				for (AdjacencyMatrix.Edge edge : matrix.getEdges(node1)) {
					DSGeneMarker marker2 = getMarkerInNode(edge.node2, microarraySet);
					if (marker2 != null && marker2.getGeneId() > 0) {
						int pdi = 1, ppi = 0;
						if (edge.info.type!=null && edge.info.type.contains("pp")){
							pdi = 0; ppi = 1;
						}
						//FIXME: pdi,ppi,Dir default 1,0,0
						String str = marker2.getGeneId()+"\t"+pdi+"\t"+ppi+"\t0";
						String entrez = String.valueOf(marker1.getGeneId());
						addMap(map, entrez, str);
					}
				}
			}
		}
		return map;
	}
	
	//add unique key->val entry to map
	private void addMap(HashMap<String, ArrayList<String>> map, String key, String val){
		if (!map.containsKey(key)){
			ArrayList<String> list = new ArrayList<String>();
			list.add(val);
			map.put(key, list);
		}else {
			ArrayList<String> list = map.get(key);
			boolean exist = false;
			for (String str : list){
				if (str.equals(val)) exist = true;
			}
			if (!exist) list.add(val);
		}
	}
	
	private DSGeneMarker getMarkerInNode(AdjacencyMatrix.Node node, DSMicroarraySet microarraySet){
		if (node == null ) return null;
		DSGeneMarker marker = null;
		if (node.type == NodeType.MARKER) 
			marker = node.getMarker();
		else 
			marker = microarraySet.getMarkers().get(node.stringId);
		return marker;
	}

	private void writeNetwork(File networkFile, HashMap<String, ArrayList<String>> map){
		if (map.isEmpty()) return;
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(networkFile));
			bw.write("Gene1\tGene2\tpdi\tppi\tDir");
			bw.newLine();
			for (String gene1 : map.keySet()){
				for (String gene2 : map.get(gene1)){
					bw.write(gene1 + "\t" + gene2);  
					bw.newLine();
				}
			}
			bw.flush();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if (bw!=null) bw.close();
				networkFile.deleteOnExit();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	private void writeParamFile(File paramFile, String expFname, String nwFname, String annoFname, String spFname, String rsltDir){
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(paramFile));
			bw.write("parameter\tvalue");
			bw.newLine();
			bw.write("expfile\t"+expFname);
			bw.newLine();
			bw.write("network_file\t"+nwFname);
			bw.newLine();
			bw.write("ANNOTFILE\t"+annoFname);
			bw.newLine();
			bw.write("phenotypefile\t"+spFname);
			bw.newLine();
			bw.write("DIR\t"+rsltDir);
			bw.newLine();
			bw.flush();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if (bw!=null) bw.close();
				paramFile.deleteOnExit();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	@Subscribe
	public void receive(org.geworkbench.events.PhenotypeSelectorEvent<DSMicroarray> e,
			Object source) {
		if (e.getTaggedItemSetTree() != null) {
			DSPanel<DSMicroarray> activatedArrays = e.getTaggedItemSetTree();
			demandPanel.setSelectorPanelForArray(activatedArrays);
		}else
			log.debug("Demand Received Microarray Selector Event: Selection panel sent was null");
	}
	
	@Subscribe
	public void receive(org.geworkbench.events.ProjectNodeRemovedEvent e,
			Object source) {
		DSDataSet<?> dataSet = e.getAncillaryDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			demandPanel.removeAdjMatrixToCombobox((AdjacencyMatrixDataSet) dataSet);
		}
	}

	@Subscribe
	public void receive(org.geworkbench.events.ProjectNodeRenamedEvent e,
			Object source) {
		DSDataSet<?> dataSet = e.getDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			demandPanel.renameAdjMatrixToCombobox((AdjacencyMatrixDataSet)dataSet, e.getOldName(),e.getNewName());
		}
	}
	
	@Subscribe
	public void receive(org.geworkbench.events.CCMUpdateEvent event, Object source) {
		DataSetNode selectedDataSetNode = ProjectPanel.getInstance().getSelection().getSelectedDataSetNode();
		if (selectedDataSetNode != null){
			DSDataSet<?> dataSet = selectedDataSetNode.getDataset();
			if (dataSet instanceof DSMicroarraySet)
				demandPanel.setMicroarraySet((DSMicroarraySet)dataSet);
		}
	}

	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
		DSDataSet<?> dataSet = e.getDataSet();
		if (dataSet == null || !(dataSet instanceof DSMicroarraySet)) return;

		demandPanel.setMicroarraySet((DSMicroarraySet)dataSet);

		ProjectSelection selection = ProjectPanel.getInstance().getSelection();
        DataSetNode dNode = selection.getSelectedDataSetNode();
        if(dNode == null) return;

        String currentTargetSet = demandPanel.getSelectedAdjMatrix();
        demandPanel.clearAdjMatrixCombobox();

        Enumeration<?> children = dNode.children();
        while (children.hasMoreElements()) {
            Object obj = children.nextElement();
            if (obj instanceof DataSetSubNode) {
                DSAncillaryDataSet<?> ads = ((DataSetSubNode) obj)._aDataSet;
                if (ads instanceof AdjacencyMatrixDataSet) {
                	demandPanel.addAdjMatrixToCombobox((AdjacencyMatrixDataSet) ads);                    
                    if (currentTargetSet != null && StringUtils.equals(ads.getDataSetName(), currentTargetSet.trim())) {
                    	demandPanel.setSelectedAdjMatrix(ads.getDataSetName());
        			}
                }
            }
        }
        log.debug("ProjectEvent processed");
	}
	
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
	
	//dblStart: double start column
	private Object[][] getResult(File resFile, int dblStart) {
		ArrayList<String[]> data = new ArrayList<String[]>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(resFile));
			String line = br.readLine();// skip table header
			while ((line = br.readLine()) != null) {
				data.add(line.split("\t"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
				resFile.deleteOnExit();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (data.size() == 0) return new Object[0][0];

		Object[][] result = new Object[data.size()][data.get(0).length];
		for (int r = 0; r < data.size(); r++){
			for (int c = 0; c < data.get(0).length; c++){
				String val = data.get(r)[c];
				if (c >= dblStart){
					try{
						result[r][c] = Double.parseDouble(val);
					}catch(NumberFormatException e){
						//FIXME: c("0","0") in Dir column of some Module.txt
						log.info(e);
						result[r][c] = 0d;
					}
				}else result[r][c] = val;
			}
		}
		return result;
	}

	private String generateHistoryString(DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
		StringBuffer histStr = new StringBuffer();

		histStr.append(aspp.getDataSetHistory());
		histStr.append(generateHistoryForMaSetView(view));
		
		return histStr.toString();
	}

}
