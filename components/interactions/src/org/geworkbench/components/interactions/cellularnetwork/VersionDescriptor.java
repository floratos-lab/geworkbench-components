package org.geworkbench.components.interactions.cellularnetwork;

/**
 * Created by Min You  
 */

/**
 * The  Version Descriptorof one Dataset.
 */
public class VersionDescriptor {
    private String version;    
    private Boolean requiresAuthentication;
    

    public VersionDescriptor(String version, boolean requiresAuthentication) {
        this.version = version;
        this.requiresAuthentication = requiresAuthentication;
         
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

  

    public void setRequiresAuthentication(Boolean requiresAuthentication) {
        this.requiresAuthentication = requiresAuthentication;
    }

    public Boolean  getRequiresAuthentication() {
        return requiresAuthentication;
    }

   
}
