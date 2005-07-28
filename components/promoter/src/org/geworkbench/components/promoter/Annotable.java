package org.geworkbench.components.promoter;

import java.util.Set;

public interface Annotable {
    public Object get(Object key);

    public void set(Object key, Object value);

    public Set keySet();
}
