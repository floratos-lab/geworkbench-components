package org.geworkbench.components.sequences;

import java.awt.Component;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Asynchronous;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.util.sequences.SequenceViewWidget;

/**
 * <p>
 * SequenceViewAppComponent controls all notification and communication for
 * SequenceViewWidget
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: Califano Lab
 * </p>
 * 
 * @author
 * @version $Id$
 */
@AcceptTypes({ CSSequenceSet.class })
public class SequenceViewAppComponent implements VisualPlugin {
	private SequenceViewWidget sViewWidget;

	public SequenceViewAppComponent() {
		sViewWidget = new SequenceViewWidget();
	}

	/**
	 * geneSelectorAction
	 * 
	 * @param e
	 *            GeneSelectorEvent
	 */
	@Subscribe(Asynchronous.class)
	public void receive(GeneSelectorEvent e, Object source) {
		sViewWidget.sequenceDBUpdate(e);
	}

	public Component getComponent() {
		return sViewWidget;
	}

	@Subscribe
	public void receiveProjectSelection(org.geworkbench.events.ProjectEvent e,
			Object source) {
		ProjectSelection selection = ((ProjectPanel) source).getSelection();
		DSDataSet<?> dataFile = selection.getDataSet();
		if (dataFile instanceof DSSequenceSet) {
			sViewWidget.setSequenceDB((DSSequenceSet<?>) dataFile);
		}
	}

}
