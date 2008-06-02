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

import gov.nih.nci.caarray.domain.data.AbstractDataColumn;
import gov.nih.nci.caarray.domain.data.BooleanColumn;
import gov.nih.nci.caarray.domain.data.DataSet;
import gov.nih.nci.caarray.domain.data.DerivedArrayData;
import gov.nih.nci.caarray.domain.data.DoubleColumn;
import gov.nih.nci.caarray.domain.data.FloatColumn;
import gov.nih.nci.caarray.domain.data.HybridizationData;
import gov.nih.nci.caarray.domain.data.IntegerColumn;
import gov.nih.nci.caarray.domain.data.LongColumn;
import gov.nih.nci.caarray.domain.data.QuantitationType;
import gov.nih.nci.caarray.domain.data.RawArrayData;
import gov.nih.nci.caarray.domain.data.ShortColumn;
import gov.nih.nci.caarray.domain.data.StringColumn;
import gov.nih.nci.caarray.domain.hybridization.Hybridization;
import gov.nih.nci.caarray.domain.project.Experiment;
import gov.nih.nci.caarray.services.CaArrayServer;
import gov.nih.nci.caarray.services.ServerConnectionException;
import gov.nih.nci.caarray.services.search.CaArraySearchService;
import gov.nih.nci.caarray.domain.data.*;
import gov.nih.nci.caarray.domain.array.*;

import java.util.List;
import java.util.Set;

import org.geworkbench.components.caarray.BaseApiClient;

/**
 * A client downloading a full data set corresponding to one data file through
 * CaArray's Remote Java API.
 */

public class OneFileDataSetDownloadClient extends BaseApiClient {
	// private static final String DEFAULT_EXPERIMENT_NAME = "Affymetrix
	// Specification with Data 01";
	private static final String DEFAULT_EXPERIMENT_NAME = "Affymetrix Experiment with CHP Data 01";// 133plus

	// 2
	// test

	public static void main(String[] args) {
		try {
			OneFileDataSetDownloadClient downloadClient = new OneFileDataSetDownloadClient();
			CaArrayServer server = new CaArrayServer(SERVER_NAME, JNDI_PORT);
			server.connect();
			CaArraySearchService searchService = server.getSearchService();

			Experiment experiment = downloadClient.lookupExperiment(
					searchService, DEFAULT_EXPERIMENT_NAME);
			if (experiment != null) {
				// Hybridization hybridization =
				// downloadClient.getFirstHybridization(searchService,
				// experiment);
				Set<Hybridization> allHybridizations = experiment
						.getHybridizations();
				for (Hybridization phybridization : allHybridizations) {
					Hybridization hybridization = searchService.search(
							phybridization).get(0);
					float[] chpSIgnalLogRatioArray = new float[345];
					if (hybridization != null) {
						log.debug("For Hybridization: "
								+ hybridization.getName());
						long startTime = System.currentTimeMillis();
						DataSet dataSet = downloadClient.getDataSet(
								searchService, hybridization);
						if (dataSet != null) {
							int numValuesRetrieved = 0;
							// Get each HybridizationData in the DataSet.
							for (HybridizationData oneHybData : dataSet
									.getHybridizationDataList()) {
								HybridizationData populatedHybData = searchService
										.search(oneHybData).get(0);
								// Get each column in the HybridizationData.
								for (AbstractDataColumn column : populatedHybData
										.getColumns()) {
									AbstractDataColumn populatedColumn = searchService
											.search(column).get(0);
									// Find the type of the column.
									QuantitationType qType = populatedColumn
											.getQuantitationType();
									log.debug(qType.getName() + " |"
											+ qType.getTypeClass() + experiment.getArrayDesigns());
									Class typeClass = qType.getTypeClass();
									// Retrieve the appropriate data depending
									// on the type of the column.
									 
									if (typeClass == String.class) {
										String[] values = ((StringColumn) populatedColumn)
												.getValues();
										numValuesRetrieved += values.length;
									} else if (typeClass == Float.class) {
										float[] values = ((FloatColumn) populatedColumn)
												.getValues();
										if(qType.getName().equalsIgnoreCase("CHPSignalLogRatio")){
											chpSIgnalLogRatioArray  =  values;
																					}
										numValuesRetrieved += values.length;
									} else if (typeClass == Short.class) {
										short[] values = ((ShortColumn) populatedColumn)
												.getValues();
										numValuesRetrieved += values.length;
									} else if (typeClass == Boolean.class) {
										boolean[] values = ((BooleanColumn) populatedColumn)
												.getValues();
										numValuesRetrieved += values.length;
									} else if (typeClass == Double.class) {
										double[] values = ((DoubleColumn) populatedColumn)
												.getValues();
										numValuesRetrieved += values.length;
									} else if (typeClass == Integer.class) {
										int[] values = ((IntegerColumn) populatedColumn)
												.getValues();
										numValuesRetrieved += values.length;
									} else if (typeClass == Long.class) {
										long[] values = ((LongColumn) populatedColumn)
												.getValues();
										numValuesRetrieved += values.length;
									} else {
										// Should never get here.
									}
								}
							}
							long endTime = System.currentTimeMillis();
							long totalTime = endTime - startTime;
							log.debug("Total Time: " + totalTime
									+ " ms.");
							log.debug("Retrieved "
									+ dataSet.getHybridizationDataList().size()
									+ " hybridization data elements, "
									+ dataSet.getQuantitationTypes().size()
									+ " quantitation types and "
									+ numValuesRetrieved + " values.");

						} else {
							System.out
									.println("Error: Retrieved null data set.");
						}
					} else {
						System.out
								.println("Error: Retrieved null hybridization.");
					}
					// Set<Hybridization> sets = downloadClient.
					// //getFirstHybridization(searchService, experiment);

				}
			} else {
				log.debug("Error: Could not find experiment.");
			}
		} catch (ServerConnectionException e) {
			log.debug("Server connection exception: " + e);
			e.printStackTrace();
		} catch (RuntimeException e) {
			log.debug("Runtime exception: " + e);
			e.printStackTrace();
		} catch (Throwable t) {
			// Catches things like out-of-memory errors and logs them.
			log.debug("Throwable: " + t);
			t.printStackTrace();
		}
	}

	Experiment lookupExperiment(CaArraySearchService service,
			String experimentName) {
		Experiment exampleExperiment = new Experiment();
		exampleExperiment.setTitle(experimentName);

		List<Experiment> experimentList = service.search(exampleExperiment);
		if (experimentList.size() == 0) {
			return null;
		}
		Experiment experiment = experimentList.get(0);
		return experiment;
	}

	private Hybridization getFirstHybridization(CaArraySearchService service,
			Experiment experiment) {
		Set<Hybridization> allHybridizations = experiment.getHybridizations();
		for (Hybridization hybridization : allHybridizations) {
			Hybridization populatedHybridization = service
					.search(hybridization).get(0);
			// Yes, we're returning only the first hybridization.
			return populatedHybridization;
		}
		return null;
	}

	 DataSet getDataSet(CaArraySearchService service,
			Hybridization hybridization) {
		DataSet dataSet = null;
		// Try to find raw data
		// RawArrayData rawArrayData = hybridization.getArrayData();
		// if (rawArrayData != null) {
		// // Return the data set associated with the first raw data.
		// RawArrayData populatedArrayData = service.search(rawArrayData).get(
		// 0);
		// dataSet = populatedArrayData.getDataSet();
		// }
		// else {
		if (true) {
			// If raw data doesn't exist, try to find derived data
			Set<DerivedArrayData> derivedArrayDataSet = hybridization
					.getDerivedDataCollection();
			for (DerivedArrayData derivedArrayData : derivedArrayDataSet) {
				// Return the data set associated with the first derived data.
				DerivedArrayData populatedArrayData = service.search(
						derivedArrayData).get(0);
				dataSet = populatedArrayData.getDataSet();
				List<DataSet> dataSetList = service.search(dataSet);
				DataSet data = dataSetList.get(0);
				DesignElementList designElementList = data
						.getDesignElementList();
				log.debug(designElementList.getDesignElementType());
				List<DesignElementList> designElementLists = service
						.search(designElementList);
				DesignElementList designElements = designElementLists.get(0);

				List<AbstractDesignElement> list = designElements
						.getDesignElements();
				for (AbstractDesignElement o : list) {
					List<AbstractDesignElement> adeList = service.search(o);
					log.debug(adeList.size() + " "
							+ ((LogicalProbe) adeList.get(0)).getName());
				}
				break;
			}
		}
		if (dataSet == null) {
			return null;
		} else {
			return service.search(dataSet).get(0);
		}
	}
}
