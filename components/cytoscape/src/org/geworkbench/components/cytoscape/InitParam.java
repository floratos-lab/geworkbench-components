/**
 * 
 */
package org.geworkbench.components.cytoscape;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.geworkbench.engine.config.UILauncher;

import cytoscape.init.CyInitParams;

/**
 * This class may not be necessary, just to guarantee the cytoscape's behavior is exactly the same as before refactoring.
 * @author zji
 *
 */
public class InitParam implements CyInitParams {
	private String[] args;
	private Properties props;
	private Properties vizmapProps;
	private String sessionFile;
	private int mode;
	
	InitParam() {
		mode = CyInitParams.TEXT;
		props = new Properties();

		vizmapProps = new Properties();

	}
	
	public Properties getVizProps() {
		return vizmapProps;
	}

	public Properties getProps() {
		return props;
	}

	public List<String> getGraphFiles() {
		return new ArrayList<String>();
	}

	public List<String> getEdgeAttributeFiles() {
		return new ArrayList<String>();
	}

	public List<String> getNodeAttributeFiles() {
		return new ArrayList<String>();
	}

	public List<String> getExpressionFiles() {
		return new ArrayList<String>();
	}

	/**
	 * Get the core plugins of cytoscape.
	 */
	// cytoscape supports specifying the plugin by teh directory name, 
	// but geworkbench component classloader ony include jar files directly under lib directory in classpath
	// so plugin jar files are exmaplcitly individually here because they can stays the lib directory
	// with non-plugin jars
	public List<String> getPlugins() {
		List<String> plugins = new ArrayList<String>();
		final String pluginDirectory = UILauncher.getComponentsDirectory()+"/cytoscape/lib/";
		plugins.add(pluginDirectory+"AdvancedNetworkMerge.jar");
		plugins.add(pluginDirectory+"AutomaticLayout.jar");
		plugins.add(pluginDirectory+"biomartClient.jar");
		//plugins.add(pluginDirectory+"biopax.jar"); // LinkageError due to its own collection15.MutilMap
		plugins.add(pluginDirectory+"browser.jar");
		plugins.add(pluginDirectory+"cPath.jar");
		plugins.add(pluginDirectory+"cpath2.jar");
		//plugins.add(pluginDirectory+"CytoscapeEditor.jar"); // prevent blocking the network view 
		plugins.add(pluginDirectory+"filter.jar");
		plugins.add(pluginDirectory+"filters.jar");
		plugins.add(pluginDirectory+"linkout.jar");
		plugins.add(pluginDirectory+"ManualLayout.jar");
		plugins.add(pluginDirectory+"ncbiClient.jar");
		plugins.add(pluginDirectory+"psi_mi.jar");
		plugins.add(pluginDirectory+"quick_find.jar");
		plugins.add(pluginDirectory+"SBMLReader.jar");
		plugins.add(pluginDirectory+"TableImport.jar");
		plugins.add(pluginDirectory+"yLayouts.jar");
		return plugins;
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

}
