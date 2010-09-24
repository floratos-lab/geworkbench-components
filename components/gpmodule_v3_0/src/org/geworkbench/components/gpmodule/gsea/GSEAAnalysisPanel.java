package org.geworkbench.components.gpmodule.gsea;

import org.geworkbench.components.gpmodule.GPAnalysisPanel;
import org.geworkbench.components.gpmodule.GPConfigPanel;
import org.geworkbench.components.gpmodule.listener.ServerConnectionListener;
import org.geworkbench.components.gpmodule.event.ServerConnectionEvent;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genepattern.webservice.TaskInfo;
import org.genepattern.webservice.ParameterInfo;

import javax.swing.*;
import javax.naming.OperationNotSupportedException;
import java.io.Serializable;
import java.io.File;
import java.util.*;
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
    JRadioButton selectGS;
    JRadioButton upLoadGS;
    private JFileChooser gsDatabaseFile;
    private JTextField gsDatabaseFileField;
    private JButton loadGSDatabaseButton;
    JRadioButton selectChip;
    JRadioButton upLoadChip;
    private JFileChooser chipPlatformFile;
    private JTextField chipPlatformFileField;
    private JButton loadChipButton;

    public GSEAAnalysisPanel()
    {
        super(new ParameterPanel(), "GSEA");

        try
        {
            GPConfigPanel.addServerConnectionListener(new MyServerConnectionListener());            
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

        gsDatabaseFile = new JFileChooser();

        gsDatabaseFileField = new JTextField();
        gsDatabaseFileField.setEditable(false);
        gsDatabaseFileField.setEnabled(false);

        selectGS = new JRadioButton("select gene set database");
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


        upLoadGS = new JRadioButton("upload gene set database");
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

        loadGSDatabaseButton = new JButton("load");
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
        TaskInfo taskInfo = getModuleInfo();
        if(taskInfo != null)
        {
            addChipPlatformData(taskInfo);
            addGeneSetDatabase(taskInfo);            
        }
        else
        {
            String connectMessage = "First login to GenePattern to see choices";
            chipPlatform.addItem(connectMessage);
            gsDatabase.addItem(connectMessage);
        }

        chipPlatformFile = new JFileChooser();

        chipPlatformFileField = new JTextField();
        chipPlatformFileField.setEditable(false);
        chipPlatformFileField.setEnabled(false);
        chipPlatformFileField.setMinimumSize(new Dimension(110, 22));
        chipPlatformFileField.setMaximumSize(new Dimension(110, 22));
        chipPlatformFileField.setPreferredSize(new Dimension(110, 22));

        selectChip = new JRadioButton("select chip platform");
        selectChip.setSelected(true);
        selectChip.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent event)
            {
                if(event.getStateChange() == ItemEvent.SELECTED)
                {
                    chipPlatform.setEnabled(true);
                }
                if(event.getStateChange() == ItemEvent.DESELECTED)
                {
                    chipPlatform.setEnabled(false);
                }
            }
        });

        upLoadChip = new JRadioButton("upload chip platform file");
        upLoadChip.setSelected(false);
        upLoadChip.addItemListener(new ItemListener()
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

        ButtonGroup chipGroup = new ButtonGroup();
        chipGroup.add(selectChip);
        chipGroup.add(upLoadChip);

        loadChipButton = new JButton("Load");
        loadChipButton.setEnabled(false);
        loadChipButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                int returnValue = chipPlatformFile.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION)
                {
                    File file = chipPlatformFile.getSelectedFile();
                    chipPlatformFileField.setText(file.getAbsolutePath());
                }
            }
        });

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
        builder.append(selectChip);
        builder.append(chipPlatform);
        builder.nextLine();
        builder.append(upLoadChip);
        builder.append(chipPlatformFileField, loadChipButton);        
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
        return GSEAAnalysisPanel.class.getResource("paramDesc.html").getPath();
    }

    protected String getDescriptionFile()
    {
        return GSEAAnalysisPanel.class.getResource("help.html").getPath();
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

    private void addChipPlatformData(TaskInfo taskInfo)
    {
        ParameterInfo[] pArray = taskInfo.getParameterInfoArray();
        ParameterInfo chipPInfo = null;
        for(int i = 0; i < pArray.length; i++)
        {
            if(pArray[i].getName().equals("chip.platform"))
            {
                chipPInfo = pArray[i];
                break;
            }
        }
        if(chipPInfo != null)
        {
            Map<String, String> choices = chipPInfo.getChoices();
            Set<String> keys = choices.keySet();

            SortedSet sortedKeys = new TreeSet();
            sortedKeys.addAll(keys);
            Iterator<String> it = sortedKeys.iterator();
            if(sortedKeys.size() > 0)
                chipPlatform.removeAllItems();
            while(it.hasNext())
            {
                String key = it.next();
                chipPlatform.removeItem(key);
                chipPlatform.addItem(key);
            }

            repaint();
        }
    }

     private void addGeneSetDatabase(TaskInfo taskInfo)
    {
        ParameterInfo[] pArray = taskInfo.getParameterInfoArray();
        ParameterInfo gsDatabaseInfo = null;
        for(int i = 0; i < pArray.length; i++)
        {
            if(pArray[i].getName().equals("gene.sets.database"))
            {
                gsDatabaseInfo = pArray[i];
                break;
            }
        }
        if(gsDatabaseInfo != null)
        {
            Map<String, String> choices = gsDatabaseInfo.getChoices();
            Set<String> keys = choices.keySet();

            SortedSet sortedKeys = new TreeSet();
            sortedKeys.addAll(keys);
            Iterator<String> it = sortedKeys.iterator();            if(keys.size() > 0)

            if(sortedKeys.size() > 0)
                gsDatabase.removeAllItems();

            while(it.hasNext())
            {
                String key = it.next();
                gsDatabase.removeItem(key);
                gsDatabase.addItem(key);
            }

            repaint();
        }
    }

    public class MyServerConnectionListener extends ServerConnectionListener
    {
        public void serverConnected(ServerConnectionEvent event)
        {
            TaskInfo info = event.getModuleInfo();
            addChipPlatformData(info);
            addGeneSetDatabase(info);
        }
    }
}
