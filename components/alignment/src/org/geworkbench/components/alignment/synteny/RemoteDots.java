package org.geworkbench.components.alignment.synteny;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * RemoteDots is a class that implements submission of a URL of two sequences to
 * the Dots program and retrieval of  results.  It
 * writes retrieved results out to a file.
 */
public class RemoteDots {

  /**
   * The sequence information string to submit.
   */
  private String queryX;

  /**
   * The sequence information string to submit.
   */
  private String queryY;

  /**
   * The default file name to write results out to.
   */
  private final String DEFAULT_FILENAME = "C:\\dots\\test.out";

  /**
   * The file name to write results out to.
   */
  public static String filename = "C:\\dots\\test.out";

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
  private final String Dots_SERVER = "www.ncbi.nlm.nih.gov";

  /**
   * A flag indicating whether Blast results have been retrieve.
   * <code>true</code if Blast is done, <code>false</code if not.
   */
  private boolean getDotsDone = false;

  /**
   * Regular expression to parse out CDD RID# with.
   */
  private Pattern p1 = Pattern.compile("RID=([0-9]+-[0-9]+-[0-9]+)");

  /**
   * Regular expression to parse out negative CDD Search results with.
   */
  private Pattern p2 = Pattern.compile("Error ...");

  /**
   * Creates a new RemoteBlast and sets query and textArea to the specified
   * String and JTextArea values.  Also sets filename to the default file
   * name.
   *
   * @param		the String value to set query with.
   * @param 		the JTextArea to set textArea with.
   */
  public RemoteDots(SyntenyQueryObj x_query, SyntenyQueryObj y_query,
                    JTextArea textArea) {
    this.queryX = x_query.getNameX();
    this.queryY = y_query.getNameY();
    this.filename = DEFAULT_FILENAME;
    this.textArea = textArea;
    this.CDD_rid = null;
  }

  /**
   * Creates a new RemoteBlast and sets query and textArea to the specified
   * String and JTextArea values.  Also sets filename to the default file
   * name.
   *
   * @param		the String value to set query with.
   * @param 		the JTextArea to set textArea with.
   */
  public RemoteDots(String fx, int fromX, int toX, String fy, int fromY,
                    int toY) {
    String cmdl = "c:\\dots\\dots"+" \""+fx+"\" "+Integer.toString(fromX)+" "
        +Integer.toString(toX)+" \""+fy+"\" "+Integer.toString(fromY)+" "
        +Integer.toString(toY)+" "+filename+" 5 20 5 1 1";
        Socket s = null;

    /* Run Dots locally */
    try {
      System.out.println(cmdl);
      Runtime.getRuntime().exec(cmdl);
    }
    catch (Exception e) {
      System.out.println("Socket:" + e.getMessage());
    }
  }

  /**
   * Creates a new RemoteBlast and sets query, filenmae, and textArea to the
   * specified String and JTextArea values.
   *
   * @param 		the String value to set query with.
   * @param 		the String value to set filename with.
   * @param 		the JTextArea to set textArea with.
   */
  public RemoteDots(String x_query, String y_query, String filename,
                    JTextArea textArea) {
    this.queryX = x_query;
    this.queryY = y_query;
    this.filename = filename;
    this.textArea = textArea;
    this.CDD_rid = null;
  }

  /**
   * Creates a new RemoteBlast and sets query, filenmae, and textArea to the
   * specified String and JTextArea values.
   *
   * @param 		the String value to set query with.
   * @param 		the String value to set filename with.
   * @param 		the JTextArea to set textArea with.
   */
  public RemoteDots() {
    submitDots();
  }

  /**
   * Returns <code>true</code> if Blast is done, <code>false</code> if not.
   *
   * @return 		getBlastDone - boolean that indicates if Blast is done.
   */
  public boolean getDotsDone() {
    return getDotsDone;
  }

  /**
   * Creates a socket connection to the NCBI Blast server and submits an
   * HTTP request to Blast with the query and parses out the Blast RID #.
   * Also initiates a Conserved Domain Search and parses out the resulting
   * CDD RID # used for retrieval of CDD Search results if domains found.
   *
   * @return 		a String representing the Blast RID # used to retrieve
   * 				Blast results, <code>null</code> if not successful.
   */
  public String submitDots() {

    String message; /* HTTP GET message */
    String cmdl = "c:\\dots\\dots c:\\work\\dots\\test1.fa 1 500 c:\\Program Files\\dots\\dots\\test2.fa 1 500 c:\\Program Files\\dots\\test.out 5 20 5 1 1";
    Socket s = null;
    message =
        "Put http://blast.ncbi.nlm.nih.gov/Blast.cgi?CMD=Put&QUERY="
        + queryX
        + queryX
        + "\n";

    /* Run Dots locally */

    try {
      Runtime.getRuntime().exec(cmdl);
    }
    catch (Exception e) {
      System.out.println("Socket:" + e.getMessage());
    }

    /*                try {
                            s = new Socket(Dots_SERVER, DEFAULT_PORT);

                            //create an output stream for sending message.
     DataOutputStream out = new DataOutputStream(s.getOutputStream());

                            //create buffered reader stream for reading incoming byte stream.
     InputStreamReader inBytes = new InputStreamReader(s.getInputStream());
                            BufferedReader in = new BufferedReader(inBytes);

     textArea.append("\n\nSending message: " + message + "\n");

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
     System.out.println("No.putative.conserved.domains.have.been.detected");
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
     */
    return null;
  }
  /* end of submitDots method */

  /**
   * Sets getBlastDone to <code>false</code> indicating Blast is not done yet
   * and creates a new GetBlast with the specified String as a parameter.
   *
   * @param 		rid - String representing the Blast RID# to retrieve
   * 				results for.
   */

  public void getDots(String rid) {
    getDotsDone = false;
    GetDots bl = new GetDots(rid);
  }

  /**
   * This class is a Thread that retrieves results by Blast RID#, which
   * can take some period of time.  The thread continually requests results
   * from the NCBI Blast server with the Blast RID# via an HTTP request using
   * a Socket and writes results out to file.
   */
  private class GetDots
      extends Thread {

    String message;
    String file;
    Socket s;

    public GetDots(String Dots_rid) {
      s = null;
      //this.file = file;
      message =
          "Get http://blast.ncbi.nlm.nih.gov/Blast.cgi?CMD=Get&FORMAT_TYPE=Text&RID="
          + Dots_rid
          + "\r\n\r\n";
      this.start();
    }

    public void run() {

      try {
        //create an output stream for writing to a file.
        PrintStream ps = new PrintStream(new FileOutputStream(new File(filename)));

        boolean BlastnotDone = false;
        while (!BlastnotDone) {

          s = new Socket(Dots_SERVER, DEFAULT_PORT);

          //create an output stream for sending message.
          DataOutputStream out = new DataOutputStream(s.getOutputStream());

          //create buffered reader stream for reading incoming byte stream.
          InputStreamReader inBytes = new InputStreamReader(s.getInputStream());
          BufferedReader in = new BufferedReader(inBytes);

          textArea.append(message + "\n");

          //write String message to output stream as byte sequence.
          out.writeBytes(message);

          String data = in.readLine();

          boolean done = false;
          while (data != null) {
            if (data.equals("\tStatus=WAITING")) {
              done = false;
            }
            else if (data.equals("\tStatus=READY")) {
              BlastnotDone = true;
              done = true;
              ps.println(data);
            }
            else {
              if (done) {
                ps.println(data);
              }
            }
            data = in.readLine();
          }
          if (!done) {
            textArea.append("Waiting for Blast to finish...\n");
            this.sleep(20000);
          }
          s.close();
        } //end of while (BlastnotDone).
        textArea.append("Blast done! You can now display your results");
        getDotsDone = true;
        ps.close();
      }
      catch (UnknownHostException e) {
        System.out.println("Socket:" + e.getMessage());
      }
      catch (EOFException e) {
        System.out.println("EOF:" + e.getMessage());
      }
      catch (IOException e) {
        System.out.println("readline:" + e.getMessage());
      }
      catch (InterruptedException e) {
        System.out.println("wait:" + e.getMessage());
      }
    } //end of run().
  } //end of class GetBlast.

  /*public static void main(String[] args) {
          RemoteBlast test = new RemoteBlast("MGARCPTRTLRARQPAHPRPPGTPRHHQRRPLPAASPTRHRSSRGRQIRARRPDRPGTRLRTGAAVDRQQPQHAPLRPLRLRSARADPRPQPGKPARRNPGHQRPRPRCRRPGAQQRPADRTLPADRSARHRVPAAPPAARPAHRRARQRRLRPARRPARPAGTRPLHDSRTRPAQLSGAADLRSDRRPATDRDHRQRRSPLLPAPCRRHHRTRRPPLPARIPPPVHSAAPHPPQQRGTRGNRGRPLLREPAQRHPSSRRRLRAPHRGRLPPGYRPAPQRGLPDHGHRRQDQRTHPRVPAGARGNGVAPTHGHPRMTSRRSHETPQGPDPRSPGAAPAYREAPPALTGRE");
          String Blast_rid = test.submitBlast();
          test.getBlast(Blast_rid);
           }*/

}
/* end of public class RemoteBlast */
