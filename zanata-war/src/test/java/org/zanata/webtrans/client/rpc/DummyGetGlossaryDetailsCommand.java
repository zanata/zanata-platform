package org.zanata.webtrans.client.rpc;

import java.util.ArrayList;
import java.util.Date;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsAction;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetGlossaryDetailsCommand implements Command {

    private final AsyncCallback<GetGlossaryDetailsResult> callback;
    private final GetGlossaryDetailsAction action;

    DummyGetGlossaryDetailsCommand(GetGlossaryDetailsAction action,
            AsyncCallback<GetGlossaryDetailsResult> callback) {
        this.callback = callback;
        this.action = action;
    }

    @Override
    public void execute() {
        Log.info("ENTER DummyGetGlossaryDetailsCommand.execute()");
        ArrayList<GlossaryDetails> items = new ArrayList<GlossaryDetails>();
        for (int i = 0; i < 4; i++) {
            ArrayList<String> srcComments = new ArrayList<String>();
            ArrayList<String> targetComments = new ArrayList<String>();

            srcComments.add("Source Comment " + (1 + i));
            srcComments.add("Source Comment " + (2 + i));
            srcComments.add("Source Comment " + (3 + i));

            targetComments.add("Target Comment " + (1 + i));
            targetComments.add("Target Comment " + (2 + i));
            targetComments.add("Target Comment " + (3 + i));
            targetComments.add("Target Comment " + (4 + i));
            targetComments.add("Target Comment " + (5 + i));
            targetComments.add("Target Comment " + (6 + i));

            GlossaryDetails details =
                    new GlossaryDetails("source content:" + (i + 1),
                            "target content:" + (i + 1), srcComments,
                            targetComments, "Dummy source ref " + (i + 1),
                            new LocaleId("en-us"), action.getWorkspaceId()
                                    .getLocaleId(), i, new Date());
            items.add(details);
        }

        callback.onSuccess(new GetGlossaryDetailsResult(items));
        Log.info("EXIT DummyGetGlossaryDetailsCommand.execute()");

    }
}
