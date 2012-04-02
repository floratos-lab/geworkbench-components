package org.geworkbench.components.medusa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.medusa.MedusaDataSet;
import org.geworkbench.bison.datastructure.properties.DSNamed;
import org.geworkbench.builtin.projects.Icons;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.FilePathnameUtils;

import com.ice.tar.tar;

/**
 * The visual component for MEDUSA. When receiving a project event, the
 * {@link MedusaVisualizationPanel} is created and added.
 * 
 * @author keshav
 * @version $Id: MedusaVisualComponent.java,v 1.10 2007/07/10 17:24:34 keshav
 *          Exp $
 */
@AcceptTypes(MedusaDataSet.class)
public class MedusaVisualComponent implements VisualPlugin {
	private Log log = LogFactory.getLog(this.getClass());

	private MedusaDataSet dataSet;

	private JPanel component;

	private MedusaVisualizationPanel medusaVisualizationPanel;

	public static final int IMAGE_HEIGHT = 300;
	public static final int IMAGE_WIDTH = 675;
	private static final String outdir = FilePathnameUtils.getTemporaryFilesDirectoryPath()+"temp/medusa/dataset/output/";

	/**
	 * 
	 * 
	 */
	public MedusaVisualComponent() {
		component = new JPanel(new BorderLayout());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.engine.config.VisualPlugin#getComponent()
	 */
	public Component getComponent() {
		return component;
	}

	private boolean handleGridOutput(String tarstr){
		log.info(dataSet.getFilename()+" received length: "+tarstr.length());

		File dirPath = new File(outdir);
		if (!dirPath.exists())  dirPath.mkdirs();
		
		String tardir = outdir+dataSet.getFilename()+".tar";
		try{
			decodeStringToFile(tarstr, tardir);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}

		String pwd = System.getProperty("user.dir");
		System.setProperty("user.dir", FilePathnameUtils.getTemporaryFilesDirectoryPath());

		tar app = new tar(); 
		String tarArgs[] = {"-xf", tardir};
		app.instanceMain(tarArgs);

		System.setProperty("user.dir", pwd);
		
		File delfile = new File(tardir);
		if (delfile.exists())
		{
			System.gc();
			boolean ret = delfile.delete();
			if (!ret) delfile.deleteOnExit();
			log.info(ret+ " delete "+ delfile.getPath());
		}

		String runpath = dataSet.getFilename();
		dataSet.setOuputPath(runpath);
		return true;
	}

    private OutputWorker outputWorker = null;
	private class OutputWorker extends SwingWorker<MedusaVisualizationPanel, Void> {
		MedusaVisualizationPanel medusaVisualizationPanel = null;

        @Override
        protected MedusaVisualizationPanel doInBackground() {
            String tarstr = dataSet.getOutputPath();
			// result returned from grid service
			if (dataSet.getFilename() != null && dataSet.getFilename() != tarstr) {
				if (isCancelled() || !handleGridOutput(tarstr)) return null;
			}
			if (isCancelled()) return null;
			medusaVisualizationPanel = new MedusaVisualizationPanel(MedusaVisualComponent.this,
					dataSet.getData(), dataSet.getOutputPath());
            return medusaVisualizationPanel;
        }

        @Override
        public void done() {
        	if (isCancelled()) return;
        	try{
        		medusaVisualizationPanel = get();
        	}catch(ExecutionException e){
    			e.printStackTrace();
    		}catch(InterruptedException e){
    			e.printStackTrace();
    		}
    		if (medusaVisualizationPanel == null)
    			return;
            component.removeAll();
            component.add(medusaVisualizationPanel, BorderLayout.CENTER);
            component.revalidate();
            component.repaint();
        }
	}

    private void setBusy(){
		component.setBackground(Color.white);
		JLabel busyGraphic = new JLabel(new ImageIcon(Icons.class.getResource("busy.gif")));
        busyGraphic.setAlignmentX(Component.CENTER_ALIGNMENT);
        component.add(busyGraphic, BorderLayout.CENTER);
        component.revalidate();
        component.repaint();	
	}

	/**
	 * 
	 * @param projectEvent
	 * @param source
	 */
	@Subscribe
	public void receive(ProjectEvent projectEvent, Object source) {
		log.debug("MEDUSA received project event.");
		DSDataSet<?> data = projectEvent.getDataSet();
		if ((data != null) && (data instanceof MedusaDataSet)) {
			if (dataSet != data) {
				dataSet = ((MedusaDataSet) data);
				component.removeAll();

				if (SwingUtilities.isEventDispatchThread())
					setBusy();
				else try{
					SwingUtilities.invokeAndWait(new Runnable(){
						public void run(){
							setBusy();
					}});
				}catch(InterruptedException e){
					e.printStackTrace();
				}catch(InvocationTargetException e){
					e.printStackTrace();
				}

				if (outputWorker != null && !outputWorker.isDone())
				{
					outputWorker.cancel(true);
					outputWorker = null;
				}
				outputWorker = new OutputWorker();
		        outputWorker.execute();
			}
		}
	}

	/**
	 * Publish a subpanel changed event. An example is creating a selection set
	 * when an "Add to set" button is clicked.
	 * 
	 * @param event
	 * @return SubpanelChangedEvent
	 */
	@Publish
	public SubpanelChangedEvent<? extends DSNamed> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<? extends DSNamed> event) {
		return event;
	}

	/**
	 * Publish a snapshot. When taking an image snapshot, the {@link Image} is
	 * first created from the {@link JComponent} of interest. The
	 * {@link Graphics} context is then retrieved from the {@link Image} to
	 * allow off-screen painting to occur.
	 * 
	 * @return {@link ImageSnapshotEvent}
	 */
	@Publish
	public org.geworkbench.events.ImageSnapshotEvent publishScreenSnapshot() {
		Image image = null;
		try {
			/* set up the image width, height, and type */
			image = new BufferedImage(medusaVisualizationPanel.getWidth(),
					medusaVisualizationPanel.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			/*
			 * get the Graphics context from the image so we can paint it off
			 * screen
			 */
			Graphics g = image.getGraphics();
			medusaVisualizationPanel.paint(g);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		ImageIcon icon = new ImageIcon(image, "Medusa");
		org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent(
				"Medusa Snapshot", icon,
				org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
		return event;
	}

	/**
	 * Publish a snapshot. When taking an image snapshot, the {@link Image} is
	 * first created from the {@link JComponent} of interest. The
	 * {@link Graphics} context is then retrieved from the {@link Image} to
	 * allow off-screen painting to occur.
	 * 
	 * @return {@link ImageSnapshotEvent}
	 */
	@Publish
	public org.geworkbench.events.ImageSnapshotEvent publishImageSnapshot() {
		Image image = null;
		try {
			/* set up the image width, height, and type */
			JViewport viewPort0=((JViewport)((JScrollPane)((JSplitPane)((JSplitPane)((JSplitPane)((JPanel)((JTabbedPane)medusaVisualizationPanel.getComponent(0)).getComponent(0)).getComponent(0)).getComponent(1)).getComponent(1)).getComponent(1)).getComponent(0));
			JViewport viewPort1=((JViewport)((JScrollPane)((JSplitPane)((JSplitPane)((JSplitPane)((JPanel)((JTabbedPane)medusaVisualizationPanel.getComponent(0)).getComponent(0)).getComponent(0)).getComponent(1)).getComponent(1)).getComponent(2)).getComponent(0));			
			JViewport viewPort2=((JViewport)((JScrollPane)((JSplitPane)((JSplitPane)((JPanel)((JTabbedPane)medusaVisualizationPanel.getComponent(0)).getComponent(0)).getComponent(0)).getComponent(1)).getComponent(2)).getComponent(0));
			JViewport viewPort3=((JViewport)((JScrollPane)((JSplitPane)((JSplitPane)((JSplitPane)((JPanel)((JTabbedPane)medusaVisualizationPanel.getComponent(0)).getComponent(0)).getComponent(0)).getComponent(2)).getComponent(1)).getComponent(1)).getComponent(0));
			JViewport viewPort4=((JViewport)((JScrollPane)((JSplitPane)((JSplitPane)((JSplitPane)((JPanel)((JTabbedPane)medusaVisualizationPanel.getComponent(0)).getComponent(0)).getComponent(0)).getComponent(2)).getComponent(1)).getComponent(2)).getComponent(0));			
			JViewport viewPort5=((JViewport)((JScrollPane)((JSplitPane)((JSplitPane)((JPanel)((JTabbedPane)medusaVisualizationPanel.getComponent(0)).getComponent(0)).getComponent(0)).getComponent(2)).getComponent(2)).getComponent(0));
			image = new BufferedImage(viewPort3.getComponent(0).getWidth()+viewPort4.getComponent(0).getWidth()+viewPort5.getComponent(0).getWidth(),
					viewPort1.getComponent(0).getHeight()+viewPort4.getComponent(0).getHeight()+15,
					BufferedImage.TYPE_INT_RGB);
			
			/*
			 * get the Graphics context from the image so we can paint it off
			 * screen
			 */
			
			
			Graphics g = image.getGraphics();
			Color tempColor=g.getColor();
			g.setColor(this.getComponent().getBackground());
			//g.fillRect(0, 0, viewPort3.getComponent(0).getWidth()+viewPort4.getComponent(0).getWidth()+viewPort5.getComponent(0).getWidth(), viewPort1.getComponent(0).getHeight()+viewPort4.getComponent(0).getHeight());
			//for speed, above line changed to following line.
			g.fillRect(0, 0, viewPort3.getComponent(0).getWidth()+viewPort4.getComponent(0).getWidth()+viewPort5.getComponent(0).getWidth(), viewPort1.getComponent(0).getHeight()+15);
			g.setColor(tempColor);
			
			Image bufImage0 = new BufferedImage(viewPort0.getComponent(0).getWidth(), viewPort0.getComponent(0).getHeight(), BufferedImage.TYPE_INT_RGB);
			viewPort0.paintComponents(bufImage0.getGraphics());
			g.drawImage(bufImage0,0,0,viewPort0.getComponent(0).getWidth(),viewPort0.getComponent(0).getHeight(),this.getComponent());

			Image bufImage1 = new BufferedImage(viewPort1.getComponent(0).getWidth(), viewPort1.getComponent(0).getHeight(), BufferedImage.TYPE_INT_RGB);
			viewPort1.paintComponents(bufImage1.getGraphics());
			g.drawImage(bufImage1,viewPort0.getComponent(0).getWidth(),0,viewPort1.getComponent(0).getWidth(),viewPort1.getComponent(0).getHeight(),this.getComponent());

			Image bufImage2 = new BufferedImage(viewPort2.getComponent(0).getWidth(), viewPort2.getComponent(0).getHeight(), BufferedImage.TYPE_INT_RGB);
			viewPort2.paintComponents(bufImage2.getGraphics());
			g.drawImage(bufImage2,viewPort0.getComponent(0).getWidth()+viewPort1.getComponent(0).getWidth(),0,viewPort2.getComponent(0).getWidth(),viewPort2.getComponent(0).getHeight(),this.getComponent());

			Image bufImage3 = new BufferedImage(viewPort3.getComponent(0).getWidth(), viewPort3.getComponent(0).getHeight(), BufferedImage.TYPE_INT_RGB);
			viewPort3.paintComponents(bufImage3.getGraphics());
			g.drawImage(bufImage3,0,viewPort1.getComponent(0).getHeight()+15,viewPort3.getComponent(0).getWidth(),viewPort3.getComponent(0).getHeight(),this.getComponent());
			
			Image bufImage4 = new BufferedImage(viewPort4.getComponent(0).getWidth(), viewPort4.getComponent(0).getHeight(), BufferedImage.TYPE_INT_RGB);
			viewPort4.paintComponents(bufImage4.getGraphics());
			g.drawImage(bufImage4,viewPort3.getComponent(0).getWidth(),viewPort1.getComponent(0).getHeight()+15,viewPort4.getComponent(0).getWidth(),viewPort4.getComponent(0).getHeight(),this.getComponent());

			Image bufImage5 = new BufferedImage(viewPort5.getComponent(0).getWidth(), viewPort5.getComponent(0).getHeight(), BufferedImage.TYPE_INT_RGB);
			viewPort5.paintComponents(bufImage5.getGraphics());
			g.drawImage(bufImage5,viewPort3.getComponent(0).getWidth()+viewPort4.getComponent(0).getWidth(),viewPort1.getComponent(0).getHeight()+15,viewPort5.getComponent(0).getWidth(),viewPort5.getComponent(0).getHeight(),this.getComponent());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		ImageIcon icon = new ImageIcon(image, "Medusa");
		org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent(
				"Medusa Snapshot", icon,
				org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
		return event;
	}

	private void decodeStringToFile(String encodedInput, String decodedFile) throws Exception {

		byte[] decodedInput = Base64.decodeBase64(encodedInput.getBytes());
		FileOutputStream out = new FileOutputStream(decodedFile);
		out.write(decodedInput);
	}

}
