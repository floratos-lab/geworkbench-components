package org.geworkbench.components.cagrid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.DefaultFormBuilder;

import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusteringParameter;

import org.geworkbench.util.Util;

/**
 * @author keshav
 * @version $Id: GridSomClusteringDialog.java,v 1.1 2007-01-09 16:17:30 keshav Exp $
 */
public class GridSomClusteringDialog extends JDialog {

    private HierarchicalClusteringParameter parameters = null;

    /**
     * @throws HeadlessException
     */
    public GridSomClusteringDialog() throws HeadlessException {
        FormLayout layout = new FormLayout( "right:max(60dlu;pref), 3dlu, 100dlu, 7dlu", "" );
        DefaultFormBuilder builder = new DefaultFormBuilder( layout );
        builder.setDefaultDialogBorder();

        builder.appendSeparator( "Hierarchical Clustering Parameters" );
        final JComboBox methodBox = new JComboBox( new String[] { "Single", "Average", "Total" } );
        final JComboBox dimBox = new JComboBox( new String[] { "Marker", "Microarray", "Both" } );
        final JComboBox distanceBox = new JComboBox( new String[] { "Euclidean", "Pearson", "Spearman" } );
        builder.append( "Method", methodBox );
        builder.append( "Dimension", dimBox );
        builder.append( "Distance", distanceBox );
        JPanel dialogPanel = builder.getPanel();
        JPanel buttonPanel = new JPanel( new FlowLayout() );
        JButton okButton = new JButton( "OK" );
        okButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                parameters = new HierarchicalClusteringParameter( ( String ) dimBox.getSelectedItem(),
                        ( String ) distanceBox.getSelectedItem(), ( String ) methodBox.getSelectedItem() );
                dispose();
            }
        } );
        JButton cancelButton = new JButton( "Cancel" );
        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                dispose();
            }
        } );
        buttonPanel.add( okButton );
        buttonPanel.add( cancelButton );
        JPanel mainPanel = new JPanel( new BorderLayout() );
        mainPanel.add( dialogPanel, BorderLayout.CENTER );
        mainPanel.add( buttonPanel, BorderLayout.SOUTH );
        getContentPane().add( mainPanel );
        setModal( true );
    }

    /**
     * @return HierarchicalClusteringParameter
     */
    public HierarchicalClusteringParameter getParameters() {
        pack();
        setSize( 300, 200 );
        Util.centerWindow( this );
        setVisible( true );
        return parameters;
    }

}
