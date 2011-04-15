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
import org.geworkbench.events.AnalysisInvokedEvent;
import org.geworkbench.events.EventHandler;

/**
 * Captures all events with an associated {@link EventHandler} defined.
 * 
 * @author keshav
 * @version $Id: GenspaceLogger.java,v 1.7 2009-01-21 01:15:04 sheths Exp $
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
		if (event != null && event.getClass().equals(AnalysisInvokedEvent.class)) {
			log.info("event: " + event.getClass().getSimpleName());

			ObjectHandler logger = new ObjectHandler(event,source);
		}
	}
}
