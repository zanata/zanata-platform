package org.zanata.webtrans.client.rpc;

import java.util.HashMap;

import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.PersonSessionDetails;
import org.zanata.webtrans.shared.rpc.GetTranslatorList;
import org.zanata.webtrans.shared.rpc.GetTranslatorListResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetTranslatorListCommand implements Command {
    private final AsyncCallback<GetTranslatorListResult> callback;

    public DummyGetTranslatorListCommand(GetTranslatorList action,
            AsyncCallback<GetTranslatorListResult> callback) {
        this.callback = callback;
    }

    @Override
    public void execute() {
        Log.info("ENTER DummyGetTranslatorListCommand.execute()");

        HashMap<EditorClientId, PersonSessionDetails> translator =
                new HashMap<EditorClientId, PersonSessionDetails>();
        translator
                .put(new EditorClientId("dummySession", 1),
                        new PersonSessionDetails(
                                new Person(
                                        new PersonId("personID"),
                                        "Some Person with an Incredibly Long Name",
                                        "http://www.gravatar.com/avatar/longname@zanata.org?d=mm&s=16"),
                                null));
        callback.onSuccess(new GetTranslatorListResult(translator, translator
                .size()));
        Log.info("EXIT DummyGetTranslatorListCommand.execute()");
    }

}
