package org.fedorahosted.flies.gwt.rpc;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.TransUnit;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Result;

public class GotTransUnits implements Result, IsSerializable {

	private static final long serialVersionUID = 3481107839585398632L;

	private int totalCount;
	private ArrayList<TransUnit> units;

	private GotTransUnits()	{
		
	}
	
	public GotTransUnits(ArrayList<TransUnit> units, int totalCount) {
		this.units = units;
		this.totalCount = totalCount;
	}
	
	public ArrayList<TransUnit> getUnits() {
		return units;
	}
	
	public int getTotalCount() {
		return totalCount;
	}
	
	
}
