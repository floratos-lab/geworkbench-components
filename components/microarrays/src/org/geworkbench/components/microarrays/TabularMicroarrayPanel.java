package org.geworkbench.components.microarrays;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.engine.management.AcceptTypes;
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
					DSMarkerValue value = array.getMarkerValue(stats);

					if (value.isMissing())
						return "n/a";
					else
						return nf.format(maSetView.getValue(row, col - 1));
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
		jTable1.getColumnModel().addColumnModelListener(new ColumnOrderTableModelListener(jTable1));

        jToolBar1.setBorder(null);
        mainPanel.add(jScrollPane1, BorderLayout.CENTER);
        jToolBar1.add(jShowAllMArrays, null);
        jToolBar1.add(jShowAllMarkers, null);
        jScrollPane1.getViewport().add(jTable1, null);
    }

	@Override
    protected void fireModelChangedEvent() {
    	microarrayTableModel.fireTableStructureChanged();
    	log.debug("fireModelChangedEvent");
		if (activatedArrays != null && activatedArrays.panels().size() > 0
				&& activatedArrays.size() > 0)
			return;

		ArrayList<Integer> columnOrder = new ArrayList<Integer>();

		if(maSetView!=null && maSetView.getDataSet()!=null 
				&& !maSetView.getDataSet().getColumnOrder().isEmpty())
			columnOrder = maSetView.getDataSet().getColumnOrder();

		if (!columnOrder.isEmpty())
		{
	    	int columncount = jTable1.getColumnCount();
			for (int i = 0; i < columncount; i++)
				jTable1.addColumn(jTable1.getColumnModel().getColumn(columnOrder.get(i)));

			for (int i = 0; i < columncount; i++)
				jTable1.removeColumn(jTable1.getColumnModel().getColumn(0));
		}
    }

    private class ColumnOrderTableModelListener implements TableColumnModelListener {
    	private JTable table;
    	public ColumnOrderTableModelListener(JTable mytable){
    		table = mytable;
    	}
		@Override
		public void columnAdded(TableColumnModelEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void columnMarginChanged(ChangeEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void columnMoved(TableColumnModelEvent e) {
			// TODO Auto-generated method stub
			int from = e.getFromIndex();
			int to = e.getToIndex();
			if (from == to) return;
			if (activatedArrays != null && activatedArrays.panels().size() > 0
					&& activatedArrays.size() > 0)
				return;

			ArrayList<Integer> columnOrder = new ArrayList<Integer>();
			int columncount = table.getColumnCount();
			for (int i = 0; i < columncount; i++)
				columnOrder.add(table.convertColumnIndexToModel(i));
			maSetView.getDataSet().setColumnOrder(columnOrder);
		}

		@Override
		public void columnRemoved(TableColumnModelEvent e) {
			// TODO Auto-generated method stub		
		}

		@Override
		public void columnSelectionChanged(ListSelectionEvent e) {
			// TODO Auto-generated method stub
			
		}
    }
    
}
