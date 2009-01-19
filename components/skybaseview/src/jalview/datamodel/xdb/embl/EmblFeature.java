package jalview.datamodel.xdb.embl;
import java.util.Hashtable;
import java.util.Vector;
public class EmblFeature {
    String name;
    Vector dbRefs;
    Vector qualifiers;
    Vector locations;
    
    /**
     * @return the dbRefs
     */
    public Vector getDbRefs() {
        return dbRefs;
    }
    /**
     * @param dbRefs the dbRefs to set
     */
    public void setDbRefs(Vector dbRefs) {
        this.dbRefs = dbRefs;
    }
    /**
     * @return the locations
     */
    public Vector getLocations() {
        return locations;
    }
    /**
     * @param locations the locations to set
     */
    public void setLocations(Vector locations) {
        this.locations = locations;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return the qualifiers
     */
    public Vector getQualifiers() {
        return qualifiers;
    }
    /**
     * @param qualifiers the qualifiers to set
     */
    public void setQualifiers(Vector qualifiers) {
        this.qualifiers = qualifiers;
    }
}
