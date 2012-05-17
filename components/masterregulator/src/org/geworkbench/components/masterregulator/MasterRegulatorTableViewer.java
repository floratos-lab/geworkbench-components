package org.geworkbench.components.masterregulator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMasterRegulatorTableResultSet;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

@AcceptTypes( { DSMasterRegulatorTableResultSet.class })
public class MasterRegulatorTableViewer extends JPanel implements VisualPlugin {

	private static final long serialVersionUID = -4814200980530475623L;
	private TableViewer tv;
	/*
    * TFsym: transcription factor id (usually this is the probeset id).
    * GeneName: transcription factor gene name.
    * NumPosGSet: number of genes (among those in the regulon of the transcription factor) which are positively regulated by the TF. Only genes represented in the microarray set are counted.
    * NumNegGSet: number of genes (among those in the regulon of the transcription factor) which are negatively regulated by the TF. Only genes represented in the microarray set are counted.
    * NumLedgePos: number of genes (among those counted under the NumPosGSet column) which are in the GSEA leading edge.
    * NumLedgeNeg: number of genes (among those counted under the NumNegGSet column) which are in the GSEA leading edge.
    * NumLedge: sum of NumLedgePos and NumLedgeNeg.
    * ES: GSEA enrichment score for the regulon of the TF.
    * NES: GSEA normalized enrichment score for the regulon of the TF.
    * absNES: absolute value of NES
    * PV: p-value of normalized (?) enrichment score.
    * OddRatio: (NumLedge/(microarray set genes in the regulon of the TF))/((number of differentially expressed genes left of the leading edge)/(total number of microarray set genes))
    * TScore: T-Score
    * MeanClass1: mean expression value of TF among all "Class 1" arrays.
    * MeanClass2: mean expression value of TF among all "Class 2" arrays.
    * Original MRA/Recovered_MRA: a value of 1 in this column means that the TF was found to be enriched by GSEA (above the significance level pvalue_gsea) and that it was not shadowed by any other TF. 
    *   A value of 0 means that the TF was found to be enriched by GSEA and that it was shadowed by another TF and that it remained enriched even after the common targets with the other TF were removed from its regulon. 
	 */
	private String[] columnNames = { "TFsym", "GeneName", "NumPosGSet", "NumNegGSet", "NumLedgePos", "NumLedgeNeg", "NumLedge",
			"ES", "NES", "absNES", "PV", "OddRatio", "TScore", "MeanClass1", "MeanClass2", "Original MRA/Recovered_MRA"};
	private final int colNum = columnNames.length;
	public MasterRegulatorTableViewer(){
		Object[][] data = new Object[1][1];
		data[0][0] = "Start";
		tv = new TableViewer(columnNames, data);		
		setLayout(new BorderLayout());
		add(tv, BorderLayout.CENTER);
		JButton saveButton=new JButton();
		JPanel bottom1=new JPanel();
		bottom1.setLayout(new GridLayout(0,5));
		bottom1.add(new JLabel());
		bottom1.add(new JLabel());
		bottom1.add(saveButton);
		saveButton.setText("Export Table");
		add(bottom1,BorderLayout.SOUTH );
		
		saveButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (tv.getTable().getRowCount() > 0)
					tv.exportTableData();
			}
		});
	}

	@Override
	public Component getComponent() {
		return this;
	}
	
	DSMasterRegulatorTableResultSet MRAResultSet = null;
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<?> dataSet = event.getDataSet();
		if (dataSet instanceof DSMasterRegulatorTableResultSet) {
			MRAResultSet = (DSMasterRegulatorTableResultSet) dataSet;
			// Results don't have "MeanClass1" & "MeanClass2" if marina is run using 
			// probe shuffling instead of gene shuffling by setting min_samples > 9
			if (MRAResultSet.getData().length>0 && MRAResultSet.getData()[0].length == colNum - 2){
				String[] reducedNames = Arrays.copyOf(columnNames, colNum - 2);
				reducedNames[colNum-3] = columnNames[colNum - 1];
				tv.headerNames = reducedNames;
			} else
				tv.headerNames = columnNames;
			tv.setTableModel(MRAResultSet.getData());
			for (int i = 2; i < columnNames.length; i++)
				tv.setNumerical(i, true);
		}
	}
}