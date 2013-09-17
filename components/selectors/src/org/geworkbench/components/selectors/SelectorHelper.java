package org.geworkbench.components.selectors;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;

import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.properties.DSSequential;
import org.geworkbench.components.selectors.GenePanel.MarkerPanelSetFileFilter;
import org.geworkbench.util.FilePathnameUtils;

import com.Ostermiller.util.CSVPrinter;
/**
 * Helper class for saving marker or array sets in selector panel
 * $Id$
 */
public class SelectorHelper<T extends DSSequential> {

	private static String osname = System.getProperty("os.name").toLowerCase();
    private final static boolean is_mac = (osname.indexOf("mac") > -1);
	private SelectorPanel<T> sp = null;

	public SelectorHelper(SelectorPanel<T> panel) {
		sp = panel;
		sp.setSelectorLastDirConf();
		sp.lastDir = getLastDataDirectory();
	}

	public String getLastDataDirectory() {
		String dir = FilePathnameUtils.getDataFilesDirPath();
		try {
			File file = new File(sp.selectorLastDirConf);
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				dir = br.readLine();
				br.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return dir;
	}

	public void setLastDataDirectory(String dir) {
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter(
					sp.selectorLastDirConf));
			br.write(dir);
			br.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static class DirectoryFileFilter extends
			javax.swing.filechooser.FileFilter {
		public String getDescription() {
			return "Directory Only";
		}

		public boolean accept(File f) {
			return f.isDirectory();
		}
	}

	private static void searchNDestroy(Container cont) {
		if (!(cont instanceof Container)) {
			return;
		}
		int n = cont.getComponentCount();
		for (int i = 0; i < n; i++) {
			JComponent comp;
			try {
				comp = (JComponent) cont.getComponent(i);
			} catch (Exception e) {
				continue;
			}
			if (comp instanceof JLabel) {
				JLabel lbl = (JLabel) comp;
				if (lbl.getText().startsWith("File Name")
						|| lbl.getText().startsWith("File:")) {
					cont.setVisible(false);
					continue;
				}
			} else if (is_mac && comp instanceof JTextField) {
				JTextField tf = (JTextField) comp;
				tf.setText(".");
			}
			if (comp instanceof Container) {
				try {
					searchNDestroy((Container) comp);
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * Utility to save a panel to the filesystem as CSV. <p/> Format: <p/> File
	 * name (without .CSV extension) is the name of the panel. <p/> Rows of the
	 * file contains the label of markers, in order. Only the first column is
	 * used.
	 * 
	 * @param filename
	 *            filename to which the current panel is to be saved.
	 */
	private void serializePanel(String filename, String[] labels) {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(filename);
			CSVPrinter out = new CSVPrinter(fileWriter);
			TreeSet<String> set = new TreeSet<String>();
			for (int i = 0; i < labels.length; i++) {
				DSPanel<T> panel = sp.context.getItemsWithLabel(labels[i]);
				if (panel != null && panel.size() > 0) {
					for (int j = 0; j < panel.size(); j++) {
						set.add(panel.get(j).getLabel());
					}
				}
			}
			for (Iterator<String> it = set.iterator(); it.hasNext();) {
				out.println(it.next());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					// Lost cause-- ignore
				}
			}
		}
	}

	public void saveMergePressed(TreePath path, String title) {
		String[] labels = sp.getSelectedTreesFromTree();
		if (labels != null && labels.length > 0) {
			JFileChooser fc = new JFileChooser(".");
			if (!sp.lastDir.equals("")) {
				fc.setCurrentDirectory(new File(sp.lastDir));
			}
			FileFilter filter = new MarkerPanelSetFileFilter();
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(filter);
			fc.setDialogTitle(title);
			String extension = ((MarkerPanelSetFileFilter) filter)
					.getExtension();
			int choice = fc.showSaveDialog(sp.mainPanel.getParent());
			if (choice == JFileChooser.APPROVE_OPTION) {
				String filename = fc.getSelectedFile().getAbsolutePath();
				if (!filename.endsWith(extension)) {
					filename += extension;
				}
				boolean confirmed = true;
				if (new File(filename).exists()) {
					int confirm = JOptionPane.showConfirmDialog(sp.getComponent(),
							"Replace existing file?");
					if (confirm != JOptionPane.YES_OPTION) {
						confirmed = false;
					}
				}
				if (confirmed) {
					sp.lastDir = filename;
					try {
						setLastDataDirectory(fc.getCurrentDirectory()
								.getCanonicalPath());
					} catch (Exception e) {
						e.printStackTrace();
					}
					serializePanel(filename, labels);
				}
			}
		}
	}

	public void saveMultiPressed(TreePath path) {
		String[] labels = sp.getSelectedTreesFromTree();
		if (labels != null && labels.length > 0) {
			JFileChooser fc = new JFileChooser(".");
			searchNDestroy(fc);
			if (!sp.lastDir.equals("")) {
				fc.setCurrentDirectory(new File(sp.lastDir));
			}
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			FileFilter filter = new MarkerPanelSetFileFilter();
			FileFilter dff = new DirectoryFileFilter();
			fc.setFileFilter(dff);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setDialogTitle("Save Multiple Marker Sets");
			String extension = ((MarkerPanelSetFileFilter) filter)
					.getExtension();
			int choice = fc.showSaveDialog(sp.mainPanel.getParent());
			if (choice == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fc.getSelectedFile();
				if (is_mac) {
					while (selectedFile.getName().equals(".")) {
						selectedFile = selectedFile.getParentFile();
					}
					if (selectedFile.getName().equals(
							selectedFile.getParentFile().getName())) {
						selectedFile = selectedFile.getParentFile();
					}
				}
				String pathname = selectedFile.getAbsolutePath();
				if (!selectedFile.isDirectory()) {
					JOptionPane.showMessageDialog(null,
							"Not a valid directory!", "Warning",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				boolean confirmedAll = false;
				for (String label : labels) {
					boolean confirmed = true;
					String filename = pathname
							+ FilePathnameUtils.FILE_SEPARATOR + label;
					if (!filename.endsWith(extension)) {
						filename += extension;
					}
					if (new File(filename).exists() && !confirmedAll) {
						int confirm = JOptionPane.showOptionDialog(
								sp.getComponent(), "Replace existing file "
										+ filename + "?", "File Exists",
								JOptionPane.DEFAULT_OPTION,
								JOptionPane.QUESTION_MESSAGE, null,
								new String[] { "Yes", "Yes to All", "No",
										"Cancel" }, "No");
						if (confirm == 1)
							confirmedAll = true;
						else if (confirm == 2)
							confirmed = false;
						else if (confirm == 3)
							break;
					}
					if (confirmed) {
						sp.lastDir = filename;
						try {
							setLastDataDirectory(selectedFile
									.getCanonicalPath());
						} catch (Exception e) {
							e.printStackTrace();
						}

						String[] labelSet = { label };
						serializePanel(filename, labelSet);
					}
				}
			}
		}
	}
	public void addListeners() {
		sp.saveOneItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveMergePressed(sp.rightClickedPath, "Save "+sp.typeName+" Set");
			}
		});
		sp.saveMergeSets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveMergePressed(sp.rightClickedPath, "Save Merged "+sp.typeName+" Set");
			}
		});
		sp.saveMultiSets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveMultiPressed(sp.rightClickedPath);
			}
		});
	}
}