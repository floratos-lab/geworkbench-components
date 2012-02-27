package org.geworkbench.components.discovery;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
import org.geworkbench.engine.management.Asynchronous;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.util.sequences.PatternTableModel;
import org.geworkbench.util.sequences.PatternTableView;
import org.geworkbench.util.sequences.SequenceViewWidget;

public class PatternViewPanel extends JPanel implements PropertyChangeListener {

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

	// TODO variables like patternResult probably should be a field eventually
	// keep it local for now to simplify the dependency
	public void setPatternResults(PatternResult patternResult) {

		DSSequenceSet<? extends DSSequence> sequenceSet = patternResult.getParentSequenceSet();
		seqWidget = new SequenceViewWidget();
		seqWidget.setSequenceDB(sequenceSet);
		mainPanel.setTopComponent(seqWidget);
	
	
		PatternTableModel model = new PatternTableModel(patternResult);
		model.fireTableDataChanged();

		JPanel view = new PatternTableView(model, sequenceSet);
		view.addPropertyChangeListener(this);
		mainPanel.setBottomComponent(view);
		repaint();
		revalidate();
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

	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object object = evt.getNewValue();
		if(object instanceof List) {
			List<DSMatchedPattern<DSSequence, CSSeqRegistration>> selected = (List<DSMatchedPattern<DSSequence, CSSeqRegistration>>)object;
			seqWidget.patternSelectionHasChanged(selected);
		}
	}

	// to implement the behavior requested by bug 2943 
	void sequenceDBUpdate(GeneSelectorEvent e) {
		if(seqWidget!=null)
			seqWidget.sequenceDBUpdate(e);
	}
	
}