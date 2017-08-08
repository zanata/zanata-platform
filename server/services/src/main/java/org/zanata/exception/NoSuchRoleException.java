package org.zanata.exception;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class NoSuchRoleException extends RuntimeException {
    private static final long serialVersionUID = -417844627406864980L;

    public NoSuchRoleException(String message) {
        super(message);
    }

    public NoSuchRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}
