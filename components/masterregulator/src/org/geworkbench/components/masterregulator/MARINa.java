package org.geworkbench.components.masterregulator;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.builtin.projects.DataSetNode;
import org.geworkbench.builtin.projects.DataSetSubNode;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;

/**
 * @author yc2480
 * @version $Id$
 */
public class MARINa extends AbstractGridAnalysis implements
		ClusteringAnalysis {
	private static final long serialVersionUID = 940204157465957195L;
	
	private static Log log = LogFactory.getLog(MARINa.class);
	private final String analysisName = "MRA"; // don't change this. this the name used by the server side code (caGrid service)
    private static final Pattern pattern = Pattern.compile("^mra\\d+$");
	private MARINaPanel mraAnalysisPanel = new MARINaPanel();

	public MARINa() {
		setDefaultPanel(mraAnalysisPanel);
	}

	@Override
	public AlgorithmExecutionResults execute(Object input) {
		return new AlgorithmExecutionResults(
				false,
				"MARINa is a caGrid-service-only component. Please choose \"Grid\" on the MARINa analysis \"Services\" panel. A related method, MRA-FET, is available as a local analysis",
				null);
	}

	@Override
	public ParamValidationResults validateParameters() {
		try {
			if ((mraAnalysisPanel.getPValue() < 0)
					|| (mraAnalysisPanel.getPValue() > 1)) {
				return new ParamValidationResults(false,
						"P-value should be a number within 0.0~1.0");
			}
		} catch (NumberFormatException nfe) {
			return new ParamValidationResults(false,
					"P-value should be a number");
		} 
		ParamValidationResults answer = new ParamValidationResults(true,
				"validate");
		return answer;
	} 
	
	@SuppressWarnings("rawtypes")
	@Subscribe
	public void receive(org.geworkbench.events.ProjectNodeRemovedEvent e,
			Object source) {
		DSDataSet dataSet = e.getAncillaryDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			this.mraAnalysisPanel
					.removeAdjMatrixToCombobox((AdjacencyMatrixDataSet) dataSet);
		}
	}
	@SuppressWarnings("rawtypes")
	@Subscribe
	public void receive(org.geworkbench.events.ProjectNodeRenamedEvent e,
			Object source) {
		DSDataSet dataSet = e.getDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			this.mraAnalysisPanel
					.renameAdjMatrixToCombobox((AdjacencyMatrixDataSet)dataSet, e.getOldName(),e.getNewName());
		}
	}
	@SuppressWarnings({ "rawtypes","unchecked" })
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
		DSDataSet dataSet = e.getDataSet();
		if (!(dataSet instanceof DSMicroarraySet)) {
			return;
		}

		mraAnalysisPanel.setMicroarraySet((DSMicroarraySet)dataSet);

		ProjectSelection selection = ProjectPanel.getInstance().getSelection();
        DataSetNode dNode = selection.getSelectedDataSetNode();
        if(dNode == null){
        	return;
        }

        String currentTargetSet = this.mraAnalysisPanel.getSelectedAdjMatrix();
        this.mraAnalysisPanel.clearAdjMatrixCombobox();
        ttesthm.clear();
        Enumeration children = dNode.children();
        while (children.hasMoreElements()) {
            Object obj = children.nextElement();
            if (obj instanceof DataSetSubNode) {
                DSAncillaryDataSet ads = ((DataSetSubNode) obj)._aDataSet;
                if (ads instanceof AdjacencyMatrixDataSet) {
                    this.mraAnalysisPanel.addAdjMatrixToCombobox((AdjacencyMatrixDataSet) ads);                    
                    if (currentTargetSet != null && StringUtils.equals(ads.getDataSetName(), currentTargetSet.trim())) {
                    	mraAnalysisPanel.setSelectedAdjMatrix(ads.getDataSetName());
        			}
                } else if (ads instanceof DSSignificanceResultSet){
                	ttesthm.put(ads.getLabel(), (DSSignificanceResultSet<DSGeneMarker>)ads);
                }
            }
        }
        log.debug("ProjectEvent processed");
	}
	private HashMap<String, DSSignificanceResultSet<DSGeneMarker>> ttesthm = new HashMap<String, DSSignificanceResultSet<DSGeneMarker>>();
	
	
	@Publish
	public org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> event) {
		return event;
	}	
	 
	@Override
	public String getAnalysisName() {
		return analysisName;
	}

	@Override
	protected Map<Serializable, Serializable> getBisonParameters() {
		Map<Serializable, Serializable> parameterMap = new HashMap<Serializable, Serializable>();
		if (mraAnalysisPanel.getResultid() != null){
			parameterMap.put("resultid", mraAnalysisPanel.getResultid());
			return parameterMap;
		}
		byte[] network = mraAnalysisPanel.getNetwork();
		if (network == null){
			parameterMap.put("network", null);
			return parameterMap;
		}
		parameterMap.put("mintg", mraAnalysisPanel.getMintg());
		parameterMap.put("minsp", mraAnalysisPanel.getMinsp());
		parameterMap.put("nperm", mraAnalysisPanel.getNperm());
		parameterMap.put("pvgsea", mraAnalysisPanel.getPValue());
		parameterMap.put("tail", mraAnalysisPanel.getTail());
		parameterMap.put("pvshadow", mraAnalysisPanel.getPVshadow());
		parameterMap.put("pvsynergy", mraAnalysisPanel.getPVsynergy());
		parameterMap.put("networkname", mraAnalysisPanel.getNetworkFilename());
		parameterMap.put("network", network);network=null;
		if (mraAnalysisPanel.allpos && mraAnalysisPanel.getTail()==2){
			JOptionPane.showMessageDialog(null, "Since all Spearman's correlation >= 0, gsea will use tail = 1.");
			parameterMap.put("tail", 1);
		}
		parameterMap.put("class1", mraAnalysisPanel.getIxClass(CSAnnotationContext.CLASS_CONTROL).toArray(new String[0]));
		parameterMap.put("class2", mraAnalysisPanel.getIxClass(CSAnnotationContext.CLASS_CASE).toArray(new String[0]));
		return parameterMap;
	}

	@Override
	public Class<?> getBisonReturnType() {
		return String.class;
	}

	@Override
	protected boolean useMicroarraySetView() {
		return true;
	}

	@Override
	protected boolean useOtherDataSet() {
		return false;
	}

	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet<?> refMASet) {
		if (maSetView == null)
			return new ParamValidationResults(false, "Invalid input.");
		assert maSetView instanceof DSMicroarraySetView;

		String runid = mraAnalysisPanel.getResultid();
		if (runid != null){
			if (!pattern.matcher(runid).find())
				return new ParamValidationResults(false, "Invalid MRA Result ID: "+runid);
			else
				return new ParamValidationResults(true, "No Error");
		}
		String vn = mraAnalysisPanel.validateNetwork();
		if (!vn.equals("Valid"))
			return new ParamValidationResults(false, "Invalid network: "+vn);
		
		if (mraAnalysisPanel.getMintg() <= 0)
			return new ParamValidationResults(false, "Min targets should be a positive integer.");
		if (mraAnalysisPanel.getMinsp() <= 0)
			return new ParamValidationResults(false, "Min samples should be a positive integer.");
		if (mraAnalysisPanel.getNperm() <= 0)
			return new ParamValidationResults(false, "Nperm should be a positive integer.");
		double pvgsea = mraAnalysisPanel.getPValue();
		if (pvgsea < 0 || pvgsea > 1)
			return new ParamValidationResults(false, "GSEA Pvalue should be between 0 and 1.");
		int tail = mraAnalysisPanel.getTail();
		if (tail != 1 && tail != 2)
			return new ParamValidationResults(false, "Tail should be 1 or 2.");
		double pvshadow = mraAnalysisPanel.getPVshadow();
		if (pvshadow < 0 || pvshadow > 1)
			return new ParamValidationResults(false, "Shadow Pvalue should be between 0 and 1.");
		double pvsynergy = mraAnalysisPanel.getPVsynergy();
		if (pvsynergy < 0 || pvsynergy > 1)
			return new ParamValidationResults(false, "Synergy Pvalue should be between 0 and 1.");
		HashSet<String> ctrls = mraAnalysisPanel.getIxClass(CSAnnotationContext.CLASS_CONTROL);
		Iterator<String> casei = mraAnalysisPanel.getIxClass(CSAnnotationContext.CLASS_CASE).iterator();
		if (ctrls.size() == 0 && !casei.hasNext()){
			return new ParamValidationResults(false,
					"Please activate at least one set of arrays for \"case\", and one set of arrays for \"control\".");
		}
		if (ctrls.size() == 0)
			return new ParamValidationResults(false, "Please activate at least one set of arrays for \"control\".");
		if (!casei.hasNext())
			return new ParamValidationResults(false, "Please activate at least one set of arrays for \"case\".");
		while (casei.hasNext()){
		    if (ctrls.contains(casei.next()))
			return new ParamValidationResults(false, "An array cannot be in case and control at the same time.");
		}
		
		return new ParamValidationResults(true, "No Error");
	}
}
