package org.geworkbench.components.hierarchicalclustering.data;

 
import java.io.Serializable;

 
import org.geworkbench.components.hierarchicalclustering.computation.HNode;
  

public class HierClusterOutput implements  Serializable {
 
	private static final long serialVersionUID = -1310527094480912866L;
	private  byte[] hnode;
 
	public HierClusterOutput(){};
	public HierClusterOutput(byte[] hnode )
	{
		 this.hnode = hnode;
	}
 
	public void setHnode(byte[] hnode)
	{
		this.hnode = hnode;
	}
	public byte[] getHnode()
	{
		return this.hnode;
	}
	
	 
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("HierCluster Result\n");		 
		HNode node = (HNode)toObject(hnode);		 
		sb.append("hnode:" + node.getId() + ", height is " + node.getHeight()).append(" \n");		 
		while(node.getRight() != null)
		{
		    sb.append("hnode:" + node.getRight().getId() + ", height is " + node.getRight().getHeight()).append(" \n");
		    node = node.getRight();
		}		 
		return sb.toString();
	}


	@SuppressWarnings("deprecation")
	public static Object toObject(byte[] bytes){ 

		Object object = null; 

		try{ 

			object = new java.io.ObjectInputStream(new 
					java.io.ByteArrayInputStream(bytes)).readObject(); 

		}catch(java.io.IOException ioe){ 

			java.util.logging.Logger.global.log(java.util.logging.Level.SEVERE, 
					ioe.getMessage()); 

		}catch(java.lang.ClassNotFoundException cnfe){ 

			java.util.logging.Logger.global.log(java.util.logging.Level.SEVERE, 
					cnfe.getMessage()); 

		} 

		return object; 

	}
	
}
