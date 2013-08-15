package org.geworkbench.components.annotations;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class BioDBnetClient {

	private static Log log = LogFactory.getLog(BioDBnetClient.class);

	final private String pathwayQueryUrlFormat = "http://biodbnet.abcc.ncifcrf.gov/webServices/rest.php/biodbnetRestApi.xml?taxonId=%d&input=Gene%%20Symbol&inputValues=%s&outputs=GeneID,GeneInfo,BiocartaPathwayName,UniGeneID";
	final private String geneQueryUrlFormat = "http://biodbnet.abcc.ncifcrf.gov/webServices/rest.php/biodbnetRestApi.xml?input=BiocartaPathwayName&inputValues=%s&outputs=GeneInfo";
	
	final private DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
			.newInstance();

	public GeneAnnotation[] queryByGeneSymbol(int taxonId, String geneSymbol) {

		HttpClient client = new HttpClient();
		DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(
				10, true);
		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				retryhandler);
		client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

		/* let's keep the original design's flexibility of returning an array. For now, we only return one element, though. */
		GeneAnnotation[] r = null;

		String url = String.format(pathwayQueryUrlFormat, taxonId, geneSymbol);
		GetMethod getMethod = new GetMethod(url);
		try {
			int statusCode = client.executeMethod(getMethod);

			if (statusCode == HttpStatus.SC_OK) {
				InputStream stream = getMethod.getResponseBodyAsStream();
				DocumentBuilder docBuilder = docBuilderFactory
						.newDocumentBuilder();
				Document doc = docBuilder.parse(stream);

				// normalize text representation
				doc.getDocumentElement().normalize();
				log.debug("Root element of the doc is "
						+ doc.getDocumentElement().getNodeName());

				// assume it is always only one
				NodeList ids = doc.getElementsByTagName("GeneID");
				/* I expect the count to be 1. if more, take the first; if zero, meaning no (valid) result. */
				int count = ids.getLength(); 
				if(count<1) {
					log.debug("no result returned");
					return null;
				}
				Element idElement = (Element) ids.item(0);
				NodeList idTextList = idElement.getChildNodes();
				/* gene ID = entrez ID = locus link */
				String geneId = ((Node) idTextList.item(0)).getNodeValue().trim();

				// assume it is always only one
				NodeList infos = doc.getElementsByTagName("GeneInfo");
				Element infoElement = (Element) infos.item(0);
				NodeList infoTextList = infoElement.getChildNodes();
				String info = ((Node) infoTextList.item(0)).getNodeValue()
						.trim();
				String geneDescription = parseGeneInfo("Description", info);

				// assume it is always only one
				NodeList unis = doc.getElementsByTagName("UniGeneID");
				Element uniElement = (Element) unis.item(0);
				NodeList unisTextList = uniElement.getChildNodes();
				String uni = ((Node) unisTextList.item(0)).getNodeValue()
						.trim();
				int dotIndex = uni.indexOf('.');
				/* text (organism) part of UniGene ID, e.g. Hs for Human, Mm for Mouse */
				String organism = uni.substring(0, dotIndex);
				/* numeric part of UniGene ID */
				Long clusterId = Long.parseLong(uni.substring(dotIndex + 1));

				NodeList pathwayList = doc
						.getElementsByTagName("BiocartaPathwayName");
				String[] pathwayNames = new String[pathwayList.getLength()];
				for (int i = 0; i < pathwayNames.length; i++) {
					Element pathwayElement = (Element) pathwayList.item(i);
					NodeList textList = pathwayElement.getChildNodes();
					String p = ((Node) textList.item(0)).getNodeValue().trim();
					pathwayNames[i] = p.substring(0, p.indexOf(' '));
				}

				r = new GeneAnnotation[1];
				r[0] = new GeneAnnotationImpl(geneSymbol, geneDescription,
						geneId, clusterId, organism, pathwayNames);
				return r;
			} else {
				log.error("HTTP status code is not OK (200): " + statusCode);
			}
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} finally {
			getMethod.releaseConnection();
		}
		return r;
	}

	public GeneBase[] queryGenesForPathway(String pathwayName) {
		HttpClient client = new HttpClient();
		DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(
				10, true);
		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				retryhandler);
		client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

		GeneBase[] genes = null;
		
		String url = String.format(geneQueryUrlFormat, pathwayName);
		GetMethod getMethod = new GetMethod(url);
		try {
			int statusCode = client.executeMethod(getMethod);

			if (statusCode == HttpStatus.SC_OK) {
				InputStream stream = getMethod.getResponseBodyAsStream();
				DocumentBuilder docBuilder = docBuilderFactory
						.newDocumentBuilder();
				Document doc = docBuilder.parse(stream);

				// normalize text representation
				doc.getDocumentElement().normalize();
				log.debug("Root element of the doc is "
						+ doc.getDocumentElement().getNodeName());

				NodeList infos = doc.getElementsByTagName("GeneInfo");
				int count = infos.getLength();
				genes = new GeneBase[count];
				for(int i=0; i<count; i++) {
					Element infoElement = (Element) infos.item(i);
					NodeList infoTextList = infoElement.getChildNodes();
					String info = ((Node) infoTextList.item(0)).getNodeValue()
							.trim();
					String geneSymbol = parseGeneInfo("Gene Symbol", info);
					String geneDescription = parseGeneInfo("Description", info);
					genes[i] = new GeneImpl(geneSymbol, geneDescription);
				}
			} else {
				log.error("HTTP status code is not OK (200): " + statusCode);
			}
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} finally {
			getMethod.releaseConnection();
		}
		return genes;
	}
	
	private static String parseGeneInfo(String fieldName, String info) {
		Pattern pattern = Pattern.compile("\\["+fieldName+":\\s+([^\\]]+)\\]");
		Matcher matcher = pattern.matcher(info);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return "";
		}
	}
}
