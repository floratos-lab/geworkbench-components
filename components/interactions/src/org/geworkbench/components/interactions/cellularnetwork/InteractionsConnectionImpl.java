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
import org.geworkbench.util.network.InteractionParticipant;

/**
 * The class to query CNKB database via servlet.
 * 
 * @author zji
 * @version $Id$
 * 
 */
public class InteractionsConnectionImpl {

	private static final Log logger = LogFactory
			.getLog(InteractionsConnectionImpl.class);

	private static final String ENTREZ_GENE = "Entrez Gene";
 
	public List<InteractionDetail> getInteractionsByEntrezIdOrGeneSymbol(
			DSGeneMarker marker, String context, String version)
			throws UnAuthenticatedException, ConnectException,
			SocketTimeoutException, IOException {
		return getInteractionsByEntrezIdOrGeneSymbol(marker, context,
				version, null);
	}

	/**
	 * This is similar to getInteractionsByEntrezIdOrGeneSymbol_1 and currently
	 * used. The difference is that this version retains all the 'edges' of the
	 * interactions that include the queried marker even if they are not
	 * connected directly.
	 */
	public List<InteractionDetail> getInteractionsByEntrezIdOrGeneSymbol(
			DSGeneMarker marker, String context, String version, String userInfo)
			throws UnAuthenticatedException, ConnectException,
			SocketTimeoutException, IOException {

		List<InteractionDetail> arrayList = new ArrayList<InteractionDetail>();

		String marker_msid = new Integer(marker.getGeneId()).toString();
		String marker_geneName = marker.getGeneName();

		String methodAndParams = "getInteractionsByEntrezIdOrGeneSymbol"
				+ Constants.DEL + marker_msid + Constants.DEL + marker_geneName
				+ Constants.DEL + context + Constants.DEL + version;

		ResultSetlUtil rs = ResultSetlUtil.executeQueryWithUserInfo(
				methodAndParams, ResultSetlUtil.getUrl(), userInfo);

		String previousInteractionId = null;
		boolean firstHitOnQueryGene = true;
		InteractionDetail interactionDetail = null;
		while (rs.next()) {
			try {
				String msid2 = rs.getString("primary_accession");
				String geneName2 = rs.getString("gene_symbol");

				String db2_xref = rs.getString("accession_db");
				String interactionType = rs.getString("interaction_type")
						.trim();
				String interactionId = rs.getString("interaction_id");
				Short evidenceId = 0;
				if (rs.getString("evidence_id") != null
						&& !rs.getString("evidence_id").trim().equals("null")) {
					evidenceId = new Short(rs.getString("evidence_id"));
				}
				if (!db2_xref.equalsIgnoreCase(ENTREZ_GENE)
						&& marker_geneName.equals(geneName2)) {
					msid2 = marker_msid;
				}

				if (previousInteractionId == null
						|| !previousInteractionId.equals(interactionId)) {
					if (interactionDetail != null) {
						arrayList.add(interactionDetail);
						interactionDetail = null;
					}
					previousInteractionId = interactionId;
					firstHitOnQueryGene = true;

				}
				if ((db2_xref.equals(ENTREZ_GENE) && marker_msid.equals(msid2))
						|| (geneName2 != null && marker_geneName
								.equalsIgnoreCase(geneName2))) {
					if (firstHitOnQueryGene == true) {
						firstHitOnQueryGene = false;
						continue;
					}
				}

				if (interactionDetail == null) {
					interactionDetail = new InteractionDetail(
							new InteractionParticipant(msid2, geneName2, db2_xref),
							interactionType, evidenceId);
					double confidenceValue = 1.0;
					try {
						confidenceValue = rs.getDouble("confidence_value");
					} catch (NumberFormatException nfe) {
						logger.info("there is no confidence value for this row. Default it to 1.");
					}
					short confidenceType = 0;
					try {
						confidenceType = new Short(rs.getString(
								"confidence_type").trim());
					} catch (NumberFormatException nfe) {
						logger.info("there is no confidence value for this row. Default it to 0.");
					}
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
				} else {
					interactionDetail
							.addParticipant(new InteractionParticipant(msid2,
									geneName2, db2_xref));
				}

			} catch (NullPointerException npe) {
				logger.error("db row is dropped because a NullPointerException");

			} catch (NumberFormatException nfe) {
				logger.error("db row is dropped because a NumberFormatExceptio");
			}
		}

		if (interactionDetail != null) {
			arrayList.add(interactionDetail);
			interactionDetail = null;
		}
		rs.close();

		return arrayList;
	}

	public List<String> getInteractionsSifFormat(String context,
			String version, String interactionType, String presentBy)
			throws UnAuthenticatedException, ConnectException,
			SocketTimeoutException, IOException {

		List<String> arrayList = new ArrayList<String>();

		String methodAndParams = "getInteractionsSifFormat" + Constants.DEL
				+ context + Constants.DEL + version + Constants.DEL
				+ interactionType + Constants.DEL + presentBy;
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
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

		return arrayList;
	}

	public List<String> getInteractionsAdjFormat(String context,
			String version, String interactionType, String presentBy)
			throws UnAuthenticatedException, ConnectException,
			SocketTimeoutException, IOException {

		List<String> arrayList = new ArrayList<String>();

		String methodAndParams = "getInteractionsAdjFormat" + Constants.DEL
				+ context + Constants.DEL + version + Constants.DEL
				+ interactionType + Constants.DEL + presentBy;
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
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

		return arrayList;
	}

	public HashMap<String, String> getInteractionTypeMap()
			throws ConnectException, SocketTimeoutException, IOException,
			UnAuthenticatedException {
		HashMap<String, String> map = new HashMap<String, String>();

		String methodAndParams = "getInteractionTypes";
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());

		while (rs.next()) {

			String interactionType = rs.getString("interaction_type").trim();
			String short_name = rs.getString("short_name").trim();

			map.put(interactionType, short_name);
			map.put(short_name, interactionType);
		}
		rs.close();

		return map;
	}

	public HashMap<String, String> getInteractionEvidenceMap()
			throws ConnectException, SocketTimeoutException, IOException,
			UnAuthenticatedException {
		HashMap<String, String> map = new HashMap<String, String>();

		String methodAndParams = "getInteractionEvidences";
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());

		while (rs.next()) {

			String evidenceDesc = rs.getString("description");
			String evidenceId = rs.getString("id");

			map.put(evidenceId, evidenceDesc);
			map.put(evidenceDesc, evidenceId);
		}
		rs.close();

		return map;
	}

	public HashMap<String, String> getConfidenceTypeMap()
			throws ConnectException, SocketTimeoutException, IOException,
			UnAuthenticatedException {
		HashMap<String, String> map = new HashMap<String, String>();

		String methodAndParams = "getConfidenceTypes";
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());

		while (rs.next()) {

			String confidenceType = rs.getString("name").trim();
			String id = rs.getString("id").trim();

			map.put(confidenceType, id);
			map.put(id, confidenceType);
		}
		rs.close();

		return map;
	}

	public List<String> getInteractionTypes() throws ConnectException,
			SocketTimeoutException, IOException, UnAuthenticatedException {
		List<String> arrayList = new ArrayList<String>();

		String methodAndParams = "getInteractionTypes";
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());

		while (rs.next()) {

			String interactionType = rs.getString("interaction_type").trim();

			arrayList.add(interactionType);
		}
		rs.close();

		return arrayList;
	}

	public List<String> getInteractionTypesByInteractomeVersion(String context,
			String version) throws ConnectException, SocketTimeoutException,
			IOException, UnAuthenticatedException {
		List<String> arrayList = new ArrayList<String>();

		String methodAndParams = "getInteractionTypesByInteractomeVersion"
				+ Constants.DEL + context + Constants.DEL + version;
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());

		while (rs.next()) {

			String interactionType = rs.getString("interaction_type").trim();

			arrayList.add(interactionType);
		}
		rs.close();

		return arrayList;
	}

	public String getInteractomeDescription(String interactomeName)
			throws ConnectException, SocketTimeoutException, IOException,
			UnAuthenticatedException {

		String interactomeDesc = null;

		String methodAndParams = "getInteractomeDescription" + Constants.DEL
				+ interactomeName;
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());
		while (rs.next()) {
			interactomeDesc = rs.getString("description").trim();
			break;
		}
		rs.close();

		return interactomeDesc;
	}

	public ArrayList<String> getDatasetAndInteractioCount()
			throws ConnectException, SocketTimeoutException, IOException,
			UnAuthenticatedException {
		ArrayList<String> arrayList = new ArrayList<String>();

		String datasetName = null;
		int interactionCount = 0;

		String methodAndParams = "getDatasetNames";
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());

		while (rs.next()) {

			datasetName = rs.getString("name").trim();
			interactionCount = (int) rs.getDouble("interaction_count");
			arrayList.add(datasetName + " (" + interactionCount
					+ " interactions)");
		}
		rs.close();

		return arrayList;
	}

	public List<VersionDescriptor> getVersionDescriptor(String interactomeName)
			throws ConnectException, SocketTimeoutException, IOException,
			UnAuthenticatedException {
		List<VersionDescriptor> arrayList = new ArrayList<VersionDescriptor>();

		String methodAndParams = "getVersionDescriptor" + Constants.DEL
				+ interactomeName;
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());
		while (rs.next()) {
			String version = rs.getString("version").trim();
			if (version.equalsIgnoreCase("DEL"))
				continue;
			String value = rs.getString("authentication_yn").trim();
			boolean needAuthentication = false;
			if (value.equalsIgnoreCase("Y")) {
				needAuthentication = true;
			}
			String versionDesc = rs.getString("description").trim();
			VersionDescriptor vd = new VersionDescriptor(version,
					needAuthentication, versionDesc);
			arrayList.add(vd);
		}
		rs.close();

		return arrayList;
	}
	
	
	boolean isExportable(String context,
			String version)
			throws ConnectException, SocketTimeoutException, IOException,
			UnAuthenticatedException {
		boolean isExportable = false;

		String methodAndParams = "getExportFlag" + Constants.DEL
				+ context +  Constants.DEL + version;
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());
		while (rs.next()) {
			 
			String value = rs.getString("export_yn").trim();			 
			if (value.equalsIgnoreCase("Y")) {
				isExportable = true;
				break;
			}
			 
		}
		rs.close();

		return isExportable;
	}

	/**
	 * Test the connection. The actual query result is ignored.
	 */
	public static boolean isValidUrl(String urlStr) {

		try {
			ResultSetlUtil rs = ResultSetlUtil.executeQuery("getDatasetNames",
					urlStr);
			rs.close();
			return true;
		} catch (java.net.ConnectException ce) {
			logger.error(ce.getMessage());
			return false;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}

	}

}
