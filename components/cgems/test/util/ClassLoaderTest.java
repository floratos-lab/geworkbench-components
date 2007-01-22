package util;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author keshav
 * @version $Id: ClassLoaderTest.java,v 1.1 2007-01-22 20:42:29 keshav Exp $
 * 
 */
public class ClassLoaderTest extends TestCase {

	private static Log log = LogFactory.getLog(ClassLoaderTest.class);

	/**
	 * @param args
	 */
	public void testClassLoading() {
		log.info("BootstrapClassLoader");
		log.info("ExtClassLoader: " + System.getProperty("java.ext.dirs"));
		String[] javaClasspath = StringUtils.split(System
				.getProperty("java.class.path"), ";");
		for (String entry : javaClasspath) {
			log.info("AppClassLoader: " + entry);
		}

	}

}
