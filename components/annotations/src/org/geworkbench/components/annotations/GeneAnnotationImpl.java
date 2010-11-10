package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.Evidence;
import gov.nih.nci.cabio.domain.Gene;
import gov.nih.nci.cabio.domain.GeneAgentAssociation;
import gov.nih.nci.cabio.domain.GeneDiseaseAssociation;
import gov.nih.nci.cabio.domain.GeneFunctionAssociation;
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

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
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

	public AgentDiseaseResults retrieveAll(RetrieveAllTask task,
			DSItemList<DSGeneMarker> retrieveMarkerInfo, CGITableModel diseaseModel, CGITableModel agentModel) {
        ArrayList<MarkerData> markerData = new ArrayList<MarkerData>();
        ArrayList<GeneData> geneData = new ArrayList<GeneData>();
        ArrayList<DiseaseData> diseaseData = new ArrayList<DiseaseData>();
        ArrayList<RoleData> roleData = new ArrayList<RoleData>();
        ArrayList<SentenceData> sentenceData = new ArrayList<SentenceData>();
        ArrayList<PubmedData> pubmedData = new ArrayList<PubmedData>();
        ArrayList<MarkerData> markerData2 = new ArrayList<MarkerData>();
        ArrayList<GeneData> geneData2 = new ArrayList<GeneData>();
        ArrayList<EvsIdData> evsIdData = new ArrayList<EvsIdData>();
        ArrayList<DiseaseData> agentData = new ArrayList<DiseaseData>();
        ArrayList<RoleData> agentRoleData = new ArrayList<RoleData>();
        ArrayList<SentenceData> agentSentenceData = new ArrayList<SentenceData>();
        ArrayList<PubmedData> agentPubmedData = new ArrayList<PubmedData>();
        if (retrieveMarkerInfo != null) {

    		ApplicationService appService = null;
    		try {
    			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    			appService = ApplicationServiceProvider.getApplicationService();
    		} catch (Exception e) {
    			log.error(e,e);
				JOptionPane
				.showConfirmDialog(
						null,
						"Please try again later",
						"Server side error", JOptionPane.OK_OPTION);
    			return null;
    		}

            int index = 0;
            //TODO: to save network communication time, we should query only once by submitting the list.
            for (int i = 0; i < retrieveMarkerInfo.size(); i++) {
                index++;
                String progressMessage = "Getting Disease/Agent Associations: ";
                progressMessage += "Marker "+index+"/"+retrieveMarkerInfo.size()+"   ";
				if (task.isCancelled()) return null;

                String geneName = retrieveMarkerInfo.get(i).getGeneName();

        		String geneSymbol = geneName;
        		//int uniGeneId = marker.getUnigene().getUnigeneId();
        		Gene gene = new Gene();
        		gene.setSymbol(geneSymbol);

        		List<Object> results2 = null;
        		try {
        			results2 = appService.search(GeneFunctionAssociation.class, gene);
        		} catch (Exception e) {
        			log.error(e,e);
        			JOptionPane.showMessageDialog(null, 
        					"geWorkbench cannot retrieve data from the caBIO server for disease/agent associations.\nIt could be connection error. Please check your internet connection or try again later.",
        					"Data processing/connection error", JOptionPane.ERROR_MESSAGE);
        			return null;
        		}

        		log.debug("\nDisease associated with Gene: " + geneSymbol);
        		int diseaseIndex = 0;
        		int agentIndex = 0;
        		int diseaseRecords=0;
        		int agentRecords=0;
        		if (results2!=null)
        		for (Object gfa : results2) {
        			if (gfa instanceof GeneDiseaseAssociation) {
        				diseaseRecords++;
        			}
        			else if (gfa instanceof GeneAgentAssociation) {
        				agentRecords++;
        			}
        		}
        		int total = diseaseRecords+agentRecords;
        		String numOutOfNumDisease = "";
        		String numOutOfNumAgent = "";
//        		if (diseaseRecords>diseaseLimitIndex){
//        			numOutOfNumDisease = diseaseLimitIndex+" out of "+diseaseRecords;
//        		}
//        		if (agentRecords>agentLimitIndex){
//        			numOutOfNumAgent = agentLimitIndex+" out of "+agentRecords;
//        		}
        		if (results2!=null)
        		for (Object gfa : results2) {
					if (task.isCancelled()) return null;
        			task.publish(progressMessage+"Records "+(diseaseIndex+agentIndex)+"/"+total+"\n");
            		task.setProgress(100 * (diseaseIndex+agentIndex)/total);

        			if (gfa instanceof GeneDiseaseAssociation) {
        				diseaseIndex++;
        				GeneDiseaseAssociation gda = (GeneDiseaseAssociation) gfa;
        				Collection<Evidence> ce = gda.getEvidenceCollection();
        				MarkerData markerDataNew = new MarkerData(retrieveMarkerInfo.get(i),numOutOfNumDisease);
        				GeneData geneDataNew = new GeneData(gda.getGene().getSymbol(),gda.getGene());
        				DiseaseData diseaseDataNew = new DiseaseData(gda.getDiseaseOntology().getName(),gda.getDiseaseOntology(), gda.getDiseaseOntology().getEVSId());
        				RoleData roleDataNew = new RoleData(gda.getRole());
        				EvidenceStruct e = GeneAnnotationImpl.getSentencePubmedid(ce);
        				SentenceData sentenceDataNew = new SentenceData(e.getSentence());
        				PubmedData pubmedDataNew = new PubmedData(e.getPubmedId());
        				if (!diseaseModel.containsRecord(markerDataNew, geneDataNew, diseaseDataNew, roleDataNew, sentenceDataNew, pubmedDataNew)){
            				markerData.add(new MarkerData(retrieveMarkerInfo.get(i),numOutOfNumDisease));
            				geneData.add(new GeneData(gda.getGene().getSymbol(),gda.getGene()));
            				log.debug("  Disease: " + gda.getDiseaseOntology().getName());
            				diseaseData.add(new DiseaseData(gda.getDiseaseOntology().getName(),gda.getDiseaseOntology(), gda.getDiseaseOntology().getEVSId()));
            				log.debug("    Role: " + gda.getRole());
            				log.debug("    Sentence: "+e.getSentence());
            				log.debug("    PubmedId:"+e.getPubmedId());
            				roleData.add(new RoleData(gda.getRole()));
            				sentenceData.add(new SentenceData(e.getSentence()));
            				pubmedData.add(new PubmedData(e.getPubmedId()));
            				log.debug("We got "+markerDataNew.name+","+geneDataNew.name+","+diseaseDataNew.name+","+roleDataNew.role+","+sentenceDataNew.sentence+","+pubmedDataNew.id);
        				}else{
        					log.debug("We already got "+markerDataNew.name+","+geneDataNew.name+","+diseaseDataNew.name+","+roleDataNew.role+","+sentenceDataNew.sentence+","+pubmedDataNew.id);
        				}
        			}
        			else if (gfa instanceof GeneAgentAssociation) {
        				diseaseIndex++;
        				GeneAgentAssociation gaa = (GeneAgentAssociation) gfa;
        				Collection<Evidence> ce = gaa.getEvidenceCollection();
        				MarkerData markerDataNew = new MarkerData(retrieveMarkerInfo.get(i),numOutOfNumAgent);
        				GeneData geneDataNew = new GeneData(gaa.getGene().getSymbol(),gaa.getGene());
        				DiseaseData diseaseDataNew = new DiseaseData(gaa.getAgent().getName(),null, gaa.getAgent().getEVSId());
        				RoleData roleDataNew = new RoleData(gaa.getRole());
        				EvidenceStruct e = GeneAnnotationImpl.getSentencePubmedid(ce);
        				SentenceData sentenceDataNew = new SentenceData(e.getSentence());
        				PubmedData pubmedDataNew = new PubmedData(e.getPubmedId());
        				if (!agentModel.containsRecord(markerDataNew, geneDataNew, diseaseDataNew, roleDataNew, sentenceDataNew, pubmedDataNew)){
            				markerData2.add(new MarkerData(retrieveMarkerInfo.get(i),numOutOfNumAgent));
            				geneData2.add(new GeneData(gaa.getGene().getSymbol(),gaa.getGene()));
            				evsIdData.add(new EvsIdData(gaa.getAgent().getEVSId()));
            				agentData.add(new DiseaseData(gaa.getAgent().getName(),null, gaa.getAgent().getEVSId()));
            				log.debug("  Id: " + gaa.getId());
            				log.debug("  Role: " + gaa.getRole());
            				log.debug("  EvsId: " + gaa.getAgent().getEVSId());
            				log.debug("  Name: " + gaa.getAgent().getName());
            				log.debug("    Sentence: "+e.getSentence());
            				log.debug("    PubmedId:"+e.getPubmedId());
            				agentRoleData.add(new RoleData(gaa.getRole()));
            				agentSentenceData.add(new SentenceData(e.getSentence()));
            				agentPubmedData.add(new PubmedData(e.getPubmedId()));
        				}
        			}
        		}
            }
        }
        task.publish("Converting data...\n");
        if(task.isCancelled()) return null;
        MarkerData[] markers = markerData.toArray(new MarkerData[0]);
        GeneData[] genes = geneData.toArray(new GeneData[0]);
        DiseaseData[] diseases = diseaseData.toArray(new DiseaseData[0]);
        RoleData[] roles = roleData.toArray(new RoleData[0]);
        SentenceData[] sentences = sentenceData.toArray(new SentenceData[0]);
        PubmedData[] pubmeds = pubmedData.toArray(new PubmedData[0]);
        MarkerData[] markers2 = markerData2.toArray(new MarkerData[0]);
        GeneData[] genes2 = geneData2.toArray(new GeneData[0]);
        //TODO: clean variables for this evsIds, since we handle evsIds in agentData now.
        EvsIdData[] evsIds = evsIdData.toArray(new EvsIdData[0]);
        DiseaseData[] agents = agentData.toArray(new DiseaseData[0]);
        RoleData[] agentRoles = agentRoleData.toArray(new RoleData[0]);
        SentenceData[] agentSentences = agentSentenceData.toArray(new SentenceData[0]);
        PubmedData[] agentPubmeds = agentPubmedData.toArray(new PubmedData[0]);
        return new AgentDiseaseResults(markers, genes, diseases, roles,
				sentences, pubmeds, markers2, genes2, evsIds, agents,
				agentRoles, agentSentences, agentPubmeds);
	}

	public AgentDiseaseResults showAnnotation(CGITask task, DSItemList<DSGeneMarker> selectedMarkerInfo) {
        ArrayList<MarkerData> markerData = new ArrayList<MarkerData>();
        ArrayList<GeneData> geneData = new ArrayList<GeneData>();
        ArrayList<DiseaseData> diseaseData = new ArrayList<DiseaseData>();
        ArrayList<RoleData> roleData = new ArrayList<RoleData>();
        ArrayList<SentenceData> sentenceData = new ArrayList<SentenceData>();
        ArrayList<PubmedData> pubmedData = new ArrayList<PubmedData>();
        ArrayList<MarkerData> markerData2 = new ArrayList<MarkerData>();
        ArrayList<GeneData> geneData2 = new ArrayList<GeneData>();
        ArrayList<EvsIdData> evsIdData = new ArrayList<EvsIdData>();
        ArrayList<DiseaseData> agentData = new ArrayList<DiseaseData>();
        ArrayList<RoleData> agentRoleData = new ArrayList<RoleData>();
        ArrayList<SentenceData> agentSentenceData = new ArrayList<SentenceData>();
        ArrayList<PubmedData> agentPubmedData = new ArrayList<PubmedData>();
        if (selectedMarkerInfo != null) {

    		ApplicationService appService = null;
    		try {
    			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    			appService = ApplicationServiceProvider.getApplicationService();
    		} catch (Exception e) {
    			log.error(e,e);
				JOptionPane
				.showConfirmDialog(
						null,
						"Please try again later",
						"Server side error", JOptionPane.OK_OPTION);
    			return null;
    		}

            int index = 0;
    		final int diseaseLimit = 10;
    		final int agentLimit = 10;
    		int roundtotal = diseaseLimit+agentLimit;
    		int total = roundtotal*selectedMarkerInfo.size();
            //TODO: to save network communication time, we should query only once by submitting the list.
            for (int i = 0; i < selectedMarkerInfo.size(); i++) {
                index++;
                String progressMessage = "Getting Disease/Agent Associations: ";
                progressMessage += "Marker "+index+"/"+selectedMarkerInfo.size()+"   ";
                if(task.isCancelled()) return null;

                String geneName = selectedMarkerInfo.get(i).getGeneName();

        		String geneSymbol = geneName;
        		//int uniGeneId = marker.getUnigene().getUnigeneId();
        		Gene gene = new Gene();
        		gene.setSymbol(geneSymbol);

        		List<Object> results2 = null;
        		try {
        			results2 = appService.search(GeneFunctionAssociation.class, gene);
        		} catch (Exception e) {
        			JOptionPane.showMessageDialog(null, 
        					"geWorkbench cannot retrieve data from the caBIO server for disease/agent associations.\nIt could be connection error. Please check your internet connection or try again later.",
        					"Data processing/connection error", JOptionPane.ERROR_MESSAGE);
        			return null;
        		}

        		log.debug("\nDisease associated with Gene: " + geneSymbol);

        		int diseaseLimitIndex = diseaseLimit;
        		int agentLimitIndex = agentLimit;
        		int diseaseRecords=0;
        		int agentRecords=0;
        		if (results2!=null)
        		for (Object gfa : results2) {
        			if (gfa instanceof GeneDiseaseAssociation) {
        				diseaseRecords++;
        			}
        			else if (gfa instanceof GeneAgentAssociation) {
        				agentRecords++;
        			}
        		}
        		String numOutOfNumDisease = "";
        		String numOutOfNumAgent = "";
        		if (diseaseRecords>diseaseLimitIndex){
        			numOutOfNumDisease = diseaseLimitIndex+" out of "+diseaseRecords;
        		}
        		if (agentRecords>agentLimitIndex){
        			numOutOfNumAgent = agentLimitIndex+" out of "+agentRecords;
        		}
        		if (results2!=null)
        		for (Object gfa : results2) {
        			if(task.isCancelled()) return null;
        			if (diseaseLimitIndex==0 && agentLimitIndex==0)  break;
        			task.publish(progressMessage+"Records "+(roundtotal-diseaseLimitIndex-agentLimitIndex)+"/"+(diseaseRecords+agentRecords)+"\n");
            		task.setProgress(100 * (roundtotal*(i+1)-diseaseLimitIndex-agentLimitIndex)/total);

        			if (gfa instanceof GeneDiseaseAssociation) {
            			if (diseaseLimitIndex>0) {
            				diseaseLimitIndex--;
	        				GeneDiseaseAssociation gda = (GeneDiseaseAssociation) gfa;
	        				markerData.add(new MarkerData(selectedMarkerInfo.get(i),numOutOfNumDisease));
	        				geneData.add(new GeneData(gda.getGene().getSymbol(),gda.getGene()));
	        				log.debug("  Disease: " + gda.getDiseaseOntology().getName());
	        				diseaseData.add(new DiseaseData(gda.getDiseaseOntology().getName(),gda.getDiseaseOntology(), gda.getDiseaseOntology().getEVSId()));
	        				log.debug("    Role: " + gda.getRole());
	        				Collection<Evidence> ce = gda.getEvidenceCollection();
	        				EvidenceStruct e = GeneAnnotationImpl.getSentencePubmedid(ce);
	        				log.debug("    Sentence: "+e.getSentence());
	        				log.debug("    PubmedId:"+e.getPubmedId());
	        				roleData.add(new RoleData(gda.getRole()));
	        				sentenceData.add(new SentenceData(e.getSentence()));
	        				pubmedData.add(new PubmedData(e.getPubmedId()));
            			}
        			}
        			else if (gfa instanceof GeneAgentAssociation) {
            			if (agentLimitIndex>0) {
            				agentLimitIndex--;
	        				GeneAgentAssociation gaa = (GeneAgentAssociation) gfa;
	        				markerData2.add(new MarkerData(selectedMarkerInfo.get(i),numOutOfNumAgent));
	        				geneData2.add(new GeneData(gaa.getGene().getSymbol(),gaa.getGene()));
	        				evsIdData.add(new EvsIdData(gaa.getAgent().getEVSId()));
	        				agentData.add(new DiseaseData(gaa.getAgent().getName(),null, gaa.getAgent().getEVSId()));
	        				log.debug("  Id: " + gaa.getId());
	        				log.debug("  Role: " + gaa.getRole());
	        				log.debug("  EvsId: " + gaa.getAgent().getEVSId());
	        				log.debug("  Name: " + gaa.getAgent().getName());
	        				Collection<Evidence> ce = gaa.getEvidenceCollection();
	        				EvidenceStruct e = GeneAnnotationImpl.getSentencePubmedid(ce);
	        				log.debug("    Sentence: "+e.getSentence());
	        				log.debug("    PubmedId:"+e.getPubmedId());
	        				agentRoleData.add(new RoleData(gaa.getRole()));
	        				agentSentenceData.add(new SentenceData(e.getSentence()));
	        				agentPubmedData.add(new PubmedData(e.getPubmedId()));
            			}
        			}
        		}
            }
        }
        
        if(task.isCancelled()) return null;
        MarkerData[] markers = markerData.toArray(new MarkerData[0]);
        GeneData[] genes = geneData.toArray(new GeneData[0]);
        DiseaseData[] diseases = diseaseData.toArray(new DiseaseData[0]);
        RoleData[] roles = roleData.toArray(new RoleData[0]);
        SentenceData[] sentences = sentenceData.toArray(new SentenceData[0]);
        PubmedData[] pubmeds = pubmedData.toArray(new PubmedData[0]);
        MarkerData[] markers2 = markerData2.toArray(new MarkerData[0]);
        GeneData[] genes2 = geneData2.toArray(new GeneData[0]);
        //TODO: clean variables for this evsIds, since we handle evsIds in agentData now.
        EvsIdData[] evsIds = evsIdData.toArray(new EvsIdData[0]);
        DiseaseData[] agents = agentData.toArray(new DiseaseData[0]);
        RoleData[] agentRoles = agentRoleData.toArray(new RoleData[0]);
        SentenceData[] agentSentences = agentSentenceData.toArray(new SentenceData[0]);
        PubmedData[] agentPubmeds = agentPubmedData.toArray(new PubmedData[0]);
        return new AgentDiseaseResults(markers, genes, diseases, roles,
				sentences, pubmeds, markers2, genes2, evsIds, agents,
				agentRoles, agentSentences, agentPubmeds);
	}

}

