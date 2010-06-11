package org.geworkbench.components.interactions.cellularnetwork;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.ArrayList;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
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

	public List<InteractionDetail> getPairWiseInteraction(DSGeneMarker marker,
			String context, String version) throws UnAuthenticatedException,
			ConnectException, SocketTimeoutException, IOException {
		BigDecimal id = new BigDecimal(marker.getGeneId());
		return this.getPairWiseInteraction(id, context, version);
	}

	public List<InteractionDetail> getPairWiseInteraction(BigDecimal id1,
			String context, String version) throws UnAuthenticatedException,
			ConnectException, SocketTimeoutException, IOException {
		String interactionType = null;
		String msid2 = null;
		String msid1 = null;
		String geneName1 = null;
		String geneName2 = null;
		String db1_xref = null;
		String db2_xref = null;
		Boolean isGene1EntrezId = null;
		Boolean isGene2EntrezId = null;

		double confidenceValue = 0d;

		List<InteractionDetail> arrayList = new ArrayList<InteractionDetail>();

		ResultSetlUtil rs = null;

		try {

			String methodAndParams = "getPairWiseInteraction"
					+ ResultSetlUtil.DEL + id1.toString() + ResultSetlUtil.DEL
					+ context + ResultSetlUtil.DEL + version;
			// String aSQL = "SELECT * FROM pairwise_interaction where ms_id1="
			// + id1.toString() + " or ms_id2=" + id1.toString();
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.INTERACTIONS_SERVLET_URL);

			while (rs.next()) {
				try {
					msid1 = rs.getString("ms_id1");
					msid2 = rs.getString("ms_id2");
					geneName1 = rs.getString("gene1");
					geneName2 = rs.getString("gene2");
					db1_xref = rs.getString("db1_xref");
					db2_xref = rs.getString("db2_xref");
					confidenceValue = rs.getDouble("confidence_value");

					interactionType = rs.getString("interaction_type").trim();

					if (db1_xref.trim().equalsIgnoreCase(ENTREZ_GENE))
						isGene1EntrezId = true;
					else
						isGene1EntrezId = false;

					if (db2_xref.trim().equalsIgnoreCase(ENTREZ_GENE))
						isGene2EntrezId = true;
					else
						isGene2EntrezId = false;

					InteractionDetail interactionDetail = new InteractionDetail(
							msid1.toString(), msid2.toString(), geneName1,
							geneName2, isGene1EntrezId.booleanValue(),
							isGene2EntrezId.booleanValue(), confidenceValue,
							interactionType);
					arrayList.add(interactionDetail);
				} catch (NullPointerException npe) {
					if (logger.isErrorEnabled()) {
						logger
								.error("db row is dropped because a NullPointerException");
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
				logger
						.error("getPairWiseInteraction(BigDecimal) - ResultSetlUtil rs=" + rs); //$NON-NLS-1$
			}
			se.printStackTrace();
		}
		return arrayList;
	}

	public List<String> getInteractionTypes() throws ConnectException,
			SocketTimeoutException, IOException {
		List<String> arrayList = new ArrayList<String>();

		ResultSetlUtil rs = null;
		String interactionType = null;

		try {

			String methodAndParams = "getInteractionTypes";
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.INTERACTIONS_SERVLET_URL);

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
				logger
						.error("getInteractionTypes() - ResultSetlUtil: " + se.getMessage()); //$NON-NLS-1$
			}

		}
		return arrayList;
	}

	public void closeDbConnection() {
		String methodAndParams = "closeDbConnection";
		try {

			ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.INTERACTIONS_SERVLET_URL);

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
					ResultSetlUtil.INTERACTIONS_SERVLET_URL);

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

	public ArrayList<String> getDatasetAndInteractioCount()
			throws ConnectException, SocketTimeoutException, IOException {
		ArrayList<String> arrayList = new ArrayList<String>();

		ResultSetlUtil rs = null;
		String datasetName = null;
        int interactionCount = 0;
		try {

			String methodAndParams = "getDatasetNames";
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.INTERACTIONS_SERVLET_URL);			
			
			while (rs.next()) {

				datasetName = rs.getString("name").trim();
				interactionCount = (int)rs.getDouble("interaction_count");
				arrayList.add(datasetName + " (" + interactionCount + " interactions)");
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

	public List<VersionDescriptor> getVersionDescriptor(String datasetName)
			throws ConnectException, SocketTimeoutException, IOException {
		List<VersionDescriptor> arrayList = new ArrayList<VersionDescriptor>();

		ResultSetlUtil rs = null;
		String version = null;
		String value = null;
		boolean needAuthentication = false;

		try {

			String methodAndParams = "getVersionDescriptor"
					+ ResultSetlUtil.DEL + datasetName;
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.INTERACTIONS_SERVLET_URL);
			while (rs.next()) {
				version = rs.getString("version").trim();
				if (version.equalsIgnoreCase("DEL"))
					continue;
				value = rs.getString("authentication_yn").trim();
				if (value.equalsIgnoreCase("Y"))
					needAuthentication = true;
				else
					needAuthentication = false;
				VersionDescriptor vd = new VersionDescriptor(version,
						needAuthentication);
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
