package org.geworkbench.components.gpmodule.consensusclustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genepattern.webservice.Parameter;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.properties.DSNamed;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.clusters.CSConsensusClusterResultSet;
import org.geworkbench.bison.model.clusters.DSConsensusClusterResultSet;
import org.geworkbench.bison.util.colorcontext.AbsoluteWhiteColorContext;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.components.gpmodule.GPAnalysis;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.SubpanelsAddedEvent;
import org.geworkbench.parsers.MicroarraySetParser;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.ProgressDialog;
import org.geworkbench.util.ProgressItem;
import org.geworkbench.util.ProgressTask;


public class ConsensusClustering<T extends DSNamed> extends GPAnalysis {

	private static final long serialVersionUID = -2283932375588941811L;

	private static Log log = LogFactory.getLog(ConsensusClustering.class);
	private static final String outputStub = "base";
	private static final String rootDir = FilePathnameUtils.getTemporaryFilesDirectoryPath();
	private static final ProgressDialog pd = ProgressDialog.getInstance(false);

	public ConsensusClustering() {
		panel = new ConsensusClusteringPanel();
		setDefaultPanel(panel);
	}

	private class ResultWrapper{
		private AlgorithmExecutionResults rst = null;
		private void setResult(AlgorithmExecutionResults rst){
			this.rst = rst;
		}
		private AlgorithmExecutionResults getResult(){
			return this.rst;
		}
	}
	
	public AlgorithmExecutionResults execute(Object input) {
		ResultWrapper rw = new ResultWrapper();
		CCTask task = new CCTask(ProgressItem.INDETERMINATE_TYPE, "Consensus Clustering Analysis Running", input, rw);
		pd.executeTask(task);
		while(!task.isDone()){
			try{
				Thread.sleep(1000);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return rw.getResult();
	}
	
	private class CCTask extends ProgressTask<Void, Void>{
		Object input;
		ResultWrapper rw = null;
		CCTask(int pbtype, String message, Object input, ResultWrapper rw){
			super(pbtype, message);
			this.input = input;
			this.rw = rw;
		}
		
		@Override
    	protected void done(){
	   		pd.removeTask(this);
	   	}

		@Override
		protected Void doInBackground() throws Exception {
			rw.setResult(executeInBackground(input, this));
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public AlgorithmExecutionResults executeInBackground(Object input, CCTask task) {
		if (!(input instanceof DSMicroarraySetView)) {
			return new AlgorithmExecutionResults(false, "Invalid microarray dataset for Consensus Clustering", null);
		}
		
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>)input;
		DSMicroarraySet maSet = view.getMicroarraySet();
		DSItemList<DSGeneMarker> markers = view.markers();
		DSItemList<DSMicroarray> arrays = view.items();
		String clusterBy = ((ConsensusClusteringPanel) panel).getClusterBy();
		if (clusterBy.equals("rows"))  arrays = maSet;
		else  markers = maSet.getMarkers();

		AlgorithmExecutionResults cancelResult = new AlgorithmExecutionResults(false, "Consensus Clustering Analysis Cancelled.", null);
		if(task.isCancelled()) return cancelResult;
		
		String gctFileName = createGCTFile(rootDir + maSet.getLabel(), markers, arrays).getAbsolutePath();
		Parameter[] parameters = encodeParameters(gctFileName);

		if(task.isCancelled()) return cancelResult;
		List<String> result = null;
		try{
			result = runAnalysis("ConsensusClustering", parameters, panel.getPassword());
		}catch(Exception e){
			return new AlgorithmExecutionResults(false, "Consensus Clustering Analysis Aborted: "+e.getMessage(), null);
		}
		if (result == null || result.size() == 0) {
			return new AlgorithmExecutionResults(false, "No Consensus Clustering Result.", null);
		}
		if(task.isCancelled()) return cancelResult;

		DSConsensusClusterResultSet resultSet = new CSConsensusClusterResultSet(maSet, "Consensus Clustering Result");
		for (String res : result) {
			log.info(res);
			resultSet.addFile(res);
		}

		// publish .clu results to selector panel
		String clusterListName = ((ConsensusClusteringPanel) panel).getClusterListName();
		if (clusterBy.equals("rows")) {
			ArrayList<DSPanel<T>> cluResult = parseCluFiles(resultSet.getCluFiles(), (DSItemList<T>)markers);
			publishSubpanelsAddedEvent(new SubpanelsAddedEvent<T>((Class<T>)DSGeneMarker.class, cluResult, clusterListName));
		} else {
			ArrayList<DSPanel<T>> cluResult = parseCluFiles(resultSet.getCluFiles(), (DSItemList<T>)arrays);
			publishSubpanelsAddedEvent(new SubpanelsAddedEvent<T>((Class<T>)DSMicroarray.class, cluResult, clusterListName));
		}

		// publish .gct results to project panel
		ArrayList<File> gctFiles = resultSet.getSortedGctFiles();
		if (gctFiles != null) {
			for (File gctFile : gctFiles) {
				File expFile = null;
				if(clusterBy.equals("rows")) expFile = convertGCT(gctFile, markers);
				else expFile = convertGCT(gctFile, null);
				DSMicroarraySet gctResult = new MicroarraySetParser().parseCSMicroarraySet(expFile);
				// for consensus cluster result maset, assign absolute(zero=white) color context
				ProjectPanel.getInstance().addProcessedMaSet(gctResult, new AbsoluteWhiteColorContext());
			}
		}

		HistoryPanel.addToHistory(resultSet, generateHistoryString(view));
		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,"Consensus Clustering Result", resultSet);
		return results;
	}
	
	private Parameter[] encodeParameters(String gctFileName) {
		List<Parameter> parameters = new ArrayList<Parameter>();
		Map<Serializable, Serializable> map = ((ConsensusClusteringPanel) panel)
		.getParameters();

		parameters.add(new Parameter("input.filename", gctFileName));
		parameters.add(new Parameter("output.stub", outputStub));
		parameters.add(new Parameter("resample", (String) map.get("resample")
				+ (String) map.get("resample.value")));

		for (Serializable k : map.keySet()) {
			String key = (String) k;
			String val = (String) map.get(k);
			if (key.startsWith("resample") || key.equals("cluster.list.name"))
				continue;
			if (key.equals("clustering.algorithm")
					|| key.equals("distance.measure")) {
				val = val.toUpperCase();
			} else if (key.equals("cluster.by")) {
				if (val.equals("columns"))
					val = "";
				else if (val.equals("rows"))
					val = "-c";
			} else if (key.equals("normalize.type")) {
				if (val.equals("row-wise"))
					val = "-n1";
				else if (val.equals("column-wise"))
					val = "-n2";
				else if (val.equals("both"))
					val = "-n3";
				else if (val.equals("none"))
					val = "";
			} else if (key.equals("create.heat.map")) {
				if (val.equals("yes"))
					val = "-p";
				else if (val.equals("no"))
					val = "";
			}
			parameters.add(new Parameter(key, val));
		}
		for (Parameter p : parameters) {
			log.info(p.getName() + " = " + p.getValue());
		}
		return (Parameter[]) parameters.toArray(new Parameter[0]);
	}

	private ArrayList<DSPanel<T>> parseCluFiles(ArrayList<File> files, DSItemList<T> itemList){
		ArrayList<DSPanel<T>> res = new ArrayList<DSPanel<T>>();
		if(files == null) return res;
		BufferedReader br=null;
		try{
			for (File file : files){
				br = new BufferedReader(new FileReader(file));
				String line = null;
				int number = 0;
				String fname = file.getName();
				String base = fname.substring(0, fname.length()-2);
				while((line = br.readLine())!=null){
					line = line.trim();
					if(Pattern.matches("^\\d+:$", line)){
						number = Integer.valueOf(line.substring(0, line.length()-1));
						res.add(new CSPanel<T>(base+number));
					}else if (line.length()>0 && res.size()>0){
						res.get(res.size()-1).add(itemList.get(line));
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(br!=null){
				try{ br.close();}catch(Exception e){e.printStackTrace();}
			}
		}
		for(DSPanel<T> panel : res){
			log.info(panel.getLabel()+": "+panel.size());
		}
		return res;
	}
	
	private File convertGCT(File gctFile, DSItemList<DSGeneMarker> markers){
		String gctName = gctFile.getPath();
		String expname = gctName.substring(0, gctName.length() - 3) + "exp";
		File expFile = new File(expname);
		BufferedWriter bw = null;
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(gctFile));
			bw = new BufferedWriter(new FileWriter(expFile));
			br.readLine(); br.readLine();
			String line = br.readLine();
			bw.write("AffyID");
			bw.write(line.substring(4));
			bw.newLine();
			while((line=br.readLine())!=null){
				if(markers == null){
					bw.write(line);
				}else{
					String[] toks = line.split("\t", 3);
					String marker = toks[0];
					bw.write(marker+"\t");
					bw.write(markers.get(marker).getGeneName()+"\t");
					bw.write(toks[2]);
				}
				bw.newLine();
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
		expFile.deleteOnExit();
		return expFile;
	}

	private String generateHistoryString(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
		StringBuffer histStr = new StringBuffer("Generated by Consensus Clustering run with parameters:\n\n");	 

		histStr.append(aspp.getDataSetHistory());
		histStr.append(generateHistoryForMaSetView(view));
		
		return histStr.toString();
	}
	
	@Publish
	public org.geworkbench.events.SubpanelsAddedEvent<T> publishSubpanelsAddedEvent(
			org.geworkbench.events.SubpanelsAddedEvent<T> event) {
		return event;
	}
}