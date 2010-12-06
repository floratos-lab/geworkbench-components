package org.geworkbench.components.idea;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;

/**
 * 
 * @author zm2165
 * @version $Id$
 */
public class AnnotationParser implements Serializable {
	private static Log log = LogFactory.getLog(AnnotationParser.class);

	private static final long serialVersionUID = -2389772520085356226L;

	private static final String GENE_ONTOLOGY_BIOLOGICAL_PROCESS = "Gene Ontology Biological Process";

	private static final String GENE_ONTOLOGY_CELLULAR_COMPONENT = "Gene Ontology Cellular Component";

	private static final String GENE_ONTOLOGY_MOLECULAR_FUNCTION = "Gene Ontology Molecular Function";

	private static final String GENE_SYMBOL = "Gene Symbol";

	private static final String PROBE_SET_ID = "Probe Set ID";

	// field names
	private static final String DESCRIPTION = "Gene Title"; // (full name)

	private static final String PATHWAY = "Pathway"; // pathway

	private static final String UNIGENE = "UniGene ID"; // Unigene

	private static final String UNIGENE_CLUSTER = "Archival UniGene Cluster";

	private static final String LOCUSLINK = "Entrez Gene"; // LocusLink

	private static final String SWISSPROT = "SwissProt"; // swissprot

	private static final String CHROMOSOMAL = "Chromosomal Location";

	private static final String REFSEQ = "RefSeq Transcript ID"; // RefSeq

	private static final String TRANSCRIPT = "Transcript Assignments";

	private static final String SCIENTIFIC_NAME = "Species Scientific Name";

	private static final String GENOME_VERSION = "Genome Version";

	private static final String ALIGNMENT = "Alignments";

	// columns read into geWorkbench
	// probe id must be first column read in, and the rest of the columns must
	// follow the same order
	// as the columns in the annotation file.
	private static final String[] labels = {
			PROBE_SET_ID // probe id must be the first item in this list
			, SCIENTIFIC_NAME, UNIGENE_CLUSTER, UNIGENE, GENOME_VERSION,
			ALIGNMENT, DESCRIPTION, GENE_SYMBOL, LOCUSLINK, SWISSPROT,
			CHROMOSOMAL, REFSEQ, GENE_ONTOLOGY_BIOLOGICAL_PROCESS,
			GENE_ONTOLOGY_CELLULAR_COMPONENT, GENE_ONTOLOGY_MOLECULAR_FUNCTION,
			PATHWAY, TRANSCRIPT };

	public static Map<String, String> processAnnotationData(String chipType,
			File datafile, TreeSet<Gene> preGeneList) {
		if (datafile.exists()) { // data file is found

			FileInputStream fis = null;
			BufferedInputStream bis = null;

			Map<String, String> probeChromosomal = new HashMap<String, String>();

			try {
				fis = new FileInputStream(datafile);
				bis = new BufferedInputStream(fis);

				CSVParser cvsParser = new CSVParser(bis);

				cvsParser.setCommentStart("#;!");// Skip all comments line.
				// XQ. The bug is reported
				// by Bernd.

				LabeledCSVParser parser = new LabeledCSVParser(cvsParser);

				while (parser.getLine() != null) {
					String affyId = parser.getValueByLabel(labels[0]);

					String locusLink = null;
					String chromosomal = null;
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
						if (label.equals(LOCUSLINK))
							locusLink = val;
						else if (label.equals(CHROMOSOMAL))
							chromosomal = val;
					}

					try {
						int entrezGene = Integer.parseInt(locusLink);
						Iterator<Gene> iterGene = preGeneList.iterator();
						while (iterGene.hasNext()) {
							Gene g = iterGene.next();
							if (g.getGeneNo() == entrezGene) {
								String s = g.getProbeIds();
								s += affyId + "\t";
								g.setProbeIds(s);
								probeChromosomal.put(affyId, chromosomal);
							}

						}

					} catch (Exception e) {
					}
				}

				return probeChromosomal;
			} catch (Exception e) {
				log.error(e);
				return null;
			} finally {
				try {
					fis.close();
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			return null;
		}

	}

}
