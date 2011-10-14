package org.geworkbench.components.discovery;

import java.awt.Component;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.components.discovery.PatternViewPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;

@AcceptTypes( {PatternResult.class})
public class PatternViewComponent implements
        VisualPlugin {
	private static Log log = LogFactory.getLog(PatternViewComponent.class);
	private PatternResult dataSet;
    private PatternViewPanel patternViewPanel;

    @Publish public org.geworkbench.events.ProjectNodeAddedEvent
            publishProjectNodeAddedEvent(org.geworkbench.events.
                                         ProjectNodeAddedEvent event) {
        return event;
    }

    public PatternViewComponent() {
        try {
            patternViewPanel = new PatternViewPanel();
            patternViewPanel.setPatternViewComponent(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
    	if ((data != null) && (data instanceof PatternResult)) {
    		dataSet = ((PatternResult) data);
    		log.debug("update blast result view panel");
    		patternViewPanel.setSequenceDB(dataSet.getActiveDataSet());
    		patternViewPanel.setPatternResults(dataSet);
    		String summary = "Number of Patterns found:" + ((PatternResult) dataSet).getPatternNo();
    		dataSet.addDescription(summary);
    	}
    }
}