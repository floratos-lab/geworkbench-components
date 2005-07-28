package org.geworkbench.components.promoter;

import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;

import java.util.HashMap;
import java.util.Set;

public class AnnotableSequence extends CSSequence implements Annotable {
    private HashMap annotation = new HashMap();

    public AnnotableSequence(CSSequence seq) {
        super(seq.getLabel(), seq.getSequence());
    }

    /**
     * get
     *
     * @param key Object
     * @return Object
     */
    public Object get(Object key) {
        return annotation.get(key);
    }

    /**
     * set
     *
     * @param key   Object
     * @param value Object
     */
    public void set(Object key, Object value) {
        annotation.put(key, value);
    }

    public Set keySet() {
        return annotation.keySet();
    }
}
