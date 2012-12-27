package org.geworkbench.components.selectors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.properties.DSSequential;
import org.geworkbench.builtin.projects.DataSetNode;
import org.geworkbench.builtin.projects.DataSetSubNode;
import org.geworkbench.builtin.projects.ProjectTreeNode;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.Asynchronous;
import org.geworkbench.engine.management.Overflow;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.visualproperties.PanelVisualProperties;
import org.geworkbench.util.visualproperties.PanelVisualPropertiesManager;
import org.geworkbench.util.visualproperties.VisualPropertiesDialog;

/**
 * @author John Watkinson
 * @version $Id$
 */
public abstract class SelectorPanel<T extends DSSequential> implements
		VisualPlugin, MenuListener {

	public static final String SELECTION_LABEL = "Selection";

	// Data models
	protected DSAnnotationContext<T> context;
	protected SelectorTreeModel<T> treeModel;
	protected DSItemList<T> itemList;

	protected FilterListModel listModel;

	protected CSAnnotationContext<T> emptyContext;
	protected CSItemList<T> emptyList;

	// Components
	protected JPanel mainPanel;
	protected ItemList itemAutoList;
	protected JTree panelTree;
	// Menu items
	protected JPopupMenu itemListPopup = new JPopupMenu();
	protected JMenuItem addToPanelItem = new JMenuItem("Add to Set");
	protected JMenuItem clearSelectionItem = new JMenuItem("Clear \"Selection\" Set");
	protected JPopupMenu treePopup = new JPopupMenu();
	protected JMenuItem renamePanelItem = new JMenuItem("Rename");
	protected JMenuItem copyPanelItem = new JMenuItem("Copy");
	protected JMenuItem activatePanelItem = new JMenuItem("Activate");
	protected JMenuItem deactivatePanelItem = new JMenuItem("Deactivate");
	protected JMenuItem deletePanelItem = new JMenuItem("Delete");
	protected JMenuItem printPanelItem = new JMenuItem("Print");
	protected JMenuItem visualPropertiesItem = new JMenuItem(
			"Visual Properties");
	protected JPopupMenu rootPopup = new JPopupMenu();
	protected JPopupMenu itemPopup = new JPopupMenu();
	protected JMenuItem removeFromPanelItem = new JMenuItem("Remove from Set");

	protected JMenuItem combineMenuItem = new JMenu("Combine");
	protected JMenuItem unionPanelItem = new JMenuItem("Union");
	protected JMenuItem intersectionPanelItem = new JMenuItem("Intersection");
	protected JMenuItem xorPanelItem = new JMenuItem("Xor");
	protected JPanel lowerPanel = new JPanel();
	protected JComboBox contextSelector;
	protected JButton newContextButton;
	// Context info for right-click events
	TreePath rightClickedPath = null;

	protected Class<T> panelType;
	protected SelectorTreeRenderer treeRenderer;
	protected JMenuItem saveMergeSets = new JMenuItem("Merge into one set");
	protected JMenuItem saveMultiSets = new JMenuItem("Save as multiple sets");
	protected SelectorHelper<T> helper = null;
	protected abstract SelectorHelper<T> getSelectorHelper();
	/*
	 * config file to store last used directory for marker set saving/loading
	 */
	protected String selectorLastDirConf = null;
	protected abstract void setSelectorLastDirConf();
	/**
	 * The variable will store last visited directory
	 */
	protected String lastDir = "";
	protected String typeName = null;

	private HashMap<String, ActionListener> menuListeners;

	public SelectorPanel(Class<T> panelType, String name) {
		this.panelType = panelType;
		this.typeName = name;
		listModel = new FilterListModel();
		itemAutoList = new ItemList(listModel);
		// Initialize data models
		emptyList = new CSItemList<T>();
		// emptyPanel = new CSPanel<T>("");
		emptyContext = new CSAnnotationContext<T>("", null);
		itemList = emptyList;
		treeModel = new SelectorTreeModel<T>(emptyContext);
		// Initialize components
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		itemAutoList.getList().setCellRenderer(new ListCellRenderer());
		// itemAutoList.getList().setPrototypeCellValue("1236789");
		itemAutoList.getList().setFixedCellWidth(200);
		itemAutoList.getList().setFixedCellHeight(15);
		panelTree = new JTree(treeModel);
		lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.Y_AXIS));
		// Add context selector
		contextSelector = new JComboBox();
		Dimension minSize = contextSelector.getMinimumSize();
		// Dimension prefSize = contextSelector.getPreferredSize();
		contextSelector.setMaximumSize(new Dimension(1000, minSize.height));
		newContextButton = new JButton("New");
		newContextButton.setEnabled(false);
		JPanel contextPanel = new JPanel();
		contextPanel.setLayout(new BoxLayout(contextPanel, BoxLayout.X_AXIS));
		contextPanel.add(contextSelector);
		contextPanel.add(newContextButton);
		JLabel groupLabel = new JLabel(" " + name + " Sets");
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		labelPanel.add(groupLabel);
		labelPanel.add(Box.createHorizontalGlue());

		lowerPanel.add(labelPanel);
		lowerPanel.add(contextPanel);
		JScrollPane panelTreePane = new JScrollPane(panelTree);
		lowerPanel.add(panelTreePane);
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				itemAutoList, lowerPanel);
		splitPane.setDividerSize(3);
		splitPane.setResizeWeight(0.65);
		treeRenderer = new SelectorTreeRenderer(this);
		panelTree.setCellRenderer(treeRenderer);
		mainPanel.add(splitPane, BorderLayout.CENTER);
		// Initialize popups
		itemListPopup.add(addToPanelItem);
		itemListPopup.add(clearSelectionItem);
		treePopup.add(renamePanelItem);
		treePopup.add(copyPanelItem);
		treePopup.add(activatePanelItem);
		treePopup.add(deactivatePanelItem);
		treePopup.add(deletePanelItem);
		combineMenuItem.add(unionPanelItem);
		combineMenuItem.add(intersectionPanelItem);
		combineMenuItem.add(xorPanelItem);
		treePopup.add(combineMenuItem);
		treePopup.add(printPanelItem);

		treePopup.add(visualPropertiesItem);

		savePanelItem.add(saveMergeSets);
		savePanelItem.add(saveMultiSets);

		// TODO - move to a new gui setup
		itemPopup.add(removeFromPanelItem);

		// Add behaviors
		menuListeners = new HashMap<String, ActionListener>();
		panelTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				panelTreeClicked(e);
			}
		});
		ActionListener addToPanelListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addToLabelPressed();
			}
		};
		addToPanelItem.addActionListener(addToPanelListener);
		menuListeners.put("Commands.Sets.Add to Set", addToPanelListener);
		ActionListener clearListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearSelectionPressed();
			}
		};
		clearSelectionItem.addActionListener(clearListener);
		menuListeners.put("Commands.Clear \"Selection\" Set", clearListener);
		ActionListener renameListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				renameLabelPressed(rightClickedPath);
			}
		};
		renamePanelItem.addActionListener(renameListener);
		menuListeners.put("Commands.Sets.Rename", renameListener);
		ActionListener copyListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copyLabelPressed(rightClickedPath);
			}
		};
		copyPanelItem.addActionListener(copyListener);
		menuListeners.put("Commands.Sets.Copy", copyListener);
		ActionListener activateListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				activateOrDeactivateLabelPressed(true);
			}
		};
		activatePanelItem.addActionListener(activateListener);
		menuListeners.put("Commands.Sets.Activate", activateListener);
		ActionListener deactivateListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				activateOrDeactivateLabelPressed(false);
			}
		};
		deactivatePanelItem.addActionListener(deactivateListener);
		ActionListener deleteListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deletePanelPressed();
			}
		};
		deletePanelItem.addActionListener(deleteListener);
		menuListeners.put("Commands.Sets.Delete", deleteListener);
		menuListeners.put("Commands.Sets.Deactivate", deactivateListener);
		printPanelItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printPanelPressed(rightClickedPath);
			}
		});
		visualPropertiesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				visualPropertiesPressed(rightClickedPath);
			}
		});
		removeFromPanelItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeFromLabelPressed();
			}
		});
		contextSelector.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				DSAnnotationContext<T> newContext = (DSAnnotationContext<T>) contextSelector
						.getSelectedItem();
				switchContext(newContext);
			}
		});
		newContextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createNewContext();
			}
		});
		ActionListener unionActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findUnion();
			}
		};
		unionPanelItem.addActionListener(unionActionListener);
		ActionListener intersectionActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findIntersection();
			}
		};
		intersectionPanelItem.addActionListener(intersectionActionListener);
		ActionListener xorActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findXor();
			}
		};
		xorPanelItem.addActionListener(xorActionListener);
	}

	protected String getLabelForPath(TreePath path) {
		if(path==null)return null;
		Object obj = path.getLastPathComponent();
		if (obj instanceof String) {
			return (String) obj;
		} else {
			return null;
		}
	}

	private T getItemForPath(TreePath path) {
		Object obj = path.getLastPathComponent();
		if (panelType.isAssignableFrom(obj.getClass())) {
			return panelType.cast(obj);
		} else {
			return null;
		}
	}

	private void ensurePathIsSelected(TreePath path) {
		if (path != null) {
			boolean alreadySelected = false;
			TreePath[] paths = panelTree.getSelectionPaths();
			if (paths != null) {
				for (int i = 0; i < paths.length; i++) {
					if (paths[i].getLastPathComponent().equals(
							path.getLastPathComponent())) {
						alreadySelected = true;
						break;
					}
				}
			}
			if (!alreadySelected) {
				panelTree.setSelectionPath(path);
			}
		}
	}

	private void ensureItemIsSelected(int index) {
		boolean alreadySelected = false;
		int[] indices = itemAutoList.getList().getSelectedIndices();
		if (indices != null) {
			for (int i = 0; i < indices.length; i++) {
				if (index == indices[i]) {
					alreadySelected = true;
					break;
				}
			}
		}
		if (!alreadySelected) {
			itemAutoList.getList().setSelectedIndex(index);
		}
	}

	private void removePanel(final String label) {
		final int index = treeModel.getIndexOfChild(context, label);
		context.removeLabel(label);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				treeModel.fireLabelRemoved(label, index);
			}
		});
	}

	protected void addPanel(DSPanel<T> panel) {
		final String label = panel.getLabel();
		context.labelItems(panel, label);
		final int index = treeModel.getIndexOfChild(context, label);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				treeModel.fireLabelAdded(label, index);
			}
		});
	}

	protected void panelTreeClicked(final MouseEvent e) {
		TreePath path = panelTree.getPathForLocation(e.getX(), e.getY());
		if (path != null) {
			String label = getLabelForPath(path);
			T item = getItemForPath(path);
			if ((e.getButton()==MouseEvent.BUTTON3) && (e.getClickCount() == 1)) {
				rightClickedPath = path;
				ensurePathIsSelected(rightClickedPath);
				if (label != null) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							showTreePopup(e);
						}
					});
				} else if (item != null) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							itemPopup
									.show(e.getComponent(), e.getX(), e.getY());
						}
					});
				} else { // root
					// Show root popup
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							rootPopup
									.show(e.getComponent(), e.getX(), e.getY());
						}
					});
				}
			} else {
				if (label != null) {
					if (e.getX() < panelTree.getPathBounds(path).x
							+ treeRenderer.getCheckBoxWidth()) {
						context.setLabelActive(label, !context
								.isLabelActive(label));
						treeModel.valueForPathChanged(path, label);
						throwLabelEvent();
					} else {
						labelClicked(e, path, label);
					}
				} else if (item != null) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						publishSingleSelectionEvent(item);
					}
				}
			}
		} else {
			panelTree.clearSelection();
		}
 
	}

	protected void findUnion() {
		HashMap<String, T> unionOfItems = new HashMap<String, T>();
		String[] selectedLabels = getSelectedTreesFromTree();
		if (selectedLabels.length > 1) {
			String label = JOptionPane.showInputDialog("Set Label:", "");
			if (label == null || label.length() < 1) {
				return;
			} else {
				for (int i = 0; i < selectedLabels.length; i++) {
					String nextLabel = selectedLabels[i];
					CSPanel<T> csPanel = (CSPanel<T>) context
							.getItemsWithLabel(nextLabel);
					for (int j = 0; j < csPanel.size(); j++) {
						T nextItem = panelType.cast(csPanel.get(j));
						unionOfItems.put(nextItem.getLabel(), nextItem);
					}
				}
				addCombinedPanel(unionOfItems, label, "union");
			}
		} else {
			JOptionPane.showMessageDialog(null,
					"Please select more than one set for this operation", "",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	protected void findIntersection() {
		HashMap<String, T> intersectOfItems = new HashMap<String, T>();
		HashMap<String, T> tempIntersectOfItems = new HashMap<String, T>();

		String[] selectedLabels = getSelectedTreesFromTree();
		if (selectedLabels.length > 1) {
			String label = JOptionPane.showInputDialog("Set Label:", "");
			if (label == null || label.length() < 1) {
				return;
			} else {
				int count = 0;
				for (int i = 0; i < selectedLabels.length; i++) {
					String nextLabel = selectedLabels[i];
					count++;
					if (count == 1) { // add everything from the first set
						// into temp intersect hashmap
						CSPanel<T> csPanel = (CSPanel<T>) context
								.getItemsWithLabel(nextLabel);
						for (int j = 0; j < csPanel.size(); j++) {
							T nextItem = panelType.cast(csPanel.get(j));
							intersectOfItems.put(nextItem.getLabel(), nextItem);
						}
					} else {
						tempIntersectOfItems = new HashMap<String, T>();
						tempIntersectOfItems.putAll(intersectOfItems);
						intersectOfItems = new HashMap<String, T>();
						CSPanel<T> csPanel = (CSPanel<T>) context
								.getItemsWithLabel(nextLabel);
						for (int j = 0; j < csPanel.size(); j++) {
							T nextItem = panelType.cast(csPanel.get(j));
							if (tempIntersectOfItems.containsKey(nextItem
									.getLabel()))
								intersectOfItems.put(nextItem.getLabel(),
										nextItem);
						}
					}
				}
				addCombinedPanel(intersectOfItems, label, "intersection");
			}
		} else {
			JOptionPane.showMessageDialog(null,
					"Please select more than one set for this operation", "",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	protected void findXor() {
		HashMap<String, T> allItems = new HashMap<String, T>();
		HashMap<String, Integer> countOfItems = new HashMap<String, Integer>();
		HashMap<String, T> xorOfItems = new HashMap<String, T>();
		String[] selectedLabels = getSelectedTreesFromTree();

		if (selectedLabels.length > 1) {
			String label = JOptionPane.showInputDialog("Set Label:", "");
			if (label == null || label.length() < 1) {
				return;
			} else {
				for (int i = 0; i < selectedLabels.length; i++) {
					String nextLabel = selectedLabels[i];
					CSPanel<T> csPanel = (CSPanel<T>) context
							.getItemsWithLabel(nextLabel);
					for (int j = 0; j < csPanel.size(); j++) {
						T nextItem = panelType.cast(csPanel.get(j));
						if (allItems.containsKey(nextItem.getLabel())) {
							int currCount = (Integer) countOfItems.get(nextItem
									.getLabel());
							countOfItems.put(nextItem.getLabel(), new Integer(
									currCount + 1));
						} else {
							allItems.put(nextItem.getLabel(), nextItem);
							countOfItems.put(nextItem.getLabel(),
									new Integer(1));
						}
					}
				}
				Set<Entry<String, Integer>> entrySet1 = countOfItems.entrySet();
				for (Iterator<Entry<String, Integer>> itr = entrySet1.iterator(); itr.hasNext();) {
					Map.Entry<String, Integer> me = itr.next();
					String nextKey = (String) me.getKey();
					Integer nextValue = (Integer) me.getValue();
					if (nextValue.intValue() == 1)
						xorOfItems.put(nextKey, allItems.get(nextKey));
				}
				addCombinedPanel(xorOfItems, label, "xor");
			}
		} else {
			JOptionPane.showMessageDialog(null,
					"Please select more than one set for this operation", "",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	private void addCombinedPanel(HashMap<String, T> combinedItems, String label,
			String combineSet) {
		if (combinedItems.size() == 0) {
			JOptionPane.showMessageDialog(null, "The size of the " + combineSet
					+ "-set is zero", "", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (context.indexOfLabel(label) == -1) {
			addPanel(new CSPanel<T>(label));
		}
		Map<String, T> sortedMap = new TreeMap<String, T>(combinedItems);
		Set<Entry<String, T>> entrySet = sortedMap.entrySet();
		for (Iterator<Entry<String, T>> itr = entrySet.iterator(); itr.hasNext();) {
			Map.Entry<String, T> me = itr.next();
			T item = (T) me.getValue();
			context.labelItem(item, label);
		}
		panelTree.scrollPathToVisible(new TreePath(new Object[] { context,
				label }));
		treeModel.fireLabelItemsChanged(label);
		throwLabelEvent();
	}

	protected void labelClicked(MouseEvent e, TreePath path, String label) {
		// No-op
	}

	protected JMenuItem saveOneItem = new JMenuItem("Save");
	protected JMenuItem savePanelItem = new JMenu("Save");
	private static final int savePos = 9;

	protected void showTreePopup(MouseEvent e) {
		String[] labels = getSelectedTreesFromTree();
		if (labels.length > 1) {
			treePopup.remove(saveOneItem);
			treePopup.insert(savePanelItem, savePos);
		} else {
			treePopup.insert(saveOneItem, savePos);
			treePopup.remove(savePanelItem);
		}
		treePopup.show(e.getComponent(), e.getX(), e.getY());
		if (saveOneItem.getActionListeners().length == 0) {
			helper = getSelectorHelper();
			helper.addListeners();
		}
	}

	protected void activateOrDeactivateLabelPressed(boolean value) {
		String[] labels = getSelectedTreesFromTree();
		if (labels.length > 0) {
			for (int i = 0; i < labels.length; i++) {
				context.setLabelActive(labels[i], value);
				// Notify model
				treeModel.fireLabelChanged(labels[i]);
			}
			throwLabelEvent();
		}
	}

	private void itemClicked(int index, MouseEvent e) {
		if (index != -1) {
			if (itemList != null) {
				T item = listModel.getItem(index);
				publishSingleSelectionEvent(item);
			}
		}
	}

	private void itemDoubleClicked(int index, MouseEvent e) {
		 
		// Get double-clicked item
		T item = listModel.getItem(index);
		if (context.hasLabel(item, SELECTION_LABEL)) {
			 //currentSelectedIndecies.remove(index);
			context.removeLabelFromItem(item, SELECTION_LABEL);
		} else {
			context.labelItem(item, SELECTION_LABEL);
			//currentSelectedIndecies.add(index);
		}
		treeModel.fireLabelItemsChanged(SELECTION_LABEL);

		throwLabelEvent();
		listModel.refreshItem(index);
		 
	}

	private void itemRightClicked(int index, final MouseEvent e) {
		ensureItemIsSelected(index);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				itemListPopup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	protected void removeFromLabelPressed() {
		TreePath[] paths = panelTree.getSelectionPaths();
		HashSet<String> affectedLabels = new HashSet<String>();
		for (int i = 0; i < paths.length; i++) {
			TreePath path = paths[i];
			Object obj = path.getLastPathComponent();
			if (panelType.isAssignableFrom(obj.getClass())) {
				T item = panelType.cast(obj);
				// Path must have a panel as the second-to-last component
				String label = (String) path.getParentPath()
						.getLastPathComponent();
				context.removeLabelFromItem(item, label);
				affectedLabels.add(label);
			}
		}
		for (Iterator<String> iterator = affectedLabels.iterator(); iterator
				.hasNext();) {
			String label = iterator.next();
			treeModel.fireLabelItemsChanged(label);
		}
		throwLabelEvent();
	}

	protected void addToLabelPressed() {
		T[] items = getSelectedItemsFromList();
		if (items.length > 0) {
			// Is there already a selected panel?
			String defaultLabel = getSelectedLabelFromTree();
			if (defaultLabel == null) {
				defaultLabel = "";
			}
			String label = JOptionPane.showInputDialog("Set Label:",
					defaultLabel);
			if (label == null) {
				return;
			} else if (label.indexOf("|") > -1) {
				JOptionPane
						.showMessageDialog(
								null,
								"\"|\" is a reserved character that cannot be used in an array set name.\nPlease choose other characters.",
								"Array Set Label Warning",
								JOptionPane.WARNING_MESSAGE);
			} else {
				if (context.indexOfLabel(label) == -1) {
					addPanel(new CSPanel<T>(label));
				}
				// for (int i = 0; i < items.length; i++) {
				// T item = items[i];
				// context.labelItem(item, label);
				// }
				context.labelItems(items, label);
				panelTree.scrollPathToVisible(new TreePath(new Object[] {
						context, label }));
				treeModel.fireLabelItemsChanged(label);
				throwLabelEvent();
			}
		}
	}

	protected void clearSelectionPressed() {
		context.clearItemsFromLabel(SELECTION_LABEL);
		itemAutoList.getList().repaint();
		treeModel.fireLabelItemsChanged(SELECTION_LABEL);
		throwLabelEvent();
	}

	/**
	 * Only effects the right-clicked path, not the entire selection
	 */
	protected void renameLabelPressed(TreePath path) {
		String oldLabel = getLabelForPath(path);
		if (oldLabel != null) {
			if (SELECTION_LABEL.equals(oldLabel)) {
				JOptionPane.showMessageDialog(getComponent(),
						"This set is built in and cannot be renamed.");
			} else {
				String newLabel = JOptionPane.showInputDialog("New Label:",
						oldLabel);
				if (newLabel != null) {
					if (context.labelExists(newLabel)) {
						JOptionPane.showMessageDialog(getComponent(),
								"A set already exists with that name.");
					} else {
						context.renameLabel(oldLabel, newLabel);
						treeModel.fireLabelChanged(newLabel);
						throwLabelEvent();
					}
				}
			}
		}
	}

	protected void copyLabelPressed(TreePath path) {
		String oldLabel = getLabelForPath(path);
		
		if (oldLabel != null) {
			String newLabel = JOptionPane.showInputDialog("New Label:",
					oldLabel);
			if (newLabel != null) {
				if (context.labelExists(newLabel)) {
					JOptionPane.showMessageDialog(getComponent(),
							"A set already exists with this name.");
				} else {
					context.addLabel(newLabel);

					HashMap<String, T> copyOfItems = new HashMap<String, T>();
					CSPanel<T> csPanelSource = (CSPanel<T>) context
										.getItemsWithLabel(oldLabel);

					for (int j = 0; j < csPanelSource.size(); j++) {
						T nextItem = panelType.cast(csPanelSource.get(j));
						copyOfItems.put(nextItem.getLabel(), nextItem);
					}

					addCombinedPanel(copyOfItems, newLabel, "copy");
						
					treeModel.fireTreeStructureChanged();
					throwLabelEvent();
				}
			}
		}
	}

	protected void deletePanelPressed() {
		String[] labels = getSelectedTreesFromTree();
		if (labels.length > 0) {
			int confirm = JOptionPane.showConfirmDialog(getComponent(),
					"Delete selected set" + (labels.length > 1 ? "s" : "")
							+ "?", "Confirm to delete", JOptionPane.OK_CANCEL_OPTION);
			if (confirm == JOptionPane.OK_OPTION) {
				panelTree.clearSelection();
				for (int i = 0; i < labels.length; i++) {
					String label = labels[i];
					// Cannot delete root label or selection label
					if (!SELECTION_LABEL.equals(label)) {
						removePanel(label);
					}
				}
				throwLabelEvent();
			}
		}
	}

	protected void printPanelPressed(TreePath path) {
		String label = getLabelForPath(path);
		if (label != null) {
			// Get a PrinterJob
			PrinterJob job = PrinterJob.getPrinterJob();			
			// Put up the dialog box
			if (job.printDialog()) {
				// Print the job if the user didn't cancel printing
				try {					
					// Ask user for page format (e.g., portrait/landscape)
					PageFormat pf = job.pageDialog(job.defaultPage());
					// Specify the Printable is an instance of
					// PrintListingPainter; also provide given PageFormat
					job.setPrintable(new PrintListingPainter(label), pf);
					// Print 1 copy
					job.setCopies(1);
					job.print();			
				} catch (Exception pe) {					
					pe.printStackTrace();	
				}
			}
		}
	}

	/**
	 * Only effects the right-clicked path, not the entire selection
	 */
	protected void visualPropertiesPressed(TreePath path) {
		DSAnnotationContextManager contextManager = CSAnnotationContextManager
				.getInstance();
		String label = getLabelForPath(path);
		DSPanel<T> panel = contextManager.getCurrentContext(itemList)
				.getItemsWithLabel(label);
		if (panel != null) {			 
			VisualPropertiesDialog dialog = new VisualPropertiesDialog(null,
					"Change Visual Properties", panel, getPanelIndex(panel));
			dialog.pack();
			dialog.setSize(600, 600);
			dialog.setVisible(true);
			if (dialog.isPropertiesChanged()) {
				PanelVisualPropertiesManager manager = PanelVisualPropertiesManager
						.getInstance();
				PanelVisualProperties visualProperties = dialog
						.getVisualProperties();
				if (visualProperties == null) {
					manager.clearVisualProperties(panel);
				} else {
					manager.setVisualProperties(panel, visualProperties);
				}
				throwLabelEvent();
			}
		}
	}
	
	protected int getPanelIndex(DSPanel<T> panel)
	{
		int index = 0;
		String label = panel.getLabel();
		if (context.isLabelActive(label)) {
			int n = context.getNumberOfLabels();
			for (int i = 0; i < n; i++) {
				String l = context.getLabel(i);
				if (context.isLabelActive(l)) {
					index++;
				}
				if (label.equals(l)) {
					break;
				}
			}
		}
		
		return index;
	}
	
	/**
	 * Convenience method to get all the selected items in the item list.
	 */
	@SuppressWarnings("unchecked")
	private T[] getSelectedItemsFromList() {
		int[] indices = itemAutoList.getList().getSelectedIndices();
		int n = indices.length;
		T[] items = (T[]) Array.newInstance(panelType, n);
		for (int i = 0; i < n; i++) {
			items[i] = listModel.getItem(indices[i]);
		}
		return items;
	}

	/**
	 * Convenience method to get all the selected panels in the panel tree.
	 */
	protected String[] getSelectedTreesFromTree() {
		TreePath[] paths = panelTree.getSelectionPaths();
		if(paths==null)return new String[0];
		
		int n = paths.length;
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < n; i++) {
			TreePath path = paths[i];
			Object obj = path.getLastPathComponent();
			if (obj instanceof String) {
				list.add((String) obj);
			}
		}
		return list.toArray(new String[] {});
	}

	/**
	 * Gets the single panel selected from the tree if there is one
	 */
	private String getSelectedLabelFromTree() {
		TreePath path = panelTree.getSelectionPath();
		if (path == null) {
			return null;
		} else {
			Object obj = path.getLastPathComponent();
			if (obj instanceof String) {
				return (String) obj;
			} else {
				return null;
			}
		}
	}

	protected void dataSetCleared() {
		treeModel.setContext(emptyContext);
		itemList = emptyList;
		itemAutoList.getList().repaint();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				treeModel.fireTreeStructureChanged();
			}
		});
		contextSelector.removeAllItems();
		newContextButton.setEnabled(false);
		// throwLabelEvent();
	}

	protected abstract boolean dataSetChanged(DSDataSet<? extends DSBioObject> dataSet);

	protected abstract void throwLabelEvent();

	public Component getComponent() {
		return mainPanel;
	}

	/**
	 * Called when a data set is selected or cleared in the project panel.
	 */
	@Subscribe(Asynchronous.class)
	public void receive(ProjectEvent projectEvent, Object source) {

		DSDataSet<?> dataSet = projectEvent.getDataSet();
		boolean processed = false;
		if (dataSet != null) {
			processed = dataSetChanged(dataSet);
		} else {
			dataSetCleared();
		}
		if (!processed) {
			TreeNode parent = null;
			ProjectTreeNode treeNode = projectEvent.getTreeNode();
			if (treeNode instanceof DataSetSubNode) {
				DataSetSubNode subNode = (DataSetSubNode) treeNode;
				parent = subNode.getParent();
			}
			if (parent instanceof DataSetNode) {
				dataSet = ((DataSetNode) parent).getDataset();
				dataSetChanged(dataSet);
				itemAutoList.getList().clearSelection();
			}
		} else {
			itemAutoList.getList().clearSelection();
		}
	}

	/**
	 * Called when a component wishes to add, change or remove a panel.
	 */
	@Subscribe(Overflow.class)
	public void receive(org.geworkbench.events.SubpanelChangedEvent<T> spe,
			Object source) {
		if (panelType.isAssignableFrom(spe.getType())) {
			DSPanel<T> receivedPanel = spe.getPanel();
			String panelName = receivedPanel.getLabel();
			switch (spe.getMode()) {
			case SubpanelChangedEvent.NEW: {
				if (context.indexOfLabel(panelName) != -1) {
					int number = 1;
					String newName = panelName + " (" + number + ")";
					receivedPanel.setLabel(newName);
					while (context.indexOfLabel(newName) != -1) {
						number++;
						newName = panelName + " (" + number + ")";
						receivedPanel.setLabel(newName);
					}
				}
				addPanel(receivedPanel);
				throwLabelEvent();
				break;
			}
			case SubpanelChangedEvent.SET_CONTENTS: {
				boolean foundPanel = false;
				if (context.indexOfLabel(panelName) != -1) {
					foundPanel = true;
					// Delete everything from the panel and re-add
					context.clearItemsFromLabel(panelName);
					context.labelItems(receivedPanel, panelName);
					synchronized (treeModel) {
						treeModel.fireLabelItemsChanged(panelName);
					}
					throwLabelEvent();
				}
				if (!foundPanel) {
					// Add it as a new panel
					addPanel(receivedPanel);
				}
				break;
			}
			case SubpanelChangedEvent.DELETE: {
				if (context.indexOfLabel(panelName) != -1) {
					int index = context.indexOfLabel(panelName);
					context.removeLabel(panelName);
					treeModel.fireLabelRemoved(panelName, index);
				}
				break;
			}
			default:
				throw new RuntimeException(
						"Unknown subpanel changed event mode: " + spe.getMode());
			}
		}
	}

	protected abstract void publishSingleSelectionEvent(T item);

	/**
	 * Printable that is responsible for printing the contents of a panel.
	 */
	private class PrintListingPainter implements Printable {
		private Font fnt = new Font("Helvetica", Font.PLAIN, 8);
		private int rememberedPageIndex = -1;
		private boolean rememberedEOF = false;
		private int index = 0;
		private int lastIndex = 0;
		DSPanel<T> panel;

		public PrintListingPainter(String label) {
			this.panel = context.getItemsWithLabel(label);
		}

		/**
		 * Called by the print job.
		 */
		public int print(Graphics g, PageFormat pf, int pageIndex)
				throws PrinterException {
			try {
				int itemNo = Math.min(panel.size(), 500);
				// For catching IOException
				if (pageIndex != rememberedPageIndex) {
					// First time we've visited this page
					rememberedPageIndex = pageIndex;
					lastIndex = index;
					// If encountered EOF on previous page, done
					if (rememberedEOF) {
						return Printable.NO_SUCH_PAGE;
					}
					// Save current position in input file
				} else {
					index = lastIndex;
				}
				g.setColor(Color.black);

				int x = (int) pf.getImageableX() + 10;
				int y = (int) pf.getImageableY() + 12;

				// Put the panel name as a title
				g.setFont(new Font("Arial", Font.PLAIN, 16));
				g.drawString(panel.getLabel(), x, y);

				// Now do the rest
				g.setFont(fnt);
				y += 36;
				while (y + 12 < pf.getImageableY() + pf.getImageableHeight()) {
					if (index >= itemNo) {
						rememberedEOF = true;
						break;
					}
					DSSequential gm = panel.get(index);
					String line = "[" + gm.getSerial() + "]";
					g.drawString(line, x, y);
					g.drawString(gm.getLabel(), x + 30, y);
					g.drawString(gm.toString(), x + 160, y);
					y += 12;
					index++;
				}
				return Printable.PAGE_EXISTS;
			} catch (Exception e) {
				return Printable.NO_SUCH_PAGE;
			}
		}
	}

	/**
	 * List Model backed by the item list.
	 */
	protected class FilterListModel extends AbstractListModel {
		private static final long serialVersionUID = -6491855100913408738L;
		private ArrayList<T> filterItems=null;
		private String searchText="";

		public FilterListModel() {
			super();
			filterItems = new ArrayList<T>();
		}
		public int getSize() {
			synchronized(filterItems) {
				if (itemList == null)     return 0;
				if (searchText.length()==0)  return itemList.size();
				return filterItems.size();
			}
		}
		public Object getElementAt(int index) {
			synchronized(filterItems) {
				if (itemList == null)      return null;
				if (searchText.length()==0 && index<itemList.size())   return itemList.get(index);
				if (index<filterItems.size())  return filterItems.get(index);
				return null;
			}
		}
		private T getItem(int index) {
			synchronized(filterItems) {
				if (itemList == null)      return null;
				if (searchText.length()==0 && index<itemList.size())   return itemList.get(index);
				if (index<filterItems.size())  return filterItems.get(index);
				return null;
			}
		}

		/**
		 * Indicates to the associated JList that the contents need to be
		 * redrawn.
		 */
		public void refresh() {
			if (itemList == null) {
				fireContentsChanged(this, 0, 0);
			} else {
				refilter();
			}
		}

		public void refreshItem(int index) {
			fireContentsChanged(this, index, index);
		}
		
		private void refilter() {
			synchronized(filterItems) {
				filterItems.clear();
				searchText = itemAutoList.getFilterString();
	
				for (T item : itemList) {
					if (item.toString().toLowerCase().indexOf(searchText, 0) != -1) {
						filterItems.add(item);
					}
				}
				fireContentsChanged(this, 0, getSize());
			}
		}
	}

	/**
	 * Auto-scrolling list for items that customizes the double-click and
	 * right-click behavior.
	 */
	protected class ItemList extends org.geworkbench.util.JAutoList {
		private static final long serialVersionUID = -4147380829426347621L;
	    private JToggleButton jToolTipToggleButton = new JToggleButton();
	    private boolean showToolTip = false;
	    private static final int iconsize = 28;
		private JTextField filterField;

		public ItemList(ListModel model) {
			super(model);
			setList(model);
	        topPanel.add(jToolTipToggleButton);

	        jToolTipToggleButton.setPreferredSize(new Dimension(iconsize, iconsize));
	        jToolTipToggleButton.setToolTipText("Toggle name tooltips");
	        jToolTipToggleButton.setSelected(false);
	        jToolTipToggleButton.setIcon(new ImageIcon(this.getClass().getResource("bulb_icon_grey.gif")));
	        jToolTipToggleButton.setSelectedIcon(new ImageIcon(this.getClass().getResource("bulb_icon_gold.gif")));
	        jToolTipToggleButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                if (jToolTipToggleButton.isSelected()) 
	                	showToolTip = true;
	                else showToolTip = false;
	            }
	        });
		}

		@Override
		protected void elementDoubleClicked(int index, MouseEvent e) {
			itemDoubleClicked(index, e);
		}

		@Override
		protected void elementRightClicked(int index, MouseEvent e) {
			itemRightClicked(index, e);
		}

		@Override
		protected void elementClicked(int index, MouseEvent e) {
			itemClicked(index, e);
		}

		public String getFilterString(){
			return filterField.getText().toLowerCase();
		}

		public void clearFilterString(){
			filterField.setText("");
		}

		private void setList(ListModel model) {
			list = new JList(model) {
				private static final long serialVersionUID = 7273196340245426337L;
				public String getToolTipText(MouseEvent e) {
	        		int i = locationToIndex(e.getPoint());
					if (!showToolTip || i < 0) return null;
	        		String text = getModel().getElementAt(i).toString();
	        		text = "<html>"+text+"</html>";
	        		if (text.indexOf(": (") > -1) {
	        			text = text.replaceFirst(": \\(", "<br>");
	        			text = text.replaceFirst("\\)", "<br>");
	        		} else
	        			text = text.replaceFirst(": ", "<br>");
	        		text = text.replaceAll("; ", "<br>");
	        		return text;
	        	}
	        };
	        filterField = new JTextField();
			filterField.getDocument().addDocumentListener(new DocumentListener(){
		        @SuppressWarnings("unchecked")
				public void changedUpdate (DocumentEvent e) {((FilterListModel)getModel()).refilter();}
		        @SuppressWarnings("unchecked")
				public void insertUpdate (DocumentEvent e) {((FilterListModel)getModel()).refilter();}
		        @SuppressWarnings("unchecked")
				public void removeUpdate (DocumentEvent e) {((FilterListModel)getModel()).refilter();}
			});
			topPanel.removeAll();
			topPanel.add(new JLabel(SEARCH_LABEL_TEXT));
	        topPanel.add(filterField);
	        revalidate(); repaint();
	        scrollPane.getViewport().setView(list);
	        list.addMouseListener(new MouseAdapter() {
	            @Override public void mouseReleased(MouseEvent e) {
	                handleMouseEvent(e);
	            }
	        });
		}
	}

	protected class ListCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -6661833903314561013L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			JLabel component = (JLabel) super.getListCellRendererComponent(
					list, value, index, isSelected, cellHasFocus);
			if (context.hasLabel(listModel.getItem(index), SELECTION_LABEL)) {
				Font font = component.getFont();
				Font boldFont = font.deriveFont(Font.BOLD);
				component.setFont(boldFont);
			}

			return component;
		}
	}

	public DSAnnotationContext<T> getContext() {
		return context;
	}

	public void setTreeRenderer(SelectorTreeRenderer treeRenderer) {
		this.treeRenderer = treeRenderer;
		panelTree.setCellRenderer(treeRenderer);
	}

	protected boolean resetContextMode = false;

	public void setItemList(DSItemList<T> itemList) {
		resetContextMode = true;
		try {
			this.itemList = itemList;
			CSAnnotationContextManager manager = CSAnnotationContextManager
					.getInstance();
			context = manager.getCurrentContext(itemList);
			initializeContext(context);
			contextSelector.removeAllItems();
			int n = manager.getNumberOfContexts(itemList);
			for (int i = 0; i < n; i++) {
				DSAnnotationContext<T> aContext = manager.getContext(itemList, i);
				contextSelector.addItem(aContext);
				if (aContext == context) {
					contextSelector.setSelectedIndex(i);
				}
			}
			newContextButton.setEnabled(true);
			// Refresh list
			listModel.refresh();
			// Refresh tree
			treeModel.setContext(context);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					treeModel.fireTreeStructureChanged();
				}
			});
			throwLabelEvent();
		} finally {
			resetContextMode = false;
		}
	}

	protected void createNewContext() {
		String name = JOptionPane.showInputDialog("New group name:");
		if( name==null || name.length()==0 )
			return;

		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		if (manager.hasContext(itemList, name)) {
			JOptionPane.showMessageDialog(mainPanel, "Group already exists.");
		} else {
			context = manager.createContext(itemList, name);
			initializeContext(context);
			contextSelector.addItem(context);
			contextSelector.setSelectedItem(context);
			manager.setCurrentContext(itemList, context);
			// Refresh list
			listModel.refresh();
			// Refresh tree
			treeModel.setContext(context);
			treeModel.fireTreeStructureChanged();
			throwLabelEvent();
		}
	}

	protected void deleteContext() {

		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();

		String contextName = context.getName();

		if (contextName.equalsIgnoreCase("Default")){
			JOptionPane.showMessageDialog(mainPanel, "You cannot delete the Default group.");
			return;
		}
		
		int confirm = JOptionPane.showConfirmDialog(getComponent(),
				"Delete Group: " + contextName + " ?");

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}
		
		if (!manager.hasContext(itemList, contextName)) {
			JOptionPane.showMessageDialog(mainPanel, "Group does not exists.");
		}else {
			contextSelector.setSelectedItem(context);
			manager.removeContext(itemList, contextName);
			contextSelector.removeItem(context);            
			context = manager.getContext(itemList, 0);
			contextSelector.setSelectedItem(context);
			
			manager.setCurrentContext(itemList, context);
			// Refresh list
			listModel.refresh();
			// Refresh tree
			treeModel.setContext(context);
			treeModel.fireTreeStructureChanged();
			throwLabelEvent();
		}
	}

	protected void switchContext(DSAnnotationContext<T> newContext) {
		if (!resetContextMode && (newContext != null)) {
			context = newContext;
			contextSelector.setSelectedItem(context);
			DSAnnotationContextManager manager = CSAnnotationContextManager
					.getInstance();
			manager.setCurrentContext(itemList, context);
			// Refresh list
			listModel.refresh();
			// Refresh tree
			treeModel.setContext(context);
			treeModel.fireTreeStructureChanged();
			throwLabelEvent();
		}
	}

	protected void initializeContext(DSAnnotationContext<?> context) {
		context.addLabel(SELECTION_LABEL);
	}

	public ActionListener getActionListener(String var) {
		return menuListeners.get(var);
	}

}
