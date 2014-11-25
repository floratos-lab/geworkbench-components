package org.geworkbench.components.pudge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.bioobjects.structure.PudgeResultSet;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ProteinSequenceAnalysis;

/**
 * Pudge analysis for protein fasta sequence
 * 
 * @author mw2518
 * @version $Id: PudgeAnalysis.java,v 1.4 2009-09-10 16:40:26 chiangy Exp $
 */
public class PudgeAnalysis extends AbstractAnalysis implements
		ProteinSequenceAnalysis {
	private static final long serialVersionUID = 1L;
	public PudgeConfigPanel pcp;
	String strurl = "https://honiglab.c2b2.columbia.edu/pudge/cgi-bin/pipe_int.cgi";
	String req = "--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"dir_name\"\r\n\r\n@\r\n--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"dummy\"\r\n\r\nacademic\r\n--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"domain\"\r\n\r\nnone\r\n--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"start\"\r\n\r\nT\r\n--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"enter\"\r\n\r\nselect_methods\r\n--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"got1\"\r\n\r\n\r\n--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"file\"; filename=\"protein.fasta\"\r\nContent-Type: text/plain\r\n\r\n";

	PudgeAnalysis() {
		pcp = new PudgeConfigPanel();
		setDefaultPanel(pcp);
	}

	@SuppressWarnings("unchecked")
	public AlgorithmExecutionResults execute(Object input) {
		if (input == null)
			return new AlgorithmExecutionResults(false, "Invalid input. ", null);
		assert input instanceof DSSequenceSet;

		DSSequenceSet<? extends DSSequence> seq = (DSSequenceSet<? extends DSSequence>) input;
		File seqfile = seq.getFile();
		File fastafile = seqfile.getAbsoluteFile();
		String resultURL = "";

		try {
			URL url = new URL(strurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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

			//append _geworkbench_timestamp to user specified jobname
			//to specify pudge jobs sent from geworkbench
			String jobname = pcp.getjobnameValue() + "_geworkbench_"
					+ new java.sql.Timestamp(new java.util.Date().getTime())
							.toString().replaceAll("[-:.]", "");
			String request = req
					.replaceFirst("@", jobname.replaceAll("[^a-zA-Z0-9_]", "_"));
			if (pcp.isAcademic())
				request = request.replaceFirst("dummy", "academic_user");
			OutputStream out = conn.getOutputStream();
			out.write(request.getBytes());
			FileInputStream fis = new FileInputStream(fastafile);
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
				BufferedReader in = new BufferedReader(new InputStreamReader(
						dat));
				String line;
				while ((line = in.readLine()) != null) {
					if (line.startsWith("window.location")) {
						resultURL = line.substring(19, line.length() - 2);
					}
				}
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (resultURL.length() == 0)
			return new AlgorithmExecutionResults(false,
					"Error in pudge website",
					new PudgeResultSet(seq, resultURL));

		return new AlgorithmExecutionResults(true, "No errors",
				new PudgeResultSet(seq, resultURL));
	}

	public int getAnalysisType() {
		return PUDGE_TYPE;
	}

}
