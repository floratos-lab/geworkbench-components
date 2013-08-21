package org.geworkbench.components.microarrays;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File; 
import java.io.FileNotFoundException; 
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.builtin.projects.SaveFileFilterFactory;
import org.geworkbench.builtin.projects.SaveFileFilterFactory.TabDelimitedFileFilter;

/**
 * 
 * <p>
 * Copyright Columbia University
 * </p>
 * 
 * @author Adam Margolin
 * @author zji
 * @version $Id$
 */
@AcceptTypes({ DSMicroarraySet.class })
public class TabularMicroarrayPanel extends MicroarrayViewEventBase {

	private static Log log = LogFactory.getLog(TabularMicroarrayPanel.class);

	private final String NUMBERS = "Number";
	private final String SCIENTIFIC = "Scientific";

	private String selectedFormat = null;
	private int decimalPlaces;
	private NumberFormat nf = null;
	private DecimalFormat sf = null;

	public TabularMicroarrayPanel() {
		try {
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static TableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

	private TableCellRenderer renderer = new DefaultTableCellRenderer() {
		private static final long serialVersionUID = -2148986327315654796L;

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int col) {
			Component defaultComponent = defaultRenderer
					.getTableCellRendererComponent(table, value, isSelected,
							hasFocus, row, col);
			Component c = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, col);

			/* Convert the model index to the visual index */
			int modelCol = table.convertColumnIndexToModel(col);

			// note: because c is in fact reused, we need to reset the
			// properties that may be changed
			if (modelCol == 0) {
				if (!isSelected) {
					c.setBackground(Color.lightGray);
					c.setForeground(defaultComponent.getForeground());
				}
				((JLabel) c).setHorizontalAlignment(JLabel.LEFT);
				((JComponent) c).setBorder(BorderFactory
						.createLineBorder(Color.white));
			} else {
				DSGeneMarker stats = uniqueMarkers.get(row);
				if (stats != null) {
					DSMarkerValue marker = maSetView.items().get(modelCol - 1)
							.getMarkerValue(stats.getSerial());
					if (marker.isMissing()) {
						c.setBackground(Color.yellow);
						c.setForeground(Color.blue);
					} else if (marker.isMasked()) {
						c.setBackground(Color.pink);
						c.setForeground(Color.blue);
					} else {
						c.setBackground(defaultComponent.getBackground());
						c.setForeground(defaultComponent.getForeground());
					}
					((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
				}
			}
			return c;
		}
	};

	private AbstractTableModel microarrayTableModel = new AbstractTableModel() {
		private static final long serialVersionUID = -6400581862850298421L;

		public int getColumnCount() {
			if (maSetView == null || refMASet == null) {
				return 0;
			} else {
				int cCount = maSetView.items().size() + 1;
				return cCount;
			}
		}

		public int getRowCount() {
			if (uniqueMarkers == null) {
				return 0;
			} else {
				return uniqueMarkers.size();
			}
		}

		public Object getValueAt(int row, int col) {
			if (row >= uniqueMarkers.size()) {
				return "";
			}

			DSGeneMarker marker = uniqueMarkers.get(row);
			if (marker != null) {
				if (col == 0) {
					return marker.toString();
				} else {
					DSMicroarray array = maSetView.get(col - 1);
					DSMarkerValue value = array.getMarkerValue(marker);

					if (value == null) return "Invalid";
					if (value.isMissing())
						return "n/a";
					else {
						if (selectedFormat.equals(NUMBERS))
							return nf.format(maSetView.getValue(row, col - 1));
						else
							return sf.format(maSetView.getValue(row, col - 1));
					}
				}
			} else {
				return "Invalid";
			}
		}

		public String getColumnName(int col) {
			if (col == 0) {
				return "Marker";
			} else {
				return maSetView.items().get(col - 1).getLabel();
			}
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	};

	private JTable jTable1 = null;

	private void initialize() {
		JScrollPane jScrollPane1 = new JScrollPane();
		JToolBar jToolBar1 = new JToolBar();
		JCheckBox jShowAllMArrays = new JCheckBox();
		JCheckBox jShowAllMarkers = new JCheckBox();

		JRadioButton jrbNumbers = new JRadioButton(NUMBERS);
		jrbNumbers.setSelected(true);
		JRadioButton jrbScientific = new JRadioButton(SCIENTIFIC);
		ButtonGroup group = new ButtonGroup();
		group.add(jrbNumbers);
		group.add(jrbScientific);

		ActionListener radioActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton aButton = (AbstractButton) actionEvent
						.getSource();
				if (aButton.getText().equals(NUMBERS)) {
					selectedFormat = NUMBERS;
				} else {
					selectedFormat = SCIENTIFIC;
				}
				microarrayTableModel.fireTableDataChanged();
			}
		};

		jrbNumbers.addActionListener(radioActionListener);
		jrbScientific.addActionListener(radioActionListener);

		JLabel labelFormat = new JLabel("                      Format: ");
		JLabel labelBlank = new JLabel("  ");
		JLabel labelDecimalPlaces = new JLabel("   Decimal places:");
		JSpinner jspDecimalPlaces = new JSpinner(new SpinnerNumberModel(2, 0,
				30, 1));

		jspDecimalPlaces
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						decimalPlaces = ((Integer) ((JSpinner) e.getSource())
								.getValue()).intValue();

						nf.setMinimumFractionDigits(decimalPlaces);
						nf.setMaximumFractionDigits(decimalPlaces);

						String s = "";
						for (int i = 0; i < decimalPlaces; i++) {
							s = s + "#";
						}

						sf.applyPattern("0." + s + "E0");
						sf.setMinimumFractionDigits(decimalPlaces);

						microarrayTableModel.fireTableDataChanged();

					}
				});

		jTable1 = new JTable(microarrayTableModel) {
			private static final long serialVersionUID = 8762298664180020948L;

			public Dimension getPreferredScrollableViewportSize() {
				return new Dimension(20 * getModel().getColumnCount(),
						20 * getModel().getRowCount());
			}

			public boolean getScrollableTracksViewportWidth() {
				return false;
			}
		};

		jTable1.setEnabled(true);
		jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jTable1.setDefaultRenderer(Object.class, renderer);
		jTable1.setCellSelectionEnabled(true);
		jTable1.getColumnModel().addColumnModelListener(
				new ColumnOrderTableModelListener(jTable1));

		jToolBar1.setBorder(null);
		mainPanel.add(jScrollPane1, BorderLayout.CENTER);
		jToolBar1.add(jShowAllMArrays, null);
		jToolBar1.add(jShowAllMarkers, null);
		jScrollPane1.getViewport().add(jTable1, null);

		jToolBar3.add(labelFormat);
		jToolBar3.add(jrbNumbers);
		jToolBar3.add(labelBlank);
		jToolBar3.add(jrbScientific);
		jToolBar3.add(labelDecimalPlaces);
		jToolBar3.add(jspDecimalPlaces);

		JButton button = new JButton("Export");
		jToolBar3.add(button);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				TabDelimitedFileFilter tabDelimitedFileFilter = SaveFileFilterFactory
						.getTabDelimitedFileFilter();
				fileChooser.setFileFilter(tabDelimitedFileFilter);
				int ret = fileChooser
						.showSaveDialog(TabularMicroarrayPanel.this
								.getComponent());

				if (ret == JFileChooser.APPROVE_OPTION) {
					String newFileName = fileChooser.getSelectedFile()
							.getAbsolutePath();

					if (fileChooser.getFileFilter().getDescription().equals(tabDelimitedFileFilter.getDescription()))
					{
						if (!tabDelimitedFileFilter.accept(new File(newFileName)))
						newFileName += "." + tabDelimitedFileFilter.getExtension();
					}
					
					File file = new File(newFileName);
					if (file.exists()) {
						int o = JOptionPane.showConfirmDialog(null, 
								"The file already exists. Do you wish to overwrite it?",
								"Replace the existing file?",
								JOptionPane.YES_NO_OPTION);
						if (o != JOptionPane.YES_OPTION) {
							return;
						}
					}					
					
					try {
						DSItemList<DSMicroarray> arrays = maSetView.items();
						PrintWriter pw = new PrintWriter(file);
						pw.print("ID");
						for (int i = 0; i < arrays.size(); i++) {
							pw.print("\t" + arrays.get(i));
						}
						pw.println();
						for (int index = 0; index < maSetView.markers().size(); index++) {
							double[] v = maSetView.getRow(index);
							pw.print(maSetView.markers().get(index).getLabel());
							for (int i = 0; i < v.length; i++) {
								pw.print("\t" + new Float(v[i]));
							}
							pw.println();
						}
						pw.close();
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		selectedFormat = NUMBERS;
		decimalPlaces = 2;
		nf = NumberFormat.getInstance();
		nf.setRoundingMode(RoundingMode.HALF_UP);
		nf.setMinimumFractionDigits(decimalPlaces);
		nf.setMaximumFractionDigits(decimalPlaces);
		sf = new DecimalFormat("0.##E0");
		sf.setRoundingMode(RoundingMode.HALF_UP);
		sf.setMinimumFractionDigits(decimalPlaces);

	}

	@Override
	protected void fireModelChangedEvent() {
		microarrayTableModel.fireTableStructureChanged();
		log.debug("fireModelChangedEvent");
		if (activatedArrays != null && activatedArrays.panels().size() > 0
				&& activatedArrays.size() > 0)
			return;

		ArrayList<Integer> columnOrder = new ArrayList<Integer>();

		if (maSetView != null && maSetView.getDataSet() != null
				&& !maSetView.getDataSet().getColumnOrder().isEmpty())
			columnOrder = maSetView.getDataSet().getColumnOrder();

		if (!columnOrder.isEmpty()) {
			int columncount = jTable1.getColumnCount();
			for (int i = 0; i < columncount; i++)
				jTable1.addColumn(jTable1.getColumnModel().getColumn(
						columnOrder.get(i)));

			for (int i = 0; i < columncount; i++)
				jTable1.removeColumn(jTable1.getColumnModel().getColumn(0));
		}
	}

	private class ColumnOrderTableModelListener implements
			TableColumnModelListener {
		private JTable table;

		public ColumnOrderTableModelListener(JTable mytable) {
			table = mytable;
		}

		@Override
		public void columnAdded(TableColumnModelEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void columnMarginChanged(ChangeEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void columnMoved(TableColumnModelEvent e) {
			// TODO Auto-generated method stub
			int from = e.getFromIndex();
			int to = e.getToIndex();
			if (from == to)
				return;
			if (activatedArrays != null && activatedArrays.panels().size() > 0
					&& activatedArrays.size() > 0)
				return;

			ArrayList<Integer> columnOrder = new ArrayList<Integer>();
			int columncount = table.getColumnCount();
			for (int i = 0; i < columncount; i++)
				columnOrder.add(table.convertColumnIndexToModel(i));
			maSetView.getDataSet().setColumnOrder(columnOrder);
		}

		@Override
		public void columnRemoved(TableColumnModelEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void columnSelectionChanged(ListSelectionEvent e) {
			// TODO Auto-generated method stub

		}
	}

}
