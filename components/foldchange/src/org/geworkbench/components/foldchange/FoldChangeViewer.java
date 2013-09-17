package org.geworkbench.components.foldchange;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.FoldChangeResult;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

/**
 * FoldChangeViewer of FoldChange analysis component
 * 
 * @author zm2165
 * @version $Id:$
 * 
 */
@AcceptTypes({ FoldChangeResult.class })
public class FoldChangeViewer extends JPanel implements VisualPlugin {

	private static final long serialVersionUID = -3855001518756439786L;	

	public FoldChangeViewer() {		
		JLabel aLabel=new JLabel(" Please see results in Markers tab at Selector Panel (bottom left)!");
		aLabel.setFont(new Font("Serif", Font.BOLD, 18));

		setLayout(new BorderLayout());
		add(aLabel, BorderLayout.CENTER);
	}

	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<?> dataSet = event.getDataSet();
		if (dataSet instanceof FoldChangeResult) {			
		}
	}

	public Component getComponent() {
		return this;
	}

}
