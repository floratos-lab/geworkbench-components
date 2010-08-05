package org.geworkbench.components.sequenceretriever;

import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.util.sequences.SequenceViewWidgetPanel;

/**
 * 
 * @author not attributable
 * @version $Id$
 */
public final class SingleSequenceViewPanel extends SequenceViewWidgetPanel {
	private static final long serialVersionUID = 7100195393198518647L;

	public void setRetrievedSequencesPanel(
			RetrievedSequencesPanel retrievedSequencesPanel) {
		// no effect
	}

	public void setSingleSequenceView(boolean singleSequenceView) {
		this.singleSequenceView = singleSequenceView;
	}

	public void setSequenceDB(DSSequenceSet<DSSequence> sequenceDB) {
		this.sequenceDB = sequenceDB;
	}

	public void setLineView(boolean lineView) {
		this.lineView = lineView;
		revalidate();
	}

	public void setSeqXclickPoint(int seqXclickPoint) {
		this.seqXclickPoint = seqXclickPoint;
	}

	/**
	 * initialize
	 */
	public void initialize() {
		this.removeAll();
		this.setSequenceDB(null);
		this.setSelectedSequence(null);
		this.setSeqXclickPoint(-1);
		this.repaint();
	}

}
