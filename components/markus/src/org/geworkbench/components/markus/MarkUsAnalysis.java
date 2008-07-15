package org.geworkbench.components.markus;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ProteinAnnotationAnalysis;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import java.util.*;
import java.io.*;
import gov.nih.nci.cagrid.client.*;

import javax.swing.JOptionPane;
import java.text.*;

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
public class MarkUsAnalysis extends AbstractAnalysis implements ProteinAnnotationAnalysis 
{
    String chain, d, run_pb1, run_pb2, f, chosen_species;
    String run_modeller, run_nest, hetatm, clustal;
    int j, b, redundancy_level, model_number;
    double h, e;
    MarkUsConfigPanel mcp;
    //    public static String remote_root = "/nfs/apollo/2/c2b2/server_data/www/skyline/markus/apache-tomcat-6.0.14/webapps/ROOT/MarkUsData";
    public static String remote_root = "/razor/3/markuswb/apache-tomcat-6.0.14/webapps/ROOT/MarkUsData";
    public static String qsubjob = remote_root+"/test.qsub";
    private static final String skylineinfile = remote_root+"/test.cfg";
    private static final String remotepdbdir = remote_root+"/PDB/";
    private static final String remoteoutdir = remote_root+"/output/";
    private static final String markusweb = "http://156.145.30.72:8070/wsrf/services/cagrid/MarkUsWeb";
    public MarkUsWebClient client;

    public MarkUsAnalysis() {
        setLabel("Protein Function Annotation Server: MarkUs");
	mcp = new MarkUsConfigPanel();
        setDefaultPanel(mcp);
	try{
	    System.out.println("markusweb: "+markusweb+"\n");
	    client = new MarkUsWebClient(markusweb);
	}catch(Exception e){
	    System.out.println("MarkUsWeb connection refused: "+markusweb);
	}
    }

    public int getAnalysisType() {
        return MARKUS_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
		if (input == null)
		    return new AlgorithmExecutionResults(false, "Invalid input.", null);
		assert input instanceof DSProteinStructure;
	
		DSProteinStructure prt = (DSProteinStructure) input;
	
		File prtfile = prt.getFile();
		HashMap<String, Integer> hm = prt.getChains();
		Iterator<String> it = hm.keySet().iterator();
		String[] chains = new String[hm.size()];
		int i = 0;
		while(it.hasNext())
		{
			chains[i++] = (String)it.next();
		}
		chain = (String) JOptionPane.showInputDialog(null, "Which Chain? ", "Chain", 
				JOptionPane.QUESTION_MESSAGE, null, chains, chains[0]);
		
		if (chain == null) { return new AlgorithmExecutionResults(true, "No errors", "cancelled");}
		String pdbname = prtfile.getName();
		File pdbfile = prtfile.getAbsoluteFile();
		String pdbcontent = getcontent(pdbfile);

		//// test: for quick display of previous results without doing analysis
		if (pdbname.equals("2pk7.pdb")) return new AlgorithmExecutionResults(true, "No errors", "MUS569");
		else if (pdbname.equals("1e09.pdb")) return new AlgorithmExecutionResults(true, "No errors", "MUS580");

		//send file from local client to server
		try{
		    client.sendFile("pdb", pdbname, pdbcontent);
		}catch (Exception e) {
		    System.out.println("MarkUsWeb sendFile error: "+pdbname);
		    return new AlgorithmExecutionResults(false, "File sending error", "File sending error");
		}
	
		//upload file to MarkUs server, get tmpfile value
		String pdbfilename = remotepdbdir + pdbname;
		String tmpfile = null;
		try{
		    tmpfile = client.uploadFile(pdbfilename);
		}catch (Exception e) {
		    System.out.println("MarkUsWeb uploadFile error: "+pdbfilename);
		    return new AlgorithmExecutionResults(false, "File uploading error", "File uploading error");
		}

		//get MarkUs job submission configuration
		String cfgcommand = generate_markusinput(pdbfilename);
		cfgcommand += tmpfile;
		System.out.println(cfgcommand);

		//submit job, get MarkUs jobid
		String jobid = null;
		try{
		    jobid = client.submitJob(cfgcommand);
		    System.out.println(jobid);
		}catch (Exception e) {
		    System.out.println("MarkUsWeb submitJob error: "+cfgcommand);
		    return new AlgorithmExecutionResults(false, "Job submission error", "Job submission error");
		}

        return new AlgorithmExecutionResults(true, "No errors", jobid);
    }

    String getJobStatus(String pname)
    {
	String status = new String();
	try{
	    //	    status = client.getJobStatus(pname);
	}catch (Exception e) {
	    System.out.println("MarkUsWeb getJobStatus error: "+pname);
	    return "not connected";
	}
	return status;
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
    
    public String generate_markusinput(String pdbfilename)
    {
	String cfgcommand = "";
	String cfgfile = "cfg.txt";
	try{
	    String cfgcontent = "";
	    cfgcontent = 
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<asgard>\n" +
"        <globals>\n" +
"                <variable name=\"EMAIL\">pplocal@cypress.bioc.columbia.edu</variable>\n" +
"                <varibale name=\"EMAILPASS\">Pm34%a!9</varibale>\n" +
"                <variable name=\"MAILBOX\">Inbox</variable>\n" +
"                <variable name=\"BLASTDB\">/razor/4/mfischer/databases/uniprotkb</variable>\n" +
"                <variable name=\"TMPDIR\">/razor/5/honigtmp/</variable>\n" +
"                <variable name=\"TARGETPDB\">/razor/5/honigtmp/mark-us/pdb</variable>\n" +
"                <variable name=\"SKADS\">/razor/0/common/pudge/dat/skads/templates.60.skads</variable>\n" +
"        </globals>\n" +
"        <services>\n" +
"\n";

	    if ( mcp.getdaliValue() ){
		cfgcontent += "<service name=\"StrSearch\" store=\"1\"><submit>yes</submit><method>Dali</method></service>\n";
	    }
	    if ( mcp.getskanValue() ){
		cfgcontent += "<service name=\"Skan\" store=\"1\"><submit>yes</submit></service>\n";
	    }
	    if ( mcp.getscreenValue() ){
		cfgcontent += "<service name=\"Screen\" store=\"1\"><submit>yes</submit></service>\n";
	    }
	    if ( mcp.getdelphiValue() ) {
                cfgcontent += "<service name=\"Delphi\" store=\"1\"><submit>yes</submit>\n";
                cfgcontent += "<param name=\"nonit\">" + mcp.getnliValue() + "</param>";
                cfgcontent += "<param name=\"linit\">" + mcp.getliValue() + "</param>";
                cfgcontent += "<param name=\"gsize\">" + mcp.getgridsizeValue() + "</param>";
                cfgcontent += "<param name=\"perfil\">" + mcp.getboxfillValue() + "</param>";
                cfgcontent += "<param name=\"bndcon\">" + mcp.getibcValue() + "</param>";
                cfgcontent += "<param name=\"salt\">" + mcp.getscValue() + "</param>";
                cfgcontent += "<param name=\"prbrad\">" + mcp.getradiusValue() + "</param>";
                cfgcontent += "<param name=\"focusing\">" + mcp.getstepsValue() + "</param>";
                cfgcontent += "<param name=\"indi\">" + mcp.getidcValue() + "</param>";
                cfgcontent += "<param name=\"exdi\">" + mcp.getedcValue() + "</param>";
                cfgcontent += "</service>\n";
	    }

	    cfgcontent += "<service name=\"Consurf\" store=\"1\"><submit>yes</submit>\n";

	    cfgcontent += "<run title=\"Pfam\" store=\"1\"><seq_search service=\"InterProScan\" store=\"1\"><domain>Pfam</domain></seq_search><seq_align service=\"Profile\" store=\"1\"><method>ClustalW</method></seq_align></run>" + "\n";

	    cfgcontent += "<run title=\"e=1.0e-3 identity 0.8\" store=\"1\"><seq_search service=\"PsiBlast\" store=\"1\"><param name=\"e\">0.001</param><param name=\"j\">3</param><param name=\"indexes\">1</param></seq_search><seq_filter service=\"CdHit\" store=\"0\"><param name=\"c\">0.80</param></seq_filter><seq_align service=\"Muscle\" store=\"1\"></seq_align></run>" + "\n";

	    NumberFormat fm = new DecimalFormat("#.##");
	    if (mcp.getconsurf3Value())
	    {
		cfgcontent += "<run title=\"" + mcp.getcsftitle3Value() + "\" store=\"1\">" + "\n";
		cfgcontent += "<seq_search service=\"PsiBlast\" store=\"1\">" + "\n";
		cfgcontent += "<param name=\"e\">" + mcp.geteval3Value() + "</param>" + "\n";
		cfgcontent += "<param name=\"j\">" + mcp.getiter3Value() + "</param>" + "\n";
		cfgcontent += "<param name=\"indexes\">1</param>" + "\n" + "</seq_search>" + "\n";
		cfgcontent += "<seq_filter service=\"CdHit\" store=\"0\">" + "\n";
		cfgcontent += "<param name=\"c\">" + fm.format(mcp.getfilter3Value()/100.0) + "</param>" + "\n" + "</seq_filter>" + "\n";
		cfgcontent += "<seq_align service=\""+mcp.getmsa3Value()+"\" store=\"1\">" + "\n" + "</seq_align>" + "\n" + "</run>" + "\n";
	    }
	    if (mcp.getconsurf4Value())
	    {
		cfgcontent += "<run title=\"" + mcp.getcsftitle4Value() + "\" store=\"1\">" + "\n";
		cfgcontent += "<seq_search service=\"PsiBlast\" store=\"1\">" + "\n";
		cfgcontent += "<param name=\"e\">" + mcp.geteval4Value() + "</param>" + "\n";
		cfgcontent += "<param name=\"j\">" + mcp.getiter4Value() + "</param>" + "\n";
		cfgcontent += "<param name=\"indexes\">1</param>" + "\n" + "</seq_search>" + "\n";
		cfgcontent += "<seq_filter service=\"CdHit\" store=\"0\">" + "\n";
		cfgcontent += "<param name=\"c\">" + fm.format(mcp.getfilter4Value()/100.0) + "</param>" + "\n" + "</seq_filter>" + "\n";
		cfgcontent += "<seq_align service=\""+mcp.getmsa4Value()+"\" store=\"1\">" + "\n" + "</seq_align>" + "\n" + "</run>" + "\n";
	    }
	    cfgcontent += "</service></services></asgard>";
	    
	    //PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("cfg.xml")));
	    //pw.println(cfgcontent);
	    //pw.flush(); pw.close();
	    //	    client.sendFile("cfg", "cfg.xml", cfgcontent);

	    //command line parameters
	    if (mcp.getdaliValue())    cfgcommand += " Dali=1";
	    if (mcp.getdelphiValue())  cfgcommand += " Delphi=1";
	    if (mcp.getconsurf3Value()) 
	    {
		cfgcommand += " C3=BLAST C3T="+mcp.getcsftitle3Value()+" C3E="+mcp.geteval3Value()+" C3I="+mcp.getiter3Value()+" C3P="+mcp.getfilter3Value()+" C3MSA="+mcp.getmsa3Value();
	    }
	    if (mcp.getconsurf4Value()) 
	    {
		cfgcommand += " C4=BLAST C4T="+mcp.getcsftitle3Value()+" C4E="+mcp.geteval3Value()+" C4I="+mcp.getiter3Value()+" C4P="+mcp.getfilter3Value()+" C4MSA="+mcp.getmsa3Value();
	    }

	    cfgcommand += " Skan=1 SkanBox=1 Screen=1 ScreenBox=1 BlastBox=1 IPSBox=1 CBox=1"
	    + " D1B=" + mcp.getibcValue() + " D1EX=" + mcp.getedcValue()
	    + " D1F=" + mcp.getstepsValue() + " D1G=" + mcp.getgridsizeValue() 
	    + " D1IN=" + mcp.getidcValue() + " D1L=" + mcp.getliValue()
	    + " D1N=" + mcp.getnliValue() + " D1P=" + mcp.getboxfillValue()
	    + " D1PR=" + mcp.getradiusValue() + " D1SC=" + mcp.getscValue() 
	    + " C1=PFAM C1T=Pfam C2=BLAST C2T=e=1.0e-3%20identity%200.8 C2E=0.001 C2I=3 C2P=80 C2MSA=Muscle"
	    + " chain_ids=" + chain + " chains=" + chain
	    + " email=" + "a@b" + " infile=" + pdbfilename
	    + " submit=Mark%20Us" + " title=" + "testGW"
	    + " tmpfile=";

	//	client.sendFile("cfg", cfgfile, cfgcommand);

	} catch(Exception e) {
	    System.out.println("MarkUsWeb sendFile error: test.cfg");
	}
	return cfgcommand;
    }


    public void generate_qsubjob(String pdbname) {

	try{
	    String aString = 
		"#!/bin/bash\n\n"+

		"#$ -N gW"+pdbname+"SkyLine\n"+
		"#$ -o sge_output.dat\n"+
		"#$ -e sge_error.dat\n"+
		"#$ -cwd\n"+
		"#$ -S /bin/bash\n"+

		"echo \"job started\"\n"+
		"date\n"+

		//		"#-- perl components/test/src/org/geworkbench/components/test/TestPerl.pl in.txt\n"+
		//		"perl /razor/5/users/mw2518/bin/batch_leverage.pl "+skylineinfile+"\n"+
		"date\n"+
		"echo \"job ended\"\n";

	    //	    client.sendFile("cfg", "test.qsub", aString);

	}catch (Exception e){
	    System.out.println("SkyLineWeb sendFile error: test.qsub");
	}
    }

    public String getType() {
        return "MarkUsAnalysis";
    }
}

