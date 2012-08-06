package org.geworkbench.components.interactions.cellularnetwork;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.GeneOntologyUtil;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GeneOntologyTree;

class ActiveMarkersTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 2700694309070316774L;

	final private int COLUMN_COUNT = 3;
	final private Vector<DSGeneMarker> allGenes;

	ActiveMarkersTableModel(Vector<DSGeneMarker> allGenes) {
		this.allGenes = allGenes;
	}

	@Override
	public boolean isCellEditable(int r, int c) {
		return false;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public int getRowCount() {
		if (allGenes != null)
			return allGenes.size();
		return 0;
	}

	@Override
	public String getColumnName(int index) {
		switch (index) {
		case 0:
			return "Marker ";
		case 1:
			return "Gene";
		case 2:
			return "Type";
		default:
			return "";
		}
	}

	@Override
	synchronized public Object getValueAt(int row, int column) {
		if (allGenes != null) {

			DSGeneMarker value = allGenes.get(row);
			if (value != null) {
				switch (column) {
				case 0: {
					return value.getLabel();
				}
				case 1: {
					if (value.getGeneName() != null) {
						return value.getGeneName();
					} else { // this should never happen
						return null;
					}
				}
				case 2: {
					GeneOntologyTree instance = GeneOntologyTree.getInstance();
					if (instance == null)
						return "pending";

					return GeneOntologyUtil.checkMarkerFunctions(value);
				}
				}
			}

		}

		return "loading ...";
	}
}