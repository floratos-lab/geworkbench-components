package org.geworkbench.components.annotations;

import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.batik.bridge.DefaultExternalResourceSecurity;
import org.apache.batik.bridge.DefaultScriptSecurity;
import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.batik.util.gui.JErrorPane;
import org.w3c.dom.Element;

class SVGUserAgentImpl implements SVGUserAgent {

	final private JPanel parent;

	SVGUserAgentImpl(JPanel pathwayPanel) {
		parent = pathwayPanel;
    }

    public void displayError(String message) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
        JDialog dialog = pane.createDialog(parent, "ERROR");
        dialog.setModal(false);
        dialog.setVisible(true);
    }

    public void checkLoadExternalResource(ParsedURL resourceURL, ParsedURL docURL) {
    }

    public void checkLoadScript(String scriptType, ParsedURL scriptURL, ParsedURL docURL) {
    }

    public void displayError(Exception ex) {
        JErrorPane pane = new JErrorPane(ex, JOptionPane.ERROR_MESSAGE);
        JDialog dialog = pane.createDialog(parent, "ERROR");
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