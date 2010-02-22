package org.fedorahosted.flies.webtrans.client.rpc;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.Concept;
import org.fedorahosted.flies.gwt.model.TermEntry;
import org.fedorahosted.flies.gwt.rpc.GetGlossaryConcept;
import org.fedorahosted.flies.gwt.rpc.GetGlossaryConceptResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetGlossaryCommand implements Command {

	private final GetGlossaryConcept action;
	private final AsyncCallback<GetGlossaryConceptResult> callback;

	public DummyGetGlossaryCommand(GetGlossaryConcept action,
			AsyncCallback<GetGlossaryConceptResult> callback) {
		this.action = action;
		this.callback = callback;
	}

	@Override
	public void execute() {
		ArrayList<Concept> matches = new ArrayList<Concept>();
		matches.add(new Concept("hello", "an expression of greeting", "", new TermEntry("hallo", "")));
		matches.add(new Concept("hello", "an expression of greeting", "", new TermEntry("bonjour", "")));
		callback.onSuccess(new GetGlossaryConceptResult(matches));
	}

}
