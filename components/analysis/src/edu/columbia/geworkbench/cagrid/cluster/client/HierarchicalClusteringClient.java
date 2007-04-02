package edu.columbia.geworkbench.cagrid.cluster.client;

import edu.columbia.geworkbench.cagrid.cluster.common.HierarchicalClusteringI;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Dim;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Distance;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalCluster;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusteringParameter;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Method;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.HierarchicalClusteringPortType;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.service.HierarchicalClusteringServiceAddressingLocator;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySet;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySetGenerator;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySetGeneratorImpl;
import gov.nih.nci.cagrid.introduce.security.client.ServiceSecurityClient;

import java.io.FileInputStream;
import java.io.InputStream;
import java.rmi.RemoteException;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.AxisClient;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.axis.utils.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ginkgo.labs.reader.TabFileReader;
import org.globus.gsi.GlobusCredential;

/**
 * 
 * @author keshav
 * @version $Id: HierarchicalClusteringClient.java,v 1.3 2007/03/09 00:36:59
 *          keshav Exp $
 */
public class HierarchicalClusteringClient extends ServiceSecurityClient
		implements HierarchicalClusteringI {
	private static Log log = LogFactory
			.getLog(HierarchicalClusteringClient.class);

	protected HierarchicalClusteringPortType portType;

	private Object portTypeMutex;

	public HierarchicalClusteringClient(String url)
			throws MalformedURIException, RemoteException {
		this(url, null);
	}

	public HierarchicalClusteringClient(String url, GlobusCredential proxy)
			throws MalformedURIException, RemoteException {
		super(url, proxy);
		initialize();
	}

	public HierarchicalClusteringClient(EndpointReferenceType epr)
			throws MalformedURIException, RemoteException {
		this(epr, null);
	}

	public HierarchicalClusteringClient(EndpointReferenceType epr,
			GlobusCredential proxy) throws MalformedURIException,
			RemoteException {
		super(epr, proxy);
		initialize();
	}

	private void initialize() throws RemoteException {
		this.portTypeMutex = new Object();
		this.portType = createPortType();
	}

	private HierarchicalClusteringPortType createPortType()
			throws RemoteException {

		HierarchicalClusteringServiceAddressingLocator locator = new HierarchicalClusteringServiceAddressingLocator();
		// attempt to load our context sensitive wsdd file
		InputStream resourceAsStream = ClassUtils.getResourceAsStream(
				getClass(), "client-config.wsdd");
		if (resourceAsStream != null) {
			// we found it, so tell axis to configure an engine to use it
			EngineConfiguration engineConfig = new FileProvider(
					resourceAsStream);
			// set the engine of the locator
			locator.setEngine(new AxisClient(engineConfig));
		}
		HierarchicalClusteringPortType port = null;
		try {
			port = locator
					.getHierarchicalClusteringPortTypePort(getEndpointReference());
		} catch (Exception e) {
			throw new RemoteException("Unable to locate portType:"
					+ e.getMessage(), e);
		}

		return port;
	}

	public static void usage() {
		System.out.println(HierarchicalClusteringClient.class.getName()
				+ " -url <service url>");
	}

	public static void main(String[] args) {
		log.debug("Running the Grid Service Client");
		try {
			if (!(args.length < 2)) {
				if (args[0].equals("-url")) {
					String url = args[1];
					HierarchicalClusteringClient client = new HierarchicalClusteringClient(
							url);

					/* my client side method invocation */
					log.debug("Invoking Hierarchical Clustering Service ... ");

					String filename = "src//edu//columbia//geworkbench//cagrid//data//aTestDataSet_without_headers_30.txt";
					InputStream is = new FileInputStream(filename);

					float[][] fdata = TabFileReader.readTabFile(filename);

					String[] rowNames = new String[fdata.length];
					for (int i = 0; i < rowNames.length; i++) {
						rowNames[i] = i + "_at";
					}

					String[] colNames = new String[fdata[0].length]; // non-ragged
					for (int j = 0; j < colNames.length; j++) {
						colNames[j] = String.valueOf(j);
					}

					MicroarraySetGenerator microarraySetGenerator = new MicroarraySetGeneratorImpl();
					MicroarraySet arraySet = microarraySetGenerator
							.float2DToMicroarraySet(fdata, rowNames, colNames);

					HierarchicalClusteringParameter parameters = new HierarchicalClusteringParameter();

					parameters.setDim(Dim.both);
					parameters.setDistance(Distance.euclidean);
					parameters.setMethod(Method.complete);
					HierarchicalCluster hierarchicalClustering = client
							.execute(arraySet, parameters);

					log.info("hierarchical cluster: "
							+ hierarchicalClustering
							+ "\nmicroarray cluster height: "
							+ hierarchicalClustering.getMicroarrayCluster()
									.getHeight()
							+ "\nmarker cluster height: "
							+ hierarchicalClustering.getMarkerCluster()
									.getHeight());

				} else {
					usage();
					System.exit(1);
				}
			} else {
				usage();
				System.exit(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata getServiceSecurityMetadata()
			throws RemoteException {
		synchronized (portTypeMutex) {
			configureStubSecurity((Stub) portType, "getServiceSecurityMetadata");
			gov.nih.nci.cagrid.introduce.security.stubs.GetServiceSecurityMetadataRequest params = new gov.nih.nci.cagrid.introduce.security.stubs.GetServiceSecurityMetadataRequest();
			gov.nih.nci.cagrid.introduce.security.stubs.GetServiceSecurityMetadataResponse boxedResult = portType
					.getServiceSecurityMetadata(params);
			return boxedResult.getServiceSecurityMetadata();
		}
	}

	public edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalCluster execute(
			edu.columbia.geworkbench.cagrid.microarray.MicroarraySet microarraySet,
			edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusteringParameter hierarchicalClusteringParameter)
			throws RemoteException {
		synchronized (portTypeMutex) {
			configureStubSecurity((Stub) portType, "execute");
			edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequest params = new edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequest();
			edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestMicroarraySet microarraySetContainer = new edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestMicroarraySet();
			microarraySetContainer.setMicroarraySet(microarraySet);
			params.setMicroarraySet(microarraySetContainer);
			edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestHierarchicalClusteringParameter hierarchicalClusteringParameterContainer = new edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestHierarchicalClusteringParameter();
			hierarchicalClusteringParameterContainer
					.setHierarchicalClusteringParameter(hierarchicalClusteringParameter);
			params
					.setHierarchicalClusteringParameter(hierarchicalClusteringParameterContainer);
			edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteResponse boxedResult = portType
					.execute(params);
			return boxedResult.getHierarchicalCluster();
		}
	}

}
