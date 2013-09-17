package org.geworkbench.components.promoter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;



class TFListModel extends AbstractListModel {

	private static final long serialVersionUID = 8988648593491632856L;
	
	Vector<String> tfNameSet = new Vector<String>();
	Vector<String> tfFilteredNameSet = new Vector<String>();
	String filterText = "";
	String taxonomyFilter = null;
	
	private DefaultListModel selectedTFModel = null;

	
	public TFListModel(DefaultListModel selectedTFModel) {
		super();
		this.selectedTFModel = selectedTFModel;
	}
	

	void setFilterText(String filterText) {
		this.filterText = filterText;
	}
	
	void setTaxonomyFilter(String taxonomyFilter) {
		this.taxonomyFilter = taxonomyFilter;
	}
	
	HashMap<String, String> fullNameTaxGroupMap = new HashMap<String, String>() ;
	void setFullNameTaxGroupMap(HashMap<String, String> fullNameTaxGroupMap) {
		this.fullNameTaxGroupMap = fullNameTaxGroupMap;
	}

	public int getSize() {
        if (tfFilteredNameSet == null) {
            return 0;
        }
        return tfFilteredNameSet.size();
    }

    public Object getElementAt(int index) {
        if ((tfFilteredNameSet == null) || tfFilteredNameSet.size() <= index) {
            return null;
        } else {
            return tfFilteredNameSet.get(index);
        }
    }

    public void addElement(Object obj) {
        if (!tfNameSet.contains(obj.toString())) {
            tfNameSet.add(obj.toString());
            Collections.sort(tfNameSet);
            refilter();
        }
    }

    public Object remove(int index) {
    	String rv = (String)tfFilteredNameSet.get(index);
    	tfNameSet.remove(rv);
    	
        refilter();
        return rv;
    }


    /**
     * Indicates to the associated JList that the contents need to be redrawn.
     */
    void refresh() {
        if (tfFilteredNameSet == null) {
            fireContentsChanged(this, 0, 0);
        } else {
        	refilter();
            fireContentsChanged(this, 0, tfFilteredNameSet.size());
        }
    }

    public int indexOf(Object object) {
    	return tfFilteredNameSet.indexOf(object);
    }

    private void refilter(){
		tfFilteredNameSet.clear();
		
		for (int i = 0; i < tfNameSet.size(); i++) {
			
			/* Text Field Filter */
			String tfName = tfNameSet.get(i).toLowerCase().trim();
			if (!tfName.contains(filterText) && filterText != null
					&& filterText != "") {
				continue;
 			}
			
			/* Drop Down Filter */
			if (taxonomyFilter != null && !taxonomyFilter.equals("All Taxa")){
				String taxonomy = fullNameTaxGroupMap.get(tfNameSet.get(i));
				if (taxonomy == null || !taxonomy.equalsIgnoreCase(taxonomyFilter)){
					continue;
				}
			}
			
			/* Selected TFs Filter */
			if (selectedTFModel == null || selectedTFModel.contains(tfNameSet.get(i))) {
	        	continue;
	        }
			
			tfFilteredNameSet.add(tfNameSet.get(i));
		}
    }
}