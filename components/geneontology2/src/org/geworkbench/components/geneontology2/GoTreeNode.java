package org.geworkbench.components.geneontology2;

import org.geworkbench.bison.datastructure.biocollections.GoAnalysisResult;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GOTerm;

class GoTreeNode {
	/**
	 * 
	 */
	private final GoTermTreeModel goTermTreeModel;
	GOTerm goTerm = null;
	int goId = -1;

	GoTreeNode(GoTermTreeModel goTermTreeModel, GOTerm goTerm) {
		this.goTermTreeModel = goTermTreeModel;
		this.goTerm = goTerm;
		goId = goTerm.getId();
	}

	@Override
	public String toString() {
		if (goId == 0)
			return "ROOT";

		if (goTermTreeModel.result == null
				|| goTermTreeModel.result.getRowAsString(goId) == null) {
			// not in the result
			return GoAnalysisResult.getGoTermName(goId);
		} else { // in the result from ontologizer
			return goTermTreeModel.result.getRowAsString(goId);
		}
	}
}