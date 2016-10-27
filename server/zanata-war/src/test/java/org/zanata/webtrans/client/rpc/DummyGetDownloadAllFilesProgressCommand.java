package org.zanata.webtrans.client.rpc;

import org.zanata.webtrans.shared.rpc.GetDownloadAllFilesProgress;
import org.zanata.webtrans.shared.rpc.GetDownloadAllFilesProgressResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetDownloadAllFilesProgressCommand implements Command {

    private final GetDownloadAllFilesProgress action;
    private final AsyncCallback<GetDownloadAllFilesProgressResult> callback;

    public DummyGetDownloadAllFilesProgressCommand(
            GetDownloadAllFilesProgress gwcAction,
            AsyncCallback<GetDownloadAllFilesProgressResult> gwcCallback) {
        this.action = gwcAction;
        this.callback = gwcCallback;
    }

    @Override
    public void execute() {
        Log.info("ENTER DummyGetDownloadAllFilesProgressCommand.execute()");

        callback.onSuccess(new GetDownloadAllFilesProgressResult(100, 100,
                "dummyId"));
        Log.info("EXIT DummyGetDownloadAllFilesProgressCommand.execute()");
    }

}
