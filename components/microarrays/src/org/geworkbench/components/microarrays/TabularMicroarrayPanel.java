package org.geworkbench.components.microarrays;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSGenotypicMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.events.MicroarraySetViewEvent;
import org.geworkbench.util.microarrayutils.MicroarrayViewEventBase;

/**
 * 
 * <p>Copyright Columbia University</p>
 *
 * @author Adam Margolin
 * @author zji
 * @version $Id$
 */
@AcceptTypes({DSMicroarraySet.class})
public class TabularMicroarrayPanel extends MicroarrayViewEventBase {

	private static Log log = LogFactory.getLog(TabularMicroarrayPanel.class);

    public TabularMicroarrayPanel() {
        try {
        	initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static TableCellRenderer defaultRenderer = new DefaultTableCellRenderer();
    
    private TableCellRenderer renderer = new DefaultTableCellRenderer() {
        private static final long serialVersionUID = -2148986327315654796L;
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component defaultComponent = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

			/* Convert the model index to the visual index */
			int modelCol = table.convertColumnIndexToModel(col);
            
			// note: because c is in fact reused, we need to reset the properties that may be changed
            if (modelCol == 0) {
            	if(!isSelected) {
	                c.setBackground(Color.lightGray);
	                c.setForeground(defaultComponent.getForeground());
            	}
                ((JLabel) c).setHorizontalAlignment(JLabel.LEFT);
                ((JComponent) c).setBorder(BorderFactory.createLineBorder(Color.white));
            } else {
                DSGeneMarker stats = uniqueMarkers.get(row);
                if (stats != null) {
                    DSMutableMarkerValue marker = maSetView.items().get(modelCol - 1).getMarkerValue(stats.getSerial());
                    if (marker.isMissing()) {
                        c.setBackground(Color.yellow);
                        c.setForeground(Color.blue);
                    } else if (marker.isMasked()) {
                        c.setBackground(Color.pink);
                        c.setForeground(Color.blue);
                    } else {
                        c.setBackground(defaultComponent.getBackground());
                        c.setForeground(defaultComponent.getForeground());
                    }
                    ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                }
            }
            return c;
        }
    };
    
    private AbstractTableModel microarrayTableModel = new AbstractTableModel() {
		private static final long serialVersionUID = -6400581862850298421L;
		final NumberFormat nf = NumberFormat.getInstance();
		{
			nf.setMaximumFractionDigits(2);
		}

        public int getColumnCount() {
            if (maSetView == null || refMASet == null) {
                return 0;
            } else {
                int cCount = maSetView.items().size() + 1;
                return cCount;
            }
        }

        public int getRowCount() {
            if (uniqueMarkers == null) {
                return 0;
            } else {
                return uniqueMarkers.size();
            }
        }

        public Object getValueAt(int row, int col) {
            if (row >= uniqueMarkers.size()) {
            	return "";
            }

            DSGeneMarker stats = uniqueMarkers.get(row);
			if (stats != null) {
				if (col == 0) {
					return stats.toString();
				} else {
					DSMicroarray array = maSetView.get(col - 1);
					DSMarkerValue value = array.getMarkerValue(row);
					if (value instanceof DSGenotypicMarkerValue) {
						return value.toString();
					} else {
						if (value.isMissing())
							return "n/a";
						return nf.format(maSetView.getValue(row, col - 1));
					}
				}
			} else {
				return "Invalid";
			}
        }

        public String getColumnName(int col) {
            if (col == 0) {
                return "Marker";
            } else {
                return maSetView.items().get(col - 1).getLabel();
            }
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private JTable jTable1 = null;

    private void initialize() {
        JScrollPane jScrollPane1 = new JScrollPane();
        JToolBar jToolBar1 = new JToolBar();
        JCheckBox jShowAllMArrays = new JCheckBox();
        JCheckBox jShowAllMarkers = new JCheckBox();

        jTable1 = new JTable(microarrayTableModel) {
			private static final long serialVersionUID = 8762298664180020948L;

			public Dimension getPreferredScrollableViewportSize() {
                return new Dimension(20 * getModel().getColumnCount(), 20 * getModel().getRowCount());
            }

            public boolean getScrollableTracksViewportWidth() {
                return false;
            }
        };

        jTable1.setEnabled(true);
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jTable1.setDefaultRenderer(Object.class, renderer);
        jTable1.setCellSelectionEnabled(true);

        jToolBar1.setBorder(null);
        mainPanel.add(jScrollPane1, BorderLayout.CENTER);
        jToolBar1.add(jShowAllMArrays, null);
        jToolBar1.add(jShowAllMarkers, null);
        jScrollPane1.getViewport().add(jTable1, null);
    }

    @SuppressWarnings("unchecked")
	@Override
    protected void fireModelChangedEvent(MicroarraySetViewEvent event) {
    	microarrayTableModel.fireTableStructureChanged();
    	log.debug("fireModelChangedEvent");
    }
    
}
