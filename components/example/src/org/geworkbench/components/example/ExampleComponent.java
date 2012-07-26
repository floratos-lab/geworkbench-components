package org.geworkbench.components.example;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

/**
 * This is an example geWorkbench component.
 * 
 * @author John Watkinson
 */
// This annotation lists the data set types that this component accepts.
// The component will only appear when a data set of the appropriate type is
// selected.
@AcceptTypes({ DSMicroarraySet.class })
public class ExampleComponent extends JPanel implements VisualPlugin {

	private static final long serialVersionUID = -4565375519545129102L;

	private DSMicroarraySet microarraySet;
	private JLabel infoLabel = new JLabel("");

	public ExampleComponent() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(infoLabel);
		JButton testButton = new JButton("Test Annotation");
		testButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(ExampleComponent.this, "Annotation File "+microarraySet.getAnnotationFileName());
			}
		});
		add(testButton);
	}

	/**
	 * This method fulfills the contract of the {@link VisualPlugin} interface.
	 * It returns the GUI component for this visual plugin.
	 */
	public Component getComponent() {
		// In this case, this object is also the GUI component.
		return this;
	}

	/**
	 * This is a <b>Subscribe</b> method. The annotation before the method
	 * alerts the engine that it should route published objects to this method.
	 * The type of objects that are routed to this method are indicated by the
	 * first parameter of the method. In this case, it is {@link ProjectEvent}.
	 * 
	 * @param event
	 *            the received object.
	 * @param source
	 *            the entity that published the object.
	 */
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<?> dataSet = event.getDataSet();
		// We will act on this object if it is a DSMicroarraySet
		if (dataSet instanceof DSMicroarraySet) {
			microarraySet = (DSMicroarraySet) dataSet;
			// We just received a new microarray set, so populate the info label
			// with some basic stats.
			String htmlText = "<html><body align='center'>" + "<h3 align='center'>"
					+ microarraySet.getLabel() + "</h3>" + "<table>"
					+ "<tr><td>Arrays:</td><td><b>" + microarraySet.size()
					+ "</b></td></tr>" + "<tr><td>Markers:</td><td><b>"
					+ microarraySet.getMarkers().size() + "</b></td></tr>"
					+ "</table>" + "</body></html>";
			infoLabel.setText(htmlText);
		}
	}

}
