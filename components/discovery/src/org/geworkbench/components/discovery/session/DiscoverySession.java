package org.geworkbench.components.discovery.session;

import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.xml.rpc.holders.ByteArrayHolder;
import javax.xml.rpc.holders.DoubleHolder;
import javax.xml.rpc.holders.IntHolder;

import org.apache.axis.types.UnsignedInt;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.util.patterns.PatternOfflet;

import polgara.soapPD_wsdl.LoginToken;
import polgara.soapPD_wsdl.Parameters;
import polgara.soapPD_wsdl.SOAPOffset;
import polgara.soapPD_wsdl.SoapPDPortType;
import polgara.soapPD_wsdl.holders.ArrayOfSOAPOffsetHolder;

/**
 * <p>
 * DiscoverySession
 * </p>
 * <p>
 * Description: Class DiscoverySession describes a session. A session is an
 * abstraction of a session on a SPLASH server on which different queries will
 * be performed.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Aner
 * @version $Id$
 */
public class DiscoverySession {
	// user id and session id as known on the host
	private LoginToken logToken = new LoginToken();

	// we make all our calls through this Soap port.
	private SoapPDPortType soapPort;

	// the database for this session
	private DSSequenceSet<? extends DSSequence> database;

	// sessionType
	private final int sType; // either DNA or Protein

	// the database will be saved with this name on the server
	private String databaseName;

	/**
	 * Create a new session with the given on the remote server
	 * 
	 * @param sessionName
	 *            a name for the session.
	 * @param database
	 *            the sequences database.
	 * @param databaseName
	 *            the name of the database. this name will used to save the
	 *            database on the server.
	 * @param connection
	 *            a link to execute calls to a server.
	 * @param userName
	 *            the user name - ignored
	 * @param userId
	 *            for creating a session
	 * @throws SessionCreationException
	 *             if a a call to the server failed.
	 */
	public DiscoverySession(String sessionName,
			DSSequenceSet<? extends DSSequence> database, String databaseName,
			SoapPDPortType soapPort, String userName, int userId)
			throws SessionCreationException {
		/* dna=0 protein=1 on server */
		sType = (database.isDNA()) ? 0 : 1;

		try {
			this.database = database;
			this.soapPort = soapPort;
			this.databaseName = databaseName;

			int sessionId = soapPort.createSession(sessionName,
					new UnsignedInt(userId), sType);

			logToken.setUserId(new UnsignedInt(userId));
			logToken.setSessionId(new UnsignedInt(sessionId));
		} catch (RemoteException exp) {
			throw new SessionCreationException("Could not reach the server.");
		}
	}

	/**
	 * This method uploads a single sequence from this sessions' sequence file.
	 * A call to loadSequenceRemote will save uploading time if this session's
	 * database was previously loaded.
	 * 
	 * @param index
	 *            the index of the sequence to upload. 0<= index < SequenceNo
	 * @throws SessionOperationException
	 *             if upload fails.
	 */
	public void upload(int index) throws SessionOperationException {
		if ((index < 0) || (index >= database.getSequenceNo())) {
			throw new IndexOutOfBoundsException("0<= idexRange <"
					+ database.getSequenceNo() + " ; index = " + index);
		}

		try {
			CSSequence seq = (CSSequence) database.getSequence(index);
			seq.maskRepeats();
			addSequence(seq.getSequence(), seq.getLabel(), sType);
		} catch (RemoteException exp) {
			throw new SessionOperationException("Could not load Sequences.");
		}
	}

	/**
	 * The method returns the sequence database.
	 * 
	 * @return the sequence database
	 */
	@SuppressWarnings("unchecked")
	public synchronized DSSequenceSet<DSSequence> getSequenceDB() {
		return (DSSequenceSet<DSSequence>) database;
	}

	/**
	 * Set the search parameters.
	 * 
	 * @throws SessionOperationException
	 *             if the parameters cannot be set.
	 */
	public void setParameters(Parameters parms)
			throws SessionOperationException {
		try {
			// returned int is ignored
			soapPort.setParameters(logToken, parms);
		} catch (RemoteException exp) {
			
			throw new SessionOperationException(
					"Could not set parameters for the session.");
		}
	}

	// TODO document what this method return before the session is done
	/**
	 * Get the number of patterns found.
	 * 
	 * @throws SessionOperationException
	 */
	public int getPatternNo() throws SessionOperationException {
		try {
			return soapPort.getPatternNo(logToken);
		} catch (RemoteException ex) {
			throw new SessionOperationException(
					"Could not get total pattern number.");
		}
	}

	/**
	 * Add a sequence to the session.
	 * 
	 * @throws RemoteException
	 */
	private int addSequence(java.lang.String sequence, java.lang.String label,
			int isDNA) throws RemoteException {
		return soapPort.addSequence(logToken, sequence, label, isDNA);
	}

	/**
	 * This method reports if a discovery is running or finished.
	 * 
	 * @return true if finished, else false
	 * @throws SessionOperationException
	 *             if a a call to the server failed.
	 */
	public boolean isDone() throws SessionOperationException {
		try {
			return soapPort.isDone(logToken);
		} catch (Exception ex) {
			throw new SessionOperationException(
					"Could not determine session status.");
		}
	}

	/**
	 * Save the database on the server.
	 */
	public void saveSeqDB() throws SessionOperationException {
		try {
			// returned int is ignored
			soapPort.saveSeqDB(logToken, databaseName);
		} catch (java.rmi.RemoteException exp) {
			throw new SessionOperationException("Could not save the sequences.");
		}
	}

	/**
	 * Start a discovery on the database. The method blocks until the discovery
	 * is done.
	 * 
	 * @throws SessionOperationException
	 *             if the discovery can't run.
	 */
	public void discover(String algorithm) throws SessionOperationException {
		try {
			// returned it is ignored
			soapPort.discover(logToken, algorithm);
		} catch (RemoteException ex) {
			String msg = ex.getMessage();
			System.out.println(msg);
			throw new SessionOperationException("Unable to run discovery. "
					+ msg);
			
		}
	}

	public void getPattern(int patId,
			org.geworkbench.util.patterns.CSMatchedSeqPattern pattern)
			throws SessionOperationException {
		DoubleHolder pValue = new DoubleHolder();
		ByteArrayHolder loci = new ByteArrayHolder();

		try {

			ArrayOfSOAPOffsetHolder arrayOfSOAPOffsetHolder = new ArrayOfSOAPOffsetHolder();
			IntHolder idNo = new IntHolder();
			IntHolder seqNo = new IntHolder();
			soapPort.getPattern(logToken, patId, idNo, seqNo, pValue,
					arrayOfSOAPOffsetHolder, loci);
			pattern.setIdNo(idNo.value);
			pattern.setSeqNo(seqNo.value);
			pattern.setPValue(pValue.value);
			pattern.setLocus(loci.value);

			SOAPOffset[] values = arrayOfSOAPOffsetHolder.value;
			ArrayList<PatternOfflet> arrayList = new ArrayList<PatternOfflet>();
			for (int i = 0; i < values.length; i++) {
				PatternOfflet patternOfflet = new PatternOfflet(
						values[i].getDx(), values[i].getToken());
				arrayList.add(i, patternOfflet);
			}

			pattern.setOffset(arrayList);
		} catch (RemoteException ex) {
			throw new SessionOperationException("Could not get the pattern.");
		}
	}

	/**
	 * Sort the patterns on the server.
	 * 
	 */
	public void sortPatterns(int sortMode) throws SessionOperationException {
		try {
			// returned int is ignored
			soapPort.sortPatterns(logToken, sortMode);
		} catch (Exception ex) {
			throw new SessionOperationException("Could not sort the patterns.");
		}
	}

	/**
	 * Return the percentage of completion search.
	 * 
	 * @throws SessionOperationException
	 */
	public double getCompletion() throws SessionOperationException {
		try {
			return soapPort.getCompletion(logToken);
		} catch (RemoteException ex) {
			throw new SessionOperationException(
					"Could not get completion status.");
		}
	}

	public void stop() throws SessionOperationException {
		try {
			soapPort.stop(logToken);
		} catch (RemoteException ex) {
			System.out.println("ex: " + ex.getMessage());
			throw new SessionOperationException("Could not stop the algorithm.");
		}
	}

	/**
	 * Mask a pattern on the server.
	 * 
	 * @throws SessionOperationException
	 */
	public void maskPattern(int patId, int complete)
			throws SessionOperationException {
		try {
			// returned int is ignored
			soapPort.maskPattern(logToken, patId, complete);
		} catch (RemoteException ex) {
			throw new SessionOperationException("Could not mask the pattern.");
		}
	}

	/**
	 * Unmask the entire sequences on the server.
	 * 
	 * @throws SessionOperationException
	 */
	public void unmask() throws SessionOperationException {
		try {
			soapPort.unmask(logToken);
		} catch (RemoteException ex) {
			throw new SessionOperationException("Could not mask all sequences.");
		}
	}

}
