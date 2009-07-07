package org.geworkbench.components.gpmodule.gsea;

import org.geworkbench.components.gpmodule.GPAnalysisPanel;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.naming.OperationNotSupportedException;
import java.io.Serializable;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.text.NumberFormat;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.*;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * User: nazaire
 */
public class GSEAAnalysisPanel extends GPAnalysisPanel
{
    private Log log = LogFactory.getLog(this.getClass());

    JRadioButton selectGS;
    JRadioButton upLoadGS;
    private JComboBox gsDatabase;
    private JComboBox chipPlatform;    
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
    private JFileChooser gsDatabaseFile;
    private JTextField gsDatabaseFileField;
    private JButton loadGSDatabaseButton;
    
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
        gsDatabase.addItem("c2.biocarta.v2.5.symbols.gmt [Curated]");
        gsDatabase.addItem("c2.cgp.v2.5.symbols.gmt [Curated]");
        gsDatabase.addItem("c2.cp.v2.5.symbols.gmt [Curated]");
        gsDatabase.addItem("c2.genmapp.v2.5.symbols.gmt [Curated]");
        gsDatabase.addItem("c2.kegg.v2.5.symbols.gmt [Curated]");
        gsDatabase.addItem("c3.all.v2.5.symbols.gmt [Motif]");
        gsDatabase.addItem("c3.mir.v2.5.symbols.gmt [Motif]");
        gsDatabase.addItem("c3.tft.v2.5.symbols.gmt [Motif];");
        gsDatabase.addItem("c4.all.v2.5.symbols.gmt [Computational]");
        gsDatabase.addItem("c4.cgn.v2.5.symbols.gmt [Computational]");
        gsDatabase.addItem("c4.cm.v2.5.symbols.gmt [Computational];");
        gsDatabase.addItem("c5.all.v2.5.symbols.gmt [Gene ontology]");
        gsDatabase.addItem("c5.all.v2.5.symbols.gmt [Gene ontology]");
        gsDatabase.addItem("c5.cc.v2.5.symbols.gmt [Gene ontology]");
        gsDatabase.addItem("c5.mf.v2.5.symbols.gmt [Gene ontology]");
        gsDatabase.addItem("c1.v2.symbols.gmt [Positional];");
        gsDatabase.addItem("c2.v2.symbols.gmt [Curated]");
        gsDatabase.addItem("c3.v2.symbols.gmt [Motif]");
        gsDatabase.addItem("c4.v2.symbols.gmt [Computational]");
        gsDatabase.addItem("c4.v1.symbols.gmt [Computational]");
        gsDatabase.addItem("c3.v1.symbols.gmt [Motif]");
        gsDatabase.addItem("c2.v1.symbols.gmt [Curated]");
        gsDatabase.addItem("c1.v1.symbols.gmt [Positional]");
        

        gsDatabase.setMinimumSize(new Dimension(270, 22));
        gsDatabase.setMaximumSize(new Dimension(270, 22));
        gsDatabase.setPreferredSize(new Dimension(270, 22));

        gsDatabaseFile = new JFileChooser();
        gsDatabaseFileField = new JTextField();
        gsDatabaseFileField.setEnabled(false);        
        gsDatabaseFileField.setMinimumSize(new Dimension(110, 22));
        gsDatabaseFileField.setMaximumSize(new Dimension(110, 22));
        gsDatabaseFileField.setPreferredSize(new Dimension(110, 22));

        selectGS = new JRadioButton("Select Gene Set Database");
        selectGS.setSelected(true);
        selectGS.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent event)
            {
                if(event.getStateChange() == ItemEvent.SELECTED)
                {
                    gsDatabase.setEnabled(true);
                }
                if(event.getStateChange() == ItemEvent.DESELECTED)
                {
                    gsDatabase.setEnabled(false);
                }
            }
        });


        upLoadGS = new JRadioButton("Upload Gene Set Database");
        upLoadGS.setSelected(false);
        upLoadGS.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent event)
            {
                if(event.getStateChange() == ItemEvent.SELECTED)
                {
                    gsDatabaseFileField.setEnabled(true);
                    loadGSDatabaseButton.setEnabled(true);                    
                }
                if(event.getStateChange() == ItemEvent.DESELECTED)
                {
                    gsDatabaseFileField.setEnabled(false);
                    loadGSDatabaseButton.setEnabled(false);
                }
            }
        });

        ButtonGroup gsGroup = new ButtonGroup();
        gsGroup.add(selectGS);
        gsGroup.add(upLoadGS);

        loadGSDatabaseButton = new JButton("Load");
        loadGSDatabaseButton.setEnabled(false);
        loadGSDatabaseButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                int returnValue = gsDatabaseFile.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION)
                {
                    File file = gsDatabaseFile.getSelectedFile();
                    gsDatabaseFileField.setText(file.getAbsolutePath());
                }
            }
        });
        collapseProbes = new JComboBox();
        collapseProbes.addItem("yes");
        collapseProbes.addItem("no");

        chipPlatform = new JComboBox();
        chipPlatform.addItem("GENE_SYMBOL.chip");
        chipPlatform.addItem("Seq_Accession.chip");
        chipPlatform.addItem("SEQ_ACCESSION.chip");
        chipPlatform.addItem("Hu35KsubA.chip");
        chipPlatform.addItem("Hu35KsubB.chip");
        chipPlatform.addItem("Hu35KsubC.chip");
        chipPlatform.addItem("Hu35KsubD.chip");
        chipPlatform.addItem("Hu6800.chip");
        
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(0);
        numPerm = new JFormattedTextField(format);
        numPerm.setValue(100);

        permType = new JComboBox();
        permType.addItem("phenotype");
        permType.addItem("gene set");
       
        FormLayout layout = new FormLayout(
                    "left:max(80dlu;pref), 7dlu,  max(70dlu;pref), 7dlu, max(55dlu;pref),20dlu, max(70dlu;pref)",
                    "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Gene Set Enrichment Analysis (GSEA) Required Parameters");
        builder.nextLine();

        builder.append(selectGS);
        builder.append(gsDatabase);

        // add the GenePattern logo
        builder.setColumn(7);
        builder.add(getGPLogo());
        builder.nextRow();
        builder.append(upLoadGS);
        builder.append(gsDatabaseFileField, loadGSDatabaseButton);
        builder.nextLine();
        builder.append("collapse probe sets to gene symbols", collapseProbes);        
        builder.nextLine();
        builder.append("chip platform", chipPlatform);
        builder.nextLine();
        builder.append("permutation type", permType);
        builder.nextLine();
        builder.append("number of permutations", numPerm);
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

    public String getChipPlatform()
    {
        return (String) chipPlatform.getSelectedItem();
    }

    public void setChipPlatform(String s)
    {
    	chipPlatform.setSelectedItem(s);
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
		//log.error(new OperationNotSupportedException("Please implement getParameters()"));

        Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();

		parameters.put("numFeatureMethod", "yes");

        return null;
    }

    @Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
	}
}
