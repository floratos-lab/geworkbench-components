/**
 * 
 */
package org.geworkbench.components.geneontology2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import ontologizer.OntologizerCore;
import ontologizer.calculation.AbstractGOTermProperties;
import ontologizer.calculation.EnrichedGOTermsResult;
import ontologizer.go.OBOParserException;
import ontologizer.go.Term;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.ProgressBar;

/**
 * @author zji
 *
 */
public class GoAnalysis extends AbstractAnalysis implements ClusteringAnalysis {
	// it is necessary to implement ClusteringAnalysis for the AnalysisPanel to pick it up. No other known effect.
	
	static int getEntrezId(String geneSymbol) {
		if(geneDetails==null || geneDetails.get(geneSymbol)==null)
			return 0;
		return geneDetails.get(geneSymbol).getEntrezId();
	}
	
	private static final String NAMESPACE_LABEL = "namespace: ";

	static Log log = LogFactory.getLog(GoAnalysis.class);

	/**
	 * 
	 */
	public GoAnalysis() {
		super();
		parameterPanel = new GoAnalysisParameterPanel();
		setDefaultPanel(parameterPanel);
	}

	private static final long serialVersionUID = 5914151910006536646L;
	
	private GoAnalysisParameterPanel parameterPanel = new GoAnalysisParameterPanel();

	@Override
	public int getAnalysisType() {
		// not used, but required by the AbstractAnalysis interface
		return  AbstractAnalysis.ZERO_TYPE;
	}
	
	static HashMap<Integer, Set<String>> term2Gene;

	@SuppressWarnings("unchecked")
	// for the cast from Object input to DSMicroarraySetView
	public AlgorithmExecutionResults execute(Object input) {
		// ProgressBar code should be as isolated from other code as possible.
		final ProgressBar progressBar = ProgressBar
				.create(ProgressBar.INDETERMINATE_TYPE);
		progressBar.addObserver(this);
		progressBar.setTitle("GO Terms Analysis");
		progressBar.setMessage("GO Terms Analysis is ongoing. Please wait.");
		
		
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

		final String studySetFileName = "STUDYSET_TEMPORARY";
		final String populationSetFileName = "POPULATIONSET_TEMPORARY";
		File studySet = new File(studySetFileName);
		File populationSet = new File(populationSetFileName);
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(studySet));
			String[] changedGenesArray = parameterPanel.getChangedGeneList();
			changedGenes = new HashSet<String>();
			if (changedGenesArray == null || changedGenesArray.length == 0) {
				progressBar.dispose();
				return new AlgorithmExecutionResults(false,
						"Study set is empty.", null);
			}
			for (String gene : changedGenesArray) {
				pw.println(gene);
				changedGenes.add(gene);
			}
			pw.close();

			pw = new PrintWriter(new FileWriter(populationSet));
			String[] referenceGenesArray = parameterPanel
					.getReferenceGeneList();
			referenceGenes = new HashSet<String>();
			if (referenceGenesArray == null || referenceGenesArray.length == 0) {
				progressBar.dispose();
				return new AlgorithmExecutionResults(false,
						"Reference set is empty.", null);
			}
			for (String gene : referenceGenesArray) {
				pw.println(gene);
				referenceGenes.add(gene);
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
		arguments.studySet = studySetFileName;
		arguments.populationFile = populationSetFileName;
		arguments.associationFile = associationFileName;

		arguments.calculationName = parameterPanel.getCalculationMethod();
		arguments.correctionName = parameterPanel.getCorrectionMethod();
		// arguments.filterOutUnannotatedGenes = cmd.hasOption('i');
		// arguments.filterFile = cmd.getOptionValue('f');

		log.debug("arguments are " + arguments);

		long start = System.currentTimeMillis();
		analyzeCompleteObo(arguments.goTermsOBOFile);
		long finish = System.currentTimeMillis();
		log.debug("time building the complete ontology tree is "
					+ (finish - start) + " milliseconds");
		parseAnnotation(associationFileName);

		/*
		 * OntologizerCore has only one constructor that take the file names. We
		 * cannot directly set the studydset as a StudySetList, which is
		 * actually used in Ontologizer.
		 */
		OntologizerCore controller = null;
		try {
			controller = new OntologizerCore(arguments);
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
					"GO Terms Analysis is cancelled", null);
		}

		GoAnalysisResult analysisResult = new GoAnalysisResult(null,
				"Go Terms Analysis Result", controller);

		progressBar.dispose();
		if (this.stopAlgorithm) {
			return new AlgorithmExecutionResults(false,
					"GO Term Analysis is cancelled.", analysisResult);
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
			ProjectPanel.addToHistory(analysisResult, generateHistoryString(
					analysisResult.getCount()));
			return new AlgorithmExecutionResults(true,
					"GO Term Analysis succeeded.", analysisResult);
		}
	}
	
	private String generateHistoryString(int resultSize) {
		StringBuffer histStr = new StringBuffer();
		histStr.append(aspp.getDataSetHistory() );

		histStr.append( "\nGO Terms Analysis returned a results of "+resultSize+" rows." );
		
		return histStr.toString();
	}
	
	static Map<Integer, List<Integer> > ontologyChild = null;
	
	private static String[] namespace = {"molecular_function", "biological_process", "cellular_component"};
	static int[] NAMESPACE_ID = {-1, -2, -3};

	static class GoTermDetail {
		String name;
		String def;
		
		GoTermDetail(String name, String def) {
			this.name = name;
			this.def = def;
		}
	}
	
	static Map<Integer, GoTermDetail> termDetail = null;
	private void analyzeCompleteObo(String goTermsOBOFile) {
		termDetail = new HashMap<Integer, GoTermDetail>();
		// this one is needed to count 'exploded' tree efficiently
		ontologyChild = new HashMap<Integer, List<Integer> >();
		ontologyChild.put(NAMESPACE_ID[0], new ArrayList<Integer>());
		ontologyChild.put(NAMESPACE_ID[1], new ArrayList<Integer>());
		ontologyChild.put(NAMESPACE_ID[2], new ArrayList<Integer>());
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(goTermsOBOFile));
			String line = br.readLine();
			Integer id = null;
			while(line!=null) {
				if(line.startsWith("id: GO:")) {
					id = Integer.valueOf( line.substring(7) );
					String name = br.readLine().substring("name:".length());
					String thisNameSpace = br.readLine().substring(NAMESPACE_LABEL.length());
					String def = br.readLine().trim();
					while(!def.startsWith("def:") && def.length()>0)
						def = br.readLine();
						
					if(def.length()>0) {
						def = def.substring("def:\"".length(), def.indexOf("\" ["));
					}
					termDetail.put( id, new GoTermDetail(name, def) );

					// use namespace as the original single parent. this could be replaced if there are some real parents
					int parent = 0;
					if(thisNameSpace.equals(namespace[0]))
						parent = NAMESPACE_ID[0];
					else if(thisNameSpace.equals(namespace[1])) 
						parent = NAMESPACE_ID[1];
					else if(thisNameSpace.equals(namespace[2]))
						parent = NAMESPACE_ID[2];
					else {
						log.error("unknown namespace: "+thisNameSpace);
					}
					List<Integer> children = ontologyChild.get(parent);
					if(children==null) {
						children = new ArrayList<Integer>();
						ontologyChild.put(parent, children);
					}
					children.add(id);
				} else if (line.startsWith("is_a: GO:")) {
					Integer parent = Integer.valueOf( line.substring("is_a: GO:".length(), line.indexOf("!")).trim() );
					List<Integer> children = ontologyChild.get(parent);
					if(children==null) {
						children = new ArrayList<Integer>();
						ontologyChild.put(parent, children);
					}
					children.add(id);
				} else if (line.startsWith("relationship: part_of GO:")) {
					Integer parent = Integer.valueOf( line.substring("relationship: part_of GO:".length(), line.indexOf("!")).trim() );
					List<Integer> children = ontologyChild.get(parent);
					if(children==null) {
						children = new ArrayList<Integer>();
						ontologyChild.put(parent, children);
					}
					children.add(id);
				} 
				line = br.readLine();
			}
			br.close();
			log.debug("term count "+termDetail.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error("Ontology tree is not successfullly created due to FileNotException: "+e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Ontology tree is not successfullly created due to IOException: "+e.getMessage());
		}
	}
	
	static Map<String, GeneDetails> geneDetails;
	// this is necessary because there may be need to include more details
	private class GeneDetails {
		public GeneDetails(String geneTitle, int entrezId) {
			this.geneTitle = geneTitle;
			this.entrezId = entrezId;
		}

		private String geneTitle;
		private int entrezId;
		
		public int getEntrezId() {return entrezId; }
		
		public String toString() {return geneTitle; }
	}

//	private final static int ANNOTATION_INDEX_PROBSET_ID = 0;
	private final static int ANNOTATION_INDEX_ENTREZ_ID = 18;
	private final static int ANNOTATION_INDEX_GENE_TITLE = 13;
	private final static int ANNOTATION_INDEX_GENE_SYMBOL = 14;
	private final static int ANNOTATION_INDEX_BIOLOGICAL_PROCESS = 30;
	private final static int ANNOTATION_INDEX_CELLULAR_COMPONENT = 31;
	private final static int ANNOTATION_INDEX_MOLECULAR_FUNCTION = 32;

	static Set<String> changedGenes;
	static Set<String> referenceGenes;
	
	private void parseOneNameSpace(String namespaceAnnotation, String[] geneSymbols, String[] geneTitles, String[] entrezIds) {
		for (Integer goId : getGoId(namespaceAnnotation)) {
			Set<String> genes = term2Gene.get(goId);
			if (genes == null) {
				genes = new HashSet<String>();
				term2Gene.put(goId, genes);
			}
			for(int i=0; i<geneSymbols.length; i++) {
				String geneSymbol = geneSymbols[i];
				String geneTitle = "";
				if(i<geneTitles.length)
					geneTitle = geneTitles[i];
				int entrezId = 0;
				if(i<entrezIds.length)
					entrezId = Integer.parseInt(entrezIds[i].trim());
				genes.add(geneSymbol);
				if(!geneDetails.containsKey(geneSymbol)) {
					GeneDetails details = new GeneDetails(geneTitle, entrezId);
					geneDetails.put(geneSymbol, details);
				}
			}
		}
		
	}
	
	private void parseAnnotation(String annotationFileName) {
		term2Gene = new HashMap<Integer, Set<String> >();
		geneDetails = new HashMap<String, GeneDetails>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(annotationFileName));
			String line = br.readLine();
			int count = 0;
			while(line!=null) {
				while(line.startsWith("#"))
					line = br.readLine();
				line = line.substring(1, line.length()-2); // trimming the leading and trailing quotation mark
				String[] fields = line.split("\",\"");
//				String probesetId = fields[ANNOTATION_INDEX_PROBSET_ID];
				String geneSymbolField = fields[ANNOTATION_INDEX_GENE_SYMBOL];
				String biologicalProcess = fields[ANNOTATION_INDEX_BIOLOGICAL_PROCESS];
				String cellularComponent = fields[ANNOTATION_INDEX_CELLULAR_COMPONENT];
				String molecularFunction = fields[ANNOTATION_INDEX_MOLECULAR_FUNCTION];
				if (count > 0 && !geneSymbolField.equals("---") ) {
//					log.debug(probesetId + "|" + geneSymbolField + "|"
//							+ biologicalProcess + "|" + cellularComponent + "|"
//							+ molecularFunction);
					String[] geneSymbols = geneSymbolField.split("///");
					String[] geneTitles = fields[ANNOTATION_INDEX_GENE_TITLE].trim().split("///");
					String[] entrezIds = fields[ANNOTATION_INDEX_ENTREZ_ID].trim().split("///");
					for(int i=0; i<geneSymbols.length; i++)
						geneSymbols[i] = geneSymbols[i].trim(); 
					parseOneNameSpace(biologicalProcess, geneSymbols, geneTitles, entrezIds);
					parseOneNameSpace(cellularComponent, geneSymbols, geneTitles, entrezIds);
					parseOneNameSpace(molecularFunction, geneSymbols, geneTitles, entrezIds);
				}
				count++;
				
				line = br.readLine();
			}
			br.close();
			log.debug("total records in annotation file is "+count);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error("Annotation map is not successfullly created due to FileNotException: "+e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Annotation map is not successfullly created due to IOException: "+e.getMessage());
		}
	}
	
	private static List<Integer> getGoId(String annotationField) {
		List<Integer> ids = new ArrayList<Integer>();

		String[] goTerm = annotationField.split("///");
		for(String g: goTerm) {
			String[] f = g.split("//");
			try {
				ids.add( Integer.valueOf(f[0].trim()) );
			} catch (NumberFormatException e) {
				// do nothing for non-number case, like "---"
				// log.debug("non-number in annotation "+f[0]);
			}
		}
		return ids;
	}
	
	/* this is needed to catch the current dataset and consequently the loaded annotation */
	@SuppressWarnings("unchecked")
	@Subscribe
	public void receive(ProjectEvent e, Object source) {
		DSDataSet<CSMicroarray> dataset = e.getDataSet();
		if ((dataset != null) && (dataset instanceof CSMicroarraySet)) {
			CSMicroarraySet<CSMicroarray> d =(CSMicroarraySet<CSMicroarray>)dataset;
			parameterPanel.setDataset(d);
		}
	}
}

// interface DSAncillaryDataSet is the minimal requirement to fit in to geWorkbench analysis's result output framework
/**
 * Go Terms Analysis Result.
 * Wrapper for the result from ontologizer 2.0.
 */
class GoAnalysisResult extends CSAncillaryDataSet<CSMicroarray> {
	private static final long serialVersionUID = -337000604982427702L;
	static Log log = LogFactory.getLog(GoAnalysisResult.class);
	
	static class ResultRow {
		String name;
		String namespace;
		double p;
		double pAdjusted;
		int popCount;
		int studyCount;
		
		ResultRow(String name, String namespace, double p, double pAdjusted, int popCount, int studyCount) {
			this.name = name;
			this.namespace = namespace;
			this.p = p;
			this.pAdjusted = pAdjusted;
			this.popCount = popCount;
			this.studyCount = studyCount;
		}
		
		public String toString() {
			return name+"|"+namespace+"|"+p+"|"+pAdjusted+"|"+popCount+"|"+studyCount;
		}
	}

	ResultRow getRow(Integer goId) {
		return result.get(goId);
	}
	
	private Map<Integer, ResultRow> result = null;
	
	Map<Integer, ResultRow> getResult() {return result; }

	public int getCount() {
		if(result!=null)
			return result.size();
		else return 0;
	}

	/**
	 * Constructor based on a result from Ontologizer 2.0.
	 * 
	 * @param parent
	 * @param label
	 * @param ontologizerResult
	 */
	protected GoAnalysisResult(DSDataSet<CSMicroarray> parent, String label,
			EnrichedGOTermsResult ontologizerResult) {
		super(parent, label);
		result = new HashMap<Integer, ResultRow>();

		appendOntologizerResult(ontologizerResult);
	}
	
	/**
	 * Constructor from Ontologizer 2.0 controller.
	 * 
	 * @param parent
	 * @param label
	 * @param ontologizerResult
	 */
	protected GoAnalysisResult(DSDataSet<CSMicroarray> parent, String label,
			OntologizerCore ontologizerCore) {
		this(parent, label, ontologizerCore.calculateNextStudy());
		
		EnrichedGOTermsResult studySetResult = null;
		while ((studySetResult = ontologizerCore.calculateNextStudy()) != null) {
			appendOntologizerResult(studySetResult);

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
	}
	
	/**
	 * Append more result rows from another Ontologizer 2.0 result.
	 * @param ontologizerResult
	 */
	void appendOntologizerResult(EnrichedGOTermsResult ontologizerResult) {
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
			ResultRow row = new ResultRow(term.getName(), term
					.getNamespaceAsString(), prop.p, prop.p_adjusted, popCount,
					studyCount);
			result.put(term.getID().id, row);
		}
	}

	void addResultRow(int goId, ResultRow row) {
		result.put(goId, row);
	}
	
	public File getDataSetFile() {
		// no use. required by the interface
		return null;
	}

	public void setDataSetFile(File file) {
		// no use. required by the interface
	}
}
