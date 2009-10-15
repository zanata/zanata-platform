package org.fedorahosted.flies.webtrans.client.rpc;


import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.LocaleId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.gwt.model.TransUnitId;
import org.fedorahosted.flies.gwt.rpc.GetDocsList;
import org.fedorahosted.flies.gwt.rpc.GetDocsListResult;
import org.fedorahosted.flies.gwt.rpc.GetTransUnits;
import org.fedorahosted.flies.gwt.rpc.GetTransUnitsResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
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
			AsyncCallback<GetTransUnitsResult> gtuCallback = (AsyncCallback<GetTransUnitsResult>) callback;
			DeferredCommand.addCommand(new GetTransUnitCommand(gtuAction, gtuCallback));
		} else if (action instanceof GetDocsList) {
			final GetDocsList gdlAction = (GetDocsList) action;
			AsyncCallback<GetDocsListResult> gdlCallback = (AsyncCallback<GetDocsListResult>) callback;
			DeferredCommand.addCommand(new GetDocsListCommand(gdlAction, gdlCallback));
		} else {
			Log.info("DummyDispatchAsync: ignoring action of "+action.getClass());
//			callback.onFailure(new RuntimeException());
		}
	}
	
	private static final class GetDocsListCommand implements Command {
		private final GetDocsList gdlAction;
		private final AsyncCallback<GetDocsListResult> callback;

		private GetDocsListCommand(GetDocsList gtuAction,
				AsyncCallback<GetDocsListResult> callback) {
			this.gdlAction = gtuAction;
			this.callback = callback;
		}

		@Override
		public void execute() {
			ProjectContainerId projectContainerId = gdlAction.getProjectContainerId();
			GetDocsListResult result = new GetDocsListResult(projectContainerId, generateTransUnitSampleData()); 
			callback.onSuccess(result);
		}

		private ArrayList<DocName> generateTransUnitSampleData() {
			ArrayList<DocName> names = new ArrayList<DocName>();
			names.add(new DocName(new DocumentId(1), "path1name1", "path/1"));
			names.add(new DocName(new DocumentId(2), "path1name2", "path/1"));
			names.add(new DocName(new DocumentId(3), "path2name1", "path/2"));
			names.add(new DocName(new DocumentId(4), "path2name2", "path/2"));
			names.add(new DocName(new DocumentId(5), "name2", ""));
			names.add(new DocName(new DocumentId(6), "name1", null));
			return names;
		}
		
	}
	
	private static final class GetTransUnitCommand implements Command {
		private final GetTransUnits gtuAction;
		private final AsyncCallback<GetTransUnitsResult> callback;

		private GetTransUnitCommand(GetTransUnits gtuAction,
				AsyncCallback<GetTransUnitsResult> callback) {
			this.gtuAction = gtuAction;
			this.callback = callback;
		}

		@Override
		public void execute() {
			DocumentId documentId = gtuAction.getDocumentId();
			int count = gtuAction.getCount();
			int offset = gtuAction.getOffset();
			int totalCount = count * 5;
			GetTransUnitsResult result = new GetTransUnitsResult(
					documentId, 
					generateTransUnitSampleData(gtuAction.getLocaleId(), count, offset), 
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

}
