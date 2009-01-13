package org.geworkbench.components.caarray.test;

import java.rmi.Remote;
import java.util.List;
import java.util.Set;

import gov.nih.nci.caarray.domain.hybridization.Hybridization;
import gov.nih.nci.caarray.domain.project.Experiment;
import gov.nih.nci.caarray.services.CaArrayServer;
import gov.nih.nci.caarray.services.ServerConnectionException;
import gov.nih.nci.caarray.services.search.CaArraySearchService;
import gov.nih.nci.cagrid.cqlquery.CQLQuery;

public class CaArraySimpleTest implements Remote {

	/**
	 * @param args
	 * @throws ServerConnectionException 
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws ServerConnectionException {
		long begin = System.currentTimeMillis();
		CQLQuery query = new CQLQuery();
		gov.nih.nci.cagrid.cqlquery.Object object = new gov.nih.nci.cagrid.cqlquery.Object();
		object.setName("gov.nih.nci.caarray.domain.project.Experiment");
		query.setTarget(object);

		CaArrayServer server = new CaArrayServer("array.nci.nih.gov", 8080);
//		CaArrayServer server = new CaArrayServer("afapp1.c2b2.columbia.edu", 31099);
		server.connect();
		CaArraySearchService service = server.getSearchService();
		List list = service.search(query);

		int i = 1;
		for(Object o: list) {
			Experiment e = ((Experiment) o);
			System.out.println("["+i+"]"+e.getDescription()+" "+e.getTitle());
			i++;
		}
		long finish = System.currentTimeMillis();
		System.out.println("It took "+( finish- begin)+ " milliseconds");
		
		i = 1;
		for(Object o: list) {
			Experiment e = ((Experiment) o);
			Set<Hybridization> h1 = (((Experiment) o).getHybridizations());
			if(h1.size() > 0) {
				System.out.println("["+i+"]"+e.getTitle());
				i++;
			}
		}

	}

}
