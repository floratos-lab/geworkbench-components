package org.geworkbench.components.markus;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.border.TitledBorder;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Parameters panel used by Mark-Us.
 * 
 * @author meng
 * @author zji
 * @version $Id: MarkUsConfigPanel.java,v 1.3 2009-02-17 21:59:38 jiz Exp $
 */
public class MarkUsConfigPanel extends AbstractSaveableParameterPanel implements Serializable {
	private static final long serialVersionUID = 238110585216063808L;
	
	//structure analysis
    JCheckBox skan = new JCheckBox("", true);
    JCheckBox dali = new JCheckBox();
    JCheckBox screen = new JCheckBox("", true);
    JCheckBox delphi = new JCheckBox("", true);

    //sequence analysis
    JCheckBox psiblast = new JCheckBox("", true);
    JCheckBox ips = new JCheckBox("", true);
    JCheckBox consurf = new JCheckBox("", true);
    JCheckBox consurf3 = new JCheckBox("", false);
    JCheckBox consurf4 = new JCheckBox("", false);

    JComboBox cbxChain = new JComboBox(new String[]{"A"} );

    //delphi parameters
    JFormattedTextField gridsize = new JFormattedTextField();
    JFormattedTextField boxfill = new JFormattedTextField();
    JFormattedTextField steps = new JFormattedTextField();
    JFormattedTextField sc = new JFormattedTextField();
    JFormattedTextField radius = new JFormattedTextField();
    JComboBox ibc = new JComboBox(new String[]
	{"Zero",
	 "Debye-Huckel Dipole",
	 "Debye-Huckel Total"});
    JFormattedTextField nli = new JFormattedTextField();
    JFormattedTextField li = new JFormattedTextField();
    JFormattedTextField idc = new JFormattedTextField();
    JFormattedTextField edc = new JFormattedTextField();

    //consurf parameters
    JFormattedTextField csftitle3 = new JFormattedTextField();
    JFormattedTextField csftitle4 = new JFormattedTextField();
    JFormattedTextField eval3 = new JFormattedTextField();
    JFormattedTextField eval4 = new JFormattedTextField();
    JFormattedTextField iter3 = new JFormattedTextField();
    JFormattedTextField iter4 = new JFormattedTextField();
    JFormattedTextField filter3 = new JFormattedTextField();
    JFormattedTextField filter4 = new JFormattedTextField();
	JComboBox msa3 = new JComboBox(new String[] { "Muscle", "ClustalW" });
	JComboBox msa4 = new JComboBox(new String[] { "Muscle", "ClustalW" });

	// markus parameters 1st part - totally 9
	public boolean getskanValue() {
		return skan.isSelected();
	}
	public boolean getdaliValue() {
		return dali.isSelected();
	}
    public boolean getscreenValue(){
    	return screen.isSelected();
    }
    public boolean getdelphiValue(){
    	return delphi.isSelected();
    }
    public boolean getpsiblastValue(){
    	return psiblast.isSelected();
    }
    public boolean getipsValue(){
    	return ips.isSelected();
    }
    public boolean getconsurfValue(){
    	return consurf.isSelected();
    }
    public boolean getconsurf3Value(){
    	return consurf3.isSelected();
    }
    public boolean getconsurf4Value(){
    	return consurf4.isSelected();
    }

    public String getChain(){
    	return (String)cbxChain.getSelectedItem();
    }

    //delphi part - totally 10
    public int getgridsizeValue(){
    	return ((Integer)gridsize.getValue()).intValue();
    }
    public int getboxfillValue(){
    	return ((Integer)boxfill.getValue()).intValue();
    }
    public int getstepsValue(){
    	return ((Integer)steps.getValue()).intValue();
    }
    public double getscValue(){
    	return ((Double)sc.getValue()).doubleValue();
    }
    public double getradiusValue(){
    	return ((Double)radius.getValue()).doubleValue();
    }
    public int getibcValue(){
    	String desc = (String) ibc.getSelectedItem();
		if (desc.equals("Zero")) {
			return 1;
		} else if (desc.equals("Debye-Huckel Dipole")) {
			return 2;
		} else if (desc.equals("Debye-Huckel Total")) {
			return 4;
		}
		return 0;
    }
    public int getnliValue(){
    	return ((Integer)nli.getValue()).intValue();
    }
    public int getliValue(){
    	return ((Integer)li.getValue()).intValue();
    }
    public int getidcValue(){
    	return ((Integer)idc.getValue()).intValue();
    }
    public int getedcValue(){
    	return ((Integer)edc.getValue()).intValue();
    }

    // analysis 3 & 4
    public String getcsftitle3Value(){
    	String val = (String)csftitle3.getValue();
    	val = val.replaceAll(" ", "%20");
    	return val;
    }
    public String getcsftitle4Value(){
		String val = (String)csftitle4.getValue();
		val = val.replaceAll(" ", "%20");
		return val;
    }
    public double geteval3Value(){
    	return ((Double)eval3.getValue()).doubleValue();
    }
    public double geteval4Value(){
    	return ((Double)eval4.getValue()).doubleValue();
    }
    public int getiter3Value(){
    	return ((Integer)iter3.getValue()).intValue();
    }
    public int getiter4Value(){
    	return ((Integer)iter4.getValue()).intValue();
    }
    public int getfilter3Value(){
    	return ((Integer)filter3.getValue()).intValue();
    }
    public int getfilter4Value(){
    	return ((Integer)filter4.getValue()).intValue();
    }
    public String getmsa3Value(){
    	return (String)msa3.getSelectedItem();
    }
    public String getmsa4Value(){
    	return (String)msa4.getSelectedItem();
    }

    public MarkUsConfigPanel() {
		JTabbedPane tp = new JTabbedPane(SwingConstants.TOP);
		tp.addTab("Markus Parameters", buildMainPanel());
		tp.addTab("DelPhi Parameters", buildDelphiPanel());
		tp.addTab("Add Customized ConSurf analysis 3", buildConsurf3Panel());
		tp.addTab("Add Customized ConSurf analysis 4", buildConsurf4Panel());
	
		ToolTipManager.sharedInstance().setDismissDelay(1000*120);
	
		setDefaultParameters();

		setLayout(new BorderLayout());
        add(tp, BorderLayout.PAGE_START);
    }
    
    private JComponent buildMainPanel()
    {
		JPanel jp = new JPanel(new GridLayout(2, 2));
		jp.add(buildLeftPanel());
		jp.add(buildRightPanel());

        FormLayout layout = new FormLayout(
                "left:max(25dlu;pref), 10dlu, left:max(35dlu;pref), 2dlu, max(25dlu;pref), 5dlu, "+
                "left:max(25dlu;pref), 2dlu, max(25dlu;pref)",
                "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.append("Chain", cbxChain);
		jp.add(builder.getPanel());
		return jp;
    }

    private JComponent buildLeftPanel()
    {
        FormLayout layout = new FormLayout(
                "left:max(25dlu;pref), 10dlu, left:max(35dlu;pref), 2dlu, max(25dlu;pref), 5dlu, "+
                "left:max(25dlu;pref), 2dlu, max(25dlu;pref)",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

		//	JComponent sp = builder.appendSeparator("Structure Homologs");
		JLabel sh = new JLabel("Structure Homologs");
		sh.setForeground(Color.blue);
		sh.setToolTipText("<html>Structure relationships are identified by the structure alignment method Skan. The reference database combines SCOP<br>domains and PDB entries filtered by 60% sequence identity. In addition the Dali structure alignment method can be used.</html>");
		builder.append(sh);
	
		skan.setEnabled(false);
        builder.append("Skan", skan);
		builder.append("Dali", dali);
	
		sh = new JLabel("Cavity Analysis");
		sh.setForeground(Color.blue);
		sh.setToolTipText("<html>SCREEN is used to identify protein cavities that are capable of binding chemical compounds. SCREEN will provide an<br>assessment of the druggability of each surface cavity based on its properties.</html>");
		builder.append(sh);
	
		screen.setEnabled(false);
		builder.append("SCREEN", screen);
		builder.nextLine();
	
		sh = new JLabel("Electrostatic Potential");
		sh.setForeground(Color.blue);
		sh.setToolTipText("<html>The electrostatic potential plays an important role in inferring protein properties like DNA binding regions or the enzymatic<br>activities. To calculate the electrostatic potential DelPhi is used. The default parameters are tuned to suit protein domains in<br>general, though adjustment by the user might be necessary. Read the DelPhi manual for a detailed parameter description.</html>");
		builder.append(sh);
		builder.append("DelPhi", delphi);
	
		JPanel p = new JPanel();
		TitledBorder tb = BorderFactory.createTitledBorder("Structure Analysis");
		p.setBorder(tb);
		p.add(builder.getPanel());
		return p;
    }

    private JComponent buildRightPanel()
    {
        FormLayout layout = new FormLayout(
                "left:max(25dlu;pref), 10dlu, left:max(35dlu;pref), 2dlu, max(25dlu;pref), 5dlu, "+
                "left:max(25dlu;pref), 2dlu, max(25dlu;pref)",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

		JLabel sh = new JLabel("Sequence Homologs");
		sh.setForeground(Color.blue);
		sh.setToolTipText("<html>Proteins sharing sequence similarity with the target protein are identified by running three PSI BLAST iterations (E-value<br>0.001) against the UniProt reference database. Additionally sequence domain and motif databases are searched using the<br>InterProScan service at the EBI.</html>");
		builder.append(sh);
	
		psiblast.setEnabled(false);
		builder.append("PSI-BLAST", psiblast);
		ips.setEnabled(false);
		builder.append("InterProScan", ips);
	
		sh = new JLabel("<html>Amino Acid<br>Conservation Profile</html>");
		sh.setForeground(Color.blue);
		sh.setToolTipText("<html>Highly conserved amino acids can indicate functionally relevant regions. To identify these amino acids sequences identified<br>by BLAST sharing less than 80% identity are aligned using Muscle. For the resulting multiple sequence alignment ConSurf is<br>used to estimate the conservation scores. If seeds and full Pfam alignments are available, these are used additionally for<br>the conservation analysis.<br>Two ConSurf analyses can be defined by the User specifying the number of PSI-BLAST iterations, the E-value threshold,<br>and the sequence identity cutoff.</html>");
		builder.append(sh);
		consurf.setEnabled(false);
		builder.append("ConSurf", consurf);
		builder.nextLine();
	
		sh = new JLabel("Add Customized ConSurf");
		sh.setForeground(Color.blue);
		builder.append(sh);
		builder.append("analysis3", consurf3);
		builder.append("analysis4", consurf4);
	
		JPanel p = new JPanel();
		TitledBorder tb = BorderFactory.createTitledBorder("Sequence Analysis");
		p.setBorder(tb);
		p.add(builder.getPanel());
		return p;
    }

    private JComponent buildDelphiPanel()
    {
        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 4dlu, max(60dlu;pref), 5dlu, "+
                "left:max(40dlu;pref), 4dlu, max(60dlu;pref), 5dlu, "+
                "left:max(40dlu;pref), 4dlu, max(60dlu;pref)",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

		builder.append("Grid size", gridsize);
		builder.append("Percentage box fill", boxfill);
		builder.append("Focusing steps", steps);
		builder.nextLine();
		builder.append("Salt concentration", sc);
		builder.append("Probe radius", radius);
		builder.append("Initial boundary condition", ibc);
		builder.nextLine();
		builder.append("Non linear iterations", nli);
		builder.append("Linear iterations", li);
		builder.nextLine();
		builder.append("Internal dielectric constant", idc);
		builder.append("External dielectric constant", edc);
	
		return builder.getPanel();
    }

    private JComponent buildConsurf3Panel()
    {
        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 4dlu, max(60dlu;pref), 5dlu, "+
                "left:max(40dlu;pref), 4dlu, max(60dlu;pref)",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

		builder.append("Title", csftitle3);
		builder.nextLine();
		builder.append("PSI-Blast E Value", eval3);
		builder.append("Iterations", iter3);
		builder.nextLine();
		builder.append("Identity Filter Percentage", filter3);
		builder.append("Multiple Sequence Alignment", msa3);
	
		return builder.getPanel();
    }

    private JComponent buildConsurf4Panel()
    {
        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 4dlu, max(60dlu;pref), 5dlu, "+
                "left:max(40dlu;pref), 4dlu, max(60dlu;pref)",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

		builder.append("Title", csftitle4);
		builder.nextLine();
		builder.append("PSI-Blast E Value", eval4);
		builder.append("Iterations", iter4);
		builder.nextLine();
		builder.append("Identity Filter Percentage", filter4);
		builder.append("Multiple Sequence Alignment", msa4);
	
		return builder.getPanel();
    }

    private void setDefaultParameters()
    {
		//structure analysis
		dali.setSelected(false);
		delphi.setSelected(true);
	
		//delphi
		int gridsizeValue = 145;
		gridsize.setValue(gridsizeValue);
		gridsize.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		int boxfillValue = 85;
		boxfill.setValue(boxfillValue);
		boxfill.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		int stepsValue = 3;
		steps.setValue(stepsValue);
		steps.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		double scValue = 0.145;
		sc.setValue(scValue);
		sc.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		double radiusValue = 1.4;
		radius.setValue(radiusValue);
		radius.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		int ibcValue = 2;
		ibc.setSelectedIndex(ibcValue);
		int nliValue = 1000;
		nli.setValue(nliValue);
		nli.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		int liValue = 1000;
		li.setValue(liValue);
		li.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		int idcValue = 2;
		idc.setValue(idcValue);
		idc.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		int edcValue = 80;
		edc.setValue(edcValue);
		edc.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
	
		//consurf
		String csftitle3Value = "analysis 3";
		csftitle3.setValue(csftitle3Value);
		csftitle3.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		int msa3Value = 0;
		msa3.setSelectedIndex(msa3Value);
	
		String csftitle4Value = "analysis 4";
		csftitle4.setValue(csftitle4Value);
		csftitle4.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		int msa4Value = 0;
		msa4.setSelectedIndex(msa4Value);
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        revalidate();
    }
}

