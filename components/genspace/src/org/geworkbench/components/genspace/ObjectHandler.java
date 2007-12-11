package org.geworkbench.components.genspace;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A handler used to log events.
 * 
 * @author seth
 * @version $Id: ObjectHandler.java,v 1.2 2007-12-11 19:51:10 keshav Exp $
 */
public class ObjectHandler {

	private Log log = LogFactory.getLog(this.getClass());

	public ObjectHandler(Object event, Object source) {
		log.info("\n***GenSpace Logger****");
		log.info("\nEvent Name: " + event.getClass().getName());

		/*
		 * if
		 * (event.getClass().getName().equals("org.geworkbench.events.ProjectEvent")) {
		 * Method methods[] = event.getClass().getMethods(); for (Method m :
		 * methods) { } }
		 */
		Field fields[] = event.getClass().getDeclaredFields();
		for (Field f : fields) {
			log.info("Field: " + f.getName());
		}
		Method methods[] = event.getClass().getDeclaredMethods();
		for (Method m : methods) {
			log.info("Method: " + m.getName());
		}
		/*
		 * System.out.println("\nSource Name: " + source.getClass().getName());
		 * fields = source.getClass().getDeclaredFields(); for(Field f : fields) {
		 * System.out.println("Field: " + f.getName()); } methods =
		 * source.getClass().getDeclaredMethods(); for(Method m: methods) {
		 * System.out.println("Method: " + m.getName()); }
		 */
	}
}