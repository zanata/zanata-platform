package org.zanata.webtrans.client.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface HasQueueDispatch<A extends Action<R>, R extends Result> {
    void executeQueue();

    boolean isQueueEmpty();

    void
            setQueueAndExecute(ArrayList<A> actionQueue,
                    AsyncCallback<R> callback);
}
