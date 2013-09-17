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

package org.geworkbench.events;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A handler to handle the {@link ProjectNodeAddedEvent}.
 * 
 * @author keshav
 * @version $Id: ProjectNodeAddedEventHandler.java,v 1.1 2007/11/26 18:40:13
 *          keshav Exp $
 */
public class ProjectNodeAddedEventHandler implements EventHandler {

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * 
	 * @param event
	 * @param source
	 */
	public ProjectNodeAddedEventHandler(Object event, Object source) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.events.EventHandler#log()
	 */
	public void log() {
		log.warn("Logging information for " + this.getClass().getName());

	}

}
