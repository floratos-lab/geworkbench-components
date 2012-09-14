/**
 *
 */
package org.geworkbench.components.geneontology2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.SwingUtilities;

import ontologizer.ByteString;
import ontologizer.OntologizerCore;
import ontologizer.association.AssociationContainer;
import ontologizer.association.Gene2Associations;
import ontologizer.calculation.AbstractGOTermProperties;
import ontologizer.calculation.EnrichedGOTermsResult;
import ontologizer.go.OBOParserException;
import ontologizer.go.Term;
import ontologizer.go.TermID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.GoAnalysisResult;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.ProgressBar;

/**
 * Go Term Analysis component of geWorkbench.
 *
 * @author zji
 * @version $Id$
 */
public class GoAnalysis extends AbstractAnalysis implements ClusteringAnalysis {
	/* necessary to implement ClusteringAnalysis for the AnalysisPanel to pick it up. No other effect. */
	static Log log = LogFactory.getLog(GoAnalysis.class);
	private DSPanel<DSGeneMarker> selectorPanel = null;

	/**
	 *
	 */
	public GoAnalysis() {
		super();
		parameterPanel = new GoAnalysisParameterPanel();
		setDefaultPanel(parameterPanel);
	}

	private static final long serialVersionUID = 5914151910006536646L;
	private GoAnalysisParameterPanel parameterPanel;

	@SuppressWarnings("unchecked")
	public AlgorithmExecutionResults execute(Object input) {
		// ProgressBar code should be as isolated from other code as possible.
		final ProgressBar progressBar = ProgressBar
				.create(ProgressBar.INDETERMINATE_TYPE);
		progressBar.addObserver(this);
		progressBar.setTitle("GO Terms Analysis");
		progressBar.setMessage("GO Terms Analysis is under way. Please wait.");

		String associationFileName = parameterPanel.getAssociationFile();

		// handle the exceptional cases - missing parameters
		if (associationFileName.trim().length() == 0) {
			return new AlgorithmExecutionResults(false,
					"The association (annotation) file is not set.", null);
		}

		// launch the progress bar
		try {
			// do this because this thread is not EDT
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					progressBar.start();
				}
			});
			stopAlgorithm = false;

		} catch (InterruptedException e1) {
			e1.printStackTrace();
			progressBar.dispose();
			return new AlgorithmExecutionResults(false, e1.getMessage(), null);
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
			progressBar.dispose();
			return new AlgorithmExecutionResults(false, e1.getMessage(), null);
		}

		DSMicroarraySetView<DSGeneMarker, DSMicroarray> microArraySetView = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
		GoAnalysisResult analysisResult = new GoAnalysisResult(microArraySetView.getDataSet(), "Go Terms Analysis Result");

		final String studySetFileName = "STUDYSET_TEMPORARY";
		final String populationSetFileName = "POPULATIONSET_TEMPORARY";
		String	studySetFilePath = FilePathnameUtils.getTemporaryFilesDirectoryPath() + studySetFileName;
		String	populationSetFilePath = FilePathnameUtils.getTemporaryFilesDirectoryPath() + populationSetFileName;

		File studySet = new File(studySetFilePath);
		File populationSet = new File(populationSetFilePath);
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(studySet));
			String[] changedGenesArray = parameterPanel.getChangedGeneList();
			if (changedGenesArray == null || changedGenesArray.length == 0) {
				progressBar.dispose();
				pw.close();
				return new AlgorithmExecutionResults(false,
						"Study set is empty.", null);
			}
			for (String gene : changedGenesArray) {
				pw.println(gene);
				analysisResult.addChangedGenes(gene);
			}
			pw.close();

			pw = new PrintWriter(new FileWriter(populationSet));
			String[] referenceGenesArray = parameterPanel
					.getReferenceGeneList();
			if (referenceGenesArray == null || referenceGenesArray.length == 0) {
				progressBar.dispose();
				return new AlgorithmExecutionResults(false,
						"Reference set is empty.", null);
			}
			for (String gene : referenceGenesArray) {
				pw.println(gene);
				analysisResult.addReferenceGenes(gene);
			}
			pw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			progressBar.dispose();
			return new AlgorithmExecutionResults(false,
					"Failed to create temporary file: " + e1.getMessage(), null);
		}

		final OntologizerCore.Arguments arguments = new OntologizerCore.Arguments();
		/*
		 * Ontologizer does not handle the null pointer for this. The name MUST
		 * be set.
		 */
		arguments.goTermsOBOFile = parameterPanel.getOntologyFile();
		arguments.studySet = studySetFilePath;
		arguments.populationFile = populationSetFilePath;
		arguments.associationFile = associationFileName;

		arguments.calculationName = parameterPanel.getCalculationMethod();
		arguments.correctionName = parameterPanel.getCorrectionMethod();
		/* leave other argument as default */

		GoAnalysisResult.parseOboFile(arguments.goTermsOBOFile);
		if (this.stopAlgorithm) {
			progressBar.dispose();
			return new AlgorithmExecutionResults(false,
					"GO Terms Analysis cancelled", null);
		}

		if (this.stopAlgorithm) {
			progressBar.dispose();
			return new AlgorithmExecutionResults(false,
					"GO Terms Analysis cancelled", null);
		}

		/*
		 * OntologizerCore has only one constructor that take the file names. We
		 * cannot directly set the studydset as a StudySetList, which is
		 * actually used in Ontologizer.
		 */
		OntologizerCore ontologizerCore = null;
		try {
			ontologizerCore = new OntologizerCore(arguments);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			progressBar.dispose();
			return new AlgorithmExecutionResults(
					false,
					"FileNotFoundException from Ontologizer: " + e.getMessage(),
					null);
		} catch (IOException e) {
			e.printStackTrace();
			progressBar.dispose();
			return new AlgorithmExecutionResults(false,
					"IOException from Ontologizer: " + e.getMessage(), null);
		} catch (OBOParserException e) {
			e.printStackTrace();
			progressBar.dispose();
			return new AlgorithmExecutionResults(false,
					"OBOParserException from Ontologizer: " + e.getMessage(),
					null);
		}

		// note there is no way to really cancel something that is not listening
		// - e.g. inside OntologizerCore(arguments)
		if (this.stopAlgorithm) {
			progressBar.dispose();
			return new AlgorithmExecutionResults(false,
					"GO Terms Analysis cancelled", null);
		}

		EnrichedGOTermsResult studySetResult = null;
		while ((studySetResult = ontologizerCore.calculateNextStudy()) != null) {
			appendOntologizerResult(analysisResult, studySetResult);

			if (this.stopAlgorithm) {
				progressBar.dispose();
				return new AlgorithmExecutionResults(false,
						"GO Terms Analysis cancelled", null);
			}

			// this is not needed except for understanding the result structure
			if (log.isDebugEnabled()) {
					if (studySetResult.getSize() > 0) {
						File outFile = new File("ONTOLOGIZER_RESULT");
						studySetResult.writeTable(outFile);
						log.debug("Ontologizer got result.");
					} else {
						log.debug("Ontologizer got empty result. Size="
								+ studySetResult.getSize());
					}
			}
		}
		
		HashMap<Integer, Set<String>> term2Gene = new HashMap<Integer, Set<String> >();
		AssociationContainer container = ontologizerCore.getGoAssociations();
		for(ByteString gene : container.getAllAnnotatedGenes()) {
			String geneString = gene.toString();
			Gene2Associations associations = container.get(gene);
			for(TermID termId : associations.getAssociations()) {
				Integer id = termId.id;
				Set<String> set = term2Gene.get(id);
				if(set==null) {
					set = new HashSet<String>();
					term2Gene.put(id, set);
				}
				if(!set.contains(geneString)) {
					set.add(gene.toString());
				}
			}
		}

		analysisResult.setTerm2Gene(term2Gene);

		progressBar.dispose();
		if (this.stopAlgorithm) {
			return new AlgorithmExecutionResults(false,
					"GO Term Analysis cancelled.", analysisResult);
		} else {
			/* after the analysis, delete the temporary file */
			if (!studySet.delete()) {
				log.error("Error in trying to delete the temporary file "
						+ studySet.getAbsolutePath());
			}
			if (!populationSet.delete()) {
				log.error("Error in trying to delete the temporary file "
						+ populationSet.getAbsolutePath());
			}
			HistoryPanel.addToHistory(analysisResult, generateHistoryString(
					analysisResult.getCount()));
			return new AlgorithmExecutionResults(true,
					"GO Term Analysis succeeded.", analysisResult);
		}
	}

	/**
	 * Append more result rows from another Ontologizer 2.0 result.
	 * @param ontologizerResult
	 */
	private void appendOntologizerResult(GoAnalysisResult result, EnrichedGOTermsResult ontologizerResult) {
		Iterator<AbstractGOTermProperties> iter = ontologizerResult.iterator();

		while (iter.hasNext()) {
			AbstractGOTermProperties prop = iter.next();
			Term term = prop.goTerm;
			int popCount = 0, studyCount = 0;
			for (int i = 0; i < prop.getNumberOfProperties(); i++) {
				/*
				 * the index may be fixed, but not 'visible' from the
				 * AbstractGOTermProperties's interface
				 */
				if (prop.getPropertyName(i).equalsIgnoreCase("Pop.term")) {
					popCount = Integer.parseInt(prop.getProperty(i));
				} else if (prop.getPropertyName(i).equalsIgnoreCase(
						"Study.term")) {
					studyCount = Integer.parseInt(prop.getProperty(i));
				} else {
					// log.trace(i+":"+prop.getPropertyName(i)+"="+prop.getProperty(i));
				}
			}
			result.addResultRow(term.getID().id, term.getName(), term
					.getNamespaceAsAbbrevString(), prop.p, prop.p_adjusted, popCount,
					studyCount);
		}
	}

	private String generateHistoryString(int resultSize) {
		StringBuffer histStr = new StringBuffer();
		histStr.append(aspp.getDataSetHistory() );

		histStr.append( "\nGO Terms Analysis result returned with "+resultSize+" rows." );

		return histStr.toString();
	}

	@Subscribe
	public void receive(GeneSelectorEvent e, Object source) {
		if (e.getPanel() != null) {
			this.selectorPanel = e.getPanel();
			((GoAnalysisParameterPanel) aspp).setSelectorPanel(((GoAnalysisParameterPanel) aspp), this.selectorPanel);
		} else
			log.debug("GO Received Gene Selector Event: Selection panel sent was null");
	}

	
	/* this is needed to catch the current dataset and consequently the loaded annotation */
	@Subscribe
	public void receive(ProjectEvent e, Object source) {
		DSDataSet<? extends DSBioObject> dataset = e.getDataSet();
		if ((dataset != null) && (dataset instanceof CSMicroarraySet)) {
			CSMicroarraySet d =(CSMicroarraySet)dataset;
			parameterPanel.setDataset(d);
		}
	}
}

