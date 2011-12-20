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
	private JSplitPane mainPanel = new JSplitPane();

	private DSSequenceSet<? extends DSSequence> sequenceDB;
	private PatternResult patternResult; 
	public static final String TABLE_EVENT = "tableEvent"; 
	
	/**
	 * No public constructor because it is used only by this package.
	 */
	PatternViewPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() { 
		this.setLayout(new BorderLayout());
		mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainPanel.setOneTouchExpandable(true);
		mainPanel.setResizeWeight(.5d);
		this.add(mainPanel, BorderLayout.CENTER);
	}
	
	public DSSequenceSet<? extends DSSequence> getSequenceDB() {
		return sequenceDB;
	}
	
	public void setPatternResults(PatternResult pr) {

		this.sequenceDB = pr.getActiveDataSet();
		seqWidget = new SequenceViewWidget();
		seqWidget.setSequenceDB(sequenceDB);
		mainPanel.setTopComponent(seqWidget);
	
		this.patternResult = pr;
	
		PatternTableModelWrapper model = new PatternTableModelWrapper();
		PatternDataSource patternDataSource = new PatternDataSource(patternResult);
		model.attach(patternDataSource);
		JPanel view = new PatternTableView(model, this);
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