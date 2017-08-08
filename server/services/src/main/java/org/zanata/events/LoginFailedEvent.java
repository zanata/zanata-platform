package org.zanata.events;

import javax.security.auth.login.LoginException;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class LoginFailedEvent {
    private final LoginException exception;

    public LoginFailedEvent(LoginException exception) {
        this.exception = exception;
    }

    public LoginException getException() {
        return exception;
    }
}
