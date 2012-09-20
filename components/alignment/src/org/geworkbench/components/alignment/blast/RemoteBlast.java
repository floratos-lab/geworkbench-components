package org.geworkbench.components.alignment.blast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * RemoteBlast is a class that implements submission of a protein sequence to
 * the NCBI BLAST server and retrieval of results with a BLAST RID #. It writes
 * retrieved results out to a file.
 *
 * @author zji
 * @version $Id$
 */
public class RemoteBlast {
	static Log LOG = LogFactory.getLog(RemoteBlast.class);

	private final static String NCBIHEADER = "<HTML><HEAD><meta http-equiv=\"content-type\""
			+ "content=\"text/html;charset=utf-8\" /></HEAD><BODY BGCOLOR=\"#FFFFFF\" LINK=\"#0000FF\" VLINK=\"#660099\" ALINK=\"#660099\">"
			+ "<IMG SRC=\"http://blast.ncbi.nlm.nih.gov/images/head_results.gif\"    WIDTH=\"600\" HEIGHT=\"45\" ALIGN=\"middle\">"
			+ "<title>NCBI Blast Result</title><br><br>";

	/**
	 * The protein sequence to submit to Blast.
	 */
	private String query;

	/**
	 * The file name to write results out to.
	 */
	private String filename;

	/**
	 * The Conserved Domain RID#.
	 */
	private String CDD_rid;

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
	 * Creates a new RemoteBlast and sets query, filename.
	 *
	 * @param the
	 *            String value to set query with.
	 * @param the
	 *            String value to set filename with.
	 */
	public RemoteBlast(String query, String filename, String cmdLine) {
		this.query = query;
		this.filename = filename;
		this.cmdLine = cmdLine;

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
			// need to decode twice to make it really clear of %
			String error = getWrappedSubtring(URLDecoder.decode(URLDecoder
					.decode(urlString, "UTF-8"), "UTF-8"), "&ERROR=", "&EXPECT");
			if (error != null)
				return error.replace("+", " ");
		}
		return null;
	}


	String submitBlast() throws NcbiResponseException {
		HttpClient client = new HttpClient();
		DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(
				10, true);
		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				retryhandler);
		client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

		String submitURLString = "http://blast.ncbi.nlm.nih.gov/Blast.cgi?CMD=Put"
				+ cmdLine;
		LOG.debug("query URL string: '"+submitURLString+"'");
		PostMethod post = new PostMethod(submitURLString);
		NameValuePair[] data = {
		          new NameValuePair("QUERY", query)
		        };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode == HttpStatus.SC_OK) {
				InputStream stream = post.getResponseBodyAsStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						stream));
				String line = null;
				while ((line = in.readLine()) != null) {
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
						if (nextLine == null) throw new NcbiResponseException("No status after <!--QBlastInfoBegin");

						String[] token = nextLine.trim().split(" ");
						if (token.length >= TOKEN_NUMBER_IN_RID_STRING) {
							return token[2];
						} // don't return here so we can further parse error
					}
				}
				throw new NcbiResponseException("No status in entire response");
			} else {
				LOG.error("Submission returns error status "+statusCode);
				throw new NcbiResponseException("Submission returns error status "+statusCode);
			}
		} catch (HttpException e) {
			throw new NcbiResponseException(e.getMessage());
		} catch (IOException e) {
			throw new NcbiResponseException(e.getMessage());
		} finally {
			post.releaseConnection();
		}
	}

	enum Status {READY, WAITING, ERROR}
	Status retrieveResult(String resultURLString) {			 
		HttpClient client = new HttpClient();
		DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(
				10, true);
		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				retryhandler);
		client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

		GetMethod getMethod = new GetMethod(resultURLString);
		try {
			int statusCode = client.executeMethod(getMethod);

			if (statusCode == HttpStatus.SC_OK) {
				InputStream stream = getMethod.getResponseBodyAsStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						stream));
				String line = in.readLine();
				StringBuilder sb = new StringBuilder();
				while (line != null) {
					line = updateImageLink(line);
					sb.append(line).append(System.getProperty("line.separator"));
					line = in.readLine();
				}
				stream.close();
				
				String s = sb.toString();
				if(s.contains("READY")) {
					s = removeBeginningPart(s); // remove everything before READY
					s = s.replaceAll("\\<iframe.*\\</iframe\\>", "");
					s = s
							.replaceAll(
									"\\s\\<div id=\"graphic\" class=\"blRes\"\\>(?s).+?\\</center\\>\\<hr\\>\\</div\\>",
									""); // remove the graphic block
					
					// true flag for append: useful for now for multiple sequences
					PrintWriter pw = new PrintWriter(new FileWriter(new File(filename), true)); //"RETRIEVED_"+rid+"_"+System.currentTimeMillis()+".html"));
					pw.println(NCBIHEADER);
					pw.println( s );
					pw.close();
					return Status.READY;
				} else if(s.contains("WAITING")) {
					LOG.debug("... waiting for blast result");
					return Status.WAITING;
				} else {
					PrintWriter pw = new PrintWriter(new File(filename));
					pw.print(s);
					pw.close();
					LOG.debug("... blast response does not have proper status");
					return Status.ERROR;
				}
			} else {				
				LOG.error("retrieve failed for " + resultURLString);
				LOG.error("status code=" + statusCode);
				String s = "Error: retrieve failed, status code=" + statusCode;
				PrintWriter pw = new PrintWriter(new File(filename));
				pw.println(s);
				pw.close();
				return Status.ERROR;
			}
		} catch (HttpException e) {
			e.printStackTrace();
			String s = "Error: " + e.getClass().getName() + ":" + e.getMessage();
			PrintWriter pw;
			try {
				pw = new PrintWriter(new File(filename));
				pw.println(s);
				pw.close();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			return Status.ERROR;
		} catch (IOException e) {
			e.printStackTrace();
			String s = "Error: " + e.getClass().getName() + ":" + e.getMessage();
			PrintWriter pw;
			try {
				pw = new PrintWriter(new File(filename));
				pw.println(s);
				pw.close();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return Status.ERROR;
		} finally {
			getMethod.releaseConnection();
		}

	}

	private String removeBeginningPart(String s) {
		int index = s.indexOf("READY");
		index = s.indexOf("</p>", index);
		return s.substring(index+"</p>".length());
	}

	private String updateImageLink(String data) {
		data = data.replaceAll("(src|SRC)=\"(/blast/)?images",
				"src=\"http://blast.ncbi.nlm.nih.gov/images");
		data = data.replaceAll("href=\"/blast/",
				"href=\"http://blast.ncbi.nlm.nih.gov/");
		data = data.replaceAll("type=(\"checkbox\"|\"?button\"?)", "type=\"hidden\"");

		return data;
	}

}
