package jalview.datamodel.xdb.embl;


import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Vector;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;

public class EmblFile {
    Vector entries;
    Vector errors;
    /**
     * @return the entries
     */
    public Vector getEntries() {
        return entries;
    }
    /**
     * @param entries the entries to set
     */
    public void setEntries(Vector entries) {
        this.entries = entries;
    }
    /**
     * @return the errors
     */
    public Vector getErrors() {
        return errors;
    }
    /**
     * @param errors the errors to set
     */
    public void setErrors(Vector errors) {
        this.errors = errors;
    }
    /**
     * Parse an EmblXML file into an EmblFile object
     * @param file
     * @return parsed EmblXML or null if exceptions were raised
     */
    public static EmblFile getEmblFile(File file)
    {
        if (file==null)
            return null;
        try {
            return EmblFile.getEmblFile(new FileReader(file));
        }
        catch (Exception e) {
            System.err.println("Exception whilst reading EMBLfile from "+file);
            e.printStackTrace(System.err);
        }
        return null;
    }
    public static EmblFile getEmblFile(Reader file) {
        EmblFile record = new EmblFile();
        try
        {
          // 1. Load the mapping information from the file
          Mapping map = new Mapping(record.getClass().getClassLoader());
          java.net.URL url = record.getClass().getResource("/embl_mapping.xml");
          map.loadMapping(url);

          // 2. Unmarshal the data
          Unmarshaller unmar = new Unmarshaller(record.getClass());
          try {
              // uncomment to DEBUG EMBLFile reading unmar.setDebug(jalview.bin.Cache.log.isDebugEnabled());
          } catch (Exception e) {};
//          unmar.setIgnoreExtraElements(true);
          unmar.setMapping(map);

          record = (EmblFile) unmar.unmarshal(file);
        }
        catch (Exception e)
        {
          e.printStackTrace(System.err);
          record=null;
        }


        return record;
      }
    public static void main(String args[]) {
        EmblFile myfile = EmblFile.getEmblFile(new File("C:\\Documents and Settings\\JimP\\workspace-3.2\\Jalview Release\\schemas\\embleRecordV1.1.xml"));
        if (myfile!=null && myfile.entries!=null && myfile.entries.size()>0)
            System.out.println(myfile.entries.size()+" Records read.");
        }
}
