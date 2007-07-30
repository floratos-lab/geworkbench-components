package org.geworkbench.components.cagrid;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis.message.addressing.EndpointReferenceType;

import edu.columbia.geworkbench.cagrid.cluster.client.HierarchicalClusteringClient;
import gov.nih.nci.cagrid.common.Utils;

/**
 * This class tests the serialization of {@link EndpointReferenceType} and the
 * creation of new clients from an {@link EndpointReferenceType}.
 * 
 * @author keshav
 * @version $Id: EndpointReferenceTypeTest.java,v 1.1 2007-07-30 16:27:58 keshav Exp $
 */
public class EndpointReferenceTypeTest extends TestCase {

	private static final String HIERARCHICAL_CLUSTERING = "http://localhost:8080/wsrf/services/cagrid/HierarchicalClustering";
	// TODO move some of these methods to our own utility class.
	HierarchicalClusteringClient client = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		client = new HierarchicalClusteringClient(HIERARCHICAL_CLUSTERING);

	}

	/**
	 * Tests whether the objects are equal after creating a second client from
	 * the {@link EndpointReferenceType} of the first.
	 * 
	 * @throws Exception
	 */
	public void testEqualityFromEndpointReferenceType() throws Exception {
		EndpointReferenceType epr = client.getEndpointReference();

		HierarchicalClusteringClient aSecondClient = new HierarchicalClusteringClient(
				epr);

		assertEquals(client.getEndpointReference(), aSecondClient
				.getEndpointReference());
	}

	/**
	 * Tests serializing the {@link EndpointReferenceType}.
	 * 
	 * @throws Exception
	 */
	public void testSerializeEndpointReferenceType() {
		EndpointReferenceType epr = client.getEndpointReference();
		boolean fail = false;
		try {
			Utils.serializeDocument("endpointReferenceTest.xml", epr,
					new QName(
							"http://schemas.xmlsoap.org/ws/2004/03/addressing",
							"EndPointReference"));
		} catch (Exception e) {
			fail = true;
		} finally {
			assertFalse(fail);
		}

	}

	/**
	 * Tests creating a client from a saved {@link EndpointReferenceType}.
	 * 
	 * @throws Exception
	 */
	public void testCreateClientFromSerializedEndpointReferenceType()
			throws Exception {
		EndpointReferenceType epr = (EndpointReferenceType) Utils
				.deserializeDocument("endpointReferenceTest.xml",
						EndpointReferenceType.class);

		assertEquals(epr.getAddress().toString(), HIERARCHICAL_CLUSTERING);
	}

}
