package org.geworkbench.components.alignment.blast;

import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * BlastObj.java A class to hold information about each individual hit of a
 * Blast database search on a protein sequence. <br>
 * Currently this class can only be instantiated by the
 * <code> BlastParser </code> class.
 */
public class BlastObj {

	/**
	 * The Databse ID of the protein sequence hit in this BlastObj.
	 */
	String databaseID;
	/**
	 * Set up the upper boundary of whole sequence size.
	 */
	static final int MAXSEQUENCESIZE = 100000;
	int maxSize = MAXSEQUENCESIZE;

	/**
	 * The accession number of the protein sequence hit in this BlastObj.
	 */
	String name;
	/**
	 * The description of the protein sequence hit in this BlastObj.
	 */
	String description;
	/**
	 * The percentage of the query that's aligned with protein sequence hit in
	 * this BlastObj.
	 */
	int percentAligned;

	/**
	 * The percentage of the alignment of query and hit in this BlastObj that's
	 * gapped.
	 */
	int percentGapped = 0;

	/**
	 * The percentage of the alignment of query and hit in this BlastObj that's
	 * "positive"/conserved.
	 */
	int percentPos;

	/**
	 * The score of the alignment of query and hit in this BlastObj; initialized
	 * to -1.
	 */
	int score = -1;

	/**
	 * The score of the alignment of query and hit in this BlastObj.
	 */
	String evalue;
	/**
	 * The length of the alignment of query and hit in this BlastObj.
	 */
	int length;
	String seqID;
	int[] subject_align = new int[2];

	/**
	 * The 2 element array containing the position in query sequence where the
	 * alignment start and stop. change to public for temp.
	 * 
	 * @todo add getter method later.
	 */
	public int[] query_align = new int[2];
	/**
	 * The String of query sequence in alignment with hit in this BlastObj.
	 */
	String query;
	/**
	 * The String of hit sequence in this BlastObj in alignment with query
	 * sequence.
	 */
	String subject;
	/**
	 * The String of alignment sequence between query sequence and hit in this
	 * BlastObj.
	 */
	String align;
	/**
	 * whether this BlastObj is included in the MSA analysis
	 */
	boolean include = false;
	/**
	 * check whether the whole sequece can be retrived.
	 */
	boolean retriveWholeSeq = false;

	/**
	 * the URL of hit info
	 */
	URL infoURL;
	/**
	 * URL of seq.
	 */
	URL seqURL;
	private String detailedAlignment = "";
	private String identity;
	private int startPoint;
	private int alignmentLength;
	private CSSequence wholeSeq;
	private int endPoint;

	/**
	 * Constructor: is blank.
	 */
	public BlastObj() {
	}

	/* Get methods for class variables. */

	/**
	 * Returns the database name of the hit sequence stored in this BlastObj.
	 * 
	 * @return the accession number as a String.
	 */
	public String getDatabaseID() {
		return databaseID;
	}

	/**
	 * Returns the accession number of the hit sequence stored in this BlastObj.
	 * 
	 * @return the accession number as a String.
	 */

	/**
	 * Returns the description of the hit protein sequence stored in this
	 * BlastObj.
	 * 
	 * @return the description as a String.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the length of the alignment of query and hit seq stored in this
	 * BlastObj.
	 * 
	 * @return the length as an int.
	 */

	/**
	 * Returns the percentage of the query that's aligned with protein sequence
	 * hit in this BlastObj.
	 * 
	 * @return percentAligned as an int.
	 */

	public int getPercentAligned() {
		return percentAligned;
	}

	/**
	 * Returns the percentage of the alignment that's gapped with protein
	 * sequence hit in this BlastObj.
	 * 
	 * @return percentGapped as an int.
	 */
	public int getPercentGapped() {
		return percentGapped;
	}

	/**
	 * Returns the percentage of the query that's conserved with protein
	 * sequence hit in this BlastObj.
	 * 
	 * @return percentPos as an int.
	 */
	public int getPercentPos() {
		return percentPos;
	}

	/**
	 * Returns the score of the alignment of query and protein sequence hit in
	 * this BlastObj.
	 * 
	 * @return score as an int.
	 */
	public int getScore() {
		return score;
	}

	/**
	 * Returns the e-value of the alignment of query and protein sequence hit in
	 * this BlastObj.
	 * 
	 * @return evalue as a String.
	 */
	public String getEvalue() {
		return evalue;
	}

	/**
	 * Returns the query in alignment with protein sequence hit in this
	 * BlastObj.
	 * 
	 * @return query as a String.
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Returns the protein sequence hit in this BlastObj in alignment with the
	 * query.
	 * 
	 * @return subject as a String.
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Returns the alignment of query and protein sequence hit in this BlastObj.
	 * 
	 * @return align as an String.
	 */
	public String getAlign() {
		return align;
	}

	/**
	 * Returns whether this BlastObj is included in MSA analysis.
	 * 
	 * @return include as a Boolean.
	 */
	public boolean getInclude() {
		return include;
	}

	/* Set methods for class variables */

	/**
	 * Sets the database name of the hit protein sequence stored in this
	 * BlastObj.
	 * 
	 * @param s,
	 *            the new database name of this BlastObj.
	 */
	public void setDatabaseID(String s) {
		databaseID = s;
	}

	/**
	 * Sets the accession number of the hit protein sequence stored in this
	 * BlastObj.
	 * 
	 * @param s,
	 *            the new accession number of this BlastObj.
	 */

	/**
	 * Sets the description of the hit protein sequence stored in this BlastObj.
	 * 
	 * @param s,
	 *            the new description of this BlastObj.
	 */
	public void setDescription(String s) {
		description = s;
	}

	/**
	 * Sets the length of the alignment of query and hit protein sequence stored
	 * in this BlastObj.
	 * 
	 * @param i,
	 *            the alignment length of this BlastObj.
	 */

	/**
	 * Sets the percentage of the query that's aligned with protein sequence hit
	 * in this BlastObj.
	 * 
	 * @param i,
	 *            the new alignment percentage of this BlastObj.
	 */
	public void setPercentAligned(int i) {
		percentAligned = i;
	}

	/**
	 * Sets the percentage of the alignment that's gapped of query and hit
	 * protein sequence hit in this BlastObj.
	 * 
	 * @param i,
	 *            the new gapped percentage of this BlastObj.
	 */
	public void setPercentGapped(int i) {
		percentGapped = i;
	}

	/**
	 * Sets the percentage of the query that's conserved with protein sequence
	 * hit in this BlastObj.
	 * 
	 * @param i,
	 *            the new conserved percentage of this BlastObj.
	 */

	public void setPercentPos(int i) {
		percentPos = i;
	}

	/**
	 * Sets the score of the alignment of the query with protein sequence hit in
	 * this BlastObj.
	 * 
	 * @param i,
	 *            the new score of this BlastObj.
	 */

	public void setScore(int i) {
		score = i;
	}

	/**
	 * Sets the e-value of the alignment of the query with protein sequence hit
	 * in this BlastObj.
	 * 
	 * @param s,
	 *            the new e-value of this BlastObj.
	 */
	public void setEvalue(String s) {
		evalue = s;
	}

	/**
	 * Sets the query sequence of the alignment with protein sequence hit in
	 * this BlastObj.
	 * 
	 * @param s,
	 *            the new query seq of this BlastObj.
	 */
	public void setQuery(String s) {
		query = s;
	}

	/**
	 * Sets hit protein sequence hit in this BlastObj in alignment w/ the query.
	 * 
	 * @param s,
	 *            the new subject seq of this BlastObj.
	 */
	public void setSubject(String s) {
		subject = s;
	}

	/**
	 * Sets the alignment sequence of the query with protein sequence hit in
	 * this BlastObj.
	 * 
	 * @param s,
	 *            the new align seq of this BlastObj.
	 */
	public void setAlign(String s) {
		align = s;
	}

	/**
	 * Sets whether this BlastObj is included in MSA analysis.
	 * 
	 * @param b,
	 *            the new include Boolean of this BlastObj.
	 */
	public void setInclude(boolean b) {
		include = b;
	}

	public URL getInfoURL() {
		return infoURL;
	}

	public void setInfoURL(URL infoURL) {
		this.infoURL = infoURL;
	}

	public URL getSeqURL() {
		return seqURL;
	}

	public void setSeqURL(URL seqURL) {
		this.seqURL = seqURL;
	}

	public boolean isRetriveWholeSeq() {
		return retriveWholeSeq;
	}

	public void setRetriveWholeSeq(boolean retriveWholeSeq) {
		this.retriveWholeSeq = retriveWholeSeq;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getDetailedAlignment() {
		return detailedAlignment;
	}

	public void setDetailedAlignment(String detailedAlignment) {
		this.detailedAlignment = detailedAlignment;
	}

	public String toString() {
		return databaseID + detailedAlignment + score + evalue;
	}

	public CSSequence getAlignedSeq() {
		CSSequence seq = new CSSequence(">" + databaseID + "|" + name
				+ "---PARTIALLY INCLUDED", subject);

		return seq;

	}

	/**
	 * getWholeSeq
	 * 
	 * @return Object
	 */
	public CSSequence getWholeSeq() throws BlastDataOutOfBoundException {

		if (retriveWholeSeq && seqURL != null) {
			try {
				InputStream uin = seqURL.openStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						uin));
				String line;
				while ((line = in.readLine()) != null) {

					if (line.startsWith("</form><pre>>")
							|| line.trim().startsWith("</div></form><pre>>")
							|| line.matches("</div></form><pre>>")
							|| line.trim().startsWith(
									"<pre><div class='recordbody'>>")) {
						String[] str = line.split(">>");
						int size = 0;
						StringBuffer name = new StringBuffer();
						String label = "";
						if (str.length > 1) {
							label = ">" + str[1] + "\n";
						}
						while ((line = in.readLine()) != null
								&& !line.startsWith("</div>")
								&& !line.startsWith("</pre>")) {
							size += line.length();
							if (size >= maxSize) {
								throw new BlastDataOutOfBoundException(
										"The sequence "
												+ label
												+ "  is too long to retrieve the whole sequence. The upper limit is "
												+ maxSize + " bases.");
							}
							name.append(line + "\n");
						}
						CSSequence seq = new CSSequence(label, name.toString());
						return seq;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return null;
	}

	public String getSeqID() {
		return seqID;
	}

	public void setSeqID(String seqID) {
		this.seqID = seqID;
	}

	public String getIdentity() {
		return identity;
	}

	public int getStartPoint() {

		return startPoint;
	}

	public int getAlignmentLength() {
		return alignmentLength;
	}

	public int getEndPoint() {
		return endPoint;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public void setStartPoint(int startPoint) {

		this.startPoint = startPoint;
	}

	public void setAlignmentLength(int alignmentLength) {
		this.alignmentLength = alignmentLength;
	}

	public void setWholeSeq(CSSequence wholeSeq) {
		this.wholeSeq = wholeSeq;
	}

	public void setEndPoint(int endPoint) {
		this.endPoint = endPoint;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

}
