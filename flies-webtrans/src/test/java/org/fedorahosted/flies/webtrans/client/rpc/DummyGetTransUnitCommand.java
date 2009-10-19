/**
 * 
 */
package org.fedorahosted.flies.webtrans.client.rpc;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.LocaleId;
import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.gwt.model.TransUnitId;
import org.fedorahosted.flies.gwt.rpc.GetTransUnits;
import org.fedorahosted.flies.gwt.rpc.GetTransUnitsResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

final class DummyGetTransUnitCommand implements Command {
	private final GetTransUnits action;
	private final AsyncCallback<GetTransUnitsResult> callback;

	DummyGetTransUnitCommand(GetTransUnits gtuAction,
			AsyncCallback<GetTransUnitsResult> callback) {
		this.action = gtuAction;
		this.callback = callback;
	}

	@Override
	public void execute() {
		DocumentId documentId = action.getDocumentId();
		int count = action.getCount();
		int offset = action.getOffset();
		int totalCount = count * 5;
		GetTransUnitsResult result = new GetTransUnitsResult(
				documentId, 
				generateTransUnitSampleData(action.getLocaleId(), count, offset), 
				totalCount);
		callback.onSuccess(result);
	}

	private ArrayList<TransUnit> generateTransUnitSampleData(LocaleId localeId, int numRows, int start) {
		ArrayList<TransUnit> units = new ArrayList<TransUnit>();
		for(int i=start;i<start+numRows; i++) {
			TransUnit unit = new TransUnit( new TransUnitId(i+1), localeId, "<hellow num=\"" + (i+1) + "\" />", "<world> \"" + (i+1) +"\"</world>");
			unit.setFuzzy(Math.random() > 0.7);
			units.add(unit);
		}
		return units;
	}
	
}