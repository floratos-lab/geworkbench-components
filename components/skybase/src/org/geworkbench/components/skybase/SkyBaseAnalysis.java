package org.geworkbench.components.skybase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.analysis.ProteinDatabaseAnalysis;

import edu.columbia.geworkbench.cagrid.skybase.client.SkyBaseWebClient;

/**
 * AbstractGridAnalysis for blast skybase on grid service on web1
 * 
 * @author mw2518
 * @version $Id: SkyBaseAnalysis.java,v 1.5 2009-01-19 14:59:33 wangm Exp $
 *
 */
public class SkyBaseAnalysis extends AbstractGridAnalysis implements
		ProteinDatabaseAnalysis {
	private Log log = LogFactory.getLog(this.getClass());
	private static final long serialVersionUID = 1L;
	SkyBaseConfigPanel scp;
	String seqname, seqfilename, seqcontent;
	public static String remote_root = "/nfs/apollo/2/c2b2/server_data/www/skyline/skybase/jakarta-tomcat-5.0.28/webapps/ROOT/SkyBaseData";
	private static final String remoteseqdir = remote_root + "/SEQ";
	private static final String skybaseweb = "http://156.145.238.15:8070/wsrf/services/cagrid/SkyBaseWeb";
	public SkyBaseWebClient client;

	SkyBaseAnalysis() {
		setLabel("Blast SkyBase");
		scp = new SkyBaseConfigPanel();
		setDefaultPanel(scp);
		try {
			log.info("skybaseweb: " + skybaseweb);
			client = new SkyBaseWebClient(skybaseweb);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("SkyBaseWeb connection refused: " + skybaseweb);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.bison.model.analysis.Analysis#execute(java.lang.Object)
	 */
	public AlgorithmExecutionResults execute(Object input) {
		if (input == null)
			return new AlgorithmExecutionResults(false, "Invalid input. ", null);
		assert input instanceof DSSequenceSet;
		return new AlgorithmExecutionResults(true, "No errors", null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractAnalysis#getAnalysisType()
	 */
	public int getAnalysisType() {
		return SKYBASE_TYPE;
	}

	public String getType() {
		return "SkyBaseAnalysis";
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getAnalysisName()
	 */
	public String getAnalysisName() {
		return "SkyBase";
	}

	/*
	 * return file content
	 */
	public String getcontent(File seqfile) {
		StringBuffer contents = new StringBuffer();
		try {
		    BufferedReader br = new BufferedReader(new FileReader(seqfile));
		    String line;
		    boolean foundseq = false;
		    while((line = br.readLine()) != null) {
			if (line.startsWith(">"))
			    if (foundseq) break;

			contents.append(line);
			contents.append("\n");
			foundseq = true;
		    }
		    br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return contents.toString();
	}

	public void set_seqfile(DSSequenceSet seq) {
		File seqfile = seq.getFile();
		seqname = seqfile.getName();
		seqfilename = remoteseqdir + seqname;
		seqcontent = getcontent(seqfile);
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#useMicroarraySetView()
	 */
	public boolean useMicroarraySetView() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#useOtherDataSet()
	 */
	public boolean useOtherDataSet() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonReturnType()
	 */
	public Class getBisonReturnType() {
		return String.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonParameters()
	 */
	public Map<Serializable, Serializable> getBisonParameters() {
		Map<Serializable, Serializable> parameterMap = new HashMap<Serializable, Serializable>();

		log.info("sendfileparam: " + seqname);
		parameterMap.put("sendnameParameter", seqname);
		System.out.println(seqcontent);
		parameterMap.put("sendcontentParameter", seqcontent);

		String cfgcommand = scp.getmincovValue() + " " + scp.getminsidValue()
				+ " " + scp.getrphitsValue();
		log.info("blastskybaseparam: " + cfgcommand);
		parameterMap.put("skybaseParameter", cfgcommand);

		return parameterMap;
	}

	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet refMASet) {
		return new ParamValidationResults(true, "Not Checked");
	}
}
