package org.geworkbench.components.alignment.blast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.session.SoapClient;

/**
 * RemoteBlast is a class that implements submission of a protein sequence to
 * the NCBI BLAST server and retrieval of results with a BLAST RID #. It writes
 * retrieved results out to a file.
 * 
 * @author zji
 * @version $Id: RemoteBlast.java,v 1.22 2008-05-14 21:04:49 jiz Exp $
 */
public class RemoteBlast {
	static Log LOG = LogFactory.getLog(RemoteBlast.class);

	private final static String NCBIHEADER = "<HTML><HEAD><meta http-equiv=\"content-type\""
			+ "content=\"text/html;charset=utf-8\" /></HEAD><BODY BGCOLOR=\"#FFFFFF\" LINK=\"#0000FF\" VLINK=\"#660099\" ALINK=\"#660099\">"
			+ "<IMG SRC=\"http://www.ncbi.nlm.nih.gov/blast/images/head_results.gif\"    WIDTH=\"600\" HEIGHT=\"45\" ALIGN=\"middle\">"
			+ "<title>NCBI Blast Result</title><br><br>";

	/**
	 * The protein sequence to submit to Blast.
	 */
	private String query;
	private String waitingTime = "0";

	/**
	 * The default file name to write results out to.
	 */
	private final String DEFAULT_FILENAME = "BLAST_results.txt";
	/**
	 * The file name to write results out to.
	 */
	private String filename;

	/**
	 * The Conserved Domain RID#.
	 */
	private String CDD_rid;
	/**
	 * The default port number for the socket to connect to.
	 */
	private final int DEFAULT_PORT = 80;
	/**
	 * The default server address for the socket to connect to.
	 */
	private final String Blast_SERVER = "www.ncbi.nlm.nih.gov";
	/**
	 * A flag indicating whether Blast results have been retrieve.
	 * <code>true</code> if Blast is done, <code>false</code> if not.
	 */
	private boolean getBlastDone = false;
	/**
	 * Regular expression to parse out CDD RID# with.
	 */
	private Pattern p1 = Pattern.compile("RID=([0-9]+-[0-9]+-[0-9]+)");
	/**
	 * Regular expression to parse out negative CDD Search results with.
	 */
	private Pattern p2 = Pattern
			.compile("No.putative.conserved.domains.have.been.detected");
	/**
	 * the combination of parameters.
	 */
	private String cmdLine;
	/**
	 * The URL of the Blast result corresponds to one sequence. Don't use it in
	 * the problem.
	 */
	private String resultURLString;

	private final String SUBMITPREFIX = "Put http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?CMD=Put&QUERY=";
	private final String RESULTPREFIX = "Get http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?CMD=Get&FORMAT_TYPE=";

	/**
	 * Creates a new RemoteBlast and sets query, filename.
	 * 
	 * @param the
	 *            String value to set query with.
	 * @param the
	 *            String value to set filename with.
	 */
	public RemoteBlast(String query, String filename) {
		this.query = query;
		this.filename = filename;
		this.CDD_rid = null;
	}

	/**
	 * Returns <code>true</code> if Blast is done, <code>false</code> if
	 * not.
	 * 
	 * @return getBlastDone - boolean that indicates if Blast is done.
	 */
	public boolean getBlastDone() {
		return getBlastDone;
	}

	/**
	 * Returns the Conserved Domain Search RID #.
	 * 
	 * @return CDD_rid - the String representing the CDD RID #.
	 */
	public String getCDD_rid() {
		return CDD_rid;
	}

	/**
	 * The exception when the response from NCBI is an error message instead of
	 * normal response with RID.
	 * 
	 * @author zji
	 * 
	 */
	public static class NcbiResponseException extends Exception {
		private static final long serialVersionUID = -1330692467559837833L;

		public NcbiResponseException(String msg) {
			super(msg);
		}
	}

	/**
	 * Get a substring between a given preceding substring and a given following
	 * substring.
	 * 
	 * @param containing string
	 * @param preceding string
	 * @param following string
	 * 
	 * @return the substring or null if not found.
	 */
	private static String getWrappedSubtring(String containing,
			String preceding, String following) {
		int length = preceding.length();
		int index1 = containing.indexOf(preceding);
		int index2 = containing.indexOf(following, index1 + length);
		if (index1 >= 0 && index2 > index1 + length)
			return containing.substring(index1 + length, index2);
		else
			return null;
	}

	/**
	 * Get the error message from one line of NCBI response.
	 * 
	 * @param line
	 * @return the error message if the line contains one; otherwise return null
	 * @throws UnsupportedEncodingException
	 */
	private static String getErrorMessage(String line)
			throws UnsupportedEncodingException {
		String urlString = getWrappedSubtring(line,
				"var myncbi_cu = unescape('", "');");
		if (urlString != null) {
			String error = getWrappedSubtring(URLDecoder.decode(urlString, "UTF-8"),
					"&ERROR=", "&EXPECT");
			if (error != null)
				return error.replace("+", " ");
		}
		return null;
	}

	/**
	 * Creates a socket connection to the NCBI Blast server and submits an HTTP
	 * request to Blast with the query and parses out the Blast RID #. Also
	 * initiates a Conserved Domain Search and parses out the resulting CDD RID #
	 * used for retrieval of CDD Search results if domains found.
	 * 
	 * @return a String representing the Blast RID # used to retrieve Blast
	 *         results, <code>null</code> if not successful.
	 * @throws NcbiResponseException
	 *             when a error message instead of a valid RID is returned by
	 *             NCBI Blast server
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public String submitBlast() throws NcbiResponseException,
			UnknownHostException, IOException {

		String message = ""; /* HTTP GET message */

		Socket s = null;

		if (cmdLine != null) {
			message = SUBMITPREFIX + query + cmdLine;
		}

		s = new Socket(Blast_SERVER, DEFAULT_PORT);

		// create an output stream for sending message.
		DataOutputStream out = new DataOutputStream(s.getOutputStream());

		// create buffered reader stream for reading incoming byte stream.
		InputStreamReader inBytes = new InputStreamReader(s.getInputStream());
		BufferedReader in = new BufferedReader(inBytes);

		// write String message to output stream as byte sequence.
		out.writeBytes(message);

		// reads each incoming line until it finds the CDD and Blast RIDs.
		String line = in.readLine();
		while (line != null) {
			// get CDD_rid. this is done if CDD_rid is still null and doesn't
			// affect the flow of getting RID
			if (CDD_rid == null) {
				Matcher m1 = p1.matcher(line);
				Matcher m2 = p2.matcher(line);
				if (m1.find()) {
					CDD_rid = m1.group(1);
				}
				if (m2.find()) {
					CDD_rid = "none";
				}
			}

			// check error response.
			String errorMessage = getErrorMessage(line);
			if (errorMessage != null)
				throw new NcbiResponseException(errorMessage);

			final int TOKEN_NUMBER_IN_RID_STRING = 3; // RID = ????
			// check RID
			if (line.equals("<!--QBlastInfoBegin")) {
				String nextLine = in.readLine();
				if (nextLine != null) {
					String[] token = nextLine.trim().split(" ");
					if (token.length >= TOKEN_NUMBER_IN_RID_STRING) {
						s.close();
						return token[2];
					} // don't return here so we can further parse error
				} else {
					s.close();
					return null;
				}
			}

			line = in.readLine();
		}

		return null;
	}

	/**
	 * Sets getBlastDone to <code>false</code> indicating Blast is not done
	 * yet and creates a new GetBlast with the specified String as a parameter.
	 * 
	 * @param rid -
	 *            String representing the Blast RID# to retrieve results for.
	 */
	public void getBlast(String rid, String format) {
		getBlastDone = false;
		String message = RESULTPREFIX + format + "&RID=" + rid + "\r\n\r\n";
		resultURLString = "http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?CMD=Get&FORMAT_TYPE="
				+ format + "&RID=";
		LOG.info(new Date() + message);
		BlastThread blastThread = new BlastThread(message);
		blastThread.start();
	}

	/**
	 * This class is a Thread that retrieves Blast results by Blast RID#, which
	 * can take some period of time. The thread continually requests results
	 * from the NCBI Blast server with the Blast RID# via an HTTP request using
	 * a Socket and writes results out to file.
	 */
	private class BlastThread extends Thread {
		String message;

		public BlastThread(String Blast_rid) {
			message = Blast_rid;
		}

		public void run() {
			Socket s;
			try {
				// create an output stream for writing to a file. appending
				// file.
				PrintStream ps = new PrintStream(new FileOutputStream(new File(
						filename), true), true);

				// print header.
				ps.println(NCBIHEADER);

				boolean BlastnotDone = false;
				while (!BlastnotDone) {

					s = new Socket(Blast_SERVER, DEFAULT_PORT);

					// create an output stream for sending message.
					DataOutputStream out = new DataOutputStream(s
							.getOutputStream());

					// create buffered reader stream for reading incoming byte
					// stream.
					InputStreamReader inBytes = new InputStreamReader(s
							.getInputStream());
					BufferedReader in = new BufferedReader(inBytes);

					// write String message to output stream as byte sequence.
					out.writeBytes(message);

					String data = in.readLine();
					boolean done = false;
					boolean getWaitingTime = false;

					while (data != null) {
						if (data.equals("\tStatus=WAITING")) {
							done = false;
						} else if (data.equals("\tStatus=READY")) {
							BlastnotDone = true;
							done = true;
							data = in.readLine();
							data = in.readLine();
							data = in.readLine();
							break;
						}
						if (getWaitingTime) {
							if (data == null) {
								setWaitingTime("0");
							} else {

								setWaitingTime(data.substring(4, 12));
							}

							getWaitingTime = false;
						}
						if (data.trim().startsWith(
								"<tr><td>Time since submission</td>")) {
							getWaitingTime = true;
						}
						data = in.readLine();
					}
					if (!done) {
						Thread.sleep(SoapClient.TIMEGAP);
					} else {
						// TODO Remove the new feature. WE need figure out a way
						// to download the images later.
						boolean needRemoveNewFeature = false;
						while (data != null) {
							boolean debug = false;
							if (data
									.equalsIgnoreCase("Sbjct  159  -KPKTVKAKPVKASKPKKAKP--VKPKAKSSAKRAGKKK  194")) {
								debug = true;
							}
							if (debug) {
								LOG.debug("After 159: " + data);
							}
							data = updateImageLink(data);
							if (data.trim().startsWith(
									"<div id=\"graphic\" class=\"blRes\">")) {
								needRemoveNewFeature = true;
							} else if (data.trim().startsWith(
									"</center><hr></div><!--/#graphic-->")
									&& needRemoveNewFeature) {
								needRemoveNewFeature = false;
							} else if (!needRemoveNewFeature) {
								ps.println(data);
							}
							data = in.readLine();
						}
					}

					s.close();
				} // end of while (BlastnotDone).

				getBlastDone = true;
				ps.close();
			} catch (UnknownHostException e) {
				LOG.warn("Socket:" + e.getMessage());
			} catch (EOFException e) {
				LOG.warn("EOF:" + e.getMessage());
			} catch (IOException e) {
				LOG.warn("readline:" + e.getMessage());
			} catch (InterruptedException e) {
				LOG.warn("wait:" + e.getMessage());
			}
		} // end of run().

	} // end of class BlastThread.

	private String updateImageLink(String data) {
		if (data.indexOf("SRC=\"/blast/images") > -1) {
			data = data.replaceAll("SRC=\"/blast/images",
					"src=\"http://www.ncbi.nlm.nih.gov/blast/images");
		}
		if (data.indexOf("src=\"images") > -1) {
			data = data.replaceAll("src=\"images",
					"src=\"http://www.ncbi.nlm.nih.gov/blast/images");
		}
		if (data.indexOf("SRC=\"images") > -1) {
			data = data.replaceAll("SRC=\"images",
					"src=\"http://www.ncbi.nlm.nih.gov/blast/images");
		}
		if (data.indexOf("src=\"/blast/images") > -1) {
			data = data.replaceAll("src=\"/blast/images",
					"src=\"http://www.ncbi.nlm.nih.gov/blast/images");
		}
		if (data.indexOf("href=\"/blast/") > -1) {
			data = data.replaceAll("href=\"/blast/",
					"href=\"http://www.ncbi.nlm.nih.gov/blast/");
		}
		if (data.indexOf("type=\"checkbox\"") > -1) {
			data = data.replaceAll("type=\"checkbox\"", "type=\"hidden\"");
		}
		if (data.indexOf("type=\"button\"") > -1) {
			data = data.replaceAll("type=\"button\"", "type=\"hidden\"");
		}
		if (data.indexOf("type=button") > -1) {
			data = data.replaceAll("type=button", "type=\"hidden\"");
		}

		return data;
	}

	/**
	 * RemoteBlast
	 * 
	 * @param aQuery
	 *            String
	 */
	public RemoteBlast(String aQuery) {
		this.query = aQuery;
		this.filename = DEFAULT_FILENAME;
		this.CDD_rid = null;
	}

	public void setWaitingTime(String waitingTime) {
		this.waitingTime = waitingTime;
	}

	public void setCmdLine(String cmdLine) {
		this.cmdLine = cmdLine;
	}

	public void setResultURLString(String resultURLString) {
		this.resultURLString = resultURLString;
	}

	public String getWaitingTime() {
		return waitingTime;
	}

	public String getCmdLine() {
		return cmdLine;
	}

	public String getResultURLString() {
		return resultURLString;
	}

}
