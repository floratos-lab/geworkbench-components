package org.geworkbench.components.analysis;

/**
 * @author yc2480
 * @version $Id: ReHighlightable.java,v 1.3 2009-05-18 19:00:56 chiangy Exp $
 */
public interface ReHighlightable {

	/**
	 * When called, Component (ex:AnalysisPanel and NormalizationPanel) should
	 * refresh the high light for the parameter set list after parameter changed
	 * in parameter panels.
	 */
	abstract void refreshHighLight();
}
