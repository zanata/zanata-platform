package org.fedorahosted.flies.webtrans.client.rpc;

import java.util.ArrayList;

import org.fedorahosted.flies.webtrans.shared.model.TransMemory;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTranslationMemory;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTranslationMemoryResult;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTranslationMemory.SearchType;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetTranslationMemoryCommand implements Command {

	private final GetTranslationMemory action;
	private final AsyncCallback<GetTranslationMemoryResult> callback;

	public DummyGetTranslationMemoryCommand(GetTranslationMemory action,
			AsyncCallback<GetTranslationMemoryResult> callback) {
		this.action = action;
		this.callback = callback;
	}

	@Override
	public void execute() {
		String query = action.getQuery();
		SearchType type = action.getSearchType();
		ArrayList<TransMemory> matches = new ArrayList<TransMemory>();
		matches.add(new TransMemory(type+"1", "target1", "sourceComment", "targetComment", "doc1", 1, 100));
		matches.add(new TransMemory(query, "target2", "sourceComment", "targetComment", "doc1", 1, 90));
		matches.add(new TransMemory("source3", "target3", "sourceComment", "targetComment", "doc2", 1, 85));
		matches.add(new TransMemory("<source4/>", "<target4/>", "sourceComment", "targetComment", "doc3", 1, 60));
		callback.onSuccess(new GetTranslationMemoryResult(matches));
	}

}
