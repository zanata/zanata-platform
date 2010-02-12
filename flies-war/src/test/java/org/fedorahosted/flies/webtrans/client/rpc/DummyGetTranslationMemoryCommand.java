package org.fedorahosted.flies.webtrans.client.rpc;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.TransMemory;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemory;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemoryResult;

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
		boolean fuzzy = action.getFuzzy();
		ArrayList<TransMemory> matches = new ArrayList<TransMemory>();
		matches.add(new TransMemory(fuzzy?"fuzzy1":"source1", "target1", "doc1", 100));
		matches.add(new TransMemory("source2", "target2", "doc1", 90));
		matches.add(new TransMemory("source3", "target3", "doc2", 85));
		matches.add(new TransMemory("<source4/>", "<target4/>", "doc3", 60));
		callback.onSuccess(new GetTranslationMemoryResult(matches));
	}

}
