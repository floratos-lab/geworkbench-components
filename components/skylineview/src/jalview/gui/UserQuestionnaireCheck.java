package jalview.gui;

import java.io.*;
import java.net.*;

import javax.swing.*;

public class UserQuestionnaireCheck
    implements Runnable
{
  /**
   * Implements the client side machinery for detecting a new questionnaire,
   * checking if the user has responded to an existing one,
   * and prompting the user for responding to a questionnaire.
   * This is intended to work with the perl CGI scripts checkresponder.pl and
   * questionnaire.pl
   */
  String url = null;
  UserQuestionnaireCheck(String url)
  {
    if (url.indexOf("questionnaire.pl") == -1)
    {
      jalview.bin.Cache.log.error("'" + url +
          "' is an Invalid URL for the checkForQuestionnaire() method.\n"
                                  + "This argument is only for questionnaires derived from jalview's questionnaire.pl cgi interface.");
    }
    else
    {
      this.url = url;
    }
  }

  String qid = null, rid = null;
  private boolean checkresponse(URL qurl)
      throws Exception
  {
    jalview.bin.Cache.log.debug("Checking Response for : " + qurl);
    boolean prompt = false;
    // see if we have already responsed to this questionnaire or get a new qid/rid pair
    BufferedReader br = new BufferedReader(new InputStreamReader(qurl.
        openStream()));
    String qresp;
    while ( (qresp = br.readLine()) != null)
    {
      if (qresp.indexOf("NOTYET:") == 0)
      {
        prompt = true; // not yet responded under that ID
      }
      else
      {
        if (qresp.indexOf("QUESTIONNAIRE:") == 0)
        {
          // QUESTIONNAIRE:qid:rid for the latest questionnaire.
          int p = qresp.indexOf(':', 14);
          if (p > -1)
          {
            rid = null;
            qid = qresp.substring(14, p);
            if (p < (qresp.length() - 1))
            {
              rid = qresp.substring(p + 1);
              prompt = true; // this is a new qid/rid pair
            }
          }
        }
      }
    }
    return prompt;
  }

  public void run()
  {
    if (url == null)
    {
      return;
    }
    boolean prompt = false;
    try
    {
      // First - check to see if wee have an old questionnaire/response id pair.
      String lastq = jalview.bin.Cache.getProperty("QUESTIONNAIRE");
      if (lastq == null)
      {
        prompt = checkresponse(new URL(url + (url.indexOf('?') > -1 ? "&" : "?") +
                                       "checkresponse=1"));
      }
      else
      {
        String qurl = url + (url.indexOf('?') > -1 ? "&" : "?") +
            "checkresponse=1";
        // query the server with the old qid/id pair
        String qqid = lastq.indexOf(':') > -1 ?
            lastq.substring(0, lastq.indexOf(':')) : null;
        if (qqid != null && qqid != "null" && qqid.length() > 0)
        {
          qurl += "&qid=" + qqid;
          qid = qqid;
          String qrid = lastq.substring(lastq.indexOf(':') + 1); // retrieve old rid
          if (qrid != null && !qrid.equals("null"))
          {
            rid = qrid;
            qurl += "&rid=" + qrid;
          }
        }
        // see if we have already responsed to this questionnaire.
        prompt = checkresponse(new URL(qurl));
      }
      if (qid != null && rid != null)
      {
        // Update our local property cache with latest qid and rid
        jalview.bin.Cache.setProperty("QUESTIONNAIRE", qid + ":" + rid);
      }
      if (prompt)
      {
        String qurl = url + (url.indexOf('?') > -1 ? "&" : "?") + "qid=" + qid +
            "&rid=" + rid;
        jalview.bin.Cache.log.info("Prompting user for questionnaire at " +
                                   qurl);
        int reply = JOptionPane.showInternalConfirmDialog(Desktop.desktop,
            "There is a new Questionnaire available." +
            "Would you like to complete it now ?\n",
            "Jalview User Survey",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (reply == JOptionPane.YES_OPTION)
        {
          jalview.bin.Cache.log.debug("Opening " + qurl);
          jalview.util.BrowserLauncher.openURL(qurl);
        }
      }
    }
    catch (Exception e)
    {
      jalview.bin.Cache.log.warn("When trying to access questionnaire URL " +
                                 url, e);
    }
  }

}
