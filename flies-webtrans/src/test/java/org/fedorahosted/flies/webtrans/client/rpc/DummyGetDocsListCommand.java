/**
 * 
 */
package org.fedorahosted.flies.webtrans.client.rpc;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.rpc.GetDocsList;
import org.fedorahosted.flies.gwt.rpc.GetDocsListResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

final class DummyGetDocsListCommand implements Command {
	private final GetDocsList action;
	private final AsyncCallback<GetDocsListResult> callback;

	DummyGetDocsListCommand(GetDocsList gtuAction,
			AsyncCallback<GetDocsListResult> callback) {
		this.action = gtuAction;
		this.callback = callback;
	}

	@Override
	public void execute() {
		ProjectContainerId projectContainerId = action.getProjectContainerId();
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
		names.add(new DocName(new DocumentId(7), "long name, a name which is really quite long, too wide to be displayed without scrolling (in most cases)", null));
		names.add(new DocName(new DocumentId(8), "another long name, a name which is really quite long, too wide to be displayed without scrolling (in most cases)", "long path, a path which is, again, really quite long, and also too wide to be displayed without scrolling (in most cases)"));
		for (int n=10; n<99; n++) // two digit numbers, to make sorting happier
			names.add(new DocName(new DocumentId(n), "multi"+n, ""));
		return names;
	}
	
}