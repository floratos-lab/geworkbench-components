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


//import jalview.datamodel.Alignment;
//import jalview.datamodel.Sequence;
//import jalview.datamodel.SequenceI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
//import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
//import org.geworkbench.components.genspace.server.stubs.ProteinSequence;
//import org.geworkbench.components.genspace.ui.SequenceAlignmentPanel;
//import org.geworkbench.components.genspace.ui.SequenceAlignmentPanel.MSARecommenderCallback;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.AnalysisInvokedEvent;
import org.geworkbench.events.EventHandler;
import org.geworkbench.events.ProjectEvent;

/**
 * Captures all events with an associated {@link EventHandler} defined.
 * 
 * @author keshav
 * @version $Id: GenspaceLogger.java,v 1.1 2011/02/07 18:09:54 jsb2125 Exp $
 */
public class GenspaceLogger {

	private Log log = LogFactory.getLog(GenspaceLogger.class);

	/**
	 * Intercept all events.
	 * 
	 * @param event
	 * @param source
	 * @throws Exception
	 */
	@Subscribe
	public void getEvent(Object event, Object source) throws Exception {
		if (event != null
				&& event.getClass().equals(AnalysisInvokedEvent.class)) {
			log.info("event: " + event.getClass().getSimpleName());

			@SuppressWarnings("unused")
			ObjectHandler logger = new ObjectHandler(event, source);
		}

		if (event == null || !event.getClass().equals(ProjectEvent.class)) {
			return;
		}
//
//		ProjectEvent projectEvent = (ProjectEvent) event;
//		if (!(projectEvent.getDataSet() instanceof CSSequenceSet<?>)) {
//			return;
//		}
//		@SuppressWarnings("unchecked")
//		final CSSequenceSet<CSSequence> sequenceSet = (CSSequenceSet<CSSequence>) projectEvent
//				.getDataSet();
//
//		if (sequenceSet.isDNA()) {
//			return;
//		}
//
//		Alignment alignment = new Alignment(new SequenceI[] {});
//		for (CSSequence sequence : sequenceSet) {
//			Sequence jalSeq = new Sequence(sequence.getLabel(),
//					sequence.getSequence());
//			alignment.addSequence(jalSeq);
//		}
//		SequenceAlignmentPanel.getInstance().setAlignment(alignment);
//		SequenceAlignmentPanel.getInstance().setMsaRecommenderCallback(
//				new MSARecommenderCallback() {
//					@Override
//					public void sequenceAdded(ProteinSequence proteinSequence) {
//						sequenceSet.add(new CSSequence(proteinSequence
//								.getAccessionNo(), proteinSequence
//								.getSequence()));
//					}
//				});
	}
}
