package org.geworkbench.components.aracne;

public class AracneException extends Exception {
 
	private static final long serialVersionUID = -3739666680356630618L;	 
	
	    private String message;	   
	 
	    public AracneException(String s) {
	        super(s);
	        this.message = s;
	    }
	 
	    @Override
	    public String getMessage() {
	        return this.message;
	    }
	

}