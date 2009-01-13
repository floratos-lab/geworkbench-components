package org.geworkbench.components.caarray.test;

import java.util.List;
import java.util.Set;

import gov.nih.nci.caarray.domain.array.AbstractDesignElement;
import gov.nih.nci.caarray.domain.array.AbstractProbe;
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
import gov.nih.nci.caarray.services.CaArrayServer;
import gov.nih.nci.caarray.services.search.CaArraySearchService;

class GeWorkbenchGetDataTest {
	public static void main(String[] args) {
		String url = "array.nci.nih.gov";
		int port = 8080;
		String hybridizationStr = "E01_U133P2";
		String quantitationType = "CHPSignal";

		long time0 = System.currentTimeMillis();
		CaArrayServer server = new CaArrayServer(url, port);
		try {
			server.connect();// disable a user login.
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		CaArraySearchService service = server.getSearchService();

		long time1 = System.currentTimeMillis();
		long duration1 = (time1 - time0);
		System.out.println("It took " + duration1
				+ " milliseconds to get connection and service.");

		AbstractProbe[] markersArray;
		Hybridization hybridization = new Hybridization();
		hybridization.setName(hybridizationStr);
		List<Hybridization> set = service.search(hybridization);
		if (set == null || set.size() == 0) {
			System.out.println("No hybridization set is returned.");
			return;
		}

		hybridization = service.search(hybridization).get(0);
		DataSet dataSet = null;

		Set<DerivedArrayData> derivedArrayDataSet = hybridization
				.getDerivedDataCollection();

		for (DerivedArrayData derivedArrayData : derivedArrayDataSet) {

			DerivedArrayData populatedArrayData = service.search(
					derivedArrayData).get(0);
			dataSet = populatedArrayData.getDataSet();
			List<DataSet> dataSetList = service.search(dataSet);
			DataSet data = dataSetList.get(0);

			DesignElementList designElementList = data.getDesignElementList();
			List<DesignElementList> designElementLists = service
					.search(designElementList);
			DesignElementList designElements = designElementLists.get(0);
			List<AbstractDesignElement> list = designElements
					.getDesignElements();
			markersArray = new AbstractProbe[list.size()];
			markersArray = list.toArray(markersArray);

			for (HybridizationData oneHybData : data.getHybridizationDataList()) {
				HybridizationData populatedHybData = service.search(oneHybData)
						.get(0);
				double[] doubleValues = new double[markersArray.length];

				for (AbstractDataColumn column : populatedHybData.getColumns()) {
					AbstractDataColumn populatedColumn = service.search(column)
							.get(0);
					QuantitationType qType = populatedColumn
							.getQuantitationType();
					if (qType.getName().equalsIgnoreCase(quantitationType)) {
						Class<?> typeClass = qType.getTypeClass();
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

						// markersArray[i] and doubleValues[i]
						// are subsequently used in geWorkbench
					}
				}
			}

		}
		long time2 = System.currentTimeMillis();
		long duration2 = (time2 - time1);
		System.out.println("It took " + duration2
				+ " milliseconds to finish all the queries to return data.");
		System.out.println("Totally time " + (duration1+duration2));
	} // end of main
}