package org.geworkbench.components.genspace.workflowRepository;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import net.eleritec.docking.DockableAdapter;
import net.eleritec.docking.DockingManager;
import net.eleritec.docking.DockingPort;
import net.eleritec.docking.defaults.ComponentProviderAdapter;
import net.eleritec.docking.defaults.DefaultDockingPort;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.components.genspace.GenSpace;
import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.server.stubs.UserWorkflow;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.components.genspace.ui.UpdateablePanel;
import org.geworkbench.components.genspace.ui.WorkflowVisualizationPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.config.events.Event;
import org.geworkbench.engine.skin.Skin;

/**
 * Added by Flavio This UI class has been created by copying a considerable
 * amount of code from package org.geworkbench.engine.skin and the skin class in
 * particular It somehow overdesigned for our purpose, but it allows for extreme
 * flexibility and extensibility
 * 
 * @author flavio
 * 
 */
public class WorkflowRepository extends JPanel implements VisualPlugin,
		 Runnable,UpdateablePanel {

	private static final long serialVersionUID = 4935248850479380240L;

	private static JFrame frame;

	private Log log = LogFactory.getLog(this.getClass());
	private Map<String, DefaultDockingPort> areas = new Hashtable<String, DefaultDockingPort>();

	public static final String REPOSITORY_AREA = "RepositoryArea";
	public static final String INBOX_AREA = "InboxArea";
	public static final String VISUAL_AREA = "VisualArea";
	public static final String DETAILS_AREA = "DetailsArea";

	private DefaultDockingPort visualDock = new DefaultDockingPort();
	private DefaultDockingPort detailsDock = new DefaultDockingPort();
	private DefaultDockingPort inboxDock = new DefaultDockingPort();
	private DefaultDockingPort repositoryDock = new DefaultDockingPort();

	public RepositoryPanel repositoryPanel;
	public WorkflowVisualizationPanel graphPanel;
	public WorkflowDetailsPanel workflowDetailsPanel;
	public WorkflowCommentsPanel workflowCommentsPanel;
	public InboxTablePanel inboxTable;

	private BorderLayout borderLayout1 = new BorderLayout();

	private JLabel statusBar = new JLabel();
	private JSplitPane jSplitPane1 = new JSplitPane();
	private JSplitPane jSplitPane2 = new JSplitPane();
	private JSplitPane jSplitPane3 = new JSplitPane();

	private JPanel contentPane = this;
	// Not sure if the functions for these are actually used
	DockingNotifier eventSink = new DockingNotifier(this);
	private static Map<Component, String> visualRegistry = new HashMap<Component, String>();
	private HashMap<Component, Class<?>> mainComponentClass = new HashMap<Component, Class<?>>();
//	private Set<Class> acceptors;
//	private ArrayList<DockableImpl> visualDockables = new ArrayList<DockableImpl>();
//	private ArrayList<DockableImpl> detailsDockables = new ArrayList<DockableImpl>();
//	private ArrayList<DockableImpl> inboxDockables = new ArrayList<DockableImpl>();
//	private ArrayList<DockableImpl> projectDockables = new ArrayList<DockableImpl>();

	public WorkflowRepository(JFrame jframe) {
		log.debug("Workflow Repository started");
		frame = jframe;
	}

	public static JFrame getFrame() {
		return frame;
	}

	@Override
	public void run() {
		registerAreas();
		try {
			initComponents();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initComponents() throws Exception {
		contentPane.setLayout(borderLayout1);
		statusBar.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
		statusBar.setText(" ");
		jSplitPane1.setBorder(BorderFactory.createLineBorder(Color.black));
		jSplitPane1.setDoubleBuffered(true);
		jSplitPane1.setContinuousLayout(true);
		jSplitPane1.setBackground(Color.black);
		jSplitPane1.setDividerSize(8);
		jSplitPane1.setResizeWeight(0);
		jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPane2.setDoubleBuffered(true);
		jSplitPane2.setContinuousLayout(true);
		jSplitPane2.setDividerSize(8);
		jSplitPane2.setOneTouchExpandable(true);
		jSplitPane2.setResizeWeight(0.9);
		jSplitPane2.setMinimumSize(new Dimension(0, 0));
		jSplitPane3.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPane3.setBorder(BorderFactory.createLineBorder(Color.black));
		jSplitPane3.setDoubleBuffered(true);
		jSplitPane3.setContinuousLayout(true);
		jSplitPane3.setDividerSize(8);
		jSplitPane3.setOneTouchExpandable(true);
		jSplitPane3.setResizeWeight(0.1);
		jSplitPane3.setMinimumSize(new Dimension(0, 0));
		JPanel statusBarPanel = new JPanel();
		statusBarPanel.setLayout(new BorderLayout());
		statusBarPanel.add(statusBar, BorderLayout.EAST);
		contentPane.add(statusBarPanel, BorderLayout.SOUTH);
		contentPane.add(jSplitPane1, BorderLayout.CENTER);
		jSplitPane1.add(jSplitPane2, JSplitPane.RIGHT);
		jSplitPane2.add(detailsDock, JSplitPane.BOTTOM);
		jSplitPane2.add(visualDock, JSplitPane.TOP);
		jSplitPane1.add(jSplitPane3, JSplitPane.LEFT);
		jSplitPane3.add(inboxDock, JSplitPane.BOTTOM);
		jSplitPane3.add(repositoryDock, JSplitPane.LEFT);
		jSplitPane1.setDividerLocation(200);
		jSplitPane2.setDividerLocation(GenSpace.getInstance().jframe
				.getHeight() * 60 / 100);
		jSplitPane3.setDividerLocation(GenSpace.getInstance().jframe
				.getHeight() * 55 / 100);
		visualDock.setComponentProvider(new ComponentProvider(VISUAL_AREA));
		detailsDock.setComponentProvider(new ComponentProvider(DETAILS_AREA));
		inboxDock.setComponentProvider(new ComponentProvider(INBOX_AREA));
		repositoryDock.setComponentProvider(new ComponentProvider(
				REPOSITORY_AREA));

		repositoryPanel = new RepositoryPanel(this);
		addToContainer(REPOSITORY_AREA, repositoryPanel.getComponent(),
				"Repository", RepositoryPanel.class);
		graphPanel = new WorkflowVisualizationPanel();
		addToContainer(VISUAL_AREA, graphPanel.getComponent(), "Workflow",
				WorkflowVisualizationPanel.class);
		inboxTable = new InboxTablePanel(this);
		addToContainer(INBOX_AREA, inboxTable.getComponent(), "Inbox",
				InboxTablePanel.class);

		// DETAILS PANEL, add in inverse order of visualization
		workflowCommentsPanel = new WorkflowCommentsPanel(this);
		addToContainer(DETAILS_AREA, workflowCommentsPanel.getComponent(),
				"Workflow Comments", WorkflowCommentsPanel.class);
		workflowDetailsPanel = new WorkflowDetailsPanel(this);
		addToContainer(DETAILS_AREA, workflowDetailsPanel.getComponent(),
				"Workflow Details", WorkflowDetailsPanel.class);
	}

	/**
	 * Associates Visual Areas with Component Holders
	 */
	protected void registerAreas() {
		areas.put(REPOSITORY_AREA, repositoryDock);
		areas.put(INBOX_AREA, inboxDock);
		areas.put(VISUAL_AREA, visualDock);
		areas.put(DETAILS_AREA, detailsDock);
	}

	@Override
	public Component getComponent() {
		// In this case, this object is also the GUI component.
		return this;
	}

	public void setStatusBarText(String text) {
		statusBar.setText(text);
	}

	private void dockingFinished(DockableImpl comp) {
		for (String area : areas.keySet()) {
			Component port = areas.get(area);
			Component container = comp.getDockable().getParent();
			if (container instanceof JTabbedPane
					|| container instanceof JSplitPane) {
				if (container.getParent() == port) {
					eventSink.throwEvent(comp.getPlugin(), area);
				}
			} else if (container instanceof DefaultDockingPort) {
				if (container == port)
					eventSink.throwEvent(comp.getPlugin(), area);
			}
		}
	}

	public String getVisualArea(Component visualPlugin) {
		return visualRegistry.get(visualPlugin);
	}

	/**
	 * Removes the designated <code>visualPlugin</code> from the GUI.
	 * 
	 * @param visualPlugin
	 *            component to be removed
	 */
	@Override
	public void remove(Component visualPluginComponent) {
		mainComponentClass.remove(visualPluginComponent);
		visualRegistry.remove(visualPluginComponent);
	}

	public void addToContainer(String areaName, Component visualPlugin,
			String pluginName, @SuppressWarnings("rawtypes") Class mainPluginClass) {
		visualPlugin.setName(pluginName);
		DockableImpl wrapper = new DockableImpl(visualPlugin, pluginName);
		DockingManager.registerDockable(wrapper);
		// if (!areaName.equals(GUIFramework.VISUAL_AREA) &&
		// !areaName.equals(GUIFramework.COMMAND_AREA)) {
		DefaultDockingPort port = areas.get(areaName);
		port.dock(wrapper, DockingPort.CENTER_REGION);
		// }
		visualRegistry.put(visualPlugin, areaName);
		mainComponentClass.put(visualPlugin, mainPluginClass);
	}

	private class DockableImpl extends DockableAdapter {

		private JPanel wrapper = null;
		private JLabel initiator = null;
		private String description = null;
		private Component plugin = null;
		private JPanel buttons = new JPanel();
		private JPanel topBar = new JPanel();
		private JButton docker = new JButton();

		private boolean docked = true;

		private ImageIcon dock_grey = new ImageIcon(
				Skin.class.getResource("dock_grey.gif"));
		private ImageIcon dock = new ImageIcon(
				Skin.class.getResource("dock.gif"));
		private ImageIcon dock_active = new ImageIcon(
				Skin.class.getResource("dock_active.gif"));
		private ImageIcon undock_grey = new ImageIcon(
				Skin.class.getResource("undock_grey.gif"));
		private ImageIcon undock = new ImageIcon(
				Skin.class.getResource("undock.gif"));
		private ImageIcon undock_active = new ImageIcon(
				Skin.class.getResource("undock_active.gif"));

		DockableImpl(Component plugin, String desc) {
			this.plugin = plugin;
			wrapper = new JPanel();

			docker.setPreferredSize(new Dimension(16, 16));
			docker.setBorderPainted(false);
			docker.setIcon(undock_grey);
			docker.setRolloverEnabled(true);
			docker.setRolloverIcon(undock);
			docker.setPressedIcon(undock_active);
			docker.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					docker_actionPerformed(e);
				}
			});

			buttons.setLayout(new GridLayout(1, 3));
			buttons.add(docker);

			initiator = new JLabel(" ");
			initiator.setForeground(Color.darkGray);
			initiator.setBackground(Color.getHSBColor(0.0f, 0.0f, 0.6f));
			initiator.setOpaque(true);
			initiator.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent me) {
					setMoveCursor(me);
				}

				@Override
				public void mouseExited(MouseEvent me) {
					setDefaultCursor(me);
				}
			});

			topBar.setLayout(new BorderLayout());
			topBar.add(initiator, BorderLayout.CENTER);
			topBar.add(buttons, BorderLayout.EAST);

			wrapper.setLayout(new BorderLayout());
			wrapper.add(topBar, BorderLayout.NORTH);
			wrapper.add(plugin, BorderLayout.CENTER);
			description = desc;
		}

		private JFrame frame = null;

		private void docker_actionPerformed(ActionEvent e) {
			log.debug("Action performed.");
			String areaName = getVisualArea(this.getPlugin());
			DefaultDockingPort port = areas.get(areaName);
			if (docked) {
				undock(port);
				return;
			} else {
				redock(port);
			}
		}

		public void undock(final DefaultDockingPort port) {
			log.debug("Undocking.");
			port.undock(wrapper);
			port.reevaluateContainerTree();
			port.revalidate();
			port.repaint();
			docker.setIcon(dock_grey);
			docker.setRolloverIcon(dock);
			docker.setPressedIcon(dock_active);
			docker.setSelected(false);
			docker.repaint();
			frame = new JFrame(description);
			frame.setUndecorated(false);
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent we) {
					redock(port);
				}
			});
			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(wrapper, BorderLayout.CENTER);
			frame.pack();
			frame.setVisible(true);
			frame.repaint();
			docked = false;
			return;
		}

		public void redock(DefaultDockingPort port) {
			if (frame != null) {
				log.debug("Redocking " + plugin);
				docker.setIcon(undock_grey);
				docker.setRolloverIcon(undock);
				docker.setPressedIcon(undock_active);
				docker.setSelected(false);
				port.dock(this, DockingPort.CENTER_REGION);
				port.reevaluateContainerTree();
				port.revalidate();
				docked = true;
				frame.getContentPane().remove(wrapper);
				frame.dispose();
			}
		}

		private void setMoveCursor(MouseEvent me) {
			initiator.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		}

		private void setDefaultCursor(MouseEvent me) {
			initiator.setCursor(Cursor.getDefaultCursor());
		}

		@Override
		public Component getDockable() {
			return wrapper;
		}

		@Override
		public String getDockableDesc() {
			return description;
		}

		@Override
		public Component getInitiator() {
			return initiator;
		}

		public Component getPlugin() {
			return plugin;
		}

		@Override
		public void dockingCompleted() {
			dockingFinished(this);
		}

	}

	private class ComponentProvider extends ComponentProviderAdapter {

//		private String area;

		public ComponentProvider(String area) {
//			this.area = area;
		}

		// Add change listeners to appropriate areas so
		@Override
		public JTabbedPane createTabbedPane() {
			final JTabbedPane pane = new JTabbedPane();
			/*
			 * if (area.equals(VISUAL_AREA)) { pane.addChangeListener(new
			 * TabChangeListener(pane, visualLastSelected)); } else if
			 * (area.equals(COMMAND_AREA)) { pane.addChangeListener(new
			 * TabChangeListener(pane, commandLastSelected)); } else if
			 * (area.equals(SELECTION_AREA)) { pane.addChangeListener(new
			 * TabChangeListener(pane, selectionLastSelected)); }
			 */
			return pane;
		}

	}

	private class DockingNotifier extends Event {
		public DockingNotifier(Object s) {
			super(s);
		}

		public void throwEvent(Component source, String region) {
			// try {
			// throwEvent(ComponentDockingListener.class, "dockingAreaChanged",
			// new ComponentDockingEvent(this, source, region));
			// } catch (AppEventListenerException aele) {
			// aele.printStackTrace();
			// }
		}
	}

	/**
	 * Must NOT be called from a worker thread
	 */
	public void updateFormFields() {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){
			int evt;
			@Override
			protected void done() {
				GenSpace.getStatusBar().stop(evt);
				super.done();
			}
			@Override
			protected Void doInBackground() throws Exception {
				evt = GenSpace.getStatusBar().start("Updating workflow repository");
				updateFormFieldsBG();
				return null;
			}
			
		};
		worker.execute();
	}

	public void clearWorkflowData() {
		if(workflowCommentsPanel != null)
			workflowCommentsPanel.clearData();
		if(workflowDetailsPanel != null)
			workflowDetailsPanel.clearData();
		if(graphPanel != null)
			graphPanel.clearData();
//		if(inboxTable != null)
//			inboxTable.clearData();
	}

	/**
	 * Must be called from a worker thread
	 */
	public void updateFormFieldsBG() {
		Workflow selected = workflowCommentsPanel.workflow;
		if(GenSpaceServerFactory.isLoggedIn())
		{
			try {
				repositoryPanel.tree.root = GenSpaceServerFactory.getUserOps().getRootFolder();
			} catch (Exception e) {
			}
		}
		if(repositoryPanel != null && repositoryPanel.tree != null)
			repositoryPanel.tree.recalculateAndReload();
		if(inboxTable != null && GenSpaceServerFactory.isLoggedIn())
		{
			try {
				inboxTable.setData((GenSpaceServerFactory.getWorkflowOps().getIncomingWorkflows()));
			} catch (Exception e) {
			}
		}
		if(workflowCommentsPanel != null && workflowCommentsPanel.workflow != null)
		{
			for(UserWorkflow w : repositoryPanel.tree.root.getWorkflows())
			{
				if(w.getWorkflow().getId() == selected.getId())
				{
					workflowCommentsPanel.setData(w.getWorkflow());
					workflowCommentsPanel.repaint();
				}
			}
		}
		// whatever was selected, shouldn't be anymore
		clearWorkflowData();
	}

}
