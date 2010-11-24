package org.geworkbench.components.idea;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;

/**
 * AnnotationParser is part of stand alone version of IDEA analysis. The class is copied and modified from
 * /geworkbench-core/src/org/geworkbench/bison/datastructure/bioobjects/markers/annotationparser/AnnotationParser.java 
 * 
 * @author zm2165
 * @version $id$	
 */
public class AnnotationParser implements Serializable{

	private static final long serialVersionUID = -2389772520085356226L;

	public static final String GENE_ONTOLOGY_BIOLOGICAL_PROCESS = "Gene Ontology Biological Process";

	public static final String GENE_ONTOLOGY_CELLULAR_COMPONENT = "Gene Ontology Cellular Component";

	public static final String GENE_ONTOLOGY_MOLECULAR_FUNCTION = "Gene Ontology Molecular Function";

	public static final String GENE_SYMBOL = "Gene Symbol";

	public static final String PROBE_SET_ID = "Probe Set ID";

	public static final String MAIN_DELIMITER = "///";

	// field names
	public static final String DESCRIPTION = "Gene Title"; // (full name)

	public static final String ABREV = GENE_SYMBOL; // title(short name)

	public static final String PATHWAY = "Pathway"; // pathway

	public static final String GOTERM = GENE_ONTOLOGY_BIOLOGICAL_PROCESS; // Goterms

	public static final String UNIGENE = "UniGene ID"; // Unigene

	public static final String UNIGENE_CLUSTER = "Archival UniGene Cluster";

	public static final String LOCUSLINK = "Entrez Gene"; // LocusLink

	public static final String SWISSPROT = "SwissProt"; // swissprot
	
	public static final String CHROMOSOMAL ="Chromosomal Location";

	public static final String REFSEQ = "RefSeq Transcript ID"; // RefSeq

	public static final String TRANSCRIPT = "Transcript Assignments";

	public static final String SCIENTIFIC_NAME = "Species Scientific Name";

	public static final String GENOME_VERSION = "Genome Version";

	public static final String ALIGNMENT = "Alignments";

	// columns read into geWorkbench
	// probe id must be first column read in, and the rest of the columns must
	// follow the same order
	// as the columns in the annotation file.
	private static final String[] labels = {
			PROBE_SET_ID // probe id must be the first item in this list
			, SCIENTIFIC_NAME, UNIGENE_CLUSTER, UNIGENE, GENOME_VERSION,
			ALIGNMENT, DESCRIPTION, GENE_SYMBOL, LOCUSLINK, SWISSPROT, CHROMOSOMAL, REFSEQ,
			GENE_ONTOLOGY_BIOLOGICAL_PROCESS, GENE_ONTOLOGY_CELLULAR_COMPONENT,
			GENE_ONTOLOGY_MOLECULAR_FUNCTION, PATHWAY, TRANSCRIPT };

	// TODO all the DSDataSets handled in this class should be DSMicroarraySet
	// FIELDS
	

	/* The reason that we need APSerializable is that the status fields are designed as static. */
	private static Map<String, MarkerAnnotation> chipTypeToAnnotation = new TreeMap<String, MarkerAnnotation>();
	public static boolean processAnnotationData(String chipType, File datafile,TreeSet<Gene> preGeneList,Map<String,String> probe_chromosomal) {
		if (datafile.exists()) { // data file is found
			
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			
			try {
				fis = new FileInputStream(datafile);
				bis = new BufferedInputStream(fis);
				
				//String midLog="c:\\idea\\output\\midLog.txt";
				//PrintWriter preout = new PrintWriter(midLog);
				
				CSVParser cvsParser = new CSVParser(bis);

				cvsParser.setCommentStart("#;!");// Skip all comments line.
													// XQ. The bug is reported
													// by Bernd.

				LabeledCSVParser parser = new LabeledCSVParser(cvsParser);

				MarkerAnnotation markerAnnotation = new MarkerAnnotation();
				
				while (parser.getLine() != null) {
					String affyId = parser.getValueByLabel(labels[0]);
					AnnotationFields fields = new AnnotationFields();
					for (int i = 1; i < labels.length; i++) {
						String label = labels[i];
						String val = parser.getValueByLabel(label);
						if (label.equals(GENE_ONTOLOGY_BIOLOGICAL_PROCESS)
								|| label
										.equals(GENE_ONTOLOGY_CELLULAR_COMPONENT)
								|| label
										.equals(GENE_ONTOLOGY_MOLECULAR_FUNCTION)) {
							// get rid of leading 0's
							while (val.startsWith("0") && (val.length() > 0)) {
								val = val.substring(1);
							}
						}
						if (label.equals(GENE_SYMBOL))
							fields.setGeneSymbol(val);
						else if (label.equals(LOCUSLINK))
							fields.setLocusLink(val);
						else if (label.equals(SWISSPROT))
							fields.setSwissProt(val);
						else if (label.equals(CHROMOSOMAL))
							fields.setChromosomal(val);
						else if (label.equals(DESCRIPTION))
							fields.setDescription(val);
						else if (label.equals(GENE_ONTOLOGY_MOLECULAR_FUNCTION))
							fields.setMolecularFunction(val);
						else if (label.equals(GENE_ONTOLOGY_CELLULAR_COMPONENT))
							fields.setCellularComponent(val);
						else if (label.equals(GENE_ONTOLOGY_BIOLOGICAL_PROCESS))
							fields.setBiologicalProcess(val);
						else if(label.equals(UNIGENE))
							fields.setUniGene(val);
						else if(label.equals(REFSEQ))
							fields.setRefSeq(val);
					}
					markerAnnotation.addMarker(affyId, fields);
					
					try{
						int entrezGene=Integer.parseInt(fields.getLocusLink());						
						Iterator<Gene> iterGene=preGeneList.iterator();						
						while(iterGene.hasNext()){
							Gene g=iterGene.next();
							if (g.getGeneNo()==entrezGene){
								String s=g.getProbeIds();
								s+=affyId+"\t";
								g.setProbeIds(s);
								probe_chromosomal.put(affyId,fields.getChromosomal());
							}
							
						}
						
						
						
					}
					catch(Exception e){
						//System.out.println(fields.getLocusLink()+":"+affyId);
					}
					
					//preout.println(fields.getLocusLink()+":"+affyId);
					//System.out.println(fields.getLocusLink()+":"+affyId);
				}

				chipTypeToAnnotation.put(chipType, markerAnnotation);
				//preout.close();//should do this
				return true;
			} catch (Exception e) {
//				log.error("", e);	//zheng
				return false;
			}finally{
				try {
					fis.close();
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		} else {
			return false;
		}
		
	}
	
	
	public static void openURL(String url) {
		String osName = System.getProperty("os.name");
		try {
			if (osName.startsWith("Mac OS")) {
				Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL",
						new Class<?>[] { String.class });
				openURL.invoke(null, new Object[] { url });
			} else if (osName.startsWith("Windows")) {
				Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler " + url);
			} else { // assume Unix or Linux
				String[] browsers = { "firefox", "opera", "konqueror",
						"epiphany", "mozilla", "netscape" };
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++)
					if (Runtime.getRuntime().exec(
							new String[] { "which", browsers[count] })
							.waitFor() == 0)
						browser = browsers[count];
				if (browser == null)
					throw new Exception("Could not find web browser");
				else
					Runtime.getRuntime().exec(new String[] { browser, url });
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Unable to open browser"
					+ ":\n" + e.getLocalizedMessage());
		}
	}

	
	static private class AnnotationFields implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4492610936907933126L;
		/**
		 * 
		 */
		
		
		void setMolecularFunction(String molecularFunction) {
		}

		void setCellularComponent(String cellularComponent) {
		}

		void setBiologicalProcess(String biologicalProcess) {
		}

		void setUniGene(String uniGene) {
		}

		void setDescription(String description) {
		}

		void setGeneSymbol(String geneSymbol) {
		}

		String getLocusLink() {
			return locusLink;
		}
		

		void setLocusLink(String locusLink) {
			this.locusLink = locusLink;
		}

		void setSwissProt(String swissProt) {
		}
		
		void setChromosomal(String chromosomal) {
			this.chromosomal=chromosomal;
		}
		String getChromosomal(){
			return chromosomal;
		}

		public void setRefSeq(String refSeq) {
		}

		private String locusLink;
		private String chromosomal;
	}
	
	static class MarkerAnnotation implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4185484725864363279L;
		/**
		 * 
		 */
		
		
		private Map<String, AnnotationFields> annotationFields;
		
		MarkerAnnotation() {
			annotationFields = new TreeMap<String, AnnotationFields>();
		}
		
		void addMarker(String marker, AnnotationFields fields) {
			annotationFields.put(marker, fields);
		}
		
		AnnotationFields getFields(String marker) {
			return annotationFields.get(marker);
		}
		
		Set<String> getMarkerSet() {
			return annotationFields.keySet();
		}
	}

}


