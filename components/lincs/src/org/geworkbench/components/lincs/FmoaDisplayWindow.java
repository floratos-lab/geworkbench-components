/**
 * 
 */
package org.geworkbench.components.lincs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import javax.swing.JLabel;
import javax.swing.JTable;

import javax.swing.JPanel;

import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import org.geworkbench.service.lincs.data.xsd.FmoaData;
import org.geworkbench.service.lincs.data.xsd.FmoaResult;
import org.geworkbench.util.ProgressBar;

/**
 * @author my2248
 * 
 */
public class FmoaDisplayWindow extends Thread implements Observer {

	private static final long serialVersionUID = -5410173882996059855L;

	private static final Log log = LogFactory.getLog(FmoaDisplayWindow.class);
	private JFrame frame;
	private JPanel mainPanel = new JPanel();
	private JPanel fmoaTitlePanel = new JPanel();
	private JPanel fmoaDataPanel = new JPanel();
	private JPanel fmoaCommandPanel = new JPanel();

	private JPanel fmoaBottomPanel = new JPanel();

	private JTextField tissueTF = new JTextField();
	private JTextField cellLineTF = new JTextField();
	private JTextField compoundTF = new JTextField();
	private JTextField fMoAAlgorithmTF = new JTextField();
	private JTextField interactomeTF = new JTextField();
	private JTextField fMoAGenesTF = new JTextField();
	private JCheckBox showDiffExpr = new JCheckBox("Show diff. expr.");
	private JCheckBox selectAll = new JCheckBox("Select all");

	private JButton exportButton = new JButton("Export Table");
	private JButton networkButton = new JButton("Generate Network");

	private JTable fmoaDataTable = null;
	DefaultTableModel fmoaDataModel = null;

	private String drugName = null;
	private Long fmoaId = null;
	private FmoaData fmoaData = null;
	private boolean cancelled = false;
	private ProgressBar pb;

	private final static String[] fmoaDataColumnNames = { "Gene Name", "NES",
			"Odds Ratio", "Include" };

	public FmoaDisplayWindow(String drugName, Long fmoaId) {
		this.drugName = drugName;
		this.fmoaId = fmoaId;

	}

	@Override
	public void run() {

		pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
		pb.addObserver(this);
		pb.setTitle("Retrieve Lincss Fmoa Data");
		pb.setMessage("Retrieve Data, please wait...");
		pb.start();		 

		try {
			Lincs lincsService = LincsInterface.getLincsService();
			fmoaData = lincsService.getFmoaData(drugName, fmoaId);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		} finally {
			if (pb != null)
				pb.dispose();
		}

		if (cancelled)
			return;
		frame = new JFrame();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		mainPanel.add(fmoaTitlePanel);
		mainPanel.add(fmoaDataPanel);
		JSeparator Separator = new JSeparator(SwingConstants.HORIZONTAL);
		mainPanel.add(Separator);
		mainPanel.add(fmoaCommandPanel);
		fmoaTitlePanel.setBackground(Color.WHITE);
		fmoaDataPanel.setBackground(Color.WHITE);
		fmoaCommandPanel.setBackground(Color.WHITE);
		JLabel fmoaLabel = new JLabel("Functional Mode of Action");
		fmoaTitlePanel.add(fmoaLabel);
		JLabel tissueTypeLabel = new JLabel("     Tissue Type");
		JLabel cellLineLabel = new JLabel("     Cell Line");
		JLabel compoundLabel = new JLabel("     Compound");
		JLabel fMoAAlgorithmLabel = new JLabel("     fMoA Algorithm");
		JLabel interactomeLabel = new JLabel("     Interactome");
		JLabel fMoAGenesLabel = new JLabel("     fMoA genes");
		JLabel fMoAGenesWithBlankLabel = new JLabel(
				"fMoA genes                    ");

		tissueTF.setEditable(false);
		tissueTF.setFocusable(false);
		cellLineTF.setEditable(false);
		cellLineTF.setFocusable(false);
		compoundTF.setEditable(false);
		compoundTF.setFocusable(false);
		fMoAAlgorithmTF.setEditable(false);
		fMoAAlgorithmTF.setFocusable(false);
		interactomeTF.setEditable(false);
		interactomeTF.setFocusable(false);
		fMoAGenesTF.setEditable(false);
		fMoAGenesTF.setFocusable(false);

		selectAll.setBackground(Color.WHITE);
		exportButton.setBackground(Color.WHITE);
		networkButton.setBackground(Color.WHITE);
		showDiffExpr.setBorderPaintedFlat(true);
		showDiffExpr.setBackground(Color.WHITE);

		fmoaDataPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.gridx = 0;
		c.gridy = 0;
		fmoaDataPanel.add(tissueTypeLabel, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		fmoaDataPanel.add(tissueTF, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.gridx = 2;
		c.gridy = 0;
		fmoaDataPanel.add(fMoAAlgorithmLabel, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 3;
		c.gridy = 0;
		fmoaDataPanel.add(fMoAAlgorithmTF, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.gridx = 4;
		c.gridy = 0;
		fmoaDataPanel.add(new JLabel("          "), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.gridx = 0;
		c.gridy = 1;
		fmoaDataPanel.add(cellLineLabel, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 1;
		fmoaDataPanel.add(cellLineTF, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.gridx = 2;
		c.gridy = 1;
		fmoaDataPanel.add(interactomeLabel, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 3;
		c.gridy = 1;
		fmoaDataPanel.add(interactomeTF, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.gridx = 0;
		c.gridy = 2;
		fmoaDataPanel.add(compoundLabel, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 2;
		fmoaDataPanel.add(compoundTF, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.gridx = 2;
		c.gridy = 2;
		fmoaDataPanel.add(fMoAGenesLabel, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 3;
		c.gridy = 2;
		fmoaDataPanel.add(fMoAGenesTF, c);

		fmoaCommandPanel.add(fMoAGenesWithBlankLabel);
		fmoaCommandPanel.add(exportButton);
		fmoaCommandPanel.add(networkButton);
		fmoaCommandPanel.add(showDiffExpr);
		fmoaCommandPanel.add(selectAll);

		tissueTF.setText(fmoaData.getTissueType());
		cellLineTF.setText(fmoaData.getCellLine());
		compoundTF.setText(drugName);
		fMoAAlgorithmTF.setText(fmoaData.getFmoaAlgorithm());
		interactomeTF.setText(fmoaData.getInteractome());
		fMoAGenesTF.setText(fmoaData.getFmoaGenesNumber().toString());
		Object[][] data = convertFmoaResultData(fmoaData.getFmoaResult());

		fmoaDataModel = new DefaultTableModel(data, fmoaDataColumnNames) {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings({ "unchecked", "rawtypes" })
			public Class getColumnClass(int c) {
				switch (c) {
				case 3:
					return Boolean.class;
				default: {
					Class returnValue = null;
					if ((c >= 0) && (c < getColumnCount())) {
						if (getValueAt(0, c) != null)
							returnValue = getValueAt(0, c).getClass();
					} else {
						returnValue = Object.class;
					}
					return returnValue;
				}
				}
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == 3)
					return true;
				else
					return false;
			}

		};

		fmoaDataTable = new JTable(fmoaDataModel);
		fmoaDataTable.setAutoCreateRowSorter(true);
		JScrollPane jScrollPane = new JScrollPane(fmoaDataTable);
		jScrollPane.getViewport().setBackground(Color.WHITE);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		centerRenderer.setBackground(Color.WHITE);
		fmoaDataTable.getColumnModel().getColumn(0)
				.setCellRenderer(centerRenderer);
		fmoaDataTable.getColumnModel().getColumn(1)
				.setCellRenderer(centerRenderer);
		fmoaDataTable.getColumnModel().getColumn(2)
				.setCellRenderer(centerRenderer);
		fmoaDataTable.setSelectionBackground(Color.LIGHT_GRAY);
		fmoaDataTable.getTableHeader().setDefaultRenderer(
				new HeaderRenderer(fmoaDataTable));

		mainPanel.add(jScrollPane, BorderLayout.CENTER);
		fmoaBottomPanel.add(new JLabel("  "));
		fmoaCommandPanel.setBackground(Color.WHITE);
		fmoaBottomPanel.setBackground(Color.WHITE);

		exportButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				List<String> hideColumns = new ArrayList<String>();
				hideColumns.add("Include");
				TableViewer.export(fmoaDataTable, hideColumns, frame);
			}
		});

		networkButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				;
				List<ValueObject> geneList = getGeneIdList();
				if (geneList.size() == 0) {
					JOptionPane.showMessageDialog(null,
							"There is no row to generate network.");
					return;
				}

				if (geneList.size() == fmoaDataTable.getModel().getRowCount()) {

					String theMessage = "You select all to generate network. This may take long time to run.";
					Object[] viewerChoices = { "Continue", "cancel" };
					int result = JOptionPane.showOptionDialog((Component) null,
							theMessage, "Warning", JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE, null, viewerChoices,
							viewerChoices[1]);
					if (result == JOptionPane.NO_OPTION)
						return;

				}

				ProgressBar createNetworkPb = null;
				createNetworkPb = ProgressBar
						.create(ProgressBar.INDETERMINATE_TYPE);

				NetworkCreator createNetworkHandler = new NetworkCreator(
						geneList, fmoaData.getCompoundId(), fmoaData
								.getDiffExpressionRunId(), fmoaData
								.getInteractomeVersionId(), networkButton,
						showDiffExpr.isSelected(), createNetworkPb);

				createNetworkHandler.start();

				createNetworkPb.setTitle("Create network");
				createNetworkPb.setMessage("Create network...");
				createNetworkPb.setModal(false);
				createNetworkPb.start();

			}
		});
		selectAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (selectAll.isSelected()) {
					for (int cx = 0; cx < fmoaDataTable.getRowCount(); cx++) {
						fmoaDataTable.setValueAt(true, cx, 3);
					}
				} else {
					for (int cx = 0; cx < fmoaDataTable.getRowCount(); cx++) {
						fmoaDataTable.setValueAt(false, cx, 3);
					}
				}

			}
		});

		fmoaDataTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				JTable target = (JTable) e.getSource();
				Point p = new Point(e.getX(), e.getY());
				int col = target.columnAtPoint(p);
				int row = target.rowAtPoint(p);
				if (target.getModel().getValueAt(row, col) instanceof Boolean) {
					if (((Boolean) target.getModel().getValueAt(row, 3))
							.booleanValue() == false)
						selectAll.setSelected(false);
				}

			}

		});

		Container frameContentPane = frame.getContentPane();
		frameContentPane.setLayout(new BorderLayout());

		frameContentPane.add(mainPanel);

		mainPanel.setBackground(Color.white);
		frame.pack();
		frame.setLocationRelativeTo(frame.getOwner());

		mainPanel.setVisible(true);

		frame.setVisible(true);

	}

	private Object[][] convertFmoaResultData(List<FmoaResult> dataList) {
		Object[][] objects = new Object[dataList.size()][fmoaDataColumnNames.length];
		for (int i = 0; i < dataList.size(); i++) {
			objects[i][0] = new ValueObject(dataList.get(i).getGeneSymbol(),
					dataList.get(i).getGeneId());
			objects[i][1] = dataList.get(i).getNes();
			if (objects[i][1] == null)
				objects[i][1] = "null";
			objects[i][2] = dataList.get(i).getOddRatio();
			if (objects[i][2] == null)
				objects[i][2] = "null";
			objects[i][3] = new Boolean(false);
		}
		return objects;
	}

	private List<ValueObject> getGeneIdList() {
		List<ValueObject> geneList = new ArrayList<ValueObject>();
		int colIndex = getHeaderNameIndex("Gene Name");
		for (int cx = 0; cx < fmoaDataTable.getRowCount(); cx++) {
			if (!TableViewer.isInclude(fmoaDataTable, cx))
				continue;
			geneList.add((ValueObject) fmoaDataTable.getModel().getValueAt(cx,
					colIndex));

		}

		return geneList;
	}

	public int getHeaderNameIndex(String name) {

		for (int i = 0; i < fmoaDataColumnNames.length; i++)
			if (fmoaDataColumnNames[i].equalsIgnoreCase(name))
				return i;
		return -1;
	}

	private class HeaderRenderer implements TableCellRenderer {

		DefaultTableCellRenderer renderer;

		public HeaderRenderer(JTable table) {
			renderer = (DefaultTableCellRenderer) table.getTableHeader()
					.getDefaultRenderer();
			renderer.setHorizontalAlignment(JLabel.CENTER);
			renderer.setBackground(Color.WHITE);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int col) {
			return renderer.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, col);
		}
	}

	
	public void update(Observable o, Object arg) {
		cancelled = true;
		pb.dispose();

	}
}
