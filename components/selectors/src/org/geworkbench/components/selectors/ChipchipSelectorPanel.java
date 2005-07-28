package org.geworkbench.components.selectors;

import org.geworkbench.events.ChipchipSubpanelChangedEvent;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSChipchipSet;

/**
 * <p>Title: caWorkbench</p>
 * <p/>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype
 * Analysis</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p/>
 * <p>Company: Columbia University</p>
 *
 * @author Adam Margolin
 * @version 3.0
 */
public class ChipchipSelectorPanel extends PanelsEnabledSelectorPanel<CSChipchipSet> {

    @Subscribe public void receive(org.geworkbench.events.ChipchipSubpanelChangedEvent e, Object source) {
        super.receive(e, source);
    }
}
