package org.geworkbench.components.poshistogram;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.sequences.PatternTableModel;
import org.geworkbench.util.sequences.PatternTableView;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author Saroja Hanasoge
 * @version $Id$
 */
@AcceptTypes({PatternResult.class})
public class PositionHistogramAppComponent implements VisualPlugin, MenuListener, PropertyChangeListener {

    PositionHistogramWidget pHistogramWidget = null;
	private final JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    Map<String, ActionListener> listeners = new HashMap<String, ActionListener>();
    
    public PositionHistogramAppComponent() {
        pHistogramWidget = new PositionHistogramWidget(this);
        mainPanel.setOneTouchExpandable(true);
		mainPanel.setResizeWeight(.5d);
    }

    @Override
    public Component getComponent() {
        return mainPanel;
    }

    @Override
    public ActionListener getActionListener(String var) {
        return listeners.get(var);
    }

    @SuppressWarnings({ "unchecked" })
	@Subscribe
    public void receiveProjectSelection(ProjectEvent e, Object source) {
    	DSDataSet<?> data = e.getDataSet();
        if (data instanceof PatternResult) {
            PatternResult patternResult = ((PatternResult) data);
            
            DSSequenceSet<DSSequence> sequenceSet = (DSSequenceSet<DSSequence>)patternResult.getParentSequenceSet();
    		pHistogramWidget.setSequenceDB(sequenceSet);
    		mainPanel.setTopComponent(pHistogramWidget);
    	
    		PatternTableModel model = new PatternTableModel(patternResult);
    		model.fireTableDataChanged();

    		JPanel view = new PatternTableView(model, sequenceSet);
    		view.addPropertyChangeListener(this);
    		mainPanel.setBottomComponent(view);
    		mainPanel.repaint();
        }
    }

    @Publish
    public org.geworkbench.events.ImageSnapshotEvent publishImageSnapshotEvent(ImageSnapshotEvent event) {
        return event;
    }

	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object object = evt.getNewValue();
		if(object instanceof List) {
			List<DSMatchedPattern<DSSequence, CSSeqRegistration>> selected = (List<DSMatchedPattern<DSSequence, CSSeqRegistration>>)object;
			pHistogramWidget.setPatterns(selected);
		}
	}

}
