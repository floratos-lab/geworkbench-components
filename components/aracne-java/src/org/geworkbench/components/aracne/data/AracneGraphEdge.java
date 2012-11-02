package org.geworkbench.components.aracne.data;

 

public class AracneGraphEdge {

	private String node1;
	private String node2;
	private float weight;

	public AracneGraphEdge() {
	};

	public AracneGraphEdge(String node1, String node2, float weight) {
		this.node1 = node1;
		this.node2 = node2;
		this.weight = weight;
	}

	public String getNode1() {
		return this.node1;
	}

	public void setNode1(String node1) {
		this.node1 = node1;
	}

	public String getNode2() {
		return this.node2;
	}

	public void setNode2(String node2) {
		this.node2 = node2;
	}

	public float getWeight() {
		return this.weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

}
