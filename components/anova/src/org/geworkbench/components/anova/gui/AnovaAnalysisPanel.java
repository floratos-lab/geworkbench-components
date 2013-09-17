package org.geworkbench.components.anova.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSeparatorUI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.events.listeners.ParameterActionListener;

import org.geworkbench.components.anova.FalseDiscoveryRateControl;
import org.geworkbench.components.anova.PValueEstimation;

/**
 * @author yc2480
 * @version $Id$
 */
public class AnovaAnalysisPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = 7706231137820482937L;

	public int permutationsNumber = 0;
	public Float falseSignificantGenesLimit = null;
	public Float pValueThreshold = null;

	public FalseDiscoveryRateControl falseDiscoveryRateControl;

	public PValueEstimation pValueEstimation = null;

	private int PermutationsNumberDefault = 100;

	private float PValueThresholdDefault = 0.05f;

	private String PermutationsNumberDefaultStr = Integer.valueOf(
			PermutationsNumberDefault).toString();
	private static Log log = LogFactory.getLog(AnovaAnalysisPanel.class);

	/***************************************************************************
	 * copy paste the GUI code from VE start from here.
	 */

	private JPanel ParamPanel = null;

	private JPanel jPanelPValueEst = null;

	private JPanel jPanelFDR = null;

	private JPanel jPanelFDC = null;

	private JPanel jPanelPValueEstTitle = null;

	private JLabel jLabelPValueTitle = null;

	private JPanel jPanelPValueParam = null;

	private JComboBox jComboBoxPValueBasedOn = null;

	private JTextField permutationsNumberJTextField = null;

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

	// FDR and FDC should be the same section. So FDR and FDC have been combined
	// as FDRC
	// private ButtonGroup groupFDR = new ButtonGroup();
	// private ButtonGroup groupFDC = new ButtonGroup();
	private ButtonGroup groupFDRC = new ButtonGroup();

	private JPanel jPanel = null;

	private JPanel jPanel8 = null;

	private JTextField jTextFieldNFSG = null;

	private JTextField jTextFieldPFSG = null;

	private JPanel jPanelPValueThreshold = null;

	private JTextField jTextFieldPValueThreshold = null;

	private JLabel jLabelPValueThreshold = null;

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getParamPanel() {
		if (ParamPanel == null) {
			ParamPanel = new JPanel();
			ParamPanel.setLayout(new BoxLayout(getParamPanel(),
					BoxLayout.Y_AXIS));
			ParamPanel.add(getJPanelPValueEst(), null);
			ParamPanel.add(getJPanelFDR(), null);
			ParamPanel.add(getJPanelFDC(), null);
		}
		return ParamPanel;
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
			jPanelPValueEst.add(getJPanelPValueEstTitle(),
					java.awt.BorderLayout.NORTH);
			jPanelPValueEst.add(getJPanelPValueParam(),
					java.awt.BorderLayout.CENTER);
			Border border = BorderFactory
					.createTitledBorder("P-Value Estimation");
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
			jLabelPValueTitle.setFont(new java.awt.Font("Dialog",
					java.awt.Font.BOLD, 12));
			jLabelPValueTitle.setName("");
			jLabelPValueTitle.setVisible(true);
			jPanelPValueEstTitle = new JPanel();
			jPanelPValueEstTitle.setLayout(new CardLayout());
			jPanelPValueEstTitle.setVisible(false);
			jPanelPValueEstTitle.add(jLabelPValueTitle,
					jLabelPValueTitle.getName());
			// jPanelPValueEstTitle.add(new
			// JSeparator(SwingConstants.HORIZONTAL));
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
			jPanelPValueParam.setLayout(new BoxLayout(getJPanelPValueParam(),
					BoxLayout.Y_AXIS));
			jPanelPValueParam.add(getJPanel7(), null);
			jPanelPValueParam.add(getJPanelPValueThreshold(), null);
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
			jComboBoxPValueBasedOn
					.addItemListener(new java.awt.event.ItemListener() {
						public void itemStateChanged(ItemEvent e) {

							if (e.getStateChange() == ItemEvent.SELECTED) {
								if (e.getItem().equals("Permutations")) {
									jRadioButton4.setEnabled(true);
									jRadioButton5.setEnabled(true);
									jRadioButton3.setEnabled(true);
									jTextFieldNFSG.setEnabled(true);
									jTextFieldPFSG.setEnabled(true);
									jLabel2.setEnabled(true);
									permutationsNumberJTextField
											.setEnabled(true);
									pValueEstimation = PValueEstimation.permutation;
								} else if (e.getItem().equals("F-distribution")) {
									pValueEstimation = PValueEstimation.fdistribution;
								}
							} else if (e.getStateChange() == ItemEvent.DESELECTED) {
								if (e.getItem().equals("Permutations")) {
									if (jRadioButton4.isSelected()) {
										jRadioButton.setSelected(true);
										falseDiscoveryRateControl = FalseDiscoveryRateControl.alpha;
									}
									;
									jRadioButton4.setEnabled(false);
									if (jRadioButton5.isSelected()) {
										jRadioButton.setSelected(true);
										falseDiscoveryRateControl = FalseDiscoveryRateControl.alpha;
									}
									;
									jRadioButton5.setEnabled(false);
									if (jRadioButton3.isSelected()) {
										jRadioButton.setSelected(true);
										falseDiscoveryRateControl = FalseDiscoveryRateControl.alpha;
									}
									;
									jRadioButton3.setEnabled(false);
									jTextFieldNFSG.setEnabled(false);
									jTextFieldPFSG.setEnabled(false);
									jLabel2.setEnabled(false);
									permutationsNumberJTextField
											.setEnabled(false);
								} else if (e.getItem().equals("F-distribution")) {

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
	private JTextField getPermNumberJTextField() {
		if (permutationsNumberJTextField == null) {
			permutationsNumberJTextField = new JTextField();
			permutationsNumberJTextField.setText(PermutationsNumberDefaultStr);
			permutationsNumberJTextField.setColumns(5);
			permutationsNumberJTextField.setMaximumSize(new Dimension(8,
					permutationsNumberJTextField.getHeight()));
			permutationsNumberJTextField.setEnabled(false);
			permutationsNumberJTextField
					.addCaretListener(new javax.swing.event.CaretListener() {
						public void caretUpdate(javax.swing.event.CaretEvent e) {
							log.debug("permutations number changes");
							try {
								if (permutationsNumberJTextField.getText()
										.equals("")) {
									permutationsNumber = PermutationsNumberDefault;
								} else {
									permutationsNumber = Integer
											.valueOf(permutationsNumberJTextField
													.getText());

								}
							} catch (NumberFormatException nfe) {

								JOptionPane.showMessageDialog(null,
										"Permutations # must be a number.",
										"Please try again.",
										JOptionPane.INFORMATION_MESSAGE);
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										permutationsNumberJTextField
												.setText("100");
									}
								});
								
							}

						}

					});
		}
		return permutationsNumberJTextField;
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
			jLabel1.setText("P-value Corrections");
			jPanel1 = new JPanel();
			jPanel1.setLayout(new BoxLayout(getJPanel1(), BoxLayout.X_AXIS));
			jPanel1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			jPanel1.add(jLabel1, null);
			JSeparator Separator = new JSeparator(SwingConstants.HORIZONTAL);
			Separator.setUI(new BasicSeparatorUI() {
				public void paint(Graphics g, JComponent c) {
					Dimension s = c.getSize();
					int pos = s.height / 2;
					g.setColor(c.getForeground());
					g.drawLine(0, pos, s.width, pos++);
					g.setColor(c.getBackground());
					g.drawLine(0, pos, s.width, pos);
				}
			});

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
			jPanel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
			jRadioButton.setFont(new java.awt.Font("Dialog",
					java.awt.Font.PLAIN, 12));
			jRadioButton.setSelected(true);
			jRadioButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					log.debug("just alpha seleted");
					falseDiscoveryRateControl = FalseDiscoveryRateControl.alpha;
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
			jRadioButton1.setFont(new java.awt.Font("Dialog",
					java.awt.Font.PLAIN, 12));
			jRadioButton1
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							log.debug("bonferroni seleted");
							falseDiscoveryRateControl = FalseDiscoveryRateControl.bonferroni;
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
			jRadioButton2.setFont(new java.awt.Font("Dialog",
					java.awt.Font.PLAIN, 12));
			jRadioButton2
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							log.debug("adj bonferroni selected");
							falseDiscoveryRateControl = FalseDiscoveryRateControl.adjbonferroni;
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
			jRadioButton3.setFont(new java.awt.Font("Dialog",
					java.awt.Font.PLAIN, 12));
			jRadioButton3.setEnabled(false);
			jRadioButton3
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							log.debug("westfall young selected");
							falseDiscoveryRateControl = FalseDiscoveryRateControl.westfallyoung;
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
			jPanel5.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
			jRadioButton4
					.setText("The number of false significant genes should not exceed:");
			jRadioButton4.setFont(new java.awt.Font("Dialog",
					java.awt.Font.PLAIN, 12));
			jRadioButton4.setEnabled(false);
			jRadioButton4
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							log.debug("number of FSG selected");
							falseDiscoveryRateControl = FalseDiscoveryRateControl.number;
							falseSignificantGenesLimit = Float
									.valueOf(jTextFieldNFSG.getText());
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
			jRadioButton5
					.setText("The proportion of false significant genes should not exceed:");
			jRadioButton5.setFont(new java.awt.Font("Dialog",
					java.awt.Font.PLAIN, 12));
			jRadioButton5.setEnabled(false);
			jRadioButton5
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							log.debug("proportion of FSG seleted");
							falseDiscoveryRateControl = FalseDiscoveryRateControl.proportion;
							falseSignificantGenesLimit = Float
									.valueOf(jTextFieldPFSG.getText());
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
			jPanel7.add(getPermNumberJTextField(), null);
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
			jPanel.setLayout(flowLayout1);
			jPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
			jPanel8.setLayout(flowLayout2);
			jPanel8.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
			jTextFieldNFSG.setColumns(5);
			jTextFieldNFSG.setMaximumSize(new Dimension(8, jTextFieldNFSG
					.getHeight()));
			jTextFieldNFSG.setEnabled(false);
			jTextFieldNFSG
					.addCaretListener(new javax.swing.event.CaretListener() {
						public void caretUpdate(javax.swing.event.CaretEvent e) {
							log.debug("NFSG updated");
							try {
								if (jTextFieldNFSG.getText().equals("")) {
									// caused by update through program
								} else {
									falseSignificantGenesLimit = Float
											.valueOf(jTextFieldNFSG.getText());
								}
							} catch (NumberFormatException nfe) {

								JOptionPane.showMessageDialog(null,
										"Please enter a number.",
										"Please try again.",
										JOptionPane.INFORMATION_MESSAGE);
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										jTextFieldNFSG
												.setText("10");
									}
								});
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
			jTextFieldPFSG.setColumns(5);
			jTextFieldPFSG.setMaximumSize(new Dimension(8, jTextFieldPFSG
					.getHeight()));
			jTextFieldPFSG.setEnabled(false);
			jTextFieldPFSG
					.addCaretListener(new javax.swing.event.CaretListener() {
						public void caretUpdate(javax.swing.event.CaretEvent e) {
							log.debug("PFSG updated");
							try {
								if (jTextFieldPFSG.getText().equals("")) {
									// caused by update through program
								} else {
									falseSignificantGenesLimit = Float
											.valueOf(jTextFieldPFSG.getText());
								}
							} catch (NumberFormatException nfe) {

								JOptionPane
										.showMessageDialog(
												null,
												"Please enter a number.",
												"Proportion should be a float number between 0.0 and 1.0.",
												JOptionPane.INFORMATION_MESSAGE);
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										jTextFieldPFSG
												.setText("0.05");
									}
								});
							}
						}
					});
		}
		return jTextFieldPFSG;
	}

	/**
	 * This method initializes jPanelPValueThreshold
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelPValueThreshold() {
		if (jPanelPValueThreshold == null) {
			jLabelPValueThreshold = new JLabel();
			jLabelPValueThreshold.setText("P-Value Threshold");
			jLabelPValueThreshold.setFont(new java.awt.Font("Dialog",
					java.awt.Font.PLAIN, 12));
			FlowLayout flowLayout3 = new FlowLayout();
			flowLayout3.setAlignment(java.awt.FlowLayout.LEFT);
			jPanelPValueThreshold = new JPanel();
			jPanelPValueThreshold.setLayout(flowLayout3);
			jPanelPValueThreshold.setName("jPanelPValueThreshold");
			jPanelPValueThreshold.add(jLabelPValueThreshold, null);
			jPanelPValueThreshold.add(getjTextFieldPValueThreshold(), null);
		}
		return jPanelPValueThreshold;
	}

	/**
	 * This method initializes jTextFieldPValueThreshold
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getjTextFieldPValueThreshold() {
		if (jTextFieldPValueThreshold == null) {
			jTextFieldPValueThreshold = new JTextField();
			jTextFieldPValueThreshold.setText(Float
					.toString(PValueThresholdDefault));
			jTextFieldPValueThreshold.setColumns(5);
			jTextFieldPValueThreshold.setMaximumSize(new Dimension(8,
					jTextFieldPValueThreshold.getHeight()));
			jTextFieldPValueThreshold
					.setToolTipText("This should be a float number between 0.0 and 1.0. After ANOVA analysis, only Markers have p-value less then this number will be returned.");
			jTextFieldPValueThreshold
					.addCaretListener(new javax.swing.event.CaretListener() {

						public void caretUpdate(javax.swing.event.CaretEvent e) {
							log.debug("P-value threshold changed to:"
									+ jTextFieldPValueThreshold.getText());
							if (jTextFieldPValueThreshold.getText().equals("")) {
								// caused by update through program
							} else {
								try {
									pValueThreshold = Float
											.valueOf(jTextFieldPValueThreshold
													.getText());
								} catch (NumberFormatException nfe) {

									JOptionPane.showMessageDialog(null,
											jTextFieldPValueThreshold
													.getToolTipText(),
											"Please try again.",
											JOptionPane.INFORMATION_MESSAGE);
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											jTextFieldPValueThreshold
													.setText("0.05");
										}
									});
								}
							}
						}
					});
		}
		return jTextFieldPValueThreshold;
	}

	/***************************************************************************
	 * copy paste the GUI code from VE till here.
	 */

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AnovaAnalysisPanel application = new AnovaAnalysisPanel();
		application.setVisible(true);
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
		this.add(getParamPanel(), BorderLayout.CENTER);
		// setup some default values.
		pValueEstimation = PValueEstimation.fdistribution;
		falseDiscoveryRateControl = FalseDiscoveryRateControl.alpha;
		permutationsNumber = PermutationsNumberDefault;
		falseSignificantGenesLimit = 10.0f;
		pValueThreshold = PValueThresholdDefault;

		ParameterActionListener parameterActionListener = new ParameterActionListener(
				this);
		jComboBoxPValueBasedOn.addActionListener(parameterActionListener);
		permutationsNumberJTextField.addActionListener(parameterActionListener);
		permutationsNumberJTextField.addFocusListener(parameterActionListener);
		// FIXME: monitor the model stead of the radio button, otherwise, the
		// event will be sent twice and only process the first one.
		jRadioButton.addActionListener(parameterActionListener);
		jRadioButton1.addActionListener(parameterActionListener);
		jRadioButton2.addActionListener(parameterActionListener);
		jRadioButton3.addActionListener(parameterActionListener);
		jRadioButton4.addActionListener(parameterActionListener);
		jRadioButton5.addActionListener(parameterActionListener);
		jTextFieldNFSG.addActionListener(parameterActionListener);
		jTextFieldNFSG.addFocusListener(parameterActionListener);
		jTextFieldPFSG.addActionListener(parameterActionListener);
		jTextFieldPFSG.addFocusListener(parameterActionListener);
		jTextFieldPValueThreshold.addActionListener(parameterActionListener);
		jTextFieldPValueThreshold.addFocusListener(parameterActionListener);
	}

	/**
	 * This method fulfills the contract of the {@link VisualPlugin} interface.
	 * It returns the GUI component for this visual plugin.
	 */
	public Component getComponent() {
		// In this case, this object is also the GUI component.
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters
	 * (java.util.Map) Set inputed parameters to GUI.
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
			if (key.equals("FalseDiscoveryRateControl")) {
				falseDiscoveryRateControl = (FalseDiscoveryRateControl) value;
			}
			if (key.equals("FalseSignificantGenesLimit")) {
				falseSignificantGenesLimit = (Float) value;
			}
			if (key.equals("PermutationsNumber")) {
				permutationsNumber = (Integer) value;
			}
			if (key.equals("PValueEstimation")) {
				pValueEstimation = (PValueEstimation) value;
			}
			if (key.equals("PValueThreshold")) {
				pValueThreshold = (Float) value;
			}
		}
		// start translate anovaParameter to AnovaAnalysisPanel by
		// manipulate GUI according to anovaParameter
		this.jTextFieldPValueThreshold.setText(Float.toString(pValueThreshold));
		if (pValueEstimation.equals(PValueEstimation.permutation)) {
			this.jComboBoxPValueBasedOn.setSelectedItem("Permutations");
		} else {
			this.jComboBoxPValueBasedOn.setSelectedItem("F-distribution");
		}
		this.permutationsNumberJTextField.setText(Integer
				.toString(permutationsNumber));

		if (falseDiscoveryRateControl.equals(FalseDiscoveryRateControl.alpha)) {
			this.jRadioButton.setSelected(true);
		} else if (falseDiscoveryRateControl
				.equals(FalseDiscoveryRateControl.bonferroni)) {
			this.jRadioButton1.setSelected(true);
		} else if (falseDiscoveryRateControl
				.equals(FalseDiscoveryRateControl.adjbonferroni)) {
			this.jRadioButton2.setSelected(true);
		} else if (falseDiscoveryRateControl
				.equals(FalseDiscoveryRateControl.westfallyoung)) {
			this.jRadioButton3.setSelected(true);
		} else if (falseDiscoveryRateControl
				.equals(FalseDiscoveryRateControl.number)) {
			this.jRadioButton4.setSelected(true);
			this.jTextFieldNFSG.setText(falseSignificantGenesLimit.toString());
		} else if (falseDiscoveryRateControl
				.equals(FalseDiscoveryRateControl.proportion)) {
			this.jRadioButton5.setSelected(true);
			this.jTextFieldPFSG.setText(falseSignificantGenesLimit.toString());
		}
		stopNotifyAnalysisPanelTemporary(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 * Since HierClustPanel only has three parameters, we return metric,
	 * dimension and method in the format same as getBisonParameters().
	 */
	public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		// FIXME: needs error checking
		if (jRadioButton.isSelected()) {
			parameters.put("FalseDiscoveryRateControl",
					FalseDiscoveryRateControl.alpha);
		}
		;
		if (jRadioButton1.isSelected()) {
			parameters.put("FalseDiscoveryRateControl",
					FalseDiscoveryRateControl.bonferroni);
		}
		;
		if (jRadioButton2.isSelected()) {
			parameters.put("FalseDiscoveryRateControl",
					FalseDiscoveryRateControl.adjbonferroni);
		}
		;
		if (jRadioButton3.isSelected()) {
			parameters.put("FalseDiscoveryRateControl",
					FalseDiscoveryRateControl.westfallyoung);
		}
		;
		if (jRadioButton4.isSelected()) {
			parameters.put("FalseDiscoveryRateControl",
					FalseDiscoveryRateControl.number);
			parameters.put("FalseSignificantGenesLimit",
					Float.valueOf(jTextFieldNFSG.getText()));
		}
		;
		if (jRadioButton5.isSelected()) {
			parameters.put("FalseDiscoveryRateControl",
					FalseDiscoveryRateControl.proportion);
			parameters.put("FalseSignificantGenesLimit",
					Float.valueOf(jTextFieldPFSG.getText()));
		}
		;
		if (jComboBoxPValueBasedOn.getSelectedItem().equals("F-distribution")) {
			parameters.put("PValueEstimation", PValueEstimation.fdistribution);
		}
		if (jComboBoxPValueBasedOn.getSelectedItem().equals("Permutations")) {
			parameters.put("PValueEstimation", PValueEstimation.permutation);
			parameters.put("PermutationsNumber",
					Integer.valueOf(permutationsNumberJTextField.getText()));
		}
		parameters.put("PValueThreshold",
				Float.parseFloat(jTextFieldPValueThreshold.getText()));
		return parameters;
	}

	@Override
	public String getDataSetHistory() {
		StringBuilder histStr = new StringBuilder("ANOVA parameters:\n");
		histStr.append("----------------------------------------\n");
		// P Value Estimation
		histStr.append("P Value estimation: ");
		if (pValueEstimation == PValueEstimation.permutation) {
			histStr.append("Permutation\n");
			histStr.append("Permutation#: ").append(permutationsNumber)
					.append("\n");
		} else {
			histStr.append("F-Distribution\n");
		}
		// P Value threshold
		histStr.append("P Value threshold: ");
		histStr.append(pValueThreshold).append("\n");

		// Correction type
		histStr.append("Correction-method: ");
		histStr.append(falseDiscoveryRateControl.toString()).append("\n");
		if (falseDiscoveryRateControl == FalseDiscoveryRateControl.number) {
			histStr.append("\tFalse Significant Genes limits: ")
					.append(falseSignificantGenesLimit.intValue()).append("\n");
		} else if (falseDiscoveryRateControl == FalseDiscoveryRateControl.proportion) {
			histStr.append("\tFalse Significant Genes proportion limits: ")
					.append(falseSignificantGenesLimit).append("\n");
		}

		return histStr.toString();
	}

	@Override
	public ParamValidationResults validateParameters() {
		if ((pValueThreshold < 0) || (pValueThreshold > 1)) {
			return new ParamValidationResults(false,
					"P-Value threshold should be a float number between 0.0 and 1.0.");
		} else if (falseDiscoveryRateControl
				.equals(FalseDiscoveryRateControl.proportion)) {
			if ((falseSignificantGenesLimit < 0)
					|| (falseSignificantGenesLimit > 1)) {
				return new ParamValidationResults(false,
						"Proportion should be a float number between 0.0 and 1.0.");
			}
		}
		return new ParamValidationResults(true, "No Error");
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		if (parameters.get("PValueEstimation") == null)
			parameters.put("PValueEstimation", PValueEstimation.fdistribution);
		if (parameters.get("FalseDiscoveryRateControl") == null)
			parameters.put("FalseDiscoveryRateControl",
					FalseDiscoveryRateControl.alpha);
		if (parameters.get("PValueEstimation") == PValueEstimation.permutation)
			if (parameters.get("PermutationsNumber") == null)
				parameters.put("PermutationsNumber", PermutationsNumberDefault);
		if (parameters.get("PValueThreshold") == null)
			parameters.put("PValueThreshold", PValueThresholdDefault);
		if (parameters.get("FalseDiscoveryRateControl") == FalseDiscoveryRateControl.number)
			if (parameters.get("FalseSignificantGenesLimit") == null)
				parameters.put("FalseSignificantGenesLimit", 10.0f);
		if (parameters.get("FalseDiscoveryRateControl") == FalseDiscoveryRateControl.proportion)
			if (parameters.get("FalseSignificantGenesLimit") == null)
				parameters.put("FalseSignificantGenesLimit", 0.05f);
	}
}
