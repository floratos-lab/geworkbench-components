package org.geworkbench.components.cgems;

import gov.nih.nci.caintegrator.domain.analysis.snp.SNPAnalysisGroup;
import gov.nih.nci.caintegrator.domain.analysis.snp.SNPAssociationAnalysis;
import gov.nih.nci.caintegrator.domain.analysis.snp.SNPAssociationFinding;
import gov.nih.nci.caintegrator.domain.annotation.gene.GeneBiomarker;
import gov.nih.nci.caintegrator.domain.annotation.snp.SNPAnnotation;
import gov.nih.nci.caintegrator.domain.annotation.snp.SNPAssay;
import gov.nih.nci.caintegrator.domain.annotation.snp.SNPPanel;
import gov.nih.nci.caintegrator.domain.finding.variation.snpFrequency.SNPFrequencyFinding;
import gov.nih.nci.caintegrator.domain.study.Population;
import gov.nih.nci.caintegrator.domain.study.Specimen;
import gov.nih.nci.caintegrator.domain.study.Study;
import gov.nih.nci.common.util.HQLCriteria;
import gov.nih.nci.system.applicationservice.ApplicationService;
import gov.nih.nci.system.applicationservice.ApplicationServiceProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2001-2004 SAIC. Copyright 2001-2003 SAIC. This software was developed in conjunction with the National Cancer Institute,
 * and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions
 * in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
 * "This product includes software developed by the SAIC and the National Cancer Institute."
 * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself,
 * wherever such third-party acknowledgments normally appear.
 * 3. The names "The National Cancer Institute", "NCI" and "SAIC" must not be used to endorse or promote products derived from this software.
 * 4. This license does not authorize the incorporation of this software into any third party proprietary programs. This license does not authorize
 * the recipient to use any trademarks owned by either NCI or SAIC-Frederick.
 * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 * SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <!-- LICENSE_TEXT_END -->
 */

/**
 * @author CGEMS Team
 * @version 1.0
 */

/**
 * TestClient.java demonstartes various ways to execute searches with and
 * without using Application Service Layer (convenience layer that abstracts
 * building criteria Uncomment different scenarios below to demonstrate the
 * various types of searches
 */

/**
 * @author sahnih, zhangd, guruswamis
 */
public class TestClient {

	public static void main(String[] args) {
		System.out.println("*** TestClient...");
		try {
			searchStudy();
			searchSNPPanel();
			searchSNPAssociationAnalysis();
			searchSNPAnalysisGroup();
			searchPopulation();			
			searchSNPAnnoation();
			searchSNPAssociationFinding();
			searchSNPFrequencyFinding();
			searchGeneBiomarker();
			searchSNPAssay();
			searchSNPAssayHQL();
		} catch (RuntimeException e) {
			e.printStackTrace();
			System.out.println("Test client throws Exception = " + e);
		}
	}

	/**
	 * Scenario 1: Retrieve all SNPPanels No search criteria specified,
	 * resulting in an unrestricted search
	 */
	private static void searchSNPPanel() {
		SNPPanel snpPanel = new SNPPanel();
		try {
			System.out
					.println("______________________________________________________________________");
			System.out.println("Retrieving all SNPPanels...");
			ApplicationService appService = ApplicationServiceProvider
					.getApplicationService();

			List resultList = appService.search(SNPPanel.class, snpPanel);
			if (resultList != null) {
				System.out.println("Number of results returned: "
						+ resultList.size());
				for (Iterator resultsIterator = resultList.iterator(); resultsIterator
						.hasNext();) {
					SNPPanel returnedObj = (SNPPanel) resultsIterator.next();
					System.out.println("Panel Name: " + returnedObj.getName()
							+ "\n" + "Description: "
							+ returnedObj.getDescription() + "\n"
							+ "Technology: " + returnedObj.getTechnology()
							+ "\n" + "Vendor: " + returnedObj.getVendor()
							+ "\n" + "Vendor PanelId: "
							+ returnedObj.getVendorPanelId() + "\n"
							+ "Version: " + returnedObj.getVersion() + "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void searchSNPAssociationAnalysis() {
		SNPAssociationAnalysis snpAnalysis = new SNPAssociationAnalysis();
		try {
			System.out
					.println("______________________________________________________________________");
			System.out.println("Retrieving all SNPAssociationAnalysis...");
			ApplicationService appService = ApplicationServiceProvider
					.getApplicationService();

			List resultList = appService.search(SNPAssociationAnalysis.class,
					snpAnalysis);
			if (resultList != null) {
				System.out.println("Number of results returned: "
						+ resultList.size());
				for (Iterator resultsIterator = resultList.iterator(); resultsIterator
						.hasNext();) {
					SNPAssociationAnalysis returnedObj = (SNPAssociationAnalysis) resultsIterator
							.next();
					System.out.println("Name: " + returnedObj.getName() + "\n"
							+ "Description: " + returnedObj.getDescription()
							+ "\n" + "Methods: " + returnedObj.getMethods()
							+ "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void searchStudy() {
		Study study = new Study();
		try {
			System.out
					.println("______________________________________________________________________");
			System.out.println("Retrieving all Study objects...");
			ApplicationService appService = ApplicationServiceProvider
					.getApplicationService();

			List resultList = appService.search(Study.class, study);
			if (resultList != null) {
				System.out.println("Number of results returned: "
						+ resultList.size());
				for (Iterator resultsIterator = resultList.iterator(); resultsIterator
						.hasNext();) {
					Study returnedObj = (Study) resultsIterator.next();
					System.out.println("Name: " + returnedObj.getName() + "\n"
							+ "Description: " + returnedObj.getDescription()
							+ "\n" + "SponsorStudyIdentifier: "
							+ returnedObj.getSponsorStudyIdentifier());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void searchSNPAnalysisGroup() {
		SNPAnalysisGroup snpAnalysisGroup = new SNPAnalysisGroup();
		try {
			System.out
					.println("______________________________________________________________________");
			System.out.println("Retrieving all SNPAnalysisGroup...");
			ApplicationService appService = ApplicationServiceProvider
					.getApplicationService();

			List resultList = appService.search(SNPAnalysisGroup.class,
					snpAnalysisGroup);
			if (resultList != null) {
				System.out.println("Number of results returned: "
						+ resultList.size());
				for (Iterator resultsIterator = resultList.iterator(); resultsIterator
						.hasNext();) {
					SNPAnalysisGroup returnedObj = (SNPAnalysisGroup) resultsIterator
							.next();
					System.out.println("Name: " + returnedObj.getName() + "\n"
							+ "Description: " + returnedObj.getDescription()
							+ "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	/**
	 * Scenario Three: Simple Search (Criteria Object Collection) to retrieve
	 * SNPFrequencyFinding for the Gene “WT1” In this example, a search is
	 * performed for WT1 genes to retrieve the SNPFrequencyFinding. The code
	 * iterates through the returned objects and prints out the several
	 * properties of each of the object, as shown in the code listing.
	 */
	@SuppressWarnings( { "unused", "unchecked" })
	private static void searchSNPFrequencyFinding() {
		Collection geneBiomarkerCollection = new ArrayList();
		GeneBiomarker wt1 = new GeneBiomarker();
		wt1.setHugoGeneSymbol("WT1");
		geneBiomarkerCollection.add(wt1);

		SNPAnnotation snpAnnotation = new SNPAnnotation();
		snpAnnotation.setGeneBiomarkerCollection(geneBiomarkerCollection);

		SNPFrequencyFinding snpFrequencyFinding = new SNPFrequencyFinding();
		snpFrequencyFinding.setSnpAnnotation(snpAnnotation);
		try {
			System.out
					.println("______________________________________________________________________");
			System.out
					.println("Retrieving all SNPFrequencyFinding objects for WT1");
			ApplicationService appService = ApplicationServiceProvider
					.getApplicationService();

			List resultList = appService.search(SNPFrequencyFinding.class,
					snpAnnotation);
			if (resultList != null) {
				System.out.println("Number of results returned: "
						+ resultList.size());
				System.out.println("DbsnpId" + "\t" + "ChromosomeName" + "\t"
						+ "ChromosomeLocation" + "\t" + "MinorAlleleFrequency"
						+ "\t" + "HardyWeinbergPValue" + "\t"
						+ "ReferenceAllele" + "\t" + "OtherAllele" + "\t"
						+ "Population" + "\n");
				for (Iterator resultsIterator = resultList.iterator(); resultsIterator
						.hasNext();) {
					SNPFrequencyFinding returnedObj = (SNPFrequencyFinding) resultsIterator
							.next();
					System.out.println(returnedObj.getSnpAnnotation()
							.getDbsnpId()
							+ "\t"
							+ returnedObj.getSnpAnnotation()
									.getChromosomeName()
							+ "\t"
							+ returnedObj.getSnpAnnotation()
									.getChromosomeLocation()
							+ "\t"
							+ returnedObj.getMinorAlleleFrequency()
							+ "\t"
							+ returnedObj.getHardyWeinbergPValue()
							+ "\t"
							+ returnedObj.getReferenceAllele()
							+ "\t"
							+ returnedObj.getOtherAllele()
							+ "\t"
							+ returnedObj.getPopulation().getName() + "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Senario Four: Nested Search to retrieve SNPAssays based on dbSnpId A
	 * nested search is one where a traversal of more than one class-class
	 * association is required to obtain a set of result objects given the
	 * criteria object. This example demon-strates one such search in which the
	 * criteria object passed to the search method is of type SNPAnnotation, and
	 * the desired objects are of type SNPAssay.
	 */
	@SuppressWarnings( { "unused", "unchecked" })
	private static void searchSNPAssay() {
		SNPAnnotation snpAnnotation = new SNPAnnotation();
		snpAnnotation.setDbsnpId("rs5030335");
		SNPAssay snpAssay = new SNPAssay();
		snpAssay.setSnpAnnotation(snpAnnotation);
		try {
			System.out
					.println("______________________________________________________________________");
			System.out.println("Retrieving all SNPAssay objects for rs5030335");
			ApplicationService appService = ApplicationServiceProvider
					.getApplicationService();

			List resultList = appService.search(SNPAssay.class, snpAnnotation);
			if (resultList != null) {
				System.out.println("Number of results returned: "
						+ resultList.size());
				System.out.println("Vender Assay ID" + "\t" + "DbsnpId" + "\t"
						+ "ChromosomeName" + "\t" + "ChromosomeLocation" + "\t"
						+ "SNP Panel" + "\t" + "Version" + "\t"
						+ "DesignAlleles" + "\t" + "Status" + "\n");
				for (Iterator resultsIterator = resultList.iterator(); resultsIterator
						.hasNext();) {
					SNPAssay returnedObj = (SNPAssay) resultsIterator.next();
					System.out.println(returnedObj.getVendorAssayId()
							+ "\t"
							+ returnedObj.getSnpAnnotation().getDbsnpId()
							+ "\t"
							+ returnedObj.getSnpAnnotation()
									.getChromosomeName()
							+ "\t"
							+ returnedObj.getSnpAnnotation()
									.getChromosomeLocation() + "\t"
							+ returnedObj.getSnpPanel().getName() + "\t"
							+ returnedObj.getVersion() + "\t"
							+ returnedObj.getDesignAlleles() + "\t"
							+ returnedObj.getStatus() + "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void searchPopulation() {
		Population population = new Population();
		try {
			System.out
					.println("______________________________________________________________________");
			System.out.println("Retrieving all Population types");
			ApplicationService appService = ApplicationServiceProvider
					.getApplicationService();

			List resultList = appService.search(Population.class, population);
			if (resultList != null) {
				System.out.println("Number of results returned: "
						+ resultList.size());
				for (Iterator resultsIterator = resultList.iterator(); resultsIterator
						.hasNext();) {
					Population returnedObj = (Population) resultsIterator
							.next();
					System.out.println("Name: " + returnedObj.getName() + "\n"
							+ "Description: " + returnedObj.getDescription()
							+ "\n" + "Source: " + returnedObj.getSource()
							+ "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static void searchSNPAssociationFinding() {
		Collection geneBiomarkerCollection = new ArrayList();
		GeneBiomarker wt1 = new GeneBiomarker();
		wt1.setHugoGeneSymbol("WT1");
		geneBiomarkerCollection.add(wt1);

		SNPAnnotation snpAnnotation = new SNPAnnotation();
		snpAnnotation.setGeneBiomarkerCollection(geneBiomarkerCollection);
		try {
			System.out
					.println("______________________________________________________________________");
			System.out.println("Retrieving all SNPAssiciationFindings for WT1");
			ApplicationService appService = ApplicationServiceProvider
					.getApplicationService();

			List resultList = appService.search(SNPAssociationFinding.class,
					snpAnnotation);
			if (resultList != null) {
				System.out.println("Number of results returned: "
						+ resultList.size());
				System.out.println("DbsnpId" + "\t" + "ChromosomeName" + "\t"
						+ "ChromosomeLocation" + "\t" + "GenomeBuild" + "\t"
						+ "ReferenceSequence" + "\t" + "ReferenceStrand" + "\t"
						+ "GeneBiomarker(s)" + "\t" + "Analysis Name" + "\t"
						+ "p-Value" + "\t" + "rank" + "\n");
				for (Iterator resultsIterator = resultList.iterator(); resultsIterator
						.hasNext();) {
					SNPAssociationFinding returnedObj = (SNPAssociationFinding) resultsIterator
							.next();
					System.out.println(returnedObj.getSnpAnnotation()
							.getDbsnpId()
							+ "\t"
							+ returnedObj.getSnpAnnotation()
									.getChromosomeName()
							+ "\t"
							+ returnedObj.getSnpAnnotation()
									.getChromosomeLocation()
							+ "\t"
							+ pipeGeneBiomarkers(returnedObj.getSnpAnnotation()
									.getGeneBiomarkerCollection())
							+ "\t"
							+ returnedObj.getSnpAssociationAnalysis().getName()
							+ "\t"
							+ returnedObj.getPvalue()
							+ "\t"
							+ returnedObj.getRank() + "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This example demonstrates the use of Hibernate detached criteria objects
	 * to formulate and perform more sophisticated searches. for more
	 * information, please consult the Hibernate documentation at
	 * http://www.hibernate.org/hib_docs/v3/api/org/hibernate/criterion/DetachedCriteria.html
	 */
	@SuppressWarnings("unused")
	private static void searchSNPAnnoation() {
		DetachedCriteria criteria = DetachedCriteria
				.forClass(SNPAnnotation.class);
		criteria.add(Restrictions
				.ge("chromosomeLocation", new Integer(4000000)));
		criteria.add(Restrictions
				.le("chromosomeLocation", new Integer(4200000)));
		criteria.add(Restrictions.eq("chromosomeName", "1"));
		try {
			System.out
					.println("______________________________________________________________________");
			System.out
					.println("Retrieving all SNPAnnotations for Chr 1,4000000 - 4200000");
			ApplicationService appService = ApplicationServiceProvider
					.getApplicationService();

			List resultList = appService.query(criteria, SNPAnnotation.class
					.getName());
			if (resultList != null) {
				System.out.println("Number of results returned: "
						+ resultList.size());
				System.out.println("DbsnpId" + "\t" + "ChromosomeName" + "\t"
						+ "ChromosomeLocation" + "\t" + "GenomeBuild" + "\t"
						+ "ReferenceSequence" + "\t" + "ReferenceStrand" + "\t"
						+ "GeneBiomarker(s)" + "\n");
				for (Iterator resultsIterator = resultList.iterator(); resultsIterator
						.hasNext();) {
					SNPAnnotation returnedObj = (SNPAnnotation) resultsIterator
							.next();
					System.out.println(returnedObj.getDbsnpId()
							+ "\t"
							+ returnedObj.getChromosomeName()
							+ "\t"
							+ returnedObj.getChromosomeLocation()
							+ "\t"
							+ returnedObj.getGenomeBuild()
							+ "\t"
							+ returnedObj.getReferenceSequence()
							+ "\t"
							+ returnedObj.getReferenceStrand()
							+ "\t"
							+ pipeGeneBiomarkers(returnedObj
									.getGeneBiomarkerCollection()) + "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This example demostrates using HQL to retrieve all SNPAssay, with id <
	 * 100. It uses a Hibernate Query Language (HQL) search string to form the
	 * query. For more information on HQL syntax, consult the Hibernate
	 * documentation at
	 * http://www.hibernate.org/hib_docs/v3/reference/en/html/queryhql. html.
	 */
	private static void searchSNPAssayHQL() {
		String hqlString = "FROM SNPAssay a WHERE a.id < 100";
		HQLCriteria hqlC = new HQLCriteria(hqlString);
		try {
			System.out
					.println("______________________________________________________________________");
			System.out.println("Retrieving all SNPAssay objects, id < 100");
			ApplicationService appService = ApplicationServiceProvider
					.getApplicationService();
			List resultList = appService.query(hqlC, SNPAnnotation.class
					.getName());
			if (resultList != null) {
				if (resultList != null) {
					System.out.println("Number of results returned: "
							+ resultList.size());
					System.out.println("Id\t" + "Vender Assay ID" + "\t"
							+ "SNP Panel" + "\t" + "Version" + "\t"
							+ "DesignAlleles" + "\t" + "Status" + "\n");
					for (Iterator resultsIterator = resultList.iterator(); resultsIterator
							.hasNext();) {
						SNPAssay returnedObj = (SNPAssay) resultsIterator
								.next();
						System.out.println(returnedObj.getId() + "\t"
								+ returnedObj.getVendorAssayId() + "\t"
								+ returnedObj.getSnpPanel().getName() + "\t"
								+ returnedObj.getVersion() + "\t"
								+ returnedObj.getDesignAlleles() + "\t"
								+ returnedObj.getStatus() + "\n");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings( { "unused", "unchecked" })
	private static void searchGeneBiomarker() {
		/*
		 * This example demonstrates the use of Hibernate detached criteria
		 * objects to formulate and perform more sophisticated searches. A
		 * detailed description of detached criteria is beyond the scope of this
		 * example; for more information, please consult the Hibernate
		 * documentation at
		 * http://www.hibernate.org/hib_docs/v3/api/org/hibernate/criterion/DetachedCriteria.html
		 */

		DetachedCriteria criteria = DetachedCriteria
				.forClass(GeneBiomarker.class);
		criteria.add(Restrictions
				.gt("startPhyscialLocation", new Long(6000000)));
		criteria.add(Restrictions.lt("endPhysicalLocation", new Long(6300000)));
		criteria.add(Restrictions.eq("chromosome", "19"));

		try {
			System.out
					.println("______________________________________________________________________");
			System.out
					.println("Retrieving all GeneBiomarker objects for Chr 19,6000000 - 6300000");
			ApplicationService appService = ApplicationServiceProvider
					.getApplicationService();

			List resultList = appService.query(criteria, GeneBiomarker.class
					.getName());
			if (resultList != null) {
				System.out.println("Number of results returned: "
						+ resultList.size());
				System.out.println("ChromosomeName" + "\t"
						+ "StartPhyscialLocation" + "\t"
						+ "EndPhysicalLocation" + "\t" + "HugoGeneSymbol"
						+ "\n");
				for (Iterator resultsIterator = resultList.iterator(); resultsIterator
						.hasNext();) {
					GeneBiomarker returnedObj = (GeneBiomarker) resultsIterator
							.next();
					System.out.println(returnedObj.getChromosome() + "\t"
							+ returnedObj.getStartPhyscialLocation() + "\t"
							+ returnedObj.getEndPhysicalLocation() + "\t"
							+ returnedObj.getHugoGeneSymbol() + "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String pipeGeneBiomarkers(Collection geneBiomarkerCollection) {
		String geneList = "";
		if (geneBiomarkerCollection != null) {
			for (Object object : geneBiomarkerCollection) {
				GeneBiomarker geneBiomarker = (GeneBiomarker) object;
				geneList = geneList + geneBiomarker.getHugoGeneSymbol() + "|";
			}
			// remove Last |
			if (geneList.endsWith("|")) {
				geneList = geneList.substring(0, geneList.lastIndexOf("|"));
			}
		}
		return geneList;
	}
}
