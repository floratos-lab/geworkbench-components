package org.geworkbench.components.genspace;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ObjectHandler {
	
	public ObjectHandler(Object event, Object source) {
		System.out.println("\n***GenSpace Logger****");
		System.out.println("\nEvent Name: " + event.getClass().getName());
		
		/*if (event.getClass().getName().equals("org.geworkbench.events.ProjectEvent")) {
			Method methods[] = event.getClass().getMethods();
			for (Method m : methods) {
				
			}
		}*/
		Field fields[] = event.getClass().getDeclaredFields();
		for(Field f : fields) {
			System.out.println("Field: " + f.getName());
		}
		Method methods[] = event.getClass().getDeclaredMethods();
		for(Method m: methods) {
			System.out.println("Method: " + m.getName());
		}
		/*System.out.println("\nSource Name: " + source.getClass().getName());
		fields = source.getClass().getDeclaredFields();
		for(Field f : fields) {
			System.out.println("Field: " + f.getName());
		}
		methods = source.getClass().getDeclaredMethods();
		for(Method m: methods) {
			System.out.println("Method: " + m.getName());
		}*/
	}
}