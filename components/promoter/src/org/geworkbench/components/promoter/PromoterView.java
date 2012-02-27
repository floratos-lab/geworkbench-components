package org.geworkbench.components.promoter;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.sequences.PatternTableModel;
import org.geworkbench.util.sequences.PatternTableView;

@AcceptTypes({ CSSequenceSet.class, PatternResult.class })
public class PromoterView implements VisualPlugin, PropertyChangeListener {

	private final JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	PromoterViewPanel main = new PromoterViewPanel();

	public PromoterView() {
		main.setSequenceDB(new CSSequenceSet<DSSequence>());
		main.setPromterView(this);

		mainPanel.setOneTouchExpandable(true);
		mainPanel.setResizeWeight(.5d);
		mainPanel.setTopComponent(main);
	}

	/**
	 * getComponent
	 * 
	 * @return Component
	 */
	public Component getComponent() {
		return mainPanel;
	}

	/**
	 * geneSelectorAction
	 * 
	 * @param e
	 *            GeneSelectorEvent
	 */
	@Subscribe
	public void receive(org.geworkbench.events.GeneSelectorEvent e,
			Object publisher) {
		main.receive(e);
	}

	/**
	 * receiveProjectSelection
	 * 
	 * @param e
	 *            ProjectEvent
	 */
	@SuppressWarnings("unchecked")
	@Subscribe
	public void receive(ProjectEvent e, Object source) {
    	DSDataSet<?> data = e.getDataSet();
        if (data instanceof PatternResult) {
			
            PatternResult patternResult = ((PatternResult) data);
            
            DSSequenceSet<DSSequence> sequenceSet = (DSSequenceSet<DSSequence>)patternResult.getParentSequenceSet();
    		main.setSequenceDB(sequenceSet);
    	
    		PatternTableModel model = new PatternTableModel(patternResult);
    		model.fireTableDataChanged();

    		JPanel view = new PatternTableView(model, sequenceSet);
    		view.addPropertyChangeListener(this);
    		mainPanel.setBottomComponent(view);
    		mainPanel.repaint();
        	
        } else if (data instanceof CSSequenceSet) {
			main.setSequenceDB((DSSequenceSet<DSSequence>) data);
			main.setPatterns(new ArrayList<DSMatchedPattern<DSSequence, CSSeqRegistration>>());
			mainPanel.setBottomComponent(null);
        }
	}

	/**
	 * Publish the image of the result
	 * 
	 * @return ImageSnapshotEvent
	 */
	@Publish
	public org.geworkbench.events.ImageSnapshotEvent createImageSnapshot(
			ImageSnapshotEvent event) {

		return event;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object object = evt.getNewValue();
		if(object instanceof List) {
			List<DSMatchedPattern<DSSequence, CSSeqRegistration>> selected = (List<DSMatchedPattern<DSSequence, CSSeqRegistration>>)object;
			main.setPatterns(selected);
		}
	}

}
