package org.geworkbench.components.cagrid;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.geworkbench.util.Util;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.columbia.geworkbench.cagrid.cluster.som.SomClusteringParameter;

/**
 * @author keshav
 * @version $Id: GridSomClusteringDialog.java,v 1.1 2007-03-15 20:11:21 keshav Exp $
 */
public class GridSomClusteringDialog extends JDialog {

    private SomClusteringParameter somClusteringParameters = null;

    /**
     * @throws HeadlessException
     */
    public GridSomClusteringDialog() throws HeadlessException {
    	FormLayout layout = new FormLayout( "right:max(40dlu;pref), 3dlu, 100dlu, 7dlu", "" );
        DefaultFormBuilder builder = new DefaultFormBuilder( layout );
        builder.setDefaultDialogBorder();
        builder.appendSeparator("Som Clustering Parameters");


        final JTextField dimxField = new JTextField("3");
		final JTextField dimyField = new JTextField("3");
		final JTextField functionField = new JTextField("1");
		final JTextField radiusField = new JTextField("3.0");
		final JTextField alphaField = new JTextField("0.08");
		final JTextField iterationsField = new JTextField("4000");
		
		/* append all the fields to the builder */
		builder.append("dim x", dimxField);
		builder.append("dim y", dimyField);
		builder.append("function", functionField);
		builder.append("radius", radiusField);
		builder.append("alpha", alphaField);
		builder.append("iteration", iterationsField);
		
        JPanel dialogPanel = builder.getPanel();
        JPanel buttonPanel = new JPanel( new FlowLayout() );
        JButton okButton = new JButton( "OK" );
        okButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
            	int dimx;

    			int dimy;

    			int function;

    			float radius;

    			float alpha;

    			int iterations;
    			
    			somClusteringParameters = new SomClusteringParameter();
    			
				dimx = Integer.parseInt(dimxField.getText());
				dimy = Integer.parseInt(dimyField.getText());
				function = Integer.parseInt(functionField.getText());
				radius = Float.parseFloat(radiusField.getText());
				alpha = Float.parseFloat(alphaField.getText());
				iterations = Integer.parseInt(iterationsField.getText());

				somClusteringParameters.setDim_x(dimx);
				somClusteringParameters.setDim_y(dimy);
				somClusteringParameters.setFunction(function);
				somClusteringParameters.setRadius(radius);
				somClusteringParameters.setAlpha(alpha);
				somClusteringParameters.setIteration(iterations);
				
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
    public SomClusteringParameter getParameters() {
        pack();
        setSize( 300, 250 );
        Util.centerWindow( this );
        setVisible( true );
        return somClusteringParameters;
    }

}
