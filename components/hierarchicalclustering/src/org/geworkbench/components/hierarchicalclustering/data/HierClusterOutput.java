package org.geworkbench.components.hierarchicalclustering.data;

 
import org.geworkbench.components.hierarchicalclustering.computation.HNode; 
import org.geworkbench.components.hierarchicalclustering.computation.ObjectConversion;

/* This is the wrapping class required by the POJO-based AXIS2 web service,
 * so it must be in the Java bean format. */
public class HierClusterOutput { 
	 
	/* We cannot use HNode as the field type because it does not have Java Bean format (no necessary setters) */
	/* Only bean types or standard primitive types can be used in the class to build POJO-based web services on. */
	private  byte[] hnode;
 
	public HierClusterOutput(){};
	
	public HierClusterOutput(HNode hnode )
	{
		 this.hnode = ObjectConversion.convertToByte(hnode);
	}
 
	public void setHnode(byte[] hnode)
	{
		this.hnode = hnode;
	}
	public byte[] getHnode()
	{
		return this.hnode;
	}
	
	public HNode getHnodeObject()
	{
		return (HNode)ObjectConversion.toObject(hnode);
	}
	 
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("HierCluster Result\n");		 
		HNode node = (HNode)ObjectConversion.toObject(hnode);		 
		sb.append("hnode:" + node.getId() + ", height is " + node.getHeight()).append(" \n");		 
		while(node.getRight() != null)
		{
		    sb.append("hnode:" + node.getRight().getId() + ", height is " + node.getRight().getHeight()).append(" \n");
		    node = node.getRight();
		}		 
		return sb.toString();
	}
}
