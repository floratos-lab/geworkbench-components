package org.geworkbench.components.filtering;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.engine.config.VisualPlugin;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Columbia.</p>
 * @author Xiaoqing Zhang
 * @version 1.0
 */

/**
 * The parameters panel for the <code>GenepixFlagsFilter</code>
 * filter. The measures will be removed based on their flags.
 */
//public class GenepixFlagsFilterPanel extends ParameterPanel implements Serializable, ItemListener{
 public class GenepixFlagsFilterPanel extends AbstractSaveableParameterPanel implements Serializable, ItemListener,VisualPlugin{
    final String PRESENT_OPTION = "P";
      final String ABSENT_OPTION = "A";
      final String MARGINAL_OPTION = "M";
      private GridLayout gridLayout1 = new GridLayout();
      private JLabel callSelectionLabel = new JLabel("<html><p>Select flags to</p><p>be filtered out.</p></html>");
      private JCheckBox presentButton = new JCheckBox(PRESENT_OPTION);
      private JCheckBox absentButton = new JCheckBox(ABSENT_OPTION);
      private JCheckBox marginalButton = new JCheckBox(MARGINAL_OPTION);
      private boolean presentButtonStatus;
      private boolean absentButtonStatus;
      private boolean marginalButtonStatus;

      public GenepixFlagsFilterPanel() {
          try {
              jbInit();
          } catch (Exception e) {
              e.printStackTrace();
          }

      }

      private void jbInit() throws Exception {
          this.setLayout(new FlowLayout());
          JPanel container = new JPanel();
          gridLayout1.setColumns(2);
          gridLayout1.setHgap(10);
          gridLayout1.setRows(1);
          gridLayout1.setVgap(10);
          container.setLayout(gridLayout1);
          // Initialize the selection status for the check box buttons.
          presentButton.setSelected(false);
          presentButtonStatus = false;
          absentButton.setSelected(false);
          absentButtonStatus = false;
          marginalButton.setSelected(false);
          marginalButtonStatus = false;
          //Put the check boxes in a column in a panel
          JPanel buttonContainer = new JPanel(new GridLayout(0, 1));
          buttonContainer.add(presentButton);
          buttonContainer.add(absentButton);
          buttonContainer.add(marginalButton);
          // Set the button item selection listener.
          presentButton.addItemListener(this);
          absentButton.addItemListener(this);
          marginalButton.addItemListener(this);
          container.add(callSelectionLabel);
          container.add(buttonContainer);
          container.setPreferredSize(new Dimension(250, 55));
          this.add(container);
      }

      /**
       * Check if the "Present" option is selected.
       */
      public boolean isPresentSelected() {
          return presentButtonStatus;
      }

      /**
       * Check if the "Absent" option is selected.
       */
      public boolean isAbsentSelected() {
          return absentButtonStatus;
      }

      /**
       * Check if the "Marginal" option is selected.
       */
      public boolean isMarginalSelected() {
          return marginalButtonStatus;
      }

      /**
       * Listens to the check boxes.
       */
      public void itemStateChanged(ItemEvent e) {
          Object source = e.getItemSelectable();
          if (source == presentButton)
              presentButtonStatus = !presentButtonStatus;
          else if (source == absentButton)
              absentButtonStatus = !absentButtonStatus;
          else if (source == marginalButton)
              marginalButtonStatus = !marginalButtonStatus;
      }

      /**
         * receiveProjectSelection
         *
         * @param e ProjectEvent
         */
        @Subscribe public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
            if (e.getMessage().equals(org.geworkbench.events.ProjectEvent.CLEARED)) {
//                refMASet = null;
//                fireModelChangedEvent(null);
            } else {
                DSDataSet dataSet = e.getDataSet();
                if (dataSet instanceof CSExprMicroarraySet) {
                   System.out.println (((CSExprMicroarraySet)dataSet).getCompatibilityLabel());

                    }
                }
                //refreshMaSetView();
            }


            public Component getComponent(){
            return this;
            };
      private void writeObject(java.io.ObjectOutputStream out) throws IOException {
          out.defaultWriteObject();
      }

      private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
          in.defaultReadObject();
          revalidate();
    }

}
