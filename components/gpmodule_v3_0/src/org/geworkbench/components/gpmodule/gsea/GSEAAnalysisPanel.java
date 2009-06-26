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
    private JComboBox collapseMode;
    private JComboBox omitFeatures;    
    
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

        gsDatabase.addItem("c1.all.v2.5.symbols.gmt [Positional]");
        gsDatabase.addItem("c2.all.v2.5.symbols.gmt [Curated]");
        

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
                    "left:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(75dlu;pref),2dlu, max(70dlu;pref)",
                    "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Gene Set Enrichment Analysis (GSEA) Required Parameters");
        builder.nextLine();

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
                    "left:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(75dlu;pref),2dlu, max(70dlu;pref)",
                    "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Gene Set Enrichment Analysis (GSEA) Basic Parameters");
        builder.nextLine();

        builder.append("scoring scheme" , scoringScheme);

        //add the GenePattern logo
        builder.setColumn(7);
        builder.add(getGPLogo());
        builder.nextRow();

        builder.append("metric for ranking genes" , rankMetric);
        builder.nextLine();
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
        collapseMode = new JComboBox();
        collapseMode.addItem("max probe");
        collapseMode.addItem("median of probes");

        normMode = new JComboBox();
        normMode.addItem("none");
        normMode.addItem("meandiv");

        randomMode = new JComboBox();
        randomMode.addItem("no balance");
        randomMode.addItem("equalize and balance");

        omitFeatures = new JComboBox();
        omitFeatures.addItem("yes");
        omitFeatures.addItem("no");

        FormLayout layout = new FormLayout(
                    "left:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(75dlu;pref),2dlu, max(70dlu;pref)",
                    "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Gene Set Enrichment Analysis (GSEA) Advanced Parameters");
        builder.nextLine();

        builder.append("collapse mode" , collapseMode);

        //add the GenePattern logo
        builder.setColumn(7);
        builder.add(getGPLogo());
        builder.nextRow();

        builder.append("normalization mode" , normMode);
        builder.nextLine();
        
        builder.append("randomization mode" , randomMode);
        builder.nextLine();

        builder.append("omit features with no symbol match" , omitFeatures);
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
		log.error(new OperationNotSupportedException("Please implement getParameters()"));
        return null;
    }

    @Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
	}
}
