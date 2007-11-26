/*
 * The geworkbench project
 * 
 * Copyright (c) 2007 Columbia University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.geworkbench.components.genspace;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.engine.management.Subscribe;

/**
 * 
 * @author keshav
 * @version $Id: GenspaceLogger.java,v 1.2 2007-11-26 18:40:13 keshav Exp $
 */
public class GenspaceLogger {

	private Log log = LogFactory.getLog(GenspaceLogger.class);

	private List<Class> hier = null;

	private boolean stop = false;

	/**
	 * Intercept all events.
	 * 
	 * @param event
	 * @param source
	 * @throws Exception
	 */
	@Subscribe
	public void getEvent(Object event, Object source) throws Exception {

		hier = new ArrayList<Class>();

		stop = false;

		if (event != null) {
			log.debug("event: " + event.getClass().getSimpleName());

			Class clazz = event.getClass();

			traverseEventHierarchy(clazz);

			Object logger = createLoggerForEvent(event, source);

			if (logger != null) {
				// logger.log(event, source);
			}

		} else {
			log.debug("null event");
		}

	}

	/**
	 * 
	 * @param clazz
	 * @return
	 */
	private void traverseEventHierarchy(Class clazz) {

		if (stop) {
			return;
		}

		hier.add(clazz);

		Type superClassType = clazz.getGenericSuperclass();

		if (superClassType == null) {
			stop = true;
			return;
		}

		else {
			Class cl = (Class) superClassType;
			traverseEventHierarchy(cl);
		}

		return;
	}

	/**
	 * 
	 * @param event
	 * @param source
	 * @throws Exception
	 */
	private Object createLoggerForEvent(Object event, Object source) {
		// FIXME create a Logger interface and return this.
		Object logger = null;

		for (Class clazz : hier) {
			StringBuffer buf = new StringBuffer();
			buf.append(clazz.getName());
			buf.append("Handler");

			Class clazzToInstantiate = null;
			try {
				clazzToInstantiate = Class.forName(buf.toString());

				Class[] parameterTypes = new Class[] { Object.class,
						Object.class };

				Constructor constructor = clazzToInstantiate
						.getDeclaredConstructor(parameterTypes);

				Object[] parameters = { event, source };

				logger = constructor.newInstance(parameters);

				break;

			} catch (Exception e) {
				// e.printStackTrace();
				log.warn("Cannot instantiate logger of type: " + buf.toString()
						+ ".  Will attempt to instantiate super class.");
				continue;
			}
		}
		return logger;
	}
}
