package org.geworkbench.components.alignment.panels;

import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.engine.management.Subscribe;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.Vector;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author XZ
 * @version 1.0
 */

public class SyntenyViewPanel extends JPanel {
    JPanel syntenyResult = new JPanel();
    JPanel detailedInfo = new JPanel();
    JPanel furtherProcess = new JPanel();
    JButton HMMButton = new JButton();
    JButton resetButton = new JButton();
    JTextArea detailTextArea = new JTextArea();
    private Vector hits;
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    BorderLayout borderLayout1 = new BorderLayout();
    JScrollPane jScrollPane1 = new JScrollPane();
    BorderLayout borderLayout2 = new BorderLayout();
    SyntenyViewComponent sViewComponent;

    public void setSyntenyViewComponent(SyntenyViewComponent sc) {
        sViewComponent = sc;
    }

    public SyntenyViewPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe public void receive(ProjectNodeAddedEvent pnae, Object publisher) {
        /**@todo Implement this medusa.components.listeners.ProjectNodeAddedListener method*/
        throw new java.lang.UnsupportedOperationException("Method projectNodeAdded() not yet implemented.");
    }

    /**
     * Creates a new <code>JPanel</code> with <code>FlowLayout</code> and the
     * specified buffering strategy.
     *
     * @param isDoubleBuffered a boolean, true for double-buffering, which uses
     *                         additional memory space to achieve fast, flicker-free updates
     * @todo Implement this javax.swing.JPanel constructor
     */
    public SyntenyViewPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    /**
     * @param layout the LayoutManager to use
     * @todo Implement this javax.swing.JPanel constructor
     */
    public SyntenyViewPanel(LayoutManager layout) {
        super(layout);
    }

    /**
     * Creates a new JPanel with the specified layout manager and buffering
     * strategy.
     *
     * @param layout           the LayoutManager to use
     * @param isDoubleBuffered a boolean, true for double-buffering, which uses
     *                         additional memory space to achieve fast, flicker-free updates
     * @todo Implement this javax.swing.JPanel constructor
     */
    public SyntenyViewPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    private void jbInit() throws Exception {
        this.setLayout(gridBagLayout2);
        syntenyResult.setLayout(borderLayout1);
        resetButton.setText("Reset");
        detailTextArea.setText("Dot Matrix panel");
        syntenyResult.setBorder(BorderFactory.createRaisedBevelBorder());
        furtherProcess.setBorder(BorderFactory.createLoweredBevelBorder());
        detailedInfo.setLayout(borderLayout2);
        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(detailedInfo, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 1, 0, 2), 449, 86));
        detailedInfo.add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(detailTextArea, null);
        this.add(furtherProcess, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 1, 2, 2), 367, 15));
        furtherProcess.add(resetButton, null);
        furtherProcess.add(HMMButton, null);
        this.add(syntenyResult, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 1, 0, 2), 527, 217));
    }

    /**
     * getResults
     *
     * @param blastAppComponent Object
     * @return Object[]
     */
    public void setResults(Vector hits) {
        this.hits = hits;
        displayResults();
    }

    public void displayResults() {
        System.out.println("displaying...");
        syntenyResult.removeAll();
        syntenyResult.repaint();
        this.repaint();
        System.out.println("end displaying...");
    }

    public void resetToWhite() {
        System.out.println("Enter resetTowhite.");
        syntenyResult.removeAll();
        detailTextArea.setRows(3);
        detailTextArea.setText("View panel");
        syntenyResult.repaint();
        this.repaint();
    }

    private void showAlignment() {
        String text;
        text = "Just a test here";
        detailTextArea.setText(text);
    }

    private class listSelectionListener implements ListSelectionListener {
        int selectedRow;

        public void valueChanged(ListSelectionEvent e) {
            //Ignore extra messages.
            if (e.getValueIsAdjusting())
                return;
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty()) {
            } else {
                selectedRow = lsm.getMinSelectionIndex();
                showAlignment();
            }
        }
    }


    /**
     * This class extends AbstractTableModel and creates a table view of Blast
     * results.
     */

    /*returns if the cell is editable; returns false for all cells in columns except column 6*/
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col < 6) {
            return false;
        } else {
            return true;
        }
    }

    /*detect change in cell at (row, col); set cell to value; update the table */

}
