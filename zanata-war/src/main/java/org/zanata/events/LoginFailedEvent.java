package org.zanata.events;

import javax.security.auth.login.LoginException;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class LoginFailedEvent {
    // TODO [CDI] remove
    public static final String EVENT_NAME = "org.zanata.events.LoginFailedEvent";
    private final LoginException exception;


    public LoginFailedEvent(LoginException exception) {
        this.exception = exception;
    }

    public LoginException getException() {
        return exception;
    }
}
