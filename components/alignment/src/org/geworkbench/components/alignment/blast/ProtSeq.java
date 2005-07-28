package org.geworkbench.components.alignment.blast;

import java.util.Vector;

/**
 * ProtSeq is a class that stores information associated with a specific
 * protein sequence record retrieved from NCBI Genbank.  It represents the
 * current protein sequence object in memory that all the single sequence-
 * specific functions of ProVIEWER apply to.
 * <br><br>
 * Currently this class can only be instantiated by the <code>Query</code> class.
 */
public class ProtSeq {

    /**
     * The Genbank ID (GI) number of the protein sequence stored in this
     * ProtSeq.
     */
    private String gi;
    /**
     * The subsequence of the protein sequence stored in this ProtSeq.
     */
    private String subseq;
    /**
     * The Accession number of the protein sequence stored in this ProtSeq.
     */
    private String acc_num;
    /**
     * The description of the protein sequence stored in this ProtSeq.
     */
    private String description;
    /**
     * The sequence of the protein sequence stored in this ProtSeq.
     */
    private String sequence;
    /**
     * The length of the protein sequence stored in this ProtSeq.
     */
    private int seq_length;
    /**
     * The Vector of <code>ConservedDomain</code> objects found in the protein
     * sequence stored in this ProtSeq through CDD analysis.
     */
    private Vector domains;

    /**
     * Creates a new ProtSeq with all fields set to null or 0.  Also creates a
     * new Vector for storing conserved domains found in sequence.
     */
    public ProtSeq() {
        this.acc_num = null;
        this.subseq = null;
        this.gi = null;
        this.description = null;
        this.sequence = null;
        this.seq_length = 0;
        this.domains = new Vector();
        this.subseq = null;
    }

    /**
     * Returns formatted String of protein sequence information.
     *
     * @return		a String containing ID numbers, description, length and
     * sequence of protein sequence represented by this ProtSeq
     * object.
     */
    public String print() {
        return ("Genbank ID: " + gi + "\n" + "Accession #: " + acc_num + "\n" + "Sequence: " + description + "\n" + "Length: " + seq_length + " aa\n\n" + sequence + "\n");
    }

    // Get methods for class variables.

    /**
     * Returns the accession number of the protein sequence stored in this ProtSeq.
     *
     * @return		the accession number as a String.
     */
    public String getAccNum() {
        return acc_num;
    }

    /**
     * Returns the GI number of the protein sequence stored in this ProtSeq.
     *
     * @return		the GI number as a String.
     */
    public String getGI() {
        return gi;
    }

    /**
     * Returns the description of the protein sequence stored in this ProtSeq.
     *
     * @return		the description as a String.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the sequence of the protein sequence stored in this ProtSeq.
     *
     * @return		the sequence as a String.
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Returns the subsequence of the protein sequence stored in this ProtSeq.
     *
     * @return		the subsequence as a String.
     */
    public String getSubseq() {
        return subseq;
    }

    /**
     * Returns the length of the protein sequence stored in this ProtSeq.
     *
     * @return		the length as an int.
     */
    public int getSeqLength() {
        return seq_length;
    }

    /**
     * Returns the Vector domains of the protein sequence stored in this
     * ProtSeq.
     *
     * @return		the Vector of type <code>ConservedDomain</code>.
     */
    public Vector getDomains() {
        return domains;
    }


    // Set methods for class variables


    /**
     * Sets the accession number of the protein sequence stored in this
     * ProtSeq.
     *
     * @param		acc_num, the new accession number of this ProtSeq.
     */
    public void setAccNum(String acc_num) {
        this.acc_num = acc_num;
    }

    /**
     * Sets the GI number of the protein sequence stored in this ProtSeq.
     *
     * @param		gi, the new GI number of this ProtSeq.
     */
    public void setGI(String gi) {
        this.gi = gi;
    }

    /**
     * Sets the description of the protein sequence stored in this ProtSeq.
     *
     * @param		description, the new description of this ProtSeq.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the sequence of the protein sequence stored in this ProtSeq.
     *
     * @param		sequence, the new sequence of this ProtSeq.
     */
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    /**
     * Sets the subsequence of the protein sequence stored in this ProtSeq.
     * This method is called by <code>SequenceInfo</code>.
     *
     * @param		subseq, the new subsequence of this ProtSeq.
     */
    public void setSubseq(String subseq) {
        this.subseq = subseq;
    }

    /**
     * Sets the length of the protein sequence stored in this ProtSeq.
     *
     * @param		seq_length, the new length of this ProtSeq.
     */
    public void setSeqLength(int seq_length) {
        this.seq_length = seq_length;
    }

    /**
     * Sets the Vector domains of the protein sequence stored
     * in this ProtSeq.
     *
     * @param		domains, the new Vector domains of this ProtSeq.
     */
    public void setDomains(Vector v) {
        this.domains = v;
    }

    /**
     * Adds a ConservedDomain to the Vector domains of this ProSeq.
     *
     * @param	cd, a new ConservedDomain for this ProtSeq.

     public void addDomain(ConservedDomain cd) {
     domains.addElement(cd);
     }
     */
} //end of class ProtSeq.
