package org.zanata.webtrans.client.rpc;

import org.zanata.webtrans.shared.rpc.DownloadAllFilesAction;
import org.zanata.webtrans.shared.rpc.DownloadAllFilesResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyDownloadAllFilesCommand implements Command {

    private final DownloadAllFilesAction action;
    private final AsyncCallback<DownloadAllFilesResult> callback;

    public DummyDownloadAllFilesCommand(DownloadAllFilesAction gwcAction,
            AsyncCallback<DownloadAllFilesResult> gwcCallback) {
        this.action = gwcAction;
        this.callback = gwcCallback;
    }

    @Override
    public void execute() {
        Log.info("ENTER DummyDownloadAllFilesCommand.execute()");

        callback.onSuccess(new DownloadAllFilesResult(true, "dummyId"));
        Log.info("EXIT DummyDownloadAllFilesCommand.execute()");
    }

}
