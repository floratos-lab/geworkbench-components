/**
 * 
 */
package org.geworkbench.components.cytoscape;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import cytoscape.init.CyInitParams;

/**
 * This class may not be necessary, just to guarantee the cytoscape's behavior is exactly the same as before refactoring.
 * @author zji
 *
 */
public class InitParam implements CyInitParams {
	private String[] args;
	private Properties props;
	private String[] graphFiles;
	private String[] plugins;
	private Properties vizmapProps;
	private String sessionFile;
	private String[] nodeAttrFiles;
	private String[] edgeAttrFiles;
	private String[] expressionFiles;
	private int mode;
	
	InitParam() {
		mode = CyInitParams.TEXT;
		props = new Properties();

		plugins = new String[1];
		plugins[0] = "plugins_v2_4";
		vizmapProps = new Properties();

	}
	
	public Properties getVizProps() {
		return vizmapProps;
	}

	public Properties getProps() {
		return props;
	}

	public List<String> getGraphFiles() {
		return createList(graphFiles);
	}

	public List<String> getEdgeAttributeFiles() {
		return createList(edgeAttrFiles);
	}

	public List<String> getNodeAttributeFiles() {
		return createList(nodeAttrFiles);
	}

	public List<String> getExpressionFiles() {
		return createList(expressionFiles);
	}

	public List<String> getPlugins() {
		return createList(plugins);
	}

	public String getSessionFile() {
		return sessionFile;
	}

	public int getMode() {
		return mode;
	}

	public String[] getArgs() {
		return args;
	}

	private List<String> createList(String[] vals) {
		if (vals == null)
			return new ArrayList<String>();
		ArrayList<String> a = new ArrayList<String>(vals.length);
		for (int i = 0; i < vals.length; i++)
			a.add(i, vals[i]);

		return a;
	}
}
