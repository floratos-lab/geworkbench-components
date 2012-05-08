package org.geworkbench.components.interactions.cellularnetwork;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbench.util.UnAuthenticatedException;
import org.geworkbench.util.network.InteractionDetail;

public class InteractionsConnectionImpl {
	/**
	 * Logger for this class
	 */
	private static final Log logger = LogFactory
			.getLog(InteractionsConnectionImpl.class);

	private static final String ENTREZ_GENE = "Entrez Gene";

	public InteractionsConnectionImpl() {
	}

	public List<InteractionDetail> getInteractionsByEntrezIdOrGeneSymbol_1(
			DSGeneMarker marker, String context, String version)
			throws UnAuthenticatedException, ConnectException,
			SocketTimeoutException, IOException {
		String interactionType = null;
		String msid2 = null;
		String msid1 = null;
		String geneName1 = null;
		String geneName2 = null;
		String db2_xref = null;
		String interactionId = null;
		Short evidenceId = 0;

		double confidenceValue = 0d;
		short confidenceType;

		List<InteractionDetail> arrayList = new ArrayList<InteractionDetail>();

		ResultSetlUtil rs = null;

		try {

			msid1 = new Integer(marker.getGeneId()).toString();
			geneName1 = marker.getGeneName();
			String methodAndParams = "getInteractionsByEntrezIdOrGeneSymbol"
					+ Constants.DEL + msid1 + Constants.DEL + geneName1
					+ Constants.DEL + context + Constants.DEL + version;

			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.getUrl());
			String previousInteractionId = null;
			boolean firstHit = true;
			while (rs.next()) {
				try {
					msid2 = rs.getString("primary_accession");
					geneName2 = rs.getString("gene_symbol");
					db2_xref = rs.getString("accession_db");
					interactionId = rs.getString("interaction_id");
					if (previousInteractionId == null
							|| !previousInteractionId.equals(interactionId)) {
						previousInteractionId = interactionId;
						firstHit = true;
					}
					if ((db2_xref.equals(ENTREZ_GENE) && msid1.equals(msid2))
							|| (geneName2.equalsIgnoreCase(geneName1))) {
						if (firstHit == true) {
							firstHit = false;
							continue;
						} else {
							msid2 = msid1;
							db2_xref = ENTREZ_GENE;
						}
					}

					interactionType = rs.getString("interaction_type").trim();
					if (rs.getString("evidence_id") != null
							&& !rs.getString("evidence_id").trim()
									.equals("null"))
						evidenceId = new Short(rs.getString("evidence_id"));

					InteractionDetail interactionDetail = new InteractionDetail(
							msid2, geneName2, db2_xref, interactionType,
							interactionId, evidenceId);

					confidenceValue = rs.getDouble("confidence_value");
					confidenceType = new Short(rs.getString("confidence_type")
							.trim());
					interactionDetail.addConfidence(confidenceValue,
							confidenceType);
					String otherConfidenceValues = rs
							.getString("other_confidence_values");
					String otherConfidenceTypes = rs
							.getString("other_confidence_types");
					if (!otherConfidenceValues.equals("null")) {
						String[] values = otherConfidenceValues.split(";");
						String[] types = otherConfidenceTypes.split(";");

						for (int i = 0; i < values.length; i++)
							interactionDetail.addConfidence(new Double(
									values[i]), new Short(types[i]));

					}

					arrayList.add(interactionDetail);

				} catch (NullPointerException npe) {
					if (logger.isErrorEnabled()) {
						logger.error("db row is dropped because a NullPointerException");
					}
				}
			}
			rs.close();
		} catch (UnAuthenticatedException uae) {
			throw new UnAuthenticatedException(uae.getMessage());

		} catch (ConnectException ce) {
			if (logger.isErrorEnabled()) {
				logger.error(ce.getMessage());
			}
			throw new ConnectException(ce.getMessage());
		} catch (SocketTimeoutException se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage());
			}
			throw new SocketTimeoutException(se.getMessage());
		} catch (IOException ie) {
			if (logger.isErrorEnabled()) {
				logger.error(ie.getMessage());
			}
			throw new IOException(ie.getMessage());

		} catch (Exception se) {
			if (logger.isErrorEnabled()) {
				logger.error("getPairWiseInteraction(BigDecimal) - ResultSetlUtil rs=" + rs); //$NON-NLS-1$
			}
			se.printStackTrace();
		}
		return arrayList;
	}

	public List<InteractionDetail> getInteractionsByEntrezIdOrGeneSymbol_2(
			DSGeneMarker marker, String context, String version)
			throws UnAuthenticatedException, ConnectException,
			SocketTimeoutException, IOException {
		String interactionType = null;
		String msid = null;
		String geneName = null;
		String db_xref = null;
		String interactionId = null;

		double confidenceValue = 1d;
		short confidenceType;

		Short evidenceId = 0;

		List<InteractionDetail> arrayList = new ArrayList<InteractionDetail>();

		ResultSetlUtil rs = null;

		try {

			String marker_msid = new Integer(marker.getGeneId()).toString();
			String marker_geneName = marker.getGeneName();

			String methodAndParams = "getInteractionsByEntrezIdOrGeneSymbol"
					+ Constants.DEL + marker_msid + Constants.DEL
					+ marker_geneName + Constants.DEL + context + Constants.DEL
					+ version;

			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.getUrl());

			String previousInteractionId = null;
			List<InteractionParticipant> participantList = new ArrayList<InteractionParticipant>();
			while (rs.next()) {
				try {
					msid = rs.getString("primary_accession");
					geneName = rs.getString("gene_symbol");
			 
					db_xref = rs.getString("accession_db");
					interactionType = rs.getString("interaction_type").trim();
					interactionId = rs.getString("interaction_id");
					if (rs.getString("evidence_id") != null
							&& !rs.getString("evidence_id").trim()
									.equals("null"))
						evidenceId = new Short(rs.getString("evidence_id"));
					if (!db_xref.equalsIgnoreCase(ENTREZ_GENE)
							&& geneName.equals(marker_geneName)) {
						msid = marker_msid;
						db_xref = ENTREZ_GENE;
					}

					if (previousInteractionId == null
							|| !previousInteractionId.equals(interactionId)) {
						previousInteractionId = interactionId;
						participantList.clear();
					} else {
						for (InteractionParticipant p : participantList) {
							InteractionDetail interactionDetail = null;
							 
							if ((p.getdSGeneName() != null && p.getdSGeneName()
									.equalsIgnoreCase(marker_geneName))
									|| (p.getdSGeneId() != null && p
											.getdSGeneId().equals(marker_msid)))
								interactionDetail = new InteractionDetail(msid,
										geneName, db_xref, interactionType,
										interactionId, evidenceId);
							else
								interactionDetail = new InteractionDetail(
										p.getdSGeneId(), p.getdSGeneName(),
										p.getDbSource(), interactionType,
										interactionId, evidenceId);

							confidenceValue = rs.getDouble("confidence_value");
							confidenceType = new Short(rs.getString(
									"confidence_type").trim());
							interactionDetail.addConfidence(confidenceValue,
									confidenceType);
							String otherConfidenceValues = rs
									.getString("other_confidence_values");
							String otherConfidenceTypes = rs
									.getString("other_confidence_types");
							if (!otherConfidenceValues.equals("null")) {
								String[] values = otherConfidenceValues
										.split(";");
								String[] types = otherConfidenceTypes
										.split(";");

								for (int i = 0; i < values.length; i++)
									interactionDetail.addConfidence(new Double(
											values[i]), new Short(types[i]));

							}

							arrayList.add(interactionDetail);

						}
					}

					participantList.add(new InteractionParticipant(msid,
							geneName, db_xref));

				} catch (NullPointerException npe) {
					if (logger.isErrorEnabled()) {
						logger.error("db row is dropped because a NullPointerException");
					}				
					
				} catch (NumberFormatException nfe)
				{
					if (logger.isErrorEnabled()) {
						logger.error("db row is dropped because a NumberFormatExceptio");
					}		
				}
			}
			rs.close();
		} catch (UnAuthenticatedException uae) {
			throw new UnAuthenticatedException(uae.getMessage());

		} catch (ConnectException ce) {
			if (logger.isErrorEnabled()) {
				logger.error(ce.getMessage());
			}
			throw new ConnectException(ce.getMessage());
		} catch (SocketTimeoutException se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage());
			}
			throw new SocketTimeoutException(se.getMessage());
		} catch (IOException ie) {
			if (logger.isErrorEnabled()) {
				logger.error(ie.getMessage());
			}
			throw new IOException(ie.getMessage());

		} catch (Exception se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage()); //$NON-NLS-1$
			}
			se.printStackTrace();
		}
		return arrayList;
	}
	
	

	public List<String> getInteractionsSifFormat(String context,
			String version, String interactionType, String presentBy)
			throws UnAuthenticatedException, ConnectException,
			SocketTimeoutException, IOException {

		List<String> arrayList = new ArrayList<String>();
		ResultSetlUtil rs = null;

		try {

			String methodAndParams = "getInteractionsSifFormat" + Constants.DEL
					+ context + Constants.DEL + version + Constants.DEL
					+ interactionType + Constants.DEL + presentBy;
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.getUrl());

			String sifLine = null;
			while (rs.next()) {
				try {
					sifLine = rs.getString("sif format data");
					arrayList.add(sifLine);
				} catch (NullPointerException npe) {
					if (logger.isErrorEnabled()) {
						logger.error("db row is dropped because a NullPointerException");
					}
				}
			}
			rs.close();
		} catch (UnAuthenticatedException uae) {
			throw new UnAuthenticatedException(uae.getMessage());

		} catch (ConnectException ce) {
			if (logger.isErrorEnabled()) {
				logger.error(ce.getMessage());
			}
			throw new ConnectException(ce.getMessage());
		} catch (SocketTimeoutException se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage());
			}
			throw new SocketTimeoutException(se.getMessage());
		} catch (IOException ie) {
			if (logger.isErrorEnabled()) {
				logger.error(ie.getMessage());
			}
			throw new IOException(ie.getMessage());

		} catch (Exception se) {
			if (logger.isErrorEnabled()) {
				logger.error("getInteractionsSifFormat - ResultSetlUtil rs=" + rs); //$NON-NLS-1$
			}
			se.printStackTrace();
		}
		return arrayList;
	}

	public List<String> getInteractionsAdjFormat(String context,
			String version, String interactionType, String presentBy)
			throws UnAuthenticatedException, ConnectException,
			SocketTimeoutException, IOException {

		List<String> arrayList = new ArrayList<String>();
		ResultSetlUtil rs = null;

		try {

			String methodAndParams = "getInteractionsAdjFormat" + Constants.DEL
					+ context + Constants.DEL + version + Constants.DEL
					+ interactionType + Constants.DEL + presentBy;
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.getUrl());

			String adjLine = null;
			while (rs.next()) {
				try {
					adjLine = rs.getString("adj format data");
					arrayList.add(adjLine);
				} catch (NullPointerException npe) {
					if (logger.isErrorEnabled()) {
						logger.error("db row is dropped because a NullPointerException");
					}
				}
			}
			rs.close();
		} catch (UnAuthenticatedException uae) {
			throw new UnAuthenticatedException(uae.getMessage());

		} catch (ConnectException ce) {
			if (logger.isErrorEnabled()) {
				logger.error(ce.getMessage());
			}
			throw new ConnectException(ce.getMessage());
		} catch (SocketTimeoutException se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage());
			}
			throw new SocketTimeoutException(se.getMessage());
		} catch (IOException ie) {
			if (logger.isErrorEnabled()) {
				logger.error(ie.getMessage());
			}
			throw new IOException(ie.getMessage());

		} catch (Exception se) {
			if (logger.isErrorEnabled()) {
				logger.error("getInteractionsAdjFormat - ResultSetlUtil rs=" + rs); //$NON-NLS-1$
			}
			se.printStackTrace();
		}
		return arrayList;
	}

	public HashMap<String, String> getInteractionTypeMap()
			throws ConnectException, SocketTimeoutException, IOException {
		HashMap<String, String> map = new HashMap<String, String>();

		ResultSetlUtil rs = null;
		String interactionType = null;
		String short_name = null;

		try {

			String methodAndParams = "getInteractionTypes";
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.getUrl());

			while (rs.next()) {

				interactionType = rs.getString("interaction_type").trim();
				short_name = rs.getString("short_name").trim();

				map.put(interactionType, short_name);
				map.put(short_name, interactionType);
			}
			rs.close();

		} catch (ConnectException ce) {
			if (logger.isErrorEnabled()) {
				logger.error(ce.getMessage());
			}
			throw new ConnectException(ce.getMessage());
		} catch (SocketTimeoutException se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage());
			}
			throw new SocketTimeoutException(se.getMessage());
		} catch (IOException ie) {
			if (logger.isErrorEnabled()) {
				logger.error(ie.getMessage());
			}
			throw new IOException(ie.getMessage());

		} catch (Exception se) {
			if (logger.isErrorEnabled()) {
				logger.error("getInteractionTypes() - ResultSetlUtil: " + se.getMessage()); //$NON-NLS-1$
			}

		}
		return map;
	}

	public HashMap<String, String> getInteractionEvidenceMap()
			throws ConnectException, SocketTimeoutException, IOException {
		HashMap<String, String> map = new HashMap<String, String>();

		ResultSetlUtil rs = null;
		String evidenceDesc = null;
		String evidenceId;

		try {

			String methodAndParams = "getInteractionEvidences";
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.getUrl());

			while (rs.next()) {

				evidenceDesc = rs.getString("description");
				evidenceId = rs.getString("id");

				map.put(evidenceId, evidenceDesc);
				map.put(evidenceDesc, evidenceId);
			}
			rs.close();

		} catch (ConnectException ce) {
			if (logger.isErrorEnabled()) {
				logger.error(ce.getMessage());
			}
			throw new ConnectException(ce.getMessage());
		} catch (SocketTimeoutException se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage());
			}
			throw new SocketTimeoutException(se.getMessage());
		} catch (IOException ie) {
			if (logger.isErrorEnabled()) {
				logger.error(ie.getMessage());
			}
			throw new IOException(ie.getMessage());

		} catch (Exception se) {
			if (logger.isErrorEnabled()) {
				logger.error("getInteractionTypes() - ResultSetlUtil: " + se.getMessage()); //$NON-NLS-1$
			}

		}
		return map;
	}

	public HashMap<String, String> getConfidenceTypeMap()
			throws ConnectException, SocketTimeoutException, IOException {
		HashMap<String, String> map = new HashMap<String, String>();

		ResultSetlUtil rs = null;
		String confidenceType = null;
		String id = null;

		try {

			String methodAndParams = "getConfidenceTypes";
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.getUrl());

			while (rs.next()) {

				confidenceType = rs.getString("name").trim();
				id = rs.getString("id").trim();

				map.put(confidenceType, id);
				map.put(id, confidenceType);
			}
			rs.close();

		} catch (ConnectException ce) {
			if (logger.isErrorEnabled()) {
				logger.error(ce.getMessage());
			}
			throw new ConnectException(ce.getMessage());
		} catch (SocketTimeoutException se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage());
			}
			throw new SocketTimeoutException(se.getMessage());
		} catch (IOException ie) {
			if (logger.isErrorEnabled()) {
				logger.error(ie.getMessage());
			}
			throw new IOException(ie.getMessage());

		} catch (Exception se) {
			if (logger.isErrorEnabled()) {
				logger.error("getConfidenceTypesMap() - ResultSetlUtil: " + se.getMessage()); //$NON-NLS-1$
			}

		}
		return map;
	}

	public List<String> getInteractionTypes() throws ConnectException,
			SocketTimeoutException, IOException {
		List<String> arrayList = new ArrayList<String>();

		ResultSetlUtil rs = null;
		String interactionType = null;

		try {

			String methodAndParams = "getInteractionTypes";
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.getUrl());

			while (rs.next()) {

				interactionType = rs.getString("interaction_type").trim();

				arrayList.add(interactionType);
			}
			rs.close();

		} catch (ConnectException ce) {
			if (logger.isErrorEnabled()) {
				logger.error(ce.getMessage());
			}
			throw new ConnectException(ce.getMessage());
		} catch (SocketTimeoutException se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage());
			}
			throw new SocketTimeoutException(se.getMessage());
		} catch (IOException ie) {
			if (logger.isErrorEnabled()) {
				logger.error(ie.getMessage());
			}
			throw new IOException(ie.getMessage());

		} catch (Exception se) {
			if (logger.isErrorEnabled()) {
				logger.error("getInteractionTypes() - ResultSetlUtil: " + se.getMessage()); //$NON-NLS-1$
			}

		}
		return arrayList;
	}

	public List<String> getInteractionTypesByInteractomeVersion(String context,
			String version) throws ConnectException, SocketTimeoutException,
			IOException {
		List<String> arrayList = new ArrayList<String>();

		ResultSetlUtil rs = null;
		String interactionType = null;

		try {

			String methodAndParams = "getInteractionTypesByInteractomeVersion"
					+ Constants.DEL + context + Constants.DEL + version;
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.getUrl());

			while (rs.next()) {

				interactionType = rs.getString("interaction_type").trim();

				arrayList.add(interactionType);
			}
			rs.close();

		} catch (ConnectException ce) {
			if (logger.isErrorEnabled()) {
				logger.error(ce.getMessage());
			}
			throw new ConnectException(ce.getMessage());
		} catch (SocketTimeoutException se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage());
			}
			throw new SocketTimeoutException(se.getMessage());
		} catch (IOException ie) {
			if (logger.isErrorEnabled()) {
				logger.error(ie.getMessage());
			}
			throw new IOException(ie.getMessage());

		} catch (Exception se) {
			if (logger.isErrorEnabled()) {
				logger.error("getInteractionTypes() - ResultSetlUtil: " + se.getMessage()); //$NON-NLS-1$
			}

		}
		return arrayList;
	}

	public void closeDbConnection() {
		String methodAndParams = "closeDbConnection";
		try {

			ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.getUrl());

		} catch (Exception se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage()); //$NON-NLS-1$
			}

		}
	}

	public ArrayList<String> getDatasetNames() throws ConnectException,
			SocketTimeoutException, IOException {
		ArrayList<String> arrayList = new ArrayList<String>();

		ResultSetlUtil rs = null;
		String datasetName = null;

		try {

			String methodAndParams = "getDatasetNames";
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.getUrl());

			while (rs.next()) {

				datasetName = rs.getString("name").trim();

				arrayList.add(datasetName);
			}
			rs.close();

		} catch (ConnectException ce) {
			if (logger.isErrorEnabled()) {
				logger.error(ce.getMessage());
			}
			throw new ConnectException(ce.getMessage());
		} catch (SocketTimeoutException se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage());
			}
			throw new SocketTimeoutException(se.getMessage());
		} catch (IOException ie) {
			if (logger.isErrorEnabled()) {
				logger.error(ie.getMessage());
			}
			throw new IOException(ie.getMessage());

		} catch (Exception se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage()); //$NON-NLS-1$
			}

		}
		return arrayList;
	}

	public String getInteractomeDescription(String interactomeName)
			throws ConnectException, SocketTimeoutException, IOException {

		ResultSetlUtil rs = null;
		String interactomeDesc = null;

		try {

			String methodAndParams = "getInteractomeDescription"
					+ Constants.DEL + interactomeName;
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.getUrl());
			while (rs.next()) {
				interactomeDesc = rs.getString("description").trim();
				break;
			}
			rs.close();

		} catch (ConnectException ce) {
			if (logger.isErrorEnabled()) {
				logger.error(ce.getMessage());
			}
			throw new ConnectException(ce.getMessage());
		} catch (SocketTimeoutException se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage());
			}
			throw new SocketTimeoutException(se.getMessage());
		} catch (IOException ie) {
			if (logger.isErrorEnabled()) {
				logger.error(ie.getMessage());
			}
			throw new IOException(ie.getMessage());

		} catch (Exception se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage()); //$NON-NLS-1$
			}

		}
		return interactomeDesc;
	}

	public ArrayList<String> getInteractomeNames() throws ConnectException,
			SocketTimeoutException, IOException {
		ArrayList<String> arrayList = new ArrayList<String>();

		ResultSetlUtil rs = null;
		String datasetName = null;
		int interactionCount = 0;
		try {

			String methodAndParams = "getInteractomeNames";
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.getUrl());

			while (rs.next()) {

				datasetName = rs.getString("name").trim();
				interactionCount = (int) rs.getDouble("interaction_count");
				arrayList.add(datasetName + " (" + interactionCount
						+ " interactions)");
			}
			rs.close();

		} catch (ConnectException ce) {
			if (logger.isErrorEnabled()) {
				logger.error(ce.getMessage());
			}
			throw new ConnectException(ce.getMessage());
		} catch (SocketTimeoutException se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage());
			}
			throw new SocketTimeoutException(se.getMessage());
		} catch (IOException ie) {
			if (logger.isErrorEnabled()) {
				logger.error(ie.getMessage());
			}
			throw new IOException(ie.getMessage());

		} catch (Exception se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage()); //$NON-NLS-1$
			}

		}
		return arrayList;
	}

	public ArrayList<String> getDatasetAndInteractioCount()
			throws ConnectException, SocketTimeoutException, IOException {
		ArrayList<String> arrayList = new ArrayList<String>();

		ResultSetlUtil rs = null;
		String datasetName = null;
		int interactionCount = 0;
		try {

			String methodAndParams = "getDatasetNames";
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.getUrl());

			while (rs.next()) {

				datasetName = rs.getString("name").trim();
				interactionCount = (int) rs.getDouble("interaction_count");
				arrayList.add(datasetName + " (" + interactionCount
						+ " interactions)");
			}
			rs.close();

		} catch (ConnectException ce) {
			if (logger.isErrorEnabled()) {
				logger.error(ce.getMessage());
			}
			throw new ConnectException(ce.getMessage());
		} catch (SocketTimeoutException se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage());
			}
			throw new SocketTimeoutException(se.getMessage());
		} catch (IOException ie) {
			if (logger.isErrorEnabled()) {
				logger.error(ie.getMessage());
			}
			throw new IOException(ie.getMessage());

		} catch (Exception se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage()); //$NON-NLS-1$
			}

		}
		return arrayList;
	}

	public List<VersionDescriptor> getVersionDescriptor(String interactomeName)
			throws ConnectException, SocketTimeoutException, IOException {
		List<VersionDescriptor> arrayList = new ArrayList<VersionDescriptor>();

		ResultSetlUtil rs = null;
		String version = null;
		String versionDesc = null;
		String value = null;
		boolean needAuthentication = false;

		try {

			String methodAndParams = "getVersionDescriptor" + Constants.DEL
					+ interactomeName;
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.getUrl());
			while (rs.next()) {
				version = rs.getString("version").trim();
				if (version.equalsIgnoreCase("DEL"))
					continue;
				value = rs.getString("authentication_yn").trim();
				if (value.equalsIgnoreCase("Y"))
					needAuthentication = true;
				else
					needAuthentication = false;
				versionDesc = rs.getString("description").trim();
				VersionDescriptor vd = new VersionDescriptor(version,
						needAuthentication, versionDesc);
				arrayList.add(vd);
			}
			rs.close();

		} catch (ConnectException ce) {
			if (logger.isErrorEnabled()) {
				logger.error(ce.getMessage());
			}
			throw new ConnectException(ce.getMessage());
		} catch (SocketTimeoutException se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage());
			}
			throw new SocketTimeoutException(se.getMessage());
		} catch (IOException ie) {
			if (logger.isErrorEnabled()) {
				logger.error(ie.getMessage());
			}
			throw new IOException(ie.getMessage());

		} catch (Exception se) {
			if (logger.isErrorEnabled()) {
				logger.error(se.getMessage()); //$NON-NLS-1$
			}

		}
		return arrayList;
	}

	public static boolean isValidUrl(String urlStr) {

		ResultSetlUtil rs = null;

		try {

			String methodAndParams = "getDatasetNames";
			rs = ResultSetlUtil.executeQuery(methodAndParams, urlStr);

			rs.close();

			return true;
		} catch (java.net.ConnectException ce) {
			if (logger.isErrorEnabled()) {
				logger.error(ce.getMessage());
			}
			return false;
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error(e.getMessage());
			}
			return false;
		}

	}

}
