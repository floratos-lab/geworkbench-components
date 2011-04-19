package org.geworkbench.components.genspace;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.properties.PropertiesManager;

public class GenSpaceLogPreferences extends JPanel implements VisualPlugin,
		ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1021538435104037797L;
	JRadioButton log, logAnon, noLog;
	ButtonGroup group;
	JPanel radioPanel, saveReset;
	JButton save, reset;
	static final String PROPERTY_KEY = "genSpace_logging_preferences"; // the
																		// key
																		// in
																		// the
																		// properties
																		// file
	int preference; // the logging preference

	public GenSpaceLogPreferences() {
		// read the preferences from the properties file
		try {
			PropertiesManager properties = PropertiesManager.getInstance();
			String pref = properties.getProperty(GenSpaceLogPreferences.class,
					PROPERTY_KEY, null);
			if (pref == null) {
				// if the preferences are not set, then show the pop up window

				// ideally this should also be in the properties file
				String message = "geWorkbench now includes a component called genSpace,\n"
						+ "which will provide social networking capabilities and allow\n"
						+ "you to connect with other geWorkbench users.\n\n"
						+ "In order for it to be effective, genSpace must log which analysis\n"
						+ "tools you use during your geWorkbench session.\n\n"
						+ "Please go to Tools > genSpace window to configure \n"
						+ "your preference. You can later change it at any time.";
				String title = "Please set your genSpace logging preferences.";
				JOptionPane.showMessageDialog(this, message, title,
						JOptionPane.INFORMATION_MESSAGE);

				// set the default to "log anonymously"
				pref = "1";
				// write it to the properties file
				properties.setProperty(GenSpaceLogPreferences.class,
						PROPERTY_KEY, pref);
			}

			// set the logging level
			ObjectHandler.setLogStatus(Integer.parseInt(pref));
			preference = Integer.parseInt(pref);
		} catch (Exception e) {
		}

		initComponents();
	}

	private void initComponents() {

		log = new JRadioButton("Log my analysis events");
		log.setActionCommand("0");
		logAnon = new JRadioButton("Log my analysis events anonymously");
		logAnon.setActionCommand("1");
		noLog = new JRadioButton("Do not log my analysis events");
		noLog.setActionCommand("2");

		group = new ButtonGroup();

		save = new JButton("Save");
		reset = new JButton("Reset");

		group.add(log);
		group.add(logAnon);
		group.add(noLog);

		log.addActionListener(this);
		logAnon.addActionListener(this);
		noLog.addActionListener(this);
		save.addActionListener(this);
		reset.addActionListener(this);

		radioPanel = new JPanel(new GridLayout(0, 1));
		radioPanel.add(log);
		radioPanel.add(logAnon);
		radioPanel.add(noLog);

		saveReset = new JPanel(new GridLayout(0, 2));
		saveReset.add(save);
		saveReset.add(reset);
		radioPanel.add(saveReset);

		add(radioPanel);

		if (preference == 0) {
			log.setSelected(true);
		} else if (preference == 1) {
			logAnon.setSelected(true);
		} else if (preference == 2) {
			noLog.setSelected(true);
		}
		save.setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == save) {
			// System.out.println("Save pressed with " +
			// group.getSelection().getActionCommand());
			preference = Integer.parseInt(group.getSelection()
					.getActionCommand());
			ObjectHandler.setLogStatus(preference);
			save.setEnabled(false);
			// write it to the properties file
			try {
				PropertiesManager properties = PropertiesManager.getInstance();
				properties.setProperty(GenSpaceLogPreferences.class,
						PROPERTY_KEY, group.getSelection().getActionCommand());
			} catch (Exception ex) {
			}

		} else if (e.getSource() == reset) {
			// System.out.println("Reset pressed");
			logAnon.setSelected(true);
			save.setEnabled(true);
		} else if (e.getSource() == log) {
			if (preference == 0) {
				save.setEnabled(false);
			} else {
				save.setEnabled(true);
			}
		} else if (e.getSource() == logAnon) {
			if (preference == 1) {
				save.setEnabled(false);
			} else {
				save.setEnabled(true);
			}
		} else if (e.getSource() == noLog) {
			if (preference == 2) {
				save.setEnabled(false);
			} else {
				save.setEnabled(true);
			}
		}
	}

	@Override
	public Component getComponent() {
		// In this case, this object is also the GUI component.
		return this;
	}
}
