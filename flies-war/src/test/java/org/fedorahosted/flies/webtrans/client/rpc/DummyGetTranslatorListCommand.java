package org.fedorahosted.flies.webtrans.client.rpc;

import java.util.ArrayList;
import java.util.Arrays;

import org.fedorahosted.flies.webtrans.shared.model.Person;
import org.fedorahosted.flies.webtrans.shared.model.PersonId;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTranslatorList;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTranslatorListResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetTranslatorListCommand implements Command {

	private final GetTranslatorList action;
	private final AsyncCallback<GetTranslatorListResult> callback;

	public DummyGetTranslatorListCommand(GetTranslatorList action,
			AsyncCallback<GetTranslatorListResult> callback) {
		this.action = action;
		this.callback = callback;
	}

	@Override
	public void execute() {
		callback.onSuccess(new GetTranslatorListResult(new ArrayList(Arrays.asList(new Person(new PersonId("personID"), "Some Person with an Incredibly Long Name")))));
	}

}
