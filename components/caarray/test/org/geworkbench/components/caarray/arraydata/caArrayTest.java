package org.geworkbench.components.caarray.arraydata;
import gov.nih.nci.caarray.domain.array.AbstractDesignElement;
import gov.nih.nci.caarray.domain.array.AbstractProbe;
import gov.nih.nci.caarray.domain.contact.Person;
import gov.nih.nci.caarray.domain.data.AbstractDataColumn;
import gov.nih.nci.caarray.domain.data.DataSet;
import gov.nih.nci.caarray.domain.data.DerivedArrayData;
import gov.nih.nci.caarray.domain.data.DesignElementList;
import gov.nih.nci.caarray.domain.data.DoubleColumn;
import gov.nih.nci.caarray.domain.data.FloatColumn;
import gov.nih.nci.caarray.domain.data.HybridizationData;
import gov.nih.nci.caarray.domain.data.IntegerColumn;
import gov.nih.nci.caarray.domain.data.LongColumn;
import gov.nih.nci.caarray.domain.data.QuantitationType;
import gov.nih.nci.caarray.domain.hybridization.Hybridization;
import gov.nih.nci.caarray.domain.project.Experiment;
import gov.nih.nci.caarray.domain.project.ExperimentContact;
import gov.nih.nci.caarray.services.CaArrayServer;
import gov.nih.nci.caarray.services.ServerConnectionException;
import gov.nih.nci.caarray.services.search.CaArraySearchService;
import gov.nih.nci.cagrid.cqlquery.Association;
import gov.nih.nci.cagrid.cqlquery.Attribute;
import gov.nih.nci.cagrid.cqlquery.CQLQuery;
import gov.nih.nci.cagrid.cqlquery.Group;
import gov.nih.nci.cagrid.cqlquery.LogicalOperator;
import gov.nih.nci.cagrid.cqlquery.Predicate;

import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.geworkbench.builtin.projects.remoteresources.query.CaARRAYQueryPanel;


public class caArrayTest extends TestCase {
    private static final String DEFAULT_SERVER = "array.nci.nih.gov";
    private static final int DEFAULT_JNDI_PORT = 8080;

    private String hostname = DEFAULT_SERVER;
    private int port = DEFAULT_JNDI_PORT;

	public caArrayTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

    /**
     * Connect to caArray server using caArray Java API
     */
    public void testConnection() {
        CaArrayServer server = new CaArrayServer(hostname, port);
        try {
            server.connect();
//            System.out.println("Successfully connected to caArray server");
        } catch (ServerConnectionException e) {
//            System.out.println("Couldn't connect to server: likely JNDI problem");
            e.printStackTrace(System.err);
        	fail("connect to server");
        }
        try {
            CaArraySearchService searchService = server.getSearchService();
            QuantitationType searchType = new QuantitationType();
            searchType.setTypeClass(Integer.class);
            List<QuantitationType> types = searchService.search(searchType);
            assertTrue(types.size()>0);
//            System.out.println(types);
//            System.out.println("Successfully ran query");
        } catch (Throwable t) {
//            System.out.println("Couldn't run query: likely RMI problem");
            t.printStackTrace(System.err);
        	fail("ran query");
        }
    }

    /**
     * Test connectivity to array-train.nci.nih.gov
     */
    public void testArrayTrain(){
    	hostname = "array-train.nci.nih.gov";
        CaArrayServer server = new CaArrayServer(hostname, port);
        try {
            server.connect();
        } catch (ServerConnectionException e) {
        	fail("connect to server");
        }
        try {
            CaArraySearchService searchService = server.getSearchService();
            QuantitationType searchType = new QuantitationType();
            searchType.setTypeClass(Integer.class);
            List<QuantitationType> types = searchService.search(searchType);
            assertTrue(types.size()>0);
        } catch (Throwable t) {
        	fail("ran query");
        }    	
    }

    /**
     * Test connectivity to afapp1.c2b2.columbia.edu
     */
    public void testArrayAfapp1(){
    	hostname = "afapp1.c2b2.columbia.edu";
    	port = 31099;
        CaArrayServer server = new CaArrayServer(hostname, port);
        try {
            server.connect();
        } catch (ServerConnectionException e) {
        	fail("connect to server");
        }
        try {
            CaArraySearchService searchService = server.getSearchService();
            QuantitationType searchType = new QuantitationType();
            searchType.setTypeClass(Integer.class);
            List<QuantitationType> types = searchService.search(searchType);
            assertTrue(types.size()>0);
        } catch (Throwable t) {
        	fail("ran query");
        }    	
    }

    /**
     * Get the list of experiments on array-train.nci.nih.gov
     */
    public void testGetExperimentList(){
    	hostname = "array-train.nci.nih.gov";
    	String username = null;
    	String password = null;
		CaArrayServer server = new CaArrayServer(hostname, port);
		try {
			if (username == null || username.trim().length() == 0) {
				server.connect();// disable a user login.
			} else {
				server.connect(username, password);
			}
			CaArraySearchService service = server.getSearchService();

			CQLQuery query = CQLQueryGenerator
					.generateQuery(CQLQueryGenerator.EXPERIMENT);
			long time0 = System.currentTimeMillis();
			List list = service.search(query);
			long time1 = System.currentTimeMillis();
			assertTrue(list.size()>0);
			System.out.println("list size "+list.size()+" returned in "+(time1-time0)+" milliseconds");
			for (Object o : list) {
				Experiment e = ((Experiment) o);
				System.out.println(new Date()+ " we got Exp: "+e.getTitle());
				Set<Hybridization> h1 = (((Experiment) o).getHybridizations());
				System.out.println("Including "+h1.size()+" arrays");
				if (h1.size() > 0) {
					for (Hybridization h : h1) {
//						System.out.println("We got an array: "+h.getName());
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace();
			fail();
		}
    }

    /**
     * Try get first experiment from array-train.nci.nih.gov
     */
    public void testGetExperiment(){
    	hostname = "array-train.nci.nih.gov";
    	String username = null;
    	String password = null;
    	DataSet dataset = null;
    	Hybridization testH = null;
		CaArrayServer server = new CaArrayServer(hostname, port);
		try {
			if (username == null || username.trim().length() == 0) {
				server.connect();// disable a user login.
			} else {
				server.connect(username, password);
			}
			CaArraySearchService service = server.getSearchService();

			CQLQuery query = CQLQueryGenerator
					.generateQuery(CQLQueryGenerator.EXPERIMENT);
			long time0 = System.currentTimeMillis();
			List list = service.search(query);
			long time1 = System.currentTimeMillis();
			assertTrue(list.size()>0);
			System.out.println("list size "+list.size()+" returned in "+(time1-time0)+" milliseconds");
			for (Object o : list) {
				Experiment e = ((Experiment) o);
				System.out.println(new Date()+ " we got Exp: "+e.getTitle());
				Set<Hybridization> h1 = (((Experiment) o).getHybridizations());
				System.out.println("Including "+h1.size()+" arrays");
				if (h1.size() > 0) {
					for (Hybridization h : h1) {
						System.out.println("We got an array: "+h.getName());
						testH = h;
						break;
					}
				}
				break;
			}
		}catch (Exception e){
			e.printStackTrace();
			fail();
		}

		CaArraySearchService service = server.getSearchService();
//		dataset = getDataSet(service,testH);
/* I don't know why above line doesn't work */		
		Hybridization hybridization = new Hybridization();
		// using ID to rebuild the Hybridization object instead of using name.
		hybridization.setId(testH.getId());
		List<Hybridization> set = service.search(hybridization);
		if (set == null || set.size() == 0) {
			return;
		}
		hybridization = service.search(hybridization).get(0);
/* but doing it indirectly (using above 8 lines) would work */		
		dataset = getDataSet(service,hybridization);
		assertNotNull("Get dataset",dataset);
		
		AbstractProbe[] markersArray;
		DesignElementList designElementList = dataset.getDesignElementList();
		List<DesignElementList> designElementLists = service
				.search(designElementList);
		DesignElementList designElements = designElementLists.get(0);
		List<AbstractDesignElement> list = designElements
				.getDesignElements();
		markersArray = new AbstractProbe[list.size()];
		markersArray = list.toArray(markersArray);
		// Add populate probe and get annotation later.

		// Below is the code to get the values for the quantitationType.
		for (HybridizationData oneHybData : dataset.getHybridizationDataList()) {
			HybridizationData populatedHybData = service.search(oneHybData)
					.get(0);
			double[] doubleValues = new double[markersArray.length];
			// Get each column in the HybridizationData.
			for (AbstractDataColumn column : populatedHybData.getColumns()) {
				AbstractDataColumn populatedColumn = service.search(column)
						.get(0);
				// Find the type of the column.
				QuantitationType qType = populatedColumn
						.getQuantitationType();
				if (qType.getName().equalsIgnoreCase("CHPSignal")) {
					Class<?> typeClass = qType.getTypeClass();
					// Retrieve the appropriate data depending
					// on the type of the column.
					if (typeClass == Float.class) {
						float[] values = ((FloatColumn) populatedColumn)
								.getValues();
						for (int i = 0; i < values.length; i++) {
							doubleValues[i] = values[i];
						}

					} else if (typeClass == Integer.class) {
						int[] values = ((IntegerColumn) populatedColumn)
								.getValues();
						for (int i = 0; i < values.length; i++) {
							doubleValues[i] = values[i];
						}

					} else if (typeClass == Long.class) {
						long[] values = ((LongColumn) populatedColumn)
								.getValues();
						for (int i = 0; i < values.length; i++) {
							doubleValues[i] = values[i];
						}

					} else if (typeClass == Double.class) {
						doubleValues = ((DoubleColumn) populatedColumn)
								.getValues();
					}

					assertTrue("marker values",doubleValues.length>0);
					//Let's only print the first 10 lines, to prove it works.
					int numLines = 10;
					if (numLines > doubleValues.length) numLines = doubleValues.length;
					for (int i = 0; i < numLines; i++) {
						System.out.println(markersArray[i].getName()
								+ "\t" + doubleValues[i]);
					}
				}
			}
		}
    }

	private static DataSet getDataSet(CaArraySearchService service,
			Hybridization hybridization) {
		DataSet dataSet = null;

		Set<DerivedArrayData> derivedArrayDataSet = hybridization
				.getDerivedDataCollection();
		for (DerivedArrayData derivedArrayData : derivedArrayDataSet) {
			// Return the data set associated with the first derived data.
			DerivedArrayData populatedArrayData = service.search(
					derivedArrayData).get(0);
			dataSet = populatedArrayData.getDataSet();
		}

		if (dataSet == null) {
			return null;
		} else {
			return service.search(dataSet).get(0);
		}
	}

    /**
     * Get the list of Persons on array-train.nci.nih.gov
     */
    public void testGetPersonListByPIName(){
    	hostname = "array-train.nci.nih.gov";
    	String username = null;
    	String password = null;
		CaArrayServer server = new CaArrayServer(hostname, port);
		try {
			if (username == null || username.trim().length() == 0) {
				server.connect();// disable a user login.
			} else {
				server.connect(username, password);
			}
			CaArraySearchService service = server.getSearchService();

			CQLQuery query = new CQLQuery();
			gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
			target.setName("gov.nih.nci.caarray.domain.contact.Person");
			Attribute att = new Attribute(); 
			att.setName("firstName"); 
			att.setPredicate(Predicate.EQUAL_TO); 
			att.setValue("Don"); 
			target.setAttribute(att);
			query.setTarget(target);
			
			long time0 = System.currentTimeMillis();
			List list = service.search(query);
			long time1 = System.currentTimeMillis();
			assertTrue(list.size()>0);
			System.out.println("list size "+list.size()+" returned in "+(time1-time0)+" milliseconds");
			for (Object o : list) {
				Person e = ((Person) o);
				System.out.println(new Date()+ " we got Person #"+e.getId()+": firstName = "+e.getFirstName()+", lastName = "+e.getLastName());
			}
		}catch (Exception e){
			e.printStackTrace();
			fail();
		}
    }

    /**
     * Get the list of ExperimentContact on array-train.nci.nih.gov
     */
    public void testGetExperimentContactListByPIName(){
    	hostname = "array-train.nci.nih.gov";
    	String username = null;
    	String password = null;
		CaArrayServer server = new CaArrayServer(hostname, port);
		try {
			if (username == null || username.trim().length() == 0) {
				server.connect();// disable a user login.
			} else {
				server.connect(username, password);
			}
			CaArraySearchService service = server.getSearchService();

			CQLQuery query = new CQLQuery();
			gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
			target.setName("gov.nih.nci.caarray.domain.project.ExperimentContact");
			Association organismAssociation = new Association();
			organismAssociation
					.setName("gov.nih.nci.caarray.domain.contact.Person");

			Attribute att = new Attribute(); 
			att.setName("firstName"); 
			att.setPredicate(Predicate.EQUAL_TO); 
			att.setValue("Don"); 
			organismAssociation.setAttribute(att);
			organismAssociation.setRoleName("contact");
			target.setAssociation(organismAssociation);
			query.setTarget(target);
			
			long time0 = System.currentTimeMillis();
			List list = service.search(query);
			long time1 = System.currentTimeMillis();
			assertTrue(list.size()>0);
			System.out.println("list size "+list.size()+" returned in "+(time1-time0)+" milliseconds");
			for (Object o : list) {
				ExperimentContact e = ((ExperimentContact) o);
				System.out.println(new Date()+ " we got ExperimentContact: "+e.getExperiment());
			}
		}catch (Exception e){
			e.printStackTrace();
			fail();
		}
    }

    /**
     * Get the list of Experiment on array-train.nci.nih.gov
     */
    public void testGetExperimentListByPIName(){
    	hostname = "array-train.nci.nih.gov";
    	String username = null;
    	String password = null;
		CaArrayServer server = new CaArrayServer(hostname, port);
		try {
			if (username == null || username.trim().length() == 0) {
				server.connect();// disable a user login.
			} else {
				server.connect(username, password);
			}
			CaArraySearchService service = server.getSearchService();

			CQLQuery query = new CQLQuery();
			gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
			target.setName("gov.nih.nci.caarray.domain.project.Experiment");
			Association contactAssociation = new Association();
			contactAssociation
					.setName("gov.nih.nci.caarray.domain.project.ExperimentContact");
			Association organismAssociation = new Association();
			organismAssociation
					.setName("gov.nih.nci.caarray.domain.contact.Person");
			Attribute att = new Attribute(); 
			att.setName("firstName"); 
			att.setPredicate(Predicate.EQUAL_TO); 
			att.setValue("Don"); 
			organismAssociation.setAttribute(att);
			organismAssociation.setRoleName("contact");
			contactAssociation.setAssociation(organismAssociation);
			contactAssociation.setRoleName("experimentContacts");
			target.setAssociation(contactAssociation);
			query.setTarget(target);
			
			long time0 = System.currentTimeMillis();
			List list = service.search(query);
			long time1 = System.currentTimeMillis();
			assertTrue(list.size()>0);
			System.out.println("list size "+list.size()+" returned in "+(time1-time0)+" milliseconds");
			for (Object o : list) {
				Experiment e = (Experiment) o;
				System.out.println(new Date()+ " we got Experiment: "+e.getTitle());
			}
		}catch (Exception e){
			e.printStackTrace();
			fail();
		}
    }

    /**
     * Get the list of Experiment on array-train.nci.nih.gov
     */
    public void testGetExperimentListByPIFirstLastName(){
    	hostname = "array-train.nci.nih.gov";
    	String username = null;
    	String password = null;
		CaArrayServer server = new CaArrayServer(hostname, port);
		try {
			if (username == null || username.trim().length() == 0) {
				server.connect();// disable a user login.
			} else {
				server.connect(username, password);
			}
			CaArraySearchService service = server.getSearchService();

			CQLQuery query = new CQLQuery();
			gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
			target.setName("gov.nih.nci.caarray.domain.project.Experiment");
			Association contactAssociation = new Association();
			contactAssociation
					.setName("gov.nih.nci.caarray.domain.project.ExperimentContact");
			Association organismAssociation = new Association();
			organismAssociation
					.setName("gov.nih.nci.caarray.domain.contact.Person");
			Attribute att = new Attribute(); 
			att.setName("firstName"); 
			att.setPredicate(Predicate.EQUAL_TO); 
			att.setValue("Don"); 
			Attribute att2 = new Attribute(); 
			att2.setName("lastName"); 
			att2.setPredicate(Predicate.EQUAL_TO); 
			att2.setValue("Swan");
			Attribute[] atts = new Attribute[2];
			atts[0]=att;
			atts[1]=att2;
			Group group = new Group();
			group.setAttribute(atts);
			group.setLogicRelation(LogicalOperator.AND);
			organismAssociation.setGroup(group);
			organismAssociation.setRoleName("contact");
			contactAssociation.setAssociation(organismAssociation);
			contactAssociation.setRoleName("experimentContacts");
			target.setAssociation(contactAssociation);
			query.setTarget(target);
			
			long time0 = System.currentTimeMillis();
			List list = service.search(query);
			long time1 = System.currentTimeMillis();
			assertTrue(list.size()>0);
			System.out.println("list size "+list.size()+" returned in "+(time1-time0)+" milliseconds");
			for (Object o : list) {
				Experiment e = (Experiment) o;
				System.out.println(new Date()+ " we got Experiment: "+e.getTitle());
			}
		}catch (Exception e){
			e.printStackTrace();
			fail();
		}
    }

    /**
     * test CQLQueryGenerator
     */
    public void testCQLQueryGenerator(){
    	String username = null;
    	String password = null;
    	hostname = "array-train.nci.nih.gov";
    	String targetname = CQLQueryGenerator.EXPERIMENT;
		CaArrayServer server = new CaArrayServer(hostname, port);
		try {
			System.out.println("testing query by organism using CQLQueryGenerator");
			if (username == null || username.trim().length() == 0) {
				server.connect();// disable a user login.
			} else {
				server.connect(username, password);
			}
			CaArraySearchService service = server.getSearchService();
			CQLQuery query = CQLQueryGenerator.generateQuery(targetname, CaARRAYQueryPanel.ORGANISM, "human");
			long time0 = System.currentTimeMillis();
			List list = service.search(query);
			long time1 = System.currentTimeMillis();
			System.out.println("list size "+list.size()+" returned in "+(time1-time0)+" milliseconds");
			assertTrue(list.size()>0);

			System.out.println("testing query by chip provider using CQLQueryGenerator");
			query = CQLQueryGenerator.generateQuery(targetname, CaARRAYQueryPanel.CHIPPROVIDER, "Affymetrix");
			time0 = System.currentTimeMillis();
			list = service.search(query);
			time1 = System.currentTimeMillis();
			System.out.println("list size "+list.size()+" returned in "+(time1-time0)+" milliseconds");
			assertTrue(list.size()>0);

			
		}catch (Exception e){
			e.printStackTrace();
			fail();
		}
    }    

}
