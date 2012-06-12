package org.geworkbench.components.genspace.ui.notebook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.server.stubs.AnalysisEvent;
import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbench.components.genspace.ui.UpdateablePanel;

public class NotebookPanel extends JPanel implements ActionListener, UpdateablePanel {
	public TableCellRenderer MyCellRenderer = new NotebookCellRenderer();
	public TableCellEditor MyCellEditor;

	public String searchTerm = null;
	public String sortByMethod = null;
	int heightParameter;

	private NoteListModel nlm = new NoteListModel();;
	private JTable noteList = new JTable();
	private JComboBox sortByDropdown;

	@Override
	public void updateFormFields() {
		if (GenSpaceServerFactory.isLoggedIn()) {
			SwingWorker<List<AnalysisEvent>, Void> worker = new SwingWorker<List<AnalysisEvent>, Void>() {
				@Override
				protected List<AnalysisEvent> doInBackground() throws Exception {
					// TODO Auto-generated method stub
					return GenSpaceServerFactory.getPrivUsageFacade().getMyNotes(searchTerm, sortByMethod);
				}

				@Override
				protected void done() {
					try
					{
					nlm.setMyList(get());
					noteList.setModel(nlm);
					noteList.validate();
					noteList.repaint();
					}
					catch(Exception ex)
					{
						
					}
				}
			};
			worker.execute();
		}
	}

	public NotebookPanel() {
	}

	public void init() {
		// TODO Auto-generated method stub
		// Log in to genspace
		MyCellEditor = new NotebookCellEditor(noteList);
		setLayout(new BorderLayout(0, 9));
		noteList.setModel(nlm);
		updateFormFields();

		List<Tool> toolStrings = GenSpaceServerFactory.getUsageOps().getAllTools();
		String[] toolNames = new String[toolStrings.size()];
		for (int i = 1; i < toolStrings.size(); i++) {
			toolNames[i] = toolStrings.get(i).getName();
		}
		JComboBox dropdown = new JComboBox(toolNames); // dropdown box
		dropdown.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				ItemSelectable is = (ItemSelectable) e.getSource();
				setSearchTerm(selectedString(is));
				List<AnalysisEvent> searchEvents = GenSpaceServerFactory.getPrivUsageFacade().getMyNotes(searchTerm, sortByMethod);
				nlm.setMyList(searchEvents);
				noteList.setModel(nlm);
				noteList.revalidate();
			}

		});
		JPanel sortBy = new JPanel(new FlowLayout());
		String[] sortByStrings = { " ", "Sort by tool", "Sort by date" };
		sortByDropdown = new JComboBox(sortByStrings);
		sortByDropdown.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (sortByDropdown.getSelectedIndex() == 1) {
					setSortBy("tool");
					List<AnalysisEvent> searchEvents = GenSpaceServerFactory.getPrivUsageFacade().getMyNotes(searchTerm, sortByMethod);
					nlm.setMyList(searchEvents);
					noteList.setModel(nlm);
					noteList.revalidate();
				}
				if (sortByDropdown.getSelectedIndex() == 2) {
					setSortBy("date");
					List<AnalysisEvent> searchEvents = GenSpaceServerFactory.getPrivUsageFacade().getMyNotes(searchTerm, sortByMethod);
					nlm.setMyList(searchEvents);
					noteList.setModel(nlm);
					noteList.revalidate();
				}

			}
		});
		sortBy.add(sortByDropdown);
		JPanel searchPanel = new JPanel(new BorderLayout());
		final JTextField searchBox = new JTextField("Enter your search query here or use the dropdown below");
		searchBox.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (searchBox.getText().equals("Enter your search query here or use the dropdown below"))
					searchBox.setText("");
				super.mousePressed(e);
			}
		});
		JButton searchButton = new JButton("Search");
		searchButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				ItemSelectable searchName = (ItemSelectable) event.getSource();
				String query = searchBox.getText();
				setSearchTerm(query);
				List<AnalysisEvent> searchQueryList = GenSpaceServerFactory.getPrivUsageFacade().getMyNotes(searchTerm, sortByMethod); // same
																																		// problem
																																		// as
																																		// above
				nlm.setMyList(searchQueryList);
				noteList.setModel(nlm);
				noteList.revalidate();
			}
		});
		JLabel searchLabel = new JLabel("Filter:");
		Font f = new Font("Dialog", Font.BOLD, 14);
		searchLabel.setFont(f);
		searchPanel.add(searchLabel, BorderLayout.NORTH);
		searchPanel.add(searchBox, BorderLayout.CENTER);
		searchPanel.add(searchButton, BorderLayout.EAST);
		SimpleDateFormat format = new SimpleDateFormat("F/M/yy h:mm a");
		noteList.setSize(800, 600);
		noteList.getColumnModel().getColumn(0).setCellRenderer(MyCellRenderer);
		noteList.getColumnModel().getColumn(0).setCellEditor(new NotebookCellEditor(noteList));
		noteList.setBackground(new Color(0,0,0,0));
		int lines = countLines(((NotebookCellEditor) MyCellEditor).getNoteText()); // counts
																					// lines
																					// needed
		System.out.println(lines);
		((NotebookCellRenderer) MyCellRenderer).setLines(lines);
		noteList.setTableHeader(null);
		JScrollPane notePane = new JScrollPane(noteList);
		notePane.setBackground(new Color(0,0,0,0));
		JPanel noteArea = new JPanel(new BorderLayout());
		JPanel sortArea = new JPanel(new BorderLayout()); // dropdown menus
		sortArea.add(searchPanel, BorderLayout.NORTH); // panel to hold sorting
														// area
		sortArea.add(dropdown, BorderLayout.CENTER);
		sortArea.add(sortBy, BorderLayout.EAST);
		JLabel noteLabel = new JLabel("My Log:");
		noteLabel.setFont(f);
		noteArea.add(noteLabel, BorderLayout.NORTH);
		noteArea.add(notePane, BorderLayout.CENTER);
		noteArea.setBackground(new Color(0,0,0,0));
		add(noteArea, BorderLayout.CENTER);
		add(sortArea, BorderLayout.NORTH);
		revalidate();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		//
	}

	// counts lines
	public static int countLines(JTextArea textArea) {
		AttributedString text = new AttributedString(textArea.getText());
		FontRenderContext frc = textArea.getFontMetrics(textArea.getFont()).getFontRenderContext();

		int lines = 0;
		if (!textArea.getText().equals("")) {
			AttributedCharacterIterator charIt = text.getIterator();
			LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(charIt, frc);
			float formatWidth = (float) textArea.getSize().width;
			lineMeasurer.setPosition(charIt.getBeginIndex());
			while (lineMeasurer.getPosition() < charIt.getEndIndex()) {
				lineMeasurer.nextLayout(formatWidth);
				lines++;
			}
			for (int i = 0; i < textArea.getText().length(); i++) {
				if (textArea.getText().charAt(i) == '\r' || textArea.getText().charAt(i) == '\n')
					lines++;
			}
		} else {
			lines = 1;
		}
		return lines;
	}

	public void setSearchTerm(String firstParam) {
		this.searchTerm = firstParam;
	}

	public void setSortBy(String secondParam) {
		this.sortByMethod = secondParam;
	}

	static private String selectedString(ItemSelectable is) {
		Object selected[] = is.getSelectedObjects();
		return ((selected == null || selected.length == 0) ? "" : (String) selected[0]);
	}

}
