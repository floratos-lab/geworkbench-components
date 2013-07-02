package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.Evidence;
import gov.nih.nci.cabio.domain.Gene;
import gov.nih.nci.common.domain.DatabaseCrossReference;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.ApplicationService;
import gov.nih.nci.system.client.ApplicationServiceProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.annotation.Pathway;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * Implementation of the <code>GeneAnnotation</code> contract
 *
 * @author First Genetic Trust
 * @version $Id$
 */
public class GeneAnnotationImpl implements GeneAnnotation {

    static Log log = LogFactory.getLog(GeneAnnotationImpl.class);

    /**
     * Web URL prefix for obtaining CGAP annotation
     */
    private static final String GENE_FINDER_PREFIX = "http://cgap.nci.nih.gov/Genes/GeneInfo?";
    /**
     * Web URL prefix currently being used
     */
    public static final String PREFIX_USED = GENE_FINDER_PREFIX;
    /**
     * Gene name
     */
    private String name = null;
    /**
     * Gene description
     */
    private String description = null;
    /**
     * Gene annotation URL
     */
    private URL url = null;
    private List<CGAPUrl> CGAPURLs = null;
    /**
     * Associated pathways
     */
    private Pathway[] pathways = null;
    /**
     * Gene Locus Link ID
     */
    private String locusLinkId = null;
    /**
     * Gene Unigene Cluster ID
     */
    private Long unigeneClusterId = null;
    /**
     * Organism abbreviation
     */
    private String organism = null;
    private static final String HUMAN_ABBREV = "Hs";
    private static final String MOUSE_ABBREV = "Mm";
    
    private String symbol; 
    
    public static boolean stopAlgorithm = false;

    /**
     * Default Constructor
     */
    public GeneAnnotationImpl() {
    }

    private Gene gene = null;
    /**
     * Constructor
     *
     * @param gene <code>Gene</code>
     */
    public GeneAnnotationImpl(Gene gene) {
    	this.gene = gene;
        name = gene.getFullName();
        symbol = gene.getSymbol();
//        description = gene.getLocusLinkSummary();
//        locusLinkId = gene.getLocusLinkId();
        description = "Unknown";
        locusLinkId = "-1";

        Gene g = new Gene();
        g.setId(gene.getId());

        gov.nih.nci.cabio.domain.Pathway pathway = new gov.nih.nci.cabio.domain.Pathway();
        Set<Gene> genes = new HashSet<Gene>();
        genes.add(g);
        pathway.setGeneCollection(genes);
        
        try {
        	ApplicationService appService = ApplicationServiceProvider.getApplicationService();
            List<gov.nih.nci.cabio.domain.Pathway> pways = appService.search(
                    "gov.nih.nci.cabio.domain.Pathway", pathway);
            pathways = PathwayImpl.toArray(pways);
        } catch (ApplicationException e) {
            log.error(e);
        }
        catch (Exception e) {
            log.error(e);
        }

        url = composeURL(gene);
        CGAPURLs = composeCGAPURLs(gene);
    }

    private URL composeURL(Gene gene) {
        URL link = null;
        unigeneClusterId = gene.getClusterId();
        try {
            organism = gene.getTaxon().getAbbreviation();
            link = new URL(GENE_FINDER_PREFIX + "ORG=" + organism + "&CID=" + unigeneClusterId);
            //link = new URL(LOCUS_LINK_PREFIX + locusLinkId);
        } catch (MalformedURLException mu) {
            mu.printStackTrace();
        }

        return link;
    }

    private List<CGAPUrl> composeCGAPURLs(Gene gene) {
        ArrayList<CGAPUrl> urls = new ArrayList<CGAPUrl>();
        unigeneClusterId = gene.getClusterId();
        try {
            urls.add(new CGAPUrl(new URL(GENE_FINDER_PREFIX + "ORG=" + HUMAN_ABBREV + "&CID=" + unigeneClusterId), HUMAN_ABBREV, "Human"));
            urls.add(new CGAPUrl(new URL(GENE_FINDER_PREFIX + "ORG=" + MOUSE_ABBREV + "&CID=" + unigeneClusterId), MOUSE_ABBREV, "Mouse"));
        } catch (MalformedURLException mu) {
            mu.printStackTrace();
        }

        return urls;
    }

    /**
     * Gets the Gene Name
     *
     * @return Gene Name
     */
    public String getGeneName() {
        return name;
    }

    /**
     * Gets the Gene Description
     *
     * @return Gene Description
     */
    public String getGeneDescription() {
        return description;
    }

    /**
     * Gets the Locus Link Identifier
     *
     * @return Locus Link ID
     */
    public String getLocusLinkId() {
        return locusLinkId;
    }

    /**
     * Gets the Unigene Cluster Identifier
     *
     * @return Unigene Cluster ID
     */
    public Long getUnigeneClusterId() {
        return unigeneClusterId;
    }

    /**
     * Gets the short form of 'Genus species' nomenclature of the organism
     * in question
     *
     * @return short name
     */
    public String getOrganismAbbreviation() {
        return organism;
    }

    /**
     * Gets the Gene Annotation Uniform Resource Locator. This could be either a
     * Locus Link annotation or an CGAP annotation
     *
     * @return annotation URL
     */
    public URL getGeneURL() {
        return url;
    }

    public List<CGAPUrl> getCGAPGeneURLs() {
        return CGAPURLs;
    }

    /**
     * Gets Scalable Vector Graphic images of Pathways contained in a
     * <code>Pathway[]</code> data structure
     *
     * @return pathways
     */
    public org.geworkbench.util.annotation.Pathway[] getPathways() {
        return pathways;
    }

    public String getGeneSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

	static class EvidenceStruct
	{
		private String sentence = null;
		private String pubmedId = null;
		EvidenceStruct(String sent, String pub)
		{
			sentence = sent;
			pubmedId = pub;
		}
		public String getSentence() {
			return sentence;
		}
		public String getPubmedId() {
			return pubmedId;
		}
}

    /*
     * get sentence and pubmedId from evidence collection
     */
    protected static EvidenceStruct getSentencePubmedid(Collection<Evidence> ce)
    {
    	String sentence = "";
    	String pubmedId = "";
		for (Iterator<Evidence> it = ce.iterator(); it.hasNext(); )
		{
			Evidence e = it.next();
			sentence += e.getSentence();
			pubmedId += Integer.toString(e.getPubmedId());
			if (it.hasNext()) 
			{
				sentence += "; ";
				pubmedId += "; ";
			}
		}
		return new EvidenceStruct(sentence, pubmedId);
    }
    
    /**
     * Creates <code>GeneAnnotation[]</code> object from <code>Gene[]</code>
     * obtained from caBIO queries
     *
     * @param geneList query result from caBIO
     * @return <code>GeneAnnotation[]</code> object
     */
    public static GeneAnnotation[] toUniqueArray(List<gov.nih.nci.cabio.domain.Gene> geneList) {
        Set<GeneAnnotation> uniqueGenes = new HashSet<GeneAnnotation>();
        for (int i = 0; i < geneList.size(); i++) {
        	if ( stopAlgorithm == true )
        	{        		 
        		stopAlgorithm = false;
        		return null;
        	}
        	Gene g = geneList.get(i);
        	if((g != null) && (g.getSymbol() != null)){
	            uniqueGenes.add(new GeneAnnotationImpl(g));
        	}
        }
        return uniqueGenes.toArray(new GeneAnnotation[]{});
    }

    public static GeneAnnotation[] toUniqueArray(List<gov.nih.nci.cabio.domain.Gene> geneList, String organism) {
        Set<GeneAnnotation> uniqueGenes = new HashSet<GeneAnnotation>();
        for (int i = 0; i < geneList.size(); i++) {
        	if ( stopAlgorithm == true )
        	{        		 
        		stopAlgorithm = false;
        		return null;
        	}
        	Gene g = geneList.get(i);

        	if((g != null) && (g.getSymbol() != null) && (g.getTaxon().getAbbreviation().equals(organism))){
	            uniqueGenes.add(new GeneAnnotationImpl(g));
        	}
        }
        return uniqueGenes.toArray(new GeneAnnotation[]{});
    }

    public boolean equals(Object object) {
        GeneAnnotation other = (GeneAnnotation) object;
        return symbol.equals(other.getGeneSymbol());
    }

    public int hashCode() {
        return symbol.hashCode();
    }

    public class CGAPUrl {
        private URL url;
        private String organismAbbrev;
        private String organismName;

        public CGAPUrl(URL url, String organismAbbrev, String organismName) {
            this.url = url;
            this.organismAbbrev = organismAbbrev;
            this.organismName = organismName;
        }

        public URL getUrl() {
            return url;
        }

        public void setUrl(URL url) {
            this.url = url;
        }

        public String getOrganismAbbrev() {
            return organismAbbrev;
        }

        public void setOrganismAbbrev(String organismAbbrev) {
            this.organismAbbrev = organismAbbrev;
        }

        public String getOrganismName() {
            return organismName;
        }

        public void setOrganismName(String organismName) {
            this.organismName = organismName;
        }
    }

	public Gene getGene() {
		return gene;
	}

	public String getEntrezId(Gene gene) {
        String entrezId = "";
        Collection<DatabaseCrossReference> crossReferences = gene.getDatabaseCrossReferenceCollection();
        for (Iterator<DatabaseCrossReference> iterator = crossReferences.iterator(); iterator.hasNext();) {
			DatabaseCrossReference crossReference = (DatabaseCrossReference) iterator.next();
			if (crossReference.getDataSourceName().equals("LOCUS_LINK_ID")){
				entrezId = crossReference.getCrossReferenceId();
				break;
			}
		}
        return entrezId;
	}
}
