package org.geworkbench.components.netboost;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

/**
 * NetBoost Analysis
 * 
 * @author ch2514
 * @version $Id: NetBoostAnalysis.java,v 1.9 2009-09-10 16:40:26 chiangy Exp $
 */

public class NetBoostAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// variable
	private static Log log = LogFactory.getLog(NetBoostAnalysis.class);

	private static final String PROPERTIES_FILE = "/netboost.properties";

	private static final String KEY_MODEL_NAMES = "netboost.models.names";

	private static final String KEY_MODEL_DESCRIPTIONS = "netboost.models.descriptions";

	private static final String KEY_MODEL_COMMANDS = "netboost.models.commands";

	private static final String PROPERTIES_DELIMITER = ";";

	public static final String TRAINING_EX = "trainingExample";

	public static final String BOOST_ITER = "boostingIteration";

	public static final String SUBGRAPH_COUNT = "subgraphCounting";

	public static final String CROSS_VALID = "crossValidationFolds";

	public static final String SELECTED_MODEL_COMMANDS = "selectedModelCommands";

	private static String[] modelNames = { "LPA", "RDG", "RDS", "DMC", "AGV",
			"SMW", "DMR" };

	private static String[] modelDescriptions = {
			"Linear Preferential Attachment", "Random Growing Networks",
			"Random Static Networks", "Dynamic Multi-Cost Networks",
			"Aging Vertex Graph", "Small World", "Random Mutations" };

	private static String[] modelCmds = { "LPA(N,m,LPA_offset)", "RDG(N,m)",
			"RDS(N,m,RDS_isdirected)", "DMC(N,m,rand)", "AGV(N,m,rand,1)",
			"SMW(N,m,rand,0)", "DMR(N,rand,rand)" };

	private int localAnalysisType;

	private final String analysisName = "NetBoost";

	private NetBoostParamPanel paramPanel;

	static {
		initModels();
	}

	public NetBoostAnalysis() {
		this.localAnalysisType = AbstractAnalysis.NETBOOST_TYPE;
		paramPanel = new NetBoostParamPanel();
		setDefaultPanel(paramPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonParameters()
	 */
	protected Map<Serializable, Serializable> getBisonParameters() {
		Map<Serializable, Serializable> parameterMap = new HashMap<Serializable, Serializable>();

		parameterMap.put(TRAINING_EX, new Integer(((NetBoostParamPanel) aspp)
				.getTrainingExamples()));
		parameterMap.put(BOOST_ITER, new Integer(((NetBoostParamPanel) aspp)
				.getBoostingIterations()));
		parameterMap.put(SUBGRAPH_COUNT, ((NetBoostParamPanel) aspp)
				.getSubgraphCountingMethods());
		parameterMap.put(CROSS_VALID, new Integer(((NetBoostParamPanel) aspp)
				.getCrossValidationFolds()));

		// assumes the number of model names == number of model selections ==
		// number of model commands
		// should already be checked at the analysis param panel level
		boolean[] b = ((NetBoostParamPanel) aspp).getSelectedModels();
		String selectedModelCmds = "";
		for (int i = 0; i < modelCmds.length; i++) {
			if (b[i])
				selectedModelCmds += modelCmds[i] + PROPERTIES_DELIMITER;
		}
		parameterMap.put(SELECTED_MODEL_COMMANDS, selectedModelCmds);

		return parameterMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getAnalysisName()
	 */
	public String getAnalysisName() {
		return this.analysisName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.bison.model.analysis.Analysis#execute(java.lang.Object)
	 */
	public AlgorithmExecutionResults execute(Object input) {
		// inform the user that only remote service is available
		return new AlgorithmExecutionResults(
				false,
				"Net Boost does not have a local algorithm service.  Please choose \"Grid\" on the Net Boost analysis panel.",
				null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractAnalysis#getAnalysisType()
	 */
	public int getAnalysisType() {
		return this.localAnalysisType;
	}

	/**
	 * <code>AbstractAnalysis</code> method
	 * 
	 * @return Analysis type
	 */
	public String getType() {
		return this.analysisName;
	}

	/**
	 * 
	 * @param projectEvent
	 * @param source
	 */
	@Subscribe
	public void receive(ProjectEvent projectEvent, Object source) {
		log.debug("NetBoost Analysis received project event.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonReturnType()
	 */
	@Override
	public Class<?> getBisonReturnType() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @return
	 */
	public static String[] getModelNames() {
		return modelNames;
	}

	/**
	 * 
	 * @return
	 */
	public static String[] getModelDescriptions() {
		return modelDescriptions;
	}

	/**
	 * 
	 * @return
	 */
	public static String[] getModelCommands() {
		return modelCmds;
	}

	/**
	 * 
	 * 
	 */
	private static void initModels() {
		log.debug("Init Models...");
		InputStream reader = null;
		try {
			reader = Class.forName(
					"org.geworkbench.components.netboost.NetBoostAnalysis")
					.getResourceAsStream(PROPERTIES_FILE);
			log.info("Reading from properties file: " + PROPERTIES_FILE);
			System.getProperties().load(reader);
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new SecurityManager());
			}
			reader.close();

			String names = System.getProperty(KEY_MODEL_NAMES);
			String descs = System.getProperty(KEY_MODEL_DESCRIPTIONS);
			String cmds = System.getProperty(KEY_MODEL_COMMANDS);
			log.info("Models read:\n\tnames=[" + names + "]\n\tdescriptions=["
					+ descs + "]\n\tcommands=[" + cmds + "]");

			if ((names != null) && (names.length() > 0) && (cmds != null)
					&& (cmds.length() > 0)) {

				log.debug("Model Names=[" + names + "]");
				if (descs != null)
					log.debug("Model Descriptions=[" + descs + "]");
				else
					log.debug("No model descriptions.");
				log.debug("Model Commands=[" + cmds + "]");

				StringTokenizer stNames = new StringTokenizer(names,
						PROPERTIES_DELIMITER);
				StringTokenizer stDescs = null;
				StringTokenizer stCmds = new StringTokenizer(cmds,
						PROPERTIES_DELIMITER);
				int numNames = stNames.countTokens();
				int numCmds = stCmds.countTokens();
				log.debug("numNames=" + numNames + ", numCmds=" + numCmds);
				if (numNames == numCmds) {
					modelNames = new String[numNames];
					modelCmds = new String[numCmds];
					int i = 0;
					while (stNames.hasMoreTokens() && stCmds.hasMoreTokens()
							&& (i < numNames)) {
						modelNames[i] = stNames.nextToken();
						modelCmds[i] = stCmds.nextToken();
						i++;
					}
					if (descs != null) {
						stDescs = new StringTokenizer(descs,
								PROPERTIES_DELIMITER);
						int numDescs = stDescs.countTokens();
						if (numNames != numDescs) {
							log
									.warn("Model name and description mismatch.  Please double check netboost properties file.");
						}
						modelDescriptions = new String[numDescs];
						int j = 0;
						while (stDescs.hasMoreTokens() && (j < numDescs)) {
							modelDescriptions[j] = stDescs.nextToken();
							j++;
						}
					} else {
						modelDescriptions = null;
						log
								.warn("No model descriptions specified in the properties file.  Model descriptions will not appear in NetBoost Analysis");
					}
				} else {
					log
							.error("Number of model names and number of model commands do not match.  NetBoost is using default model list.");
				}

			} else {
				String msg = "";
				if ((names == null) || (names.length() <= 0))
					msg += "No model names specified in the properties file.  ";
				if ((descs == null) || (descs.length() <= 0))
					msg += "No model descriptions specified in the properties file.  ";
				if ((cmds == null) || (cmds.length() <= 0))
					msg += "No model commands specified in the properties file.  ";
				log.error(msg + "NetBoost is using default model list.");
			}
			log.debug("Model init complete.");
		} catch (Exception e) {
			log.error("Cannot load netboost model properties file: "
					+ e.getMessage() + ": using default list of models", e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#useMicroarraySetView()
	 */
	@Override
	protected boolean useMicroarraySetView() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#useOtherDataSet()
	 */
	@Override
	protected boolean useOtherDataSet() {
		return true;
	}

	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet<?> refMASet) {
		// TODO Auto-generated method stub
		return new ParamValidationResults(true,"No Checking");
	}
}
