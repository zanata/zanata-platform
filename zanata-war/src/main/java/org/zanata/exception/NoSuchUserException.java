package org.zanata.exception;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class NoSuchUserException extends RuntimeException {
    private static final long serialVersionUID = -6048894722020394330L;

    public NoSuchUserException(String message) {
        super(message);
    }

    public NoSuchUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
