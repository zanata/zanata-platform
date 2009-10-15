package org.fedorahosted.flies.webtrans.client.rpc;


import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.gwt.rpc.GetTransUnits;
import org.fedorahosted.flies.gwt.rpc.GetTransUnitsResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyDispatchAsync extends SeamDispatchAsync {
	public DummyDispatchAsync() {
		Log.info("DummyDispatchAsync()");
	}

	@Override
	public <A extends Action<R>, R extends Result> void execute(A action,
			AsyncCallback<R> callback) {

		if (action instanceof GetTransUnits) {
			GetTransUnits gtuAction = (GetTransUnits) action;
			int count = gtuAction.getCount();
			int offset = gtuAction.getOffset();
			int totalCount = count * 5;
			GetTransUnitsResult result = new GetTransUnitsResult(((GetTransUnits) action).getDocumentId(), generateTransUnitSampleData(count, offset), totalCount);
			callback.onSuccess((R) result);
		}
	}
	
	private ArrayList<TransUnit> generateTransUnitSampleData(int numRows, int start) {
		ArrayList<TransUnit> units = new ArrayList<TransUnit>();
		for(int i=start;i<start+numRows; i++) {
			TransUnit unit = new TransUnit("<hellow num=\"" + (i+1) + "\" />", "<world> \"" + (i+1) +"\"</world>");
			unit.setFuzzy(Math.random() > 0.7);
			units.add(unit);
		}
		return units;
	}

}
