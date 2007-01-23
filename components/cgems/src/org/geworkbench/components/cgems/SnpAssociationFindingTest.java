package org.geworkbench.components.cgems;

import gov.nih.nci.caintegrator.domain.analysis.snp.SNPAssociationFinding;
import gov.nih.nci.caintegrator.domain.annotation.gene.GeneBiomarker;
import gov.nih.nci.caintegrator.domain.annotation.snp.SNPAnnotation;
import gov.nih.nci.system.applicationservice.ApplicationService;
import gov.nih.nci.system.applicationservice.ApplicationServiceProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * 
 * @author keshav
 * @version $Id: SnpAssociationFindingTest.java,v 1.2 2007-01-23 19:10:35 keshav Exp $
 */
public class SnpAssociationFindingTest extends TestCase {

	/**
	 * Gets snp data from caIntegrator.
	 * 
	 */
	private void searchSNPAssociationFinding() {
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
	 * 
	 * @param geneName
	 * @return GeneBiomarker
	 */
	private GeneBiomarker createGeneBioMarker(String geneName) {
		GeneBiomarker geneBiomarker = new GeneBiomarker();
		geneBiomarker.setHugoGeneSymbol(geneName);
		return geneBiomarker;
	}

	/**
	 * 
	 * @param geneBiomarkerCollection
	 * @return String
	 */
	private static String pipeGeneBiomarkers(Collection geneBiomarkerCollection) {
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

	/**
	 * Tests getting snp data from caintegrator
	 * 
	 */
	public void testSearchSnpAssociationFinding() {
		/*
		 * Note: org.hibernate.LazyInitializationException that is seen on the
		 * console is a caCore 3.1 issue, and has been fixed in caCore 3.2. This
		 * does not effect retrieving the data.
		 */
		CGemsPanel cGemsPanel = new CGemsPanel();
		searchSNPAssociationFinding();
	}

}
