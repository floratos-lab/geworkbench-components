package org.geworkbench.components.annotations;

public class AgentDiseaseResults {
    MarkerData[] markers = null;
    GeneData[] genes = null;
    DiseaseData[] diseases = null;
    RoleData[] roles = null;
    SentenceData[] sentences = null;
    PubmedData[] pubmeds = null;
    MarkerData[] markers2 = null;
    GeneData[] genes2 = null;
    EvsIdData[] evsIds = null;
    DiseaseData[] agents = null;
    RoleData[] agentRoles = null;
    SentenceData[] agentSentences = null;
    PubmedData[] agentPubmeds = null;
    int size = -1;
    
	public AgentDiseaseResults(MarkerData[] markers, GeneData[] genes,
			DiseaseData[] diseases, RoleData[] roles, SentenceData[] sentences,
			PubmedData[] pubmeds, MarkerData[] markers2, GeneData[] genes2,
			EvsIdData[] evsIds, DiseaseData[] agents, RoleData[] agentRoles,
			SentenceData[] agentSentences, PubmedData[] agentPubmeds) {
		super();
		this.markers = markers;
		this.genes = genes;
		this.diseases = diseases;
		this.roles = roles;
		this.sentences = sentences;
		this.pubmeds = pubmeds;
		this.markers2 = markers2;
		this.genes2 = genes2;
		this.evsIds = evsIds;
		this.agents = agents;
		this.agentRoles = agentRoles;
		this.agentSentences = agentSentences;
		this.agentPubmeds = agentPubmeds;
		if (markers==null)
			this.size = 0;
		else
			this.size = markers.length;
	}
	public MarkerData[] getMarkers() {
		return markers;
	}
	public GeneData[] getGenes() {
		return genes;
	}
	public DiseaseData[] getDiseases() {
		return diseases;
	}
	public RoleData[] getRoles() {
		return roles;
	}
	public SentenceData[] getSentences() {
		return sentences;
	}
	public PubmedData[] getPubmeds() {
		return pubmeds;
	}
	public MarkerData[] getMarkers2() {
		return markers2;
	}
	public GeneData[] getGenes2() {
		return genes2;
	}
	public EvsIdData[] getEvsIds() {
		return evsIds;
	}
	public DiseaseData[] getAgents() {
		return agents;
	}
	public RoleData[] getAgentRoles() {
		return agentRoles;
	}
	public SentenceData[] getAgentSentences() {
		return agentSentences;
	}
	public PubmedData[] getAgentPubmeds() {
		return agentPubmeds;
	}
	public int getSize() {
		return size;
	}
}
