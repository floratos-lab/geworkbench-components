package org.geworkbench.components.caarray.arraydata;

import gov.nih.nci.cagrid.cqlquery.Association;
import gov.nih.nci.cagrid.cqlquery.Attribute;
import gov.nih.nci.cagrid.cqlquery.CQLQuery;
import gov.nih.nci.cagrid.cqlquery.Group;
import gov.nih.nci.cagrid.cqlquery.LogicalOperator;
import gov.nih.nci.cagrid.cqlquery.Predicate;

import org.geworkbench.builtin.projects.remoteresources.query.CaARRAYQueryPanel;

/**
 * @author xiaoqing
 * @version $Id$
 */
public class CQLQueryGenerator {
	public final static String EXPERIMENT = "Experiment";

	/**
	 * Create a CQLQuery to get all Objects for the class target name.
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
			} else {
				object.setName(targetname);
				query.setTarget(object);
			}
			return query;
		}
		return null;
	}

	/**
	 * Create a CQLQuery to get a CQLQuery with a filter with the type:value
	 * pair.
	 * 
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
					String[] name = value.split(StandAloneCaArrayClientExec.NAME_SEPARATOR);
					if (name.length<2)
						return null;
					String firstName = name[1];
					String lastName = name[0];

					Association contactAssociation = new Association();
					contactAssociation
							.setName("gov.nih.nci.caarray.domain.project.ExperimentContact");
					Association organismAssociation = new Association();
					organismAssociation
							.setName("gov.nih.nci.caarray.domain.contact.Person");
					Attribute att = new Attribute(); 
					att.setName("firstName"); 
					att.setPredicate(Predicate.EQUAL_TO); 
					att.setValue(firstName); 
					Attribute att2 = new Attribute(); 
					att2.setName("lastName"); 
					att2.setPredicate(Predicate.EQUAL_TO); 
					att2.setValue(lastName);
					Attribute[] atts = new Attribute[2];
					atts[0]=att;
					atts[1]=att2;
					Group group = new Group();
					group.setAttribute(atts);
					group.setLogicRelation(LogicalOperator.AND);
					organismAssociation.setGroup(group);
					organismAssociation.setRoleName("contact");
					contactAssociation.setAssociation(organismAssociation);
					contactAssociation.setRoleName("experimentContacts");
					object.setAssociation(contactAssociation);
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
	 * Create a CQLQuery to get the valid values for one search criteria.
	 * 
	 * @param type
	 * @param value
	 * @return
	 */
	static CQLQuery generateQuery(String type, String value) {
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