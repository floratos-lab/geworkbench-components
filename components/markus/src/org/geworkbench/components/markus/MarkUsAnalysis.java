package org.geworkbench.components.markus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultComboBoxModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbench.bison.datastructure.bioobjects.structure.MarkUsResultDataSet;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.analysis.ProteinStructureAnalysis;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;

/**
 * MarkUs Analysis.

 * @author meng
 * @author zji
 * @version $Id: MarkUsAnalysis.java,v 1.7 2009-09-10 16:40:26 chiangy Exp $
 */
public class MarkUsAnalysis extends AbstractGridAnalysis implements ProteinStructureAnalysis 
{
	private static final long serialVersionUID = -4702468130439199874L;
	Log log = LogFactory.getLog(MarkUsAnalysis.class);

	private final String analysisName = "MarkUs";
	
    private MarkUsConfigPanel mcp;
	private static final String strurl = "https://bhapp.c2b2.columbia.edu/MarkUs/cgi-bin/submit.pl";
	private static final String browseUrl = "https://bhapp.c2b2.columbia.edu/MarkUs/cgi-bin/browse.pl?pdb_id=";
	private String req = "--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"submit\"\r\n\r\nUpload\r\n--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"infile\"; filename=\"PDB\"\r\nContent-Type: text/plain\r\n\r\n";
	private boolean STOPSIG = false;

    public MarkUsAnalysis() {
		mcp = new MarkUsConfigPanel();
		setDefaultPanel(mcp);
    }

    /** implements org.geworkbench.analysis.AbstractAnalysis.getAnalysisType */
    public int getAnalysisType() {
        return MARKUS_TYPE;
    }

    /** implements org.geworkbench.bison.model.analysis.Analysis.execute */
	public AlgorithmExecutionResults execute(Object input) {
		if (input == null)
			return new AlgorithmExecutionResults(false, "Invalid input. ", null);
		assert input instanceof DSProteinStructure;

		STOPSIG = false;
		ProgressBar pBar = Util.createProgressBar("Analysis Status", "MarkUs Analysis Pending");
		AbortObserver abortObserver = new AbortObserver();
		pBar.addObserver(abortObserver);
		pBar.start();

		DSProteinStructure prt = (DSProteinStructure) input;
		File prtfile = prt.getFile();
		File pdbfile = prtfile.getAbsoluteFile();
		String pdbname = prtfile.getName();

		String results = null;
		/* for quick display of previous results without doing analysis */
/*
		if (pdbname.equals("2pk7.pdb"))
			results = "MUS569";
		else if (pdbname.equals("1e09.pdb"))
			results = "MUS580";
		else if (pdbname.equals("input_txt_Q14994_8-83.pudge.pdb"))
			results = "MUS662";
		else if (pdbname.equals("fboxM1_gi_42544167.pudge.pdb"))
			results = "MUS668";
		else if (pdbname.equals("myb_gi_46361980.pudge.pdb"))
			results = "MUS669";
		else if (pdbname.equals("myc_gi_71774083.pudge.pdb"))
			results = "MUS670";

		else {
		*/
			if(STOPSIG){
				pBar.stop();
				return new AlgorithmExecutionResults(false, "MarkUs analysis cancelled", null);
			}

			// upload file to MarkUs server, get tmpfile value
			String tmpfile = null;
			try {
				tmpfile = uploadFile(pdbfile);
			} catch (Exception e) {
				log.warn("MarkUsWeb uploadFile error: " + pdbname);
				log.info( "MarkUs analysis service - File uploading error" );
				pBar.stop();
				return new AlgorithmExecutionResults(false, "Failed to upload pdb file for MarkUs analysis", null);
			}

			// get MarkUs job submission configuration
			String cfgcommand = generateMarkusInput(pdbname, tmpfile);

			if(STOPSIG){
				pBar.stop();
				return new AlgorithmExecutionResults(false, "MarkUs analysis cancelled", null);
			}

			// submit job, get MarkUs job id
			try {
				results = submitJob(cfgcommand);
				log.debug("MarkUs job cfgcommand: " + cfgcommand);
				log.info("MarkUs job ID return: " + results);
			} catch (Exception e) {
				log.warn("MarkUsWeb submitJob error: " + cfgcommand);
				pBar.stop();
				return new AlgorithmExecutionResults(false, "Failed to submit job for MarkUs analysis", null);
			}
			/*
		}
		*/

		if(results==null){
			pBar.stop();
			return new AlgorithmExecutionResults(false, "No result for MarkUs analysis", null);
		}
		
		String impossibleResult = results.toLowerCase();
		if (impossibleResult.contains("error")
				|| results.equals("cancelled") || results.equals("na")){
			pBar.stop();
			return new AlgorithmExecutionResults(false, 
					"Error: unexpected results in MarkUs analysis service: "
							+ impossibleResult, null);
		}

		if(STOPSIG){
			pBar.stop();
			return new AlgorithmExecutionResults(false, "MarkUs analysis cancelled", null);
		}

		// start waiting for this job's results
		String url = browseUrl + results;
		UrlStatus urlstat = checkUrlStatus(url);
		log.info("URL status: " + urlstat + " " + url);
		if (urlstat != UrlStatus.FINISHED && thread4pdb.get(url) == null) {
			Thread t = new Thread(new MarkUsTask(url, results));
			t.start();
		}
		
		log.debug("result of MarkUs analysis: "+results);
		while(urlstat != UrlStatus.FINISHED) {
			if(STOPSIG){
				pBar.stop();
				return new AlgorithmExecutionResults(false, "MarkUs analysis cancelled", null);
			}
			log.info("... still waiting for result "+results+" to finish at "+new java.util.Date());
			try {
				Thread.sleep(30000L);
			} catch (InterruptedException e) {
				// if interrupted while sleeping, do nothing
			}
			urlstat = checkUrlStatus(url);
		}

		MarkUsResultDataSet resultset = new MarkUsResultDataSet(prt, results);
		resultset.setResult(results);
		pBar.stop();
		return new AlgorithmExecutionResults(true, "No errors", resultset);

    }
    
    @Override
	public String getAnalysisName() {
    	return analysisName;
	}

	@Override
	protected Map<Serializable, Serializable> getBisonParameters() {
		log.debug("Reading bison parameters");

		Map<Serializable, Serializable> bisonParameters = new HashMap<Serializable, Serializable>();
		MarkUsConfigPanel paramPanel = (MarkUsConfigPanel) this.aspp;

		// main - all booleans
		bisonParameters.put("skan", paramPanel.getskanValue());
		bisonParameters.put("dali", paramPanel.getdaliValue());
		bisonParameters.put("screen", paramPanel.getscreenValue());
		bisonParameters.put("delphi", paramPanel.getdelphiValue());
		bisonParameters.put("psi_blast", paramPanel.getpsiblastValue());
		bisonParameters.put("ips", paramPanel.getipsValue());
		bisonParameters.put("consurf", paramPanel.getconsurfValue());
		bisonParameters.put("consurf3", paramPanel.getconsurf3Value());
		bisonParameters.put("consurf4", paramPanel.getconsurf4Value());
		// string
		bisonParameters.put("chain", paramPanel.getChain());
		bisonParameters.put("key", paramPanel.getkeyValue());
		bisonParameters.put("email", paramPanel.getEmail(true));
		bisonParameters.put("title", paramPanel.getTitle(true));

		// delphi
		bisonParameters.put("grid_size", paramPanel.getgridsizeValue()); // int
		bisonParameters.put("box_fill", paramPanel.getboxfillValue()); // int
		bisonParameters.put("steps", paramPanel.getstepsValue()); // int
		bisonParameters.put("sc", paramPanel.getscValue()); // double
		bisonParameters.put("radius", paramPanel.getradiusValue()); // double
		bisonParameters.put("ibc", paramPanel.getibcValue()); // int
		bisonParameters.put("nli", paramPanel.getnliValue()); // int
		bisonParameters.put("li", paramPanel.getliValue()); // int
		bisonParameters.put("idc", paramPanel.getidcValue()); // int
		bisonParameters.put("edc", paramPanel.getedcValue()); // int

		// analysis 3
		if(paramPanel.getconsurf3Value()) {
			bisonParameters.put("csftitle3", paramPanel.getcsftitle3Value()); // String
			bisonParameters.put("eval3", paramPanel.geteval3Value()); // double
			bisonParameters.put("iter3", paramPanel.getiter3Value()); // int
			bisonParameters.put("filter3", paramPanel.getfilter3Value()); // int
			bisonParameters.put("msa3", paramPanel.getmsa3Value()); // String
		}

		// analysis 4
		if(paramPanel.getconsurf4Value()) {
			bisonParameters.put("csftitle4", paramPanel.getcsftitle4Value()); // String
			bisonParameters.put("eval4", paramPanel.geteval4Value()); // double
			bisonParameters.put("iter4", paramPanel.getiter4Value()); // int
			bisonParameters.put("filter4", paramPanel.getfilter4Value()); // int
			bisonParameters.put("msa4", paramPanel.getmsa4Value()); // String
		}
		return bisonParameters;	}

	@Override
	public Class<?> getBisonReturnType() {
		return String.class;
	}

	@Override
	protected boolean useMicroarraySetView() {
		return false;
	}

	@Override
	protected boolean useOtherDataSet() {
		return true;
	}

	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet<?> refMASet) {
		// TODO Auto-generated method stub
		return new ParamValidationResults(true, null);
	}
	
	@Override
	public boolean isAuthorizationRequired() {
		return false;
	}

	@Subscribe
    public void receive(ProjectEvent e, Object source) {
        DSDataSet<?> data = e.getDataSet();
        if (data instanceof DSProteinStructure)
        {
        	DSProteinStructure dsp = (DSProteinStructure)data;
        	HashMap<String, Integer> chains = dsp.getChains();
        	mcp.cbxChain.setModel(new DefaultComboBoxModel(chains.keySet().toArray()));
        }
    }

	private java.lang.String uploadFile(File pdbfile) throws Exception {
		HttpURLConnection conn = null;
		BufferedReader in = null;
		String tmpfile = null;
		try {
			URL url = new URL(strurl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(true);
			conn.setAllowUserInteraction(true);
			conn.setRequestProperty("Content-Type",
					"multipart/form-data, boundary=AaB03x");
			conn.setRequestProperty("Content-Transfer-Encoding", "binary");
			conn.connect();

			req = req.replaceFirst("PDB", pdbfile.getName());
			OutputStream out = conn.getOutputStream();
			out.write(req.getBytes());
			FileInputStream fis = new FileInputStream(pdbfile);
			byte[] buffer = new byte[4096];
			int bytes_read;
			while ((bytes_read = fis.read(buffer)) != -1) {
				out.write(buffer, 0, bytes_read);
			}
			out.write("\r\n--AaB03x--\r\n".getBytes());
			out.flush();
			out.close();
			fis.close();

			InputStream dat = conn.getInputStream();
			String contenttype = conn.getContentType();

			if (contenttype.toLowerCase().startsWith("text")) {
				in = new BufferedReader(new InputStreamReader(
						dat));
				String line = null; int i=-1, j=-1;
				while ((line = in.readLine()) != null)
				{
				    if ((i = line.indexOf("name=\"tmpfile\" value=\"")) > -1)
				    {
					j = line.indexOf(".pdb");
					tmpfile = line.substring(i+22, j+4);
				    }
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			conn.disconnect();
			try {
				if(in!=null) in.close(); 
			} catch (Exception a) {
				a.printStackTrace();
			}
			conn = null;
		}
		return tmpfile;
	}

	private HashMap<String, String> thread4pdb = new HashMap<String, String>();

	private String generateMarkusInput(String pdbfilename, String tmpfile)
	{
		StringBuilder cfgcommand = new StringBuilder();

		if (mcp.getdaliValue())
			cfgcommand.append(" Dali=1");
		if (mcp.getdelphiValue())
			cfgcommand.append(" Delphi=1");
		if (mcp.getconsurf3Value()) {
			cfgcommand.append(" C3=BLAST C3T=").append(mcp.getcsftitle3Value())
					.append(" C3E=").append(mcp.geteval3Value())
					.append(" C3I=").append(mcp.getiter3Value())
					.append(" C3P=").append(mcp.getfilter3Value())
					.append(" C3MSA=").append(mcp.getmsa3Value());
		}
		if (mcp.getconsurf4Value()) {
			cfgcommand.append(" C4=BLAST C4T=").append(mcp.getcsftitle4Value())
					.append(" C4E=").append(mcp.geteval4Value())
					.append(" C4I=").append(mcp.getiter4Value())
					.append(" C4P=").append(mcp.getfilter4Value())
					.append(" C4MSA=").append(mcp.getmsa4Value());
		}
		if (mcp.getkeyValue())
			cfgcommand.append(" privateKey=1");
		
		String chain = mcp.getChain();

		cfgcommand
				.append(
						" Skan=1 SkanBox=1 Screen=1 ScreenBox=1 VASP=1 LBias=1 PredUs=1 BlastBox=1 IPSBox=1 CBox=1")
				.append(" D1B=")
				.append(mcp.getibcValue())
				.append(" D1EX=")
				.append(mcp.getedcValue())
				.append(" D1F=")
				.append(mcp.getstepsValue())
				.append(" D1G=")
				.append(mcp.getgridsizeValue())
				.append(" D1IN=")
				.append(mcp.getidcValue())
				.append(" D1L=")
				.append(mcp.getliValue())
				.append(" D1N=")
				.append(mcp.getnliValue())
				.append(" D1P=")
				.append(mcp.getboxfillValue())
				.append(" D1PR=")
				.append(mcp.getradiusValue())
				.append(" D1SC=")
				.append(mcp.getscValue())
				.append(
						" C1=PFAM C1T=Pfam C2=BLAST C2T=e@1.0e-3%20identity%200.8 C2E=0.001 C2I=3 C2P=80 C2MSA=Muscle")
				.append(" chain_ids=").append(chain).append(" chains=").append(
						chain).append(" email=").append(mcp.getEmail(false)).append(
						" infile=").append(pdbfilename).append(
						" submit=Mark%20Us").append(" title=").append(mcp.getTitle(false))
				.append(" tmpfile=").append(tmpfile)
				.append("\r\n--AaB03x--\r\n");
		String cfgstr = cfgcommand.toString();
		cfgstr = cfgstr.replaceAll("=", "\"\r\n\r\n");
		cfgstr = cfgstr.replaceAll(" ", "\r\n--AaB03x\r\n" + "content-disposition: form-data; name=\"");
		cfgstr = cfgstr.replaceFirst("e@1.0e", "e=1.0e");
		cfgstr = cfgstr.replaceAll("%20", " ");
		return cfgstr;
	}  

    private java.lang.String submitJob(java.lang.String string) throws Exception {
    	HttpURLConnection conn = null;
    	BufferedReader in = null;
        String process_id = "na";
		try {
			URL url = new URL(strurl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(true);
			conn.setAllowUserInteraction(true);
			conn.setRequestProperty("Content-Type",
					"multipart/form-data, boundary=AaB03x");
			conn.setRequestProperty("Content-Transfer-Encoding", "binary");
			conn.connect();

			OutputStream out = conn.getOutputStream();
			out.write(string.getBytes());
			out.flush();
			out.close();

			InputStream dat = conn.getInputStream();
			String contenttype = conn.getContentType();

			if (contenttype.toLowerCase().startsWith("text")) {
				in = new BufferedReader(new InputStreamReader(
						dat));

				String line = null; int i=-1; int j = -1;
				while ((line = in.readLine()) != null)
				{
					if ((i = line.indexOf("pdb_id=")) > -1 && (j = line.indexOf("\">")) > -1)
				    {
						process_id = line.substring(i+7, j);
				    }
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			conn.disconnect();
			try {
				if(in!=null) in.close(); 
			} catch (Exception a) {
				a.printStackTrace();
			}
			conn = null;
		}
        log.debug("SubmitJob "+process_id);
        return process_id;
    }

    private static enum UrlStatus {FINISHED, PENDING, NO_RECORD}
    /** Check the status of given URL */
    static private UrlStatus checkUrlStatus(String url)
    {
    	BufferedReader br = null;
    	try{
    	    URLConnection uc = new URL(url).openConnection();
    	    if (((HttpURLConnection)uc).getResponseCode() == 404)
    	    	return UrlStatus.NO_RECORD;
    	    
    	    br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
    	    String tmp = null;
    	    while((tmp = br.readLine()) != null) {
    	    	if (tmp.indexOf("functional annotation is pending") > -1) {
    	    		br.close();
    	    		return UrlStatus.PENDING;
    	    	}
    	    }
    	}catch (IOException e){
    	    e.printStackTrace();
    	    return UrlStatus.NO_RECORD;
    	}finally{
    	    try {
    			if(br!=null) br.close();
    		} catch (IOException e1) { // no action intentionally
    			e1.printStackTrace();
    		}
    	}
    	return UrlStatus.FINISHED;
    }

    /** The task to get MarkUs results. 
	* It returns when the result is available, but does not track or process the actual result. 
	*/
	private class MarkUsTask implements Runnable
	{
		private String myurl = null;
		
		MarkUsTask(String myurl, String process_id) {
			this.myurl = myurl;
		}
		
		public void run() 
		{
			if (STOPSIG) return;
			boolean change = false;
			while (checkUrlStatus(myurl) != UrlStatus.FINISHED)
			{
				if (!change) { thread4pdb.put(myurl, new String("run")); }
				change = true;
				try{
					Thread.sleep(30*1000);
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}
			if (!change) return;

			thread4pdb.put(myurl, new String("done"));
		}
	}
	/* when user close the ProgressBar, abort analysis */
	private class AbortObserver implements Observer {
		public AbortObserver() {
		}
		public void update(Observable o, Object arg) {
			STOPSIG = true;
		}
	}
}

