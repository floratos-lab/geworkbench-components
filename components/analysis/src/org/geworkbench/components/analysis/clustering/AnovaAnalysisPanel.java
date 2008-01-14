package org.geworkbench.components.analysis.clustering;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GeneOntologyTree;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GoMapping;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GOTerm;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.events.ProjectEvent;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.stat.descriptive.SummaryStatisticsImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.io.IOException;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Event;
import java.awt.BorderLayout;
import javax.swing.KeyStroke;
import javax.swing.JPanel;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JTree;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;

import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;

import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.algorithm.impl.OneWayANOVA;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.gui.Experiment;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Event;
import java.awt.BorderLayout;
import javax.swing.KeyStroke;
import javax.swing.JPanel;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.BoxLayout;
import java.awt.CardLayout;
import javax.swing.JButton;
import java.awt.GridLayout;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import javax.swing.JSlider;
import javax.swing.JProgressBar;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.plaf.basic.BasicSeparatorUI;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.ButtonGroup;
import javax.swing.table.DefaultTableModel;

import edu.columbia.geworkbench.cagrid.anova.*;
/**
 * @author yc2480
 * @version $id$
 */

public class AnovaAnalysisPanel extends AbstractSaveableParameterPanel implements Serializable {

	private int PermutationsNumberDefault=100;
	private String PermutationsNumberDefaultStr=Integer.valueOf(PermutationsNumberDefault).toString();
	
	public AnovaParameter anovaParameter=new AnovaParameter();

	/***
	 * copy paste the GUI code from VE start from here.
	 */	

	private JMenuBar jJMenuBar = null;

	private JMenu fileMenu = null;

	private JMenu editMenu = null;

	private JMenu helpMenu = null;

	private JMenuItem exitMenuItem = null;

	private JMenuItem aboutMenuItem = null;

	private JMenuItem cutMenuItem = null;

	private JMenuItem copyMenuItem = null;

	private JMenuItem pasteMenuItem = null;

	private JMenuItem saveMenuItem = null;

	private JTabbedPane jTabbedPane = null;

	private JPanel ParamPanel = null;

	private JPanel ServicePanel = null;

	private JPanel jPanelPValueEst = null;

	private JPanel jPanelFDR = null;

	private JPanel jPanelFDC = null;

	private JPanel jPanelPValueEstTitle = null;

	private JLabel jLabelPValueTitle = null;

	private JPanel jPanelPValueParam = null;

	private JComboBox jComboBoxPValueBasedOn = null;

	private JTextField jTextField = null;

	private JPanel jPanel1 = null;

	private JLabel jLabel1 = null;

	private JPanel jPanel2 = null;

	private JRadioButton jRadioButton = null;

	private JRadioButton jRadioButton1 = null;

	private JRadioButton jRadioButton2 = null;

	private JRadioButton jRadioButton3 = null;

	private JPanel jPanel3 = null;

	private JPanel jPanel4 = null;

	private JPanel jPanel5 = null;

	private JLabel jLabel3 = null;

	private JPanel jPanel6 = null;

	private JRadioButton jRadioButton4 = null;

	private JRadioButton jRadioButton5 = null;

	private JPanel jPanel7 = null;

	private JLabel jLabel = null;

	private JLabel jLabel2 = null;

//FDR and FDC should be the same section. So FDR and FDC have been combined as FDRC	
//	private ButtonGroup groupFDR = new ButtonGroup();
//	private ButtonGroup groupFDC = new ButtonGroup();	
	private ButtonGroup groupFDRC = new ButtonGroup();

	private JPanel jPanel = null;

	private JPanel jPanel8 = null;

	private JTextField jTextFieldNFSG = null;

	private JTextField jTextFieldPFSG = null;

	private JScrollPane jScrollPane = null;

	private JTable jTable = null;

	private DefaultTableModel defaultTableModel = null;  //  @jve:decl-index=0:visual-constraint=""

	private Vector vector = null;  //  @jve:decl-index=0:visual-constraint=""

	/**
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.addTab("Parameters", null, getParamPanel(), null);
			jTabbedPane.addTab("Service", null, getServicePanel(), null);
		}
		return jTabbedPane;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getParamPanel() {
		if (ParamPanel == null) {
			ParamPanel = new JPanel();
			ParamPanel.setLayout(new BoxLayout(getParamPanel(), BoxLayout.Y_AXIS));
			ParamPanel.add(getJPanelPValueEst(), null);
			ParamPanel.add(getJPanelFDR(), null);
			ParamPanel.add(getJPanelFDC(), null);
		}
		return ParamPanel;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getServicePanel() {
		if (ServicePanel == null) {
			ServicePanel = new JPanel();
			ServicePanel.setLayout(new BorderLayout());
			ServicePanel.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
		}
		return ServicePanel;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelPValueEst() {
		if (jPanelPValueEst == null) {
			jPanelPValueEst = new JPanel();
			jPanelPValueEst.setLayout(new BorderLayout());
			jPanelPValueEst.setName("jPanelPValueEst");
			jPanelPValueEst.setPreferredSize(new java.awt.Dimension(127,60));
			jPanelPValueEst.add(getJPanelPValueEstTitle(), java.awt.BorderLayout.NORTH);
			jPanelPValueEst.add(getJPanelPValueParam(), java.awt.BorderLayout.CENTER);
			Border border = BorderFactory.createTitledBorder("P-Value Estimation");
			jPanelPValueEst.setBorder(border);						
		}
		return jPanelPValueEst;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelFDR() {
		if (jPanelFDR == null) {
			jPanelFDR = new JPanel();
			jPanelFDR.setLayout(new BorderLayout());
			jPanelFDR.setName("jPanelFDR");
			jPanelFDR.add(getJPanel3(), java.awt.BorderLayout.NORTH);
		}
		return jPanelFDR;
	}

	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelFDC() {
		if (jPanelFDC == null) {
			jPanelFDC = new JPanel();
			jPanelFDC.setLayout(new BorderLayout());
			jPanelFDC.setName("jPanelFDC");
			jPanelFDC.add(getJPanel4(), java.awt.BorderLayout.NORTH);
		}
		return jPanelFDC;
	}

	/**
	 * This method initializes jPanelPValueEstTitle	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelPValueEstTitle() {
		if (jPanelPValueEstTitle == null) {
			jLabelPValueTitle = new JLabel();
			jLabelPValueTitle.setText("P-Value Estimation");
			jLabelPValueTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
			jLabelPValueTitle.setName("");
			jLabelPValueTitle.setVisible(true);
			jPanelPValueEstTitle = new JPanel();
			jPanelPValueEstTitle.setLayout(new CardLayout());
			jPanelPValueEstTitle.setVisible(false);
			jPanelPValueEstTitle.add(jLabelPValueTitle, jLabelPValueTitle.getName());
//			jPanelPValueEstTitle.add(new JSeparator(SwingConstants.HORIZONTAL));
		}
		return jPanelPValueEstTitle;
	}

	/**
	 * This method initializes jPanelPValueParam	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelPValueParam() {
		if (jPanelPValueParam == null) {
			jPanelPValueParam = new JPanel();
			jPanelPValueParam.setLayout(new CardLayout());
			jPanelPValueParam.setPreferredSize(new java.awt.Dimension(280,30));
			jPanelPValueParam.add(getJPanel7(), getJPanel7().getName());
		}
		return jPanelPValueParam;
	}

	/**
	 * This method initializes jComboBoxPValueBasedOn	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBoxPValueBasedOn() {
		if (jComboBoxPValueBasedOn == null) {
			jComboBoxPValueBasedOn = new JComboBox();
			jComboBoxPValueBasedOn.setName("P-Value Based On");
			jComboBoxPValueBasedOn.setToolTipText("P-Value Based On");
			jComboBoxPValueBasedOn.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if (e.getStateChange()==e.SELECTED){
						if (e.getItem().equals("Permutations")){
							jRadioButton4.setEnabled(true);
							jRadioButton5.setEnabled(true);
							jRadioButton3.setEnabled(true);
							jTextFieldNFSG.setEnabled(true);
							jTextFieldPFSG.setEnabled(true);
							jLabel2.setEnabled(true);
							jTextField.setEnabled(true);
							anovaParameter.setPValueEstimation(PValueEstimation.permutation);
						}else if (e.getItem().equals("F-distribution")){
							anovaParameter.setPValueEstimation(PValueEstimation.fdistribution);							
						}						
					}else if (e.getStateChange()==e.DESELECTED){
						if (e.getItem().equals("Permutations")){
							if (jRadioButton4.isSelected()){jRadioButton.setSelected(true);};
							jRadioButton4.setEnabled(false);							
							if (jRadioButton5.isSelected()){jRadioButton.setSelected(true);};
							jRadioButton5.setEnabled(false);							
							if (jRadioButton3.isSelected()){jRadioButton.setSelected(true);};
							jRadioButton3.setEnabled(false);
							jTextFieldNFSG.setEnabled(false);
							jTextFieldPFSG.setEnabled(false);
							jLabel2.setEnabled(false);
							jTextField.setEnabled(false);
						}else if (e.getItem().equals("F-distribution")){
							
						}						
					}
				}
			});
			jComboBoxPValueBasedOn.addItem("F-distribution");
			jComboBoxPValueBasedOn.addItem("Permutations");
		}
		return jComboBoxPValueBasedOn;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setText(PermutationsNumberDefaultStr);
			jTextField.setPreferredSize(new java.awt.Dimension(40,20));
			jTextField.setEnabled(false);
			jTextField.addCaretListener(new javax.swing.event.CaretListener() {
				public void caretUpdate(javax.swing.event.CaretEvent e) {
					//System.out.println("permutations number changes"); // TODO Auto-generated Event stub caretUpdate()
					if (jTextField.getText().equals("")){
						anovaParameter.setPermutationsNumber(PermutationsNumberDefault);
					}else{
						anovaParameter.setPermutationsNumber(Integer.valueOf(jTextField.getText()));
					}
				}
			});
		}
		return jTextField;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jLabel1 = new JLabel();
			jLabel1.setName("");
			jLabel1.setText("False Discovery Rate");
			jPanel1 = new JPanel();
			jPanel1.setLayout(new BoxLayout(getJPanel1(), BoxLayout.X_AXIS));
			jPanel1.setPreferredSize(new java.awt.Dimension(107,36));
			jPanel1.add(jLabel1, null);
			JSeparator Separator=new JSeparator(SwingConstants.HORIZONTAL);
			Separator.setUI( new BasicSeparatorUI() {
				public void paint( Graphics g, JComponent c ) {
					Dimension s = c.getSize();
					int pos = s.height/2;
					g.setColor( c.getForeground() );
					g.drawLine( 0, pos, s.width, pos++);
					g.setColor( c.getBackground() );
					g.drawLine( 0, pos, s.width, pos);
				}
			} );

			jPanel1.add(Separator, null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.setLayout(new BoxLayout(getJPanel2(), BoxLayout.Y_AXIS));
			jPanel2.add(getJRadioButton(), null);
			jPanel2.add(getJRadioButton1(), null);
			jPanel2.add(getJRadioButton2(), null);
			jPanel2.add(getJRadioButton3(), null);
			groupFDRC.add(getJRadioButton());
			groupFDRC.add(getJRadioButton1());
			groupFDRC.add(getJRadioButton2());			
			groupFDRC.add(getJRadioButton3());
		}
		return jPanel2;
	}

	/**
	 * This method initializes jRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButton() {
		if (jRadioButton == null) {
			jRadioButton = new JRadioButton();
			jRadioButton.setText("Just alpha (no correction)");
			jRadioButton.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			jRadioButton.setSelected(true);
			jRadioButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("just alpha seleted"); // TODO Auto-generated Event stub actionPerformed()
					anovaParameter.setFalseDiscoveryRateControl(FalseDiscoveryRateControl.alpha);
				}
			});
		}
		return jRadioButton;
	}

	/**
	 * This method initializes jRadioButton1	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButton1() {
		if (jRadioButton1 == null) {
			jRadioButton1 = new JRadioButton();
			jRadioButton1.setText("Standard Bonferroni");
			jRadioButton1.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("bonferroni seleted"); // TODO Auto-generated Event stub actionPerformed()
					anovaParameter.setFalseDiscoveryRateControl(FalseDiscoveryRateControl.bonferroni);
				}
			});
		}
		return jRadioButton1;
	}

	/**
	 * This method initializes jRadioButton2	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButton2() {
		if (jRadioButton2 == null) {
			jRadioButton2 = new JRadioButton();
			jRadioButton2.setText("Adjusted Bonferroni");
			jRadioButton2.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("adj bonferroni selected"); // TODO Auto-generated Event stub actionPerformed()
					anovaParameter.setFalseDiscoveryRateControl(FalseDiscoveryRateControl.adjbonferroni);
				}
			});
		}
		return jRadioButton2;
	}

	/**
	 * This method initializes jRadioButton3	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButton3() {
		if (jRadioButton3 == null) {
			jRadioButton3 = new JRadioButton();
			jRadioButton3.setText("Westfall-Young step down");
			jRadioButton3.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			jRadioButton3.setEnabled(false);
			jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("westfall young selected"); // TODO Auto-generated Event stub actionPerformed()
					anovaParameter.setFalseDiscoveryRateControl(FalseDiscoveryRateControl.westfallyoung);
				}
			});
		}
		return jRadioButton3;
	}

	/**
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			jPanel3 = new JPanel();
			jPanel3.setLayout(new BorderLayout());
			jPanel3.add(getJPanel1(), java.awt.BorderLayout.NORTH);
			jPanel3.add(getJPanel2(), java.awt.BorderLayout.CENTER);
		}
		return jPanel3;
	}

	/**
	 * This method initializes jPanel4	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			jPanel4 = new JPanel();
			jPanel4.setLayout(new BorderLayout());
			jPanel4.add(getJPanel5(), java.awt.BorderLayout.NORTH);
			jPanel4.add(getJPanel6(), java.awt.BorderLayout.CENTER);
		}
		return jPanel4;
	}

	/**
	 * This method initializes jPanel5	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel5() {
		if (jPanel5 == null) {
			jLabel3 = new JLabel();
			jLabel3.setName("");
			jLabel3.setText("False Discovery Control (permutations only)");
			jPanel5 = new JPanel();
			jPanel5.setLayout(new BoxLayout(getJPanel5(), BoxLayout.X_AXIS));
			jPanel5.setPreferredSize(new java.awt.Dimension(107,36));
			jPanel5.add(jLabel3, null);
		}
		return jPanel5;
	}

	/**
	 * This method initializes jPanel6	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel6() {
		if (jPanel6 == null) {
			jPanel6 = new JPanel();
			jPanel6.setLayout(new BoxLayout(getJPanel6(), BoxLayout.Y_AXIS));
			jPanel6.add(getJPanel(), null);
			jPanel6.add(getJPanel8(), null);
			groupFDRC.add(getJRadioButton4());
			groupFDRC.add(getJRadioButton5());
		}
		return jPanel6;
	}

	/**
	 * This method initializes jRadioButton4	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButton4() {
		if (jRadioButton4 == null) {
			jRadioButton4 = new JRadioButton();
			jRadioButton4.setText("The number of false significant genes should not exceed:");
			jRadioButton4.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			jRadioButton4.setEnabled(false);
			jRadioButton4.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("number of FSG selected"); // TODO Auto-generated Event stub actionPerformed()
					anovaParameter.setFalseDiscoveryRateControl(FalseDiscoveryRateControl.number);
					anovaParameter.setFalseSignificantGenesLimit(Float.valueOf(jTextFieldNFSG.getText()));
				}
			});
		}
		return jRadioButton4;
	}

	/**
	 * This method initializes jRadioButton5	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButton5() {
		if (jRadioButton5 == null) {
			jRadioButton5 = new JRadioButton();
			jRadioButton5.setText("The proportion of false significant genes should not exceed:");
			jRadioButton5.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			jRadioButton5.setEnabled(false);
			jRadioButton5.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("proportion of FSG seleted"); // TODO Auto-generated Event stub actionPerformed()
					anovaParameter.setFalseDiscoveryRateControl(FalseDiscoveryRateControl.proportion);
					anovaParameter.setFalseSignificantGenesLimit(Float.valueOf(jTextFieldPFSG.getText()));
				}
			});
		}
		return jRadioButton5;
	}

	/**
	 * This method initializes jPanel7	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel7() {
		if (jPanel7 == null) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(java.awt.FlowLayout.LEFT);
			jLabel2 = new JLabel();
			jLabel2.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			jLabel2.setEnabled(false);
			jLabel2.setText("Permutations #");
			jLabel = new JLabel();
			jLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			jLabel.setText("P-Value based on ");
			jLabel.setName("");
			jPanel7 = new JPanel();
			jPanel7.setLayout(flowLayout);
			jPanel7.setName("jPanel7");			
			jPanel7.add(jLabel, null);
			jPanel7.add(getJComboBoxPValueBasedOn(), null);
			jPanel7.add(jLabel2, null);
			jPanel7.add(getJTextField(), null);
		}
		return jPanel7;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			FlowLayout flowLayout1 = new FlowLayout();
			flowLayout1.setAlignment(java.awt.FlowLayout.LEFT);
			jPanel = new JPanel();
			jPanel.setPreferredSize(new java.awt.Dimension(383,30));
			jPanel.setLayout(flowLayout1);
			jPanel.add(getJRadioButton4(), null);
			jPanel.add(getJTextFieldNFSG(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel8	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel8() {
		if (jPanel8 == null) {
			FlowLayout flowLayout2 = new FlowLayout();
			flowLayout2.setAlignment(java.awt.FlowLayout.LEFT);
			jPanel8 = new JPanel();
			jPanel8.setPreferredSize(new java.awt.Dimension(396,30));
			jPanel8.setLayout(flowLayout2);
			jPanel8.add(getJRadioButton5(), null);
			jPanel8.add(getJTextFieldPFSG(), null);
		}
		return jPanel8;
	}

	/**
	 * This method initializes jTextFieldNFSG	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextFieldNFSG() {
		if (jTextFieldNFSG == null) {
			jTextFieldNFSG = new JTextField();
			jTextFieldNFSG.setText("10");
			jTextFieldNFSG.setEnabled(false);
			jTextFieldNFSG.setPreferredSize(new java.awt.Dimension(30,20));
			jTextFieldNFSG.addCaretListener(new javax.swing.event.CaretListener() {
				public void caretUpdate(javax.swing.event.CaretEvent e) {
					System.out.println("NFSG updated"); // TODO Auto-generated Event stub caretUpdate()
					if (jTextFieldNFSG.getText().equals("")){
						//caused by update through program
					}else{					
						anovaParameter.setFalseSignificantGenesLimit(Float.valueOf(jTextFieldNFSG.getText()));
					}
				}
			});
		}
		return jTextFieldNFSG;
	}

	/**
	 * This method initializes jTextFieldPFSG	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextFieldPFSG() {
		if (jTextFieldPFSG == null) {
			jTextFieldPFSG = new JTextField();
			jTextFieldPFSG.setText("0.05");
			jTextFieldPFSG.setEnabled(false);
			jTextFieldPFSG.setPreferredSize(new java.awt.Dimension(30,20));
			jTextFieldPFSG.addCaretListener(new javax.swing.event.CaretListener() {
				public void caretUpdate(javax.swing.event.CaretEvent e) {
					System.out.println("PFSG updated"); // TODO Auto-generated Event stub caretUpdate()
					if (jTextFieldPFSG.getText().equals("")){
						//caused by update through program
					}else{
						anovaParameter.setFalseSignificantGenesLimit(Float.valueOf(jTextFieldPFSG.getText()));
					}
				}
			});
		}
		return jTextFieldPFSG;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTable());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable();
			jTable.setModel(getDefaultTableModel());
		}
		return jTable;
	}

	/**
	 * This method initializes defaultTableModel	
	 * 	
	 * @return javax.swing.table.DefaultTableModel	
	 */
	private DefaultTableModel getDefaultTableModel() {
		if (defaultTableModel == null) {
			defaultTableModel = new DefaultTableModel();
			defaultTableModel.setColumnCount(5);
			defaultTableModel.setNumRows(5);
			defaultTableModel.setColumnIdentifiers(getVector());
			defaultTableModel.setRowCount(5);
		}
		return defaultTableModel;
	}

	/**
	 * This method initializes vector	
	 * 	
	 * @return java.util.Vector	
	 */
	private Vector getVector() {
		if (vector == null) {
			vector = new Vector();
			vector.setSize(5);
		}
		return vector;
	}




/***
 * copy paste the GUI code from VE till here.
 */	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AnovaAnalysisPanel application = new AnovaAnalysisPanel();
		application.show();
	}

	/**
	 * This is the default constructor
	 */
	public AnovaAnalysisPanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getJTabbedPane(), BorderLayout.CENTER);
		//setup some default values.
		anovaParameter.setPValueEstimation(PValueEstimation.fdistribution);
		anovaParameter.setFalseDiscoveryRateControl(FalseDiscoveryRateControl.alpha);
		anovaParameter.setPermutationsNumber(PermutationsNumberDefault);
		anovaParameter.setFalseSignificantGenesLimit(10.0f);
		//this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//this.setContentPane(getJTabbedPane());
		//this.setJMenuBar(getJJMenuBar());
		//this.setSize(585, 408);
		//this.setTitle("Application");
	}
	
    private DSMicroarraySet microarraySet;
    private JLabel infoLabel;

    /**
     * This method fulfills the contract of the {@link VisualPlugin} interface.
     * It returns the GUI component for this visual plugin.
     */
    public Component getComponent() {
        // In this case, this object is also the GUI component.
        return this;
    }

    /**
     * This is a <b>Subscribe</b> method. The annotation before the method alerts
     * the engine that it should route published objects to this method.
     * The type of objects that are routed to this method are indicated by the first parameter of the method.
     * In this case, it is {@link ProjectEvent}.
     *
     * @param event  the received object.
     * @param source the entity that published the object.
     */
    @Subscribe
    public void receive(ProjectEvent event, Object source) {

    	DSDataSet dataSet = event.getDataSet();
        // We will act on this object if it is a DSMicroarraySet
        if (dataSet instanceof DSMicroarraySet) {
	        microarraySet = (DSMicroarraySet) dataSet;
	        // We just received a new microarray set, so populate the info label with some basic stats.
	        String htmlText = "<html><body>"
	                + "<h3>" + microarraySet.getLabel() + "</h3><br>"
	                + "<table>"
	                + "<tr><td>Arrays:</td><td><b>" + microarraySet.size() + "</b></td></tr>"
	                + "<tr><td>Markers:</td><td><b>" + microarraySet.getMarkers().size() + "</b></td></tr>"
	                + "</table>"
	                + "<img src='http://www.google.com/intl/en_ALL/images/logo.gif'>"
	                + microarraySet.getDataSetName()
	                + "</body></html>";
	        infoLabel.setText(htmlText);
	        
	        
	        
//	        MultipleArrayData MADdata = new MultipleArrayData();
//	        MADdata.
/*
	        Experiment experiment;

	        ISlideDataElement slideDataElement;
	        for(int i = 0; i < maxRows ; i++){
	            for(int j = 0; j < maxColumns; j++){
	                if(!realData[i][j]){
	                    slideDataElement = new SlideDataElement(new int[]{i+1, 1, 1}, new int[]{j+1, 1,1}, intensities, new String[0]);
	                    slideData.insertElementAt(slideDataElement, i*maxColumns+j);
	                }
	            }
	        }
	        
	        
	        AlgorithmData data = new AlgorithmData();
            
            data.addMatrix("experiment", experiment.getMatrix());
            data.addParam("distance-factor", String.valueOf(1.0f));
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            
            data.addParam("distance-function", String.valueOf(function));
            data.addIntArray("group-assignments", groupAssignments);
            data.addParam("usePerms", String.valueOf(usePerms));
            data.addParam("numPerms", String.valueOf(numPerms));
            data.addParam("alpha", String.valueOf(alpha));
            data.addParam("correction-method", String.valueOf(correctionMethod));
            data.addParam("numGroups", String.valueOf(numGroups));
	        
	        for (int cx=0; cx<microarraySet.size(); cx++){
	        	FloatMatrix FM=new FloatMatrix(doubleArray2floatArray(microarraySet.getRow(cx)),1);
	        	data.addMatrix(((DSMicroarray)microarraySet.get(cx)).getLabel(), FM);
	        }
	        OneWayANOVA OWA=new OneWayANOVA();
	        try{
	            AlgorithmData result=OWA.execute(data);
	        	System.out.println(result.toString());
	        }catch (AlgorithmException AE){
	        	AE.printStackTrace();
	        }
*/	        
	        if (dataSet instanceof CSExprMicroarraySet) {
	        	System.out.println("CSExprMicroarraySet");
	        }
	        
        }        
    }
    
    private float[] doubleArray2floatArray(double [] doubleArray){
    	int arrayLength=doubleArray.length;
    	float [] ans = new float[arrayLength];
    	for (int cx=0; cx<arrayLength; cx++){
    		ans[cx]=(float)doubleArray[cx];
    	}
    	return ans; 
    }    

    private static class SerializedInstance implements Serializable {

        private AnovaParameter anovaParameter;
        
        public SerializedInstance(AnovaParameter anovaParameter) {
            this.anovaParameter=anovaParameter;
        }

    	//we rewrite the readResolve so we can store the GUI using serialized anovaParameter data.
        Object readResolve() throws ObjectStreamException {
            AnovaAnalysisPanel panel = new AnovaAnalysisPanel();
            panel.anovaParameter=this.anovaParameter;
            
            //start translate anovaParameter to AnovaAnalysisPanel by manipulate GUI according to anovaParameter
            if (anovaParameter.getPValueEstimation().equals(PValueEstimation.permutation)){
            	panel.jComboBoxPValueBasedOn.setSelectedItem("Permutations");
            }else{
            	panel.jComboBoxPValueBasedOn.setSelectedItem("F-distribution");
            }
            panel.jTextField.setText(anovaParameter.getPermutationsNumber().toString());
            
            if (anovaParameter.getFalseDiscoveryRateControl().equals(FalseDiscoveryRateControl.alpha)){
            	panel.jRadioButton.setSelected(true);
            }else if (anovaParameter.getFalseDiscoveryRateControl().equals(FalseDiscoveryRateControl.bonferroni)){
            	panel.jRadioButton1.setSelected(true);            	
            }else if (anovaParameter.getFalseDiscoveryRateControl().equals(FalseDiscoveryRateControl.adjbonferroni)){
            	panel.jRadioButton2.setSelected(true);
            }else if (anovaParameter.getFalseDiscoveryRateControl().equals(FalseDiscoveryRateControl.westfallyoung)){
            	panel.jRadioButton3.setSelected(true);
            }else if (anovaParameter.getFalseDiscoveryRateControl().equals(FalseDiscoveryRateControl.number)){
            	panel.jRadioButton4.setSelected(true);
            	panel.jTextFieldNFSG.setText(anovaParameter.getFalseSignificantGenesLimit().toString());
            }else if (anovaParameter.getFalseDiscoveryRateControl().equals(FalseDiscoveryRateControl.proportion)){
            	panel.jRadioButton5.setSelected(true);
            	panel.jTextFieldPFSG.setText(anovaParameter.getFalseSignificantGenesLimit().toString());
            }
            
            
            System.out.println(anovaParameter.getPValueEstimation());
            System.out.println(anovaParameter.getPermutationsNumber());
            System.out.println(anovaParameter.getFalseDiscoveryRateControl());
            System.out.println(anovaParameter.getFalseSignificantGenesLimit());

            return panel;
        }
    }

    Object writeReplace() throws ObjectStreamException {
    	//we rewrite the writeReplace so when serialization, only anovaParameter been stored.
        return new SerializedInstance(this.anovaParameter);
    }
    
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        revalidate();
    }

}
