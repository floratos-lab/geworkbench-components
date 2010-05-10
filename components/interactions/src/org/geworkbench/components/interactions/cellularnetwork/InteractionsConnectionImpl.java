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

	private static final String UNSUPPORTED_OPERATION_MESSAGE = "unsupported operation";
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
					ResultSetlUtil.MYSQL,
					ResultSetlUtil.INTERACTIONS_SERVLET_URL);

			while (rs.next()) {				
			try
			{
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
						geneName2, isGene1EntrezId.booleanValue(), isGene2EntrezId.booleanValue(), confidenceValue, interactionType);
				arrayList.add(interactionDetail);
			}catch(NullPointerException npe)
			{
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
					ResultSetlUtil.MYSQL,
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
				logger.error("getInteractionTypes() - ResultSetlUtil: " + se.getMessage() ); //$NON-NLS-1$
			}
			 
		}
		return arrayList;
	}
	
	public void closeDbConnection()   {
		String methodAndParams = "closeDbConnection";
		try {

		     ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.MYSQL,
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
					ResultSetlUtil.MYSQL,
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
					ResultSetlUtil.MYSQL,
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
			rs = ResultSetlUtil.executeQuery(methodAndParams,
					ResultSetlUtil.MYSQL, urlStr);

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

	public void insert() throws java.rmi.RemoteException {

		/*
		 * try { conn = DriverManager.getConnection(JDBC_ORACLE_THIN,
		 * USER_INTERACTION_RO, PSWD_ORACKE_LINKT0CELLNET); statement =
		 * conn.createStatement(); statement.executeUpdate("INSERT INTO
		 * PAIRWISE_INTERACTION" + "(ms_id1, ms_id2, confidence, is_modulated,
		 * interaction_type, control_type, direction, is_reversible, source) " +
		 * "values \"" + msid1.toString() + "," + msid2.toString() + "," +
		 * confidenceValue + "," + isModulated + "," + interactionType + "," +
		 * controlType + "," + direction + "," + isReversible + "," + source +
		 * "\""); conn.commit(); conn.close(); } catch (SQLException se) {
		 * se.printStackTrace(); }
		 */
		throw new RuntimeException(UNSUPPORTED_OPERATION_MESSAGE);

	}

	public void retrieve() throws java.rmi.RemoteException {

		/*
		 * try { conn = DriverManager.getConnection(JDBC_ORACLE_THIN,
		 * USER_INTERACTION_RO, PSWD_ORACKE_LINKT0CELLNET); statement =
		 * conn.createStatement(); ResultSet rs = statement.executeQuery("SELECT *
		 * FROM PAIRWISE_INTERACTION where ms_id1=" + msid1.toString()); if
		 * (rs.getRow() > 0) { rs.next(); msid2 = rs.getBigDecimal("ms_id2");
		 * confidenceValue = rs.getDouble("confidence"); isModulated =
		 * rs.getString("is_modulated"); interactionType =
		 * rs.getString("interaction_type"); controlType =
		 * rs.getString("control_type"); direction = rs.getString("direction");
		 * isReversible = rs.getString("is_reversible"); source =
		 * rs.getString("source"); } conn.close(); } catch (SQLException se) {
		 * se.printStackTrace(); }
		 */
		throw new RuntimeException(UNSUPPORTED_OPERATION_MESSAGE);

	}

	public String getCHROMOSOME() throws java.rmi.RemoteException {
		/*
		 * try { conn = DriverManager.getConnection(JDBC_ORACLE_THIN,
		 * USER_INTERACTION_RO, PSWD_ORACKE_LINKT0CELLNET); statement =
		 * conn.createStatement(); ResultSet rs = statement.executeQuery("select
		 * CHROMOSOME from (select CHROMOSOME, rownum rn from master_gene where
		 * rownum<" + (chromosomeId + 1) + ") where rn=" + chromosomeId);
		 * rs.next(); String chr = rs.getString("CHROMOSOME"); rs.close();
		 * statement.close(); conn.close(); return chr; } catch (SQLException
		 * se) { se.printStackTrace(); } return "";
		 * 
		 */
		throw new RuntimeException(UNSUPPORTED_OPERATION_MESSAGE);

	}

	private int chromosomeId = 0;

	public void setCHROMOSOME(String in0) throws java.rmi.RemoteException {
		chromosomeId = Integer.parseInt(in0);
	}

	public BigDecimal getGENECOUNT() throws java.rmi.RemoteException {

		/*
		 * try { conn = DriverManager.getConnection(JDBC_ORACLE_THIN,
		 * USER_INTERACTION_RO, PSWD_ORACKE_LINKT0CELLNET); statement =
		 * conn.createStatement(); ResultSet rs = statement.executeQuery("SELECT
		 * count(*) FROM MASTER_GENE"); rs.next(); System.out.println("count: " +
		 * rs.getObject(1)); BigDecimal count = (BigDecimal) rs.getObject(1);
		 * rs.close(); statement.close(); conn.close(); return count; } catch
		 * (SQLException se) { se.printStackTrace(); }
		 * System.out.println("ResultSet.getRow() == 0, impl 191"); return new
		 * BigDecimal(0);
		 */
		throw new RuntimeException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	private int egIndex = 0;

	public BigDecimal getENTREZID() throws java.rmi.RemoteException {

		/*
		 * try { conn = DriverManager.getConnection(JDBC_ORACLE_THIN,
		 * USER_INTERACTION_RO, PSWD_ORACKE_LINKT0CELLNET); statement =
		 * conn.createStatement(); ResultSet rs = statement.executeQuery("select
		 * entrez_id from (select entrez_id, rownum rn from master_gene where
		 * rownum<" + (egIndex + 1) + ") where rn=" + egIndex); rs.next();
		 * BigDecimal ei = rs.getBigDecimal("ENTREZ_ID"); rs.close();
		 * statement.close(); conn.close(); return ei; } catch (SQLException se) {
		 * se.printStackTrace(); } return new BigDecimal(0);
		 */
		throw new RuntimeException(UNSUPPORTED_OPERATION_MESSAGE);

	}

	public void setENTREZID(BigDecimal in0) throws java.rmi.RemoteException {
		egIndex = in0.intValue() + 1;
	}

	private int taxonIndex = 0;

	public BigDecimal getTAXONID() throws java.rmi.RemoteException {

		/*
		 * try { conn = DriverManager.getConnection(JDBC_ORACLE_THIN,
		 * USER_INTERACTION_RO, PSWD_ORACKE_LINKT0CELLNET); statement =
		 * conn.createStatement(); ResultSet rs = statement.executeQuery("select
		 * taxon_id from (select taxon_id, rownum rn from master_gene where
		 * rownum<" + (taxonIndex + 1) + ") where rn=" + taxonIndex);
		 * rs.next(); BigDecimal ti = rs.getBigDecimal("TAXON_ID"); rs.close();
		 * statement.close(); conn.close(); return ti; } catch (SQLException se) {
		 * se.printStackTrace(); } return new BigDecimal(0);
		 */
		throw new RuntimeException(UNSUPPORTED_OPERATION_MESSAGE);

	}

	public void setTAXONID(BigDecimal in0) throws java.rmi.RemoteException {
		taxonIndex = in0.intValue() + 1;
	}

	private int geneTypeIndex = 0;

	public String getGENETYPE() throws java.rmi.RemoteException {

		/*
		 * try { conn = DriverManager.getConnection(JDBC_ORACLE_THIN,
		 * USER_INTERACTION_RO, PSWD_ORACKE_LINKT0CELLNET); statement =
		 * conn.createStatement(); ResultSet rs = statement.executeQuery("select
		 * GENE_TYPE from (select GENE_TYPE, rownum rn from master_gene where
		 * rownum<" + (geneTypeIndex + 1) + ") where rn=" + geneTypeIndex);
		 * rs.next(); String gt = rs.getString("GENE_TYPE"); rs.close();
		 * statement.close(); conn.close(); return gt; } catch (SQLException se) {
		 * se.printStackTrace(); } return "";
		 */
		throw new RuntimeException(UNSUPPORTED_OPERATION_MESSAGE);

	}

	public void setGENETYPE(String in0) throws java.rmi.RemoteException {
		geneTypeIndex = Integer.parseInt(in0) + 1;
	}

	private int geneSymbolIndex = 0;

	public String getGENESYMBOL() throws java.rmi.RemoteException {

		/*
		 * try { conn = DriverManager.getConnection(JDBC_ORACLE_THIN,
		 * USER_INTERACTION_RO, PSWD_ORACKE_LINKT0CELLNET); statement =
		 * conn.createStatement(); ResultSet rs = statement.executeQuery("select
		 * GENE_SYMBOL from (select GENE_SYMBOL, rownum rn from master_gene
		 * where rownum<" + (geneSymbolIndex + 1) + ") where rn=" +
		 * geneSymbolIndex); rs.next(); String gs = rs.getString("GENE_SYMBOL");
		 * rs.close(); statement.close(); conn.close(); return gs; } catch
		 * (SQLException se) { se.printStackTrace(); } return "";
		 */
		throw new RuntimeException(UNSUPPORTED_OPERATION_MESSAGE);

	}

	public void setGENESYMBOL(String in0) throws java.rmi.RemoteException {
		geneSymbolIndex = Integer.parseInt(in0) + 1;
	}

	public Object[] getENTREZTOGO() throws java.rmi.RemoteException {
		return null;
	}

	private int locusTagIndex = 0;

	public String getLOCUSTAG() throws java.rmi.RemoteException {

		/*
		 * try { conn = DriverManager.getConnection(JDBC_ORACLE_THIN,
		 * USER_INTERACTION_RO, PSWD_ORACKE_LINKT0CELLNET); statement =
		 * conn.createStatement(); ResultSet rs = statement.executeQuery("select
		 * LOCUS_TAG from (select LOCUS_TAG, rownum rn from master_gene where
		 * rownum<" + (locusTagIndex + 1) + ") where rn=" + locusTagIndex);
		 * rs.next(); String lt = rs.getString("LOCUS_TAG"); rs.close();
		 * statement.close(); conn.close(); return lt; } catch (SQLException se) {
		 * se.printStackTrace(); } return "";
		 */
		throw new RuntimeException(UNSUPPORTED_OPERATION_MESSAGE);

	}

	public void setLOCUSTAG(String in0) throws java.rmi.RemoteException {
		locusTagIndex = Integer.parseInt(in0) + 1;
	}

	private int descIndex = 0;

	public String getDESCRIPTION() throws java.rmi.RemoteException {

		/*
		 * try { conn = DriverManager.getConnection(JDBC_ORACLE_THIN,
		 * USER_INTERACTION_RO, PSWD_ORACKE_LINKT0CELLNET); statement =
		 * conn.createStatement(); ResultSet rs = statement.executeQuery("select
		 * DESCRIPTION from (select DESCRIPTION, rownum rn from master_gene
		 * where rownum<" + (descIndex + 1) + ") where rn=" + descIndex);
		 * rs.next(); String desc = rs.getString("DESCRIPTION"); rs.close();
		 * statement.close(); conn.close(); return desc; } catch (SQLException
		 * se) { se.printStackTrace(); } return "";
		 */
		throw new RuntimeException(UNSUPPORTED_OPERATION_MESSAGE);

	}

	public void setDESCRIPTION(String in0) throws java.rmi.RemoteException {
		descIndex = Integer.parseInt(in0) + 1;
	}

	public Object[] getGENEROW(BigDecimal in0) throws RemoteException {

		/*
		 * int index = in0.intValue(); try { conn =
		 * DriverManager.getConnection(JDBC_ORACLE_THIN, USER_INTERACTION_RO,
		 * PSWD_ORACKE_LINKT0CELLNET); statement = conn.createStatement();
		 * ResultSet rs = statement.executeQuery("select ENTREZ_ID, TAXON_ID,
		 * GENE_TYPE, CHROMOSOME, GENE_SYMBOL, LOCUS_TAG, DESCRIPTION " + "from
		 * (select ENTREZ_ID, TAXON_ID, GENE_TYPE, CHROMOSOME, GENE_SYMBOL,
		 * LOCUS_TAG, DESCRIPTION, rownum rn from master_gene " + "where rownum<" +
		 * (index + 1) + ") where rn=" + index); // if (rs.getRow() == 0) //
		 * return new Object[]{}; rs.next(); BigDecimal ei =
		 * rs.getBigDecimal("ENTREZ_ID"); BigDecimal ti =
		 * rs.getBigDecimal("TAXON_ID"); String gt = rs.getString("GENE_TYPE");
		 * String chr = rs.getString("CHROMOSOME"); String gs =
		 * rs.getString("GENE_SYMBOL"); String lt = rs.getString("LOCUS_TAG");
		 * String desc = rs.getString("DESCRIPTION"); rs.close();
		 * statement.close(); conn.close(); return new Object[]{ei, ti, gs, lt,
		 * chr, desc, gt}; } catch (SQLException se) { se.printStackTrace(); }
		 * return new Object[]{};
		 */
		throw new RuntimeException(UNSUPPORTED_OPERATION_MESSAGE);

	}

	public BigDecimal getINTERACTIONCOUNT(BigDecimal in0, String in1)
			throws RemoteException {

		/*
		 * try { conn = DriverManager.getConnection(JDBC_ORACLE_THIN,
		 * USER_INTERACTION_RO, PSWD_ORACKE_LINKT0CELLNET); statement =
		 * conn.createStatement(); ResultSet rs = statement.executeQuery("SELECT
		 * count(unique MS_ID2) FROM PAIRWISE_INTERACTION where MS_ID1=" +
		 * in0.intValue() + " and (INTERACTION_TYPE=\'" + in1.toLowerCase() +
		 * "\' or INTERACTION_TYPE=\'" + in1.toUpperCase() + "\')"); rs.next();
		 * BigDecimal count = (BigDecimal) rs.getObject(1); rs.close(); rs =
		 * statement.executeQuery("SELECT count(unique MS_ID1) FROM
		 * PAIRWISE_INTERACTION where MS_ID2=" + in0.intValue() + " and
		 * (INTERACTION_TYPE=\'" + in1.toLowerCase() + "\' or
		 * INTERACTION_TYPE=\'" + in1.toUpperCase() + "\')"); rs.next(); count =
		 * new BigDecimal(count.intValue() + ((BigDecimal)
		 * rs.getObject(1)).intValue()); rs.close(); statement.close();
		 * conn.close(); return count; } catch (SQLException se) {
		 * se.printStackTrace(); } return new BigDecimal(0);
		 */
		throw new RuntimeException(UNSUPPORTED_OPERATION_MESSAGE);

	}

	public Object[] getFIRSTNEIGHBORS(BigDecimal in0, String in1)
			throws RemoteException {

		/*
		 * Vector<BigDecimal> neighbors = new Vector<BigDecimal>(); try { conn =
		 * DriverManager.getConnection(JDBC_ORACLE_THIN, USER_INTERACTION_RO,
		 * PSWD_ORACKE_LINKT0CELLNET); statement = conn.createStatement();
		 * ResultSet rs = statement.executeQuery("SELECT MS_ID1 FROM
		 * PAIRWISE_INTERACTION where MS_ID2=" + in0.intValue() + " and
		 * (INTERACTION_TYPE=\'" + in1.toLowerCase() + "\' or
		 * INTERACTION_TYPE=\'" + in1.toUpperCase() + "\')"); while (rs.next()) {
		 * if (!neighbors.contains((BigDecimal) rs.getObject(1)))
		 * neighbors.add((BigDecimal) rs.getObject(1)); } rs.close(); rs =
		 * statement.executeQuery("SELECT MS_ID2 FROM PAIRWISE_INTERACTION where
		 * MS_ID1=" + in0.intValue() + " and (INTERACTION_TYPE=\'" +
		 * in1.toLowerCase() + "\' or INTERACTION_TYPE=\'" + in1.toUpperCase() +
		 * "\')"); while (rs.next()) { neighbors.add((BigDecimal)
		 * rs.getObject(1)); } rs.close(); statement.close(); conn.close(); }
		 * catch (SQLException se) { se.printStackTrace(); } return
		 * neighbors.toArray();
		 */
		throw new RuntimeException(UNSUPPORTED_OPERATION_MESSAGE);

	}
}
