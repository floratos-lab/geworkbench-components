/**
 *
 */
package jalview.gui;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Vector;
import java.util.jar.JarOutputStream;

import javax.swing.JInternalFrame;

import jalview.bin.Cache;
import jalview.io.VamsasDatastore;

import org.vamsas.client.UserHandle;
import org.vamsas.client.simpleclient.FileWatcher;
import org.vamsas.client.simpleclient.VamsasArchive;
import org.vamsas.client.simpleclient.VamsasFile;
import org.vamsas.objects.core.Entry;
import org.vamsas.objects.core.VamsasDocument;
import org.vamsas.test.simpleclient.ArchiveClient;
import org.vamsas.test.simpleclient.ClientDoc;

/**
 * @author jimp
 *
 */
public class VamsasClient
    extends ArchiveClient
{
  // Cache.preferences for vamsas client session arena
  // preferences for check for default session at startup.
  // user and organisation stuff.
  public VamsasClient(Desktop jdesktop,
                      File sessionPath)
  {
    super(System.getProperty("user.name"), System.getProperty("host.name"),
          "jalview", "2.7",
          sessionPath);
  }

  public void initial_update()
  {
    Cache.log.info("Jalview loading the Vamsas Session.");
    // load in the vamsas archive for the first time
    ClientDoc cdoc = this.getUpdateable();
    updateJalview(cdoc);
    JInternalFrame[] frames = Desktop.desktop.getAllFrames();

    if (frames == null)
    {
      return;
    }

    try
    {
      //REVERSE ORDER
      for (int i = frames.length - 1; i > -1; i--)
      {
        if (frames[i] instanceof AlignFrame)
        {
          AlignFrame af = (AlignFrame) frames[i];
          af.alignPanel.alignmentChanged();
        }
      }
    }
    catch (Exception e)
    {
      Cache.log.warn(
          "Exception whilst refreshing jalview windows after a vamsas document update.",
          e);
    }
    doUpdate(cdoc);
    cdoc.closeDoc();
  }

  /**
   * this will close all windows currently in Jalview.
   *

    protected void closeWindows() {
     JInternalFrame[] frames = Desktop.desktop.getAllFrames();

        if (frames == null)
        {
            return;
        }

        try
        {
            for (int i = frames.length - 1; i > -1; i--) {
             frames[i].dispose();
            }
        } catch (Exception e) {
         Cache.log.error("Whilst closing windows",e);
        }

    }

    public void get_update(VamsasArchive doc) {
     // Close windows - load update.
     Cache.log.info("Jalview updating from Vamsas Session.");
    }
   */
  VamsasClientWatcher watcher = null;
  public void push_update()
  {
    watchForChange = false;
    try
    {
      Thread.sleep(WATCH_SLEEP);
    }
    catch (Exception e)
    {

    }
    ;
    ClientDoc cdoc = getUpdateable();
    updateVamsasDocument(cdoc);
    doUpdate(cdoc);
    cdoc.closeDoc();
    cdoc = null;
    watchForChange = true;
    if (watcher != null)
    {
      watcher.start();
    }
    // collect all uncached alignments and put them into the vamsas dataset.
    // store them.
    Cache.log.info("Jalview updating the Vamsas Session.");
  }

  public void end_session()
  {
    //   stop any update/watcher thread.
    watchForChange = false; // this makes any watch(long) loops return.
    // we should also wait arount for this.WATCH_SLEEP to really make sure the watcher thread has stopped.
    try
    {
      Thread.sleep(WATCH_SLEEP);
    }
    catch (Exception e)
    {

    }
    ;
    Cache.log.info("Jalview disconnecting from the Vamsas Session.");
  }

  public void updateJalview(ClientDoc cdoc)
  {
    ensureJvVamsas();
    VamsasDatastore vds = new VamsasDatastore(cdoc, vobj2jv, jv2vobj,
                                              baseProvEntry());
    vds.updateToJalview();
  }

  private void ensureJvVamsas()
  {
    if (jv2vobj == null)
    {
      jv2vobj = new IdentityHashMap();
      vobj2jv = new Hashtable();
    }
  }

  /**
   * jalview object binding to VorbaIds
   */
  IdentityHashMap jv2vobj = null;
  Hashtable vobj2jv = null;
  public void updateVamsasDocument(ClientDoc doc)
  {
    ensureJvVamsas();
    VamsasDatastore vds = new VamsasDatastore(doc, vobj2jv, jv2vobj,
                                              baseProvEntry());
    // wander through frames
    JInternalFrame[] frames = Desktop.desktop.getAllFrames();

    if (frames == null)
    {
      return;
    }

    try
    {
      //REVERSE ORDER
      for (int i = frames.length - 1; i > -1; i--)
      {
        if (frames[i] instanceof AlignFrame)
        {
          AlignFrame af = (AlignFrame) frames[i];

          // update alignment and root from frame.
          vds.storeVAMSAS(af.getViewport(), af.getTitle());
        }
      }
    }
    catch (Exception e)
    {
      Cache.log.error("Vamsas Document store exception", e);
    }
  }

  private Entry baseProvEntry()
  {
    org.vamsas.objects.core.Entry pentry = new org.vamsas.objects.core.Entry();
    pentry.setUser(this.getProvenanceUser());
    pentry.setApp(this.getClientHandle().getClientName());
    pentry.setDate(new org.exolab.castor.types.Date(new java.util.Date()));
    pentry.setAction("created");
    return pentry;
  }

  protected class VamsasClientWatcher
      extends Thread
  {
    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    VamsasClient client = null;
    VamsasClientWatcher(VamsasClient client)
    {
      this.client = client;
    }

    boolean running = false;
    public void run()
    {
      running = true;
      while (client.watchForChange)
      {
        ClientDoc docio = client.watch(0);
        if (docio != null)
        {
          client.disableGui(true);
          Cache.log.debug("Updating jalview from changed vamsas document.");
          client.updateJalview(docio);
          Cache.log.debug("Finished updating from document change.");
          docio.closeDoc();
          docio = null;
          client.disableGui(false);
        }
      }
      running = false;

    }

  }

  public void disableGui(boolean b)
  {
    Desktop.instance.setVamsasUpdate(b);
  }

  public void startWatcher()
  {
    if (watcher == null)
    {
      watcher = new VamsasClientWatcher(this);
    }
    Thread thr = new Thread()
    {
      public void run()
      {
        watcher.start();
      }
    };
    thr.start();
  }
}
