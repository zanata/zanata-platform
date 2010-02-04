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
		ArrayList<TransMemory> matches = new ArrayList<TransMemory>();
		matches.add(new TransMemory("source1", "target1"));
		matches.add(new TransMemory("source2", "target2"));
		matches.add(new TransMemory("source3", "target3"));
		matches.add(new TransMemory("source4", "target4"));
		callback.onSuccess(new GetTranslationMemoryResult(matches));
	}

}
