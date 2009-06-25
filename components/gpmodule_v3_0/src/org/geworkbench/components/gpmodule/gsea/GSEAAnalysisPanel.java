package org.geworkbench.components.gpmodule.gsea;

import org.geworkbench.components.gpmodule.GPAnalysisPanel;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.naming.OperationNotSupportedException;
import java.io.Serializable;
import java.util.Map;
import java.text.NumberFormat;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * User: nazaire
 */
public class GSEAAnalysisPanel extends GPAnalysisPanel
{
    private Log log = LogFactory.getLog(this.getClass());

    private JComboBox gsDatabase;
    private JFormattedTextField numPerm;
    private JComboBox permType;
    private JComboBox collapseProbes;
    private JComboBox rankMetric;
    private JComboBox scoringScheme;
    private JFormattedTextField minSize;
    private JFormattedTextField maxSize;
    private JComboBox geneListOrder;
    private JComboBox normMode;
    private JComboBox randomMode;
    
    public GSEAAnalysisPanel()
    {
        super(new ParameterPanel(), "GSEA");

        try
        {
            init();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public JPanel getRequiredParametersPanel()
    {
        gsDatabase = new JComboBox();
        gsDatabase.addItem("GENE_SYMBOL.chip");
        gsDatabase.addItem("Seq_Accession.chip");

        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(0);
        numPerm = new JFormattedTextField(format);
        numPerm.setValue(100);

        permType = new JComboBox();
        permType.addItem("phenotype");
        permType.addItem("gene set");

        collapseProbes = new JComboBox();
        collapseProbes.addItem("yes");
        collapseProbes.addItem("no");

        FormLayout layout = new FormLayout(
                    "right:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(95dlu;pref),7dlu, max(70dlu;pref)",
                    "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Gene Set Enrichment Analysis (GSEA) Required Parameters");
        builder.nextRow();

        builder.append("gene sets database" , gsDatabase);

        // add the GenePattern logo
        builder.setColumn(7);
        builder.add(getGPLogo());
        builder.nextRow();
        builder.append("number of permutations", numPerm);
        builder.nextLine();
        builder.append("permutation type", permType);
        builder.nextLine();
        builder.append("collapse probe sets to gene symbols", collapseProbes);
        builder.nextLine();

        return builder.getPanel();
    }

    public JPanel getBasicParametersPanel()
    {
        scoringScheme = new JComboBox();
        scoringScheme.addItem("classic");
        scoringScheme.addItem("weighted");
        scoringScheme.addItem("weighted_p2");
        scoringScheme.addItem("weighted_p1.5");        

        rankMetric = new JComboBox();
        rankMetric.addItem("Cosine");
        rankMetric.addItem("Euclidean");
        rankMetric.addItem("Manhattan");
        rankMetric.addItem("Pearson");               

        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(0);
        minSize = new JFormattedTextField(format);
        minSize.setValue(15);

        maxSize = new JFormattedTextField(format);
        maxSize.setValue(500);

        geneListOrder = new JComboBox();
        geneListOrder.addItem("descending");
        geneListOrder.addItem("ascending");

        FormLayout layout = new FormLayout(
                    "right:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(95dlu;pref),7dlu, max(70dlu;pref)",
                    "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Gene Set Enrichment Analysis (GSEA) Basic Parameters");
        builder.nextRow();

        builder.append("scoring scheme" , scoringScheme);
        builder.nextRow();
        builder.append("metric for ranking genes" , rankMetric);

        // add the GenePattern logo
        builder.setColumn(7);
        builder.add(getGPLogo());
        builder.nextRow();
        builder.append("min gene set size", minSize);
        builder.nextLine();
        builder.append("max gene set size", maxSize);
        builder.nextLine();
        builder.append("gene list ordering mode", geneListOrder);
        builder.nextLine();

        return builder.getPanel();
    }

    public JPanel getAdvancedParametersPanel()
    {
        normMode = new JComboBox();
        normMode.addItem("none");
        normMode.addItem("meandiv");

        randomMode = new JComboBox();
        randomMode.addItem("no_balance");
        randomMode.addItem("equalize_and_balance");

        FormLayout layout = new FormLayout(
                    "right:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(95dlu;pref),7dlu, max(70dlu;pref)",
                    "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Gene Set Enrichment Analysis (GSEA) Advanced Parameters");
        builder.nextRow();

        builder.append("normalization mode" , normMode);
        builder.nextRow();

        // add the GenePattern logo
        builder.setColumn(7);
        builder.add(getGPLogo());
        builder.nextRow();
        builder.append("randomization mode" , randomMode);
        builder.nextLine();

        return builder.getPanel();
    }
    public void initParameterPanel()
    {
        JTabbedPane multiPane = new JTabbedPane();

        multiPane.addTab("Required parameters", getRequiredParametersPanel());
        multiPane.addTab("Basic parameters", getBasicParametersPanel());
        multiPane.addTab("Advanced parameters", getAdvancedParametersPanel());        


        parameterPanel.add(multiPane);
    }

    public String getGsDatabase()
    {
        return (String) gsDatabase.getSelectedItem();
    }

    public void setGsDatabase(String s)
    {
    	gsDatabase.setSelectedItem(s);
    }

    public Long getNumPermutations()
    {
        return (Long) numPerm.getValue();
    }

    public void setNumPermutations(Long nperm)
    {
    	numPerm.setValue(nperm);
    }

    protected String getParamDescriptionFile()
    {
        return null; //GSEAAnalysisPanel.class.getResource("paramDesc.html").getPath();
    }

    protected String getDescriptionFile()
    {
        return null; //GSEAAnalysisPanel.class.getResource("help.html").getPath();
    }
    /*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    public void setParameters(Map<Serializable, Serializable> parameters)
    {
        /*Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if (key.equals("gene sets database"))
            {
				setGsDatabase((String)value);
			}
            if (key.equals("number of permutations")){
				setNumPermutations((Long)value);
			}
        }*/

        log.error(new OperationNotSupportedException("Please implement setParameters()"));
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 *      Since HierClustPanel only has three parameters, we return metric,
	 *      dimension and method in the format same as getBisonParameters().
	 */
    public Map<Serializable, Serializable> getParameters()
    {
       /* Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();

        parameters.put("gene sets database", getGsDatabase());
        parameters.put("number of permutations", getGsDatabase());

        return parameters; */
		log.error(new OperationNotSupportedException("Please implement getParameters()"));
        return null;
    }

    @Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	}
}
