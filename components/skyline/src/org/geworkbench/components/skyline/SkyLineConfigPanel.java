package org.geworkbench.components.skyline;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.events.listeners.ParameterActionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Parameters panel used by the SkyLine
 * 
 * @author mw2518
 * @version $Id: SkyLineConfigPanel.java,v 1.8 2009-06-19 19:16:51 jiz Exp $
 * 
 */
public class SkyLineConfigPanel extends AbstractSaveableParameterPanel
		implements Serializable {
	private Log log = LogFactory.getLog(this.getClass());
	final static String YES = "YES";
	final static String NO = "NO";
	final String MIN_OPTION = "Minimum";
	final String MAX_OPTION = "Maximum";
	final String IGNORE_OPTION = "Ignore";
	final String REPLACE_OPTION = "Replace";
	private JComboBox cutoffTypeSelection = new JComboBox(new String[] {
			MIN_OPTION, MAX_OPTION });
	private JComboBox missingValuesSelection = new JComboBox(new String[] {
			IGNORE_OPTION, REPLACE_OPTION });

	private JFormattedTextField chainEdit = new JFormattedTextField();
	private JFormattedTextField dEdit = new JFormattedTextField();
	private JFormattedTextField jEdit = new JFormattedTextField();
	private JFormattedTextField bEdit = new JFormattedTextField();
	private NumberFormat formatdouble = NumberFormat.getInstance();
	private JFormattedTextField hEdit;
	private JFormattedTextField eEdit;
	private JFormattedTextField redundancy_levelEdit = new JFormattedTextField();
	private JFormattedTextField model_numberEdit = new JFormattedTextField();

	private JComboBox run_pb1ValueSelection = new JComboBox(new String[] { YES,
			NO });
	private JComboBox run_pb2ValueSelection = new JComboBox(new String[] { YES,
			NO });
	private JComboBox fValueSelection = new JComboBox(new String[] { YES, NO });
	// optional ',' separated species names, default is blank for 'no' species
	// limitation
	// invalid species names are same as 'no'
	private JFormattedTextField chosen_speciesEdit = new JFormattedTextField();
	private JComboBox run_modellerValueSelection = new JComboBox(new String[] {
			YES, NO });
	private JComboBox run_nestValueSelection = new JComboBox(new String[] {
			YES, NO });
	private JComboBox hetatmValueSelection = new JComboBox(new String[] { YES,
			NO });
	private JComboBox clustalValueSelection = new JComboBox(new String[] { YES,
			NO });

	private String chain = "A", d = "nr", chosen_species = "";
	private int run_pb1 = 0, run_pb2 = 1, f = 0;
	private int run_modeller = 0, run_nest = 1, hetatm = 1, clustal = 0;
	private Integer j = 5, b = 1000, redundancy_level = 100, model_number = 1;
	private Double h = 0.0005, e = 10.0;

	private static class SerializedInstance implements Serializable {
		private static final long serialVersionUID = 1L;
		private String chain = "A", d = "nr", chosen_species = "";
		private int run_pb1 = 0, run_pb2 = 1, f = 0;
		private int run_modeller = 0, run_nest = 1, hetatm = 1, clustal = 0;
		private Integer j = 5, b = 1000, redundancy_level = 100,
				model_number = 1;
		private Double h = 0.0005, e = 10.0;

		public SerializedInstance(String chain, String d, int run_pb1,
				int run_pb2, int f, String chosen_species, int run_modeller,
				int run_nest, int hetatm, int clustal, int j, int b, Double h,
				Double e, int redundancy_level, int model_number) {
			this.chain = chain;
			this.d = d;
			this.run_pb1 = run_pb1;
			this.run_pb2 = run_pb2;
			this.f = f;
			this.chosen_species = chosen_species;
			this.run_modeller = run_modeller;
			this.run_nest = run_nest;
			this.hetatm = hetatm;
			this.clustal = clustal;
			this.j = j;
			this.b = b;
			this.h = h;
			this.e = e;
			this.redundancy_level = redundancy_level;
			this.model_number = model_number;
		}

		Object readResolve() throws ObjectStreamException {

			SkyLineConfigPanel panel = new SkyLineConfigPanel();
			panel.chainEdit.setValue(chain);
			panel.dEdit.setValue(d);
			panel.run_pb1ValueSelection.setSelectedIndex(run_pb1);
			panel.run_pb2ValueSelection.setSelectedIndex(run_pb2);
			panel.fValueSelection.setSelectedIndex(f);
			panel.chosen_speciesEdit.setValue(chosen_species);
			panel.run_modellerValueSelection.setSelectedIndex(run_modeller);
			panel.run_nestValueSelection.setSelectedIndex(run_nest);
			panel.hetatmValueSelection.setSelectedIndex(hetatm);
			panel.clustalValueSelection.setSelectedIndex(clustal);
			panel.jEdit.setValue(j);
			panel.bEdit.setValue(b);
			panel.hEdit.setValue(h);
			panel.eEdit.setValue(e);
			panel.redundancy_levelEdit.setValue(redundancy_level);
			panel.model_numberEdit.setValue(model_number);
			return panel;
		}
	}

	public Object writeReplace() throws ObjectStreamException {
		return new SerializedInstance((String) chainEdit.getValue(),
				(String) dEdit.getValue(), run_pb1ValueSelection
						.getSelectedIndex(), run_pb2ValueSelection
						.getSelectedIndex(),
				fValueSelection.getSelectedIndex(), (String) chosen_speciesEdit
						.getValue(), run_modellerValueSelection
						.getSelectedIndex(), run_nestValueSelection
						.getSelectedIndex(), hetatmValueSelection
						.getSelectedIndex(), clustalValueSelection
						.getSelectedIndex(), (Integer) jEdit.getValue(),
				(Integer) bEdit.getValue(), (Double) hEdit.getValue(),
				(Double) eEdit.getValue(), (Integer) redundancy_levelEdit
						.getValue(), (Integer) model_numberEdit.getValue());

	}

	public SkyLineConfigPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* every editing component needs to notify parameter panel about its change */
		ParameterActionListener parameterActionListener = new ParameterActionListener(
				this);
		chainEdit.addPropertyChangeListener(parameterActionListener);
		run_pb1ValueSelection.addActionListener(parameterActionListener);
		run_pb2ValueSelection.addActionListener(parameterActionListener);
		dEdit.addPropertyChangeListener(parameterActionListener);
		chosen_speciesEdit.addPropertyChangeListener(parameterActionListener);
		jEdit.addPropertyChangeListener(parameterActionListener);
		bEdit.addPropertyChangeListener(parameterActionListener);
		hEdit.addPropertyChangeListener(parameterActionListener);
		eEdit.addPropertyChangeListener(parameterActionListener);
		fValueSelection.addActionListener(parameterActionListener);
		redundancy_levelEdit.addPropertyChangeListener(parameterActionListener);
		run_modellerValueSelection.addActionListener(parameterActionListener);
		model_numberEdit.addPropertyChangeListener(parameterActionListener);
		hetatmValueSelection.addActionListener(parameterActionListener);
		clustalValueSelection.addActionListener(parameterActionListener);

		log.info("SkyLineConfigPanel is created.");
	}

	/*
	 * form for skyline parameter list
	 */
	private void jbInit() throws Exception {
		formatdouble.setMaximumFractionDigits(4);
		hEdit = new JFormattedTextField(formatdouble);
		eEdit = new JFormattedTextField(formatdouble);
		FormLayout layout = new FormLayout(
				"right:max(40dlu;pref), 8dlu, max(60dlu;pref)", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		builder.appendSeparator("SkyLine Parameters");

		builder.append("Protein Chain Identifier", chainEdit);
		builder.append("Run PSI-BLAST on Input Structure",
				run_pb1ValueSelection);
		builder.append("Run PSI-BLAST on Homologous Structures",
				run_pb2ValueSelection);
		builder.append("Sequence Database to Search by PSI-BLAST", dEdit);
		builder.append(
				"Optional Species Selection (latin names, comma-separated)",
				chosen_speciesEdit);
		builder.append("PSI-BLAST Rounds to Run", jEdit);
		builder.append("Maximum Number of PSI-BLAST Hits to Report", bEdit);
		builder.append("PSI-BLAST PSSM Inclusion Threshold", hEdit);
		builder.append("PSI-BLAST Expectation Value", eEdit);
		builder.append("Low-Complexity-Region Filter", fValueSelection);
		builder.append("% Sequence Identity Threshold for Clustering",
				redundancy_levelEdit);
		builder
				.append("Build Models With MODELLER",
						run_modellerValueSelection);
		// builder.append("Run NEST", run_nestValueSelection);
		builder.append("Number of Models Per Alignment by MODELLER",
				model_numberEdit);
		builder.append("Include Heteroatoms From Input Structure",
				hetatmValueSelection);
		builder.append("Build Family Alignment With ClustalW",
				clustalValueSelection);
		this.add(builder.getPanel(), BorderLayout.CENTER);

		setDefaultParameters();
	}

	/*
	 * default parameters for skyline
	 */
	public void setDefaultParameters() {
		chainEdit.setFont(new Font("SansSerif", Font.BOLD, 14));
		chainEdit.setValue(chain);
		chainEdit.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		dEdit.setFont(new Font("SansSerif", Font.BOLD, 14));
		dEdit.setValue(d);
		dEdit.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		run_pb1ValueSelection.setSelectedIndex(run_pb1);
		run_pb2ValueSelection.setSelectedIndex(run_pb2);
		fValueSelection.setSelectedIndex(f);
		chosen_speciesEdit.setValue(chosen_species);
		run_modellerValueSelection.setSelectedIndex(run_modeller);
		run_nestValueSelection.setSelectedIndex(run_nest);
		hetatmValueSelection.setSelectedIndex(hetatm);
		clustalValueSelection.setSelectedIndex(clustal);
		jEdit.setFont(new Font("SansSerif", Font.BOLD, 14));
		jEdit.setValue(j);
		jEdit.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		bEdit.setFont(new Font("SansSerif", Font.BOLD, 14));
		bEdit.setValue(b);
		bEdit.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		hEdit.setFont(new Font("SansSerif", Font.BOLD, 14));
		hEdit.setValue(h);
		hEdit.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		eEdit.setFont(new Font("SansSerif", Font.BOLD, 14));
		eEdit.setValue(e);
		eEdit.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		redundancy_levelEdit.setFont(new Font("SansSerif", Font.BOLD, 14));
		redundancy_levelEdit.setValue(redundancy_level);
		redundancy_levelEdit
				.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		model_numberEdit.setFont(new Font("SansSerif", Font.BOLD, 14));
		model_numberEdit.setValue(model_number);
		model_numberEdit
				.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
	}

	public String getchainValue() {
		return (String) chainEdit.getValue();
	}

	public String getdValue() {
		return (String) dEdit.getValue();
	}

	public String getrun_pb1Value() {
		if (run_pb1ValueSelection.getSelectedItem().equals(YES))
			return SkyLineConfigPanel.YES;
		else
			return SkyLineConfigPanel.NO;
	}

	public String getrun_pb2Value() {
		if (run_pb2ValueSelection.getSelectedItem().equals(YES))
			return SkyLineConfigPanel.YES;
		else
			return SkyLineConfigPanel.NO;
	}

	public String getfValue() {
		if (fValueSelection.getSelectedItem().equals(YES))
			return SkyLineConfigPanel.YES;
		else
			return SkyLineConfigPanel.NO;
	}

	public String getchosen_speciesValue() {
		return (String) chosen_speciesEdit.getValue();
	}

	public String getrun_modellerValue() {
		if (run_modellerValueSelection.getSelectedItem().equals(YES))
			return SkyLineConfigPanel.YES;
		else
			return SkyLineConfigPanel.NO;
	}

	public String getrun_nestValue() {
		if (run_nestValueSelection.getSelectedItem().equals(YES))
			return SkyLineConfigPanel.YES;
		else
			return SkyLineConfigPanel.NO;
	}

	public String gethetatmValue() {
		if (hetatmValueSelection.getSelectedItem().equals(YES))
			return SkyLineConfigPanel.YES;
		else
			return SkyLineConfigPanel.NO;
	}

	public String getclustalValue() {
		if (clustalValueSelection.getSelectedItem().equals(YES))
			return SkyLineConfigPanel.YES;
		else
			return SkyLineConfigPanel.NO;
	}

	public int getjValue() {
		return ((Integer) jEdit.getValue()).intValue();
	}

	public int getbValue() {
		return ((Integer) bEdit.getValue()).intValue();
	}

	public int getredundancy_levelValue() {
		return ((Integer) redundancy_levelEdit.getValue()).intValue();
	}

	public int getmodel_numberValue() {
		return ((Integer) model_numberEdit.getValue()).intValue();
	}

	public double gethValue() {
		Object obj = hEdit.getValue();
		if(obj.getClass().getSimpleName().equals("Long"))
			return ((Long)obj).doubleValue();
		else if(obj.getClass().getSimpleName().equals("Double"))
			return ((Double)obj).doubleValue();
		else {
			log.error("wrong type returned from gethValue: "+obj.getClass().getName());
			return 0;
		}
	}

	public double geteValue() {
		Object obj = eEdit.getValue();
		if(obj.getClass().getSimpleName().equals("Long"))
			return ((Long)obj).doubleValue();
		else if(obj.getClass().getSimpleName().equals("Double"))
			return ((Double)obj).doubleValue();
		else {
			log.error("wrong type returned from geteValue: "+obj.getClass().getName());
			return 0;
		}
	}

	public int getCutoffType() {
		if (cutoffTypeSelection.getSelectedItem().equals(MIN_OPTION))
			return SkyLineAnalysis.MINIMUM;
		else
			return SkyLineAnalysis.MAXIMUM;
	}

	public int getMissingValueTreatment() {
		if (missingValuesSelection.getSelectedItem().equals(IGNORE_OPTION))
			return SkyLineAnalysis.IGNORE;
		else
			return SkyLineAnalysis.REPLACE;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		revalidate();
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
	public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		parameters.put("chain", getchainValue()); // String
		parameters.put("pbOnInput", getrun_pb1Value()); // String
		parameters.put("pbOnHomolugous", getrun_pb2Value()); // String 
		parameters.put("sequenceDatabase", getdValue()) ; // String
		parameters.put("chosenSpecies", getchosen_speciesValue()); /// String
		parameters.put("roundToRun", getjValue()); /// int
		parameters.put("maximumNumber", getbValue()); /// int
		parameters.put("inclusionThreshold", gethValue()); // double
		parameters.put("expectation", geteValue()); // double
		parameters.put("filter", getfValue()); // String
		parameters.put("redundancyLevel", getredundancy_levelValue()); // int
		parameters.put("runModeller", getrun_modellerValue()); // String
		parameters.put("modelNumber", getmodel_numberValue()); // int
		parameters.put("hetatm", gethetatmValue()); // String
		parameters.put("clustal", getclustalValue()); // String

		return parameters;
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 */
	public void setParameters(Map<Serializable, Serializable> parameters) {
		if (getStopNotifyAnalysisPanelTemporaryFlag() == true)
			return;
		stopNotifyAnalysisPanelTemporary(true);
		Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
		for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set
				.iterator(); iterator.hasNext();) {
			Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if(key.equals("chain"))chainEdit.setValue(value); // String
			else if(key.equals("pbOnInput"))run_pb1ValueSelection.setSelectedItem(value); // String
			else if(key.equals("pbOnHomolugous"))run_pb2ValueSelection.setSelectedItem(value); // String 
			else if(key.equals("sequenceDatabase"))dEdit.setValue(value) ; // String
			else if(key.equals("chosenSpecies"))chosen_speciesEdit.setValue(value); /// String
			else if(key.equals("roundToRun"))jEdit.setValue(value); /// int
			else if(key.equals("maximumNumber"))bEdit.setValue(value); /// int
			else if(key.equals("inclusionThreshold"))hEdit.setValue(value); // double
			else if(key.equals("expectation"))eEdit.setValue(value); // double
			else if(key.equals("filter"))fValueSelection.setSelectedItem(value); // String
			else if(key.equals("redundancyLevel"))redundancy_levelEdit.setValue(value); // int
			else if(key.equals("runModeller"))run_modellerValueSelection.setSelectedItem(value); // String
			else if(key.equals("modelNumber"))model_numberEdit.setValue(value); // int
			else if(key.equals("hetatm"))hetatmValueSelection.setSelectedItem(value); // String
			else if(key.equals("clustal"))clustalValueSelection.setSelectedItem(value); // String
		}
		stopNotifyAnalysisPanelTemporary(false);
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		if(parameters.get("chain")==null)parameters.put("chain", chain);
		if(parameters.get("pbOnInput")==null)parameters.put("pbOnInput", YES);
		if(parameters.get("pbOnHomolugous")==null)parameters.put("pbOnHomolugous", NO);
		if(parameters.get("sequenceDatabase")==null)parameters.put("sequenceDatabase", d);
		if(parameters.get("chosenSpecies")==null)parameters.put("chosenSpecies", chosen_species);
		if(parameters.get("roundToRun")==null)parameters.put("roundToRun", j);
		if(parameters.get("maximumNumber")==null)parameters.put("maximumNumber", b);
		if(parameters.get("inclusionThreshold")==null)parameters.put("inclusionThreshold", h);
		if(parameters.get("expectation")==null)parameters.put("expectation", e);
		if(parameters.get("filter")==null)parameters.put("filter", YES);
		if(parameters.get("redundancyLevel")==null)parameters.put("redundancyLevel", redundancy_level);
		if(parameters.get("runModeller")==null)parameters.put("runModeller", YES);
		if(parameters.get("modelNumber")==null)parameters.put("modelNumber", model_number);
		if(parameters.get("hetatm")==null)parameters.put("hetatm", NO);
		if(parameters.get("clustal")==null)parameters.put("clustal", YES);
	}

}
