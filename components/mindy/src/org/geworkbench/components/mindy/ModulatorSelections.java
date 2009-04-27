package org.geworkbench.components.mindy;

import java.util.ArrayList;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

/**
 * @author oshteynb
 * @version $Id: ModulatorSelections.java,v 1.2 2009-04-27 15:49:02 keshav Exp $
 *
 */
public class ModulatorSelections {
	private MindyPlugin mindyPlugin;
	ModulatorModel modulatorModel;

	// from ModulatorModel
	private ArrayList<DSGeneMarker> selectedModulators;

	public ModulatorSelections(MindyPlugin mindyPlugin,
			ModulatorModel modulatorModel) {
		this.mindyPlugin = mindyPlugin;
		this.modulatorModel = modulatorModel;

		this.selectedModulators = new ArrayList<DSGeneMarker>();
	}

	public ArrayList<DSGeneMarker> getSelectedModulators() {
		return selectedModulators;
	}

	// TODO update in corresponding models
	public void setSelectedModulators(ArrayList<DSGeneMarker> selectedModulators) {
		if (selectedModulators == null) {

			/* clear modulators in tabs */
			this.selectedModulators.clear();

			this.mindyPlugin.aggregateModel.disableAllModulators();

			this.mindyPlugin.modTargetModel.disableAllModulators();
		} else {
			this.selectedModulators = selectedModulators;
		}
	}

	// TODO update in corresponding models
	public void addSelectedModulator(DSGeneMarker mod) {
		this.selectedModulators.add(mod);
	}

}
