package edu.columbia.geworkbench.cagrid.discovery.client;

import gov.nih.nci.cagrid.common.Utils;
import gov.nih.nci.cagrid.discovery.MetadataConstants;
import gov.nih.nci.cagrid.discovery.MetadataUtils;
import gov.nih.nci.cagrid.discovery.ResourcePropertyHelper;
import gov.nih.nci.cagrid.discovery.XPathUtils;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;
import gov.nih.nci.cagrid.metadata.common.PointOfContact;
import gov.nih.nci.cagrid.metadata.common.UMLClass;

import java.util.HashMap;
import java.util.Map;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.wsrf.WSRFConstants;
import org.globus.wsrf.encoding.ObjectDeserializer;

/**
 * DiscoveryClient represents the base discovery API. The client should be constructed passing a URL of an Index
 * Service. Services can then be discovered by calling the discover methods, passing in the necessary criteria. The
 * methods all return a EndPointReferenceType[]. See the main method for examples. This should be extended to provide
 * specialized service-type discovery (beyond data services).
 * 
 * @author keshav
 * @author oster
 */
public class DiscoveryClient {

    protected static final String DEFAULT_INDEX_SERVICE_URL = "http://cagrid04.bmi.ohio-state.edu:7080/wsrf/services/DefaultIndexService";
    protected EndpointReferenceType indexEPR = null;

    // Define the prefixes
    protected static final String wssg = WSRFConstants.SERVICEGROUP_PREFIX;
    protected static final String agg = "agg";
    protected static final String cagrid = "cagrid";
    protected static final String com = "com";
    protected static final String serv = "serv";
    protected static final String data = "data";
    protected static final String uml = "uml";

    // some common paths for reuse
    protected static final String CONTENT_PATH = wssg + ":Content/" + agg + ":AggregatorData";

    protected static final String MD_PATH = CONTENT_PATH + "/" + cagrid + ":ServiceMetadata";
    protected static final String SERV_PATH = MD_PATH + "/" + cagrid + ":serviceDescription/" + serv + ":Service";
    protected static final String OPER_PATH = SERV_PATH + "/" + serv + ":serviceContextCollection/" + serv
            + ":ServiceContext/" + serv + ":operationCollection/" + serv + ":Operation";

    protected static final String DATA_MD_PATH = CONTENT_PATH + "/" + data + ":DomainModel";

    // Map the prefixes to there namepsaces
    protected static Map nsMap = new HashMap();
    static {
        nsMap.put( wssg, WSRFConstants.SERVICEGROUP_NS );
        nsMap.put( agg, MetadataConstants.AGGREGATOR_NAMESPACE );
        nsMap.put( cagrid, MetadataConstants.CAGRID_MD_NAMESPACE );
        nsMap.put( com, MetadataConstants.CAGRID_COMMON_MD_NAMESPACE );
        nsMap.put( serv, MetadataConstants.CAGRID_SERVICE_MD_NAMESPACE );
        nsMap.put( data, MetadataConstants.CAGRID_DATA_MD_NAMESPACE );
        nsMap.put( uml, MetadataConstants.CADSR_UML_NAMESPACE );
    }

    protected static Log LOG = LogFactory.getLog( DiscoveryClient.class.getName() );

    /**
     * Uuses the Default Index Service
     * 
     * @throws MalformedURIException if the Default Index Service is invalid
     */
    public DiscoveryClient() throws MalformedURIException {
        this( DEFAULT_INDEX_SERVICE_URL );
    }

    /**
     * Uses the specified Index Service
     * 
     * @param indexURL the URL to the Index Service to use
     * @throws MalformedURIException if the specified Index Service URL is invalid
     */
    public DiscoveryClient( String indexURL ) throws MalformedURIException {
        this.indexEPR = new EndpointReferenceType();
        this.indexEPR.setAddress( new Address( indexURL ) );

    }

    /**
     * Uses the specified Index Service
     * 
     * @param indexEPR the EPR to the Index Service to use
     */
    public DiscoveryClient( EndpointReferenceType indexEPR ) {
        this.indexEPR = indexEPR;
    }

    /**
     * Query the registry for all registered services
     * 
     * @param requireMetadataCompliance if true, only services providing the standard metadata will be returned.
     *        Otherwise, all services registered will be returned, regardless of whether or not any metadata has been
     *        aggregated.
     * @return EndpointReferenceType[] contain all registered services
     */
    public EndpointReferenceType[] getAllServices( boolean requireMetadataCompliance ) throws Exception {
        if ( !requireMetadataCompliance ) {
            return discoverByFilter( "*" );
        } else {
            return discoverByFilter( MD_PATH );
        }

    }

    /**
     * Searches ALL metadata to find occurance of the given string. The search string is case-sensitive.
     * 
     * @param searchString the search string.
     * @return EndpointReferenceType[] matching the criteria
     */
    public EndpointReferenceType[] discoverServicesBySearchString( String searchString ) throws Exception {
        return discoverByFilter( CONTENT_PATH + "//*[contains(text(),'" + searchString + "') or @*[contains(string(),'"
                + searchString + "')]]" );
    }

    /**
     * Searches research center info to find services provided by a given cancer center.
     * 
     * @param centerName research center name
     * @return EndpointReferenceType[] matching the criteria
     */
    public EndpointReferenceType[] discoverServicesByResearchCenter( String centerName ) throws Exception {
        return discoverByFilter( MD_PATH + "/" + cagrid + ":hostingResearchCenter/" + com
                + ":ResearchCenter[@displayName='" + centerName + "' or @shortName='" + centerName + "']" );
    }

    /**
     * Searches to find services that have the given point of contact associated with them. Any fields set on the point
     * of contact are checked for a match. For example, you can set only the lastName, and only it will be checked, or
     * you can specify several feilds and they all must be equal.
     * 
     * @param contact point of contact
     * @return EndpointReferenceType[] matching the criteria
     */
    public EndpointReferenceType[] discoverServicesByPointOfContact( PointOfContact contact ) throws Exception {
        String pocPredicate = buildPOCPredicate( contact );

        return discoverByFilter( MD_PATH + "[" + cagrid + ":hostingResearchCenter/" + com + ":ResearchCenter/" + com
                + ":pointOfContactCollection/" + com + ":PointOfContact[" + pocPredicate + "] or " + cagrid
                + ":serviceDescription/" + serv + ":Service/" + serv + ":pointOfContactCollection/" + com
                + ":PointOfContact[" + pocPredicate + "]]" );
    }

    /**
     * Searches to find services that have a given name.
     * 
     * @param serviceName The service's name
     * @return EndpointReferenceType[] matching the criteria
     */
    public EndpointReferenceType[] discoverServicesByName( String serviceName ) throws Exception {
        return discoverByFilter( SERV_PATH + "[@name='" + serviceName + "']" );
    }

    /**
     * Searches to find services that have a given operation.
     * 
     * @param operationName The operation's name
     * @return EndpointReferenceType[] matching the criteria
     */
    public EndpointReferenceType[] discoverServicesByOperationName( String operationName ) throws Exception {
        return discoverByFilter( OPER_PATH + "[@name='" + operationName + "']" );
    }

    /**
     * Searches to find services that have an operation defined that takes the given UMLClass as input. Any fields set
     * on the UMLClass are checked for a match. For example, you can set only the packageName, and only it will be
     * checked, or you can specify several feilds and they all must be equal. NOTE: Only attributes of the UMLClass are
     * examined (associated objects (e.g. UMLAttributeCollection and SemanticMetadataCollection) are ignored).
     * 
     * @param clazzPrototype The protype UMLClass
     * @return EndpointReferenceType[] matching the criteria
     */
    public EndpointReferenceType[] discoverServicesByOperationInput( UMLClass clazzPrototype ) throws Exception {
        String umlClassPredicate = buildUMLClassPredicate( clazzPrototype );

        return discoverByFilter( OPER_PATH + "/" + serv + ":inputParameterCollection/" + serv + ":InputParam/" + com
                + ":UMLClass[" + umlClassPredicate + "]" );
    }

    /**
     * Searches to find services that have an operation defined that produces the given UMLClass. Any fields set on the
     * UMLClass are checked for a match. For example, you can set only the packageName, and only it will be checked, or
     * you can specify several feilds and they all must be equal. NOTE: Only attributes of the UMLClass are examined
     * (associated objects (e.g. UMLAttributeCollection and SemanticMetadataCollection) are ignored).
     * 
     * @param clazzPrototype The protype UMLClass
     * @return EndpointReferenceType[] matching the criteria
     */
    public EndpointReferenceType[] discoverServicesByOperationOutput( UMLClass clazzPrototype ) throws Exception {
        String umlClassPredicate = buildUMLClassPredicate( clazzPrototype );

        return discoverByFilter( OPER_PATH + "/" + serv + ":Output/" + com + ":UMLClass[" + umlClassPredicate + "]" );
    }

    /**
     * Searches to find services that have an operation defined that produces the given UMLClass or takes it as input.
     * Any fields set on the UMLClass are checked for a match. For example, you can set only the packageName, and only
     * it will be checked, or you can specify several feilds and they all must be equal. NOTE: Only attributes of the
     * UMLClass are examined (associated objects (e.g. UMLAttributeCollection and SemanticMetadataCollection) are
     * ignored).
     * 
     * @param clazzPrototype The protype UMLClass
     * @return EndpointReferenceType[] matching the criteria
     */
    public EndpointReferenceType[] discoverServicesByOperationClass( UMLClass clazzPrototype ) throws Exception {
        String umlClassPredicate = buildUMLClassPredicate( clazzPrototype );

        return discoverByFilter( OPER_PATH + "[" + serv + ":Output/" + com + ":UMLClass[" + umlClassPredicate + "] or "
                + serv + ":inputParameterCollection/" + serv + ":InputParam/" + com + ":UMLClass[" + umlClassPredicate
                + "]" + "]" );
    }

    /**
     * Searches to find services that have an operation defined that produces the or takes as input, a Class based on
     * the given concept code or one with attributes based on that concept code.
     * 
     * @param conceptCode The concept to look for
     * @return EndpointReferenceType[] matching the criteria
     */
    public EndpointReferenceType[] discoverServicesByConceptCode( String conceptCode ) throws Exception {
        String conceptPredicatedUMLClass = createConceptPredicatedUMLClass( conceptCode );

        return discoverByFilter( OPER_PATH + "[" + serv + ":Output/" + conceptPredicatedUMLClass + " or " + serv
                + ":inputParameterCollection/" + serv + ":InputParam/" + conceptPredicatedUMLClass + "]" );
    }

    /**
     * Creates a UMLClass step that is predicated to contain either an attribute based on the given concept, or the
     * class itself based on that concept.
     * 
     * @param conceptCode the code to look for
     * @return
     */
    private String createConceptPredicatedUMLClass( String conceptCode ) {
        return com + ":UMLClass[" + com + ":semanticMetadataCollection/" + uml + ":SemanticMetadata/@conceptCode='"
                + conceptCode + "' or " + com + ":umlAttributeCollection/" + com + ":UMLAttribute/" + com
                + ":semanticMetadataCollection/" + uml + ":SemanticMetadata/@conceptCode='" + conceptCode + "'" + "]";
    }

    /**
     * Query the registry for all registered data services
     * 
     * @return EndpointReferenceType[] contain all registered services
     */
    public EndpointReferenceType[] getAllDataServices() throws Exception {
        return discoverByFilter( DATA_MD_PATH );
    }

    /**
     * Searches to find data services that are exposing a subset of given domain (short name or long name).
     * 
     * @param modelName The model to look for
     * @return EndpointReferenceType[] matching the criteria
     */
    public EndpointReferenceType[] discoverDataServicesByDomainModel( String modelName ) throws Exception {
        return discoverByFilter( DATA_MD_PATH + "[@projectShortName='" + modelName + "' or @projectLongName='"
                + modelName + "']" );
    }

    public EndpointReferenceType[] discoverDataServicesByModelConceptCode( String conceptCode ) throws Exception {
        return discoverByFilter( DATA_MD_PATH + "/" + data + ":exposedUMLClassCollection/"
                + createConceptPredicatedUMLClass( conceptCode ) );
    }

    public EndpointReferenceType[] discoverDataServicesByExposedClass( UMLClass clazzPrototype ) throws Exception {
        String umlClassPredicate = buildUMLClassPredicate( clazzPrototype );

        return discoverByFilter( DATA_MD_PATH + "/" + data + ":exposedUMLClassCollection/" + com + ":UMLClass["
                + umlClassPredicate + "]" );
    }

    public EndpointReferenceType[] discoverDataServicesByAssociationsWithClass( UMLClass clazzPrototype )
            throws Exception {
        String referenceFiler = data + ":UMLAssociationEdge/" + data + ":UMLClassReference/@refid=" + data
                + ":exposedUMLClassCollection/" + com + ":UMLClass[" + buildUMLClassPredicate( clazzPrototype )
                + "]/@id";

        return discoverByFilter( DATA_MD_PATH + "[" + data + ":exposedUMLAssociationCollection/" + data
                + ":UMLAssociation/" + data + ":targetUMLAssociationEdge/" + referenceFiler + " or " + data
                + ":exposedUMLAssociationCollection/" + data + ":UMLAssociation/" + data + ":sourceUMLAssociationEdge/"
                + referenceFiler + "]" );
    }

    /**
     * Builds up a predicate for a PointOfContact, based on the prototype passed in.
     * 
     * @param contact the prototype POC
     * @return "*" if the prototype has no non-null or non-whitespace values, or the predicate necessary to match all
     *         values.
     */
    protected static String buildPOCPredicate( PointOfContact contact ) {
        String pocPredicate = "true()";

        if ( contact != null ) {
            pocPredicate += addNonNullPredicate( "affiliation", contact.getAffiliation(), true );
            pocPredicate += addNonNullPredicate( "email", contact.getEmail(), true );
            pocPredicate += addNonNullPredicate( "firstName", contact.getFirstName(), true );
            pocPredicate += addNonNullPredicate( "lastName", contact.getLastName(), true );
            pocPredicate += addNonNullPredicate( "phoneNumber", contact.getPhoneNumber(), true );
            pocPredicate += addNonNullPredicate( "role", contact.getRole(), true );
        }

        return pocPredicate;
    }

    /**
     * Builds up a predicate for a UMLClass, based on the prototype passed in. NOTE: Only attributes of the UMLClass are
     * examined (associated objects (e.g. UMLAttributeCollection and SemanticMetadataCollection) are ignored).
     * 
     * @param clazz the prototype UMLClass
     * @return "*" if the prototype has no non-null or non-whitespace values, or the predicate necessary to match all
     *         values.
     */
    protected static String buildUMLClassPredicate( UMLClass clazz ) {
        String umlPredicate = "true()";

        if ( clazz != null ) {
            umlPredicate += addNonNullPredicate( "projectName", clazz.getProjectName(), true );
            umlPredicate += addNonNullPredicate( "projectVersion", clazz.getProjectVersion(), true );
            umlPredicate += addNonNullPredicate( "className", clazz.getClassName(), true );
            umlPredicate += addNonNullPredicate( "packageName", clazz.getPackageName(), true );
            umlPredicate += addNonNullPredicate( "description", clazz.getDescription(), true );
        }

        return umlPredicate;
    }

    /**
     * @param name the element or attribute name to check
     * @param value the value to add the predicate filter against if this is null or whitespace only, no predicated is
     *        added.
     * @param isAttribute whether or not name represents an attribute or element
     * @return "" or the specified predicate (prefixed with " and " )
     */
    protected static String addNonNullPredicate( String name, String value, boolean isAttribute ) {
        if ( Utils.clean( value ) == null ) {
            return "";
        }
        if ( isAttribute ) {
            return " and @" + name + "='" + value + "'";
        } else {
            return " and " + name + "/text()='" + value + "'";
        }
    }

    /**
     * Applies the specified predicate to the common path in the Index Service's Resource Properties to return
     * registered services' EPRs that match the predicate.
     * 
     * @param xpathPredicate predicate to apply to the "Entry" in Index Service
     * @return EndpointReferenceType[] of matching services
     * @throws Exception
     */
    protected EndpointReferenceType[] discoverByFilter( String xpathPredicate ) throws Exception {
        EndpointReferenceType[] results = null;

        // query the service and deser the results
        MessageElement[] elements = ResourcePropertyHelper.queryResourceProperties( indexEPR,
                translateXPath( xpathPredicate ) );
        Object[] objects = ObjectDeserializer.toObject( elements, EndpointReferenceType.class );

        // if we got results, cast them into what we are expected to return
        if ( objects != null ) {
            results = new EndpointReferenceType[objects.length];
            System.arraycopy( objects, 0, results, 0, objects.length );
        }

        return results;

    }

    /**
     * Adds the common Index RP Entry filter, and translates the xpath to IndexService friendly XPath.
     * 
     * @param xpathPredicate
     * @return the modified xpath
     */
    protected String translateXPath( String xpathPredicate ) {
        String xpath = "/*/" + wssg + ":Entry[" + xpathPredicate + "]/" + wssg + ":MemberServiceEPR";
        LOG.debug( "Querying for: " + xpath );

        String translatedxpath = XPathUtils.translateXPath( xpath, nsMap );
        LOG.debug( "Issuing actual query: " + translatedxpath );

        return translatedxpath;
    }

    /**
     * Gets the EPR of the Index Service being used.
     */
    public EndpointReferenceType getIndexEPR() {
        return indexEPR;
    }

    /**
     * Sets the EPR of the Index Service to use.
     * 
     * @param indexEPR the EPR of the Index Service to use.
     */
    public void setIndexEPR( EndpointReferenceType indexEPR ) {
        this.indexEPR = indexEPR;
    }

    /**
     * testing stub
     * 
     * @param args optional URL to Index Service to query.
     */
    public static void main( String[] args ) {
        DiscoveryClient client = null;
        try {
            if ( args.length == 1 ) {
                client = new DiscoveryClient( args[0] );
            } else {
                client = new DiscoveryClient();
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit( -1 );
        }

        EndpointReferenceType[] allServices = null;
        try {
            allServices = client.getAllServices( true );
        } catch ( Exception e1 ) {
            e1.printStackTrace();
            System.exit( -1 );
        }

        if ( allServices != null ) {
            for ( int i = 0; i < allServices.length; i++ ) {
                EndpointReferenceType service = allServices[i];
                System.out.println( "\n\n" + service.getAddress() );
                try {
                    ServiceMetadata commonMetadata = MetadataUtils.getServiceMetadata( service );
                    if ( commonMetadata != null && commonMetadata.getHostingResearchCenter() != null
                            && commonMetadata.getHostingResearchCenter().getResearchCenter() != null ) {
                        System.out.println( "Service is from:"
                                + commonMetadata.getHostingResearchCenter().getResearchCenter().getDisplayName() );
                    }
                } catch ( Exception e ) {
                    // e.printStackTrace();
                    System.out.println( "ERROR: Unable to access service's standard resource properties: "
                            + e.getMessage() );
                }
            }
        } else {
            System.out.println( "No services found." );
        }
    }
}