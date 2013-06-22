package org.geworkbench.components.lincs;

public class ValueObject {

	private Object value;	 
	private Long referenceId;  
	
	public ValueObject(Object value, long referenceId) {
		this.value = value;
		this.referenceId = referenceId;
	}
	
	public Object getValue()
	{
		return value;
	}
	
	public long getReferenceId()
	{
		if (referenceId  == null)
			return 0;
		else
			return referenceId.longValue();
	}
	
	
	@Override
	 public String toString() {
	    if (value == null)
	    	return "";
	    else
	    	return value.toString();
	 }
	
	
}
