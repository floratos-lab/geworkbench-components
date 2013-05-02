package org.geworkbench.components.hierarchicalclustering.computation;

/**
 * @author zji
 * @version $Id$
 * 
 */
class SortStruct implements Comparable<SortStruct> {
	public double distance;
	private HNode a, b;

	/**
	 * 
	 * @param distance
	 * @param a
	 * @param b
	 */
	public SortStruct(double distance, HNode a, HNode b) {
		this.distance = distance;
		if (a.getId() < b.getId()) {
			this.a = a;
			this.b = b;
		} else {
			this.a = b;
			this.b = a;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final SortStruct that = (SortStruct) o;

		if (!a.equals(that.a))
			return false;
		if (!b.equals(that.b))
			return false;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int result;
		result = a.hashCode();
		result = 29 * result + b.hashCode();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(SortStruct s) {

		if (this.equals(s)) {
			return 0;
		} else {
			int compare = Double.compare(distance, s.distance);
			if (compare == 0) {
				compare = Double.compare(a.getId(), s.a.getId());
				if (compare == 0) {
					compare = Double.compare(b.getId(), s.b.getId());
				}
			}
			return compare;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + a.getId() + ", " + b.getId() + "): " + distance;
	}

	public HNode getLeftNode() {
		return a;
	}

	public HNode getRightNode() {
		return b;
	}
}
