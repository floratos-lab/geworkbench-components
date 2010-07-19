package org.geworkbench.components.annotations;

public class EvsIdData implements Comparable<EvsIdData> {

    public String evsId;

    public EvsIdData(String evsId) {
        this.evsId = evsId;
    }

	@Override
	public int compareTo(EvsIdData evsIdData) {
        return evsId.compareTo(evsIdData.evsId);
	}
}