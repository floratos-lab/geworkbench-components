package jalview.datamodel;

import jalview.util.MapList;

public class Mapping {
  /**
   * Contains the
   * start-end pairs mapping from
   * the associated sequence to the
   * sequence in the database
   * coordinate system
   * it also takes care of step difference between coordinate systems
   */
  MapList map=null;
  /**
   * The seuqence that map maps the associated seuqence to (if any).
   */
  SequenceI to=null;
  public Mapping(MapList map) {
    super();
    this.map = map;
  }
  public Mapping(SequenceI to, MapList map) {
    this(map);
    this.to = to;
  }
  /**
   * create a new mapping from
   * @param to the sequence being mapped
   * @param exon int[] {start,end,start,end} series on associated sequence
   * @param is int[] {start,end,...} ranges on the reference frame being mapped to
   * @param i step size on associated sequence
   * @param j step size on mapped frame
   */
  public Mapping(SequenceI to, int[] exon, int[] is, int i, int j)
  {
    this(to, new MapList(exon, is, i, j));
  }
  /**
   * create a duplicate (and independent) mapping object with
   * the same reference to any SequenceI being mapped to.
   * @param map2
   */
  public Mapping(Mapping map2)
  {
    if (map2!=this && map2!=null) {
      if (map2.map!=null)
      {
        map=new MapList(map2.map);
      }
      to = map2.to;
    }
  }
  /**
   * @return the map
   */
  public MapList getMap() {
    return map;
  }

  /**
   * @param map the map to set
   */
  public void setMap(MapList map) {
    this.map = map;
  }
  /**
   * Equals that compares both the to references and MapList mappings.
   * @param other
   * @return
   */
  public boolean equals(Mapping other) {
    if (other==null)
      return false;
    if (other==this)
      return true;
    if (other.to!=to)
      return false;
    if ((map!=null && other.map==null) || (map==null && other.map!=null))
      return false;
    if (map.equals(other.map))
      return true;
    return false;
  }
  /**
   * get the 'initial' position in the associated
   * sequence for a position in the mapped reference frame
   * @param mpos
   * @return
   */
  public int getPosition(int mpos)
  {
    if (map!=null) {
      int[] mp = map.shiftTo(mpos);
      if (mp!=null)
      {
        return mp[0];
      }
    }
    return mpos;
  }
  /**
   * gets boundary in direction of mapping 
   * @param position in mapped reference frame
   * @return int{start, end} positions in associated sequence (in direction of mapped word)
   */
  public int[] getWord(int mpos) {
    if (map!=null) {
      return map.getToWord(mpos);
    }
    return null;
  }
  /**
   * width of mapped unit in associated sequence
   *
   */
  public int getWidth() {
    if (map!=null) {
      return map.getFromRatio();
    }
    return 1;
  }

  /**
   * width of unit in mapped reference frame
   * @return
   */
  public int getMappedWidth() {
    if (map!=null) {
      return map.getToRatio();
    }
    return 1;
  }
  /**
   * get mapped position in the associated
   * reference frame for position pos in the
   * associated sequence.
   * @param pos
   * @return
   */
  public int getMappedPosition(int pos) {
    if (map!=null) {
      int[] mp = map.shiftFrom(pos);
      if (mp!=null)
      {
        return mp[0];
      }
    }
    return pos;
  }
  public int[] getMappedWord(int pos) {
    if (map!=null) {
      int[] mp = map.shiftFrom(pos);
      if (mp!=null)
      {
        return new int[] { mp[0], mp[0]+mp[2]*(map.getToRatio()-1)};
      }
    }
    return null;
  }
  /**
   * locates the region of feature f in the associated sequence's reference frame
   * @param f
   * @return one or more features corresponding to f
   */
  public SequenceFeature[] locateFeature(SequenceFeature f)
  {
    if (true) { // f.getBegin()!=f.getEnd()) {
      if (map!=null) {
        int[] frange = map.locateInFrom(f.getBegin(), f.getEnd());
        SequenceFeature[] vf = new SequenceFeature[frange.length/2];
        for (int i=0,v=0;i<frange.length;i+=2,v++) {
          vf[v] = new SequenceFeature(f);
          vf[v].setBegin(frange[i]);
          vf[v].setEnd(frange[i+1]);
          if (frange.length>2)
            vf[v].setDescription(f.getDescription() +"\nPart "+v);
        }
        return vf;
      }
    }
    if (false) //else
    {
      int[] word = getWord(f.getBegin());
      if (word[0]<word[1])
      {
        f.setBegin(word[0]);
      } else {
        f.setBegin(word[1]);
      }
      word = getWord(f.getEnd());
      if (word[0]>word[1])
      {
        f.setEnd(word[0]);
      } else {
        f.setEnd(word[1]);
      }
    }
    // give up and just return the feature.
    return new SequenceFeature[] { f };
  }

    /**
   * return a series of contigs on the associated sequence corresponding to
   * the from,to interval on the mapped reference frame
   * @param from
   * @param to
   * @return
   */
  public int[] locateRange(int from, int to) {
    //TODO
    return null;
  }
  /**
   * return a series of contigs on the mapped reference frame corresponding to
   * the from,to interval on the associated sequence
   * @param from
   * @param to
   * @return
   */
  public int[] locateMappedRange(int from, int to) {
    //TODO
    return null;
  }
}
