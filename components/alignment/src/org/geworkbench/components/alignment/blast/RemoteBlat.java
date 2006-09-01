/*
 * RemoteBlat.java
 *
 * Created on July 31, 2006, 5:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.geworkbench.components.alignment.blast;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import org.geworkbench.util.session.SoapClient;
import org.geworkbench.components.alignment.panels.ParameterBlatSetting;
import org.geworkbench.components.alignment.panels.BlastAppComponent;

/**
 *
 * @author avv2101
 */

/**
 * RemoteBlat is a class that implements submission of a protein sequence to
 * the WEB BLAT server and retrieval of  results with a BLAT RID #.  It
 * writes retrieved results out to a file.
 */

public class RemoteBlat {
    private final static String BLAT_HEADER =
            "<HTML><HEAD><meta http-equiv=\"content-type\""
            + "content=\"text/html;charset=utf-8\" /></HEAD><BODY BGCOLOR=\"#FFFFFF\" LINK=\"#0000FF\" VLINK=\"#660099\" ALINK=\"#660099\">"
            + "<IMG SRC=\"http://www.ncbi.nlm.nih.gov/blast/images/head_results.gif\"    WIDTH=\"600\" HEIGHT=\"45\" ALIGN=\"middle\">"
            +"<title>Blat Search Result</title><br><br>";
            
    public RemoteBlat() {
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
    
    /**
     * The default file name to write results out to.
     */
    private final String DEFAULT_FILENAME = "BLAT_RESULTS.txt";
    /**
     * The file name to write results out to.
     */
    private String filename;
    /**
     * The default port number for the socket to connect to.
     */
    private ArrayList output;
    
    private String sequenceID;
    
    private static final int DEFAULT_PORT = 80;
    /**
     * The default server address for the socket to connect to.
     */
    private static final String BLAT_SERVER = "www.genome.ucsc.edu";
    
    private final String BLAT = "Blat";
    /**
     * A flag indicating whether Blat results have been retrieve.
     * <code>true</code if Blat is done, <code>false</code if not.
     */
    private boolean getBlatDone = false;
    
    private Pattern p1 = Pattern.compile("BLAT Search Results");
    /**
     *Regular expression to find the hgsid value of the web Blat
     */
    private Pattern p3 = Pattern.compile("hgsid=");
    
    private Pattern p4 = Pattern.compile("Alignment of");
    /**
     * the combination of parameters.
     */

    private String cmdLine;
    /**
     * the option feel lucky
     */
    private String strFeelLucky = null;
    
    /**
     *  The URL of the Blast result coresponds to one sequence. Don't use it in the problem.
     */
    private String resultURLString;

    private final String SUBMITPREFIX ="PUT /cgi-bin/hgBlat?CMD=Put&hgsid=";
    private final String RESULTPREFIX =
            "Get http://genome.ucsc.edu/cgi-bin/hgBlat?CMD=Get&FORMAT_TYPE=";
    
    /**
     * This is the patterns used to extracts informations from Blat Server web pages.
     * It should be modified accordingly in case there is an update on the structure 
     * of the web page
     */
    private final String BLATDIRECTORY = "/cgi-bin/hgBlat"; //the directory where the blat script resides 
    private final String ENDOFRESULTS = "</TT></PRE>"; //all information we need will stay before this line
    private final String STARTOFRESULTS = "<TT><PRE>";//all information we need will stay after this line    
    private final String CONTENTPREFIX = "/trash/body/body_"; //the prefix of the links content the results
    private final String NAMEOFSEQUENCE = "Alignment of ";//the line content the name of the sequence
    private final String GENOMICPREFIX = "chr"; //chr + Number is the name of genomic
    private final char DELIMITER1 = ':';
    private final char DELIMITER2 = '-';
    private final char DELIMITER3 = '<';
    private final String BORDEROFSEQUENCE = "<FONTCOLOR=\"#22CCEE\">";//starting or ending of matching sequence;
    private final int numOfGenOnEachLine = 50;// the number of Gen that web Blat server will print out on each line
    private final String REVERSESTRAND = "reverse strand";
    /**
     * Constructor, set query and file output name
     */
    public RemoteBlat(String query, String sequenceID, String filename, ArrayList output) {
        this.query = query;
        this.sequenceID = sequenceID;
        this.filename = filename;
        this.output = output;
    }

    /**
     * Returns <code>true</code> if Blat is done, <code>false</code> if not.
     *
     * @return getBlatDone - boolean that indicates if Blat is done.
     */
    public boolean getBlatDone() {
        return getBlatDone;
    }
     public void getBlat(String message) {
        getBlatDone = false;
        GetBlat bl = new GetBlat(message);
    }
    /**
     * Creates a socket connection to the Web Blast server and submits an
     * HTTP request to Blat with the query and parses out the Blat RID #.
     * Also initiates a Conserved Domain Search and parses out the resulting
     * CDD RID # used for retrieval of CDD Search results if domains found.
     *
     * @return a String representing the Blat RID # used to retrieve
     *         Blat results, <code>null</code> if not successful.
     */
    public String submitBlat() {

        String message = ""; /* HTTP GET message */
        String sResult = "";
        Socket s = null;
        try {
            //first, we need to get an identification number from the server (hgsid)
            s = new Socket(BLAT_SERVER, DEFAULT_PORT);

            //create an output stream for sending message.
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            
            //create buffered reader stream for reading incoming byte stream.
            InputStreamReader inBytes = new InputStreamReader(s.getInputStream());
            BufferedReader in = new BufferedReader(inBytes);
            
            out.writeBytes("PUT " + BLATDIRECTORY + "\r\n");
            String hgsid = null;
            while (hgsid == null) {
                String sData = in.readLine();
                if (sData == null){
                    System.out.println("Can't connect to the server, Communication Error");
                    return null;
                }
                Matcher m3 = p3.matcher(sData);
                if (m3.find()){
                    int iCount = m3.end();
                    int iStart = iCount;
                    while(Character.isDigit(sData.charAt(iCount))){
                        iCount++;
                    }
                    hgsid = sData.substring(iStart, iCount);
                }
            }
            inBytes.close();
            out.close();
            if (hgsid == null){
                System.out.println("Can't get hgsid, Communication Error with the Server");
                return null;
            }
                
            s = new Socket(BLAT_SERVER, DEFAULT_PORT);
            out = new DataOutputStream(s.getOutputStream());
            inBytes = new InputStreamReader(s.getInputStream());
            in = new BufferedReader(inBytes);
            
            //write String message to output stream as byte sequence.
            
            //reads each incoming line until it finds the CDD and Blast RIDs.
            if (cmdLine != null) {
                if (strFeelLucky == null)
                    message = SUBMITPREFIX + hgsid + cmdLine + query +"\r\n";
                else
                    message = SUBMITPREFIX + hgsid + cmdLine + query + "&Lucky=" + formatString(strFeelLucky)+"\r\n";
            }
            out.writeBytes(message);
            //case of option String FeelLucky to be implemented here
            if(strFeelLucky != null){
                //not yet implemented
                return null;
            }
            String sData = null;
            while (true) {
                sData = in.readLine();
                if (sData == null) {
                    break;
                }
                Matcher m1 = p1.matcher(sData);
                if (m1.find()) {
                    sData = in.readLine();
                    break;
                }
            }
            while (true){
                sData = in.readLine();
                if (sData == null){
                    break;
                }
                if (sData.equals(ENDOFRESULTS)){
                    break;
                }
                String newQuery = getString(sData, '\"', '\"', 1);
                sResult += getIDRequest("PUT "+newQuery.substring(2)+"\r\n") + ":";
            }
        } catch (UnknownHostException e) {
            System.out.println("Socket:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("readline:" + e.getMessage());
        }
        return sResult;
    }

    /* end of submitBlat method */

    /**
     * This class is a Thread that retrieves Blat results by Blat RID#, which
     * can take some period of time.  The thread continually requests results
     * from the Blat server with the Blat RID# via an HTTP request using
     * a Socket and writes results out to file.
     */
    private class GetBlat extends Thread {
        String message;

        public GetBlat(String message) {
            this.message = message;
            this.start();
        }
        public void run() {
            runMain(message);
        } 

        public void runMain(String message) {
            String file;
            Socket s;
            try {
                String[] messageSplit = message.split(":");
                int i = 0;
                int n = messageSplit.length;
                //create an output stream for writing to a file. appending file.
                PrintStream ps = new PrintStream(new FileOutputStream(new File(
                        filename), true), true);
                //print header.
                ps.println(BLAT_HEADER);
                while (i < n) { 
                    s = new Socket(BLAT_SERVER, DEFAULT_PORT);
                    DataOutputStream out = new DataOutputStream(s.
                            getOutputStream());
                    InputStreamReader inBytes = new InputStreamReader(s.
                            getInputStream());
                    BufferedReader in = new BufferedReader(inBytes);
                    out.writeBytes("GET " + CONTENTPREFIX + messageSplit[i] + ".html\r\n");
                    BlatOutput blatOutput = getBlatOutput(in);
                    printOut(ps, blatOutput);//method to be modified to make the output look better
                    output.add(blatOutput);
                    s.close();
                    i += 1;
                    Thread.sleep(SoapClient.TIMEGAP);
                } 
                getBlatDone = true;
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
        } 


    } //end of class GetBlat.

    public void setWaitingTime(String waitingTime) {
        this.waitingTime = waitingTime;
    }

    public void setCmdLine(String cmdLine) {
        this.cmdLine = cmdLine;
    }
    /**
     *This method generate a command line from a parameter setting object
     */
    public void setCmdLine(ParameterBlatSetting ps){
        String strResult = "&org=";
        strResult += formatString(ps.getStringGenome());
        strResult += "&db=";
        strResult += formatString(ps.getStringAssembly());
        strResult += "&type=";
        strResult += formatString(ps.getStringQueryType());
        strResult += "&sort=";
        strResult += formatString(ps.getStringSortOutput());
        strResult += "&output=";
        strResult += formatString(ps.getStringOutputType());
        strResult += "&userSeq=";
        this.cmdLine = strResult; 
    }
    
    public void setStringFeelLucky(String strFeelLucky){
        this.strFeelLucky = strFeelLucky;
    }
    public void setResultURLString(String resultURLString) {
        this.resultURLString = resultURLString;
    }

    public String getWaitingTime() {
        return waitingTime;
    }

    public String getCmdLine() {
        return cmdLine;
    }
    public String getStringFeelLucky() {
        return strFeelLucky;
    }
    public String getResultURLString() {
        return resultURLString;
    }
    
    private void jbInit() throws Exception {
    }
    /**
     *This method take a string input and return a new string in 
     *web format: ' -> %27, space -> + for examples
     */
    public static String formatString(String input){
        char[] cArray = input.toCharArray();
        int i = 0;
        int nlength = cArray.length;
        while(i < cArray.length){
            switch(cArray[i]){
                case '\'':
                    nlength += 2;
                    break;
                case ',':
                    nlength +=2;
                    break;
                default:
                    break;
            }
            i++;
        }
        char[] cResult = new char[nlength];
        i = 0;
        int j = 0;
        while(i < cArray.length){
            switch(cArray[i]){
                case ' ':
                    cResult[j] = '+';
                    break;
                case '\'':
                    cResult[j] = '%';
                    cResult[j+1] = '2';
                    cResult[j+2] = '7';
                    j += 2;
                    break;
                case ',':
                    cResult[j] = '%';
                    cResult[j+1] = '2';
                    cResult[j+2] = 'C';
                    j += 2;
                    break;
                default:
                    cResult[j] = cArray[i];
                    break;
            }
            i++;
            j++;
        }
        String strResult = String.copyValueOf(cResult);
        return strResult;
    }
    public static String getBlatServer(){
        return BLAT_SERVER;
    }
    public static int getBlatPort(){
        return DEFAULT_PORT;
    }
/** This method will find two char regex in a string and 
 *  return the substring between these two characters. 
 * The first n pair will be ignored.   
 **/    
    public static String getString(String sInput, char regex1, char regex2, int n){
        char[] cInput = sInput.toCharArray();
        int i = 0;
        int start = -1;
        int end = -1;
        int count = 0;
        while(count < n){
            while (i < cInput.length && cInput[i] != regex1){
                i++;
            }
            if (i == cInput.length){
                break;
            }
            i++;
            while (i < cInput.length && cInput[i] != regex2){
                i++;
            }
            if (i == cInput.length){
                break;
            }
            count++;
        }
        if (count < n)
            return null;
        else {
            if (n > 0)
                i++;
            while(i < cInput.length && cInput[i] !=regex1){
                i++;
            }
            if (i < cInput.length){
                start = i + 1;
                i++;
            }
            while(i < cInput.length && cInput[i] !=regex2){
                i++;
            }
            if (i < cInput.length){
                end = i;
                i++;
            }
            if (start != -1 && end != -1)
                return sInput.substring(start, end);
            else
                return null;
        }
    }
    public static String getString(String sInput, char regex1, char regex2){
        return getString(sInput, regex1, regex2, 0);
    }
    public static String getString(String sInput, char regex){
        return getString(sInput, regex, regex);
    }
    /*** This method will send message to the server via socket and
    *** extract the ID necessary to get the matching sequences
    ***/
    public String getIDRequest(String message){
        String sResult = null;
        Pattern p1 = Pattern.compile(CONTENTPREFIX);
        Pattern p2 = Pattern.compile(".html");
        try{
            Socket s = new Socket(BLAT_SERVER, DEFAULT_PORT);
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            InputStreamReader inBytes = new InputStreamReader(s.getInputStream());
            BufferedReader in = new BufferedReader(inBytes); 
            out.writeBytes(message);
            while(true){
                 String sData = in.readLine();
                 if (sData == null)
                     break;
                 Matcher m1 = p1.matcher(sData);
                 if (m1.find()){
                     Matcher m2 = p2.matcher(sData);
                     if(m2.find()){
                         if (m2.start() > m1.end())
                            return sData.substring(m1.end(), m2.start());
                     }
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
    /*
     **read the input and extract necessary information, store it in a structure Blatoutput
     */
    public BlatOutput getBlatOutput(BufferedReader input){
        BlatOutput blatOutput = new BlatOutput();
        Pattern p = Pattern.compile(NAMEOFSEQUENCE);
        try{
            String sData = null;
            String cDNASequence = "";
            String genomicSequence = "";
            //get CDNA name
            blatOutput.setCDNA(sequenceID);
            /*get genomic name, gStart and gEnd
             *****************/
            while(blatOutput.genomic == null){
                sData = input.readLine();
                if (sData == null)
                    break;
                Matcher m = p.matcher(sData);
                if (m.find()){
                    String tempo = sData.substring(m.end());
                    String[] tempoSplit = tempo.split(" ");
                    int i = tempoSplit.length - 1;
                    while (i >= 0){
                        if (tempoSplit[i].startsWith(GENOMICPREFIX))
                            break;
                        i--;
                    }
                    if (i == -1){
                        System.out.println("Format of the Blat Server Web page has changed, please check the code:CHR");
                        break;
                    }
                    blatOutput.genomic = GENOMICPREFIX.charAt(0) + getString(tempoSplit[i], GENOMICPREFIX.charAt(0), ':');
                    blatOutput.gStart = Integer.parseInt(getString(tempoSplit[i], DELIMITER1, DELIMITER2));
                    blatOutput.gEnd = Integer.parseInt(getString(tempoSplit[i], DELIMITER2, DELIMITER3));
                    }
                }
            /*get matching sequences in CDNA
             *******************************/
            Pattern p1 = Pattern.compile(STARTOFRESULTS);
            while(true){
                sData = input.readLine();
                if (sData == null)
                    break;
                Matcher m = p1.matcher(sData);
                if(m.find())
                    break;
            }
            Pattern p2 = Pattern.compile(ENDOFRESULTS);
            sData += input.readLine();
            String[] firstLineSplit = sData.split(" ");
            //reference of the first letter in the sequence, 
            int reference = Integer.parseInt(firstLineSplit[firstLineSplit.length - 1]) - numOfGenOnEachLine + 1;
            while(true){
                if (sData == null){
                    System.out.println("Format of the Blat Server Web page has changed, please check the code: line 555");
                    break;
                }
                cDNASequence += removeLastNumber(sData);
                Matcher m = p2.matcher(sData);
                if(m.find())
                    break;
                sData = input.readLine();
            }
            Pattern pFirstOccurent = Pattern.compile(BORDEROFSEQUENCE);
            Matcher mFirstOccurent = pFirstOccurent.matcher(cDNASequence);
            int offset = -1;
            if(mFirstOccurent.find()){
                offset = reference + countCharacters(cDNASequence, 0, mFirstOccurent.start());
            }
            blatOutput.cDNASegments = getSegments(cDNASequence, offset);  
            blatOutput.cDNAStart = offset;
            GenomeSegment g = (GenomeSegment) blatOutput.cDNASegments.get(blatOutput.cDNASegments.size() - 1);
            blatOutput.cDNAEnd = g.sEnd;
            /*get matching sequences in genomics
             ***********************************/
            boolean isReversed = false;
            Pattern checkReversed = Pattern.compile(REVERSESTRAND);
            while(true){
                sData = input.readLine();
                if (sData == null)
                    break;
                Matcher m2 = checkReversed.matcher(sData);
                if(m2.find())
                    isReversed = true;
                Matcher m = p1.matcher(sData);
                if(m.find())
                    break;
            }
            sData += input.readLine();
            while(true){
                if (sData == null){
                    System.out.println("Format of the Blat Server Web page has changed, please check the code: line 573");
                    break;
                }
                genomicSequence += removeLastNumber(sData);
                Matcher m = p2.matcher(sData);
                if(m.find())
                    break;
                sData = input.readLine();
            }
            if(isReversed){
                blatOutput.genomicSegments = getReverseSegments(genomicSequence, blatOutput.gEnd); 
                int exchange = blatOutput.gStart;
                blatOutput.gStart = blatOutput.gEnd;
                blatOutput.gEnd = exchange;
            }
            else
                blatOutput.genomicSegments = getSegments(genomicSequence, blatOutput.gStart);
              
           /*get matching side by side alignment
            ************************************/
            while(true){
                sData = input.readLine();
                if(sData == null){
                    System.out.println("Format of the Blat Server Web page has changed, please check the code: line 628");
                    break;
                }
                Matcher m = p1.matcher(sData);
                if(m.find())
                    break;
            }
            if(sData.equals(STARTOFRESULTS))
                sData = input.readLine();
            ArrayList SByS = new ArrayList(); 
            while(true){
                int x1 = -1;
                int x2 = -1;
                int x3 = -1;
                int x4 = -1;
                String sUp = null;
                String sDown = null;
                //pass all blank line
                while(sData.equals("")){
                    sData = input.readLine();
                }
                Matcher m = p2.matcher(sData);
                if(m.find())
                    break;
                //process to receive x1, x2 and string sUp: the upper parameters in SBySSegment
                int i = 0;
                while(!java.lang.Character.isDigit(sData.charAt(i))){
                    i++;
                }
                int j = i;
                while(java.lang.Character.isDigit(sData.charAt(j))){
                    j++;
                }      
                x1 = Integer.parseInt(sData.substring(i, j));
                j++;
                while(java.lang.Character.isWhitespace(sData.charAt(j))){
                    j++;
                }
                i = j;
                while(!java.lang.Character.isWhitespace(sData.charAt(i))){
                    i++;
                }
                sUp = sData.substring(j, i);
                while(!java.lang.Character.isDigit(sData.charAt(i))){
                    i++;
                }
                j = i;
                while(j < sData.length() && java.lang.Character.isDigit(sData.charAt(j))){
                    j++;
                }
                x2 = Integer.parseInt(sData.substring(i, j));
                sData = input.readLine();
                sData = input.readLine();
                //process to receive x3, x4 and string sDown: the lower parameters in SBySSegment
                i = 0;
                while(!java.lang.Character.isDigit(sData.charAt(i))){
                    i++;
                }
                j = i;
                while(java.lang.Character.isDigit(sData.charAt(j))){
                    j++;
                }      
                x3 = Integer.parseInt(sData.substring(i, j));
                j++;
                while(java.lang.Character.isWhitespace(sData.charAt(j))){
                    j++;
                }
                i = j;
                while(!java.lang.Character.isWhitespace(sData.charAt(i))){
                    i++;
                }
                sDown = sData.substring(j, i);
                while(!java.lang.Character.isDigit(sData.charAt(i))){
                    i++;
                }
                j = i;
                while(j < sData.length() && java.lang.Character.isDigit(sData.charAt(j))){
                    j++;
                }
                x4 = Integer.parseInt(sData.substring(i, j));
                SBySSegment s = new SBySSegment(x1, x2, x3, x4, sUp, sDown);
                SByS.add(s);
                sData = input.readLine();
            } 
            blatOutput.SBySAlignment = SByS;
        }
        catch (IOException e){  
            System.out.println(e.getMessage());
        }
        return blatOutput;
    }
    /*
     * Extract the genome name from the string input
     */
    public String getGenomeName(String input){
        Pattern p = Pattern.compile("detail");
        Matcher m = p.matcher(input);
        if (m.find()){
            int i = m.end();
            while(!java.lang.Character.isDigit(input.charAt(i))){
                i++;
            }
            int j = i;
            while(!java.lang.Character.isSpaceChar(input.charAt(j))){
                j++;
            }
            return input.substring(i, j);
        }
        return null;
    }
    /**
     * Remove all white space and remove the number at the end of the string input 
     **/
    public String removeLastNumber(String input){
        String sResult = input.replace(" ", "");
        int length = sResult.length();
        int i = length - 1;
        while(i >= 0 && java.lang.Character.isDigit(sResult.charAt(i)))
            i--;
        return sResult.substring(0, i + 1);
    }
    /*
     * Extract from the matching sequence the matching subsequences, their start and end index
     */ 
    public ArrayList getSegments(String input, int offset){
        ArrayList aResult = new ArrayList();
        Pattern p = Pattern.compile(BORDEROFSEQUENCE);
        Matcher m = p.matcher(input);
        int lastMatches = -1;
        int startIndex = -1;
        int endIndex = -1;
        boolean consecutive = false;// true if the end of the previous matching segment 
                                    // and the beginning of the next one is consecutive 
        loop:
        while(true){
            if(!consecutive){
                if(m.find()){
                    startIndex = m.end();
                }
                else{
                    break loop;
                }
            }
            else{
                startIndex = endIndex + 1;
            }
            if(lastMatches == -1)
                lastMatches = offset;
            else
                lastMatches = countCharacters(input, endIndex, startIndex) + lastMatches;
            int sStart = lastMatches;
            m.find();
            endIndex = m.end();
            lastMatches = countCharacters(input, startIndex, endIndex) + lastMatches;
            int sEnd = lastMatches;
            String sValue = BORDEROFSEQUENCE + input.substring(startIndex, endIndex + 1) + "</FONT>";
            GenomeSegment s = new GenomeSegment(sStart, sEnd, sValue);
            aResult.add(s);
            consecutive = !(input.charAt(endIndex + 1) == '<');
        }
        return aResult;
    }
    
    public ArrayList getReverseSegments(String input, int offset){
        ArrayList aResult = new ArrayList();
        Pattern p = Pattern.compile(BORDEROFSEQUENCE);
        Matcher m = p.matcher(input);
        int lastMatches = -1;
        int startIndex = -1;
        int endIndex = -1;
        boolean consecutive = false;
        loop:
        while(true){
            if(!consecutive){
                if(m.find())
                    startIndex = m.end();
                else
                    break loop;
            }
            else
                startIndex = endIndex + 1;
            if(lastMatches == -1)
                lastMatches = offset;
            else
                lastMatches = - countCharacters(input, endIndex, startIndex) + lastMatches;
            int sStart = lastMatches;
            m.find();
            endIndex = m.end();
            lastMatches = - countCharacters(input, startIndex, endIndex) + lastMatches;
            int sEnd = lastMatches;
            String sValue = BORDEROFSEQUENCE + input.substring(startIndex, endIndex + 1) + "</FONT>";
            GenomeSegment s = new GenomeSegment(sStart, sEnd, sValue);
            aResult.add(s);
            consecutive = !(input.charAt(endIndex + 1) == '<');
        }
        return aResult;
    }
    /*
     * Count the number of characters between 2 positions. All special HTML tags
     * won't be counted.    
     */
    public int countCharacters(String input, int startIndex, int endIndex){
        int count = 0;
        int index = startIndex;
        int length = input.length();
        while(index < endIndex){
            if(input.charAt(index) == '<'){//start of special HTML tags, omit it
                index++;
                while(index < length && input.charAt(index) != '>')
                    index++;
                index++;
            }
            else{//character that we want to count
                index++;
                count++;
            }
        }
        return count;
    } 
    
    /*Print the blatOutput structure to an output file. 
     *This is a simple example implemented method
     *Should be modified to make the output more beautiful
     */
    public void printOut(PrintStream ps, BlatOutput blatOutput){
        ps.print("CDNA Sequence ");
        ps.print(blatOutput.cDNA);
        ps.print(" Genomic Sequence ");
        ps.print(blatOutput.genomic);
        if(blatOutput.gStart > blatOutput.gEnd)
            ps.print("(reverse strand)");
        ps.println("<br><br>");
        ps.print(blatOutput.cDNAStart);
        ps.print("----->");
        ps.print(blatOutput.cDNAEnd);
        ps.println("<br>");
        for(int i = 0; i < blatOutput.cDNASegments.size(); i++){
            GenomeSegment s = (GenomeSegment) blatOutput.cDNASegments.get(i);
            ps.print(" Matching Sequence Number ");
            ps.print(i + 1);
            ps.print(":");
            ps.print(s.sStart);
            ps.print("---->");
            ps.print(s.sEnd);
            ps.println("<br>");
            ps.print("Value:");
            ps.print(s.sValue.replaceAll("FONTCOLOR", "FONT COLOR"));
            ps.println("<br>");
        }
        ps.println("<br>");
        ps.print(blatOutput.gStart);
        ps.print("----->");
        ps.print(blatOutput.gEnd);
        ps.println("<br>");
        for(int i = 0; i < blatOutput.genomicSegments.size(); i++){
            GenomeSegment s = (GenomeSegment) blatOutput.genomicSegments.get(i);
            ps.print(" Matching Sequence Number ");
            ps.print(i + 1);
            ps.print(":");
            ps.print(s.sStart);
            ps.print("---->");
            ps.print(s.sEnd);
            ps.println("<br>");
            ps.print("Value:");
            ps.print(s.sValue.replaceAll("FONTCOLOR", "FONT COLOR"));
            ps.println("<br>");
        }
        ps.println("<br>");
        ps.println("---------------------------------------------------" + "<br><br>");
    }
}

class GenomeSegment{
    int sStart;
    int sEnd;
    String sValue;
    public GenomeSegment(int sStart, int sEnd, String sValue) {
        this.sStart = sStart;
        this.sEnd = sEnd;
        this.sValue = sValue;
    }
}

class SBySSegment{
    int x1, x2, x3, x4;
    String sUp, sDown;

    public SBySSegment(int x1, int x2, int x3, int x4, String sUp, String sDown){
        this.x1 = x1;
        this.x2 = x2;
        this.x3 = x3;
        this.x4 = x4;
        this.sUp = sUp;
        this.sDown = sDown;
    }
}

