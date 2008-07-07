package org.geworkbench.components.markus;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.*;

/**
 * Parameters panel used by Mark-Us
 */
public class MarkUsConfigPanel extends AbstractSaveableParameterPanel implements Serializable {
    private NumberFormat formatdouble = NumberFormat.getInstance();

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
    JComboBox msa3 = new JComboBox(new String[]{"Muscle", "ClustalW"});
    JComboBox msa4 = new JComboBox(new String[]{"Muscle", "ClustalW"});

    public boolean getskanValue(){
	return skan.isSelected();
    }
    public boolean getdaliValue(){
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
	String desc = (String)ibc.getSelectedItem();
	if (desc.equals("Zero")) { return 1; }
	else if (desc.equals("Debye-Huckel Dipole")) { return 2; }
	else if (desc.equals("Debye-Huckel Total")) { return 4; }
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
    //todo: make sure input type is correct
    public int getedcValue(){
	return ((Integer)edc.getValue()).intValue();
    }
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
    public Integer getfilter3Value(){
	return ((Integer)filter3.getValue()).intValue();
    }
    public Integer getfilter4Value(){
	return ((Integer)filter4.getValue()).intValue();
    }
    public String getmsa3Value(){
	return (String)msa3.getSelectedItem();
    }
    public String getmsa4Value(){
	return (String)msa4.getSelectedItem();
    }

    private static class SerializedInstance implements Serializable {


        public SerializedInstance(){
	}

        Object readResolve() throws ObjectStreamException {

            MarkUsConfigPanel panel = new MarkUsConfigPanel();
            return panel;
        }
    }

    Object writeReplace() throws ObjectStreamException {
	return new SerializedInstance();
    }

    public MarkUsConfigPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
		JTabbedPane tp = new JTabbedPane(SwingConstants.TOP);
		tp.addTab("Markus Parameters", buildMainPanel());
		tp.addTab("DelPhi Parameters", buildDelphiPanel());
		tp.addTab("Add Customized ConSurf analysis 3", buildConsurf3Panel());
		tp.addTab("Add Customized ConSurf analysis 4", buildConsurf4Panel());
	
		ToolTipManager.sharedInstance().setDismissDelay(1000*120);
	
		setDefaultParameters();

        this.add(tp, BorderLayout.CENTER);
    }
    
    private JComponent buildMainPanel()
    {
		JPanel jp = new JPanel(new GridLayout(1, 2));
		jp.add(buildLeftPanel());
		jp.add(buildRightPanel());
		return jp;
    }

    private JComponent buildLeftPanel()
    {
	formatdouble.setMaximumFractionDigits(4);

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
	formatdouble.setMaximumFractionDigits(4);

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
	formatdouble.setMaximumFractionDigits(4);

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
	formatdouble.setMaximumFractionDigits(4);

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
	formatdouble.setMaximumFractionDigits(4);

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

    public void setDefaultParameters()
    {
	//structure analysis
	dali.setSelected(false);
	delphi.setSelected(true);

	//sequence analysis
	//	consurf.setSelected(true);

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

