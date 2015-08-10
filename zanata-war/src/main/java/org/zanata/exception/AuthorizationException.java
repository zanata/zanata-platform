package org.zanata.exception;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class AuthorizationException extends RuntimeException {
    private static final long serialVersionUID = -3736905587855150110L;

    public AuthorizationException(String message) {
        super(message);
    }
}
