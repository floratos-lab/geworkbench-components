package org.geworkbench.service.client;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.geworkbench.components.anova.data.AnovaInput;
import org.geworkbench.components.anova.data.AnovaOutput;

public class AnovaClient {

	/**
	 * @param args
	 * @throws AxisFault
	 */
	public static void main(String[] args1) {		 

		float[][] A = { { 3.1f, 30.1f, 22.2f, 11.7f, 15.3f, 27.1f },
				{ 53.2f, 7.3f, 23.5f, 17.3f, 5.1f, 21.8f },
				{ 473.4f, 324.4f, 329.9f, 159.f, 225.5f, 272.8f },
				{ 1753.2f, 1611.5f, 1619.9f, 1420.6f, 2010.0f, 1518.4f },
				{ 284.2f, 276.0f, 307.8f, 306.3f, 328.5f, 529.9f },
				{ 547.3f, 582.1f, 660.3f, 564.8f, 508.4f, 657.2f },
				{ 169.1f, 152.2f, 218.9f, 225.7f, 257.7f, 272.5f },
				{ 190.4f, 189.6f, 180.3f, 172.2f, 191.1f, 182.1f } };

		int[] groupAssignments = { 1, 1, 2, 2, 3, 3 };
		int numGenes = 8;
		int numSelectedGroups = 3;
		double pvalueth = 0.05;
		int pValueEstimation = 0;
		int permutationsNumber = 100;
		int falseDiscoveryRateControl = 0;
		float falseSignificantGenesLimit = 10.0f;

		AnovaInput input = new AnovaInput();
		input.setA(A);
		input.setFalseDiscoveryRateControl(falseDiscoveryRateControl);
		input.setFalseSignificantGenesLimit(falseSignificantGenesLimit);
		input.setGroupAssignments(groupAssignments);
		input.setNumGenes(numGenes);
		input.setNumSelectedGroups(numSelectedGroups);
		input.setPermutationsNumber(permutationsNumber);
		input.setPValueEstimation(pValueEstimation);
		input.setPvalueth(pvalueth);
		 
		RPCServiceClient serviceClient;
		
		try {
			serviceClient = new RPCServiceClient();

			Options options = serviceClient.getOptions();

			EndpointReference targetEPR = new EndpointReference(
					"http://localhost:8080/axis2/services/AnovaService");
			options.setTo(targetEPR);

			// notice that that namespace is in the required form
			QName opName = new QName(
					"http://service.anova.components.geworkbench.org",
					"execute");
			Object[] args = new Object[] { input };

			Class<?>[] returnType = new Class[] { AnovaOutput.class };

			Object[] response = serviceClient.invokeBlocking(opName, args,
					returnType);
			AnovaOutput output = (AnovaOutput) response[0];
			System.out.println(output.toString());
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			// System.out.println( e );
			OMElement x = e.getDetail();
			if (x != null)
				System.out.println(x);

			Throwable y = e.getCause();
			while (y != null) {
				y.printStackTrace();
				y = y.getCause();
			}

			System.out.println("message: " + e.getMessage());
			System.out.println("fault action: " + e.getFaultAction());
			System.out.println("reason: " + e.getReason());
			e.printStackTrace();
		}
	}

}
