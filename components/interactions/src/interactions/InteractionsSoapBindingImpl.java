/**
 * InteractionsSoapBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package interactions;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class InteractionsSoapBindingImpl implements interactions.INTERACTIONS{
    
    private String interactionType = null;
    private BigDecimal msid2 = null;
    private String isReversible = null;
    private BigDecimal msid1 = null;
    private String source = null;
    private String controlType = null;
    private String direction = null;
    private double confidenceValue = 0d;
    private String isModulated = null;
    private BigDecimal id = null;
    
    private Connection conn = null;
    private Statement statement = null;
    
    static {
        try {
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (SQLException se){
            se.printStackTrace();
        }
    }
    
    public InteractionsSoapBindingImpl(){
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
        } catch (SQLException se){
            se.printStackTrace();
        }
    }
    
    public java.lang.String getINTERACTIONTYPE() throws java.rmi.RemoteException {
        return interactionType;
    }
    
    public void setINTERACTIONTYPE(java.lang.String in0) throws java.rmi.RemoteException {
        interactionType = in0;
    }
    
    public java.math.BigDecimal getMSID2() throws java.rmi.RemoteException {
        return msid2;
    }
    
    public void setMSID2(java.math.BigDecimal in0) throws java.rmi.RemoteException {
        msid2 = in0;
    }
    
    public java.lang.String getISREVERSIBLE() throws java.rmi.RemoteException {
        return isReversible;
    }
    
    public void setISREVERSIBLE(java.lang.String in0) throws java.rmi.RemoteException {
        isReversible = in0;
    }
    
    public java.math.BigDecimal getMSID1() throws java.rmi.RemoteException {
        return msid1;
    }
    
    public void setMSID1(java.math.BigDecimal in0) throws java.rmi.RemoteException {
        msid1 = in0;
    }
    
    public java.lang.String getSOURCE() throws java.rmi.RemoteException {
        return source;
    }
    
    public void setSOURCE(java.lang.String in0) throws java.rmi.RemoteException {
        source = in0;
    }
    
    public java.lang.String getCONTROLTYPE() throws java.rmi.RemoteException {
        return controlType;
    }
    
    public void setCONTROLTYPE(java.lang.String in0) throws java.rmi.RemoteException {
        controlType = in0;
    }
    
    public java.lang.String getDIRECTION() throws java.rmi.RemoteException {
        return direction;
    }
    
    public void setDIRECTION(java.lang.String in0) throws java.rmi.RemoteException {
        direction = in0;
    }
    
    public double getCONFIDENCEVALUE() throws java.rmi.RemoteException {
        return confidenceValue;
    }
    
    public void setCONFIDENCEVALUE(double in0) throws java.rmi.RemoteException {
        confidenceValue = in0;
    }
    
    public java.lang.String getISMODULATED() throws java.rmi.RemoteException {
        return isModulated;
    }
    
    public void setISMODULATED(java.lang.String in0) throws java.rmi.RemoteException {
        isModulated = in0;
    }
    
    public java.math.BigDecimal getID() throws java.rmi.RemoteException {
        return id;
    }
    
    public void setID(java.math.BigDecimal in0) throws java.rmi.RemoteException {
        id = in0;
    }
    
    public void insert() throws java.rmi.RemoteException{
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            statement.executeUpdate("INSERT INTO PAIRWISE_INTERACTION" +
                    "(ms_id1, ms_id2, confidence, is_modulated, interaction_type, control_type, direction, is_reversible, source) " +
                    "values \"" + msid1.toString() + "," + msid2.toString() + "," + confidenceValue + "," + isModulated + "," +
                    interactionType + "," + controlType + "," + direction + "," + isReversible + "," + source + "\"");
            conn.commit();
            conn.close();
        } catch (SQLException se){
            se.printStackTrace();
        }
    }
    
    public void retrieve() throws java.rmi.RemoteException{
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM PAIRWISE_INTERACTION where ms_id1=" + msid1.toString());
            if (rs.getRow() > 0){
                rs.next();
                msid2 = rs.getBigDecimal("ms_id2");
                confidenceValue = rs.getDouble("confidence");
                isModulated = rs.getString("is_modulated");
                interactionType = rs.getString("interaction_type");
                controlType = rs.getString("control_type");
                direction = rs.getString("direction");
                isReversible = rs.getString("is_reversible");
                source = rs.getString("source");
            }
            conn.close();
        } catch (SQLException se){
            se.printStackTrace();
        }
    }
    
    public java.lang.String getCHROMOSOME() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select CHROMOSOME from (select CHROMOSOME, rownum rn from master_gene where rownum<" + (chromosomeId + 1) +") where rn=" + chromosomeId);
            rs.next();
            String chr = rs.getString("CHROMOSOME");
            rs.close();
            statement.close();
            conn.close();
            return chr;
        } catch (SQLException se){
            se.printStackTrace();
        }
        return "";
    }
    
    private int chromosomeId = 0;
    public void setCHROMOSOME(java.lang.String in0) throws java.rmi.RemoteException {
        chromosomeId = Integer.parseInt(in0);
    }
    
    public java.math.BigDecimal getGENECOUNT() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT count(*) FROM MASTER_GENE");
            rs.next();
            System.out.println("count: " + rs.getObject(1));
            BigDecimal count = (BigDecimal)rs.getObject(1);
            rs.close();
            statement.close();
            conn.close();
            return count;
        } catch (SQLException se){
            se.printStackTrace();
        }
        System.out.println("ResultSet.getRow() == 0, impl 191");
        return new BigDecimal(0);
    }
    
    private int egIndex = 0;
    public java.math.BigDecimal getENTREZID() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select entrez_id from (select entrez_id, rownum rn from master_gene where rownum<" + (egIndex + 1) +") where rn=" + egIndex);
            rs.next();
            BigDecimal ei = rs.getBigDecimal("ENTREZ_ID");
            rs.close();
            statement.close();
            conn.close();
            return ei;
        } catch (SQLException se){
            se.printStackTrace();
        }
        return new BigDecimal(0);
    }
    
    public void setENTREZID(java.math.BigDecimal in0) throws java.rmi.RemoteException {
        egIndex = in0.intValue() + 1;
    }
    
    private int taxonIndex = 0;
    public java.math.BigDecimal getTAXONID() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select taxon_id from (select taxon_id, rownum rn from master_gene where rownum<" + (taxonIndex + 1) +") where rn=" + taxonIndex);
            rs.next();
            BigDecimal ti = rs.getBigDecimal("TAXON_ID");
            rs.close();
            statement.close();
            conn.close();
            return ti;
        } catch (SQLException se){
            se.printStackTrace();
        }
        return new BigDecimal(0);
    }
    
    public void setTAXONID(java.math.BigDecimal in0) throws java.rmi.RemoteException {
        taxonIndex = in0.intValue() + 1;
    }
    
    private int geneTypeIndex = 0;
    public java.lang.String getGENETYPE() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select GENE_TYPE from (select GENE_TYPE, rownum rn from master_gene where rownum<" + (geneTypeIndex + 1) +") where rn=" + geneTypeIndex);
            rs.next();
            String gt = rs.getString("GENE_TYPE");
            rs.close();
            statement.close();
            conn.close();
            return gt;
        } catch (SQLException se){
            se.printStackTrace();
        }
        return "";
    }
    
    public void setGENETYPE(java.lang.String in0) throws java.rmi.RemoteException {
        geneTypeIndex = Integer.parseInt(in0) + 1;
    }
    
    private int geneSymbolIndex = 0;
    public java.lang.String getGENESYMBOL() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select GENE_SYMBOL from (select GENE_SYMBOL, rownum rn from master_gene where rownum<" + (geneSymbolIndex + 1) +") where rn=" + geneSymbolIndex);
            rs.next();
            String gs = rs.getString("GENE_SYMBOL");
            rs.close();
            statement.close();
            conn.close();
            return gs;
        } catch (SQLException se){
            se.printStackTrace();
        }
        return "";
    }
    
    public void setGENESYMBOL(java.lang.String in0) throws java.rmi.RemoteException {
        geneSymbolIndex = Integer.parseInt(in0) + 1;
    }
    
    public java.lang.Object[] getENTREZTOGO() throws java.rmi.RemoteException {
        return null;
    }
    
    private int locusTagIndex = 0;
    public java.lang.String getLOCUSTAG() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select LOCUS_TAG from (select LOCUS_TAG, rownum rn from master_gene where rownum<" + (locusTagIndex + 1) +") where rn=" + locusTagIndex);
            rs.next();
            String lt = rs.getString("LOCUS_TAG");
            rs.close();
            statement.close();
            conn.close();
            return lt;
        } catch (SQLException se){
            se.printStackTrace();
        }
        return "";
    }
    
    public void setLOCUSTAG(java.lang.String in0) throws java.rmi.RemoteException {
        locusTagIndex = Integer.parseInt(in0) + 1;
    }
    
    private int descIndex = 0;
    public java.lang.String getDESCRIPTION() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select DESCRIPTION from (select DESCRIPTION, rownum rn from master_gene where rownum<" + (descIndex + 1) +") where rn=" + descIndex);
            rs.next();
            String desc = rs.getString("DESCRIPTION");
            rs.close();
            statement.close();
            conn.close();
            return desc;
        } catch (SQLException se){
            se.printStackTrace();
        }
        return "";
    }
    
    public void setDESCRIPTION(java.lang.String in0) throws java.rmi.RemoteException {
        descIndex = Integer.parseInt(in0) + 1;
    }

    public Object[] getGENEROW(BigDecimal in0) throws RemoteException {
        int index = in0.intValue();
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select ENTREZ_ID, TAXON_ID, GENE_TYPE, CHROMOSOME, GENE_SYMBOL, LOCUS_TAG, DESCRIPTION " +
                    "from (select ENTREZ_ID, TAXON_ID, GENE_TYPE, CHROMOSOME, GENE_SYMBOL, LOCUS_TAG, DESCRIPTION, rownum rn from master_gene " +
                    "where rownum<" + (index + 1) +") where rn=" + index);
//            if (rs.getRow() == 0)
//                return new Object[]{};
            rs.next();
            BigDecimal ei = rs.getBigDecimal("ENTREZ_ID");
            BigDecimal ti = rs.getBigDecimal("TAXON_ID");
            String gt = rs.getString("GENE_TYPE");
            String chr = rs.getString("CHROMOSOME");
            String gs = rs.getString("GENE_SYMBOL");
            String lt = rs.getString("LOCUS_TAG");
            String desc = rs.getString("DESCRIPTION");
            rs.close();
            statement.close();
            conn.close();
            return new Object[]{ei, ti, gs, lt, chr, desc, gt};
        } catch (SQLException se){
            se.printStackTrace();
        }
        return new Object[]{};
    }

    public BigDecimal getINTERACTIONCOUNT(String in0, BigDecimal in1) throws RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT count(*) FROM PAIRWISE_INTERACTION where MS_ID1=" +
                    "(SELECT EntrezID from MASTER_GENE where GENE_SYMBOL=" + in0 + ") or " +
                    "MS_ID2=(SELECT EntrezID from MASTER_GENE where GENE_SYMBOL=" + in0 + ") " +
                    "and INTERACTION_TYPE=" + in1.intValue());
            rs.next();
            System.out.println("count: " + rs.getObject(1));
            BigDecimal count = (BigDecimal)rs.getObject(1);
            rs.close();
            statement.close();
            conn.close();
            return count;
        } catch (SQLException se){
            se.printStackTrace();
        }
        System.out.println("ResultSet.getRow() == 0, impl 191");
        return new BigDecimal(0);
    }
    
    public java.lang.Object[] getFIRSTNEIGHBORS(java.lang.String in0) throws java.rmi.RemoteException{
        return null;
    }
}