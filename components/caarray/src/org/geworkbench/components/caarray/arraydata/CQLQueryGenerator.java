/**
 * 
 */
package org.geworkbench.components.caarray.arraydata;

import org.apache.commons.lang.time.StopWatch;
import org.geworkbench.builtin.projects.remoteresources.query.CaARRAYQueryPanel;

import gov.nih.nci.cagrid.cqlquery.Association;
import gov.nih.nci.cagrid.cqlquery.Attribute;
import gov.nih.nci.cagrid.cqlquery.CQLQuery;
import gov.nih.nci.cagrid.cqlquery.Predicate;

/**
 * @author xiaoqing
 * 
 */
public class CQLQueryGenerator {
	public final static String EXPERIMENT = "Experiment";
/**
 * Create a CQLQuery to get all Objects for the class targetname.
 * 
 * @param targetname
 * @return
 */
	static CQLQuery generateQuery(String targetname) {

		CQLQuery query = new CQLQuery();
		gov.nih.nci.cagrid.cqlquery.Object object = new gov.nih.nci.cagrid.cqlquery.Object();
		if (targetname != null) {
			if (targetname.equalsIgnoreCase(EXPERIMENT)) {
				String target = "gov.nih.nci.caarray.domain.project.Experiment";
				object.setName(target);
				query.setTarget(object);
			}else{
				object.setName(targetname);
				query.setTarget(object);
			}
			return query;
		}
		return null;
	}

	/**
	 * Create a CQLQuery to get a CQLQuery with a filter with the type:value pair.
	 * @param targetname
	 * @param type
	 * @param value
	 * @return
	 */
	static CQLQuery generateQuery(String targetname, String type, String value) {

		CQLQuery query = new CQLQuery();
		gov.nih.nci.cagrid.cqlquery.Object object = new gov.nih.nci.cagrid.cqlquery.Object();
		if (targetname != null) {
			if (targetname.equalsIgnoreCase(EXPERIMENT)) {
				String target = "gov.nih.nci.caarray.domain.project.Experiment";
				object.setName(target);
				if (type.equalsIgnoreCase(CaARRAYQueryPanel.ORGANISM)) {
					String organismName = value;
					Association organismAssociation = new Association();
					organismAssociation.setName("edu.georgetown.pir.Organism");
					Attribute organismAttribute = new Attribute();
					organismAttribute.setName("commonName");
					organismAttribute.setValue(organismName);
					organismAttribute.setPredicate(Predicate.EQUAL_TO);
					organismAssociation.setAttribute(organismAttribute);
					organismAssociation.setRoleName("organism");
					object.setAssociation(organismAssociation);
				} else if (type.equalsIgnoreCase(CaARRAYQueryPanel.PINAME)) {
					Association organismAssociation = new Association();
					organismAssociation
							.setName("gov.nih.nci.caarray.domain.project.ExperimentContact");
					Attribute organismAttribute = new Attribute();
					organismAttribute.setName("name");
					organismAttribute.setValue(value);
					organismAttribute.setPredicate(Predicate.EQUAL_TO);
					organismAssociation.setAttribute(organismAttribute);
					organismAssociation.setRoleName("manufacturer");
					object.setAssociation(organismAssociation);
				} else if (type
						.equalsIgnoreCase(CaARRAYQueryPanel.CHIPPROVIDER)) {
					Association organismAssociation = new Association();
					organismAssociation
							.setName("gov.nih.nci.caarray.domain.contact.Organization");
					Attribute organismAttribute = new Attribute();
					organismAttribute.setName("name");
					organismAttribute.setValue(value);
					organismAttribute.setPredicate(Predicate.EQUAL_TO);
					organismAssociation.setAttribute(organismAttribute);
					organismAssociation.setRoleName("manufacturer");
					object.setAssociation(organismAssociation);
				} else if (type.equalsIgnoreCase(CaARRAYQueryPanel.TISSUETYPE)) {
					Association termAssociation = new Association();
					termAssociation
							.setName("gov.nih.nci.caarray.domain.vocabulary.Term");
					Attribute termAttribute = new Attribute();
					termAttribute.setName("value");
					termAttribute.setValue(value);
					termAttribute.setPredicate(Predicate.EQUAL_TO);
					termAssociation.setAttribute(termAttribute);
					termAssociation.setRoleName("tissueSite");
					Association sourceAssociation = new Association();
					sourceAssociation
							.setName("gov.nih.nci.caarray.domain.sample.Source");
					sourceAssociation.setRoleName("sources");
					sourceAssociation.setAssociation(termAssociation);
					object.setAssociation(sourceAssociation);
				}

			}
			query.setTarget(object);
			return query;
		}
		return null;
	}
/**
 * Create a CQLQuery to get the valid values for one search critiria.
 * @param type
 * @param value
 * @return
 */
	public static CQLQuery generateQuery(String type, String value) {
		CQLQuery query = new CQLQuery();
		query.setTarget(new gov.nih.nci.cagrid.cqlquery.Object());
		String target = "edu.georgetown.pir.Organism";
		if (type.equalsIgnoreCase(CaARRAYQueryPanel.ORGANISM)) {
			target = "edu.georgetown.pir.Organism";
		} else if (type.equalsIgnoreCase(CaARRAYQueryPanel.PINAME)) {
			target = "gov.nih.nci.caarray.domain.contact.Person";
		} else if (type.equalsIgnoreCase(CaARRAYQueryPanel.CHIPPROVIDER)) {
			target = "gov.nih.nci.caarray.domain.contact.Organization";
		} else if (type.equalsIgnoreCase(CaARRAYQueryPanel.TISSUETYPE)) {
			target = "gov.nih.nci.caarray.domain.sample.AbstractBioMaterial";
		}

		if (type.equalsIgnoreCase(CaARRAYQueryPanel.TISSUETYPE)) {

			gov.nih.nci.cagrid.cqlquery.Object object = new gov.nih.nci.cagrid.cqlquery.Object();
			Association termAssociation = new Association();
			termAssociation
					.setName("gov.nih.nci.caarray.domain.vocabulary.Term");
			Attribute termAttribute = new Attribute();
			termAttribute.setName("value");
			termAttribute.setPredicate(Predicate.IS_NOT_NULL);
			termAssociation.setAttribute(termAttribute);
			termAssociation.setRoleName("tissueSite");
			object.setAssociation(termAssociation);
			object.setName(target);
			query.setTarget(object);
		} else {
			query.getTarget().setName(target);
		}
		return query;
	}
}