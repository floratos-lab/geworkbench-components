package org.geworkbench.components.versioninfo;

import java.awt.AWTEvent;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Class to display version information dialog.
 * 
 * @author not attributable
 * @version $Id: VersionInfoDialog.java,v 1.14 2009-06-09 20:27:29 keshav Exp $
 */
public class VersionInfoDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 4152518674720567787L;
	
	static final String VERSION = System.getProperty("application.version");

	static Log log = LogFactory.getLog(VersionInfoDialog.class);

	/**
	 * VersionInfoDialog
	 */
	public VersionInfoDialog() {
		this(null);
	}

	private static Properties properties = new Properties();	
	private JButton okButton = new JButton();
	
	private static String buildTime = (new Date()).toString();

	public VersionInfoDialog(Frame parent) {
		super(parent);

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		try {
			properties.load(VersionInfoDialog.class.getResource(VERSION_INFO_FILENAME)
					.openStream());		
			
		} catch (IOException ex1) {
			log.warn("VersionInfo reading error" + ex1.toString());
		}

		buildTime = properties.getProperty("buildTime");
		
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		pack();
	}

	private void jbInit() throws Exception {

		setTitle("geWorkbench Version Info");
		setResizable(false);

		getContentPane().setLayout(
				new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));

		okButton.setText("OK");
		okButton.addActionListener(this);

		{
			FormLayout layout = new FormLayout("right:270dlu,10dlu", "");
			DefaultFormBuilder builder = new DefaultFormBuilder(layout);
			builder.setDefaultDialogBorder();
			builder.appendSeparator("geWorkbench Core");
			builder.nextLine();
			builder.append(new JLabel("geWorkbench"));
			builder.nextLine();
			builder.append(new JLabel(VERSION));
			builder.nextLine();
			if (!StringUtils.isEmpty(buildTime)) {
				builder.append(new JLabel("Updated on " + buildTime));
			}

			getContentPane().add(builder.getPanel(), null);
		}

		{
			FormLayout layout = new FormLayout("right:270dlu", "");
			DefaultFormBuilder builder = new DefaultFormBuilder(layout);
			builder.setDefaultDialogBorder();
			builder.append(okButton);

			getContentPane().add(builder.getPanel(), null);
		}
	}

	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			dispose();
		}
		super.processWindowEvent(e);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			dispose();
		}
	}

	private static String VERSION_INFO_FILENAME = "version.txt";

	/**
	 * Run to build a update properties file for version and build time
	 * information.
	 * 
	 * This method is used independent of the geWorkbench application.
	 * 
	 * @param string
	 *            - args[0] should be the version number, e.g. "1.6.1" args[1]
	 *            should be the path to save the file e.g."c:/java/apps/geworkbench_workspace2/geworkbench-core/componenents/versioninfo/org/geworkbench/components/versioninfo/"
	 *            ;
	 * 
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out
					.println("VersionInfoDialog is invoked with wrong number of arguments.");
			System.exit(1);
		}

		DateFormat df = new SimpleDateFormat("EEEE MMMM d HH:mm:ss zzz yyyy");

		try {
			PrintWriter pw = new PrintWriter(new FileWriter(args[0]
					+ VERSION_INFO_FILENAME));
			pw.println("#Version Information:");			
			pw.println("buildTime=" + df.format(new java.util.Date()));
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1); // make sure the building process will stop
		}
	}
}
