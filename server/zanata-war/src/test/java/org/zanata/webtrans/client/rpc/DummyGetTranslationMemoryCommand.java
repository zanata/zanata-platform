package org.zanata.webtrans.client.rpc;

import java.util.ArrayList;

import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

import static org.zanata.webtrans.shared.model.TransMemoryResultItem.MatchType.ApprovedInternal;
import static org.zanata.webtrans.shared.model.TransMemoryResultItem.MatchType.TranslatedInternal;

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
        Log.info("ENTER DummyGetTranslationMemoryCommand.execute()");
        ArrayList<TransMemoryResultItem> matches =
                new ArrayList<TransMemoryResultItem>();
        ArrayList<String> source = new ArrayList<String>();
        source.add("<s>source1</s>");
        source.add("<s>source2</s>");
        ArrayList<String> target1 = new ArrayList<String>();
        target1.add("<tr> &lt;target1</tr>");
        target1.add("<tr> &lt;target1-1</tr>");
        ArrayList<String> target2 = new ArrayList<String>();
        target2.add("<tr> &lt;target2</tr>");
        ArrayList<String> target3 = new ArrayList<String>();
        target3.add("<tr> &lt;target3</tr>");
        ArrayList<String> target4 = new ArrayList<String>();
        target4.add("<tr> &lt;target4</tr>");
        matches.add(new TransMemoryResultItem(source, target1,
                ApprovedInternal, 3, 85));
        matches.add(new TransMemoryResultItem(source, target2,
                ApprovedInternal, 3, 85));
        matches.add(new TransMemoryResultItem(source, target3,
                TranslatedInternal, 3, 85));
        matches.add(new TransMemoryResultItem(source, target4,
                TranslatedInternal, 3, 85));
        callback.onSuccess(new GetTranslationMemoryResult(action, matches));
        Log.info("EXIT DummyGetTranslationMemoryCommand.execute()");
    }

}
