package org.geworkbench.components.interactions.cellularnetwork;

import java.awt.Color; 

public class LegendObject {

	private String label;
	private Color color;

	private boolean isChecked = true;

	public LegendObject(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;

	}

	public Color getColor() {
		return this.color;

	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setSelected(boolean flag) {
		this.isChecked = flag;
	}

	public boolean isChecked() {
		return this.isChecked;
	}

	public boolean equals(Object obj) {

		if (!(obj instanceof LegendObject)) {
			return false;
		}
		LegendObject that = (LegendObject) obj;

		if (!this.label.equals(that.label)) {
			return false;
		}

		return true;
	}

}
