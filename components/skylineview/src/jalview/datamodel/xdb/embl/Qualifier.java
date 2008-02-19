package jalview.datamodel.xdb.embl;

public class Qualifier {
    String name;
    String[] values;
    String[] evidence;
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
     * @return the values
     */
    public String[] getValues() {
        return values;
    }
    /**
     * @param values the values to set
     */
    public void setValues(String[] values) {
        this.values = values;
    }
    public void addEvidence(String qevidence)
    {
      if (evidence==null)
      {
        evidence = new String[1];
      }
      else
      {
        String[] temp = new String[evidence.length+1];
        System.arraycopy(evidence,0,temp,0,evidence.length);
        evidence = temp;
      }
      evidence[evidence.length-1] = qevidence;
    }
    public void addValues(String value)
    {
      if (values==null)
      {
        values = new String[1];
      }
      else
      {
        String[] temp = new String[values.length+1];
        System.arraycopy(values,0,temp,0,values.length);
        values = temp;
      }
      values[values.length-1] = value;
    }
    /**
     * @return the evidence
     */
    public String[] getEvidence()
    {
      return evidence;
    }
    /**
     * @param evidence the evidence to set
     */
    public void setEvidence(String[] evidence)
    {
      this.evidence = evidence;
    }
}
