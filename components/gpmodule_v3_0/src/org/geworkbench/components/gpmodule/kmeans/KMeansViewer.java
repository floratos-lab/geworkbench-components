package org.geworkbench.components.gpmodule.kmeans;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.KMeansResult;
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
@AcceptTypes({ KMeansResult.class })
public class KMeansViewer extends JPanel implements VisualPlugin {

	private static final long serialVersionUID = 8763573636321803637L;
	private String resultText="see result here!";
	private JTextArea textBox=null;
	private JScrollPane textPanel=null;

	public KMeansViewer() {		
		textBox=new JTextArea();
		textBox.setText(resultText);
		textPanel = new JScrollPane(textBox);

		//aLabel.setFont(new Font("Serif", Font.BOLD, 18));

		setLayout(new BorderLayout());
		add(textPanel, BorderLayout.CENTER);
	}

	private KMeansResult kmResult=null;
	
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<?> dataSet = event.getDataSet();
		if (dataSet instanceof KMeansResult) {
			kmResult=(KMeansResult) dataSet;
			resultText=kmResult.getResultText();
			textBox.setText(resultText);
		}
	}

	public Component getComponent() {
		return this;
	}

}
