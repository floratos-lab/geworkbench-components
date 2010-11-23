package org.geworkbench.components.geneontology2;

import java.util.HashMap;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.GoAnalysisResult;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;

class GoTableModel extends AbstractTableModel {
	static Log log = LogFactory.getLog( GoTableModel.class );

	private static final long serialVersionUID = -7009237666149228067L;
	private int COLUMN_COUNT = 7;

	private static final int TABLE_COLUMN_INDEX_GO_ID = 0;
	static final int TABLE_COLUMN_INDEX_GO_TERM_NAME = 1;
	static final int TABLE_COLUMN_INDEX_NAMESPACE = 2;
	private static final int TABLE_COLUMN_INDEX_P = 3;
	private static final int TABLE_COLUMN_INDEX_ADJUSTED_P = 4;
	private static final int TABLE_COLUMN_INDEX_POP_COUNT = 5;
	private static final int TABLE_COLUMN_INDEX_STUDY_COUNT = 6;
	
	private String[] columnNames = { "GO:ID", "Name", "Namespace",
			"P-value", "Adjusted P-value", "Population Count", "Study Count" };
    private Object[][] data = new Object[0][COLUMN_COUNT];

	public int getColumnCount() {
		return COLUMN_COUNT;
	}
	
	// this is potentially useful in other place or as part of annotation parser
	private void deepParse(String goTermAnnotation, Map<Integer, String> map) {
		if(goTermAnnotation.startsWith("---"))
				return;

		String[] fields = goTermAnnotation.split("//");
		int id = Integer.parseInt( fields[0].trim() );
		if(map.get(id)==null) {
			map.put(id, fields[1].trim());
		}
	}

	void populateFromDataSet(DSMicroarraySet<CSMicroarray> dataSet) {
		Map<Integer, String> biologyProcessMap = new HashMap<Integer, String>();
		Map<Integer, String> cellularComponentMap = new HashMap<Integer, String>();
		Map<Integer, String> molecularFunctionMap = new HashMap<Integer, String>();
		for (DSGeneMarker marker : dataSet.getMarkers()) {

			String[] biologyProcess = AnnotationParser.getInfo(
					marker.getLabel(),
					AnnotationParser.GENE_ONTOLOGY_BIOLOGICAL_PROCESS);
			if (biologyProcess != null) {
				for (String b : biologyProcess) {
					deepParse(b, biologyProcessMap);
				}
			}
			String[] cellularComponent = AnnotationParser.getInfo(
					marker.getLabel(),
					AnnotationParser.GENE_ONTOLOGY_CELLULAR_COMPONENT);
			if (cellularComponent != null) {
				for (String c : cellularComponent) {
					deepParse(c, cellularComponentMap);
				}
			}
			String[] molecularFunction = AnnotationParser.getInfo(
					marker.getLabel(),
					AnnotationParser.GENE_ONTOLOGY_MOLECULAR_FUNCTION);
			if (molecularFunction != null) {
				for (String m : molecularFunction) {
					deepParse(m, molecularFunctionMap);
				}
			}
		}
		COLUMN_COUNT = 3;
		data = new Object[biologyProcessMap.size()+cellularComponentMap.size()+molecularFunctionMap.size()][COLUMN_COUNT];
		int index = 0;
		for(int id: biologyProcessMap.keySet()) {
			data[index][0] = id;
			data[index][1] = biologyProcessMap.get(id);
			data[index][2] = "B";
			index++;
		}
		for(int id: cellularComponentMap.keySet()) {
			data[index][0] = id;
			data[index][1] = cellularComponentMap.get(id);
			data[index][2] = "C";
			index++;
		}
		for(int id: molecularFunctionMap.keySet()) {
			data[index][0] = id;
			data[index][1] = molecularFunctionMap.get(id);
			data[index][2] = "M";
			index++;
		}
		
		fireTableStructureChanged();
	}

	public int getRowCount() {
		return data.length;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return data[rowIndex][columnIndex];
	}
	
	public String getColumnName(int col) {
		return columnNames[col];
	}
	
	public void populateNewResult(GoAnalysisResult result) {
		COLUMN_COUNT = 7;
		data = result.getResultAsArray();
		
		fireTableStructureChanged();
	}
	
	public Class<?> getColumnClass(int columnIndex) {
		if(columnIndex==TABLE_COLUMN_INDEX_ADJUSTED_P || columnIndex==TABLE_COLUMN_INDEX_P)
			return Double.class;
		else if(columnIndex==TABLE_COLUMN_INDEX_GO_ID || columnIndex==TABLE_COLUMN_INDEX_POP_COUNT || columnIndex==TABLE_COLUMN_INDEX_STUDY_COUNT)
			return Integer.class;
		else if(columnIndex==TABLE_COLUMN_INDEX_GO_TERM_NAME || columnIndex==TABLE_COLUMN_INDEX_NAMESPACE )
			return String.class;
		else {
			log.warn("Unspecified column type");
			return Object.class;
		}
	}
}