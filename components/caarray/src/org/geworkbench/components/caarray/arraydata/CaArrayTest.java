/**
 * 
 */
package org.geworkbench.components.caarray.arraydata;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import gov.nih.nci.caarray.domain.data.DataRetrievalRequest;
import gov.nih.nci.caarray.domain.hybridization.Hybridization;
import gov.nih.nci.caarray.domain.project.Experiment;
import gov.nih.nci.caarray.domain.project.ExperimentContact;
import gov.nih.nci.caarray.domain.sample.Source;
import gov.nih.nci.caarray.services.CaArrayServer;
import gov.nih.nci.caarray.services.search.CaArraySearchService;
import gov.nih.nci.cagrid.cqlquery.Association;
import gov.nih.nci.cagrid.cqlquery.Attribute;
import gov.nih.nci.cagrid.cqlquery.CQLQuery;
import gov.nih.nci.cagrid.cqlquery.Predicate;

import org.apache.commons.lang.time.StopWatch;
import org.geworkbench.builtin.projects.remoteresources.carraydata.CaArray2Experiment;
import org.geworkbench.builtin.projects.remoteresources.query.CaARRAYQueryPanel;

/**
 * @author xiaoqing
 * 
 */
public class CaArrayTest {

	/**
	 * 
	 */
	public CaArrayTest() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	protected static final String SERVER_NAME = "array.nci.nih.gov ";
	protected static final int JNDI_PORT = 8080;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CaArrayTest caArrayTest = new CaArrayTest();
		String username = "ZhangXi";
		String password = "Xz0401!!";
		String busername = "JaglaB";
		String bpassword = "Robo45$x";
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		try {
			// Test the wrong IP address. about 7 seconds
			// caArrayTest.getExperiments("127.0.0.1", JNDI_PORT, null, null);
			// stopWatch.stop();
			// Test the wrong user name, about 4 seconds
			// caArrayTest.getExperiments(SERVER_NAME, JNDI_PORT, "pub",
			// "test");
			// stopWatch.stop();
			// Test the wrong port number, about 27 seconds for port number 900.
			System.out.println("Check the port 80..." + new Date());
			// caArrayTest.getAssocations(SERVER_NAME, JNDI_PORT, null, null);
			
			CaArrayServer server = new CaArrayServer(SERVER_NAME, JNDI_PORT);
			CaArraySearchService searchService;// = server.getSearchService();
			DataRetrievalRequest request = new DataRetrievalRequest();
			if (username != null) {
				server.connect(username, password);
			} else {
				server.connect();
			}
			 
			searchService = server.getSearchService();
			request = new DataRetrievalRequest();
			CaArrayQueryClient.lookupTypeValues(searchService, request, CaARRAYQueryPanel.listContent);
			caArrayTest.getAssocations(SERVER_NAME, JNDI_PORT, null, null,
					CaARRAYQueryPanel.TISSUETYPE, "Prostate");
			stopWatch.stop();
			System.out.println("used in lookupTypeValues:  " + stopWatch.getTime());
			 caArrayTest.getExperiments(SERVER_NAME, JNDI_PORT, null, null);
			
			 caArrayTest.getExperiments(SERVER_NAME, JNDI_PORT, busername,
					 bpassword);
			 caArrayTest.getExperiments(SERVER_NAME, JNDI_PORT, username,
					 password);
			 caArrayTest.getExperiments(SERVER_NAME, JNDI_PORT, null, null);
		} catch (Exception e) {
			stopWatch.stop();
			System.out.println("Exception: " + stopWatch.getTime());
			e.printStackTrace();
		}
	}

	public List getExperiments(String url, int port, String username,
			String password) throws Exception {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		CaArrayServer server = new CaArrayServer(url, port);
		CaArraySearchService searchService;// = server.getSearchService();
		CaArrayQueryClient dataClient = new CaArrayQueryClient();
		if (username != null) {
			server.connect(username, password);
		} else {
			server.connect();
		}
		stopWatch.stop();
		System.out.println("used: " + stopWatch.getTime());
		searchService = server.getSearchService();
		CQLQuery query = new CQLQuery();
		query.setTarget(new gov.nih.nci.cagrid.cqlquery.Object());
		String target = "gov.nih.nci.caarray.domain.project.Experiment";
		query.getTarget().setName(target);
		List list = searchService.search(query);
		ArrayList experimentsWithDataList = new ArrayList();
		for (Object o : list) {
			Experiment e = ((Experiment) o);
			Set<Hybridization> h1 = (((Experiment) o).getHybridizations());
			if (h1.size() > 0) {
				String[] hybridizationValues = new String[h1.size()];
				TreeSet<String> set = new TreeSet<String>();
				for (Hybridization h : h1) {
					set.add(h.getName());
				}
				experimentsWithDataList.add(o);
			}
		}

		if (username != null) {
			System.out
					.println("With password, the total number of Exp with at least one hybridization is "
							+ experimentsWithDataList.size());
		} else {
			System.out
					.println("Without password, the total number of Exp with at least one hybridization "
							+ experimentsWithDataList.size());

		}
		return list;
	}

	public List getAssocations(String url, int port, String username,
			String password) throws Exception {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		CaArrayServer server = new CaArrayServer(url, port);
		CaArraySearchService searchService;// = server.getSearchService();
		DataRetrievalRequest request = new DataRetrievalRequest();

		CaArray2Experiment[] exps;
		CaArrayQueryClient dataClient = new CaArrayQueryClient();
		if (username != null) {
			server.connect(username, password);
		} else {
			server.connect();
		}
		stopWatch.stop();
		System.out.println("used: " + stopWatch.getTime());
		searchService = server.getSearchService();
		request = new DataRetrievalRequest();
		CQLQuery query = new CQLQuery();
		query.setTarget(new gov.nih.nci.cagrid.cqlquery.Object());
		String target = "gov.nih.nci.caarray.domain.project.Experiment";
		query.getTarget().setName(target);
		List list = searchService.search(query);
		ArrayList experimentsWithDataList = new ArrayList();
		for (Object o : list) {
			Experiment e = ((Experiment) o);
			Set<Hybridization> h1 = (((Experiment) o).getHybridizations());
			if (h1.size() > 0) {
				String[] hybridizationValues = new String[h1.size()];
				TreeSet<String> set = new TreeSet<String>();
				for (Hybridization h : h1) {
					set.add(h.getName());
				}
				experimentsWithDataList.add(o);
			}
			Set<Source> sources = e.getSources();
			if (sources != null && sources.size() > 0) {
				for (Source s : sources) {
					List<Source> slist = searchService.search(s);
					if (slist != null && slist.size() > 0)
						for (int i = 0; i < slist.size(); i++)
							System.out.println(i + " Source "
									+ slist.get(i).getName() + "|"
									+ slist.get(i).getTissueSite().getValue());

				}
			}
		}
		return list;
	}

	public List getAssocations(String url, int port, String username,
			String password, String type, String value) throws Exception {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		CaArrayServer server = new CaArrayServer(url, port);
		CaArraySearchService searchService;// = server.getSearchService();
		DataRetrievalRequest request = new DataRetrievalRequest();
		if (username != null) {
			server.connect(username, password);
		} else {
			server.connect();
		}
		stopWatch.stop();
		System.out.println("used: " + stopWatch.getTime());
		searchService = server.getSearchService();
		request = new DataRetrievalRequest();

		CQLQuery query = new CQLQuery();
		gov.nih.nci.cagrid.cqlquery.Object object = new gov.nih.nci.cagrid.cqlquery.Object();
		String target = "gov.nih.nci.caarray.domain.project.Experiment";

		

		if (type.equalsIgnoreCase(CaARRAYQueryPanel.TISSUETYPE)) {
			Association termAssociation = new Association();
			termAssociation.setName("gov.nih.nci.caarray.domain.vocabulary.Term");
			Attribute termAttribute = new Attribute();
			termAttribute.setName("value");
			termAttribute.setValue("Prostate");
			termAttribute.setPredicate(Predicate.EQUAL_TO);
			termAssociation.setAttribute(termAttribute);
			termAssociation.setRoleName("tissueSite");
			Association sourceAssociation = new Association();
			sourceAssociation.setName("gov.nih.nci.caarray.domain.sample.Source");
			sourceAssociation.setRoleName("sources");
			sourceAssociation.setAssociation(termAssociation);
			object.setAssociation(sourceAssociation);

			object.setName(target);
			query.setTarget(object);
		} else if (type.equalsIgnoreCase(CaARRAYQueryPanel.PINAME)) {
			Association termAssociation = new Association();
			termAssociation.setName("gov.nih.nci.caarray.domain.vocabulary.Term");
			Attribute termAttribute = new Attribute();
			termAttribute.setName("value");
			termAttribute.setValue("Prostate");
			termAttribute.setPredicate(Predicate.EQUAL_TO);
			termAssociation.setAttribute(termAttribute);
			termAssociation.setRoleName("tissueSite");
			Association sourceAssociation = new Association();
			sourceAssociation.setName("gov.nih.nci.caarray.domain.sample.Source");
			sourceAssociation.setRoleName("sources");
			sourceAssociation.setAssociation(termAssociation);
			object.setAssociation(sourceAssociation);

			object.setName(target);
			
		} 
		query.setTarget(object);
		List list = searchService.search(query);
		ArrayList experimentsWithDataList = new ArrayList();
		for (Object o : list) {
			Experiment e = ((Experiment) o);
			Set<Hybridization> h1 = (((Experiment) o).getHybridizations());
			if (h1.size() > 0) {
				String[] hybridizationValues = new String[h1.size()];
				TreeSet<String> set = new TreeSet<String>();
				for (Hybridization h : h1) {
					set.add(h.getName());
				}
				experimentsWithDataList.add(o);
			}
//			Set<Source> sources = e.getSources();
//			if (sources != null && sources.size() > 0) {
//				for (Source s : sources) {
//					List<Source> slist = searchService.search(s);
//					if (slist != null && slist.size() > 0)
//						for (int i = 0; i < slist.size(); i++)
//							System.out.println(i + " Source "
//									+ slist.get(i).getName() + "|"
//									+ slist.get(i).getTissueSite().getValue());
//
//				}
//			}

			ExperimentContact contact = e.getPrimaryInvestigator();
			List<ExperimentContact> contacts = searchService.search(contact);
			if(contacts!=null && contacts.size()>0){
				for ( ExperimentContact contactss: contacts){
					System.out.println(contactss.getClass() + "|"  + contactss.toString());
 				}
			}
		
		}

		if (username != null) {
			System.out
					.println("With password, the total number of Exp with at least one hybridization is "
							+ experimentsWithDataList.size());
		} else {
			System.out
					.println("At assocation, Without password, the total number of Exp with at least one hybridization "
							+ experimentsWithDataList.size());

		}
		return list;
	}
}
