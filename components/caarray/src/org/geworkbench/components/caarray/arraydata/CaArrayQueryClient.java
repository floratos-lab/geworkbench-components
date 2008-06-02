/**
 * The software subject to this notice and license includes both human readable
 * source code form and machine readable, binary, object code form. The caArray
 * Software was developed in conjunction with the National Cancer Institute
 * (NCI) by NCI employees and 5AM Solutions, Inc. (5AM). To the extent
 * government employees are authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 *
 * This caArray Software License (the License) is between NCI and You. You (or
 * Your) shall mean a person or an entity, and all other entities that control,
 * are controlled by, or are under common control with the entity. Control for
 * purposes of this definition means (i) the direct or indirect power to cause
 * the direction or management of such entity, whether by contract or otherwise,
 * or (ii) ownership of fifty percent (50%) or more of the outstanding shares,
 * or (iii) beneficial ownership of such entity.
 *
 * This License is granted provided that You agree to the conditions described
 * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up,
 * no-charge, irrevocable, transferable and royalty-free right and license in
 * its rights in the caArray Software to (i) use, install, access, operate,
 * execute, copy, modify, translate, market, publicly display, publicly perform,
 * and prepare derivative works of the caArray Software; (ii) distribute and
 * have distributed to and by third parties the caArray Software and any
 * modifications and derivative works thereof; and (iii) sublicense the
 * foregoing rights set out in (i) and (ii) to third parties, including the
 * right to license such rights to further third parties. For sake of clarity,
 * and not by way of limitation, NCI shall have no right of accounting or right
 * of payment from You or Your sub-licensees for the rights granted under this
 * License. This License is granted at no charge to You.
 *
 * Your redistributions of the source code for the Software must retain the
 * above copyright notice, this list of conditions and the disclaimer and
 * limitation of liability of Article 6, below. Your redistributions in object
 * code form must reproduce the above copyright notice, this list of conditions
 * and the disclaimer of Article 6 in the documentation and/or other materials
 * provided with the distribution, if any.
 *
 * Your end-user documentation included with the redistribution, if any, must
 * include the following acknowledgment: This product includes software
 * developed by 5AM and the National Cancer Institute. If You do not include
 * such end-user documentation, You shall include this acknowledgment in the
 * Software itself, wherever such third-party acknowledgments normally appear.
 *
 * You may not use the names "The National Cancer Institute", "NCI", or "5AM"
 * to endorse or promote products derived from this Software. This License does
 * not authorize You to use any trademarks, service marks, trade names, logos or
 * product names of either NCI or 5AM, except as required to comply with the
 * terms of this License.
 *
 * For sake of clarity, and not by way of limitation, You may incorporate this
 * Software into Your proprietary programs and into any third party proprietary
 * programs. However, if You incorporate the Software into third party
 * proprietary programs, You agree that You are solely responsible for obtaining
 * any permission from such third parties required to incorporate the Software
 * into such third party proprietary programs and for informing Your
 * sub-licensees, including without limitation Your end-users, of their
 * obligation to secure any required permissions from such third parties before
 * incorporating the Software into such third party proprietary software
 * programs. In the event that You fail to obtain such permissions, You agree
 * to indemnify NCI for any claims against NCI by such third parties, except to
 * the extent prohibited by law, resulting from Your failure to obtain such
 * permissions.
 *
 * For sake of clarity, and not by way of limitation, You may add Your own
 * copyright statement to Your modifications and to the derivative works, and
 * You may provide additional or different license terms and conditions in Your
 * sublicenses of modifications of the Software, or any derivative works of the
 * Software as a whole, provided Your use, reproduction, and distribution of the
 * Work otherwise complies with the conditions stated in this License.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO
 * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC. OR THEIR
 * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.geworkbench.components.caarray.arraydata;

import edu.georgetown.pir.Organism;
import gov.nih.nci.caarray.domain.contact.Organization;
import gov.nih.nci.caarray.domain.contact.Person;
import gov.nih.nci.caarray.domain.data.DataRetrievalRequest;
import gov.nih.nci.caarray.domain.data.DataSet;
import gov.nih.nci.caarray.domain.data.DerivedArrayData;
import gov.nih.nci.caarray.domain.data.QuantitationType;
import gov.nih.nci.caarray.domain.hybridization.Hybridization;
import gov.nih.nci.caarray.domain.project.Experiment;
import gov.nih.nci.caarray.domain.project.ExperimentContact;
import gov.nih.nci.caarray.domain.sample.AbstractBioMaterial;
import gov.nih.nci.caarray.services.CaArrayServer;
import gov.nih.nci.caarray.services.search.CaArraySearchService;
import gov.nih.nci.cagrid.cqlquery.Association;
import gov.nih.nci.cagrid.cqlquery.Attribute;
import gov.nih.nci.cagrid.cqlquery.CQLQuery;
import gov.nih.nci.cagrid.cqlquery.Predicate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.lang.time.StopWatch;
import org.geworkbench.builtin.projects.remoteresources.carraydata.CaArray2Experiment;
import org.geworkbench.builtin.projects.remoteresources.query.CaARRAYQueryPanel;
import org.geworkbench.components.caarray.BaseApiClient;

/**
 * A client downloading an array data set through CaArray's Remote Java API. The
 * DataSet can contain data from multiple experiments, hybridizations and a
 * subset of quantitation types.
 * 
 * @author Rashmi Srinivasa
 */
public class CaArrayQueryClient extends BaseApiClient {
	// private static final String DEFAULT_EXPERIMENT_NAME = "Affymetrix
	// Specification with Data 01";
	// private static final String DEFAULT_EXPERIMENT_NAME = "133plus 2
	// test";//133plus 2 test
	// private static final String DEFAULT_EXPERIMENT_NAME = "Affymetrix
	// Experiment with CHP Data 01"; // TEst

	private static final String DEFAULT_QUANTITATION_TYPE = "CELintensity";
	private static TreeMap<String, String> experimentDesciptions = new TreeMap<String, String>(); // For

	// experiment
	// info.

	// public static void main(String[] args) {
	// DataRetrievalRequest request = new DataRetrievalRequest();
	// try {
	// DataSetDownloadClient downloadClient = new DataSetDownloadClient();
	// CaArrayServer server = new CaArrayServer(SERVER_NAME, JNDI_PORT);
	// server.connect();
	// CaArraySearchService searchService = server.getSearchService();
	//
	// downloadClient.lookupExperiments(searchService, request);
	// downloadClient.lookupQuantitationTypes(searchService, request);
	// DataRetrievalService dataService = server.getDataRetrievalService();
	// long startTime = System.currentTimeMillis();
	// DataSet dataSet = dataService.getDataSet(request);
	//
	// int numValuesRetrieved = 0;
	//
	// // Check if the retrieved hybridizations and quantitation types are
	// // as requested.
	// if (dataSet != null) {
	//
	// // get the ArrayDesign
	// // ArrayDesign[] arrayDesigns =
	// // experiment.getArrayDesigns().toArray(new ArrayDesign[1]);
	//
	// // System.out.println(" (" + experiment.getManufacturer() +")"
	// // +arrayDesigns[0].getName());
	//
	// // Get each HybridizationData in the DataSet.
	// for (HybridizationData oneHybData : dataSet
	// .getHybridizationDataList()) {
	// HybridizationData populatedHybData = searchService.search(
	// oneHybData).get(0);
	// // Get each column in the HybridizationData.
	// for (AbstractDataColumn column : populatedHybData
	// .getColumns()) {
	// AbstractDataColumn populatedColumn = searchService
	// .search(column).get(0);
	// // Find the type of the column.
	// QuantitationType qType = populatedColumn
	// .getQuantitationType();
	// Class typeClass = qType.getTypeClass();
	// // Retrieve the appropriate data depending on the type
	// // of the column.
	// if (typeClass == String.class) {
	// String[] values = ((StringColumn) populatedColumn)
	// .getValues();
	// numValuesRetrieved += values.length;
	// } else if (typeClass == Float.class) {
	// float[] values = ((FloatColumn) populatedColumn)
	// .getValues();
	// numValuesRetrieved += values.length;
	// } else if (typeClass == Short.class) {
	// short[] values = ((ShortColumn) populatedColumn)
	// .getValues();
	// numValuesRetrieved += values.length;
	// } else if (typeClass == Boolean.class) {
	// boolean[] values = ((BooleanColumn) populatedColumn)
	// .getValues();
	// numValuesRetrieved += values.length;
	// } else if (typeClass == Double.class) {
	// double[] values = ((DoubleColumn) populatedColumn)
	// .getValues();
	// numValuesRetrieved += values.length;
	// } else if (typeClass == Integer.class) {
	// int[] values = ((IntegerColumn) populatedColumn)
	// .getValues();
	// numValuesRetrieved += values.length;
	// } else if (typeClass == Long.class) {
	// long[] values = ((LongColumn) populatedColumn)
	// .getValues();
	// numValuesRetrieved += values.length;
	// } else {
	// // Should never get here.
	// }
	// }
	// }
	// long endTime = System.currentTimeMillis();
	// long totalTime = endTime - startTime;
	// log.debug("Total Time: " + totalTime + " ms.");
	// log.debug("Retrieved "
	// + dataSet.getHybridizationDataList().size()
	// + " hybridization data elements, "
	// + dataSet.getQuantitationTypes().size()
	// + " quantitation types and " + numValuesRetrieved
	// + " values.");
	// } else {
	// log.debug("Retrieved null DataSet.");
	// }
	// } catch (ServerConnectionException e) {
	// log.debug("Server connection exception: " + e);
	// e.printStackTrace();
	// } catch (RuntimeException e) {
	// log.error("Runtime exception: " + e);
	// e.printStackTrace();
	// } catch (Throwable t) {
	// // Catches things like out-of-memory errors and logs them.
	// log.error("Throwable: " + t);
	// t.printStackTrace();
	// }
	// }

	// public CaArray2Experiment[] lookupExperiments(String url, int port,
	// String username, String password) throws Exception {
	// CaArrayServer server = new CaArrayServer(url, port);
	// server.connect();
	// CaArraySearchService searchService = server.getSearchService();
	// DataRetrievalRequest request = new DataRetrievalRequest();
	// return lookupExperiments(searchService, request, url, port, username,
	// password);
	// }

	public CaArray2Experiment[] lookupExperiments(CaArraySearchService service,
			String url, int port, String username, String password,
			HashMap<String, String[]> filters) throws Exception {
		CaArrayServer server = new CaArrayServer(url, port);
		server.connect();
		String[] arrays = new String[filters.size()];
		arrays = filters.keySet().toArray(arrays);
		for (String key : arrays) {
			String[] values = filters.get(key);
			if (values != null && values.length > 0) {
				return lookupExperimentsWithFilter(service, url, port, key,
						values[0]);
			}
		}
		return null;

	}

	// public TreeMap<String, String[]> lookupExperiments(String url,
	// int port) throws Exception {
	// CaArrayServer server = new CaArrayServer(url, port);
	// server.connect();
	// CaArraySearchService searchService = server.getSearchService();
	// DataRetrievalRequest request = new DataRetrievalRequest();
	// return lookupExperiments(searchService, request);
	// }

	public static String[] lookupQuantitationTypes(String url, int port,
			String experimentName) throws Exception {
		CaArrayServer server = new CaArrayServer(url, port);
		server.connect();
		CaArraySearchService searchService = server.getSearchService();
		DataRetrievalRequest request = new DataRetrievalRequest();
		return lookupQuantitationTypes(searchService, request, experimentName);
	}

	public static TreeMap<String, String> getExperimentDesciptions() {
		return experimentDesciptions;
	}

	public static void setExperimentDesciptions(
			TreeMap<String, String> _experimentDesciptions) {
		experimentDesciptions = _experimentDesciptions;
	}

	public static String[] lookupQuantitationTypes(
			CaArraySearchService service, DataRetrievalRequest request,
			String experimentName) {
		Experiment exampleExperiment = new Experiment();
		exampleExperiment.setTitle(experimentName);

		List<Experiment> experimentList = service.search(exampleExperiment);
		if (experimentList.size() == 0) {
			return null;
		} else {
			Experiment experiment = experimentList.get(0);
			Set<Hybridization> h1 = experiment.getHybridizations();
			if (h1.size() > 0) {
				// The following assumption: for all hybridization in a
				// experiment, they have the same QTypes.

				Hybridization h = (Hybridization) h1.toArray()[0];
				Hybridization hybridization = (Hybridization) (service
						.search(h).get(0));
				DataSet dataSet = getDataSet(service, hybridization);

				if (dataSet != null) {
					List<QuantitationType> qList = dataSet
							.getQuantitationTypes();
					String[] qTypes = new String[qList.size()];
					int i = 0;
					for (QuantitationType qu : qList) {
						qTypes[i] = qu.getName();
						i++;
					}
					return qTypes;

				}

			}
		}

		return null;
	}

	static DataSet getDataSet(CaArraySearchService service,
			Hybridization hybridization) {
		DataSet dataSet = null;

		// If raw data doesn't exist, try to find derived data
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

	public static CaArray2Experiment[] lookupExperimentsWithFilter(
			CaArraySearchService service, String url, int port, String type,
			String value) {

		log.debug("Get  into lookupExpermentWithFilter");
		try {

			// CaArraySearchService service = server.getSearchService();
			TreeMap<String, String[]> tree = new TreeMap<String, String[]>();
			gov.nih.nci.cagrid.cqlquery.Object object = new gov.nih.nci.cagrid.cqlquery.Object();
			// CQLQuery query = new CQLQuery();
			// String target = "gov.nih.nci.caarray.domain.project.Experiment";
			// object.setName(target);
			// if (type.equalsIgnoreCase(CaARRAYQueryPanel.ORGANISM)) {
			// String organismName = value;
			// Association organismAssociation = new Association();
			// organismAssociation.setName("edu.georgetown.pir.Organism");
			// Attribute organismAttribute = new Attribute();
			// organismAttribute.setName("commonName");
			// organismAttribute.setValue(organismName);
			// organismAttribute.setPredicate(Predicate.EQUAL_TO);
			// organismAssociation.setAttribute(organismAttribute);
			// organismAssociation.setRoleName("organism");
			// object.setAssociation(organismAssociation);
			// } else if (type.equalsIgnoreCase(CaARRAYQueryPanel.PINAME)) {
			// Association organismAssociation = new Association();
			// organismAssociation
			// .setName("gov.nih.nci.caarray.domain.project.ExperimentContact");
			// Attribute organismAttribute = new Attribute();
			// organismAttribute.setName("name");
			// organismAttribute.setValue(value);
			// organismAttribute.setPredicate(Predicate.EQUAL_TO);
			// organismAssociation.setAttribute(organismAttribute);
			// organismAssociation.setRoleName("manufacturer");
			// object.setAssociation(organismAssociation);
			// } else if (type.equalsIgnoreCase(CaARRAYQueryPanel.CHIPPROVIDER))
			// {
			// Association organismAssociation = new Association();
			// organismAssociation
			// .setName("gov.nih.nci.caarray.domain.contact.Organization");
			// Attribute organismAttribute = new Attribute();
			// organismAttribute.setName("name");
			// organismAttribute.setValue(value);
			// organismAttribute.setPredicate(Predicate.EQUAL_TO);
			// organismAssociation.setAttribute(organismAttribute);
			// organismAssociation.setRoleName("manufacturer");
			// object.setAssociation(organismAssociation);
			// } else if (type.equalsIgnoreCase(CaARRAYQueryPanel.TISSUETYPE)) {
			// Association termAssociation = new Association();
			// termAssociation.setName("gov.nih.nci.caarray.domain.vocabulary.Term");
			// Attribute termAttribute = new Attribute();
			// termAttribute.setName("value");
			// termAttribute.setValue(value);
			// termAttribute.setPredicate(Predicate.EQUAL_TO);
			// termAssociation.setAttribute(termAttribute);
			// termAssociation.setRoleName("tissueSite");
			// Association sourceAssociation = new Association();
			// sourceAssociation.setName("gov.nih.nci.caarray.domain.sample.Source");
			// sourceAssociation.setRoleName("sources");
			// sourceAssociation.setAssociation(termAssociation);
			// object.setAssociation(sourceAssociation);
			// }
			// query.setTarget(object);
			CQLQuery query = CQLQueryGenerator.generateQuery(
					CQLQueryGenerator.EXPERIMENT, type, value);
			List list = service.search(query);

			Vector<CaArray2Experiment> exps = new Vector<CaArray2Experiment>();
			for (Object o : list) {
				Experiment e = ((Experiment) o);
				log.debug("Experiment : " + e.getTitle() + "| "
						+ e.getManufacturer().getName());
				Set<Hybridization> h1 = (((Experiment) o).getHybridizations());

				if (h1.size() > 0) {
					String[] hybridizationValues = new String[h1.size()];
					int i = 0;
					Hybridization oneHybridization = null;
					TreeSet<String> set = new TreeSet<String>();
					for (Hybridization h : h1) {
						oneHybridization = h;
						set.add(h.getName());
					}
					hybridizationValues = set.toArray(hybridizationValues);

					Hybridization hybridization = (Hybridization) (service
							.search(oneHybridization).get(0));
					DataSet dataSet = getDataSet(service, hybridization);
					String[] qTypes = null;
					if (dataSet != null) {
						List<QuantitationType> qList = dataSet
								.getQuantitationTypes();
						qTypes = new String[qList.size()];
						int j = 0;
						for (QuantitationType qu : qList) {
							qTypes[j] = qu.getName();
							j++;
						}
					}
					CaArray2Experiment exp = new CaArray2Experiment(url, port);

					exp.setName(e.getTitle());
					exp.setDescription(e.getDescription());
					exp.setHybridizations(hybridizationValues);
					exp.setQuantitationTypes(qTypes);
					exps.add(exp);

				}
			}
			CaArray2Experiment[] experimentsArray = new CaArray2Experiment[exps
					.size()];
			return exps.toArray(experimentsArray);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public CaArray2Experiment[] lookupExperiments(CaArraySearchService service,
			DataRetrievalRequest request, String url, int port,
			String usesname, String password) throws Exception {
		experimentDesciptions = new TreeMap<String, String>();
		Vector<CaArray2Experiment> exps = new Vector<CaArray2Experiment>();
		// log.debug("Get into lookupExperment");
		// CQLQuery query = new CQLQuery();
		// query.setTarget(new gov.nih.nci.cagrid.cqlquery.Object());
		// String target = "gov.nih.nci.caarray.domain.project.Experiment";
		// query.getTarget().setName(target);
		CQLQuery query = CQLQueryGenerator
				.generateQuery(CQLQueryGenerator.EXPERIMENT);
		List list = service.search(query);
		for (Object o : list) {
			Experiment e = ((Experiment) o);
			// log.debug("Experiment : " + e.getTitle() + "| "
			// + e.getManufacturer().getName() + "|" +
			// e.getPrimaryInvestigator());
			ExperimentContact contact = e.getPrimaryInvestigator();

			Set<Hybridization> h1 = (((Experiment) o).getHybridizations());
			if (h1.size() > 0) {
				String[] hybridizationValues = new String[h1.size()];
				TreeSet<String> set = new TreeSet<String>();
				Hybridization oneHybridization = null;
				for (Hybridization h : h1) {
					oneHybridization = h;
					set.add(h.getName());
				}
				hybridizationValues = set.toArray(hybridizationValues);
				// below is to get the QuantitationType for each experiment.
				Hybridization hybridization = (Hybridization) (service
						.search(oneHybridization).get(0));
				DataSet dataSet = getDataSet(service, hybridization);
				String[] qTypes = null;
				Set<String> sets = new TreeSet<String>();
				if (dataSet != null) {
					List<QuantitationType> qList = dataSet
							.getQuantitationTypes();
					for (QuantitationType qType : qList) {

						Class typeClass = qType.getTypeClass();
						// Retrieve the appropriate data depending on the type
						// of the column.
						if (typeClass != String.class
								&& typeClass != Boolean.class) {
							sets.add(qType.getName());
						}
					}
					qTypes = new String[sets.size()];
					qTypes = sets.toArray(qTypes);
				}
				CaArray2Experiment exp = new CaArray2Experiment(url, port);

				exp.setName(e.getTitle());
				exp.setDescription(e.getDescription());
				exp.setHybridizations(hybridizationValues);
				exp.setQuantitationTypes(qTypes);
				exps.add(exp);

			}
		}
		CaArray2Experiment[] experimentsArray = new CaArray2Experiment[exps
				.size()];
		// experimentsArray = exps.toArray(experimentsArray);
		return exps.toArray(experimentsArray);
	}

	public TreeMap<String, String[]> lookupExperiments(
			CaArraySearchService service, DataRetrievalRequest request) {
		experimentDesciptions = new TreeMap<String, String>();
		TreeMap<String, String[]> tree = new TreeMap<String, String[]>();
		CQLQuery query = CQLQueryGenerator
				.generateQuery(CQLQueryGenerator.EXPERIMENT);
		List list = service.search(query);
		for (Object o : list) {
			Experiment e = ((Experiment) o);
			// log.debug("Experiment : " + e.getTitle() + "| "
			// + e.getManufacturer().getName() + "|" +
			// e.getPrimaryInvestigator() + "|" + e.getMainPointOfContact() +
			// "|" + e.getExperimentContacts());
			ExperimentContact contact = e.getPrimaryInvestigator();

			Set<Hybridization> h1 = (((Experiment) o).getHybridizations());
			if (h1.size() > 0) {
				String[] hybridizationValues = new String[h1.size()];
				int i = 0;
				for (Hybridization h : h1) {
					hybridizationValues[i++] = h.getName();
				}
				tree.put(e.getTitle(), hybridizationValues);
				experimentDesciptions.put(e.getTitle(), e.getDescription());
			}
		}
		return tree;
	}

	/**
	 * The method to query caArray server to return valid values for a type. For
	 * example, return all valid Organisms in caArray.
	 * 
	 * @param service
	 * @param request
	 * @param type
	 * @return
	 */
	public static TreeMap<String, Set<String>> lookupTypeValues(
			CaArraySearchService service, DataRetrievalRequest request,
			String[] types) {
		log.debug("Get into lookupTypeValues");
		TreeMap<String, Set<String>> tree = new TreeMap<String, Set<String>>();
		CQLQuery query = new CQLQuery();
		query.setTarget(new gov.nih.nci.cagrid.cqlquery.Object());
		String target = "edu.georgetown.pir.Organism";
		StopWatch stopwatch = new StopWatch();

		for (String type : types) {
//			if (type.equalsIgnoreCase(CaARRAYQueryPanel.ORGANISM)) {
//				target = "edu.georgetown.pir.Organism";
//			} else if (type.equalsIgnoreCase(CaARRAYQueryPanel.PINAME)) {
//				target = "gov.nih.nci.caarray.domain.contact.Person";
//			} else if (type.equalsIgnoreCase(CaARRAYQueryPanel.CHIPPROVIDER)) {
//				target = "gov.nih.nci.caarray.domain.contact.Organization";
//			} else if (type.equalsIgnoreCase(CaARRAYQueryPanel.TISSUETYPE)) {
//				target = "gov.nih.nci.caarray.domain.sample.AbstractBioMaterial";
//			}
//
//			if (type.equalsIgnoreCase(CaARRAYQueryPanel.TISSUETYPE)) {
//
//				gov.nih.nci.cagrid.cqlquery.Object object = new gov.nih.nci.cagrid.cqlquery.Object();
//				Association termAssociation = new Association();
//				termAssociation
//						.setName("gov.nih.nci.caarray.domain.vocabulary.Term");
//				Attribute termAttribute = new Attribute();
//				termAttribute.setName("value");
//
//				termAttribute.setPredicate(Predicate.IS_NOT_NULL);
//				termAssociation.setAttribute(termAttribute);
//				termAssociation.setRoleName("tissueSite");
//				object.setAssociation(termAssociation);
//				object.setName(target);
//				query.setTarget(object);
//			} else {
//				query.getTarget().setName(target);
//			}
//			stopwatch.reset();
//			stopwatch.start();
			query = CQLQueryGenerator.generateQuery(type, type);
			List list = service.search(query);
			
			TreeSet<String> values = new TreeSet<String>();
			log.debug(type + " size = " + list.size());
			for (Object o : list) {
				stopwatch.reset();
				stopwatch.start();
				if (o instanceof Organism) {
					Organism e = ((Organism) o);
					if (e.getCommonName() != null) {
						values.add(e.getCommonName());
					}
				}
				if (o instanceof Person) {
					Person p = (Person) o;
					values.add(p.getFirstName() + " " + p.getLastName());
				}
				if (o instanceof AbstractBioMaterial) {
					AbstractBioMaterial p = (AbstractBioMaterial) o;
					if (p != null && p.getTissueSite() != null
							&& p.getTissueSite().getValue() != null) {
						String tissueSite = p.getTissueSite().getValue();
						log.debug(type + " : " + tissueSite);
						values.add(tissueSite);
					} else {
						// log.debug("NULL TissueType");
					}

				}
				if (o instanceof Organization) {
					Organization p = (Organization) o;
					if (p.isProvider()) {

						values.add(p.getName());
					}
				}
				stopwatch.stop();
				// System.out.println("Need: " + stopwatch.getTime() + " for " +
				// o.getClass() );
			}
			tree.put(type, values);
		}
		return tree;
	}

	public static CQLQuery createQuery(String type) {
		CQLQuery query = new CQLQuery();
		query.setTarget(new gov.nih.nci.cagrid.cqlquery.Object());
		query.getTarget().setName(type);
		return query;
	}

	private void lookupQuantitationTypes(CaArraySearchService service,
			DataRetrievalRequest request) {
		String[] quantitationTypeNames = DEFAULT_QUANTITATION_TYPE.split(",");
		if (quantitationTypeNames == null) {
			return;
		}

		// Locate each quantitation type and add it to the request.
		QuantitationType exampleQuantitationType = new QuantitationType();
		for (int i = 0; i < quantitationTypeNames.length; i++) {
			String quantitationTypeName = quantitationTypeNames[i];
			exampleQuantitationType.setName(quantitationTypeName);
			List<QuantitationType> quantitationTypeList = service
					.search(exampleQuantitationType);
			request.getQuantitationTypes().addAll(quantitationTypeList);
		}
	}

	private Set<Hybridization> getAllHybridizations(
			List<Experiment> experimentList) {
		Set<Hybridization> hybridizations = new HashSet<Hybridization>();
		for (Experiment experiment : experimentList) {
			hybridizations.addAll(getAllHybridizations(experiment));
		}
		return hybridizations;
	}

	private Set<Hybridization> getAllHybridizations(Experiment experiment) {
		Set<Hybridization> hybridizations = new HashSet<Hybridization>();
		hybridizations.addAll(experiment.getHybridizations());
		return hybridizations;
	}
}
