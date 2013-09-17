package org.geworkbench.service.client;

//import javax.xml.namespace.QName;

//import org.apache.axiom.om.OMElement;
//import org.apache.axis2.AxisFault;
//import org.apache.axis2.addressing.EndpointReference;
//import org.apache.axis2.client.Options;
//import org.apache.axis2.rpc.client.RPCServiceClient;
import org.geworkbench.components.anova.data.AnovaInput;
import org.geworkbench.components.anova.data.AnovaOutput;
import org.geworkbench.components.anova.Anova;
import org.geworkbench.components.anova.AnovaException;
import org.geworkbench.components.anova.FalseDiscoveryRateControl;
import org.geworkbench.components.anova.PValueEstimation;

public class AnovaClient {

	 
	/*float[][] A = { { 3.1f, 30.1f, 22.2f, 11.7f, 15.3f, 27.1f },
	{ 53.2f, 7.3f, 23.5f, 17.3f, 5.1f, 21.8f },
	{ 473.4f, 324.4f, 329.9f, 159.f, 225.5f, 272.8f },
	{ 1753.2f, 1611.5f, 1619.9f, 1420.6f, 2010.0f, 1518.4f },
	{ 284.2f, 276.0f, 307.8f, 306.3f, 328.5f, 529.9f },
	{ 547.3f, 582.1f, 660.3f, 564.8f, 508.4f, 657.2f },
	{ 169.1f, 152.2f, 218.9f, 225.7f, 257.7f, 272.5f },
	{ 190.4f, 189.6f, 180.3f, 172.2f, 191.1f, 182.1f } }; */


float[][] A ={{34.7f,46.3f,10.6f,13.3f,32.0f,13.3f},
{44.4f,19.0f,11.4f,8.0f,12.4f,6.7f},
{20.3f,28.7f,4.4f,21.8f,16.1f,8.1f},
{7.8f,16.3f,68.8f,32.1f,1.4f,28.5f},
{314.3f,102.7f,158.3f,241.8f,152.7f,258.0f},
{752.3f,136.4f,192.1f,336.8f,257.2f,319.4f},
{371.6f,43.2f,26.9f,177.1f,110.7f,248.1f},
{2838.4f,874.3f,1251.4f,1653.6f,1162.7f,1687.1f},
{2342.0f,668.5f,884.7f,1364.7f,982.5f,1392.8f},
{2357.4f,719.1f,1113.2f,1504.7f,1155.0f,1338.2f},
{11795.0f,3265.2f,4808.8f,4755.0f,4520.5f,5583.9f},
{24428.5f,7440.8f,9763.0f,9030.9f,10065.8f,14271.6f},
{21248.9f,8305.2f,9183.7f,7439.5f,8800.6f,11514.8f},
{35.8f,31.5f,87.7f,40.5f,37.9f,53.1f},
{21.3f,61.1f,107.9f,58.1f,19.0f,97.2f},
{24.3f,23.0f,55.7f,22.5f,16.7f,19.3f},
{8.6f,16.5f,104.8f,12.0f,11.1f,13.4f},
{11.8f,5.9f,10.8f,6.3f,15.0f,10.0f},
{173.2f,100.4f,34.7f,248.4f,130.6f,94.8f},
{95.5f,106.0f,173.3f,161.9f,116.5f,115.8f},
{138.8f,124.9f,79.3f,101.6f,123.8f,135.4f},
{109.2f,178.1f,96.1f,179.1f,257.2f,253.7f},
{5876.9f,1907.3f,2027.6f,292.6f,320.9f,6112.9f},
{26.9f,32.5f,34.7f,28.6f,38.0f,22.4f},
{48.9f,40.8f,39.7f,37.7f,31.8f,45.3f},
{3.9f,19.1f,7.2f,3.5f,6.3f,5.7f},
{22.2f,22.1f,5.4f,10.6f,2.1f,3.5f},
{6.8f,7.7f,11.7f,7.7f,30.3f,25.4f},
{18.7f,15.6f,19.3f,2.7f,10.3f,1.3f},
{4.5f,3.0f,3.5f,2.7f,3.0f,5.1f},
{4.2f,2.6f,4.0f,1.7f,1.8f,7.9f},
{51.3f,36.1f,20.3f,47.5f,15.9f,21.0f},
{14.9f,7.9f,4.7f,9.6f,7.7f,14.0f},
{3.8f,21.7f,5.8f,3.3f,17.2f,13.1f},
{14.1f,8.9f,8.8f,8.7f,5.1f,5.7f},
{13.0f,40.5f,48.5f,9.1f,13.2f,38.5f},
{3.2f,7.8f,6.1f,7.4f,2.8f,3.2f},
{13.7f,3.9f,3.8f,1.0f,0.7f,4.3f},
{37.7f,70.0f,70.0f,46.4f,50.0f,137.2f},
{27.0f,184.2f,191.6f,99.0f,125.6f,758.9f},
{71.8f,142.6f,148.7f,150.2f,119.0f,399.3f},
{131.9f,239.8f,325.9f,271.5f,240.2f,1477.3f},
{237.7f,11.3f,5.3f,47.0f,69.3f,21.7f},
{223.6f,5.0f,8.8f,23.4f,88.0f,6.0f},
{235.7f,88.4f,117.4f,67.6f,117.4f,15.4f},
{5581.1f,5072.0f,6789.4f,6305.8f,5307.2f,5645.5f},
{5773.0f,4631.1f,5196.8f,4529.8f,5098.9f,5986.4f},
{7468.9f,5470.0f,7141.7f,5582.5f,6058.2f,7039.1f},
{8284.1f,5980.9f,7966.5f,7076.5f,6192.1f,11534.1f},
{10358.9f,7852.2f,8195.0f,7126.8f,8358.5f,11707.0f},
{14067.2f,8168.9f,7050.6f,7606.4f,8121.3f,12829.5f},
{113.5f,145.5f,99.8f,155.1f,147.2f,97.5f},
{46.4f,48.6f,35.6f,42.9f,50.9f,57.7f},
{146.1f,178.6f,191.0f,219.9f,161.0f,150.4f},
{4038.3f,569.8f,590.5f,762.0f,894.0f,181.1f},
{1342.4f,3270.3f,3389.5f,4231.7f,3918.4f,353.6f},
{256.9f,111.2f,178.4f,268.9f,203.0f,61.3f},
{567.4f,346.9f,288.1f,441.0f,562.3f,227.5f},
{15.2f,14.9f,80.6f,38.7f,46.2f,12.5f},
{98.4f,141.8f,82.5f,103.1f,145.1f,88.1f},
{98.9f,114.0f,99.4f,119.2f,126.3f,64.5f},
{27.3f,23.3f,22.8f,68.5f,129.3f,35.4f},
{110.5f,50.7f,86.6f,87.8f,90.4f,62.6f},
{37.8f,23.5f,29.0f,3.8f,17.7f,14.2f},
{34.8f,3.8f,2.7f,1.2f,2.7f,4.6f},
{45.4f,32.2f,26.0f,35.2f,22.8f,39.1f},
{9.2f,10.4f,7.8f,21.2f,28.5f,32.2f},
{1.2f,1.2f,1.7f,2.6f,0.6f,1.4f},
{5.6f,42.1f,16.7f,16.3f,3.1f,8.1f},
{31.0f,7.1f,6.4f,15.1f,10.3f,19.5f},
{12.2f,7.9f,2.0f,7.5f,9.1f,11.3f},
{12.8f,7.5f,5.1f,5.1f,8.8f,6.0f},
{33.0f,40.5f,53.2f,44.3f,34.0f,63.2f},
{16.2f,14.7f,34.8f,10.5f,13.4f,12.9f},
{71.6f,13.3f,48.1f,54.5f,34.5f,7.5f},
{1589.9f,2105.4f,4161.2f,2429.3f,2899.7f,1203.5f},
{2.3f,1.7f,1.3f,1.1f,1.1f,3.8f},
{364.9f,526.6f,489.6f,383.6f,269.1f,306.6f},
{2.2f,1.7f,2.7f,2.6f,0.9f,2.4f},
{43.2f,212.9f,246.6f,422.6f,178.7f,262.8f},
{351.5f,121.4f,171.5f,386.0f,251.8f,63.0f},
{81.7f,101.4f,53.7f,42.0f,29.1f,41.2f},
{13.1f,15.3f,13.1f,5.0f,7.1f,17.1f},
{54.0f,33.1f,27.1f,39.0f,38.7f,39.8f},
{17.5f,108.6f,9.5f,101.6f,59.0f,25.4f},
{14.7f,24.9f,11.0f,26.5f,10.5f,20.2f},
{50.4f,77.7f,62.2f,60.7f,40.6f,31.8f},
{5.0f,3.4f,1.4f,2.9f,4.1f,4.9f},
{45.7f,53.3f,30.8f,78.6f,69.2f,85.9f},
{31.1f,25.9f,23.0f,23.8f,9.2f,5.6f},
{20730.9f,7346.3f,6881.4f,6435.3f,7243.7f,16067.2f},
{4.3f,3.8f,26.8f,3.8f,3.7f,35.0f},
{14.6f,17.5f,5.9f,6.5f,4.9f,3.5f},
{26.5f,11.4f,6.7f,22.4f,4.9f,12.9f},
{15.2f,13.8f,3.6f,12.7f,3.9f,4.4f},
{120.8f,98.9f,131.1f,122.7f,81.9f,97.3f},
{10.1f,3.4f,6.4f,2.2f,4.1f,24.1f},
{93.6f,140.8f,123.3f,182.5f,51.0f,192.3f},
{6.6f,19.3f,22.5f,40.5f,27.0f,36.6f},
{8.1f,8.8f,6.1f,6.9f,6.6f,10.6f},
{20.1f,7.0f,15.3f,37.3f,22.0f,27.9f},
{168.1f,55.1f,45.5f,27.2f,32.3f,40.1f},
{90.3f,127.5f,100.6f,90.7f,95.7f,211.3f},
{10.9f,3.0f,15.0f,11.7f,10.0f,17.7f},
{157.9f,90.8f,137.2f,170.0f,127.5f,154.6f},
{17.4f,29.4f,52.4f,3.7f,20.0f,13.5f},
{110.5f,81.3f,86.2f,153.4f,33.6f,244.5f},
{377.1f,403.9f,410.1f,535.9f,644.2f,475.3f},
{7.8f,3.2f,7.1f,9.1f,7.1f,8.7f},
{49.7f,17.7f,67.9f,68.2f,45.4f,13.4f},
{285.4f,224.8f,243.9f,219.9f,212.2f,192.4f},
{16.7f,4.1f,5.3f,3.7f,5.1f,14.8f},
{44.0f,25.7f,12.4f,38.2f,27.7f,12.2f},
{162.9f,254.4f,224.2f,190.1f,196.2f,116.1f},
{29.0f,26.1f,41.6f,16.8f,33.2f,31.1f},
{16.5f,18.0f,19.3f,17.3f,14.7f,12.6f},
{51.1f,54.9f,51.1f,67.5f,46.4f,72.7f},
{2.2f,32.1f,5.2f,4.7f,37.0f,4.1f},
{9.7f,3.4f,3.7f,16.0f,21.5f,9.0f},
{4.2f,3.5f,1.4f,2.0f,1.6f,1.0f},
{35.5f,33.0f,33.1f,33.0f,6.8f,43.6f}}; 

int[] groupAssignments = { 1, 1, 2, 2, 3, 3 };
int numGenes = 121;
int numSelectedGroups = 3;
double pvalueth = 0.05;
int pValueEstimation = PValueEstimation.fdistribution.ordinal();
int permutationsNumber = 100;
int falseDiscoveryRateControl = FalseDiscoveryRateControl.number.ordinal();
float falseSignificantGenesLimit = 121;
	
	/**
	 * @param args
	 * @throws AxisFault
	 */
	
	/*public void runAnova()
	{
		for(int i=0; i<1; i++)
		{	
			AnovaThread t = new AnovaThread();
		    t.start();
		}
	}*/
	
	public void runAnovaLocal()
	{
		for(int i=0; i<1; i++)
		{	
			AnovaLocalThread t = new AnovaLocalThread();
		    t.start();
		}
	}
	
	public static void main(String[] args1) {		 
     
		AnovaClient ac = new AnovaClient();
		ac.runAnovaLocal();
		//ac.runAnova();
	}
	
	
	/*private class AnovaThread extends Thread {

		 
		public void run() {

			System.out.println("Start service ..." + new java.util.Date());
			

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
						"http://156.145.28.209:8080/axis2/services/AnovaService");
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
				System.out.println("Finished service ..." + new java.util.Date());
				
				int[] featuresIndexes = output.getFeaturesIndexes();
				double[] significances = output.getSignificances();
				String[] significantMarkerNames = new String[featuresIndexes.length];
				
				
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
	}*/
	
	private class AnovaLocalThread extends Thread {

		 
		public void run() {

			System.out.println("Start service ..." + new java.util.Date());
		

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
			 
			AnovaOutput output = null;
			try {
				Anova anova = new Anova(input);
				output = anova.execute();
				System.out.println(output.toString());	
				System.out.println("Finished service ..." + new java.util.Date());
		   } catch (AnovaException AE) {
				 
			   
		   }
		 
			 
		}
	}

}
