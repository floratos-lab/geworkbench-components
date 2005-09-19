package org.geworkbench.components.annotations;

import org.geworkbench.util.ProgressBar;
import org.geworkbench.events.AnnotationsEvent;
import org.apache.batik.bridge.DefaultExternalResourceSecurity;
import org.apache.batik.bridge.DefaultScriptSecurity;
import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.LinkActivationEvent;
import org.apache.batik.swing.svg.LinkActivationListener;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.batik.util.gui.JErrorPane;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.util.BrowserLauncher;
import org.geworkbench.util.annotation.*;
import org.geworkbench.engine.config.VisualPlugin;
import org.w3c.dom.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * Component to visualize Scalable Vector Graphic Images representing
 * Biochemical Pathways as obtained from the caBIO PathwayManager
 *
 * @author First Genetic Trust
 * @version 1.0
 * @(#)PathwayPanel.java	1.0 06/02/03
 */
public class PathwayPanel implements VisualPlugin {
    /**
     * Visual Widget
     */
    private JPanel pathwayPanel = new JPanel();
    /**
     * Visual Widget
     */
    private JScrollPane scrollPane1 = new JScrollPane();
    private JTextField pathwayName = new JTextField();
    /**
     * Visual Widget
     */
    private BorderLayout borderLayout1 = new BorderLayout();
    /**
     * <code>Canvas</code> on which the Pathway SVG image is drawn
     */
    private JSVGCanvas svgCanvas = new JSVGCanvas(new UserAgent(), true, true);

    /**
     * Default Constructor
     */
    public PathwayPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Configures the Graphical User Interface and Listeners
     *
     * @throws Exception
     */
    private void jbInit() throws Exception {
        pathwayPanel.setLayout(borderLayout1);
        pathwayPanel.add(pathwayName, BorderLayout.NORTH);
        pathwayPanel.add(scrollPane1, BorderLayout.CENTER);
        svgCanvas.addLinkActivationListener(new LinkActivationListener() {
            public void linkActivated(LinkActivationEvent lae) {
                svgCanvas_linkActivated(lae);
            }

        });
        scrollPane1.getViewport().add(svgCanvas, null);
    }

    /**
     * Interface <code>VisualPlugin</code> method that returns a
     * <code>Component</code> which is the visual representation of
     * the this plugin.
     *
     * @return <code>Component</code> visual representation of
     *         <code>PathwayPanel</code>
     */
    public Component getComponent() {
        return pathwayPanel;
    }

    org.geworkbench.util.annotation.Pathway pathway = null;

    /**
     * Interface <code>AnnotationsListener</code> method that received a
     * selected <code>Pathway</code> to be shown in the <code>PathwayPanel</code>
     * plugin.
     *
     * @param ae <code>AnnotationsEvent</code> that contains the
     *           <code>Pathway</code> to be shown
     */
    @Subscribe public void receive(org.geworkbench.events.AnnotationsEvent ae, Object source) {
        Container parent = pathwayPanel.getParent();
        if (parent instanceof JTabbedPane)
            ((JTabbedPane) parent).setSelectedComponent(pathwayPanel);
        pathway = ae.getPathway();
        Runnable pway = new Runnable() {
            public void run() {
                org.geworkbench.util.ProgressBar pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
                pb.setTitle("Constructing SVG Pathway");
                pb.setMessage("Creating Image..");
                pb.start();
                setSvg(pathway.getPathwayDiagram().getSvgString());
                pathwayName.setText(pathway.getPathwayName());
                pathwayName.invalidate();
                pb.stop();
                pb.dispose();
            }
        };
        Thread t = new Thread(pway);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    /**
     * Wrapper method for setting the <code>SVGDocument</code> received
     * from the <code>Pathway</code> objects obtained from a caBIO search
     *
     * @param svgString SVG document returned from a caBIO search as a String
     */
    private void setSvg(String svgString) {
        if (svgString != null) {
            StringReader reader = new StringReader(svgString);
            Document document = null;
            try {
                String parser = XMLResourceDescriptor.getXMLParserClassName();
                SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
                document = f.createDocument(null, reader);
            } catch (IOException ex) {
            }

            svgCanvas.setDocument(document);
            svgCanvas.revalidate();
            pathwayPanel.revalidate();
        } else {
            JOptionPane.showMessageDialog(pathwayPanel, "No Pathway diagram obtained from caBIO", "Diagram missing", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    private void walk(Node node) {
        int type = node.getNodeType();
        switch (type) {
            case Node.DOCUMENT_NODE:
                {
                    System.out.println("<?xml version=\"1.0\" encoding=\"" + "UTF-8" + "\"?>");
                    break;
                }

            case Node.ELEMENT_NODE:
                {
                    System.out.print('<' + node.getNodeName());
                    NamedNodeMap nnm = node.getAttributes();
                    if (nnm != null) {
                        int len = nnm.getLength();
                        Attr attr;
                        for (int i = 0; i < len; i++) {
                            attr = (Attr) nnm.item(i);
                            System.out.print(" " + attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"");
                        }

                    }

                    System.out.print('>');
                    break;
                }

            case Node.ENTITY_REFERENCE_NODE:
                {
                    System.out.print('&' + node.getNodeName() + ';');
                    break;
                }

            case Node.CDATA_SECTION_NODE:
                {
                    System.out.print("<![CDATA[" + node.getNodeValue() + "]]>");
                    break;
                }

            case Node.TEXT_NODE:
                {
                    System.out.print(node.getNodeValue());
                    break;
                }

            case Node.PROCESSING_INSTRUCTION_NODE:
                {
                    System.out.print("<?" + node.getNodeName());
                    String data = node.getNodeValue();
                    if (data != null && data.length() > 0) {
                        System.out.print(' ');
                        System.out.print(data);
                    }

                    System.out.println("?>");
                    break;
                }

        }

        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            walk(child);
        }

        if (type == Node.ELEMENT_NODE)
            System.out.print("</" + node.getNodeName() + ">");
    }

    private void svgCanvas_linkActivated(LinkActivationEvent lae) {
        String uri = lae.getReferencedURI();
        int index = uri.indexOf("BCID");
        String bcid = uri.substring(index + 5, uri.length());
        GeneSearchCriteria criteria = new GeneSearchCriteriaImpl();
        criteria.setSearchByBCID(bcid);
        criteria.search();
        GeneAnnotation[] matchingGenes = criteria.getGeneAnnotations();
        assert matchingGenes.length == 1 : "Search on BCID should return just 1 Gene";
        String url = matchingGenes[0].getGeneURL().toString();
        try {
            org.geworkbench.util.BrowserLauncher.openURL(url.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    protected class UserAgent implements SVGUserAgent {
        protected UserAgent() {
        }

        public void displayError(String message) {
            JOptionPane pane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
            JDialog dialog = pane.createDialog(pathwayPanel, "ERROR");
            dialog.setModal(false);
            dialog.setVisible(true);
        }

        public void checkLoadExternalResource(ParsedURL resourceURL, ParsedURL docURL) {
        }

        public void checkLoadScript(String scriptType, ParsedURL scriptURL, ParsedURL docURL) {
        }

        public void displayError(Exception ex) {
            JErrorPane pane = new JErrorPane(ex, JOptionPane.ERROR_MESSAGE);
            JDialog dialog = pane.createDialog(pathwayPanel, "ERROR");
            dialog.setModal(false);
            dialog.setVisible(true);
        }

        public void displayMessage(String message) {
        }

        public String getAlternateStyleSheet() {
            return "alternate";
        }

        public float getBolderFontWeight(float f) {
            return 10f;
        }

        public String getDefaultFontFamily() {
            return "Arial";
        }

        public ExternalResourceSecurity getExternalResourceSecurity(ParsedURL resourceURL, ParsedURL docURL) {
            return new DefaultExternalResourceSecurity(resourceURL, docURL);
        }

        public float getLighterFontWeight(float f) {
            return 8f;
        }

        public float getMediumFontSize() {
            return 9f;
        }

        public float getPixelToMM() {
            return 0.264583333333333333333f; // 96 dpi
        }

        public float getPixelUnitToMillimeter() {
            return 0.264583333333333333333f; // 96 dpi
        }

        public ScriptSecurity getScriptSecurity(String scriptType, ParsedURL scriptURL, ParsedURL docURL) {
            return new DefaultScriptSecurity(scriptType, scriptURL, docURL);
        }

        public String getLanguages() {
            return Locale.getDefault().getLanguage();
        }

        public String getUserStyleSheetURI() {
            return null;
        }

        public String getXMLParserClassName() {
            return XMLResourceDescriptor.getXMLParserClassName();
        }

        public boolean isXMLParserValidating() {
            return true;
        }

        public String getMedia() {
            return "screen";
        }

        public void openLink(String uri, boolean newc) {
        }

        public void showAlert(String message) {
        }

        public boolean showConfirm(String message) {
            return true;
        }

        public boolean supportExtension(String s) {
            return false;
        }

        public String showPrompt(java.lang.String message) {
            return "";
        }

        public String showPrompt(String message, String defaultValue) {
            return "";
        }

        public void handleElement(Element elt, Object data) {
        }

    }

}

