package org.geworkbench.components.gpmodule.consensusclustering;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.geworkbench.components.gpmodule.GPAnalysisPanel;
import org.geworkbench.events.listeners.ParameterActionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author mw2518
 * $Id$
 */
public class ConsensusClusteringPanel extends GPAnalysisPanel  
{
	private static final long serialVersionUID = -2258456667092378338L;
	
	private JTextField kmax;
	private JTextField resamplingIterations;
	private JTextField seedValue;
	private JComboBox  clusteringAlgorithm;
	private JComboBox  clusterBy;
	private JComboBox  distanceMeasure;
	private JComboBox  resample;
	private JTextField resampleValue;
	private JComboBox  mergeType;
	private JTextField descentIterations;
	private JComboBox  normalizeType;
	private JTextField normalizationIterations;
	private JComboBox  createHeatMap;
	private JTextField heatMapSize;
	private JTextField clusterListName;
	private static final String clusterListNameBase = "consensus clustering kmax=";
    
    public ConsensusClusteringPanel()
     {
        super(new ParameterPanel(), "ConsensusClustering");
         
        try{
            init();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void initParameterPanel()
    {
    	ToolTipManager.sharedInstance().setDismissDelay(1000 * 120);
    	
    	kmax = new JTextField();
    	kmax.setText("5");
    	kmax.getDocument().addDocumentListener(new DocumentListener(){
			public void changedUpdate(DocumentEvent e) {
				clusterListName.setText(clusterListNameBase+kmax.getText());
			}
			public void insertUpdate(DocumentEvent e) {
				clusterListName.setText(clusterListNameBase+kmax.getText());
			}
			public void removeUpdate(DocumentEvent e) {
				clusterListName.setText(clusterListNameBase+kmax.getText());
			}	
    	});
    	kmax.setToolTipText("Try K=2,3,...,kmax clusters (must be > 1)");
    	
    	resamplingIterations = new JTextField();
    	resamplingIterations.setText("20");
    	resamplingIterations.setToolTipText("Number of resampling iterations");
    	
    	seedValue = new JTextField();
    	seedValue.setText("12345");
    	seedValue.setToolTipText("Random number generator seed");
    	
        clusteringAlgorithm = new JComboBox();
        clusteringAlgorithm.addItem("Hierarchical");
        clusteringAlgorithm.addItem("SOM");
        clusteringAlgorithm.addItem("NMF");
        clusteringAlgorithm.addItem("KMeans");
        clusteringAlgorithm.setToolTipText("Type of clustering algorithm");
        
        clusterBy = new JComboBox();
        clusterBy.addItem("columns");
        clusterBy.addItem("rows");
        clusterBy.setToolTipText("Whether to cluster by rows/genes or columns/experiments");
        
        distanceMeasure = new JComboBox();
        distanceMeasure.addItem("Euclidean");
        distanceMeasure.addItem("Pearson");
        distanceMeasure.setToolTipText("Distance measure");
        
        resample = new JComboBox();
        resample.addItem("subsample");
        resample.addItem("features");
        resample.addItem("nosampling");
        resample.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				resampleValue.setEnabled(true);
				if(resample.getSelectedItem().equals("nosampling")){
					resampleValue.setText("");
					resampleValue.setEnabled(false);
				}
			}        	
        });
        resample.setToolTipText("resampling scheme(one of 'subsample[ratio]', 'features[nfeat]', 'nosampling')");
        
        resampleValue = new JTextField();
        resampleValue.setText("");
        resampleValue.setToolTipText("numeric entry for 'subsample[ratio]' or 'features[nfeat]'");
        
        mergeType = new JComboBox();
        mergeType.addItem("average");
        mergeType.addItem("complete");
        mergeType.addItem("single");
        mergeType.setToolTipText("Ignored when algorithm other than hierarchical selected");
        
        descentIterations = new JTextField();
        descentIterations.setText("2000");
        descentIterations.setToolTipText("Number of SOM/NMF iterations");
        
        normalizeType = new JComboBox();
        normalizeType.addItem("row-wise");
        normalizeType.addItem("column-wise");
        normalizeType.addItem("both");
        normalizeType.addItem("none");
        normalizeType.setToolTipText("row-wise, column-wise, both");
        
        normalizationIterations = new JTextField();
        normalizationIterations.setText("0");
        normalizationIterations.setToolTipText("number of row/column normalization iterations (supercedes normalize.type)");
        
        createHeatMap = new JComboBox();
        createHeatMap.addItem("no");
        createHeatMap.addItem("yes");
        createHeatMap.setToolTipText("Whether to create heatmaps (one for each cluster number)");
        
        heatMapSize = new JTextField();
        heatMapSize.setText("2");
        heatMapSize.setToolTipText("point size of a consensus matrix's heat map (between 1 and 20)");
        
        clusterListName = new JTextField();
        clusterListName.setText(clusterListNameBase+kmax.getText());
        clusterListName.setToolTipText("name for cluster results list");

        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        
        kmax.addActionListener(parameterActionListener);
    	resamplingIterations.addActionListener(parameterActionListener);
    	seedValue.addActionListener(parameterActionListener);
    	clusteringAlgorithm.addActionListener(parameterActionListener);
    	clusterBy.addActionListener(parameterActionListener);
    	distanceMeasure.addActionListener(parameterActionListener);
    	resample.addActionListener(parameterActionListener);
    	resampleValue.addActionListener(parameterActionListener);
    	mergeType.addActionListener(parameterActionListener);
    	descentIterations.addActionListener(parameterActionListener);
    	normalizeType.addActionListener(parameterActionListener);
    	normalizationIterations.addActionListener(parameterActionListener);
    	createHeatMap.addActionListener(parameterActionListener);
    	heatMapSize.addActionListener(parameterActionListener);
    	clusterListName.addActionListener(parameterActionListener);

        FormLayout layout = new FormLayout(
                    "right:max(80dlu;pref), 7dlu,  max(100dlu;pref), 7dlu, max(25dlu;pref), 7dlu, max(70dlu;pref)",
                    "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Consensus Clustering Analysis Parameters");
        builder.nextLine();

        builder.append("kmax", kmax);
        
        // add the GenePattern logo
        builder.setColumn(7);
        builder.add(getGPLogo());
        builder.nextRow();

        builder.append("resampling iterations", resamplingIterations);
        builder.nextLine();
        builder.append("seed value", seedValue);
        builder.nextLine();
        builder.append("clustering algorithm", clusteringAlgorithm);
        builder.nextLine();
        builder.append("cluster by", clusterBy);
        builder.nextLine();
        builder.append("distance measure", distanceMeasure);
        builder.nextLine();
        builder.append("resample", resample);
        builder.nextLine();
        builder.append("resample value", resampleValue);
        builder.nextLine();
        builder.append("merge type", mergeType);
        builder.nextLine();
        builder.append("descent iterations", descentIterations);
        builder.nextLine();
        builder.append("normalize type", normalizeType);
        builder.nextLine();
        builder.append("normalization iterations", normalizationIterations);
        builder.nextLine();
        builder.append("create heat map", createHeatMap);
        builder.nextLine();
        builder.append("heat map size", heatMapSize);
        builder.nextLine();
        builder.append("cluster list name", clusterListName);

        parameterPanel.add(builder.getPanel(), BorderLayout.WEST);
    }

    protected URL getParamDescriptionFile()
    {
        return ConsensusClusteringPanel.class.getResource("paramDesc.html");
    }

    protected URL getDescriptionFile()
    {
        return ConsensusClusteringPanel.class.getResource("help.html");
    }
    
    public String getClusterBy(){
    	return (String)clusterBy.getSelectedItem();
    }
    
    public String getClusterListName(){
    	return clusterListName.getText();
    }
    
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    public void setParameters(Map<Serializable, Serializable> parameters){
    	if (getStopNotifyAnalysisPanelTemporaryFlag() == true)
			return;
		stopNotifyAnalysisPanelTemporary(true);
        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			String value = (String)parameter.getValue();

			if      (key.equals("kmax"))					kmax.setText(value);
			else if (key.equals("resampling.iterations"))	resamplingIterations.setText(value);
			else if (key.equals("seed.value"))				seedValue.setText(value);
			else if (key.equals("clustering.algorithm"))	clusteringAlgorithm.setSelectedItem(value);
			else if (key.equals("cluster.by"))				clusterBy.setSelectedItem(value);
			else if (key.equals("distance.measure"))		distanceMeasure.setSelectedItem(value);
			else if (key.equals("resample"))				resample.setSelectedItem(value);
			else if (key.equals("resample.value"))			resampleValue.setText(value);
			else if (key.equals("merge.type"))				mergeType.setSelectedItem(value);
			else if (key.equals("descent.iterations"))		descentIterations.setText(value);
			else if (key.equals("normalize.type"))			normalizeType.setSelectedItem(value);
			else if (key.equals("normalization.iterations"))normalizationIterations.setText(value);
			else if (key.equals("create.heat.map"))			createHeatMap.setSelectedItem(value);
			else if (key.equals("heat.map.size"))			heatMapSize.setText(value);
			else if (key.equals("cluster.list.name"))		clusterListName.setText(value);
		}
        stopNotifyAnalysisPanelTemporary(false);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 *      Since HierClustPanel only has three parameters, we return metric,
	 *      dimension and method in the format same as getBisonParameters().
	 */
    public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new LinkedHashMap<Serializable, Serializable>();

		parameters.put("kmax",						kmax.getText());
		parameters.put("resampling.iterations",		resamplingIterations.getText());
		parameters.put("seed.value",				seedValue.getText());
		parameters.put("clustering.algorithm",		(String)clusteringAlgorithm.getSelectedItem());
		parameters.put("cluster.by",				(String)clusterBy.getSelectedItem());
		parameters.put("distance.measure",			(String)distanceMeasure.getSelectedItem());
		parameters.put("resample",					(String)resample.getSelectedItem());
		parameters.put("resample.value",			resampleValue.getText());
		parameters.put("merge.type",				(String)mergeType.getSelectedItem());
		parameters.put("descent.iterations",		descentIterations.getText());
		parameters.put("normalize.type",			(String)normalizeType.getSelectedItem());
		parameters.put("normalization.iterations",	normalizationIterations.getText());
		parameters.put("create.heat.map",			(String)createHeatMap.getSelectedItem());
		parameters.put("heat.map.size",				heatMapSize.getText());
		parameters.put("cluster.list.name",			clusterListName.getText());

		return parameters;
	}
    
	@Override
	public String getDataSetHistory() {
		/* translate between machine index to human readable text. */
		String histStr = "";
		Map<Serializable, Serializable> pMap = getParameters();
		histStr += "Consensus Clustering Analysis parameters:\n";
		histStr += "----------------------------------------\n";
		for (Iterator<Map.Entry<Serializable, Serializable>> iterator = pMap
				.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			histStr += key.toString() + " = " + value.toString() + "\n";
		}
		return histStr;
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}
	
	private boolean isInt(JTextField field, int min, int max){
		int n = 0;
		try{
			n = Integer.parseInt(field.getText());
		}catch(NumberFormatException e){
			return false;
		}
		return (n >= min && n <= max);
	}
	
	private boolean isDouble(JTextField field, double min, double max){
		double n = 0;
		try{
			n = Double.parseDouble(field.getText());
		}catch(NumberFormatException e){
			return false;
		}
		return (n > min && n < max);
	}
	
	@Override
    public ParamValidationResults validateParameters() {
		if(!isInt(kmax, 2, Integer.MAX_VALUE))
			return new ParamValidationResults(false, "kmax should be an interger > 1");
		if(!isInt(resamplingIterations, 0, Integer.MAX_VALUE))
			return new ParamValidationResults(false, "Please enter an interger for resampling iterations");
		if(!isInt(seedValue, 0, Integer.MAX_VALUE))
			return new ParamValidationResults(false, "Please enter an interger for seed value");
		String resamplestr = (String)resample.getSelectedItem();
		if(!resamplestr.equals("nosampling") && resampleValue.getText().trim().length()>0){
			if(resamplestr.equals("subsample") && !isDouble(resampleValue, 0, 1))
				return new ParamValidationResults(false, "sub-sample ratio should between 0 and 1 exclusively");
			else if(resamplestr.equals("features") && !isInt(resampleValue, 1, Integer.MAX_VALUE))
				return new ParamValidationResults(false, "Please enter a positive interger for features number");
		}
		if(!isInt(descentIterations, 0, Integer.MAX_VALUE))
			return new ParamValidationResults(false, "Please enter an interger for descent iterations");
		if(!isInt(normalizationIterations, 0, Integer.MAX_VALUE))
			return new ParamValidationResults(false, "Please enter an interger for normalization iterations");
		if(!isInt(heatMapSize, 1, 20))
			return new ParamValidationResults(false, "heat map size should be an interger between 1 and 20");

		return new ParamValidationResults(true, null);
    }

}