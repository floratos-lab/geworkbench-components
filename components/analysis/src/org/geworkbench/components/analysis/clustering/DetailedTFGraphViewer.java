package org.geworkbench.components.analysis.clustering;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JPanel;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMasterRagulatorResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;

public class DetailedTFGraphViewer extends JPanel {
	DSMasterRagulatorResultSet mraResultSet;
	DSGeneMarker tfA;
	DSItemList targetGenes;
	DSSignificanceResultSet<DSGeneMarker> sigSet;
	int numberOfSigMarkers;
	HashMap<Integer,DSGeneMarker> Rank2GeneMap;
	HashMap<DSGeneMarker, Integer> Gene2RankMap;
	public void setTFA(DSMasterRagulatorResultSet mraResultSet, DSGeneMarker tfA){
		this.mraResultSet = mraResultSet;
		this.tfA = tfA;
		targetGenes = new CSItemList();
		if (mraResultSet!=null){
			sigSet=mraResultSet.getSignificanceResultSet();
			sigSet.sortMarkersBySignificance();
			numberOfSigMarkers = sigSet.getSignificantMarkers().size();
		}else{
			sigSet = null;
			numberOfSigMarkers = 0;
		}
		
		//hash map to store rank of genes.
		Rank2GeneMap = new HashMap<Integer,DSGeneMarker>();
		Gene2RankMap = new HashMap<DSGeneMarker,Integer>();
		for (int cx=0; cx<numberOfSigMarkers; cx++){
			Rank2GeneMap.put(new Integer(cx),(DSGeneMarker)sigSet.getSignificantMarkers().get(cx));
			Gene2RankMap.put(((DSGeneMarker)sigSet.getSignificantMarkers().get(cx)),new Integer(cx));
		}
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		g.setColor(Color.red);
		int width = this.getWidth();
		int height = this.getHeight();
		int numMarkers = numberOfSigMarkers;
		g.drawRect(0, 0, width, height);
		if ((numberOfSigMarkers>0) && (mraResultSet!=null)){
			//draw the color sections
			for(int i=0;i<numMarkers;i++){
				int disToMid = Math.abs((int)((numMarkers)/2-i));
				int colorindex = (255*2*disToMid/numMarkers);
				if (colorindex<0)colorindex = 0;
				if (colorindex>255)colorindex = 255;
				if (i<=numMarkers/2){
					g.setColor(new Color(255,255-colorindex,255-colorindex));
				}else{
					g.setColor(new Color(255-colorindex,255-colorindex,255));
				}
				g.fillRect((int)(i*width/numMarkers), height*2/3, (int)(width/numMarkers)+1, height/3);
			}
			
			//draw the lines for geneMarkers
			int sectionWidth = width/numMarkers;
			for(int i=0;i<numMarkers;i++){
				int geneNum = i;
				int left = sectionWidth*(geneNum);
				int right = sectionWidth*(geneNum+1);
				int center = (left+right)/2;
				DSItemList genesInTargetList = mraResultSet.getGenesInTargetList(tfA);
				for (int cx = 0; cx < genesInTargetList.size(); cx++){
					if (Gene2RankMap.get(genesInTargetList.get(cx)).intValue()==i){
						g.drawLine(center, 0, center, height*2/3);
					}
				}
			}
		}
	}
}
