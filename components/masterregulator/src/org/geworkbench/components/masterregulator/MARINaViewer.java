package org.geworkbench.components.masterregulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMasterRegulatorResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMasterRagulatorResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMasterRegulatorTableResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.components.masterregulator.TAnalysis.TAnalysisException;
import org.geworkbench.components.masterregulator.TableViewer.DefaultViewerTableModel;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

@AcceptTypes( { DSMasterRegulatorTableResultSet.class })
public class MARINaViewer extends MasterRegulatorViewer{

	private static final long serialVersionUID = 6192020161641949395L;
	private Log log = LogFactory.getLog(MARINaViewer.class);
	
	String[] columnNames = { "Master Regulator", "GSEA P-Value", "Markers in regulon",
			"Num Leading Edge", "Odds Ratio", "NES", "absNES", "Mode" };
	String[] detailColumnNames = { "Markers in Leading Edge", " -log10(P-value) * sign of t-value" };
	private static final int absnesCol = 6;

	public MARINaViewer(){
		tv.headerNames = columnNames;
		tv2.headerNames = detailColumnNames;
		intersectionBar.setText("GSEA Leading Edge Set");
	}
	
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<?> dataSet = event.getDataSet();
		if (dataSet instanceof DSMasterRegulatorTableResultSet) {
			MRAResultSet = convert((DSMasterRegulatorTableResultSet) dataSet);
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					updateTable();
					for (int i = 1; i < columnNames.length-1; i++)
						tv.setNumerical(i, true);
					updateSelectedTF(MRAResultSet, currentSelectedtfA, tv2);
					useSymbol = true;
				}
			});
		}
	}
	
	protected void sortSummaryTable(){
		((DefaultViewerTableModel) tv.model).sort(absnesCol, false); //sort by absNES in descending order
	}
	
	private DSMasterRagulatorResultSet<DSGeneMarker> convert(DSMasterRegulatorTableResultSet marinaResultSet){
		DSMicroarraySet maSet = (DSMicroarraySet)marinaResultSet.getParentDataSet();
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(maSet);
		DSMasterRagulatorResultSet<DSGeneMarker> mraResultSet = new CSMasterRegulatorResultSet<DSGeneMarker>(
				maSet, marinaResultSet.getLabel(), view.markers().size());
		
		//t-analysis
		Map<DSGeneMarker, Double> values = null;
		if(marinaResultSet.getControls().length>0){
			log.info("Executing T Analysis");
			try {
				TAnalysis tTestAnalysis= new TAnalysis(view, marinaResultSet.getCases(), marinaResultSet.getControls());
				values = tTestAnalysis.calculateDisplayValues();
			} catch (TAnalysisException e1) {
				JOptionPane.showMessageDialog(null, 
						"Can't find valid case or control arrrays for T Analysis in MARINa Viewer",
						"T Analysis Error", JOptionPane.ERROR_MESSAGE);
				return mraResultSet;
			}
		}else{
			values = getPairedValues(view, marinaResultSet.getCases());
		}
		if (values==null){
			log.error("The set of display values is set null.");
			return null;
		}
		mraResultSet.setValues(values);
		
		MRA.sortByValue(values, mraResultSet);
		
		Object[][] res = marinaResultSet.getData();
		if(res.length==0) return mraResultSet;
		DSItemList<DSGeneMarker> markers = maSet.getMarkers();

		//for each TF A 
		for(Object[] row : res){
			String tf	= (String)row[0];
			double pval = Double.parseDouble((String)row[10]);
			Object odd	= row[11];
			Object nes	= row[8];
			Object absnes = row[9];
			char mode	= Double.parseDouble((String) nes) >= 0 ? CSMasterRegulatorResultSet.ACTIVATOR
						: CSMasterRegulatorResultSet.REPRESSOR;
			
			DSGeneMarker tfA = markers.get(tf);
			if(tfA==null){
				log.warn("tf not found: "+tf);
				continue;
			}
			
			ArrayList<DSGeneMarker> nA = new ArrayList<DSGeneMarker>();
			Set<String> regulons = marinaResultSet.getRegulon(tf);
			if(regulons != null){
				for(String str : regulons){
					DSGeneMarker neighbor = markers.get(str);
					nA.add(neighbor);
				}
			}
			DSItemList<DSGeneMarker> nAItemList = new CSItemList<DSGeneMarker>();
			nAItemList.addAll(nA);
			mraResultSet.setGenesInRegulon(tfA, nAItemList);			

			ArrayList<DSGeneMarker> genesInTargetList = new ArrayList<DSGeneMarker>();
			Set<String> ledges = marinaResultSet.getLeadingEdge(tf);
			if(ledges != null){
				for(String str : ledges){
					DSGeneMarker ledge = markers.get(str);
					if(ledge == null){
						log.warn(tf+" ledge not found: "+str);
					}else
						genesInTargetList.add(ledge);
				}
			}
			DSItemList<DSGeneMarker> genesInTargetItemList = new CSItemList<DSGeneMarker>();
			genesInTargetItemList.addAll(genesInTargetList);
			mraResultSet.setGenesInTargetList(tfA, genesInTargetItemList);

			mraResultSet.setPValue(tfA, pval);
			mraResultSet.setMode(tfA, mode);
			mraResultSet.setOddRatio(tfA, odd);
			mraResultSet.setNES(tfA, nes);
			mraResultSet.setAbsNES(tfA, absnes);
		}
		
		return mraResultSet;
	}

	//value = mean/stdev
	private Map<DSGeneMarker, Double> getPairedValues(DSMicroarraySetView<DSGeneMarker, DSMicroarray> datasetView, String[] controls){
		Map<DSGeneMarker, Double> map = new HashMap<DSGeneMarker, Double>();
		DSItemList<DSGeneMarker> markers = datasetView.markers();
		DSItemList<DSMicroarray> arrays = datasetView.items();
		boolean[] isControl = new boolean[arrays.size()];
		int numControl = 0;
		for (int i = 0; i < arrays.size(); i++) {
			DSMicroarray array = arrays.get(i);
			for (String controlArray : controls){
				if(controlArray.equals(array.getLabel())){
					isControl[i] = true;
					numControl++;
				}
			}
		}
		for (int i = 0; i < markers.size(); i++) {
			DSGeneMarker m = markers.get(i);
			double[] controlValues = new double[numControl];
			int k = 0;
			for (int j = 0; j < arrays.size(); j++) {
				if(isControl[j])
					controlValues[k++] = datasetView.getValue(i, j);
			}
			DescriptiveStatistics stat = new DescriptiveStatistics(controlValues);
			double mean  = stat.getMean();
			double stdev = stat.getStandardDeviation();
			Double v = 0d;
			if(stdev != 0)  v = mean / stdev;
			map.put(m, v);
		}
		return map;
	}
}
