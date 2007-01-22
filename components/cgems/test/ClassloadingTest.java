import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author keshav
 * 
 */
public class ClassloadingTest extends TestCase {

	private static Log log = LogFactory.getLog(ClassloadingTest.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log.info("BootstrapClassLoader");
		log.info("ExtClassLoader: " + System.getProperty("java.ext.dirs"));
		String[] javaClasspath = StringUtils.split(System
				.getProperty("java.class.path"), ";");
		for (String entry : javaClasspath) {
			log.info("AppClassLoader: " + entry);
		}

	}

}
