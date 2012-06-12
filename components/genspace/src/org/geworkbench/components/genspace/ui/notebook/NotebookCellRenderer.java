package org.geworkbench.components.genspace.ui.notebook;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.XMLGregorianCalendar;

import org.geworkbench.components.genspace.server.stubs.AnalysisEvent;

public class NotebookCellRenderer implements TableCellRenderer{


	private int rows;
	private int lines;

	public void getRows(int rows)
	{
		this.rows = rows;
	}

	private Date convertToDate(XMLGregorianCalendar cal) {
		return DatatypeConverter.parseDateTime(cal.toXMLFormat()).getTime();
	}
	static Font headerFont;
	static Font dataSetFont;

	static
	{
		headerFont = new Font("Dialog", Font.BOLD, 13);
		dataSetFont = new Font("Dialog",Font.ITALIC,13);
	}
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) 
	{
		SimpleDateFormat format = new SimpleDateFormat("M/d/yy h:mm a");
		JPanel panel = new JPanel();
		TableModel lm = table.getModel();
		AnalysisEvent e = (AnalysisEvent) lm.getValueAt(row, 0);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel noteInfo = new JLabel(e.getTool().getName() + " at " + format.format(convertToDate(e.getCreatedAt())));
		noteInfo.setFont(headerFont);
		JLabel dataSetName = new JLabel("Dataset: " + e.getTransaction().getDataSetName());
		dataSetName.setFont(dataSetFont);
		
		JTextArea noteText = new JTextArea(e.getNote());
		noteText.setEditable(true);
		noteText.setLineWrap(true);
		noteText.setWrapStyleWord(true);
		noteText.setSize(table.getWidth(), 0);
		noteText.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		table.setRowHeight(row, 25 + noteText.getFontMetrics(noteText.getFont()).getHeight() * (NotebookPanel.countLines(noteText)+2));
		panel.add(noteInfo);
		panel.add(dataSetName);
		panel.add(noteText);
		noteText.setBackground(new Color(0,0,0,0));
		return panel;
	}

	public void setLines(int lines)
	{
		this.lines = lines;
	}

	public static int countLines(JTextArea textArea) {
		AttributedString text = new AttributedString(textArea.getText());
		FontRenderContext frc = textArea.getFontMetrics(textArea.getFont())
				.getFontRenderContext();

		int lines = 0;
		if (!textArea.getText().equals("") )
		{
			AttributedCharacterIterator charIt = text.getIterator();
			LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(charIt, frc);
			float formatWidth = (float) textArea.getSize().width;
			lineMeasurer.setPosition(charIt.getBeginIndex());
			while (lineMeasurer.getPosition() < charIt.getEndIndex()) {
				lineMeasurer.nextLayout(formatWidth);
				lines++;
			}
			for(int i = 0; i < textArea.getText().length();i++)
			{
				if(textArea.getText().charAt(i) == '\r' || textArea.getText().charAt(i) == '\n')
					lines++;
			}
		}
		else
		{
			lines = 1;
		}
		return lines;
	}

}


