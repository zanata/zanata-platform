package org.fedorahosted.flies.gwt.rpc;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.TransUnit;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Result;

public class GotTransUnits implements Result, IsSerializable {

	private static final long serialVersionUID = 3481107839585398632L;

	private ArrayList<TransUnit> units;

	private GotTransUnits()	{
		
	}
	
	public GotTransUnits(ArrayList<TransUnit> units) {
		this.units = units;
	}
	
	public ArrayList<TransUnit> getUnits() {
		return units;
	}
	
	
}
