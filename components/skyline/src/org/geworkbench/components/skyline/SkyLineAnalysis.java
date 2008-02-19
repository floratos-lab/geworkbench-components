package org.geworkbench.components.skyline;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ProteinStructureAnalysis;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;

import java.io.*;
import gov.nih.nci.cagrid.client.*;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust Inc.
 * @version 1.0
 */

/**
 * Replaces all values less (or more) than a user designated Threshold X
 * with the value X.
 */
public class SkyLineAnalysis extends AbstractAnalysis implements ProteinStructureAnalysis {
    // Static fields used to designate the available user option within the
    // normalizer's parameters panel.
    public static final String YES = "yes";
    public static final String NO = "no";
    public static final int MINIMUM = 0;
    public static final int MAXIMUM = 1;
    public static final int IGNORE = 0;
    public static final int REPLACE = 1;
    double threshold;
    int thresholdType;
    int missingValues;
    String chain, d, run_pb1, run_pb2, f, chosen_species;
    String run_modeller, run_nest, hetatm, clustal;
    int j, b, redundancy_level, model_number;
    double h, e;
    SkyLineConfigPanel slp;
    public static String remote_root = "/nfs/apollo/2/c2b2/server_data/www/skyline/apache-tomcat-6.0.14/webapps/ROOT/SkyLineData";
    public static String qsubjob = remote_root+"/test.qsub";
    private static final String skylineinfile = remote_root+"/test.cfg";
    private static final String remotepdbdir = remote_root+"/PDB/";
    private static final String remoteoutdir = remote_root+"/output/";
    private static final String skylineweb = "http://156.111.188.2:8090/wsrf/services/cagrid/SkyLineWeb";
    public SkyLineWebClient client;

    public SkyLineAnalysis() {
        setLabel("Comparative Modeling Pipeline: SkyLine");
	slp = new SkyLineConfigPanel();
        setDefaultPanel(slp);
	try{
	    System.out.println("skylineweb: "+skylineweb+"\n");
	    client = new SkyLineWebClient(skylineweb);
	}catch(Exception e){
	    System.out.println("SkyLineWeb connection refused: "+skylineweb);
	}
    }

    public int getAnalysisType() {
        return SKYLINE_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null)
            return new AlgorithmExecutionResults(false, "Invalid input.", null);
        assert input instanceof DSProteinStructure;
	
	DSProteinStructure prt = (DSProteinStructure) input;

	File prtfile = prt.getFile();
	String pdbname = prtfile.getName();
	File pdbfile = prtfile.getAbsoluteFile();
	String pdbcontent = getcontent(pdbfile);
	String pdbshort = pdbname.substring(0, pdbname.length()-4);
	try{
	    client.sendFile("pdb", pdbname, pdbcontent);
	}catch (Exception e){
	    System.out.println("SkyLineWeb sendFile error: "+pdbname);
	}

	generate_skylineinput(remotepdbdir+pdbname);
	generate_qsubjob(pdbshort);

	String htmlText = new String();
	try{
	    htmlText = client.submitJob(qsubjob);
	    System.out.println(htmlText);
	}catch (Exception e){
	    System.out.println("SkyLineWeb submitJob error: "+qsubjob);
	    return new AlgorithmExecutionResults(false, "Job submission error", "Job submission error");
	}

        return new AlgorithmExecutionResults(true, "No errors", htmlText);
    }

    String getJobStatus(String pname)
    {
	String status = new String();
	try{
	    status = client.getJobStatus(pname);
	}catch (Exception e) {
	    System.out.println("SkyLineWeb getJobStatus error: "+pname);
	    return "not connected";
	}
	return status;
    }

    class StreamGobbler extends Thread
    {
	InputStream is;
	OutputStream os;
	String type;

	StreamGobbler(InputStream is, String type)
	{
	    this.is = is;
	    this.type = type;
	}
	StreamGobbler(InputStream is, String type, OutputStream redirect)
	{
	    this.is = is;
	    this.type = type;
	    this.os = redirect;
	}
	public void run()
	{
	    try {
		PrintWriter pw = null;
		if (os != null) { pw = new PrintWriter(os); }

		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line = null;

		while ( (line = br.readLine()) != null) {
		    if (pw != null) { pw.println(line); }
		    System.out.println(type + ">" + line);
		}
		if (pw != null) { pw.flush(); }
	    } catch (IOException ioe){
		ioe.printStackTrace();
	    }

	}
    }

    public String getcontent(File pdbfile)
    {
	byte[] fileBytes = null;
	try {
	    FileInputStream fileIn = new FileInputStream(pdbfile);
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

    public void generate_skylineinput(String pdbfile)
    {
        // Collect the parameters needed for the execution of the normalizer

	chain = slp.getchainValue();
	d = slp.getdValue();

	run_pb1 = slp.getrun_pb1Value();
	run_pb2 = slp.getrun_pb2Value();
	f = slp.getfValue();
	chosen_species = slp.getchosen_speciesValue();
	run_modeller = slp.getrun_modellerValue();
	run_nest = slp.getrun_nestValue();
	hetatm = slp.gethetatmValue();
	clustal = slp.getclustalValue();
	j = slp.getjValue();
	b = slp.getbValue();
	redundancy_level = slp.getredundancy_levelValue();
	model_number = slp.getmodel_numberValue();
	h = slp.gethValue();
	e = slp.geteValue();

	try{
	    //	    Runtime.getRuntime().exec("chmod 777 " + skylineinfile);
	    // cfg file not needed on client side
	    //	    BufferedWriter bw = new BufferedWriter(new FileWriter(skylineinfile));
	    String cfgcontent = "#PDB|chain|input_PB|homolog_PB|d|j|B|h|e|F|species|redundancy|modeller|nest|#_models|hetero|clustal|output_dir\n";
	    cfgcontent += pdbfile+"\t" +chain+"\t" +run_pb1+"\t" +run_pb2+"\t"
		+d+"\t" +j+"\t" +b+"\t" +h+"\t" +e+"\t" +f+"\t"
		+chosen_species+"\t" +redundancy_level+"\t"
		+run_modeller+"\t" +run_nest+"\t" +model_number+"\t"
		+hetatm+"\t" +clustal+"\t" +remoteoutdir+"\n";
	    //	    bw.write(cfgcontent);
	    //	    bw.flush();
	    //	    bw.close();

	    client.sendFile("cfg", "test.cfg", cfgcontent);

	} catch(Exception e) {
	    System.out.println("SkyLineWeb sendFile error: test.cfg");
	}
    }

    public void generate_qsubjob(String pdbname) {
	try{
	    //	    String qsubjob = "components/test/src/org/geworkbench/components/test/testperl.qsub";
	    //	    Runtime.getRuntime().exec("chmod 777 " + qsubjob);
	    // qsub jobscript not needed on client side
	    //	    BufferedWriter bwout = new BufferedWriter(new FileWriter(qsubjob));
//	    String host = System.getenv("HOSTNAME");
//	    String disport = System.getenv("DISPLAY");
	    String aString = 
		"#!/bin/bash\n\n"+

		"#$ -N gW"+pdbname+"SkyLine\n"+
		"#$ -o sge_output.dat\n"+
		"#$ -e sge_error.dat\n"+
		"#-- -j y\n"+
		"#$ -cwd\n"+
		"#-- -M mw2518@columbia.edu\n"+
		"#-- -m e\n"+
		"#-- -pe lam 1-30\n"+
		"#$ -S /bin/bash\n"+

		"echo \"job started\"\n"+
		"date\n"+
		"### begin MODELLER6v2 ######################################################\n\n"+

		"MODINSTALL6v2=/razor/5/users/mw2518/bin/modeller-6v2\n"+
		"EXECUTABLE_TYPE6v2=i386-absoft\n"+
		"LIBS_LIB6v2=$MODINSTALL6v2/modlib/libs.lib\n"+
		"KEY_MODELLER6v2=MODELIRANJE\n"+
		"mod=mod6v2\n"+
		"export MODINSTALL6v2 EXECUTABLE_TYPE6v2 LIBS_LIB6v2 KEY_MODELLER6v2 mod\n"+
		"PATH=$PATH:$MODINSTALL6v2/bin\n"+
		"ulimit -S -s unlimited\n\n"+

		"### end MODELLER6v2 ########################################################\n\n"+

		"export PROSA_BASE=/razor/5/users/mw2518/bin/prosa/ProSaData\n"+
		"PATH=$PATH:/razor/5/users/mw2518/bin/prosa/bin\n"+

		"#-- perl components/test/src/org/geworkbench/components/test/TestPerl.pl in.txt\n"+
		"perl /razor/5/users/mw2518/bin/batch_leverage.pl "+skylineinfile+"\n"+
		"date\n"+
		"echo \"job ended\"\n";
	    //	    bwout.write(aString);
	    //	    bwout.flush();
	    //	    bwout.close();

	    client.sendFile("cfg", "test.qsub", aString);

	}catch (Exception e){
	    System.out.println("SkyLineWeb sendFile error: test.qsub");
	}
    }


    public String getType() {
        return "SkyLineAnalysis";
    }

}

