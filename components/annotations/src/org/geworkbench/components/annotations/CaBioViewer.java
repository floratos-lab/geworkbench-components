package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.Evidence;
import gov.nih.nci.cabio.domain.Gene;
import gov.nih.nci.cabio.domain.GeneDiseaseAssociation;
import gov.nih.nci.cabio.domain.GeneFunctionAssociation;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.ApplicationService;
import gov.nih.nci.system.client.ApplicationServiceProvider;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.components.anova.gui.TableViewer;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ProjectEvent;
@AcceptTypes({DSMicroarraySet.class})
public class CaBioViewer extends JPanel implements VisualPlugin {
	private Log log = LogFactory.getLog(this.getClass());
	private DSMicroarraySet maSet = null;
	JPanel panel = new JPanel();
	TableViewer tv = new TableViewer();

	public CaBioViewer(){
		this.setLayout(new BorderLayout());
		//add a space on top and add a button "Display Preference" on the right.		
		panel = new JPanel();
		panel.add(tv);
		panel.setLayout(new BorderLayout());
		add(panel,java.awt.BorderLayout.NORTH);
	}
	public Component getComponent() {
		// TODO Auto-generated method stub
		return this;
	}

	/**
	 * Receives GeneSelectorEvent from the framework. Extracts markers in
	 * the selected marker sets from the Selector Panel.
	 * 
	 * @param e -
	 *            GeneSelectorEvent
	 * @param source -
	 *            source of the GeneSelectorEvent
	 */
	@SuppressWarnings("unchecked")
	@Subscribe
	public void receive(GeneSelectorEvent e, Object source) {
		log.error("***Received gene selector event ");
		if (false) return;
		if (maSet != null) {
			List<DSGeneMarker> selectedMarkers = null;
			if (e.getPanel() == null)
				log
						.debug("Received Gene Selector Event: Selection panel sent was null");

			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(
					maSet);
			maView.setMarkerPanel(e.getPanel());
			maView.useMarkerPanel(true);
			if ((maView.getMarkerPanel() != null)
					&& (maView.getMarkerPanel().activeSubset() != null)
					&& (maView.getMarkerPanel().activeSubset().size() == 0)) {
				selectedMarkers = null;
			} else {
				try {
					if ((maView != null) && (maView.getUniqueMarkers() != null)) {
						DSItemList<DSGeneMarker> uniqueMarkers = maView
								.getUniqueMarkers();
						if (uniqueMarkers.size() > 0) {
							selectedMarkers = (List<DSGeneMarker>) uniqueMarkers;
							Object[][] table = new Object[uniqueMarkers.size()][2];
							String[] headerNames = new String[2];
							headerNames[0]="Marker";
							headerNames[1]="Info";
							int cx = 0;
							for (Iterator iterator = selectedMarkers.iterator(); iterator
									.hasNext();) {
								DSGeneMarker marker = (DSGeneMarker) iterator.next();
								log.error("marker "+marker.getLabel()+" selected");
								table[cx][0]=marker.getLabel();
								table[cx++][1]=marker.getGeneName();
							}
							panel.remove(tv);
							
							//tv = new TableViewer(headerNames, table);
							tv = getDiseaseForMarkers(selectedMarkers);
							tv.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
							tv.revalidate();
							panel.add(tv,java.awt.BorderLayout.NORTH);
							panel.revalidate();
							tv.updateUI();

							//testGetDiseaseForMarker();
							
						}
					}
				} catch (NullPointerException npe) {
					selectedMarkers = null;
					log.debug("Gene Selector Event contained no marker data.");
				}
			}
		}
	}
	
	/**
	 * Receives the general ProjectEvent from the framework. Creates MINDY's
	 * data set based on data from the ProjectEvent.
	 * 
	 * @param projectEvent
	 * @param source -
	 *            source of the ProjectEvent
	 */
	@Subscribe
	public void receive(ProjectEvent projectEvent, Object source) {
		DSDataSet data = projectEvent.getDataSet();
		if (data instanceof DSMicroarraySet<?>)
			maSet = (DSMicroarraySet<?>)data;
	}
	
	public final void testGetDiseaseForMarker() {
		ApplicationService appService = null;
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			appService = ApplicationServiceProvider.getApplicationService();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* this example use marker 31310_at */
		DSGeneMarker marker = (DSGeneMarker)maSet.getMarkers().get("31310_at");

		/* String geneSymbol = "GLRA1"; */
		String geneSymbol = marker.getLabel();
		geneSymbol = marker.getGeneName();
		//int uniGeneId = marker.getUnigene().getUnigeneId();
		Gene gene = new Gene();
		gene.setSymbol(geneSymbol);

		/* Get Agents for "GLRA1" */
		List<Object> results2 = null;
		try {
			results2 = appService.search(GeneFunctionAssociation.class, gene);
		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("\nAgents associated with Gene: " + geneSymbol);
		for (Object gfa : results2) {
			if (gfa instanceof GeneDiseaseAssociation) {
				GeneDiseaseAssociation gda = (GeneDiseaseAssociation) gfa;
				System.out.println("  Disease: " + gda.getDiseaseOntology().getName());
				System.out.println("    Role: " + gda.getRole());
				Evidence e = gda.getEvidence();
				System.out.println("    Sentence: "+e.getSentence());
				System.out.println("    PubmedId:"+e.getPubmedId());
			}
		}
	}

	public TableViewer getDiseaseForMarkers(List<DSGeneMarker> selectedMarkers) {
		ApplicationService appService = null;
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			appService = ApplicationServiceProvider.getApplicationService();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* this example use marker 31310_at */
		DSGeneMarker marker = (DSGeneMarker)maSet.getMarkers().get("31310_at");

		/* String geneSymbol = "GLRA1"; */
		String geneSymbol = marker.getLabel();
		geneSymbol = marker.getGeneName();
		//int uniGeneId = marker.getUnigene().getUnigeneId();
		Gene gene = new Gene();
		gene.setSymbol(geneSymbol);

		/* Get Agents for "GLRA1" */
		List<Object> results2 = null;
		try {
			results2 = appService.search(GeneFunctionAssociation.class, gene);
		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("\nAgents associated with Gene: " + geneSymbol);
		for (Object gfa : results2) {
			if (gfa instanceof GeneDiseaseAssociation) {
				GeneDiseaseAssociation gda = (GeneDiseaseAssociation) gfa;
				System.out.println("  Disease: " + gda.getDiseaseOntology().getName());
				System.out.println("    Role: " + gda.getRole());
				Evidence e = gda.getEvidence();
				System.out.println("    Sentence: "+e.getSentence());
				System.out.println("    PubmedId:"+e.getPubmedId());
			}
		}
		String[] headerNames = new String[4];
		headerNames[0]="Role";
		headerNames[1]="Disease";
		headerNames[2]="Sentence";
		headerNames[3]="PubmedId";
		Object[][] table = new Object[results2.size()][4];
		int cx=0;
		for (Object gfa : results2) {
			if (gfa instanceof GeneDiseaseAssociation) {
				GeneDiseaseAssociation gda = (GeneDiseaseAssociation) gfa;
				table[cx][0]=gda.getRole();
				table[cx][1]=gda.getDiseaseOntology().getName();
				Evidence e = gda.getEvidence();
				System.out.println("    Sentence: "+e.getSentence());
				System.out.println("    PubmedId:"+e.getPubmedId());
				table[cx][2]=e.getSentence();
				table[cx][3]=Integer.toString(e.getPubmedId());
				cx++;
			}
		}
		return new TableViewer(headerNames, table);
	}

}
