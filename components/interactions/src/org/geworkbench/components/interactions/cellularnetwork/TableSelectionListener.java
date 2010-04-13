package org.geworkbench.components.interactions.cellularnetwork;
import java.util.EventListener;

/**
  * The listener that's notified when a table selection value changes.
  * @author Min You
  */
public interface TableSelectionListener extends EventListener {
  /**
  * Called whenever the value of the selection changes.
  */
  public void valueChanged(TableSelectionEvent e);
}
