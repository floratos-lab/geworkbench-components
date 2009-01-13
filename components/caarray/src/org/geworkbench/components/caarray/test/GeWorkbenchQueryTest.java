package org.geworkbench.components.caarray.test;

import gov.nih.nci.caarray.domain.data.DataSet;
import gov.nih.nci.caarray.domain.data.DerivedArrayData;
import gov.nih.nci.caarray.domain.hybridization.Hybridization;
import gov.nih.nci.caarray.domain.project.Experiment;
import gov.nih.nci.caarray.services.CaArrayServer;
import gov.nih.nci.caarray.services.ServerConnectionException;
import gov.nih.nci.caarray.services.search.CaArraySearchService;
import gov.nih.nci.cagrid.cqlquery.CQLQuery;

import java.rmi.Remote;
import java.util.List;
import java.util.Set;

public class GeWorkbenchQueryTest implements Remote {

	/**
	 * This test includes all the code in geWorkbench that may generate caArray
	 * queries in the case of retrieving the list of experiments
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		long begin = System.currentTimeMillis();

		CaArrayServer server = new CaArrayServer("array.nci.nih.gov", 8080);
//		CaArrayServer server = new CaArrayServer("127.0.0.1", 18083);
		try {
			server.connect();
		} catch (ServerConnectionException e) {
			e.printStackTrace();
			System.exit(1);
		}
		CaArraySearchService service = server.getSearchService();

		CQLQuery query = new CQLQuery();
		gov.nih.nci.cagrid.cqlquery.Object object = new gov.nih.nci.cagrid.cqlquery.Object();
		String target = "gov.nih.nci.caarray.domain.project.Experiment";
		object.setName(target);
		query.setTarget(object);

		List list = service.search(query);

		long firstPart = System.currentTimeMillis();
		System.out.println("First part took " + (firstPart - begin)
				+ " milliseconds");

		for (Object o : list) {

			Set<Hybridization> h1 = (((Experiment) o).getHybridizations());

			if (h1.size() > 0) {
				Hybridization oneHybridization = null;
				for (Hybridization h : h1)
					oneHybridization = h;
				Hybridization hybridization = (Hybridization) (service
						.search(oneHybridization).get(0));
				Set<DerivedArrayData> derivedArrayDataSet = hybridization
						.getDerivedDataCollection();

				DataSet dataSet = null;
				for (DerivedArrayData derivedArrayData : derivedArrayDataSet) {
					DerivedArrayData populatedArrayData = service.search(
							derivedArrayData).get(0);
					dataSet = populatedArrayData.getDataSet();
				}

				if (dataSet != null) {
					DataSet returnDataSet = service.search(dataSet).get(0);
					// the return data set is then used in geWorkbench
					System.out.println("data set returned "+returnDataSet.getCaBigId());
				}
			}
		}
		long secondPart = System.currentTimeMillis();
		System.out.println("Second part took " + (secondPart - firstPart)
				+ " milliseconds");
	}
}
