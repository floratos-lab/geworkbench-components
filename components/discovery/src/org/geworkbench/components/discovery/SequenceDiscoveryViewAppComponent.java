package org.geworkbench.components.discovery;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
import org.geworkbench.bison.datastructure.complex.pattern.PatternDiscoveryParameters;
import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Asynchronous;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.HistoryEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SequenceDiscoveryTableEvent;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.PropertiesMonitor;
import org.geworkbench.util.remote.Connection;
import org.geworkbench.util.remote.ConnectionCreationException;
import org.geworkbench.util.remote.SPLASHDefinition;
import org.geworkbench.util.session.DiscoverySession;
import org.geworkbench.util.session.Logger;
import org.geworkbench.util.session.LoggerException;
import org.geworkbench.util.session.LoginPanelModel;
import org.geworkbench.util.session.SessionCreationException;
import org.geworkbench.util.session.dialog.SessionChooser;

import polgara.soapPD_wsdl.Parameters;

/**
 * <p>
 * This AppComponent controls its associated widget SequenceDiscoveryViewWidget
 * </p>
 * <p>
 * Description: SequenceDiscoveryViewAppComponent is controller of its
 * associated widget: SequenceDiscoveryViewWidget. Could make this the primary
 * event thrower for all events from SequenceDiscoveryViewWidget; at the moment,
 * SequenceDiscoveryViewWidget throws a row selection
 * SequenceDiscoveryTableEvent.
 * </p>
 * <p>
 * Copyright (c) 2003
 * </p>
 * <p>
 * Company: Califano Lab
 * </p>
 *
 * @version $Id$
 */

@AcceptTypes( { CSSequenceSet.class, PatternResult.class } )
public class SequenceDiscoveryViewAppComponent implements VisualPlugin,
		PropertyChangeListener {

	private Log log = LogFactory
			.getLog(SequenceDiscoveryViewAppComponent.class);

	private SequenceDiscoveryViewWidget sDiscoveryViewWidget = null;

	// This is the currently selected database in the project.
	// It is updated every time a file selection occurs in the main project.
	private DSSequenceSet<? extends DSSequence> fullSequenceDB = null;
	private CSSequenceSet<? extends DSSequence> activeSequenceDB = null;

	// Following variables will be used to fix bug 660, add selection to the
	// module.
	private DSPanel<? extends DSGeneMarker> activatedMarkers = null;
	private final int ATDATANODEWITHOUTSUBNODE = 0;
	private final int SEQUENCE = 3;
	private final int NONSEQUENCE = 4;
	private int currentStatus = ATDATANODEWITHOUTSUBNODE;

	// login data parameters are stored here
	private LoginPanelModel loginPanelModel = new LoginPanelModel();

	public SequenceDiscoveryViewAppComponent() {
		try {
			sDiscoveryViewWidget = new SequenceDiscoveryViewWidget();
			sDiscoveryViewWidget.addPropertyChangeListener(this);
			sDiscoveryViewWidget.setSequenceDiscoveryViewAppComponent(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private DiscoverySession connectToService(String host, int port,
			String userName, char[] password, String sessionName)
			throws SessionCreationException {
		// establish a connection
		Connection connection = getConnection(host, port);
		// now try to login
		Logger logger = getLogger(connection, userName, password);

		File seqFile = null;
		/*
		 * This returns the file on which a discovery will be made on.
		 */
		synchronized (this) {
			if (activeSequenceDB != null && activeSequenceDB.size() > 0) {
				seqFile = activeSequenceDB.getFile();
			} else if (fullSequenceDB != null) {
				seqFile = fullSequenceDB.getFile();
			} else {
				return null;
			}
		}

		if (seqFile == null) {
			return null;
		}
		DSSequenceSet<? extends DSSequence> database = CSSequenceSet
				.getSequenceDB(seqFile);
		// the database will be saved with this name on the server.
		String databaseName = SPLASHDefinition.encodeFile(database.getFile(),
				userName);
		// create a session
		return new DiscoverySession(sessionName, database, databaseName,
				connection, userName, logger.getUserId());
	}

	private Logger getLogger(Connection connection, String user, char[] password)
			throws SessionCreationException {
		Logger logger = null;
		try {
			logger = new Logger(connection, user, password);
		} catch (LoggerException exp) {
			throw new SessionCreationException("Login operation failed.");
		}
		return logger;
	}

	private Connection getConnection(String host, int port)
			throws SessionCreationException {
		URL url = null;
		try {
			url = Connection.getURL(host, port);
		} catch (MalformedURLException ex) {
			throw new SessionCreationException("Could not form URL. (host: "
					+ host + "port: " + port + ")");
		}

		try {
			return new Connection(url);
		} catch (ConnectionCreationException ex) {
			throw new SessionCreationException(
					"Could not connect to the server.");
		}
	}

	public Component getComponent() {
		return sDiscoveryViewWidget;
	}

	private void showInfoMessage(String message, String title) {
		JOptionPane.showMessageDialog(null, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	void updateDataSetView() {

		boolean activateMarkers = true;
		if (currentStatus == NONSEQUENCE) {
			return;
		}

		if (activatedMarkers != null && activatedMarkers.size() > 0) {
			if (activateMarkers && (fullSequenceDB != null)) {
				// createActivatedSequenceSet();
				activeSequenceDB = (CSSequenceSet<? extends DSSequence>) ((CSSequenceSet<? extends DSSequence>) fullSequenceDB)
						.getActiveSequenceSet(activatedMarkers);
			}
		} else {
			activeSequenceDB = (CSSequenceSet<? extends DSSequence>) fullSequenceDB;
		}
		if (activeSequenceDB == null) {
			activeSequenceDB = (CSSequenceSet<? extends DSSequence>) fullSequenceDB;
		} else if (activeSequenceDB.size() < fullSequenceDB.size()) {
			// create a temp folder for new Sequence.
			String tempFolder = FilePathnameUtils
					.getTemporaryFilesDirectoryPath();
			String tempString = fullSequenceDB.getFile().getName() + "temp-"
					+ activeSequenceDB.size();
			File tempFile = new File(tempFolder + tempString);
			if (createFile(tempFile)) {
				activeSequenceDB = new CSSequenceSet<DSSequence>();
				activeSequenceDB.readFASTAFile(tempFile);
				activeSequenceDB.setFASTAFile(tempFile);
			}
		}

		if (activeSequenceDB instanceof DSSequenceSet) {
			sDiscoveryViewWidget.setSequenceDB(activeSequenceDB);
		}

	}

	/**
	 * Write the sequence data into a file.
	 * 
	 * @return <code>true<code> if succeeds.
	 * @param tempFile
	 * @param sequences
	 * @return
	 */
	private boolean createFile(File tempFile) {
		try {
			if (activeSequenceDB == null || activeSequenceDB.size() == 0) {
				return false;
			}
			PrintWriter out = new PrintWriter(new FileOutputStream(tempFile));
			for (int i = 0; i < activeSequenceDB.size(); i++) {

				CSSequence seq = (CSSequence) activeSequenceDB.get(i);
				if (seq != null) {
					out.println(">" + seq.getLabel());
					out.println(seq.getSequence());
				}
			}

			out.flush();
			out.close();

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * geneSelectorAction
	 *
	 * @param e
	 *            GeneSelectorEvent
	 */
	@Subscribe(Asynchronous.class)
	public void receive(GeneSelectorEvent e, Object source) {
		if (e.getPanel() != null && e.getPanel().size() > 0) {
			activatedMarkers = e.getPanel().activeSubset();
		} else {
			activatedMarkers = null;
		}
		log.debug(activatedMarkers);
		updateDataSetView();
	}

	/**
	 * We want to know when the user selects file
	 */
	@SuppressWarnings("unchecked")
	@Subscribe
	public void receive(ProjectEvent e, Object source) {
		if (e.getMessage().equalsIgnoreCase("Project Cleared")) {
			fullSequenceDB = null;
			return;
		}
		ProjectSelection selection = ((ProjectPanel) source).getSelection();
		DSDataSet<?> df = selection.getDataSet();
		if (df != null) {
			// update db with the selected file in the project
			if (df instanceof DSSequenceSet) {
				currentStatus = SEQUENCE;
				fullSequenceDB = (DSSequenceSet<? extends DSSequence>) df;
				sDiscoveryViewWidget.setSequenceDB((DSSequenceSet<? extends DSSequence>) df,
						false, null, null, null);
			} else {
				currentStatus = NONSEQUENCE;
			}
		}

		DSDataSet<?> dataset = e.getDataSet();
		if (dataset instanceof PatternResult) {
			PatternResult soapParmsDataSet = (PatternResult) dataset;
			PatternDiscoveryParameters p = soapParmsDataSet.getParameters();
			if(p==null)
				p = new PatternDiscoveryParameters();
			Parameters parms = ParameterTranslation
					.getParameters(p);
			DSSequenceSet<? extends DSSequence> sequenceDB = (DSSequenceSet<? extends DSSequence>)df;
			if (df.equals(fullSequenceDB)) sequenceDB = activeSequenceDB;
			sDiscoveryViewWidget.setSequenceDB(sequenceDB, true,
					soapParmsDataSet.getID(), parms, soapParmsDataSet);
			sDiscoveryViewWidget
					.setCurrentView(SequenceDiscoveryViewWidget.PATTERN_TABLE);
		} else {
			sDiscoveryViewWidget
					.setCurrentView(SequenceDiscoveryViewWidget.DEFAULT_VIEW);
		}
	}

	/**
	 * The method returns a session. Note: The method will pop up a dialog to
	 * create a session.
	 *
	 * @return the active session.
	 */
	synchronized DiscoverySession getSession() {
		// check that a database file is selected in the project.
		if (!isDiscoveryFileSet()) {
			showInfoMessage("Please select a sequence file.", "Select File");
			return null;
		}

		/*
		 * This part creates a new session. The session is created with the
		 * database file returned from getDivcoveryFile.
		 */
		// if the chooser is null, the user cancelled the dialog
		// intermediate values of the loginPanelModel are saved here
		LoginPanelModel tempLoginModel = new LoginPanelModel();
		copyLoginPanelModel(loginPanelModel, tempLoginModel);
		SessionChooser chooser = new SessionChooser(null,
				"New DiscoverySession", tempLoginModel);
		int retVal = chooser.show();
		if (retVal == SessionChooser.CANCEL_OPTION) {
			return null;
		}

		String host = chooser.getHostName();
		int port = chooser.getPortNum();
		String userName = chooser.getUserName();
		char[] password = chooser.getPassWord();
		String sName = chooser.getSession();

		// try to create this session
		DiscoverySession aDiscoverySession = null;
		try {
			aDiscoverySession = connectToService(host, port, userName,
					password, sName);

		} catch (SessionCreationException exp) {
			//exp.printStackTrace();
			showInfoMessage("DiscoverySession was not created. "
					+ exp.getMessage(), "DiscoverySession Error");
			return null;
		}

		// save the user's choosing to the Properties file and to the model
		saveSessionProperties(host, port, userName);
		copyLoginPanelModel(tempLoginModel, loginPanelModel);

		return aDiscoverySession;
	}

	/**
	 * copy the data from one LoginPanelModel to the other.
	 *
	 * @param from
	 * @param to
	 */
	private void copyLoginPanelModel(LoginPanelModel from, LoginPanelModel to) {
		to.setHostNames(from.getHostSet(), from.getHostName());
		to.setPort(from.getPort());
		to.setUserName(from.getUserName());
	}

	/**
	 * This method passes the session properties to the Properties manager.
	 *
	 * @param host
	 *            host name
	 * @param port
	 *            port number
	 * @param user
	 *            user name
	 */
	private void saveSessionProperties(String host, int port, String user) {
		org.geworkbench.util.PropertiesMonitor pmMgr = PropertiesMonitor
				.getPropertiesMonitor();
		pmMgr.addHost(host);
		pmMgr.setPort(port);
		pmMgr.setUserName(user);
		pmMgr.setHostSelected(host);
		pmMgr.writeProperties();
	}

	/**
	 * This method returns a true if a discovery file is selected.
	 *
	 * @return true if and only if a file is selected in the project pannel.
	 */
	private boolean isDiscoveryFileSet() {
		return (fullSequenceDB != null);
	}

	/**
	 * This method is used to fire events from the SequenceDiscoveryViewWidget
	 *
	 * @param evt
	 *            property event
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String property = evt.getPropertyName();
		if (property
				.equalsIgnoreCase(SequenceDiscoveryViewWidget.TABLE_EVENT)) {
			notifyTableEvent(evt);
		}
	}

	public void createNewNode(DSAncillaryDataSet<? extends DSBioObject> dataset) {
		org.geworkbench.events.ProjectNodeAddedEvent event = new org.geworkbench.events.ProjectNodeAddedEvent(
				"message", null, dataset);
		publishProjectNodeAddedEvent(event);
	}

	@Publish
	public org.geworkbench.events.ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			org.geworkbench.events.ProjectNodeAddedEvent event) {
		return event;
	}

	private void notifyTableEvent(PropertyChangeEvent evt) {
		if (!isDiscoveryFileSet()) {
			return;
		}

		JTable table = (JTable) evt.getNewValue();
		List<DSMatchedPattern<DSSequence, CSSeqRegistration>> patternMatches = new ArrayList<DSMatchedPattern<DSSequence, CSSeqRegistration>>();
		if (table != null) {
			org.geworkbench.util.patterns.PatternTableModel model = (org.geworkbench.util.patterns.PatternTableModel) (table)
					.getModel();
			int[] rows = table.getSelectedRows();
			for (int i = 0; i < rows.length; i++) {
				DSMatchedPattern<DSSequence, CSSeqRegistration> pattern = model
						.getPattern(rows[i]);
				patternMatches.add(pattern);
			}
		}

		SequenceDiscoveryTableEvent e = new SequenceDiscoveryTableEvent(
				patternMatches);
		publishSequenceDiscoveryTableEvent(e);
	} // end notify table event

	@Publish
	public SequenceDiscoveryTableEvent publishSequenceDiscoveryTableEvent(
			SequenceDiscoveryTableEvent event) {
		return event;
	}

	/**
	 * This method is used to trigger HistoryPanel to refresh.
	 *
	 * @param event
	 * @return
	 */
	@Publish
	public HistoryEvent publishHistoryEvent(HistoryEvent event) {
		return event;
	}

}
