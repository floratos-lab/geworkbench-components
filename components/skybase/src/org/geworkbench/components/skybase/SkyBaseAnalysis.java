package org.geworkbench.components.skybase;

import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.analysis.ProteinDatabaseAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import java.util.*;
import java.io.*;
import edu.columbia.geworkbench.cagrid.skybase.client.*;

public class SkyBaseAnalysis extends AbstractGridAnalysis implements ProteinDatabaseAnalysis
{
    SkyBaseConfigPanel scp;
    String seqname, seqfilename, seqcontent;
    public static String remote_root = "/nfs/apollo/2/c2b2/server_data/www/skyline/skybase/jakarta-tomcat-5.0.28/webapps/ROOT/SkyBaseData";
    private static final String remoteseqdir = remote_root+"/SEQ";
    //    private static final String skybaseweb = "http://156.145.238.15:8070/wsrf/services/cagrid/SkyBaseWeb";
    private static final String skybaseweb = "http://156.145.238.15:8070/wsrf/services/cagrid/SkyBaseWeb";
    public SkyBaseWebClient client;

    SkyBaseAnalysis()
    {
	setLabel("Blast SkyBase");
	scp = new SkyBaseConfigPanel();
	setDefaultPanel(scp);
	try{
	    System.out.println("skybaseweb: "+skybaseweb);
	    client = new SkyBaseWebClient(skybaseweb);
	}catch(Exception e){
	    System.out.println("SkyBaseWeb connection refused: "+skybaseweb);
	}
    }

    public AlgorithmExecutionResults execute(Object input)
    {
	if (input == null)
	    return new AlgorithmExecutionResults(false, "Invalid input. ", null);
	assert input instanceof DSSequenceSet;
	return new AlgorithmExecutionResults(true, "No errors", null);
    }

    public int getAnalysisType()
    {
	return SKYBASE_TYPE;
    }
    public String getType() 
    {
        return "SkyBaseAnalysis";
    }
    
    public String getAnalysisName()
    {
    	return "SkyBase";
    }

    public String getcontent(File seqfile)
    {
	byte[] fileBytes = null;
	try {
	    FileInputStream fileIn = new FileInputStream(seqfile);
	    DataInputStream dataIn = new DataInputStream(fileIn);
	    fileBytes = new byte[dataIn.available()];
	    dataIn.readFully(fileBytes);
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return new String(fileBytes);
    }
    
    public void set_seqfile(DSSequenceSet seq)
    {
	File seqfile = seq.getFile();
	seqname = seqfile.getName();
	seqfilename = remoteseqdir + seqname;
	seqcontent = getcontent(seqfile);
    }

    public boolean useMicroarraySetView()
    {
    	return false;
    }
    public boolean useOtherDataSet()
    {
    	return true;
    }
    public Class getBisonReturnType()
    {
    	return String.class;
    }
    public Map<Serializable, Serializable> getBisonParameters()
    {
	Map<Serializable, Serializable> parameterMap = new HashMap<Serializable, Serializable>();
		
	System.out.println("sendfileparam: "+seqname);
	parameterMap.put("sendnameParameter", seqname);
	parameterMap.put("sendcontentParameter", seqcontent);	

	String cfgcommand = scp.getmincovValue() + " " +
	    scp.getminsidValue() + " " + scp.getrphitsValue();
	System.out.println("blastskybaseparam: "+cfgcommand);
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
