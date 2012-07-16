package org.geworkbench.components.gpmodule.classification;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.SelectorResult;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

/**

 * @author emj2132
 * @version $Id:$
 * 
 */
@AcceptTypes({ SelectorResult.class })
public class SelectorResultViewer extends JPanel implements VisualPlugin {

	private static final long serialVersionUID = -3855001518756439786L;
	JLabel aLabel;

	public SelectorResultViewer() {		
		aLabel=new JLabel(" Please see results in Arrays/Phenotypes tab at Selector Panel (bottom left)!");
		aLabel.setFont(new Font("Serif", Font.BOLD, 18));

		setLayout(new BorderLayout());
		add(aLabel, BorderLayout.CENTER);
	}

	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<?> dataSet = event.getDataSet();
		if (dataSet instanceof SelectorResult) {
			SelectorResult a = (SelectorResult)dataSet;
			if(!(a.getText().equals(""))){
			aLabel.setText(a.getText());
			}
		}
	}

	public Component getComponent() {
		return this;
	}

}
