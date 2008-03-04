package interactions;

import java.math.BigDecimal;
import java.sql.*;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.ArrayList;

public class InteractionsConnectionImpl implements INTERACTIONS {

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
    public static String INTERACTIONDATABASEURL = "jdbc:mysql://afdev:3306/CNKB?autoReconnect=true";

    private Connection conn = null;
    private Statement statement = null;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public InteractionsConnectionImpl() {
        try {
            //conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            Class.forName("com.mysql.jdbc.Driver");
            conn =
                    DriverManager.getConnection(
                            INTERACTIONDATABASEURL, "xiaoqing", "zhangc2b2");
            statement = conn.createStatement();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public ArrayList<InteractionDetail> getPairWiseInteraction(BigDecimal id1) {
        ArrayList arrayList = new ArrayList<InteractionDetail>();

        try {

            Class.forName("com.mysql.jdbc.Driver");
            conn =
                    DriverManager.getConnection(
                            INTERACTIONDATABASEURL, "xiaoqing", "zhangc2b2");
            statement = conn.createStatement();

            boolean isFinished = statement.execute("SELECT * FROM pairwise_interaction where ms_id1=" + id1.toString() + " or ms_id2=" + id1.toString());
            int i = 0;
            if (isFinished) {
                ResultSet rs = statement.getResultSet();
                while (rs.next()) {
                    msid2 = rs.getBigDecimal("ms_id2");
                    
                    confidenceValue = rs.getDouble("confidence_value");
                    isModulated = rs.getString("is_modulated");
                    interactionType = rs.getString("interaction_type");
                    controlType = rs.getString("control_type");
                    direction = rs.getString("direction");
                    isReversible = rs.getString("is_reversible");
                    source = rs.getString("source");
                    if (InteractionDetail.PROTEINPRETEININTERACTION.equalsIgnoreCase(interactionType)) {
                        interactionType = InteractionDetail.PROTEINPRETEININTERACTION;
                    } else {
                        interactionType = InteractionDetail.PROTEINDNAINTERACTION;

                    }
                    InteractionDetail interactionDetail = new InteractionDetail(id1.toString(), msid2.toString(), confidenceValue, interactionType);
                    arrayList.add(interactionDetail);
                }


            }
            conn.close();
        } catch (Exception se) {
            se.printStackTrace();
        }
        return arrayList;
    }

    public String getINTERACTIONTYPE() throws java.rmi.RemoteException {
        return interactionType;
    }

    public void setINTERACTIONTYPE(String in0) throws java.rmi.RemoteException {
        interactionType = in0;
    }

    public BigDecimal getMSID2() throws java.rmi.RemoteException {
        return msid2;
    }

    public void setMSID2(BigDecimal in0) throws java.rmi.RemoteException {
        msid2 = in0;
    }

    public String getISREVERSIBLE() throws java.rmi.RemoteException {
        return isReversible;
    }

    public void setISREVERSIBLE(String in0) throws java.rmi.RemoteException {
        isReversible = in0;
    }

    public BigDecimal getMSID1() throws java.rmi.RemoteException {
        return msid1;
    }

    public void setMSID1(BigDecimal in0) throws java.rmi.RemoteException {
        msid1 = in0;
    }

    public String getSOURCE() throws java.rmi.RemoteException {
        return source;
    }

    public void setSOURCE(String in0) throws java.rmi.RemoteException {
        source = in0;
    }

    public String getCONTROLTYPE() throws java.rmi.RemoteException {
        return controlType;
    }

    public void setCONTROLTYPE(String in0) throws java.rmi.RemoteException {
        controlType = in0;
    }

    public String getDIRECTION() throws java.rmi.RemoteException {
        return direction;
    }

    public void setDIRECTION(String in0) throws java.rmi.RemoteException {
        direction = in0;
    }

    public double getCONFIDENCEVALUE() throws java.rmi.RemoteException {
        return confidenceValue;
    }

    public void setCONFIDENCEVALUE(double in0) throws java.rmi.RemoteException {
        confidenceValue = in0;
    }

    public String getISMODULATED() throws java.rmi.RemoteException {
        return isModulated;
    }

    public void setISMODULATED(String in0) throws java.rmi.RemoteException {
        isModulated = in0;
    }

    public BigDecimal getID() throws java.rmi.RemoteException {
        return id;
    }

    public void setID(BigDecimal in0) throws java.rmi.RemoteException {
        id = in0;
    }

    public void insert() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            statement.executeUpdate("INSERT INTO PAIRWISE_INTERACTION" +
                    "(ms_id1, ms_id2, confidence, is_modulated, interaction_type, control_type, direction, is_reversible, source) " +
                    "values \"" + msid1.toString() + "," + msid2.toString() + "," + confidenceValue + "," + isModulated + "," +
                    interactionType + "," + controlType + "," + direction + "," + isReversible + "," + source + "\"");
            conn.commit();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void retrieve() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM PAIRWISE_INTERACTION where ms_id1=" + msid1.toString());
            if (rs.getRow() > 0) {
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
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public String getCHROMOSOME() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select CHROMOSOME from (select CHROMOSOME, rownum rn from master_gene where rownum<" + (chromosomeId + 1) + ") where rn=" + chromosomeId);
            rs.next();
            String chr = rs.getString("CHROMOSOME");
            rs.close();
            statement.close();
            conn.close();
            return chr;
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return "";
    }

    private int chromosomeId = 0;

    public void setCHROMOSOME(String in0) throws java.rmi.RemoteException {
        chromosomeId = Integer.parseInt(in0);
    }

    public BigDecimal getGENECOUNT() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT count(*) FROM MASTER_GENE");
            rs.next();
            System.out.println("count: " + rs.getObject(1));
            BigDecimal count = (BigDecimal) rs.getObject(1);
            rs.close();
            statement.close();
            conn.close();
            return count;
        } catch (SQLException se) {
            se.printStackTrace();
        }
        System.out.println("ResultSet.getRow() == 0, impl 191");
        return new BigDecimal(0);
    }

    private int egIndex = 0;

    public BigDecimal getENTREZID() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select entrez_id from (select entrez_id, rownum rn from master_gene where rownum<" + (egIndex + 1) + ") where rn=" + egIndex);
            rs.next();
            BigDecimal ei = rs.getBigDecimal("ENTREZ_ID");
            rs.close();
            statement.close();
            conn.close();
            return ei;
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return new BigDecimal(0);
    }

    public void setENTREZID(BigDecimal in0) throws java.rmi.RemoteException {
        egIndex = in0.intValue() + 1;
    }

    private int taxonIndex = 0;

    public BigDecimal getTAXONID() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select taxon_id from (select taxon_id, rownum rn from master_gene where rownum<" + (taxonIndex + 1) + ") where rn=" + taxonIndex);
            rs.next();
            BigDecimal ti = rs.getBigDecimal("TAXON_ID");
            rs.close();
            statement.close();
            conn.close();
            return ti;
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return new BigDecimal(0);
    }

    public void setTAXONID(BigDecimal in0) throws java.rmi.RemoteException {
        taxonIndex = in0.intValue() + 1;
    }

    private int geneTypeIndex = 0;

    public String getGENETYPE() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select GENE_TYPE from (select GENE_TYPE, rownum rn from master_gene where rownum<" + (geneTypeIndex + 1) + ") where rn=" + geneTypeIndex);
            rs.next();
            String gt = rs.getString("GENE_TYPE");
            rs.close();
            statement.close();
            conn.close();
            return gt;
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return "";
    }

    public void setGENETYPE(String in0) throws java.rmi.RemoteException {
        geneTypeIndex = Integer.parseInt(in0) + 1;
    }

    private int geneSymbolIndex = 0;

    public String getGENESYMBOL() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select GENE_SYMBOL from (select GENE_SYMBOL, rownum rn from master_gene where rownum<" + (geneSymbolIndex + 1) + ") where rn=" + geneSymbolIndex);
            rs.next();
            String gs = rs.getString("GENE_SYMBOL");
            rs.close();
            statement.close();
            conn.close();
            return gs;
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return "";
    }

    public void setGENESYMBOL(String in0) throws java.rmi.RemoteException {
        geneSymbolIndex = Integer.parseInt(in0) + 1;
    }

    public Object[] getENTREZTOGO() throws java.rmi.RemoteException {
        return null;
    }

    private int locusTagIndex = 0;

    public String getLOCUSTAG() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select LOCUS_TAG from (select LOCUS_TAG, rownum rn from master_gene where rownum<" + (locusTagIndex + 1) + ") where rn=" + locusTagIndex);
            rs.next();
            String lt = rs.getString("LOCUS_TAG");
            rs.close();
            statement.close();
            conn.close();
            return lt;
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return "";
    }

    public void setLOCUSTAG(String in0) throws java.rmi.RemoteException {
        locusTagIndex = Integer.parseInt(in0) + 1;
    }

    private int descIndex = 0;

    public String getDESCRIPTION() throws java.rmi.RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select DESCRIPTION from (select DESCRIPTION, rownum rn from master_gene where rownum<" + (descIndex + 1) + ") where rn=" + descIndex);
            rs.next();
            String desc = rs.getString("DESCRIPTION");
            rs.close();
            statement.close();
            conn.close();
            return desc;
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return "";
    }

    public void setDESCRIPTION(String in0) throws java.rmi.RemoteException {
        descIndex = Integer.parseInt(in0) + 1;
    }

    public Object[] getGENEROW(BigDecimal in0) throws RemoteException {
        int index = in0.intValue();
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select ENTREZ_ID, TAXON_ID, GENE_TYPE, CHROMOSOME, GENE_SYMBOL, LOCUS_TAG, DESCRIPTION " +
                    "from (select ENTREZ_ID, TAXON_ID, GENE_TYPE, CHROMOSOME, GENE_SYMBOL, LOCUS_TAG, DESCRIPTION, rownum rn from master_gene " +
                    "where rownum<" + (index + 1) + ") where rn=" + index);
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
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return new Object[]{};
    }

    public BigDecimal getINTERACTIONCOUNT(BigDecimal in0, String in1) throws RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT count(unique MS_ID2) FROM PAIRWISE_INTERACTION where MS_ID1=" + in0.intValue() +
                    " and (INTERACTION_TYPE=\'" + in1.toLowerCase() + "\' or INTERACTION_TYPE=\'" + in1.toUpperCase() + "\')");
            rs.next();
            BigDecimal count = (BigDecimal) rs.getObject(1);
            rs.close();
            rs = statement.executeQuery("SELECT count(unique MS_ID1) FROM PAIRWISE_INTERACTION where MS_ID2=" + in0.intValue() +
                    " and (INTERACTION_TYPE=\'" + in1.toLowerCase() + "\' or INTERACTION_TYPE=\'" + in1.toUpperCase() + "\')");
            rs.next();
            count = new BigDecimal(count.intValue() + ((BigDecimal) rs.getObject(1)).intValue());
            rs.close();
            statement.close();
            conn.close();
            return count;
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return new BigDecimal(0);
    }

    public Object[] getFIRSTNEIGHBORS(BigDecimal in0, String in1) throws RemoteException {
        Vector<BigDecimal> neighbors = new Vector<BigDecimal>();
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@adora.cgc.cpmc.columbia.edu:1521:BIODB2", "interaction_ro", "linkt0cellnet");
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT MS_ID1 FROM PAIRWISE_INTERACTION where MS_ID2=" + in0.intValue() +
                    " and (INTERACTION_TYPE=\'" + in1.toLowerCase() + "\' or INTERACTION_TYPE=\'" + in1.toUpperCase() + "\')");
            while (rs.next()) {
                if (!neighbors.contains((BigDecimal) rs.getObject(1)))
                    neighbors.add((BigDecimal) rs.getObject(1));
            }
            rs.close();
            rs = statement.executeQuery("SELECT MS_ID2 FROM PAIRWISE_INTERACTION where MS_ID1=" + in0.intValue() +
                    " and (INTERACTION_TYPE=\'" + in1.toLowerCase() + "\' or INTERACTION_TYPE=\'" + in1.toUpperCase() + "\')");
            while (rs.next()) {
                neighbors.add((BigDecimal) rs.getObject(1));
            }
            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return neighbors.toArray();
    }
}
