package org.geworkbench.aracne.service.client;

import javax.xml.namespace.QName;

 
import org.geworkbench.components.aracne.data.AracneInput;
import org.geworkbench.components.aracne.data.AracneOutput;
import org.geworkbench.components.aracne.AracneComputation;
import org.geworkbench.components.aracne.AracneException;
 

public class AracneClient {
	 
	String dataSetName = "web100.exp";
	String dataSetIdentifier = "-2144751240";
	String[] hubGeneList = {"31358_at","31359_at","31360_at"};
	String mode = "Discovery";
	String algorithm = "Adaptive Partitioning";
	boolean isKernelWidthSpecified = false;
	float kernelWidth = 0.1f;
	boolean isThresholdMI = false;
	boolean noCorrection = true;
	float threshold = 0.01f;
	boolean isDPIToleranceSpecified = false;
	float dPITolerance = 0.1f;	 
	String[] targetGeneList;
	int bootstrapNumber =1;
	float consensusThreshold = 1.0E-6f;
	
	String[] markers =
	{"31349_at","31350_at","31351_at","31352_at","31353_f_at","31354_r_at","31355_at","31356_at","31357_at","31358_at","31359_at","31360_at","31342_at","31343_at","31344_at","31345_at","31346_at","31347_at","31348_at"};
	String[] microarrayNames = {"CB26-2","CB511","CB512","CB1171","CB1193","N 4-13","N 4-14","N 4-7","N1 4-21","N2 4-21","M 4-12","M 4-14","M 4-26","M 5-2","M 6-8","CB 2-23","CB 3-10","CB 3-30","CB 3-7","CB 6-8","CC 3-28","CC 4-14","CC 4-6","CC 7-25","CC 7-7","P-CLL1318","P-CLL1269","P-CLL1275","P-CLL1282","CLL1310","CLL4","CLL14","CLL1208","CLL1255","CLL1259","CLL1333","CLL1270","CLL1272","CLL1332","P-CLL1304","P-CLL1263","P-CLL1215","P-CLL1268","P-CLL1285","P-CLL1291","P-CLL1302","P-CLLB2","P-CLLB3","P-CLLB4","P-CLLB6","P-CLLB7","P-CLLB9","P-CLLB14","P-CLLB15","P-CLL1297","P-CLLB10","P-CLLB13","CLL.B12.real","CLLB-8.real","HCL 04","HCL 08","HCL 09","HCL 10","HCL 11","HCL 13 PB","HCL 14(A)"};
	float[][] markerValues =
	{{49.7f,285.4f,16.7f,44.0f,162.9f,29.0f,16.5f,51.1f,2.2f,9.7f,4.2f,35.5f,90.3f,10.9f,157.9f,17.4f,110.5f,377.1f,7.8f},
			{17.7f,224.8f,4.1f,25.7f,254.4f,26.1f,18.0f,54.9f,32.1f,3.4f,3.5f,33.0f,127.5f,3.0f,90.8f,29.4f,81.3f,403.9f,3.2f},
			{67.9f,243.9f,5.3f,12.4f,224.2f,41.6f,19.3f,51.1f,5.2f,3.7f,1.4f,33.1f,100.6f,15.0f,137.2f,52.4f,86.2f,410.1f,7.1f},
			{68.2f,219.9f,3.7f,38.2f,190.1f,16.8f,17.3f,67.5f,4.7f,16.0f,2.0f,33.0f,90.7f,11.7f,170.0f,3.7f,153.4f,535.9f,9.1f},
			{45.4f,212.2f,5.1f,27.7f,196.2f,33.2f,14.7f,46.4f,37.0f,21.5f,1.6f,6.8f,95.7f,10.0f,127.5f,20.0f,33.6f,644.2f,7.1f},
			{13.4f,192.4f,14.8f,12.2f,116.1f,31.1f,12.6f,72.7f,4.1f,9.0f,1.0f,43.6f,211.3f,17.7f,154.6f,13.5f,244.5f,475.3f,8.7f},
			{9.7f,128.7f,16.3f,30.9f,161.1f,17.3f,12.5f,56.3f,3.6f,3.6f,0.7f,6.4f,179.1f,12.7f,192.6f,14.2f,210.8f,596.7f,7.1f},
			{13.9f,190.2f,8.6f,5.6f,129.8f,22.5f,19.0f,39.2f,13.6f,8.1f,0.8f,8.2f,107.1f,3.3f,128.0f,26.0f,232.1f,504.6f,4.3f},
			{6.7f,267.4f,8.0f,29.1f,219.0f,46.1f,15.5f,77.5f,34.0f,12.0f,4.9f,23.6f,162.8f,24.5f,321.8f,66.1f,408.2f,701.3f,10.3f},
			{13.5f,206.0f,15.5f,7.3f,132.0f,44.5f,10.6f,68.7f,6.2f,17.8f,1.5f,32.0f,220.3f,13.2f,176.1f,9.8f,289.6f,553.7f,9.2f},
			{15.6f,148.6f,3.6f,14.7f,123.9f,28.3f,12.9f,43.2f,18.4f,11.9f,1.0f,30.5f,74.0f,16.8f,43.7f,31.9f,219.1f,657.0f,9.3f},
			{13.7f,238.4f,11.2f,9.5f,164.3f,33.6f,15.5f,45.6f,3.3f,4.0f,2.2f,7.0f,109.4f,2.6f,194.3f,30.7f,228.8f,509.8f,6.3f},
			{19.2f,175.6f,3.9f,23.6f,151.1f,38.1f,20.2f,51.3f,3.6f,4.3f,1.1f,29.6f,102.1f,26.4f,262.6f,22.9f,314.3f,961.0f,17.5f},
			{13.0f,205.2f,5.2f,24.8f,147.2f,20.6f,18.5f,59.5f,6.1f,4.0f,2.5f,30.4f,84.6f,4.0f,97.4f,16.4f,220.4f,533.2f,6.9f},
			{9.1f,198.5f,3.1f,4.4f,118.7f,19.5f,14.5f,74.2f,7.7f,4.3f,0.8f,19.0f,78.5f,5.3f,156.1f,26.3f,150.4f,605.3f,6.3f},
			{13.5f,137.7f,11.3f,27.2f,84.5f,24.6f,7.1f,32.1f,30.5f,3.2f,1.2f,31.7f,85.1f,25.0f,354.3f,42.5f,236.3f,868.7f,8.4f},
			{13.7f,201.2f,5.8f,36.1f,166.8f,33.7f,16.1f,34.3f,5.6f,5.0f,9.0f,9.0f,66.6f,3.6f,320.8f,13.4f,267.0f,618.9f,9.7f},
			{31.8f,361.7f,6.2f,35.5f,280.5f,50.0f,12.9f,79.9f,7.5f,9.5f,1.2f,34.2f,118.5f,9.7f,119.8f,48.2f,409.7f,947.0f,52.5f},
			{23.3f,144.6f,3.5f,18.3f,113.0f,22.7f,10.5f,47.9f,6.6f,3.9f,1.6f,27.6f,55.4f,6.3f,56.6f,14.7f,194.8f,591.9f,6.9f},
			{8.0f,158.9f,4.0f,22.4f,104.6f,22.4f,18.6f,43.3f,5.8f,3.8f,0.6f,22.1f,89.5f,2.5f,75.1f,18.5f,202.0f,494.9f,6.2f},
			{9.1f,178.7f,4.9f,31.5f,156.9f,14.9f,9.7f,53.4f,10.5f,15.3f,2.2f,17.1f,73.9f,9.9f,140.3f,16.0f,250.0f,430.8f,8.5f},
			{31.3f,169.4f,3.7f,33.9f,129.0f,26.0f,9.0f,48.3f,18.4f,4.4f,2.0f,29.4f,45.6f,2.2f,120.3f,30.6f,174.6f,530.4f,8.7f},
			{8.5f,149.1f,3.8f,24.3f,100.9f,13.5f,11.2f,34.7f,8.4f,2.8f,0.6f,5.4f,132.5f,2.5f,243.4f,2.5f,190.9f,639.4f,5.1f},
			{19.1f,224.5f,2.9f,25.7f,198.6f,33.3f,8.7f,59.2f,10.1f,3.9f,9.0f,44.4f,156.8f,9.2f,149.1f,23.9f,291.6f,859.5f,7.9f},
			{46.1f,212.4f,6.6f,13.1f,162.4f,27.1f,8.9f,50.0f,4.2f,18.6f,9.5f,25.0f,120.3f,2.3f,192.6f,8.8f,284.6f,863.9f,11.6f},
			{65.3f,231.8f,4.3f,3.6f,195.9f,12.7f,12.6f,56.5f,5.0f,38.8f,1.6f,33.1f,174.5f,3.9f,28.7f,40.4f,344.7f,376.9f,31.9f},
			{51.5f,306.2f,3.3f,47.1f,124.4f,34.0f,17.8f,81.9f,6.3f,34.5f,1.9f,54.0f,229.9f,27.3f,48.6f,21.0f,322.2f,515.2f,6.7f},
			{27.8f,312.2f,7.7f,6.3f,206.8f,11.7f,19.1f,36.3f,9.3f,8.2f,1.6f,13.6f,174.7f,21.2f,138.9f,4.2f,262.0f,56.4f,9.9f},
			{22.3f,208.2f,12.5f,5.0f,257.8f,28.6f,13.8f,81.7f,14.5f,8.3f,0.9f,37.5f,284.8f,5.1f,31.2f,6.5f,314.1f,359.2f,10.6f},
			{20.5f,336.6f,9.0f,7.7f,245.2f,49.6f,107.4f,81.9f,60.5f,18.7f,2.2f,31.9f,124.0f,19.3f,29.2f,46.8f,334.0f,824.4f,15.7f},
			{14.4f,239.3f,20.4f,10.5f,289.0f,21.4f,27.0f,50.8f,8.4f,6.7f,1.7f,5.5f,117.4f,1.2f,18.4f,9.9f,215.2f,686.4f,7.9f},
			{11.4f,124.8f,4.4f,18.1f,97.2f,17.4f,16.6f,46.3f,30.5f,1.7f,2.6f,34.4f,93.7f,4.0f,9.0f,6.1f,174.0f,246.5f,6.5f},
			{34.5f,406.0f,7.3f,44.3f,201.8f,25.3f,13.2f,74.3f,9.3f,16.8f,1.3f,40.1f,93.9f,26.1f,44.4f,9.1f,340.6f,727.5f,11.1f},
			{29.1f,250.5f,8.1f,34.0f,172.2f,23.3f,14.6f,72.2f,4.3f,13.0f,0.9f,23.8f,108.7f,15.1f,24.2f,41.5f,260.5f,490.6f,9.3f},
			{7.1f,298.2f,10.3f,13.9f,234.0f,26.2f,43.0f,82.2f,9.0f,31.7f,4.2f,51.8f,184.3f,11.1f,22.8f,3.5f,273.4f,1310.9f,6.5f},
			{19.5f,238.3f,5.3f,2.2f,172.9f,33.6f,27.9f,76.2f,6.8f,3.4f,4.7f,4.1f,105.1f,3.4f,25.8f,7.7f,333.7f,129.6f,12.1f},
			{16.0f,335.0f,8.2f,43.2f,190.0f,23.1f,17.9f,62.2f,8.0f,4.6f,1.6f,7.0f,106.3f,17.7f,98.8f,8.8f,182.2f,464.0f,16.6f},
			{21.7f,334.2f,5.7f,4.4f,164.5f,30.5f,16.4f,61.6f,17.9f,5.5f,1.7f,21.1f,109.7f,20.7f,23.3f,30.2f,249.4f,4826.6f,10.2f},
			{18.1f,304.6f,4.0f,58.7f,225.1f,28.2f,68.2f,86.0f,8.1f,13.0f,1.8f,14.2f,110.5f,7.5f,137.3f,44.0f,350.0f,72.2f,13.4f},
			{59.9f,193.3f,6.0f,22.4f,128.2f,18.5f,9.2f,20.7f,21.0f,18.5f,1.0f,5.4f,128.8f,0.5f,17.6f,10.7f,38.8f,98.4f,3.0f},
			{23.1f,189.2f,7.6f,22.5f,182.0f,12.8f,10.8f,64.6f,5.4f,18.0f,3.7f,15.4f,22.7f,2.9f,30.4f,25.9f,33.4f,28.2f,6.2f},
			{19.4f,275.1f,6.1f,5.2f,173.3f,23.2f,9.3f,69.0f,6.0f,13.1f,0.6f,11.1f,78.3f,7.5f,56.2f,5.6f,68.0f,55.1f,5.4f},
			{8.0f,193.3f,6.8f,19.4f,155.2f,16.5f,10.2f,42.6f,32.3f,13.0f,0.5f,6.5f,212.2f,2.7f,52.9f,6.1f,64.9f,85.1f,6.4f},
			{39.8f,195.3f,6.1f,7.0f,177.0f,16.2f,19.9f,50.2f,8.8f,8.2f,1.4f,1.0f,139.5f,1.2f,24.9f,23.4f,30.8f,108.0f,6.3f},
			{14.9f,230.2f,3.9f,18.5f,225.9f,37.6f,6.8f,36.2f,3.5f,24.4f,1.2f,6.7f,95.9f,1.9f,16.9f,23.4f,39.6f,18.9f,2.7f},
			{26.2f,122.2f,2.0f,22.4f,91.0f,14.6f,13.7f,32.6f,5.5f,3.5f,1.0f,18.1f,115.1f,1.2f,6.0f,16.1f,31.0f,104.0f,4.4f},
			{27.2f,166.6f,4.4f,31.9f,169.5f,23.5f,11.8f,74.3f,10.9f,17.7f,10.1f,43.7f,96.6f,6.9f,22.7f,6.1f,31.2f,36.0f,5.8f},
			{17.1f,234.0f,4.0f,14.8f,85.8f,23.0f,18.3f,43.1f,27.1f,30.4f,0.8f,19.1f,234.0f,1.5f,18.0f,5.5f,32.7f,30.6f,5.8f},
			{14.5f,197.6f,6.6f,25.7f,141.0f,26.1f,16.3f,44.8f,12.0f,15.4f,2.0f,31.3f,164.5f,2.1f,109.0f,5.5f,47.8f,15.5f,4.9f},
			{98.5f,182.8f,5.5f,25.3f,128.2f,16.7f,16.3f,47.0f,8.4f,11.9f,2.9f,14.0f,102.4f,3.5f,112.0f,20.1f,91.5f,22.2f,4.7f},
			{29.4f,258.0f,6.7f,29.5f,182.4f,20.5f,15.0f,37.4f,9.6f,3.9f,2.0f,11.9f,92.0f,7.4f,1302.0f,43.4f,33.4f,21.2f,6.4f},
			{15.7f,155.4f,9.6f,7.9f,132.5f,24.8f,9.7f,34.2f,18.1f,20.4f,1.1f,8.7f,51.2f,4.6f,51.5f,43.0f,33.3f,18.9f,3.6f},
			{41.0f,196.4f,3.2f,19.4f,134.3f,21.6f,9.4f,56.2f,6.0f,3.6f,0.8f,7.1f,134.6f,10.5f,14.9f,14.8f,38.9f,21.6f,4.5f},
			{9.4f,209.5f,2.5f,24.4f,155.4f,23.3f,11.5f,38.0f,5.1f,8.1f,0.7f,5.5f,139.6f,0.7f,39.3f,21.3f,34.4f,20.3f,4.1f},
			{13.6f,261.5f,4.7f,24.6f,120.5f,21.3f,14.0f,59.1f,8.3f,22.6f,0.9f,13.3f,113.8f,3.5f,16.8f,31.2f,170.6f,87.0f,2.8f},
			{57.4f,210.8f,4.1f,31.7f,148.9f,11.2f,23.0f,42.9f,5.8f,19.6f,1.1f,46.4f,113.5f,7.7f,27.5f,26.0f,30.5f,150.3f,2.6f},
			{10.7f,187.1f,4.8f,22.1f,122.9f,19.9f,11.9f,30.9f,11.4f,21.0f,1.0f,32.0f,92.5f,2.8f,12.5f,12.5f,54.6f,24.7f,4.6f},
			{14.4f,186.7f,1.8f,23.7f,116.6f,25.0f,9.6f,35.7f,10.6f,3.4f,0.6f,7.3f,85.4f,1.8f,11.4f,14.6f,46.4f,25.5f,2.2f},
			{13.9f,219.4f,4.4f,13.6f,167.7f,29.6f,12.2f,32.9f,5.9f,8.2f,1.3f,3.8f,136.3f,2.4f,14.8f,27.0f,91.2f,16.9f,5.9f},
			{9.4f,141.6f,1.0f,12.6f,96.1f,13.9f,3.8f,19.1f,5.5f,9.1f,2.0f,12.4f,74.2f,0.6f,56.1f,11.9f,54.5f,70.1f,3.4f},
			{9.8f,95.9f,2.9f,20.3f,81.3f,18.5f,7.6f,24.3f,3.4f,22.5f,0.7f,16.7f,84.2f,2.7f,138.9f,12.8f,49.4f,283.4f,4.0f},
			{10.3f,94.5f,1.7f,17.6f,91.8f,11.8f,6.5f,16.1f,2.8f,2.3f,0.4f,17.0f,84.5f,1.5f,30.3f,2.4f,50.8f,176.8f,2.8f},
			{8.0f,102.5f,2.3f,12.0f,123.0f,20.0f,6.2f,24.2f,6.2f,2.9f,2.0f,17.1f,66.9f,6.7f,157.2f,12.9f,60.6f,629.9f,4.4f},
			{55.9f,121.8f,5.2f,2.4f,115.5f,22.7f,9.6f,42.8f,10.6f,4.1f,0.5f,31.4f,98.1f,3.3f,140.5f,5.1f,64.3f,494.1f,7.3f},
			{19.2f,122.5f,2.1f,24.4f,124.1f,18.3f,17.0f,32.7f,3.0f,2.3f,1.2f,3.3f,29.6f,12.0f,50.5f,20.2f,41.8f,83.2f,1.8f},
			{5.4f,59.9f,4.8f,8.6f,124.5f,13.8f,4.5f,28.1f,2.4f,4.1f,0.8f,24.8f,48.4f,3.6f,273.2f,1.3f,17.8f,812.1f,5.3f}};
	
	/**
	 * @param args
	 * @throws AxisFault
	 */
	
	/*public void runaracne()
	{
		for(int i=0; i<1; i++)
		{	
			aracneThread t = new aracneThread();
		    t.start();
		}
	}*/
	
	public void runAranceLocal()
	{
		for(int i=0; i<1; i++)
		{	
			AracneLocalThread t = new AracneLocalThread();
		    t.start();
		}
	}
	
	public static void main(String[] args1) {		 
     
		AracneClient ac = new AracneClient();
		ac.runAranceLocal();
		//ac.runaracne();
	}
	
	
	/*private class aracneThread extends Thread {

		 
		public void run() {

			System.out.println("Start service ..." + new java.util.Date());
			

			aracneInput input = new aracneInput();
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
						"http://156.145.28.209:8080/axis2/services/aracneService");
				options.setTo(targetEPR);

				// notice that that namespace is in the required form
				QName opName = new QName(
						"http://service.aracne.components.geworkbench.org",
						"execute");
				Object[] args = new Object[] { input };

				Class<?>[] returnType = new Class[] { aracneOutput.class };

				Object[] response = serviceClient.invokeBlocking(opName, args,
						returnType);
				
				aracneOutput output = (aracneOutput) response[0];
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
	
	private class AracneLocalThread extends Thread {

		 
		public void run() {

			System.out.println("Start service ..." + new java.util.Date());
		

			AracneInput input = new AracneInput(dataSetName, dataSetIdentifier, hubGeneList, mode,
					algorithm, isKernelWidthSpecified,
					kernelWidth, isThresholdMI, noCorrection,
				    threshold, isDPIToleranceSpecified,
					dPITolerance, targetGeneList,
					bootstrapNumber, consensusThreshold, markers,
					microarrayNames,
					markerValues);
			 
			AracneOutput output = null;
			try {
				AracneComputation aracneComputation = new AracneComputation(input);
				output = aracneComputation.execute();
				System.out.println(output.toString());	
				System.out.println("Finished service ..." + new java.util.Date());
		   } catch (AracneException AE) {
				 
			   
		   }
		 
			 
		}
	}

}
