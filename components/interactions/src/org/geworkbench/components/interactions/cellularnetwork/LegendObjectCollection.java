package org.geworkbench.components.interactions.cellularnetwork;

import java.util.List;
import java.util.ArrayList;

/**
 * A collection of legend items.
 */
public class LegendObjectCollection {

	/** Storage for the legend items. */
	private List<LegendObject> items;

	/**
	 * Constructs a new legend item collection, initially empty.
	 */
	public LegendObjectCollection() {
		this.items = new ArrayList<LegendObject>();
	}

	/**
	 * Adds a legend item to the collection.
	 * 
	 * @param item
	 *            the item to add.
	 */
	public void add(LegendObject item) {
		this.items.add(item);
	}

	/**
	 * Adds the legend items from another collection to this collection.
	 * 
	 * @param collection
	 *            the other collection.
	 */
	public void addAll(LegendObjectCollection collection) {
		this.items.addAll(collection.items);
	}

	/**
	 * Returns a legend item from the collection.
	 * 
	 * @param index
	 *            the legend item index (zero-based).
	 * 
	 * @return The legend item.
	 */
	public LegendObject get(int index) {
		return (LegendObject) this.items.get(index);
	}

	public LegendObject get(String label) {
		for (LegendObject item : items) {
			if (item.getLabel().equals(label))
				return item;
		}
		return null;
	}

	/**
	 * Returns the number of legend items in the collection.
	 * 
	 * @return The item count.
	 */
	public int getItemCount() {
		return this.items.size();
	}

	public void clear() {
		this.items.clear();
	}

	public void remove(int index) {
		this.items.remove(index);
	}

	public boolean contains(LegendObject item) {
		return this.items.contains(item);
	}

	public List<String> getSelectedInteractionTypeList() {
		List<String> interactionTypeList = new ArrayList<String>();
		for (int i = 1; i < getItemCount(); i++) {
			if (items.get(i).isChecked())
				interactionTypeList.add(items.get(i).getLabel());
		}
		return interactionTypeList;
	}

}
