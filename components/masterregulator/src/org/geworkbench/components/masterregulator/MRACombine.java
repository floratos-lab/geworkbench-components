package org.geworkbench.components.masterregulator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.EdgeInfo;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.GeneOntologyUtil;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMasterRegulatorResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMasterRagulatorResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;

import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.builtin.projects.DataSetNode;
import org.geworkbench.builtin.projects.DataSetSubNode;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.builtin.projects.SaveFileFilterFactory;
import org.geworkbench.builtin.projects.SaveFileFilterFactory.CustomFileFilter;
import org.geworkbench.builtin.projects.history.HistoryPanel;

import org.geworkbench.components.masterregulator.TAnalysis.TAnalysisException;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.util.ProgressDialog;
import org.geworkbench.util.ProgressItem;
import org.geworkbench.util.ProgressTask;

/**
 * @author Min You
 * @version $Id 
 */
public class MRACombine extends AbstractAnalysis implements ClusteringAnalysis {
	private static final long serialVersionUID = 940204157465957195L;

	private Log log = LogFactory.getLog(this.getClass());
	private final String analysisName = "MRA-Combine";

	private MRACombinePanel mraCombinePanel = new MRACombinePanel();
	private ProgressDialog pd = ProgressDialog.getInstance(false);

	private class ResultWrapper {
		private AlgorithmExecutionResults rst = null;

		private void setResult(AlgorithmExecutionResults rst) {
			this.rst = rst;
		}

		private AlgorithmExecutionResults getResult() {
			return this.rst;
		}
	}

	public MRACombine() {
		setDefaultPanel(mraCombinePanel);
	}

	@Override
	public AlgorithmExecutionResults execute(Object input) {
		ResultWrapper rw = new ResultWrapper();
		MRATask task = new MRATask(ProgressItem.BOUNDED_TYPE,
				"Executing MRA-Combine Analysis: started", input, rw);
		pd.executeTask(task);
		while (!task.isDone()) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return rw.getResult();
	}

	private class MRATask extends ProgressTask<Void, String> {
		Object input;
		ResultWrapper rw = null;

		MRATask(int pbtype, String message, Object input, ResultWrapper rw) {
			super(pbtype, message);
			this.input = input;
			this.rw = rw;
		}

		@Override
		protected void done() {
			pd.removeTask(this);
		}

		@Override
		protected void process(List<String> chunks) {
			for (String message : chunks) {
				if (isCancelled())
					return;
				this.setMessage(message);
			}
		}

		@Override
		protected Void doInBackground() {
			try {
				rw.setResult(executeInBackground());
			} catch (Exception e) {
				rw.setResult(new AlgorithmExecutionResults(false, "Exception: "
						+ e, null));
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		private AlgorithmExecutionResults executeInBackground()
				throws Exception {
			// read input data, dataset view, dataset, etc.
			if (!(input instanceof DSMicroarraySetView)) {
				return new AlgorithmExecutionResults(false,
						"Input dataset for MRA-Combine analysis should be a MicroarraySet.\n"
								+ "But you selected a "
								+ input.getClass().getName(), null);
			}
			;

			if (mraCombinePanel.use5colnetwork()) {
				return new AlgorithmExecutionResults(false,
						"Currently not support network in "
								+ mraCombinePanel.marina5colformat, null);
			}

			DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
			DSMicroarraySet maSet = view.getMicroarraySet();
			AdjacencyMatrixDataSet amSet = mraCombinePanel
					.getAdjMatrixDataSet();

			if (amSet == null || amSet.getMatrix() == null) {
				return new AlgorithmExecutionResults(false,
						"Network (Adjacency Matrix) has not been loaded yet.",
						null);
			}
			;

			// validate data and parameters.
			ParamValidationResults validation = validateParameters();
			if (!validation.isValid()) {
				return new AlgorithmExecutionResults(false,
						validation.getMessage(), null);
			}
			if (isCancelled())
				return null;
			// analysis
			DSMasterRagulatorResultSet<DSGeneMarker> mraResultSet = new CSMasterRegulatorResultSet<DSGeneMarker>(
					maSet, analysisName, view.markers().size());
			// t-analysis
			log.info("Executing T Analysis");
			Map<DSGeneMarker, Double> values = null;
			try {
				TAnalysis tTestAnalysis = new TAnalysis(view);
				values = tTestAnalysis.calculateDisplayValues();
			} catch (TAnalysisException e1) {
				return new AlgorithmExecutionResults(false, e1.getMessage(),
						null);
			}
			if (values == null)
				return new AlgorithmExecutionResults(false,
						"The set of display values is set null.", null);
			mraResultSet.setValues(values);

			sortByValue(values, mraResultSet);

			AlgorithmExecutionResults results = new AlgorithmExecutionResults(
					true, "MRA-Combine Analysis", null);

			CustomFileFilter filter = null;
			File f = null;

			f = new File("MRA_Combine.txt");
			filter = SaveFileFilterFactory.createTxtFileFilter();

			JFileChooser jFileChooser1 = new JFileChooser(f);
			jFileChooser1.setFileFilter(filter);
			String lastDir = null;
			if ((lastDir = mraCombinePanel.getLastDir()) != null) {
				jFileChooser1.setCurrentDirectory(new File(lastDir));
			}

			jFileChooser1.setSelectedFile(f);

			String newFileName = null;
			if (JFileChooser.APPROVE_OPTION == jFileChooser1
					.showSaveDialog(null)) {
				newFileName = jFileChooser1.getSelectedFile().getPath();
				if (!filter.accept(new File(newFileName))) {
					newFileName += "." + filter.getExtension();
				}

			} else {
				return results;
			}

			mraCombinePanel.saveLastDir(jFileChooser1.getSelectedFile()
					.getParent());

			if (new File(newFileName).exists()) {
				int o = JOptionPane
						.showConfirmDialog(
								null,
								"The file already exists. Do you wish to overwrite it?",
								"Replace the existing file?",
								JOptionPane.YES_NO_OPTION);
				if (o != JOptionPane.YES_OPTION) {
					return results;
				}

			}

			// writeToFile(mraResultSet, newFileName);

			BufferedWriter writer = null;
			File file = null;

			try {
				file = new File(newFileName);
				file.createNewFile();
				if (!file.canWrite()) {
					JOptionPane.showMessageDialog(null,
							"Cannot write to specified file.");
					return results;
				}
				writer = new BufferedWriter(new FileWriter(file));

				try {

					writeExperimentData(mraResultSet, writer);
					List<String[]> mraList = mraCombinePanel.getMraDataList();

					for (int i = 0; i < mraList.size(); i++) {
						if(isCancelled()) return null;						 
						publish("Executing Master Regulator Analysis: "+100*i/mraList.size()+"%");
						setProgress(100*i/mraList.size());
						String[] mraData = mraList.get(i);
						writeMRAData(mraData, amSet, mraResultSet, writer);
						writer.flush();

					}

				} catch (Exception ex) {
					throw ex;
				} finally {
					if (writer != null)
						writer.close();
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				log.error(ex.getMessage());

			}
			 
			String historyStr = generateHistoryString(view);

			HistoryPanel.addToHistory(mraResultSet, historyStr);

			return results;
		}
	}

	static void sortByValue(final Map<DSGeneMarker, Double> values,
			DSMasterRagulatorResultSet<DSGeneMarker> mraResultSet) {
		Map<DSGeneMarker, Integer> gene2rankMap = new HashMap<DSGeneMarker, Integer>();
		List<DSGeneMarker> genes = new ArrayList<DSGeneMarker>();
		genes.addAll(values.keySet());

		// sort genes by value
		Collections.sort(genes, new Comparator<DSGeneMarker>() {
			public int compare(DSGeneMarker m1, DSGeneMarker m2) {
				return values.get(m1).compareTo(values.get(m2));
			}
		});
		mraResultSet.setMinValue(values.get(genes.get(0)));
		mraResultSet.setMaxValue(values.get(genes.get(genes.size() - 1)));

		// give same ranks to genes with same value
		// TODO we should use standard library, e.g. apache commons math to do
		// this
		gene2rankMap.put(genes.get(0), 0);
		double lastValue = values.get(genes.get(0));
		int lastRank = 0;
		for (int i = 1; i < genes.size(); i++) {
			int rank = i;
			DSGeneMarker marker = genes.get(i);
			double value = values.get(marker);
			if (value == lastValue) {
				rank = lastRank;
			}
			gene2rankMap.put(marker, rank);
			lastValue = value;
			lastRank = rank;
		}
		mraResultSet.setRanks(gene2rankMap);
	}

	public ParamValidationResults validateParameters() {

		ParamValidationResults answer = new ParamValidationResults(true,
				"validate");
		return answer;
	}

	private String generateHistoryString(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
		StringBuffer histStr = new StringBuffer(
				"Generated with MRA run with parameters:\n\n");
		histStr.append(
				"[PARA] Load Network: "
						+ mraCombinePanel.getAdjMatrixDataSet()
								.getDataSetName()).append("\n");

		histStr.append(generateHistoryForMaSetView(view));

		return histStr.toString();
	}

	@SuppressWarnings("rawtypes")
	@Subscribe
	public void receive(org.geworkbench.events.ProjectNodeRemovedEvent e,
			Object source) {
		DSDataSet dataSet = e.getAncillaryDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			this.mraCombinePanel
					.removeAdjMatrixToCombobox((AdjacencyMatrixDataSet) dataSet);
		}
	}

	@SuppressWarnings("rawtypes")
	@Subscribe
	public void receive(org.geworkbench.events.ProjectNodeRenamedEvent e,
			Object source) {
		DSDataSet dataSet = e.getDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			this.mraCombinePanel.renameAdjMatrixToCombobox(
					(AdjacencyMatrixDataSet) dataSet, e.getOldName(),
					e.getNewName());
		}
	}

	@SuppressWarnings("rawtypes")
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
		DSDataSet dataSet = e.getDataSet();
		if (!(dataSet instanceof DSMicroarraySet)) {
			return;
		}

		mraCombinePanel.setMicroarraySet((DSMicroarraySet) dataSet);

		ProjectSelection selection = ProjectPanel.getInstance().getSelection();
		DataSetNode dNode = selection.getSelectedDataSetNode();
		if (dNode == null) {
			return;
		}

		String currentTargetSet = this.mraCombinePanel.getSelectedAdjMatrix();
		this.mraCombinePanel.clearAdjMatrixCombobox();

		Enumeration children = dNode.children();
		while (children.hasMoreElements()) {
			Object obj = children.nextElement();
			if (obj instanceof DataSetSubNode) {
				DSAncillaryDataSet ads = ((DataSetSubNode) obj)._aDataSet;
				if (ads instanceof AdjacencyMatrixDataSet) {
					this.mraCombinePanel
							.addAdjMatrixToCombobox((AdjacencyMatrixDataSet) ads);
					if (currentTargetSet != null
							&& StringUtils.equals(ads.getDataSetName(),
									currentTargetSet.trim())) {
						mraCombinePanel.setSelectedAdjMatrix(ads
								.getDataSetName());
					}
				}
			}
		}
	}

	private void writeExperimentData(
			DSMasterRagulatorResultSet<DSGeneMarker> mraResultSet,
			BufferedWriter writer) throws IOException {
		writer.write("^DATABASE=CTD2\n");
		writer.write("^MR_Experiment_Header(one per file)\n");
		writer.write("!master_regulator_method=" + mraCombinePanel.getMethod()
				+ "\n");
		writer.write("!score_type=" + mraCombinePanel.getScoreType()
				+ "\n");
		writer.write("!total_number_of_genes=" + mraResultSet.getMarkerCount()
				+ "\n");
		writer.write("!abs_max_z-score_observed="
				+ Math.max(Math.abs(mraResultSet.getMinValue()),
						Math.abs(mraResultSet.getMaxValue())) + "\n");
		writer.write("!species=homo sapiens\n");
		writer.write("!disease_condition=glioblastoma\n");
		writer.write("!phenotype1-case=mesenchymal\n");
		writer.write("!phenotype2-contal=proneural\n");
		writer.write("!confidence_value_type="
				+ mraCombinePanel.getconfidenceType() + "\n");

	}

	private void writeMRAData(String[] mraData, AdjacencyMatrixDataSet amSet,
			DSMasterRagulatorResultSet<DSGeneMarker> mraResultSet,
			BufferedWriter writer) throws IOException {

		DSMicroarraySet maSet = mraResultSet.getMicroarraySet();
		DSGeneMarker mraMarker = maSet.getMarkers().get(mraData[0].trim());
		AdjacencyMatrix adjMatrix = amSet.getMatrix();
		Map<DSGeneMarker, Set<EdgeInfo>> genesInRegulonMap = adjMatrix
				.getEdgeInfoMap(mraMarker, maSet);
		Set<DSGeneMarker> genesInRegulonList = genesInRegulonMap.keySet();
		writer.write("^MR_GENE_ID=" + mraMarker.getGeneId() + "\n");
		writer.write("!gene_symbol=" + mraData[0] + "\n");
		writer.write("!MR_DE_RANK=" + mraResultSet.getRank(mraMarker) + "\n");
		writer.write("!gene_in_regulon=" + genesInRegulonMap.keySet().size()
				+ "\n");
		writer.write("!" + mraCombinePanel.getScoreType() + "=" + mraData[1].trim()
				+ "\n");
		writer.write("!DE=" + mraResultSet.getValue(mraMarker) + "\n");	
	
		writer.write("#Target_Entrez_ID=EntrezID of target gene\n");
		writer.write("#Target_Symbol=gene symbol for target gene\n");
		writer.write("#Target_type=type of gene(TF/K/other)\n");
		writer.write("#Conf=network edge confidence value\n");
		writer.write("#DE_Rank=differential expression rank\n");
		writer.write("#DSpearmans=Spearman's Correlation TF with target\n");
		writer.write("!target_table_begin\n");
		writer.write("Target_Entrez_ID\tTarget_Symbol\tTarget_Type\tConf\tDE_Rank\tSpearmans\n");

		for (DSGeneMarker regulonMarker : genesInRegulonList) {
			
			// double value = mraResultSet.getValue(marker);
			int rank = mraResultSet.getRank(regulonMarker);

			SpearmansCorrelation SC = new SpearmansCorrelation();
			double spearCor = 0.0f;
			float confValue = 0.0f;
			double[] arrayData1 = maSet.getRow(mraMarker);
			double[] arrayData2 = maSet.getRow(regulonMarker);

			spearCor = new Float(SC.correlation(arrayData1, arrayData2));

			confValue = genesInRegulonMap.get(regulonMarker).iterator().next().value;

			String geneType = GeneOntologyUtil
					.checkMarkerFunctions(regulonMarker);
			if (geneType == null || geneType.trim().equals(""))
				geneType = "none";
			writer.write(regulonMarker.getGeneId() + "\t"
					+ regulonMarker.getGeneName() + "\t" + geneType + "\t"
					+ confValue + "\t" + rank + "\t" + spearCor + "\n");

		}	 
		writer.write("!target_table_end\n");

	}

	 

}
