package org.fedorahosted.flies.webtrans.client.rpc;

import java.util.ArrayList;

import org.fedorahosted.flies.common.LocaleId;
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
		TermEntry frEntry = new TermEntry("trou noir", "");
		frEntry.setLocaleid(new LocaleId("fr-FR"));
		TermEntry deEntry = new TermEntry("Schwarzes Loch", "");
		deEntry.setLocaleid(new LocaleId("de-DE"));
		String desc = "The leftover core of a super massive star after a supernova,that exerts a tremendous gravitational pull.";
		matches.add(new Concept("black hole", desc, "", deEntry));
		callback.onSuccess(new GetGlossaryConceptResult(matches));
	}

}
