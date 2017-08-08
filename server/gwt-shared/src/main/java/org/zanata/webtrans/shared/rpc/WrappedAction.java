package org.zanata.webtrans.shared.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

public class WrappedAction<R extends Result> implements Action<R>,
        IsSerializable {
    // generated
    private static final long serialVersionUID = 4059317550536068556L;

    private Action<R> action;
    private String csrfToken;

    public WrappedAction() {
    }

    public WrappedAction(Action<R> action, String csrfToken) {
        this.action = action;
        this.csrfToken = csrfToken;
    }

    public Action<R> getAction() {
        return action;
    }

    public String getCsrfToken() {
        return csrfToken;
    }

}
