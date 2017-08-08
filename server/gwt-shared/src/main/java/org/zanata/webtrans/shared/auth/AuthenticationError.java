package org.zanata.webtrans.shared.auth;

import net.customware.gwt.dispatch.shared.ActionException;

public class AuthenticationError extends ActionException {

    private static final long serialVersionUID = 1L;

    public AuthenticationError() {
    }

    public AuthenticationError(String message) {
        super(message);
    }

    public AuthenticationError(Throwable e) {
        super(e);
    }

}
