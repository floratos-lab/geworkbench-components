package org.geworkbench.components.discovery;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;
import org.geworkbench.engine.management.Asynchronous;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.util.sequences.SequenceViewWidget;

public class PatternViewPanel extends JPanel {

	private static final long serialVersionUID = -7717226801492350199L;

	private SequenceViewWidget seqWidget;
	private final JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

	/**
	 * No public constructor because it is used only by this package.
	 */
	PatternViewPanel() {
		try {
			setLayout(new BorderLayout());
			mainPanel.setOneTouchExpandable(true);
			mainPanel.setResizeWeight(.5d);
			add(mainPanel, BorderLayout.CENTER);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setPatternResults(PatternResult pr) {

		DSSequenceSet<? extends DSSequence> sequenceSet = pr.getActiveDataSet();
		seqWidget = new SequenceViewWidget();
		seqWidget.setSequenceDB(sequenceSet);
		mainPanel.setTopComponent(seqWidget);
	
		// TODO variables like this probably should be a field eventually
		// keep it local for now to simplify the dependency
		PatternResult patternResult = pr;
	
		PatternDataSource patternDataSource = new PatternDataSource(patternResult);
		PatternTableModel model = new PatternTableModel(patternDataSource);
		model.setRowCount(patternDataSource.getPatternSourceSize());
		model.fireTableDataChanged();

		JPanel view = new PatternTableView(model, sequenceSet);
		mainPanel.setBottomComponent(view);
		repaint();
		revalidate();
	}
	
	@Subscribe
	public void sequenceDiscoveryTableRowSelected(
			org.geworkbench.events.SequenceDiscoveryTableEvent e,
			Object publisher) {
		seqWidget.patternSelectionHasChanged(e);
	}

	/**
	 * geneSelectorAction
	 * 
	 * @param e
	 *            GeneSelectorEvent
	 */
	@Subscribe(Asynchronous.class)
	public void receive(GeneSelectorEvent e, Object source) {
		seqWidget.sequenceDBUpdate(e);
	}
	
}