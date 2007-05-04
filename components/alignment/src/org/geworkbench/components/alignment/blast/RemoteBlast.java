package org.geworkbench.components.alignment.blast;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import org.geworkbench.util.session.SoapClient;

import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RemoteBlast is a class that implements submission of a protein sequence to
 * the NCBI BLAST server and retrieval of  results with a BLAST RID #.  It
 * writes retrieved results out to a file.
 */
public class RemoteBlast {
    private final static String NCBIHEADER =
            "<HTML><HEAD><meta http-equiv=\"content-type\""
                    + "content=\"text/html;charset=utf-8\" /></HEAD><BODY BGCOLOR=\"#FFFFFF\" LINK=\"#0000FF\" VLINK=\"#660099\" ALINK=\"#660099\">"
                    + "<IMG SRC=\"http://www.ncbi.nlm.nih.gov/blast/images/head_results.gif\"    WIDTH=\"600\" HEIGHT=\"45\" ALIGN=\"middle\">"
                    + "<title>NCBI Blast Result</title><br><br>";

    public RemoteBlast() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * The protein sequence to submit to Blast.
     */
    private String query;
    private String waitingTime = "0";

    private static String DEFAULTPROGRAM = "blastp";
    private static String DEFAULTDBNAME = "nr";
    /**
     * The default file name to write results out to.
     */
    private final String DEFAULT_FILENAME = "BLAST_results.txt";
    /**
     * The file name to write results out to.
     */
    private String filename;
    /**
     * The textArea belonging to the BlastInfo panel to send messages to for
     * informing the user of Blast status.
     */
    private JTextArea textArea;
    /**
     * The Conserved Domain RID#.
     */
    private String CDD_rid;
    /**
     * The default port number for the socket to connect to.
     */
    private final int DEFAULT_PORT = 80;
    /**
     * The default server address for the socket to connect to.
     */
    private final String Blast_SERVER = "www.ncbi.nlm.nih.gov";
    /**
     * A flag indicating whether Blast results have been retrieve.
     * <code>true</code if Blast is done, <code>false</code if not.
     */
    private boolean getBlastDone = false;
    /**
     * Regular expression to parse out CDD RID# with.
     */
    private Pattern p1 = Pattern.compile("RID=([0-9]+-[0-9]+-[0-9]+)");
    /**
     * Regular expression to parse out negative CDD Search results with.
     */
    private Pattern p2 = Pattern.compile(
            "No.putative.conserved.domains.have.been.detected");
    /**
     * the combination of parameters.
     */
    private String cmdLine;
    /**
     * The URL of the Blast result coresponds to one sequence. Don't use it in the problem.
     */
    private String resultURLString;

    private final String SUBMITPREFIX =
            "Put http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?CMD=Put&QUERY=";
    private final String RESULTPREFIX =
            "Get http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?CMD=Get&FORMAT_TYPE=";
    private String dbName;
    private String programName;

    /**
     * Creates a new RemoteBlast and sets query and textArea to the specified
     * String and JTextArea values.  Also sets filename to the default file
     * name.
     *
     * @param the JTextArea to set textArea with.
     * @param the String value to set query with.
     */
    public RemoteBlast(String query, JTextArea textArea) {
        this.query = query;
        this.filename = DEFAULT_FILENAME;
        this.textArea = textArea;
        this.CDD_rid = null;
    }

    public RemoteBlast(String query, JProgressBar progressBar) {
        this.query = query;
        this.filename = DEFAULT_FILENAME;
        this.textArea = new JTextArea();
        this.CDD_rid = null;
    }

    public RemoteBlast(String query, String filename) {
        this.query = query;
        this.filename = filename;
        this.textArea = new JTextArea();
        this.CDD_rid = null;
    }

    /**
     * Creates a new RemoteBlast and sets query, filenmae, and textArea to the
     * specified String and JTextArea values.
     *
     * @param the String value to set query with.
     * @param the String value to set filename with.
     * @param the JTextArea to set textArea with.
     */
    public RemoteBlast(String query, String filename, JTextArea textArea) {
        this.query = query;
        this.filename = filename;
        this.textArea = textArea;
        this.CDD_rid = null;
    }

    /**
     * Returns <code>true</code> if Blast is done, <code>false</code> if not.
     *
     * @return getBlastDone - boolean that indicates if Blast is done.
     */
    public boolean getBlastDone() {
        return getBlastDone;
    }

    /**
     * Returns the Conserved Domain Search RID #.
     *
     * @return CDD_rid - the String representing the CDD RID #.
     */
    public String getCDD_rid() {
        return CDD_rid;
    }

    /**
     * Creates a socket connection to the NCBI Blast server and submits an
     * HTTP request to Blast with the query and parses out the Blast RID #.
     * Also initiates a Conserved Domain Search and parses out the resulting
     * CDD RID # used for retrieval of CDD Search results if domains found.
     *
     * @return a String representing the Blast RID # used to retrieve
     *         Blast results, <code>null</code> if not successful.
     */
    public String submitBlast() {

        String message = ""; /* HTTP GET message */

        Socket s = null;

        if (cmdLine != null) {
            message = SUBMITPREFIX + query + cmdLine;
        }

        try {

            s = new Socket(Blast_SERVER, DEFAULT_PORT);

            //create an output stream for sending message.
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            //create buffered reader stream for reading incoming byte stream.
            InputStreamReader inBytes = new InputStreamReader(s.getInputStream());
            BufferedReader in = new BufferedReader(inBytes);

            //System.out.println("\n\nSending message: " + message + "\n");

            //write String message to output stream as byte sequence.
            out.writeBytes(message);

            //reads each incoming line until it finds the CDD and Blast RIDs.
            while (true) {
                String data = in.readLine();
//                System.out.println(data);
                if (CDD_rid == null && data != null) {
                    Matcher m1 = p1.matcher(data);
                    Matcher m2 = p2.matcher(data);
                    if (m1.find()) {
                        CDD_rid = m1.group(1);
                    }
                    if (m2.find()) {
                        CDD_rid = "none";

                    }
                }
                if (data == null) {
                    break;
                }

                if (data.equals("<!--QBlastInfoBegin")) {
                    StringTokenizer st = new StringTokenizer(in.readLine(), " ");
                    String str = st.nextToken();
                    str = st.nextToken();
                    s.close();
                    return st.nextToken();
                }

                if (data == null) {
                    break;
                }
            }
        } catch (UnknownHostException e) {
            System.out.println("Socket:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("readline:" + e.getMessage());
        }
        return null;
    }

    /* end of submitBlast method */

    public String submitBlast(String message) {

        // String message; /* HTTP GET message */

        Socket s = null;

        try {

            s = new Socket(Blast_SERVER, DEFAULT_PORT);

            //create an output stream for sending message.
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            //create buffered reader stream for reading incoming byte stream.
            InputStreamReader inBytes = new InputStreamReader(s.getInputStream());
            BufferedReader in = new BufferedReader(inBytes);

            textArea.append("\n\nSending message: " + message + "\n");
            //System.out.println(message);
            //write String message to output stream as byte sequence.
            out.writeBytes(message);

            //reads each incoming line until it finds the CDD and Blast RIDs.
            while (true) {
                String data = in.readLine();
                if (CDD_rid == null) {
                    Matcher m1 = p1.matcher(data);
                    Matcher m2 = p2.matcher(data);
                    if (m1.find()) {
                        CDD_rid = m1.group(1);
                    }
                    if (m2.find()) {
                        CDD_rid = "none";
                        System.out.println(
                                "No.putative.conserved.domains.have.been.detected");
                    }
                }
                if (data.equals("<!--QBlastInfoBegin")) {
                    StringTokenizer st = new StringTokenizer(in.readLine(), " ");
                    String str = st.nextToken();
                    str = st.nextToken();
                    s.close();
                    return st.nextToken();
                }
                //				System.out.println(data);
                if (data == null) {
                    break;
                }
            }
        } catch (UnknownHostException e) {
            System.out.println("Socket:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("readline:" + e.getMessage());
        }
        return null;
    }

    /**
     * Sets getBlastDone to <code>false</code> indicating Blast is not done yet
     * and creates a new GetBlast with the specified String as a parameter.
     *
     * @param rid - String representing the Blast RID# to retrieve
     *            results for.
     */
    public void getBlast(String rid) {
        getBlastDone = false;
        GetBlast bl = new GetBlast(rid);
    }

    public void getBlast(String rid, String format) {
        getBlastDone = false;
        String message =
                RESULTPREFIX +
                        format + "&RID=" + rid + "\r\n\r\n";
        resultURLString =
                "http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?CMD=Get&FORMAT_TYPE=" +
                        format + "&RID=";
        // System.out.println(new Date() + message);
        GetBlast getBlast = new GetBlast(message);
        //  runMain(message);
        //System.out.println(new Date() + "END");
        //GetBlast bl = new GetBlast(rid, format);
    }

    /**
     * This class is a Thread that retrieves Blast results by Blast RID#, which
     * can take some period of time.  The thread continually requests results
     * from the NCBI Blast server with the Blast RID# via an HTTP request using
     * a Socket and writes results out to file.
     */
    private class GetBlast extends Thread {

        String message;
        String file;
        Socket s;

        public GetBlast(String Blast_rid) {
            message = Blast_rid;
            this.start();
        }


        public GetBlast(String Blast_rid, String format) {
            message =
                    RESULTPREFIX + format + "&RID=" + Blast_rid + "\r\n\r\n";

            this.start();
        }

        public void run() {

            runMain(message);

        } //end of run().

        public void runMain(String message) {

            String file;
            Socket s;
            try {
                //create an output stream for writing to a file. appending file.
                PrintStream ps = new PrintStream(new FileOutputStream(new File(
                        filename), true), true);

                //print header.

                ps.println(NCBIHEADER);

                boolean BlastnotDone = false;
                while (!BlastnotDone) {

                    s = new Socket(Blast_SERVER, DEFAULT_PORT);

                    //create an output stream for sending message.
                    DataOutputStream out = new DataOutputStream(s.
                            getOutputStream());

                    //create buffered reader stream for reading incoming byte stream.
                    InputStreamReader inBytes = new InputStreamReader(s.
                            getInputStream());
                    BufferedReader in = new BufferedReader(inBytes);

                    textArea.append(message + "\n");

                    //write String message to output stream as byte sequence.
                    out.writeBytes(message);

                    String data = in.readLine();
//                System.out.println("IN HTML" +   data );
                    boolean done = false;
                    boolean getWaitingTime = false;

                    while (data != null) {
                        // System.out.println("IN HTML" +   data );
                        if (data.equals("\tStatus=WAITING")) {
                            done = false;
                        } else if (data.equals("\tStatus=READY")) {
                            BlastnotDone = true;
                            done = true;
                            data = in.readLine();
                            data = in.readLine();
                            data = in.readLine();
                            break;
                            //ps.println(data);
                        }
                        if (getWaitingTime) {
                            if (data == null) {
                                setWaitingTime("0");
                            } else {

                                setWaitingTime(data.substring(4, 12));
                            }

                            getWaitingTime = false;
                        }
                        if (data.trim().startsWith(
                                "<tr><td>Time since submission</td>")) {
                            getWaitingTime = true;
                        }
                        data = in.readLine();
                    }
                    if (!done) {

                        Thread.sleep(SoapClient.TIMEGAP);
                    } else {
                        //todo Remove the new feature. WE need figure out a way to download the images later.
                        boolean needRemoveNewFeature = false;
                        while (data != null) {
                            data = updateImageLink(data);
                            if (data.trim().startsWith("<div id=\"graphic\" class=\"blRes\">")) {
                                needRemoveNewFeature = true;
                            } else
                            if (data.trim().startsWith("</center><hr></div><!--/#graphic-->") && needRemoveNewFeature) {
                                needRemoveNewFeature = false;
//                                data = in.readLine();
//                                ps.println(data);
//                                while (!data.trim().startsWith("<a href=\"http://www.ncbi.nlm.nih.gov/blast/treeview/blast_tree_view.cgi?"))
//                                {
//                                    ps.println(data);
//                                    data = in.readLine();
//
//                                }
//                                String newStr = "http://www.ncbi.nlm.nih.gov/blast/images/D.gif";
//                                if (data.indexOf("src=\"/blast/images") > -1) {
//                                    data = data.replaceAll("src=\"/blast/images", "http://www.ncbi.nlm.nih.gov/blast/images");
//                                }
//
//                                ps.println(data);
                            } else if (!needRemoveNewFeature) {

                                ps.println(data);
                            }
                            data = in.readLine();
                        }
                    }

                    s.close();
                } //end of while (BlastnotDone).
                textArea.append("Blast done! You can now display your results");
                getBlastDone = true;
                ps.close();
            } catch (UnknownHostException e) {
                System.out.println("Socket:" + e.getMessage());
            } catch (EOFException e) {
                System.out.println("EOF:" + e.getMessage());
            } catch (IOException e) {
                System.out.println("readline:" + e.getMessage());
            } catch (InterruptedException e) {
                System.out.println("wait:" + e.getMessage());
            }
        } //end of run().


    } //end of class GetBlast.

    private String updateImageLink(String data) {
        if (data.indexOf("SRC=\"/blast/images") > -1) {
            data = data.replaceAll("SRC=\"/blast/images", "src=\"http://www.ncbi.nlm.nih.gov/blast/images");
        }
        if (data.indexOf("src=\"/blast/images") > -1) {
            data = data.replaceAll("src=\"/blast/images", "src=\"http://www.ncbi.nlm.nih.gov/blast/images");
        }
         if (data.indexOf("href=\"/blast/") > -1) {
            data = data.replaceAll("href=\"/blast/", "href=\"http://www.ncbi.nlm.nih.gov/blast/");
        }
         if (data.indexOf("type=\"checkbox\"") > -1) {
            data = data.replaceAll("type=\"checkbox\"", "type=\"hidden\"");
        }
            if (data.indexOf("type=\"button\"") > -1) {
            data = data.replaceAll("type=\"button\"", "type=\"hidden\"");
        }
        if (data.indexOf("type=button") > -1) {
            data = data.replaceAll("type=button", "type=\"hidden\"");
        }

        return data;
    }

//    public static void main(String[] args) {
//        connectJDBC(args);
//        String query = "MGARCPTRTLRARQPAHPRPPGTPRHHQRRPLPAASPTRHRSSRGRQIRARRPDRPGTRLRTGAAVDRQQPQHAPLRPLRLRSARADPRPQPGKPARRNPGHQRPRPRCRRPGAQQRPADRTLPADRSARHRVPAAPPAARPAHRRARQRRLRPARRPARPAGTRPLHDSRTRPAQLSGAADLRSDRRPATDRDHRQRRSPLLPAPCRRHHRTRRPPLPARIPPPVHSAAPHPPQQRGTRGNRGRPLLREPAQRHPSSRRRLRAPHRGRLPPGYRPAPQRGLPDHGHRRQDQRTHPRVPAGARGNGVAPTHGHPRMTSRRSHETPQGPDPRSPGAAPAYREAPPALTGRE";
//        RemoteBlast test = new RemoteBlast(query);
//        String message =
//                "Put http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?CMD=Put&QUERY=" +
//                        query + "&DATABASE=nr&PROGRAM=blastp&FILTER=L&GAPCOSTS=11%202&HITLIST_SZE=500&AUTO_FORMAT=Semiauto&CDD_SEARCH=on&SHOW_OVERVIEW=on&SERVICE=plain\r\n\r\n";
//
//        String Blast_rid = test.submitBlast(message);
//        String format = "HTML";
//        test.getBlast(Blast_rid, format);
//        format = "TEXT";
//        System.out.println("START TEXT");
//
//        //test.getBlast(Blast_rid, format);
//    }

    public static void connectJDBC(String args[]) {

        try {
            Statement stmt;

            //Register the JDBC driver for MySQL.
            Class.forName("com.mysql.jdbc.Driver");

            //Define URL of database server for
            // database named mysql on the localhost
            // with the default port number 3306.
            String url =
                    "jdbc:mysql://genome-mysql.cse.ucsc.edu:3306/hg18";

            //Get a connection to the database for a
            // user named root with a blank password.
            // This user is the default administrator
            // having full privileges to do anything.
            Connection con =
                    DriverManager.getConnection(
                            url, "genome", "");

            //Display URL and connection information
            System.out.println("URL: " + url);
            System.out.println("Connection: " + con);

            //Get a Statement object
            stmt = con.createStatement();
            boolean success = stmt.execute("select * from knownGene where name = 'BC073913' ");
            success = stmt.execute("select * from knownGene where name = 'NM_010548'");
            if (success) {
                ResultSet rs = stmt.getResultSet();
                while (rs.next()) {

                    //String s = rs.getString(1);
                    for (int i = 1; i < 10; i++) {
                        Object o = rs.getString(i);
                        System.out.println(o);
                    }
                }
                // Get the data from the row using the column name

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * RemoteBlast
     *
     * @param aQuery String
     */
    public RemoteBlast(String aQuery) {
        this.query = aQuery;
        textArea = new JTextArea();
        this.filename = DEFAULT_FILENAME;
        this.CDD_rid = null;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setWaitingTime(String waitingTime) {
        this.waitingTime = waitingTime;
    }

    public void setCmdLine(String cmdLine) {
        this.cmdLine = cmdLine;
    }

    public void setResultURLString(String resultURLString) {
        this.resultURLString = resultURLString;
    }

    public void setProgamName(String progamName) {
        this.programName = progamName;
    }

    public String getDbName() {
        return dbName;
    }

    public String getWaitingTime() {
        return waitingTime;
    }

    public String getCmdLine() {
        return cmdLine;
    }

    public String getResultURLString() {
        return resultURLString;
    }

    public String getProgamName() {
        return programName;
    }

    private void jbInit() throws Exception {
    }

}
