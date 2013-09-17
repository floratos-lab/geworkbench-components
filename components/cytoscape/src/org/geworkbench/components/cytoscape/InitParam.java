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
		plugins.add(pluginDirectory+"advanced-network-merge-2.8.2-jar-with-dependencies.jar");
		plugins.add(pluginDirectory+"attribute-browser-2.8.2-jar-with-dependencies.jar");
		plugins.add(pluginDirectory+"automatic-layout-2.8.2-jar-with-dependencies.jar");
		plugins.add(pluginDirectory+"biomart-client-2.8.2-jar-with-dependencies.jar"); // LinkageError due to its own collection15.MutilMap
		plugins.add(pluginDirectory+"biopax-2.8.2-jar-with-dependencies.jar");
		plugins.add(pluginDirectory+"core-commands-2.8.2-jar-with-dependencies.jar");
		plugins.add(pluginDirectory+"cpath-2.8.2-jar-with-dependencies.jar");
		plugins.add(pluginDirectory+"cpath2-2.8.2-jar-with-dependencies.jar"); // prevent blocking the network view 
		plugins.add(pluginDirectory+"editor-2.8.2-jar-with-dependencies.jar");
		plugins.add(pluginDirectory+"equation-functions-2.8.2-jar-with-dependencies.jar");
		plugins.add(pluginDirectory+"filters-2.8.2-jar-with-dependencies.jar");
		plugins.add(pluginDirectory+"linkout-2.8.2-jar-with-dependencies.jar");
		plugins.add(pluginDirectory+"manual-layout-2.8.2-jar-with-dependencies.jar");
		plugins.add(pluginDirectory+"network-analyzer-2.8.2-jar-with-dependencies.jar");
		plugins.add(pluginDirectory+"psi-mi-2.8.2-jar-with-dependencies.jar");
		plugins.add(pluginDirectory+"quickfind-2.8.2-jar-with-dependencies.jar");
		plugins.add(pluginDirectory+"rfilters-2.8.2-jar-with-dependencies.jar");
		plugins.add(pluginDirectory+"sbml-reader-2.8.2-jar-with-dependencies.jar");		
		plugins.add(pluginDirectory+"table-import-2.8.2-jar-with-dependencies.jar");	
		plugins.add(pluginDirectory+"y-layouts-1.0.1.jar");
		//plugins.add(pluginDirectory+"ncbi-client-2.8.2-jar-with-dependencies.jar");
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
