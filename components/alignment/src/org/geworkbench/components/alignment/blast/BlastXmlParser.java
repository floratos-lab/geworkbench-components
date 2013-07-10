/**
 * 
 */
package org.geworkbench.components.alignment.blast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.sequence.BlastObj;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 
 * The parser to retrieve the BLAST result in XML format.
 * 
 * @author zji
 * @version $Id$
 */
public class BlastXmlParser {
	private static Log log = LogFactory.getLog(BlastXmlParser.class);

	final private String RID;
	final private String maxTargetNumber;

	public BlastXmlParser(String RID, String maxTargetNumber) {
		this.RID = RID;
		this.maxTargetNumber = maxTargetNumber;
	}

	// this naive parser is used mainly because
	// (1) there is no formal schema available (2) we can keep it simple
	static private Vector<BlastObj> parse(InputStream stream) {
		Vector<BlastObj> vector = new Vector<BlastObj>();
		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			// this is necessary because we do have the DTD file specified in
			// the xml response
			docBuilderFactory
					.setFeature(
							"http://apache.org/xml/features/nonvalidating/load-external-dtd",
							false);
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(stream);

			// normalize text representation
			doc.getDocumentElement().normalize();
			log.debug("Root element of the doc is "
					+ doc.getDocumentElement().getNodeName());

			NodeList listOfHits = doc.getElementsByTagName("Hit");
			log.debug("number of hits :" + listOfHits.getLength());

			for (int s = 0; s < listOfHits.getLength(); s++) {

				Node hitNode = listOfHits.item(s);
				if (hitNode.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}

				Element hitElement = (Element) hitNode;

				NodeList idList = hitElement.getElementsByTagName("Hit_id");
				Element firstNameElement = (Element) idList.item(0);

				NodeList idText = firstNameElement.getChildNodes();
				String id = ((Node) idText.item(0)).getNodeValue().trim();
				String dbId = id.split("\\|")[2];
				log.debug("Hit_id: " + id);

				NodeList defList = hitElement.getElementsByTagName("Hit_def");
				Element defElement = (Element) defList.item(0);

				NodeList delText = defElement.getChildNodes();
				String description = ((Node) delText.item(0)).getNodeValue()
						.trim();

				NodeList accessionList = hitElement
						.getElementsByTagName("Hit_accession");
				Element accessionElement = (Element) accessionList.item(0);

				NodeList accessionText = accessionElement.getChildNodes();
				String name = ((Node) accessionText.item(0)).getNodeValue()
						.trim();

				// the following are not at the same level as the previous ones
				// it is like Hit -> Hit_hsps -> Hsp - >Hsp_evalue
				/* Each hit and Hits_hsps could have multiple hsp */
				NodeList hspList = hitElement.getElementsByTagName("Hsp");
				int numHsp = hspList.getLength();
				log.debug("number of alignment "+numHsp);
				String[] evalue = new String[numHsp];
				String[] length = new String[numHsp];
				String[] identity = new String[numHsp];
				
				NodeList evalueList = hitElement
						.getElementsByTagName("Hsp_evalue");
				Element evalueElement = (Element) evalueList.item(0);

				NodeList evalueText = evalueElement.getChildNodes();
				evalue[0] = ((Node) evalueText.item(0)).getNodeValue()
						.trim();

				NodeList fromList = hitElement
						.getElementsByTagName("Hsp_hit-from");
				Element fromElement = (Element) fromList.item(0);
				NodeList fromText = fromElement.getChildNodes();
				String from = ((Node) fromText.item(0)).getNodeValue().trim();
				int startPoint = Integer.parseInt(from);

				NodeList lengthList = hitElement
						.getElementsByTagName("Hsp_align-len");
				Element lengthElement = (Element) lengthList.item(0);
				NodeList lengthText = lengthElement.getChildNodes();
				length[0] = ((Node) lengthText.item(0)).getNodeValue()
						.trim();
				int alignmentLength = Integer.parseInt(length[0]);

				String[] subject = getStringArray(hitElement, numHsp, "Hsp_hseq");

				NodeList identityList = hitElement
						.getElementsByTagName("Hsp_identity");
				Element identityElement = (Element) identityList.item(0);
				NodeList identityText = identityElement.getChildNodes();
				identity[0] = ((Node) identityText.item(0)).getNodeValue()
						.trim();
				int identityCount = Integer.parseInt(identity[0]);
				int percentage = (int) Math.round(100. * identityCount
						/ alignmentLength);
				if (percentage == 100) { // don't round to 100% if it is not really
					percentage = 100 * identityCount / alignmentLength;
				}

				String alignedParts = formatAlignedParts(dbId, name, subject);
				BlastObj blastObj = new BlastObj(dbId, name, description,
						evalue[0], startPoint, alignmentLength, percentage,
						alignedParts);
				vector.add(blastObj);
			}// end of for loop of all 'Hit' element

		} catch (SAXParseException err) {
			log.error("** Parsing error" + ", line " + err.getLineNumber()
					+ ", uri " + err.getSystemId() + ":\n" + err.getMessage());

		} catch (SAXException e) {
			Exception x = e.getException();
			((x == null) ? e : x).printStackTrace();

		} catch (Throwable t) {
			t.printStackTrace();
		}
		return vector;
	}
	
	static private String[] getStringArray(Element hitElement, int numHsp,
			String tag) {
		NodeList nodeList = hitElement.getElementsByTagName(tag);
		/* this must hold: numHsp == nodeList.getLengTH() */
		String[] s = new String[numHsp];
		for (int i = 0; i < numHsp; i++) {
			Element element = (Element) nodeList.item(i);
			NodeList nodeText = element.getChildNodes();
			s[i] = ((Node) nodeText.item(0)).getNodeValue().trim();
		}
		return s;
	}
	
	static private String formatAlignedParts(String databaseID, String name,
			String[] subject) {
		StringBuilder sb = new StringBuilder();
		String a = "";
		for (int i = 0; i < subject.length; i++) {
			if (i > 0) {
				a = "(" + i + ")";
			}
			sb.append(">" + databaseID + "|" + name + a
					+ "---PARTIALLY INCLUDED\n"
					+ subject[i].replaceAll("-", "") + "\n");
		}

		return sb.toString();
	}

	// return null in any case when this parser fails
	public Vector<BlastObj> getResult() {
		String url = "http://blast.ncbi.nlm.nih.gov/Blast.cgi?CMD=Get&FORMAT_TYPE=XML&RID="
				+ RID + "&ALIGNMENTS=" + maxTargetNumber;
		HttpClient client = new HttpClient();
		DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(
				10, true);
		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				retryhandler);
		client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

		GetMethod getMethod = new GetMethod(url);
		try {
			int statusCode = client.executeMethod(getMethod);

			if (statusCode == HttpStatus.SC_OK) {
				InputStream stream = getMethod.getResponseBodyAsStream();
				return parse(stream);
			} else {
				log.error("HTTP status code is not OK (200): " + statusCode);
				return null;
			}
		} catch (HttpException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			getMethod.releaseConnection();
		}
	}
}
