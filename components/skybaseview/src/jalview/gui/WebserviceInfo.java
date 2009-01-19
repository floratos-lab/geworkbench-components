/*
 * Jalview - A Sequence Alignment Editor and Viewer
 * Copyright (C) 2007 AM Waterhouse, J Procter, G Barton, M Clamp, S Searle
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package jalview.gui;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

import jalview.jbgui.*;

/**
 * Base class for web service client thread and gui
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class WebserviceInfo
    extends GWebserviceInfo
{

  /** Job is Queued */
  public static final int STATE_QUEUING = 0;

  /** Job is Running */
  public static final int STATE_RUNNING = 1;

  /** Job has finished with no errors */
  public static final int STATE_STOPPED_OK = 2;

  /** Job has been cancelled with no errors */
  public static final int STATE_CANCELLED_OK = 3;

  /** job has stopped because of some error */
  public static final int STATE_STOPPED_ERROR = 4;

  /** job has failed because of some unavoidable service interruption */
  public static final int STATE_STOPPED_SERVERERROR = 5;
  int currentStatus = STATE_QUEUING;
  Image image;
  int angle = 0;
  String title = "";
  jalview.ws.WSClientI thisService;
  boolean serviceIsCancellable;
  JInternalFrame frame;
  JTabbedPane subjobs = null;
  java.util.Vector jobPanes = null;
  private boolean serviceCanMergeResults = false;
  private boolean viewResultsImmediatly = true;
  // tabbed or not
  public synchronized int addJobPane()
  {
    JScrollPane jobpane = new JScrollPane();
    JTextArea progressText = new JTextArea();
    progressText.setFont(new java.awt.Font("Verdana", 0, 10));
    progressText.setBorder(null);
    progressText.setEditable(false);
    progressText.setText("WS Job");
    progressText.setLineWrap(true);
    progressText.setWrapStyleWord(true);
    jobpane.setName("JobPane");
    jobpane.getViewport().add(progressText, null);
    jobpane.setBorder(null);
    if (jobPanes == null)
    {
      jobPanes = new Vector();
    }
    int newpane = jobPanes.size();
    jobPanes.add(jobpane);

    if (newpane == 0)
    {
      this.add(jobpane, BorderLayout.CENTER);
    }
    else
    {
      if (newpane == 1)
      {
        // revert to a tabbed pane.
        JScrollPane firstpane;
        this.remove(firstpane = (JScrollPane) jobPanes.get(0));
        subjobs = new JTabbedPane();
        this.add(subjobs, BorderLayout.CENTER);
        subjobs.add(firstpane);
        subjobs.setTitleAt(0, firstpane.getName());
      }
      subjobs.add(jobpane);
    }
    return newpane; // index for accessor methods below
  }

  /**
   * Creates a new WebserviceInfo object.
   *
   * @param title short name and job type
   * @param info reference or other human readable description
   */
  public WebserviceInfo(String title, String info)
  {
    init(title, info, 520, 500);
  }

  /**
   * Creates a new WebserviceInfo object.
   *
   * @param title DOCUMENT ME!
   * @param info DOCUMENT ME!
   * @param width DOCUMENT ME!
   * @param height DOCUMENT ME!
   */
  public WebserviceInfo(String title, String info, int width, int height)
  {
    init(title, info, width, height);
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public jalview.ws.WSClientI getthisService()
  {
    return thisService;
  }

  /**
   * DOCUMENT ME!
   *
   * @param newservice DOCUMENT ME!
   */
  public void setthisService(jalview.ws.WSClientI newservice)
  {
    thisService = newservice;
    serviceIsCancellable = newservice.isCancellable();
    frame.setClosable(!serviceIsCancellable);
    serviceCanMergeResults = newservice.canMergeResults();
  }

  /**
   * DOCUMENT ME!
   *
   * @param title DOCUMENT ME!
   * @param info DOCUMENT ME!
   * @param width DOCUMENT ME!
   * @param height DOCUMENT ME!
   */
  void init(String title, String info, int width, int height)
  {
    frame = new JInternalFrame();
    frame.setContentPane(this);
    Desktop.addInternalFrame(frame, title, width, height);
    frame.setClosable(false);

    this.title = title;
    setInfoText(info);

    java.net.URL url = getClass().getResource("/images/logo.gif");
    image = java.awt.Toolkit.getDefaultToolkit().createImage(url);

    MediaTracker mt = new MediaTracker(this);
    mt.addImage(image, 0);

    try
    {
      mt.waitForID(0);
    }
    catch (Exception ex)
    {
    }

    AnimatedPanel ap = new AnimatedPanel();
    titlePanel.add(ap, BorderLayout.CENTER);

    Thread thread = new Thread(ap);
    thread.start();
  }

  /**
   * DOCUMENT ME!
   *
   * @param status integer status from state constants
   */
  public void setStatus(int status)
  {
    currentStatus = status;
  }

  /**
   * subjob status indicator
   * @param jobpane
   * @param status
   */
  public void setStatus(int jobpane, int status)
  {
    if (jobpane < 0 || jobpane >= jobPanes.size())
    {
      throw new Error("setStatus called for non-existent job pane." + jobpane);
    }
    switch (status)
    {
      case STATE_QUEUING:
        setProgressName(jobpane + " - QUEUED", jobpane);
        break;
      case STATE_RUNNING:
        setProgressName(jobpane + " - RUNNING", jobpane);
        break;
      case STATE_STOPPED_OK:
        setProgressName(jobpane + " - FINISHED", jobpane);
        break;
      case STATE_CANCELLED_OK:
        setProgressName(jobpane + " - CANCELLED", jobpane);
        break;
      case STATE_STOPPED_ERROR:
        setProgressName(jobpane + " - BROKEN", jobpane);
        break;
      case STATE_STOPPED_SERVERERROR:
        setProgressName(jobpane + " - ALERT", jobpane);
        break;
      default:
        setProgressName(jobpane + " - UNKNOWN STATE", jobpane);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getInfoText()
  {
    return infoText.getText();
  }

  /**
   * DOCUMENT ME!
   *
   * @param text DOCUMENT ME!
   */
  public void setInfoText(String text)
  {
    infoText.setText(text);
  }

  /**
   * DOCUMENT ME!
   *
   * @param text DOCUMENT ME!
   */
  public void appendInfoText(String text)
  {
    infoText.append(text);
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getProgressText(int which)
  {
    if (jobPanes == null)
    {
      addJobPane();
    }
    return ( (JTextArea) ( (JScrollPane) jobPanes.get(which)).getViewport().
            getComponent(0)).getText();
  }

  /**
   * DOCUMENT ME!
   *
   * @param text DOCUMENT ME!
   */
  public void setProgressText(int which, String text)
  {
    if (jobPanes == null)
    {
      addJobPane();
    }
    ( (JTextArea) ( (JScrollPane) jobPanes.get(which)).getViewport().
     getComponent(0)).setText(text);
  }

  /**
   * DOCUMENT ME!
   *
   * @param text DOCUMENT ME!
   */
  public void appendProgressText(int which, String text)
  {
    if (jobPanes == null)
    {
      addJobPane();
    }
    ( (JTextArea) ( (JScrollPane) jobPanes.get(which)).getViewport().
     getComponent(0)).append(text);
  }

  /**
   * setProgressText(0, text)
   */
  public void setProgressText(String text)
  {
    setProgressText(0, text);
  }

  /**
   * appendProgressText(0, text)
   */
  public void appendProgressText(String text)
  {
    appendProgressText(0, text);
  }

  /**
   * getProgressText(0)
   */
  public String getProgressText()
  {
    return getProgressText(0);
  }

  /**
   * get the tab title for a subjob
   * @param which int
   * @return String
   */
  public String getProgressName(int which)
  {
    if (jobPanes == null)
    {
      addJobPane();
    }
    if (subjobs != null)
    {
      return subjobs.getTitleAt(which);
    }
    else
    {
      return ( (JScrollPane) jobPanes.get(which)).getViewport().getComponent(0).
          getName();
    }
  }

  /**
   * set the tab title for a subjob
   * @param name String
   * @param which int
   */
  public void setProgressName(String name, int which)
  {
    if (subjobs != null)
    {
      subjobs.setTitleAt(which, name);
      subjobs.revalidate();
      subjobs.repaint();
    }
    JScrollPane c = (JScrollPane) jobPanes.get(which);
    c.getViewport().getComponent(0).setName(name);
    c.repaint();
  }

  /**
   * Gui action for cancelling the current job, if possible.
   *
   * @param e DOCUMENT ME!
   */
  protected void cancel_actionPerformed(ActionEvent e)
  {
    if (!serviceIsCancellable)
    {
      JOptionPane.showInternalMessageDialog(Desktop.desktop,
                                            "This job cannot be cancelled.\nJust close the window.",
                                            "Cancel job",
                                            JOptionPane.WARNING_MESSAGE);
    }
    else
    {
      thisService.cancelJob();
    }
    frame.setClosable(true);
  }

  /**
   * Set up GUI for user to get at results - and possibly automatically display
   * them if viewResultsImmediatly is set.
   */
  public void setResultsReady()
  {
    frame.setClosable(true);
    buttonPanel.remove(cancel);
    buttonPanel.add(showResultsNewFrame);
    if (serviceCanMergeResults)
    {
      buttonPanel.add(mergeResults);
      buttonPanel.setLayout(new GridLayout(2, 1, 5, 5));
    }
    buttonPanel.validate();
    validate();
    if (viewResultsImmediatly)
    {
      showResultsNewFrame.doClick();
    }
  }

  /**
   * called when job has finished but no result objects can be passed back to user
   */
  public void setFinishedNoResults()
  {
    frame.setClosable(true);
    buttonPanel.remove(cancel);
    buttonPanel.validate();
    validate();
  }

  class AnimatedPanel
      extends JPanel implements Runnable
  {
    long startTime = 0;
    BufferedImage offscreen;

    public void run()
    {
      startTime = System.currentTimeMillis();

      while (currentStatus < STATE_STOPPED_OK)
      {
        try
        {
          Thread.sleep(50);

          int units = (int) ( (System.currentTimeMillis() - startTime) /
                             10f);
          angle += units;
          angle %= 360;
          startTime = System.currentTimeMillis();

          if (currentStatus >= STATE_STOPPED_OK)
          {
            angle = 0;
          }

          repaint();
        }
        catch (Exception ex)
        {
        }
      }

      cancel.setEnabled(false);
    }

    void drawPanel()
    {
      if (offscreen == null || offscreen.getWidth(this) != getWidth()
          || offscreen.getHeight(this) != getHeight())
      {
        offscreen = new BufferedImage(getWidth(), getHeight(),
                                      BufferedImage.TYPE_INT_ARGB);
      }

      Graphics2D g = (Graphics2D) offscreen.getGraphics();

      g.setColor(Color.white);
      g.fillRect(0, 0, getWidth(), getHeight());

      g.setFont(new Font("Arial", Font.BOLD, 12));
      g.setColor(Color.black);

      switch (currentStatus)
      {
        case STATE_QUEUING:
          g.drawString(title.concat(" - queuing"), 60, 30);

          break;

        case STATE_RUNNING:
          g.drawString(title.concat(" - running"), 60, 30);

          break;

        case STATE_STOPPED_OK:
          g.drawString(title.concat(" - complete"), 60, 30);

          break;

        case STATE_CANCELLED_OK:
          g.drawString(title.concat(" - job cancelled!"), 60, 30);

          break;

        case STATE_STOPPED_ERROR:
          g.drawString(title.concat(" - job error!"), 60, 30);

          break;

        case STATE_STOPPED_SERVERERROR:
          g.drawString(title.concat(" - Server Error! (try later)"),
                       60,
                       30);

          break;
      }

      if (image != null)
      {
        g.rotate(Math.toRadians(angle), 28, 28);
        g.drawImage(image, 10, 10, this);
        g.rotate( -Math.toRadians(angle), 28, 28);
      }
    }

    public void paintComponent(Graphics g1)
    {
      drawPanel();

      g1.drawImage(offscreen, 0, 0, this);
    }
  }
}
