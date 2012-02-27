package org.geworkbench.components.discovery;

import java.awt.Component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;

@AcceptTypes( {PatternResult.class})
public class PatternViewComponent implements
        VisualPlugin {

	private static Log log = LogFactory.getLog(PatternViewComponent.class);
    private PatternViewPanel patternViewPanel;

	public PatternViewComponent() {
		patternViewPanel = new PatternViewPanel();
	}

    /**
     * getComponent
     *
     * @return Component
     */
    public Component getComponent() {
        return patternViewPanel;
    }

    /**
     * receiveProjectSelection
     *
     * @param e ProjectEvent
     */
	@Subscribe public void receive(org.geworkbench.events.ProjectEvent e,
                                   Object source) { 	

    	DSDataSet<?> data = e.getDataSet();
    	if (data instanceof PatternResult) {
    		log.debug("ProjectEvent received");
    		PatternResult patterResult = ((PatternResult) data);
    		patternViewPanel.setPatternResults(patterResult);
    	}
    }

	@Subscribe
	public void receive(org.geworkbench.events.GeneSelectorEvent e,
			Object publisher) {
		patternViewPanel.sequenceDBUpdate(e);
	}
}