package org.geworkbench.components.genspace.ui.graph;

import com.mxgraph.view.mxGraph;

public class myGraph extends mxGraph{
	public boolean isCellFoldable(Object cell, boolean collapse)
	{
		return false;
	}
	@Override
	public boolean isCellConnectable(Object arg0) {
		return false;
	}
}
