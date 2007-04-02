package edu.columbia.geworkbench.cagrid.aracne.client;

import edu.columbia.geworkbench.cagrid.aracne.AdjacencyMatrix;
import edu.columbia.geworkbench.cagrid.aracne.AracneParameter;
import edu.columbia.geworkbench.cagrid.aracne.common.AracneI;
import edu.columbia.geworkbench.cagrid.aracne.stubs.AracnePortType;
import edu.columbia.geworkbench.cagrid.aracne.stubs.service.AracneServiceAddressingLocator;
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
 * An Aracne client to invoke (ca)Grid Aracne.
 * 
 * @author keshav
 * @version $Id: AracneClient.java,v 1.1 2007-04-02 14:57:04 keshav Exp $
 */
public class AracneClient extends ServiceSecurityClient implements AracneI {
	private static Log log = LogFactory.getLog(AracneClient.class);

	protected AracnePortType portType;

	private Object portTypeMutex;

	public AracneClient(String url) throws MalformedURIException,
			RemoteException {
		this(url, null);
	}

	public AracneClient(String url, GlobusCredential proxy)
			throws MalformedURIException, RemoteException {
		super(url, proxy);
		initialize();
	}

	public AracneClient(EndpointReferenceType epr)
			throws MalformedURIException, RemoteException {
		this(epr, null);
	}

	public AracneClient(EndpointReferenceType epr, GlobusCredential proxy)
			throws MalformedURIException, RemoteException {
		super(epr, proxy);
		initialize();
	}

	private void initialize() throws RemoteException {
		this.portTypeMutex = new Object();
		this.portType = createPortType();
	}

	private AracnePortType createPortType() throws RemoteException {

		AracneServiceAddressingLocator locator = new AracneServiceAddressingLocator();
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
		AracnePortType port = null;
		try {
			port = locator.getAracnePortTypePort(getEndpointReference());
		} catch (Exception e) {
			throw new RemoteException("Unable to locate portType:"
					+ e.getMessage(), e);
		}

		return port;
	}

	public static void usage() {
		System.out
				.println(AracneClient.class.getName() + " -url <service url>");
	}

	public static void main(String[] args) {
		System.out.println("Running the Grid Service Client");
		try {
			if (!(args.length < 2)) {
				if (args[0].equals("-url")) {
					AracneClient client = new AracneClient(args[1]);

					AracneParameter aracneParameters = new AracneParameter();
					log
							.info("test with aracne parameters algorithm, kernelWidth, hub.");
					// FIXME you need these 3 parameters to run the
					// algorithm.
					// Use friendly defaults in the service.
					aracneParameters.setAlgorithm("accurate");
					aracneParameters.setKernelWidth(0.7);
					aracneParameters.setHub("4_at");

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

					try {
						log.info("invoking aracne service");
						AdjacencyMatrix adj = client.execute(aracneParameters,
								arraySet);

						if (adj != null) {
							if (adj.getMutualInformationValues() != null)
								log
										.debug("MI values "
												+ adj
														.getMutualInformationValues().length);

							if (adj.getGeneListA() != null)
								log
										.debug("List A "
												+ adj.getGeneListA().length);

							if (adj.getGeneListB() != null)
								log
										.debug("List B "
												+ adj.getGeneListB().length);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

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
			System.exit(1);
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

	public edu.columbia.geworkbench.cagrid.aracne.AdjacencyMatrix execute(
			edu.columbia.geworkbench.cagrid.aracne.AracneParameter aracneParameter,
			edu.columbia.geworkbench.cagrid.microarray.MicroarraySet microarraySet)
			throws RemoteException {
		synchronized (portTypeMutex) {
			configureStubSecurity((Stub) portType, "execute");
			edu.columbia.geworkbench.cagrid.aracne.stubs.ExecuteRequest params = new edu.columbia.geworkbench.cagrid.aracne.stubs.ExecuteRequest();
			edu.columbia.geworkbench.cagrid.aracne.stubs.ExecuteRequestAracneParameter aracneParameterContainer = new edu.columbia.geworkbench.cagrid.aracne.stubs.ExecuteRequestAracneParameter();
			aracneParameterContainer.setAracneParameter(aracneParameter);
			params.setAracneParameter(aracneParameterContainer);
			edu.columbia.geworkbench.cagrid.aracne.stubs.ExecuteRequestMicroarraySet microarraySetContainer = new edu.columbia.geworkbench.cagrid.aracne.stubs.ExecuteRequestMicroarraySet();
			microarraySetContainer.setMicroarraySet(microarraySet);
			params.setMicroarraySet(microarraySetContainer);
			edu.columbia.geworkbench.cagrid.aracne.stubs.ExecuteResponse boxedResult = portType
					.execute(params);
			return boxedResult.getAdjacencyMatrix();
		}
	}

}
