package org.geworkbench.components.welcome.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.engine.ccm.ComponentConfigurationManager;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.ComponentClassLoader;
import org.geworkbench.engine.management.ComponentRegistry;
import org.geworkbench.engine.management.ComponentResource;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.ComponentConfigurationManagerUpdateEvent;

/**
 * 
 */

/**
 * This is the Welcome component, which will by default show up when you start
 * geWorkbench. This component is used to welcome user and tell user how to use
 * CCM to add components to geWorkbench. And if user click the button in it, it
 * will remove itself from CCM, and will not show up there after, unless user
 * active it in CCM again.
 * 
 * @author yc2480
 * @version $Id$
 */
public class WelcomeScreen extends JPanel implements VisualPlugin {
	private static final long serialVersionUID = -8058072598346112808L;

	static Log log = LogFactory.getLog(WelcomeScreen.class);

	/* the CCM file used for welcome component */
	private static final String CCMFILENAME = "WelcomeScreen.ccm.xml";

	/* the resource string used for CCM */
	private static final String RESOURCE = "welcome";

	/*
	 * The name of the welcome file. This file can either be a plain text file
	 * or a html file.
	 */
	private static final String WELCOMETEXT_FILENAME = "welcometext.html";

	/*
	 * Text for the button After press this button, this welcome component will
	 * be removed from CCM.
	 */
	private static final String BUTTON_TEXT = "Enter";

	/*
	 * The panel to hold JEditorPane, so the text field will expand to the whole
	 * visual area. (Using BorderLayout.CENTER)
	 */
	JPanel textFieldPanel = null;

	/* The text area to show welcome message */
	JEditorPane textField = null;

	/* The button to remove welcome component from CCM. */
	JButton button = null;

	/*
	 * will be at the bottom of the visual area, and will use default layout, so
	 * button can be in the middle of it.
	 */
	JPanel buttonPanel = null;

	/**
	 * This constructor will load the welcome file and display it in visual
	 * area. ccmRefresh() will be called, to let geWorkbench correctly display
	 * it on startup.
	 */
	public WelcomeScreen() {
		textFieldPanel = new JPanel();
		textFieldPanel.setLayout(new BorderLayout());
		String filename = "";
		try {
			/* Try load the welcome file */
			filename = getComponentPath(this) + File.separatorChar
					+ WELCOMETEXT_FILENAME;
			textField = new JEditorPane();
			textField.setContentType("text/html");
			textField.read(new BufferedReader(new FileReader(filename)),
					filename);
			textFieldPanel.add(textField, BorderLayout.CENTER);
		} catch (IOException e1) {
			/*
			 * If we can not load the welcome file, disable welcome component
			 * for failover.
			 */
			log.error("FIle " + filename + " not found.");
			removeComponent(RESOURCE, CCMFILENAME);
			return;
		}
		button = new JButton(BUTTON_TEXT);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeComponent(RESOURCE, CCMFILENAME);
			}
		});
		buttonPanel = new JPanel();
		buttonPanel.add(button);
		textFieldPanel.add(buttonPanel, BorderLayout.SOUTH);
	}

	/**
	 * This method will remove the specified component from GUI, CCM, and
	 * refresh geWorkbench.
	 * 
	 * @param resource
	 *            resource used in CCM
	 * @param ccmFileName
	 *            file name of CCM file, currently ending with .ccm.xml
	 */
	private void removeComponent(String resource, String ccmFileName) {
		// remove from GUI
		ComponentConfigurationManager manager = ComponentConfigurationManager.getInstance();
		manager.removeComponent(resource, ccmFileName);
		// remove from CCM
		String propFileName = ccmFileName.replace(".ccm.xml", ".ccmproperties");
		String sChoice = (new Boolean(false)).toString();
		ComponentConfigurationManager.writeProperty(resource, propFileName,
				"on-off", sChoice);
		// refresh
		ccmRefresh();
	}

	/*
	 * This method will publish ComponentConfigurationManagerUpdateEvent to all
	 * acceptors. And also tell CCM to load everything again, to make CCM status
	 * up to date.
	 */
	private void ccmRefresh() {
		ComponentRegistry componentRegistry = ComponentRegistry.getRegistry();
		ComponentConfigurationManagerUpdateEvent ccmEvent = new ComponentConfigurationManagerUpdateEvent(
				componentRegistry
				.getAcceptorsHashMap());
		publishComponentConfigurationManagerUpadateEvent(ccmEvent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.engine.config.VisualPlugin#getComponent()
	 */
	public Component getComponent() {
		return textFieldPanel;
	}

	/**
	 * Publish the {@link ComponentConfigurationManagerUpdateEvent}.
	 * 
	 * @param event
	 * @return event
	 */
	@Publish
	public ComponentConfigurationManagerUpdateEvent publishComponentConfigurationManagerUpadateEvent(
			ComponentConfigurationManagerUpdateEvent event) {
		return event;
	}

	/**
	 * This method will return the path of given Object. Used for obtain the
	 * path of geWorkbench component. Can be extracted to geWorkbench-core.
	 * 
	 * @param aspp
	 * @return
	 */
	private String getComponentPath(Object aspp) {
		String answer = null;
		try {
			ComponentClassLoader ccl = (ComponentClassLoader) aspp.getClass()
					.getClassLoader();
			ComponentResource componentResource = ccl.getComponentResource();
			String componentDirectory = componentResource.getDir();
			answer = componentDirectory;
		} catch (Exception e) {
			log.error(e, e);
		}
		return answer;
	}

}
